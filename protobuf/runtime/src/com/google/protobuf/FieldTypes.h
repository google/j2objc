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

//  Declares the FieldDescriptor.Type and FieldDescriptor.JavaType enums.
//  These are copied directly from the translation of
//  com.google.protobuf.Descriptors.java.

#ifndef __ComGoogleProtobufFieldTypes_H__
#define __ComGoogleProtobufFieldTypes_H__

#import "com/google/protobuf/ProtocolMessageEnum.h"
#import "java/lang/Boolean.h"
#import "java/lang/Double.h"
#import "java/lang/Enum.h"
#import "java/lang/Float.h"
#import "java/lang/Integer.h"
#import "java/lang/Long.h"

@class ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum;
@class ComGoogleProtobufDescriptorProtos$FieldDescriptorProto$TypeEnum;

#define TYPE_Int jint
#define TYPE_Long jlong
#define TYPE_Float jfloat
#define TYPE_Double jdouble
#define TYPE_Bool jboolean
#define TYPE_Enum id
#define TYPE_Id id
#define TYPE_Retainable id

#define TYPE_RETAIN_Int(value) value
#define TYPE_RETAIN_Long(value) value
#define TYPE_RETAIN_Float(value) value
#define TYPE_RETAIN_Double(value) value
#define TYPE_RETAIN_Bool(value) value
#define TYPE_RETAIN_Enum(value) value
#define TYPE_RETAIN_Retainable(value) [value retain]

#define TYPE_ASSIGN_Int(assignee, value) assignee = value
#define TYPE_ASSIGN_Long(assignee, value) assignee = value
#define TYPE_ASSIGN_Float(assignee, value) assignee = value
#define TYPE_ASSIGN_Double(assignee, value) assignee = value
#define TYPE_ASSIGN_Bool(assignee, value) assignee = value
#define TYPE_ASSIGN_Enum(assignee, value) assignee = value
#define TYPE_ASSIGN_Retainable(assignee, value) \
  ([assignee autorelease], assignee = [value retain])

// Functions that box a value from its field storage type into an object type.
// For enums, the boxed type is the same as its storage type. (a Java enum
// object)
#define CGPBoxedValueInt(value) [JavaLangInteger valueOfWithInt:value]
#define CGPBoxedValueLong(value) [JavaLangLong valueOfWithLong:value]
#define CGPBoxedValueFloat(value) [JavaLangFloat valueOfWithFloat:value]
#define CGPBoxedValueDouble(value) [JavaLangDouble valueOfWithDouble:value]
#define CGPBoxedValueBool(value) [JavaLangBoolean valueOfWithBoolean:value]
#define CGPBoxedValueId(value) value

// Functions that unbox a value into its primitive type.
#define CGPUnboxValueInt(value) [value intValue]
#define CGPUnboxValueLong(value) [value longLongValue]
#define CGPUnboxValueFloat(value) [value floatValue]
#define CGPUnboxValueDouble(value) [value doubleValue]
#define CGPUnboxValueBool(value) [value booleanValue]
#define CGPUnboxValueEnum(value) value
#define CGPUnboxValueRetainable(value) value

// Functions that convert a value from its reflection to its storage type.
#define CGPFromReflectionTypeInt(value) [value intValue]
#define CGPFromReflectionTypeLong(value) [value longLongValue]
#define CGPFromReflectionTypeFloat(value) [value floatValue]
#define CGPFromReflectionTypeDouble(value) [value doubleValue]
#define CGPFromReflectionTypeBool(value) [value booleanValue]
#define CGPFromReflectionTypeEnum(value) ((CGPEnumValueDescriptor *)value)->enum_
#define CGPFromReflectionTypeRetainable(value) value

// Creates a switch statement over the java types grouping enums together with
// the other object types.
#define SWITCH_TYPES_NO_ENUM(type, CASE_MACRO) \
  switch (type) { \
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_INT: \
      CASE_MACRO(Int) \
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_LONG: \
      CASE_MACRO(Long) \
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_FLOAT: \
      CASE_MACRO(Float) \
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_DOUBLE: \
      CASE_MACRO(Double) \
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_BOOLEAN: \
      CASE_MACRO(Bool) \
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_STRING: \
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_BYTE_STRING: \
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_ENUM: \
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_MESSAGE: \
      CASE_MACRO(Id) \
  }

// Creates a switch statement over the java types separating enums from the
// other object types.
#define SWITCH_TYPES_WITH_ENUM(type, CASE_MACRO) \
  switch (type) { \
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_INT: \
      CASE_MACRO(Int) \
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_LONG: \
      CASE_MACRO(Long) \
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_FLOAT: \
      CASE_MACRO(Float) \
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_DOUBLE: \
      CASE_MACRO(Double) \
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_BOOLEAN: \
      CASE_MACRO(Bool) \
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_STRING: \
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_BYTE_STRING: \
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_MESSAGE: \
      CASE_MACRO(Retainable) \
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_ENUM: \
      CASE_MACRO(Enum) \
  }

// Declares the given macro once for each java field type, grouping enums and
// the other object types together as "Id".
#define FOR_EACH_TYPE_NO_ENUM(MACRO) \
  MACRO(Int) \
  MACRO(Long) \
  MACRO(Float) \
  MACRO(Double) \
  MACRO(Bool) \
  MACRO(Id)

// Declares the given macro once for each java field type, declaring it
// separately for enums (as "Enum) and the other object types (as "Retainable").
#define FOR_EACH_TYPE_WITH_ENUM(MACRO) \
  MACRO(Int) \
  MACRO(Long) \
  MACRO(Float) \
  MACRO(Double) \
  MACRO(Bool) \
  MACRO(Enum) \
  MACRO(Retainable)

// The remainder of this file is copied from the translation of the types
// FieldDescriptor.Type and FieldDescriptor.JavaType in Descriptor.java.

typedef NS_ENUM(NSUInteger, ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum) {
  ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_DOUBLE = 0,
  ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_FLOAT = 1,
  ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_INT64 = 2,
  ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_UINT64 = 3,
  ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_INT32 = 4,
  ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_FIXED64 = 5,
  ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_FIXED32 = 6,
  ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_BOOL = 7,
  ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_STRING = 8,
  ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_GROUP = 9,
  ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_MESSAGE = 10,
  ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_BYTES = 11,
  ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_UINT32 = 12,
  ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_ENUM = 13,
  ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_SFIXED32 = 14,
  ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_SFIXED64 = 15,
  ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_SINT32 = 16,
  ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_SINT64 = 17,
};
// TODO(kstanger): Remove after users have migrated.
#define ComGoogleProtobufDescriptors_FieldDescriptor_Type ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum
#define ComGoogleProtobufDescriptors_FieldDescriptor_Type_DOUBLE ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_DOUBLE
#define ComGoogleProtobufDescriptors_FieldDescriptor_Type_FLOAT ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_FLOAT
#define ComGoogleProtobufDescriptors_FieldDescriptor_Type_INT64 ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_INT64
#define ComGoogleProtobufDescriptors_FieldDescriptor_Type_UINT64 ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_UINT64
#define ComGoogleProtobufDescriptors_FieldDescriptor_Type_INT32 ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_INT32
#define ComGoogleProtobufDescriptors_FieldDescriptor_Type_FIXED64 ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_FIXED64
#define ComGoogleProtobufDescriptors_FieldDescriptor_Type_FIXED32 ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_FIXED32
#define ComGoogleProtobufDescriptors_FieldDescriptor_Type_BOOL ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_BOOL
#define ComGoogleProtobufDescriptors_FieldDescriptor_Type_STRING ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_STRING
#define ComGoogleProtobufDescriptors_FieldDescriptor_Type_GROUP ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_GROUP
#define ComGoogleProtobufDescriptors_FieldDescriptor_Type_MESSAGE ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_MESSAGE
#define ComGoogleProtobufDescriptors_FieldDescriptor_Type_BYTES ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_BYTES
#define ComGoogleProtobufDescriptors_FieldDescriptor_Type_UINT32 ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_UINT32
#define ComGoogleProtobufDescriptors_FieldDescriptor_Type_ENUM ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_ENUM
#define ComGoogleProtobufDescriptors_FieldDescriptor_Type_SFIXED32 ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_SFIXED32
#define ComGoogleProtobufDescriptors_FieldDescriptor_Type_SFIXED64 ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_SFIXED64
#define ComGoogleProtobufDescriptors_FieldDescriptor_Type_SINT32 ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_SINT32
#define ComGoogleProtobufDescriptors_FieldDescriptor_Type_SINT64 ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_SINT64

typedef ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum CGPFieldType;

@interface ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum : JavaLangEnum < NSCopying > {
}

- (instancetype)initWithComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum:(ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum *)javaType
                                                                     withNSString:(NSString *)__name
                                                                          withInt:(jint)__ordinal;

- (ComGoogleProtobufDescriptorProtos$FieldDescriptorProto$TypeEnum *)toProto;

- (ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum *)getJavaType;

+ (ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum *)valueOfWithComGoogleProtobufDescriptorProtos$FieldDescriptorProto$TypeEnum:(ComGoogleProtobufDescriptorProtos$FieldDescriptorProto$TypeEnum *)type;
// TODO(kstanger): Remove after users have migrated.
#define valueOfWithComGoogleProtobufDescriptorProtos_FieldDescriptorProto_TypeEnum valueOfWithComGoogleProtobufDescriptorProtos$FieldDescriptorProto$TypeEnum

+ (IOSObjectArray *)values;
FOUNDATION_EXPORT IOSObjectArray *ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values();

+ (ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum *)valueOfWithNSString:(NSString *)name;
FOUNDATION_EXPORT ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum *ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_valueOfWithNSString_(NSString *name);
// TODO(kstanger): Remove after users have migrated.
#define ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_valueOfWithNSString_ ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_valueOfWithNSString_

- (id)copyWithZone:(NSZone *)zone;

@end
// TODO(kstanger): Remove after users have migrated.
#define ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum

J2OBJC_STATIC_INIT(ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum)

FOUNDATION_EXPORT ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum *ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_valueOfWithComGoogleProtobufDescriptorProtos$FieldDescriptorProto$TypeEnum_(ComGoogleProtobufDescriptorProtos$FieldDescriptorProto$TypeEnum *type);
// TODO(kstanger): Remove after users have migrated.
#define ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_valueOfWithComGoogleProtobufDescriptorProtos_FieldDescriptorProto_TypeEnum_ ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_valueOfWithComGoogleProtobufDescriptorProtos$FieldDescriptorProto$TypeEnum_

FOUNDATION_EXPORT ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum *ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values_[];

#define ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_DOUBLE ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_DOUBLE]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum, DOUBLE)

#define ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_FLOAT ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_FLOAT]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum, FLOAT)

#define ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_INT64 ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_INT64]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum, INT64)

#define ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_UINT64 ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_UINT64]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum, UINT64)

#define ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_INT32 ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_INT32]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum, INT32)

#define ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_FIXED64 ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_FIXED64]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum, FIXED64)

#define ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_FIXED32 ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_FIXED32]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum, FIXED32)

#define ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_BOOL ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_BOOL]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum, BOOL)

#define ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_STRING ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_STRING]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum, STRING)

#define ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_GROUP ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_GROUP]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum, GROUP)

#define ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_MESSAGE ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_MESSAGE]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum, MESSAGE)

#define ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_BYTES ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_BYTES]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum, BYTES)

#define ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_UINT32 ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_UINT32]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum, UINT32)

#define ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_ENUM ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_ENUM]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum, ENUM)

#define ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_SFIXED32 ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_SFIXED32]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum, SFIXED32)

#define ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_SFIXED64 ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_SFIXED64]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum, SFIXED64)

#define ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_SINT32 ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_SINT32]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum, SINT32)

#define ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_SINT64 ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$Type_Enum_SINT64]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum, SINT64)

J2OBJC_TYPE_LITERAL_HEADER(ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum)
// TODO(kstanger): Remove these defines when users have migrated.
#define ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_class_ ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_class_

typedef NS_ENUM(NSUInteger, ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum) {
  ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_INT = 0,
  ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_LONG = 1,
  ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_FLOAT = 2,
  ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_DOUBLE = 3,
  ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_BOOLEAN = 4,
  ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_STRING = 5,
  ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_BYTE_STRING = 6,
  ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_ENUM = 7,
  ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_MESSAGE = 8,
};
// TODO(kstanger): Remove these defines when users have migrated.
#define ComGoogleProtobufDescriptors_FieldDescriptor_JavaType ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum
#define ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_INT ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_INT
#define ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_LONG ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_LONG
#define ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_FLOAT ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_FLOAT
#define ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_DOUBLE ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_DOUBLE
#define ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_BOOLEAN ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_BOOLEAN
#define ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_STRING ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_STRING
#define ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_BYTE_STRING ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_BYTE_STRING
#define ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_ENUM ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_ENUM
#define ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_MESSAGE ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_MESSAGE

@interface ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum : JavaLangEnum < NSCopying > {
}

- (instancetype)initWithId:(id)defaultDefault
              withNSString:(NSString *)__name
                   withInt:(jint)__ordinal;

+ (IOSObjectArray *)values;
FOUNDATION_EXPORT IOSObjectArray *ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_values();

+ (ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum *)valueOfWithNSString:(NSString *)name;

FOUNDATION_EXPORT ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum *ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_valueOfWithNSString_(NSString *name);
- (id)copyWithZone:(NSZone *)zone;

@end
// TODO(kstanger): Remove after users have migrated.
#define ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum

J2OBJC_STATIC_INIT(ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum)

FOUNDATION_EXPORT ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum *ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_values_[];

#define ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_INT ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_INT]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum, INT)

#define ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_LONG ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_LONG]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum, LONG)

#define ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_FLOAT ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_FLOAT]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum, FLOAT)

#define ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_DOUBLE ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_DOUBLE]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum, DOUBLE)

#define ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_BOOLEAN ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_BOOLEAN]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum, BOOLEAN)

#define ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_STRING ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_STRING]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum, STRING)

#define ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_BYTE_STRING ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_BYTE_STRING]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum, BYTE_STRING)

#define ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_ENUM ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_ENUM]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum, ENUM)

#define ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_MESSAGE ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_values_[ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_MESSAGE]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum, MESSAGE)

J2OBJC_TYPE_LITERAL_HEADER(ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum)
// TODO(kstanger): Remove after users have migrated.
#define ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_class_ ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_class_

#endif // __ComGoogleProtobufFieldTypes_H__
