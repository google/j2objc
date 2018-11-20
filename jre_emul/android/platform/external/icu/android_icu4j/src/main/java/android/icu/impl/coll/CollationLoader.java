/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
*
*   Copyright (C) 1996-2016, International Business Machines
*   Corporation and others.  All Rights Reserved.
*
*******************************************************************************
*
* CollationLoader.java, ported from ucol_res.cpp
*
* created by: Markus W. Scherer
*/

package android.icu.impl.coll;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.MissingResourceException;

import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.util.ICUUncheckedIOException;
import android.icu.util.Output;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;

/**
 * Convenience string denoting the Collation data tree
 * @hide Only a subset of ICU is exposed in Android
 */
public final class CollationLoader {

    // not implemented, all methods are static
    private CollationLoader() {
    }

    private static volatile String rootRules = null;

    private static void loadRootRules() {
        if (rootRules != null) {
            return;
        }
        synchronized(CollationLoader.class) {
            if (rootRules == null) {
                UResourceBundle rootBundle = UResourceBundle.getBundleInstance(
                        ICUData.ICU_COLLATION_BASE_NAME, ULocale.ROOT);
                rootRules = rootBundle.getString("UCARules");
            }
        }
    }

    // C++: static void appendRootRules(UnicodeString &s)
    public static String getRootRules() {
        loadRootRules();
        return rootRules;
    }

    /**
     * Simpler/faster methods for ASCII than ones based on Unicode data.
     * TODO: There should be code like this somewhere already??
     */
    private static final class ASCII {
        static String toLowerCase(String s) {
            for (int i = 0; i < s.length(); ++i) {
                char c = s.charAt(i);
                if ('A' <= c && c <= 'Z') {
                    StringBuilder sb = new StringBuilder(s.length());
                    sb.append(s, 0, i).append((char)(c + 0x20));
                    while (++i < s.length()) {
                        c = s.charAt(i);
                        if ('A' <= c && c <= 'Z') { c = (char)(c + 0x20); }
                        sb.append(c);
                    }
                    return sb.toString();
                }
            }
            return s;
        }
    }

    static String loadRules(ULocale locale, String collationType) {
        UResourceBundle bundle = UResourceBundle.getBundleInstance(
                ICUData.ICU_COLLATION_BASE_NAME, locale);
        UResourceBundle data = ((ICUResourceBundle)bundle).getWithFallback(
                "collations/" + ASCII.toLowerCase(collationType));
        String rules = data.getString("Sequence");
        return rules;
    }

    private static final UResourceBundle findWithFallback(UResourceBundle table, String entryName) {
        return ((ICUResourceBundle)table).findWithFallback(entryName);
    }

    public static CollationTailoring loadTailoring(ULocale locale, Output<ULocale> outValidLocale) {

        // Java porting note: ICU4J getWithFallback/getStringWithFallback currently does not
        // work well when alias table is involved in a resource path, unless full path is specified.
        // For now, collation resources does not contain such data, so the code below should work fine.

        CollationTailoring root = CollationRoot.getRoot();
        String localeName = locale.getName();
        if (localeName.length() == 0 || localeName.equals("root")) {
            outValidLocale.value = ULocale.ROOT;
            return root;
        }

        UResourceBundle bundle = null;
        try {
            bundle = ICUResourceBundle.getBundleInstance(
                    ICUData.ICU_COLLATION_BASE_NAME, locale,
                    ICUResourceBundle.OpenType.LOCALE_ROOT);
        } catch (MissingResourceException e) {
            outValidLocale.value = ULocale.ROOT;
            return root;
        }

        ULocale validLocale = bundle.getULocale();
        // Normalize the root locale. See
        // http://bugs.icu-project.org/trac/ticket/10715
        String validLocaleName = validLocale.getName();
        if (validLocaleName.length() == 0 || validLocaleName.equals("root")) {
            validLocale = ULocale.ROOT;
        }
        outValidLocale.value = validLocale;

        // There are zero or more tailorings in the collations table.
        UResourceBundle collations;
        try {
            collations = bundle.get("collations");
            if (collations == null) {
                return root;
            }
        } catch(MissingResourceException ignored) {
            return root;
        }

        // Fetch the collation type from the locale ID and the default type from the data.
        String type = locale.getKeywordValue("collation");
        String defaultType = "standard";

        String defT = ((ICUResourceBundle)collations).findStringWithFallback("default");
        if (defT != null) {
            defaultType = defT;
        }

        if (type == null || type.equals("default")) {
            type = defaultType;
        } else {
            type = ASCII.toLowerCase(type);
        }

        // Load the collations/type tailoring, with type fallback.

        // Java porting note: typeFallback is used for setting U_USING_DEFAULT_WARNING in
        // ICU4C, but not used by ICU4J

        // boolean typeFallback = false;
        UResourceBundle data = findWithFallback(collations, type);
        if (data == null &&
                type.length() > 6 && type.startsWith("search")) {
            // fall back from something like "searchjl" to "search"
            // typeFallback = true;
            type = "search";
            data = findWithFallback(collations, type);
        }

        if (data == null && !type.equals(defaultType)) {
            // fall back to the default type
            // typeFallback = true;
            type = defaultType;
            data = findWithFallback(collations, type);
        }

        if (data == null && !type.equals("standard")) {
            // fall back to the "standard" type
            // typeFallback = true;
            type = "standard";
            data = findWithFallback(collations, type);
        }

        if (data == null) {
            return root;
        }

        // Is this the same as the root collator? If so, then use that instead.
        ULocale actualLocale = data.getULocale();
        // http://bugs.icu-project.org/trac/ticket/10715 ICUResourceBundle(root).getULocale() != ULocale.ROOT
        // Therefore not just if (actualLocale.equals(ULocale.ROOT) && type.equals("standard")) {
        String actualLocaleName = actualLocale.getName();
        if (actualLocaleName.length() == 0 || actualLocaleName.equals("root")) {
            actualLocale = ULocale.ROOT;
            if (type.equals("standard")) {
                return root;
            }
        }

        CollationTailoring t = new CollationTailoring(root.settings);
        t.actualLocale = actualLocale;

        // deserialize
        UResourceBundle binary = data.get("%%CollationBin");
        ByteBuffer inBytes = binary.getBinary();
        try {
            CollationDataReader.read(root, inBytes, t);
        } catch (IOException e) {
            throw new ICUUncheckedIOException("Failed to load collation tailoring data for locale:"
                    + actualLocale + " type:" + type, e);
        }

        // Try to fetch the optional rules string.
        try {
            t.setRulesResource(data.get("Sequence"));
        } catch(MissingResourceException ignored) {
        }

        // Set the collation types on the informational locales,
        // except when they match the default types (for brevity and backwards compatibility).
        // For the valid locale, suppress the default type.
        if (!type.equals(defaultType)) {
            outValidLocale.value = validLocale.setKeywordValue("collation", type);
        }

        // For the actual locale, suppress the default type *according to the actual locale*.
        // For example, zh has default=pinyin and contains all of the Chinese tailorings.
        // zh_Hant has default=stroke but has no other data.
        // For the valid locale "zh_Hant" we need to suppress stroke.
        // For the actual locale "zh" we need to suppress pinyin instead.
        if (!actualLocale.equals(validLocale)) {
            // Opening a bundle for the actual locale should always succeed.
            UResourceBundle actualBundle = UResourceBundle.getBundleInstance(
                    ICUData.ICU_COLLATION_BASE_NAME, actualLocale);
            defT = ((ICUResourceBundle)actualBundle).findStringWithFallback("collations/default");
            if (defT != null) {
                defaultType = defT;
            }
        }

        if (!type.equals(defaultType)) {
            t.actualLocale = t.actualLocale.setKeywordValue("collation", type);
        }

        // if (typeFallback) {
        //     ICU4C implementation sets U_USING_DEFAULT_WARNING here
        // }

        return t;
    }
}
