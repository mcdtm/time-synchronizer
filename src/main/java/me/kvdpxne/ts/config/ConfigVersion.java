package me.kvdpxne.ts.config;

/**
 * Current configuration schema version. Bump when a new key is introduced or
 * an existing key changes semantics. {@link SettingsParser} compares the value
 * stored in the file against this constant and logs a hint when they differ.
 */
public final class ConfigVersion {

  public static final int CURRENT = 1;

  private ConfigVersion() {
  }
}