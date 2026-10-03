package me.kvdpxne.ts.time;

/**
 * Immutable snapshot of a wall-clock time within a single day.
 *
 * @param hours   hour of day in range {@code [0, 23]}
 * @param minutes minute of hour in range {@code [0, 59]}
 * @param seconds second of minute in range {@code [0, 59]}
 */
public record TimeOfDay(int hours, int minutes, int seconds) {

  public TimeOfDay {
    if (0 > hours || 23 < hours) {
      throw new IllegalArgumentException("hours out of range: " + hours);
    }
    if (0 > minutes || 59 < minutes) {
      throw new IllegalArgumentException("minutes out of range: " + minutes);
    }
    if (0 > seconds || 59 < seconds) {
      throw new IllegalArgumentException("seconds out of range: " + seconds);
    }
  }
}