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
/**
 * <code>TimeZoneTransition</code> is a class representing a time zone transition.
 * An instance has a time of transition and rules for both before and
 * after the transition.
 *
 * @hide Only a subset of ICU is exposed in Android
 */
public class TimeZoneTransition {
    private final TimeZoneRule from;
    private final TimeZoneRule to;
    private final long time;

    /**
     * Constructs a <code>TimeZoneTransition</code> with the time and the rules before/after
     * the transition.
     *
     * @param time  The time of transition in milliseconds since the base time.
     * @param from  The time zone rule used before the transition.
     * @param to    The time zone rule used after the transition.
     */
    public TimeZoneTransition(long time, TimeZoneRule from, TimeZoneRule to) {
        this.time = time;
        this.from = from;
        this.to = to;
    }

    /**
     * Returns the time of transition in milliseconds since the base time.
     *
     * @return The time of the transition in milliseconds since the base time.
     */
    public long getTime() {
        return time;
    }

    /**
     * Returns the rule used after the transition.
     *
     * @return The time zone rule used after the transition.
     */
    public TimeZoneRule getTo() {
        return to;
    }

    /**
     * Returns the rule used before the transition.
     *
     * @return The time zone rule used after the transition.
     */
    public TimeZoneRule getFrom() {
        return from;
    }

    /**
     * Returns a <code>String</code> representation of this <code>TimeZoneTransition</code> object.
     * This method is used for debugging purpose only.  The string representation can be changed
     * in future version of ICU without any notice.
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("time=" + time);
        buf.append(", from={" + from + "}");
        buf.append(", to={" + to + "}");
        return buf.toString();
    }
}
