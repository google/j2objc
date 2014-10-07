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
#import "JavaMetadata.h"
#import "java/lang/AssertionError.h"
#import "java/lang/ExceptionInInitializerError.h"
#import "java/lang/IllegalArgumentException.h"
#import "java/lang/Throwable.h"
#import "java/lang/reflect/InvocationTargetException.h"
#import "java/lang/reflect/Method.h"

#import <objc/runtime.h>

@implementation JavaLangReflectConstructor

+ (instancetype)constructorWithMethodSignature:(NSMethodSignature *)methodSignature
                                      selector:(SEL)selector
                                         class:(IOSClass *)aClass
                                      metadata:(JavaMethodMetadata *)metadata {
  return [[[JavaLangReflectConstructor alloc] initWithMethodSignature:methodSignature
                                                             selector:selector
                                                                class:aClass
                                                             metadata:metadata] autorelease];
}

- (id)newInstanceWithNSObjectArray:(IOSObjectArray *)initArgs {
  id newInstance;
  @try {
    newInstance = AUTORELEASE([class_.objcClass alloc]);
  }
  @catch (JavaLangThrowable *e) {
    @throw AUTORELEASE([[JavaLangExceptionInInitializerError alloc] initWithJavaLangThrowable:e]);
  }

  NSInvocation *invocation =
      [NSInvocation invocationWithMethodSignature:methodSignature_];
  [invocation setTarget:newInstance];
  [invocation setSelector:selector_];

  IOSObjectArray *parameterTypes = [self getParameterTypes];
  if (initArgs->size_ != parameterTypes->size_) {
    @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc] initWithNSString:
        @"wrong number of arguments"]);
  }

  jint count = initArgs->size_;
  for (jint i = 0; i < count; i++) {
    J2ObjcRawValue arg;
    if (![parameterTypes->buffer_[i] __unboxValue:initArgs->buffer_[i] toRawValue:&arg]) {
      @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc] initWithNSString:
          @"argument type mismatch"]);
    }
    [invocation setArgument:&arg atIndex:i + SKIPPED_ARGUMENTS];
  }

  @try {
    [invocation invoke];
  }
  @catch (JavaLangThrowable *e) {
    @throw AUTORELEASE(
        [[JavaLangReflectInvocationTargetException alloc] initWithJavaLangThrowable:e]);
  }

  return newInstance;
}

// Returns the class name, like java.lang.reflect.Constructor does.
- (NSString *)getName {
  return [class_ getName];
}

- (NSString *)internalName {
  return [class_ objcName];
}

@end
