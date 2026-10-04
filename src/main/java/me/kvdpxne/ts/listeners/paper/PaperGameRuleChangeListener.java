package me.kvdpxne.ts.listeners.paper;

import me.kvdpxne.ts.world.WorldFilter;
import io.papermc.paper.event.world.WorldGameRuleChangeEvent;
import org.bukkit.GameRule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Objects;

/**
 * Cancels attempts to re-enable {@code doDaylightCycle} on managed worlds.
 * Registered only when Paper exposes {@link WorldGameRuleChangeEvent}.
 * <p>
 * Runs at {@link EventPriority#LOWEST} so other plugins can still observe the
 * event (they see it as cancelled); our cancellation short-circuits the change.
 */
public final class PaperGameRuleChangeListener implements Listener {

  private static final String ENABLED_VALUE = "true";

  private final WorldFilter filter;

  public PaperGameRuleChangeListener(final WorldFilter filter) {
    this.filter = Objects.requireNonNull(filter, "filter");
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onGameRuleChange(final WorldGameRuleChangeEvent event) {
    if (!GameRule.DO_DAYLIGHT_CYCLE.equals(event.getGameRule())) {
      return;
    }
    if (!ENABLED_VALUE.equalsIgnoreCase(event.getValue())) {
      return;
    }
    if (this.filter.isManaged(event.getWorld())) {
      event.setCancelled(true);
    }
  }
}