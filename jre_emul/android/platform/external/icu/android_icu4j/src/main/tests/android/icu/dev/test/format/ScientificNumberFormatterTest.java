/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.format;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.DecimalFormat;
import android.icu.text.NumberFormat;
import android.icu.text.ScientificNumberFormatter;
import android.icu.util.ULocale;

/**
 * @author rocketman
 *
 */
public class ScientificNumberFormatterTest extends TestFmwk {
    @Test
    public void TestBasic() {
        ScientificNumberFormatter markup = ScientificNumberFormatter.getMarkupInstance(
                ULocale.ENGLISH, "<sup>", "</sup>");
        ScientificNumberFormatter superscript = ScientificNumberFormatter.getSuperscriptInstance(ULocale.ENGLISH);
        assertEquals(
                "toMarkupExponentDigits",
                "1.23456×10<sup>-78</sup>",
                markup.format(1.23456e-78));
        assertEquals(
                "toSuperscriptExponentDigits",
                "1.23456×10⁻⁷⁸",
                superscript.format(1.23456e-78));
    }
    
    
    @Test
    public void TestFarsi() {
        ScientificNumberFormatter fmt = ScientificNumberFormatter.getMarkupInstance(
                new ULocale("fa"), "<sup>", "</sup>");
        assertEquals(
                "",
                "۱٫۲۳۴۵۶×۱۰<sup>‎−۷۸</sup>",
                fmt.format(1.23456e-78));
    }


    @Test
    public void TestPlusSignInExponentMarkup() {
        DecimalFormat decfmt = (DecimalFormat) NumberFormat.getScientificInstance(ULocale.ENGLISH);
        decfmt.applyPattern("0.00E+0");
        ScientificNumberFormatter fmt = ScientificNumberFormatter.getMarkupInstance(
                decfmt, "<sup>", "</sup>");
                
        assertEquals(
                "",
                "6.02×10<sup>+23</sup>",
                fmt.format(6.02e23));
    }

    
    @Test
    public void TestPlusSignInExponentSuperscript() {
        DecimalFormat decfmt = (DecimalFormat) NumberFormat.getScientificInstance(ULocale.ENGLISH);
        decfmt.applyPattern("0.00E+0");
        ScientificNumberFormatter fmt = ScientificNumberFormatter.getSuperscriptInstance(
                decfmt);
        assertEquals(
                "",
                "6.02×10⁺²³",
                fmt.format(6.02e23));
    }
    
    @Test
    public void TestFixedDecimalMarkup() {
        DecimalFormat decfmt = (DecimalFormat) NumberFormat.getInstance(ULocale.ENGLISH);
        ScientificNumberFormatter fmt = ScientificNumberFormatter.getMarkupInstance(
                decfmt, "<sup>", "</sup>");
        assertEquals(
                "",
                "123,456",
                fmt.format(123456.0));
    }
    
    @Test
    public void TestFixedDecimalSuperscript() {
        DecimalFormat decfmt = (DecimalFormat) NumberFormat.getInstance(ULocale.ENGLISH);
        ScientificNumberFormatter fmt = ScientificNumberFormatter.getSuperscriptInstance(decfmt);
        assertEquals(
                "",
                "123,456",
                fmt.format(123456.0));
    }
}
