package me.kvdpxne.ts.config;

import me.kvdpxne.ts.util.parser.EnumParser;

import java.util.Optional;

/**
 * Which Bukkit API is used when applying time to a world while day-sync is disabled.
 */
public enum TimeApplyMode {

  /** {@code World#setTime}. Preserves world age and moon phases. */
  SET_TIME,

  /** {@code World#setFullTime} computed from the world's current day. */
  SET_FULL_TIME;

  /**
   * @return the parsed mode, or {@link Optional#empty()} when the input is unknown or null
   */
  public static Optional<TimeApplyMode> tryFromString(final String raw) {
    return EnumParser.tryParse(TimeApplyMode.class, raw);
  }
}