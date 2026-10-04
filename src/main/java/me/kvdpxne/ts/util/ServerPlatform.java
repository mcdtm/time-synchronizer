package me.kvdpxne.ts.util;

import org.bukkit.Bukkit;

import java.util.Locale;

/**
 * Identifies the running server implementation.
 */
public final class ServerPlatform {

  private static final String PAPER = "paper";
  private static final String PURPUR = "purpur";
  private static final String FOLIA = "folia";
  private static final String Pufferfish = "pufferfish";

  private ServerPlatform() {
  }

  /** @return {@code true} for Paper, Purpur, Folia, Pufferfish and compatible forks */
  public static boolean isPaperFamily() {
    final var name = serverName().toLowerCase(Locale.ROOT);
    return name.contains(PAPER)
        || name.contains(PURPUR)
        || name.contains(FOLIA)
        || name.contains(Pufferfish);
  }

  public static String serverName() {
    return Bukkit.getServer().getName();
  }
}