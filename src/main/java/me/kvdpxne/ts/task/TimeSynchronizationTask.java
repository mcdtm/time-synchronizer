package me.kvdpxne.ts.task;

import me.kvdpxne.ts.time.TimeCalculator;
import me.kvdpxne.ts.world.WorldTimeApplier;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Scheduler task that pulls a snapshot, applies it, and notifies listeners.
 * When {@code eventThrottleMillis > 0}, {@link TimeAppliedListener} fires at
 * most once per window. Throttled events are dropped silently; the applier
 * still runs on every tick.
 */
public final class TimeSynchronizationTask implements Runnable {

  private final TimeCalculator calculator;
  private final WorldTimeApplier applier;
  private final TimeAppliedListener listener;
  private final long eventThrottleMillis;
  private final AtomicLong lastEventFiredAt = new AtomicLong(0L);

  public TimeSynchronizationTask(
    final TimeCalculator calculator,
    final WorldTimeApplier applier,
    final TimeAppliedListener listener,
    final long eventThrottleMillis
  ) {
    this.calculator = Objects.requireNonNull(calculator, "calculator");
    this.applier = Objects.requireNonNull(applier, "applier");
    this.listener = Objects.requireNonNull(listener, "listener");
    if (0L > eventThrottleMillis) {
      throw new IllegalArgumentException("eventThrottleMillis must be non-negative");
    }
    this.eventThrottleMillis = eventThrottleMillis;
  }

  @Override
  public void run() {
    final var snapshot = this.calculator.currentSnapshot();
    final var affected = this.applier.apply(snapshot);
    if (!affected.isEmpty() && this.shouldFireEvent()) {
      this.listener.onTimeApplied(snapshot, affected);
    }
  }

  private boolean shouldFireEvent() {
    if (0L == this.eventThrottleMillis) {
      return true;
    }
    final long now = System.currentTimeMillis();
    final long last = this.lastEventFiredAt.get();
    if (now - last < this.eventThrottleMillis) {
      return false;
    }
    return this.lastEventFiredAt.compareAndSet(last, now);
  }
}