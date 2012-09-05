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
//  IOSCharArray.h
//  JreEmulation
//
//  Created by Tom Ball on 6/14/11.
//

#ifndef _IOSCHARARRAY_H
#define _IOSCHARARRAY_H

#import "IOSArray.h"

// An emulation class that represents a Java char array.  Like a Java array,
// an IOSCharArray is fixed-size but its elements are mutable.
@interface IOSCharArray : IOSArray {
 @private
  unichar *buffer_;
}

// Create an array from a C unichar array and length.
- (id)initWithCharacters:(const unichar *)chars count:(NSUInteger)count;
+ (id)arrayWithCharacters:(const unichar *)chars count:(NSUInteger)count;

// Return char at a specified index, throws IndexOutOfBoundsException
// if out out range;
- (unichar)charAtIndex:(NSUInteger)index;

// Sets char at a specified index, throws IndexOutOfBoundsException
// if out out range.  Returns replacement value.
- (unichar)replaceCharAtIndex:(NSUInteger)index withChar:(unichar)c;

// Returns a copy of the array contents.
- (unichar *)getChars;

// Copies the array contents into a specified buffer, up to the specified
// length.  An IndexOutOfBoundsException is thrown if the specified length
// is greater than the array size.
- (void)getChars:(unichar *)buffer length:(NSUInteger)length;

// Increments an array element.
- (unichar)incr:(NSUInteger)index;

// Decrements an array element.
- (unichar)decr:(NSUInteger)index;

// Increments an array element but returns the initial value, like the postfix
// operator.
- (unichar)postIncr:(NSUInteger)index;

// Decrements an array element but returns the initial value, like the postfix
// operator.
- (unichar)postDecr:(NSUInteger)index;

@end

#endif // _IOSCHARARRAY_H
