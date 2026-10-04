package me.kvdpxne.ts.api;

import me.kvdpxne.ts.util.parser.EnumParser;

import java.util.Optional;

/**
 * Tri-state configuration for plugin-managed listeners.
 * <ul>
 *   <li>{@link #DISABLED} - never registered; locked, cannot be toggled via API.</li>
 *   <li>{@link #DYNAMIC}  - not registered by default; mutable via API.</li>
 *   <li>{@link #ENABLED}  - registered by default; mutable via API.</li>
 * </ul>
 */
public enum ListenerState {

  DISABLED,
  DYNAMIC,
  ENABLED;

  public boolean isMutable() {
    return DISABLED != this;
  }

  public boolean isInitiallyEnabled() {
    return ENABLED == this;
  }

  /**
   * Parses a case-insensitive, trimmed value.
   *
   * @return the parsed state, or {@link Optional#empty()} when the input is unknown or null
   */
  public static Optional<ListenerState> tryFromString(final String raw) {
    return EnumParser.tryParse(ListenerState.class, raw);
  }
}