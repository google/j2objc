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
#import "IOSClass.h"

@implementation IOSByteArray

PRIMITIVE_ARRAY_IMPLEMENTATION(byte, Byte, char)

- (void)getBytes:(char *)buffer
          offset:(NSUInteger)offset
          length:(NSUInteger)length {
  IOSArray_checkRange(size_, NSMakeRange(offset, length));
  memcpy(buffer, &buffer_[offset], length);
}

- (void)replaceBytes:(const char *)source
              length:(NSUInteger)length
              offset:(NSUInteger)destOffset {
  IOSArray_checkRange(size_, NSMakeRange(destOffset, length));
  memcpy(&buffer_[destOffset], source, length);
}

- (NSString *)descriptionOfElementAtIndex:(NSUInteger)index {
  return [NSString stringWithFormat:@"0x%x", buffer_[index]];
}

- (IOSClass *)elementType {
  return [IOSClass byteClass];
}

+ (IOSClass *)iosClass {
  return [IOSClass arrayClassWithComponentType:[IOSClass byteClass]];
}

- (id)copyWithZone:(NSZone *)zone {
  return [[IOSByteArray allocWithZone:zone]
          initWithBytes:buffer_ count:size_];
}

- (NSData *)toNSData {
  return [NSData dataWithBytes:buffer_ length:size_];
}

- (void)dealloc {
  free(buffer_);
#if ! __has_feature(objc_arc)
  [super dealloc];
#endif
}

@end
