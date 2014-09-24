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

#import "IOSObjectArray.h"
#import "IOSClass.h"
#import "java/lang/ArrayStoreException.h"
#import "java/lang/AssertionError.h"

// Defined in IOSArray.m
extern id IOSArray_NewArrayWithDimensions(
    Class self, NSUInteger dimensionCount, const jint *dimensionLengths, IOSClass *type);

static IOSObjectArray *IOSObjectArray_NewArray(jint length, IOSClass *type) {
  IOSObjectArray *array = NSAllocateObject([IOSObjectArray class], length * sizeof(id), nil);
  array->size_ = length;
  array->elementType_ = type; // All IOSClass types are singleton so don't need to retain.
  return array;
}

static IOSObjectArray *IOSObjectArray_NewArrayWithObjects(
    jint length, IOSClass *type, const id *objects) {
  IOSObjectArray *array = IOSObjectArray_NewArray(length, type);
  for (jint i = 0; i < length; i++) {
    array->buffer_[i] = [objects[i] retain];
  }
  return array;
}

@implementation IOSObjectArray

@synthesize elementType = elementType_;

+ (instancetype)newArrayWithLength:(NSUInteger)length type:(IOSClass *)type {
  return IOSObjectArray_NewArray((jint)length, type);
}

+ (instancetype)arrayWithLength:(NSUInteger)length type:(IOSClass *)type {
  return [IOSObjectArray_NewArray((jint)length, type) autorelease];
}

+ (instancetype)newArrayWithObjects:(const id *)objects
                              count:(NSUInteger)count
                               type:(IOSClass *)type {
  return IOSObjectArray_NewArrayWithObjects((jint)count, type, objects);
}

+ (instancetype)arrayWithObjects:(const id *)objects
                           count:(NSUInteger)count
                            type:(IOSClass *)type {
  return [IOSObjectArray_NewArrayWithObjects((jint)count, type, objects) autorelease];
}

+ (instancetype)arrayWithArray:(IOSObjectArray *)array {
  return [IOSObjectArray arrayWithObjects:array->buffer_
                                    count:array->size_
                                     type:array->elementType_];
}

+ (instancetype)arrayWithNSArray:(NSArray *)array type:(IOSClass *)type {
  NSUInteger count = [array count];
  IOSObjectArray *result = IOSObjectArray_NewArray((jint)count, type);
  [array getObjects:result->buffer_ range:NSMakeRange(0, count)];
  for (NSUInteger i = 0; i < count; i++) {
    [result->buffer_[i] retain];
  }
  return [result autorelease];
}

+ (instancetype)arrayWithDimensions:(NSUInteger)dimensionCount
                            lengths:(const jint *)dimensionLengths
                               type:(IOSClass *)type {
  return [IOSArray_NewArrayWithDimensions(
      self, dimensionCount, dimensionLengths, type) autorelease];
}

+ (instancetype)newArrayWithDimensions:(NSUInteger)dimensionCount
                               lengths:(const jint *)dimensionLengths
                                  type:(IOSClass *)type {
  return IOSArray_NewArrayWithDimensions(self, dimensionCount, dimensionLengths, type);
}

+ (id)iosClassWithType:(IOSClass *)type {
  return [IOSClass arrayClassWithComponentType:type];
}

+ (id)iosClassWithDimensions:(NSUInteger)dimensions type:(IOSClass *)type {
  IOSClass *result = [IOSClass arrayClassWithComponentType:type];
  while (--dimensions > 0) {
    result = [IOSClass arrayClassWithComponentType:result];
  }
  return result;
}

id IOSObjectArray_Get(__unsafe_unretained IOSObjectArray *array, NSUInteger index) {
  IOSArray_checkIndex(array->size_, (jint)index);
  return array->buffer_[index];
}

- (id)objectAtIndex:(NSUInteger)index {
  IOSArray_checkIndex(size_, (jint)index);
  return buffer_[index];
}

#if !defined(J2OBJC_DISABLE_ARRAY_CHECKS)
static void ThrowArrayStoreException(IOSObjectArray *array, id value) {
  NSString *msg = [NSString stringWithFormat:
      @"attempt to add object of type %@ to array with type %@",
      [[value getClass] getName], [array->elementType_ getName]];
  @throw AUTORELEASE([[JavaLangArrayStoreException alloc] initWithNSString:msg]);
}
#endif

static inline id IOSObjectArray_checkValue(
    __unsafe_unretained IOSObjectArray *array, __unsafe_unretained id value) {
#if !defined(J2OBJC_DISABLE_ARRAY_CHECKS)
  if (value && ![array->elementType_ isInstance:value]) {
    ThrowArrayStoreException(array, value);
  }
#endif
  return value;
}

// Same as above, but releases the value before throwing an exception.
static inline void IOSObjectArray_checkRetainedValue(IOSObjectArray *array, id value) {
#if !defined(J2OBJC_DISABLE_ARRAY_CHECKS)
  if (value && ![array->elementType_ isInstance:value]) {
    [value autorelease];
    ThrowArrayStoreException(array, value);
  }
#endif
}

// Same as IOSArray_checkIndex, but releases the value before throwing an
// exception.
static inline void IOSObjectArray_checkIndexRetainedValue(jint size, jint index, id value) {
#if !defined(J2OBJC_DISABLE_ARRAY_CHECKS)
  if (index < 0 || index >= size) {
    [value autorelease];
    IOSArray_throwOutOfBoundsWithMsg(size, index);
  }
#endif
}

id IOSObjectArray_Set(
    __unsafe_unretained IOSObjectArray *array, NSUInteger index, __unsafe_unretained id value) {
  IOSArray_checkIndex(array->size_, (jint)index);
  IOSObjectArray_checkValue(array, value);
#if ! __has_feature(objc_arc)
  [array->buffer_[index] autorelease];
#endif
  return array->buffer_[index] = RETAIN_(value);
}

id IOSObjectArray_SetAndConsume(IOSObjectArray *array, NSUInteger index, id value) {
  IOSObjectArray_checkIndexRetainedValue(array->size_, (jint)index, value);
  IOSObjectArray_checkRetainedValue(array, value);
  [array->buffer_[index] autorelease];
  return array->buffer_[index] = value;
}

- (id)replaceObjectAtIndex:(NSUInteger)index withObject:(id)value {
  IOSArray_checkIndex(size_, (jint)index);
  IOSObjectArray_checkValue(self, value);
#if ! __has_feature(objc_arc)
  [buffer_[index] autorelease];
#endif
  return buffer_[index] = RETAIN_(value);
}

- (void)getObjects:(NSObject **)buffer length:(NSUInteger)length {
  IOSArray_checkIndex(size_, (jint)length - 1);
  for (NSUInteger i = 0; i < length; i++) {
    id element = buffer_[i];
    buffer[i] = element;
  }
}

void CopyWithMemmove(id __strong *buffer, NSUInteger src, NSUInteger dest, NSUInteger length) {
  NSUInteger releaseStart = dest;
  NSUInteger releaseEnd = dest + length;
  NSUInteger retainStart = src;
  NSUInteger retainEnd = src + length;
  if (retainEnd > releaseStart && retainEnd <= releaseEnd) {
    NSUInteger tmp = releaseStart;
    releaseStart = retainEnd;
    retainEnd = tmp;
  } else if (releaseEnd > retainStart && releaseEnd <= retainEnd) {
    NSUInteger tmp = retainStart;
    retainStart = releaseEnd;
    releaseEnd = tmp;
  }
#if __has_feature(objc_arc)
  for (NSUInteger i = releaseStart; i < releaseEnd; i++) {
    buffer[i] = nil;
  }
  // memmove is unsafe for general use in ARC so we must cast the buffer to the
  // unretained data type void**. Then we must manually correct the retain
  // counts using bridged casts.
  void **buffer_unretained = (void *)buffer;
  memmove(buffer_unretained + dest, buffer_unretained + src, length * sizeof(id));
  for (NSUInteger i = retainStart; i < retainEnd; i++) {
    // Use a __bridge_retained cast to trick ARC into retaining the element.
    void *tmp = (__bridge_retained void *) buffer[i];
    tmp = nil;  // Avoid unused variable warning.
  }
#else
  for (NSUInteger i = releaseStart; i < releaseEnd; i++) {
    [buffer[i] autorelease];
  }
  memmove(buffer + dest, buffer + src, length * sizeof(id));
  for (NSUInteger i = retainStart; i < retainEnd; i++) {
    [buffer[i] retain];
  }
#endif
}

// We can get a significant performance gain for an overlapping arraycopy in ARC
// by using memmove when the amount of overlap is a large fraction of the moved
// elements. However, the memmove method is more costly than directly copying
// each element with a small overlap. This value has been determined
// experimentally on a OSX desktop device.
#define ARC_MEMMOVE_OVERLAP_RATIO 15

- (void)arraycopy:(jint)offset
      destination:(IOSArray *)destination
        dstOffset:(jint)dstOffset
           length:(jint)length {
  IOSArray_checkRange(size_, offset, length);
  IOSArray_checkRange(destination->size_, dstOffset, length);
  IOSObjectArray *dest = (IOSObjectArray *) destination;

#ifdef J2OBJC_DISABLE_ARRAY_TYPE_CHECKS
  BOOL skipElementCheck = YES;
#else
  // If dest element type can be assigned to this array, then all of its
  // elements are assignable and therefore don't need to be individually
  // checked.
  BOOL skipElementCheck = [dest->elementType_ isAssignableFrom:elementType_];
#endif

#if __has_feature(objc_arc)
  if (self == dest) {
    int shift = abs(dstOffset - offset);
    if (length > ARC_MEMMOVE_OVERLAP_RATIO * shift) {
      CopyWithMemmove(buffer_, offset, dstOffset, length);
    } else if (offset < dstOffset) {
      for (int i = length - 1; i >= 0; i--) {
        buffer_[i + dstOffset] = buffer_[i + offset];
      }
    } else {
      for (int i = 0; i < length; i++) {
        buffer_[i + dstOffset] = buffer_[i + offset];
      }
    }
  } else if (skipElementCheck) {
    for (int i = 0; i < length; i++) {
      dest->buffer_[i + dstOffset] = buffer_[i + offset];
    }
  } else {
    for (int i = 0; i < length; i++) {
      dest->buffer_[i + dstOffset] = IOSObjectArray_checkValue(dest, buffer_[i + offset]);
    }
  }
#else
  if (self == dest) {
    CopyWithMemmove(buffer_, offset, dstOffset, length);
  } else if (skipElementCheck) {
    for (jint i = 0; i < length; i++) {
      id newElement = buffer_[i + offset];
      [dest->buffer_[i + dstOffset] autorelease];
      dest->buffer_[i + dstOffset] = [newElement retain];
    }
  } else {
    for (jint i = 0; i < length; i++) {
      id newElement = skipElementCheck ? buffer_[i + offset] :
          IOSObjectArray_checkValue(dest, buffer_[i + offset]);
      [dest->buffer_[i + dstOffset] autorelease];
      dest->buffer_[i + dstOffset] = [newElement retain];
    }
  }
#endif
}

- (id)copyWithZone:(NSZone *)zone {
  IOSObjectArray *result = IOSObjectArray_NewArray(size_, elementType_);
  for (jint i = 0; i < size_; i++) {
    result->buffer_[i] = [buffer_[i] retain];
  }
  return result;
}

- (NSString *)descriptionOfElementAtIndex:(jint)index {
  return (NSString *) [buffer_[index] description];
}

- (void)dealloc {
  for (jint i = 0; i < size_; i++) {
    [buffer_[i] release];
  }
  [super dealloc];
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

- (NSArray *)memDebugStrongReferences {
  NSMutableArray *result = [NSMutableArray array];
  for (jint i = 0; i < size_; i++) {
    [result addObject:[JreMemDebugStrongReference strongReferenceWithObject:buffer_[i] name:@"element"]];
  }
  return result;
}

@end
