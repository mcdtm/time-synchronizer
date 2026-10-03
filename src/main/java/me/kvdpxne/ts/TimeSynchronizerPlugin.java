package me.kvdpxne.ts;

import me.kvdpxne.ts.config.PluginSettings;
import me.kvdpxne.ts.config.PropertiesSettingsLoader;
import me.kvdpxne.ts.task.TimeSynchronizationTask;
import me.kvdpxne.ts.time.ZonedTimeCalculator;
import me.kvdpxne.ts.world.ConfiguredWorldTimeApplier;
import me.kvdpxne.ts.world.DaylightCycleDisabler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Level;

/**
 * Plugin entry point. Loads settings, wires the calculator/applier pair into
 * the scheduler, and bootstraps the daylight-cycle gamerule.
 */
public final class TimeSynchronizerPlugin extends JavaPlugin {

  private static final long INITIAL_DELAY_TICKS = 20L;
  private static final long UPDATE_INTERVAL_TICKS = 1_200L;

  @Override
  public void onEnable() {
    final var logger = getLogger();

    final PluginSettings settings;
    try {
      settings = new PropertiesSettingsLoader(getDataFolder(), logger).load();
    } catch (final IOException e) {
      logger.log(Level.SEVERE, "Failed to load settings; disabling plugin", e);
      getServer().getPluginManager().disablePlugin(this);
      return;
    }

    final var calculator = new ZonedTimeCalculator(settings.zoneId());
    final var applier = new ConfiguredWorldTimeApplier(settings);
    final var task = new TimeSynchronizationTask(calculator, applier);

    new DaylightCycleDisabler(logger).disableForOverworlds();

    Bukkit.getScheduler().runTaskTimer(
      this,
      task,
      INITIAL_DELAY_TICKS,
      UPDATE_INTERVAL_TICKS
    );

    logger.info(() -> "Time synchronization enabled [zone=" + settings.zoneId()
      + ", daySync=" + settings.daySyncMode()
      + ", timeApply=" + settings.timeApplyMode() + "]");
  }
}