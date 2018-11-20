/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
******************************************************************************
* Copyright (C) 2007, International Business Machines Corporation and   *
* others. All Rights Reserved.                                               *
******************************************************************************
*/

package android.icu.impl.duration;

import java.util.Date;
import java.util.TimeZone;

/**
 * Abstract formatter for dates.  Differs from DateFormat in that it
 * provides <code>withLocale</code> and <code>withTimeZone</code> methods.
 * @hide Only a subset of ICU is exposed in Android
 */
public interface DateFormatter {

  /**
   * Format the date, provided as a java Date object.
   * 
   * @param date the date
   * @return the formatted time
   */
  String format(Date date);

  /**
   * Format the date, provided as milliseconds.
   *
   * @param date the date in milliseconds
   * @return the formatted time
   */
  String format(long date);

  /**
   * Returns a new DateFormatter that uses data for a new locale.
   *
   * @param locale the new locale to use
   * @return a new formatter for the given locale
   */
  DateFormatter withLocale(String localeName);

  /**
   * Returns a new DateFormatter that uses the new time zone.
   *
   * @param tz the new time zone
   * @return a new formatter for the given time zone
   */
  DateFormatter withTimeZone(TimeZone tz);
}
