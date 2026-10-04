package me.kvdpxne.ts.world;

import me.kvdpxne.ts.config.DaySyncMode;
import me.kvdpxne.ts.config.PluginSettings;
import org.bukkit.World;

import java.util.Objects;
import java.util.Set;

/**
 * {@link WorldFilter} driven by {@link PluginSettings}.
 * <ul>
 *   <li>Worlds in {@code excluded-worlds} are never managed.</li>
 *   <li>{@code DISABLED}/{@code OVERWORLD} manage only {@code NORMAL}.</li>
 *   <li>{@code ALL_WORLDS} manages every environment, including {@code CUSTOM}.</li>
 * </ul>
 */
public final class SettingsWorldFilter implements WorldFilter {

  private final DaySyncMode daySyncMode;
  private final Set<String> excludedWorlds;

  public SettingsWorldFilter(final PluginSettings settings) {
    Objects.requireNonNull(settings, "settings");
    this.daySyncMode = settings.daySyncMode();
    this.excludedWorlds = settings.excludedWorlds();
  }

  @Override
  public boolean isManaged(final World world) {
    if (this.excludedWorlds.contains(world.getName())) {
      return false;
    }
    return switch (this.daySyncMode) {
      case DISABLED, OVERWORLD -> World.Environment.NORMAL == world.getEnvironment();
      case ALL_WORLDS -> true;
    };
  }
}