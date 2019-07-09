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

#include "java_lang_Long.h"

#include "IOSPrimitiveArray.h"
#include "java/lang/Character.h"
#include "java/lang/Integer.h"
#include "java/lang/Long.h"
#include "java_lang_Integer.h"
#include "jni.h"

jstring Java_java_lang_Long_toString(JNIEnv *env, jclass cls, jlong i, jint radix) {
  if (radix < JavaLangCharacter_MIN_RADIX || radix > JavaLangCharacter_MAX_RADIX)
    radix = 10;
  if (radix == 10)
    return JavaLangLong_toStringWithLong_(i);
  jchar buf[65];
  jint charPos = 64;
  jboolean negative = (i < 0);

  if (!negative) {
    i = -i;
  }

  while (i <= -radix) {
    buf[charPos--] = JavaLangInteger_digits[(int)(-(i % radix))];
    i = i / radix;
  }
  buf[charPos] = JavaLangInteger_digits[(int)(-i)];

  if (negative) {
    buf[--charPos] = '-';
  }

  return [NSString stringWithCharacters:buf + charPos length:65 - charPos];
}

jstring Java_java_lang_Long_toUnsignedString0(JNIEnv *env, jclass cls, jlong i, jint shift) {
  jchar buf[64];
  jint charPos = 64;
  jint radix = 1 << shift;
  jlong mask = radix - 1;
  do {
    buf[--charPos] = JavaLangInteger_digits[(int)(i & mask)];
    i = (uint64_t)i >> shift;
  } while (i != 0);
  return [NSString stringWithCharacters:buf + charPos length:64 - charPos];
}

void Java_java_lang_Long_getChars(JNIEnv *env, jclass cls, jlong i, jint index, jarray buf) {
  JavaLangLong_getCharsRaw(i, index, ((IOSCharArray *)buf)->buffer_);
}

void JavaLangLong_getCharsRaw(jlong i, jint index, jchar *buf) {
  jlong q;
  jint r;
  jint charPos = index;
  jchar sign = 0;

  if (i < 0) {
    sign = '-';
    i = -i;
  }

  // Get 2 digits/iteration using longs until quotient fits into an int
  while (i > JavaLangInteger_MAX_VALUE) {
    q = i / 100;
    // really: r = i - (q * 100);
    r = (jint)(i - ((q << 6) + (q << 5) + (q << 2)));
    i = q;
    buf[--charPos] = JavaLangInteger_DigitOnes[r];
    buf[--charPos] = JavaLangInteger_DigitTens[r];
  }

  // Get 2 digits/iteration using ints
  jint q2;
  jint i2 = (jint)i;
  while (i2 >= 65536) {
    q2 = i2 / 100;
    // really: r = i2 - (q * 100);
    r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
    i2 = q2;
    buf[--charPos] = JavaLangInteger_DigitOnes[r];
    buf[--charPos] = JavaLangInteger_DigitTens[r];
  }

  // Fall thru to fast mode for smaller numbers
  // assert(i2 <= 65536, i2);
  for (;;) {
    q2 = (uint32_t)(i2 * 52429) >> (16 + 3);
    r = i2 - ((q2 << 3) + (q2 << 1));  // r = i2-(q2*10) ...
    buf[--charPos] = JavaLangInteger_digits[r];
    i2 = q2;
    if (i2 == 0) break;
  }
  if (sign != 0) {
    buf[--charPos] = sign;
  }
}
