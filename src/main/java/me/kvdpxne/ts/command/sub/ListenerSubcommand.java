package me.kvdpxne.ts.command.sub;

import me.kvdpxne.ts.api.TimeSyncApi;
import me.kvdpxne.ts.command.Subcommand;
import me.kvdpxne.ts.exception.ListenerLockedException;
import me.kvdpxne.ts.exception.RuntimeNotStartedException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * {@code /timesync listener <name> [enabled|disabled]} - view or toggle a listener.
 * <p>
 * Names: {@code player-join}, {@code world-lifecycle}.
 * States: {@code enabled}, {@code disabled}.
 */
public final class ListenerSubcommand implements Subcommand {

  private static final String PLAYER_JOIN = "player-join";
  private static final String WORLD_LIFECYCLE = "world-lifecycle";
  private static final String ENABLED = "enabled";
  private static final String DISABLED = "disabled";

  private final TimeSyncApi api;

  public ListenerSubcommand(final TimeSyncApi api) {
    this.api = Objects.requireNonNull(api, "api");
  }

  @Override
  public String name() {
    return "listener";
  }

  @Override
  public String permission() {
    return "timesync.listener";
  }

  @Override
  public String usage() {
    return "listener <" + PLAYER_JOIN + "|" + WORLD_LIFECYCLE + "> ["
      + ENABLED + "|" + DISABLED + "]";
  }

  @Override
  public boolean execute(final CommandSender sender, final String[] args) {
    try {
      if (0 == args.length) {
        this.show(sender, PLAYER_JOIN);
        this.show(sender, WORLD_LIFECYCLE);
        return true;
      }
      final var name = args[0].toLowerCase(Locale.ROOT);
      if (2 == args.length) {
        this.apply(sender, name, args[1]);
        return true;
      }
      this.show(sender, name);
      return true;
    } catch (final RuntimeNotStartedException _) {
      sender.sendMessage(ChatColor.RED
        + "Runtime is not active; listener state is unavailable.");
      return true;
    }
  }

  @Override
  public List<String> tabComplete(final CommandSender sender, final String[] args) {
    if (1 == args.length) {
      final var prefix = args[0].toLowerCase(Locale.ROOT);
      return List.of(PLAYER_JOIN, WORLD_LIFECYCLE).stream()
        .filter(name -> name.startsWith(prefix))
        .toList();
    }
    if (2 == args.length) {
      final var prefix = args[1].toLowerCase(Locale.ROOT);
      return List.of(ENABLED, DISABLED).stream()
        .filter(state -> state.startsWith(prefix))
        .toList();
    }
    return List.of();
  }

  private void show(final CommandSender sender, final String name) {
    final var status = this.resolve(name).map(ListenerHandle::describe).orElse(null);
    if (null == status) {
      sender.sendMessage(ChatColor.RED + "Unknown listener: " + name);
      return;
    }
    sender.sendMessage(ChatColor.GRAY + name + ": " + ChatColor.WHITE + status);
  }

  private void apply(final CommandSender sender, final String name, final String state) {
    final var parsed = switch (state.toLowerCase(Locale.ROOT)) {
      case ENABLED -> Optional.of(Boolean.TRUE);
      case DISABLED -> Optional.of(Boolean.FALSE);
      default -> Optional.<Boolean>empty();
    };
    if (parsed.isEmpty()) {
      sender.sendMessage(ChatColor.RED
        + "Invalid state '" + state + "'; expected enabled or disabled");
      return;
    }
    final var handle = this.resolve(name);
    if (handle.isEmpty()) {
      sender.sendMessage(ChatColor.RED + "Unknown listener: " + name);
      return;
    }
    try {
      handle.get().apply(parsed.get());
      sender.sendMessage(ChatColor.GREEN
        + name + " is now " + state.toLowerCase(Locale.ROOT));
    } catch (final ListenerLockedException e) {
      sender.sendMessage(ChatColor.RED + e.getMessage());
    }
  }

  private Optional<ListenerHandle> resolve(final String name) {
    return switch (name) {
      case PLAYER_JOIN -> Optional.of(new ListenerHandle(
        this.api::isPlayerJoinSyncListenerEnabled,
        this.api::isPlayerJoinSyncListenerMutable,
        this.api::setPlayerJoinSyncListenerEnabled));
      case WORLD_LIFECYCLE -> Optional.of(new ListenerHandle(
        this.api::isWorldLifecycleListenerEnabled,
        this.api::isWorldLifecycleListenerMutable,
        this.api::setWorldLifecycleListenerEnabled));
      default -> Optional.empty();
    };
  }

  private record ListenerHandle(
    BooleanSupplier enabled,
    BooleanSupplier mutable,
    Consumer<Boolean> setter
  ) {
    String describe() {
      final var state = this.enabled.getAsBoolean() ? "ENABLED" : "DISABLED";
      return this.mutable.getAsBoolean() ? state + " (mutable)" : state + " (locked)";
    }

    void apply(final boolean value) {
      this.setter.accept(value);
    }
  }
}