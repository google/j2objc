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
//  Method.m
//  JreEmulation
//
//  Created by Tom Ball on 11/07/11.
//

#import "IOSClass.h"
#import "IOSObjectArray.h"
#import "JreEmulation.h"
#import "java/lang/AssertionError.h"
#import "java/lang/Boolean.h"
#import "java/lang/Byte.h"
#import "java/lang/Character.h"
#import "java/lang/Double.h"
#import "java/lang/Float.h"
#import "java/lang/Integer.h"
#import "java/lang/Long.h"
#import "java/lang/NullPointerException.h"
#import "java/lang/Short.h"
#import "java/lang/Void.h"
#import "java/lang/reflect/Method.h"

@implementation JavaLangReflectMethod

typedef union {
  void *asId;
  char asChar;
  unichar asUnichar;
  short asShort;
  int asInt;
  long long asLong;
  float asFloat;
  double asDouble;
  BOOL asBOOL;
} JavaResult;

static id Box(JavaResult *value, const char *type);

+ (id)methodWithSelector:(SEL)aSelector withClass:(IOSClass *)aClass {
  id method = [[JavaLangReflectMethod alloc] initWithSelector:aSelector
                                                    withClass:aClass];
#if ! __has_feature(objc_arc)
  [method autorelease];
#endif
  return method;
}

- (NSString *)getName {
  return NSStringFromSelector(selector_);
}

- (IOSClass *)getReturnType {
  const char *argType = [methodSignature_ methodReturnType];
  if (strlen(argType) != 1) {
    NSString *errorMsg =
        [NSString stringWithFormat:@"unexpected return type: %s", argType];
    id exception = [[JavaLangAssertionError alloc] initWithNSString:errorMsg];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }
  return decodeTypeEncoding(*argType);
}

- (id)invokeWithId:(id)object
       withNSObjectArray:(IOSObjectArray *)arguments {
  if (!classMethod_ && object == nil) {
    id exception =
        [[JavaLangNullPointerException alloc]
            initWithNSString:@"null object specified for non-final method"];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }

  NSInvocation *invocation =
      [NSInvocation invocationWithMethodSignature:methodSignature_];
  [invocation setSelector:selector_];
  int nArgs = [arguments count];
  for (NSUInteger i = 0; i < nArgs; i++) {
    NSObject *arg = [arguments objectAtIndex:i];
    [invocation setArgument:&arg atIndex:i + SKIPPED_ARGUMENTS];
  }
  if (object == nil) {
    [invocation setTarget:class_];
  } else {
    [invocation setTarget:object];
  }

  [invocation invoke];
  const char *returnType = [methodSignature_ methodReturnType];
  if (*returnType != 'v') {  // if not void
    JavaResult returnValue;
    [invocation getReturnValue:&returnValue];
    return Box(&returnValue, returnType);
  } else {
    return nil;
  }
}

- (NSString *)description {
  NSString *kind = classMethod_ ? @"+" : @"-";
  const char *argType = [methodSignature_ methodReturnType];
  NSString *returnType = [NSString stringWithUTF8String:argType];
  NSString *result = [NSString stringWithFormat:@"%@ %@ %@(", kind,
                      describeTypeEncoding(returnType), [self getName]];

  NSUInteger nArgs = [methodSignature_ numberOfArguments] - SKIPPED_ARGUMENTS;
  for (NSUInteger i = 0; i < nArgs; i++) {
    const char *argType =
        [methodSignature_ getArgumentTypeAtIndex:i + SKIPPED_ARGUMENTS];
    NSString *paramEncoding = [NSString stringWithUTF8String:argType];
    result = [result stringByAppendingFormat:@"%@",
                  describeTypeEncoding(paramEncoding)];
    if (i + 1 < nArgs) {
      result = [result stringByAppendingString:@", "];
    }
  }
  return [result stringByAppendingString:@")"];
}

// Return a wrapper object for a value with a specified Obj-C type encoding.
id Box(JavaResult *value, const char *type) {
  if (strlen(type) == 1) {
    char typeChar = *type;
    switch (typeChar) {
      case '@':
        return (ARCBRIDGE id)value->asId;
      case '#':
      return [IOSClass classWithClass:(ARCBRIDGE Class)value];
      case 'c':
        return [JavaLangByte valueOfWithChar:value->asChar];
      case 'S':
        // A Java character is an unsigned two-byte int; in other words,
        // an unsigned short with an encoding of 'S'.
        return [JavaLangCharacter valueOfWithUnichar:value->asUnichar];
      case 's':
        return [JavaLangShort valueOfWithShortInt:value->asShort];
      case 'i':
        return [JavaLangInteger valueOfWithInt:value->asInt];
      case 'l':
      case 'L':
      case 'q':
      case 'Q':
        return [JavaLangLong valueOfWithLongInt:value->asLong];
      case 'f':
        return [JavaLangFloat valueOfWithFloat:value->asFloat];
      case 'd':
        return [JavaLangDouble valueOfWithDouble:value->asDouble];
      case 'B':
        return [JavaLangBoolean valueOfWithBOOL:value->asBOOL];
    }
  }
  id exception =
      [[JavaLangAssertionError alloc] initWithNSString:
          [NSString stringWithFormat:@"unknown Java type encoding: %s", type]];
#if ! __has_feature(objc_arc)
  [exception autorelease];
#endif
  @throw exception;
}

@end
