package me.kvdpxne.ts.util.parser;

import java.util.Locale;
import java.util.Optional;

/**
 * Case-insensitive boolean parser. Accepts only {@code true} and {@code false}.
 */
public final class BooleanParser {

  private BooleanParser() {
  }

  public static Optional<Boolean> tryParse(final String raw) {
    if (null == raw) {
      return Optional.empty();
    }
    return switch (raw.trim().toLowerCase(Locale.ROOT)) {
      case "true" -> Optional.of(Boolean.TRUE);
      case "false" -> Optional.of(Boolean.FALSE);
      default -> Optional.empty();
    };
  }
}