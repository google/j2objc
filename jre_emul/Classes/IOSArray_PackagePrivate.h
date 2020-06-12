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
//  IOSArray_PackagePrivate.h
//  JreEmulation
//

#ifndef IOSArray_Package_Private_H
#define IOSArray_Package_Private_H

#include "IOSArray.h"

@interface IOSArray(PackagePrivate)

/** Create an empty multi-dimensional array. */
+ (id)arrayWithDimensions:(NSUInteger)dimensionCount
                  lengths:(const jint *)dimensionLengths;

/** Create an auto-released empty multi-dimensional array. */
+ (id)newArrayWithDimensions:(NSUInteger)dimensionCount
                     lengths:(const jint *)dimensionLengths
    __attribute__((objc_method_family(none), ns_returns_retained));
// We must set the above method family to "none" because as a "new" method
// family clang will assume the return type to be the same type as the class
// being called.

// Copies a range of elements from this array into another.  This method is
// only called from java.lang.System.arraycopy(), which verifies that the
// destination array is the same type as this array.
- (void)arraycopy:(jint)offset
      destination:(IOSArray *)destination
        dstOffset:(jint)dstOffset
           length:(jint)length;

+ (id)iosClass;

@end

/**
 * Create an empty multi-dimensional array of a specified type.
 */
FOUNDATION_EXPORT id IOSArray_NewArrayWithDimensions(
    Class self, NSUInteger dimensionCount, const jint *dimensionLengths, IOSClass *type);

#endif // IOSArray_Package_Private_H
