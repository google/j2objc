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

typedef union {
  jint valueInt;
  jlong valueLong;
  jfloat valueFloat;
  jdouble valueDouble;
  jboolean valueBool;
  id valueId;
  const void *valuePtr;
} CGPValue;

#define CGPValueField_Int valueInt
#define CGPValueField_Long valueLong
#define CGPValueField_Float valueFloat
#define CGPValueField_Double valueDouble
#define CGPValueField_Bool valueBool
#define CGPValueField_Enum valueId
#define CGPValueField_Retainable valueId

typedef NS_OPTIONS(uint32_t, CGPMessageFlags) {
  CGPMessageFlagExtendable = 1 << 0,
  CGPMessageFlagMessageSetWireFormat = 1 << 1,
};

typedef NS_OPTIONS(uint32_t, CGPFieldFlags) {
  CGPFieldFlagRequired = 1 << 0,
  CGPFieldFlagRepeated = 1 << 1,
  CGPFieldFlagExtension = 1 << 2,
  CGPFieldFlagPacked = 1 << 3,
};

typedef struct CGPFieldData {
  const char *name;
  const char *javaName;
  int number;
  CGPFieldFlags flags;
  CGPFieldType type;
  CGPValue defaultValue;
  uint32_t hasBitIndex;
  uint32_t offset;
  const char *className;
  const char *containingType;
  const char *optionsData;
} CGPFieldData;

@interface ComGoogleProtobufDescriptors_Descriptor () {
 @package
  Class messageClass_;
  Class builderClass_;
  CGPMessageFlags flags_;
  size_t storageSize_;
  IOSObjectArray *fields_;
  ComGoogleProtobufGeneratedMessage *defaultInstance_;
}

- (instancetype)initWithMessageClass:(Class)messageClass
                        builderClass:(Class)builderClass
                               flags:(CGPMessageFlags)flags
                         storageSize:(size_t)storageSize
                              fields:(IOSObjectArray *)fields;

@end

@interface ComGoogleProtobufDescriptors_FieldDescriptor () {
 @package
  CGPFieldData *data_;
  uint32_t tag_;
  CGPFieldJavaType javaType_;
  // Either nil, a Descriptor or a EnumDescriptor depending on the field type.
  id valueType_;
  ComGoogleProtobufDescriptorProtos_FieldOptions *fieldOptions_;
}

- (instancetype)initWithData:(CGPFieldData *)data;

@end

@interface ComGoogleProtobufDescriptors_EnumDescriptor () {
 @package
  ptrdiff_t valueOffset_;
  IOSObjectArray *values_;
}

- (instancetype)initWithValueOffset:(ptrdiff_t)valueOffset values:(IOSObjectArray *)values;

@end

@interface ComGoogleProtobufDescriptors_EnumValueDescriptor () {
 @package
  JavaLangEnum<ComGoogleProtobufProtocolMessageEnum> *enum_;
  int number_;
}

- (instancetype)initWithValue:(JavaLangEnum<ComGoogleProtobufProtocolMessageEnum> *)value;

@end

// Functions that convert a value from its field storage type to the type
// expected by a reflection accessor. (accessing with a descriptor)
// For enums, the reflection type is a EnumValueDescriptor.
#define CGPToReflectionTypeInt(value, field) [JavaLangInteger valueOfWithInt:value]
#define CGPToReflectionTypeLong(value, field) [JavaLangLong valueOfWithLong:value]
#define CGPToReflectionTypeFloat(value, field) [JavaLangFloat valueOfWithFloat:value]
#define CGPToReflectionTypeDouble(value, field) [JavaLangDouble valueOfWithDouble:value]
#define CGPToReflectionTypeBool(value, field) [JavaLangBoolean valueOfWithBoolean:value]
#define CGPToReflectionTypeEnum(value, field) \
    ((CGPEnumDescriptor *)field->valueType_)->values_->buffer_[[(JavaLangEnum *)value ordinal]]
#define CGPToReflectionTypeRetainable(value, field) value

CF_EXTERN_C_BEGIN

void CGPInitDescriptor(
    CGPDescriptor **pDescriptor, Class messageClass, Class builderClass, CGPMessageFlags flags,
    size_t storageSize, jint fieldCount, CGPFieldData *fieldData);

CGP_ALWAYS_INLINE inline BOOL CGPIsExtendable(const CGPDescriptor *descriptor) {
  return descriptor->flags_ & CGPMessageFlagExtendable;
}

CGP_ALWAYS_INLINE inline BOOL CGPIsMessageSetWireFormat(const CGPDescriptor *descriptor) {
  return descriptor->flags_ & CGPMessageFlagMessageSetWireFormat;
}

CGPEnumDescriptor *CGPNewEnumDescriptor(
    Class enumClass, jint valuesCount, JavaLangEnum<ComGoogleProtobufProtocolMessageEnum> **values);

void CGPFieldFixDefaultValue(CGPFieldDescriptor *descriptor);

CGP_ALWAYS_INLINE inline int CGPFieldGetNumber(const CGPFieldDescriptor *field) {
  return field->data_->number;
}

CGP_ALWAYS_INLINE inline BOOL CGPFieldIsRequired(const CGPFieldDescriptor *field) {
  return field->data_->flags & CGPFieldFlagRequired;
}

CGP_ALWAYS_INLINE inline BOOL CGPFieldIsRepeated(const CGPFieldDescriptor *field) {
  return field->data_->flags & CGPFieldFlagRepeated;
}

CGP_ALWAYS_INLINE inline BOOL CGPFieldIsPacked(const CGPFieldDescriptor *field) {
  return field->data_->flags & CGPFieldFlagPacked;
}

CGP_ALWAYS_INLINE inline CGPFieldType CGPFieldGetType(const CGPFieldDescriptor *field) {
  return field->data_->type;
}

CGP_ALWAYS_INLINE inline CGPFieldJavaType CGPFieldGetJavaType(const CGPFieldDescriptor *field) {
  return field->javaType_;
}

CGP_ALWAYS_INLINE inline uint32_t CGPFieldGetHasBitIndex(const CGPFieldDescriptor *field) {
  return field->data_->hasBitIndex;
}

CGP_ALWAYS_INLINE inline uint32_t CGPFieldGetOffset(const CGPFieldDescriptor *field, Class cls) {
  return (uint32_t)class_getInstanceSize(cls) + field->data_->offset;
}

CGP_ALWAYS_INLINE inline BOOL CGPTypeIsGroup(CGPFieldType type) {
  return type == ComGoogleProtobufDescriptors_FieldDescriptor_Type_GROUP;
}

CGP_ALWAYS_INLINE inline BOOL CGPJavaTypeIsMessage(CGPFieldJavaType type) {
  return type == ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_MESSAGE;
}

CGP_ALWAYS_INLINE inline BOOL CGPJavaTypeIsEnum(CGPFieldJavaType type) {
  return type == ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_ENUM;
}

CGP_ALWAYS_INLINE inline int CGPEnumGetIntValue(CGPEnumDescriptor *descriptor, id enumObj) {
  return *(int *)((char *)enumObj + descriptor->valueOffset_);
}

CGPDescriptor *CGPFieldGetContainingType(CGPFieldDescriptor *field);

id CGPFieldGetDefaultValue(CGPFieldDescriptor *field);

BOOL CGPIsRetainedType(CGPFieldJavaType type);

size_t CGPGetTypeSize(CGPFieldJavaType type);

CGPEnumValueDescriptor *CGPEnumValueDescriptorFromInt(CGPEnumDescriptor *enumType, int value);

CF_EXTERN_C_END

// The remainder of this file is copied from the translation of the types
// FieldDescriptor.Type and FieldDescriptor.JavaType in Descriptor.java.

@interface ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum () {
 @public
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum *javaType_;
}
@end

J2OBJC_FIELD_SETTER(ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum, javaType_, ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum *)

@interface ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum () {
 @public
  id defaultDefault_;
}
@end

J2OBJC_FIELD_SETTER(ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum, defaultDefault_, id)

#endif // __ComGoogleProtobufDescriptors_PackagePrivate_H__
