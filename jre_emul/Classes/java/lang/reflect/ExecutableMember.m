//
//  ExecutableMember.m
//  JreEmulation
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
//  Created by Tom Ball on 11/11/11.
//

#import "ExecutableMember.h"
#import "IOSClass.h"
#import "IOSObjectArray.h"
#import "IOSReflection.h"
#import "java/lang/ClassLoader.h"
#import "java/lang/NoSuchMethodException.h"
#import "java/lang/StringBuilder.h"
#import "java/lang/annotation/Annotation.h"
#import "java/lang/reflect/Constructor.h"
#import "java/lang/reflect/Method.h"
#import "java/lang/reflect/Modifier.h"
#import "java/lang/reflect/Type.h"
#import "java/lang/reflect/TypeVariable.h"
#import "libcore/reflect/GenericSignatureParser.h"
#import "libcore/reflect/ListOfTypes.h"
#import "libcore/reflect/Types.h"
#import "objc/message.h"
#import "objc/runtime.h"

@interface ExecutableMember () {
  _Atomic(IOSObjectArray *) paramTypes_;
}
@end

// Value class from Android's java.lang.reflect.AbstractMethod class.
@interface GenericInfo : NSObject {
 @public
  LibcoreReflectListOfTypes *genericExceptionTypes_;
  LibcoreReflectListOfTypes *genericParameterTypes_;
  id<JavaLangReflectType> genericReturnType_;
  IOSObjectArray *formalTypeParameters_;
}

-(instancetype)init:(LibcoreReflectListOfTypes *)exceptions
         parameters:(LibcoreReflectListOfTypes *)parameters
         returnType:(id<JavaLangReflectType>)returnType
     typeParameters:(IOSObjectArray *)typeParameters;
@end

static GenericInfo *getMethodOrConstructorGenericInfo(ExecutableMember *self);

@implementation ExecutableMember

- (instancetype)initWithDeclaringClass:(IOSClass *)aClass
                              metadata:(const J2ObjcMethodInfo *)metadata {
  if ((self = [super init])) {
    class_ = aClass; // IOSClass types are never dealloced.
    metadata_ = metadata;
    ptrTable_ = IOSClass_GetMetadataOrFail(aClass)->ptrTable;
  }
  return self;
}

- (NSString *)getName {
  // can't call an abstract method
  [self doesNotRecognizeSelector:_cmd];
  return nil;
}

- (int)getModifiers {
  return metadata_->modifiers;
}

- (IOSObjectArray *)getParameterTypesInternal {
  IOSObjectArray *result = __c11_atomic_load(&paramTypes_, __ATOMIC_ACQUIRE);
  if (!result) {
    @synchronized(self) {
      result = __c11_atomic_load(&paramTypes_, __ATOMIC_RELAXED);
      if (!result) {
        result = [JreParseClassList(JrePtrAtIndex(ptrTable_, metadata_->paramsIdx)) retain];
        __c11_atomic_store(&paramTypes_, result, __ATOMIC_RELEASE);
      }
    }
  }
  return result;
}

- (IOSObjectArray *)getParameterTypes {
  return [IOSObjectArray arrayWithArray:[self getParameterTypesInternal]];
}

// Returns the class this executable is a member of.
- (IOSClass *)getDeclaringClass {
  return class_;
}

- (IOSObjectArray *)getTypeParameters {
  GenericInfo *info = getMethodOrConstructorGenericInfo(self);
  if (info->formalTypeParameters_->size_ == 0) {
    return info->formalTypeParameters_;
  }
  return [info->formalTypeParameters_ clone];
}

- (IOSObjectArray *)getGenericParameterTypes {
  return LibcoreReflectTypes_getTypeArray_clone_(
      getMethodOrConstructorGenericInfo(self)->genericParameterTypes_, false);
}

- (IOSObjectArray *)getGenericExceptionTypes {
  return LibcoreReflectTypes_getTypeArray_clone_(
      getMethodOrConstructorGenericInfo(self)->genericExceptionTypes_, false);
}

- (jboolean)isSynthetic {
  return (metadata_->modifiers & JavaLangReflectModifier_SYNTHETIC) > 0;
}

- (IOSObjectArray *)getExceptionTypes {
  return JreParseClassList(JrePtrAtIndex(ptrTable_, metadata_->exceptionsIdx));
}

- (IOSObjectArray *)getDeclaredAnnotations {
  id (*annotations)() = JrePtrAtIndex(ptrTable_, metadata_->annotationsIdx);
  if (annotations) {
    return annotations();
  }
  return [IOSObjectArray arrayWithLength:0 type:JavaLangAnnotationAnnotation_class_()];
}

- (IOSObjectArray *)getParameterAnnotations {
  id (*paramAnnotations)() = JrePtrAtIndex(ptrTable_, metadata_->paramAnnotationsIdx);
  if (paramAnnotations) {
    return paramAnnotations();
  }
  // No parameter annotations, so return an array of empty arrays, one for each parameter.
  jint nParams = [self getParameterTypesInternal]->size_;
  return [IOSObjectArray arrayWithDimensions:2 lengths:(int[]){nParams, 0}
      type:JavaLangAnnotationAnnotation_class_()];
}

- (NSString *)toGenericString {
  // Code generated from Android's java.lang.reflect.AbstractMethod class.
  JavaLangStringBuilder *sb = [[JavaLangStringBuilder alloc] initWithInt:80];
  GenericInfo *info = getMethodOrConstructorGenericInfo(self);
  jint modifiers = metadata_->modifiers;
  if (modifiers != 0) {
    [[sb appendWithNSString:
        JavaLangReflectModifier_toStringWithInt_(modifiers & ~JavaLangReflectModifier_VARARGS)]
     appendWithChar:' '];
  }
  if (info && info->formalTypeParameters_ && info->formalTypeParameters_->size_ > 0) {
    [sb appendWithChar:'<'];
    for (jint i = 0; i < info->formalTypeParameters_->size_; i++) {
      LibcoreReflectTypes_appendGenericType_type_(
          sb, IOSObjectArray_Get(info->formalTypeParameters_, i));
      if (i < info->formalTypeParameters_->size_ - 1) {
        [sb appendWithNSString:@","];
      }
    }
    [sb appendWithNSString:@"> "];
  }
  IOSClass *declaringClass = [self getDeclaringClass];
  if ([self isKindOfClass:[JavaLangReflectConstructor class]]) {
    LibcoreReflectTypes_appendTypeName_class_(sb, declaringClass);
  } else {
    if (info) {
      LibcoreReflectTypes_appendGenericType_type_(
          sb, LibcoreReflectTypes_getType_(info->genericReturnType_));
    }
    [sb appendWithChar:' '];
    LibcoreReflectTypes_appendTypeName_class_(sb, declaringClass);
    [[sb appendWithNSString:@"."] appendWithNSString:[self getName]];
  }
  [sb appendWithChar:'('];
  if (info) {
    LibcoreReflectTypes_appendArrayGenericType_types_(
        sb, [info->genericParameterTypes_ getResolvedTypes]);
  }
  [sb appendWithChar:')'];
  if (info) {
    IOSObjectArray *genericExceptionTypeArray =
        LibcoreReflectTypes_getTypeArray_clone_(info->genericExceptionTypes_, false);
    if (genericExceptionTypeArray->size_ > 0) {
      [sb appendWithNSString:@" throws "];
      LibcoreReflectTypes_appendArrayGenericType_types_(sb, genericExceptionTypeArray);
    }
  }
  return [sb description];
}

- (jboolean)isVarArgs {
  return (metadata_->modifiers & JavaLangReflectModifier_VARARGS) > 0;
}

- (jboolean)isBridge {
  // Translator doesn't generate bridge methods.
  return false;
}

- (SEL)getSelector {
  return JreMethodSelector(metadata_);
}

// isEqual and hash are uniquely identified by their class and selectors.
- (BOOL)isEqual:(id)anObject {
  if (![anObject isKindOfClass:[ExecutableMember class]]) {
    return NO;
  }
  ExecutableMember *other = (ExecutableMember *) anObject;
  return class_ == other->class_ && metadata_ == other->metadata_;
}

- (NSUInteger)hash {
  return [class_ hash] ^ (NSUInteger)metadata_;
}

- (void)dealloc {
  [__c11_atomic_load(&paramTypes_, __ATOMIC_RELAXED) release];
  [super dealloc];
}

+ (const J2ObjcClassInfo *)__metadata {
  static J2ObjcMethodInfo methods[] = {
    { NULL, NULL, 0x1, -1, -1, -1, -1, -1, -1 },
  };
  methods[0].selector = @selector(init);
  static const J2ObjcClassInfo _ExecutableMember = {
    "ExecutableMember", "java.lang.reflect", NULL, methods, NULL, 7, 0x401, 1, 0, -1, -1, -1, -1, -1
  };
  return &_ExecutableMember;
}

// Function generated from Android's java.lang.reflect.AbstractMethod class.
GenericInfo *getMethodOrConstructorGenericInfo(ExecutableMember *self) {
  const J2ObjcMethodInfo *metadata = self->metadata_;
  NSString *signatureAttribute = JreMethodGenericString(metadata, self->ptrTable_);
  jboolean isMethod = [self isKindOfClass:[JavaLangReflectMethod class]];
  IOSObjectArray *exceptionTypes = JreParseClassList(
      JrePtrAtIndex(self->ptrTable_, metadata->exceptionsIdx));
  LibcoreReflectGenericSignatureParser *parser =
      [[[LibcoreReflectGenericSignatureParser alloc]
        initWithJavaLangClassLoader:JavaLangClassLoader_getSystemClassLoader()] autorelease];
  if (isMethod) {
    [parser parseForMethodWithJavaLangReflectGenericDeclaration:self
                                                   withNSString:signatureAttribute
                                              withIOSClassArray:exceptionTypes];
  }
  else {
    [parser parseForConstructorWithJavaLangReflectGenericDeclaration:self
                                                        withNSString:signatureAttribute
                                                   withIOSClassArray:exceptionTypes];
  }
  return [[[GenericInfo alloc] init:parser->exceptionTypes_
                         parameters:parser->parameterTypes_
                         returnType:parser->returnType_
                     typeParameters:parser->formalTypeParameters_] autorelease];
}

@end

@implementation GenericInfo

-(instancetype)init:(LibcoreReflectListOfTypes *)exceptions
         parameters:(LibcoreReflectListOfTypes *)parameters
         returnType:(id<JavaLangReflectType>)returnType
     typeParameters:(IOSObjectArray *)typeParameters {
  if ((self = [super init])) {
    genericExceptionTypes_ = [exceptions retain];
    genericParameterTypes_ = [parameters retain];
    genericReturnType_ = [returnType retain];
    formalTypeParameters_ = [typeParameters retain];
  }
  return self;
}

- (void)dealloc {
  [genericExceptionTypes_ release];
  [genericParameterTypes_ release];
  [genericReturnType_ release];
  [formalTypeParameters_ release];
  [super dealloc];
}

@end
