package me.kvdpxne.ts.world;

import me.kvdpxne.ts.ThrottledLogger;
import me.kvdpxne.ts.listeners.WorldUnloadListener;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Periodically re-applies {@code doDaylightCycle=false} on managed worlds.
 * Protects against {@code /gamerule} commands and other plugins that turn it on.
 * <p>
 * <b>Must run on the main server thread.</b> Uses a plain {@link ThrottledLogger}
 * (backed by a {@code HashMap}) and cleans up per-world entries on
 * {@link WorldUnloadListener}.
 */
public final class DaylightCycleGuard implements Runnable {

  private static final String GAMERULE = "doDaylightCycle";
  private static final String DISABLED_VALUE = "false";
  private static final String ENABLED_VALUE = "true";
  private static final long LOG_THROTTLE_MILLIS = 30_000L;

  private final WorldFilter filter;
  private final ThrottledLogger throttled;

  public DaylightCycleGuard(final WorldFilter filter, final Logger logger) {
    this.filter = Objects.requireNonNull(filter, "filter");
    Objects.requireNonNull(logger, "logger");
    this.throttled = new ThrottledLogger(logger, LOG_THROTTLE_MILLIS);
  }

  @Override
  public void run() {
    for (final World world : Bukkit.getWorlds()) {
      if (this.filter.isManaged(world) && this.isDaylightCycleEnabled(world)) {
        world.setGameRuleValue(GAMERULE, DISABLED_VALUE);
        this.throttled.warn(world.getName(), () ->
          "Detected re-enabled doDaylightCycle in world '" + world.getName()
            + "'; forcing it back to false");
      }
    }
  }

  /** Drops the throttle entry so a re-created world logs fresh warnings. */
  public void onWorldUnload(final String worldName) {
    this.throttled.forget(worldName);
  }

  public void reset() {
    this.throttled.clear();
  }

  private boolean isDaylightCycleEnabled(final World world) {
    return ENABLED_VALUE.equalsIgnoreCase(world.getGameRuleValue(GAMERULE));
  }
}