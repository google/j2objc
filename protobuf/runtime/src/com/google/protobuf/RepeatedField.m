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
#import <Foundation/Foundation.h>
#import <limits.h>
#import <stdint.h>

#import "J2ObjC_source.h"
#import "com/google/protobuf/ByteString.h"
#import "com/google/protobuf/Descriptors_PackagePrivate.h"
#import "com/google/protobuf/ProtocolStringList.h"
#import "java/lang/IndexOutOfBoundsException.h"
#import "java/lang/Integer.h"
#import "java/lang/OutOfMemoryError.h"
#import "java/util/AbstractList.h"
#import "java/util/ArrayList.h"

// Initial element capacity when an empty repeated field first allocates storage.
#define MIN_REPEATED_FIELD_SIZE 4

// J2ObjC_common.h has no JreThrowOutOfMemoryError() helper; use the generated create_*_init()
// factory so class initialization and ARC/MRC autorelease conventions match translated code.
static __attribute__((noreturn)) void ThrowOutOfMemoryError(void) {
  @throw create_JavaLangOutOfMemoryError_init();
}

// Allocates a zeroed repeated-field header with ref_count = 1, or returns NULL on OOM.
static CGPRepeatedFieldData *NewDataOrNull(void) {
  CGPRepeatedFieldData *data = calloc(1, sizeof(CGPRepeatedFieldData));
  if (data != NULL) {
    __c11_atomic_store(&data->ref_count, 1, __ATOMIC_RELAXED);
  }
  return data;
}

static CGPRepeatedFieldData *NewData(void) {
  CGPRepeatedFieldData *data = NewDataOrNull();
  if (data == NULL) {
    ThrowOutOfMemoryError();
  }
  return data;
}

void CGPRepeatedFieldReserve(CGPRepeatedField *field, uint32_t new_size, size_t elemSize) {
  // Repeated fields back Java Lists whose size is a signed 32-bit jint.
  if (new_size > (uint32_t)INT_MAX) {
    ThrowOutOfMemoryError();
  }

  CGPRepeatedFieldData *data = field->data;
  if (data == NULL) {
    data = field->data = NewData();
  }

  if (data->total_size >= new_size) {
    return;
  }

  // Clamp capacity doubling at INT_MAX and check byte-count multiplication for size_t overflow.
  uint32_t doubledSize =
      data->total_size > (uint32_t)INT_MAX / 2 ? (uint32_t)INT_MAX : data->total_size * 2;
  uint32_t newTotalSize = MAX(MIN_REPEATED_FIELD_SIZE, MAX(doubledSize, new_size));
  uint32_t oldTotalSize = data->total_size;
  if (elemSize != 0 && (size_t)newTotalSize > SIZE_MAX / elemSize) {
    ThrowOutOfMemoryError();
  }
  size_t allocationBytes = (size_t)newTotalSize * elemSize;
  // Allocate into a temporary pointer before updating data so existing elements stay valid on OOM.
  void *newBuffer = realloc(data->buffer, allocationBytes);
  if (newBuffer == NULL && allocationBytes > 0) {
    ThrowOutOfMemoryError();
  }
  if (allocationBytes > 0) {
    bzero((uint8_t *)newBuffer + elemSize * oldTotalSize, elemSize * (newTotalSize - oldTotalSize));
  }
  data->buffer = newBuffer;
  data->total_size = newTotalSize;
}

void CGPRepeatedFieldCopyData(CGPRepeatedField *field, CGPFieldJavaType type) {
  CGPRepeatedFieldData *oldData = field->data;
  if (oldData == NULL) {
    return;
  }

  size_t elemSize = CGPGetTypeSize(type);
  // Check byte-size multiplication for overflow, and finish allocations before replacing
  // field->data.
  if (elemSize != 0 && (size_t)oldData->size > SIZE_MAX / elemSize) {
    ThrowOutOfMemoryError();
  }
  size_t bufferBytes = (size_t)oldData->size * elemSize;
  void *copiedBuffer = NULL;
  if (bufferBytes > 0) {
    copiedBuffer = malloc(bufferBytes);
    if (copiedBuffer == NULL) {
      ThrowOutOfMemoryError();
    }
    memcpy(copiedBuffer, oldData->buffer, bufferBytes);
  }
  CGPRepeatedFieldData *newData = NewDataOrNull();
  if (newData == NULL) {
    free(copiedBuffer);
    ThrowOutOfMemoryError();
  }
  newData->size = newData->total_size = oldData->size;
  newData->buffer = copiedBuffer;
  field->data = newData;

  if (CGPIsRetainedType(type)) {
    for (uint32_t index = 0; index < newData->size; index++) {
      RETAIN_(((id *)newData->buffer)[index]);
    }
  }
}

void CGPRepeatedFieldAppendOther(CGPRepeatedField *field, CGPRepeatedField *other,
                                 CGPFieldJavaType type) {
  uint32_t otherSize = CGPRepeatedFieldSize(other);
  if (otherSize == 0) {
    return;
  }
  size_t elemSize = CGPGetTypeSize(type);
  CGPRepeatedFieldReserveAdditionalCapacity(field, otherSize, elemSize);
  CGPRepeatedFieldData *data = field->data;
  uint8_t *newBuffer = (uint8_t *)data->buffer + data->size * elemSize;
  memcpy(newBuffer, other->data->buffer, otherSize * elemSize);
  data->size += otherSize;
  if (CGPIsRetainedType(type)) {
    for (uint32_t index = 0; index < otherSize; index++) {
      RETAIN_(((id *)newBuffer)[index]);
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
  return CGPToReflectionType##NAME(((TYPE_##NAME *)field->data->buffer)[index]);

  SWITCH_TYPES(CGPFieldGetJavaType(descriptor), REPEATED_GET_CASE)

#undef REPEATED_GET_CASE
}

void CGPRepeatedMessageFieldRemove(CGPRepeatedField *field, jint index) {
  CGPRepeatedFieldCheckBounds(field, index);
  uint32_t count = CGPRepeatedFieldSize(field);
  id *msgBuffer = (id *)field->data->buffer;
  AUTORELEASE(msgBuffer[index]);
  if (count > (uint32_t)index + 1) {
    memmove(&(msgBuffer[index]), &(msgBuffer[index + 1]), sizeof(id) * (count - (index + 1)));
  }
  field->data->size -= 1;
}

void CGPRepeatedFieldSet(CGPRepeatedField *field, jint index, id value, CGPFieldJavaType type) {
  CGPRepeatedFieldCheckBounds(field, index);

#define REPEATED_SET_CASE(NAME)                                      \
  {                                                                  \
    TYPE_##NAME *ptr = &((TYPE_##NAME *)field->data->buffer)[index]; \
    TYPE_ASSIGN_##NAME(ptr, CGPFromReflectionType##NAME(value));     \
  }

  SWITCH_TYPES(type, REPEATED_SET_CASE)

#undef REPEATED_SET_CASE
}

// Make sure to reserve enough space in the buffer BEFORE calling this.
static void CGPRepeatedFieldAddUnsafe(CGPRepeatedFieldData *data, id value, CGPFieldJavaType type) {
#define REPEATED_ADD_CASE(NAME)                                      \
  {                                                                  \
    TYPE_##NAME *ptr = &((TYPE_##NAME *)data->buffer)[data->size++]; \
    TYPE_ASSIGN_##NAME(ptr, CGPFromReflectionType##NAME(value));     \
  }

  SWITCH_TYPES(type, REPEATED_ADD_CASE)

#undef REPEATED_ADD_CASE
}

void CGPRepeatedFieldAdd(CGPRepeatedField *field, id value, CGPFieldJavaType type) {
  CGPRepeatedFieldReserveAdditionalCapacity(field, 1, CGPGetTypeSize(type));
  CGPRepeatedFieldAddUnsafe(field->data, value, type);
}

void CGPRepeatedFieldAssignFromList(CGPRepeatedField *field, id<JavaUtilList> list,
                                    CGPFieldJavaType type) {
  CGPRepeatedFieldClear(field, type);

  jint listSize = [list size];
  if (listSize > 0) {
    CGPRepeatedFieldReserve(field, (uint32_t)listSize, CGPGetTypeSize(type));
    for (id elem in list) {
      CGPRepeatedFieldAddUnsafe(field->data, elem, type);
    }
  }
}

id<JavaUtilList> CGPRepeatedFieldCopyList(CGPRepeatedField *field, CGPFieldDescriptor *descriptor) {
  id<JavaUtilList> newList =
      AUTORELEASE([[JavaUtilArrayList alloc] initWithInt:CGPRepeatedFieldSize(field)]);
  CGPRepeatedFieldData *data = field->data;
  if (data == NULL) {
    return newList;
  }

#define REPEATED_COPY_ELEM_CASE(NAME)                                                \
  for (uint32_t i = 0; i < data->size; i++) {                                        \
    [newList addWithId:CGPToReflectionType##NAME(((TYPE_##NAME *)data->buffer)[i])]; \
  }

  SWITCH_TYPES(CGPFieldGetJavaType(descriptor), REPEATED_COPY_ELEM_CASE)

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
@interface CGPRepeatedStringFieldList : CGPRepeatedFieldList <ComGoogleProtobufProtocolStringList>
@end

@interface CGPStringAsByteStringList : JavaUtilAbstractList {
 @package
  CGPRepeatedField field_;
}
@end

@interface CGPRepeatedEnumValueFieldList : JavaUtilAbstractList {
 @package
  CGPRepeatedField field_;
}
@end

@implementation CGPRepeatedEnumValueFieldList

- (id)getWithInt:(jint)index {
  CGPRepeatedFieldCheckBounds(&field_, index);
  ComGoogleProtobufDescriptors_EnumValueDescriptor *valueDesc =
      ((ComGoogleProtobufDescriptors_EnumValueDescriptor **)field_.data->buffer)[index];
  jint valueNum = valueDesc != nil ? [valueDesc getNumber] : 0;
  return [JavaLangInteger valueOfWithInt:valueNum];
}

- (jint)size {
  return CGPRepeatedFieldSize(&field_);
}

- (void)dealloc {
  ReleaseData(field_.data, ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_ENUM);
  [super dealloc];
}

@end

id<JavaUtilList> CGPNewRepeatedEnumValueFieldList(CGPRepeatedField *field) {
  CGPRepeatedEnumValueFieldList *list = [[CGPRepeatedEnumValueFieldList alloc] init];
  CGPRepeatedFieldData *data = field->data;
  if (data != NULL) {
    list->field_.data = data;
    __c11_atomic_fetch_add(&data->ref_count, 1, __ATOMIC_RELAXED);
  }
  return list;
}

id<JavaUtilList> CGPNewRepeatedFieldList(CGPRepeatedField *field, CGPFieldJavaType type) {
  CGPRepeatedFieldList *list =
      type == ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_STRING
          ? [[CGPRepeatedStringFieldList alloc] init]
          : [[CGPRepeatedFieldList alloc] init];
  CGPRepeatedFieldData *data = field->data;
  if (data != NULL) {
    list->field_.data = data;
    __c11_atomic_fetch_add(&data->ref_count, 1, __ATOMIC_RELAXED);
  }
  list->type_ = type;
  return list;
}

NSArray *CGPNewRepeatedFieldArray(CGPRepeatedField *field, CGPFieldJavaType type) {
  // Currently, we just copy the list to a NSArray. Alternatively, we could create a NSArray
  // subclass that behaves similar to the local list implementation here.
  id<JavaUtilList> list = CGPNewRepeatedFieldList(field, type);
  NSMutableArray *result = [[NSMutableArray alloc] init];
  for (id object in list) {
    [result addObject:object];
  }
  RELEASE_(list);
  return result;
}

@implementation CGPRepeatedFieldList

- (id)getWithInt:(jint)index {
  CGPRepeatedFieldCheckBounds(&field_, index);

#define REPEATED_LIST_GET_CASE(NAME) \
  return ToExternalRepeated##NAME(((TYPE_##NAME *)field_.data->buffer)[index]);

  SWITCH_TYPES(type_, REPEATED_LIST_GET_CASE)

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
