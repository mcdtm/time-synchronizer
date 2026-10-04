package me.kvdpxne.ts.config;

import me.kvdpxne.ts.api.ListenerState;

/**
 * Default values and the template written on first start.
 */
final class SettingsDefaults {

  static final String FILE_NAME = "settings.properties";

  static final String KEY_CONFIG_VERSION = "config-version";
  static final String KEY_TIME_ZONE = "time-zone";
  static final String KEY_DAY_SYNC_MODE = "day-sync-mode";
  static final String KEY_TIME_APPLY_MODE = "time-apply-mode";
  static final String KEY_TIME_SCALE = "time-scale";
  static final String KEY_UPDATE_INTERVAL = "update-interval-seconds";
  static final String KEY_EXCLUDED_WORLDS = "excluded-worlds";
  static final String KEY_PLAYER_JOIN_LISTENER = "player-join-sync-listener";
  static final String KEY_WORLD_LIFECYCLE_LISTENER = "world-lifecycle-listener";
  static final String KEY_DAYLIGHT_GUARD = "daylight-guard-enabled";
  static final String KEY_FALLBACK_GUARD_INTERVAL = "fallback-guard-interval-ticks";
  static final String KEY_EVENT_THROTTLE = "event-throttle-ms";
  static final String KEY_JUMP_THRESHOLD = "time-jump-warning-threshold-ticks";
  static final String KEY_WARN_NON_PAPER = "warn-non-paper";
  static final String KEY_BSTATS = "bstats-enabled";

  static final String TIME_ZONE = "Europe/Warsaw";
  static final DaySyncMode DAY_SYNC_MODE = DaySyncMode.OVERWORLD;
  static final TimeApplyMode TIME_APPLY_MODE = TimeApplyMode.SET_TIME;
  static final double TIME_SCALE = 1.0d;
  static final long UPDATE_INTERVAL_SECONDS = 60L;
  static final ListenerState PLAYER_JOIN_STATE = ListenerState.DISABLED;
  static final ListenerState WORLD_LIFECYCLE_STATE = ListenerState.ENABLED;
  static final boolean DAYLIGHT_GUARD = true;
  static final long FALLBACK_GUARD_INTERVAL_TICKS = 20L;
  static final long EVENT_THROTTLE_MILLIS = 0L;
  static final long JUMP_THRESHOLD_TICKS = 24_000L;
  static final boolean WARN_NON_PAPER = true;
  static final boolean BSTATS = true;

  static final long TICKS_PER_SECOND = 20L;
  static final long MIN_UPDATE_INTERVAL_SECONDS = 1L;
  static final long MIN_FALLBACK_GUARD_INTERVAL_TICKS = 1L;
  static final String SYSTEM_ZONE_ALIAS = "SYSTEM";

  static final String FILE_TEMPLATE = """
      # ---------------------------------------------------------------------------
      # Time Synchronization plugin - configuration
      # Values are case-insensitive on read.
      # Schema version; do not edit manually.
      # ---------------------------------------------------------------------------
      config-version=1

      # IANA time zone id used to compute the in-game time-of-day.
      # Use SYSTEM to follow the JVM's default zone.
      # Examples: Europe/Warsaw, America/New_York, Asia/Tokyo, UTC, SYSTEM.
      time-zone=Europe/Warsaw

      # How the day counter is synchronized with the real-world calendar.
      #   DISABLED   - Minecraft manages the day counter.
      #   OVERWORLD  - Day counter matches real calendar; Overworld worlds only.
      #   ALL_WORLDS - Day counter matches real calendar; every world.
      day-sync-mode=OVERWORLD

      # How time is applied while day-sync-mode=DISABLED.
      #   SET_TIME      - World#setTime. World age and moon phases preserved.
      #   SET_FULL_TIME - World#setFullTime computed from the world's current day.
      time-apply-mode=SET_TIME

      # Time progression multiplier, anchored at real 06:00 == Minecraft 06:00 (dawn).
      #   1.0 - normal.  2.0 - twice as fast.  0.5 - half speed.
      time-scale=1.0

      # How often the synchronization task runs, in seconds. Minimum: 1.
      update-interval-seconds=60

      # Comma-separated list of world names to skip entirely.
      excluded-worlds=

      # State of the player-join synchronization listener.
      #   DISABLED - never registered; locked, cannot be toggled via API.
      #   DYNAMIC  - not registered by default; toggled via public API.
      #   ENABLED  - registered by default; toggled via public API.
      player-join-sync-listener=DISABLED

      # State of the world-lifecycle listener (WorldInitEvent / WorldLoadEvent).
      world-lifecycle-listener=ENABLED

      # Periodically re-applies doDaylightCycle=false on managed worlds.
      daylight-guard-enabled=true

      # Interval of the fallback daylight guard, in ticks. Minimum: 1.
      # This guard only runs on servers without Paper's WorldGameRuleChangeEvent
      # (i.e. Spigot and most forks). On Paper it is not scheduled at all.
      fallback-guard-interval-ticks=20

      # Throttle for TimeSynchronizedEvent, in milliseconds. 0 disables throttling.
      # Useful when update-interval-seconds=1 and external listeners are expensive.
      event-throttle-ms=0

      # Log a warning when a single sync moves a world's time by at least this many ticks.
      # Set to 0 to disable the warning.
      time-jump-warning-threshold-ticks=24000

      # Show a startup warning when the server is not Paper-family.
      # Some features (e.g. WorldGameRuleChangeEvent) require Paper.
      warn-non-paper=true

      # Anonymous usage statistics via bStats. Set to false to opt out.
      bstats-enabled=true
      """;

  private SettingsDefaults() {
  }
}