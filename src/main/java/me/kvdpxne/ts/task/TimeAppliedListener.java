package me.kvdpxne.ts.task;

import me.kvdpxne.ts.api.TimeSnapshot;
import org.bukkit.World;

import java.util.List;

/**
 * Callback invoked after the applier has touched at least one world.
 * Decouples the scheduler task from the Bukkit event system, keeping it unit-testable.
 */
@FunctionalInterface
public interface TimeAppliedListener {
  void onTimeApplied(TimeSnapshot snapshot, List<World> affectedWorlds);
}