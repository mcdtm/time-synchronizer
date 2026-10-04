package me.kvdpxne.ts.command.sub;

import me.kvdpxne.ts.api.TimeSyncApi;
import me.kvdpxne.ts.command.Subcommand;
import me.kvdpxne.ts.exception.RuntimeNotStartedException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Objects;

/**
 * {@code /timesync status} - prints a snapshot of the current runtime state.
 */
public final class StatusSubcommand implements Subcommand {

  private final TimeSyncApi api;

  public StatusSubcommand(final TimeSyncApi api) {
    this.api = Objects.requireNonNull(api, "api");
  }

  @Override
  public String name() {
    return "status";
  }

  @Override
  public String permission() {
    return "timesync.status";
  }

  @Override
  public String usage() {
    return "status";
  }

  @Override
  public boolean execute(final CommandSender sender, final String[] args) {
    sender.sendMessage(ChatColor.GOLD + "=== TimeSync Status ===");
    try {
      final var settings = this.api.settings();
      this.send(sender, "Config version", Integer.toString(settings.configVersion()));
      this.send(sender, "Zone", settings.zoneId().getId());
      this.send(sender, "Scale", Double.toString(settings.scale()));
      this.send(sender, "Day sync mode", settings.daySyncMode().name());
      this.send(sender, "Time apply mode", settings.timeApplyMode().name());
      this.send(sender, "Update interval",
        settings.updateIntervalTicks() + " ticks ("
          + settings.updateIntervalTicks() / 20L + "s)");
      this.send(sender, "Event throttle", settings.eventThrottleMillis() + " ms");
      this.send(sender, "Daylight guard",
        settings.daylightGuardEnabled()
          ? "ENABLED (interval " + settings.fallbackGuardIntervalTicks() + " ticks)"
          : "DISABLED");
      this.send(sender, "Excluded worlds",
        settings.excludedWorlds().isEmpty()
          ? "(none)"
          : String.join(", ", settings.excludedWorlds()));
      this.send(sender, "PlayerJoin listener",
        this.describeListener(
          this.api.isPlayerJoinSyncListenerEnabled(),
          this.api.isPlayerJoinSyncListenerMutable()));
      this.send(sender, "WorldLifecycle listener",
        this.describeListener(
          this.api.isWorldLifecycleListenerEnabled(),
          this.api.isWorldLifecycleListenerMutable()));

      final var snapshot = this.api.currentSnapshot();
      this.send(sender, "Current time-of-day", snapshot.timeOfDay() + " ticks");
      this.send(sender, "Current full time", Long.toString(snapshot.fullTime()));
    } catch (final RuntimeNotStartedException _) {
      sender.sendMessage(ChatColor.RED
        + "Runtime is not active (plugin disabled or still starting).");
    }
    return true;
  }

  private void send(final CommandSender sender, final String key, final String value) {
    sender.sendMessage(ChatColor.GRAY + key + ": " + ChatColor.WHITE + value);
  }

  private String describeListener(final boolean enabled, final boolean mutable) {
    final var state = enabled ? "ENABLED" : "DISABLED";
    return mutable ? state + " (mutable)" : state + " (locked)";
  }
}