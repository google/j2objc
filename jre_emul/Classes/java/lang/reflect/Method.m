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

+ (instancetype)methodWithDeclaringClass:(IOSClass *)aClass
                                metadata:(const J2ObjcMethodInfo *)metadata {
  return [[[JavaLangReflectMethod alloc] initWithDeclaringClass:aClass
                                                       metadata:metadata] autorelease];
}

static bool IsStatic(const J2ObjcMethodInfo *metadata) {
  return (metadata->modifiers & JavaLangReflectModifier_STATIC) > 0;
}

// Returns method name.
- (NSString *)getName {
  return [NSString stringWithUTF8String:JreMethodJavaName(metadata_, ptrTable_)];
}

- (int)getModifiers {
  return metadata_->modifiers;
}

- (IOSClass *)getReturnType {
  return JreClassForString(metadata_->returnType);
}

- (id<JavaLangReflectType>)getGenericReturnType {
  NSString *genericSignature = JreMethodGenericString(metadata_, ptrTable_);
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
  return [self getReturnType];
}

- (id)invokeWithId:(id)object
    withNSObjectArray:(IOSObjectArray *)arguments {
  if (!IsStatic(metadata_) && object == nil) {
    @throw AUTORELEASE([[JavaLangNullPointerException alloc] initWithNSString:
      @"null object specified for non-final method"]);
  }

  IOSObjectArray *paramTypes = [self getParameterTypesInternal];
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
  for (int i = 0; i < [self getParameterTypesInternal]->size_; i++) {
    [invocation setArgument:(void *)&args[i] atIndex:i + SKIPPED_ARGUMENTS];
  }

  [self invoke:invocation object:object];

  if (result) {
    [invocation getReturnValue:result];
  }
}

// Creates a unique method selector by prepending the class name.
static SEL GetPrivatizedMethodSelector(Class cls, SEL sel) {
  NSMutableString *str = [NSMutableString stringWithUTF8String:class_getName(cls)];
  [str appendString:@"_"];
  [str appendString:NSStringFromSelector(sel)];
  return sel_registerName([str UTF8String]);
}

- (NSInvocation *)invocationForTarget:(id)object {
  NSInvocation *invocation = [NSInvocation invocationWithMethodSignature:[self getSignature]];
  SEL sel = JreMethodSelector(metadata_);
  if (object == nil || [object isKindOfClass:[IOSClass class]]) {
    [invocation setTarget:class_.objcClass];
  } else {
    [invocation setTarget:object];
    if ((metadata_->modifiers & JavaLangReflectModifier_PRIVATE) > 0 &&
        class_ != [object getClass]) {
      // Private methods do not have virtual invocation. If an overriding class "overrides" this
      // private method then the NSInvocation would incorrectly call the overriding method.
      // To work around this we add a new method to the declaring class with a uniquified name.
      Class cls = class_.objcClass;
      Method method = class_getInstanceMethod(cls, sel);
      sel = GetPrivatizedMethodSelector(cls, sel);
      class_addMethod(cls, sel, method_getImplementation(method), method_getTypeEncoding(method));
    }
  }
  [invocation setSelector:sel];
  return invocation;
}

- (void)invoke:(NSInvocation *)invocation object:(id)object {
  NSException *exception = nil;
  @try {
    [invocation invoke];
  }
  @catch (NSException *t) {
    exception = t;
  }
  if (exception) {
    @throw AUTORELEASE([[JavaLangReflectInvocationTargetException alloc]
                        initWithNSException:exception]);
  }
}

- (NSMethodSignature *)getSignature {
  SEL sel = JreMethodSelector(metadata_);
  Protocol *protocol = class_.objcProtocol;
  if (protocol) {
    struct objc_method_description methodDesc =
        protocol_getMethodDescription(protocol, sel, YES, YES);
    return [NSMethodSignature signatureWithObjCTypes:methodDesc.types];
  }
  Class cls = class_.objcClass;
  bool isStatic = (metadata_->modifiers & JavaLangReflectModifier_STATIC) > 0;
  Method method = isStatic ? class_getClassMethod(cls, sel) : class_getInstanceMethod(cls, sel);
  return [NSMethodSignature signatureWithObjCTypes:method_getTypeEncoding(method)];
}

- (NSString *)description {
  NSMutableString *s = [NSMutableString string];
  NSString *modifiers = JavaLangReflectModifier_toStringWithInt_(metadata_->modifiers);
  NSString *returnType = [[self getReturnType] getName];
  NSString *declaringClass = [[self getDeclaringClass] getName];
  [s appendFormat:@"%@ %@ %@.%@(", modifiers, returnType, declaringClass, [self getName]];
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

- (jboolean)isDefault {
  // Default methods are public, non-abstract instance methods declared in an interface.
  BOOL isPublicNonAbstractInstance =
      ((metadata_->modifiers & (JavaLangReflectModifier_ABSTRACT
                               | JavaLangReflectModifier_PUBLIC
                               | JavaLangReflectModifier_STATIC))
       == JavaLangReflectModifier_PUBLIC);
  return isPublicNonAbstractInstance && [self->class_ isInterface];
}

// A method's hash is the hash of its declaring class's name XOR its name.
- (NSUInteger)hash {
  return [[class_ getName] hash] ^ [[self getName] hash];
}

+ (const J2ObjcClassInfo *)__metadata {
  static const J2ObjcMethodInfo methods[] = {
    { "getName", "LNSString;", 0x1, -1, -1, -1, -1, -1, -1 },
    { "getModifiers", "I", 0x1, -1, -1, -1, -1, -1, -1 },
    { "getReturnType", "LIOSClass;", 0x1, -1, -1, -1, -1, -1, -1 },
    { "getGenericReturnType", "LJavaLangReflectType;", 0x1, -1, -1, -1, -1, -1, -1 },
    { "getDeclaringClass", "LIOSClass;", 0x1, -1, -1, -1, 0, -1, -1 },
    { "getParameterTypes", "[LIOSClass;", 0x1, -1, -1, -1, -1, -1, -1 },
    { "getGenericParameterTypes", "[LJavaLangReflectType;", 0x1, -1, -1, -1, -1, -1, -1 },
    { "invokeWithId:withNSObjectArray:", "LNSObject;", 0x81, 1, 2, 3, -1, -1, -1 },
    { "getAnnotationWithIOSClass:", "LJavaLangAnnotationAnnotation;", 0x1, 4, 5, -1, 6, -1, -1 },
    { "getDeclaredAnnotations", "[LJavaLangAnnotationAnnotation;", 0x1, -1, -1, -1, -1, -1, -1 },
    { "getParameterAnnotations", "[[LJavaLangAnnotationAnnotation;", 0x1, -1, -1, -1, -1, -1, -1 },
    { "getTypeParameters", "[LJavaLangReflectTypeVariable;", 0x1, -1, -1, -1, -1, -1, -1 },
    { "isSynthetic", "Z", 0x1, -1, -1, -1, -1, -1, -1 },
    { "getExceptionTypes", "[LIOSClass;", 0x1, -1, -1, -1, -1, -1, -1 },
    { "getGenericExceptionTypes", "[LJavaLangReflectType;", 0x1, -1, -1, -1, -1, -1, -1 },
    { "toGenericString", "LNSString;", 0x1, -1, -1, -1, -1, -1, -1 },
    { "isBridge", "Z", 0x1, -1, -1, -1, -1, -1, -1 },
    { "isVarArgs", "Z", 0x1, -1, -1, -1, -1, -1, -1 },
    { "getDefaultValue", "LNSObject;", 0x1, -1, -1, -1, -1, -1, -1 },
    { "isDefault", "Z", 0x1, -1, -1, -1, -1, -1, -1 },
    { "getAnnotatedReturnType", "LJavaLangReflectAnnotatedType;", 0x1, -1, -1, -1, -1, -1, -1 },
    { "init", NULL, 0x1, -1, -1, -1, -1, -1, -1 },
  };
  static const void *ptrTable[] = {
    "()Ljava/lang/Class<*>;", "invoke", "LNSObject;[LNSObject;",
    "LJavaLangIllegalAccessException;LJavaLangIllegalArgumentException;"
    "LJavaLangReflectInvocationTargetException;", "getAnnotation", "LIOSClass;",
    "<T::Ljava/lang/annotation/Annotation;>(Ljava/lang/Class<TT;>;)TT;" };
  static const J2ObjcClassInfo _JavaLangReflectMethod = {
    "Method", "java.lang.reflect", ptrTable, methods, NULL, 7, 0x1, 22, 0, -1, -1, -1, -1, -1 };
  return &_JavaLangReflectMethod;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(JavaLangReflectMethod)