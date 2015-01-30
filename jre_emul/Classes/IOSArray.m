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
#import "IOSObjectArray.h"
#import "IOSPrimitiveArray.h"
#import "java/lang/AssertionError.h"
#import "java/lang/ArrayIndexOutOfBoundsException.h"

static id NewArrayWithDimensionsAndComponentTypes(
    Class self, NSUInteger dimensionCount, const jint *dimensionLengths,
    IOSClass * const *componentTypes) {
  jint size = *dimensionLengths;
  __unsafe_unretained IOSClass *componentType = *componentTypes;

  // If dimension of 1, just return a regular array.
  if (dimensionCount == 1) {
    if (componentType) {
      return [IOSObjectArray newArrayWithLength:size type:componentType];
    } else {
      return [self newArrayWithLength:size];
    }
  }

  // Create an array of arrays, which is recursive to handle additional
  // dimensions.
  __unsafe_unretained id subarrays[size];
  for (jint i = 0; i < size; i++) {
    subarrays[i] = [NewArrayWithDimensionsAndComponentTypes(
        self, dimensionCount - 1, dimensionLengths + 1, componentTypes + 1) autorelease];
  }
  return [IOSObjectArray newArrayWithObjects:subarrays count:size type:componentType];
}

id IOSArray_NewArrayWithDimensions(
    Class self, NSUInteger dimensionCount, const jint *dimensionLengths, IOSClass *type) {
  if (dimensionCount == 0) {
    @throw AUTORELEASE([[JavaLangAssertionError alloc] initWithId:@"invalid dimension count"]);
  }

  __unsafe_unretained IOSClass *componentTypes[dimensionCount];
  componentTypes[dimensionCount - 1] = type;
  for (NSInteger i = (NSInteger) dimensionCount - 2; i >= 0; i--) {
    __unsafe_unretained IOSClass *last = componentTypes[i + 1];
    if (last) {
      componentTypes[i] = IOSClass_arrayOf(last);
    } else {
      componentTypes[i] = [self iosClass];
    }
  }
  return NewArrayWithDimensionsAndComponentTypes(
      self, dimensionCount, dimensionLengths, componentTypes);
}

@implementation IOSArray

+ (id)arrayWithDimensions:(NSUInteger)dimensionCount
                  lengths:(const jint *)dimensionLengths {
  return [IOSArray_NewArrayWithDimensions(self, dimensionCount, dimensionLengths, nil) autorelease];
}

+ (id)newArrayWithDimensions:(NSUInteger)dimensionCount
                     lengths:(const jint *)dimensionLengths {
  return IOSArray_NewArrayWithDimensions(self, dimensionCount, dimensionLengths, nil);
}

+ (id)iosClass {
  @throw AUTORELEASE([[JavaLangAssertionError alloc] initWithNSString:
      @"abstract method not overridden"]);
}

- (jint)length {
  return size_;
}

- (NSUInteger)count {
  return size_;
}

void IOSArray_throwOutOfBounds() {
  @throw AUTORELEASE([[JavaLangArrayIndexOutOfBoundsException alloc] init]);
}

void IOSArray_throwOutOfBoundsWithMsg(jint size, jint index) {
  NSString *msg = [NSString stringWithFormat:
      @"index out of range: %d for array containing %d elements", index, size];
  @throw AUTORELEASE([[JavaLangArrayIndexOutOfBoundsException alloc] initWithNSString:msg]);
}

- (NSString *)descriptionOfElementAtIndex:(jint)index {
  [self doesNotRecognizeSelector:_cmd];
  return nil;
}

- (NSString *)description {
  if (size_ == 0) {
    return @"[]";
  }
  NSString *result = @"[";
  for (jint i = 0; i < size_; i++) {
    NSString *separator = i < size_ - 1 ? @", " : @"]";
    result = [result stringByAppendingFormat:@"%@%@",
                     [self descriptionOfElementAtIndex:i], separator];
  }
  return result;
}

- (IOSClass *)getClass {
  return IOSClass_arrayOf([self elementType]);
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

- (void)arraycopy:(jint)offset
      destination:(IOSArray *)destination
        dstOffset:(jint)dstOffset
           length:(jint)length {
  @throw [[[JavaLangAssertionError alloc]
      initWithNSString:@"abstract method not overridden"] autorelease];
}

- (void *)buffer {
  return nil;
}

@end
