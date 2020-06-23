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

static UBool (*j2_isIDIgnorable)(UChar32 c);
static UBool (*j2_isIDPart)(UChar32 c);
static UBool (*j2_isIDStart)(UChar32 c);
static UBool (*j2_isISOControl)(UChar32 c);
static UBool (*j2_isJavaIDPart)(UChar32 c);
static UBool (*j2_isJavaIDStart)(UChar32 c);
static UBool (*j2_isJavaSpaceChar)(UChar32 c);
static UBool (*j2_isMirrored)(UChar32 c);
static UBool (*j2_isWhitespace)(UChar32 c);
static UBool (*j2_isalnum)(UChar32 c);
static UBool (*j2_isalpha)(UChar32 c);
static UBool (*j2_isdefined)(UChar32 c);
static UBool (*j2_isdigit)(UChar32 c);
static UBool (*j2_islower)(UChar32 c);
static UBool (*j2_istitle)(UChar32 c);
static UBool (*j2_isupper)(UChar32 c);
static UChar32 (*j2_tolower)(UChar32 c);
static UChar32 (*j2_totitle)(UChar32 c);
static UChar32 (*j2_toupper)(UChar32 c);
static UCharDirection (*j2_charDirection)(UChar32 c);
static int8_t (*j2_charType)(UChar32 c);
static int32_t (*j2_digit)(UChar32 ch, int8_t radix);
static const char* (*j2_errorName)(UErrorCode code);
static UChar32 (*j2_forDigit)(int32_t digit, int8_t radix);
static int32_t (*j2_getIntPropertyValue)(UChar32 c, UProperty which);

static URegularExpression* (*j2_uregex_clone)(const URegularExpression *regexp, UErrorCode *status);
static void (*j2_uregex_close)(URegularExpression* regexp);
static int32_t (*j2_uregex_end)(URegularExpression* regexp, int32_t groupNum, UErrorCode* status);
static UBool (*j2_uregex_find)(URegularExpression* regexp, int32_t startIndex, UErrorCode* status);
static UBool (*j2_uregex_findNext)(URegularExpression* regexp, UErrorCode* status);
static int32_t (*j2_uregex_groupCount)(URegularExpression* regexp, UErrorCode* status);
static UBool (*j2_uregex_hitEnd)(const URegularExpression* regexp, UErrorCode* status);
static UBool (*j2_uregex_lookingAt)(
    URegularExpression* regexp, int32_t startIndex, UErrorCode* status);
static UBool (*j2_uregex_matches)(
    URegularExpression* regexp, int32_t startIndex, UErrorCode* status);
static URegularExpression* (*j2_uregex_open)(const UChar* pattern, int32_t patternLength,
    uint32_t flags, UParseError* pe, UErrorCode* status);
static UBool (*j2_uregex_requireEnd)(const URegularExpression* regexp, UErrorCode* status);
static void (*j2_uregex_setRegion)(URegularExpression* regexp, int32_t regionStart,
    int32_t regionLimit, UErrorCode* status);
static void (*j2_uregex_setText)(URegularExpression* regexp, const UChar* text,
    int32_t textLength, UErrorCode* status);
static int32_t (*j2_uregex_start)(
    URegularExpression* regexp, int32_t groupNum, UErrorCode* status);
static void (*j2_uregex_useAnchoringBounds)(
    URegularExpression* regexp, UBool b, UErrorCode* status);
static void (*j2_uregex_useTransparentBounds)(
    URegularExpression* regexp, UBool b, UErrorCode* status);
static int32_t (*j2_uregex_groupNumberFromName)(URegularExpression* regexp,
    const UChar* groupName, int32_t nameLength, UErrorCode* status);


static void ThrowLinkError() {
  NSString *msg = [NSString stringWithUTF8String:dlerror()];
  @throw AUTORELEASE([[JavaLangLinkageError alloc] initWithNSString:msg]);
}

static void* GetFunction(void* handle, const char* symbol) {
  void* function = dlsym(handle, symbol);
  if (!function) {
    dlclose(handle);
    ThrowLinkError();
  }
  return function;
}


// Initialize ICU function pointers.
U_STABLE void J2ObjC_icu_init() {
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    void *handle = dlopen("/usr/lib/libicucore.dylib", RTLD_NOW);
    if (!handle) {
      ThrowLinkError();
    }

    j2_isIDIgnorable = GetFunction(handle, "u_isIDIgnorable");
    j2_isIDPart = GetFunction(handle, "u_isIDPart");
    j2_isIDStart = GetFunction(handle, "u_isIDStart");
    j2_isISOControl = GetFunction(handle, "u_isISOControl");
    j2_isJavaIDPart = GetFunction(handle, "u_isJavaIDPart");
    j2_isJavaIDStart = GetFunction(handle, "u_isJavaIDStart");
    j2_isJavaSpaceChar = GetFunction(handle, "u_isJavaSpaceChar");
    j2_isMirrored = GetFunction(handle, "u_isMirrored");
    j2_isWhitespace = GetFunction(handle, "u_isWhitespace");
    j2_isalnum = GetFunction(handle, "u_isalnum");
    j2_isalpha = GetFunction(handle, "u_isalpha");
    j2_isdefined = GetFunction(handle, "u_isdefined");
    j2_isdigit = GetFunction(handle, "u_isdigit");
    j2_islower = GetFunction(handle, "u_islower");
    j2_istitle = GetFunction(handle, "u_istitle");
    j2_isupper = GetFunction(handle, "u_isupper");
    j2_tolower = GetFunction(handle, "u_tolower");
    j2_totitle = GetFunction(handle, "u_totitle");
    j2_toupper = GetFunction(handle, "u_toupper");

    j2_charDirection = GetFunction(handle, "u_charDirection");
    j2_charType = GetFunction(handle, "u_charType");
    j2_digit = GetFunction(handle, "u_digit");
    j2_errorName = GetFunction(handle, "u_errorName");
    j2_forDigit = GetFunction(handle, "u_forDigit");
    j2_getIntPropertyValue = GetFunction(handle, "u_getIntPropertyValue");

    j2_uregex_clone = GetFunction(handle, "uregex_clone");
    j2_uregex_close = GetFunction(handle, "uregex_close");
    j2_uregex_end = GetFunction(handle, "uregex_end");
    j2_uregex_find = GetFunction(handle, "uregex_find");
    j2_uregex_findNext = GetFunction(handle, "uregex_findNext");
    j2_uregex_groupCount = GetFunction(handle, "uregex_groupCount");
    j2_uregex_hitEnd = GetFunction(handle, "uregex_hitEnd");
    j2_uregex_lookingAt = GetFunction(handle, "uregex_lookingAt");
    j2_uregex_matches = GetFunction(handle, "uregex_matches");
    j2_uregex_open = GetFunction(handle, "uregex_open");
    j2_uregex_requireEnd = GetFunction(handle, "uregex_requireEnd");
    j2_uregex_setRegion = GetFunction(handle, "uregex_setRegion");
    j2_uregex_setText = GetFunction(handle, "uregex_setText");
    j2_uregex_start = GetFunction(handle, "uregex_start");
    j2_uregex_useAnchoringBounds = GetFunction(handle, "uregex_useAnchoringBounds");
    j2_uregex_useTransparentBounds = GetFunction(handle, "uregex_useTransparentBounds");
    j2_uregex_groupNumberFromName = GetFunction(handle, "uregex_groupNumberFromName");

    // Don't close library handle, or these function pointers will be invalidated.
  });
}


// Character test functions.

#define CharTest(name) \
  U_STABLE UBool u_##name##_j2objc(UChar32 c) { \
    return (*j2_##name)(c); \
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
    return (*j2_##name)(c); \
  }\

CharConvert(tolower)
CharConvert(totitle)
CharConvert(toupper)


// Other character functions.

U_STABLE UCharDirection u_charDirection_j2objc(UChar32 c) {
  return (*j2_charDirection)(c);
}

U_STABLE int8_t u_charType_j2objc(UChar32 c) {
  return (*j2_charType)(c);
}

U_STABLE int32_t u_digit_j2objc(UChar32 ch, int8_t radix) {
  return (*j2_digit)(ch, radix);
}

U_STABLE const char* u_errorName_j2objc(UErrorCode code) {
  return (*j2_errorName)(code);
}

U_STABLE UChar32 u_forDigit_j2objc(int32_t digit, int8_t radix) {
  return (*j2_forDigit)(digit, radix);
}

U_STABLE int32_t u_getIntPropertyValue_j2objc(UChar32 c, UProperty which) {
  return (*j2_getIntPropertyValue)(c, which);
}

// Regex functions.

U_STABLE URegularExpression* uregex_clone_j2objc(
    const URegularExpression *regexp, UErrorCode *status) {
  return (*j2_uregex_clone)(regexp, status);
}

U_STABLE void uregex_close_j2objc(URegularExpression* regexp) {
  (*j2_uregex_close)(regexp);
}

U_STABLE int32_t uregex_end_j2objc(
    URegularExpression* regexp, int32_t groupNum, UErrorCode* status) {
  return (*j2_uregex_end)(regexp, groupNum, status);
}

U_STABLE UBool uregex_find_j2objc(
    URegularExpression* regexp, int32_t startIndex, UErrorCode* status) {
  return (*j2_uregex_find)(regexp, startIndex, status);
}

U_STABLE UBool uregex_findNext_j2objc(URegularExpression* regexp, UErrorCode* status) {
  return (*j2_uregex_findNext)(regexp, status);
}

U_STABLE int32_t uregex_groupCount_j2objc(URegularExpression* regexp, UErrorCode* status) {
  return (*j2_uregex_groupCount)(regexp, status);
}

U_STABLE UBool uregex_hitEnd_j2objc(const URegularExpression* regexp, UErrorCode* status) {
  return (*j2_uregex_hitEnd)(regexp, status);
}

U_STABLE UBool uregex_lookingAt_j2objc(
    URegularExpression* regexp, int32_t startIndex, UErrorCode* status) {
  return (*j2_uregex_lookingAt)(regexp, startIndex, status);
}

U_STABLE UBool uregex_matches_j2objc(
    URegularExpression* regexp, int32_t startIndex, UErrorCode* status) {
  return (*j2_uregex_matches)(regexp, startIndex, status);
}

U_STABLE URegularExpression* uregex_open_j2objc(const UChar* pattern, int32_t patternLength,
    uint32_t flags, UParseError* pe, UErrorCode* status) {
  return (*j2_uregex_open)(pattern, patternLength, flags, pe, status);
}

U_STABLE UBool uregex_requireEnd_j2objc(const URegularExpression* regexp, UErrorCode* status) {
  return (*j2_uregex_requireEnd)(regexp, status);
}

U_STABLE void uregex_setRegion_j2objc(URegularExpression* regexp, int32_t regionStart,
    int32_t regionLimit, UErrorCode* status) {
  (*j2_uregex_setRegion)(regexp, regionStart, regionLimit, status);
}

U_STABLE void uregex_setText_j2objc(URegularExpression* regexp, const UChar* text,
    int32_t textLength, UErrorCode* status) {
  (*j2_uregex_setText)(regexp, text, textLength, status);
}

U_STABLE int32_t uregex_start_j2objc(
    URegularExpression* regexp, int32_t groupNum, UErrorCode* status) {
  return (*j2_uregex_start)(regexp, groupNum, status);
}

U_STABLE void uregex_useAnchoringBounds_j2objc(
    URegularExpression* regexp, UBool b, UErrorCode* status) {
  (*j2_uregex_useAnchoringBounds)(regexp, b, status);
}

U_STABLE void uregex_useTransparentBounds_j2objc(
    URegularExpression* regexp, UBool b, UErrorCode* status) {
  (*j2_uregex_useTransparentBounds)(regexp, b, status);
}

U_STABLE int32_t uregex_groupNumberFromName_j2objc(URegularExpression* regexp,
    const UChar* groupName, int32_t nameLength, UErrorCode* status) {
  return (*j2_uregex_groupNumberFromName)(regexp, groupName, nameLength, status);
}
