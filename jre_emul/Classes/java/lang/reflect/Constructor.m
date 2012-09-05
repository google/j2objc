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
//  Constructor.m
//  JreEmulation
//
//  Created by Tom Ball on 11/11/11.
//

#import "Constructor.h"
#import "java/lang/AssertionError.h"
#import "java/lang/Boolean.h"
#import "java/lang/Byte.h"
#import "java/lang/Character.h"
#import "java/lang/Double.h"
#import "java/lang/ExceptionInInitializerError.h"
#import "java/lang/Float.h"
#import "java/lang/IllegalArgumentException.h"
#import "java/lang/Integer.h"
#import "java/lang/Long.h"
#import "java/lang/Short.h"
#import "java/lang/Throwable.h"
#import "java/lang/reflect/InvocationTargetException.h"

#import <objc/runtime.h>

static id makeException(Class exceptionClass) {
#if __has_feature(objc_arc)
    return [[exceptionClass alloc] init];
#else
    return [[[exceptionClass alloc] init] autorelease];
#endif
}

@implementation JavaLangReflectConstructor

+ (id)constructorWithSelector:(SEL)aSelector withClass:(IOSClass *)aClass {
  id c = [[JavaLangReflectConstructor alloc] initWithSelector:aSelector
                                                    withClass:aClass];
#if ! __has_feature(objc_arc)
  [c autorelease];
#endif
  return c;
}

- (id)newInstanceWithNSObjectArray:(IOSObjectArray *)initArgs {
  id newInstance;
  @try {
    newInstance = [class_ alloc];
  }
  @catch (JavaLangThrowable *e) {
    JavaLangThrowable *throwable =
        [[JavaLangExceptionInInitializerError alloc]
            initWithJavaLangThrowable:e];
#if !__has_feature(objc_arc)
    [throwable autorelease];
#endif
    @throw throwable;
  }

  NSInvocation *invocation =
      [NSInvocation invocationWithMethodSignature:methodSignature_];
  [invocation setTarget:newInstance];
  [invocation setSelector:selector_];

  IOSObjectArray *parameterTypes = [self getParameterTypes];
  if ([initArgs count] != [parameterTypes count]) {
    @throw makeException([JavaLangIllegalArgumentException class]);
  }

  int count = [initArgs count];
  for (int i = 0; i < count; i++) {
    int argIndex = i + 2;  // Add 2 to account for self and _cmd.

    IOSClass *type = [parameterTypes objectAtIndex:i];
    id arg = [initArgs objectAtIndex:i];

    if ([type.objcClass isEqual:[NSObject class]] ||
        [type.objcClass isEqual:[IOSClass class]]) {
      [invocation setArgument:&arg atIndex:argIndex];

    } else if ([type.objcClass isEqual:[JavaLangByte class]]) {
      if (![arg isKindOfClass:[JavaLangByte class]]) {
        @throw makeException([JavaLangIllegalArgumentException class]);
      }

      char primitiveArg = [arg byteValue];
      [invocation setArgument:&primitiveArg atIndex:argIndex];

    } else if ([type.objcClass isEqual:[JavaLangCharacter class]]) {
      if (![arg isKindOfClass:[JavaLangCharacter class]]) {
        @throw makeException([JavaLangIllegalArgumentException class]);
      }

      unichar primitiveArg = [(JavaLangCharacter *)arg charValue];
      [invocation setArgument:&primitiveArg atIndex:argIndex];

    } else if ([type.objcClass isEqual:[JavaLangShort class]]) {
      if (![arg isKindOfClass:[JavaLangShort class]]) {
        @throw makeException([JavaLangIllegalArgumentException class]);
      }

      short primitiveArg = [arg byteValue];
      [invocation setArgument:&primitiveArg atIndex:argIndex];

    } else if ([type.objcClass isEqual:[JavaLangInteger class]]) {
      if (![arg isKindOfClass:[JavaLangInteger class]]) {
        @throw makeException([JavaLangIllegalArgumentException class]);
      }

      int primitiveArg = [arg intValue];
      [invocation setArgument:&primitiveArg atIndex:argIndex];

    } else if ([type.objcClass isEqual:[JavaLangLong class]]) {
      if (![arg isKindOfClass:[JavaLangLong class]]) {
        @throw makeException([JavaLangIllegalArgumentException class]);
      }

      long primitiveArg = [arg longValue];
      [invocation setArgument:&primitiveArg atIndex:argIndex];

    } else if ([type.objcClass isEqual:[JavaLangFloat class]]) {
      if (![arg isKindOfClass:[JavaLangFloat class]]) {
        @throw makeException([JavaLangIllegalArgumentException class]);
      }

      float primitiveArg = [arg floatValue];
      [invocation setArgument:&primitiveArg atIndex:argIndex];

    } else if ([type.objcClass isEqual:[JavaLangDouble class]]) {
      if (![arg isKindOfClass:[JavaLangDouble class]]) {
        @throw makeException([JavaLangIllegalArgumentException class]);
      }

      double primitiveArg = [arg doubleValue];
      [invocation setArgument:&primitiveArg atIndex:argIndex];

    } else if ([type.objcClass isEqual:[JavaLangBoolean class]]) {
      if (![arg isKindOfClass:[JavaLangBoolean class]]) {
        @throw makeException([JavaLangIllegalArgumentException class]);
      }

      BOOL primitiveArg = [arg booleanValue];
      [invocation setArgument:&primitiveArg atIndex:argIndex];

    } else {
      // Only remaining case is JavaLangVoid, which can't be a constructor
      // argument.
      @throw makeException([JavaLangIllegalArgumentException class]);
    }
  }

  @try {
    [invocation invoke];
  }
  @catch (JavaLangThrowable *e) {
    JavaLangThrowable *throwable =
        [[JavaLangReflectInvocationTargetException alloc]
            initWithJavaLangThrowable:e];
#if !__has_feature(objc_arc)
    [throwable autorelease];
#endif
    @throw throwable;
  }

#if !__has_feature(objc_arc)
  [newInstance autorelease];
#endif

  return newInstance;
}

- (NSString *)getName {
  const char *cname = class_getName(class_);
  NSString *name = [NSString stringWithCString:cname
                                      encoding:NSUTF8StringEncoding];
  return name;
}

@end
