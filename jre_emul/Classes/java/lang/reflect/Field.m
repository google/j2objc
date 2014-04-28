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
#import "IOSPrimitiveClass.h"
#import "JavaMetadata.h"
#import "java/lang/AssertionError.h"
#import "java/lang/IllegalAccessException.h"
#import "java/lang/IllegalArgumentException.h"
#import "java/lang/NullPointerException.h"
#import "java/lang/reflect/Field.h"
#import "java/lang/reflect/Method.h"
#import "java/lang/reflect/Modifier.h"
#import "java/lang/reflect/TypeVariable.h"
#import "objc/message.h"
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
    metadata_ = RETAIN_(metadata);
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
            [[self getDeclaringClass] getName], [self propertyName]];
  }
  return [NSString stringWithFormat:@"%@ %@.%@", [self getType], [[self getDeclaringClass] getName],
          [self propertyName]];
}

static BOOL IsStatic(JavaLangReflectField *field) {
  return ([field->metadata_ modifiers] & JavaLangReflectModifier_STATIC) > 0;
}

static BOOL IsFinal(JavaLangReflectField *field) {
  return ([field->metadata_ modifiers] & JavaLangReflectModifier_FINAL) > 0;
}

static void ReadRawValue(
    J2ObjcRawValue *rawValue, JavaLangReflectField *field, id object, IOSClass *toType) {
  IOSClass *type = TypeToClass([field->metadata_ type]);
  if (!type) {
    // Reflection stripped, assume the caller knows the correct type.
    type = toType;
  }
  if (IsStatic(field)) {
    const void *addr = [field->metadata_ staticRef];
    if (addr) {
      [type __readRawValue:rawValue fromAddress:addr];
    } else {
      *rawValue = *[field->metadata_ getConstantValue];
    }
  } else {
    nil_chk(object);
    [type __readRawValue:rawValue fromAddress:((void *)object) + ivar_getOffset(field->ivar_)];
  }
  if (![type __convertRawValue:rawValue toType:toType]) {
    @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc] initWithNSString:
        @"field type mismatch"]);
  }
}

static void SetWithRawValue(
    J2ObjcRawValue *rawValue, JavaLangReflectField *field, id object, IOSClass *fromType) {
  IOSClass *type = TypeToClass([field->metadata_ type]);
  if (!type) {
    // Reflection stripped, assume the caller knows the correct type.
    type = fromType;
  }
  if (![fromType __convertRawValue:rawValue toType:type]) {
    @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc] initWithNSString:
        @"field type mismatch"]);
  }
  if (IsStatic(field)) {
    if (IsFinal(field) && !field->accessible_) {
      @throw AUTORELEASE([[JavaLangIllegalAccessException alloc] initWithNSString:
          @"Cannot set static final field"]);
    }
    const void *addr = [field->metadata_ staticRef];
    [type __writeRawValue:rawValue toAddress:addr];
  } else {
    nil_chk(object);
    if (IsFinal(field) && !field->accessible_) {
      @throw AUTORELEASE([[JavaLangIllegalAccessException alloc] initWithNSString:
                          @"Cannot set final field"]);
    }
    [type __writeRawValue:rawValue toAddress:((void *)object) + ivar_getOffset(field->ivar_)];
  }
}

- (id)getWithId:(id)object {
  J2ObjcRawValue rawValue;
  IOSClass *fieldType = [self getType];
  ReadRawValue(&rawValue, self, object, fieldType);
  return [fieldType __boxValue:&rawValue];
}

- (BOOL)getBooleanWithId:(id)object {
  J2ObjcRawValue rawValue;
  ReadRawValue(&rawValue, self, object, [IOSClass booleanClass]);
  return rawValue.asBOOL;
}

- (char)getByteWithId:(id)object {
  J2ObjcRawValue rawValue;
  ReadRawValue(&rawValue, self, object, [IOSClass byteClass]);
  return rawValue.asChar;
}

- (unichar)getCharWithId:(id)object {
  J2ObjcRawValue rawValue;
  ReadRawValue(&rawValue, self, object, [IOSClass charClass]);
  return rawValue.asUnichar;
}

- (double)getDoubleWithId:(id)object {
  J2ObjcRawValue rawValue;
  ReadRawValue(&rawValue, self, object, [IOSClass doubleClass]);
  return rawValue.asDouble;
}

- (float)getFloatWithId:(id)object {
  J2ObjcRawValue rawValue;
  ReadRawValue(&rawValue, self, object, [IOSClass floatClass]);
  return rawValue.asFloat;
}

- (int)getIntWithId:(id)object {
  J2ObjcRawValue rawValue;
  ReadRawValue(&rawValue, self, object, [IOSClass intClass]);
  return rawValue.asInt;
}

- (long long)getLongWithId:(id)object {
  J2ObjcRawValue rawValue;
  ReadRawValue(&rawValue, self, object, [IOSClass longClass]);
  return rawValue.asLong;
}

- (short)getShortWithId:(id)object {
  J2ObjcRawValue rawValue;
  ReadRawValue(&rawValue, self, object, [IOSClass shortClass]);
  return rawValue.asShort;
}

- (void)setWithId:(id)object withId:(id)value {
  J2ObjcRawValue rawValue;
  IOSClass *fieldType = [self getType];
  [fieldType __unboxValue:value toRawValue:&rawValue];
  SetWithRawValue(&rawValue, self, object, fieldType);
}

- (void)setBooleanWithId:(id)object withBoolean:(BOOL)value {
  J2ObjcRawValue rawValue = { .asBOOL = value };
  SetWithRawValue(&rawValue, self, object, [IOSClass booleanClass]);
}

- (void)setByteWithId:(id)object withByte:(char)value {
  J2ObjcRawValue rawValue = { .asChar = value };
  SetWithRawValue(&rawValue, self, object, [IOSClass byteClass]);
}

- (void)setCharWithId:(id)object withChar:(unichar)value {
  J2ObjcRawValue rawValue = { .asUnichar = value };
  SetWithRawValue(&rawValue, self, object, [IOSClass charClass]);
}

- (void)setDoubleWithId:(id)object withDouble:(double)value {
  J2ObjcRawValue rawValue = { .asDouble = value };
  SetWithRawValue(&rawValue, self, object, [IOSClass doubleClass]);
}

- (void)setFloatWithId:(id)object withFloat:(float)value {
  J2ObjcRawValue rawValue = { .asFloat = value };
  SetWithRawValue(&rawValue, self, object, [IOSClass floatClass]);
}

- (void)setIntWithId:(id)object withInt:(int)value {
  J2ObjcRawValue rawValue = { .asInt = value };
  SetWithRawValue(&rawValue, self, object, [IOSClass intClass]);
}

- (void)setLongWithId:(id)object withLong:(long long)value {
  J2ObjcRawValue rawValue = { .asLong = value };
  SetWithRawValue(&rawValue, self, object, [IOSClass longClass]);
}

- (void)setShortWithId:(id)object withShort:(short)value {
  J2ObjcRawValue rawValue = { .asShort = value };
  SetWithRawValue(&rawValue, self, object, [IOSClass shortClass]);
}


- (IOSClass *)getType {
  if (metadata_) {
    return TypeToClass([metadata_ type]);
  }
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
  // TODO(tball): update when field metadata has a generic type attribute.
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
  Class cls = declaringClass_.objcClass;
  if (cls) {
    NSString *annotationsMethodName =
        [NSString stringWithFormat:@"__annotations_%@_", [self getName]];
    Method annotationsMethod = JreFindClassMethod(cls, [annotationsMethodName UTF8String]);
    if (annotationsMethod) {
      return method_invoke(cls, annotationsMethod);
    }
  }
  IOSClass *annotationType = [IOSClass classWithProtocol:@protocol(JavaLangAnnotationAnnotation)];
  return [IOSObjectArray arrayWithLength:0 type:annotationType];
}

- (int)unsafeOffset {
  return ivar_getOffset(ivar_);
}

// isEqual and hash are uniquely identified by their class and field names.
- (BOOL)isEqual:(id)anObject {
  if (![anObject isKindOfClass:[JavaLangReflectField class]]) {
    return NO;
  }
  JavaLangReflectField *other = (JavaLangReflectField *) anObject;
  return declaringClass_ == other->declaringClass_ &&
      [[self propertyName] isEqual:[other propertyName]];
}

- (NSUInteger)hash {
  return [[declaringClass_ getName] hash] ^ [[self propertyName] hash];
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [metadata_ release];
  [super dealloc];
}
#endif

@end
