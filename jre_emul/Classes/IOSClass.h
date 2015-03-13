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
#import "J2ObjC_common.h"
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

// Class.newInstance()
- (id)newInstance NS_RETURNS_NOT_RETAINED;

// Class.getSuperclass()
- (IOSClass *)getSuperclass;
- (id<JavaLangReflectType>)getGenericSuperclass;

// Class.getDeclaringClass()
- (IOSClass *)getDeclaringClass;

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
- (BOOL)isSynthetic;

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

- (JavaLangReflectConstructor *)getEnclosingConstructor;
- (JavaLangReflectMethod *)getEnclosingMethod;
- (BOOL)isAnonymousClass;

- (BOOL)desiredAssertionStatus;

- (IOSObjectArray *)getEnumConstants;

// Class.getResource, getResourceAsStream
- (JavaNetURL *)getResource:(NSString *)name;
- (JavaIoInputStream *)getResourceAsStream:(NSString *)name;

- (IOSObjectArray *)getClasses;
- (IOSObjectArray *)getDeclaredClasses;

- (id)getProtectionDomain;
- (id)getSigners;

// Boxing and unboxing (internal)
- (id)__boxValue:(J2ObjcRawValue *)rawValue;
- (BOOL)__unboxValue:(id)value toRawValue:(J2ObjcRawValue *)rawValue;
- (void)__readRawValue:(J2ObjcRawValue *)rawValue fromAddress:(const void *)addr;
- (void)__writeRawValue:(J2ObjcRawValue *)rawValue toAddress:(const void *)addr;
- (BOOL)__convertRawValue:(J2ObjcRawValue *)rawValue toType:(IOSClass *)type;

// Internal methods
- (void)collectMethods:(NSMutableDictionary *)methodMap
            publicOnly:(BOOL)publicOnly;
- (JavaLangReflectMethod *)findMethodWithTranslatedName:(NSString *)objcName
                                        checkSupertypes:(BOOL)checkSupertypes;
- (JavaLangReflectConstructor *)findConstructorWithTranslatedName:(NSString *)objcName;
// Same as getInterfaces, but not a defensive copy.
- (IOSObjectArray *)getInterfacesInternal;
- (JavaClassMetadata *)getMetadata;
- (NSString *)objcName;
- (NSString *)binaryName;
// Get the IOSArray subclass that would be used to hold this type.
- (Class)objcArrayClass;
- (size_t)getSizeof;

@end

CF_EXTERN_C_BEGIN

// Class.forName(String)
IOSClass *IOSClass_forName_(NSString *className);
// Class.forName(String, boolean, ClassLoader)
IOSClass *IOSClass_forName_initialize_classLoader_(
    NSString *className, BOOL load, JavaLangClassLoader *loader);

// Lookup a IOSClass from its associated ObjC class, protocol or component type.
IOSClass *IOSClass_fromClass(Class cls);
IOSClass *IOSClass_fromProtocol(Protocol *protocol);
IOSClass *IOSClass_arrayOf(IOSClass *componentType);
// Same as "arrayOf" but allows dimensions to be specified.
IOSClass *IOSClass_arrayType(IOSClass *componentType, jint dimensions);

// Primitive array type literals.
#define IOSClass_byteArray(DIM) IOSClass_arrayType([IOSClass byteClass], DIM)
#define IOSClass_charArray(DIM) IOSClass_arrayType([IOSClass charClass], DIM)
#define IOSClass_doubleArray(DIM) IOSClass_arrayType([IOSClass doubleClass], DIM)
#define IOSClass_floatArray(DIM) IOSClass_arrayType([IOSClass floatClass], DIM)
#define IOSClass_intArray(DIM) IOSClass_arrayType([IOSClass intClass], DIM)
#define IOSClass_longArray(DIM) IOSClass_arrayType([IOSClass longClass], DIM)
#define IOSClass_shortArray(DIM) IOSClass_arrayType([IOSClass shortClass], DIM)
#define IOSClass_booleanArray(DIM) IOSClass_arrayType([IOSClass booleanClass], DIM)

// Internal functions
NSString *IOSClass_GetTranslatedMethodName(
    IOSClass *cls, NSString *name, IOSObjectArray *paramTypes);

// Return value is retained
IOSObjectArray *IOSClass_NewInterfacesFromProtocolList(Protocol **list, unsigned int count);

CF_EXTERN_C_END

J2OBJC_STATIC_INIT(IOSClass)

J2OBJC_TYPE_LITERAL_HEADER(IOSClass)

#endif // _IOSClass_H_
