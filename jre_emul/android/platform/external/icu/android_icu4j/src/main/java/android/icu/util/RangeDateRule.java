/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package android.icu.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <b>Note:</b> The Holiday framework is a technology preview.
 * Despite its age, is still draft API, and clients should treat it as such.
 * 
 * Implementation of DateRule that takes a range.
 * @hide Only a subset of ICU is exposed in Android
 * @hide draft / provisional / internal are hidden on Android
 */
public class RangeDateRule implements DateRule {
    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public RangeDateRule() {
    }

    // Range is a package-private class so this should be package-private too, probably
//    public RangeDateRule(Range[] ranges)
//    {
//        for (int i = 0; i < ranges.length; i++) {
//            this.ranges.addElement(ranges[i]);
//        }
//    }

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public void add(DateRule rule) {
        add(new Date(Long.MIN_VALUE), rule);
    }

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public void add(Date start, DateRule rule) {
        // TODO: Insert in the right place
        // System.out.println("Add: " + start.toString());
        ranges.add(new Range(start, rule));
    }

    //-----------------------------------------------------------------------

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public Date firstAfter(Date start) {
        // Find the range that I should look at
        int index = startIndex(start);
        if (index == ranges.size()) {
            index = 0;
        }
        Date result = null;

        Range r = rangeAt(index);
        Range e = rangeAt(index+1);

        if (r != null && r.rule != null)
        {
            if (e != null) {
                result = r.rule.firstBetween(start, e.start);
            } else {
                result = r.rule.firstAfter(start);
            }
        }
        return result;
    }

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public Date firstBetween(Date start, Date end) {
        if (end == null) {
            return firstAfter(start);
        }
        
        // Find the range that I should look at
        int index = startIndex(start);
        Date result = null;

        Range next = rangeAt(index);

        while (result == null && next != null && !next.start.after(end))
        {
            Range r = next;
            next = rangeAt(index+1);

            if (r.rule != null) {
                Date e = (next != null && !next.start.after(end)) ? next.start
                                                                  : end;
                result = r.rule.firstBetween(start, e);
            }
        }
        return result;
    }

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    public boolean isOn(Date date) {
        Range r = rangeAt(startIndex(date));
        return r != null && r.rule != null && r.rule.isOn(date);
    }

    /**
     * Check whether this event occurs at least once between the two
     * dates given.
     * @hide draft / provisional / internal are hidden on Android
     */
    public boolean isBetween(Date start, Date end) {
        return firstBetween(start,end) == null;
    }

    /*
     * find the index of the last range whose start date is before "start"
     * returns an index >= ranges.size() if there is none
     */
    private int startIndex(Date start) {
        int lastIndex = ranges.size();

        for (int i = 0; i < ranges.size(); i++) {
            Range r = ranges.get(i);
            if (start.before(r.start)) {
                break;
            }
            lastIndex = i;
        }
        return lastIndex;
    }

    private Range rangeAt(int index) {
       return (index < ranges.size()) ? ranges.get(index)
                                      : null;
    }

    List<Range> ranges = new ArrayList<Range>(2);
}

//-----------------------------------------------------------------------
// Privates
//

class Range {
    public Range(Date start, DateRule rule) {
        this.start = start;
        this.rule = rule;
    }
    public Date     start;
    public DateRule rule;
}

