package me.kvdpxne.ts.world;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;

public final class DaylightCycleDisabler {

  private static final String GAMERULE_DO_DAYLIGHT_CYCLE = "doDaylightCycle";
  private static final String GAMERULE_VALUE_DISABLED = "false";

  private final WorldFilter filter;
  private final Logger logger;

  public DaylightCycleDisabler(final WorldFilter filter, final Logger logger) {
    this.filter = Objects.requireNonNull(filter, "filter");
    this.logger = Objects.requireNonNull(logger, "logger");
  }

  public void disableForManagedWorlds() {
    final var changed = new ArrayList<String>();
    for (final World world : Bukkit.getWorlds()) {
      if (this.disable(world)) {
        changed.add(world.getName());
      }
    }
    if (changed.isEmpty()) {
      this.logger.fine("Daylight cycle already disabled on all managed worlds.");
      return;
    }
    final var names = String.join(", ", changed);
    this.logger.info(() -> "Daylight cycle disabled for worlds: " + names);
  }

  /**
   * @return {@code true} when the gamerule was actually modified. Worlds that
   *         already had {@code doDaylightCycle=false} are left untouched and
   *         reported as no-ops, which keeps the console quiet on reloads.
   */
  public boolean disable(final World world) {
    if (!this.filter.isManaged(world)) {
      return false;
    }
    final var current = world.getGameRuleValue(GAMERULE_DO_DAYLIGHT_CYCLE);
    if (GAMERULE_VALUE_DISABLED.equalsIgnoreCase(current)) {
      return false;
    }
    world.setGameRuleValue(GAMERULE_DO_DAYLIGHT_CYCLE, GAMERULE_VALUE_DISABLED);
    return true;
  }
}