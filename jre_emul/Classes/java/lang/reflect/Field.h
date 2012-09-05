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

#import "AccessibleObject.h"
#import <objc/runtime.h>

@class IOSClass;
@class IOSObjectArray;

// A native implementation of java.lang.reflect.Field.  Its methods are 
// limited to those that can be derived from the Objective-C runtime,
// so instances can be created and released as needed.
@interface JavaLangReflectField : AccessibleObject {
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
- (id)get:(id)object;
- (BOOL)getBoolean:(id)object;
- (char)getByte:(id)object;
- (unichar)getChar:(id)object;
- (double)getDouble:(id)object;
- (float)getFloat:(id)object;
- (int)getInt:(id)object;
- (long long)getLong:(id)object;
- (short)getShort:(id)object;

// Field.set(Object, Object), etc.
- (void)set:(id)object value:(id)value;
- (void)setBoolean:(id)object value:(BOOL)value;
- (void)setByte:(id)object value:(char)value;
- (void)setChar:(id)object value:(unichar)value;
- (void)setDouble:(id)object value:(double)value;
- (void)setFloat:(id)object value:(float)value;
- (void)setInt:(id)object value:(int)value;
- (void)setLong:(id)object value:(long long)value;
- (void)setShort:(id)object value:(short)value;

- (IOSClass *)getDeclaringClass;
- (int)getModifiers;
- (IOSClass *)getType;

@end
