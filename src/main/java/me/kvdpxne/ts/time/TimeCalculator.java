package me.kvdpxne.ts.time;

/**
 * Strategy for producing the current target {@link TimeSnapshot}.
 */
@FunctionalInterface
public interface TimeCalculator {

  TimeSnapshot currentSnapshot();
}