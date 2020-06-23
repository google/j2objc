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

#include "IOSPrimitiveArray.h"
#include "J2ObjC_icu.h"
#include "java/lang/ArithmeticException.h"
#include "java/lang/ArrayIndexOutOfBoundsException.h"
#include "java/lang/IllegalArgumentException.h"
#include "java/lang/RuntimeException.h"
#include "java/lang/UnsupportedOperationException.h"
#include "jni.h"

// ICU documentation: http://icu-project.org/apiref/icu4c/classRegexMatcher.html

// Taken from libcore/luni/src/main/native/IcuUtilities.cpp
bool maybeThrowIcuException(const char* function, UErrorCode error) {
  if (U_SUCCESS(error)) {
    return false;
  }
  NSString *msg = [NSString stringWithFormat:@"%s failed: %s", function, u_errorName(error)];
  JavaLangRuntimeException *e;
  if (error == U_ILLEGAL_ARGUMENT_ERROR) {
    e = [[JavaLangIllegalArgumentException alloc] initWithNSString:msg];
  } else if (error == U_INDEX_OUTOFBOUNDS_ERROR || error == U_BUFFER_OVERFLOW_ERROR) {
    e = [[JavaLangArrayIndexOutOfBoundsException alloc] initWithNSString:msg];
  } else if (error == U_UNSUPPORTED_ERROR) {
    e = [[JavaLangUnsupportedOperationException alloc] initWithNSString:msg];
  } else if (error == U_FORMAT_INEXACT_ERROR) {
    e = [[JavaLangArithmeticException alloc] initWithNSString:msg];
  } else {
    e = [[JavaLangRuntimeException alloc] initWithNSString:msg];
  }
  @throw [e autorelease];
  return true;
}

static void updateOffsets(URegularExpression *regex, IOSIntArray *offsets, UErrorCode *status) {
  jint groupCount = uregex_groupCount(regex, status);
  for (jint i = 0; i <= groupCount; ++i) {
    offsets->buffer_[2 * i + 0] = uregex_start(regex, i, status);
    offsets->buffer_[2 * i + 1] = uregex_end(regex, i, status);
  }
}

void Java_java_util_regex_Matcher_closeImpl(JNIEnv *env, jclass cls, jlong address) {
  uregex_close((URegularExpression *)address);
}

jboolean Java_java_util_regex_Matcher_findImpl(
    JNIEnv *env, jclass cls, jlong addr, jint startIndex, IOSIntArray *offsets) {
  URegularExpression *regex = (URegularExpression *)addr;
  UErrorCode status = U_ZERO_ERROR;
  jboolean result = uregex_find(regex, startIndex, &status);
  if (result) {
    updateOffsets(regex, offsets, &status);
  }
  maybeThrowIcuException("uregex_find", status);
  return result;
}

jboolean Java_java_util_regex_Matcher_findNextImpl(
    JNIEnv *env, jclass cls, jlong addr, IOSIntArray *offsets) {
  URegularExpression *regex = (URegularExpression *)addr;
  UErrorCode status = U_ZERO_ERROR;
  jboolean result = uregex_findNext(regex, &status);
  if (result) {
    updateOffsets(regex, offsets, &status);
  }
  maybeThrowIcuException("uregex_findNext", status);
  return result;
}

jint Java_java_util_regex_Matcher_groupCountImpl(JNIEnv *env, jclass cls, jlong addr) {
  UErrorCode status = U_ZERO_ERROR;
  jint result = uregex_groupCount((URegularExpression *)addr, &status);
  maybeThrowIcuException("uregex_groupCount", status);
  return result;
}

jboolean Java_java_util_regex_Matcher_hitEndImpl(JNIEnv *env, jclass cls, jlong addr) {
  UErrorCode status = U_ZERO_ERROR;
  jboolean result = uregex_hitEnd((URegularExpression *)addr, &status);
  maybeThrowIcuException("uregex_hitEnd", status);
  return result;
}

jboolean Java_java_util_regex_Matcher_lookingAtImpl(
    JNIEnv *env, jclass cls, jlong addr, IOSIntArray *offsets) {
  URegularExpression *regex = (URegularExpression *)addr;
  UErrorCode status = U_ZERO_ERROR;
  jboolean result = uregex_lookingAt(regex, -1, &status);
  if (result) {
    updateOffsets(regex, offsets, &status);
  }
  maybeThrowIcuException("uregex_lookingAt", status);
  return result;
}

jboolean Java_java_util_regex_Matcher_matchesImpl(
    JNIEnv *env, jclass cls, jlong addr, IOSIntArray *offsets) {
  URegularExpression *regex = (URegularExpression *)addr;
  UErrorCode status = U_ZERO_ERROR;
  jboolean result = uregex_matches(regex, -1, &status);
  if (result) {
    updateOffsets(regex, offsets, &status);
  }
  maybeThrowIcuException("uregex_matches", status);
  return result;
}

jlong Java_java_util_regex_Matcher_openImpl(JNIEnv *env, jclass cls, jlong patternAddr) {
  UErrorCode status = U_ZERO_ERROR;
  URegularExpression *result = uregex_clone((URegularExpression *)patternAddr, &status);
  maybeThrowIcuException("uregex_clone", status);
  return (jlong)result;
}

jint Java_java_util_regex_Matcher_getMatchedGroupIndexImpl(
    JNIEnv *env, jclass cls, jlong addr, jstring name) {
  UErrorCode status = U_ZERO_ERROR;
  jint nameLength = (jint)[name length];
  jint result = uregex_groupNumberFromName((URegularExpression *)addr, (UChar *)name, nameLength, &status);
  if (U_SUCCESS(status)) {
    return result;
  }
  if (status == U_REGEX_INVALID_CAPTURE_GROUP_NAME) {
    return -1;
  }
  maybeThrowIcuException("uregex_groupNumberFromName", status);
  return -1;
}

jboolean Java_java_util_regex_Matcher_requireEndImpl(JNIEnv *env, jclass cls, jlong addr) {
  UErrorCode status = U_ZERO_ERROR;
  jboolean result = uregex_requireEnd((URegularExpression *)addr, &status);
  maybeThrowIcuException("uregex_requireEnd", status);
  return result;
}

void Java_java_util_regex_Matcher_useAnchoringBoundsImpl(
    JNIEnv *env, jclass cls, jlong addr, jboolean value) {
  UErrorCode status = U_ZERO_ERROR;
  uregex_useAnchoringBounds((URegularExpression *)addr, value, &status);
  maybeThrowIcuException("uregex_useAnchoringBounds", status);
}

void Java_java_util_regex_Matcher_useTransparentBoundsImpl(
    JNIEnv *env, jclass cls, jlong addr, jboolean value) {
  UErrorCode status = U_ZERO_ERROR;
  uregex_useTransparentBounds((URegularExpression *)addr, value, &status);
  maybeThrowIcuException("uregex_useTransparentBounds", status);
}

void Java_java_util_regex_Matcher_setInputImpl(
    JNIEnv *env, jclass cls, jlong addr, IOSCharArray *javaText, jint start, jint end,
    jboolean anchoringBounds, jboolean transparentBounds) {
  URegularExpression *regex = (URegularExpression *)addr;
  UErrorCode status = U_ZERO_ERROR;
  uregex_setText(regex, javaText->buffer_, javaText->size_, &status);
  uregex_setRegion(regex, start, end, &status);
  maybeThrowIcuException("uregex_setText", status);
  Java_java_util_regex_Matcher_useAnchoringBoundsImpl(env, cls, addr, anchoringBounds);
  Java_java_util_regex_Matcher_useTransparentBoundsImpl(env, cls, addr, transparentBounds);
}

void Java_java_util_regex_Matcher_initICU(JNIEnv *env, jclass cls) {
  J2ObjC_icu_init();
}
