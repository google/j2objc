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
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_INT:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_LONG:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_FLOAT:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_DOUBLE:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_BOOLEAN:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_ENUM:
      return NO;
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_STRING:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_BYTE_STRING:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_MESSAGE:
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

CGPEnumDescriptor *CGPNewEnumDescriptor(
    Class enumClass, jint valuesCount,
    JavaLangEnum<ComGoogleProtobufProtocolMessageEnum> **values) {
  Ivar valuesIvar = class_getInstanceVariable(enumClass, "value_");
  CGPEnumValueDescriptor *valuesBuf[valuesCount];
  for (jint i = 0; i < valuesCount; i++) {
    valuesBuf[i] = [[CGPEnumValueDescriptor alloc] initWithValue:values[i]];
  }
  IOSObjectArray *valuesArray = [IOSObjectArray arrayWithObjects:valuesBuf count:valuesCount
      type:ComGoogleProtobufDescriptors_EnumValueDescriptor_class_()];
  return [[CGPEnumDescriptor alloc] initWithValueOffset:ivar_getOffset(valuesIvar)
                                                 values:valuesArray];
}

static inline ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum *GetTypeObj(CGPFieldType type) {
  ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_initialize();
  return ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values_[type];
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

- (ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum *)getType {
  return GetTypeObj(data_->type);
}

- (ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum *)getJavaType {
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
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_INT:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_LONG:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_FLOAT:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_DOUBLE:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_BOOLEAN:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_STRING:
      break;
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_ENUM:
      {
        Class enumClass = objc_getClass(data->className);
        CGPEnumDescriptor *enumDescriptor = [enumClass performSelector:@selector(getDescriptor)];
        CGPEnumValueDescriptor *valueDescriptor =
            IOSObjectArray_Get(enumDescriptor->values_, data->defaultValue.valueInt);
        data->defaultValue.valueId = valueDescriptor->enum_;
        descriptor->valueType_ = enumDescriptor;
        break;
      }
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_BYTE_STRING:
      if (data->defaultValue.valueId == nil) {
        data->defaultValue.valueId = ComGoogleProtobufByteString_get_EMPTY_();
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
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_MESSAGE:
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
  for (int i = 0; i < count; i++) {
    CGPEnumValueDescriptor *valueDescriptor = valuesBuf[i];
    if (valueDescriptor->number_ == value) {
      return valueDescriptor;
    }
  }
  return nil;
}

@implementation ComGoogleProtobufDescriptors_EnumDescriptor

- (instancetype)initWithValueOffset:(ptrdiff_t)valueOffset values:(IOSObjectArray *)values {
  if (self = [super init]) {
    valueOffset_ = valueOffset;
    values_ = [values retain];
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

- (instancetype)initWithValue:(JavaLangEnum<ComGoogleProtobufProtocolMessageEnum> *)value {
  if (self = [super init]) {
    enum_ = value;
    number_ = [value getNumber];
  }
  return self;
}

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

J2OBJC_INITIALIZED_DEFN(ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum)

ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum *ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values_[18];

@implementation ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum

- (instancetype)initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum:(ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum *)javaType
                                                                     withNSString:(NSString *)__name
                                                                          withInt:(jint)__ordinal {
  if (self = [super initWithNSString:__name withInt:__ordinal]) {
    ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_set_javaType_(self, javaType);
  }
  return self;
}

- (ComGoogleProtobufDescriptorProtos_FieldDescriptorProto_TypeEnum *)toProto {
  return ComGoogleProtobufDescriptorProtos_FieldDescriptorProto_TypeEnum_valueOfWithInt_([self ordinal] + 1);
}

- (ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum *)getJavaType {
  return javaType_;
}

+ (ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum *)valueOfWithComGoogleProtobufDescriptorProtos_FieldDescriptorProto_TypeEnum:(ComGoogleProtobufDescriptorProtos_FieldDescriptorProto_TypeEnum *)type {
  return ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_valueOfWithComGoogleProtobufDescriptorProtos_FieldDescriptorProto_TypeEnum_(type);
}

IOSObjectArray *ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values() {
  ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_initialize();
  return [IOSObjectArray arrayWithObjects:ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values_ count:18 type:ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_class_()];
}
+ (IOSObjectArray *)values {
  return ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values();
}

+ (ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum *)valueOfWithNSString:(NSString *)name {
  return ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_valueOfWithNSString_(name);
}

ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum *ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_valueOfWithNSString_(NSString *name) {
  ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_initialize();
  for (int i = 0; i < 18; i++) {
    ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum *e = ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values_[i];
    if ([name isEqual:[e name]]) {
      return e;
    }
  }
  @throw [[[JavaLangIllegalArgumentException alloc] initWithNSString:name] autorelease];
  return nil;
}

- (id)copyWithZone:(NSZone *)zone {
  return [self retain];
}

+ (void)initialize {
  if (self == [ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum class]) {
    ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_DOUBLE = [[ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum alloc] initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum:ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_get_DOUBLE() withNSString:@"DOUBLE" withInt:0];
    ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_FLOAT = [[ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum alloc] initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum:ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_get_FLOAT() withNSString:@"FLOAT" withInt:1];
    ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_INT64 = [[ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum alloc] initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum:ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_get_LONG() withNSString:@"INT64" withInt:2];
    ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_UINT64 = [[ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum alloc] initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum:ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_get_LONG() withNSString:@"UINT64" withInt:3];
    ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_INT32 = [[ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum alloc] initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum:ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_get_INT() withNSString:@"INT32" withInt:4];
    ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_FIXED64 = [[ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum alloc] initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum:ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_get_LONG() withNSString:@"FIXED64" withInt:5];
    ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_FIXED32 = [[ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum alloc] initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum:ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_get_INT() withNSString:@"FIXED32" withInt:6];
    ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_BOOL = [[ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum alloc] initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum:ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_get_BOOLEAN() withNSString:@"BOOL" withInt:7];
    ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_STRING = [[ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum alloc] initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum:ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_get_STRING() withNSString:@"STRING" withInt:8];
    ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_GROUP = [[ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum alloc] initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum:ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_get_MESSAGE() withNSString:@"GROUP" withInt:9];
    ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_MESSAGE = [[ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum alloc] initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum:ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_get_MESSAGE() withNSString:@"MESSAGE" withInt:10];
    ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_BYTES = [[ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum alloc] initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum:ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_get_BYTE_STRING() withNSString:@"BYTES" withInt:11];
    ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_UINT32 = [[ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum alloc] initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum:ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_get_INT() withNSString:@"UINT32" withInt:12];
    ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_ENUM = [[ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum alloc] initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum:ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_get_ENUM() withNSString:@"ENUM" withInt:13];
    ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_SFIXED32 = [[ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum alloc] initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum:ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_get_INT() withNSString:@"SFIXED32" withInt:14];
    ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_SFIXED64 = [[ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum alloc] initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum:ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_get_LONG() withNSString:@"SFIXED64" withInt:15];
    ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_SINT32 = [[ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum alloc] initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum:ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_get_INT() withNSString:@"SINT32" withInt:16];
    ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_SINT64 = [[ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum alloc] initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum:ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_get_LONG() withNSString:@"SINT64" withInt:17];
    J2OBJC_SET_INITIALIZED(ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum)
  }
}

+ (const J2ObjcClassInfo *)__metadata {
  static const J2ObjcMethodInfo methods[] = {
    { "initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum:withNSString:withInt:", "Type", NULL, 0x2, NULL },
    { "toProto", NULL, "Lcom.google.protobuf.DescriptorProtos$FieldDescriptorProto$Type;", 0x1, NULL },
    { "getJavaType", NULL, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", 0x1, NULL },
    { "valueOfWithComGoogleProtobufDescriptorProtos_FieldDescriptorProto_TypeEnum:", "valueOf", "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", 0x9, NULL },
  };
  static const J2ObjcFieldInfo fields[] = {
    { "DOUBLE", "DOUBLE", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_DOUBLE,  },
    { "FLOAT", "FLOAT", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_FLOAT,  },
    { "INT64", "INT64", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_INT64,  },
    { "UINT64", "UINT64", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_UINT64,  },
    { "INT32", "INT32", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_INT32,  },
    { "FIXED64", "FIXED64", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_FIXED64,  },
    { "FIXED32", "FIXED32", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_FIXED32,  },
    { "BOOL", "BOOL", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_BOOL,  },
    { "STRING", "STRING", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_STRING,  },
    { "GROUP", "GROUP", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_GROUP,  },
    { "MESSAGE", "MESSAGE", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_MESSAGE,  },
    { "BYTES", "BYTES", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_BYTES,  },
    { "UINT32", "UINT32", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_UINT32,  },
    { "ENUM", "ENUM", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_ENUM,  },
    { "SFIXED32", "SFIXED32", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_SFIXED32,  },
    { "SFIXED64", "SFIXED64", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_SFIXED64,  },
    { "SINT32", "SINT32", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_SINT32,  },
    { "SINT64", "SINT64", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_SINT64,  },
    { "javaType_", NULL, 0x2, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", NULL,  },
  };
  static const char *superclass_type_args[] = {"Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;"};
  static const J2ObjcClassInfo _ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum = { 1, "Type", "com.google.protobuf", "Descriptors$FieldDescriptor", 0x4019, 4, methods, 19, fields, 1, superclass_type_args};
  return &_ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum;
}

@end

ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum *ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_valueOfWithComGoogleProtobufDescriptorProtos_FieldDescriptorProto_TypeEnum_(ComGoogleProtobufDescriptorProtos_FieldDescriptorProto_TypeEnum *type) {
  ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_initialize();
  return IOSObjectArray_Get(nil_chk(ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum_values()), [((ComGoogleProtobufDescriptorProtos_FieldDescriptorProto_TypeEnum *) nil_chk(type)) getNumber] - 1);
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufDescriptors_FieldDescriptor_TypeEnum)

J2OBJC_INITIALIZED_DEFN(ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum)

ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum *ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_values_[9];

@implementation ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum

- (instancetype)initWithId:(id)defaultDefault
              withNSString:(NSString *)__name
                   withInt:(jint)__ordinal {
  if (self = [super initWithNSString:__name withInt:__ordinal]) {
    ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_set_defaultDefault_(self, defaultDefault);
  }
  return self;
}

IOSObjectArray *ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_values() {
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_initialize();
  return [IOSObjectArray arrayWithObjects:ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_values_ count:9 type:ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_class_()];
}
+ (IOSObjectArray *)values {
  return ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_values();
}

+ (ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum *)valueOfWithNSString:(NSString *)name {
  return ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_valueOfWithNSString_(name);
}

ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum *ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_valueOfWithNSString_(NSString *name) {
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_initialize();
  for (int i = 0; i < 9; i++) {
    ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum *e = ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_values_[i];
    if ([name isEqual:[e name]]) {
      return e;
    }
  }
  @throw [[[JavaLangIllegalArgumentException alloc] initWithNSString:name] autorelease];
  return nil;
}

- (id)copyWithZone:(NSZone *)zone {
  return [self retain];
}

+ (void)initialize {
  if (self == [ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum class]) {
    ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_INT = [[ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum alloc] initWithId:JavaLangInteger_valueOfWithInt_(0) withNSString:@"INT" withInt:0];
    ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_LONG = [[ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum alloc] initWithId:JavaLangLong_valueOfWithLong_(0LL) withNSString:@"LONG" withInt:1];
    ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_FLOAT = [[ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum alloc] initWithId:JavaLangFloat_valueOfWithFloat_(0.0f) withNSString:@"FLOAT" withInt:2];
    ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_DOUBLE = [[ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum alloc] initWithId:JavaLangDouble_valueOfWithDouble_(0.0) withNSString:@"DOUBLE" withInt:3];
    ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_BOOLEAN = [[ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum alloc] initWithId:JavaLangBoolean_valueOfWithBoolean_(NO) withNSString:@"BOOLEAN" withInt:4];
    ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_STRING = [[ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum alloc] initWithId:@"" withNSString:@"STRING" withInt:5];
    ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_BYTE_STRING = [[ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum alloc] initWithId:ComGoogleProtobufByteString_get_EMPTY_() withNSString:@"BYTE_STRING" withInt:6];
    ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_ENUM = [[ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum alloc] initWithId:nil withNSString:@"ENUM" withInt:7];
    ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_MESSAGE = [[ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum alloc] initWithId:nil withNSString:@"MESSAGE" withInt:8];
    J2OBJC_SET_INITIALIZED(ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum)
  }
}

+ (const J2ObjcClassInfo *)__metadata {
  static const J2ObjcMethodInfo methods[] = {
    { "initWithId:withNSString:withInt:", "JavaType", NULL, 0x2, NULL },
  };
  static const J2ObjcFieldInfo fields[] = {
    { "INT", "INT", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_INT,  },
    { "LONG", "LONG", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_LONG,  },
    { "FLOAT", "FLOAT", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_FLOAT,  },
    { "DOUBLE", "DOUBLE", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_DOUBLE,  },
    { "BOOLEAN", "BOOLEAN", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_BOOLEAN,  },
    { "STRING", "STRING", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_STRING,  },
    { "BYTE_STRING", "BYTE_STRING", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_BYTE_STRING,  },
    { "ENUM", "ENUM", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_ENUM,  },
    { "MESSAGE", "MESSAGE", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum_MESSAGE,  },
    { "defaultDefault_", NULL, 0x12, "Ljava.lang.Object;", NULL,  },
  };
  static const char *superclass_type_args[] = {"Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;"};
  static const J2ObjcClassInfo _ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum = { 1, "JavaType", "com.google.protobuf", "Descriptors$FieldDescriptor", 0x4019, 1, methods, 10, fields, 1, superclass_type_args};
  return &_ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufDescriptors_FieldDescriptor_JavaTypeEnum)
