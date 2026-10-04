package me.kvdpxne.ts.config;

import me.kvdpxne.ts.exception.ConfigurationLoadException;

/**
 * Strategy for loading {@link PluginSettings} from a backing store.
 */
@FunctionalInterface
public interface SettingsLoader {

  /**
   * Loads settings, creating the backing store with defaults when absent.
   *
   * @throws ConfigurationLoadException when the backing store cannot be read or written
   */
  PluginSettings load() throws ConfigurationLoadException;
}