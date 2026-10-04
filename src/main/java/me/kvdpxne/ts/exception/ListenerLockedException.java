package me.kvdpxne.ts.exception;

/**
 * Raised when a listener is toggled while locked by configuration
 * ({@code DISABLED} state). The caller should check
 * {@code isXxxListenerMutable()} first if it wants to avoid this.
 */
public final class ListenerLockedException extends TimeSyncException {

  public ListenerLockedException(final String name) {
    super("Listener '" + name + "' is locked by configuration (DISABLED) and cannot be toggled");
  }
}