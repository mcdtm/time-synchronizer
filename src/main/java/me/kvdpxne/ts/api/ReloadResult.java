package me.kvdpxne.ts.api;

/**
 * Outcome of a {@link TimeSyncApi#reloadAsync()} call.
 *
 * @param success {@code true} when the runtime was rebuilt and swapped
 * @param message human-readable summary, safe for command output
 */
public record ReloadResult(boolean success, String message) {

  public static ReloadResult success(final String message) {
    return new ReloadResult(true, message);
  }

  public static ReloadResult failure(final String message) {
    return new ReloadResult(false, message);
  }
}