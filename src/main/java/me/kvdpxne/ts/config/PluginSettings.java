package me.kvdpxne.ts.config;

import me.kvdpxne.ts.api.ListenerState;

import java.time.ZoneId;
import java.util.Objects;
import java.util.Set;

public record PluginSettings(
  int configVersion,
  ZoneId zoneId,
  DaySyncMode daySyncMode,
  TimeApplyMode timeApplyMode,
  double scale,
  long updateIntervalTicks,
  Set<String> excludedWorlds,
  ListenerState playerJoinSyncListenerState,
  ListenerState worldLifecycleListenerState,
  boolean daylightGuardEnabled,
  long fallbackGuardIntervalTicks,
  long eventThrottleMillis,
  long timeJumpWarningThresholdTicks,
  boolean warnNonPaper,
  boolean bStatsEnabled
) {

  public PluginSettings {
    Objects.requireNonNull(zoneId, "zoneId");
    Objects.requireNonNull(daySyncMode, "daySyncMode");
    Objects.requireNonNull(timeApplyMode, "timeApplyMode");
    Objects.requireNonNull(playerJoinSyncListenerState, "playerJoinSyncListenerState");
    Objects.requireNonNull(worldLifecycleListenerState, "worldLifecycleListenerState");
    excludedWorlds = Set.copyOf(Objects.requireNonNull(excludedWorlds, "excludedWorlds"));
    if (0.0 >= scale) {
      throw new IllegalArgumentException("scale must be positive: " + scale);
    }
    if (0L >= updateIntervalTicks) {
      throw new IllegalArgumentException("updateIntervalTicks must be positive");
    }
    if (0L >= fallbackGuardIntervalTicks) {
      throw new IllegalArgumentException("fallbackGuardIntervalTicks must be positive");
    }
    if (0L > eventThrottleMillis) {
      throw new IllegalArgumentException("eventThrottleMillis must be non-negative");
    }
    if (0L > timeJumpWarningThresholdTicks) {
      throw new IllegalArgumentException("timeJumpWarningThresholdTicks must be non-negative");
    }
  }

  public boolean isDaySyncEnabled() {
    return DaySyncMode.DISABLED != this.daySyncMode;
  }
}