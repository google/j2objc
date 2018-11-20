/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2000-2015, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
/* Generated from 'DiagBigDecimal.nrx' 27 Mar 2000 22:38:44 [v1.162] */
/* Options: Binary Comments Crossref Format Java Logo Trace1 Verbose3 */
/* The generated code has been manually modified. */
package android.icu.dev.test.bigdec;

import java.math.BigInteger;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.dev.test.TestUtil;
import android.icu.dev.test.TestUtil.JavaVendor;
import android.icu.math.BigDecimal;

/* ------------------------------------------------------------------ */
/* Decimal diagnostic tests mfc */
/* Copyright (c) IBM Corporation 1996-2010. All Rights Reserved. */
/* ------------------------------------------------------------------ */
/* DiagBigDecimal */
/*                                                                    */
/* A class that tests the BigDecimal and MathContext classes. */
/*                                                                    */
/* The tests here are derived from or cover the same paths as: */
/* -- ANSI X3-274 testcases */
/* -- Java JCK testcases */
/* -- NetRexx testcases */
/* -- VM/CMS S/370 REXX implementation testcases [1981+] */
/* -- IBM Vienna Laboratory Rexx compiler testcases [1988+] */
/* -- New testcases */
/*                                                                    */
/* The authoritative sources for how the underlying technology */
/* (arithmetic) should work are: */
/* -- for digits=0 (fixed point): java.math.BigDecimal */
/* -- for digits>0 (floating point): ANSI X3.274-1996 + errata */
/*                                                                    */
/* ------------------------------------------------------------------ */
/* Change list */
/* 1997.09.05 Initial implementation, from DiagRexx [NetRexx tests] */
/* 1998.05.02 0.07 changes (e.g., compareTo) */
/* 1998.06.06 Rounding modes and format additions */
/* 1998.06.25 Rename from DiagDecimal; make stand-alone [add */
/* DiagException as a Minor class] */
/* 1998.06.27 Start adding testcases for DIGITS=0/FORM=PLAIN cases */
/* Reorganize for faster trace compilation */
/* 1998.06.28 new: valueof, scale, movePointX, unscaledValue, etc. */
/* 1998.07.07 Scaled divide */
/* 1998.07.08 setScale */
/* 1998.07.15 new scaffolding (Minor Test class) -- see diagabs */
/* 1998.12.14 add toBigDecimal and BigDecimal(java.math.BigDecimal) */
/* 1999.02.04 number preparation rounds instead of digits+1 trunc */
/* 1999.02.09 format method now only has two signatures */
/* 1999.02.27 no longer use Rexx class or RexxIO class */
/* 1999.03.05 add MathContext tests */
/* 1999.03.05 update for 0.96 [no null settings, etc.] */
/* drop sundry constructors; no blanks; char[] gets ints */
/* drop sundry converters, add Exact converters */
/* 1999.05.27 additional tests for scaled arithmetic */
/* 1999.06.29 additional tests for exponent overflows */
/* 1999.07.03 add 'continue' option */
/* 1999.07.10 additional tests for scaled arithmetic */
/* 1999.07.18 randomly-generated tests added for base operators */
/* 1999.10.28 weird intValueExact bad cases */
/* 1999.12.21 multiplication fast path failure and edge cases */
/* 2000.01.01 copyright update */
/* 2000.03.26 cosmetic updates; add extra format() testcases */
/* 2000.03.27 1.00 move to android.icu.math package; open source release; */
/* change to javadoc comments */
/* ------------------------------------------------------------------ */

// note BINARY for conversions checking

/**
 * The <code>DiagBigDecimal</code> class forms a standalone test suite for the
 * <code>android.icu.math.BigDecimal</code> and
 * <code>android.icu.math.MathContext</code> classes (or, by changing the
 * <code>package</code> statement, other classes of the same names and
 * definition in other packages). It may also be used as a constructed object to
 * embed the tests in an external test harness.
 * <p>
 * The tests are collected into <i>groups</i>, each corresponding to a tested
 * method or a more general grouping. By default, when run from the static
 * {@link #main(java.lang.String[])} method, the run will end if any test fails
 * in a group. The <code>continue</code> argument may be specified to force
 * the tests to run to completion.
 *
 * @see android.icu.math.BigDecimal
 * @see android.icu.math.MathContext
 * @version 1.00 2000.03.27
 * @author Mike Cowlishaw
 */

public class DiagBigDecimalTest extends TestFmwk {
    private static final android.icu.math.BigDecimal zero = android.icu.math.BigDecimal.ZERO;
    private static final android.icu.math.BigDecimal one = android.icu.math.BigDecimal.ONE;
    private static final android.icu.math.BigDecimal two = new android.icu.math.BigDecimal(2);
    private static final android.icu.math.BigDecimal ten = android.icu.math.BigDecimal.TEN;
    private static final android.icu.math.BigDecimal tenlong = new android.icu.math.BigDecimal((long) 1234554321); // 10-digiter

    /* Some context objects -- [some of these are checked later] */
    private static final android.icu.math.MathContext mcdef = android.icu.math.MathContext.DEFAULT;
    private static final android.icu.math.MathContext mc3 = new android.icu.math.MathContext(3);
    private static final android.icu.math.MathContext mc6 = new android.icu.math.MathContext(6);
    private static final android.icu.math.MathContext mc9 = new android.icu.math.MathContext(9);
    private static final android.icu.math.MathContext mc50 = new android.icu.math.MathContext(50);
    private static final android.icu.math.MathContext mcs = new android.icu.math.MathContext(9, android.icu.math.MathContext.SCIENTIFIC);
    private static final android.icu.math.MathContext mce = new android.icu.math.MathContext(9, android.icu.math.MathContext.ENGINEERING);
    private static final android.icu.math.MathContext mcld = new android.icu.math.MathContext(9, android.icu.math.MathContext.SCIENTIFIC, true); // lost digits
    private static final android.icu.math.MathContext mcld0 = new android.icu.math.MathContext(0, android.icu.math.MathContext.SCIENTIFIC, true); // lost digits, digits=0
    private static final android.icu.math.MathContext mcfd = new android.icu.math.MathContext(0, android.icu.math.MathContext.PLAIN); // fixed decimal style

    /* boundary primitive values */
    private static final byte bmin = -128;
    private static final byte bmax = 127;
    private static final byte bzer = 0;
    private static final byte bneg = -1;
    private static final byte bpos = 1;
    private static final int imin = -2147483648;
    private static final int imax = 2147483647;
    private static final int izer = 0;
    private static final int ineg = -1;
    private static final int ipos = 1;
    private static final long lmin = -9223372036854775808L;
    private static final long lmax = 9223372036854775807L;
    private static final long lzer = 0;
    private static final long lneg = -1;
    private static final long lpos = 1;
    private static final short smin = -32768;
    private static final short smax = 32767;
    private static final short szer = (short) 0;
    private static final short sneg = (short) (-1);
    private static final short spos = (short) 1;

    /**
     * Constructs a <code>DiagBigDecimal</code> test suite.
     * <p>
     * Invoke its {@link #diagrun} method to run the tests.
     */

    public DiagBigDecimalTest() {
        super();
    }

    static final boolean isJDK15OrLater =
            TestUtil.getJavaVendor() == JavaVendor.Android ||
            TestUtil.getJavaVersion() >= 5;


    /*--------------------------------------------------------------------*/
    /* Diagnostic group methods */
    /*--------------------------------------------------------------------*/

    /** Test constructors (and {@link #toString()} for equalities). */
    @Test
    public void diagconstructors() {
        boolean flag = false;
        java.lang.String num;
        java.math.BigInteger bip;
        java.math.BigInteger biz;
        java.math.BigInteger bin;
        android.icu.math.BigDecimal bda;
        android.icu.math.BigDecimal bdb;
        android.icu.math.BigDecimal bmc;
        android.icu.math.BigDecimal bmd;
        android.icu.math.BigDecimal bme;
        java.lang.RuntimeException e = null;
        char ca[];
        double dzer;
        double dpos;
        double dneg;
        double dpos5;
        double dneg5;
        double dmin;
        double dmax;
        double d;
        java.lang.String badstrings[];
        int i = 0;

        // constants [statically-called constructors]
        TestFmwk.assertTrue("con001", (android.icu.math.BigDecimal.ZERO.toString()).equals("0"));
        TestFmwk.assertTrue("con002", (android.icu.math.BigDecimal.ONE.toString()).equals("1"));
        TestFmwk.assertTrue("con003", (android.icu.math.BigDecimal.TEN.toString()).equals("10"));
        TestFmwk.assertTrue("con004", (android.icu.math.BigDecimal.ZERO.intValueExact()) == 0);
        TestFmwk.assertTrue("con005", (android.icu.math.BigDecimal.ONE.intValueExact()) == 1);
        TestFmwk.assertTrue("con006", (android.icu.math.BigDecimal.TEN.intValueExact()) == 10);

        // [java.math.] BigDecimal
        TestFmwk.assertTrue("cbd001", ((new android.icu.math.BigDecimal(new java.math.BigDecimal("0"))).toString()).equals("0"));
        TestFmwk.assertTrue("cbd002", ((new android.icu.math.BigDecimal(new java.math.BigDecimal("1"))).toString()).equals("1"));
        TestFmwk.assertTrue("cbd003", ((new android.icu.math.BigDecimal(new java.math.BigDecimal("10"))).toString()).equals("10"));
        TestFmwk.assertTrue("cbd004", ((new android.icu.math.BigDecimal(new java.math.BigDecimal("1000"))).toString()).equals("1000"));
        TestFmwk.assertTrue("cbd005", ((new android.icu.math.BigDecimal(new java.math.BigDecimal("10.0"))).toString()).equals("10.0"));
        TestFmwk.assertTrue("cbd006", ((new android.icu.math.BigDecimal(new java.math.BigDecimal("10.1"))).toString()).equals("10.1"));
        TestFmwk.assertTrue("cbd007", ((new android.icu.math.BigDecimal(new java.math.BigDecimal("-1.1"))).toString()).equals("-1.1"));
        TestFmwk.assertTrue("cbd008", ((new android.icu.math.BigDecimal(new java.math.BigDecimal("-9.0"))).toString()).equals("-9.0"));
        TestFmwk.assertTrue("cbd009", ((new android.icu.math.BigDecimal(new java.math.BigDecimal("0.9"))).toString()).equals("0.9"));

        num = "123456789.123456789";
        TestFmwk.assertTrue("cbd010", ((new android.icu.math.BigDecimal(new java.math.BigDecimal(num))).toString()).equals(num));
        num = "123456789.000000000";
        TestFmwk.assertTrue("cbd011", ((new android.icu.math.BigDecimal(new java.math.BigDecimal(num))).toString()).equals(num));
        num = "123456789000000000";
        TestFmwk.assertTrue("cbd012", ((new android.icu.math.BigDecimal(new java.math.BigDecimal(num))).toString()).equals(num));
        num = "0.00000123456789";
        TestFmwk.assertTrue("cbd013", ((new android.icu.math.BigDecimal(new java.math.BigDecimal(num))).toString()).equals(num));
        num = "0.000000123456789";

        // ignore format change issues with 1.5
        if (!isJDK15OrLater)
            TestFmwk.assertTrue("cbd014", ((new android.icu.math.BigDecimal(new java.math.BigDecimal(num))).toString()).equals(num));

        try {
            new android.icu.math.BigDecimal((java.math.BigDecimal) null);
            flag = false;
        } catch (java.lang.NullPointerException $3) {
            flag = true;
        }/* checknull */
        TestFmwk.assertTrue("cbi015", flag);

        // BigInteger
        bip = new BigInteger("987654321987654321987654321"); // biggie +ve
        biz = new BigInteger("0"); // biggie 0
        bin = new BigInteger("-12345678998765432112345678"); // biggie -ve
        TestFmwk.assertTrue("cbi001", ((new android.icu.math.BigDecimal(bip)).toString()).equals(bip.toString()));
        TestFmwk.assertTrue("cbi002", ((new android.icu.math.BigDecimal(biz)).toString()).equals("0"));
        TestFmwk.assertTrue("cbi003", ((new android.icu.math.BigDecimal(bin)).toString()).equals(bin.toString()));
        try {
            new android.icu.math.BigDecimal((java.math.BigInteger) null);
            flag = false;
        } catch (java.lang.NullPointerException $4) {
            flag = true;
        }/* checknull */
        TestFmwk.assertTrue("cbi004", flag);

        // BigInteger with scale
        bip = new BigInteger("123456789"); // bigish
        bda = new android.icu.math.BigDecimal(bip);
        bdb = new android.icu.math.BigDecimal(bip, 5);
        bmc = new android.icu.math.BigDecimal(bip, 15);
        TestFmwk.assertTrue("cbs001", (bda.toString()).equals("123456789"));
        TestFmwk.assertTrue("cbs002", (bdb.toString()).equals("1234.56789"));
        TestFmwk.assertTrue("cbs003", (bmc.toString()).equals("0.000000123456789"));
        bip = new BigInteger("123456789123456789123456789"); // biggie
        bda = new android.icu.math.BigDecimal(bip);
        bdb = new android.icu.math.BigDecimal(bip, 7);
        bmc = new android.icu.math.BigDecimal(bip, 13);
        bmd = new android.icu.math.BigDecimal(bip, 19);
        bme = new android.icu.math.BigDecimal(bip, 29);
        TestFmwk.assertTrue("cbs011", (bda.toString()).equals("123456789123456789123456789"));
        TestFmwk.assertTrue("cbs012", (bdb.toString()).equals("12345678912345678912.3456789"));
        TestFmwk.assertTrue("cbs013", (bmc.toString()).equals("12345678912345.6789123456789"));
        TestFmwk.assertTrue("cbs014", (bmd.toString()).equals("12345678.9123456789123456789"));
        TestFmwk.assertTrue("cbs015", (bme.toString()).equals("0.00123456789123456789123456789"));
        try {
            new android.icu.math.BigDecimal((java.math.BigInteger) null, 1);
            flag = false;
        } catch (java.lang.NullPointerException $5) {
            flag = true;
        }/* checknull */
        TestFmwk.assertTrue("cbs004", flag);
        try {
            new android.icu.math.BigDecimal(bip, -8);
            flag = false;
        } catch (java.lang.RuntimeException $6) {
            e = $6;
            flag = (e.getMessage()).equals("Negative scale: -8");
        }/* checkscale */
        TestFmwk.assertTrue("cbs005", flag);

        // char[]
        // We just test it's there
        // Functionality is tested by BigDecimal(String).
        ca = ("123.45").toCharArray();
        TestFmwk.assertTrue("cca001", ((new android.icu.math.BigDecimal(ca)).toString()).equals("123.45"));
        try {
            new android.icu.math.BigDecimal((char[]) null);
            flag = false;
        } catch (java.lang.NullPointerException $7) {
            flag = true;
        }/* checknull */
        TestFmwk.assertTrue("cca010", flag);

        // char[],int,int
        // We just test it's there, and that offsets work.
        // Functionality is tested by BigDecimal(String).
        ca = ("123.45").toCharArray();
        TestFmwk.assertTrue("cca101", ((new android.icu.math.BigDecimal(ca, 0, 6)).toString()).equals("123.45"));
        TestFmwk.assertTrue("cca102", ((new android.icu.math.BigDecimal(ca, 1, 5)).toString()).equals("23.45"));
        TestFmwk.assertTrue("cca103", ((new android.icu.math.BigDecimal(ca, 2, 4)).toString()).equals("3.45"));
        TestFmwk.assertTrue("cca104", ((new android.icu.math.BigDecimal(ca, 3, 3)).toString()).equals("0.45"));
        TestFmwk.assertTrue("cca105", ((new android.icu.math.BigDecimal(ca, 4, 2)).toString()).equals("45"));
        TestFmwk.assertTrue("cca106", ((new android.icu.math.BigDecimal(ca, 5, 1)).toString()).equals("5"));

        TestFmwk.assertTrue("cca110", ((new android.icu.math.BigDecimal(ca, 0, 1)).toString()).equals("1"));
        TestFmwk.assertTrue("cca111", ((new android.icu.math.BigDecimal(ca, 1, 1)).toString()).equals("2"));
        TestFmwk.assertTrue("cca112", ((new android.icu.math.BigDecimal(ca, 2, 1)).toString()).equals("3"));
        TestFmwk.assertTrue("cca113", ((new android.icu.math.BigDecimal(ca, 4, 1)).toString()).equals("4"));

        TestFmwk.assertTrue("cca120", ((new android.icu.math.BigDecimal(ca, 0, 2)).toString()).equals("12"));
        TestFmwk.assertTrue("cca121", ((new android.icu.math.BigDecimal(ca, 1, 2)).toString()).equals("23"));
        TestFmwk.assertTrue("cca122", ((new android.icu.math.BigDecimal(ca, 2, 2)).toString()).equals("3"));
        TestFmwk.assertTrue("cca123", ((new android.icu.math.BigDecimal(ca, 3, 2)).toString()).equals("0.4"));

        TestFmwk.assertTrue("cca130", ((new android.icu.math.BigDecimal(ca, 0, 3)).toString()).equals("123"));
        TestFmwk.assertTrue("cca131", ((new android.icu.math.BigDecimal(ca, 1, 3)).toString()).equals("23"));
        TestFmwk.assertTrue("cca132", ((new android.icu.math.BigDecimal(ca, 2, 3)).toString()).equals("3.4"));

        TestFmwk.assertTrue("cca140", ((new android.icu.math.BigDecimal(ca, 0, 4)).toString()).equals("123"));
        TestFmwk.assertTrue("cca141", ((new android.icu.math.BigDecimal(ca, 1, 4)).toString()).equals("23.4"));

        TestFmwk.assertTrue("cca150", ((new android.icu.math.BigDecimal(ca, 0, 5)).toString()).equals("123.4"));

        // a couple of oddies
        ca = ("x23.4x").toCharArray();
        TestFmwk.assertTrue("cca160", ((new android.icu.math.BigDecimal(ca, 1, 4)).toString()).equals("23.4"));
        TestFmwk.assertTrue("cca161", ((new android.icu.math.BigDecimal(ca, 1, 1)).toString()).equals("2"));
        TestFmwk.assertTrue("cca162", ((new android.icu.math.BigDecimal(ca, 4, 1)).toString()).equals("4"));

        ca = ("0123456789.9876543210").toCharArray();
        TestFmwk.assertTrue("cca163", ((new android.icu.math.BigDecimal(ca, 0, 21)).toString()).equals("123456789.9876543210"));
        TestFmwk.assertTrue("cca164", ((new android.icu.math.BigDecimal(ca, 1, 20)).toString()).equals("123456789.9876543210"));
        TestFmwk.assertTrue("cca165", ((new android.icu.math.BigDecimal(ca, 2, 19)).toString()).equals("23456789.9876543210"));
        TestFmwk.assertTrue("cca166", ((new android.icu.math.BigDecimal(ca, 2, 18)).toString()).equals("23456789.987654321"));
        TestFmwk.assertTrue("cca167", ((new android.icu.math.BigDecimal(ca, 2, 17)).toString()).equals("23456789.98765432"));
        TestFmwk.assertTrue("cca168", ((new android.icu.math.BigDecimal(ca, 2, 16)).toString()).equals("23456789.9876543"));

        try {
            new android.icu.math.BigDecimal((char[]) null, 0, 1);
            flag = false;
        } catch (java.lang.NullPointerException $8) {
            flag = true;
        }/* checknull */
        TestFmwk.assertTrue("cca200", flag);

        try {
            new android.icu.math.BigDecimal("123".toCharArray(), 0, 0);
            flag = false;
        } catch (java.lang.NumberFormatException $9) {
            flag = true;
        }/* checklen */
        TestFmwk.assertTrue("cca201", flag);

        try {
            new android.icu.math.BigDecimal("123".toCharArray(), 2, 4);
            flag = false;
        } catch (java.lang.RuntimeException $10) { // anything OK
            flag = true;
        }/* checkbound */
        TestFmwk.assertTrue("cca202", flag);
        try {
            new android.icu.math.BigDecimal("123".toCharArray(), -1, 2);
            flag = false;
        } catch (java.lang.RuntimeException $11) { // anything OK
            flag = true;
        }/* checkbound2 */
        TestFmwk.assertTrue("cca203", flag);
        try {
            new android.icu.math.BigDecimal("123".toCharArray(), 1, -2);
            flag = false;
        } catch (java.lang.RuntimeException $12) { // anything OK
            flag = true;
        }/* checkbound3 */
        TestFmwk.assertTrue("cca204", flag);

        // double [deprecated]
        // Note that many of these differ from the valueOf(double) results.
        dzer = 0;
        dpos = 1;
        dpos = dpos / (10);
        dneg = -dpos;
        TestFmwk.assertTrue("cdo001", ((new android.icu.math.BigDecimal(dneg)).toString()).equals("-0.1000000000000000055511151231257827021181583404541015625"));

        TestFmwk.assertTrue("cdo002", ((new android.icu.math.BigDecimal(dzer)).toString()).equals("0")); // NB, not '0.0'
        TestFmwk.assertTrue("cdo003", ((new android.icu.math.BigDecimal(dpos)).toString()).equals("0.1000000000000000055511151231257827021181583404541015625"));

        dpos5 = 0.5D;
        dneg5 = -dpos5;
        TestFmwk.assertTrue("cdo004", ((new android.icu.math.BigDecimal(dneg5)).toString()).equals("-0.5"));
        TestFmwk.assertTrue("cdo005", ((new android.icu.math.BigDecimal(dpos5)).toString()).equals("0.5"));
        dmin = java.lang.Double.MIN_VALUE;
        dmax = java.lang.Double.MAX_VALUE;
        if (!isJDK15OrLater) // for some reason we format using scientific
                                // notation on 1.5 after 30 decimals or so
            TestFmwk.assertTrue("cdo006", ((new android.icu.math.BigDecimal(dmin)).toString()).equals("0.000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000004940656458412465441765687928682213723650598026143247644255856825006755072702087518652998363616359923797965646954457177309266567103559397963987747960107818781263007131903114045278458171678489821036887186360569987307230500063874091535649843873124733972731696151400317153853980741262385655911710266585566867681870395603106249319452715914924553293054565444011274801297099995419319894090804165633245247571478690147267801593552386115501348035264934720193790268107107491703332226844753335720832431936092382893458368060106011506169809753078342277318329247904982524730776375927247874656084778203734469699533647017972677717585125660551199131504891101451037862738167250955837389733598993664809941164205702637090279242767544565229087538682506419718265533447265625"));

        TestFmwk.assertTrue("cdo007", ((new android.icu.math.BigDecimal(dmax)).toString()).equals("179769313486231570814527423731704356798070567525844996598917476803157260780028538760589558632766878171540458953514382464234321326889464182768467546703537516986049910576551282076245490090389328944075868508455133942304583236903222948165808559332123348274797826204144723168738177180919299881250404026184124858368"));

        // nasties
        d = 9;
        d = d / (10);
        TestFmwk.assertTrue("cdo010", ((new android.icu.math.BigDecimal(d)).toString()).equals("0.90000000000000002220446049250313080847263336181640625"));

        d = d / (10);
        TestFmwk.assertTrue("cdo011", ((new android.icu.math.BigDecimal(d)).toString()).equals("0.0899999999999999966693309261245303787291049957275390625"));

        d = d / (10);
        TestFmwk.assertTrue("cdo012", ((new android.icu.math.BigDecimal(d)).toString()).equals("0.00899999999999999931998839741709161899052560329437255859375"));

        d = d / (10);
        TestFmwk.assertTrue("cdo013", ((new android.icu.math.BigDecimal(d)).toString()).equals("0.00089999999999999997536692664112933925935067236423492431640625"));

        d = d / (10);
        TestFmwk.assertTrue("cdo014", ((new android.icu.math.BigDecimal(d)).toString()).equals("0.00008999999999999999211568180168541175589780323207378387451171875"));

        d = d / (10);
        TestFmwk.assertTrue("cdo015", ((new android.icu.math.BigDecimal(d)).toString()).equals("0.00000899999999999999853394182236510090433512232266366481781005859375"));

        d = d / (10);
        if (!isJDK15OrLater)
            TestFmwk.assertTrue("cdo016", ((new android.icu.math.BigDecimal(d)).toString()).equals("0.000000899999999999999853394182236510090433512232266366481781005859375"));

        d = d / (10);
        if (!isJDK15OrLater)
            TestFmwk.assertTrue("cdo017", ((new android.icu.math.BigDecimal(d)).toString()).equals("0.0000000899999999999999853394182236510090433512232266366481781005859375"));

        d = d / (10);
        if (!isJDK15OrLater)
            TestFmwk.assertTrue("cdo018", ((new android.icu.math.BigDecimal(d)).toString()).equals("0.000000008999999999999997872197332322678764437995369007694534957408905029296875"));

        try {
            new android.icu.math.BigDecimal(
                    java.lang.Double.POSITIVE_INFINITY);
            flag = false;
        } catch (java.lang.NumberFormatException $13) {
            flag = true;
        }/* checkpin */
        TestFmwk.assertTrue("cdo101", flag);
        try {
            new android.icu.math.BigDecimal(
                    java.lang.Double.NEGATIVE_INFINITY);
            flag = false;
        } catch (java.lang.NumberFormatException $14) {
            flag = true;
        }/* checknin */
        TestFmwk.assertTrue("cdo102", flag);
        try {
            new android.icu.math.BigDecimal(java.lang.Double.NaN);
            flag = false;
        } catch (java.lang.NumberFormatException $15) {
            flag = true;
        }/* checknan */
        TestFmwk.assertTrue("cdo103", flag);

        // int
        TestFmwk.assertTrue("cin001", ((new android.icu.math.BigDecimal(imin)).toString()).equals("-2147483648"));
        TestFmwk.assertTrue("cin002", ((new android.icu.math.BigDecimal(imax)).toString()).equals("2147483647"));
        TestFmwk.assertTrue("cin003", ((new android.icu.math.BigDecimal(ineg)).toString()).equals("-1"));
        TestFmwk.assertTrue("cin004", ((new android.icu.math.BigDecimal(izer)).toString()).equals("0"));
        TestFmwk.assertTrue("cin005", ((new android.icu.math.BigDecimal(ipos)).toString()).equals("1"));
        TestFmwk.assertTrue("cin006", ((new android.icu.math.BigDecimal(10)).toString()).equals("10"));
        TestFmwk.assertTrue("cin007", ((new android.icu.math.BigDecimal(9)).toString()).equals("9"));
        TestFmwk.assertTrue("cin008", ((new android.icu.math.BigDecimal(5)).toString()).equals("5"));
        TestFmwk.assertTrue("cin009", ((new android.icu.math.BigDecimal(2)).toString()).equals("2"));
        TestFmwk.assertTrue("cin010", ((new android.icu.math.BigDecimal(-2)).toString()).equals("-2"));
        TestFmwk.assertTrue("cin011", ((new android.icu.math.BigDecimal(-5)).toString()).equals("-5"));
        TestFmwk.assertTrue("cin012", ((new android.icu.math.BigDecimal(-9)).toString()).equals("-9"));
        TestFmwk.assertTrue("cin013", ((new android.icu.math.BigDecimal(-10)).toString()).equals("-10"));
        TestFmwk.assertTrue("cin014", ((new android.icu.math.BigDecimal(-11)).toString()).equals("-11"));
        TestFmwk.assertTrue("cin015", ((new android.icu.math.BigDecimal(-99)).toString()).equals("-99"));
        TestFmwk.assertTrue("cin016", ((new android.icu.math.BigDecimal(-100)).toString()).equals("-100"));
        TestFmwk.assertTrue("cin017", ((new android.icu.math.BigDecimal(-999)).toString()).equals("-999"));
        TestFmwk.assertTrue("cin018", ((new android.icu.math.BigDecimal(-1000)).toString()).equals("-1000"));

        TestFmwk.assertTrue("cin019", ((new android.icu.math.BigDecimal(11)).toString()).equals("11"));
        TestFmwk.assertTrue("cin020", ((new android.icu.math.BigDecimal(99)).toString()).equals("99"));
        TestFmwk.assertTrue("cin021", ((new android.icu.math.BigDecimal(100)).toString()).equals("100"));
        TestFmwk.assertTrue("cin022", ((new android.icu.math.BigDecimal(999)).toString()).equals("999"));
        TestFmwk.assertTrue("cin023", ((new android.icu.math.BigDecimal(1000)).toString()).equals("1000"));

        // long
        TestFmwk.assertTrue("clo001", ((new android.icu.math.BigDecimal(lmin)).toString()).equals("-9223372036854775808"));
        TestFmwk.assertTrue("clo002", ((new android.icu.math.BigDecimal(lmax)).toString()).equals("9223372036854775807"));
        TestFmwk.assertTrue("clo003", ((new android.icu.math.BigDecimal(lneg)).toString()).equals("-1"));
        TestFmwk.assertTrue("clo004", ((new android.icu.math.BigDecimal(lzer)).toString()).equals("0"));
        TestFmwk.assertTrue("clo005", ((new android.icu.math.BigDecimal(lpos)).toString()).equals("1"));

        // String [many more examples are elsewhere]
        // strings without E cannot generate E in result
        TestFmwk.assertTrue("cst001", ((new android.icu.math.BigDecimal("12")).toString()).equals("12"));
        TestFmwk.assertTrue("cst002", ((new android.icu.math.BigDecimal("-76")).toString()).equals("-76"));
        TestFmwk.assertTrue("cst003", ((new android.icu.math.BigDecimal("12.76")).toString()).equals("12.76"));
        TestFmwk.assertTrue("cst004", ((new android.icu.math.BigDecimal("+12.76")).toString()).equals("12.76"));
        TestFmwk.assertTrue("cst005", ((new android.icu.math.BigDecimal("012.76")).toString()).equals("12.76"));
        TestFmwk.assertTrue("cst006", ((new android.icu.math.BigDecimal("+0.003")).toString()).equals("0.003"));
        TestFmwk.assertTrue("cst007", ((new android.icu.math.BigDecimal("17.")).toString()).equals("17"));
        TestFmwk.assertTrue("cst008", ((new android.icu.math.BigDecimal(".5")).toString()).equals("0.5"));
        TestFmwk.assertTrue("cst009", ((new android.icu.math.BigDecimal("044")).toString()).equals("44"));
        TestFmwk.assertTrue("cst010", ((new android.icu.math.BigDecimal("0044")).toString()).equals("44"));
        TestFmwk.assertTrue("cst011", ((new android.icu.math.BigDecimal("0.0005")).toString()).equals("0.0005"));
        TestFmwk.assertTrue("cst012", ((new android.icu.math.BigDecimal("00.00005")).toString()).equals("0.00005"));
        TestFmwk.assertTrue("cst013", ((new android.icu.math.BigDecimal("0.000005")).toString()).equals("0.000005"));
        TestFmwk.assertTrue("cst014", ((new android.icu.math.BigDecimal("0.0000005")).toString()).equals("0.0000005")); // \NR
        TestFmwk.assertTrue("cst015", ((new android.icu.math.BigDecimal("0.00000005")).toString()).equals("0.00000005")); // \NR
        TestFmwk.assertTrue("cst016", ((new android.icu.math.BigDecimal("12345678.876543210")).toString()).equals("12345678.876543210"));
        TestFmwk.assertTrue("cst017", ((new android.icu.math.BigDecimal("2345678.876543210")).toString()).equals("2345678.876543210"));
        TestFmwk.assertTrue("cst018", ((new android.icu.math.BigDecimal("345678.876543210")).toString()).equals("345678.876543210"));
        TestFmwk.assertTrue("cst019", ((new android.icu.math.BigDecimal("0345678.87654321")).toString()).equals("345678.87654321"));
        TestFmwk.assertTrue("cst020", ((new android.icu.math.BigDecimal("345678.8765432")).toString()).equals("345678.8765432"));
        TestFmwk.assertTrue("cst021", ((new android.icu.math.BigDecimal("+345678.8765432")).toString()).equals("345678.8765432"));
        TestFmwk.assertTrue("cst022", ((new android.icu.math.BigDecimal("+0345678.8765432")).toString()).equals("345678.8765432"));
        TestFmwk.assertTrue("cst023", ((new android.icu.math.BigDecimal("+00345678.8765432")).toString()).equals("345678.8765432"));
        TestFmwk.assertTrue("cst024", ((new android.icu.math.BigDecimal("-345678.8765432")).toString()).equals("-345678.8765432"));
        TestFmwk.assertTrue("cst025", ((new android.icu.math.BigDecimal("-0345678.8765432")).toString()).equals("-345678.8765432"));
        TestFmwk.assertTrue("cst026", ((new android.icu.math.BigDecimal("-00345678.8765432")).toString()).equals("-345678.8765432"));

        // exotics --
        TestFmwk.assertTrue("cst035", ((new android.icu.math.BigDecimal("\u0e57.\u0e50")).toString()).equals("7.0"));
        TestFmwk.assertTrue("cst036", ((new android.icu.math.BigDecimal("\u0b66.\u0b67")).toString()).equals("0.1"));
        TestFmwk.assertTrue("cst037", ((new android.icu.math.BigDecimal("\u0b66\u0b66")).toString()).equals("0"));
        TestFmwk.assertTrue("cst038", ((new android.icu.math.BigDecimal("\u0b6a\u0b66")).toString()).equals("40"));

        // strings with E
        TestFmwk.assertTrue("cst040", ((new android.icu.math.BigDecimal("1E+9")).toString()).equals("1E+9"));
        TestFmwk.assertTrue("cst041", ((new android.icu.math.BigDecimal("1e+09")).toString()).equals("1E+9"));
        TestFmwk.assertTrue("cst042", ((new android.icu.math.BigDecimal("1E+90")).toString()).equals("1E+90"));
        TestFmwk.assertTrue("cst043", ((new android.icu.math.BigDecimal("+1E+009")).toString()).equals("1E+9"));
        TestFmwk.assertTrue("cst044", ((new android.icu.math.BigDecimal("0E+9")).toString()).equals("0"));
        TestFmwk.assertTrue("cst045", ((new android.icu.math.BigDecimal("1E+9")).toString()).equals("1E+9"));
        TestFmwk.assertTrue("cst046", ((new android.icu.math.BigDecimal("1E+09")).toString()).equals("1E+9"));
        TestFmwk.assertTrue("cst047", ((new android.icu.math.BigDecimal("1e+90")).toString()).equals("1E+90"));
        TestFmwk.assertTrue("cst048", ((new android.icu.math.BigDecimal("1E+009")).toString()).equals("1E+9"));
        TestFmwk.assertTrue("cst049", ((new android.icu.math.BigDecimal("0E+9")).toString()).equals("0"));
        TestFmwk.assertTrue("cst050", ((new android.icu.math.BigDecimal("1E9")).toString()).equals("1E+9"));
        TestFmwk.assertTrue("cst051", ((new android.icu.math.BigDecimal("1e09")).toString()).equals("1E+9"));
        TestFmwk.assertTrue("cst052", ((new android.icu.math.BigDecimal("1E90")).toString()).equals("1E+90"));
        TestFmwk.assertTrue("cst053", ((new android.icu.math.BigDecimal("1E009")).toString()).equals("1E+9"));
        TestFmwk.assertTrue("cst054", ((new android.icu.math.BigDecimal("0E9")).toString()).equals("0"));
        TestFmwk.assertTrue("cst055", ((new android.icu.math.BigDecimal("0.000e+0")).toString()).equals("0"));
        TestFmwk.assertTrue("cst056", ((new android.icu.math.BigDecimal("0.000E-1")).toString()).equals("0"));
        TestFmwk.assertTrue("cst057", ((new android.icu.math.BigDecimal("4E+9")).toString()).equals("4E+9"));
        TestFmwk.assertTrue("cst058", ((new android.icu.math.BigDecimal("44E+9")).toString()).equals("4.4E+10"));
        TestFmwk.assertTrue("cst059", ((new android.icu.math.BigDecimal("0.73e-7")).toString()).equals("7.3E-8"));
        TestFmwk.assertTrue("cst060", ((new android.icu.math.BigDecimal("00E+9")).toString()).equals("0"));
        TestFmwk.assertTrue("cst061", ((new android.icu.math.BigDecimal("00E-9")).toString()).equals("0"));
        TestFmwk.assertTrue("cst062", ((new android.icu.math.BigDecimal("10E+9")).toString()).equals("1.0E+10"));
        TestFmwk.assertTrue("cst063", ((new android.icu.math.BigDecimal("10E+09")).toString()).equals("1.0E+10"));
        TestFmwk.assertTrue("cst064", ((new android.icu.math.BigDecimal("10e+90")).toString()).equals("1.0E+91"));
        TestFmwk.assertTrue("cst065", ((new android.icu.math.BigDecimal("10E+009")).toString()).equals("1.0E+10"));
        TestFmwk.assertTrue("cst066", ((new android.icu.math.BigDecimal("100e+9")).toString()).equals("1.00E+11"));
        TestFmwk.assertTrue("cst067", ((new android.icu.math.BigDecimal("100e+09")).toString()).equals("1.00E+11"));
        TestFmwk.assertTrue("cst068", ((new android.icu.math.BigDecimal("100E+90")).toString()).equals("1.00E+92"));
        TestFmwk.assertTrue("cst069", ((new android.icu.math.BigDecimal("100e+009")).toString()).equals("1.00E+11"));

        TestFmwk.assertTrue("cst070", ((new android.icu.math.BigDecimal("1.265")).toString()).equals("1.265"));
        TestFmwk.assertTrue("cst071", ((new android.icu.math.BigDecimal("1.265E-20")).toString()).equals("1.265E-20"));
        TestFmwk.assertTrue("cst072", ((new android.icu.math.BigDecimal("1.265E-8")).toString()).equals("1.265E-8"));
        TestFmwk.assertTrue("cst073", ((new android.icu.math.BigDecimal("1.265E-4")).toString()).equals("1.265E-4"));
        TestFmwk.assertTrue("cst074", ((new android.icu.math.BigDecimal("1.265E-3")).toString()).equals("1.265E-3"));
        TestFmwk.assertTrue("cst075", ((new android.icu.math.BigDecimal("1.265E-2")).toString()).equals("1.265E-2"));
        TestFmwk.assertTrue("cst076", ((new android.icu.math.BigDecimal("1.265E-1")).toString()).equals("1.265E-1"));
        TestFmwk.assertTrue("cst077", ((new android.icu.math.BigDecimal("1.265E-0")).toString()).equals("1.265"));
        TestFmwk.assertTrue("cst078", ((new android.icu.math.BigDecimal("1.265E+1")).toString()).equals("1.265E+1"));
        TestFmwk.assertTrue("cst079", ((new android.icu.math.BigDecimal("1.265E+2")).toString()).equals("1.265E+2"));
        TestFmwk.assertTrue("cst080", ((new android.icu.math.BigDecimal("1.265E+3")).toString()).equals("1.265E+3"));
        TestFmwk.assertTrue("cst081", ((new android.icu.math.BigDecimal("1.265E+4")).toString()).equals("1.265E+4"));
        TestFmwk.assertTrue("cst082", ((new android.icu.math.BigDecimal("1.265E+8")).toString()).equals("1.265E+8"));
        TestFmwk.assertTrue("cst083", ((new android.icu.math.BigDecimal("1.265E+20")).toString()).equals("1.265E+20"));

        TestFmwk.assertTrue("cst090", ((new android.icu.math.BigDecimal("12.65")).toString()).equals("12.65"));
        TestFmwk.assertTrue("cst091", ((new android.icu.math.BigDecimal("12.65E-20")).toString()).equals("1.265E-19"));
        TestFmwk.assertTrue("cst092", ((new android.icu.math.BigDecimal("12.65E-8")).toString()).equals("1.265E-7"));
        TestFmwk.assertTrue("cst093", ((new android.icu.math.BigDecimal("12.65E-4")).toString()).equals("1.265E-3"));
        TestFmwk.assertTrue("cst094", ((new android.icu.math.BigDecimal("12.65E-3")).toString()).equals("1.265E-2"));
        TestFmwk.assertTrue("cst095", ((new android.icu.math.BigDecimal("12.65E-2")).toString()).equals("1.265E-1"));
        TestFmwk.assertTrue("cst096", ((new android.icu.math.BigDecimal("12.65E-1")).toString()).equals("1.265"));
        TestFmwk.assertTrue("cst097", ((new android.icu.math.BigDecimal("12.65E-0")).toString()).equals("1.265E+1"));
        TestFmwk.assertTrue("cst098", ((new android.icu.math.BigDecimal("12.65E+1")).toString()).equals("1.265E+2"));
        TestFmwk.assertTrue("cst099", ((new android.icu.math.BigDecimal("12.65E+2")).toString()).equals("1.265E+3"));
        TestFmwk.assertTrue("cst100", ((new android.icu.math.BigDecimal("12.65E+3")).toString()).equals("1.265E+4"));
        TestFmwk.assertTrue("cst101", ((new android.icu.math.BigDecimal("12.65E+4")).toString()).equals("1.265E+5"));
        TestFmwk.assertTrue("cst102", ((new android.icu.math.BigDecimal("12.65E+8")).toString()).equals("1.265E+9"));
        TestFmwk.assertTrue("cst103", ((new android.icu.math.BigDecimal("12.65E+20")).toString()).equals("1.265E+21"));

        TestFmwk.assertTrue("cst110", ((new android.icu.math.BigDecimal("126.5")).toString()).equals("126.5"));
        TestFmwk.assertTrue("cst111", ((new android.icu.math.BigDecimal("126.5E-20")).toString()).equals("1.265E-18"));
        TestFmwk.assertTrue("cst112", ((new android.icu.math.BigDecimal("126.5E-8")).toString()).equals("1.265E-6"));
        TestFmwk.assertTrue("cst113", ((new android.icu.math.BigDecimal("126.5E-4")).toString()).equals("1.265E-2"));
        TestFmwk.assertTrue("cst114", ((new android.icu.math.BigDecimal("126.5E-3")).toString()).equals("1.265E-1"));
        TestFmwk.assertTrue("cst115", ((new android.icu.math.BigDecimal("126.5E-2")).toString()).equals("1.265"));
        TestFmwk.assertTrue("cst116", ((new android.icu.math.BigDecimal("126.5E-1")).toString()).equals("1.265E+1"));
        TestFmwk.assertTrue("cst117", ((new android.icu.math.BigDecimal("126.5E-0")).toString()).equals("1.265E+2"));
        TestFmwk.assertTrue("cst118", ((new android.icu.math.BigDecimal("126.5E+1")).toString()).equals("1.265E+3"));
        TestFmwk.assertTrue("cst119", ((new android.icu.math.BigDecimal("126.5E+2")).toString()).equals("1.265E+4"));
        TestFmwk.assertTrue("cst120", ((new android.icu.math.BigDecimal("126.5E+3")).toString()).equals("1.265E+5"));
        TestFmwk.assertTrue("cst121", ((new android.icu.math.BigDecimal("126.5E+4")).toString()).equals("1.265E+6"));
        TestFmwk.assertTrue("cst122", ((new android.icu.math.BigDecimal("126.5E+8")).toString()).equals("1.265E+10"));
        TestFmwk.assertTrue("cst123", ((new android.icu.math.BigDecimal("126.5E+20")).toString()).equals("1.265E+22"));

        TestFmwk.assertTrue("cst130", ((new android.icu.math.BigDecimal("1265")).toString()).equals("1265"));
        TestFmwk.assertTrue("cst131", ((new android.icu.math.BigDecimal("1265E-20")).toString()).equals("1.265E-17"));
        TestFmwk.assertTrue("cst132", ((new android.icu.math.BigDecimal("1265E-8")).toString()).equals("1.265E-5"));
        TestFmwk.assertTrue("cst133", ((new android.icu.math.BigDecimal("1265E-4")).toString()).equals("1.265E-1"));
        TestFmwk.assertTrue("cst134", ((new android.icu.math.BigDecimal("1265E-3")).toString()).equals("1.265"));
        TestFmwk.assertTrue("cst135", ((new android.icu.math.BigDecimal("1265E-2")).toString()).equals("1.265E+1"));
        TestFmwk.assertTrue("cst136", ((new android.icu.math.BigDecimal("1265E-1")).toString()).equals("1.265E+2"));
        TestFmwk.assertTrue("cst137", ((new android.icu.math.BigDecimal("1265E-0")).toString()).equals("1.265E+3"));
        TestFmwk.assertTrue("cst138", ((new android.icu.math.BigDecimal("1265E+1")).toString()).equals("1.265E+4"));
        TestFmwk.assertTrue("cst139", ((new android.icu.math.BigDecimal("1265E+2")).toString()).equals("1.265E+5"));
        TestFmwk.assertTrue("cst140", ((new android.icu.math.BigDecimal("1265E+3")).toString()).equals("1.265E+6"));
        TestFmwk.assertTrue("cst141", ((new android.icu.math.BigDecimal("1265E+4")).toString()).equals("1.265E+7"));
        TestFmwk.assertTrue("cst142", ((new android.icu.math.BigDecimal("1265E+8")).toString()).equals("1.265E+11"));
        TestFmwk.assertTrue("cst143", ((new android.icu.math.BigDecimal("1265E+20")).toString()).equals("1.265E+23"));

        TestFmwk.assertTrue("cst150", ((new android.icu.math.BigDecimal("0.1265")).toString()).equals("0.1265"));
        TestFmwk.assertTrue("cst151", ((new android.icu.math.BigDecimal("0.1265E-20")).toString()).equals("1.265E-21"));
        TestFmwk.assertTrue("cst152", ((new android.icu.math.BigDecimal("0.1265E-8")).toString()).equals("1.265E-9"));
        TestFmwk.assertTrue("cst153", ((new android.icu.math.BigDecimal("0.1265E-4")).toString()).equals("1.265E-5"));
        TestFmwk.assertTrue("cst154", ((new android.icu.math.BigDecimal("0.1265E-3")).toString()).equals("1.265E-4"));
        TestFmwk.assertTrue("cst155", ((new android.icu.math.BigDecimal("0.1265E-2")).toString()).equals("1.265E-3"));
        TestFmwk.assertTrue("cst156", ((new android.icu.math.BigDecimal("0.1265E-1")).toString()).equals("1.265E-2"));
        TestFmwk.assertTrue("cst157", ((new android.icu.math.BigDecimal("0.1265E-0")).toString()).equals("1.265E-1"));
        TestFmwk.assertTrue("cst158", ((new android.icu.math.BigDecimal("0.1265E+1")).toString()).equals("1.265"));
        TestFmwk.assertTrue("cst159", ((new android.icu.math.BigDecimal("0.1265E+2")).toString()).equals("1.265E+1"));
        TestFmwk.assertTrue("cst160", ((new android.icu.math.BigDecimal("0.1265E+3")).toString()).equals("1.265E+2"));
        TestFmwk.assertTrue("cst161", ((new android.icu.math.BigDecimal("0.1265E+4")).toString()).equals("1.265E+3"));
        TestFmwk.assertTrue("cst162", ((new android.icu.math.BigDecimal("0.1265E+8")).toString()).equals("1.265E+7"));
        TestFmwk.assertTrue("cst163", ((new android.icu.math.BigDecimal("0.1265E+20")).toString()).equals("1.265E+19"));

        TestFmwk.assertTrue("cst170", ((new android.icu.math.BigDecimal("0.09e999999999")).toString()).equals("9E+999999997"));
        TestFmwk.assertTrue("cst171", ((new android.icu.math.BigDecimal("0.9e999999999")).toString()).equals("9E+999999998"));
        TestFmwk.assertTrue("cst172", ((new android.icu.math.BigDecimal("9e999999999")).toString()).equals("9E+999999999"));
        TestFmwk.assertTrue("cst173", ((new android.icu.math.BigDecimal("9.9e999999999")).toString()).equals("9.9E+999999999"));
        TestFmwk.assertTrue("cst174", ((new android.icu.math.BigDecimal("9.99e999999999")).toString()).equals("9.99E+999999999"));
        TestFmwk.assertTrue("cst175", ((new android.icu.math.BigDecimal("9.99e-999999999")).toString()).equals("9.99E-999999999"));
        TestFmwk.assertTrue("cst176", ((new android.icu.math.BigDecimal("9.9e-999999999")).toString()).equals("9.9E-999999999"));
        TestFmwk.assertTrue("cst177", ((new android.icu.math.BigDecimal("9e-999999999")).toString()).equals("9E-999999999"));
        TestFmwk.assertTrue("cst179", ((new android.icu.math.BigDecimal("99e-999999999")).toString()).equals("9.9E-999999998"));
        TestFmwk.assertTrue("cst180", ((new android.icu.math.BigDecimal("999e-999999999")).toString()).equals("9.99E-999999997"));

        // baddies --
        badstrings = new java.lang.String[] { "1..2", ".", "..", "++1", "--1",
                "-+1", "+-1", "12e", "12e++", "12f4", " +1", "+ 1", "12 ",
                " + 1", " - 1 ", "x", "-1-", "12-", "3+", "", "1e-",
                "7e1000000000", "", "e100", "\u0e5a", "\u0b65", "99e999999999",
                "999e999999999", "0.9e-999999999", "0.09e-999999999",
                "0.1e1000000000", "10e-1000000000", "0.9e9999999999",
                "99e-9999999999", "111e9999999999",
                "1111e-9999999999" + " " + "111e*123", "111e123-", "111e+12+",
                "111e1-3-", "111e1*23", "111e1e+3", "1e1.0", "1e123e", "ten",
                "ONE", "1e.1", "1e1.", "1ee", "e+1" }; // 200-203
        // 204-207
        // 208-211
        // 211-214
        // 215-219
        // 220-222
        // 223-224
        // 225-226
        // 227-228
        // 229-230
        // 231-232
        // 233-234
        // 235-237
        // 238-240
        // 241-244
        // 245-248

        // watch out for commas on continuation lines

        {
            int $16 = badstrings.length;
            i = 0;
            for (; $16 > 0; $16--, i++) {
                try {
                    new android.icu.math.BigDecimal(badstrings[i]);
                    say(">>> cst"+(200+i)+":"+" "+badstrings[i]+" "+(new android.icu.math.BigDecimal(badstrings[i])).toString());
                    flag = false;
                } catch (java.lang.NumberFormatException $17) {
                    flag = true;
                }
                TestFmwk.assertTrue("cst" + (200 + i), flag);
            }
        }/* i */

        try {
            new android.icu.math.BigDecimal((java.lang.String) null);
            flag = false;
        } catch (java.lang.NullPointerException $18) {
            flag = true;
        }/* checknull */
        TestFmwk.assertTrue("cst301", flag);

        return;
    }

    /** Mutation tests (checks that contents of constant objects are unchanged). */

    @Test
    public void diagmutation() {
        /* ---------------------------------------------------------------- */
        /* Final tests -- check constants haven't mutated */
        /* -- also that MC objects haven't mutated */
        /* ---------------------------------------------------------------- */
        TestFmwk.assertTrue("cuc001", (android.icu.math.BigDecimal.ZERO.toString()).equals("0"));
        TestFmwk.assertTrue("cuc002", (android.icu.math.BigDecimal.ONE.toString()).equals("1"));
        TestFmwk.assertTrue("cuc003", (android.icu.math.BigDecimal.TEN.toString()).equals("10"));

        @SuppressWarnings("unused")
        int constantVal; // workaround for "Comparing identical expressions" warnings
        TestFmwk.assertTrue("cuc010", android.icu.math.BigDecimal.ROUND_CEILING == (constantVal = android.icu.math.MathContext.ROUND_CEILING));
        TestFmwk.assertTrue("cuc011", android.icu.math.BigDecimal.ROUND_DOWN == (constantVal = android.icu.math.MathContext.ROUND_DOWN));
        TestFmwk.assertTrue("cuc012", android.icu.math.BigDecimal.ROUND_FLOOR == (constantVal = android.icu.math.MathContext.ROUND_FLOOR));
        TestFmwk.assertTrue("cuc013", android.icu.math.BigDecimal.ROUND_HALF_DOWN == (constantVal = android.icu.math.MathContext.ROUND_HALF_DOWN));
        TestFmwk.assertTrue("cuc014", android.icu.math.BigDecimal.ROUND_HALF_EVEN == (constantVal = android.icu.math.MathContext.ROUND_HALF_EVEN));
        TestFmwk.assertTrue("cuc015", android.icu.math.BigDecimal.ROUND_HALF_UP == (constantVal = android.icu.math.MathContext.ROUND_HALF_UP));
        TestFmwk.assertTrue("cuc016", android.icu.math.BigDecimal.ROUND_UNNECESSARY == (constantVal = android.icu.math.MathContext.ROUND_UNNECESSARY));
        TestFmwk.assertTrue("cuc017", android.icu.math.BigDecimal.ROUND_UP == (constantVal = android.icu.math.MathContext.ROUND_UP));

        TestFmwk.assertTrue("cuc020", (android.icu.math.MathContext.DEFAULT.getDigits()) == 9);
        TestFmwk.assertTrue("cuc021", (android.icu.math.MathContext.DEFAULT.getForm()) == android.icu.math.MathContext.SCIENTIFIC);
        TestFmwk.assertTrue("cuc022", (android.icu.math.MathContext.DEFAULT.getLostDigits() ? 1 : 0) == 0);
        TestFmwk.assertTrue("cuc023", (android.icu.math.MathContext.DEFAULT.getRoundingMode()) == android.icu.math.MathContext.ROUND_HALF_UP);

        // mc9 =MathContext(9)
        // mcld =MathContext(9, SCIENTIFIC, 1)
        // mcfd =MathContext(0, PLAIN)
        TestFmwk.assertTrue("cuc030", (mc9.getDigits()) == 9);
        TestFmwk.assertTrue("cuc031", (mc9.getForm()) == android.icu.math.MathContext.SCIENTIFIC);
        TestFmwk.assertTrue("cuc032", (mc9.getLostDigits() ? 1 : 0) == 0);
        TestFmwk.assertTrue("cuc033", (mc9.getRoundingMode()) == android.icu.math.MathContext.ROUND_HALF_UP);
        TestFmwk.assertTrue("cuc034", (mcld.getDigits()) == 9);
        TestFmwk.assertTrue("cuc035", (mcld.getForm()) == android.icu.math.MathContext.SCIENTIFIC);
        TestFmwk.assertTrue("cuc036", (mcld.getLostDigits() ? 1 : 0) == 1);
        TestFmwk.assertTrue("cuc037", (mcld.getRoundingMode()) == android.icu.math.MathContext.ROUND_HALF_UP);
        TestFmwk.assertTrue("cuc038", (mcfd.getDigits()) == 0);
        TestFmwk.assertTrue("cuc039", (mcfd.getForm()) == android.icu.math.MathContext.PLAIN);
        TestFmwk.assertTrue("cuc040", (mcfd.getLostDigits() ? 1 : 0) == 0);
        TestFmwk.assertTrue("cuc041", (mcfd.getRoundingMode()) == android.icu.math.MathContext.ROUND_HALF_UP);

    }


    /* ----------------------------------------------------------------- */
    /* Operator test methods */
    /* ----------------------------------------------------------------- */
    // The use of context in these tests are primarily to show that they
    // are correctly passed to the methods, except that we check that
    // each method checks for lostDigits.

    /** Test the {@link android.icu.math.BigDecimal#abs} method. */

    @Test
    public void diagabs() {
        boolean flag = false;
        java.lang.ArithmeticException ae = null;

        // most of the function of this is tested by add
        TestFmwk.assertTrue("abs001", ((new android.icu.math.BigDecimal("2")).abs().toString()).equals("2"));
        TestFmwk.assertTrue("abs002", ((new android.icu.math.BigDecimal("-2")).abs().toString()).equals("2"));
        TestFmwk.assertTrue("abs003", ((new android.icu.math.BigDecimal("+0.000")).abs().toString()).equals("0.000"));
        TestFmwk.assertTrue("abs004", ((new android.icu.math.BigDecimal("00.000")).abs().toString()).equals("0.000"));
        TestFmwk.assertTrue("abs005", ((new android.icu.math.BigDecimal("-0.000")).abs().toString()).equals("0.000"));
        TestFmwk.assertTrue("abs006", ((new android.icu.math.BigDecimal("+0.000")).abs(mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("abs007", ((new android.icu.math.BigDecimal("00.000")).abs(mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("abs008", ((new android.icu.math.BigDecimal("-0.000")).abs(mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("abs009", ((new android.icu.math.BigDecimal("-2000000")).abs().toString()).equals("2000000"));
        TestFmwk.assertTrue("abs010", ((new android.icu.math.BigDecimal("-2000000")).abs(mcdef).toString()).equals("2000000"));
        TestFmwk.assertTrue("abs011", ((new android.icu.math.BigDecimal("-2000000")).abs(mc6).toString()).equals("2.00000E+6"));
        TestFmwk.assertTrue("abs012", ((new android.icu.math.BigDecimal("2000000")).abs(mc6).toString()).equals("2.00000E+6"));
        TestFmwk.assertTrue("abs013", ((new android.icu.math.BigDecimal("0.2")).abs().toString()).equals("0.2"));
        TestFmwk.assertTrue("abs014", ((new android.icu.math.BigDecimal("-0.2")).abs().toString()).equals("0.2"));
        TestFmwk.assertTrue("abs015", ((new android.icu.math.BigDecimal("0.01")).abs().toString()).equals("0.01"));
        TestFmwk.assertTrue("abs016", ((new android.icu.math.BigDecimal("-0.01")).abs().toString()).equals("0.01"));
        try {
            tenlong.abs(mcld);
            flag = false;
        } catch (java.lang.ArithmeticException $19) {
            ae = $19;
            flag = (ae.getMessage()).equals("Too many digits:" + " "
                    + tenlong.toString());
        }/* checkdigits */
        TestFmwk.assertTrue("abs020", flag);
        // check lostdigits not raised if digits=0 [monadic method]
        try {
            tenlong.abs(mcld0);
            flag = true;
        } catch (java.lang.ArithmeticException $20) {
            ae = $20;
            flag = false;
        }/* checkdigits */
        TestFmwk.assertTrue("abs021", flag);
        try {
            android.icu.math.BigDecimal.TEN
                    .abs((android.icu.math.MathContext) null);
            flag = false;
        } catch (java.lang.NullPointerException $21) {
            flag = true;
        }/* checknull */
        TestFmwk.assertTrue("abs022", flag);

    }

    /* ----------------------------------------------------------------- */

    /** Test the {@link android.icu.math.BigDecimal#add} method. */

    @Test
    public void diagadd() {
        boolean flag = false;
        android.icu.math.BigDecimal alhs;
        android.icu.math.BigDecimal arhs;
        java.lang.ArithmeticException ae = null;

        // [first group are 'quick confidence check']
        TestFmwk.assertTrue("add001", ((new android.icu.math.BigDecimal(2)).add(new android.icu.math.BigDecimal(3),mcdef).toString()).equals("5"));
        TestFmwk.assertTrue("add003", ((new android.icu.math.BigDecimal("5.75")).add(new android.icu.math.BigDecimal("3.3"),mcdef).toString()).equals("9.05"));
        TestFmwk.assertTrue("add004", ((new android.icu.math.BigDecimal("5")).add(new android.icu.math.BigDecimal("-3"),mcdef).toString()).equals("2"));
        TestFmwk.assertTrue("add005", ((new android.icu.math.BigDecimal("-5")).add(new android.icu.math.BigDecimal("-3"),mcdef).toString()).equals("-8"));
        TestFmwk.assertTrue("add006", ((new android.icu.math.BigDecimal("-7")).add(new android.icu.math.BigDecimal("2.5"),mcdef).toString()).equals("-4.5"));
        TestFmwk.assertTrue("add007", ((new android.icu.math.BigDecimal("0.7")).add(new android.icu.math.BigDecimal("0.3"),mcdef).toString()).equals("1.0"));
        TestFmwk.assertTrue("add008", ((new android.icu.math.BigDecimal("1.25")).add(new android.icu.math.BigDecimal("1.25"),mcdef).toString()).equals("2.50"));
        TestFmwk.assertTrue("add009", ((new android.icu.math.BigDecimal("1.23456789")).add(new android.icu.math.BigDecimal("1.00000000"),mcdef).toString()).equals("2.23456789"));

        TestFmwk.assertTrue("add010", ((new android.icu.math.BigDecimal("1.23456789")).add(new android.icu.math.BigDecimal("1.00000011"),mcdef).toString()).equals("2.23456800"));


        TestFmwk.assertTrue("add011", ((new android.icu.math.BigDecimal("0.4444444444")).add(new android.icu.math.BigDecimal("0.5555555555"),mcdef).toString()).equals("1.00000000"));

        TestFmwk.assertTrue("add012", ((new android.icu.math.BigDecimal("0.4444444440")).add(new android.icu.math.BigDecimal("0.5555555555"),mcdef).toString()).equals("1.00000000"));

        TestFmwk.assertTrue("add013", ((new android.icu.math.BigDecimal("0.4444444444")).add(new android.icu.math.BigDecimal("0.5555555550"),mcdef).toString()).equals("0.999999999"));

        TestFmwk.assertTrue("add014", ((new android.icu.math.BigDecimal("0.4444444444999")).add(new android.icu.math.BigDecimal("0"),mcdef).toString()).equals("0.444444444"));

        TestFmwk.assertTrue("add015", ((new android.icu.math.BigDecimal("0.4444444445000")).add(new android.icu.math.BigDecimal("0"),mcdef).toString()).equals("0.444444445"));


        TestFmwk.assertTrue("add016", ((new android.icu.math.BigDecimal("70")).add(new android.icu.math.BigDecimal("10000e+9"),mcdef).toString()).equals("1.00000000E+13"));

        TestFmwk.assertTrue("add017", ((new android.icu.math.BigDecimal("700")).add(new android.icu.math.BigDecimal("10000e+9"),mcdef).toString()).equals("1.00000000E+13"));

        TestFmwk.assertTrue("add018", ((new android.icu.math.BigDecimal("7000")).add(new android.icu.math.BigDecimal("10000e+9"),mcdef).toString()).equals("1.00000000E+13"));

        TestFmwk.assertTrue("add019", ((new android.icu.math.BigDecimal("70000")).add(new android.icu.math.BigDecimal("10000e+9"),mcdef).toString()).equals("1.00000001E+13"));

        TestFmwk.assertTrue("add020", ((new android.icu.math.BigDecimal("700000")).add(new android.icu.math.BigDecimal("10000e+9"),mcdef).toString()).equals("1.00000007E+13"));


        // [Now the same group with fixed arithmetic]
        TestFmwk.assertTrue("add030", ((new android.icu.math.BigDecimal(2)).add(new android.icu.math.BigDecimal(3)).toString()).equals("5"));
        TestFmwk.assertTrue("add031", ((new android.icu.math.BigDecimal("5.75")).add(new android.icu.math.BigDecimal("3.3")).toString()).equals("9.05"));
        TestFmwk.assertTrue("add032", ((new android.icu.math.BigDecimal("5")).add(new android.icu.math.BigDecimal("-3")).toString()).equals("2"));
        TestFmwk.assertTrue("add033", ((new android.icu.math.BigDecimal("-5")).add(new android.icu.math.BigDecimal("-3")).toString()).equals("-8"));
        TestFmwk.assertTrue("add034", ((new android.icu.math.BigDecimal("-7")).add(new android.icu.math.BigDecimal("2.5")).toString()).equals("-4.5"));
        TestFmwk.assertTrue("add035", ((new android.icu.math.BigDecimal("0.7")).add(new android.icu.math.BigDecimal("0.3")).toString()).equals("1.0"));
        TestFmwk.assertTrue("add036", ((new android.icu.math.BigDecimal("1.25")).add(new android.icu.math.BigDecimal("1.25")).toString()).equals("2.50"));
        TestFmwk.assertTrue("add037", ((new android.icu.math.BigDecimal("1.23456789")).add(new android.icu.math.BigDecimal("1.00000000")).toString()).equals("2.23456789"));

        TestFmwk.assertTrue("add038", ((new android.icu.math.BigDecimal("1.23456789")).add(new android.icu.math.BigDecimal("1.00000011")).toString()).equals("2.23456800"));


        TestFmwk.assertTrue("add039", ((new android.icu.math.BigDecimal("0.4444444444")).add(new android.icu.math.BigDecimal("0.5555555555")).toString()).equals("0.9999999999"));

        TestFmwk.assertTrue("add040", ((new android.icu.math.BigDecimal("0.4444444440")).add(new android.icu.math.BigDecimal("0.5555555555")).toString()).equals("0.9999999995"));

        TestFmwk.assertTrue("add041", ((new android.icu.math.BigDecimal("0.4444444444")).add(new android.icu.math.BigDecimal("0.5555555550")).toString()).equals("0.9999999994"));

        TestFmwk.assertTrue("add042", ((new android.icu.math.BigDecimal("0.4444444444999")).add(new android.icu.math.BigDecimal("0")).toString()).equals("0.4444444444999"));

        TestFmwk.assertTrue("add043", ((new android.icu.math.BigDecimal("0.4444444445000")).add(new android.icu.math.BigDecimal("0")).toString()).equals("0.4444444445000"));


        TestFmwk.assertTrue("add044", ((new android.icu.math.BigDecimal("70")).add(new android.icu.math.BigDecimal("10000e+9")).toString()).equals("10000000000070"));

        TestFmwk.assertTrue("add045", ((new android.icu.math.BigDecimal("700")).add(new android.icu.math.BigDecimal("10000e+9")).toString()).equals("10000000000700"));

        TestFmwk.assertTrue("add046", ((new android.icu.math.BigDecimal("7000")).add(new android.icu.math.BigDecimal("10000e+9")).toString()).equals("10000000007000"));

        TestFmwk.assertTrue("add047", ((new android.icu.math.BigDecimal("70000")).add(new android.icu.math.BigDecimal("10000e+9")).toString()).equals("10000000070000"));

        TestFmwk.assertTrue("add048", ((new android.icu.math.BigDecimal("700000")).add(new android.icu.math.BigDecimal("10000e+9")).toString()).equals("10000000700000"));


        // symmetry:
        TestFmwk.assertTrue("add049", ((new android.icu.math.BigDecimal("10000e+9")).add(new android.icu.math.BigDecimal("70"),mcdef).toString()).equals("1.00000000E+13"));

        TestFmwk.assertTrue("add050", ((new android.icu.math.BigDecimal("10000e+9")).add(new android.icu.math.BigDecimal("700"),mcdef).toString()).equals("1.00000000E+13"));

        TestFmwk.assertTrue("add051", ((new android.icu.math.BigDecimal("10000e+9")).add(new android.icu.math.BigDecimal("7000"),mcdef).toString()).equals("1.00000000E+13"));

        TestFmwk.assertTrue("add052", ((new android.icu.math.BigDecimal("10000e+9")).add(new android.icu.math.BigDecimal("70000"),mcdef).toString()).equals("1.00000001E+13"));

        TestFmwk.assertTrue("add053", ((new android.icu.math.BigDecimal("10000e+9")).add(new android.icu.math.BigDecimal("700000"),mcdef).toString()).equals("1.00000007E+13"));


        TestFmwk.assertTrue("add054", ((new android.icu.math.BigDecimal("10000e+9")).add(new android.icu.math.BigDecimal("70")).toString()).equals("10000000000070"));

        TestFmwk.assertTrue("add055", ((new android.icu.math.BigDecimal("10000e+9")).add(new android.icu.math.BigDecimal("700")).toString()).equals("10000000000700"));

        TestFmwk.assertTrue("add056", ((new android.icu.math.BigDecimal("10000e+9")).add(new android.icu.math.BigDecimal("7000")).toString()).equals("10000000007000"));

        TestFmwk.assertTrue("add057", ((new android.icu.math.BigDecimal("10000e+9")).add(new android.icu.math.BigDecimal("70000")).toString()).equals("10000000070000"));

        TestFmwk.assertTrue("add058", ((new android.icu.math.BigDecimal("10000e+9")).add(new android.icu.math.BigDecimal("700000")).toString()).equals("10000000700000"));

        // some rounding effects
        TestFmwk.assertTrue("add059", ((new android.icu.math.BigDecimal("0.9998")).add(new android.icu.math.BigDecimal("0.0000")).toString()).equals("0.9998"));

        TestFmwk.assertTrue("add060", ((new android.icu.math.BigDecimal("0.9998")).add(new android.icu.math.BigDecimal("0.0001")).toString()).equals("0.9999"));

        TestFmwk.assertTrue("add061", ((new android.icu.math.BigDecimal("0.9998")).add(new android.icu.math.BigDecimal("0.0002")).toString()).equals("1.0000"));

        TestFmwk.assertTrue("add062", ((new android.icu.math.BigDecimal("0.9998")).add(new android.icu.math.BigDecimal("0.0003")).toString()).equals("1.0001"));


        // MC
        TestFmwk.assertTrue("add070", ((new android.icu.math.BigDecimal("10000e+9")).add(new android.icu.math.BigDecimal("70000"),mcfd).toString()).equals("10000000070000"));

        TestFmwk.assertTrue("add071", ((new android.icu.math.BigDecimal("10000e+9")).add(new android.icu.math.BigDecimal("70000"),mcdef).toString()).equals("1.00000001E+13"));

        TestFmwk.assertTrue("add072", ((new android.icu.math.BigDecimal("10000e+9")).add(new android.icu.math.BigDecimal("70000"),mc6).toString()).equals("1.00000E+13"));


        // zero preservation
        TestFmwk.assertTrue("add080", (android.icu.math.BigDecimal.ONE.add(new android.icu.math.BigDecimal("0.0001"),mc6).toString()).equals("1.0001"));

        TestFmwk.assertTrue("add081", (android.icu.math.BigDecimal.ONE.add(new android.icu.math.BigDecimal("0.00001"),mc6).toString()).equals("1.00001"));

        TestFmwk.assertTrue("add082", (android.icu.math.BigDecimal.ONE.add(new android.icu.math.BigDecimal("0.000001"),mc6).toString()).equals("1.00000"));

        TestFmwk.assertTrue("add083", (android.icu.math.BigDecimal.ONE.add(new android.icu.math.BigDecimal("0.0000001"),mc6).toString()).equals("1.00000"));

        TestFmwk.assertTrue("add084", (android.icu.math.BigDecimal.ONE.add(new android.icu.math.BigDecimal("0.00000001"),mc6).toString()).equals("1.00000"));


        // more fixed, LHS swaps
        TestFmwk.assertTrue("add090", ((new android.icu.math.BigDecimal("-56267E-10")).add(zero).toString()).equals("-0.0000056267"));
        TestFmwk.assertTrue("add091", ((new android.icu.math.BigDecimal("-56267E-6")).add(zero).toString()).equals("-0.056267"));
        TestFmwk.assertTrue("add092", ((new android.icu.math.BigDecimal("-56267E-5")).add(zero).toString()).equals("-0.56267"));
        TestFmwk.assertTrue("add093", ((new android.icu.math.BigDecimal("-56267E-4")).add(zero).toString()).equals("-5.6267"));
        TestFmwk.assertTrue("add094", ((new android.icu.math.BigDecimal("-56267E-3")).add(zero).toString()).equals("-56.267"));
        TestFmwk.assertTrue("add095", ((new android.icu.math.BigDecimal("-56267E-2")).add(zero).toString()).equals("-562.67"));
        TestFmwk.assertTrue("add096", ((new android.icu.math.BigDecimal("-56267E-1")).add(zero).toString()).equals("-5626.7"));
        TestFmwk.assertTrue("add097", ((new android.icu.math.BigDecimal("-56267E-0")).add(zero).toString()).equals("-56267"));
        TestFmwk.assertTrue("add098", ((new android.icu.math.BigDecimal("-5E-10")).add(zero).toString()).equals("-0.0000000005"));
        TestFmwk.assertTrue("add099", ((new android.icu.math.BigDecimal("-5E-5")).add(zero).toString()).equals("-0.00005"));
        TestFmwk.assertTrue("add100", ((new android.icu.math.BigDecimal("-5E-1")).add(zero).toString()).equals("-0.5"));
        TestFmwk.assertTrue("add101", ((new android.icu.math.BigDecimal("-5E-10")).add(zero).toString()).equals("-0.0000000005"));
        TestFmwk.assertTrue("add102", ((new android.icu.math.BigDecimal("-5E-5")).add(zero).toString()).equals("-0.00005"));
        TestFmwk.assertTrue("add103", ((new android.icu.math.BigDecimal("-5E-1")).add(zero).toString()).equals("-0.5"));
        TestFmwk.assertTrue("add104", ((new android.icu.math.BigDecimal("-5E10")).add(zero).toString()).equals("-50000000000"));
        TestFmwk.assertTrue("add105", ((new android.icu.math.BigDecimal("-5E5")).add(zero).toString()).equals("-500000"));
        TestFmwk.assertTrue("add106", ((new android.icu.math.BigDecimal("-5E1")).add(zero).toString()).equals("-50"));
        TestFmwk.assertTrue("add107", ((new android.icu.math.BigDecimal("-5E0")).add(zero).toString()).equals("-5"));

        // more fixed, RHS swaps
        TestFmwk.assertTrue("add108", (zero.add(new android.icu.math.BigDecimal("-56267E-10")).toString()).equals("-0.0000056267"));
        TestFmwk.assertTrue("add109", (zero.add(new android.icu.math.BigDecimal("-56267E-6")).toString()).equals("-0.056267"));
        TestFmwk.assertTrue("add110", (zero.add(new android.icu.math.BigDecimal("-56267E-5")).toString()).equals("-0.56267"));
        TestFmwk.assertTrue("add111", (zero.add(new android.icu.math.BigDecimal("-56267E-4")).toString()).equals("-5.6267"));
        TestFmwk.assertTrue("add112", (zero.add(new android.icu.math.BigDecimal("-56267E-3")).toString()).equals("-56.267"));
        TestFmwk.assertTrue("add113", (zero.add(new android.icu.math.BigDecimal("-56267E-2")).toString()).equals("-562.67"));
        TestFmwk.assertTrue("add114", (zero.add(new android.icu.math.BigDecimal("-56267E-1")).toString()).equals("-5626.7"));
        TestFmwk.assertTrue("add115", (zero.add(new android.icu.math.BigDecimal("-56267E-0")).toString()).equals("-56267"));
        TestFmwk.assertTrue("add116", (zero.add(new android.icu.math.BigDecimal("-5E-10")).toString()).equals("-0.0000000005"));
        TestFmwk.assertTrue("add117", (zero.add(new android.icu.math.BigDecimal("-5E-5")).toString()).equals("-0.00005"));
        TestFmwk.assertTrue("add118", (zero.add(new android.icu.math.BigDecimal("-5E-1")).toString()).equals("-0.5"));
        TestFmwk.assertTrue("add129", (zero.add(new android.icu.math.BigDecimal("-5E-10")).toString()).equals("-0.0000000005"));
        TestFmwk.assertTrue("add130", (zero.add(new android.icu.math.BigDecimal("-5E-5")).toString()).equals("-0.00005"));
        TestFmwk.assertTrue("add131", (zero.add(new android.icu.math.BigDecimal("-5E-1")).toString()).equals("-0.5"));
        TestFmwk.assertTrue("add132", (zero.add(new android.icu.math.BigDecimal("-5E10")).toString()).equals("-50000000000"));
        TestFmwk.assertTrue("add133", (zero.add(new android.icu.math.BigDecimal("-5E5")).toString()).equals("-500000"));
        TestFmwk.assertTrue("add134", (zero.add(new android.icu.math.BigDecimal("-5E1")).toString()).equals("-50"));
        TestFmwk.assertTrue("add135", (zero.add(new android.icu.math.BigDecimal("-5E0")).toString()).equals("-5"));

        // [some of the next group are really constructor tests]
        TestFmwk.assertTrue("add140", ((new android.icu.math.BigDecimal("00.0")).add(new android.icu.math.BigDecimal("0.00"),mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("add141", ((new android.icu.math.BigDecimal("0.00")).add(new android.icu.math.BigDecimal("00.0"),mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("add142", ((new android.icu.math.BigDecimal("3")).add(new android.icu.math.BigDecimal(".3"),mcdef).toString()).equals("3.3"));
        TestFmwk.assertTrue("add143", ((new android.icu.math.BigDecimal("3.")).add(new android.icu.math.BigDecimal(".3"),mcdef).toString()).equals("3.3"));
        TestFmwk.assertTrue("add144", ((new android.icu.math.BigDecimal("3.0")).add(new android.icu.math.BigDecimal(".3"),mcdef).toString()).equals("3.3"));
        TestFmwk.assertTrue("add145", ((new android.icu.math.BigDecimal("3.00")).add(new android.icu.math.BigDecimal(".3"),mcdef).toString()).equals("3.30"));
        TestFmwk.assertTrue("add146", ((new android.icu.math.BigDecimal("3")).add(new android.icu.math.BigDecimal("3"),mcdef).toString()).equals("6"));
        TestFmwk.assertTrue("add147", ((new android.icu.math.BigDecimal("3")).add(new android.icu.math.BigDecimal("+3"),mcdef).toString()).equals("6"));
        TestFmwk.assertTrue("add148", ((new android.icu.math.BigDecimal("3")).add(new android.icu.math.BigDecimal("-3"),mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("add149", ((new android.icu.math.BigDecimal("0.03")).add(new android.icu.math.BigDecimal("-0.03"),mcdef).toString()).equals("0"));

        TestFmwk.assertTrue("add150", ((new android.icu.math.BigDecimal("00.0")).add(new android.icu.math.BigDecimal("0.00")).toString()).equals("0.00"));
        TestFmwk.assertTrue("add151", ((new android.icu.math.BigDecimal("0.00")).add(new android.icu.math.BigDecimal("00.0")).toString()).equals("0.00"));
        TestFmwk.assertTrue("add152", ((new android.icu.math.BigDecimal("3")).add(new android.icu.math.BigDecimal(".3")).toString()).equals("3.3"));
        TestFmwk.assertTrue("add153", ((new android.icu.math.BigDecimal("3.")).add(new android.icu.math.BigDecimal(".3")).toString()).equals("3.3"));
        TestFmwk.assertTrue("add154", ((new android.icu.math.BigDecimal("3.0")).add(new android.icu.math.BigDecimal(".3")).toString()).equals("3.3"));
        TestFmwk.assertTrue("add155", ((new android.icu.math.BigDecimal("3.00")).add(new android.icu.math.BigDecimal(".3")).toString()).equals("3.30"));
        TestFmwk.assertTrue("add156", ((new android.icu.math.BigDecimal("3")).add(new android.icu.math.BigDecimal("3")).toString()).equals("6"));
        TestFmwk.assertTrue("add157", ((new android.icu.math.BigDecimal("3")).add(new android.icu.math.BigDecimal("+3")).toString()).equals("6"));
        TestFmwk.assertTrue("add158", ((new android.icu.math.BigDecimal("3")).add(new android.icu.math.BigDecimal("-3")).toString()).equals("0"));
        TestFmwk.assertTrue("add159", ((new android.icu.math.BigDecimal("0.3")).add(new android.icu.math.BigDecimal("-0.3")).toString()).equals("0.0"));
        TestFmwk.assertTrue("add160", ((new android.icu.math.BigDecimal("0.03")).add(new android.icu.math.BigDecimal("-0.03")).toString()).equals("0.00"));
        TestFmwk.assertTrue("add161", ((new android.icu.math.BigDecimal("7E+12")).add(new android.icu.math.BigDecimal("-1"),mcfd).toString()).equals("6999999999999"));

        TestFmwk.assertTrue("add162", ((new android.icu.math.BigDecimal("7E+12")).add(new android.icu.math.BigDecimal("1.11"),mcfd).toString()).equals("7000000000001.11"));

        TestFmwk.assertTrue("add163", ((new android.icu.math.BigDecimal("1.11")).add(new android.icu.math.BigDecimal("7E+12"),mcfd).toString()).equals("7000000000001.11"));


        // input preparation tests
        alhs=new android.icu.math.BigDecimal("12345678900000");
        arhs=new android.icu.math.BigDecimal("9999999999999");
        TestFmwk.assertTrue("add170", (alhs.add(arhs,mc3).toString()).equals("2.23E+13"));
        TestFmwk.assertTrue("add171", (arhs.add(alhs,mc3).toString()).equals("2.23E+13"));
        TestFmwk.assertTrue("add172", ((new android.icu.math.BigDecimal("12E+3")).add(new android.icu.math.BigDecimal("3456"),mc3).toString()).equals("1.55E+4"));
        // next was 1.54E+4 under old [truncate to digits+1] rules
        TestFmwk.assertTrue("add173", ((new android.icu.math.BigDecimal("12E+3")).add(new android.icu.math.BigDecimal("3446"),mc3).toString()).equals("1.55E+4"));
        TestFmwk.assertTrue("add174", ((new android.icu.math.BigDecimal("12E+3")).add(new android.icu.math.BigDecimal("3454"),mc3).toString()).equals("1.55E+4"));
        TestFmwk.assertTrue("add175", ((new android.icu.math.BigDecimal("12E+3")).add(new android.icu.math.BigDecimal("3444"),mc3).toString()).equals("1.54E+4"));

        TestFmwk.assertTrue("add176", ((new android.icu.math.BigDecimal("3456")).add(new android.icu.math.BigDecimal("12E+3"),mc3).toString()).equals("1.55E+4"));
        // next was 1.54E+4 under old [truncate to digits+1] rules
        TestFmwk.assertTrue("add177", ((new android.icu.math.BigDecimal("3446")).add(new android.icu.math.BigDecimal("12E+3"),mc3).toString()).equals("1.55E+4"));
        TestFmwk.assertTrue("add178", ((new android.icu.math.BigDecimal("3454")).add(new android.icu.math.BigDecimal("12E+3"),mc3).toString()).equals("1.55E+4"));
        TestFmwk.assertTrue("add179", ((new android.icu.math.BigDecimal("3444")).add(new android.icu.math.BigDecimal("12E+3"),mc3).toString()).equals("1.54E+4"));

        try {
            ten.add((android.icu.math.BigDecimal) null);
            flag = false;
        } catch (java.lang.NullPointerException $22) {
            flag = true;
        }/* checknull */
        TestFmwk.assertTrue("add200", flag);
        try {
            ten.add(ten, (android.icu.math.MathContext) null);
            flag = false;
        } catch (java.lang.NullPointerException $23) {
            flag = true;
        }/* checknull2 */
        TestFmwk.assertTrue("add201", flag);

        try {
            tenlong.add(android.icu.math.BigDecimal.ZERO, mcld);
            flag = false;
        } catch (java.lang.ArithmeticException $24) {
            ae = $24;
            flag = (ae.getMessage()).equals("Too many digits:" + " "
                    + tenlong.toString());
        }/* checkdigits */
        TestFmwk.assertTrue("add202", flag);
        try {
            android.icu.math.BigDecimal.ZERO.add(tenlong, mcld);
            flag = false;
        } catch (java.lang.ArithmeticException $25) {
            ae = $25;
            flag = (ae.getMessage()).equals("Too many digits:" + " "
                    + tenlong.toString());
        }/* checkdigits */
        TestFmwk.assertTrue("add203", flag);

        // check lostdigits not raised if digits=0 [dyadic method]
        try {
            tenlong.add(android.icu.math.BigDecimal.ZERO, mcld0);
            flag = true;
        } catch (java.lang.ArithmeticException $26) {
            ae = $26;
            flag = false;
        }/* checkdigits */
        TestFmwk.assertTrue("add204", flag);
        try {
            android.icu.math.BigDecimal.ZERO.add(tenlong, mcld0);
            flag = true;
        } catch (java.lang.ArithmeticException $27) {
            ae = $27;
            flag = false;
        }/* checkdigits */
        TestFmwk.assertTrue("add205", flag);

    }

    /* ----------------------------------------------------------------- */
    /**
     * Test the {@link android.icu.math.BigDecimal#compareTo(BigDecimal)}
     * method.
     */

    @Test
    public void diagcompareto() {
        boolean flag = false;
        java.lang.ArithmeticException ae = null;
        // we assume add/subtract test function; this just
        // tests existence, exceptions, and possible results

        TestFmwk.assertTrue("cpt001", ((new android.icu.math.BigDecimal("5")).compareTo(new android.icu.math.BigDecimal("2")))==1);
        TestFmwk.assertTrue("cpt002", ((new android.icu.math.BigDecimal("5")).compareTo(new android.icu.math.BigDecimal("5")))==0);
        TestFmwk.assertTrue("cpt003", ((new android.icu.math.BigDecimal("5")).compareTo(new android.icu.math.BigDecimal("5.00")))==0);
        TestFmwk.assertTrue("cpt004", ((new android.icu.math.BigDecimal("0.5")).compareTo(new android.icu.math.BigDecimal("0.5")))==0);
        TestFmwk.assertTrue("cpt005", ((new android.icu.math.BigDecimal("2")).compareTo(new android.icu.math.BigDecimal("5")))==(-1));
        TestFmwk.assertTrue("cpt006", ((new android.icu.math.BigDecimal("2")).compareTo(new android.icu.math.BigDecimal("5"),mcdef))==(-1));
        TestFmwk.assertTrue("cpt007", ((new android.icu.math.BigDecimal("2")).compareTo(new android.icu.math.BigDecimal("5"),mc6))==(-1));
        TestFmwk.assertTrue("cpt008", ((new android.icu.math.BigDecimal("2")).compareTo(new android.icu.math.BigDecimal("5"),mcfd))==(-1));
        try {
            ten.compareTo((android.icu.math.BigDecimal) null);
            flag = false;
        } catch (java.lang.NullPointerException $28) {
            flag = true;
        }/* checknull */
        TestFmwk.assertTrue("cpt100", flag);
        try {
            ten.compareTo(ten, (android.icu.math.MathContext) null);
            flag = false;
        } catch (java.lang.NullPointerException $29) {
            flag = true;
        }/* checknull2 */
        TestFmwk.assertTrue("cpt101", flag);

        try {
            tenlong.compareTo(android.icu.math.BigDecimal.ONE, mcld);
            flag = false;
        } catch (java.lang.ArithmeticException $30) {
            ae = $30;
            flag = (ae.getMessage()).equals("Too many digits:" + " "
                    + tenlong.toString());
        }/* checkdigits */
        TestFmwk.assertTrue("cpt102", flag);
        try {
            android.icu.math.BigDecimal.ONE.compareTo(tenlong, mcld);
            flag = false;
        } catch (java.lang.ArithmeticException $31) {
            ae = $31;
            flag = (ae.getMessage()).equals("Too many digits:" + " "
                    + tenlong.toString());
        }/* checkdigits */
        TestFmwk.assertTrue("cpt103", flag);

    }

    /* ----------------------------------------------------------------- */

    /** Test the {@link android.icu.math.BigDecimal#divide} method. */

    @Test
    public void diagdivide() {
        boolean flag = false;
        android.icu.math.MathContext rmcd;
        int rhu;
        int rd;
        int ru;
        java.lang.RuntimeException e = null;
        java.lang.ArithmeticException ae = null;

        TestFmwk.assertTrue("div301", ((new android.icu.math.BigDecimal("1")).divide(new android.icu.math.BigDecimal("3"),mcdef).toString()).equals("0.333333333"));
        TestFmwk.assertTrue("div302", ((new android.icu.math.BigDecimal("2")).divide(new android.icu.math.BigDecimal("3"),mcdef).toString()).equals("0.666666667"));
        TestFmwk.assertTrue("div303", ((new android.icu.math.BigDecimal("2.4")).divide(new android.icu.math.BigDecimal("1"),mcdef).toString()).equals("2.4"));
        TestFmwk.assertTrue("div304", ((new android.icu.math.BigDecimal("2.4")).divide(new android.icu.math.BigDecimal("-1"),mcdef).toString()).equals("-2.4"));
        TestFmwk.assertTrue("div305", ((new android.icu.math.BigDecimal("-2.4")).divide(new android.icu.math.BigDecimal("1"),mcdef).toString()).equals("-2.4"));
        TestFmwk.assertTrue("div306", ((new android.icu.math.BigDecimal("-2.4")).divide(new android.icu.math.BigDecimal("-1"),mcdef).toString()).equals("2.4"));
        TestFmwk.assertTrue("div307", ((new android.icu.math.BigDecimal("2.40")).divide(new android.icu.math.BigDecimal("1"),mcdef).toString()).equals("2.4"));
        TestFmwk.assertTrue("div308", ((new android.icu.math.BigDecimal("2.400")).divide(new android.icu.math.BigDecimal("1"),mcdef).toString()).equals("2.4"));
        TestFmwk.assertTrue("div309", ((new android.icu.math.BigDecimal("2.4")).divide(new android.icu.math.BigDecimal("2"),mcdef).toString()).equals("1.2"));
        TestFmwk.assertTrue("div310", ((new android.icu.math.BigDecimal("2.400")).divide(new android.icu.math.BigDecimal("2"),mcdef).toString()).equals("1.2"));
        TestFmwk.assertTrue("div311", ((new android.icu.math.BigDecimal("2.")).divide(new android.icu.math.BigDecimal("2"),mcdef).toString()).equals("1"));
        TestFmwk.assertTrue("div312", ((new android.icu.math.BigDecimal("20")).divide(new android.icu.math.BigDecimal("20"),mcdef).toString()).equals("1"));
        TestFmwk.assertTrue("div313", ((new android.icu.math.BigDecimal("187")).divide(new android.icu.math.BigDecimal("187"),mcdef).toString()).equals("1"));
        TestFmwk.assertTrue("div314", ((new android.icu.math.BigDecimal("5")).divide(new android.icu.math.BigDecimal("2"),mcdef).toString()).equals("2.5"));
        TestFmwk.assertTrue("div315", ((new android.icu.math.BigDecimal("5")).divide(new android.icu.math.BigDecimal("2.0"),mcdef).toString()).equals("2.5"));
        TestFmwk.assertTrue("div316", ((new android.icu.math.BigDecimal("5")).divide(new android.icu.math.BigDecimal("2.000"),mcdef).toString()).equals("2.5"));
        TestFmwk.assertTrue("div317", ((new android.icu.math.BigDecimal("5")).divide(new android.icu.math.BigDecimal("0.200"),mcdef).toString()).equals("25"));
        TestFmwk.assertTrue("div318", ((new android.icu.math.BigDecimal("999999999")).divide(new android.icu.math.BigDecimal("1"),mcdef).toString()).equals("999999999"));
        TestFmwk.assertTrue("div319", ((new android.icu.math.BigDecimal("999999999.4")).divide(new android.icu.math.BigDecimal("1"),mcdef).toString()).equals("999999999"));
        TestFmwk.assertTrue("div320", ((new android.icu.math.BigDecimal("999999999.5")).divide(new android.icu.math.BigDecimal("1"),mcdef).toString()).equals("1E+9"));
        TestFmwk.assertTrue("div321", ((new android.icu.math.BigDecimal("999999999.9")).divide(new android.icu.math.BigDecimal("1"),mcdef).toString()).equals("1E+9"));
        TestFmwk.assertTrue("div322", ((new android.icu.math.BigDecimal("999999999.999")).divide(new android.icu.math.BigDecimal("1"),mcdef).toString()).equals("1E+9"));
        TestFmwk.assertTrue("div323", ((new android.icu.math.BigDecimal("0.0000E-50")).divide(new android.icu.math.BigDecimal("1"),mcdef).toString()).equals("0"));
        // MC
        TestFmwk.assertTrue("div325", ((new android.icu.math.BigDecimal("999999999")).divide(new android.icu.math.BigDecimal("1"),mcdef).toString()).equals("999999999"));
        TestFmwk.assertTrue("div326", ((new android.icu.math.BigDecimal("999999999")).divide(new android.icu.math.BigDecimal("1"),mc6).toString()).equals("1E+9"));
        TestFmwk.assertTrue("div327", ((new android.icu.math.BigDecimal("9999999")).divide(new android.icu.math.BigDecimal("1"),mc6).toString()).equals("1E+7"));
        TestFmwk.assertTrue("div328", ((new android.icu.math.BigDecimal("999999")).divide(new android.icu.math.BigDecimal("1"),mc6).toString()).equals("999999"));

        // check rounding explicitly [note: digits+1 truncation]
        rmcd=new android.icu.math.MathContext(2,android.icu.math.MathContext.SCIENTIFIC,false,android.icu.math.MathContext.ROUND_CEILING);
        TestFmwk.assertTrue("div330", ((new android.icu.math.BigDecimal("1.50")).divide(one,rmcd).toString()).equals("1.5"));
        TestFmwk.assertTrue("div331", ((new android.icu.math.BigDecimal("1.51")).divide(one,rmcd).toString()).equals("1.6"));
        TestFmwk.assertTrue("div332", ((new android.icu.math.BigDecimal("1.55")).divide(one,rmcd).toString()).equals("1.6"));
        rmcd=new android.icu.math.MathContext(2,android.icu.math.MathContext.SCIENTIFIC,false,android.icu.math.MathContext.ROUND_DOWN);
        TestFmwk.assertTrue("div333", ((new android.icu.math.BigDecimal("1.55")).divide(one,rmcd).toString()).equals("1.5"));
        TestFmwk.assertTrue("div334", ((new android.icu.math.BigDecimal("1.59")).divide(one,rmcd).toString()).equals("1.5"));
        rmcd=new android.icu.math.MathContext(2,android.icu.math.MathContext.SCIENTIFIC,false,android.icu.math.MathContext.ROUND_FLOOR);
        TestFmwk.assertTrue("div335", ((new android.icu.math.BigDecimal("1.55")).divide(one,rmcd).toString()).equals("1.5"));
        TestFmwk.assertTrue("div336", ((new android.icu.math.BigDecimal("1.59")).divide(one,rmcd).toString()).equals("1.5"));
        rmcd=new android.icu.math.MathContext(2,android.icu.math.MathContext.SCIENTIFIC,false,android.icu.math.MathContext.ROUND_HALF_DOWN);
        TestFmwk.assertTrue("div337", ((new android.icu.math.BigDecimal("1.45")).divide(one,rmcd).toString()).equals("1.4"));
        TestFmwk.assertTrue("div338", ((new android.icu.math.BigDecimal("1.50")).divide(one,rmcd).toString()).equals("1.5"));
        TestFmwk.assertTrue("div339", ((new android.icu.math.BigDecimal("1.55")).divide(one,rmcd).toString()).equals("1.5"));
        rmcd=new android.icu.math.MathContext(2,android.icu.math.MathContext.SCIENTIFIC,false,android.icu.math.MathContext.ROUND_HALF_EVEN);
        TestFmwk.assertTrue("div340", ((new android.icu.math.BigDecimal("1.45")).divide(one,rmcd).toString()).equals("1.4"));
        TestFmwk.assertTrue("div341", ((new android.icu.math.BigDecimal("1.50")).divide(one,rmcd).toString()).equals("1.5"));
        TestFmwk.assertTrue("div342", ((new android.icu.math.BigDecimal("1.55")).divide(one,rmcd).toString()).equals("1.6"));
        rmcd=new android.icu.math.MathContext(2,android.icu.math.MathContext.SCIENTIFIC,false,android.icu.math.MathContext.ROUND_HALF_UP);
        TestFmwk.assertTrue("div343", ((new android.icu.math.BigDecimal("1.45")).divide(one,rmcd).toString()).equals("1.5"));
        TestFmwk.assertTrue("div344", ((new android.icu.math.BigDecimal("1.50")).divide(one,rmcd).toString()).equals("1.5"));
        TestFmwk.assertTrue("div345", ((new android.icu.math.BigDecimal("1.55")).divide(one,rmcd).toString()).equals("1.6"));
        rmcd=new android.icu.math.MathContext(2,android.icu.math.MathContext.SCIENTIFIC,false,android.icu.math.MathContext.ROUND_UP);
        TestFmwk.assertTrue("div346", ((new android.icu.math.BigDecimal("1.50")).divide(one,rmcd).toString()).equals("1.5"));
        TestFmwk.assertTrue("div347", ((new android.icu.math.BigDecimal("1.51")).divide(one,rmcd).toString()).equals("1.6"));
        TestFmwk.assertTrue("div348", ((new android.icu.math.BigDecimal("1.55")).divide(one,rmcd).toString()).equals("1.6"));

        // fixed point...
        TestFmwk.assertTrue("div350", ((new android.icu.math.BigDecimal("1")).divide(new android.icu.math.BigDecimal("3")).toString()).equals("0"));
        TestFmwk.assertTrue("div351", ((new android.icu.math.BigDecimal("2")).divide(new android.icu.math.BigDecimal("3")).toString()).equals("1"));
        TestFmwk.assertTrue("div352", ((new android.icu.math.BigDecimal("2.4")).divide(new android.icu.math.BigDecimal("1")).toString()).equals("2.4"));
        TestFmwk.assertTrue("div353", ((new android.icu.math.BigDecimal("2.4")).divide(new android.icu.math.BigDecimal("-1")).toString()).equals("-2.4"));
        TestFmwk.assertTrue("div354", ((new android.icu.math.BigDecimal("-2.4")).divide(new android.icu.math.BigDecimal("1")).toString()).equals("-2.4"));
        TestFmwk.assertTrue("div355", ((new android.icu.math.BigDecimal("-2.4")).divide(new android.icu.math.BigDecimal("-1")).toString()).equals("2.4"));
        TestFmwk.assertTrue("div356", ((new android.icu.math.BigDecimal("2.40")).divide(new android.icu.math.BigDecimal("1")).toString()).equals("2.40"));
        TestFmwk.assertTrue("div357", ((new android.icu.math.BigDecimal("2.400")).divide(new android.icu.math.BigDecimal("1")).toString()).equals("2.400"));
        TestFmwk.assertTrue("div358", ((new android.icu.math.BigDecimal("2.4")).divide(new android.icu.math.BigDecimal("2")).toString()).equals("1.2"));
        TestFmwk.assertTrue("div359", ((new android.icu.math.BigDecimal("2.400")).divide(new android.icu.math.BigDecimal("2")).toString()).equals("1.200"));
        TestFmwk.assertTrue("div360", ((new android.icu.math.BigDecimal("2.")).divide(new android.icu.math.BigDecimal("2")).toString()).equals("1"));
        TestFmwk.assertTrue("div361", ((new android.icu.math.BigDecimal("20")).divide(new android.icu.math.BigDecimal("20")).toString()).equals("1"));
        TestFmwk.assertTrue("div362", ((new android.icu.math.BigDecimal("187")).divide(new android.icu.math.BigDecimal("187")).toString()).equals("1"));
        TestFmwk.assertTrue("div363", ((new android.icu.math.BigDecimal("5")).divide(new android.icu.math.BigDecimal("2")).toString()).equals("3"));
        TestFmwk.assertTrue("div364", ((new android.icu.math.BigDecimal("5")).divide(new android.icu.math.BigDecimal("2.0")).toString()).equals("3"));
        TestFmwk.assertTrue("div365", ((new android.icu.math.BigDecimal("5")).divide(new android.icu.math.BigDecimal("2.000")).toString()).equals("3"));
        TestFmwk.assertTrue("div366", ((new android.icu.math.BigDecimal("5")).divide(new android.icu.math.BigDecimal("0.200")).toString()).equals("25"));
        TestFmwk.assertTrue("div367", ((new android.icu.math.BigDecimal("5.0")).divide(new android.icu.math.BigDecimal("2")).toString()).equals("2.5"));
        TestFmwk.assertTrue("div368", ((new android.icu.math.BigDecimal("5.0")).divide(new android.icu.math.BigDecimal("2.0")).toString()).equals("2.5"));
        TestFmwk.assertTrue("div369", ((new android.icu.math.BigDecimal("5.0")).divide(new android.icu.math.BigDecimal("2.000")).toString()).equals("2.5"));
        TestFmwk.assertTrue("div370", ((new android.icu.math.BigDecimal("5.0")).divide(new android.icu.math.BigDecimal("0.200")).toString()).equals("25.0"));
        TestFmwk.assertTrue("div371", ((new android.icu.math.BigDecimal("999999999")).divide(new android.icu.math.BigDecimal("1")).toString()).equals("999999999"));
        TestFmwk.assertTrue("div372", ((new android.icu.math.BigDecimal("999999999.4")).divide(new android.icu.math.BigDecimal("1")).toString()).equals("999999999.4"));
        TestFmwk.assertTrue("div373", ((new android.icu.math.BigDecimal("999999999.5")).divide(new android.icu.math.BigDecimal("1")).toString()).equals("999999999.5"));
        TestFmwk.assertTrue("div374", ((new android.icu.math.BigDecimal("999999999.9")).divide(new android.icu.math.BigDecimal("1")).toString()).equals("999999999.9"));
        TestFmwk.assertTrue("div375", ((new android.icu.math.BigDecimal("999999999.999")).divide(new android.icu.math.BigDecimal("1")).toString()).equals("999999999.999"));
        TestFmwk.assertTrue("div376", ((new android.icu.math.BigDecimal("0.0000E-5")).divide(new android.icu.math.BigDecimal("1")).toString()).equals("0"));
        TestFmwk.assertTrue("div377", ((new android.icu.math.BigDecimal("0.000000000")).divide(new android.icu.math.BigDecimal("1")).toString()).equals("0.000000000"));

        // - Fixed point; explicit scales & rounds [old BigDecimal divides]
        rhu = android.icu.math.MathContext.ROUND_HALF_UP;
        rd = android.icu.math.MathContext.ROUND_DOWN;
        TestFmwk.assertTrue("div001", ((new android.icu.math.BigDecimal("0")).divide(new android.icu.math.BigDecimal("3")).toString()).equals("0"));
        TestFmwk.assertTrue("div002", ((new android.icu.math.BigDecimal("0")).divide(new android.icu.math.BigDecimal("3"),rhu).toString()).equals("0"));
        TestFmwk.assertTrue("div003", ((new android.icu.math.BigDecimal("0")).divide(new android.icu.math.BigDecimal("3"),0,rhu).toString()).equals("0"));
        TestFmwk.assertTrue("div004", ((new android.icu.math.BigDecimal("0")).divide(new android.icu.math.BigDecimal("3"),1,rhu).toString()).equals("0.0"));
        TestFmwk.assertTrue("div005", ((new android.icu.math.BigDecimal("0")).divide(new android.icu.math.BigDecimal("3"),2,rhu).toString()).equals("0.00"));
        TestFmwk.assertTrue("div006", ((new android.icu.math.BigDecimal("0")).divide(new android.icu.math.BigDecimal("3"),3,rhu).toString()).equals("0.000"));
        TestFmwk.assertTrue("div007", ((new android.icu.math.BigDecimal("0")).divide(new android.icu.math.BigDecimal("3"),4,rhu).toString()).equals("0.0000"));
        TestFmwk.assertTrue("div008", ((new android.icu.math.BigDecimal("1")).divide(new android.icu.math.BigDecimal("3")).toString()).equals("0"));
        TestFmwk.assertTrue("div009", ((new android.icu.math.BigDecimal("1")).divide(new android.icu.math.BigDecimal("3"),rhu).toString()).equals("0"));
        TestFmwk.assertTrue("div010", ((new android.icu.math.BigDecimal("1")).divide(new android.icu.math.BigDecimal("3"),0,rhu).toString()).equals("0"));
        TestFmwk.assertTrue("div011", ((new android.icu.math.BigDecimal("1")).divide(new android.icu.math.BigDecimal("3"),1,rhu).toString()).equals("0.3"));
        TestFmwk.assertTrue("div012", ((new android.icu.math.BigDecimal("1")).divide(new android.icu.math.BigDecimal("3"),2,rhu).toString()).equals("0.33"));
        TestFmwk.assertTrue("div013", ((new android.icu.math.BigDecimal("1")).divide(new android.icu.math.BigDecimal("3"),3,rhu).toString()).equals("0.333"));
        TestFmwk.assertTrue("div014", ((new android.icu.math.BigDecimal("1")).divide(new android.icu.math.BigDecimal("3"),4,rhu).toString()).equals("0.3333"));
        TestFmwk.assertTrue("div015", ((new android.icu.math.BigDecimal("2")).divide(new android.icu.math.BigDecimal("3")).toString()).equals("1"));
        TestFmwk.assertTrue("div016", ((new android.icu.math.BigDecimal("2")).divide(new android.icu.math.BigDecimal("3"),rhu).toString()).equals("1"));
        TestFmwk.assertTrue("div017", ((new android.icu.math.BigDecimal("2")).divide(new android.icu.math.BigDecimal("3"),0,rhu).toString()).equals("1"));
        TestFmwk.assertTrue("div018", ((new android.icu.math.BigDecimal("2")).divide(new android.icu.math.BigDecimal("3"),1,rhu).toString()).equals("0.7"));
        TestFmwk.assertTrue("div019", ((new android.icu.math.BigDecimal("2")).divide(new android.icu.math.BigDecimal("3"),2,rhu).toString()).equals("0.67"));
        TestFmwk.assertTrue("div020", ((new android.icu.math.BigDecimal("2")).divide(new android.icu.math.BigDecimal("3"),3,rhu).toString()).equals("0.667"));
        TestFmwk.assertTrue("div021", ((new android.icu.math.BigDecimal("2")).divide(new android.icu.math.BigDecimal("3"),4,rhu).toString()).equals("0.6667"));

        TestFmwk.assertTrue("div030", ((new android.icu.math.BigDecimal("1000")).divide(new android.icu.math.BigDecimal("2000"),4,rhu).toString()).equals("0.5000"));
        TestFmwk.assertTrue("div031", ((new android.icu.math.BigDecimal("1000")).divide(new android.icu.math.BigDecimal("2000"),3,rhu).toString()).equals("0.500"));
        TestFmwk.assertTrue("div032", ((new android.icu.math.BigDecimal("1000")).divide(new android.icu.math.BigDecimal("2000"),2,rhu).toString()).equals("0.50"));
        TestFmwk.assertTrue("div033", ((new android.icu.math.BigDecimal("1000")).divide(new android.icu.math.BigDecimal("2000"),1,rhu).toString()).equals("0.5"));
        TestFmwk.assertTrue("div034", ((new android.icu.math.BigDecimal("1000")).divide(new android.icu.math.BigDecimal("2000"),0,rhu).toString()).equals("1"));

        TestFmwk.assertTrue("div035", ((new android.icu.math.BigDecimal("100")).divide(new android.icu.math.BigDecimal("5000"),4,rhu).toString()).equals("0.0200"));
        TestFmwk.assertTrue("div036", ((new android.icu.math.BigDecimal("100")).divide(new android.icu.math.BigDecimal("5000"),3,rhu).toString()).equals("0.020"));
        TestFmwk.assertTrue("div037", ((new android.icu.math.BigDecimal("100")).divide(new android.icu.math.BigDecimal("5000"),2,rhu).toString()).equals("0.02"));
        TestFmwk.assertTrue("div038", ((new android.icu.math.BigDecimal("100")).divide(new android.icu.math.BigDecimal("5000"),1,rhu).toString()).equals("0.0"));
        TestFmwk.assertTrue("div039", ((new android.icu.math.BigDecimal("100")).divide(new android.icu.math.BigDecimal("5000"),0,rhu).toString()).equals("0"));

        TestFmwk.assertTrue("div040", ((new android.icu.math.BigDecimal("9.99999999")).divide(new android.icu.math.BigDecimal("9.77777777"),4,rhu).toString()).equals("1.0227"));
        TestFmwk.assertTrue("div041", ((new android.icu.math.BigDecimal("9.9999999")).divide(new android.icu.math.BigDecimal("9.7777777"),4,rhu).toString()).equals("1.0227"));
        TestFmwk.assertTrue("div042", ((new android.icu.math.BigDecimal("9.999999")).divide(new android.icu.math.BigDecimal("9.777777"),4,rhu).toString()).equals("1.0227"));
        TestFmwk.assertTrue("div043", ((new android.icu.math.BigDecimal("9.77777777")).divide(new android.icu.math.BigDecimal("9.99999999"),4,rhu).toString()).equals("0.9778"));
        TestFmwk.assertTrue("div044", ((new android.icu.math.BigDecimal("9.7777777")).divide(new android.icu.math.BigDecimal("9.9999999"),4,rhu).toString()).equals("0.9778"));
        TestFmwk.assertTrue("div045", ((new android.icu.math.BigDecimal("9.777777")).divide(new android.icu.math.BigDecimal("9.999999"),4,rhu).toString()).equals("0.9778"));
        TestFmwk.assertTrue("div046", ((new android.icu.math.BigDecimal("9.77777")).divide(new android.icu.math.BigDecimal("9.99999"),4,rhu).toString()).equals("0.9778"));
        TestFmwk.assertTrue("div047", ((new android.icu.math.BigDecimal("9.7777")).divide(new android.icu.math.BigDecimal("9.9999"),4,rhu).toString()).equals("0.9778"));
        TestFmwk.assertTrue("div048", ((new android.icu.math.BigDecimal("9.777")).divide(new android.icu.math.BigDecimal("9.999"),4,rhu).toString()).equals("0.9778"));
        TestFmwk.assertTrue("div049", ((new android.icu.math.BigDecimal("9.77")).divide(new android.icu.math.BigDecimal("9.99"),4,rhu).toString()).equals("0.9780"));
        TestFmwk.assertTrue("div050", ((new android.icu.math.BigDecimal("9.7")).divide(new android.icu.math.BigDecimal("9.9"),4,rhu).toString()).equals("0.9798"));
        TestFmwk.assertTrue("div051", ((new android.icu.math.BigDecimal("9.")).divide(new android.icu.math.BigDecimal("9."),4,rhu).toString()).equals("1.0000"));

        TestFmwk.assertTrue("div060", ((new android.icu.math.BigDecimal("9.99999999")).divide(new android.icu.math.BigDecimal("9.77777777"),rhu).toString()).equals("1.02272727"));
        TestFmwk.assertTrue("div061", ((new android.icu.math.BigDecimal("9.9999999")).divide(new android.icu.math.BigDecimal("9.7777777"),rhu).toString()).equals("1.0227273"));
        TestFmwk.assertTrue("div062", ((new android.icu.math.BigDecimal("9.999999")).divide(new android.icu.math.BigDecimal("9.777777"),rhu).toString()).equals("1.022727"));
        TestFmwk.assertTrue("div063", ((new android.icu.math.BigDecimal("9.77777777")).divide(new android.icu.math.BigDecimal("9.99999999"),rhu).toString()).equals("0.97777778"));
        TestFmwk.assertTrue("div064", ((new android.icu.math.BigDecimal("9.7777777")).divide(new android.icu.math.BigDecimal("9.9999999"),rhu).toString()).equals("0.9777778"));
        TestFmwk.assertTrue("div065", ((new android.icu.math.BigDecimal("9.777777")).divide(new android.icu.math.BigDecimal("9.999999"),rhu).toString()).equals("0.977778"));
        TestFmwk.assertTrue("div066", ((new android.icu.math.BigDecimal("9.77777")).divide(new android.icu.math.BigDecimal("9.99999"),rhu).toString()).equals("0.97778"));
        TestFmwk.assertTrue("div067", ((new android.icu.math.BigDecimal("9.7777")).divide(new android.icu.math.BigDecimal("9.9999"),rhu).toString()).equals("0.9778"));
        TestFmwk.assertTrue("div068", ((new android.icu.math.BigDecimal("9.777")).divide(new android.icu.math.BigDecimal("9.999"),rhu).toString()).equals("0.978"));
        TestFmwk.assertTrue("div069", ((new android.icu.math.BigDecimal("9.77")).divide(new android.icu.math.BigDecimal("9.99"),rhu).toString()).equals("0.98"));
        TestFmwk.assertTrue("div070", ((new android.icu.math.BigDecimal("9.7")).divide(new android.icu.math.BigDecimal("9.9"),rhu).toString()).equals("1.0"));
        TestFmwk.assertTrue("div071", ((new android.icu.math.BigDecimal("9.")).divide(new android.icu.math.BigDecimal("9."),rhu).toString()).equals("1"));

        rd=android.icu.math.MathContext.ROUND_DOWN; // test this is actually being used
        TestFmwk.assertTrue("div080", ((new android.icu.math.BigDecimal("2")).divide(new android.icu.math.BigDecimal("3"),0,rd).toString()).equals("0"));
        TestFmwk.assertTrue("div081", ((new android.icu.math.BigDecimal("2")).divide(new android.icu.math.BigDecimal("3"),1,rd).toString()).equals("0.6"));
        TestFmwk.assertTrue("div082", ((new android.icu.math.BigDecimal("2")).divide(new android.icu.math.BigDecimal("3"),2,rd).toString()).equals("0.66"));
        TestFmwk.assertTrue("div083", ((new android.icu.math.BigDecimal("2")).divide(new android.icu.math.BigDecimal("3"),3,rd).toString()).equals("0.666"));
        TestFmwk.assertTrue("div084", ((new android.icu.math.BigDecimal("2")).divide(new android.icu.math.BigDecimal("3"),4,rd).toString()).equals("0.6666"));

        ru=android.icu.math.MathContext.ROUND_UNNECESSARY; // check for some 0 residues
        TestFmwk.assertTrue("div090", ((new android.icu.math.BigDecimal("1000")).divide(new android.icu.math.BigDecimal("5"),4,ru).toString()).equals("200.0000"));
        TestFmwk.assertTrue("div091", ((new android.icu.math.BigDecimal("1000")).divide(new android.icu.math.BigDecimal("50"),4,ru).toString()).equals("20.0000"));
        TestFmwk.assertTrue("div092", ((new android.icu.math.BigDecimal("1000")).divide(new android.icu.math.BigDecimal("500"),4,ru).toString()).equals("2.0000"));
        TestFmwk.assertTrue("div093", ((new android.icu.math.BigDecimal("1000")).divide(new android.icu.math.BigDecimal("5000"),4,ru).toString()).equals("0.2000"));
        TestFmwk.assertTrue("div094", ((new android.icu.math.BigDecimal("1000")).divide(new android.icu.math.BigDecimal("5000"),3,ru).toString()).equals("0.200"));
        TestFmwk.assertTrue("div095", ((new android.icu.math.BigDecimal("1000")).divide(new android.icu.math.BigDecimal("5000"),2,ru).toString()).equals("0.20"));
        TestFmwk.assertTrue("div096", ((new android.icu.math.BigDecimal("1000")).divide(new android.icu.math.BigDecimal("5000"),1,ru).toString()).equals("0.2"));

        // check rounding explicitly
        TestFmwk.assertTrue("div101", ((new android.icu.math.BigDecimal("0.055")).divide(one,2,android.icu.math.MathContext.ROUND_CEILING).toString()).equals("0.06"));
        TestFmwk.assertTrue("div102", ((new android.icu.math.BigDecimal("0.055")).divide(one,1,android.icu.math.MathContext.ROUND_CEILING).toString()).equals("0.1"));
        TestFmwk.assertTrue("div103", ((new android.icu.math.BigDecimal("0.055")).divide(one,0,android.icu.math.MathContext.ROUND_CEILING).toString()).equals("1"));
        TestFmwk.assertTrue("div104", ((new android.icu.math.BigDecimal("0.055")).divide(one,2,android.icu.math.MathContext.ROUND_DOWN).toString()).equals("0.05"));
        TestFmwk.assertTrue("div105", ((new android.icu.math.BigDecimal("0.055")).divide(one,1,android.icu.math.MathContext.ROUND_DOWN).toString()).equals("0.0"));
        TestFmwk.assertTrue("div106", ((new android.icu.math.BigDecimal("0.055")).divide(one,0,android.icu.math.MathContext.ROUND_DOWN).toString()).equals("0"));
        TestFmwk.assertTrue("div107", ((new android.icu.math.BigDecimal("0.055")).divide(one,2,android.icu.math.MathContext.ROUND_FLOOR).toString()).equals("0.05"));
        TestFmwk.assertTrue("div108", ((new android.icu.math.BigDecimal("0.055")).divide(one,1,android.icu.math.MathContext.ROUND_FLOOR).toString()).equals("0.0"));
        TestFmwk.assertTrue("div109", ((new android.icu.math.BigDecimal("0.055")).divide(one,0,android.icu.math.MathContext.ROUND_FLOOR).toString()).equals("0"));

        TestFmwk.assertTrue("div110", ((new android.icu.math.BigDecimal("0.045")).divide(one,2,android.icu.math.MathContext.ROUND_HALF_DOWN).toString()).equals("0.04"));
        TestFmwk.assertTrue("div111", ((new android.icu.math.BigDecimal("0.045")).divide(one,1,android.icu.math.MathContext.ROUND_HALF_DOWN).toString()).equals("0.0"));
        TestFmwk.assertTrue("div112", ((new android.icu.math.BigDecimal("0.045")).divide(one,0,android.icu.math.MathContext.ROUND_HALF_DOWN).toString()).equals("0"));
        TestFmwk.assertTrue("div113", ((new android.icu.math.BigDecimal("0.050")).divide(one,2,android.icu.math.MathContext.ROUND_HALF_DOWN).toString()).equals("0.05"));
        TestFmwk.assertTrue("div114", ((new android.icu.math.BigDecimal("0.050")).divide(one,1,android.icu.math.MathContext.ROUND_HALF_DOWN).toString()).equals("0.0"));
        TestFmwk.assertTrue("div115", ((new android.icu.math.BigDecimal("0.050")).divide(one,0,android.icu.math.MathContext.ROUND_HALF_DOWN).toString()).equals("0"));
        TestFmwk.assertTrue("div116", ((new android.icu.math.BigDecimal("0.055")).divide(one,2,android.icu.math.MathContext.ROUND_HALF_DOWN).toString()).equals("0.05"));
        TestFmwk.assertTrue("div117", ((new android.icu.math.BigDecimal("0.055")).divide(one,1,android.icu.math.MathContext.ROUND_HALF_DOWN).toString()).equals("0.1"));
        TestFmwk.assertTrue("div118", ((new android.icu.math.BigDecimal("0.055")).divide(one,0,android.icu.math.MathContext.ROUND_HALF_DOWN).toString()).equals("0"));

        TestFmwk.assertTrue("div120", ((new android.icu.math.BigDecimal("0.045")).divide(one,2,android.icu.math.MathContext.ROUND_HALF_EVEN).toString()).equals("0.04"));
        TestFmwk.assertTrue("div121", ((new android.icu.math.BigDecimal("0.045")).divide(one,1,android.icu.math.MathContext.ROUND_HALF_EVEN).toString()).equals("0.0"));
        TestFmwk.assertTrue("div122", ((new android.icu.math.BigDecimal("0.045")).divide(one,0,android.icu.math.MathContext.ROUND_HALF_EVEN).toString()).equals("0"));
        TestFmwk.assertTrue("div123", ((new android.icu.math.BigDecimal("0.050")).divide(one,2,android.icu.math.MathContext.ROUND_HALF_EVEN).toString()).equals("0.05"));
        TestFmwk.assertTrue("div124", ((new android.icu.math.BigDecimal("0.050")).divide(one,1,android.icu.math.MathContext.ROUND_HALF_EVEN).toString()).equals("0.0"));
        TestFmwk.assertTrue("div125", ((new android.icu.math.BigDecimal("0.050")).divide(one,0,android.icu.math.MathContext.ROUND_HALF_EVEN).toString()).equals("0"));
        TestFmwk.assertTrue("div126", ((new android.icu.math.BigDecimal("0.150")).divide(one,2,android.icu.math.MathContext.ROUND_HALF_EVEN).toString()).equals("0.15"));
        TestFmwk.assertTrue("div127", ((new android.icu.math.BigDecimal("0.150")).divide(one,1,android.icu.math.MathContext.ROUND_HALF_EVEN).toString()).equals("0.2"));
        TestFmwk.assertTrue("div128", ((new android.icu.math.BigDecimal("0.150")).divide(one,0,android.icu.math.MathContext.ROUND_HALF_EVEN).toString()).equals("0"));
        TestFmwk.assertTrue("div129", ((new android.icu.math.BigDecimal("0.055")).divide(one,2,android.icu.math.MathContext.ROUND_HALF_EVEN).toString()).equals("0.06"));
        TestFmwk.assertTrue("div130", ((new android.icu.math.BigDecimal("0.055")).divide(one,1,android.icu.math.MathContext.ROUND_HALF_EVEN).toString()).equals("0.1"));
        TestFmwk.assertTrue("div131", ((new android.icu.math.BigDecimal("0.055")).divide(one,0,android.icu.math.MathContext.ROUND_HALF_EVEN).toString()).equals("0"));

        TestFmwk.assertTrue("div140", ((new android.icu.math.BigDecimal("0.045")).divide(one,2,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("0.05"));
        TestFmwk.assertTrue("div141", ((new android.icu.math.BigDecimal("0.045")).divide(one,1,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("0.0"));
        TestFmwk.assertTrue("div142", ((new android.icu.math.BigDecimal("0.045")).divide(one,0,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("0"));
        TestFmwk.assertTrue("div143", ((new android.icu.math.BigDecimal("0.050")).divide(one,2,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("0.05"));
        TestFmwk.assertTrue("div144", ((new android.icu.math.BigDecimal("0.050")).divide(one,1,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("0.1"));
        TestFmwk.assertTrue("div145", ((new android.icu.math.BigDecimal("0.050")).divide(one,0,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("0"));
        TestFmwk.assertTrue("div146", ((new android.icu.math.BigDecimal("0.055")).divide(one,2,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("0.06"));
        TestFmwk.assertTrue("div147", ((new android.icu.math.BigDecimal("0.055")).divide(one,1,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("0.1"));
        TestFmwk.assertTrue("div148", ((new android.icu.math.BigDecimal("0.055")).divide(one,0,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("0"));

        TestFmwk.assertTrue("div150", ((new android.icu.math.BigDecimal("0.055")).divide(one,2,android.icu.math.MathContext.ROUND_UP).toString()).equals("0.06"));
        TestFmwk.assertTrue("div151", ((new android.icu.math.BigDecimal("0.055")).divide(one,1,android.icu.math.MathContext.ROUND_UP).toString()).equals("0.1"));
        TestFmwk.assertTrue("div52.", ((new android.icu.math.BigDecimal("0.055")).divide(one,0,android.icu.math.MathContext.ROUND_UP).toString()).equals("1"));

        // - error conditions ---
        try {
            ten.divide((android.icu.math.BigDecimal) null);
            flag = false;
        } catch (java.lang.NullPointerException $32) {
            flag = true;
        }/* checknull */
        TestFmwk.assertTrue("div201", flag);
        try {
            ten.divide(ten, (android.icu.math.MathContext) null);
            flag = false;
        } catch (java.lang.NullPointerException $33) {
            flag = true;
        }/* checknull2 */
        TestFmwk.assertTrue("div202", flag);

        try {
            (new android.icu.math.BigDecimal("1")).divide(new android.icu.math.BigDecimal("3"), -8, 0);
            flag = false;
        } catch (java.lang.RuntimeException $34) {
            e = $34;
            flag = flag & (e.getMessage()).equals("Negative scale: -8");
        }/* checkscale */
        TestFmwk.assertTrue("div203", flag);

        try {
            (new android.icu.math.BigDecimal("1000")).divide(new android.icu.math.BigDecimal("5000"), 0, android.icu.math.MathContext.ROUND_UNNECESSARY);
            flag = false;
        } catch (java.lang.ArithmeticException $35) {
            ae = $35;
            flag = (ae.getMessage()).equals("Rounding necessary");
        }/* rounn */
        TestFmwk.assertTrue("div204", flag);
        try {
            (new android.icu.math.BigDecimal("1001")).divide(new android.icu.math.BigDecimal("10"), 0, android.icu.math.MathContext.ROUND_UNNECESSARY);
            flag = false;
        } catch (java.lang.ArithmeticException $36) {
            ae = $36;
            flag = (ae.getMessage()).equals("Rounding necessary");
        }/* rounn */
        TestFmwk.assertTrue("div205", flag);
        try {
            (new android.icu.math.BigDecimal("1001")).divide(new android.icu.math.BigDecimal("100"), 1, android.icu.math.MathContext.ROUND_UNNECESSARY);
            flag = false;
        } catch (java.lang.ArithmeticException $37) {
            ae = $37;
            flag = (ae.getMessage()).equals("Rounding necessary");
        }/* rounn */
        TestFmwk.assertTrue("div206", flag);
        try {
            (new android.icu.math.BigDecimal("10001")).divide(
                    new android.icu.math.BigDecimal("10000"), 1,
                    android.icu.math.MathContext.ROUND_UNNECESSARY);
            flag = false;
        } catch (java.lang.ArithmeticException $38) {
            ae = $38;
            flag = (ae.getMessage()).equals("Rounding necessary");
        }/* rounn */
        TestFmwk.assertTrue("div207", flag);
        try {
            (new android.icu.math.BigDecimal("1.0001")).divide(
                    new android.icu.math.BigDecimal("1"), 1,
                    android.icu.math.MathContext.ROUND_UNNECESSARY);
                flag = false;
        } catch (java.lang.ArithmeticException $39) {
            ae = $39;
            flag = (ae.getMessage()).equals("Rounding necessary");
        }/* rounn */
        TestFmwk.assertTrue("div208", flag);

        try {
            (new android.icu.math.BigDecimal("5"))
                    .divide(new android.icu.math.BigDecimal("0.00"));
            flag = false;
        } catch (java.lang.ArithmeticException $40) {
            ae = $40;
            flag = (ae.getMessage()).equals("Divide by 0");
        }/* div0 */
        TestFmwk.assertTrue("div209", flag);

        try {
            tenlong.divide(android.icu.math.BigDecimal.ONE, mcld);
            flag = false;
        } catch (java.lang.ArithmeticException $41) {
            ae = $41;
            flag = (ae.getMessage()).equals("Too many digits:" + " "
                    + tenlong.toString());
        }/* checkdigits */
        TestFmwk.assertTrue("div210", flag);
        try {
            android.icu.math.BigDecimal.ONE.divide(tenlong, mcld);
            flag = false;
        } catch (java.lang.ArithmeticException $42) {
            ae = $42;
            flag = (ae.getMessage()).equals("Too many digits:" + " "
                    + tenlong.toString());
        }/* checkdigits */
        TestFmwk.assertTrue("div211", flag);

    }

    /* ----------------------------------------------------------------- */

    /** Test the {@link android.icu.math.BigDecimal#divideInteger} method. */

    @Test
    public void diagdivideInteger() {
        boolean flag = false;
        java.lang.ArithmeticException ae = null;

        TestFmwk.assertTrue("dvI001", ((new android.icu.math.BigDecimal("101.3")).divideInteger(new android.icu.math.BigDecimal("1"),mcdef).toString()).equals("101"));
        TestFmwk.assertTrue("dvI002", ((new android.icu.math.BigDecimal("101.0")).divideInteger(new android.icu.math.BigDecimal("1"),mcdef).toString()).equals("101"));
        TestFmwk.assertTrue("dvI003", ((new android.icu.math.BigDecimal("101.3")).divideInteger(new android.icu.math.BigDecimal("3"),mcdef).toString()).equals("33"));
        TestFmwk.assertTrue("dvI004", ((new android.icu.math.BigDecimal("101.0")).divideInteger(new android.icu.math.BigDecimal("3"),mcdef).toString()).equals("33"));
        TestFmwk.assertTrue("dvI005", ((new android.icu.math.BigDecimal("2.4")).divideInteger(new android.icu.math.BigDecimal("1"),mcdef).toString()).equals("2"));
        TestFmwk.assertTrue("dvI006", ((new android.icu.math.BigDecimal("2.400")).divideInteger(new android.icu.math.BigDecimal("1"),mcdef).toString()).equals("2"));
        TestFmwk.assertTrue("dvI007", ((new android.icu.math.BigDecimal("18")).divideInteger(new android.icu.math.BigDecimal("18"),mcdef).toString()).equals("1"));
        TestFmwk.assertTrue("dvI008", ((new android.icu.math.BigDecimal("1120")).divideInteger(new android.icu.math.BigDecimal("1000"),mcdef).toString()).equals("1"));
        TestFmwk.assertTrue("dvI009", ((new android.icu.math.BigDecimal("2.4")).divideInteger(new android.icu.math.BigDecimal("2"),mcdef).toString()).equals("1"));
        TestFmwk.assertTrue("dvI010", ((new android.icu.math.BigDecimal("2.400")).divideInteger(new android.icu.math.BigDecimal("2"),mcdef).toString()).equals("1"));
        TestFmwk.assertTrue("dvI011", ((new android.icu.math.BigDecimal("0.5")).divideInteger(new android.icu.math.BigDecimal("2.000"),mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("dvI012", ((new android.icu.math.BigDecimal("8.005")).divideInteger(new android.icu.math.BigDecimal("7"),mcdef).toString()).equals("1"));
        TestFmwk.assertTrue("dvI013", ((new android.icu.math.BigDecimal("5")).divideInteger(new android.icu.math.BigDecimal("2"),mcdef).toString()).equals("2"));
        TestFmwk.assertTrue("dvI014", ((new android.icu.math.BigDecimal("0")).divideInteger(new android.icu.math.BigDecimal("2"),mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("dvI015", ((new android.icu.math.BigDecimal("0.00")).divideInteger(new android.icu.math.BigDecimal("2"),mcdef).toString()).equals("0"));
        // MC
        TestFmwk.assertTrue("dvI016", ((new android.icu.math.BigDecimal("5")).divideInteger(new android.icu.math.BigDecimal("2"), mce).toString()).equals("2"));
        TestFmwk.assertTrue("dvI017", ((new android.icu.math.BigDecimal("5")).divideInteger(new android.icu.math.BigDecimal("2"), mc6).toString()).equals("2"));

        // Fixed --
        TestFmwk.assertTrue("dvI021", ((new android.icu.math.BigDecimal("101.3")).divideInteger(new android.icu.math.BigDecimal("1")).toString()).equals("101"));
        TestFmwk.assertTrue("dvI022", ((new android.icu.math.BigDecimal("101.0")).divideInteger(new android.icu.math.BigDecimal("1")).toString()).equals("101"));
        TestFmwk.assertTrue("dvI023", ((new android.icu.math.BigDecimal("101.3")).divideInteger(new android.icu.math.BigDecimal("3")).toString()).equals("33"));
        TestFmwk.assertTrue("dvI024", ((new android.icu.math.BigDecimal("101.0")).divideInteger(new android.icu.math.BigDecimal("3")).toString()).equals("33"));
        TestFmwk.assertTrue("dvI025", ((new android.icu.math.BigDecimal("2.4")).divideInteger(new android.icu.math.BigDecimal("1")).toString()).equals("2"));
        TestFmwk.assertTrue("dvI026", ((new android.icu.math.BigDecimal("2.400")).divideInteger(new android.icu.math.BigDecimal("1")).toString()).equals("2"));
        TestFmwk.assertTrue("dvI027", ((new android.icu.math.BigDecimal("18")).divideInteger(new android.icu.math.BigDecimal("18")).toString()).equals("1"));
        TestFmwk.assertTrue("dvI028", ((new android.icu.math.BigDecimal("1120")).divideInteger(new android.icu.math.BigDecimal("1000")).toString()).equals("1"));
        TestFmwk.assertTrue("dvI029", ((new android.icu.math.BigDecimal("2.4")).divideInteger(new android.icu.math.BigDecimal("2")).toString()).equals("1"));
        TestFmwk.assertTrue("dvI030", ((new android.icu.math.BigDecimal("2.400")).divideInteger(new android.icu.math.BigDecimal("2")).toString()).equals("1"));
        TestFmwk.assertTrue("dvI031", ((new android.icu.math.BigDecimal("0.5")).divideInteger(new android.icu.math.BigDecimal("2.000")).toString()).equals("0"));
        TestFmwk.assertTrue("dvI032", ((new android.icu.math.BigDecimal("8.005")).divideInteger(new android.icu.math.BigDecimal("7")).toString()).equals("1"));
        TestFmwk.assertTrue("dvI033", ((new android.icu.math.BigDecimal("5")).divideInteger(new android.icu.math.BigDecimal("2")).toString()).equals("2"));
        TestFmwk.assertTrue("dvI034", ((new android.icu.math.BigDecimal("0")).divideInteger(new android.icu.math.BigDecimal("2")).toString()).equals("0"));
        TestFmwk.assertTrue("dvI035", ((new android.icu.math.BigDecimal("0.00")).divideInteger(new android.icu.math.BigDecimal("2")).toString()).equals("0"));

        try {
            ten.divideInteger((android.icu.math.BigDecimal) null);
            flag = false;
        } catch (java.lang.NullPointerException $43) {
            flag = true;
        }/* checknull */
        TestFmwk.assertTrue("dvI101", flag);
        try {
            ten.divideInteger(ten, (android.icu.math.MathContext) null);
            flag = false;
        } catch (java.lang.NullPointerException $44) {
            flag = true;
        }/* checknull2 */
        TestFmwk.assertTrue("dvI102", flag);

        try {
            android.icu.math.BigDecimal.ONE.divideInteger(tenlong, mcld);
            flag = false;
        } catch (java.lang.ArithmeticException $45) {
            ae = $45;
            flag = (ae.getMessage()).equals("Too many digits:" + " "
                    + tenlong.toString());
        }/* checkdigits */
        TestFmwk.assertTrue("dvI103", flag);

        try {
            tenlong.divideInteger(android.icu.math.BigDecimal.ONE, mcld);
            flag = false;
        } catch (java.lang.ArithmeticException $46) {
            ae = $46;
            flag = (ae.getMessage()).equals("Too many digits:" + " "
                    + tenlong.toString());
        }/* checkdigits */
        TestFmwk.assertTrue("dvI104", flag);

    }

    /* ----------------------------------------------------------------- */

    /** Test the {@link android.icu.math.BigDecimal#max} method. */

    @Test
    public void diagmax() {
        boolean flag = false;
        java.lang.ArithmeticException ae = null;

        // we assume add/subtract test function; this and min just
        // test existence and test the truth table
        TestFmwk.assertTrue("max001", ((new android.icu.math.BigDecimal("5")).max(new android.icu.math.BigDecimal("2")).toString()).equals("5"));
        TestFmwk.assertTrue("max002", ((new android.icu.math.BigDecimal("5")).max(new android.icu.math.BigDecimal("5")).toString()).equals("5"));
        TestFmwk.assertTrue("max003", ((new android.icu.math.BigDecimal("2")).max(new android.icu.math.BigDecimal("7")).toString()).equals("7"));
        TestFmwk.assertTrue("max004", ((new android.icu.math.BigDecimal("2")).max(new android.icu.math.BigDecimal("7"),mcdef).toString()).equals("7"));
        TestFmwk.assertTrue("max005", ((new android.icu.math.BigDecimal("2")).max(new android.icu.math.BigDecimal("7"),mc6).toString()).equals("7"));
        TestFmwk.assertTrue("max006", ((new android.icu.math.BigDecimal("2E+3")).max(new android.icu.math.BigDecimal("7")).toString()).equals("2000"));
        TestFmwk.assertTrue("max007", ((new android.icu.math.BigDecimal("2E+3")).max(new android.icu.math.BigDecimal("7"),mc3).toString()).equals("2E+3"));
        TestFmwk.assertTrue("max008", ((new android.icu.math.BigDecimal("7")).max(new android.icu.math.BigDecimal("2E+3")).toString()).equals("2000"));
        TestFmwk.assertTrue("max009", ((new android.icu.math.BigDecimal("7")).max(new android.icu.math.BigDecimal("2E+3"),mc3).toString()).equals("2E+3"));
        try {
            ten.max((android.icu.math.BigDecimal) null);
            flag = false;
        } catch (java.lang.NullPointerException $47) {
            flag = true;
        }/* checknull */
        TestFmwk.assertTrue("max010", flag);
        try {
            ten.max(ten, (android.icu.math.MathContext) null);
            flag = false;
        } catch (java.lang.NullPointerException $48) {
            flag = true;
        }/* checknull2 */
        TestFmwk.assertTrue("max011", flag);
        try {
            tenlong.max(android.icu.math.BigDecimal.ONE, mcld);
            flag = false;
        } catch (java.lang.ArithmeticException $49) {
            ae = $49;
            flag = (ae.getMessage()).equals("Too many digits:" + " "
                    + tenlong.toString());
        }/* checkdigits */
        TestFmwk.assertTrue("max012", flag);
        try {
            android.icu.math.BigDecimal.ONE.max(tenlong, mcld);
            flag = false;
        } catch (java.lang.ArithmeticException $50) {
            ae = $50;
            flag = (ae.getMessage()).equals("Too many digits:" + " "
                    + tenlong.toString());
        }/* checkdigits */
        TestFmwk.assertTrue("max013", flag);
    }

    /** Test the {@link android.icu.math.BigDecimal#min} method. */

    @Test
    public void diagmin() {
        boolean flag = false;
        android.icu.math.BigDecimal minx = null;
        java.lang.ArithmeticException ae = null;
        // we assume add/subtract test function; this and max just
        // test existence and test the truth table

        TestFmwk.assertTrue("min001", ((new android.icu.math.BigDecimal("5")).min(new android.icu.math.BigDecimal("2")).toString()).equals("2"));
        TestFmwk.assertTrue("min002", ((new android.icu.math.BigDecimal("5")).min(new android.icu.math.BigDecimal("5")).toString()).equals("5"));
        TestFmwk.assertTrue("min003", ((new android.icu.math.BigDecimal("2")).min(new android.icu.math.BigDecimal("7")).toString()).equals("2"));
        TestFmwk.assertTrue("min004", ((new android.icu.math.BigDecimal("2")).min(new android.icu.math.BigDecimal("7"),mcdef).toString()).equals("2"));
        TestFmwk.assertTrue("min005", ((new android.icu.math.BigDecimal("1")).min(new android.icu.math.BigDecimal("7"),mc6).toString()).equals("1"));
        TestFmwk.assertTrue("min006", ((new android.icu.math.BigDecimal("-2E+3")).min(new android.icu.math.BigDecimal("7")).toString()).equals("-2000"));
        TestFmwk.assertTrue("min007", ((new android.icu.math.BigDecimal("-2E+3")).min(new android.icu.math.BigDecimal("7"),mc3).toString()).equals("-2E+3"));
        TestFmwk.assertTrue("min008", ((new android.icu.math.BigDecimal("7")).min(new android.icu.math.BigDecimal("-2E+3")).toString()).equals("-2000"));
        TestFmwk.assertTrue("min009", ((new android.icu.math.BigDecimal("7")).min(new android.icu.math.BigDecimal("-2E+3"),mc3).toString()).equals("-2E+3"));
        try {
            minx = ten;
            minx.min((android.icu.math.BigDecimal) null);
            flag = false;
        } catch (java.lang.NullPointerException $51) {
            flag = true;
        }/* checknull */
        TestFmwk.assertTrue("min010", flag);
        try {
            minx = ten;
            minx.min(ten, (android.icu.math.MathContext) null);
            flag = false;
        } catch (java.lang.NullPointerException $52) {
            flag = true;
        }/* checknull2 */
        TestFmwk.assertTrue("min011", flag);

        try {
            tenlong.min(android.icu.math.BigDecimal.ONE, mcld);
            flag = false;
        } catch (java.lang.ArithmeticException $53) {
            ae = $53;
            flag = (ae.getMessage()).equals("Too many digits:" + " "
                    + tenlong.toString());
        }/* checkdigits */
        TestFmwk.assertTrue("min012", flag);
        try {
            (new android.icu.math.BigDecimal(9)).min(tenlong, mcld);
            flag = false;
        } catch (java.lang.ArithmeticException $54) {
            ae = $54;
            flag = (ae.getMessage()).equals("Too many digits:" + " "
                    + tenlong.toString());
        }/* checkdigits */
        TestFmwk.assertTrue("min013", flag);
    }

    /* ----------------------------------------------------------------- */

    /** Test the {@link android.icu.math.BigDecimal#multiply} method. */

    @Test
    public void diagmultiply() {
        boolean flag = false;
        android.icu.math.BigDecimal l9;
        android.icu.math.BigDecimal l77e;
        android.icu.math.BigDecimal l12345;
        android.icu.math.BigDecimal edge;
        android.icu.math.BigDecimal tenedge;
        android.icu.math.BigDecimal hunedge;
        android.icu.math.BigDecimal opo;
        android.icu.math.BigDecimal d1 = null;
        android.icu.math.BigDecimal d2 = null;
        java.lang.ArithmeticException oe = null;
        java.lang.ArithmeticException ae = null;

        TestFmwk.assertTrue("mul001", ((new android.icu.math.BigDecimal("2")).multiply(new android.icu.math.BigDecimal("3"),mcdef).toString()).equals("6"));
        TestFmwk.assertTrue("mul002", ((new android.icu.math.BigDecimal("5")).multiply(new android.icu.math.BigDecimal("1"),mcdef).toString()).equals("5"));
        TestFmwk.assertTrue("mul003", ((new android.icu.math.BigDecimal("5")).multiply(new android.icu.math.BigDecimal("2"),mcdef).toString()).equals("10"));
        TestFmwk.assertTrue("mul004", ((new android.icu.math.BigDecimal("1.20")).multiply(new android.icu.math.BigDecimal("2"),mcdef).toString()).equals("2.40"));
        TestFmwk.assertTrue("mul005", ((new android.icu.math.BigDecimal("1.20")).multiply(new android.icu.math.BigDecimal("0"),mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("mul006", ((new android.icu.math.BigDecimal("1.20")).multiply(new android.icu.math.BigDecimal("-2"),mcdef).toString()).equals("-2.40"));
        TestFmwk.assertTrue("mul007", ((new android.icu.math.BigDecimal("-1.20")).multiply(new android.icu.math.BigDecimal("2"),mcdef).toString()).equals("-2.40"));
        TestFmwk.assertTrue("mul008", ((new android.icu.math.BigDecimal("-1.20")).multiply(new android.icu.math.BigDecimal("0"),mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("mul009", ((new android.icu.math.BigDecimal("-1.20")).multiply(new android.icu.math.BigDecimal("-2"),mcdef).toString()).equals("2.40"));
        TestFmwk.assertTrue("mul010", ((new android.icu.math.BigDecimal("5.09")).multiply(new android.icu.math.BigDecimal("7.1"),mcdef).toString()).equals("36.139"));
        TestFmwk.assertTrue("mul011", ((new android.icu.math.BigDecimal("2.5")).multiply(new android.icu.math.BigDecimal("4"),mcdef).toString()).equals("10.0"));
        TestFmwk.assertTrue("mul012", ((new android.icu.math.BigDecimal("2.50")).multiply(new android.icu.math.BigDecimal("4"),mcdef).toString()).equals("10.00"));
        TestFmwk.assertTrue("mul013", ((new android.icu.math.BigDecimal("1.23456789")).multiply(new android.icu.math.BigDecimal("1.00000000"),mcdef).toString()).equals("1.23456789"));

        TestFmwk.assertTrue("mul014", ((new android.icu.math.BigDecimal("9.999999999")).multiply(new android.icu.math.BigDecimal("9.999999999"),mcdef).toString()).equals("100.000000"));

        TestFmwk.assertTrue("mul015", ((new android.icu.math.BigDecimal("2.50")).multiply(new android.icu.math.BigDecimal("4"),mcdef).toString()).equals("10.00"));
        TestFmwk.assertTrue("mul016", ((new android.icu.math.BigDecimal("2.50")).multiply(new android.icu.math.BigDecimal("4"),mc6).toString()).equals("10.00"));
        TestFmwk.assertTrue("mul017", ((new android.icu.math.BigDecimal("9.999999999")).multiply(new android.icu.math.BigDecimal("9.999999999"),mc6).toString()).equals("100.000"));


        TestFmwk.assertTrue("mul020", ((new android.icu.math.BigDecimal("2")).multiply(new android.icu.math.BigDecimal("3")).toString()).equals("6"));
        TestFmwk.assertTrue("mul021", ((new android.icu.math.BigDecimal("5")).multiply(new android.icu.math.BigDecimal("1")).toString()).equals("5"));
        TestFmwk.assertTrue("mul022", ((new android.icu.math.BigDecimal("5")).multiply(new android.icu.math.BigDecimal("2")).toString()).equals("10"));
        TestFmwk.assertTrue("mul023", ((new android.icu.math.BigDecimal("1.20")).multiply(new android.icu.math.BigDecimal("2")).toString()).equals("2.40"));
        TestFmwk.assertTrue("mul024", ((new android.icu.math.BigDecimal("1.20")).multiply(new android.icu.math.BigDecimal("0")).toString()).equals("0.00"));
        TestFmwk.assertTrue("mul025", ((new android.icu.math.BigDecimal("1.20")).multiply(new android.icu.math.BigDecimal("-2")).toString()).equals("-2.40"));
        TestFmwk.assertTrue("mul026", ((new android.icu.math.BigDecimal("-1.20")).multiply(new android.icu.math.BigDecimal("2")).toString()).equals("-2.40"));
        TestFmwk.assertTrue("mul027", ((new android.icu.math.BigDecimal("-1.20")).multiply(new android.icu.math.BigDecimal("0")).toString()).equals("0.00"));
        TestFmwk.assertTrue("mul028", ((new android.icu.math.BigDecimal("-1.20")).multiply(new android.icu.math.BigDecimal("-2")).toString()).equals("2.40"));
        TestFmwk.assertTrue("mul029", ((new android.icu.math.BigDecimal("5.09")).multiply(new android.icu.math.BigDecimal("7.1")).toString()).equals("36.139"));
        TestFmwk.assertTrue("mul030", ((new android.icu.math.BigDecimal("2.5")).multiply(new android.icu.math.BigDecimal("4")).toString()).equals("10.0"));
        TestFmwk.assertTrue("mul031", ((new android.icu.math.BigDecimal("2.50")).multiply(new android.icu.math.BigDecimal("4")).toString()).equals("10.00"));
        TestFmwk.assertTrue("mul032", ((new android.icu.math.BigDecimal("1.23456789")).multiply(new android.icu.math.BigDecimal("1.00000000")).toString()).equals("1.2345678900000000"));

        TestFmwk.assertTrue("mul033", ((new android.icu.math.BigDecimal("1234.56789")).multiply(new android.icu.math.BigDecimal("-1000.00000")).toString()).equals("-1234567.8900000000"));

        TestFmwk.assertTrue("mul034", ((new android.icu.math.BigDecimal("-1234.56789")).multiply(new android.icu.math.BigDecimal("1000.00000")).toString()).equals("-1234567.8900000000"));

        TestFmwk.assertTrue("mul035", ((new android.icu.math.BigDecimal("9.999999999")).multiply(new android.icu.math.BigDecimal("9.999999999")).toString()).equals("99.999999980000000001"));

        TestFmwk.assertTrue("mul036", ((new android.icu.math.BigDecimal("5.00")).multiply(new android.icu.math.BigDecimal("1E-3")).toString()).equals("0.00500"));
        TestFmwk.assertTrue("mul037", ((new android.icu.math.BigDecimal("00.00")).multiply(new android.icu.math.BigDecimal("0.000")).toString()).equals("0.00000"));
        TestFmwk.assertTrue("mul038", ((new android.icu.math.BigDecimal("00.00")).multiply(new android.icu.math.BigDecimal("0E-3")).toString()).equals("0.00")); // rhs is '0'
        // 1999.12.21: next one is a edge case if intermediate longs are used
        TestFmwk.assertTrue("mul039", ((new android.icu.math.BigDecimal("999999999999")).multiply(new android.icu.math.BigDecimal("9765625")).toString()).equals("9765624999990234375"));

        l9 = new android.icu.math.BigDecimal("123456789E+10");
        l77e = new android.icu.math.BigDecimal("77E-20");
        TestFmwk.assertTrue("mul040", (l9.multiply(new android.icu.math.BigDecimal("3456757")).toString()).equals("4267601195732730000000000"));
        TestFmwk.assertTrue("mul041", (l9.multiply(new android.icu.math.BigDecimal("3456757"), mc3).toString()).equals("4.26E+24"));
        TestFmwk.assertTrue("mul042", (l9.multiply(l77e).toString()).equals("0.95061727530000000000"));
        TestFmwk.assertTrue("mul043", (l9.multiply(l77e, mc3).toString()).equals("0.947"));
        TestFmwk.assertTrue("mul044", (l77e.multiply(l9, mc3).toString()).equals("0.947"));

        l12345 = new android.icu.math.BigDecimal("123.45");
        TestFmwk.assertTrue("mul050", (l12345.multiply(new android.icu.math.BigDecimal("1e11"),mcdef).toString()).equals("1.2345E+13"));
        TestFmwk.assertTrue("mul051", (l12345.multiply(new android.icu.math.BigDecimal("1e11"),mcs).toString()).equals("1.2345E+13"));
        TestFmwk.assertTrue("mul052", (l12345.multiply(new android.icu.math.BigDecimal("1e+9"),mce).toString()).equals("123.45E+9"));
        TestFmwk.assertTrue("mul053", (l12345.multiply(new android.icu.math.BigDecimal("1e10"),mce).toString()).equals("1.2345E+12"));
        TestFmwk.assertTrue("mul054", (l12345.multiply(new android.icu.math.BigDecimal("1e11"),mce).toString()).equals("12.345E+12"));
        TestFmwk.assertTrue("mul055", (l12345.multiply(new android.icu.math.BigDecimal("1e12"),mce).toString()).equals("123.45E+12"));
        TestFmwk.assertTrue("mul056", (l12345.multiply(new android.icu.math.BigDecimal("1e13"),mce).toString()).equals("1.2345E+15"));

        // test some cases that are close to exponent overflow
        TestFmwk.assertTrue("mul060", (one.multiply(new android.icu.math.BigDecimal("9e999999999"),mcs).toString()).equals("9E+999999999"));
        TestFmwk.assertTrue("mul061", (one.multiply(new android.icu.math.BigDecimal("9.9e999999999"),mcs).toString()).equals("9.9E+999999999"));
        TestFmwk.assertTrue("mul062", (one.multiply(new android.icu.math.BigDecimal("9.99e999999999"),mcs).toString()).equals("9.99E+999999999"));
        TestFmwk.assertTrue("mul063", (ten.multiply(new android.icu.math.BigDecimal("9e999999999"),mce).toString()).equals("90E+999999999"));
        TestFmwk.assertTrue("mul064", (ten.multiply(new android.icu.math.BigDecimal("9.9e999999999"),mce).toString()).equals("99.0E+999999999"));
        edge = new android.icu.math.BigDecimal("9.999e999999999");
        tenedge = ten.multiply(edge, mce);
        TestFmwk.assertTrue("mul065", (tenedge.toString()).equals("99.990E+999999999"));
        hunedge = ten.multiply(tenedge, mce);
        TestFmwk.assertTrue("mul066", (hunedge.toString()).equals("999.900E+999999999"));
        opo = new android.icu.math.BigDecimal("0.1"); // one tenth
        TestFmwk.assertTrue("mul067", (opo.multiply(new android.icu.math.BigDecimal("9e-999999998"),mcs).toString()).equals("9E-999999999"));
        TestFmwk.assertTrue("mul068", (opo.multiply(new android.icu.math.BigDecimal("99e-999999998"),mcs).toString()).equals("9.9E-999999998"));
        TestFmwk.assertTrue("mul069", (opo.multiply(new android.icu.math.BigDecimal("999e-999999998"),mcs).toString()).equals("9.99E-999999997"));

        TestFmwk.assertTrue("mul070", (opo.multiply(new android.icu.math.BigDecimal("9e-999999998"),mce).toString()).equals("9E-999999999"));
        TestFmwk.assertTrue("mul071", (opo.multiply(new android.icu.math.BigDecimal("99e-999999998"),mce).toString()).equals("99E-999999999"));
        TestFmwk.assertTrue("mul072", (opo.multiply(new android.icu.math.BigDecimal("999e-999999998"),mce).toString()).equals("999E-999999999"));
        TestFmwk.assertTrue("mul073", (opo.multiply(new android.icu.math.BigDecimal("999e-999999997"),mce).toString()).equals("9.99E-999999996"));
        TestFmwk.assertTrue("mul074", (opo.multiply(new android.icu.math.BigDecimal("9999e-999999997"),mce).toString()).equals("99.99E-999999996"));
        TestFmwk.assertTrue("mul074", (opo.multiply(new android.icu.math.BigDecimal("99999e-999999997"),mce).toString()).equals("999.99E-999999996"));

        // test some intermediate lengths
        TestFmwk.assertTrue("mul080", (opo.multiply(new android.icu.math.BigDecimal("123456789"),mcs).toString()).equals("12345678.9"));
        TestFmwk.assertTrue("mul081", (opo.multiply(new android.icu.math.BigDecimal("12345678901234"),mcs).toString()).equals("1.23456789E+12"));
        TestFmwk.assertTrue("mul082", (opo.multiply(new android.icu.math.BigDecimal("123456789123456789"),mcs).toString()).equals("1.23456789E+16"));
        TestFmwk.assertTrue("mul083", (opo.multiply(new android.icu.math.BigDecimal("123456789"),mcfd).toString()).equals("12345678.9"));
        TestFmwk.assertTrue("mul084", (opo.multiply(new android.icu.math.BigDecimal("12345678901234"),mcfd).toString()).equals("1234567890123.4"));
        TestFmwk.assertTrue("mul085", (opo.multiply(new android.icu.math.BigDecimal("123456789123456789"),mcfd).toString()).equals("12345678912345678.9"));

        TestFmwk.assertTrue("mul090", ((new android.icu.math.BigDecimal("123456789")).multiply(opo,mcs).toString()).equals("12345678.9"));
        TestFmwk.assertTrue("mul091", ((new android.icu.math.BigDecimal("12345678901234")).multiply(opo,mcs).toString()).equals("1.23456789E+12"));
        TestFmwk.assertTrue("mul092", ((new android.icu.math.BigDecimal("123456789123456789")).multiply(opo,mcs).toString()).equals("1.23456789E+16"));
        TestFmwk.assertTrue("mul093", ((new android.icu.math.BigDecimal("123456789")).multiply(opo,mcfd).toString()).equals("12345678.9"));
        TestFmwk.assertTrue("mul094", ((new android.icu.math.BigDecimal("12345678901234")).multiply(opo,mcfd).toString()).equals("1234567890123.4"));
        TestFmwk.assertTrue("mul095", ((new android.icu.math.BigDecimal("123456789123456789")).multiply(opo,mcfd).toString()).equals("12345678912345678.9"));

        // test some more edge cases and carries
        TestFmwk.assertTrue("mul101", ((new android.icu.math.BigDecimal("9")).multiply(new android.icu.math.BigDecimal("9")).toString()).equals("81"));
        TestFmwk.assertTrue("mul102", ((new android.icu.math.BigDecimal("9")).multiply(new android.icu.math.BigDecimal("90")).toString()).equals("810"));
        TestFmwk.assertTrue("mul103", ((new android.icu.math.BigDecimal("9")).multiply(new android.icu.math.BigDecimal("900")).toString()).equals("8100"));
        TestFmwk.assertTrue("mul104", ((new android.icu.math.BigDecimal("9")).multiply(new android.icu.math.BigDecimal("9000")).toString()).equals("81000"));
        TestFmwk.assertTrue("mul105", ((new android.icu.math.BigDecimal("9")).multiply(new android.icu.math.BigDecimal("90000")).toString()).equals("810000"));
        TestFmwk.assertTrue("mul106", ((new android.icu.math.BigDecimal("9")).multiply(new android.icu.math.BigDecimal("900000")).toString()).equals("8100000"));
        TestFmwk.assertTrue("mul107", ((new android.icu.math.BigDecimal("9")).multiply(new android.icu.math.BigDecimal("9000000")).toString()).equals("81000000"));
        TestFmwk.assertTrue("mul108", ((new android.icu.math.BigDecimal("9")).multiply(new android.icu.math.BigDecimal("90000000")).toString()).equals("810000000"));
        TestFmwk.assertTrue("mul109", ((new android.icu.math.BigDecimal("9")).multiply(new android.icu.math.BigDecimal("900000000")).toString()).equals("8100000000"));
        TestFmwk.assertTrue("mul110", ((new android.icu.math.BigDecimal("9")).multiply(new android.icu.math.BigDecimal("9000000000")).toString()).equals("81000000000"));
        TestFmwk.assertTrue("mul111", ((new android.icu.math.BigDecimal("9")).multiply(new android.icu.math.BigDecimal("90000000000")).toString()).equals("810000000000"));
        TestFmwk.assertTrue("mul112", ((new android.icu.math.BigDecimal("9")).multiply(new android.icu.math.BigDecimal("900000000000")).toString()).equals("8100000000000"));
        TestFmwk.assertTrue("mul113", ((new android.icu.math.BigDecimal("9")).multiply(new android.icu.math.BigDecimal("9000000000000")).toString()).equals("81000000000000"));
        TestFmwk.assertTrue("mul114", ((new android.icu.math.BigDecimal("9")).multiply(new android.icu.math.BigDecimal("90000000000000")).toString()).equals("810000000000000"));
        TestFmwk.assertTrue("mul115", ((new android.icu.math.BigDecimal("9")).multiply(new android.icu.math.BigDecimal("900000000000000")).toString()).equals("8100000000000000"));
        TestFmwk.assertTrue("mul116", ((new android.icu.math.BigDecimal("9")).multiply(new android.icu.math.BigDecimal("9000000000000000")).toString()).equals("81000000000000000"));
        TestFmwk.assertTrue("mul117", ((new android.icu.math.BigDecimal("9")).multiply(new android.icu.math.BigDecimal("90000000000000000")).toString()).equals("810000000000000000"));
        TestFmwk.assertTrue("mul118", ((new android.icu.math.BigDecimal("9")).multiply(new android.icu.math.BigDecimal("900000000000000000")).toString()).equals("8100000000000000000"));
        TestFmwk.assertTrue("mul119", ((new android.icu.math.BigDecimal("9")).multiply(new android.icu.math.BigDecimal("9000000000000000000")).toString()).equals("81000000000000000000"));
        TestFmwk.assertTrue("mul120", ((new android.icu.math.BigDecimal("9")).multiply(new android.icu.math.BigDecimal("90000000000000000000")).toString()).equals("810000000000000000000"));
        TestFmwk.assertTrue("mul121", ((new android.icu.math.BigDecimal("9")).multiply(new android.icu.math.BigDecimal("900000000000000000000")).toString()).equals("8100000000000000000000"));
        TestFmwk.assertTrue("mul122", ((new android.icu.math.BigDecimal("9")).multiply(new android.icu.math.BigDecimal("9000000000000000000000")).toString()).equals("81000000000000000000000"));
        TestFmwk.assertTrue("mul123", ((new android.icu.math.BigDecimal("9")).multiply(new android.icu.math.BigDecimal("90000000000000000000000")).toString()).equals("810000000000000000000000"));
        // test some more edge cases without carries
        TestFmwk.assertTrue("mul131", ((new android.icu.math.BigDecimal("3")).multiply(new android.icu.math.BigDecimal("3")).toString()).equals("9"));
        TestFmwk.assertTrue("mul132", ((new android.icu.math.BigDecimal("3")).multiply(new android.icu.math.BigDecimal("30")).toString()).equals("90"));
        TestFmwk.assertTrue("mul133", ((new android.icu.math.BigDecimal("3")).multiply(new android.icu.math.BigDecimal("300")).toString()).equals("900"));
        TestFmwk.assertTrue("mul134", ((new android.icu.math.BigDecimal("3")).multiply(new android.icu.math.BigDecimal("3000")).toString()).equals("9000"));
        TestFmwk.assertTrue("mul135", ((new android.icu.math.BigDecimal("3")).multiply(new android.icu.math.BigDecimal("30000")).toString()).equals("90000"));
        TestFmwk.assertTrue("mul136", ((new android.icu.math.BigDecimal("3")).multiply(new android.icu.math.BigDecimal("300000")).toString()).equals("900000"));
        TestFmwk.assertTrue("mul137", ((new android.icu.math.BigDecimal("3")).multiply(new android.icu.math.BigDecimal("3000000")).toString()).equals("9000000"));
        TestFmwk.assertTrue("mul138", ((new android.icu.math.BigDecimal("3")).multiply(new android.icu.math.BigDecimal("30000000")).toString()).equals("90000000"));
        TestFmwk.assertTrue("mul139", ((new android.icu.math.BigDecimal("3")).multiply(new android.icu.math.BigDecimal("300000000")).toString()).equals("900000000"));
        TestFmwk.assertTrue("mul140", ((new android.icu.math.BigDecimal("3")).multiply(new android.icu.math.BigDecimal("3000000000")).toString()).equals("9000000000"));
        TestFmwk.assertTrue("mul141", ((new android.icu.math.BigDecimal("3")).multiply(new android.icu.math.BigDecimal("30000000000")).toString()).equals("90000000000"));
        TestFmwk.assertTrue("mul142", ((new android.icu.math.BigDecimal("3")).multiply(new android.icu.math.BigDecimal("300000000000")).toString()).equals("900000000000"));
        TestFmwk.assertTrue("mul143", ((new android.icu.math.BigDecimal("3")).multiply(new android.icu.math.BigDecimal("3000000000000")).toString()).equals("9000000000000"));
        TestFmwk.assertTrue("mul144", ((new android.icu.math.BigDecimal("3")).multiply(new android.icu.math.BigDecimal("30000000000000")).toString()).equals("90000000000000"));
        TestFmwk.assertTrue("mul145", ((new android.icu.math.BigDecimal("3")).multiply(new android.icu.math.BigDecimal("300000000000000")).toString()).equals("900000000000000"));
        TestFmwk.assertTrue("mul146", ((new android.icu.math.BigDecimal("3")).multiply(new android.icu.math.BigDecimal("3000000000000000")).toString()).equals("9000000000000000"));
        TestFmwk.assertTrue("mul147", ((new android.icu.math.BigDecimal("3")).multiply(new android.icu.math.BigDecimal("30000000000000000")).toString()).equals("90000000000000000"));
        TestFmwk.assertTrue("mul148", ((new android.icu.math.BigDecimal("3")).multiply(new android.icu.math.BigDecimal("300000000000000000")).toString()).equals("900000000000000000"));
        TestFmwk.assertTrue("mul149", ((new android.icu.math.BigDecimal("3")).multiply(new android.icu.math.BigDecimal("3000000000000000000")).toString()).equals("9000000000000000000"));
        TestFmwk.assertTrue("mul150", ((new android.icu.math.BigDecimal("3")).multiply(new android.icu.math.BigDecimal("30000000000000000000")).toString()).equals("90000000000000000000"));
        TestFmwk.assertTrue("mul151", ((new android.icu.math.BigDecimal("3")).multiply(new android.icu.math.BigDecimal("300000000000000000000")).toString()).equals("900000000000000000000"));
        TestFmwk.assertTrue("mul152", ((new android.icu.math.BigDecimal("3")).multiply(new android.icu.math.BigDecimal("3000000000000000000000")).toString()).equals("9000000000000000000000"));
        TestFmwk.assertTrue("mul153", ((new android.icu.math.BigDecimal("3")).multiply(new android.icu.math.BigDecimal("30000000000000000000000")).toString()).equals("90000000000000000000000"));

        try {
            ten.multiply((android.icu.math.BigDecimal) null);
            flag = false;
        } catch (java.lang.NullPointerException $55) {
            flag = true;
        }/* checknull */
        TestFmwk.assertTrue("mul200", flag);
        try {
            ten.multiply(ten, (android.icu.math.MathContext) null);
            flag = false;
        } catch (java.lang.NullPointerException $56) {
            flag = true;
        }/* checknull2 */
        TestFmwk.assertTrue("mul201", flag);

        try {
            d1 = new android.icu.math.BigDecimal("-1.23456789012345E-0");
            d2 = new android.icu.math.BigDecimal("9E+999999999");
            d1.multiply(d2, mcdef); // marginal overflow
            flag = false;
        } catch (java.lang.ArithmeticException $57) {
            oe = $57;
            flag = (oe.getMessage()).equals("Exponent Overflow: 1000000000");
        }/* checkover */
        TestFmwk.assertTrue("mul202", flag);
        try {
            d1 = new android.icu.math.BigDecimal("112");
            d2 = new android.icu.math.BigDecimal("9E+999999999");
            d1.multiply(d2, mce); // marginal overflow, engineering
            flag = false;
        } catch (java.lang.ArithmeticException $58) {
            oe = $58;
            flag = (oe.getMessage()).equals("Exponent Overflow: 1000000002");
        }/* checkover */
        TestFmwk.assertTrue("mul203", flag);

        try {
            d1 = new android.icu.math.BigDecimal("0.9");
            d2 = new android.icu.math.BigDecimal("1E-999999999");
            d1.multiply(d2, mcdef); // marginal negative overflow
            flag = false;
        } catch (java.lang.ArithmeticException $59) {
            oe = $59;
            flag = (oe.getMessage()).equals("Exponent Overflow: -1000000000");
        }/* checkover */
        TestFmwk.assertTrue("mul204", flag);
        try {
            d1 = new android.icu.math.BigDecimal("0.9");
            d2 = new android.icu.math.BigDecimal("1E-999999999");
            d1.multiply(d2, mce); // marginal negative overflow,
                                    // engineering
            flag = false;
        } catch (java.lang.ArithmeticException $60) {
            oe = $60;
            flag = (oe.getMessage()).equals("Exponent Overflow: -1000000002");
        }/* checkover */
        TestFmwk.assertTrue("mul205", flag);

        try {
            tenlong.multiply(android.icu.math.BigDecimal.ONE, mcld);
            flag = false;
        } catch (java.lang.ArithmeticException $61) {
            ae = $61;
            flag = (ae.getMessage()).equals("Too many digits:" + " "
                    + tenlong.toString());
        }/* checkdigits */
        TestFmwk.assertTrue("mul206", flag);
        try {
            android.icu.math.BigDecimal.TEN.multiply(tenlong, mcld);
            flag = false;
        } catch (java.lang.ArithmeticException $62) {
            ae = $62;
            flag = (ae.getMessage()).equals("Too many digits:" + " "
                    + tenlong.toString());
        }/* checkdigits */
        TestFmwk.assertTrue("mul207", flag);

    }

    /* ----------------------------------------------------------------- */

    /** Test the {@link android.icu.math.BigDecimal#negate} method. */

    @Test
    public void diagnegate() {
        boolean flag = false;
        java.lang.ArithmeticException ae = null;

        TestFmwk.assertTrue("neg001", ((new android.icu.math.BigDecimal("2")).negate().toString()).equals("-2"));
        TestFmwk.assertTrue("neg002", ((new android.icu.math.BigDecimal("-2")).negate().toString()).equals("2"));
        TestFmwk.assertTrue("neg003", ((new android.icu.math.BigDecimal("2.00")).negate(mcdef).toString()).equals("-2.00"));
        TestFmwk.assertTrue("neg004", ((new android.icu.math.BigDecimal("-2.00")).negate(mcdef).toString()).equals("2.00"));
        TestFmwk.assertTrue("neg005", ((new android.icu.math.BigDecimal("0")).negate(mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("neg006", ((new android.icu.math.BigDecimal("0.00")).negate(mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("neg007", ((new android.icu.math.BigDecimal("00.0")).negate(mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("neg008", ((new android.icu.math.BigDecimal("00")).negate(mcdef).toString()).equals("0"));

        TestFmwk.assertTrue("neg010", ((new android.icu.math.BigDecimal("2.00")).negate().toString()).equals("-2.00"));
        TestFmwk.assertTrue("neg011", ((new android.icu.math.BigDecimal("-2.00")).negate().toString()).equals("2.00"));
        TestFmwk.assertTrue("neg012", ((new android.icu.math.BigDecimal("0")).negate().toString()).equals("0"));
        TestFmwk.assertTrue("neg013", ((new android.icu.math.BigDecimal("0.00")).negate().toString()).equals("0.00"));
        TestFmwk.assertTrue("neg014", ((new android.icu.math.BigDecimal("00.0")).negate().toString()).equals("0.0"));
        TestFmwk.assertTrue("neg015", ((new android.icu.math.BigDecimal("00.00")).negate().toString()).equals("0.00"));
        TestFmwk.assertTrue("neg016", ((new android.icu.math.BigDecimal("00")).negate().toString()).equals("0"));

        TestFmwk.assertTrue("neg020", ((new android.icu.math.BigDecimal("-2000000")).negate().toString()).equals("2000000"));
        TestFmwk.assertTrue("neg021", ((new android.icu.math.BigDecimal("-2000000")).negate(mcdef).toString()).equals("2000000"));
        TestFmwk.assertTrue("neg022", ((new android.icu.math.BigDecimal("-2000000")).negate(mc6).toString()).equals("2.00000E+6"));
        TestFmwk.assertTrue("neg023", ((new android.icu.math.BigDecimal("2000000")).negate(mc6).toString()).equals("-2.00000E+6"));

        try {
            ten.negate((android.icu.math.MathContext) null);
            flag = false;
        } catch (java.lang.NullPointerException $63) {
            flag = true;
        }/* checknull */
        TestFmwk.assertTrue("neg100", flag);

        try {
            tenlong.negate(mcld);
            flag = false;
        } catch (java.lang.ArithmeticException $64) {
            ae = $64;
            flag = (ae.getMessage()).equals("Too many digits:" + " "
                    + tenlong.toString());
        }/* checkdigits */
        TestFmwk.assertTrue("neg101", flag);
    }

    /* ----------------------------------------------------------------- */

    /** Test the {@link android.icu.math.BigDecimal#plus} method. */

    @Test
    public void diagplus() {
        boolean flag = false;
        android.icu.math.MathContext mche1;
        java.lang.ArithmeticException ae = null;

        TestFmwk.assertTrue("plu001", ((new android.icu.math.BigDecimal("2")).plus(mcdef).toString()).equals("2"));
        TestFmwk.assertTrue("plu002", ((new android.icu.math.BigDecimal("-2")).plus(mcdef).toString()).equals("-2"));
        TestFmwk.assertTrue("plu003", ((new android.icu.math.BigDecimal("2.00")).plus(mcdef).toString()).equals("2.00"));
        TestFmwk.assertTrue("plu004", ((new android.icu.math.BigDecimal("-2.00")).plus(mcdef).toString()).equals("-2.00"));
        TestFmwk.assertTrue("plu005", ((new android.icu.math.BigDecimal("0")).plus(mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("plu006", ((new android.icu.math.BigDecimal("0.00")).plus(mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("plu007", ((new android.icu.math.BigDecimal("00.0")).plus(mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("plu008", ((new android.icu.math.BigDecimal("00")).plus(mcdef).toString()).equals("0"));

        TestFmwk.assertTrue("plu010", ((new android.icu.math.BigDecimal("2")).plus().toString()).equals("2"));
        TestFmwk.assertTrue("plu011", ((new android.icu.math.BigDecimal("-2")).plus().toString()).equals("-2"));
        TestFmwk.assertTrue("plu012", ((new android.icu.math.BigDecimal("2.00")).plus().toString()).equals("2.00"));
        TestFmwk.assertTrue("plu013", ((new android.icu.math.BigDecimal("-2.00")).plus().toString()).equals("-2.00"));
        TestFmwk.assertTrue("plu014", ((new android.icu.math.BigDecimal("0")).plus().toString()).equals("0"));
        TestFmwk.assertTrue("plu015", ((new android.icu.math.BigDecimal("0.00")).plus().toString()).equals("0.00"));
        TestFmwk.assertTrue("plu016", ((new android.icu.math.BigDecimal("00.0")).plus().toString()).equals("0.0"));
        TestFmwk.assertTrue("plu017", ((new android.icu.math.BigDecimal("00.00")).plus().toString()).equals("0.00"));
        TestFmwk.assertTrue("plu018", ((new android.icu.math.BigDecimal("00")).plus().toString()).equals("0"));

        TestFmwk.assertTrue("plu020", ((new android.icu.math.BigDecimal("-2000000")).plus().toString()).equals("-2000000"));
        TestFmwk.assertTrue("plu021", ((new android.icu.math.BigDecimal("-2000000")).plus(mcdef).toString()).equals("-2000000"));
        TestFmwk.assertTrue("plu022", ((new android.icu.math.BigDecimal("-2000000")).plus(mc6).toString()).equals("-2.00000E+6"));
        TestFmwk.assertTrue("plu023", ((new android.icu.math.BigDecimal("2000000")).plus(mc6).toString()).equals("2.00000E+6"));

        // try some exotic but silly rounding [format checks more varieties]
        // [this mostly ensures we can set up and pass the setting]
        mche1=new android.icu.math.MathContext(1,android.icu.math.MathContext.SCIENTIFIC,false,android.icu.math.MathContext.ROUND_HALF_EVEN);
        TestFmwk.assertTrue("plu030", ((new android.icu.math.BigDecimal("0.24")).plus(mche1).toString()).equals("0.2"));
        TestFmwk.assertTrue("plu031", ((new android.icu.math.BigDecimal("0.25")).plus(mche1).toString()).equals("0.2"));
        TestFmwk.assertTrue("plu032", ((new android.icu.math.BigDecimal("0.26")).plus(mche1).toString()).equals("0.3"));
        TestFmwk.assertTrue("plu033", ((new android.icu.math.BigDecimal("0.14")).plus(mche1).toString()).equals("0.1"));
        TestFmwk.assertTrue("plu034", ((new android.icu.math.BigDecimal("0.15")).plus(mche1).toString()).equals("0.2"));
        TestFmwk.assertTrue("plu035", ((new android.icu.math.BigDecimal("0.16")).plus(mche1).toString()).equals("0.2"));

        TestFmwk.assertTrue("plu040", ((new android.icu.math.BigDecimal("0.251")).plus(mche1).toString()).equals("0.3"));
        TestFmwk.assertTrue("plu041", ((new android.icu.math.BigDecimal("0.151")).plus(mche1).toString()).equals("0.2"));

        TestFmwk.assertTrue("plu050", ((new android.icu.math.BigDecimal("-0.24")).plus(mche1).toString()).equals("-0.2"));
        TestFmwk.assertTrue("plu051", ((new android.icu.math.BigDecimal("-0.25")).plus(mche1).toString()).equals("-0.2"));
        TestFmwk.assertTrue("plu052", ((new android.icu.math.BigDecimal("-0.26")).plus(mche1).toString()).equals("-0.3"));
        TestFmwk.assertTrue("plu053", ((new android.icu.math.BigDecimal("-0.14")).plus(mche1).toString()).equals("-0.1"));
        TestFmwk.assertTrue("plu054", ((new android.icu.math.BigDecimal("-0.15")).plus(mche1).toString()).equals("-0.2"));
        TestFmwk.assertTrue("plu055", ((new android.icu.math.BigDecimal("-0.16")).plus(mche1).toString()).equals("-0.2"));

        // more fixed, potential LHS swaps if done by add 0
        TestFmwk.assertTrue("plu060", ((new android.icu.math.BigDecimal("-56267E-10")).plus().toString()).equals("-0.0000056267"));
        TestFmwk.assertTrue("plu061", ((new android.icu.math.BigDecimal("-56267E-5")).plus().toString()).equals("-0.56267"));
        TestFmwk.assertTrue("plu062", ((new android.icu.math.BigDecimal("-56267E-2")).plus().toString()).equals("-562.67"));
        TestFmwk.assertTrue("plu063", ((new android.icu.math.BigDecimal("-56267E-1")).plus().toString()).equals("-5626.7"));
        TestFmwk.assertTrue("plu065", ((new android.icu.math.BigDecimal("-56267E-0")).plus().toString()).equals("-56267"));

        try {
            ten.plus((android.icu.math.MathContext) null);
            flag = false;
        } catch (java.lang.NullPointerException $65) {
            flag = true;
        }/* checknull */
        TestFmwk.assertTrue("plu100", flag);

        try {
            tenlong.plus(mcld);
            flag = false;
        } catch (java.lang.ArithmeticException $66) {
            ae = $66;
            flag = (ae.getMessage()).equals("Too many digits:" + " "
                    + tenlong.toString());
        }/* checkdigits */
        TestFmwk.assertTrue("plu101", flag);
    }

    /* ----------------------------------------------------------------- */

    /** Test the {@link android.icu.math.BigDecimal#pow} method. */

    @Test
    public void diagpow() {
        boolean flag;
        android.icu.math.BigDecimal x;
        android.icu.math.BigDecimal temp;
        int n = 0;
        android.icu.math.BigDecimal vx;
        android.icu.math.BigDecimal vn;
        java.lang.ArithmeticException ae = null;
        flag = true;
        TestFmwk.assertTrue("pow001", "1".equals((new android.icu.math.BigDecimal("0.3")).pow(new android.icu.math.BigDecimal("0"),mcdef).toString()));
        TestFmwk.assertTrue("pow002", "0.3".equals((new android.icu.math.BigDecimal("0.3")).pow(new android.icu.math.BigDecimal("1"),mcdef).toString()));
        TestFmwk.assertTrue("pow003", "0.3".equals((new android.icu.math.BigDecimal("0.3")).pow(new android.icu.math.BigDecimal("1.00"),mcdef).toString()));
        TestFmwk.assertTrue("pow004", "0.09".equals((new android.icu.math.BigDecimal("0.3")).pow(new android.icu.math.BigDecimal("2.00"),mcdef).toString()));
        TestFmwk.assertTrue("pow005", "0.09".equals((new android.icu.math.BigDecimal("0.3")).pow(new android.icu.math.BigDecimal("2.000000000"),mcdef).toString()));
        TestFmwk.assertTrue("pow006", ("1E-8").equals((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("-8"),mcdef).toString()));
        TestFmwk.assertTrue("pow007", ("1E-7").equals((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("-7"),mcdef).toString()));
        TestFmwk.assertTrue("pow008", "0.000001".equals((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("-6"),mcdef).toString()));
        TestFmwk.assertTrue("pow009", "0.00001".equals((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("-5"),mcdef).toString()));
        TestFmwk.assertTrue("pow010", "0.0001".equals((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("-4"),mcdef).toString()));
        TestFmwk.assertTrue("pow011", "0.001".equals((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("-3"),mcdef).toString()));
        TestFmwk.assertTrue("pow012", "0.01".equals((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("-2"),mcdef).toString()));
        TestFmwk.assertTrue("pow013", "0.1".equals((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("-1"),mcdef).toString()));
        TestFmwk.assertTrue("pow014", "1".equals((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("0"),mcdef).toString()));
        TestFmwk.assertTrue("pow015", "10".equals((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("1"),mcdef).toString()));
        TestFmwk.assertTrue("pow016", "100000000".equals((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("8"),mcdef).toString()));
        TestFmwk.assertTrue("pow017", ("1E+9").equals((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("9"),mcdef).toString()));
        TestFmwk.assertTrue("pow018", ("1E+99").equals((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("99"),mcdef).toString()));
        TestFmwk.assertTrue("pow019", ("1E+999999999").equals((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("999999999"),mcdef).toString()));
        TestFmwk.assertTrue("pow020", ("1E+999999998").equals((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("999999998"),mcdef).toString()));
        TestFmwk.assertTrue("pow021", ("1E+999999997").equals((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("999999997"),mcdef).toString()));
        TestFmwk.assertTrue("pow022", ("1E+333333333").equals((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("333333333"),mcdef).toString()));
        TestFmwk.assertTrue("pow023", ("1E-333333333").equals((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("-333333333"),mcdef).toString()));
        TestFmwk.assertTrue("pow024", ("1E-999999998").equals((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("-999999998"),mcdef).toString()));
        TestFmwk.assertTrue("pow025", ("1E-999999999").equals((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("-999999999"),mcdef).toString()));
        TestFmwk.assertTrue("pow026", "0.5".equals((new android.icu.math.BigDecimal("2")).pow(new android.icu.math.BigDecimal("-1"),mcdef).toString()));
        TestFmwk.assertTrue("pow027", "0.25".equals((new android.icu.math.BigDecimal("2")).pow(new android.icu.math.BigDecimal("-2"),mcdef).toString()));
        TestFmwk.assertTrue("pow028", "0.0625".equals((new android.icu.math.BigDecimal("2")).pow(new android.icu.math.BigDecimal("-4"),mcdef).toString()));

        TestFmwk.assertTrue("pow050", ((new android.icu.math.BigDecimal("0")).pow(new android.icu.math.BigDecimal("0"),mcdef).toString()).equals("1"));
        TestFmwk.assertTrue("pow051", ((new android.icu.math.BigDecimal("0")).pow(new android.icu.math.BigDecimal("1"),mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("pow052", ((new android.icu.math.BigDecimal("0")).pow(new android.icu.math.BigDecimal("2"),mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("pow053", ((new android.icu.math.BigDecimal("1")).pow(new android.icu.math.BigDecimal("0"),mcdef).toString()).equals("1"));
        TestFmwk.assertTrue("pow054", ((new android.icu.math.BigDecimal("1")).pow(new android.icu.math.BigDecimal("1"),mcdef).toString()).equals("1"));
        TestFmwk.assertTrue("pow055", ((new android.icu.math.BigDecimal("1")).pow(new android.icu.math.BigDecimal("2"),mcdef).toString()).equals("1"));
        TestFmwk.assertTrue("pow056", ((new android.icu.math.BigDecimal("0.3")).pow(new android.icu.math.BigDecimal("0"),mcdef).toString()).equals("1"));
        TestFmwk.assertTrue("pow057", ((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("999999999"),mcdef).toString()).equals("1E+999999999"));
        TestFmwk.assertTrue("pow058", ((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("999999998"),mcdef).toString()).equals("1E+999999998"));
        TestFmwk.assertTrue("pow059", ((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("999999997"),mcdef).toString()).equals("1E+999999997"));
        TestFmwk.assertTrue("pow060", ((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("333333333"),mcdef).toString()).equals("1E+333333333"));
        TestFmwk.assertTrue("pow061", ((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("77"),mcdef).toString()).equals("1E+77"));
        TestFmwk.assertTrue("pow062", ((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("22"),mcdef).toString()).equals("1E+22"));
        TestFmwk.assertTrue("pow063", ((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("-77"),mcdef).toString()).equals("1E-77"));
        TestFmwk.assertTrue("pow064", ((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("-22"),mcdef).toString()).equals("1E-22"));
        TestFmwk.assertTrue("pow065", ((new android.icu.math.BigDecimal("2")).pow(new android.icu.math.BigDecimal("-1"),mcdef).toString()).equals("0.5"));
        TestFmwk.assertTrue("pow066", ((new android.icu.math.BigDecimal("2")).pow(new android.icu.math.BigDecimal("-2"),mcdef).toString()).equals("0.25"));
        TestFmwk.assertTrue("pow067", ((new android.icu.math.BigDecimal("2")).pow(new android.icu.math.BigDecimal("-4"),mcdef).toString()).equals("0.0625"));
        TestFmwk.assertTrue("pow068", ((new android.icu.math.BigDecimal("6.0")).pow(new android.icu.math.BigDecimal("2"),mcdef).toString()).equals("36"));
        TestFmwk.assertTrue("pow069", ((new android.icu.math.BigDecimal("-3")).pow(new android.icu.math.BigDecimal("2"),mcdef).toString()).equals("9"));/* from book */
        TestFmwk.assertTrue("pow070", ((new android.icu.math.BigDecimal("2")).pow(new android.icu.math.BigDecimal("2"),mcdef).pow(new android.icu.math.BigDecimal("3"),mcdef).toString()).equals("64"));/* from book */

        // 1998.12.14 Next test removed as pow() no longer rounds RHS [as per ANSI]
        // Test('pow071').ok=BigDecimal('2').pow(BigDecimal('2.000000001'),mcdef).toString == '4'/* check input rounding */

        /* General tests from original Rexx diagnostics */
        x = new android.icu.math.BigDecimal("0.5");
        temp = android.icu.math.BigDecimal.ONE;
        flag = true;
        {
            n = 1;
            for (; n <= 10; n++) {
                temp = temp.multiply(x).divide(android.icu.math.BigDecimal.ONE);
                flag = flag
                        & (x.pow(new android.icu.math.BigDecimal(n), mcdef)
                                .toString()).equals(temp.toString());
            }
        }/* n */
        TestFmwk.assertTrue("pow080", flag);

        x = new android.icu.math.BigDecimal("2");
        temp = android.icu.math.BigDecimal.ONE;
        flag = true;
        {
            n = 1;
            for (; n <= 29; n++) {
                temp = temp.multiply(x).divide(android.icu.math.BigDecimal.ONE);
                flag=flag&(x.pow(new android.icu.math.BigDecimal(n),mcdef).toString()).equals(temp.toString());
                flag=flag&(x.pow(new android.icu.math.BigDecimal(-n),mcdef).toString()).equals(android.icu.math.BigDecimal.ONE.divide(temp,mcdef).toString());
                /* Note that rounding errors are possible for larger "n" */
                /* due to the information content of the exponent */
            }
        }/* n */
        TestFmwk.assertTrue("pow081", flag);

        /* The Vienna case. Checks both setup and 1/acc working precision */
        // Modified 1998.12.14 as RHS no longer rounded before use (must fit)
        // Modified 1990.02.04 as LHS is now rounded (instead of truncated to guard)
        vx=new android.icu.math.BigDecimal("123456789E+10"); // lhs .. rounded to 1.23E+18
        vn=new android.icu.math.BigDecimal("-1.23000e+2"); // rhs .. [was: -1.23455e+2, rounds to -123]
        TestFmwk.assertTrue("pow090", (vx.pow(vn,mc3).toString()).equals("8.74E-2226"));

        // - fixed point versions ---
        TestFmwk.assertTrue("pow101", "1".equals((new android.icu.math.BigDecimal("0.3")).pow(new android.icu.math.BigDecimal("0")).toString()));
        TestFmwk.assertTrue("pow102", "0.3".equals((new android.icu.math.BigDecimal("0.3")).pow(new android.icu.math.BigDecimal("1")).toString()));
        TestFmwk.assertTrue("pow103", "0.3".equals((new android.icu.math.BigDecimal("0.3")).pow(new android.icu.math.BigDecimal("1.00")).toString()));
        TestFmwk.assertTrue("pow104", "0.09".equals((new android.icu.math.BigDecimal("0.3")).pow(new android.icu.math.BigDecimal("2")).toString()));
        TestFmwk.assertTrue("pow105", "0.09".equals((new android.icu.math.BigDecimal("0.3")).pow(new android.icu.math.BigDecimal("2.00")).toString()));
        TestFmwk.assertTrue("pow106", "10".equals((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("1")).toString()));
        TestFmwk.assertTrue("pow107", "100000000".equals((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("8")).toString()));
        TestFmwk.assertTrue("pow108", "1000000000".equals((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("9")).toString()));
        TestFmwk.assertTrue("pow109", "10000000000".equals((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("10")).toString()));
        TestFmwk.assertTrue("pow110", "1".equals((new android.icu.math.BigDecimal("2")).pow(new android.icu.math.BigDecimal("0")).toString()));
        TestFmwk.assertTrue("pow111", "16".equals((new android.icu.math.BigDecimal("2")).pow(new android.icu.math.BigDecimal("4")).toString()));
        TestFmwk.assertTrue("pow112", "256".equals((new android.icu.math.BigDecimal("2")).pow(new android.icu.math.BigDecimal("8")).toString()));
        TestFmwk.assertTrue("pow113", "1024".equals((new android.icu.math.BigDecimal("2")).pow(new android.icu.math.BigDecimal("10")).toString()));
        TestFmwk.assertTrue("pow114", "1.0510100501".equals((new android.icu.math.BigDecimal("1.01")).pow(new android.icu.math.BigDecimal("5")).toString()));

        TestFmwk.assertTrue("pow120", ((new android.icu.math.BigDecimal("0")).pow(new android.icu.math.BigDecimal("0")).toString()).equals("1"));
        TestFmwk.assertTrue("pow121", ((new android.icu.math.BigDecimal("0")).pow(new android.icu.math.BigDecimal("1")).toString()).equals("0"));
        TestFmwk.assertTrue("pow122", ((new android.icu.math.BigDecimal("0")).pow(new android.icu.math.BigDecimal("2")).toString()).equals("0"));
        TestFmwk.assertTrue("pow123", ((new android.icu.math.BigDecimal("1")).pow(new android.icu.math.BigDecimal("0")).toString()).equals("1"));
        TestFmwk.assertTrue("pow144", ((new android.icu.math.BigDecimal("1")).pow(new android.icu.math.BigDecimal("1")).toString()).equals("1"));
        TestFmwk.assertTrue("pow125", ((new android.icu.math.BigDecimal("1")).pow(new android.icu.math.BigDecimal("2")).toString()).equals("1"));
        TestFmwk.assertTrue("pow126", ((new android.icu.math.BigDecimal("0.3")).pow(new android.icu.math.BigDecimal("0")).toString()).equals("1"));
        TestFmwk.assertTrue("pow127", ((new android.icu.math.BigDecimal("10")).pow(new android.icu.math.BigDecimal("7")).toString()).equals("10000000"));
        TestFmwk.assertTrue("pow128", ((new android.icu.math.BigDecimal("6.0")).pow(new android.icu.math.BigDecimal("2")).toString()).equals("36.00"));
        TestFmwk.assertTrue("pow129", ((new android.icu.math.BigDecimal("6.00")).pow(new android.icu.math.BigDecimal("2")).toString()).equals("36.0000"));
        TestFmwk.assertTrue("pow130", ((new android.icu.math.BigDecimal("6.000")).pow(new android.icu.math.BigDecimal("2")).toString()).equals("36.000000"));
        TestFmwk.assertTrue("pow131", ((new android.icu.math.BigDecimal("-3")).pow(new android.icu.math.BigDecimal("2")).toString()).equals("9"));
        TestFmwk.assertTrue("pow132", ((new android.icu.math.BigDecimal("2")).pow(new android.icu.math.BigDecimal("2")).pow(new android.icu.math.BigDecimal("3")).toString()).equals("64"));

        /* errors */
        try {
            ten.pow((android.icu.math.BigDecimal) null);
            flag = false;
        } catch (java.lang.NullPointerException $67) {
            flag = true;
        }/* checknull */
        TestFmwk.assertTrue("pow150", flag);
        try {
            ten.pow(ten, (android.icu.math.MathContext) null);
            flag = false;
        } catch (java.lang.NullPointerException $68) {
            flag = true;
        }/* checknull2 */
        TestFmwk.assertTrue("pow151", flag);

        flag = true;
        try {
            tenlong.pow(android.icu.math.BigDecimal.ONE, mcld);
            flag = false;
        } catch (java.lang.ArithmeticException $69) {
            ae = $69;
            flag = (ae.getMessage()).equals("Too many digits:" + " "
                    + tenlong.toString());
        }/* checkdigits */
        TestFmwk.assertTrue("pow152", flag);

        try {
            android.icu.math.BigDecimal.ONE.pow(tenlong, mcld);
            flag = false;
        } catch (java.lang.ArithmeticException $70) {
            ae = $70;
            flag = (ae.getMessage()).equals("Too many digits:" + " "
                    + tenlong.toString());
        }/* checkdigits */
        TestFmwk.assertTrue("pow153", flag);

        try {
            android.icu.math.BigDecimal.ONE
                    .pow(new android.icu.math.BigDecimal("-71"));
            flag = false;
        } catch (java.lang.ArithmeticException $71) {
            ae = $71;
            flag = (ae.getMessage()).equals("Negative power: -71");
        }/* checkpos */
        TestFmwk.assertTrue("pow154", flag);

        try {
            android.icu.math.BigDecimal.ONE.pow(
                    new android.icu.math.BigDecimal("1234"), mc3);
            flag = false;
        } catch (java.lang.ArithmeticException $72) {
            ae = $72;
            flag = (ae.getMessage()).equals("Too many digits: 1234");
        }/* checkwhole */
        TestFmwk.assertTrue("pow155", flag);

        try {
            android.icu.math.BigDecimal.ONE.pow(
                    new android.icu.math.BigDecimal("12.34e+2"), mc3);
            flag = false;
        } catch (java.lang.ArithmeticException $73) {
            ae = $73;
            flag = (ae.getMessage()).equals("Too many digits: 1.234E+3");
        }/* checkwhole1 */
        TestFmwk.assertTrue("pow156", flag);

        try {
            android.icu.math.BigDecimal.ONE.pow(
                    new android.icu.math.BigDecimal("12.4"), mcdef);
            flag = false;
        } catch (java.lang.ArithmeticException $74) {
            ae = $74;
            flag = (ae.getMessage()).equals("Decimal part non-zero: 12.4");
        }/* checkwhole2 */
        TestFmwk.assertTrue("pow157", flag);

        try {
            android.icu.math.BigDecimal.ONE.pow(
                    new android.icu.math.BigDecimal("1.01"), mcdef);
            flag = false;
        } catch (java.lang.ArithmeticException $75) {
            ae = $75;
            flag = (ae.getMessage()).equals("Decimal part non-zero: 1.01");
        }/* checkwhole3 */
        TestFmwk.assertTrue("pow158", flag);

        try {
            android.icu.math.BigDecimal.ONE.pow(
                    new android.icu.math.BigDecimal("1.000000001"), mcdef);
            flag = false;
        } catch (java.lang.ArithmeticException $76) {
            ae = $76;
            flag = (ae.getMessage())
                    .equals("Decimal part non-zero: 1.000000001");
        }/* checkwhole4 */
        TestFmwk.assertTrue("pow159", flag);

        try {
            android.icu.math.BigDecimal.ONE.pow(
                    new android.icu.math.BigDecimal("1.000000001"), mc3);
            flag = false;
        } catch (java.lang.ArithmeticException $77) {
            ae = $77;
            flag = (ae.getMessage())
                    .equals("Decimal part non-zero: 1.000000001");
        }/* checkwhole5 */
        TestFmwk.assertTrue("pow160", flag);

        try {
            android.icu.math.BigDecimal.ONE
                    .pow(
                            new android.icu.math.BigDecimal(
                                    "5.67E-987654321"), mc3);
            flag = false;
        } catch (java.lang.ArithmeticException $78) {
            ae = $78;
            flag = (ae.getMessage())
                    .equals("Decimal part non-zero: 5.67E-987654321");
        }/* checkwhole6 */
        TestFmwk.assertTrue("pow161", flag);
    }

    /*--------------------------------------------------------------------*/

    /** Test the {@link android.icu.math.BigDecimal#remainder} method. */

    @Test
    public void diagremainder() {
        boolean flag = false;
        java.lang.ArithmeticException ae = null;

        TestFmwk.assertTrue("rem001", ((new android.icu.math.BigDecimal("1")).remainder(new android.icu.math.BigDecimal("3"),mcdef).toString()).equals("1"));
        TestFmwk.assertTrue("rem002", ((new android.icu.math.BigDecimal("5")).remainder(new android.icu.math.BigDecimal("5"),mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("rem003", ((new android.icu.math.BigDecimal("13")).remainder(new android.icu.math.BigDecimal("10"),mcdef).toString()).equals("3"));
        TestFmwk.assertTrue("rem004", ((new android.icu.math.BigDecimal("13")).remainder(new android.icu.math.BigDecimal("50"),mcdef).toString()).equals("13"));
        TestFmwk.assertTrue("rem005", ((new android.icu.math.BigDecimal("13")).remainder(new android.icu.math.BigDecimal("100"),mcdef).toString()).equals("13"));
        TestFmwk.assertTrue("rem006", ((new android.icu.math.BigDecimal("13")).remainder(new android.icu.math.BigDecimal("1000"),mcdef).toString()).equals("13"));
        TestFmwk.assertTrue("rem007", ((new android.icu.math.BigDecimal(".13")).remainder(one).toString()).equals("0.13"));
        TestFmwk.assertTrue("rem008", ((new android.icu.math.BigDecimal("0.133")).remainder(one).toString()).equals("0.133"));
        TestFmwk.assertTrue("rem009", ((new android.icu.math.BigDecimal("0.1033")).remainder(one).toString()).equals("0.1033"));
        TestFmwk.assertTrue("rem010", ((new android.icu.math.BigDecimal("1.033")).remainder(one).toString()).equals("0.033"));
        TestFmwk.assertTrue("rem011", ((new android.icu.math.BigDecimal("10.33")).remainder(one).toString()).equals("0.33"));
        TestFmwk.assertTrue("rem012", ((new android.icu.math.BigDecimal("10.33")).remainder(android.icu.math.BigDecimal.TEN).toString()).equals("0.33"));
        TestFmwk.assertTrue("rem013", ((new android.icu.math.BigDecimal("103.3")).remainder(android.icu.math.BigDecimal.ONE).toString()).equals("0.3"));
        TestFmwk.assertTrue("rem014", ((new android.icu.math.BigDecimal("133")).remainder(android.icu.math.BigDecimal.TEN).toString()).equals("3"));
        TestFmwk.assertTrue("rem015", ((new android.icu.math.BigDecimal("1033")).remainder(android.icu.math.BigDecimal.TEN).toString()).equals("3"));
        TestFmwk.assertTrue("rem016", ((new android.icu.math.BigDecimal("1033")).remainder(new android.icu.math.BigDecimal(50),mcdef).toString()).equals("33"));
        TestFmwk.assertTrue("rem017", ((new android.icu.math.BigDecimal("101.0")).remainder(new android.icu.math.BigDecimal(3),mcdef).toString()).equals("2.0"));
        TestFmwk.assertTrue("rem018", ((new android.icu.math.BigDecimal("102.0")).remainder(new android.icu.math.BigDecimal(3),mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("rem019", ((new android.icu.math.BigDecimal("103.0")).remainder(new android.icu.math.BigDecimal(3),mcdef).toString()).equals("1.0"));
        TestFmwk.assertTrue("rem020", ((new android.icu.math.BigDecimal("2.40")).remainder(one).toString()).equals("0.40"));
        TestFmwk.assertTrue("rem021", ((new android.icu.math.BigDecimal("2.400")).remainder(one).toString()).equals("0.400"));
        TestFmwk.assertTrue("rem022", ((new android.icu.math.BigDecimal("2.4")).remainder(one).toString()).equals("0.4"));
        TestFmwk.assertTrue("rem023", ((new android.icu.math.BigDecimal("2.4")).remainder(new android.icu.math.BigDecimal(2),mcdef).toString()).equals("0.4"));
        TestFmwk.assertTrue("rem024", ((new android.icu.math.BigDecimal("2.400")).remainder(new android.icu.math.BigDecimal(2),mcdef).toString()).equals("0.400"));
        TestFmwk.assertTrue("rem025", ((new android.icu.math.BigDecimal("1")).remainder(new android.icu.math.BigDecimal("0.3"),mcdef).toString()).equals("0.1"));
        TestFmwk.assertTrue("rem026", ((new android.icu.math.BigDecimal("1")).remainder(new android.icu.math.BigDecimal("0.30"),mcdef).toString()).equals("0.10"));
        TestFmwk.assertTrue("rem027", ((new android.icu.math.BigDecimal("1")).remainder(new android.icu.math.BigDecimal("0.300"),mcdef).toString()).equals("0.100"));
        TestFmwk.assertTrue("rem028", ((new android.icu.math.BigDecimal("1")).remainder(new android.icu.math.BigDecimal("0.3000"),mcdef).toString()).equals("0.1000"));
        TestFmwk.assertTrue("rem029", ((new android.icu.math.BigDecimal("1.0")).remainder(new android.icu.math.BigDecimal("0.3"),mcdef).toString()).equals("0.1"));
        TestFmwk.assertTrue("rem030", ((new android.icu.math.BigDecimal("1.00")).remainder(new android.icu.math.BigDecimal("0.3"),mcdef).toString()).equals("0.10"));
        TestFmwk.assertTrue("rem031", ((new android.icu.math.BigDecimal("1.000")).remainder(new android.icu.math.BigDecimal("0.3"),mcdef).toString()).equals("0.100"));
        TestFmwk.assertTrue("rem032", ((new android.icu.math.BigDecimal("1.0000")).remainder(new android.icu.math.BigDecimal("0.3"),mcdef).toString()).equals("0.1000"));
        TestFmwk.assertTrue("rem033", ((new android.icu.math.BigDecimal("0.5")).remainder(new android.icu.math.BigDecimal("2.001"),mcdef).toString()).equals("0.5"));

        TestFmwk.assertTrue("rem040", ((new android.icu.math.BigDecimal("0.5")).remainder(new android.icu.math.BigDecimal("0.5000001"),mcdef).toString()).equals("0.5"));
        TestFmwk.assertTrue("rem041", ((new android.icu.math.BigDecimal("0.5")).remainder(new android.icu.math.BigDecimal("0.50000001"),mcdef).toString()).equals("0.5"));
        TestFmwk.assertTrue("rem042", ((new android.icu.math.BigDecimal("0.5")).remainder(new android.icu.math.BigDecimal("0.500000001"),mcdef).toString()).equals("0.5"));
        TestFmwk.assertTrue("rem043", ((new android.icu.math.BigDecimal("0.5")).remainder(new android.icu.math.BigDecimal("0.5000000001"),mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("rem044", ((new android.icu.math.BigDecimal("0.5")).remainder(new android.icu.math.BigDecimal("0.50000000001"),mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("rem045", ((new android.icu.math.BigDecimal("0.5")).remainder(new android.icu.math.BigDecimal("0.4999999"),mcdef).toString()).equals("1E-7"));
        TestFmwk.assertTrue("rem046", ((new android.icu.math.BigDecimal("0.5")).remainder(new android.icu.math.BigDecimal("0.49999999"),mcdef).toString()).equals("1E-8"));
        TestFmwk.assertTrue("rem047", ((new android.icu.math.BigDecimal("0.5")).remainder(new android.icu.math.BigDecimal("0.499999999"),mcdef).toString()).equals("1E-9"));
        TestFmwk.assertTrue("rem048", ((new android.icu.math.BigDecimal("0.5")).remainder(new android.icu.math.BigDecimal("0.4999999999"),mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("rem049", ((new android.icu.math.BigDecimal("0.5")).remainder(new android.icu.math.BigDecimal("0.49999999999"),mcdef).toString()).equals("0"));

        TestFmwk.assertTrue("rem050", ((new android.icu.math.BigDecimal("0.03")).remainder(new android.icu.math.BigDecimal("7"),mcdef).toString()).equals("0.03"));
        TestFmwk.assertTrue("rem051", ((new android.icu.math.BigDecimal("5")).remainder(new android.icu.math.BigDecimal("2"),mcdef).toString()).equals("1"));
        TestFmwk.assertTrue("rem052", ((new android.icu.math.BigDecimal("4.1")).remainder(new android.icu.math.BigDecimal("2"),mcdef).toString()).equals("0.1"));
        TestFmwk.assertTrue("rem053", ((new android.icu.math.BigDecimal("4.01")).remainder(new android.icu.math.BigDecimal("2"),mcdef).toString()).equals("0.01"));
        TestFmwk.assertTrue("rem054", ((new android.icu.math.BigDecimal("4.001")).remainder(new android.icu.math.BigDecimal("2"),mcdef).toString()).equals("0.001"));
        TestFmwk.assertTrue("rem055", ((new android.icu.math.BigDecimal("4.0001")).remainder(new android.icu.math.BigDecimal("2"),mcdef).toString()).equals("0.0001"));
        TestFmwk.assertTrue("rem056", ((new android.icu.math.BigDecimal("4.00001")).remainder(new android.icu.math.BigDecimal("2"),mcdef).toString()).equals("0.00001"));
        TestFmwk.assertTrue("rem057", ((new android.icu.math.BigDecimal("4.000001")).remainder(new android.icu.math.BigDecimal("2"),mcdef).toString()).equals("0.000001"));
        TestFmwk.assertTrue("rem058", ((new android.icu.math.BigDecimal("4.0000001")).remainder(new android.icu.math.BigDecimal("2"),mcdef).toString()).equals("1E-7"));

        TestFmwk.assertTrue("rem060", ((new android.icu.math.BigDecimal("1.2")).remainder(new android.icu.math.BigDecimal("0.7345"),mcdef).toString()).equals("0.4655"));
        TestFmwk.assertTrue("rem061", ((new android.icu.math.BigDecimal("0.8")).remainder(new android.icu.math.BigDecimal("12"),mcdef).toString()).equals("0.8"));
        TestFmwk.assertTrue("rem062", ((new android.icu.math.BigDecimal("0.8")).remainder(new android.icu.math.BigDecimal("0.2"),mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("rem063", ((new android.icu.math.BigDecimal("0.8")).remainder(new android.icu.math.BigDecimal("0.3"),mcdef).toString()).equals("0.2"));
        TestFmwk.assertTrue("rem064", ((new android.icu.math.BigDecimal("0.800")).remainder(new android.icu.math.BigDecimal("12"),mcdef).toString()).equals("0.800"));
        TestFmwk.assertTrue("rem065", ((new android.icu.math.BigDecimal("0.800")).remainder(new android.icu.math.BigDecimal("1.7"),mcdef).toString()).equals("0.800"));
        TestFmwk.assertTrue("rem066", ((new android.icu.math.BigDecimal("2.400")).remainder(new android.icu.math.BigDecimal(2),mcdef).toString()).equals("0.400"));

        // MC --
        TestFmwk.assertTrue("rem071", ((new android.icu.math.BigDecimal("2.400")).remainder(new android.icu.math.BigDecimal(2),mc6).toString()).equals("0.400"));
        TestFmwk.assertTrue("rem072", ((new android.icu.math.BigDecimal("12345678900000")).remainder(new android.icu.math.BigDecimal("12e+12"),mc3).toString()).equals("3E+11"));

        // Fixed --
        TestFmwk.assertTrue("rem101", ((new android.icu.math.BigDecimal("1")).remainder(new android.icu.math.BigDecimal("3")).toString()).equals("1"));
        TestFmwk.assertTrue("rem102", ((new android.icu.math.BigDecimal("5")).remainder(new android.icu.math.BigDecimal("5")).toString()).equals("0"));
        TestFmwk.assertTrue("rem103", ((new android.icu.math.BigDecimal("13")).remainder(new android.icu.math.BigDecimal("10")).toString()).equals("3"));
        TestFmwk.assertTrue("rem104", ((new android.icu.math.BigDecimal("13")).remainder(new android.icu.math.BigDecimal("50")).toString()).equals("13"));
        TestFmwk.assertTrue("rem105", ((new android.icu.math.BigDecimal("13")).remainder(new android.icu.math.BigDecimal("100")).toString()).equals("13"));
        TestFmwk.assertTrue("rem106", ((new android.icu.math.BigDecimal("13")).remainder(new android.icu.math.BigDecimal("1000")).toString()).equals("13"));
        TestFmwk.assertTrue("rem107", ((new android.icu.math.BigDecimal(".13")).remainder(one).toString()).equals("0.13"));
        TestFmwk.assertTrue("rem108", ((new android.icu.math.BigDecimal("0.133")).remainder(one).toString()).equals("0.133"));
        TestFmwk.assertTrue("rem109", ((new android.icu.math.BigDecimal("0.1033")).remainder(one).toString()).equals("0.1033"));
        TestFmwk.assertTrue("rem110", ((new android.icu.math.BigDecimal("1.033")).remainder(one).toString()).equals("0.033"));
        TestFmwk.assertTrue("rem111", ((new android.icu.math.BigDecimal("10.33")).remainder(one).toString()).equals("0.33"));
        TestFmwk.assertTrue("rem112", ((new android.icu.math.BigDecimal("10.33")).remainder(android.icu.math.BigDecimal.TEN).toString()).equals("0.33"));
        TestFmwk.assertTrue("rem113", ((new android.icu.math.BigDecimal("103.3")).remainder(android.icu.math.BigDecimal.ONE).toString()).equals("0.3"));
        TestFmwk.assertTrue("rem114", ((new android.icu.math.BigDecimal("133")).remainder(android.icu.math.BigDecimal.TEN).toString()).equals("3"));
        TestFmwk.assertTrue("rem115", ((new android.icu.math.BigDecimal("1033")).remainder(android.icu.math.BigDecimal.TEN).toString()).equals("3"));
        TestFmwk.assertTrue("rem116", ((new android.icu.math.BigDecimal("1033")).remainder(new android.icu.math.BigDecimal(50)).toString()).equals("33"));
        TestFmwk.assertTrue("rem117", ((new android.icu.math.BigDecimal("101.0")).remainder(new android.icu.math.BigDecimal(3)).toString()).equals("2.0"));
        TestFmwk.assertTrue("rem118", ((new android.icu.math.BigDecimal("102.0")).remainder(new android.icu.math.BigDecimal(3)).toString()).equals("0"));
        TestFmwk.assertTrue("rem119", ((new android.icu.math.BigDecimal("103.0")).remainder(new android.icu.math.BigDecimal(3)).toString()).equals("1.0"));
        TestFmwk.assertTrue("rem120", ((new android.icu.math.BigDecimal("2.40")).remainder(one).toString()).equals("0.40"));
        TestFmwk.assertTrue("rem121", ((new android.icu.math.BigDecimal("2.400")).remainder(one).toString()).equals("0.400"));
        TestFmwk.assertTrue("rem122", ((new android.icu.math.BigDecimal("2.4")).remainder(one).toString()).equals("0.4"));
        TestFmwk.assertTrue("rem123", ((new android.icu.math.BigDecimal("2.4")).remainder(new android.icu.math.BigDecimal(2)).toString()).equals("0.4"));
        TestFmwk.assertTrue("rem124", ((new android.icu.math.BigDecimal("2.400")).remainder(new android.icu.math.BigDecimal(2)).toString()).equals("0.400"));
        TestFmwk.assertTrue("rem125", ((new android.icu.math.BigDecimal("1")).remainder(new android.icu.math.BigDecimal("0.3")).toString()).equals("0.1"));
        TestFmwk.assertTrue("rem126", ((new android.icu.math.BigDecimal("1")).remainder(new android.icu.math.BigDecimal("0.30")).toString()).equals("0.10"));
        TestFmwk.assertTrue("rem127", ((new android.icu.math.BigDecimal("1")).remainder(new android.icu.math.BigDecimal("0.300")).toString()).equals("0.100"));
        TestFmwk.assertTrue("rem128", ((new android.icu.math.BigDecimal("1")).remainder(new android.icu.math.BigDecimal("0.3000")).toString()).equals("0.1000"));
        TestFmwk.assertTrue("rem129", ((new android.icu.math.BigDecimal("1.0")).remainder(new android.icu.math.BigDecimal("0.3")).toString()).equals("0.1"));
        TestFmwk.assertTrue("rem130", ((new android.icu.math.BigDecimal("1.00")).remainder(new android.icu.math.BigDecimal("0.3")).toString()).equals("0.10"));
        TestFmwk.assertTrue("rem131", ((new android.icu.math.BigDecimal("1.000")).remainder(new android.icu.math.BigDecimal("0.3")).toString()).equals("0.100"));
        TestFmwk.assertTrue("rem132", ((new android.icu.math.BigDecimal("1.0000")).remainder(new android.icu.math.BigDecimal("0.3")).toString()).equals("0.1000"));
        TestFmwk.assertTrue("rem133", ((new android.icu.math.BigDecimal("0.5")).remainder(new android.icu.math.BigDecimal("2.001")).toString()).equals("0.5"));
        TestFmwk.assertTrue("rem134", ((new android.icu.math.BigDecimal("0.5")).remainder(new android.icu.math.BigDecimal("0.500000001")).toString()).equals("0.5"));
        TestFmwk.assertTrue("rem135", ((new android.icu.math.BigDecimal("0.5")).remainder(new android.icu.math.BigDecimal("0.5000000001")).toString()).equals("0.5"));
        TestFmwk.assertTrue("rem136", ((new android.icu.math.BigDecimal("0.03")).remainder(new android.icu.math.BigDecimal("7")).toString()).equals("0.03"));
        TestFmwk.assertTrue("rem137", ((new android.icu.math.BigDecimal("5")).remainder(new android.icu.math.BigDecimal("2")).toString()).equals("1"));
        TestFmwk.assertTrue("rem138", ((new android.icu.math.BigDecimal("4.1")).remainder(new android.icu.math.BigDecimal("2")).toString()).equals("0.1"));
        TestFmwk.assertTrue("rem139", ((new android.icu.math.BigDecimal("4.01")).remainder(new android.icu.math.BigDecimal("2")).toString()).equals("0.01"));
        TestFmwk.assertTrue("rem140", ((new android.icu.math.BigDecimal("4.001")).remainder(new android.icu.math.BigDecimal("2")).toString()).equals("0.001"));
        TestFmwk.assertTrue("rem141", ((new android.icu.math.BigDecimal("4.0001")).remainder(new android.icu.math.BigDecimal("2")).toString()).equals("0.0001"));
        TestFmwk.assertTrue("rem142", ((new android.icu.math.BigDecimal("4.00001")).remainder(new android.icu.math.BigDecimal("2")).toString()).equals("0.00001"));
        TestFmwk.assertTrue("rem143", ((new android.icu.math.BigDecimal("4.000001")).remainder(new android.icu.math.BigDecimal("2")).toString()).equals("0.000001"));
        TestFmwk.assertTrue("rem144", ((new android.icu.math.BigDecimal("4.0000001")).remainder(new android.icu.math.BigDecimal("2")).toString()).equals("0.0000001")); // 1E-7, plain
        TestFmwk.assertTrue("rem145", ((new android.icu.math.BigDecimal("1.2")).remainder(new android.icu.math.BigDecimal("0.7345")).toString()).equals("0.4655"));
        TestFmwk.assertTrue("rem146", ((new android.icu.math.BigDecimal("0.8")).remainder(new android.icu.math.BigDecimal("12")).toString()).equals("0.8"));
        TestFmwk.assertTrue("rem147", ((new android.icu.math.BigDecimal("0.8")).remainder(new android.icu.math.BigDecimal("0.2")).toString()).equals("0"));
        TestFmwk.assertTrue("rem148", ((new android.icu.math.BigDecimal("0.8")).remainder(new android.icu.math.BigDecimal("0.3")).toString()).equals("0.2"));
        TestFmwk.assertTrue("rem149", ((new android.icu.math.BigDecimal("0.800")).remainder(new android.icu.math.BigDecimal("12")).toString()).equals("0.800"));
        TestFmwk.assertTrue("rem150", ((new android.icu.math.BigDecimal("0.800")).remainder(new android.icu.math.BigDecimal("1.7")).toString()).equals("0.800"));
        TestFmwk.assertTrue("rem151", ((new android.icu.math.BigDecimal("2.400")).remainder(new android.icu.math.BigDecimal(2),mcdef).toString()).equals("0.400"));


        try {
            ten.remainder((android.icu.math.BigDecimal) null);
            flag = false;
        } catch (java.lang.NullPointerException $79) {
            flag = true;
        }/* checknull */
        TestFmwk.assertTrue("rem200", flag);
        try {
            ten.remainder(ten, (android.icu.math.MathContext) null);
            flag = false;
        } catch (java.lang.NullPointerException $80) {
            flag = true;
        }/* checknull2 */
        TestFmwk.assertTrue("rem201", flag);

        try {
            android.icu.math.BigDecimal.ONE.remainder(tenlong, mcld);
            flag = false;
        } catch (java.lang.ArithmeticException $81) {
            ae = $81;
            flag = (ae.getMessage()).equals("Too many digits:" + " "
                    + tenlong.toString());
        }/* checkdigits */
        TestFmwk.assertTrue("rem202", flag);

        try {
            tenlong.remainder(one, mcld);
            flag = false;
        } catch (java.lang.ArithmeticException $82) {
            ae = $82;
            flag = (ae.getMessage()).equals("Too many digits:" + " "
                    + tenlong.toString());
        }/* checkdigits */
        TestFmwk.assertTrue("rem203", flag);
    }

    /*--------------------------------------------------------------------*/

    /** Test the {@link android.icu.math.BigDecimal#subtract} method. */

    @Test
    public void diagsubtract() {
        boolean flag = false;
        android.icu.math.BigDecimal alhs;
        android.icu.math.BigDecimal arhs;
        java.lang.ArithmeticException ae = null;

        // [first group are 'quick confidence check']
        TestFmwk.assertTrue("sub301", ((new android.icu.math.BigDecimal(2)).subtract(new android.icu.math.BigDecimal(3),mcdef).toString()).equals("-1"));
        TestFmwk.assertTrue("sub302", ((new android.icu.math.BigDecimal("5.75")).subtract(new android.icu.math.BigDecimal("3.3"),mcdef).toString()).equals("2.45"));
        TestFmwk.assertTrue("sub303", ((new android.icu.math.BigDecimal("5")).subtract(new android.icu.math.BigDecimal("-3"),mcdef).toString()).equals("8"));
        TestFmwk.assertTrue("sub304", ((new android.icu.math.BigDecimal("-5")).subtract(new android.icu.math.BigDecimal("-3"),mcdef).toString()).equals("-2"));
        TestFmwk.assertTrue("sub305", ((new android.icu.math.BigDecimal("-7")).subtract(new android.icu.math.BigDecimal("2.5"),mcdef).toString()).equals("-9.5"));
        TestFmwk.assertTrue("sub306", ((new android.icu.math.BigDecimal("0.7")).subtract(new android.icu.math.BigDecimal("0.3"),mcdef).toString()).equals("0.4"));
        TestFmwk.assertTrue("sub307", ((new android.icu.math.BigDecimal("1.3")).subtract(new android.icu.math.BigDecimal("0.3"),mcdef).toString()).equals("1.0"));
        TestFmwk.assertTrue("sub308", ((new android.icu.math.BigDecimal("1.25")).subtract(new android.icu.math.BigDecimal("1.25"),mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("sub309", ((new android.icu.math.BigDecimal("1.23456789")).subtract(new android.icu.math.BigDecimal("1.00000000"),mcdef).toString()).equals("0.23456789"));

        TestFmwk.assertTrue("sub310", ((new android.icu.math.BigDecimal("1.23456789")).subtract(new android.icu.math.BigDecimal("1.00000089"),mcdef).toString()).equals("0.23456700"));

        TestFmwk.assertTrue("sub311", ((new android.icu.math.BigDecimal("0.5555555559")).subtract(new android.icu.math.BigDecimal("0.0000000001"),mcdef).toString()).equals("0.555555556"));

        TestFmwk.assertTrue("sub312", ((new android.icu.math.BigDecimal("0.5555555559")).subtract(new android.icu.math.BigDecimal("0.0000000005"),mcdef).toString()).equals("0.555555556"));

        TestFmwk.assertTrue("sub313", ((new android.icu.math.BigDecimal("0.4444444444")).subtract(new android.icu.math.BigDecimal("0.1111111111"),mcdef).toString()).equals("0.333333333"));

        TestFmwk.assertTrue("sub314", ((new android.icu.math.BigDecimal("1.0000000000")).subtract(new android.icu.math.BigDecimal("0.00000001"),mcdef).toString()).equals("0.99999999"));

        TestFmwk.assertTrue("sub315", ((new android.icu.math.BigDecimal("0.4444444444999")).subtract(new android.icu.math.BigDecimal("0"),mcdef).toString()).equals("0.444444444"));

        TestFmwk.assertTrue("sub316", ((new android.icu.math.BigDecimal("0.4444444445000")).subtract(new android.icu.math.BigDecimal("0"),mcdef).toString()).equals("0.444444445"));


        TestFmwk.assertTrue("sub317", ((new android.icu.math.BigDecimal("70")).subtract(new android.icu.math.BigDecimal("10000e+9"),mcdef).toString()).equals("-1.00000000E+13"));

        TestFmwk.assertTrue("sub318", ((new android.icu.math.BigDecimal("700")).subtract(new android.icu.math.BigDecimal("10000e+9"),mcdef).toString()).equals("-1.00000000E+13"));

        TestFmwk.assertTrue("sub319", ((new android.icu.math.BigDecimal("7000")).subtract(new android.icu.math.BigDecimal("10000e+9"),mcdef).toString()).equals("-1.00000000E+13"));

        TestFmwk.assertTrue("sub320", ((new android.icu.math.BigDecimal("70000")).subtract(new android.icu.math.BigDecimal("10000e+9"),mcdef).toString()).equals("-9.9999999E+12"));

        TestFmwk.assertTrue("sub321", ((new android.icu.math.BigDecimal("700000")).subtract(new android.icu.math.BigDecimal("10000e+9"),mcdef).toString()).equals("-9.9999993E+12"));

        // symmetry:
        TestFmwk.assertTrue("sub322", ((new android.icu.math.BigDecimal("10000e+9")).subtract(new android.icu.math.BigDecimal("70"),mcdef).toString()).equals("1.00000000E+13"));

        TestFmwk.assertTrue("sub323", ((new android.icu.math.BigDecimal("10000e+9")).subtract(new android.icu.math.BigDecimal("700"),mcdef).toString()).equals("1.00000000E+13"));

        TestFmwk.assertTrue("sub324", ((new android.icu.math.BigDecimal("10000e+9")).subtract(new android.icu.math.BigDecimal("7000"),mcdef).toString()).equals("1.00000000E+13"));

        TestFmwk.assertTrue("sub325", ((new android.icu.math.BigDecimal("10000e+9")).subtract(new android.icu.math.BigDecimal("70000"),mcdef).toString()).equals("9.9999999E+12"));

        TestFmwk.assertTrue("sub326", ((new android.icu.math.BigDecimal("10000e+9")).subtract(new android.icu.math.BigDecimal("700000"),mcdef).toString()).equals("9.9999993E+12"));


        // [same with fixed point arithmetic]
        TestFmwk.assertTrue("sub001", ((new android.icu.math.BigDecimal(2)).subtract(new android.icu.math.BigDecimal(3)).toString()).equals("-1"));
        TestFmwk.assertTrue("sub002", ((new android.icu.math.BigDecimal("5.75")).subtract(new android.icu.math.BigDecimal("3.3")).toString()).equals("2.45"));
        TestFmwk.assertTrue("sub003", ((new android.icu.math.BigDecimal("5")).subtract(new android.icu.math.BigDecimal("-3")).toString()).equals("8"));
        TestFmwk.assertTrue("sub004", ((new android.icu.math.BigDecimal("-5")).subtract(new android.icu.math.BigDecimal("-3")).toString()).equals("-2"));
        TestFmwk.assertTrue("sub005", ((new android.icu.math.BigDecimal("-7")).subtract(new android.icu.math.BigDecimal("2.5")).toString()).equals("-9.5"));
        TestFmwk.assertTrue("sub006", ((new android.icu.math.BigDecimal("0.7")).subtract(new android.icu.math.BigDecimal("0.3")).toString()).equals("0.4"));
        TestFmwk.assertTrue("sub007", ((new android.icu.math.BigDecimal("1.3")).subtract(new android.icu.math.BigDecimal("0.3")).toString()).equals("1.0"));
        TestFmwk.assertTrue("sub008", ((new android.icu.math.BigDecimal("1.25")).subtract(new android.icu.math.BigDecimal("1.25")).toString()).equals("0.00"));
        TestFmwk.assertTrue("sub009", ((new android.icu.math.BigDecimal("0.02")).subtract(new android.icu.math.BigDecimal("0.02")).toString()).equals("0.00"));

        TestFmwk.assertTrue("sub010", ((new android.icu.math.BigDecimal("1.23456789")).subtract(new android.icu.math.BigDecimal("1.00000000")).toString()).equals("0.23456789"));

        TestFmwk.assertTrue("sub011", ((new android.icu.math.BigDecimal("1.23456789")).subtract(new android.icu.math.BigDecimal("1.00000089")).toString()).equals("0.23456700"));

        TestFmwk.assertTrue("sub012", ((new android.icu.math.BigDecimal("0.5555555559")).subtract(new android.icu.math.BigDecimal("0.0000000001")).toString()).equals("0.5555555558"));

        TestFmwk.assertTrue("sub013", ((new android.icu.math.BigDecimal("0.5555555559")).subtract(new android.icu.math.BigDecimal("0.0000000005")).toString()).equals("0.5555555554"));

        TestFmwk.assertTrue("sub014", ((new android.icu.math.BigDecimal("0.4444444444")).subtract(new android.icu.math.BigDecimal("0.1111111111")).toString()).equals("0.3333333333"));

        TestFmwk.assertTrue("sub015", ((new android.icu.math.BigDecimal("1.0000000000")).subtract(new android.icu.math.BigDecimal("0.00000001")).toString()).equals("0.9999999900"));

        TestFmwk.assertTrue("sub016", ((new android.icu.math.BigDecimal("0.4444444444999")).subtract(new android.icu.math.BigDecimal("0")).toString()).equals("0.4444444444999"));

        TestFmwk.assertTrue("sub017", ((new android.icu.math.BigDecimal("0.4444444445000")).subtract(new android.icu.math.BigDecimal("0")).toString()).equals("0.4444444445000"));


        TestFmwk.assertTrue("sub018", ((new android.icu.math.BigDecimal("70")).subtract(new android.icu.math.BigDecimal("10000e+9")).toString()).equals("-9999999999930"));

        TestFmwk.assertTrue("sub019", ((new android.icu.math.BigDecimal("700")).subtract(new android.icu.math.BigDecimal("10000e+9")).toString()).equals("-9999999999300"));

        TestFmwk.assertTrue("sub020", ((new android.icu.math.BigDecimal("7000")).subtract(new android.icu.math.BigDecimal("10000e+9")).toString()).equals("-9999999993000"));

        TestFmwk.assertTrue("sub021", ((new android.icu.math.BigDecimal("70000")).subtract(new android.icu.math.BigDecimal("10000e+9")).toString()).equals("-9999999930000"));

        TestFmwk.assertTrue("sub022", ((new android.icu.math.BigDecimal("700000")).subtract(new android.icu.math.BigDecimal("10000e+9")).toString()).equals("-9999999300000"));

        // symmetry:
        TestFmwk.assertTrue("sub023", ((new android.icu.math.BigDecimal("10000e+9")).subtract(new android.icu.math.BigDecimal("70")).toString()).equals("9999999999930"));

        TestFmwk.assertTrue("sub024", ((new android.icu.math.BigDecimal("10000e+9")).subtract(new android.icu.math.BigDecimal("700")).toString()).equals("9999999999300"));

        TestFmwk.assertTrue("sub025", ((new android.icu.math.BigDecimal("10000e+9")).subtract(new android.icu.math.BigDecimal("7000")).toString()).equals("9999999993000"));

        TestFmwk.assertTrue("sub026", ((new android.icu.math.BigDecimal("10000e+9")).subtract(new android.icu.math.BigDecimal("70000")).toString()).equals("9999999930000"));

        TestFmwk.assertTrue("sub027", ((new android.icu.math.BigDecimal("10000e+9")).subtract(new android.icu.math.BigDecimal("700000")).toString()).equals("9999999300000"));

        // MC
        TestFmwk.assertTrue("sub030", ((new android.icu.math.BigDecimal("10000e+9")).subtract(new android.icu.math.BigDecimal("70000"),mcdef).toString()).equals("9.9999999E+12"));

        TestFmwk.assertTrue("sub031", ((new android.icu.math.BigDecimal("10000e+9")).subtract(new android.icu.math.BigDecimal("70000"),mc6).toString()).equals("1.00000E+13"));


        // some of the next group are really constructor tests
        TestFmwk.assertTrue("sub040", ((new android.icu.math.BigDecimal("00.0")).subtract(new android.icu.math.BigDecimal("0.0")).toString()).equals("0.0"));
        TestFmwk.assertTrue("sub041", ((new android.icu.math.BigDecimal("00.0")).subtract(new android.icu.math.BigDecimal("0.00")).toString()).equals("0.00"));
        TestFmwk.assertTrue("sub042", ((new android.icu.math.BigDecimal("0.00")).subtract(new android.icu.math.BigDecimal("00.0")).toString()).equals("0.00"));
        TestFmwk.assertTrue("sub043", ((new android.icu.math.BigDecimal("00.0")).subtract(new android.icu.math.BigDecimal("0.00"),mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("sub044", ((new android.icu.math.BigDecimal("0.00")).subtract(new android.icu.math.BigDecimal("00.0"),mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("sub045", ((new android.icu.math.BigDecimal("3")).subtract(new android.icu.math.BigDecimal(".3"),mcdef).toString()).equals("2.7"));
        TestFmwk.assertTrue("sub046", ((new android.icu.math.BigDecimal("3.")).subtract(new android.icu.math.BigDecimal(".3"),mcdef).toString()).equals("2.7"));
        TestFmwk.assertTrue("sub047", ((new android.icu.math.BigDecimal("3.0")).subtract(new android.icu.math.BigDecimal(".3"),mcdef).toString()).equals("2.7"));
        TestFmwk.assertTrue("sub048", ((new android.icu.math.BigDecimal("3.00")).subtract(new android.icu.math.BigDecimal(".3"),mcdef).toString()).equals("2.70"));
        TestFmwk.assertTrue("sub049", ((new android.icu.math.BigDecimal("3")).subtract(new android.icu.math.BigDecimal("3"),mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("sub050", ((new android.icu.math.BigDecimal("3")).subtract(new android.icu.math.BigDecimal("+3"),mcdef).toString()).equals("0"));
        TestFmwk.assertTrue("sub051", ((new android.icu.math.BigDecimal("3")).subtract(new android.icu.math.BigDecimal("-3"),mcdef).toString()).equals("6"));
        TestFmwk.assertTrue("sub052", ((new android.icu.math.BigDecimal("3")).subtract(new android.icu.math.BigDecimal(".3")).toString()).equals("2.7"));
        TestFmwk.assertTrue("sub053", ((new android.icu.math.BigDecimal("3.")).subtract(new android.icu.math.BigDecimal(".3")).toString()).equals("2.7"));
        TestFmwk.assertTrue("sub054", ((new android.icu.math.BigDecimal("3.0")).subtract(new android.icu.math.BigDecimal(".3")).toString()).equals("2.7"));
        TestFmwk.assertTrue("sub055", ((new android.icu.math.BigDecimal("3.00")).subtract(new android.icu.math.BigDecimal(".3")).toString()).equals("2.70"));
        TestFmwk.assertTrue("sub056", ((new android.icu.math.BigDecimal("3")).subtract(new android.icu.math.BigDecimal("3")).toString()).equals("0"));
        TestFmwk.assertTrue("sub057", ((new android.icu.math.BigDecimal("3")).subtract(new android.icu.math.BigDecimal("+3")).toString()).equals("0"));
        TestFmwk.assertTrue("sub058", ((new android.icu.math.BigDecimal("3")).subtract(new android.icu.math.BigDecimal("-3")).toString()).equals("6"));

        // the above all from add; massaged and extended. Now some new ones...
        // [particularly important for comparisons]
        // NB: -1E-7 below were non-exponents pre-ANSI
        TestFmwk.assertTrue("sub080", ("-1E-7").equals((new android.icu.math.BigDecimal("10.23456784")).subtract(new android.icu.math.BigDecimal("10.23456789"),mcdef).toString()));
        TestFmwk.assertTrue("sub081", "0".equals((new android.icu.math.BigDecimal("10.23456785")).subtract(new android.icu.math.BigDecimal("10.23456789"),mcdef).toString()));
        TestFmwk.assertTrue("sub082", "0".equals((new android.icu.math.BigDecimal("10.23456786")).subtract(new android.icu.math.BigDecimal("10.23456789"),mcdef).toString()));
        TestFmwk.assertTrue("sub083", "0".equals((new android.icu.math.BigDecimal("10.23456787")).subtract(new android.icu.math.BigDecimal("10.23456789"),mcdef).toString()));
        TestFmwk.assertTrue("sub084", "0".equals((new android.icu.math.BigDecimal("10.23456788")).subtract(new android.icu.math.BigDecimal("10.23456789"),mcdef).toString()));
        TestFmwk.assertTrue("sub085", "0".equals((new android.icu.math.BigDecimal("10.23456789")).subtract(new android.icu.math.BigDecimal("10.23456789"),mcdef).toString()));
        TestFmwk.assertTrue("sub086", "0".equals((new android.icu.math.BigDecimal("10.23456790")).subtract(new android.icu.math.BigDecimal("10.23456789"),mcdef).toString()));
        TestFmwk.assertTrue("sub087", "0".equals((new android.icu.math.BigDecimal("10.23456791")).subtract(new android.icu.math.BigDecimal("10.23456789"),mcdef).toString()));
        TestFmwk.assertTrue("sub088", "0".equals((new android.icu.math.BigDecimal("10.23456792")).subtract(new android.icu.math.BigDecimal("10.23456789"),mcdef).toString()));
        TestFmwk.assertTrue("sub089", "0".equals((new android.icu.math.BigDecimal("10.23456793")).subtract(new android.icu.math.BigDecimal("10.23456789"),mcdef).toString()));
        TestFmwk.assertTrue("sub090", "0".equals((new android.icu.math.BigDecimal("10.23456794")).subtract(new android.icu.math.BigDecimal("10.23456789"),mcdef).toString()));
        TestFmwk.assertTrue("sub091", ("-1E-7").equals((new android.icu.math.BigDecimal("10.23456781")).subtract(new android.icu.math.BigDecimal("10.23456786"),mcdef).toString()));
        TestFmwk.assertTrue("sub092", ("-1E-7").equals((new android.icu.math.BigDecimal("10.23456782")).subtract(new android.icu.math.BigDecimal("10.23456786"),mcdef).toString()));
        TestFmwk.assertTrue("sub093", ("-1E-7").equals((new android.icu.math.BigDecimal("10.23456783")).subtract(new android.icu.math.BigDecimal("10.23456786"),mcdef).toString()));
        TestFmwk.assertTrue("sub094", ("-1E-7").equals((new android.icu.math.BigDecimal("10.23456784")).subtract(new android.icu.math.BigDecimal("10.23456786"),mcdef).toString()));
        TestFmwk.assertTrue("sub095", "0".equals((new android.icu.math.BigDecimal("10.23456785")).subtract(new android.icu.math.BigDecimal("10.23456786"),mcdef).toString()));
        TestFmwk.assertTrue("sub096", "0".equals((new android.icu.math.BigDecimal("10.23456786")).subtract(new android.icu.math.BigDecimal("10.23456786"),mcdef).toString()));
        TestFmwk.assertTrue("sub097", "0".equals((new android.icu.math.BigDecimal("10.23456787")).subtract(new android.icu.math.BigDecimal("10.23456786"),mcdef).toString()));
        TestFmwk.assertTrue("sub098", "0".equals((new android.icu.math.BigDecimal("10.23456788")).subtract(new android.icu.math.BigDecimal("10.23456786"),mcdef).toString()));
        TestFmwk.assertTrue("sub099", "0".equals((new android.icu.math.BigDecimal("10.23456789")).subtract(new android.icu.math.BigDecimal("10.23456786"),mcdef).toString()));
        TestFmwk.assertTrue("sub100", "0".equals((new android.icu.math.BigDecimal("10.23456790")).subtract(new android.icu.math.BigDecimal("10.23456786"),mcdef).toString()));
        TestFmwk.assertTrue("sub101", "0".equals((new android.icu.math.BigDecimal("10.23456791")).subtract(new android.icu.math.BigDecimal("10.23456786"),mcdef).toString()));
        TestFmwk.assertTrue("sub102", "0".equals(android.icu.math.BigDecimal.ONE.subtract(new android.icu.math.BigDecimal("0.999999999"),mcdef).toString()));
        TestFmwk.assertTrue("sub103", "0".equals((new android.icu.math.BigDecimal("0.999999999")).subtract(android.icu.math.BigDecimal.ONE,mcdef).toString()));

        alhs = new android.icu.math.BigDecimal("12345678900000");
        arhs = new android.icu.math.BigDecimal("9999999999999");
        TestFmwk.assertTrue("sub110", (alhs.subtract(arhs, mc3).toString()).equals("2.3E+12"));
        TestFmwk.assertTrue("sub111", (arhs.subtract(alhs, mc3).toString()).equals("-2.3E+12"));
        TestFmwk.assertTrue("sub112", (alhs.subtract(arhs).toString()).equals("2345678900001"));
        TestFmwk.assertTrue("sub113", (arhs.subtract(alhs).toString()).equals("-2345678900001"));

        // additional scaled arithmetic tests [0.97 problem]
        TestFmwk.assertTrue("sub120", ((new android.icu.math.BigDecimal("0")).subtract(new android.icu.math.BigDecimal(".1")).toString()).equals("-0.1"));
        TestFmwk.assertTrue("sub121", ((new android.icu.math.BigDecimal("00")).subtract(new android.icu.math.BigDecimal(".97983")).toString()).equals("-0.97983"));
        TestFmwk.assertTrue("sub122", ((new android.icu.math.BigDecimal("0")).subtract(new android.icu.math.BigDecimal(".9")).toString()).equals("-0.9"));
        TestFmwk.assertTrue("sub123", ((new android.icu.math.BigDecimal("0")).subtract(new android.icu.math.BigDecimal("0.102")).toString()).equals("-0.102"));
        TestFmwk.assertTrue("sub124", ((new android.icu.math.BigDecimal("0")).subtract(new android.icu.math.BigDecimal(".4")).toString()).equals("-0.4"));
        TestFmwk.assertTrue("sub125", ((new android.icu.math.BigDecimal("0")).subtract(new android.icu.math.BigDecimal(".307")).toString()).equals("-0.307"));
        TestFmwk.assertTrue("sub126", ((new android.icu.math.BigDecimal("0")).subtract(new android.icu.math.BigDecimal(".43822")).toString()).equals("-0.43822"));
        TestFmwk.assertTrue("sub127", ((new android.icu.math.BigDecimal("0")).subtract(new android.icu.math.BigDecimal(".911")).toString()).equals("-0.911"));
        TestFmwk.assertTrue("sub128", ((new android.icu.math.BigDecimal(".0")).subtract(new android.icu.math.BigDecimal(".02")).toString()).equals("-0.02"));
        TestFmwk.assertTrue("sub129", ((new android.icu.math.BigDecimal("00")).subtract(new android.icu.math.BigDecimal(".392")).toString()).equals("-0.392"));
        TestFmwk.assertTrue("sub130", ((new android.icu.math.BigDecimal("0")).subtract(new android.icu.math.BigDecimal(".26")).toString()).equals("-0.26"));
        TestFmwk.assertTrue("sub131", ((new android.icu.math.BigDecimal("0")).subtract(new android.icu.math.BigDecimal("0.51")).toString()).equals("-0.51"));
        TestFmwk.assertTrue("sub132", ((new android.icu.math.BigDecimal("0")).subtract(new android.icu.math.BigDecimal(".2234")).toString()).equals("-0.2234"));
        TestFmwk.assertTrue("sub133", ((new android.icu.math.BigDecimal("0")).subtract(new android.icu.math.BigDecimal(".2")).toString()).equals("-0.2"));
        TestFmwk.assertTrue("sub134", ((new android.icu.math.BigDecimal(".0")).subtract(new android.icu.math.BigDecimal(".0008")).toString()).equals("-0.0008"));
        // 0. on left
        TestFmwk.assertTrue("sub140", ((new android.icu.math.BigDecimal("0.0")).subtract(new android.icu.math.BigDecimal("-.1")).toString()).equals("0.1"));
        TestFmwk.assertTrue("sub141", ((new android.icu.math.BigDecimal("0.00")).subtract(new android.icu.math.BigDecimal("-.97983")).toString()).equals("0.97983"));
        TestFmwk.assertTrue("sub142", ((new android.icu.math.BigDecimal("0.0")).subtract(new android.icu.math.BigDecimal("-.9")).toString()).equals("0.9"));
        TestFmwk.assertTrue("sub143", ((new android.icu.math.BigDecimal("0.0")).subtract(new android.icu.math.BigDecimal("-0.102")).toString()).equals("0.102"));
        TestFmwk.assertTrue("sub144", ((new android.icu.math.BigDecimal("0.0")).subtract(new android.icu.math.BigDecimal("-.4")).toString()).equals("0.4"));
        TestFmwk.assertTrue("sub145", ((new android.icu.math.BigDecimal("0.0")).subtract(new android.icu.math.BigDecimal("-.307")).toString()).equals("0.307"));
        TestFmwk.assertTrue("sub146", ((new android.icu.math.BigDecimal("0.0")).subtract(new android.icu.math.BigDecimal("-.43822")).toString()).equals("0.43822"));
        TestFmwk.assertTrue("sub147", ((new android.icu.math.BigDecimal("0.0")).subtract(new android.icu.math.BigDecimal("-.911")).toString()).equals("0.911"));
        TestFmwk.assertTrue("sub148", ((new android.icu.math.BigDecimal("0.0")).subtract(new android.icu.math.BigDecimal("-.02")).toString()).equals("0.02"));
        TestFmwk.assertTrue("sub149", ((new android.icu.math.BigDecimal("0.00")).subtract(new android.icu.math.BigDecimal("-.392")).toString()).equals("0.392"));
        TestFmwk.assertTrue("sub150", ((new android.icu.math.BigDecimal("0.0")).subtract(new android.icu.math.BigDecimal("-.26")).toString()).equals("0.26"));
        TestFmwk.assertTrue("sub151", ((new android.icu.math.BigDecimal("0.0")).subtract(new android.icu.math.BigDecimal("-0.51")).toString()).equals("0.51"));
        TestFmwk.assertTrue("sub152", ((new android.icu.math.BigDecimal("0.0")).subtract(new android.icu.math.BigDecimal("-.2234")).toString()).equals("0.2234"));
        TestFmwk.assertTrue("sub153", ((new android.icu.math.BigDecimal("0.0")).subtract(new android.icu.math.BigDecimal("-.2")).toString()).equals("0.2"));
        TestFmwk.assertTrue("sub154", ((new android.icu.math.BigDecimal("0.0")).subtract(new android.icu.math.BigDecimal("-.0008")).toString()).equals("0.0008"));
        // negatives of same
        TestFmwk.assertTrue("sub160", ((new android.icu.math.BigDecimal("0")).subtract(new android.icu.math.BigDecimal("-.1")).toString()).equals("0.1"));
        TestFmwk.assertTrue("sub161", ((new android.icu.math.BigDecimal("00")).subtract(new android.icu.math.BigDecimal("-.97983")).toString()).equals("0.97983"));
        TestFmwk.assertTrue("sub162", ((new android.icu.math.BigDecimal("0")).subtract(new android.icu.math.BigDecimal("-.9")).toString()).equals("0.9"));
        TestFmwk.assertTrue("sub163", ((new android.icu.math.BigDecimal("0")).subtract(new android.icu.math.BigDecimal("-0.102")).toString()).equals("0.102"));
        TestFmwk.assertTrue("sub164", ((new android.icu.math.BigDecimal("0")).subtract(new android.icu.math.BigDecimal("-.4")).toString()).equals("0.4"));
        TestFmwk.assertTrue("sub165", ((new android.icu.math.BigDecimal("0")).subtract(new android.icu.math.BigDecimal("-.307")).toString()).equals("0.307"));
        TestFmwk.assertTrue("sub166", ((new android.icu.math.BigDecimal("0")).subtract(new android.icu.math.BigDecimal("-.43822")).toString()).equals("0.43822"));
        TestFmwk.assertTrue("sub167", ((new android.icu.math.BigDecimal("0")).subtract(new android.icu.math.BigDecimal("-.911")).toString()).equals("0.911"));
        TestFmwk.assertTrue("sub168", ((new android.icu.math.BigDecimal(".0")).subtract(new android.icu.math.BigDecimal("-.02")).toString()).equals("0.02"));
        TestFmwk.assertTrue("sub169", ((new android.icu.math.BigDecimal("00")).subtract(new android.icu.math.BigDecimal("-.392")).toString()).equals("0.392"));
        TestFmwk.assertTrue("sub170", ((new android.icu.math.BigDecimal("0")).subtract(new android.icu.math.BigDecimal("-.26")).toString()).equals("0.26"));
        TestFmwk.assertTrue("sub171", ((new android.icu.math.BigDecimal("0")).subtract(new android.icu.math.BigDecimal("-0.51")).toString()).equals("0.51"));
        TestFmwk.assertTrue("sub172", ((new android.icu.math.BigDecimal("0")).subtract(new android.icu.math.BigDecimal("-.2234")).toString()).equals("0.2234"));
        TestFmwk.assertTrue("sub173", ((new android.icu.math.BigDecimal("0")).subtract(new android.icu.math.BigDecimal("-.2")).toString()).equals("0.2"));
        TestFmwk.assertTrue("sub174", ((new android.icu.math.BigDecimal(".0")).subtract(new android.icu.math.BigDecimal("-.0008")).toString()).equals("0.0008"));

        // more fixed, LHS swaps [really same as testcases under add]
        TestFmwk.assertTrue("sub180", ((new android.icu.math.BigDecimal("-56267E-10")).subtract(zero).toString()).equals("-0.0000056267"));
        TestFmwk.assertTrue("sub181", ((new android.icu.math.BigDecimal("-56267E-5")).subtract(zero).toString()).equals("-0.56267"));
        TestFmwk.assertTrue("sub182", ((new android.icu.math.BigDecimal("-56267E-2")).subtract(zero).toString()).equals("-562.67"));
        TestFmwk.assertTrue("sub183", ((new android.icu.math.BigDecimal("-56267E-1")).subtract(zero).toString()).equals("-5626.7"));
        TestFmwk.assertTrue("sub185", ((new android.icu.math.BigDecimal("-56267E-0")).subtract(zero).toString()).equals("-56267"));

        try {
            ten.subtract((android.icu.math.BigDecimal) null);
            flag = false;
        } catch (java.lang.NullPointerException $83) {
            flag = true;
        }/* checknull */
        TestFmwk.assertTrue("sub200", flag);
        try {
            ten.subtract(ten, (android.icu.math.MathContext) null);
            flag = false;
        } catch (java.lang.NullPointerException $84) {
            flag = true;
        }/* checknull2 */
        TestFmwk.assertTrue("sub201", flag);

        try {
            android.icu.math.BigDecimal.ONE.subtract(tenlong, mcld);
            flag = false;
        } catch (java.lang.ArithmeticException $85) {
            ae = $85;
            flag = (ae.getMessage()).equals("Too many digits:" + " "
                    + tenlong.toString());
        }/* checkdigits */
        TestFmwk.assertTrue("sub202", flag);
        try {
            tenlong.subtract(android.icu.math.BigDecimal.ONE, mcld);
            flag = false;
        } catch (java.lang.ArithmeticException $86) {
            ae = $86;
            flag = (ae.getMessage()).equals("Too many digits:" + " "
                    + tenlong.toString());
        }/* checkdigits */
        TestFmwk.assertTrue("sub203", flag);
    }

    /* ----------------------------------------------------------------- */

    /* ----------------------------------------------------------------- */
    /* Other methods */
    /* ----------------------------------------------------------------- */

    /** Test the <code>BigDecimal.byteValue()</code> method. */

    @Test
    public void diagbyteValue() {
        boolean flag = false;
        java.lang.String v = null;
        java.lang.ArithmeticException ae = null;
        java.lang.String badstrings[];
        int i = 0;
        java.lang.String norm = null;

        TestFmwk.assertTrue("byv001", ((((byte)-128)))==((new android.icu.math.BigDecimal("-128")).byteValue()));
        TestFmwk.assertTrue("byv002", ((0))==((new android.icu.math.BigDecimal("0")).byteValue()));
        TestFmwk.assertTrue("byv003", ((1))==((new android.icu.math.BigDecimal("1")).byteValue()));
        TestFmwk.assertTrue("byv004", ((99))==((new android.icu.math.BigDecimal("99")).byteValue()));
        TestFmwk.assertTrue("byv005", ((127))==((new android.icu.math.BigDecimal("127")).byteValue()));
        TestFmwk.assertTrue("byv006", ((-128))==((new android.icu.math.BigDecimal("128")).byteValue()));
        TestFmwk.assertTrue("byv007", ((-127))==((new android.icu.math.BigDecimal("129")).byteValue()));
        TestFmwk.assertTrue("byv008", ((127))==((new android.icu.math.BigDecimal("-129")).byteValue()));
        TestFmwk.assertTrue("byv009", ((126))==((new android.icu.math.BigDecimal("-130")).byteValue()));
        TestFmwk.assertTrue("byv010", ((bmax))==((new android.icu.math.BigDecimal(bmax)).byteValue()));
        TestFmwk.assertTrue("byv011", ((bmin))==((new android.icu.math.BigDecimal(bmin)).byteValue()));
        TestFmwk.assertTrue("byv012", ((bneg))==((new android.icu.math.BigDecimal(bneg)).byteValue()));
        TestFmwk.assertTrue("byv013", ((bzer))==((new android.icu.math.BigDecimal(bzer)).byteValue()));
        TestFmwk.assertTrue("byv014", ((bpos))==((new android.icu.math.BigDecimal(bpos)).byteValue()));
        TestFmwk.assertTrue("byv015", ((bmin))==((new android.icu.math.BigDecimal(bmax+1)).byteValue()));
        TestFmwk.assertTrue("byv016", ((bmax))==((new android.icu.math.BigDecimal(bmin-1)).byteValue()));

        TestFmwk.assertTrue("byv021", ((((byte)-128)))==((new android.icu.math.BigDecimal("-128")).byteValueExact()));
        TestFmwk.assertTrue("byv022", ((0))==((new android.icu.math.BigDecimal("0")).byteValueExact()));
        TestFmwk.assertTrue("byv023", ((1))==((new android.icu.math.BigDecimal("1")).byteValueExact()));
        TestFmwk.assertTrue("byv024", ((99))==((new android.icu.math.BigDecimal("99")).byteValueExact()));
        TestFmwk.assertTrue("byv025", ((127))==((new android.icu.math.BigDecimal("127")).byteValueExact()));
        TestFmwk.assertTrue("byv026", ((bmax))==((new android.icu.math.BigDecimal(bmax)).byteValueExact()));
        TestFmwk.assertTrue("byv027", ((bmin))==((new android.icu.math.BigDecimal(bmin)).byteValueExact()));
        TestFmwk.assertTrue("byv028", ((bneg))==((new android.icu.math.BigDecimal(bneg)).byteValueExact()));
        TestFmwk.assertTrue("byv029", ((bzer))==((new android.icu.math.BigDecimal(bzer)).byteValueExact()));
        TestFmwk.assertTrue("byv030", ((bpos))==((new android.icu.math.BigDecimal(bpos)).byteValueExact()));
        try {
            v = "-129";
            (new android.icu.math.BigDecimal(v)).byteValueExact();
            flag = false;
        } catch (java.lang.ArithmeticException $87) {
            ae = $87;
            flag = (ae.getMessage()).equals("Conversion overflow:" + " " + v);
        }
        TestFmwk.assertTrue("byv100", flag);
        try {
            v = "128";
            (new android.icu.math.BigDecimal(v)).byteValueExact();
            flag = false;
        } catch (java.lang.ArithmeticException $88) {
            ae = $88;
            flag = (ae.getMessage()).equals("Conversion overflow:" + " " + v);
        }
        TestFmwk.assertTrue("byv101", flag);
        try {
            v = "1.5";
            (new android.icu.math.BigDecimal(v)).byteValueExact();
            flag = false;
        } catch (java.lang.ArithmeticException $89) {
            ae = $89;
            flag = (ae.getMessage()).equals("Decimal part non-zero:" + " " + v);
        }
        TestFmwk.assertTrue("byv102", flag);

        badstrings = new java.lang.String[] {
                "1234",
                (new android.icu.math.BigDecimal(bmax)).add(one).toString(),
                (new android.icu.math.BigDecimal(bmin)).subtract(one)
                        .toString(),
                "170",
                "270",
                "370",
                "470",
                "570",
                "670",
                "770",
                "870",
                "970",
                "-170",
                "-270",
                "-370",
                "-470",
                "-570",
                "-670",
                "-770",
                "-870",
                "-970",
                (new android.icu.math.BigDecimal(bmin)).multiply(two)
                        .toString(),
                (new android.icu.math.BigDecimal(bmax)).multiply(two)
                        .toString(),
                (new android.icu.math.BigDecimal(bmin)).multiply(ten)
                        .toString(),
                (new android.icu.math.BigDecimal(bmax)).multiply(ten)
                        .toString(), "-1234" }; // 220
        // 221
        // 222
        // 223
        // 224
        // 225
        // 226
        // 227
        // 228
        // 229
        // 230
        // 231
        // 232
        // 233
        // 234
        // 235
        // 236
        // 237
        // 238
        // 239
        // 240
        // 241
        // 242
        // 243
        // 244
        // 245
        {
            int $90 = badstrings.length;
            i = 0;
            for (; $90 > 0; $90--, i++) {
                try {
                    v = badstrings[i];
                    (new android.icu.math.BigDecimal(v)).byteValueExact();
                    flag = false;
                } catch (java.lang.ArithmeticException $91) {
                    ae = $91;
                    norm = (new android.icu.math.BigDecimal(v)).toString();
                    flag = (ae.getMessage()).equals("Conversion overflow:"
                            + " " + norm);
                }
                TestFmwk.assertTrue("byv" + (220 + i), flag);
            }
        }/* i */
    }

    /* ----------------------------------------------------------------- */

    /**
     * Test the {@link android.icu.math.BigDecimal#compareTo(java.lang.Object)}
     * method.
     */

    @Test
    public void diagcomparetoObj() {
//        boolean flag = false;
//        android.icu.math.BigDecimal d;
//        android.icu.math.BigDecimal long1;
//        android.icu.math.BigDecimal long2;
//
//        d = new android.icu.math.BigDecimal(17);
//        (new Test("cto001")).ok = (d
//                .compareTo((java.lang.Object) (new android.icu.math.BigDecimal(
//                        66)))) == (-1);
//        (new Test("cto002")).ok = (d
//                .compareTo((java.lang.Object) ((new android.icu.math.BigDecimal(
//                        10)).add(new android.icu.math.BigDecimal(7))))) == 0;
//        (new Test("cto003")).ok = (d
//                .compareTo((java.lang.Object) (new android.icu.math.BigDecimal(
//                        10)))) == 1;
//        long1 = new android.icu.math.BigDecimal("12345678903");
//        long2 = new android.icu.math.BigDecimal("12345678900");
//        TestFmwk.assertTrue("cto004", (long1.compareTo((java.lang.Object) long2)) == 1);
//        TestFmwk.assertTrue("cto005", (long2.compareTo((java.lang.Object) long1)) == (-1));
//        TestFmwk.assertTrue("cto006", (long2.compareTo((java.lang.Object) long2)) == 0);
//        try {
//            d.compareTo((java.lang.Object) null);
//            flag = false;
//        } catch (java.lang.NullPointerException $92) {
//            flag = true; // should get here
//        }
//        TestFmwk.assertTrue("cto101", flag);
//        try {
//            d.compareTo((java.lang.Object) "foo");
//            flag = false;
//        } catch (java.lang.ClassCastException $93) {
//            flag = true; // should get here
//        }
//        TestFmwk.assertTrue("cto102", flag);
//        summary("compareTo(Obj)");
    }

    /* ----------------------------------------------------------------- */

    /** Test the {@link android.icu.math.BigDecimal#doubleValue} method. */

    @Test
    public void diagdoublevalue() {
        java.lang.String val;
        // 1999.03.07 Infinities no longer errors
        val = "-1";
        TestFmwk.assertTrue("dov001", ((new android.icu.math.BigDecimal(val)).doubleValue()) == ((new java.lang.Double(val)).doubleValue()));
        val = "-0.1";
        TestFmwk.assertTrue("dov002", ((new android.icu.math.BigDecimal(val)).doubleValue()) == ((new java.lang.Double(val)).doubleValue()));
        val = "0";
        TestFmwk.assertTrue("dov003", ((new android.icu.math.BigDecimal(val)).doubleValue()) == ((new java.lang.Double(val)).doubleValue()));
        val = "0.1";
        TestFmwk.assertTrue("dov004", ((new android.icu.math.BigDecimal(val)).doubleValue()) == ((new java.lang.Double(val)).doubleValue()));
        val = "1";
        TestFmwk.assertTrue("dov005", ((new android.icu.math.BigDecimal(val)).doubleValue()) == ((new java.lang.Double(val)).doubleValue()));
        val = "1e1000";
        TestFmwk.assertTrue("dov006", ((new android.icu.math.BigDecimal(val)).doubleValue()) == java.lang.Double.POSITIVE_INFINITY);
        val = "-1e1000";
        TestFmwk.assertTrue("dov007", ((new android.icu.math.BigDecimal(val)).doubleValue()) == java.lang.Double.NEGATIVE_INFINITY);
    }

    /* ----------------------------------------------------------------- */

    /** Test the {@link android.icu.math.BigDecimal#equals} method. */

    @Test
    public void diagequals() {
        android.icu.math.BigDecimal d;
        d = new android.icu.math.BigDecimal(17);
        TestFmwk.assertTrue("equ001", (!(d.equals((java.lang.Object) null))));
        TestFmwk.assertTrue("equ002", (!(d.equals("foo"))));
        TestFmwk.assertTrue("equ003", (!(d.equals((new android.icu.math.BigDecimal(66))))));
        TestFmwk.assertTrue("equ004", d.equals(d));
        TestFmwk.assertTrue("equ005", d.equals(((new android.icu.math.BigDecimal(10)).add(new android.icu.math.BigDecimal(7)))));
    }

    /* ----------------------------------------------------------------- */

    /** Test the {@link android.icu.math.BigDecimal#floatValue} method. */

    @Test
    public void diagfloatvalue() {
        java.lang.String val;
        // 1999.03.07 Infinities no longer errors
        val = "-1";
        TestFmwk.assertTrue("flv001", ((new android.icu.math.BigDecimal(val)).floatValue()) == ((new java.lang.Float(val)).floatValue()));
        val = "-0.1";
        TestFmwk.assertTrue("flv002", ((new android.icu.math.BigDecimal(val)).floatValue()) == ((new java.lang.Float(val)).floatValue()));
        val = "0";
        TestFmwk.assertTrue("flv003", ((new android.icu.math.BigDecimal(val)).floatValue()) == ((new java.lang.Float(val)).floatValue()));
        val = "0.1";
        TestFmwk.assertTrue("flv004", ((new android.icu.math.BigDecimal(val)).floatValue()) == ((new java.lang.Float(val)).floatValue()));
        val = "1";
        TestFmwk.assertTrue("flv005", ((new android.icu.math.BigDecimal(val)).floatValue()) == ((new java.lang.Float(val)).floatValue()));
        val = "1e200";
        TestFmwk.assertTrue("flv006", ((new android.icu.math.BigDecimal(val)).floatValue()) == java.lang.Float.POSITIVE_INFINITY);
        val = "-1e200";
        TestFmwk.assertTrue("flv007", ((new android.icu.math.BigDecimal(val)).floatValue()) == java.lang.Float.NEGATIVE_INFINITY);
        val = "1e1000";
        TestFmwk.assertTrue("flv008", ((new android.icu.math.BigDecimal(val)).floatValue()) == java.lang.Float.POSITIVE_INFINITY);
        val = "-1e1000";
        TestFmwk.assertTrue("flv009", ((new android.icu.math.BigDecimal(val)).floatValue()) == java.lang.Float.NEGATIVE_INFINITY);
    }

    /* ----------------------------------------------------------------- */

    /** Test the {@link android.icu.math.BigDecimal#format} method. */

    @Test
    public void diagformat() {
        boolean flag = false;
        int eng;
        int sci;
        android.icu.math.BigDecimal d04;
        android.icu.math.BigDecimal d05;
        android.icu.math.BigDecimal d06;
        android.icu.math.BigDecimal d15;
        java.lang.IllegalArgumentException iae = null;
        android.icu.math.BigDecimal d050;
        android.icu.math.BigDecimal d150;
        android.icu.math.BigDecimal m050;
        android.icu.math.BigDecimal m150;
        android.icu.math.BigDecimal d051;
        android.icu.math.BigDecimal d151;
        android.icu.math.BigDecimal d000;
        android.icu.math.BigDecimal d500;
        java.lang.ArithmeticException ae = null;
        // 1999.02.09 now only two signatures for format(), so some tests below
        // may now be redundant

        TestFmwk.assertTrue("for001", ((new android.icu.math.BigDecimal("12.3")).format(-1,-1)).equals("12.3"));
        TestFmwk.assertTrue("for002", ((new android.icu.math.BigDecimal("-12.73")).format(-1,-1)).equals("-12.73"));
        TestFmwk.assertTrue("for003", ((new android.icu.math.BigDecimal("0.000")).format(-1,-1)).equals("0.000"));
        TestFmwk.assertTrue("for004", ((new android.icu.math.BigDecimal("3E+3")).format(-1,-1)).equals("3000"));
        TestFmwk.assertTrue("for005", ((new android.icu.math.BigDecimal("3")).format(4,-1)).equals("   3"));
        TestFmwk.assertTrue("for006", ((new android.icu.math.BigDecimal("1.73")).format(4,0)).equals("   2"));
        TestFmwk.assertTrue("for007", ((new android.icu.math.BigDecimal("1.73")).format(4,1)).equals("   1.7"));
        TestFmwk.assertTrue("for008", ((new android.icu.math.BigDecimal("1.75")).format(4,1)).equals("   1.8"));
        TestFmwk.assertTrue("for009", ((new android.icu.math.BigDecimal("0.5")).format(4,1)).equals("   0.5"));
        TestFmwk.assertTrue("for010", ((new android.icu.math.BigDecimal("0.05")).format(4,1)).equals("   0.1"));
        TestFmwk.assertTrue("for011", ((new android.icu.math.BigDecimal("0.04")).format(4,1)).equals("   0.0"));
        TestFmwk.assertTrue("for012", ((new android.icu.math.BigDecimal("0")).format(4,0)).equals("   0"));
        TestFmwk.assertTrue("for013", ((new android.icu.math.BigDecimal("0")).format(4,1)).equals("   0.0"));
        TestFmwk.assertTrue("for014", ((new android.icu.math.BigDecimal("0")).format(4,2)).equals("   0.00"));
        TestFmwk.assertTrue("for015", ((new android.icu.math.BigDecimal("0")).format(4,3)).equals("   0.000"));
        TestFmwk.assertTrue("for016", ((new android.icu.math.BigDecimal("0")).format(4,4)).equals("   0.0000"));
        TestFmwk.assertTrue("for017", ((new android.icu.math.BigDecimal("0.005")).format(4,0)).equals("   0"));
        TestFmwk.assertTrue("for018", ((new android.icu.math.BigDecimal("0.005")).format(4,1)).equals("   0.0"));
        TestFmwk.assertTrue("for019", ((new android.icu.math.BigDecimal("0.005")).format(4,2)).equals("   0.01"));
        TestFmwk.assertTrue("for020", ((new android.icu.math.BigDecimal("0.004")).format(4,2)).equals("   0.00"));
        TestFmwk.assertTrue("for021", ((new android.icu.math.BigDecimal("0.005")).format(4,3)).equals("   0.005"));
        TestFmwk.assertTrue("for022", ((new android.icu.math.BigDecimal("0.005")).format(4,4)).equals("   0.0050"));

        TestFmwk.assertTrue("for023", ((new android.icu.math.BigDecimal("1.73")).format(4,2)).equals("   1.73"));
        TestFmwk.assertTrue("for024", ((new android.icu.math.BigDecimal("1.73")).format(4,3)).equals("   1.730"));
        TestFmwk.assertTrue("for025", ((new android.icu.math.BigDecimal("-.76")).format(4,1)).equals("  -0.8"));
        TestFmwk.assertTrue("for026", ((new android.icu.math.BigDecimal("-12.73")).format(-1,4)).equals("-12.7300"));

        TestFmwk.assertTrue("for027", ((new android.icu.math.BigDecimal("3.03")).format(4,-1)).equals("   3.03"));
        TestFmwk.assertTrue("for028", ((new android.icu.math.BigDecimal("3.03")).format(4,1)).equals("   3.0"));
        TestFmwk.assertTrue("for029", ((new android.icu.math.BigDecimal("3.03")).format(4,-1,3,-1,-1,-1)).equals("   3.03     "));
        TestFmwk.assertTrue("for030", ((new android.icu.math.BigDecimal("3.03")).format(-1,-1,3,-1,-1,-1)).equals("3.03     "));
        TestFmwk.assertTrue("for031", ((new android.icu.math.BigDecimal("12345.73")).format(-1,-1,-1,4,-1,-1)).equals("1.234573E+4"));
        TestFmwk.assertTrue("for032", ((new android.icu.math.BigDecimal("12345.73")).format(-1,-1,-1,5,-1,-1)).equals("12345.73"));
        TestFmwk.assertTrue("for033", ((new android.icu.math.BigDecimal("12345.73")).format(-1,-1,-1,6,-1,-1)).equals("12345.73"));

        TestFmwk.assertTrue("for034", ((new android.icu.math.BigDecimal("12345.73")).format(-1,8,-1,3,-1,-1)).equals("1.23457300E+4"));
        TestFmwk.assertTrue("for035", ((new android.icu.math.BigDecimal("12345.73")).format(-1,7,-1,3,-1,-1)).equals("1.2345730E+4"));
        TestFmwk.assertTrue("for036", ((new android.icu.math.BigDecimal("12345.73")).format(-1,6,-1,3,-1,-1)).equals("1.234573E+4"));
        TestFmwk.assertTrue("for037", ((new android.icu.math.BigDecimal("12345.73")).format(-1,5,-1,3,-1,-1)).equals("1.23457E+4"));
        TestFmwk.assertTrue("for038", ((new android.icu.math.BigDecimal("12345.73")).format(-1,4,-1,3,-1,-1)).equals("1.2346E+4"));
        TestFmwk.assertTrue("for039", ((new android.icu.math.BigDecimal("12345.73")).format(-1,3,-1,3,-1,-1)).equals("1.235E+4"));
        TestFmwk.assertTrue("for040", ((new android.icu.math.BigDecimal("12345.73")).format(-1,2,-1,3,-1,-1)).equals("1.23E+4"));
        TestFmwk.assertTrue("for041", ((new android.icu.math.BigDecimal("12345.73")).format(-1,1,-1,3,-1,-1)).equals("1.2E+4"));
        TestFmwk.assertTrue("for042", ((new android.icu.math.BigDecimal("12345.73")).format(-1,0,-1,3,-1,-1)).equals("1E+4"));

        TestFmwk.assertTrue("for043", ((new android.icu.math.BigDecimal("99999.99")).format(-1,6,-1,3,-1,-1)).equals("9.999999E+4"));
        TestFmwk.assertTrue("for044", ((new android.icu.math.BigDecimal("99999.99")).format(-1,5,-1,3,-1,-1)).equals("1.00000E+5"));
        TestFmwk.assertTrue("for045", ((new android.icu.math.BigDecimal("99999.99")).format(-1,2,-1,3,-1,-1)).equals("1.00E+5"));
        TestFmwk.assertTrue("for046", ((new android.icu.math.BigDecimal("99999.99")).format(-1,0,-1,3,-1,-1)).equals("1E+5"));
        TestFmwk.assertTrue("for047", ((new android.icu.math.BigDecimal("99999.99")).format(3,0,-1,3,-1,-1)).equals("  1E+5"));

        TestFmwk.assertTrue("for048", ((new android.icu.math.BigDecimal("12345.73")).format(-1,-1,2,2,-1,-1)).equals("1.234573E+04"));
        TestFmwk.assertTrue("for049", ((new android.icu.math.BigDecimal("12345.73")).format(-1,3,-1,0,-1,-1)).equals("1.235E+4"));
        TestFmwk.assertTrue("for050", ((new android.icu.math.BigDecimal("1.234573")).format(-1,3,-1,0,-1,-1)).equals("1.235"));
        TestFmwk.assertTrue("for051", ((new android.icu.math.BigDecimal("123.45")).format(-1,3,2,0,-1,-1)).equals("1.235E+02"));

        TestFmwk.assertTrue("for052", ((new android.icu.math.BigDecimal("0.444")).format(-1,0)).equals("0"));
        TestFmwk.assertTrue("for053", ((new android.icu.math.BigDecimal("-0.444")).format(-1,0)).equals("0"));
        TestFmwk.assertTrue("for054", ((new android.icu.math.BigDecimal("0.4")).format(-1,0)).equals("0"));
        TestFmwk.assertTrue("for055", ((new android.icu.math.BigDecimal("-0.4")).format(-1,0)).equals("0"));

        eng = android.icu.math.MathContext.ENGINEERING;
        sci = android.icu.math.MathContext.SCIENTIFIC;
        TestFmwk.assertTrue("for060", ((new android.icu.math.BigDecimal("1234.5")).format(-1,3,2,0,eng,-1)).equals("1.235E+03"));
        TestFmwk.assertTrue("for061", ((new android.icu.math.BigDecimal("12345")).format(-1,3,3,0,eng,-1)).equals("12.345E+003"));
        TestFmwk.assertTrue("for062", ((new android.icu.math.BigDecimal("12345")).format(-1,3,3,0,sci,-1)).equals("1.235E+004"));
        TestFmwk.assertTrue("for063", ((new android.icu.math.BigDecimal("1234.5")).format(4,3,2,0,eng,-1)).equals("   1.235E+03"));
        TestFmwk.assertTrue("for064", ((new android.icu.math.BigDecimal("12345")).format(5,3,3,0,eng,-1)).equals("   12.345E+003"));
        TestFmwk.assertTrue("for065", ((new android.icu.math.BigDecimal("12345")).format(6,3,3,0,sci,-1)).equals("     1.235E+004"));

        TestFmwk.assertTrue("for066", ((new android.icu.math.BigDecimal("1.2345")).format(-1,3,2,0,-1,-1)).equals("1.235    "));
        TestFmwk.assertTrue("for067", ((new android.icu.math.BigDecimal("12345.73")).format(-1,-1,3,6,-1,-1)).equals("12345.73     "));
        TestFmwk.assertTrue("for068", ((new android.icu.math.BigDecimal("12345e+5")).format(-1,0)).equals("1234500000"));
        TestFmwk.assertTrue("for069", ((new android.icu.math.BigDecimal("12345e+5")).format(-1,1)).equals("1234500000.0"));
        TestFmwk.assertTrue("for070", ((new android.icu.math.BigDecimal("12345e+5")).format(-1,2)).equals("1234500000.00"));
        TestFmwk.assertTrue("for071", ((new android.icu.math.BigDecimal("12345e+5")).format(-1,3)).equals("1234500000.000"));
        TestFmwk.assertTrue("for072", ((new android.icu.math.BigDecimal("12345e+5")).format(-1,4)).equals("1234500000.0000"));

        // some from ANSI Dallas [Nov 1998]
        TestFmwk.assertTrue("for073", ((new android.icu.math.BigDecimal("99.999")).format(-1,2,-1,2,-1,-1)).equals("100.00"));
        TestFmwk.assertTrue("for074", ((new android.icu.math.BigDecimal("0.99999")).format(-1,4,2,2,-1,-1)).equals("1.0000    "));

        // try some rounding modes [default ROUND_HALF_UP widely tested above]
        // the first few also tests that defaults are accepted for the others
        d04 = new android.icu.math.BigDecimal("0.04");
        d05 = new android.icu.math.BigDecimal("0.05");
        d06 = new android.icu.math.BigDecimal("0.06");
        d15 = new android.icu.math.BigDecimal("0.15");
        TestFmwk.assertTrue("for080", (d05.format(-1, 1)).equals("0.1"));
        TestFmwk.assertTrue("for081", (d05.format(-1, 1, -1, -1, -1, android.icu.math.MathContext.ROUND_HALF_UP)).equals("0.1"));
        TestFmwk.assertTrue("for082", (d05.format(-1, 1, -1, -1, -1, -1)).equals("0.1"));
        TestFmwk.assertTrue("for083", (d05.format(-1, -1, -1, -1, -1, -1)).equals("0.05"));
        TestFmwk.assertTrue("for084", (d05.format(-1, -1)).equals("0.05"));
        try {
            d05.format(-1, -1, -1, -1, -1, 30); // bad mode
            flag = false; // shouldn't get here
        } catch (java.lang.IllegalArgumentException $94) {
            iae = $94;
            flag = (iae.getMessage()).equals("Bad argument 6 to format: 30");
        }
        TestFmwk.assertTrue("for085", flag);

        TestFmwk.assertTrue("for090", (d04.format(-1,1)).equals("0.0"));
        TestFmwk.assertTrue("for091", (d06.format(-1,1)).equals("0.1"));
        TestFmwk.assertTrue("for092", (d04.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_HALF_DOWN)).equals("0.0"));
        TestFmwk.assertTrue("for093", (d05.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_HALF_DOWN)).equals("0.0"));
        TestFmwk.assertTrue("for094", (d06.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_HALF_DOWN)).equals("0.1"));

        TestFmwk.assertTrue("for095", (d04.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_HALF_EVEN)).equals("0.0"));
        TestFmwk.assertTrue("for096", (d05.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_HALF_EVEN)).equals("0.0"));
        TestFmwk.assertTrue("for097", (d06.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_HALF_EVEN)).equals("0.1"));
        TestFmwk.assertTrue("for098", (d15.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_HALF_EVEN)).equals("0.2"));
        d050 = new android.icu.math.BigDecimal("0.050");
        d150 = new android.icu.math.BigDecimal("0.150");
        TestFmwk.assertTrue("for099", (d050.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_HALF_EVEN)).equals("0.0"));
        TestFmwk.assertTrue("for100", (d150.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_HALF_EVEN)).equals("0.2"));
        m050 = new android.icu.math.BigDecimal("-0.050");
        m150 = new android.icu.math.BigDecimal("-0.150");
        TestFmwk.assertTrue("for101", (m050.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_HALF_EVEN)).equals("0.0"));
        TestFmwk.assertTrue("for102", (m150.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_HALF_EVEN)).equals("-0.2"));
        d051 = new android.icu.math.BigDecimal("0.051");
        d151 = new android.icu.math.BigDecimal("0.151");
        TestFmwk.assertTrue("for103", (d051.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_HALF_EVEN)).equals("0.1"));
        TestFmwk.assertTrue("for104", (d151.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_HALF_EVEN)).equals("0.2"));

        TestFmwk.assertTrue("for105", (m050.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_CEILING)).equals("0.0"));
        TestFmwk.assertTrue("for106", (m150.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_CEILING)).equals("-0.1"));
        TestFmwk.assertTrue("for107", (d050.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_CEILING)).equals("0.1"));
        TestFmwk.assertTrue("for108", (d150.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_CEILING)).equals("0.2"));

        TestFmwk.assertTrue("for109", (m050.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_FLOOR)).equals("-0.1"));
        TestFmwk.assertTrue("for110", (m150.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_FLOOR)).equals("-0.2"));
        TestFmwk.assertTrue("for111", (d050.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_FLOOR)).equals("0.0"));
        TestFmwk.assertTrue("for112", (d150.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_FLOOR)).equals("0.1"));

        TestFmwk.assertTrue("for113", (m050.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_UP)).equals("-0.1"));
        TestFmwk.assertTrue("for114", (m150.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_UP)).equals("-0.2"));
        TestFmwk.assertTrue("for115", (d050.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_UP)).equals("0.1"));
        TestFmwk.assertTrue("for116", (d150.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_UP)).equals("0.2"));

        TestFmwk.assertTrue("for117", (m050.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_DOWN)).equals("0.0"));
        TestFmwk.assertTrue("for118", (m150.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_DOWN)).equals("-0.1"));
        TestFmwk.assertTrue("for119", (d050.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_DOWN)).equals("0.0"));
        TestFmwk.assertTrue("for120", (d150.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_DOWN)).equals("0.1"));

        d000 = new android.icu.math.BigDecimal("0.000");
        d500 = new android.icu.math.BigDecimal("0.500");
        TestFmwk.assertTrue("for121", (d000.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_UNNECESSARY)).equals("0.0"));
        TestFmwk.assertTrue("for122", (d000.format(-1,2,-1,-1,-1,android.icu.math.MathContext.ROUND_UNNECESSARY)).equals("0.00"));
        TestFmwk.assertTrue("for123", (d000.format(-1,3,-1,-1,-1,android.icu.math.MathContext.ROUND_UNNECESSARY)).equals("0.000"));
        try { // this should trap..
            d050.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_UNNECESSARY);
            flag = false;
        } catch (java.lang.ArithmeticException $95) {
            ae = $95;
            flag = (ae.getMessage()).equals("Rounding necessary");
        }
        TestFmwk.assertTrue("for124", flag);
        TestFmwk.assertTrue("for125", (d050.format(-1,2,-1,-1,-1,android.icu.math.MathContext.ROUND_UNNECESSARY)).equals("0.05"));
        TestFmwk.assertTrue("for126", (d050.format(-1,3,-1,-1,-1,android.icu.math.MathContext.ROUND_UNNECESSARY)).equals("0.050"));
        TestFmwk.assertTrue("for127", (d500.format(-1,1,-1,-1,-1,android.icu.math.MathContext.ROUND_UNNECESSARY)).equals("0.5"));
        TestFmwk.assertTrue("for128", (d500.format(-1,2,-1,-1,-1,android.icu.math.MathContext.ROUND_UNNECESSARY)).equals("0.50"));
        TestFmwk.assertTrue("for129", (d500.format(-1,3,-1,-1,-1,android.icu.math.MathContext.ROUND_UNNECESSARY)).equals("0.500"));

        // bad negs --
        try {
            d050.format(-2, -1, -1, -1, -1, -1);
            flag = false;
        } catch (java.lang.IllegalArgumentException $96) {
            flag = true;
        }
        TestFmwk.assertTrue("for131", flag);
        try {
            d050.format(-1, -2, -1, -1, -1, -1);
            flag = false;
        } catch (java.lang.IllegalArgumentException $97) {
            flag = true;
        }
        TestFmwk.assertTrue("for132", flag);
        try {
            d050.format(-1, -1, -2, -1, -1, -1);
            flag = false;
        } catch (java.lang.IllegalArgumentException $98) {
            flag = true;
        }
        TestFmwk.assertTrue("for133", flag);
        try {
            d050.format(-1, -1, -1, -2, -1, -1);
            flag = false;
        } catch (java.lang.IllegalArgumentException $99) {
            flag = true;
        }
        TestFmwk.assertTrue("for134", flag);
        try {
            d050.format(-1, -1, -1, -1, -2, -1);
            flag = false;
        } catch (java.lang.IllegalArgumentException $100) {
            flag = true;
        }
        TestFmwk.assertTrue("for135", flag);
        try {
            d050.format(-1, -1, -1, -1, -1, -2);
            flag = false;
        } catch (java.lang.IllegalArgumentException $101) {
            flag = true;
        }
        TestFmwk.assertTrue("for136", flag);
    }

    /* ----------------------------------------------------------------- */

    /** Test the {@link android.icu.math.BigDecimal#hashCode} method. */

    @Test
    public void diaghashcode() {
        java.lang.String hs;
        android.icu.math.BigDecimal d;
        hs = "27827817";
        d = new android.icu.math.BigDecimal(hs);
        TestFmwk.assertTrue("has001", (d.hashCode()) == (hs.hashCode()));
        hs = "1.265E+200";
        d = new android.icu.math.BigDecimal(hs);
        TestFmwk.assertTrue("has002", (d.hashCode()) == (hs.hashCode()));
        hs = "126.5E+200";
        d = new android.icu.math.BigDecimal(hs);
        TestFmwk.assertTrue("has003", (d.hashCode()) != (hs.hashCode()));
    }

    /* ----------------------------------------------------------------- */

    /** Test the {@link android.icu.math.BigDecimal#intValue} method. */

    @Test
    public void diagintvalue() {
        boolean flag = false;
        java.lang.String v = null;
        java.lang.ArithmeticException ae = null;
        java.lang.String badstrings[];
        int i = 0;
        java.lang.String norm = null;
        android.icu.math.BigDecimal dimax;
        android.icu.math.BigDecimal num = null;
        android.icu.math.BigDecimal dv = null;
        android.icu.math.BigDecimal dimin;

        // intValue --

        TestFmwk.assertTrue("inv001", imin==((new android.icu.math.BigDecimal(imin)).intValue()));
        TestFmwk.assertTrue("inv002", ((99))==((new android.icu.math.BigDecimal("99")).intValue()));
        TestFmwk.assertTrue("inv003", ((1))==((new android.icu.math.BigDecimal("1")).intValue()));
        TestFmwk.assertTrue("inv004", ((0))==((new android.icu.math.BigDecimal("0")).intValue()));
        TestFmwk.assertTrue("inv005", ((-1))==((new android.icu.math.BigDecimal("-1")).intValue()));
        TestFmwk.assertTrue("inv006", ((-99))==((new android.icu.math.BigDecimal("-99")).intValue()));
        TestFmwk.assertTrue("inv007", imax==((new android.icu.math.BigDecimal(imax)).intValue()));
        TestFmwk.assertTrue("inv008", ((5))==((new android.icu.math.BigDecimal("5.0")).intValue()));
        TestFmwk.assertTrue("inv009", ((5))==((new android.icu.math.BigDecimal("5.3")).intValue()));
        TestFmwk.assertTrue("inv010", ((5))==((new android.icu.math.BigDecimal("5.5")).intValue()));
        TestFmwk.assertTrue("inv011", ((5))==((new android.icu.math.BigDecimal("5.7")).intValue()));
        TestFmwk.assertTrue("inv012", ((5))==((new android.icu.math.BigDecimal("5.9")).intValue()));
        TestFmwk.assertTrue("inv013", ((-5))==((new android.icu.math.BigDecimal("-5.0")).intValue()));
        TestFmwk.assertTrue("inv014", ((-5))==((new android.icu.math.BigDecimal("-5.3")).intValue()));
        TestFmwk.assertTrue("inv015", ((-5))==((new android.icu.math.BigDecimal("-5.5")).intValue()));
        TestFmwk.assertTrue("inv016", ((-5))==((new android.icu.math.BigDecimal("-5.7")).intValue()));
        TestFmwk.assertTrue("inv017", ((-5))==((new android.icu.math.BigDecimal("-5.9")).intValue()));
        TestFmwk.assertTrue("inv018", ((new android.icu.math.BigDecimal("88888888888")).intValue())==(-1305424328)); // ugh
        TestFmwk.assertTrue("inv019", ((new android.icu.math.BigDecimal("-88888888888")).intValue())==1305424328); // ugh
        TestFmwk.assertTrue("inv020", ((imin))==((new android.icu.math.BigDecimal((((long)imax))+1)).intValue()));
        TestFmwk.assertTrue("inv021", ((imax))==((new android.icu.math.BigDecimal((((long)imin))-1)).intValue()));

        // intValueExact --

        TestFmwk.assertTrue("inv101", imin==((new android.icu.math.BigDecimal(imin)).intValueExact()));
        TestFmwk.assertTrue("inv102", ((99))==((new android.icu.math.BigDecimal("99")).intValue()));
        TestFmwk.assertTrue("inv103", ((1))==((new android.icu.math.BigDecimal("1")).intValue()));
        TestFmwk.assertTrue("inv104", ((0))==((new android.icu.math.BigDecimal("0")).intValue()));
        TestFmwk.assertTrue("inv105", ((-1))==((new android.icu.math.BigDecimal("-1")).intValue()));
        TestFmwk.assertTrue("inv106", ((-99))==((new android.icu.math.BigDecimal("-99")).intValue()));
        TestFmwk.assertTrue("inv107", imax==((new android.icu.math.BigDecimal(imax)).intValue()));
        TestFmwk.assertTrue("inv108", ((5))==((new android.icu.math.BigDecimal("5.0")).intValue()));
        TestFmwk.assertTrue("inv109", ((-5))==((new android.icu.math.BigDecimal("-5.0")).intValue()));
        TestFmwk.assertTrue("inv110", imax==((new android.icu.math.BigDecimal(imax)).intValueExact()));

        try {
            v = "-88588688888";
            (new android.icu.math.BigDecimal(v)).intValueExact();
            flag = false;
        } catch (java.lang.ArithmeticException $102) {
            ae = $102;
            flag = (ae.getMessage()).equals("Conversion overflow:" + " " + v);
        }
        TestFmwk.assertTrue("inv200", flag);

        // this one could raise either overflow or bad decimal part
        try {
            v = "88088818888.00001";
            (new android.icu.math.BigDecimal(v)).intValueExact();
            flag = false;
        } catch (java.lang.ArithmeticException $103) {
            flag = true;
        }
        TestFmwk.assertTrue("inv201", flag);

        // 1999.10.28: the testcases marked '*' failed
        badstrings = new java.lang.String[] {
                "12345678901",
                (new android.icu.math.BigDecimal(imax)).add(one).toString(),
                (new android.icu.math.BigDecimal(imin)).subtract(one)
                        .toString(),
                "3731367293",
                "4731367293",
                "5731367293",
                "6731367293",
                "7731367293",
                "8731367293",
                "9731367293",
                "-3731367293",
                "-4731367293",
                "-5731367293",
                "-6731367293",
                "-7731367293",
                "-8731367293",
                "-9731367293",
                (new android.icu.math.BigDecimal(imin)).multiply(two)
                        .toString(),
                (new android.icu.math.BigDecimal(imax)).multiply(two)
                        .toString(),
                (new android.icu.math.BigDecimal(imin)).multiply(ten)
                        .toString(),
                (new android.icu.math.BigDecimal(imax)).multiply(ten)
                        .toString(), "4731367293", "4831367293", "4931367293",
                "5031367293", "5131367293", "5231367293", "5331367293",
                "5431367293", "5531367293", "5631367293", "5731367293",
                "5831367293", "5931367293", "6031367293", "6131367293",
                "6231367293", "6331367293", "6431367293", "6531367293",
                "6631367293", "6731367293", "2200000000", "2300000000",
                "2400000000", "2500000000", "2600000000", "2700000000",
                "2800000000", "2900000000", "-2200000000", "-2300000000",
                "-2400000000", "-2500000000", "-2600000000", "-2700000000",
                "-2800000000", "-2900000000", "25E+8", "-25E+8", "-12345678901" }; // 220
        // 221
        // 222
        // 223
        // 224
        // 225 *
        // 226
        // 227
        // 228
        // 229 *
        // 230
        // 231
        // 232 *
        // 233
        // 234
        // 235
        // 236 *
        // 237
        // 238
        // 239
        // 240
        // 241
        // 242 *
        // 243 *
        // 244 *
        // 245 *
        // 246 *
        // 247 *
        // 248 *
        // 249 *
        // 250 *
        // 251 *
        // 252 *
        // 253 *
        // 254 *
        // 255 *
        // 256 *
        // 257 *
        // 258 *
        // 259
        // 260
        // 261
        // 262
        // 263
        // 264
        // 265
        // 266
        // 267
        // 268
        // 269
        // 270
        // 271
        // 272
        // 273
        // 274
        // 275
        // 276
        // 277
        // 278
        // 279
        // 280
        {
            int $104 = badstrings.length;
            i = 0;
            for (; $104 > 0; $104--, i++) {
                try {
                    v = badstrings[i];
                    (new android.icu.math.BigDecimal(v)).intValueExact();
                    flag = false;
                } catch (java.lang.ArithmeticException $105) {
                    ae = $105;
                    norm = (new android.icu.math.BigDecimal(v)).toString();
                    flag = (ae.getMessage()).equals("Conversion overflow:"
                            + " " + norm);
                }
                TestFmwk.assertTrue("inv" + (220 + i), flag);
            }
        }/* i */

        // now slip in some single bits...
        dimax = new android.icu.math.BigDecimal(imax);
        {
            i = 0;
            for (; i <= 49; i++) {
                try {
                    num = two.pow(new android.icu.math.BigDecimal(i), mc50);
                    dv = dimax.add(num, mc50);
                    dv.intValueExact();
                    flag = false;
                } catch (java.lang.ArithmeticException $106) {
                    ae = $106;
                    norm = dv.toString();
                    flag = (ae.getMessage()).equals("Conversion overflow:"
                            + " " + norm);
                }
                TestFmwk.assertTrue("inv" + (300 + i), flag);
            }
        }/* i */
        dimin = new android.icu.math.BigDecimal(imin);
        {
            i = 50;
            for (; i <= 99; i++) {
                try {
                    num = two.pow(new android.icu.math.BigDecimal(i), mc50);
                    dv = dimin.subtract(num, mc50);
                    dv.intValueExact();
                    flag = false;
                } catch (java.lang.ArithmeticException $107) {
                    ae = $107;
                    norm = dv.toString();
                    flag = (ae.getMessage()).equals("Conversion overflow:"
                            + " " + norm);
                }
                TestFmwk.assertTrue("inv" + (300 + i), flag);
            }
        }/* i */

        // the following should all raise bad-decimal-part exceptions
        badstrings = new java.lang.String[] { "0.09", "0.9", "0.01", "0.1",
                "-0.01", "-0.1", "1.01", "-1.01", "-1.1", "-111.111",
                "+111.111", "1.09", "1.05", "1.04", "1.99", "1.9", "1.5",
                "1.4", "-1.09", "-1.05", "-1.04", "-1.99", "-1.9", "-1.5",
                "-1.4", "1E-1000", "-1E-1000", "11E-1", "1.5" }; // 400-403
        // 404-407
        // 408-411
        // 412-416
        // 417-420
        // 421-424
        // 425-428

        {
            int $108 = badstrings.length;
            i = 0;
            for (; $108 > 0; $108--, i++) {
                try {
                    v = badstrings[i];
                    (new android.icu.math.BigDecimal(v)).intValueExact();
                    flag = false;
                } catch (java.lang.ArithmeticException $109) {
                    ae = $109;
                    norm = (new android.icu.math.BigDecimal(v)).toString();
                    flag = (ae.getMessage()).equals("Decimal part non-zero:"
                            + " " + norm);
                }
                TestFmwk.assertTrue("inv" + (400 + i), flag);
            }
        }/* i */
    }

    /* ----------------------------------------------------------------- */

    /** Test the {@link android.icu.math.BigDecimal#longValue} method. */

    @Test
    public void diaglongvalue() {
        boolean flag = false;
        java.lang.String v = null;
        java.lang.ArithmeticException ae = null;
        java.lang.String badstrings[];
        int i = 0;
        java.lang.String norm = null;
        android.icu.math.BigDecimal dlmax;
        android.icu.math.BigDecimal num = null;
        android.icu.math.BigDecimal dv = null;
        android.icu.math.BigDecimal dlmin;

        // longValue --

        TestFmwk.assertTrue("lov001", lmin==((new android.icu.math.BigDecimal(lmin)).longValue()));
        TestFmwk.assertTrue("lov002", ((99))==((new android.icu.math.BigDecimal("99")).longValue()));
        TestFmwk.assertTrue("lov003", ((1))==((new android.icu.math.BigDecimal("1")).longValue()));
        TestFmwk.assertTrue("lov004", ((0))==((new android.icu.math.BigDecimal("0")).longValue()));
        TestFmwk.assertTrue("lov005", ((-1))==((new android.icu.math.BigDecimal("-1")).longValue()));
        TestFmwk.assertTrue("lov006", ((-99))==((new android.icu.math.BigDecimal("-99")).longValue()));
        TestFmwk.assertTrue("lov007", lmax==((new android.icu.math.BigDecimal(lmax)).longValue()));
        TestFmwk.assertTrue("lov008", ((5))==((new android.icu.math.BigDecimal("5.0")).longValue()));
        TestFmwk.assertTrue("lov009", ((5))==((new android.icu.math.BigDecimal("5.3")).longValue()));
        TestFmwk.assertTrue("lov010", ((5))==((new android.icu.math.BigDecimal("5.5")).longValue()));
        TestFmwk.assertTrue("lov011", ((5))==((new android.icu.math.BigDecimal("5.7")).longValue()));
        TestFmwk.assertTrue("lov012", ((5))==((new android.icu.math.BigDecimal("5.9")).longValue()));
        TestFmwk.assertTrue("lov013", ((-5))==((new android.icu.math.BigDecimal("-5.0")).longValue()));
        TestFmwk.assertTrue("lov014", ((-5))==((new android.icu.math.BigDecimal("-5.3")).longValue()));
        TestFmwk.assertTrue("lov015", ((-5))==((new android.icu.math.BigDecimal("-5.5")).longValue()));
        TestFmwk.assertTrue("lov016", ((-5))==((new android.icu.math.BigDecimal("-5.7")).longValue()));
        TestFmwk.assertTrue("lov017", ((-5))==((new android.icu.math.BigDecimal("-5.9")).longValue()));
        TestFmwk.assertTrue("lov018", ((new android.icu.math.BigDecimal("888888888899999999998")).longValue())==3445173361941522430L); // ugh
        TestFmwk.assertTrue("lov019", ((new android.icu.math.BigDecimal("-888888888899999999998")).longValue())==(-3445173361941522430L)); // ugh

        // longValueExact --

        TestFmwk.assertTrue("lov101", lmin==((new android.icu.math.BigDecimal(lmin)).longValue()));
        TestFmwk.assertTrue("lov102", ((99))==((new android.icu.math.BigDecimal("99")).longValue()));
        TestFmwk.assertTrue("lov103", ((1))==((new android.icu.math.BigDecimal("1")).longValue()));
        TestFmwk.assertTrue("lov104", ((0))==((new android.icu.math.BigDecimal("0")).longValue()));
        TestFmwk.assertTrue("lov105", ((-1))==((new android.icu.math.BigDecimal("-1")).longValue()));
        TestFmwk.assertTrue("lov106", ((-99))==((new android.icu.math.BigDecimal("-99")).longValue()));
        TestFmwk.assertTrue("lov107", lmax==((new android.icu.math.BigDecimal(lmax)).longValue()));
        TestFmwk.assertTrue("lov108", ((5))==((new android.icu.math.BigDecimal("5.0")).longValue()));
        TestFmwk.assertTrue("lov109", ((-5))==((new android.icu.math.BigDecimal("-5.0")).longValue()));

        try {
            v = "-888888888899999999998";
            (new android.icu.math.BigDecimal(v)).longValueExact();
            flag = false;
        } catch (java.lang.ArithmeticException $110) {
            ae = $110;
            flag = (ae.getMessage()).equals("Conversion overflow:" + " " + v);
        }
        TestFmwk.assertTrue("lov200", flag);
        try {
            v = "88888887487487479488888";
            (new android.icu.math.BigDecimal(v)).longValueExact();
            flag = false;
        } catch (java.lang.ArithmeticException $111) {
            ae = $111;
            flag = (ae.getMessage()).equals("Conversion overflow:" + " " + v);
        }
        TestFmwk.assertTrue("lov201", flag);
        try {
            v = "1.5";
            (new android.icu.math.BigDecimal(v)).longValueExact();
            flag = false;
        } catch (java.lang.ArithmeticException $112) {
            ae = $112;
            flag = (ae.getMessage()).equals("Decimal part non-zero:" + " " + v);
        }
        TestFmwk.assertTrue("lov202", flag);

        badstrings = new java.lang.String[] {
                "1234567890110987654321",
                "-1234567890110987654321",
                (new android.icu.math.BigDecimal(lmax)).add(one).toString(),
                (new android.icu.math.BigDecimal(lmin)).subtract(one)
                        .toString(),
                (new android.icu.math.BigDecimal(lmin)).multiply(two)
                        .toString(),
                (new android.icu.math.BigDecimal(lmax)).multiply(two)
                        .toString(),
                (new android.icu.math.BigDecimal(lmin)).multiply(ten)
                        .toString(),
                (new android.icu.math.BigDecimal(lmax)).multiply(ten)
                        .toString(), "9223372036854775818",
                "9323372036854775818", "9423372036854775818",
                "9523372036854775818", "9623372036854775818",
                "9723372036854775818", "9823372036854775818",
                "9923372036854775818", "-9223372036854775818",
                "-9323372036854775818", "-9423372036854775818",
                "-9523372036854775818", "-9623372036854775818",
                "-9723372036854775818", "-9823372036854775818",
                "-9923372036854775818", "12345678901234567890" }; // 220
        // 221
        // 222
        // 223
        // 224
        // 225
        // 226
        // 227
        // 228
        // 229
        // 230
        // 231
        // 232
        // 233
        // 234
        // 235
        // 236
        // 237
        // 238
        // 239
        // 240
        // 241
        // 242
        // 243
        // 244
        {
            int $113 = badstrings.length;
            i = 0;
            for (; $113 > 0; $113--, i++) {
                try {
                    v = badstrings[i];
                    (new android.icu.math.BigDecimal(v)).longValueExact();
                    flag = false;
                } catch (java.lang.ArithmeticException $114) {
                    ae = $114;
                    norm = (new android.icu.math.BigDecimal(v)).toString();
                    flag = (ae.getMessage()).equals("Conversion overflow:"
                            + " " + norm);
                }
                TestFmwk.assertTrue("lov" + (220 + i), flag);
            }
        }/* i */

        // now slip in some single bits...
        dlmax = new android.icu.math.BigDecimal(lmax);
        {
            i = 0;
            for (; i <= 99; i++) {
                try {
                    num = two.pow(new android.icu.math.BigDecimal(i), mc50);
                    dv = dlmax.add(num, mc50);
                    dv.longValueExact();
                    flag = false;
                } catch (java.lang.ArithmeticException $115) {
                    ae = $115;
                    norm = dv.toString();
                    flag = (ae.getMessage()).equals("Conversion overflow:"
                            + " " + norm);
                }
                TestFmwk.assertTrue("lov" + (300 + i), flag);
            }
        }/* i */
        dlmin = new android.icu.math.BigDecimal(lmin);
        {
            i = 0;
            for (; i <= 99; i++) {
                try {
                    num = two.pow(new android.icu.math.BigDecimal(i), mc50);
                    dv = dlmin.subtract(num, mc50);
                    dv.longValueExact();
                    flag = false;
                } catch (java.lang.ArithmeticException $116) {
                    ae = $116;
                    norm = dv.toString();
                    flag = (ae.getMessage()).equals("Conversion overflow:"
                            + " " + norm);
                }
                TestFmwk.assertTrue("lov" + (400 + i), flag);
            }
        }/* i */
    }

    /* ----------------------------------------------------------------- */

    /** Test the {@link android.icu.math.BigDecimal#movePointLeft} method. */

    @Test
    public void diagmovepointleft() {
        TestFmwk.assertTrue("mpl001", ((new android.icu.math.BigDecimal("-1")).movePointLeft(-10).toString()).equals("-10000000000"));
        TestFmwk.assertTrue("mpl002", ((new android.icu.math.BigDecimal("-1")).movePointLeft(-5).toString()).equals("-100000"));
        TestFmwk.assertTrue("mpl003", ((new android.icu.math.BigDecimal("-1")).movePointLeft(-1).toString()).equals("-10"));
        TestFmwk.assertTrue("mpl004", ((new android.icu.math.BigDecimal("-1")).movePointLeft(0).toString()).equals("-1"));
        TestFmwk.assertTrue("mpl005", ((new android.icu.math.BigDecimal("-1")).movePointLeft(+1).toString()).equals("-0.1"));
        TestFmwk.assertTrue("mpl006", ((new android.icu.math.BigDecimal("-1")).movePointLeft(+5).toString()).equals("-0.00001"));
        TestFmwk.assertTrue("mpl007", ((new android.icu.math.BigDecimal("-1")).movePointLeft(+10).toString()).equals("-0.0000000001"));

        TestFmwk.assertTrue("mpl010", ((new android.icu.math.BigDecimal("0")).movePointLeft(-10).toString()).equals("0"));
        TestFmwk.assertTrue("mpl010", ((new android.icu.math.BigDecimal("0")).movePointLeft(-5).toString()).equals("0"));
        TestFmwk.assertTrue("mpl010", ((new android.icu.math.BigDecimal("0")).movePointLeft(-1).toString()).equals("0"));
        TestFmwk.assertTrue("mpl010", ((new android.icu.math.BigDecimal("0")).movePointLeft(0).toString()).equals("0"));
        TestFmwk.assertTrue("mpl010", ((new android.icu.math.BigDecimal("0")).movePointLeft(+1).toString()).equals("0.0"));
        TestFmwk.assertTrue("mpl010", ((new android.icu.math.BigDecimal("0")).movePointLeft(+5).toString()).equals("0.00000"));
        TestFmwk.assertTrue("mpl010", ((new android.icu.math.BigDecimal("0")).movePointLeft(+10).toString()).equals("0.0000000000"));

        TestFmwk.assertTrue("mpl020", ((new android.icu.math.BigDecimal("+1")).movePointLeft(-10).toString()).equals("10000000000"));
        TestFmwk.assertTrue("mpl021", ((new android.icu.math.BigDecimal("+1")).movePointLeft(-5).toString()).equals("100000"));
        TestFmwk.assertTrue("mpl022", ((new android.icu.math.BigDecimal("+1")).movePointLeft(-1).toString()).equals("10"));
        TestFmwk.assertTrue("mpl023", ((new android.icu.math.BigDecimal("+1")).movePointLeft(0).toString()).equals("1"));
        TestFmwk.assertTrue("mpl024", ((new android.icu.math.BigDecimal("+1")).movePointLeft(+1).toString()).equals("0.1"));
        TestFmwk.assertTrue("mpl025", ((new android.icu.math.BigDecimal("+1")).movePointLeft(+5).toString()).equals("0.00001"));
        TestFmwk.assertTrue("mpl026", ((new android.icu.math.BigDecimal("+1")).movePointLeft(+10).toString()).equals("0.0000000001"));

        TestFmwk.assertTrue("mpl030", ((new android.icu.math.BigDecimal("0.5E+1")).movePointLeft(-10).toString()).equals("50000000000"));
        TestFmwk.assertTrue("mpl031", ((new android.icu.math.BigDecimal("0.5E+1")).movePointLeft(-5).toString()).equals("500000"));
        TestFmwk.assertTrue("mpl032", ((new android.icu.math.BigDecimal("0.5E+1")).movePointLeft(-1).toString()).equals("50"));
        TestFmwk.assertTrue("mpl033", ((new android.icu.math.BigDecimal("0.5E+1")).movePointLeft(0).toString()).equals("5"));
        TestFmwk.assertTrue("mpl034", ((new android.icu.math.BigDecimal("0.5E+1")).movePointLeft(+1).toString()).equals("0.5"));
        TestFmwk.assertTrue("mpl035", ((new android.icu.math.BigDecimal("0.5E+1")).movePointLeft(+5).toString()).equals("0.00005"));
        TestFmwk.assertTrue("mpl036", ((new android.icu.math.BigDecimal("0.5E+1")).movePointLeft(+10).toString()).equals("0.0000000005"));
    }

    /* ----------------------------------------------------------------- */

    /** Test the {@link android.icu.math.BigDecimal#movePointRight} method. */

    @Test
    public void diagmovepointright() {
        TestFmwk.assertTrue("mpr001", ((new android.icu.math.BigDecimal("-1")).movePointRight(+10).toString()).equals("-10000000000"));
        TestFmwk.assertTrue("mpr002", ((new android.icu.math.BigDecimal("-1")).movePointRight(+5).toString()).equals("-100000"));
        TestFmwk.assertTrue("mpr003", ((new android.icu.math.BigDecimal("-1")).movePointRight(+1).toString()).equals("-10"));
        TestFmwk.assertTrue("mpr004", ((new android.icu.math.BigDecimal("-1")).movePointRight(0).toString()).equals("-1"));
        TestFmwk.assertTrue("mpr005", ((new android.icu.math.BigDecimal("-1")).movePointRight(-1).toString()).equals("-0.1"));
        TestFmwk.assertTrue("mpr006", ((new android.icu.math.BigDecimal("-1")).movePointRight(-5).toString()).equals("-0.00001"));
        TestFmwk.assertTrue("mpr007", ((new android.icu.math.BigDecimal("-1")).movePointRight(-10).toString()).equals("-0.0000000001"));

        TestFmwk.assertTrue("mpr010", ((new android.icu.math.BigDecimal("0")).movePointRight(+10).toString()).equals("0"));
        TestFmwk.assertTrue("mpr011", ((new android.icu.math.BigDecimal("0")).movePointRight(+5).toString()).equals("0"));
        TestFmwk.assertTrue("mpr012", ((new android.icu.math.BigDecimal("0")).movePointRight(+1).toString()).equals("0"));
        TestFmwk.assertTrue("mpr013", ((new android.icu.math.BigDecimal("0")).movePointRight(0).toString()).equals("0"));
        TestFmwk.assertTrue("mpr014", ((new android.icu.math.BigDecimal("0")).movePointRight(-1).toString()).equals("0.0"));
        TestFmwk.assertTrue("mpr015", ((new android.icu.math.BigDecimal("0")).movePointRight(-5).toString()).equals("0.00000"));
        TestFmwk.assertTrue("mpr016", ((new android.icu.math.BigDecimal("0")).movePointRight(-10).toString()).equals("0.0000000000"));

        TestFmwk.assertTrue("mpr020", ((new android.icu.math.BigDecimal("+1")).movePointRight(+10).toString()).equals("10000000000"));
        TestFmwk.assertTrue("mpr021", ((new android.icu.math.BigDecimal("+1")).movePointRight(+5).toString()).equals("100000"));
        TestFmwk.assertTrue("mpr022", ((new android.icu.math.BigDecimal("+1")).movePointRight(+1).toString()).equals("10"));
        TestFmwk.assertTrue("mpr023", ((new android.icu.math.BigDecimal("+1")).movePointRight(0).toString()).equals("1"));
        TestFmwk.assertTrue("mpr024", ((new android.icu.math.BigDecimal("+1")).movePointRight(-1).toString()).equals("0.1"));
        TestFmwk.assertTrue("mpr025", ((new android.icu.math.BigDecimal("+1")).movePointRight(-5).toString()).equals("0.00001"));
        TestFmwk.assertTrue("mpr026", ((new android.icu.math.BigDecimal("+1")).movePointRight(-10).toString()).equals("0.0000000001"));

        TestFmwk.assertTrue("mpr030", ((new android.icu.math.BigDecimal("0.5E+1")).movePointRight(+10).toString()).equals("50000000000"));
        TestFmwk.assertTrue("mpr031", ((new android.icu.math.BigDecimal("0.5E+1")).movePointRight(+5).toString()).equals("500000"));
        TestFmwk.assertTrue("mpr032", ((new android.icu.math.BigDecimal("0.5E+1")).movePointRight(+1).toString()).equals("50"));
        TestFmwk.assertTrue("mpr033", ((new android.icu.math.BigDecimal("0.5E+1")).movePointRight(0).toString()).equals("5"));
        TestFmwk.assertTrue("mpr034", ((new android.icu.math.BigDecimal("0.5E+1")).movePointRight(-1).toString()).equals("0.5"));
        TestFmwk.assertTrue("mpr035", ((new android.icu.math.BigDecimal("0.5E+1")).movePointRight(-5).toString()).equals("0.00005"));
        TestFmwk.assertTrue("mpr036", ((new android.icu.math.BigDecimal("0.5E+1")).movePointRight(-10).toString()).equals("0.0000000005"));
    }

    /* ----------------------------------------------------------------- */

    /** Test the {@link android.icu.math.BigDecimal#scale} method. */

    @Test
    public void diagscale() {
        TestFmwk.assertTrue("sca001", ((new android.icu.math.BigDecimal("-1")).scale())==0);
        TestFmwk.assertTrue("sca002", ((new android.icu.math.BigDecimal("-10")).scale())==0);
        TestFmwk.assertTrue("sca003", ((new android.icu.math.BigDecimal("+1")).scale())==0);
        TestFmwk.assertTrue("sca004", ((new android.icu.math.BigDecimal("+10")).scale())==0);
        TestFmwk.assertTrue("sca005", ((new android.icu.math.BigDecimal("1E+10")).scale())==0);
        TestFmwk.assertTrue("sca006", ((new android.icu.math.BigDecimal("1E-10")).scale())==10);
        TestFmwk.assertTrue("sca007", ((new android.icu.math.BigDecimal("0E-10")).scale())==0);
        TestFmwk.assertTrue("sca008", ((new android.icu.math.BigDecimal("0.000")).scale())==3);
        TestFmwk.assertTrue("sca009", ((new android.icu.math.BigDecimal("0.00")).scale())==2);
        TestFmwk.assertTrue("sca010", ((new android.icu.math.BigDecimal("0.0")).scale())==1);
        TestFmwk.assertTrue("sca011", ((new android.icu.math.BigDecimal("0.1")).scale())==1);
        TestFmwk.assertTrue("sca012", ((new android.icu.math.BigDecimal("0.12")).scale())==2);
        TestFmwk.assertTrue("sca013", ((new android.icu.math.BigDecimal("0.123")).scale())==3);
        TestFmwk.assertTrue("sca014", ((new android.icu.math.BigDecimal("-0.0")).scale())==1);
        TestFmwk.assertTrue("sca015", ((new android.icu.math.BigDecimal("-0.1")).scale())==1);
        TestFmwk.assertTrue("sca016", ((new android.icu.math.BigDecimal("-0.12")).scale())==2);
        TestFmwk.assertTrue("sca017", ((new android.icu.math.BigDecimal("-0.123")).scale())==3);
    }

    /* ----------------------------------------------------------------- */

    /** Test the {@link android.icu.math.BigDecimal#setScale} method. */

    @Test
    public void diagsetscale() {
        boolean flag = false;
        java.lang.RuntimeException e = null;

        TestFmwk.assertTrue("ssc001", ((new android.icu.math.BigDecimal("-1")).setScale(0).toString()).equals("-1"));
        TestFmwk.assertTrue("ssc002", ((new android.icu.math.BigDecimal("-1")).setScale(1).toString()).equals("-1.0"));
        TestFmwk.assertTrue("ssc003", ((new android.icu.math.BigDecimal("-1")).setScale(2).toString()).equals("-1.00"));
        TestFmwk.assertTrue("ssc004", ((new android.icu.math.BigDecimal("0")).setScale(0).toString()).equals("0"));
        TestFmwk.assertTrue("ssc005", ((new android.icu.math.BigDecimal("0")).setScale(1).toString()).equals("0.0"));
        TestFmwk.assertTrue("ssc006", ((new android.icu.math.BigDecimal("0")).setScale(2).toString()).equals("0.00"));
        TestFmwk.assertTrue("ssc007", ((new android.icu.math.BigDecimal("+1")).setScale(0).toString()).equals("1"));
        TestFmwk.assertTrue("ssc008", ((new android.icu.math.BigDecimal("+1")).setScale(1).toString()).equals("1.0"));
        TestFmwk.assertTrue("ssc009", ((new android.icu.math.BigDecimal("+1")).setScale(2).toString()).equals("1.00"));
        TestFmwk.assertTrue("ssc010", ((new android.icu.math.BigDecimal("-1")).setScale(0,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("-1"));
        TestFmwk.assertTrue("ssc011", ((new android.icu.math.BigDecimal("-1")).setScale(1,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("-1.0"));
        TestFmwk.assertTrue("ssc012", ((new android.icu.math.BigDecimal("-1")).setScale(2,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("-1.00"));
        TestFmwk.assertTrue("ssc013", ((new android.icu.math.BigDecimal("0")).setScale(0,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("0"));
        TestFmwk.assertTrue("ssc014", ((new android.icu.math.BigDecimal("0")).setScale(1,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("0.0"));
        TestFmwk.assertTrue("ssc015", ((new android.icu.math.BigDecimal("0")).setScale(2,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("0.00"));
        TestFmwk.assertTrue("ssc016", ((new android.icu.math.BigDecimal("+1")).setScale(0,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("1"));
        TestFmwk.assertTrue("ssc017", ((new android.icu.math.BigDecimal("+1")).setScale(1,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("1.0"));
        TestFmwk.assertTrue("ssc018", ((new android.icu.math.BigDecimal("+1")).setScale(2,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("1.00"));

        TestFmwk.assertTrue("ssc020", ((new android.icu.math.BigDecimal("1.04")).setScale(3,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("1.040"));
        TestFmwk.assertTrue("ssc021", ((new android.icu.math.BigDecimal("1.04")).setScale(2,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("1.04"));
        TestFmwk.assertTrue("ssc022", ((new android.icu.math.BigDecimal("1.04")).setScale(1,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("1.0"));
        TestFmwk.assertTrue("ssc023", ((new android.icu.math.BigDecimal("1.04")).setScale(0,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("1"));
        TestFmwk.assertTrue("ssc024", ((new android.icu.math.BigDecimal("1.05")).setScale(3,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("1.050"));
        TestFmwk.assertTrue("ssc025", ((new android.icu.math.BigDecimal("1.05")).setScale(2,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("1.05"));
        TestFmwk.assertTrue("ssc026", ((new android.icu.math.BigDecimal("1.05")).setScale(1,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("1.1"));
        TestFmwk.assertTrue("ssc027", ((new android.icu.math.BigDecimal("1.05")).setScale(0,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("1"));
        TestFmwk.assertTrue("ssc028", ((new android.icu.math.BigDecimal("1.05")).setScale(3,android.icu.math.MathContext.ROUND_HALF_DOWN).toString()).equals("1.050"));
        TestFmwk.assertTrue("ssc029", ((new android.icu.math.BigDecimal("1.05")).setScale(2,android.icu.math.MathContext.ROUND_HALF_DOWN).toString()).equals("1.05"));
        TestFmwk.assertTrue("ssc030", ((new android.icu.math.BigDecimal("1.05")).setScale(1,android.icu.math.MathContext.ROUND_HALF_DOWN).toString()).equals("1.0"));
        TestFmwk.assertTrue("ssc031", ((new android.icu.math.BigDecimal("1.05")).setScale(0,android.icu.math.MathContext.ROUND_HALF_DOWN).toString()).equals("1"));
        TestFmwk.assertTrue("ssc032", ((new android.icu.math.BigDecimal("1.06")).setScale(3,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("1.060"));
        TestFmwk.assertTrue("ssc033", ((new android.icu.math.BigDecimal("1.06")).setScale(2,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("1.06"));
        TestFmwk.assertTrue("ssc034", ((new android.icu.math.BigDecimal("1.06")).setScale(1,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("1.1"));
        TestFmwk.assertTrue("ssc035", ((new android.icu.math.BigDecimal("1.06")).setScale(0,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("1"));

        TestFmwk.assertTrue("ssc040", ((new android.icu.math.BigDecimal("-10")).setScale(2,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("-10.00"));
        TestFmwk.assertTrue("ssc041", ((new android.icu.math.BigDecimal("+1")).setScale(2,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("1.00"));
        TestFmwk.assertTrue("ssc042", ((new android.icu.math.BigDecimal("+10")).setScale(2,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("10.00"));
        TestFmwk.assertTrue("ssc043", ((new android.icu.math.BigDecimal("1E+10")).setScale(2,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("10000000000.00"));
        TestFmwk.assertTrue("ssc044", ((new android.icu.math.BigDecimal("1E-10")).setScale(2,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("0.00"));
        TestFmwk.assertTrue("ssc045", ((new android.icu.math.BigDecimal("1E-2")).setScale(2,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("0.01"));
        TestFmwk.assertTrue("ssc046", ((new android.icu.math.BigDecimal("0E-10")).setScale(2,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("0.00"));

        // check rounding
        TestFmwk.assertTrue("ssc050", ((new android.icu.math.BigDecimal("0.005")).setScale(2,android.icu.math.MathContext.ROUND_CEILING).toString()).equals("0.01"));
        TestFmwk.assertTrue("ssc051", ((new android.icu.math.BigDecimal("0.005")).setScale(1,android.icu.math.MathContext.ROUND_CEILING).toString()).equals("0.1"));
        TestFmwk.assertTrue("ssc052", ((new android.icu.math.BigDecimal("0.005")).setScale(0,android.icu.math.MathContext.ROUND_CEILING).toString()).equals("1"));
        TestFmwk.assertTrue("ssc053", ((new android.icu.math.BigDecimal("0.005")).setScale(2,android.icu.math.MathContext.ROUND_DOWN).toString()).equals("0.00"));
        TestFmwk.assertTrue("ssc054", ((new android.icu.math.BigDecimal("0.005")).setScale(1,android.icu.math.MathContext.ROUND_DOWN).toString()).equals("0.0"));
        TestFmwk.assertTrue("ssc055", ((new android.icu.math.BigDecimal("0.005")).setScale(0,android.icu.math.MathContext.ROUND_DOWN).toString()).equals("0"));
        TestFmwk.assertTrue("ssc056", ((new android.icu.math.BigDecimal("0.005")).setScale(2,android.icu.math.MathContext.ROUND_FLOOR).toString()).equals("0.00"));
        TestFmwk.assertTrue("ssc057", ((new android.icu.math.BigDecimal("0.005")).setScale(1,android.icu.math.MathContext.ROUND_FLOOR).toString()).equals("0.0"));
        TestFmwk.assertTrue("ssc058", ((new android.icu.math.BigDecimal("0.005")).setScale(0,android.icu.math.MathContext.ROUND_FLOOR).toString()).equals("0"));
        TestFmwk.assertTrue("ssc059", ((new android.icu.math.BigDecimal("0.005")).setScale(2,android.icu.math.MathContext.ROUND_HALF_DOWN).toString()).equals("0.00"));
        TestFmwk.assertTrue("ssc060", ((new android.icu.math.BigDecimal("0.005")).setScale(1,android.icu.math.MathContext.ROUND_HALF_DOWN).toString()).equals("0.0"));
        TestFmwk.assertTrue("ssc061", ((new android.icu.math.BigDecimal("0.005")).setScale(0,android.icu.math.MathContext.ROUND_HALF_DOWN).toString()).equals("0"));
        TestFmwk.assertTrue("ssc062", ((new android.icu.math.BigDecimal("0.005")).setScale(2,android.icu.math.MathContext.ROUND_HALF_EVEN).toString()).equals("0.00"));
        TestFmwk.assertTrue("ssc063", ((new android.icu.math.BigDecimal("0.005")).setScale(1,android.icu.math.MathContext.ROUND_HALF_EVEN).toString()).equals("0.0"));
        TestFmwk.assertTrue("ssc064", ((new android.icu.math.BigDecimal("0.005")).setScale(0,android.icu.math.MathContext.ROUND_HALF_EVEN).toString()).equals("0"));
        TestFmwk.assertTrue("ssc065", ((new android.icu.math.BigDecimal("0.015")).setScale(2,android.icu.math.MathContext.ROUND_HALF_EVEN).toString()).equals("0.02"));
        TestFmwk.assertTrue("ssc066", ((new android.icu.math.BigDecimal("0.015")).setScale(1,android.icu.math.MathContext.ROUND_HALF_EVEN).toString()).equals("0.0"));
        TestFmwk.assertTrue("ssc067", ((new android.icu.math.BigDecimal("0.015")).setScale(0,android.icu.math.MathContext.ROUND_HALF_EVEN).toString()).equals("0"));
        TestFmwk.assertTrue("ssc068", ((new android.icu.math.BigDecimal("0.005")).setScale(2,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("0.01"));
        TestFmwk.assertTrue("ssc069", ((new android.icu.math.BigDecimal("0.005")).setScale(1,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("0.0"));
        TestFmwk.assertTrue("ssc070", ((new android.icu.math.BigDecimal("0.005")).setScale(0,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("0"));
        TestFmwk.assertTrue("ssc071", ((new android.icu.math.BigDecimal("0.095")).setScale(2,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("0.10"));
        TestFmwk.assertTrue("ssc072", ((new android.icu.math.BigDecimal("0.095")).setScale(1,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("0.1"));
        TestFmwk.assertTrue("ssc073", ((new android.icu.math.BigDecimal("0.095")).setScale(0,android.icu.math.MathContext.ROUND_HALF_UP).toString()).equals("0"));
        TestFmwk.assertTrue("ssc074", ((new android.icu.math.BigDecimal("0.005")).setScale(2,android.icu.math.MathContext.ROUND_UP).toString()).equals("0.01"));
        TestFmwk.assertTrue("ssc075", ((new android.icu.math.BigDecimal("0.005")).setScale(1,android.icu.math.MathContext.ROUND_UP).toString()).equals("0.1"));
        TestFmwk.assertTrue("ssc076", ((new android.icu.math.BigDecimal("0.005")).setScale(0,android.icu.math.MathContext.ROUND_UP).toString()).equals("1"));

        try {
            (new android.icu.math.BigDecimal(1)).setScale(-8);
            flag = false;
        } catch (java.lang.RuntimeException $117) {
            e = $117;
            flag = (e.getMessage()).equals("Negative scale: -8");
        }/* checkscale */
        TestFmwk.assertTrue("ssc100", flag);
        try {
            (new android.icu.math.BigDecimal(1.0001D)).setScale(3);
            flag = false;
        } catch (java.lang.RuntimeException $118) {
            e = $118;
            flag = (e.getMessage()).equals("Rounding necessary");
        }/* checkrunn */
        TestFmwk.assertTrue("ssc101", flag);
        try {
            (new android.icu.math.BigDecimal(1E-8D)).setScale(3);
            flag = false;
        } catch (java.lang.RuntimeException $119) {
            e = $119;
            flag = (e.getMessage()).equals("Rounding necessary");
        }/* checkrunn */
        TestFmwk.assertTrue("ssc102", flag);
    }

    /* ----------------------------------------------------------------- */

    /** Test the <code>BigDecimal.shortValue()</code> method. */

    @Test
    public void diagshortvalue() {
        boolean flag = false;
        java.lang.String v = null;
        java.lang.ArithmeticException ae = null;
        java.lang.String badstrings[];
        int i = 0;
        java.lang.String norm = null;

        TestFmwk.assertTrue("shv002", (((short)0))==((new android.icu.math.BigDecimal("0")).shortValue()));
        TestFmwk.assertTrue("shv003", (((short)1))==((new android.icu.math.BigDecimal("1")).shortValue()));
        TestFmwk.assertTrue("shv004", (((short)99))==((new android.icu.math.BigDecimal("99")).shortValue()));
        TestFmwk.assertTrue("shv006", ((smax))==((new android.icu.math.BigDecimal(smax)).shortValue()));
        TestFmwk.assertTrue("shv007", ((smin))==((new android.icu.math.BigDecimal(smin)).shortValue()));
        TestFmwk.assertTrue("shv008", ((sneg))==((new android.icu.math.BigDecimal(sneg)).shortValue()));
        TestFmwk.assertTrue("shv009", ((szer))==((new android.icu.math.BigDecimal(szer)).shortValue()));
        TestFmwk.assertTrue("shv010", ((spos))==((new android.icu.math.BigDecimal(spos)).shortValue()));
        TestFmwk.assertTrue("shv011", ((smin))==((new android.icu.math.BigDecimal(smax+1)).shortValue()));
        TestFmwk.assertTrue("shv012", ((smax))==((new android.icu.math.BigDecimal(smin-1)).shortValue()));

        TestFmwk.assertTrue("shv022", (((short)0))==((new android.icu.math.BigDecimal("0")).shortValueExact()));
        TestFmwk.assertTrue("shv023", (((short)1))==((new android.icu.math.BigDecimal("1")).shortValueExact()));
        TestFmwk.assertTrue("shv024", (((short)99))==((new android.icu.math.BigDecimal("99")).shortValueExact()));
        TestFmwk.assertTrue("shv026", ((smax))==((new android.icu.math.BigDecimal(smax)).shortValueExact()));
        TestFmwk.assertTrue("shv027", ((smin))==((new android.icu.math.BigDecimal(smin)).shortValueExact()));
        TestFmwk.assertTrue("shv028", ((sneg))==((new android.icu.math.BigDecimal(sneg)).shortValueExact()));
        TestFmwk.assertTrue("shv029", ((szer))==((new android.icu.math.BigDecimal(szer)).shortValueExact()));
        TestFmwk.assertTrue("shv030", ((spos))==((new android.icu.math.BigDecimal(spos)).shortValueExact()));
        try {
            v = "-88888888888";
            (new android.icu.math.BigDecimal(v)).shortValueExact();
            flag = false;
        } catch (java.lang.ArithmeticException $120) {
            ae = $120;
            flag = (ae.getMessage()).equals("Conversion overflow:" + " " + v);
        }
        TestFmwk.assertTrue("shv100", flag);
        try {
            v = "88888888888";
            (new android.icu.math.BigDecimal(v)).shortValueExact();
            flag = false;
        } catch (java.lang.ArithmeticException $121) {
            ae = $121;
            flag = (ae.getMessage()).equals("Conversion overflow:" + " " + v);
        }
        TestFmwk.assertTrue("shv101", flag);
        try {
            v = "1.5";
            (new android.icu.math.BigDecimal(v)).shortValueExact();
            flag = false;
        } catch (java.lang.ArithmeticException $122) {
            ae = $122;
            flag = (ae.getMessage()).equals("Decimal part non-zero:" + " " + v);
        }
        TestFmwk.assertTrue("shv102", flag);

        badstrings = new java.lang.String[] {
                "123456",
                (new android.icu.math.BigDecimal(smax)).add(one).toString(),
                (new android.icu.math.BigDecimal(smin)).subtract(one)
                        .toString(),
                "71111",
                "81111",
                "91111",
                "-71111",
                "-81111",
                "-91111",
                (new android.icu.math.BigDecimal(smin)).multiply(two)
                        .toString(),
                (new android.icu.math.BigDecimal(smax)).multiply(two)
                        .toString(),
                (new android.icu.math.BigDecimal(smin)).multiply(ten)
                        .toString(),
                (new android.icu.math.BigDecimal(smax)).multiply(ten)
                        .toString(), "-123456" }; // 220
        // 221
        // 222
        // 223
        // 224
        // 225
        // 226
        // 227
        // 228
        // 229
        // 230
        // 231
        // 232
        // 233
        {
            int $123 = badstrings.length;
            i = 0;
            for (; $123 > 0; $123--, i++) {
                try {
                    v = badstrings[i];
                    (new android.icu.math.BigDecimal(v)).shortValueExact();
                    flag = false;
                } catch (java.lang.ArithmeticException $124) {
                    ae = $124;
                    norm = (new android.icu.math.BigDecimal(v)).toString();
                    flag = (ae.getMessage()).equals("Conversion overflow:"
                            + " " + norm);
                }
                TestFmwk.assertTrue("shv" + (220 + i), flag);
            }
        }/* i */
    }

    /* ----------------------------------------------------------------- */

    /** Test the {@link android.icu.math.BigDecimal#signum} method. */

    @Test
    public void diagsignum() {
        // necessarily checks some obscure constructions, too
        TestFmwk.assertTrue("sig001", (-1)==((new android.icu.math.BigDecimal("-1")).signum()));
        TestFmwk.assertTrue("sig002", (-1)==((new android.icu.math.BigDecimal("-0.0010")).signum()));
        TestFmwk.assertTrue("sig003", (-1)==((new android.icu.math.BigDecimal("-0.001")).signum()));
        TestFmwk.assertTrue("sig004", 0==((new android.icu.math.BigDecimal("-0.00")).signum()));
        TestFmwk.assertTrue("sig005", 0==((new android.icu.math.BigDecimal("-0")).signum()));
        TestFmwk.assertTrue("sig006", 0==((new android.icu.math.BigDecimal("0")).signum()));
        TestFmwk.assertTrue("sig007", 0==((new android.icu.math.BigDecimal("00")).signum()));
        TestFmwk.assertTrue("sig008", 0==((new android.icu.math.BigDecimal("00.0")).signum()));
        TestFmwk.assertTrue("sig009", 1==((new android.icu.math.BigDecimal("00.01")).signum()));
        TestFmwk.assertTrue("sig010", 1==((new android.icu.math.BigDecimal("00.01")).signum()));
        TestFmwk.assertTrue("sig011", 1==((new android.icu.math.BigDecimal("00.010")).signum()));
        TestFmwk.assertTrue("sig012", 1==((new android.icu.math.BigDecimal("01.01")).signum()));
        TestFmwk.assertTrue("sig013", 1==((new android.icu.math.BigDecimal("+0.01")).signum()));
        TestFmwk.assertTrue("sig014", 1==((new android.icu.math.BigDecimal("+0.001")).signum()));
        TestFmwk.assertTrue("sig015", 1==((new android.icu.math.BigDecimal("1")).signum()));
        TestFmwk.assertTrue("sig016", 1==((new android.icu.math.BigDecimal("1e+12")).signum()));
        TestFmwk.assertTrue("sig017", 0==((new android.icu.math.BigDecimal("00e+12")).signum()));
    }

    /* ----------------------------------------------------------------- */

    /** Test the {@link android.icu.math.BigDecimal#toBigDecimal} method. */

    @Test
    public void diagtobigdecimal() {
        TestFmwk.assertTrue("tbd001", ((new android.icu.math.BigDecimal("0")).toBigDecimal().toString()).equals("0"));
        TestFmwk.assertTrue("tbd002", ((new android.icu.math.BigDecimal("-1")).toBigDecimal().toString()).equals("-1"));
        TestFmwk.assertTrue("tbd003", ((new android.icu.math.BigDecimal("+1")).toBigDecimal().toString()).equals("1"));
        TestFmwk.assertTrue("tbd004", ((new android.icu.math.BigDecimal("1")).toBigDecimal().toString()).equals("1"));
        TestFmwk.assertTrue("tbd005", ((new android.icu.math.BigDecimal("1E+2")).toBigDecimal().toString()).equals("100"));
        TestFmwk.assertTrue("tbd006", ((new android.icu.math.BigDecimal("1E-2")).toBigDecimal().toString()).equals("0.01"));
        if (!isJDK15OrLater) {
            TestFmwk.assertTrue("tbd007", ((new android.icu.math.BigDecimal("1E-8")).toBigDecimal().toString()).equals("0.00000001"));
        }
        if (!isJDK15OrLater) {
            TestFmwk.assertTrue("tbd008", ((new android.icu.math.BigDecimal("1E-9")).toBigDecimal().toString()).equals("0.000000001"));
        }
        TestFmwk.assertTrue("tbd009", ((new android.icu.math.BigDecimal("1E10")).toBigDecimal().toString()).equals("10000000000"));
        TestFmwk.assertTrue("tbd010", ((new android.icu.math.BigDecimal("1E12")).toBigDecimal().toString()).equals("1000000000000"));
    }

    /* ----------------------------------------------------------------- */

    /** Test the {@link android.icu.math.BigDecimal#toBigInteger} method. */

    @Test
    public void diagtobiginteger() {
        boolean flag = false;
        java.lang.String badstrings[];
        int i = 0;
        TestFmwk.assertTrue("tbi001", ((new android.icu.math.BigDecimal("-1")).toBigInteger().toString()).equals("-1"));
        TestFmwk.assertTrue("tbi002", ((new android.icu.math.BigDecimal("0")).toBigInteger().toString()).equals("0"));
        TestFmwk.assertTrue("tbi003", ((new android.icu.math.BigDecimal("+1")).toBigInteger().toString()).equals("1"));
        TestFmwk.assertTrue("tbi004", ((new android.icu.math.BigDecimal("10")).toBigInteger().toString()).equals("10"));
        TestFmwk.assertTrue("tbi005", ((new android.icu.math.BigDecimal("1000")).toBigInteger().toString()).equals("1000"));
        TestFmwk.assertTrue("tbi006", ((new android.icu.math.BigDecimal("-1E+0")).toBigInteger().toString()).equals("-1"));
        TestFmwk.assertTrue("tbi007", ((new android.icu.math.BigDecimal("0E+0")).toBigInteger().toString()).equals("0"));
        TestFmwk.assertTrue("tbi008", ((new android.icu.math.BigDecimal("+1E+0")).toBigInteger().toString()).equals("1"));
        TestFmwk.assertTrue("tbi009", ((new android.icu.math.BigDecimal("10E+0")).toBigInteger().toString()).equals("10"));
        TestFmwk.assertTrue("tbi010", ((new android.icu.math.BigDecimal("1E+3")).toBigInteger().toString()).equals("1000"));
        TestFmwk.assertTrue("tbi011", ((new android.icu.math.BigDecimal("0.00")).toBigInteger().toString()).equals("0"));
        TestFmwk.assertTrue("tbi012", ((new android.icu.math.BigDecimal("0.01")).toBigInteger().toString()).equals("0"));
        TestFmwk.assertTrue("tbi013", ((new android.icu.math.BigDecimal("0.0")).toBigInteger().toString()).equals("0"));
        TestFmwk.assertTrue("tbi014", ((new android.icu.math.BigDecimal("0.1")).toBigInteger().toString()).equals("0"));
        TestFmwk.assertTrue("tbi015", ((new android.icu.math.BigDecimal("-0.00")).toBigInteger().toString()).equals("0"));
        TestFmwk.assertTrue("tbi016", ((new android.icu.math.BigDecimal("-0.01")).toBigInteger().toString()).equals("0"));
        TestFmwk.assertTrue("tbi017", ((new android.icu.math.BigDecimal("-0.0")).toBigInteger().toString()).equals("0"));
        TestFmwk.assertTrue("tbi018", ((new android.icu.math.BigDecimal("-0.1")).toBigInteger().toString()).equals("0"));
        TestFmwk.assertTrue("tbi019", ((new android.icu.math.BigDecimal("1.00")).toBigInteger().toString()).equals("1"));
        TestFmwk.assertTrue("tbi020", ((new android.icu.math.BigDecimal("1.01")).toBigInteger().toString()).equals("1"));
        TestFmwk.assertTrue("tbi021", ((new android.icu.math.BigDecimal("1.0")).toBigInteger().toString()).equals("1"));
        TestFmwk.assertTrue("tbi022", ((new android.icu.math.BigDecimal("1.1")).toBigInteger().toString()).equals("1"));
        TestFmwk.assertTrue("tbi023", ((new android.icu.math.BigDecimal("-1.00")).toBigInteger().toString()).equals("-1"));
        TestFmwk.assertTrue("tbi024", ((new android.icu.math.BigDecimal("-1.01")).toBigInteger().toString()).equals("-1"));
        TestFmwk.assertTrue("tbi025", ((new android.icu.math.BigDecimal("-1.0")).toBigInteger().toString()).equals("-1"));
        TestFmwk.assertTrue("tbi026", ((new android.icu.math.BigDecimal("-1.1")).toBigInteger().toString()).equals("-1"));
        TestFmwk.assertTrue("tbi027", ((new android.icu.math.BigDecimal("-111.111")).toBigInteger().toString()).equals("-111"));
        TestFmwk.assertTrue("tbi028", ((new android.icu.math.BigDecimal("+111.111")).toBigInteger().toString()).equals("111"));
        TestFmwk.assertTrue("tbi029", ((new android.icu.math.BigDecimal("0.09")).toBigInteger().toString()).equals("0"));
        TestFmwk.assertTrue("tbi030", ((new android.icu.math.BigDecimal("0.9")).toBigInteger().toString()).equals("0"));
        TestFmwk.assertTrue("tbi031", ((new android.icu.math.BigDecimal("1.09")).toBigInteger().toString()).equals("1"));
        TestFmwk.assertTrue("tbi032", ((new android.icu.math.BigDecimal("1.05")).toBigInteger().toString()).equals("1"));
        TestFmwk.assertTrue("tbi033", ((new android.icu.math.BigDecimal("1.04")).toBigInteger().toString()).equals("1"));
        TestFmwk.assertTrue("tbi034", ((new android.icu.math.BigDecimal("1.99")).toBigInteger().toString()).equals("1"));
        TestFmwk.assertTrue("tbi034", ((new android.icu.math.BigDecimal("1.9")).toBigInteger().toString()).equals("1"));
        TestFmwk.assertTrue("tbi035", ((new android.icu.math.BigDecimal("1.5")).toBigInteger().toString()).equals("1"));
        TestFmwk.assertTrue("tbi036", ((new android.icu.math.BigDecimal("1.4")).toBigInteger().toString()).equals("1"));
        TestFmwk.assertTrue("tbi037", ((new android.icu.math.BigDecimal("-1.09")).toBigInteger().toString()).equals("-1"));
        TestFmwk.assertTrue("tbi038", ((new android.icu.math.BigDecimal("-1.05")).toBigInteger().toString()).equals("-1"));
        TestFmwk.assertTrue("tbi039", ((new android.icu.math.BigDecimal("-1.04")).toBigInteger().toString()).equals("-1"));
        TestFmwk.assertTrue("tbi040", ((new android.icu.math.BigDecimal("-1.99")).toBigInteger().toString()).equals("-1"));
        TestFmwk.assertTrue("tbi041", ((new android.icu.math.BigDecimal("-1.9")).toBigInteger().toString()).equals("-1"));
        TestFmwk.assertTrue("tbi042", ((new android.icu.math.BigDecimal("-1.5")).toBigInteger().toString()).equals("-1"));
        TestFmwk.assertTrue("tbi043", ((new android.icu.math.BigDecimal("-1.4")).toBigInteger().toString()).equals("-1"));
        TestFmwk.assertTrue("tbi044", ((new android.icu.math.BigDecimal("1E-1000")).toBigInteger().toString()).equals("0"));
        TestFmwk.assertTrue("tbi045", ((new android.icu.math.BigDecimal("-1E-1000")).toBigInteger().toString()).equals("0"));

        // Exact variety --
        TestFmwk.assertTrue("tbi101", ((new android.icu.math.BigDecimal("-1")).toBigIntegerExact().toString()).equals("-1"));
        TestFmwk.assertTrue("tbi102", ((new android.icu.math.BigDecimal("0")).toBigIntegerExact().toString()).equals("0"));
        TestFmwk.assertTrue("tbi103", ((new android.icu.math.BigDecimal("+1")).toBigIntegerExact().toString()).equals("1"));
        TestFmwk.assertTrue("tbi104", ((new android.icu.math.BigDecimal("10")).toBigIntegerExact().toString()).equals("10"));
        TestFmwk.assertTrue("tbi105", ((new android.icu.math.BigDecimal("1000")).toBigIntegerExact().toString()).equals("1000"));
        TestFmwk.assertTrue("tbi106", ((new android.icu.math.BigDecimal("-1E+0")).toBigIntegerExact().toString()).equals("-1"));
        TestFmwk.assertTrue("tbi107", ((new android.icu.math.BigDecimal("0E+0")).toBigIntegerExact().toString()).equals("0"));
        TestFmwk.assertTrue("tbi108", ((new android.icu.math.BigDecimal("+1E+0")).toBigIntegerExact().toString()).equals("1"));
        TestFmwk.assertTrue("tbi109", ((new android.icu.math.BigDecimal("10E+0")).toBigIntegerExact().toString()).equals("10"));
        TestFmwk.assertTrue("tbi110", ((new android.icu.math.BigDecimal("1E+3")).toBigIntegerExact().toString()).equals("1000"));
        TestFmwk.assertTrue("tbi111", ((new android.icu.math.BigDecimal("0.00")).toBigIntegerExact().toString()).equals("0"));
        TestFmwk.assertTrue("tbi112", ((new android.icu.math.BigDecimal("0.0")).toBigIntegerExact().toString()).equals("0"));
        TestFmwk.assertTrue("tbi113", ((new android.icu.math.BigDecimal("-0.00")).toBigIntegerExact().toString()).equals("0"));
        TestFmwk.assertTrue("tbi114", ((new android.icu.math.BigDecimal("-0.0")).toBigIntegerExact().toString()).equals("0"));
        TestFmwk.assertTrue("tbi115", ((new android.icu.math.BigDecimal("1.00")).toBigIntegerExact().toString()).equals("1"));
        TestFmwk.assertTrue("tbi116", ((new android.icu.math.BigDecimal("1.0")).toBigIntegerExact().toString()).equals("1"));
        TestFmwk.assertTrue("tbi117", ((new android.icu.math.BigDecimal("-1.00")).toBigIntegerExact().toString()).equals("-1"));
        TestFmwk.assertTrue("tbi118", ((new android.icu.math.BigDecimal("-1.0")).toBigIntegerExact().toString()).equals("-1"));
        TestFmwk.assertTrue("tbi119", ((new android.icu.math.BigDecimal("1.00000000000000000000000000000")).toBigIntegerExact().toString()).equals("1"));


        // the following should all raise exceptions

        badstrings = new java.lang.String[] { "0.09", "0.9", "0.01", "0.1",
                "-0.01", "-0.1", "1.01", "-1.01", "-1.1", "-111.111",
                "+111.111", "1.09", "1.05", "1.04", "1.99", "1.9", "1.5",
                "1.4", "-1.09", "-1.05", "-1.04", "-1.99", "-1.9", "-1.5",
                "-1.4", "1E-1000", "-1E-1000", "11E-1", "1.1",
                "127623156123656561356123512315631231551312356.000001",
                "0.000000000000000000000000000000000000000000000001" }; // 300-303
        // 304-307
        // 308-311
        // 312-316
        // 317-320
        // 321-324
        // 325-328
        // 329
        // 330

        {
            int $125 = badstrings.length;
            i = 0;
            for (; $125 > 0; $125--, i++) {
                try {
                    (new android.icu.math.BigDecimal(badstrings[i]))
                            .toBigIntegerExact();
                    flag = false;
                } catch (java.lang.ArithmeticException $126) {
                    flag = true;
                }
                TestFmwk.assertTrue("tbi" + (300 + i), flag);
            }
        }/* i */
    }

    /* ----------------------------------------------------------------- */

    /** Test the {@link android.icu.math.BigDecimal#toCharArray} method. */

    @Test
    public void diagtochararray() {
        java.lang.String str;
        char car[];
        android.icu.math.BigDecimal d;
        char ca[];
        // the function of this has been tested above, this is simply an
        // existence proof and type-check
        str = "-123.45";
        car = (str).toCharArray();
        d = new android.icu.math.BigDecimal(str);
        ca = d.toCharArray();
        TestFmwk.assertTrue("tca001", ca.length == car.length);
        TestFmwk.assertTrue("tca002", (new java.lang.String(ca))
                .equals((new java.lang.String(car))));
        TestFmwk.assertTrue("tca003", (d.toCharArray() instanceof char[]));
        TestFmwk.assertTrue("tca004", (ca instanceof char[]));
    }

    /* ----------------------------------------------------------------- */

    /** Test the {@link android.icu.math.BigDecimal#toString} method. */

    @Test
    public void diagtostring() {
        java.lang.String str;
        char car[];
        android.icu.math.BigDecimal d;
        char ca[];
        java.lang.String cs;
        // the function of this has been tested above, this is simply an
        // existence proof and type-check
        str = "123.45";
        car = (str).toCharArray();
        d = new android.icu.math.BigDecimal(car, 0, car.length);
        ca = d.toCharArray();
        cs = d.toString();
        TestFmwk.assertTrue("tos001", (str.toCharArray().length) == ca.length);
        TestFmwk.assertTrue("tos002", (str.length()) == (cs.length()));
        TestFmwk.assertTrue("tos003", str.equals((new java.lang.String(ca))));
        TestFmwk.assertTrue("tos004", str.equals(cs));
        TestFmwk.assertTrue("tos005", (cs instanceof java.lang.String));
        TestFmwk.assertTrue("tos006", (d.toString() instanceof java.lang.String));
    }

    /* ----------------------------------------------------------------- */

    /** Test the {@link android.icu.math.BigDecimal#unscaledValue} method. */

    @Test
    public void diagunscaledvalue() {
        // just like toBigInteger, but scaly bits are preserved [without dots]
        TestFmwk.assertTrue("uns001", ((new android.icu.math.BigDecimal("-1")).unscaledValue().toString()).equals("-1"));
        TestFmwk.assertTrue("uns002", ((new android.icu.math.BigDecimal("0")).unscaledValue().toString()).equals("0"));
        TestFmwk.assertTrue("uns003", ((new android.icu.math.BigDecimal("+1")).unscaledValue().toString()).equals("1"));
        TestFmwk.assertTrue("uns004", ((new android.icu.math.BigDecimal("10")).unscaledValue().toString()).equals("10"));
        TestFmwk.assertTrue("uns005", ((new android.icu.math.BigDecimal("1000")).unscaledValue().toString()).equals("1000"));
        TestFmwk.assertTrue("uns006", ((new android.icu.math.BigDecimal("-1E+0")).unscaledValue().toString()).equals("-1"));
        TestFmwk.assertTrue("uns007", ((new android.icu.math.BigDecimal("0E+0")).unscaledValue().toString()).equals("0"));
        TestFmwk.assertTrue("uns008", ((new android.icu.math.BigDecimal("+1E+0")).unscaledValue().toString()).equals("1"));
        TestFmwk.assertTrue("uns009", ((new android.icu.math.BigDecimal("10E+0")).unscaledValue().toString()).equals("10"));
        TestFmwk.assertTrue("uns010", ((new android.icu.math.BigDecimal("1E+3")).unscaledValue().toString()).equals("1000"));
        TestFmwk.assertTrue("uns011", ((new android.icu.math.BigDecimal("0.00")).unscaledValue().toString()).equals("0"));
        TestFmwk.assertTrue("uns012", ((new android.icu.math.BigDecimal("0.01")).unscaledValue().toString()).equals("1"));
        TestFmwk.assertTrue("uns013", ((new android.icu.math.BigDecimal("0.0")).unscaledValue().toString()).equals("0"));
        TestFmwk.assertTrue("uns014", ((new android.icu.math.BigDecimal("0.1")).unscaledValue().toString()).equals("1"));
        TestFmwk.assertTrue("uns015", ((new android.icu.math.BigDecimal("-0.00")).unscaledValue().toString()).equals("0"));
        TestFmwk.assertTrue("uns016", ((new android.icu.math.BigDecimal("-0.01")).unscaledValue().toString()).equals("-1"));
        TestFmwk.assertTrue("uns017", ((new android.icu.math.BigDecimal("-0.0")).unscaledValue().toString()).equals("0"));
        TestFmwk.assertTrue("uns018", ((new android.icu.math.BigDecimal("-0.1")).unscaledValue().toString()).equals("-1"));
        TestFmwk.assertTrue("uns019", ((new android.icu.math.BigDecimal("1.00")).unscaledValue().toString()).equals("100"));
        TestFmwk.assertTrue("uns020", ((new android.icu.math.BigDecimal("1.01")).unscaledValue().toString()).equals("101"));
        TestFmwk.assertTrue("uns021", ((new android.icu.math.BigDecimal("1.0")).unscaledValue().toString()).equals("10"));
        TestFmwk.assertTrue("uns022", ((new android.icu.math.BigDecimal("1.1")).unscaledValue().toString()).equals("11"));
        TestFmwk.assertTrue("uns023", ((new android.icu.math.BigDecimal("-1.00")).unscaledValue().toString()).equals("-100"));
        TestFmwk.assertTrue("uns024", ((new android.icu.math.BigDecimal("-1.01")).unscaledValue().toString()).equals("-101"));
        TestFmwk.assertTrue("uns025", ((new android.icu.math.BigDecimal("-1.0")).unscaledValue().toString()).equals("-10"));
        TestFmwk.assertTrue("uns026", ((new android.icu.math.BigDecimal("-1.1")).unscaledValue().toString()).equals("-11"));
        TestFmwk.assertTrue("uns027", ((new android.icu.math.BigDecimal("-111.111")).unscaledValue().toString()).equals("-111111"));
        TestFmwk.assertTrue("uns028", ((new android.icu.math.BigDecimal("+111.111")).unscaledValue().toString()).equals("111111"));
    }

    /* ----------------------------------------------------------------- */

    /**
     * Test the {@link android.icu.math.BigDecimal#valueOf} method [long and
     * double].
     */

    @Test
    public void diagvalueof() {
        boolean flag = false;
        java.lang.NumberFormatException e = null;
        double dzer;
        double dpos;
        double dneg;
        double dpos5;
        double dneg5;
        double dmin;
        double dmax;
        double d;

        // valueOf(long [,scale]) --

        TestFmwk.assertTrue("val001", (android.icu.math.BigDecimal.valueOf(((byte)-2)).toString()).equals("-2"));
        TestFmwk.assertTrue("val002", (android.icu.math.BigDecimal.valueOf(((byte)-1)).toString()).equals("-1"));
        TestFmwk.assertTrue("val003", (android.icu.math.BigDecimal.valueOf(((byte)-0)).toString()).equals("0"));
        TestFmwk.assertTrue("val004", (android.icu.math.BigDecimal.valueOf(((byte)+1)).toString()).equals("1"));
        TestFmwk.assertTrue("val005", (android.icu.math.BigDecimal.valueOf(((byte)+2)).toString()).equals("2"));
        TestFmwk.assertTrue("val006", (android.icu.math.BigDecimal.valueOf(((byte)10)).toString()).equals("10"));
        TestFmwk.assertTrue("val007", (android.icu.math.BigDecimal.valueOf(((byte)11)).toString()).equals("11"));
        TestFmwk.assertTrue("val008", (android.icu.math.BigDecimal.valueOf(lmin).toString()).equals("-9223372036854775808"));
        TestFmwk.assertTrue("val009", (android.icu.math.BigDecimal.valueOf(lmax).toString()).equals("9223372036854775807"));
        TestFmwk.assertTrue("val010", (android.icu.math.BigDecimal.valueOf(lneg).toString()).equals("-1"));
        TestFmwk.assertTrue("val011", (android.icu.math.BigDecimal.valueOf(lzer).toString()).equals("0"));
        TestFmwk.assertTrue("val012", (android.icu.math.BigDecimal.valueOf(lpos).toString()).equals("1"));
        TestFmwk.assertTrue("val013", (android.icu.math.BigDecimal.valueOf(lmin,0).toString()).equals("-9223372036854775808"));
        TestFmwk.assertTrue("val014", (android.icu.math.BigDecimal.valueOf(lmax,0).toString()).equals("9223372036854775807"));
        TestFmwk.assertTrue("val015", (android.icu.math.BigDecimal.valueOf(lneg,0).toString()).equals("-1"));
        TestFmwk.assertTrue("val016", (android.icu.math.BigDecimal.valueOf(lpos,0).toString()).equals("1"));

        TestFmwk.assertTrue("val017", (android.icu.math.BigDecimal.valueOf(lzer,0).toString()).equals("0"));
        TestFmwk.assertTrue("val018", (android.icu.math.BigDecimal.valueOf(lzer,1).toString()).equals("0.0"));
        TestFmwk.assertTrue("val019", (android.icu.math.BigDecimal.valueOf(lzer,2).toString()).equals("0.00"));
        TestFmwk.assertTrue("val020", (android.icu.math.BigDecimal.valueOf(lzer,3).toString()).equals("0.000"));
        TestFmwk.assertTrue("val021", (android.icu.math.BigDecimal.valueOf(lzer,10).toString()).equals("0.0000000000"));

        TestFmwk.assertTrue("val022", (android.icu.math.BigDecimal.valueOf(lmin,7).toString()).equals("-922337203685.4775808"));
        TestFmwk.assertTrue("val023", (android.icu.math.BigDecimal.valueOf(lmax,11).toString()).equals("92233720.36854775807"));

        try {
            android.icu.math.BigDecimal.valueOf(23, -8);
            flag = false;
        } catch (java.lang.NumberFormatException $127) {
            e = $127;
            flag = (e.getMessage()).equals("Negative scale: -8");
        }/* checkscale */
        TestFmwk.assertTrue("val100", flag);

        // valueOf(double) --

        dzer = 0;
        dpos = 1;
        dpos = dpos / (10);
        dneg = -dpos;
        TestFmwk.assertTrue("val201", (android.icu.math.BigDecimal.valueOf(dneg).toString()).equals("-0.1"));
        TestFmwk.assertTrue("val202", (android.icu.math.BigDecimal.valueOf(dzer).toString()).equals("0.0")); // cf. constructor
        TestFmwk.assertTrue("val203", (android.icu.math.BigDecimal.valueOf(dpos).toString()).equals("0.1"));
        dpos5 = 0.5D;
        dneg5 = -dpos5;
        TestFmwk.assertTrue("val204", (android.icu.math.BigDecimal.valueOf(dneg5).toString()).equals("-0.5"));
        TestFmwk.assertTrue("val205", (android.icu.math.BigDecimal.valueOf(dpos5).toString()).equals("0.5"));
        dmin = java.lang.Double.MIN_VALUE;
        dmax = java.lang.Double.MAX_VALUE;
        TestFmwk.assertTrue("val206", (android.icu.math.BigDecimal.valueOf(dmin).toString()).equals("4.9E-324"));
        TestFmwk.assertTrue("val207", (android.icu.math.BigDecimal.valueOf(dmax).toString()).equals("1.7976931348623157E+308"));

        // nasties
        d = 9;
        d = d / (10);
        TestFmwk.assertTrue("val210", (android.icu.math.BigDecimal.valueOf(d).toString()).equals("0.9"));
        d = d / (10);
        TestFmwk.assertTrue("val211", (android.icu.math.BigDecimal.valueOf(d).toString()).equals("0.09"));
        d = d / (10);
        // The primitive double 0.009 is different in OpenJDK. In Oracle/IBM java <= 6, there is a trailing 0 (e.g 0.0090).
        String s = android.icu.math.BigDecimal.valueOf(d).toString();
        TestFmwk.assertTrue("val212", s.equals("0.0090") || s.equals("0.009"));
        d = d / (10);
        TestFmwk.assertTrue("val213", (android.icu.math.BigDecimal.valueOf(d).toString()).equals("9.0E-4"));
        d = d / (10);
        TestFmwk.assertTrue("val214", (android.icu.math.BigDecimal.valueOf(d).toString()).equals("8.999999999999999E-5"));
        d = d / (10);
        TestFmwk.assertTrue("val215", (android.icu.math.BigDecimal.valueOf(d).toString()).equals("8.999999999999999E-6"));
        d = d / (10);
        TestFmwk.assertTrue("val216", (android.icu.math.BigDecimal.valueOf(d).toString()).equals("8.999999999999999E-7"));
        d = d / (10);
        TestFmwk.assertTrue("val217", (android.icu.math.BigDecimal.valueOf(d).toString()).equals("8.999999999999999E-8"));
        d = d / (10);
        TestFmwk.assertTrue("val218", (android.icu.math.BigDecimal.valueOf(d).toString()).equals("8.999999999999998E-9"));

        try {
            android.icu.math.BigDecimal
                    .valueOf(java.lang.Double.POSITIVE_INFINITY);
            flag = false;
        } catch (java.lang.NumberFormatException $128) {
            flag = true;
        }/* checkpin */
        TestFmwk.assertTrue("val301", flag);
        try {
            android.icu.math.BigDecimal
                    .valueOf(java.lang.Double.NEGATIVE_INFINITY);
            flag = false;
        } catch (java.lang.NumberFormatException $129) {
            flag = true;
        }/* checknin */
        TestFmwk.assertTrue("val302", flag);
        try {
            android.icu.math.BigDecimal.valueOf(java.lang.Double.NaN);
            flag = false;
        } catch (java.lang.NumberFormatException $130) {
            flag = true;
        }/* checknan */
        TestFmwk.assertTrue("val303", flag);
    }

    /* ----------------------------------------------------------------- */

    /** Test the {@link android.icu.math.MathContext} class. */

    @Test
    public void diagmathcontext() {
        android.icu.math.MathContext mccon1;
        android.icu.math.MathContext mccon2;
        android.icu.math.MathContext mccon3;
        android.icu.math.MathContext mccon4;
        android.icu.math.MathContext mcrmc;
        android.icu.math.MathContext mcrmd;
        android.icu.math.MathContext mcrmf;
        android.icu.math.MathContext mcrmhd;
        android.icu.math.MathContext mcrmhe;
        android.icu.math.MathContext mcrmhu;
        android.icu.math.MathContext mcrmun;
        android.icu.math.MathContext mcrmu;
        boolean flag = false;
        java.lang.IllegalArgumentException e = null;
        // these tests are mostly existence checks
        TestFmwk.assertTrue("mcn001", (android.icu.math.MathContext.DEFAULT.getDigits())==9);
        TestFmwk.assertTrue("mcn002", (android.icu.math.MathContext.DEFAULT.getForm())==android.icu.math.MathContext.SCIENTIFIC);
        TestFmwk.assertTrue("mcn003", (android.icu.math.MathContext.DEFAULT.getForm())!=android.icu.math.MathContext.ENGINEERING);
        TestFmwk.assertTrue("mcn004", (android.icu.math.MathContext.DEFAULT.getForm())!=android.icu.math.MathContext.PLAIN);
        TestFmwk.assertTrue("mcn005", (android.icu.math.MathContext.DEFAULT.getLostDigits()?1:0)==0);
        TestFmwk.assertTrue("mcn006", (android.icu.math.MathContext.DEFAULT.getRoundingMode())==android.icu.math.MathContext.ROUND_HALF_UP);

        TestFmwk.assertTrue("mcn010", android.icu.math.MathContext.ROUND_CEILING>=0);
        TestFmwk.assertTrue("mcn011", android.icu.math.MathContext.ROUND_DOWN>=0);
        TestFmwk.assertTrue("mcn012", android.icu.math.MathContext.ROUND_FLOOR>=0);
        TestFmwk.assertTrue("mcn013", android.icu.math.MathContext.ROUND_HALF_DOWN>=0);
        TestFmwk.assertTrue("mcn014", android.icu.math.MathContext.ROUND_HALF_EVEN>=0);
        TestFmwk.assertTrue("mcn015", android.icu.math.MathContext.ROUND_HALF_UP>=0);
        TestFmwk.assertTrue("mcn016", android.icu.math.MathContext.ROUND_UNNECESSARY>=0);
        TestFmwk.assertTrue("mcn017", android.icu.math.MathContext.ROUND_UP>=0);

        mccon1=new android.icu.math.MathContext(111);
        TestFmwk.assertTrue("mcn021", (mccon1.getDigits())==111);
        TestFmwk.assertTrue("mcn022", (mccon1.getForm())==android.icu.math.MathContext.SCIENTIFIC);
        TestFmwk.assertTrue("mcn023", (mccon1.getLostDigits()?1:0)==0);
        TestFmwk.assertTrue("mcn024", (mccon1.getRoundingMode())==android.icu.math.MathContext.ROUND_HALF_UP);

        mccon2=new android.icu.math.MathContext(78,android.icu.math.MathContext.ENGINEERING);
        TestFmwk.assertTrue("mcn031", (mccon2.getDigits())==78);
        TestFmwk.assertTrue("mcn032", (mccon2.getForm())==android.icu.math.MathContext.ENGINEERING);
        TestFmwk.assertTrue("mcn033", (mccon2.getLostDigits()?1:0)==0);
        TestFmwk.assertTrue("mcn034", (mccon2.getRoundingMode())==android.icu.math.MathContext.ROUND_HALF_UP);

        mccon3=new android.icu.math.MathContext(5,android.icu.math.MathContext.PLAIN,true);
        TestFmwk.assertTrue("mcn041", (mccon3.getDigits())==5);
        TestFmwk.assertTrue("mcn042", (mccon3.getForm())==android.icu.math.MathContext.PLAIN);
        TestFmwk.assertTrue("mcn043", (mccon3.getLostDigits()?1:0)==1);
        TestFmwk.assertTrue("mcn044", (mccon3.getRoundingMode())==android.icu.math.MathContext.ROUND_HALF_UP);

        mccon4=new android.icu.math.MathContext(0,android.icu.math.MathContext.SCIENTIFIC,false,android.icu.math.MathContext.ROUND_FLOOR);
        TestFmwk.assertTrue("mcn051", (mccon4.getDigits()) == 0);
        TestFmwk.assertTrue("mcn052", (mccon4.getForm()) == android.icu.math.MathContext.SCIENTIFIC);
        TestFmwk.assertTrue("mcn053", (mccon4.getLostDigits() ? 1 : 0) == 0);
        TestFmwk.assertTrue("mcn054", (mccon4.getRoundingMode()) == android.icu.math.MathContext.ROUND_FLOOR);

        TestFmwk.assertTrue("mcn061", (mccon1.toString()).equals("digits=111 form=SCIENTIFIC lostDigits=0 roundingMode=ROUND_HALF_UP"));

        TestFmwk.assertTrue("mcn062", (mccon2.toString()).equals("digits=78 form=ENGINEERING lostDigits=0 roundingMode=ROUND_HALF_UP"));

        TestFmwk.assertTrue("mcn063", (mccon3.toString()).equals("digits=5 form=PLAIN lostDigits=1 roundingMode=ROUND_HALF_UP"));

        TestFmwk.assertTrue("mcn064", (mccon4.toString()).equals("digits=0 form=SCIENTIFIC lostDigits=0 roundingMode=ROUND_FLOOR"));

        // complete testing rounding modes round trips
        mcrmc=new android.icu.math.MathContext(0,android.icu.math.MathContext.PLAIN,false,android.icu.math.MathContext.ROUND_CEILING);
        mcrmd=new android.icu.math.MathContext(0,android.icu.math.MathContext.PLAIN,false,android.icu.math.MathContext.ROUND_DOWN);
        mcrmf=new android.icu.math.MathContext(0,android.icu.math.MathContext.PLAIN,false,android.icu.math.MathContext.ROUND_FLOOR);
        mcrmhd=new android.icu.math.MathContext(0,android.icu.math.MathContext.PLAIN,false,android.icu.math.MathContext.ROUND_HALF_DOWN);
        mcrmhe=new android.icu.math.MathContext(0,android.icu.math.MathContext.PLAIN,false,android.icu.math.MathContext.ROUND_HALF_EVEN);
        mcrmhu=new android.icu.math.MathContext(0,android.icu.math.MathContext.PLAIN,false,android.icu.math.MathContext.ROUND_HALF_UP);
        mcrmun=new android.icu.math.MathContext(0,android.icu.math.MathContext.PLAIN,false,android.icu.math.MathContext.ROUND_UNNECESSARY);
        mcrmu=new android.icu.math.MathContext(0,android.icu.math.MathContext.PLAIN,false,android.icu.math.MathContext.ROUND_UP);

        TestFmwk.assertTrue("mcn071", (mcrmc.toString()).equals("digits=0 form=PLAIN lostDigits=0 roundingMode=ROUND_CEILING"));

        TestFmwk.assertTrue("mcn072", (mcrmd.toString()).equals("digits=0 form=PLAIN lostDigits=0 roundingMode=ROUND_DOWN"));

        TestFmwk.assertTrue("mcn073", (mcrmf.toString()).equals("digits=0 form=PLAIN lostDigits=0 roundingMode=ROUND_FLOOR"));

        TestFmwk.assertTrue("mcn074", (mcrmhd.toString()).equals("digits=0 form=PLAIN lostDigits=0 roundingMode=ROUND_HALF_DOWN"));

        TestFmwk.assertTrue("mcn075", (mcrmhe.toString()).equals("digits=0 form=PLAIN lostDigits=0 roundingMode=ROUND_HALF_EVEN"));

        TestFmwk.assertTrue("mcn076", (mcrmhu.toString()).equals("digits=0 form=PLAIN lostDigits=0 roundingMode=ROUND_HALF_UP"));

        TestFmwk.assertTrue("mcn077", (mcrmun.toString()).equals("digits=0 form=PLAIN lostDigits=0 roundingMode=ROUND_UNNECESSARY"));

        TestFmwk.assertTrue("mcn078", (mcrmu.toString()).equals("digits=0 form=PLAIN lostDigits=0 roundingMode=ROUND_UP"));

        // [get methods tested already]

        // errors...

        try {
            new android.icu.math.MathContext(-1);
            flag = false;
        } catch (java.lang.IllegalArgumentException $131) {
            e = $131;
            flag = (e.getMessage()).equals("Digits too small: -1");
        }/* checkdig */
        TestFmwk.assertTrue("mcn101", flag);
        try {
            new android.icu.math.MathContext(1000000000);
            flag = false;
        } catch (java.lang.IllegalArgumentException $132) {
            e = $132;
            flag = (e.getMessage()).equals("Digits too large: 1000000000");
        }/* checkdigbig */
        TestFmwk.assertTrue("mcn102", flag);

        try {
            new android.icu.math.MathContext(0, 5);
            flag = false;
        } catch (java.lang.IllegalArgumentException $133) {
            e = $133;
            flag = (e.getMessage()).equals("Bad form value: 5");
        }/* checkform */
        TestFmwk.assertTrue("mcn111", flag);
        try {
            new android.icu.math.MathContext(0, -1);
            flag = false;
        } catch (java.lang.IllegalArgumentException $134) {
            e = $134;
            flag = (e.getMessage()).equals("Bad form value: -1");
        }/* checkformneg */
        TestFmwk.assertTrue("mcn112", flag);

        // [lostDigits cannot be invalid]

        try {
            new android.icu.math.MathContext(0,
                    android.icu.math.MathContext.PLAIN, false, 12);
            flag = false;
        } catch (java.lang.IllegalArgumentException $135) {
            e = $135;
            flag = (e.getMessage()).equals("Bad roundingMode value: 12");
        }/* checkround */
        TestFmwk.assertTrue("mcn121", flag);
        try {
            new android.icu.math.MathContext(0,
                    android.icu.math.MathContext.PLAIN, false, -1);
            flag = false;
        } catch (java.lang.IllegalArgumentException $136) {
            e = $136;
            flag = (e.getMessage()).equals("Bad roundingMode value: -1");
        }/* checkroundneg */
        TestFmwk.assertTrue("mcn122", flag);
    }

    /* ----------------------------------------------------------------- */

    /**
     * Test general arithmetic (base operators).
     * <p>
     * Unlike the specific method tests, these tests were randomly generated by
     * an IBM Object Rexx procedure, then manually corrected for known
     * differences from ANSI X3-274. These differences are:
     * <ol>
     * <li>the trigger point in exponential notation is fixed in ANSI X3-274
     * but varies with DIGITS in Classic and Object Rexx
     * <li>some trailing zeros were missing (e.g., 1.3 + 1E-60 should show
     * seven trailing zeros)
     * <li>the power operator is less accurate in Object Rexx
     * <li>ANSI X3-274 [errata 1999] rounds input numbers to DIGITS (rather
     * than truncating to DIGITS+1).
     * </ol>
     */

    @Test
    public void diagmath() {
        android.icu.math.MathContext def;
        def = android.icu.math.MathContext.DEFAULT;
        mathtest(1,def,"-9375284.42","5516.99832E+27276984","5.51699832E+27276987","-5.51699832E+27276987","-5.17234284E+27276994","-1.69934516E-27276981","0","-9375284.42","6.79057752E+41");
        mathtest(2,def,"-410.832710","99.3588243E-502740862","-410.832710","-410.832710","-4.08198550E-502740858","-4.13483868E+502740862","","","1.36977786E+26");
        mathtest(3,def,"80025.2340","-8.03097581","80017.2030","80033.2650","-642680.718","-9964.57167","-9964","4.59102916","5.94544517E-40");
        mathtest(4,def,"81052020.2","-464525495","-383473475","545577515","-3.76507298E+16","-0.17448347","0","81052020.2","");
        mathtest(5,def,"715.069294E-26923151","85.4404128E+796388557","8.54404128E+796388558","-8.54404128E+796388558","6.10958157E+769465410","8.36921628E-823311708","0","7.15069294E-26923149","4.88802213E-242308334");
        mathtest(6,def,"-21971575.0E+31454441","-70944960.3E+111238221","-7.09449603E+111238228","7.09449603E+111238228","1.55877252E+142692677","3.09698884E-79783781","0","-2.19715750E+31454448","-4.04549502E-220181139");
        mathtest(7,def,"682.25316","54470185.6","54470867.9","-54469503.4","3.71624563E+10","0.0000125252586","0","682.25316","3.48578699E+154365541");
        mathtest(8,def,"-257586757.","2082888.71","-255503868","-259669646","-5.36524548E+14","-123.668036","-123","-1391445.67","-1.26879515E+17519020");
        mathtest(9,def,"319577540.E+242599761","60.7124561","3.19577540E+242599769","3.19577540E+242599769","1.94023374E+242599771","5.26378869E+242599767","","","");
        mathtest(10,def,"-13769977.0","24371.3381","-13745605.7","-13794348.3","-3.35592765E+11","-565.007015","-565","-170.9735","-8.73734001E+173982");
        mathtest(11,def,"-475.434972E-725464311","-3.22214066E-865476836","-4.75434972E-725464309","-4.75434972E-725464309","","1.47552519E+140012527","","","");
        mathtest(12,def,"842.01250","197199893","197200735","-197199051","1.66044775E+11","0.00000426984258","0","842.01250","7.00674164E+576872502");
        mathtest(13,def,"572.173103E+280128428","-7140.19428","5.72173103E+280128430","5.72173103E+280128430","-4.08542712E+280128434","-8.01341085E+280128426","","","");
        mathtest(14,def,"674235.954E+476135291","9684.82245","6.74235954E+476135296","6.74235954E+476135296","6.52985550E+476135300","6.96177919E+476135292","","","");
        mathtest(15,def,"-360557.921E+437116514","930428850","-3.60557921E+437116519","-3.60557921E+437116519","-3.35473492E+437116528","-3.87517993E+437116510","","","");
        mathtest(16,def,"957165918E-394595705","1676.59073E-829618944","9.57165918E-394595697","9.57165918E-394595697","","5.70900161E+435023244","","","9.16166595E-789191393");
        mathtest(17,def,"-2610864.40","31245912.7","28635048.3","-33856777.1","-8.15788411E+13","-0.0835585897","0","-2610864.40","-3.12008905E+200498284");
        mathtest(18,def,"959.548461","98.994577E+776775426","9.89945770E+776775427","-9.89945770E+776775427","9.49900940E+776775430","9.69293965E-776775426","0","959.548461","6.61712185E+29");
        mathtest(19,def,"-41085.0268","3115477.61","3074392.58","-3156562.64","-1.27999481E+11","-0.0131873927","0","-41085.0268","4.73844173E+14373829");
        mathtest(20,def,"-723420285.","2681660.35","-720738625","-726101945","-1.93996749E+15","-269.765813","-269","-2053650.85","4.14324113E+23757873");
        mathtest(21,def,"63542018.0E-817732230","-8836243.22","-8836243.22","8836243.22","-5.61472726E-817732216","-7.19106711E-817732230","0","6.35420180E-817732223","");
        mathtest(22,def,"-96051.7108","-291201.955","-387253.666","195150.244","2.79704460E+10","0.329845694","0","-96051.7108","3.53617153E-1450916");
        mathtest(23,def,"108490.853","91685996.5","91794487.4","-91577505.7","9.94709197E+12","0.00118328706","0","108490.853","6.98124265E+461675038");
        mathtest(24,def,"-27489.1735","-9835835.4E-506411649","-27489.1735","-27489.1735","2.70378986E-506411638","2.79479804E+506411646","","","4.05866472E-45");
        mathtest(25,def,"-89220406.6","993391.008E-611041175","-89220406.6","-89220406.6","-8.86307496E-611041162","-8.98139865E+611041176","","","3.19625913E+79");
        mathtest(26,def,"4.75502020","-17089144.9","-17089140.2","17089149.7","-81259229.2","-2.78247989E-7","0","4.75502020","1.0630191E-11571955");
        mathtest(27,def,"68027916.2","-796883.839","67231032.4","68824800.0","-5.42103470E+13","-85.3674185","-85","292789.885","8.29415374E-6241744");
        mathtest(28,def,"-8.01969439E+788605478","92154156.0","-8.01969439E+788605478","-8.01969439E+788605478","-7.39048168E+788605486","-8.70247717E+788605470","","","");
        mathtest(29,def,"-8012.98341","96188.8651","88175.8817","-104201.849","-770759780","-0.0833046881","0","-8012.98341","-1.16010156E+375502");
        mathtest(30,def,"21761476E+592330677","-9.70744506","2.17614760E+592330684","2.17614760E+592330684","-2.11248333E+592330685","-2.24173053E+592330683","","","");
        mathtest(31,def,"-9840778.51","-17907.219","-9858685.73","-9822871.29","1.76220976E+11","549.542534","549","-9715.279","-6.62997437E-125225");
        mathtest(32,def,"-4.1097614","-819.225776E-145214751","-4.10976140","-4.10976140","3.36682247E-145214748","5.01664074E+145214748","","","0.0000122876018");
        mathtest(33,def,"-448.880985","-394.087374E-442511435","-448.880985","-448.880985","1.76898329E-442511430","1.13903925E+442511435","","","2.46306099E-11");
        mathtest(34,def,"779.445304E+882688544","-797868519","7.79445304E+882688546","7.79445304E+882688546","-6.21894870E+882688555","-9.7690946E+882688537","","","");
        mathtest(35,def,"799995477","-6.23675208E+156309440","-6.23675208E+156309440","6.23675208E+156309440","-4.98937346E+156309449","-1.28271169E-156309432","0","799995477","3.81482667E-54");
        mathtest(36,def,"-51932.8170","591840275E-278480289","-51932.8170","-51932.8170","-3.07359327E-278480276","-8.7748028E+278480284","","","1.96178443E+28");
        mathtest(37,def,"70.3552392","-4228656.73","-4228586.38","4228727.09","-297508156","-0.0000166377277","0","70.3552392","9.14742382E-7811584");
        mathtest(38,def,"1588359.34","-12232799.2","-10644439.9","13821158.5","-1.94300809E+13","-0.129844307","0","1588359.34","1.56910086E-75854960");
        mathtest(39,def,"2842.16206","-3.23234345","2838.92972","2845.39440","-9186.84392","-879.288388","-879","0.93216745","4.35565514E-11");
        mathtest(40,def,"29960.2305","45.2735747E-95205475","29960.2305","29960.2305","1.35640673E-95205469","6.61759773E+95205477","","","2.413936E+22");
        mathtest(41,def,"2916565.77","1151935.43E-787118724","2916565.77","2916565.77","3.35969544E-787118712","2.53188303E+787118724","","","2916565.77");
        mathtest(42,def,"-52723012.9E-967143787","79.4088237","79.4088237","-79.4088237","-4.18667244E-967143778","-6.63944011E-967143782","0","-5.27230129E-967143780","");
        mathtest(43,def,"-167473465","793646.597","-166679819","-168267112","-1.32914746E+14","-211.017682","-211","-14033.033","-1.19053789E+6526910");
        mathtest(44,def,"-31769071.0","133.4360","-31768937.6","-31769204.4","-4.23913776E+9","-238084.707","-238084","-94.3760","-5.84252432E+997");
        mathtest(45,def,"45960.6383","-93352.7468","-47392.1085","139313.385","-4.29055183E+9","-0.492333004","0","45960.6383","1.88335323E-435248");
        mathtest(46,def,"606.175648","5.28528458E-981983620","606.175648","606.175648","3.20381081E-981983617","1.14691203E+981983622","","","8.18450516E+13");
        mathtest(47,def,"171578.617E+643006110","-407774.293","1.71578617E+643006115","1.71578617E+643006115","-6.99653492E+643006120","-4.20768597E+643006109","","","");
        mathtest(48,def,"-682286332.","-464.871699","-682286797","-682285867","3.17175606E+11","1467687.39","1467687","-182.709787","-1.6050843E-4108");
        mathtest(49,def,"492088.428","653.72170","492742.150","491434.706","321688884","752.74911","752","489.70960","3.94658596E+3722");
        mathtest(50,def,"74303782.5","1141.68058","74304924.2","74302640.8","8.48311855E+10","65082.812","65082","926.99244","4.94849869E+8988");
        mathtest(51,def,"74.7794084E+119375329","-34799355.6","7.47794084E+119375330","7.47794084E+119375330","-2.60227522E+119375338","-2.14887337E+119375323","","","");
        mathtest(52,def,"-9432.08369","33735.5058","24303.4221","-43167.5895","-318196114","-0.279589218","0","-9432.08369","2.309567E+134087");
        mathtest(53,def,"4249198.78E-112433155","418673051.","418673051","-418673051","1.77902502E-112433140","1.01492054E-112433157","0","4.24919878E-112433149","");
        mathtest(54,def,"-2960933.02","-207933.38","-3168866.40","-2752999.64","6.15676811E+11","14.2398158","14","-49865.70","-2.75680397E-1345624");
        mathtest(55,def,"29317.7519E+945600035","1.43555750","2.93177519E+945600039","2.93177519E+945600039","4.20873186E+945600039","2.04225549E+945600039","","","2.93177519E+945600039");
        mathtest(56,def,"-51.1693770","-638055.414","-638106.583","638004.245","32648898.0","0.0000801958198","0","-51.1693770","-3.48266075E-1090443");
        mathtest(57,def,"-756343055.","-68.9248344E+217100975","-6.89248344E+217100976","6.89248344E+217100976","5.21308198E+217100985","1.09734475E-217100968","0","-756343055","-7.06265897E-63");
        mathtest(58,def,"2538.80406E+694185197","-3386499.65","2.53880406E+694185200","2.53880406E+694185200","-8.59765906E+694185206","-7.49683839E+694185193","","","");
        mathtest(59,def,"-54344.0672","-8086.45235","-62430.5196","-46257.6149","439450710","6.72038427","6","-5825.35310","3.62916861E-38289");
        mathtest(60,def,"3.31600054","217481648","217481651","-217481645","721169262","1.5247266E-8","0","3.31600054","3.73134969E+113224119");
        mathtest(61,def,"681832.671","320341.161E+629467560","3.20341161E+629467565","-3.20341161E+629467565","2.18419069E+629467571","2.12845789E-629467560","0","681832.671","3.16981139E+17");
        mathtest(62,def,"832689481","348040024E-882122501","832689481","832689481","2.89809267E-882122484","2.3925107E+882122501","","","5.77363381E+26");
        mathtest(63,def,"14.5512326E+257500811","60.9979577E-647314724","1.45512326E+257500812","1.45512326E+257500812","8.87595471E-389813911","2.38552784E+904815534","","","");
        mathtest(64,def,"-901.278844","449461667.","449460766","-449462568","-4.05090292E+11","-0.00000200524074","0","-901.278844","");
        mathtest(65,def,"-5.32627675","-738860216E-238273224","-5.32627675","-5.32627675","3.93537399E-238273215","7.20877459E+238273215","","","-0.00000822306838");
        mathtest(66,def,"-505383463.","3.18756328","-505383460","-505383466","-1.61094177E+9","-158548527","-158548527","-0.23671144","-1.29081226E+26");
        mathtest(67,def,"769241.44E-720927320","-145382631.","-145382631","145382631","-1.11834344E-720927306","-5.29115091E-720927323","0","7.6924144E-720927315","");
        mathtest(68,def,"-6.45038910","56736.4411E+440937167","5.67364411E+440937171","-5.67364411E+440937171","-3.65972121E+440937172","-1.13690407E-440937171","0","-6.45038910","72030.3421");
        mathtest(69,def,"58.4721075","-712186829","-712186771","712186887","-4.16430648E+10","-8.21022028E-8","0","58.4721075","");
        mathtest(70,def,"8244.08357","245.302828E+652007959","2.45302828E+652007961","-2.45302828E+652007961","2.02229701E+652007965","3.36077804E-652007958","0","8244.08357","67964913.9");
        mathtest(71,def,"45.5361397","-76579063.9","-76579018.4","76579109.4","-3.48711495E+9","-5.94629098E-7","0","45.5361397","3.98335374E-126995367");
        mathtest(72,def,"594420.54E+685263039","-952420.179","5.94420540E+685263044","5.94420540E+685263044","-5.66138117E+685263050","-6.24115861E+685263038","","","");
        mathtest(73,def,"-841310701.","9398110.4","-831912591","-850708811","-7.90673085E+15","-89.5191337","-89","-4878875.4","1.30001466E+83877722");
        mathtest(74,def,"904392146E-140100276","168116093.","168116093","-168116093","1.52042874E-140100259","5.37956914E-140100276","0","9.04392146E-140100268","");
        mathtest(75,def,"-907324792E+685539670","-15.6902171","-9.07324792E+685539678","-9.07324792E+685539678","1.42361230E+685539680","5.78274211E+685539677","","","");
        mathtest(76,def,"987013606.","-26818.3572E+560907442","-2.68183572E+560907446","2.68183572E+560907446","-2.64700834E+560907455","-3.68036565E-560907438","0","987013606","1.0399934E-27");
        mathtest(77,def,"-741317564","630.241530E-212782946","-741317564","-741317564","-4.67209116E-212782935","-1.1762436E+212782952","","","1.65968527E+53");
        mathtest(78,def,"61867907.2","-139204670","-77336763","201072577","-8.61230161E+15","-0.444438446","0","61867907.2","");
        mathtest(79,def,"-273.622743E+531282717","-4543.68684","-2.73622743E+531282719","-2.73622743E+531282719","1.24325606E+531282723","6.02204229E+531282715","","","");
        mathtest(80,def,"-383588949.","-428640583.","-812229532","45051634","1.64421791E+17","0.89489648","0","-383588949","");
        mathtest(81,def,"-56182.2686","32.7741649","-56149.4944","-56215.0428","-1841326.94","-1714.22426","-1714","-7.3499614","-5.45476402E+156");
        mathtest(82,def,"-6366384.30","332014.980","-6034369.32","-6698399.28","-2.11373496E+12","-19.1749911","-19","-58099.680","-3.05392399E+2258994");
        mathtest(83,def,"-1.27897702","-8213776.03E-686519123","-1.27897702","-1.27897702","1.05052308E-686519116","1.55711212E+686519116","","","0.139668371");
        mathtest(84,def,"65.4059036","401162145E+884155506","4.01162145E+884155514","-4.01162145E+884155514","2.62383726E+884155516","1.63041066E-884155513","0","65.4059036","18300704.1");
        mathtest(85,def,"-20630916.8","158987411.E-480500612","-20630916.8","-20630916.8","-3.28005605E-480500597","-1.29764468E+480500611","","","4.25634728E+14");
        mathtest(86,def,"-4.72705853","-97626742.4","-97626747.1","97626737.7","461487325","4.84197097E-8","0","-4.72705853","2.92654449E-65858120");
        mathtest(87,def,"8.43528169","-4573.45752","-4565.02224","4581.89280","-38578.4025","-0.00184439927","0","8.43528169","8.84248688E-4236");
        mathtest(88,def,"1.91075189","-704247089.","-704247087","704247091","-1.34564146E+9","-2.71318394E-9","0","1.91075189","6.84547494E-198037309");
        mathtest(89,def,"31997198E-551746308","326.892584","326.892584","-326.892584","1.04596467E-551746298","9.78829119E-551746304","0","3.1997198E-551746301","");
        mathtest(90,def,"127589.213","84184304.","84311893.2","-84056714.8","1.07410091E+13","0.00151559385","0","127589.213","2.87917042E+429829394");
        mathtest(91,def,"714494248","-7025063.59","707469185","721519312","-5.01936753E+15","-101.706446","-101","4962825.41","1.65018516E-62199908");
        mathtest(92,def,"-52987680.2E+279533503","-42014114.8","-5.29876802E+279533510","-5.29876802E+279533510","2.22623048E+279533518","1.26118759E+279533503","","","");
        mathtest(93,def,"-8795.0513","-225294.394E-884414238","-8795.05130","-8795.05130","1.98147575E-884414229","3.90380388E+884414236","","","1.2927759E-8");
        mathtest(94,def,"83280.1394","161566354.","161649634","-161483074","1.34552685E+13","0.000515454718","0","83280.1394","5.30774809E+794993940");
        mathtest(95,def,"112.877897","-9.96481666","102.913080","122.842714","-1124.80755","-11.3276441","-11","3.26491374","2.97790545E-21");
        mathtest(96,def,"-572542.121E+847487397","433.843420","-5.72542121E+847487402","-5.72542121E+847487402","-2.48393632E+847487405","-1.3196976E+847487400","","","");
        mathtest(97,def,"4709649.89","20949266.4","25658916.3","-16239616.5","9.86637102E+13","0.224812163","0","4709649.89","4.85293644E+139794213");
        mathtest(98,def,"-9475.19322","-30885.2475E+584487341","-3.08852475E+584487345","3.08852475E+584487345","2.92643688E+584487349","3.06787026E-584487342","0","-9475.19322","-1.17553557E-12");
        mathtest(99,def,"-213230447.","864.815822E+127783046","8.64815822E+127783048","-8.64815822E+127783048","-1.84405064E+127783057","-2.46561686E-127783041","0","-213230447","-9.11261361E+74");
        mathtest(100,def,"-89.1168786E+403375873","6464.05744","-8.91168786E+403375874","-8.91168786E+403375874","-5.76056622E+403375878","-1.37865233E+403375871","","","");
        mathtest(101,def,"61774.4958","-14000.7706","47773.7252","75775.2664","-864890545","-4.41222112","-4","5771.4134","7.59030407E-67077");
        mathtest(102,def,"1.60731414","7.04330293E-427033419","1.60731414","1.60731414","1.13208004E-427033418","2.28204602E+427033418","","","27.7143921");
        mathtest(103,def,"7955012.51","-230117662.","-222162650","238072675","-1.83058888E+15","-0.0345693261","0","7955012.51","");
        mathtest(104,def,"4086661.08","1.77621994","4086662.86","4086659.30","7258808.90","2300762.98","2300762","1.73840572","1.67007988E+13");
        mathtest(105,def,"-610.076931","-207.658306","-817.735237","-402.418625","126687.542","2.93788841","2","-194.760319","4.36518377E-580");
        mathtest(106,def,"-98.6353697","-99253.3899E-716309653","-98.6353697","-98.6353697","9.78989481E-716309647","9.93773309E+716309649","","","1.14729007E-20");
        mathtest(107,def,"-959923730","409.125542E-900295528","-959923730","-959923730","-3.92729316E-900295517","-2.3462816E+900295534","","","8.49076677E+35");
        mathtest(108,def,"379965133","-8.15869657","379965125","379965141","-3.10002023E+9","-46571793.6","-46571793","5.19214999","2.30170697E-69");
        mathtest(109,def,"833.646797","1389499.46E-443407251","833.646797","833.646797","1.15835177E-443407242","5.99961944E+443407247","","","833.646797");
        mathtest(110,def,"2314933.4E-646489194","-7401538.17","-7401538.17","7401538.17","-1.71340679E-646489181","-3.12763826E-646489195","0","2.3149334E-646489188","");
        mathtest(111,def,"808525347","-5959.74667E+58232168","-5.95974667E+58232171","5.95974667E+58232171","-4.81860624E+58232180","-1.35664382E-58232163","0","808525347","3.5796302E-54");
        mathtest(112,def,"-17220490.6E+726428704","19.9855688","-1.72204906E+726428711","-1.72204906E+726428711","-3.44161300E+726428712","-8.61646259E+726428709","","","");
        mathtest(113,def,"59015.9705","-72070405.4E+322957279","-7.20704054E+322957286","7.20704054E+322957286","-4.25330492E+322957291","-8.18865527E-322957283","0","59015.9705","4.01063488E-34");
        mathtest(114,def,"16411470E+578192008","497470.005E-377473621","1.64114700E+578192015","1.64114700E+578192015","8.16421406E+200718399","3.29898684E+955665630","","","");
        mathtest(115,def,"-107.353544E+609689808","-659.50136E-456711743","-1.07353544E+609689810","-1.07353544E+609689810","7.07998083E+152978069","","","","");
        mathtest(116,def,"786.134163","-53.0292275E-664419768","786.134163","786.134163","-4.16880874E-664419764","-1.48245449E+664419769","","","3.33055532E-15");
        mathtest(117,def,"23.5414714","5000786.91","5000810.45","-5000763.37","117725882","0.0000047075534","0","23.5414714","4.4895618E+6860247");
        mathtest(118,def,"-69775.6113","561292120.","561222344","-561361896","-3.91645008E+13","-0.000124312473","0","-69775.6113","");
        mathtest(119,def,"919043.871","-71606613.7","-70687569.8","72525657.6","-6.58096194E+13","-0.0128346227","0","919043.871","3.05862429E-427014317");
        mathtest(120,def,"-27667.1915","-293455.107E-789181924","-27667.1915","-27667.1915","8.11907864E-789181915","9.42808315E+789181922","","","-4.72176938E-14");
        mathtest(121,def,"-908603625.","-982.409273E+449441134","-9.82409273E+449441136","9.82409273E+449441136","8.92620627E+449441145","9.2487281E-449441129","0","-908603625","2.60768632E-90");
        mathtest(122,def,"847.113351","5.71511268","852.828464","841.398238","4841.34825","148.223386","148","1.27667436","3.69529538E+17");
        mathtest(123,def,"-992140475","3.82918218","-992140471","-992140479","-3.79908663E+9","-259099836","-259099836","-0.14787752","9.68930595E+35");
        mathtest(124,def,"-12606437.5","268123145E+362798858","2.68123145E+362798866","-2.68123145E+362798866","-3.38007767E+362798873","-4.70173416E-362798860","0","-12606437.5","-2.00344362E+21");
        mathtest(125,def,"3799470.64","-264.703992","3799205.94","3799735.34","-1.00573505E+9","-14353.6583","-14353","174.242824","2.3625466E-1744");
        mathtest(126,def,"-8.11070247","-931284056.E-654288974","-8.11070247","-8.11070247","7.55336789E-654288965","8.70916067E+654288965","","","-6.58375662E-9");
        mathtest(127,def,"-242660177.","-6.09832715E-943742415","-242660177","-242660177","1.47982115E-943742406","3.97912692E+943742422","","","4.89788901E-51");
        mathtest(128,def,"76.1463803","-45.6758006E-636907996","76.1463803","76.1463803","-3.47804688E-636907993","-1.66710554E+636907996","","","3.90619287E-10");
        mathtest(129,def,"761185.862","-70878470.9E+221214712","-7.08784709E+221214719","7.08784709E+221214719","-5.39516900E+221214725","-1.07393099E-221214714","0","761185.862","6.75406144E-42");
        mathtest(130,def,"6203606.54","-195.92748E-833512061","6203606.54","6203606.54","-1.21545700E-833512052","-3.1662769E+833512065","","","2.59843292E-14");
        mathtest(131,def,"-163274837.","95.0448550E+887876533","9.50448550E+887876534","-9.50448550E+887876534","-1.55184332E+887876543","-1.71787139E-887876527","0","-163274837","1.34645731E+82");
        mathtest(132,def,"2.38638190","-807986179.","-807986177","807986181","-1.92816359E+9","-2.95349347E-9","0","2.38638190","1.19029305E-305208656");
        mathtest(133,def,"-109022296E-811981158","7.19685680","7.19685680","-7.19685680","-7.84617852E-811981150","-1.51485988E-811981151","0","-1.09022296E-811981150","");
        mathtest(134,def,"-559250.780E-273710421","-393780811.","-393780811","393780811","2.20222226E-273710407","1.42020831E-273710424","0","-5.59250780E-273710416","");
        mathtest(135,def,"-88021.9966E+555334642","7599686.64E+818884053","7.59968664E+818884059","-7.59968664E+818884059","","-1.15823192E-263549413","0","-8.80219966E+555334646","");
        mathtest(136,def,"194.317648E-197450009","-930.979064","-930.979064","930.979064","-1.80905662E-197450004","-2.08723972E-197450010","0","1.94317648E-197450007","");
        mathtest(137,def,"9495479.65","7405697.96","16901177.6","2089781.69","7.03206543E+13","1.28218565","1","2089781.69","1.0135446E+51673383");
        mathtest(138,def,"-1656.28925","-163050511E-682882380","-1656.28925","-1656.28925","2.70058809E-682882369","1.01581359E+682882375","","","3.64525265E-7");
        mathtest(139,def,"95581.3784E+64262149","-99.2879365","9.55813784E+64262153","9.55813784E+64262153","-9.49007783E+64262155","-9.62668596E+64262151","","","");
        mathtest(140,def,"643761.452","3.73446939","643765.186","643757.718","2404107.44","172383.647","172383","2.41514363","1.71751236E+23");
        mathtest(141,def,"7960.49866E-129827423","3220.22850","3220.22850","-3220.22850","2.56346247E-129827416","2.47202913E-129827423","0","7.96049866E-129827420","");
        mathtest(142,def,"-6356.64112E-707203818","1805054.98","1805054.98","-1805054.98","-1.14740867E-707203808","-3.52157756E-707203821","0","-6.35664112E-707203815","");
        mathtest(143,def,"2.3904042","8476.52006","8478.91046","-8474.12966","20262.3092","0.000282003013","0","2.3904042","2.00251752E+3208");
        mathtest(144,def,"-713298.658","-957.782729","-714256.441","-712340.875","683185135","744.739528","744","-708.307624","3.68122321E-5608");
        mathtest(145,def,"607779233.E-820497365","-20.1188742E-857318323","6.07779233E-820497357","6.07779233E-820497357","","-3.02094057E+36820965","","","");
        mathtest(146,def,"-205888251","-908.792922E+250680613","-9.08792922E+250680615","9.08792922E+250680615","1.87109785E+250680624","2.26551336E-250680608","0","-205888251","-1.5042358E-75");
        mathtest(147,def,"51542399.1","-23212.2414","51519186.9","51565611.3","-1.19641461E+12","-2220.4835","-2220","11223.1920","1.71641348E-179015");
        mathtest(148,def,"4.44287230","158923023","158923027","-158923019","706074697","2.79561275E-8","0","4.44287230","7.12573416E+102928693");
        mathtest(149,def,"-79123682.6","-3.8571770","-79123686.5","-79123678.8","305194049","20513365.8","20513365","-2.9293950","2.55137345E-32");
        mathtest(150,def,"-80.3324347E-569715030","883142.351","883142.351","-883142.351","-7.09449752E-569715023","-9.09620455E-569715035","0","-8.03324347E-569715029","");
        mathtest(151,def,"13637.483","-52798.5631","-39161.0801","66436.0461","-720039507","-0.258292692","0","13637.483","1.47163791E-218310");
        mathtest(152,def,"6.42934843E-276476458","84057440.0E-388039782","6.42934843E-276476458","6.42934843E-276476458","5.40434570E-664516232","7.64875593E+111563316","","","");
        mathtest(153,def,"-5.64133087","-17401297.","-17401302.6","17401291.4","98166473.9","3.24190253E-7","0","-5.64133087","-1.25908916E-13075014");
        mathtest(154,def,"95469.7057E+865733824","198.829749","9.54697057E+865733828","9.54697057E+865733828","1.89822176E+865733831","4.80158056E+865733826","","","");
        mathtest(155,def,"-416466.209","-930153427","-930569893","929736961","3.87377472E+14","0.000447739262","0","-416466.209","");
        mathtest(156,def,"-1541733.85","-1.99208708","-1541735.84","-1541731.86","3071268.08","773928.944","773928","-1.88034976","4.20708401E-13");
        mathtest(157,def,"-39152691.8","-645131748.","-684284440","605979056","2.52586445E+16","0.0606894513","0","-39152691.8","");
        mathtest(158,def,"113.939979","-58282550.4","-58282436.5","58282664.3","-6.64071257E+9","-0.0000019549587","0","113.939979","2.106557E-119868330");
        mathtest(159,def,"-324971.736","-9517.15154","-334488.888","-315454.585","3.09280526E+9","34.1459033","34","-1388.58364","-5.82795263E-52457");
        mathtest(160,def,"-76.9436744","-9548122.75E-273599728","-76.9436744","-76.9436744","7.34667648E-273599720","8.05851332E+273599722","","","1.37489895E-19");
        mathtest(161,def,"-430393.282","-70.2551505","-430463.537","-430323.027","30237344.8","6126.14561","6126","-10.2300370","4.26006409E-395");
        mathtest(162,def,"-3308051.90","-349433799.E+397813188","-3.49433799E+397813196","3.49433799E+397813196","1.15594514E+397813203","9.46689161E-397813191","0","-3308051.90","-2.76237768E-20");
        mathtest(163,def,"23.1543212E-655822712","5848.20853","5848.20853","-5848.20853","1.35411299E-655822707","3.95921607E-655822715","0","2.31543212E-655822711","");
        mathtest(164,def,"-174.261308E-82902077","-200096204.","-200096204","200096204","3.48690262E-82902067","8.70887626E-82902084","0","-1.74261308E-82902075","");
        mathtest(165,def,"-50669105.2","9105789.01E+609889700","9.10578901E+609889706","-9.10578901E+609889706","-4.61382181E+609889714","-5.56449366E-609889700","0","-50669105.2","-2.20135008E+69");
        mathtest(166,def,"424768856.","-971.71757","424767884","424769828","-4.12755361E+11","-437132.012","-437132","11.19076","2.72651473E-8387");
        mathtest(167,def,"7181.2767","999117.918","1006299.19","-991936.641","7.17494223E+9","0.00718761677","0","7181.2767","3.09655124E+3852800");
        mathtest(168,def,"8096417.07E-433694528","-68.4863363","-68.4863363","68.4863363","-5.54493942E-433694520","-1.18219451E-433694523","0","8.09641707E-433694522","");
        mathtest(169,def,"1236287.5","-7119.97299E-176200498","1236287.50","1236287.50","-8.80233361E-176200489","-1.73636544E+176200500","","","2.26549784E-43");
        mathtest(170,def,"-752995833E-654401067","-15.2736930E+803939983","-1.52736930E+803939984","1.52736930E+803939984","1.15010272E+149538926","","0","-7.52995833E-654401059","");
        mathtest(171,def,"702992.459","-312.689474","702679.770","703305.148","-219818342","-2248.21274","-2248","66.521448","8.02493322E-1831");
        mathtest(172,def,"-4414.38805","-17680.4630E-584364536","-4414.38805","-4414.38805","7.80484246E-584364529","2.49676044E+584364535","","","5.13167312E-8");
        mathtest(173,def,"9.46350807","7826.65424","7836.11775","-7817.19073","74067.6056","0.00120913839","0","9.46350807","3.63271495E+7639");
        mathtest(174,def,"2078153.7","-16934607.3E+233594439","-1.69346073E+233594446","1.69346073E+233594446","-3.51927168E+233594452","-1.2271638E-233594440","0","2078153.7","2.31549939E-13");
        mathtest(175,def,"-9359.74629","7.07761788E+252457696","7.07761788E+252457696","-7.07761788E+252457696","-6.62447077E+252457700","-1.32244301E-252457693","0","-9359.74629","-6.29286677E+27");
        mathtest(176,def,"66.2319284E+730468479","25.9391685E+221147044","6.62319284E+730468480","6.62319284E+730468480","1.71800115E+951615526","2.55335588E+509321435","","","");
        mathtest(177,def,"317997088.E-90968742","-977426.461","-977426.461","977426.461","-3.10818768E-90968728","-3.2534119E-90968740","0","3.17997088E-90968734","");
        mathtest(178,def,"227473386","-6759.61390","227466626","227480146","-1.53763226E+12","-33651.8312","-33651","5618.65110","1.40992627E-56493");
        mathtest(179,def,"-392019.462","-245456.503","-637475.965","-146562.959","9.62237263E+10","1.59710359","1","-146562.959","-3.08656533E-1372917");
        mathtest(180,def,"-3619556.28E+587673583","-3.45236972","-3.61955628E+587673589","-3.61955628E+587673589","1.24960465E+587673590","1.04842661E+587673589","","","");
        mathtest(181,def,"-249.400704E-923930848","831102.919","831102.919","-831102.919","-2.07277653E-923930840","-3.00084019E-923930852","0","-2.49400704E-923930846","");
        mathtest(182,def,"65234.2739E+154949914","-694581895","6.52342739E+154949918","6.52342739E+154949918","-4.53105456E+154949927","-9.39187652E+154949909","","","");
        mathtest(183,def,"45.2316213","-88775083.4","-88775038.2","88775128.6","-4.01544095E+9","-5.09508069E-7","0","45.2316213","1.92314254E-146962015");
        mathtest(184,def,"331100375.","442.343378","331100817","331099933","1.46460058E+11","748514.37","748514","163.759708","6.64011043E+3765");
        mathtest(185,def,"81.8162765","5.61239515E+467372163","5.61239515E+467372163","-5.61239515E+467372163","4.59185273E+467372165","1.45777826E-467372162","0","81.8162765","2.99942677E+11");
        mathtest(186,def,"-5738.13069E+789464078","33969715.0","-5.73813069E+789464081","-5.73813069E+789464081","-1.94922664E+789464089","-1.68919012E+789464074","","","");
        mathtest(187,def,"-7413.03911","2.70630320E-254858264","-7413.03911","-7413.03911","-2.00619315E-254858260","-2.73917539E+254858267","","","-4.07369842E+11");
        mathtest(188,def,"-417696.182","27400.6002","-390295.582","-445096.782","-1.14451261E+10","-15.2440523","-15","-6687.1790","-1.58020334E+154017");
        mathtest(189,def,"68.8538735E+655647287","3198.17933E-132454826","6.88538735E+655647288","6.88538735E+655647288","2.20207035E+523192466","2.15290846E+788102111","","","");
        mathtest(190,def,"-6817.04246","434420.439","427603.397","-441237.481","-2.96146258E+9","-0.0156922692","0","-6817.04246","5.94143518E+1665390");
        mathtest(191,def,"8578.27511","647042.341E-490924334","8578.27511","8578.27511","5.55050721E-490924325","1.3257672E+490924332","","","3.98473846E+23");
        mathtest(192,def,"4124.11615E+733109424","597385828E+375928745","4.12411615E+733109427","4.12411615E+733109427","","6.9036056E+357180673","","","");
        mathtest(193,def,"102.714400","-919017.468","-918914.754","919120.182","-94396327.8","-0.000111765449","0","102.714400","4.04295689E-1848724");
        mathtest(194,def,"-4614.33015E+996778733","-433.560812E+22860599","-4.61433015E+996778736","-4.61433015E+996778736","","1.06428672E+973918135","","","");
        mathtest(195,def,"457455170.","3709230.48E+677010879","3.70923048E+677010885","-3.70923048E+677010885","1.69680666E+677010894","1.23328861E-677010877","0","457455170","4.37919376E+34");
        mathtest(196,def,"-2522468.15","-48482043.5","-51004511.7","45959575.4","1.22294411E+14","0.0520289156","0","-2522468.15","1.42348178E-310373595");
        mathtest(197,def,"-659811384","62777.6118","-659748606","-659874162","-4.14213829E+13","-10510.2976","-10510","-18683.9820","3.4393524E+553665");
        mathtest(198,def,"4424.94176","-825848.20","-821423.258","830273.142","-3.65433019E+9","-0.00535805704","0","4424.94176","3.42152775E-3010966");
        mathtest(199,def,"43.6441884","-6509.89663E-614169377","43.6441884","43.6441884","-2.84119155E-614169372","-6.70428286E+614169374","","","3.31524056E-12");
        mathtest(200,def,"897.388381E-843864876","84195.1369","84195.1369","-84195.1369","7.55557376E-843864869","1.06584348E-843864878","0","8.97388381E-843864874","");
        mathtest(201,def,"796199825","496.76834","796200322","796199328","3.95526865E+11","1602758.79","1602758","393.91828","6.42647264E+4423");
        mathtest(202,def,"573583582","1598.69521","573585181","573581983","9.16985325E+11","358782.323","358782","517.16578","9.91156302E+14004");
        mathtest(203,def,"-783144270.","6347.71496","-783137922","-783150618","-4.97117660E+12","-123374.202","-123374","-1284.52496","1.28110803E+56458");
        mathtest(204,def,"26909234.7","52411.5081","26961646.2","26856823.2","1.41035357E+12","513.422255","513","22131.0447","9.75836528E+389415");
        mathtest(205,def,"8.21915282","24859.7841E-843282959","8.21915282","8.21915282","2.04326365E-843282954","3.30620443E+843282955","","","67.5544731");
        mathtest(206,def,"-688.387710","82783.5207E-831870858","-688.387710","-688.387710","-5.69871582E-831870851","-8.31551623E+831870855","","","5.04272012E+22");
        mathtest(207,def,"-9792232.","-1749.01166","-9793981.01","-9790482.99","1.71267279E+10","5598.72311","5598","-1264.72732","-8.86985674E-12228");
        mathtest(208,def,"-130.765600","8.67437427","-122.091226","-139.439974","-1134.30976","-15.0749317","-15","-0.64998595","-1.11799947E+19");
        mathtest(209,def,"917.259102","-368640.426","-367723.167","369557.685","-338138786","-0.00248822169","0","917.259102","8.67104255E-1092094");
        mathtest(210,def,"-4.9725631","-294563717.","-294563722","294563712","1.46473667E+9","1.6881112E-8","0","-4.9725631","-6.27962584E-205187284");
        mathtest(211,def,"-60962887.2E-514249661","-243021.407","-243021.407","243021.407","1.48152866E-514249648","2.5085398E-514249659","0","-6.09628872E-514249654","");
        mathtest(212,def,"-55389219.8","-3772200E+981866393","-3.77220000E+981866399","3.77220000E+981866399","2.08939215E+981866407","1.46835321E-981866392","0","-55389219.8","1.06242678E-31");
        mathtest(213,def,"681.666010","626886700","626887382","-626886018","4.27327356E+11","0.00000108738311","0","681.666010","");
        mathtest(214,def,"6.42652138","53465894.5","53465900.9","-53465888.1","343599714","1.2019852E-7","0","6.42652138","4.61155532E+43199157");
        mathtest(215,def,"561546656","651408.476","562198064","560895248","3.65796251E+14","862.049968","862","32549.688","8.6052377E+5699419");
        mathtest(216,def,"7845778.36E-79951139","9.45859047","9.45859047","-9.45859047","7.42100044E-79951132","8.29487056E-79951134","0","7.84577836E-79951133","1.12648216E-719560189");
        mathtest(217,def,"54486.2112","10.7565078","54496.9677","54475.4547","586081.356","5065.41828","5065","4.4991930","1.25647168E+52");
        mathtest(218,def,"16576482.5","-2217720.83","14358761.7","18794203.3","-3.67620105E+13","-7.47455779","-7","1052436.69","1.38259374E-16010820");
        mathtest(219,def,"61.2793787E-392070111","6.22575651","6.22575651","-6.22575651","3.81510491E-392070109","9.84288072E-392070111","0","6.12793787E-392070110","");
        mathtest(220,def,"5115136.39","-653674372.","-648559236","658789508","-3.34363357E+15","-0.00782520565","0","5115136.39","");
        mathtest(221,def,"-7.84238366E-416477339","-37432758.9E+97369393","-3.74327589E+97369400","3.74327589E+97369400","2.93562057E-319107938","2.09505895E-513846739","0","-7.84238366E-416477339","");
        mathtest(222,def,"-387781.3E+284108380","-218085.592","-3.87781300E+284108385","-3.87781300E+284108385","8.45695144E+284108390","1.77811517E+284108380","","","");
        mathtest(223,def,"-5353.17736","3.39332346E+546685359","3.39332346E+546685359","-3.39332346E+546685359","-1.81650623E+546685363","-1.57756177E-546685356","0","-5353.17736","-1.53403369E+11");
        mathtest(224,def,"-20837.2900E-168652772","-8236.78305E-712819173","-2.08372900E-168652768","-2.08372900E-168652768","1.71632237E-881471937","2.52978497E+544166401","","","");
        mathtest(225,def,"-98573.8722E+829022366","309011.007","-9.85738722E+829022370","-9.85738722E+829022370","-3.04604115E+829022376","-3.18997932E+829022365","","","");
        mathtest(226,def,"49730750.7","-5315.10636E-299586991","49730750.7","49730750.7","-2.64324229E-299586980","-9.35649211E+299586994","","","3.28756936E-39");
        mathtest(227,def,"1539523.40","-962388.581","577134.82","2501911.98","-1.48161974E+12","-1.59969001","-1","577134.819","3.10144834E-5954673");
        mathtest(228,def,"81596.2121","-37600.9653","43995.2468","119197.177","-3.06809634E+9","-2.17005631","-2","6394.2815","1.97878299E-184684");
        mathtest(229,def,"590146199","-1425404.61","588720794","591571604","-8.41197113E+14","-414.020128","-414","28690.46","2.04650994E-12502170");
        mathtest(230,def,"196.05543","505.936305","701.991735","-309.880875","99191.5598","0.387510104","0","196.05543","8.78437397E+1159");
        mathtest(231,def,"77.8058449","-642.275274","-564.469429","720.081119","-49972.7704","-0.121140963","0","77.8058449","9.33582626E-1215");
        mathtest(232,def,"1468.60684","10068.138","11536.7448","-8599.5312","14786136.3","0.145866777","0","1468.60684","2.54122484E+31884");
        mathtest(233,def,"4.98774767E-387968632","4.41731439E-578812376","4.98774767E-387968632","4.98774767E-387968632","2.20324496E-966781007","1.12913577E+190843744","","","");
        mathtest(234,def,"981.091059","-92238.9930","-91257.9020","93220.0841","-90494851.3","-0.0106364025","0","981.091059","5.29943342E-275953");
        mathtest(235,def,"-3606.24992","8290224.70","8286618.45","-8293830.95","-2.98966222E+10","-0.000435000262","0","-3606.24992","-1.23747107E+29488793");
        mathtest(236,def,"-8978571.35","92243.4796","-8886327.87","-9070814.83","-8.28214663E+11","-97.3355666","-97","-30953.8288","-4.95762813E+641384");
        mathtest(237,def,"-61968.1992E+810060478","474294671.E+179263414","-6.19681992E+810060482","-6.19681992E+810060482","-2.93911867E+989323905","-1.30653374E+630797060","","","");
        mathtest(238,def,"61298431.6E-754429041","-2584862.79","-2584862.79","2584862.79","-1.58448035E-754429027","-2.37143851E-754429040","0","6.12984316E-754429034","");
        mathtest(239,def,"621039.064","-5351539.62","-4730500.56","5972578.68","-3.32351516E+12","-0.116048672","0","621039.064","2.41163312E-31002108");
        mathtest(240,def,"-19.6007605","-57905696.","-57905715.6","57905676.4","1.13499568E+9","3.38494515E-7","0","-19.6007605","1.05663646E-74829963");
        mathtest(241,def,"3626.13109E+687030346","189.896004","3.62613109E+687030349","3.62613109E+687030349","6.88587804E+687030351","1.90953523E+687030347","","","");
        mathtest(242,def,"-249334.026","-7.54735834E-14137188","-249334.026","-249334.026","1.88181324E-14137182","3.30359332E+14137192","","","6.69495408E-44");
        mathtest(243,def,"417613928.","-925213.216","416688715","418539141","-3.86381925E+14","-451.370474","-451","342767.584","8.38430085E-7976054");
        mathtest(244,def,"23.8320309","-50074996.1","-50074972.3","50075019.9","-1.19338885E+9","-4.75926765E-7","0","23.8320309","5.81466387E-68961335");
        mathtest(245,def,"49789677.7","-131827812E+156412534","-1.31827812E+156412542","1.31827812E+156412542","-6.56366427E+156412549","-3.77687204E-156412535","0","49789677.7","2.00844843E-8");
        mathtest(246,def,"-8907163.61E-741867246","773651.288E-472033282","7.73651288E-472033277","-7.73651288E-472033277","","-1.15131504E-269833963","0","-8.90716361E-741867240","");
        mathtest(247,def,"514021711.E+463536646","617441659.","5.14021711E+463536654","5.14021711E+463536654","3.17378418E+463536663","8.32502478E+463536645","","","");
        mathtest(248,def,"998175750","2.39285478","998175752","998175748","2.38848961E+9","417148487","417148486","1.30513692","9.96354828E+17");
        mathtest(249,def,"873575426.","647853.152E+497450781","6.47853152E+497450786","-6.47853152E+497450786","5.65948593E+497450795","1.3484158E-497450778","0","873575426","4.44429064E+53");
        mathtest(250,def,"4352626.8","-130338048.E-744560911","4352626.80","4352626.80","-5.67312881E-744560897","-3.33949055E+744560909","","","2.29746322E-7");
        mathtest(251,def,"437.286960","7.37560835","444.662568","429.911352","3225.25735","59.2882565","59","2.12606735","3.05749452E+18");
        mathtest(252,def,"8498280.45E+220511522","588617612","8.49828045E+220511528","8.49828045E+220511528","5.00223754E+220511537","1.44376931E+220511520","","","");
        mathtest(253,def,"-5320387.77","-7673237.46","-12993625.2","2352849.69","4.08245987E+13","0.693369363","0","-5320387.77","-1.30113745E-51609757");
        mathtest(254,def,"587655375","-4.9748366","587655370","587655380","-2.92348947E+9","-118125563","-118125563","0.7919942","1.42687667E-44");
        mathtest(255,def,"1266098.44","-2661.64904E-642601142","1266098.44","1266098.44","-3.36990970E-642601133","-4.75681963E+642601144","","","4.92717036E-19");
        mathtest(256,def,"3.92737463E+482873483","-685.522747","3.92737463E+482873483","3.92737463E+482873483","-2.69230464E+482873486","-5.72902161E+482873480","","","");
        mathtest(257,def,"22826494.1","986189474.","1.00901597E+9","-963362980","2.25112482E+16","0.0231461547","0","22826494.1","");
        mathtest(258,def,"-647342.380","-498816386","-499463728","498169044","3.22904986E+14","0.00129775685","0","-647342.380","");
        mathtest(259,def,"393092373.","-25.7226822","393092347","393092399","-1.01113902E+10","-15281935.6","-15281935","15.5939430","3.49252839E-224");
        mathtest(260,def,"2.96253492","20.7444888","23.7070237","-17.7819539","61.4562725","0.142810698","0","2.96253492","8.03402246E+9");
        mathtest(261,def,"53553.3750E+386955423","-732470876","5.35533750E+386955427","5.35533750E+386955427","-3.92262875E+386955436","-7.31133165E+386955418","","","");
        mathtest(262,def,"-696451.406E-286535917","-73086090.8","-73086090.8","73086090.8","5.09009107E-286535904","9.52919219E-286535920","0","-6.96451406E-286535912","");
        mathtest(263,def,"1551.29957","-580358622.E+117017265","-5.80358622E+117017273","5.80358622E+117017273","-9.00310081E+117017276","-2.67300168E-117017271","0","1551.29957","7.17506711E-20");
        mathtest(264,def,"-205123006.E-213752799","-78638468.6","-78638468.6","78638468.6","1.61305591E-213752783","2.60843083E-213752799","0","-2.05123006E-213752791","");
        mathtest(265,def,"77632.8073","-3378542.88E+677441319","-3.37854288E+677441325","3.37854288E+677441325","-2.62285768E+677441330","-2.29781921E-677441321","0","77632.8073","2.13729331E-15");
        mathtest(266,def,"3068999.37","2.21006212","3069001.58","3068997.16","6782679.25","1388648.46","1388648","1.02718624","9.41875713E+12");
        mathtest(267,def,"625524274.","55.2468624","625524329","625524219","3.45582535E+10","11322349.3","11322349","16.7522224","6.21482943E+483");
        mathtest(268,def,"61269134.9","-845761303.","-784492168","907030438","-5.18190634E+16","-0.0724425848","0","61269134.9","");
        mathtest(269,def,"-2840.12099","-2856.76731E-82743650","-2840.12099","-2840.12099","8.11356480E-82743644","9.94173022E+82743649","","","-4.36505254E-11");
        mathtest(270,def,"8.9538781","-7.56603391","1.38784419","16.5199120","-67.7453453","-1.18343087","-1","1.38784419","2.42053061E-8");
        mathtest(271,def,"-56233547.2","509752530","453518983","-565986077","-2.86651930E+16","-0.110315386","0","-56233547.2","");
        mathtest(272,def,"-3167.47853E-854859497","-110852115","-110852115","110852115","3.51121694E-854859486","2.85739116E-854859502","0","-3.16747853E-854859494","");
        mathtest(273,def,"-5652.52092","-632243244.","-632248897","632237592","3.57376816E+12","0.00000894042123","0","-5652.52092","");
        mathtest(274,def,"-946.009928","820090.66E-589278015","-946.009928","-946.009928","-7.75813906E-589278007","-1.15354311E+589278012","","","6.41454053E+23");
        mathtest(275,def,"-367.757758","-959.626016","-1327.38377","591.868258","352909.912","0.383230292","0","-367.757758","1.14982199E-2463");
        mathtest(276,def,"809926721.E-744611554","-67.6560549","-67.6560549","67.6560549","-5.47964467E-744611544","-1.19712378E-744611547","0","8.09926721E-744611546","");
        mathtest(277,def,"-1725.08555","75586.3031","73861.2176","-77311.3887","-130392839","-0.0228227269","0","-1725.08555","3.70540587E+244657");
        mathtest(278,def,"2659.84191E+29314492","-74372.4551E+518196680","-7.43724551E+518196684","7.43724551E+518196684","-1.97818973E+547511180","-3.5763804E-488882190","0","2.65984191E+29314495","1.06171811E-205201468");
        mathtest(279,def,"-91.1431113","12147507.0","12147415.9","-12147598.1","-1.10716158E+9","-0.00000750303015","0","-91.1431113","-1.52417006E+23805759");
        mathtest(280,def,"-1136778.91E+697783878","-801552569.","-1.13677891E+697783884","-1.13677891E+697783884","9.11188056E+697783892","1.41822128E+697783875","","","");
        mathtest(281,def,"73123773.0E+433334149","63.3548930","7.31237730E+433334156","7.31237730E+433334156","4.63274881E+433334158","1.15419298E+433334155","","","");
        mathtest(282,def,"-9765484.8","7979.90802E-234029715","-9765484.80","-9765484.80","-7.79276705E-234029705","-1.22375907E+234029718","","","8.27085614E+55");
        mathtest(283,def,"-695010288","-8.26582820","-695010296","-695010280","5.74483564E+9","84082353.4","84082353","-3.45024540","1.83683495E-71");
        mathtest(284,def,"23975643.3E-155955264","-505547.692E+137258948","-5.05547692E+137258953","5.05547692E+137258953","-1.21208311E-18696303","-4.7425087E-293214211","0","2.39756433E-155955257","1.26225952E+779776283");
        mathtest(285,def,"2862.95921","-32601248.6E-605861333","2862.95921","2862.95921","-9.33360449E-605861323","-8.78174712E+605861328","","","4.26142175E-11");
        mathtest(286,def,"-13.133518E+246090516","-8.71269925E-945092108","-1.31335180E+246090517","-1.31335180E+246090517","1.14428392E-699001590","","","","");
        mathtest(287,def,"-34671.2232","817710.762","783039.539","-852381.985","-2.83510323E+10","-0.0424003508","0","-34671.2232","-5.30788828E+3712382");
        mathtest(288,def,"-22464769","62.4366060","-22464706.6","-22464831.4","-1.40262393E+9","-359801.252","-359801","-15.7245940","6.21042536E+455");
        mathtest(289,def,"-9458.60887E-563051963","5676056.01","5676056.01","-5676056.01","-5.36875937E-563051953","-1.66640513E-563051966","0","-9.45860887E-563051960","");
        mathtest(290,def,"-591.924123E-95331874","-134.596188","-134.596188","134.596188","7.96707305E-95331870","4.39777777E-95331874","0","-5.91924123E-95331872","");
        mathtest(291,def,"-182566085.E+68870646","-960345993.","-1.82566085E+68870654","-1.82566085E+68870654","1.75326608E+68870663","1.9010449E+68870645","","","");
        mathtest(292,def,"8232.54893","-99822004E+891979845","-9.98220040E+891979852","9.98220040E+891979852","-8.21789532E+891979856","-8.24722867E-891979850","0","8232.54893","6.99289156E-40");
        mathtest(293,def,"-4336.94317","-819373.601E+563233430","-8.19373601E+563233435","8.19373601E+563233435","3.55357674E+563233439","5.29299841E-563233433","0","-4336.94317","7.98969405E-30");
        mathtest(294,def,"-2.09044362E-876527908","-6515463.33","-6515463.33","6515463.33","1.36202087E-876527901","3.20843433E-876527915","0","-2.09044362E-876527908","");
        mathtest(295,def,"-194343.344","1.95929977","-194341.385","-194345.303","-380776.869","-99190.2041","-99190","-0.39981370","3.77693354E+10");
        mathtest(296,def,"-326002.927","4215.99030","-321786.937","-330218.917","-1.37442518E+9","-77.3253503","-77","-1371.67390","5.51875821E+23243");
        mathtest(297,def,"-12037.8590E+876429044","314.81827","-1.20378590E+876429048","-1.20378590E+876429048","-3.78973794E+876429050","-3.82374854E+876429045","","","");
        mathtest(298,def,"21036045.4E-162804809","-91.7149219","-91.7149219","91.7149219","-1.92931926E-162804800","-2.2936339E-162804804","0","2.10360454E-162804802","");
        mathtest(299,def,"-947019.534","9916.29280","-937103.241","-956935.827","-9.39092299E+9","-95.5013686","-95","-4971.71800","3.76029022E+59261");
        mathtest(300,def,"-5985.84136","-12.4090184E-12364204","-5985.84136","-5985.84136","7.42784156E-12364200","4.82378313E+12364206","","","-0.000167060893");
        mathtest(301,def,"-85344379.4","-6783.08669E+218840215","-6.78308669E+218840218","6.78308669E+218840218","5.78898324E+218840226","1.25819385E-218840211","0","-85344379.4","-3.03232347E-56");
        mathtest(302,def,"-94.1947070E-938257103","15003.240","15003.2400","-15003.2400","-1.41322580E-938257097","-6.27829102E-938257106","0","-9.41947070E-938257102","");
        mathtest(303,def,"-4846233.6","-8289769.76","-13136003.4","3443536.16","4.01741607E+13","0.584604125","0","-4846233.6","4.25077524E-55420465");
        mathtest(304,def,"67.9147198","-108373645.E+291715415","-1.08373645E+291715423","1.08373645E+291715423","-7.36016573E+291715424","-6.26671916E-291715422","0","67.9147198","0.0147243485");
        mathtest(305,def,"1958.77994","5.57285137E+690137826","5.57285137E+690137826","-5.57285137E+690137826","1.09159895E+690137830","3.51486126E-690137824","0","1958.77994","5.64824968E+19");
        mathtest(306,def,"22780314.3","8805279.83","31585594.1","13975034.5","2.00587042E+14","2.58711986","2","5169754.64","2.39132169E+64785373");
        mathtest(307,def,"596745.184","197602423.","198199168","-197005678","1.17918294E+14","0.00301992848","0","596745.184","");
        mathtest(308,def,"171.340497","-480349.924","-480178.584","480521.264","-82303394.7","-0.000356699332","0","171.340497","2.17914102E-1073035");
        mathtest(309,def,"824.65555","-379287.530","-378462.875","380112.186","-312781567","-0.00217422268","0","824.65555","6.35829256E-1106108");
        mathtest(310,def,"19.3164031","-9207644.24E+988115069","-9.20764424E+988115075","9.20764424E+988115075","-1.77858568E+988115077","-2.09786592E-988115075","0","19.3164031","2.67093711E-12");
        mathtest(311,def,"-3123.77646E+177814265","973284435.E+383256112","9.73284435E+383256120","-9.73284435E+383256120","-3.04032301E+561070389","-3.20952062E-205441853","0","-3.12377646E+177814268","");
        mathtest(312,def,"-850.123915E+662955309","6774849.81E-846576865","-8.50123915E+662955311","-8.50123915E+662955311","-5.75946184E-183621547","","","","");
        mathtest(313,def,"-23349.7724","2921.35355","-20428.4189","-26271.1260","-68212940.5","-7.99279238","-7","-2900.29755","-5.6705546E+12759");
        mathtest(314,def,"18886653.3","568707476.","587594129","-549820823","1.07409809E+16","0.0332097855","0","18886653.3","");
        mathtest(315,def,"-90552818.0","-542.03563E-986606878","-90552818.0","-90552818.0","4.90828538E-986606868","1.67060638E+986606883","","","-1.64244241E-40");
        mathtest(316,def,"41501126.1E+791838765","-69.6651675E+204268348","4.15011261E+791838772","4.15011261E+791838772","-2.89118290E+996107122","-5.95722763E+587570422","","","");
        mathtest(317,def,"76783193.3E-271488154","3765.01829E-520346003","7.67831933E-271488147","7.67831933E-271488147","2.89090127E-791834146","2.03938434E+248857853","","","");
        mathtest(318,def,"4192.9928","987822007E-146560989","4192.99280","4192.99280","4.14193056E-146560977","4.24468454E+146560983","","","1.67973653E+36");
        mathtest(319,def,"-891845.629","48277955.","47386109.4","-49169800.6","-4.30564831E+13","-0.0184731443","0","-891845.629","-6.32964147E+287267817");
        mathtest(320,def,"334.901176","-7609296.55E+447340228","-7.60929655E+447340234","7.60929655E+447340234","-2.54836236E+447340237","-4.40121073E-447340233","0","334.901176","6.31926575E-21");
        mathtest(321,def,"4.49868636","-341880896E-447251873","4.49868636","4.49868636","-1.53801492E-447251864","-1.31586363E+447251865","","","0.010983553");
        mathtest(322,def,"807615.58","-314286480","-313478865","315094096","-2.53822658E+14","-0.00256967968","0","807615.58","");
        mathtest(323,def,"-37.7457954","53277.8129E-859225538","-37.7457954","-37.7457954","-2.01101343E-859225532","-7.08471188E+859225534","","","-76620134.1");
        mathtest(324,def,"-28671081.","98.8819623","-28670982.1","-28671179.9","-2.83505275E+9","-289952.589","-289952","-58.2671904","-1.93625566E+738");
        mathtest(325,def,"-89752.2106E-469496896","99.9879961","99.9879961","-99.9879961","-8.97414368E-469496890","-8.97629857E-469496894","0","-8.97522106E-469496892","");
        mathtest(326,def,"-497983567E-13538052","39.4578742","39.4578742","-39.4578742","-1.96493729E-13538042","-1.26206385E-13538045","0","-4.97983567E-13538044","-1.55376543E-527983689");
        mathtest(327,def,"845739221E-654202565","-33313.1551","-33313.1551","33313.1551","-2.81742418E-654202552","-2.53875449E-654202561","0","8.45739221E-654202557","");
        mathtest(328,def,"742.332067E+537827843","-4532.70023E-855387414","7.42332067E+537827845","7.42332067E+537827845","-3.36476873E-317559565","","","","");
        mathtest(329,def,"-893.48654","670389960","670389067","-670390853","-5.98984406E+11","-0.00000133278628","0","-893.48654","");
        mathtest(330,def,"1.37697162","-915.737474E-351578724","1.37697162","1.37697162","-1.26094451E-351578721","-1.50367508E+351578721","","","0.0561920784");
        mathtest(331,def,"-65.2839808E+550288403","-121389.306","-6.52839808E+550288404","-6.52839808E+550288404","7.92477712E+550288409","5.37806689E+550288399","","","");
        mathtest(332,def,"-30346603.E+346067390","792661.544","-3.03466030E+346067397","-3.03466030E+346067397","-2.40545852E+346067403","-3.82844396E+346067391","","","");
        mathtest(333,def,"-61170.7065","-453731131.","-453792302","453669960","2.77550538E+13","0.000134817081","0","-61170.7065","");
        mathtest(334,def,"6569.51133","13.8706351E+399434914","1.38706351E+399434915","-1.38706351E+399434915","9.11232944E+399434918","4.73627291E-399434912","0","6569.51133","6569.51133");
        mathtest(335,def,"300703925.","-3156736.8","297547188","303860662","-9.49243146E+14","-95.2578387","-95","813929.0","4.18609114E-26763256");
        mathtest(336,def,"192138216E+353011592","-473.080633","1.92138216E+353011600","1.92138216E+353011600","-9.08968688E+353011602","-4.06142637E+353011597","","","");
        mathtest(337,def,"8607.64794","-34740.3367","-26132.6888","43347.9846","-299032588","-0.247770999","0","8607.64794","1.29604519E-136698");
        mathtest(338,def,"-67913.8241","-93815.4229","-161729.247","25901.5988","6.37136413E+9","0.723908948","0","-67913.8241","-6.96355203E-453311");
        mathtest(339,def,"34.5559455","-998799398.","-998799364","998799433","-3.45144576E+10","-3.45974833E-8","0","34.5559455","");
        mathtest(340,def,"387995.328","990199543.E-124623607","387995.328","387995.328","3.84192796E-124623593","3.91835495E+124623603","","","7.73152138E+55");
        mathtest(341,def,"-471.09166E-83521919","-441222368","-441222368","441222368","2.07856178E-83521908","1.06769669E-83521925","0","-4.7109166E-83521917","");
        mathtest(342,def,"-97834.3858","70779789.8E+502166065","7.07797898E+502166072","-7.07797898E+502166072","-6.92469726E+502166077","-1.38223617E-502166068","0","-97834.3858","-8.57907886E+34");
        mathtest(343,def,"7732331.06","-952719.482E+115325505","-9.52719482E+115325510","9.52719482E+115325510","-7.36674244E+115325517","-8.11606271E-115325505","0","7732331.06","1.30886724E-69");
        mathtest(344,def,"23.2745547","2.23194245E-221062592","23.2745547","23.2745547","5.19474666E-221062591","1.04279368E+221062593","","","541.704896");
        mathtest(345,def,"671.083363E-218324205","-787150031","-787150031","787150031","-5.28243290E-218324194","-8.52548227E-218324212","0","6.71083363E-218324203","");
        mathtest(346,def,"365167.80","-80263.6516","284904.148","445431.452","-2.93097011E+10","-4.54960362","-4","44113.1936","1.27052227E-446468");
        mathtest(347,def,"-1.43297604E-65129780","56.598733E-135581942","-1.43297604E-65129780","-1.43297604E-65129780","-8.11046283E-200711721","-2.53181646E+70452160","","","8.65831881E-390778680");
        mathtest(348,def,"416998859.","260.220323E-349285593","416998859","416998859","1.08511578E-349285582","1.60248383E+349285599","","","7.25111178E+25");
        mathtest(349,def,"7267.17611E+862630607","4021.56861","7.26717611E+862630610","7.26717611E+862630610","2.92254473E+862630614","1.80705014E+862630607","","","");
        mathtest(350,def,"12.2142434E+593908740","5.27236571E-396050748","1.22142434E+593908741","1.22142434E+593908741","6.43979581E+197857993","2.3166533E+989959488","","","");
        mathtest(351,def,"-28.591932","-1.79153238E-817064576","-28.5919320","-28.5919320","5.12233720E-817064575","1.59594838E+817064577","","","0.00122324372");
        mathtest(352,def,"590.849666","753424.306E+277232744","7.53424306E+277232749","-7.53424306E+277232749","4.45160500E+277232752","7.84219014E-277232748","0","590.849666","1.48530607E+22");
        mathtest(353,def,"1.7270628","-1325026.67","-1325024.94","1325028.40","-2288404.27","-0.00000130341739","0","1.7270628","2.09260036E-314440");
        mathtest(354,def,"33402118.","-5534.83745","33396583.2","33407652.8","-1.84875294E+11","-6034.8869","-6034","4908.82670","8.14473913E-41645");
        mathtest(355,def,"-439842.506","-775110.807","-1214953.31","335268.301","3.40926680E+11","0.567457584","0","-439842.506","-1.84678472E-4374182");
        mathtest(356,def,"-248664.779","-440890.44E+666433944","-4.40890440E+666433949","4.40890440E+666433949","1.09633924E+666433955","5.64005831E-666433945","0","-248664.779","2.61542877E-22");
        mathtest(357,def,"-14161.9142","8306.49493","-5855.4193","-22468.4091","-117635869","-1.70492059","-1","-5855.41927","1.65573372E+34479");
        mathtest(358,def,"-6417227.13","16679.8842","-6400547.25","-6433907.01","-1.07038605E+11","-384.728518","-384","-12151.5972","3.58767978E+113546");
        mathtest(359,def,"514825024.","-25.0446345E-103809457","514825024","514825024","-1.28936046E-103809447","-2.05563002E+103809464","","","7.32860062E-27");
        mathtest(360,def,"525948196","219450390","745398586","306497806","1.15419537E+17","2.39666102","2","87047416","");
        mathtest(361,def,"-638509.181","45580189.0E+269212559","4.55801890E+269212566","-4.55801890E+269212566","-2.91033691E+269212572","-1.40084803E-269212561","0","-638509.181","-1.06129405E+29");
        mathtest(362,def,"330590422","74.359928E+535377965","7.43599280E+535377966","-7.43599280E+535377966","2.45826800E+535377975","4.44581418E-535377959","0","330590422","4.31550742E+59");
        mathtest(363,def,"-3.48593871E-940579904","-20265.9640E-322988987","-2.02659640E-322988983","2.02659640E-322988983","","1.72009519E-617590921","0","-3.48593871E-940579904","");
        mathtest(364,def,"-328103480.","-721.949371E-923938665","-328103480","-328103480","2.36874101E-923938654","4.54468822E+923938670","","","-2.4430038E-60");
        mathtest(365,def,"-1857.01448","19081578.1","19079721.1","-19083435.1","-3.54347668E+10","-0.0000973197537","0","-1857.01448","8.44397087E+62374153");
        mathtest(366,def,"347.28720E+145930771","-62821.9906E-676564106","3.47287200E+145930773","3.47287200E+145930773","-2.18172732E-530633328","-5.52811518E+822494874","","","5.69990135E-875584642");
        mathtest(367,def,"-643.211399E+441807003","-50733419.2","-6.43211399E+441807005","-6.43211399E+441807005","3.26323135E+441807013","1.26782584E+441806998","","","");
        mathtest(368,def,"-53991661.4E-843339554","20718.7346","20718.7346","-20718.7346","-1.11863890E-843339542","-2.60593431E-843339551","0","-5.39916614E-843339547","");
        mathtest(369,def,"-900181424","-105763982.","-1.00594541E+9","-794417442","9.52067719E+16","8.51122856","8","-54069568","1.32627061E-947045602");
        mathtest(370,def,"94218.7462E+563233951","19262.6382E+765263890","1.92626382E+765263894","-1.92626382E+765263894","","4.89126906E-202029939","0","9.42187462E+563233955","");
        mathtest(371,def,"28549.271E+921331828","-2150590.40","2.85492710E+921331832","2.85492710E+921331832","-6.13977881E+921331838","-1.32750853E+921331826","","","");
        mathtest(372,def,"810.7080E+779625763","5957.94044","8.10708000E+779625765","8.10708000E+779625765","4.83014998E+779625769","1.36071854E+779625762","","","");
        mathtest(373,def,"-23.7357549E+77116908","351.100649E+864348022","3.51100649E+864348024","-3.51100649E+864348024","-8.33363895E+941464933","-6.7603848E-787231116","0","-2.37357549E+77116909","3.17403853E+308467637");
        mathtest(374,def,"40216102.2E+292724544","661.025962","4.02161022E+292724551","4.02161022E+292724551","2.65838876E+292724554","6.08389148E+292724548","","","");
        mathtest(375,def,"22785024.3E+783719168","399.505989E+137478666","2.27850243E+783719175","2.27850243E+783719175","9.10275367E+921197843","5.70329981E+646240506","","","");
        mathtest(376,def,"515.591819E+821371364","-692137914.E-149498690","5.15591819E+821371366","5.15591819E+821371366","-3.56860646E+671872685","-7.44926421E+970870047","","","");
        mathtest(377,def,"-536883072E+477911251","624996.301","-5.36883072E+477911259","-5.36883072E+477911259","-3.35549934E+477911265","-8.59017999E+477911253","","","");
        mathtest(378,def,"-399492.914E-334369192","5202119.87E+442442258","5.20211987E+442442264","-5.20211987E+442442264","-2.07821003E+108073078","-7.67942539E-776811452","0","-3.99492914E-334369187","");
        mathtest(379,def,"762.071184","9851631.37","9852393.44","-9850869.30","7.50764438E+9","0.0000773548213","0","762.071184","4.02198436E+28392356");
        mathtest(380,def,"5626.12471","72989818.3","72995444.4","-72984192.2","4.10649820E+11","0.0000770809524","0","5626.12471","1.79814757E+273727098");
        mathtest(381,def,"-47207260.1","-2073.3152","-47209333.4","-47205186.8","9.78755299E+10","22768.9741","22768","-2019.6264","-6.02238319E-15909");
        mathtest(382,def,"207.740860","-51.0390090","156.701851","258.779869","-10602.8876","-4.07023694","-4","3.5848240","6.40297515E-119");
        mathtest(383,def,"-572.812464E-745934021","-182805872.E+604508681","-1.82805872E+604508689","1.82805872E+604508689","1.04713482E-141425329","","0","-5.72812464E-745934019","");
        mathtest(384,def,"-6418504E+3531407","8459416.1","-6.41850400E+3531413","-6.41850400E+3531413","-5.42967961E+3531420","-7.58740784E+3531406","","","");
        mathtest(385,def,"280689.531","-128212543","-127931854","128493233","-3.59879186E+13","-0.00218925173","0","280689.531","1.42173809E-698530938");
        mathtest(386,def,"15.803551E-783422793","239108038E-489186308","2.39108038E-489186300","-2.39108038E-489186300","","6.60937672E-294236493","0","1.5803551E-783422792","");
        mathtest(387,def,"26.515922","-9418242.96E-105481628","26.5159220","26.5159220","-2.49733396E-105481620","-2.81537885E+105481622","","","1.54326108E-13");
        mathtest(388,def,"-88.1094557","-54029934.1","-54030022.2","54029846.0","4.76054809E+9","0.0000016307526","0","-88.1094557","5.05289826E-105089439");
        mathtest(389,def,"6770.68602E-498420397","-6.11248908E-729616908","6.77068602E-498420394","6.77068602E-498420394","","-1.10768067E+231196514","","","");
        mathtest(390,def,"-892973818.E-781904441","555201299.","555201299","-555201299","-4.95780224E-781904424","-1.60837847E-781904441","0","-8.92973818E-781904433","");
        mathtest(391,def,"670175802E+135430680","27355195.4","6.70175802E+135430688","6.70175802E+135430688","1.83327900E+135430696","2.44990318E+135430681","","","");
        mathtest(392,def,"-440950.26","205.477469E-677345561","-440950.260","-440950.260","-9.06053434E-677345554","-2.14597864E+677345564","","","1.94437132E+11");
        mathtest(393,def,"-8.2335779","573665010E+742722075","5.73665010E+742722083","-5.73665010E+742722083","-4.72331555E+742722084","-1.43525886E-742722083","0","-8.2335779","311552.753");
        mathtest(394,def,"452943.863","7022.23629","459966.099","445921.627","3.18067883E+9","64.5013703","64","3520.74044","5.54158976E+39716");
        mathtest(395,def,"62874.1079","-52719654.1","-52656780.0","52782528.2","-3.31470122E+12","-0.0011926123","0","62874.1079","1.18819936E-252973775");
        mathtest(396,def,"-7428.41741E+609772037","-46024819.3","-7.42841741E+609772040","-7.42841741E+609772040","3.41891569E+609772048","1.61400251E+609772033","","","");
        mathtest(397,def,"2.27959297","41937.019","41939.2986","-41934.7394","95599.3337","0.0000543575348","0","2.27959297","2.89712423E+15007");
        mathtest(398,def,"508692408E-671967782","8491989.20","8491989.20","-8491989.20","4.31981043E-671967767","5.99026207E-671967781","0","5.08692408E-671967774","");
        mathtest(399,def,"940.533705E-379310421","-4.01176961E+464620037","-4.01176961E+464620037","4.01176961E+464620037","-3.77320453E+85309619","-2.34443599E-843930456","0","9.40533705E-379310419","");
        mathtest(400,def,"97.0649652","-92.4485649E-151989098","97.0649652","97.0649652","-8.97351673E-151989095","-1.0499348E+151989098","","","1.30748728E-18");
        mathtest(401,def,"297544.536E+360279473","8.80275007","2.97544536E+360279478","2.97544536E+360279478","2.61921019E+360279479","3.38013159E+360279477","","","");
        mathtest(402,def,"-28861028.","82818.820E+138368758","8.28188200E+138368762","-8.28188200E+138368762","-2.39023628E+138368770","-3.48483932E-138368756","0","-28861028","4.81387013E+59");
        mathtest(403,def,"36.2496238E+68828039","49243.00","3.62496238E+68828040","3.62496238E+68828040","1.78504022E+68828045","7.36137599E+68828035","","","");
        mathtest(404,def,"22.447828E-476014683","-56067.5520","-56067.5520","56067.5520","-1.25859476E-476014677","-4.00371109E-476014687","0","2.2447828E-476014682","");
        mathtest(405,def,"282688.791E+75011952","5.99789051","2.82688791E+75011957","2.82688791E+75011957","1.69553642E+75011958","4.7131369E+75011956","","","5.10330507E+450071744");
        mathtest(406,def,"-981.860310E-737387002","-994046289","-994046289","994046289","9.76014597E-737386991","9.87741035E-737387009","0","-9.81860310E-737387000","");
        mathtest(407,def,"-702.91210","-6444903.55","-6445606.46","6444200.64","4.53020069E+9","0.000109064797","0","-702.91210","1.70866703E-18348004");
        mathtest(408,def,"972456720E-17536823","16371.2590","16371.2590","-16371.2590","1.59203408E-17536810","5.94002404E-17536819","0","9.72456720E-17536815","");
        mathtest(409,def,"71471.2045","-74303278.4","-74231807.2","74374749.6","-5.31054481E+12","-0.00096188494","0","71471.2045","2.14535374E-360677853");
        mathtest(410,def,"643.103951E+439708441","788251925.","6.43103951E+439708443","6.43103951E+439708443","5.06927927E+439708452","8.15860933E+439708434","","","");
        mathtest(411,def,"4.30838663","-7.43110827","-3.12272164","11.7394949","-32.0160875","-0.579777131","0","4.30838663","0.0000362908645");
        mathtest(412,def,"823.678025","-513.581840E-324453141","823.678025","823.678025","-4.23026076E-324453136","-1.60379118E+324453141","","","2.63762228E-15");
        mathtest(413,def,"4461.81162","3.22081680","4465.03244","4458.59080","14370.6778","1385.30438","1385","0.98035200","8.8824688E+10");
        mathtest(414,def,"-4458527.10","-99072605","-103531132","94614077.9","4.41717894E+14","0.0450026231","0","-4458527.10","-6.23928099E-658752715");
        mathtest(415,def,"-577964618","487424368.","-90540250","-1.06538899E+9","-2.81714039E+17","-1.18575241","-1","-90540250","");
        mathtest(416,def,"-867.036184","-57.1768608","-924.213045","-809.859323","49574.4072","15.1641096","15","-9.3832720","-3.40312837E-168");
        mathtest(417,def,"771871921E-330504770","5.34285236","5.34285236","-5.34285236","4.12399771E-330504761","1.44468136E-330504762","0","7.71871921E-330504762","");
        mathtest(418,def,"-338683.062E-728777518","166441931","166441931","-166441931","-5.63710628E-728777505","-2.03484218E-728777521","0","-3.38683062E-728777513","");
        mathtest(419,def,"-512568743","-416376887.E-965945295","-512568743","-512568743","2.13421778E-965945278","1.23102112E+965945295","","","1.44874358E-35");
        mathtest(420,def,"7447181.99","5318438.52","12765620.5","2128743.47","3.96073796E+13","1.40025723","1","2128743.47","1.21634782E+36548270");
        mathtest(421,def,"54789.8207","93165435.2","93220225.0","-93110645.4","5.10451749E+12","0.000588091716","0","54789.8207","3.80769825E+441483035");
        mathtest(422,def,"41488.5960","146.797094","41635.3931","41341.7989","6090405.33","282.625459","282","91.815492","6.84738153E+678");
        mathtest(423,def,"785741.663E+56754529","-461.531732","7.85741663E+56754534","7.85741663E+56754534","-3.62644711E+56754537","-1.70246509E+56754532","","","");
        mathtest(424,def,"-4.95436786","-3132.4233","-3137.37767","3127.46893","15519.1773","0.0015816406","0","-4.95436786","1.98062422E-2177");
        mathtest(425,def,"77321.8478E+404626874","82.4797688","7.73218478E+404626878","7.73218478E+404626878","6.37748813E+404626880","9.3746441E+404626876","","","");
        mathtest(426,def,"-7.99307725","-29153.7273","-29161.7204","29145.7342","233027.994","0.000274169994","0","-7.99307725","1.88688028E-26318");
        mathtest(427,def,"-61.6337401E+474999517","5254.87092","-6.16337401E+474999518","-6.16337401E+474999518","-3.23877349E+474999522","-1.1728878E+474999515","","","");
        mathtest(428,def,"-16.4043088","35.0064812","18.6021724","-51.4107900","-574.257128","-0.468607762","0","-16.4043088","-3.33831843E+42");
        mathtest(429,def,"-8.41156520","-56508958.9","-56508967.3","56508950.5","475328792","1.48853657E-7","0","-8.41156520","-8.86365458E-52263827");
        mathtest(430,def,"-360165.79E+503559835","-196688.515","-3.60165790E+503559840","-3.60165790E+503559840","7.08404744E+503559845","1.83114805E+503559835","","","");
        mathtest(431,def,"-653236480.E+565648495","-930.445274","-6.53236480E+565648503","-6.53236480E+565648503","6.07800796E+565648506","7.02068674E+565648500","","","");
        mathtest(432,def,"-3.73342903","855.029289","851.295860","-858.762718","-3192.19117","-0.00436643408","0","-3.73342903","-1.41988961E+489");
        mathtest(433,def,"-5.14890532E+562048011","10847127.8E-390918910","-5.14890532E+562048011","-5.14890532E+562048011","-5.58508340E+171129108","-4.74679142E+952966914","","","-5.14890532E+562048011");
        mathtest(434,def,"653311907","-810.036965E+744537823","-8.10036965E+744537825","8.10036965E+744537825","-5.29206794E+744537834","-8.06521104E-744537818","0","653311907","3.01325171E-71");
        mathtest(435,def,"-1.31557907","98.9139300E-579281802","-1.31557907","-1.31557907","-1.30129096E-579281800","-1.33002406E+579281800","","","15.529932");
        mathtest(436,def,"-875192389","-72071565.6","-947263955","-803120823","6.30764857E+16","12.1433797","12","-10333601.8","1.25564408E-644471405");
        mathtest(437,def,"-72838078.8","-391.398423","-72838470.2","-72837687.4","2.85087092E+10","186097.017","186097","-6.474969","-6.574057E-3075");
        mathtest(438,def,"29186560.9","-79.7419988","29186481.2","29186640.6","-2.32739470E+9","-366012.407","-366012","32.4352144","6.10050869E-598");
        mathtest(439,def,"-329801660E-730249465","-6489.9256","-6489.92560","6489.92560","2.14038824E-730249453","5.08174793E-730249461","0","-3.29801660E-730249457","");
        mathtest(440,def,"91.8429117E+103164883","7131455.16","9.18429117E+103164884","9.18429117E+103164884","6.54973607E+103164891","1.28785654E+103164878","","","");
        mathtest(441,def,"3943866.38E+150855113","-31927007.3","3.94386638E+150855119","3.94386638E+150855119","-1.25915851E+150855127","-1.23527594E+150855112","","","");
        mathtest(442,def,"-7002.0468E-795962156","-5937891.05","-5937891.05","5937891.05","4.15773910E-795962146","1.17921443E-795962159","0","-7.0020468E-795962153","");
        mathtest(443,def,"696504605.","54506.4617","696559111","696450099","3.79640016E+13","12778.386","12778","21037.3974","2.6008532E+481992");
        mathtest(444,def,"-5115.76467","690.960979E+815126701","6.90960979E+815126703","-6.90960979E+815126703","-3.53479376E+815126707","-7.4038402E-815126701","0","-5115.76467","-9.17009655E+25");
        mathtest(445,def,"-261.279392","-613.079357","-874.358749","351.799965","160185.002","0.426175484","0","-261.279392","-2.06318841E-1482");
        mathtest(446,def,"-591407763","-80145822.8","-671553586","-511261940","4.73988618E+16","7.37914644","7","-30387003.4","-2.79334522E-703030105");
        mathtest(447,def,"615630407","-69.4661869","615630338","615630476","-4.27654969E+10","-8862303.15","-8862303","10.4375693","3.44283102E-607");
        mathtest(448,def,"1078757.50","27402569.0E-713742082","1078757.50","1078757.50","2.95607268E-713742069","3.93670207E+713742080","","","1.25536924E+18");
        mathtest(449,def,"-4865.60358E-401116515","66952.5315","66952.5315","-66952.5315","-3.25764477E-401116507","-7.26724363E-401116517","0","-4.86560358E-401116512","");
        mathtest(450,def,"-87805.3921E-934896690","-1875.14745","-1875.14745","1875.14745","1.64648057E-934896682","4.68258601E-934896689","0","-8.78053921E-934896686","");
        mathtest(451,def,"-232540609.E+602702520","68.0834223","-2.32540609E+602702528","-2.32540609E+602702528","-1.58321605E+602702530","-3.41552468E+602702526","","","");
        mathtest(452,def,"-320610803.","-863871235.","-1.18448204E+9","543260432","2.76966450E+17","0.37113263","0","-320610803","");
        mathtest(453,def,"-303956364E+278139979","229537.920E+479603725","2.29537920E+479603730","-2.29537920E+479603730","-6.97695116E+757743717","-1.3242098E-201463743","0","-3.03956364E+278139987","9.23894712E+556279974");
        mathtest(454,def,"-439.747348","74.9494457E-353117582","-439.747348","-439.747348","-3.29588200E-353117578","-5.86725284E+353117582","","","-3.17996693E+18");
        mathtest(455,def,"-89702231.9","1.28993993","-89702230.6","-89702233.2","-115710491","-69539852.1","-69539852","-0.07890964","-89702231.9");
        mathtest(456,def,"-5856939.14","-6743375.34","-12600314.5","886436.20","3.94955390E+13","0.868547107","0","-5856939.14","-3.29213248E-45636942");
        mathtest(457,def,"733317.669E+100381349","-13832.6792E+174055607","-1.38326792E+174055611","1.38326792E+174055611","-1.01437481E+274436966","-5.30134227E-73674257","0","7.33317669E+100381354","1.36366549E-100381355");
        mathtest(458,def,"87.4798787E-80124704","108497.32","108497.320","-108497.320","9.49133239E-80124698","8.06286079E-80124708","0","8.74798787E-80124703","");
        mathtest(459,def,"-694562052","310681.319E+549445264","3.10681319E+549445269","-3.10681319E+549445269","-2.15787454E+549445278","-2.23560932E-549445261","0","-694562052","-3.35068155E+26");
        mathtest(460,def,"-9744135.85","1797016.04","-7947119.81","-11541151.9","-1.75103684E+13","-5.42239782","-5","-759055.65","3.83848006E+12558883");
        mathtest(461,def,"3625.87308","-50.2208536E+658627487","-5.02208536E+658627488","5.02208536E+658627488","-1.82094441E+658627492","-7.21985554E-658627486","0","3625.87308","1.5956477E-18");
        mathtest(462,def,"365347.52","-3655414.47","-3290066.95","4020761.99","-1.33549661E+12","-0.099946948","0","365347.52","1.02663257E-20333994");
        mathtest(463,def,"-19706333.6E-816923050","-383858032.","-383858032","383858032","7.56443443E-816923035","5.1337557E-816923052","0","-1.97063336E-816923043","");
        mathtest(464,def,"-86346.2616","-98.8063785","-86445.0680","-86247.4552","8531561.41","873.893598","873","-88.2931695","-2.05064086E-489");
        mathtest(465,def,"-445588.160E-496592215","328.822976","328.822976","-328.822976","-1.46519625E-496592207","-1.35510044E-496592212","0","-4.45588160E-496592210","");
        mathtest(466,def,"-9709213.71","-34.6690137","-9709248.38","-9709179.04","336608863","280054.512","280054","-17.7472602","-2.80903974E-245");
        mathtest(467,def,"742395536.","-43533.6889","742352002","742439070","-3.23192163E+13","-17053.3569","-17053","15539.1883","5.7622734E-386175");
        mathtest(468,def,"-878849193.","-5842982.47E-972537342","-878849193","-878849193","5.13510043E-972537327","1.50411061E+972537344","","","2.17027042E-54");
        mathtest(469,def,"-78014142.1","-624658.522","-78638800.6","-77389483.6","4.87321987E+13","124.890863","124","-556485.372","-7.86063865E-4929918");
        mathtest(470,def,"857039.371","454.379672","857493.751","856584.991","389421268","1886.17454","1886","79.309608","3.82253101E+2693");
        mathtest(471,def,"166534010.","-173.012236","166533837","166534183","-2.88124214E+10","-962556.255","-962556","44.164784","4.78620664E-1423");
        mathtest(472,def,"-810.879063","43776.610","42965.7309","-44587.4891","-35497536.5","-0.0185231123","0","-810.879063","-2.34758691E+127345");
        mathtest(473,def,"-327.127935","93458944","93458616.9","-93459271.1","-3.05730314E+10","-0.00000350023145","0","-327.127935","2.29323021E+235022854");
        mathtest(474,def,"539295218.","-9587941.10E-309643098","539295218","539295218","-5.17073079E-309643083","-5.62472394E+309643099","","","4.80545269E-88");
        mathtest(475,def,"-3862702.65","879616.733","-2983085.92","-4742319.38","-3.39769789E+12","-4.3913474","-4","-344235.718","-3.50650167E+5793941");
        mathtest(476,def,"-8.25290500","992.091584E+256070257","9.92091584E+256070259","-9.92091584E+256070259","-8.18763759E+256070260","-8.31869268E-256070260","0","-8.25290500","1.46577888E+9");
        mathtest(477,def,"546875205.","447.52857E+557357101","4.47528570E+557357103","-4.47528570E+557357103","2.44742278E+557357112","1.22198948E-557357095","0","546875205","8.94443542E+34");
        mathtest(478,def,"177623437","-7779116.14","169844321","185402553","-1.38175335E+15","-22.83337","-22","6482881.92","2.90085309E-64173820");
        mathtest(479,def,"377204735.","13768.1401","377218503","377190967","5.19340764E+12","27396.9274","27396","12768.8204","2.06065297E+118082");
        mathtest(480,def,"-2435.49239","-11732.0640E-23331504","-2435.49239","-2435.49239","2.85733526E-23331497","2.07592832E+23331503","","","-0.00041059459");
        mathtest(481,def,"-6128465.14E-137123294","-5742264.27","-5742264.27","5742264.27","3.51912664E-137123281","1.06725585E-137123294","0","-6.12846514E-137123288","");
        mathtest(482,def,"-2898065.44","-5.11638105","-2898070.56","-2898060.32","14827607.1","566428.773","566428","-3.95461060","-4.89169151E-33");
        mathtest(483,def,"1851395.31E+594383160","-550301.475","1.85139531E+594383166","1.85139531E+594383166","-1.01882557E+594383172","-3.36432918E+594383160","","","");
        mathtest(484,def,"536412589.E+379583977","899.601161","5.36412589E+379583985","5.36412589E+379583985","4.82557388E+379583988","5.96278231E+379583982","","","");
        mathtest(485,def,"185.85297","867419480.","867419666","-867419294","1.61212487E+11","2.14259622E-7","0","185.85297","");
        mathtest(486,def,"-5.26631053","-3815941.35E+183291763","-3.81594135E+183291769","3.81594135E+183291769","2.00959321E+183291770","1.38008162E-183291769","0","-5.26631053","0.00130009218");
        mathtest(487,def,"-8.11587021E-245942806","4553.06753E+943412048","4.55306753E+943412051","-4.55306753E+943412051","-3.69521051E+697469246","","0","-8.11587021E-245942806","");
        mathtest(488,def,"-405765.352","854963231","854557466","-855368996","-3.46914456E+14","-0.000474599769","0","-405765.352","");
        mathtest(489,def,"-159.609757","-43356.7567","-43516.3665","43197.1470","6920161.40","0.00368131219","0","-159.609757","-8.95397849E-95519");
        mathtest(490,def,"-564240.241E-501316672","-557.781977","-557.781977","557.781977","3.14723037E-501316664","1.01157847E-501316669","0","-5.64240241E-501316667","");
        mathtest(491,def,"318847.270","582107878.E+399633412","5.82107878E+399633420","-5.82107878E+399633420","1.85603508E+399633426","5.47746014E-399633416","0","318847.270","1.0507423E+33");
        mathtest(492,def,"-4426.59663","95.1096765","-4331.48695","-4521.70631","-421012.173","-46.5420217","-46","-51.5515110","-2.38037379E+346");
        mathtest(493,def,"6037.28310","578264.105","584301.388","-572226.822","3.49114411E+9","0.010440356","0","6037.28310","3.57279483E+2186324");
        mathtest(494,def,"-66.9556692","-53.8519404","-120.807610","-13.1037288","3605.69271","1.24332881","1","-13.1037288","2.55554086E-99");
        mathtest(495,def,"-92486.0222","-59935.8544","-152421.877","-32550.1678","5.54322876E+9","1.5430834","1","-32550.1678","1.83152656E-297647");
        mathtest(496,def,"852136219.E+917787351","9246221.91","8.52136219E+917787359","8.52136219E+917787359","7.87904058E+917787366","9.21604767E+917787352","","","");
        mathtest(497,def,"-2120096.16E-269253718","9437.00514","9437.00514","-9437.00514","-2.00073584E-269253708","-2.24657731E-269253716","0","-2.12009616E-269253712","");
        mathtest(498,def,"-524653.169E-865784226","228054.698","228054.698","-228054.698","-1.19649620E-865784215","-2.30055848E-865784226","0","-5.24653169E-865784221","");
        mathtest(499,def,"-288193133","-312268737.","-600461870","24075604","8.99937057E+16","0.922901011","0","-288193133","");
        mathtest(500,def,"-373484759E-113589964","844101958E-852538240","-3.73484759E-113589956","-3.73484759E-113589956","-3.15259216E-966128187","-4.42464036E+738948275","","","3.78602147E-908719644");
    }

    /* mathtest -- general arithmetic test routine
     Arg1  is test number
     Arg2  is MathContext
     Arg3  is left hand side (LHS)
     Arg4  is right hand side (RHS)
     Arg5  is the expected result for add
     Arg6  is the expected result for subtract
     Arg7  is the expected result for multiply
     Arg8  is the expected result for divide
     Arg9  is the expected result for integerDivide
     Arg10 is the expected result for remainder
     Arg11 is the expected result for power

     For power RHS, 0 is added to the number, any exponent is removed and
     the number is then rounded to an integer, using format(rhs+0,,0)

     If an error should result for an operation, the 'expected result' is
     an empty string.
     */

    private void mathtest(int test, android.icu.math.MathContext mc,
            java.lang.String slhs, java.lang.String srhs, java.lang.String add,
            java.lang.String sub, java.lang.String mul, java.lang.String div,
            java.lang.String idv, java.lang.String rem, java.lang.String pow) {
        android.icu.math.BigDecimal lhs;
        android.icu.math.BigDecimal rhs;
        java.lang.String res = null;
        java.lang.String sn = null;
        int e = 0;

        lhs = new android.icu.math.BigDecimal(slhs);
        rhs = new android.icu.math.BigDecimal(srhs);

        try {
            res = lhs.add(rhs, mc).toString();
        } catch (java.lang.ArithmeticException $137) {
            res = "";
        }
        mathtestcheck(test, lhs, rhs, "add", res, add);

        try {
            res = lhs.subtract(rhs, mc).toString();
        } catch (java.lang.ArithmeticException $138) {
            res = "";
        }
        mathtestcheck(test, lhs, rhs, "sub", res, sub);

        try {
            res = lhs.multiply(rhs, mc).toString();
        } catch (java.lang.ArithmeticException $139) {
            res = "";
        }
        mathtestcheck(test, lhs, rhs, "mul", res, mul);

        try {
            res = lhs.divide(rhs, mc).toString();
        } catch (java.lang.ArithmeticException $140) {
            res = "";
        }
        mathtestcheck(test, lhs, rhs, "div", res, div);

        try {
            res = lhs.divideInteger(rhs, mc).toString();
        } catch (java.lang.ArithmeticException $141) {
            res = "";
        }
        mathtestcheck(test, lhs, rhs, "idv", res, idv);

        try {
            res = lhs.remainder(rhs, mc).toString();
        } catch (java.lang.ArithmeticException $142) {
            res = "";
        }
        mathtestcheck(test, lhs, rhs, "rem", res, rem);

        try {
            // prepare an integer from the rhs
            // in Rexx:
            //   n=rhs+0
            //   e=pos('E', n)
            //   if e>0 then n=left(n,e-1)
            //   n=format(n,,0)

            sn = rhs.plus(mc).toString();
            e = sn.indexOf("E", 0);
            if (e > 0)
                sn = sn.substring(0, e);
            sn = (new android.icu.math.BigDecimal(sn)).format(-1, 0);

            res = lhs.pow(new android.icu.math.BigDecimal(sn), mc).toString();
        } catch (java.lang.ArithmeticException $143) {
            res = "";
        }
        mathtestcheck(test, lhs, rhs, "pow", res, pow);
        return;
    }

    /* mathtestcheck -- check for general mathtest error
     Arg1  is test number
     Arg2  is left hand side (LHS)
     Arg3  is right hand side (RHS)
     Arg4  is the operation
     Arg5  is the actual result
     Arg6  is the expected result
     Show error message if a problem, otherwise return quietly
     */

    private void mathtestcheck(int test, android.icu.math.BigDecimal lhs,
            android.icu.math.BigDecimal rhs, java.lang.String op,
            java.lang.String got, java.lang.String want) {
        boolean flag;
        java.lang.String testnum;

        flag = want.equals(got);

        if ((!flag))
            say(">" + test + ">" + " " + lhs.toString() + " " + op + " "
                    + rhs.toString() + " " + "=" + " " + want + " " + "[got"
                    + " " + got + "]");

        testnum = "gen"
                + right((new android.icu.math.BigDecimal(test + 1000))
                        .toString(), 3);

        TestFmwk.assertTrue(testnum, flag);
        return;
    }

    /* ------------------------------------------------------------------ */
    /* Support routines and minor classes follow                          */
    /* ------------------------------------------------------------------ */

    /* ----------------------------------------------------------------- */
    /* Method called to summarise pending tests                          */
    /* ----------------------------------------------------------------- */
    /* Arg1 is section name */

//    private void summary(java.lang.String section) {
//        int bad;
//        int count;
//        int i = 0;
//        Test item = null;
//        bad = 0;
//        count = Tests.size();
//        {
//            int $144 = count;
//            i = 0;
//            for (; $144 > 0; $144--, i++) {
//                item = (Test) (Tests.get(i));
//                if ((!item.ok))
//                {
//                    bad++;
//                    errln("Failed:" + " " + item.name);
//                }
//            }
//        }/*i*/
//        totalcount = totalcount + count;
//        Tests = new java.util.ArrayList(100); // reinitialize
//        if (bad == 0)
//            say("OK" + " " + left(section, 14) + " "
//                    + right("[" + count + " " + "tests]", 12));
//        else
//            throw new DiagException(section + " " + "[failed" + " " + bad + " "
//                    + "of" + " " + count + " " + "tests]", bad);
//    }

    /* ----------------------------------------------------------------- */
    /* right - Utility to do a 'right' on a Java String                  */
    /* ----------------------------------------------------------------- */
    /* Arg1 is string to right-justify */
    /* Arg2 is desired length */

    private static java.lang.String right(java.lang.String s, int len) {
        int slen;
        slen = s.length();
        if (slen == len)
            return s; // length just right
        if (slen > len)
            return s.substring(slen - len); // truncate on left
        // too short
        return (new java.lang.String(new char[len - slen]))
                .replace('\000', ' ').concat(s);
    }

    /* ----------------------------------------------------------------- */
    /* say - Utility to do a display                                     */
    /* ----------------------------------------------------------------- */
    /* Arg1 is string to display, omitted if none */
    /*         [null or omitted gives blank line] */
    // this version doesn't heed continuation final character
    private void say(java.lang.String s) {
        if (s == null)
            s = "  ";
        logln(s);
    }

}
