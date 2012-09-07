/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.j2objc.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.devtools.j2objc.J2ObjC;

import java.io.UnsupportedEncodingException;

/**
 * Utility methods for translating Unicode strings to Objective-C.
 *
 * @author Tom Ball
 */
public final class UnicodeUtils {

  private UnicodeUtils() {
    // Don't instantiate.
  }

  /**
   * Converts Unicode sequences that aren't all valid C++ universal
   * characters, as defined by ISO 14882, section 2.2, to either
   * characters or hexadecimal escape sequences.
   */
  public static String escapeUnicodeSequences(String s) {
    return escapeUnicodeSequences(s, true);
  }

  @VisibleForTesting
  static String escapeUnicodeSequences(String s, boolean logErrorMessage) {
    if (s.contains("\\u")) {
      StringBuilder buffer = new StringBuilder();
      int i, lastIndex = 0;
      while ((i = s.indexOf("\\u", lastIndex)) != -1) {
        String chunk = s.substring(lastIndex, i);
        buffer.append(chunk);

        // Convert hex Unicode number; format valid due to compiler check.
        if (s.length() >= i + 6) {
          int value = Integer.parseInt(s.substring(i + 2, i + 6), 16);
          String convertedChar = escapeCharacter(value);
          if (convertedChar != null) {
            buffer.append(convertedChar);
          } else {
            if (!isValidCppCharacter((char) value)) {
              if (logErrorMessage) {
                J2ObjC.error(String.format("Illegal C/C++ Unicode character \\u%4x in \"%s\"",
                    value, s));
              } else {
                J2ObjC.error();
              }
              // Fall-through to print, so output is debug-able.
            }
            // Print Unicode sequence.
            buffer.append(s.substring(i, i + 6));
          }
          lastIndex = i + 6;
        } else {
          buffer.append(s.substring(i));
          lastIndex = s.length();
        }
      }
      buffer.append(s.substring(lastIndex));
      return buffer.toString();
    } else {
      return s;
    }
  }

  /**
   * Returns true if all characters in a string can be expressed as either
   * C++ universal characters or valid hexadecimal escape sequences.
   */
  public static boolean hasValidCppCharacters(String s) {
    for (char c : s.toCharArray()) {
      if (!isValidCppCharacter(c)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Converts a character into either a character or a hexadecimal sequence,
   * depending on whether it is a valid C++ universal character, as defined
   * by ISO 14882, section 2.2. Returns the converted character as a String,
   * or null if the given value was not handled.
   */
  public static String escapeCharacter(int value) {
    StringBuilder buffer = new StringBuilder();
    if (value >= 0x20 && value <= 0x7E) {
      // Printable ASCII character.
      buffer.append((char) value);
    } else if (value < 0x20 || (value >= 0x7F && value < 0xA0)) {
      // Invalid C++ Unicode number, convert to UTF-8 sequence.
      String charString = new String(new char[]{ (char) value });
      try {
        for (byte b : charString.getBytes("UTF-8")) {
          int unsignedByte = b & 0xFF;
          buffer.append("\\x");
          if (unsignedByte < 16) {
            buffer.append('0');
          }
          buffer.append(Integer.toHexString(unsignedByte));
        }
      } catch (UnsupportedEncodingException e) {
        throw new AssertionError("UTF-8 is an unsupported encoding");
      }
    } else {
      return null;
    }
    return buffer.toString();
  }

  /**
   * Returns true if the specified character can be represented in a C string
   * or character literal declaration.  Value ranges were determined from the
   * <a href="http://www.unicode.org/charts/">Unicode 6.0 Character Code
   * Charts</a>.
   * <p>
   * Note: these ranges include code points which Character.isDefined(ch)
   * returns as false in Java 6.  OpenJDK 7 lists an update to Unicode 6.0
   * as one of its features, so when Java 7 is widely available this method
   * can be removed.
   */
  public static boolean isValidCppCharacter(char c) {
    // This would be more efficiently implemented as a bitmap, but since it's
    // not used in performance-critical code, this form is easier to inspect.
    return c < 0xe000 ||
        c >= 0xf900 && c <= 0xfad9 ||
        c >= 0xfb50 && c <= 0xfbc1 ||
        c >= 0xfbd3 && c <= 0xfd3f ||
        c >= 0xfd5f && c <= 0xfd8f ||
        c >= 0xfc92 && c <= 0xfdc7 ||
        c >= 0xfdf0 && c <= 0xfdfd ||
        c >= 0xfe10 && c <= 0xfe19 ||
        c >= 0xfe20 && c <= 0xfe26 ||
        c >= 0xfe30 && c <= 0xfe4f ||
        c >= 0xfe50 && c <= 0xfe52 ||
        c >= 0xfe54 && c <= 0xfe66 ||
        c >= 0xfe68 && c <= 0xfe6b ||
        c >= 0xfe70 && c <= 0xfe74 ||
        c >= 0xfe76 && c <= 0xfefc ||
        c == 0xfeff ||
        c >= 0xff01 && c <= 0xffbe ||
        c >= 0xffc2 && c <= 0xffc7 ||
        c >= 0xffca && c <= 0xffcf ||
        c >= 0xffd2 && c <= 0xffd7 ||
        c >= 0xffda && c <= 0xffdc ||
        c >= 0xffe0 && c <= 0xffe6 ||
        c >= 0xffe8 && c <= 0xffee ||
        c >= 0xfff9 && c <= 0xfffd;
  }
}
