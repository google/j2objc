/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2011-2012, Google, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.dev.util;

import android.icu.text.DecimalFormat;
import android.icu.text.NumberFormat;
import android.icu.util.ULocale;

public final class Timer {
    public static final long SECONDS = 100000000;

    private long startTime;
    private long duration;
    private boolean timing = false;
    private int iterations;
    private long timingPeriod = 5*SECONDS;
    {
        start();
    }

    public Timer start() {
        startTime = System.nanoTime();
        timing = true;
        duration = Long.MIN_VALUE;
        return this;
    }

    public long getDuration() {
        if (timing) {
            duration = System.nanoTime() - startTime;
            timing = false;
        }
        return duration;
    }

    public long stop() {
        return getDuration();
    }

    public int getIterations() {
        return iterations;
    }

    public long getTimingPeriod() {
        return timingPeriod;
    }

    public Timer setTimingPeriod(long timingPeriod) {
        this.timingPeriod = timingPeriod;
        return this;
    }

    public DecimalFormat getNumberFormat() {
        return nf;
    }

    public DecimalFormat getPercentFormat() {
        return pf;
    }

    public String toString() {
        return nf.format(getDuration()) + "\tns";
    }
    public String toString(Timer other) {
        return toString(1L, other.getDuration());
    }
    public String toString(long iterations) {
        return nf.format(getDuration()/iterations) + "\tns";
    }

    public String toString(long iterations, long other) {
        return nf.format(getDuration()/iterations) + "\tns\t" + pf.format((double)getDuration()/other - 1D) + "";
    }

    private DecimalFormat nf = (DecimalFormat) NumberFormat.getNumberInstance(ULocale.ENGLISH);
    private DecimalFormat pf = (DecimalFormat) NumberFormat.getPercentInstance(ULocale.ENGLISH);
    
    {
        pf.setMaximumFractionDigits(1);
        pf.setPositivePrefix("+");
    }

    public abstract static class Loop {
        public void init(Object... params) {}
        abstract public void time(int repeat);
    }

    public long timeIterations(Loop loop, Object... params) {
        // Timing on Java is very tricky, especially when you count in garbage collection. This is a simple strategy for now, we might improve later.
        // The current strategy is to warm up once, then time it until we reach the timingPeriod (eg 5 seconds), increasing the iterations each time
        // At first, we double the iterations.
        // Once we get to within 1/4 of the timingPeriod, we change to adding 33%, plus 1. We also remember the shortest duration from this point on.
        // We return the shortest of the durations.
        loop.init(params);
        System.gc();
        start();
        loop.time(1);
        stop();
        iterations = 1;
        long shortest = Long.MAX_VALUE;
        while (true) {
            System.gc();
            start();
            loop.time(iterations);
            stop();
            if (duration >= timingPeriod) {
                duration /= iterations;
                return Math.min(duration, shortest);
            } else if (duration >= timingPeriod / 4) {
                duration /= iterations;
                shortest = Math.min(duration, shortest);
                iterations = (iterations * 4) / 3 + 1;
            } else {
                iterations = iterations * 2;
            }
        }
    }
}