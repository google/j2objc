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
//  IOSReferenceTest.m
//  JreEmulation
//
//

#import "IOSReference.h"

#import <XCTest/XCTest.h>

// Unit tests for IOSClass.
@interface IOSReferenceTest : XCTestCase
@end


@implementation IOSReferenceTest

- (void)testTaggedPointerDetection {
  // Attempt to be a tripwire if tagged pointers change in unexpected ways.
  // Unfortunately, this also requires us to assume we know we can create
  // tagged pointers in certain ways.
  NSString *taggedString = [NSString stringWithCString:"x"];
  XCTAssertTrue([IOSReference isTaggedPointer:taggedString]);

  NSNumber *taggedNumber = [NSNumber numberWithShort:1];
  XCTAssertTrue([IOSReference isTaggedPointer:taggedNumber]);
}

@end
