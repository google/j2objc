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
//  IOSBooleanArray.h
//  JreEmulation
//
//  Created by Tom Ball on 6/16/11.
//

#ifndef _IOSBooleanArray_H_
#define _IOSBooleanArray_H_

#import "IOSArray.h"

// An emulation class that represents a Java boolean array.  Like a Java array,
// an IOSBooleanArray is fixed-size but its elements are mutable.
@interface IOSBooleanArray : IOSArray {
 @public
  BOOL *buffer_;
}

// Create an array from a Objective-C BOOL array and length.
- (id)initWithBooleans:(const BOOL *)booleans count:(NSUInteger)count;
+ (id)arrayWithBooleans:(const BOOL *)booleans count:(NSUInteger)count;

// Return boolean at a specified index, throws IndexOutOfBoundsException
// if out out range.
FOUNDATION_EXPORT BOOL IOSBooleanArray_Get(IOSBooleanArray *array, NSUInteger index);
FOUNDATION_EXPORT BOOL *IOSBooleanArray_GetRef(IOSBooleanArray *array, NSUInteger index);
- (BOOL)booleanAtIndex:(NSUInteger)index;
- (BOOL *)booleanRefAtIndex:(NSUInteger)index;

// Sets boolean at a specified index, throws IndexOutOfBoundsException
// if out out range.  Returns replacement value.
- (BOOL)replaceBooleanAtIndex:(NSUInteger)index withBoolean:(BOOL)boolean;

// Copies the array contents into a specified buffer, up to the specified
// length.  An IndexOutOfBoundsException is thrown if the specified length
// is greater than the array size.
- (void)getBooleans:(BOOL *)buffer length:(NSUInteger)length;

@end

#endif // _IOSBooleanArray_H_
