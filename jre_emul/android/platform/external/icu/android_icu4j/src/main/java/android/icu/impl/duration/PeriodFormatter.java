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

/**
 * Formats a Period, such as '2 hours 23 minutes'.  
 * The Period defines the fields to format and their
 * values, and the formatter defines how to format them.
 * <p>
 * PeriodFormatters are immutable.
 * <p> 
 * PeriodFormatter can be instantiated using a PeriodFormatterFactory.
 *
 * @see Period
 * @see PeriodBuilder
 * @see PeriodFormatterFactory
 * @hide Only a subset of ICU is exposed in Android
 */
public interface PeriodFormatter {
  /**
   * Format a Period.
   *
   * @param ts the Period to format
   * @return the formatted time
   */
  String format(Period period);

  /**
   * Return a new PeriodFormatter with the same customizations but
   * using data for a new locale.  Some locales impose limits on the
   * fields that can be directly formatter.
   *
   * @param localeName the name of the new locale
   * @return a new formatter for the given locale
   */
  PeriodFormatter withLocale(String localeName);
}
