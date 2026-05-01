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

#import "JreCollectionAdapters.h"

#include "java/util/Iterator.h"
#include "java/util/List.h"
#include "java/util/Map.h"
#include "java/util/Set.h"

// Helper subclass of NSArray that avoids copying a Java immutable list.
// Can only be safely used with known-immutable Java lists.
@interface JREImmutableJavaListArray : NSArray

// Initialize with a Java list.
//
// Note that this is not a designated init to avoid warnings and the need to override
// other inits. Other inits fall through to base class and thus get other return types
// from the class cluster.
- (instancetype)initWithList:(id<JavaUtilList>)list;

@end

@implementation JREImmutableJavaListArray {
  NSUInteger _size;
  id<JavaUtilList> _list;
}

- (instancetype)initWithList:(id<JavaUtilList>)list {
  self = [super init];
  if (self) {
    _list = list;
    _size = [list size];
  }
  return self;
}

#pragma mark NSCopying

- (id)copyWithZone:(NSZone *)zone {
  return self;  // Immutable
}

#pragma mark NSCoding

- (instancetype)initWithCoder:(NSCoder *)coder {
  // We encode to standard NSArray, so just let it handle decode.
  return [super initWithCoder:coder];
}

- (void)encodeWithCoder:(NSCoder *)coder {
  // We don't want to try to serialize the underlying Java list, so convert to a NSCoding type.
  NSMutableArray *array = [NSMutableArray arrayWithCapacity:_size];
  for (int index = 0; index < _size; index++) {
    [array addObject:[_list getWithInt:index]];
  }
  // Encode the mutable array directly. In theory we should convert to NSArray, but in
  // practice doesn't matter what type initWithCoder: returns.
  [array encodeWithCoder:coder];
}

#pragma mark NSObject

- (BOOL)isEqual:(id)object {
  // Java list classes may implement an isEqual but that wouldn't handle
  // cross-type comparisons, so we want to allow NSArray to handle all comparisons based
  // on its primitive implementation.
  return [super isEqual:object];
}

- (NSUInteger)hash {
  // Allow super to compute hashes for reasons similar to equality, see above.
  return [super hash];
}

#pragma mark NSArray

- (BOOL)containsObject:(id)anObject {
  return [_list containsWithId:anObject] ? YES : NO;
}

- (NSUInteger)count {
  return _size;
}

- (id)objectAtIndex:(NSUInteger)index {
  // _size previously constrained to jint/int32_t range implicitly in initializer.
  if (index >= _size) {
    [NSException raise:NSRangeException
                format:@"%@ index %lu beyond bounds.", [self class], (unsigned long)index];
    return nil;  // Won't get here.
  }
  return [_list getWithInt:(jint)index];
}

- (NSEnumerator<id> *)objectEnumerator {
  // Could wrap the Java iterator here, but no obvious advantage to doing so yet.
  return [super objectEnumerator];
}

- (NSUInteger)indexOfObject:(id)anObject {
  jint index = [_list indexOfWithId:anObject];
  if (index == -1) {
    return NSNotFound;
  }
  return index;
}

#pragma mark NSFastEnumeration

- (NSUInteger)countByEnumeratingWithState:(NSFastEnumerationState *)state
                                  objects:(id __unsafe_unretained _Nullable[_Nonnull])buffer
                                    count:(NSUInteger)len {
  // No obvious speedup in the Java list interface, let NSArray do its best.
  return [super countByEnumeratingWithState:state objects:buffer count:len];
}

@end

// Helper to wrap Java Iterator as NSEnumerator.
@interface JREJavaIteratorEnumerator : NSEnumerator
- (instancetype)initWithIterator:(id<JavaUtilIterator>)iterator;
@end

@implementation JREJavaIteratorEnumerator {
  id<JavaUtilIterator> _iterator;
}

- (instancetype)initWithIterator:(id<JavaUtilIterator>)iterator {
  self = [super init];
  if (self) {
    _iterator = iterator;
  }
  return self;
}

- (id)nextObject {
  if ([_iterator hasNext]) {
    return [_iterator next];
  }
  return nil;
}

@end

// Helper subclass of NSSet that avoids copying a Java immutable set.
@interface JREImmutableJavaSet : NSSet
- (instancetype)initWithSet:(id<JavaUtilSet>)set;
@end

@implementation JREImmutableJavaSet {
  NSUInteger _size;
  id<JavaUtilSet> _set;
}

- (instancetype)initWithSet:(id<JavaUtilSet>)set {
  self = [super init];
  if (self) {
    _set = set;
    _size = [set size];
  }
  return self;
}

- (NSUInteger)count {
  return _size;
}

- (id)member:(id)object {
  return [_set containsWithId:object] ? object : nil;
}

- (NSEnumerator *)objectEnumerator {
  return [[[JREJavaIteratorEnumerator alloc] initWithIterator:[_set iterator]] autorelease];
}

- (id)copyWithZone:(NSZone *)zone {
  return self;  // Immutable
}

#pragma mark NSCoding

- (instancetype)initWithCoder:(NSCoder *)coder {
  return [super initWithCoder:coder];
}

- (void)encodeWithCoder:(NSCoder *)coder {
  NSMutableSet *set = [NSMutableSet setWithCapacity:_size];
  for (id object in _set) {
    [set addObject:object];
  }
  [set encodeWithCoder:coder];
}

@end

// Helper subclass of NSDictionary that avoids copying a Java immutable map.
@interface JREImmutableJavaMap : NSDictionary
- (instancetype)initWithMap:(id<JavaUtilMap>)map;
@end

@implementation JREImmutableJavaMap {
  NSUInteger _size;
  id<JavaUtilMap> _map;
}

- (instancetype)initWithMap:(id<JavaUtilMap>)map {
  self = [super init];
  if (self) {
    _map = map;
    _size = [map size];
  }
  return self;
}

- (NSUInteger)count {
  return _size;
}

- (id)objectForKey:(id)aKey {
  return [_map getWithId:aKey];
}

- (NSEnumerator *)keyEnumerator {
  return
      [[[JREJavaIteratorEnumerator alloc] initWithIterator:[[_map keySet] iterator]] autorelease];
}

- (id)copyWithZone:(NSZone *)zone {
  return self;  // Immutable
}

#pragma mark NSCoding

- (instancetype)initWithCoder:(NSCoder *)coder {
  return [super initWithCoder:coder];
}

- (void)encodeWithCoder:(NSCoder *)coder {
  NSMutableDictionary *dict = [NSMutableDictionary dictionaryWithCapacity:_size];
  id<JavaUtilSet> entrySet = [_map entrySet];
  id<JavaUtilIterator> iterator = [entrySet iterator];
  while ([iterator hasNext]) {
    id<JavaUtilMap_Entry> entry = [iterator next];
    [dict setObject:[entry getValue] forKey:[entry getKey]];
  }
  [dict encodeWithCoder:coder];
}

@end

static NSSet *ImmutableCollectionClasses(void) {
  static dispatch_once_t immutableClassesOnce = 0;
  static NSSet *immutableClasses = nil;
  dispatch_once(&immutableClassesOnce, ^{
    NSMutableSet *classes = [NSMutableSet set];

    Class javaImmutableCollections =
        NSClassFromString(@"JavaUtilImmutableCollections_AbstractImmutableCollection");
    if (javaImmutableCollections) {
      [classes addObject:javaImmutableCollections];
    }

    Class javaUnmodifiableCollections =
        NSClassFromString(@"JavaUtilCollections_UnmodifiableCollection");
    if (javaUnmodifiableCollections) {
      [classes addObject:javaUnmodifiableCollections];
    }

    Class comGoogleCommonCollectImmutableCollection =
        NSClassFromString(@"ComGoogleCommonCollectImmutableCollection");
    if (comGoogleCommonCollectImmutableCollection) {
      [classes addObject:comGoogleCommonCollectImmutableCollection];
    }

    immutableClasses = [classes retain];
  });
  return immutableClasses;
}

static NSSet *ImmutableMapClasses(void) {
  static dispatch_once_t immutableMapClassesOnce = 0;
  static NSSet *immutableMapClasses = nil;
  dispatch_once(&immutableMapClassesOnce, ^{
    NSMutableSet *classes = [NSMutableSet set];

    Class javaImmutableMap =
        NSClassFromString(@"JavaUtilImmutableCollections_AbstractImmutableMap");
    if (javaImmutableMap) {
      [classes addObject:javaImmutableMap];
    }

    Class javaUnmodifiableMap = NSClassFromString(@"JavaUtilCollections_UnmodifiableMap");
    if (javaUnmodifiableMap) {
      [classes addObject:javaUnmodifiableMap];
    }

    Class comGoogleCommonCollectImmutableMap =
        NSClassFromString(@"ComGoogleCommonCollectImmutableMap");
    if (comGoogleCommonCollectImmutableMap) {
      [classes addObject:comGoogleCommonCollectImmutableMap];
    }

    immutableMapClasses = [classes retain];
  });
  return immutableMapClasses;
}

NSArray *JREAdaptedArrayFromJavaList(id<JavaUtilList> list) {
  if (!list) return nil;

  // Empty lists require no conversion.
  if (list.isEmpty) {
    return [NSArray array];
  }

  for (Class immutableClass in ImmutableCollectionClasses()) {
    if ([list isKindOfClass:immutableClass]) {
      return [[[JREImmutableJavaListArray alloc] initWithList:list] autorelease];
    }
  }

  // Could convert using toArray, but in practice that's two cycles through the list,
  // once through object array and then to NSArray.
  NSMutableArray<id> *array = [[NSMutableArray alloc] initWithCapacity:[list size]];
  for (id object in list) {
    [array addObject:object];
  }
  return [array autorelease];  // Ownership transfer, no need to copy to immutable NSArray.
}

NSSet *JREAdaptedSetFromJavaSet(id<JavaUtilSet> set) {
  if (!set) return nil;

  if (set.isEmpty) {
    return [NSSet set];
  }

  for (Class immutableClass in ImmutableCollectionClasses()) {
    if ([set isKindOfClass:immutableClass]) {
      return [[[JREImmutableJavaSet alloc] initWithSet:set] autorelease];
    }
  }

  NSMutableSet *mutableSet = [[NSMutableSet alloc] initWithCapacity:[set size]];
  for (id object in set) {
    [mutableSet addObject:object];
  }
  return [mutableSet autorelease];
}

NSDictionary *JREAdaptedDictionaryFromJavaMap(id<JavaUtilMap> map) {
  if (!map) return nil;

  if (map.isEmpty) {
    return [NSDictionary dictionary];
  }

  for (Class immutableClass in ImmutableMapClasses()) {
    if ([map isKindOfClass:immutableClass]) {
      return [[[JREImmutableJavaMap alloc] initWithMap:map] autorelease];
    }
  }

  NSMutableDictionary *mutableDict = [[NSMutableDictionary alloc] initWithCapacity:[map size]];
  id<JavaUtilSet> entrySet = [map entrySet];
  id<JavaUtilIterator> iterator = [entrySet iterator];
  while ([iterator hasNext]) {
    id<JavaUtilMap_Entry> entry = [iterator next];
    [mutableDict setObject:[entry getValue] forKey:[entry getKey]];
  }
  return [mutableDict autorelease];
}
