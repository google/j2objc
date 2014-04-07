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
 * Implements the common initializers for the primitive array types.
 * @define PRIMITIVE_ARRAY_INIT_IMPL
 */
#define PRIMITIVE_ARRAY_INIT_IMPL(U_NAME, C_TYPE) \
  - (id)initWithLength:(NSUInteger)length { \
    if ((self = [super initWithLength:length])) { \
      buffer_ = calloc(length, sizeof(C_TYPE)); \
    } \
    return self; \
  } \
  \
  - (id)initWith##U_NAME##s:(const C_TYPE *)buf count:(NSUInteger)count { \
    if ((self = [self initWithLength:count])) { \
      if (buf != nil) { \
        memcpy(buffer_, buf, count * sizeof(C_TYPE)); \
      } \
    } \
    return self; \
  }

/*!
 * Implements the common constructors for the primitive array types.
 * @define PRIMITIVE_ARRAY_CTOR_IMPL
 */
#define PRIMITIVE_ARRAY_CTOR_IMPL(U_NAME, C_TYPE) \
  + (id)arrayWith##U_NAME##s:(const C_TYPE *)buf count:(NSUInteger)count { \
    return AUTORELEASE([[IOS##U_NAME##Array alloc] initWith##U_NAME##s:buf count:count]); \
  }

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
#define PRIMITIVE_ARRAY_COPY_IMPL(U_NAME, C_TYPE) \
  - (void)arraycopy:(NSRange)sourceRange \
        destination:(IOSArray *)destination \
             offset:(NSInteger)offset { \
    IOSArray_checkRange(size_, sourceRange); \
    IOSArray_checkRange(destination->size_, NSMakeRange(offset, sourceRange.length)); \
    memmove(((IOS##U_NAME##Array *) destination)->buffer_ + offset, \
            self->buffer_ + sourceRange.location, \
            sourceRange.length * sizeof(C_TYPE)); \
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
  PRIMITIVE_ARRAY_INIT_IMPL(U_NAME, C_TYPE) \
  PRIMITIVE_ARRAY_CTOR_IMPL(U_NAME, C_TYPE) \
  PRIMITIVE_ARRAY_ACCESSORS_IMPL(L_NAME, U_NAME, C_TYPE) \
  PRIMITIVE_ARRAY_COPY_IMPL(U_NAME, C_TYPE)


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

- (id)copyWithZone:(NSZone *)zone {
  return [[IOSBooleanArray allocWithZone:zone]
          initWithBooleans:buffer_ count:size_];
}

- (void)dealloc {
  free(buffer_);
  [super dealloc];
}

@end


// ********** IOSCharArray **********

@implementation IOSCharArray

PRIMITIVE_ARRAY_IMPLEMENTATION(char, Char, unichar)

- (id)initWithNSString:(NSString *)string {
  int length = [string length];
  if ((self = [super initWithLength:length])) {
    if (length > 0) {
      buffer_ = malloc(length * sizeof(unichar));
      [string getCharacters:buffer_ range:NSMakeRange(0, length)];
    }
  }
  return self;
}

+ (id)arrayWithNSString:(NSString *)string {
  return AUTORELEASE([[IOSCharArray alloc] initWithNSString:string]);
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

- (id)copyWithZone:(NSZone *)zone {
  return [[IOSCharArray allocWithZone:zone] initWithChars:buffer_ count:size_];
}

- (void)dealloc {
  free(buffer_);
  [super dealloc];
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

- (id)copyWithZone:(NSZone *)zone {
  return [[IOSByteArray allocWithZone:zone]
          initWithBytes:buffer_ count:size_];
}

- (NSData *)toNSData {
  return [NSData dataWithBytes:buffer_ length:size_];
}

- (void)dealloc {
  free(buffer_);
  [super dealloc];
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

- (id)copyWithZone:(NSZone *)zone {
  return [[IOSShortArray allocWithZone:zone] initWithShorts:buffer_ count:size_];
}

- (void)dealloc {
  free(buffer_);
  [super dealloc];
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

- (id)copyWithZone:(NSZone *)zone {
  return [[IOSIntArray allocWithZone:zone] initWithInts:buffer_ count:size_];
}

- (void)dealloc {
  free(buffer_);
  [super dealloc];
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

- (id)copyWithZone:(NSZone *)zone {
  return [[IOSLongArray allocWithZone:zone] initWithLongs:buffer_ count:size_];
}

- (void)dealloc {
  free(buffer_);
  [super dealloc];
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

- (id)copyWithZone:(NSZone *)zone {
  return [[IOSFloatArray allocWithZone:zone]
          initWithFloats:buffer_ count:size_];
}

- (void)dealloc {
  free(buffer_);
  [super dealloc];
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

- (id)copyWithZone:(NSZone *)zone {
  return [[IOSDoubleArray allocWithZone:zone]
          initWithDoubles:buffer_ count:size_];
}

- (void)dealloc {
  free(buffer_);
  [super dealloc];
}

@end
