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
//  Created by Tom Ball on 7/6/11.
//

#import <SenTestingKit/SenTestingKit.h>
#import <objc/runtime.h>
#import "IOSArray.h"
#import "IOSBooleanArray.h"
#import "IOSByteArray.h"
#import "IOSCharArray.h"
#import "IOSDoubleArray.h"
#import "IOSFloatArray.h"
#import "IOSIntArray.h"
#import "IOSLongArray.h"
#import "IOSShortArray.h"
#import "java/lang/IndexOutOfBoundsException.h"
#import "java/util/Date.h"

// Unit tests for IOSArray.
@interface IOSArrayTest : SenTestCase {
}

- (void)testCheckIndex;
- (void)testCheckRange;
- (void)testCheckRangeWithOffset;
@end


@implementation IOSArrayTest

- (void)testCheckIndex {
  IOSArray *array = [[IOSArray alloc] initWithLength:2];
  STAssertEquals([array count], (NSUInteger) 2, 
                 @"count should have returned 2, but was %d",
                 [array count]);
  STAssertNoThrowSpecific([array checkIndex:0], 
                          JavaLangIndexOutOfBoundsException,
                          @"Exception was thrown for a valid index");
  STAssertNoThrowSpecific([array checkIndex:1], 
                          JavaLangIndexOutOfBoundsException,
                          @"Exception was thrown for a valid index");
  STAssertThrowsSpecific([array checkIndex:-1], 
                         JavaLangIndexOutOfBoundsException,
                         @"Exception was not thrown for an invalid index");
  STAssertThrowsSpecific([array checkIndex:2], 
                         JavaLangIndexOutOfBoundsException,
                         @"Exception was not thrown for an invalid index");
}

- (void)testCheckRange {
  IOSArray *array = [[IOSArray alloc] initWithLength:2];
  STAssertEquals([array count], (NSUInteger) 2, 
                 @"count should have returned 2, but was %d",
                 [array count]);
  STAssertNoThrowSpecific([array checkRange:NSMakeRange(0, 1)], 
                          JavaLangIndexOutOfBoundsException,
                          @"Exception was thrown for a valid index");
  STAssertNoThrowSpecific([array checkRange:NSMakeRange(0, 2)], 
                          JavaLangIndexOutOfBoundsException,
                          @"Exception was thrown for a valid index");
  STAssertNoThrowSpecific([array checkRange:NSMakeRange(1, 1)], 
                          JavaLangIndexOutOfBoundsException,
                          @"Exception was thrown for a valid index");
  STAssertThrowsSpecific([array checkRange:NSMakeRange(-1, 2)], 
                          JavaLangIndexOutOfBoundsException,
                          @"Exception was not thrown for an invalid index");
  STAssertThrowsSpecific([array checkRange:NSMakeRange(0, 3)], 
                         JavaLangIndexOutOfBoundsException,
                         @"Exception was not thrown for an invalid index");
  STAssertThrowsSpecific([array checkRange:NSMakeRange(1, 2)], 
                         JavaLangIndexOutOfBoundsException,
                         @"Exception was not thrown for an invalid index");
  STAssertNoThrow([array checkRange:NSMakeRange(2, 0)], 
                         @"Exception was thrown for a zero length range");
}

- (void)testCheckRangeWithOffset {
  IOSArray *array = [[IOSArray alloc] initWithLength:4];
  STAssertEquals([array count], (NSUInteger) 4, 
                 @"count should have returned 4, but was %d",
                 [array count]);
  STAssertNoThrowSpecific([array checkRange:NSMakeRange(0, 1) withOffset:2], 
                          JavaLangIndexOutOfBoundsException,
                          @"Exception was thrown for a valid index");
  STAssertNoThrowSpecific([array checkRange:NSMakeRange(0, 2) withOffset:1], 
                          JavaLangIndexOutOfBoundsException,
                          @"Exception was thrown for a valid index");
  STAssertThrowsSpecific([array checkRange:NSMakeRange(-1, 2) withOffset:1], 
                         JavaLangIndexOutOfBoundsException,
                         @"Exception was not thrown for an invalid index");
  STAssertThrowsSpecific([array checkRange:NSMakeRange(0, 4) withOffset:1], 
                         JavaLangIndexOutOfBoundsException,
                         @"Exception was not thrown for an invalid index");
  STAssertThrowsSpecific([array checkRange:NSMakeRange(1, 2) withOffset:2], 
                         JavaLangIndexOutOfBoundsException,
                         @"Exception was not thrown for an invalid index");
  STAssertThrowsSpecific([array checkRange:NSMakeRange(2, 1) withOffset:-1], 
                         JavaLangIndexOutOfBoundsException,
                         @"Exception was not thrown for an invalid index");
}

- (void)testBooleanArrayCopy {
  const BOOL *bools = (BOOL[]){ TRUE, FALSE, TRUE, FALSE };
  IOSBooleanArray *a1 = [IOSBooleanArray arrayWithBooleans:bools count:4];
  IOSBooleanArray *a2 = [[a1 copy] autorelease];
  STAssertEquals([a1 count], [a2 count], @"bad array size");
  for (NSUInteger i = 0; i < 4; i++) {
    STAssertEquals([a1 booleanAtIndex:i], [a2 booleanAtIndex:i],
                   @"bad IOSBooleanArray element");
  }
}

- (void)testByteArrayCopy {
  const char *bytes = (char[]){ 1, 2, 3, 4 };
  IOSByteArray *a1 = [IOSByteArray arrayWithBytes:bytes count:4];
  IOSByteArray *a2 = [[a1 copy] autorelease];
  STAssertEquals([a1 count], [a2 count], @"bad array size");
  for (NSUInteger i = 0; i < 4; i++) {
    STAssertEquals([a1 byteAtIndex:i], [a2 byteAtIndex:i],
                   @"bad IOSByteArray element");
  }
}

- (void)testCharArrayCopy {
  const unichar *chars = (unichar[]){ 'a', 'b', 'c', 'd' };
  IOSCharArray *a1 = [IOSCharArray arrayWithCharacters:chars count:4];
  IOSCharArray *a2 = [[a1 copy] autorelease];
  STAssertEquals([a1 count], [a2 count], @"bad array size");
  for (NSUInteger i = 0; i < 4; i++) {
    STAssertEquals([a1 charAtIndex:i], [a2 charAtIndex:i],
                   @"bad IOSCharArray element");
  }
}

- (void)testDoubleArrayCopy {
  const double *doubles = (double[]){ 1.1, 2.2, 3.3, 4.4 };
  IOSDoubleArray *a1 = [IOSDoubleArray arrayWithDoubles:doubles count:4];
  IOSDoubleArray *a2 = [[a1 copy] autorelease];
  STAssertEquals([a1 count], [a2 count], @"bad array size");
  for (NSUInteger i = 0; i < 4; i++) {
    STAssertEquals([a1 doubleAtIndex:i], [a2 doubleAtIndex:i],
                   @"bad IOSDoubleArray element");
  }
}

- (void)testFloatArrayCopy {
  const float *floats = (float[]){ 1.1f, 2.2f, 3.3f, 4.4f };
  IOSFloatArray *a1 = [IOSFloatArray arrayWithFloats:floats count:4];
  IOSFloatArray *a2 = [[a1 copy] autorelease];
  STAssertEquals([a1 count], [a2 count], @"bad array size");
  for (NSUInteger i = 0; i < 4; i++) {
    STAssertEquals([a1 floatAtIndex:i], [a2 floatAtIndex:i],
                   @"bad IOSFloatArray element");
  }
}

- (void)testIntArrayCopy {
  const int *ints = (int[]){ 1, 2, 3, 4 };
  IOSIntArray *a1 = [IOSIntArray arrayWithInts:ints count:4];
  IOSIntArray *a2 = [[a1 copy] autorelease];
  STAssertEquals([a1 count], [a2 count], @"bad array size");
  for (NSUInteger i = 0; i < 4; i++) {
    STAssertEquals([a1 intAtIndex:i], [a2 intAtIndex:i],
                   @"bad IOSIntArray element");
  }
}

- (void)testLongArrayCopy {
  const long long *longs = (long long[]){ 1, 2, 3, 4 };
  IOSLongArray *a1 = [IOSLongArray arrayWithLongs:longs count:4];
  IOSLongArray *a2 = [[a1 copy] autorelease];
  STAssertEquals([a1 count], [a2 count], @"bad array size");
  for (NSUInteger i = 0; i < 4; i++) {
    STAssertEquals([a1 longAtIndex:i], [a2 longAtIndex:i],
                   @"bad IOSLongArray element");
  }
}

- (void)testShortArrayCopy {
  const short *shorts = (short[]){ 1, 2, 3, 4 };
  IOSShortArray *a1 = [IOSShortArray arrayWithShorts:shorts count:4];
  IOSShortArray *a2 = [[a1 copy] autorelease];
  STAssertEquals([a1 count], [a2 count], @"bad array size");
  for (NSUInteger i = 0; i < 4; i++) {
    STAssertEquals([a1 shortAtIndex:i], [a2 shortAtIndex:i],
                   @"bad IOSShortArray element");
  }
}

// Booleans are tested here, but all primitive types work because the code
// being tested is in IOSArray, which uses [self class] in its initializer
// to work correctly with sub-types.
- (void)testBooleanMultiDimensionalCreate {
  // Verify single dimension array is correct type.
  id array =
      [IOSBooleanArray arrayWithDimensions:1 lengths:(NSUInteger[]){2}];
  STAssertTrue([array isMemberOfClass:[IOSBooleanArray class]],
               @"wrong array type: %@", [array class]);

  // Verify multiple dimension array is an array of arrays (of arrays).
  array =
      [IOSBooleanArray arrayWithDimensions:3 lengths:(NSUInteger[]){2, 4, 6}];
  STAssertTrue([array isMemberOfClass:[IOSObjectArray class]],
               @"wrong array type: %@", [array class]);
  STAssertTrue([array count] == 2, @"invalid array count");
  for (NSUInteger i = 0; i < 2; i++) {
    id subarray = [array objectAtIndex:i];
    STAssertTrue([subarray isMemberOfClass:[IOSObjectArray class]],
                 @"wrong subarray type: %@", [subarray class]);
    STAssertTrue([subarray count] == 4, @"invalid subarray count");
    for (NSUInteger i = 0; i < 4; i++) {
      id subsubarray = [subarray objectAtIndex:i];
      STAssertTrue([subsubarray isMemberOfClass:[IOSBooleanArray class]],
                   @"wrong subarray type: %@", [subarray class]);
      STAssertTrue([subsubarray count] == 6, @"invalid subarray count");
    }
  }
}

// Objects are separately tested, because unlike primitive types, object arrays
// need a specified element type.
- (void)testObjectMultiDimensionalCreate {
  IOSClass *type = [IOSClass classWithClass:[JavaUtilDate class]];
  
  // Verify single dimension array is correct type.
  id array = [IOSObjectArray arrayWithDimensions:1
                                         lengths:(NSUInteger[]){2}
                                            type:type];
  STAssertEqualObjects([array elementType], type,
                       @"wrong element type: %@", [array elementType]);
  
  // Verify multiple dimension array is an array of arrays (of arrays).
  array = [IOSObjectArray arrayWithDimensions:3
                                      lengths:(NSUInteger[]){2, 4, 6}
                                         type:type];
  STAssertTrue([array isMemberOfClass:[IOSObjectArray class]],
               @"wrong array type: %@", [array class]);
  STAssertTrue([array count] == 2,
               @"invalid array count, was %d", [array count]);
  for (NSUInteger i = 0; i < 2; i++) {
    id subarray = [array objectAtIndex:i];
    STAssertTrue([subarray isMemberOfClass:[IOSObjectArray class]],
                 @"wrong subarray type: %@", [subarray class]);
    STAssertTrue([subarray count] == 4,
                 @"invalid subarray count, was %d", [subarray count]);
    for (NSUInteger i = 0; i < 4; i++) {
      id subsubarray = [subarray objectAtIndex:i];
      STAssertTrue([subsubarray isMemberOfClass:[IOSObjectArray class]],
                   @"wrong subarray type: %@", [subarray class]);
      STAssertTrue([subsubarray count] == 6,
                   @"invalid subsubarray count, was %d", [subsubarray count]);
      STAssertEqualObjects([subsubarray elementType], type,
                           @"wrong array type: %@", [subsubarray elementType]);
    }
  }
}

@end
