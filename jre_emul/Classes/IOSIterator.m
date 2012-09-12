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
//  IOSIterator.m
//  JreEmulation
//
//  Created by Tom Ball on 2/2/12.
//

#import "IOSIterator.h"
#import "java/lang/IllegalStateException.h"
#import "java/lang/IndexOutOfBoundsException.h"
#import "java/util/NoSuchElementException.h"

@implementation IOSIterator

- (id)initWithList:(NSMutableArray *)list {
  self = [super init];
  if (self) {
#if ! __has_feature(objc_arc)
    list_ = [list retain];
#endif
    list_ = list;
    available_ = [list_ count];
    lastPosition_ = -1;
  }
  return self;
}

- (BOOL)hasNext {
  return available_ > 0;
}

- (id)next {
  int i = [list_ count] - available_;
  if (i >= 0) {
    id result = [list_ objectAtIndex:i];
    lastPosition_ = i;
    available_--;
    return result == [NSNull null] ? nil : result;
  }
  id exception = [[JavaUtilNoSuchElementException alloc] init];
#if ! __has_feature(objc_arc)
  [exception autorelease];
#endif
  @throw exception;
  return nil;
}

- (void)remove {
  id exception = nil;
  if (lastPosition_ < 0) {
    exception = [[JavaLangIllegalStateException alloc] init];
#if ! __has_feature(objc_arc)
    [exception autorelease]; // Xcode analyzer needs autorelease next to alloc.
#endif
  }
  if ([list_ count] == 0 || lastPosition_ >= [list_ count]) {
    exception = [[JavaLangIndexOutOfBoundsException alloc] init];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
  }
  if (exception) {
    @throw exception;
  }
  [list_ removeObjectAtIndex:lastPosition_];
  lastPosition_ = -1;
}

- (NSString *)description {
  return
      [NSString stringWithFormat:@"[size=%ld, available=%d, lastPosition=%d]",
       (long)[list_ count], (int)available_, (int)lastPosition_];
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [list_ autorelease];
  [super dealloc];
}
#endif

@end
