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
 * Port From:   ICU4C v1.8.1 : format : IntlTestDecimalFormatSymbols
 * Source File: $ICU4CRoot/source/test/intltest/tsdcfmsy.cpp
 **/

package android.icu.dev.test.format;

import java.text.FieldPosition;
import java.util.Locale;

import org.junit.Test;

import android.icu.text.DecimalFormat;
import android.icu.text.DecimalFormatSymbols;

/**
 * Tests for DecimalFormatSymbols
 **/
public class IntlTestDecimalFormatSymbolsC extends android.icu.dev.test.TestFmwk {
    /**
     * Test the API of DecimalFormatSymbols; primarily a simple get/set set.
     */
    @Test
    public void TestSymbols() {    
        DecimalFormatSymbols fr = new DecimalFormatSymbols(Locale.FRENCH);    
        DecimalFormatSymbols en = new DecimalFormatSymbols(Locale.ENGLISH);
    
        if (en.equals(fr)) {
            errln("ERROR: English DecimalFormatSymbols equal to French");
        }
    
        // just do some VERY basic tests to make sure that get/set work
    
        char zero = en.getZeroDigit();
        fr.setZeroDigit(zero);
        if (fr.getZeroDigit() != en.getZeroDigit()) {
            errln("ERROR: get/set ZeroDigit failed");
        }
    
        char group = en.getGroupingSeparator();
        fr.setGroupingSeparator(group);
        if (fr.getGroupingSeparator() != en.getGroupingSeparator()) {
            errln("ERROR: get/set GroupingSeparator failed");
        }
    
        char decimal = en.getDecimalSeparator();
        fr.setDecimalSeparator(decimal);
        if (fr.getDecimalSeparator() != en.getDecimalSeparator()) {
            errln("ERROR: get/set DecimalSeparator failed");
        }
    
        char perMill = en.getPerMill();
        fr.setPerMill(perMill);
        if (fr.getPerMill() != en.getPerMill()) {
            errln("ERROR: get/set PerMill failed");
        }
    
        char percent = en.getPercent();
        fr.setPercent(percent);
        if (fr.getPercent() != en.getPercent()) {
            errln("ERROR: get/set Percent failed");
        }
    
        char digit = en.getDigit();
        fr.setDigit(digit);
        if (fr.getPercent() != en.getPercent()) {
            errln("ERROR: get/set Percent failed");
        }
    
        char patternSeparator = en.getPatternSeparator();
        fr.setPatternSeparator(patternSeparator);
        if (fr.getPatternSeparator() != en.getPatternSeparator()) {
            errln("ERROR: get/set PatternSeparator failed");
        }
    
        String infinity = en.getInfinity();
        fr.setInfinity(infinity);
        String infinity2 = fr.getInfinity();
        if (!infinity.equals(infinity2)) {
            errln("ERROR: get/set Infinity failed");
        }
    
        String nan = en.getNaN();
        fr.setNaN(nan);
        String nan2 = fr.getNaN();
        if (!nan.equals(nan2)) {
            errln("ERROR: get/set NaN failed");
        }
    
        char minusSign = en.getMinusSign();
        fr.setMinusSign(minusSign);
        if (fr.getMinusSign() != en.getMinusSign()) {
            errln("ERROR: get/set MinusSign failed");
        }
    
        //        char exponential = en.getExponentialSymbol();
        //        fr.setExponentialSymbol(exponential);
        //        if(fr.getExponentialSymbol() != en.getExponentialSymbol()) {
        //            errln("ERROR: get/set Exponential failed");
        //        }
    
        //DecimalFormatSymbols foo = new DecimalFormatSymbols(); //The variable is never used
    
        en = (DecimalFormatSymbols) fr.clone();
    
        if (!en.equals(fr)) {
            errln("ERROR: Clone failed");
        }
        
        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.US);
    
        verify(34.5, "00.00", sym, "34.50");
        sym.setDecimalSeparator('S');
        verify(34.5, "00.00", sym, "34S50");
        sym.setPercent('P');
        verify(34.5, "00 %", sym, "3450 P");
        sym.setCurrencySymbol("D");
        verify(34.5, "\u00a4##.##", sym, "D34.5");
        sym.setGroupingSeparator('|');
        verify(3456.5, "0,000.##", sym, "3|456S5");
    }
    
    /** helper functions**/
    public void verify(double value, String pattern, DecimalFormatSymbols sym, String expected) {
        DecimalFormat df = new DecimalFormat(pattern, sym);
        StringBuffer buffer = new StringBuffer("");
        FieldPosition pos = new FieldPosition(-1);
        buffer = df.format(value, buffer, pos);
        if(!buffer.toString().equals(expected)){
            errln("ERROR: format failed after setSymbols()\n Expected" + 
                expected + ", Got " + buffer);
        }
    }
}