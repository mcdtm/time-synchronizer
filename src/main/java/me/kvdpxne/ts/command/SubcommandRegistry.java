package me.kvdpxne.ts.command;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Ordered, case-insensitive lookup of {@link Subcommand}s.
 */
final class SubcommandRegistry {

  private final Map<String, Subcommand> byName = new LinkedHashMap<>();

  void register(final Subcommand subcommand) {
    this.byName.put(subcommand.name().toLowerCase(), subcommand);
  }

  Optional<Subcommand> find(final String name) {
    if (null == name) {
      return Optional.empty();
    }
    return Optional.ofNullable(this.byName.get(name.toLowerCase()));
  }

  List<Subcommand> all() {
    return List.copyOf(this.byName.values());
  }
}