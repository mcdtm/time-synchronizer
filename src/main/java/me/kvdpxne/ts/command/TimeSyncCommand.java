package me.kvdpxne.ts.command;

import me.kvdpxne.ts.api.TimeSyncApi;
import me.kvdpxne.ts.command.sub.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Root {@code /timesync} command. Dispatches to registered {@link Subcommand}s
 * and handles permission checks and tab completion for the first argument.
 * <p>
 * Always runs on the main server thread (Bukkit guarantee).
 */
public final class TimeSyncCommand implements CommandExecutor, TabCompleter {

  private final SubcommandRegistry registry = new SubcommandRegistry();
  private final TimeSyncApi api;
  private final Logger logger;

  public TimeSyncCommand(final TimeSyncApi api, final Logger logger) {
    this.api = Objects.requireNonNull(api, "api");
    this.logger = Objects.requireNonNull(logger, "logger");
    this.registry.register(new ReloadSubcommand(this.api, this.logger));
    this.registry.register(new StatusSubcommand(this.api));
    this.registry.register(new SyncNowSubcommand(this.api));
    this.registry.register(new ListenerSubcommand(this.api));
    this.registry.register(new ZoneSubcommand(this.api));
  }

  @Override
  public boolean onCommand(
    final CommandSender sender,
    final Command command,
    final String label,
    final String[] args
  ) {
    if (0 == args.length) {
      this.sendUsage(sender, label);
      return true;
    }
    final var found = this.registry.find(args[0]);
    if (found.isEmpty()) {
      sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + args[0]);
      this.sendUsage(sender, label);
      return true;
    }
    final var sub = found.get();
    if (!sender.hasPermission(sub.permission())) {
      sender.sendMessage(ChatColor.RED + "You lack permission: " + sub.permission());
      return true;
    }
    final var subArgs = Arrays.copyOfRange(args, 1, args.length);
    return sub.execute(sender, subArgs);
  }

  @Override
  public List<String> onTabComplete(
    final CommandSender sender,
    final Command command,
    final String alias,
    final String[] args
  ) {
    if (1 == args.length) {
      final var prefix = args[0].toLowerCase();
      return this.registry.all().stream()
        .filter(sub -> sender.hasPermission(sub.permission()))
        .map(Subcommand::name)
        .filter(name -> name.startsWith(prefix))
        .toList();
    }
    if (1 < args.length) {
      return this.registry.find(args[0])
        .map(sub -> sub.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length)))
        .orElse(List.of());
    }
    return List.of();
  }

  private void sendUsage(final CommandSender sender, final String label) {
    sender.sendMessage(ChatColor.GOLD + "=== TimeSync ===");
    for (final var sub : this.registry.all()) {
      if (sender.hasPermission(sub.permission())) {
        sender.sendMessage(ChatColor.GRAY + "  /" + label + " " + sub.usage());
      }
    }
  }
}