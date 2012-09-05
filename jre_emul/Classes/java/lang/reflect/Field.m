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
//  Field.m
//  JreEmulation
//
//  Created by Tom Ball on 06/18/2012.
//

#import "IOSClass.h"
#import "IOSObjectArray.h"
#import "java/lang/AssertionError.h"
#import "java/lang/Boolean.h"
#import "java/lang/Byte.h"
#import "java/lang/Character.h"
#import "java/lang/Double.h"
#import "java/lang/Float.h"
#import "java/lang/Integer.h"
#import "java/lang/Long.h"
#import "java/lang/NullPointerException.h"
#import "java/lang/Short.h"
#import "java/lang/Void.h"
#import "java/lang/reflect/Field.h"
#import "java/lang/reflect/Modifier.h"

@implementation JavaLangReflectField

typedef union {
  void *asId;
  char asChar;
  unichar asUnichar;
  short asShort;
  int asInt;
  long long asLong;
  float asFloat;
  double asDouble;
  BOOL asBOOL;
} JavaResult;

- (id)initWithName:(NSString *)name withClass:(IOSClass *)aClass {
  if ((self = [super init])) {
    const char* cname =
        [name cStringUsingEncoding:[NSString defaultCStringEncoding]];
    ivar_ = class_getInstanceVariable(aClass.objcClass, cname);
    declaringClass_ = aClass;
  }
  return self;
}

- (id)initWithIvar:(Ivar)ivar withClass:(IOSClass *)aClass {
  if ((self = [super init])) {
    ivar_ = ivar;
    declaringClass_ = aClass;
  }
  return self;
}

+ (id)fieldWithName:(NSString *)name withClass:(IOSClass *)aClass {
  JavaLangReflectField *field =
      [[JavaLangReflectField alloc]
       initWithName:name withClass:aClass];
#if ! __has_feature(objc_arc)
  [field autorelease];
#endif
  return field;
}

+ (id)fieldWithIvar:(Ivar)ivar withClass:(IOSClass *)aClass {
  JavaLangReflectField *field =
      [[JavaLangReflectField alloc] initWithIvar:ivar withClass:aClass];
#if ! __has_feature(objc_arc)
  [field autorelease];
#endif
  return field;
}

- (NSString *)getName {
  return [NSString stringWithCString:ivar_getName(ivar_)
                            encoding:[NSString defaultCStringEncoding]];
}

- (NSString *)description {
    return [self getName];
}

- (id)get:(id)object {
  return object_getIvar(object, ivar_);
}

- (BOOL)getBoolean:(id)object {
  BOOL *field = ((BOOL *) object) + ivar_getOffset(ivar_);
  return *field;
}

- (char)getByte:(id)object {
  char *field = ((char *) object) + ivar_getOffset(ivar_);
  return *field;
}

- (unichar)getChar:(id)object {
  unichar *field = ((unichar *) object) + ivar_getOffset(ivar_);
  return *field;
}

- (double)getDouble:(id)object {
  double *field = ((double *) object) + ivar_getOffset(ivar_);
  return *field;
}

- (float)getFloat:(id)object {
  float *field = ((float *) object) + ivar_getOffset(ivar_);
  return *field;
}

- (int)getInt:(id)object {
  int *field = ((int *) object) + ivar_getOffset(ivar_);
  return *field;
}

- (long long)getLong:(id)object {
  long long *field = ((long long *) object) + ivar_getOffset(ivar_);
  return *field;
}

- (short)getShort:(id)object {
  short *field = ((short *) object) + ivar_getOffset(ivar_);
  return *field;
}

- (void)set:(id)object value:(id)value {
  object_setIvar(object, ivar_, value);
}

- (void)setBoolean:(id)object value:(BOOL)value {
  BOOL *field = ((BOOL *) object) + ivar_getOffset(ivar_);
  *field = value;
}

- (void)setByte:(id)object value:(char)value {
  char *field = ((char *) object) + ivar_getOffset(ivar_);
  *field = value;
}

- (void)setChar:(id)object value:(unichar)value {
  unichar *field = ((unichar *) object) + ivar_getOffset(ivar_);
  *field = value;
}

- (void)setDouble:(id)object value:(double)value {
  double *field = ((double *) object) + ivar_getOffset(ivar_);
  *field = value;
}

- (void)setFloat:(id)object value:(float)value {
  float *field = ((float *) object) + ivar_getOffset(ivar_);
  *field = value;
}

- (void)setInt:(id)object value:(int)value {
  int *field = ((int *) object) + ivar_getOffset(ivar_);
  *field = value;
}

- (void)setLong:(id)object value:(long long)value {
  long long *field = ((long long *) object) + ivar_getOffset(ivar_);
  *field = value;
}

- (void)setShort:(id)object value:(short)value {
  short *field = ((short *) object) + ivar_getOffset(ivar_);
  *field = value;
}


- (IOSClass *)getType {
  const char *argType = ivar_getTypeEncoding(ivar_);
  if (strlen(argType) != 1) {
    NSString *errorMsg =
    [NSString stringWithFormat:@"unexpected type: %s", argType];
    id exception = [[JavaLangAssertionError alloc] initWithNSString:errorMsg];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }
  return decodeTypeEncoding(*argType);
}

- (int)getModifiers {
  // All Objective-C fields and methods are public at runtime.
  return JavaLangReflectModifier_PUBLIC;
}

- (IOSClass *)getDeclaringClass {
  return declaringClass_;
}

@end
