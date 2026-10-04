package me.kvdpxne.ts.exception;

/**
 * Base type for every domain-specific exception thrown by the plugin.
 * <p>
 * Sealed to force new exception types to be added to a single known list;
 * this keeps {@code catch} hierarchies in downstream code exhaustive.
 */
public abstract sealed class TimeSyncException extends RuntimeException
    permits ConfigurationLoadException, ListenerLockedException,
            NotMainThreadException, RuntimeNotStartedException,
            UnknownListenerException {

  protected TimeSyncException(final String message) {
    super(message);
  }

  protected TimeSyncException(final String message, final Throwable cause) {
    super(message, cause);
  }
}