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
//  IOSFloatArray.h
//  JreEmulation
//
//  Created by Tom Ball on 6/16/11.
//

#ifndef _IOSFloatArray_H_
#define _IOSFloatArray_H_

#import "IOSArray.h"

// An emulation class that represents a Java float array.  Like a Java array,
// an IOSFloatArray is fixed-size but its elements are mutable.
@interface IOSFloatArray : IOSArray {
 @public
  float *buffer_;
}

// Create an array from a C float array and length.
- (id)initWithFloats:(const float *)floats count:(NSUInteger)count;
+ (id)arrayWithFloats:(const float *)floats count:(NSUInteger)count;

// Return float at a specified index, throws IndexOutOfBoundsException
// if out out range.
FOUNDATION_EXPORT float IOSFloatArray_Get(IOSFloatArray *array, NSUInteger index);
FOUNDATION_EXPORT float *IOSFloatArray_GetRef(IOSFloatArray *array, NSUInteger index);
- (float)floatAtIndex:(NSUInteger)index;
- (float *)floatRefAtIndex:(NSUInteger)index;

// Sets float at a specified index, throws IndexOutOfBoundsException
// if out out range.  Returns replacement value.
- (float)replaceFloatAtIndex:(NSUInteger)index withFloat:(float)value;

// Copies the array contents into a specified buffer, up to the specified
// length.  An IndexOutOfBoundsException is thrown if the specified length
// is greater than the array size.
- (void)getFloats:(float *)buffer length:(NSUInteger)length;

@end

#endif // _IOSFloatArray_H_
