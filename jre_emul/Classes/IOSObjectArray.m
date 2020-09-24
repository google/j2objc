// Copyright 2011 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

//
//  IOSObjectArray.m
//  JreEmulation
//
//  Created by Tom Ball on 9/9/11.
//

#if __has_feature(objc_arc)
#error "b/158196682: IOSObjectArray cannot be built with ARC"
#endif

#import "IOSObjectArray.h"

#import "IOSArray_PackagePrivate.h"
#import "IOSClass.h"
#import "java/lang/ArrayStoreException.h"
#import "java/lang/AssertionError.h"
#import "java/lang/NegativeArraySizeException.h"

// Defined in IOSArray.m
extern id IOSArray_NewArrayWithDimensions(
    Class self, NSUInteger dimensionCount, const jint *dimensionLengths, IOSClass *type);

static IOSObjectArray *IOSObjectArray_CreateArray(jint length, IOSClass *type) {
  if (length < 0) {
    @throw AUTORELEASE([[JavaLangNegativeArraySizeException alloc] init]);
  }
  size_t buf_size = length * sizeof(id);
  IOSObjectArray *array = NSAllocateObject([IOSObjectArray class], buf_size, nil);
  // Set array contents to Java default value (null).
  memset(array->buffer_, 0, buf_size);
  array->size_ = length;
  array->elementType_ = type; // All IOSClass types are singleton so don't need to retain.
  return array;
}

static IOSObjectArray *IOSObjectArray_CreateArrayWithObjects(
    jint length, IOSClass *type, const id *objects) {
  IOSObjectArray *array = IOSObjectArray_CreateArray(length, type);
  for (jint i = 0; i < length; i++) {
    array->buffer_[i] = RETAIN_(objects[i]);
  }
  return array;
}

@implementation IOSObjectArray

@synthesize elementType = elementType_;

+ (instancetype)newArrayWithLength:(NSUInteger)length type:(IOSClass *)type {
  return IOSObjectArray_CreateArray((jint)length, type);
}

+ (instancetype)arrayWithLength:(NSUInteger)length type:(IOSClass *)type {
  return AUTORELEASE(IOSObjectArray_CreateArray((jint)length, type));
}

+ (instancetype)newArrayWithObjects:(const id *)objects
                              count:(NSUInteger)count
                               type:(IOSClass *)type {
  return IOSObjectArray_CreateArrayWithObjects((jint)count, type, objects);
}

+ (instancetype)arrayWithObjects:(const id *)objects
                           count:(NSUInteger)count
                            type:(IOSClass *)type {
  return AUTORELEASE(IOSObjectArray_CreateArrayWithObjects((jint)count, type, objects));
}

+ (instancetype)arrayWithArray:(IOSObjectArray *)array {
  return [IOSObjectArray arrayWithObjects:array->buffer_
                                    count:array->size_
                                     type:array->elementType_];
}

+ (instancetype)arrayWithNSArray:(NSArray *)array type:(IOSClass *)type {
  NSUInteger count = [array count];
  IOSObjectArray *result = AUTORELEASE(IOSObjectArray_CreateArray((jint)count, type));
  [array getObjects:result->buffer_ range:NSMakeRange(0, count)];
  for (NSUInteger i = 0; i < count; i++) {
    RETAIN_(result->buffer_[i]);
  }
  return result;
}

+ (instancetype)arrayWithDimensions:(NSUInteger)dimensionCount
                            lengths:(const jint *)dimensionLengths
                               type:(IOSClass *)type {
  return AUTORELEASE(IOSArray_NewArrayWithDimensions(
      self, dimensionCount, dimensionLengths, type));
}

+ (instancetype)newArrayWithDimensions:(NSUInteger)dimensionCount
                               lengths:(const jint *)dimensionLengths
                                  type:(IOSClass *)type {
  return IOSArray_NewArrayWithDimensions(self, dimensionCount, dimensionLengths, type);
}

- (id)objectAtIndex:(NSUInteger)index {
  return IOSObjectArray_Get(self, (jint)index);
}

static void ThrowArrayStoreException(IOSObjectArray *array, id value) {
  NSString *msg = [NSString stringWithFormat:
      @"attempt to add object of type %@ to array with type %@",
      [[value java_getClass] getName], [array->elementType_ getName]];
  @throw AUTORELEASE([[JavaLangArrayStoreException alloc] initWithNSString:msg]);
}

static inline id IOSObjectArray_checkValue(
    __unsafe_unretained IOSObjectArray *array, __unsafe_unretained id value) {
  if (value && ![array->elementType_ isInstance:value]) {
    ThrowArrayStoreException(array, value);
  }
  return value;
}

// Same as above, but releases the value before throwing an exception.
static inline void IOSObjectArray_checkRetainedValue(IOSObjectArray *array, id value) {
  if (value && ![array->elementType_ isInstance:value]) {
    AUTORELEASE(value);
    ThrowArrayStoreException(array, value);
  }
}

// Same as IOSArray_checkIndex, but releases the value before throwing an
// exception.
static inline void IOSObjectArray_checkIndexRetainedValue(jint size, jint index, id value) {
  if (index < 0 || index >= size) {
    AUTORELEASE(value);
    IOSArray_throwOutOfBoundsWithMsg(size, index);
  }
}

id IOSObjectArray_Set(
    __unsafe_unretained IOSObjectArray *array, NSUInteger index, __unsafe_unretained id value) {
  IOSArray_checkIndex(array->size_, (jint)index);
  IOSObjectArray_checkValue(array, value);
  return JreAutoreleasedAssign(&array->buffer_[index], RETAIN_(value));
}

id IOSObjectArray_SetAndConsume(IOSObjectArray *array, NSUInteger index, id value) {
  IOSObjectArray_checkIndexRetainedValue(array->size_, (jint)index, value);
  IOSObjectArray_checkRetainedValue(array, value);
  return JreAutoreleasedAssign(&array->buffer_[index], value);
}

id IOSObjectArray_SetRef(JreArrayRef ref, id value) {
  // Index is checked when accessing the JreArrayRef.
  IOSObjectArray_checkValue(ref.arr, value);
  return JreAutoreleasedAssign(ref.pValue, RETAIN_(value));
}

- (id)replaceObjectAtIndex:(NSUInteger)index withObject:(id)value {
  return IOSObjectArray_Set(self, index, value);
}

- (void)getObjects:(NSObject **)buffer length:(NSUInteger)length {
  IOSArray_checkIndex(size_, (jint)length - 1);
  for (NSUInteger i = 0; i < length; i++) {
    id element = buffer_[i];
    buffer[i] = RETAIN_(element);
  }
}

static void DoRetainedMove(id __strong *buffer, jint src, jint dest, jint length) {
  jint releaseStart = dest;
  jint releaseEnd = dest + length;
  jint retainStart = src;
  jint retainEnd = src + length;
  if (retainEnd > releaseStart && retainEnd <= releaseEnd) {
    jint tmp = releaseStart;
    releaseStart = retainEnd;
    retainEnd = tmp;
  } else if (releaseEnd > retainStart && releaseEnd <= retainEnd) {
    jint tmp = retainStart;
    retainStart = releaseEnd;
    releaseEnd = tmp;
  }
  for (jint i = releaseStart; i < releaseEnd; i++) {
    AUTORELEASE(buffer[i]);
  }
  memmove(buffer + dest, buffer + src, length * sizeof(id));
#if ! __has_feature(objc_arc)
  for (jint i = retainStart; i < retainEnd; i++) {
    [buffer[i] retain];
  }
#endif
}

- (void)arraycopy:(jint)offset
      destination:(IOSArray *)destination
        dstOffset:(jint)dstOffset
           length:(jint)length {
  IOSArray_checkRange(size_, offset, length);
  IOSArray_checkRange(destination->size_, dstOffset, length);
  IOSObjectArray *dest = (IOSObjectArray *) destination;

  if (self == dest) {
    DoRetainedMove(buffer_, offset, dstOffset, length);
  } else {
    // If dest element type can be assigned to this array, then all of its
    // elements are assignable and therefore don't need to be individually
    // checked.
    if ([dest->elementType_ isAssignableFrom:elementType_]) {
      for (jint i = 0; i < length; i++) {
        JreAutoreleasedAssign(&dest->buffer_[i + dstOffset], RETAIN_(buffer_[i + offset]));
      }
    } else {
      for (jint i = 0; i < length; i++) {
        id newElement = IOSObjectArray_checkValue(dest, buffer_[i + offset]);
        JreAutoreleasedAssign(&dest->buffer_[i + dstOffset], RETAIN_(newElement));
      }
    }
  }
}

- (id)copyWithZone:(NSZone *)zone {
  IOSObjectArray *result = IOSObjectArray_CreateArray(size_, elementType_);
  for (jint i = 0; i < size_; i++) {
    result->buffer_[i] = RETAIN_(buffer_[i]);
  }
  return result;
}

- (NSString *)descriptionOfElementAtIndex:(jint)index {
  return (NSString *) [buffer_[index] description];
}

- (void)dealloc {
  for (jint i = 0; i < size_; i++) {
    RELEASE_(buffer_[i]);   // NO-OP with ARC.
    buffer_[i] = nil;
  }
#if ! __has_feature(objc_arc)
  [super dealloc];
#endif
}

- (NSUInteger)countByEnumeratingWithState:(NSFastEnumerationState *)state
                                  objects:(__unsafe_unretained id *)stackbuf
                                    count:(NSUInteger)len {
  if (state->state == 0) {
    state->mutationsPtr = (unsigned long *) (ARCBRIDGE void *) self;
    state->itemsPtr = (__unsafe_unretained id *) (void *) buffer_;
    state->state = 1;
    return size_;
  } else {
    return 0;
  }
}

- (void *)buffer {
  return buffer_;
}

@end
