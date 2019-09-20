/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2001-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

/** 
 * Porting From: ICU4C v1.8.1 : format : NumberFormatRoundTripTest
 * Source File: $ICU4CRoot/source/test/intltest/nmfmtrt.cpp
 **/

package android.icu.dev.test.format;

import java.util.Locale;
import java.util.Random;

import org.junit.Test;

import android.icu.text.DecimalFormat;
import android.icu.text.NumberFormat;

/** 
 * Performs round-trip tests for NumberFormat
 **/
public class NumberFormatRoundTripTest extends android.icu.dev.test.TestFmwk {
    
    public double MAX_ERROR = 1e-14;
    public double max_numeric_error = 0.0;
    public double min_numeric_error = 1.0;
    public boolean verbose = false;
    public boolean STRING_COMPARE = false;
    public boolean EXACT_NUMERIC_COMPARE = false;
    public boolean DEBUG = false;
    public boolean quick = true;
    
    @Test
    public void TestNumberFormatRoundTrip() {
    
        NumberFormat fmt = null;
    
        logln("Default Locale");
        
        logln("Default Number format");
        fmt = NumberFormat.getInstance();
        _test(fmt);
    
        logln("Currency Format");
        fmt = NumberFormat.getCurrencyInstance();
        _test(fmt);
    
        logln("Percent Format");
        fmt = NumberFormat.getPercentInstance();
        _test(fmt);
    
    
        int locCount = 0;
        final Locale[] loc = NumberFormat.getAvailableLocales();
        if(quick) {
            if(locCount > 5)
                locCount = 5;
            logln("Quick mode: only _testing first 5 Locales");
        }
        for(int i = 0; i < locCount; ++i) {
            logln(loc[i].getDisplayName());
    
            fmt = NumberFormat.getInstance(loc[i]);
            _test(fmt);
        
            fmt = NumberFormat.getCurrencyInstance(loc[i]);
            _test(fmt);
        
            fmt = NumberFormat.getPercentInstance(loc[i]);
            _test(fmt);
        }
    
        logln("Numeric error " + min_numeric_error + " to " + max_numeric_error);
    }
    
    /**
     * Return a random value from -range..+range.
     */
    private Random random;
    public double randomDouble(double range) {
        if (random == null) {
            random = createRandom(); // use test framework's random seed
        }
        return  random.nextDouble() * range;
    } 
    
    private void _test(NumberFormat fmt) {
    
        _test(fmt, Double.NaN);
        _test(fmt, Double.POSITIVE_INFINITY);
        _test(fmt, Double.NEGATIVE_INFINITY);
    
        _test(fmt, 500);
        _test(fmt, 0);
        _test(fmt, -0);
        _test(fmt, 0.0);
        double negZero = 0.0;
        negZero /= -1.0;
        _test(fmt, negZero);
        _test(fmt, 9223372036854775808.0d);
        _test(fmt, -9223372036854775809.0d);
        //_test(fmt, 6.936065876100493E74d);
        
    //    _test(fmt, 6.212122845281909E48d);
        for (int i = 0; i < 10; ++i) {
    
            _test(fmt, randomDouble(1));
            
            _test(fmt, randomDouble(10000));
    
            _test(fmt, Math.floor((randomDouble(10000))));
    
            _test(fmt, randomDouble(1e50));
    
            _test(fmt, randomDouble(1e-50));
    
            _test(fmt, randomDouble(1e100));
    
            _test(fmt, randomDouble(1e75));
    
            _test(fmt, randomDouble(1e308) / ((DecimalFormat) fmt).getMultiplier());
    
            _test(fmt, randomDouble(1e75) / ((DecimalFormat) fmt).getMultiplier());
    
            _test(fmt, randomDouble(1e65) / ((DecimalFormat) fmt).getMultiplier());
    
            _test(fmt, randomDouble(1e-292));
    
            _test(fmt, randomDouble(1e-78));
    
            _test(fmt, randomDouble(1e-323));
    
            _test(fmt, randomDouble(1e-100));
    
            _test(fmt, randomDouble(1e-78));
        }
    }
    
    private void _test(NumberFormat fmt, double value) {
        _test(fmt, new Double(value));
    }
    
    private void _test(NumberFormat fmt, long value) {
        _test(fmt, new Long(value));
    }
    
    private void _test(NumberFormat fmt, Number value) {
        logln("test data = " + value);
        fmt.setMaximumFractionDigits(999);
        String s, s2;
        if (value.getClass().getName().equalsIgnoreCase("java.lang.Double"))
            s = fmt.format(value.doubleValue());
        else
            s = fmt.format(value.longValue());
    
        Number n = new Double(0);
        boolean show = verbose;
        if (DEBUG)
            logln(
            /*value.getString(temp) +*/ " F> " + s);
        try {
            n = fmt.parse(s);
        } catch (java.text.ParseException e) {
            System.out.println(e);
        }
    
        if (DEBUG)
            logln(s + " P> " /*+ n.getString(temp)*/);
    
        if (value.getClass().getName().equalsIgnoreCase("java.lang.Double"))
            s2 = fmt.format(n.doubleValue());
        else
            s2 = fmt.format(n.longValue());
    
        if (DEBUG)
            logln(/*n.getString(temp) +*/ " F> " + s2);
    
        if (STRING_COMPARE) {
            if (!s.equals(s2)) {
                errln("*** STRING ERROR \"" + s + "\" != \"" + s2 + "\"");
                show = true;
            }
        }
    
        if (EXACT_NUMERIC_COMPARE) {
            if (value != n) {
                errln("*** NUMERIC ERROR");
                show = true;
            }
        } else {
            // Compute proportional error
            double error = proportionalError(value, n);
    
            if (error > MAX_ERROR) {
                errln("*** NUMERIC ERROR " + error);
                show = true;
            }
    
            if (error > max_numeric_error)
                max_numeric_error = error;
            if (error < min_numeric_error)
                min_numeric_error = error;
        }
    
        if (show)
            logln(
            /*value.getString(temp) +*/ value.getClass().getName() + " F> " + s + " P> " +
            /*n.getString(temp) +*/ n.getClass().getName() + " F> " + s2);
    
    }
        
    private double proportionalError(Number a, Number b) {
        double aa,bb;
        
        if(a.getClass().getName().equalsIgnoreCase("java.lang.Double"))
            aa = a.doubleValue();
        else
            aa = a.longValue();
    
        if(a.getClass().getName().equalsIgnoreCase("java.lang.Double"))
            bb = b.doubleValue();
        else
            bb = b.longValue();
    
        double error = aa - bb;
        if(aa != 0 && bb != 0) 
            error /= aa;
           
        return Math.abs(error);
    }   
}
