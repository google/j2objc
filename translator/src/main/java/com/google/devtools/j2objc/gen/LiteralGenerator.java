/*
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

package com.google.devtools.j2objc.gen;

import com.google.common.annotations.VisibleForTesting;
import com.google.devtools.j2objc.util.UnicodeUtils;

import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.regex.Pattern;

/**
 * Utility methods for generating correct Objective-C literals.
 */
public class LiteralGenerator {

  private static final String EXPONENTIAL_FLOATING_POINT_REGEX =
      "[+-]?\\d*\\.?\\d*[eE][+-]?\\d+";
  private static final String FLOATING_POINT_SUFFIX_REGEX = ".*[fFdD]";
  private static final String HEX_LITERAL_REGEX = "0[xX].*";
  private static final Pattern TRIGRAPH_REGEX = Pattern.compile("@\".*\\?\\?[=/'()!<>-].*\"");

  public static String generateStringLiteral(String value) {
    if (!UnicodeUtils.hasValidCppCharacters(value)) {
      return buildStringFromChars(value);
    }
    String s = "@\"" + UnicodeUtils.escapeStringLiteral(value) + "\"";
    if (TRIGRAPH_REGEX.matcher(s).matches()) {
      // Split string between the two '?' chars in the trigraph, so compiler
      // will concatenate the string without interpreting the trigraph.
      String[] substrings = s.split("\\?\\?");
      StringBuilder buffer = new StringBuilder(substrings[0]);
      for (int i = 1; i < substrings.length; i++) {
        buffer.append("?\" \"?");
        buffer.append(substrings[i]);
      }
      return buffer.toString();
    } else {
      return s;
    }
  }

  @VisibleForTesting
  static String buildStringFromChars(String s) {
    int length = s.length();
    StringBuilder buffer = new StringBuilder();
    buffer.append(
        "[NSString stringWithCharacters:(jchar[]) { ");
    int i = 0;
    while (i < length) {
      char c = s.charAt(i);
      buffer.append("(int) 0x");
      buffer.append(Integer.toHexString(c));
      if (++i < length) {
        buffer.append(", ");
      }
    }
    buffer.append(" } length:");
    String lengthString = Integer.toString(length);
    buffer.append(lengthString);
    buffer.append(']');
    return buffer.toString();
  }

  public static String fixNumberToken(String token, ITypeBinding type) {
    token = token.replace("_", "");  // Remove any embedded underscores.
    assert type.isPrimitive();
    char kind = type.getKey().charAt(0);  // Primitive types have single-character keys.

    switch (kind) {
      case 'D':
        return fixDoubleToken(token);
      case 'F':
        return fixFloatToken(token);
      case 'J':
        return fixLongToken(token);
      case 'I':
        return fixIntToken(token);
      default:
        return token;
    }
  }

  public static String fixDoubleToken(String token) {
    // Convert floating point literals to C format.  No checking is
    // necessary, since the format was verified by the parser.
    if (token.matches(FLOATING_POINT_SUFFIX_REGEX)) {
      token = token.substring(0, token.length() - 1);  // strip suffix
    }
    if (token.matches(HEX_LITERAL_REGEX)) {
      token = Double.toString(Double.parseDouble(token));
    } else if (!token.matches(EXPONENTIAL_FLOATING_POINT_REGEX)) {
      if (token.indexOf('.') == -1) {
        token += ".0";  // C requires a fractional part, except in exponential form.
      }
    }
    return token;
  }

  public static String fixFloatToken(String token) {
    return fixDoubleToken(token) + 'f';
  }

  public static String fixLongToken(String token) {
    if (token.equals("0x8000000000000000L") || token.equals("-9223372036854775808L")) {
      // Convert min long literal to an expression
      token = "-0x7fffffffffffffffLL - 1";
    } else {
      // Convert Java long literals to jlong for Obj-C
      if (token.startsWith("0x")) {
        token = "(jlong) " + token;  // Ensure constant is treated as signed.
      }
      int pos = token.length() - 1;
      int numLs = 0;
      while (pos > 0 && token.charAt(pos) == 'L') {
        numLs++;
        pos--;
      }

      if (numLs == 1) {
        token += 'L';
      }
    }
    return token;
  }

  public static String fixIntToken(String token) {
    if (token.equals("0x80000000") || token.equals("-2147483648")) {
      // Convert min int literal to an expression
      token = "-0x7fffffff - 1";
    } else if (token.startsWith("0x")) {
      token = "(jint) " + token;  // Ensure constant is treated as signed.
    }
    return token;
  }

  public static String generate(Object value) {
    if (value instanceof Boolean) {
      return ((Boolean) value).booleanValue() ? "true" : "false";
    } else if (value instanceof Character) {
      return UnicodeUtils.escapeCharLiteral(((Character) value).charValue());
    } else if (value instanceof Number) {
      return generate((Number) value);
    } else {
      return value.toString();
    }
  }

  public static String generate(Number value) {
    if (value instanceof Long) {
      return generate((Long) value);
    } else if (value instanceof Integer) {
      return generate((Integer) value);
    } else if (value instanceof Float) {
      return generate((Float) value);
    } else if (value instanceof Double) {
      return generate((Double) value);
    } else {
      return value.toString();
    }
  }

  public static String generate(Long value) {
    if (value.longValue() == Long.MIN_VALUE) {
      return "((jlong) 0x8000000000000000LL)";
    } else {
      return value.toString() + "LL";
    }
  }

  public static String generate(Integer value) {
    if (value.intValue() == Integer.MIN_VALUE) {
      return "((jint) 0x80000000)";
    } else {
      return value.toString();
    }
  }

  public static String generate(Float value) {
    float f = value.floatValue();
    if (Float.isNaN(f)) {
      return "NAN";
    } else if (f == Float.POSITIVE_INFINITY) {
      return "INFINITY";
    } else if (f == Float.NEGATIVE_INFINITY) {
      // FP representations are symmetrical.
      return "-INFINITY";
    } else if (f == Float.MAX_VALUE) {
      return "__FLT_MAX__";
    } else if (f == Float.MIN_NORMAL) {
      return "__FLT_MIN__";
    } else {
      return value.toString() + "f";
    }
  }

  public static String generate(Double value) {
    double d = ((Double) value).doubleValue();
    if (Double.isNaN(d)) {
      return "NAN";
    } else if (d == Double.POSITIVE_INFINITY) {
      return "INFINITY";
    } else if (d == Double.NEGATIVE_INFINITY) {
      // FP representations are symmetrical.
      return "-INFINITY";
    } else if (d == Double.MAX_VALUE) {
      return "__DBL_MAX__";
    } else if (d == Double.MIN_NORMAL) {
      return "__DBL_MIN__";
    } else {
      return value.toString();
    }
  }
}
