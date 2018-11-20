/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 ******************************************************************************
 * Copyright (C) 2007-2015, International Business Machines Corporation and
 * others. All Rights Reserved.
 ******************************************************************************
 */

// Copyright 2006 Google Inc.  All Rights Reserved.

package android.icu.dev.test.duration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.duration.BasicPeriodFormatterService;
import android.icu.impl.duration.Period;
import android.icu.impl.duration.PeriodBuilder;
import android.icu.impl.duration.PeriodBuilderFactory;
import android.icu.impl.duration.PeriodFormatter;
import android.icu.impl.duration.PeriodFormatterFactory;
import android.icu.impl.duration.TimeUnit;
import android.icu.impl.duration.TimeUnitConstants;
import android.icu.impl.duration.impl.DataRecord.ECountVariant;
import android.icu.impl.duration.impl.DataRecord.EUnitVariant;

/**
 * Test cases for en
 */
public abstract class LanguageTestFmwk extends TestFmwk implements TimeUnitConstants {

    private static final TimeUnit[] units = {
        TimeUnit.YEAR, TimeUnit.MONTH, TimeUnit.WEEK, TimeUnit.DAY, TimeUnit.HOUR, 
        TimeUnit.MINUTE, TimeUnit.SECOND, TimeUnit.MILLISECOND
    };

    protected boolean inheritTargets() {
        return true;
    }

    private static final BasicPeriodFormatterService pfs = BasicPeriodFormatterService
            .getInstance();

    private TestData data;
    private String locale;

    //private DurationFormatterFactory dfFactory;
    private PeriodFormatterFactory pfFactory;
    private PeriodBuilderFactory pbFactory;

    private PrintWriter pw;

    private static final Map datacache = new HashMap(); // String->TestData

    private static final long[] approxDurations = {
        36525L*24*60*60*10, 3045*24*60*60*10L, 7*24*60*60*1000L, 24*60*60*1000L, 
        60*60*1000L, 60*1000L, 1000L, 1L
    };

    private static long approximateDuration(TimeUnit unit) {
        return approxDurations[unit.ordinal()];
    }

    private static TestData getTestData(String locale) {
        // debug
        if (locale.equals("testFullPluralizedForms")) {
            Thread.dumpStack();
        }
        TestData data = (TestData) datacache.get(locale);
        if (data == null) {
            try {
                InputStream is = LanguageTestFmwk.class
                        .getResourceAsStream("testdata/testdata_" + locale
                                + ".txt");
                // debug
                if (is == null) {
                    System.out.println("test data for locale '" + locale
                            + "' is null");
                }
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                try {
                    data = new FileTestData(isr);
                } finally {
                    isr.close();
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
                // swallow any exception
            }
        }
        return data;
    }

    public LanguageTestFmwk(String locale, boolean ignore) {
        this(getTestData(locale), locale);
    }

    private LanguageTestFmwk(TestData data, String locale) {
        if (data == null) {
            data = DefaultData.getInstance();
        }
        this.data = data;
        this.locale = locale;
    }

//    public static void writeData(PrintWriter pw, String locale)
//            throws Exception {
//        LanguageTestRoot test = new LanguageTestRoot(DefaultData.getInstance(),
//                locale);
//        test.writeData(pw);
//    }

//    private void writeData(PrintWriter writer) throws Exception {
///*
//      pw = writer;
//      setUp();
//      testFullPluralizedForms();
//      tearDown();
//      setUp();
//      testMediumForms();
//      tearDown();
//      setUp();
//      testShortForms();
//      tearDown();
//      setUp();
//      testCustomMinutes();
//      tearDown();
//      setUp();
//      testLimitedUnits();
//      tearDown();
//      setUp();
//      testHalfUnits();
//      tearDown();
//      setUp();
//      testFractionalUnits();
//      tearDown();
//      setUp();
//      testMultipleUnits();
//      tearDown();
//      pw = null;
//      writer.flush();
//*/
//    }

    protected void xAssertEquals(String msg, String[] expected, int n,
            String actual) {
        if (pw != null) {
            pw.println(actual);
        } else {
            // java doesn't dump enough context to be useful, so do it myself
            if (actual == null) {
                assertEquals(msg, expected[n], actual);
            } else {
                if (!actual.equals(expected[n])) {
                    fail("\n(!!"
                            + msg
                            + ") "
                            + asciify("expected '" + expected[n]
                                    + "' but got '" + actual + "'"));
                }
            }
        }
    }

    protected static String timestring(Period ts) {
        StringBuffer buf = new StringBuffer();
        if (ts.isMoreThan()) {
            buf.append("mt");
        } else if (ts.isLessThan()) {
            buf.append("lt");
        }
        for (int i = 0; i < units.length; ++i) {
            TimeUnit p = units[i];
            if (ts.isSet(p)) {
                buf.append(Float.toString(ts.getCount(p)));
                buf.append(p.toString().charAt(0));
            }
        }
        buf.append(ts.isInPast() ? "p" : "f");
        return buf.toString();
    }

    protected static String asciify(String s) {
        StringBuffer sb = null;
        for (int i = 0, e = s.length(); i < e; ++i) {
            char c = s.charAt(i);
            if (c < 0x20 || c > 0x7e) {
                if (sb == null) {
                    sb = new StringBuffer();
                    sb.append(s.substring(0, i));
                }
                sb.append("\\u");
                if (c < 0x10) {
                    sb.append("000");
                } else if (c < 0x100) {
                    sb.append("00");
                } else if (c < 0x1000) {
                    sb.append("0");
                }
                sb.append(Integer.toHexString(c));
            } else {
                if (sb != null) {
                    sb.append(c);
                }
            }
        }
        if (sb != null) {
            System.out.println("asciify '" + s + "' --> '" + sb.toString()
                    + "'");
        }
        return sb == null ? s : sb.toString();
    }

    private void xAssertEquals(String[] expected, int n, String actual) {
        xAssertEquals(null, expected, n, actual);
    }

    protected void setUp() throws Exception {
        pfFactory = pfs.newPeriodFormatterFactory().setLocale(locale);
        pbFactory = pfs.newPeriodBuilderFactory().setLocale(locale);
    }

    @Test
    public void testFullPluralizedForms() throws Exception {
        setUp();
        int[] counts = data.getFullPluralizedFormCounts();
        String[] targets = data.getFullPluralizedFormTargets();
        if (pw != null) {
            pw.println("=fullPluralizedFormCounts");
            for (int i = 0; i < counts.length; ++i) {
                int c = counts[i];
                pw.println(String.valueOf(c));
            }
            pw.println("=fullPluralizedFormTargets");
        }

        int n = 0;
        PeriodFormatter pf = pfFactory.getFormatter();
        for (int i = 0; i < units.length; ++i) {
            TimeUnit u = units[i];
            // System.err.print("\nunit: " + u);
            PeriodBuilder pb = pbFactory.getFixedUnitBuilder(u);
            for (int j = 0; j < counts.length; ++j) {
                int c = counts[j];
                // System.err.println("\ncount[" + j + "]: " + c);
                Period p = pb.create(approximateDuration(u) * c);
                String string = pf.format(p);
                xAssertEquals(u.toString() + c, targets, n++, string);
            }
        }
    }

    @Test
    public void testMediumForms() throws Exception {
        setUp();
        String[] targets = data.getMediumFormTargets();

        if (pw != null) {
            pw.println("=mediumFormTargets");
        }

        pfFactory.setUnitVariant(EUnitVariant.MEDIUM);
        pfFactory.setDisplayPastFuture(false);
        PeriodFormatter pf = pfFactory.getFormatter();
        int n = 0;
        for (int i = 0; i < units.length; ++i) {
            TimeUnit u = units[i];
            PeriodBuilder pb = pbFactory.getFixedUnitBuilder(u);
            Period p = pb.create(approximateDuration(u) * 3);
            String string = pf.format(p);
            xAssertEquals(u.toString(), targets, n++, string);
        }
    }

    @Test
    public void testShortForms() throws Exception {
        setUp();
        String[] targets = data.getShortFormTargets();

        if (pw != null) {
            pw.println("=shortFormTargets");
        }

        pfFactory.setUnitVariant(EUnitVariant.SHORT);
        pfFactory.setDisplayPastFuture(false);
        PeriodFormatter pf = pfFactory.getFormatter();
        int n = 0;
        for (int i = 0; i < units.length; ++i) {
            TimeUnit u = units[i];
            PeriodBuilder pb = pbFactory.getFixedUnitBuilder(u);
            Period p = pb.create(approximateDuration(u) * 3);
            String string = pf.format(p);
            xAssertEquals(u.toString(), targets, n++, string);
        }
    }

    @Test
    public void testCustomMinutes() throws Exception {
        setUp();
        String[] targets = data.getCustomMinuteTargets();

        if (pw != null) {
            pw.println("=customMinuteTargets");
        }

        pfFactory.setCountVariant(ECountVariant.INTEGER_CUSTOM);
        pfFactory.setDisplayPastFuture(false);
        PeriodFormatter pf = pfFactory.getFormatter();

        Period p = Period.at(1, HOUR);
        int n = 0;
        for (int i = 1; i < 12; ++i) {
            p = p.and(i * 5, MINUTE).omit(HOUR);
            xAssertEquals(targets, n++, pf.format(p));
            p = p.and(1, HOUR);
            xAssertEquals(targets, n++, pf.format(p));
        }
    }

    @Test
    public void testLimitedUnits() throws Exception {
        setUp();
        String[] targets = data.getLimitedUnitTargets();

        if (pw != null) {
            pw.println("=limitedPeriodTargets");
        }

        Period p = Period.at(1, MONTH);
        int n = 0;
        for (int i = 0; i < 3; ++i) {
            switch (i) {
            case 0:
                p = p.at();
                break;
            case 1:
                p = p.lessThan();
                break;
            case 2:
                p = p.moreThan();
                break;
            }
            for (int j = 0; j < 3; ++j) {
                pfFactory.setDisplayPastFuture(true);
                switch (j) {
                case 0:
                    pfFactory.setDisplayPastFuture(false);
                    break;
                case 1:
                    p = p.inPast();
                    break;
                case 2:
                    p = p.inFuture();
                    break;
                }

                PeriodFormatter pf = pfFactory.getFormatter();

                p = p.omit(WEEK).omit(DAY);
                xAssertEquals(targets, n++, pf.format(p));

                p = p.and(2, WEEK);
                xAssertEquals(targets, n++, pf.format(p));

                p = p.and(3, DAY);
                xAssertEquals(targets, n++, pf.format(p));
            }
        }

        p = p.omit(MONTH).omit(WEEK).omit(DAY).and(1, HOUR);
        for (int i = 0; i < 3; ++i) {
            switch (i) {
            case 0:
                p = p.at();
                break;
            case 1:
                p = p.lessThan();
                break;
            case 2:
                p = p.moreThan();
                break;
            }
            for (int j = 0; j < 3; ++j) {
                pfFactory.setDisplayPastFuture(true);
                switch (j) {
                case 0:
                    pfFactory.setDisplayPastFuture(false);
                    break;
                case 1:
                    p = p.inPast();
                    break;
                case 2:
                    p = p.inFuture();
                    break;
                }

                PeriodFormatter pf = pfFactory.getFormatter();

                p = p.omit(MINUTE).omit(SECOND);
                xAssertEquals(targets, n++, pf.format(p));

                p = p.and(2, MINUTE);
                xAssertEquals(targets, n++, pf.format(p));

                p = p.and(3, SECOND);
                xAssertEquals(targets, n++, pf.format(p));
            }
        }
    }

    @Test
    public void testHalfUnits() throws Exception {
        setUp();
        int[] counts = data.getHalfUnitCounts();
        String[] targets = data.getHalfUnitTargets();

        if (pw != null) {
            pw.println("=halfPeriodCounts");
            for (int i = 0; i < counts.length; ++i) {
                int c = counts[i];
                pw.println(String.valueOf(c));
            }
            pw.println("=halfPeriodTargets");
        }

        pfFactory.setCountVariant(ECountVariant.HALF_FRACTION);
        pfFactory.setDisplayPastFuture(false);
        PeriodFormatter pf = pfFactory.getFormatter();

        int n = 0;
        for (int i = 0; i < units.length; ++i) {
            TimeUnit u = units[i];
            for (int j = 0; j < counts.length; ++j) {
                int c = counts[j];
                Period p = Period.at(c + .5f, u);
                String string = pf.format(p);
                xAssertEquals(u.toString(), targets, n++, string);
            }
        }
    }

    @Test
    public void testFractionalUnits() throws Exception {
        setUp();
        float[] counts = data.getFractionalUnitCounts();
        String[] targets = data.getFractionalUnitTargets();

        if (pw != null) {
            pw.println("=fractionalPeriodCounts");
            for (int i = 0; i < counts.length; ++i) {
                float c = counts[i];
                pw.println(String.valueOf(c));
            }
            pw.println("=fractionalPeriodTargets");
        }

        pfFactory.setCountVariant(ECountVariant.DECIMAL2);
        pfFactory.setDisplayPastFuture(false);
        PeriodFormatter pf = pfFactory.getFormatter();

        int n = 0;
        for (int i = 0; i < units.length; ++i) {
            TimeUnit u = units[i];
            for (int j = 0; j < counts.length; ++j) {
                float c = counts[j];
                Period p = Period.at(c, u);
                String string = pf.format(p);
                xAssertEquals(u.toString(), targets, n++, string);
            }
        }
    }

    @Test
    public void testMultipleUnits() throws Exception {
        setUp();
        String[] targets = data.getMultipleUnitTargets();

        if (pw != null) {
            pw.println("=multiplePeriodTargets");
        }

        pfFactory.setCountVariant(ECountVariant.INTEGER);
        pfFactory.setDisplayPastFuture(false);
        PeriodFormatter pf = pfFactory.getFormatter();

        int n = 0;
        for (int i = 0; i < units.length - 1; ++i) {
            Period p = Period.at(1, units[i]).and(2, units[i + 1]);
            xAssertEquals(targets, n++, pf.format(p));
            if (i < units.length - 2) {
                p = Period.at(1, units[i]).and(3, units[i + 2]);
                xAssertEquals(targets, n++, pf.format(p));
                p = Period.at(1, units[i]).and(2, units[i + 1]).and(3,
                        units[i + 2]);
                xAssertEquals(targets, n++, pf.format(p));
            }
        }
    }

    public static abstract class TestData {
        abstract int[] getFullPluralizedFormCounts();
        abstract String[] getFullPluralizedFormTargets();
        abstract String[] getMediumFormTargets();
        abstract String[] getShortFormTargets();
        abstract String[] getCustomMinuteTargets();
        abstract String[] getLimitedUnitTargets();
        abstract int[] getHalfUnitCounts();
        abstract String[] getHalfUnitTargets();
        abstract float[] getFractionalUnitCounts();
        abstract String[] getFractionalUnitTargets();
        abstract String[] getMultipleUnitTargets();
    }

}

class FileTestData extends LanguageTestFmwk.TestData {
    private int[] fullPluralizedFormCounts;
    private String[] fullPluralizedFormTargets;
    private String[] mediumFormTargets;
    private String[] shortFormTargets;
    private String[] customMinuteTargets;
    private String[] limitedUnitTargets;
    private int[] halfUnitCounts;
    private String[] halfUnitTargets;
    private float[] fractionalUnitCounts;
    private String[] fractionalUnitTargets;
    private String[] multipleUnitTargets;

    int[] getFullPluralizedFormCounts() {
        return fullPluralizedFormCounts;
    }

    String[] getFullPluralizedFormTargets() {
        return fullPluralizedFormTargets;
    }

    String[] getMediumFormTargets() {
        return mediumFormTargets;
    }

    String[] getShortFormTargets() {
        return shortFormTargets;
    }

    String[] getCustomMinuteTargets() {
        return customMinuteTargets;
    }

    String[] getLimitedUnitTargets() {
        return limitedUnitTargets;
    }

    int[] getHalfUnitCounts() {
        return halfUnitCounts;
    }

    String[] getHalfUnitTargets() {
        return halfUnitTargets;
    }

    float[] getFractionalUnitCounts() {
        return fractionalUnitCounts;
    }

    String[] getFractionalUnitTargets() {
        return fractionalUnitTargets;
    }

    String[] getMultipleUnitTargets() {
        return multipleUnitTargets;
    }

    public FileTestData(InputStreamReader isr) throws Exception {
        BufferedReader br = new BufferedReader(isr);

        class Wrapup {
            int[] intArray;

            float[] floatArray;

            String[] stringArray;

            void wrapup(List /* of String */list, Element element) {
                if (list == null)
                    return;

                switch (element.mode) {
                case EMode.mString:
                    stringArray = (String[]) list.toArray(new String[list
                            .size()]);
                    break;

                case EMode.mInt:
                    intArray = new int[list.size()];
                    for (int i = 0, e = intArray.length; i < e; ++i) {
                        intArray[i] = Integer.parseInt((String) list.get(i));
                    }
                    break;

                case EMode.mFloat:
                    floatArray = new float[list.size()];
                    for (int i = 0, e = floatArray.length; i < e; ++i) {
                        floatArray[i] = Float.parseFloat((String) list.get(i));
                    }
                    break;
                }

                switch (element.which) {
                case Element.XfullPluralizedFormCounts:
                    FileTestData.this.fullPluralizedFormCounts = intArray;
                    break;
                case Element.XfullPluralizedFormTargets:
                    FileTestData.this.fullPluralizedFormTargets = stringArray;
                    break;
                case Element.XmediumFormTargets:
                    FileTestData.this.mediumFormTargets = stringArray;
                    break;
                case Element.XshortFormTargets:
                    FileTestData.this.shortFormTargets = stringArray;
                    break;
                case Element.XcustomMinuteTargets:
                    FileTestData.this.customMinuteTargets = stringArray;
                    break;
                case Element.XlimitedUnitTargets:
                    FileTestData.this.limitedUnitTargets = stringArray;
                    break;
                case Element.XhalfUnitCounts:
                    FileTestData.this.halfUnitCounts = intArray;
                    break;
                case Element.XhalfUnitTargets:
                    FileTestData.this.halfUnitTargets = stringArray;
                    break;
                case Element.XfractionalUnitCounts:
                    FileTestData.this.fractionalUnitCounts = floatArray;
                    break;
                case Element.XfractionalUnitTargets:
                    FileTestData.this.fractionalUnitTargets = stringArray;
                    break;
                case Element.XmultipleUnitTargets:
                    FileTestData.this.multipleUnitTargets = stringArray;
                    break;
                }
            }
        }
        Wrapup w = new Wrapup();

        List /* of String */list = null;
        Element element = null;
        String line = null;
        while (null != (line = br.readLine())) {
            line = line.trim();
            if (line.length() == 0 || line.charAt(0) == '#')
                continue;
            if (line.charAt(0) == '=') {
                w.wrapup(list, element);

                list = new ArrayList();
                element = Element.forString(line.substring(1));
            } else if (line.equals("null")) {
                list.add(null);
            } else {
                list.add(line);
            }
        }
        w.wrapup(list, element);
    }
}

class DefaultData extends LanguageTestFmwk.TestData {
    private static final int[] fullPluralizedFormCounts = { -3, -2, -1, 0, 1,
            2, 3, 5, 10, 11, 12, 20, 21, 22, 23, 25 };

    private static final int[] halfUnitCounts = { 0, 1, 2, 5, 10, 11, 12, 20,
            21, 22 };

    private static final float[] fractionalUnitCounts = { 0.025f, 1.0f, 1.205f,
            2.125f, 12.05f };

    private static final DefaultData instance = new DefaultData();

    private DefaultData() {
    }

    public static DefaultData getInstance() {
        return instance;
    }

    int[] getFullPluralizedFormCounts() {
        return fullPluralizedFormCounts;
    }

    String[] getFullPluralizedFormTargets() {
        return null;
    }

    String[] getMediumFormTargets() {
        return null;
    }

    String[] getShortFormTargets() {
        return null;
    }

    String[] getCustomMinuteTargets() {
        return null;
    }

    String[] getLimitedUnitTargets() {
        return null;
    }

    int[] getHalfUnitCounts() {
        return halfUnitCounts;
    }

    String[] getHalfUnitTargets() {
        return null;
    }

    float[] getFractionalUnitCounts() {
        return fractionalUnitCounts;
    }

    String[] getFractionalUnitTargets() {
        return null;
    }

    String[] getMultipleUnitTargets() {
        return null;
    }
}

class EMode {
    static final int mString = 0;
    static final int mInt = 1;
    static final int mFloat = 2;
}

class Element {
    final String name;
    final int mode;
    final int which;

    static int counter = 0;
    static Element[] list = new Element[11];

    Element(String name) {
        this.name = name;
        mode = EMode.mString;
        this.which = counter++;
        list[this.which] = this;
    }

    Element(String name, int mode) {
        this.name = name;
        this.mode = mode;
        this.which = counter++;
        list[this.which] = this;
    }

    static final int XfullPluralizedFormCounts = 0;
    static final int XfullPluralizedFormTargets = 1;
    static final int XmediumFormTargets = 2;
    static final int XshortFormTargets = 3;
    static final int XcustomMinuteTargets = 4;
    static final int XlimitedUnitTargets = 5;
    static final int XhalfUnitCounts = 6;
    static final int XhalfUnitTargets = 7;
    static final int XfractionalUnitCounts = 8;
    static final int XfractionalUnitTargets = 9;
    static final int XmultipleUnitTargets = 10;

    static final Element fullPluralizedFormCounts = new Element(
            "fullPluralizedFormCounts", EMode.mInt);

    static final Element fullPluralizedFormTargets = new Element(
            "fullPluralizedFormTargets");

    static final Element mediumFormTargets = new Element("mediumFormTargets");

    static final Element shortFormTargets = new Element("shortFormTargets");

    static final Element customMinuteTargets = new Element(
            "customMinuteTargets");

    static final Element limitedUnitTargets = new Element("limitedUnitTargets");

    static final Element halfUnitCounts = new Element("halfUnitCounts",
            EMode.mInt);

    static final Element halfUnitTargets = new Element("halfUnitTargets");

    static final Element fractionalUnitCounts = new Element(
            "fractionalUnitCounts", EMode.mFloat);

    static final Element fractionalUnitTargets = new Element(
            "fractionalUnitTargets");

    static final Element multipleUnitTargets = new Element(
            "multipleUnitTargets");

    static Element forString(String s) {
        for (int i = 0; i < list.length; ++i) {
            if (list[i].name.equals(s)) {
                return list[i];
            }
        }
        return null;
    }
}
