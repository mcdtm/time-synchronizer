package me.kvdpxne.ts.util;

import org.bukkit.World;

import java.util.Optional;

/**
 * Version-aware access to {@link World.Environment} constants that may not
 * exist on older server builds. {@code CUSTOM} was introduced in 1.13; servers
 * older than that do not expose it, so referencing it directly would fail with
 * {@link NoSuchFieldError} on those platforms.
 * <p>
 * The lookup happens once at class initialization; results are cached.
 */
public final class EnvironmentSupport {

  private static final Optional<World.Environment> CUSTOM = detectCustom();

  private EnvironmentSupport() {
  }

  public static boolean isCustom(final World.Environment environment) {
    return CUSTOM.isPresent() && CUSTOM.get() == environment;
  }

  public static boolean isCustomAvailable() {
    return CUSTOM.isPresent();
  }

  private static Optional<World.Environment> detectCustom() {
    try {
      return Optional.of(Enum.valueOf(World.Environment.class, "CUSTOM"));
    } catch (final IllegalArgumentException _) {
      return Optional.empty();
    }
  }
}