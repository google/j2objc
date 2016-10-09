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
//  NSObject+CloneTest.m
//  JreEmulation
//
//  Created by Tom Ball on 8/25/11.
//

#import "NSObject+JavaObject.h"
#import "IOSClass.h"
#import "java/lang/CloneNotSupportedException.h"
#import "java/util/ArrayList.h"
#import "java/util/List.h"
#import <XCTest/XCTest.h>

// Unit tests for NSObject+Clone.
@interface NSObject_JavaObjectTest : XCTestCase
@end

@implementation NSObject_JavaObjectTest

- (void)testResponds {
  XCTAssertTrue([@"tester" respondsToSelector: @selector(java_compareTo:)],
               @"NSObject+Clone category not loaded", nil);
}

- (void)testGetClass {
  // Test with class.
  JavaUtilArrayList *one = [[[JavaUtilArrayList alloc] init] autorelease];
  IOSClass *clazz = [one java_getClass];
  XCTAssertEqualObjects([clazz getName], @"java.util.ArrayList",
                 @"incorrect class name", nil);

  // Now with a protocol.
  id<JavaUtilList> two = [[[JavaUtilArrayList alloc] init] autorelease];
  clazz = [(id<JavaObject>) two java_getClass];
  XCTAssertEqualObjects([clazz getName], @"java.util.ArrayList",
                 @"incorrect class name", nil);
}

@end
