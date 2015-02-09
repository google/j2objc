/*
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

// Native versions of some methods in Android's IntegralToString.java.

#include "java_lang_IntegralToString.h"

#include "java/lang/AbstractStringBuilder.h"
#include "java/lang/Character.h"
#include "java/lang/IntegralToString.h"
#include "java/lang/StringBuilder.h"

static int IntegralToString_intIntoCharArray(unichar *buf, int cursor, uint32_t n);

/**
 * These tables are used to special-case toString computation for
 * small values.  This serves three purposes: it reduces memory usage;
 * it increases performance for small values; and it decreases the
 * number of comparisons required to do the length computation.
 * Elements of this table are lazily initialized on first use.
 * No locking is necessary, i.e., we use the non-volatile, racy
 * single-check idiom.
 */
static NSString *IntegralToString_SMALL_NONNEGATIVE_VALUES[100];
static NSString *IntegralToString_SMALL_NEGATIVE_VALUES[100];

/** TENS[i] contains the tens digit of the number i, 0 <= i <= 99. */
static const jchar IntegralToString_TENS[] = {
  '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
  '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
  '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
  '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
  '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
  '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
  '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
  '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
  '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
  '9', '9', '9', '9', '9', '9', '9', '9', '9', '9'
};

/** Ones [i] contains the tens digit of the number i, 0 <= i <= 99. */
static const jchar IntegralToString_ONES[] = {
  '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
  '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
  '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
  '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
  '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
  '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
  '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
  '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
  '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
  '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
};

/**
 * Table for MOD / DIV 10 computation described in Section 10-21
 * of Hank Warren's "Hacker's Delight" online addendum.
 * http://www.hackersdelight.org/divcMore.pdf
 */
static const jbyte IntegralToString_MOD_10_TABLE[] = {
  0, 1, 2, 2, 3, 3, 4, 5, 5, 6, 7, 7, 8, 8, 9, 0
};

/**
 * The digits for every supported radix.
 */
static const jchar IntegralToString_DIGITS[] = {
  '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
  'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
  'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
  'u', 'v', 'w', 'x', 'y', 'z'
};

static const jchar IntegralToString_UPPER_CASE_DIGITS[] = {
  '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
  'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
  'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
  'U', 'V', 'W', 'X', 'Y', 'Z'
};

/**
 * Creates strings to be cached in SMALL_NONNEGATIVE_VALUES or
 * SMALL_NEGATIVE_VALUES. The returned strings are not autoreleased because they
 * will live for the duration of the program.
 */
static NSString *IntegralToString_stringOf1(unichar c) {
  return [[NSString alloc] initWithCharacters:&c length:1];
}

static NSString *IntegralToString_stringOf2(unichar c1, unichar c2) {
  unichar buf[2];
  buf[0] = c1;
  buf[1] = c2;
  return [[NSString alloc] initWithCharacters:buf length:2];
}

static NSString *IntegralToString_stringOf3(unichar c1, unichar c2, unichar c3) {
  unichar buf[3];
  buf[0] = c1;
  buf[1] = c2;
  buf[2] = c3;
  return [[NSString alloc] initWithCharacters:buf length:3];
}

/**
 * Equivalent to Integer.toString(i, radix).
 */
NSString *JavaLangIntegralToString_intToStringWithInt_withInt_(jint i, jint radix) {
  if (radix < JavaLangCharacter_MIN_RADIX || radix > JavaLangCharacter_MAX_RADIX) {
    radix = 10;
  }
  if (radix == 10) {
    return IntegralToString_convertInt(nil, i);
  }

  /*
   * If i is positive, negate it. This is the opposite of what one might
   * expect. It is necessary because the range of the negative values is
   * strictly larger than that of the positive values: there is no
   * positive value corresponding to Integer.MIN_VALUE.
   */
  jboolean negative = NO;
  if (i < 0) {
    negative = YES;
  } else {
    i = -i;
  }

  jint bufLen = radix < 8 ? 33 : 12;  // Max chars in result (conservative)
  jchar buf[bufLen];
  jint cursor = bufLen;

  do {
    jint q = i / radix;
    buf[--cursor] = IntegralToString_DIGITS[radix * q - i];
    i = q;
  } while (i != 0);

  if (negative) {
    buf[--cursor] = '-';
  }

  return [NSString stringWithCharacters:buf + cursor length:bufLen - cursor];
}

/**
 * Returns the string representation of i and leaves sb alone if sb is null.
 * Returns null and appends the string representation of i to sb if sb is non-null.
 */
NSString *IntegralToString_convertInt(JreStringBuilder *sb, int i) {
  BOOL negative = NO;
  NSString *quickResult = nil;
  if (i < 0) {
    negative = YES;
    i = -i;
    if (i < 100) {
      if (i < 0) {
        // If -n is still negative, n is Integer.MIN_VALUE
        quickResult = @"-2147483648";
      } else {
        quickResult = IntegralToString_SMALL_NEGATIVE_VALUES[i];
        if (!quickResult) {
          IntegralToString_SMALL_NEGATIVE_VALUES[i] = quickResult =
              i < 10 ? IntegralToString_stringOf2('-', IntegralToString_ONES[i])
              : IntegralToString_stringOf3('-', IntegralToString_TENS[i], IntegralToString_ONES[i]);
        }
      }
    }
  } else {
    if (i < 100) {
      quickResult = IntegralToString_SMALL_NONNEGATIVE_VALUES[i];
      if (!quickResult) {
        IntegralToString_SMALL_NONNEGATIVE_VALUES[i] = quickResult =
            i < 10 ? IntegralToString_stringOf1(IntegralToString_ONES[i])
            : IntegralToString_stringOf2(IntegralToString_TENS[i], IntegralToString_ONES[i]);
      }
    }
  }
  if (quickResult) {
    if (sb) {
      JreStringBuilder_appendString(sb, quickResult);
      return nil;
    }
    return quickResult;
  }

  int bufLen = 11; // Max number of chars in result
  unichar buf[bufLen];
  int cursor = bufLen;

  // Calculate digits two-at-a-time till remaining digits fit in 16 bits
  while (i >= (1 << 16)) {
    // Compute q = n/100 and r = n % 100 as per "Hacker's Delight" 10-8
    int q = (int) (((unsigned long long) 0x51EB851FLL * i) >> 37);
    int r = i - 100 * q;
    buf[--cursor] = IntegralToString_ONES[r];
    buf[--cursor] = IntegralToString_TENS[r];
    i = q;
  }

  // Calculate remaining digits one-at-a-time for performance
  while (i != 0) {
    // Compute q = n/10 and r = n % 10 as per "Hacker's Delight" 10-8
    int q = (int) (((unsigned int) 0xCCCD * i) >> 19);
    int r = i - 10 * q;
    buf[--cursor] = IntegralToString_DIGITS[r];
    i = q;
  }

  if (negative) {
    buf[--cursor] = '-';
  }

  if (sb) {
    JreStringBuilder_appendBuffer(sb, buf + cursor, bufLen - cursor);
    return nil;
  } else {
    return [NSString stringWithCharacters:buf + cursor length:bufLen - cursor];
  }
}

/**
 * Equivalent to Long.toString(v, radix).
 */
NSString *JavaLangIntegralToString_longToStringWithLong_withInt_(jlong v, jint radix) {
  jint i = (jint) v;
  if (i == v) {
    return JavaLangIntegralToString_intToStringWithInt_withInt_(i, radix);
  }

  if (radix < JavaLangCharacter_MIN_RADIX || radix > JavaLangCharacter_MAX_RADIX) {
    radix = 10;
  }
  if (radix == 10) {
    return IntegralToString_convertLong(nil, v);
  }

  /*
   * If v is positive, negate it. This is the opposite of what one might
   * expect. It is necessary because the range of the negative values is
   * strictly larger than that of the positive values: there is no
   * positive value corresponding to Integer.MIN_VALUE.
   */
  jboolean negative = NO;
  if (v < 0) {
    negative = YES;
  } else {
    v = -v;
  }

  jint bufLen = radix < 8 ? 65 : 23;  // Max chars in result (conservative)
  jchar buf[bufLen];
  jint cursor = bufLen;

  do {
    jlong q = v / radix;
    buf[--cursor] = IntegralToString_DIGITS[(jint) (radix * q - v)];
    v = q;
  } while (v != 0);

  if (negative) {
    buf[--cursor] = '-';
  }

  return [NSString stringWithCharacters:buf + cursor length:bufLen - cursor];
}

/**
 * Returns the string representation of n and leaves sb alone if sb is null.
 * Returns null and appends the string representation of n to sb if sb is non-null.
 */
NSString *IntegralToString_convertLong(JreStringBuilder *sb, long long n) {
  int i = (int) n;
  if (i == n) {
    return IntegralToString_convertInt(sb, i);
  }

  BOOL negative = (n < 0);
  if (negative) {
    n = -n;
    if (n < 0) {
      // If -n is still negative, n is Long.MIN_VALUE
      NSString *quickResult = @"-9223372036854775808";
      if (sb) {
        JreStringBuilder_appendString(sb, quickResult);
        return nil;
      }
      return quickResult;
    }
  }

  int bufLen = 20; // Maximum number of chars in result
  unichar buf[bufLen];

  int low = (int) (n % 1000000000); // Extract low-order 9 digits
  int cursor = IntegralToString_intIntoCharArray(buf, bufLen, low);

  // Zero-pad Low order part to 9 digits
  while (cursor != (bufLen - 9)) {
    buf[--cursor] = '0';
  }

  /*
   * The remaining digits are (n - low) / 1,000,000,000.  This
   * "exact division" is done as per the online addendum to Hank Warren's
   * "Hacker's Delight" 10-20, http://www.hackersdelight.org/divcMore.pdf
   */
  n = ((long long) (((unsigned long long) (n - low)) >> 9)) * (long long) 0x8E47CE423A2E9C6DLL;

  /*
   * If the remaining digits fit in an int, emit them using a
   * single call to intIntoCharArray. Otherwise, strip off the
   * low-order digit, put it in buf, and then call intIntoCharArray
   * on the remaining digits (which now fit in an int).
   */
  if ((n & ((long long) (((uint64_t) -1LL) << 32))) == 0) {
    cursor = IntegralToString_intIntoCharArray(buf, cursor, (int) n);
  } else {
    /*
     * Set midDigit to n % 10
     */
    unsigned int lo32 = (unsigned int) n;
    unsigned int hi32 = (unsigned int) (((unsigned long long) n) >> 32);

    // midDigit = ((unsigned) low32) % 10, per "Hacker's Delight" 10-21
    int midDigit =
        IntegralToString_MOD_10_TABLE[(0x19999999 * lo32 + (lo32 >> 1) + (lo32 >> 3)) >> 28];

    // Adjust midDigit for hi32. (assert hi32 == 1 || hi32 == 2)
    midDigit -= (int)(hi32 << 2);  // 1L << 32 == -4 MOD 10
    if (midDigit < 0) {
        midDigit += 10;
    }
    buf[--cursor] = IntegralToString_DIGITS[midDigit];

    // Exact division as per Warren 10-20
    int rest = ((int) (((unsigned long long) (n - midDigit)) >> 1)) * (int) 0xCCCCCCCD;
    cursor = IntegralToString_intIntoCharArray(buf, cursor, rest);
  }

  if (negative) {
    buf[--cursor] = '-';
  }
  if (sb) {
    JreStringBuilder_appendBuffer(sb, buf + cursor, bufLen - cursor);
    return nil;
  } else {
    return [NSString stringWithCharacters:buf + cursor length:bufLen - cursor];
  }
}

/**
 * Inserts the unsigned decimal integer represented by n into the specified
 * character array starting at position cursor.  Returns the index after
 * the last character inserted (i.e., the value to pass in as cursor the
 * next time this method is called). Note that n is interpreted as a large
 * positive integer (not a negative integer) if its sign bit is set.
 */
static int IntegralToString_intIntoCharArray(unichar *buf, int cursor, uint32_t n) {
  // Calculate digits two-at-a-time till remaining digits fit in 16 bits
  while ((n & 0xffff0000) != 0) {
    /*
     * Compute q = n/100 and r = n % 100 as per "Hacker's Delight" 10-8.
     */
    uint32_t q = ((uint64_t)0x51EB851FLL * n) >> 37;
    uint32_t r = n - 100 * q;
    buf[--cursor] = IntegralToString_ONES[r];
    buf[--cursor] = IntegralToString_TENS[r];
    n = q;
  }

  // Calculate remaining digits one-at-a-time for performance
  while (n != 0) {
    // Compute q = n / 10 and r = n % 10 as per "Hacker's Delight" 10-8
    int q = ((uint32_t)0xCCCD * n) >> 19;
    int r = n - 10 * q;
    buf[--cursor] = IntegralToString_DIGITS[r];
    n = q;
  }
  return cursor;
}

NSString *JavaLangIntegralToString_intToBinaryStringWithInt_(jint i) {
  jint bufLen = 32;  // Max number of binary digits in an int
  jchar buf[bufLen];
  jint cursor = bufLen;

  do {
    buf[--cursor] = IntegralToString_DIGITS[i & 1];
  } while ((URShiftAssignInt(&i, 1)) != 0);

  return [NSString stringWithCharacters:buf + cursor length:bufLen - cursor];
}

NSString *JavaLangIntegralToString_longToBinaryStringWithLong_(jlong v) {
  jint i = (jint) v;
  if (v >= 0 && i == v) {
    return JavaLangIntegralToString_intToBinaryStringWithInt_(i);
  }

  jint bufLen = 64;  // Max number of binary digits in a long
  jchar buf[bufLen];
  jint cursor = bufLen;

  do {
    buf[--cursor] = IntegralToString_DIGITS[((jint) v) & 1];
  } while ((URShiftAssignLong(&v, 1)) != 0);

  return [NSString stringWithCharacters:buf + cursor length:bufLen - cursor];
}

JavaLangStringBuilder *
    JavaLangIntegralToString_appendByteAsHexWithJavaLangStringBuilder_withByte_withBoolean_(
    JavaLangStringBuilder *sb, jbyte b, jboolean upperCase) {
  nil_chk(sb);
  const jchar *digits = upperCase ? IntegralToString_UPPER_CASE_DIGITS : IntegralToString_DIGITS;
  [sb appendWithChar:digits[(b >> 4) & 0xf]];
  [sb appendWithChar:digits[b & 0xf]];
  return sb;
}

NSString *JavaLangIntegralToString_byteToHexStringWithByte_withBoolean_(
    jbyte b, jboolean upperCase) {
  const jchar *digits = upperCase ? IntegralToString_UPPER_CASE_DIGITS : IntegralToString_DIGITS;
  jchar buf[2];
  buf[0] = digits[(b >> 4) & 0xf];
  buf[1] = digits[b & 0xf];
  return [NSString stringWithCharacters:buf length:2];
}

NSString *JavaLangIntegralToString_bytesToHexStringWithByteArray_withBoolean_(
    IOSByteArray *bytes, jboolean upperCase) {
  const jchar *digits = upperCase ? IntegralToString_UPPER_CASE_DIGITS : IntegralToString_DIGITS;
  jint size = bytes->size_;
  jchar buf[size * 2];
  jint c = 0;
  for (jint i = 0; i < size; i++) {
    jbyte b = bytes->buffer_[i];
    buf[c++] = digits[(b >> 4) & 0xf];
    buf[c++] = digits[b & 0xf];
  }
  return [NSString stringWithCharacters:buf length:size * 2];
}

NSString *JavaLangIntegralToString_intToHexStringWithInt_withBoolean_withInt_(
    jint i, jboolean upperCase, jint minWidth) {
  jint bufLen = 8;  // Max number of hex digits in an int
  jchar buf[bufLen];
  jint cursor = bufLen;

  const jchar *digits = upperCase ? IntegralToString_UPPER_CASE_DIGITS : IntegralToString_DIGITS;
  do {
    buf[--cursor] = digits[i & 0xf];
  } while ((URShiftAssignInt(&i, 4)) != 0 || (bufLen - cursor < minWidth));

  return [NSString stringWithCharacters:buf + cursor length:bufLen - cursor];
}

NSString *JavaLangIntegralToString_longToHexStringWithLong_(jlong v) {
  jint i = (jint) v;
  if (v >= 0 && i == v) {
    return JavaLangIntegralToString_intToHexStringWithInt_withBoolean_withInt_(i, false, 0);
  }

  jint bufLen = 16;  // Max number of hex digits in a long
  jchar buf[bufLen];
  jint cursor = bufLen;

  do {
    buf[--cursor] = IntegralToString_DIGITS[((jint) v) & 0xF];
  } while ((URShiftAssignLong(&v, 4)) != 0);

  return [NSString stringWithCharacters:buf + cursor length:bufLen - cursor];
}

NSString *JavaLangIntegralToString_intToOctalStringWithInt_(jint i) {
  jint bufLen = 11;  // Max number of octal digits in an int
  jchar buf[bufLen];
  jint cursor = bufLen;

  do {
    buf[--cursor] = IntegralToString_DIGITS[i & 7];
  } while ((URShiftAssignInt(&i, 3)) != 0);

  return [NSString stringWithCharacters:buf + cursor length:bufLen - cursor];
}

NSString *JavaLangIntegralToString_longToOctalStringWithLong_(jlong v) {
  jint i = (jint) v;
  if (v >= 0 && i == v) {
    return JavaLangIntegralToString_intToOctalStringWithInt_(i);
  }
  jint bufLen = 22;  // Max number of octal digits in a long
  jchar buf[bufLen];
  jint cursor = bufLen;

  do {
    buf[--cursor] = IntegralToString_DIGITS[((jint) v) & 7];
  } while ((URShiftAssignLong(&v, 3)) != 0);

  return [NSString stringWithCharacters:buf + cursor length:bufLen - cursor];
}
