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

@class ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum;
@class ComGoogleProtobufDescriptorProtos_FieldDescriptorProto_TypeEnum;

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
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_INT: \
      CASE_MACRO(Int) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_LONG: \
      CASE_MACRO(Long) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_FLOAT: \
      CASE_MACRO(Float) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_DOUBLE: \
      CASE_MACRO(Double) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_BOOLEAN: \
      CASE_MACRO(Bool) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_STRING: \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_BYTE_STRING: \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_ENUM: \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_MESSAGE: \
      CASE_MACRO(Id) \
  }

// Creates a switch statement over the java types separating enums from the
// other object types.
#define SWITCH_TYPES_WITH_ENUM(type, CASE_MACRO) \
  switch (type) { \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_INT: \
      CASE_MACRO(Int) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_LONG: \
      CASE_MACRO(Long) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_FLOAT: \
      CASE_MACRO(Float) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_DOUBLE: \
      CASE_MACRO(Double) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_BOOLEAN: \
      CASE_MACRO(Bool) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_STRING: \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_BYTE_STRING: \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_MESSAGE: \
      CASE_MACRO(Retainable) \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_ENUM: \
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

typedef NS_ENUM(NSUInteger, ComGoogleProtobufDescriptors_FieldDescriptor_Type) {
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_DOUBLE = 0,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_FLOAT = 1,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_INT64 = 2,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_UINT64 = 3,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_INT32 = 4,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_FIXED64 = 5,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_FIXED32 = 6,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_BOOL = 7,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_STRING = 8,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_GROUP = 9,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_MESSAGE = 10,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_BYTES = 11,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_UINT32 = 12,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_ENUM = 13,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_SFIXED32 = 14,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_SFIXED64 = 15,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_SINT32 = 16,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_SINT64 = 17,
};

typedef ComGoogleProtobufDescriptors_FieldDescriptor_Type CGPFieldType;

@interface ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum : JavaLangEnum < NSCopying > {
}

- (instancetype)initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum:(ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum *)javaType
                                                                     withNSString:(NSString *)__name
                                                                          withInt:(jint)__ordinal;

- (ComGoogleProtobufDescriptorProtos_FieldDescriptorProto_TypeEnum *)toProto;

- (ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum *)getJavaType;

+ (ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum *)valueOfWithComGoogleProtobufDescriptorProtos_FieldDescriptorProto_TypeEnum:(ComGoogleProtobufDescriptorProtos_FieldDescriptorProto_TypeEnum *)type;

+ (IOSObjectArray *)values;
FOUNDATION_EXPORT IOSObjectArray *ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values();

+ (ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum *)valueOfWithNSString:(NSString *)name;

FOUNDATION_EXPORT ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum *ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_valueOfWithNSString_(NSString *name);
- (id)copyWithZone:(NSZone *)zone;

@end

J2OBJC_STATIC_INIT(ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum)

FOUNDATION_EXPORT ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum *ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_valueOfWithComGoogleProtobufDescriptorProtos_FieldDescriptorProto_TypeEnum_(ComGoogleProtobufDescriptorProtos_FieldDescriptorProto_TypeEnum *type);

FOUNDATION_EXPORT ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum *ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values_[];

#define ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_DOUBLE ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_Type_DOUBLE]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum, DOUBLE)

#define ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_FLOAT ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_Type_FLOAT]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum, FLOAT)

#define ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_INT64 ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_Type_INT64]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum, INT64)

#define ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_UINT64 ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_Type_UINT64]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum, UINT64)

#define ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_INT32 ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_Type_INT32]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum, INT32)

#define ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_FIXED64 ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_Type_FIXED64]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum, FIXED64)

#define ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_FIXED32 ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_Type_FIXED32]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum, FIXED32)

#define ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_BOOL ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_Type_BOOL]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum, BOOL)

#define ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_STRING ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_Type_STRING]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum, STRING)

#define ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_GROUP ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_Type_GROUP]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum, GROUP)

#define ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_MESSAGE ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_Type_MESSAGE]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum, MESSAGE)

#define ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_BYTES ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_Type_BYTES]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum, BYTES)

#define ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_UINT32 ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_Type_UINT32]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum, UINT32)

#define ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_ENUM ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_Type_ENUM]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum, ENUM)

#define ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_SFIXED32 ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_Type_SFIXED32]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum, SFIXED32)

#define ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_SFIXED64 ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_Type_SFIXED64]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum, SFIXED64)

#define ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_SINT32 ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_Type_SINT32]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum, SINT32)

#define ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_SINT64 ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_Type_SINT64]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum, SINT64)

J2OBJC_TYPE_LITERAL_HEADER(ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum)

typedef NS_ENUM(NSUInteger, ComGoogleProtobufDescriptors_FieldDescriptor_JavaType) {
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_INT = 0,
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_LONG = 1,
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_FLOAT = 2,
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_DOUBLE = 3,
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_BOOLEAN = 4,
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_STRING = 5,
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_BYTE_STRING = 6,
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_ENUM = 7,
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_MESSAGE = 8,
};

@interface ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum : JavaLangEnum < NSCopying > {
}

- (instancetype)initWithId:(id)defaultDefault
              withNSString:(NSString *)__name
                   withInt:(jint)__ordinal;

+ (IOSObjectArray *)values;
FOUNDATION_EXPORT IOSObjectArray *ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_values();

+ (ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum *)valueOfWithNSString:(NSString *)name;

FOUNDATION_EXPORT ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum *ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_valueOfWithNSString_(NSString *name);
- (id)copyWithZone:(NSZone *)zone;

@end

J2OBJC_STATIC_INIT(ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum)

FOUNDATION_EXPORT ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum *ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_values_[];

#define ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_INT ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_INT]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum, INT)

#define ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_LONG ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_LONG]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum, LONG)

#define ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_FLOAT ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_FLOAT]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum, FLOAT)

#define ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_DOUBLE ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_DOUBLE]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum, DOUBLE)

#define ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_BOOLEAN ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_BOOLEAN]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum, BOOLEAN)

#define ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_STRING ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_STRING]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum, STRING)

#define ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_BYTE_STRING ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_BYTE_STRING]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum, BYTE_STRING)

#define ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_ENUM ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_ENUM]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum, ENUM)

#define ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_MESSAGE ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_values_[ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_MESSAGE]
J2OBJC_ENUM_CONSTANT_GETTER(ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum, MESSAGE)

J2OBJC_TYPE_LITERAL_HEADER(ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum)

#endif // __ComGoogleProtobufFieldTypes_H__
