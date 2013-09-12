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
//  IOSLongArray.h
//  JreEmulation
//
//  Created by Tom Ball on 6/16/11.
//

#ifndef _IOSLongArray_H_
#define _IOSLongArray_H_

#import "IOSArray.h"

// An emulation class that represents a Java long array.  Like a Java array,
// an IOSLongArray is fixed-size but its elements are mutable.
@interface IOSLongArray : IOSArray {
 @public
  long long *buffer_;
}

// Create an array from a C long array and length.
- (id)initWithLongs:(const long long *)longs count:(NSUInteger)count;
+ (id)arrayWithLongs:(const long long *)longs count:(NSUInteger)count;

// Return long at a specified index, throws IndexOutOfBoundsException
// if out out range.
FOUNDATION_EXPORT long long IOSLongArray_Get(IOSLongArray *array, NSUInteger index);
FOUNDATION_EXPORT long long *IOSLongArray_GetRef(IOSLongArray *array, NSUInteger index);
- (long long)longAtIndex:(NSUInteger)index;
- (long long *)longRefAtIndex:(NSUInteger)index;

// Sets long at a specified index, throws IndexOutOfBoundsException
// if out out range.  Returns replacement value.
- (long long)replaceLongAtIndex:(NSUInteger)index withLong:(long long)value;

// Copies the array contents into a specified buffer, up to the specified
// length.  An IndexOutOfBoundsException is thrown if the specified length
// is greater than the array size.
- (void)getLongs:(long long *)buffer length:(NSUInteger)length;

@end

#endif // _IOSLongArray_H_
