package me.kvdpxne.ts.world;

import me.kvdpxne.ts.config.DaySyncMode;
import me.kvdpxne.ts.config.PluginSettings;
import me.kvdpxne.ts.config.TimeApplyMode;
import me.kvdpxne.ts.time.TimeSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.Objects;

/**
 * Applies a {@link TimeSnapshot} according to {@link PluginSettings}.
 * <p>
 * Behaviour matrix:
 * <pre>
 *   day-sync-mode  time-apply-mode  effect
 *   -------------  ---------------  ----------------------------------------------
 *   DISABLED       SET_TIME         world.setTime(timeOfDay) on Overworld
 *   DISABLED       SET_FULL_TIME    world.setFullTime(currentDay*24000 + timeOfDay)
 *   OVERWORLD      (any)            world.setFullTime(epochDay*24000 + timeOfDay)
 *   ALL_WORLDS     (any)            same, but every world
 * </pre>
 */
public final class ConfiguredWorldTimeApplier implements WorldTimeApplier {

  private static final long TICKS_PER_DAY = 24_000L;

  private final DaySyncMode daySyncMode;
  private final TimeApplyMode timeApplyMode;

  public ConfiguredWorldTimeApplier(final PluginSettings settings) {
    Objects.requireNonNull(settings, "settings");
    this.daySyncMode = settings.daySyncMode();
    this.timeApplyMode = settings.timeApplyMode();
  }

  @Override
  public void apply(final TimeSnapshot snapshot) {
    Objects.requireNonNull(snapshot, "snapshot");
    for (final World world : Bukkit.getWorlds()) {
      if (shouldApply(world)) {
        applyTo(world, snapshot);
      }
    }
  }

  private boolean shouldApply(final World world) {
    return switch (daySyncMode) {
      case DISABLED, OVERWORLD -> World.Environment.NORMAL == world.getEnvironment();
      case ALL_WORLDS -> true;
    };
  }

  private void applyTo(final World world, final TimeSnapshot snapshot) {
    if (DaySyncMode.DISABLED == daySyncMode) {
      applyTimeOfDayOnly(world, snapshot);
    } else {
      world.setFullTime(snapshot.fullTime());
    }
  }

  private void applyTimeOfDayOnly(final World world, final TimeSnapshot snapshot) {
    switch (timeApplyMode) {
      case SET_TIME -> world.setTime(snapshot.timeOfDay());
      case SET_FULL_TIME -> {
        final long currentDay = Math.floorDiv(world.getFullTime(), TICKS_PER_DAY);
        world.setFullTime(currentDay * TICKS_PER_DAY + snapshot.timeOfDay());
      }
    }
  }
}