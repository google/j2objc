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
//  IOSArrayTest.m
//  JreEmulation
//

#import <XCTest/XCTest.h>
#import <objc/runtime.h>
#include "IOSArray.h"
#include "IOSClass.h"
#include "IOSPrimitiveArray.h"
#include "J2ObjC_source.h"
#include "java/lang/IndexOutOfBoundsException.h"
#include "java/util/Calendar.h"
#include "java/util/Date.h"

// Unit tests for IOSArray.
@interface IOSArrayTest : XCTestCase
@end


@implementation IOSArrayTest

- (void)testCheckIndex {
  XCTAssertNoThrowSpecific(IOSArray_checkIndex(2, 0),
                          JavaLangIndexOutOfBoundsException,
                          @"Exception was thrown for a valid index");
  XCTAssertNoThrowSpecific(IOSArray_checkIndex(2, 1),
                          JavaLangIndexOutOfBoundsException,
                          @"Exception was thrown for a valid index");
  XCTAssertThrowsSpecific(IOSArray_checkIndex(2, -1),
                         JavaLangIndexOutOfBoundsException,
                         @"Exception was not thrown for an invalid index");
  XCTAssertThrowsSpecific(IOSArray_checkIndex(2, 2),
                         JavaLangIndexOutOfBoundsException,
                         @"Exception was not thrown for an invalid index");
}

- (void)testCheckRange {
  XCTAssertNoThrowSpecific(IOSArray_checkRange(2, 0, 1),
                          JavaLangIndexOutOfBoundsException,
                          @"Exception was thrown for a valid index");
  XCTAssertNoThrowSpecific(IOSArray_checkRange(2, 0, 2),
                          JavaLangIndexOutOfBoundsException,
                          @"Exception was thrown for a valid index");
  XCTAssertNoThrowSpecific(IOSArray_checkRange(2, 1, 1),
                          JavaLangIndexOutOfBoundsException,
                          @"Exception was thrown for a valid index");
  XCTAssertThrowsSpecific(IOSArray_checkRange(2, -1, 2),
                          JavaLangIndexOutOfBoundsException,
                          @"Exception was not thrown for an invalid index");
  XCTAssertThrowsSpecific(IOSArray_checkRange(2, 0, 3),
                         JavaLangIndexOutOfBoundsException,
                         @"Exception was not thrown for an invalid index");
  XCTAssertThrowsSpecific(IOSArray_checkRange(2, 1, 2),
                         JavaLangIndexOutOfBoundsException,
                         @"Exception was not thrown for an invalid index");
  XCTAssertNoThrow(IOSArray_checkRange(2, 2, 0),
                         @"Exception was thrown for a zero length range");
}

- (void)testBooleanArrayCopy {
  const jboolean *bools = (jboolean[]){ TRUE, FALSE, TRUE, FALSE };
  IOSBooleanArray *a1 = [IOSBooleanArray arrayWithBooleans:bools count:4];
  IOSBooleanArray *a2 = AUTORELEASE([a1 copy]);
  XCTAssertEqual([a1 length], [a2 length], @"bad array size");
  for (jint i = 0; i < 4; i++) {
    XCTAssertEqual([a1 booleanAtIndex:i], [a2 booleanAtIndex:i],
                   @"bad IOSBooleanArray element");
  }
}

- (void)testByteArrayCopy {
  const jbyte *bytes = (jbyte[]){ 1, 2, 3, 4 };
  IOSByteArray *a1 = [IOSByteArray arrayWithBytes:bytes count:4];
  IOSByteArray *a2 = AUTORELEASE([a1 copy]);
  XCTAssertEqual([a1 length], [a2 length], @"bad array size");
  for (jint i = 0; i < 4; i++) {
    XCTAssertEqual([a1 byteAtIndex:i], [a2 byteAtIndex:i],
                   @"bad IOSByteArray element");
  }
}

- (void)testCharArrayCopy {
  const jchar *chars = (jchar[]){ 'a', 'b', 'c', 'd' };
  IOSCharArray *a1 = [IOSCharArray arrayWithChars:chars count:4];
  IOSCharArray *a2 = AUTORELEASE([a1 copy]);
  XCTAssertEqual([a1 length], [a2 length], @"bad array size");
  for (jint i = 0; i < 4; i++) {
    XCTAssertEqual([a1 charAtIndex:i], [a2 charAtIndex:i],
                   @"bad IOSCharArray element");
  }
}

- (void)testDoubleArrayCopy {
  const jdouble *doubles = (jdouble[]){ 1.1, 2.2, 3.3, 4.4 };
  IOSDoubleArray *a1 = [IOSDoubleArray arrayWithDoubles:doubles count:4];
  IOSDoubleArray *a2 = AUTORELEASE([a1 copy]);
  XCTAssertEqual([a1 length], [a2 length], @"bad array size");
  for (jint i = 0; i < 4; i++) {
    XCTAssertEqual([a1 doubleAtIndex:i], [a2 doubleAtIndex:i],
                   @"bad IOSDoubleArray element");
  }
}

- (void)testFloatArrayCopy {
  const float *floats = (float[]){ 1.1f, 2.2f, 3.3f, 4.4f };
  IOSFloatArray *a1 = [IOSFloatArray arrayWithFloats:floats count:4];
  IOSFloatArray *a2 = AUTORELEASE([a1 copy]);
  XCTAssertEqual([a1 length], [a2 length], @"bad array size");
  for (jint i = 0; i < 4; i++) {
    XCTAssertEqual([a1 floatAtIndex:i], [a2 floatAtIndex:i],
                   @"bad IOSFloatArray element");
  }
}

- (void)testIntArrayCopy {
  const int *ints = (int[]){ 1, 2, 3, 4 };
  IOSIntArray *a1 = [IOSIntArray arrayWithInts:ints count:4];
  IOSIntArray *a2 = AUTORELEASE([a1 copy]);
  XCTAssertEqual([a1 length], [a2 length], @"bad array size");
  for (jint i = 0; i < 4; i++) {
    XCTAssertEqual([a1 intAtIndex:i], [a2 intAtIndex:i],
                   @"bad IOSIntArray element");
  }
}

- (void)testLongArrayCopy {
  const jlong *longs = (jlong[]){ 1, 2, 3, 4 };
  IOSLongArray *a1 = [IOSLongArray arrayWithLongs:longs count:4];
  IOSLongArray *a2 = AUTORELEASE([a1 copy]);
  XCTAssertEqual([a1 length], [a2 length], @"bad array size");
  for (jint i = 0; i < 4; i++) {
    XCTAssertEqual([a1 longAtIndex:i], [a2 longAtIndex:i],
                   @"bad IOSLongArray element");
  }
}

- (void)testShortArrayCopy {
  const jshort *shorts = (jshort[]){ 1, 2, 3, 4 };
  IOSShortArray *a1 = [IOSShortArray arrayWithShorts:shorts count:4];
  IOSShortArray *a2 = AUTORELEASE([a1 copy]);
  XCTAssertEqual([a1 length], [a2 length], @"bad array size");
  for (jint i = 0; i < 4; i++) {
    XCTAssertEqual([a1 shortAtIndex:i], [a2 shortAtIndex:i],
                   @"bad IOSShortArray element");
  }
}

// Booleans are tested here, but all primitive types work because the code
// being tested is in IOSArray, which uses [self class] in its initializer
// to work correctly with sub-types.
- (void)testBooleanMultiDimensionalCreate {
  // Verify single dimension array is correct type.
  id array =
      [IOSBooleanArray arrayWithDimensions:1 lengths:(int[]){2}];
  XCTAssertTrue([array isMemberOfClass:[IOSBooleanArray class]],
               @"wrong array type: %@", [array class]);

  // Verify multiple dimension array is an array of arrays (of arrays).
  array =
      [IOSBooleanArray arrayWithDimensions:3 lengths:(int[]){2, 4, 6}];
  XCTAssertTrue([array isMemberOfClass:[IOSObjectArray class]],
               @"wrong array type: %@", [array class]);
  XCTAssertTrue([array length] == 2, @"invalid array length");
  for (jint i = 0; i < 2; i++) {
    id subarray = [array objectAtIndex:i];
    XCTAssertTrue([subarray isMemberOfClass:[IOSObjectArray class]],
                 @"wrong subarray type: %@", [subarray class]);
    XCTAssertTrue([subarray length] == 4, @"invalid subarray length");
    for (jint i = 0; i < 4; i++) {
      id subsubarray = [subarray objectAtIndex:i];
      XCTAssertTrue([subsubarray isMemberOfClass:[IOSBooleanArray class]],
                   @"wrong subarray type: %@", [subarray class]);
      XCTAssertTrue([subsubarray length] == 6, @"invalid subarray length");
    }
  }
}

// Objects are separately tested, because unlike primitive types, object arrays
// need a specified element type.
- (void)testObjectMultiDimensionalCreate {
  IOSClass *type = JavaUtilDate_class_();

  // Verify single dimension array is correct type.
  IOSObjectArray *array = [IOSObjectArray arrayWithDimensions:1
                                                      lengths:(int[]){2}
                                                         type:type];
  XCTAssertEqualObjects([array elementType], type,
                       @"wrong element type: %@", [array elementType]);

  // Verify multiple dimension array is an array of arrays (of arrays).
  array = [IOSObjectArray arrayWithDimensions:3
                                      lengths:(int[]){2, 4, 6}
                                         type:type];
  XCTAssertTrue([array isMemberOfClass:[IOSObjectArray class]],
               @"wrong array type: %@", [array class]);
  XCTAssertTrue([array length] == 2,
               @"invalid array length, was %d", (jint)[array length]);
  for (jint i = 0; i < 2; i++) {
    IOSObjectArray *subarray = [array objectAtIndex:i];
    XCTAssertTrue([subarray isMemberOfClass:[IOSObjectArray class]],
                 @"wrong subarray type: %@", [subarray class]);
    XCTAssertTrue([subarray length] == 4,
                 @"invalid subarray length, was %d", (jint)[subarray length]);
    for (jint i = 0; i < 4; i++) {
      IOSArray *subsubarray = [subarray objectAtIndex:i];
      XCTAssertTrue([subsubarray isMemberOfClass:[IOSObjectArray class]],
                   @"wrong subarray type: %@", [subarray class]);
      XCTAssertTrue([subsubarray length] == 6,
                   @"invalid subsubarray length, was %d", (jint)[subsubarray length]);
      XCTAssertEqualObjects([subsubarray elementType], type,
                           @"wrong array type: %@", [subsubarray elementType]);
    }
  }
}

// Verify that array type classes can be compared correctly.
- (void)testIsEqual {
  // Verify primitive array types are equal ...
  IOSBooleanArray *boolArray1 = [IOSBooleanArray arrayWithLength:0];
  IOSBooleanArray *boolArray2 = [IOSBooleanArray arrayWithLength:10];
  XCTAssertTrue([[boolArray1 java_getClass] isEqual:[boolArray2 java_getClass]],
               @"boolean array types not equal");
  IOSIntArray *intArray1 = [IOSIntArray arrayWithLength:0];
  IOSIntArray *intArray2 = [IOSIntArray arrayWithLength:10];
  XCTAssertTrue([[intArray1 java_getClass] isEqual:[intArray2 java_getClass]],
               @"int array types not equal");

  // ... but not to each other.
  XCTAssertFalse([[boolArray1 java_getClass] isEqual:[intArray2 java_getClass]],
                @"different primitive array types equal");

  // Verify object array types are equal only if their element type is equal.
  IOSObjectArray *dateArray1 = [IOSObjectArray arrayWithLength:0 type:JavaUtilDate_class_()];
  IOSObjectArray *dateArray2 = [IOSObjectArray arrayWithLength:10 type:JavaUtilDate_class_()];
  IOSObjectArray *calArray = [IOSObjectArray arrayWithLength:0 type:JavaUtilCalendar_class_()];
  XCTAssertTrue([[dateArray1 java_getClass] isEqual:[dateArray2 java_getClass]],
               @"Date array types not equal");
  XCTAssertFalse([[dateArray1 java_getClass] isEqual:[calArray java_getClass]],
                @"different object array types equal");
}

// Verify that all elements in a newly created empty object array are nil.
- (void)testNilObjectArrayElements {
  const int arraySize = 1000;
  IOSObjectArray *array = [IOSObjectArray arrayWithLength:arraySize type:NSString_class_()];
  for (int i = 0; i < arraySize; i++) {
    XCTAssertNil(array->buffer_[i], @"empty array element was non-nil");
  }
}

@end
