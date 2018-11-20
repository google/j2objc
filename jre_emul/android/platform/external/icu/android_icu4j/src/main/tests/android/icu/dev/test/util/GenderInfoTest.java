/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2003-2012, Google, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
*/
package android.icu.dev.test.util;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.util.GenderInfo;
import android.icu.util.GenderInfo.Gender;
import android.icu.util.ULocale;

public class GenderInfoTest extends TestFmwk {
    public static GenderInfo NEUTRAL_LOCALE = GenderInfo.getInstance(ULocale.ENGLISH);
    public static GenderInfo MIXED_NEUTRAL_LOCALE = GenderInfo.getInstance(new ULocale("is"));
    public static GenderInfo MALE_TAINTS_LOCALE = GenderInfo.getInstance(ULocale.FRENCH);

    @Test
    public void TestEmpty() {
        // Gender of the empty list is always OTHER regardless of gender style.
        check(Gender.OTHER, Gender.OTHER, Gender.OTHER);
    }

    @Test
    public void TestOne() {
        // Gender of single item list is always gender of sole item regardless of
        // gender style.
        for (Gender g : Gender.values()) {
            check(g, g, g, g);
        }
    }

    @Test
    public void TestOther() {
        check(Gender.OTHER, Gender.MALE, Gender.MALE, Gender.MALE, Gender.MALE);
        check(Gender.OTHER, Gender.OTHER, Gender.MALE, Gender.MALE, Gender.FEMALE);
        check(Gender.OTHER, Gender.OTHER, Gender.MALE, Gender.MALE, Gender.OTHER);
    
        check(Gender.OTHER, Gender.OTHER, Gender.MALE, Gender.FEMALE, Gender.MALE);
        check(Gender.OTHER, Gender.FEMALE, Gender.FEMALE, Gender.FEMALE, Gender.FEMALE);
        check(Gender.OTHER, Gender.OTHER, Gender.MALE, Gender.FEMALE, Gender.OTHER);

        check(Gender.OTHER, Gender.OTHER, Gender.MALE, Gender.OTHER, Gender.MALE);
        check(Gender.OTHER, Gender.OTHER, Gender.MALE, Gender.OTHER, Gender.FEMALE);
        check(Gender.OTHER, Gender.OTHER, Gender.MALE, Gender.OTHER, Gender.OTHER);
    }

    public void check(Gender neutral, Gender mixed, Gender taints, Gender... genders) {
        List<Gender> mixed0 = Arrays.asList(genders);
        assertEquals("neutral " + mixed0, neutral, NEUTRAL_LOCALE.getListGender(mixed0));
        assertEquals("mixed neutral " + mixed0, mixed, MIXED_NEUTRAL_LOCALE.getListGender(mixed0));
        assertEquals("male taints " + mixed0, taints, MALE_TAINTS_LOCALE.getListGender(mixed0));
    }
    
    @Test
    public void TestFallback() {
        assertEquals("Strange locale = root", GenderInfo.getInstance(ULocale.ROOT), GenderInfo.getInstance(new ULocale("xxx")));
        assertEquals("Strange locale = root", GenderInfo.getInstance(ULocale.FRANCE), GenderInfo.getInstance(ULocale.CANADA_FRENCH));
    }
}
