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
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_INT:
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_LONG:
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_FLOAT:
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_DOUBLE:
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_BOOLEAN:
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_ENUM:
      return NO;
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_STRING:
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_BYTE_STRING:
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_MESSAGE:
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
      type:ComGoogleProtobufDescriptors$FieldDescriptor_class_()];
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
      type:ComGoogleProtobufDescriptors$EnumValueDescriptor_class_()];
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

static inline ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum *GetTypeObj(CGPFieldType type) {
  ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_initialize();
  return ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values_[type];
}

@implementation ComGoogleProtobufDescriptors$Descriptor

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

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufDescriptors$Descriptor)

@implementation ComGoogleProtobufDescriptors$FieldDescriptor

static uint32_t TagFromData(CGPFieldData *data) {
  BOOL isPacked = data->flags & CGPFieldFlagPacked;
  return CGPWireFormatMakeTag(data->number, CGPWireFormatForType(data->type, isPacked));
}

static ComGoogleProtobufDescriptorProtos$FieldOptions *InitFieldOptions(const char *data) {
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
  CGPDescriptor *descriptor = [ComGoogleProtobufDescriptorProtos$FieldOptions getDescriptor];
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

- (ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum *)getType {
  return GetTypeObj(data_->type);
}

- (ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum *)getJavaType {
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

- (ComGoogleProtobufDescriptorProtos$FieldOptions *)getOptions {
  if (fieldOptions_ != nil) {
    return fieldOptions_;
  } else {
    return [ComGoogleProtobufDescriptorProtos$FieldOptions getDefaultInstance];
  }
}

J2OBJC_ETERNAL_SINGLETON

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufDescriptors$FieldDescriptor)

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
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_INT:
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_LONG:
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_FLOAT:
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_DOUBLE:
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_BOOLEAN:
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_STRING:
      break;
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_ENUM:
      {
        Class enumClass = objc_getClass(data->className);
        CGPEnumDescriptor *enumDescriptor = [enumClass performSelector:@selector(getDescriptor)];
        CGPEnumValueDescriptor *valueDescriptor =
            IOSObjectArray_Get(enumDescriptor->values_, data->defaultValue.valueInt);
        data->defaultValue.valueId = valueDescriptor->enum_;
        descriptor->valueType_ = enumDescriptor;
        break;
      }
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_BYTE_STRING:
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
    case ComGoogleProtobufDescriptors$FieldDescriptor$JavaType_Enum_MESSAGE:
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

@implementation ComGoogleProtobufDescriptors$EnumDescriptor

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

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufDescriptors$EnumDescriptor)

@implementation ComGoogleProtobufDescriptors$EnumValueDescriptor

- (int)getNumber {
  return number_;
}

- (NSString *)getName {
  return [enum_ name];
}

J2OBJC_ETERNAL_SINGLETON

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufDescriptors$EnumValueDescriptor)

// The remainder of this file is copied from the translation of the types
// FieldDescriptor.Type and FieldDescriptor.JavaType in Descriptor.java.

J2OBJC_INITIALIZED_DEFN(ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum)

ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum *ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values_[18];

@implementation ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum

- (instancetype)initWithComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum:(ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum *)javaType
                                                                     withNSString:(NSString *)__name
                                                                          withInt:(jint)__ordinal {
  if (self = [super initWithNSString:__name withInt:__ordinal]) {
    ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_set_javaType_(self, javaType);
  }
  return self;
}

- (ComGoogleProtobufDescriptorProtos$FieldDescriptorProto$TypeEnum *)toProto {
  return ComGoogleProtobufDescriptorProtos$FieldDescriptorProto$TypeEnum_valueOfWithInt_([self ordinal] + 1);
}

- (ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum *)getJavaType {
  return javaType_;
}

+ (ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum *)valueOfWithComGoogleProtobufDescriptorProtos$FieldDescriptorProto$TypeEnum:(ComGoogleProtobufDescriptorProtos$FieldDescriptorProto$TypeEnum *)type {
  return ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_valueOfWithComGoogleProtobufDescriptorProtos$FieldDescriptorProto$TypeEnum_(type);
}

IOSObjectArray *ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values() {
  ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_initialize();
  return [IOSObjectArray arrayWithObjects:ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values_ count:18 type:ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_class_()];
}

+ (IOSObjectArray *)values {
  return ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values();
}

+ (ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum *)valueOfWithNSString:(NSString *)name {
  return ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_valueOfWithNSString_(name);
}

ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum *ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_valueOfWithNSString_(NSString *name) {
  ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_initialize();
  for (int i = 0; i < 18; i++) {
    ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum *e = ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values_[i];
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
  if (self == [ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum class]) {
    ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_DOUBLE = [[ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum alloc] initWithComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum:ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_get_DOUBLE() withNSString:@"DOUBLE" withInt:0];
    ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_FLOAT = [[ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum alloc] initWithComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum:ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_get_FLOAT() withNSString:@"FLOAT" withInt:1];
    ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_INT64 = [[ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum alloc] initWithComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum:ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_get_LONG() withNSString:@"INT64" withInt:2];
    ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_UINT64 = [[ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum alloc] initWithComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum:ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_get_LONG() withNSString:@"UINT64" withInt:3];
    ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_INT32 = [[ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum alloc] initWithComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum:ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_get_INT() withNSString:@"INT32" withInt:4];
    ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_FIXED64 = [[ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum alloc] initWithComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum:ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_get_LONG() withNSString:@"FIXED64" withInt:5];
    ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_FIXED32 = [[ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum alloc] initWithComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum:ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_get_INT() withNSString:@"FIXED32" withInt:6];
    ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_BOOL = [[ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum alloc] initWithComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum:ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_get_BOOLEAN() withNSString:@"BOOL" withInt:7];
    ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_STRING = [[ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum alloc] initWithComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum:ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_get_STRING() withNSString:@"STRING" withInt:8];
    ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_GROUP = [[ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum alloc] initWithComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum:ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_get_MESSAGE() withNSString:@"GROUP" withInt:9];
    ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_MESSAGE = [[ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum alloc] initWithComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum:ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_get_MESSAGE() withNSString:@"MESSAGE" withInt:10];
    ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_BYTES = [[ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum alloc] initWithComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum:ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_get_BYTE_STRING() withNSString:@"BYTES" withInt:11];
    ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_UINT32 = [[ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum alloc] initWithComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum:ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_get_INT() withNSString:@"UINT32" withInt:12];
    ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_ENUM = [[ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum alloc] initWithComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum:ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_get_ENUM() withNSString:@"ENUM" withInt:13];
    ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_SFIXED32 = [[ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum alloc] initWithComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum:ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_get_INT() withNSString:@"SFIXED32" withInt:14];
    ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_SFIXED64 = [[ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum alloc] initWithComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum:ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_get_LONG() withNSString:@"SFIXED64" withInt:15];
    ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_SINT32 = [[ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum alloc] initWithComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum:ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_get_INT() withNSString:@"SINT32" withInt:16];
    ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_SINT64 = [[ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum alloc] initWithComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum:ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_get_LONG() withNSString:@"SINT64" withInt:17];
    J2OBJC_SET_INITIALIZED(ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum)
  }
}

+ (const J2ObjcClassInfo *)__metadata {
  static const J2ObjcMethodInfo methods[] = {
    { "initWithComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum:withNSString:withInt:", "Type", NULL, 0x2, NULL },
    { "toProto", NULL, "Lcom.google.protobuf.DescriptorProtos$FieldDescriptorProto$Type;", 0x1, NULL },
    { "getJavaType", NULL, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", 0x1, NULL },
    { "valueOfWithComGoogleProtobufDescriptorProtos$FieldDescriptorProto$TypeEnum:", "valueOf", "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", 0x9, NULL },
  };
  static const J2ObjcFieldInfo fields[] = {
    { "DOUBLE", "DOUBLE", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_DOUBLE,  },
    { "FLOAT", "FLOAT", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_FLOAT,  },
    { "INT64", "INT64", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_INT64,  },
    { "UINT64", "UINT64", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_UINT64,  },
    { "INT32", "INT32", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_INT32,  },
    { "FIXED64", "FIXED64", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_FIXED64,  },
    { "FIXED32", "FIXED32", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_FIXED32,  },
    { "BOOL", "BOOL", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_BOOL,  },
    { "STRING", "STRING", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_STRING,  },
    { "GROUP", "GROUP", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_GROUP,  },
    { "MESSAGE", "MESSAGE", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_MESSAGE,  },
    { "BYTES", "BYTES", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_BYTES,  },
    { "UINT32", "UINT32", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_UINT32,  },
    { "ENUM", "ENUM", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_ENUM,  },
    { "SFIXED32", "SFIXED32", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_SFIXED32,  },
    { "SFIXED64", "SFIXED64", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_SFIXED64,  },
    { "SINT32", "SINT32", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_SINT32,  },
    { "SINT64", "SINT64", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;", &ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_SINT64,  },
    { "javaType_", NULL, 0x2, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", NULL,  },
  };
  static const char *superclass_type_args[] = {"Lcom.google.protobuf.Descriptors$FieldDescriptor$Type;"};
  static const J2ObjcClassInfo _ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum = { 1, "Type", "com.google.protobuf", "Descriptors$FieldDescriptor", 0x4019, 4, methods, 19, fields, 1, superclass_type_args};
  return &_ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum;
}

@end

ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum *ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_valueOfWithComGoogleProtobufDescriptorProtos$FieldDescriptorProto$TypeEnum_(ComGoogleProtobufDescriptorProtos$FieldDescriptorProto$TypeEnum *type) {
  ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_initialize();
  return IOSObjectArray_Get(nil_chk(ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum_values()), [((ComGoogleProtobufDescriptorProtos$FieldDescriptorProto$TypeEnum *) nil_chk(type)) getNumber] - 1);
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufDescriptors$FieldDescriptor$TypeEnum)

J2OBJC_INITIALIZED_DEFN(ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum)

ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum *ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_values_[9];

@implementation ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum

- (instancetype)initWithId:(id)defaultDefault
              withNSString:(NSString *)__name
                   withInt:(jint)__ordinal {
  if (self = [super initWithNSString:__name withInt:__ordinal]) {
    ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_set_defaultDefault_(self, defaultDefault);
  }
  return self;
}

IOSObjectArray *ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_values() {
  ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_initialize();
  return [IOSObjectArray arrayWithObjects:ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_values_ count:9 type:ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_class_()];
}
+ (IOSObjectArray *)values {
  return ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_values();
}

+ (ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum *)valueOfWithNSString:(NSString *)name {
  return ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_valueOfWithNSString_(name);
}

ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum *ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_valueOfWithNSString_(NSString *name) {
  ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_initialize();
  for (int i = 0; i < 9; i++) {
    ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum *e = ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_values_[i];
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
  if (self == [ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum class]) {
    ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_INT = [[ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum alloc] initWithId:JavaLangInteger_valueOfWithInt_(0) withNSString:@"INT" withInt:0];
    ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_LONG = [[ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum alloc] initWithId:JavaLangLong_valueOfWithLong_(0LL) withNSString:@"LONG" withInt:1];
    ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_FLOAT = [[ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum alloc] initWithId:JavaLangFloat_valueOfWithFloat_(0.0f) withNSString:@"FLOAT" withInt:2];
    ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_DOUBLE = [[ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum alloc] initWithId:JavaLangDouble_valueOfWithDouble_(0.0) withNSString:@"DOUBLE" withInt:3];
    ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_BOOLEAN = [[ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum alloc] initWithId:JavaLangBoolean_valueOfWithBoolean_(NO) withNSString:@"BOOLEAN" withInt:4];
    ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_STRING = [[ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum alloc] initWithId:@"" withNSString:@"STRING" withInt:5];
    ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_BYTE_STRING = [[ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum alloc] initWithId:ComGoogleProtobufByteString_get_EMPTY_() withNSString:@"BYTE_STRING" withInt:6];
    ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_ENUM = [[ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum alloc] initWithId:nil withNSString:@"ENUM" withInt:7];
    ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_MESSAGE = [[ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum alloc] initWithId:nil withNSString:@"MESSAGE" withInt:8];
    J2OBJC_SET_INITIALIZED(ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum)
  }
}

+ (const J2ObjcClassInfo *)__metadata {
  static const J2ObjcMethodInfo methods[] = {
    { "initWithId:withNSString:withInt:", "JavaType", NULL, 0x2, NULL },
  };
  static const J2ObjcFieldInfo fields[] = {
    { "INT", "INT", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_INT,  },
    { "LONG", "LONG", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_LONG,  },
    { "FLOAT", "FLOAT", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_FLOAT,  },
    { "DOUBLE", "DOUBLE", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_DOUBLE,  },
    { "BOOLEAN", "BOOLEAN", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_BOOLEAN,  },
    { "STRING", "STRING", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_STRING,  },
    { "BYTE_STRING", "BYTE_STRING", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_BYTE_STRING,  },
    { "ENUM", "ENUM", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_ENUM,  },
    { "MESSAGE", "MESSAGE", 0x4019, "Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;", &ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum_MESSAGE,  },
    { "defaultDefault_", NULL, 0x12, "Ljava.lang.Object;", NULL,  },
  };
  static const char *superclass_type_args[] = {"Lcom.google.protobuf.Descriptors$FieldDescriptor$JavaType;"};
  static const J2ObjcClassInfo _ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum = { 1, "JavaType", "com.google.protobuf", "Descriptors$FieldDescriptor", 0x4019, 1, methods, 10, fields, 1, superclass_type_args};
  return &_ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufDescriptors$FieldDescriptor$JavaTypeEnum)
