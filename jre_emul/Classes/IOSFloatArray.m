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
//  IOSFloatArray.m
//  JreEmulation
//
//  Created by Tom Ball on 6/16/11.
//

#import "IOSFloatArray.h"
#import "IOSPrimitiveClass.h"
#import "java/lang/Float.h"

@implementation IOSFloatArray

- (id)initWithLength:(NSUInteger)length {
  if ((self = [super initWithLength:length])) {
    buffer_ = calloc(length, sizeof(float));
  }
  return self;
}

- (id)initWithFloats:(const float *)floats count:(NSUInteger)count {
  if ((self = [self initWithLength:count])) {
    if (floats != nil) {
      memcpy(buffer_, floats, count * sizeof(float));
    }
  }
  return self;
}

+ (id)arrayWithFloats:(const float *)floats count:(NSUInteger)count {
  id array = [[IOSFloatArray alloc] initWithFloats:floats count:count];
#if ! __has_feature(objc_arc)
  [array autorelease];
#endif
  return array;
}

- (float)floatAtIndex:(NSUInteger)index {
  [self checkIndex:index];
  return buffer_[index];
}

- (float)replaceFloatAtIndex:(NSUInteger)index withFloat:(float)value {
  [self checkIndex:index];
  buffer_[index] = value;
  return value;
}

- (void)getFloats:(float *)buffer length:(NSUInteger)length {
  [self checkIndex:(length - 1)];
  memcpy(buffer, buffer_, length * sizeof(float));
}

- (void) arraycopy:(NSRange)sourceRange
       destination:(IOSArray *)destination
            offset:(NSInteger)offset {
  [self checkRange:sourceRange];
  NSRange destRange = NSMakeRange(offset, sourceRange.length);
  [destination checkRange:destRange];
  memmove(((IOSFloatArray *) destination)->buffer_ + offset,
          self->buffer_ + sourceRange.location,
          sourceRange.length * sizeof(float));
}

- (float)incr:(NSUInteger)index {
  [self checkIndex:index];
  return ++buffer_[index];
}

- (float)decr:(NSUInteger)index {
  [self checkIndex:index];
  return --buffer_[index];
}

- (float)postIncr:(NSUInteger)index {
  [self checkIndex:index];
  return buffer_[index]++;
}

- (float)postDecr:(NSUInteger)index {
  [self checkIndex:index];
  return buffer_[index]--;
}

- (NSString *)descriptionOfElementAtIndex:(NSUInteger)index {
  return [NSString stringWithFormat:@"%g", buffer_[index]];
}

- (IOSClass *)elementType {
  id type = [[IOSPrimitiveClass alloc] initWithName:@"float" type:@"F"];
#if ! __has_feature(objc_arc)
  [type autorelease];
#endif
  return type;
}

- (id)copyWithZone:(NSZone *)zone {
  return [[IOSFloatArray allocWithZone:zone]
          initWithFloats:buffer_ count:size_];
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  free(buffer_);
  [super dealloc];
}
#endif

@end
