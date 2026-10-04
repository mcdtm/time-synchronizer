package me.kvdpxne.ts.time;

import me.kvdpxne.ts.api.TimeSnapshot;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Computes the target snapshot from real wall-clock time.
 * <p>
 * Mapping is anchored so real {@code 06:00} == Minecraft {@code 06:00} (dawn).
 * A scale of {@code 1.0} preserves the classic {@code 1 real hour = 1 in-game hour}
 * ratio; other values speed the in-game clock up or slow it down proportionally.
 */
public final class ZonedTimeCalculator implements TimeCalculator {

  private static final long TICKS_PER_DAY = 24_000L;
  private static final long TICKS_AT_MIDNIGHT_OFFSET = 18_000L;
  private static final long TICKS_PER_HOUR = 1_000L;
  private static final long SECONDS_PER_MINUTE = 60L;
  private static final long SECONDS_PER_HOUR = 3_600L;
  private static final double HOURS_PER_DAY = 24.0d;
  private static final double ANCHOR_HOUR = 6.0d;

  private final ZoneId zoneId;
  private final double scale;
  private final Clock clock;

  public ZonedTimeCalculator(
    final ZoneId zoneId,
    final double scale,
    final Clock clock
  ) {
    if (0.0 >= scale) {
      throw new IllegalArgumentException("scale must be positive: " + scale);
    }
    this.zoneId = Objects.requireNonNull(zoneId, "zoneId");
    this.clock = Objects.requireNonNull(clock, "clock");
    this.scale = scale;
  }

  @Override
  public TimeSnapshot currentSnapshot() {
    final var now = LocalDateTime.ofInstant(this.clock.instant(), this.zoneId);
    final long timeOfDay = 1.0d == this.scale
      ? this.computeVanillaTimeOfDay(now)
      : this.computeScaledTimeOfDay(now);
    final long fullTime = now.toLocalDate().toEpochDay() * TICKS_PER_DAY + timeOfDay;
    return new TimeSnapshot(timeOfDay, fullTime);
  }

  private long computeVanillaTimeOfDay(final LocalDateTime now) {
    final long raw = now.getHour() * TICKS_PER_HOUR
      + now.getMinute() * TICKS_PER_HOUR / SECONDS_PER_MINUTE
      + now.getSecond() * TICKS_PER_HOUR / SECONDS_PER_HOUR
      + TICKS_AT_MIDNIGHT_OFFSET;
    return Math.floorMod(raw, TICKS_PER_DAY);
  }

  private long computeScaledTimeOfDay(final LocalDateTime now) {
    final double realHours = now.getHour()
      + now.getMinute() / 60.0d
      + now.getSecond() / 3_600.0d;
    final double shifted = (realHours - ANCHOR_HOUR) * this.scale + ANCHOR_HOUR;
    final double wrapped = ((shifted % HOURS_PER_DAY) + HOURS_PER_DAY) % HOURS_PER_DAY;
    final long scaledTicks = Math.round(wrapped * TICKS_PER_HOUR);
    return Math.floorMod(scaledTicks + TICKS_AT_MIDNIGHT_OFFSET, TICKS_PER_DAY);
  }
}