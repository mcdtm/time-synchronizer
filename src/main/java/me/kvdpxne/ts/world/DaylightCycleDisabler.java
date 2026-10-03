package me.kvdpxne.ts.world;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Disables the vanilla {@code doDaylightCycle} gamerule on every Overworld so
 * the plugin is the only source of truth for world time.
 */
public final class DaylightCycleDisabler {

  private static final String GAMERULE_DO_DAYLIGHT_CYCLE = "doDaylightCycle";
  private static final String GAMERULE_VALUE_DISABLED = "false";

  private final Logger logger;

  public DaylightCycleDisabler(final Logger logger) {
    this.logger = Objects.requireNonNull(logger, "logger");
  }

  public void disableForOverworlds() {
    final var affected = new ArrayList<String>();

    for (final World world : Bukkit.getWorlds()) {
      if (World.Environment.NORMAL == world.getEnvironment()) {
        world.setGameRuleValue(GAMERULE_DO_DAYLIGHT_CYCLE, GAMERULE_VALUE_DISABLED);
        affected.add(world.getName());
      }
    }

    if (affected.isEmpty()) {
      logger.info("No Overworld worlds found; daylight cycle left untouched.");
    } else {
      final var names = String.join(", ", affected);
      logger.info(() -> "Daylight cycle disabled for worlds: " + names);
    }
  }
}