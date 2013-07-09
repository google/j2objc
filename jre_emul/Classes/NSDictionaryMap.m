//
//  NSDictionaryMap.h
//  JreEmulation
//
//  Created by Pankaj Kakkar on 5/20/13.
//  Copyright 2013 Google, Inc. All rights reserved.
//

#import "NSDictionaryMap.h"
#import "java/util/LinkedHashSet.h"
#import "java/util/LinkedList.h"

@interface NSDictionaryMap_Entry : NSObject<JavaUtilMap_Entry> {
@private
  NSMutableDictionary *dictionary_;
  id key_;
}

@end

@implementation NSDictionaryMap_Entry

- (id)initWithDictionary:(NSMutableDictionary *)dictionary key:(id)key {
  if ((self = [super init])) {
    dictionary_ = RETAIN(dictionary);
    key_ = RETAIN(key);
  }
  return self;
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [dictionary_ autorelease];
  [key_ autorelease];
  [super dealloc];
}
#endif

- (id)getKey {
  return key_;
}

- (id)getValue {
  return [dictionary_ objectForKey:key_];
}

- (id)setValueWithId:(id)object {
  id current = RETAIN_AND_AUTORELEASE([dictionary_ objectForKey:key_]);
  [dictionary_ setObject:object forKey:key_];
  return current;
}

@end

@implementation NSDictionaryMap

- (id)init {
  if ((self = [super init])) {
    dictionary_ = [NSMutableDictionary dictionary];
  }
  return self;
}

+ (NSDictionaryMap *)map {
  return AUTORELEASE([[[self class] alloc] init]);
}

- (id)initWithDictionary:(NSDictionary *)dictionary {
  if ((self = [super init])) {
    dictionary_ = [nil_chk(dictionary) mutableCopy];
  }
  return self;
}

+ (NSDictionaryMap *)mapWithDictionary:(NSDictionary *)dictionary {
  return AUTORELEASE([[[self class] alloc] initWithDictionary:dictionary]);
}

- (void)clear {
  [dictionary_ removeAllObjects];
}

- (BOOL)containsKeyWithId:(id)key {
  return [dictionary_ objectForKey:key] != nil;
}

- (BOOL)containsValueWithId:(id)value {
  return [[dictionary_ allValues] containsObject:value];
}

- (id<JavaUtilSet>)entrySet {
  id<JavaUtilSet> set = AUTORELEASE([[JavaUtilLinkedHashSet alloc] init]);
  for (id key in dictionary_) {
    NSDictionaryMap_Entry *entry =
        AUTORELEASE([[NSDictionaryMap_Entry alloc]
                     initWithDictionary:dictionary_ key:key]);
    [set addWithId:entry];
  }

  return set;
}

- (BOOL)isEqual:(id)object {
  if (!object) {
    return NO;
  }

  if (![object conformsToProtocol:@protocol(JavaUtilMap)]) {
    return NO;
  }

  if ([object isKindOfClass:[NSDictionaryMap class]]) {
    NSDictionaryMap *otherNSDictionaryMap = object;
    return [dictionary_ isEqualToDictionary:otherNSDictionaryMap->dictionary_];
  }

  id<JavaUtilMap> otherMap = object;
  return [[self entrySet] isEqual:[otherMap entrySet]];
}

- (id)getWithId:(id)key {
  return [dictionary_ objectForKey:nil_chk(key)];
}

- (NSUInteger)hash {
  return [dictionary_ hash];
}

- (BOOL)isEmpty {
  return [dictionary_ count] == 0;
}

- (id<JavaUtilSet>)keySet {
  id<JavaUtilSet> set = AUTORELEASE([[JavaUtilLinkedHashSet alloc] init]);
  for (id key in dictionary_) {
    [set addWithId:key];
  }

  return set;
}

- (id)putWithId:(id)key
         withId:(id)value {
  id current = RETAIN_AND_AUTORELEASE([dictionary_ objectForKey:nil_chk(key)]);
  [dictionary_ setObject:value forKey:key];
  return current;
}

- (void)putAllWithJavaUtilMap:(id<JavaUtilMap>)map {
  id<JavaUtilIterator> keyIterator = nil_chk([[nil_chk(map) keySet] iterator]);
  while ([keyIterator hasNext]) {
    id key = [keyIterator next];
    [dictionary_ setObject:[map getWithId:nil_chk(key)] forKey:key];
  }
}

- (id)removeWithId:(id)key {
  id current = RETAIN_AND_AUTORELEASE([dictionary_ objectForKey:nil_chk(key)]);
  [dictionary_ removeObjectForKey:key];
  return current;
}

- (int)size {
  return [dictionary_ count];
}

- (id<JavaUtilCollection>)values {
  id<JavaUtilList> list = AUTORELEASE([[JavaUtilLinkedList alloc] init]);
  for (id key in dictionary_) {
    [list addWithId:[dictionary_ objectForKey:key]];
  }

  return list;
}

@end
