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
//  JavaMetadata.h
//  JreEmulation
//
//  Created by Tom Ball on 9/23/13.
//

#import "JreEmulation.h"

// An internal-use-only value object that contains the reflection metadata
// for an IOSClass.
@interface JavaClassMetadata : NSObject {
  J2ObjcClassInfo *data_;
  J2ObjCAttribute *attributes;
}

@property (readonly, retain) NSString *typeName;
@property (readonly, retain) NSString *packageName;
@property (readonly, retain) NSString *enclosingName;
@property (readonly, assign) uint16_t modifiers;

- (id)initWithMetadata:(J2ObjcClassInfo *)metadata;

- (NSString *)qualifiedName;
- (const J2ObjcMethodInfo *)findMethodInfo:(NSString *)methodName;
- (const J2ObjcFieldInfo *)findFieldInfo:(Ivar)field;
- (IOSObjectArray *)getSuperclassTypeArguments;

@end
