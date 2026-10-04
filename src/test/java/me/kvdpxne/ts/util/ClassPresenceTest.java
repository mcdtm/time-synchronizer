package me.kvdpxne.ts.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassPresenceTest {

  @Test
  void detectsKnownJdkClass() {
    assertTrue(ClassPresence.isAvailable("java.lang.String"));
  }

  @Test
  void falseForMissingClass() {
    assertFalse(ClassPresence.isAvailable("com.example.DoesNotExist"));
  }

  @Test
  void falseForBlank() {
    assertFalse(ClassPresence.isAvailable(""));
    assertFalse(ClassPresence.isAvailable(null));
  }
}