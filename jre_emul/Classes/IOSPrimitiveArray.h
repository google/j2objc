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
 *  + (id)newArrayWithInts:(const int *)buf count:(NSUInteger)count;
 *  + (id)arrayWithInts:(const int *)buf count:(NSUInteger)count;
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
+ (id)newArrayWithLength:(NSUInteger)length; \
+ (id)arrayWithLength:(NSUInteger)length; \
+ (id)newArrayWith##U_NAME##s:(const C_TYPE *)buf count:(NSUInteger)count; \
+ (id)arrayWith##U_NAME##s:(const C_TYPE *)buf count:(NSUInteger)count; \
FOUNDATION_EXPORT C_TYPE IOS##U_NAME##Array_Get(IOS##U_NAME##Array *array, NSUInteger index); \
FOUNDATION_EXPORT C_TYPE *IOS##U_NAME##Array_GetRef(IOS##U_NAME##Array *array, NSUInteger index); \
- (C_TYPE)L_NAME##AtIndex:(NSUInteger)index; \
- (C_TYPE *)L_NAME##RefAtIndex:(NSUInteger)index; \
- (C_TYPE)replace##U_NAME##AtIndex:(NSUInteger)index with##U_NAME:(C_TYPE)value; \
- (void)get##U_NAME##s:(C_TYPE *)buffer length:(NSUInteger)length; \


// ********** IOSBooleanArray **********

@interface IOSBooleanArray : IOSArray {
 @public
  BOOL *buffer_;  // java.nio requires this be first field in IOSArray subclasses.
}

PRIMITIVE_ARRAY_INTERFACE(boolean, Boolean, BOOL)

@end


// ********** IOSCharArray **********

@interface IOSCharArray : IOSArray {
 @public
  unichar *buffer_;  // java.nio requires this be first field in IOSArray subclasses.
}

PRIMITIVE_ARRAY_INTERFACE(char, Char, unichar)

// Create an array from an NSString.
+ (id)arrayWithNSString:(NSString *)string;

// Returns a copy of the array contents.
- (unichar *)getChars;

@end


// ********** IOSByteArray **********

@interface IOSByteArray : IOSArray {
 @public
  char *buffer_;  // java.nio requires this be first field in IOSArray subclasses.
}

PRIMITIVE_ARRAY_INTERFACE(byte, Byte, char)

// Copies the array contents into a specified buffer, up to the specified
// length.  An IndexOutOfBoundsException is thrown if the specified length
// is greater than the array size.
- (void)getBytes:(char *)buffer
          offset:(NSUInteger)offset
          length:(NSUInteger)length;

// Copies the specified native buffer into this array at the specified offset.
- (void)replaceBytes:(const char *)source
              length:(NSUInteger)length
              offset:(NSUInteger)destOffset;

// Returns the bytes of the array encapsulated in an NSData *. Copies the
// underlying data.
- (NSData *)toNSData;

@end


// ********** IOSShortArray **********

@interface IOSShortArray : IOSArray {
 @public
  short *buffer_;  // java.nio requires this be first field in IOSArray subclasses.
}

PRIMITIVE_ARRAY_INTERFACE(short, Short, short)

@end


// ********** IOSIntArray **********

@interface IOSIntArray : IOSArray {
 @public
  int *buffer_;  // java.nio requires this be first field in IOSArray subclasses.
}

PRIMITIVE_ARRAY_INTERFACE(int, Int, int)

@end


// ********** IOSLongArray **********

@interface IOSLongArray : IOSArray {
 @public
  long long *buffer_;  // java.nio requires this be first field in IOSArray subclasses.
}

PRIMITIVE_ARRAY_INTERFACE(long, Long, long long)

@end


// ********** IOSFloatArray **********

@interface IOSFloatArray : IOSArray {
 @public
  float *buffer_;  // java.nio requires this be first field in IOSArray subclasses.
}

PRIMITIVE_ARRAY_INTERFACE(float, Float, float)

@end


// ********** IOSDoubleArray **********

@interface IOSDoubleArray : IOSArray {
 @public
  double *buffer_;  // java.nio requires this be first field in IOSArray subclasses.
}

PRIMITIVE_ARRAY_INTERFACE(double, Double, double)

@end

#endif // _IOSPrimitiveArray_H_
