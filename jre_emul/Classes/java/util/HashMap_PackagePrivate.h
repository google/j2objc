//
//  HashMap_PackagePrivate.h
//  JreEmulation
//
//  Created by Tom Ball on 2/23/12.
//  Copyright 2012 Google, Inc. All rights reserved.
//

#import "java/util/HashMap.h"
#import "java/util/AbstractCollection.h"
#import "java/util/AbstractSet.h"
#import "java/util/Collection.h"
#import "java/util/Iterator.h"
#import "java/util/MapEntry.h"
#import "java/util/Set.h"
#import "IOSIterator.h"

// Non-public classes, methods and properties shared by HashMap and
// LinkedHashMap.

@interface JavaUtilHashMap ()

@property (readonly) NSMutableDictionary *dictionary;

- (JavaUtilHashMap_Entry *)entry:(id)key;
- (void)putAllImpl:(id<JavaUtilMap>)map;

@end

@interface JavaUtilHashMap_Entry : JavaUtilMapEntry

- (id)initWithKey:(id)key value:(id)value;

@end

@interface JavaUtilHashMap_KeySet : JavaUtilAbstractSet {
@private
  JavaUtilHashMap *map_;
}

@property (readonly) JavaUtilHashMap *map;

- (id)initWithJavaUtilHashMap:(JavaUtilHashMap *)map;

@end

@interface JavaUtilHashMap_EntrySet : JavaUtilHashMap_KeySet
@end

@interface JavaUtilHashMap_KeySetIterator : NSObject < JavaUtilIterator > {
@private
  JavaUtilHashMap *map_;
  IOSIterator *iterator_;
  id lastKey_;
}

@property (readonly) JavaUtilHashMap *map;
@property (readonly) IOSIterator *iterator;

- (id)initWithJavaUtilHashMap:(JavaUtilHashMap *)map
                 withIterator:(IOSIterator *)iterator;

@end

@interface JavaUtilHashMap_EntrySetIterator : JavaUtilHashMap_KeySetIterator
@end

@interface JavaUtilHashMap_Values : JavaUtilAbstractCollection {
@private
  JavaUtilHashMap *map_;
}

@property (readonly) JavaUtilHashMap *map;

- (id)initWithJavaUtilHashMap:(JavaUtilHashMap *)map;

@end

@interface JavaUtilHashMap_ValuesIterator : NSObject < JavaUtilIterator > {
@private
  JavaUtilHashMap *map_;
  IOSIterator *iterator_;
  id lastValue_;
}

@property (readonly) JavaUtilHashMap *map;

- (id)initWithJavaUtilHashMap:(JavaUtilHashMap *)map
                 withIterator:(IOSIterator *)iterator;

@end

// Functions that convert between NSNull and nil references.
extern id nullify(id object);
extern id denullify(id object);
