package me.kvdpxne.ts.task;

import me.kvdpxne.ts.time.TimeCalculator;
import me.kvdpxne.ts.time.TimeSnapshot;
import me.kvdpxne.ts.world.WorldTimeApplier;

import java.util.Objects;

/**
 * Scheduler task that pulls a {@link TimeSnapshot}
 * from a calculator and forwards it to an applier.
 * <p>
 * Knows nothing about worlds, zones or configuration - pure orchestration.
 */
public final class TimeSynchronizationTask implements Runnable {

  private final TimeCalculator calculator;
  private final WorldTimeApplier applier;

  public TimeSynchronizationTask(
    final TimeCalculator calculator,
    final WorldTimeApplier applier
  ) {
    this.calculator = Objects.requireNonNull(calculator, "calculator");
    this.applier = Objects.requireNonNull(applier, "applier");
  }

  @Override
  public void run() {
    applier.apply(calculator.currentSnapshot());
  }
}