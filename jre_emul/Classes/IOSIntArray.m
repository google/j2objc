// Copyright 2011 Google Inc. All Rights Reserved.
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
//  IOSIntArray.m
//  JreEmulation
//
//  Created by Tom Ball on 6/16/11.
//

#import "IOSIntArray.h"
#import "IOSPrimitiveClass.h"
#import "java/lang/Integer.h"

@implementation IOSIntArray

- (id)initWithLength:(NSUInteger)length {
  if ((self = [super initWithLength:length])) {
    buffer_ = calloc(length, sizeof(int));
  }
  return self;
}

- (id)initWithInts:(const int *)ints count:(NSUInteger)count {
  if ((self = [self initWithLength:count])) {
    if (ints != nil) {
      memcpy(buffer_, ints, count * sizeof(int));
    }
  }
  return self;
}

+ (id)arrayWithInts:(const int *)ints count:(NSUInteger)count {
  id array = [[IOSIntArray alloc] initWithInts:ints count:count];
#if ! __has_feature(objc_arc)
  [array autorelease];
#endif
  return array;
}

- (int)intAtIndex:(NSUInteger)index {
  [self checkIndex:index];
  return buffer_[index];
}

- (int)replaceIntAtIndex:(NSUInteger)index withInt:(int)i {
  [self checkIndex:index];
  buffer_[index] = i;
  return i;
}

- (void)getInts:(int *)buffer length:(NSUInteger)length {
  [self checkIndex:(length - 1)];
  memcpy(buffer, buffer_, length * sizeof(int));
}

- (void) arraycopy:(NSRange)sourceRange
       destination:(IOSArray *)destination
            offset:(NSInteger)offset {
  [self checkRange:sourceRange];
  NSRange destRange = NSMakeRange(offset, sourceRange.length);
  [destination checkRange:destRange];
  memmove(((IOSIntArray *) destination)->buffer_ + offset,
          self->buffer_ + sourceRange.location,
          sourceRange.length * sizeof(int));
}

- (int)incr:(NSUInteger)index {
  [self checkIndex:index];
  return ++buffer_[index];
}

- (int)decr:(NSUInteger)index {
  [self checkIndex:index];
  return --buffer_[index];
}

- (int)postIncr:(NSUInteger)index {
  [self checkIndex:index];
  return buffer_[index]++;
}

- (int)postDecr:(NSUInteger)index {
  [self checkIndex:index];
  return buffer_[index]--;
}

- (NSString *)descriptionOfElementAtIndex:(NSUInteger)index {
  return [NSString stringWithFormat:@"%d", buffer_[index]];
}

- (IOSClass *)elementType {
  id type = [[IOSPrimitiveClass alloc] initWithName:@"int" type:@"I"];
#if ! __has_feature(objc_arc)
  [type autorelease];
#endif
  return type;
}

- (id)copyWithZone:(NSZone *)zone {
  return [[IOSIntArray allocWithZone:zone] initWithInts:buffer_ count:size_];
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  free(buffer_);
  [super dealloc];
}
#endif

@end
