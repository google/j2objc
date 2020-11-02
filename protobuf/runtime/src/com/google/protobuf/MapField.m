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

#include "com/google/protobuf/MapField.h"

#include "J2ObjC_source.h"
#include "com/google/protobuf/Descriptors_PackagePrivate.h"
#include "com/google/protobuf/MapEntry.h"
#include "com/google/protobuf/ProtocolMessageEnum.h"
#include "java/lang/IndexOutOfBoundsException.h"
#include "java/util/AbstractMap.h"
#include "java/util/AbstractSet.h"
#include "java/util/ArrayList.h"
#include "java/util/ConcurrentModificationException.h"
#include "java/util/Iterator.h"
#include "java/util/NoSuchElementException.h"

#define MIN_HASH_CAPACITY 16
#define HASH_LOAD_FACTOR 0.75f

#define HASH_IDX(hash, data) (hash & (data->hashCapacity - 1))

static uint32_t Hash0(CGPValue value, CGPFieldJavaType type) {
#define HASH_CASE(NAME) return HASH_##NAME(value.CGPValueField_##NAME);

SWITCH_TYPES_NO_ENUM(type, HASH_CASE)

#undef HASH_CASE
}

static uint32_t Hash(CGPValue key, CGPFieldJavaType keyType) {
  uint32_t h = Hash0(key, keyType);

  // Variant of single-word Wang/Jenkins hash copied from sun.misc.Hashing.
  h += (h <<  15) ^ 0xffffcd7d;
  h ^= (h >> 10);
  h += (h << 3);
  h ^= (h >> 6);
  h += (h << 2) + (h << 14);
  return h ^ (h >> 16);
}

static bool Equals(CGPValue a, CGPValue b, CGPFieldJavaType type) {
  switch (type) {
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_INT:
      return a.valueInt == b.valueInt;
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_LONG:
      return a.valueLong == b.valueLong;
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_BOOLEAN:
      return a.valueBool == b.valueBool;
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_ENUM:
      return a.valueId == b.valueId;
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_FLOAT:
      return a.valueFloat == b.valueFloat;
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_DOUBLE:
      return a.valueDouble == b.valueDouble;
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_BYTE_STRING:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_STRING:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_MESSAGE:
      return [a.valueId isEqual:b.valueId];
  }
}

static CGPMapFieldData *NewData() {
  CGPMapFieldData *data = calloc(sizeof(CGPMapFieldData), 1 );
  __c11_atomic_store(&data->refCount, 1, __ATOMIC_RELAXED);
  data->validArray = true;
  data->validHashMap = true;
  data->header.next = &data->header;
  data->header.prev = &data->header;
  return data;
}

// The passed in key and value must already have an incremented retain count if they have a
// retainable type.
static CGPMapFieldEntry *NewEntryConsuming(CGPValue key, CGPValue value, uint32_t hash) {
  CGPMapFieldEntry *entry = malloc(sizeof(CGPMapFieldEntry));
  entry->key = key;
  entry->value = value;
  entry->hash = hash;
  return entry;
}

static CGPMapFieldEntry *NewEntry(
    CGPValue key, CGPFieldJavaType keyType, CGPValue value, CGPFieldJavaType valueType,
    uint32_t hash) {
  if (CGPIsRetainedType(keyType)) {
    RETAIN_(key.valueId);
  }
  if (CGPIsRetainedType(valueType)) {
    RETAIN_(value.valueId);
  }
  return NewEntryConsuming(key, value, hash);
}

static void ReleaseEntry(
    CGPMapFieldEntry *entry, CGPFieldJavaType keyType, CGPFieldJavaType valueType) {
  if (CGPIsRetainedType(keyType)) {
    RELEASE_(entry->key.valueId);
  }
  if (CGPIsRetainedType(valueType)) {
    RELEASE_(entry->value.valueId);
  }
  free(entry);
}

static void MoveArray(CGPMapFieldEntry **from, CGPMapFieldEntry **to, uint32_t len) {
  for (uint32_t i = 0; i < len; i++) {
    to[i] = from[i];
  }
}

static void PutInHashArray(CGPMapFieldData *data, CGPMapFieldEntry *entry) {
  uint32_t hashIdx = HASH_IDX(entry->hash, data);
  entry->hashNext = data->hashArray[hashIdx];
  data->hashArray[hashIdx] = entry;
}

static CGPMapFieldEntry *GetFromHashArray(
    CGPMapFieldData *data, CGPValue key, CGPFieldJavaType keyType, uint32_t hash) {
  CGPMapFieldEntry *entry = data->hashArray[HASH_IDX(hash, data)];
  while (entry) {
    if (Equals(key, entry->key, keyType)) {
      break;
    }
    entry = entry->hashNext;
  }
  return entry;
}

static void AddToHashMap(CGPMapFieldData *data, CGPMapFieldEntry *entry) {
  entry->next = &data->header;
  entry->prev = data->header.prev;
  entry->prev->next = entry;
  data->header.prev = entry;
  PutInHashArray(data, entry);
  data->numEntries++;
}

static void RemoveFromHashArray(CGPMapFieldData *data, CGPMapFieldEntry *entry) {
  CGPMapFieldEntry **current = &data->hashArray[HASH_IDX(entry->hash, data)];
  while (*current != NULL) {
    if (*current == entry) {
      *current = entry->hashNext;
      break;
    }
    current = &(*current)->hashNext;
  }
}

static void CopyHashEntries(
    CGPMapFieldData *fromData, CGPMapFieldData *toData, CGPFieldJavaType keyType,
    CGPFieldJavaType valueType) {
  CGPMapFieldEntry *current = fromData->header.next;
  while (current != &fromData->header) {
    AddToHashMap(toData, NewEntry(current->key, keyType, current->value, valueType, current->hash));
    current = current->next;
  }
  toData->validArray = false;
}

static void NewArray(CGPMapFieldData *data, uint32_t newArraySize, uint32_t newHashCapacity) {
  uint32_t newBufferSize = newArraySize + newHashCapacity;
  CGPMapFieldEntry **newArray = calloc(sizeof(CGPMapFieldEntry *), newBufferSize);
  data->array = newArray;
  data->arrayCapacity = newArraySize;
  data->hashArray = newArray + newArraySize;
  data->hashCapacity = newHashCapacity;
}

static void Grow(CGPMapFieldData *data, uint32_t minSize) {
  uint32_t newHashCapacity = MAX(data->hashCapacity << 1, MIN_HASH_CAPACITY);
  uint32_t newArraySize = newHashCapacity * HASH_LOAD_FACTOR;
  while (newArraySize < minSize) {
    newHashCapacity <<= 1;
    newArraySize = newHashCapacity * HASH_LOAD_FACTOR;
  }
  NewArray(data, newArraySize, newHashCapacity);
}

static void GrowAsMap(CGPMapFieldData *data, uint32_t minSize) {
  CGPMapFieldEntry **oldArray = data->array;
  Grow(data, minSize);
  free(oldArray);
  CGPMapFieldEntry *current = data->header.next;
  while (current != &data->header) {
    PutInHashArray(data, current);
    current = current->next;
  }
}

static void GrowAsArray(CGPMapFieldData *data, uint32_t minSize) {
  CGPMapFieldEntry **oldArray = data->array;
  Grow(data, minSize);
  MoveArray(oldArray, data->array, data->numEntries);
  free(oldArray);
}

static void EnsureValidArray(CGPMapFieldData *data) {
  if (data->validArray) {
    return;
  }

  uint32_t i = 0;
  CGPMapFieldEntry *current = data->header.next;
  while (current != &data->header) {
    data->array[i++] = current;
    current = current->next;
  }
  data->validArray = true;
}

static void EnsureValidMap(
    CGPMapFieldData *data, CGPFieldJavaType keyType, CGPFieldJavaType valueType) {
  if (data->validHashMap) {
    return;
  }

  uint32_t arraySize = data->numEntries;
  // Clear the hash array.
  memset(data->hashArray, 0, sizeof(CGPMapFieldEntry *) * data->hashCapacity);
  data->header.next = &data->header;
  data->header.prev = &data->header;
  data->numEntries = 0;

  BOOL keyTypeIsRetainable = CGPIsRetainedType(keyType);
  BOOL valueTypeIsRetainable = CGPIsRetainedType(valueType);
  // Construct the hash map from the array.
  for (uint32_t i = 0; i < arraySize; i++) {
    CGPMapFieldEntry *entry = data->array[i];
    CGPMapFieldEntry *existingEntry = GetFromHashArray(data, entry->key, keyType, entry->hash);
    if (existingEntry != NULL) {
      // The array had multiple entries with the same key. We keep the insertion order from the
      // first key but replace the value.
      if (keyTypeIsRetainable) {
        [entry->key.valueId autorelease];
      }
      if (valueTypeIsRetainable) {
        [existingEntry->value.valueId autorelease];
      }
      existingEntry->value = entry->value;
      free(entry);
      data->validArray = false;  // We just freed an entry in the array.
    } else {
      AddToHashMap(data, entry);
    }
  }
  data->validHashMap = true;
}

void CGPMapFieldEnsureValidMap(
    CGPMapFieldData *data, CGPFieldJavaType keyType, CGPFieldJavaType valueType) {
  EnsureValidMap(data, keyType, valueType);
}

static void EnsureAdditionalListCapacity(CGPMapField *field, uint32_t additionalEntries) {
  CGPMapFieldData *data = field->data;
  if (data == NULL) {
    data = field->data = NewData();
  } else {
    EnsureValidArray(data);
  }
  uint32_t totalEntries = data->numEntries + additionalEntries;
  if (data->arrayCapacity < totalEntries) {
    GrowAsArray(data, totalEntries);
  }
  data->validHashMap = false;
}

static void EnsureAdditionalHashMapCapacity(
    CGPMapField *field, uint32_t additionalEntries, CGPFieldJavaType keyType,
    CGPFieldJavaType valueType) {
  CGPMapFieldData *data = field->data;
  if (data == NULL) {
    data = field->data = NewData();
  } else {
    EnsureValidMap(data, keyType, valueType);
  }
  uint32_t totalEntries = data->numEntries + additionalEntries;
  if (data->arrayCapacity < totalEntries) {
    GrowAsMap(data, totalEntries);
  }
  data->validArray = false;
}

CGPMapFieldEntry *CGPMapFieldGetWithKey(
    CGPMapField *field, CGPValue key, CGPFieldJavaType keyType, CGPFieldJavaType valueType) {
  CGPMapFieldData *data = field->data;
  if (data == NULL) {
    return NULL;
  }
  EnsureValidMap(data, keyType, valueType);
  return GetFromHashArray(data, key, keyType, Hash(key, keyType));
}

void CGPMapFieldPut(
    CGPMapField *field, CGPValue key, CGPFieldJavaType keyType, CGPValue value,
    CGPFieldJavaType valueType, bool retainedKeyAndValue) {
  BOOL keyTypeIsRetainable = CGPIsRetainedType(keyType);
  BOOL valueTypeIsRetainable = CGPIsRetainedType(valueType);
  // The value is always added to the map so make sure it's retained.
  if (valueTypeIsRetainable && !retainedKeyAndValue) {
    RETAIN_(value.valueId);
  }
  uint32_t hash = Hash(key, keyType);
  CGPMapFieldEntry *entry = NULL;
  CGPMapFieldData *data = field->data;
  if (data != NULL) {
    EnsureValidMap(data, keyType, valueType);
    entry = GetFromHashArray(data, key, keyType, hash);
  }
  if (entry) {
    // Existing entry so the key is not added to the map and must not be retained.
    if (keyTypeIsRetainable && retainedKeyAndValue) {
      [key.valueId autorelease];
    }
    // Release the previous value.
    if (valueTypeIsRetainable) {
      [entry->value.valueId autorelease];
    }
    entry->value = value;
  } else {
    // Creating a new entry using the passed in key which must be retained.
    if (keyTypeIsRetainable && !retainedKeyAndValue) {
      RETAIN_(key.valueId);
    }
    EnsureAdditionalHashMapCapacity(field, 1, keyType, valueType);
    data = field->data;
    entry = NewEntryConsuming(key, value, hash);
    AddToHashMap(data, entry);
  }
  data->modCount++;
}

void CGPMapFieldRemove(
    CGPMapField *field, CGPValue key, CGPFieldJavaType keyType, CGPFieldJavaType valueType) {
  CGPMapFieldEntry *entry = CGPMapFieldGetWithKey(field, key, keyType, valueType);
  if (entry == NULL) {
    return;
  }
  CGPMapFieldData *data = field->data;
  RemoveFromHashArray(data, entry);
  entry->prev->next = entry->next;
  entry->next->prev = entry->prev;
  ReleaseEntry(entry, keyType, valueType);
  data->numEntries--;
  data->validArray = false;
  data->modCount++;
}

void CGPMapFieldCopyData(CGPMapField *field, CGPFieldJavaType keyType, CGPFieldJavaType valueType) {
  CGPMapFieldData *oldData = field->data;
  if (oldData == NULL) {
    return;
  }

  EnsureValidMap(oldData, keyType, valueType);
  CGPMapFieldData *newData = field->data = NewData();
  NewArray(newData, oldData->arrayCapacity, oldData->hashCapacity);
  CopyHashEntries(oldData, newData, keyType, valueType);
}

void CGPMapFieldAppendOther(
    CGPMapField *field, CGPMapField *other, CGPFieldJavaType keyType, CGPFieldJavaType valueType) {
  uint32_t otherSize = CGPMapFieldMapSize(other, keyType, valueType);
  if (otherSize == 0) {
    return;
  }
  CGPMapFieldData *otherData = other->data;
  EnsureAdditionalHashMapCapacity(field, otherSize, keyType, valueType);
  CGPMapFieldData *data = field->data;
  CopyHashEntries(otherData, data, keyType, valueType);
  data->modCount++;
}

static void ReleaseAllEntries(
    CGPMapFieldData *data, CGPFieldJavaType keyType, CGPFieldJavaType valueType) {
  if (data->validArray) {
    for (uint32_t i = 0; i < data->numEntries; i++) {
      ReleaseEntry(data->array[i], keyType, valueType);
    }
  } else {  // validHashMap implied
    CGPMapFieldEntry *current = data->header.next;
    while (current != &data->header) {
      CGPMapFieldEntry *next = current->next;
      ReleaseEntry(current, keyType, valueType);
      current = next;
    }
  }
}

static void ReleaseData(
    CGPMapFieldData *data, CGPFieldJavaType keyType, CGPFieldJavaType valueType) {
  if (data == NULL) {
    return;
  }

  if (__c11_atomic_fetch_sub(&data->refCount, 1, __ATOMIC_RELEASE) == 1) {
    __c11_atomic_thread_fence(__ATOMIC_ACQUIRE);

    ReleaseAllEntries(data, keyType, valueType);

    // Don't need to free hashArray because it is on the same allocation as array.
    free(data->array);
    free(data);
  }
}

void CGPMapFieldClear(CGPMapField *field, CGPFieldJavaType keyType, CGPFieldJavaType valueType) {
  CGPMapFieldData *data = field->data;
  if (data == NULL) {
    return;
  }
  ReleaseAllEntries(data, keyType, valueType);
  data->numEntries = 0;
  data->modCount++;
  data->validArray = true;
  // The hash array and header are now dirty.
  data->validHashMap = false;
}

bool CGPMapFieldIsEqual(
    CGPMapField *fieldA, CGPMapField *fieldB, CGPFieldJavaType keyType,
    CGPFieldJavaType valueType) {
  uint32_t sizeA = CGPMapFieldMapSize(fieldA, keyType, valueType);
  uint32_t sizeB = CGPMapFieldMapSize(fieldB, keyType, valueType);
  if (sizeA != sizeB) {
    return false;
  }
  if (sizeA == 0) {
    return true;
  }
  CGPMapFieldData *dataA = fieldA->data;
  CGPMapFieldData *dataB = fieldB->data;

  CGPMapFieldEntry *current = dataA->header.next;
  while (current != &dataA->header) {
    CGPValue key = current->key;
    CGPMapFieldEntry *entryB = GetFromHashArray(dataB, key, keyType, current->hash);
    if (entryB == NULL || !Equals(current->value, entryB->value, valueType)) {
      return false;
    }
    current = current->next;
  }
  return true;
}

int CGPMapFieldHash(CGPMapField *field, CGPFieldJavaType keyType, CGPFieldJavaType valueType) {
  int hash = 0;
  CGPMapFieldData *data = field->data;
  if (data == NULL) {
    return hash;
  }

  EnsureValidMap(data, keyType, valueType);
  CGPMapFieldEntry *current = data->header.next;
  while (current != &data->header) {
    hash += current->hash ^ Hash(current->value, valueType);
    current = current->next;
  }

  return hash;
}

static id BoxedValue(CGPValue value, CGPFieldJavaType type) {
  switch (type) {
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_INT:
      return JavaLangInteger_valueOfWithInt_(value.valueInt);
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_LONG:
      return JavaLangLong_valueOfWithLong_(value.valueLong);
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_FLOAT:
      return JavaLangFloat_valueOfWithFloat_(value.valueFloat);
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_DOUBLE:
      return JavaLangDouble_valueOfWithDouble_(value.valueDouble);
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_BOOLEAN:
      return JavaLangBoolean_valueOfWithBoolean_(value.valueBool);
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_ENUM:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_STRING:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_BYTE_STRING:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_MESSAGE:
      return value.valueId;
  }
}

static CGPValue UnboxValue(id value, CGPFieldJavaType type) {
  CGPValue result;
  switch (type) {
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_INT:
      result.valueInt = [value intValue];
      break;
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_LONG:
      result.valueLong = [value longLongValue];
      break;
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_FLOAT:
      result.valueFloat = [value floatValue];
      break;
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_DOUBLE:
      result.valueDouble = [value doubleValue];
      break;
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_BOOLEAN:
      result.valueBool = [value booleanValue];
      break;
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_ENUM:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_STRING:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_BYTE_STRING:
    case ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_MESSAGE:
      result.valueId = value;
      break;
  }
  return result;
}

// Unlike CGPBoxedValue##NAME and CGPToReflectionType##NAME, maps store enum values as
// java.lang.Integer using the enum number (not ordinal).
static id BoxedReflectionValue(CGPValue value, CGPFieldJavaType type) {
  if (type == ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_ENUM) {
    return JavaLangInteger_valueOfWithInt_(
        [(id<ComGoogleProtobufProtocolMessageEnum>)value.valueId getNumber]);
  }
  return BoxedValue(value, type);
}

static CGPValue UnboxReflectionValue(id value, CGPFieldDescriptor *field) {
  CGPFieldJavaType type = CGPFieldGetJavaType(field);
  if (type == ComGoogleProtobufDescriptors_FieldDescriptor_JavaType_Enum_ENUM) {
    CGPEnumValueDescriptor *valueDesc =
        CGPEnumValueDescriptorFromInt(field->valueType_, [value intValue]);
    CGPValue result;
    result.valueId = valueDesc ? valueDesc->enum_ : nil;
    return result;
  }
  return UnboxValue(value, type);
}

ComGoogleProtobufMapEntry *CreateReflectionMapEntry(
    CGPMapFieldEntry *entry, CGPFieldJavaType keyType, CGPFieldJavaType valueType) {
  id key = BoxedReflectionValue(entry->key, keyType);
  id value = BoxedReflectionValue(entry->value, valueType);
  return [[[ComGoogleProtobufMapEntry alloc] initWithKey:key value:value] autorelease];
}

id<JavaUtilList> CGPMapFieldCopyList(
    CGPMapField *field, CGPFieldJavaType keyType, CGPFieldJavaType valueType) {
  id<JavaUtilList> newList = create_JavaUtilArrayList_initWithInt_(CGPMapFieldListSize(field));
  CGPMapFieldData *data = field->data;
  if (data == NULL) {
    return newList;
  }

  EnsureValidArray(data);
  for (uint32_t i = 0; i < data->numEntries; i++) {
    [newList addWithId:CreateReflectionMapEntry(data->array[i], keyType, valueType)];
  }
  return newList;
}

static void CheckArrayBounds(CGPMapField *field, jint idx) {
  uint32_t size = CGPMapFieldListSize(field);
  if (idx < 0 || size <= (uint32_t)idx) {
    @throw create_JavaLangIndexOutOfBoundsException_initWithNSString_([NSString stringWithFormat:
        @"Map field index out-of-bounds. (index = %d, size = %d", idx, size]);
  }
}

id CGPMapFieldGetAtIndex(CGPMapField *field, jint idx, CGPFieldDescriptor *descriptor) {
  CheckArrayBounds(field, idx);
  CGPMapFieldData *data = field->data;
  EnsureValidArray(data);
  CGPFieldJavaType keyType = CGPFieldGetJavaType(CGPFieldMapKey(descriptor));
  CGPFieldJavaType valueType = CGPFieldGetJavaType(CGPFieldMapValue(descriptor));
  return CreateReflectionMapEntry(data->array[idx], keyType, valueType);
}

void CGPMapFieldAdd(CGPMapField *field, id object, CGPFieldDescriptor *descriptor) {
  ComGoogleProtobufMapEntry *mapEntry =
      (ComGoogleProtobufMapEntry *)cast_chk(object, [ComGoogleProtobufMapEntry class]);
  EnsureAdditionalListCapacity(field, 1);
  CGPMapFieldData *data = field->data;
  CGPFieldDescriptor *keyField = CGPFieldMapKey(descriptor);
  CGPFieldDescriptor *valueField = CGPFieldMapValue(descriptor);
  CGPFieldJavaType keyType = CGPFieldGetJavaType(keyField);
  CGPFieldJavaType valueType = CGPFieldGetJavaType(valueField);
  CGPValue key = UnboxReflectionValue([mapEntry getKey], keyField);
  CGPValue value = UnboxReflectionValue([mapEntry getValue], valueField);
  data->array[data->numEntries++] = NewEntry(key, keyType, value, valueType, Hash(key, keyType));
  data->modCount++;
}

void CGPMapFieldSet(CGPMapField *field, jint idx, id object, CGPFieldDescriptor *descriptor) {
  CheckArrayBounds(field, idx);
  CGPMapFieldData *data = field->data;
  ComGoogleProtobufMapEntry *mapEntry =
      (ComGoogleProtobufMapEntry *)cast_chk(object, [ComGoogleProtobufMapEntry class]);
  EnsureValidArray(data);
  data->validHashMap = false;
  CGPFieldDescriptor *keyField = CGPFieldMapKey(descriptor);
  CGPFieldDescriptor *valueField = CGPFieldMapValue(descriptor);
  CGPFieldJavaType keyType = CGPFieldGetJavaType(keyField);
  CGPFieldJavaType valueType = CGPFieldGetJavaType(valueField);
  CGPValue key = UnboxReflectionValue([mapEntry getKey], keyField);
  CGPValue value = UnboxReflectionValue([mapEntry getValue], valueField);
  ReleaseEntry(data->array[idx], keyType, valueType);
  data->array[idx] = NewEntry(key, keyType, value, valueType, Hash(key, keyType));
  data->modCount++;
}

void CGPMapFieldAssignFromList(
    CGPMapField *field, id<JavaUtilList> list, CGPFieldDescriptor *descriptor) {
  CGPFieldDescriptor *keyField = CGPFieldMapKey(descriptor);
  CGPFieldDescriptor *valueField = CGPFieldMapValue(descriptor);
  CGPFieldJavaType keyType = CGPFieldGetJavaType(keyField);
  CGPFieldJavaType valueType = CGPFieldGetJavaType(valueField);
  CGPMapFieldClear(field, keyType, valueType);
  EnsureAdditionalListCapacity(field, [list size]);
  CGPMapFieldData *data = field->data;

  for (id obj in list) {
    ComGoogleProtobufMapEntry *mapEntry =
        (ComGoogleProtobufMapEntry *)cast_chk(obj, [ComGoogleProtobufMapEntry class]);
    CGPValue key = UnboxReflectionValue([mapEntry getKey], keyField);
    CGPValue value = UnboxReflectionValue([mapEntry getValue], valueField);
    data->array[data->numEntries++] = NewEntry(key, keyType, value, valueType, Hash(key, keyType));
  }
  data->modCount++;
}

@interface CGPMapFieldMap : JavaUtilAbstractMap {
 @package
  CGPMapField field_;
  CGPFieldJavaType keyType_;
  CGPFieldJavaType valueType_;
}
@end

id<JavaUtilMap> CGPMapFieldAsJavaMap(
    CGPMapField *field, CGPFieldJavaType keyType, CGPFieldJavaType valueType) {
  CGPMapFieldMap *map = [[[CGPMapFieldMap alloc] init] autorelease];
  CGPMapFieldData *data = field->data;
  if (data != NULL) {
    map->field_.data = data;
    __c11_atomic_fetch_add(&data->refCount, 1, __ATOMIC_RELAXED);
  }
  map->keyType_ = keyType;
  map->valueType_ = valueType;
  return map;
}

@interface CGPMapFieldEntrySet : JavaUtilAbstractSet {
 @package
  CGPMapFieldMap *map_;
}
@end

@interface CGPMapFieldEntrySetIterator : NSObject < JavaUtilIterator > {
 @package
  CGPMapFieldMap *map_;
  CGPMapFieldEntry *nextEntry_;
  uint32_t expectedModCount_;
}
@end

@implementation CGPMapFieldMap

- (id<JavaUtilSet>)entrySet {
  CGPMapFieldEntrySet *entrySet = [[[CGPMapFieldEntrySet alloc] init] autorelease];
  entrySet->map_ = RETAIN_(self);
  return entrySet;
}

- (jint)size {
  return CGPMapFieldMapSize(&field_, keyType_, valueType_);
}

- (jboolean)containsKeyWithId:(id)pKey {
  CGPValue key = UnboxValue(pKey, keyType_);
  return CGPMapFieldGetWithKey(&field_, key, keyType_, valueType_) != NULL;
}

- (id)getWithId:(id)pKey {
  CGPValue key = UnboxValue(pKey, keyType_);
  CGPMapFieldEntry *entry = CGPMapFieldGetWithKey(&field_, key, keyType_, valueType_);
  if (entry != NULL) {
    return BoxedValue(entry->value, valueType_);
  }
  return nil;
}

- (void)dealloc {
  ReleaseData(field_.data, keyType_, valueType_);
  [super dealloc];
}

@end

@implementation CGPMapFieldEntrySet

- (jint)size {
  return CGPMapFieldMapSize(&map_->field_, map_->keyType_, map_->valueType_);
}

- (id<JavaUtilIterator>)iterator {
  // Start iteration as a valid array. The iterator will throw ConcurrentModification if the array
  // becomes invalid.
  CGPMapFieldData *data = map_->field_.data;
  CGPMapFieldEntrySetIterator *iterator = [[[CGPMapFieldEntrySetIterator alloc] init] autorelease];
  iterator->map_ = RETAIN_(map_);
  if (data != NULL) {
    EnsureValidMap(data, map_->keyType_, map_->valueType_);
    iterator->nextEntry_ = data->header.next;
    iterator->expectedModCount_ = data->modCount;
  }
  return iterator;
}

- (void)dealloc {
  RELEASE_(map_);
  [super dealloc];
}

@end

@implementation CGPMapFieldEntrySetIterator

- (jboolean)hasNext {
  return nextEntry_ != NULL;
}

- (id<JavaUtilMap_Entry>)next {
  if (nextEntry_ == NULL) {
    @throw create_JavaUtilNoSuchElementException_init();
  }
  if (map_->field_.data->modCount != expectedModCount_) {
    @throw create_JavaUtilConcurrentModificationException_init();
  }
  CGPMapFieldEntry *entry = nextEntry_;
  nextEntry_ = entry->next;
  if (nextEntry_ == &map_->field_.data->header) {
    nextEntry_ = NULL;
  }
  return create_JavaUtilAbstractMap_SimpleImmutableEntry_initWithId_withId_(
    BoxedValue(entry->key, map_->keyType_), BoxedValue(entry->value, map_->valueType_));
}

- (void)remove {
  // Default method impl.
  JavaUtilIterator_remove(self);
}

- (void)forEachRemainingWithJavaUtilFunctionConsumer:(id<JavaUtilFunctionConsumer>)action {
  // Default method impl.
  JavaUtilIterator_forEachRemainingWithJavaUtilFunctionConsumer_(self, action);
}

- (void)dealloc {
  RELEASE_(map_);
  [super dealloc];
}

@end
