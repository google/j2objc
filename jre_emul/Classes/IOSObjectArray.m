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
#import "java/lang/NegativeArraySizeException.h"

// Defined in IOSArray.m
extern id IOSArray_NewArrayWithDimensions(
    Class self, NSUInteger dimensionCount, const jint *dimensionLengths, IOSClass *type);

static IOSObjectArray *IOSObjectArray_CreateArray(jint length, IOSClass *type, BOOL retained) {
  if (length < 0) {
    @throw AUTORELEASE([[JavaLangNegativeArraySizeException alloc] init]);
  }
  IOSObjectArray *array = NSAllocateObject([IOSObjectArray class], length * sizeof(id), nil);
  if (!retained) {
    // It is important that this autorelease occurs here and NOT as part of the
    // return statement of one of the public methods. When such a public method
    // is called from ARC code, it can omit the autorelease call (and the
    // subsequent retain in the caller) even though this code is compiled as
    // non-ARC. Such behavior would allow our isRetained_ field to remain false
    // even when this array has a strong reference.
    [array autorelease];
  }
  array->size_ = length;
  array->elementType_ = type; // All IOSClass types are singleton so don't need to retain.
  array->isRetained_ = retained;
  return array;
}

static IOSObjectArray *IOSObjectArray_CreateArrayWithObjects(
    jint length, IOSClass *type, BOOL retained, const id *objects) {
  IOSObjectArray *array = IOSObjectArray_CreateArray(length, type, retained);
  if (retained) {
    for (jint i = 0; i < length; i++) {
      array->buffer_[i] = [objects[i] retain];
    }
  } else {
    memcpy(array->buffer_, objects, length * sizeof(id));
  }
  return array;
}

@implementation IOSObjectArray

@synthesize elementType = elementType_;

+ (instancetype)newArrayWithLength:(NSUInteger)length type:(IOSClass *)type {
  return IOSObjectArray_CreateArray((jint)length, type, YES);
}

+ (instancetype)arrayWithLength:(NSUInteger)length type:(IOSClass *)type {
  return IOSObjectArray_CreateArray((jint)length, type, NO);
}

+ (instancetype)newArrayWithObjects:(const id *)objects
                              count:(NSUInteger)count
                               type:(IOSClass *)type {
  return IOSObjectArray_CreateArrayWithObjects((jint)count, type, YES, objects);
}

+ (instancetype)arrayWithObjects:(const id *)objects
                           count:(NSUInteger)count
                            type:(IOSClass *)type {
  return IOSObjectArray_CreateArrayWithObjects((jint)count, type, NO, objects);
}

+ (instancetype)arrayWithArray:(IOSObjectArray *)array {
  return [IOSObjectArray arrayWithObjects:array->buffer_
                                    count:array->size_
                                     type:array->elementType_];
}

+ (instancetype)arrayWithNSArray:(NSArray *)array type:(IOSClass *)type {
  NSUInteger count = [array count];
  IOSObjectArray *result = IOSObjectArray_CreateArray((jint)count, type, NO);
  [array getObjects:result->buffer_ range:NSMakeRange(0, count)];
  return result;
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

- (id)objectAtIndex:(NSUInteger)index {
  IOSArray_checkIndex(size_, (jint)index);
  return buffer_[index];
}

#if !defined(J2OBJC_DISABLE_ARRAY_TYPE_CHECKS)
static void ThrowArrayStoreException(IOSObjectArray *array, id value) {
  NSString *msg = [NSString stringWithFormat:
      @"attempt to add object of type %@ to array with type %@",
      [[value getClass] getName], [array->elementType_ getName]];
  @throw AUTORELEASE([[JavaLangArrayStoreException alloc] initWithNSString:msg]);
}
#endif

static inline id IOSObjectArray_checkValue(
    __unsafe_unretained IOSObjectArray *array, __unsafe_unretained id value) {
#if !defined(J2OBJC_DISABLE_ARRAY_TYPE_CHECKS)
  if (value && ![array->elementType_ isInstance:value]) {
    ThrowArrayStoreException(array, value);
  }
#endif
  return value;
}

// Same as above, but releases the value before throwing an exception.
static inline void IOSObjectArray_checkRetainedValue(IOSObjectArray *array, id value) {
#if !defined(J2OBJC_DISABLE_ARRAY_TYPE_CHECKS)
  if (value && ![array->elementType_ isInstance:value]) {
    [value autorelease];
    ThrowArrayStoreException(array, value);
  }
#endif
}

// Same as IOSArray_checkIndex, but releases the value before throwing an
// exception.
static inline void IOSObjectArray_checkIndexRetainedValue(jint size, jint index, id value) {
#if !defined(J2OBJC_DISABLE_ARRAY_BOUND_CHECKS)
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
  if (array->isRetained_) {
    [array->buffer_[index] autorelease];
    [value retain];
  }
  return array->buffer_[index] = value;
}

id IOSObjectArray_SetAndConsume(IOSObjectArray *array, NSUInteger index, id value) {
  IOSObjectArray_checkIndexRetainedValue(array->size_, (jint)index, value);
  IOSObjectArray_checkRetainedValue(array, value);
  if (array->isRetained_) {
    [array->buffer_[index] autorelease];
  } else {
    [value autorelease];
  }
  return array->buffer_[index] = value;
}

- (id)replaceObjectAtIndex:(NSUInteger)index withObject:(id)value {
  return IOSObjectArray_Set(self, index, value);
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
  for (NSUInteger i = releaseStart; i < releaseEnd; i++) {
    [buffer[i] autorelease];
  }
  memmove(buffer + dest, buffer + src, length * sizeof(id));
  for (NSUInteger i = retainStart; i < retainEnd; i++) {
    [buffer[i] retain];
  }
}

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

  if (self == dest) {
    if (dest->isRetained_) {
      CopyWithMemmove(buffer_, offset, dstOffset, length);
    } else {
      memmove(buffer_ + dstOffset, buffer_ + offset, length * sizeof(id));
    }
  } else {
    if (dest->isRetained_) {
      if (skipElementCheck) {
        for (jint i = 0; i < length; i++) {
          [dest->buffer_[i + dstOffset] autorelease];
          dest->buffer_[i + dstOffset] = [buffer_[i + offset] retain];
        }
      } else {
        for (jint i = 0; i < length; i++) {
          id newElement = IOSObjectArray_checkValue(dest, buffer_[i + offset]);
          [dest->buffer_[i + dstOffset] autorelease];
          [newElement retain];
          dest->buffer_[i + dstOffset] = newElement;
        }
      }
    } else {
      if (skipElementCheck) {
        memcpy(dest->buffer_ + dstOffset, buffer_ + offset, length * sizeof(id));
      } else {
        for (jint i = 0; i < length; i++) {
          dest->buffer_[i + dstOffset] = IOSObjectArray_checkValue(dest, buffer_[i + offset]);
        }
      }
    }
  }
}

- (id)copyWithZone:(NSZone *)zone {
  IOSObjectArray *result = IOSObjectArray_CreateArray(size_, elementType_, YES);
  for (jint i = 0; i < size_; i++) {
    result->buffer_[i] = [buffer_[i] retain];
  }
  return result;
}

- (NSString *)descriptionOfElementAtIndex:(jint)index {
  return (NSString *) [buffer_[index] description];
}

- (id)retain {
  if (!isRetained_) {
    for (jint i = 0; i < size_; i++) {
      [buffer_[i] retain];
    }
    isRetained_ = YES;
  }
  return [super retain];
}

- (void)dealloc {
  if (isRetained_) {
    for (jint i = 0; i < size_; i++) {
      [buffer_[i] release];
    }
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

- (void *)buffer {
  return buffer_;
}

@end
