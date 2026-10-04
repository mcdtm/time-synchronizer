package me.kvdpxne.ts;

import me.kvdpxne.ts.api.TimeSyncApi;
import me.kvdpxne.ts.command.TimeSyncCommand;
import me.kvdpxne.ts.config.PluginSettings;
import me.kvdpxne.ts.exception.ConfigurationLoadException;
import me.kvdpxne.ts.listeners.SelfDisableListener;
import me.kvdpxne.ts.metrics.BStatsBootstrap;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Clock;
import java.util.logging.Level;

public final class TimeSyncPlugin extends JavaPlugin {

  private static final int PLUGIN_ID = 34484;

  private final Clock clock = Clock.systemUTC();
  private PluginBootstrap bootstrap;
  private TimeSyncApiImpl api;

  @Override
  public void onLoad() {
    // Defensive: if a previous shutdown did not call onDisable cleanly
    // (Bukkit reload edge cases), tear down leftover state before starting.
    if (null != this.bootstrap) {
      this.bootstrap.stopRuntime();
      this.bootstrap.close();
    }
    this.bootstrap = new PluginBootstrap(this, this.clock);
  }

  @Override
  public void onEnable() {
    final PluginSettings settings;
    try {
      settings = this.bootstrap.loadInitialSettingsBlocking();
    } catch (final ConfigurationLoadException e) {
      this.getLogger().log(Level.SEVERE, "Cannot enable without valid configuration", e);
      this.getServer().getPluginManager().disablePlugin(this);
      return;
    }

    PlatformWarning.warnIfNotPaper(this.getLogger(), settings.warnNonPaper());
    this.bootstrap.startRuntime(settings);
    this.api = new TimeSyncApiImpl(this.bootstrap);

    this.getServer().getServicesManager().register(
      TimeSyncApi.class, this.api, this, ServicePriority.Normal);

    this.registerCommand();

    // Defensive cleanup trigger for partially-honoured disable flows.
    this.getServer().getPluginManager().registerEvents(
      new SelfDisableListener(this, this.bootstrap), this);

    new BStatsBootstrap(this, settings, PLUGIN_ID).start();
    this.getLogger().info("Public API registered under " + TimeSyncApi.class.getName());
  }

  @Override
  public void onDisable() {
    if (null != this.bootstrap) {
      this.bootstrap.stopRuntime();
      this.bootstrap.close();
      this.bootstrap = null;
    }
    this.api = null;
  }

  private void registerCommand() {
    final var command = this.getCommand("timesync");
    if (null == command) {
      this.getLogger().warning("Command 'timesync' is missing from plugin.yml");
      return;
    }
    final var executor = new TimeSyncCommand(this.api, this.getLogger());
    command.setExecutor(executor);
    command.setTabCompleter(executor);
  }
}