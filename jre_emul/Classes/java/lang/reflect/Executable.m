//
//  Executable.m
//  JreEmulation
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

#import "IOSClass.h"
#import "IOSObjectArray.h"
#import "IOSReflection.h"
#import "java/lang/AssertionError.h"
#import "java/lang/ClassLoader.h"
#import "java/lang/NoSuchMethodException.h"
#import "java/lang/StringBuilder.h"
#import "java/lang/annotation/Annotation.h"
#import "java/lang/reflect/Constructor.h"
#import "java/lang/reflect/Executable.h"
#import "java/lang/reflect/Method.h"
#import "java/lang/reflect/Modifier.h"
#import "java/lang/reflect/Parameter.h"
#import "java/lang/reflect/Type.h"
#import "java/lang/reflect/TypeVariable.h"
#import "libcore/reflect/AnnotatedElements.h"
#import "libcore/reflect/GenericSignatureParser.h"
#import "libcore/reflect/ListOfTypes.h"
#import "libcore/reflect/Types.h"
#import "objc/message.h"
#import "objc/runtime.h"

@interface JavaLangReflectExecutable () {
  _Atomic(IOSObjectArray *) params_;
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

static GenericInfo *getMethodOrConstructorGenericInfo(JavaLangReflectExecutable *self);

@implementation JavaLangReflectExecutable

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

- (jint)getParameterCount {
  return [self getParameterTypesInternal]->size_;
}

- (IOSObjectArray *)getParametersInternal {
  IOSObjectArray *result = __c11_atomic_load(&params_, __ATOMIC_ACQUIRE);
  if (!result) {
    @synchronized(self) {
      result = __c11_atomic_load(&params_, __ATOMIC_RELAXED);
      if (!result) {
        IOSObjectArray *paramTypes = [self getParameterTypesInternal];
        jint nParams = paramTypes->size_;
        result = [IOSObjectArray newArrayWithLength:nParams type:JavaLangReflectParameter_class_()];
        for (jint i = 0; i < nParams; i++) {
          NSString *name = [NSString stringWithFormat:@"arg%d", i];
          id param =
              new_JavaLangReflectParameter_initWithNSString_withInt_withJavaLangReflectExecutable_withInt_(
                  name, 0, self, i);
          IOSObjectArray_Set(result, i, param);
        }
        __c11_atomic_store(&params_, result, __ATOMIC_RELEASE);
      }
    }
  }
  return result;
}

- (IOSObjectArray *)getParameters {
  return [IOSObjectArray arrayWithArray:[self getParametersInternal]];
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
  return [info->formalTypeParameters_ java_clone];
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
  id (*annotations)(void) = JrePtrAtIndex(ptrTable_, metadata_->annotationsIdx);
  if (annotations) {
    return annotations();
  }
  return [IOSObjectArray arrayWithLength:0 type:JavaLangAnnotationAnnotation_class_()];
}

- (IOSObjectArray *)getParameterAnnotations {
  id (*paramAnnotations)(void) = JrePtrAtIndex(ptrTable_, metadata_->paramAnnotationsIdx);
  if (paramAnnotations) {
    return paramAnnotations();
  }
  // No parameter annotations, so return an array of empty arrays, one for each parameter.
  jint nParams = [self getParameterTypesInternal]->size_;
  return [IOSObjectArray arrayWithDimensions:2 lengths:(int[]){nParams, 0}
      type:JavaLangAnnotationAnnotation_class_()];
}

- (id<JavaLangReflectAnnotatedType>)getAnnotatedReturnType {
  return nil;
}

- (IOSObjectArray *)getAnnotatedParameterTypes {
  return [IOSObjectArray newArrayWithLength:0 type:IOSClass_class_()];
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

- (SEL)getSelector {
  return JreMethodSelector(metadata_);
}

- (jboolean)hasRealParameterData {
  return metadata_ != nil;
}

- (IOSObjectArray *)getAllGenericParameterTypes {
  return LibcoreReflectTypes_getTypeArray_clone_(
      getMethodOrConstructorGenericInfo(self)->genericParameterTypes_, false);
}

- (IOSObjectArray *)getAnnotationsByTypeWithIOSClass:(IOSClass *)cls {
  return
      LibcoreReflectAnnotatedElements_getDirectOrIndirectAnnotationsByTypeWithJavaLangReflectAnnotatedElement_withIOSClass_(
          self, nil_chk(cls));
}

// isEqual and hash are uniquely identified by their class and selectors.
- (BOOL)isEqual:(id)anObject {
  if (![anObject isKindOfClass:[JavaLangReflectExecutable class]]) {
    return NO;
  }
  JavaLangReflectExecutable *other = (JavaLangReflectExecutable *) anObject;
  return class_ == other->class_ && metadata_ == other->metadata_;
}

- (NSUInteger)hash {
  return [class_ hash] ^ (NSUInteger)metadata_;
}

#if !__has_feature(objc_arc)
- (void)dealloc {
  [__c11_atomic_load(&paramTypes_, __ATOMIC_RELAXED) release];
  [super dealloc];
}
#endif

+ (const J2ObjcClassInfo *)__metadata {
  static J2ObjcMethodInfo methods[] = {
    { NULL, NULL, 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "LIOSClass;", 0x401, -1, -1, -1, 0, -1, -1 },
    { NULL, "LNSString;", 0x401, -1, -1, -1, -1, -1, -1 },
    { NULL, "I", 0x401, -1, -1, -1, -1, -1, -1 },
    { NULL, "[LJavaLangReflectTypeVariable;", 0x401, -1, -1, -1, 1, -1, -1 },
    { NULL, "[LIOSClass;", 0x401, -1, -1, -1, 2, -1, -1 },
    { NULL, "[LIOSClass;", 0x401, -1, -1, -1, 2, -1, -1 },
    { NULL, "[[LJavaLangAnnotationAnnotation;", 0x401, -1, -1, -1, -1, -1, -1 },
    { NULL, "LNSString;", 0x401, -1, -1, -1, -1, -1, -1 },
    { NULL, "I", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "[LJavaLangReflectType;", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "[LJavaLangReflectParameter;", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "[LJavaLangReflectType;", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "Z", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "Z", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "LJavaLangAnnotationAnnotation;", 0x1, 3, 4, -1, 5, -1, -1 },
    { NULL, "[LJavaLangAnnotationAnnotation;", 0x1, 6, 4, -1, 7, -1, -1 },
    { NULL, "[LJavaLangAnnotationAnnotation;", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "Z", 0x0, -1, -1, -1, -1, -1, -1 },
    { NULL, "[LJavaLangReflectType;", 0x0, -1, -1, -1, -1, -1, -1 },
  };
  #pragma clang diagnostic push
  #pragma clang diagnostic ignored "-Wobjc-multiple-method-names"
  methods[0].selector = @selector(init);
  methods[1].selector = @selector(getDeclaringClass);
  methods[2].selector = @selector(getName);
  methods[3].selector = @selector(getModifiers);
  methods[4].selector = @selector(getTypeParameters);
  methods[5].selector = @selector(getParameterTypes);
  methods[6].selector = @selector(getExceptionTypes);
  methods[7].selector = @selector(getParameterAnnotations);
  methods[8].selector = @selector(toGenericString);
  methods[9].selector = @selector(getParameterCount);
  methods[10].selector = @selector(getGenericParameterTypes);
  methods[11].selector = @selector(getParameters);
  methods[12].selector = @selector(getGenericExceptionTypes);
  methods[13].selector = @selector(isVarArgs);
  methods[14].selector = @selector(isSynthetic);
  methods[15].selector = @selector(getAnnotationWithIOSClass:);
  methods[16].selector = @selector(getAnnotationsByTypeWithIOSClass:);
  methods[17].selector = @selector(getDeclaredAnnotations);
  methods[18].selector = @selector(hasRealParameterData);
  methods[19].selector = @selector(getAllGenericParameterTypes);
  #pragma clang diagnostic pop
  static const void *ptrTable[] = {
    "()Ljava/lang/Class<*>;", "()[Ljava/lang/reflect/TypeVariable<*>;", "()[Ljava/lang/Class<*>;",
    "getAnnotation", "LIOSClass;",
    "<T::Ljava/lang/annotation/Annotation;>(Ljava/lang/Class<TT;>;)TT;", "getAnnotationsByType",
    "<T::Ljava/lang/annotation/Annotation;>(Ljava/lang/Class<TT;>;)[TT;" };
  static const J2ObjcClassInfo _JavaLangReflectExecutable = {
    "Executable", "java.lang.reflect", ptrTable, methods, NULL, 7, 0x401, 20, 0, -1, -1, -1, -1, -1
  };
  return &_JavaLangReflectExecutable;
}

// Function generated from Android's java.lang.reflect.AbstractMethod class.
GenericInfo *getMethodOrConstructorGenericInfo(JavaLangReflectExecutable *self) {
  const J2ObjcMethodInfo *metadata = self->metadata_;
  NSString *signatureAttribute = JreMethodGenericString(metadata, self->ptrTable_);
  jboolean isMethod = [self isKindOfClass:[JavaLangReflectMethod class]];
  IOSObjectArray *exceptionTypes = JreParseClassList(
      JrePtrAtIndex(self->ptrTable_, metadata->exceptionsIdx));
  LibcoreReflectGenericSignatureParser *parser =
      AUTORELEASE([[LibcoreReflectGenericSignatureParser alloc]
        initWithJavaLangClassLoader:JavaLangClassLoader_getSystemClassLoader()]);
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
  return AUTORELEASE([[GenericInfo alloc] init:parser->exceptionTypes_
                                    parameters:parser->parameterTypes_
                                    returnType:parser->returnType_
                                typeParameters:parser->formalTypeParameters_]);
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
