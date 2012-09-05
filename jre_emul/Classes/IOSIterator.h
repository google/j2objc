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
//  IOSIterator.h
//  JreEmulation
//
//  Created by Tom Ball on 2/2/12.
//

#import <Foundation/Foundation.h>
#import "java/util/Iterator.h"

// Implementation of java.util.Iterator for IOSList and IOSSet.
@interface IOSIterator : NSObject < JavaUtilIterator > {
 @protected
  NSMutableArray *list_;
  int available_;
  int lastPosition_;
}

- (id)initWithList:(NSMutableArray *)list;

@end
