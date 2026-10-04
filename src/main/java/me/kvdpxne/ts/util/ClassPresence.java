package me.kvdpxne.ts.util;

/**
 * Detects whether a class is loadable on the current runtime without triggering
 * initialization or surfacing {@link ClassNotFoundException} to callers.
 */
public final class ClassPresence {

  private ClassPresence() {
  }

  public static boolean isAvailable(final String className) {
    if (null == className || className.isBlank()) {
      return false;
    }
    try {
      Class.forName(className, false, ClassPresence.class.getClassLoader());
      return true;
    } catch (final ClassNotFoundException | LinkageError _) {
      return false;
    }
  }
}