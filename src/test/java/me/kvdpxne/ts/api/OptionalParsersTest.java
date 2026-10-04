package me.kvdpxne.ts.api;

import me.kvdpxne.ts.config.DaySyncMode;
import me.kvdpxne.ts.config.TimeApplyMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OptionalParsersTest {

  @ParameterizedTest
  @ValueSource(strings = {"disabled", "DISABLED", "  Disabled ", "dynamic", "enabled"})
  void listenerStateParsesCaseInsensitively(final String raw) {
    assertTrue(ListenerState.tryFromString(raw).isPresent());
  }

  @Test
  void listenerStateReturnsEmptyForUnknown() {
    assertTrue(ListenerState.tryFromString("nonsense").isEmpty());
    assertTrue(ListenerState.tryFromString(null).isEmpty());
  }

  @Test
  void daySyncModeReturnsEmptyForUnknown() {
    assertTrue(DaySyncMode.tryFromString("x").isEmpty());
  }

  @Test
  void timeApplyModeParsesKnownValues() {
    assertEquals(TimeApplyMode.SET_TIME, TimeApplyMode.tryFromString("SET_TIME").orElseThrow());
    assertEquals(TimeApplyMode.SET_FULL_TIME,
        TimeApplyMode.tryFromString(" set_full_time ").orElseThrow());
  }
}