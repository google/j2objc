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
//  IOSLongArray.m
//  JreEmulation
//
//  Created by Tom Ball on 6/16/11.
//

#import "IOSLongArray.h"
#import "IOSArrayClass.h"
#import "IOSPrimitiveClass.h"
#import "java/lang/Long.h"

@implementation IOSLongArray

- (id)initWithLength:(NSUInteger)length {
  if ((self = [super initWithLength:length])) {
    buffer_ = calloc(length, sizeof(long long));
  }
  return self;
}

- (id)initWithLongs:(const long long *)longs count:(NSUInteger)count {
  if ((self = [self initWithLength:count])) {
    if (longs != nil) {
      memcpy(buffer_, longs, count * sizeof(long long));
    }
  }
  return self;
}

+ (id)arrayWithLongs:(const long long*)longs count:(NSUInteger)count {
  id array = [[IOSLongArray alloc] initWithLongs:longs count:count];
#if ! __has_feature(objc_arc)
  [array autorelease];
#endif
  return array;
}

- (long long)longAtIndex:(NSUInteger)index {
  IOSArray_checkIndex(size_, index);
  return buffer_[index];
}

- (long long *)longRefAtIndex:(NSUInteger)index {
  IOSArray_checkIndex(size_, index);
  return &buffer_[index];
}

- (long long)replaceLongAtIndex:(NSUInteger)index withLong:(long long)value {
  IOSArray_checkIndex(size_, index);
  buffer_[index] = value;
  return value;
}

- (void)getLongs:(long long *)buffer length:(NSUInteger)length {
  IOSArray_checkIndex(size_, length - 1);
  memcpy(buffer, buffer_, length * sizeof(long long));
}

- (void) arraycopy:(NSRange)sourceRange
       destination:(IOSArray *)destination
            offset:(NSInteger)offset {
  IOSArray_checkRange(size_, sourceRange);
  IOSArray_checkRange(destination->size_, NSMakeRange(offset, sourceRange.length));
  memmove(((IOSLongArray *) destination)->buffer_ + offset,
          self->buffer_ + sourceRange.location,
          sourceRange.length * sizeof(long long));
}

- (long long)incr:(NSUInteger)index {
  IOSArray_checkIndex(size_, index);
  return ++buffer_[index];
}

- (long long)decr:(NSUInteger)index {
  IOSArray_checkIndex(size_, index);
  return --buffer_[index];
}

- (long long)postIncr:(NSUInteger)index {
  IOSArray_checkIndex(size_, index);
  return buffer_[index]++;
}

- (long long)postDecr:(NSUInteger)index {
  IOSArray_checkIndex(size_, index);
  return buffer_[index]--;
}

- (NSString *)descriptionOfElementAtIndex:(NSUInteger)index {
  return [NSString stringWithFormat:@"%lld", buffer_[index]];
}

- (IOSClass *)elementType {
  return [IOSClass longClass];
}

+ (IOSClass *)iosClass {
  return [IOSClass arrayClassWithComponentType:[IOSClass longClass]];
}

- (id)copyWithZone:(NSZone *)zone {
  return [[IOSLongArray allocWithZone:zone] initWithLongs:buffer_ count:size_];
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  free(buffer_);
  [super dealloc];
}
#endif

@end
