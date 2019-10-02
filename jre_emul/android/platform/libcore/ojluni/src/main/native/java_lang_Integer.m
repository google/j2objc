/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 1994, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

#include "java_lang_Integer.h"

#include "IOSPrimitiveArray.h"
#include "java/lang/Character.h"
#include "java/lang/Integer.h"
#include "jni.h"

/**
 * All possible chars for representing a number as a String
 */
const jchar JavaLangInteger_digits[] = {
  '0' , '1' , '2' , '3' , '4' , '5' ,
  '6' , '7' , '8' , '9' , 'a' , 'b' ,
  'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
  'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
  'o' , 'p' , 'q' , 'r' , 's' , 't' ,
  'u' , 'v' , 'w' , 'x' , 'y' , 'z'
};

static NSString *SMALL_NEG_VALUES[100];
static NSString *SMALL_NONNEG_VALUES[100];

const jchar JavaLangInteger_DigitTens[] = {
  '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
  '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
  '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
  '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
  '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
  '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
  '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
  '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
  '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
  '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
};

const jchar JavaLangInteger_DigitOnes[] = {
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

jstring Java_java_lang_Integer_toString__I(JNIEnv *env, jclass cls, jint i);

/**
 * Creates strings to be cached in SMALL_NONNEG_VALUES or SMALL_NEG_VALUES. The returned strings are
 * not autoreleased because they will live for the duration of the program.
 */
static NSString *StringOf1(unichar c) {
  return [[NSString alloc] initWithCharacters:&c length:1];
}

static NSString *StringOf2(unichar c1, unichar c2) {
  unichar buf[2];
  buf[0] = c1;
  buf[1] = c2;
  return [[NSString alloc] initWithCharacters:buf length:2];
}

static NSString *StringOf3(unichar c1, unichar c2, unichar c3) {
  unichar buf[3];
  buf[0] = c1;
  buf[1] = c2;
  buf[2] = c3;
  return [[NSString alloc] initWithCharacters:buf length:3];
}

jstring Java_java_lang_Integer_toString__II(JNIEnv *env, jclass cls, jint i, jint radix) {
  if (radix < JavaLangCharacter_MIN_RADIX || radix > JavaLangCharacter_MAX_RADIX)
    radix = 10;

  /* Use the faster version */
  if (radix == 10) {
    return Java_java_lang_Integer_toString__I(env, cls, i);
  }

  jchar buf[33];
  jboolean negative = (i < 0);
  jint charPos = 32;

  if (!negative) {
    i = -i;
  }

  while (i <= -radix) {
    jint q = i / radix;
    buf[charPos--] = JavaLangInteger_digits[radix * q - i];
    i = q;
  }
  buf[charPos] = JavaLangInteger_digits[-i];

  if (negative) {
    buf[--charPos] = '-';
  }

  return [NSString stringWithCharacters:buf + charPos length:33 - charPos];
}

jstring Java_java_lang_Integer_toUnsignedString0(JNIEnv *env, jclass cls, jint i, jint shift) {
  jchar buf[32];
  jint charPos = 32;
  jint radix = 1 << shift;
  jint mask = radix - 1;
  do {
    buf[--charPos] = JavaLangInteger_digits[i & mask];
    i = (uint32_t)i >> shift;
  } while (i != 0);

  return [NSString stringWithCharacters:buf + charPos length:32 - charPos];
}

jstring Java_java_lang_Integer_toString__I(JNIEnv *env, jclass cls, jint i) {
  if (i == JavaLangInteger_MIN_VALUE)
    return @"-2147483648";

  // Android-changed: cache the string literal for small values.
  jboolean negative = i < 0;
  jboolean small = negative ? i > -100 : i < 100;
  if (small) {
    NSString **smallValues = negative ? SMALL_NEG_VALUES : SMALL_NONNEG_VALUES;

    if (negative) {
      i = -i;
      if (smallValues[i] == nil) {
        smallValues[i] =
            i < 10 ? StringOf2('-', JavaLangInteger_DigitOnes[i])
                   : StringOf3('-', JavaLangInteger_DigitTens[i], JavaLangInteger_DigitOnes[i]);
      }
    } else {
      if (smallValues[i] == nil) {
        smallValues[i] =
            i < 10 ? StringOf1(JavaLangInteger_DigitOnes[i])
                   : StringOf2(JavaLangInteger_DigitTens[i], JavaLangInteger_DigitOnes[i]);
      }
    }
    return smallValues[i];
  }

  jint size = negative ? JavaLangInteger_stringSizeWithInt_(-i) + 1
                       : JavaLangInteger_stringSizeWithInt_(i);
  // J2ObjC-changed: use a stack buffer.
  jchar buf[size];
  JavaLangInteger_getCharsRaw(i, size, buf);
  return [NSString stringWithCharacters:buf length:size];
}

void Java_java_lang_Integer_getChars(JNIEnv *env, jclass cls, jint i, jint index, jarray buf) {
  JavaLangInteger_getCharsRaw(i, index, ((IOSCharArray *)buf)->buffer_);
}

void JavaLangInteger_getCharsRaw(jint i, jint index, jchar *buf) {
  jint q, r;
  jint charPos = index;
  jchar sign = 0;

  if (i < 0) {
    sign = '-';
    i = -i;
  }

  // Generate two digits per iteration
  while (i >= 65536) {
    q = i / 100;
    // really: r = i - (q * 100);
    r = i - ((q << 6) + (q << 5) + (q << 2));
    i = q;
    buf[--charPos] = JavaLangInteger_DigitOnes[r];
    buf[--charPos] = JavaLangInteger_DigitTens[r];
  }

  // Fall thru to fast mode for smaller numbers
  // assert(i <= 65536, i);
  for (;;) {
    q = (jint)(((uint32_t)(i * 52429)) >> (16 + 3));
    r = i - ((q << 3) + (q << 1));  // r = i-(q*10) ...
    buf[--charPos] = JavaLangInteger_digits[r];
    i = q;
    if (i == 0) break;
  }
  if (sign != 0) {
    buf[--charPos] = sign;
  }
}
