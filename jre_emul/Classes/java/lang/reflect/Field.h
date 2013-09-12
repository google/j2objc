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

#import "AccessibleObject.h"
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
}

- (id)initWithName:(NSString *)name withClass:(IOSClass *)aClass;
- (id)initWithIvar:(Ivar)ivar withClass:(IOSClass *)aClass;
+ (id)fieldWithName:(NSString *)name withClass:(IOSClass *)aClass;
+ (id)fieldWithIvar:(Ivar)ivar withClass:(IOSClass *)aClass;

// Returns field name.
- (NSString *)getName;

// Field.get(Object), etc.
- (id)getWithId:(id)object;
- (BOOL)getBooleanWithId:(id)object;
- (char)getByteWithId:(id)object;
- (unichar)getCharWithId:(id)object;
- (double)getDoubleWithId:(id)object;
- (float)getFloatWithId:(id)object;
- (int)getIntWithId:(id)object;
- (long long)getLongWithId:(id)object;
- (short)getShortWithId:(id)object;

// Field.set(Object, Object), etc.
- (void)setWithId:(id)object withId:(id)value;
- (void)setBooleanWithId:(id)object withBoolean:(BOOL)value;
- (void)setByteWithId:(id)object withChar:(char)value;
- (void)setCharWithId:(id)object withUnichar:(unichar)value;
- (void)setDoubleWithId:(id)object withDouble:(double)value;
- (void)setFloatWithId:(id)object withFloat:(float)value;
- (void)setIntWithId:(id)object withInt:(int)value;
- (void)setLongWithId:(id)object withLong:(long long)value;
- (void)setShortWithId:(id)object withShortInt:(short)value;

- (IOSClass *)getDeclaringClass;
- (int)getModifiers;
- (IOSClass *)getType;

// Returns type.
- (IOSClass *)getGenericType;

// Convert between property and variable names.
+ (NSString *)propertyName:(NSString *)name;
+ (NSString *)variableName:(NSString *)name;

@end

#endif // _JavaLangReflectField_H_
