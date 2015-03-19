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
#import "java/lang/AssertionError.h"
#import "java/lang/Exception.h"
#import "java/lang/reflect/ExecutableMember.h"

@implementation JavaClassMetadata {
  J2ObjcClassInfo *data_;
}

@synthesize version;
@synthesize typeName;
@synthesize packageName;
@synthesize enclosingName;
@synthesize fieldCount;
@synthesize methodCount;
@synthesize modifiers;

- (instancetype)initWithMetadata:(J2ObjcClassInfo *)metadata {
  if (self = [super init]) {
    data_ = metadata;
    version = data_->version;
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

static jint countArgs(char *s) {
  jint count = 0;
  while (*s) {
    if (*s++ == ':') {
      ++count;
    }
  }
  return count;
}

- (JavaMethodMetadata *)findMethodMetadataWithJavaName:(NSString *)javaName
                                        argCount:(jint)argCount {
  const char *name = [javaName cStringUsingEncoding:[NSString defaultCStringEncoding]];
  for (int i = 0; i < data_->methodCount; i++) {
    const char *cname = data_->methods[i].javaName;
    if (cname && strcmp(name, cname) == 0
        && argCount == countArgs((char *)data_->methods[i].selector)) {
      // Skip leading matches followed by "With", which follow the standard selector
      // pattern using typed parameters. This method is for resolving mapped methods
      // which don't follow that pattern, and thus need help from metadata.
      char buffer[256];
      strcpy(buffer, cname);
      strcat(buffer, "With");
      if (strncmp(buffer, data_->methods[i].selector, strlen(buffer)) != 0) {
        return [[JavaMethodMetadata alloc] initWithMetadata:&data_->methods[i]];
      }
    }
  }
  return nil;
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
  IOSObjectArray *result = [IOSObjectArray arrayWithLength:size type:JavaLangReflectType_class_()];
  for (int i = 0; i < size; i++) {
    IOSObjectArray_Set(result, i, JreTypeForString(data_->superclassTypeArgs[i]));
  }
  return result;
}

- (IOSObjectArray *)allFields {
  IOSObjectArray *result =
      [IOSObjectArray arrayWithLength:data_->fieldCount type:NSObject_class_()];
  J2ObjcFieldInfo *fields = (J2ObjcFieldInfo *) data_->fields;
  for (int i = 0; i < data_->fieldCount; i++) {
    [result replaceObjectAtIndex:i
                      withObject:[[JavaFieldMetadata alloc] initWithMetadata:fields++]];
  }
  return result;
}

- (IOSObjectArray *)allMethods {
  IOSObjectArray *result =
      [IOSObjectArray arrayWithLength:data_->methodCount type:NSObject_class_()];
  J2ObjcMethodInfo *methods = (J2ObjcMethodInfo *) data_->methods;
  for (int i = 0; i < data_->methodCount; i++) {
    [result replaceObjectAtIndex:i
                      withObject:[[JavaMethodMetadata alloc] initWithMetadata:methods++]];
  }
  return result;
}

- (IOSObjectArray *)getInnerClasses {
  if (J2OBJC_METADATA_VERSION < 2) {
    return 0;
  }
  uint16_t size = data_->innerClassCount;
  if (size == 0) {
    return nil;
  }
  IOSObjectArray *result = [IOSObjectArray arrayWithLength:size type:JavaLangReflectType_class_()];
  for (int i = 0; i < size; i++) {
    IOSObjectArray_Set(result, i, JreTypeForString(data_->innerClassnames[i]));
  }
  return result;
}

- (JavaEnclosingMethodMetadata *)getEnclosingMethod {
  if (J2OBJC_METADATA_VERSION < 2 || !data_->enclosingMethod) {
    return nil;
  }
  return [[[JavaEnclosingMethodMetadata alloc]
           initWithMetadata:data_->enclosingMethod] autorelease];
}

- (NSString *)genericSignature {
  if (J2OBJC_METADATA_VERSION < 2 || !data_->genericSignature) {
    return nil;
  }
  return [NSString stringWithUTF8String:data_->genericSignature];
}

- (NSString *)description {
  return [NSString stringWithFormat:@"{ typeName=%@ packageName=%@ modifiers=0x%x }",
          typeName, packageName, modifiers];
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [typeName release];
  [packageName release];
  [enclosingName release];
  [super dealloc];
}
#endif

@end

@implementation JavaFieldMetadata {
  const J2ObjcFieldInfo *data_;
}

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

- (NSString *)genericSignature {
  if (J2OBJC_METADATA_VERSION < 2 || !data_->genericSignature) {
    return nil;
  }
  return [NSString stringWithUTF8String:data_->genericSignature];
}

@end

@implementation JavaMethodMetadata {
  const J2ObjcMethodInfo *data_;
}

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

- (NSString *)objcName {
  return [NSString stringWithUTF8String:data_->selector];
}

- (int)modifiers {
  return data_->modifiers;
}

- (id<JavaLangReflectType>)returnType {
  return JreTypeForString(data_->returnType);
}

- (IOSObjectArray *)exceptionTypes {
  if (!data_->exceptions) {
    return nil;
  }

  const char *p = data_->exceptions;
  int n = 0;
  while (p != NULL) {
    const char *semi = strchr(p, ';');
    if (semi != NULL) {
      ++n;
      p = semi + 1;
    } else {
      p = NULL;
    }
  }
  IOSObjectArray *result = [IOSObjectArray arrayWithLength:(jint)n
                                                      type:JavaLangReflectType_class_()];
  jint count = 0;
  p = data_->exceptions;
  while (p != NULL) {
    char *semi = strchr(p, ';');
    if (semi != NULL) {
      char *exc = strndup(p, semi - p + 1);  // Include trailing ';'.
      IOSObjectArray_Set(result, count++, JreTypeForString(exc));
      free(exc);
      p = semi + 1;
    } else {
      p = NULL;
    }
  }
  return result;
}

- (BOOL)isConstructor {
  const char *name = data_->javaName ? data_->javaName : data_->selector;
  return strcmp(name, "init") == 0 || strstr(name, "initWith") == name;
}

- (NSString *)genericSignature {
  if (J2OBJC_METADATA_VERSION < 2 || !data_->genericSignature) {
    return nil;
  }
  return [NSString stringWithUTF8String:data_->genericSignature];
}

@end

@implementation JavaEnclosingMethodMetadata

@synthesize typeName;
@synthesize selector;

- (instancetype)initWithMetadata:(const J2ObjCEnclosingMethodInfo *)metadata {
  if (self = [super init]) {
    typeName = [[NSString alloc] initWithCString:metadata->typeName
                                        encoding:NSUTF8StringEncoding];
    selector = [[NSString alloc] initWithCString:metadata->selector
                                        encoding:NSUTF8StringEncoding];
  }
  return self;
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [typeName release];
  [selector release];
  [super dealloc];
}
#endif

@end
