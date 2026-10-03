package me.kvdpxne.ts.config;

import java.time.ZoneId;
import java.util.Objects;

/**
 * Immutable, validated plugin settings loaded from the properties file.
 *
 * @param zoneId        IANA zone id used to compute the in-game time-of-day
 * @param daySyncMode   how the day counter is synchronized with the real calendar
 * @param timeApplyMode which API is used when day sync is disabled
 */
public record PluginSettings(
  ZoneId zoneId,
  DaySyncMode daySyncMode,
  TimeApplyMode timeApplyMode
) {

  public PluginSettings {
    Objects.requireNonNull(zoneId, "zoneId");
    Objects.requireNonNull(daySyncMode, "daySyncMode");
    Objects.requireNonNull(timeApplyMode, "timeApplyMode");
  }

  /**
   * @return {@code true} when day counter must be derived from the real calendar
   */
  public boolean isDaySyncEnabled() {
    return DaySyncMode.DISABLED != daySyncMode;
  }

  /**
   * @return {@code true} when the applier should touch every world, not only Overworld
   */
  public boolean appliesToAllWorlds() {
    return DaySyncMode.ALL_WORLDS == daySyncMode;
  }
}