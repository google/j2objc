/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
******************************************************************************
* Copyright (C) 2007-2010, International Business Machines Corporation and   *
* others. All Rights Reserved.                                               *
******************************************************************************
*/

package android.icu.impl.duration;

import java.util.Locale;

import android.icu.impl.duration.impl.DataRecord.ECountVariant;
import android.icu.impl.duration.impl.DataRecord.ESeparatorVariant;
import android.icu.impl.duration.impl.DataRecord.EUnitVariant;
import android.icu.impl.duration.impl.PeriodFormatterData;
import android.icu.impl.duration.impl.PeriodFormatterDataService;

/**
 * An implementation of PeriodFormatterFactory that provides customization of
 * formatting behavior. Instances of this factory are created by
 * BasicPeriodFormatterService.
 *
 * The settings on BasicPeriodFormatterFactory are:
 * <ul>
 *
 * <li><b>setDisplayLimit</b> controls whether phrases like 'more than'
 * or 'less than' will be displayed when the Period has a defined
 * limit.  Default is to display them.</li>
 *
 * <li><b>setDisplayPastFuture</b> controls whether phrases like 'ago'
 * or 'from now' will be displayed to indicate past or future
 * time. Default is to display them.</li>
 *
 * <li><b>setSeparatorVariant</b> controls how separators (between
 * count and period, and multiple periods) will be displayed, when
 * appropriate for the language. Default is to use full
 * separators.</li>
 *
 * <li><b>setUnitVariant</b> controls which of various types of
 * unit names to use.  PLURALIZED indicates that full names will be
 * used.  MEDIUM indicates that medium-length (usually 2-3 character)
 * names will be used.  SHORT indicates that short (usually single
 * character) names will be used.  If there is no localization data
 * available for either the SHORT or MEDIUM names, the other will be
 * used, if neither is available, the PLURALIZED names will be used.
 * Default is PLURALIZED.</li>
 *
 * <li><b>setCountVariant</b> controls how the count for the smallest
 * unit will be formatted: either as an integer, a fraction to the
 * smallest half, or as a decimal with 1, 2, or 3 decimal points.</li>
 * Counts for higher units will be formatted as integers.
 *
 * </ul>
 * @hide Only a subset of ICU is exposed in Android
 */
public class BasicPeriodFormatterFactory implements PeriodFormatterFactory {
  private final PeriodFormatterDataService ds;
  private PeriodFormatterData data;
  private Customizations customizations;
  private boolean customizationsInUse;
  private String localeName;

  // package-only constructor
  BasicPeriodFormatterFactory(PeriodFormatterDataService ds) {
    this.ds = ds;
    this.customizations = new Customizations();
    this.localeName = Locale.getDefault().toString();
  }

  /**
   * Return the default rdf factory as a BasicPeriodFormatterFactory.
   *
   * @return a default BasicPeriodFormatterFactory
   */
  public static BasicPeriodFormatterFactory getDefault() {
      return (BasicPeriodFormatterFactory)
        BasicPeriodFormatterService.getInstance().newPeriodFormatterFactory();
  }

  /**
   * Set the locale for this factory.
   */
  @Override
  public PeriodFormatterFactory setLocale(String localeName) {
    data = null;
    this.localeName = localeName;
    return this;
  }

  /**
   * Set whether limits will be displayed.
   *
   * @param display true if limits will be displayed
   * @return this PeriodFormatterFactory
   */
  @Override
  public PeriodFormatterFactory setDisplayLimit(boolean display) {
    updateCustomizations().displayLimit = display;
    return this;
  }

  /**
   * Return true if limits will be displayed.
   *
   * @return true if limits will be displayed
   */
  public boolean getDisplayLimit() {
    return customizations.displayLimit;
  }

  /**
   * Set whether past and future will be displayed.
   *
   * @param display true if past and future will be displayed
   * @return this PeriodFormatterFactory
   */
  @Override
  public PeriodFormatterFactory setDisplayPastFuture(boolean display) {
    updateCustomizations().displayDirection = display;
    return this;
  }

  /**
   * Return true if past and future will be displayed.
   *
   * @return true if past and future will be displayed
   */
  public boolean getDisplayPastFuture() {
    return customizations.displayDirection;
  }

  /**
   * Set how separators will be displayed.
   *
   * @param variant the variant indicating separators will be displayed
   * @return this PeriodFormatterFactory
   */
  @Override
  public PeriodFormatterFactory setSeparatorVariant(int variant) {
    updateCustomizations().separatorVariant = (byte) variant;
    return this;
  }

  /**
   * Return the variant indicating how separators will be displayed.
   *
   * @return the variant
   */
  public int getSeparatorVariant() {
    return customizations.separatorVariant;
  }

  /**
   * Set the variant of the time unit names to use.
   *
   * @param variant the variant to use
   * @return this PeriodFormatterFactory
   */
  @Override
  public PeriodFormatterFactory setUnitVariant(int variant) {
    updateCustomizations().unitVariant = (byte) variant;
    return this;
  }

  /**
   * Return the unit variant.
   *
   * @return the unit variant
   */
  public int getUnitVariant() {
    return customizations.unitVariant;
  }

  /**
   * Set the variant of the count to use.
   *
   * @param variant the variant to use
   * @return this PeriodFormatterFactory
   */
  @Override
  public PeriodFormatterFactory setCountVariant(int variant) {
    updateCustomizations().countVariant = (byte) variant;
    return this;
  }

  /**
   * Return the count variant.
   *
   * @return the count variant
   */
  public int getCountVariant() {
    return customizations.countVariant;
  }

  @Override
  public PeriodFormatter getFormatter() {
    customizationsInUse = true;
    return new BasicPeriodFormatter(this, localeName, getData(),
                                    customizations);
  }

  private Customizations updateCustomizations() {
    if (customizationsInUse) {
      customizations = customizations.copy();
      customizationsInUse = false;
    }
    return customizations;
  }

  // package access only
  PeriodFormatterData getData() {
    if (data == null) {
      data = ds.get(localeName);
    }
    return data;
  }

  // package access for use by BasicPeriodFormatter
  PeriodFormatterData getData(String locName) {
    return ds.get(locName);
  }

  // package access for use by BasicPeriodFormatter
  static class Customizations {
    boolean displayLimit = true;
    boolean displayDirection = true;
    byte separatorVariant = ESeparatorVariant.FULL;
    byte unitVariant = EUnitVariant.PLURALIZED;
    byte countVariant = ECountVariant.INTEGER;

    public Customizations copy() {
        Customizations result = new Customizations();
        result.displayLimit = displayLimit;
        result.displayDirection = displayDirection;
        result.separatorVariant = separatorVariant;
        result.unitVariant = unitVariant;
        result.countVariant = countVariant;
        return result;
    }
  }
}
