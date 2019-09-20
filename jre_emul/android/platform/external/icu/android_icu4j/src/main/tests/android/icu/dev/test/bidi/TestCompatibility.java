/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
*   Copyright (C) 2007-2010, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*/

package android.icu.dev.test.bidi;

import java.awt.font.NumericShaper;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import org.junit.Test;

import android.icu.text.Bidi;

/**
 * Regression test for java.text.Bidi compatibility
 *
 * @author Matitiahu Allouche
 */

public class TestCompatibility extends BidiFmwk {

    void compareBidi(Bidi bidi, java.text.Bidi jbidi)
    {
        byte paraLevel = bidi.getParaLevel();
        if (bidi.baseIsLeftToRight() != jbidi.baseIsLeftToRight()) {
            errln("Discrepancy in baseIsLeftToRight for case " +
                  "(level=" + paraLevel + "): " +
                  u16ToPseudo(bidi.getTextAsString()) +
                  "\n    bidi: " + bidi.baseIsLeftToRight() +
                  "\n   jbidi: " + jbidi.baseIsLeftToRight());
        }
        if (bidi.getBaseLevel() != jbidi.getBaseLevel()) {
            errln("Discrepancy in getBaseLevel for case " +
                  "(level=" + paraLevel + "): " +
                  u16ToPseudo(bidi.getTextAsString()) +
                  "\n    bidi: " + bidi.getBaseLevel() +
                  "\n   jbidi: " + jbidi.getBaseLevel());
        }
        if (bidi.getLength() != jbidi.getLength()) {
            errln("Discrepancy in getLength for case " +
                  "(level=" + paraLevel + "): " +
                  u16ToPseudo(bidi.getTextAsString()) +
                  "\n    bidi: " + bidi.getLength() +
                  "\n   jbidi: " + jbidi.getLength());
        }
        int len = bidi.getLength();
        for (int i = 0; i < len; i++) {
            if (bidi.getLevelAt(i) != jbidi.getLevelAt(i)) {
                errln("Discrepancy in getLevelAt for offset " + i +
                      " of case " +
                      "(level=" + paraLevel + "): " +
                      u16ToPseudo(bidi.getTextAsString()) +
                      "\n    bidi: " + bidi.getLevelAt(i) +
                      "\n   jbidi: " + jbidi.getLevelAt(i));
            }
        }
        if (bidi.getRunCount() != jbidi.getRunCount()) {
            if (!(len == 0 && jbidi.getRunCount() == 1)) {
                errln("Discrepancy in getRunCount for case " +
                      "(level=" + paraLevel + "): " +
                      u16ToPseudo(bidi.getTextAsString()) +
                      "\n    bidi: " + bidi.getRunCount() +
                      "\n   jbidi: " + jbidi.getRunCount());
            }
        }
        int runCount = bidi.getRunCount();
        for (int i = 0; i < runCount; i++) {
            if (bidi.getRunLevel(i) != jbidi.getRunLevel(i)) {
                errln("Discrepancy in getRunLevel for run " + i +
                      " of case " +
                      "(level=" + paraLevel + "): " +
                      u16ToPseudo(bidi.getTextAsString()) +
                      "\n    bidi: " + bidi.getRunLevel(i) +
                      "\n   jbidi: " + jbidi.getRunLevel(i));
            }
            if (bidi.getRunLimit(i) != jbidi.getRunLimit(i)) {
                errln("Discrepancy in getRunLimit for run " + i +
                      " of case " +
                      "(level=" + paraLevel + "): " +
                      u16ToPseudo(bidi.getTextAsString()) +
                      "\n    bidi: " + bidi.getRunLimit(i) +
                      "\n   jbidi: " + jbidi.getRunLimit(i));
            }
            if (bidi.getRunStart(i) != jbidi.getRunStart(i)) {
                errln("Discrepancy in getRunStart for run " + i +
                      " of case " +
                      "(level=" + paraLevel + "): " +
                      u16ToPseudo(bidi.getTextAsString()) +
                      "\n    bidi: " + bidi.getRunStart(i) +
                      "\n   jbidi: " + jbidi.getRunStart(i));
            }
        }
        if (bidi.isLeftToRight() != jbidi.isLeftToRight()) {
            errln("Discrepancy in isLeftToRight for case " +
                  "(level=" + paraLevel + "): " +
                  u16ToPseudo(bidi.getTextAsString()) +
                  "\n    bidi: " + bidi.isLeftToRight() +
                  "\n   jbidi: " + jbidi.isLeftToRight());
        }
        if (bidi.isMixed() != jbidi.isMixed()) {
            errln("Discrepancy in isMixed for case " +
                  "(level=" + paraLevel + "): " +
                  u16ToPseudo(bidi.getTextAsString()) +
                  "\n    bidi: " + bidi.isMixed() +
                  "\n   jbidi: " + jbidi.isMixed());
        }
        if (bidi.isRightToLeft() != jbidi.isRightToLeft()) {
            errln("Discrepancy in isRightToLeft for case " +
                  "(level=" + paraLevel + "): " +
                  u16ToPseudo(bidi.getTextAsString()) +
                  "\n    bidi: " + bidi.isRightToLeft() +
                  "\n   jbidi: " + jbidi.isRightToLeft());
        }
        char[] text = bidi.getText();
        if (Bidi.requiresBidi(text, 0, text.length) !=
            java.text.Bidi.requiresBidi(text, 0, text.length)) {
            errln("Discrepancy in requiresBidi for case " +
                  u16ToPseudo(bidi.getTextAsString()) +
                  "\n    bidi: " + Bidi.requiresBidi(text, 0, text.length) +
                  "\n   jbidi: " + java.text.Bidi.requiresBidi(text, 0, text.length));
        }
        /* skip the next test, since the toString implementation are
         * not compatible
        if (!bidi.toString().equals(jbidi.toString())) {
            errln("Discrepancy in toString for case " +
                  "(level=" + paraLevel + "): " +
                  u16ToPseudo(bidi.getTextAsString() +
                  "\n    bidi: " + bidi.toString() +
                  "\n   jbidi: " + jbidi.toString()));
        }
         */
    }

    @Test
    public void testCompatibility()
    {
        // This test case does not work well on Java 1.4/1.4.1 environment,
        // because of insufficient Bidi implementation in these versions.
        // This test case also does not work will with Java 1.7 environment,
        // because the changes to the Java Bidi implementation.
        String javaVersion = System.getProperty("java.version");
        if (javaVersion.startsWith("1.4.0") || javaVersion.startsWith("1.4.1") || javaVersion.startsWith("1.7")) {
            logln("\nSkipping TestCompatibility.  The test case is known to fail on Java "
                    + javaVersion + "\n");
            return;
        }
        logln("\nEntering TestCompatibility\n");
        /* check constant field values */
        int val;
        val = Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT;
        val = Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT;
        val = Bidi.DIRECTION_LEFT_TO_RIGHT;
        val = Bidi.DIRECTION_RIGHT_TO_LEFT;
        logln("last val = " + val);

        String[] data = {"",
                         /* the following 2 cases are skipped, because
                          * java.text.Bidi has bugs which cause discrepancies
                         "abc",
                         "ABC",
                          */
                         "abc def",
                         "ABC DEF",
                         "abc 123 def",
                         "ABC 123 DEF",
                         "abc DEF ghi",
                         "abc DEF 123 xyz",
                         "abc GHIJ 12345 def KLM"
                        };
        int dataCnt = data.length;
        Bidi bidi;
        java.text.Bidi jbidi;
        for (int i = 0; i < dataCnt; i++) {
            String src = pseudoToU16(data[i]);
            bidi = new Bidi(src, Bidi.DIRECTION_LEFT_TO_RIGHT);
            jbidi = new java.text.Bidi(src, java.text.Bidi.DIRECTION_LEFT_TO_RIGHT);
            compareBidi(bidi, jbidi);
            bidi = new Bidi(src, Bidi.DIRECTION_RIGHT_TO_LEFT);
            jbidi = new java.text.Bidi(src, java.text.Bidi.DIRECTION_RIGHT_TO_LEFT);
            compareBidi(bidi, jbidi);
            char[] chars = src.toCharArray();
            bidi = new Bidi(chars, 0, null, 0, chars.length, Bidi.DIRECTION_LEFT_TO_RIGHT);
            jbidi = new java.text.Bidi(chars, 0, null, 0, chars.length, java.text.Bidi.DIRECTION_LEFT_TO_RIGHT);
            compareBidi(bidi, jbidi);
        }
        /* check bogus flags */
        bidi = new Bidi("abc", 999);
        assertEquals("\nDirection should be LTR", Bidi.LTR, bidi.getDirection());
        /* check constructor with overriding embeddings */
        bidi = new Bidi(new char[] { 's', 's', 's' }, 0,
                        new byte[] {(byte) -7, (byte) -2, (byte) -3 },
                        0, 3, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        jbidi = new java.text.Bidi(new char[] { 's', 's', 's' }, 0,
                        new byte[] {(byte) -7, (byte) -2, (byte) -3 },
                        0, 3, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);

        AttributedString as = new AttributedString("HEBREW 123 english MOREHEB");
        as.addAttribute(TextAttribute.RUN_DIRECTION, TextAttribute.RUN_DIRECTION_RTL);
        as.addAttribute(TextAttribute.NUMERIC_SHAPING, NumericShaper.getShaper(NumericShaper.ARABIC));
        as.addAttribute(TextAttribute.BIDI_EMBEDDING, new Integer(1), 0, 26);
        as.addAttribute(TextAttribute.BIDI_EMBEDDING, new Integer(-1), 0, 6);
        as.addAttribute(TextAttribute.BIDI_EMBEDDING, new Integer(-1), 19, 26);
        AttributedCharacterIterator aci = as.getIterator();
        bidi = new Bidi(aci);
        jbidi = new java.text.Bidi(aci);
        compareBidi(bidi, jbidi);
        String out = bidi.writeReordered(0);
        logln("Output #1 of Bidi(AttributedCharacterIterator): " + out);

        as = new AttributedString("HEBREW 123 english MOREHEB");
        as.addAttribute(TextAttribute.RUN_DIRECTION, TextAttribute.RUN_DIRECTION_RTL);
        as.addAttribute(TextAttribute.BIDI_EMBEDDING, new Integer(0), 0, 26);
        aci = as.getIterator();
        bidi = new Bidi(aci);
        jbidi = new java.text.Bidi(aci);
        compareBidi(bidi, jbidi);
        out = bidi.writeReordered(0);
        logln("Output #2 of Bidi(AttributedCharacterIterator): " + out);

        as = new AttributedString("HEBREW 123 english MOREHEB");
        aci = as.getIterator();
        bidi = new Bidi(aci);
        jbidi = new java.text.Bidi(aci);
        compareBidi(bidi, jbidi);
        out = bidi.writeReordered(0);
        logln("Output #3 of Bidi(AttributedCharacterIterator): " + out);

        char[] text = "abc==(123)==>def".toCharArray();
        bidi = new Bidi(text, 3, null, 0, 10, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        jbidi = new java.text.Bidi(text, 3, null, 0, 10, java.text.Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
        compareBidi(bidi, jbidi);
        out = bidi.writeReordered(0);
        logln("Output of Bidi(abc==(123)==>def,3,null,0,10, DEFAULT_LTR): " + out);
        bidi = new Bidi(text, 3, null, 0, 10, Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT);
        jbidi = new java.text.Bidi(text, 3, null, 0, 10, java.text.Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT);
        compareBidi(bidi, jbidi);
        out = bidi.writeReordered(0);
        logln("Output of Bidi(abc==(123)==>def,3,null,0,10, DEFAULT_RTL): " + out);
        byte[] levels = new byte[] {0,0,0,-1,-1,-1,0,0,0,0};
        bidi = new Bidi(text, 3, levels, 0, 10, Bidi.DIRECTION_LEFT_TO_RIGHT);
        jbidi = new java.text.Bidi(text, 3, levels, 0, 10, java.text.Bidi.DIRECTION_LEFT_TO_RIGHT);
        compareBidi(bidi, jbidi);
        out = bidi.writeReordered(0);
        logln("Output of Bidi(abc==(123)==>def,3,levels,0,10, LTR): " + out);
        bidi = new Bidi(text, 3, levels, 0, 10, Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT);
        jbidi = new java.text.Bidi(text, 3, levels, 0, 10, java.text.Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT);
        compareBidi(bidi, jbidi);
        out = bidi.writeReordered(0);
        logln("Output of Bidi(abc==(123)==>def,3,levels,0,10, DEFAULT_RTL): " + out);

        /* test reorderVisually */
        byte[] myLevels = new byte[] {1,2,0,1,2,1,2,0,1,2};
        Character[] objects = new Character[10];
        levels = new byte[objects.length];
        for (int i = 0; i < objects.length; i++) {
            objects[i] = new Character((char)('a'+i));
            levels[i] = myLevels[i];
        }
        Bidi.reorderVisually(levels, 3, objects, 3, 7);
        String strbidi = "";
        for (int i = 0; i < objects.length; i++) {
            strbidi += objects[i].toString();
        }
        for (int i = 0; i < objects.length; i++) {
            objects[i] = new Character((char)('a'+i));
            levels[i] = myLevels[i];
        }
        java.text.Bidi.reorderVisually(levels, 3, objects, 3, 7);
        String strjbidi = "";
        for (int i = 0; i < objects.length; i++) {
            strjbidi += objects[i].toString();
        }
        if (!strjbidi.equals(strbidi)) {
            errln("Discrepancy in reorderVisually " +
                  "\n      bidi: " + strbidi +
                  "\n     jbidi: " + strjbidi);
        } else {
            logln("Correct match in reorderVisually " +
                  "\n      bidi: " + strbidi +
                  "\n     jbidi: " + strjbidi);
        }

        logln("\nExiting TestCompatibility\n");
    }
}
