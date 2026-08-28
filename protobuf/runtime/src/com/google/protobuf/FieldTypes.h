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
#import "com/google/protobuf/common.h"
#import "java/lang/Boolean.h"
#import "java/lang/Double.h"
#import "java/lang/Enum.h"
#import "java/lang/Float.h"
#import "java/lang/Integer.h"
#import "java/lang/Long.h"

@class ComGoogleProtobufDescriptors_FieldDescriptor_JavaType;
@class ComGoogleProtobufDescriptorProtos_FieldDescriptorProto_Type;

typedef jint TYPE_Int;
typedef jlong TYPE_Long;
typedef jfloat TYPE_Float;
typedef jdouble TYPE_Double;
typedef bool TYPE_Bool;
typedef JavaLangEnum<ComGoogleProtobufProtocolMessageEnum> *TYPE_Enum;
typedef id TYPE_Id;

typedef TYPE_Int EXTERNAL_TYPE_Int;
typedef TYPE_Long EXTERNAL_TYPE_Long;
typedef TYPE_Float EXTERNAL_TYPE_Float;
typedef TYPE_Double EXTERNAL_TYPE_Double;
typedef TYPE_Bool EXTERNAL_TYPE_Bool;
typedef TYPE_Enum EXTERNAL_TYPE_Enum;
typedef TYPE_Id EXTERNAL_TYPE_Id;

CGP_ALWAYS_INLINE void TYPE_ASSIGN_Int(jint *assignee, jint value) { *assignee = value; }
CGP_ALWAYS_INLINE void TYPE_ASSIGN_Long(jlong *assignee, jlong value) { *assignee = value; }
CGP_ALWAYS_INLINE void TYPE_ASSIGN_Float(jfloat *assignee, jfloat value) { *assignee = value; }
CGP_ALWAYS_INLINE void TYPE_ASSIGN_Double(jdouble *assignee, jdouble value) { *assignee = value; }
CGP_ALWAYS_INLINE void TYPE_ASSIGN_Bool(bool *assignee, bool value) { *assignee = value; }
CGP_ALWAYS_INLINE void TYPE_ASSIGN_Enum(id *assignee, id value) { *assignee = value; }
CGP_ALWAYS_INLINE void TYPE_ASSIGN_Id(id *assignee, id value) {
  __unused id unused = AUTORELEASE(*assignee);
  *assignee = RETAIN_(value);
}

CGP_ALWAYS_INLINE int HASH_Int(jint value) { return value; }
CGP_ALWAYS_INLINE int HASH_Long(jlong value) {
  return (int)((uint64_t)value ^ ((uint64_t)value >> 32));
}
CGP_ALWAYS_INLINE int HASH_Float(jfloat value) {
  uint32_t bits;
  memcpy(&bits, &value, sizeof(bits));
  return bits;
}
CGP_ALWAYS_INLINE int HASH_Double(jdouble value) {
  uint64_t bits;
  memcpy(&bits, &value, sizeof(bits));
  return (int)(bits ^ (bits >> 32));
}
CGP_ALWAYS_INLINE int HASH_Bool(bool value) { return (value ? 1231 : 1237); }
CGP_ALWAYS_INLINE int HASH_Enum(id value) { return (int)[value hash]; }
CGP_ALWAYS_INLINE int HASH_Id(id value) { return (int)[value hash]; }

// Creates a switch statement over the java types grouping enums together with
// the other object types.
#define SWITCH_TYPES(type, PRIMITIVE_TYPE_MACRO, ENUM_TYPE_MACRO, ID_TYPE_MACRO) \
  switch (type) {                                                                \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_INT:         \
      PRIMITIVE_TYPE_MACRO(Int)                                                  \
      break;                                                                     \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_LONG:        \
      PRIMITIVE_TYPE_MACRO(Long)                                                 \
      break;                                                                     \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_FLOAT:       \
      PRIMITIVE_TYPE_MACRO(Float)                                                \
      break;                                                                     \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_DOUBLE:      \
      PRIMITIVE_TYPE_MACRO(Double)                                               \
      break;                                                                     \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_BOOLEAN:     \
      PRIMITIVE_TYPE_MACRO(Bool)                                                 \
      break;                                                                     \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_ENUM:        \
      ENUM_TYPE_MACRO(Enum)                                                      \
      break;                                                                     \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_STRING:      \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_BYTE_STRING: \
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_MESSAGE:     \
      ID_TYPE_MACRO(Id)                                                          \
      break;                                                                     \
  }

// Declares the given macro once for each java field type except for retainable
// types.
#define FOR_EACH_PRIMITIVE_TYPE(PRIMITIVE_TYPE_MACRO) \
  PRIMITIVE_TYPE_MACRO(Int)                           \
  PRIMITIVE_TYPE_MACRO(Long)                          \
  PRIMITIVE_TYPE_MACRO(Float)                         \
  PRIMITIVE_TYPE_MACRO(Double)                        \
  PRIMITIVE_TYPE_MACRO(Bool)

#define FOR_EACH_TYPE(TYPE_MACRO)     \
  FOR_EACH_PRIMITIVE_TYPE(TYPE_MACRO) \
  TYPE_MACRO(Enum)                    \
  TYPE_MACRO(Id)

// The remainder of this file is copied from the translation of the types
// FieldDescriptor.Type and FieldDescriptor.JavaType in Descriptor.java.

typedef NS_ENUM(NSUInteger, ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum) {
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_DOUBLE = 0,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FLOAT = 1,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_INT64 = 2,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_UINT64 = 3,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_INT32 = 4,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FIXED64 = 5,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_FIXED32 = 6,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_BOOL = 7,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_STRING = 8,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_GROUP = 9,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_MESSAGE = 10,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_BYTES = 11,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_UINT32 = 12,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_ENUM = 13,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SFIXED32 = 14,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SFIXED64 = 15,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SINT32 = 16,
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum_SINT64 = 17,
};

typedef ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum CGPFieldType;

@interface ComGoogleProtobufDescriptors_FieldDescriptor_Type : JavaLangEnum <NSCopying>

#pragma mark Public

- (ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *)getJavaType;

- (ComGoogleProtobufDescriptorProtos_FieldDescriptorProto_Type *)toProto;

+ (ComGoogleProtobufDescriptors_FieldDescriptor_Type *)
    valueOfWithComGoogleProtobufDescriptorProtos_FieldDescriptorProto_Type:
        (ComGoogleProtobufDescriptorProtos_FieldDescriptorProto_Type *)type;

#pragma mark Package-Private

+ (IOSObjectArray *)values;

+ (ComGoogleProtobufDescriptors_FieldDescriptor_Type *)valueOfWithNSString:(NSString *)name;

- (id)copyWithZone:(NSZone *)zone;

@end

J2OBJC_STATIC_INIT(ComGoogleProtobufDescriptors_FieldDescriptor_Type)

/*! INTERNAL ONLY - Use enum accessors declared below. */
FOUNDATION_EXPORT ComGoogleProtobufDescriptors_FieldDescriptor_Type
    *ComGoogleProtobufDescriptors_FieldDescriptor_Type_values_[];

inline ComGoogleProtobufDescriptors_FieldDescriptor_Type *
ComGoogleProtobufDescriptors_FieldDescriptor_Type_get_DOUBLE(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_Type, DOUBLE)

inline ComGoogleProtobufDescriptors_FieldDescriptor_Type *
ComGoogleProtobufDescriptors_FieldDescriptor_Type_get_FLOAT(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_Type, FLOAT)

inline ComGoogleProtobufDescriptors_FieldDescriptor_Type *
ComGoogleProtobufDescriptors_FieldDescriptor_Type_get_INT64(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_Type, INT64)

inline ComGoogleProtobufDescriptors_FieldDescriptor_Type *
ComGoogleProtobufDescriptors_FieldDescriptor_Type_get_UINT64(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_Type, UINT64)

inline ComGoogleProtobufDescriptors_FieldDescriptor_Type *
ComGoogleProtobufDescriptors_FieldDescriptor_Type_get_INT32(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_Type, INT32)

inline ComGoogleProtobufDescriptors_FieldDescriptor_Type *
ComGoogleProtobufDescriptors_FieldDescriptor_Type_get_FIXED64(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_Type, FIXED64)

inline ComGoogleProtobufDescriptors_FieldDescriptor_Type *
ComGoogleProtobufDescriptors_FieldDescriptor_Type_get_FIXED32(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_Type, FIXED32)

inline ComGoogleProtobufDescriptors_FieldDescriptor_Type *
ComGoogleProtobufDescriptors_FieldDescriptor_Type_get_BOOL(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_Type, BOOL)

inline ComGoogleProtobufDescriptors_FieldDescriptor_Type *
ComGoogleProtobufDescriptors_FieldDescriptor_Type_get_STRING(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_Type, STRING)

inline ComGoogleProtobufDescriptors_FieldDescriptor_Type *
ComGoogleProtobufDescriptors_FieldDescriptor_Type_get_GROUP(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_Type, GROUP)

inline ComGoogleProtobufDescriptors_FieldDescriptor_Type *
ComGoogleProtobufDescriptors_FieldDescriptor_Type_get_MESSAGE(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_Type, MESSAGE)

inline ComGoogleProtobufDescriptors_FieldDescriptor_Type *
ComGoogleProtobufDescriptors_FieldDescriptor_Type_get_BYTES(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_Type, BYTES)

inline ComGoogleProtobufDescriptors_FieldDescriptor_Type *
ComGoogleProtobufDescriptors_FieldDescriptor_Type_get_UINT32(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_Type, UINT32)

inline ComGoogleProtobufDescriptors_FieldDescriptor_Type *
ComGoogleProtobufDescriptors_FieldDescriptor_Type_get_ENUM(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_Type, ENUM)

inline ComGoogleProtobufDescriptors_FieldDescriptor_Type *
ComGoogleProtobufDescriptors_FieldDescriptor_Type_get_SFIXED32(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_Type, SFIXED32)

inline ComGoogleProtobufDescriptors_FieldDescriptor_Type *
ComGoogleProtobufDescriptors_FieldDescriptor_Type_get_SFIXED64(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_Type, SFIXED64)

inline ComGoogleProtobufDescriptors_FieldDescriptor_Type *
ComGoogleProtobufDescriptors_FieldDescriptor_Type_get_SINT32(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_Type, SINT32)

inline ComGoogleProtobufDescriptors_FieldDescriptor_Type *
ComGoogleProtobufDescriptors_FieldDescriptor_Type_get_SINT64(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_Type, SINT64)

FOUNDATION_EXPORT ComGoogleProtobufDescriptors_FieldDescriptor_Type *
ComGoogleProtobufDescriptors_FieldDescriptor_Type_valueOfWithComGoogleProtobufDescriptorProtos_FieldDescriptorProto_Type_(
    ComGoogleProtobufDescriptorProtos_FieldDescriptorProto_Type *type);

FOUNDATION_EXPORT IOSObjectArray *ComGoogleProtobufDescriptors_FieldDescriptor_Type_values(void);

FOUNDATION_EXPORT ComGoogleProtobufDescriptors_FieldDescriptor_Type *
ComGoogleProtobufDescriptors_FieldDescriptor_Type_valueOfWithNSString_(NSString *name);

FOUNDATION_EXPORT ComGoogleProtobufDescriptors_FieldDescriptor_Type *
ComGoogleProtobufDescriptors_FieldDescriptor_Type_fromOrdinal(NSUInteger ordinal);

J2OBJC_TYPE_LITERAL_HEADER(ComGoogleProtobufDescriptors_FieldDescriptor_Type)

typedef NS_ENUM(NSUInteger, ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum) {
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_INT = 0,
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_LONG = 1,
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_FLOAT = 2,
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_DOUBLE = 3,
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_BOOLEAN = 4,
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_STRING = 5,
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_BYTE_STRING = 6,
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_ENUM = 7,
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_MESSAGE = 8,
};

@interface ComGoogleProtobufDescriptors_FieldDescriptor_JavaType : JavaLangEnum <NSCopying>

#pragma mark Package-Private

+ (IOSObjectArray *)values;

+ (ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *)valueOfWithNSString:(NSString *)name;

- (id)copyWithZone:(NSZone *)zone;

@end

J2OBJC_STATIC_INIT(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType)

/*! INTERNAL ONLY - Use enum accessors declared below. */
FOUNDATION_EXPORT ComGoogleProtobufDescriptors_FieldDescriptor_JavaType
    *ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_values_[];

inline ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *
ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_get_INT(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, INT)

inline ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *
ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_get_LONG(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, LONG)

inline ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *
ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_get_FLOAT(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, FLOAT)

inline ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *
ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_get_DOUBLE(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, DOUBLE)

inline ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *
ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_get_BOOLEAN(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, BOOLEAN)

inline ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *
ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_get_STRING(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, STRING)

inline ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *
ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_get_BYTE_STRING(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, BYTE_STRING)

inline ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *
ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_get_ENUM(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, ENUM)

inline ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *
ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_get_MESSAGE(void);
J2OBJC_ENUM_CONSTANT(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, MESSAGE)

FOUNDATION_EXPORT IOSObjectArray *ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_values(
    void);

FOUNDATION_EXPORT ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *
ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_valueOfWithNSString_(NSString *name);

FOUNDATION_EXPORT ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *
ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_fromOrdinal(NSUInteger ordinal);

J2OBJC_TYPE_LITERAL_HEADER(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType)

#endif  // __ComGoogleProtobufFieldTypes_H__
