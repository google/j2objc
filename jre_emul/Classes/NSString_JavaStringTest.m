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

#import <SenTestingKit/SenTestingKit.h>
#import "JreEmulation.h"

@interface NSString_JavaStringTest : SenTestCase {
}
@end

@implementation NSString_JavaStringTest

- (void)testAddedProtocols {
  STAssertTrue([NSString conformsToProtocol:@protocol(JavaLangCharSequence)],
               @"NSString does not include JavaString category", nil);
  STAssertTrue([NSString conformsToProtocol:@protocol(JavaLangComparable)],
               @"NSString does not include JavaString category", nil);
}

- (void)testCharSequenceLength {
  id<JavaLangCharSequence> cs = @"12345";
  NSUInteger len = [cs sequenceLength];
  STAssertEquals(len, (NSUInteger) 5,
                 @"char sequence length should be 5, but was %d",
                 len);
}

- (void)testSplit {
  // Interspersed occurrences.
  IOSObjectArray *parts = [@"ababa" split:@"b"];
  STAssertEquals((NSUInteger) 3, [parts count], @"Wrong number of parts.");
  STAssertEqualObjects(@"a", [parts objectAtIndex:0], @"Wrong part.");
  STAssertEqualObjects(@"a", [parts objectAtIndex:1], @"Wrong part.");
  STAssertEqualObjects(@"a", [parts objectAtIndex:2], @"Wrong part.");

  // String begins and ends with token.
  parts = [@"bbbabacbb" split:@"b"];
  STAssertEquals((NSUInteger) 2, [parts count], @"Wrong number of parts.");
  STAssertEqualObjects(@"a", [parts objectAtIndex:0], @"Wrong part.");
  STAssertEqualObjects(@"ac", [parts objectAtIndex:1], @"Wrong part.");

  // Regular expression.
  parts = [@"abba" split:@"[b]*"];
  STAssertEquals((NSUInteger) 2, [parts count], @"Wrong number of parts.");
  STAssertEqualObjects(@"a", [parts objectAtIndex:0], @"Wrong part.");
  STAssertEqualObjects(@"a", [parts objectAtIndex:1], @"Wrong part.");

  // Space regular expression.
  parts = [@"what up" split:@"\\s+"];
  STAssertEquals((NSUInteger) 2, [parts count], @"Wrong number of parts.");
  STAssertEqualObjects(@"what", [parts objectAtIndex:0],
                       @"First part is wrong.");
  STAssertEqualObjects(@"up", [parts objectAtIndex:1],
                       @"Second part is wrong.");

  // Regular expression occurs at beginning and end.
  parts = [@"   what  up " split:@"\\s+"];
  STAssertEquals((NSUInteger) 2, [parts count], @"Wrong number of parts.");
  STAssertEqualObjects(@"what", [parts objectAtIndex:0],
                       @"First part is wrong.");
  STAssertEqualObjects(@"up", [parts objectAtIndex:1],
                       @"Second part is wrong.");

  // Empty string.
  parts = [@"" split:@"\\s+"];
  STAssertEquals((NSUInteger) 0, [parts count], @"Wrong number of parts.");

  // No matches, not regex.
  parts = [@"a" split:@"b"];
  STAssertEquals((NSUInteger) 1, [parts count], @"Wrong number of parts.");
  STAssertEqualObjects(@"a", [parts objectAtIndex:0],
                       @"First part is wrong.");

  // No matches with regex.
  parts = [@"a" split:@"\\s+"];
  STAssertEquals((NSUInteger) 1, [parts count], @"Wrong number of parts.");
  STAssertEqualObjects(@"a", [parts objectAtIndex:0],
                       @"First part is wrong.");
}

- (void)testReplaceFirst {
  NSString *s = @"red-yellow-green-yellow";

  // Regular string replacement.
  STAssertEquals(@"red-blue-green-yellow",
      [s replaceFirst:@"yellow" withReplacement:@"blue"],
      @"Incorrect replacement");

  // Regex string replacement.
  STAssertEquals(@"red-blue-green-yellow",
                 [s replaceFirst:@"y[a-z]+w" withReplacement:@"blue"],
                 @"Incorrect replacement");
}

- (void)testReplaceAll {
  NSString *s = @"red-yellow-green-yellow";

  // Regular string replacement.
  STAssertEquals(@"red-blue-green-blue",
                 [s replaceFirst:@"yellow" withReplacement:@"blue"],
                 @"Incorrect replacement");

  // Regex string replacement.
  STAssertEquals(@"red-blue-green-blue",
                 [s replaceFirst:@"y[a-z]+w" withReplacement:@"blue"],
                 @"Incorrect replacement");
}

@end
