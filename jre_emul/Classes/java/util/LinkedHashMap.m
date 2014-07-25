//
//  LinkedHashMap.m
//  JreEmulation
//
//  Created by Keith Stanger on 11/06/12.
//  Copyright 2012 Google, Inc. All rights reserved.
//

#include "IOSClass.h"
#include "java/lang/IllegalStateException.h"
#include "java/util/Collection.h"
#include "java/util/ConcurrentModificationException.h"
#include "java/util/HashMap.h"
#include "java/util/HashMap_PackagePrivate.h"
#include "java/util/Iterator.h"
#include "java/util/LinkedHashMap_PackagePrivate.h"
#include "java/util/Map.h"
#include "java/util/NoSuchElementException.h"
#include "java/util/Set.h"

#if __has_feature(objc_arc)
#error "JavaUtilLinkedHashMap is not built with ARC"
#endif

static NSUInteger EnumerateEntries(
    JavaUtilHashMap *map, NSFastEnumerationState *state, __unsafe_unretained id *stackbuf,
    NSUInteger len);

@implementation JavaUtilLinkedHashMap

@synthesize accessOrder = accessOrder_;
@synthesize head = head_;
@synthesize tail = tail_;

- (instancetype)init {
  if ((self = [super init])) {
    accessOrder_ = NO;
#if ! __has_feature(objc_arc)
    [head_ autorelease];
#endif
    head_ = nil;
  }
  return self;
}

- (instancetype)initWithInt:(int)s {
  if ((self = [super initWithInt:s])) {
    accessOrder_ = NO;
#if ! __has_feature(objc_arc)
    [head_ autorelease];
#endif
    head_ = nil;
  }
  return self;
}

- (instancetype)initWithInt:(int)s
                  withFloat:(float)lf {
  if ((self = [super initWithInt:s withFloat:lf])) {
    accessOrder_ = NO;
#if ! __has_feature(objc_arc)
    [head_ autorelease];
    [tail_ autorelease];
#endif
    head_ = nil;
    tail_ = nil;
  }
  return self;
}

- (instancetype)initWithInt:(int)s
                  withFloat:(float)lf
                withBoolean:(BOOL)order {
  if ((self = [super initWithInt:s withFloat:lf])) {
    accessOrder_ = order;
#if ! __has_feature(objc_arc)
    [head_ autorelease];
    [tail_ autorelease];
#endif
    head_ = nil;
    tail_ = nil;
  }
  return self;
}

- (instancetype)initWithJavaUtilMap:(id<JavaUtilMap>)m {
  if ((self = [super init])) {
    accessOrder_ = NO;
#if ! __has_feature(objc_arc)
    [head_ autorelease];
    [tail_ autorelease];
#endif
    head_ = nil;
    tail_ = nil;
    [self putAllWithJavaUtilMap:m];
  }
  return self;
}

- (BOOL)containsValueWithId:(id)value {
  JavaUtilLinkedHashMap_LinkedHashMapEntry *entry = head_;
  if (nil == value) {
    while (nil != entry) {
      if (nil == entry->value_) {
        return YES;
      }
      entry = entry->chainForward_;
    }
  }
  else {
    while (nil != entry) {
      if ([value isEqual:entry->value_]) {
        return YES;
      }
      entry = entry->chainForward_;
    }
  }
  return NO;
}

- (id)getWithId:(id)key {
  JavaUtilLinkedHashMap_LinkedHashMapEntry *m;
  if (key == nil) {
    m = (JavaUtilLinkedHashMap_LinkedHashMapEntry *) [self findNullKeyEntry];
  }
  else {
    int hash_ = (int) [key hash];
    int index = (hash_ & (int) 0x7FFFFFFF) % elementDataLength_;
    m = (JavaUtilLinkedHashMap_LinkedHashMapEntry *)
        [self findNonNullKeyEntryWithId:key withInt:index withInt:hash_];
  }
  if (m == nil) {
    return nil;
  }
  if (accessOrder_ && tail_ != m) {
    JavaUtilLinkedHashMap_LinkedHashMapEntry *p = m->chainBackward_;
    JavaUtilLinkedHashMap_LinkedHashMapEntry *n = m->chainForward_;
    n->chainBackward_ = p;
    if (p != nil) {
      p->chainForward_ = n;
    }
    else {
      head_ = n;
    }
    m->chainForward_ = nil;
    m->chainBackward_ = tail_;
    tail_->chainForward_ = m;
    tail_ = m;
  }
  return m->value_;
}

- (JavaUtilHashMap_Entry *)createHashedEntryWithId:(id)key
                                           withInt:(int)index
                                           withInt:(int)hash_ {
  JavaUtilLinkedHashMap_LinkedHashMapEntry *m =
      [[JavaUtilLinkedHashMap_LinkedHashMapEntry alloc] initWithId:key withInt:hash_];
  m->next_ = elementData_[index];
  elementData_[index] = m;
  [self linkEntryWithJavaUtilLinkedHashMap_LinkedHashMapEntry:m];
  return m;
}

- (id)putWithId:(id)key
         withId:(id)value {
  id result = [self putImplWithId:key withId:value];
  if ([self removeEldestEntryWithJavaUtilMap_Entry:head_]) {
    [self removeWithId:head_->key_];
  }
  return result;
}

- (id)putImplWithId:(id)key
             withId:(id)value {
  JavaUtilLinkedHashMap_LinkedHashMapEntry *m;
  if (elementCount_ == 0) {
    head_ = tail_ = nil;
  }
  if (key == nil) {
    m = (JavaUtilLinkedHashMap_LinkedHashMapEntry *) [self findNullKeyEntry];
    if (m == nil) {
      modCount_++;
      if (++elementCount_ > threshold_) {
        [self rehash];
      }
      m = (JavaUtilLinkedHashMap_LinkedHashMapEntry *)
          [self createHashedEntryWithId:nil withInt:0 withInt:0];
    }
    else {
      [self linkEntryWithJavaUtilLinkedHashMap_LinkedHashMapEntry:m];
    }
  }
  else {
    int hash_ = (int) [key hash];
    int index = (hash_ & (int) 0x7FFFFFFF) % elementDataLength_;
    m = (JavaUtilLinkedHashMap_LinkedHashMapEntry *)
        [self findNonNullKeyEntryWithId:key withInt:index withInt:hash_];
    if (m == nil) {
      modCount_++;
      if (++elementCount_ > threshold_) {
        [self rehash];
        index = (hash_ & (int) 0x7FFFFFFF) % elementDataLength_;
      }
      m = (JavaUtilLinkedHashMap_LinkedHashMapEntry *)
          [self createHashedEntryWithId:key withInt:index withInt:hash_];
    }
    else {
      [self linkEntryWithJavaUtilLinkedHashMap_LinkedHashMapEntry:m];
    }
  }
  id result = m->value_;
#if ! __has_feature(objc_arc)
  [result autorelease];
  [value retain];
#endif
  m->value_ = value;
  return result;
}

- (void)linkEntryWithJavaUtilLinkedHashMap_LinkedHashMapEntry:(JavaUtilLinkedHashMap_LinkedHashMapEntry *)m {
  if (tail_ == m) {
    return;
  }
  if (head_ == nil) {
    head_ = tail_ = m;
    return;
  }
  JavaUtilLinkedHashMap_LinkedHashMapEntry *p = m->chainBackward_;
  JavaUtilLinkedHashMap_LinkedHashMapEntry *n = m->chainForward_;
  if (p == nil) {
    if (n != nil) {
      if (accessOrder_) {
        head_ = n;
        n->chainBackward_ = nil;
        m->chainBackward_ = tail_;
        m->chainForward_ = nil;
        tail_->chainForward_ = m;
        tail_ = m;
      }
    }
    else {
      m->chainBackward_ = tail_;
      m->chainForward_ = nil;
      tail_->chainForward_ = m;
      tail_ = m;
    }
    return;
  }
  if (n == nil) {
    return;
  }
  if (accessOrder_) {
    p->chainForward_ = n;
    n->chainBackward_ = p;
    m->chainForward_ = nil;
    m->chainBackward_ = tail_;
    tail_->chainForward_ = m;
    tail_ = m;
  }
}

- (id<JavaUtilSet>)entrySet {
  return AUTORELEASE([[JavaUtilLinkedHashMap_LinkedHashMapEntrySet alloc]
                      initWithJavaUtilLinkedHashMap:self]);
}

- (id<JavaUtilSet>)keySet {
  if (keySet__ == nil) {
    keySet__ = [[JavaUtilLinkedHashMap_KeySet alloc] initWithJavaUtilLinkedHashMap:self];
  }
  return keySet__;
}

- (id<JavaUtilCollection>)values {
  if (valuesCollection_ == nil) {
    valuesCollection_ =
        [[JavaUtilLinkedHashMap_ValuesCollection alloc] initWithJavaUtilLinkedHashMap:self];
  }
  return valuesCollection_;
}

- (id)removeWithId:(id)key {
  JavaUtilLinkedHashMap_LinkedHashMapEntry *m =
      (JavaUtilLinkedHashMap_LinkedHashMapEntry *) [self removeEntryWithId:key];
  if (m == nil) {
    return nil;
  }
  JavaUtilLinkedHashMap_LinkedHashMapEntry *p = m->chainBackward_;
  JavaUtilLinkedHashMap_LinkedHashMapEntry *n = m->chainForward_;
  if (p != nil) {
    p->chainForward_ = n;
  } else {
    head_ = n;
  }
  if (n != nil) {
    n->chainBackward_ = p;
  }
  else {
    tail_ = p;
  }
  return m->value_;
}

- (BOOL)removeEldestEntryWithJavaUtilMap_Entry:(id<JavaUtilMap_Entry>)eldest {
  return NO;
}

- (void)clear {
  [super clear];
  head_ = tail_ = nil;
}

- (void)copyAllFieldsTo:(JavaUtilLinkedHashMap *)other {
  [super copyAllFieldsTo:other];
  other.accessOrder = accessOrder_;
}

- (id<JavaUtilMap_Entry>)eldest {
  return head_;
}

@end


@implementation JavaUtilLinkedHashMap_AbstractMapIterator

- (instancetype)initWithJavaUtilLinkedHashMap:(JavaUtilLinkedHashMap *)map {
  if ((self = [super init])) {
    expectedModCount_ = map.modCount;
    futureEntry_ = map.head;
#if ! __has_feature(objc_arc)
    [associatedMap_ autorelease];
    associatedMap_ = [map retain];
#endif
  }
  return self;
}

- (BOOL)hasNext {
  return (futureEntry_ != nil);
}

- (void)checkConcurrentMod {
  if (expectedModCount_ != associatedMap_.modCount) {
    @throw AUTORELEASE([[JavaUtilConcurrentModificationException alloc] init]);
  }
}

- (void)makeNext {
  [self checkConcurrentMod];
  if (![self hasNext]) {
    @throw AUTORELEASE([[JavaUtilNoSuchElementException alloc] init]);
  }
  currentEntry_ = futureEntry_;
  futureEntry_ = futureEntry_->chainForward_;
}

- (void)remove {
  [self checkConcurrentMod];
  if (currentEntry_ == nil) {
    @throw AUTORELEASE([[JavaLangIllegalStateException alloc] init]);
  }
  [associatedMap_ removeEntryWithJavaUtilHashMap_Entry:currentEntry_];
  JavaUtilLinkedHashMap_LinkedHashMapEntry *lhme = currentEntry_;
  JavaUtilLinkedHashMap_LinkedHashMapEntry *p = lhme->chainBackward_;
  JavaUtilLinkedHashMap_LinkedHashMapEntry *n = lhme->chainForward_;
  JavaUtilLinkedHashMap *lhm = associatedMap_;
  if (p != nil) {
    p->chainForward_ = n;
    if (n != nil) {
      n->chainBackward_ = p;
    }
    else {
      lhm.tail = p;
    }
  }
  else {
    lhm.head = n;
    if (n != nil) {
      n->chainBackward_ = nil;
    } else {
      lhm.tail = nil;
    }
  }
  currentEntry_ = nil;
  expectedModCount_++;
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [associatedMap_ autorelease];
  [super dealloc];
}
#endif

@end


@implementation JavaUtilLinkedHashMap_EntryIterator

- (instancetype)initWithJavaUtilLinkedHashMap:(JavaUtilLinkedHashMap *)map {
  return [super initWithJavaUtilLinkedHashMap:map];
}

- (id<JavaUtilMap_Entry>)next {
  [self makeNext];
  return currentEntry_;
}

@end


@implementation JavaUtilLinkedHashMap_KeyIterator

- (instancetype)initWithJavaUtilLinkedHashMap:(JavaUtilLinkedHashMap *)map {
  return [super initWithJavaUtilLinkedHashMap:map];
}

- (id)next {
  [self makeNext];
  return ((JavaUtilLinkedHashMap_LinkedHashMapEntry *) nil_chk(currentEntry_))->key_;
}

@end


@implementation JavaUtilLinkedHashMap_ValueIterator

- (instancetype)initWithJavaUtilLinkedHashMap:(JavaUtilLinkedHashMap *)map {
  return [super initWithJavaUtilLinkedHashMap:map];
}

- (id)next {
  [self makeNext];
  return ((JavaUtilLinkedHashMap_LinkedHashMapEntry *) nil_chk(currentEntry_))->value_;
}

@end


@implementation JavaUtilLinkedHashMap_LinkedHashMapEntrySet

- (instancetype)initWithJavaUtilLinkedHashMap:(JavaUtilLinkedHashMap *)lhm {
  return [super initWithJavaUtilHashMap:lhm];
}

- (id<JavaUtilIterator>)iterator {
  return AUTORELEASE([[JavaUtilLinkedHashMap_EntryIterator alloc]
                      initWithJavaUtilLinkedHashMap:(JavaUtilLinkedHashMap *) [self hashMap]]);
}

- (NSUInteger)countByEnumeratingWithState:(NSFastEnumerationState *)state
                                  objects:(__unsafe_unretained id *)stackbuf
                                    count:(NSUInteger)len {
  return EnumerateEntries([self hashMap], state, stackbuf, len);
}

@end


@implementation JavaUtilLinkedHashMap_LinkedHashMapEntry

- (instancetype)initWithId:(id)theKey
                    withId:(id)theValue {
  if ((self = [super initWithId:theKey withId:theValue])) {
    chainForward_ = nil;
    chainBackward_ = nil;
  }
  return self;
}

- (instancetype)initWithId:(id)theKey
                   withInt:(int)hash_ {
  if ((self = [super initWithId:theKey withInt:hash_])) {
    chainForward_ = nil;
    chainBackward_ = nil;
  }
  return self;
}

@end


@implementation JavaUtilLinkedHashMap_KeySet

- (BOOL)containsWithId:(id)object {
  return [outer_ containsKeyWithId:object];
}

- (int)size {
  return [outer_ size];
}

- (void)clear {
  [outer_ clear];
}

- (BOOL)removeWithId:(id)key {
  if ([outer_ containsKeyWithId:key]) {
    [outer_ removeWithId:key];
    return YES;
  }
  return NO;
}

- (id<JavaUtilIterator>)iterator {
  return AUTORELEASE([[JavaUtilLinkedHashMap_KeyIterator alloc]
                      initWithJavaUtilLinkedHashMap:outer_]);
}

- (instancetype)initWithJavaUtilLinkedHashMap:(JavaUtilLinkedHashMap *)outer {
  if ((self = [super init])) {
    outer_ = outer;
  }
  return self;
}

- (NSUInteger)countByEnumeratingWithState:(NSFastEnumerationState *)state
                                  objects:(__unsafe_unretained id *)stackbuf
                                    count:(NSUInteger)len {
  NSUInteger objCount = EnumerateEntries(outer_, state, stackbuf, len);
  __unsafe_unretained id *entries = state->itemsPtr;
  __unsafe_unretained id *end = entries + objCount;
  while (entries < end) {
    *entries = ((JavaUtilMapEntry *) *entries)->key_;
    entries++;
  }
  return objCount;
}

@end


@implementation JavaUtilLinkedHashMap_ValuesCollection

- (BOOL)containsWithId:(id)object {
  return [outer_ containsValueWithId:object];
}

- (int)size {
  return [outer_ size];
}

- (void)clear {
  [outer_ clear];
}

- (id<JavaUtilIterator>)iterator {
  return AUTORELEASE([[JavaUtilLinkedHashMap_ValueIterator alloc]
                      initWithJavaUtilLinkedHashMap:outer_]);
}

- (instancetype)initWithJavaUtilLinkedHashMap:(JavaUtilLinkedHashMap *)outer {
  if ((self = [super init])) {
    outer_ = outer;
  }
  return self;
}

- (NSUInteger)countByEnumeratingWithState:(NSFastEnumerationState *)state
                                  objects:(__unsafe_unretained id *)stackbuf
                                    count:(NSUInteger)len {
  NSUInteger objCount = EnumerateEntries(outer_, state, stackbuf, len);
  __unsafe_unretained id *entries = state->itemsPtr;
  __unsafe_unretained id *end = entries + objCount;
  while (entries < end) {
    *entries = ((JavaUtilMapEntry *) *entries)->value_;
    entries++;
  }
  return objCount;
}

@end

NSUInteger EnumerateEntries(
    JavaUtilLinkedHashMap *map, NSFastEnumerationState *state, __unsafe_unretained id *stackbuf,
    NSUInteger len) {
  __unsafe_unretained JavaUtilLinkedHashMap_LinkedHashMapEntry *entry;
  if (state->state == 0) {
    state->state = 1;
    state->mutationsPtr = (unsigned long *) &map->modCount_;
    entry = map->head_;
  } else {
    entry = (ARCBRIDGE id) (void *) state->extra[0];
  }
  state->itemsPtr = stackbuf;
  NSUInteger objCount = 0;
  while (entry && objCount < len) {
    *stackbuf++ = entry;
    objCount++;
    entry = entry->chainForward_;
  }
  state->extra[0] = (unsigned long) entry;
  return objCount;
}
