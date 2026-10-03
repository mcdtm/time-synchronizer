package me.kvdpxne.ts.config;

import java.io.IOException;

/**
 * Strategy for loading {@link PluginSettings} from some backing store.
 */
@FunctionalInterface
public interface SettingsLoader {

  /**
   * Loads settings, creating the backing store with defaults when absent.
   *
   * @throws IOException when the backing store cannot be read or written
   */
  PluginSettings load() throws IOException;
}