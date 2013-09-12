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
//  IOSShortArray.h
//  JreEmulation
//
//  Created by Tom Ball on 6/16/11.
//

#ifndef _IOSShortArray_H_
#define _IOSShortArray_H_

#import "IOSArray.h"

// An emulation class that represents a Java short array.  Like a Java array,
// an IOSShortArray is fixed-size but its elements are mutable.
@interface IOSShortArray : IOSArray {
 @public
  short *buffer_;
}

// Create an array from a C short array and length.
- (id)initWithShorts:(const short *)shorts count:(NSUInteger)count;
+ (id)arrayWithShorts:(const short *)shorts count:(NSUInteger)count;

// Return short at a specified index, throws IndexOutOfBoundsException
// if out out range.
FOUNDATION_EXPORT short IOSShortArray_Get(IOSShortArray *array, NSUInteger index);
FOUNDATION_EXPORT short *IOSShortArray_GetRef(IOSShortArray *array, NSUInteger index);
- (short)shortAtIndex:(NSUInteger)index;
- (short *)shortRefAtIndex:(NSUInteger)index;

// Sets short at a specified index, throws IndexOutOfBoundsException
// if out out range.  Returns replacement value.
- (short)replaceShortAtIndex:(NSUInteger)index withShort:(short)value;

// Copies the array contents into a specified buffer, up to the specified
// length.  An IndexOutOfBoundsException is thrown if the specified length
// is greater than the array size.
- (void)getShorts:(short *)buffer length:(NSUInteger)length;

@end

#endif // _IOSShortArray_H_
