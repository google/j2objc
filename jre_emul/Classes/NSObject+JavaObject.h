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
//  NSObject+JavaObject.h
//  JreEmulation
//
//  Created by Tom Ball on 8/15/11.
//

#import <Foundation/Foundation.h>
#import "JavaObject.h"

@class IOSClass;

// A category that adds Java Object-compatible methods to NSObject.
@interface NSObject (JavaObject) <JavaObject>

// Object.clone()
- (id)clone;

// JavaObject doesn't implement Comparable, but the Comparable contract wants
// a ClassCastException to be thrown if "if the specified object's type
// prevents it from being compared to this object."  This method therefore
// throws a ClassCastException, unless overridden by a class implementing the
// Comparable protocol.
- (int)compareToWithId:(id)other;

+ (id)throwNullPointerException;

@end

// Marked as unused to avoid a clang warning when this file is included
// but NIL_CHK isn't used.
__attribute__ ((unused)) static id nil_chk(id p) {
  return p ? p : [NSObject throwNullPointerException];
}

#define NIL_CHK(p) nil_chk(p)
