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
//  IOSCollection.m
//  JreEmulation
//
//  Created by Tom Ball on 2/2/12.
//

#import "IOSCollection.h"
#import "IOSSet.h"
#import "IOSList.h"
#import "IOSIterator.h"
#import "java/lang/IllegalArgumentException.h"
#import "java/lang/IndexOutOfBoundsException.h"
#import "java/lang/NullPointerException.h"
#import "java/lang/UnsupportedOperationException.h"
#import "java/util/Iterator.h"
#import "java/util/List.h"
#import "java/util/Set.h"

@implementation IOSCollection

@synthesize delegate = delegate_;

+ (IOSCollection *)collectionWithJavaUtilCollection:(id<JavaUtilCollection>)c {
  if ([c isKindOfClass:[IOSCollection class]]) {
    return (IOSCollection *)c;
  }
  
  if ([c conformsToProtocol:@protocol(JavaUtilList)]) {
    return [IOSList listWithJavaUtilList:(id<JavaUtilList>)c];
  }
  
  if ([c conformsToProtocol:@protocol(JavaUtilSet)]) {
    return [IOSSet setWithJavaUtilSet:(id<JavaUtilSet>)c];
  }
  
  id exception = [[JavaLangIllegalArgumentException alloc] init];
#if ! __has_feature(objc_arc)
  [exception autorelease];
#endif
  @throw exception;
}

- (id)init {
  return [self initWithInt:10];  // default capacity from ArrayList
}

- (id)initWithInt:(int)capacity {
  NSMutableArray *array = [[NSMutableArray alloc] initWithCapacity:capacity];
#if ! __has_feature(objc_arc)
  [array autorelease];
#endif
  return [self initWithNSArray:array];
}

- (id)initWithJavaUtilCollection:(id<JavaUtilCollection>)collection {
  self = [self initWithInt:[collection size]];
  if (self) {
    [self addAllWithJavaUtilCollection:collection];
  }
  return self;  
}

- (id)initWithNSArray:(NSMutableArray *)array {
  self = [super init];
  if (self) {
#if __has_feature(objc_arc)
    delegate_ = array;
#else
    delegate_ = [array retain];
#endif
  }
  return self;  
}

#pragma mark -
#pragma mark JavaUtilCollection

- (BOOL)addWithId:(id)object {
  if (!object) {
    object = [NSNull null];
  }
  if ([self canAdd:object]) {
    [delegate_ addObject:object];
    return YES;
  }
  return NO;
}

- (BOOL)addAllWithJavaUtilCollection:(id<JavaUtilCollection>)collection {
  return [self addAllAtLocation:0 collection:collection];
}

- (BOOL)addAllAtLocation:(int)location
              collection:(id<JavaUtilCollection>)collection {
  if ([collection isEmpty]) {
    return NO;
  }
  if ([collection isKindOfClass:[IOSCollection class]]) {
    for (id object in (IOSCollection *) collection) {
      if ([self canAdd:object]) {
        [delegate_ insertObject:object atIndex:location++];
      }
    }
  } else {
    id<JavaUtilIterator> iterator = [collection iterator];
    while ([iterator hasNext]) {
      id object = [iterator next];
      if ([self canAdd:object]) {
        [delegate_ insertObject:object atIndex:location++];
      }
    }
  }
  return YES;
}

- (BOOL)canAdd:(id)object {
  id exception = [[JavaLangUnsupportedOperationException alloc] init];
#if ! __has_feature(objc_arc)
  [exception autorelease];
#endif
  @throw exception;
}

- (void)clear {
  [delegate_ removeAllObjects];
}

- (NSObject *)clone {
  NSMutableArray *array = [delegate_ mutableCopy];
  id result = [[[self class] alloc] initWithNSArray:array];
#if ! __has_feature(objc_arc)
  [array autorelease];
  [result autorelease];
#endif
  return result;
}

- (BOOL)containsWithId:(id)object {
  if (!object) {
    object = [NSNull null];
  }
  return [delegate_ containsObject:object];
}

- (BOOL)containsAllWithJavaUtilCollection:(id<JavaUtilCollection>)collection {
  if ([collection isKindOfClass:[IOSCollection class]]) {
    for (id object in (IOSCollection *) collection) {
      if (![delegate_ containsObject:object]) {
        return NO;
      }
    }
  } else {
    id<JavaUtilIterator> iterator = [collection iterator];
    @try {
      while ([iterator hasNext]) {
        if (![delegate_ containsObject:[iterator next]]) {
          return NO;
        }
      }
    }
    @finally {
#if ! __has_feature(objc_arc)
      [iterator release];
#endif
    }
  }
  return YES;
}

- (NSUInteger)hash {
  return [super hash];
}

- (BOOL)isEmpty {
  return [delegate_ count] == 0;
}

- (BOOL)isEqual:(id)object {
  return [super isEqual:object];
}

- (id<JavaUtilIterator>)iterator {
  id result = [[IOSIterator alloc] initWithList:delegate_];
#if ! __has_feature(objc_arc)
  [result autorelease];
#endif
  return result;
}

- (BOOL)removeWithId:(id)object {
  if (!object) {
    object = [NSNull null];
  }
  if ([delegate_ containsObject:object] ) {
    [delegate_ removeObject:object];
    return YES;
  }
  return NO;
}

- (BOOL)removeAllWithJavaUtilCollection:(id<JavaUtilCollection>)collection {
  BOOL modified = NO;
  if ([collection isKindOfClass:[IOSCollection class]]) {
    for (id element in (IOSCollection *) collection) {
      if ([delegate_ containsObject:element]) {
        [delegate_ removeObject:element];
        modified = YES;
      }
    }
  } else {
    id<JavaUtilIterator> iterator = [collection iterator];
    @try {
      while ([iterator hasNext]) {
        id element = [iterator next];
        if ([delegate_ containsObject:element]) {
          [delegate_ removeObject:element];
          modified = YES;
        }
      }
    }
    @finally {
#if ! __has_feature(objc_arc)
      [iterator release];
#endif
    }
  }
  return modified;
}

- (BOOL)retainAllWithJavaUtilCollection:(id<JavaUtilCollection>)collection {
  BOOL modified = NO;
  if ([collection isKindOfClass:[IOSCollection class]]) {
    for (id element in (IOSCollection *) collection) {
      if (![delegate_ containsObject:element]) {
        [delegate_ removeObject:element];
        modified = YES;
      }
    }
  } else {
    id<JavaUtilIterator> iterator = [collection iterator];
    @try {
      while ([iterator hasNext]) {
        id element = [iterator next];
        if (![delegate_ containsObject:element]) {
          [delegate_ removeObject:element];
          modified = YES;
        }
      }
    }
    @finally {
#if ! __has_feature(objc_arc)
      [iterator release];
#endif
    }
  }
  return modified;
}

- (int)size {
  return [delegate_ count];
}

- (id)mutableCopyWithZone:(NSZone *)zone {
  id clone = [self clone];
#if ! __has_feature(objc_arc)
  [clone retain];
#endif
  return clone;
}

static IOSObjectArray *makeEmptyObjectArray(NSUInteger size) {
  IOSClass *arrayType = [IOSClass classWithClass:[NSObject class]];
  id result = [[IOSObjectArray alloc] initWithLength:size
                                                type:arrayType];
#if ! __has_feature(objc_arc)
  [result autorelease];
#endif
  return result;
}

- (IOSObjectArray *)toArray {
  IOSObjectArray *result = makeEmptyObjectArray([self size]);
  return [self toArrayWithNSObjectArray:result];
}

- (IOSObjectArray *)toArrayWithNSObjectArray:(IOSObjectArray *)array {
  if (!array) {
    id exception = [[JavaLangNullPointerException alloc] init];
#if ! __has_feature(objc_arc)
    [exception release];
#endif
    @throw exception;
  }
  if ([array count] < [self size]) {
#if ! __has_feature(objc_arc)
    [array release];
#endif
    array = makeEmptyObjectArray([self size]);
  }
  NSUInteger i = 0;
  id<JavaUtilIterator> it = [self iterator];
  while ([it hasNext]) {
    [array replaceObjectAtIndex:i++ withObject:[it next]];
  }
  return array;
}

#pragma mark -
#pragma mark NSFastEnumeration

- (NSUInteger)countByEnumeratingWithState:(NSFastEnumerationState *)state
                                  objects:(id __unsafe_unretained [])stackbuf
                                    count:(NSUInteger)len {
  return [delegate_ countByEnumeratingWithState:state
                                        objects:stackbuf
                                          count:len];
}

#pragma mark -

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [delegate_ release];
  [super dealloc];
}
#endif

@end
