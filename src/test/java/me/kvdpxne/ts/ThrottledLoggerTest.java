package me.kvdpxne.ts;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThrottledLoggerTest {

  @Test
  void emitsAtMostOncePerWindow() {
    final var records = new ArrayList<LogRecord>();
    final var logger = captureInto(records);
    final var throttled = new ThrottledLogger(logger, 60_000L);

    throttled.warn("world", () -> "first");
    throttled.warn("world", () -> "second");
    throttled.warn("other", () -> "third");

    assertEquals(2, records.size());
  }

  @Test
  void forgetAllowsNextEmission() {
    final var records = new ArrayList<LogRecord>();
    final var logger = captureInto(records);
    final var throttled = new ThrottledLogger(logger, 60_000L);

    throttled.warn("world", () -> "a");
    throttled.forget("world");
    throttled.warn("world", () -> "b");

    assertEquals(2, records.size());
  }

  private static Logger captureInto(final List<LogRecord> sink) {
    final var logger = Logger.getAnonymousLogger();
    logger.setUseParentHandlers(false);
    logger.addHandler(new Handler() {
      @Override public void publish(final LogRecord record) {
        sink.add(record);
      }
      @Override public void flush() { }
      @Override public void close() { }
    });
    return logger;
  }
}