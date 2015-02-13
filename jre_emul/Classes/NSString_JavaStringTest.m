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
//  NSString+JavaStringTest.m
//  JreEmulation
//
//  Created by Tom Ball on 10/4/11.
//

#import <XCTest/XCTest.h>
#import "JreEmulation.h"

@interface NSString_JavaStringTest : XCTestCase {
}
@end

@implementation NSString_JavaStringTest

- (void)testAddedProtocols {
  XCTAssertTrue([NSString conformsToProtocol:@protocol(JavaLangCharSequence)],
               @"NSString does not include JavaString category", nil);
  XCTAssertTrue([NSString conformsToProtocol:@protocol(JavaLangComparable)],
               @"NSString does not include JavaString category", nil);
}

- (void)testCharSequenceLength {
  id<JavaLangCharSequence> cs = @"12345";
  jint len = [cs length];
  XCTAssertEqual(len, 5,
                 @"char sequence length should be 5, but was %d",
                 len);
}

- (void)testSplit {
  // Interspersed occurrences.
  IOSObjectArray *parts = [@"ababa" split:@"b"];
  XCTAssertEqual(3, [parts length], @"Wrong number of parts.");
  XCTAssertEqualObjects(@"a", [parts objectAtIndex:0], @"Wrong part.");
  XCTAssertEqualObjects(@"a", [parts objectAtIndex:1], @"Wrong part.");
  XCTAssertEqualObjects(@"a", [parts objectAtIndex:2], @"Wrong part.");

  // String begins and ends with token.
  parts = [@"bbbabacbb" split:@"b"];
  XCTAssertEqual(5, [parts length], @"Wrong number of parts.");
  XCTAssertEqualObjects(@"", [parts objectAtIndex:0], @"Wrong part.");
  XCTAssertEqualObjects(@"", [parts objectAtIndex:1], @"Wrong part.");
  XCTAssertEqualObjects(@"", [parts objectAtIndex:2], @"Wrong part.");
  XCTAssertEqualObjects(@"a", [parts objectAtIndex:3], @"Wrong part.");
  XCTAssertEqualObjects(@"ac", [parts objectAtIndex:4], @"Wrong part.");

  // Regular expression.
  parts = [@"abba" split:@"[b]+"];
  XCTAssertEqual(2, [parts length], @"Wrong number of parts.");
  XCTAssertEqualObjects(@"a", [parts objectAtIndex:0], @"Wrong part.");
  XCTAssertEqualObjects(@"a", [parts objectAtIndex:1], @"Wrong part.");

  // Space regular expression.
  parts = [@"what up" split:@"\\s+"];
  XCTAssertEqual(2, [parts length], @"Wrong number of parts.");
  XCTAssertEqualObjects(@"what", [parts objectAtIndex:0],
                       @"First part is wrong.");
  XCTAssertEqualObjects(@"up", [parts objectAtIndex:1],
                       @"Second part is wrong.");

  // Regular expression occurs at beginning and end.
  parts = [@"   what  up " split:@"\\s+"];
  XCTAssertEqual(3, [parts length], @"Wrong number of parts.");
  XCTAssertEqualObjects(@"", [parts objectAtIndex:0], @"Wrong part.");
  XCTAssertEqualObjects(@"what", [parts objectAtIndex:1], @"Wrong part.");
  XCTAssertEqualObjects(@"up", [parts objectAtIndex:2], @"Wrong part.");

  // Empty string.
  parts = [@"" split:@"\\s+"];
  XCTAssertEqual(1, [parts length], @"Wrong number of parts.");
  XCTAssertEqualObjects(@"", [parts objectAtIndex:0], @"Wrong part.");

  // No matches, not regex.
  parts = [@"a" split:@"b"];
  XCTAssertEqual(1, [parts length], @"Wrong number of parts.");
  XCTAssertEqualObjects(@"a", [parts objectAtIndex:0],
                       @"First part is wrong.");

  // No matches with regex.
  parts = [@"a" split:@"\\s+"];
  XCTAssertEqual(1, [parts length], @"Wrong number of parts.");
  XCTAssertEqualObjects(@"a", [parts objectAtIndex:0],
                       @"First part is wrong.");
}

- (void)testReplaceFirst {
  NSString *s = @"red-yellow-green-yellow";
  NSString *replacement = [s replaceFirst:@"yellow" withReplacement:@"blue"];
  // Regular string replacement.
  XCTAssertTrue([@"red-blue-green-yellow" isEqualToString:replacement],
               @"Incorrect replacement");

  replacement = [s replaceFirst:@"y[a-z]+w" withReplacement:@"blue"];
  // Regex string replacement.
  XCTAssertTrue([@"red-blue-green-yellow" isEqualToString:replacement],
               @"Incorrect replacement");
}

- (void)testReplaceAll {
  NSString *s = @"red-yellow-green-yellow";
  NSString *replacement = [s replaceAll:@"yellow" withReplacement:@"blue"];

  // Regular string replacement.
  XCTAssertTrue([@"red-blue-green-blue" isEqualToString:replacement],
               @"Incorrect replacement");

  replacement = [s replaceAll:@"y[a-z]+w" withReplacement:@"blue"];
  // Regex string replacement.
  XCTAssertTrue([@"red-blue-green-blue" isEqualToString:replacement],
               @"Incorrect replacement");
}

- (void)testIndexOfCharacters {
  // Single character.
  XCTAssertEqual(0, [@"a" indexOf:'a'], @"Wrong index.");
  XCTAssertEqual(-1, [@"a" indexOf:'b'], @"Wrong index.");

  // Not single characters
  XCTAssertEqual(0, [@"ab" indexOf:'a'], @"Wrong index.");
  XCTAssertEqual(1, [@"ab" indexOf:'b'], @"Wrong index.");
  XCTAssertEqual(-1, [@"ab" indexOf:'c'], @"Wrong index.");

  // Finds first occurrence properly.
  XCTAssertEqual(0, [@"aba" indexOf:'a'], @"Wrong index.");
}

- (void)testLastIndexOfCharacters {
  // Single character.
  XCTAssertEqual(0, [@"a" lastIndexOf:'a'], @"Wrong index.");
  XCTAssertEqual(-1, [@"a" lastIndexOf:'b'], @"Wrong index.");

  // Not single characters
  XCTAssertEqual(0, [@"ab" lastIndexOf:'a'], @"Wrong index.");
  XCTAssertEqual(1, [@"ab" lastIndexOf:'b'], @"Wrong index.");
  XCTAssertEqual(-1, [@"ab" lastIndexOf:'c'], @"Wrong index.");

  // Finds last occurrence properly.
  XCTAssertEqual(2, [@"aba" lastIndexOf:'a'], @"Wrong index.");
}

// Empty test to workaround an Xcode race condition parsing the test
// log. Without it, there are intermittant failures that one or more
// unit tests did not finish, even though the log shows they did.
// Lots of projects have run into this issue, and consensus is that
// this "sleep for 1 second" does the trick.
- (void)testThatMakesSureWeDontFinishTooFast {
  [NSThread sleepForTimeInterval:1.0];
}

// Verify that an index sent to indexOf:int: with an offset greater
// than the string's length returns -1 as spec'd, rather than throw
// an NSRangeException.
- (void)testIndexOfOffsetTooLarge {
  XCTAssertEqual(-1, [@"12345" indexOfString:@"3" fromIndex:20], @"missing range check");
}
@end
