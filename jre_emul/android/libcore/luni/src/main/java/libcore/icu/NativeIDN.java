/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.icu;

/**
 * Java version of Android's NativeIDN class, rewritten for J2ObjC because the
 * Android version uses native code that depends on ICU's uidna functions, which
 * aren't public on iOS.
 * <p>
 * This code is converted from the pseudo-code from the RFC 3492 specification
 * (https://www.ietf.org/rfc/rfc3492.txt).
 */
public final class NativeIDN {

  // Bootstring parameters for Punycode.
  private static final int BASE = 36;
  private static final int TMIN = 1;
  private static final int TMAX = 26;
  private static final int SKEW = 38;
  private static final int DAMP = 700;
  private static final int INITIAL_BIAS = 72;
  private static final int INITIAL_N = 128;
  private static final char DELIMITER = '-';

  /**
   * Convert a Unicode string to Punycode/ASCII. The flags parameter is
   * ignored; it's used by the ICU functions, but the spec doesn't describe
   * a need for them.
   */
  public static String toASCII(String s, int flags) {
    int n = INITIAL_N;
    int delta = 0;
    int bias = INITIAL_BIAS;
    StringBuffer output = new StringBuffer();

    // Copy all basic code points to the output.
    int b = 0;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (basicCodePoint(c)) {
        output.append(c);
        b++;
      }
    }
    if (b > 0) {
      output.append(DELIMITER);
    }
    int h = b;
    while (h < s.length()) {
      int m = Integer.MAX_VALUE;
      for (int i = 0; i < s.length(); i++) {
        int c = s.charAt(i);
        if (c >= n && c < m) {
          m = c;
        }
      }
      if (m - n > (Integer.MAX_VALUE - delta) / (h + 1)) {
        // This should probably be a java.text.ParseException, but
        // IllegalArgumentException is what Android's IDN expects.
        throw new IllegalArgumentException("encoding overflow");
      }
      delta = delta + (m - n) * (h + 1);
      n = m;
      for (int j = 0; j < s.length(); j++) {
        int c = s.charAt(j);
        if (c < n) {
          delta++;
          if (0 == delta) {
            throw new IllegalArgumentException("encoding overflow");
          }
        }
        if (c == n) {
          int q = delta;
          for (int k = BASE;; k += BASE) {
            int t;
            if (k <= bias) {
              t = TMIN;
            } else if (k >= bias + TMAX) {
              t = TMAX;
            } else {
              t = k - bias;
            }
            if (q < t) {
              break;
            }
            output.append((char) encodeDigit(t + (q - t) % (BASE - t)));
            q = (q - t) / (BASE - t);
          }

          output.append((char) encodeDigit(q));
          bias = adapt(delta, h + 1, h == b);
          delta = 0;
          h++;
        }
      }
      delta++;
      n++;
    }
    return output.toString();
  }

  /**
   * Convert a Punycode/ASCII string to Unicode. The flags parameter is ignored;
   * it's used by the ICU functions, but the spec doesn't describe a need
   * for them.
   */
  public static String toUnicode(String s, int flags) {
    int n = INITIAL_N;
    int i = 0;
    int bias = INITIAL_BIAS;
    StringBuffer output = new StringBuffer();
    int d = s.lastIndexOf(DELIMITER);
    if (d > 0) {
      for (int j = 0; j < d; j++) {
        char c = s.charAt(j);
        if (!basicCodePoint(c)) {
          throw new IllegalArgumentException("bad input: " + c);
        }
        output.append(c);
      }
      d++;
    } else {
      d = 0;
    }
    while (d < s.length()) {
      int oldi = i;
      int w = 1;
      for (int k = BASE;; k += BASE) {
        if (d == s.length()) {
          throw new IllegalArgumentException("bad input: " + d);
        }
        int c = s.charAt(d++);
        int digit = decodeDigit(c);
        if (digit > (Integer.MAX_VALUE - i) / w) {
          throw new IllegalArgumentException("encoding overflow");
        }
        i = i + digit * w;
        int t;
        if (k <= bias) {
          t = TMIN;
        } else if (k >= bias + TMAX) {
          t = TMAX;
        } else {
          t = k - bias;
        }
        if (digit < t) {
          break;
        }
        w = w * (BASE - t);
      }
      bias = adapt(i - oldi, output.length() + 1, oldi == 0);
      if (i / (output.length() + 1) > Integer.MAX_VALUE - n) {
        throw new IllegalArgumentException("encoding overflow");
      }
      n = n + i / (output.length() + 1);
      i = i % (output.length() + 1);
      output.insert(i, (char) n);
      i++;
    }

    return output.toString();
  }

  private static int adapt(int delta, int numpoints, boolean firsttime) {
    if (firsttime) {
      delta = delta / DAMP;
    } else {
      delta = delta / 2;
    }
    delta = delta + (delta / numpoints);
    int k = 0;
    while (delta > ((BASE - TMIN) * TMAX) / 2) {
      delta = delta / (BASE - TMIN);
      k = k + BASE;
    }
    return k + (((BASE - TMIN + 1) * delta) / (delta + SKEW));
  }

  private static int decodeDigit(int c) {
    if (c >= 'A' && c <= 'Z') {
      return c - 'A';
    }
    if (c >= 'a' && c <= 'z') {
      return (c - 'a');
    }
    if (c >= '0' && c <= '9') {
      return c - '0' + 26;
    }
    throw new IllegalArgumentException("bad input: " + c);
  }

  private static char encodeDigit(int d) {
    if (d >= 0 && d <= 25) {
      return (char) (d + 'a');
    }
    if (d >= 26 && d <= 35) {
      return (char) (d - 26 + '0');
    }
    throw new IllegalArgumentException("bad input: " + d);
  }

  private static boolean basicCodePoint(int cp) {
    return cp < 0x80;
  }

  private NativeIDN() {
  }
}
