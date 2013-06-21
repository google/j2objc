//
//  LinkedHashMap_PackagePrivate.h
//  JreEmulation
//
//  Created by Keith Stanger on 11/06/12.
//  Copyright 2012 Google, Inc. All rights reserved.
//

#ifndef _JavaUtilLinkedHashMap_PackagePrivate_H_
#define _JavaUtilLinkedHashMap_PackagePrivate_H_

@class JavaUtilLinkedHashMap_LinkedHashMapEntry;

#include "JreEmulation.h"
#include "java/util/AbstractCollection.h"
#include "java/util/AbstractSet.h"
#include "java/util/HashMap_PackagePrivate.h"
#include "java/util/Iterator.h"
#include "java/util/LinkedHashMap.h"
#include "java/util/Map.h"

#define JavaUtilLinkedHashMap_serialVersionUID 3801124242820219131

// Non-public classes, methods and properties for LinkedHashMap.
@interface JavaUtilLinkedHashMap () {
@public
  BOOL accessOrder_;
  // TODO(user): a CF version of LinkedHashMapEntry should be used
  // instead (CFLinkedHashMapEntry), so we can retain/release in ARC
  // using __bridge_retain and __bridge_transfer casts, rather than
  // use __unsafe_unretained.
  JavaUtilLinkedHashMap_LinkedHashMapEntry __unsafe_unretained *head_,
                                           __unsafe_unretained *tail_;
}

@property (nonatomic, assign) BOOL accessOrder;
@property (nonatomic, assign) JavaUtilLinkedHashMap_LinkedHashMapEntry *head;
@property (nonatomic, assign) JavaUtilLinkedHashMap_LinkedHashMapEntry *tail;

- (BOOL)containsValueWithId:(id)value;
- (id)getWithId:(id)key;
- (JavaUtilHashMap_Entry *)createHashedEntryWithId:(id)key
                                           withInt:(int)index
                                           withInt:(int)hash_;
- (id)putWithId:(id)key
         withId:(id)value;
- (id)putImplWithId:(id)key
             withId:(id)value;
- (void)linkEntryWithJavaUtilLinkedHashMap_LinkedHashMapEntry:(JavaUtilLinkedHashMap_LinkedHashMapEntry *)m;
- (id<JavaUtilSet>)entrySet;
- (id<JavaUtilSet>)keySet;
- (id<JavaUtilCollection>)values;
- (id)removeWithId:(id)key;
- (BOOL)removeEldestEntryWithJavaUtilMap_Entry:(id<JavaUtilMap_Entry>)eldest;
- (void)clear;
@end

@interface JavaUtilLinkedHashMap_AbstractMapIterator : NSObject {
 @public
  int expectedModCount_;
  JavaUtilLinkedHashMap_LinkedHashMapEntry __unsafe_unretained *futureEntry_;
  JavaUtilLinkedHashMap_LinkedHashMapEntry __unsafe_unretained *currentEntry_;
  JavaUtilLinkedHashMap __unsafe_unretained *associatedMap_;
}

- (id)initWithJavaUtilLinkedHashMap:(JavaUtilLinkedHashMap *)map;
- (BOOL)hasNext;
- (void)checkConcurrentMod;
- (void)makeNext;
- (void)remove;
@end

@interface JavaUtilLinkedHashMap_EntryIterator : JavaUtilLinkedHashMap_AbstractMapIterator < JavaUtilIterator > {
}

- (id)initWithJavaUtilLinkedHashMap:(JavaUtilLinkedHashMap *)map;
- (id<JavaUtilMap_Entry>)next;
@end

@interface JavaUtilLinkedHashMap_KeyIterator : JavaUtilLinkedHashMap_AbstractMapIterator < JavaUtilIterator > {
}

- (id)initWithJavaUtilLinkedHashMap:(JavaUtilLinkedHashMap *)map;
- (id)next;
@end

@interface JavaUtilLinkedHashMap_ValueIterator : JavaUtilLinkedHashMap_AbstractMapIterator < JavaUtilIterator > {
}

- (id)initWithJavaUtilLinkedHashMap:(JavaUtilLinkedHashMap *)map;
- (id)next;
@end

@interface JavaUtilLinkedHashMap_LinkedHashMapEntrySet : JavaUtilHashMap_HashMapEntrySet {
}

- (id)initWithJavaUtilLinkedHashMap:(JavaUtilLinkedHashMap *)lhm;
- (id<JavaUtilIterator>)iterator;
@end

@interface JavaUtilLinkedHashMap_LinkedHashMapEntry : JavaUtilHashMap_Entry {
 @public
  JavaUtilLinkedHashMap_LinkedHashMapEntry *chainForward_, *chainBackward_;
}

- (id)initWithId:(id)theKey
          withId:(id)theValue;
- (id)initWithId:(id)theKey
         withInt:(int)hash_;
@end

@interface JavaUtilLinkedHashMap_KeySet : JavaUtilAbstractSet {
 @public
  JavaUtilLinkedHashMap __weak *outer_;
}

- (BOOL)containsWithId:(id)object;
- (int)size;
- (void)clear;
- (BOOL)removeWithId:(id)key;
- (id<JavaUtilIterator>)iterator;
- (id)initWithJavaUtilLinkedHashMap:(JavaUtilLinkedHashMap *)outer;
@end

@interface JavaUtilLinkedHashMap_ValuesCollection : JavaUtilAbstractCollection {
 @public
  JavaUtilLinkedHashMap __weak *outer_;
}

- (BOOL)containsWithId:(id)object;
- (int)size;
- (void)clear;
- (id<JavaUtilIterator>)iterator;
- (id)initWithJavaUtilLinkedHashMap:(JavaUtilLinkedHashMap *)outer;
@end

#endif // _JavaUtilLinkedHashMap_PackagePrivate_H_
