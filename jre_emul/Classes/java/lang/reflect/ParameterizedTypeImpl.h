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
//  ParameterizedTypeImpl.h
//  JreEmulation
//
//  Created by Keith Stanger on Nov 12, 2013.
//

#ifndef _JavaLangReflectParameterizedTypeImpl_H_
#define _JavaLangReflectParameterizedTypeImpl_H_

#import "java/lang/reflect/ParameterizedType.h"

@class IOSObjectArray;

@interface JavaLangReflectParameterizedTypeImpl : NSObject
    <JavaLangReflectParameterizedType> {
 @private
  IOSObjectArray *actualTypeArguments_;
  id<JavaLangReflectType> ownerType_;
  id<JavaLangReflectType> rawType_;
}

- (instancetype)initWithTypeArguments:(IOSObjectArray *)typeArgs
                            ownerType:(id<JavaLangReflectType>)ownerType
                              rawType:(id<JavaLangReflectType>)rawType;
+ (instancetype)parameterizedTypeWithTypeArguments:(IOSObjectArray *)typeArgs
                                         ownerType:(id<JavaLangReflectType>)ownerType
                                           rawType:(id<JavaLangReflectType>)rawType;

- (IOSObjectArray *)getActualTypeArguments;
- (id<JavaLangReflectType>)getOwnerType;
- (id<JavaLangReflectType>)getRawType;

@end

#endif // _JavaLangReflectParameterizedTypeImpl_H_
