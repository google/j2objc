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
#include "java/lang/ArithmeticException.h"
#include "java/lang/ArrayIndexOutOfBoundsException.h"
#include "java/lang/IllegalArgumentException.h"
#include "java/lang/RuntimeException.h"
#include "java/lang/UnsupportedOperationException.h"
#include "unicode/uregex.h"

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

void JavaUtilRegexMatcher_closeImplWithLong_(jlong address) {
  uregex_close((URegularExpression *)address);
}

jboolean JavaUtilRegexMatcher_findImplWithLong_withInt_withIntArray_(
    jlong addr, jint startIndex, IOSIntArray *offsets) {
  URegularExpression *regex = (URegularExpression *)addr;
  UErrorCode status = U_ZERO_ERROR;
  jboolean result = uregex_find(regex, startIndex, &status);
  if (result) {
    updateOffsets(regex, offsets, &status);
  }
  maybeThrowIcuException("uregex_find", status);
  return result;
}

jboolean JavaUtilRegexMatcher_findNextImplWithLong_withIntArray_(jlong addr, IOSIntArray *offsets) {
  URegularExpression *regex = (URegularExpression *)addr;
  UErrorCode status = U_ZERO_ERROR;
  jboolean result = uregex_findNext(regex, &status);
  if (result) {
    updateOffsets(regex, offsets, &status);
  }
  maybeThrowIcuException("uregex_findNext", status);
  return result;
}

jint JavaUtilRegexMatcher_groupCountImplWithLong_(jlong addr) {
  UErrorCode status = U_ZERO_ERROR;
  jint result = uregex_groupCount((URegularExpression *)addr, &status);
  maybeThrowIcuException("uregex_groupCount", status);
  return result;
}

jboolean JavaUtilRegexMatcher_hitEndImplWithLong_(jlong addr) {
  UErrorCode status = U_ZERO_ERROR;
  jboolean result = uregex_hitEnd((URegularExpression *)addr, &status);
  maybeThrowIcuException("uregex_hitEnd", status);
  return result;
}

jboolean JavaUtilRegexMatcher_lookingAtImplWithLong_withIntArray_(
    jlong addr, IOSIntArray *offsets) {
  URegularExpression *regex = (URegularExpression *)addr;
  UErrorCode status = U_ZERO_ERROR;
  jboolean result = uregex_lookingAt(regex, -1, &status);
  if (result) {
    updateOffsets(regex, offsets, &status);
  }
  maybeThrowIcuException("uregex_lookingAt", status);
  return result;
}

jboolean JavaUtilRegexMatcher_matchesImplWithLong_withIntArray_(jlong addr, IOSIntArray *offsets) {
  URegularExpression *regex = (URegularExpression *)addr;
  UErrorCode status = U_ZERO_ERROR;
  jboolean result = uregex_matches(regex, -1, &status);
  if (result) {
    updateOffsets(regex, offsets, &status);
  }
  maybeThrowIcuException("uregex_matches", status);
  return result;
}

jlong JavaUtilRegexMatcher_openImplWithLong_(jlong patternAddr) {
  UErrorCode status = U_ZERO_ERROR;
  URegularExpression *result = uregex_clone((URegularExpression *)patternAddr, &status);
  maybeThrowIcuException("uregex_clone", status);
  return (jlong)result;
}

jboolean JavaUtilRegexMatcher_requireEndImplWithLong_(jlong addr) {
  UErrorCode status = U_ZERO_ERROR;
  jboolean result = uregex_requireEnd((URegularExpression *)addr, &status);
  maybeThrowIcuException("uregex_requireEnd", status);
  return result;
}

void JavaUtilRegexMatcher_setInputImplWithLong_withCharArray_withInt_withInt_(
    jlong addr, IOSCharArray *javaText, jint start, jint end) {
  URegularExpression *regex = (URegularExpression *)addr;
  UErrorCode status = U_ZERO_ERROR;
  uregex_setText(regex, javaText->buffer_, javaText->size_, &status);
  uregex_setRegion(regex, start, end, &status);
  maybeThrowIcuException("uregex_setText", status);
}

void JavaUtilRegexMatcher_useAnchoringBoundsImplWithLong_withBoolean_(jlong addr, jboolean value) {
  UErrorCode status = U_ZERO_ERROR;
  uregex_useAnchoringBounds((URegularExpression *)addr, value, &status);
  maybeThrowIcuException("uregex_useAnchoringBounds", status);
}

void JavaUtilRegexMatcher_useTransparentBoundsImplWithLong_withBoolean_(
    jlong addr, jboolean value) {
  UErrorCode status = U_ZERO_ERROR;
  uregex_useTransparentBounds((URegularExpression *)addr, value, &status);
  maybeThrowIcuException("uregex_useTransparentBounds", status);
}
