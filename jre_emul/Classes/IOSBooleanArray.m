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
//  IOSBooleanArray.m
//  JreEmulation
//
//  Created by Tom Ball on 6/16/11.
//

#import "IOSBooleanArray.h"
#import "IOSArrayClass.h"
#import "IOSPrimitiveClass.h"
#import "java/lang/Boolean.h"

@implementation IOSBooleanArray

- (id)initWithLength:(NSUInteger)length {
  if ((self = [super initWithLength:length])) {
    buffer_ = calloc(length, sizeof(BOOL));
  }
  return self;
}

- (id)initWithBooleans:(const BOOL *)booleans count:(NSUInteger)count {
  if ((self = [self initWithLength:count])) {
    if (booleans != nil) {
      memcpy(buffer_, booleans, count * sizeof(BOOL));
    }
  }
  return self;
}

+ (id)arrayWithBooleans:(const BOOL *)booleans count:(NSUInteger)count {
  id array = [[IOSBooleanArray alloc] initWithBooleans:booleans count:count];
#if __has_feature(objc_arc)
  return array;
#else
  return [array autorelease];
#endif
}

- (BOOL)booleanAtIndex:(NSUInteger)index {
  [self checkIndex:index];
  return buffer_[index];
}

- (BOOL)replaceBooleanAtIndex:(NSUInteger)index withBoolean:(BOOL)boolean {
  [self checkIndex:index];
  buffer_[index] = boolean;
  return boolean;
}

- (void)getBooleans:(BOOL *)buffer length:(NSUInteger)length {
  [self checkIndex:(length - 1)];
  memcpy(buffer, buffer_, length * sizeof(BOOL));
}

- (void) arraycopy:(NSRange)sourceRange
       destination:(IOSArray *)destination
            offset:(NSInteger)offset {
  [self checkRange:sourceRange];
  NSRange destRange = NSMakeRange(offset, sourceRange.length);
  [destination checkRange:destRange];
  memmove(((IOSBooleanArray *) destination)->buffer_ + offset,
          self->buffer_ + sourceRange.location,
          sourceRange.length * sizeof(BOOL));
}

- (NSString *)descriptionOfElementAtIndex:(NSUInteger)index {
  return [NSString stringWithFormat:@"%@", (buffer_[index] ? @"YES" : @"NO")];
}

- (IOSClass *)elementType {
  id type = [[IOSPrimitiveClass alloc]
             initWithName:@"boolean" type:@"Z"];
#if __has_feature(objc_arc)
  return type;
#else
  return [type autorelease];
#endif
}

- (id)copyWithZone:(NSZone *)zone {
  return [[IOSBooleanArray allocWithZone:zone]
          initWithBooleans:buffer_ count:size_];
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  free(buffer_);
  [super dealloc];
}
#endif

@end
