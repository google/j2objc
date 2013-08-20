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
#import "IOSArrayClass.h"
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
  IOSArray_checkIndex(size_, index);
  return buffer_[index];
}

- (unichar *)charRefAtIndex:(NSUInteger)index {
  IOSArray_checkIndex(size_, index);
  return &buffer_[index];
}

- (unichar)replaceCharAtIndex:(NSUInteger)index withChar:(unichar)c {
  IOSArray_checkIndex(size_, index);
  buffer_[index] = c;
  return c;
}

- (unichar *)getChars {
  unichar *result = calloc(size_, sizeof(unichar));
  [self getChars:result length:size_];
  return result;
}

- (void)getChars:(unichar *)buffer length:(NSUInteger)length {
  IOSArray_checkIndex(size_, length - 1);
  memcpy(buffer, buffer_, length * sizeof(unichar));
}

- (void) arraycopy:(NSRange)sourceRange
       destination:(IOSArray *)destination
            offset:(NSInteger)offset {
  IOSArray_checkRange(size_, sourceRange);
  IOSArray_checkRange(destination->size_, NSMakeRange(offset, sourceRange.length));
  memmove(((IOSCharArray *) destination)->buffer_ + offset,
          self->buffer_ + sourceRange.location,
          sourceRange.length * sizeof(unichar));
}

- (unichar)incr:(NSUInteger)index {
  IOSArray_checkIndex(size_, index);
  return ++buffer_[index];
}

- (unichar)decr:(NSUInteger)index {
  IOSArray_checkIndex(size_, index);
  return --buffer_[index];
}

- (unichar)postIncr:(NSUInteger)index {
  IOSArray_checkIndex(size_, index);
  return buffer_[index]++;
}

- (unichar)postDecr:(NSUInteger)index {
  IOSArray_checkIndex(size_, index);
  return buffer_[index]--;
}

- (NSString *)descriptionOfElementAtIndex:(NSUInteger)index {
  return [NSString stringWithFormat:@"%C", buffer_[index]];
}

- (IOSClass *)elementType {
  return [IOSClass charClass];
}

+ (IOSClass *)iosClass {
  return [IOSClass arrayClassWithComponentType:[IOSClass charClass]];
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
