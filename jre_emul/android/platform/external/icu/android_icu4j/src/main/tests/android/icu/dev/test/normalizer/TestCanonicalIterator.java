/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2010, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.dev.test.normalizer;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.Utility;
import android.icu.lang.UCharacter;
import android.icu.text.CanonicalIterator;
import android.icu.text.Normalizer;
import android.icu.text.UTF16;


// TODO: fit into test framework

public class TestCanonicalIterator extends TestFmwk {
    
    static final boolean SHOW_NAMES = false;

    static final String testArray[][] = {
        {"\u00C5d\u0307\u0327", "A\u030Ad\u0307\u0327, A\u030Ad\u0327\u0307, A\u030A\u1E0B\u0327, "
            + "A\u030A\u1E11\u0307, \u00C5d\u0307\u0327, \u00C5d\u0327\u0307, "
            + "\u00C5\u1E0B\u0327, \u00C5\u1E11\u0307, \u212Bd\u0307\u0327, "
            + "\u212Bd\u0327\u0307, \u212B\u1E0B\u0327, \u212B\u1E11\u0307"},
        {"\u010d\u017E", "c\u030Cz\u030C, c\u030C\u017E, \u010Dz\u030C, \u010D\u017E"},
        {"x\u0307\u0327", "x\u0307\u0327, x\u0327\u0307, \u1E8B\u0327"},
    };

    @Test
    public void TestExhaustive() {
        int counter = 0;
        CanonicalIterator it = new CanonicalIterator("");
        /*
        CanonicalIterator slowIt = new CanonicalIterator("");
        slowIt.SKIP_ZEROS = false;
        */
        //Transliterator name = Transliterator.getInstance("[^\\u0020-\\u007F] name");
        //Set itSet = new TreeSet();
        //Set slowItSet = new TreeSet();


        for (int i = 0; i < 0x10FFFF; ++i) {

            // skip characters we know don't have decomps
            int type = UCharacter.getType(i);
            if (type == Character.UNASSIGNED || type == Character.PRIVATE_USE
                || type == Character.SURROGATE) continue;

            if ((++counter % 5000) == 0) logln("Testing " + Utility.hex(i,0));

            String s = UTF16.valueOf(i);
            characterTest(s, i, it);

            characterTest(s + "\u0345", i, it);
        }
    }
    
    public int TestSpeed() {
         // skip unless verbose
        if (!isVerbose()) return 0;

           String s = "\uAC01\u0345";
           
        CanonicalIterator it = new CanonicalIterator(s);
        double start, end;
        int x = 0; // just to keep code from optimizing away.
        int iterations = 10000;
        double slowDelta = 0;
        
        /*
        CanonicalIterator slowIt = new CanonicalIterator(s);
        slowIt.SKIP_ZEROS = false;

        start = System.currentTimeMillis();
        for (int i = 0; i < iterations; ++i) {
            slowIt.setSource(s);
            while (true) {
                String item = slowIt.next();
                if (item == null) break;
                x += item.length();
            }
        }
        end = System.currentTimeMillis();
        double slowDelta = (end-start) / iterations;
        logln("Slow iteration: " + slowDelta);
        */

        start = System.currentTimeMillis();
        for (int i = 0; i < iterations; ++i) {
            it.setSource(s);
            while (true) {
                String item = it.next();
                if (item == null) break;
                x += item.length();
            }
        }
        end = System.currentTimeMillis();
        double fastDelta = (end-start) / iterations;
        logln("Fast iteration: " + fastDelta + (slowDelta != 0 ? ", " + (fastDelta/slowDelta) : ""));
        
        
        return x;
    }
    
    @Test
    public void TestBasic() {
//      This is not interesting anymore as the data is already built 
//      beforehand

//        check build
//        UnicodeSet ss = CanonicalIterator.getSafeStart();
//        logln("Safe Start: " + ss.toPattern(true));
//        ss = CanonicalIterator.getStarts('a');
//        expectEqual("Characters with 'a' at the start of their decomposition: ", "", CanonicalIterator.getStarts('a'),
//            new UnicodeSet("[\u00E0-\u00E5\u0101\u0103\u0105\u01CE\u01DF\u01E1\u01FB"
//            + "\u0201\u0203\u0227\u1E01\u1EA1\u1EA3\u1EA5\u1EA7\u1EA9\u1EAB\u1EAD\u1EAF\u1EB1\u1EB3\u1EB5\u1EB7]")
//                );
        
        // check permute
        // NOTE: we use a TreeSet below to sort the output, which is not guaranteed to be sorted!
        
        Set results = new TreeSet();
        CanonicalIterator.permute("ABC", false, results);
        expectEqual("Simple permutation ", "", collectionToString(results), "ABC, ACB, BAC, BCA, CAB, CBA");
        
        // try samples
        SortedSet set = new TreeSet();
        for (int i = 0; i < testArray.length; ++i) {
            //logln("Results for: " + name.transliterate(testArray[i]));
            CanonicalIterator it = new CanonicalIterator(testArray[i][0]);
           // int counter = 0;
            set.clear();
            String first = null;
            while (true) {
                String result = it.next();
                if(first==null){
                    first = result;
                }
                if (result == null) break;
                set.add(result); // sort them
                //logln(++counter + ": " + hex.transliterate(result));
                //logln(" = " + name.transliterate(result));
            }
            expectEqual(i + ": ", testArray[i][0], collectionToString(set), testArray[i][1]);
            it.reset();
            if(!it.next().equals(first)){
                errln("CanonicalIterator.reset() failed");
            }
            if(!it.getSource().equals(Normalizer.normalize(testArray[i][0],Normalizer.NFD))){
                errln("CanonicalIterator.getSource() does not return NFD of input source");
            }
        }
    }
    
    private void expectEqual(String message, String item, Object a, Object b) {
        if (!a.equals(b)) {
            errln("FAIL: " + message + getReadable(item));
            errln("\t" + getReadable(a));
            errln("\t" + getReadable(b));
        } else {
            logln("Checked: " + message + getReadable(item));
            logln("\t" + getReadable(a));
            logln("\t" + getReadable(b));
        }
    }
    
    //Transliterator name = null;
    //Transliterator hex = null;
        
    public String getReadable(Object obj) {
        if (obj == null) return "null";
        String s = obj.toString();
        if (s.length() == 0) return "";
        // set up for readable display
        //if (name == null) name = Transliterator.getInstance("[^\\ -\\u007F] name");
        //if (hex == null) hex = Transliterator.getInstance("[^\\ -\\u007F] hex");
        return "[" + (SHOW_NAMES ? hex(s) + "; " : "") + hex(s) + "]";
    }
    
    private void characterTest(String s, int ch, CanonicalIterator it)
    {
        int mixedCounter = 0;
        int lastMixedCounter = -1;
        boolean gotDecomp = false;
        boolean gotComp = false;
        boolean gotSource = false;
        String decomp = Normalizer.decompose(s, false);
        String comp = Normalizer.compose(s, false);
        
        // skip characters that don't have either decomp.
        // need quick test for this!
        if (s.equals(decomp) && s.equals(comp)) return;
        
        it.setSource(s);

        while (true) {
            String item = it.next();
            if (item == null) break;
            if (item.equals(s)) gotSource = true;
            if (item.equals(decomp)) gotDecomp = true;
            if (item.equals(comp)) gotComp = true;
            if ((mixedCounter & 0x7F) == 0 && (ch < 0xAD00 || ch > 0xAC00 + 11172)) {
                if (lastMixedCounter != mixedCounter) {
                    logln("");
                    lastMixedCounter = mixedCounter;
                }
                logln("\t" + mixedCounter + "\t" + hex(item)
                + (item.equals(s) ? "\t(*original*)" : "")
                + (item.equals(decomp) ? "\t(*decomp*)" : "")
                + (item.equals(comp) ? "\t(*comp*)" : "")
                );
            }

        }
        
        // check that zeros optimization doesn't mess up.
        /*
        if (true) {
            it.reset();
            itSet.clear();
            while (true) {
                String item = it.next();
                if (item == null) break;
                itSet.add(item);
            }
            slowIt.setSource(s);
            slowItSet.clear();
            while (true) {
                String item = slowIt.next();
                if (item == null) break;
                slowItSet.add(item);
            }
            if (!itSet.equals(slowItSet)) {
                errln("Zero optimization failure with " + getReadable(s));
            }
        }
        */
        
        mixedCounter++;
        if (!gotSource || !gotDecomp || !gotComp) {
            errln("FAIL CanonicalIterator: " + s + " decomp: " +decomp+" comp: "+comp);
            it.reset();
            for(String item=it.next();item!=null;item=it.next()){
                err(item + "    ");
            }
            errln("");   
        }
    }
    
    static String collectionToString(Collection col) {
        StringBuffer result = new StringBuffer();
        Iterator it = col.iterator();
        while (it.hasNext()) {
            if (result.length() != 0) result.append(", ");
            result.append(it.next().toString());
        }
        return result.toString();
    }
}