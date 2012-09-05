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

#import "IOSObjectArray.h"
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
    elementType_ = type;
#if ! __has_feature(objc_arc)
    [elementType_ retain];
#endif
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
        id element = objects[i];
#if ! __has_feature(objc_arc)
        [element retain];
#endif
        buffer_[i] = element;
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

+ (id)arrayWithType:(IOSClass *)type count:(int)count args:(va_list)args {
  if (count > 0) {
    __unsafe_unretained id *objects =
        (__unsafe_unretained id *) calloc(count, sizeof(id));
    for (int i = 0; i < count; i++) {
      objects[i] = va_arg(args, id);
    }
    id array = [[self class] arrayWithObjects:objects
                                        count:count
                                         type:type];
    free(objects);
    return array;
  } else {
    return [[self class] arrayWithObjects:nil count:0 type:type];
  }
}

+ (id)arrayWithType:(IOSClass *)type count:(int)count, ... {
  va_list args;
  va_start(args, count);
  id result = [self arrayWithType:type count:count args:args];
  va_end(args);
  return result;
}

+ (id)arrayWithClass:(Class)clazz count:(int)count, ... {
  va_list args;
  va_start(args, count);
  id result = [self arrayWithType:[IOSClass classWithClass:clazz]
                            count:count
                             args:args];
  va_end(args);
  return result;
}

+ (id)arrayWithArray:(IOSObjectArray *)array {
  return [IOSObjectArray arrayWithObjects:array->buffer_ 
                                    count:array->size_
                                     type:array->elementType_];
}

+ (id)arrayWithNSArray:(NSArray *)array type:(IOSClass *)type {
  NSUInteger count = [array count];
  id *objects = malloc(sizeof(id) * count);
  [array getObjects:objects range:NSMakeRange(0, count)];
  id result = [IOSObjectArray arrayWithObjects:objects count:count type:type];
  free(objects);
  return result;
}

+ (id)arrayWithDimensions:(NSUInteger)dimensionCount
                  lengths:(NSUInteger *)dimensionLengths
                     type:(IOSClass *)type {

  // If dimension of 1, just return a regular array of objects.
  if (dimensionCount == 1) {
    NSUInteger size = *dimensionLengths;
    IOSObjectArray *result = [[[self class] alloc] initWithLength:size
                                                             type:type];
    return AUTORELEASE(result);
  }

  // Create an array of arrays, which is recursive to handle additional
  // dimensions.
  NSUInteger arraySize = *dimensionLengths;
  IOSObjectArray *result =
      [[IOSObjectArray alloc] initWithLength:arraySize type:
          [IOSClass classWithClass:[IOSObjectArray class]]];
  result->elementType_ = type;
#if ! __has_feature(objc_arc)
  [result->elementType_ retain];
#endif
  for (NSUInteger i = 0; i < arraySize; i++) {
    id subarray = [[self class] arrayWithDimensions:dimensionCount - 1
                                            lengths:dimensionLengths + 1
                                               type:type];
    [result replaceObjectAtIndex:i withObject:subarray];
  }

  return AUTORELEASE(result);
}

- (id)objectAtIndex:(NSUInteger)index {
  [self checkIndex:index];
  return buffer_[index];
}

- (id)replaceObjectAtIndex:(NSUInteger)index withObject:(id)value {
  [self checkIndex:index];
#if ! __has_feature(objc_arc)
  id prev = buffer_[index];
  [prev autorelease];
  [value retain];
#endif
  buffer_[index] = value;
  return value;
}

- (void)getObjects:(NSObject **)buffer length:(NSUInteger)length {
  [self checkIndex:(length - 1)];
  for (NSUInteger i = 0; i < length; i++) {
    id element = buffer_[i];
#if ! __has_feature(objc_arc)
    [element retain];
#endif
    buffer[i] = element;
  }
}

- (void) arraycopy:(NSRange)sourceRange
       destination:(IOSArray *)destination
            offset:(NSInteger)offset {
  [self checkRange:sourceRange];
  NSUInteger count = sourceRange.length;
  IOSObjectArray *dest = (IOSObjectArray *) destination;
  [dest checkRange:NSMakeRange(offset, count)];

  // Do ranges overlap?
  if (self == destination && sourceRange.location < offset) {
    for (int i = sourceRange.length - 1; i >= 0; i--) {
      id newElement = self->buffer_[i + sourceRange.location];
#if ! __has_feature(objc_arc)
      id oldElement = dest->buffer_[i + offset];
      [oldElement autorelease];
      [newElement retain];
#endif
      dest->buffer_[i + offset] = newElement;
    }
  } else {
    for (NSUInteger i = 0; i < sourceRange.length; i++) {
      id newElement = self->buffer_[i + sourceRange.location];
#if ! __has_feature(objc_arc)
      id oldElement = dest->buffer_[i + offset];
      [oldElement autorelease];
      [newElement retain];
#endif
      dest->buffer_[i + offset] = newElement;
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
  for (NSUInteger i = 0; i < size_; i++) {
    [buffer_[i] release];
  }
#endif
  free(buffer_);
#if ! __has_feature(objc_arc)
  [super dealloc];
#endif
}

@end
