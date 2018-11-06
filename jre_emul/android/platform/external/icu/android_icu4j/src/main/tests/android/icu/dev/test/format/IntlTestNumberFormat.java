/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2001-2011, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

/** 
 * Port From:   ICU4C v1.8.1 : format : IntlTestNumberFormat
 * Source File: $ICU4CRoot/source/test/intltest/tsnmfmt.cpp
 **/

package android.icu.dev.test.format;
import java.util.Locale;
import java.util.Random;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.DecimalFormat;
import android.icu.text.NumberFormat;

/**
 * This test does round-trip testing (format -> parse -> format -> parse -> etc.) of
 * NumberFormat.
 */
public class IntlTestNumberFormat extends android.icu.dev.test.TestFmwk {
    
    public NumberFormat fNumberFormat;

    /**
     * Internal use
     */
    private void _testLocale(Locale locale) {
        String localeName = locale + " (" + locale.getDisplayName() + ")";
            
        logln("Number test " + localeName);
        fNumberFormat = NumberFormat.getInstance(locale);
        _testFormat();
    
        logln("Currency test " + localeName);
        fNumberFormat = NumberFormat.getCurrencyInstance(locale);
        _testFormat();
    
        logln("Percent test " + localeName);
        fNumberFormat = NumberFormat.getPercentInstance(locale);
        _testFormat();

        if (locale.toString().compareTo("en_US_POSIX") != 0 ) {
           logln("Scientific test " + localeName);
           fNumberFormat = NumberFormat.getScientificInstance(locale);
           _testFormat();
        }
    }
    
    /**
     * call _testFormat for currency, percent and plain number instances
     */
    @Test
    public void TestLocale() {
        Locale locale = Locale.getDefault();
        String localeName = locale + " (" + locale.getDisplayName() + ")";
            
        logln("Number test " + localeName);
        fNumberFormat = NumberFormat.getInstance(locale);
        _testFormat();
    
        logln("Currency test " + localeName);
        fNumberFormat = NumberFormat.getCurrencyInstance(locale);
        _testFormat();
    
        logln("Percent test " + localeName);
        fNumberFormat = NumberFormat.getPercentInstance(locale);
        _testFormat();
    }
    
    /**
     * call tryIt with many variations, called by testLocale
     */
    private void _testFormat() {
        
        if (fNumberFormat == null){
            errln("**** FAIL: Null format returned by createXxxInstance.");
             return;
        }
        DecimalFormat s = (DecimalFormat)fNumberFormat;
        logln("pattern :" + s.toPattern());
    
        tryIt(-2.02147304840132e-68);
        tryIt(3.88057859588817e-68); 
        tryIt(-2.64651110485945e+65);
        tryIt(9.29526819488338e+64);
    
        tryIt(-2.02147304840132e-100);
        tryIt(3.88057859588817e-096); 
        tryIt(-2.64651110485945e+306);
        tryIt(9.29526819488338e+250); 
    
        tryIt(-9.18228054496402e+64);
        tryIt(-9.69413034454191e+64);
    
        tryIt(-9.18228054496402e+255);
        tryIt(-9.69413034454191e+273);
    
    
        tryIt(1.234e-200);
        tryIt(-2.3e-168);
    
        tryIt(Double.NaN);
        tryIt(Double.POSITIVE_INFINITY);
        tryIt(Double.NEGATIVE_INFINITY);

        tryIt(251887531);
        tryIt(5e-20 / 9);
        tryIt(5e20 / 9);
        tryIt(1.234e-50);
        tryIt(9.99999999999996);
        tryIt(9.999999999999996);

        tryIt(5.06e-27);

        tryIt(Integer.MIN_VALUE);
        tryIt(Integer.MAX_VALUE);
        tryIt((double)Integer.MIN_VALUE);
        tryIt((double)Integer.MAX_VALUE);
        tryIt((double)Integer.MIN_VALUE - 1.0);
        tryIt((double)Integer.MAX_VALUE + 1.0);
    
        tryIt(5.0 / 9.0 * 1e-20);
        tryIt(4.0 / 9.0 * 1e-20);
        tryIt(5.0 / 9.0 * 1e+20);
        tryIt(4.0 / 9.0 * 1e+20);
    
        tryIt(2147483647.);
        tryIt(0);
        tryIt(0.0);
        tryIt(1);
        tryIt(10);
        tryIt(100);
        tryIt(-1);
        tryIt(-10);
        tryIt(-100);
        tryIt(-1913860352);
    
        Random random = createRandom(); // use test framework's random seed
        for (int j = 0; j < 10; j++) {
            double d = random.nextDouble()*2e10 - 1e10;
            tryIt(d);
            
        }
    }
    
    /**
     * Perform tests using aNumber and fNumberFormat, called in many variations
     */
    public void tryIt(double aNumber) {    
        final int DEPTH = 10;
        double[] number = new double[DEPTH];
        String[] string = new String[DEPTH];
        int numberMatch = 0;
        int stringMatch = 0;
        boolean dump = false;
        int i;
    
        for (i = 0; i < DEPTH; i++) {
            if (i == 0) {
                number[i] = aNumber;
            } else {
                try {
                    number[i - 1] = fNumberFormat.parse(string[i - 1]).doubleValue();
                } catch(java.text.ParseException pe) {
                    errln("**** FAIL: Parse of " + string[i-1] + " failed.");
                    dump = true;
                    break;
                }
            }
    
            string[i] = fNumberFormat.format(number[i]);
            if (i > 0)
            {
                if (numberMatch == 0 && number[i] == number[i-1])
                    numberMatch = i;
                else if (numberMatch > 0 && number[i] != number[i-1])
                {
                    errln("**** FAIL: Numeric mismatch after match.");
                    dump = true;
                    break;
                }
                if (stringMatch == 0 && string[i] == string[i-1])
                    stringMatch = i;
                else if (stringMatch > 0 && string[i] != string[i-1])
                {
                    errln("**** FAIL: String mismatch after match.");
                    dump = true;
                    break;
                }
            }
            if (numberMatch > 0 && stringMatch > 0)
                break;
    
            if (i == DEPTH)
            --i;
    
        if (stringMatch > 2 || numberMatch > 2)
        {
            errln("**** FAIL: No string and/or number match within 2 iterations.");
            dump = true;
        }
    
        if (dump)
        {
            for (int k=0; k<=i; ++k)
            {
                logln(k + ": " + number[k] + " F> " +
                      string[k] + " P> ");
            }
        }
        }
    }
    
    /**
     *  perform tests using aNumber and fNumberFormat, called in many variations
     **/
    public void tryIt(int aNumber) {
        long number;
        
        String stringNum = fNumberFormat.format(aNumber);
        try {
            number = fNumberFormat.parse(stringNum).longValue();
        } catch (java.text.ParseException pe) {
            errln("**** FAIL: Parse of " + stringNum + " failed.");
            return;
        }
    
        if (number != aNumber) {
            errln("**** FAIL: Parse of " + stringNum + " failed. Got:" + number
                + " Expected:" + aNumber);
        }
        
    }
    
    /**
     *  test NumberFormat::getAvailableLocales
     **/
    @Test
    public void TestAvailableLocales() {
        final Locale[] locales = NumberFormat.getAvailableLocales();
        int count = locales.length;
        logln(count + " available locales");
        if (count != 0)
        {
            String all = "";
            for (int i = 0; i< count; ++i)
            {
                if (i!=0)
                    all += ", ";
                all += locales[i].getDisplayName();
            }
            logln(all);
        }
        else
            errln("**** FAIL: Zero available locales or null array pointer");
    }
    
    /**
     *  call testLocale for all locales
     **/    
    @Test
    public void TestMonster() {
        final String SEP = "============================================================\n";
        int count;
        final Locale[] allLocales = NumberFormat.getAvailableLocales();
        Locale[] locales = allLocales;
        count = locales.length;
        if (count != 0)
        {
            if (TestFmwk.getExhaustiveness() < 10 && count > 6) {
                count = 6;
                locales = new Locale[6];
                locales[0] = allLocales[0];
                locales[1] = allLocales[1];
                locales[2] = allLocales[2];
                // In a quick test, make sure we test locales that use
                // currency prefix, currency suffix, and choice currency
                // logic.  Otherwise bugs in these areas can slip through.
                locales[3] = new Locale("ar", "AE", "");
                locales[4] = new Locale("cs", "CZ", "");
                locales[5] = new Locale("en", "IN", "");
            }
            for (int i=0; i<count; ++i)
            {
                logln(SEP);
                _testLocale(locales[i]);
            }
        }
    
        logln(SEP);
    }
}
