/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 ******************************************************************************
 * Copyright (C) 1996-2007, International Business Machines Corporation and   *
 * others. All Rights Reserved.                                               *
 ******************************************************************************
 *
 ******************************************************************************
 */
 
package android.icu.impl;

import java.util.Locale;

/**
 * A class to hold utility functions missing from java.util.Locale.
 * @hide Only a subset of ICU is exposed in Android
 */
public class LocaleUtility {

    /**
     * A helper function to convert a string of the form
     * aa_BB_CC to a locale object.  Why isn't this in Locale?
     */
    public static Locale getLocaleFromName(String name) {
        String language = "";
        String country = "";
        String variant = "";

        int i1 = name.indexOf('_');
        if (i1 < 0) {
            language = name;
        } else {
            language = name.substring(0, i1);
            ++i1;
            int i2 = name.indexOf('_', i1);
            if (i2 < 0) {
                country = name.substring(i1);
            } else {
                country = name.substring(i1, i2);
                variant = name.substring(i2+1);
            }
        }

        return new Locale(language, country, variant);
    }

    /**
     * Compare two locale strings of the form aa_BB_CC, and
     * return true if parent is a 'strict' fallback of child, that is,
     * if child =~ "^parent(_.+)*" (roughly).
     */
    public static boolean isFallbackOf(String parent, String child) {
        if (!child.startsWith(parent)) {
            return false;
        }
        int i = parent.length();
        return (i == child.length() ||
                child.charAt(i) == '_');
    }

    /**
     * Compare two locales, and return true if the parent is a
     * 'strict' fallback of the child (parent string is a fallback
     * of child string).
     */
    public static boolean isFallbackOf(Locale parent, Locale child) {
        return isFallbackOf(parent.toString(), child.toString());
    }


    /*
     * Convenience method that calls canonicalLocaleString(String) with
     * locale.toString();
     */
    /*public static String canonicalLocaleString(Locale locale) {
        return canonicalLocaleString(locale.toString());
    }*/

    /*
     * You'd think that Locale canonicalizes, since it munges the
     * renamed languages, but it doesn't quite.  It forces the region
     * to be upper case but doesn't do anything about the language or
     * variant.  Our canonical form is 'lower_UPPER_UPPER'.  
     */
    /*public static String canonicalLocaleString(String id) {
        if (id != null) {
            int x = id.indexOf("_");
            if (x == -1) {
                id = id.toLowerCase(Locale.ENGLISH);
            } else {
                StringBuffer buf = new StringBuffer();
                buf.append(id.substring(0, x).toLowerCase(Locale.ENGLISH));
                buf.append(id.substring(x).toUpperCase(Locale.ENGLISH));

                int len = buf.length();
                int n = len;
                while (--n >= 0 && buf.charAt(n) == '_') {
                }
                if (++n != len) {
                    buf.delete(n, len);
                }
                id = buf.toString();
            }
        }
        return id;
    }*/

    /**
     * Fallback from the given locale name by removing the rightmost _-delimited
     * element. If there is none, return the root locale ("", "", ""). If this
     * is the root locale, return null. NOTE: The string "root" is not
     * recognized; do not use it.
     * 
     * @return a new Locale that is a fallback from the given locale, or null.
     */
    public static Locale fallback(Locale loc) {

        // Split the locale into parts and remove the rightmost part
        String[] parts = new String[]
            { loc.getLanguage(), loc.getCountry(), loc.getVariant() };
        int i;
        for (i=2; i>=0; --i) {
            if (parts[i].length() != 0) {
                parts[i] = "";
                break;
            }
        }
        if (i<0) {
            return null; // All parts were empty
        }
        return new Locale(parts[0], parts[1], parts[2]);
    }
}
