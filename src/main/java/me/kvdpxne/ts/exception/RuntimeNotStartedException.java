package me.kvdpxne.ts.exception;

/**
 * Raised when the plugin runtime is accessed before {@code start()} or after
 * {@code stop()}. Callers that can tolerate this state should use the
 * {@link java.util.Optional}-returning variants instead.
 */
public final class RuntimeNotStartedException extends TimeSyncException {

  public RuntimeNotStartedException() {
    super("Plugin runtime is not started");
  }
}