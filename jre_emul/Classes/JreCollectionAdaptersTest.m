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

#import <XCTest/XCTest.h>
#import "JreCollectionAdapters.h"
#include "java/util/ArrayList.h"
#include "java/util/Collections.h"
#include "java/util/List.h"

@interface JreCollectionAdaptersTest : XCTestCase
@end

@implementation JreCollectionAdaptersTest

- (void)testEmptyList {
  id<JavaUtilList> emptyImmutableList = [JavaUtilList of];
  NSArray *emptyImmutableListArray = JREAdaptedArrayFromJavaList(emptyImmutableList);
  XCTAssertEqual(emptyImmutableListArray.count, 0);
  XCTAssertFalse(
      [emptyImmutableListArray isKindOfClass:NSClassFromString(@"JREImmutableJavaListArray")]);

  id<JavaUtilList> emptyList = [[JavaUtilArrayList alloc] init];
  NSArray *emptyListArray = JREAdaptedArrayFromJavaList(emptyList);
  XCTAssertEqual(emptyListArray.count, 0);
  XCTAssertFalse([emptyListArray isKindOfClass:NSClassFromString(@"JREImmutableJavaListArray")]);
}

- (void)testImmutableList {
  id<JavaUtilList> immutableList = [JavaUtilList ofWithId:@"thing"];
  NSArray *immutableListArray = JREAdaptedArrayFromJavaList(immutableList);

  XCTAssertTrue([immutableListArray isKindOfClass:NSClassFromString(@"JREImmutableJavaListArray")]);
  XCTAssertEqual(immutableListArray.count, 1);
  XCTAssertEqualObjects(immutableListArray[0], @"thing");
}

- (void)testUnmodifiableCollection {
  id<JavaUtilList> unmodifiableList =
      [JavaUtilCollections unmodifiableListWithJavaUtilList:[JavaUtilList ofWithId:@"thing"]];
  NSArray *unmodifiableListArray = JREAdaptedArrayFromJavaList(unmodifiableList);

  XCTAssertTrue(
      [unmodifiableListArray isKindOfClass:NSClassFromString(@"JREImmutableJavaListArray")]);
  XCTAssertEqual(unmodifiableListArray.count, 1);
  XCTAssertEqualObjects(unmodifiableListArray[0], @"thing");
}

- (void)testMutableListCopied {
  id<JavaUtilList> mutableList = [[JavaUtilArrayList alloc] init];
  [mutableList addWithId:@"thing"];

  NSArray *listArray = JREAdaptedArrayFromJavaList(mutableList);
  XCTAssertEqual([mutableList size], 1);
  XCTAssertEqual(listArray.count, 1);

  [mutableList addWithId:@"other"];

  XCTAssertEqual([mutableList size], 2);
  XCTAssertEqual(listArray.count, 1);

  XCTAssertFalse([listArray isKindOfClass:NSClassFromString(@"JREImmutableJavaListArray")]);
}

@end
