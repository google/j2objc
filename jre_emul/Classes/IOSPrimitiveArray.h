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

#ifndef _IOSPrimitiveArray_H_
#define _IOSPrimitiveArray_H_

#import "IOSArray.h"

/*!
 * Declares all of the common methods for the primitive array types. For example
 * this would declare the following for IOSIntArray:
 *
 *  Constructors:
 *  + (instancetype)newArrayWithInts:(const int *)buf count:(NSUInteger)count;
 *  + (instancetype)arrayWithInts:(const int *)buf count:(NSUInteger)count;
 *
 *  Accessors - These throw IndexOutOfBoundsException if index is out of range:
 *  FOUNDATION_EXPORT int IOSIntArray_Get(IOSIntrray *array, NSUInteger index);
 *  FOUNDATION_EXPORT int *IOSIntArray_GetRef(IOSIntArray *array, NSUInteger index);
 *  - (int)intAtIndex:(NSUInteger)index;
 *  - (int *)intRefAtIndex:(NSUInteger)index;
 *  - (int)replaceIntAtIndex:(NSUInteger)index withInt:(int)value;
 *  - (void)getInts:(int *)buffer length:(NSUInteger)length;
 *
 * @define PRIMITIVE_ARRAY_INTERFACE
 * @param L_NAME Lowercase name of the primitive type. (e.g. "char")
 * @param U_NAME Uppercase name of the primitive type. (e.g. "Char")
 * @param C_TYPE Objective-C type for the primitive type, (e.g. "unichar")
 */
#define PRIMITIVE_ARRAY_INTERFACE(L_NAME, U_NAME, C_TYPE) \
+ (instancetype)newArrayWithLength:(NSUInteger)length; \
+ (instancetype)arrayWithLength:(NSUInteger)length; \
+ (instancetype)newArrayWith##U_NAME##s:(const C_TYPE *)buf count:(NSUInteger)count; \
+ (instancetype)arrayWith##U_NAME##s:(const C_TYPE *)buf count:(NSUInteger)count; \
- (C_TYPE)L_NAME##AtIndex:(NSUInteger)index; \
- (C_TYPE *)L_NAME##RefAtIndex:(NSUInteger)index; \
- (C_TYPE)replace##U_NAME##AtIndex:(NSUInteger)index with##U_NAME:(C_TYPE)value; \
- (void)get##U_NAME##s:(C_TYPE *)buffer length:(NSUInteger)length; \

/*!
 * Defines the C interface for the primitive array types. This macro is used
 * after the @end declaration for the @interface.
 *
 * @define PRIMITIVE_ARRAY_C_INTERFACE
 * @param L_NAME Lowercase name of the primitive type. (e.g. "char")
 * @param U_NAME Uppercase name of the primitive type. (e.g. "Char")
 * @param C_TYPE Objective-C type for the primitive type, (e.g. "unichar")
 */
#define PRIMITIVE_ARRAY_C_INTERFACE(L_NAME, U_NAME, C_TYPE) \
\
__attribute__((always_inline)) inline C_TYPE IOS##U_NAME##Array_Get( \
    __unsafe_unretained IOS##U_NAME##Array *array, NSUInteger index) { \
  IOSArray_checkIndex(array->size_, (jint)index); \
  return array->buffer_[index]; \
} \
\
__attribute__((always_inline)) inline C_TYPE *IOS##U_NAME##Array_GetRef( \
    __unsafe_unretained IOS##U_NAME##Array *array, NSUInteger index) { \
  IOSArray_checkIndex(array->size_, (jint)index); \
  return &array->buffer_[index]; \
} \


// ********** IOSBooleanArray **********

@interface IOSBooleanArray : IOSArray {
 @public
  jboolean buffer_[0];
}

PRIMITIVE_ARRAY_INTERFACE(boolean, Boolean, jboolean)

@end

PRIMITIVE_ARRAY_C_INTERFACE(boolean, Boolean, jboolean)


// ********** IOSCharArray **********

@interface IOSCharArray : IOSArray {
 @public
  jchar buffer_[0];
}

PRIMITIVE_ARRAY_INTERFACE(char, Char, jchar)

// Create an array from an NSString.
+ (instancetype)arrayWithNSString:(NSString *)string;

@end

PRIMITIVE_ARRAY_C_INTERFACE(char, Char, jchar)


// ********** IOSByteArray **********

@interface IOSByteArray : IOSArray {
 @public
  jbyte buffer_[0];
}

PRIMITIVE_ARRAY_INTERFACE(byte, Byte, jbyte)

// Copies the array contents into a specified buffer, up to the specified
// length.  An IndexOutOfBoundsException is thrown if the specified length
// is greater than the array size.
- (void)getBytes:(jbyte *)buffer
          offset:(jint)offset
          length:(jint)length;

// Copies the specified native buffer into this array at the specified offset.
- (void)replaceBytes:(const jbyte *)source
              length:(jint)length
              offset:(jint)destOffset;

// Returns the bytes of the array encapsulated in an NSData *. Copies the
// underlying data.
- (NSData *)toNSData;

@end

PRIMITIVE_ARRAY_C_INTERFACE(byte, Byte, jbyte)


// ********** IOSShortArray **********

@interface IOSShortArray : IOSArray {
 @public
  jshort buffer_[0];
}

PRIMITIVE_ARRAY_INTERFACE(short, Short, jshort)

@end

PRIMITIVE_ARRAY_C_INTERFACE(short, Short, jshort)


// ********** IOSIntArray **********

@interface IOSIntArray : IOSArray {
 @public
  jint buffer_[0];
}

PRIMITIVE_ARRAY_INTERFACE(int, Int, jint)

@end

PRIMITIVE_ARRAY_C_INTERFACE(int, Int, jint)


// ********** IOSLongArray **********

@interface IOSLongArray : IOSArray {
 @public
  jlong buffer_[0];
}

PRIMITIVE_ARRAY_INTERFACE(long, Long, jlong)

@end

PRIMITIVE_ARRAY_C_INTERFACE(long, Long, jlong)


// ********** IOSFloatArray **********

@interface IOSFloatArray : IOSArray {
 @public
  jfloat buffer_[0];
}

PRIMITIVE_ARRAY_INTERFACE(float, Float, jfloat)

@end

PRIMITIVE_ARRAY_C_INTERFACE(float, Float, jfloat)


// ********** IOSDoubleArray **********

@interface IOSDoubleArray : IOSArray {
 @public
  jdouble buffer_[0];
}

PRIMITIVE_ARRAY_INTERFACE(double, Double, jdouble)

@end

PRIMITIVE_ARRAY_C_INTERFACE(double, Double, jdouble)


#undef PRIMITIVE_ARRAY_INTERFACE
#undef PRIMITIVE_ARRAY_C_INTERFACE

#endif // _IOSPrimitiveArray_H_
