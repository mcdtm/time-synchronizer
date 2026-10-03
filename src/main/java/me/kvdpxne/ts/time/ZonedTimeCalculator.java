package me.kvdpxne.ts.time;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Computes target time from the real wall-clock time in a given zone.
 * <p>
 * Mapping: real {@code 06:00} → Minecraft tick {@code 0}. One real hour equals
 * one in-game hour. DST transitions are handled transparently by {@link ZoneId}.
 */
public final class ZonedTimeCalculator implements TimeCalculator {

  private static final long TICKS_PER_DAY = 24_000L;
  private static final long TICKS_AT_MIDNIGHT_OFFSET = 18_000L;
  private static final long TICKS_PER_HOUR = 1_000L;
  private static final long SECONDS_PER_MINUTE = 60L;
  private static final long SECONDS_PER_HOUR = 3_600L;

  private final ZoneId zoneId;

  public ZonedTimeCalculator(final ZoneId zoneId) {
    this.zoneId = Objects.requireNonNull(zoneId, "zoneId");
  }

  @Override
  public TimeSnapshot currentSnapshot() {
    final LocalDateTime now = LocalDateTime.now(zoneId);

    final long rawTimeOfDay = now.getHour() * TICKS_PER_HOUR
      + now.getMinute() * TICKS_PER_HOUR / SECONDS_PER_MINUTE
      + now.getSecond() * TICKS_PER_HOUR / SECONDS_PER_HOUR
      + TICKS_AT_MIDNIGHT_OFFSET;

    final long timeOfDay = Math.floorMod(rawTimeOfDay, TICKS_PER_DAY);
    final long epochDay = now.toLocalDate().toEpochDay();
    final long fullTime = epochDay * TICKS_PER_DAY + timeOfDay;

    return new TimeSnapshot(timeOfDay, fullTime);
  }
}