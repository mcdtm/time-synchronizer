package me.kvdpxne.ts.world;

import me.kvdpxne.ts.ThrottledLogger;
import me.kvdpxne.ts.api.TimeSnapshot;
import me.kvdpxne.ts.config.DaySyncMode;
import me.kvdpxne.ts.config.PluginSettings;
import me.kvdpxne.ts.config.TimeApplyMode;
import me.kvdpxne.ts.listeners.WorldUnloadListener;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Applies a {@link TimeSnapshot} according to {@link PluginSettings}.
 * <p>
 * Must be called on the main server thread (touches {@link World}).
 */
public final class ConfiguredWorldTimeApplier implements WorldTimeApplier {

  private static final long TICKS_PER_DAY = 24_000L;
  private static final long LOG_THROTTLE_MILLIS = 30_000L;

  private final WorldFilter filter;
  private final DaySyncMode daySyncMode;
  private final TimeApplyMode timeApplyMode;
  private final long jumpWarningThresholdTicks;
  private final ThrottledLogger throttled;

  public ConfiguredWorldTimeApplier(
    final PluginSettings settings,
    final WorldFilter filter,
    final Logger logger
  ) {
    Objects.requireNonNull(settings, "settings");
    this.filter = Objects.requireNonNull(filter, "filter");
    this.daySyncMode = settings.daySyncMode();
    this.timeApplyMode = settings.timeApplyMode();
    this.jumpWarningThresholdTicks = settings.timeJumpWarningThresholdTicks();
    this.throttled = new ThrottledLogger(
      Objects.requireNonNull(logger, "logger"), LOG_THROTTLE_MILLIS);
  }

  @Override
  public List<World> apply(final TimeSnapshot snapshot) {
    Objects.requireNonNull(snapshot, "snapshot");
    final var affected = new ArrayList<World>();
    for (final World world : Bukkit.getWorlds()) {
      if (this.applyToWorld(world, snapshot)) {
        affected.add(world);
      }
    }
    return List.copyOf(affected);
  }

  /**
   * @return {@code true} when the world was actually modified
   */
  public boolean applyToWorld(final World world, final TimeSnapshot snapshot) {
    if (!this.filter.isManaged(world)) {
      return false;
    }
    final long before = world.getFullTime();
    this.doApply(world, snapshot);
    this.warnIfLargeJump(world, before, world.getFullTime());
    return true;
  }

  /** Called by {@link WorldUnloadListener}. */
  public void onWorldUnload(final String worldName) {
    this.throttled.forget(worldName);
  }

  private void doApply(final World world, final TimeSnapshot snapshot) {
    if (DaySyncMode.DISABLED == this.daySyncMode) {
      this.applyTimeOfDayOnly(world, snapshot);
    } else {
      this.applyFullTimeIfChanged(world, snapshot.fullTime());
    }
  }

  private void applyTimeOfDayOnly(final World world, final TimeSnapshot snapshot) {
    switch (this.timeApplyMode) {
      case SET_TIME -> this.applyTimeIfChanged(world, snapshot.timeOfDay());
      case SET_FULL_TIME -> {
        final long currentDay = Math.floorDiv(world.getFullTime(), TICKS_PER_DAY);
        this.applyFullTimeIfChanged(world, currentDay * TICKS_PER_DAY + snapshot.timeOfDay());
      }
    }
  }

  private void applyTimeIfChanged(final World world, final long timeOfDay) {
    if (world.getTime() != timeOfDay) {
      world.setTime(timeOfDay);
    }
  }

  private void applyFullTimeIfChanged(final World world, final long fullTime) {
    if (world.getFullTime() != fullTime) {
      world.setFullTime(fullTime);
    }
  }

  private void warnIfLargeJump(final World world, final long before, final long after) {
    if (0L >= this.jumpWarningThresholdTicks) {
      return;
    }
    final long delta = Math.abs(after - before);
    if (delta >= this.jumpWarningThresholdTicks) {
      this.throttled.warn(world.getName(), () ->
        "Large time jump in world '" + world.getName() + "': " + delta + " ticks ("
          + before + " -> " + after + ")");
    }
  }
}