package me.kvdpxne.ts.api;

/**
 * Immutable snapshot of a target in-game time. Part of the public API.
 *
 * @param timeOfDay time-of-day in ticks, within {@code [0, 24000)}
 * @param fullTime  absolute time in ticks, aligned to the real-world epoch day
 */
public record TimeSnapshot(long timeOfDay, long fullTime) {

  public TimeSnapshot {
    if (0L > timeOfDay || 24_000L <= timeOfDay) {
      throw new IllegalArgumentException("timeOfDay out of range: " + timeOfDay);
    }
    if (0L > fullTime) {
      throw new IllegalArgumentException("fullTime must be non-negative: " + fullTime);
    }
  }
}