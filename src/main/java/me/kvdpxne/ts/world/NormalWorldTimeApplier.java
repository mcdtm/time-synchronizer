package me.kvdpxne.ts.world;

import org.bukkit.Bukkit;
import org.bukkit.World;

/**
 * Applies the time-of-day only to Overworld worlds (environment {@code NORMAL}).
 * <p>
 * Uses {@link World#setTime(long)} rather than {@link World#setFullTime(long)}
 * so that the world's age is preserved (moon phases, local difficulty, etc.).
 */
public final class NormalWorldTimeApplier implements WorldTimeApplier {

  @Override
  public void apply(final long timeOfDay) {
    for (final World world : Bukkit.getWorlds()) {
      if (World.Environment.NORMAL == world.getEnvironment()) {
        world.setTime(timeOfDay);
      }
    }
  }
}