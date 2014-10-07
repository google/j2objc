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
//  TypeVariableImpl.h
//  JreEmulation
//
//  Created by Keith Stanger on Nov 12, 2013.
//

#ifndef _JavaLangReflectTypeVariableImpl_H_
#define _JavaLangReflectTypeVariableImpl_H_

#import "java/lang/reflect/TypeVariable.h"

@interface JavaLangReflectTypeVariableImpl : NSObject <JavaLangReflectTypeVariable> {
 @private
  NSString *name_;
}

- (instancetype)initWithName:(NSString *)name;
+ (instancetype)typeVariableWithName:(NSString *)name;

- (IOSObjectArray *)getBounds;
- (id)getGenericDeclaration;
- (NSString *)getName;

@end

#endif // _JavaLangReflectTypeVariableImpl_H_
