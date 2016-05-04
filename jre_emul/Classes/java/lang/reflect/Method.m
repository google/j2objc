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

#import "IOSReflection.h"
#import "J2ObjC_source.h"
#import "java/lang/AssertionError.h"
#import "java/lang/ClassLoader.h"
#import "java/lang/IllegalArgumentException.h"
#import "java/lang/NoSuchMethodException.h"
#import "java/lang/NullPointerException.h"
#import "java/lang/reflect/InvocationTargetException.h"
#import "java/lang/reflect/Method.h"
#import "java/lang/reflect/Modifier.h"
#import "java/lang/reflect/TypeVariable.h"
#import "libcore/reflect/GenericSignatureParser.h"
#import "libcore/reflect/Types.h"

@implementation JavaLangReflectMethod

- (instancetype)initWithMethodSignature:(NSMethodSignature *)methodSignature
                               selector:(SEL)selector
                                  class:(IOSClass *)aClass
                               isStatic:(jboolean)isStatic
                               metadata:(const J2ObjcMethodInfo *)metadata {
  if (self = [super initWithMethodSignature:methodSignature
                                   selector:selector
                                      class:aClass
                                   metadata:metadata]) {
    isStatic_ = isStatic;
  }
  return self;
}

+ (instancetype)methodWithMethodSignature:(NSMethodSignature *)methodSignature
                                 selector:(SEL)selector
                                    class:(IOSClass *)aClass
                                 isStatic:(jboolean)isStatic
                                 metadata:(const J2ObjcMethodInfo *)metadata {
  return [[[JavaLangReflectMethod alloc] initWithMethodSignature:methodSignature
                                                        selector:selector
                                                           class:aClass
                                                        isStatic:isStatic
                                                        metadata:metadata] autorelease];
}

// Returns method name.
- (NSString *)getName {
  NSString *javaName = JreMethodJavaName(metadata_);
  if (javaName) {
    return javaName;
  }

  // Demangle signature to retrieve original method name.
  NSString *name = NSStringFromSelector(selector_);
  NSRange range = [name rangeOfString:@":"];
  if (range.location == NSNotFound) {
    return name;  // It's not mangled.
  }

  // The name ends with the last "WithType" before the first colon.
  range = [name rangeOfString:@"With" options:NSBackwardsSearch
      range:NSMakeRange(0, range.location)];
  if (range.location == NSNotFound) {
    return name;
  }
  return [name substringToIndex:range.location];
}

- (int)getModifiers {
  int mods = JreMethodModifiers(metadata_);
  if (isStatic_) {
    mods |= JavaLangReflectModifier_STATIC;
  }
  return mods;
}

- (IOSClass *)getReturnType {
  id<JavaLangReflectType> returnType = JreMethodReturnType(metadata_);
  if (returnType) {
    if (![returnType isKindOfClass:[IOSClass class]]) {
      return NSObject_class_();
    } else {
      return (IOSClass *) returnType;
    }
  }
  const char *argType = [methodSignature_ methodReturnType];
  if (strlen(argType) != 1) {
    NSString *errorMsg =
        [NSString stringWithFormat:@"unexpected return type: %s", argType];
    id exception = [[JavaLangAssertionError alloc] initWithId:errorMsg];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }
  return decodeTypeEncoding(argType);
}

- (id<JavaLangReflectType>)getGenericReturnType {
  NSString *genericSignature = JreMethodGenericString(metadata_);
  if (genericSignature) {
    LibcoreReflectGenericSignatureParser *parser =
        [[LibcoreReflectGenericSignatureParser alloc]
         initWithJavaLangClassLoader:JavaLangClassLoader_getSystemClassLoader()];
    IOSObjectArray *rawExceptions = [self getExceptionTypes];
    [parser parseForMethodWithJavaLangReflectGenericDeclaration:self
                                                   withNSString:genericSignature
                                              withIOSClassArray:rawExceptions];
    id<JavaLangReflectType> result = [LibcoreReflectTypes getType:parser->returnType_];
    [parser release];
    return result;
  }

  id<JavaLangReflectType> returnType = JreMethodReturnType(metadata_);
  if (returnType) {
    if (returnType && [returnType conformsToProtocol:@protocol(JavaLangReflectTypeVariable)]) {
      return returnType;
    }
  }
  return [self getReturnType];
}

- (id)invokeWithId:(id)object
    withNSObjectArray:(IOSObjectArray *)arguments {
  if (!isStatic_ && object == nil) {
    @throw AUTORELEASE([[JavaLangNullPointerException alloc] initWithNSString:
      @"null object specified for non-final method"]);
  }

  IOSObjectArray *paramTypes = [self getParameterTypes];
  jint nArgs = arguments ? arguments->size_ : 0;
  if (nArgs != paramTypes->size_) {
    @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc] initWithNSString:
        @"wrong number of arguments"]);
  }

  NSInvocation *invocation = [self invocationForTarget:object];
  for (jint i = 0; i < nArgs; i++) {
    J2ObjcRawValue arg;
    if (![paramTypes->buffer_[i] __unboxValue:arguments->buffer_[i] toRawValue:&arg]) {
      @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc] initWithNSString:
          @"argument type mismatch"]);
    }
    [invocation setArgument:&arg atIndex:i + SKIPPED_ARGUMENTS];
  }

  [self invoke:invocation object:object];

  IOSClass *returnType = [self getReturnType];
  if (returnType == [IOSClass voidClass]) {
    return nil;
  }
  J2ObjcRawValue returnValue;
  [invocation getReturnValue:&returnValue];
  return [returnType __boxValue:&returnValue];
}

- (void)jniInvokeWithId:(id)object
                   args:(const J2ObjcRawValue *)args
                 result:(J2ObjcRawValue *)result {
  NSInvocation *invocation = [self invocationForTarget:object];
  for (int i = 0; i < [self getNumParams]; i++) {
    [invocation setArgument:(void *)&args[i] atIndex:i + SKIPPED_ARGUMENTS];
  }

  [self invoke:invocation object:object];

  if (result) {
    [invocation getReturnValue:result];
  }
}

- (NSInvocation *)invocationForTarget:(id)object {
  NSInvocation *invocation =
      [NSInvocation invocationWithMethodSignature:methodSignature_];
  [invocation setSelector:selector_];
  if (object == nil || [object isKindOfClass:[IOSClass class]]) {
    [invocation setTarget:class_.objcClass];
  } else {
    [invocation setTarget:object];
  }
  return invocation;
}

- (void)invoke:(NSInvocation *)invocation object:(id)object {
  IOSClass *declaringClass = [self getDeclaringClass];
  NSException *exception = nil;
  if (object &&
      (JreMethodModifiers(metadata_) & JavaLangReflectModifier_PRIVATE) > 0 &&
      declaringClass != [object getClass]) {
    // A superclass's private instance method is invoked, so temporarily
    // change the object's type to the superclass.
    Class originalClass = object_setClass(object, declaringClass.objcClass);
    @try {
      [invocation invoke];
    }
    @catch (NSException *t) {
      exception = t;
    }
    object_setClass(object, originalClass);
  } else {
    @try {
      [invocation invoke];
    }
    @catch (NSException *t) {
      exception = t;
    }
  }
  if (exception) {
    @throw AUTORELEASE([[JavaLangReflectInvocationTargetException alloc]
                        initWithNSException:exception]);
  }
}

- (NSString *)description {
  NSMutableString *s = [NSMutableString string];
  NSString *modifiers =
      JavaLangReflectModifier_toStringWithInt_(JreMethodModifiers(metadata_));
  NSString *returnType = [[self getReturnType] getName];
  NSString *declaringClass = [[self getDeclaringClass] getName];
  [s appendFormat:@"%@ %@ %@.%@(", modifiers, returnType, declaringClass, [self getName]];
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

- (id)getDefaultValue {
  if ([self->class_ isAnnotation]) {
    // Invoke the class method for this method name plus "Default". For example, if this
    // method is named "foo", then return the result from "fooDefault".
    NSString *defaultName = [[self getName] stringByAppendingString:@"Default"];
    Class cls = class_.objcClass;
    Method defaultValueMethod = JreFindClassMethod(cls, [defaultName UTF8String]);
    if (defaultValueMethod) {
      struct objc_method_description *methodDesc = method_getDescription(defaultValueMethod);
      NSInvocation *invocation = [NSInvocation invocationWithMethodSignature:
          [NSMethodSignature signatureWithObjCTypes:methodDesc->types]];
      [invocation setSelector:methodDesc->name];
      [invocation invokeWithTarget:cls];
      J2ObjcRawValue returnValue;
      [invocation getReturnValue:&returnValue];
      return [[self getReturnType] __boxValue:&returnValue];
    }
  }
  return nil;
}

// A method's hash is the hash of its declaring class's name XOR its name.
- (NSUInteger)hash {
  return [[class_ getName] hash] ^ [[self getName] hash];
}

+ (const J2ObjcClassInfo *)__metadata {
  static const J2ObjcMethodInfo methods[] = {
    { "getName", NULL, "Ljava.lang.String;", 0x1, NULL, NULL },
    { "getModifiers", NULL, "I", 0x1, NULL, NULL },
    { "getReturnType", NULL, "Ljava.lang.Class;", 0x1, NULL, NULL },
    { "getGenericReturnType", NULL, "Ljava.lang.reflect.Type;", 0x1, NULL, NULL },
    { "getDeclaringClass", NULL, "Ljava.lang.Class;", 0x1, NULL, "()Ljava/lang/Class<*>;" },
    { "getParameterTypes", NULL, "[Ljava.lang.Class;", 0x1, NULL, NULL },
    { "getGenericParameterTypes", NULL, "[Ljava.lang.reflect.Type;", 0x1, NULL, NULL },
    { "invokeWithId:withNSObjectArray:", "invoke", "Ljava.lang.Object;", 0x81,
      "Ljava.lang.IllegalAccessException;Ljava.lang.IllegalArgumentException;"
      "Ljava.lang.reflect.InvocationTargetException;", NULL },
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
    { "getDefaultValue", NULL, "Ljava.lang.Object;", 0x1, NULL, NULL },
    { "init", NULL, NULL, 0x1, NULL, NULL },
  };
  static const J2ObjcClassInfo _JavaLangReflectMethod = {
    2, "Method", "java.lang.reflect", NULL, 0x1, 20, methods, 0, NULL, 0, NULL, 0, NULL, NULL, NULL
  };
  return &_JavaLangReflectMethod;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(JavaLangReflectMethod)
