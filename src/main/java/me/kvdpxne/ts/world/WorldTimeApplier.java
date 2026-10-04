package me.kvdpxne.ts.world;

import me.kvdpxne.ts.api.TimeSnapshot;
import org.bukkit.World;

import java.util.List;

@FunctionalInterface
public interface WorldTimeApplier {
  /**
   * @return the worlds that were actually modified
   */
  List<World> apply(TimeSnapshot snapshot);
}