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
//  IOSDoubleArray.m
//  JreEmulation
//
//  Created by Tom Ball on 6/16/11.
//

#import "IOSDoubleArray.h"
#import "IOSPrimitiveClass.h"
#import "java/lang/Double.h"

@implementation IOSDoubleArray

- (id)initWithLength:(NSUInteger)length {
  if ((self = [super initWithLength:length])) {
    buffer_ = calloc(length, sizeof(double));
  }
  return self;
}

- (id)initWithDoubles:(const double *)doubles count:(NSUInteger)count {
  if ((self = [self initWithLength:count])) {
    if (doubles != nil) {
      memcpy(buffer_, doubles, count * sizeof(double));
    }
  }
  return self;
}

+ (id)arrayWithDoubles:(const double *)doubles count:(NSUInteger)count {
  id array = [[IOSDoubleArray alloc] initWithDoubles:doubles count:count];
#if ! __has_feature(objc_arc)
  [array autorelease];
#endif
  return array;
}

- (double)doubleAtIndex:(NSUInteger)index {
  [self checkIndex:index];
  return buffer_[index];
}

- (double)replaceDoubleAtIndex:(NSUInteger)index withDouble:(double)value {
  [self checkIndex:index];
  buffer_[index] = value;
  return value;
}

- (void)getDoubles:(double *)buffer length:(NSUInteger)length {
  [self checkIndex:(length - 1)];
  memcpy(buffer, buffer_, length * sizeof(double));
}

- (void) arraycopy:(NSRange)sourceRange
       destination:(IOSArray *)destination
            offset:(NSInteger)offset {
  [self checkRange:sourceRange];
  NSRange destRange = NSMakeRange(offset, sourceRange.length);
  [destination checkRange:destRange];
  memmove(((IOSDoubleArray *) destination)->buffer_ + offset,
          self->buffer_ + sourceRange.location,
          sourceRange.length * sizeof(double));
}

- (double)incr:(NSUInteger)index {
  [self checkIndex:index];
  return ++buffer_[index];
}

- (double)decr:(NSUInteger)index {
  [self checkIndex:index];
  return --buffer_[index];
}

- (double)postIncr:(NSUInteger)index {
  [self checkIndex:index];
  return buffer_[index]++;
}

- (double)postDecr:(NSUInteger)index {
  [self checkIndex:index];
  return buffer_[index]--;
}

- (NSString *)descriptionOfElementAtIndex:(NSUInteger)index {
  return [NSString stringWithFormat:@"%g", buffer_[index]];
}

- (IOSClass *)elementType {
  id type = [[IOSPrimitiveClass alloc] initWithName:@"double" type:@"D"];
#if ! __has_feature(objc_arc)
  [type autorelease];
#endif
  return type;
}

- (id)copyWithZone:(NSZone *)zone {
  return [[IOSDoubleArray allocWithZone:zone]
          initWithDoubles:buffer_ count:size_];
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  free(buffer_);
  [super dealloc];
}
#endif

@end
