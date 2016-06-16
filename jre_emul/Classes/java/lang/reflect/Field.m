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

#import "IOSClass.h"
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
    ptrTable_ = IOSClass_GetMetadataOrFail(aClass)->ptrTable;
  }
  return self;
}

+ (instancetype)fieldWithIvar:(Ivar)ivar
                    withClass:(IOSClass *)aClass
                 withMetadata:(const J2ObjcFieldInfo *)metadata {
  return [[[JavaLangReflectField alloc] initWithIvar:ivar
                                           withClass:aClass
                                        withMetadata:metadata] autorelease];
}

- (NSString *)getName {
  const char *javaName = JrePtrAtIndex(ptrTable_, metadata_->javaNameIdx);
  if (javaName) {
    return [NSString stringWithUTF8String:javaName];
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
  return JreClassForString(field->metadata_->type);
}

static void ReadRawValue(
    J2ObjcRawValue *rawValue, JavaLangReflectField *field, id object, IOSClass *toType) {
  IOSClass *type = GetErasedFieldType(field);
  if (!type) {
    // Reflection stripped, assume the caller knows the correct type.
    type = toType;
  }
  if (IsStatic(field)) {
    const void *addr = JrePtrAtIndex(field->ptrTable_, field->metadata_->staticRefIdx);
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
    [type __writeRawValue:rawValue toAddress:field->ptrTable_[field->metadata_->staticRefIdx]];
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
  const char *genericSig = JrePtrAtIndex(ptrTable_, metadata_->genericSignatureIdx);
  if (!genericSig) {
    return [self getType];
  }
  NSString *genericSignature = [NSString stringWithUTF8String:genericSig];
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
  id (*annotations)() = JrePtrAtIndex(ptrTable_, metadata_->annotationsIdx);
  if (annotations) {
    return annotations();
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
    { "getName", "Ljava.lang.String;", 0x1, -1, -1, -1, -1, -1 },
    { "getModifiers", "I", 0x1, -1, -1, -1, -1, -1 },
    { "getType", "Ljava.lang.Class;", 0x1, -1, -1, 0, -1, -1 },
    { "getGenericType", "Ljava.lang.reflect.Type;", 0x1, -1, -1, -1, -1, -1 },
    { "getDeclaringClass", "Ljava.lang.Class;", 0x1, -1, -1, 0, -1, -1 },
    { "getWithId:", "Ljava.lang.Object;", 0x1, 1, 2, -1, -1, -1 },
    { "getBooleanWithId:", "Z", 0x1, 3, 2, -1, -1, -1 },
    { "getByteWithId:", "B", 0x1, 4, 2, -1, -1, -1 },
    { "getCharWithId:", "C", 0x1, 5, 2, -1, -1, -1 },
    { "getDoubleWithId:", "D", 0x1, 6, 2, -1, -1, -1 },
    { "getFloatWithId:", "F", 0x1, 7, 2, -1, -1, -1 },
    { "getIntWithId:", "I", 0x1, 8, 2, -1, -1, -1 },
    { "getLongWithId:", "J", 0x1, 9, 2, -1, -1, -1 },
    { "getShortWithId:", "S", 0x1, 10, 2, -1, -1, -1 },
    { "setWithId:withId:", "V", 0x1, 11, 2, -1, -1, -1 },
    { "setBooleanWithId:withBoolean:", "V", 0x1, 12, 2, -1, -1, -1 },
    { "setByteWithId:withByte:", "V", 0x1, 13, 2, -1, -1, -1 },
    { "setCharWithId:withChar:", "V", 0x1, 14, 2, -1, -1, -1 },
    { "setDoubleWithId:withDouble:", "V", 0x1, 15, 2, -1, -1, -1 },
    { "setFloatWithId:withFloat:", "V", 0x1, 16, 2, -1, -1, -1 },
    { "setIntWithId:withInt:", "V", 0x1, 17, 2, -1, -1, -1 },
    { "setLongWithId:withLong:", "V", 0x1, 18, 2, -1, -1, -1 },
    { "setShortWithId:withShort:", "V", 0x1, 19, 2, -1, -1, -1 },
    { "getAnnotationWithIOSClass:", "TT;", 0x1, 20, -1, 21, -1, -1 },
    { "getDeclaredAnnotations", "[Ljava.lang.annotation.Annotation;", 0x1, -1, -1, -1, -1, -1 },
    { "isSynthetic", "Z", 0x1, -1, -1, -1, -1, -1 },
    { "isEnumConstant", "Z", 0x1, -1, -1, -1, -1, -1 },
    { "toGenericString", "Ljava.lang.String;", 0x1, -1, -1, -1, -1, -1 },
    { "init", NULL, 0x1, -1, -1, -1, -1, -1 },
  };
  static const void *ptrTable[] = {
    "()Ljava/lang/Class<*>;", "get",
    "LJavaLangIllegalArgumentException;LJavaLangIllegalAccessException;", "getBoolean", "getByte",
    "getChar", "getDouble", "getFloat", "getInt", "getLong", "getShort", "set", "setBoolean",
    "setByte", "setChar", "setDouble", "setFloat", "setInt", "setLong", "setShort", "getAnnotation",
    "<T::Ljava/lang/annotation/Annotation;>(Ljava/lang/Class<TT;>;)TT;" };
  static const J2ObjcClassInfo _JavaLangReflectField = {
    4, "Field", "java.lang.reflect", NULL, 0x1, 29, methods, 0, NULL, 0, NULL, NULL, NULL, -1,
    ptrTable };
  return &_JavaLangReflectField;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(JavaLangReflectField)
