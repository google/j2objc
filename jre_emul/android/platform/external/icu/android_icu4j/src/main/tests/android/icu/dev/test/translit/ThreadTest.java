/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.translit;

import java.util.ArrayList;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.Transliterator;

// Test for ICU Ticket #7201.  With threading bugs in RuleBasedTransliterator, this
//   test would reliably crash.

public class ThreadTest extends TestFmwk {
    private ArrayList<Worker> threads = new ArrayList<Worker>();
    // Android patch: Halved the execution time of ThreadTest#TestThreads.
    private int iterationCount = 50000;
    // Android patch end.
    
    @Test
    public void TestThreads()  {
        if (TestFmwk.getExhaustiveness() >= 9) {
            // Exhaustive test.  Run longer.
            // Android patch: Halved the execution time of ThreadTest#TestThreads.
            iterationCount = 500000;
            // Android patch end.
        }
        
        for (int i = 0; i < 8; i++) {
            Worker thread = new Worker();
            threads.add(thread);
            thread.start();
        }
        long expectedCount = 0;
        for (Worker thread: threads) {
            try {
                thread.join();
                if (expectedCount == 0) {
                    expectedCount = thread.count;
                } else {
                    if (expectedCount != thread.count) {
                        errln("Threads gave differing results.");
                    }
                }
            } catch (InterruptedException e) {
                errln(e.toString());
            }
        }
    }
    
    private static final String [] WORDS = {"edgar", "allen", "poe"};
   
    private class Worker extends Thread {   
        public long count = 0;
        public void run() {
            Transliterator tx = Transliterator.getInstance("Latin-Thai");        
            for (int loop = 0; loop < iterationCount; loop++) {
                for (String s : WORDS) {
                    count += tx.transliterate(s).length();
                }                
            }
        }
    }
    
    // Test for ticket #10673, race in cache code in AnyTransliterator.
    // It's difficult to make the original unsafe code actually fail, but
    // this test will fairly reliably take the code path for races in 
    // populating the cache.
    // 
    @Test
    public void TestAnyTranslit() {
        final Transliterator tx = Transliterator.getInstance("Any-Latin");
        ArrayList<Thread> threads = new ArrayList<Thread>();
        for (int i=0; i<8; i++) {
            threads.add(new Thread() {
                public void run() {
                    tx.transliterate("διαφορετικούς");
                }
            });
        }
        for (Thread th:threads) {
            th.start();
        }
        for (Thread th:threads) {
            try {
                th.join();
            } catch (InterruptedException e) {
                errln("Uexpected exception: " + e);
            }
        }
    }

}
