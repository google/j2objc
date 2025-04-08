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
//  IOSPrimitiveClass.h
//  JreEmulation
//
//  Created by Tom Ball on 1/22/12.
//

#ifndef _IOSPrimitiveClass_H_
#define _IOSPrimitiveClass_H_

#import "IOSClass.h"

// An IOSClass instance for primitive Java types, which allow primitives to
// be used with Java reflection routines.  This class is minimal because Java
// primitive types have/need little runtime support, other than their name.
@interface IOSPrimitiveClass : IOSClass {
  NSString * _Nonnull name_;
  NSString * _Nonnull type_;
}

- (instancetype)initWithName:(NSString * _Nonnull)name type:(NSString * _Nonnull)type;

// For a primitive type, return its associated wrapper class.
- (IOSClass * _Nonnull)wrapperClass;

@end

#endif // _IOSPrimitiveClass_H_
