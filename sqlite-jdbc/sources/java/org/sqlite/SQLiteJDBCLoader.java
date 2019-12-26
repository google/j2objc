/**
 * SQLiteJDBCLoader stub for iOS version.
 *
 * Author: DaeHoon Zee
 */

package org.sqlite;

public class SQLiteJDBCLoader {
    private static final boolean extracted = true;

    public SQLiteJDBCLoader() {
    }

    public static synchronized boolean initialize() throws Exception {
        return extracted;
    }

    public static boolean isNativeMode() throws Exception {
        return extracted;
    }

    public static int getMajorVersion() {
        String[] c = getVersion().split("\\.");
        return c.length > 0 ? Integer.parseInt(c[0]) : 1;
    }

    public static int getMinorVersion() {
        String[] c = getVersion().split("\\.");
        return c.length > 1 ? Integer.parseInt(c[1]) : 0;
    }

    public static String getVersion() {
        return "3.28.0";
    }
}
