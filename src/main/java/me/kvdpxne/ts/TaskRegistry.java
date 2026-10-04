package me.kvdpxne.ts;

import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks {@link BukkitTask}s so they can all be cancelled at once.
 * <p>
 * Main-thread-only.
 */
final class TaskRegistry {

  private final List<BukkitTask> tasks = new ArrayList<>();

  void track(final BukkitTask task) {
    this.tasks.add(task);
  }

  void cancelAll() {
    for (final BukkitTask task : this.tasks) {
      task.cancel();
    }
    this.tasks.clear();
  }
}