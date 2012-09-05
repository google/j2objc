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
//  IOSByteArray.m
//  JreEmulation
//
//  Created by Tom Ball on 6/16/11.
//

#import "IOSByteArray.h"
#import "IOSPrimitiveClass.h"
#import "java/lang/Byte.h"

@implementation IOSByteArray

- (id)initWithLength:(NSUInteger)length {
  if ((self = [super initWithLength:length])) {
    buffer_ = calloc(length, sizeof(char));
  }
  return self;
}

- (id)initWithBytes:(const char *)bytes count:(NSUInteger)count {
  if ((self = [self initWithLength:count])) {
    if (bytes != nil) {
      memcpy(buffer_, bytes, count * sizeof(char));
    }
  }
  return self;
}

+ (id)arrayWithBytes:(const char *)bytes count:(NSUInteger)count {
  id array = [[IOSByteArray alloc] initWithBytes:bytes count:count];
#if __has_feature(objc_arc)
  return array;
#else
  return [array autorelease];
#endif
}

- (char)byteAtIndex:(NSUInteger)index {
  [self checkIndex:index];
  return buffer_[index];
}

- (char)replaceByteAtIndex:(NSUInteger)index withByte:(char)byte {
  [self checkIndex:index];
  buffer_[index] = byte;
  return byte;
}

- (void)getBytes:(char *)buffer
          offset:(NSUInteger)offset
          length:(NSUInteger)length {
  [self checkRange:NSMakeRange(offset, length)];
  memcpy(buffer, &buffer_[offset], length);
}

- (void)replaceBytes:(char *)source
              length:(NSUInteger)length
              offset:(NSUInteger)destOffset {
  [self checkRange:NSMakeRange(destOffset, length)];
  memcpy(&buffer_[destOffset], source, length);
}

- (void) arraycopy:(NSRange)sourceRange
       destination:(IOSArray *)destination
            offset:(NSInteger)offset {
  [self checkRange:sourceRange];
  NSRange destRange = NSMakeRange(offset, sourceRange.length);
  [destination checkRange:destRange];
  memmove(((IOSByteArray *) destination)->buffer_ + offset,
          self->buffer_ + sourceRange.location,
          sourceRange.length * sizeof(char));
}

- (char)incr:(NSUInteger)index {
  [self checkIndex:index];
  return ++buffer_[index];
}

- (char)decr:(NSUInteger)index {
  [self checkIndex:index];
  return --buffer_[index];
}

- (char)postIncr:(NSUInteger)index {
  [self checkIndex:index];
  return buffer_[index]++;
}

- (char)postDecr:(NSUInteger)index {
  [self checkIndex:index];
  return buffer_[index]--;
}

- (NSString *)descriptionOfElementAtIndex:(NSUInteger)index {
  return [NSString stringWithFormat:@"0x%x", buffer_[index]];
}

- (IOSClass *)elementType {
  id type = [[IOSPrimitiveClass alloc]
             initWithName:@"byte" type:@"B"];
#if __has_feature(objc_arc)
  return type;
#else
  return [type autorelease];
#endif
}

- (id)copyWithZone:(NSZone *)zone {
  return [[IOSByteArray allocWithZone:zone]
          initWithBytes:buffer_ count:size_];
}

- (NSData *)toNSData {
  return [NSData dataWithBytes:buffer_ length:size_];
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  free(buffer_);
  [super dealloc];
}
#endif

@end
