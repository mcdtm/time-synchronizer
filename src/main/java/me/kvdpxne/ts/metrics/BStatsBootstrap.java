package me.kvdpxne.ts.metrics;

import me.kvdpxne.ts.config.PluginSettings;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Logger;

public final class BStatsBootstrap {

  /**
   * Sentinel used until a real id is assigned at <a href="https://bstats.org/">bStats</a>.
   * Never publish with this value; the plugin logs a warning and skips
   * metrics registration entirely.
   */
  public static final int UNCONFIGURED_PLUGIN_ID = -1;

  private final JavaPlugin plugin;
  private final PluginSettings settings;
  private final int pluginId;
  private final Logger logger;

  public BStatsBootstrap(final JavaPlugin plugin, final PluginSettings settings) {
    this(plugin, settings, UNCONFIGURED_PLUGIN_ID);
  }

  public BStatsBootstrap(
    final JavaPlugin plugin,
    final PluginSettings settings,
    final int pluginId
  ) {
    this.plugin = Objects.requireNonNull(plugin, "plugin");
    this.settings = Objects.requireNonNull(settings, "settings");
    this.pluginId = pluginId;
    this.logger = this.plugin.getLogger();
  }

  public void start() {
    if (!this.settings.bStatsEnabled()) {
      this.logger.info("bStats metrics are disabled in the configuration.");
      return;
    }
    if (UNCONFIGURED_PLUGIN_ID >= this.pluginId) {
      this.logger.warning(
        "bStats plugin id is not configured; metrics will not be sent. "
          + "Set a real id in BStatsBootstrap before publishing.");
      return;
    }
    final var metrics = new Metrics(this.plugin, this.pluginId);
    this.registerCharts(metrics);
    this.logger.info(() -> "bStats metrics enabled (id " + this.pluginId
      + "). Set bstats-enabled=false to opt out.");
  }

  private void registerCharts(final Metrics metrics) {
    metrics.addCustomChart(new SimplePie("day_sync_mode",
      () -> this.settings.daySyncMode().name()));
    metrics.addCustomChart(new SimplePie("time_apply_mode",
      () -> this.settings.timeApplyMode().name()));
    metrics.addCustomChart(new SimplePie("time_scale_bucket",
      () -> bucketScale(this.settings.scale())));
    metrics.addCustomChart(new SimplePie("player_join_listener",
      () -> this.settings.playerJoinSyncListenerState().name()));
    metrics.addCustomChart(new SimplePie("world_lifecycle_listener",
      () -> this.settings.worldLifecycleListenerState().name()));
    metrics.addCustomChart(new SimplePie("config_version",
      () -> Integer.toString(this.settings.configVersion())));
  }

  private static String bucketScale(final double scale) {
    if (1.0d == scale) {
      return "1.0";
    }
    if (1.0d > scale) {
      return "<1.0";
    }
    if (2.0d >= scale) {
      return "1.0-2.0";
    }
    return ">2.0";
  }
}