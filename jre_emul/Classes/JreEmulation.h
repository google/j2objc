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

static inline id JreOperatorRetainedAssign(id *pIvar, id self, id value) {
  // We need a lock here because during
  // JreMemDebugGenerateAllocationsReport(), we want the list of links
  // of the graph to be consistent.
#if JREMEMDEBUG_ENABLED
  if (JreMemDebugEnabled) {
    JreMemDebugLock();
  }
#endif // JREMEMDEBUG_ENABLED
#if __has_feature(objc_arc)
  * pIvar = value;
#else
  if (* pIvar != self) {
    [* pIvar autorelease];
  }
  * pIvar = value != self ? [value retain] : self;
#endif // __has_feature(objc_arc)
#if JREMEMDEBUG_ENABLED
  if (JreMemDebugEnabled) {
    JreMemDebugUnlock();
  }
#endif // JREMEMDEBUG_ENABLED

  return value;
}

// Converts main() arguments into an IOSObjectArray of NSStrings.
FOUNDATION_EXPORT
    IOSObjectArray *JreEmulationMainArguments(int argc, const char *argv[]);

FOUNDATION_EXPORT id JreOperatorRetainedAssign(id *pIvar, id self, id value);

#define UR_SHIFT_ASSIGN_DEFN(NAME, TYPE) \
  static inline TYPE URShiftAssign##NAME(TYPE *pLhs, int rhs) { \
    return *pLhs = (TYPE) (((unsigned TYPE) *pLhs) >> rhs); \
  }

UR_SHIFT_ASSIGN_DEFN(Byte, char)
UR_SHIFT_ASSIGN_DEFN(Int, int)
UR_SHIFT_ASSIGN_DEFN(Long, long long)
UR_SHIFT_ASSIGN_DEFN(Short, short int)

#endif // __OBJC__

#endif // _JreEmulation_H_
