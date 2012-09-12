//
//  LinkedHashMap.m
//  JreEmulation
//
//  Created by Tom Ball on 2/17/12.
//  Copyright (c) 2012 Google, Inc. All rights reserved.
//

#import "LinkedHashMap_PackagePrivate.h"

@implementation JavaUtilLinkedHashMap

@synthesize index = index_;
@synthesize lastAccessedOrder = lastAccessedOrder_;

- (id)initWithInt:(int)capacity {
  if ((self = [super initWithInt:capacity])) {
    index_ = [[NSMutableArray alloc] initWithCapacity:capacity];
    lastAccessedOrder_ = NO;
  }
  return self;
}

- (id)initWithInt:(int)capacity
        withFloat:(float)loadFactor
  withBOOL:(BOOL)lastAccessedOrder {
  if ((self = [self initWithInt:capacity])) {
    lastAccessedOrder_ = lastAccessedOrder;
  }
  return self;
}

- (id)initWithJavaUtilMap:(id<JavaUtilMap>)map {
  if ((self = [super initWithJavaUtilMap:map])) {
    index_ = [[NSMutableArray alloc] initWithCapacity:[map size]];
  }
  return self;
}

#pragma mark -

- (void)clear {
  [super clear];
  [index_ removeAllObjects];
}

- (id<JavaUtilSet>)entrySet {
  id result = [[JavaUtilLinkedHashMap_EntrySet alloc]
               initWithJavaUtilHashMap:self];
#if ! __has_feature(objc_arc)
  [result autorelease];
#endif
  return result;
}

- (id)getWithId:(id)key {
  key = denullify(key);
  id result = [super getWithId:key];
  if (lastAccessedOrder_ && result != nil) {
    [index_ removeObject:key];
    [index_ addObject:key];
  }
  return nullify(result);
}

- (BOOL)isEqual:(id)object {
  return [object isKindOfClass:[JavaUtilLinkedHashMap class]] &&
      [super isEqual:object];
}

- (id<JavaUtilSet>)keySet {
  id keySet =
      [[JavaUtilLinkedHashMap_KeySet alloc] initWithJavaUtilHashMap:self];
#if ! __has_feature(objc_arc)
  [keySet autorelease];
#endif
  return keySet;
}

- (id)putWithId:(id)key
         withId:(id)value {
  key = denullify(key);
  id previous = [super putWithId:key withId:value];
  if (lastAccessedOrder_) {
    if (previous) {
      [index_ removeObject:key];
    }
    [index_ addObject:key];
  } else if (!previous) {
    [index_ addObject:key];
  }
  return previous;  // already nullified by superclass.
}

- (id)removeWithId:(id)key {
  id result = [super removeWithId:key];
  [index_ removeObject:denullify(key)];
  return result;  // already nullified by superclass.
}

- (id<JavaUtilCollection>)values {
  id result = [[JavaUtilLinkedHashMap_Values alloc]
               initWithJavaUtilHashMap:self];
#if ! __has_feature(objc_arc)
  [result autorelease];
#endif
  return result;
}

- (id)mutableCopyWithZone:(NSZone *)zone {
  JavaUtilLinkedHashMap *copy = [super mutableCopyWithZone:zone];
  copy->index_ = [index_ mutableCopy];
  return copy;
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [index_ release];
  [super dealloc];
}
#endif

#pragma mark -

@end

@implementation JavaUtilLinkedHashMap_KeySet

- (id<JavaUtilIterator>)iterator {
  JavaUtilLinkedHashMap *linkedMap = (JavaUtilLinkedHashMap *) self.map;
  NSMutableArray *keyList = [linkedMap.index mutableCopy];
#if ! __has_feature(objc_arc)
  [keyList autorelease];
#endif
  IOSIterator *keyIterator = [[IOSIterator alloc] initWithList:keyList];
#if ! __has_feature(objc_arc)
  [keyIterator autorelease];
#endif
  id iterator =
      [[JavaUtilLinkedHashMap_KeySetIterator alloc]
       initWithJavaUtilHashMap:self.map withIterator:keyIterator];
#if ! __has_feature(objc_arc)
  [iterator autorelease];
#endif
  return iterator;
}

@end

@implementation JavaUtilLinkedHashMap_KeySetIterator

- (id)next {
  id key = [super next];
  JavaUtilLinkedHashMap *linkedMap = (JavaUtilLinkedHashMap *) self.map;
  if (linkedMap.lastAccessedOrder) {
    [self.map getWithId:key];
  }
  return key;
}

@end

@implementation JavaUtilLinkedHashMap_EntrySet

- (id<JavaUtilIterator>)iterator {
  JavaUtilLinkedHashMap *linkedMap = (JavaUtilLinkedHashMap *) self.map;
  NSMutableArray *keyList = [linkedMap.index mutableCopy];
#if ! __has_feature(objc_arc)
  [keyList autorelease];
#endif
  IOSIterator *keyIterator = [[IOSIterator alloc] initWithList:keyList];
#if ! __has_feature(objc_arc)
  [keyIterator autorelease];
#endif
  id iterator =
      [[JavaUtilLinkedHashMap_EntrySetIterator alloc]
       initWithJavaUtilHashMap:linkedMap withIterator:keyIterator];
#if ! __has_feature(objc_arc)
  [iterator autorelease];
#endif
  return iterator;
}

@end

@implementation JavaUtilLinkedHashMap_EntrySetIterator

- (id)next {
  id key = [super next];
  JavaUtilLinkedHashMap *linkedMap = (JavaUtilLinkedHashMap *) self.map;
  if (linkedMap.lastAccessedOrder) {
    [self.map getWithId:key];
  }
  return key;
}

@end

@implementation JavaUtilLinkedHashMap_Values

- (id<JavaUtilIterator>)iterator {
  JavaUtilLinkedHashMap *linkedMap = (JavaUtilLinkedHashMap *) self.map;
  NSMutableArray *keyList = [linkedMap.index mutableCopy];
#if ! __has_feature(objc_arc)
  [keyList autorelease];
#endif
  IOSIterator *keyIterator = [[IOSIterator alloc] initWithList:keyList];
#if ! __has_feature(objc_arc)
  [keyIterator autorelease];
#endif
  id iterator =
      [[JavaUtilLinkedHashMap_ValuesIterator alloc]
       initWithJavaUtilHashMap:linkedMap withIterator:keyIterator];
#if ! __has_feature(objc_arc)
  [iterator autorelease];
#endif
  return iterator;
}

@end

@implementation JavaUtilLinkedHashMap_ValuesIterator

- (id)next {
  id key = [super next];
  id value = [self.map getWithId:key];
  JavaUtilLinkedHashMap *linkedMap = (JavaUtilLinkedHashMap *) self.map;
  if (linkedMap.lastAccessedOrder) {
    // Side-effect of get is that it updates the index ordering.
    [self.map getWithId:key];
  }
  return value;
}

@end
