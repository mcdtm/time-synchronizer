package me.kvdpxne.ts.exception;

/**
 * Raised when a listener is requested by a name that is not registered.
 * Signals a programming error - listener names are compile-time constants.
 */
public final class UnknownListenerException extends TimeSyncException {

  public UnknownListenerException(final String name) {
    super("Unknown listener: " + name);
  }
}