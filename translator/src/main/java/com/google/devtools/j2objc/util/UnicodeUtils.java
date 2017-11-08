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
   * or character literal declaration. This invalid character range is from
   * section the <a
   * href="http://www.open-std.org/jtc1/sc22/wg14/www/docs/n1124.pdf">C99
   * specification</a>, section 6.4.3.
   */
  public static boolean isValidCppCharacter(char c) {
    return c < 0xd800 || c > 0xdfff;
  }

  /**
   * For a given String, returns a legal identifier for Objective-C.
   */
  // TODO(mthvedt): Consider using this for all identifiers.
  public static String asValidObjcIdentifier(String word) {
    StringBuilder objcWord = new StringBuilder();
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
