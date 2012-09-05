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
//  Created by Pankaj Kakkar on 07/23/12.
//
//  Category that adds a method to NSData to return an IOSByteArray.

#import "NSData+IOSByteArray.h"
#import "IOSByteArray.h"

@implementation NSData (IOSByteArray)

- (IOSByteArray *)toByteArray {
  return [IOSByteArray arrayWithBytes:[self bytes] count:[self length]];
}

@end
