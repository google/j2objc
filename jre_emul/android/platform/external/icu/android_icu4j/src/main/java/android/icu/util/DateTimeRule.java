/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2007-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.util;

import java.io.Serializable;

/**
 * <code>DateTimeRule</code> is a class representing a time in a year by
 * a rule specified by month, day of month, day of week and
 * time in the day.
 *
 * @hide Only a subset of ICU is exposed in Android
 */
public class DateTimeRule implements Serializable {

    private static final long serialVersionUID = 2183055795738051443L;

    /**
     * Date rule type defined by exact day of month.
     * For example, March 14.
     */
    public static final int DOM = 0;

    /**
     * Date rule type defined by day of week in month.
     * For example, 2nd Sunday in March.
     */
    public static final int DOW = 1;

    /**
     * Date rule type defined by first day of week on or
     * after exact day of month.
     * For example, 1st Monday on or after March 15.
     */
    public static final int DOW_GEQ_DOM = 2;

    /**
     * Date rule type defined by last day of week on or
     * before exact day of month.
     * For example, last Saturday on or before March 15.
     */
    public static final int DOW_LEQ_DOM = 3;

    /**
     * Time rule type for local wall time.
     */
    public static final int WALL_TIME = 0;

    /**
     * Time rule type for local standard time.
     */
    public static final int STANDARD_TIME = 1;

    /**
     * Time rule type for coordinated universal time.
     */
    public static final int UTC_TIME = 2;

    // private stuff
    private final int dateRuleType;
    private final int month;
    private final int dayOfMonth;
    private final int dayOfWeek;
    private final int weekInMonth;

    private final int timeRuleType;
    private final int millisInDay;

    /**
     * Constructs a <code>DateTimeRule</code> by the day of month and
     * the time rule.  The date rule type for an instance created by
     * this constructor is <code>DOM</code>.
     *
     * @param month         The rule month, for example, <code>Calendar.JANUARY</code>
     * @param dayOfMonth    The day of month, 1-based.
     * @param millisInDay   The milliseconds in the rule date.
     * @param timeType      The time type, <code>WALL_TIME</code> or <code>STANDARD_TIME</code>
     *                      or <code>UTC_TIME</code>.
     */
    public DateTimeRule(int month, int dayOfMonth,
            int millisInDay, int timeType) {
        dateRuleType = DOM;
        this.month = month;
        this.dayOfMonth = dayOfMonth;

        this.millisInDay = millisInDay;
        this.timeRuleType = timeType;

        // not used by this rule type
        this.dayOfWeek = 0;
        this.weekInMonth = 0;
    }

    /**
     * Constructs a <code>DateTimeRule</code> by the day of week and its oridinal
     * number and the time rule.  The date rule type for an instance created
     * by this constructor is <code>DOW</code>.
     *
     * @param month         The rule month, for example, <code>Calendar.JANUARY</code>.
     * @param weekInMonth   The ordinal number of the day of week.  Negative number
     *                      may be used for specifying a rule date counted from the
     *                      end of the rule month.
     * @param dayOfWeek     The day of week, for example, <code>Calendar.SUNDAY</code>.
     * @param millisInDay   The milliseconds in the rule date.
     * @param timeType      The time type, <code>WALL_TIME</code> or <code>STANDARD_TIME</code>
     *                      or <code>UTC_TIME</code>.
     */
    public DateTimeRule(int month, int weekInMonth, int dayOfWeek,
            int millisInDay, int timeType) {
        dateRuleType = DOW;
        this.month = month;
        this.weekInMonth = weekInMonth;
        this.dayOfWeek = dayOfWeek;

        this.millisInDay = millisInDay;
        this.timeRuleType = timeType;

        // not used by this rule type
        this.dayOfMonth = 0;
    }

    /**
     * Constructs a <code>DateTimeRule</code> by the first/last day of week
     * on or after/before the day of month and the time rule.  The date rule
     * type for an instance created by this constructor is either
     * <code>DOM_GEQ_DOM</code> or <code>DOM_LEQ_DOM</code>.
     *
     * @param month         The rule month, for example, <code>Calendar.JANUARY</code>
     * @param dayOfMonth    The day of month, 1-based.
     * @param dayOfWeek     The day of week, for example, <code>Calendar.SUNDAY</code>.
     * @param after         true if the rule date is on or after the day of month.
     * @param millisInDay   The milliseconds in the rule date.
     * @param timeType      The time type, <code>WALL_TIME</code> or <code>STANDARD_TIME</code>
     *                      or <code>UTC_TIME</code>.
     */
    public DateTimeRule(int month, int dayOfMonth, int dayOfWeek, boolean after,
            int millisInDay, int timeType) {
        this.dateRuleType = after ? DOW_GEQ_DOM : DOW_LEQ_DOM;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek = dayOfWeek;

        this.millisInDay = millisInDay;
        this.timeRuleType = timeType;

        // not used by this rule type
        this.weekInMonth = 0;
    }

    /**
     * Gets the date rule type, such as <code>DOM</code>
     *
     * @return The date rule type.
     */
    public int getDateRuleType() {
        return dateRuleType;
    }

    /**
     * Gets the rule month.
     *
     * @return The rule month.
     */
    public int getRuleMonth() {
        return month;
    }

    /**
     * Gets the rule day of month.  When the date rule type
     * is <code>DOW</code>, the value is always 0.
     *
     * @return The rule day of month
     */
    public int getRuleDayOfMonth() {
        return dayOfMonth;
    }

    /**
     * Gets the rule day of week.  When the date rule type
     * is <code>DOM</code>, the value is always 0.
     *
     * @return The rule day of week.
     */
    public int getRuleDayOfWeek() {
        return dayOfWeek;
    }

    /**
     * Gets the rule day of week ordinal number in the month.
     * When the date rule type is not <code>DOW</code>, the value is
     * always 0.
     *
     * @return The rule day of week ordinal number in the month.
     */
    public int getRuleWeekInMonth() {
        return weekInMonth;
    }

    /**
     * Gets the time rule type
     *
     * @return The time rule type, either <code>WALL_TIME</code> or <code>STANDARD_TIME</code>
     *         or <code>UTC_TIME</code>.
     */
    public int getTimeRuleType() {
        return timeRuleType;
    }

    /**
     * Gets the rule time in the rule day.
     *
     * @return The time in the rule day in milliseconds.
     */
    public int getRuleMillisInDay() {
        return millisInDay;
    }

    private static final String[] DOWSTR = {"", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private static final String[] MONSTR = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    /**
     * Returns a <code>String</code> representation of this <code>DateTimeRule</code> object.
     * This method is used for debugging purpose only.  The string representation can be changed
     * in future version of ICU without any notice.
     */
    @Override
    public String toString() {
        String sDate = null;
        String sTimeRuleType = null;

        switch (dateRuleType) {
        case DOM:
            sDate = Integer.toString(dayOfMonth);
            break;
        case DOW:
            sDate = Integer.toString(weekInMonth) + DOWSTR[dayOfWeek];
            break;
        case DOW_GEQ_DOM:
            sDate = DOWSTR[dayOfWeek] + ">=" + Integer.toString(dayOfMonth);
            break;
        case DOW_LEQ_DOM:
            sDate = DOWSTR[dayOfWeek] + "<=" + Integer.toString(dayOfMonth);
            break;
        }

        switch (timeRuleType) {
        case WALL_TIME:
            sTimeRuleType = "WALL";
            break;
        case STANDARD_TIME:
            sTimeRuleType = "STD";
            break;
        case UTC_TIME:
            sTimeRuleType = "UTC";
            break;
        }

        int time = millisInDay;
        int millis = time % 1000;
        time /= 1000;
        int secs = time % 60;
        time /= 60;
        int mins = time % 60;
        int hours = time / 60;

        StringBuilder buf = new StringBuilder();
        buf.append("month=");
        buf.append(MONSTR[month]);
        buf.append(", date=");
        buf.append(sDate);
        buf.append(", time=");
        buf.append(hours);
        buf.append(":");
        buf.append(mins/10);
        buf.append(mins%10);
        buf.append(":");
        buf.append(secs/10);
        buf.append(secs%10);
        buf.append(".");
        buf.append(millis/100);
        buf.append((millis/10)%10);
        buf.append(millis%10);
        buf.append("(");
        buf.append(sTimeRuleType);
        buf.append(")");
        return buf.toString();
    }
}
