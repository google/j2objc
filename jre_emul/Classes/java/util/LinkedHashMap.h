//
//  LinkedHashMap.h
//  JreEmulation
//
//  Created by Tom Ball on 2/17/12.
//  Copyright 2012 Google, Inc. All rights reserved.
//

@class JavaUtilLinkedHashMap_LinkedHashMapEntry;

#import "JreEmulation.h"
#import "java/util/AbstractCollection.h"
#import "java/util/AbstractSet.h"
#import "java/util/HashMap_PackagePrivate.h"
#import "java/util/Iterator.h"
#import "java/util/LinkedHashMap.h"
#import "java/util/Map.h"

@interface JavaUtilLinkedHashMap : JavaUtilHashMap < JavaUtilMap > {
 @public
  BOOL accessOrder_;
  JavaUtilLinkedHashMap_LinkedHashMapEntry *head_, *tail_;
}

- (id)initWithInt:(int)capacity
        withFloat:(float)loadFactor
         withBOOL:(BOOL)accessOrder;

@end
