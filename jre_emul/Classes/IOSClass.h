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
//  IOSClass.h
//  JreEmulation
//
//  Created by Tom Ball on 10/18/11.
//

#ifndef _IOSClass_H_
#define _IOSClass_H_

#import "IOSReflection.h"
#import "java/io/Serializable.h"
#import "java/lang/reflect/AnnotatedElement.h"
#import "java/lang/reflect/GenericDeclaration.h"
#import "java/lang/reflect/Type.h"

@class IOSObjectArray;
@class JavaLangClassLoader;
@class JavaLangReflectConstructor;
@class JavaLangReflectField;
@class JavaLangReflectMethod;
@protocol JavaLangAnnotationAnnotation;
@class JavaIoInputStream;
@class JavaClassMetadata;
@class JavaNetURL;

// A wrapper class for an Objective-C Class or Protocol,
// similar in functionality to java.lang.Class.  Its
// methods are limited to those that can be derived
// from a Class instance, so instances can be created
// and released as needed.
@interface IOSClass : NSObject <JavaLangReflectAnnotatedElement,
    JavaLangReflectGenericDeclaration, JavaIoSerializable,
    JavaLangReflectType, NSCopying> {
}

@property (readonly) Class objcClass;
@property (readonly) Protocol *objcProtocol;

// IOSClass Getters.
+ (IOSClass *)classWithClass:(Class)cls;
+ (IOSClass *)classWithProtocol:(Protocol *)protocol;
+ (IOSClass *)arrayClassWithComponentType:(IOSClass *)componentType;
+ (IOSClass *)classForIosName:(NSString *)iosName;
+ (IOSClass *)primitiveClassForChar:(unichar)c;

// Primitive class instance getters.
+ (IOSClass *)byteClass;
+ (IOSClass *)charClass;
+ (IOSClass *)doubleClass;
+ (IOSClass *)floatClass;
+ (IOSClass *)intClass;
+ (IOSClass *)longClass;
+ (IOSClass *)shortClass;
+ (IOSClass *)booleanClass;
+ (IOSClass *)voidClass;

+ (IOSClass *)objectClass;
+ (IOSClass *)stringClass;

// Class.newInstance()
- (id)newInstance NS_RETURNS_NOT_RETAINED;

// Class.getSuperclass()
- (IOSClass *)getSuperclass;
- (id<JavaLangReflectType>)getGenericSuperclass;

// Class.isInstance(Object)
- (BOOL)isInstance:(id)object;

// These methods all return the same class name.
- (NSString *)getName;
- (NSString *)getSimpleName;
- (NSString *)getCanonicalName;

// Class.getModifiers()
- (int)getModifiers;

// Class.getDeclaredConstructors()
- (IOSObjectArray *)getDeclaredConstructors;

// Class.getDeclaredMethods()
- (IOSObjectArray *)getDeclaredMethods;

// Class.getMethod(String, Class...)
- (JavaLangReflectMethod *)getMethod:(NSString *)name
                      parameterTypes:(IOSObjectArray *)types;

// Class.getDeclaredMethod(String, Class...)
- (JavaLangReflectMethod *)getDeclaredMethod:(NSString *)name
                              parameterTypes:(IOSObjectArray *)types;

// Class.getDeclaredConstructor(Class...)
- (JavaLangReflectConstructor *)getDeclaredConstructor:(IOSObjectArray *)types;

// Class.getConstructor(Class)
- (JavaLangReflectConstructor *)getConstructor:(IOSObjectArray *)types;

// Class.getConstructors()
- (IOSObjectArray *)getConstructors;

// Class.getMethods()
- (IOSObjectArray *)getMethods;

// Class.isAssignableFrom(Class)
- (BOOL) isAssignableFrom:(IOSClass *)cls;

// Class.asSubclass(Class)
- (IOSClass *)asSubclass:(IOSClass *)cls;

// Class.getComponentType()
- (IOSClass *)getComponentType;

// Class.forName
+ (IOSClass *)forName:(NSString *)className;
+ (IOSClass *)forName:(NSString *)className
           initialize:(BOOL)load
          classLoader:(JavaLangClassLoader *)loader;

// Class.cast(Object)
- (id)cast:(id)throwable;

// Class.getEnclosingClass()
- (IOSClass *)getEnclosingClass;

// Class.isMemberClass
- (BOOL)isMemberClass;
- (BOOL)isLocalClass;

- (BOOL)isArray;
- (BOOL)isEnum;
- (BOOL)isInterface;
- (BOOL)isPrimitive;
- (BOOL)isAnnotation;

- (IOSObjectArray *)getInterfaces;
- (IOSObjectArray *)getGenericInterfaces;
- (IOSObjectArray *)getTypeParameters;

- (id)getAnnotationWithIOSClass:(IOSClass *)annotationClass;
- (BOOL)isAnnotationPresentWithIOSClass:(IOSClass *)annotationType;
- (IOSObjectArray *)getAnnotations;
- (IOSObjectArray *)getDeclaredAnnotations;

- (id)getPackage;
- (id)getClassLoader;

- (JavaLangReflectField *)getDeclaredField:(NSString *)name;
- (IOSObjectArray *)getDeclaredFields;
- (JavaLangReflectField *)getField:(NSString *)name;
- (IOSObjectArray *)getFields;

- (JavaLangReflectMethod *)getEnclosingConstructor;
- (JavaLangReflectMethod *)getEnclosingMethod;
- (BOOL)isAnonymousClass;

- (BOOL)desiredAssertionStatus;

- (IOSObjectArray *)getEnumConstants;

// Class.getResource, getResourceAsStream
- (JavaNetURL *)getResource:(NSString *)name;
- (JavaIoInputStream *)getResourceAsStream:(NSString *)name;

// Boxing and unboxing (internal)
- (id)__boxValue:(J2ObjcRawValue *)rawValue;
- (BOOL)__unboxValue:(id)value toRawValue:(J2ObjcRawValue *)rawValue;
- (void)__readRawValue:(J2ObjcRawValue *)rawValue fromAddress:(const void *)addr;
- (void)__writeRawValue:(J2ObjcRawValue *)rawValue toAddress:(const void *)addr;
- (BOOL)__convertRawValue:(J2ObjcRawValue *)rawValue toType:(IOSClass *)type;

// Internal methods
- (void)collectMethods:(NSMutableDictionary *)methodMap
            publicOnly:(BOOL)publicOnly;
- (JavaLangReflectMethod *)findMethodWithTranslatedName:(NSString *)objcName;
- (IOSObjectArray *)getInterfacesWithArrayType:(IOSClass *)arrayType;
- (JavaClassMetadata *)getMetadata;
- (NSString *)objcName;
- (NSString *)binaryName;

#ifdef __cplusplus
extern "C" {
#endif

extern NSString *IOSClass_GetTranslatedMethodName(
    NSString *name, IOSObjectArray *paramTypes);

FOUNDATION_EXPORT IOSClass *IOSClass_forNameWithNSString_(NSString *className);
FOUNDATION_EXPORT IOSClass *IOSClass_forNameWithNSString_withBoolean_withJavaLangClassLoader_(
    NSString *className, BOOL load, JavaLangClassLoader *loader);

#ifdef __cplusplus
}
#endif

@end

#endif // _IOSClass_H_
