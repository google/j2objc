/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2009-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */

package android.icu.text;

import java.util.ArrayList;
import java.util.Locale;
import java.util.MissingResourceException;

import android.icu.impl.CacheBase;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SoftCache;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import android.icu.util.UResourceBundle;
import android.icu.util.UResourceBundleIterator;


/**
 * <code>NumberingSystem</code> is the base class for all number
 * systems. This class provides the interface for setting different numbering
 * system types, whether it be a simple alternate digit system such as
 * Thai digits or Devanagari digits, or an algorithmic numbering system such
 * as Hebrew numbering or Chinese numbering.
 *
 * @author       John Emmons
 */
public class NumberingSystem {
    private static final String[] OTHER_NS_KEYWORDS = { "native", "traditional", "finance" };

    /**
     * Default constructor.  Returns a numbering system that uses the Western decimal
     * digits 0 through 9.
     */
    public NumberingSystem() {
        radix = 10;
        algorithmic = false;
        desc = "0123456789";
        name = "latn";
    }

    /**
     * Factory method for creating a numbering system.
     * @param radix_in The radix for this numbering system.  ICU currently
     * supports only numbering systems whose radix is 10.
     * @param isAlgorithmic_in Specifies whether the numbering system is algorithmic
     * (true) or numeric (false).
     * @param desc_in String used to describe the characteristics of the numbering
     * system.  For numeric systems, this string contains the digits used by the
     * numbering system, in order, starting from zero.  For algorithmic numbering
     * systems, the string contains the name of the RBNF ruleset in the locale's
     * NumberingSystemRules section that will be used to format numbers using
     * this numbering system.
     */
    public static NumberingSystem getInstance(int radix_in, boolean isAlgorithmic_in, String desc_in ) {
        return getInstance(null,radix_in,isAlgorithmic_in,desc_in);
    }

    /**
     * Factory method for creating a numbering system.
     * @param name_in The string representing the name of the numbering system.
     * @param radix_in The radix for this numbering system.  ICU currently
     * supports only numbering systems whose radix is 10.
     * @param isAlgorithmic_in Specifies whether the numbering system is algorithmic
     * (true) or numeric (false).
     * @param desc_in String used to describe the characteristics of the numbering
     * system.  For numeric systems, this string contains the digits used by the
     * numbering system, in order, starting from zero.  For algorithmic numbering
     * systems, the string contains the name of the RBNF ruleset in the locale's
     * NumberingSystemRules section that will be used to format numbers using
     * this numbering system.
     */

    private static NumberingSystem getInstance(String name_in, int radix_in, boolean isAlgorithmic_in, String desc_in ) {
        if ( radix_in < 2 ) {
            throw new IllegalArgumentException("Invalid radix for numbering system");
        }

        if ( !isAlgorithmic_in ) {
            if ( desc_in.length() != radix_in || !isValidDigitString(desc_in)) {
                throw new IllegalArgumentException("Invalid digit string for numbering system");
            }
        }
        NumberingSystem ns = new NumberingSystem();
        ns.radix = radix_in;
        ns.algorithmic = isAlgorithmic_in;
        ns.desc = desc_in;
        ns.name = name_in;
        return ns;
    }

    /**
     * Returns the default numbering system for the specified locale.
     */
    public static NumberingSystem getInstance(Locale inLocale) {
        return getInstance(ULocale.forLocale(inLocale));
    }

    /**
     * Returns the default numbering system for the specified ULocale.
     */
    public static NumberingSystem getInstance(ULocale locale) {
        // Check for @numbers
        boolean nsResolved = true;
        String numbersKeyword = locale.getKeywordValue("numbers");
        if (numbersKeyword != null ) {
            for ( String keyword : OTHER_NS_KEYWORDS ) {
                if ( numbersKeyword.equals(keyword)) {
                    nsResolved = false;
                    break;
                }
            }
        } else {
            numbersKeyword = "default";
            nsResolved = false;
        }

        if (nsResolved) {
            NumberingSystem ns = getInstanceByName(numbersKeyword);
            if (ns != null) {
                return ns;
            }
            // If the @numbers keyword points to a bogus numbering system name,
            // we return the default for the locale.
            numbersKeyword = "default";
        }

        // Attempt to get the numbering system from the cache
        String baseName = locale.getBaseName();
        // TODO: Caching by locale+numbersKeyword could yield a large cache.
        // Try to load for each locale the mappings from OTHER_NS_KEYWORDS and default
        // to real numbering system names; can we get those from supplemental data?
        // Then look up those mappings for the locale and resolve the keyword.
        String key = baseName+"@numbers="+numbersKeyword;
        LocaleLookupData localeLookupData = new LocaleLookupData(locale, numbersKeyword);
        return cachedLocaleData.getInstance(key, localeLookupData);
    }

    private static class LocaleLookupData {
        public final ULocale locale;
        public final String numbersKeyword;

        LocaleLookupData(ULocale locale, String numbersKeyword) {
            this.locale = locale;
            this.numbersKeyword = numbersKeyword;
        }
    }

    static NumberingSystem lookupInstanceByLocale(LocaleLookupData localeLookupData) {
        ULocale locale = localeLookupData.locale;
        ICUResourceBundle rb;
        try {
            rb = (ICUResourceBundle)UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, locale);
            rb = rb.getWithFallback("NumberElements");
        } catch (MissingResourceException ex) {
            return new NumberingSystem();
        }

        String numbersKeyword = localeLookupData.numbersKeyword;
        String resolvedNumberingSystem = null;
        for (;;) {
            try {
                resolvedNumberingSystem = rb.getStringWithFallback(numbersKeyword);
                break;
            } catch (MissingResourceException ex) { // Fall back behavior as defined in TR35
                if (numbersKeyword.equals("native") || numbersKeyword.equals("finance")) {
                    numbersKeyword = "default";
                } else if (numbersKeyword.equals("traditional")) {
                    numbersKeyword = "native";
                } else {
                    break;
                }
            }
        }

        NumberingSystem ns = null;
        if (resolvedNumberingSystem != null) {
            ns = getInstanceByName(resolvedNumberingSystem);
        }

        if (ns == null) {
            ns = new NumberingSystem();
        }
        return ns;
    }

    /**
     * Returns the default numbering system for the default <code>FORMAT</code> locale.
     * @see Category#FORMAT
     */
    public static NumberingSystem getInstance() {
        return getInstance(ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Returns a numbering system from one of the predefined numbering systems
     * known to ICU.  Numbering system names are based on the numbering systems
     * defined in CLDR.  To get a list of available numbering systems, use the
     * getAvailableNames method.
     * @param name The name of the desired numbering system.  Numbering system
     * names often correspond with the name of the script they are associated
     * with.  For example, "thai" for Thai digits, "hebr" for Hebrew numerals.
     */
    public static NumberingSystem getInstanceByName(String name) {
        // Get the numbering system from the cache.
        return cachedStringData.getInstance(name, null /* unused */);
    }

    private static NumberingSystem lookupInstanceByName(String name) {
        int radix;
        boolean isAlgorithmic;
        String description;
        try {
            UResourceBundle numberingSystemsInfo = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "numberingSystems");
            UResourceBundle nsCurrent = numberingSystemsInfo.get("numberingSystems");
            UResourceBundle nsTop = nsCurrent.get(name);

            description = nsTop.getString("desc");
            UResourceBundle nsRadixBundle = nsTop.get("radix");
            UResourceBundle nsAlgBundle = nsTop.get("algorithmic");
            radix = nsRadixBundle.getInt();
            int algorithmic = nsAlgBundle.getInt();

            isAlgorithmic = ( algorithmic == 1 );

        } catch (MissingResourceException ex) {
            return null;
        }

        return getInstance(name, radix, isAlgorithmic, description);
    }

    /**
     * Returns a string array containing a list of the names of numbering systems
     * currently known to ICU.
     */
    public static String [] getAvailableNames() {

            UResourceBundle numberingSystemsInfo = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "numberingSystems");
            UResourceBundle nsCurrent = numberingSystemsInfo.get("numberingSystems");
            UResourceBundle temp;

            String nsName;
            ArrayList<String> output = new ArrayList<String>();
            UResourceBundleIterator it = nsCurrent.getIterator();
            while (it.hasNext()) {
                temp = it.next();
                nsName = temp.getKey();
                output.add(nsName);
            }
            return output.toArray(new String[output.size()]);
    }

    /**
     * Convenience method to determine if a given digit string is valid for use as a
     * descriptor of a numeric ( non-algorithmic ) numbering system.  In order for
     * a digit string to be valid, it must contain exactly ten Unicode code points.
     */
    public static boolean isValidDigitString(String str) {
        int numCodepoints = str.codePointCount(0, str.length());
        return (numCodepoints == 10);
    }

    /**
     * Returns the radix of the current numbering system.
     */
    public int getRadix() {
        return radix;
    }

    /**
     * Returns the description string of the current numbering system.
     * The description string describes the characteristics of the numbering
     * system.  For numeric systems, this string contains the digits used by the
     * numbering system, in order, starting from zero.  For algorithmic numbering
     * systems, the string contains the name of the RBNF ruleset in the locale's
     * NumberingSystemRules section that will be used to format numbers using
     * this numbering system.
     */
    public String getDescription() {
        return desc;
    }

    /**
     * Returns the string representing the name of the numbering system.
     */
    public String getName() {
        return name;
    }
    /**
     * Returns the numbering system's algorithmic status.  If true,
     * the numbering system is algorithmic and uses an RBNF formatter to
     * format numerals.  If false, the numbering system is numeric and
     * uses a fixed set of digits.
     */
    public boolean isAlgorithmic() {
        return algorithmic;
    }

    private String desc;
    private int radix;
    private boolean algorithmic;
    private String name;

    /**
     * Cache to hold the NumberingSystems by Locale.
     */
    private static CacheBase<String, NumberingSystem, LocaleLookupData> cachedLocaleData =
            new SoftCache<String, NumberingSystem, LocaleLookupData>() {
        @Override
        protected NumberingSystem createInstance(String key, LocaleLookupData localeLookupData) {
            return lookupInstanceByLocale(localeLookupData);
        }
    };

    /**
     * Cache to hold the NumberingSystems by name.
     */
    private static CacheBase<String, NumberingSystem, Void>  cachedStringData =
            new SoftCache<String, NumberingSystem, Void>() {
        @Override
        protected NumberingSystem createInstance(String key, Void unused) {
            return lookupInstanceByName(key);
        }
    };
}
