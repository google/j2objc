//
//  NSDictionaryMap
//  JreEmulation
//
//  Created by Pankaj Kakkar on 5/20/13.
//  Copyright 2013 Google, Inc. All rights reserved.
//

#ifndef _NSDictionaryMap_H_
#define _NSDictionaryMap_H_

#import <Foundation/Foundation.h>

#import "java/util/Map.h"

// An implementation of java.util.Map backed by an NSDictionary.
// The entrySet, keySet and valueSet methods return sets not backed
// by the Map, so modifications to the map won't be reflected in the
// sets and vice-versa.
// TODO(user): Make this inherit from AbstractMap to get compliance
// with spec.
@interface NSDictionaryMap : NSObject<JavaUtilMap> {
@private
  // The backing native map.
  NSMutableDictionary *dictionary_;
}

// Initializes an empty map.
- (id)init;
+ (NSDictionaryMap *)map;

// Initializes a map with the given dictionary.
- (id)initWithDictionary:(NSDictionary *)dictionary;
+ (NSDictionaryMap *)mapWithDictionary:(NSDictionary *)dictionary;

@end

#endif // _NSDictionaryMap_H_
