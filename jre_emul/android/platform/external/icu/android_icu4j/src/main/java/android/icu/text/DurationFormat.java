/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2007-2015, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.text;

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Date;

import android.icu.impl.duration.BasicDurationFormat;
import android.icu.util.ULocale;

/**
 * This <b>deprecated</b> class implements a formatter over a duration in time
 * such as "2 days from now" or "3 hours ago".
 *
 * <p>Use MeasureFormat to format periods like "5 days, 3 hours";
 * use RelativeDateTimeFormatter to format relative dates like "5 days ago".
 *
 * @see MeasureFormat
 * @see RelativeDateTimeFormatter
 * @deprecated ICU 56 Use MeasureFormat or RelativeDateTimeFormatter instead.
 * @hide Only a subset of ICU is exposed in Android
 */
@Deprecated
public abstract class DurationFormat extends UFormat {

    private static final long serialVersionUID = -2076961954727774282L;

    /**
     * Construct a duration format for the specified locale
     * @deprecated ICU 56
     */
    @Deprecated
    public static DurationFormat getInstance(ULocale locale) {
        return BasicDurationFormat.getInstance(locale);
    }


    /**
     * Subclass interface
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected DurationFormat() {
    }

    /**
     * Subclass interface
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected DurationFormat(ULocale locale) {
        setLocale(locale,locale);
    }

    /**
     * Format an arbitrary object.
     * Defaults to a call to formatDurationFromNow() for either Long or Date objects.
     * @param object the object to format. Should be either a Long, Date, or javax.xml.datatype.Duration object.
     * @param toAppend the buffer to append to
     * @param pos the field position, may contain additional error messages.
     * @return the toAppend buffer
     * @deprecated ICU 56
     */
    @Deprecated
    @Override
    public abstract StringBuffer format(Object object, StringBuffer toAppend,
            FieldPosition pos);

    /**
     * DurationFormat cannot parse, by default. This method will throw an UnsupportedOperationException.
     * @deprecated ICU 56
     */
    @Override
    @Deprecated
    public Object parseObject(String source, ParsePosition pos) {
       throw new UnsupportedOperationException();
    }

    /**
     * Formats the duration between now and a target date.
     * <p>
     * This is a convenience method that calls
     * formatDurationFrom(long, long) using now
     * as the reference date, and the difference between now and
     * <code>targetDate.getTime()</code> as the duration.
     *
     * @param targetDate the ending date
     * @return the formatted time
     * @deprecated ICU 56
     */
    @Deprecated
    public abstract String formatDurationFromNowTo(Date targetDate);

    /**
     * Formats a duration expressed in milliseconds.
     * <p>
     * This is a convenience method that calls formatDurationFrom
     * using the current system time as the reference date.
     *
     * @param duration the duration in milliseconds
     * @return the formatted time
     * @deprecated ICU 56
     */
    @Deprecated
    public abstract String formatDurationFromNow(long duration);

    /**
     * Formats a duration expressed in milliseconds from a reference date.
     * <p>
     * The reference date allows formatters to use actual durations of
     * variable-length periods (like months) if they wish.
     * <p>
     * The duration is expressed as the number of milliseconds in the
     * past (negative values) or future (positive values) with respect
     * to a reference date (expressed as milliseconds in epoch).
     *
     * @param duration the duration in milliseconds
     * @param referenceDate the date from which to compute the duration
     * @return the formatted time
     * @deprecated ICU 56
     */
    @Deprecated
    public abstract String formatDurationFrom(long duration, long referenceDate);
}
