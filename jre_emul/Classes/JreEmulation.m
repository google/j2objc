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
//  JreEmulation.m
//  J2ObjC
//
//  Created by Tom Ball on 4/23/12.
//

#import "JreEmulation.h"

// Converts main() arguments into an IOSObjectArray of NSStrings.  The first
// argument, the program name, is skipped so the returned array matches what
// is passed to a Java main method.
FOUNDATION_EXPORT
    IOSObjectArray *JreEmulationMainArguments(int argc, const char *argv[]) {
  IOSClass *stringType = [IOSClass classWithClass:[NSString class]];
  if (argc <= 1) {
    return [IOSObjectArray arrayWithType:stringType count:0];
  }
  IOSObjectArray *args = [[IOSObjectArray alloc] initWithLength:argc - 1
                                                           type:stringType];
#if ! __has_feature(objc_arc)
  [args autorelease];
#endif
  for (int i = 1; i < argc; i++) {
    NSString *arg =
        [NSString stringWithCString:argv[i]
                           encoding:[NSString defaultCStringEncoding]];
    [args replaceObjectAtIndex:i - 1 withObject:arg];
  }
  return args;
}