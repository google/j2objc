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
#import "JavaMetadata.h"
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
#import "java/lang/reflect/Method.h"
#import "java/lang/reflect/Modifier.h"
#import "java/lang/reflect/TypeVariable.h"
#import "objc/runtime.h"

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

- (id)initWithIvar:(Ivar)ivar
         withClass:(IOSClass *)aClass
      withMetadata:(JavaFieldMetadata *)metadata {
  if ((self = [super init])) {
    ivar_ = ivar;
    declaringClass_ = aClass;
    metadata_ = metadata;
  }
  return self;
}

+ (id)fieldWithIvar:(Ivar)ivar
          withClass:(IOSClass *)aClass
       withMetadata:(JavaFieldMetadata *)metadata {
  JavaLangReflectField *field =
      [[JavaLangReflectField alloc] initWithIvar:ivar withClass:aClass withMetadata:metadata];
#if ! __has_feature(objc_arc)
  [field autorelease];
#endif
  return field;
}

- (NSString *)getName {
  return [self propertyName];
}

- (NSString *)description {
  NSString *mods =
      metadata_ ? [JavaLangReflectModifier toStringWithInt:[metadata_ modifiers]] : @"";
  if ([mods length] > 0) {
    return [NSString stringWithFormat:@"%@ %@ %@.%@", mods, [self getType],
            [self getDeclaringClass], [self propertyName]];
  }
  return [NSString stringWithFormat:@"%@ %@.%@", [self getType], [self getDeclaringClass],
          [self propertyName]];
}

static id GetStaticValue(JavaLangReflectField *field) {
  JavaLangReflectMethod *getter = [field->declaringClass_ getMethod:[field getName]
                                                     parameterTypes:nil];
  return [getter invokeWithId:field->declaringClass_ withNSObjectArray:nil];
}

static void SetStaticValue(JavaLangReflectField *field, id value) {
  NSString *fieldName = [field getName];
  NSString *firstChar = [[fieldName substringToIndex:1] capitalizedString];
  NSString *setterName =
      [NSString stringWithFormat:@"set%@",
       [fieldName stringByReplacingCharactersInRange:NSMakeRange(0, 1) withString:firstChar]];
  IOSObjectArray *parameterTypes = [IOSObjectArray arrayWithLength:1 type:[IOSClass getClass]];
  [parameterTypes replaceObjectAtIndex:0 withObject:[field getType]];
  JavaLangReflectMethod *setter =
      [field->declaringClass_ getMethod:setterName parameterTypes:parameterTypes];
  IOSObjectArray *args = [IOSObjectArray arrayWithLength:1 type:[IOSClass objectClass]];
  [args replaceObjectAtIndex:0 withObject:value];
  [setter invokeWithId:field->declaringClass_ withNSObjectArray:nil];
}

BOOL IsStatic(JavaLangReflectField *field) {
  return ([field->metadata_ modifiers] & JavaLangReflectModifier_STATIC) > 0;
}

- (id)getWithId:(id)object {
  if (IsStatic(self)) {
    return GetStaticValue(self);
  }
  return object_getIvar(object, ivar_);
}

// Returns a pointer to this field's value for a specified object.
- (void *)pvar:(id)object {
  return ((ARCBRIDGE void *) object) + ivar_getOffset(ivar_);
}

- (BOOL)getBooleanWithId:(id)object {
  if (IsStatic(self)) {
    return [(JavaLangBoolean *) GetStaticValue(self) booleanValue];
  }
  return *(BOOL *) [self pvar:object];
}

- (char)getByteWithId:(id)object {
  if (IsStatic(self)) {
    return [(JavaLangByte *) GetStaticValue(self) charValue];
  }
  return *(char *) [self pvar:object];
}

- (unichar)getCharWithId:(id)object {
  if (IsStatic(self)) {
    return [(JavaLangCharacter *) GetStaticValue(self) charValue];
  }
  return *(unichar *) [self pvar:object];
}

- (double)getDoubleWithId:(id)object {
  if (IsStatic(self)) {
    return [(JavaLangDouble *) GetStaticValue(self) doubleValue];
  }
  return *(double *) [self pvar:object];
}

- (float)getFloatWithId:(id)object {
  if (IsStatic(self)) {
    return [(JavaLangFloat *) GetStaticValue(self) floatValue];
  }
  return *(float *) [self pvar:object];
}

- (int)getIntWithId:(id)object {
  if (IsStatic(self)) {
    return [(JavaLangInteger *) GetStaticValue(self) intValue];
  }
  return *(int *) [self pvar:object];
}

- (long long)getLongWithId:(id)object {
  if (IsStatic(self)) {
    return [(JavaLangLong *) GetStaticValue(self) longLongValue];
  }
  return *(long long *) [self pvar:object];
}

- (short)getShortWithId:(id)object {
  if (IsStatic(self)) {
    return [(JavaLangShort *) GetStaticValue(self) shortValue];
  }
  return *(short *) [self pvar:object];
}

- (void)setAndRetain:(id)object withId:(id) ARC_CONSUME_PARAMETER value {
  object_setIvar(object, ivar_, value);
}

- (void)setWithId:(id)object withId:(id)value {
  if (IsStatic(self)) {
    SetStaticValue(self, value);
  } else {
    // Test for nil, since calling a method that consumes its parameters
    // with nil causes a leak.
    // http://clang.llvm.org/docs/AutomaticReferenceCounting.html#retain-count-semantics
    if (value) {
      [self setAndRetain:object withId:value];
    } else {
      object_setIvar(object, ivar_, value);
    }
  }
}

- (void)setBooleanWithId:(id)object withBoolean:(BOOL)value {
  if (IsStatic(self)) {
    SetStaticValue(self, value ? [JavaLangBoolean getTRUE] : [JavaLangBoolean getFALSE]);
  } else {
    BOOL *field = (BOOL *) [self pvar:object];
    *field = value;
  }
}

- (void)setByteWithId:(id)object withByte:(char)value {
  if (IsStatic(self)) {
    SetStaticValue(self, [JavaLangByte valueOfWithByte:value]);
  } else {
    char *field = (char *) [self pvar:object];
    *field = value;
  }
}

- (void)setCharWithId:(id)object withChar:(unichar)value {
  if (IsStatic(self)) {
    SetStaticValue(self, [JavaLangCharacter valueOfWithChar:value]);
  } else {
    unichar *field = (unichar *) [self pvar:object];
    *field = value;
  }
}

- (void)setDoubleWithId:(id)object withDouble:(double)value {
  if (IsStatic(self)) {
    SetStaticValue(self, [JavaLangDouble valueOfWithDouble:value]);
  } else {
    double *field = (double *) [self pvar:object];
    *field = value;
  }
}

- (void)setFloatWithId:(id)object withFloat:(float)value {
  if (IsStatic(self)) {
    SetStaticValue(self, [JavaLangFloat valueOfWithFloat:value]);
  } else {
    float *field = (float *) [self pvar:object];
    *field = value;
  }
}

- (void)setIntWithId:(id)object withInt:(int)value {
  if (IsStatic(self)) {
    SetStaticValue(self, [JavaLangInteger valueOfWithInt:value]);
  } else {
    int *field = (int *) [self pvar:object];
    *field = value;
  }
}

- (void)setLongWithId:(id)object withLong:(long long)value {
  if (IsStatic(self)) {
    SetStaticValue(self, [JavaLangLong valueOfWithLong:value]);
  } else {
    long long *field = (long long *) [self pvar:object];
    *field = value;
  }
}

- (void)setShortWithId:(id)object withShort:(short)value {
  if (IsStatic(self)) {
    SetStaticValue(self, [JavaLangShort valueOfWithShort:value]);
  } else {
    short *field = (short *) [self pvar:object];
    *field = value;
  }
}


- (IOSClass *)getType {
  if (!ivar_) {
    // Static field, use accessor method's return type.
    NSAssert(metadata_ != nil, @"malformed field instance");
    JavaLangReflectMethod *accessor = [declaringClass_ getMethod:[self getName]
                                                  parameterTypes:nil];
    nil_chk(accessor);
    return [accessor getReturnType];
  }
  return decodeTypeEncoding(ivar_getTypeEncoding(ivar_));
}

- (id<JavaLangReflectType>)getGenericType {
  if (metadata_) {
    return [metadata_ type];
  }
  return [self getType];
}

- (int)getModifiers {
  if (metadata_) {
    return [metadata_ modifiers];
  }
  // All Objective-C fields and methods are public at runtime.
  return JavaLangReflectModifier_PUBLIC;
}

- (IOSClass *)getDeclaringClass {
  return declaringClass_;
}

- (NSString *)propertyName {
  NSString *name = metadata_ ?
      [metadata_ name] : [NSString stringWithUTF8String:ivar_getName(ivar_)];
  return [JavaLangReflectField propertyName:name];
}

+ (NSString *)propertyName:(NSString *)name {
  int lastCharIndex = [name length] - 1;
  if ([name characterAtIndex:lastCharIndex] == '_') {
    return [name substringToIndex:lastCharIndex];
  }
  return name;
}

+ (NSString *)variableName:(NSString *)name {
  if ([name characterAtIndex:[name length] - 1] != '_') {
    return [name stringByAppendingString:@"_"];
  }
  return name;
}

- (BOOL)isSynthetic {
  return NO;
}

- (NSString *)toGenericString {
  NSString *mods =
      metadata_ ? [JavaLangReflectModifier toStringWithInt:[metadata_ modifiers]] : @"";
  id<JavaLangReflectType> type = [self getGenericType];
  NSString *typeString = [type conformsToProtocol:@protocol(JavaLangReflectTypeVariable)] ?
      [(id<JavaLangReflectTypeVariable>) type getName] : [type description];
  return [NSString stringWithFormat:@"%@ %@ %@.%@", mods, typeString,
          [self getDeclaringClass], [self propertyName]];
}

- (IOSObjectArray *)getDeclaredAnnotations {
  NSString *annotationsMethod =
      [NSString stringWithFormat:@"__annotations_%@_", [self getName]];
  IOSObjectArray *methods = [declaringClass_ allDeclaredMethods];
  NSUInteger n = [methods count];
  for (NSUInteger i = 0; i < n; i++) {
    JavaLangReflectMethod *method = methods->buffer_[i];
    if ([annotationsMethod isEqualToString:[method getName]] &&
        [[method getParameterTypes] count] == 0) {
      IOSObjectArray *noArgs = [IOSObjectArray arrayWithLength:0 type:[NSObject getClass]];
      return (IOSObjectArray *) [method invokeWithId:nil withNSObjectArray:noArgs];
    }
  }
  IOSClass *annotationType = [IOSClass classWithProtocol:@protocol(JavaLangAnnotationAnnotation)];
  return [IOSObjectArray arrayWithLength:0 type:annotationType];
}

- (int)unsafeOffset {
  return ivar_getOffset(ivar_);
}

@end
