package me.kvdpxne.ts.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExceptionHierarchyTest {

  @Test
  void everyCustomExceptionExtendsBase() {
    final var samples = new TimeSyncException[] {
        new ConfigurationLoadException("boom"),
        new NotMainThreadException("op"),
        new RuntimeNotStartedException(),
        new UnknownListenerException("X"),
        new ListenerLockedException("X"),
    };
    for (final var sample : samples) {
      assertInstanceOf(TimeSyncException.class, sample);
    }
  }

  @Test
  void notMainThreadIncludesOperationName() {
    final var ex = new NotMainThreadException("myOp");
    assertTrue(ex.getMessage().contains("myOp"));
  }

  @Test
  void listenerLockedIncludesName() {
    final var ex = new ListenerLockedException("PlayerJoin");
    assertTrue(ex.getMessage().contains("PlayerJoin"));
  }

  @Test
  void configurationLoadPreservesCause() {
    final var cause = new java.io.IOException("disk");
    final var ex = new ConfigurationLoadException("failed", cause);
    assertEquals(cause, ex.getCause());
  }
}