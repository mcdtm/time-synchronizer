package me.kvdpxne.ts.util.parser;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Case-insensitive, trimming enum lookup that never throws on bad input.
 */
public final class EnumParser {

  private EnumParser() {
  }

  /**
   * @return the parsed constant, or {@link Optional#empty()} when {@code raw}
   *         is null/unknown or does not match any constant of {@code enumClass}
   */
  public static <E extends Enum<E>> Optional<E> tryParse(
      final Class<E> enumClass,
      final String raw
  ) {
    Objects.requireNonNull(enumClass, "enumClass");
    if (null == raw) {
      return Optional.empty();
    }
    try {
      return Optional.of(Enum.valueOf(enumClass, raw.trim().toUpperCase(Locale.ROOT)));
    } catch (final IllegalArgumentException _) {
      return Optional.empty();
    }
  }
}