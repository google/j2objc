//
//  HashMap.h
//  JreEmulation
//
//  Created by Tom Ball on 1/27/12.
//  Copyright 2012 Google, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "java/util/Map.h"

@class JavaUtilHashMap_Entry;

// An implementation for the java.util.Map protocol, using a
// NSMutableDictionary delegate. Its constructors are defined by HashMap, so
// that this class can potentially be a drop-in replacement.
@interface JavaUtilHashMap : NSObject < JavaUtilMap, NSMutableCopying > {
 @private
  NSMutableDictionary *dictionary_;
}

- (id)initWithInt:(int)capacity;
- (id)initWithInt:(int)capacity withFloat:(float)loadFactor;
- (id)initWithJavaUtilMap:(id<JavaUtilMap>)map;

@end
