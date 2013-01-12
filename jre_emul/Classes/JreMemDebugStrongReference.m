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

#import "JreMemDebugStrongReference.h"

@implementation JreMemDebugStrongReference {
#if !__has_feature(objc_arc)
  id object_;
#else
  __weak id object_;
#endif
  NSString * name_;
}

@synthesize object = object_;
@synthesize name = name_;

+ (JreMemDebugStrongReference *)strongReferenceWithObject:(id)object
                                                     name:(NSString *)name {
  JreMemDebugStrongReference *ref = [[JreMemDebugStrongReference alloc] init];
#if !__has_feature(objc_arc)
  [ref autorelease];
#endif
  [ref setObject:object];
  [ref setName:name];
  return ref;
}

#if !__has_feature(objc_arc)
- (void)dealloc {
  [name_ release];
  [super dealloc];
}
#endif

@end
