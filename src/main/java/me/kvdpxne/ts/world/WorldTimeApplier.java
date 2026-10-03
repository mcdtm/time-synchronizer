package me.kvdpxne.ts.world;

import me.kvdpxne.ts.time.TimeSnapshot;

/**
 * Strategy for applying a {@link TimeSnapshot} to one or more worlds.
 */
@FunctionalInterface
public interface WorldTimeApplier {

  void apply(TimeSnapshot snapshot);
}