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
//  IOSObjectArray.m
//  JreEmulation
//
//  Created by Tom Ball on 9/9/11.
//

#import "IOSArrayClass.h"
#import "IOSClass.h"
#import "IOSObjectArray.h"
#import "java/lang/ArrayStoreException.h"
#import "java/lang/AssertionError.h"

@implementation IOSObjectArray

@synthesize elementType = elementType_;

- (id)initWithLength:(NSUInteger)length {
  id exception = [[JavaLangAssertionError alloc]
                  initWithNSString:@"type argument not specified"];
  @throw AUTORELEASE(exception);
  return nil;
}

// Initializes buffer_ with a CFMutableArray.  This array has a fixed size like
// Java, done by setting its maximum size.  If no objects are specified, the
// array slots are initialized to null, which also provides access to the slot
// regardless of whether it holds an id or not.
- (id)initWithLength:(NSUInteger)length type:(IOSClass *)type {
  if ((self = [super initWithLength:length])) {
    buffer_ = (id __strong *) calloc(length, sizeof(id));
    elementType_ = RETAIN(type);
  }
  return self;
}

+ (id)arrayWithLength:(NSUInteger)length type:(IOSClass *)type {
  id array = [[IOSObjectArray alloc] initWithLength:length type:type];
  return AUTORELEASE(array);
}

- (id)initWithObjects:(const id *)objects
                count:(NSUInteger)count
                 type:(IOSClass *)type {
  if ((self = [self initWithLength:count type:type])) {
    if (objects != nil) {
      for (NSUInteger i = 0; i < count; i++) {
        buffer_[i] = RETAIN(objects[i]);
      }
    }
  }
  return self;
}

+ (id)arrayWithObjects:(const id *)objects
                 count:(NSUInteger)count
                  type:(IOSClass *)type {
  id array = [[IOSObjectArray alloc] initWithObjects:objects
                                               count:count
                                                type:type];
  return AUTORELEASE(array);
}

+ (id)arrayWithArray:(IOSObjectArray *)array {
  return [IOSObjectArray arrayWithObjects:array->buffer_
                                    count:array->size_
                                     type:array->elementType_];
}

+ (id)arrayWithNSArray:(NSArray *)array type:(IOSClass *)type {
  NSUInteger count = [array count];
  id __unsafe_unretained *objects =
      (id __unsafe_unretained *) malloc(sizeof(id) * count);
  [array getObjects:objects range:NSMakeRange(0, count)];
  id result = [IOSObjectArray arrayWithObjects:objects count:count type:type];
  free(objects);
  return result;
}

+ (id)arrayWithDimensions:(NSUInteger)dimensionCount
                  lengths:(const int *)dimensionLengths
                     type:(IOSClass *)type {
  if (dimensionCount == 0) {
    @throw AUTORELEASE([[JavaLangAssertionError alloc] initWithId:@"invalid dimension count"]);
  }

  NSUInteger size = *dimensionLengths;

  // If dimension of 1, just return a regular array of objects.
  if (dimensionCount == 1) {
    return AUTORELEASE([[[self class] alloc] initWithLength:size type:type]);
  }

  // Create an array of arrays, which is recursive to handle additional
  // dimensions.
  IOSObjectArray *result = [IOSObjectArray arrayWithLength:size type:
      [self iosClassWithDimensions:dimensionCount - 1 type:type]];
  for (NSUInteger i = 0; i < size; i++) {
    id subarray = [[self class] arrayWithDimensions:dimensionCount - 1
                                            lengths:dimensionLengths + 1
                                               type:type];
    [result replaceObjectAtIndex:i withObject:subarray];
  }

  return result;
}

+ (id)iosClassWithType:(IOSClass *)type {
  return [IOSArrayClass classWithComponentType:type];
}

+ (id)iosClassWithDimensions:(NSUInteger)dimensions type:(IOSClass *)type {
  IOSClass *result = [IOSArrayClass classWithComponentType:type];
  while (--dimensions > 0) {
    result = [IOSArrayClass classWithComponentType:result];
  }
  return result;
}

- (id)objectAtIndex:(NSUInteger)index {
  IOSArray_checkIndex(size_, index);
  return buffer_[index];
}

__attribute__ ((unused))
static inline id IOSObjectArray_checkValue(IOSObjectArray *array, id value) {
#if !defined(J2OBJC_DISABLE_ARRAY_CHECKS)
  if (value && ![array->elementType_ isInstance:value]) {
    NSString *msg = [NSString stringWithFormat:
        @"attempt to add object of type %@ to array with type %@",
        [[value getClass] getName], [array->elementType_ getName]];
    @throw AUTORELEASE([[JavaLangArrayStoreException alloc] initWithNSString:msg]);
  }
#endif
  return value;
}

- (id)replaceObjectAtIndex:(NSUInteger)index withObject:(id)value {
  IOSArray_checkIndex(size_, index);
#if ! __has_feature(objc_arc)
  id prev = buffer_[index];
  [prev autorelease];
  [value retain];
#endif
  buffer_[index] = IOSObjectArray_checkValue(self, value);
  return value;
}

- (void)getObjects:(NSObject **)buffer length:(NSUInteger)length {
  IOSArray_checkIndex(size_, length - 1);
  for (NSUInteger i = 0; i < length; i++) {
    id element = buffer_[i];
    buffer[i] = element;
  }
}

- (void) arraycopy:(NSRange)sourceRange
       destination:(IOSArray *)destination
            offset:(NSInteger)offset {
  IOSArray_checkRange(size_, sourceRange);
  IOSArray_checkRange(destination->size_, NSMakeRange(offset, sourceRange.length));
  IOSObjectArray *dest = (IOSObjectArray *) destination;

  // Do ranges overlap?
  if (self == destination && sourceRange.location < offset) {
    for (int i = sourceRange.length - 1; i >= 0; i--) {
      id newElement = self->buffer_[i + sourceRange.location];
#if ! __has_feature(objc_arc)
      id oldElement = dest->buffer_[i + offset];
      [oldElement autorelease];
      [newElement retain];
#endif
      dest->buffer_[i + offset] = IOSObjectArray_checkValue(dest, newElement);
    }
  } else {
    for (NSUInteger i = 0; i < sourceRange.length; i++) {
      id newElement = self->buffer_[i + sourceRange.location];
#if ! __has_feature(objc_arc)
      id oldElement = dest->buffer_[i + offset];
      [oldElement autorelease];
      [newElement retain];
#endif
      dest->buffer_[i + offset] = IOSObjectArray_checkValue(dest, newElement);
    }
  }
}

- (id)copyWithZone:(NSZone *)zone {
  IOSObjectArray *result =
      [[IOSObjectArray allocWithZone:zone] initWithLength:size_
                                                     type:elementType_];
  for (NSUInteger i = 0; i < size_; i++) {
    id element = buffer_[i];
#if ! __has_feature(objc_arc)
    [element retain];
#endif
    result->buffer_[i] = element;
  }
  return result;
}

- (NSString *)descriptionOfElementAtIndex:(NSUInteger)index {
  return (NSString *) [buffer_[index] description];
}

- (void)dealloc {
#if ! __has_feature(objc_arc)
  [elementType_ release];
#endif
  for (NSUInteger i = 0; i < size_; i++) {
#if __has_feature(objc_arc)
    buffer_[i] = nil;
#else
    [buffer_[i] release];
#endif
  }
  free(buffer_);
#if ! __has_feature(objc_arc)
  [super dealloc];
#endif
}

- (NSArray *)memDebugStrongReferences {
  NSMutableArray *result = [NSMutableArray array];
  for (NSUInteger i = 0; i < size_; i++) {
    [result addObject:[JreMemDebugStrongReference strongReferenceWithObject:buffer_[i] name:@"element"]];
  }
  return result;
}

@end
