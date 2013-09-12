// Copyright 2011 Google Inc. All Rights Reserved.
//
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

//
// Prefix header for all source files of the 'JreEmulation' target in
// the 'JreEmulation' project.
//

#ifndef _JreEmulation_H_
#define _JreEmulation_H_

#ifndef __has_feature
#define __has_feature(x) 0  // Compatibility with non-clang compilers.
#endif

#ifdef __OBJC__
#import <Foundation/Foundation.h>
#import "JavaObject.h"
#import "NSObject+JavaObject.h"
#import "NSString+JavaString.h"
#import "JreMemDebug.h"

# ifndef __has_attribute
#  define __has_attribute(x) 0 // Compatibility with non-clang compilers.
# endif // __has_attribute

# ifndef OBJC_METHOD_FAMILY_NONE
#  if __has_attribute(objc_method_family)
#   define OBJC_METHOD_FAMILY_NONE __attribute__((objc_method_family(none)))
#  else
#   define OBJC_METHOD_FAMILY_NONE
#  endif
# endif

# if __has_feature(objc_arc)
#  define ARCBRIDGE __bridge
#  define ARCBRIDGE_TRANSFER __bridge_transfer
#  define ARC_CONSUME_PARAMETER __attribute((ns_consumed))
#  define AUTORELEASE(x) x
#  define RETAIN(x) x
#  define RETAIN_AND_AUTORELEASE(x) x
# else
#  define ARCBRIDGE
#  define ARCBRIDGE_TRANSFER
#  define ARC_CONSUME_PARAMETER
#  define AUTORELEASE(x) [x autorelease]
#  define RETAIN(x) [x retain]
#  define RETAIN_AND_AUTORELEASE(x) [[x retain] autorelease]
# endif

#define J2OBJC_COMMA() ,

#ifdef J2OBJC_DISABLE_ALL_CHECKS
 #define J2OBJC_DISABLE_NIL_CHECKS 1
 #define J2OBJC_DISABLE_CAST_CHECKS 1
 #define J2OBJC_DISABLE_ARRAY_CHECKS 1
 #define J2OBJC_DISABLE_ARRAY_TYPE_CHECKS 1
#endif

FOUNDATION_EXPORT void JreThrowNullPointerException() __attribute__((noreturn));

#ifdef J2OBJC_COUNT_NIL_CHK
extern int j2objc_nil_chk_count;
#endif

extern void JrePrintNilChkCount();
extern void JrePrintNilChkCountAtExit();

// Marked as unused to avoid a clang warning when this file is included
// but NIL_CHK isn't used.
__attribute__ ((unused)) static inline id nil_chk(id __unsafe_unretained p) {
#ifdef J2OBJC_COUNT_NIL_CHK
  j2objc_nil_chk_count++;
#endif
#if !defined(J2OBJC_DISABLE_NIL_CHECKS)
  if (!p) {
    JreThrowNullPointerException();
  }
#endif
  return p;
}

// Separate methods for class and protocol cast checks are used to reduce
// overhead, since the difference is statically known.
__attribute__ ((unused)) static inline id check_class_cast(id __unsafe_unretained p, Class clazz) {
#if !defined(J2OBJC_DISABLE_CAST_CHECKS)
  return (!p || [p isKindOfClass:clazz]) ? p : [NSObject throwClassCastException];
#endif
}

__attribute__ ((unused)) static inline id check_protocol_cast(id __unsafe_unretained p,
                                                              Protocol *protocol) {
#if !defined(J2OBJC_DISABLE_CAST_CHECKS)
  return (!p || [p conformsToProtocol:protocol]) ? p : [NSObject throwClassCastException];
#endif
}

// Should only be used with manual reference counting.
#if !__has_feature(objc_arc)
static inline id JreOperatorRetainedAssign(id *pIvar, id self, id value) {
  // We need a lock here because during
  // JreMemDebugGenerateAllocationsReport(), we want the list of links
  // of the graph to be consistent.
#if JREMEMDEBUG_ENABLED
  if (JreMemDebugEnabled) {
    JreMemDebugLock();
  }
#endif // JREMEMDEBUG_ENABLED
  if (* pIvar != self) {
    [* pIvar autorelease];
  }
  * pIvar = value != self ? [value retain] : self;
#if JREMEMDEBUG_ENABLED
  if (JreMemDebugEnabled) {
    JreMemDebugUnlock();
  }
#endif // JREMEMDEBUG_ENABLED

  return value;
}
#endif

// Converts main() arguments into an IOSObjectArray of NSStrings.
FOUNDATION_EXPORT
    IOSObjectArray *JreEmulationMainArguments(int argc, const char *argv[]);

#if __has_feature(objc_arc)
#define J2OBJC_FIELD_SETTER(CLASS, FIELD, TYPE) \
  static inline TYPE CLASS##_set_##FIELD(CLASS *instance, TYPE value) { \
    return instance->FIELD = value; \
  }
#else
#define J2OBJC_FIELD_SETTER(CLASS, FIELD, TYPE) \
  static inline TYPE CLASS##_set_##FIELD(CLASS *instance, TYPE value) { \
    return JreOperatorRetainedAssign(&instance->FIELD, instance, value); \
  }
#endif

#define MOD_ASSIGN_DEFN(NAME, TYPE) \
  static inline TYPE ModAssign##NAME(TYPE *pLhs, double rhs) { \
    return *pLhs = fmod(*pLhs, rhs); \
  }

MOD_ASSIGN_DEFN(Byte, char)
MOD_ASSIGN_DEFN(Char, unichar)
MOD_ASSIGN_DEFN(Double, double)
MOD_ASSIGN_DEFN(Float, float)
MOD_ASSIGN_DEFN(Int, int)
MOD_ASSIGN_DEFN(Long, long long)
MOD_ASSIGN_DEFN(Short, short int)

#define UR_SHIFT_ASSIGN_DEFN(NAME, TYPE) \
  static inline TYPE URShiftAssign##NAME(TYPE *pLhs, int rhs) { \
    return *pLhs = (TYPE) (((unsigned TYPE) *pLhs) >> rhs); \
  }

UR_SHIFT_ASSIGN_DEFN(Byte, char)
UR_SHIFT_ASSIGN_DEFN(Int, int)
UR_SHIFT_ASSIGN_DEFN(Long, long long)
UR_SHIFT_ASSIGN_DEFN(Short, short int)

// This macro is used by the translator to add increment and decrement
// operations to the header files of the boxed types.
#define BOXED_INC_AND_DEC(CNAME, VALUE_METHOD, TYPE) \
  static inline TYPE *PreIncr##CNAME(TYPE **value) { \
    nil_chk(*value); \
    return *value = [TYPE valueOfWith##CNAME:[*value VALUE_METHOD] + 1]; \
  } \
  static inline TYPE *PostIncr##CNAME(TYPE **value) { \
    nil_chk(*value); \
    TYPE *original = *value; \
    *value = [TYPE valueOfWith##CNAME:[*value VALUE_METHOD] + 1]; \
    return original; \
  } \
  static inline TYPE *PreDecr##CNAME(TYPE **value) { \
    nil_chk(*value); \
    return *value = [TYPE valueOfWith##CNAME:[*value VALUE_METHOD] - 1]; \
  } \
  static inline TYPE *PostDecr##CNAME(TYPE **value) { \
    nil_chk(*value); \
    TYPE *original = *value; \
    *value = [TYPE valueOfWith##CNAME:[*value VALUE_METHOD] - 1]; \
    return original; \
  }

#endif // __OBJC__

#endif // _JreEmulation_H_
