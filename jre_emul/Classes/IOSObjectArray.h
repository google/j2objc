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
//  IOSObjectArray.h
//  JreEmulation
//
//  Created by Tom Ball on 9/9/11.
//

#ifndef _IOSObjectArray_H_
#define _IOSObjectArray_H_

#import "IOSArray.h"

@class IOSClass;

// An emulation class that represents a Java object array.  Like a Java array,
// an IOSObjectArray is fixed-size but its elements are mutable.
@interface IOSObjectArray : IOSArray {
 @public
  id __strong *buffer_;
 @private
  IOSClass *elementType_;
}

@property (readonly) IOSClass *elementType;

// Create an array from a C object array, length, and type.
- (id)initWithObjects:(const id *)objects
                count:(NSUInteger)count
                 type:(IOSClass *)type;
+ (id)arrayWithObjects:(const id *)objects
                 count:(NSUInteger)count
                  type:(IOSClass *)type;

// Create an empty array with a type and length.
- (id)initWithLength:(NSUInteger)length type:(IOSClass *)type;
+ (id)arrayWithLength:(NSUInteger)length type:(IOSClass *)type;

// Create an empty multidimensional array.
+ (id)arrayWithDimensions:(NSUInteger)dimensionCount
                  lengths:(const int *)dimensionLengths
                     type:(IOSClass *)type;

+ (id)arrayWithArray:(IOSObjectArray *)array;
+ (id)arrayWithNSArray:(NSArray *)array type:(IOSClass *)type;

+ (id)iosClassWithType:(IOSClass *)type;
+ (id)iosClassWithDimensions:(NSUInteger)dimensions type:(IOSClass *)type;

// Return  at a specified index, throws IndexOutOfBoundsException
// if out out range;
FOUNDATION_EXPORT id IOSObjectArray_Get(IOSObjectArray *array, NSUInteger index);
- (id)objectAtIndex:(NSUInteger)index;

// Sets  at a specified index, throws IndexOutOfBoundsException
// if out out range.  Returns replacement object.
FOUNDATION_EXPORT id IOSObjectArray_Set(IOSObjectArray *array, NSUInteger index, id value);
- (id)replaceObjectAtIndex:(NSUInteger)index withObject:(id)value;

// Copies the array contents into a specified buffer, up to the specified
// length.  An IndexOutOfBoundsException is thrown if the specified length
// is greater than the array size.
- (void)getObjects:(NSObject **)buffer length:(NSUInteger)length;

@end

#endif // _IOSObjectArray_H_
