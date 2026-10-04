package me.kvdpxne.ts;

import me.kvdpxne.ts.time.ZonedTimeCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ZonedTimeCalculatorTest {

  private static final ZoneId UTC = ZoneOffset.UTC;
  private static final Clock NOON_UTC = Clock.fixed(
    Instant.parse("2025-06-15T12:00:00Z"), UTC);

  @ParameterizedTest
  @CsvSource({
    "06,  0,     0",       // dawn
    "12,  0,  6000",       // noon
    "18,  0, 12000",       // dusk
    "00,  0, 18000",       // midnight
    "23, 59, 17983",
    "00, 30, 18500",
  })
  void mapsWallClockToTimeOfDayAtUnitScale(final int hour, final int minute, final long expectedTick) {
    final var instant = Instant.parse("2025-06-15T%02d:%02d:00Z".formatted(hour, minute));
    final var calc = new ZonedTimeCalculator(UTC, 1.0d, Clock.fixed(instant, UTC));
    assertEquals(expectedTick, calc.currentSnapshot().timeOfDay());
  }

  @Test
  void fullTimeFollowsEpochDay() {
    final var epochDawn = new ZonedTimeCalculator(
      UTC, 1.0d, Clock.fixed(Instant.parse("1970-01-01T06:00:00Z"), UTC));
    final var nextDayDawn = new ZonedTimeCalculator(
      UTC, 1.0d, Clock.fixed(Instant.parse("1970-01-02T06:00:00Z"), UTC));
    assertAll(
      () -> assertEquals(0L, epochDawn.currentSnapshot().fullTime()),
      () -> assertEquals(24_000L, nextDayDawn.currentSnapshot().fullTime())
    );
  }

  @Test
  void respectsZoneId() {
    // 12:30 UTC == 14:30 in Warsaw (CEST in June) -> tick 8500.
    final var calc = new ZonedTimeCalculator(
      ZoneId.of("Europe/Warsaw"), 1.0d, NOON_UTC);
    assertEquals(8_000L, calc.currentSnapshot().timeOfDay());
  }

  @Test
  void scaleTwoHalvesTheRealDay() {
    // Real 12:00 UTC with scale=2 -> shifted 6 real hours -> 12 in-game hours -> dusk (12000).
    final var calc = new ZonedTimeCalculator(UTC, 2.0d, NOON_UTC);
    assertEquals(12_000L, calc.currentSnapshot().timeOfDay());
  }

  @Test
  void anchorIsPreservedAtAnyScale() {
    final var dawn = Clock.fixed(Instant.parse("2025-06-15T06:00:00Z"), UTC);
    final var scaleTwo = new ZonedTimeCalculator(UTC, 2.0d, dawn);
    final var scaleHalf = new ZonedTimeCalculator(UTC, 0.5d, dawn);
    assertAll(
      () -> assertEquals(0L, scaleTwo.currentSnapshot().timeOfDay()),
      () -> assertEquals(0L, scaleHalf.currentSnapshot().timeOfDay())
    );
  }

  @Test
  void rejectsNonPositiveScale() {
    assertThrows(IllegalArgumentException.class,
      () -> new ZonedTimeCalculator(UTC, 0.0d, NOON_UTC));
  }
}