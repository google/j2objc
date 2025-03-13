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
//  IOSObjectArray.h
//  JreEmulation
//
//  Created by Tom Ball on 9/9/11.
//

#ifndef IOSObjectArray_H
#define IOSObjectArray_H

#import "IOSArray.h"

#pragma clang diagnostic push
#pragma GCC diagnostic ignored "-Wnullability-completeness"

@class IOSClass;
@class IOSObjectArray;

/**
 * An emulation class that represents a Java object array.  Like a Java array,
 * an IOSObjectArray is fixed-size but its elements are mutable.
 */
NS_ASSUME_NONNULL_BEGIN
@interface IOSObjectArray<__covariant ObjectType> : IOSArray <ObjectType> {
 @public
  /**
   * The type of elements in this array.
   * This field is read-only, visible only for performance reasons. DO NOT MODIFY!
   */
  IOSClass *elementType_;

  /**
   * The elements of this array.
   */
  // Ensure alignment for java.util.concurrent.atomic.AtomicReferenceArray.
  ObjectType __strong buffer_[0] __attribute__((aligned(__alignof__(volatile_id))));
}

@property(readonly) IOSClass *elementType;

/** Create an array from a C object array, length, and type. */
+ (instancetype)newArrayWithObjects:(const _Nonnull ObjectType *_Nonnull)objects
                              count:(NSUInteger)count
                               type:(IOSClass *)type;

/** Create an autoreleased array from a C object array, length, and type. */
+ (instancetype)arrayWithObjects:(const _Nonnull ObjectType *_Nonnull)objects
                           count:(NSUInteger)count
                            type:(IOSClass *)type;

/** Create an empty array with a type and length. */
+ (instancetype)newArrayWithLength:(NSUInteger)length type:(IOSClass *)type;

/** Create an autoreleased empty array with a type and length. */
+ (instancetype)arrayWithLength:(NSUInteger)length type:(IOSClass *)type;

/** Create an autoreleased empty multidimensional array. */
+ (instancetype)arrayWithDimensions:(NSUInteger)dimensionCount
                            lengths:(const jint *)dimensionLengths
                               type:(IOSClass *)type;

/** Create an empty multidimensional array. */
+ (instancetype)newArrayWithDimensions:(NSUInteger)dimensionCount
                               lengths:(const jint *)dimensionLengths
                                  type:(IOSClass *)type;

/** Create an autoreleased array with the elements from an NSArray. */
+ (instancetype)arrayWithArray:(IOSObjectArray<ObjectType> *)array;

/** Create an autoreleased array with the elements from an NSArray. */
+ (instancetype)arrayWithNSArray:(NSArray *)array type:(IOSClass *)type;

/**
 * Return element at a specified index.
 * @throws IndexOutOfBoundsException
 * if out out range
 */
- (ObjectType)objectAtIndex:(NSUInteger)index;

/**
 * Sets element at a specified index.
 * @throws IndexOutOfBoundsException
 * if index is out of range
 * @return the replacement object.
 */
- (ObjectType)replaceObjectAtIndex:(NSUInteger)index withObject:(id)value;

/**
 * Copies the array contents into a specified buffer, up to the specified
 * length.
 * @throws IndexOutOfBoundsException
 * if the specified length is greater than the array size.
 */
- (void)getObjects:(NSObject *_Nonnull *_Nonnull)buffer length:(NSUInteger)length;

@end

/**
 * Gets element at a specified index, functional equivalent to objectAtIndex:.
 * @throws IndexOutOfBoundsException
 * if index is out of range
 * @return the element at index.
 */
__attribute__((always_inline)) inline id _Nullable IOSObjectArray_Get(
    __unsafe_unretained IOSObjectArray *_Nonnull array, jint index) {
  IOSArray_checkIndex(array->size_, index);
  return ALWAYS_RETAINED_AUTORELEASED_RETURN_VALUE(array->buffer_[index]);
}

/**
 * Sets element at a specified index, functional equivalent to replaceObjectAtIndex:withObject:.
 * @throws IndexOutOfBoundsException
 * if index is out of range
 * @return the replacement object.
 */
FOUNDATION_EXPORT id _Nullable IOSObjectArray_Set(IOSObjectArray *_Nonnull array, NSUInteger index,
                                                  id _Nullable value);

/**
 * Sets element at a specified index, same as IOSObjectArray_Set(), but this function
 * releases the object before throwing an exception.
 * @throws IndexOutOfBoundsException
 * if index is out of range
 * @return the replacement object.
 */
FOUNDATION_EXPORT id _Nullable IOSObjectArray_SetAndConsume(IOSObjectArray *_Nonnull array,
                                                            NSUInteger index,
                                                            id _Nullable
                                                            __attribute__((ns_consumed)) value);

// Internal only. Provides a pointer to an element with the array itself.
// Used for translating certain compound expressions.
typedef struct JreArrayRef {
  __unsafe_unretained IOSObjectArray *_Nonnull arr;
  __strong id _Nonnull *_Nullable pValue;
} JreArrayRef;

// Internal only functions.
__attribute__((always_inline)) inline JreArrayRef IOSObjectArray_GetRef(
    __unsafe_unretained IOSObjectArray *_Nonnull array, jint index) {
  IOSArray_checkIndex(array->size_, index);
  return (JreArrayRef){.arr = array, .pValue = &array->buffer_[index]};
}
FOUNDATION_EXPORT id _Nullable IOSObjectArray_SetRef(JreArrayRef ref, id _Nullable value);

NS_ASSUME_NONNULL_END

#pragma clang diagnostic pop

#endif  // IOSObjectArray_H
