//
//  LinkedHashMap.m
//  JreEmulation
//
//  Created by Keith Stanger on 11/06/12.
//  Copyright 2012 Google, Inc. All rights reserved.
//

#import "IOSClass.h"
#import "java/lang/IllegalStateException.h"
#import "java/util/Collection.h"
#import "java/util/ConcurrentModificationException.h"
#import "java/util/HashMap.h"
#import "java/util/HashMap_PackagePrivate.h"
#import "java/util/Iterator.h"
#import "java/util/LinkedHashMap_PackagePrivate.h"
#import "java/util/Map.h"
#import "java/util/NoSuchElementException.h"
#import "java/util/Set.h"

@implementation JavaUtilLinkedHashMap

@synthesize accessOrder = accessOrder_;
@synthesize head = head_;
@synthesize tail = tail_;

- (id)init {
  if ((self = [super init])) {
    accessOrder_ = NO;
#if ! __has_feature(objc_arc)
    [head_ autorelease];
#endif
    head_ = nil;
  }
  return self;
}

- (id)initWithInt:(int)s {
  if ((self = [super initWithInt:s])) {
    accessOrder_ = NO;
#if ! __has_feature(objc_arc)
    [head_ autorelease];
#endif
    head_ = nil;
  }
  return self;
}

- (id)initWithInt:(int)s
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

- (id)initWithInt:(int)s
        withFloat:(float)lf
         withBOOL:(BOOL)order {
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

- (id)initWithJavaUtilMap:(id<JavaUtilMap>)m {
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
    int hash_ = [key hash];
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
    int hash_ = [key hash];
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

- (void)copyAllPropertiesTo:(id)copy {
  [super copyAllPropertiesTo:copy];
  JavaUtilLinkedHashMap *typedCopy = (JavaUtilLinkedHashMap *) copy;
  typedCopy.accessOrder = accessOrder_;
}

@end


@implementation JavaUtilLinkedHashMap_AbstractMapIterator

- (id)initWithJavaUtilLinkedHashMap:(JavaUtilLinkedHashMap *)map {
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

- (id)initWithJavaUtilLinkedHashMap:(JavaUtilLinkedHashMap *)map {
  return [super initWithJavaUtilLinkedHashMap:map];
}

- (id<JavaUtilMap_Entry>)next {
  [self makeNext];
  return currentEntry_;
}

@end


@implementation JavaUtilLinkedHashMap_KeyIterator

- (id)initWithJavaUtilLinkedHashMap:(JavaUtilLinkedHashMap *)map {
  return [super initWithJavaUtilLinkedHashMap:map];
}

- (id)next {
  [self makeNext];
  return ((JavaUtilLinkedHashMap_LinkedHashMapEntry *) NIL_CHK(currentEntry_)).key;
}

@end


@implementation JavaUtilLinkedHashMap_ValueIterator

- (id)initWithJavaUtilLinkedHashMap:(JavaUtilLinkedHashMap *)map {
  return [super initWithJavaUtilLinkedHashMap:map];
}

- (id)next {
  [self makeNext];
  return ((JavaUtilLinkedHashMap_LinkedHashMapEntry *) NIL_CHK(currentEntry_)).value;
}

@end


@implementation JavaUtilLinkedHashMap_LinkedHashMapEntrySet

- (id)initWithJavaUtilLinkedHashMap:(JavaUtilLinkedHashMap *)lhm {
  return [super initWithJavaUtilHashMap:lhm];
}

- (id<JavaUtilIterator>)iterator {
  return AUTORELEASE([[JavaUtilLinkedHashMap_EntryIterator alloc]
                      initWithJavaUtilLinkedHashMap:(JavaUtilLinkedHashMap *) [self hashMap]]);
}

@end


@implementation JavaUtilLinkedHashMap_LinkedHashMapEntry

- (id)initWithId:(id)theKey
          withId:(id)theValue {
  if ((self = [super initWithId:theKey withId:theValue])) {
    chainForward_ = nil;
    chainBackward_ = nil;
  }
  return self;
}

- (id)initWithId:(id)theKey
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

- (id)initWithJavaUtilLinkedHashMap:(JavaUtilLinkedHashMap *)outer {
  if ((self = [super init])) {
#if ! __has_feature(objc_arc)
    [outer_ autorelease];
#endif
    outer_ = outer;
#if ! __has_feature(objc_arc)
    [outer retain];
#endif
  }
  return self;
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [outer_ autorelease];
  [super dealloc];
}
#endif

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

- (id)initWithJavaUtilLinkedHashMap:(JavaUtilLinkedHashMap *)outer {
  if ((self = [super init])) {
#if ! __has_feature(objc_arc)
    [outer_ autorelease];
#endif
    outer_ = outer;
#if ! __has_feature(objc_arc)
    [outer retain];
#endif
  }
  return self;
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [outer_ autorelease];
  [super dealloc];
}
#endif

@end
