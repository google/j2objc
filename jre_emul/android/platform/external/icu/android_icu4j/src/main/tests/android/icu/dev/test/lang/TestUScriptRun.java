/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
*******************************************************************************
* Copyright (C) 1999-2010, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*/

package android.icu.dev.test.lang;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.lang.UScript;
import android.icu.lang.UScriptRun;

public class TestUScriptRun extends TestFmwk
{
    public TestUScriptRun()
    {
        // nothing
    }
    
    private static final class RunTestData
    {
        String runText;
        int    runScript;
        
        public RunTestData(String theText, int theScriptCode)
        {
            runText   = theText;
            runScript = theScriptCode;
        }
    }
    
    private static final RunTestData[][] m_testData = {
        {
            new RunTestData("\u0020\u0946\u0939\u093F\u0928\u094D\u0926\u0940\u0020", UScript.DEVANAGARI),
            new RunTestData("\u0627\u0644\u0639\u0631\u0628\u064A\u0629\u0020", UScript.ARABIC),
            new RunTestData("\u0420\u0443\u0441\u0441\u043A\u0438\u0439\u0020", UScript.CYRILLIC),
            new RunTestData("English (", UScript.LATIN),
            new RunTestData("\u0E44\u0E17\u0E22", UScript.THAI),
            new RunTestData(") ", UScript.LATIN),
            new RunTestData("\u6F22\u5B75", UScript.HAN),
            new RunTestData("\u3068\u3072\u3089\u304C\u306A\u3068", UScript.HIRAGANA),
            new RunTestData("\u30AB\u30BF\u30AB\u30CA", UScript.KATAKANA),
            new RunTestData("\uD801\uDC00\uD801\uDC01\uD801\uDC02\uD801\uDC03", UScript.DESERET),
        },
        {
            new RunTestData("((((((((((abc))))))))))", UScript.LATIN)
        }
    };
    
    private static final String padding = "This string is used for padding...";
    
    private void CheckScriptRuns(UScriptRun scriptRun, int[] runStarts, RunTestData[] testData)
    {
        int run, runStart, runLimit;
        int runScript;

        /* iterate over all the runs */
        run = 0;
        while (scriptRun.next()) {
            runStart  = scriptRun.getScriptStart();
            runLimit  = scriptRun.getScriptLimit();
            runScript = scriptRun.getScriptCode();
            
            if (runStart != runStarts[run]) {
                errln("Incorrect start offset for run " + run + ": expected " + runStarts[run] + ", got " + runStart);
            }

            if (runLimit != runStarts[run + 1]) {
                errln("Incorrect limit offset for run " + run + ": expected " + runStarts[run + 1] + ", got " + runLimit);
            }

            if (runScript != testData[run].runScript) {
                errln("Incorrect script for run " + run + ": expected \"" + UScript.getName(testData[run].runScript) + "\", got \"" + UScript.getName(runScript) + "\"");
            }
            
            run += 1;

            /* stop when we've seen all the runs we expect to see */
            if (run >= testData.length) {
                break;
            }
        }

        /* Complain if we didn't see then number of runs we expected */
        if (run != testData.length) {
            errln("Incorrect number of runs: expected " + testData.length + ", got " + run);
        }
    }

    @Test
    public void TestContstruction()
    {
        UScriptRun scriptRun = null;
        char[] nullChars  = null, dummyChars  = {'d', 'u', 'm', 'm', 'y'};
        String nullString = null, dummyString = new String(dummyChars);
        
        try {
            scriptRun = new UScriptRun(nullString, 0, 100);
            errln("new UScriptRun(nullString, 0, 100) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UScriptRun failed as expected");
        }
        
        try {
            scriptRun = new UScriptRun(nullString, 100, 0);
            errln("new UScriptRun(nullString, 100, 0) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UScriptRun failed as expected");
        }
        
        try {
            scriptRun = new UScriptRun(nullString, 0, -100);
            errln("new UScriptRun(nullString, 0, -100) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UScriptRun failed as expected");
        }
        
        try {
            scriptRun = new UScriptRun(nullString, -100, 0);
            errln("new UScriptRun(nullString, -100, 0) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UScriptRun failed as expected");
        }
        
        try {
            scriptRun = new UScriptRun(nullChars, 0, 100);
            errln("new UScriptRun(nullChars, 0, 100) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UScriptRun failed as expected");
        }
        
        try {
            scriptRun = new UScriptRun(nullChars, 100, 0);
            errln("new UScriptRun(nullChars, 100, 0) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UScriptRun failed as expected");
        }
        
        try {
            scriptRun = new UScriptRun(nullChars, 0, -100);
            errln("new UScriptRun(nullChars, 0, -100) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UScriptRun failed as expected");
        }
        
        try {
            scriptRun = new UScriptRun(nullChars, -100, 0);
            errln("new UScriptRun(nullChars, -100, 0) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UScriptRun failed as expected");
        }
        
        try {
            scriptRun = new UScriptRun(dummyString, 0, 6);
            errln("new UScriptRun(dummyString, 0, 6) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UScriptRun failed as expected");
        }
        
        try {
            scriptRun = new UScriptRun(dummyString, 6, 0);
            errln("new UScriptRun(dummy, 6, 0) did not produce an IllegalArgumentException!");
        }catch (IllegalArgumentException iae) {
            logln("PASS: UScriptRun failed as expected");
        }
        
        try {
            scriptRun = new UScriptRun(dummyString, 0, -100);
            errln("new UScriptRun(dummyString, 0, -100) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UScriptRun failed as expected");
        }
        
        try {
            scriptRun = new UScriptRun(dummyString, -100, 0);
            errln("new UScriptRun(dummy, -100, 0) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UScriptRun failed as expected");
        }
        
        try {
            scriptRun = new UScriptRun(dummyChars, 0, 6);
            errln("new UScriptRun(dummyChars, 0, 6) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UScriptRun failed as expected");
        }
        
        try {
            scriptRun = new UScriptRun(dummyChars, 6, 0);
            errln("new UScriptRun(dummyChars, 6, 0) did not produce an IllegalArgumentException!");
        }catch (IllegalArgumentException iae) {
            logln("PASS: UScriptRun failed as expected");
        }
        
        try {
            scriptRun = new UScriptRun(dummyChars, 0, -100);
            errln("new UScriptRun(dummyChars, 0, -100) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UScriptRun failed as expected");
        }
        
        try {
            scriptRun = new UScriptRun(dummyChars, -100, 0);
            errln("new UScriptRun(dummy, -100, 0) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
            logln("PASS: UScriptRun failed as expected");
        }
        if(scriptRun!=null){
            errln("Did not get the expected Exception");
        }
    }
    
    @Test
    public void TestReset()
    {
        UScriptRun scriptRun = null;
        char[] dummy = {'d', 'u', 'm', 'm', 'y'};
        
        try {
            scriptRun = new UScriptRun();
        } catch (IllegalArgumentException iae) {
            errln("new UScriptRun() produced an IllegalArgumentException!");
        }
        
        try {
            scriptRun.reset(0, 100);
            errln("scriptRun.reset(0, 100) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
            logln("PASS: scriptRun.reset failed as expected");
        }
        
        try {
            scriptRun.reset(100, 0);
            errln("scriptRun.reset(100, 0) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
            logln("PASS: scriptRun.reset failed as expected");
        }
        
        try {
            scriptRun.reset(0, -100);
            errln("scriptRun.reset(0, -100) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
            logln("PASS: scriptRun.reset failed as expected");
        }
        
        try {
            scriptRun.reset(-100, 0);
            errln("scriptRun.reset(-100, 0) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
            logln("PASS: scriptRun.reset failed as expected");
        }
        
        try {
            scriptRun.reset(dummy, 0, 6);
            errln("scriptRun.reset(dummy, 0, 6) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
            logln("PASS: scriptRun.reset failed as expected");
        }
        
        try {
            scriptRun.reset(dummy, 6, 0);
            errln("scriptRun.reset(dummy, 6, 0) did not produce an IllegalArgumentException!");
        }catch (IllegalArgumentException iae) {
            logln("PASS: scriptRun.reset failed as expected");
        }
        
        try {
            scriptRun.reset(dummy, 0, -100);
            errln("scriptRun.reset(dummy, 0, -100) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
            logln("PASS: scriptRun.reset failed as expected");
        }
        
        try {
            scriptRun.reset(dummy, -100, 0);
            errln("scriptRun.reset(dummy, -100, 0) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
            logln("PASS: scriptRun.reset failed as expected");
        }
        
        try {
            scriptRun.reset(dummy, 0, dummy.length);
        } catch (IllegalArgumentException iae) {
            errln("scriptRun.reset(dummy, 0, dummy.length) produced an IllegalArgumentException!");
        }
        
        
        try {
            scriptRun.reset(0, 6);
            errln("scriptRun.reset(0, 6) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
            logln("PASS: scriptRun.reset failed as expected");
        }
        
        try {
            scriptRun.reset(6, 0);
            errln("scriptRun.reset(6, 0) did not produce an IllegalArgumentException!");
        } catch (IllegalArgumentException iae) {
            logln("PASS: scriptRun.reset failed as expected");
        }
        
        try {
            scriptRun.reset(dummy, 0, dummy.length);
            scriptRun.reset();
        } catch(IllegalArgumentException iae){
            errln("scriptRun.reset() produced an IllegalArgumentException!");
        }
        
        try {
            scriptRun.reset((char[]) null);
        } catch(IllegalArgumentException iae){
            errln("scriptRun.reset((char[])null) produced an IllegalArgumentException!");
        }
        
        try {
            scriptRun.reset((String) null);
        } catch(IllegalArgumentException iae){
            errln("scriptRun.reset((String)null) produced an IllegalArgumentException!");
        }
    }
    
    @Test
    public void TestRuns()
    {
        for (int i = 0; i < m_testData.length; i += 1) {
            RunTestData[] test = m_testData[i];
            int stringLimit = 0;
            int[] runStarts = new int[test.length + 1];
            String testString = "";
            UScriptRun scriptRun = null;
        
            /*
             * Fill in the test string and the runStarts array.
             */
            for (int run = 0; run < test.length; run += 1) {
                runStarts[run] = stringLimit;
                stringLimit += test[run].runText.length();
                testString  += test[run].runText;
            }

            /* The limit of the last run */ 
            runStarts[test.length] = stringLimit;
        
            try {
                scriptRun = new UScriptRun(testString);
                CheckScriptRuns(scriptRun, runStarts, test);
            } catch (IllegalArgumentException iae) {
                errln("new UScriptRun(testString) produced an IllegalArgumentException!");
            }
        
            try {
                scriptRun.reset();
                CheckScriptRuns(scriptRun, runStarts, test);
            } catch (IllegalArgumentException iae) {
                errln("scriptRun.reset() on a valid UScriptRun produced an IllegalArgumentException!");
            }
        
            try {
                scriptRun = new UScriptRun(testString.toCharArray());
                CheckScriptRuns(scriptRun, runStarts, test);
            } catch (IllegalArgumentException iae) {
                errln("new UScriptRun(testString.toCharArray()) produced an IllegalArgumentException!");
            }
        
            try {
                scriptRun.reset();
                CheckScriptRuns(scriptRun, runStarts, test);
            } catch (IllegalArgumentException iae) {
                errln("scriptRun.reset() on a valid UScriptRun produced an IllegalArgumentException!");
            }
        
            try {
                scriptRun = new UScriptRun();
            
                if (scriptRun.next()) {
                    errln("scriptRun.next() on an empty UScriptRun returned true!");
                }
            } catch (IllegalArgumentException iae) {
                errln("new UScriptRun() produced an IllegalArgumentException!");
            }
        
            try {
                scriptRun.reset(testString, 0, testString.length());
                CheckScriptRuns(scriptRun, runStarts, test);
            } catch (IllegalArgumentException iae) {
                errln("scriptRun.reset(testString, 0, testString.length) produced an IllegalArgumentException!");
            }

            try {
                scriptRun.reset(testString.toCharArray(), 0, testString.length());
                CheckScriptRuns(scriptRun, runStarts, test);
            } catch (IllegalArgumentException iae) {
                errln("scriptRun.reset(testString.toCharArray(), 0, testString.length) produced an IllegalArgumentException!");
            }

            String paddedTestString = padding + testString + padding;
            int startOffset = padding.length();
            int count = testString.length();
            
            for (int run = 0; run < runStarts.length; run += 1) {
                runStarts[run] += startOffset;
            }
            
            try {
                scriptRun.reset(paddedTestString, startOffset, count);
                CheckScriptRuns(scriptRun, runStarts, test);
            } catch (IllegalArgumentException iae) {
                errln("scriptRun.reset(paddedTestString, startOffset, count) produced an IllegalArgumentException!");
            }

            try {
                scriptRun.reset(paddedTestString.toCharArray(), startOffset, count);
                CheckScriptRuns(scriptRun, runStarts, test);
            } catch (IllegalArgumentException iae) {
                errln("scriptRun.reset(paddedTestString.toCharArray(), startOffset, count) produced an IllegalArgumentException!");
            }
            
            /* Tests "public final void reset()" */
            // Tests when "while (stackIsNotEmpty())" is true
            try{
                UScriptRun usr = new UScriptRun((String)null);
                usr.reset();
            } catch (Exception e){
                errln("scriptRun.reset() was not suppose to produce an exception.");
            }
        }
    }
}
