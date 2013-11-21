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
      withMetadata:(const J2ObjcFieldInfo *)metadata {
  if ((self = [super init])) {
    ivar_ = ivar;
    declaringClass_ = aClass;
    metadata_ = metadata;
  }
  return self;
}

+ (id)fieldWithIvar:(Ivar)ivar
          withClass:(IOSClass *)aClass
       withMetadata:(const J2ObjcFieldInfo *)metadata {
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
      metadata_ ? [JavaLangReflectModifier toStringWithInt:metadata_->modifiers ] : @"";
  return [NSString stringWithFormat:@"%@ %@ %@.%@", mods, [self getType],
          [self getDeclaringClass], [self propertyName]];
}

- (id)getWithId:(id)object {
  return object_getIvar(object, ivar_);
}

// Returns a pointer to this field's value for a specified object.
- (void *)pvar:(id)object {
  return ((ARCBRIDGE void *) object) + ivar_getOffset(ivar_);
}

- (BOOL)getBooleanWithId:(id)object {
  return *(BOOL *) [self pvar:object];
}

- (char)getByteWithId:(id)object {
  return *(char *) [self pvar:object];
}

- (unichar)getCharWithId:(id)object {
  return *(unichar *) [self pvar:object];
}

- (double)getDoubleWithId:(id)object {
  return *(double *) [self pvar:object];
}

- (float)getFloatWithId:(id)object {
  return *(float *) [self pvar:object];
}

- (int)getIntWithId:(id)object {
  return *(int *) [self pvar:object];
}

- (long long)getLongWithId:(id)object {
  return *(long long *) [self pvar:object];
}

- (short)getShortWithId:(id)object {
  return *(short *) [self pvar:object];
}

- (void)setAndRetain:(id)object withId:(id) ARC_CONSUME_PARAMETER value {
  object_setIvar(object, ivar_, value);
}

- (void)setWithId:(id)object withId:(id) value {
  // Test for nil, since calling a method that consumes its parameters
  // with nil causes a leak.
  // http://clang.llvm.org/docs/AutomaticReferenceCounting.html#retain-count-semantics
  if (value) {
    [self setAndRetain:object withId:value];
  } else {
    object_setIvar(object, ivar_, value);
  }
}

- (void)setBooleanWithId:(id)object withBoolean:(BOOL)value {
  BOOL *field = (BOOL *) [self pvar:object];
  *field = value;
}

- (void)setByteWithId:(id)object withByte:(char)value {
  char *field = (char *) [self pvar:object];
  *field = value;
}

- (void)setCharWithId:(id)object withChar:(unichar)value {
  unichar *field = (unichar *) [self pvar:object];
  *field = value;
}

- (void)setDoubleWithId:(id)object withDouble:(double)value {
  double *field = (double *) [self pvar:object];
  *field = value;
}

- (void)setFloatWithId:(id)object withFloat:(float)value {
  float *field = (float *) [self pvar:object];
  *field = value;
}

- (void)setIntWithId:(id)object withInt:(int)value {
  int *field = (int *) [self pvar:object];
  *field = value;
}

- (void)setLongWithId:(id)object withLong:(long long)value {
  long long *field = (long long *) [self pvar:object];
  *field = value;
}

- (void)setShortWithId:(id)object withShort:(short)value {
  short *field = (short *) [self pvar:object];
  *field = value;
}


- (IOSClass *)getType {
  return decodeTypeEncoding(ivar_getTypeEncoding(ivar_));
}

- (id<JavaLangReflectType>)getGenericType {
  if (metadata_) {
    return JreTypeForString(metadata_->type);
  }
  return [self getType];
}

- (int)getModifiers {
  if (metadata_) {
    return metadata_->modifiers;
  }
  // All Objective-C fields and methods are public at runtime.
  return JavaLangReflectModifier_PUBLIC;
}

- (IOSClass *)getDeclaringClass {
  return declaringClass_;
}

- (NSString *)propertyName {
  if (metadata_ && metadata_->javaName) {
    return [NSString stringWithCString:metadata_->javaName
                              encoding:[NSString defaultCStringEncoding]];
  }
  NSString *name = [NSString stringWithCString:ivar_getName(ivar_)
                                      encoding:[NSString defaultCStringEncoding]];
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
      metadata_ ? [JavaLangReflectModifier toStringWithInt:metadata_->modifiers ] : @"";
  id<JavaLangReflectType> type = [self getGenericType];
  NSString *typeString = [type conformsToProtocol:@protocol(JavaLangReflectTypeVariable)] ?
      [(id<JavaLangReflectTypeVariable>) type getName] : [type description];
  return [NSString stringWithFormat:@"%@ %@ %@.%@", mods, typeString,
          [self getDeclaringClass], [self propertyName]];
}

- (IOSObjectArray *)getDeclaredAnnotations {
  NSString *annotationsMethod =
      [NSString stringWithFormat:@"__annotations_%@_", [self getName]];
  IOSObjectArray *methods = [declaringClass_ getDeclaredMethods];
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
