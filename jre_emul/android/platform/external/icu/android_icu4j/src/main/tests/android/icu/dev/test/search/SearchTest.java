/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2000-2015, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

/**
 * Port From:   ICU4C v2.1 : collate/StringSearchTest
 * Source File: $ICU4CRoot/source/test/intltest/srchtest.cpp
 **/

package android.icu.dev.test.search;

import static android.icu.text.Collator.IDENTICAL;
import static android.icu.text.Collator.PRIMARY;
import static android.icu.text.Collator.QUATERNARY;
import static android.icu.text.Collator.SECONDARY;
import static android.icu.text.Collator.TERTIARY;
import static android.icu.text.SearchIterator.ElementComparisonType.ANY_BASE_WEIGHT_IS_WILDCARD;
import static android.icu.text.SearchIterator.ElementComparisonType.PATTERN_BASE_WEIGHT_IS_WILDCARD;
import static android.icu.text.SearchIterator.ElementComparisonType.STANDARD_ELEMENT_COMPARISON;

import java.text.StringCharacterIterator;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.BreakIterator;
import android.icu.text.Collator;
import android.icu.text.RuleBasedCollator;
import android.icu.text.SearchIterator;
import android.icu.text.SearchIterator.ElementComparisonType;
import android.icu.text.StringSearch;
import android.icu.util.ULocale;

public class SearchTest extends TestFmwk {

    //inner class
    static class SearchData {
        SearchData(String text, String pattern,
                    String coll, int strength, ElementComparisonType cmpType, String breaker,
                    int[] offset, int[] size) {
            this.text = text;
            this.pattern = pattern;
            this.collator = coll;
            this.strength = strength;
            this.cmpType = cmpType;
            this.breaker = breaker;
            this.offset = offset;
            this.size = size;
        }
        String              text;
        String              pattern;
        String              collator;
        int                 strength;
        ElementComparisonType   cmpType;
        String              breaker;
        int[]               offset;
        int[]               size;
    }

    RuleBasedCollator m_en_us_;
    RuleBasedCollator m_fr_fr_;
    RuleBasedCollator m_de_;
    RuleBasedCollator m_es_;
    BreakIterator     m_en_wordbreaker_;
    BreakIterator     m_en_characterbreaker_;

    // Just calling SearchData constructor, to make the test data source code
    // nice and short
    private static SearchData SD(String text, String pattern, String coll, int strength,
                    ElementComparisonType cmpType, String breaker, int[] offset, int[] size) {
        return new SearchData(text, pattern, coll, strength, cmpType, breaker, offset, size);
    }

    // Just returning int[], to make the test data nice and short
    private static int[] IA(int... elements) {
        return elements;
    }

    static SearchData[] BASIC = {
        SD("xxxxxxxxxxxxxxxxxxxx", "fisher", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("silly spring string", "string", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(13, -1), IA(6)),
        SD("silly spring string string", "string", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(13, 20, -1), IA(6, 6)),
        SD("silly string spring string", "string", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(6, 20, -1), IA(6, 6)),
        SD("string spring string", "string", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, 14, -1), IA(6, 6)),
        SD("Scott Ganyo", "c", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(1, -1), IA(1)),
        SD("Scott Ganyo", " ", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(5, -1), IA(1)),
        SD("\u0300\u0325", "\u0300", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("a\u0300\u0325", "\u0300", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("a\u0300\u0325", "\u0300\u0325", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("a\u0300b", "\u0300", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("\u00c9", "e", null, PRIMARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, -1), IA(1)),
    };

    SearchData BREAKITERATOREXACT[] = {
        SD("foxy fox", "fox", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, "characterbreaker", IA(0, 5, -1), IA(3, 3)),
        SD("foxy fox", "fox", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, "wordbreaker", IA(5, -1), IA(3)),
        SD("This is a toe T\u00F6ne", "toe", "de", PRIMARY, STANDARD_ELEMENT_COMPARISON, "characterbreaker", IA(10, 14, -1), IA(3, 2)),
        SD("This is a toe T\u00F6ne", "toe", "de", PRIMARY, STANDARD_ELEMENT_COMPARISON, "wordbreaker", IA(10, -1), IA(3)),
        SD("Channel, another channel, more channels, and one last Channel", "Channel", "es", TERTIARY, STANDARD_ELEMENT_COMPARISON, "wordbreaker", IA(0, 54, -1), IA(7, 7)),
        /* jitterbug 1745 */
        SD("testing that \u00e9 does not match e", "e", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, "characterbreaker", IA(1, 17, 30, -1), IA(1, 1, 1)),
        SD("testing that string ab\u00e9cd does not match e", "e", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, "characterbreaker", IA(1, 28, 41, -1), IA(1, 1, 1)),
        SD("\u00c9", "e", "fr", PRIMARY, STANDARD_ELEMENT_COMPARISON, "characterbreaker", IA(0, -1), IA(1)),
    };

    SearchData BREAKITERATORCANONICAL[] = {
        SD("foxy fox", "fox", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, "characterbreaker", IA(0, 5, -1), IA(3, 3)),
        SD("foxy fox", "fox", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, "wordbreaker", IA(5, -1), IA(3)),
        SD("This is a toe T\u00F6ne", "toe", "de", PRIMARY, STANDARD_ELEMENT_COMPARISON, "characterbreaker", IA(10, 14, -1), IA(3, 2)),
        SD("This is a toe T\u00F6ne", "toe", "de", PRIMARY, STANDARD_ELEMENT_COMPARISON, "wordbreaker", IA(10, -1), IA(3)),
        SD("Channel, another channel, more channels, and one last Channel", "Channel", "es", TERTIARY, STANDARD_ELEMENT_COMPARISON, "wordbreaker", IA(0, 54, -1), IA(7, 7)),
        /* jitterbug 1745 */
        SD("testing that \u00e9 does not match e", "e", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, "characterbreaker", IA(1, 17, 30, -1), IA(1, 1, 1)),
        SD("testing that string ab\u00e9cd does not match e", "e", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, "characterbreaker", IA(1, 28, 41, -1), IA(1, 1, 1)),
        SD("\u00c9", "e", "fr", PRIMARY, STANDARD_ELEMENT_COMPARISON, "characterbreaker", IA(0, -1), IA(1)),
    };

    SearchData BASICCANONICAL[] = {
        SD("xxxxxxxxxxxxxxxxxxxx", "fisher", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("silly spring string", "string", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(13, -1), IA(6)),
        SD("silly spring string string", "string", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(13, 20, -1), IA(6, 6)),
        SD("silly string spring string", "string", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(6, 20, -1), IA(6, 6)),
        SD("string spring string", "string", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, 14, -1), IA(6, 6)),
        SD("Scott Ganyo", "c", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(1, -1), IA(1)),
        SD("Scott Ganyo", " ", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(5, -1), IA(1)),

        SD("\u0300\u0325", "\u0300", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("a\u0300\u0325", "\u0300", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("a\u0300\u0325", "\u0300\u0325", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("a\u0300b", "\u0300", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("a\u0300\u0325b", "\u0300b", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("\u0325\u0300A\u0325\u0300", "\u0300A\u0300", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("\u0325\u0300A\u0325\u0300", "\u0325A\u0325", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("a\u0300\u0325b\u0300\u0325c \u0325b\u0300 \u0300b\u0325", "\u0300b\u0325", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        SD("\u00c4\u0323", "A\u0323\u0308", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, -1), IA(2)),
        SD("\u0308\u0323", "\u0323\u0308", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, -1), IA(2)),
    };

    SearchData COLLATOR[] = {
        /* english */
        SD("fox fpx", "fox", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, -1), IA(3)),
        /* tailored */
        SD("fox fpx", "fox", null, PRIMARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, 4, -1), IA(3, 3)),
    };

    String TESTCOLLATORRULE = "& o,O ; p,P";
    String EXTRACOLLATIONRULE = " & ae ; \u00e4 & AE ; \u00c4 & oe ; \u00f6 & OE ; \u00d6 & ue ; \u00fc & UE ; \u00dc";

    SearchData COLLATORCANONICAL[] = {
        /* english */
        SD("fox fpx", "fox", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, -1), IA(3)),
        /* tailored */
        SD("fox fpx", "fox", null, PRIMARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, 4, -1), IA(3, 3)),
    };

    SearchData COMPOSITEBOUNDARIES[] = {
        SD("\u00C0", "A", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("A\u00C0C", "A", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, -1), IA(1)),
        SD("\u00C0A", "A", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(1, -1), IA(1)),
        SD("B\u00C0", "A", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("\u00C0B", "A", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("\u00C0", "\u0300", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        /* first one matches only because it's at the start of the text */
        SD("\u0300\u00C0", "\u0300", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, -1), IA(1)),

        /* \\u0300 blocked by \\u0300 */
        SD("\u00C0\u0300", "\u0300", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        /* A + 030A + 0301 */
        SD("\u01FA", "\u01FA", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, -1), IA(1)),
        SD("\u01FA", "A\u030A\u0301", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, -1), IA(1)),

        SD("\u01FA", "\u030A", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("\u01FA", "A\u030A", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        SD("\u01FA", "\u030AA", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        SD("\u01FA", "\u0301", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        /* blocked accent */
        SD("\u01FA", "A\u0301", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("\u01FA", "\u0301A", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        SD("\u01FA", "\u030A\u0301", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("A\u01FA", "A\u030A", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("\u01FAA", "\u0301A", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        SD("\u0F73", "\u0F73", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, -1), IA(1)),

        SD("\u0F73", "\u0F71", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("\u0F73", "\u0F72", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        SD("\u0F73", "\u0F71\u0F72", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, -1), IA(1)),

        SD("A\u0F73", "A\u0F71", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("\u0F73A", "\u0F72A", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("\u01FA A\u0301\u030A A\u030A\u0301 A\u030A \u01FA", "A\u030A", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(10, -1), IA(2)),
    };

    SearchData COMPOSITEBOUNDARIESCANONICAL[] = {
        SD("\u00C0", "A", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("A\u00C0C", "A", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, -1), IA(1)),
        SD("\u00C0A", "A", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(1, -1), IA(1)),
        SD("B\u00C0", "A", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("\u00C0B", "A", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("\u00C0", "\u0300", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        /* first one matches only because it's at the start of the text */
        SD("\u0300\u00C0", "\u0300", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, -1), IA(1)),

        /* \u0300 blocked by \u0300 */
        SD("\u00C0\u0300", "\u0300", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        /* A + 030A + 0301 */
        SD("\u01FA", "\u01FA", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, -1), IA(1)),
        SD("\u01FA", "A\u030A\u0301", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, -1), IA(1)),

        SD("\u01FA", "\u030A", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("\u01FA", "A\u030A", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        SD("\u01FA", "\u030AA", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        SD("\u01FA", "\u0301", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        /* blocked accent */
        SD("\u01FA", "A\u0301", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("\u01FA", "\u0301A", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        SD("\u01FA", "\u030A\u0301", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("A\u01FA", "A\u030A", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("\u01FAA", "\u0301A", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        SD("\u0F73", "\u0F73", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, -1), IA(1)),

        SD("\u0F73", "\u0F71", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("\u0F73", "\u0F72", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        SD("\u0F73", "\u0F71\u0F72", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, -1), IA(1)),

        SD("A\u0F73", "A\u0F71", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("\u0F73A", "\u0F72A", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        SD("\u01FA A\u0301\u030A A\u030A\u0301 A\u030A \u01FA", "A\u030A", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(10, -1), IA(2)),
    };

    SearchData SUPPLEMENTARY[] = {
        SD("abc \uD800\uDC00 \uD800\uDC01 \uD801\uDC00 \uD800\uDC00abc abc\uD800\uDC00 \uD800\uD800\uDC00 \uD800\uDC00\uDC00",
                "\uD800\uDC00", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(4, 13, 22, 26, 29, -1), IA(2, 2, 2, 2, 2)),
        SD("and\uD834\uDDB9this sentence", "\uD834\uDDB9", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(3, -1), IA(2)),
        SD("and \uD834\uDDB9 this sentence", " \uD834\uDDB9 ", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(3, -1), IA(4)),
        SD("and-\uD834\uDDB9-this sentence", "-\uD834\uDDB9-", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(3, -1), IA(4)),
        SD("and,\uD834\uDDB9,this sentence", ",\uD834\uDDB9,", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(3, -1), IA(4)),
        SD("and?\uD834\uDDB9?this sentence", "?\uD834\uDDB9?", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(3, -1), IA(4)),
    };

    String CONTRACTIONRULE = "&z = ab/c < AB < X\u0300 < ABC < X\u0300\u0315";

    SearchData CONTRACTION[] = {
        /* common discontiguous */
        SD("A\u0300\u0315", "\u0300", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        SD("A\u0300\u0315", "\u0300\u0315", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        /* contraction prefix */
        SD("AB\u0315C", "A", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        SD("AB\u0315C", "AB", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("AB\u0315C", "\u0315", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        /*
         * discontiguous problem here for backwards iteration. accents not found because discontiguous stores all
         * information
         */
        SD("X\u0300\u0319\u0315", "\u0319", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        /* ends not with a contraction character */
        SD("X\u0315\u0300D", "\u0300\u0315", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("X\u0315\u0300D", "X\u0300\u0315", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, -1), IA(3)),
        SD("X\u0300\u031A\u0315D", "X\u0300", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        /* blocked discontiguous */
        SD("X\u0300\u031A\u0315D", "\u031A\u0315D", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        /*
         * "ab" generates a contraction that's an expansion. The "z" matches the first CE of the expansion but the
         * match fails because it ends in the middle of an expansion...
         */
        SD("ab", "z", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
    };

    SearchData CONTRACTIONCANONICAL[] = {
        /* common discontiguous */
        SD("A\u0300\u0315", "\u0300", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("A\u0300\u0315", "\u0300\u0315", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        /* contraction prefix */
        SD("AB\u0315C", "A", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        SD("AB\u0315C", "AB", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("AB\u0315C", "\u0315", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        /*
         * discontiguous problem here for backwards iteration. forwards gives 0, 4 but backwards give 1, 3
         */
        /*
         * {"X\u0300\u0319\u0315", "\u0319", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, {0, -1), {4}),
         */

        /* ends not with a contraction character */
        SD("X\u0315\u0300D", "\u0300\u0315", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("X\u0315\u0300D", "X\u0300\u0315", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, -1), IA(3)),

        SD("X\u0300\u031A\u0315D", "X\u0300", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        /* blocked discontiguous */
        SD("X\u0300\u031A\u0315D", "\u031A\u0315D", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),

        /*
         * "ab" generates a contraction that's an expansion. The "z" matches the first CE of the expansion but the
         * match fails because it ends in the middle of an expansion...
         */
        SD("ab", "z", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(2)),
    };

    SearchData MATCH[] = {
        SD("a busy bee is a very busy beeee", "bee", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(7, 26, -1), IA(3, 3)),
        /*  012345678901234567890123456789012345678901234567890 */
        SD("a busy bee is a very busy beeee with no bee life", "bee", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(7, 26, 40, -1), IA(3, 3, 3)),
    };

    String IGNORABLERULE = "&a = \u0300";

    SearchData IGNORABLE[] = {
        /*
         * This isn't much of a test when matches have to be on grapheme boundiaries. The match at 0 only works because it's
         * at the start of the text.
         */
        SD("\u0300\u0315 \u0300\u0315 ", "\u0300", null, PRIMARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, -1), IA(2)),
    };

    SearchData DIACTRICMATCH[] = {
        SD("\u0061\u0061\u00E1", "\u0061\u00E1", null, SECONDARY, STANDARD_ELEMENT_COMPARISON, null, IA(1, -1), IA(2)),
        SD("\u0020\u00C2\u0303\u0020\u0041\u0061\u1EAA\u0041\u0302\u0303\u00C2\u0303\u1EAB\u0061\u0302\u0303\u00E2\u0303\uD806\uDC01\u0300\u0020", "\u00C2\u0303",
            null, PRIMARY, STANDARD_ELEMENT_COMPARISON, null, IA(1, 4, 5, 6, 7, 10, 12, 13, 16, -1), IA(2, 1, 1, 1, 3, 2, 1, 3, 2)),
        SD("\u03BA\u03B1\u03B9\u0300\u0020\u03BA\u03B1\u1F76", "\u03BA\u03B1\u03B9", null, PRIMARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, 5, -1), IA(4, 3)),
    };

    SearchData NORMCANONICAL[] = {
        SD("\u0300\u0325", "\u0300", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("\u0300\u0325", "\u0325", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("a\u0300\u0325", "\u0325\u0300", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("a\u0300\u0325", "\u0300\u0325", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("a\u0300\u0325", "\u0325", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("a\u0300\u0325", "\u0300", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
    };

    SearchData NORMEXACT[] = {
        SD("a\u0300\u0325", "a\u0325\u0300", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, -1), IA(3)),
    };

    SearchData NONNORMEXACT[] = {
        SD("a\u0300\u0325", "\u0325\u0300", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
    };

    SearchData OVERLAP[] = {
        SD("abababab", "abab", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, 2, 4, -1), IA(4, 4, 4)),
    };

    SearchData NONOVERLAP[] = {
        SD("abababab", "abab", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, 4, -1), IA(4, 4)),
    };

    SearchData OVERLAPCANONICAL[] = {
        SD("abababab", "abab", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, 2, 4, -1), IA(4, 4, 4)),
    };

    SearchData NONOVERLAPCANONICAL[] = {
        SD("abababab", "abab", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, 4, -1), IA(4, 4)),
    };

    SearchData PATTERNCANONICAL[] = {
        SD("The quick brown fox jumps over the lazy foxes", "the", null, PRIMARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, 31, -1), IA(3, 3)),
        SD("The quick brown fox jumps over the lazy foxes", "fox", null, PRIMARY, STANDARD_ELEMENT_COMPARISON, null, IA(16, 40, -1), IA(3, 3)),
    };

    SearchData PATTERN[] = {
        SD("The quick brown fox jumps over the lazy foxes", "the", null, PRIMARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, 31, -1), IA(3, 3)),
        SD("The quick brown fox jumps over the lazy foxes", "fox", null, PRIMARY, STANDARD_ELEMENT_COMPARISON, null, IA(16, 40, -1), IA(3, 3)),
    };

    String PECHE_WITH_ACCENTS = "un p\u00E9ch\u00E9, "
                                + "\u00E7a p\u00E8che par, "
                                + "p\u00E9cher, "
                                + "une p\u00EAche, "
                                + "un p\u00EAcher, "
                                + "j\u2019ai p\u00EAch\u00E9, "
                                + "un p\u00E9cheur, "
                                + "\u201Cp\u00E9che\u201D, "
                                + "decomp peche\u0301, "
                                + "base peche";
    // in the above, the interesting words and their offsets are:
    //    3 pe<301>che<301>
    //    13 pe<300>che
    //    24 pe<301>cher
    //    36 pe<302>che
    //    46 pe<302>cher
    //    59 pe<302>che<301>
    //    69 pe<301>cheur
    //    79 pe<301>che
    //    94 peche<+301>
    //    107 peche

    SearchData STRENGTH[] = {
        /*  012345678901234567890123456789012345678901234567890123456789 */
        SD("The quick brown fox jumps over the lazy foxes", "fox", "en", PRIMARY, STANDARD_ELEMENT_COMPARISON, null, IA(16, 40, -1), IA(3, 3)),
        SD("The quick brown fox jumps over the lazy foxes", "fox", "en", PRIMARY, STANDARD_ELEMENT_COMPARISON, "wordbreaker", IA(16, -1), IA(3)),
        SD("blackbirds Pat p\u00E9ch\u00E9 p\u00EAche p\u00E9cher p\u00EAcher Tod T\u00F6ne black Tofu blackbirds Ton PAT toehold blackbird black-bird pat toe big Toe",
                "peche", "fr", PRIMARY, STANDARD_ELEMENT_COMPARISON, null, IA(15, 21, 27, 34, -1), IA(5, 5, 5, 5)),
        SD("This is a toe T\u00F6ne", "toe", "de", PRIMARY, STANDARD_ELEMENT_COMPARISON, null, IA(10, 14, -1), IA(3, 2)),
        SD("A channel, another CHANNEL, more Channels, and one last channel...", "channel", "es", PRIMARY, STANDARD_ELEMENT_COMPARISON, null, IA(2, 19, 33, 56, -1), IA(7, 7, 7, 7)),
        SD("\u00c0 should match but not A", "A\u0300", "en", IDENTICAL, STANDARD_ELEMENT_COMPARISON,  null, IA(0, -1), IA(1, 0)),

        /* some tests for modified element comparison, ticket #7093 */
        SD(PECHE_WITH_ACCENTS, "peche", "en", PRIMARY, STANDARD_ELEMENT_COMPARISON, null, IA(3, 13, 24, 36, 46, 59, 69, 79, 94, 107, -1), IA(5, 5, 5, 5, 5, 5, 5, 5, 6, 5)),
        SD(PECHE_WITH_ACCENTS, "peche", "en", PRIMARY, STANDARD_ELEMENT_COMPARISON, "wordbreaker", IA(3, 13, 36, 59, 79, 94, 107, -1), IA(5, 5, 5, 5, 5, 6, 5)),
        SD(PECHE_WITH_ACCENTS, "peche", "en", SECONDARY, STANDARD_ELEMENT_COMPARISON, null, IA(107, -1), IA(5)),
        SD(PECHE_WITH_ACCENTS, "peche", "en", SECONDARY, PATTERN_BASE_WEIGHT_IS_WILDCARD, null, IA(3, 13, 24, 36, 46, 59, 69, 79, 94, 107, -1), IA(5, 5, 5, 5, 5, 5, 5, 5, 6, 5)),
        SD(PECHE_WITH_ACCENTS, "peche", "en", SECONDARY, PATTERN_BASE_WEIGHT_IS_WILDCARD, "wordbreaker", IA(3, 13, 36, 59, 79, 94, 107, -1), IA(5, 5, 5, 5, 5, 6, 5)),
        SD(PECHE_WITH_ACCENTS, "p\u00E9che", "en", SECONDARY, STANDARD_ELEMENT_COMPARISON, null, IA(24, 69, 79, -1), IA(5, 5, 5)),
        SD(PECHE_WITH_ACCENTS, "p\u00E9che", "en", SECONDARY, STANDARD_ELEMENT_COMPARISON, "wordbreaker", IA(79, -1), IA(5)),
        SD(PECHE_WITH_ACCENTS, "p\u00E9che", "en", SECONDARY, PATTERN_BASE_WEIGHT_IS_WILDCARD, null, IA(3, 24, 69, 79, -1), IA(5, 5, 5, 5)),
        SD(PECHE_WITH_ACCENTS, "p\u00E9che", "en", SECONDARY, PATTERN_BASE_WEIGHT_IS_WILDCARD, "wordbreaker", IA(3, 79, -1), IA(5, 5)),
        SD(PECHE_WITH_ACCENTS, "p\u00E9che", "en", SECONDARY, ANY_BASE_WEIGHT_IS_WILDCARD, null, IA(3, 24, 69, 79, 94, 107, -1), IA(5, 5, 5, 5, 6, 5)),
        SD(PECHE_WITH_ACCENTS, "p\u00E9che", "en", SECONDARY, ANY_BASE_WEIGHT_IS_WILDCARD, "wordbreaker", IA(3, 79, 94, 107, -1), IA(5, 5, 6, 5)),
        SD(PECHE_WITH_ACCENTS, "pech\u00E9", "en", SECONDARY, PATTERN_BASE_WEIGHT_IS_WILDCARD, null, IA(3, 59, 94, -1), IA(5, 5, 6)),
        SD(PECHE_WITH_ACCENTS, "pech\u00E9", "en", SECONDARY, PATTERN_BASE_WEIGHT_IS_WILDCARD, "wordbreaker", IA(3, 59, 94, -1), IA(5, 5, 6)),
        SD(PECHE_WITH_ACCENTS, "pech\u00E9", "en", SECONDARY, ANY_BASE_WEIGHT_IS_WILDCARD, null, IA(3, 13, 24, 36, 46, 59, 69, 79, 94, 107, -1), IA(5, 5, 5, 5, 5, 5, 5, 5, 6, 5)),
        SD(PECHE_WITH_ACCENTS, "pech\u00E9", "en", SECONDARY, ANY_BASE_WEIGHT_IS_WILDCARD, "wordbreaker", IA(3, 13, 36, 59, 79, 94, 107, -1), IA(5, 5, 5, 5, 5, 6, 5)),
        SD(PECHE_WITH_ACCENTS, "peche\u0301", "en", SECONDARY, PATTERN_BASE_WEIGHT_IS_WILDCARD, null, IA(3, 59, 94, -1), IA(5, 5, 6)),
        SD(PECHE_WITH_ACCENTS, "peche\u0301", "en", SECONDARY, PATTERN_BASE_WEIGHT_IS_WILDCARD, "wordbreaker", IA(3, 59, 94, -1), IA(5, 5, 6)),
        SD(PECHE_WITH_ACCENTS, "peche\u0301", "en", SECONDARY, ANY_BASE_WEIGHT_IS_WILDCARD, null, IA(3, 13, 24, 36, 46, 59, 69, 79, 94, 107, -1), IA(5, 5, 5, 5, 5, 5, 5, 5, 6, 5)),
        SD(PECHE_WITH_ACCENTS, "peche\u0301", "en", SECONDARY, ANY_BASE_WEIGHT_IS_WILDCARD, "wordbreaker", IA(3, 13, 36, 59, 79, 94, 107, -1), IA(5, 5, 5, 5, 5, 6, 5)),

        /* more tests for modified element comparison (with fr), ticket #7093 */
        SD(PECHE_WITH_ACCENTS, "peche", "fr", PRIMARY, STANDARD_ELEMENT_COMPARISON, null, IA(3, 13, 24, 36, 46, 59, 69, 79, 94, 107, -1), IA(5, 5, 5, 5, 5, 5, 5, 5, 6, 5)),
        SD(PECHE_WITH_ACCENTS, "peche", "fr", PRIMARY, STANDARD_ELEMENT_COMPARISON, "wordbreaker", IA(3, 13, 36, 59, 79, 94, 107, -1), IA(5, 5, 5, 5, 5, 6, 5)),
        SD(PECHE_WITH_ACCENTS, "peche", "fr", SECONDARY, STANDARD_ELEMENT_COMPARISON, null, IA(107, -1), IA(5)),
        SD(PECHE_WITH_ACCENTS, "peche", "fr", SECONDARY, PATTERN_BASE_WEIGHT_IS_WILDCARD, null, IA(3, 13, 24, 36, 46, 59, 69, 79, 94, 107, -1), IA(5, 5, 5, 5, 5, 5, 5, 5, 6, 5)),
        SD(PECHE_WITH_ACCENTS, "peche", "fr", SECONDARY, PATTERN_BASE_WEIGHT_IS_WILDCARD, "wordbreaker", IA(3, 13, 36, 59, 79, 94, 107, -1), IA(5, 5, 5, 5, 5, 6, 5)),
        SD(PECHE_WITH_ACCENTS, "p\u00E9che", "fr", SECONDARY, STANDARD_ELEMENT_COMPARISON, null, IA(24, 69, 79, -1), IA(5, 5, 5)),
        SD(PECHE_WITH_ACCENTS, "p\u00E9che", "fr", SECONDARY, STANDARD_ELEMENT_COMPARISON, "wordbreaker", IA(79, -1), IA(5)),
        SD(PECHE_WITH_ACCENTS, "p\u00E9che", "fr", SECONDARY, PATTERN_BASE_WEIGHT_IS_WILDCARD, null, IA(3, 24, 69, 79, -1), IA(5, 5, 5, 5)),
        SD(PECHE_WITH_ACCENTS, "p\u00E9che", "fr", SECONDARY, PATTERN_BASE_WEIGHT_IS_WILDCARD, "wordbreaker", IA(3, 79, -1), IA(5, 5)),
        SD(PECHE_WITH_ACCENTS, "p\u00E9che", "fr", SECONDARY, ANY_BASE_WEIGHT_IS_WILDCARD, null, IA(3, 24, 69, 79, 94, 107, -1), IA(5, 5, 5, 5, 6, 5)),
        SD(PECHE_WITH_ACCENTS, "p\u00E9che", "fr", SECONDARY, ANY_BASE_WEIGHT_IS_WILDCARD, "wordbreaker", IA(3, 79, 94, 107, -1), IA(5, 5, 6, 5)),
        SD(PECHE_WITH_ACCENTS, "pech\u00E9", "fr", SECONDARY, PATTERN_BASE_WEIGHT_IS_WILDCARD, null, IA(3, 59, 94, -1), IA(5, 5, 6)),
        SD(PECHE_WITH_ACCENTS, "pech\u00E9", "fr", SECONDARY, PATTERN_BASE_WEIGHT_IS_WILDCARD, "wordbreaker", IA(3, 59, 94, -1), IA(5, 5, 6)),
        SD(PECHE_WITH_ACCENTS, "pech\u00E9", "fr", SECONDARY, ANY_BASE_WEIGHT_IS_WILDCARD, null, IA(3, 13, 24, 36, 46, 59, 69, 79, 94, 107, -1), IA(5, 5, 5, 5, 5, 5, 5, 5, 6, 5)),
        SD(PECHE_WITH_ACCENTS, "pech\u00E9", "fr", SECONDARY, ANY_BASE_WEIGHT_IS_WILDCARD, "wordbreaker", IA(3, 13, 36, 59, 79, 94, 107, -1), IA(5, 5, 5, 5, 5, 6, 5)),
        SD(PECHE_WITH_ACCENTS, "peche\u0301", "fr", SECONDARY, PATTERN_BASE_WEIGHT_IS_WILDCARD, null, IA(3, 59, 94, -1), IA(5, 5, 6)),
        SD(PECHE_WITH_ACCENTS, "peche\u0301", "fr", SECONDARY, PATTERN_BASE_WEIGHT_IS_WILDCARD, "wordbreaker", IA(3, 59, 94, -1), IA(5, 5, 6)),
        SD(PECHE_WITH_ACCENTS, "peche\u0301", "fr", SECONDARY, ANY_BASE_WEIGHT_IS_WILDCARD, null, IA(3, 13, 24, 36, 46, 59, 69, 79, 94, 107, -1), IA(5, 5, 5, 5, 5, 5, 5, 5, 6, 5)),
        SD(PECHE_WITH_ACCENTS, "peche\u0301", "fr", SECONDARY, ANY_BASE_WEIGHT_IS_WILDCARD, "wordbreaker", IA(3, 13, 36, 59, 79, 94, 107, -1), IA(5, 5, 5, 5, 5, 6, 5)),

    };

    SearchData STRENGTHCANONICAL[] = {
        /*  012345678901234567890123456789012345678901234567890123456789 */
        SD("The quick brown fox jumps over the lazy foxes", "fox", "en", PRIMARY, STANDARD_ELEMENT_COMPARISON, null, IA(16, 40, -1), IA(3, 3)),
        SD("The quick brown fox jumps over the lazy foxes", "fox", "en", PRIMARY, STANDARD_ELEMENT_COMPARISON, "wordbreaker", IA(16, -1), IA(3)),
        SD("blackbirds Pat p\u00E9ch\u00E9 p\u00EAche p\u00E9cher p\u00EAcher Tod T\u00F6ne black Tofu blackbirds Ton PAT toehold blackbird black-bird pat toe big Toe",
                "peche", "fr", PRIMARY, STANDARD_ELEMENT_COMPARISON, null, IA(15, 21, 27, 34, -1), IA(5, 5, 5, 5)),
        SD("This is a toe T\u00F6ne", "toe", "de", PRIMARY, STANDARD_ELEMENT_COMPARISON, null, IA(10, 14, -1), IA(3, 2)),
        SD("A channel, another CHANNEL, more Channels, and one last channel...", "channel", "es", PRIMARY, STANDARD_ELEMENT_COMPARISON, null, IA(2, 19, 33, 56, -1), IA(7, 7, 7, 7)),
    };

    SearchData SUPPLEMENTARYCANONICAL[] = {
        /*  012345678901234567890123456789012345678901234567890012345678901234567890123456789012345678901234567890012345678901234567890123456789 */
        SD("abc \uD800\uDC00 \uD800\uDC01 \uD801\uDC00 \uD800\uDC00abc abc\uD800\uDC00 \uD800\uD800\uDC00 \uD800\uDC00\uDC00", "\uD800\uDC00",
            null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(4, 13, 22, 26, 29, -1), IA(2, 2, 2, 2, 2)),
        SD("and\uD834\uDDB9this sentence", "\uD834\uDDB9", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(3, -1), IA(2)),
        SD("and \uD834\uDDB9 this sentence", " \uD834\uDDB9 ", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(3, -1), IA(4)),
        SD("and-\uD834\uDDB9-this sentence", "-\uD834\uDDB9-", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(3, -1), IA(4)),
        SD("and,\uD834\uDDB9,this sentence", ",\uD834\uDDB9,", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(3, -1), IA(4)),
        SD("and?\uD834\uDDB9?this sentence", "?\uD834\uDDB9?", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(3, -1), IA(4)),
    };

    static SearchData VARIABLE[] = {
        /*  012345678901234567890123456789012345678901234567890123456789 */
        SD("blackbirds black blackbirds blackbird black-bird", "blackbird", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, 17, 28, 38, -1), IA(9, 9, 9, 10)),

        /*
         * to see that it doesn't go into an infinite loop if the start of text is a ignorable character
         */
        SD(" on", "go", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
        SD("abcdefghijklmnopqrstuvwxyz", "   ",
            null, PRIMARY, STANDARD_ELEMENT_COMPARISON, null,
            IA(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1),
            IA(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)),

        /* testing tightest match */
        SD(" abc  a bc   ab c    a  bc     ab  c", "abc", null, QUATERNARY, STANDARD_ELEMENT_COMPARISON, null, IA(1, -1), IA(3)),
        /*  012345678901234567890123456789012345678901234567890123456789 */
        SD(" abc  a bc   ab c    a  bc     ab  c", "abc", null, SECONDARY, STANDARD_ELEMENT_COMPARISON, null, IA(1, 6, 13, 21, 31, -1), IA(3, 4, 4, 5, 5)),

        /* totally ignorable text */
        SD("           ---------------", "abc", null, SECONDARY, STANDARD_ELEMENT_COMPARISON, null, IA(-1), IA(0)),
    };

    static SearchData TEXTCANONICAL[] = {
        SD("the foxy brown fox", "fox", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(4, 15, -1), IA(3, 3)),
        SD("the quick brown fox", "fox", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(16, -1), IA(3)),
    };

    static SearchData INDICPREFIXMATCH[] = {
        SD("\u0915\u0020\u0915\u0901\u0020\u0915\u0902\u0020\u0915\u0903\u0020\u0915\u0940\u0020\u0915\u093F\u0020\u0915\u0943\u0020\u0915\u093C\u0020\u0958",
                "\u0915", null, PRIMARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, 2, 5, 8, 11, 14, 17, 20, 23,-1), IA(1, 2, 2, 2, 1, 1, 1, 2, 1)),
        SD("\u0915\u0924\u0020\u0915\u0924\u0940\u0020\u0915\u0924\u093F\u0020\u0915\u0924\u0947\u0020\u0915\u0943\u0924\u0020\u0915\u0943\u0924\u0947",
                "\u0915\u0924", null, PRIMARY, STANDARD_ELEMENT_COMPARISON, null, IA(0, 3, 7, 11, -1), IA(2, 2, 2, 2)),
        SD("\u0915\u0924\u0020\u0915\u0924\u0940\u0020\u0915\u0924\u093F\u0020\u0915\u0924\u0947\u0020\u0915\u0943\u0924\u0020\u0915\u0943\u0924\u0947",
                "\u0915\u0943\u0924", null, PRIMARY, STANDARD_ELEMENT_COMPARISON, null, IA(15, 19, -1), IA(3, 3)),
    };

    /**
     * Constructor
     */
    public SearchTest()
    {

    }

    @Before
    public void init() throws Exception {
        m_en_us_ = (RuleBasedCollator)Collator.getInstance(Locale.US);
        m_fr_fr_ = (RuleBasedCollator)Collator.getInstance(Locale.FRANCE);
        m_de_ = (RuleBasedCollator)Collator.getInstance(new Locale("de", "DE"));
        m_es_ = (RuleBasedCollator)Collator.getInstance(new Locale("es", "ES"));
        m_en_wordbreaker_ = BreakIterator.getWordInstance();
        m_en_characterbreaker_ = BreakIterator.getCharacterInstance();
        String rules = m_de_.getRules() + EXTRACOLLATIONRULE;
        m_de_ = new RuleBasedCollator(rules);
        rules = m_es_.getRules() + EXTRACOLLATIONRULE;
        m_es_ = new RuleBasedCollator(rules);

    }

    RuleBasedCollator getCollator(String collator) {
        if (collator == null) {
            return m_en_us_;
        } if (collator.equals("fr")) {
            return m_fr_fr_;
        } else if (collator.equals("de")) {
            return m_de_;
        } else if (collator.equals("es")) {
            return m_es_;
        } else {
            return m_en_us_;
        }
    }

    BreakIterator getBreakIterator(String breaker) {
        if (breaker == null) {
            return null;
        } if (breaker.equals("wordbreaker")) {
            return m_en_wordbreaker_;
        } else {
            return m_en_characterbreaker_;
        }
    }

    boolean assertCanonicalEqual(SearchData search) {
        Collator      collator = getCollator(search.collator);
        BreakIterator breaker  = getBreakIterator(search.breaker);
        StringSearch  strsrch;

        String text = search.text;
        String  pattern = search.pattern;

        if (breaker != null) {
            breaker.setText(text);
        }
        collator.setStrength(search.strength);
        collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        try {
            strsrch = new StringSearch(pattern, new StringCharacterIterator(text), (RuleBasedCollator)collator, breaker);
            strsrch.setElementComparisonType(search.cmpType);
            strsrch.setCanonical(true);
        } catch (Exception e) {
            errln("Error opening string search" + e.getMessage());
            return false;
        }

        if (!assertEqualWithStringSearch(strsrch, search)) {
            collator.setStrength(TERTIARY);
            collator.setDecomposition(Collator.NO_DECOMPOSITION);
            return false;
        }
        collator.setStrength(TERTIARY);
        collator.setDecomposition(Collator.NO_DECOMPOSITION);
        return true;
    }

    boolean assertEqual(SearchData search) {
        Collator      collator = getCollator(search.collator);
        BreakIterator breaker  = getBreakIterator(search.breaker);
        StringSearch  strsrch;

        String text = search.text;
        String  pattern = search.pattern;

        if (breaker != null) {
            breaker.setText(text);
        }
        collator.setStrength(search.strength);
        try {
            strsrch = new StringSearch(pattern, new StringCharacterIterator(text), (RuleBasedCollator)collator, breaker);
            strsrch.setElementComparisonType(search.cmpType);
        } catch (Exception e) {
            errln("Error opening string search " + e.getMessage());
            return false;
        }

        if (!assertEqualWithStringSearch(strsrch, search)) {
            collator.setStrength(TERTIARY);
            return false;
        }
        collator.setStrength(TERTIARY);
        return true;
    }

    boolean assertEqualWithAttribute(SearchData search, boolean canonical, boolean overlap) {
        Collator      collator = getCollator(search.collator);
        BreakIterator breaker  = getBreakIterator(search.breaker);
        StringSearch  strsrch;

        String text = search.text;
        String  pattern = search.pattern;

        if (breaker != null) {
            breaker.setText(text);
        }
        collator.setStrength(search.strength);
        try {
            strsrch = new StringSearch(pattern, new StringCharacterIterator(text), (RuleBasedCollator)collator, breaker);
            strsrch.setCanonical(canonical);
            strsrch.setOverlapping(overlap);
            strsrch.setElementComparisonType(search.cmpType);
        } catch (Exception e) {
            errln("Error opening string search " + e.getMessage());
            return false;
        }

        if (!assertEqualWithStringSearch(strsrch, search)) {
            collator.setStrength(TERTIARY);
            return false;
        }
        collator.setStrength(TERTIARY);
        return true;
    }

    boolean assertEqualWithStringSearch(StringSearch strsrch, SearchData search) {
        int           count       = 0;
        int   matchindex  = search.offset[count];
        String matchtext;

        if (strsrch.getMatchStart() != SearchIterator.DONE ||
            strsrch.getMatchLength() != 0) {
            errln("Error with the initialization of match start and length");
        }
        // start of following matches
        while (matchindex >= 0) {
            int matchlength = search.size[count];
            strsrch.next();
            //int x = strsrch.getMatchStart();
            if (matchindex != strsrch.getMatchStart() ||
                matchlength != strsrch.getMatchLength()) {
                errln("Text: " + search.text);
                errln("Searching forward for pattern: " + strsrch.getPattern());
                errln("Expected offset,len " + matchindex + ", " + matchlength + "; got " + strsrch.getMatchStart() + ", " + strsrch.getMatchLength());
                return false;
            }
            count ++;

            matchtext = strsrch.getMatchedText();
            String targetText = search.text;
            if (matchlength > 0 &&
                targetText.substring(matchindex, matchindex + matchlength).compareTo(matchtext) != 0) {
                errln("Error getting following matched text");
            }

            matchindex = search.offset[count];
        }
        strsrch.next();
        if (strsrch.getMatchStart() != SearchIterator.DONE ||
            strsrch.getMatchLength() != 0) {
                errln("Text: " + search.text);
                errln("Searching forward for pattern: " + strsrch.getPattern());
                errln("Expected DONE offset,len -1, 0; got " + strsrch.getMatchStart() + ", " + strsrch.getMatchLength());
                return false;
        }
        // start of preceding matches
        count = count == 0 ? 0 : count - 1;
        matchindex = search.offset[count];
        while (matchindex >= 0) {
            int matchlength = search.size[count];
            strsrch.previous();
            if (matchindex != strsrch.getMatchStart() ||
                matchlength != strsrch.getMatchLength()) {
                errln("Text: " + search.text);
                errln("Searching backward for pattern: " + strsrch.getPattern());
                errln("Expected offset,len " + matchindex + ", " + matchlength + "; got " + strsrch.getMatchStart() + ", " + strsrch.getMatchLength());
                return false;
            }

            matchtext = strsrch.getMatchedText();
            String targetText = search.text;
            if (matchlength > 0 &&
                targetText.substring(matchindex, matchindex + matchlength).compareTo(matchtext) != 0) {
                errln("Error getting following matched text");
            }

            matchindex = count > 0 ? search.offset[count - 1] : -1;
            count --;
        }
        strsrch.previous();
        if (strsrch.getMatchStart() != SearchIterator.DONE ||
            strsrch.getMatchLength() != 0) {
                errln("Text: " + search.text);
                errln("Searching backward for pattern: " + strsrch.getPattern());
                errln("Expected DONE offset,len -1, 0; got " + strsrch.getMatchStart() + ", " + strsrch.getMatchLength());
                return false;
        }
        return true;
    }

    @Test
    public void TestConstructor()
    {
        String pattern = "pattern";
        String text = "text";
        StringCharacterIterator textiter = new StringCharacterIterator(text);
        Collator defaultcollator = Collator.getInstance();
        BreakIterator breaker = BreakIterator.getCharacterInstance();
        breaker.setText(text);
        StringSearch search = new StringSearch(pattern, text);
        if (!search.getPattern().equals(pattern)
            || !search.getTarget().equals(textiter)
            || !search.getCollator().equals(defaultcollator)
            /*|| !search.getBreakIterator().equals(breaker)*/) {
            errln("StringSearch(String, String) error");
        }
        search = new StringSearch(pattern, textiter, m_fr_fr_);
        if (!search.getPattern().equals(pattern)
            || !search.getTarget().equals(textiter)
            || !search.getCollator().equals(m_fr_fr_)
            /*|| !search.getBreakIterator().equals(breaker)*/) {
            errln("StringSearch(String, StringCharacterIterator, "
                  + "RuleBasedCollator) error");
        }
        Locale de = new Locale("de", "DE");
        breaker = BreakIterator.getCharacterInstance(de);
        breaker.setText(text);
        search = new StringSearch(pattern, textiter, de);
        if (!search.getPattern().equals(pattern)
            || !search.getTarget().equals(textiter)
            || !search.getCollator().equals(Collator.getInstance(de))
            /*|| !search.getBreakIterator().equals(breaker)*/) {
            errln("StringSearch(String, StringCharacterIterator, Locale) "
                  + "error");
        }

        search = new StringSearch(pattern, textiter, m_fr_fr_,
                                  m_en_wordbreaker_);
        if (!search.getPattern().equals(pattern)
            || !search.getTarget().equals(textiter)
            || !search.getCollator().equals(m_fr_fr_)
            || !search.getBreakIterator().equals(m_en_wordbreaker_)) {
            errln("StringSearch(String, StringCharacterIterator, Locale) "
                  + "error");
        }
    }

    @Test
    public void TestBasic() {
        for (int count = 0; count < BASIC.length; count++) {
            if (!assertEqual(BASIC[count])) {
                errln("Error at test number " + count);
            }
        }
    }

    @Test
    public void TestBreakIterator() {

        String text = BREAKITERATOREXACT[0].text;
        String pattern = BREAKITERATOREXACT[0].pattern;
        StringSearch strsrch = null;
        try {
            strsrch = new StringSearch(pattern, new StringCharacterIterator(text), m_en_us_, null);
        } catch (Exception e) {
            errln("Error opening string search");
            return;
        }

        strsrch.setBreakIterator(null);
        if (strsrch.getBreakIterator() != null) {
            errln("Error usearch_getBreakIterator returned wrong object");
        }

        strsrch.setBreakIterator(m_en_characterbreaker_);
        if (!strsrch.getBreakIterator().equals(m_en_characterbreaker_)) {
            errln("Error usearch_getBreakIterator returned wrong object");
        }

        strsrch.setBreakIterator(m_en_wordbreaker_);
        if (!strsrch.getBreakIterator().equals(m_en_wordbreaker_)) {
            errln("Error usearch_getBreakIterator returned wrong object");
        }

        int count = 0;
        while (count < 4) {
            // special purposes for tests numbers 0-3
            SearchData        search   = BREAKITERATOREXACT[count];
            RuleBasedCollator collator = getCollator(search.collator);
            BreakIterator     breaker  = getBreakIterator(search.breaker);
                  //StringSearch      strsrch;

            text = search.text;
            pattern = search.pattern;
            if (breaker != null) {
                breaker.setText(text);
            }
            collator.setStrength(search.strength);
            strsrch = new StringSearch(pattern, new StringCharacterIterator(text), collator, breaker);
            if (strsrch.getBreakIterator() != breaker) {
                errln("Error setting break iterator");
            }
            if (!assertEqualWithStringSearch(strsrch, search)) {
                collator.setStrength(TERTIARY);
            }
            search   = BREAKITERATOREXACT[count + 1];
            breaker  = getBreakIterator(search.breaker);
            if (breaker != null) {
                breaker.setText(text);
            }
            strsrch.setBreakIterator(breaker);
            if (strsrch.getBreakIterator() != breaker) {
                errln("Error setting break iterator");
            }
            strsrch.reset();
            if (!assertEqualWithStringSearch(strsrch, search)) {
                 errln("Error at test number " + count);
            }
            count += 2;
        }
        for (count = 0; count < BREAKITERATOREXACT.length; count++) {
            if (!assertEqual(BREAKITERATOREXACT[count])) {
                errln("Error at test number " + count);
            }
        }
    }

    @Test
    public void TestBreakIteratorCanonical() {
        int        count  = 0;
        while (count < 4) {
            // special purposes for tests numbers 0-3
            SearchData     search   = BREAKITERATORCANONICAL[count];

            String text = search.text;
            String pattern = search.pattern;
            RuleBasedCollator collator = getCollator(search.collator);
            collator.setStrength(search.strength);

            BreakIterator breaker = getBreakIterator(search.breaker);
            StringSearch  strsrch = null;
            try {
                strsrch = new StringSearch(pattern, new StringCharacterIterator(text), collator, breaker);
            } catch (Exception e) {
                errln("Error creating string search data");
                return;
            }
            strsrch.setCanonical(true);
            if (!strsrch.getBreakIterator().equals(breaker)) {
                errln("Error setting break iterator");
                return;
            }
            if (!assertEqualWithStringSearch(strsrch, search)) {
                collator.setStrength(TERTIARY);
                return;
            }
            search  = BREAKITERATOREXACT[count + 1];
            breaker = getBreakIterator(search.breaker);
            breaker.setText(strsrch.getTarget());
            strsrch.setBreakIterator(breaker);
            if (!strsrch.getBreakIterator().equals(breaker)) {
                errln("Error setting break iterator");
                return;
            }
            strsrch.reset();
            strsrch.setCanonical(true);
            if (!assertEqualWithStringSearch(strsrch, search)) {
                 errln("Error at test number " + count);
                 return;
            }
            count += 2;
        }

        for (count = 0; count < BREAKITERATORCANONICAL.length; count++) {
             if (!assertEqual(BREAKITERATORCANONICAL[count])) {
                 errln("Error at test number " + count);
                 return;
             }
        }
    }

    @Test
    public void TestCanonical() {
        for (int count = 0; count < BASICCANONICAL.length; count++) {
            if (!assertCanonicalEqual(BASICCANONICAL[count])) {
                errln("Error at test number " + count);
            }
        }
    }

    @Test
    public void TestCollator() {
        // test collator that thinks "o" and "p" are the same thing
        String text = COLLATOR[0].text;
        String pattern  = COLLATOR[0].pattern;
        StringSearch strsrch = null;
        try {
            strsrch = new StringSearch(pattern, new StringCharacterIterator(text), m_en_us_, null);
        } catch (Exception e) {
            errln("Error opening string search ");
            return;
        }
        if (!assertEqualWithStringSearch(strsrch, COLLATOR[0])) {
            return;
        }
        String rules = TESTCOLLATORRULE;
        RuleBasedCollator tailored = null;
        try {
            tailored = new RuleBasedCollator(rules);
            tailored.setStrength(COLLATOR[1].strength);
        } catch (Exception e) {
            errln("Error opening rule based collator ");
            return;
        }

        strsrch.setCollator(tailored);
        if (!strsrch.getCollator().equals(tailored)) {
            errln("Error setting rule based collator");
        }
        strsrch.reset();
        if (!assertEqualWithStringSearch(strsrch, COLLATOR[1])) {
            return;
        }
        strsrch.setCollator(m_en_us_);
        strsrch.reset();
        if (!strsrch.getCollator().equals(m_en_us_)) {
            errln("Error setting rule based collator");
        }
        if (!assertEqualWithStringSearch(strsrch, COLLATOR[0])) {
           errln("Error searching collator test");
        }
    }

    @Test
    public void TestCollatorCanonical() {
        /* test collator that thinks "o" and "p" are the same thing */
        String text = COLLATORCANONICAL[0].text;
        String pattern = COLLATORCANONICAL[0].pattern;

        StringSearch strsrch = null;
        try {
            strsrch = new StringSearch(pattern, new StringCharacterIterator(text), m_en_us_, null);
            strsrch.setCanonical(true);
        } catch (Exception e) {
            errln("Error opening string search ");
        }

        if (!assertEqualWithStringSearch(strsrch, COLLATORCANONICAL[0])) {
            return;
        }

        String rules = TESTCOLLATORRULE;
        RuleBasedCollator tailored = null;
        try {
            tailored = new RuleBasedCollator(rules);
            tailored.setStrength(COLLATORCANONICAL[1].strength);
            tailored.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        } catch (Exception e) {
            errln("Error opening rule based collator ");
        }

        strsrch.setCollator(tailored);
        if (!strsrch.getCollator().equals(tailored)) {
            errln("Error setting rule based collator");
        }
        strsrch.reset();
        strsrch.setCanonical(true);
        if (!assertEqualWithStringSearch(strsrch, COLLATORCANONICAL[1])) {
            logln("COLLATORCANONICAL[1] failed");  // Error should already be reported.
        }
        strsrch.setCollator(m_en_us_);
        strsrch.reset();
        if (!strsrch.getCollator().equals(m_en_us_)) {
            errln("Error setting rule based collator");
        }
        if (!assertEqualWithStringSearch(strsrch, COLLATORCANONICAL[0])) {
            logln("COLLATORCANONICAL[0] failed");  // Error should already be reported.
        }
    }

    @Test
    public void TestCompositeBoundaries() {
        for (int count = 0; count < COMPOSITEBOUNDARIES.length; count++) {
            // logln("composite " + count);
            if (!assertEqual(COMPOSITEBOUNDARIES[count])) {
                errln("Error at test number " + count);
            }
        }
    }

    @Test
    public void TestCompositeBoundariesCanonical() {
        for (int count = 0; count < COMPOSITEBOUNDARIESCANONICAL.length; count++) {
            // logln("composite " + count);
            if (!assertCanonicalEqual(COMPOSITEBOUNDARIESCANONICAL[count])) {
                errln("Error at test number " + count);
            }
        }
    }

    @Test
    public void TestContraction() {
        String rules = CONTRACTIONRULE;
        RuleBasedCollator collator = null;
        try {
            collator = new RuleBasedCollator(rules);
            collator.setStrength(TERTIARY);
            collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        } catch (Exception e) {
            errln("Error opening collator ");
        }
        String text = "text";
        String pattern = "pattern";
        StringSearch strsrch = null;
        try {
            strsrch = new StringSearch(pattern, new StringCharacterIterator(text), collator, null);
        } catch (Exception e) {
            errln("Error opening string search ");
        }

        for (int count = 0; count< CONTRACTION.length; count++) {
            text = CONTRACTION[count].text;
            pattern = CONTRACTION[count].pattern;
            strsrch.setTarget(new StringCharacterIterator(text));
            strsrch.setPattern(pattern);
            if (!assertEqualWithStringSearch(strsrch, CONTRACTION[count])) {
                errln("Error at test number " + count);
            }
        }
    }

    @Test
    public void TestContractionCanonical() {
        String rules = CONTRACTIONRULE;
        RuleBasedCollator collator = null;
        try {
            collator = new RuleBasedCollator(rules);
            collator.setStrength(TERTIARY);
            collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        } catch (Exception e) {
            errln("Error opening collator ");
        }
        String text = "text";
        String pattern = "pattern";
        StringSearch strsrch = null;
        try {
            strsrch = new StringSearch(pattern, new StringCharacterIterator(text), collator, null);
            strsrch.setCanonical(true);
        } catch (Exception e) {
            errln("Error opening string search");
        }

        for (int count = 0; count < CONTRACTIONCANONICAL.length; count++) {
            text = CONTRACTIONCANONICAL[count].text;
            pattern = CONTRACTIONCANONICAL[count].pattern;
            strsrch.setTarget(new StringCharacterIterator(text));
            strsrch.setPattern(pattern);
            if (!assertEqualWithStringSearch(strsrch, CONTRACTIONCANONICAL[count])) {
                errln("Error at test number " + count);
            }
        }
    }

    @Test
    public void TestGetMatch() {
        SearchData search = MATCH[0];
        String text = search.text;
        String pattern = search.pattern;

        StringSearch strsrch = null;
        try {
            strsrch = new StringSearch(pattern, new StringCharacterIterator(text), m_en_us_, null);
        } catch (Exception e) {
            errln("Error opening string search ");
            return;
        }

        int           count      = 0;
        int   matchindex = search.offset[count];
        String matchtext;
        while (matchindex >= 0) {
            int matchlength = search.size[count];
            strsrch.next();
            if (matchindex != strsrch.getMatchStart() ||
                matchlength != strsrch.getMatchLength()) {
                errln("Text: " + search.text);
                errln("Pattern: " + strsrch.getPattern());
                errln("Error match found at " + strsrch.getMatchStart() + ", " + strsrch.getMatchLength());
                return;
            }
            count++;

            matchtext = strsrch.getMatchedText();
            if (matchtext.length() != matchlength){
                errln("Error getting match text");
            }
            matchindex = search.offset[count];
        }
        strsrch.next();
        if (strsrch.getMatchStart()  != StringSearch.DONE ||
            strsrch.getMatchLength() != 0) {
            errln("Error end of match not found");
        }
        matchtext = strsrch.getMatchedText();
        if (matchtext != null) {
            errln("Error getting null matches");
        }
    }

    @Test
    public void TestGetSetAttribute() {
        String  pattern = "pattern";
        String  text = "text";
        StringSearch  strsrch = null;
        try {
            strsrch = new StringSearch(pattern, new StringCharacterIterator(text), m_en_us_, null);
        } catch (Exception e) {
            errln("Error opening search");
            return;
        }

        if (strsrch.isOverlapping()) {
            errln("Error default overlaping should be false");
        }
        strsrch.setOverlapping(true);
        if (!strsrch.isOverlapping()) {
            errln("Error setting overlap true");
        }
        strsrch.setOverlapping(false);
        if (strsrch.isOverlapping()) {
            errln("Error setting overlap false");
        }

        strsrch.setCanonical(true);
        if (!strsrch.isCanonical()) {
            errln("Error setting canonical match true");
        }
        strsrch.setCanonical(false);
        if (strsrch.isCanonical()) {
            errln("Error setting canonical match false");
        }

        if (strsrch.getElementComparisonType() != STANDARD_ELEMENT_COMPARISON) {
            errln("Error default element comparison type should be STANDARD_ELEMENT_COMPARISON");
        }
        strsrch.setElementComparisonType(ElementComparisonType.PATTERN_BASE_WEIGHT_IS_WILDCARD);
        if (strsrch.getElementComparisonType() != ElementComparisonType.PATTERN_BASE_WEIGHT_IS_WILDCARD) {
            errln("Error setting element comparison type PATTERN_BASE_WEIGHT_IS_WILDCARD");
        }
    }

    @Test
    public void TestGetSetOffset() {
        String  pattern = "1234567890123456";
        String  text  = "12345678901234567890123456789012";
        StringSearch  strsrch = null;
        try {
            strsrch = new StringSearch(pattern, new StringCharacterIterator(text), m_en_us_, null);
        } catch (Exception e) {
            errln("Error opening search");

            return;
        }

        /* testing out of bounds error */
        try {
            strsrch.setIndex(-1);
            errln("Error expecting set offset error");
        } catch (IndexOutOfBoundsException e) {
            logln("PASS: strsrch.setIndex(-1) failed as expected");
        }

        try {
            strsrch.setIndex(128);
            errln("Error expecting set offset error");
        } catch (IndexOutOfBoundsException e) {
            logln("PASS: strsrch.setIndex(128) failed as expected");
        }

        for (int index = 0; index < BASIC.length; index++) {
            SearchData  search      = BASIC[index];

            text =search.text;
            pattern = search.pattern;
            strsrch.setTarget(new StringCharacterIterator(text));
            strsrch.setPattern(pattern);
            strsrch.getCollator().setStrength(search.strength);
            strsrch.reset();

            int count = 0;
            int matchindex  = search.offset[count];

            while (matchindex >= 0) {
                int matchlength = search.size[count];
                strsrch.next();
                if (matchindex != strsrch.getMatchStart() ||
                    matchlength != strsrch.getMatchLength()) {
                    errln("Text: " + text);
                    errln("Pattern: " + strsrch.getPattern());
                    errln("Error match found at " + strsrch.getMatchStart() + ", " + strsrch.getMatchLength());
                    return;
                }
                matchindex = search.offset[count + 1] == -1 ? -1 :
                             search.offset[count + 2];
                if (search.offset[count + 1] != -1) {
                    strsrch.setIndex(search.offset[count + 1] + 1);
                    if (strsrch.getIndex() != search.offset[count + 1] + 1) {
                        errln("Error setting offset\n");
                        return;
                    }
                }

                count += 2;
            }
            strsrch.next();
            if (strsrch.getMatchStart() != StringSearch.DONE) {
                errln("Text: " + text);
                errln("Pattern: " + strsrch.getPattern());
                errln("Error match found at " + strsrch.getMatchStart() + ", " + strsrch.getMatchLength());
                return;
            }
        }
        strsrch.getCollator().setStrength(TERTIARY);
    }

    @Test
    public void TestGetSetOffsetCanonical() {

        String  text = "text";
        String  pattern = "pattern";
        StringSearch  strsrch = null;
        try {
            strsrch = new StringSearch(pattern, new StringCharacterIterator(text), m_en_us_, null);
        } catch (Exception e) {
            errln("Fail to open StringSearch!");
            return;
        }
        strsrch.setCanonical(true);
        //TODO: setCanonical is not sufficient for canonical match. See #10725
        strsrch.getCollator().setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        /* testing out of bounds error */
        try {
            strsrch.setIndex(-1);
            errln("Error expecting set offset error");
        } catch (IndexOutOfBoundsException e) {
            logln("PASS: strsrch.setIndex(-1) failed as expected");
        }
        try {
            strsrch.setIndex(128);
            errln("Error expecting set offset error");
        } catch (IndexOutOfBoundsException e) {
            logln("PASS: strsrch.setIndex(128) failed as expected");
        }

        for (int index = 0; index < BASICCANONICAL.length; index++) {
            SearchData  search      = BASICCANONICAL[index];
            text = search.text;
            pattern = search.pattern;
            strsrch.setTarget(new StringCharacterIterator(text));
            strsrch.setPattern(pattern);
            int         count       = 0;
            int matchindex  = search.offset[count];
            while (matchindex >= 0) {
                int matchlength = search.size[count];
                strsrch.next();
                if (matchindex != strsrch.getMatchStart() ||
                    matchlength != strsrch.getMatchLength()) {
                    errln("Text: " + text);
                    errln("Pattern: " + strsrch.getPattern());
                    errln("Error match found at " + strsrch.getMatchStart() + ", " + strsrch.getMatchLength());
                    return;
                }
                matchindex = search.offset[count + 1] == -1 ? -1 :
                             search.offset[count + 2];
                if (search.offset[count + 1] != -1) {
                    strsrch.setIndex(search.offset[count + 1] + 1);
                    if (strsrch.getIndex() != search.offset[count + 1] + 1) {
                        errln("Error setting offset");
                        return;
                    }
                }

                count += 2;
            }
            strsrch.next();
            if (strsrch.getMatchStart() != StringSearch.DONE) {
                errln("Text: " + text);
                errln("Pattern: %s" + strsrch.getPattern());
                errln("Error match found at " + strsrch.getMatchStart() + ", " + strsrch.getMatchLength());
                return;
            }
        }
        strsrch.getCollator().setStrength(TERTIARY);
        strsrch.getCollator().setDecomposition(Collator.NO_DECOMPOSITION);
    }

    @Test
    public void TestIgnorable() {
        String rules = IGNORABLERULE;
        int        count  = 0;
        RuleBasedCollator collator = null;
        try {
            collator = new RuleBasedCollator(rules);
            collator.setStrength(IGNORABLE[count].strength);
            collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        } catch (Exception e) {
            errln("Error opening collator ");
            return;
        }
        String pattern = "pattern";
        String text = "text";
        StringSearch strsrch = null;
        try {
            strsrch = new StringSearch(pattern, new StringCharacterIterator(text), collator, null);
        } catch (Exception e) {
            errln("Error opening string search ");
            return;
        }

        for (; count < IGNORABLE.length; count++) {
            text = IGNORABLE[count].text;
            pattern = IGNORABLE[count].pattern;
            strsrch.setTarget(new StringCharacterIterator(text));
            strsrch.setPattern(pattern);
            if (!assertEqualWithStringSearch(strsrch, IGNORABLE[count])) {
                errln("Error at test number " + count);
            }
        }
    }

    @Test
    public void TestInitialization() {
        String  pattern;
        String  text;
        String  temp = "a";
        StringSearch  result;

        /* simple test on the pattern ce construction */
        pattern = temp + temp;
        text = temp + temp + temp;
        try {
            result = new StringSearch(pattern, new StringCharacterIterator(text), m_en_us_, null);
        } catch (Exception e) {
            errln("Error opening search ");
            return;
        }

        /* testing if an extremely large pattern will fail the initialization */
        pattern = "";
        for (int count = 0; count < 512; count ++) {
            pattern += temp;
        }
        try {
            result = new StringSearch(pattern, new StringCharacterIterator(text), m_en_us_, null);
            logln("pattern:" + result.getPattern());
        } catch (Exception e) {
            errln("Fail: an extremely large pattern will fail the initialization");
            return;
        }
    }

    @Test
    public void TestNormCanonical() {
        m_en_us_.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        for (int count = 0; count < NORMCANONICAL.length; count++) {
            if (!assertCanonicalEqual(NORMCANONICAL[count])) {
                errln("Error at test number " + count);
            }
        }
        m_en_us_.setDecomposition(Collator.NO_DECOMPOSITION);
    }

    @Test
    public void TestNormExact() {
        int count;

        m_en_us_.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
        for (count = 0; count < BASIC.length; count++) {
            if (!assertEqual(BASIC[count])) {
                errln("Error at test number " + count);
            }
        }
        for (count = 0; count < NORMEXACT.length; count++) {
            if (!assertEqual(NORMEXACT[count])) {
                errln("Error at test number " + count);
            }
        }
        m_en_us_.setDecomposition(Collator.NO_DECOMPOSITION);
        for (count = 0; count < NONNORMEXACT.length; count++) {
            if (!assertEqual(NONNORMEXACT[count])) {
                errln("Error at test number " + count);
            }
        }
    }

    @Test
    public void TestOpenClose() {
        StringSearch            result;
        BreakIterator           breakiter = m_en_wordbreaker_;
        String           pattern = "";
        String           text = "";
        String           temp  = "a";
        StringCharacterIterator  chariter= new StringCharacterIterator(text);

        /* testing null arguments */
        try {
            result = new StringSearch(pattern, new StringCharacterIterator(text), null, null);
            errln("Error: null arguments should produce an error");
        } catch (Exception e) {
            logln("PASS: null arguments failed as expected");
        }

        chariter.setText(text);
        try {
            result = new StringSearch(pattern, chariter, null, null);
            errln("Error: null arguments should produce an error");
        } catch (Exception e) {
            logln("PASS: null arguments failed as expected");
        }

        text  = String.valueOf(0x1);
        try {
            result = new StringSearch(pattern, new StringCharacterIterator(text), null, null);
            errln("Error: Empty pattern should produce an error");
        } catch (Exception e) {
            logln("PASS: Empty pattern failed as expected");
        }

        chariter.setText(text);
        try {
            result = new StringSearch(pattern, chariter, null, null);
            errln("Error: Empty pattern should produce an error");
        } catch (Exception e) {
            logln("PASS: Empty pattern failed as expected");
        }

        text = "";
        pattern =temp;
        try {
            result = new StringSearch(pattern, new StringCharacterIterator(text), null, null);
            errln("Error: Empty text should produce an error");
        } catch (Exception e) {
            logln("PASS: Empty text failed as expected");
        }

        chariter.setText(text);
        try {
            result = new StringSearch(pattern, chariter, null, null);
            errln("Error: Empty text should produce an error");
        } catch (Exception e) {
            logln("PASS: Empty text failed as expected");
        }

        text += temp;
        try {
            result = new StringSearch(pattern, new StringCharacterIterator(text), null, null);
            errln("Error: null arguments should produce an error");
        } catch (Exception e) {
            logln("PASS: null arguments failed as expected");
        }

        chariter.setText(text);
        try {
            result = new StringSearch(pattern, chariter, null, null);
            errln("Error: null arguments should produce an error");
        } catch (Exception e) {
            logln("PASS: null arguments failed as expected");
        }

        try {
            result = new StringSearch(pattern, new StringCharacterIterator(text), m_en_us_, null);
        } catch (Exception e) {
            errln("Error: null break iterator is valid for opening search");
        }

        try {
            result = new StringSearch(pattern, chariter, m_en_us_, null);
        } catch (Exception e) {
            errln("Error: null break iterator is valid for opening search");
        }

        try {
            result = new StringSearch(pattern, new StringCharacterIterator(text), Locale.ENGLISH);
        } catch (Exception e) {
            errln("Error: null break iterator is valid for opening search");
        }

        try {
            result = new StringSearch(pattern, chariter, Locale.ENGLISH);
        } catch (Exception e) {
            errln("Error: null break iterator is valid for opening search");
        }

        try {
            result = new StringSearch(pattern, new StringCharacterIterator(text), m_en_us_, breakiter);
        } catch (Exception e) {
            errln("Error: Break iterator is valid for opening search");
        }

        try {
            result = new StringSearch(pattern, chariter, m_en_us_, null);
            logln("pattern:" + result.getPattern());
        } catch (Exception e) {
            errln("Error: Break iterator is valid for opening search");
        }
    }

    @Test
    public void TestOverlap() {
        int count;

        for (count = 0; count < OVERLAP.length; count++) {
            if (!assertEqualWithAttribute(OVERLAP[count], false, true)) {
                errln("Error at overlap test number " + count);
            }
        }

        for (count = 0; count < NONOVERLAP.length; count++) {
            if (!assertEqual(NONOVERLAP[count])) {
                errln("Error at non overlap test number " + count);
            }
        }

        for (count = 0; count < OVERLAP.length && count < NONOVERLAP.length; count++) {
            SearchData search = (OVERLAP[count]);
            String text = search.text;
            String pattern = search.pattern;

            RuleBasedCollator collator = getCollator(search.collator);
            StringSearch strsrch = null;
            try {
                strsrch  = new StringSearch(pattern, new StringCharacterIterator(text), collator, null);
            } catch (Exception e) {
                errln("error open StringSearch");
                return;
            }

            strsrch.setOverlapping(true);
            if (!strsrch.isOverlapping()) {
                errln("Error setting overlap option");
            }
            if (!assertEqualWithStringSearch(strsrch, search)) {
                return;
            }

            search = NONOVERLAP[count];
            strsrch.setOverlapping(false);
            if (strsrch.isOverlapping()) {
                errln("Error setting overlap option");
            }
            strsrch.reset();
            if (!assertEqualWithStringSearch(strsrch, search)) {
                errln("Error at test number " + count);
             }
        }
    }

    @Test
    public void TestOverlapCanonical() {
        int count;

        for (count = 0; count < OVERLAPCANONICAL.length; count++) {
            if (!assertEqualWithAttribute(OVERLAPCANONICAL[count], true, true)) {
                errln("Error at overlap test number %d" + count);
            }
        }

        for (count = 0; count < NONOVERLAP.length; count++) {
            if (!assertCanonicalEqual(NONOVERLAPCANONICAL[count])) {
                errln("Error at non overlap test number %d" + count);
            }
        }

        for (count = 0; count < OVERLAPCANONICAL.length && count < NONOVERLAPCANONICAL.length; count++) {
            SearchData search = OVERLAPCANONICAL[count];
            RuleBasedCollator collator = getCollator(search.collator);
            StringSearch strsrch = new StringSearch(search.pattern, new StringCharacterIterator(search.text), collator, null);
            strsrch.setCanonical(true);
            strsrch.setOverlapping(true);
            if (strsrch.isOverlapping() != true) {
                errln("Error setting overlap option");
            }
            if (!assertEqualWithStringSearch(strsrch, search)) {
                strsrch = null;
                return;
            }
            search = NONOVERLAPCANONICAL[count];
            strsrch.setOverlapping(false);
            if (strsrch.isOverlapping() != false) {
                errln("Error setting overlap option");
            }
            strsrch.reset();
            if (!assertEqualWithStringSearch(strsrch, search)) {
                strsrch = null;
                errln("Error at test number %d" + count);
             }
        }
    }

    @Test
    public void TestPattern() {
        m_en_us_.setStrength(PATTERN[0].strength);
        StringSearch strsrch = new StringSearch(PATTERN[0].pattern, new StringCharacterIterator(PATTERN[0].text), m_en_us_, null);

        if (strsrch.getPattern() != PATTERN[0].pattern) {
            errln("Error setting pattern");
        }
        if (!assertEqualWithStringSearch(strsrch, PATTERN[0])) {
            m_en_us_.setStrength(TERTIARY);
            if (strsrch != null) {
                strsrch = null;
            }
            return;
        }

        strsrch.setPattern(PATTERN[1].pattern);
        if (PATTERN[1].pattern != strsrch.getPattern()) {
            errln("Error setting pattern");
            m_en_us_.setStrength(TERTIARY);
            if (strsrch != null) {
                strsrch = null;
            }
            return;
        }
        strsrch.reset();

        if (!assertEqualWithStringSearch(strsrch, PATTERN[1])) {
            m_en_us_.setStrength(TERTIARY);
            if (strsrch != null) {
                strsrch = null;
            }
            return;
        }

        strsrch.setPattern(PATTERN[0].pattern);
        if (PATTERN[0].pattern != strsrch.getPattern()) {
            errln("Error setting pattern");
            m_en_us_.setStrength(TERTIARY);
            if (strsrch != null) {
                strsrch = null;
            }
            return;
        }
            strsrch.reset();

        if (!assertEqualWithStringSearch(strsrch, PATTERN[0])) {
            m_en_us_.setStrength(TERTIARY);
            if (strsrch != null) {
                strsrch = null;
            }
            return;
        }
        /* enormous pattern size to see if this crashes */
        String pattern = "";
        for (int templength = 0; templength != 512; templength ++) {
            pattern += 0x61;
        }
        try{
            strsrch.setPattern(pattern);
        }catch(Exception e) {
            errln("Error setting pattern with size 512");
        }

        m_en_us_.setStrength(TERTIARY);
        if (strsrch != null) {
            strsrch = null;
        }
    }

    @Test
    public void TestPatternCanonical() {
        //StringCharacterIterator text = new StringCharacterIterator(PATTERNCANONICAL[0].text);
        m_en_us_.setStrength(PATTERNCANONICAL[0].strength);
        StringSearch strsrch = new StringSearch(PATTERNCANONICAL[0].pattern, new StringCharacterIterator(PATTERNCANONICAL[0].text),
                                                m_en_us_, null);
        strsrch.setCanonical(true);

        if (PATTERNCANONICAL[0].pattern != strsrch.getPattern()) {
            errln("Error setting pattern");
        }
        if (!assertEqualWithStringSearch(strsrch, PATTERNCANONICAL[0])) {
            m_en_us_.setStrength(TERTIARY);
            strsrch = null;
            return;
        }

        strsrch.setPattern(PATTERNCANONICAL[1].pattern);
        if (PATTERNCANONICAL[1].pattern != strsrch.getPattern()) {
            errln("Error setting pattern");
            m_en_us_.setStrength(TERTIARY);
            strsrch = null;
            return;
        }
        strsrch.reset();
        strsrch.setCanonical(true);

        if (!assertEqualWithStringSearch(strsrch, PATTERNCANONICAL[1])) {
            m_en_us_.setStrength(TERTIARY);
            strsrch = null;
            return;
        }

        strsrch.setPattern(PATTERNCANONICAL[0].pattern);
        if (PATTERNCANONICAL[0].pattern != strsrch.getPattern()) {
            errln("Error setting pattern");
            m_en_us_.setStrength(TERTIARY);
            strsrch = null;
            return;
        }

        strsrch.reset();
        strsrch.setCanonical(true);
        if (!assertEqualWithStringSearch(strsrch, PATTERNCANONICAL[0])) {
            m_en_us_.setStrength(TERTIARY);
            strsrch = null;
            return;
        }
    }

    @Test
    public void TestReset() {
        StringCharacterIterator text = new StringCharacterIterator("fish fish");
        String pattern = "s";

        StringSearch  strsrch = new StringSearch(pattern, text, m_en_us_, null);
        strsrch.setOverlapping(true);
        strsrch.setCanonical(true);
        strsrch.setIndex(9);
        strsrch.reset();
        if (strsrch.isCanonical() || strsrch.isOverlapping() ||
            strsrch.getIndex() != 0 || strsrch.getMatchLength() != 0 ||
            strsrch.getMatchStart() != SearchIterator.DONE) {
                errln("Error resetting string search");
        }

        strsrch.previous();
        if (strsrch.getMatchStart() != 7 || strsrch.getMatchLength() != 1) {
            errln("Error resetting string search\n");
        }
    }

    @Test
    public void TestSetMatch() {
        for (int count = 0; count < MATCH.length; count++) {
            SearchData     search = MATCH[count];
            StringSearch strsrch = new StringSearch(search.pattern, new StringCharacterIterator(search.text),
                                                    m_en_us_, null);

            int size = 0;
            while (search.offset[size] != -1) {
                size ++;
            }

            if (strsrch.first() != search.offset[0]) {
                errln("Error getting first match");
            }
            if (strsrch.last() != search.offset[size -1]) {
                errln("Error getting last match");
            }

            int index = 0;
            while (index < size) {
                if (index + 2 < size) {
                    if (strsrch.following(search.offset[index + 2] - 1) != search.offset[index + 2]) {
                        errln("Error getting following match at index " + (search.offset[index + 2]-1));
                    }
                }
                if (index + 1 < size) {
                    if (strsrch.preceding(search.offset[index + 1] + search.size[index + 1] + 1) != search.offset[index + 1]) {
                        errln("Error getting preceeding match at index " + (search.offset[index + 1] + 1));
                    }
                }
                index += 2;
            }

            if (strsrch.following(search.text.length()) != SearchIterator.DONE) {
                errln("Error expecting out of bounds match");
            }
            if (strsrch.preceding(0) != SearchIterator.DONE) {
                errln("Error expecting out of bounds match");
            }
        }
    }

    @Test
    public void TestStrength() {
        for (int count = 0; count < STRENGTH.length; count++) {
            if (!assertEqual(STRENGTH[count])) {
                errln("Error at test number " + count);
            }
        }
    }

    @Test
    public void TestStrengthCanonical() {
        for (int count = 0; count < STRENGTHCANONICAL.length; count++) {
            if (!assertCanonicalEqual(STRENGTHCANONICAL[count])) {
                errln("Error at test number" + count);
            }
        }
    }

    @Test
    public void TestSupplementary() {
        for (int count = 0; count < SUPPLEMENTARY.length; count++) {
            if (!assertEqual(SUPPLEMENTARY[count])) {
                errln("Error at test number " + count);
            }
        }
    }

    @Test
    public void TestSupplementaryCanonical() {
        for (int count = 0; count < SUPPLEMENTARYCANONICAL.length; count++) {
            if (!assertCanonicalEqual(SUPPLEMENTARYCANONICAL[count])) {
                errln("Error at test number" + count);
            }
        }
    }

    @Test
    public void TestText() {
        SearchData TEXT[] = {
            SD("the foxy brown fox", "fox", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(4, 15, -1), IA(3, 3)),
            SD("the quick brown fox", "fox", null, TERTIARY, STANDARD_ELEMENT_COMPARISON, null, IA(16, -1), IA(3))
        };
        StringCharacterIterator t = new StringCharacterIterator(TEXT[0].text);
        StringSearch strsrch = new StringSearch(TEXT[0].pattern, t, m_en_us_, null);

        if (!t.equals(strsrch.getTarget())) {
            errln("Error setting text");
        }
        if (!assertEqualWithStringSearch(strsrch, TEXT[0])) {
            errln("Error at assertEqualWithStringSearch");
            return;
        }

        t = new StringCharacterIterator(TEXT[1].text);
        strsrch.setTarget(t);
        if (!t.equals(strsrch.getTarget())) {
            errln("Error setting text");
            return;
        }

        if (!assertEqualWithStringSearch(strsrch, TEXT[1])) {
            errln("Error at assertEqualWithStringSearch");
            return;
        }
    }

    @Test
    public void TestTextCanonical() {
        StringCharacterIterator t = new StringCharacterIterator(TEXTCANONICAL[0].text);
        StringSearch strsrch = new StringSearch(TEXTCANONICAL[0].pattern, t, m_en_us_, null);
        strsrch.setCanonical(true);

        if (!t.equals(strsrch.getTarget())) {
            errln("Error setting text");
        }
        if (!assertEqualWithStringSearch(strsrch, TEXTCANONICAL[0])) {
            strsrch = null;
            return;
        }

        t = new StringCharacterIterator(TEXTCANONICAL[1].text);
        strsrch.setTarget(t);
        if (!t.equals(strsrch.getTarget())) {
            errln("Error setting text");
            strsrch = null;
            return;
        }

        if (!assertEqualWithStringSearch(strsrch, TEXTCANONICAL[1])) {
            strsrch = null;
            return;
        }

        t = new StringCharacterIterator(TEXTCANONICAL[0].text);
        strsrch.setTarget(t);
        if (!t.equals(strsrch.getTarget())) {
            errln("Error setting text");
            strsrch = null;
            return;
        }

        if (!assertEqualWithStringSearch(strsrch, TEXTCANONICAL[0])) {
            errln("Error at assertEqualWithStringSearch");
            strsrch = null;
            return;
        }
    }

    @Test
    public void TestVariable() {
        m_en_us_.setAlternateHandlingShifted(true);
        for (int count = 0; count < VARIABLE.length; count++) {
            // logln("variable" + count);
            if (!assertEqual(VARIABLE[count])) {
                errln("Error at test number " + count);
            }
        }
        m_en_us_.setAlternateHandlingShifted(false);
    }

    @Test
    public void TestVariableCanonical() {
        m_en_us_.setAlternateHandlingShifted(true);
        for (int count = 0; count < VARIABLE.length; count++) {
            // logln("variable " + count);
            if (!assertCanonicalEqual(VARIABLE[count])) {
                errln("Error at test number " + count);
            }
        }
        m_en_us_.setAlternateHandlingShifted(false);
    }

    @Test
    public void TestSubClass()
    {
        class TestSearch extends SearchIterator
        {
            String pattern;
            String text;

            TestSearch(StringCharacterIterator target, BreakIterator breaker,
                       String pattern)
            {
                super(target, breaker);
                this.pattern = pattern;
                StringBuffer buffer = new StringBuffer();
                while (targetText.getIndex() != targetText.getEndIndex()) {
                    buffer.append(targetText.current());
                    targetText.next();
                }
                text = buffer.toString();
                targetText.setIndex(targetText.getBeginIndex());
            }
            protected int handleNext(int start)
            {
                int match = text.indexOf(pattern, start);
                if (match < 0) {
                    targetText.last();
                    return DONE;
                }
                targetText.setIndex(match);
                setMatchLength(pattern.length());
                return match;
            }
            protected int handlePrevious(int start)
            {
                int match = text.lastIndexOf(pattern, start - 1);
                if (match < 0) {
                    targetText.setIndex(0);
                    return DONE;
                }
                targetText.setIndex(match);
                setMatchLength(pattern.length());
                return match;
            }

            public int getIndex()
            {
                int result = targetText.getIndex();
                if (result < 0 || result >= text.length()) {
                    return DONE;
                }
                return result;
            }
        }

        TestSearch search = new TestSearch(
                            new StringCharacterIterator("abc abcd abc"),
                            null, "abc");
        int expected[] = {0, 4, 9};
        for (int i = 0; i < expected.length; i ++) {
            if (search.next() != expected[i]) {
                errln("Error getting next match");
            }
            if (search.getMatchLength() != search.pattern.length()) {
                errln("Error getting next match length");
            }
        }
        if (search.next() != SearchIterator.DONE) {
            errln("Error should have reached the end of the iteration");
        }
        for (int i = expected.length - 1; i >= 0; i --) {
            if (search.previous() != expected[i]) {
                errln("Error getting next match");
            }
            if (search.getMatchLength() != search.pattern.length()) {
                errln("Error getting next match length");
            }
        }
        if (search.previous() != SearchIterator.DONE) {
            errln("Error should have reached the start of the iteration");
        }
    }
    
    //Test for ticket 5024
    @Test
    public void TestDiactricMatch() {
        String pattern = "pattern";
        String text = "text";
        StringSearch strsrch = null;
        try {
            strsrch = new StringSearch(pattern, text);
        } catch (Exception e) {
            errln("Error opening string search ");
            return;
        }

        for (int count = 0; count < DIACTRICMATCH.length; count++) {
            strsrch.setCollator(getCollator(DIACTRICMATCH[count].collator));
            strsrch.getCollator().setStrength(DIACTRICMATCH[count].strength);
            strsrch.setBreakIterator(getBreakIterator(DIACTRICMATCH[count].breaker));
            strsrch.reset();
            text = DIACTRICMATCH[count].text;
            pattern = DIACTRICMATCH[count].pattern;
            strsrch.setTarget(new StringCharacterIterator(text));
            strsrch.setPattern(pattern);
            if (!assertEqualWithStringSearch(strsrch, DIACTRICMATCH[count])) {
                errln("Error at test number " + count);
            }
        }
    }

    @Test
    public void TestUsingSearchCollator() {
        String scKoText =
            " " +
    /*01*/  "\uAC00 " +                   // simple LV Hangul
    /*03*/  "\uAC01 " +                   // simple LVT Hangul
    /*05*/  "\uAC0F " +                   // LVTT, last jamo expands for search
    /*07*/  "\uAFFF " +                   // LLVVVTT, every jamo expands for search
    /*09*/  "\u1100\u1161\u11A8 " +       // 0xAC01 as conjoining jamo
    /*13*/  "\u1100\u1161\u1100 " +       // 0xAC01 as basic conjoining jamo (per search rules)
    /*17*/  "\u3131\u314F\u3131 " +       // 0xAC01 as compatibility jamo
    /*21*/  "\u1100\u1161\u11B6 " +       // 0xAC0F as conjoining jamo; last expands for search
    /*25*/  "\u1100\u1161\u1105\u1112 " + // 0xAC0F as basic conjoining jamo; last expands for search
    /*30*/  "\u1101\u1170\u11B6 " +       // 0xAFFF as conjoining jamo; all expand for search
    /*34*/  "\u00E6 " +                   // small letter ae, expands
    /*36*/  "\u1E4D " +                   // small letter o with tilde and acute, decomposes
            "";

        String scKoPat0 = "\uAC01";
        String scKoPat1 = "\u1100\u1161\u11A8"; // 0xAC01 as conjoining jamo
        String scKoPat2 = "\uAC0F";
        String scKoPat3 = "\u1100\u1161\u1105\u1112"; // 0xAC0F as basic conjoining jamo
        String scKoPat4 = "\uAFFF";
        String scKoPat5 = "\u1101\u1170\u11B6"; // 0xAFFF as conjoining jamo

        int[] scKoSrchOff01 = { 3,  9, 13 };
        int[] scKoSrchOff23 = { 5, 21, 25 };
        int[] scKoSrchOff45 = { 7, 30     };

        int[] scKoStndOff01 = { 3,  9 };
        int[] scKoStndOff2  = { 5, 21 };
        int[] scKoStndOff3  = { 25    };
        int[] scKoStndOff45 = { 7, 30 };

        class PatternAndOffsets {
            private String pattern;
            private int[] offsets;
            PatternAndOffsets(String pat, int[] offs) {
                pattern = pat;
                offsets = offs;
            }
            public String getPattern() { return pattern; }
            public int[] getOffsets() { return offsets; }
        }
        final PatternAndOffsets[] scKoSrchPatternsOffsets = { 
            new PatternAndOffsets( scKoPat0, scKoSrchOff01 ),
            new PatternAndOffsets( scKoPat1, scKoSrchOff01 ),
            new PatternAndOffsets( scKoPat2, scKoSrchOff23 ),
            new PatternAndOffsets( scKoPat3, scKoSrchOff23 ),
            new PatternAndOffsets( scKoPat4, scKoSrchOff45 ),
            new PatternAndOffsets( scKoPat5, scKoSrchOff45 ),
        };
        final PatternAndOffsets[] scKoStndPatternsOffsets = { 
            new PatternAndOffsets( scKoPat0, scKoStndOff01 ),
            new PatternAndOffsets( scKoPat1, scKoStndOff01 ),
            new PatternAndOffsets( scKoPat2, scKoStndOff2  ),
            new PatternAndOffsets( scKoPat3, scKoStndOff3  ),
            new PatternAndOffsets( scKoPat4, scKoStndOff45 ),
            new PatternAndOffsets( scKoPat5, scKoStndOff45 ),
        };

        class TUSCItem {
            private String localeString;
            private String text;
            private PatternAndOffsets[] patternsAndOffsets;
            TUSCItem(String locStr, String txt, PatternAndOffsets[] patsAndOffs) {
                localeString = locStr;
                text = txt;
                patternsAndOffsets = patsAndOffs;
            }
            public String getLocaleString() { return localeString; }
            public String getText() { return text; }
            public PatternAndOffsets[] getPatternsAndOffsets() { return patternsAndOffsets; }
        }
        final TUSCItem[] tuscItems = { 
            new TUSCItem( "root",                  scKoText, scKoStndPatternsOffsets ),
            new TUSCItem( "root@collation=search", scKoText, scKoSrchPatternsOffsets ),
            new TUSCItem( "ko@collation=search",   scKoText, scKoSrchPatternsOffsets ),
        };
        
        String dummyPat = "a";

        for (TUSCItem tuscItem: tuscItems) {
            String localeString = tuscItem.getLocaleString();
            ULocale uloc = new ULocale(localeString);
            RuleBasedCollator col = null;
            try {
                col = (RuleBasedCollator)Collator.getInstance(uloc);
            } catch (Exception e) {
                errln("Error: in locale " + localeString + ", err in Collator.getInstance");
                continue;
            }
            StringCharacterIterator ci = new StringCharacterIterator(tuscItem.getText());
            StringSearch srch = new StringSearch(dummyPat, ci, col);
            for ( PatternAndOffsets patternAndOffsets: tuscItem.getPatternsAndOffsets() ) {
                srch.setPattern(patternAndOffsets.getPattern());
                int[] offsets = patternAndOffsets.getOffsets();
                int ioff, noff = offsets.length;
                int offset;

                srch.reset();
                ioff = 0;
                while (true) {
                    offset = srch.next();
                    if (offset == SearchIterator.DONE) {
                        break;
                    }
                    if ( ioff < noff ) {
                        if ( offset != offsets[ioff] ) {
                            errln("Error: in locale " + localeString + ", expected SearchIterator.next() " + offsets[ioff] + ", got " + offset);
                            //ioff = noff;
                            //break;
                        }
                        ioff++;
                    } else {
                        errln("Error: in locale " + localeString + ", SearchIterator.next() returned more matches than expected");
                    }
                }
                if ( ioff < noff ) {
                    errln("Error: in locale " + localeString + ", SearchIterator.next() returned fewer matches than expected");
                }

                srch.reset();
                ioff = noff;
                while (true) {
                    offset = srch.previous();
                    if (offset == SearchIterator.DONE) {
                        break;
                    }
                    if ( ioff > 0 ) {
                        ioff--;
                        if ( offset != offsets[ioff] ) {
                             errln("Error: in locale " + localeString + ", expected SearchIterator.previous() " + offsets[ioff] + ", got " + offset);
                            //ioff = 0;
                            // break;
                        }
                    } else {
                        errln("Error: in locale " + localeString + ", expected SearchIterator.previous() returned more matches than expected");
                    }
                }
                if ( ioff > 0 ) {
                    errln("Error: in locale " + localeString + ", expected SearchIterator.previous() returned fewer matches than expected");
                }
            }
        }
    }

    @Test
    public void TestIndicPrefixMatch() {
        for (int count = 0; count < INDICPREFIXMATCH.length; count++) {
            if (!assertEqual(INDICPREFIXMATCH[count])) {
                errln("Error at test number" + count);
            }
        }
    }

 

}
