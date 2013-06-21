//
//  HashMap_PackagePrivate.h
//  JreEmulation
//
//  Created by Keith Stanger on 10/19/12.
//  Copyright 2012 Google, Inc. All rights reserved.
//

#ifndef _JavaUtilHashMap_PackagePrivate_H_
#define _JavaUtilHashMap_PackagePrivate_H_

#include "JreEmulation.h"

#include "java/io/Serializable.h"
#include "java/util/AbstractCollection.h"
#include "java/util/AbstractMap.h"
#include "java/util/AbstractSet.h"
#include "java/util/HashMap.h"
#include "java/util/Iterator.h"
#include "java/util/Map.h"
#include "java/util/MapEntry.h"

#define JavaUtilHashMap_DEFAULT_SIZE 16
#define JavaUtilHashMap_DEFAULT_LOAD_FACTOR 0.75f
#define JavaUtilHashMap_serialVersionUID 362498820763181265

@interface JavaUtilHashMap () {
@public
  int elementCount_;
  // TODO(user): a CF version of JavaUtilHashMap_Entry should be used
  // instead (CFEntry **elementData_), so we can retain/release in ARC
  // using __bridge_retain and __bridge_transfer casts, rather than
  // use __unsafe_unretained.
  JavaUtilHashMap_Entry * __unsafe_unretained *elementData_;
  int elementDataLength_;
  int modCount_;
  float loadFactor_;
  int threshold_;
}

@property (nonatomic, assign) int elementCount;
@property (nonatomic, assign) int elementDataLength;
@property (nonatomic, assign) int modCount;
@property (nonatomic, assign) float loadFactor;
@property (nonatomic, assign) int threshold;

+ (int)calculateCapacityWithInt:(int)x;
- (void)clear;
- (id)clone;
- (void)computeThreshold;
- (BOOL)containsKeyWithId:(id)key;
- (BOOL)containsValueWithId:(id)value;
- (id<JavaUtilSet>)entrySet;
- (id)getWithId:(id)key;
- (JavaUtilHashMap_Entry *)getEntryWithId:(id)key;
- (JavaUtilHashMap_Entry *)findNonNullKeyEntryWithId:(id)key
                                             withInt:(int)index
                                             withInt:(int)keyHash;
- (JavaUtilHashMap_Entry *)findNullKeyEntry;
- (BOOL)isEmpty;
- (id<JavaUtilSet>)keySet;
- (id)putWithId:(id)key
         withId:(id)value;
- (id)putImplWithId:(id)key
             withId:(id)value;
- (JavaUtilHashMap_Entry *)createHashedEntryWithId:(id)key
                                           withInt:(int)index
                                           withInt:(int)hash_;
- (void)putAllWithJavaUtilMap:(id<JavaUtilMap>)map;
- (void)putAllImplWithJavaUtilMap:(id<JavaUtilMap>)map;
- (void)rehashWithInt:(int)capacity;
- (void)rehash;
- (id)removeWithId:(id)key;
- (void)removeEntryWithJavaUtilHashMap_Entry:(JavaUtilHashMap_Entry *)entry;
- (JavaUtilHashMap_Entry *)removeEntryWithId:(id)key;
- (int)size;
- (id<JavaUtilCollection>)values;
+ (int)computeHashCodeWithId:(id)key;
+ (BOOL)areEqualKeysWithId:(id)key1
                    withId:(id)key2;
+ (BOOL)areEqualValuesWithId:(id)value1
                      withId:(id)value2;
- (id)copyWithZone:(NSZone *)zone;
@end

@interface JavaUtilHashMap_Entry : JavaUtilMapEntry {
 @public
  int origKeyHash_;
  JavaUtilHashMap_Entry *next_;
}

- (id)initWithId:(id)theKey
         withInt:(int)hash_;
- (id)initWithId:(id)theKey
          withId:(id)theValue;
@end

@interface JavaUtilHashMap_AbstractMapIterator : NSObject {
 @public
  int position_;
  int expectedModCount_;
  JavaUtilHashMap_Entry __unsafe_unretained *futureEntry_;
  JavaUtilHashMap_Entry __unsafe_unretained *currentEntry_;
  JavaUtilHashMap_Entry __unsafe_unretained *prevEntry_;
  JavaUtilHashMap *associatedMap_;
}

@property (nonatomic, assign) int position;
@property (nonatomic, assign) int expectedModCount;
@property (nonatomic, assign) JavaUtilHashMap_Entry *futureEntry;
@property (nonatomic, assign) JavaUtilHashMap_Entry *currentEntry;
@property (nonatomic, assign) JavaUtilHashMap_Entry *prevEntry;
@property (nonatomic, strong) JavaUtilHashMap *associatedMap;

- (id)initWithJavaUtilHashMap:(JavaUtilHashMap *)hm;
- (BOOL)hasNext;
- (void)checkConcurrentMod;
- (void)makeNext;
- (void)remove;
@end

@interface JavaUtilHashMap_EntryIterator : JavaUtilHashMap_AbstractMapIterator < JavaUtilIterator > {
}

- (id)initWithJavaUtilHashMap:(JavaUtilHashMap *)map;
- (id<JavaUtilMap_Entry>)next;
@end

@interface JavaUtilHashMap_KeyIterator : JavaUtilHashMap_AbstractMapIterator < JavaUtilIterator > {
}

- (id)initWithJavaUtilHashMap:(JavaUtilHashMap *)map;
- (id)next;
@end

@interface JavaUtilHashMap_ValueIterator : JavaUtilHashMap_AbstractMapIterator < JavaUtilIterator > {
}

- (id)initWithJavaUtilHashMap:(JavaUtilHashMap *)map;
- (id)next;
@end

@interface JavaUtilHashMap_HashMapEntrySet : JavaUtilAbstractSet {
 @public
  JavaUtilHashMap *associatedMap_;
}

- (id)initWithJavaUtilHashMap:(JavaUtilHashMap *)hm;
- (JavaUtilHashMap *)hashMap;
- (int)size;
- (void)clear;
- (BOOL)removeWithId:(id)object;
- (BOOL)containsWithId:(id)object;
+ (BOOL)valuesEqWithJavaUtilHashMap_Entry:(JavaUtilHashMap_Entry *)entry
                    withJavaUtilMap_Entry:(id<JavaUtilMap_Entry>)oEntry;
- (id<JavaUtilIterator>)iterator;
@end

@interface JavaUtilHashMap_KeySet : JavaUtilAbstractSet {
 @public
  JavaUtilHashMap __weak *outer_;
}

- (BOOL)containsWithId:(id)object;
- (int)size;
- (void)clear;
- (BOOL)removeWithId:(id)key;
- (id<JavaUtilIterator>)iterator;
- (id)initWithJavaUtilHashMap:(JavaUtilHashMap *)outer;
@end

@interface JavaUtilHashMap_ValuesCollection : JavaUtilAbstractCollection {
 @public
  JavaUtilHashMap __weak *outer_;
}

- (BOOL)containsWithId:(id)object;
- (int)size;
- (void)clear;
- (id<JavaUtilIterator>)iterator;
- (id)initWithJavaUtilHashMap:(JavaUtilHashMap *)outer;
@end

#endif // _JavaUtilHashMap_PackagePrivate_H_
