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
#import "java/lang/Enum.h"
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

IOSObjectArray *CreateFields(
    jint fieldCount, CGPFieldData *fieldData, CGPDescriptor *containingType) {
  IOSObjectArray *fields = [IOSObjectArray newArrayWithLength:fieldCount
      type:ComGoogleProtobufDescriptors_FieldDescriptor_class_()];
  CGPFieldDescriptor **fieldsBuf = fields->buffer_;
  for (jint i = 0; i < fieldCount; i++) {
    fieldsBuf[i] = [[CGPFieldDescriptor alloc] initWithData:&fieldData[i]
                                             containingType:containingType];
  }
  return fields;
}

CGPDescriptor *CGPInitDescriptor(
    Class messageClass, Class builderClass, CGPMessageFlags flags,
    size_t storageSize) {
  return [[CGPDescriptor alloc]
      initWithMessageClass:messageClass
              builderClass:builderClass
                     flags:flags
               storageSize:storageSize];
}

void CGPInitFields(
    CGPDescriptor *descriptor, jint fieldCount, CGPFieldData *fieldData,
    jint oneofCount, CGPOneofData *oneofData) {
  descriptor->fields_ = CreateFields(fieldCount, fieldData, descriptor);

  if (oneofCount > 0) {
    IOSObjectArray *oneofs = [IOSObjectArray newArrayWithLength:oneofCount
        type:ComGoogleProtobufDescriptors_OneofDescriptor_class_()];
    CGPFieldDescriptor **fieldsBuf = descriptor->fields_->buffer_;
    for (jint i = 0; i < oneofCount; i++) {
      CGPOneofDescriptor *newOneof = [[CGPOneofDescriptor alloc] initWithData:&oneofData[i]
                                                               containingType:descriptor];
      oneofs->buffer_[i] = newOneof;
      uint32_t firstFieldIdx = oneofData[i].firstFieldIdx;
      uint32_t lastFieldIdx = firstFieldIdx + oneofData[i].fieldCount;
      for (uint32_t j = firstFieldIdx; j < lastFieldIdx; j++) {
        fieldsBuf[j]->containingOneof_ = newOneof;
      }
    }
    descriptor->oneofs_ = oneofs;
  }
}

CGPDescriptor *NewMapEntryDescriptor(CGPFieldData *fieldData) {
  CGPDescriptor *descriptor = [[CGPDescriptor alloc] init];
  descriptor->fields_ = CreateFields(2, fieldData, descriptor);
  return descriptor;
}

CGPEnumDescriptor *CGPInitializeEnumType(
    Class enumClass, jint valuesCount, JavaLangEnum<ComGoogleProtobufProtocolMessageEnum> *values[],
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
    JavaLangEnum_initWithNSString_withInt_(newEnum, names[i], i);
    *(jint *)(enumPtr + valueOffset) = intValues[i];
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

void CGPInitializeOneofCaseEnum(
    Class enumClass, jint valuesCount, JavaLangEnum<ComGoogleProtobufInternal_EnumLite> *values[],
    NSString **names, jint *intValues) {
  Ivar valueIvar = class_getInstanceVariable(enumClass, "value_");
  ptrdiff_t valueOffset = ivar_getOffset(valueIvar);

  size_t enumSize = class_getInstanceSize(enumClass);
  uintptr_t enumPtr = (uintptr_t)calloc(valuesCount, enumSize);

  for (jint i = 0; i < valuesCount; i++) {
    JavaLangEnum<ComGoogleProtobufInternal_EnumLite> *newEnum =
        objc_constructInstance(enumClass, (void *)enumPtr);
    JavaLangEnum_initWithNSString_withInt_(newEnum, names[i], i);
    *(jint *)(enumPtr + valueOffset) = intValues[i];
    values[i] = newEnum;
    enumPtr += enumSize;
  }
}

static inline ComGoogleProtobufDescriptors_FieldDescriptor_Type *GetTypeObj(CGPFieldType type) {
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_initialize();
  return ComGoogleProtobufDescriptors_FieldDescriptor_Type_values_[type];
}

@implementation ComGoogleProtobufDescriptors_Descriptor

- (instancetype)initWithMessageClass:(Class)messageClass
                        builderClass:(Class)builderClass
                               flags:(CGPMessageFlags)flags
                         storageSize:(size_t)storageSize {
  if (self = [self init]) {
    messageClass_ = messageClass;
    builderClass_ = builderClass;
    flags_ = flags;
    storageSize_ = storageSize;
    defaultInstance_ = CGPNewMessage(self);
  }
  return self;
}

- (NSString *)getName {
  return [[messageClass_ java_getClass] getSimpleName];
}

- (NSString *)getFullName {
  return [[messageClass_ java_getClass] getName];
}

- (id<JavaUtilList>)getFields {
  return [JavaUtilArrays asListWithNSObjectArray:fields_];
}

- (id<JavaUtilList>)getOneofs {
  return [JavaUtilArrays asListWithNSObjectArray:oneofs_];
}

- (CGPFieldDescriptor *)findFieldByNumberWithInt:(jint)fieldId {
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

int SerializationOrderComp(const void *a, const void *b) {
  return CGPFieldGetNumber(*(const CGPFieldDescriptor **)a) -
      CGPFieldGetNumber(*(const CGPFieldDescriptor **)b);
}

IOSObjectArray *CGPGetSerializationOrderFields(CGPDescriptor *descriptor) {
  @synchronized(descriptor) {
    IOSObjectArray *result = descriptor->serializationOrderFields_;
    if (!result) {
      result = [descriptor->fields_ copyWithZone:nil];
      qsort(result->buffer_, result->size_, sizeof(id), SerializationOrderComp);
      descriptor->serializationOrderFields_ = result;
    }
    return result;
  }
}

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

// Default values for enums and message types can't be assigned in static data.
static void CGPFieldFixDefaultValue(CGPFieldDescriptor *descriptor) {
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
        Class enumClass = data->objcType;
        NSCAssert(enumClass != nil, @"Field data is missing objc enum type.");
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
        if (CGPFieldIsMap(descriptor)) {
          descriptor->valueType_ = NewMapEntryDescriptor(data->mapEntryFields);
          break;
        }
        CGPDescriptor *msgDescriptor =
            data->descriptorRef ? (__bridge id)*(data->descriptorRef) : nil;
        if (msgDescriptor == nil) {
          // The descriptorRef wasn't specified, so use its accessor.
          Class msgClass = data->objcType;
          msgDescriptor = [msgClass performSelector:@selector(getDescriptor)];
        }
        NSCAssert(msgDescriptor != nil, @"Field data is missing descriptor reference.");
        data->defaultValue.valueId = msgDescriptor->defaultInstance_;
        descriptor->valueType_ = msgDescriptor;
        break;
      }
  }
}

- (instancetype)initWithData:(CGPFieldData *)data
              containingType:(CGPDescriptor *)containingType {
  if (self = [self init]) {
    data_ = data;
    tag_ = TagFromData(data);
    javaType_ = [GetTypeObj(data->type)->javaType_ ordinal];
    fieldOptions_ = InitFieldOptions(data->optionsData);
    containingType_ = containingType;
    CGPFieldFixDefaultValue(self);
  }
  return self;
}

- (ComGoogleProtobufDescriptors_FieldDescriptor_Type *)getType {
  return GetTypeObj(data_->type);
}

- (ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *)getJavaType {
  return GetTypeObj(data_->type)->javaType_;
}

- (jint)getNumber {
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

- (CGPOneofDescriptor *)getContainingOneof {
  return containingOneof_;
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

CGPEnumValueDescriptor *CGPEnumValueDescriptorFromInt(CGPEnumDescriptor *enumType, jint value) {
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

- (CGPEnumValueDescriptor *)findValueByNumberWithInt:(jint)number {
  return CGPEnumValueDescriptorFromInt(self, number);
}

J2OBJC_ETERNAL_SINGLETON

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufDescriptors_EnumDescriptor)

@implementation ComGoogleProtobufDescriptors_EnumValueDescriptor

- (jint)getNumber {
  return number_;
}

- (NSString *)getName {
  return [enum_ name];
}

J2OBJC_ETERNAL_SINGLETON

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufDescriptors_EnumValueDescriptor)

@implementation ComGoogleProtobufDescriptors_OneofDescriptor

- (instancetype)initWithData:(CGPOneofData *)data
              containingType:(CGPDescriptor *)containingType {
  if (self = [self init]) {
    data_ = data;
    containingType_ = containingType;
  }
  return self;
}

- (NSString *)getName {
  return [NSString stringWithUTF8String:data_->name];
}

- (CGPDescriptor *)getContainingType {
  return containingType_;
}

- (id<JavaUtilList>)getFields {
  jint toIndex = data_->firstFieldIdx + data_->fieldCount;
  return [[containingType_ getFields] subListWithInt:data_->firstFieldIdx withInt:toIndex];
}

J2OBJC_ETERNAL_SINGLETON

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufDescriptors_OneofDescriptor)

Class<ComGoogleProtobufInternal_EnumLite> CGPOneofGetCaseClass(CGPOneofDescriptor *oneof) {
  Class containingCls = oneof->containingType_->messageClass_;
  const char *containingName = class_getName(containingCls);
  size_t len = strlen(containingName) + strlen(oneof->data_->javaName) + 6;
  char *clsName = (char *)malloc(len);
  strcpy(clsName, containingName);
  strcat(clsName, "_");
  strcat(clsName, oneof->data_->javaName);
  strcat(clsName, "Case");
  Class<ComGoogleProtobufInternal_EnumLite> cls = objc_getClass(clsName);
  free(clsName);
  return cls;
}

// The remainder of this file is copied from the translation of the types
// FieldDescriptor.Type and FieldDescriptor.JavaType in Descriptor.java.

J2OBJC_INITIALIZED_DEFN(ComGoogleProtobufDescriptors_FieldDescriptor_Type)

ComGoogleProtobufDescriptors_FieldDescriptor_Type *ComGoogleProtobufDescriptors_FieldDescriptor_Type_values_[18];

@implementation ComGoogleProtobufDescriptors_FieldDescriptor_Type

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
    size_t objSize = class_getInstanceSize(self);
    size_t allocSize = 18 * objSize;
    uintptr_t ptr = (uintptr_t)calloc(allocSize, 1);
    id e;
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, DOUBLE) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(e, JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, DOUBLE), @"DOUBLE", 0);
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, FLOAT) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(e, JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, FLOAT), @"FLOAT", 1);
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, INT64) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(e, JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, LONG), @"INT64", 2);
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, UINT64) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(e, JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, LONG), @"UINT64", 3);
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, INT32) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(e, JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, INT), @"INT32", 4);
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, FIXED64) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(e, JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, LONG), @"FIXED64", 5);
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, FIXED32) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(e, JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, INT), @"FIXED32", 6);
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, BOOL) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(e, JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, BOOLEAN), @"BOOL", 7);
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, STRING) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(e, JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, STRING), @"STRING", 8);
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, GROUP) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(e, JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, MESSAGE), @"GROUP", 9);
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, MESSAGE) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(e, JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, MESSAGE), @"MESSAGE", 10);
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, BYTES) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(e, JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, BYTE_STRING), @"BYTES", 11);
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, UINT32) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(e, JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, INT), @"UINT32", 12);
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, ENUM) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(e, JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, ENUM), @"ENUM", 13);
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, SFIXED32) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(e, JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, INT), @"SFIXED32", 14);
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, SFIXED64) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(e, JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, LONG), @"SFIXED64", 15);
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, SINT32) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(e, JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, INT), @"SINT32", 16);
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, SINT64) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(e, JreLoadEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, LONG), @"SINT64", 17);
    J2OBJC_SET_INITIALIZED(ComGoogleProtobufDescriptors_FieldDescriptor_Type)
  }
}

+ (const J2ObjcClassInfo *)__metadata {
  static J2ObjcMethodInfo methods[] = {
    { NULL, "LComGoogleProtobufDescriptorProtos_FieldDescriptorProto_Type;", 0x1, -1, -1, -1, -1,
      -1, -1 },
    { NULL, "LComGoogleProtobufDescriptors_FieldDescriptor_JavaType;", 0x1, -1, -1, -1, -1, -1,
      -1 },
    { NULL, "LComGoogleProtobufDescriptors_FieldDescriptor_Type;", 0x9, 0, 1, -1, -1, -1, -1 },
    { NULL, "[LComGoogleProtobufDescriptors_FieldDescriptor_Type;", 0x9, -1, -1, -1, -1, -1, -1 },
    { NULL, "LComGoogleProtobufDescriptors_FieldDescriptor_Type;", 0x9, 0, 2, -1, -1, -1, -1 },
  };

  methods[0].selector = @selector(toProto);
  methods[1].selector = @selector(getJavaType);
  methods[2].selector =
      @selector(valueOfWithComGoogleProtobufDescriptorProtos_FieldDescriptorProto_Type:);
  methods[3].selector = @selector(values);
  methods[4].selector = @selector(valueOfWithNSString:);

  static const J2ObjcFieldInfo fields[] = {
    { "DOUBLE", "LComGoogleProtobufDescriptors_FieldDescriptor_Type;", .constantValue.asLong = 0,
      0x4019, -1, 3, -1, -1 },
    { "FLOAT", "LComGoogleProtobufDescriptors_FieldDescriptor_Type;", .constantValue.asLong = 0,
      0x4019, -1, 4, -1, -1 },
    { "INT64", "LComGoogleProtobufDescriptors_FieldDescriptor_Type;", .constantValue.asLong = 0,
      0x4019, -1, 5, -1, -1 },
    { "UINT64", "LComGoogleProtobufDescriptors_FieldDescriptor_Type;", .constantValue.asLong = 0,
      0x4019, -1, 6, -1, -1 },
    { "INT32", "LComGoogleProtobufDescriptors_FieldDescriptor_Type;", .constantValue.asLong = 0,
      0x4019, -1, 7, -1, -1 },
    { "FIXED64", "LComGoogleProtobufDescriptors_FieldDescriptor_Type;", .constantValue.asLong = 0,
      0x4019, -1, 8, -1, -1 },
    { "FIXED32", "LComGoogleProtobufDescriptors_FieldDescriptor_Type;", .constantValue.asLong = 0,
      0x4019, -1, 9, -1, -1 },
    { "BOOL", "LComGoogleProtobufDescriptors_FieldDescriptor_Type;", .constantValue.asLong = 0,
      0x4019, -1, 10, -1, -1 },
    { "STRING", "LComGoogleProtobufDescriptors_FieldDescriptor_Type;", .constantValue.asLong = 0,
      0x4019, -1, 11, -1, -1 },
    { "GROUP", "LComGoogleProtobufDescriptors_FieldDescriptor_Type;", .constantValue.asLong = 0,
      0x4019, -1, 12, -1, -1 },
    { "MESSAGE", "LComGoogleProtobufDescriptors_FieldDescriptor_Type;", .constantValue.asLong = 0,
      0x4019, -1, 13, -1, -1 },
    { "BYTES", "LComGoogleProtobufDescriptors_FieldDescriptor_Type;", .constantValue.asLong = 0,
      0x4019, -1, 14, -1, -1 },
    { "UINT32", "LComGoogleProtobufDescriptors_FieldDescriptor_Type;", .constantValue.asLong = 0,
      0x4019, -1, 15, -1, -1 },
    { "ENUM", "LComGoogleProtobufDescriptors_FieldDescriptor_Type;", .constantValue.asLong = 0,
      0x4019, -1, 16, -1, -1 },
    { "SFIXED32", "LComGoogleProtobufDescriptors_FieldDescriptor_Type;", .constantValue.asLong = 0,
      0x4019, -1, 17, -1, -1 },
    { "SFIXED64", "LComGoogleProtobufDescriptors_FieldDescriptor_Type;", .constantValue.asLong = 0,
      0x4019, -1, 18, -1, -1 },
    { "SINT32", "LComGoogleProtobufDescriptors_FieldDescriptor_Type;", .constantValue.asLong = 0,
      0x4019, -1, 19, -1, -1 },
    { "SINT64", "LComGoogleProtobufDescriptors_FieldDescriptor_Type;", .constantValue.asLong = 0,
      0x4019, -1, 20, -1, -1 },
    { "javaType_", "LComGoogleProtobufDescriptors_FieldDescriptor_JavaType;",
      .constantValue.asLong = 0, 0x2, -1, -1, -1, -1 },
  };
  static const void *ptrTable[] = {
    "valueOf", "LComGoogleProtobufDescriptorProtos_FieldDescriptorProto_Type;", "LNSString;",
    &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, DOUBLE),
    &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, FLOAT),
    &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, INT64),
    &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, UINT64),
    &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, INT32),
    &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, FIXED64),
    &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, FIXED32),
    &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, BOOL),
    &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, STRING),
    &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, GROUP),
    &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, MESSAGE),
    &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, BYTES),
    &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, UINT32),
    &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, ENUM),
    &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, SFIXED32),
    &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, SFIXED64),
    &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, SINT32),
    &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_Type, SINT64),
    "LComGoogleProtobufDescriptors_FieldDescriptor;",
    "Ljava/lang/Enum<Lcom/google/protobuf/Descriptors$FieldDescriptor$Type;>;" };
  static const J2ObjcClassInfo _ComGoogleProtobufDescriptors_FieldDescriptor_Type = {
    "Type", "com.google.protobuf", ptrTable, methods, fields, 7, 0x4019, 5, 19, 21, -1, -1, 22, -1
  };
  return &_ComGoogleProtobufDescriptors_FieldDescriptor_Type;
}

@end

void ComGoogleProtobufDescriptors_FieldDescriptor_Type_initWithComGoogleProtobufDescriptors_FieldDescriptor_JavaType_withNSString_withInt_(ComGoogleProtobufDescriptors_FieldDescriptor_Type *self, ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *javaType, NSString *__name, jint __ordinal) {
  JavaLangEnum_initWithNSString_withInt_(self, __name, __ordinal);
  JreStrongAssign(&self->javaType_, javaType);
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

ComGoogleProtobufDescriptors_FieldDescriptor_Type *ComGoogleProtobufDescriptors_FieldDescriptor_Type_fromOrdinal(NSUInteger ordinal) {
  ComGoogleProtobufDescriptors_FieldDescriptor_Type_initialize();
  if (ordinal >= 18) {
    return nil;
  }
  return ComGoogleProtobufDescriptors_FieldDescriptor_Type_values_[ordinal];
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufDescriptors_FieldDescriptor_Type)

J2OBJC_INITIALIZED_DEFN(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType)

ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_values_[9];

@implementation ComGoogleProtobufDescriptors_FieldDescriptor_JavaType

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
    size_t objSize = class_getInstanceSize(self);
    size_t allocSize = 9 * objSize;
    uintptr_t ptr = (uintptr_t)calloc(allocSize, 1);
    id e;
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, INT) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initWithId_withNSString_withInt_(e, JavaLangInteger_valueOfWithInt_(0), @"INT", 0);
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, LONG) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initWithId_withNSString_withInt_(e, JavaLangLong_valueOfWithLong_(0LL), @"LONG", 1);
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, FLOAT) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initWithId_withNSString_withInt_(e, JavaLangFloat_valueOfWithFloat_(0.0f), @"FLOAT", 2);
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, DOUBLE) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initWithId_withNSString_withInt_(e, JavaLangDouble_valueOfWithDouble_(0.0), @"DOUBLE", 3);
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, BOOLEAN) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initWithId_withNSString_withInt_(e, JavaLangBoolean_valueOfWithBoolean_(false), @"BOOLEAN", 4);
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, STRING) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initWithId_withNSString_withInt_(e, @"", @"STRING", 5);
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, BYTE_STRING) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initWithId_withNSString_withInt_(e, JreLoadStatic(ComGoogleProtobufByteString, EMPTY), @"BYTE_STRING", 6);
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, ENUM) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initWithId_withNSString_withInt_(e, nil, @"ENUM", 7);
    (JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, MESSAGE) = e = objc_constructInstance(self, (void *)ptr), ptr += objSize);
    ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initWithId_withNSString_withInt_(e, nil, @"MESSAGE", 8);
    J2OBJC_SET_INITIALIZED(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType)
  }
}

+ (const J2ObjcClassInfo *)__metadata {
  static J2ObjcMethodInfo methods[] = {
    { NULL, "[LComGoogleProtobufDescriptors_FieldDescriptor_JavaType;", 0x9, -1, -1, -1, -1, -1,
      -1 },
    { NULL, "LComGoogleProtobufDescriptors_FieldDescriptor_JavaType;", 0x9, 0, 1, -1, -1, -1, -1 },
  };

  methods[0].selector = @selector(values);
  methods[1].selector = @selector(valueOfWithNSString:);

  static const J2ObjcFieldInfo fields[] = {
    { "INT", "LComGoogleProtobufDescriptors_FieldDescriptor_JavaType;", .constantValue.asLong = 0,
      0x4019, -1, 2, -1, -1 },
    { "LONG", "LComGoogleProtobufDescriptors_FieldDescriptor_JavaType;", .constantValue.asLong = 0,
      0x4019, -1, 3, -1, -1 },
    { "FLOAT", "LComGoogleProtobufDescriptors_FieldDescriptor_JavaType;", .constantValue.asLong = 0,
      0x4019, -1, 4, -1, -1 },
    { "DOUBLE", "LComGoogleProtobufDescriptors_FieldDescriptor_JavaType;",
      .constantValue.asLong = 0, 0x4019, -1, 5, -1, -1 },
    { "BOOLEAN", "LComGoogleProtobufDescriptors_FieldDescriptor_JavaType;",
      .constantValue.asLong = 0, 0x4019, -1, 6, -1, -1 },
    { "STRING", "LComGoogleProtobufDescriptors_FieldDescriptor_JavaType;",
      .constantValue.asLong = 0, 0x4019, -1, 7, -1, -1 },
    { "BYTE_STRING", "LComGoogleProtobufDescriptors_FieldDescriptor_JavaType;",
      .constantValue.asLong = 0, 0x4019, -1, 8, -1, -1 },
    { "ENUM", "LComGoogleProtobufDescriptors_FieldDescriptor_JavaType;", .constantValue.asLong = 0,
      0x4019, -1, 9, -1, -1 },
    { "MESSAGE", "LComGoogleProtobufDescriptors_FieldDescriptor_JavaType;",
      .constantValue.asLong = 0, 0x4019, -1, 10, -1, -1 },
    { "defaultDefault_", "LNSObject;", .constantValue.asLong = 0, 0x12, -1, -1, -1, -1 },
  };
  static const void *ptrTable[] = {
    "valueOf", "LNSString;", &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, INT),
    &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, LONG),
    &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, FLOAT),
    &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, DOUBLE),
    &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, BOOLEAN),
    &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, STRING),
    &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, BYTE_STRING),
    &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, ENUM),
    &JreEnum(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType, MESSAGE),
    "LComGoogleProtobufDescriptors_FieldDescriptor;",
    "Ljava/lang/Enum<Lcom/google/protobuf/Descriptors$FieldDescriptor$JavaType;>;" };
  static const J2ObjcClassInfo _ComGoogleProtobufDescriptors_FieldDescriptor_JavaType = {
    "JavaType", "com.google.protobuf", ptrTable, methods, fields, 7, 0x4019, 2, 10, 11, -1, -1, 12,
    -1 };
  return &_ComGoogleProtobufDescriptors_FieldDescriptor_JavaType;
}

@end

void ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initWithId_withNSString_withInt_(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *self, id defaultDefault, NSString *__name, jint __ordinal) {
  JavaLangEnum_initWithNSString_withInt_(self, __name, __ordinal);
  JreStrongAssign(&self->defaultDefault_, defaultDefault);
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

ComGoogleProtobufDescriptors_FieldDescriptor_JavaType *ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_fromOrdinal(NSUInteger ordinal) {
  ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_initialize();
  if (ordinal >= 9) {
    return nil;
  }
  return ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_values_[ordinal];
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufDescriptors_FieldDescriptor_JavaType)
