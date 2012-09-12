//
//  HashMap.m
//  JreEmulation
//
//  Created by Tom Ball on 1/27/12.
//  Copyright 2012 Google, Inc. All rights reserved.
//

#import "java/lang/IllegalArgumentException.h"
#import "java/lang/IllegalStateException.h"
#import "java/lang/NullPointerException.h"
#import "java/util/HashMap_PackagePrivate.h"
#import "java/util/Map.h"
#import "IOSList.h"
#import "IOSSet.h"
#import "JreEmulation.h"
#import <CoreFoundation/CFDictionary.h>

@implementation JavaUtilHashMap

#define JavaUtilHashMap_DEFAULT_SIZE 16

@synthesize dictionary = dictionary_;

- (id)init {
  return [self initWithInt:JavaUtilHashMap_DEFAULT_SIZE];
}

- (id)initWithInt:(int)capacity {
  if (capacity < 0) {
    id exception = [[JavaLangIllegalArgumentException alloc] init];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }
  self = [super init];
  if (self) {
    dictionary_ = [NSMutableDictionary dictionaryWithCapacity:capacity];
#if ! __has_feature(objc_arc)
    [dictionary_ retain];
#endif
  }
  return self;
}

- (id)initWithInt:(int)capacity withFloat:(float)loadFactor {
  if (loadFactor <= 0) {
    id exception = [[JavaLangIllegalArgumentException alloc] init];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }
  return [self initWithInt:capacity];
}

- (id)initWithJavaUtilMap:(id<JavaUtilMap>)map {
  if (!map) {
    id exception = [[JavaLangNullPointerException alloc] init];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }
  int size = [map size];
  self = [self initWithInt:size];
  if (self) {
    if ([map isMemberOfClass:[JavaUtilHashMap class]]) {
      JavaUtilHashMap *other = (JavaUtilHashMap *) map;
      dictionary_ =
          [[NSMutableDictionary alloc] initWithDictionary:other->dictionary_];
    } else {
      [self putAllImpl:map];
    }
  }
  return self;
}

#pragma mark -

- (void)clear {
  [dictionary_ removeAllObjects];
}

id nullify(id object) {
  return object == [NSNull null] ? nil : object;
}

id denullify(id object) {
  return object == nil ? [NSNull null] : object;
}

- (BOOL)containsKeyWithId:(id)key {
  return [dictionary_ objectForKey:denullify(key)] != nil;
}

- (BOOL)containsValueWithId:(id)value {
  NSArray *keys = [dictionary_ allKeysForObject:denullify(value)];
  return [keys count] > 0;
}

- (id<JavaUtilSet>)entrySet {
  id result = [[JavaUtilHashMap_EntrySet alloc] initWithJavaUtilHashMap:self];
#if ! __has_feature(objc_arc)
  [result autorelease];
#endif
  return result;
}

- (id)getWithId:(id)key {
  id result = [dictionary_ objectForKey:denullify(key)];
  return nullify(result);
}

- (NSUInteger)hash {
  return [dictionary_ hash];
}

- (BOOL)isEmpty {
  return [dictionary_ count] == 0;
}

- (BOOL)isEqual:(id)object {
  if ([object isKindOfClass:[JavaUtilHashMap class]]) {
    JavaUtilHashMap *other = (JavaUtilHashMap *) object;
    BOOL foo = [dictionary_ isEqualToDictionary:other->dictionary_];
    return foo;
  }
  return NO;
}

- (id<JavaUtilSet>)keySet {
  id result = [[JavaUtilHashMap_KeySet alloc] initWithJavaUtilHashMap:self];
#if ! __has_feature(objc_arc)
  [result autorelease];
#endif
  return result;
}

- (id)putWithId:(id)key
         withId:(id)value {
  key = denullify(key);
  value = denullify(value);
  id previous = [dictionary_ objectForKey:key];

#if ! __has_feature(objc_arc)
  [[previous retain] autorelease];
#endif

  // Use a CFDictionary function so the key is retained rather than copied,
  // as HashMap keys can't be required to be cloneable.
  CFDictionarySetValue((ARCBRIDGE CFMutableDictionaryRef) dictionary_,
                       (ARCBRIDGE void *) key, (ARCBRIDGE void *) value);
  return nullify(previous);
}

- (void)putAllWithJavaUtilMap:(id<JavaUtilMap>)map {
  if (![map isEmpty]) {
    [self putAllImpl:map];
  }
}

- (void)putAllImpl:(id<JavaUtilMap>)map {
  id<JavaUtilSet> entrySet = [map entrySet];
  id<JavaUtilIterator> iterator = [entrySet iterator];

  // Throw NPE if null iterator, but only if there's something to iterate.
  if (!iterator) {
    id exception = [[JavaLangNullPointerException alloc] init];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }

  while ([iterator hasNext]) {
    id entry = [iterator next];
    [self putWithId:[entry getKey] withId:[entry getValue]];
  }
}

- (id)removeWithId:(id)key {
  key = denullify(key);
  id result = [dictionary_ objectForKey:key];
  if (result) {
#if ! __has_feature(objc_arc)
    [[result retain] autorelease];
#endif
    [dictionary_ removeObjectForKey:key];
  }
  return nullify(result);
}

- (int)size {
  return [dictionary_ count];
}

- (id<JavaUtilCollection>)values {
  id result = [[JavaUtilHashMap_Values alloc] initWithJavaUtilHashMap:self];
#if ! __has_feature(objc_arc)
  [result autorelease];
#endif
  return result;
}

- (JavaUtilHashMap_Entry *)entry:(id)key {
  key = denullify(key);
  id value = [dictionary_ objectForKey:key];
  id result = [[JavaUtilHashMap_Entry alloc] initWithKey:key value:value];
#if ! __has_feature(objc_arc)
  [result autorelease];
#endif
  return result;
}

- (id)mutableCopyWithZone:(NSZone *)zone {
  JavaUtilHashMap *copy =
      [[[self class] alloc] initWithInt:[dictionary_ count]];
  copy->dictionary_ = [dictionary_ mutableCopy];
  return copy;
}

- (id)clone {
  return [self mutableCopy];
}

- (NSString *)description {
  // Handle case where a map contains itself (legal, but weird).
  static BOOL recursing = NO;
  if (recursing) {
    return @"(this Map)";
  } else {
    BOOL wasRecursing = recursing;
    @try {
      recursing = YES;
      return [dictionary_ description];
    }
    @finally {
      recursing = wasRecursing;
    }
  }
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [dictionary_ release];
  [super dealloc];
}
#endif

#pragma mark -

@end

@implementation JavaUtilHashMap_Entry

- (id)initWithKey:(id)key value:(id)value {
  return [super initWithId:key withId:value];
}

#pragma mark -
#pragma mark JavaUtilMap_Entry

- (id)getKey {
  return nullify([super key]);
}

- (id)getValue {
  return nullify([super value]);
}

@end

@implementation JavaUtilHashMap_KeySet

@synthesize map = map_;

- (id)initWithJavaUtilHashMap:(JavaUtilHashMap *)map {
  if ((self = [super init])) {
    map_ = map;
  }
  return self;
}

- (int)size {
  return [map_ size];
}

- (void)clear {
  [map_ clear];
}

- (BOOL)removeWithId:(id)key {
  if ([map_ containsKeyWithId:key]) {
    [map_ removeWithId:key];
    return YES;
  }
  return NO;
}

- (BOOL)containsWithId:(id)key {
  return [map_ containsKeyWithId:key];
}

- (id<JavaUtilIterator>)iterator {
  NSMutableArray *keyList = [[self.map.dictionary allKeys] mutableCopy];
#if ! __has_feature(objc_arc)
  [keyList autorelease];
#endif
  IOSIterator *keyIterator = [[IOSIterator alloc] initWithList:keyList];
#if ! __has_feature(objc_arc)
  [keyIterator autorelease];
#endif
  id iterator =
      [[JavaUtilHashMap_KeySetIterator alloc]
       initWithJavaUtilHashMap:self.map withIterator:keyIterator];
#if ! __has_feature(objc_arc)
  [iterator autorelease];
#endif
  return iterator;
}

@end

@implementation JavaUtilHashMap_EntrySet

- (BOOL)removeWithId:(id)object {
  if ([object conformsToProtocol:@protocol(JavaUtilMap_Entry)]) {
    id<JavaUtilMap_Entry> entry = (id<JavaUtilMap_Entry>) object;
    return [super removeWithId:[entry getKey]];
  }
  return NO;
}

- (BOOL)containsWithId:(id)object {
  if ([object conformsToProtocol:@protocol(JavaUtilMap_Entry)]) {
    id<JavaUtilMap_Entry>entry = (id<JavaUtilMap_Entry>) object;
    return [super containsWithId:[entry getKey]];
  }
  return NO;
}

- (id<JavaUtilIterator>)iterator {
  NSMutableArray *keyList = [[self.map.dictionary allKeys] mutableCopy];
#if ! __has_feature(objc_arc)
  [keyList autorelease];
#endif
  IOSIterator *keyIterator = [[IOSIterator alloc] initWithList:keyList];
#if ! __has_feature(objc_arc)
  [keyIterator autorelease];
#endif
  id iterator =
      [[JavaUtilHashMap_EntrySetIterator alloc]
       initWithJavaUtilHashMap:self.map withIterator:keyIterator];
#if ! __has_feature(objc_arc)
  [iterator autorelease];
#endif
  return iterator;
}

@end

@implementation JavaUtilHashMap_KeySetIterator

@synthesize map = map_;
@synthesize iterator = iterator_;

- (id)initWithJavaUtilHashMap:(JavaUtilHashMap *)map
                 withIterator:(IOSIterator *)iterator {
  if ((self = [super init])) {
    map_ = map;
    iterator_ = iterator;
    lastKey_ = nil;
  }
  return self;
}

- (BOOL)hasNext {
  return [iterator_ hasNext];
}

- (id)next {
  lastKey_ = [iterator_ next];
  return lastKey_;
}

- (void)remove {
  if (!lastKey_) {
    id exception = [[JavaLangIllegalStateException alloc] init];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }
  [map_ removeWithId:lastKey_];
  lastKey_ = nil;
}

- (NSString *)description {
  return [iterator_ description];
}

@end

@implementation JavaUtilHashMap_EntrySetIterator

- (id)next {
  id key = [self.iterator next];
  id value = [self.map getWithId:key];
  id result = [[JavaUtilHashMap_Entry alloc] initWithKey:key value:value];
#if ! __has_feature(objc_arc)
  [result autorelease];
#endif
  return result;
}

@end

@implementation JavaUtilHashMap_Values

@synthesize map = map_;

- (id)initWithJavaUtilHashMap:(JavaUtilHashMap *)map {
  if ((self = [super init])) {
    map_ = map;
  }
  return self;
}

- (int)size {
  return [map_ size];
}

- (void)clear {
  [map_ clear];
}

- (BOOL)containsWithId:(id)object {
  return [map_ containsValueWithId:object];
}

- (id<JavaUtilIterator>)iterator {
  NSMutableArray *valueList = [[self.map.dictionary allValues] mutableCopy];
#if ! __has_feature(objc_arc)
  [valueList autorelease];
#endif
  IOSIterator *valueIterator = [[IOSIterator alloc] initWithList:valueList];
#if ! __has_feature(objc_arc)
  [valueIterator autorelease];
#endif
  id iterator = [[JavaUtilHashMap_ValuesIterator alloc]
                 initWithJavaUtilHashMap:self.map withIterator:valueIterator];
#if ! __has_feature(objc_arc)
  [iterator autorelease];
#endif
  return iterator;
}

- (BOOL)removeWithId:(id)value {
  if ([map_ containsValueWithId:value]) {
    for (id key in [map_.dictionary allKeysForObject:value]) {
      [map_ removeWithId:key];
      return YES;
    }
  }
  return NO;
}

@end

@implementation JavaUtilHashMap_ValuesIterator

@synthesize map = map_;

- (id)initWithJavaUtilHashMap:(JavaUtilHashMap *)map
                 withIterator:(IOSIterator *)iterator {
  if ((self = [super init])) {
    map_ = map;
    iterator_ = iterator;
    lastValue_ = nil;
  }
  return self;
}

- (BOOL)hasNext {
  return [iterator_ hasNext];
}

- (id)next {
  lastValue_ = [iterator_ next];
  return lastValue_;
}

- (void)remove {
  if (!lastValue_) {
    id exception = [[JavaLangIllegalStateException alloc] init];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }
  for (id key in [map_.dictionary allKeysForObject:lastValue_]) {
    [map_ removeWithId:key];
    break;
  }
  lastValue_ = nil;
}

- (NSString *)description {
  return [iterator_ description];
}

@end
