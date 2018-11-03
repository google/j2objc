/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2007-2008, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.util;

import java.util.Date;

/**
 * <code>InitialTimeZoneRule</code> represents a time zone rule
 * representing a time zone effective from the beginning and
 * has no actual start times.
 *
 * @hide Only a subset of ICU is exposed in Android
 */
public class InitialTimeZoneRule extends TimeZoneRule {

    private static final long serialVersionUID = 1876594993064051206L;

    /**
     * Constructs a <code>InitialTimeZoneRule</code> with the name, the GMT offset of its
     * standard time and the amount of daylight saving offset adjustment.
     *
     * @param name          The time zone name.
     * @param rawOffset     The UTC offset of its standard time in milliseconds.
     * @param dstSavings    The amount of daylight saving offset adjustment in milliseconds.
     *                      If this ia a rule for standard time, the value of this argument is 0.
     */
    public InitialTimeZoneRule(String name, int rawOffset, int dstSavings) {
        super(name, rawOffset, dstSavings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEquivalentTo(TimeZoneRule other) {
        if (other instanceof InitialTimeZoneRule) {
            return super.isEquivalentTo(other);
        }
        return false;
    }

    /**
     * {@inheritDoc}<br><br>
     * Note: This method in <code>InitialTimeZoneRule</code> always returns null.
     */
    @Override
    public Date getFinalStart(int prevRawOffset, int prevDSTSavings) {
        // No start time available
        return null;
    }

    /**
     * {@inheritDoc}<br><br>
     * Note: This method in <code>InitialTimeZoneRule</code> always returns null.
     */
    @Override
    public Date getFirstStart(int prevRawOffset, int prevDSTSavings) {
        // No start time available
        return null;
    }

    /**
     * {@inheritDoc}<br><br>
     * Note: This method in <code>InitialTimeZoneRule</code> always returns null.
     */
    @Override
    public Date getNextStart(long base, int prevRawOffset, int prevDSTSavings,
            boolean inclusive) {
        // No start time available
        return null;
    }

    /**
     * {@inheritDoc}<br><br>
     * Note: This method in <code>InitialTimeZoneRule</code> always returns null.
     */
    @Override
    public Date getPreviousStart(long base, int prevRawOffset,
            int prevDSTSavings, boolean inclusive) {
        // No start time available
        return null;
    }

    /**
     * {@inheritDoc}<br><br>
     * Note: This method in <code>InitialTimeZoneRule</code> always returns false.
     */
    @Override
    public boolean isTransitionRule() {
        return false;
    }
}
