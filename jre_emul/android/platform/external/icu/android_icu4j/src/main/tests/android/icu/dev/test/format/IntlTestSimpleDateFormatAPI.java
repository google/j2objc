/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*****************************************************************************************
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996-2012 - All Rights Reserved
 *
 *   The original version of this source code and documentation is copyrighted and
 * owned by Taligent, Inc., a wholly-owned subsidiary of IBM. These materials are
 * provided under terms of a License Agreement between Taligent and Sun. This
 * technology is protected by multiple US and International patents. This notice and
 * attribution to Taligent may not be removed.
 *   Taligent is a registered trademark of Taligent, Inc.
 **/

/** 
 * Port From:   JDK 1.4b1 : java.text.Format.IntlTestSimpleDateFormatAPI
 * Source File: java/text/format/IntlTestSimpleDateFormatAPI.java
 **/
 
package android.icu.dev.test.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Locale;

import org.junit.Test;

import android.icu.text.DateFormatSymbols;
import android.icu.text.SimpleDateFormat;

/**
* @test 1.4 98/03/06
* @summary test International Simple Date Format API
*/
public class IntlTestSimpleDateFormatAPI extends android.icu.dev.test.TestFmwk
{
    // This test checks various generic API methods in DecimalFormat to achieve 100% API coverage.
    @Test
    public void TestAPI()
    {
        logln("SimpleDateFormat API test---"); logln("");

        Locale.setDefault(Locale.ENGLISH);

        // ======= Test constructors

        logln("Testing SimpleDateFormat constructors");

        SimpleDateFormat def = new SimpleDateFormat();

        final String pattern = new String("yyyy.MM.dd G 'at' hh:mm:ss z");
        SimpleDateFormat pat = new SimpleDateFormat(pattern);

        SimpleDateFormat pat_fr = new SimpleDateFormat(pattern, Locale.FRENCH);

        DateFormatSymbols symbols = new DateFormatSymbols(Locale.FRENCH);

        SimpleDateFormat cust1 = new SimpleDateFormat(pattern, symbols);

        // ======= Test clone() and equality

        logln("Testing clone(), assignment and equality operators");

        Format clone = (Format) def.clone();
        if( ! clone.equals(def) ) {
            errln("ERROR: Format clone or equals failed");
        }

        // ======= Test various format() methods

        logln("Testing various format() methods");

        Date d = new Date((long)837039928046.0);

        StringBuffer res1 = new StringBuffer();
        StringBuffer res2 = new StringBuffer();
        FieldPosition pos1 = new FieldPosition(0);
        FieldPosition pos2 = new FieldPosition(0);

        res1 = def.format(d, res1, pos1);
        logln( "" + d.getTime() + " formatted to " + res1);

        res2 = cust1.format(d, res2, pos2);
        logln("" + d.getTime() + " formatted to " + res2);

        // ======= Test parse()

        logln("Testing parse()");

        String text = new String("02/03/76, 2:50 AM, CST");
        Date result1 = new Date();
        Date result2 = new Date();
        ParsePosition pos= new ParsePosition(0);
        result1 = def.parse(text, pos);
        logln(text + " parsed into " + result1);

        try {
            result2 = def.parse(text);
        }
        catch (ParseException e) {
            errln("ERROR: parse() failed");
        }
        logln(text + " parsed into " + result2);

        // ======= Test getters and setters

        logln("Testing getters and setters");

        final DateFormatSymbols syms = pat.getDateFormatSymbols();
        def.setDateFormatSymbols(syms);
        pat_fr.setDateFormatSymbols(syms);
        if( ! pat.getDateFormatSymbols().equals(def.getDateFormatSymbols()) ) {
            errln("ERROR: set DateFormatSymbols() failed");
        }

        /*
        DateFormatSymbols has not the method getTwoDigitStartDate();
        //Date startDate = null; //The variable is never used
        try {
//            startDate = pat.getTwoDigitStartDate();
        }
        catch (Exception e) {
            errln("ERROR: getTwoDigitStartDate() failed");
        }

        try {
//            pat_fr.setTwoDigitStartDate(startDate);
        }
        catch (Exception e) {
            errln("ERROR: setTwoDigitStartDate() failed");
        }*/

        // ======= Test applyPattern()

        logln("Testing applyPattern()");

        String p1 = new String("yyyy.MM.dd G 'at' hh:mm:ss z");
        logln("Applying pattern " + p1);
        pat.applyPattern(p1);

        String s2 = pat.toPattern();
        logln("Extracted pattern is " + s2);
        if( ! s2.equals(p1) ) {
            errln("ERROR: toPattern() result did not match pattern applied");
        }

        logln("Applying pattern " + p1);
        pat.applyLocalizedPattern(p1);
        String s3 = pat.toLocalizedPattern();
        logln("Extracted pattern is " + s3);
        if( ! s3.equals(p1) ) {
            errln("ERROR: toLocalizedPattern() result did not match pattern applied");
        }
        
        // ======= Test for Ticket 5684 (Parsing patterns with 'Y' and 'e'
        logln("Testing parse()");

        String p2 = new String("YYYY'W'wwe");
        logln("Applying pattern " + p2);
        pat.applyPattern(p2);
        Date dt = pat.parse("2007W014", new ParsePosition(0));
        if (dt == null) {
            errln("ERROR: Parsing failed using 'Y' and 'e'");
        }


        // ======= Test getStaticClassID()

//        logln("Testing instanceof");

//        try {
//            DateFormat test = new SimpleDateFormat();

//            if (! (test instanceof SimpleDateFormat)) {
//                errln("ERROR: instanceof failed");
//            }
//        }
//        catch (Exception e) {
//            errln("ERROR: Couldn't create a SimpleDateFormat");
//        }
    }
    
    // Jitterbug 4451, for coverage
    @Test
    public void TestCoverage(){
        class StubDateFormat extends SimpleDateFormat{
            /**
             * For serialization
             */
            private static final long serialVersionUID = 8460897119491427934L;

            public void run(){
                if (!zeroPaddingNumber(12, 4, 6).equals("0012")){
                    errln("SimpleDateFormat(zeroPaddingNumber(long , int , int )");
                }
            }
        }
        new StubDateFormat().run();
    }
}
