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
//  ParameterizedTypeImpl.m
//  JreEmulation
//
//  Created by Keith Stanger on Nov 12, 2013.
//

#import "java/lang/reflect/ParameterizedTypeImpl.h"

#import "JreEmulation.h"

@implementation JavaLangReflectParameterizedTypeImpl

- (instancetype)initWithTypeArguments:(IOSObjectArray *)typeArgs
                            ownerType:(id<JavaLangReflectType>)ownerType
                              rawType:(id<JavaLangReflectType>)rawType {
  if ((self = [super init])) {
    actualTypeArguments_ = RETAIN_(typeArgs);
    ownerType_ = RETAIN_(ownerType);
    rawType_ = RETAIN_(rawType);
  }
  return self;
}

+ (instancetype)parameterizedTypeWithTypeArguments:(IOSObjectArray *)typeArgs
                                         ownerType:(id<JavaLangReflectType>)ownerType
                                           rawType:(id<JavaLangReflectType>)rawType {
  return AUTORELEASE([[JavaLangReflectParameterizedTypeImpl alloc] initWithTypeArguments:typeArgs
                                                                               ownerType:ownerType
                                                                                 rawType:rawType]);
}

- (IOSObjectArray *)getActualTypeArguments {
  return actualTypeArguments_;
}

- (id<JavaLangReflectType>)getOwnerType {
  return ownerType_;
}

- (id<JavaLangReflectType>)getRawType {
  return rawType_;
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [actualTypeArguments_ release];
  [ownerType_ release];
  [rawType_ release];
  [super dealloc];
}
#endif

@end
