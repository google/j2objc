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
//  IOSList.h
//  JreEmulation
//
//  Created by Tom Ball on 1/27/12.
//

#import <Foundation/Foundation.h>
#import "IOSCollection.h"
#import "IOSIterator.h"
#import "java/util/List.h"
#import "java/util/ListIterator.h"

// An IOSCollection implementation for the java.util.List protocol, with
// additional methods from the public ArrayList API.
@interface IOSList : IOSCollection < JavaUtilList > {
}

+ (IOSList *)listWithJavaUtilList:(id<JavaUtilList>)list;

- (void)ensureCapacityWithInt:(int)minimumCapacity;
- (void)removeRangeWithInt:(int)start withInt:(int)end;
- (void)trimToSize;

@end

// Private class, returned by IOSList.iterator() and IOSList.listIterator().
@interface IOSListIterator : IOSIterator < JavaUtilListIterator > {
}

- (id)initWithList:(NSMutableArray *)list location:(NSUInteger)location;

@end
