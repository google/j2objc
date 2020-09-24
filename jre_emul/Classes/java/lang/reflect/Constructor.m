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
#import "java/lang/AssertionError.h"
#import "java/lang/IllegalArgumentException.h"
#import "java/lang/Throwable.h"
#import "java/lang/reflect/InvocationTargetException.h"
#import "java/lang/reflect/Method.h"
#import "java/lang/reflect/Modifier.h"

#import <objc/runtime.h>

@implementation JavaLangReflectConstructor

+ (instancetype)constructorWithDeclaringClass:(IOSClass *)aClass
                                     metadata:(const J2ObjcMethodInfo *)metadata {
  return AUTORELEASE([[JavaLangReflectConstructor alloc] initWithDeclaringClass:aClass
                                                                       metadata:metadata]);
}

static id NewInstance(JavaLangReflectConstructor *self, void (^fillArgs)(NSInvocation *)) {
  SEL selector = self->metadata_->selector;
  Class cls = self->class_.objcClass;
  bool isFactory = false;
  Method method = JreFindInstanceMethod(cls, selector);
  if (!method) {
    // Special case for constructors declared as class methods.
    method = JreFindClassMethod(cls, selector);
    isFactory = true;
  }
  NSInvocation *invocation = [NSInvocation invocationWithMethodSignature:
      [NSMethodSignature signatureWithObjCTypes:method_getTypeEncoding(method)]];
  [invocation setSelector:selector];
  fillArgs(invocation);
  id newInstance;
  @try {
    if (isFactory) {
      [invocation invokeWithTarget:cls];
      [invocation getReturnValue:&newInstance];
    } else {
      newInstance = AUTORELEASE([cls alloc]);
      [invocation invokeWithTarget:newInstance];
    }
  }
  @catch (JavaLangThrowable *e) {
    @throw create_JavaLangReflectInvocationTargetException_initWithJavaLangThrowable_(e);
  }
  return newInstance;
}

- (id)newInstanceWithNSObjectArray:(IOSObjectArray *)initArgs {
  jint argCount = initArgs ? initArgs->size_ : 0;
  IOSObjectArray *parameterTypes = [self getParameterTypesInternal];
  if (argCount != parameterTypes->size_) {
    @throw create_JavaLangIllegalArgumentException_initWithNSString_(@"wrong number of arguments");
  }

  return NewInstance(self, ^(NSInvocation *invocation) {
    for (jint i = 0; i < argCount; i++) {
      J2ObjcRawValue arg;
      if (![parameterTypes->buffer_[i] __unboxValue:initArgs->buffer_[i] toRawValue:&arg]) {
        @throw create_JavaLangIllegalArgumentException_initWithNSString_(@"argument type mismatch");
      }
      [invocation setArgument:&arg atIndex:i + SKIPPED_ARGUMENTS];
    }
  });
}

- (id)jniNewInstance:(const J2ObjcRawValue *)args {
  return NewInstance(self, ^(NSInvocation *invocation) {
    for (int i = 0; i < [self getParameterTypesInternal]->size_; i++) {
      [invocation setArgument:(void *)&args[i] atIndex:i + SKIPPED_ARGUMENTS];
    }
  });
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
  NSString *modifiers = JavaLangReflectModifier_toStringWithInt_(metadata_->modifiers);
  NSString *type = [[self getDeclaringClass] getName];
  [s appendFormat:@"%@ %@(", modifiers, type];
  IOSObjectArray *params = [self getParameterTypesInternal];
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
  static J2ObjcMethodInfo methods[] = {
    { NULL, NULL, 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "I", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "LIOSClass;", 0x1, -1, -1, -1, 0, -1, -1 },
    { NULL, "[LIOSClass;", 0x1, -1, -1, -1, 1, -1, -1 },
    { NULL, "[LJavaLangReflectType;", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "LNSObject;", 0x81, 2, 3, 4, 5, -1, -1 },
    { NULL, "LJavaLangAnnotationAnnotation;", 0x1, 6, 7, -1, 8, -1, -1 },
    { NULL, "[LJavaLangAnnotationAnnotation;", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "[[LJavaLangAnnotationAnnotation;", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "[LJavaLangReflectTypeVariable;", 0x1, -1, -1, -1, 9, -1, -1 },
    { NULL, "Z", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "[LIOSClass;", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "[LJavaLangReflectType;", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "Z", 0x1, -1, -1, -1, -1, -1, -1 },
  };
  #pragma clang diagnostic push
  #pragma clang diagnostic ignored "-Wobjc-multiple-method-names"
  methods[0].selector = @selector(init);
  methods[1].selector = @selector(getName);
  methods[2].selector = @selector(getModifiers);
  methods[3].selector = @selector(getDeclaringClass);
  methods[4].selector = @selector(getParameterTypes);
  methods[5].selector = @selector(getGenericParameterTypes);
  methods[6].selector = @selector(newInstanceWithNSObjectArray:);
  methods[7].selector = @selector(getAnnotationWithIOSClass:);
  methods[8].selector = @selector(getDeclaredAnnotations);
  methods[9].selector = @selector(getParameterAnnotations);
  methods[10].selector = @selector(getTypeParameters);
  methods[11].selector = @selector(isSynthetic);
  methods[12].selector = @selector(getExceptionTypes);
  methods[13].selector = @selector(getGenericExceptionTypes);
  methods[14].selector = @selector(toGenericString);
  methods[15].selector = @selector(isVarArgs);
  #pragma clang diagnostic pop
  static const void *ptrTable[] = {
    "()Ljava/lang/Class<TT;>;", "()[Ljava/lang/Class<*>;", "newInstance", "[LNSObject;",
    "LJavaLangInstantiationException;LJavaLangIllegalAccessException;"
    "LJavaLangIllegalArgumentException;LJavaLangReflectInvocationTargetException;",
    "([Ljava/lang/Object;)TT;", "getAnnotation", "LIOSClass;",
    "<T::Ljava/lang/annotation/Annotation;>(Ljava/lang/Class<TT;>;)TT;",
    "()[Ljava/lang/reflect/TypeVariable<Ljava/lang/reflect/Method;>;",
    "<T:Ljava/lang/Object;>Ljava/lang/reflect/AccessibleObject;"
    "Ljava/lang/reflect/GenericDeclaration;Ljava/lang/reflect/Member;" };
  static const J2ObjcClassInfo _JavaLangReflectConstructor = {
    "Constructor", "java.lang.reflect", ptrTable, methods, NULL, 7, 0x1, 16, 0, -1, -1, -1, 10, -1
  };
  return &_JavaLangReflectConstructor;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(JavaLangReflectConstructor)
