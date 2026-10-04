package me.kvdpxne.ts.api;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;
import java.util.Objects;

/**
 * Fired after the plugin has applied a new time to one or more worlds.
 * Listeners can read the snapshot and inspect which worlds were touched.
 */
public final class TimeSynchronizedEvent extends Event {

  private static final HandlerList HANDLERS = new HandlerList();

  private final TimeSnapshot snapshot;
  private final List<World> affectedWorlds;

  public TimeSynchronizedEvent(final TimeSnapshot snapshot, final List<World> affectedWorlds) {
    this.snapshot = Objects.requireNonNull(snapshot, "snapshot");
    this.affectedWorlds = List.copyOf(Objects.requireNonNull(affectedWorlds, "affectedWorlds"));
  }

  public TimeSnapshot snapshot() {
    return snapshot;
  }

  public List<World> affectedWorlds() {
    return affectedWorlds;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }
}