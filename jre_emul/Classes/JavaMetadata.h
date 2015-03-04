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

// Internal-use-only value classes that contain the reflection metadata
// for an IOSClass.

@interface JavaFieldMetadata : NSObject

- (instancetype)initWithMetadata:(const J2ObjcFieldInfo *)metadata;
- (NSString *)name;
- (NSString *)iosName;
- (NSString *)javaName;
- (int)modifiers;
- (id<JavaLangReflectType>)type;
- (const void *)staticRef;
- (const J2ObjcRawValue * const)getConstantValue;
- (NSString *)genericSignature;

@end

@interface JavaMethodMetadata : NSObject

- (instancetype)initWithMetadata:(const J2ObjcMethodInfo *)metadata;
- (SEL)selector;
- (NSString *)name;
- (NSString *)javaName;
- (NSString *)objcName;
- (int)modifiers;
- (id<JavaLangReflectType>)returnType;
- (IOSObjectArray *)exceptionTypes;
- (BOOL)isConstructor;
- (NSString *)genericSignature;

@end

@interface JavaEnclosingMethodMetadata : NSObject

@property (readonly, retain) NSString *typeName;
@property (readonly, retain) NSString *selector;

- (instancetype)initWithMetadata:(const J2ObjCEnclosingMethodInfo *)metadata;

@end

@interface JavaClassMetadata : NSObject

@property (readonly, assign) uint16_t version;
@property (readonly, retain) NSString *typeName;
@property (readonly, retain) NSString *packageName;
@property (readonly, retain) NSString *enclosingName;
@property (readonly, assign) uint16_t fieldCount;
@property (readonly, assign) uint16_t methodCount;
@property (readonly, assign) uint16_t modifiers;

- (id)initWithMetadata:(J2ObjcClassInfo *)metadata;

- (NSString *)qualifiedName;
- (JavaMethodMetadata *)findMethodMetadata:(NSString *)methodName;
- (JavaMethodMetadata *)findMethodMetadataWithJavaName:(NSString *)javaName
                                              argCount:(jint)argCount;
- (IOSObjectArray *)allMethods;
- (JavaFieldMetadata *)findFieldMetadata:(const char *)fieldName;
- (IOSObjectArray *)allFields;
- (IOSObjectArray *)getSuperclassTypeArguments;
- (IOSObjectArray *)getInnerClasses;
- (JavaEnclosingMethodMetadata *)getEnclosingMethod;
- (NSString *)genericSignature;

@end
