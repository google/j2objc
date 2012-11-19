//
//  HashMap.h
//  JreEmulation
//
//  Created by Tom Ball on 1/27/12.
//  Copyright 2012 Google, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "java/io/Serializable.h"
#import "java/util/AbstractMap.h"
#import "java/util/Map.h"

@class JavaUtilHashMap_Entry;

// An implementation for the java.util.Map protocol, based on the java harmony
// implementation.
@interface JavaUtilHashMap : JavaUtilAbstractMap < JavaUtilMap, NSCopying, JavaIoSerializable > {
 @public
  int elementCount_;
  JavaUtilHashMap_Entry **elementData_;
  int elementDataLength_;
  int modCount_;
  float loadFactor_;
  int threshold_;
}

- (id)initWithInt:(int)capacity;
- (id)initWithInt:(int)capacity withFloat:(float)loadFactor;
- (id)initWithJavaUtilMap:(id<JavaUtilMap>)map;

@end
