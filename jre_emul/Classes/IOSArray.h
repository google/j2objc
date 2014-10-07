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

#import "J2ObjC_common.h"

@class IOSClass;

// An abstract class that represents a Java array.  Like a Java array,
// an IOSArray is fixed-size but its elements are mutable.
@interface IOSArray : NSObject < NSCopying > {
 @public
  jint size_;
}

// Create an empty multi-dimensional array.
+ (id)arrayWithDimensions:(NSUInteger)dimensionCount
                  lengths:(const jint *)dimensionLengths;
// We must set the method family to "none" because as a "new" method family
// clang will assume the return type to be the same type as the class being
// called.
+ (id)newArrayWithDimensions:(NSUInteger)dimensionCount
                     lengths:(const jint *)dimensionLengths
    __attribute__((objc_method_family(none), ns_returns_retained));

+ (id)iosClass;
+ (id)iosClassWithDimensions:(NSUInteger)dimensions;

// Returns the size of this array.
- (jint)length;
// DEPRECATED: Use length instead.
- (NSUInteger)count __attribute__((deprecated));

- (NSString *)descriptionOfElementAtIndex:(jint)index;

// Returns the element type of this array.
- (IOSClass *)elementType;

// Creates and returns an array containing the values from this array.
- (id)clone;

// Copies a range of elements from this array into another.  This method is
// only called from java.lang.System.arraycopy(), which verifies that the
// destination array is the same type as this array.
- (void)arraycopy:(jint)offset
      destination:(IOSArray *)destination
        dstOffset:(jint)dstOffset
           length:(jint)length;

@end

extern void IOSArray_throwOutOfBounds();
extern void IOSArray_throwOutOfBoundsWithMsg(jint size, jint index);

// Implement IOSArray |checkIndex| and |checkRange| methods as C functions. This
// allows IOSArray index and range checks to be completely removed via the
// J2OBJC_DISABLE_ARRAY_CHECKS macro to improve performance.
__attribute__ ((unused))
static inline void IOSArray_checkIndex(jint size, jint index) {
#if !defined(J2OBJC_DISABLE_ARRAY_CHECKS)
  if (index < 0 || index >= size) {
    IOSArray_throwOutOfBoundsWithMsg(size, index);
  }
#endif
}
__attribute__ ((unused))
static inline void IOSArray_checkRange(jint size, jint offset, jint length) {
#if !defined(J2OBJC_DISABLE_ARRAY_CHECKS)
  if (length < 0 || offset < 0 || offset + length > size) {
    IOSArray_throwOutOfBounds();
  }
#endif
}

#endif // _IOSARRAY_H
