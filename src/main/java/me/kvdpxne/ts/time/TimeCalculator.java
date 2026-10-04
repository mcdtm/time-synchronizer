package me.kvdpxne.ts.time;

import me.kvdpxne.ts.api.TimeSnapshot;

/**
 * Strategy for producing the current target {@link TimeSnapshot}.
 */
@FunctionalInterface
public interface TimeCalculator {

  TimeSnapshot currentSnapshot();
}