package me.kvdpxne.ts;

import me.kvdpxne.ts.api.ListenerState;
import me.kvdpxne.ts.api.TimeSnapshot;
import me.kvdpxne.ts.api.TimeSynchronizedEvent;
import me.kvdpxne.ts.config.PluginSettings;
import me.kvdpxne.ts.exception.RuntimeNotStartedException;
import me.kvdpxne.ts.listeners.PlayerJoinSyncListener;
import me.kvdpxne.ts.listeners.WorldLifecycleListener;
import me.kvdpxne.ts.listeners.WorldUnloadListener;
import me.kvdpxne.ts.listeners.paper.PaperListeners;
import me.kvdpxne.ts.task.TimeSynchronizationTask;
import me.kvdpxne.ts.time.TimeCalculator;
import me.kvdpxne.ts.time.ZonedTimeCalculator;
import me.kvdpxne.ts.util.EnvironmentSupport;
import me.kvdpxne.ts.util.ThreadGuards;
import me.kvdpxne.ts.world.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Clock;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Owns every component that has to be created, started and torn down between
 * enable cycles. All lifecycle methods must run on the main server thread.
 */
public final class PluginRuntime {

  private static final long FIRST_RUN_DELAY_TICKS = 1L;
  private static final String LISTENER_PLAYER_JOIN = "PlayerJoinSyncListener";
  private static final String LISTENER_WORLD_LIFECYCLE = "WorldLifecycleListener";
  private static final String LISTENER_WORLD_UNLOAD = "WorldUnloadListener";

  /** Persists across reloads so we do not spam the console on every reload. */
  private static final Set<String> LOGGED_CUSTOM_WORLDS = new HashSet<>();

  private final JavaPlugin plugin;
  private final PluginSettings settings;
  private final Clock clock;
  private final Logger logger;
  private final ListenerRegistry listeners = new ListenerRegistry();
  private final TaskRegistry tasks = new TaskRegistry();

  private TimeCalculator calculator;
  private ConfiguredWorldTimeApplier applier;
  private TimeSynchronizationTask syncTask;
  private DaylightCycleGuard guard;
  private Listener paperGameRuleListener;

  public PluginRuntime(final JavaPlugin plugin, final PluginSettings settings) {
    this(plugin, settings, Clock.systemUTC());
  }

  public PluginRuntime(
    final JavaPlugin plugin,
    final PluginSettings settings,
    final Clock clock
  ) {
    this.plugin = Objects.requireNonNull(plugin, "plugin");
    this.settings = Objects.requireNonNull(settings, "settings");
    this.clock = Objects.requireNonNull(clock, "clock");
    this.logger = this.plugin.getLogger();
  }

  public void start() {
    ThreadGuards.requireMainThread("PluginRuntime#start");
    this.calculator = new ZonedTimeCalculator(
      this.settings.zoneId(), this.settings.scale(), this.clock);

    final WorldFilter filter = new SettingsWorldFilter(this.settings);
    this.applier = new ConfiguredWorldTimeApplier(this.settings, filter, this.logger);
    final var disabler = new DaylightCycleDisabler(filter, this.logger);

    this.syncTask = new TimeSynchronizationTask(
      this.calculator, this.applier,
      (snapshot, worlds) -> Bukkit.getPluginManager()
        .callEvent(new TimeSynchronizedEvent(snapshot, worlds)),
      this.settings.eventThrottleMillis());

    disabler.disableForManagedWorlds();
    this.logCustomWorlds(filter);
    this.configureDaylightProtection(filter);

    this.listeners.add(LISTENER_WORLD_LIFECYCLE, new ManagedListener(
      new WorldLifecycleListener(this.calculator, this.applier, disabler, this.logger),
      this.plugin, LISTENER_WORLD_LIFECYCLE,
      this.settings.worldLifecycleListenerState(), this.logger));

    this.listeners.add(LISTENER_PLAYER_JOIN, new ManagedListener(
      new PlayerJoinSyncListener(this.buildJoinStrategy()),
      this.plugin, LISTENER_PLAYER_JOIN,
      this.settings.playerJoinSyncListenerState(), this.logger));

    // Single cleanup chain: any component holding per-world state registers here.
    this.listeners.add(LISTENER_WORLD_UNLOAD, new ManagedListener(
      new WorldUnloadListener(this.buildWorldCleanup()),
      this.plugin, LISTENER_WORLD_UNLOAD, ListenerState.ENABLED, this.logger));

    this.listeners.applyAllInitialStates();

    this.syncTask.run();
    this.tasks.track(Bukkit.getScheduler().runTaskTimer(
      this.plugin, this.syncTask, FIRST_RUN_DELAY_TICKS, this.settings.updateIntervalTicks()));

    this.logger.info(() -> "Runtime started [zone=" + this.settings.zoneId()
      + ", scale=" + this.settings.scale()
      + ", daySync=" + this.settings.daySyncMode()
      + ", intervalTicks=" + this.settings.updateIntervalTicks()
      + ", eventThrottleMs=" + this.settings.eventThrottleMillis() + "]");
  }

  public void stop() {
    ThreadGuards.requireMainThread("PluginRuntime#stop");
    this.tasks.cancelAll();
    this.listeners.stopAll();
    if (null != this.paperGameRuleListener) {
      HandlerList.unregisterAll(this.paperGameRuleListener);
      this.paperGameRuleListener = null;
    }
    if (null != this.guard) {
      this.guard.reset();
      this.guard = null;
    }
    this.calculator = null;
    this.applier = null;
    this.syncTask = null;
    this.logger.info("Runtime stopped.");
  }

  public PluginSettings settings() {
    return this.settings;
  }

  /** Forces an immediate sync; used by {@code /timesync syncnow}. */
  public void syncNow() {
    ThreadGuards.requireMainThread("PluginRuntime#syncNow");
    if (null == this.syncTask) {
      throw new RuntimeNotStartedException();
    }
    this.syncTask.run();
  }

  /** @return the current snapshot, or empty when the runtime is not started. */
  public TimeSnapshot currentSnapshot() {
    final var calc = this.calculator;
    if (null == calc) {
      throw new RuntimeNotStartedException();
    }
    return calc.currentSnapshot();
  }

  public boolean isPlayerJoinSyncListenerEnabled() {
    return this.listeners.require(LISTENER_PLAYER_JOIN).isEnabled();
  }

  public boolean isPlayerJoinSyncListenerMutable() {
    return this.listeners.require(LISTENER_PLAYER_JOIN).isMutable();
  }

  public void setPlayerJoinSyncListenerEnabled(final boolean enabled) {
    this.listeners.require(LISTENER_PLAYER_JOIN).setEnabled(enabled);
  }

  public boolean isWorldLifecycleListenerEnabled() {
    return this.listeners.require(LISTENER_WORLD_LIFECYCLE).isEnabled();
  }

  public boolean isWorldLifecycleListenerMutable() {
    return this.listeners.require(LISTENER_WORLD_LIFECYCLE).isMutable();
  }

  public void setWorldLifecycleListenerEnabled(final boolean enabled) {
    this.listeners.require(LISTENER_WORLD_LIFECYCLE).setEnabled(enabled);
  }

  private Consumer<String> buildWorldCleanup() {
    return name -> {
      this.applier.onWorldUnload(name);
      final var g = this.guard;
      if (null != g) {
        g.onWorldUnload(name);
      }
      LOGGED_CUSTOM_WORLDS.remove(name);
    };
  }

  private Consumer<Player> buildJoinStrategy() {
    if (this.settings.isDaySyncEnabled()) {
      return player -> this.syncTask.run();
    }
    return player -> this.applier.applyToWorld(
      player.getWorld(), this.calculator.currentSnapshot());
  }

  private void configureDaylightProtection(final WorldFilter filter) {
    if (!this.settings.daylightGuardEnabled()) {
      this.logger.info("Daylight protection disabled by configuration.");
      return;
    }
    if (PaperListeners.isGameRuleChangeEventAvailable()) {
      this.installPaperListener(filter);
    } else {
      this.installFallbackGuard(filter);
    }
  }

  private void installPaperListener(final WorldFilter filter) {
    PaperListeners.createGameRuleChangeListener(filter).ifPresent(listener -> {
      Bukkit.getPluginManager().registerEvents(listener, this.plugin);
      this.paperGameRuleListener = listener;
      this.logger.info("Daylight protection: Paper WorldGameRuleChangeEvent (reactive).");
    });
  }

  private void installFallbackGuard(final WorldFilter filter) {
    this.guard = new DaylightCycleGuard(filter, this.logger);
    this.tasks.track(Bukkit.getScheduler().runTaskTimer(
      this.plugin, this.guard,
      FIRST_RUN_DELAY_TICKS, this.settings.fallbackGuardIntervalTicks()));
    this.logger.info(() ->
      "Daylight protection: periodic fallback guard, interval="
        + this.settings.fallbackGuardIntervalTicks() + " ticks "
        + "(Paper event not available on this server).");
  }

  private void logCustomWorlds(final WorldFilter filter) {
    for (final var world : Bukkit.getWorlds()) {
      if (!EnvironmentSupport.isCustom(world.getEnvironment())) {
        continue;
      }
      final var managed = filter.isManaged(world);
      final var name = world.getName();
      if (LOGGED_CUSTOM_WORLDS.add(name)) {
        this.logger.info(() ->
          "Custom world '" + name + "' detected; managed=" + managed);
      } else {
        this.logger.fine(() ->
          "Custom world '" + name + "' re-detected; managed=" + managed);
      }
    }
  }
}