package me.kvdpxne.ts;

import me.kvdpxne.ts.api.ListenerState;
import me.kvdpxne.ts.exception.ListenerLockedException;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Wraps a Bukkit {@link Listener}, managing its registration according to an
 * initial {@link ListenerState}. Main-thread-only.
 */
public final class ManagedListener {

  private final Listener listener;
  private final JavaPlugin plugin;
  private final Logger logger;
  private final String name;
  private final boolean mutable;
  private boolean registered;

  public ManagedListener(
    final Listener listener,
    final JavaPlugin plugin,
    final String name,
    final ListenerState initialState,
    final Logger logger
  ) {
    this.listener = Objects.requireNonNull(listener, "listener");
    this.plugin = Objects.requireNonNull(plugin, "plugin");
    this.name = Objects.requireNonNull(name, "name");
    this.logger = Objects.requireNonNull(logger, "logger");
    Objects.requireNonNull(initialState, "initialState");
    this.mutable = initialState.isMutable();
    this.registered = initialState.isInitiallyEnabled();
  }

  public void applyInitialState() {
    if (this.registered) {
      this.doRegister();
      this.logger.info(() -> "Listener '" + this.name + "' registered (state=ENABLED)");
    } else {
      final var kind = this.mutable ? "DYNAMIC" : "DISABLED";
      this.logger.info(() -> "Listener '" + this.name + "' not registered (state=" + kind + ")");
    }
  }

  public boolean isEnabled() {
    return this.registered;
  }

  public boolean isMutable() {
    return this.mutable;
  }

  public void setEnabled(final boolean enabled) {
    if (!this.mutable) {
      throw new ListenerLockedException(this.name);
    }
    if (enabled == this.registered) {
      return;
    }
    if (enabled) {
      this.doRegister();
    } else {
      this.doUnregister();
    }
    this.registered = enabled;
    this.logger.info(() -> "Listener '" + this.name + "' is now "
      + (enabled ? "enabled" : "disabled"));
  }

  public void stop() {
    if (this.registered) {
      this.doUnregister();
      this.registered = false;
    }
  }

  private void doRegister() {
    Bukkit.getPluginManager().registerEvents(this.listener, this.plugin);
  }

  private void doUnregister() {
    HandlerList.unregisterAll(this.listener);
  }
}