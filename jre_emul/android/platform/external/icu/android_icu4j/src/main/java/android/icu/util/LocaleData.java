/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 **************************************************************************************
 * Copyright (C) 2009-2016, International Business Machines Corporation,
 * Google, Inc. and others. All Rights Reserved.
 **************************************************************************************
 */
package android.icu.util;

import java.util.MissingResourceException;

import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.text.UnicodeSet;
import android.icu.util.ULocale.Category;

/**
 * A class for accessing miscellaneous data in the locale bundles
 * @author ram
 * @hide Only a subset of ICU is exposed in Android
 */
public final class LocaleData {

    //    private static final String EXEMPLAR_CHARS      = "ExemplarCharacters";
    private static final String MEASUREMENT_SYSTEM  = "MeasurementSystem";
    private static final String PAPER_SIZE          = "PaperSize";
    private static final String LOCALE_DISPLAY_PATTERN  = "localeDisplayPattern";
    private static final String PATTERN             = "pattern";
    private static final String SEPARATOR           = "separator";
    private boolean noSubstitute;
    private ICUResourceBundle bundle;
    private ICUResourceBundle langBundle;

    /**
     * EXType for {@link #getExemplarSet(int, int)}.
     * Corresponds to the 'main' (aka 'standard') CLDR exemplars in
     * <a href="http://www.unicode.org/reports/tr35/tr35-general.html#Character_Elements">
     *   http://www.unicode.org/reports/tr35/tr35-general.html#Character_Elements</a>.
     */
    public static final int ES_STANDARD = 0;

    /**
     * EXType for {@link #getExemplarSet(int, int)}.
     * Corresponds to the 'auxiliary' CLDR exemplars in
     * <a href="http://www.unicode.org/reports/tr35/tr35-general.html#Character_Elements">
     *   http://www.unicode.org/reports/tr35/tr35-general.html#Character_Elements</a>.
     */
    public static final int ES_AUXILIARY = 1;

    /**
     * EXType for {@link #getExemplarSet(int, int)}.
     * Corresponds to the 'index' CLDR exemplars in
     * <a href="http://www.unicode.org/reports/tr35/tr35-general.html#Character_Elements">
     *   http://www.unicode.org/reports/tr35/tr35-general.html#Character_Elements</a>.
     */
    public static final int ES_INDEX = 2;

    /**
     * EXType for {@link #getExemplarSet(int, int)}.
     * Corresponds to the 'currencySymbol' CLDR exemplars in
     * <a href="http://www.unicode.org/reports/tr35/tr35-general.html#Character_Elements">
     *   http://www.unicode.org/reports/tr35/tr35-general.html#Character_Elements</a>.
     * Note: This type is no longer supported.
     * @deprecated ICU 51
     */
    @Deprecated
    public static final int ES_CURRENCY = 3;

    /**
     * Corresponds to the 'punctuation' CLDR exemplars in
     * <a href="http://www.unicode.org/reports/tr35/tr35-general.html#Character_Elements">
     *   http://www.unicode.org/reports/tr35/tr35-general.html#Character_Elements</a>.
     * EXType for {@link #getExemplarSet(int, int)}.
     */
    public static final int ES_PUNCTUATION = 4;

    /**
     * Count of EXTypes for {@link #getExemplarSet(int, int)}.
     * @deprecated ICU 58 The numeric value may change over time, see ICU ticket #12420.
     */
    @Deprecated
    public static final int ES_COUNT = 5;

    /**
     * Delimiter type for {@link #getDelimiter(int)}.
     */
    public static final int QUOTATION_START = 0;

    /**
     * Delimiter type for {@link #getDelimiter(int)}.
     */
    public static final int QUOTATION_END = 1;

    /**
     * Delimiter type for {@link #getDelimiter(int)}.
     */
    public static final int ALT_QUOTATION_START = 2;

    /**
     * Delimiter type for {@link #getDelimiter(int)}.
     */
    public static final int ALT_QUOTATION_END = 3;

    /**
     * Count of delimiter types for {@link #getDelimiter(int)}.
     * @deprecated ICU 58 The numeric value may change over time, see ICU ticket #12420.
     */
    @Deprecated
    public static final int DELIMITER_COUNT = 4;

    // private constructor to prevent default construction
    ///CLOVER:OFF
    private LocaleData(){}
    ///CLOVER:ON

    /**
     * Returns the set of exemplar characters for a locale. Equivalent to calling {@link #getExemplarSet(ULocale, int, int)} with
     * the extype == {@link #ES_STANDARD}.
     *
     * @param locale    Locale for which the exemplar character set
     *                  is to be retrieved.
     * @param options   Bitmask for options to apply to the exemplar pattern.
     *                  Specify zero to retrieve the exemplar set as it is
     *                  defined in the locale data.  Specify
     *                  UnicodeSet.CASE to retrieve a case-folded exemplar
     *                  set.  See {@link UnicodeSet#applyPattern(String,
     *                  int)} for a complete list of valid options.  The
     *                  IGNORE_SPACE bit is always set, regardless of the
     *                  value of 'options'.
     * @return          The set of exemplar characters for the given locale.
     */
    public static UnicodeSet getExemplarSet(ULocale locale, int options) {
        return LocaleData.getInstance(locale).getExemplarSet(options, ES_STANDARD);
    }

    /**
     * Returns the set of exemplar characters for a locale.
     * Equivalent to calling new LocaleData(locale).{@link #getExemplarSet(int, int)}.
     *
     * @param locale    Locale for which the exemplar character set
     *                  is to be retrieved.
     * @param options   Bitmask for options to apply to the exemplar pattern.
     *                  Specify zero to retrieve the exemplar set as it is
     *                  defined in the locale data.  Specify
     *                  UnicodeSet.CASE to retrieve a case-folded exemplar
     *                  set.  See {@link UnicodeSet#applyPattern(String,
     *                  int)} for a complete list of valid options.  The
     *                  IGNORE_SPACE bit is always set, regardless of the
     *                  value of 'options'.
     * @param extype    The type of exemplar character set to retrieve.
     * @return          The set of exemplar characters for the given locale.
     */
    public static UnicodeSet getExemplarSet(ULocale locale, int options, int extype) {
        return LocaleData.getInstance(locale).getExemplarSet(options, extype);
    }

    /**
     * Returns the set of exemplar characters for a locale.
     *
     * @param options   Bitmask for options to apply to the exemplar pattern.
     *                  Specify zero to retrieve the exemplar set as it is
     *                  defined in the locale data.  Specify
     *                  UnicodeSet.CASE to retrieve a case-folded exemplar
     *                  set.  See {@link UnicodeSet#applyPattern(String,
     *                  int)} for a complete list of valid options.  The
     *                  IGNORE_SPACE bit is always set, regardless of the
     *                  value of 'options'.
     * @param extype    The type of exemplar set to be retrieved,
     *                  ES_STANDARD, ES_INDEX, ES_AUXILIARY, or ES_PUNCTUATION
     * @return          The set of exemplar characters for the given locale.
     *                  If there is nothing available for the locale,
     *                  then null is returned if {@link #getNoSubstitute()} is true, otherwise the
     *                  root value is returned (which may be UnicodeSet.EMPTY).
     * @exception       RuntimeException if the extype is invalid.
     */
    public UnicodeSet getExemplarSet(int options, int extype) {
        String [] exemplarSetTypes = {
                "ExemplarCharacters",
                "AuxExemplarCharacters",
                "ExemplarCharactersIndex",
                "ExemplarCharactersCurrency",
                "ExemplarCharactersPunctuation"
        };

        if (extype == ES_CURRENCY) {
            // currency symbol exemplar is no longer available
            return noSubstitute ? null : UnicodeSet.EMPTY;
        }

        try{
            final String aKey = exemplarSetTypes[extype]; // will throw an out-of-bounds exception
            ICUResourceBundle stringBundle = (ICUResourceBundle) bundle.get(aKey);

            if (noSubstitute && !bundle.isRoot() && stringBundle.isRoot()) {
                return null;
            }
            String unicodeSetPattern = stringBundle.getString();
            return new UnicodeSet(unicodeSetPattern, UnicodeSet.IGNORE_SPACE | options);
        } catch (ArrayIndexOutOfBoundsException aiooe) {
            throw new IllegalArgumentException(aiooe);
        } catch (Exception ex){
            return noSubstitute ? null : UnicodeSet.EMPTY;
        }
    }

    /**
     * Gets the LocaleData object associated with the ULocale specified in locale
     *
     * @param locale    Locale with thich the locale data object is associated.
     * @return          A locale data object.
     */
    public static final LocaleData getInstance(ULocale locale) {
        LocaleData ld = new LocaleData();
        ld.bundle = (ICUResourceBundle)UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, locale);
        ld.langBundle = (ICUResourceBundle)UResourceBundle.getBundleInstance(ICUData.ICU_LANG_BASE_NAME, locale);
        ld.noSubstitute = false;
        return ld;
    }

    /**
     * Gets the LocaleData object associated with the default <code>FORMAT</code> locale
     *
     * @return          A locale data object.
     * @see Category#FORMAT
     */
    public static final LocaleData getInstance() {
        return LocaleData.getInstance(ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Sets the "no substitute" behavior of this locale data object.
     *
     * @param setting   Value for the no substitute behavior.  If TRUE,
     *                  methods of this locale data object will return
     *                  an error when no data is available for that method,
     *                  given the locale ID supplied to the constructor.
     */
    public void setNoSubstitute(boolean setting) {
        noSubstitute = setting;
    }

    /**
     * Gets the "no substitute" behavior of this locale data object.
     *
     * @return          Value for the no substitute behavior.  If TRUE,
     *                  methods of this locale data object will return
     *                  an error when no data is available for that method,
     *                  given the locale ID supplied to the constructor.
     */
    public boolean getNoSubstitute() {
        return noSubstitute;
    }

    private static final String [] DELIMITER_TYPES = {
        "quotationStart",
        "quotationEnd",
        "alternateQuotationStart",
        "alternateQuotationEnd"
    };

    /**
     * Retrieves a delimiter string from the locale data.
     *
     * @param type      The type of delimiter string desired.  Currently,
     *                  the valid choices are QUOTATION_START, QUOTATION_END,
     *                  ALT_QUOTATION_START, or ALT_QUOTATION_END.
     * @return          The desired delimiter string.
     */
    public String getDelimiter(int type) {
        ICUResourceBundle delimitersBundle = (ICUResourceBundle) bundle.get("delimiters");
        // Only some of the quotation marks may be here. So we make sure that we do a multilevel fallback.
        ICUResourceBundle stringBundle = delimitersBundle.getWithFallback(DELIMITER_TYPES[type]);

        if (noSubstitute && !bundle.isRoot() && stringBundle.isRoot()) {
            return null;
        }
        return stringBundle.getString();
    }

    /**
     * Utility for getMeasurementSystem and getPaperSize
     */
    private static UResourceBundle measurementTypeBundleForLocale(ULocale locale, String measurementType){
        // Much of this is taken from getCalendarType in impl/CalendarUtil.java
        UResourceBundle measTypeBundle = null;
        String region = ULocale.getRegionForSupplementalData(locale, true);
        try {
            UResourceBundle rb = UResourceBundle.getBundleInstance(
                    ICUData.ICU_BASE_NAME,
                    "supplementalData",
                    ICUResourceBundle.ICU_DATA_CLASS_LOADER);
            UResourceBundle measurementData = rb.get("measurementData");
            UResourceBundle measDataBundle = null;
            try {
                measDataBundle = measurementData.get(region);
                measTypeBundle = measDataBundle.get(measurementType);
            } catch (MissingResourceException mre) {
                // use "001" as fallback
                measDataBundle = measurementData.get("001");
                measTypeBundle = measDataBundle.get(measurementType);
            }
        } catch (MissingResourceException mre) {
            // fall through
        }
        return measTypeBundle;
    }


    /**
     * Enumeration for representing the measurement systems.
     */
    public static final class MeasurementSystem{
        /**
         * Measurement system specified by Le Syst&#x00E8;me International d'Unit&#x00E9;s (SI)
         * otherwise known as Metric system.
         */
        public static final MeasurementSystem SI = new MeasurementSystem();

        /**
         * Measurement system followed in the United States of America.
         */
        public static final MeasurementSystem US = new MeasurementSystem();

        /**
         * Mix of metric and imperial units used in Great Britain.
         */
        public static final MeasurementSystem UK = new MeasurementSystem();

        private MeasurementSystem() {}
    }

    /**
     * Returns the measurement system used in the locale specified by the locale.
     *
     * @param locale      The locale for which the measurement system to be retrieved.
     * @return MeasurementSystem the measurement system used in the locale.
     */
    public static final MeasurementSystem getMeasurementSystem(ULocale locale){
        UResourceBundle sysBundle = measurementTypeBundleForLocale(locale, MEASUREMENT_SYSTEM);

        switch (sysBundle.getInt()) {
        case 0: return MeasurementSystem.SI;
        case 1: return MeasurementSystem.US;
        case 2: return MeasurementSystem.UK;
        default:
            // return null if the object is null or is not an instance
            // of integer indicating an error
            return null;
        }
    }

    /**
     * A class that represents the size of letter head
     * used in the country
     */
    public static final class PaperSize{
        private int height;
        private int width;

        private PaperSize(int h, int w){
            height = h;
            width = w;
        }
        /**
         * Retruns the height of the paper
         * @return the height
         */
        public int getHeight(){
            return height;
        }
        /**
         * Returns the width of the paper
         * @return the width
         */
        public int getWidth(){
            return width;
        }
    }

    /**
     * Returns the size of paper used in the locale. The paper sizes returned are always in
     * <em>milli-meters</em>.
     * @param locale The locale for which the measurement system to be retrieved.
     * @return The paper size used in the locale
     */
    public static final PaperSize getPaperSize(ULocale locale){
        UResourceBundle obj = measurementTypeBundleForLocale(locale, PAPER_SIZE);
        int[] size = obj.getIntVector();
        return new PaperSize(size[0], size[1]);
    }

    /**
     * Returns LocaleDisplayPattern for this locale, e.g., {0}({1})
     * @return locale display pattern as a String.
     */
    public String getLocaleDisplayPattern() {
        ICUResourceBundle locDispBundle = (ICUResourceBundle) langBundle.get(LOCALE_DISPLAY_PATTERN);
        String localeDisplayPattern = locDispBundle.getStringWithFallback(PATTERN);
        return localeDisplayPattern;
    }

    /**
     * Returns LocaleDisplaySeparator for this locale.
     * @return locale display separator as a char.
     */
    public String getLocaleSeparator() {
        String sub0 = "{0}";
        String sub1 = "{1}";
        ICUResourceBundle locDispBundle = (ICUResourceBundle) langBundle.get(LOCALE_DISPLAY_PATTERN);
        String  localeSeparator = locDispBundle.getStringWithFallback(SEPARATOR);
        int index0 = localeSeparator.indexOf(sub0);
        int index1 = localeSeparator.indexOf(sub1);
        if (index0 >= 0 && index1 >= 0 && index0 <= index1) {
            return localeSeparator.substring(index0 + sub0.length(), index1);
        }
        return localeSeparator;
    }

    private static VersionInfo gCLDRVersion = null;

    /**
     * Returns the current CLDR version
     */
    public static VersionInfo getCLDRVersion() {
        // fetching this data should be idempotent.
        if(gCLDRVersion == null) {
            // from ZoneMeta.java
            UResourceBundle supplementalDataBundle = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
            UResourceBundle cldrVersionBundle = supplementalDataBundle.get("cldrVersion");
            gCLDRVersion = VersionInfo.getInstance(cldrVersionBundle.getString());
        }
        return gCLDRVersion;
    }
}
