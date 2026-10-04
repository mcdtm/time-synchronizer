package me.kvdpxne.ts.exception;

/**
 * Raised when a main-thread-only operation is invoked from another thread.
 */
public final class NotMainThreadException extends TimeSyncException {

  public NotMainThreadException(final String operationName) {
    super("Operation '%s' must be called from the main server thread".formatted(operationName));
  }
}