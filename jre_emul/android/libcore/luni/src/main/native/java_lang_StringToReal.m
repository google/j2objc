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

// Modified version of Android's java_lang_StringToReal.cpp, converted
// to not use JNI calling convention.

#include <stdlib.h>
#include <string.h>
#include <math.h>
#include "cbigint.h"
#include "java/lang/OutOfMemoryError.h"

/* ************************* Defines ************************* */
#if defined(__linux__) || defined(__APPLE__)
#define USE_LL
#endif

#define LOW_I32_FROM_VAR(u64)     LOW_I32_FROM_LONG64(u64)
#define LOW_I32_FROM_PTR(u64ptr)  LOW_I32_FROM_LONG64_PTR(u64ptr)
#define HIGH_I32_FROM_VAR(u64)    HIGH_I32_FROM_LONG64(u64)
#define HIGH_I32_FROM_PTR(u64ptr) HIGH_I32_FROM_LONG64_PTR(u64ptr)

#define MAX_DOUBLE_ACCURACY_WIDTH 17

#define DEFAULT_DOUBLE_WIDTH MAX_DOUBLE_ACCURACY_WIDTH

#if defined(USE_LL)
#define DOUBLE_INFINITE_LONGBITS (0x7FF0000000000000LL)
#else
#if defined(USE_L)
#define DOUBLE_INFINITE_LONGBITS (0x7FF0000000000000L)
#else
#define DOUBLE_INFINITE_LONGBITS (0x7FF0000000000000)
#endif /* USE_L */
#endif /* USE_LL */

#define DOUBLE_MINIMUM_LONGBITS (0x1)

#if defined(USE_LL)
#define DOUBLE_MANTISSA_MASK (0x000FFFFFFFFFFFFFLL)
#define DOUBLE_EXPONENT_MASK (0x7FF0000000000000LL)
#define DOUBLE_NORMAL_MASK   (0x0010000000000000LL)
#else
#if defined(USE_L)
#define DOUBLE_MANTISSA_MASK (0x000FFFFFFFFFFFFFL)
#define DOUBLE_EXPONENT_MASK (0x7FF0000000000000L)
#define DOUBLE_NORMAL_MASK   (0x0010000000000000L)
#else
#define DOUBLE_MANTISSA_MASK (0x000FFFFFFFFFFFFF)
#define DOUBLE_EXPONENT_MASK (0x7FF0000000000000)
#define DOUBLE_NORMAL_MASK   (0x0010000000000000)
#endif /* USE_L */
#endif /* USE_LL */

/* Keep a count of the number of times we decrement and increment to
 * approximate the double, and attempt to detect the case where we
 * could potentially toggle back and forth between decrementing and
 * incrementing. It is possible for us to be stuck in the loop when
 * incrementing by one or decrementing by one may exceed or stay below
 * the value that we are looking for. In this case, just break out of
 * the loop if we toggle between incrementing and decrementing for more
 * than twice.
 */
#define INCREMENT_DOUBLE(_x, _decCount, _incCount) \
    { \
        ++DOUBLE_TO_LONGBITS(_x); \
        _incCount++; \
        if( (_incCount > 2) && (_decCount > 2) ) { \
            if( _decCount > _incCount ) { \
                DOUBLE_TO_LONGBITS(_x) += _decCount - _incCount; \
            } else if( _incCount > _decCount ) { \
                DOUBLE_TO_LONGBITS(_x) -= _incCount - _decCount; \
            } \
            break; \
        } \
    }
#define DECREMENT_DOUBLE(_x, _decCount, _incCount) \
    { \
        --DOUBLE_TO_LONGBITS(_x); \
        _decCount++; \
        if( (_incCount > 2) && (_decCount > 2) ) { \
            if( _decCount > _incCount ) { \
                DOUBLE_TO_LONGBITS(_x) += _decCount - _incCount; \
            } else if( _incCount > _decCount ) { \
                DOUBLE_TO_LONGBITS(_x) -= _incCount - _decCount; \
            } \
            break; \
        } \
    }

#define allocateU64(x, n) if (!((x) = (uint64_t*)malloc((n) * sizeof(uint64_t)))) goto OutOfMemory;

/* *********************************************************** */

/* ************************ local data ************************ */
static const jdouble double_tens[] = {
  1.0,
  1.0e1,
  1.0e2,
  1.0e3,
  1.0e4,
  1.0e5,
  1.0e6,
  1.0e7,
  1.0e8,
  1.0e9,
  1.0e10,
  1.0e11,
  1.0e12,
  1.0e13,
  1.0e14,
  1.0e15,
  1.0e16,
  1.0e17,
  1.0e18,
  1.0e19,
  1.0e20,
  1.0e21,
  1.0e22
};
/* *********************************************************** */

/* ************** private function declarations ************** */
static jdouble createDouble1   (uint64_t * f, int32_t length, jint e);
static jdouble doubleAlgorithm (uint64_t * f, int32_t length, jint e,
                                jdouble z);
/* *********************************************************** */

#define doubleTenToTheE(e) (*(double_tens + (e)))
#define DOUBLE_LOG5_OF_TWO_TO_THE_N 23

#define sizeOfTenToTheE(e) (((e) / 19) + 1)

static jdouble createDouble(const char* s, jint e) {
  /* assumes s is a null terminated string with at least one
   * character in it */
  uint64_t def[DEFAULT_DOUBLE_WIDTH];
  uint64_t defBackup[DEFAULT_DOUBLE_WIDTH];
  uint64_t* f;
  uint64_t* fNoOverflow;
  uint64_t* g;
  uint64_t* tempBackup;
  uint32_t overflow;
  jdouble result;
  int32_t index = 1;
  int unprocessedDigits = 0;

  f = def;
  fNoOverflow = defBackup;
  *f = 0;
  tempBackup = g = 0;
  do
    {
      if (*s >= '0' && *s <= '9')
        {
          /* Make a back up of f before appending, so that we can
           * back out of it if there is no more room, i.e. index >
           * MAX_DOUBLE_ACCURACY_WIDTH.
           */
          memcpy (fNoOverflow, f, sizeof (uint64_t) * index);
          overflow =
            simpleAppendDecimalDigitHighPrecision (f, index, *s - '0');
          if (overflow)
            {
              f[index++] = overflow;
              /* There is an overflow, but there is no more room
               * to store the result. We really only need the top 52
               * bits anyway, so we must back out of the overflow,
               * and ignore the rest of the string.
               */
              if (index >= MAX_DOUBLE_ACCURACY_WIDTH)
                {
                  index--;
                  memcpy (f, fNoOverflow, sizeof (uint64_t) * index);
                  break;
                }
              if (tempBackup)
                {
                  fNoOverflow = tempBackup;
                }
            }
        }
      else
        index = -1;
    }
  while (index > 0 && *(++s) != '\0');

  /* We've broken out of the parse loop either because we've reached
   * the end of the string or we've overflowed the maximum accuracy
   * limit of a double. If we still have unprocessed digits in the
   * given string, then there are three possible results:
   *   1. (unprocessed digits + e) == 0, in which case we simply
   *      convert the existing bits that are already parsed
   *   2. (unprocessed digits + e) < 0, in which case we simply
   *      convert the existing bits that are already parsed along
   *      with the given e
   *   3. (unprocessed digits + e) > 0 indicates that the value is
   *      simply too big to be stored as a double, so return Infinity
   */
  if ((unprocessedDigits = (int) strlen (s)) > 0)
    {
      e += unprocessedDigits;
      if (index > -1)
        {
          if (e == 0)
            result = toDoubleHighPrecision (f, index);
          else if (e < 0)
            result = createDouble1 (f, index, e);
          else
            {
              DOUBLE_TO_LONGBITS (result) = DOUBLE_INFINITE_LONGBITS;
            }
        }
      else
        {
          LOW_I32_FROM_VAR  (result) = -1;
          HIGH_I32_FROM_VAR (result) = -1;
        }
    }
  else
    {
      if (index > -1)
        {
          if (e == 0)
            result = toDoubleHighPrecision (f, index);
          else
            result = createDouble1 (f, index, e);
        }
      else
        {
          LOW_I32_FROM_VAR  (result) = -1;
          HIGH_I32_FROM_VAR (result) = -1;
        }
    }

  return result;

}

static jdouble createDouble1(uint64_t* f, int32_t length, jint e) {
  int32_t numBits;
  jdouble result;

  static const jint APPROX_MIN_MAGNITUDE = -309;
  static const jint APPROX_MAX_MAGNITUDE = 309;

  numBits = highestSetBitHighPrecision (f, length) + 1;
  numBits -= lowestSetBitHighPrecision (f, length);
  if (numBits < 54 && e >= 0 && e < DOUBLE_LOG5_OF_TWO_TO_THE_N)
    {
      return toDoubleHighPrecision (f, length) * doubleTenToTheE (e);
    }
  else if (numBits < 54 && e < 0 && (-e) < DOUBLE_LOG5_OF_TWO_TO_THE_N)
    {
      return toDoubleHighPrecision (f, length) / doubleTenToTheE (-e);
    }
  else if (e >= 0 && e < APPROX_MAX_MAGNITUDE)
    {
      result = toDoubleHighPrecision (f, length) * pow (10.0, e);
    }
  else if (e >= APPROX_MAX_MAGNITUDE)
    {
      /* Convert the partial result to make sure that the
       * non-exponential part is not zero. This check fixes the case
       * where the user enters 0.0e309! */
      result = toDoubleHighPrecision (f, length);
      /* Don't go straight to zero as the fact that x*0 = 0 independent of x might
         cause the algorithm to produce an incorrect result.  Instead try the min value
         first and let it fall to zero if need be. */

      if (result == 0.0)
        {
          DOUBLE_TO_LONGBITS (result) = DOUBLE_MINIMUM_LONGBITS;
        }
      else
        {
          DOUBLE_TO_LONGBITS (result) = DOUBLE_INFINITE_LONGBITS;
          return result;
        }
    }
  else if (e > APPROX_MIN_MAGNITUDE)
    {
      result = toDoubleHighPrecision (f, length) / pow (10.0, -e);
    }

  if (e <= APPROX_MIN_MAGNITUDE)
    {

      result = toDoubleHighPrecision (f, length) * pow (10.0, e + 52);
      result = result * pow (10.0, -52);

    }

  /* Don't go straight to zero as the fact that x*0 = 0 independent of x might
     cause the algorithm to produce an incorrect result.  Instead try the min value
     first and let it fall to zero if need be. */

  if (result == 0.0)

    DOUBLE_TO_LONGBITS (result) = DOUBLE_MINIMUM_LONGBITS;

  return doubleAlgorithm (f, length, e, result);
}

/* The algorithm for the function doubleAlgorithm() below can be found
 * in:
 *
 *      "How to Read Floating-Point Numbers Accurately", William D.
 *      Clinger, Proceedings of the ACM SIGPLAN '90 Conference on
 *      Programming Language Design and Implementation, June 20-22,
 *      1990, pp. 92-101.
 *
 * There is a possibility that the function will end up in an endless
 * loop if the given approximating floating-point number (a very small
 * floating-point whose value is very close to zero) straddles between
 * two approximating integer values. We modified the algorithm slightly
 * to detect the case where it oscillates back and forth between
 * incrementing and decrementing the floating-point approximation. It
 * is currently set such that if the oscillation occurs more than twice
 * then return the original approximation.
 */
static jdouble doubleAlgorithm(uint64_t* f, int32_t length, jint e, jdouble z) {
  uint64_t m;
  int32_t k, comparison, comparison2;
  uint64_t* x;
  uint64_t* y;
  uint64_t* D;
  uint64_t* D2;
  int32_t xLength, yLength, DLength, D2Length, decApproxCount, incApproxCount;

  x = y = D = D2 = 0;
  xLength = yLength = DLength = D2Length = 0;
  decApproxCount = incApproxCount = 0;

  do
    {
      m = doubleMantissa (z);
      k = doubleExponent (z);

      if (x && x != f)
          free(x);

      free(y);
      free(D);
      free(D2);

      if (e >= 0 && k >= 0)
        {
          xLength = sizeOfTenToTheE (e) + length;
          allocateU64 (x, xLength);
          memset (x + length, 0, sizeof (uint64_t) * (xLength - length));
          memcpy (x, f, sizeof (uint64_t) * length);
          timesTenToTheEHighPrecision (x, xLength, e);

          yLength = (k >> 6) + 2;
          allocateU64 (y, yLength);
          memset (y + 1, 0, sizeof (uint64_t) * (yLength - 1));
          *y = m;
          simpleShiftLeftHighPrecision (y, yLength, k);
        }
      else if (e >= 0)
        {
          xLength = sizeOfTenToTheE (e) + length + ((-k) >> 6) + 1;
          allocateU64 (x, xLength);
          memset (x + length, 0, sizeof (uint64_t) * (xLength - length));
          memcpy (x, f, sizeof (uint64_t) * length);
          timesTenToTheEHighPrecision (x, xLength, e);
          simpleShiftLeftHighPrecision (x, xLength, -k);

          yLength = 1;
          allocateU64 (y, 1);
          *y = m;
        }
      else if (k >= 0)
        {
          xLength = length;
          x = f;

          yLength = sizeOfTenToTheE (-e) + 2 + (k >> 6);
          allocateU64 (y, yLength);
          memset (y + 1, 0, sizeof (uint64_t) * (yLength - 1));
          *y = m;
          timesTenToTheEHighPrecision (y, yLength, -e);
          simpleShiftLeftHighPrecision (y, yLength, k);
        }
      else
        {
          xLength = length + ((-k) >> 6) + 1;
          allocateU64 (x, xLength);
          memset (x + length, 0, sizeof (uint64_t) * (xLength - length));
          memcpy (x, f, sizeof (uint64_t) * length);
          simpleShiftLeftHighPrecision (x, xLength, -k);

          yLength = sizeOfTenToTheE (-e) + 1;
          allocateU64 (y, yLength);
          memset (y + 1, 0, sizeof (uint64_t) * (yLength - 1));
          *y = m;
          timesTenToTheEHighPrecision (y, yLength, -e);
        }

      comparison = compareHighPrecision (x, xLength, y, yLength);
      if (comparison > 0)
        {                       /* x > y */
          DLength = xLength;
          allocateU64 (D, DLength);
          memcpy (D, x, DLength * sizeof (uint64_t));
          subtractHighPrecision (D, DLength, y, yLength);
        }
      else if (comparison)
        {                       /* y > x */
          DLength = yLength;
          allocateU64 (D, DLength);
          memcpy (D, y, DLength * sizeof (uint64_t));
          subtractHighPrecision (D, DLength, x, xLength);
        }
      else
        {                       /* y == x */
          DLength = 1;
          allocateU64 (D, 1);
          *D = 0;
        }

      D2Length = DLength + 1;
      allocateU64 (D2, D2Length);
      m <<= 1;
      multiplyHighPrecision (D, DLength, &m, 1, D2, D2Length);
      m >>= 1;

      comparison2 = compareHighPrecision (D2, D2Length, y, yLength);
      if (comparison2 < 0)
        {
          if (comparison < 0 && m == DOUBLE_NORMAL_MASK)
            {
              simpleShiftLeftHighPrecision (D2, D2Length, 1);
              if (compareHighPrecision (D2, D2Length, y, yLength) > 0)
                {
                  DECREMENT_DOUBLE (z, decApproxCount, incApproxCount);
                }
              else
                {
                  break;
                }
            }
          else
            {
              break;
            }
        }
      else if (comparison2 == 0)
        {
          if ((LOW_U32_FROM_VAR (m) & 1) == 0)
            {
              if (comparison < 0 && m == DOUBLE_NORMAL_MASK)
                {
                  DECREMENT_DOUBLE (z, decApproxCount, incApproxCount);
                }
              else
                {
                  break;
                }
            }
          else if (comparison < 0)
            {
              DECREMENT_DOUBLE (z, decApproxCount, incApproxCount);
              break;
            }
          else
            {
              INCREMENT_DOUBLE (z, decApproxCount, incApproxCount);
              break;
            }
        }
      else if (comparison < 0)
        {
          DECREMENT_DOUBLE (z, decApproxCount, incApproxCount);
        }
      else
        {
          if (DOUBLE_TO_LONGBITS (z) == DOUBLE_INFINITE_LONGBITS)
            break;
          INCREMENT_DOUBLE (z, decApproxCount, incApproxCount);
        }
    }
  while (1);

  if (x && x != f)
     free(x);
  free(y);
  free(D);
  free(D2);
  return z;

OutOfMemory:
  if (x && x != f)
      free(x);
  free(y);
  free(D);
  free(D2);
  @throw [[[JavaLangOutOfMemoryError alloc] init] autorelease];
  return z;
}



#define MAX_FLOAT_ACCURACY_WIDTH 8

#define DEFAULT_FLOAT_WIDTH MAX_FLOAT_ACCURACY_WIDTH

static jfloat createFloat1(uint64_t* f, int32_t length, jint e);
static jfloat floatAlgorithm(uint64_t* f, int32_t length, jint e, jfloat z);

static const uint32_t float_tens[] = {
  0x3f800000,
  0x41200000,
  0x42c80000,
  0x447a0000,
  0x461c4000,
  0x47c35000,
  0x49742400,
  0x4b189680,
  0x4cbebc20,
  0x4e6e6b28,
  0x501502f9                    /* 10 ^ 10 in float */
};

#define floatTenToTheE(e) (*(const jfloat*)(float_tens + (e)))
#define FLOAT_LOG5_OF_TWO_TO_THE_N 11

#define FLOAT_INFINITE_INTBITS (0x7F800000)
#define FLOAT_MINIMUM_INTBITS (1)

#define FLOAT_MANTISSA_MASK (0x007FFFFF)
#define FLOAT_EXPONENT_MASK (0x7F800000)
#define FLOAT_NORMAL_MASK   (0x00800000)

/* Keep a count of the number of times we decrement and increment to
 * approximate the double, and attempt to detect the case where we
 * could potentially toggle back and forth between decrementing and
 * incrementing. It is possible for us to be stuck in the loop when
 * incrementing by one or decrementing by one may exceed or stay below
 * the value that we are looking for. In this case, just break out of
 * the loop if we toggle between incrementing and decrementing for more
 * than twice.
 */
#define INCREMENT_FLOAT(_x, _decCount, _incCount) \
    { \
        ++FLOAT_TO_INTBITS(_x); \
        _incCount++; \
        if( (_incCount > 2) && (_decCount > 2) ) { \
            if( _decCount > _incCount ) { \
                FLOAT_TO_INTBITS(_x) += _decCount - _incCount; \
            } else if( _incCount > _decCount ) { \
                FLOAT_TO_INTBITS(_x) -= _incCount - _decCount; \
            } \
            break; \
        } \
    }
#define DECREMENT_FLOAT(_x, _decCount, _incCount) \
    { \
        --FLOAT_TO_INTBITS(_x); \
        _decCount++; \
        if( (_incCount > 2) && (_decCount > 2) ) { \
            if( _decCount > _incCount ) { \
                FLOAT_TO_INTBITS(_x) += _decCount - _incCount; \
            } else if( _incCount > _decCount ) { \
                FLOAT_TO_INTBITS(_x) -= _incCount - _decCount; \
            } \
            break; \
        } \
    }

static jfloat createFloat(const char* s, jint e) {
  /* assumes s is a null terminated string with at least one
   * character in it */
  uint64_t def[DEFAULT_FLOAT_WIDTH];
  uint64_t defBackup[DEFAULT_FLOAT_WIDTH];
  uint64_t* f;
  uint64_t* fNoOverflow;
  uint64_t* g;
  uint64_t* tempBackup;
  uint32_t overflow;
  jfloat result;
  int32_t index = 1;
  int unprocessedDigits = 0;

  f = def;
  fNoOverflow = defBackup;
  *f = 0;
  tempBackup = g = 0;
  do
    {
      if (*s >= '0' && *s <= '9')
        {
          /* Make a back up of f before appending, so that we can
           * back out of it if there is no more room, i.e. index >
           * MAX_FLOAT_ACCURACY_WIDTH.
           */
          memcpy (fNoOverflow, f, sizeof (uint64_t) * index);
          overflow =
            simpleAppendDecimalDigitHighPrecision (f, index, *s - '0');
          if (overflow)
            {

              f[index++] = overflow;
              /* There is an overflow, but there is no more room
               * to store the result. We really only need the top 52
               * bits anyway, so we must back out of the overflow,
               * and ignore the rest of the string.
               */
              if (index >= MAX_FLOAT_ACCURACY_WIDTH)
                {
                  index--;
                  memcpy (f, fNoOverflow, sizeof (uint64_t) * index);
                  break;
                }
              if (tempBackup)
                {
                  fNoOverflow = tempBackup;
                }
            }
        }
      else
        index = -1;
    }
  while (index > 0 && *(++s) != '\0');

  /* We've broken out of the parse loop either because we've reached
   * the end of the string or we've overflowed the maximum accuracy
   * limit of a double. If we still have unprocessed digits in the
   * given string, then there are three possible results:
   *   1. (unprocessed digits + e) == 0, in which case we simply
   *      convert the existing bits that are already parsed
   *   2. (unprocessed digits + e) < 0, in which case we simply
   *      convert the existing bits that are already parsed along
   *      with the given e
   *   3. (unprocessed digits + e) > 0 indicates that the value is
   *      simply too big to be stored as a double, so return Infinity
   */
  if ((unprocessedDigits = (int) strlen (s)) > 0)
    {
      e += unprocessedDigits;
      if (index > -1)
        {
          if (e <= 0)
            {
              result = createFloat1 (f, index, e);
            }
          else
            {
              FLOAT_TO_INTBITS (result) = FLOAT_INFINITE_INTBITS;
            }
        }
      else
        {
          result = INTBITS_TO_FLOAT(index);
        }
    }
  else
    {
      if (index > -1)
        {
          result = createFloat1 (f, index, e);
        }
      else
        {
          result = INTBITS_TO_FLOAT(index);
        }
    }

  return result;

}

static jfloat createFloat1 (uint64_t* f, int32_t length, jint e) {
  int32_t numBits;
  jdouble dresult;
  jfloat result;

  numBits = highestSetBitHighPrecision (f, length) + 1;
  if (numBits < 25 && e >= 0 && e < FLOAT_LOG5_OF_TWO_TO_THE_N)
    {
      return ((jfloat) LOW_I32_FROM_PTR (f)) * floatTenToTheE (e);
    }
  else if (numBits < 25 && e < 0 && (-e) < FLOAT_LOG5_OF_TWO_TO_THE_N)
    {
      return ((jfloat) LOW_I32_FROM_PTR (f)) / floatTenToTheE (-e);
    }
  else if (e >= 0 && e < 39)
    {
      result = (jfloat) (toDoubleHighPrecision (f, length) * pow (10.0, (double) e));
    }
  else if (e >= 39)
    {
      /* Convert the partial result to make sure that the
       * non-exponential part is not zero. This check fixes the case
       * where the user enters 0.0e309! */
      result = (jfloat) toDoubleHighPrecision (f, length);

      if (result == 0.0)

        FLOAT_TO_INTBITS (result) = FLOAT_MINIMUM_INTBITS;
      else
        FLOAT_TO_INTBITS (result) = FLOAT_INFINITE_INTBITS;
    }
  else if (e > -309)
    {
      int dexp;
      uint32_t fmant, fovfl;
      uint64_t dmant;
      dresult = toDoubleHighPrecision (f, length) / pow (10.0, (double) -e);
      if (IS_DENORMAL_DBL (dresult))
        {
          FLOAT_TO_INTBITS (result) = 0;
          return result;
        }
      dexp = doubleExponent (dresult) + 51;
      dmant = doubleMantissa (dresult);
      /* Is it too small to be represented by a single-precision
       * float? */
      if (dexp <= -155)
        {
          FLOAT_TO_INTBITS (result) = 0;
          return result;
        }
      /* Is it a denormalized single-precision float? */
      if ((dexp <= -127) && (dexp > -155))
        {
          /* Only interested in 24 msb bits of the 53-bit double mantissa */
          fmant = (uint32_t) (dmant >> 29);
          fovfl = ((uint32_t) (dmant & 0x1FFFFFFF)) << 3;
          while ((dexp < -127) && ((fmant | fovfl) != 0))
            {
              if ((fmant & 1) != 0)
                {
                  fovfl |= 0x80000000;
                }
              fovfl >>= 1;
              fmant >>= 1;
              dexp++;
            }
          if ((fovfl & 0x80000000) != 0)
            {
              if ((fovfl & 0x7FFFFFFC) != 0)
                {
                  fmant++;
                }
              else if ((fmant & 1) != 0)
                {
                  fmant++;
                }
            }
          else if ((fovfl & 0x40000000) != 0)
            {
              if ((fovfl & 0x3FFFFFFC) != 0)
                {
                  fmant++;
                }
            }
          FLOAT_TO_INTBITS (result) = fmant;
        }
      else
        {
          result = (jfloat) dresult;
        }
    }

  /* Don't go straight to zero as the fact that x*0 = 0 independent
   * of x might cause the algorithm to produce an incorrect result.
   * Instead try the min  value first and let it fall to zero if need
   * be.
   */
  if (e <= -309 || FLOAT_TO_INTBITS (result) == 0)
    FLOAT_TO_INTBITS (result) = FLOAT_MINIMUM_INTBITS;

  return floatAlgorithm (f, length, e, (jfloat) result);
}

/* The algorithm for the function floatAlgorithm() below can be found
 * in:
 *
 *      "How to Read Floating-Point Numbers Accurately", William D.
 *      Clinger, Proceedings of the ACM SIGPLAN '90 Conference on
 *      Programming Language Design and Implementation, June 20-22,
 *      1990, pp. 92-101.
 *
 * There is a possibility that the function will end up in an endless
 * loop if the given approximating floating-point number (a very small
 * floating-point whose value is very close to zero) straddles between
 * two approximating integer values. We modified the algorithm slightly
 * to detect the case where it oscillates back and forth between
 * incrementing and decrementing the floating-point approximation. It
 * is currently set such that if the oscillation occurs more than twice
 * then return the original approximation.
 */
static jfloat floatAlgorithm(uint64_t* f, int32_t length, jint e, jfloat z) {
  uint64_t m;
  int32_t k, comparison, comparison2;
  uint64_t* x;
  uint64_t* y;
  uint64_t* D;
  uint64_t* D2;
  int32_t xLength, yLength, DLength, D2Length;
  int32_t decApproxCount, incApproxCount;

  x = y = D = D2 = 0;
  xLength = yLength = DLength = D2Length = 0;
  decApproxCount = incApproxCount = 0;

  do
    {
      m = floatMantissa (z);
      k = floatExponent (z);

      if (x && x != f)
          free(x);

      free(y);
      free(D);
      free(D2);

      if (e >= 0 && k >= 0)
        {
          xLength = sizeOfTenToTheE (e) + length;
          allocateU64 (x, xLength);
          memset (x + length, 0, sizeof (uint64_t) * (xLength - length));
          memcpy (x, f, sizeof (uint64_t) * length);
          timesTenToTheEHighPrecision (x, xLength, e);

          yLength = (k >> 6) + 2;
          allocateU64 (y, yLength);
          memset (y + 1, 0, sizeof (uint64_t) * (yLength - 1));
          *y = m;
          simpleShiftLeftHighPrecision (y, yLength, k);
        }
      else if (e >= 0)
        {
          xLength = sizeOfTenToTheE (e) + length + ((-k) >> 6) + 1;
          allocateU64 (x, xLength);
          memset (x + length, 0, sizeof (uint64_t) * (xLength - length));
          memcpy (x, f, sizeof (uint64_t) * length);
          timesTenToTheEHighPrecision (x, xLength, e);
          simpleShiftLeftHighPrecision (x, xLength, -k);

          yLength = 1;
          allocateU64 (y, 1);
          *y = m;
        }
      else if (k >= 0)
        {
          xLength = length;
          x = f;

          yLength = sizeOfTenToTheE (-e) + 2 + (k >> 6);
          allocateU64 (y, yLength);
          memset (y + 1, 0, sizeof (uint64_t) * (yLength - 1));
          *y = m;
          timesTenToTheEHighPrecision (y, yLength, -e);
          simpleShiftLeftHighPrecision (y, yLength, k);
        }
      else
        {
          xLength = length + ((-k) >> 6) + 1;
          allocateU64 (x, xLength);
          memset (x + length, 0, sizeof (uint64_t) * (xLength - length));
          memcpy (x, f, sizeof (uint64_t) * length);
          simpleShiftLeftHighPrecision (x, xLength, -k);

          yLength = sizeOfTenToTheE (-e) + 1;
          allocateU64 (y, yLength);
          memset (y + 1, 0, sizeof (uint64_t) * (yLength - 1));
          *y = m;
          timesTenToTheEHighPrecision (y, yLength, -e);
        }

      comparison = compareHighPrecision (x, xLength, y, yLength);
      if (comparison > 0)
        {                       /* x > y */
          DLength = xLength;
          allocateU64 (D, DLength);
          memcpy (D, x, DLength * sizeof (uint64_t));
          subtractHighPrecision (D, DLength, y, yLength);
        }
      else if (comparison)
        {                       /* y > x */
          DLength = yLength;
          allocateU64 (D, DLength);
          memcpy (D, y, DLength * sizeof (uint64_t));
          subtractHighPrecision (D, DLength, x, xLength);
        }
      else
        {                       /* y == x */
          DLength = 1;
          allocateU64 (D, 1);
          *D = 0;
        }

      D2Length = DLength + 1;
      allocateU64 (D2, D2Length);
      m <<= 1;
      multiplyHighPrecision (D, DLength, &m, 1, D2, D2Length);
      m >>= 1;

      comparison2 = compareHighPrecision (D2, D2Length, y, yLength);
      if (comparison2 < 0)
        {
          if (comparison < 0 && m == FLOAT_NORMAL_MASK)
            {
              simpleShiftLeftHighPrecision (D2, D2Length, 1);
              if (compareHighPrecision (D2, D2Length, y, yLength) > 0)
                {
                  DECREMENT_FLOAT (z, decApproxCount, incApproxCount);
                }
              else
                {
                  break;
                }
            }
          else
            {
              break;
            }
        }
      else if (comparison2 == 0)
        {
          if ((m & 1) == 0)
            {
              if (comparison < 0 && m == FLOAT_NORMAL_MASK)
                {
                  DECREMENT_FLOAT (z, decApproxCount, incApproxCount);
                }
              else
                {
                  break;
                }
            }
          else if (comparison < 0)
            {
              DECREMENT_FLOAT (z, decApproxCount, incApproxCount);
              break;
            }
          else
            {
              INCREMENT_FLOAT (z, decApproxCount, incApproxCount);
              break;
            }
        }
      else if (comparison < 0)
        {
          DECREMENT_FLOAT (z, decApproxCount, incApproxCount);
        }
      else
        {
          if (FLOAT_TO_INTBITS (z) == FLOAT_EXPONENT_MASK)
            break;
          INCREMENT_FLOAT (z, decApproxCount, incApproxCount);
        }
    }
  while (1);

  if (x && x != f)
      free(x);
  free(y);
  free(D);
  free(D2);
  return z;

OutOfMemory:
  if (x && x != f)
      free(x);
  free(y);
  free(D);
  free(D2);
  @throw [[[JavaLangOutOfMemoryError alloc] init] autorelease];
  return z;
}

jfloat JavaLangStringToReal_parseFltImplWithNSString_withInt_(NSString *s, jint e) {
    const char *str = [s UTF8String];
    if (!str) {
        return 0.0;
    }
    return createFloat(str, e);
}

jdouble JavaLangStringToReal_parseDblImplWithNSString_withInt_(NSString *s, jint e) {
    const char *str = [s UTF8String];
    if (!str) {
        return 0.0;
    }
    return createDouble(str, e);
}
