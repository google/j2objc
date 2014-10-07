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
//  TypeVariableImpl.m
//  JreEmulation
//
//  Created by Keith Stanger on Nov 12, 2013.
//

#import "java/lang/reflect/TypeVariableImpl.h"

#import "JreEmulation.h"
#import "java/lang/AssertionError.h"

@implementation JavaLangReflectTypeVariableImpl

- (instancetype)initWithName:(NSString *)name {
  if ((self = [super init])) {
    name_ = RETAIN_(name);
  }
  return self;
}

+ (instancetype)typeVariableWithName:(NSString *)name {
  return AUTORELEASE([[JavaLangReflectTypeVariableImpl alloc] initWithName:name]);
}

- (IOSObjectArray *)getBounds {
  @throw AUTORELEASE([[JavaLangAssertionError alloc] initWithNSString:@"not implemented"]);
}

- (id)getGenericDeclaration {
  @throw AUTORELEASE([[JavaLangAssertionError alloc] initWithNSString:@"not implemented"]);
}

- (NSString *)getName {
  return name_;
}

- (NSString *)description {
  return name_;
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [name_ release];
  [super dealloc];
}
#endif

@end
