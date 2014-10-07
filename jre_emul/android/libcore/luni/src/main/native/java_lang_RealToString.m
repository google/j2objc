/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

// Modified version of Android's java_lang_RealToString.cpp, converted
// to not use JNI calling convention.

#include "java_lang_RealToString.h"

#define LOG_TAG "RealToString"

#include <string.h>
#include <math.h>
#include <stdlib.h>

#include "cbigint.h"
#include "java_lang_IntegralToString.h"
#include "java/lang/AbstractStringBuilder.h"
#include "java/lang/AssertionError.h"
#include "java/lang/Double.h"
#include "java/lang/Float.h"
#include "java/lang/StringBuilder.h"

#define INV_LOG_OF_TEN_BASE_2 (0.30102999566398114) /* Local */

/*NB the Number converter methods are synchronized so it is possible to
 *have global data for use by bigIntDigitGenerator */
#define RM_SIZE 21     /* Local. */
#define STemp_SIZE 22  /* Local. */

#define DIGITS_SIZE 64

static void freeFormatExponential(
    JreStringBuilder *sb, BOOL positive, int k, const char digits[], int digitCount);
static void freeFormat(
    JreStringBuilder *sb, BOOL positive, int k, const char digits[], int digitCount);
static void bigIntDigitGenerator(
    long long int f, int e, BOOL isDenormalized, int p, int *firstK, char digits[],
    int *digitCount);
static void longDigitGenerator(
    long long f, int e, BOOL isDenormalized, BOOL mantissaIsZero, int p, int *firstK, char digits[],
    int *digitCount);

/**
 * An array with powers of ten that fit in the type <code>long</code>
 * (<code>10^0,10^1,...,10^18</code>).
 */
static const long long LONG_POWERS_OF_TEN[] = {
  1LL,
  10LL,
  100LL,
  1000LL,
  10000LL,
  100000LL,
  1000000LL,
  10000000LL,
  100000000LL,
  1000000000LL,
  10000000000LL,
  100000000000LL,
  1000000000000LL,
  10000000000000LL,
  100000000000000LL,
  1000000000000000LL,
  10000000000000000LL,
  100000000000000000LL,
  1000000000000000000LL,
};

static NSString *resultOrSideEffect(JreStringBuilder *sb, NSString *s) {
  if (sb) {
    JreStringBuilder_appendString(sb, s);
    return nil;
  }
  return s;
}

NSString *RealToString_convertDouble(JreStringBuilder *sb, double inputNumber) {
  long long inputNumberBits = *(long long *)&inputNumber;
  BOOL positive = (inputNumberBits & JavaLangDouble_SIGN_MASK) == 0;
  int e = (inputNumberBits & JavaLangDouble_EXPONENT_MASK) >> JavaLangDouble_MANTISSA_BITS;
  long long f = inputNumberBits & JavaLangDouble_MANTISSA_MASK;
  BOOL mantissaIsZero = f == 0;

  NSString *quickResult = nil;
  if (e == 2047) {
    if (mantissaIsZero) {
      quickResult = positive ? @"Infinity" : @"-Infinity";
    } else {
      quickResult = @"NaN";
    }
  } else if (e == 0) {
    if (mantissaIsZero) {
      quickResult = positive ? @"0.0" : @"-0.0";
    } else if (f == 1) {
      // special case to increase precision even though 2 * Double.MIN_VALUE is 1.0e-323
      quickResult = positive ? @"4.9E-324" : @"-4.9E-324";
    }
  }
  if (quickResult) {
    return resultOrSideEffect(sb, quickResult);
  }

  // the power offset (precision)
  int p = JavaLangDouble_EXPONENT_BIAS + JavaLangDouble_MANTISSA_BITS;
  int pow;
  int numBits = JavaLangDouble_MANTISSA_BITS;
  if (e == 0) {
    pow = 1 - p; // a denormalized number
    long long ff = f;
    while ((ff & 0x0010000000000000LL) == 0) {
      ff = (uint64_t)ff << 1;
      numBits--;
    }
  } else {
    // 0 < e < 2047
    // a "normalized" number
    f = f | 0x0010000000000000LL;
    pow = e - p;
  }

  int firstK = 0, digitCount = 0;
  char digits[DIGITS_SIZE];
  if ((-59 < pow && pow < 6) || (pow == -59 && !mantissaIsZero)) {
    longDigitGenerator(f, pow, e == 0, mantissaIsZero, numBits, &firstK, digits, &digitCount);
  } else {
    bigIntDigitGenerator(f, pow, e == 0, numBits, &firstK, digits, &digitCount);
  }
  JreStringBuilder localSb;
  JreStringBuilder *dst = sb;
  if (!dst) {
    dst = &localSb;
    JreStringBuilder_initWithCapacity(dst, 26);
  }
  if (inputNumber >= 1e7 || inputNumber <= -1e7 || (inputNumber > -1e-3 && inputNumber < 1e-3)) {
    freeFormatExponential(dst, positive, firstK, digits, digitCount);
  } else {
    freeFormat(dst, positive, firstK, digits, digitCount);
  }
  if (sb) {
    return nil;
  } else {
    NSString *result = JreStringBuilder_toString(dst);
    free(dst->buffer_);
    return result;
  }
}

NSString *RealToString_convertFloat(JreStringBuilder *sb, float inputNumber) {
  int inputNumberBits = *(int *)&inputNumber;
  BOOL positive = (inputNumberBits & JavaLangFloat_SIGN_MASK) == 0;
  int e = (inputNumberBits & JavaLangFloat_EXPONENT_MASK) >> JavaLangFloat_MANTISSA_BITS;
  int f = inputNumberBits & JavaLangFloat_MANTISSA_MASK;
  BOOL mantissaIsZero = f == 0;

  NSString *quickResult = nil;
  if (e == 255) {
    if (mantissaIsZero) {
      quickResult = positive ? @"Infinity" : @"-Infinity";
    } else {
      quickResult = @"NaN";
    }
  } else if (e == 0 && mantissaIsZero) {
    quickResult = positive ? @"0.0" : @"-0.0";
  }
  if (quickResult) {
    return resultOrSideEffect(sb, quickResult);
  }

  // the power offset (precision)
  int p = JavaLangFloat_EXPONENT_BIAS + JavaLangFloat_MANTISSA_BITS;
  int pow;
  int numBits = JavaLangFloat_MANTISSA_BITS;
  if (e == 0) {
    pow = 1 - p; // a denormalized number
    if (f < 8) { // want more precision with smallest values
      f = f << 2;
      pow -= 2;
    }
    int ff = f;
    while ((ff & 0x00800000) == 0) {
      ff = ff << 1;
      numBits--;
    }
  } else {
    // 0 < e < 255
    // a "normalized" number
    f = f | 0x00800000;
    pow = e - p;
  }

  int firstK = 0, digitCount = 0;
  char digits[DIGITS_SIZE];
  if ((-59 < pow && pow < 35) || (pow == -59 && !mantissaIsZero)) {
    longDigitGenerator(f, pow, e == 0, mantissaIsZero, numBits, &firstK, digits, &digitCount);
  } else {
    bigIntDigitGenerator(f, pow, e == 0, numBits, &firstK, digits, &digitCount);
  }
  JreStringBuilder localSb;
  JreStringBuilder *dst = sb;
  if (!dst) {
    dst = &localSb;
    JreStringBuilder_initWithCapacity(dst, 26);
  }
  if (inputNumber >= 1e7f || inputNumber <= -1e7f
      || (inputNumber > -1e-3f && inputNumber < 1e-3f)) {
    freeFormatExponential(dst, positive, firstK, digits, digitCount);
  } else {
    freeFormat(dst, positive, firstK, digits, digitCount);
  }
  if (sb) {
    return nil;
  } else {
    NSString *result = JreStringBuilder_toString(dst);
    free(dst->buffer_);
    return result;
  }
}

void freeFormatExponential(
    JreStringBuilder *sb, BOOL positive, int k, const char digits[], int digitCount) {
  int digitIndex = 0;
  if (!positive) {
    JreStringBuilder_appendChar(sb, '-');
  }
  JreStringBuilder_appendChar(sb, '0' + digits[digitIndex++]);
  JreStringBuilder_appendChar(sb, '.');

  int exponent = k;
  while (YES) {
    k--;
    if (digitIndex >= digitCount) {
      break;
    }
    JreStringBuilder_appendChar(sb, '0' + digits[digitIndex++]);
  }

  if (k == exponent - 1) {
    JreStringBuilder_appendChar(sb, '0');
  }
  JreStringBuilder_appendChar(sb, 'E');
  IntegralToString_convertInt(sb, exponent);
}

void freeFormat(JreStringBuilder *sb, BOOL positive, int k, const char digits[], int digitCount) {
  int digitIndex = 0;
  if (!positive) {
    JreStringBuilder_appendChar(sb, '-');
  }
  if (k < 0) {
    JreStringBuilder_appendChar(sb, '0');
    JreStringBuilder_appendChar(sb, '.');
    for (int i = k + 1; i < 0; ++i) {
      JreStringBuilder_appendChar(sb, '0');
    }
  }
  int U = digits[digitIndex++];
  do {
    if (U != -1) {
      JreStringBuilder_appendChar(sb, '0' + U);
    } else if (k >= -1) {
      JreStringBuilder_appendChar(sb, '0');
    }
    if (k == 0) {
      JreStringBuilder_appendChar(sb, '.');
    }
    k--;
    U = digitIndex < digitCount ? digits[digitIndex++] : -1;
  } while (U != -1 || k >= -1);
}

/* The algorithm for this particular function can be found in:
 *
 *      Printing Floating-Point Numbers Quickly and Accurately, Robert
 *      G. Burger, and R. Kent Dybvig, Programming Language Design and
 *      Implementation (PLDI) 1996, pp.108-116.
 *
 * The previous implementation of this function combined m+ and m- into
 * one single M which caused some inaccuracy of the last digit. The
 * particular case below shows this inaccuracy:
 *
 *       System.out.println(new Double((1.234123412431233E107)).toString());
 *       System.out.println(new Double((1.2341234124312331E107)).toString());
 *       System.out.println(new Double((1.2341234124312332E107)).toString());
 *
 *       outputs the following:
 *
 *           1.234123412431233E107
 *           1.234123412431233E107
 *           1.234123412431233E107
 *
 *       instead of:
 *
 *           1.234123412431233E107
 *           1.2341234124312331E107
 *           1.2341234124312331E107
 *
 */
void bigIntDigitGenerator(
    long long int f, int e, BOOL isDenormalized, int p, int *firstK, char digits[],
    int *digitCount) {
  int RLength, SLength, TempLength, mplus_Length, mminus_Length;
  int high, low, i;
  jint k, U;

  uint64_t R[RM_SIZE], S[STemp_SIZE], mplus[RM_SIZE], mminus[RM_SIZE], Temp[STemp_SIZE];

  memset (R     , 0, RM_SIZE    * sizeof (uint64_t));
  memset (S     , 0, STemp_SIZE * sizeof (uint64_t));
  memset (mplus , 0, RM_SIZE    * sizeof (uint64_t));
  memset (mminus, 0, RM_SIZE    * sizeof (uint64_t));
  memset (Temp  , 0, STemp_SIZE * sizeof (uint64_t));

  if (e >= 0)
    {
      *R = f;
      *mplus = *mminus = 1;
      simpleShiftLeftHighPrecision (mminus, RM_SIZE, e);
      if (f != (2 << (p - 1)))
        {
          simpleShiftLeftHighPrecision (R, RM_SIZE, e + 1);
          *S = 2;
          /*
           * m+ = m+ << e results in 1.0e23 to be printed as
           * 0.9999999999999999E23
           * m+ = m+ << e+1 results in 1.0e23 to be printed as
           * 1.0e23 (caused too much rounding)
           *      470fffffffffffff = 2.0769187434139308E34
           *      4710000000000000 = 2.076918743413931E34
           */
          simpleShiftLeftHighPrecision (mplus, RM_SIZE, e);
        }
      else
        {
          simpleShiftLeftHighPrecision (R, RM_SIZE, e + 2);
          *S = 4;
          simpleShiftLeftHighPrecision (mplus, RM_SIZE, e + 1);
        }
    }
  else
    {
      if (isDenormalized || (f != (2 << (p - 1))))
        {
          *R = f << 1;
          *S = 1;
          simpleShiftLeftHighPrecision (S, STemp_SIZE, 1 - e);
          *mplus = *mminus = 1;
        }
      else
        {
          *R = f << 2;
          *S = 1;
          simpleShiftLeftHighPrecision (S, STemp_SIZE, 2 - e);
          *mplus = 2;
          *mminus = 1;
        }
    }

  k = (int) (ceil ((e + p - 1) * INV_LOG_OF_TEN_BASE_2 - 1e-10));

  if (k > 0)
    {
      timesTenToTheEHighPrecision (S, STemp_SIZE, k);
    }
  else
    {
      timesTenToTheEHighPrecision (R     , RM_SIZE, -k);
      timesTenToTheEHighPrecision (mplus , RM_SIZE, -k);
      timesTenToTheEHighPrecision (mminus, RM_SIZE, -k);
    }

  RLength = mplus_Length = mminus_Length = RM_SIZE;
  SLength = TempLength = STemp_SIZE;

  memset (Temp + RM_SIZE, 0, (STemp_SIZE - RM_SIZE) * sizeof (uint64_t));
  memcpy (Temp, R, RM_SIZE * sizeof (uint64_t));

  while (RLength > 1 && R[RLength - 1] == 0)
    --RLength;
  while (mplus_Length > 1 && mplus[mplus_Length - 1] == 0)
    --mplus_Length;
  while (mminus_Length > 1 && mminus[mminus_Length - 1] == 0)
    --mminus_Length;
  while (SLength > 1 && S[SLength - 1] == 0)
    --SLength;
  TempLength = (RLength > mplus_Length ? RLength : mplus_Length) + 1;
  addHighPrecision (Temp, TempLength, mplus, mplus_Length);

  if (compareHighPrecision (Temp, TempLength, S, SLength) >= 0)
    {
      *firstK = k;
    }
  else
    {
      *firstK = k - 1;
      simpleAppendDecimalDigitHighPrecision (R     , ++RLength      , 0);
      simpleAppendDecimalDigitHighPrecision (mplus , ++mplus_Length , 0);
      simpleAppendDecimalDigitHighPrecision (mminus, ++mminus_Length, 0);
      while (RLength > 1 && R[RLength - 1] == 0)
        --RLength;
      while (mplus_Length > 1 && mplus[mplus_Length - 1] == 0)
        --mplus_Length;
      while (mminus_Length > 1 && mminus[mminus_Length - 1] == 0)
        --mminus_Length;
    }

  do
    {
      U = 0;
      for (i = 3; i >= 0; --i)
        {
          TempLength = SLength + 1;
          Temp[SLength] = 0;
          memcpy (Temp, S, SLength * sizeof (uint64_t));
          simpleShiftLeftHighPrecision (Temp, TempLength, i);
          if (compareHighPrecision (R, RLength, Temp, TempLength) >= 0)
            {
              subtractHighPrecision (R, RLength, Temp, TempLength);
              U += 1 << i;
            }
        }

      low = compareHighPrecision (R, RLength, mminus, mminus_Length) <= 0;

      memset (Temp + RLength, 0, (STemp_SIZE - RLength) * sizeof (uint64_t));
      memcpy (Temp, R, RLength * sizeof (uint64_t));
      TempLength = (RLength > mplus_Length ? RLength : mplus_Length) + 1;
      addHighPrecision (Temp, TempLength, mplus, mplus_Length);

      high = compareHighPrecision (Temp, TempLength, S, SLength) >= 0;

      if (low || high)
        break;

      simpleAppendDecimalDigitHighPrecision (R     , ++RLength      , 0);
      simpleAppendDecimalDigitHighPrecision (mplus , ++mplus_Length , 0);
      simpleAppendDecimalDigitHighPrecision (mminus, ++mminus_Length, 0);
      while (RLength > 1 && R[RLength - 1] == 0)
        --RLength;
      while (mplus_Length > 1 && mplus[mplus_Length - 1] == 0)
        --mplus_Length;
      while (mminus_Length > 1 && mminus[mminus_Length - 1] == 0)
        --mminus_Length;
      digits[(*digitCount)++] = U;
      if (*digitCount >= DIGITS_SIZE) {
        // Should only happen if there is a bug in the above, since digits is
        // set to hold any valid double.
        NSString *msg =
            [NSString stringWithFormat:@"maximum digits length exceeded: %d", *digitCount];
        @throw AUTORELEASE([[JavaLangAssertionError alloc] initWithNSString:msg]);
      }
    }
  while (1);

  simpleShiftLeftHighPrecision (R, ++RLength, 1);
  if (low && !high)
    digits[(*digitCount)++] = U;
  else if (high && !low)
    digits[(*digitCount)++] = U + 1;
  else if (compareHighPrecision (R, RLength, S, SLength) < 0)
    digits[(*digitCount)++] = U;
  else
    digits[(*digitCount)++] = U + 1;
}

void longDigitGenerator(
    long long f, int e, BOOL isDenormalized, BOOL mantissaIsZero, int p, int *firstK, char digits[],
    int *digitCount) {
  long long R, S, M;
  if (e >= 0) {
    M = (uint64_t)1l << e;
    if (!mantissaIsZero) {
      R = (uint64_t)f << (e + 1);
      S = 2;
    } else {
      R = (uint64_t)f << (e + 2);
      S = 4;
    }
  } else {
    M = 1;
    if (isDenormalized || !mantissaIsZero) {
      R = (uint64_t)f << 1;
      S = (uint64_t)1l << (1 - e);
    } else {
      R = (uint64_t)f << 2;
      S = (uint64_t)1l << (2 - e);
    }
  }

  int k = (int) ceil((e + p - 1) * INV_LOG_OF_TEN_BASE_2 - 1e-10);

  if (k > 0) {
    S = S * LONG_POWERS_OF_TEN[k];
  } else if (k < 0) {
    long long scale = LONG_POWERS_OF_TEN[-k];
    R = R * scale;
    M = M == 1 ? scale : M * scale;
  }

  if (R + M > S) { // was M_plus
    *firstK = k;
  } else {
    *firstK = k - 1;
    R = R * 10;
    M = M * 10;
  }

  BOOL low, high;
  int U;
  while (YES) {
    // Set U to floor(R/S) and R to the remainder, using *unsigned* 64-bit division
    U = 0;
    for (int i = 3; i >= 0; i--) {
      long long remainder = R - ((uint64_t)S << i);
      if (remainder >= 0) {
        R = remainder;
        U += 1 << i;
      }
    }

    low = R < M; // was M_minus
    high = R + M > S; // was M_plus

    if (low || high) {
      break;
    }
    R = R * 10;
    M = M * 10;
    digits[(*digitCount)++] = U;
  }
  if (low && !high) {
    digits[(*digitCount)++] = U;
  } else if (high && !low) {
    digits[(*digitCount)++] = U + 1;
  } else if ((R << 1) < S) {
    digits[(*digitCount)++] = U;
  } else {
    digits[(*digitCount)++] = U + 1;
  }
}
