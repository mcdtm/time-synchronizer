package me.kvdpxne.ts.exception;

/**
 * Raised when the plugin cannot read or parse its settings file.
 * Wraps the underlying I/O or parser failure.
 */
public final class ConfigurationLoadException extends TimeSyncException {

  public ConfigurationLoadException(final String message) {
    super(message);
  }

  public ConfigurationLoadException(final String message, final Throwable cause) {
    super(message, cause);
  }
}