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
//  IOSArray.h
//  JreEmulation
//
//  Created by Tom Ball on 6/21/11.
//

#ifndef _IOSARRAY_H
#define _IOSARRAY_H

#import <Foundation/Foundation.h>

@class IOSClass;

// An abstract class that represents a Java array.  Like a Java array,
// an IOSArray is fixed-size but its elements are mutable.
@interface IOSArray : NSObject < NSCopying > {
 @protected
  NSUInteger size_;
}

// Initializes this array with a specified array size.
- (id)initWithLength:(NSUInteger)length;

// Create an empty multi-dimensional array.
+ (id)arrayWithDimensions:(NSUInteger)dimensionCount
                  lengths:(NSUInteger *)dimensionLengths;

// Returns the size of this array.
- (NSUInteger)count;

// Verifies that 0 >= index < length, throwing an IndexOutOfBoundsException
// if index is out of range.
- (void)checkIndex:(NSUInteger)index;

// Verifies that the specified range fits within the array bounds, throwing
// an IndexOutOfBoundsException if it is out of bounds.
- (void)checkRange:(NSRange)range;

// Verifies that the specified range fits within the array bounds after
// applying the specified offset.  An IndexOutOfBoundsException is
// thrown if it is out of bounds.
- (void)checkRange:(NSRange)range withOffset:(NSUInteger)offset;

- (NSString*)descriptionOfElementAtIndex:(NSUInteger)index;

// Returns the element type of this array.
- (IOSClass *)elementType;

// Creates and returns an array containing the values from this array.
- (id)clone;

// Copies a range of elements from this array into another.  This method is
// only called from java.lang.System.arraycopy(), which verifies that the
// destination array is the same type as this array.
- (void) arraycopy:(NSRange)sourceRange
       destination:(IOSArray *)destination
            offset:(NSInteger)offset;

@end

#endif // _IOSARRAY_H
