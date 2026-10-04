package me.kvdpxne.ts.listeners;

import me.kvdpxne.ts.PluginBootstrap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

/**
 * Defensive cleanup trigger. {@link PluginDisableEvent} is fired by Bukkit
 * <em>before</em> {@code onDisable()} runs, and (unlike {@code onDisable})
 * it is dispatched even in scenarios where the plugin lifecycle is not fully
 * honoured - for example, when {@code Bukkit.reload()} partially disables a
 * plugin and only later re-enables it.
 * <p>
 * The handler only fires for the owning plugin and is idempotent: if
 * {@code onDisable} also runs and calls {@code stopRuntime()}, the second
 * invocation is a no-op.
 */
public final class SelfDisableListener implements Listener {

  private final Plugin self;
  private final PluginBootstrap bootstrap;

  public SelfDisableListener(final Plugin self, final PluginBootstrap bootstrap) {
    this.self = Objects.requireNonNull(self, "self");
    this.bootstrap = Objects.requireNonNull(bootstrap, "bootstrap");
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPluginDisable(final PluginDisableEvent event) {
    if (event.getPlugin() != this.self) {
      return;
    }
    if (this.bootstrap.hasRuntime()) {
      this.self.getLogger().info(
        "Defensive shutdown triggered by PluginDisableEvent.");
      this.bootstrap.stopRuntime();
    }
  }
}