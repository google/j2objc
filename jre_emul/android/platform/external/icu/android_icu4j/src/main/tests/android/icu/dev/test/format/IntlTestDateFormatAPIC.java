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
 * Port From:   ICU4C v1.8.1 : format : IntlTestDateFormatAPI
 * Source File: $ICU4CRoot/source/test/intltest/dtfmapts.cpp
 **/

package android.icu.dev.test.format;

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Date;

import org.junit.Test;

import android.icu.text.DateFormat;
import android.icu.text.DecimalFormat;
import android.icu.text.NumberFormat;
import android.icu.text.SimpleDateFormat;

/*
 * This is an API test, not a unit test.  It doesn't test very many cases, and doesn't
 * try to test the full functionality.  It just calls each function in the class and
 * verifies that it works on a basic level.
 */
public class IntlTestDateFormatAPIC extends android.icu.dev.test.TestFmwk {
    /**
     * Test hiding of parse() and format() APIs in the Format hierarchy.
     * We test the entire hierarchy, even though this test is located in
     * the DateFormat API test.
     */
    @Test
    public void TestNameHiding() {
    
        // N.B.: This test passes if it COMPILES, since it's a test of
        // compile-time name hiding.
    
        Date dateObj = new Date(0);
        Number numObj = new Double(3.1415926535897932384626433832795);
        StringBuffer strBuffer = new StringBuffer("");
        String str;
        FieldPosition fpos = new FieldPosition(0);
        ParsePosition ppos = new ParsePosition(0);
    
        // DateFormat calling Format API
        {
            logln("DateFormat");
            DateFormat dateFmt = DateFormat.getInstance();
            if (dateFmt != null) {
                str = dateFmt.format(dateObj);
                strBuffer = dateFmt.format(dateObj, strBuffer, fpos);
            } else {
                errln("FAIL: Can't create DateFormat");
            }
        }
    
        // SimpleDateFormat calling Format & DateFormat API
        {
            logln("SimpleDateFormat");
            SimpleDateFormat sdf = new SimpleDateFormat();
            // Format API
            str = sdf.format(dateObj);
            strBuffer = sdf.format(dateObj, strBuffer, fpos);
            // DateFormat API
            strBuffer = sdf.format(new Date(0), strBuffer, fpos);
            str = sdf.format(new Date(0));
            try {
                sdf.parse(str);
                sdf.parse(str, ppos);
            } catch (java.text.ParseException pe) {
                System.out.println(pe);
            }
        }
    
        // NumberFormat calling Format API
        {
            logln("NumberFormat");
            NumberFormat fmt = NumberFormat.getInstance();
            if (fmt != null) {
                str = fmt.format(numObj);
                strBuffer = fmt.format(numObj, strBuffer, fpos);
            } else {
                errln("FAIL: Can't create NumberFormat");
            }
        }
    
        // DecimalFormat calling Format & NumberFormat API
        {
            logln("DecimalFormat");
            DecimalFormat fmt = new DecimalFormat();
            // Format API
            str = fmt.format(numObj);
            strBuffer = fmt.format(numObj, strBuffer, fpos);
            // NumberFormat API
            str = fmt.format(2.71828);
            str = fmt.format(1234567);
            strBuffer = fmt.format(1.41421, strBuffer, fpos);
            strBuffer = fmt.format(9876543, strBuffer, fpos);
            Number obj = fmt.parse(str, ppos);
            try {
                obj = fmt.parse(str);
                if(obj==null){
                    errln("FAIL: The format object could not parse the string : "+str);
                }
            } catch (java.text.ParseException pe) {
                System.out.println(pe);
            }
        }
        
        //ICU4J have not the classes ChoiceFormat and MessageFormat
        /*
        // ChoiceFormat calling Format & NumberFormat API
        {
            logln("ChoiceFormat");
            ChoiceFormat fmt = new ChoiceFormat("0#foo|1#foos|2#foos");
            // Format API
            str = fmt.format(numObj);
            strBuffer = fmt.format(numObj, strBuffer, fpos);
            // NumberFormat API
            str = fmt.format(2.71828);
            str = fmt.format(1234567);
            strBuffer = fmt.format(1.41421, strBuffer, fpos);
            strBuffer = fmt.format(9876543, strBuffer, fpos);
            Number obj = fmt.parse(str, ppos);
            try {
                obj = fmt.parse(str);
            } catch (java.text.ParseException pe) {
                System.out.println(pe);
            }
        }
    
        
        // MessageFormat calling Format API
        {
            logln("MessageFormat");
            MessageFormat fmt = new MessageFormat("");
            // Format API
            // We use dateObj, which MessageFormat should reject.
            // We're testing name hiding, not the format method.
            try {
                str = fmt.format(dateObj);
            } catch (Exception e) {
                //e.printStackTrace();
            }
            try {
                strBuffer = fmt.format(dateObj, strBuffer, fpos);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        */
    }
}