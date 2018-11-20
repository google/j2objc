/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2007-2012, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.format;

import java.text.FieldPosition;
import java.text.ParsePosition;
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
import android.icu.dev.test.util.DateTimeStyleSet;
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
 */
@RunWith(JUnitParamsRunner.class)
public class DataDrivenFormatTest extends TestFmwk {

    /**
     * @param baseName
     * @param locName
     */
    public DataDrivenFormatTest() {
        //super("com/ibm/icu/dev/data/testdata/", "format");
    }

    @SuppressWarnings("unused")
    private List<TestDataPair> getTestData() throws Exception { 
        return ModuleTest.getTestData("android/icu/dev/data/testdata/", "format");
    }

    /* (non-Javadoc)
     * @see android.icu.dev.test.ModuleTest#processModules()
     */
    @Test
    @Parameters(method="getTestData")
    public void formatTest(TestDataPair pair) {
        TestData td = pair.td;
        DataMap settings = pair.dm;


        String type = settings.getString("Type");

        if(type.equals("date_format")) {
            testConvertDate(td, settings, true);
        } else if(type.equals("date_parse")) {
            testConvertDate(td, settings, false);
        } else {
            errln("Unknown type: " + type);
        }
    }

   
    private static final String kPATTERN = "PATTERN=";
    private static final String kMILLIS = "MILLIS=";
    private static final String kRELATIVE_MILLIS = "RELATIVE_MILLIS=";
    private static final String kRELATIVE_ADD = "RELATIVE_ADD:";
    
    private void testConvertDate(TestDataModule.TestData testData, DataMap  settings, boolean fmt) {
        DateFormat basicFmt = new SimpleDateFormat("EEE MMM dd yyyy / YYYY'-W'ww-ee");

        int n = 0;
        for (Iterator iter = testData.getDataIterator(); iter.hasNext();) {
            ++n;
            long now = System.currentTimeMillis();
            DataMap currentCase = (DataMap) iter.next();
            String caseString = "["+testData.getName()+"#"+n+(fmt?"format":"parse")+"]";
            
            String locale = currentCase.getString("locale");
            String zone = currentCase.getString("zone");
            String spec = currentCase.getString("spec");
            String date = currentCase.getString("date");
            String str = currentCase.getString("str");
            
            Date fromDate = null;
            boolean useDate = false;
            
            ULocale loc = new ULocale(locale);
            String pattern = null;
//            boolean usePattern = false;
            DateFormat format = null;
            DateTimeStyleSet styleSet;
            CalendarFieldsSet fromSet = null;
            
            // parse 'spec'  - either 'PATTERN=yy mm dd' or 'DATE=x,TIME=y'
            if(spec.startsWith(kPATTERN)) {
                pattern = spec.substring(kPATTERN.length());
//                usePattern = true;
                format = new SimpleDateFormat(pattern, loc);
            } else {
                styleSet = new DateTimeStyleSet();
                styleSet.parseFrom(spec);
                format = DateFormat.getDateTimeInstance(styleSet.getDateStyle(), styleSet.getTimeStyle(), loc);
            }

            Calendar cal = Calendar.getInstance(loc);

            if (zone.length() > 0) {
                TimeZone tz = TimeZone.getFrozenTimeZone(zone);
                cal.setTimeZone(tz);
                format.setTimeZone(tz);
            }
            
            // parse 'date' - either 'MILLIS=12345' or  a CalendarFieldsSet
            if(date.startsWith(kMILLIS)) {
                useDate = true;
                fromDate = new Date(Long.parseLong(date.substring(kMILLIS.length())));
            } else if(date.startsWith(kRELATIVE_MILLIS)) {
                useDate = true;
                fromDate = new Date(now+Long.parseLong(date.substring(kRELATIVE_MILLIS.length())));
            } else if(date.startsWith(kRELATIVE_ADD)) {
                String add = date.substring(kRELATIVE_ADD.length()); // "add" is a string indicating which fields to add
                CalendarFieldsSet addSet = new CalendarFieldsSet();
                addSet.parseFrom(add);
                useDate = true;
                cal.clear();
                cal.setTimeInMillis(now);

                /// perform op on 'to calendar'
                for (int q=0; q<addSet.fieldCount(); q++) {
                    if (addSet.isSet(q)) {
                        if (q == Calendar.DATE) {
                            cal.add(q,addSet.get(q));
                        } else {
                            cal.set(q,addSet.get(q));
                        }
                    }
                }

                fromDate = cal.getTime();
            } else {
                fromSet = new CalendarFieldsSet();
                fromSet.parseFrom(date);
            }
            
            // run the test
            if(fmt) {
                StringBuffer output = new StringBuffer();
                cal.clear();
                FieldPosition pos = new FieldPosition(0);
                if(useDate) {
                    output = format.format(fromDate, output, pos);
                } else {
                    fromSet.setOnCalendar(cal);
                    format.format(cal, output, pos);
                }
                
                if(output.toString().equals(str)) {
                    logln(caseString + " Success - strings match: " + output);
                } else {
                    errln(caseString + " FAIL: got " + output + " expected " + str);
                }
            } else { // parse
                cal.clear();
                ParsePosition pos = new ParsePosition(0);
                format.parse(str, cal, pos);
                if(useDate) {
                    Date gotDate = cal.getTime(); 
                    if(gotDate.equals(fromDate)) {
                        logln(caseString + " SUCCESS: got=parse="+str);
                    } else {
                        errln(caseString + " FAIL: parsed " + str + " but got " + 
                                basicFmt.format(gotDate) + " - " + gotDate + "  expected " + 
                                basicFmt.format(fromDate));
                    }
                } else  {
                    CalendarFieldsSet diffSet = new CalendarFieldsSet();
                    if(!fromSet.matches(cal, diffSet)) {
                        String diffs = diffSet.diffFrom(fromSet);
                        errln(caseString + " FAIL:  differences: " + diffs);
                    } else {
                        logln(caseString + " SUCCESS: got=parse: " + str + " - " + fromSet.toString());
                    }
                }
            }
        }
    }
}
