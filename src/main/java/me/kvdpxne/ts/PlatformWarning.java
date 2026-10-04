package me.kvdpxne.ts;

import me.kvdpxne.ts.util.ServerPlatform;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Emits a one-shot advisory when the server is not Paper-family. Controlled by
 * the {@code warn-non-paper} configuration key.
 */
final class PlatformWarning {

  private PlatformWarning() {
  }

  static void warnIfNotPaper(final Logger logger, final boolean enabled) {
    Objects.requireNonNull(logger, "logger");
    if (!enabled || ServerPlatform.isPaperFamily()) {
      return;
    }
    logger.warning(() ->
        "Server platform '" + ServerPlatform.serverName() + "' is not Paper-family. "
            + "This plugin is optimized for Paper: features such as WorldGameRuleChangeEvent "
            + "are unavailable on this server. Set warn-non-paper=false to silence this warning.");
  }
}