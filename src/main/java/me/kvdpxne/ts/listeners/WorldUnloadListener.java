package me.kvdpxne.ts.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Notifies a cleanup callback when a world is unloaded. The callback is
 * responsible for releasing any per-world state held by the plugin
 * (throttle maps, caches). Using a {@link Consumer} keeps this listener
 * decoupled from the concrete components that own that state.
 */
public final class WorldUnloadListener implements Listener {

  private final Consumer<String> cleanup;

  public WorldUnloadListener(final Consumer<String> cleanup) {
    this.cleanup = Objects.requireNonNull(cleanup, "cleanup");
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onWorldUnload(final WorldUnloadEvent event) {
    this.cleanup.accept(event.getWorld().getName());
  }
}