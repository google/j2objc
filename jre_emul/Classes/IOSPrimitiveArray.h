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
//  IOSPrimitiveArray.h
//  JreEmulation
//

// Declares the emulation classes that represent Java arrays of primitive types.
// Like Java arrays these arrays are fixed-size but their elements are mutable.
//
// Primitive array types:
// IOSBooleanArray
// IOSCharArray
// IOSByteArray
// IOSShortArray
// IOSIntArray
// IOSLongArray
// IOSFloatArray
// IOSDoubleArray

#ifndef IOSPrimitiveArray_H
#define IOSPrimitiveArray_H

#import "IOSArray.h"

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wzero-length-array"

@class JavaLangBoolean;
@class JavaLangByte;
@class JavaLangCharacter;
@class JavaLangDouble;
@class JavaLangFloat;
@class JavaLangInteger;
@class JavaLangLong;
@class JavaLangShort;

// ********** IOSBooleanArray **********

/**
 * An Objective-C representation of a Java boolean array. Like a Java array,
 * an IOSBooleanArray is fixed-size but its elements are mutable.
 */
@interface IOSBooleanArray : IOSArray<JavaLangBoolean *> {
 @public
  /**
   * The elements of this array.
   */
  bool buffer_[0];
}

/**
 * Create a new array of a specified length, setting all booleans to false.
 */
+ (instancetype)newArrayWithLength:(NSUInteger)length;

/**
 * Create a new autoreleased array of a specified length, setting all booleans to false.
 */
+ (instancetype)arrayWithLength:(NSUInteger)length;

/**
 * Create a new array of a specified length, setting the elements to the values in buf.
 */
+ (instancetype)newArrayWithBooleans:(const bool *)buf count:(NSUInteger)count;

/**
 * Create a new autoreleased array of a specified length, setting the elements to the values in buf.
 */
+ (instancetype)arrayWithBooleans:(const bool *)buf count:(NSUInteger)count;

/**
 * Create a new multi-dimensional array of booleans.
 */
+ (id)arrayWithDimensions:(NSUInteger)dimensionCount lengths:(const jint *)dimensionLengths;

/**
 * Create a new autoreleased multi-dimensional array of booleans.
 */
+ (id)newArrayWithDimensions:(NSUInteger)dimensionCount lengths:(const jint *)dimensionLengths
    __attribute__((objc_method_family(none), ns_returns_retained));

/**
 * Return the boolean at the specified index.
 */
- (bool)booleanAtIndex:(NSUInteger)index;

/**
 * Return a pointer to the boolean at the specified index.
 */
- (bool *)booleanRefAtIndex:(NSUInteger)index;

/**
 * Replace the boolean at the specified index. Return the new value.
 */
- (bool)replaceBooleanAtIndex:(NSUInteger)index withBoolean:(bool)value;

/**
 * Copy this array's booleans to a buffer.
 */
- (void)getBooleans:(bool *)buffer length:(NSUInteger)length;

@end

/**
 * @brief Return the boolean at the specified index.
 * Equivalent to booleanAtIndex:.
 */
__attribute__((always_inline)) inline bool IOSBooleanArray_Get(
    __unsafe_unretained IOSBooleanArray *array, jint index) {
  IOSArray_checkIndex(array->size_, index);
  return array->buffer_[index];
}

/**
 * @brief Return a pointer to the boolean at the specified index.
 * Equivalent to booleanRefAtIndex:.
 */
__attribute__((always_inline)) inline bool *IOSBooleanArray_GetRef(
    __unsafe_unretained IOSBooleanArray *array, jint index) {
  IOSArray_checkIndex(array->size_, index);
  return &array->buffer_[index];
}

// ********** IOSCharArray **********

/**
 * An Objective-C representation of a Java char array. Like a Java array,
 * an IOSCharArray is fixed-size but its elements are mutable.
 */
@interface IOSCharArray : IOSArray<JavaLangCharacter *> {
 @public
  /**
   * The elements of this array.
   */
  uint16_t buffer_[0];
}

/**
 * Create a new array of a specified length, setting all chars to '\u0000'.
 */
+ (instancetype)newArrayWithLength:(NSUInteger)length;

/**
 * Create a new autoreleased array of a specified length, setting all chars to '\u0000'.
 */
+ (instancetype)arrayWithLength:(NSUInteger)length;

/**
 * Create a new array of a specified length, setting the elements to the values in buf.
 */
+ (instancetype)newArrayWithChars:(const uint16_t *)buf count:(NSUInteger)count;

/**
 * Create a new autoreleased array of a specified length, setting the elements to the values in buf.
 */
+ (instancetype)arrayWithChars:(const uint16_t *)buf count:(NSUInteger)count;

/**
 * Create a new multi-dimensional array of chars.
 */
+ (id)arrayWithDimensions:(NSUInteger)dimensionCount lengths:(const jint *)dimensionLengths;

/**
 * Create a new autoreleased multi-dimensional array of chars.
 */
+ (id)newArrayWithDimensions:(NSUInteger)dimensionCount lengths:(const jint *)dimensionLengths
    __attribute__((objc_method_family(none), ns_returns_retained));

/**
 * Return the L_NAME at the specified index.
 */
- (uint16_t)charAtIndex:(NSUInteger)index;

/**
 * Return a pointer to the char at the specified index.
 */
- (uint16_t *)charRefAtIndex:(NSUInteger)index;

/**
 * Replace the char at the specified index. Return the new value.
 */
- (uint16_t)replaceCharAtIndex:(NSUInteger)index withChar:(uint16_t)value;

/**
 * Copy this array's chars to a buffer.
 */
- (void)getChars:(uint16_t *)buffer length:(NSUInteger)length;

/**
 * Create a char array from an NSString.
 */
+ (instancetype)arrayWithNSString:(NSString *)string;

@end

/**
 * @brief Return the char at the specified index.
 * Equivalent to charAtIndex:.
 */
__attribute__((always_inline)) inline uint16_t IOSCharArray_Get(
    __unsafe_unretained IOSCharArray *array, jint index) {
  IOSArray_checkIndex(array->size_, index);
  return array->buffer_[index];
}

/**
 * @brief Return a pointer to the char at the specified index.
 * Equivalent to charRefAtIndex:.
 */
__attribute__((always_inline)) inline uint16_t *IOSCharArray_GetRef(
    __unsafe_unretained IOSCharArray *array, jint index) {
  IOSArray_checkIndex(array->size_, index);
  return &array->buffer_[index];
}


// ********** IOSByteArray **********

/**
 * An Objective-C representation of a Java byte array. Like a Java array,
 * an IOSByteArray is fixed-size but its elements are mutable.
 */
@interface IOSByteArray : IOSArray<JavaLangByte *> {
 @public
  /**
   * The elements of this array.
   */
  int8_t buffer_[0];
}

/**
 * Create a new array of a specified length, setting all bytes to 0.
 */
+ (instancetype)newArrayWithLength:(NSUInteger)length;

/**
 * Create a new autoreleased array of a specified length, setting all bytes to 0.
 */
+ (instancetype)arrayWithLength:(NSUInteger)length;

/**
 * Create a new array of a specified length, setting the elements to the values in buf.
 */
+ (instancetype)newArrayWithBytes:(const int8_t *)buf count:(NSUInteger)count;

/**
 * Create a new autoreleased array of a specified length, setting the elements to the values in buf.
 */
+ (instancetype)arrayWithBytes:(const int8_t *)buf count:(NSUInteger)count;

/**
 * Create a new multi-dimensional array of bytes.
 */
+ (id)arrayWithDimensions:(NSUInteger)dimensionCount lengths:(const jint *)dimensionLengths;

/**
 * Create a new autoreleased multi-dimensional array of bytes.
 */
+ (id)newArrayWithDimensions:(NSUInteger)dimensionCount lengths:(const jint *)dimensionLengths
    __attribute__((objc_method_family(none), ns_returns_retained));

/**
 * Return the byte at the specified index.
 */
- (int8_t)byteAtIndex:(NSUInteger)index;

/**
 * Return a pointer to the byte at the specified index.
 */
- (int8_t *)byteRefAtIndex:(NSUInteger)index;

/**
 * Replace the byte at the specified index. Return the new value.
 */
- (int8_t)replaceByteAtIndex:(NSUInteger)index withByte:(int8_t)value;

/**
 * Copy this array's bytes to a buffer.
 */
- (void)getBytes:(int8_t *)buffer length:(NSUInteger)length;

// Create an array from an NSData object.
+ (instancetype)arrayWithNSData:(NSData *)data;

// Copies the array contents into a specified buffer, up to the specified
// length.  An IndexOutOfBoundsException is thrown if the specified length
// is greater than the array size.
- (void)getBytes:(int8_t *)buffer
          offset:(jint)offset
          length:(jint)length;

// Copies the specified native buffer into this array at the specified offset.
- (void)replaceBytes:(const int8_t *)source
              length:(jint)length
              offset:(jint)destOffset;

// Returns the bytes of the array encapsulated in an NSData *. Copies the
// underlying data.
- (NSData *)toNSData;

@end

/**
 * @brief Return the byte at the specified index.
 * Equivalent to byteAtIndex:.
 */
__attribute__((always_inline)) inline int8_t IOSByteArray_Get(
    __unsafe_unretained IOSByteArray *array, jint index) {
  IOSArray_checkIndex(array->size_, index);
  return array->buffer_[index];
}

/**
 * @brief Return a pointer to the byte at the specified index.
 * Equivalent to byteRefAtIndex:.
 */
__attribute__((always_inline)) inline int8_t *IOSByteArray_GetRef(
    __unsafe_unretained IOSByteArray *array, jint index) {
  IOSArray_checkIndex(array->size_, index);
  return &array->buffer_[index];
}


// ********** IOSShortArray **********

/**
 * An Objective-C representation of a Java array of shorts. Like a Java array,
 * an IOSShortArray is fixed-size but its elements are mutable.
 */
@interface IOSShortArray : IOSArray<JavaLangShort *> {
 @public
  /**
   * The elements of this array.
   */
  int16_t buffer_[0];
}

/**
 * Create a new array of a specified length, setting all shorts to 0.
 */
+ (instancetype)newArrayWithLength:(NSUInteger)length;

/**
 * Create a new autoreleased array of a specified length, setting all shorts to 0.
 */
+ (instancetype)arrayWithLength:(NSUInteger)length;

/**
 * Create a new array of a specified length, setting the elements to the values in buf.
 */
+ (instancetype)newArrayWithShorts:(const int16_t *)buf count:(NSUInteger)count;

/**
 * Create a new autoreleased array of a specified length, setting the elements to the values in buf.
 */
+ (instancetype)arrayWithShorts:(const int16_t *)buf count:(NSUInteger)count;

/**
 * Create a new multi-dimensional array of shorts.
 */
+ (id)arrayWithDimensions:(NSUInteger)dimensionCount lengths:(const jint *)dimensionLengths;

/**
 * Create a new autoreleased multi-dimensional array of shorts.
 */
+ (id)newArrayWithDimensions:(NSUInteger)dimensionCount lengths:(const jint *)dimensionLengths
    __attribute__((objc_method_family(none), ns_returns_retained));

/**
 * Return the short at the specified index.
 */
- (int16_t)shortAtIndex:(NSUInteger)index;

/**
 * Return a pointer to the short at the specified index.
 */
- (int16_t *)shortRefAtIndex:(NSUInteger)index;

/**
 * Replace the short at the specified index. Return the new value.
 */
- (int16_t)replaceShortAtIndex:(NSUInteger)index withShort:(int16_t)value;

/**
 * Copy this array's shorts to a buffer.
 */
- (void)getShorts:(int16_t *)buffer length:(NSUInteger)length;

@end

/**
 * @brief Return the short at the specified index.
 * Equivalent to shortAtIndex:.
 */
__attribute__((always_inline)) inline int16_t IOSShortArray_Get(
    __unsafe_unretained IOSShortArray *array, jint index) {
  IOSArray_checkIndex(array->size_, index);
  return array->buffer_[index];
}

/**
 * @brief Return a pointer to the short at the specified index.
 * Equivalent to shortRefAtIndex:.
 */
__attribute__((always_inline)) inline int16_t *IOSShortArray_GetRef(
    __unsafe_unretained IOSShortArray *array, jint index) {
  IOSArray_checkIndex(array->size_, index);
  return &array->buffer_[index];
}


// ********** IOSIntArray **********

/**
 * An Objective-C representation of a Java int array. Like a Java array,
 * an IOSIntArray is fixed-size but its elements are mutable.
 */
@interface IOSIntArray : IOSArray<JavaLangInteger *> {
 @public
  /**
   * The elements of this array.
   */
  // Ensure alignment for java.util.concurrent.atomic.AtomicIntegerArray.
  jint buffer_[0] __attribute__((aligned(__alignof__(volatile_jint))));
}

/**
 * Create a new array of a specified length, setting all ints to 0.
 */
+ (instancetype)newArrayWithLength:(NSUInteger)length;

/**
 * Create a new autoreleased array of a specified length, setting all ints to 0.
 */
+ (instancetype)arrayWithLength:(NSUInteger)length;

/**
 * Create a new array of a specified length, setting the elements to the values in buf.
 */
+ (instancetype)newArrayWithInts:(const jint *)buf count:(NSUInteger)count;

/**
 * Create a new autoreleased array of a specified length, setting the elements to the values in buf.
 */
+ (instancetype)arrayWithInts:(const jint *)buf count:(NSUInteger)count;

/**
 * Create a new multi-dimensional array of ints.
 */
+ (id)arrayWithDimensions:(NSUInteger)dimensionCount lengths:(const jint *)dimensionLengths;

/**
 * Create a new autoreleased multi-dimensional array of ints.
 */
+ (id)newArrayWithDimensions:(NSUInteger)dimensionCount lengths:(const jint *)dimensionLengths
    __attribute__((objc_method_family(none), ns_returns_retained));

/**
 * Return the int at the specified index.
 */
- (jint)intAtIndex:(NSUInteger)index;

/**
 * Return a pointer to the int at the specified index.
 */
- (jint *)intRefAtIndex:(NSUInteger)index;

/**
 * Replace the int at the specified index. Return the new value.
 */
- (jint)replaceIntAtIndex:(NSUInteger)index withInt:(jint)value;

/**
 * Copy this array's ints to a buffer.
 */
- (void)getInts:(jint *)buffer length:(NSUInteger)length;

@end

/**
 * @brief Return the int at the specified index.
 * Equivalent to shortAtIndex:.
 */
__attribute__((always_inline)) inline jint IOSIntArray_Get(
    __unsafe_unretained IOSIntArray *array, jint index) {
  IOSArray_checkIndex(array->size_, index);
  return array->buffer_[index];
}

/**
 * @brief Return a pointer to the int at the specified index.
 * Equivalent to shortRefAtIndex:.
 */
__attribute__((always_inline)) inline jint *IOSIntArray_GetRef(
    __unsafe_unretained IOSIntArray *array, jint index) {
  IOSArray_checkIndex(array->size_, index);
  return &array->buffer_[index];
}


// ********** IOSLongArray **********

/**
 * An Objective-C representation of a Java long array. Like a Java array,
 * an IOSLongArray is fixed-size but its elements are mutable.
 */
@interface IOSLongArray : IOSArray<JavaLangLong *> {
 @public
  /**
   * The elements of this array.
   */
  // Ensure alignment for java.util.concurrent.atomic.AtomicLongArray.
  int64_t buffer_[0] __attribute__((aligned(__alignof__(volatile_int64_t))));
}

/**
 * Create a new array of a specified length, setting all longs to 0L.
 */
+ (instancetype)newArrayWithLength:(NSUInteger)length;

/**
 * Create a new autoreleased array of a specified length, setting all longs to 0L.
 */
+ (instancetype)arrayWithLength:(NSUInteger)length;

/**
 * Create a new array of a specified length, setting the elements to the values in buf.
 */
+ (instancetype)newArrayWithLongs:(const int64_t *)buf count:(NSUInteger)count;

/**
 * Create a new autoreleased array of a specified length, setting the elements to the values in buf.
 */
+ (instancetype)arrayWithLongs:(const int64_t *)buf count:(NSUInteger)count;

/**
 * Create a new multi-dimensional array of longs.
 */
+ (id)arrayWithDimensions:(NSUInteger)dimensionCount lengths:(const jint *)dimensionLengths;

/**
 * Create a new autoreleased multi-dimensional array of longs.
 */
+ (id)newArrayWithDimensions:(NSUInteger)dimensionCount lengths:(const jint *)dimensionLengths
    __attribute__((objc_method_family(none), ns_returns_retained));

/**
 * Return the long at the specified index.
 */
- (int64_t)longAtIndex:(NSUInteger)index;

/**
 * Return a pointer to the long at the specified index.
 */
- (int64_t *)longRefAtIndex:(NSUInteger)index;

/**
 * Replace the long at the specified index. Return the new value.
 */
- (int64_t)replaceLongAtIndex:(NSUInteger)index withLong:(int64_t)value;

/**
 * Copy this array's longs to a buffer.
 */
- (void)getLongs:(int64_t *)buffer length:(NSUInteger)length;

@end

/**
 * @brief Return the long at the specified index.
 * Equivalent to longAtIndex:.
 */
__attribute__((always_inline)) inline int64_t IOSLongArray_Get(
    __unsafe_unretained IOSLongArray *array, jint index) {
  IOSArray_checkIndex(array->size_, index);
  return array->buffer_[index];
}

/**
 * @brief Return a pointer to the long at the specified index.
 * Equivalent to longRefAtIndex:.
 */
__attribute__((always_inline)) inline int64_t *IOSLongArray_GetRef(
    __unsafe_unretained IOSLongArray *array, jint index) {
  IOSArray_checkIndex(array->size_, index);
  return &array->buffer_[index];
}


// ********** IOSFloatArray **********

/**
 * An Objective-C representation of a Java float array. Like a Java array,
 * an IOSFloatArray is fixed-size but its elements are mutable.
 */
@interface IOSFloatArray : IOSArray<JavaLangFloat *> {
 @public
  /**
   * The elements of this array.
   */
  float buffer_[0];
}

/**
 * Create a new array of a specified length, setting all floats to 0.0F.
 */
+ (instancetype)newArrayWithLength:(NSUInteger)length;

/**
 * Create a new autoreleased array of a specified length, setting all floats to 0.0F.
 */
+ (instancetype)arrayWithLength:(NSUInteger)length;

/**
 * Create a new array of a specified length, setting the elements to the values in buf.
 */
+ (instancetype)newArrayWithFloats:(const float *)buf count:(NSUInteger)count;

/**
 * Create a new autoreleased array of a specified length, setting the elements to the values in buf.
 */
+ (instancetype)arrayWithFloats:(const float *)buf count:(NSUInteger)count;

/**
 * Create a new multi-dimensional array of floats.
 */
+ (id)arrayWithDimensions:(NSUInteger)dimensionCount lengths:(const jint *)dimensionLengths;

/**
 * Create a new autoreleased multi-dimensional array of floats.
 */
+ (id)newArrayWithDimensions:(NSUInteger)dimensionCount lengths:(const jint *)dimensionLengths
    __attribute__((objc_method_family(none), ns_returns_retained));

/**
 * Return the float at the specified index.
 */
- (float)floatAtIndex:(NSUInteger)index;

/**
 * Return a pointer to the float at the specified index.
 */
- (float *)floatRefAtIndex:(NSUInteger)index;

/**
 * Replace the float at the specified index. Return the new value.
 */
- (float)replaceFloatAtIndex:(NSUInteger)index withFloat:(float)value;

/**
 * Copy this array's floats to a buffer.
 */
- (void)getFloats:(float *)buffer length:(NSUInteger)length;

@end

/**
 * @brief Return the float at the specified index.
 * Equivalent to floatAtIndex:.
 */
__attribute__((always_inline)) inline float IOSFloatArray_Get(
    __unsafe_unretained IOSFloatArray *array, jint index) {
  IOSArray_checkIndex(array->size_, index);
  return array->buffer_[index];
}

/**
 * @brief Return a pointer to the float at the specified index.
 * Equivalent to floatRefAtIndex:.
 */
__attribute__((always_inline)) inline float *IOSFloatArray_GetRef(
    __unsafe_unretained IOSFloatArray *array, jint index) {
  IOSArray_checkIndex(array->size_, index);
  return &array->buffer_[index];
}


// ********** IOSDoubleArray **********

/**
 * An Objective-C representation of a Java double array. Like a Java array,
 * an IOSDoubleArray is fixed-size but its elements are mutable.
 */
@interface IOSDoubleArray : IOSArray<JavaLangDouble *> {
 @public
  /**
   * The elements of this array.
   */
  double buffer_[0];
}

/**
 * Create a new array of a specified length, setting all doubles to 0.0.
 */
+ (instancetype)newArrayWithLength:(NSUInteger)length;

/**
 * Create a new autoreleased array of a specified length, setting all doubles to 0.0.
 */
+ (instancetype)arrayWithLength:(NSUInteger)length;

/**
 * Create a new array of a specified length, setting the elements to the values in buf.
 */
+ (instancetype)newArrayWithDoubles:(const double *)buf count:(NSUInteger)count;

/**
 * Create a new autoreleased array of a specified length, setting the elements to the values in buf.
 */
+ (instancetype)arrayWithDoubles:(const double *)buf count:(NSUInteger)count;

/**
 * Create a new multi-dimensional array of doubles.
 */
+ (id)arrayWithDimensions:(NSUInteger)dimensionCount lengths:(const jint *)dimensionLengths;

/**
 * Create a new autoreleased multi-dimensional array of doubles.
 */
+ (id)newArrayWithDimensions:(NSUInteger)dimensionCount lengths:(const jint *)dimensionLengths
    __attribute__((objc_method_family(none), ns_returns_retained));

/**
 * Return the double at the specified index.
 */
- (double)doubleAtIndex:(NSUInteger)index;

/**
 * Return a pointer to the double at the specified index.
 */
- (double *)doubleRefAtIndex:(NSUInteger)index;

/**
 * Replace the double at the specified index. Return the new value.
 */
- (double)replaceDoubleAtIndex:(NSUInteger)index withDouble:(double)value;

/**
 * Copy this array's doubles to a buffer.
 */
- (void)getDoubles:(double *)buffer length:(NSUInteger)length;

@end

/**
 * @brief Return the double at the specified index.
 * Equivalent to doubleAtIndex:.
 */
__attribute__((always_inline)) inline double IOSDoubleArray_Get(
    __unsafe_unretained IOSDoubleArray *array, jint index) {
  IOSArray_checkIndex(array->size_, index);
  return array->buffer_[index];
}

/**
 * @brief Return a pointer to the double at the specified index.
 * Equivalent to doubleRefAtIndex:.
 */
__attribute__((always_inline)) inline double *IOSDoubleArray_GetRef(
    __unsafe_unretained IOSDoubleArray *array, jint index) {
  IOSArray_checkIndex(array->size_, index);
  return &array->buffer_[index];
}


#undef PRIMITIVE_ARRAY_INTERFACE
#undef PRIMITIVE_ARRAY_C_INTERFACE

#pragma clang diagnostic pop
#endif // IOSPrimitiveArray_H
