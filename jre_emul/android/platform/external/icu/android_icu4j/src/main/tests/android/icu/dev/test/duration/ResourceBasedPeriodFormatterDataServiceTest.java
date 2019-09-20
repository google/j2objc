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

package android.icu.dev.test.duration;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.duration.impl.PeriodFormatterData;
import android.icu.impl.duration.impl.ResourceBasedPeriodFormatterDataService;

public class ResourceBasedPeriodFormatterDataServiceTest extends TestFmwk {
  @Test
  public void testAvailable() {
    ResourceBasedPeriodFormatterDataService service =
        ResourceBasedPeriodFormatterDataService.getInstance();
    Collection locales = service.getAvailableLocales();
    for (Iterator i = locales.iterator(); i.hasNext();) {
      String locale = (String)i.next();
      PeriodFormatterData pfd = service.get(locale);
      assertFalse(locale + ": " + pfd.pluralization(), -1 == pfd.pluralization());
    }
  }
}
