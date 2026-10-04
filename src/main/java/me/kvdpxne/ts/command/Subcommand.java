package me.kvdpxne.ts.command;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * A single {@code /timesync <name>} branch. Every subcommand owns its own
 * permission check, argument validation and tab completion.
 */
public interface Subcommand {

  /** Lower-case identifier used in {@code /timesync <name>}. */
  String name();

  /** Permission required to execute. */
  String permission();

  /** One-line usage hint shown in {@code /timesync}. */
  String usage();

  /**
   * @return {@code true} when the command was handled (Bukkit convention)
   */
  boolean execute(CommandSender sender, String[] args);

  default List<String> tabComplete(final CommandSender sender, final String[] args) {
    return List.of();
  }
}