/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2001-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.impl;

import android.icu.util.VersionInfo;

/**
 * @hide Only a subset of ICU is exposed in Android
 */
public final class ICUDebug {
    private static String params;
    static {
        try {
            params = System.getProperty("ICUDebug");
        }
        catch (SecurityException e) {
        }
    }
    private static boolean debug = params != null;
    private static boolean help = debug && (params.equals("") || params.indexOf("help") != -1);

    static {
        if (debug) {
            System.out.println("\nICUDebug=" + params);
        }
    }

    public static final String javaVersionString = System.getProperty("java.version", "0");
    public static final boolean isJDK14OrHigher;
    public static final VersionInfo javaVersion;

    public static VersionInfo getInstanceLenient(String s) {
        // Extracting ASCII numbers up to 4 delimited by
        // any non digit characters
        int[] ver = new int[4];
        boolean numeric = false;
        int i = 0, vidx = 0;
        while (i < s.length()) {
            char c = s.charAt(i++);
            if (c < '0' || c > '9') {
                if (numeric) {
                    if (vidx == 3) {
                        // up to 4 numbers
                        break;
                    }
                    numeric = false;
                    vidx++;
                }
            } else {
                if (numeric) {
                    ver[vidx] = ver[vidx] * 10 + (c - '0');
                    if (ver[vidx] > 255) {
                        // VersionInfo does not support numbers
                        // greater than 255.  In such case, we
                        // ignore the number and the rest
                        ver[vidx] = 0;
                        break;
                    }
                } else {
                    numeric = true;
                    ver[vidx] = c - '0';
                }
            }
        }

        return VersionInfo.getInstance(ver[0], ver[1], ver[2], ver[3]);
    }

    static {
        javaVersion = getInstanceLenient(javaVersionString);

        VersionInfo java14Version = VersionInfo.getInstance("1.4.0");

        isJDK14OrHigher = javaVersion.compareTo(java14Version) >= 0;
    }

    public static boolean enabled() {
        return debug;
    }

    public static boolean enabled(String arg) {
        if (debug) {
            boolean result = params.indexOf(arg) != -1;
            if (help) System.out.println("\nICUDebug.enabled(" + arg + ") = " + result);
            return result;
        }
        return false;
    }

    public static String value(String arg) {
        String result = "false";
        if (debug) {
            int index = params.indexOf(arg);
            if (index != -1) {
                index += arg.length();
                if (params.length() > index && params.charAt(index) == '=') {
                    index += 1;
                    int limit = params.indexOf(",", index);
                    result = params.substring(index, limit == -1 ? params.length() : limit);
                } else {
                    result = "true";
                }
            }

            if (help) System.out.println("\nICUDebug.value(" + arg + ") = " + result);
        }
        return result;
    }

//    static public void main(String[] args) {
//        // test
//        String[] tests = {
//            "1.3.0",
//            "1.3.0_02",
//            "1.3.1ea",
//            "1.4.1b43",
//            "___41___5",
//            "x1.4.51xx89ea.7f",
//            "1.6_2009",
//            "10-100-1000-10000",
//            "beta",
//            "0",
//        };
//        for (int i = 0; i < tests.length; ++i) {
//            System.out.println(tests[i] + " => " + getInstanceLenient(tests[i]));
//        }
//    }
}
