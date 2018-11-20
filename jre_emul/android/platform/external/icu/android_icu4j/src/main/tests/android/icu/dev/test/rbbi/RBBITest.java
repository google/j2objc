/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.dev.test.rbbi;

//Regression testing of RuleBasedBreakIterator
//
//  TODO:  These tests should be mostly retired.
//          Much of the test data that was originally here was removed when the RBBI rules
//            were updated to match the Unicode boundary TRs, and the data was found to be invalid.
//          Much of the remaining data has been moved into the rbbitst.txt test data file,
//            which is common between ICU4C and ICU4J.  The remaining test data should also be moved,
//            or simply retired if it is no longer interesting.
import java.text.CharacterIterator;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.BreakIterator;
import android.icu.text.RuleBasedBreakIterator;
import android.icu.util.ULocale;

public class RBBITest extends TestFmwk {
    public RBBITest() {
    }

    private static final String halfNA = "\u0928\u094d\u200d"; /*
                                                                * halfform NA = devanigiri NA + virama(supresses
                                                                * inherent vowel)+ zero width joiner
                                                                */

    // tests default rules based character iteration.
    // Builds a new iterator from the source rules in the default (prebuilt) iterator.
    //
    @Test
    public void TestDefaultRuleBasedCharacterIteration() {
        RuleBasedBreakIterator rbbi = (RuleBasedBreakIterator) BreakIterator.getCharacterInstance();
        logln("Testing the RBBI for character iteration by using default rules");

        // fetch the rules used to create the above RuleBasedBreakIterator
        String defaultRules = rbbi.toString();

        RuleBasedBreakIterator charIterDefault = null;
        try {
            charIterDefault = new RuleBasedBreakIterator(defaultRules);
        } catch (IllegalArgumentException iae) {
            errln("ERROR: failed construction in TestDefaultRuleBasedCharacterIteration()" + iae.toString());
        }

        List<String> chardata = new ArrayList<String>();
        chardata.add("H");
        chardata.add("e");
        chardata.add("l");
        chardata.add("l");
        chardata.add("o");
        chardata.add("e\u0301"); // acuteE
        chardata.add("&");
        chardata.add("e\u0303"); // tildaE
        // devanagiri characters for Hindi support
        chardata.add("\u0906"); // devanagiri AA
        // chardata.add("\u093e\u0901"); //devanagiri vowelsign AA+ chandrabindhu
        chardata.add("\u0916\u0947"); // devanagiri KHA+vowelsign E
        chardata.add("\u0938\u0941\u0902"); // devanagiri SA+vowelsign U + anusvara(bindu)
        chardata.add("\u0926"); // devanagiri consonant DA
        chardata.add("\u0930"); // devanagiri consonant RA
        // chardata.add("\u0939\u094c"); //devanagiri HA+vowel sign AI
        chardata.add("\u0964"); // devanagiri danda
        // end hindi characters
        chardata.add("A\u0302"); // circumflexA
        chardata.add("i\u0301"); // acuteBelowI
        // conjoining jamo...
        chardata.add("\u1109\u1161\u11bc");
        chardata.add("\u1112\u1161\u11bc");
        chardata.add("\n");
        chardata.add("\r\n"); // keep CRLF sequences together
        chardata.add("S\u0300"); // graveS
        chardata.add("i\u0301"); // acuteBelowI
        chardata.add("!");

        // What follows is a string of Korean characters (I found it in the Yellow Pages
        // ad for the Korean Presbyterian Church of San Francisco, and I hope I transcribed
        // it correctly), first as precomposed syllables, and then as conjoining jamo.
        // Both sequences should be semantically identical and break the same way.
        // precomposed syllables...
        chardata.add("\uc0c1");
        chardata.add("\ud56d");
        chardata.add(" ");
        chardata.add("\ud55c");
        chardata.add("\uc778");
        chardata.add(" ");
        chardata.add("\uc5f0");
        chardata.add("\ud569");
        chardata.add(" ");
        chardata.add("\uc7a5");
        chardata.add("\ub85c");
        chardata.add("\uad50");
        chardata.add("\ud68c");
        chardata.add(" ");
        // conjoining jamo...
        chardata.add("\u1109\u1161\u11bc");
        chardata.add("\u1112\u1161\u11bc");
        chardata.add(" ");
        chardata.add("\u1112\u1161\u11ab");
        chardata.add("\u110b\u1175\u11ab");
        chardata.add(" ");
        chardata.add("\u110b\u1167\u11ab");
        chardata.add("\u1112\u1161\u11b8");
        chardata.add(" ");
        chardata.add("\u110c\u1161\u11bc");
        chardata.add("\u1105\u1169");
        chardata.add("\u1100\u116d");
        chardata.add("\u1112\u116c");

        generalIteratorTest(charIterDefault, chardata);

    }

    @Test
    public void TestDefaultRuleBasedWordIteration() {
        logln("Testing the RBBI for word iteration using default rules");
        RuleBasedBreakIterator rbbi = (RuleBasedBreakIterator) BreakIterator.getWordInstance();
        // fetch the rules used to create the above RuleBasedBreakIterator
        String defaultRules = rbbi.toString();

        RuleBasedBreakIterator wordIterDefault = null;
        try {
            wordIterDefault = new RuleBasedBreakIterator(defaultRules);
        } catch (IllegalArgumentException iae) {
            errln("ERROR: failed construction in TestDefaultRuleBasedWordIteration() -- custom rules" + iae.toString());
        }

        List<String> worddata = new ArrayList<String>();
        worddata.add("Write");
        worddata.add(" ");
        worddata.add("wordrules");
        worddata.add(".");
        worddata.add(" ");
        // worddata.add("alpha-beta-gamma");
        worddata.add(" ");
        worddata.add("\u092f\u0939");
        worddata.add(" ");
        worddata.add("\u0939\u093f" + halfNA + "\u0926\u0940");
        worddata.add(" ");
        worddata.add("\u0939\u0948");
        // worddata.add("\u0964"); //danda followed by a space
        worddata.add(" ");
        worddata.add("\u0905\u093e\u092a");
        worddata.add(" ");
        worddata.add("\u0938\u093f\u0916\u094b\u0917\u0947");
        worddata.add("?");
        worddata.add(" ");
        worddata.add("\r");
        worddata.add("It's");
        worddata.add(" ");
        // worddata.add("$30.10");
        worddata.add(" ");
        worddata.add(" ");
        worddata.add("Badges");
        worddata.add("?");
        worddata.add(" ");
        worddata.add("BADGES");
        worddata.add("!");
        worddata.add("1000,233,456.000");
        worddata.add(" ");

        generalIteratorTest(wordIterDefault, worddata);
    }

//    private static final String kParagraphSeparator = "\u2029";
    private static final String kLineSeparator      = "\u2028";

    @Test
    public void TestDefaultRuleBasedSentenceIteration() {
        logln("Testing the RBBI for sentence iteration using default rules");
        RuleBasedBreakIterator rbbi = (RuleBasedBreakIterator) BreakIterator.getSentenceInstance();

        // fetch the rules used to create the above RuleBasedBreakIterator
        String defaultRules = rbbi.toString();
        RuleBasedBreakIterator sentIterDefault = null;
        try {
            sentIterDefault = new RuleBasedBreakIterator(defaultRules);
        } catch (IllegalArgumentException iae) {
            errln("ERROR: failed construction in TestDefaultRuleBasedSentenceIteration()" + iae.toString());
        }

        List<String> sentdata = new ArrayList<String>();
        sentdata.add("(This is it.) ");
        sentdata.add("Testing the sentence iterator. ");
        sentdata.add("\"This isn\'t it.\" ");
        sentdata.add("Hi! ");
        sentdata.add("This is a simple sample sentence. ");
        sentdata.add("(This is it.) ");
        sentdata.add("This is a simple sample sentence. ");
        sentdata.add("\"This isn\'t it.\" ");
        sentdata.add("Hi! ");
        sentdata.add("This is a simple sample sentence. ");
        sentdata.add("It does not have to make any sense as you can see. ");
        sentdata.add("Nel mezzo del cammin di nostra vita, mi ritrovai in una selva oscura. ");
        sentdata.add("Che la dritta via aveo smarrita. ");

        generalIteratorTest(sentIterDefault, sentdata);
    }

    @Test
    public void TestDefaultRuleBasedLineIteration() {
        logln("Testing the RBBI for line iteration using default rules");
        RuleBasedBreakIterator rbbi = (RuleBasedBreakIterator) RuleBasedBreakIterator.getLineInstance();
        // fetch the rules used to create the above RuleBasedBreakIterator
        String defaultRules = rbbi.toString();
        RuleBasedBreakIterator lineIterDefault = null;
        try {
            lineIterDefault = new RuleBasedBreakIterator(defaultRules);
        } catch (IllegalArgumentException iae) {
            errln("ERROR: failed construction in TestDefaultRuleBasedLineIteration()" + iae.toString());
        }

        List<String> linedata = new ArrayList<String>();
        linedata.add("Multi-");
        linedata.add("Level ");
        linedata.add("example ");
        linedata.add("of ");
        linedata.add("a ");
        linedata.add("semi-");
        linedata.add("idiotic ");
        linedata.add("non-");
        linedata.add("sensical ");
        linedata.add("(non-");
        linedata.add("important) ");
        linedata.add("sentence. ");

        linedata.add("Hi  ");
        linedata.add("Hello ");
        linedata.add("How\n");
        linedata.add("are\r");
        linedata.add("you" + kLineSeparator);
        linedata.add("fine.\t");
        linedata.add("good.  ");

        linedata.add("Now\r");
        linedata.add("is\n");
        linedata.add("the\r\n");
        linedata.add("time\n");
        linedata.add("\r");
        linedata.add("for\r");
        linedata.add("\r");
        linedata.add("all");

        generalIteratorTest(lineIterDefault, linedata);

    }

    // =========================================================================
    // general test subroutines
    // =========================================================================

    private void generalIteratorTest(RuleBasedBreakIterator rbbi, List<String> expectedResult) {
        StringBuffer buffer = new StringBuffer();
        String text;
        for (int i = 0; i < expectedResult.size(); i++) {
            text = expectedResult.get(i);
            buffer.append(text);
        }
        text = buffer.toString();
        if (rbbi == null) {
            errln("null iterator, test skipped.");
            return;
        }

        rbbi.setText(text);

        List<String> nextResults = _testFirstAndNext(rbbi, text);
        List<String> previousResults = _testLastAndPrevious(rbbi, text);

        logln("comparing forward and backward...");
        //TODO(user) - needs to be rewritten
        //int errs = getErrorCount();
        compareFragmentLists("forward iteration", "backward iteration", nextResults, previousResults);
        //if (getErrorCount() == errs) {
        logln("comparing expected and actual...");
        compareFragmentLists("expected result", "actual result", expectedResult, nextResults);
            logln("comparing expected and actual...");
            compareFragmentLists("expected result", "actual result", expectedResult, nextResults);
        //}

        int[] boundaries = new int[expectedResult.size() + 3];
        boundaries[0] = RuleBasedBreakIterator.DONE;
        boundaries[1] = 0;
        for (int i = 0; i < expectedResult.size(); i++) {
            boundaries[i + 2] = boundaries[i + 1] + (expectedResult.get(i).length());
        }

        boundaries[boundaries.length - 1] = RuleBasedBreakIterator.DONE;

        _testFollowing(rbbi, text, boundaries);
        _testPreceding(rbbi, text, boundaries);
        _testIsBoundary(rbbi, text, boundaries);

        doMultipleSelectionTest(rbbi, text);
    }

     private List<String> _testFirstAndNext(RuleBasedBreakIterator rbbi, String text) {
         int p = rbbi.first();
         int lastP = p;
         List<String> result = new ArrayList<String>();

         if (p != 0) {
             errln("first() returned " + p + " instead of 0");
         }

         while (p != RuleBasedBreakIterator.DONE) {
             p = rbbi.next();
             if (p != RuleBasedBreakIterator.DONE) {
                 if (p <= lastP) {
                     errln("next() failed to move forward: next() on position "
                                     + lastP + " yielded " + p);
                 }
                 result.add(text.substring(lastP, p));
             }
             else {
                 if (lastP != text.length()) {
                     errln("next() returned DONE prematurely: offset was "
                                     + lastP + " instead of " + text.length());
                 }
             }
             lastP = p;
         }
         return result;
     }

     private List<String> _testLastAndPrevious(RuleBasedBreakIterator rbbi, String text) {
         int p = rbbi.last();
         int lastP = p;
         List<String> result = new ArrayList<String>();

         if (p != text.length()) {
             errln("last() returned " + p + " instead of " + text.length());
         }

         while (p != RuleBasedBreakIterator.DONE) {
             p = rbbi.previous();
             if (p != RuleBasedBreakIterator.DONE) {
                 if (p >= lastP) {
                     errln("previous() failed to move backward: previous() on position "
                                     + lastP + " yielded " + p);
                 }

                 result.add(0, text.substring(p, lastP));
             }
             else {
                 if (lastP != 0) {
                     errln("previous() returned DONE prematurely: offset was "
                                     + lastP + " instead of 0");
                 }
             }
             lastP = p;
         }
         return result;
     }

     private void compareFragmentLists(String f1Name, String f2Name, List<String> f1, List<String> f2) {
         int p1 = 0;
         int p2 = 0;
         String s1;
         String s2;
         int t1 = 0;
         int t2 = 0;

         while (p1 < f1.size() && p2 < f2.size()) {
             s1 = f1.get(p1);
             s2 = f2.get(p2);
             t1 += s1.length();
             t2 += s2.length();

             if (s1.equals(s2)) {
                 debugLogln("   >" + s1 + "<");
                 ++p1;
                 ++p2;
             }
             else {
                 int tempT1 = t1;
                 int tempT2 = t2;
                 int tempP1 = p1;
                 int tempP2 = p2;

                 while (tempT1 != tempT2 && tempP1 < f1.size() && tempP2 < f2.size()) {
                     while (tempT1 < tempT2 && tempP1 < f1.size()) {
                         tempT1 += (f1.get(tempP1)).length();
                         ++tempP1;
                     }
                     while (tempT2 < tempT1 && tempP2 < f2.size()) {
                         tempT2 += (f2.get(tempP2)).length();
                         ++tempP2;
                     }
                 }
                 logln("*** " + f1Name + " has:");
                 while (p1 <= tempP1 && p1 < f1.size()) {
                     s1 = f1.get(p1);
                     t1 += s1.length();
                     debugLogln(" *** >" + s1 + "<");
                     ++p1;
                 }
                 logln("***** " + f2Name + " has:");
                 while (p2 <= tempP2 && p2 < f2.size()) {
                     s2 = f2.get(p2);
                     t2 += s2.length();
                     debugLogln(" ***** >" + s2 + "<");
                     ++p2;
                 }
                 errln("Discrepancy between " + f1Name + " and " + f2Name);
             }
         }
     }

    private void _testFollowing(RuleBasedBreakIterator rbbi, String text, int[] boundaries) {
       logln("testFollowing():");
       int p = 2;
       for(int i = 0; i <= text.length(); i++) {
           if (i == boundaries[p])
               ++p;
           int b = rbbi.following(i);
           logln("rbbi.following(" + i + ") -> " + b);
           if (b != boundaries[p])
               errln("Wrong result from following() for " + i + ": expected " + boundaries[p]
                               + ", got " + b);
       }
   }

   private void _testPreceding(RuleBasedBreakIterator rbbi, String text, int[] boundaries) {
       logln("testPreceding():");
       int p = 0;
       for(int i = 0; i <= text.length(); i++) {
           int b = rbbi.preceding(i);
           logln("rbbi.preceding(" + i + ") -> " + b);
           if (b != boundaries[p])
               errln("Wrong result from preceding() for " + i + ": expected " + boundaries[p]
                              + ", got " + b);
           if (i == boundaries[p + 1])
               ++p;
       }
   }

   private void _testIsBoundary(RuleBasedBreakIterator rbbi, String text, int[] boundaries) {
       logln("testIsBoundary():");
       int p = 1;
       boolean isB;
       for(int i = 0; i <= text.length(); i++) {
           isB = rbbi.isBoundary(i);
           logln("rbbi.isBoundary(" + i + ") -> " + isB);
           if(i == boundaries[p]) {
               if (!isB)
                   errln("Wrong result from isBoundary() for " + i + ": expected true, got false");
               ++p;
           }
           else {
               if(isB)
                   errln("Wrong result from isBoundary() for " + i + ": expected false, got true");
           }
       }
   }
   private void doMultipleSelectionTest(RuleBasedBreakIterator iterator, String testText)
   {
       logln("Multiple selection test...");
       RuleBasedBreakIterator testIterator = (RuleBasedBreakIterator)iterator.clone();
       int offset = iterator.first();
       int testOffset;
       int count = 0;

       do {
           testOffset = testIterator.first();
           testOffset = testIterator.next(count);
           logln("next(" + count + ") -> " + testOffset);
           if (offset != testOffset)
               errln("next(n) and next() not returning consistent results: for step " + count + ", next(n) returned " + testOffset + " and next() had " + offset);

           if (offset != RuleBasedBreakIterator.DONE) {
               count++;
               offset = iterator.next();
           }
       } while (offset != RuleBasedBreakIterator.DONE);

       // now do it backwards...
       offset = iterator.last();
       count = 0;

       do {
           testOffset = testIterator.last();
           testOffset = testIterator.next(count);
           logln("next(" + count + ") -> " + testOffset);
           if (offset != testOffset)
               errln("next(n) and next() not returning consistent results: for step " + count + ", next(n) returned " + testOffset + " and next() had " + offset);

           if (offset != RuleBasedBreakIterator.DONE) {
               count--;
               offset = iterator.previous();
           }
       } while (offset != RuleBasedBreakIterator.DONE);
   }

   private void debugLogln(String s) {
        final String zeros = "0000";
        String temp;
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= ' ' && c < '\u007f')
                out.append(c);
            else {
                out.append("\\u");
                temp = Integer.toHexString(c);
                out.append(zeros.substring(0, 4 - temp.length()));
                out.append(temp);
            }
        }
         logln(out.toString());
    }

    @Test
   public void TestThaiDictionaryBreakIterator() {
       int position;
       int index;
       int result[] = { 1, 2, 5, 10, 11, 12, 11, 10, 5, 2, 1, 0 };
       char ctext[] = {
               0x0041, 0x0020,
               0x0E01, 0x0E32, 0x0E23, 0x0E17, 0x0E14, 0x0E25, 0x0E2D, 0x0E07,
               0x0020, 0x0041
               };
       String text = new String(ctext);

       ULocale locale = ULocale.createCanonical("th");
       BreakIterator b = BreakIterator.getWordInstance(locale);

       b.setText(text);

       index = 0;
       // Test forward iteration
       while ((position = b.next())!= BreakIterator.DONE) {
           if (position != result[index++]) {
               errln("Error with ThaiDictionaryBreakIterator forward iteration test at " + position + ".\nShould have been " + result[index-1]);
           }
       }

       // Test backward iteration
       while ((position = b.previous())!= BreakIterator.DONE) {
           if (position != result[index++]) {
               errln("Error with ThaiDictionaryBreakIterator backward iteration test at " + position + ".\nShould have been " + result[index-1]);
           }
       }

       //Test invalid sequence and spaces
       char text2[] = {
               0x0E01, 0x0E39, 0x0020, 0x0E01, 0x0E34, 0x0E19, 0x0E01, 0x0E38, 0x0E49, 0x0E07, 0x0020, 0x0E1B,
               0x0E34, 0x0E49, 0x0E48, 0x0E07, 0x0E2D, 0x0E22, 0x0E39, 0x0E48, 0x0E43, 0x0E19,
               0x0E16, 0x0E49, 0x0E33
       };
       int expectedWordResult[] = {
               2, 3, 6, 10, 11, 15, 17, 20, 22
       };
       int expectedLineResult[] = {
               3, 6, 11, 15, 17, 20, 22
       };
       BreakIterator brk = BreakIterator.getWordInstance(new ULocale("th"));
       brk.setText(new String(text2));
       position = index = 0;
       while ((position = brk.next()) != BreakIterator.DONE && position < text2.length) {
           if (position != expectedWordResult[index++]) {
               errln("Incorrect break given by thai word break iterator. Expected: " + expectedWordResult[index-1] + " Got: " + position);
           }
       }

       brk = BreakIterator.getLineInstance(new ULocale("th"));
       brk.setText(new String(text2));
       position = index = 0;
       while ((position = brk.next()) != BreakIterator.DONE && position < text2.length) {
           if (position != expectedLineResult[index++]) {
               errln("Incorrect break given by thai line break iterator. Expected: " + expectedLineResult[index-1] + " Got: " + position);
           }
       }
       // Improve code coverage
       if (brk.preceding(expectedLineResult[1]) != expectedLineResult[0]) {
           errln("Incorrect preceding position.");
       }
       if (brk.following(expectedLineResult[1]) != expectedLineResult[2]) {
           errln("Incorrect following position.");
       }
       int []fillInArray = new int[2];
       if (((RuleBasedBreakIterator)brk).getRuleStatusVec(fillInArray) != 1 || fillInArray[0] != 0) {
           errln("Error: Since getRuleStatusVec is not supported in DictionaryBasedBreakIterator, it should return 1 and fillInArray[0] == 0.");
       }
   }


   // TODO: Move these test cases to rbbitst.txt if they aren't there already, then remove this test. It is redundant.
    @Test
    public void TestTailoredBreaks() {
        class TBItem {
            private int     type;
            private ULocale locale;
            private String  text;
            private int[]   expectOffsets;
            TBItem(int typ, ULocale loc, String txt, int[] eOffs) {
                type          = typ;
                locale        = loc;
                text          = txt;
                expectOffsets = eOffs;
            }
            private static final int maxOffsetCount = 128;
            private boolean offsetsMatchExpected(int[] foundOffsets, int foundOffsetsLength) {
                if ( foundOffsetsLength != expectOffsets.length ) {
                    return false;
                }
                for (int i = 0; i < foundOffsetsLength; i++) {
                    if ( foundOffsets[i] != expectOffsets[i] ) {
                        return false;
                    }
                }
                return true;
            }
            private String formatOffsets(int[] offsets, int length) {
                StringBuffer buildString = new StringBuffer(4*maxOffsetCount);
                for (int i = 0; i < length; i++) {
                    buildString.append(" " + offsets[i]);
                }
                return buildString.toString();
            }
    @Test
            public void doTest() {
                BreakIterator brkIter;
                switch( type ) {
                    case BreakIterator.KIND_CHARACTER: brkIter = BreakIterator.getCharacterInstance(locale); break;
                    case BreakIterator.KIND_WORD:      brkIter = BreakIterator.getWordInstance(locale); break;
                    case BreakIterator.KIND_LINE:      brkIter = BreakIterator.getLineInstance(locale); break;
                    case BreakIterator.KIND_SENTENCE:  brkIter = BreakIterator.getSentenceInstance(locale); break;
                    default: errln("Unsupported break iterator type " + type); return;
                }
                brkIter.setText(text);
                int[] foundOffsets = new int[maxOffsetCount];
                int offset, foundOffsetsCount = 0;
                // do forwards iteration test
                while ( foundOffsetsCount < maxOffsetCount && (offset = brkIter.next()) != BreakIterator.DONE ) {
                    foundOffsets[foundOffsetsCount++] = offset;
                }
                if ( !offsetsMatchExpected(foundOffsets, foundOffsetsCount) ) {
                    // log error for forwards test
                    String textToDisplay = (text.length() <= 16)? text: text.substring(0,16);
                    errln("For type " + type + " " + locale + ", text \"" + textToDisplay + "...\"" +
                            "; expect " + expectOffsets.length + " offsets:" + formatOffsets(expectOffsets, expectOffsets.length) +
                            "; found " + foundOffsetsCount + " offsets fwd:" + formatOffsets(foundOffsets, foundOffsetsCount) );
                } else {
                    // do backwards iteration test
                    --foundOffsetsCount; // back off one from the end offset
                    while ( foundOffsetsCount > 0 ) {
                        offset = brkIter.previous();
                        if ( offset != foundOffsets[--foundOffsetsCount] ) {
                            // log error for backwards test
                            String textToDisplay = (text.length() <= 16)? text: text.substring(0,16);
                            errln("For type " + type + " " + locale + ", text \"" + textToDisplay + "...\"" +
                                    "; expect " + expectOffsets.length + " offsets:" + formatOffsets(expectOffsets, expectOffsets.length) +
                                    "; found rev offset " + offset + " where expect " + foundOffsets[foundOffsetsCount] );
                            break;
                        }
                    }
                }
            }
        }
        // KIND_SENTENCE "el"
        final String elSentText     = "\u0391\u03B2, \u03B3\u03B4; \u0395 \u03B6\u03B7\u037E \u0398 \u03B9\u03BA. " +
                                      "\u039B\u03BC \u03BD\u03BE! \u039F\u03C0, \u03A1\u03C2? \u03A3";
        final int[]  elSentTOffsets = { 8, 14, 20, 27, 35, 36 };
        final int[]  elSentROffsets = {        20, 27, 35, 36 };
        // KIND_CHARACTER "th"
        final String thCharText     = "\u0E01\u0E23\u0E30\u0E17\u0E48\u0E2D\u0E21\u0E23\u0E08\u0E19\u0E32 " +
                                      "(\u0E2A\u0E38\u0E0A\u0E32\u0E15\u0E34-\u0E08\u0E38\u0E11\u0E32\u0E21\u0E32\u0E28) " +
                                      "\u0E40\u0E14\u0E47\u0E01\u0E21\u0E35\u0E1B\u0E31\u0E0D\u0E2B\u0E32 ";
        final int[]  thCharTOffsets = { 1, 2, 3, 5, 6, 7, 8, 9, 10, 11,
                                        12, 13, 15, 16, 17, 19, 20, 22, 23, 24, 25, 26, 27, 28,
                                        29, 30, 32, 33, 35, 37, 38, 39, 40, 41 };
        //starting in Unicode 6.1, root behavior should be the same as Thai above
        //final int[]  thCharROffsets = { 1,    3, 5, 6, 7, 8, 9,     11,
        //                                12, 13, 15,     17, 19, 20, 22,     24,     26, 27, 28,
        //                                29,     32, 33, 35, 37, 38,     40, 41 };

        final TBItem[] tests = {
            new TBItem( BreakIterator.KIND_SENTENCE,  new ULocale("el"),          elSentText,   elSentTOffsets   ),
            new TBItem( BreakIterator.KIND_SENTENCE,  ULocale.ROOT,               elSentText,   elSentROffsets   ),
            new TBItem( BreakIterator.KIND_CHARACTER, new ULocale("th"),          thCharText,   thCharTOffsets   ),
            new TBItem( BreakIterator.KIND_CHARACTER, ULocale.ROOT,               thCharText,   thCharTOffsets   ),
        };
        for (int iTest = 0; iTest < tests.length; iTest++) {
            tests[iTest].doTest();
        }
    }

    /* Tests the method public Object clone() */
    @Test
    public void TestClone() {
        RuleBasedBreakIterator rbbi = new RuleBasedBreakIterator(".;");
        try {
            rbbi.setText((CharacterIterator) null);
            if (((RuleBasedBreakIterator) rbbi.clone()).getText() != null)
                errln("RuleBasedBreakIterator.clone() was suppose to return "
                        + "the same object because fText is set to null.");
        } catch (Exception e) {
            errln("RuleBasedBreakIterator.clone() was not suppose to return " + "an exception.");
        }
    }

    /*
     * Tests the method public boolean equals(Object that)
     */
    @Test
    public void TestEquals() {
        RuleBasedBreakIterator rbbi = new RuleBasedBreakIterator(".;");
        RuleBasedBreakIterator rbbi1 = new RuleBasedBreakIterator(".;");

        // TODO: Tests when "if (fRData != other.fRData && (fRData == null || other.fRData == null))" is true

        // Tests when "if (fText == null || other.fText == null)" is true
        rbbi.setText((CharacterIterator) null);
        if (rbbi.equals(rbbi1)) {
            errln("RuleBasedBreakIterator.equals(Object) was not suppose to return "
                    + "true when the other object has a null fText.");
        }

        // Tests when "if (fText == null && other.fText == null)" is true
        rbbi1.setText((CharacterIterator) null);
        if (!rbbi.equals(rbbi1)) {
            errln("RuleBasedBreakIterator.equals(Object) was not suppose to return "
                    + "false when both objects has a null fText.");
        }

        // Tests when an exception occurs
        if (rbbi.equals(0)) {
            errln("RuleBasedBreakIterator.equals(Object) was suppose to return " + "false when comparing to integer 0.");
        }
        if (rbbi.equals(0.0)) {
            errln("RuleBasedBreakIterator.equals(Object) was suppose to return " + "false when comparing to float 0.0.");
        }
        if (rbbi.equals("0")) {
            errln("RuleBasedBreakIterator.equals(Object) was suppose to return "
                    + "false when comparing to string '0'.");
        }
    }

    /*
     * Tests the method public int first()
     */
    @Test
    public void TestFirst() {
        RuleBasedBreakIterator rbbi = new RuleBasedBreakIterator(".;");
        // Tests when "if (fText == null)" is true
        rbbi.setText((CharacterIterator) null);
        assertEquals("RuleBasedBreakIterator.first()", BreakIterator.DONE, rbbi.first());

        rbbi.setText("abc");
        assertEquals("RuleBasedBreakIterator.first()", 0, rbbi.first());
        assertEquals("RuleBasedBreakIterator.next()", 1, rbbi.next());
    }

    /*
     * Tests the method public int last()
     */
    @Test
    public void TestLast() {
        RuleBasedBreakIterator rbbi = new RuleBasedBreakIterator(".;");
        // Tests when "if (fText == null)" is true
        rbbi.setText((CharacterIterator) null);
        if (rbbi.last() != BreakIterator.DONE) {
            errln("RuleBasedBreakIterator.last() was suppose to return "
                    + "BreakIterator.DONE when the object has a null fText.");
        }
    }

    /*
     * Tests the method public int following(int offset)
     */
    @Test
    public void TestFollowing() {
        RuleBasedBreakIterator rbbi = new RuleBasedBreakIterator(".;");
        // Tests when "else if (offset < fText.getBeginIndex())" is true
        rbbi.setText("dummy");
        if (rbbi.following(-1) != 0) {
            errln("RuleBasedBreakIterator.following(-1) was suppose to return "
                    + "0 when the object has a fText of dummy.");
        }
    }

    /*
     * Tests the method public int preceding(int offset)
     */
    @Test
    public void TestPreceding() {
        RuleBasedBreakIterator rbbi = new RuleBasedBreakIterator(".;");
        // Tests when "if (fText == null || offset > fText.getEndIndex())" is true
        rbbi.setText((CharacterIterator)null);
        if (rbbi.preceding(-1) != BreakIterator.DONE) {
            errln("RuleBasedBreakIterator.preceding(-1) was suppose to return "
                    + "0 when the object has a fText of null.");
        }

        // Tests when "else if (offset < fText.getBeginIndex())" is true
        rbbi.setText("dummy");
        if (rbbi.preceding(-1) != 0) {
            errln("RuleBasedBreakIterator.preceding(-1) was suppose to return "
                    + "0 when the object has a fText of dummy.");
        }
    }

    /* Tests the method public int current() */
    @Test
    public void TestCurrent(){
        RuleBasedBreakIterator rbbi = new RuleBasedBreakIterator(".;");
        // Tests when "(fText != null) ? fText.getIndex() : BreakIterator.DONE" is true and false
        rbbi.setText((CharacterIterator)null);
        if(rbbi.current() != BreakIterator.DONE){
            errln("RuleBasedBreakIterator.current() was suppose to return "
                    + "BreakIterator.DONE when the object has a fText of null.");
        }
        rbbi.setText("dummy");
        if(rbbi.current() != 0){
            errln("RuleBasedBreakIterator.current() was suppose to return "
                    + "0 when the object has a fText of dummy.");
        }
    }

    @Test
    public void TestBug7547() {
        try {
            new RuleBasedBreakIterator("");
            fail("TestBug7547: RuleBasedBreakIterator constructor failed to throw an exception with empty rules.");
        }
        catch (IllegalArgumentException e) {
            // expected exception with empty rules.
        }
        catch (Exception e) {
            fail("TestBug7547: Unexpected exception while creating RuleBasedBreakIterator: " + e);
        }
    }

    @Test
    public void TestBug12797() {
        String rules = "!!chain; !!forward; $v=b c; a b; $v; !!reverse; .*;";
        RuleBasedBreakIterator bi = new RuleBasedBreakIterator(rules);

        bi.setText("abc");
        bi.first();
        assertEquals("Rule chaining test", 3,  bi.next());
         }
    }
