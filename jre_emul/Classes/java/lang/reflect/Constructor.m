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
#import "J2ObjC_source.h"
#import "JavaMetadata.h"
#import "java/lang/AssertionError.h"
#import "java/lang/ExceptionInInitializerError.h"
#import "java/lang/IllegalArgumentException.h"
#import "java/lang/Throwable.h"
#import "java/lang/reflect/InvocationTargetException.h"
#import "java/lang/reflect/Method.h"
#import "java/lang/reflect/Modifier.h"

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

  jint argCount = initArgs ? initArgs->size_ : 0;
  IOSObjectArray *parameterTypes = [self getParameterTypes];
  if (argCount != parameterTypes->size_) {
    @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc] initWithNSString:
        @"wrong number of arguments"]);
  }

  for (jint i = 0; i < argCount; i++) {
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

// A constructor's hash is the hash of its declaring class's name.
- (NSUInteger)hash {
  return [[class_ getName] hash];
}

- (NSString *)description {
  NSMutableString *s = [NSMutableString string];
  NSString *modifiers = JavaLangReflectModifier_toStringWithInt_([self getModifiers]);
  NSString *type = [[self getDeclaringClass] getName];
  [s appendFormat:@"%@ %@(", modifiers, type];
  IOSObjectArray *params = [self getParameterTypes];
  jint n = params->size_;
  if (n > 0) {
    [s appendString:[(IOSClass *) params->buffer_[0] getName]];
    for (jint i = 1; i < n; i++) {
      [s appendFormat:@",%@", [(IOSClass *) params->buffer_[i] getName]];
    }
  }
  [s appendString:@")"];
  IOSObjectArray *throws = [self getExceptionTypes];
  n = throws->size_;
  if (n > 0) {
    [s appendFormat:@" throws %@", [(IOSClass *) throws->buffer_[0] getName]];
    for (jint i = 1; i < n; i++) {
      [s appendFormat:@",%@", [(IOSClass *) throws->buffer_[i] getName]];
    }
  }
  return [s description];
}

+ (const J2ObjcClassInfo *)__metadata {
  static const J2ObjcMethodInfo methods[] = {
    { "getName", NULL, "Ljava.lang.String;", 0x1, NULL },
    { "getModifiers", NULL, "I", 0x1, NULL },
    { "getDeclaringClass", NULL, "Ljava.lang.Class;", 0x1, NULL },
    { "getParameterTypes", NULL, "[Ljava.lang.Class;", 0x1, NULL },
    { "getGenericParameterTypes", NULL, "[Ljava.lang.reflect.Type;", 0x1, NULL },
    { "newInstanceWithNSObjectArray:", "newInstance", "TT;", 0x81, "Ljava.lang.InstantiationException;Ljava.lang.IllegalAccessException;Ljava.lang.IllegalArgumentException;Ljava.lang.reflect.InvocationTargetException;" },
    { "getAnnotationWithIOSClass:", "getAnnotation", "TT;", 0x1, NULL },
    { "getDeclaredAnnotations", NULL, "[Ljava.lang.annotation.Annotation;", 0x1, NULL },
    { "getParameterAnnotations", NULL, "[[Ljava.lang.annotation.Annotation;", 0x1, NULL },
    { "getTypeParameters", NULL, "[Ljava.lang.reflect.TypeVariable;", 0x1, NULL },
    { "isSynthetic", NULL, "Z", 0x1, NULL },
    { "getExceptionTypes", NULL, "[Ljava.lang.Class;", 0x1, NULL },
    { "getGenericExceptionTypes", NULL, "[Ljava.lang.reflect.Type;", 0x1, NULL },
    { "toGenericString", NULL, "Ljava.lang.String;", 0x1, NULL },
    { "isBridge", NULL, "Z", 0x1, NULL },
    { "isVarArgs", NULL, "Z", 0x1, NULL },
    { "init", NULL, NULL, 0x1, NULL },
  };
  static const J2ObjcClassInfo _JavaLangReflectConstructor = {
    1, "Constructor", "java.lang.reflect", NULL, 0x1, 17, methods, 0, NULL, 0, NULL
  };
  return &_JavaLangReflectConstructor;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(JavaLangReflectConstructor)
