/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2007-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.impl;

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.MissingResourceException;

import android.icu.lang.UCharacter;
import android.icu.text.BreakIterator;
import android.icu.text.DateFormat;
import android.icu.text.DisplayContext;
import android.icu.text.MessageFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;

/**
 * @author srl
 * @hide Only a subset of ICU is exposed in Android
 */
public class RelativeDateFormat extends DateFormat {

    /**
     * @author srl
     *
     */
    public static class URelativeString {
        URelativeString(int offset, String string) {
            this.offset = offset;
            this.string = string;
        }
        URelativeString(String offset, String string) {
            this.offset = Integer.parseInt(offset);
            this.string = string;
        }
        public int    offset;
        public String string;
    }

    // copy c'tor?

    /**
     * @param timeStyle The time style for the date and time.
     * @param dateStyle The date style for the date and time.
     * @param locale The locale for the date.
     * @param cal The calendar to be used
     */
    public RelativeDateFormat(int timeStyle, int dateStyle, ULocale locale, Calendar cal) {
        calendar = cal;

        fLocale = locale;
        fTimeStyle = timeStyle;
        fDateStyle = dateStyle;

        if (fDateStyle != DateFormat.NONE) {
            int newStyle = fDateStyle & ~DateFormat.RELATIVE;
            DateFormat df = DateFormat.getDateInstance(newStyle, locale);
            if (df instanceof SimpleDateFormat) {
                fDateTimeFormat = (SimpleDateFormat)df;
            } else {
                throw new IllegalArgumentException("Can't create SimpleDateFormat for date style");
            }
            fDatePattern = fDateTimeFormat.toPattern();
            if (fTimeStyle != DateFormat.NONE) {
                newStyle = fTimeStyle & ~DateFormat.RELATIVE;
                df = DateFormat.getTimeInstance(newStyle, locale);
                if (df instanceof SimpleDateFormat) {
                    fTimePattern = ((SimpleDateFormat)df).toPattern();
                }
            }
        } else {
            // does not matter whether timeStyle is UDAT_NONE, we need something for fDateTimeFormat
            int newStyle = fTimeStyle & ~DateFormat.RELATIVE;
            DateFormat df = DateFormat.getTimeInstance(newStyle, locale);
            if (df instanceof SimpleDateFormat) {
                fDateTimeFormat = (SimpleDateFormat)df;
            } else {
                throw new IllegalArgumentException("Can't create SimpleDateFormat for time style");
            }
            fTimePattern = fDateTimeFormat.toPattern();
        }

        initializeCalendar(null, fLocale);
        loadDates();
        initializeCombinedFormat(calendar, fLocale);
    }

    /**
     * serial version (generated)
     */
    private static final long serialVersionUID = 1131984966440549435L;

    /* (non-Javadoc)
     * @see android.icu.text.DateFormat#format(android.icu.util.Calendar, java.lang.StringBuffer, java.text.FieldPosition)
     */
    @Override
    public StringBuffer format(Calendar cal, StringBuffer toAppendTo,
            FieldPosition fieldPosition) {

        String relativeDayString = null;
        DisplayContext capitalizationContext = getContext(DisplayContext.Type.CAPITALIZATION);

        if (fDateStyle != DateFormat.NONE) {
            // calculate the difference, in days, between 'cal' and now.
            int dayDiff = dayDifference(cal);

            // look up string
            relativeDayString = getStringForDay(dayDiff);
        }

        if (fDateTimeFormat != null) {
            if (relativeDayString != null && fDatePattern != null &&
                    (fTimePattern == null || fCombinedFormat == null || combinedFormatHasDateAtStart) ) {
                // capitalize relativeDayString according to context for relative, set formatter no context
                if ( relativeDayString.length() > 0 && UCharacter.isLowerCase(relativeDayString.codePointAt(0)) &&
                     (capitalizationContext == DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE ||
                        (capitalizationContext == DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU && capitalizationOfRelativeUnitsForListOrMenu) ||
                        (capitalizationContext == DisplayContext.CAPITALIZATION_FOR_STANDALONE && capitalizationOfRelativeUnitsForStandAlone) )) {
                    if (capitalizationBrkIter == null) {
                        // should only happen when deserializing, etc.
                        capitalizationBrkIter = BreakIterator.getSentenceInstance(fLocale);
                    }
                    relativeDayString = UCharacter.toTitleCase(fLocale, relativeDayString, capitalizationBrkIter,
                                    UCharacter.TITLECASE_NO_LOWERCASE | UCharacter.TITLECASE_NO_BREAK_ADJUSTMENT);
                }
                fDateTimeFormat.setContext(DisplayContext.CAPITALIZATION_NONE);
            } else {
                // set our context for the formatter
                fDateTimeFormat.setContext(capitalizationContext);
            }
        }

        if (fDateTimeFormat != null && (fDatePattern != null || fTimePattern != null)) {
            // The new way
            if (fDatePattern == null) {
                // must have fTimePattern
                fDateTimeFormat.applyPattern(fTimePattern);
                fDateTimeFormat.format(cal, toAppendTo, fieldPosition);
            } else if (fTimePattern == null) {
                // must have fDatePattern
                if (relativeDayString != null) {
                    toAppendTo.append(relativeDayString);
                } else {
                    fDateTimeFormat.applyPattern(fDatePattern);
                    fDateTimeFormat.format(cal, toAppendTo, fieldPosition);
                }
            } else {
                String datePattern = fDatePattern; // default;
                if (relativeDayString != null) {
                    // Need to quote the relativeDayString to make it a legal date pattern
                    datePattern = "'" + relativeDayString.replace("'", "''") + "'";
                }
                StringBuffer combinedPattern = new StringBuffer("");
                fCombinedFormat.format(new Object[] {fTimePattern, datePattern}, combinedPattern, new FieldPosition(0));
                fDateTimeFormat.applyPattern(combinedPattern.toString());
                fDateTimeFormat.format(cal, toAppendTo, fieldPosition);
            }
        } else if (fDateFormat != null) {
            // A subset of the old way, for serialization compatibility
            // (just do the date part)
            if (relativeDayString != null) {
                toAppendTo.append(relativeDayString);
            } else {
                fDateFormat.format(cal, toAppendTo, fieldPosition);
            }
        }

        return toAppendTo;
    }

    /* (non-Javadoc)
     * @see android.icu.text.DateFormat#parse(java.lang.String, android.icu.util.Calendar, java.text.ParsePosition)
     */
    @Override
    public void parse(String text, Calendar cal, ParsePosition pos) {
        throw new UnsupportedOperationException("Relative Date parse is not implemented yet");
    }

    /* (non-Javadoc)
     * @see android.icu.text.DateFormat#setContext(android.icu.text.DisplayContext)
     * Here we override the DateFormat implementation in order to
     * lazily initialize relevant items
     */
    @Override
    public void setContext(DisplayContext context) {
        super.setContext(context);
        if (!capitalizationInfoIsSet &&
              (context==DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU || context==DisplayContext.CAPITALIZATION_FOR_STANDALONE)) {
            initCapitalizationContextInfo(fLocale);
            capitalizationInfoIsSet = true;
        }
        if (capitalizationBrkIter == null && (context==DisplayContext.CAPITALIZATION_FOR_BEGINNING_OF_SENTENCE ||
              (context==DisplayContext.CAPITALIZATION_FOR_UI_LIST_OR_MENU && capitalizationOfRelativeUnitsForListOrMenu) ||
              (context==DisplayContext.CAPITALIZATION_FOR_STANDALONE && capitalizationOfRelativeUnitsForStandAlone) )) {
            capitalizationBrkIter = BreakIterator.getSentenceInstance(fLocale);
        }
    }

    private DateFormat fDateFormat; // keep for serialization compatibility
    @SuppressWarnings("unused")
    private DateFormat fTimeFormat; // now unused, keep for serialization compatibility
    private MessageFormat fCombinedFormat; //  the {0} {1} format.
    private SimpleDateFormat fDateTimeFormat = null; // the held date/time formatter
    private String fDatePattern = null;
    private String fTimePattern = null;

    int fDateStyle;
    int fTimeStyle;
    ULocale  fLocale;

    private transient List<URelativeString> fDates = null;

    private boolean combinedFormatHasDateAtStart = false;
    private boolean capitalizationInfoIsSet = false;
    private boolean capitalizationOfRelativeUnitsForListOrMenu = false;
    private boolean capitalizationOfRelativeUnitsForStandAlone = false;
    private transient BreakIterator capitalizationBrkIter = null;

    /**
     * Get the string at a specific offset.
     * @param day day offset ( -1, 0, 1, etc.. ). Does not require sorting by offset.
     * @return the string, or NULL if none at that location.
     */
    private String getStringForDay(int day) {
        if(fDates == null) {
            loadDates();
        }
        for(URelativeString dayItem : fDates) {
            if(dayItem.offset == day) {
                return dayItem.string;
            }
        }
        return null;
    }

    // Sink to get "fields/day/relative".
    private final class RelDateFmtDataSink extends UResource.Sink {

        @Override
        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            if (value.getType() == ICUResourceBundle.ALIAS) {
                return;
            }

            UResource.Table table = value.getTable();
            for (int i = 0; table.getKeyAndValue(i, key, value); ++i) {

                int keyOffset;
                try {
                    keyOffset = Integer.parseInt(key.toString());
                }
                catch (NumberFormatException nfe) {
                    // Flag the error?
                    return;
                }
                // Check if already set.
                if (getStringForDay(keyOffset) == null) {
                    URelativeString newDayInfo = new URelativeString(keyOffset, value.getString());
                    fDates.add(newDayInfo);
                }
            }
        }
    }

    /**
     * Load the Date string array
     */
    private synchronized void loadDates() {
        ICUResourceBundle rb = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, fLocale);

        // Use sink mechanism to traverse data structure.
        fDates = new ArrayList<URelativeString>();
        RelDateFmtDataSink sink = new RelDateFmtDataSink();
        rb.getAllItemsWithFallback("fields/day/relative", sink);
    }

    /**
     * Set capitalizationOfRelativeUnitsForListOrMenu, capitalizationOfRelativeUnitsForStandAlone
     */
    private void initCapitalizationContextInfo(ULocale locale) {
        ICUResourceBundle rb = (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, locale);
        try {
            ICUResourceBundle rdb = rb.getWithFallback("contextTransforms/relative");
            int[] intVector = rdb.getIntVector();
            if (intVector.length >= 2) {
                capitalizationOfRelativeUnitsForListOrMenu = (intVector[0] != 0);
                capitalizationOfRelativeUnitsForStandAlone = (intVector[1] != 0);
            }
        } catch (MissingResourceException e) {
            // use default
        }
    }

    /**
     * @return the number of days in "until-now"
     */
    private static int dayDifference(Calendar until) {
        Calendar nowCal = (Calendar)until.clone();
        Date nowDate = new Date(System.currentTimeMillis());
        nowCal.clear();
        nowCal.setTime(nowDate);
        int dayDiff = until.get(Calendar.JULIAN_DAY) - nowCal.get(Calendar.JULIAN_DAY);
        return dayDiff;
    }

    /**
     * initializes fCalendar from parameters.  Returns fCalendar as a convenience.
     * @param zone  Zone to be adopted, or NULL for TimeZone::createDefault().
     * @param locale Locale of the calendar
     * @param status Error code
     * @return the newly constructed fCalendar
     */
    private Calendar initializeCalendar(TimeZone zone, ULocale locale) {
        if (calendar == null) {
            if(zone == null) {
                calendar = Calendar.getInstance(locale);
            } else {
                calendar = Calendar.getInstance(zone, locale);
            }
        }
        return calendar;
    }

    private MessageFormat initializeCombinedFormat(Calendar cal, ULocale locale) {
        String pattern;
        ICUResourceBundle rb = (ICUResourceBundle) UResourceBundle.getBundleInstance(
            ICUData.ICU_BASE_NAME, locale);
        String resourcePath = "calendar/" + cal.getType() + "/DateTimePatterns";
        ICUResourceBundle patternsRb= rb.findWithFallback(resourcePath);
        if (patternsRb == null && !cal.getType().equals("gregorian")) {
            // Try again with gregorian, if not already attempted.
            patternsRb = rb.findWithFallback("calendar/gregorian/DateTimePatterns");
        }

        if (patternsRb == null || patternsRb.getSize() < 9) {
            // Undefined or too few elements.
            pattern = "{1} {0}";
        } else {
            int glueIndex = 8;
            if (patternsRb.getSize() >= 13) {
              if (fDateStyle >= DateFormat.FULL && fDateStyle <= DateFormat.SHORT) {
                  glueIndex += fDateStyle + 1;
              } else
                  if (fDateStyle >= DateFormat.RELATIVE_FULL &&
                      fDateStyle <= DateFormat.RELATIVE_SHORT) {
                      glueIndex += fDateStyle + 1 - DateFormat.RELATIVE;
                  }
            }
            int elementType = patternsRb.get(glueIndex).getType();
            if (elementType == UResourceBundle.ARRAY) {
                pattern = patternsRb.get(glueIndex).getString(0);
            } else {
                pattern = patternsRb.getString(glueIndex);
            }
        }
        combinedFormatHasDateAtStart = pattern.startsWith("{1}");
        fCombinedFormat = new MessageFormat(pattern, locale);
        return fCombinedFormat;
    }
}
