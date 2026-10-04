package me.kvdpxne.ts.util.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnumParserTest {

  private enum Sample { ALPHA, BETA }

  @ParameterizedTest
  @ValueSource(strings = {"alpha", "ALPHA", "  Alpha "})
  void parsesCaseInsensitivelyAndTrims(final String raw) {
    assertEquals(Sample.ALPHA, EnumParser.tryParse(Sample.class, raw).orElseThrow());
  }

  @Test
  void emptyForUnknown() {
    assertTrue(EnumParser.tryParse(Sample.class, "gamma").isEmpty());
  }

  @Test
  void emptyForNull() {
    assertTrue(EnumParser.tryParse(Sample.class, null).isEmpty());
  }

  @Test
  void rejectsNullClass() {
    assertThrows(NullPointerException.class, () -> EnumParser.tryParse(null, "x"));
  }
}