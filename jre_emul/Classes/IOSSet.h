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
//  IOSSet.h
//  JreEmulation
//
//  Created by Tom Ball on 1/27/12.
//

#import <Foundation/Foundation.h>
#import "IOSCollection.h"
#import "java/util/Set.h"

@class IOSMap;

// An IOSCollection subclass that implements the java.util.Set protocol.
// Its new methods are defined by HashSet's public API, so that this class can
// be easily swapped for HashSet.
@interface IOSSet : IOSCollection < JavaUtilSet > {
}

+ (IOSSet *)setWithJavaUtilSet:(id<JavaUtilSet>)set;

- (id)initWithInt:(int)capacity withFloat:(float)loadFactor;

@end
