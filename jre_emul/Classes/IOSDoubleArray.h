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
//  IOSDoubleArray.h
//  JreEmulation
//
//  Created by Tom Ball on 6/16/11.
//

#ifndef _IOSDoubleArray_H_
#define _IOSDoubleArray_H_

#import "IOSArray.h"

// An emulation class that represents a Java double array.  Like a Java array,
// an IOSDoubleArray is fixed-size but its elements are mutable.
@interface IOSDoubleArray : IOSArray {
 @public
  double *buffer_;
}

// Create an array from a C double array and length.
- (id)initWithDoubles:(const double *)doubles count:(NSUInteger)count;
+ (id)arrayWithDoubles:(const double *)doubles count:(NSUInteger)count;

// Return double at a specified index, throws IndexOutOfBoundsException
// if out out range.
FOUNDATION_EXPORT double IOSDoubleArray_Get(IOSDoubleArray *array, NSUInteger index);
FOUNDATION_EXPORT double *IOSDoubleArray_GetRef(IOSDoubleArray *array, NSUInteger index);
- (double)doubleAtIndex:(NSUInteger)index;
- (double *)doubleRefAtIndex:(NSUInteger)index;

// Sets double at a specified index, throws IndexOutOfBoundsException
// if out out range.  Returns replacement value.
- (double)replaceDoubleAtIndex:(NSUInteger)index withDouble:(double)value;

// Copies the array contents into a specified buffer, up to the specified
// length.  An IndexOutOfBoundsException is thrown if the specified length
// is greater than the array size.
- (void)getDoubles:(double *)buffer length:(NSUInteger)length;

@end

#endif // _IOSDoubleArray_H_
