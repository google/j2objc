// Copyright 2012 Google Inc. All Rights Reserved.
//
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

//
//  IOSList.m
//  JreEmulation
//
//  Created by Tom Ball on 1/27/12.
//

#import "IOSList.h"
#import "java/lang/IllegalStateException.h"
#import "java/lang/IndexOutOfBoundsException.h"
#import "java/util/Collection.h"
#import "java/util/NoSuchElementException.h"

@implementation IOSList

+ (IOSList *)listWithJavaUtilList:(id<JavaUtilList>)list {
  IOSList *newList = [[IOSList alloc] initWithJavaUtilCollection:list];
#if ! __has_feature(objc_arc)
  [newList autorelease];
#endif
  return newList;
}

- (id)initWithNSArray:(NSMutableArray *)list location:(NSUInteger)location {
  if (location == 0) {
    return [self initWithNSArray:list];
  }
  self = [self initWithInt:[list count] + location];
  [self addWithInt:location withId:list];
  return self;
}

- (void)addWithInt:(int)location
            withId:(id)object {
  if (!object) {
    object = [NSNull null];
  }
  [delegate_ insertObject:object atIndex:location];
}

- (BOOL)canAdd:(id)object {
  return YES;
}

#pragma mark -
#pragma mark JavaUtilList

- (BOOL)addAllWithInt:(int)location
withJavaUtilCollection:(id<JavaUtilCollection>)collection {
  return [self addAllAtLocation:location collection:collection];
}

- (id)getWithInt:(int)location {
  return [delegate_ objectAtIndex:location];
}

- (int)indexOfWithId:(id)object {
  return [delegate_ indexOfObject:object];
}

- (int)lastIndexOfWithId:(id)object {      
  NSUInteger index =
      [delegate_ indexOfObjectWithOptions:NSEnumerationReverse
                              passingTest:^(id obj, NSUInteger idx,
                                            BOOL *stop) {
                                if ([object isEqual:obj]) {
                                  *stop = YES;
                                  return YES;
                                }
                                return NO;
                              }];
  return index == NSNotFound ? -1 : index;
}

- (id<JavaUtilListIterator>)listIterator {
  id result = [[IOSListIterator alloc] initWithList:delegate_ location:0];
#if ! __has_feature(objc_arc)
  [result autorelease];
#endif
  return result;
}

- (id<JavaUtilListIterator>)listIteratorWithInt:(int)location {
  id result =
      [[IOSListIterator alloc] initWithList:delegate_ location:location];
#if ! __has_feature(objc_arc)
  [result autorelease];
#endif
  return result;
}

- (id)removeWithInt:(int)location {
  id result = [delegate_ objectAtIndex:location];
  [delegate_ removeObjectAtIndex:location];
  return result;
}

- (id)setWithInt:(int)location
          withId:(id)object {
  id result = [delegate_ objectAtIndex:location];
  [delegate_ replaceObjectAtIndex:location withObject:object];
  return result;
}

- (id<JavaUtilList>)subListWithInt:(int)start
                           withInt:(int)end {
  NSUInteger size = [delegate_ count];
  if (start >= size || end >= size) {
    id exception = [[JavaLangIndexOutOfBoundsException alloc] init];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }
  NSRange range = NSMakeRange(start, end - start);
  NSMutableArray *subArray =
      [NSMutableArray arrayWithArray:[delegate_ subarrayWithRange:range]];
  id<JavaUtilList> sublist = [[IOSList alloc] initWithNSArray:subArray];
#if ! __has_feature(objc_arc)
  [sublist autorelease];
#endif
  return sublist;
}

#pragma mark -
#pragma mark JavaUtilArrayList

- (void)ensureCapacityWithInt:(int)minimumCapacity {
  // No equivalent in NSMutableArray.
}

- (void)removeRangeWithInt:(int)start withInt:(int)end {
  [delegate_ removeObjectsInRange:NSMakeRange(start, end - start)];
}

- (void)trimToSize {
  // No equivalent in NSMutableArray.
}

#pragma mark -

@end

@implementation IOSListIterator

- (id)initWithList:(NSMutableArray *)list location:(NSUInteger)location {
  if (location > [list count]) {
    id exception = [[JavaLangIndexOutOfBoundsException alloc] init];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }
  self = [super initWithList:list];
  if (self) {
    available_ -= location;
  }
  return self;
}

#pragma mark -
#pragma JavaUtilListIterator

- (void)addWithId:(id)object {
  [list_ insertObject:object atIndex:[list_ count] - available_];
  lastPosition_ = -1;
}

- (BOOL)hasPrevious {
  return available_ < [list_ count];
}

- (int)nextIndex {
  return [list_ count] - available_;
}

- (id)previous {
  int max = [list_ count];
  int index = max - available_ - 1;
  if (index < 0 || index >= max) {
    id exception = [[JavaUtilNoSuchElementException alloc] init];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }
  available_++;
  lastPosition_ = index;
  return [list_ objectAtIndex:index];
}

- (int)previousIndex {
  return [list_ count] - available_ - 1;
}

- (void)setWithId:(id)object {
  if (lastPosition_ < 0 || lastPosition_ >= [list_ count]) {
    [list_ replaceObjectAtIndex:lastPosition_ withObject:object];
  } else {
    id exception = [[JavaLangIllegalStateException alloc] init];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }
}

@end
