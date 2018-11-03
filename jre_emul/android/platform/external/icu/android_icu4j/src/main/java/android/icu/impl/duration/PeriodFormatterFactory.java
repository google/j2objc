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
 * Abstract factory interface used to create PeriodFormatters.
 * PeriodFormatters are immutable once created.
 * <p>
 * Setters on the factory mutate the factory and return it,
 * for chaining.
 * @hide Only a subset of ICU is exposed in Android
 */
public interface PeriodFormatterFactory {

  /**
   * Set the name of the locale that will be used when 
   * creating new formatters.
   *
   * @param localeName the name of the Locale
   * @return this PeriodFormatterFactory
   */
  public PeriodFormatterFactory setLocale(String localeName);

  /**
   * Set whether limits will be displayed.
   *
   * @param display true if limits will be displayed
   * @return this PeriodFormatterFactory
   */
  public PeriodFormatterFactory setDisplayLimit(boolean display);

  /**
   * Set whether past and future will be displayed.
   *
   * @param display true if past and future will be displayed
   * @return this PeriodFormatterFactory
   */
  public PeriodFormatterFactory setDisplayPastFuture(boolean display);

  /**
   * Set how separators will be displayed.
   *
   * @param variant the variant indicating how separators will be displayed
   * @return this PeriodFormatterFactory
   */
  public PeriodFormatterFactory setSeparatorVariant(int variant);

  /**
   * Set the variant of the time unit names to use.
   *
   * @param variant the variant to use
   * @return this PeriodFormatterFactory
   */
  public PeriodFormatterFactory setUnitVariant(int variant);

  /**
   * Set the variant of the count to use.
   *
   * @param variant the variant to use
   * @return this PeriodFormatterFactory
   */
  public PeriodFormatterFactory setCountVariant(int variant);

  /**
   * Return a formatter based on this factory's current settings.
   *
   * @return a PeriodFormatter
   */
  public PeriodFormatter getFormatter();
}
