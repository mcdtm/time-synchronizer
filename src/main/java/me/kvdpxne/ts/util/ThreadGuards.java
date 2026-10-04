package me.kvdpxne.ts.util;

import me.kvdpxne.ts.exception.NotMainThreadException;
import org.bukkit.Bukkit;

/**
 * Central place for main-server-thread assertions.
 */
public final class ThreadGuards {

  private ThreadGuards() {
  }

  /**
   * @throws NotMainThreadException when not invoked from the primary server thread
   */
  public static void requireMainThread(final String operation) {
    if (!Bukkit.isPrimaryThread()) {
      throw new NotMainThreadException(operation);
    }
  }
}