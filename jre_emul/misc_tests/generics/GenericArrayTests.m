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

#import "IOSObjectArray.h"
#import "NSString+JavaString.h"
#import "java/lang/ArrayStoreException.h"
#import "java/util/Arrays.h"

#import <XCTest/XCTest.h>

@interface GenericArrayTests : XCTestCase
@end

@implementation GenericArrayTests {
  NSArray<NSString *> *colors;
  IOSObjectArray<NSString *> *javaColors;
}

- (void)setUp {
  colors = @[ @"Blue", @"Red", @"Yellow" ];
  javaColors = [IOSObjectArray arrayWithNSArray:colors type:NSString_class_()];
}

// Verify NSArray methods work with IOSObjectArray.
- (void)testNSArrayMethods {
  XCTAssertEqual(3, [javaColors count]);
  XCTAssertFalse([javaColors isEqual:colors]);        // Classes are different, ...
  XCTAssertTrue([javaColors isEqualToArray:colors]);  // but array lengths and contents are equal.
  XCTAssertEqual([javaColors firstObject], @"Blue");
  XCTAssertEqual([javaColors objectAtIndex:1], @"Red");
  XCTAssertEqual([javaColors lastObject], @"Yellow");
}

// Verify NSMutableArray methods: only replaceObjectAtIndex:withObject: should work.
- (void)testNSMutableArrayMethods {
  [javaColors replaceObjectAtIndex:1 withObject:@"Green"];
  XCTAssertThrowsSpecific([javaColors addObject:@"Black"], JavaLangArrayStoreException);
  XCTAssertThrowsSpecific([javaColors insertObject:@"Black" atIndex:0], JavaLangArrayStoreException);
  XCTAssertThrowsSpecific([javaColors removeObjectAtIndex:0], JavaLangArrayStoreException);
  XCTAssertThrowsSpecific([javaColors removeLastObject], JavaLangArrayStoreException);
}

- (void)testArraySubscripting {
  javaColors[0] = @"Orange";
  XCTAssertEqual(javaColors[0], @"Orange");
}

@end
