//
//  HashMap.h
//  JreEmulation
//
//  Created by Tom Ball on 1/27/12.
//  Copyright 2012 Google, Inc. All rights reserved.
//

#ifndef _JavaUtilHashMap_H_
#define _JavaUtilHashMap_H_

#import <Foundation/Foundation.h>
#include "java/io/Serializable.h"
#include "java/util/AbstractMap.h"
#include "java/util/Map.h"

@class JavaUtilHashMap_Entry;

// An implementation for the java.util.Map protocol, based on the java harmony
// implementation.
@interface JavaUtilHashMap : JavaUtilAbstractMap < JavaUtilMap, NSCopying, JavaIoSerializable >

- (id)initWithInt:(int)capacity;
- (id)initWithInt:(int)capacity withFloat:(float)loadFactor;
- (id)initWithJavaUtilMap:(id<JavaUtilMap>)map;

@end

#endif // _JavaUtilHashMap_H_
