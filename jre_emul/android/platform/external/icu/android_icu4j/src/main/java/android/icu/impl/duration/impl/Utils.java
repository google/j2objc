/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
******************************************************************************
* Copyright (C) 2007-2012, International Business Machines Corporation and   *
* others. All Rights Reserved.                                               *
******************************************************************************
*/

package android.icu.impl.duration.impl;

import java.util.Locale;

/**
 * @hide Only a subset of ICU is exposed in Android
 */
public class Utils {
  public static final Locale localeFromString(String s) {
    String language = s;
    String region = "";
    String variant = "";

    int x = language.indexOf("_");
    if (x != -1) {
      region = language.substring(x+1);
      language = language.substring(0, x);
    }
    x = region.indexOf("_");
    if (x != -1) {
      variant = region.substring(x+1);
      region = region.substring(0, x);
    }
    return new Locale(language, region, variant);
  }
    /*
  public static <T> T[] arraycopy(T[] src) {
    T[] result = (T[])Array.newInstance(src.getClass().getComponentType(), src.length); // can we do this without casting?
    for (int i = 0; i < src.length; ++i) {
      result[i] = src[i];
    }
    return result;
  }
    */

  /**
   * Interesting features of chinese numbers:
   * - Each digit is followed by a unit symbol (10's, 100's, 1000's).
   * - Units repeat in levels of 10,000, there are symbols for each level too (except 1's).
   * - The digit 2 has a special form before the 10 symbol and at the end of the number.
   * - If the first digit in the number is 1 and its unit is 10, the 1 is omitted.
   * - Sequences of 0 digits and their units are replaced by a single 0 and no unit.
   * - If there are two such sequences of 0 digits in a level (1000's and 10's), the 1000's 0 is also omitted.
   * - The 1000's 0 is also omitted in alternating levels, such that it is omitted in the rightmost
   *     level with a 10's 0, or if none, in the rightmost level.
   * - Level symbols are omitted if all of their units are omitted
   */
  public static String chineseNumber(long n, ChineseDigits zh) {
    if (n < 0) {
      n = -n;
    }
    if (n <= 10) {
      if (n == 2) {
        return String.valueOf(zh.liang);
      }
      return String.valueOf(zh.digits[(int)n]);
    }

    // 9223372036854775807
    char[] buf = new char[40]; // as long as we get, and actually we can't get this high, no units past zhao
    char[] digits = String.valueOf(n).toCharArray();

    // first, generate all the digits in place
    // convert runs of zeros into a single zero, but keep places
    // 
    boolean inZero = true; // true if we should zap zeros in this block, resets at start of block
    boolean forcedZero = false; // true if we have a 0 in tens's place
    int x = buf.length;
    for (int i = digits.length, u = -1, l = -1; --i >= 0;) {
      if (u == -1) {
        if (l != -1) {
          buf[--x] = zh.levels[l];
          inZero = true;
          forcedZero = false;
        }
        ++u;
      } else {
        buf[--x] = zh.units[u++];
        if (u == 3) {
          u = -1;
          ++l;
        }
      }
      int d = digits[i] - '0';
      if (d == 0) {
        if (x < buf.length-1 && u != 0) {
          buf[x] = '*';
        }
        if (inZero || forcedZero) {
          buf[--x] = '*';
        } else {
          buf[--x] = zh.digits[0];
          inZero = true;
          forcedZero = u == 1;
        }
      } else {
        inZero = false;
        buf[--x] = zh.digits[d];
      }
    }

    // scanning from right, find first required 'ling'
    // we only care if n > 101,0000 as this is the first case where
    // it might shift.  remove optional lings in alternating blocks.
    if (n > 1000000) {
      boolean last = true;
      int i = buf.length - 3;
      do {
        if (buf[i] == '0') {
          break;
        }
        i -= 8;
        last = !last;
      } while (i > x);

      i = buf.length - 7;
      do {
        if (buf[i] == zh.digits[0] && !last) {
          buf[i] = '*';
        }
        i -= 8;
        last = !last;
      } while (i > x);

      // remove levels for empty blocks
      if (n >= 100000000) {
        i = buf.length - 8;
        do {
          boolean empty = true;
          for (int j = i-1, e = Math.max(x-1, i-8); j > e; --j) {
            if (buf[j] != '*') {
              empty = false;
              break;
            }
          }
          if (empty) {
            if (buf[i+1] != '*' && buf[i+1] != zh.digits[0]) {
              buf[i] = zh.digits[0];
            } else {
              buf[i] = '*';
            }
          }
          i -= 8;
        } while (i > x);
      }          
    }

    // replace er by liang except before or after shi or after ling
    for (int i = x; i < buf.length; ++i) {
      if (buf[i] != zh.digits[2]) continue;
      if (i < buf.length - 1 && buf[i+1] == zh.units[0]) continue;
      if (i > x && (buf[i-1] == zh.units[0] || buf[i-1] == zh.digits[0] || buf[i-1] == '*')) continue;

      buf[i] = zh.liang;
    }
    
    // eliminate leading 1 if following unit is shi
    if (buf[x] == zh.digits[1] && (zh.ko || buf[x+1] == zh.units[0])) {
      ++x;
    }

    // now, compress out the '*'
    int w = x;
    for (int r = x; r < buf.length; ++r) {
      if (buf[r] != '*') {
        buf[w++] = buf[r];
      }
    }
    return new String(buf, x, w-x);
  }

//  public static void main(String[] args) {
//    for (int i = 0; i < args.length; ++i) {
//      String arg = args[i];
//      System.out.print(arg);
//      System.out.print(" > ");
//      long n = Long.parseLong(arg);
//      System.out.println(chineseNumber(n, ChineseDigits.DEBUG));
//    }
//  }

  public static class ChineseDigits {
    final char[] digits;
    final char[] units;
    final char[] levels;
    final char liang;
    final boolean ko;

    ChineseDigits(String digits, String units, String levels, char liang, boolean ko) {
      this.digits = digits.toCharArray();
      this.units = units.toCharArray();
      this.levels = levels.toCharArray();
      this.liang = liang;
      this.ko = ko;
    }

    public static final ChineseDigits DEBUG = 
      new ChineseDigits("0123456789s", "sbq", "WYZ", 'L', false);

    public static final ChineseDigits TRADITIONAL = 
      new ChineseDigits("\u96f6\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341", // to shi
                        "\u5341\u767e\u5343", // shi, bai, qian
                        "\u842c\u5104\u5146", // wan, yi, zhao
                        '\u5169', false); // liang

    public static final ChineseDigits SIMPLIFIED = 
      new ChineseDigits("\u96f6\u4e00\u4e8c\u4e09\u56db\u4e94\u516d\u4e03\u516b\u4e5d\u5341", // to shi
                        "\u5341\u767e\u5343", // shi, bai, qian
                        "\u4e07\u4ebf\u5146", // wan, yi, zhao
                        '\u4e24', false); // liang
    
    // no 1 before first unit no matter what it is
    // not sure if there are 'ling' units
    public static final ChineseDigits KOREAN =
      new ChineseDigits("\uc601\uc77c\uc774\uc0bc\uc0ac\uc624\uc721\uce60\ud314\uad6c\uc2ed", // to ten
                        "\uc2ed\ubc31\ucc9c", // 10, 100, 1000
                        "\ub9cc\uc5b5?", // 10^4, 10^8, 10^12
                        '\uc774', true);
  }
}
