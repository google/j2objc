// Copyright 2012 Google Inc. All Rights Reserved.
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
//  Field.h
//  JreEmulation
//
//  Created by Tom Ball on 06/18/2012.
//

#ifndef _JavaLangReflectField_H_
#define _JavaLangReflectField_H_

#import "IOSMetadata.h"
#import "J2ObjC_common.h"
#import "java/lang/reflect/AccessibleObject.h"
#import "java/lang/reflect/Member.h"
#import <objc/runtime.h>

@class IOSClass;
@class IOSObjectArray;

// A native implementation of java.lang.reflect.Field.  Its methods are
// limited to those that can be derived from the Objective-C runtime,
// so instances can be created and released as needed.
@interface JavaLangReflectField : JavaLangReflectAccessibleObject < JavaLangReflectMember > {
@protected
  Ivar ivar_;
  IOSClass *declaringClass_;
  const J2ObjcFieldInfo *metadata_;
  const void **ptrTable_;
}

- (instancetype)initWithIvar:(Ivar)ivar
                   withClass:(IOSClass *)aClass
                withMetadata:(const J2ObjcFieldInfo *)metadata;
+ (instancetype)fieldWithIvar:(Ivar)ivar
                    withClass:(IOSClass *)aClass
                 withMetadata:(const J2ObjcFieldInfo *)metadata;

// Returns field name.
- (NSString *)getName;

// Field.get(Object), etc.
- (id)getWithId:(id)object;
- (bool)getBooleanWithId:(id)object;
- (int8_t)getByteWithId:(id)object;
- (uint16_t)getCharWithId:(id)object;
- (double)getDoubleWithId:(id)object;
- (float)getFloatWithId:(id)object;
- (int32_t)getIntWithId:(id)object;
- (int64_t)getLongWithId:(id)object;
- (int16_t)getShortWithId:(id)object;

// Field.set(Object, Object), etc.
- (void)setWithId:(id)object withId:(id)value;
- (void)setBooleanWithId:(id)object withBoolean:(bool)value;
- (void)setByteWithId:(id)object withByte:(int8_t)value;
- (void)setCharWithId:(id)object withChar:(uint16_t)value;
- (void)setDoubleWithId:(id)object withDouble:(double)value;
- (void)setFloatWithId:(id)object withFloat:(float)value;
- (void)setIntWithId:(id)object withInt:(int32_t)value;
- (void)setLongWithId:(id)object withLong:(int64_t)value;
- (void)setShortWithId:(id)object withShort:(int16_t)value;

- (IOSClass *)getDeclaringClass;
- (int32_t)getModifiers;
- (IOSClass *)getType;
- (bool)isEnumConstant;

// Returns type.
- (id<JavaLangReflectType>)getGenericType;

// Should only be used by sun.misc.Unsafe.
- (int64_t)unsafeOffset;

@end

J2OBJC_EMPTY_STATIC_INIT(JavaLangReflectField)

J2OBJC_TYPE_LITERAL_HEADER(JavaLangReflectField)

#endif // _JavaLangReflectField_H_
