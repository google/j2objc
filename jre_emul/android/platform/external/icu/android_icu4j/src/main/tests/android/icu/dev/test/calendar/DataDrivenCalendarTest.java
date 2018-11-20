/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2007-2013, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.calendar;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.icu.dev.test.ModuleTest;
import android.icu.dev.test.ModuleTest.TestDataPair;
import android.icu.dev.test.TestDataModule;
import android.icu.dev.test.TestDataModule.DataMap;
import android.icu.dev.test.TestDataModule.TestData;
import android.icu.dev.test.TestFmwk;
import android.icu.dev.test.util.CalendarFieldsSet;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

/**
 * @author srl
 * @author sgill
 * 
 * analog of dadrcal.cpp
 *
 */
@RunWith(JUnitParamsRunner.class)
public class DataDrivenCalendarTest extends TestFmwk {

    public DataDrivenCalendarTest() {
        //super("com/ibm/icu/dev/data/testdata/", "calendar");
    }

    @SuppressWarnings("unused")
    private List<TestDataPair> getTestData() throws Exception { 
        return ModuleTest.getTestData("android/icu/dev/data/testdata/", "calendar");
    }

    /* (non-Javadoc)
     * @see android.icu.dev.test.ModuleTest#processModules()
     */
    @Test
    @Parameters(method="getTestData")
    public void calendarTest(TestDataPair pair) {
        TestData td = pair.td;
        DataMap settings = pair.dm;

        String type = settings.getString("Type");

        if(type.equals("convert_fwd")) {
            testConvert(td, settings, true);
        } else if(type.equals("convert_rev")) {
            testConvert(td, settings, false);
        } else if(type.equals("ops")) {
            testOps(td, settings);
        } else {
            errln("Unknown type: " + type);
        }
    }


    void testConvert(String caseString,
            CalendarFieldsSet fromSet, Calendar fromCalendar,
            CalendarFieldsSet toSet, Calendar toCalendar, boolean forward) {
        String thisString = caseString+(forward ? "forward"
                : "reverse")+" "+fromCalendar.getType()+"->"+toCalendar.getType()+" ";

        fromCalendar.clear();

        fromSet.setOnCalendar(fromCalendar);

        CalendarFieldsSet diffSet = new CalendarFieldsSet();

        diffSet.clear();
        // Is the calendar sane at the first?
        if (!fromSet.matches(fromCalendar, diffSet)) {
            String diffs = diffSet.diffFrom(fromSet);
            errln((String)"FAIL: "+thisString
                    +", SOURCE calendar was not set: Differences: "+ diffs);
        } else {
            logln("PASS: "+thisString+" SOURCE calendar match.");
        }

        //logln("Set Source calendar: " + from);

        Date fromTime = fromCalendar.getTime();

        diffSet.clear();
        // Is the calendar sane after being set?
        if (!fromSet.matches(fromCalendar, diffSet)) {
            String diffs = diffSet.diffFrom(fromSet);
            errln((String)"FAIL: "+thisString
                    +", SET SOURCE calendar was not set: Differences: "+ diffs);
        } else {
            logln("PASS: "+thisString+" SET SOURCE calendar match.");
        }

        toCalendar.clear();
        toCalendar.setTime(fromTime);

        diffSet.clear();
        if (!toSet.matches(toCalendar, diffSet)) {
            String diffs = diffSet.diffFrom(toSet);
            errln((String)"FAIL: "+thisString+", Differences: "+ diffs);
            DateFormat fmt = new SimpleDateFormat(new String("EEE MMM dd yyyy G"));
            String fromString = fmt.format(fromTime);
            logln("Source Time: "+fromString+", Source Calendar: "
                    +fromCalendar.getType());
        } else {
            logln("PASS: "+thisString+" match.");
        }
    }

    private void testConvert(TestDataModule.TestData testData, DataMap settings, boolean forward) {
        Calendar toCalendar= null;
        // build to calendar
        String testSetting = settings.getString("ToCalendar");
        ULocale loc = new ULocale(testSetting);
        toCalendar = Calendar.getInstance(loc);
        CalendarFieldsSet fromSet = new CalendarFieldsSet(), toSet = new CalendarFieldsSet();
//        DateFormat fmt = new SimpleDateFormat("EEE MMM dd yyyy / YYYY'-W'ww-ee");
        // Start the processing
        int n = 0;
        for (Iterator iter = testData.getDataIterator(); iter.hasNext();) {
            ++n;
            DataMap currentCase = (DataMap) iter.next();

            String caseString = "["+testData.getName()+"#"+n+" "+"]";
            String locale = testSetting = currentCase.getString("locale");
            ULocale fromLoc = new ULocale(testSetting);
            Calendar fromCalendar = Calendar.getInstance(fromLoc);

            fromSet.clear();
            toSet.clear();

            String from = currentCase.getString("from");
            fromSet.parseFrom(from);
            String to = currentCase.getString("to");
            toSet.parseFrom(to, fromSet);

            // now, do it.
            if (forward) {
                logln(caseString +" "+locale+"/"+from+" >>> "+loc+"/"
                        +to);
                testConvert(caseString, fromSet, fromCalendar, toSet, toCalendar, forward);
            } else {
                logln(caseString +" "+locale+"/"+from+" <<< "+loc+"/"
                        +to);
                testConvert(caseString, toSet, toCalendar, fromSet, fromCalendar, forward);
            }
        }
    }

    private static final String kADD = "add";
    private static final String kROLL = "roll";
    private static final String kMILLIS = "MILLIS=";

    private void testOps(TestDataModule.TestData testData, DataMap settings) {
        // Get 'from' time 
        CalendarFieldsSet fromSet = new CalendarFieldsSet(), toSet = new CalendarFieldsSet(), paramsSet = new CalendarFieldsSet(), diffSet = new CalendarFieldsSet();
        //        DateFormat fmt = new SimpleDateFormat("EEE MMM dd yyyy / YYYY'-W'ww-ee");
        // Start the processing
        int n = 0;
        long fromDate = 0;
        long toDate = 0;

        boolean useDate = false;

        for (Iterator iter = testData.getDataIterator(); iter.hasNext();) {
            ++n;
            DataMap currentCase = (DataMap) iter.next();

            String caseString = "[case "+n+"]";
            // build to calendar
            //             Headers { "locale","from","operation","params","to" }
            // #1 locale
            String param = "locale";
            String locale;
            String testSetting = currentCase.getString(param);
            locale = testSetting;
            ULocale loc = new ULocale(locale);
            Calendar fromCalendar = Calendar.getInstance(loc);

            fromSet.clear();
            // #2 'from' info
            param = "from";
            String from = testSetting=currentCase.getString(param);
            if(from.startsWith(kMILLIS)){
                useDate = true;
                fromDate = Long.parseLong(from.substring(kMILLIS.length()));
            }else{
                fromSet.parseFrom(testSetting);
            }
//            System.err.println("fromset: ["+testSetting+"] >> " + fromSet);

            // #4 'operation' info
            param = "operation";
            String operation = testSetting=currentCase.getString(param);
            paramsSet.clear();
            // #3 'params' info
            param = "params";
            String paramsData = testSetting =  currentCase.getString(param);
            paramsSet.parseFrom(paramsData); // parse with inheritance.
//            System.err.println("paramsSet: ["+testSetting+"] >> " + paramsSet);
            
            toSet.clear();
            // #4 'to' info
            param = "to";
            String to = testSetting=currentCase.getString(param);
            if(to.startsWith(kMILLIS)){
                useDate = true;
                toDate = Long.parseLong(to.substring(kMILLIS.length()));
            }else{ 
                toSet.parseFrom(testSetting, fromSet);
            }
            //toSet.parseFrom(testSetting, fromSet); // parse with inheritance.
//            System.err.println("toSet: ["+testSetting+"] >> " + toSet);

            String caseContentsString = locale+":  from "+from+": "
                    +operation +" [[[ "+paramsSet+" ]]]   >>> "+to;
            logln(caseString+": "+caseContentsString);

            // END_INCLUDE(---)
            // now, do it.

            /// prepare calendar
            if(useDate){
                fromCalendar.setTimeInMillis(fromDate);
            }else {
                fromSet.setOnCalendar(fromCalendar);
            }

            // from calendar:  'starting date'

            diffSet.clear();

            // Is the calendar sane after being set?
            if (!fromSet.matches(fromCalendar, diffSet)) {
                String diffs = diffSet.diffFrom(fromSet);
                errln((String)"FAIL: "+caseString
                        +", SET SOURCE calendar was not set: Differences: "+ diffs);
            }  else {
                logln(" "+caseString+" SET SOURCE calendar match."); // verifies that the requested fields were set.
            }

            // to calendar - copy of from calendar
            Calendar toCalendar = (Calendar)fromCalendar.clone();

            /// perform op on 'to calendar'
            for (int q=0; q<paramsSet.fieldCount(); q++) {
                if (paramsSet.isSet(q)) {
                    if (operation.equals(kROLL)) {
                        toCalendar.roll(q,
                                paramsSet.get(q));
                    } else if (operation.equals(kADD)) {
                        toCalendar.add(q,
                                paramsSet.get(q));
                    } else {
                        errln(caseString+ " FAIL: unknown operation "+ operation);
                    }
                    logln(operation + " of "+ paramsSet.get(q));
                }
            }
            // now - what's the result?
            diffSet.clear();

            // toset contains 'expected'

            if(useDate) {
                if(toCalendar.getTimeInMillis()==toDate) {
                    logln(caseString + " SUCCESS: got=expected="+toDate);
                    logln("PASS: "+caseString+" matched! ");
                } else {
                    // Note: With JDK TimeZone implementation, tz offset on dates earlier than
                    // mid-1900 might be different from the TZDB. Following test cases are
                    // failing because of this.
                    if ((caseString.equals("[case 31]") || caseString.equals("[case 36]")) 
                            && TimeZone.getDefaultTimeZoneType() == TimeZone.TIMEZONE_JDK) {
                        logln(caseString + " FAIL(expected): got " + 
                                toCalendar.getTimeInMillis() + "  expected " + 
                                toDate);
                    } else {
                        errln(caseString + " FAIL: got " + 
                                toCalendar.getTimeInMillis() + "  expected " + 
                                toDate);
                    }
                }
            }else if (!toSet.matches(toCalendar, diffSet)) {
                String diffs = diffSet.diffFrom(toSet);
                errln((String)"FAIL: "+caseString+" - , "+caseContentsString
                        +" Differences: "+ diffs );
            } else{
                logln("PASS: "+caseString+" matched! ");
            }

        }
    }
}
