package me.kvdpxne.ts.config;

import me.kvdpxne.ts.exception.ConfigurationLoadException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class PropertiesSettingsLoader implements SettingsLoader, SettingsWriter {

  private static final String ZONE_KEY_PREFIX = SettingsDefaults.KEY_TIME_ZONE + "=";

  private final File dataFolder;
  private final Logger logger;

  public PropertiesSettingsLoader(final File dataFolder, final Logger logger) {
    this.dataFolder = Objects.requireNonNull(dataFolder, "dataFolder");
    this.logger = Objects.requireNonNull(logger, "logger");
  }

  @Override
  public PluginSettings load() throws ConfigurationLoadException {
    final var file = this.ensureFileExists();
    final var properties = this.readProperties(file);
    return new SettingsParser(this.logger).parse(properties);
  }

  @Override
  public void updateZone(final ZoneId zoneId) throws ConfigurationLoadException {
    Objects.requireNonNull(zoneId, "zoneId");
    final var file = this.ensureFileExists();
    this.rewriteZoneLine(file, zoneId.getId());
    this.logger.info(() -> "Updated time-zone to " + zoneId.getId());
  }

  private File ensureFileExists() throws ConfigurationLoadException {
    final var file = new File(this.dataFolder, SettingsDefaults.FILE_NAME);
    if (file.exists()) {
      return file;
    }
    if (!this.dataFolder.exists() && !this.dataFolder.mkdirs()) {
      throw new ConfigurationLoadException(
        "Cannot create plugin data folder: " + this.dataFolder);
    }
    try {
      Files.writeString(file.toPath(), SettingsDefaults.FILE_TEMPLATE, UTF_8);
    } catch (final IOException e) {
      throw new ConfigurationLoadException(
        "Cannot write default settings file: " + file.getAbsolutePath(), e);
    }
    this.logger.info(() -> "Created default settings file at " + file.getAbsolutePath());
    return file;
  }

  private Properties readProperties(final File file) throws ConfigurationLoadException {
    final var properties = new Properties();
    try (final Reader reader = new InputStreamReader(new FileInputStream(file), UTF_8)) {
      properties.load(reader);
    } catch (final IOException e) {
      throw new ConfigurationLoadException(
        "Cannot read settings file: " + file.getAbsolutePath(), e);
    }
    return properties;
  }

  /**
   * Replaces only the {@code time-zone=...} line, preserving every comment and
   * unrelated key. When no such line exists, one is appended at the end so the
   * parser will pick it up on the next load.
   */
  private void rewriteZoneLine(final File file, final String zoneId) throws ConfigurationLoadException {
    final var lines = this.readAllLines(file);
    final var output = new ArrayList<String>(lines.size() + 1);
    boolean replaced = false;
    for (final var line : lines) {
      if (!replaced && line.stripLeading().startsWith(ZONE_KEY_PREFIX)) {
        output.add(SettingsDefaults.KEY_TIME_ZONE + "=" + zoneId);
        replaced = true;
      } else {
        output.add(line);
      }
    }
    if (!replaced) {
      output.add(SettingsDefaults.KEY_TIME_ZONE + "=" + zoneId);
    }
    this.writeAllLines(file, output);
  }

  private List<String> readAllLines(final File file) throws ConfigurationLoadException {
    try {
      return Files.readAllLines(file.toPath(), UTF_8);
    } catch (final IOException e) {
      throw new ConfigurationLoadException(
        "Cannot read settings file: " + file.getAbsolutePath(), e);
    }
  }

  private void writeAllLines(final File file, final java.util.List<String> lines)
    throws ConfigurationLoadException {
    try {
      Files.write(file.toPath(), lines, UTF_8);
    } catch (final IOException e) {
      throw new ConfigurationLoadException(
        "Cannot write settings file: " + file.getAbsolutePath(), e);
    }
  }
}