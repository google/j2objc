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

#import "IOSArray_PackagePrivate.h"
#import "IOSClass.h"
#import "java/lang/NegativeArraySizeException.h"

/*!
 * Implements the common constructors for the primitive array types.
 * @define PRIMITIVE_ARRAY_CTOR_IMPL
 */
#define PRIMITIVE_ARRAY_CTOR_IMPL(U_NAME, C_TYPE) \
  static IOS##U_NAME##Array *IOS##U_NAME##Array_NewArray(jint length) { \
    if (length < 0) { \
      @throw AUTORELEASE([[JavaLangNegativeArraySizeException alloc] init]); \
    } \
    size_t buf_size = length * sizeof(C_TYPE); \
    IOS##U_NAME##Array *array = NSAllocateObject( \
        [IOS##U_NAME##Array class], buf_size, nil); \
    memset(array->buffer_, 0, buf_size); \
    array->size_ = length; \
    return array; \
  } \
  \
  static IOS##U_NAME##Array *IOS##U_NAME##Array_NewArrayWith##U_NAME##s( \
      jint length, const C_TYPE *buf) { \
    IOS##U_NAME##Array *array = IOS##U_NAME##Array_NewArray(length); \
    memcpy(array->buffer_, buf, length * sizeof(C_TYPE)); \
    return array; \
  } \
  \
  + (instancetype)newArrayWithLength:(NSUInteger)length { \
    return IOS##U_NAME##Array_NewArray((jint)length); \
  } \
  \
  + (instancetype)arrayWithLength:(NSUInteger)length { \
    return AUTORELEASE(IOS##U_NAME##Array_NewArray((jint)length)); \
  } \
  \
  + (instancetype)newArrayWith##U_NAME##s:(const C_TYPE *)buf count:(NSUInteger)count { \
    return IOS##U_NAME##Array_NewArrayWith##U_NAME##s((jint)count, buf); \
  } \
  \
  + (instancetype)arrayWith##U_NAME##s:(const C_TYPE *)buf count:(NSUInteger)count { \
    return AUTORELEASE(IOS##U_NAME##Array_NewArrayWith##U_NAME##s((jint)count, buf)); \
  } \
  \
  + (id)arrayWithDimensions:(NSUInteger)dimensionCount lengths:(const jint *)dimensionLengths { \
    return AUTORELEASE(IOSArray_NewArrayWithDimensions(self, dimensionCount, dimensionLengths, nil)\
        ); \
  } \
+ (id)newArrayWithDimensions:(NSUInteger)dimensionCount lengths:(const jint *)dimensionLengths \
    __attribute__((objc_method_family(none), ns_returns_retained)) { \
    return IOSArray_NewArrayWithDimensions(self, dimensionCount, dimensionLengths, nil); \
  }

/*!
 * Implements the common accessor methods for the primitive array types.
 * @define PRIMITIVE_ARRAY_ACCESSORS_IMPL
 */
#define PRIMITIVE_ARRAY_ACCESSORS_IMPL(L_NAME, U_NAME, C_TYPE) \
  - (C_TYPE)L_NAME##AtIndex:(NSUInteger)index { \
    IOSArray_checkIndex(size_, (jint)index); \
    return buffer_[index]; \
  } \
  \
  - (C_TYPE *)L_NAME##RefAtIndex:(NSUInteger)index { \
    IOSArray_checkIndex(size_, (jint)index); \
    return &buffer_[index]; \
  } \
  \
  - (C_TYPE)replace##U_NAME##AtIndex:(NSUInteger)index with##U_NAME:(C_TYPE)value { \
    IOSArray_checkIndex(size_, (jint)index); \
    buffer_[index] = value; \
    return value; \
  } \
  \
  - (void)get##U_NAME##s:(C_TYPE *)buffer length:(NSUInteger)length { \
    IOSArray_checkIndex(size_, (jint)length - 1); \
    memcpy(buffer, buffer_, length * sizeof(C_TYPE)); \
  } \
  \
  - (void *)buffer { \
    return buffer_; \
  } \
  \
  - (IOSClass *)elementType { \
    return [IOSClass L_NAME##Class]; \
  } \
  \
  + (IOSClass *)iosClass { \
    return IOSClass_arrayOf([IOSClass L_NAME##Class]); \
  }

/*!
 * Implements the arraycopy method used by System.arraycopy.
 * @define PRIMITIVE_ARRAY_COPY_IMPL
 */
#define PRIMITIVE_ARRAY_RANGE_COPY_IMPL(U_NAME, C_TYPE) \
  - (void)arraycopy:(jint)offset \
        destination:(IOSArray *)destination \
          dstOffset:(jint)dstOffset \
             length:(jint)length { \
    IOSArray_checkRange(size_, offset, length); \
    IOSArray_checkRange(destination->size_, dstOffset, length); \
    memmove(((IOS##U_NAME##Array *) destination)->buffer_ + dstOffset, \
            self->buffer_ + offset, length * sizeof(C_TYPE)); \
  }

#define PRIMITIVE_ARRAY_COPY_IMPL(U_NAME) \
  - (instancetype)copyWithZone:(NSZone *)zone { \
    return [IOS##U_NAME##Array newArrayWith##U_NAME##s:buffer_ count:size_]; \
  }

/*!
 * Adds all the common implementations for the primitive array types.
 *
 * @define PRIMITIVE_ARRAY_IMPLEMENTATION
 * @param L_NAME Lowercase name of the primitive type. (e.g. "char")
 * @param U_NAME Uppercase name of the primitive type. (e.g. "Char")
 * @param C_TYPE Objective-C type for the primitive type, (e.g. "jchar")
 */
#define PRIMITIVE_ARRAY_IMPLEMENTATION(L_NAME, U_NAME, C_TYPE) \
  PRIMITIVE_ARRAY_CTOR_IMPL(U_NAME, C_TYPE) \
  PRIMITIVE_ARRAY_ACCESSORS_IMPL(L_NAME, U_NAME, C_TYPE) \
  PRIMITIVE_ARRAY_RANGE_COPY_IMPL(U_NAME, C_TYPE) \
  PRIMITIVE_ARRAY_COPY_IMPL(U_NAME)


// ********** IOSBooleanArray **********

@implementation IOSBooleanArray

PRIMITIVE_ARRAY_IMPLEMENTATION(boolean, Boolean, jboolean)

- (NSString *)descriptionOfElementAtIndex:(jint)index {
  return [NSString stringWithFormat:@"%@", (buffer_[index] ? @"true" : @"false")];
}

@end


// ********** IOSCharArray **********

@implementation IOSCharArray

PRIMITIVE_ARRAY_IMPLEMENTATION(char, Char, jchar)

+ (instancetype)arrayWithNSString:(NSString *)string {
  NSUInteger length = [string length];
  IOSCharArray *array = IOSCharArray_NewArray((jint)length);
  if (length > 0) {
    [string getCharacters:array->buffer_ range:NSMakeRange(0, length)];
  }
  return AUTORELEASE(array);
}

- (NSString *)descriptionOfElementAtIndex:(jint)index {
  return [NSString stringWithFormat:@"%C", buffer_[index]];
}

@end


// ********** IOSByteArray **********

@implementation IOSByteArray

PRIMITIVE_ARRAY_IMPLEMENTATION(byte, Byte, jbyte)

+ (instancetype)arrayWithNSData:(NSData *)data {
  NSUInteger length = [data length];
  IOSByteArray *array = IOSByteArray_NewArray((jint)length);
  if (length > 0) {
    [data getBytes:array->buffer_ length:length];
  }
  return AUTORELEASE(array);
}

- (void)getBytes:(jbyte *)buffer
          offset:(jint)offset
          length:(jint)length {
  IOSArray_checkRange(size_, (jint)offset, (jint)length);
  memcpy(buffer, &buffer_[offset], length);
}

- (void)replaceBytes:(const jbyte *)source
              length:(jint)length
              offset:(jint)destOffset {
  IOSArray_checkRange(size_, (jint)destOffset, (jint)length);
  memcpy(&buffer_[destOffset], source, length);
}

- (NSString *)descriptionOfElementAtIndex:(jint)index {
  return [NSString stringWithFormat:@"0x%x", buffer_[index]];
}

- (NSData *)toNSData {
  return [NSData dataWithBytes:buffer_ length:size_];
}

@end


// ********** IOSShortArray **********

@implementation IOSShortArray

PRIMITIVE_ARRAY_IMPLEMENTATION(short, Short, jshort)

- (NSString *)descriptionOfElementAtIndex:(jint)index {
  return [NSString stringWithFormat:@"%hi", buffer_[index]];
}

@end


// ********** IOSIntArray **********

@implementation IOSIntArray

PRIMITIVE_ARRAY_IMPLEMENTATION(int, Int, jint)

- (NSString *)descriptionOfElementAtIndex:(jint)index {
  return [NSString stringWithFormat:@"%d", buffer_[index]];
}

@end


// ********** IOSLongArray **********

@implementation IOSLongArray

PRIMITIVE_ARRAY_IMPLEMENTATION(long, Long, jlong)

- (NSString *)descriptionOfElementAtIndex:(jint)index {
  return [NSString stringWithFormat:@"%lld", buffer_[index]];
}

@end


// ********** IOSFloatArray **********

@implementation IOSFloatArray

PRIMITIVE_ARRAY_IMPLEMENTATION(float, Float, jfloat)

- (NSString *)descriptionOfElementAtIndex:(jint)index {
  return [NSString stringWithFormat:@"%g", buffer_[index]];
}

@end


// ********** IOSDoubleArray **********

@implementation IOSDoubleArray

PRIMITIVE_ARRAY_IMPLEMENTATION(double, Double, jdouble)

- (NSString *)descriptionOfElementAtIndex:(jint)index {
  return [NSString stringWithFormat:@"%g", buffer_[index]];
}

@end
