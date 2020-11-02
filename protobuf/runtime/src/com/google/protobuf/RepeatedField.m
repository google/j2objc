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

#import "com/google/protobuf/RepeatedField.h"

#import "J2ObjC_source.h"
#import "com/google/protobuf/ByteString.h"
#import "com/google/protobuf/Descriptors_PackagePrivate.h"
#import "com/google/protobuf/ProtocolStringList.h"
#import "java/lang/IndexOutOfBoundsException.h"
#import "java/util/AbstractList.h"
#import "java/util/ArrayList.h"

#define MIN_REPEATED_FIELD_SIZE 4

static CGPRepeatedFieldData *NewData() {
  CGPRepeatedFieldData *data = calloc(sizeof(CGPRepeatedFieldData), 1);
  __c11_atomic_store(&data->ref_count, 1, __ATOMIC_RELAXED);
  return data;
}

void CGPRepeatedFieldReserve(CGPRepeatedField *field, uint32_t new_size, size_t elemSize) {
  CGPRepeatedFieldData *data = field->data;
  if (data == NULL) {
    data = field->data = NewData();
  }

  if (data->total_size >= new_size) {
    return;
  }

  uint32_t newTotalSize = MAX(MIN_REPEATED_FIELD_SIZE, MAX(data->total_size * 2, new_size));
  data->total_size = newTotalSize;
  data->buffer = realloc(data->buffer, newTotalSize * elemSize);
}

void CGPRepeatedFieldCopyData(CGPRepeatedField *field, CGPFieldJavaType type) {
  CGPRepeatedFieldData *oldData = field->data;
  if (oldData == NULL) {
    return;
  }

  size_t elemSize = CGPGetTypeSize(type);
  CGPRepeatedFieldData *newData = NewData();
  newData->size = newData->total_size = oldData->size;
  field->data = newData;
  newData->buffer = malloc(newData->size * elemSize);
  memcpy(newData->buffer, oldData->buffer, newData->size * elemSize);

  if (CGPIsRetainedType(type)) {
    for (uint32_t i = 0; i < newData->size; i++) {
      RETAIN_(((id *)newData->buffer)[i]);
    }
  }
}

void CGPRepeatedFieldAppendOther(
    CGPRepeatedField *field, CGPRepeatedField *other, CGPFieldJavaType type) {
  uint32_t otherSize = CGPRepeatedFieldSize(other);
  if (otherSize == 0) {
    return;
  }
  uint32_t fieldSize = CGPRepeatedFieldSize(field);
  size_t elemSize = CGPGetTypeSize(type);
  CGPRepeatedFieldReserve(field, fieldSize + otherSize, elemSize);
  CGPRepeatedFieldData *data = field->data;
  uint8_t *newBuffer = (uint8_t *)data->buffer + data->size * elemSize;
  memcpy(newBuffer, other->data->buffer, otherSize * elemSize);
  data->size += otherSize;
  if (CGPIsRetainedType(type)) {
    for (uint32_t i = 0; i < otherSize; i++) {
      RETAIN_(((id *)newBuffer)[i]);
    }
  }
}

static void ReleaseData(CGPRepeatedFieldData *data, CGPFieldJavaType type) {
  if (data == NULL) {
    return;
  }

  if (__c11_atomic_fetch_sub(&data->ref_count, 1, __ATOMIC_RELEASE) == 1) {
    __c11_atomic_thread_fence(__ATOMIC_ACQUIRE);

    if (CGPIsRetainedType(type)) {
      for (uint32_t i = 0; i < data->size; i++) {
        RELEASE_(((id *)data->buffer)[i]);
      }
    }

    free(data->buffer);
    free(data);
  }
}

void CGPRepeatedFieldClear(CGPRepeatedField *field, CGPFieldJavaType type) {
  ReleaseData(field->data, type);
  field->data = NULL;
}

id CGPRepeatedFieldGet(CGPRepeatedField *field, jint index, CGPFieldDescriptor *descriptor) {
  CGPRepeatedFieldCheckBounds(field, index);

#define REPEATED_GET_CASE(NAME) \
  return CGPToReflectionType##NAME(((TYPE_##NAME *)field->data->buffer)[index], descriptor); \

  SWITCH_TYPES_WITH_ENUM(CGPFieldGetJavaType(descriptor), REPEATED_GET_CASE)

#undef REPEATED_GET_CASE
}

void CGPRepeatedMessageFieldRemove(CGPRepeatedField *field, jint index) {
  CGPRepeatedFieldCheckBounds(field, index);
  uint32_t count = CGPRepeatedFieldSize(field);
  id *msgBuffer = (id *)field->data->buffer;
  AUTORELEASE(msgBuffer[index]);
  if (count > (uint32_t)index + 1) {
    memmove(&(msgBuffer[index]),
            &(msgBuffer[index + 1]),
            sizeof(id) * (count - (index + 1)));
  }
  field->data->size -= 1;
}

void CGPRepeatedFieldSet(CGPRepeatedField *field, jint index, id value, CGPFieldJavaType type) {
  CGPRepeatedFieldCheckBounds(field, index);

#define REPEATED_SET_CASE(NAME) \
  { \
    TYPE_##NAME *ptr = &((TYPE_##NAME *)field->data->buffer)[index]; \
    TYPE_ASSIGN_##NAME(*ptr, CGPFromReflectionType##NAME(value)); \
    break; \
  }

  SWITCH_TYPES_WITH_ENUM(type, REPEATED_SET_CASE)

#undef REPEATED_SET_CASE
}

// Make sure to reserve enough space in the buffer BEFORE calling this.
static void CGPRepeatedFieldAddUnsafe(CGPRepeatedFieldData *data, id value, CGPFieldJavaType type) {
#define REPEATED_ADD_CASE(NAME) \
  ((TYPE_##NAME *)data->buffer)[data->size++] = \
      TYPE_RETAIN_##NAME(CGPFromReflectionType##NAME(value)); \
  break;

  SWITCH_TYPES_WITH_ENUM(type, REPEATED_ADD_CASE)

#undef REPEATED_ADD_CASE
}

void CGPRepeatedFieldAdd(CGPRepeatedField *field, id value, CGPFieldJavaType type) {
  uint32_t total_size = CGPRepeatedFieldTotalSize(field);
  if (CGPRepeatedFieldSize(field) == total_size) {
    CGPRepeatedFieldReserve(field, total_size + 1, CGPGetTypeSize(type));
  }
  CGPRepeatedFieldAddUnsafe(field->data, value, type);
}

void CGPRepeatedFieldAssignFromList(
    CGPRepeatedField *field, id<JavaUtilList> list, CGPFieldJavaType type) {
  CGPRepeatedFieldClear(field, type);

  CGPRepeatedFieldReserve(field, [list size], CGPGetTypeSize(type));
  for (id elem in list) {
    CGPRepeatedFieldAddUnsafe(field->data, elem, type);
  }
}

id<JavaUtilList> CGPRepeatedFieldCopyList(CGPRepeatedField *field, CGPFieldDescriptor *descriptor) {
  id<JavaUtilList> newList =
      AUTORELEASE([[JavaUtilArrayList alloc] initWithInt:CGPRepeatedFieldSize(field)]);
  CGPRepeatedFieldData *data = field->data;
  if (data == NULL) {
    return newList;
  }

#define REPEATED_COPY_ELEM_CASE(NAME) \
  for (uint32_t i = 0; i < data->size; i++) { \
    [newList addWithId:CGPToReflectionType##NAME(((TYPE_##NAME *)data->buffer)[i], descriptor)]; \
  } \
  break;

  SWITCH_TYPES_WITH_ENUM(CGPFieldGetJavaType(descriptor), REPEATED_COPY_ELEM_CASE)

#undef REPEATED_COPY_ELEM_CASE

  return newList;
}

BOOL CGPRepeatedFieldIsEqual(CGPRepeatedField *a, CGPRepeatedField *b, CGPFieldJavaType type) {
  uint32_t aSize = CGPRepeatedFieldSize(a);
  uint32_t bSize = CGPRepeatedFieldSize(b);
  if (aSize != bSize) {
    return NO;
  }
  if (aSize == 0) {
    return YES;
  }
  if (CGPIsRetainedType(type)) {
    id *bufA = (id *)a->data->buffer;
    id *bufB = (id *)b->data->buffer;
    for (uint32_t i = 0; i < aSize; i++) {
      if (![bufA[i] isEqual:bufB[i]]) {
        return NO;
      }
    }
    return YES;
  } else {
    return memcmp(a->data->buffer, b->data->buffer, aSize * CGPGetTypeSize(type)) == 0;
  }
}

void CGPRepeatedFieldOutOfBounds(jint idx, uint32_t size) {
  NSString *msg =
      [NSString stringWithFormat:@"Repeated field index out-of-bounds. (index = %d, size = %d)",
                                 idx, (int)size];
  @throw AUTORELEASE([[JavaLangIndexOutOfBoundsException alloc] initWithNSString:msg]);
}

@interface CGPRepeatedFieldList : JavaUtilAbstractList {
 @package
  CGPRepeatedField field_;
  CGPFieldJavaType type_;
}
@end

// We need a subclass for String fields which must implement the ProtocolStringList interface.
@interface CGPRepeatedStringFieldList : CGPRepeatedFieldList < ComGoogleProtobufProtocolStringList >
@end

@interface CGPStringAsByteStringList : JavaUtilAbstractList {
 @package
  CGPRepeatedField field_;
}
@end

id<JavaUtilList> CGPNewRepeatedFieldList(CGPRepeatedField *field, CGPFieldJavaType type) {
  CGPRepeatedFieldList *list =
      type == ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_STRING ?
      [[CGPRepeatedStringFieldList alloc] init] : [[CGPRepeatedFieldList alloc] init];
  CGPRepeatedFieldData *data = field->data;
  if (data != NULL) {
    list->field_.data = data;
    __c11_atomic_fetch_add(&data->ref_count, 1, __ATOMIC_RELAXED);
  }
  list->type_ = type;
  return list;
}

@implementation CGPRepeatedFieldList

- (id)getWithInt:(jint)index {
  CGPRepeatedFieldCheckBounds(&field_, index);

#define REPEATED_LIST_GET_CASE(NAME) \
  return CGPBoxedValue##NAME(((TYPE_##NAME *)field_.data->buffer)[index]); \

  SWITCH_TYPES_NO_ENUM(type_, REPEATED_LIST_GET_CASE)

#undef REPEATED_LIST_GET_CASE
}

- (jint)size {
  return CGPRepeatedFieldSize(&field_);
}

- (void)dealloc {
  ReleaseData(field_.data, type_);
  [super dealloc];
}

@end

@implementation CGPRepeatedStringFieldList

- (id<JavaUtilList>)asByteStringList {
  CGPStringAsByteStringList *list = AUTORELEASE([[CGPStringAsByteStringList alloc] init]);
  if (field_.data != NULL) {
    list->field_.data = field_.data;
    __c11_atomic_fetch_add(&field_.data->ref_count, 1, __ATOMIC_RELAXED);
  }
  return list;
}

@end

@implementation CGPStringAsByteStringList

- (id)getWithInt:(jint)index {
  CGPRepeatedFieldCheckBounds(&field_, index);
  NSString *stringValue = ((id *)field_.data->buffer)[index];
  return ComGoogleProtobufByteString_copyFromUtf8WithNSString_(stringValue);
}

- (jint)size {
  return CGPRepeatedFieldSize(&field_);
}

- (void)dealloc {
  ReleaseData(field_.data, ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_STRING);
  [super dealloc];
}

@end
