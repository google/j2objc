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

import java.io.UnsupportedEncodingException;
import java.util.Locale;

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
   * Returns a valid C/C++ character literal (including quotes).
   */
  public static String escapeCharLiteral(char c) {
    if (c >= 0x20 && c <= 0x7E) { // if ASCII
      switch (c) {
        case '\'': return "'\\''";
        case '\\': return "'\\\\'";
      }
      return "'" + c + "'";
    } else {
      return UnicodeUtils.format("0x%04x", (int) c);
    }
  }

  /**
   * Returns a valid ObjC string literal (excluding quotes).
   */
  public static String escapeStringLiteral(String s) {
    StringBuilder sb = null;
    int len = s.length();
    int lastIndex = 0;
    for (int i = 0; i < len; i++) {
      String replacement = escapeCharacterForStringLiteral(s.charAt(i), s, i);
      if (replacement == null) {
        continue;
      }
      if (sb == null) {
        sb = new StringBuilder();
      }
      if (lastIndex < i) {
        sb.append(s.substring(lastIndex, i));
      }
      lastIndex = i + 1;
      sb.append(replacement);
    }
    if (sb != null) {
      sb.append(s.substring(lastIndex, len));
      return sb.toString();
    } else {
      return s;
    }
  }

  private static String escapeCharacterForStringLiteral(char c, String s, int idx) {
    switch (c) {
      case '\\': return "\\\\";
      case '"': return "\\\"";
      case '\n': return "\\n";
      case '\t': return "\\t";
    }
    if (c >= 0x20 && c <= 0x7E) {
      // Printable ASCII character.
      return null;
    } else if (c < 0x20 || (c >= 0x7F && c < 0xA0)) {
      // Invalid C++ Unicode number, convert to UTF-8 sequence.
      if (idx + 1 < s.length() && isHexChar(s.charAt(idx + 1))) {
        // If followed by another hex character, we must terminate the hex sequence.
        return escapeUtf8(c) + "\"\"";
      }
      return escapeUtf8(c);
    } else {
      if (!isValidCppCharacter(c)) {
        ErrorUtil.error(String.format(
            "Illegal C/C++ Unicode character \\u%4x in \"%s\"", (int) c, s));
      }
      return "\\u" + UnicodeUtils.format("%04x", (int) c);
    }
  }

  private static boolean isHexChar(char c) {
    return c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
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

  private static String escapeUtf8(char value) {
    StringBuilder buffer = new StringBuilder();
    String charString = Character.toString(value);
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
    return c < 0xd800 ||
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

  /**
   * For a given String, returns a legal identifier for Objective-C.
   */
  // TODO(mthvedt): Consider using this for all identifiers.
  public static String asValidObjcIdentifier(String word) {
    StringBuffer objcWord = new StringBuffer();
    int offset = 0;

    if (word.length() > 0 && Character.isDigit(word.codePointAt(0))) {
      // Identifiers must not start with a digit
      objcWord.append("_");
    }

    while (offset < word.length()) {
      int codepoint = word.codePointAt(offset);
      offset += Character.charCount(codepoint);
      if (Character.isLetterOrDigit(codepoint)) {
        objcWord.appendCodePoint(codepoint);
      } else if (codepoint == '$') {
        // Allowed by Clang in non-strict mode (and used in J2ObjC)
        objcWord.append('$');
      } else {
        objcWord.append("_");
      }
    }

    return objcWord.toString();
  }

  /**
   * Invokes String.format() using Locale.ROOT, so that local locale
   * settings don't cause generated code to have characters the C compiler
   * can't manage. This method shouldn't be called for error messages or
   * other text displayed to the developer invoking j2objc, however.
   *
   * {@link https://github.com/google/j2objc/issues/698}
   */
  public static String format(String format, Object... args) {
    return String.format(Locale.ROOT, format, args);
  }
}
