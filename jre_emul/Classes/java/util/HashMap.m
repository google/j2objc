//
//  HashMap.m
//  JreEmulation
//
//  Created by Keith Stanger on 10/19/12.
//  Copyright 2012 Google, Inc. All rights reserved.
//

#include "java/lang/CloneNotSupportedException.h"
#include "java/lang/IllegalArgumentException.h"
#include "java/lang/IllegalStateException.h"
#include "java/lang/NullPointerException.h"
#include "java/util/Collection.h"
#include "java/util/ConcurrentModificationException.h"
#include "java/util/HashMap_PackagePrivate.h"
#include "java/util/Iterator.h"
#include "java/util/Map.h"
#include "java/util/NoSuchElementException.h"
#include "java/util/Set.h"

#if __has_feature(objc_arc)
#error "JavaUtilHashMap is not built with ARC"
#endif

static NSUInteger EnumerateEntries(
    JavaUtilHashMap *map, NSFastEnumerationState *state, __unsafe_unretained id *stackbuf,
    NSUInteger len);

@implementation JavaUtilHashMap

@synthesize elementCount = elementCount_;
@synthesize elementDataLength = elementDataLength_;
@synthesize modCount = modCount_;
@synthesize loadFactor = loadFactor_;
@synthesize threshold = threshold_;


- (instancetype)init {
  return [self initJavaUtilHashMapWithInt:JavaUtilHashMap_DEFAULT_SIZE
                                withFloat:JavaUtilHashMap_DEFAULT_LOAD_FACTOR];
}

- (instancetype)initWithInt:(int)capacity {
  return [self initJavaUtilHashMapWithInt:capacity withFloat:JavaUtilHashMap_DEFAULT_LOAD_FACTOR];
}

+ (int)calculateCapacityWithInt:(int)x {
  if (x >= 1 << 30) {
    return 1 << 30;
  }
  if (x == 0) {
    return 16;
  }
  x = x - 1;
  x |= x >> 1;
  x |= x >> 2;
  x |= x >> 4;
  x |= x >> 8;
  x |= x >> 16;
  return x + 1;
}

- (instancetype)initJavaUtilHashMapWithInt:(int)capacity
                                 withFloat:(float)loadFactor {
  if ((self = [super init])) {
    JreMemDebugAdd(self);
    modCount_ = 0;
    if (capacity >= 0 && loadFactor > 0) {
      capacity = [JavaUtilHashMap calculateCapacityWithInt:capacity];
      elementCount_ = 0;
      free(elementData_);
      elementData_ = (JavaUtilHashMap_Entry * __unsafe_unretained *)
          calloc(capacity, sizeof(JavaUtilHashMap_Entry *));
      elementDataLength_ = capacity;
      self.loadFactor = loadFactor;
      [self computeThreshold];
    }
    else {
      @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc] init]);
    }
  }
  return self;
}

- (instancetype)initWithInt:(int)capacity
                  withFloat:(float)loadFactor {
  return [self initJavaUtilHashMapWithInt:capacity withFloat:loadFactor];
}

- (instancetype)initWithJavaUtilMap:(id<JavaUtilMap>)map {
  if ((self = [self initJavaUtilHashMapWithInt:
      [JavaUtilHashMap calculateCapacityWithInt:[((id<JavaUtilMap>) nil_chk(map)) size]]
      withFloat:JavaUtilHashMap_DEFAULT_LOAD_FACTOR])) {
    [self putAllImplWithJavaUtilMap:map];
  }
  return self;
}

- (void)clear {
  if (elementCount_ > 0) {
    elementCount_ = 0;
    for (int i = 0; i < elementDataLength_; i++) {
      JavaUtilHashMap_Entry *entry = elementData_[i];
      elementData_[i] = nil;
      while (entry != nil) {
        JavaUtilHashMap_Entry *next = entry->next_;
#if ! __has_feature(objc_arc)
        [entry release];
#endif
        entry = next;
      }
    }
    modCount_++;
  }
}

- (void)copyAllFieldsTo:(JavaUtilHashMap *)other {
  [super copyAllFieldsTo:other];
  other.modCount = modCount_;
  other.loadFactor = loadFactor_;
  other.threshold = threshold_;
}

- (instancetype)clone {
  @try {
    JavaUtilHashMap *map = (JavaUtilHashMap *) [super clone];
    map.elementCount = 0;
    // map->elementData_ is NULL at this point.
    map->elementData_ = (JavaUtilHashMap_Entry * __unsafe_unretained *)
        calloc(elementDataLength_, sizeof(JavaUtilHashMap_Entry *));
    map.elementDataLength = elementDataLength_;
    [map putAllWithJavaUtilMap:self];
    return map;
  }
  @catch (JavaLangCloneNotSupportedException *e) {
    return nil;
  }
}

- (void)computeThreshold {
  threshold_ = (int) (elementDataLength_ * loadFactor_);
}

- (BOOL)containsKeyWithId:(id)key {
  JavaUtilHashMap_Entry *m = [self getEntryWithId:key];
  return m != nil;
}

- (BOOL)containsValueWithId:(id)value {
  if (value != nil) {
    for (int i = 0; i < elementDataLength_; i++) {
      JavaUtilHashMap_Entry *entry = elementData_[i];
      while (entry != nil) {
        if ([JavaUtilHashMap areEqualValuesWithId:value withId:entry->value_]) {
          return YES;
        }
        entry = entry->next_;
      }
    }
  }
  else {
    for (int i = 0; i < elementDataLength_; i++) {
      JavaUtilHashMap_Entry *entry = elementData_[i];
      while (entry != nil) {
        if (entry->value_ == nil) {
          return YES;
        }
        entry = entry->next_;
      }
    }
  }
  return NO;
}

- (id<JavaUtilSet>)entrySet {
  return AUTORELEASE([[JavaUtilHashMap_HashMapEntrySet alloc]
                      initWithJavaUtilHashMap:self]);
}

- (id)getWithId:(id)key {
  JavaUtilHashMap_Entry *m = [self getEntryWithId:key];
  if (m != nil) {
    return m->value_;
  }
  return nil;
}

- (JavaUtilHashMap_Entry *)getEntryWithId:(id)key {
  JavaUtilHashMap_Entry *m;
  if (key == nil) {
    m = [self findNullKeyEntry];
  }
  else {
    int hash_ = [JavaUtilHashMap computeHashCodeWithId:key];
    int index = hash_ & (elementDataLength_ - 1);
    m = [self findNonNullKeyEntryWithId:key withInt:index withInt:hash_];
  }
  return m;
}

- (JavaUtilHashMap_Entry *)findNonNullKeyEntryWithId:(id)key
                                             withInt:(int)index
                                             withInt:(int)keyHash {
  JavaUtilHashMap_Entry *m = elementData_[index];
  while (m != nil
      && (m->origKeyHash_ != keyHash || ![JavaUtilHashMap areEqualKeysWithId:key withId:m->key_])) {
    m = m->next_;
  }
  return m;
}

- (JavaUtilHashMap_Entry *)findNullKeyEntry {
  JavaUtilHashMap_Entry *m = elementData_[0];
  while (m != nil && m->key_ != nil) m = m->next_;
  return m;
}

- (BOOL)isEmpty {
  return elementCount_ == 0;
}

- (id<JavaUtilSet>)keySet {
  if (keySet__ == nil) {
    keySet__ = [[JavaUtilHashMap_KeySet alloc] initWithJavaUtilHashMap:self];
  }
  return keySet__;
}

- (id)putWithId:(id)key
         withId:(id)value {
  return [self putImplWithId:key withId:value];
}

- (id)putImplWithId:(id)key
             withId:(id)value {
  JavaUtilHashMap_Entry *entry;
  if (key == nil) {
    entry = [self findNullKeyEntry];
    if (entry == nil) {
      modCount_++;
      entry = [self createHashedEntryWithId:nil withInt:0 withInt:0];
      if (++elementCount_ > threshold_) {
        [self rehash];
      }
    }
  }
  else {
    int hash_ = [JavaUtilHashMap computeHashCodeWithId:key];
    int index = hash_ & (elementDataLength_ - 1);
    entry = [self findNonNullKeyEntryWithId:key withInt:index withInt:hash_];
    if (entry == nil) {
      modCount_++;
      entry = [self createHashedEntryWithId:key withInt:index withInt:hash_];
      if (++elementCount_ > threshold_) {
        [self rehash];
      }
    }
  }
#if __has_feature(objc_arc)
  id result = entry->value_;
  entry->value_ = value;
#else
  id result = [entry->value_ autorelease];
  entry->value_ = [value retain];
#endif
  return result;
}

- (JavaUtilHashMap_Entry *)createHashedEntryWithId:(id)key
                                           withInt:(int)index
                                           withInt:(int)hash_ {
  JavaUtilHashMap_Entry *entry = [[JavaUtilHashMap_Entry alloc] initWithId:key withInt:hash_];
  entry->next_ = elementData_[index];
  elementData_[index] = entry;
  return entry;
}

- (void)putAllWithJavaUtilMap:(id<JavaUtilMap>)map {
  if (![((id<JavaUtilMap>) nil_chk(map)) isEmpty]) {
    [self putAllImplWithJavaUtilMap:map];
  }
}

- (void)putAllImplWithJavaUtilMap:(id<JavaUtilMap>)map {
  int capacity = elementCount_ + [((id<JavaUtilMap>) nil_chk(map)) size];
  if (capacity > threshold_) {
    [self rehashWithInt:capacity];
  }
  {
    id<JavaLangIterable> array__ = (id<JavaLangIterable>) [((id<JavaUtilMap>) nil_chk(map)) entrySet];
    if (!array__) {
      @throw AUTORELEASE([[JavaLangNullPointerException alloc] init]);
    }
    id<JavaUtilIterator> iter__ = [array__ iterator];
    while ([iter__ hasNext]) {
      id<JavaUtilMap_Entry> entry = (id<JavaUtilMap_Entry>) [iter__ next];
      [self putImplWithId:((id) [((id<JavaUtilMap_Entry>) nil_chk(entry)) getKey]) withId:((id) [((id<JavaUtilMap_Entry>) nil_chk(entry)) getValue])];
    }
  }
}

- (void)rehashWithInt:(int)capacity {
  int length = [JavaUtilHashMap calculateCapacityWithInt:(capacity == 0 ? 1 : capacity << 1)];
  JavaUtilHashMap_Entry * __unsafe_unretained *newData =
      (JavaUtilHashMap_Entry * __unsafe_unretained *)
      calloc(length, sizeof(JavaUtilHashMap_Entry *));
  for (int i = 0; i < elementDataLength_; i++) {
    JavaUtilHashMap_Entry *entry = elementData_[i];
    elementData_[i] = nil;
    while (entry != nil) {
      int index = entry->origKeyHash_ & (length - 1);
      JavaUtilHashMap_Entry *next = entry->next_;
      entry->next_ = newData[index];
      newData[index] = entry;
      entry = next;
    }
  }
  free(elementData_);
  elementData_ = newData;
  elementDataLength_ = length;
  [self computeThreshold];
}

- (void)rehash {
  [self rehashWithInt:elementDataLength_];
}

- (id)removeWithId:(id)key {
  JavaUtilHashMap_Entry *entry = [self removeEntryWithId:key];
  if (entry != nil) {
    return entry->value_;
  }
  return nil;
}

- (void)removeEntryWithJavaUtilHashMap_Entry:(JavaUtilHashMap_Entry *)entry {
  int index = entry->origKeyHash_ & (elementDataLength_ - 1);
  JavaUtilHashMap_Entry *m = elementData_[index];
  if (m == entry) {
    elementData_[index] = entry->next_;
  }
  else {
    while (m->next_ != entry) {
      m = m->next_;
    }
    m->next_ = entry->next_;
  }
  modCount_++;
  elementCount_--;
#if ! __has_feature(objc_arc)
  [entry autorelease];
#endif
}

- (JavaUtilHashMap_Entry *)removeEntryWithId:(id)key {
  int index = 0;
  JavaUtilHashMap_Entry *entry;
  JavaUtilHashMap_Entry *last = nil;
  if (key != nil) {
    int hash_ = [JavaUtilHashMap computeHashCodeWithId:key];
    index = hash_ & (elementDataLength_ - 1);
    entry = elementData_[index];
    while (entry != nil
        && !(entry->origKeyHash_ == hash_
            && [JavaUtilHashMap areEqualKeysWithId:key withId:entry->key_])) {
      last = entry;
      entry = entry->next_;
    }
  }
  else {
    entry = elementData_[0];
    while (entry != nil && entry->key_ != nil) {
      last = entry;
      entry = entry->next_;
    }
  }
  if (entry == nil) {
    return nil;
  }
  if (last == nil) {
    elementData_[index] = entry->next_;
  } else {
    last->next_ = entry->next_;
  }
  modCount_++;
  elementCount_--;
  return AUTORELEASE(entry);
}

- (int)size {
  return elementCount_;
}

- (id<JavaUtilCollection>)values {
  if (valuesCollection_ == nil) {
    valuesCollection_ = [[JavaUtilHashMap_ValuesCollection alloc] initWithJavaUtilHashMap:self];
  }
  return valuesCollection_;
}

+ (int)computeHashCodeWithId:(id)key {
  return (int) [nil_chk(key) hash];
}

+ (BOOL)areEqualKeysWithId:(id)key1
                    withId:(id)key2 {
  return (key1 == key2) || [nil_chk(key1) isEqual:key2];
}

+ (BOOL)areEqualValuesWithId:(id)value1
                      withId:(id)value2 {
  return (value1 == value2) || [nil_chk(value1) isEqual:value2];
}

- (instancetype)copyWithZone:(NSZone *)zone {
  id clone = [self clone];
#if ! __has_feature(objc_arc)
  [clone retain];
#endif
  return clone;
}

- (void)dealloc {
  JreMemDebugRemove(self);
  [self clear];
  free(elementData_);
  elementData_ = nil;
#if ! __has_feature(objc_arc)
  [super dealloc];
#endif
}

- (NSArray *)memDebugStrongReferences {
  NSMutableArray *result =
      AUTORELEASE([[super memDebugStrongReferences] mutableCopy]);
  for (int i = 0; i < elementDataLength_; i++) {
    JavaUtilHashMap_Entry *entry = elementData_[i];
    while (entry != nil) {
      [result addObject:[JreMemDebugStrongReference
          strongReferenceWithObject:entry name:@"elementData"]];
      entry = entry->next_;
    }
  }
  return result;
}

@end


@implementation JavaUtilHashMap_Entry

- (instancetype)initWithId:(id)theKey
                   withInt:(int)hash_ {
  if ((self = [super initWithId:theKey withId:nil])) {
    origKeyHash_ = hash_;
  }
  return self;
}

- (instancetype)initWithId:(id)theKey
                    withId:(id)theValue {
  if ((self = [super initWithId:theKey withId:theValue])) {
    origKeyHash_ = (theKey == nil ? 0 : [JavaUtilHashMap computeHashCodeWithId:theKey]);
  }
  return self;
}

@end


@implementation JavaUtilHashMap_AbstractMapIterator

@synthesize position = position_;
@synthesize expectedModCount = expectedModCount_;
@synthesize futureEntry = futureEntry_;
@synthesize currentEntry = currentEntry_;
@synthesize prevEntry = prevEntry_;
@synthesize associatedMap = associatedMap_;


- (instancetype)initWithJavaUtilHashMap:(JavaUtilHashMap *)hm {
  if ((self = [super init])) {
    position_ = 0;
#if ! __has_feature(objc_arc)
    [associatedMap_ autorelease];
#endif
    associatedMap_ = hm;
#if ! __has_feature(objc_arc)
    [associatedMap_  retain];
#endif
    expectedModCount_ = ((JavaUtilHashMap *) nil_chk(hm)).modCount;
    futureEntry_ = nil;
  }
  return self;
}

- (BOOL)hasNext {
  if (futureEntry_ != nil) {
    return YES;
  }
  while (position_ < associatedMap_.elementDataLength) {
    if (associatedMap_->elementData_[position_] == nil) {
      position_++;
    }
    else {
      return YES;
    }
  }
  return NO;
}

- (void)checkConcurrentMod {
  if (expectedModCount_ != ((JavaUtilHashMap *) nil_chk(associatedMap_)).modCount) {
    @throw AUTORELEASE([[JavaUtilConcurrentModificationException alloc] init]);
  }
}

- (void)makeNext {
  [self checkConcurrentMod];
  if (![self hasNext]) {
    @throw AUTORELEASE([[JavaUtilNoSuchElementException alloc] init]);
  }
  if (futureEntry_ == nil) {
    currentEntry_ = associatedMap_->elementData_[position_++];
    futureEntry_ = currentEntry_->next_;
    prevEntry_ = nil;
  }
  else {
    if (currentEntry_ != nil) {
      prevEntry_ = currentEntry_;
    }
    currentEntry_ = futureEntry_;
    futureEntry_ = futureEntry_->next_;
  }
}

- (void)remove {
  [self checkConcurrentMod];
  if (currentEntry_ == nil) {
    @throw AUTORELEASE([[JavaLangIllegalStateException alloc] init]);
  }
  if (prevEntry_ == nil) {
    int index = currentEntry_->origKeyHash_ & (associatedMap_.elementDataLength - 1);
    associatedMap_->elementData_[index] = associatedMap_->elementData_[index]->next_;
  }
  else {
    prevEntry_->next_ = currentEntry_->next_;
  }
#if ! __has_feature(objc_arc)
  [currentEntry_ autorelease];
#endif
  currentEntry_ = nil;
  expectedModCount_++;
  associatedMap_.modCount++;
  associatedMap_.elementCount--;
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [associatedMap_ autorelease];
  [super dealloc];
}
#endif

@end


@implementation JavaUtilHashMap_EntryIterator

- (instancetype)initWithJavaUtilHashMap:(JavaUtilHashMap *)map {
  return [super initWithJavaUtilHashMap:map];
}

- (id<JavaUtilMap_Entry>)next {
  [self makeNext];
  return currentEntry_;
}

@end


@implementation JavaUtilHashMap_KeyIterator

- (instancetype)initWithJavaUtilHashMap:(JavaUtilHashMap *)map {
  return [super initWithJavaUtilHashMap:map];
}

- (id)next {
  [self makeNext];
  return ((JavaUtilHashMap_Entry *) nil_chk(currentEntry_))->key_;
}

@end


@implementation JavaUtilHashMap_ValueIterator

- (instancetype)initWithJavaUtilHashMap:(JavaUtilHashMap *)map {
  return [super initWithJavaUtilHashMap:map];
}

- (id)next {
  [self makeNext];
  return ((JavaUtilHashMap_Entry *) nil_chk(currentEntry_))->value_;
}

@end


@implementation JavaUtilHashMap_HashMapEntrySet

- (instancetype)initWithJavaUtilHashMap:(JavaUtilHashMap *)hm {
  if ((self = [super init])) {
    associatedMap_ = hm;
  }
  return self;
}

- (JavaUtilHashMap *)hashMap {
  return associatedMap_;
}

- (int)size {
  return ((JavaUtilHashMap *) nil_chk(associatedMap_)).elementCount;
}

- (void)clear {
  [((JavaUtilHashMap *) nil_chk(associatedMap_)) clear];
}

- (BOOL)removeWithId:(id)object {
  if ([object conformsToProtocol: @protocol(JavaUtilMap_Entry)]) {
    id<JavaUtilMap_Entry> oEntry = (id<JavaUtilMap_Entry>) object;
    JavaUtilHashMap_Entry *entry = [associatedMap_ getEntryWithId:[oEntry getKey]];
    if ([JavaUtilHashMap_HashMapEntrySet valuesEqWithJavaUtilHashMap_Entry:entry
                                                     withJavaUtilMap_Entry:oEntry]) {
      [associatedMap_ removeEntryWithJavaUtilHashMap_Entry:entry];
      return YES;
    }
  }
  return NO;
}

- (BOOL)containsWithId:(id)object {
  if ([object conformsToProtocol: @protocol(JavaUtilMap_Entry)]) {
    id<JavaUtilMap_Entry> oEntry = (id<JavaUtilMap_Entry>) object;
    JavaUtilHashMap_Entry *entry = [associatedMap_ getEntryWithId:[oEntry getKey]];
    return [JavaUtilHashMap_HashMapEntrySet valuesEqWithJavaUtilHashMap_Entry:entry
                                                        withJavaUtilMap_Entry:oEntry];
  }
  return NO;
}

+ (BOOL)valuesEqWithJavaUtilHashMap_Entry:(JavaUtilHashMap_Entry *)entry
                    withJavaUtilMap_Entry:(id<JavaUtilMap_Entry>)oEntry {
  return (entry != nil)
      && ((entry->value_ == nil) ? ([oEntry getValue] == nil)
          : ([JavaUtilHashMap areEqualValuesWithId:entry->value_ withId:[oEntry getValue]]));
}

- (id<JavaUtilIterator>)iterator {
  return AUTORELEASE([[JavaUtilHashMap_EntryIterator alloc]
                      initWithJavaUtilHashMap:associatedMap_]);
}

- (NSUInteger)countByEnumeratingWithState:(NSFastEnumerationState *)state
                                  objects:(__unsafe_unretained id *)stackbuf
                                    count:(NSUInteger)len {
  return EnumerateEntries(associatedMap_, state, stackbuf, len);
}

@end


@implementation JavaUtilHashMap_KeySet

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
  JavaUtilHashMap_Entry *entry = [outer_ removeEntryWithId:key];
  return entry != nil;
}

- (id<JavaUtilIterator>)iterator {
  return AUTORELEASE([[JavaUtilHashMap_KeyIterator alloc]
                      initWithJavaUtilHashMap:outer_]);
}

- (instancetype)initWithJavaUtilHashMap:(JavaUtilHashMap *)outer {
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
    *entries = ((JavaUtilHashMap_Entry *) *entries)->key_;
    entries++;
  }
  return objCount;
}

@end


@implementation JavaUtilHashMap_ValuesCollection

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
  return AUTORELEASE([[JavaUtilHashMap_ValueIterator alloc]
                      initWithJavaUtilHashMap:outer_]);
}

- (instancetype)initWithJavaUtilHashMap:(JavaUtilHashMap *)outer {
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
    *entries = ((JavaUtilHashMap_Entry *) *entries)->value_;
    entries++;
  }
  return objCount;
}

@end

NSUInteger EnumerateEntries(
    JavaUtilHashMap *map, NSFastEnumerationState *state, __unsafe_unretained id *stackbuf,
    NSUInteger len) {
  // Note: Must not use extra[4] because it is set by HashSet.
  if (state->state == 0) {
    state->state = 1;
    state->mutationsPtr = (unsigned long *) &map->modCount_;
    state->extra[0] = 0;
    state->extra[1] = 0;
  }
  NSUInteger position = state->extra[0];
  __unsafe_unretained JavaUtilHashMap_Entry *entry = (ARCBRIDGE id) (void *) state->extra[1];
  state->itemsPtr = stackbuf;
  NSUInteger objCount = 0;
  while (objCount < len) {
    if (entry) {
      entry = entry->next_;
    }
    while (!entry && position < (NSUInteger) map->elementDataLength_) {
      entry = map->elementData_[position++];
    }
    if (!entry) {
      break;
    }
    *stackbuf++ = entry;
    objCount++;
  }
  state->extra[0] = position;
  state->extra[1] = (unsigned long) entry;
  return objCount;
}
