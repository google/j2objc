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

#import "IOSClass.h"
#import "IOSObjectArray.h"
#import <SenTestingKit/SenTestingKit.h>

// Unit tests for IOSObjectArray.
@interface IOSObjectArrayTest : SenTestCase {
}
@end

@implementation IOSObjectArrayTest

- (void)testInitialization {
  IOSClass * elementType = [IOSClass classWithClass:[NSObject class]];
  IOSObjectArray *array =
      [[IOSObjectArray alloc] initWithLength:10 type:elementType];
  int length = (int) [array count];
  STAssertEquals(length, 10, @"incorrect array size: %d", length);
  for (NSUInteger i = 0; i < 10; i++) {
    id element = [array objectAtIndex:i];
    STAssertNil(element, @"non-nil array element at index %d", i);
  }
}

- (void)testElementAccess {
  IOSClass * elementType = [IOSClass classWithClass:[NSString class]];
  IOSObjectArray *array =
      [[IOSObjectArray alloc] initWithLength:3 type:elementType];
  [array replaceObjectAtIndex:0 withObject:@"zero"];
  [array replaceObjectAtIndex:2 withObject:@"two"];
  STAssertEquals([array objectAtIndex:0], @"zero", @"incorrect element", nil);
  STAssertNil([array objectAtIndex:1], @"non-nil object", nil);
  STAssertEquals([array objectAtIndex:2], @"two", @"incorrect element", nil);
}

- (void)testGetObjects {
  IOSClass * elementType = [IOSClass classWithClass:[NSString class]];
  IOSObjectArray *array =
      [[IOSObjectArray alloc] initWithLength:3 type:elementType];
  [array replaceObjectAtIndex:0 withObject:@"zero"];
  [array replaceObjectAtIndex:2 withObject:@"two"];
  NSObject **copy = malloc(3 * sizeof(NSObject *));
  [array getObjects:copy length:3];
  STAssertEquals(*copy, @"zero", @"incorrect element", nil);
  STAssertNil(*(copy + 1), @"non-nil object", nil);
  STAssertEquals(*(copy + 2), @"two", @"incorrect element", nil);
  free(copy);
}

- (void)testReplaceObject {
  IOSClass * elementType = [IOSClass classWithClass:[NSString class]];
  IOSObjectArray *array =
      [[IOSObjectArray alloc] initWithLength:1 type:elementType];
  NSObject *item = @"foo";
  id result = [array replaceObjectAtIndex:0 withObject:item];
  STAssertEquals(item, result, @"same item wasn't returned", nil);
  result = [array objectAtIndex:0];
  STAssertEquals(item, result, @"same item wasn't returned", nil);
}

- (void)testArrayCopy {
  IOSClass *type = [IOSClass classWithClass:[NSNumber class]];
  IOSObjectArray *numbers = [[IOSObjectArray alloc] initWithLength:5
                                                              type:type];
  for (int i = 0; i < 5; i++) {
    [numbers replaceObjectAtIndex:i
                       withObject:[NSNumber numberWithInt:i]];
  }
  type = [IOSClass classWithClass:[NSString class]];
  IOSObjectArray *strings =
      [IOSObjectArray arrayWithType:type count:3, @"huey", @"dewey", @"louie"];
  [strings arraycopy:NSMakeRange(1, 1) 
         destination:numbers
              offset:2];
  STAssertEquals([[numbers objectAtIndex:0] intValue], 0, @"incorrect element", nil);
  STAssertEquals([[numbers objectAtIndex:1] intValue], 1, @"incorrect element", nil);
  STAssertEquals([numbers objectAtIndex:2], @"dewey", @"incorrect element", nil);
  STAssertEquals([[numbers objectAtIndex:3] intValue], 3, @"incorrect element", nil);
  STAssertEquals([[numbers objectAtIndex:4] intValue], 4, @"incorrect element", nil);
}

- (void)testOverlappingArrayCopy {
  IOSClass *type = [IOSClass classWithClass:[NSNumber class]];
  IOSObjectArray *numbers = [[IOSObjectArray alloc] initWithLength:5
                                                              type:type];
  for (int i = 0; i < 5; i++) {
    [numbers replaceObjectAtIndex:i
                       withObject:[NSNumber numberWithInt:i]];
  }
  [numbers arraycopy:NSMakeRange(1, 3)
         destination:numbers
              offset:2];
  STAssertEquals([[numbers objectAtIndex:0] intValue], 0, @"incorrect element", nil);
  STAssertEquals([[numbers objectAtIndex:1] intValue], 1, @"incorrect element", nil);
  STAssertEquals([[numbers objectAtIndex:2] intValue], 1, @"incorrect element", nil);
  STAssertEquals([[numbers objectAtIndex:3] intValue], 2, @"incorrect element", nil);
  STAssertEquals([[numbers objectAtIndex:4] intValue], 3, @"incorrect element", nil);
}

- (void)testCopy {
  IOSClass *type = [IOSClass classWithClass:[NSNumber class]];
  IOSObjectArray *array = [[IOSObjectArray alloc] initWithLength:10
                                                            type:type];
  for (int i = 0; i < 10; i++) {
    [array replaceObjectAtIndex:i
                     withObject:[NSNumber numberWithInt:i]];
  }
  IOSObjectArray *clone = [array copy];
  STAssertEquals([array count], [clone count], @"counts don't match", nil);
  for (int i = 0; i < 10; i++) {
    STAssertEquals([array objectAtIndex:i], [clone objectAtIndex:i],
                   @"elements don't match at index: %d", i);
  }
}  

@end
