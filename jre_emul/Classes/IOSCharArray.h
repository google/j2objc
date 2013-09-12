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
 @public
  unichar *buffer_;
}

// Create an array from a C unichar array and length.
- (id)initWithCharacters:(const unichar *)chars count:(NSUInteger)count;
+ (id)arrayWithCharacters:(const unichar *)chars count:(NSUInteger)count;

// Create an array from an NSString.
- (id)initWithNSString:(NSString *)string;
+ (id)arrayWithNSString:(NSString *)string;

// Return char at a specified index, throws IndexOutOfBoundsException
// if out out range;
FOUNDATION_EXPORT unichar IOSCharArray_Get(IOSCharArray *array, NSUInteger index);
FOUNDATION_EXPORT unichar *IOSCharArray_GetRef(IOSCharArray *array, NSUInteger index);
- (unichar)charAtIndex:(NSUInteger)index;
- (unichar *)charRefAtIndex:(NSUInteger)index;

// Sets char at a specified index, throws IndexOutOfBoundsException
// if out out range.  Returns replacement value.
- (unichar)replaceCharAtIndex:(NSUInteger)index withChar:(unichar)c;

// Returns a copy of the array contents.
- (unichar *)getChars;

// Copies the array contents into a specified buffer, up to the specified
// length.  An IndexOutOfBoundsException is thrown if the specified length
// is greater than the array size.
- (void)getChars:(unichar *)buffer length:(NSUInteger)length;

@end

#endif // _IOSCHARARRAY_H
