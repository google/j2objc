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
//  IOSIntArray.h
//  JreEmulation
//
//  Created by Tom Ball on 6/16/11.
//

#ifndef _IOSIntArray_H_
#define _IOSIntArray_H_

#import "IOSArray.h"

// An emulation class that represents a Java int array.  Like a Java array,
// an IOSIntArray is fixed-size but its elements are mutable.
@interface IOSIntArray : IOSArray {
 @public
  int *buffer_;
}

// Create an array from a C int array and length.
- (id)initWithInts:(const int *)ints count:(NSUInteger)count;
+ (id)arrayWithInts:(const int *)ints count:(NSUInteger)count;

// Return int at a specified index, throws IndexOutOfBoundsException
// if out out range;
FOUNDATION_EXPORT int IOSIntArray_Get(IOSIntArray *array, NSUInteger index);
FOUNDATION_EXPORT int *IOSIntArray_GetRef(IOSIntArray *array, NSUInteger index);
- (int)intAtIndex:(NSUInteger)index;
- (int *)intRefAtIndex:(NSUInteger)index;

// Sets int at a specified index, throws IndexOutOfBoundsException
// if out out range. Returns replacement value.
- (int)replaceIntAtIndex:(NSUInteger)index withInt:(int)c;

// Copies the array contents into a specified buffer, up to the specified
// length.  An IndexOutOfBoundsException is thrown if the specified length
// is greater than the array size.
- (void)getInts:(int *)buffer length:(NSUInteger)length;

@end

#endif // _IOSIntArray_H_
