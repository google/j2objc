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
//  IOSMetadata.m
//  JreEmulation
//
//  Created by Tom Ball on 9/23/13.
//

#import "JavaMetadata.h"

#import "IOSClass.h"
#import "IOSObjectArray.h"
#import "IOSReflection.h"

@implementation JavaClassMetadata

@synthesize typeName;
@synthesize packageName;
@synthesize enclosingName;
@synthesize fieldCount;
@synthesize methodCount;
@synthesize modifiers;

- (instancetype)initWithMetadata:(J2ObjcClassInfo *)metadata {
  if (self = [super init]) {
    data_ = metadata;
    NSStringEncoding defaultEncoding = [NSString defaultCStringEncoding];
    typeName = [[NSString alloc] initWithCString:metadata->typeName encoding:defaultEncoding];
    if (metadata->packageName) {
      packageName =
          [[NSString alloc] initWithCString:metadata->packageName encoding:defaultEncoding];
    }
    if (metadata->enclosingName) {
      enclosingName =
          [[NSString alloc] initWithCString:metadata->enclosingName encoding:defaultEncoding];
    }
    fieldCount = metadata->fieldCount;
    methodCount = metadata->methodCount;
    modifiers = metadata->modifiers;
  }
  return self;
}

- (NSString *)qualifiedName {
  NSMutableString *qName = [NSMutableString string];
  if ([packageName length] > 0) {
    [qName appendString:packageName];
    [qName appendString:@"."];
  }
  if (enclosingName) {
    [qName appendString:enclosingName];
    [qName appendString:@"$"];
  }
  [qName appendString:typeName];
  return qName;
}

- (const J2ObjcMethodInfo *)findMethodInfo:(NSString *)methodName {
  const char *name = [methodName cStringUsingEncoding:[NSString defaultCStringEncoding]];
  for (int i = 0; i < data_->methodCount; i++) {
    if (strcmp(name, data_->methods[i].selector) == 0) {
      return &data_->methods[i];
    }
  }
  return nil;
}

- (JavaMethodMetadata *)findMethodMetadata:(NSString *)methodName {
  const J2ObjcMethodInfo *info = [self findMethodInfo:methodName];
  return info ? AUTORELEASE([[JavaMethodMetadata alloc] initWithMetadata:info]) : nil;
}

- (const J2ObjcFieldInfo *)findFieldInfo:(const char *)fieldName {
  for (int i = 0; i < data_->fieldCount; i++) {
    const J2ObjcFieldInfo *fieldInfo = &data_->fields[i];
    if (fieldInfo->javaName && strcmp(fieldName, fieldInfo->javaName) == 0) {
      return fieldInfo;
    }
    if (strcmp(fieldName, fieldInfo->name) == 0) {
      return fieldInfo;
    }
    // See if field name has trailing underscore added.
    size_t max  = strlen(fieldInfo->name) - 1;
    if (fieldInfo->name[max] == '_' && strlen(fieldName) == max &&
        strncmp(fieldName, fieldInfo->name, max) == 0) {
      return fieldInfo;
    }
  }
  return nil;
}

- (JavaFieldMetadata *)findFieldMetadata:(const char *)fieldName {
  const J2ObjcFieldInfo *info = [self findFieldInfo:fieldName];
  return info ? AUTORELEASE([[JavaFieldMetadata alloc] initWithMetadata:info]) : nil;
}

- (IOSObjectArray *)getSuperclassTypeArguments {
  uint16_t size = data_->superclassTypeArgsCount;
  if (size == 0) {
    return nil;
  }
  IOSObjectArray *result = [IOSObjectArray arrayWithLength:size type:
      [IOSClass classWithProtocol:@protocol(JavaLangReflectType)]];
  for (int i = 0; i < size; i++) {
    IOSObjectArray_Set(result, i, JreTypeForString(data_->superclassTypeArgs[i]));
  }
  return result;
}

- (IOSObjectArray *)allFields {
  IOSObjectArray *result =
      [IOSObjectArray arrayWithLength:data_->fieldCount type:[JavaFieldMetadata getClass]];
  J2ObjcFieldInfo *fields = (J2ObjcFieldInfo *) data_->fields;
  for (int i = 0; i < data_->fieldCount; i++) {
    [result replaceObjectAtIndex:i
                      withObject:[[JavaFieldMetadata alloc] initWithMetadata:fields++]];
  }
  return result;
}

- (IOSObjectArray *)allMethods {
  IOSObjectArray *result =
      [IOSObjectArray arrayWithLength:data_->methodCount type:[JavaMethodMetadata getClass]];
  J2ObjcMethodInfo *methods = (J2ObjcMethodInfo *) data_->methods;
  for (int i = 0; i < data_->methodCount; i++) {
    [result replaceObjectAtIndex:i
                      withObject:[[JavaMethodMetadata alloc] initWithMetadata:methods++]];
  }
  return result;
}

- (NSString *)description {
  return [NSString stringWithFormat:@"{ typeName=%@ packageName=%@ modifiers=0x%x }",
          typeName, packageName, modifiers];
}

- (void)dealloc {
  if (attributes) {
    free(attributes);
  }
#if ! __has_feature(objc_arc)
  [typeName release];
  [packageName release];
  [enclosingName release];
  [super dealloc];
#endif
}

@end

@implementation JavaFieldMetadata

- (instancetype)initWithMetadata:(const J2ObjcFieldInfo *)metadata {
  if (self = [super init]) {
    data_ = metadata;
  }
  return self;
}

- (NSString *)name {
  return data_->javaName ?
      [NSString stringWithUTF8String:data_->javaName] :
      [NSString stringWithUTF8String:data_->name];
}

- (NSString *)iosName {
  return [NSString stringWithUTF8String:data_->name];
}

- (NSString *)javaName {
  return data_->javaName ? [NSString stringWithUTF8String:data_->javaName] : nil;
}

- (int)modifiers {
  return data_->modifiers;
}

- (id<JavaLangReflectType>)type {
  return JreTypeForString(data_->type);
}

- (const void *)staticRef {
  return data_->staticRef;
}

- (const J2ObjcRawValue * const)getConstantValue {
  return &data_->constantValue;
}

@end

@implementation JavaMethodMetadata

- (instancetype)initWithMetadata:(const J2ObjcMethodInfo *)metadata {
  if (self = [super init]) {
    data_ = metadata;
  }
  return self;
}

- (SEL)selector {
  return sel_registerName(data_->selector);
}

- (NSString *)name {
  return data_->javaName ?
      [NSString stringWithUTF8String:data_->javaName] :
      [NSString stringWithUTF8String:data_->selector];
}

- (NSString *)javaName {
  return data_->javaName ? [NSString stringWithUTF8String:data_->javaName] : nil;
}

- (int)modifiers {
  return data_->modifiers;
}

- (id<JavaLangReflectType>)returnType {
  return JreTypeForString(data_->returnType);
}

- (IOSObjectArray *)exceptionTypes {
  NSString *exceptionsStr = [NSString stringWithUTF8String:data_->exceptions];
  NSArray *exceptionsArray = [exceptionsStr componentsSeparatedByString:@";"];
  // The last string is empty, due to the trailing semi-colon of the last exception.
  NSUInteger n = [exceptionsArray count] - 1;
  IOSObjectArray *result = [IOSObjectArray arrayWithLength:(jint)n type:[IOSClass getClass]];
  jint count = 0;
  for (NSUInteger i = 0; i < n; i++) {
    // Strip off leading 'L'.
    NSString *thrownException = [[exceptionsArray objectAtIndex:i] substringFromIndex:1];
    IOSObjectArray_Set(result, count++, [IOSClass forName:thrownException]);
  }
  return result;
}

- (BOOL)isConstructor {
  const char *name = data_->javaName ? data_->javaName : data_->selector;
  return strcmp(name, "init") == 0 || strstr(name, "initWith") == name;
}

@end
