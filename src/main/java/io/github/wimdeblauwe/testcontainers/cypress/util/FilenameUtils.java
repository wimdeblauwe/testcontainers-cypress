package io.github.wimdeblauwe.testcontainers.cypress.util;

import java.io.File;

/**
 * This code was copied from Apache commons-io (https://github.com/apache/commons-io/blob/commons-io-2.6/src/main/java/org/apache/commons/io/FilenameUtils.java)
 * to avoid the extra dependency on it from this library.
 */
public final class FilenameUtils {
    private static final int NOT_FOUND = -1;
    private static final char UNIX_SEPARATOR = '/';
    private static final char WINDOWS_SEPARATOR = '\\';
    private static final char SYSTEM_SEPARATOR = File.separatorChar;

    /**
     * Converts all separators to the Unix separator of forward slash.
     *
     * @param path the path to be changed, null ignored
     * @return the updated path
     */
    public static String separatorsToUnix(final String path) {
        if (path == null || path.indexOf(WINDOWS_SEPARATOR) == NOT_FOUND) {
            return path;
        }
        return path.replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
    }

    /**
     * Converts all separators to the Windows separator of backslash.
     *
     * @param path the path to be changed, null ignored
     * @return the updated path
     */
    public static String separatorsToWindows(final String path) {
        if (path == null || path.indexOf(UNIX_SEPARATOR) == NOT_FOUND) {
            return path;
        }
        return path.replace(UNIX_SEPARATOR, WINDOWS_SEPARATOR);
    }

    /**
     * Converts all separators to the system separator.
     *
     * @param path the path to be changed, null ignored
     * @return the updated path
     */
    public static String separatorsToSystem(final String path) {
        if (path == null) {
            return null;
        }
        if (isSystemWindows()) {
            return separatorsToWindows(path);
        } else {
            return separatorsToUnix(path);
        }
    }

    /**
     * Determines if Windows file system is in use.
     *
     * @return true if the system is Windows
     */
    static boolean isSystemWindows() {
        return SYSTEM_SEPARATOR == WINDOWS_SEPARATOR;
    }

    private FilenameUtils() {
    }
}
