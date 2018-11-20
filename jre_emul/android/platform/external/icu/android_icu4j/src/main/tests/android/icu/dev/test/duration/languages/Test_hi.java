/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
******************************************************************************
* Copyright (C) 2007-2010, International Business Machines Corporation and   *
* others. All Rights Reserved.                                               *
******************************************************************************
*/

// Copyright 2006 Google Inc.  All Rights Reserved.

package android.icu.dev.test.duration.languages;

import org.junit.Test;

import android.icu.dev.test.duration.LanguageTestFmwk;
import android.icu.impl.duration.TimeUnitConstants;

/**
 * Test cases for hi
 */
public class Test_hi extends LanguageTestFmwk implements TimeUnitConstants {
  public Test_hi() {
    super("hi", false);
  }

  @Test
  public void testMonthNames() {
    // test that month uses the plural form with singular digit
    // in these cases:
    // "1 months from now"
    // "1 months, and 2 weeks from now"
    // "more than 1 months",
    // "more than 1 months ago"
/*
    Period[] times = {
      Period.at(1, MONTH).inFuture(),
      Period.at(1, MONTH).and(2, WEEK).inFuture(),
      Period.moreThan(1, MONTH),
      Period.moreThan(1, MONTH).inFuture(),
    };

    String[] targets = {
      "\u0905\u092d\u0940 \u0938\u0947 \u0967 \u092e\u0939\u0940\u0928\u0947 \u092c\u093e\u0926",
      "\u0905\u092d\u0940 \u0938\u0947 \u0967 \u092e\u0939\u0940\u0928\u0947 \u0914\u0930 \u0968 \u0938\u092a\u094d\u0924\u093e\u0939 \u092c\u093e\u0926",
      "\u0967 \u092e\u0939\u0940\u0928\u0947 \u0938\u0947 \u0915\u092e",
      "\u0967 \u092e\u0939\u0940\u0928\u0947 \u0938\u0947 \u0915\u092e \u092a\u0939\u0932\u0947",
    };

    PeriodFormatterFactory pff = BasicPeriodFormatterFactory.getDefault()
        .setLocale("hi");
    PeriodFormatter pf = pff.getFormatter();
    for (int i = 0; i < targets.length; ++i) {
        xAssertEquals(timestring(times[i]), targets, i, pf.format(times[i]));
    }
*/
  }
}
