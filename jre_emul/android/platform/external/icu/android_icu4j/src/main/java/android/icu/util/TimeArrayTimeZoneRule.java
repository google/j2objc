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
import java.util.Arrays;
import java.util.Date;

/**
 * <code>TimeArrayTimeZoneRule</code> represents a time zone rule whose start times are
 * defined by an array of milliseconds since the standard base time.
 *
 * @hide Only a subset of ICU is exposed in Android
 */
public class TimeArrayTimeZoneRule extends TimeZoneRule {

    private static final long serialVersionUID = -1117109130077415245L;

    private final long[] startTimes;
    private final int timeType;

    /**
     * Constructs a <code>TimeArrayTimeZoneRule</code> with the name, the GMT offset of its
     * standard time, the amount of daylight saving offset adjustment and
     * the array of times when this rule takes effect.
     *
     * @param name          The time zone name.
     * @param rawOffset     The UTC offset of its standard time in milliseconds.
     * @param dstSavings    The amount of daylight saving offset adjustment in
     *                      milliseconds.  If this ia a rule for standard time,
     *                      the value of this argument is 0.
     * @param startTimes    The start times in milliseconds since the base time
     *                      (January 1, 1970, 00:00:00).
     * @param timeType      The time type of the start times, which is one of
     *                      <code>DataTimeRule.WALL_TIME</code>, <code>STANDARD_TIME</code>
     *                      and <code>UTC_TIME</code>.
     */
    public TimeArrayTimeZoneRule(String name, int rawOffset, int dstSavings, long[] startTimes, int timeType) {
        super(name, rawOffset, dstSavings);
        if (startTimes == null || startTimes.length == 0) {
            throw new IllegalArgumentException("No start times are specified.");
        } else {
            this.startTimes = startTimes.clone();
            Arrays.sort(this.startTimes);
        }
        this.timeType = timeType;
    }

    /**
     * Gets the array of start times used by this rule.
     *
     * @return  An array of the start times in milliseconds since the base time
     *          (January 1, 1970, 00:00:00 GMT).
     */
    public long[] getStartTimes() {
        return startTimes.clone();
    }

    /**
     * Gets the time type of the start times used by this rule.  The return value
     * is either <code>DateTimeRule.WALL_TIME</code> or <code>DateTimeRule.STANDARD_TIME</code>
     * or <code>DateTimeRule.UTC_TIME</code>.
     *
     * @return The time type used of the start times used by this rule.
     */
    public int getTimeType() {
        return timeType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getFirstStart(int prevRawOffset, int prevDSTSavings) {
        return new Date(getUTC(startTimes[0], prevRawOffset, prevDSTSavings));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getFinalStart(int prevRawOffset, int prevDSTSavings) {
        return new Date(getUTC(startTimes[startTimes.length - 1], prevRawOffset, prevDSTSavings));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getNextStart(long base, int prevOffset, int prevDSTSavings, boolean inclusive) {
        int i = startTimes.length - 1;
        for (; i >= 0; i--) {
            long time = getUTC(startTimes[i], prevOffset, prevDSTSavings);
            if (time < base || (!inclusive && time == base)) {
                break;
            }
        }
        if (i == startTimes.length - 1) {
            return null;
        }
        return new Date(getUTC(startTimes[i + 1], prevOffset, prevDSTSavings));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getPreviousStart(long base, int prevOffset, int prevDSTSavings, boolean inclusive) {
        int i = startTimes.length - 1;
        for (; i >= 0; i--) {
            long time = getUTC(startTimes[i], prevOffset, prevDSTSavings);
            if (time < base || (inclusive && time == base)) {
                return new Date(time);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEquivalentTo(TimeZoneRule other) {
        if (!(other instanceof TimeArrayTimeZoneRule)) {
            return false;
        }
        if (timeType == ((TimeArrayTimeZoneRule)other).timeType
                && Arrays.equals(startTimes, ((TimeArrayTimeZoneRule)other).startTimes)) {
            return super.isEquivalentTo(other);
        }
        return false;
    }

    /**
     * {@inheritDoc}<br><br>
     * Note: This method in <code>TimeArrayTimeZoneRule</code> always returns true.
     */
    @Override
    public boolean isTransitionRule() {
        return true;
    }

    /* Get UTC of the time with the raw/dst offset */
    private long getUTC(long time, int raw, int dst) {
        if (timeType != DateTimeRule.UTC_TIME) {
            time -= raw;
        }
        if (timeType == DateTimeRule.WALL_TIME) {
            time -= dst;
        }
        return time;
    }

    /**
     * Returns a <code>String</code> representation of this <code>TimeArrayTimeZoneRule</code> object.
     * This method is used for debugging purpose only.  The string representation can be changed
     * in future version of ICU without any notice.
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        buf.append(", timeType=");
        buf.append(timeType);
        buf.append(", startTimes=[");
        for (int i = 0; i < startTimes.length; i++) {
            if (i != 0) {
                buf.append(", ");
            }
            buf.append(Long.toString(startTimes[i]));
        }
        buf.append("]");
        return buf.toString();
    }
}
