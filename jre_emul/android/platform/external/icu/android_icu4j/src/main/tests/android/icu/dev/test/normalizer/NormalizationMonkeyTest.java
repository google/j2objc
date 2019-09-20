/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2004, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package android.icu.dev.test.normalizer;

import java.util.Random;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.lang.UCharacter;
import android.icu.lang.UCharacterCategory;
import android.icu.text.Normalizer;
import android.icu.text.UTF16;

public class NormalizationMonkeyTest extends TestFmwk {
    int loopCount = 100;
    int maxCharCount = 20;
    int maxCodePoint = 0x10ffff;
    Random random = null; // initialized in getTestSource
    UnicodeNormalizer unicode_NFD;
    UnicodeNormalizer unicode_NFC;
    UnicodeNormalizer unicode_NFKD;
    UnicodeNormalizer unicode_NFKC;
    
    public NormalizationMonkeyTest() {
    }
    
    @Test
    public void TestNormalize() {
        if (unicode_NFD == null) {
            try {
                unicode_NFD = new UnicodeNormalizer(UnicodeNormalizer.D, true);
                unicode_NFC = new UnicodeNormalizer(UnicodeNormalizer.C, true);
                unicode_NFKD = new UnicodeNormalizer(UnicodeNormalizer.KD, true);
                unicode_NFKC = new UnicodeNormalizer(UnicodeNormalizer.KC, true);
            }
            catch (Exception e) {
                errln("Normalization tests could not be run: " + e.getMessage());
            }
        }
        int i = 0;
        while (i < loopCount) {
            String source = getTestSource();
            logln("Test source:" + source);
            //NFD
            String uncodeNorm = unicode_NFD.normalize(source);
            String icuNorm = Normalizer.normalize(source, Normalizer.NFD);
            logln("\tNFD(Unicode): " + uncodeNorm);
            logln("\tNFD(icu4j)  : " + icuNorm);
            if (!uncodeNorm.equals(icuNorm)) {
                errln("NFD: Unicode sample output => " + uncodeNorm + "; icu4j output=> " + icuNorm);
            }
            //NFC
            uncodeNorm = unicode_NFC.normalize(source);
            icuNorm = Normalizer.normalize(source, Normalizer.NFC);
            logln("\tNFC(Unicode): " + uncodeNorm);
            logln("\tNFC(icu4j)  : " + icuNorm);
            if (!uncodeNorm.equals(icuNorm)) {
                errln("NFC: Unicode sample output => " + uncodeNorm + "; icu4j output=> " + icuNorm);
            }
            //NFKD
            uncodeNorm = unicode_NFKD.normalize(source);
            icuNorm = Normalizer.normalize(source, Normalizer.NFKD);
            logln("\tNFKD(Unicode): " + uncodeNorm);
            logln("\tNFKD(icu4j)  : " + icuNorm);
            if (!uncodeNorm.equals(icuNorm)) {
                errln("NFKD: Unicode sample output => " + uncodeNorm + "; icu4j output=> " + icuNorm);
            }
            //NFKC
            uncodeNorm = unicode_NFKC.normalize(source);
            icuNorm = Normalizer.normalize(source, Normalizer.NFKC);
            logln("\tNFKC(Unicode): " + uncodeNorm);
            logln("\tNFKC(icu4j)  : " + icuNorm);
            if (!uncodeNorm.equals(icuNorm)) {
                errln("NFKC: Unicode sample output => " + uncodeNorm + "; icu4j output=> " + icuNorm);
            }
            
            i++;
        }
    }
    
    String getTestSource() {
    if (random == null) {
        random = createRandom(); // use test framework's random seed
    }
        String source = "";
        int i = 0;
        while (i < (random.nextInt(maxCharCount) + 1)) {
            int codepoint = random.nextInt(maxCodePoint);
            //Elimate unassigned characters
            while (UCharacter.getType(codepoint) == UCharacterCategory.UNASSIGNED) {
                codepoint = random.nextInt(maxCodePoint);
            }
            source = source + UTF16.valueOf(codepoint);
            i++;
        }
        return source;
    }
}
