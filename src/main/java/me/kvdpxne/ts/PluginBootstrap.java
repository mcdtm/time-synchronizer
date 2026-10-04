package me.kvdpxne.ts;

import me.kvdpxne.ts.api.ReloadResult;
import me.kvdpxne.ts.config.PluginSettings;
import me.kvdpxne.ts.config.PropertiesSettingsLoader;
import me.kvdpxne.ts.config.SettingsLoader;
import me.kvdpxne.ts.config.SettingsWriter;
import me.kvdpxne.ts.exception.ConfigurationLoadException;
import me.kvdpxne.ts.exception.RuntimeNotStartedException;
import me.kvdpxne.ts.util.ThreadGuards;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Orchestrates plugin startup and reloads.
 * <p>
 * <b>Async strategy:</b> only blocking I/O runs on virtual threads - file reads
 * and future external sources (DB, HTTP). Everything that touches the Bukkit
 * API (runtime construction, event registration, scheduler tasks, world state)
 * runs on the main server thread. This split exists because:
 * <ul>
 *   <li>Bukkit API is not thread-safe and enforces main-thread access for
 *       most operations; violating this throws or corrupts state.</li>
 *   <li>Reading a small properties file is measurable only in aggregate; the
 *       real benefit of async is <i>not blocking a live server</i> while an
 *       admin reloads config mid-game.</li>
 *   <li>The swap itself is cheap (pointer assignment) and must be atomic with
 *       respect to the runtime's stop() call.</li>
 * </ul>
 * Startup ({@code onEnable}) deliberately performs synchronous load: the
 * server expects the plugin to be ready when {@code onEnable} returns, and
 * there is no live state to protect. Async there would only delay readiness.
 */
public final class PluginBootstrap implements AutoCloseable {

  private static final long RELOAD_TIMEOUT_SECONDS = 30L;

  private final JavaPlugin plugin;
  private final Logger logger;
  private final Clock clock;
  private final SettingsLoader settingsLoader;
  private final SettingsWriter settingsWriter;
  private final ExecutorService ioExecutor;
  private final Executor mainThreadExecutor;
  private final ReentrantLock reloadLock = new ReentrantLock();
  private final AtomicReference<PluginRuntime> runtime = new AtomicReference<>();

  public PluginBootstrap(final JavaPlugin plugin, final Clock clock) {
    this.plugin = Objects.requireNonNull(plugin, "plugin");
    this.clock = Objects.requireNonNull(clock, "clock");
    this.logger = this.plugin.getLogger();
    final var loader = new PropertiesSettingsLoader(this.plugin.getDataFolder(), this.logger);
    this.settingsLoader = loader;
    this.settingsWriter = loader;
    this.ioExecutor = Executors.newVirtualThreadPerTaskExecutor();
    this.mainThreadExecutor = this.buildMainThreadExecutor();
  }

  public PluginSettings loadInitialSettingsBlocking() throws ConfigurationLoadException {
    ThreadGuards.requireMainThread("loadInitialSettingsBlocking");
    try {
      // Synchronous by design: onEnable must return with a ready plugin.
      return this.settingsLoader.load();
    } catch (final ConfigurationLoadException e) {
      throw e;
    } catch (final RuntimeException e) {
      throw new ConfigurationLoadException("Initial settings load failed", e);
    }
  }

  public void startRuntime(final PluginSettings settings) {
    ThreadGuards.requireMainThread("startRuntime");
    final var newRuntime = new PluginRuntime(this.plugin, settings, this.clock);
    newRuntime.start();
    this.runtime.set(newRuntime);
  }

  /**
   * @throws RuntimeNotStartedException when the plugin is not enabled or has
   *         just been disabled. Treat as a programming error in lifecycle code.
   */
  public PluginRuntime runtime() {
    final var rt = this.runtime.get();
    if (null == rt) {
      throw new RuntimeNotStartedException();
    }
    return rt;
  }

  /** @return {@code true} when a runtime is currently active. */
  public boolean hasRuntime() {
    return null != this.runtime.get();
  }

  public CompletableFuture<ReloadResult> setZoneAndReloadAsync(final ZoneId zoneId) {
    Objects.requireNonNull(zoneId, "zoneId");
    return CompletableFuture
      .supplyAsync(() -> this.persistZone(zoneId), this.ioExecutor)
      .thenComposeAsync(this::swapRuntime, this.mainThreadExecutor)
      .orTimeout(RELOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)
      .exceptionally(this::onReloadTimeout);
  }

  private LoadOutcome persistZone(final ZoneId zoneId) {
    try {
      this.settingsWriter.updateZone(zoneId);
      return LoadOutcome.success(this.settingsLoader.load());
    } catch (final ConfigurationLoadException e) {
      this.logger.log(Level.SEVERE, "Failed to persist zone", e);
      return LoadOutcome.failure(e.getMessage());
    }
  }

  /**
   * Asynchronously reloads settings and swaps the runtime.
   * <p>
   * <b>Async here is intentional.</b> A live server is running; the admin
   * invoked a command and other players are online. Blocking the main thread
   * for I/O (or a future DB/HTTP source) would hurt TPS. This method returns
   * immediately; the swap happens on the main thread and the future completes
   * there, so callers can safely send messages to a {@code CommandSender}.
   * <p>
   * Reloads are serialized: a concurrent call fails fast with a
   * {@link ReloadResult#failure(String)} rather than queuing.
   */
  public CompletableFuture<ReloadResult> reloadAsync() {
    return CompletableFuture
      .supplyAsync(this::loadSettingsOrFailure, this.ioExecutor)
      .thenComposeAsync(this::swapRuntime, this.mainThreadExecutor)
      .orTimeout(RELOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)
      .exceptionally(this::onReloadTimeout);
  }

  /** Idempotent; safe to call from {@code onDisable}. */
  public void stopRuntime() {
    final var rt = this.runtime.getAndSet(null);
    if (null != rt) {
      rt.stop();
    }
  }

  @Override
  public void close() {
    this.ioExecutor.shutdownNow();
  }

  private CompletableFuture<ReloadResult> swapRuntime(final LoadOutcome outcome) {
    if (!outcome.isSuccess()) {
      return CompletableFuture.completedFuture(ReloadResult.failure(outcome.error()));
    }
    if (!this.reloadLock.tryLock()) {
      return CompletableFuture.completedFuture(
        ReloadResult.failure("Another reload is already in progress"));
    }
    try {
      // Start the new runtime BEFORE stopping the old one. If start() throws,
      // the old runtime is still live and the server keeps running on the
      // previous configuration - no downtime window.
      final var newRuntime = new PluginRuntime(this.plugin, outcome.settings(), this.clock);
      newRuntime.start();

      // Atomic swap. getAndSet both stores and returns the previous value in
      // one operation, avoiding a get/set race if another thread reads runtime
      // between our two calls.
      final var oldRuntime = this.runtime.getAndSet(newRuntime);
      if (null != oldRuntime) {
        oldRuntime.stop();
      }

      return CompletableFuture.completedFuture(ReloadResult.success(
        "Configuration reloaded (version " + outcome.settings().configVersion() + ")"));
    } catch (final RuntimeException e) {
      this.logger.log(Level.SEVERE, "Runtime swap failed; keeping previous runtime", e);
      return CompletableFuture.completedFuture(
        ReloadResult.failure("Reload failed: " + e.getMessage()));
    } finally {
      this.reloadLock.unlock();
    }
  }

  private ReloadResult onReloadTimeout(final Throwable ex) {
    if (ex instanceof TimeoutException) {
      this.logger.warning(() ->
        "Reload timed out after " + RELOAD_TIMEOUT_SECONDS + "s; "
          + "main thread may be blocked. Server is still running on the previous config.");
      return ReloadResult.failure(
        "Reload timed out after " + RELOAD_TIMEOUT_SECONDS + "s");
    }
    this.logger.log(Level.SEVERE, "Reload failed unexpectedly", ex);
    return ReloadResult.failure("Reload failed: " + ex.getMessage());
  }

  private LoadOutcome loadSettingsOrFailure() {
    try {
      return LoadOutcome.success(this.settingsLoader.load());
    } catch (final ConfigurationLoadException e) {
      this.logger.log(Level.SEVERE, "Failed to load settings", e);
      return LoadOutcome.failure(e.getMessage());
    }
  }

  private Executor buildMainThreadExecutor() {
    return task -> {
      if (Bukkit.isPrimaryThread()) {
        task.run();
      } else {
        Bukkit.getScheduler().runTask(this.plugin, task);
      }
    };
  }

  private record LoadOutcome(PluginSettings settings, String error) {
    static LoadOutcome success(final PluginSettings settings) {
      return new LoadOutcome(Objects.requireNonNull(settings, "settings"), null);
    }

    static LoadOutcome failure(final String error) {
      return new LoadOutcome(null, Objects.requireNonNull(error, "error"));
    }

    boolean isSuccess() {
      return null != this.settings;
    }
  }
}