package me.kvdpxne.ts.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimeSnapshotTest {

  @ParameterizedTest
  @ValueSource(longs = {-1L, 24_000L, 24_001L})
  void rejectsOutOfRangeTimeOfDay(final long timeOfDay) {
    assertThrows(IllegalArgumentException.class, () -> new TimeSnapshot(timeOfDay, 0L));
  }

  @Test
  void rejectsNegativeFullTime() {
    assertThrows(IllegalArgumentException.class, () -> new TimeSnapshot(0L, -1L));
  }

  @Test
  void acceptsBoundaryValues() {
    assertDoesNotThrow(() -> new TimeSnapshot(0L, 0L));
    assertDoesNotThrow(() -> new TimeSnapshot(23_999L, Long.MAX_VALUE));
  }
}