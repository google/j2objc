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
#import "IOSReflection.h"
#import "J2ObjC_source.h"
#import "NSException+JavaThrowable.h"
#import "java/lang/AssertionError.h"
#import "java/lang/ExceptionInInitializerError.h"
#import "java/lang/IllegalArgumentException.h"
#import "java/lang/reflect/InvocationTargetException.h"
#import "java/lang/reflect/Method.h"
#import "java/lang/reflect/Modifier.h"

#import <objc/runtime.h>

@implementation JavaLangReflectConstructor

+ (instancetype)constructorWithMethodSignature:(NSMethodSignature *)methodSignature
                                      selector:(SEL)selector
                                         class:(IOSClass *)aClass
                                      metadata:(const J2ObjcMethodInfo *)metadata {
  return [[[JavaLangReflectConstructor alloc] initWithMethodSignature:methodSignature
                                                             selector:selector
                                                                class:aClass
                                                             metadata:metadata] autorelease];
}

- (id)newInstanceWithNSObjectArray:(IOSObjectArray *)initArgs {
  id newInstance = [self allocInstance];
  NSInvocation *invocation = [self invocationForTarget:newInstance];

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

  [self invoke:invocation];

  return newInstance;
}

- (id)jniNewInstance:(const J2ObjcRawValue *)args {
  id newInstance = [self allocInstance];
  NSInvocation *invocation = [self invocationForTarget:newInstance];
  for (int i = 0; i < [self getNumParams]; i++) {
    [invocation setArgument:(void *)&args[i] atIndex:i + SKIPPED_ARGUMENTS];
  }
  [self invoke:invocation];
  return newInstance;
}

- (id)allocInstance {
  id newInstance;
  @try {
    newInstance = AUTORELEASE([class_.objcClass alloc]);
  }
  @catch (NSException *e) {
    @throw AUTORELEASE([[JavaLangExceptionInInitializerError alloc] initWithNSException:e]);
  }
  return newInstance;
}

- (NSInvocation *)invocationForTarget:(id)object {
  NSInvocation *invocation =
      [NSInvocation invocationWithMethodSignature:methodSignature_];
  [invocation setSelector:selector_];
  [invocation setTarget:object];
  return invocation;
}

- (void)invoke:(NSInvocation *)invocation {
  @try {
    [invocation invoke];
  }
  @catch (NSException *e) {
    @throw AUTORELEASE(
        [[JavaLangReflectInvocationTargetException alloc] initWithNSException:e]);
  }
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
  NSString *modifiers =
      JavaLangReflectModifier_toStringWithInt_(JreMethodModifiers(metadata_));
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
    { "getName", NULL, "Ljava.lang.String;", 0x1, NULL, NULL },
    { "getModifiers", NULL, "I", 0x1, NULL, NULL },
    { "getDeclaringClass", NULL, "Ljava.lang.Class;", 0x1, NULL, "()Ljava/lang/Class<TT;>;" },
    { "getParameterTypes", NULL, "[Ljava.lang.Class;", 0x1, NULL, NULL },
    { "getGenericParameterTypes", NULL, "[Ljava.lang.reflect.Type;", 0x1, NULL, NULL },
    { "newInstanceWithNSObjectArray:", "newInstance", "TT;", 0x81,
      "Ljava.lang.InstantiationException;Ljava.lang.IllegalAccessException;"
      "Ljava.lang.IllegalArgumentException;Ljava.lang.reflect.InvocationTargetException;",
      "([Ljava/lang/Object;)TT;" },
    { "getAnnotationWithIOSClass:", "getAnnotation", "TT;", 0x1, NULL,
      "<T::Ljava/lang/annotation/Annotation;>(Ljava/lang/Class<TT;>;)TT;" },
    { "getDeclaredAnnotations", NULL, "[Ljava.lang.annotation.Annotation;", 0x1, NULL, NULL },
    { "getParameterAnnotations", NULL, "[[Ljava.lang.annotation.Annotation;", 0x1, NULL, NULL },
    { "getTypeParameters", NULL, "[Ljava.lang.reflect.TypeVariable;", 0x1, NULL, NULL },
    { "isSynthetic", NULL, "Z", 0x1, NULL, NULL },
    { "getExceptionTypes", NULL, "[Ljava.lang.Class;", 0x1, NULL, NULL },
    { "getGenericExceptionTypes", NULL, "[Ljava.lang.reflect.Type;", 0x1, NULL, NULL },
    { "toGenericString", NULL, "Ljava.lang.String;", 0x1, NULL, NULL },
    { "isBridge", NULL, "Z", 0x1, NULL, NULL },
    { "isVarArgs", NULL, "Z", 0x1, NULL, NULL },
    { "init", NULL, NULL, 0x1, NULL, NULL },
  };
  static const J2ObjcClassInfo _JavaLangReflectConstructor = {
    2, "Constructor", "java.lang.reflect", NULL, 0x1, 17, methods, 0, NULL, 0, NULL, 0, NULL, NULL,
    "<T:Ljava/lang/Object;>Ljava/lang/reflect/AccessibleObject;"
    "Ljava/lang/reflect/GenericDeclaration;Ljava/lang/reflect/Member;"
  };
  return &_JavaLangReflectConstructor;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(JavaLangReflectConstructor)
