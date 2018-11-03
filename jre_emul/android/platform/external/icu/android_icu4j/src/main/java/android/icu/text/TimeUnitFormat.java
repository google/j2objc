/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 **************************************************************************
 * Copyright (C) 2008-2014, Google, International Business Machines
 * Corporation and others. All Rights Reserved.
 **************************************************************************
 */
package android.icu.text;

import java.io.ObjectStreamException;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.TreeMap;

import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.UResource;
import android.icu.util.Measure;
import android.icu.util.TimeUnit;
import android.icu.util.TimeUnitAmount;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import android.icu.util.UResourceBundle;


/**
 * Format or parse a TimeUnitAmount, using plural rules for the units where available.
 *
 * <P>
 * Code Sample:
 * <pre>
 *   // create a time unit instance.
 *   // only SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, and YEAR are supported
 *   TimeUnit timeUnit = TimeUnit.SECOND;
 *   // create time unit amount instance - a combination of Number and time unit
 *   TimeUnitAmount source = new TimeUnitAmount(2, timeUnit);
 *   // create time unit format instance
 *   TimeUnitFormat format = new TimeUnitFormat();
 *   // set the locale of time unit format
 *   format.setLocale(new ULocale("en"));
 *   // format a time unit amount
 *   String formatted = format.format(source);
 *   System.out.println(formatted);
 *   try {
 *       // parse a string into time unit amount
 *       TimeUnitAmount result = (TimeUnitAmount) format.parseObject(formatted);
 *       // result should equal to source
 *   } catch (ParseException e) {
 *   }
 * </pre>
 *
 * <P>
 * @see TimeUnitAmount
 * @see MeasureFormat
 * @author markdavis
 * @deprecated ICU 53 use {@link MeasureFormat} instead.
 * @hide Only a subset of ICU is exposed in Android
 */
@Deprecated
public class TimeUnitFormat extends MeasureFormat {

    /**
     * Constant for full name style format.
     * For example, the full name for "hour" in English is "hour" or "hours".
     * @deprecated ICU 53 see {@link MeasureFormat.FormatWidth}
     */
    @Deprecated
    public static final int FULL_NAME = 0;
    /**
     * Constant for abbreviated name style format.
     * For example, the abbreviated name for "hour" in English is "hr" or "hrs".
     * @deprecated ICU 53 see {@link MeasureFormat.FormatWidth}
     */
    @Deprecated
    public static final int ABBREVIATED_NAME = 1;

    private static final int TOTAL_STYLES = 2;

    private static final long serialVersionUID = -3707773153184971529L;

    // These fields are supposed to be the same as the fields in mf. They
    // are here for serialization backward compatibility and to support parsing.
    private NumberFormat format;
    private ULocale locale;
    private int style;

    // We use this field in lieu of the super class because the super class
    // is immutable while this class is mutable. The contents of the super class
    // is an empty shell. Every public method of the super class is overridden to
    // delegate to this field. Each time this object mutates, it replaces this field with
    // a new immutable instance.
    private transient MeasureFormat mf;

    private transient Map<TimeUnit, Map<String, Object[]>> timeUnitToCountToPatterns;
    private transient PluralRules pluralRules;
    private transient boolean isReady;

    private static final String DEFAULT_PATTERN_FOR_SECOND = "{0} s";
    private static final String DEFAULT_PATTERN_FOR_MINUTE = "{0} min";
    private static final String DEFAULT_PATTERN_FOR_HOUR = "{0} h";
    private static final String DEFAULT_PATTERN_FOR_DAY = "{0} d";
    private static final String DEFAULT_PATTERN_FOR_WEEK = "{0} w";
    private static final String DEFAULT_PATTERN_FOR_MONTH = "{0} m";
    private static final String DEFAULT_PATTERN_FOR_YEAR = "{0} y";

    /**
     * Create empty format using full name style, for example, "hours".
     * Use setLocale and/or setFormat to modify.
     * @deprecated ICU 53 use {@link MeasureFormat} instead.
     */
    @Deprecated
    public TimeUnitFormat() {
        mf = MeasureFormat.getInstance(ULocale.getDefault(), FormatWidth.WIDE);
        isReady = false;
        style = FULL_NAME;
    }

    /**
     * Create TimeUnitFormat given a ULocale, and using full name style.
     * @param locale   locale of this time unit formatter.
     * @deprecated ICU 53 use {@link MeasureFormat} instead.
     */
    @Deprecated
    public TimeUnitFormat(ULocale locale) {
        this(locale, FULL_NAME);
    }

    /**
     * Create TimeUnitFormat given a Locale, and using full name style.
     * @param locale   locale of this time unit formatter.
     * @deprecated ICU 53 use {@link MeasureFormat} instead.
     */
    @Deprecated
    public TimeUnitFormat(Locale locale) {
        this(locale, FULL_NAME);
    }

    /**
     * Create TimeUnitFormat given a ULocale and a formatting style.
     * @param locale   locale of this time unit formatter.
     * @param style    format style, either FULL_NAME or ABBREVIATED_NAME style.
     * @throws IllegalArgumentException if the style is not FULL_NAME or
     *                                  ABBREVIATED_NAME style.
     * @deprecated ICU 53 use {@link MeasureFormat} instead.
     */
    @Deprecated
    public TimeUnitFormat(ULocale locale, int style) {
        if (style < FULL_NAME || style >= TOTAL_STYLES) {
            throw new IllegalArgumentException("style should be either FULL_NAME or ABBREVIATED_NAME style");
        }
        mf = MeasureFormat.getInstance(
                locale, style == FULL_NAME ? FormatWidth.WIDE : FormatWidth.SHORT);
        this.style = style;

        // Needed for getLocale(ULocale.VALID_LOCALE)
        setLocale(locale, locale);
        this.locale = locale;
        isReady = false;
    }

    private TimeUnitFormat(ULocale locale, int style, NumberFormat numberFormat) {
        this(locale, style);
        if (numberFormat != null) {
            setNumberFormat((NumberFormat) numberFormat.clone());
        }
    }

    /**
     * Create TimeUnitFormat given a Locale and a formatting style.
     * @deprecated ICU 53 use {@link MeasureFormat} instead.
     */
    @Deprecated
    public TimeUnitFormat(Locale locale, int style) {
        this(ULocale.forLocale(locale),  style);
    }

    /**
     * Set the locale used for formatting or parsing.
     * @param locale   locale of this time unit formatter.
     * @return this, for chaining.
     * @deprecated ICU 53 see {@link MeasureFormat}.
     */
    @Deprecated
    public TimeUnitFormat setLocale(ULocale locale) {
        if (locale != this.locale){
            mf = mf.withLocale(locale);

            // Needed for getLocale(ULocale.VALID_LOCALE)
            setLocale(locale, locale);
            this.locale = locale;
            isReady = false;
        }
        return this;
    }

    /**
     * Set the locale used for formatting or parsing.
     * @param locale   locale of this time unit formatter.
     * @return this, for chaining.
     * @deprecated ICU 53 see {@link MeasureFormat}.
     */
    @Deprecated
    public TimeUnitFormat setLocale(Locale locale) {
        return setLocale(ULocale.forLocale(locale));
    }

    /**
     * Set the format used for formatting or parsing. Passing null is equivalent to passing
     * {@link NumberFormat#getNumberInstance(ULocale)}.
     * @param format   the number formatter.
     * @return this, for chaining.
     * @deprecated ICU 53 see {@link MeasureFormat}.
     */
    @Deprecated
    public TimeUnitFormat setNumberFormat(NumberFormat format) {
        if (format == this.format) {
            return this;
        }
        if (format == null) {
            if (locale == null) {
                isReady = false;
                mf = mf.withLocale(ULocale.getDefault());
            } else {
                this.format = NumberFormat.getNumberInstance(locale);
                mf = mf.withNumberFormat(this.format);
            }
        } else {
            this.format = format;
            mf = mf.withNumberFormat(this.format);
        }
        return this;
    }


    /**
     * Format a TimeUnitAmount.
     * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer, java.text.FieldPosition)
     * @deprecated ICU 53 see {@link MeasureFormat}.
     */
    @Deprecated
    public StringBuffer format(Object obj, StringBuffer toAppendTo,
            FieldPosition pos) {
        return mf.format(obj, toAppendTo, pos);
    }

    /**
     * Parse a TimeUnitAmount.
     * @see java.text.Format#parseObject(java.lang.String, java.text.ParsePosition)
     * @deprecated ICU 53 see {@link MeasureFormat}.
     */
    @Deprecated
    @Override
    public TimeUnitAmount parseObject(String source, ParsePosition pos) {
        if (!isReady) {
            setup();
        }
        Number resultNumber = null;
        TimeUnit resultTimeUnit = null;
        int oldPos = pos.getIndex();
        int newPos = -1;
        int longestParseDistance = 0;
        String countOfLongestMatch = null;
        // we don't worry too much about speed on parsing, but this can be optimized later if needed.
        // Parse by iterating through all available patterns
        // and looking for the longest match.
        for (TimeUnit timeUnit : timeUnitToCountToPatterns.keySet()) {
            Map<String, Object[]> countToPattern = timeUnitToCountToPatterns.get(timeUnit);
            for (Entry<String, Object[]> patternEntry : countToPattern.entrySet()) {
                String count = patternEntry.getKey();
                for (int styl = FULL_NAME; styl < TOTAL_STYLES; ++styl) {
                    MessageFormat pattern = (MessageFormat) (patternEntry.getValue())[styl];
                    pos.setErrorIndex(-1);
                    pos.setIndex(oldPos);
                    // see if we can parse
                    Object parsed = pattern.parseObject(source, pos);
                    if (pos.getErrorIndex() != -1 || pos.getIndex() == oldPos) {
                        // nothing parsed
                        continue;
                    }
                    Number temp = null;
                    if (((Object[]) parsed).length != 0) {
                        // pattern with Number as beginning,
                        // such as "{0} d".
                        // check to make sure that the timeUnit is consistent
                        Object tempObj = ((Object[]) parsed)[0];
                        if (tempObj instanceof Number) {
                            temp = (Number) tempObj;
                        } else {
                            // Since we now format the number ourselves, parseObject will likely give us back a String
                            // for
                            // the number. When this happens we must parse the formatted number ourselves.
                            try {
                                temp = format.parse(tempObj.toString());
                            } catch (ParseException e) {
                                continue;
                            }
                        }
                    }
                    int parseDistance = pos.getIndex() - oldPos;
                    if (parseDistance > longestParseDistance) {
                        resultNumber = temp;
                        resultTimeUnit = timeUnit;
                        newPos = pos.getIndex();
                        longestParseDistance = parseDistance;
                        countOfLongestMatch = count;
                    }
                }
            }
        }
        /*
         * After find the longest match, parse the number. Result number could be null for the pattern without number
         * pattern. such as unit pattern in Arabic. When result number is null, use plural rule to set the number.
         */
        if (resultNumber == null && longestParseDistance != 0) {
            // set the number using plurrual count
            if (countOfLongestMatch.equals("zero")) {
                resultNumber = Integer.valueOf(0);
            } else if (countOfLongestMatch.equals("one")) {
                resultNumber = Integer.valueOf(1);
            } else if (countOfLongestMatch.equals("two")) {
                resultNumber = Integer.valueOf(2);
            } else {
                // should not happen.
                // TODO: how to handle?
                resultNumber = Integer.valueOf(3);
            }
        }
        if (longestParseDistance == 0) {
            pos.setIndex(oldPos);
            pos.setErrorIndex(0);
            return null;
        } else {
            pos.setIndex(newPos);
            pos.setErrorIndex(-1);
            return new TimeUnitAmount(resultNumber, resultTimeUnit);
        }
    }

    private void setup() {
        if (locale == null) {
            if (format != null) {
                locale = format.getLocale(null);
            } else {
                locale = ULocale.getDefault(Category.FORMAT);
            }
            // Needed for getLocale(ULocale.VALID_LOCALE)
            setLocale(locale, locale);
        }
        if (format == null) {
            format = NumberFormat.getNumberInstance(locale);
        }
        pluralRules = PluralRules.forLocale(locale);
        timeUnitToCountToPatterns = new HashMap<TimeUnit, Map<String, Object[]>>();
        Set<String> pluralKeywords = pluralRules.getKeywords();
        setup("units/duration", timeUnitToCountToPatterns, FULL_NAME, pluralKeywords);
        setup("unitsShort/duration", timeUnitToCountToPatterns, ABBREVIATED_NAME, pluralKeywords);
        isReady = true;
    }

    private static final class TimeUnitFormatSetupSink extends UResource.Sink {
        Map<TimeUnit, Map<String, Object[]>> timeUnitToCountToPatterns;
        int style;
        Set<String> pluralKeywords;
        ULocale locale;
        boolean beenHere;

        TimeUnitFormatSetupSink(Map<TimeUnit, Map<String, Object[]>> timeUnitToCountToPatterns,
                int style, Set<String> pluralKeywords, ULocale locale) {
            this.timeUnitToCountToPatterns = timeUnitToCountToPatterns;
            this.style = style;
            this.pluralKeywords = pluralKeywords;
            this.locale = locale;
            this.beenHere = false;
        }

        @Override
        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            // Skip all put() calls except the first one -- discard all fallback data.
            if (beenHere) {
                return;
            } else {
                beenHere = true;
            }
            
            UResource.Table units = value.getTable();
            for (int i = 0; units.getKeyAndValue(i, key, value); ++i) {
                String timeUnitName = key.toString();
                TimeUnit timeUnit = null;

                if (timeUnitName.equals("year")) {
                    timeUnit = TimeUnit.YEAR;
                } else if (timeUnitName.equals("month")) {
                    timeUnit = TimeUnit.MONTH;
                } else if (timeUnitName.equals("day")) {
                    timeUnit = TimeUnit.DAY;
                } else if (timeUnitName.equals("hour")) {
                    timeUnit = TimeUnit.HOUR;
                } else if (timeUnitName.equals("minute")) {
                    timeUnit = TimeUnit.MINUTE;
                } else if (timeUnitName.equals("second")) {
                    timeUnit = TimeUnit.SECOND;
                } else if (timeUnitName.equals("week")) {
                    timeUnit = TimeUnit.WEEK;
                } else {
                    continue;
                }

                Map<String, Object[]> countToPatterns = timeUnitToCountToPatterns.get(timeUnit);
                if (countToPatterns == null) {
                    countToPatterns = new TreeMap<String, Object[]>();
                    timeUnitToCountToPatterns.put(timeUnit, countToPatterns);
                }

                UResource.Table countsToPatternTable = value.getTable();
                for (int j = 0; countsToPatternTable.getKeyAndValue(j, key, value); ++j) {
                    String pluralCount = key.toString();
                    if (!pluralKeywords.contains(pluralCount))
                        continue;
                    // save both full name and abbreviated name in one table
                    // is good space-wise, but it degrades performance,
                    // since it needs to check whether the needed space
                    // is already allocated or not.
                    Object[] pair = countToPatterns.get(pluralCount);
                    if (pair == null) {
                        pair = new Object[2];
                        countToPatterns.put(pluralCount, pair);
                    }
                    if (pair[style] == null) {
                        String pattern = value.getString();
                        final MessageFormat messageFormat = new MessageFormat(pattern, locale);
                        pair[style] = messageFormat;
                    }
                }
            }
        }
    }

    private void setup(String resourceKey, Map<TimeUnit, Map<String, Object[]>> timeUnitToCountToPatterns, int style,
            Set<String> pluralKeywords) {
        // fill timeUnitToCountToPatterns from resource file
        try {

            ICUResourceBundle resource = (ICUResourceBundle) UResourceBundle.getBundleInstance(
                    ICUData.ICU_UNIT_BASE_NAME, locale);

            TimeUnitFormatSetupSink sink = new TimeUnitFormatSetupSink(
                    timeUnitToCountToPatterns, style, pluralKeywords, locale);
            resource.getAllItemsWithFallback(resourceKey, sink);
        } catch (MissingResourceException e) {
        }
        // there should be patterns for each plural rule in each time unit.
        // For each time unit,
        // for each plural rule, following is unit pattern fall-back rule:
        // ( for example: "one" hour )
        // look for its unit pattern in its locale tree.
        // if pattern is not found in its own locale, such as de_DE,
        // look for the pattern in its parent, such as de,
        // keep looking till found or till root.
        // if the pattern is not found in root either,
        // fallback to plural count "other",
        // look for the pattern of "other" in the locale tree:
        // "de_DE" to "de" to "root".
        // If not found, fall back to value of
        // static variable DEFAULT_PATTERN_FOR_xxx, such as "{0} h".
        //
        // Following is consistency check to create pattern for each
        // plural rule in each time unit using above fall-back rule.
        //
        final TimeUnit[] timeUnits = TimeUnit.values();
        Set<String> keywords = pluralRules.getKeywords();
        for (int i = 0; i < timeUnits.length; ++i) {
            // for each time unit,
            // get all the patterns for each plural rule in this locale.
            final TimeUnit timeUnit = timeUnits[i];
            Map<String, Object[]> countToPatterns = timeUnitToCountToPatterns.get(timeUnit);
            if (countToPatterns == null) {
                countToPatterns = new TreeMap<String, Object[]>();
                timeUnitToCountToPatterns.put(timeUnit, countToPatterns);
            }
            for (String pluralCount : keywords) {
                if (countToPatterns.get(pluralCount) == null || countToPatterns.get(pluralCount)[style] == null) {
                    // look through parents
                    searchInTree(resourceKey, style, timeUnit, pluralCount, pluralCount, countToPatterns);
                }
            }
        }
    }

    // srcPluralCount is the original plural count on which the pattern is
    // searched for.
    // searchPluralCount is the fallback plural count.
    // For example, to search for pattern for ""one" hour",
    // "one" is the srcPluralCount,
    // if the pattern is not found even in root, fallback to
    // using patterns of plural count "other",
    // then, "other" is the searchPluralCount.
    private void searchInTree(String resourceKey, int styl, TimeUnit timeUnit, String srcPluralCount,
            String searchPluralCount, Map<String, Object[]> countToPatterns) {
        ULocale parentLocale = locale;
        String srcTimeUnitName = timeUnit.toString();
        while (parentLocale != null) {
            try {
                // look for pattern for srcPluralCount in locale tree
                ICUResourceBundle unitsRes = (ICUResourceBundle) UResourceBundle.getBundleInstance(
                        ICUData.ICU_UNIT_BASE_NAME, parentLocale);
                unitsRes = unitsRes.getWithFallback(resourceKey);
                ICUResourceBundle oneUnitRes = unitsRes.getWithFallback(srcTimeUnitName);
                String pattern = oneUnitRes.getStringWithFallback(searchPluralCount);
                final MessageFormat messageFormat = new MessageFormat(pattern, locale);
                Object[] pair = countToPatterns.get(srcPluralCount);
                if (pair == null) {
                    pair = new Object[2];
                    countToPatterns.put(srcPluralCount, pair);
                }
                pair[styl] = messageFormat;
                return;
            } catch (MissingResourceException e) {
            }
            parentLocale = parentLocale.getFallback();
        }
        // if no unitsShort resource was found even after fallback to root locale
        // then search the units resource fallback from the current level to root
        if (parentLocale == null && resourceKey.equals("unitsShort")) {
            searchInTree("units", styl, timeUnit, srcPluralCount, searchPluralCount, countToPatterns);
            if (countToPatterns.get(srcPluralCount) != null
                    && countToPatterns.get(srcPluralCount)[styl] != null) {
                return;
            }
        }
        // if not found the pattern for this plural count at all,
        // fall-back to plural count "other"
        if (searchPluralCount.equals("other")) {
            // set default fall back the same as the resource in root
            MessageFormat messageFormat = null;
            if (timeUnit == TimeUnit.SECOND) {
                messageFormat = new MessageFormat(DEFAULT_PATTERN_FOR_SECOND, locale);
            } else if (timeUnit == TimeUnit.MINUTE) {
                messageFormat = new MessageFormat(DEFAULT_PATTERN_FOR_MINUTE, locale);
            } else if (timeUnit == TimeUnit.HOUR) {
                messageFormat = new MessageFormat(DEFAULT_PATTERN_FOR_HOUR, locale);
            } else if (timeUnit == TimeUnit.WEEK) {
                messageFormat = new MessageFormat(DEFAULT_PATTERN_FOR_WEEK, locale);
            } else if (timeUnit == TimeUnit.DAY) {
                messageFormat = new MessageFormat(DEFAULT_PATTERN_FOR_DAY, locale);
            } else if (timeUnit == TimeUnit.MONTH) {
                messageFormat = new MessageFormat(DEFAULT_PATTERN_FOR_MONTH, locale);
            } else if (timeUnit == TimeUnit.YEAR) {
                messageFormat = new MessageFormat(DEFAULT_PATTERN_FOR_YEAR, locale);
            }
            Object[] pair = countToPatterns.get(srcPluralCount);
            if (pair == null) {
                pair = new Object[2];
                countToPatterns.put(srcPluralCount, pair);
            }
            pair[styl] = messageFormat;
        } else {
            // fall back to rule "other", and search in parents
            searchInTree(resourceKey, styl, timeUnit, srcPluralCount, "other", countToPatterns);
        }
    }

    // boilerplate code to make TimeUnitFormat otherwise follow the contract of
    // MeasureFormat


    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    @Override
    public StringBuilder formatMeasures(
            StringBuilder appendTo, FieldPosition fieldPosition, Measure... measures) {
        return mf.formatMeasures(appendTo, fieldPosition, measures);
    }

    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    @Override
    public MeasureFormat.FormatWidth getWidth() {
        return mf.getWidth();
    }

    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    @Override
    public NumberFormat getNumberFormat() {
        return mf.getNumberFormat();
    }

    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    @Override
    public Object clone() {
        TimeUnitFormat result = (TimeUnitFormat) super.clone();
        result.format = (NumberFormat) format.clone();
        return result;
    }
    // End boilerplate.

    // Serialization

    private Object writeReplace() throws ObjectStreamException {
        return mf.toTimeUnitProxy();
    }

    // Preserve backward serialize backward compatibility.
    private Object readResolve() throws ObjectStreamException {
        return new TimeUnitFormat(locale, style, format);
    }
}
