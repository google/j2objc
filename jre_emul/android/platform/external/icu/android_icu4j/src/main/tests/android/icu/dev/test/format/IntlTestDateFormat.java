/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/***************************************************************************************
 *
 *   Copyright (C) 1996-2010, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 */

/** 
 * Port From:   JDK 1.4b1 : java.text.Format.IntlTestDateFormat
 * Source File: java/text/format/IntlTestDateFormat.java
 **/

/*
    @test 1.4 98/03/06
    @summary test International Date Format
*/

package android.icu.dev.test.format;

import java.text.FieldPosition;
import java.text.ParseException;
import java.util.Date;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.ULocale;

public class IntlTestDateFormat extends android.icu.dev.test.TestFmwk {
    // Values in milliseconds (== Date)
    private static final long ONESECOND = 1000;
    private static final long ONEMINUTE = 60 * ONESECOND;
    private static final long ONEHOUR = 60 * ONEMINUTE;
    private static final long ONEDAY = 24 * ONEHOUR;
    //private static final double ONEYEAR = 365.25 * ONEDAY; // Approximate //The variable is never used

    // EModes
    //private static final byte GENERIC = 0;
    //private static final byte TIME = GENERIC + 1; //The variable is never used
    //private static final byte DATE = TIME + 1; //The variable is never used
    //private static final byte DATE_TIME = DATE + 1; //The variable is never used

    private  DateFormat fFormat = null;
    private static String fTestName = new String("getInstance");
    private static int fLimit = 3; // How many iterations it should take to reach convergence
    private Random random; // initialized in randDouble

    public IntlTestDateFormat() {
        //Constructure
    }
    
    @Before
    public void init() throws Exception {
        fFormat = DateFormat.getInstance();
    }
    
    @Test
    public void TestULocale() {
        localeTest(ULocale.getDefault(), "Default Locale");
    }

    // This test does round-trip testing (format -> parse -> format -> parse -> etc.) of DateFormat.
    private void localeTest(final ULocale locale, final String localeName) {
        int timeStyle, dateStyle;

        // For patterns including only time information and a timezone, it may take
        // up to three iterations, since the timezone may shift as the year number
        // is determined.  For other patterns, 2 iterations should suffice.
        fLimit = 3;

        for(timeStyle = 0; timeStyle < 4; timeStyle++) {
            fTestName = new String("Time test " + timeStyle + " (" + localeName + ")");
            try {
                fFormat = DateFormat.getTimeInstance(timeStyle, locale);
            }
            catch(StringIndexOutOfBoundsException e) {
                errln("FAIL: localeTest time getTimeInstance exception");
                throw e;
            }
            TestFormat();
        }

        fLimit = 2;

        for(dateStyle = 0; dateStyle < 4; dateStyle++) {
            fTestName = new String("Date test " + dateStyle + " (" + localeName + ")");
            try {
                fFormat = DateFormat.getDateInstance(dateStyle, locale);
            }
            catch(StringIndexOutOfBoundsException e) {
                errln("FAIL: localeTest date getTimeInstance exception");
                throw e;
            }
            TestFormat();
        }

        for(dateStyle = 0; dateStyle < 4; dateStyle++) {
            for(timeStyle = 0; timeStyle < 4; timeStyle++) {
                fTestName = new String("DateTime test " + dateStyle + "/" + timeStyle + " (" + localeName + ")");
                try {
                    fFormat = DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
                }
                catch(StringIndexOutOfBoundsException e) {
                    errln("FAIL: localeTest date/time getDateTimeInstance exception");
                    throw e;
                }
                TestFormat();
            }
        }
    }

    @Test
    public void TestFormat() {
        if (fFormat == null) {
            errln("FAIL: DateFormat creation failed");
            return;
        }
        //        logln("TestFormat: " + fTestName);
        Date now = new Date();
        tryDate(new Date(0));
        tryDate(new Date((long) 1278161801778.0));
        tryDate(now);
        // Shift 6 months into the future, AT THE SAME TIME OF DAY.
        // This will test the DST handling.
        tryDate(new Date(now.getTime() + 6*30*ONEDAY));

        Date limit = new Date(now.getTime() * 10); // Arbitrary limit
        for (int i=0; i<2; ++i)
            //            tryDate(new Date(floor(randDouble() * limit)));
            tryDate(new Date((long) (randDouble() * limit.getTime())));
    }

    private void describeTest() {
        if (fFormat == null) {
            errln("FAIL: no DateFormat");
            return;
        }

        // Assume it's a SimpleDateFormat and get some info
        SimpleDateFormat s = (SimpleDateFormat) fFormat;
        logln(fTestName + " Pattern " + s.toPattern());
    }
    
    private void tryDate(Date theDate) {
        final int DEPTH = 10;
        Date[] date = new Date[DEPTH];
        StringBuffer[] string = new StringBuffer[DEPTH];

        int dateMatch = 0;
        int stringMatch = 0;
        boolean dump = false;
        int i;
        for (i=0; i<DEPTH; ++i) string[i] = new StringBuffer();
        for (i=0; i<DEPTH; ++i) {
            if (i == 0) date[i] = theDate;
            else {
                try {
                    date[i] = fFormat.parse(string[i-1].toString());
                }
                catch (ParseException e) {
                    describeTest();
                    errln("********** FAIL: Parse of " + string[i-1] + " failed for locale: "+fFormat.getLocale(ULocale.ACTUAL_LOCALE));
                    dump = true;
                    break;
                }
            }
            FieldPosition position = new FieldPosition(0);
            fFormat.format(date[i], string[i], position);
            if (i > 0) {
                if (dateMatch == 0 && date[i] == date[i-1]) dateMatch = i;
                else if (dateMatch > 0 && date[i] != date[i-1]) {
                    describeTest();
                    errln("********** FAIL: Date mismatch after match.");
                    dump = true;
                    break;
                }
                if (stringMatch == 0 && string[i] == string[i-1]) stringMatch = i;
                else if (stringMatch > 0 && string[i] != string[i-1]) {
                    describeTest();
                    errln("********** FAIL: String mismatch after match.");
                    dump = true;
                    break;
                }
            }
            if (dateMatch > 0 && stringMatch > 0) break;
        }
        if (i == DEPTH) --i;

        if (stringMatch > fLimit || dateMatch > fLimit) {
            describeTest();
            errln("********** FAIL: No string and/or date match within " + fLimit + " iterations.");
            dump = true;
        }

        if (dump) {
            for (int k=0; k<=i; ++k) {
                logln("" + k + ": " + date[k] + " F> " + string[k] + " P> ");
            }
        }
    }

    // Return a random double from 0.01 to 1, inclusive
    private double randDouble() {
    if (random == null) {
        random = createRandom();
    }
        // Assume 8-bit (or larger) rand values.  Also assume
        // that the system rand() function is very poor, which it always is.
        //        double d;
        //        int i;
        //        do {
        //            for (i=0; i < sizeof(double); ++i)
        //            {
        //                char poke = (char*)&d;
        //                poke[i] = (rand() & 0xFF);
        //            }
        //        } while (TPlatformUtilities.isNaN(d) || TPlatformUtilities.isInfinite(d));

        //        if (d < 0.0) d = -d;
        //        if (d > 0.0)
        //        {
        //            double e = floor(log10(d));
        //            if (e < -2.0) d *= pow(10.0, -e-2);
        //            else if (e > -1.0) d /= pow(10.0, e+1);
        //        }
        //        return d;
        return random.nextDouble();
    }

    @Test
    public void TestAvailableLocales() {
        final ULocale[] locales = DateFormat.getAvailableULocales();
        long count = locales.length;
        logln("" + count + " available locales");
        if (locales != null  &&  count != 0) {
            StringBuffer all = new StringBuffer();
            for (int i=0; i<count; ++i) {
                if (i!=0) all.append(", ");
                all.append(locales[i].getDisplayName());
            }
            logln(all.toString());
        }
        else errln("********** FAIL: Zero available locales or null array pointer");
    }

    @Test
    public void TestRoundtrip() {
        ULocale[] locales;
        if (isQuick()) {
            locales = new ULocale[] {
                    new ULocale("bg_BG"),
                    new ULocale("fr_CA"),
                    new ULocale("zh_TW"),
            };
        } else {
            locales = DateFormat.getAvailableULocales();
        }
        long count = locales.length;
        if (locales != null  &&  count != 0) {
            for (int i=0; i<count; ++i) {
                String name = locales[i].getDisplayName();
                logln("Testing " + name + "...");
                try {
                    localeTest(locales[i], name);
                }
                catch(Exception e) {
                    errln("FAIL: TestMonster localeTest exception" + e);
                }
            }
        }
    }
}

//eof
