/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 ******************************************************************************************
 * Copyright (C) 2009-2010, Google, Inc.; International Business Machines Corporation and *
 * others. All Rights Reserved.                                                           *
 ******************************************************************************************
 */

package android.icu.dev.test.util;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.util.LocalePriorityList;
import android.icu.util.ULocale;

/**
 * Test the LanguagePriorityList
 * @author markdavis@google.com
 */
public class LocalePriorityListTest extends TestFmwk {
  @Test
  public void testLanguagePriorityList() {
    final String expected = "af, en, fr";

    LocalePriorityList list = LocalePriorityList.add("af, en, fr;q=0.9").build();
    assertEquals(expected, list.toString());

    // check looseness, and that later values win
    LocalePriorityList list2 = LocalePriorityList.add(
        ", fr ; q = 0.9 ,   en;q=0.1 , af, en, de;q=0, ").build();
    assertEquals(expected, list2.toString());
    assertEquals(list, list2);

    LocalePriorityList list3 = LocalePriorityList
        .add(new ULocale("af"))
        .add(ULocale.FRENCH, 0.9d)
        .add(ULocale.ENGLISH)
        .build();
    assertEquals(expected, list3.toString());
    assertEquals(list, list3);
    
    LocalePriorityList list4 = LocalePriorityList
    .add(list).build();
    assertEquals(expected, list4.toString());
    assertEquals(list, list4);
    
    LocalePriorityList list5 = LocalePriorityList.add("af, fr;q=0.9, en").build(true);
    assertEquals("af, en, fr;q=0.9", list5.toString());
  }

private void assertEquals(Object expected, Object string) {
    assertEquals("", expected, string);
}
}
