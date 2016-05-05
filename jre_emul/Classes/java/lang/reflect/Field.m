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

#import "IOSReflection.h"
#import "J2ObjC_source.h"
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
                withMetadata:(const J2ObjcFieldInfo *)metadata {
  if ((self = [super init])) {
    ivar_ = ivar;
    declaringClass_ = aClass;
    metadata_ = metadata;
  }
  return self;
}

+ (instancetype)fieldWithIvar:(Ivar)ivar
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
  if (metadata_->javaName) {
    return [NSString stringWithUTF8String:metadata_->javaName];
  } else if (IsStatic(self)) {
    return [NSString stringWithUTF8String:metadata_->name];
  } else {
    // Remove the trailing "_" from instance fields.
    return [[[NSString alloc] initWithBytes:metadata_->name
                                     length:strlen(metadata_->name) - 1
                                   encoding:NSUTF8StringEncoding] autorelease];
  }
}

- (NSString *)description {
  NSString *mods = JavaLangReflectModifier_toStringWithInt_(metadata_->modifiers);
  if ([mods length] > 0) {
    return [NSString stringWithFormat:@"%@ %@ %@.%@", mods, [self getType],
            [[self getDeclaringClass] getName], [self getName]];
  }
  return [NSString stringWithFormat:@"%@ %@.%@", [self getType], [[self getDeclaringClass] getName],
          [self getName]];
}

static jboolean IsStatic(JavaLangReflectField *field) {
  return (field->metadata_->modifiers & JavaLangReflectModifier_STATIC) > 0;
}

static jboolean IsFinal(JavaLangReflectField *field) {
  return (field->metadata_->modifiers & JavaLangReflectModifier_FINAL) > 0;
}

static IOSClass *GetErasedFieldType(JavaLangReflectField *field) {
  return TypeToClass(JreTypeForString(field->metadata_->type));
}

static void ReadRawValue(
    J2ObjcRawValue *rawValue, JavaLangReflectField *field, id object, IOSClass *toType) {
  IOSClass *type = GetErasedFieldType(field);
  if (!type) {
    // Reflection stripped, assume the caller knows the correct type.
    type = toType;
  }
  if (IsStatic(field)) {
    const void *addr = field->metadata_->staticRef;
    if (addr) {
      [type __readRawValue:rawValue fromAddress:addr];
    } else {
      *rawValue = field->metadata_->constantValue;
    }
  } else {
    nil_chk(object);
    if (field->ivar_ == NULL) {
      // May be a mapped class "virtual" field, call equivalent accessor method if it exists.
      SEL getter = NSSelectorFromString([NSString stringWithFormat:@"__%@", [field getName]]);
      if (getter && [object respondsToSelector:getter]) {
        rawValue->asId = [object performSelector:getter];
      }
    } else {
      if (![field->declaringClass_ isInstance:object]) {
        @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc]
                            initWithNSString:@"field type mismatch"]);
      }
      [type __readRawValue:rawValue fromAddress:((char *)object) + ivar_getOffset(field->ivar_)];
    }
  }
  if (![type __convertRawValue:rawValue toType:toType]) {
    @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc]
                        initWithNSString:@"field type mismatch"]);
  }
}

static void SetWithRawValue(
    J2ObjcRawValue *rawValue, JavaLangReflectField *field, id object, IOSClass *fromType) {
  IOSClass *type = GetErasedFieldType(field);
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
    const void *addr = field->metadata_->staticRef;
    [type __writeRawValue:rawValue toAddress:addr];
  } else {
    nil_chk(object);
    if (IsFinal(field) && !field->accessible_) {
      @throw AUTORELEASE([[JavaLangIllegalAccessException alloc] initWithNSString:
                          @"Cannot set final field"]);
    }
    if (field->ivar_ == NULL) {
      // May be a mapped class "virtual" field, call equivalent accessor method if it exists.
      SEL setter = NSSelectorFromString([NSString stringWithFormat:@"__set%@:", [field getName]]);
      if (setter && [object respondsToSelector:setter]) {
        [object performSelector:setter withObject:rawValue->asId];
        return;
      }
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
  // If ivar_ is NULL and the field is not static then the field is a mapped
  // class "virtual" field.
  jboolean needsRetain = ![fieldType isPrimitive] && (ivar_ || IsStatic(self));
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
  return GetErasedFieldType(self);
}

- (id<JavaLangReflectType>)getGenericType {
  if (!metadata_->genericSignature) {
    return [self getType];
  }
  NSString *genericSignature = [NSString stringWithUTF8String:metadata_->genericSignature];
  LibcoreReflectGenericSignatureParser *parser =
      [[LibcoreReflectGenericSignatureParser alloc]
       initWithJavaLangClassLoader:JavaLangClassLoader_getSystemClassLoader()];
  [parser parseForFieldWithJavaLangReflectGenericDeclaration:declaringClass_
                                                withNSString:genericSignature];
  id<JavaLangReflectType> result = parser->fieldType_;
  if (result) {
    [[result retain] autorelease];
  } else {
    result = [self getType];
  }
  [parser release];
  return result;
}

- (int)getModifiers {
  return metadata_->modifiers;
}

- (IOSClass *)getDeclaringClass {
  return declaringClass_;
}

- (jboolean)isSynthetic {
  return (metadata_->modifiers & JavaLangReflectModifier_SYNTHETIC) > 0;
}

- (jboolean)isEnumConstant {
  return (metadata_->modifiers & JavaLangReflectModifier_ENUM) > 0;
}

- (NSString *)toGenericString {
  NSString *mods = JavaLangReflectModifier_toStringWithInt_(metadata_->modifiers);
  if ([mods length] > 0) { // Separate test, since Modifer.toString() might return empty string.
    mods = [mods stringByAppendingString:@" "];
  }
  id<JavaLangReflectType> type = [self getGenericType];
  NSString *typeString = [type conformsToProtocol:@protocol(JavaLangReflectTypeVariable)] ?
      [(id<JavaLangReflectTypeVariable>) type getName] : [type description];
  return [NSString stringWithFormat:@"%@%@ %@.%@", mods, typeString,
          [[self getDeclaringClass] getName], [self getName]];
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
  return declaringClass_ == other->declaringClass_ && [[self getName] isEqual:[other getName]];
}

- (NSUInteger)hash {
  return [[declaringClass_ getName] hash] ^ [[self getName] hash];
}

+ (const J2ObjcClassInfo *)__metadata {
  static const J2ObjcMethodInfo methods[] = {
    { "getName", NULL, "Ljava.lang.String;", 0x1, NULL, NULL },
    { "getModifiers", NULL, "I", 0x1, NULL, NULL },
    { "getType", NULL, "Ljava.lang.Class;", 0x1, NULL, "()Ljava/lang/Class<*>;" },
    { "getGenericType", NULL, "Ljava.lang.reflect.Type;", 0x1, NULL, NULL },
    { "getDeclaringClass", NULL, "Ljava.lang.Class;", 0x1, NULL, "()Ljava/lang/Class<*>;" },
    { "getWithId:", "get", "Ljava.lang.Object;", 0x1,
      "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;", NULL },
    { "getBooleanWithId:", "getBoolean", "Z", 0x1,
      "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;", NULL },
    { "getByteWithId:", "getByte", "B", 0x1,
      "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;", NULL },
    { "getCharWithId:", "getChar", "C", 0x1,
      "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;", NULL },
    { "getDoubleWithId:", "getDouble", "D", 0x1,
      "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;", NULL },
    { "getFloatWithId:", "getFloat", "F", 0x1,
      "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;", NULL },
    { "getIntWithId:", "getInt", "I", 0x1,
      "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;", NULL },
    { "getLongWithId:", "getLong", "J", 0x1,
      "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;", NULL },
    { "getShortWithId:", "getShort", "S", 0x1,
      "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;", NULL },
    { "setWithId:withId:", "set", "V", 0x1,
      "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;", NULL },
    { "setBooleanWithId:withBoolean:", "setBoolean", "V", 0x1,
      "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;", NULL },
    { "setByteWithId:withByte:", "setByte", "V", 0x1,
      "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;", NULL },
    { "setCharWithId:withChar:", "setChar", "V", 0x1,
      "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;", NULL },
    { "setDoubleWithId:withDouble:", "setDouble", "V", 0x1,
      "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;", NULL },
    { "setFloatWithId:withFloat:", "setFloat", "V", 0x1,
      "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;", NULL },
    { "setIntWithId:withInt:", "setInt", "V", 0x1,
      "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;", NULL },
    { "setLongWithId:withLong:", "setLong", "V", 0x1,
      "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;", NULL },
    { "setShortWithId:withShort:", "setShort", "V", 0x1,
      "Ljava.lang.IllegalArgumentException;Ljava.lang.IllegalAccessException;", NULL },
    { "getAnnotationWithIOSClass:", "getAnnotation", "TT;", 0x1, NULL,
      "<T::Ljava/lang/annotation/Annotation;>(Ljava/lang/Class<TT;>;)TT;" },
    { "getDeclaredAnnotations", NULL, "[Ljava.lang.annotation.Annotation;", 0x1, NULL, NULL },
    { "isSynthetic", NULL, "Z", 0x1, NULL, NULL },
    { "isEnumConstant", NULL, "Z", 0x1, NULL, NULL },
    { "toGenericString", NULL, "Ljava.lang.String;", 0x1, NULL, NULL },
    { "init", NULL, NULL, 0x1, NULL, NULL },
  };
  static const J2ObjcClassInfo _JavaLangReflectField = {
    2, "Field", "java.lang.reflect", NULL, 0x1, 29, methods, 0, NULL, 0, NULL, 0, NULL, NULL, NULL
  };
  return &_JavaLangReflectField;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(JavaLangReflectField)
