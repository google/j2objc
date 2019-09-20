/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2001-2014, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */

/*
 * New added, 2001-10-17 [Jing/GCL]
 */

package android.icu.dev.test.format;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import android.icu.text.DateFormat;
import android.icu.text.DateFormatSymbols;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;

public class DateFormatRegressionTestJ extends android.icu.dev.test.TestFmwk {
    
    private static final String TIME_STRING = "2000/11/17 08:01:00";
    private static final long UTC_LONG = 974476860000L;
    private SimpleDateFormat sdf_;
    
    @Before
    public void init()throws Exception {
        sdf_ = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");        
    }
    
    //Return value of getAmPmStrings
    @Test
    public void Test4103926() {
        String act_Ampms[];
        String exp_Ampms[]={"AM","PM"};
        Locale.setDefault(Locale.US);
        
        DateFormatSymbols dfs = new DateFormatSymbols();
        act_Ampms = dfs.getAmPmStrings();
        if(act_Ampms.length != exp_Ampms.length) {
            errln("The result is not expected!");
        } else {
            for(int i =0; i<act_Ampms.length; i++) {
                if(!act_Ampms[i].equals(exp_Ampms[i]))
                    errln("The result is not expected!");
            }
        }
    }

    // Missing digit in millisecond format in SimpleDateFormat 
    @Test
    public void Test4148168() {
            Date d = new Date(1002705212906L);
            String[] ISOPattern = {
                "''yyyy-MM-dd-hh.mm.ss.S''", "''yyyy-MM-dd-hh.mm.ss.SS''", 
                "''yyyy-MM-dd-hh.mm.ss.SSS''", "''yyyy-MM-dd-hh.mm.ss.SSSS''", 
                "''yyyy-MM-dd-hh.mm.ss.SSSSS''", "''yyyy-MM-dd-hh.mm.ss.SSSSSS''", 
                "''yyyy-MM-dd-hh.mm.ss.SSSSSSS''", "''yyyy-MM-dd-hh.mm.ss.SSS000''"};
            SimpleDateFormat aSimpleDF = (SimpleDateFormat)DateFormat.getDateTimeInstance();
    
            for(int i = 0; i<ISOPattern.length; i++) {
                aSimpleDF.applyPattern( ISOPattern[i] );
                logln( "Pattern = " + aSimpleDF.toPattern());
                logln( "Format = " + aSimpleDF.format(d));
            }
    }
    
    //DateFormat getDateTimeInstance(int, int), invalid styles no exception
    @Test
    public void Test4213086() {
        Date someDate = new Date();
        String d=null;
        try {
            DateFormat df2 = DateFormat.getDateTimeInstance(2, -2);
            d = df2.format(someDate);
            errln("we should catch an exception here");
        } catch(Exception e){
            logln("dateStyle = 2" + "\t timeStyle = -2");
            logln("Exception caught!");
        }            
        
        try {
            DateFormat df3 = DateFormat.getDateTimeInstance(4, 2);
            d = df3.format(someDate);
            errln("we should catch an exception here");
        } catch(Exception e){
            logln("dateStyle = 4" + "\t timeStyle = 2");
            logln("Exception caught!");
            logln("********************************************");
        }
    
        try {
            DateFormat df4 = DateFormat.getDateTimeInstance(-12, -12);
            d = df4.format(someDate);
            errln("we should catch an exception here");
        } catch(Exception e){
            logln("dateStyle = -12" + "\t timeStyle = -12");
            logln("Exception caught!");
            logln("********************************************");
        }
    
        try{
            DateFormat df5 = DateFormat.getDateTimeInstance(2, 123);
            d = df5.format(someDate);    
            errln("we should catch an exception here");
        } catch(Exception e){
            logln("dateStyle = 2" + "\t timeStyle = 123");
            logln("Exception caught!");
            logln("********************************************");
        }
        //read the value in d to get rid of the warning
        if(d!=null){
            logln("The value of d: " + d);
        }
    }
    
    //DateFormat.format works wrongly?
    @Test
    public void Test4250359() {
        Locale.setDefault(Locale.US);
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(101 + 1900, 9, 9, 17, 53);
        Date d = cal.getTime();
        DateFormat tf = DateFormat.getTimeInstance(DateFormat.SHORT);
        String act_result = tf.format(d);
        String exp_result = "5:53 PM";
        
        if(!act_result.equals(exp_result)){
            errln("The result is not expected");
        }
    }
    
    //pattern "s.S, parse '1ms'"
    @Test
    public void Test4253490() {
        Date d = new Date(1002705212231L);

        String[] ISOPattern = {
                "''yyyy-MM-dd-hh.mm.ss.S''", 
                "''yyyy-MM-dd-hh.mm.ss.SS''", 
                "''yyyy-MM-dd-hh.mm.ss.SSS''", 
                "''yyyy-MM-dd-hh.mm.ss.SSSS''", 
                "''yyyy-MM-dd-hh.mm.ss.SSSSS''", 
                "''yyyy-MM-dd-hh.mm.ss.SSSSSS''", 
                "''yyyy-MM-dd-hh.mm.ss.SSSSSSS''"}; 
    
        SimpleDateFormat aSimpleDF = (SimpleDateFormat) DateFormat.getDateTimeInstance();
        for (int i = 0; i < ISOPattern.length; i++) {
            aSimpleDF.applyPattern(ISOPattern[i]);
            logln("Pattern = " + aSimpleDF.toPattern());
            logln("Format = " + aSimpleDF.format(d));
        }
    }
    
    //about regression test
    @Test
    public void Test4266432() {
        Locale.setDefault(Locale.JAPAN);
        Locale loc = Locale.getDefault();
        String dateFormat = "MM/dd/yy HH:mm:ss zzz";
        SimpleDateFormat fmt = new SimpleDateFormat(dateFormat);
    
        ParsePosition p0 = new ParsePosition(0);
        logln("Under  " + loc +"  locale");
        Date d = fmt.parse("01/22/92 04:52:00 GMT", p0);
        logln(d.toString());
    }
    
    //SimpleDateFormat inconsistent for number of digits for years
    @Test
    public void Test4358730() {
        SimpleDateFormat sdf = new SimpleDateFormat();
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2001,11,10);
        Date today = cal.getTime();
    
        sdf.applyPattern("MM d y");
        logln(sdf.format(today));
        sdf.applyPattern("MM d yy");
        logln(sdf.format(today));
    
        sdf.applyPattern("MM d yyy");
        logln(sdf.format(today));
    
        sdf.applyPattern("MM d yyyy");
        logln(sdf.format(today));
    
        sdf.applyPattern("MM d yyyyy");
        logln(sdf.format(today));
    }
    
    //Parse invalid string
    @Test
    public void Test4375399() {
        final String pattern = new String("yyyy.MM.dd G 'at' hh:mm:ss z");
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.JAPAN);
        try{
            Date currentTime = sdf.parse("vggf 20  01.0 9.29 ap. J.-C. at 05:26:33 GMT+08:00",
                                new ParsePosition(0));
            if(currentTime ==null)
                logln("parse right");
        } catch(Exception e){
            errln("Error");
        }
    }
    /*
    @Test
    public void Test4407042() {
        DateParseThread d1 = new DateParseThread();
        DateFormatThread d2 = new DateFormatThread();
        d1.start();
        d2.start();
        try {
            logln("test");
            Thread.sleep(1000);
        } catch (Exception e) {}
    }*/
    
    @Test
    public void Test4468663() {
        Date d =new Date(-93716671115767L);
        String origin_d = d.toString();
        String str;
        final String pattern = new String("EEEE, MMMM d, yyyy");
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);

        if (sdf.getTimeZone().useDaylightTime()) {
            logln("original date: " + origin_d.toString());
            str = sdf.format(d);
            logln(" after format----->" + str);
            
            d = sdf.parse(str, new ParsePosition(0));
            logln(" after parse----->" + d.toString());
    
            str = sdf.format(d);
            logln(" after format----->" + str);
    
            d = sdf.parse(str, new ParsePosition(0));
            logln(" after parse----->" + d.toString());
    
            str = sdf.format(d);
            logln(" after format----->" + str);        
        }
    }
    
    //Class used by Test4407042
    class DateParseThread extends Thread {
        public void run() {
            SimpleDateFormat sdf = (SimpleDateFormat) sdf_.clone();
            TimeZone defaultTZ = TimeZone.getDefault();
            TimeZone PST = TimeZone.getTimeZone("PST");
            int defaultOffset = defaultTZ.getRawOffset();
            int PSTOffset = PST.getRawOffset();
            int offset = defaultOffset - PSTOffset;
            long ms = UTC_LONG - offset;
            try {
                int i = 0;
                while (i < 10000) {
                    Date date = sdf.parse(TIME_STRING);
                    long t = date.getTime();
                    i++;
                    if (t != ms) {
                        throw new ParseException("Parse Error: " + i + " (" + sdf.format(date) 
                                  + ") " + t + " != " + ms, 0);
                    }
                }
            } catch (Exception e) {
                errln("parse error: " + e.getMessage());
            }
        }
    }
    
    //Class used by Test4407042
    class DateFormatThread extends Thread {
        public void run() {            
            SimpleDateFormat sdf = (SimpleDateFormat) sdf_.clone();
            TimeZone tz = TimeZone.getTimeZone("PST");
            sdf.setTimeZone(tz);
            int i = 0;
            while (i < 10000) {
                i++;
                String s = sdf.format(new Date(UTC_LONG));
                if (!s.equals(TIME_STRING)) {
                    errln("Format Error: " + i + " " + s + " != " 
                                    + TIME_STRING);
                }
            }
        }
    }
    
}
