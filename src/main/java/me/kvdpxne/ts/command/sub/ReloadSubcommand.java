package me.kvdpxne.ts.command.sub;

import me.kvdpxne.ts.api.TimeSyncApi;
import me.kvdpxne.ts.command.Subcommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * {@code /timesync reload} - loads settings asynchronously and swaps the runtime.
 * The future completes on the main thread, so replying to the sender is safe.
 */
public final class ReloadSubcommand implements Subcommand {

  private final TimeSyncApi api;
  private final Logger logger;

  public ReloadSubcommand(final TimeSyncApi api, final Logger logger) {
    this.api = Objects.requireNonNull(api, "api");
    this.logger = Objects.requireNonNull(logger, "logger");
  }

  @Override
  public String name() {
    return "reload";
  }

  @Override
  public String permission() {
    return "timesync.reload";
  }

  @Override
  public String usage() {
    return "reload";
  }

  @Override
  public boolean execute(final CommandSender sender, final String[] args) {
    sender.sendMessage(ChatColor.GRAY + "Reloading configuration...");
    this.logger.info(() -> "Reload requested by " + sender.getName());

    this.api.reloadAsync().thenAccept(result -> {
      // Completed on the main thread by PluginBootstrap#mainThreadExecutor.
      if (result.success()) {
        sender.sendMessage(ChatColor.GREEN + "[OK] " + result.message());
      } else {
        sender.sendMessage(ChatColor.RED + "[FAIL] " + result.message());
      }
    });
    return true;
  }
}