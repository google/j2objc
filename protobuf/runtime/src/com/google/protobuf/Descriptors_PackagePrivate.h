// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
// https://developers.google.com/protocol-buffers/
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
//     * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// in the documentation and/or other materials provided with the
// distribution.
//     * Neither the name of Google Inc. nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

// DO NOT INCLUDE EXTERNALLY.
// Contains declarations used within the runtime and generated protocol buffers.

#ifndef __ComGoogleProtobufDescriptors_PackagePrivate_H__
#define __ComGoogleProtobufDescriptors_PackagePrivate_H__

#import "com/google/protobuf/Descriptors.h"

#import "objc/runtime.h"

typedef union {
  jint valueInt;
  jlong valueLong;
  jfloat valueFloat;
  jdouble valueDouble;
  bool valueBool;
  __unsafe_unretained id valueId;
  __unsafe_unretained JavaLangEnum<ComGoogleProtobufProtocolMessageEnum> *valueEnum;
  const void *valuePtr;
} CGPValue;

#define CGPValueField_Int valueInt
#define CGPValueField_Long valueLong
#define CGPValueField_Float valueFloat
#define CGPValueField_Double valueDouble
#define CGPValueField_Bool valueBool
#define CGPValueField_Enum valueEnum
#define CGPValueField_Id valueId

typedef NS_OPTIONS(uint32_t, CGPMessageFlags) {
  CGPMessageFlagExtendable = 1 << 0,
  CGPMessageFlagMessageSetWireFormat = 1 << 1,
};

typedef NS_OPTIONS(uint32_t, CGPFieldFlags) {
  CGPFieldFlagRequired = 1 << 0,
  CGPFieldFlagRepeated = 1 << 1,
  CGPFieldFlagExtension = 1 << 2,
  CGPFieldFlagPacked = 1 << 3,
  CGPFieldFlagMap = 1 << 4,
};

typedef struct CGPFieldData {
  const char *name;
  jint number;
  CGPFieldFlags flags;
  CGPFieldType type;
  CGPValue defaultValue;
  uint32_t hasBitIndex;
  uint32_t offset;
  union {
    Class objcType;
    struct CGPFieldData *mapEntryFields;
  };
  const __unsafe_unretained id *descriptorRef;
  const char *containingType;
  const char *optionsData;
} CGPFieldData;

typedef struct CGPOneofData {
  const char *name;
  const char *javaName;
  uint32_t firstFieldIdx;
  uint32_t fieldCount;
  uint32_t offset;
} CGPOneofData;

@interface ComGoogleProtobufDescriptors_Descriptor () {
 @package
  Class messageClass_;
  Class builderClass_;
  CGPMessageFlags flags_;
  size_t storageSize_;
  IOSObjectArray *fields_;
  IOSObjectArray *serializationOrderFields_;
  IOSObjectArray *oneofs_;
  ComGoogleProtobufGeneratedMessage *defaultInstance_;
}

- (instancetype)initWithMessageClass:(Class)messageClass
                        builderClass:(Class)builderClass
                               flags:(CGPMessageFlags)flags
                         storageSize:(size_t)storageSize;

@end

@interface ComGoogleProtobufDescriptors_FieldDescriptor () {
 @package
  CGPFieldData *data_;
  uint32_t tag_;
  CGPFieldJavaType javaType_;
  // Either nil, a Descriptor or a EnumDescriptor depending on the field type.
  id valueType_;
  ComGoogleProtobufDescriptorProtos_FieldOptions *fieldOptions_;
  CGPOneofDescriptor *containingOneof_;
}

- (instancetype)initWithData:(CGPFieldData *)data;

@end

@interface ComGoogleProtobufDescriptors_EnumDescriptor () {
 @public
  ptrdiff_t valueOffset_;
  IOSObjectArray *values_;
  bool is_closed_;
}

- (instancetype)initWithValueOffset:(ptrdiff_t)valueOffset
                     retainedValues:(IOSObjectArray *)values
                          is_closed:(bool)is_closed;

@end

@interface ComGoogleProtobufDescriptors_EnumValueDescriptor () {
 @package
  JavaLangEnum<ComGoogleProtobufProtocolMessageEnum> *enum_;
  jint number_;
}
@end

@interface ComGoogleProtobufDescriptors_OneofDescriptor () {
 @package
  const CGPOneofData *data_;
  CGPDescriptor *containingType_;
}

- (instancetype)initWithData:(const CGPOneofData *)data
              containingType:(CGPDescriptor *)containingType;

@end

CF_EXTERN_C_BEGIN

NS_RETURNS_RETAINED CGPDescriptor *CGPInitDescriptor(Class messageClass, Class builderClass,
                                                     CGPMessageFlags flags, size_t storageSize);

void CGPInitFields(CGPDescriptor *descriptor, jint fieldCount, CGPFieldData *fieldData,
                   jint oneofCount, const CGPOneofData *oneofData);

CGP_ALWAYS_INLINE BOOL CGPIsExtendable(const CGPDescriptor *descriptor) {
  return descriptor->flags_ & CGPMessageFlagExtendable;
}

CGP_ALWAYS_INLINE BOOL CGPIsMessageSetWireFormat(const CGPDescriptor *descriptor) {
  return descriptor->flags_ & CGPMessageFlagMessageSetWireFormat;
}

IOSObjectArray *CGPGetSerializationOrderFields(CGPDescriptor *descriptor);

NS_RETURNS_RETAINED CGPEnumDescriptor *CGPInitializeEnumType(
    Class enumClass, jint valuesCount,
    __strong JavaLangEnum<ComGoogleProtobufProtocolMessageEnum> *values[],
    __strong NSString **names, jint *intValues, bool is_closed);

void CGPInitializeOneofCaseEnum(Class enumClass, jint valuesCount,
                                __strong JavaLangEnum<ComGoogleProtobufInternal_EnumLite> *values[],
                                __strong NSString **names, jint *intValues);

id CGPValueOfEnumOrOneOfWithNSString(NSString *name, __strong id values[], jint count);

id CGPValueOfEnumOrOneOfWithInt(jint value, __strong id values[], jint count);

CGP_ALWAYS_INLINE jint CGPFieldGetNumber(const CGPFieldDescriptor *field) {
  return field->data_->number;
}

CGP_ALWAYS_INLINE BOOL CGPFieldIsRequired(const CGPFieldDescriptor *field) {
  return field->data_->flags & CGPFieldFlagRequired;
}

CGP_ALWAYS_INLINE BOOL CGPFieldIsRepeated(const CGPFieldDescriptor *field) {
  return field->data_->flags & CGPFieldFlagRepeated;
}

CGP_ALWAYS_INLINE BOOL CGPFieldIsMap(const CGPFieldDescriptor *field) {
  return field->data_->flags & CGPFieldFlagMap;
}

CGP_ALWAYS_INLINE CGPFieldDescriptor *CGPFieldMapKey(const CGPFieldDescriptor *field) {
  return ((CGPDescriptor *)field->valueType_)->fields_->buffer_[0];
}

CGP_ALWAYS_INLINE CGPFieldDescriptor *CGPFieldMapValue(const CGPFieldDescriptor *field) {
  return ((CGPDescriptor *)field->valueType_)->fields_->buffer_[1];
}

CGP_ALWAYS_INLINE BOOL CGPFieldIsPacked(const CGPFieldDescriptor *field) {
  return field->data_->flags & CGPFieldFlagPacked;
}

CGP_ALWAYS_INLINE CGPFieldType CGPFieldGetType(const CGPFieldDescriptor *field) {
  return field->data_->type;
}

CGP_ALWAYS_INLINE CGPFieldJavaType CGPFieldGetJavaType(const CGPFieldDescriptor *field) {
  return field->javaType_;
}

CGP_ALWAYS_INLINE uint32_t CGPFieldGetHasBitIndex(const CGPFieldDescriptor *field) {
  return field->data_->hasBitIndex;
}

CGP_ALWAYS_INLINE uint32_t CGPFieldGetOffset(const CGPFieldDescriptor *field, Class cls) {
  return (uint32_t)class_getInstanceSize(cls) + field->data_->offset;
}

CGP_ALWAYS_INLINE BOOL CGPTypeIsGroup(CGPFieldType type) {
  return type == ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_GROUP;
}

CGP_ALWAYS_INLINE BOOL CGPJavaTypeIsMessage(CGPFieldJavaType type) {
  return type == ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_MESSAGE;
}

CGP_ALWAYS_INLINE BOOL CGPFieldTypeIsMessage(const CGPFieldDescriptor *field) {
  return CGPJavaTypeIsMessage(CGPFieldGetJavaType(field));
}

CGP_ALWAYS_INLINE BOOL CGPJavaTypeIsEnum(CGPFieldJavaType type) {
  return type == ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_ENUM;
}

CGP_ALWAYS_INLINE jint CGPEnumGetIntValue(CGPEnumDescriptor *descriptor, TYPE_Enum enumObj) {
  return *(jint *)((char *)(ARCBRIDGE void *)enumObj + descriptor->valueOffset_);
}

id CGPFieldGetDefaultValue(CGPFieldDescriptor *field);

Class<ComGoogleProtobufInternal_EnumLite> CGPOneofGetCaseClass(CGPOneofDescriptor *oneof);

CGP_ALWAYS_INLINE uint32_t CGPOneofGetOffset(const CGPOneofDescriptor *oneof, Class cls) {
  return (uint32_t)class_getInstanceSize(cls) + oneof->data_->offset;
}

BOOL CGPIsRetainedType(CGPFieldJavaType type);

size_t CGPGetTypeSize(CGPFieldJavaType type);

CGPEnumValueDescriptor *CGPEnumValueDescriptorFromInt(CGPEnumDescriptor *enumType, jint value);

CF_EXTERN_C_END

// The remainder of this file is copied from the translation of the types
// FieldDescriptor.Type and FieldDescriptor.JavaType in Descriptor.java.

@interface ComGoogleProtobufDescriptors_FieldDescriptor_Type () {
 @public
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *javaType_;
}

@end

J2OBJC_FIELD_SETTER(ComGoogleProtobufDescriptors_FieldDescriptor_Type, javaType_,
                    ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *)

@interface ComGoogleProtobufDescriptors_FieldDescriptor_JavaType () {
 @public
  id defaultDefault_;
}

@end

J2OBJC_FIELD_SETTER(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, defaultDefault_, id)

// Functions that box a value from its field storage type into an object type.
// For enums, the boxed type is a Java enum object.
CGP_ALWAYS_INLINE JavaLangInteger *CGPBoxedValueInt(jint value) {
  return [JavaLangInteger valueOfWithInt:value];
}
CGP_ALWAYS_INLINE JavaLangLong *CGPBoxedValueLong(jlong value) {
  return [JavaLangLong valueOfWithLong:value];
}
CGP_ALWAYS_INLINE JavaLangFloat *CGPBoxedValueFloat(jfloat value) {
  return [JavaLangFloat valueOfWithFloat:value];
}
CGP_ALWAYS_INLINE JavaLangDouble *CGPBoxedValueDouble(jdouble value) {
  return [JavaLangDouble valueOfWithDouble:value];
}
CGP_ALWAYS_INLINE JavaLangBoolean *CGPBoxedValueBool(bool value) {
  return [JavaLangBoolean valueOfWithBoolean:value];
}
CGP_ALWAYS_INLINE JavaLangEnum<ComGoogleProtobufProtocolMessageEnum> *CGPBoxedValueEnum(
    JavaLangEnum<ComGoogleProtobufProtocolMessageEnum> *value) {
  return value;
}
CGP_ALWAYS_INLINE id CGPBoxedValueId(id value) { return value; }

// Functions that unbox a value into its primitive type.
CGP_ALWAYS_INLINE jint CGPUnboxValueInt(JavaLangInteger *value) { return [value intValue]; }
CGP_ALWAYS_INLINE jlong CGPUnboxValueLong(JavaLangLong *value) { return [value longLongValue]; }
CGP_ALWAYS_INLINE jfloat CGPUnboxValueFloat(JavaLangFloat *value) { return [value floatValue]; }
CGP_ALWAYS_INLINE jdouble CGPUnboxValueDouble(JavaLangDouble *value) { return [value doubleValue]; }
CGP_ALWAYS_INLINE bool CGPUnboxValueBool(JavaLangBoolean *value) { return [value booleanValue]; }
CGP_ALWAYS_INLINE JavaLangEnum<ComGoogleProtobufProtocolMessageEnum> *CGPUnboxValueEnum(
    JavaLangEnum<ComGoogleProtobufProtocolMessageEnum> *value) {
  return value;
}
CGP_ALWAYS_INLINE id CGPUnboxValueId(id value) { return value; }

// Functions that convert a value from its reflection to its storage type.
CGP_ALWAYS_INLINE jint CGPFromReflectionTypeInt(JavaLangInteger *value) { return [value intValue]; }
CGP_ALWAYS_INLINE jlong CGPFromReflectionTypeLong(JavaLangLong *value) {
  return [value longLongValue];
}
CGP_ALWAYS_INLINE jfloat CGPFromReflectionTypeFloat(JavaLangFloat *value) {
  return [value floatValue];
}
CGP_ALWAYS_INLINE jdouble CGPFromReflectionTypeDouble(JavaLangDouble *value) {
  return [value doubleValue];
}
CGP_ALWAYS_INLINE bool CGPFromReflectionTypeBool(JavaLangBoolean *value) {
  return [value booleanValue];
}
CGP_ALWAYS_INLINE JavaLangEnum<ComGoogleProtobufProtocolMessageEnum> *CGPFromReflectionTypeEnum(
    CGPEnumValueDescriptor *value) {
  return value->enum_;
}
CGP_ALWAYS_INLINE id CGPFromReflectionTypeId(id value) { return value; }

// Functions that convert a value from its field storage type to the type
// expected by a reflection accessor. (accessing with a descriptor)
// For enums, the reflection type is a EnumValueDescriptor.
CGP_ALWAYS_INLINE JavaLangInteger *CGPToReflectionTypeInt(jint value, CGPFieldDescriptor *field) {
  return [JavaLangInteger valueOfWithInt:value];
}
CGP_ALWAYS_INLINE JavaLangLong *CGPToReflectionTypeLong(jlong value, CGPFieldDescriptor *field) {
  return [JavaLangLong valueOfWithLong:value];
}
CGP_ALWAYS_INLINE JavaLangFloat *CGPToReflectionTypeFloat(jfloat value, CGPFieldDescriptor *field) {
  return [JavaLangFloat valueOfWithFloat:value];
}
CGP_ALWAYS_INLINE JavaLangDouble *CGPToReflectionTypeDouble(jdouble value,
                                                            CGPFieldDescriptor *field) {
  return [JavaLangDouble valueOfWithDouble:value];
}
CGP_ALWAYS_INLINE JavaLangBoolean *CGPToReflectionTypeBool(bool value, CGPFieldDescriptor *field) {
  return [JavaLangBoolean valueOfWithBoolean:value];
}
CGP_ALWAYS_INLINE CGPEnumValueDescriptor *CGPToReflectionTypeEnum(
    JavaLangEnum<ComGoogleProtobufProtocolMessageEnum> *value, CGPFieldDescriptor *field) {
  return ((CGPEnumDescriptor *)field->valueType_)->values_->buffer_[[value ordinal]];
}
CGP_ALWAYS_INLINE id CGPToReflectionTypeId(id value, CGPFieldDescriptor *field) {
  return RETAIN_AND_AUTORELEASE(value);
}

#endif  // __ComGoogleProtobufDescriptors_PackagePrivate_H__
