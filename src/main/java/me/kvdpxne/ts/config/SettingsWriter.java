package me.kvdpxne.ts.config;

import me.kvdpxne.ts.exception.ConfigurationLoadException;

import java.time.ZoneId;

/**
 * Mutation interface for the settings file. Implementations must preserve
 * unrelated keys and comments. All methods block on file I/O.
 */
public interface SettingsWriter {

  /**
   * Rewrites the {@code time-zone} key, preserving comments and every other key.
   *
   * @throws ConfigurationLoadException on I/O failure
   */
  void updateZone(ZoneId zoneId) throws ConfigurationLoadException;
}