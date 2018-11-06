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
 * Port From:   ICU4C v1.8.1 : format : DateFormatRoundTripTest
 * Source File: $ICU4CRoot/source/test/intltest/dtfmtrtts.cpp
 **/

package android.icu.dev.test.format;

import java.text.FieldPosition;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import org.junit.Test;

import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.icu.util.TimeZone;

/**
 * Performs round-trip tests for DateFormat
 **/
public class DateFormatRoundTripTest extends android.icu.dev.test.TestFmwk {
    public boolean INFINITE = false;
    public boolean quick = true;
    private SimpleDateFormat dateFormat;
    private Calendar getFieldCal;
    private int SPARSENESS = 18;
    private int TRIALS = 4;
    private int DEPTH = 5;
    private Random ran;

    // TODO: test is randomly failing depending on the randomly generated date
    @Test
    public void TestDateFormatRoundTrip() {
        dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss.SSS zzz yyyy G");
        getFieldCal = Calendar.getInstance();
        ran = createRandom(); // use test framework's random seed

        final Locale[] avail = DateFormat.getAvailableLocales();
        int locCount = avail.length;
        logln("DateFormat available locales: " + locCount);
        if (quick) {
            if (locCount > 5)
                locCount = 5;
            logln("Quick mode: only testing first 5 Locales");
        }
        TimeZone tz = TimeZone.getDefault();
        logln("Default TimeZone:             " + tz.getID());

        if (INFINITE) {
            // Special infinite loop test mode for finding hard to reproduce errors
            Locale loc = Locale.getDefault();
            logln("ENTERING INFINITE TEST LOOP FOR Locale: " + loc.getDisplayName());
            for (;;) {
                _test(loc);
            }
        } else {
            _test(Locale.getDefault());
            for (int i = 0; i < locCount; ++i) {
                _test(avail[i]);
            }
        }
    }

    private String styleName(int s) {
        switch (s) {
            case DateFormat.SHORT :
                return "SHORT";
            case DateFormat.MEDIUM :
                return "MEDIUM";
            case DateFormat.LONG :
                return "LONG";
            case DateFormat.FULL :
                return "FULL";
            default :
                return "Unknown";
        }
    }

    private void _test(Locale loc) {
        if (!INFINITE) {
            logln("Locale: " + loc.getDisplayName());
        }
        // Total possibilities = 24
        //  4 date
        //  4 time
        //  16 date-time
        boolean[] TEST_TABLE = new boolean[24];
        int i = 0;
        for (i = 0; i < 24; ++i)
            TEST_TABLE[i] = true;

        // If we have some sparseness, implement it here.  Sparseness decreases
        // test time by eliminating some tests, up to 23.
        for (i = 0; i < SPARSENESS; i++) {
            int random = (int) (ran.nextDouble() * 24);
            if (random >= 0 && random < 24 && TEST_TABLE[i]) {
                TEST_TABLE[random] = false;
            }
        }

        int itable = 0;
        int style = 0;
        for (style = DateFormat.FULL; style <= DateFormat.SHORT; ++style) {
            if (TEST_TABLE[itable++]) {
                logln("Testing style " + styleName(style));
                DateFormat df = DateFormat.getDateInstance(style, loc);
                _test(df, false);
            }
        }

        for (style = DateFormat.FULL; style <= DateFormat.SHORT; ++style) {
            if (TEST_TABLE[itable++]) {
                logln("Testing style " + styleName(style));
                DateFormat  df = DateFormat.getTimeInstance(style, loc);
                _test(df, true);
            }
        }

        for (int dstyle = DateFormat.FULL; dstyle <= DateFormat.SHORT; ++dstyle) {
            for (int tstyle = DateFormat.FULL; tstyle <= DateFormat.SHORT; ++tstyle) {
                if (TEST_TABLE[itable++]) {
                    logln("Testing dstyle " + styleName(dstyle) + ", tstyle " + styleName(tstyle));
                    DateFormat df = DateFormat.getDateTimeInstance(dstyle, tstyle, loc);
                    _test(df, false);
                }
            }
        }
    }

    private void _test(DateFormat fmt, boolean timeOnly) {

        if (!(fmt instanceof SimpleDateFormat)) {
            errln("DateFormat wasn't a SimpleDateFormat");
            return;
        }

        String pat = ((SimpleDateFormat) fmt).toPattern();
        logln(pat);

        // NOTE TO MAINTAINER
        // This indexOf check into the pattern needs to be refined to ignore
        // quoted characters.  Currently, this isn't a problem with the locale
        // patterns we have, but it may be a problem later.

        boolean hasEra = (pat.indexOf("G") != -1);
        boolean hasZoneDisplayName = (pat.indexOf("z") != -1) || (pat.indexOf("v") != -1) || (pat.indexOf("V") != -1);
        boolean hasTwoDigitYear = pat.indexOf("yy") >= 0 && pat.indexOf("yyy") < 0;

        // Because patterns contain incomplete data representing the Date,
        // we must be careful of how we do the roundtrip.  We start with
        // a randomly generated Date because they're easier to generate.
        // From this we get a string.  The string is our real starting point,
        // because this string should parse the same way all the time.  Note
        // that it will not necessarily parse back to the original date because
        // of incompleteness in patterns.  For example, a time-only pattern won't
        // parse back to the same date.

        try {
            for (int i = 0; i < TRIALS; ++i) {
                Date[] d = new Date[DEPTH];
                String[] s = new String[DEPTH];

                d[0] = generateDate();

                // We go through this loop until we achieve a match or until
                // the maximum loop count is reached.  We record the points at
                // which the date and the string starts to match.  Once matching
                // starts, it should continue.
                int loop;
                int dmatch = 0; // d[dmatch].getTime() == d[dmatch-1].getTime()
                int smatch = 0; // s[smatch].equals(s[smatch-1])
                for (loop = 0; loop < DEPTH; ++loop) {
                    if (loop > 0) {
                        d[loop] = fmt.parse(s[loop - 1]);
                    }

                    s[loop] = fmt.format(d[loop]);

                    if (loop > 0) {
                        if (smatch == 0) {
                            boolean match = s[loop].equals(s[loop - 1]);
                            if (smatch == 0) {
                                if (match)
                                    smatch = loop;
                            } else
                                if (!match)
                                    errln("FAIL: String mismatch after match");
                        }

                        if (dmatch == 0) {
                            // {sfb} watch out here, this might not work
                            boolean match = d[loop].getTime() == d[loop - 1].getTime();
                            if (dmatch == 0) {
                                if (match)
                                    dmatch = loop;
                            } else
                                if (!match)
                                    errln("FAIL: Date mismatch after match");
                        }

                        if (smatch != 0 && dmatch != 0)
                            break;
                    }
                }
                // At this point loop == DEPTH if we've failed, otherwise loop is the
                // max(smatch, dmatch), that is, the index at which we have string and
                // date matching.

                // Date usually matches in 2.  Exceptions handled below.
                int maxDmatch = 2;
                int maxSmatch = 1;
                if (dmatch > maxDmatch || smatch > maxSmatch) {
                    //If the Date is BC
                    if (!timeOnly && !hasEra && getField(d[0], Calendar.ERA) == GregorianCalendar.BC) {
                        maxDmatch = 3;
                        maxSmatch = 2;
                    }
                    if (hasZoneDisplayName &&
                            (fmt.getTimeZone().inDaylightTime(d[0])
                                    || fmt.getTimeZone().inDaylightTime(d[1])
                                    || d[0].getTime() < 0L /* before 1970 */
                                    || hasTwoDigitYear && d[1].getTime() < 0L
                                       /* before 1970 as the result of 2-digit year parse */)) {
                        maxSmatch = 2;
                        if (timeOnly) {
                            maxDmatch = 3;
                        }
                    }
                }

                if (dmatch > maxDmatch || smatch > maxSmatch) {
                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy HH:mm:ss, z G", Locale.US);
                    logln("Date = " + sdf.format(d[0]) + "; ms = " + d[0].getTime());
                    logln("Dmatch: " + dmatch + " maxD: " + maxDmatch + " Smatch:" + smatch + " maxS:" + maxSmatch);
                    for (int j = 0; j <= loop && j < DEPTH; ++j) {
                        StringBuffer temp = new StringBuffer("");
                        FieldPosition pos = new FieldPosition(0);
                        logln((j > 0 ? " P> " : "    ") + dateFormat.format(d[j], temp, pos)
                            + " F> " + s[j] + (j > 0 && d[j].getTime() == d[j - 1].getTime() ? " d==" : "")
                            + (j > 0 && s[j].equals(s[j - 1]) ? " s==" : ""));
                    }
                    errln("Pattern: " + pat + " failed to match" + "; ms = " + d[0].getTime());
                }
            }
        } catch (ParseException e) {
            errln("Exception: " + e.getMessage());
            logln(e.toString());
        }
    }

    private int getField(Date d, int f) {
        getFieldCal.setTime(d);
        int ret = getFieldCal.get(f);
        return ret;
    }

    private Date generateDate() {
        double a = ran.nextDouble();
        // Now 'a' ranges from 0..1; scale it to range from 0 to 8000 years
        a *= 8000;
        // Range from (4000-1970) BC to (8000-1970) AD
        a -= 4000;
        // Now scale up to ms
        a *= 365.25 * 24 * 60 * 60 * 1000;
        return new Date((long)a);
    }
}
