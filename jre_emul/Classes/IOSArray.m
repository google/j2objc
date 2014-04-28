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
//  IOSArray.m
//  JreEmulation
//
//  Created by Tom Ball on 6/21/11.
//

#import "IOSArray.h"
#import "IOSArrayClass.h"
#import "IOSClass.h"
#import "java/lang/AssertionError.h"
#import "java/lang/ArrayIndexOutOfBoundsException.h"

@implementation IOSArray

- (void)dealloc {
  JreMemDebugRemove(self);
#if ! __has_feature(objc_arc)
  [super dealloc];
#endif
}

+ (id)arrayWithDimensions:(NSUInteger)dimensionCount
                  lengths:(const int *)dimensionLengths {
  if (dimensionCount == 0) {
    @throw AUTORELEASE([[JavaLangAssertionError alloc] initWithId:@"invalid dimension count"]);
  }

  __unsafe_unretained IOSClass *componentTypes[dimensionCount];
  componentTypes[dimensionCount - 1] = nil;
  for (int i = dimensionCount - 2; i >= 0; i--) {
    __unsafe_unretained IOSClass *last = componentTypes[i + 1];
    if (last) {
      componentTypes[i] = [IOSClass arrayClassWithComponentType:last];
    } else {
      componentTypes[i] = [self iosClass];
    }
  }
  return [self arrayWithDimensions:dimensionCount lengths:dimensionLengths types:componentTypes];
}

+ (id)arrayWithDimensions:(NSUInteger)dimensionCount
                  lengths:(const int *)dimensionLengths
                    types:(__unsafe_unretained IOSClass * const *)componentTypes {
  NSUInteger size = *dimensionLengths;
  __unsafe_unretained IOSClass *componentType = *componentTypes;

  // If dimension of 1, just return a regular array.
  if (dimensionCount == 1) {
    if (componentType) {
      return [IOSObjectArray arrayWithLength:size type:componentType];
    } else {
      return [[self class] arrayWithLength:size];
    }
  }

  // Create an array of arrays, which is recursive to handle additional
  // dimensions.
  __unsafe_unretained id subarrays[size];
  for (NSUInteger i = 0; i < size; i++) {
    subarrays[i] = [[self class] arrayWithDimensions:dimensionCount - 1
                                             lengths:dimensionLengths + 1
                                               types:componentTypes + 1];
  }
  return [IOSObjectArray arrayWithObjects:subarrays count:size type:componentType];
}

+ (id)iosClass {
  @throw AUTORELEASE([[JavaLangAssertionError alloc] initWithNSString:
      @"abstract method not overridden"]);
}

+ (id)iosClassWithDimensions:(NSUInteger)dimensions {
  IOSClass *result = [self iosClass];
  while (--dimensions > 0) {
    result = [IOSClass arrayClassWithComponentType:result];
  }
  return result;
}

- (NSUInteger)count {
  return size_;
}

void IOSArray_throwOutOfBounds(NSUInteger size, NSUInteger index) {
  NSString *msg = [NSString stringWithFormat:
      @"index out of range: %ld for array containing %ld elements",
      (long)index, (long)size];
  @throw AUTORELEASE([[JavaLangArrayIndexOutOfBoundsException alloc] initWithNSString:msg]);
}

- (NSString *)descriptionOfElementAtIndex:(NSUInteger)index {
  [self doesNotRecognizeSelector:_cmd];
  return nil;
}

- (NSString *)description {
  if (size_ == 0) {
    return @"[]";
  }
  NSString *result = @"[";
  for (int i = 0; i < size_; i++) {
    NSString *separator = i < size_ - 1 ? @", " : @"]";
    result = [result stringByAppendingFormat:@"%@%@",
                     [self descriptionOfElementAtIndex:i], separator];
  }
  return result;
}

- (IOSClass *)getClass {
  return [IOSClass arrayClassWithComponentType:[self elementType]];
}

- (IOSClass *)elementType {
#if __has_feature(objc_arc)
  @throw [[JavaLangAssertionError alloc]
           initWithNSString:@"abstract method not overridden"];
#else
  @throw [[[JavaLangAssertionError alloc]
           initWithNSString:@"abstract method not overridden"] autorelease];
#endif
  return nil;
}

- (id)copyWithZone:(NSZone *)zone {
  IOSArray *copy = [[[self class] allocWithZone:zone] init];
  copy->size_ = size_;
  return copy;
}

- (id)clone {
  id result = [self copyWithZone:nil];
#if ! __has_feature(objc_arc)
  [result autorelease];
#endif
  return result;
}

- (void) arraycopy:(NSRange)sourceRange
       destination:(IOSArray *)destination
            offset:(NSInteger)offset {
#if __has_feature(objc_arc)
  @throw [[JavaLangAssertionError alloc]
          initWithNSString:@"abstract method not overridden"];
#else
  @throw [[[JavaLangAssertionError alloc]
           initWithNSString:@"abstract method not overridden"] autorelease];
#endif
}

@end
