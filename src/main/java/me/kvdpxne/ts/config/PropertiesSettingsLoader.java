package me.kvdpxne.ts.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Reads {@link PluginSettings} from a plain {@code .properties} file.
 * <p>
 * The file is created with defaults when it does not exist yet. Every invalid
 * value is logged as a warning and replaced by a safe default, so a malformed
 * file never prevents the plugin from starting.
 */
public final class PropertiesSettingsLoader implements SettingsLoader {

  private static final String FILE_NAME = "settings.properties";

  private static final String KEY_TIME_ZONE = "time-zone";
  private static final String KEY_DAY_SYNC_MODE = "day-sync-mode";
  private static final String KEY_TIME_APPLY_MODE = "time-apply-mode";

  private static final String DEFAULT_TIME_ZONE = "Europe/Warsaw";
  private static final DaySyncMode DEFAULT_DAY_SYNC_MODE = DaySyncMode.OVERWORLD;
  private static final TimeApplyMode DEFAULT_TIME_APPLY_MODE = TimeApplyMode.SET_TIME;

  private static final ZoneId FALLBACK_ZONE = ZoneOffset.UTC;

  private static final String DEFAULT_FILE_CONTENT = """
      # ---------------------------------------------------------------------------
      # Time Synchronization plugin - configuration
      # All values are case-insensitive on read.
      # ---------------------------------------------------------------------------

      # IANA time zone id used to compute the in-game time-of-day.
      # Examples: Europe/Warsaw, America/New_York, Asia/Tokyo, UTC.
      time-zone=Europe/Warsaw

      # How the day counter is synchronized with the real-world calendar.
      #   DISABLED   - Minecraft manages the day counter.
      #                Only time-of-day is synced; moon phases are preserved.
      #   OVERWORLD  - Day counter matches the real calendar.
      #                Applied to Overworld worlds only.
      #   ALL_WORLDS - Day counter matches the real calendar.
      #                Applied to every world.
      # When this is not DISABLED, time-apply-mode is ignored and
      # World#setFullTime is used (moon phases are lost).
      day-sync-mode=OVERWORLD

      # How time is applied to a world while day-sync-mode=DISABLED.
      #   SET_TIME      - World#setTime. World age and moon phases preserved.
      #   SET_FULL_TIME - World#setFullTime computed from the world's current day.
      #                   Behaviourally equivalent to SET_TIME.
      # Ignored when day-sync-mode is not DISABLED.
      time-apply-mode=SET_TIME
      """;

  private final File dataFolder;
  private final Logger logger;

  public PropertiesSettingsLoader(final File dataFolder, final Logger logger) {
    this.dataFolder = Objects.requireNonNull(dataFolder, "dataFolder");
    this.logger = Objects.requireNonNull(logger, "logger");
  }

  @Override
  public PluginSettings load() throws IOException {
    final var file = new File(dataFolder, FILE_NAME);
    ensureFileExists(file);

    final var properties = new Properties();
    try (final Reader reader = new InputStreamReader(new FileInputStream(file), UTF_8)) {
      properties.load(reader);
    }

    return parse(properties);
  }

  private void ensureFileExists(final File file) throws IOException {
    if (file.exists()) {
      return;
    }
    if (!dataFolder.exists() && !dataFolder.mkdirs()) {
      throw new IOException("Cannot create plugin data folder: " + dataFolder);
    }
    Files.writeString(file.toPath(), DEFAULT_FILE_CONTENT, UTF_8);
    logger.info(() -> "Created default settings file at " + file.getAbsolutePath());
  }

  private PluginSettings parse(final Properties properties) {
    final var zoneRaw = properties.getProperty(KEY_TIME_ZONE, DEFAULT_TIME_ZONE);
    final var daySyncRaw = properties.getProperty(KEY_DAY_SYNC_MODE, DEFAULT_DAY_SYNC_MODE.name());
    final var timeApplyRaw = properties.getProperty(KEY_TIME_APPLY_MODE, DEFAULT_TIME_APPLY_MODE.name());

    final var zoneId = parseZoneId(zoneRaw);
    final var daySyncMode = parseDaySyncMode(daySyncRaw);
    final var timeApplyMode = parseTimeApplyMode(timeApplyRaw);

    if (DaySyncMode.DISABLED != daySyncMode && TimeApplyMode.SET_TIME == timeApplyMode) {
      logger.warning(() ->
        "time-apply-mode=SET_TIME is ignored because day-sync-mode=" + daySyncMode
          + " (setFullTime is required to sync day counter)");
    }

    return new PluginSettings(zoneId, daySyncMode, timeApplyMode);
  }

  private ZoneId parseZoneId(final String raw) {
    try {
      return ZoneId.of(raw.trim());
    } catch (final DateTimeException _) {
      logger.warning(() ->
        "Invalid time-zone '" + raw + "', falling back to " + FALLBACK_ZONE);
      return FALLBACK_ZONE;
    }
  }

  private DaySyncMode parseDaySyncMode(final String raw) {
    try {
      return DaySyncMode.fromString(raw);
    } catch (final IllegalArgumentException cause) {
      logger.warning(() -> cause.getMessage()
        + "; falling back to " + DEFAULT_DAY_SYNC_MODE);
      return DEFAULT_DAY_SYNC_MODE;
    }
  }

  private TimeApplyMode parseTimeApplyMode(final String raw) {
    try {
      return TimeApplyMode.fromString(raw);
    } catch (final IllegalArgumentException cause) {
      logger.warning(() -> cause.getMessage()
        + "; falling back to " + DEFAULT_TIME_APPLY_MODE);
      return DEFAULT_TIME_APPLY_MODE;
    }
  }

  /** Exposed for tests; not part of the public runtime API. */
  static String defaultFileName() {
    return FILE_NAME;
  }

  /** Exposed for tests; returns the exact bytes written on first start. */
  static String defaultFileContent() {
    return DEFAULT_FILE_CONTENT;
  }

  // Silences an unused-import warning in IDEs that do not analyze switch expressions.
  @SuppressWarnings("unused")
  private static Locale localeAnchor() {
    return Locale.ROOT;
  }
}