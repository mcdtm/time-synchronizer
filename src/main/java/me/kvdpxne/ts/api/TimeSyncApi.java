package me.kvdpxne.ts.api;

import me.kvdpxne.ts.config.PluginSettings;
import me.kvdpxne.ts.exception.RuntimeNotStartedException;

import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;

/**
 * Public service exposed through Bukkit's {@code ServicesManager}.
 * <p>
 * Listener-management methods must be called from the main server thread.
 * {@link #reloadAsync()} is safe from any thread.
 */
public interface TimeSyncApi {

  boolean isPlayerJoinSyncListenerEnabled();

  boolean isPlayerJoinSyncListenerMutable();

  void setPlayerJoinSyncListenerEnabled(boolean enabled);

  boolean isWorldLifecycleListenerEnabled();

  boolean isWorldLifecycleListenerMutable();

  void setWorldLifecycleListenerEnabled(boolean enabled);

  /**
   * @throws RuntimeNotStartedException when the plugin
   *                                    is not enabled or has just been disabled
   */
  TimeSnapshot currentSnapshot();

  /**
   * @throws RuntimeNotStartedException when the plugin is not enabled
   */
  PluginSettings settings();

  /**
   * Forces an immediate sync; bypasses the scheduler interval.
   * Must be called from the main thread.
   *
   * @throws RuntimeNotStartedException when the plugin is not enabled
   */
  void syncNow();

  /**
   * Rewrites the {@code time-zone} key and reloads the runtime.
   * Async; the future completes on the main thread.
   *
   * @throws RuntimeNotStartedException in {@link #currentSnapshot()} and other
   *         runtime-bound methods if the plugin is not enabled
   */
  CompletableFuture<ReloadResult> setZoneAsync(ZoneId zoneId);

  /**
   * Reloads settings and swaps the runtime without blocking the caller.
   * The returned future completes on the main server thread.
   */
  CompletableFuture<ReloadResult> reloadAsync();
}