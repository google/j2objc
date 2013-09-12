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
//  IOSByteArray.h
//  JreEmulation
//
//  Created by Tom Ball on 6/16/11.
//

#ifndef _IOSBYTEARRAY_H
#define _IOSBYTEARRAY_H

#import "IOSArray.h"

// An emulation class that represents a Java byte array.  Like a Java array,
// an IOSByteArray is fixed-size but its elements are mutable.
@interface IOSByteArray : IOSArray {
 @public
  char *buffer_;
}

// Create an array from a C char array and length.
- (id)initWithBytes:(const char *)ints count:(NSUInteger)count;
+ (id)arrayWithBytes:(const char *)ints count:(NSUInteger)count;

// Return byte at a specified index, throws IndexOutOfBoundsException
// if out out range.
FOUNDATION_EXPORT char IOSByteArray_Get(IOSByteArray *array, NSUInteger index);
FOUNDATION_EXPORT char *IOSByteArray_GetRef(IOSByteArray *array, NSUInteger index);
- (char)byteAtIndex:(NSUInteger)index;
- (char *)byteRefAtIndex:(NSUInteger)index;

// Sets byte at a specified index, throws IndexOutOfBoundsException
// if out out range.  Returns the replacement value.
- (char)replaceByteAtIndex:(NSUInteger)index withByte:(char)byte;

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

#endif // _IOSBYTEARRAY_H
