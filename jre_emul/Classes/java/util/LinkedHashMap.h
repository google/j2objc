//
//  LinkedHashMap.h
//  JreEmulation
//
//  Created by Tom Ball on 2/17/12.
//  Copyright 2012 Google, Inc. All rights reserved.
//

#ifndef _JavaUtilLinkedHashMap_H_
#define _JavaUtilLinkedHashMap_H_

#include "JreEmulation.h"
#include "java/util/AbstractCollection.h"
#include "java/util/AbstractSet.h"
#include "java/util/HashMap.h"
#include "java/util/Iterator.h"
#include "java/util/Map.h"

@interface JavaUtilLinkedHashMap : JavaUtilHashMap < JavaUtilMap >

- (id)initWithInt:(int)capacity
        withFloat:(float)loadFactor
      withBoolean:(BOOL)accessOrder;

@end

#endif // _JavaUtilLinkedHashMap_H_
