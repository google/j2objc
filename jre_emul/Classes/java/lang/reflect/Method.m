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

#import "J2ObjC_source.h"
#import "JavaMetadata.h"
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
                               isStatic:(BOOL)isStatic
                               metadata:(JavaMethodMetadata *)metadata {
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
                                 isStatic:(BOOL)isStatic
                                 metadata:(JavaMethodMetadata *)metadata {
  return [[[JavaLangReflectMethod alloc] initWithMethodSignature:methodSignature
                                                        selector:selector
                                                           class:aClass
                                                        isStatic:isStatic
                                                        metadata:metadata] autorelease];
}

// Returns method name.
- (NSString *)getName {
  NSString *javaName = [metadata_ javaName];
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
  int mods = [super getModifiers];
  if (isStatic_) {
    mods |= JavaLangReflectModifier_STATIC;
  }
  return mods;
}

- (IOSClass *)getReturnType {
  id<JavaLangReflectType> returnType = [metadata_ returnType];
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
    id exception = [[JavaLangAssertionError alloc] initWithNSString:errorMsg];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }
  return decodeTypeEncoding(argType);
}

- (id<JavaLangReflectType>)getGenericReturnType {
  NSString *genericSignature = [metadata_ genericSignature];
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

  id<JavaLangReflectType> returnType = [metadata_ returnType];
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

  NSInvocation *invocation =
      [NSInvocation invocationWithMethodSignature:methodSignature_];
  [invocation setSelector:selector_];
  for (jint i = 0; i < nArgs; i++) {
    J2ObjcRawValue arg;
    if (![paramTypes->buffer_[i] __unboxValue:arguments->buffer_[i] toRawValue:&arg]) {
      @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc] initWithNSString:
          @"argument type mismatch"]);
    }
    [invocation setArgument:&arg atIndex:i + SKIPPED_ARGUMENTS];
  }
  if (object == nil || [object isKindOfClass:[IOSClass class]]) {
    [invocation setTarget:class_.objcClass];
  } else {
    [invocation setTarget:object];
  }

  IOSClass *declaringClass = [self getDeclaringClass];
  JavaLangThrowable *exception = nil;
  if (object &&
      ([self getModifiers] & JavaLangReflectModifier_PRIVATE) > 0 &&
      declaringClass != [object getClass]) {
    // A superclass's private instance method is invoked, so temporarily
    // change the object's type to the superclass.
    Class originalClass = object_setClass(object, declaringClass.objcClass);
    @try {
      [invocation invoke];
    }
    @catch (JavaLangThrowable *t) {
      exception = t;
    }
    object_setClass(object, originalClass);
  } else {
    @try {
      [invocation invoke];
    }
    @catch (JavaLangThrowable *t) {
      exception = t;
    }
  }
  if (exception) {
    @throw AUTORELEASE([[JavaLangReflectInvocationTargetException alloc]
                        initWithJavaLangThrowable:exception]);
  }
  IOSClass *returnType = [self getReturnType];
  if (returnType == [IOSClass voidClass]) {
    return nil;
  }
  J2ObjcRawValue returnValue;
  [invocation getReturnValue:&returnValue];
  return [returnType __boxValue:&returnValue];
}

- (NSString *)description {
  NSMutableString *s = [NSMutableString string];
  NSString *modifiers = JavaLangReflectModifier_toStringWithInt_([self getModifiers]);
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
    // method is named "foo", then return the result from "fooDefault()".
    NSString *defaultName = [[self getName] stringByAppendingString:@"Default"];
    @try {
      JavaLangReflectMethod *defaultMethod =
          [self->class_ getDeclaredMethod:defaultName
                   parameterTypes:[IOSObjectArray arrayWithLength:0 type:IOSClass_class_()]];
      if (defaultMethod) {
        return [defaultMethod invokeWithId:self->class_
                         withNSObjectArray:[IOSObjectArray arrayWithLength:0
                                                                      type:NSObject_class_()]];
      }
    }
    @catch (JavaLangNoSuchMethodException *exception) {
      // Fall-through.
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
    { "getName", NULL, "Ljava.lang.String;", 0x1, NULL },
    { "getModifiers", NULL, "I", 0x1, NULL },
    { "getReturnType", NULL, "Ljava.lang.Class;", 0x1, NULL },
    { "getGenericReturnType", NULL, "Ljava.lang.reflect.Type;", 0x1, NULL },
    { "getDeclaringClass", NULL, "Ljava.lang.Class;", 0x1, NULL },
    { "getParameterTypes", NULL, "[Ljava.lang.Class;", 0x1, NULL },
    { "getGenericParameterTypes", NULL, "[Ljava.lang.reflect.Type;", 0x1, NULL },
    { "invokeWithId:withNSObjectArray:", "invoke", "Ljava.lang.Object;", 0x81, "Ljava.lang.IllegalAccessException;Ljava.lang.IllegalArgumentException;Ljava.lang.reflect.InvocationTargetException;" },
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
    { "getDefaultValue", NULL, "Ljava.lang.Object;", 0x1, NULL },
    { "init", NULL, NULL, 0x1, NULL },
  };
  static const J2ObjcClassInfo _JavaLangReflectMethod = {
    1, "Method", "java.lang.reflect", NULL, 0x1, 20, methods, 0, NULL, 0, NULL
  };
  return &_JavaLangReflectMethod;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(JavaLangReflectMethod)
