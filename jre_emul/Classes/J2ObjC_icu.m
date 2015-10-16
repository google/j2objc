// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// ICU functions used by jre_emul, invoked dynamically to avoid the need for
// apps using J2ObjC to link with the icucore dynamic library, as well as to
// avoid duplicate symbols with apps linking with ICU as a static library.

// These are private functions, and should only be used by jre_emul sources.

#include "J2ObjC_icu.h"
#include "J2ObjC_source.h"
#include "java/lang/LinkageError.h"

#include <dlfcn.h>

static void ThrowLinkError() {
  NSString *msg = [NSString stringWithUTF8String:dlerror()];
  @throw AUTORELEASE([[JavaLangLinkageError alloc] initWithNSString:msg]);
}

__attribute__((always_inline)) inline void* OpenICU() {
  void *handle = dlopen("/usr/lib/libicucore.dylib", RTLD_NOW);
  if (!handle) {
    ThrowLinkError();
  }
  return handle;
}

__attribute__((always_inline)) inline void* GetFunction(void* handle, const char* symbol) {
  void* function = dlsym(handle, symbol);
  if (!function) {
    dlclose(handle);
    ThrowLinkError();
  }
  return function;
}

__attribute__((always_inline)) inline void CloseICU(void* handle) {
  dlclose(handle);
}


// Character test functions.

// Preprocessor hack to convert a text string into a C string.
// For example, Stringify(hello) returns "hello".
#define Stringify(s) #s

#define CharTest(name) \
  U_STABLE UBool u_##name(UChar32 c) { \
    void *handle = OpenICU(); \
    UBool (*func)(UChar32) = GetFunction(handle, Stringify(u_##name)); \
    UBool result = (*func)(c); \
    CloseICU(handle); \
    return result; \
  }\

CharTest(isIDIgnorable)
CharTest(isIDPart)
CharTest(isIDStart)
CharTest(isISOControl)
CharTest(isJavaIDPart)
CharTest(isJavaIDStart)
CharTest(isJavaSpaceChar)
CharTest(isMirrored)
CharTest(isWhitespace)
CharTest(isalnum)
CharTest(isalpha)
CharTest(isdefined)
CharTest(isdigit)
CharTest(islower)
CharTest(istitle)
CharTest(isupper)


// Character conversion functions.

#define CharConvert(name) \
  U_STABLE UChar32 u_##name##_j2objc(UChar32 c) { \
    void *handle = OpenICU(); \
    UChar32 (*func)(UChar32) = GetFunction(handle, Stringify(u_##name)); \
    UChar32 result = (*func)(c); \
    CloseICU(handle); \
    return result; \
  }\

CharConvert(tolower)
CharConvert(totitle)
CharConvert(toupper)


// Other character functions.

U_STABLE UCharDirection u_charDirection_j2objc(UChar32 c) {
  void *handle = OpenICU();
  UCharDirection (*func)(UChar32) = GetFunction(handle, "u_charDirection");
  UCharDirection result = (*func)(c);
  CloseICU(handle);
  return result;
}

U_STABLE int8_t u_charType_j2objc(UChar32 c) {
  void *handle = OpenICU();
  int8_t (*func)(UChar32) = GetFunction(handle, "u_charType");
  int8_t result = (*func)(c);
  CloseICU(handle);
  return result;
}

U_STABLE int32_t u_digit_j2objc(UChar32 ch, int8_t radix) {
  void *handle = OpenICU();
  int32_t (*func)(UChar32, int8_t) = GetFunction(handle, "u_digit");
  int32_t result = (*func)(ch, radix);
  CloseICU(handle);
  return result;
}

U_STABLE const char* u_errorName_j2objc(UErrorCode code) {
  void *handle = OpenICU();
  const char* (*func)(UErrorCode) = GetFunction(handle, "u_errorName");
  const char* result = (*func)(code);
  CloseICU(handle);
  return result;
}

U_STABLE UChar32 u_forDigit_j2objc(int32_t digit, int8_t radix) {
  void *handle = OpenICU();
  UChar32 (*func)(int32_t, int8_t) = GetFunction(handle, "u_forDigit");
  UChar32 result = (*func)(digit, radix);
  CloseICU(handle);
  return result;
}

U_STABLE int32_t u_getIntPropertyValue_j2objc(UChar32 c, UProperty which) {
  void *handle = OpenICU();
  int32_t (*func)(UChar32, UProperty) = GetFunction(handle, "u_getIntPropertyValue");
  int32_t result = (*func)(c, which);
  CloseICU(handle);
  return result;
}

// Regex functions.

U_STABLE URegularExpression* uregex_clone_j2objc(
    const URegularExpression *regexp, UErrorCode *status) {
  void *handle = OpenICU();
  URegularExpression* (*func)(const URegularExpression*, UErrorCode*) =
      GetFunction(handle, "uregex_clone");
  URegularExpression* result = (*func)(regexp, status);
  CloseICU(handle);
  return result;
}

U_STABLE void uregex_close_j2objc(URegularExpression* regexp) {
  void *handle = OpenICU();
  void (*func)(URegularExpression*) = GetFunction(handle, "uregex_close");
  (*func)(regexp);
  CloseICU(handle);
}

U_STABLE int32_t uregex_end_j2objc(
    URegularExpression* regexp, int32_t groupNum, UErrorCode* status) {
  void *handle = OpenICU();
  int32_t (*func)(URegularExpression*, int32_t, UErrorCode*) = GetFunction(handle, "uregex_end");
  int32_t result = (*func)(regexp, groupNum, status);
  CloseICU(handle);
  return result;
}

U_STABLE UBool uregex_find_j2objc(
    URegularExpression* regexp, int32_t startIndex, UErrorCode* status) {
  void *handle = OpenICU();
  UBool (*func)(URegularExpression*, int32_t, UErrorCode*) = GetFunction(handle, "uregex_find");
  UBool result = (*func)(regexp, startIndex, status);
  CloseICU(handle);
  return result;
}

U_STABLE UBool uregex_findNext_j2objc(URegularExpression* regexp, UErrorCode* status) {
  void *handle = OpenICU();
  UBool (*func)(URegularExpression*, UErrorCode*) = GetFunction(handle, "uregex_findNext");
  UBool result = (*func)(regexp, status);
  CloseICU(handle);
  return result;
}

U_STABLE int32_t uregex_groupCount_j2objc(URegularExpression* regexp, UErrorCode* status) {
  void *handle = OpenICU();
  int32_t (*func)(URegularExpression*, UErrorCode*) = GetFunction(handle, "uregex_groupCount");
  int32_t result = (*func)(regexp, status);
  CloseICU(handle);
  return result;
}

U_STABLE UBool uregex_hitEnd_j2objc(const URegularExpression* regexp, UErrorCode* status) {
  void *handle = OpenICU();
  UBool (*func)(const URegularExpression*, UErrorCode*) = GetFunction(handle, "uregex_hitEnd");
  UBool result = (*func)(regexp, status);
  CloseICU(handle);
  return result;
}

U_STABLE UBool uregex_lookingAt_j2objc(
    URegularExpression* regexp, int32_t startIndex, UErrorCode* status) {
  void *handle = OpenICU();
  UBool (*func)(URegularExpression*, int32_t, UErrorCode*) =
      GetFunction(handle, "uregex_lookingAt");
  UBool result = (*func)(regexp, startIndex, status);
  CloseICU(handle);
  return result;
}

U_STABLE UBool uregex_matches_j2objc(
    URegularExpression* regexp, int32_t startIndex, UErrorCode* status) {
  void *handle = OpenICU();
  UBool (*func)(URegularExpression*, int32_t, UErrorCode*) = GetFunction(handle, "uregex_matches");
  UBool result = (*func)(regexp, startIndex, status);
  CloseICU(handle);
  return result;
}

U_STABLE URegularExpression* uregex_open(const UChar* pattern, int32_t patternLength,
    uint32_t flags, UParseError* pe, UErrorCode* status) {
  void *handle = OpenICU();
  URegularExpression* (*func)(const UChar*, int32_t, uint32_t, UParseError*, UErrorCode*) =
      GetFunction(handle, "uregex_open");
  URegularExpression* result = (*func)(pattern, patternLength, flags, pe, status);
  CloseICU(handle);
  return result;
}

U_STABLE UBool uregex_requireEnd_j2objc(const URegularExpression* regexp, UErrorCode* status) {
  void *handle = OpenICU();
  UBool (*func)(const URegularExpression*, UErrorCode*) = GetFunction(handle, "uregex_requireEnd");
  UBool result = (*func)(regexp, status);
  CloseICU(handle);
  return result;
}

U_STABLE void uregex_setRegion_j2objc(URegularExpression* regexp, int32_t regionStart,
    int32_t regionLimit, UErrorCode* status) {
  void *handle = OpenICU();
  void (*func)(URegularExpression*, int32_t, int32_t, UErrorCode*) =
      GetFunction(handle, "uregex_setRegion");
  (*func)(regexp, regionStart, regionLimit, status);
  CloseICU(handle);
}

U_STABLE void uregex_setText_j2objc(URegularExpression* regexp, const UChar* text,
    int32_t textLength, UErrorCode* status) {
  void *handle = OpenICU();
  void (*func)(URegularExpression*, const UChar*, int32_t, UErrorCode*) =
      GetFunction(handle, "uregex_setText");
  (*func)(regexp, text, textLength, status);
  CloseICU(handle);
}

U_STABLE int32_t uregex_start_j2objc(
    URegularExpression* regexp, int32_t groupNum, UErrorCode* status) {
  void *handle = OpenICU();
  int32_t (*func)(URegularExpression*, int32_t, UErrorCode*) = GetFunction(handle, "uregex_start");
  int32_t result = (*func)(regexp, groupNum, status);
  CloseICU(handle);
  return result;
}

U_STABLE void uregex_useAnchoringBounds_j2objc(
    URegularExpression* regexp, UBool b, UErrorCode* status) {
  void *handle = OpenICU();
  void (*func)(URegularExpression*, UBool, UErrorCode*) =
      GetFunction(handle, "uregex_useAnchoringBounds");
  (*func)(regexp, b, status);
  CloseICU(handle);
}

U_STABLE void uregex_useTransparentBounds_j2objc(
    URegularExpression* regexp, UBool b, UErrorCode* status) {
  void *handle = OpenICU();
  void (*func)(URegularExpression*, UBool, UErrorCode*) =
      GetFunction(handle, "uregex_useTransparentBounds");
  (*func)(regexp, b, status);
  CloseICU(handle);
}

