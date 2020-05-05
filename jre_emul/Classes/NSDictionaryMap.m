//
//  NSDictionaryMap.h
//  JreEmulation
//
//  Created by Pankaj Kakkar on 5/20/13.
//  Copyright 2013 Google, Inc. All rights reserved.
//

#import "NSDictionaryMap.h"
#import "java/util/Iterator.h"
#import "java/util/LinkedHashSet.h"
#import "java/util/LinkedList.h"
#import "java/util/function/BiConsumer.h"

@interface NSDictionaryMap_Entry : NSObject<JavaUtilMap_Entry> {
@private
  NSMutableDictionary *dictionary_;
  id key_;
}

@end

@implementation NSDictionaryMap_Entry

- (instancetype)initWithDictionary:(NSMutableDictionary *)dictionary key:(id)key {
  if ((self = [super init])) {
    dictionary_ = RETAIN_(dictionary);
    key_ = RETAIN_(key);
  }
  return self;
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [dictionary_ release];
  [key_ release];
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

- (instancetype)init {
  if ((self = [super init])) {
    dictionary_ = [NSMutableDictionary dictionary];
  }
  return self;
}

+ (instancetype)map {
  return AUTORELEASE([[[self class] alloc] init]);
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [dictionary_ release];
  [super dealloc];
}
#endif

- (instancetype)initWithDictionary:(NSDictionary *)dictionary {
  if ((self = [super init])) {
    dictionary_ = dictionary ?
        [dictionary mutableCopy] :
        [[NSMutableDictionary alloc] init];
  }
  return self;
}

+ (instancetype)mapWithDictionary:(NSDictionary *)dictionary {
  return AUTORELEASE([[[self class] alloc] initWithDictionary:dictionary]);
}

- (void)clear {
  [dictionary_ removeAllObjects];
}

- (jboolean)containsKeyWithId:(id)key {
  return [dictionary_ objectForKey:key] != nil;
}

- (jboolean)containsValueWithId:(id)value {
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

- (jboolean)isEqual:(id)object {
  if (!object) {
    return false;
  }

  if (![object conformsToProtocol:@protocol(JavaUtilMap)]) {
    return false;
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

- (jboolean)isEmpty {
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
  return (int) [dictionary_ count];
}

- (id<JavaUtilCollection>)values {
  id<JavaUtilList> list = AUTORELEASE([[JavaUtilLinkedList alloc] init]);
  for (id key in dictionary_) {
    [list addWithId:[dictionary_ objectForKey:key]];
  }

  return list;
}

- (void)forEachWithJavaUtilFunctionBiConsumer:(id<JavaUtilFunctionBiConsumer>)action {
  (void)nil_chk(action);
  if (dictionary_.count > 0) {
    NSEnumerator *enumerator = [dictionary_ keyEnumerator];
    id key;
    while ((key = [enumerator nextObject])) {
      [action acceptWithId:key withId:[dictionary_ objectForKey:key]];
    }
  }
}


@end
