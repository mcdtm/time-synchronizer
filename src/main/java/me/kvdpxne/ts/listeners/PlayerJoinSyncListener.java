package me.kvdpxne.ts.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Triggers a sync when a player joins. The actual strategy is injected:
 * <ul>
 *   <li>When day sync is enabled, the whole world set is synced (they must stay aligned).</li>
 *   <li>Otherwise only the player's current world is synced, keeping the handler O(1).</li>
 * </ul>
 */
public final class PlayerJoinSyncListener implements Listener {

  private final Consumer<Player> strategy;

  public PlayerJoinSyncListener(final Consumer<Player> strategy) {
    this.strategy = Objects.requireNonNull(strategy, "strategy");
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerJoin(final PlayerJoinEvent event) {
    this.strategy.accept(event.getPlayer());
  }
}