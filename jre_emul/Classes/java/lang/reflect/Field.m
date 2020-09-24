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
  return AUTORELEASE([[JavaLangReflectField alloc] initWithIvar:ivar
                                                      withClass:aClass
                                                   withMetadata:metadata]);
}

- (NSString *)getName {
  const char *javaName = JrePtrAtIndex(ptrTable_, metadata_->javaNameIdx);
  if (javaName) {
    return [NSString stringWithUTF8String:javaName];
  } else if (IsStatic(self)) {
    return [NSString stringWithUTF8String:metadata_->name];
  } else {
    // Remove the trailing "_" from instance fields.
    return AUTORELEASE([[NSString alloc] initWithBytes:metadata_->name
                                                length:strlen(metadata_->name) - 1
                                              encoding:NSUTF8StringEncoding]);
  }
}

- (NSString *)description {
  NSString *mods = JavaLangReflectModifier_toStringWithInt_(metadata_->modifiers);
  if ([mods length] > 0) {
    return [NSString stringWithFormat:@"%@ %@ %@.%@", mods, [[self getType] getName],
            [[self getDeclaringClass] getName], [self getName]];
  }
  return [NSString stringWithFormat:@"%@ %@.%@", [[self getType] getName],
          [[self getDeclaringClass] getName], [self getName]];
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
    (void)nil_chk(object);
    if (![field->declaringClass_ isInstance:object]) {
      @throw create_JavaLangIllegalArgumentException_initWithNSString_(@"field type mismatch");
    }
    if (field->ivar_) {
      [type __readRawValue:rawValue fromAddress:((char *)object) + ivar_getOffset(field->ivar_)];
    } else {
      // May be a mapped class "virtual" field, call equivalent accessor method if it exists.
      SEL getter = NSSelectorFromString([NSString stringWithFormat:@"__%@", [field getName]]);
      if (getter && [object respondsToSelector:getter]) {
        rawValue->asId = [object performSelector:getter];
      } else {
        // It's a final instance field, return its constant value.
        *rawValue = field->metadata_->constantValue;
      }
    }
  }
  if (![type __convertRawValue:rawValue toType:toType]) {
    @throw create_JavaLangIllegalArgumentException_initWithNSString_(@"field type mismatch");
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
    @throw create_JavaLangIllegalArgumentException_initWithNSString_(@"field type mismatch");
  }
  if (IsStatic(field)) {
    if (IsFinal(field) && !field->accessible_) {
      @throw create_JavaLangIllegalAccessException_initWithNSString_(
          @"Cannot set static final field");
    }
    [type __writeRawValue:rawValue toAddress:field->ptrTable_[field->metadata_->staticRefIdx]];
  } else {
    (void)nil_chk(object);
    if (IsFinal(field) && !field->accessible_) {
      @throw create_JavaLangIllegalAccessException_initWithNSString_(@"Cannot set final field");
    }
    if (field->ivar_) {
      [type __writeRawValue:rawValue toAddress:((char *)object) + ivar_getOffset(field->ivar_)];
    } else {
      // May be a mapped class "virtual" field, call equivalent accessor method if it exists.
      SEL setter = NSSelectorFromString([NSString stringWithFormat:@"__set%@:", [field getName]]);
      if (setter && [object respondsToSelector:setter]) {
        [object performSelector:setter withObject:rawValue->asId];
      }
      // else: It's a final instance field, return without any side effects.
    }
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
    RETAIN_AND_AUTORELEASE(result);
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
  id (*annotations)(void) = JrePtrAtIndex(ptrTable_, metadata_->annotationsIdx);
  if (annotations) {
    return annotations();
  }
  return [IOSObjectArray arrayWithLength:0 type:JavaLangAnnotationAnnotation_class_()];
}

- (jlong)unsafeOffset {
  if (IsStatic(self)) {
    return (jlong)JrePtrAtIndex(ptrTable_, metadata_->staticRefIdx);
  } else {
    return (jlong)ivar_getOffset(ivar_);
  }
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
  static J2ObjcMethodInfo methods[] = {
    { NULL, NULL, 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "I", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "LIOSClass;", 0x1, -1, -1, -1, 0, -1, -1 },
    { NULL, "LJavaLangReflectType;", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "LIOSClass;", 0x1, -1, -1, -1, 0, -1, -1 },
    { NULL, "LNSObject;", 0x1, 1, 2, 3, -1, -1, -1 },
    { NULL, "Z", 0x1, 4, 2, 3, -1, -1, -1 },
    { NULL, "B", 0x1, 5, 2, 3, -1, -1, -1 },
    { NULL, "C", 0x1, 6, 2, 3, -1, -1, -1 },
    { NULL, "D", 0x1, 7, 2, 3, -1, -1, -1 },
    { NULL, "F", 0x1, 8, 2, 3, -1, -1, -1 },
    { NULL, "I", 0x1, 9, 2, 3, -1, -1, -1 },
    { NULL, "J", 0x1, 10, 2, 3, -1, -1, -1 },
    { NULL, "S", 0x1, 11, 2, 3, -1, -1, -1 },
    { NULL, "V", 0x1, 12, 13, 3, -1, -1, -1 },
    { NULL, "V", 0x1, 14, 15, 3, -1, -1, -1 },
    { NULL, "V", 0x1, 16, 17, 3, -1, -1, -1 },
    { NULL, "V", 0x1, 18, 19, 3, -1, -1, -1 },
    { NULL, "V", 0x1, 20, 21, 3, -1, -1, -1 },
    { NULL, "V", 0x1, 22, 23, 3, -1, -1, -1 },
    { NULL, "V", 0x1, 24, 25, 3, -1, -1, -1 },
    { NULL, "V", 0x1, 26, 27, 3, -1, -1, -1 },
    { NULL, "V", 0x1, 28, 29, 3, -1, -1, -1 },
    { NULL, "LJavaLangAnnotationAnnotation;", 0x1, 30, 31, -1, 32, -1, -1 },
    { NULL, "[LJavaLangAnnotationAnnotation;", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "Z", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "Z", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x1, -1, -1, -1, -1, -1, -1 },
  };
  #pragma clang diagnostic push
  #pragma clang diagnostic ignored "-Wobjc-multiple-method-names"
  methods[0].selector = @selector(init);
  methods[1].selector = @selector(getName);
  methods[2].selector = @selector(getModifiers);
  methods[3].selector = @selector(getType);
  methods[4].selector = @selector(getGenericType);
  methods[5].selector = @selector(getDeclaringClass);
  methods[6].selector = @selector(getWithId:);
  methods[7].selector = @selector(getBooleanWithId:);
  methods[8].selector = @selector(getByteWithId:);
  methods[9].selector = @selector(getCharWithId:);
  methods[10].selector = @selector(getDoubleWithId:);
  methods[11].selector = @selector(getFloatWithId:);
  methods[12].selector = @selector(getIntWithId:);
  methods[13].selector = @selector(getLongWithId:);
  methods[14].selector = @selector(getShortWithId:);
  methods[15].selector = @selector(setWithId:withId:);
  methods[16].selector = @selector(setBooleanWithId:withBoolean:);
  methods[17].selector = @selector(setByteWithId:withByte:);
  methods[18].selector = @selector(setCharWithId:withChar:);
  methods[19].selector = @selector(setDoubleWithId:withDouble:);
  methods[20].selector = @selector(setFloatWithId:withFloat:);
  methods[21].selector = @selector(setIntWithId:withInt:);
  methods[22].selector = @selector(setLongWithId:withLong:);
  methods[23].selector = @selector(setShortWithId:withShort:);
  methods[24].selector = @selector(getAnnotationWithIOSClass:);
  methods[25].selector = @selector(getDeclaredAnnotations);
  methods[26].selector = @selector(isSynthetic);
  methods[27].selector = @selector(isEnumConstant);
  methods[28].selector = @selector(toGenericString);
  #pragma clang diagnostic pop
  static const void *ptrTable[] = {
    "()Ljava/lang/Class<*>;", "get", "LNSObject;",
    "LJavaLangIllegalArgumentException;LJavaLangIllegalAccessException;", "getBoolean", "getByte",
    "getChar", "getDouble", "getFloat", "getInt", "getLong", "getShort", "set",
    "LNSObject;LNSObject;", "setBoolean", "LNSObject;Z", "setByte", "LNSObject;B", "setChar",
    "LNSObject;C", "setDouble", "LNSObject;D", "setFloat", "LNSObject;F", "setInt", "LNSObject;I",
    "setLong", "LNSObject;J", "setShort", "LNSObject;S", "getAnnotation", "LIOSClass;",
    "<T::Ljava/lang/annotation/Annotation;>(Ljava/lang/Class<TT;>;)TT;" };
  static const J2ObjcClassInfo _JavaLangReflectField = {
    "Field", "java.lang.reflect", ptrTable, methods, NULL, 7, 0x1, 29, 0, -1, -1, -1, -1, -1 };
  return &_JavaLangReflectField;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(JavaLangReflectField)
