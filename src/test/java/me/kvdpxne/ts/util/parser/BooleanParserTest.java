package me.kvdpxne.ts.util.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BooleanParserTest {

  @Test
  void parsesTrueFalseCaseInsensitively() {
    assertEquals(Boolean.TRUE, BooleanParser.tryParse("TRUE").orElseThrow());
    assertEquals(Boolean.FALSE, BooleanParser.tryParse(" false ").orElseThrow());
  }

  @Test
  void emptyForUnknown() {
    assertTrue(BooleanParser.tryParse("yes").isEmpty());
    assertTrue(BooleanParser.tryParse(null).isEmpty());
  }
}