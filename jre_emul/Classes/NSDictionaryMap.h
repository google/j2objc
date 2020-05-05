//
//  NSDictionaryMap
//  JreEmulation
//
//  Created by Pankaj Kakkar on 5/20/13.
//  Copyright 2013 Google, Inc. All rights reserved.
//

#ifndef _NSDictionaryMap_H_
#define _NSDictionaryMap_H_

#import "java/util/AbstractMap.h"
#import "java/util/Map.h"

@protocol JavaUtilFunctionBiConsumer;

// An implementation of java.util.Map backed by an NSDictionary.
// The entrySet, keySet and valueSet methods return sets not backed
// by the Map, so modifications to the map won't be reflected in the
// sets and vice-versa.
@interface NSDictionaryMap : JavaUtilAbstractMap < JavaUtilMap > {
 @private
  // The backing native map.
  NSMutableDictionary *dictionary_;
}

// Initializes an empty map.
- (instancetype)init;
+ (instancetype)map;

// Initializes a map with the given dictionary.
- (id)initWithDictionary:(NSDictionary *)dictionary;
+ (NSDictionaryMap *)mapWithDictionary:(NSDictionary *)dictionary;

// Iterate over native dictionary without creating an entry set.
- (void)forEachWithJavaUtilFunctionBiConsumer:(id<JavaUtilFunctionBiConsumer>)action;

@end

#endif // _NSDictionaryMap_H_
