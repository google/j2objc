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

#include <libkern/OSAtomic.h>

#import "com/google/protobuf/Descriptors_PackagePrivate.h"
#import "java/lang/IndexOutOfBoundsException.h"
#import "java/util/ArrayList.h"

#define MIN_REPEATED_FIELD_SIZE 4

void CGPRepeatedFieldReserve(CGPRepeatedField *field, uint32_t new_size, size_t elemSize) {
  CGPRepeatedFieldData *data = field->data;
  if (data == NULL) {
    data = field->data = calloc(sizeof(CGPRepeatedFieldData), 1);
  }

  if (data->total_size >= new_size) {
    return;
  }

  int32_t newTotalSize = MAX(MIN_REPEATED_FIELD_SIZE, MAX(data->total_size * 2, new_size));
  data->total_size = newTotalSize;
  data->buffer = realloc(data->buffer, newTotalSize * elemSize);
}

void CGPRepeatedFieldCopyData(CGPRepeatedField *field, CGPFieldJavaType type) {
  CGPRepeatedFieldData *oldData = field->data;
  if (oldData == NULL) {
    return;
  }

  size_t elemSize = CGPGetTypeSize(type);
  CGPRepeatedFieldData *newData = calloc(sizeof(CGPRepeatedFieldData), 1);
  newData->size = newData->total_size = oldData->size;
  field->data = newData;
  newData->buffer = malloc(newData->size * elemSize);
  memcpy(newData->buffer, oldData->buffer, newData->size * elemSize);

  if (CGPIsRetainedType(type)) {
    for (uint32_t i = 0; i < newData->size; i++) {
      [((id *)newData->buffer)[i] retain];
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
      [((id *)newBuffer)[i] retain];
    }
  }
}

static void ReleaseData(CGPRepeatedFieldData *data, CGPFieldJavaType type) {
  if (data == NULL) {
    return;
  }

  while (YES) {
    uint32_t local_count = data->ref_count;
    if (local_count > 0) {
      if (OSAtomicCompareAndSwap32Barrier(
          (int32_t)local_count, (int32_t)local_count - 1, (int32_t *)&data->ref_count)) {
        return;
      }
    } else {
      break;
    }
  }

  if (CGPIsRetainedType(type)) {
    for (uint32_t i = 0; i < data->size; i++) {
      [((id *)data->buffer)[i] release];
    }
  }

  free(data->buffer);
  free(data);
}

void CGPRepeatedFieldClear(CGPRepeatedField *field, CGPFieldJavaType type) {
  ReleaseData(field->data, type);
  field->data = NULL;
}

id CGPRepeatedFieldGet(CGPRepeatedField *field, int index, CGPFieldDescriptor *descriptor) {
  CGPRepeatedFieldCheckBounds(field, index);

#define REPEATED_GET_CASE(NAME) \
  return CGPToReflectionType##NAME(((TYPE_##NAME *)field->data->buffer)[index], descriptor); \

  SWITCH_TYPES_WITH_ENUM(CGPFieldGetJavaType(descriptor), REPEATED_GET_CASE)

#undef REPEATED_GET_CASE
}

void CGPRepeatedFieldSet(CGPRepeatedField *field, int index, id value, CGPFieldJavaType type) {
  CGPRepeatedFieldCheckBounds(field, index);

#define REPEATED_GET_CASE(NAME) \
  { \
    TYPE_##NAME *ptr = &((TYPE_##NAME *)field->data->buffer)[index]; \
    TYPE_ASSIGN_##NAME(*ptr, CGPFromReflectionType##NAME(value)); \
    break; \
  }

  SWITCH_TYPES_WITH_ENUM(type, REPEATED_GET_CASE)

#undef REPEATED_GET_CASE
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
  int32_t total_size = CGPRepeatedFieldTotalSize(field);
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
      [[[JavaUtilArrayList alloc] initWithInt:CGPRepeatedFieldSize(field)] autorelease];
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
  int32_t aSize = CGPRepeatedFieldSize(a);
  int32_t bSize = CGPRepeatedFieldSize(b);
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

void CGPRepeatedFieldOutOfBounds(int idx, uint32_t size) {
  @throw [[[JavaLangIndexOutOfBoundsException alloc] initWithNSString:
      [NSString stringWithFormat:@"Repeated field index out-of-bounds. (index = %d, size = %d)",
          idx, (int)size]] autorelease];
}

CGPRepeatedFieldList *CGPNewRepeatedFieldList(CGPRepeatedField *field, CGPFieldJavaType type) {
  CGPRepeatedFieldList *list = [[CGPRepeatedFieldList alloc] init];
  CGPRepeatedFieldData *data = field->data;
  if (data != NULL) {
    list->field_.data = data;
    OSAtomicIncrement32((int32_t *)&data->ref_count);
  }
  list->type_ = type;
  return list;
}

@implementation CGPRepeatedFieldList

- (id)getWithInt:(int)index {
  CGPRepeatedFieldCheckBounds(&field_, index);

#define REPEATED_LIST_GET_CASE(NAME) \
  return CGPBoxedValue##NAME(((TYPE_##NAME *)field_.data->buffer)[index]); \

  SWITCH_TYPES_NO_ENUM(type_, REPEATED_LIST_GET_CASE)

#undef REPEATED_LIST_GET_CASE
}

- (int)size {
  return CGPRepeatedFieldSize(&field_);
}

- (void)dealloc {
  ReleaseData(field_.data, type_);
  [super dealloc];
}

@end
