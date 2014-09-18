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
 * Defines an init function for a class that will ensure that the class is
 * initialized. For class "Foo" the function will have the following signature:
 *   inline void Foo_init();
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
FOUNDATION_EXPORT C_TYPE IOS##U_NAME##Array_Get(IOS##U_NAME##Array *array, NSUInteger index); \
FOUNDATION_EXPORT C_TYPE *IOS##U_NAME##Array_GetRef(IOS##U_NAME##Array *array, NSUInteger index); \
- (C_TYPE)L_NAME##AtIndex:(NSUInteger)index; \
- (C_TYPE *)L_NAME##RefAtIndex:(NSUInteger)index; \
- (C_TYPE)replace##U_NAME##AtIndex:(NSUInteger)index with##U_NAME:(C_TYPE)value; \
- (void)get##U_NAME##s:(C_TYPE *)buffer length:(NSUInteger)length; \


// ********** IOSBooleanArray **********

@interface IOSBooleanArray : IOSArray {
 @public
  jboolean buffer_[0];
}

PRIMITIVE_ARRAY_INTERFACE(boolean, Boolean, jboolean)

@end


// ********** IOSCharArray **********

@interface IOSCharArray : IOSArray {
 @public
  jchar buffer_[0];
}

PRIMITIVE_ARRAY_INTERFACE(char, Char, jchar)

// Create an array from an NSString.
+ (instancetype)arrayWithNSString:(NSString *)string;

@end


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


// ********** IOSShortArray **********

@interface IOSShortArray : IOSArray {
 @public
  jshort buffer_[0];
}

PRIMITIVE_ARRAY_INTERFACE(short, Short, jshort)

@end


// ********** IOSIntArray **********

@interface IOSIntArray : IOSArray {
 @public
  jint buffer_[0];
}

PRIMITIVE_ARRAY_INTERFACE(int, Int, jint)

@end


// ********** IOSLongArray **********

@interface IOSLongArray : IOSArray {
 @public
  jlong buffer_[0];
}

PRIMITIVE_ARRAY_INTERFACE(long, Long, jlong)

@end


// ********** IOSFloatArray **********

@interface IOSFloatArray : IOSArray {
 @public
  jfloat buffer_[0];
}

PRIMITIVE_ARRAY_INTERFACE(float, Float, jfloat)

@end


// ********** IOSDoubleArray **********

@interface IOSDoubleArray : IOSArray {
 @public
  jdouble buffer_[0];
}

PRIMITIVE_ARRAY_INTERFACE(double, Double, jdouble)

@end

#endif // _IOSPrimitiveArray_H_
