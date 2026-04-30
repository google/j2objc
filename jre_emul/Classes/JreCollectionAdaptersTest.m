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
#include "java/util/HashMap.h"
#include "java/util/HashSet.h"
#include "java/util/List.h"
#include "java/util/Map.h"
#include "java/util/Set.h"

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

- (void)testEmptySet {
  id<JavaUtilSet> emptyImmutableSet = [JavaUtilSet of];
  NSSet *emptyImmutableSetArray = JREAdaptedSetFromJavaSet(emptyImmutableSet);
  XCTAssertEqual(emptyImmutableSetArray.count, 0);
  XCTAssertFalse([emptyImmutableSetArray isKindOfClass:NSClassFromString(@"JREImmutableJavaSet")]);

  id<JavaUtilSet> emptySet = [[JavaUtilHashSet alloc] init];
  NSSet *emptySetArray = JREAdaptedSetFromJavaSet(emptySet);
  XCTAssertEqual(emptySetArray.count, 0);
  XCTAssertFalse([emptySetArray isKindOfClass:NSClassFromString(@"JREImmutableJavaSet")]);
}

- (void)testImmutableSet {
  id<JavaUtilSet> immutableSet = [JavaUtilSet ofWithId:@"thing"];
  NSSet *immutableSetArray = JREAdaptedSetFromJavaSet(immutableSet);

  XCTAssertTrue([immutableSetArray isKindOfClass:NSClassFromString(@"JREImmutableJavaSet")]);
  XCTAssertEqual(immutableSetArray.count, 1);
  XCTAssertTrue([immutableSetArray containsObject:@"thing"]);
}

- (void)testMutableSetCopied {
  id<JavaUtilSet> mutableSet = [[JavaUtilHashSet alloc] init];
  [mutableSet addWithId:@"thing"];

  NSSet *setArray = JREAdaptedSetFromJavaSet(mutableSet);
  XCTAssertEqual([mutableSet size], 1);
  XCTAssertEqual(setArray.count, 1);

  [mutableSet addWithId:@"other"];

  XCTAssertEqual([mutableSet size], 2);
  XCTAssertEqual(setArray.count, 1);

  XCTAssertFalse([setArray isKindOfClass:NSClassFromString(@"JREImmutableJavaSet")]);
}

- (void)testEmptyMap {
  id<JavaUtilMap> emptyImmutableMap = [JavaUtilMap of];
  NSDictionary *emptyImmutableMapArray = JREAdaptedDictionaryFromJavaMap(emptyImmutableMap);
  XCTAssertEqual(emptyImmutableMapArray.count, 0);
  XCTAssertFalse([emptyImmutableMapArray isKindOfClass:NSClassFromString(@"JREImmutableJavaMap")]);

  id<JavaUtilMap> emptyMap = [[JavaUtilHashMap alloc] init];
  NSDictionary *emptyMapArray = JREAdaptedDictionaryFromJavaMap(emptyMap);
  XCTAssertEqual(emptyMapArray.count, 0);
  XCTAssertFalse([emptyMapArray isKindOfClass:NSClassFromString(@"JREImmutableJavaMap")]);
}

- (void)testImmutableMap {
  id<JavaUtilMap> immutableMap = [JavaUtilMap ofWithId:@"key" withId:@"value"];
  NSDictionary *immutableMapArray = JREAdaptedDictionaryFromJavaMap(immutableMap);

  XCTAssertTrue([immutableMapArray isKindOfClass:NSClassFromString(@"JREImmutableJavaMap")]);
  XCTAssertEqual(immutableMapArray.count, 1);
  XCTAssertEqualObjects(immutableMapArray[@"key"], @"value");
}

- (void)testMutableMapCopied {
  id<JavaUtilMap> mutableMap = [[JavaUtilHashMap alloc] init];
  [mutableMap putWithId:@"key" withId:@"value"];

  NSDictionary *mapArray = JREAdaptedDictionaryFromJavaMap(mutableMap);
  XCTAssertEqual([mutableMap size], 1);
  XCTAssertEqual(mapArray.count, 1);

  [mutableMap putWithId:@"key2" withId:@"value2"];

  XCTAssertEqual([mutableMap size], 2);
  XCTAssertEqual(mapArray.count, 1);

  XCTAssertFalse([mapArray isKindOfClass:NSClassFromString(@"JREImmutableJavaMap")]);
}

- (void)testCodingList {
  id<JavaUtilList> immutableList = [JavaUtilList ofWithId:@"thing"];
  NSArray *immutableListArray = JREAdaptedArrayFromJavaList(immutableList);

  NSData *data = [NSKeyedArchiver archivedDataWithRootObject:immutableListArray
                                       requiringSecureCoding:NO
                                                       error:nil];
  NSArray *unarchivedArray = [NSKeyedUnarchiver unarchivedObjectOfClass:[NSArray class]
                                                               fromData:data
                                                                  error:nil];

  XCTAssertEqualObjects(unarchivedArray, immutableListArray);
}

- (void)testCodingSet {
  id<JavaUtilSet> immutableSet = [JavaUtilSet ofWithId:@"thing"];
  NSSet *immutableSetArray = JREAdaptedSetFromJavaSet(immutableSet);

  NSData *data = [NSKeyedArchiver archivedDataWithRootObject:immutableSetArray
                                       requiringSecureCoding:NO
                                                       error:nil];
  NSSet *unarchivedSet = [NSKeyedUnarchiver unarchivedObjectOfClass:[NSSet class]
                                                           fromData:data
                                                              error:nil];

  XCTAssertEqualObjects(unarchivedSet, immutableSetArray);
}

- (void)testCodingMap {
  id<JavaUtilMap> immutableMap = [JavaUtilMap ofWithId:@"key" withId:@"value"];
  NSDictionary *immutableMapArray = JREAdaptedDictionaryFromJavaMap(immutableMap);

  NSData *data = [NSKeyedArchiver archivedDataWithRootObject:immutableMapArray
                                       requiringSecureCoding:NO
                                                       error:nil];
  NSDictionary *unarchivedDict = [NSKeyedUnarchiver unarchivedObjectOfClass:[NSDictionary class]
                                                                   fromData:data
                                                                      error:nil];

  XCTAssertEqualObjects(unarchivedDict, immutableMapArray);
}

@end
