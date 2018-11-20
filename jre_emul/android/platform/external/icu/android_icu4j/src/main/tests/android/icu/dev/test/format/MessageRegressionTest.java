/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
**********************************************************************
* Copyright (c) 2005-2011, International Business Machines
* Corporation and others.  All Rights Reserved.
**********************************************************************
* Author: Alan Liu
* Created: April 12, 2004
* Since: ICU 3.0
**********************************************************************
*/
/**
 * MessageRegressionTest.java
 *
 * @test 1.29 01/03/12
 * @bug 4031438 4058973 4074764 4094906 4104976 4105380 4106659 4106660 4106661
 * 4111739 4112104 4113018 4114739 4114743 4116444 4118592 4118594 4120552
 * 4142938 4169959 4232154 4293229
 * @summary Regression tests for MessageFormat and associated classes
 */
/*
(C) Copyright Taligent, Inc. 1996 - All Rights Reserved
(C) Copyright IBM Corp. 1996 - All Rights Reserved

  The original version of this source code and documentation is copyrighted and
owned by Taligent, Inc., a wholly-owned subsidiary of IBM. These materials are
provided under terms of a License Agreement between Taligent and Sun. This
technology is protected by multiple US and International patents. This notice and
attribution to Taligent may not be removed.
  Taligent is a registered trademark of Taligent, Inc.
*/
package android.icu.dev.test.format;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ChoiceFormat;
import java.text.ParsePosition;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;

import android.icu.text.MessageFormat;
import android.icu.text.NumberFormat;
import android.icu.util.ULocale;

public class MessageRegressionTest extends android.icu.dev.test.TestFmwk {
    /* @bug 4074764
     * Null exception when formatting pattern with MessageFormat
     * with no parameters.
     */
    @Test
    public void Test4074764() {
        String[] pattern = {"Message without param",
        "Message with param:{0}",
        "Longer Message with param {0}"};
        //difference between the two param strings are that
        //in the first one, the param position is within the
        //length of the string without param while it is not so
        //in the other case.

        MessageFormat messageFormatter = new MessageFormat("");

        try {
            //Apply pattern with param and print the result
            messageFormatter.applyPattern(pattern[1]);
            Object[] paramArray = {new String("BUG"), new Date()};
            String tempBuffer = messageFormatter.format(paramArray);
            if (!tempBuffer.equals("Message with param:BUG"))
                errln("MessageFormat with one param test failed.");
            logln("Formatted with one extra param : " + tempBuffer);

            //Apply pattern without param and print the result
            messageFormatter.applyPattern(pattern[0]);
            tempBuffer = messageFormatter.format(null);
            if (!tempBuffer.equals("Message without param"))
                errln("MessageFormat with no param test failed.");
            logln("Formatted with no params : " + tempBuffer);

             tempBuffer = messageFormatter.format(paramArray);
             if (!tempBuffer.equals("Message without param"))
                errln("Formatted with arguments > subsitution failed. result = " + tempBuffer.toString());
             logln("Formatted with extra params : " + tempBuffer);
            //This statement gives an exception while formatting...
            //If we use pattern[1] for the message with param,
            //we get an NullPointerException in MessageFormat.java(617)
            //If we use pattern[2] for the message with param,
            //we get an StringArrayIndexOutOfBoundsException in MessageFormat.java(614)
            //Both are due to maxOffset not being reset to -1
            //in applyPattern() when the pattern does not
            //contain any param.
        } catch (Exception foo) {
            errln("Exception when formatting with no params.");
        }
    }

    /* @bug 4058973
     * MessageFormat.toPattern has weird rounding behavior.
     *
     * ICU 4.8: This test is commented out because toPattern() has been changed to return
     * the original pattern string, rather than reconstituting a new (equivalent) one.
     * This trivially eliminates issues with rounding or any other pattern string differences.
     */
    /*public void Test4058973() {

        MessageFormat fmt = new MessageFormat("{0,choice,0#no files|1#one file|1< {0,number,integer} files}");
        String pat = fmt.toPattern();
        if (!pat.equals("{0,choice,0.0#no files|1.0#one file|1.0< {0,number,integer} files}")) {
            errln("MessageFormat.toPattern failed");
        }
    }*/
    /* @bug 4031438
     * More robust message formats.
     */
    @Test
    public void Test4031438() {
        String pattern1 = "Impossible {1} has occurred -- status code is {0} and message is {2}.";
        String pattern2 = "Double '' Quotes {0} test and quoted '{1}' test plus 'other {2} stuff'.";

        MessageFormat messageFormatter = new MessageFormat("");

        try {
            logln("Apply with pattern : " + pattern1);
            messageFormatter.applyPattern(pattern1);
            Object[] paramArray = {new Integer(7)};
            String tempBuffer = messageFormatter.format(paramArray);
            if (!tempBuffer.equals("Impossible {1} has occurred -- status code is 7 and message is {2}."))
                errln("Tests arguments < substitution failed");
            logln("Formatted with 7 : " + tempBuffer);
            ParsePosition status = new ParsePosition(0);
            Object[] objs = messageFormatter.parse(tempBuffer, status);
            if (objs[paramArray.length] != null)
                errln("Parse failed with more than expected arguments");
            for (int i = 0; i < objs.length; i++) {
                if (objs[i] != null && !objs[i].toString().equals(paramArray[i].toString())) {
                    errln("Parse failed on object " + objs[i] + " at index : " + i);
                }
            }
            tempBuffer = messageFormatter.format(null);
            if (!tempBuffer.equals("Impossible {1} has occurred -- status code is {0} and message is {2}."))
                errln("Tests with no arguments failed");
            logln("Formatted with null : " + tempBuffer);
            logln("Apply with pattern : " + pattern2);
            messageFormatter.applyPattern(pattern2);
            tempBuffer = messageFormatter.format(paramArray);
            if (!tempBuffer.equals("Double ' Quotes 7 test and quoted {1} test plus 'other {2} stuff'."))
                errln("quote format test (w/ params) failed.");
            logln("Formatted with params : " + tempBuffer);
            tempBuffer = messageFormatter.format(null);
            if (!tempBuffer.equals("Double ' Quotes {0} test and quoted {1} test plus 'other {2} stuff'."))
                errln("quote format test (w/ null) failed.");
            logln("Formatted with null : " + tempBuffer);
            logln("toPattern : " + messageFormatter.toPattern());
        } catch (Exception foo) {
            warnln("Exception when formatting in bug 4031438. "+foo.getMessage());
        }
    }
    @Test
    public void Test4052223()
    {
        ParsePosition pos = new ParsePosition(0);
        if (pos.getErrorIndex() != -1) {
            errln("ParsePosition.getErrorIndex initialization failed.");
        }
        MessageFormat fmt = new MessageFormat("There are {0} apples growing on the {1} tree.");
        String str = new String("There is one apple growing on the peach tree.");
        Object[] objs = fmt.parse(str, pos);
        logln("unparsable string , should fail at " + pos.getErrorIndex());
        if (pos.getErrorIndex() == -1)
            errln("Bug 4052223 failed : parsing string " + str);
        pos.setErrorIndex(4);
        if (pos.getErrorIndex() != 4)
            errln("setErrorIndex failed, got " + pos.getErrorIndex() + " instead of 4");
        
        if (objs != null) {
            errln("objs should be null");
        }
        ChoiceFormat f = new ChoiceFormat(
            "-1#are negative|0#are no or fraction|1#is one|1.0<is 1+|2#are two|2<are more than 2.");
        pos.setIndex(0); pos.setErrorIndex(-1);
        Number obj = f.parse("are negative", pos);
        if (pos.getErrorIndex() != -1 && obj.doubleValue() == -1.0)
            errln("Parse with \"are negative\" failed, at " + pos.getErrorIndex());
        pos.setIndex(0); pos.setErrorIndex(-1);
        obj = f.parse("are no or fraction ", pos);
        if (pos.getErrorIndex() != -1 && obj.doubleValue() == 0.0)
            errln("Parse with \"are no or fraction\" failed, at " + pos.getErrorIndex());
        pos.setIndex(0); pos.setErrorIndex(-1);
        obj = f.parse("go postal", pos);
        if (pos.getErrorIndex() == -1 && !Double.isNaN(obj.doubleValue()))
            errln("Parse with \"go postal\" failed, at " + pos.getErrorIndex());
    }
    /* @bug 4104976
     * ChoiceFormat.equals(null) throws NullPointerException
     */
    @Test
    public void Test4104976()
    {
        double[] limits = {1, 20};
        String[] formats = {"xyz", "abc"};
        ChoiceFormat cf = new ChoiceFormat(limits, formats);
        try {
            log("Compares to null is always false, returned : ");
            logln(cf.equals(null) ? "TRUE" : "FALSE");
        } catch (Exception foo) {
            errln("ChoiceFormat.equals(null) throws exception.");
        }
    }
    /* @bug 4106659
     * ChoiceFormat.ctor(double[], String[]) doesn't check
     * whether lengths of input arrays are equal.
     */
    @Test
    public void Test4106659()
    {
        double[] limits = {1, 2, 3};
        String[] formats = {"one", "two"};
        ChoiceFormat cf = null;
        try {
            cf = new ChoiceFormat(limits, formats);
        } catch (Exception foo) {
            logln("ChoiceFormat constructor should check for the array lengths");
            cf = null;
        }
        if (cf != null) errln(cf.format(5));
    }

    /* @bug 4106660
     * ChoiceFormat.ctor(double[], String[]) allows unordered double array.
     * This is not a bug, added javadoc to emphasize the use of limit
     * array must be in ascending order.
     */
    @Test
    public void Test4106660()
    {
        double[] limits = {3, 1, 2};
        String[] formats = {"Three", "One", "Two"};
        ChoiceFormat cf = new ChoiceFormat(limits, formats);
        double d = 5.0;
        String str = cf.format(d);
        if (!str.equals("Two"))
            errln("format(" + d + ") = " + cf.format(d));
    }

    /* @bug 4111739
     * MessageFormat is incorrectly serialized/deserialized.
     */
    @Test
    public void Test4111739()
    {
        MessageFormat format1 = null;
        MessageFormat format2 = null;
        ObjectOutputStream ostream = null;
        ByteArrayOutputStream baos = null;
        ObjectInputStream istream = null;

        try {
            baos = new ByteArrayOutputStream();
            ostream = new ObjectOutputStream(baos);
        } catch(IOException e) {
            errln("Unexpected exception : " + e.getMessage());
            return;
        }

        try {
            format1 = new MessageFormat("pattern{0}");
            ostream.writeObject(format1);
            ostream.flush();

            byte bytes[] = baos.toByteArray();

            istream = new ObjectInputStream(new ByteArrayInputStream(bytes));
            format2 = (MessageFormat)istream.readObject();
        } catch(Exception e) {
            errln("Unexpected exception : " + e.getMessage());
        }

        if (!format1.equals(format2)) {
            errln("MessageFormats before and after serialization are not" +
                " equal\nformat1 = " + format1 + "(" + format1.toPattern() + ")\nformat2 = " +
                format2 + "(" + format2.toPattern() + ")");
        } else {
            logln("Serialization for MessageFormat is OK.");
        }
    }
    /* @bug 4114743
     * MessageFormat.applyPattern allows illegal patterns.
     */
    @Test
    public void Test4114743()
    {
        String originalPattern = "initial pattern";
        MessageFormat mf = new MessageFormat(originalPattern);
        String illegalPattern = "ab { '}' de";
        try {
            mf.applyPattern(illegalPattern);
            errln("illegal pattern: \"" + illegalPattern + "\"");
        } catch (IllegalArgumentException foo) {
            if (illegalPattern.equals(mf.toPattern()))
                errln("pattern after: \"" + mf.toPattern() + "\"");
        }
    }

    /* @bug 4116444
     * MessageFormat.parse has different behavior in case of null.
     */
    @Test
    public void Test4116444()
    {
        String[] patterns = {"", "one", "{0,date,short}"};
        MessageFormat mf = new MessageFormat("");

        for (int i = 0; i < patterns.length; i++) {
            String pattern = patterns[i];
            mf.applyPattern(pattern);
            try {
                Object[] array = mf.parse(null, new ParsePosition(0));
                logln("pattern: \"" + pattern + "\"");
                log(" parsedObjects: ");
                if (array != null) {
                    log("{");
                    for (int j = 0; j < array.length; j++) {
                        if (array[j] != null)
                            err("\"" + array[j].toString() + "\"");
                        else
                            log("null");
                        if (j < array.length - 1) log(",");
                    }
                    log("}") ;
                } else {
                    log("null");
                }
                logln("");
            } catch (Exception e) {
                errln("pattern: \"" + pattern + "\"");
                errln("  Exception: " + e.getMessage());
            }
        }

    }
    /* @bug 4114739 (FIX and add javadoc)
     * MessageFormat.format has undocumented behavior about empty format objects.
     */
    @Test
    public void Test4114739()
    {

        MessageFormat mf = new MessageFormat("<{0}>");
        Object[] objs1 = null;
        Object[] objs2 = {};
        Object[] objs3 = {null};
        try {
            logln("pattern: \"" + mf.toPattern() + "\"");
            log("format(null) : ");
            logln("\"" + mf.format(objs1) + "\"");
            log("format({})   : ");
            logln("\"" + mf.format(objs2) + "\"");
            log("format({null}) :");
            logln("\"" + mf.format(objs3) + "\"");
        } catch (Exception e) {
            errln("Exception thrown for null argument tests.");
        }
    }

    /* @bug 4113018
     * MessageFormat.applyPattern works wrong with illegal patterns.
     */
    @Test
    public void Test4113018()
    {
        String originalPattern = "initial pattern";
        MessageFormat mf = new MessageFormat(originalPattern);
        String illegalPattern = "format: {0, xxxYYY}";
        logln("pattern before: \"" + mf.toPattern() + "\"");
        logln("illegal pattern: \"" + illegalPattern + "\"");
        try {
            mf.applyPattern(illegalPattern);
            errln("Should have thrown IllegalArgumentException for pattern : " + illegalPattern);
        } catch (IllegalArgumentException e) {
            if (illegalPattern.equals(mf.toPattern()))
                errln("pattern after: \"" + mf.toPattern() + "\"");
        }
    }
    /* @bug 4106661
     * ChoiceFormat is silent about the pattern usage in javadoc.
     */
    @Test
    public void Test4106661()
    {
        ChoiceFormat fmt = new ChoiceFormat(
          "-1#are negative| 0#are no or fraction | 1#is one |1.0<is 1+ |2#are two |2<are more than 2.");
        logln("Formatter Pattern : " + fmt.toPattern());

        logln("Format with -INF : " + fmt.format(Double.NEGATIVE_INFINITY));
        logln("Format with -1.0 : " + fmt.format(-1.0));
        logln("Format with 0 : " + fmt.format(0));
        logln("Format with 0.9 : " + fmt.format(0.9));
        logln("Format with 1.0 : " + fmt.format(1));
        logln("Format with 1.5 : " + fmt.format(1.5));
        logln("Format with 2 : " + fmt.format(2));
        logln("Format with 2.1 : " + fmt.format(2.1));
        logln("Format with NaN : " + fmt.format(Double.NaN));
        logln("Format with +INF : " + fmt.format(Double.POSITIVE_INFINITY));
    }
    /* @bug 4094906
     * ChoiceFormat should accept \u221E as eq. to INF.
     */
    @Test
    public void Test4094906()
    {
        ChoiceFormat fmt = new ChoiceFormat(
          "-\u221E<are negative|0<are no or fraction|1#is one|1.0<is 1+|\u221E<are many.");
        if (!fmt.toPattern().startsWith("-\u221E<are negative|0.0<are no or fraction|1.0#is one|1.0<is 1+|\u221E<are many."))
            errln("Formatter Pattern : " + fmt.toPattern());
        logln("Format with -INF : " + fmt.format(Double.NEGATIVE_INFINITY));
        logln("Format with -1.0 : " + fmt.format(-1.0));
        logln("Format with 0 : " + fmt.format(0));
        logln("Format with 0.9 : " + fmt.format(0.9));
        logln("Format with 1.0 : " + fmt.format(1));
        logln("Format with 1.5 : " + fmt.format(1.5));
        logln("Format with 2 : " + fmt.format(2));
        logln("Format with +INF : " + fmt.format(Double.POSITIVE_INFINITY));
    }

    /* @bug 4118592
     * MessageFormat.parse fails with ChoiceFormat.
     */
    @Test
    public void Test4118592()
    {
        MessageFormat mf = new MessageFormat("");
        String pattern = "{0,choice,1#YES|2#NO}";
        String prefix = "";
        for (int i = 0; i < 5; i++) {
            String formatted = prefix + "YES";
            mf.applyPattern(prefix + pattern);
            prefix += "x";
            Object[] objs = mf.parse(formatted, new ParsePosition(0));
            logln(i + ". pattern :\"" + mf.toPattern() + "\"");
            log(" \"" + formatted + "\" parsed as ");
            if (objs == null) logln("  null");
            else logln("  " + objs[0]);
        }
    }
    /* @bug 4118594
     * MessageFormat.parse fails for some patterns.
     */
    @Test
    public void Test4118594()
    {
        MessageFormat mf = new MessageFormat("{0}, {0}, {0}");
        String forParsing = "x, y, z";
        Object[] objs = mf.parse(forParsing, new ParsePosition(0));
        logln("pattern: \"" + mf.toPattern() + "\"");
        logln("text for parsing: \"" + forParsing + "\"");
        if (!objs[0].toString().equals("z"))
            errln("argument0: \"" + objs[0] + "\"");
        mf.setLocale(Locale.US);
        mf.applyPattern("{0,number,#.##}, {0,number,#.#}");
        Object[] oldobjs = {new Double(3.1415)};
        String result = mf.format( oldobjs );
        logln("pattern: \"" + mf.toPattern() + "\"");
        logln("text for parsing: \"" + result + "\"");
        // result now equals "3.14, 3.1"
        if (!result.equals("3.14, 3.1"))
            errln("result = " + result);
        Object[] newobjs = mf.parse(result, new ParsePosition(0));
        // newobjs now equals {new Double(3.1)}
        if (((Number)newobjs[0]).doubleValue() != 3.1) // was (Double) [alan]
            errln( "newobjs[0] = " + newobjs[0]);
    }
    /* @bug 4105380
     * When using ChoiceFormat, MessageFormat is not good for I18n.
     */
    @Test
    public void Test4105380()
    {
        String patternText1 = "The disk \"{1}\" contains {0}.";
        String patternText2 = "There are {0} on the disk \"{1}\"";
        MessageFormat form1 = new MessageFormat(patternText1);
        MessageFormat form2 = new MessageFormat(patternText2);
        double[] filelimits = {0,1,2};
        String[] filepart = {"no files","one file","{0,number} files"};
        ChoiceFormat fileform = new ChoiceFormat(filelimits, filepart);
        form1.setFormat(1, fileform);
        form2.setFormat(0, fileform);
        Object[] testArgs = {new Long(12373), "MyDisk"};
        logln(form1.format(testArgs));
        logln(form2.format(testArgs));
    }
    /* @bug 4120552
     * MessageFormat.parse incorrectly sets errorIndex.
     */
    @Test
    public void Test4120552()
    {
        MessageFormat mf = new MessageFormat("pattern");
        String texts[] = {"pattern", "pat", "1234"};
        logln("pattern: \"" + mf.toPattern() + "\"");
        for (int i = 0; i < texts.length; i++) {
            ParsePosition pp = new ParsePosition(0);
            Object[] objs = mf.parse(texts[i], pp);
            log("  text for parsing: \"" + texts[i] + "\"");
            if (objs == null) {
                logln("  (incorrectly formatted string)");
                if (pp.getErrorIndex() == -1)
                    errln("Incorrect error index: " + pp.getErrorIndex());
            } else {
                logln("  (correctly formatted string)");
            }
        }
    }

    /**
     * @bug 4142938
     * MessageFormat handles single quotes in pattern wrong.
     * This is actually a problem in ChoiceFormat; it doesn't
     * understand single quotes.
     */
    @Test
    public void Test4142938() {
        String pat = "''Vous'' {0,choice,0#n''|1#}avez s\u00E9lectionne\u00E9 " + 
            "{0,choice,0#aucun|1#{0}} client{0,choice,0#s|1#|2#s} " + 
            "personnel{0,choice,0#s|1#|2#s}.";
        MessageFormat mf = new MessageFormat(pat);

        String[] PREFIX = {
            "'Vous' n'avez s\u00E9lectionne\u00E9 aucun clients personnels.",
            "'Vous' avez s\u00E9lectionne\u00E9 ",
            "'Vous' avez s\u00E9lectionne\u00E9 "
        };  
        String[] SUFFIX = {
            null,
            " client personnel.",
            " clients personnels."
        };
    
        for (int i=0; i<3; i++) {
            String out = mf.format(new Object[]{new Integer(i)});
            if (SUFFIX[i] == null) {
                if (!out.equals(PREFIX[i]))
                    errln("" + i + ": Got \"" + out + "\"; Want \"" + PREFIX[i] + "\"");
            }
            else {
                if (!out.startsWith(PREFIX[i]) ||
                    !out.endsWith(SUFFIX[i]))
                    errln("" + i + ": Got \"" + out + "\"; Want \"" + PREFIX[i] + "\"...\"" +
                          SUFFIX[i] + "\"");
            }
        }
    }

    /**
     * @bug 4142938
     * Test the applyPattern and toPattern handling of single quotes
     * by ChoiceFormat.  (This is in here because this was a bug reported
     * against MessageFormat.)  The single quote is used to quote the
     * pattern characters '|', '#', '<', and '\u2264'.  Two quotes in a row
     * is a quote literal.
     */
    @Test
    public void TestChoicePatternQuote() {
        String[] DATA = {
            // Pattern                  0 value           1 value
            "0#can''t|1#can",           "can't",          "can",
            "0#'pound(#)=''#'''|1#xyz", "pound(#)='#'",   "xyz",
            "0#'1<2 | 1\u22641'|1#''",  "1<2 | 1\u22641", "'",
        };
        for (int i=0; i<DATA.length; i+=3) {
            try {
                ChoiceFormat cf = new ChoiceFormat(DATA[i]);
                for (int j=0; j<=1; ++j) {
                    String out = cf.format(j);
                    if (!out.equals(DATA[i+1+j]))
                        errln("Fail: Pattern \"" + DATA[i] + "\" x "+j+" -> " +
                              out + "; want \"" + DATA[i+1+j] + '"');
                }
                String pat = cf.toPattern();
                String pat2 = new ChoiceFormat(pat).toPattern();
                if (!pat.equals(pat2))
                    errln("Fail: Pattern \"" + DATA[i] + "\" x toPattern -> \"" + pat + '"');
                else
                    logln("Ok: Pattern \"" + DATA[i] + "\" x toPattern -> \"" + pat + '"');
            }
            catch (IllegalArgumentException e) {
                errln("Fail: Pattern \"" + DATA[i] + "\" -> " + e);
            }
        }
    }

    /**
     * @bug 4112104
     * MessageFormat.equals(null) throws a NullPointerException.  The JLS states
     * that it should return false.
     */
    @Test
    public void Test4112104() {
        MessageFormat format = new MessageFormat("");
        try {
            // This should NOT throw an exception
            if (format.equals(null)) {
                // It also should return false
                errln("MessageFormat.equals(null) returns false");
            }
        }
        catch (NullPointerException e) {
            errln("MessageFormat.equals(null) throws " + e);
        }
    }

    /**
     * @bug 4169959
     * MessageFormat does not format null objects. CANNOT REPRODUCE THIS BUG.
     */
    @Test
    public void Test4169959() {
        // This works
        logln(MessageFormat.format("This will {0}", new Object[]{"work"}));

        // This fails
        logln(MessageFormat.format("This will {0}", new Object[]{ null }));
    }

    @Test
    public void test4232154() {
        boolean gotException = false;
        try {
            new MessageFormat("The date is {0:date}");
        } catch (Exception e) {
            gotException = true;
            if (!(e instanceof IllegalArgumentException)) {
                throw new RuntimeException("got wrong exception type");
            }
            if ("argument number too large at ".equals(e.getMessage())) {
                throw new RuntimeException("got wrong exception message");
            }
        }
        if (!gotException) {
            throw new RuntimeException("didn't get exception for invalid input");
        }
    }
    
    @Test
    public void test4293229() {
        MessageFormat format = new MessageFormat("'''{'0}'' '''{0}'''");
        Object[] args = { null };
        String expected = "'{0}' '{0}'";
        String result = format.format(args);
        if (!result.equals(expected)) {
            throw new RuntimeException("wrong format result - expected \"" +
                    expected + "\", got \"" + result + "\"");
        }
    }
     
    // This test basically ensures that the tests defined above also work with
    // valid named arguments.
    @Test
    public void testBugTestsWithNamesArguments() {
        
      { // Taken from Test4031438().
        String pattern1 = "Impossible {arg1} has occurred -- status code is {arg0} and message is {arg2}.";
        String pattern2 = "Double '' Quotes {ARG_ZERO} test and quoted '{ARG_ONE}' test plus 'other {ARG_TWO} stuff'.";

        MessageFormat messageFormatter = new MessageFormat("");

        try {
            logln("Apply with pattern : " + pattern1);
            messageFormatter.applyPattern(pattern1);
            HashMap paramsMap = new HashMap();
            paramsMap.put("arg0", new Integer(7));
            String tempBuffer = messageFormatter.format(paramsMap);
            if (!tempBuffer.equals("Impossible {arg1} has occurred -- status code is 7 and message is {arg2}."))
                errln("Tests arguments < substitution failed");
            logln("Formatted with 7 : " + tempBuffer);
            ParsePosition status = new ParsePosition(0);
            Map objs = messageFormatter.parseToMap(tempBuffer, status);
            if (objs.get("arg1") != null || objs.get("arg2") != null)
                errln("Parse failed with more than expected arguments");
            for (Iterator keyIter = objs.keySet().iterator();
                 keyIter.hasNext();) {
                String key = (String) keyIter.next();
                if (objs.get(key) != null && !objs.get(key).toString().equals(paramsMap.get(key).toString())) {
                    errln("Parse failed on object " + objs.get(key) + " with argument name : " + key );
                }
            }
            tempBuffer = messageFormatter.format(null);
            if (!tempBuffer.equals("Impossible {arg1} has occurred -- status code is {arg0} and message is {arg2}."))
                errln("Tests with no arguments failed");
            logln("Formatted with null : " + tempBuffer);
            logln("Apply with pattern : " + pattern2);
            messageFormatter.applyPattern(pattern2);
            paramsMap.clear();
            paramsMap.put("ARG_ZERO", new Integer(7));
            tempBuffer = messageFormatter.format(paramsMap);
            if (!tempBuffer.equals("Double ' Quotes 7 test and quoted {ARG_ONE} test plus 'other {ARG_TWO} stuff'."))
                errln("quote format test (w/ params) failed.");
            logln("Formatted with params : " + tempBuffer);
            tempBuffer = messageFormatter.format(null);
            if (!tempBuffer.equals("Double ' Quotes {ARG_ZERO} test and quoted {ARG_ONE} test plus 'other {ARG_TWO} stuff'."))
                errln("quote format test (w/ null) failed.");
            logln("Formatted with null : " + tempBuffer);
            logln("toPattern : " + messageFormatter.toPattern());
        } catch (Exception foo) {
            warnln("Exception when formatting in bug 4031438. "+foo.getMessage());
        }
      }{ // Taken from Test4052223().
        ParsePosition pos = new ParsePosition(0);
        if (pos.getErrorIndex() != -1) {
            errln("ParsePosition.getErrorIndex initialization failed.");
        }
        MessageFormat fmt = new MessageFormat("There are {numberOfApples} apples growing on the {whatKindOfTree} tree.");
        String str = new String("There is one apple growing on the peach tree.");
        Map objs = fmt.parseToMap(str, pos);
        logln("unparsable string , should fail at " + pos.getErrorIndex());
        if (pos.getErrorIndex() == -1)
            errln("Bug 4052223 failed : parsing string " + str);
        pos.setErrorIndex(4);
        if (pos.getErrorIndex() != 4)
            errln("setErrorIndex failed, got " + pos.getErrorIndex() + " instead of 4");
        if (objs != null)
            errln("unparsable string, should return null");
    }{ // Taken from Test4111739().
        MessageFormat format1 = null;
        MessageFormat format2 = null;
        ObjectOutputStream ostream = null;
        ByteArrayOutputStream baos = null;
        ObjectInputStream istream = null;

        try {
            baos = new ByteArrayOutputStream();
            ostream = new ObjectOutputStream(baos);
        } catch(IOException e) {
            errln("Unexpected exception : " + e.getMessage());
            return;
        }

        try {
            format1 = new MessageFormat("pattern{argument}");
            ostream.writeObject(format1);
            ostream.flush();

            byte bytes[] = baos.toByteArray();

            istream = new ObjectInputStream(new ByteArrayInputStream(bytes));
            format2 = (MessageFormat)istream.readObject();
        } catch(Exception e) {
            errln("Unexpected exception : " + e.getMessage());
        }

        if (!format1.equals(format2)) {
            errln("MessageFormats before and after serialization are not" +
                " equal\nformat1 = " + format1 + "(" + format1.toPattern() + ")\nformat2 = " +
                format2 + "(" + format2.toPattern() + ")");
        } else {
            logln("Serialization for MessageFormat is OK.");
        }
    }{ // Taken from Test4116444().
        String[] patterns = {"", "one", "{namedArgument,date,short}"};
        MessageFormat mf = new MessageFormat("");

        for (int i = 0; i < patterns.length; i++) {
            String pattern = patterns[i];
            mf.applyPattern(pattern);
            try {
                Map objs = mf.parseToMap(null, new ParsePosition(0));
                logln("pattern: \"" + pattern + "\"");
                log(" parsedObjects: ");
                if (objs != null) {
                    log("{");
                    for (Iterator keyIter = objs.keySet().iterator();
                         keyIter.hasNext();) {
                        String key = (String)keyIter.next();
                        if (objs.get(key) != null) {
                            err("\"" + objs.get(key).toString() + "\"");
                        } else {
                            log("null");
                        }
                        if (keyIter.hasNext()) {
                            log(",");
                        }
                    }
                    log("}") ;
                } else {
                    log("null");
                }
                logln("");
            } catch (Exception e) {
                errln("pattern: \"" + pattern + "\"");
                errln("  Exception: " + e.getMessage());
            }
        }
    }{ // Taken from Test4114739().
        MessageFormat mf = new MessageFormat("<{arg}>");
        Map objs1 = null;
        Map objs2 = new HashMap();
        Map objs3 = new HashMap();
        objs3.put("arg", null);
        try {
            logln("pattern: \"" + mf.toPattern() + "\"");
            log("format(null) : ");
            logln("\"" + mf.format(objs1) + "\"");
            log("format({})   : ");
            logln("\"" + mf.format(objs2) + "\"");
            log("format({null}) :");
            logln("\"" + mf.format(objs3) + "\"");
        } catch (Exception e) {
            errln("Exception thrown for null argument tests.");
        } 
    }{ // Taken from Test4118594().
        String argName = "something_stupid";
        MessageFormat mf = new MessageFormat("{"+ argName + "}, {" + argName + "}, {" + argName + "}");
        String forParsing = "x, y, z";
        Map objs = mf.parseToMap(forParsing, new ParsePosition(0));
        logln("pattern: \"" + mf.toPattern() + "\"");
        logln("text for parsing: \"" + forParsing + "\"");
        if (!objs.get(argName).toString().equals("z"))
            errln("argument0: \"" + objs.get(argName) + "\"");
        mf.setLocale(Locale.US);
        mf.applyPattern("{" + argName + ",number,#.##}, {" + argName + ",number,#.#}");
        Map oldobjs = new HashMap();
        oldobjs.put(argName, new Double(3.1415));
        String result = mf.format( oldobjs );
        logln("pattern: \"" + mf.toPattern() + "\"");
        logln("text for parsing: \"" + result + "\"");
        // result now equals "3.14, 3.1"
        if (!result.equals("3.14, 3.1"))
            errln("result = " + result);
        Map newobjs = mf.parseToMap(result, new ParsePosition(0));
        // newobjs now equals {new Double(3.1)}
        if (((Number)newobjs.get(argName)).doubleValue() != 3.1) // was (Double) [alan]
            errln( "newobjs.get(argName) = " + newobjs.get(argName));
    }{ // Taken from Test4105380().
        String patternText1 = "The disk \"{diskName}\" contains {numberOfFiles}.";
        String patternText2 = "There are {numberOfFiles} on the disk \"{diskName}\"";
        MessageFormat form1 = new MessageFormat(patternText1);
        MessageFormat form2 = new MessageFormat(patternText2);
        double[] filelimits = {0,1,2};
        String[] filepart = {"no files","one file","{numberOfFiles,number} files"};
        ChoiceFormat fileform = new ChoiceFormat(filelimits, filepart);
        form1.setFormat(1, fileform);
        form2.setFormat(0, fileform);
        Map testArgs = new HashMap();
        testArgs.put("diskName", "MyDisk");
        testArgs.put("numberOfFiles", new Long(12373));
        logln(form1.format(testArgs));
        logln(form2.format(testArgs));
    }{ // Taken from test4293229().
        MessageFormat format = new MessageFormat("'''{'myNamedArgument}'' '''{myNamedArgument}'''");
        Map args = new HashMap();
        String expected = "'{myNamedArgument}' '{myNamedArgument}'";
        String result = format.format(args);
        if (!result.equals(expected)) {
            throw new RuntimeException("wrong format result - expected \"" +
                    expected + "\", got \"" + result + "\"");
        }
    }
  }

    private MessageFormat serializeAndDeserialize(MessageFormat original) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream ostream = new ObjectOutputStream(baos);
            ostream.writeObject(original);
            ostream.flush();
            byte bytes[] = baos.toByteArray();
    
            ObjectInputStream istream = new ObjectInputStream(new ByteArrayInputStream(bytes));
            MessageFormat reconstituted = (MessageFormat)istream.readObject();
            return reconstituted;
        } catch(IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void TestSerialization() {
        MessageFormat format1 = null;
        MessageFormat format2 = null;

        format1 = new MessageFormat("", ULocale.GERMAN);
        format2 = serializeAndDeserialize(format1);
        assertEquals("MessageFormats (empty pattern) before and after serialization are not equal", format1, format2);

        format1.applyPattern("ab{1}cd{0,number}ef{3,date}gh");
        format1.setFormat(2, null);
        format1.setFormatByArgumentIndex(1, NumberFormat.getInstance(ULocale.ENGLISH));
        format2 = serializeAndDeserialize(format1);
        assertEquals("MessageFormats (with custom formats) before and after serialization are not equal", format1, format2);
        assertEquals(
                "MessageFormat (with custom formats) does not "+
                "format correctly after serialization",
                "ab3.3cd4,4ef***gh",
                format2.format(new Object[] { 4.4, 3.3, "+++", "***" }));
    }
}
