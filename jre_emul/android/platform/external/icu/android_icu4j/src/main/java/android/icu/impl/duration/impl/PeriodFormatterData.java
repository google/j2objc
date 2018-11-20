/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
******************************************************************************
* Copyright (C) 2009-2011, International Business Machines Corporation and   *
* others. All Rights Reserved.                                               *
******************************************************************************
*/

package android.icu.impl.duration.impl;

import java.util.Arrays;

import android.icu.impl.duration.TimeUnit;
import android.icu.impl.duration.impl.DataRecord.ECountVariant;
import android.icu.impl.duration.impl.DataRecord.EDecimalHandling;
import android.icu.impl.duration.impl.DataRecord.EFractionHandling;
import android.icu.impl.duration.impl.DataRecord.EGender;
import android.icu.impl.duration.impl.DataRecord.EHalfPlacement;
import android.icu.impl.duration.impl.DataRecord.EHalfSupport;
import android.icu.impl.duration.impl.DataRecord.ENumberSystem;
import android.icu.impl.duration.impl.DataRecord.EPluralization;
import android.icu.impl.duration.impl.DataRecord.EUnitVariant;
import android.icu.impl.duration.impl.DataRecord.EZeroHandling;
import android.icu.impl.duration.impl.DataRecord.ScopeData;


/**
 * PeriodFormatterData provides locale-specific data used to format
 * relative dates and times, and convenience api to access it.
 *
 * An instance of PeriodFormatterData is usually created by requesting
 * data for a given locale from an PeriodFormatterDataService.
 * @hide Only a subset of ICU is exposed in Android
 */
public class PeriodFormatterData {
  final DataRecord dr;
  String localeName;

  // debug
  public static boolean trace = false;

  public PeriodFormatterData(String localeName, DataRecord dr) {
    this.dr = dr;
    this.localeName = localeName;
    if(localeName == null) {
        throw new NullPointerException("localename is null");
    }
//    System.err.println("** localeName is " + localeName);
    if (dr == null) {
//      Thread.dumpStack();
      throw new NullPointerException("data record is null");
    }
  }

  // none - chinese (all forms the same)
  // plural - english, special form for 1
  // dual - special form for 1 and 2
  // paucal - russian, special form for 1, for 2-4 and n > 20 && n % 10 == 2-4
  // rpt_dual_few - slovenian, special form for 1, 2, 3-4 and n as above
  // hebrew, dual plus singular form for years > 11
  // arabic, dual, plus singular form for all terms > 10

  /**
   * Return the pluralization format used by this locale.
   * @return the pluralization format
   */
  public int pluralization() {
    return dr.pl;
  }

  /**
   * Return true if zeros are allowed in the display.
   * @return true if zeros should be allowed
   */
  public boolean allowZero() {
    return dr.allowZero;
  }

  public boolean weeksAloneOnly() {
    return dr.weeksAloneOnly;
  }

  public int useMilliseconds() {
    return dr.useMilliseconds;
  }

  /**
   * Append the appropriate prefix to the string builder, depending on whether and
   * how a limit and direction are to be displayed.
   *
   * @param tl how and whether to display the time limit
   * @param td how and whether to display the time direction
   * @param sb the string builder to which to append the text
   * @return true if a following digit will require a digit prefix
   */
  public boolean appendPrefix(int tl, int td, StringBuffer sb) {
    if (dr.scopeData != null) {
      int ix = tl * 3 + td;
      ScopeData sd = dr.scopeData[ix];
      if (sd != null) {
        String prefix = sd.prefix;
        if (prefix != null) {
          sb.append(prefix);
          return sd.requiresDigitPrefix;
        }
      }
    }
    return false;
  }

  /**
   * Append the appropriate suffix to the string builder, depending on whether and
   * how a limit and direction are to be displayed.
   *
   * @param tl how and whether to display the time limit
   * @param td how and whether to display the time direction
   * @param sb the string builder to which to append the text
   */
  public void appendSuffix(int tl, int td, StringBuffer sb) {
    if (dr.scopeData != null) {
      int ix = tl * 3 + td;
      ScopeData sd = dr.scopeData[ix];
      if (sd != null) {
        String suffix = sd.suffix;
        if (suffix != null) {
          if (trace) {
            System.out.println("appendSuffix '" + suffix + "'");
          }
          sb.append(suffix);
        }
      }
    }
  }

  /**
   * Append the count and unit to the string builder.
   *
   * @param unit the unit to append
   * @param count the count of units, * 1000
   * @param cv the format to use for displaying the count
   * @param uv the format to use for displaying the unit
   * @param useCountSep if false, force no separator between count and unit
   * @param useDigitPrefix if true, use the digit prefix
   * @param multiple true if there are multiple units in this string
   * @param last true if this is the last unit
   * @param wasSkipped true if the unit(s) before this were skipped
   * @param sb the string builder to which to append the text
   * @return true if will require skip marker
   */
  @SuppressWarnings("fallthrough")
  public boolean appendUnit(TimeUnit unit, int count, int cv, 
                            int uv, boolean useCountSep, 
                            boolean useDigitPrefix, boolean multiple, 
                            boolean last, boolean wasSkipped, 
                            StringBuffer sb) {
    int px = unit.ordinal();

    boolean willRequireSkipMarker = false;
    if (dr.requiresSkipMarker != null && dr.requiresSkipMarker[px] && 
        dr.skippedUnitMarker != null) {
      if (!wasSkipped && last) {
        sb.append(dr.skippedUnitMarker);
      }
      willRequireSkipMarker = true;
    }

    if (uv != EUnitVariant.PLURALIZED) {
      boolean useMedium = uv == EUnitVariant.MEDIUM; 
      String[] names = useMedium ? dr.mediumNames : dr.shortNames;
      if (names == null || names[px] == null) {
        names = useMedium ? dr.shortNames : dr.mediumNames;
      }
      if (names != null && names[px] != null) {
        appendCount(unit, false, false, count, cv, useCountSep, 
                    names[px], last, sb); // omit suffix, ok?
        return false; // omit skip marker
      }
    }

    // check cv
    if (cv == ECountVariant.HALF_FRACTION && dr.halfSupport != null) {
      switch (dr.halfSupport[px]) {
        case EHalfSupport.YES: break;
        case EHalfSupport.ONE_PLUS:
          if (count > 1000) {
            break;
          }
          // else fall through to decimal
        case EHalfSupport.NO: {
          count = (count / 500) * 500;  // round to 1/2
          cv = ECountVariant.DECIMAL1; 
        } break;
      }
    }
          
    String name = null;
    int form = computeForm(unit, count, cv, multiple && last);
    if (form == FORM_SINGULAR_SPELLED) {
      if (dr.singularNames == null) {
        form = FORM_SINGULAR;
        name = dr.pluralNames[px][form];
      } else {
        name = dr.singularNames[px];
      }
    } else if (form == FORM_SINGULAR_NO_OMIT) {
      name = dr.pluralNames[px][FORM_SINGULAR];
    } else if (form == FORM_HALF_SPELLED) {
      name = dr.halfNames[px];
    } else { 
      try {
        name = dr.pluralNames[px][form];
      } catch (NullPointerException e) {
        System.out.println("Null Pointer in PeriodFormatterData["+localeName+"].au px: " + px + " form: " + form + " pn: " + Arrays.toString(dr.pluralNames));
        throw e;
      }
    }
    if (name == null) {
      form = FORM_PLURAL;
      name = dr.pluralNames[px][form];
    }

    boolean omitCount =
      (form == FORM_SINGULAR_SPELLED || form == FORM_HALF_SPELLED) ||
      (dr.omitSingularCount && form == FORM_SINGULAR) ||
      (dr.omitDualCount && form == FORM_DUAL);

    int suffixIndex = appendCount(unit, omitCount, useDigitPrefix, count, cv, 
                                  useCountSep, name, last, sb);
    if (last && suffixIndex >= 0) {
      String suffix = null;
      if (dr.rqdSuffixes != null && suffixIndex < dr.rqdSuffixes.length) {
        suffix = dr.rqdSuffixes[suffixIndex];
      }
      if (suffix == null && dr.optSuffixes != null && 
          suffixIndex < dr.optSuffixes.length) {
        suffix = dr.optSuffixes[suffixIndex];
      }
      if (suffix != null) {
        sb.append(suffix);
      }
    }
    return willRequireSkipMarker;
  }

  /**
   * Append a count to the string builder.
   *
   * @param unit the unit
   * @param count the count
   * @param cv the format to use for displaying the count
   * @param useSep whether to use the count separator, if available
   * @param name the term name
   * @param last true if this is the last unit to be formatted
   * @param sb the string builder to which to append the text
   * @return index to use if might have required or optional suffix, or -1 if none required
   */
  public int appendCount(TimeUnit unit, boolean omitCount, 
                         boolean useDigitPrefix, 
                         int count, int cv, boolean useSep, 
                         String name, boolean last, StringBuffer sb) {
    if (cv == ECountVariant.HALF_FRACTION && dr.halves == null) {
      cv = ECountVariant.INTEGER;
    }

    if (!omitCount && useDigitPrefix && dr.digitPrefix != null) {
      sb.append(dr.digitPrefix);
    }

    int index = unit.ordinal();
    switch (cv) {
      case ECountVariant.INTEGER: {
        if (!omitCount) {
          appendInteger(count/1000, 1, 10, sb);
        }
      } break;

      case ECountVariant.INTEGER_CUSTOM: {
        int val = count / 1000;
        // only custom names we have for now
        if (unit == TimeUnit.MINUTE && 
            (dr.fiveMinutes != null || dr.fifteenMinutes != null)) {
          if (val != 0 && val % 5 == 0) {
            if (dr.fifteenMinutes != null && (val == 15 || val == 45)) {
              val = val == 15 ? 1 : 3;
              if (!omitCount) appendInteger(val, 1, 10, sb);
              name = dr.fifteenMinutes;
              index = 8; // hack
              break;
            }
            if (dr.fiveMinutes != null) {
              val = val / 5;
              if (!omitCount) appendInteger(val, 1, 10, sb);
              name = dr.fiveMinutes;
              index = 9; // hack
              break;
            }
          }
        }
        if (!omitCount) appendInteger(val, 1, 10, sb);
      } break;

      case ECountVariant.HALF_FRACTION: {
        // 0, 1/2, 1, 1-1/2...
        int v = count / 500;
        if (v != 1) {
          if (!omitCount) appendCountValue(count, 1, 0, sb);
        }
        if ((v & 0x1) == 1) {
          // hack, using half name
          if (v == 1 && dr.halfNames != null && dr.halfNames[index] != null) {
            sb.append(name);
            return last ? index : -1;
          }

          int solox = v == 1 ? 0 : 1;
          if (dr.genders != null && dr.halves.length > 2) {
            if (dr.genders[index] == EGender.F) {
              solox += 2;
            }
          }
          int hp = dr.halfPlacements == null 
              ? EHalfPlacement.PREFIX
              : dr.halfPlacements[solox & 0x1];
          String half = dr.halves[solox];
          String measure = dr.measures == null ? null : dr.measures[index];
          switch (hp) {
            case EHalfPlacement.PREFIX:
              sb.append(half);
              break;
            case EHalfPlacement.AFTER_FIRST: {
              if (measure != null) {
                sb.append(measure);
                sb.append(half);
                if (useSep && !omitCount) {
                  sb.append(dr.countSep);
                } 
                sb.append(name);
              } else { // ignore sep completely
                sb.append(name);
                sb.append(half);
                return last ? index : -1; // might use suffix
              }
            } return -1; // exit early
            case EHalfPlacement.LAST: {
              if (measure != null) {
                sb.append(measure);
              }
              if (useSep && !omitCount) {
                sb.append(dr.countSep);
              }
              sb.append(name);
              sb.append(half);
            } return last ? index : -1; // might use suffix
          }
        }
      } break;
      default: {
        int decimals = 1;
        switch (cv) {
          case ECountVariant.DECIMAL2: decimals = 2; break;
          case ECountVariant.DECIMAL3: decimals = 3; break;
          default: break;
        }
        if (!omitCount) appendCountValue(count, 1, decimals, sb);
      } break;
    }
    if (!omitCount && useSep) {
      sb.append(dr.countSep);
    }
    if (!omitCount && dr.measures != null && index < dr.measures.length) {
      String measure = dr.measures[index];
      if (measure != null) {
        sb.append(measure);
      }
    }
    sb.append(name);
    return last ? index : -1;
  }

  /**
   * Append a count value to the builder.
   *
   * @param count the count
   * @param integralDigits the number of integer digits to display
   * @param decimalDigits the number of decimal digits to display, <= 3
   * @param sb the string builder to which to append the text
   */
  public void appendCountValue(int count, int integralDigits, 
                               int decimalDigits, StringBuffer sb) {
    int ival = count / 1000;
    if (decimalDigits == 0) {
      appendInteger(ival, integralDigits, 10, sb);
      return;
    }

    if (dr.requiresDigitSeparator && sb.length() > 0) {
      sb.append(' ');
    }
    appendDigits(ival, integralDigits, 10, sb);
    int dval = count % 1000;
    if (decimalDigits == 1) {
      dval /= 100;
    } else if (decimalDigits == 2) {
      dval /= 10;
    }
    sb.append(dr.decimalSep);
    appendDigits(dval, decimalDigits, decimalDigits, sb);
    if (dr.requiresDigitSeparator) {
      sb.append(' ');
    }
  }

  public void appendInteger(int num, int mindigits, int maxdigits, 
                            StringBuffer sb) {
    if (dr.numberNames != null && num < dr.numberNames.length) {
      String name = dr.numberNames[num];
      if (name != null) {
        sb.append(name);
        return;
      }
    }

    if (dr.requiresDigitSeparator && sb.length() > 0) {
      sb.append(' ');
    }
    switch (dr.numberSystem) {
      case ENumberSystem.DEFAULT: appendDigits(num, mindigits, maxdigits, sb); break;
      case ENumberSystem.CHINESE_TRADITIONAL: sb.append(
          Utils.chineseNumber(num, Utils.ChineseDigits.TRADITIONAL)); break;
      case ENumberSystem.CHINESE_SIMPLIFIED: sb.append(
          Utils.chineseNumber(num, Utils.ChineseDigits.SIMPLIFIED)); break;
      case ENumberSystem.KOREAN: sb.append(
          Utils.chineseNumber(num, Utils.ChineseDigits.KOREAN)); break;
    }
    if (dr.requiresDigitSeparator) {
      sb.append(' ');
    }
  }

  /**
   * Append digits to the string builder, using this.zero for '0' etc.
   *
   * @param num the integer to append
   * @param mindigits the minimum number of digits to append
   * @param maxdigits the maximum number of digits to append
   * @param sb the string builder to which to append the text
   */
  public void appendDigits(long num, int mindigits, int maxdigits,  
                           StringBuffer sb) {
    char[] buf = new char[maxdigits];
    int ix = maxdigits;
    while (ix > 0 && num > 0) {
      buf[--ix] = (char)(dr.zero + (num % 10));
      num /= 10;
    }
    for (int e = maxdigits - mindigits; ix > e;) {
      buf[--ix] = dr.zero;
    }
    sb.append(buf, ix, maxdigits - ix);
  }

  /**
   * Append a marker for skipped units internal to a string.
   * @param sb the string builder to which to append the text
   */
  public void appendSkippedUnit(StringBuffer sb) {
    if (dr.skippedUnitMarker != null) {
      sb.append(dr.skippedUnitMarker);
    }
  }

  /**
   * Append the appropriate separator between units
   *
   * @param unit the unit to which to append the separator
   * @param afterFirst true if this is the first unit formatted
   * @param beforeLast true if this is the next-to-last unit to be formatted
   * @param sb the string builder to which to append the text
   * @return true if a prefix will be required before a following unit
   */
  public boolean appendUnitSeparator(TimeUnit unit, boolean longSep, 
                                     boolean afterFirst, boolean beforeLast, 
                                     StringBuffer sb) {
    // long seps
    // false, false "...b', '...d"
    // false, true  "...', and 'c"
    // true, false - "a', '...c"
    // true, true - "a' and 'b"
    if ((longSep && dr.unitSep != null) || dr.shortUnitSep != null) {
      if (longSep && dr.unitSep != null) {
        int ix = (afterFirst ? 2 : 0) + (beforeLast ? 1 : 0);
        sb.append(dr.unitSep[ix]);
        return dr.unitSepRequiresDP != null && dr.unitSepRequiresDP[ix];
      }
      sb.append(dr.shortUnitSep); // todo: investigate whether DP is required
    }
    return false;
  }

  private static final int 
    FORM_PLURAL = 0,
    FORM_SINGULAR = 1,
    FORM_DUAL = 2,
    FORM_PAUCAL = 3,
    FORM_SINGULAR_SPELLED = 4, // following are not in the pluralization list
    FORM_SINGULAR_NO_OMIT = 5, // a hack
    FORM_HALF_SPELLED = 6;

  private int computeForm(TimeUnit unit, int count, int cv, 
                          boolean lastOfMultiple) {
    // first check if a particular form is forced by the countvariant.  if
    // SO, just return that.  otherwise convert the count to an integer
    // and use pluralization rules to determine which form to use.
    // careful, can't assume any forms but plural exist.

    if (trace) {
      System.err.println("pfd.cf unit: " + unit + " count: " + count + " cv: " + cv + " dr.pl: " + dr.pl);
      Thread.dumpStack();
    }
    if (dr.pl == EPluralization.NONE) {
      return FORM_PLURAL;
    }
    // otherwise, assume we have at least a singular and plural form

    int val = count/1000;

    switch (cv) {
      case ECountVariant.INTEGER: 
      case ECountVariant.INTEGER_CUSTOM: {
        // do more analysis based on floor of count
      } break;
      case ECountVariant.HALF_FRACTION: {
        switch (dr.fractionHandling) {
          case EFractionHandling.FPLURAL:
            return FORM_PLURAL;

          case EFractionHandling.FSINGULAR_PLURAL_ANDAHALF:
          case EFractionHandling.FSINGULAR_PLURAL: {
            // if half-floor is 1/2, use singular
            // else if half-floor is not integral, use plural
            // else do more analysis
            int v = count / 500;
            if (v == 1) {
              if (dr.halfNames != null && dr.halfNames[unit.ordinal()] != null) {
                return FORM_HALF_SPELLED;
              }
              return FORM_SINGULAR_NO_OMIT;
            }
            if ((v & 0x1) == 1) {
              if (dr.pl == EPluralization.ARABIC && v > 21) { // hack
                return FORM_SINGULAR_NO_OMIT;
              }
              if (v == 3 && dr.pl == EPluralization.PLURAL &&
                  dr.fractionHandling != EFractionHandling.FSINGULAR_PLURAL_ANDAHALF) {
                return FORM_PLURAL;
              }
            }
            
            // it will display like an integer, so do more analysis
          } break;

          case EFractionHandling.FPAUCAL: {
            int v = count / 500;
            if (v == 1 || v == 3) {
              return FORM_PAUCAL;
            }
            // else use integral form
          } break;

          default:
            throw new IllegalStateException();
        }
      } break;
      default: { // for all decimals
        switch (dr.decimalHandling) {
          case EDecimalHandling.DPLURAL: break;
          case EDecimalHandling.DSINGULAR: return FORM_SINGULAR_NO_OMIT;
          case EDecimalHandling.DSINGULAR_SUBONE:
            if (count < 1000) {
              return FORM_SINGULAR_NO_OMIT;
            }
            break;
          case EDecimalHandling.DPAUCAL:
            if (dr.pl == EPluralization.PAUCAL) {
              return FORM_PAUCAL;
            }
            break;
          default:
            break;
        }
        return FORM_PLURAL;
      }
    }

    // select among pluralization forms
    if (trace && count == 0) {
      System.err.println("EZeroHandling = " + dr.zeroHandling);
    }
    if (count == 0 && dr.zeroHandling == EZeroHandling.ZSINGULAR) {
      return FORM_SINGULAR_SPELLED;
    }

    int form = FORM_PLURAL;
    switch(dr.pl) {
      case EPluralization.NONE: break; // never get here
      case EPluralization.PLURAL: {
        if (val == 1) { 
          form = FORM_SINGULAR_SPELLED; // defaults to form_singular if no spelled forms
        } 
      } break;
      case EPluralization.DUAL: {
        if (val == 2) {
          form = FORM_DUAL; 
        } else if (val == 1) {
          form = FORM_SINGULAR; 
        } 
      } break;
      case EPluralization.PAUCAL: {
        int v = val;
        v = v % 100;
        if (v > 20) {
          v = v % 10;
        }
        if (v == 1) {
          form = FORM_SINGULAR;
        } else if (v > 1 && v < 5) {
          form = FORM_PAUCAL;
        }
      } break;
        /*
      case EPluralization.RPT_DUAL_FEW: {
        int v = val;
        if (v > 20) {
          v = v % 10;
        }
        if (v == 1) {
          form = FORM_SINGULAR;
        } else if (v == 2) {
          form = FORM_DUAL;
        } else if (v > 2 && v < 5) {
          form = FORM_PAUCAL;
        }
      } break;
        */
      case EPluralization.HEBREW: {
        if (val == 2) {
          form = FORM_DUAL;
        } else if (val == 1) {
          if (lastOfMultiple) {
            form = FORM_SINGULAR_SPELLED;
          } else {
            form = FORM_SINGULAR;
          } 
        } else if (unit == TimeUnit.YEAR && val > 11) {
          form = FORM_SINGULAR_NO_OMIT;
        }
      } break;
      case EPluralization.ARABIC: {
        if (val == 2) {
          form = FORM_DUAL;
        } else if (val == 1) {
          form = FORM_SINGULAR;
        } else if (val > 10) {
          form = FORM_SINGULAR_NO_OMIT;
        }
      } break;
      default: 
        System.err.println("dr.pl is " + dr.pl);
        throw new IllegalStateException();
    }

    return form;
  }
}
