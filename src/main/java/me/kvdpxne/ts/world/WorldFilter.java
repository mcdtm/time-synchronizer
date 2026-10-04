package me.kvdpxne.ts.world;

import org.bukkit.World;

/**
 * Decides whether a world is managed by the plugin.
 */
@FunctionalInterface
public interface WorldFilter {

  boolean isManaged(World world);
}