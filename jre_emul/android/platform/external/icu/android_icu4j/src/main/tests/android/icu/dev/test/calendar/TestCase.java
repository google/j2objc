/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.calendar;

import java.util.Date;
import java.util.Locale;

import android.icu.dev.test.TestLog;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.icu.util.SimpleTimeZone;

/**
 * A pseudo <code>Calendar</code> that is useful for testing
 * new calendars.  A <code>TestCase</code> object is used to hold the
 * field and millisecond values that the calendar should have at one
 * particular instant in time.  The applyFields and applyTime
 * methods are used to apply these settings to the calendar object being
 * tested, and the equals and fieldsEqual methods are used to ensure
 * that the calendar has ended up in the right state.
 */
public class TestCase {

    //------------------------------------------------------------------
    // Pseudo-Calendar fields and methods
    //------------------------------------------------------------------

    protected int[] fields = new int[32];
    protected boolean[] isSet = new boolean[32];
    protected long time;

    protected void set(int field, int value) {
        fields[field] = value;
        isSet[field] = true;
    }

    protected int get(int field) {
        return fields[field];
    }

    protected boolean isSet(int field) {
        return isSet[field];
    }

    protected void setTime(Date d) {
        time = d.getTime();
    }

    public Date getTime() {
        return new Date(time);
    }

    /**
     * Return a String representation of this test case's time.
     */
    public String toString() {
        return dowToString(get(Calendar.DAY_OF_WEEK)) + " " +
            get(Calendar.YEAR) + "/" + (get(Calendar.MONTH)+1) + "/" +
            get(Calendar.DATE);
    }

    private static final String[] DOW_NAMES = {
        "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
    };

    public static String dowToString(int dow) {
        --dow;
        return (dow < 0 || dow > 6) ?
            ("<DOW " + dow + ">") : DOW_NAMES[dow];
    }

    /**
     * Initialize a TestCase object using a julian day number and
     * the corresponding fields for the calendar being tested.
     *
     * @param era       The ERA field of tested calendar on the given julian day
     * @param year      The YEAR field of tested calendar on the given julian day
     * @param month     The MONTH (1-based) field of tested calendar on the given julian day
     * @param day       The DAY_OF_MONTH field of tested calendar on the given julian day
     * @param dayOfWeek The DAY_OF_WEEK field of tested calendar on the given julian day
     * @param hour      The HOUR field of tested calendar on the given julian day
     * @param min       The MINUTE field of tested calendar on the given julian day
     * @param sec       The SECOND field of tested calendar on the given julian day
     */
    public TestCase(double julian,
                    int era, int year, int month, int day,
                    int dayOfWeek,
                    int hour, int min, int sec)
    {
        setTime(new Date(JULIAN_EPOCH + (long)(ONE_DAY * julian)));

        set(Calendar.ERA, era);
        set(Calendar.YEAR, year);
        set(Calendar.MONTH, month - 1);
        set(Calendar.DATE, day);
        set(Calendar.DAY_OF_WEEK, dayOfWeek);
        set(Calendar.HOUR, hour);
        set(Calendar.MINUTE, min);
        set(Calendar.SECOND, sec);
    }

    /**
     * Initialize a TestCase object using a Gregorian year/month/day and
     * the corresponding fields for the calendar being tested.
     *
     * @param gregYear  The Gregorian year of the date to be tested
     * @param gregMonth The Gregorian month of the date to be tested
     * @param gregDay   The Gregorian day of the month of the date to be tested
     *
     * @param era       The ERA field of tested calendar on the given gregorian date
     * @param year      The YEAR field of tested calendar on the given gregorian date
     * @param month     The MONTH (0-based) field of tested calendar on the given gregorian date
     * @param day       The DAY_OF_MONTH field of tested calendar on the given gregorian date
     * @param dayOfWeek The DAY_OF_WEEK field of tested calendar on the given gregorian date
     * @param hour      The HOUR field of tested calendar on the given gregorian date
     * @param min       The MINUTE field of tested calendar on the given gregorian date
     * @param sec       The SECOND field of tested calendar on the given gregorian date
     */
    public TestCase(int gregYear, int gregMonth, int gregDay,
                    int era, int year, int month, int day,
                    int dayOfWeek,
                    int hour, int min, int sec)
    {
        GregorianCalendar greg = new GregorianCalendar(UTC, Locale.getDefault());
        greg.clear();
        greg.set(gregYear, gregMonth-1, gregDay);
        setTime(greg.getTime());

        set(Calendar.ERA, era);
        set(Calendar.YEAR, year);
        set(Calendar.MONTH, month - 1);
        set(Calendar.DATE, day);
        set(Calendar.DAY_OF_WEEK, dayOfWeek);
        set(Calendar.HOUR, hour);
        set(Calendar.MINUTE, min);
        set(Calendar.SECOND, sec);
    }

    /**
     * For subclasses.
     */
    protected TestCase() {}

    /**
     * Apply this test case's field values to another calendar
     * by calling its set method for each field.  This is useful in combination
     * with the equal method.
     *
     * @see android.icu.util.Calendar#equals
     */
    public void applyFields(Calendar c) {
        for (int i=0; i < c.getFieldCount(); i++) {
            if (isSet(i)) {
                c.set(i, get(i));
            }
        }
    }

    /**
     * Apply this test case's time in milliseconds to another calendar
     * by calling its setTime method.  This is useful in combination
     * with fieldsEqual
     *
     * @see #fieldsEqual
     */
    public void applyTime(Calendar c) {
        c.setTime(new Date(time));
    }

    /**
     * Determine whether the fields of this calendar
     * are the same as that of the other calendar.  This method is useful
     * for determining whether the other calendar's computeFields method
     * works properly.  For example:
     * <pre>
     *    Calendar testCalendar = ...
     *    TestCase case = ...
     *    case.applyTime(testCalendar);
     *    if (!case.fieldsEqual(testCalendar)) {
     *        // Error!
     *    }
     * </pre>
     *
     * @see #applyTime
     */
    public boolean fieldsEqual(Calendar c, TestLog log) {
        for (int i=0; i < c.getFieldCount(); i++) {
            if (isSet(i) && get(i) != c.get(i)) {
                StringBuffer buf = new StringBuffer();
                buf.append("Fail: " + CalendarTestFmwk.fieldName(i) + " = " + c.get(i) +
                          ", expected " + get(i));
                for (int j=0; j<c.getFieldCount(); ++j) {
                    if (isSet(j)) {
                        if (get(j) == c.get(j)) {
                            buf.append("\n  ok: " + CalendarTestFmwk.fieldName(j) + " = " +
                                      c.get(j));
                        } else {
                            buf.append("\n  fail: " + CalendarTestFmwk.fieldName(j) + " = " +
                                      c.get(j) + ", expected " + get(j));
                        }
                    }
                }
                // TODO(user): blanked out TestLog
                //log.errln(buf.toString());
                return false;
            }
        }

        return true;
    }

    /**
     * Determine whether time in milliseconds of this calendar
     * is the same as that of the other calendar.  This method is useful
     * for determining whether the other calendar's computeTime method
     * works properly.  For example:
     * <pre>
     *    Calendar testCalendar = ...
     *    TestCase case = ...
     *    case.applyFields(testCalendar);
     *    if (!case.equals(testCalendar)) {
     *        // Error!
     *    }
     * </pre>
     *
     * @see #applyFields
     */
    public boolean equals(Object obj) {
        return time == ((Calendar)obj).getTime().getTime();
    }

    protected static final int  ONE_SECOND = 1000;
    protected static final int  ONE_MINUTE = 60*ONE_SECOND;
    protected static final int  ONE_HOUR   = 60*ONE_MINUTE;
    protected static final long ONE_DAY    = 24*ONE_HOUR;
    protected static final long JULIAN_EPOCH = -210866760000000L;   // 1/1/4713 BC 12:00

    public final static SimpleTimeZone UTC = new SimpleTimeZone(0, "GMT");
}
