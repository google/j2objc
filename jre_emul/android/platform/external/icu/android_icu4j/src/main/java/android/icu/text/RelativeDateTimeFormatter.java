/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2013-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.text;

import java.util.EnumMap;
import java.util.Locale;

import android.icu.impl.CacheBase;
import android.icu.impl.DontCareFieldPosition;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SimpleFormatterImpl;
import android.icu.impl.SoftCache;
import android.icu.impl.StandardPlural;
import android.icu.impl.UResource;
import android.icu.lang.UCharacter;
import android.icu.util.Calendar;
import android.icu.util.ICUException;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;


/**
 * Formats simple relative dates. There are two types of relative dates that
 * it handles:
 * <ul>
 *   <li>relative dates with a quantity e.g "in 5 days"</li>
 *   <li>relative dates without a quantity e.g "next Tuesday"</li>
 * </ul>
 * <p>
 * This API is very basic and is intended to be a building block for more
 * fancy APIs. The caller tells it exactly what to display in a locale
 * independent way. While this class automatically provides the correct plural
 * forms, the grammatical form is otherwise as neutral as possible. It is the
 * caller's responsibility to handle cut-off logic such as deciding between
 * displaying "in 7 days" or "in 1 week." This API supports relative dates
 * involving one single unit. This API does not support relative dates
 * involving compound units.
 * e.g "in 5 days and 4 hours" nor does it support parsing.
 * This class is both immutable and thread-safe.
 * <p>
 * Here are some examples of use:
 * <blockquote>
 * <pre>
 * RelativeDateTimeFormatter fmt = RelativeDateTimeFormatter.getInstance();
 * fmt.format(1, Direction.NEXT, RelativeUnit.DAYS); // "in 1 day"
 * fmt.format(3, Direction.NEXT, RelativeUnit.DAYS); // "in 3 days"
 * fmt.format(3.2, Direction.LAST, RelativeUnit.YEARS); // "3.2 years ago"
 *
 * fmt.format(Direction.LAST, AbsoluteUnit.SUNDAY); // "last Sunday"
 * fmt.format(Direction.THIS, AbsoluteUnit.SUNDAY); // "this Sunday"
 * fmt.format(Direction.NEXT, AbsoluteUnit.SUNDAY); // "next Sunday"
 * fmt.format(Direction.PLAIN, AbsoluteUnit.SUNDAY); // "Sunday"
 *
 * fmt.format(Direction.LAST, AbsoluteUnit.DAY); // "yesterday"
 * fmt.format(Direction.THIS, AbsoluteUnit.DAY); // "today"
 * fmt.format(Direction.NEXT, AbsoluteUnit.DAY); // "tomorrow"
 *
 * fmt.format(Direction.PLAIN, AbsoluteUnit.NOW); // "now"
 * </pre>
 * </blockquote>
 * <p>
 * In the future, we may add more forms, such as abbreviated/short forms
 * (3 secs ago), and relative day periods ("yesterday afternoon"), etc.
 */
public final class RelativeDateTimeFormatter {

    /**
     * The formatting style
     *
     */
    public static enum Style {

        /**
         * Everything spelled out.
         */
        LONG,

        /**
         * Abbreviations used when possible.
         */
        SHORT,

        /**
         * Use single letters when possible.
         */
        NARROW;

        private static final int INDEX_COUNT = 3;  // NARROW.ordinal() + 1
    }

    /**
     * Represents the unit for formatting a relative date. e.g "in 5 days"
     * or "in 3 months"
     */
    public static enum RelativeUnit {

        /**
         * Seconds
         */
        SECONDS,

        /**
         * Minutes
         */
        MINUTES,

       /**
        * Hours
        */
        HOURS,

        /**
         * Days
         */
        DAYS,

        /**
         * Weeks
         */
        WEEKS,

        /**
         * Months
         */
        MONTHS,

        /**
         * Years
         */
        YEARS,

        /**
         * Quarters
         * @deprecated This API is ICU internal only.
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        QUARTERS,
    }

    /**
     * Represents an absolute unit.
     */
    public static enum AbsoluteUnit {

       /**
        * Sunday
        */
        SUNDAY,

        /**
         * Monday
         */
        MONDAY,

        /**
         * Tuesday
         */
        TUESDAY,

        /**
         * Wednesday
         */
        WEDNESDAY,

        /**
         * Thursday
         */
        THURSDAY,

        /**
         * Friday
         */
        FRIDAY,

        /**
         * Saturday
         */
        SATURDAY,

        /**
         * Day
         */
        DAY,

        /**
         * Week
         */
        WEEK,

        /**
         * Month
         */
        MONTH,

        /**
         * Year
         */
        YEAR,

        /**
         * Now
         */
        NOW,

        /**
         * Quarter
         * @deprecated This API is ICU internal only.
         * @hide draft / provisional / internal are hidden on Android
         */
        @Deprecated
        QUARTER,
    }

    /**
     * Represents a direction for an absolute unit e.g "Next Tuesday"
     * or "Last Tuesday"
     */
    public static enum Direction {
          /**
           * Two before. Not fully supported in every locale
           */
          LAST_2,

          /**
           * Last
           */
          LAST,

          /**
           * This
           */
          THIS,

          /**
           * Next
           */
          NEXT,

          /**
           * Two after. Not fully supported in every locale
           */
          NEXT_2,

          /**
           * Plain, which means the absence of a qualifier
           */
          PLAIN,
    }

    /**
     * Represents the unit for formatting a relative date. e.g "in 5 days"
     * or "next year"
     * @hide draft / provisional / internal are hidden on Android
     */
    public static enum RelativeDateTimeUnit {
        /**
         * Specifies that relative unit is year, e.g. "last year",
         * "in 5 years".
         * @hide draft / provisional / internal are hidden on Android
         */
        YEAR,
        /**
         * Specifies that relative unit is quarter, e.g. "last quarter",
         * "in 5 quarters".
         * @hide draft / provisional / internal are hidden on Android
         */
        QUARTER,
        /**
         * Specifies that relative unit is month, e.g. "last month",
         * "in 5 months".
         * @hide draft / provisional / internal are hidden on Android
         */
        MONTH,
        /**
         * Specifies that relative unit is week, e.g. "last week",
         * "in 5 weeks".
         * @hide draft / provisional / internal are hidden on Android
         */
        WEEK,
        /**
         * Specifies that relative unit is day, e.g. "yesterday",
         * "in 5 days".
         * @hide draft / provisional / internal are hidden on Android
         */
        DAY,
        /**
         * Specifies that relative unit is hour, e.g. "1 hour ago",
         * "in 5 hours".
         * @hide draft / provisional / internal are hidden on Android
         */
        HOUR,
        /**
         * Specifies that relative unit is minute, e.g. "1 minute ago",
         * "in 5 minutes".
         * @hide draft / provisional / internal are hidden on Android
         */
        MINUTE,
        /**
         * Specifies that relative unit is second, e.g. "1 second ago",
         * "in 5 seconds".
         * @hide draft / provisional / internal are hidden on Android
         */
        SECOND,
        /**
         * Specifies that relative unit is Sunday, e.g. "last Sunday",
         * "this Sunday", "next Sunday", "in 5 Sundays".
         * @hide draft / provisional / internal are hidden on Android
         */
        SUNDAY,
        /**
         * Specifies that relative unit is Monday, e.g. "last Monday",
         * "this Monday", "next Monday", "in 5 Mondays".
         * @hide draft / provisional / internal are hidden on Android
         */
        MONDAY,
        /**
         * Specifies that relative unit is Tuesday, e.g. "last Tuesday",
         * "this Tuesday", "next Tuesday", "in 5 Tuesdays".
         * @hide draft / provisional / internal are hidden on Android
         */
        TUESDAY,
        /**
         * Specifies that relative unit is Wednesday, e.g. "last Wednesday",
         * "this Wednesday", "next Wednesday", "in 5 Wednesdays".
         * @hide draft / provisional / internal are hidden on Android
         */
        WEDNESDAY,
        /**
         * Specifies that relative unit is Thursday, e.g. "last Thursday",
         * "this Thursday", "next Thursday", "in 5 Thursdays".
         * @hide draft / provisional / internal are hidden on Android
         */
        THURSDAY,
        /**
         * Specifies that relative unit is Friday, e.g. "last Friday",
         * "this Friday", "next Friday", "in 5 Fridays".
         * @hide draft / provisional / internal are hidden on Android
         */
        FRIDAY,
        /**
         * Specifies that relative unit is Saturday, e.g. "last Saturday",
         * "this Saturday", "next Saturday", "in 5 Saturdays".
         * @hide draft / provisional / internal are hidden on Android
         */
        SATURDAY,
    }

    /**
     * Returns a RelativeDateTimeFormatter for the default locale.
     */
    public static RelativeDateTimeFormatter getInstance() {
        return getInstance(ULocale.getDefault(), null, Style.LONG, DisplayContext.CAPITALIZATION_NONE);
    }

    /**
     * Returns a RelativeDateTimeFormatter for a particular locale.
     *
     * @param locale the locale.
     * @return An instance of RelativeDateTimeFormatter.
     */
    public static RelativeDateTimeFormatter getInstance(ULocale locale) {
        return getInstance(locale, null, Style.LONG, DisplayContext.CAPITALIZATION_NONE);
    }

    /**
     * Returns a RelativeDateTimeFormatter for a particular {@link java.util.Locale}.
     *
     * @param locale the {@link java.util.Locale}.
     * @return An instance of RelativeDateTimeFormatter.
     */
    public static RelativeDateTimeFormatter getInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale));
    }

    /**
     * Returns a RelativeDateTimeFormatter for a particular locale that uses a particular
     * NumberFormat object.
     *
     * @param locale the locale
     * @param nf the number format object. It is defensively copied to ensure thread-safety
     * and immutability of this class.
     * @return An instance of RelativeDateTimeFormatter.
     */
    public static RelativeDateTimeFormatter getInstance(ULocale locale, NumberFormat nf) {
        return getInstance(locale, nf, Style.LONG, DisplayContext.CAPITALIZATION_NONE);
    }

    /**
     * Returns a RelativeDateTimeFormatter for a particular locale that uses a particular
     * NumberFormat object, style, and capitalization context
     *
     * @param locale the locale
     * @param nf the number format object. It is defensively copied to ensure thread-safety
     * and immutability of this class. May be null.
     * @param style the style.
     * @param capitalizationContext the capitalization context.
     */
    public static RelativeDateTimeFormatter getInstance(
            ULocale locale,
            NumberFormat nf,
            Style style,
            DisplayContext capitalizationContext) {
        RelativeDateTimeFormatterData data = cache.get(locale);
        if (nf == null) {
            nf = NumberFormat.getInstance(locale);
        } else {
            nf = (NumberFormat) nf.clone();
        }
        return new RelativeDateTimeFormatter(
                data.qualitativeUnitMap,
                data.relUnitPatternMap,
                SimpleFormatterImpl.compileToStringMinMaxArguments(
                        data.dateTimePattern, new StringBuilder(), 2, 2),
                PluralRules.forLocale(locale),
                nf,
                style,
                capitalizationContext,
                capitalizationContext == DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE ?
                    BreakIterator.getSentenceInstance(locale) : null,
                locale);
    }

    /**
     * Returns a RelativeDateTimeFormatter for a particular {@link java.util.Locale} that uses a
     * particular NumberFormat object.
     *
     * @param locale the {@link java.util.Locale}
     * @param nf the number format object. It is defensively copied to ensure thread-safety
     * and immutability of this class.
     * @return An instance of RelativeDateTimeFormatter.
     */
    public static RelativeDateTimeFormatter getInstance(Locale locale, NumberFormat nf) {
        return getInstance(ULocale.forLocale(locale), nf);
    }

    /**
     * Formats a relative date with a quantity such as "in 5 days" or
     * "3 months ago"
     * @param quantity The numerical amount e.g 5. This value is formatted
     * according to this object's {@link NumberFormat} object.
     * @param direction NEXT means a future relative date; LAST means a past
     * relative date.
     * @param unit the unit e.g day? month? year?
     * @return the formatted string
     * @throws IllegalArgumentException if direction is something other than
     * NEXT or LAST.
     */
    public String format(double quantity, Direction direction, RelativeUnit unit) {
        if (direction != Direction.LAST && direction != Direction.NEXT) {
            throw new IllegalArgumentException("direction must be NEXT or LAST");
        }
        String result;
        int pastFutureIndex = (direction == Direction.NEXT ? 1 : 0);

        // This class is thread-safe, yet numberFormat is not. To ensure thread-safety of this
        // class we must guarantee that only one thread at a time uses our numberFormat.
        synchronized (numberFormat) {
            StringBuffer formatStr = new StringBuffer();
            DontCareFieldPosition fieldPosition = DontCareFieldPosition.INSTANCE;
            StandardPlural pluralForm = QuantityFormatter.selectPlural(quantity,
                    numberFormat, pluralRules, formatStr, fieldPosition);

            String formatter = getRelativeUnitPluralPattern(style, unit, pastFutureIndex, pluralForm);
            result = SimpleFormatterImpl.formatCompiledPattern(formatter, formatStr);
        }
        return adjustForContext(result);

    }

    /**
     * Format a combination of RelativeDateTimeUnit and numeric offset
     * using a numeric style, e.g. "1 week ago", "in 1 week",
     * "5 weeks ago", "in 5 weeks".
     *
     * @param offset    The signed offset for the specified unit. This
     *                  will be formatted according to this object's
     *                  NumberFormat object.
     * @param unit      The unit to use when formatting the relative
     *                  date, e.g. RelativeDateTimeUnit.WEEK,
     *                  RelativeDateTimeUnit.FRIDAY.
     * @return          The formatted string (may be empty in case of error)
     * @hide draft / provisional / internal are hidden on Android
     */
    public String formatNumeric(double offset, RelativeDateTimeUnit unit) {
        // TODO:
        // The full implementation of this depends on CLDR data that is not yet available,
        // see: http://unicode.org/cldr/trac/ticket/9165 Add more relative field data.
        // In the meantime do a quick bring-up by calling the old format method. When the
        // new CLDR data is available, update the data storage accordingly, rewrite this
        // to use it directly, and rewrite the old format method to call this new one;
        // that is covered by http://bugs.icu-project.org/trac/ticket/12171.
        RelativeUnit relunit = RelativeUnit.SECONDS;
        switch (unit) {
            case YEAR:      relunit = RelativeUnit.YEARS; break;
            case QUARTER:   relunit = RelativeUnit.QUARTERS; break;
            case MONTH:     relunit = RelativeUnit.MONTHS; break;
            case WEEK:      relunit = RelativeUnit.WEEKS; break;
            case DAY:       relunit = RelativeUnit.DAYS; break;
            case HOUR:      relunit = RelativeUnit.HOURS; break;
            case MINUTE:    relunit = RelativeUnit.MINUTES; break;
            case SECOND:    break; // set above
            default: // SUNDAY..SATURDAY
                throw new UnsupportedOperationException("formatNumeric does not currently support RelativeUnit.SUNDAY..SATURDAY");
        }
        Direction direction = Direction.NEXT;
        if (offset < 0) {
            direction = Direction.LAST;
            offset = -offset;
        }
        String result = format(offset, direction, relunit);
        return (result != null)? result: "";
    }

    private int[] styleToDateFormatSymbolsWidth = {
                DateFormatSymbols.WIDE, DateFormatSymbols.SHORT, DateFormatSymbols.NARROW
    };

    /**
     * Formats a relative date without a quantity.
     * @param direction NEXT, LAST, THIS, etc.
     * @param unit e.g SATURDAY, DAY, MONTH
     * @return the formatted string. If direction has a value that is documented as not being
     *  fully supported in every locale (for example NEXT_2 or LAST_2) then this function may
     *  return null to signal that no formatted string is available.
     * @throws IllegalArgumentException if the direction is incompatible with
     * unit this can occur with NOW which can only take PLAIN.
     */
    public String format(Direction direction, AbsoluteUnit unit) {
        if (unit == AbsoluteUnit.NOW && direction != Direction.PLAIN) {
            throw new IllegalArgumentException("NOW can only accept direction PLAIN.");
        }
        String result;
        // Get plain day of week names from DateFormatSymbols.
        if ((direction == Direction.PLAIN) &&  (AbsoluteUnit.SUNDAY.ordinal() <= unit.ordinal() &&
                unit.ordinal() <= AbsoluteUnit.SATURDAY.ordinal())) {
            // Convert from AbsoluteUnit days to Calendar class indexing.
            int dateSymbolsDayOrdinal = (unit.ordinal() - AbsoluteUnit.SUNDAY.ordinal()) + Calendar.SUNDAY;
            String[] dayNames =
                    dateFormatSymbols.getWeekdays(DateFormatSymbols.STANDALONE,
                    styleToDateFormatSymbolsWidth[style.ordinal()]);
            result = dayNames[dateSymbolsDayOrdinal];
        } else {
            // Not PLAIN, or not a weekday.
            result = getAbsoluteUnitString(style, unit, direction);
        }
        return result != null ? adjustForContext(result) : null;
    }

    /**
     * Format a combination of RelativeDateTimeUnit and numeric offset
     * using a text style if possible, e.g. "last week", "this week",
     * "next week", "yesterday", "tomorrow". Falls back to numeric
     * style if no appropriate text term is available for the specified
     * offset in the object’s locale.
     *
     * @param offset    The signed offset for the specified field.
     * @param unit      The unit to use when formatting the relative
     *                  date, e.g. RelativeDateTimeUnit.WEEK,
     *                  RelativeDateTimeUnit.FRIDAY.
     * @return          The formatted string (may be empty in case of error)
     * @hide draft / provisional / internal are hidden on Android
     */
    public String format(double offset, RelativeDateTimeUnit unit) {
        // TODO:
        // The full implementation of this depends on CLDR data that is not yet available,
        // see: http://unicode.org/cldr/trac/ticket/9165 Add more relative field data.
        // In the meantime do a quick bring-up by calling the old format method. When the
        // new CLDR data is available, update the data storage accordingly, rewrite this
        // to use it directly, and rewrite the old format method to call this new one;
        // that is covered by http://bugs.icu-project.org/trac/ticket/12171.
        boolean useNumeric = true;
        Direction direction = Direction.THIS;
        if (offset > -2.1 && offset < 2.1) {
            // Allow a 1% epsilon, so offsets in -1.01..-0.99 map to LAST
            double offsetx100 = offset * 100.0;
            int intoffsetx100 = (offsetx100 < 0)? (int)(offsetx100-0.5) : (int)(offsetx100+0.5);
            switch (intoffsetx100) {
                case -200/*-2*/: direction = Direction.LAST_2; useNumeric = false; break;
                case -100/*-1*/: direction = Direction.LAST;   useNumeric = false; break;
                case    0/* 0*/: useNumeric = false; break; // direction = Direction.THIS was set above
                case  100/* 1*/: direction = Direction.NEXT;   useNumeric = false; break;
                case  200/* 2*/: direction = Direction.NEXT_2; useNumeric = false; break;
                default: break;
            }
        }
        AbsoluteUnit absunit = AbsoluteUnit.NOW;
        switch (unit) {
            case YEAR:      absunit = AbsoluteUnit.YEAR;    break;
            case QUARTER:   absunit = AbsoluteUnit.QUARTER; break;
            case MONTH:     absunit = AbsoluteUnit.MONTH;   break;
            case WEEK:      absunit = AbsoluteUnit.WEEK;    break;
            case DAY:       absunit = AbsoluteUnit.DAY;     break;
            case SUNDAY:    absunit = AbsoluteUnit.SUNDAY;  break;
            case MONDAY:    absunit = AbsoluteUnit.MONDAY;  break;
            case TUESDAY:   absunit = AbsoluteUnit.TUESDAY; break;
            case WEDNESDAY: absunit = AbsoluteUnit.WEDNESDAY; break;
            case THURSDAY:  absunit = AbsoluteUnit.THURSDAY; break;
            case FRIDAY:    absunit = AbsoluteUnit.FRIDAY;  break;
            case SATURDAY:  absunit = AbsoluteUnit.SATURDAY; break;
            case SECOND:
                if (direction == Direction.THIS) {
                    // absunit = AbsoluteUnit.NOW was set above
                    direction = Direction.PLAIN;
                    break;
                }
                // could just fall through here but that produces warnings
                useNumeric = true;
                break;
            case HOUR:
            default:
                useNumeric = true;
                break;
        }
        if (!useNumeric) {
            String result = format(direction, absunit);
            if (result != null && result.length() > 0) {
                return result;
            }
        }
        // otherwise fallback to formatNumeric
        return formatNumeric(offset, unit);
    }

    /**
     * Gets the string value from qualitativeUnitMap with fallback based on style.
     */
    private String getAbsoluteUnitString(Style style, AbsoluteUnit unit, Direction direction) {
        EnumMap<AbsoluteUnit, EnumMap<Direction, String>> unitMap;
        EnumMap<Direction, String> dirMap;

        do {
            unitMap = qualitativeUnitMap.get(style);
            if (unitMap != null) {
                dirMap = unitMap.get(unit);
                if (dirMap != null) {
                    String result = dirMap.get(direction);
                    if (result != null) {
                        return result;
                    }
                }

            }

            // Consider other styles from alias fallback.
            // Data loading guaranteed no endless loops.
        } while ((style = fallbackCache[style.ordinal()]) != null);
        return null;
    }

    /**
     * Combines a relative date string and a time string in this object's
     * locale. This is done with the same date-time separator used for the
     * default calendar in this locale.
     * @param relativeDateString the relative date e.g 'yesterday'
     * @param timeString the time e.g '3:45'
     * @return the date and time concatenated according to the default
     * calendar in this locale e.g 'yesterday, 3:45'
     */
    public String combineDateAndTime(String relativeDateString, String timeString) {
        return SimpleFormatterImpl.formatCompiledPattern(
                combinedDateAndTime, timeString, relativeDateString);
    }

    /**
     * Returns a copy of the NumberFormat this object is using.
     * @return A copy of the NumberFormat.
     */
    public NumberFormat getNumberFormat() {
        // This class is thread-safe, yet numberFormat is not. To ensure thread-safety of this
        // class we must guarantee that only one thread at a time uses our numberFormat.
        synchronized (numberFormat) {
            return (NumberFormat) numberFormat.clone();
        }
    }

    /**
     * Return capitalization context.
     * @return The capitalization context.
     */
    public DisplayContext getCapitalizationContext() {
        return capitalizationContext;
    }

    /**
     * Return style
     * @return The formatting style.
     */
    public Style getFormatStyle() {
        return style;
    }

    private String adjustForContext(String originalFormattedString) {
        if (breakIterator == null || originalFormattedString.length() == 0
                || !UCharacter.isLowerCase(UCharacter.codePointAt(originalFormattedString, 0))) {
            return originalFormattedString;
        }
        synchronized (breakIterator) {
            return UCharacter.toTitleCase(
                    locale,
                    originalFormattedString,
                    breakIterator,
                    UCharacter.TITLECASE_NO_LOWERCASE | UCharacter.TITLECASE_NO_BREAK_ADJUSTMENT);
        }
    }

    private RelativeDateTimeFormatter(
            EnumMap<Style, EnumMap<AbsoluteUnit, EnumMap<Direction, String>>> qualitativeUnitMap,
            EnumMap<Style, EnumMap<RelativeUnit, String[][]>> patternMap,
            String combinedDateAndTime,
            PluralRules pluralRules,
            NumberFormat numberFormat,
            Style style,
            DisplayContext capitalizationContext,
            BreakIterator breakIterator,
            ULocale locale) {
        this.qualitativeUnitMap = qualitativeUnitMap;
        this.patternMap = patternMap;
        this.combinedDateAndTime = combinedDateAndTime;
        this.pluralRules = pluralRules;
        this.numberFormat = numberFormat;
        this.style = style;
        if (capitalizationContext.type() != DisplayContext.Type.CAPITALIZATION) {
            throw new IllegalArgumentException(capitalizationContext.toString());
        }
        this.capitalizationContext = capitalizationContext;
        this.breakIterator = breakIterator;
        this.locale = locale;
        this.dateFormatSymbols = new DateFormatSymbols(locale);
    }

    private String getRelativeUnitPluralPattern(
            Style style, RelativeUnit unit, int pastFutureIndex, StandardPlural pluralForm) {
        if (pluralForm != StandardPlural.OTHER) {
            String formatter = getRelativeUnitPattern(style, unit, pastFutureIndex, pluralForm);
            if (formatter != null) {
                return formatter;
            }
        }
        return getRelativeUnitPattern(style, unit, pastFutureIndex, StandardPlural.OTHER);
    }

    private String getRelativeUnitPattern(
            Style style, RelativeUnit unit, int pastFutureIndex, StandardPlural pluralForm) {
        int pluralIndex = pluralForm.ordinal();
        do {
            EnumMap<RelativeUnit, String[][]> unitMap = patternMap.get(style);
            if (unitMap != null) {
                String[][] spfCompiledPatterns = unitMap.get(unit);
                if (spfCompiledPatterns != null) {
                    if (spfCompiledPatterns[pastFutureIndex][pluralIndex] != null) {
                        return spfCompiledPatterns[pastFutureIndex][pluralIndex];
                    }
                }

            }

            // Consider other styles from alias fallback.
            // Data loading guaranteed no endless loops.
        } while ((style = fallbackCache[style.ordinal()]) != null);
        return null;
    }

    private final EnumMap<Style, EnumMap<AbsoluteUnit, EnumMap<Direction, String>>> qualitativeUnitMap;
    private final EnumMap<Style, EnumMap<RelativeUnit, String[][]>> patternMap;

    private final String combinedDateAndTime;  // compiled SimpleFormatter pattern
    private final PluralRules pluralRules;
    private final NumberFormat numberFormat;

    private final Style style;
    private final DisplayContext capitalizationContext;
    private final BreakIterator breakIterator;
    private final ULocale locale;

    private final DateFormatSymbols dateFormatSymbols;

    private static final Style fallbackCache[] = new Style[Style.INDEX_COUNT];

    private static class RelativeDateTimeFormatterData {
        public RelativeDateTimeFormatterData(
                EnumMap<Style, EnumMap<AbsoluteUnit, EnumMap<Direction, String>>> qualitativeUnitMap,
                EnumMap<Style, EnumMap<RelativeUnit, String[][]>> relUnitPatternMap,
                String dateTimePattern) {
            this.qualitativeUnitMap = qualitativeUnitMap;
            this.relUnitPatternMap = relUnitPatternMap;

            this.dateTimePattern = dateTimePattern;
        }

        public final EnumMap<Style, EnumMap<AbsoluteUnit, EnumMap<Direction, String>>> qualitativeUnitMap;
        EnumMap<Style, EnumMap<RelativeUnit, String[][]>> relUnitPatternMap;
        public final String dateTimePattern;  // Example: "{1}, {0}"
    }

    private static class Cache {
        private final CacheBase<String, RelativeDateTimeFormatterData, ULocale> cache =
            new SoftCache<String, RelativeDateTimeFormatterData, ULocale>() {
                @Override
                protected RelativeDateTimeFormatterData createInstance(String key, ULocale locale) {
                    return new Loader(locale).load();
                }
            };

        public RelativeDateTimeFormatterData get(ULocale locale) {
            String key = locale.toString();
            return cache.getInstance(key, locale);
        }
    }

    private static Direction keyToDirection(UResource.Key key) {
        if (key.contentEquals("-2")) {
            return Direction.LAST_2;
        }
        if (key.contentEquals("-1")) {
            return Direction.LAST;
        }
        if (key.contentEquals("0")) {
            return Direction.THIS;
        }
        if (key.contentEquals("1")) {
            return Direction.NEXT;
        }
        if (key.contentEquals("2")) {
            return Direction.NEXT_2;
        }
        return null;
    }

    /**
     * Sink for enumerating all of the relative data time formatter names.
     *
     * More specific bundles (en_GB) are enumerated before their parents (en_001, en, root):
     * Only store a value if it is still missing, that is, it has not been overridden.
     */
    private static final class RelDateTimeDataSink extends UResource.Sink {

        // For white list of units to handle in RelativeDateTimeFormatter.
        private enum DateTimeUnit {
            SECOND(RelativeUnit.SECONDS, null),
            MINUTE(RelativeUnit.MINUTES, null),
            HOUR(RelativeUnit.HOURS, null),
            DAY(RelativeUnit.DAYS, AbsoluteUnit.DAY),
            WEEK(RelativeUnit.WEEKS, AbsoluteUnit.WEEK),
            MONTH(RelativeUnit.MONTHS, AbsoluteUnit.MONTH),
            QUARTER(RelativeUnit.QUARTERS, AbsoluteUnit.QUARTER),
            YEAR(RelativeUnit.YEARS, AbsoluteUnit.YEAR),
            SUNDAY(null, AbsoluteUnit.SUNDAY),
            MONDAY(null, AbsoluteUnit.MONDAY),
            TUESDAY(null, AbsoluteUnit.TUESDAY),
            WEDNESDAY(null, AbsoluteUnit.WEDNESDAY),
            THURSDAY(null, AbsoluteUnit.THURSDAY),
            FRIDAY(null, AbsoluteUnit.FRIDAY),
            SATURDAY(null, AbsoluteUnit.SATURDAY);

            RelativeUnit relUnit;
            AbsoluteUnit absUnit;

            DateTimeUnit(RelativeUnit relUnit, AbsoluteUnit absUnit) {
                this.relUnit = relUnit;
                this.absUnit = absUnit;
            }

            private static final DateTimeUnit orNullFromString(CharSequence keyword) {
                // Quick check from string to enum.
                switch (keyword.length()) {
                case 3:
                    if ("day".contentEquals(keyword)) {
                        return DAY;
                    } else if ("sun".contentEquals(keyword)) {
                        return SUNDAY;
                    } else if ("mon".contentEquals(keyword)) {
                        return MONDAY;
                    } else if ("tue".contentEquals(keyword)) {
                        return TUESDAY;
                    } else if ("wed".contentEquals(keyword)) {
                        return WEDNESDAY;
                    } else if ("thu".contentEquals(keyword)) {
                        return THURSDAY;
                    }    else if ("fri".contentEquals(keyword)) {
                        return FRIDAY;
                    } else if ("sat".contentEquals(keyword)) {
                        return SATURDAY;
                    }
                    break;
                case 4:
                    if ("hour".contentEquals(keyword)) {
                        return HOUR;
                    } else if ("week".contentEquals(keyword)) {
                        return WEEK;
                    } else if ("year".contentEquals(keyword)) {
                        return YEAR;
                    }
                    break;
                case 5:
                    if ("month".contentEquals(keyword)) {
                        return MONTH;
                    }
                    break;
                case 6:
                    if ("minute".contentEquals(keyword)) {
                        return MINUTE;
                    }else if ("second".contentEquals(keyword)) {
                        return SECOND;
                    }
                    break;
                case 7:
                    if ("quarter".contentEquals(keyword)) {
                        return QUARTER;  // TODO: Check @provisional
                    }
                    break;
                default:
                    break;
                }
                return null;
            }
        }

        EnumMap<Style, EnumMap<AbsoluteUnit, EnumMap<Direction, String>>> qualitativeUnitMap =
                new EnumMap<Style, EnumMap<AbsoluteUnit, EnumMap<Direction, String>>>(Style.class);
        EnumMap<Style, EnumMap<RelativeUnit, String[][]>> styleRelUnitPatterns =
                new EnumMap<Style, EnumMap<RelativeUnit, String[][]>>(Style.class);

        StringBuilder sb = new StringBuilder();

        // Values keep between levels of parsing the CLDR data.
        int pastFutureIndex;
        Style style;                        // {LONG, SHORT, NARROW} Derived from unit key string.
        DateTimeUnit unit;                  // From the unit key string, with the style (e.g., "-short") separated out.

        private Style styleFromKey(UResource.Key key) {
            if (key.endsWith("-short")) {
                return Style.SHORT;
            } else if (key.endsWith("-narrow")) {
                return Style.NARROW;
            } else {
                return Style.LONG;
            }
        }

        private Style styleFromAlias(UResource.Value value) {
                String s = value.getAliasString();
                if (s.endsWith("-short")) {
                    return Style.SHORT;
                } else if (s.endsWith("-narrow")) {
                    return Style.NARROW;
                } else {
                    return Style.LONG;
                }
        }

        private static int styleSuffixLength(Style style) {
            switch (style) {
            case SHORT: return 6;
            case NARROW: return 7;
            default: return 0;
            }
        }

        public void consumeTableRelative(UResource.Key key, UResource.Value value) {
            UResource.Table unitTypesTable = value.getTable();
            for (int i = 0; unitTypesTable.getKeyAndValue(i, key, value); i++) {
                if (value.getType() == ICUResourceBundle.STRING) {
                    String valueString = value.getString();

                    EnumMap<AbsoluteUnit, EnumMap<Direction, String>> absMap = qualitativeUnitMap.get(style);

                    if (unit.relUnit == RelativeUnit.SECONDS) {
                        if (key.contentEquals("0")) {
                            // Handle Zero seconds for "now".
                            EnumMap<Direction, String> unitStrings = absMap.get(AbsoluteUnit.NOW);
                            if (unitStrings == null) {
                                unitStrings = new EnumMap<Direction, String>(Direction.class);
                                absMap.put(AbsoluteUnit.NOW, unitStrings);
                            }
                            if (unitStrings.get(Direction.PLAIN) == null) {
                                unitStrings.put(Direction.PLAIN, valueString);
                            }
                            continue;
                        }
                    }
                    Direction keyDirection = keyToDirection(key);
                    if (keyDirection == null) {
                        continue;
                    }
                    AbsoluteUnit absUnit = unit.absUnit;
                    if (absUnit == null) {
                        continue;
                    }

                    if (absMap == null) {
                        absMap = new EnumMap<AbsoluteUnit, EnumMap<Direction, String>>(AbsoluteUnit.class);
                        qualitativeUnitMap.put(style, absMap);
                    }
                    EnumMap<Direction, String> dirMap = absMap.get(absUnit);
                    if (dirMap == null) {
                        dirMap = new EnumMap<Direction, String>(Direction.class);
                        absMap.put(absUnit, dirMap);
                    }
                    if (dirMap.get(keyDirection) == null) {
                        // Do not override values already entered.
                        dirMap.put(keyDirection, value.getString());
                    }
                }
            }
        }

        // Record past or future and
        public void consumeTableRelativeTime(UResource.Key key, UResource.Value value) {
            if (unit.relUnit == null) {
                return;
            }
            UResource.Table unitTypesTable = value.getTable();
            for (int i = 0; unitTypesTable.getKeyAndValue(i, key, value); i++) {
                if (key.contentEquals("past")) {
                    pastFutureIndex = 0;
                } else if (key.contentEquals("future")) {
                    pastFutureIndex = 1;
                } else {
                    continue;
                }
                // Get the details of the relative time.
                consumeTimeDetail(key, value);
            }
        }

        public void consumeTimeDetail(UResource.Key key, UResource.Value value) {
            UResource.Table unitTypesTable = value.getTable();

            EnumMap<RelativeUnit, String[][]> unitPatterns  = styleRelUnitPatterns.get(style);
            if (unitPatterns == null) {
                unitPatterns = new EnumMap<RelativeUnit, String[][]>(RelativeUnit.class);
                styleRelUnitPatterns.put(style, unitPatterns);
            }
            String[][] patterns = unitPatterns.get(unit.relUnit);
            if (patterns == null) {
                patterns = new String[2][StandardPlural.COUNT];
                unitPatterns.put(unit.relUnit, patterns);
            }

            // Stuff the pattern for the correct plural index with a simple formatter.
            for (int i = 0; unitTypesTable.getKeyAndValue(i, key, value); i++) {
                if (value.getType() == ICUResourceBundle.STRING) {
                    int pluralIndex = StandardPlural.indexFromString(key.toString());
                    if (patterns[pastFutureIndex][pluralIndex] == null) {
                        patterns[pastFutureIndex][pluralIndex] =
                                SimpleFormatterImpl.compileToStringMinMaxArguments(
                                        value.getString(), sb, 0, 1);
                    }
                }
            }
        }

        private void handlePlainDirection(UResource.Key key, UResource.Value value) {
            AbsoluteUnit absUnit = unit.absUnit;
            if (absUnit == null) {
                return;  // Not interesting.
            }
            EnumMap<AbsoluteUnit, EnumMap<Direction, String>> unitMap =
                    qualitativeUnitMap.get(style);
            if (unitMap == null) {
                unitMap = new EnumMap<AbsoluteUnit, EnumMap<Direction, String>>(AbsoluteUnit.class);
                qualitativeUnitMap.put(style, unitMap);
            }
            EnumMap<Direction,String> dirMap = unitMap.get(absUnit);
            if (dirMap == null) {
                dirMap = new EnumMap<Direction,String>(Direction.class);
                unitMap.put(absUnit, dirMap);
            }
            if (dirMap.get(Direction.PLAIN) == null) {
                dirMap.put(Direction.PLAIN, value.toString());
            }
        }

        // Handle at the Unit level,
        public void consumeTimeUnit(UResource.Key key, UResource.Value value) {
            UResource.Table unitTypesTable = value.getTable();
            for (int i = 0; unitTypesTable.getKeyAndValue(i, key, value); i++) {
                if (key.contentEquals("dn") && value.getType() == ICUResourceBundle.STRING) {
                    handlePlainDirection(key, value);
                }
                if (value.getType() == ICUResourceBundle.TABLE) {
                    if (key.contentEquals("relative")) {
                        consumeTableRelative(key, value);
                    } else if (key.contentEquals("relativeTime")) {
                        consumeTableRelativeTime(key, value);
                    }
                }
            }
        }

        private void handleAlias(UResource.Key key, UResource.Value value, boolean noFallback) {
            Style sourceStyle = styleFromKey(key);
            int limit = key.length() - styleSuffixLength(sourceStyle);
            DateTimeUnit unit = DateTimeUnit.orNullFromString(key.substring(0, limit));
            if (unit != null) {
                // Record the fallback chain for the values.
                // At formatting time, limit to 2 levels of fallback.
                Style targetStyle = styleFromAlias(value);
                if (sourceStyle == targetStyle) {
                    throw new ICUException("Invalid style fallback from " + sourceStyle + " to itself");
                }

                // Check for inconsistent fallbacks.
                if (fallbackCache[sourceStyle.ordinal()] == null) {
                    fallbackCache[sourceStyle.ordinal()] = targetStyle;
                } else if (fallbackCache[sourceStyle.ordinal()] != targetStyle) {
                    throw new ICUException(
                            "Inconsistent style fallback for style " + sourceStyle + " to " + targetStyle);
                }
                return;
            }
        }

        @Override
        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            // Main entry point to sink
            if (value.getType() == ICUResourceBundle.ALIAS) {
                return;
            }

            UResource.Table table = value.getTable();
            // Process each key / value in this table.
            for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                if (value.getType() == ICUResourceBundle.ALIAS) {
                    handleAlias(key, value, noFallback);
                } else {
                    // Remember style and unit for deeper levels.
                    style = styleFromKey(key);
                    int limit = key.length() - styleSuffixLength(style);
                    unit = DateTimeUnit.orNullFromString(key.substring(0, limit));
                    if (unit != null) {
                        // Process only if unitString is in the white list.
                        consumeTimeUnit(key, value);
                    }
                }
            }
        }

        RelDateTimeDataSink() {
        }
    }

    private static class Loader {
        private final ULocale ulocale;

        public Loader(ULocale ulocale) {
            this.ulocale = ulocale;
        }

        private String getDateTimePattern(ICUResourceBundle r) {
            String calType = r.getStringWithFallback("calendar/default");
            if (calType == null || calType.equals("")) {
                calType = "gregorian";
            }
            String resourcePath = "calendar/" + calType + "/DateTimePatterns";
            ICUResourceBundle patternsRb = r.findWithFallback(resourcePath);
            if (patternsRb == null && calType.equals("gregorian")) {
                // Try with gregorian.
                patternsRb = r.findWithFallback("calendar/gregorian/DateTimePatterns");
            }
            if (patternsRb == null || patternsRb.getSize() < 9) {
                // Undefined or too few elements.
                return "{1} {0}";
            } else {
                int elementType = patternsRb.get(8).getType();
                if (elementType == UResourceBundle.ARRAY) {
                    return patternsRb.get(8).getString(0);
                } else {
                    return patternsRb.getString(8);
                }
            }
        }

        public RelativeDateTimeFormatterData load() {
            // Sink for traversing data.
            RelDateTimeDataSink sink = new RelDateTimeDataSink();

            ICUResourceBundle r = (ICUResourceBundle)UResourceBundle.
                    getBundleInstance(ICUData.ICU_BASE_NAME, ulocale);
            r.getAllItemsWithFallback("fields", sink);

            // Check fallbacks array for loops or too many levels.
            for (Style testStyle : Style.values()) {
                Style newStyle1 = fallbackCache[testStyle.ordinal()];
                // Data loading guaranteed newStyle1 != testStyle.
                if (newStyle1 != null) {
                    Style newStyle2 = fallbackCache[newStyle1.ordinal()];
                    if (newStyle2 != null) {
                        // No fallback should take more than 2 steps.
                        if (fallbackCache[newStyle2.ordinal()] != null) {
                            throw new IllegalStateException("Style fallback too deep");
                        }
                    }
                }
            }

            return new RelativeDateTimeFormatterData(
                    sink.qualitativeUnitMap, sink.styleRelUnitPatterns,
                    getDateTimePattern(r));
        }
    }

    private static final Cache cache = new Cache();
}
