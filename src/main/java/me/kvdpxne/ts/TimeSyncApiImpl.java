package me.kvdpxne.ts;

import me.kvdpxne.ts.api.ReloadResult;
import me.kvdpxne.ts.api.TimeSnapshot;
import me.kvdpxne.ts.api.TimeSyncApi;
import me.kvdpxne.ts.config.PluginSettings;

import java.time.ZoneId;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Public {@link TimeSyncApi} adapter. All methods delegate to the active
 * {@link PluginRuntime}, which is resolved lazily so this class stays valid
 * across reloads.
 */
public final class TimeSyncApiImpl implements TimeSyncApi {

  private final PluginBootstrap bootstrap;

  public TimeSyncApiImpl(final PluginBootstrap bootstrap) {
    this.bootstrap = Objects.requireNonNull(bootstrap, "bootstrap");
  }

  @Override
  public boolean isPlayerJoinSyncListenerEnabled() {
    return this.bootstrap.runtime().isPlayerJoinSyncListenerEnabled();
  }

  @Override
  public boolean isPlayerJoinSyncListenerMutable() {
    return this.bootstrap.runtime().isPlayerJoinSyncListenerMutable();
  }

  @Override
  public void setPlayerJoinSyncListenerEnabled(final boolean enabled) {
    this.bootstrap.runtime().setPlayerJoinSyncListenerEnabled(enabled);
  }

  @Override
  public boolean isWorldLifecycleListenerEnabled() {
    return this.bootstrap.runtime().isWorldLifecycleListenerEnabled();
  }

  @Override
  public boolean isWorldLifecycleListenerMutable() {
    return this.bootstrap.runtime().isWorldLifecycleListenerMutable();
  }

  @Override
  public void setWorldLifecycleListenerEnabled(final boolean enabled) {
    this.bootstrap.runtime().setWorldLifecycleListenerEnabled(enabled);
  }

  @Override
  public TimeSnapshot currentSnapshot() {
    return this.bootstrap.runtime().currentSnapshot();
  }

  @Override
  public PluginSettings settings() {
    return this.bootstrap.runtime().settings();
  }

  @Override
  public void syncNow() {
    this.bootstrap.runtime().syncNow();
  }

  @Override
  public CompletableFuture<ReloadResult> setZoneAsync(final ZoneId zoneId) {
    return this.bootstrap.setZoneAndReloadAsync(zoneId);
  }

  @Override
  public CompletableFuture<ReloadResult> reloadAsync() {
    return this.bootstrap.reloadAsync();
  }
}