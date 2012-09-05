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
//  IOSCharArray.m
//  JreEmulation
//
//  Created by Tom Ball on 6/14/11.
//

#import "IOSCharArray.h"
#import "IOSPrimitiveClass.h"
#import "java/lang/Character.h"

@implementation IOSCharArray

- (id)initWithLength:(NSUInteger)length {
  if ((self = [super initWithLength:length])) {
    buffer_ = calloc(length, sizeof(unichar));
  }
  return self;
}

- (id)initWithCharacters:(const unichar *)chars count:(NSUInteger)count {
  if ((self = [self initWithLength:count])) {
    if (chars != nil) {
      memcpy(buffer_, chars, count * sizeof(unichar));
    }
  }
  return self;
}

+ (id)arrayWithCharacters:(const unichar *)chars count:(NSUInteger)count {
  id array = [[IOSCharArray alloc] initWithCharacters:chars count:count];
#if __has_feature(objc_arc)
  return array;
#else
  return [array autorelease];
#endif
}

- (unichar)charAtIndex:(NSUInteger)index {
  [self checkIndex:index];
  return buffer_[index];
}

- (unichar)replaceCharAtIndex:(NSUInteger)index withChar:(unichar)c {
  [self checkIndex:index];
  buffer_[index] = c;
  return c;
}

- (unichar *)getChars {
  unichar *result = calloc(size_, sizeof(unichar));
  [self getChars:result length:size_];
  return result;
}

- (void)getChars:(unichar *)buffer length:(NSUInteger)length {
  [self checkIndex:(length - 1)];
  memcpy(buffer, buffer_, length * sizeof(unichar));
}

- (void) arraycopy:(NSRange)sourceRange
       destination:(IOSArray *)destination
            offset:(NSInteger)offset {
  [self checkRange:sourceRange];
  NSRange destRange = NSMakeRange(offset, sourceRange.length);
  [destination checkRange:destRange];
  memmove(((IOSCharArray *) destination)->buffer_ + offset,
          self->buffer_ + sourceRange.location,
          sourceRange.length * sizeof(unichar));
}

- (unichar)incr:(NSUInteger)index {
  [self checkIndex:index];
  return ++buffer_[index];
}

- (unichar)decr:(NSUInteger)index {
  [self checkIndex:index];
  return --buffer_[index];
}

- (unichar)postIncr:(NSUInteger)index {
  [self checkIndex:index];
  return buffer_[index]++;
}

- (unichar)postDecr:(NSUInteger)index {
  [self checkIndex:index];
  return buffer_[index]--;
}

- (NSString *)descriptionOfElementAtIndex:(NSUInteger)index {
  return [NSString stringWithFormat:@"%C", buffer_[index]];
}

- (IOSClass *)elementType {
  id type = [[IOSPrimitiveClass alloc]
             initWithName:@"char" type:@"C"];
#if __has_feature(objc_arc)
  return type;
#else
  return [type autorelease];
#endif
}

- (id)copyWithZone:(NSZone *)zone {
  return [[IOSCharArray allocWithZone:zone]
          initWithCharacters:buffer_ count:size_];
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  free(buffer_);
  [super dealloc];
}
#endif

@end
