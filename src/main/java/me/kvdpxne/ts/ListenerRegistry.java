package me.kvdpxne.ts;

import me.kvdpxne.ts.exception.UnknownListenerException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Holds {@link ManagedListener}s keyed by logical name.
 * <p>
 * Main-thread-only. {@link #find(String)} returns {@link Optional} for callers
 * that want to check presence; {@link #require(String)} throws
 * {@link UnknownListenerException} for callers relying on a compile-time invariant.
 */
final class ListenerRegistry {

  private final Map<String, ManagedListener> listeners = new LinkedHashMap<>();

  void add(final String name, final ManagedListener listener) {
    Objects.requireNonNull(name, "name");
    this.listeners.put(name, Objects.requireNonNull(listener, "listener"));
  }

  Optional<ManagedListener> find(final String name) {
    return Optional.ofNullable(this.listeners.get(name));
  }

  ManagedListener require(final String name) {
    return this.find(name).orElseThrow(() -> new UnknownListenerException(name));
  }

  void applyAllInitialStates() {
    this.listeners.values().forEach(ManagedListener::applyInitialState);
  }

  void stopAll() {
    this.listeners.values().forEach(ManagedListener::stop);
    this.listeners.clear();
  }
}