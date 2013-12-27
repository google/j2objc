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
#import "IOSClass.h"

@implementation IOSCharArray

PRIMITIVE_ARRAY_IMPLEMENTATION(char, Char, unichar)

- (id)initWithNSString:(NSString *)string {
  int length = [string length];
  if ((self = [super initWithLength:length])) {
    if (length > 0) {
      buffer_ = malloc(length * sizeof(unichar));
      [string getCharacters:buffer_ range:NSMakeRange(0, length)];
    }
  }
  return self;
}

+ (id)arrayWithNSString:(NSString *)string {
  return AUTORELEASE([[IOSCharArray alloc] initWithNSString:string]);
}

- (unichar *)getChars {
  unichar *result = calloc(size_, sizeof(unichar));
  [self getChars:result length:size_];
  return result;
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
  return [[IOSCharArray allocWithZone:zone] initWithChars:buffer_ count:size_];
}

- (void)dealloc {
  free(buffer_);
#if ! __has_feature(objc_arc)
  [super dealloc];
#endif
}

@end
