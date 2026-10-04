package me.kvdpxne.ts.config;

import me.kvdpxne.ts.api.ListenerState;
import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsParserTest {

  private static final Logger SILENT = Logger.getAnonymousLogger();

  @Test
  void appliesDefaultsWhenKeysMissing() {
    final var settings = new SettingsParser(SILENT).parse(new Properties());
    assertAll(
      () -> assertEquals(0, settings.configVersion()),
      () -> assertEquals("Europe/Warsaw", settings.zoneId().getId()),
      () -> assertEquals(DaySyncMode.OVERWORLD, settings.daySyncMode()),
      () -> assertEquals(TimeApplyMode.SET_TIME, settings.timeApplyMode()),
      () -> assertEquals(1.0d, settings.scale()),
      () -> assertEquals(ListenerState.DISABLED, settings.playerJoinSyncListenerState()),
      () -> assertEquals(ListenerState.ENABLED, settings.worldLifecycleListenerState()),
      () -> assertTrue(settings.daylightGuardEnabled())
    );
  }

  @Test
  void invalidEnumFallsBackSilently() {
    final var props = new Properties();
    props.setProperty("day-sync-mode", "not-a-mode");
    assertEquals(DaySyncMode.OVERWORLD, new SettingsParser(SILENT).parse(props).daySyncMode());
  }

  @Test
  void clampsUpdateIntervalToMinimum() {
    final var props = new Properties();
    props.setProperty("update-interval-seconds", "0");
    assertEquals(20L, new SettingsParser(SILENT).parse(props).updateIntervalTicks());
  }

  @Test
  void parsesExcludedWorldsTrimmingWhitespace() {
    final var props = new Properties();
    props.setProperty("excluded-worlds", " world_nether , arena ,  ");
    final var settings = new SettingsParser(SILENT).parse(props);
    assertEquals(java.util.Set.of("world_nether", "arena"), settings.excludedWorlds());
  }

  @Test
  void systemZoneAliasIsCaseInsensitive() {
    final var props = new Properties();
    props.setProperty("time-zone", "system");
    assertEquals(java.time.ZoneId.systemDefault(),
      new SettingsParser(SILENT).parse(props).zoneId());
  }
}