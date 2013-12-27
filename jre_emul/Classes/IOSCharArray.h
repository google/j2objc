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
//  IOSCharArray.h
//  JreEmulation
//
//  Created by Tom Ball on 6/14/11.
//

#ifndef _IOSCHARARRAY_H
#define _IOSCHARARRAY_H

#import "IOSArray.h"

// An emulation class that represents a Java char array.  Like a Java array,
// an IOSCharArray is fixed-size but its elements are mutable.
@interface IOSCharArray : IOSArray {
 @public
  unichar *buffer_;
}

PRIMITIVE_ARRAY_INTERFACE(char, Char, unichar)

// Create an array from an NSString.
- (id)initWithNSString:(NSString *)string;
+ (id)arrayWithNSString:(NSString *)string;

// Returns a copy of the array contents.
- (unichar *)getChars;

@end

#endif // _IOSCHARARRAY_H
