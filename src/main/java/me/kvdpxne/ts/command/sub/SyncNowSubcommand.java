package me.kvdpxne.ts.command.sub;

import me.kvdpxne.ts.api.TimeSyncApi;
import me.kvdpxne.ts.command.Subcommand;
import me.kvdpxne.ts.exception.RuntimeNotStartedException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Objects;

/**
 * {@code /timesync syncnow} - forces an immediate sync, bypassing the interval.
 */
public final class SyncNowSubcommand implements Subcommand {

  private final TimeSyncApi api;

  public SyncNowSubcommand(final TimeSyncApi api) {
    this.api = Objects.requireNonNull(api, "api");
  }

  @Override
  public String name() {
    return "syncnow";
  }

  @Override
  public String permission() {
    return "timesync.syncnow";
  }

  @Override
  public String usage() {
    return "syncnow";
  }

  @Override
  public boolean execute(final CommandSender sender, final String[] args) {
    try {
      this.api.syncNow();
      sender.sendMessage(ChatColor.GREEN + "Time synchronized.");
    } catch (final RuntimeNotStartedException _) {
      sender.sendMessage(ChatColor.RED + "Runtime is not active; cannot sync.");
    }
    return true;
  }
}