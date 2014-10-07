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
//  JreEmulation.m
//  J2ObjC
//
//  Created by Tom Ball on 4/23/12.
//

#import "JreEmulation.h"
#import "IOSClass.h"
#import "java/lang/AbstractStringBuilder.h"
#import "java/lang/NullPointerException.h"
#import "java_lang_IntegralToString.h"
#import "java_lang_RealToString.h"

void JreThrowNullPointerException() {
  @throw AUTORELEASE([[JavaLangNullPointerException alloc] init]);
}

#ifdef J2OBJC_COUNT_NIL_CHK
int j2objc_nil_chk_count = 0;
#endif

void JrePrintNilChkCount() {
#ifdef J2OBJC_COUNT_NIL_CHK
  printf("nil_chk count: %d\n", j2objc_nil_chk_count);
#endif
}

void JrePrintNilChkCountAtExit() {
  atexit(JrePrintNilChkCount);
}

// Converts main() arguments into an IOSObjectArray of NSStrings.  The first
// argument, the program name, is skipped so the returned array matches what
// is passed to a Java main method.
FOUNDATION_EXPORT
    IOSObjectArray *JreEmulationMainArguments(int argc, const char *argv[]) {
  IOSClass *stringType = [IOSClass classWithClass:[NSString class]];
  if (argc <= 1) {
    return [IOSObjectArray arrayWithLength:0 type:stringType];
  }
  IOSObjectArray *args = [IOSObjectArray arrayWithLength:argc - 1 type:stringType];
  for (int i = 1; i < argc; i++) {
    NSString *arg =
        [NSString stringWithCString:argv[i]
                           encoding:[NSString defaultCStringEncoding]];
    IOSObjectArray_Set(args, i - 1, arg);
  }
  return args;
}

FOUNDATION_EXPORT NSString *JreStrcat(const char *pTypes, ...) {
  // Count number of object arguments.
  unsigned int numObjs = 0;
  const char *types = pTypes;
  while (*types) {
    if (*(types++) == '@') numObjs++;
  }
  NSString *objDescriptions[numObjs];

  // Compute the capacity for the buffer.
  jint capacity = 0;
  unsigned int objIdx = 0;
  va_list va;
  va_start(va, pTypes);
  types = pTypes;
  while (*types) {
    switch(*types) {
      case 'C':
        capacity++;
        va_arg(va, jint);
        break;
      case 'D':
        capacity += 24;  // Determined experimentally.
        va_arg(va, jdouble);
        break;
      case 'F':
        capacity += 15;  // Determined experimentally.
        va_arg(va, jdouble);
        break;
      case 'B':
        capacity += 4;
        va_arg(va, jint);
        break;
      case 'S':
        capacity += 6;
        va_arg(va, jint);
        break;
      case 'I':
        capacity += 11;
        va_arg(va, jint);
        break;
      case 'J':
        capacity += 20;
        va_arg(va, jlong);
        break;
      case 'Z':
        capacity += (jboolean)va_arg(va, jint) ? 4 : 5;
        break;
      case '$':
        {
          NSString *str = va_arg(va, NSString *);
          capacity += str ? CFStringGetLength((CFStringRef)str) : 4;
        }
        break;
      case '@':
        {
          id obj = va_arg(va, id);
          if (obj) {
            NSString *description = [obj description];
            objDescriptions[objIdx++] = description;
            capacity += CFStringGetLength((CFStringRef)description);
          } else {
            objDescriptions[objIdx++] = nil;
            capacity += 4;
          }
        }
        break;
    }
    types++;
  }
  va_end(va);

  // Create a string builder and fill it.
  va_start(va, pTypes);
  JreStringBuilder sb;
  JreStringBuilder_initWithCapacity(&sb, capacity);
  types = pTypes;
  objIdx = 0;
  while (*types) {
    switch (*types) {
      case 'C':
        JreStringBuilder_appendChar(&sb, (jchar)va_arg(va, jint));
        break;
      case 'D':
        RealToString_appendDouble(&sb, va_arg(va, jdouble));
        break;
      case 'F':
        RealToString_appendFloat(&sb, (jfloat)va_arg(va, jdouble));
        break;
      case 'B':
      case 'I':
      case 'S':
        IntegralToString_convertInt(&sb, va_arg(va, jint));
        break;
      case 'J':
        IntegralToString_convertLong(&sb, va_arg(va, jlong));
        break;
      case 'Z':
        JreStringBuilder_appendString(&sb, (jboolean)va_arg(va, jint) ? @"true" : @"false");
        break;
      case '$':
        JreStringBuilder_appendString(&sb, va_arg(va, NSString *));
        break;
      case '@':
        {
          va_arg(va, id);
          NSString *str = objDescriptions[objIdx++];
          JreStringBuilder_appendString(&sb, str);
        }
        break;
    }
    types++;
  }
  va_end(va);
  return JreStringBuilder_toStringAndDealloc(&sb);
}
