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
//  IOSObjectArrayTest.m
//  JreEmulation
//
//  Created by Tom Ball on 9/11/11.
//

#include "J2ObjC_source.h"
#include "IOSArray_PackagePrivate.h"
#import <XCTest/XCTest.h>

// Unit tests for IOSObjectArray.
@interface IOSObjectArrayTest : XCTestCase {
}
@end

@implementation IOSObjectArrayTest

- (void)testInitialization {
  IOSObjectArray *array = [IOSObjectArray arrayWithLength:10 type:NSObject_class_()];
  jint length = [array length];
  XCTAssertEqual(length, 10, @"incorrect array size: %d", length);
  for (jint i = 0; i < 10; i++) {
    id element = [array objectAtIndex:i];
    XCTAssertNil(element, @"non-nil array element at index %d", i);
  }
}

- (void)testElementAccess {
  IOSObjectArray *array = [IOSObjectArray arrayWithLength:3 type:NSString_class_()];
  [array replaceObjectAtIndex:0 withObject:@"zero"];
  [array replaceObjectAtIndex:2 withObject:@"two"];
  XCTAssertEqual([array objectAtIndex:0], @"zero", @"incorrect element", nil);
  XCTAssertNil([array objectAtIndex:1], @"non-nil object", nil);
  XCTAssertEqual([array objectAtIndex:2], @"two", @"incorrect element", nil);
}

- (void)testGetObjects {
  IOSObjectArray *array = [IOSObjectArray arrayWithLength:3 type:NSString_class_()];
  [array replaceObjectAtIndex:0 withObject:@"zero"];
  [array replaceObjectAtIndex:2 withObject:@"two"];
  NSObject **copy = malloc(3 * sizeof(NSObject *));
  [array getObjects:copy length:3];
  XCTAssertEqual(*copy, @"zero", @"incorrect element", nil);
  XCTAssertNil(*(copy + 1), @"non-nil object", nil);
  XCTAssertEqual(*(copy + 2), @"two", @"incorrect element", nil);
  free(copy);
}

- (void)testReplaceObject {
  IOSObjectArray *array = [IOSObjectArray arrayWithLength:1 type:NSString_class_()];
  NSObject *item = @"foo";
  id result = [array replaceObjectAtIndex:0 withObject:item];
  XCTAssertEqual(item, result, @"same item wasn't returned", nil);
  result = [array objectAtIndex:0];
  XCTAssertEqual(item, result, @"same item wasn't returned", nil);
}

- (void)testArrayCopy {
  IOSClass *type = NSNumber_class_();
  IOSObjectArray *numbers = [IOSObjectArray arrayWithLength:5 type:type];
  for (int i = 0; i < 5; i++) {
    [numbers replaceObjectAtIndex:i
                       withObject:[NSNumber numberWithInt:i]];
  }
  IOSObjectArray *numbers2 = [IOSObjectArray arrayWithObjects:(id[]){
      [NSNumber numberWithInt:11], [NSNumber numberWithInt:12],
      [NSNumber numberWithInt:13] }
      count:3 type:type];
  [numbers2 arraycopy:1
          destination:numbers
            dstOffset:2
               length:1];
  XCTAssertEqual([[numbers objectAtIndex:0] intValue], 0, @"incorrect element", nil);
  XCTAssertEqual([[numbers objectAtIndex:1] intValue], 1, @"incorrect element", nil);
  XCTAssertEqual([[numbers objectAtIndex:2] intValue], 12, @"incorrect element", nil);
  XCTAssertEqual([[numbers objectAtIndex:3] intValue], 3, @"incorrect element", nil);
  XCTAssertEqual([[numbers objectAtIndex:4] intValue], 4, @"incorrect element", nil);
}

- (void)testOverlappingArrayCopy {
  IOSClass *type = NSNumber_class_();
  IOSObjectArray *numbers = [IOSObjectArray arrayWithLength:5 type:type];
  for (int i = 0; i < 5; i++) {
    [numbers replaceObjectAtIndex:i
                       withObject:[NSNumber numberWithInt:i]];
  }
  [numbers arraycopy:1
         destination:numbers
           dstOffset:2
              length:3];
  XCTAssertEqual([[numbers objectAtIndex:0] intValue], 0, @"incorrect element", nil);
  XCTAssertEqual([[numbers objectAtIndex:1] intValue], 1, @"incorrect element", nil);
  XCTAssertEqual([[numbers objectAtIndex:2] intValue], 1, @"incorrect element", nil);
  XCTAssertEqual([[numbers objectAtIndex:3] intValue], 2, @"incorrect element", nil);
  XCTAssertEqual([[numbers objectAtIndex:4] intValue], 3, @"incorrect element", nil);
}

- (void)testCopy {
  IOSClass *type = NSNumber_class_();
  IOSObjectArray *array = [IOSObjectArray arrayWithLength:10 type:type];
  for (int i = 0; i < 10; i++) {
    [array replaceObjectAtIndex:i
                     withObject:[NSNumber numberWithInt:i]];
  }
  IOSObjectArray *clone = [array java_clone];
  XCTAssertEqual([array length], [clone length], @"counts don't match", nil);
  for (int i = 0; i < 10; i++) {
    XCTAssertEqual([array objectAtIndex:i], [clone objectAtIndex:i],
                   @"elements don't match at index: %d", i);
  }
}

@end
