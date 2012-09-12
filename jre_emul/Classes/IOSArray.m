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

- (id)initWithLength:(NSUInteger)length {
  if ((self = [super init])) {
    size_ = length;
  }
  return self;
}

+ (id)arrayWithDimensions:(NSUInteger)dimensionCount
                  lengths:(NSUInteger *)dimensionLengths {
  if (dimensionCount == 0) {
    id exception = [[JavaLangAssertionError alloc]
                    initWithId:@"invalid dimension count"];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }

  NSUInteger size = *dimensionLengths;

  // If dimension of 1, just return a regular array.
  if (dimensionCount == 1) {
    id array = [[[self class] alloc] initWithLength:size];
#if ! __has_feature(objc_arc)
    [array autorelease];
#endif
    return array;
  }

  // Create an array of arrays, which is recursive to handle additional
  // dimensions.
  IOSObjectArray *result =
      [[IOSObjectArray alloc] initWithLength:size type:
       [IOSClass classWithClass:[IOSObjectArray class]]];
  for (NSUInteger i = 0; i < size; i++) {
    id subarray = [[self class] arrayWithDimensions:dimensionCount - 1
                                            lengths:dimensionLengths + 1];
    [result replaceObjectAtIndex:i withObject:subarray];
  }
#if ! __has_feature(objc_arc)
  [result autorelease];
#endif

  return result;
}

- (NSUInteger)count {
  return size_;
}

- (void)checkIndex:(NSUInteger)index {
  if (index >= size_) {
    NSString *msg = [NSString stringWithFormat:
        @"index out of range: %ld for array containing %ld elements",
        (long)index, (long)size_];
    id exception = [[JavaLangArrayIndexOutOfBoundsException alloc]
                    initWithNSString:msg];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }
}

- (void)checkRange:(NSRange)range {
  if (range.length > 0) {
    [self checkIndex:range.location];
    NSUInteger bounds = range.location + range.length;
    if (bounds > size_) {
      NSString *msg = [NSString stringWithFormat:
                       @"length out of range: %ld for array of %ld elements",
                       (long)bounds, (long)size_];
      id exception = [[JavaLangArrayIndexOutOfBoundsException alloc]
                      initWithNSString:msg];
#if ! __has_feature(objc_arc)
      [exception autorelease];
#endif
      @throw exception;
    }
  }
}

- (void)checkRange:(NSRange)range withOffset:(NSUInteger)offset {
  if (range.length == 0) {
    return;
  }
  
  [self checkIndex:offset];
  [self checkIndex:range.location];
  [self checkRange:NSMakeRange(range.location + offset, range.length)];
}

- (NSString *)descriptionOfElementAtIndex:(NSUInteger)index {
  [self doesNotRecognizeSelector:_cmd];
  return nil;
}

- (NSString *)description {
  NSString *result = @"[";
  for (int i = 0; i < size_; i++) {
    NSString *separator = i < size_ - 1 ? @", " : @"]";
    result = [result stringByAppendingFormat:@"%@%@",
                     [self descriptionOfElementAtIndex:i], separator];
  }

  return result;
}

- (IOSClass *)getClass {
  return [IOSArrayClass classWithComponentType:[self elementType]];
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

- (NSString *)binaryName {
  return [[self getClass] getName];
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
