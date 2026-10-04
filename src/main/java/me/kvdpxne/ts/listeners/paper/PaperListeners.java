package me.kvdpxne.ts.listeners.paper;

import me.kvdpxne.ts.util.ClassPresence;
import me.kvdpxne.ts.world.WorldFilter;
import org.bukkit.event.Listener;

import java.util.Optional;

/**
 * Factory for Paper-only listeners. Every factory method first verifies that
 * the underlying event class is present so the enclosing plugin remains
 * compatible with Spigot and other forks.
 * <p>
 * The JVM loads {@link PaperGameRuleChangeListener} lazily, on the first
 * execution of the {@code new} instruction; if {@link #isGameRuleChangeEventAvailable()}
 * returns {@code false}, that instruction is never reached and no
 * {@link NoClassDefFoundError} surfaces.
 */
public final class PaperListeners {

  private static final String WORLD_GAME_RULE_CHANGE_EVENT =
      "io.papermc.paper.event.world.WorldGameRuleChangeEvent";

  private PaperListeners() {
  }

  public static boolean isGameRuleChangeEventAvailable() {
    return ClassPresence.isAvailable(WORLD_GAME_RULE_CHANGE_EVENT);
  }

  /**
   * @return a listener cancelling re-enables of {@code doDaylightCycle} when
   *         the Paper event is available, {@link Optional#empty()} otherwise
   */
  public static Optional<Listener> createGameRuleChangeListener(final WorldFilter filter) {
    if (!isGameRuleChangeEventAvailable()) {
      return Optional.empty();
    }
    return Optional.of(new PaperGameRuleChangeListener(filter));
  }
}