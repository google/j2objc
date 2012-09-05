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
//  IOSShortArray.m
//  JreEmulation
//
//  Created by Tom Ball on 6/16/11.
//

#import "IOSShortArray.h"
#import "IOSPrimitiveClass.h"
#import "java/lang/Short.h"

@implementation IOSShortArray

- (id)initWithLength:(NSUInteger)length {
  if ((self = [super initWithLength:length])) {
    buffer_ = calloc(length, sizeof(short));
  }
  return self;
}

- (id)initWithShorts:(const short *)shorts count:(NSUInteger)count {
  if ((self = [self initWithLength:count])) {
    if (shorts != nil) {
      memcpy(buffer_, shorts, count * sizeof(short));
    }
  }
  return self;
}

+ (id)arrayWithShorts:(const short *)shorts count:(NSUInteger)count {
  id array = [[IOSShortArray alloc] initWithShorts:shorts count:count];
#if ! __has_feature(objc_arc)
  [array autorelease];
#endif
  return array;
}

- (short)shortAtIndex:(NSUInteger)index {
  [self checkIndex:index];
  return buffer_[index];
}

- (short)replaceShortAtIndex:(NSUInteger)index withShort:(short)value {
  [self checkIndex:index];
  buffer_[index] = value;
  return value;
}

- (void)getShorts:(short *)buffer length:(NSUInteger)length {
  [self checkIndex:(length - 1)];
  memcpy(buffer, buffer_, length * sizeof(short));
}

- (void) arraycopy:(NSRange)sourceRange
       destination:(IOSArray *)destination
            offset:(NSInteger)offset {
  [self checkRange:sourceRange];
  NSRange destRange = NSMakeRange(offset, sourceRange.length);
  [destination checkRange:destRange];
  memmove(((IOSShortArray *) destination)->buffer_ + offset,
          self->buffer_ + sourceRange.location,
          sourceRange.length * sizeof(short));
}

- (short)incr:(NSUInteger)index {
  [self checkIndex:index];
  return ++buffer_[index];
}

- (short)decr:(NSUInteger)index {
  [self checkIndex:index];
  return --buffer_[index];
}

- (short)postIncr:(NSUInteger)index {
  [self checkIndex:index];
  return buffer_[index]++;
}

- (short)postDecr:(NSUInteger)index {
  [self checkIndex:index];
  return buffer_[index]--;
}

- (NSString *)descriptionOfElementAtIndex:(NSUInteger)index {
  return [NSString stringWithFormat:@"%hi", buffer_[index]];
}

- (IOSClass *)elementType {
  id type = [[IOSPrimitiveClass alloc] initWithName:@"short" type:@"S"];
#if ! __has_feature(objc_arc)
  [type autorelease];
#endif
  return type;
}

- (id)copyWithZone:(NSZone *)zone {
  return [[IOSShortArray allocWithZone:zone] initWithShorts:buffer_ count:size_];
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  free(buffer_);
  [super dealloc];
}
#endif

@end
