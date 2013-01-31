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

#ifndef __has_feature
#define __has_feature(x) 0  // Compatibility with non-clang compilers.
#endif

#ifdef __OBJC__
# import <Foundation/Foundation.h>
# import "IOSArray.h"
# import "java/lang/CharSequence.h"
# import "java/lang/Comparable.h"
# import "IOSClass.h"
# import "JavaObject.h"
# import "NSObject+JavaObject.h"
# import "NSString+JavaString.h"
# import "IOSClass.h"
# import <fcntl.h>
# import "JreMemDebug.h"

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
#  define AUTORELEASE(x) x
#  define ARCBRIDGE __bridge
#  define ARCBRIDGE_TRANSFER __bridge_transfer
#  define ARC_CONSUME_PARAMETER __attribute((ns_consumed))
# else
#  define AUTORELEASE(x) [x autorelease]
#  define ARCBRIDGE
#  define ARCBRIDGE_TRANSFER
#  define ARC_CONSUME_PARAMETER
# endif


static inline id JreOperatorRetainedAssign(id *pIvar, id value) {
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
  [* pIvar autorelease];
  * pIvar = [value retain];
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

FOUNDATION_EXPORT id JreOperatorRetainedAssign(id *pIvar, id value);

#endif // __OBJC__
