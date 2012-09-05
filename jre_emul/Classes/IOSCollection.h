// Copyright 2012 Google Inc. All Rights Reserved.
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

//
//  IOSCollection.h
//  JreEmulation
//
//  Created by Tom Ball on 2/2/12.
//

#import <Foundation/Foundation.h>
#import "java/util/Collection.h"

// Abstract base class for IOSList and IOSSet.
@interface IOSCollection :
    NSObject < JavaUtilCollection, NSFastEnumeration, NSMutableCopying > {
 @protected
  NSMutableArray *delegate_;
}

@property (readonly) NSMutableArray *delegate;

// Makes sure given collection adopts the NSFastEnumeration protocol
// If it is not already an IOSCollection, but is a JavaUtilList or JavaUtilSet
// constructs the right kind of IOSCollection; otherwise throws an exception.
+ (IOSCollection *)collectionWithJavaUtilCollection:(id<JavaUtilCollection>)c;

- (id)initWithInt:(int)capacity;
- (id)initWithJavaUtilCollection:(id<JavaUtilCollection>)collection;
- (id)initWithNSArray:(NSMutableArray *)array;

- (BOOL)addAllAtLocation:(int)location
              collection:(id<JavaUtilCollection>)collection;

// Subclasses override this method to return whether a given object can be
// added to the collection.
- (BOOL)canAdd:(id)object;

@end
