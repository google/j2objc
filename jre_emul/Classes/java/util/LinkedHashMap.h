//
//  LinkedHashMap.h
//  JreEmulation
//
//  Created by Tom Ball on 2/17/12.
//  Copyright (c) 2012 Google, Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "java/util/HashMap.h"
#import "IOSIterator.h"
#import "IOSList.h"

// An ordered dictionary, which is a replacement for java.util.LinkedHashMap.
@interface JavaUtilLinkedHashMap : JavaUtilHashMap {
 @private
  NSMutableArray *index_;
  BOOL lastAccessedOrder_;
}

- (id)initWithInt:(int)capacity
        withFloat:(float)loadFactor
         withBOOL:(BOOL)lastAccessedOrder;

@end
