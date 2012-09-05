// Copyright 2012 Google Inc. All Rights Reserved.
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
//  IOSSet.m
//  JreEmulation
//
//  Created by Tom Ball on 1/27/12.
//

#import "IOSSet.h"

@implementation IOSSet

+ (IOSSet *)setWithJavaUtilSet:(id<JavaUtilSet>)set {
  IOSSet *newSet = [[IOSSet alloc] initWithJavaUtilCollection:set];
#if ! __has_feature(objc_arc)
  [newSet autorelease];
#endif
  return newSet;
}

- (id)initWithInt:(int)capacity withFloat:(float)loadFactor {
  return [self initWithInt:capacity];  
}

- (BOOL)canAdd:(id)object {
  return ![delegate_ containsObject:object];
}

@end
