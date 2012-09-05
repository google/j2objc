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

#import <Foundation/Foundation.h>
#import "java/lang/reflect/GenericDeclaration.h"
#import "java/lang/reflect/Type.h"

@class IOSObjectArray;
@class JavaLangAnnotationAnnotation;
@class JavaLangReflectConstructor;
@class JavaLangReflectField;
@class JavaLangReflectMethod;

// A wrapper class for an Objective-C Class or Protocol,
// similar in functionality to java.lang.Class.  Its
// methods are limited to those that can be derived
// from a Class instance, so instances can be created
// and released as needed.
@interface IOSClass : NSObject <JavaLangReflectGenericDeclaration,
    JavaLangReflectType> {
 @private
  // Only one of these may be set.
  Class class_;
  Protocol *protocol_;
}

@property (readonly) Class objcClass;
@property (readonly) Protocol *objcProtocol;

+ (IOSClass *)classWithClass:(Class)cls;
- (id)initWithClass:(Class)cls;

+ (IOSClass *)classWithProtocol:(Protocol *)protocol;
- (id)initWithProtocol:(Protocol *)protocol;

// Class.newInstance()
- (id)newInstance NS_RETURNS_RETAINED;

// Class.getSuperclass()
- (IOSClass *)getSuperclass;

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
          classLoader:(id)loader;

// Class.cast(Object)
- (id)cast:(id)throwable;

// Class.getEnclosingClass()
- (IOSClass *)getEnclosingClass;

- (BOOL)isArray;
- (BOOL)isEnum;
- (BOOL)isInterface;
- (BOOL)isPrimitive;

- (IOSObjectArray *)getInterfaces;
- (IOSObjectArray *)getGenericInterfaces;
- (IOSObjectArray *)getTypeParameters;

- (JavaLangAnnotationAnnotation *)getAnnotation:(IOSClass *)annotationClass;
- (BOOL)isAnnotationPresent:(IOSClass *)annotationClass;
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

// Internal methods
- (NSString *)binaryName;

@end
