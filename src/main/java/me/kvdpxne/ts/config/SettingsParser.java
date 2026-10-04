package me.kvdpxne.ts.config;

import me.kvdpxne.ts.api.ListenerState;
import me.kvdpxne.ts.util.parser.BooleanParser;
import me.kvdpxne.ts.util.parser.EnumParser;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Parses a {@link Properties} instance into a validated {@link PluginSettings}.
 * <p>
 * Every {@code tryParse*} helper returns {@link Optional} instead of throwing;
 * invalid entries are logged and replaced by the caller with safe defaults.
 * Not thread-safe; create a fresh instance per load.
 */
final class SettingsParser {

  private static final ZoneId FALLBACK_ZONE = ZoneOffset.UTC;

  private final Logger logger;

  SettingsParser(final Logger logger) {
    this.logger = Objects.requireNonNull(logger, "logger");
  }

  PluginSettings parse(final Properties properties) {
    final var configVersion = (int) this.parseLong(
      properties.getProperty(SettingsDefaults.KEY_CONFIG_VERSION, "0"), 0L);
    //
    //
    this.checkConfigVersion(configVersion);

    final var zoneId = this.tryParseZoneId(
        properties.getProperty(SettingsDefaults.KEY_TIME_ZONE, SettingsDefaults.TIME_ZONE))
      .orElse(FALLBACK_ZONE);

    final var daySyncMode = this.parseEnum(
      DaySyncMode.class,
      properties.getProperty(
        SettingsDefaults.KEY_DAY_SYNC_MODE,
        SettingsDefaults.DAY_SYNC_MODE.name()
      ),
      SettingsDefaults.DAY_SYNC_MODE
    );

    final var timeApplyMode = this.parseEnum(
      TimeApplyMode.class,
      properties.getProperty(
        SettingsDefaults.KEY_TIME_APPLY_MODE,
        SettingsDefaults.TIME_APPLY_MODE.name()
      ),
      SettingsDefaults.TIME_APPLY_MODE
    );

    final var scale = this.parsePositiveDouble(
      properties.getProperty(
        SettingsDefaults.KEY_TIME_SCALE,
        Double.toString(SettingsDefaults.TIME_SCALE)
      ),
      SettingsDefaults.TIME_SCALE
    );

    final var updateIntervalTicks = Math.max(
      SettingsDefaults.MIN_UPDATE_INTERVAL_SECONDS,
      this.parseLong(
        properties.getProperty(
          SettingsDefaults.KEY_UPDATE_INTERVAL,
          Long.toString(SettingsDefaults.UPDATE_INTERVAL_SECONDS)
        ),
        SettingsDefaults.UPDATE_INTERVAL_SECONDS
      )
    ) * SettingsDefaults.TICKS_PER_SECOND;

    final var excluded = this.parseExcludedWorlds(
      properties.getProperty(
        SettingsDefaults.KEY_EXCLUDED_WORLDS,
        ""
      )
    );

    final var playerJoinState = this.parseEnum(
      ListenerState.class,
      properties.getProperty(
        SettingsDefaults.KEY_PLAYER_JOIN_LISTENER,
        SettingsDefaults.PLAYER_JOIN_STATE.name()
      ),
      SettingsDefaults.PLAYER_JOIN_STATE
    );

    final var worldLifecycleState = this.parseEnum(
      ListenerState.class,
      properties.getProperty(
        SettingsDefaults.KEY_WORLD_LIFECYCLE_LISTENER,
        SettingsDefaults.WORLD_LIFECYCLE_STATE.name()
      ),
      SettingsDefaults.WORLD_LIFECYCLE_STATE
    );

    final var daylightGuard = this.parseBoolean(
      properties.getProperty(
        SettingsDefaults.KEY_DAYLIGHT_GUARD,
        Boolean.toString(SettingsDefaults.DAYLIGHT_GUARD)
      ),
      SettingsDefaults.DAYLIGHT_GUARD
    );

    final var fallbackGuardIntervalTicks = Math.max(
      SettingsDefaults.MIN_FALLBACK_GUARD_INTERVAL_TICKS,
      this.parseLong(
        properties.getProperty(
          SettingsDefaults.KEY_FALLBACK_GUARD_INTERVAL,
          Long.toString(SettingsDefaults.FALLBACK_GUARD_INTERVAL_TICKS)
        ),
        SettingsDefaults.FALLBACK_GUARD_INTERVAL_TICKS)
    );

    final var eventThrottle = this.parseLong(
      properties.getProperty(
        SettingsDefaults.KEY_EVENT_THROTTLE,
        Long.toString(SettingsDefaults.EVENT_THROTTLE_MILLIS)
      ),
      SettingsDefaults.EVENT_THROTTLE_MILLIS);

    final var jumpThreshold = this.parseLong(
      properties.getProperty(
        SettingsDefaults.KEY_JUMP_THRESHOLD,
        Long.toString(SettingsDefaults.JUMP_THRESHOLD_TICKS)
      ),
      SettingsDefaults.JUMP_THRESHOLD_TICKS);

    final var bStatsEnabled = this.parseBoolean(
      properties.getProperty(
        SettingsDefaults.KEY_BSTATS,
        Boolean.toString(SettingsDefaults.BSTATS)
      ),
      SettingsDefaults.BSTATS
    );

    final var warnNonPaper = this.parseBoolean(
      properties.getProperty(
        SettingsDefaults.KEY_WARN_NON_PAPER,
        Boolean.toString(SettingsDefaults.WARN_NON_PAPER)
      ),
      SettingsDefaults.WARN_NON_PAPER
    );

    if (DaySyncMode.DISABLED != daySyncMode && TimeApplyMode.SET_TIME == timeApplyMode) {
      this.logger.warning(() ->
        "time-apply-mode=SET_TIME is ignored because day-sync-mode=" + daySyncMode);
    }
    if (DaySyncMode.DISABLED != daySyncMode && 1.0d != scale) {
      this.logger.warning(() ->
        "time-scale=" + scale + " with day-sync-mode=" + daySyncMode
          + " may cause periodic jumps");
    }

    return new PluginSettings(
      configVersion, zoneId, daySyncMode, timeApplyMode, scale,
      updateIntervalTicks, excluded,
      playerJoinState, worldLifecycleState,
      daylightGuard, fallbackGuardIntervalTicks,
      eventThrottle, jumpThreshold, warnNonPaper, bStatsEnabled);
  }

  private void checkConfigVersion(final int fileVersion) {
    if (ConfigVersion.CURRENT == fileVersion) {
      return;
    }
    if (fileVersion > ConfigVersion.CURRENT) {
      this.logger.warning(() ->
        "Config version " + fileVersion + " is newer than supported "
          + ConfigVersion.CURRENT + "; unknown keys are ignored");
      return;
    }
    this.logger.warning(() ->
      "Config version " + fileVersion + " is older than " + ConfigVersion.CURRENT
        + "; missing keys fall back to defaults");
  }

  private Optional<ZoneId> tryParseZoneId(final String raw) {
    final var trimmed = raw.trim();
    if (SettingsDefaults.SYSTEM_ZONE_ALIAS.equalsIgnoreCase(trimmed)) {
      final var systemZone = ZoneId.systemDefault();
      this.logger.info(() -> "time-zone=SYSTEM resolved to " + systemZone);
      return Optional.of(systemZone);
    }
    try {
      return Optional.of(ZoneId.of(trimmed));
    } catch (final DateTimeException _) {
      this.logger.warning(() -> "Invalid time-zone '" + raw + "'");
      return Optional.empty();
    }
  }

  private <E extends Enum<E>> E parseEnum(
    final Class<E> type,
    final String raw,
    final E fallback
  ) {
    return EnumParser.tryParse(type, raw).orElseGet(() -> {
      this.logger.warning(() ->
        "Unknown value '" + raw + "' for " + type.getSimpleName() + ", using " + fallback);
      return fallback;
    });
  }

  private long parseLong(final String raw, final long fallback) {
    if (null == raw) {
      return fallback;
    }
    try {
      return Long.parseLong(raw.trim());
    } catch (final NumberFormatException _) {
      this.logger.warning(() -> "Invalid number '" + raw + "', using " + fallback);
      return fallback;
    }
  }

  private double parsePositiveDouble(final String raw, final double fallback) {
    if (null == raw) {
      return fallback;
    }
    try {
      final double value = Double.parseDouble(raw.trim());
      if (0.0d >= value) {
        this.logger.warning(() -> "Non-positive number '" + raw + "', using " + fallback);
        return fallback;
      }
      return value;
    } catch (final NumberFormatException _) {
      this.logger.warning(() -> "Invalid number '" + raw + "', using " + fallback);
      return fallback;
    }
  }

  private boolean parseBoolean(final String raw, final boolean fallback) {
    return BooleanParser.tryParse(raw).orElseGet(() -> {
      this.logger.warning(() -> "Invalid boolean '" + raw + "', using " + fallback);
      return fallback;
    });
  }

  private Set<String> parseExcludedWorlds(final String raw) {
    if (raw.isBlank()) {
      return Set.of();
    }
    return Arrays.stream(raw.split(","))
      .map(String::trim)
      .filter(value -> !value.isEmpty())
      .collect(Collectors.toUnmodifiableSet());
  }
}