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

#import "java/lang/reflect/Field.h"

#import "J2ObjC_source.h"
#import "JavaMetadata.h"
#import "java/lang/AssertionError.h"
#import "java/lang/ClassLoader.h"
#import "java/lang/IllegalAccessException.h"
#import "java/lang/IllegalArgumentException.h"
#import "java/lang/NullPointerException.h"
#import "java/lang/annotation/Annotation.h"
#import "java/lang/reflect/Method.h"
#import "java/lang/reflect/Modifier.h"
#import "java/lang/reflect/TypeVariable.h"
#import "libcore/reflect/GenericSignatureParser.h"
#import "objc/message.h"
#import "objc/runtime.h"

@implementation JavaLangReflectField

- (instancetype)initWithIvar:(Ivar)ivar
                   withClass:(IOSClass *)aClass
                withMetadata:(JavaFieldMetadata *)metadata {
  if ((self = [super init])) {
    ivar_ = ivar;
    declaringClass_ = aClass;
    metadata_ = RETAIN_(metadata);
  }
  return self;
}

+ (instancetype)fieldWithIvar:(Ivar)ivar
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
      metadata_ ? JavaLangReflectModifier_toStringWithInt_([metadata_ modifiers]) : @"";
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
    if (![field->declaringClass_ isInstance:object]) {
      @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc]
                          initWithNSString:@"field type mismatch"]);
    }
    [type __readRawValue:rawValue fromAddress:((char *)object) + ivar_getOffset(field->ivar_)];
  }
  if (![type __convertRawValue:rawValue toType:toType]) {
    @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc]
                        initWithNSString:@"field type mismatch"]);
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
    [type __writeRawValue:rawValue toAddress:((char *)object) + ivar_getOffset(field->ivar_)];
  }
}

- (id)getWithId:(id)object {
  J2ObjcRawValue rawValue;
  IOSClass *fieldType = [self getType];
  ReadRawValue(&rawValue, self, object, fieldType);
  return [fieldType __boxValue:&rawValue];
}

- (jboolean)getBooleanWithId:(id)object {
  J2ObjcRawValue rawValue;
  ReadRawValue(&rawValue, self, object, [IOSClass booleanClass]);
  return rawValue.asBOOL;
}

- (jbyte)getByteWithId:(id)object {
  J2ObjcRawValue rawValue;
  ReadRawValue(&rawValue, self, object, [IOSClass byteClass]);
  return rawValue.asChar;
}

- (jchar)getCharWithId:(id)object {
  J2ObjcRawValue rawValue;
  ReadRawValue(&rawValue, self, object, [IOSClass charClass]);
  return rawValue.asUnichar;
}

- (jdouble)getDoubleWithId:(id)object {
  J2ObjcRawValue rawValue;
  ReadRawValue(&rawValue, self, object, [IOSClass doubleClass]);
  return rawValue.asDouble;
}

- (jfloat)getFloatWithId:(id)object {
  J2ObjcRawValue rawValue;
  ReadRawValue(&rawValue, self, object, [IOSClass floatClass]);
  return rawValue.asFloat;
}

- (jint)getIntWithId:(id)object {
  J2ObjcRawValue rawValue;
  ReadRawValue(&rawValue, self, object, [IOSClass intClass]);
  return rawValue.asInt;
}

- (jlong)getLongWithId:(id)object {
  J2ObjcRawValue rawValue;
  ReadRawValue(&rawValue, self, object, [IOSClass longClass]);
  return rawValue.asLong;
}

- (jshort)getShortWithId:(id)object {
  J2ObjcRawValue rawValue;
  ReadRawValue(&rawValue, self, object, [IOSClass shortClass]);
  return rawValue.asShort;
}

- (void)setWithId:(id)object withId:(id)value {
  // TODO(kstanger): correctly handle @Weak fields.
  IOSClass *fieldType = [self getType];
  BOOL needsRetain = ![fieldType isPrimitive];
  if (needsRetain) {
    AUTORELEASE([self getWithId:object]);
  }
  J2ObjcRawValue rawValue;
  if (![fieldType __unboxValue:value toRawValue:&rawValue]) {
    @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc]
                        initWithNSString:@"field type mismatch"]);
  }
  SetWithRawValue(&rawValue, self, object, fieldType);
  if (needsRetain) {
    RETAIN_(value);
  }
}

- (void)setBooleanWithId:(id)object withBoolean:(jboolean)value {
  J2ObjcRawValue rawValue = { .asBOOL = value };
  SetWithRawValue(&rawValue, self, object, [IOSClass booleanClass]);
}

- (void)setByteWithId:(id)object withByte:(jbyte)value {
  J2ObjcRawValue rawValue = { .asChar = value };
  SetWithRawValue(&rawValue, self, object, [IOSClass byteClass]);
}

- (void)setCharWithId:(id)object withChar:(jchar)value {
  J2ObjcRawValue rawValue = { .asUnichar = value };
  SetWithRawValue(&rawValue, self, object, [IOSClass charClass]);
}

- (void)setDoubleWithId:(id)object withDouble:(jdouble)value {
  J2ObjcRawValue rawValue = { .asDouble = value };
  SetWithRawValue(&rawValue, self, object, [IOSClass doubleClass]);
}

- (void)setFloatWithId:(id)object withFloat:(jfloat)value {
  J2ObjcRawValue rawValue = { .asFloat = value };
  SetWithRawValue(&rawValue, self, object, [IOSClass floatClass]);
}

- (void)setIntWithId:(id)object withInt:(jint)value {
  J2ObjcRawValue rawValue = { .asInt = value };
  SetWithRawValue(&rawValue, self, object, [IOSClass intClass]);
}

- (void)setLongWithId:(id)object withLong:(jlong)value {
  J2ObjcRawValue rawValue = { .asLong = value };
  SetWithRawValue(&rawValue, self, object, [IOSClass longClass]);
}

- (void)setShortWithId:(id)object withShort:(jshort)value {
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
  id<JavaLangReflectType> result = [self getType];
  if (metadata_) {
    NSString *genericSignature = [metadata_ genericSignature];
    if (!genericSignature) {
      return result;
    }
    LibcoreReflectGenericSignatureParser *parser =
        [[LibcoreReflectGenericSignatureParser alloc]
         initWithJavaLangClassLoader:JavaLangClassLoader_getSystemClassLoader()];
    [parser parseForFieldWithJavaLangReflectGenericDeclaration:declaringClass_
                                                  withNSString:genericSignature];
    if (parser->fieldType_) {
      result = [[parser->fieldType_ retain] autorelease];
    }
    [parser release];
  }
  return result;
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
  NSUInteger lastCharIndex = [name length] - 1;
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

- (jboolean)isSynthetic {
  if (metadata_) {
    return ([metadata_ modifiers] & JavaLangReflectModifier_SYNTHETIC) > 0;
  }
  return NO;
}

- (jboolean)isEnumConstant {
  if (metadata_) {
    return ([metadata_ modifiers] & JavaLangReflectModifier_ENUM) > 0;
  }
  return [declaringClass_ isEnum] && [[self getType] isEqual:declaringClass_];
}

- (NSString *)toGenericString {
  NSString *mods =
      metadata_ ? JavaLangReflectModifier_toStringWithInt_([metadata_ modifiers]) : @"";
  if ([mods length] > 0) { // Separate test, since Modifer.toString() might return empty string.
    mods = [mods stringByAppendingString:@" "];
  }
  id<JavaLangReflectType> type = [self getGenericType];
  NSString *typeString = [type conformsToProtocol:@protocol(JavaLangReflectTypeVariable)] ?
      [(id<JavaLangReflectTypeVariable>) type getName] : [type description];
  return [NSString stringWithFormat:@"%@%@ %@.%@", mods, typeString,
          [[self getDeclaringClass] getName], [self propertyName]];
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
  return [IOSObjectArray arrayWithLength:0 type:JavaLangAnnotationAnnotation_class_()];
}

- (int)unsafeOffset {
  return (int) ivar_getOffset(ivar_);
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

+ (const J2ObjcClassInfo *)__metadata {
  static const J2ObjcMethodInfo methods[] = {
    { "getName", NULL, "Ljava.lang.String;", 0x1, NULL },
    { "getModifiers", NULL, "I", 0x1, NULL },
    { "getType", NULL, "Ljava.lang.Class;", 0x1, NULL },
    { "getGenericType", NULL, "Ljava.lang.Class;", 0x1, NULL },
    { "getDeclaringClass", NULL, "Ljava.lang.Class;", 0x1, NULL },
    { "getWithId:", "get", "Ljava.lang.Object;", 0x1, "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;" },
    { "getBooleanWithId:", "getBoolean", "Z", 0x1, "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;" },
    { "getByteWithId:", "getByte", "B", 0x1, "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;" },
    { "getCharWithId:", "getChar", "C", 0x1, "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;" },
    { "getDoubleWithId:", "getDouble", "D", 0x1, "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;" },
    { "getFloatWithId:", "getFloat", "F", 0x1, "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;" },
    { "getIntWithId:", "getInt", "I", 0x1, "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;" },
    { "getLongWithId:", "getLong", "J", 0x1, "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;" },
    { "getShortWithId:", "getShort", "S", 0x1, "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;" },
    { "setWithId:withId:", "set", "V", 0x1, "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;" },
    { "setBooleanWithId:withBoolean:", "setBoolean", "V", 0x1, "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;" },
    { "setByteWithId:withByte:", "setByte", "V", 0x1, "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;" },
    { "setCharWithId:withChar:", "setChar", "V", 0x1, "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;" },
    { "setDoubleWithId:withDouble:", "setDouble", "V", 0x1, "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;" },
    { "setFloatWithId:withFloat:", "setFloat", "V", 0x1, "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;" },
    { "setIntWithId:withInt:", "setInt", "V", 0x1, "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;" },
    { "setLongWithId:withLong:", "setLong", "V", 0x1, "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;" },
    { "setShortWithId:withShort:", "setShort", "V", 0x1, "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;" },
    { "getAnnotationWithIOSClass:", "getAnnotation", "TT;", 0x1, NULL },
    { "getDeclaredAnnotations", NULL, "[Ljava.lang.annotation.Annotation;", 0x1, NULL },
    { "isSynthetic", NULL, "Z", 0x1, NULL },
    { "isEnumConstant", NULL, "Z", 0x1, NULL },
    { "toGenericString", NULL, "Ljava.lang.String;", 0x1, NULL },
    { "init", NULL, NULL, 0x1, NULL },
  };
  static const J2ObjcClassInfo _JavaLangReflectField = {
    1, "Field", "java.lang.reflect", NULL, 0x1, 29, methods, 0, NULL, 0, NULL
  };
  return &_JavaLangReflectField;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(JavaLangReflectField)
