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

//  Hand written counterpart of com.google.protobuf.Descriptors.

#import "com/google/protobuf/Descriptors_PackagePrivate.h"

#import "IOSClass.h"
#import "J2ObjC_source.h"
#import "com/google/protobuf/ByteString.h"
#import "com/google/protobuf/DescriptorProtos.h"
#import "com/google/protobuf/GeneratedMessage_PackagePrivate.h"
#import "com/google/protobuf/ProtocolMessageEnum.h"
#import "com/google/protobuf/WireFormat.h"
#import "java/lang/Boolean.h"
#import "java/lang/Double.h"
#import "java/lang/Float.h"
#import "java/lang/IllegalArgumentException.h"
#import "java/lang/Integer.h"
#import "java/lang/Long.h"
#import "java/lang/UnsupportedOperationException.h"
#import "java/util/Arrays.h"
#import "java/util/Collections.h"

// Defines the field in the CGPValue union type to use for each field type.
#define VALUE_FIELD_Int valueInt
#define VALUE_FIELD_Long valueLong
#define VALUE_FIELD_Float valueFloat
#define VALUE_FIELD_Double valueDouble
#define VALUE_FIELD_Bool valueBool
#define VALUE_FIELD_Enum valueId
#define VALUE_FIELD_Retainable valueId

BOOL CGPIsRetainedType(CGPFieldJavaType type) {
  switch (type) {
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_INT:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_LONG:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_FLOAT:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_DOUBLE:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_BOOLEAN:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_ENUM:
      return NO;
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_STRING:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_BYTE_STRING:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_MESSAGE:
      return YES;
  }
}

size_t CGPGetTypeSize(CGPFieldJavaType type) {
#define GET_TYPE_SIZE_CASE(NAME) \
  return sizeof(TYPE_##NAME);

  SWITCH_TYPES_NO_ENUM(type, GET_TYPE_SIZE_CASE)

#undef GET_TYPE_SIZE_CASE
}

void CGPInitDescriptor(
    CGPDescriptor **pDescriptor, Class messageClass, Class builderClass, CGPMessageFlags flags,
    size_t storageSize, jint fieldCount, CGPFieldData *fieldData) {
  CGPFieldDescriptor *fieldsBuf[fieldCount];
  for (jint i = 0; i < fieldCount; i++) {
    fieldsBuf[i] = [[CGPFieldDescriptor alloc] initWithData:&fieldData[i]];
  }
  IOSObjectArray *fields = [IOSObjectArray arrayWithObjects:fieldsBuf count:fieldCount
      type:ComGoogleProtobufDescriptors_FieldDescriptor_class_()];
  CGPDescriptor *descriptor = [[CGPDescriptor alloc]
      initWithMessageClass:messageClass
              builderClass:builderClass
                     flags:flags
               storageSize:storageSize
                    fields:fields];
  *pDescriptor = descriptor;
  for (CGPFieldDescriptor *field in descriptor->fields_) {
    CGPFieldFixDefaultValue(field);
  }
}

CGPEnumDescriptor *CGPInitializeEnumType(
    Class enumClass, jint valuesCount, JavaLangEnum<ComGoogleProtobufProtocolMessageEnum> **values,
    NSString **names, jint *intValues) {
  Ivar valueIvar = class_getInstanceVariable(enumClass, "value_");
  ptrdiff_t valueOffset = ivar_getOffset(valueIvar);

  // Put all enum instances and descriptors on the same allocation.
  size_t enumSize = class_getInstanceSize(enumClass);
  size_t enumDescSize = class_getInstanceSize([CGPEnumDescriptor class]);
  size_t enumValueDescSize = class_getInstanceSize([CGPEnumValueDescriptor class]);
  size_t allocSize = enumSize * valuesCount + enumDescSize + enumValueDescSize * valuesCount;
  uintptr_t enumPtr = (uintptr_t)calloc(allocSize, 1);
  uintptr_t enumDescPtr = enumPtr + enumSize * valuesCount;
  uintptr_t enumValueDescPtr = enumDescPtr + enumDescSize;

  IOSObjectArray *valuesArray = [IOSObjectArray newArrayWithLength:valuesCount
      type:ComGoogleProtobufDescriptors_EnumValueDescriptor_class_()];
  id *valueDescBuf = valuesArray->buffer_;

  for (jint i = 0; i < valuesCount; i++) {
    // Construct the Java enum instance.
    JavaLangEnum<ComGoogleProtobufProtocolMessageEnum> *newEnum =
        objc_constructInstance(enumClass, (void *)enumPtr);
    [newEnum initWithNSString:names[i] withInt:i];
    *(int *)(enumPtr + valueOffset) = intValues[i];
    values[i] = newEnum;
    enumPtr += enumSize;

    // Construct the enum value descriptor.
    CGPEnumValueDescriptor *valueDesc =
        objc_constructInstance([CGPEnumValueDescriptor class], (void *)enumValueDescPtr);
    valueDesc->enum_ = newEnum;
    valueDesc->number_ = intValues[i];
    valueDescBuf[i] = valueDesc;
    enumValueDescPtr += enumValueDescSize;
  }

  // Construct the enum descriptor.
  CGPEnumDescriptor *enumDesc =
      objc_constructInstance([CGPEnumDescriptor class], (void *)enumDescPtr);
  return [enumDesc initWithValueOffset:valueOffset retainedValues:valuesArray];
}

static inline ComGoogleProtobufDescriptors_FieldDescriptor_Type *GetTypeObj(CGPFieldType type) {
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_initialize();
  return ComGoogleProtobufDescriptors_FieldDescriptor_Type_values_[type];
}

@implementation ComGoogleProtobufDescriptors_Descriptor

- (instancetype)initWithMessageClass:(Class)messageClass
                        builderClass:(Class)builderClass
                               flags:(CGPMessageFlags)flags
                         storageSize:(size_t)storageSize
                              fields:(IOSObjectArray *)fields {
  if (self = [self init]) {
    messageClass_ = messageClass;
    builderClass_ = builderClass;
    flags_ = flags;
    storageSize_ = storageSize;
    fields_ = [fields retain];
    defaultInstance_ = CGPNewMessage(self);
  }
  return self;
}

- (id<JavaUtilList>)getFields {
  return [JavaUtilArrays asListWithNSObjectArray:fields_];
}

- (CGPFieldDescriptor *)findFieldByNumberWithInt:(int)fieldId {
  NSUInteger count = fields_->size_;
  CGPFieldDescriptor **fieldsBuf = fields_->buffer_;
  for (NSUInteger i = 0; i < count; i++) {
    CGPFieldDescriptor *field = fieldsBuf[i];
    if (field->data_->number == fieldId) {
      return field;
    }
  }
  return nil;
}

J2OBJC_ETERNAL_SINGLETON

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufDescriptors_Descriptor)

@implementation ComGoogleProtobufDescriptors_FieldDescriptor

static uint32_t TagFromData(CGPFieldData *data) {
  BOOL isPacked = data->flags & CGPFieldFlagPacked;
  return CGPWireFormatMakeTag(data->number, CGPWireFormatForType(data->type, isPacked));
}

static ComGoogleProtobufDescriptorProtos_FieldOptions *InitFieldOptions(const char *data) {
  if (data == NULL) {
    return nil;
  }
  uint32_t optionsLength = *((uint32_t *)data);
  // The length is stored in network byte order.
  optionsLength = ntohl(optionsLength);
  if (optionsLength == 0) {
    return nil;
  }
  data += sizeof(optionsLength);
  CGPDescriptor *descriptor = [ComGoogleProtobufDescriptorProtos_FieldOptions getDescriptor];
  id msg = CGPNewMessage(descriptor);
  CGPMergeFromRawData(msg, descriptor, data, optionsLength);
  return msg;
}

- (instancetype)initWithData:(CGPFieldData *)data {
  if (self = [self init]) {
    data_ = data;
    tag_ = TagFromData(data);
    javaType_ = [GetTypeObj(data->type)->javaType_ ordinal];
    fieldOptions_ = InitFieldOptions(data->optionsData);
  }
  return self;
}

- (ComGoogleProtobufDescriptors_FieldDescriptor_Type *)getType {
  return GetTypeObj(data_->type);
}

- (ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *)getJavaType {
  return GetTypeObj(data_->type)->javaType_;
}

- (int)getNumber {
  return data_->number;
}

- (NSString *)getName {
  NSString *result = [NSString stringWithUTF8String:data_->name];
  if (CGPTypeIsGroup(CGPFieldGetType(self))) {
    return [result lowercaseString];
  } else {
    return result;
  }
}

- (BOOL)isRequired {
  return CGPFieldIsRequired(self);
}

- (BOOL)isRepeated {
  return CGPFieldIsRepeated(self);
}

- (BOOL)isExtension {
  return data_->flags & CGPFieldFlagExtension;
}

- (CGPDescriptor *)getMessageType {
  if (!CGPJavaTypeIsMessage(CGPFieldGetJavaType(self))) {
    @throw [[[JavaLangUnsupportedOperationException alloc] initWithNSString:
        @"This field is not of message type."] autorelease];
  }
  return valueType_;
}

- (CGPEnumDescriptor *)getEnumType {
  if (!CGPJavaTypeIsEnum(CGPFieldGetJavaType(self))) {
    @throw [[[JavaLangUnsupportedOperationException alloc] initWithNSString:
        @"This field is not of enum type."] autorelease];
  }
  return valueType_;
}

- (id)getDefaultValue {
  return CGPFieldGetDefaultValue(self);
}

- (ComGoogleProtobufDescriptorProtos_FieldOptions *)getOptions {
  if (fieldOptions_ != nil) {
    return fieldOptions_;
  } else {
    return [ComGoogleProtobufDescriptorProtos_FieldOptions getDefaultInstance];
  }
}

J2OBJC_ETERNAL_SINGLETON

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufDescriptors_FieldDescriptor)

id CGPFieldGetDefaultValue(CGPFieldDescriptor *field) {
  if (CGPFieldIsRepeated(field)) {
    return [JavaUtilCollections emptyList];
  }

#define GET_DEFAULT_VALUE_CASE(NAME) \
  return CGPToReflectionType##NAME(field->data_->defaultValue.VALUE_FIELD_##NAME, field);

  SWITCH_TYPES_WITH_ENUM(CGPFieldGetJavaType(field), GET_DEFAULT_VALUE_CASE)

#undef GET_DEFAULT_VALUE_CASE
}

// Default values for enums and message types can't be assigned in static data.
void CGPFieldFixDefaultValue(CGPFieldDescriptor *descriptor) {
  CGPFieldData *data = descriptor->data_;
  switch (descriptor->javaType_) {
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_INT:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_LONG:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_FLOAT:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_DOUBLE:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_BOOLEAN:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_STRING:
      break;
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_ENUM:
      {
        Class enumClass = objc_getClass(data->className);
        CGPEnumDescriptor *enumDescriptor = [enumClass performSelector:@selector(getDescriptor)];
        CGPEnumValueDescriptor *valueDescriptor =
            IOSObjectArray_Get(enumDescriptor->values_, data->defaultValue.valueInt);
        data->defaultValue.valueId = valueDescriptor->enum_;
        descriptor->valueType_ = enumDescriptor;
        break;
      }
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_BYTE_STRING:
      if (data->defaultValue.valueId == nil) {
        data->defaultValue.valueId = ComGoogleProtobufByteString_get_EMPTY();
      } else {
        // Default byte string data is written to static data as a length
        // prefixed c-string.
        const uint8_t *rawBytes = (const uint8_t *)data->defaultValue.valueId;
        uint32_t length = *((uint32_t *)rawBytes);
        // The length is stored in network byte order.
        length = ntohl(length);
        rawBytes += sizeof(length);
        CGPByteString *byteString = CGPNewByteString(length);
        memcpy(byteString->buffer_, rawBytes, length);
        data->defaultValue.valueId = byteString;
      }
      break;
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_MESSAGE:
      {
        Class msgClass = objc_getClass(data->className);
        CGPDescriptor *msgDescriptor = [msgClass performSelector:@selector(getDescriptor)];
        data->defaultValue.valueId = msgDescriptor->defaultInstance_;
        descriptor->valueType_ = msgDescriptor;
        break;
      }
  }
}

CGPDescriptor *CGPFieldGetContainingType(CGPFieldDescriptor *field) {
  Class msgClass = objc_getClass(field->data_->containingType);
  NSCAssert(msgClass != nil, @"Containing message type not found.");
  return [msgClass performSelector:@selector(getDescriptor)];
}

CGPEnumValueDescriptor *CGPEnumValueDescriptorFromInt(CGPEnumDescriptor *enumType, int value) {
  NSUInteger count = enumType->values_->size_;
  CGPEnumValueDescriptor **valuesBuf = enumType->values_->buffer_;
  for (NSUInteger i = 0; i < count; i++) {
    CGPEnumValueDescriptor *valueDescriptor = valuesBuf[i];
    if (valueDescriptor->number_ == value) {
      return valueDescriptor;
    }
  }
  return nil;
}

@implementation ComGoogleProtobufDescriptors_EnumDescriptor

- (instancetype)initWithValueOffset:(ptrdiff_t)valueOffset retainedValues:(IOSObjectArray *)values {
  if (self = [super init]) {
    valueOffset_ = valueOffset;
    values_ = values; // Already retained.
  }
  return self;
}

- (CGPEnumValueDescriptor *)findValueByNumberWithInt:(int)number {
  return CGPEnumValueDescriptorFromInt(self, number);
}

J2OBJC_ETERNAL_SINGLETON

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufDescriptors_EnumDescriptor)

@implementation ComGoogleProtobufDescriptors_EnumValueDescriptor

- (int)getNumber {
  return number_;
}

- (NSString *)getName {
  return [enum_ name];
}

J2OBJC_ETERNAL_SINGLETON

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufDescriptors_EnumValueDescriptor)

// The remainder of this file is copied from the translation of the types
// FieldDescriptor.Type and FieldDescriptor.JavaType in Descriptor.java.

J2OBJC_INITIALIZED_DEFN(ComGoogleProtobufDescriptors_FieldDescriptor_Type)

ComGoogleProtobufDescriptors_FieldDescriptor_Type *ComGoogleProtobufDescriptors_FieldDescriptor_Type_values_[18];

@implementation ComGoogleProtobufDescriptors_FieldDescriptor_Type

- (instancetype)initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType:(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *)javaType
                                                                 withNSString:(NSString *)__name
                                                                      withInt:(jint)__ordinal {
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(self, javaType, __name, __ordinal);
  return self;
}

- (ComGoogleProtobufDescriptorProtos_FieldDescriptorProto_Type *)toProto {
  return ComGoogleProtobufDescriptorProtos_FieldDescriptorProto_Type_valueOfWithInt_([self ordinal] + 1);
}

- (ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *)getJavaType {
  return javaType_;
}

+ (ComGoogleProtobufDescriptors_FieldDescriptor_Type *)valueOfWithComGoogleProtobufDescriptorProtos_FieldDescriptorProto_Type:(ComGoogleProtobufDescriptorProtos_FieldDescriptorProto_Type *)type {
  return ComGoogleProtobufDescriptors_FieldDescriptor_Type_valueOfWithComGoogleProtobufDescriptorProtos_FieldDescriptorProto_Type_(type);
}

+ (IOSObjectArray *)values {
  return ComGoogleProtobufDescriptors_FieldDescriptor_Type_values();
}

+ (ComGoogleProtobufDescriptors_FieldDescriptor_Type *)valueOfWithNSString:(NSString *)name {
  return ComGoogleProtobufDescriptors_FieldDescriptor_Type_valueOfWithNSString_(name);
}

- (id)copyWithZone:(NSZone *)zone {
  return self;
}

+ (void)initialize {
  if (self == [ComGoogleProtobufDescriptors_FieldDescriptor_Type class]) {
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, DOUBLE) = new_ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, DOUBLE), @"DOUBLE", 0);
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, FLOAT) = new_ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, FLOAT), @"FLOAT", 1);
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, INT64) = new_ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, LONG), @"INT64", 2);
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, UINT64) = new_ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, LONG), @"UINT64", 3);
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, INT32) = new_ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, INT), @"INT32", 4);
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, FIXED64) = new_ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, LONG), @"FIXED64", 5);
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, FIXED32) = new_ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, INT), @"FIXED32", 6);
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, BOOL) = new_ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, BOOLEAN), @"BOOL", 7);
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, STRING) = new_ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, STRING), @"STRING", 8);
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, GROUP) = new_ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, MESSAGE), @"GROUP", 9);
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, MESSAGE) = new_ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, MESSAGE), @"MESSAGE", 10);
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, BYTES) = new_ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, BYTE_STRING), @"BYTES", 11);
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, UINT32) = new_ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, INT), @"UINT32", 12);
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, ENUM) = new_ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, ENUM), @"ENUM", 13);
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, SFIXED32) = new_ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, INT), @"SFIXED32", 14);
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, SFIXED64) = new_ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, LONG), @"SFIXED64", 15);
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, SINT32) = new_ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, INT), @"SINT32", 16);
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, SINT64) = new_ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, LONG), @"SINT64", 17);
    J2OBJC_SET_INITIALIZED(ComGoogleProtobufDescriptors_FieldDescriptor_Type)
  }
}

+ (const J2ObjcClassInfo *)__metadata {
  static const J2ObjcMethodInfo methods[] = {
    { "toProto", NULL, "Lcom.google.protobuf.DescriptorProtos$FieldDescriptorProto$Type;", 0x1, NULL, NULL },
    { "getJavaType", NULL, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", 0x1, NULL, NULL },
    { "valueOfWithComGoogleProtobufDescriptorProtos_FieldDescriptorProto_Type:", "valueOf", "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", 0x9, NULL, NULL },
  };
  static const J2ObjcFieldInfo fields[] = {
    { "DOUBLE", "DOUBLE", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, DOUBLE), NULL, .constantValue.asLong = 0 },
    { "FLOAT", "FLOAT", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, FLOAT), NULL, .constantValue.asLong = 0 },
    { "INT64", "INT64", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, INT64), NULL, .constantValue.asLong = 0 },
    { "UINT64", "UINT64", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, UINT64), NULL, .constantValue.asLong = 0 },
    { "INT32", "INT32", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, INT32), NULL, .constantValue.asLong = 0 },
    { "FIXED64", "FIXED64", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, FIXED64), NULL, .constantValue.asLong = 0 },
    { "FIXED32", "FIXED32", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, FIXED32), NULL, .constantValue.asLong = 0 },
    { "BOOL", "BOOL", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, BOOL), NULL, .constantValue.asLong = 0 },
    { "STRING", "STRING", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, STRING), NULL, .constantValue.asLong = 0 },
    { "GROUP", "GROUP", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, GROUP), NULL, .constantValue.asLong = 0 },
    { "MESSAGE", "MESSAGE", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, MESSAGE), NULL, .constantValue.asLong = 0 },
    { "BYTES", "BYTES", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, BYTES), NULL, .constantValue.asLong = 0 },
    { "UINT32", "UINT32", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, UINT32), NULL, .constantValue.asLong = 0 },
    { "ENUM", "ENUM", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, ENUM), NULL, .constantValue.asLong = 0 },
    { "SFIXED32", "SFIXED32", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, SFIXED32), NULL, .constantValue.asLong = 0 },
    { "SFIXED64", "SFIXED64", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, SFIXED64), NULL, .constantValue.asLong = 0 },
    { "SINT32", "SINT32", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, SINT32), NULL, .constantValue.asLong = 0 },
    { "SINT64", "SINT64", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, SINT64), NULL, .constantValue.asLong = 0 },
    { "javaType_", NULL, 0x2, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", NULL, NULL, .constantValue.asLong = 0 },
  };
  static const char *superclass_type_args[] = {"Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;"};
  static const J2ObjcClassInfo _ComGoogleProtobufDescriptors_FieldDescriptor_Type = { 2, "Type", "com.google.protobuf", "Descriptors$FieldDescriptor", 0x4019, 3, methods, 19, fields, 1, superclass_type_args, 0, NULL, NULL, "Ljava/lang/Enum<Lcom/google/protobuf/Descriptors$FieldDescriptor$Type;>;" };
  return &_ComGoogleProtobufDescriptors_FieldDescriptor_Type;
}

@end

void ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(ComGoogleProtobufDescriptors_FieldDescriptor_Type *self, ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *javaType, NSString *__name, jint __ordinal) {
  JavaLangEnum_initWithNSString_withInt_(self, __name, __ordinal);
  JreStrongAssign(&self->javaType_, javaType);
}

ComGoogleProtobufDescriptors_FieldDescriptor_Type *new_ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *javaType, NSString *__name, jint __ordinal) {
  ComGoogleProtobufDescriptors_FieldDescriptor_Type *self = [ComGoogleProtobufDescriptors_FieldDescriptor_Type alloc];
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(self, javaType, __name, __ordinal);
  return self;
}

ComGoogleProtobufDescriptors_FieldDescriptor_Type *ComGoogleProtobufDescriptors_FieldDescriptor_Type_valueOfWithComGoogleProtobufDescriptorProtos_FieldDescriptorProto_Type_(ComGoogleProtobufDescriptorProtos_FieldDescriptorProto_Type *type) {
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_initialize();
  return IOSObjectArray_Get(nil_chk(ComGoogleProtobufDescriptors_FieldDescriptor_Type_values()), [((ComGoogleProtobufDescriptorProtos_FieldDescriptorProto_Type *) nil_chk(type)) getNumber] - 1);
}

IOSObjectArray *ComGoogleProtobufDescriptors_FieldDescriptor_Type_values() {
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_initialize();
  return [IOSObjectArray arrayWithObjects:ComGoogleProtobufDescriptors_FieldDescriptor_Type_values_ count:18 type:ComGoogleProtobufDescriptors_FieldDescriptor_Type_class_()];
}

ComGoogleProtobufDescriptors_FieldDescriptor_Type *ComGoogleProtobufDescriptors_FieldDescriptor_Type_valueOfWithNSString_(NSString *name) {
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_initialize();
  for (int i = 0; i < 18; i++) {
    ComGoogleProtobufDescriptors_FieldDescriptor_Type *e = ComGoogleProtobufDescriptors_FieldDescriptor_Type_values_[i];
    if ([name isEqual:[e name]]) {
      return e;
    }
  }
  @throw [[[JavaLangIllegalArgumentException alloc] initWithNSString:name] autorelease];
  return nil;
}

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wtautological-constant-out-of-range-compare"
ComGoogleProtobufDescriptors_FieldDescriptor_Type *ComGoogleProtobufDescriptors_FieldDescriptor_Type_fromNative(ComGoogleProtobufDescriptors_FieldDescriptor_Type_Enum nativeValue) {
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_initialize();
  if (nativeValue >= 18) {
    return nil;
  }
  return ComGoogleProtobufDescriptors_FieldDescriptor_Type_values_[nativeValue];
}
#pragma clang diagnostic pop

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufDescriptors_FieldDescriptor_Type)

J2OBJC_INITIALIZED_DEFN(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType)

ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_values_[9];

@implementation ComGoogleProtobufDescriptors_FieldDescriptor_JavaType

- (instancetype)initWithId:(id)defaultDefault
              withNSString:(NSString *)__name
                   withInt:(jint)__ordinal {
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initWithId_withNSString_withInt_(self, defaultDefault, __name, __ordinal);
  return self;
}

+ (IOSObjectArray *)values {
  return ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_values();
}

+ (ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *)valueOfWithNSString:(NSString *)name {
  return ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_valueOfWithNSString_(name);
}

- (id)copyWithZone:(NSZone *)zone {
  return self;
}

+ (void)initialize {
  if (self == [ComGoogleProtobufDescriptors_FieldDescriptor_JavaType class]) {
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, INT) = new_ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initWithId_withNSString_withInt_(JavaLangInteger_valueOfWithInt_(0), @"INT", 0);
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, LONG) = new_ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initWithId_withNSString_withInt_(JavaLangLong_valueOfWithLong_(0LL), @"LONG", 1);
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, FLOAT) = new_ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initWithId_withNSString_withInt_(JavaLangFloat_valueOfWithFloat_(0.0f), @"FLOAT", 2);
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, DOUBLE) = new_ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initWithId_withNSString_withInt_(JavaLangDouble_valueOfWithDouble_(0.0), @"DOUBLE", 3);
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, BOOLEAN) = new_ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initWithId_withNSString_withInt_(JavaLangBoolean_valueOfWithBoolean_(false), @"BOOLEAN", 4);
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, STRING) = new_ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initWithId_withNSString_withInt_(@"", @"STRING", 5);
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, BYTE_STRING) = new_ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initWithId_withNSString_withInt_(JreLoadStatic(ComGoogleProtobufByteString, EMPTY), @"BYTE_STRING", 6);
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, ENUM) = new_ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initWithId_withNSString_withInt_(nil, @"ENUM", 7);
    JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, MESSAGE) = new_ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initWithId_withNSString_withInt_(nil, @"MESSAGE", 8);
    J2OBJC_SET_INITIALIZED(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType)
  }
}

+ (const J2ObjcClassInfo *)__metadata {
  static const J2ObjcFieldInfo fields[] = {
    { "INT", "INT", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, INT), NULL, .constantValue.asLong = 0 },
    { "LONG", "LONG", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, LONG), NULL, .constantValue.asLong = 0 },
    { "FLOAT", "FLOAT", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, FLOAT), NULL, .constantValue.asLong = 0 },
    { "DOUBLE", "DOUBLE", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, DOUBLE), NULL, .constantValue.asLong = 0 },
    { "BOOLEAN", "BOOLEAN", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, BOOLEAN), NULL, .constantValue.asLong = 0 },
    { "STRING", "STRING", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, STRING), NULL, .constantValue.asLong = 0 },
    { "BYTE_STRING", "BYTE_STRING", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, BYTE_STRING), NULL, .constantValue.asLong = 0 },
    { "ENUM", "ENUM", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, ENUM), NULL, .constantValue.asLong = 0 },
    { "MESSAGE", "MESSAGE", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, MESSAGE), NULL, .constantValue.asLong = 0 },
    { "defaultDefault_", NULL, 0x12, "Ljava.lang.Object;", NULL, NULL, .constantValue.asLong = 0 },
  };
  static const char *superclass_type_args[] = {"Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;"};
  static const J2ObjcClassInfo _ComGoogleProtobufDescriptors_FieldDescriptor_JavaType = { 2, "JavaType", "com.google.protobuf", "Descriptors$FieldDescriptor", 0x4019, 0, NULL, 10, fields, 1, superclass_type_args, 0, NULL, NULL, "Ljava/lang/Enum<Lcom/google/protobuf/Descriptors$FieldDescriptor$JavaType;>;" };
  return &_ComGoogleProtobufDescriptors_FieldDescriptor_JavaType;
}

@end

void ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initWithId_withNSString_withInt_(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *self, id defaultDefault, NSString *__name, jint __ordinal) {
  JavaLangEnum_initWithNSString_withInt_(self, __name, __ordinal);
  JreStrongAssign(&self->defaultDefault_, defaultDefault);
}

ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *new_ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initWithId_withNSString_withInt_(id defaultDefault, NSString *__name, jint __ordinal) {
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *self = [ComGoogleProtobufDescriptors_FieldDescriptor_JavaType alloc];
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initWithId_withNSString_withInt_(self, defaultDefault, __name, __ordinal);
  return self;
}

IOSObjectArray *ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_values() {
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initialize();
  return [IOSObjectArray arrayWithObjects:ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_values_ count:9 type:ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_class_()];
}

ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_valueOfWithNSString_(NSString *name) {
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initialize();
  for (int i = 0; i < 9; i++) {
    ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *e = ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_values_[i];
    if ([name isEqual:[e name]]) {
      return e;
    }
  }
  @throw [[[JavaLangIllegalArgumentException alloc] initWithNSString:name] autorelease];
  return nil;
}

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wtautological-constant-out-of-range-compare"
ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_fromNative(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum nativeValue) {
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initialize();
  if (nativeValue >= 9) {
    return nil;
  }
  return ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_values_[nativeValue];
}
#pragma clang diagnostic pop

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType)
