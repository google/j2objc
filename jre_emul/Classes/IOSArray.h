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
 @public
  NSUInteger size_;
}

// Initializes this array with a specified array size.
- (id)initWithLength:(NSUInteger)length;

+ (id)arrayWithLength:(NSUInteger)length;

// Create an empty multi-dimensional array.
+ (id)arrayWithDimensions:(NSUInteger)dimensionCount
                  lengths:(const int *)dimensionLengths;

+ (id)arrayWithDimensions:(NSUInteger)dimensionCount
                  lengths:(const int *)dimensionLengths
                    types:(__unsafe_unretained IOSClass * const *)componentTypes;

+ (id)iosClass;
+ (id)iosClassWithDimensions:(NSUInteger)dimensions;

// Returns the size of this array.
- (NSUInteger)count;

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

extern void IOSArray_throwOutOfBounds(NSUInteger size, NSUInteger index);

// Implement IOSArray |checkIndex| and |checkRange| methods as C functions. This
// allows IOSArray index and range checks to be completely removed via the
// J2OBJC_DISABLE_ARRAY_CHECKS macro to improve performance.
__attribute__ ((unused))
static inline void IOSArray_checkIndex(NSUInteger size, NSUInteger index) {
#if !defined(J2OBJC_DISABLE_ARRAY_CHECKS)
  if (index >= size) {
    IOSArray_throwOutOfBounds(size, index);
  }
#endif
}
__attribute__ ((unused))
static inline void IOSArray_checkRange(NSUInteger size, NSRange range) {
#if !defined(J2OBJC_DISABLE_ARRAY_CHECKS)
  if (range.length > 0) {
    IOSArray_checkIndex(size, range.location);
    IOSArray_checkIndex(size, range.location + range.length - 1);
  }
#endif
}

#endif // _IOSARRAY_H
