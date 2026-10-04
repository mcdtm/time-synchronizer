package me.kvdpxne.ts.listeners;

import me.kvdpxne.ts.time.TimeCalculator;
import me.kvdpxne.ts.world.ConfiguredWorldTimeApplier;
import me.kvdpxne.ts.world.DaylightCycleDisabler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Keeps newly created or newly loaded worlds in sync with the configured mode.
 * <ul>
 *   <li>{@link WorldInitEvent} - disables the vanilla daylight cycle before the world ticks.</li>
 *   <li>{@link WorldLoadEvent} - applies the current snapshot immediately so the world
 *       does not wait up to {@code update-interval-seconds} for the first sync.</li>
 * </ul>
 */
public final class WorldLifecycleListener implements Listener {

  private final TimeCalculator calculator;
  private final ConfiguredWorldTimeApplier applier;
  private final DaylightCycleDisabler disabler;
  private final Logger logger;

  public WorldLifecycleListener(
    final TimeCalculator calculator,
    final ConfiguredWorldTimeApplier applier,
    final DaylightCycleDisabler disabler,
    final Logger logger
  ) {
    this.calculator = Objects.requireNonNull(calculator, "calculator");
    this.applier = Objects.requireNonNull(applier, "applier");
    this.disabler = Objects.requireNonNull(disabler, "disabler");
    this.logger = Objects.requireNonNull(logger, "logger");
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onWorldInit(final WorldInitEvent event) {
    if (disabler.disable(event.getWorld())) {
      logger.info(() -> "Daylight cycle disabled for new world '" + event.getWorld().getName() + "'");
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onWorldLoad(final WorldLoadEvent event) {
    final var world = event.getWorld();
    final var snapshot = calculator.currentSnapshot();
    if (applier.applyToWorld(world, snapshot)) {
      logger.info(() -> "Synchronized time for newly loaded world '" + world.getName() + "'");
    }
  }
}