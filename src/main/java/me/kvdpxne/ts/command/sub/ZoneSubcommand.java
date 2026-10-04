package me.kvdpxne.ts.command.sub;

import me.kvdpxne.ts.api.TimeSyncApi;
import me.kvdpxne.ts.command.Subcommand;
import me.kvdpxne.ts.exception.RuntimeNotStartedException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * {@code /timesync zone}           - prints the current zone.
 * {@code /timesync zone <zoneId>}  - updates the config and reloads.
 */
public final class ZoneSubcommand implements Subcommand {

  private static final int TAB_LIMIT = 40;

  private final TimeSyncApi api;

  public ZoneSubcommand(final TimeSyncApi api) {
    this.api = Objects.requireNonNull(api, "api");
  }

  @Override
  public String name() {
    return "zone";
  }

  @Override
  public String permission() {
    return "timesync.zone";
  }

  @Override
  public String usage() {
    return "zone [<zoneId>]";
  }

  @Override
  public boolean execute(final CommandSender sender, final String[] args) {
    if (0 == args.length) {
      return this.show(sender);
    }
    return this.apply(sender, args[0]);
  }

  @Override
  public List<String> tabComplete(final CommandSender sender, final String[] args) {
    if (1 != args.length) {
      return List.of();
    }
    final var prefix = args[0].toLowerCase();
    return ZoneId.getAvailableZoneIds().stream()
      .filter(id -> id.toLowerCase().startsWith(prefix))
      .sorted()
      .limit(TAB_LIMIT)
      .toList();
  }

  private boolean show(final CommandSender sender) {
    try {
      final var zone = this.api.settings().zoneId();
      sender.sendMessage(ChatColor.GRAY + "Current zone: "
        + ChatColor.WHITE + zone.getId());
    } catch (final RuntimeNotStartedException _) {
      sender.sendMessage(ChatColor.RED + "Runtime is not active; zone unavailable.");
    }
    return true;
  }

  private boolean apply(final CommandSender sender, final String raw) {
    final ZoneId zoneId;
    try {
      zoneId = ZoneId.of(raw.trim());
    } catch (final DateTimeException _) {
      sender.sendMessage(ChatColor.RED
        + "Unknown zone id: '" + raw + "'. Examples: Europe/Warsaw, America/New_York, UTC.");
      return true;
    }

    // ZoneId.of accepts some legacy aliases; restrict to IANA-canonical ids.
    if (!Set.copyOf(ZoneId.getAvailableZoneIds()).contains(zoneId.getId())) {
      sender.sendMessage(ChatColor.RED
        + "Zone '" + zoneId.getId() + "' is not in the IANA database.");
      return true;
    }

    sender.sendMessage(ChatColor.GRAY
      + "Setting zone to " + zoneId.getId() + " and reloading...");

    this.api.setZoneAsync(zoneId).thenAccept(result -> {
      if (result.success()) {
        sender.sendMessage(ChatColor.GREEN + "[OK] " + result.message());
      } else {
        sender.sendMessage(ChatColor.RED + "[FAIL] " + result.message());
      }
    });
    return true;
  }
}