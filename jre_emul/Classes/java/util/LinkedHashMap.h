//
//  LinkedHashMap.h
//  JreEmulation
//
//  Created by Tom Ball on 2/17/12.
//  Copyright 2012 Google, Inc. All rights reserved.
//

#import "JreEmulation.h"
#import "java/util/AbstractCollection.h"
#import "java/util/AbstractSet.h"
#import "java/util/HashMap.h"
#import "java/util/Iterator.h"
#import "java/util/LinkedHashMap.h"
#import "java/util/Map.h"

@interface JavaUtilLinkedHashMap : JavaUtilHashMap < JavaUtilMap >

- (id)initWithInt:(int)capacity
        withFloat:(float)loadFactor
         withBOOL:(BOOL)accessOrder;

@end
