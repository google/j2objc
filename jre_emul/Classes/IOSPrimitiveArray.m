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
//  IOSPrimitiveArray.m
//  JreEmulation
//

#import "IOSPrimitiveArray.h"

#import "IOSClass.h"

/*!
 * Implements the common constructors for the primitive array types.
 * @define PRIMITIVE_ARRAY_CTOR_IMPL
 */
#define PRIMITIVE_ARRAY_CTOR_IMPL(U_NAME, C_TYPE) \
  static IOS##U_NAME##Array *IOS##U_NAME##Array_NewArray(NSUInteger length) { \
    IOS##U_NAME##Array *array = [IOS##U_NAME##Array alloc]; \
    array->size_ = length; \
    array->buffer_ = calloc(length, sizeof(C_TYPE)); \
    return array; \
  } \
  \
  static IOS##U_NAME##Array *IOS##U_NAME##Array_NewArrayWith##U_NAME##s( \
      NSUInteger length, const C_TYPE *buf) { \
    IOS##U_NAME##Array *array = IOS##U_NAME##Array_NewArray(length); \
    memcpy(array->buffer_, buf, length * sizeof(C_TYPE)); \
    return array; \
  } \
  \
  + (id)newArrayWithLength:(NSUInteger)length { \
    return IOS##U_NAME##Array_NewArray(length); \
  } \
  \
  + (id)arrayWithLength:(NSUInteger)length { \
    return [IOS##U_NAME##Array_NewArray(length) autorelease]; \
  } \
  \
  + (id)newArrayWith##U_NAME##s:(const C_TYPE *)buf count:(NSUInteger)count { \
    return IOS##U_NAME##Array_NewArrayWith##U_NAME##s(count, buf); \
  } \
  \
  + (id)arrayWith##U_NAME##s:(const C_TYPE *)buf count:(NSUInteger)count { \
    return [IOS##U_NAME##Array_NewArrayWith##U_NAME##s(count, buf) autorelease]; \
  }

/*!
 * Implements the dealloc method for primitive arrays.
 * @define PRIMITIVE_ARRAY_DEALLOC_IMPL
 */
#define PRIMITIVE_ARRAY_DEALLOC_IMPL \
  - (void)dealloc { \
    free(buffer_); \
    [super dealloc]; \
  } \

/*!
 * Implements the common accessor methods for the primitive array types.
 * @define PRIMITIVE_ARRAY_ACCESSORS_IMPL
 */
#define PRIMITIVE_ARRAY_ACCESSORS_IMPL(L_NAME, U_NAME, C_TYPE) \
  C_TYPE IOS##U_NAME##Array_Get(__unsafe_unretained IOS##U_NAME##Array *array, NSUInteger index) { \
    IOSArray_checkIndex(array->size_, index); \
    return array->buffer_[index]; \
  } \
  \
  C_TYPE *IOS##U_NAME##Array_GetRef( \
      __unsafe_unretained IOS##U_NAME##Array *array, NSUInteger index) { \
    IOSArray_checkIndex(array->size_, index); \
    return &array->buffer_[index]; \
  } \
  \
  - (C_TYPE)L_NAME##AtIndex:(NSUInteger)index { \
    IOSArray_checkIndex(size_, index); \
    return buffer_[index]; \
  } \
  \
  - (C_TYPE *)L_NAME##RefAtIndex:(NSUInteger)index { \
    IOSArray_checkIndex(size_, index); \
    return &buffer_[index]; \
  } \
  \
  - (C_TYPE)replace##U_NAME##AtIndex:(NSUInteger)index with##U_NAME:(C_TYPE)value { \
    IOSArray_checkIndex(size_, index); \
    buffer_[index] = value; \
    return value; \
  } \
  \
  - (void)get##U_NAME##s:(C_TYPE *)buffer length:(NSUInteger)length { \
    IOSArray_checkIndex(size_, length - 1); \
    memcpy(buffer, buffer_, length * sizeof(C_TYPE)); \
  }

/*!
 * Implements the arraycopy method used by System.arraycopy.
 * @define PRIMITIVE_ARRAY_COPY_IMPL
 */
#define PRIMITIVE_ARRAY_RANGE_COPY_IMPL(U_NAME, C_TYPE) \
  - (void)arraycopy:(NSRange)sourceRange \
        destination:(IOSArray *)destination \
             offset:(NSInteger)offset { \
    IOSArray_checkRange(size_, sourceRange); \
    IOSArray_checkRange(destination->size_, NSMakeRange(offset, sourceRange.length)); \
    memmove(((IOS##U_NAME##Array *) destination)->buffer_ + offset, \
            self->buffer_ + sourceRange.location, \
            sourceRange.length * sizeof(C_TYPE)); \
  }

#define PRIMITIVE_ARRAY_COPY_IMPL(U_NAME) \
  - (id)copyWithZone:(NSZone *)zone { \
    return [IOS##U_NAME##Array newArrayWith##U_NAME##s:buffer_ count:size_]; \
  }

/*!
 * Adds all the common implementations for the primitive array types.
 *
 * @define PRIMITIVE_ARRAY_IMPLEMENTATION
 * @param L_NAME Lowercase name of the primitive type. (e.g. "char")
 * @param U_NAME Uppercase name of the primitive type. (e.g. "Char")
 * @param C_TYPE Objective-C type for the primitive type, (e.g. "unichar")
 */
#define PRIMITIVE_ARRAY_IMPLEMENTATION(L_NAME, U_NAME, C_TYPE) \
  PRIMITIVE_ARRAY_CTOR_IMPL(U_NAME, C_TYPE) \
  PRIMITIVE_ARRAY_ACCESSORS_IMPL(L_NAME, U_NAME, C_TYPE) \
  PRIMITIVE_ARRAY_RANGE_COPY_IMPL(U_NAME, C_TYPE) \
  PRIMITIVE_ARRAY_COPY_IMPL(U_NAME) \
  PRIMITIVE_ARRAY_DEALLOC_IMPL


// ********** IOSBooleanArray **********

@implementation IOSBooleanArray

PRIMITIVE_ARRAY_IMPLEMENTATION(boolean, Boolean, BOOL)

- (NSString *)descriptionOfElementAtIndex:(NSUInteger)index {
  return [NSString stringWithFormat:@"%@", (buffer_[index] ? @"YES" : @"NO")];
}

- (IOSClass *)elementType {
  return [IOSClass booleanClass];
}

+ (IOSClass *)iosClass {
  return [IOSClass arrayClassWithComponentType:[IOSClass booleanClass]];
}

@end


// ********** IOSCharArray **********

@implementation IOSCharArray

PRIMITIVE_ARRAY_IMPLEMENTATION(char, Char, unichar)

+ (id)arrayWithNSString:(NSString *)string {
  NSUInteger length = [string length];
  IOSCharArray *array = IOSCharArray_NewArray(length);
  if (length > 0) {
    [string getCharacters:array->buffer_ range:NSMakeRange(0, length)];
  }
  return [array autorelease];
}

- (unichar *)getChars {
  unichar *result = calloc(size_, sizeof(unichar));
  [self getChars:result length:size_];
  return result;
}

- (NSString *)descriptionOfElementAtIndex:(NSUInteger)index {
  return [NSString stringWithFormat:@"%C", buffer_[index]];
}

- (IOSClass *)elementType {
  return [IOSClass charClass];
}

+ (IOSClass *)iosClass {
  return [IOSClass arrayClassWithComponentType:[IOSClass charClass]];
}

@end


// ********** IOSByteArray **********

@implementation IOSByteArray

PRIMITIVE_ARRAY_IMPLEMENTATION(byte, Byte, char)

- (void)getBytes:(char *)buffer
          offset:(NSUInteger)offset
          length:(NSUInteger)length {
  IOSArray_checkRange(size_, NSMakeRange(offset, length));
  memcpy(buffer, &buffer_[offset], length);
}

- (void)replaceBytes:(const char *)source
              length:(NSUInteger)length
              offset:(NSUInteger)destOffset {
  IOSArray_checkRange(size_, NSMakeRange(destOffset, length));
  memcpy(&buffer_[destOffset], source, length);
}

- (NSString *)descriptionOfElementAtIndex:(NSUInteger)index {
  return [NSString stringWithFormat:@"0x%x", buffer_[index]];
}

- (IOSClass *)elementType {
  return [IOSClass byteClass];
}

+ (IOSClass *)iosClass {
  return [IOSClass arrayClassWithComponentType:[IOSClass byteClass]];
}

- (NSData *)toNSData {
  return [NSData dataWithBytes:buffer_ length:size_];
}

@end


// ********** IOSShortArray **********

@implementation IOSShortArray

PRIMITIVE_ARRAY_IMPLEMENTATION(short, Short, short)

- (NSString *)descriptionOfElementAtIndex:(NSUInteger)index {
  return [NSString stringWithFormat:@"%hi", buffer_[index]];
}

- (IOSClass *)elementType {
  return [IOSClass shortClass];
}

+ (IOSClass *)iosClass {
  return [IOSClass arrayClassWithComponentType:[IOSClass shortClass]];
}

@end


// ********** IOSIntArray **********

@implementation IOSIntArray

PRIMITIVE_ARRAY_IMPLEMENTATION(int, Int, int)

- (NSString *)descriptionOfElementAtIndex:(NSUInteger)index {
  return [NSString stringWithFormat:@"%d", buffer_[index]];
}

- (IOSClass *)elementType {
  return [IOSClass intClass];
}

+ (IOSClass *)iosClass {
  return [IOSClass arrayClassWithComponentType:[IOSClass intClass]];
}

@end


// ********** IOSLongArray **********

@implementation IOSLongArray

PRIMITIVE_ARRAY_IMPLEMENTATION(long, Long, long long)

- (NSString *)descriptionOfElementAtIndex:(NSUInteger)index {
  return [NSString stringWithFormat:@"%lld", buffer_[index]];
}

- (IOSClass *)elementType {
  return [IOSClass longClass];
}

+ (IOSClass *)iosClass {
  return [IOSClass arrayClassWithComponentType:[IOSClass longClass]];
}

@end


// ********** IOSFloatArray **********

@implementation IOSFloatArray

PRIMITIVE_ARRAY_IMPLEMENTATION(float, Float, float)

- (NSString *)descriptionOfElementAtIndex:(NSUInteger)index {
  return [NSString stringWithFormat:@"%g", buffer_[index]];
}

- (IOSClass *)elementType {
  return [IOSClass floatClass];
}

+ (IOSClass *)iosClass {
  return [IOSClass arrayClassWithComponentType:[IOSClass floatClass]];
}

@end


// ********** IOSDoubleArray **********

@implementation IOSDoubleArray

PRIMITIVE_ARRAY_IMPLEMENTATION(double, Double, double)

- (NSString *)descriptionOfElementAtIndex:(NSUInteger)index {
  return [NSString stringWithFormat:@"%g", buffer_[index]];
}

- (IOSClass *)elementType {
  return [IOSClass doubleClass];
}

+ (IOSClass *)iosClass {
  return [IOSClass arrayClassWithComponentType:[IOSClass doubleClass]];
}

@end
