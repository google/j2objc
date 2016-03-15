/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "javax/annotation/Resource.h"

#include "J2ObjC_source.h"
#include "java/lang/IllegalArgumentException.h"
#include "java/lang/annotation/Annotation.h"
#include "java/lang/annotation/ElementType.h"
#include "java/lang/annotation/Retention.h"
#include "java/lang/annotation/RetentionPolicy.h"
#include "java/lang/annotation/Target.h"

static void JavaxAnnotationResource_AuthenticationType_initWithNSString_withInt_(
    JavaxAnnotationResource_AuthenticationType *self, NSString *__name, jint __ordinal);

@implementation JavaxAnnotationResource
@synthesize authenticationType;
@synthesize description;
@synthesize mappedName;
@synthesize name;
@synthesize shareable;
@synthesize type;

- (instancetype)initWithAuthenticationType:
    (JavaxAnnotationResource_AuthenticationType *)authenticationType_
                           withDescription:(NSString *)description_
                            withMappedName:(NSString *)mappedName_
                                  withName:(NSString *)name_
                             withShareable:(jboolean)shareable_
                                  withType:(IOSClass *)type_ {
  if ((self = [super init])) {
    authenticationType = RETAIN_(authenticationType_);
    description = RETAIN_(description_);
    mappedName = RETAIN_(mappedName_);
    name = RETAIN_(name_);
    shareable = shareable_;
    type = RETAIN_(type_);
  }
  return self;
}

+ (JavaxAnnotationResource_AuthenticationType *)authenticationTypeDefault {
  return JavaxAnnotationResource_AuthenticationType_get_CONTAINER();
}

+ (NSString *)descriptionDefault {
  return @"";
}

+ (NSString *)mappedNameDefault {
  return @"";
}

+ (NSString *)nameDefault {
  return @"";
}

+ (jboolean)shareableDefault {
  return true;
}

+ (IOSClass *)typeDefault {
  return NSObject_class_();
}

- (IOSClass *)annotationType {
  return JavaxAnnotationResource_class_();
}

+ (IOSObjectArray *)__annotations {
  return [IOSObjectArray arrayWithObjects:(id[]) {
    [[[JavaLangAnnotationRetention alloc]
      initWithValue:JavaLangAnnotationRetentionPolicy_get_RUNTIME()] autorelease],
    [[[JavaLangAnnotationTarget alloc] initWithValue:[IOSObjectArray arrayWithObjects:(id[]) {
      JavaLangAnnotationElementType_get_TYPE(),
      JavaLangAnnotationElementType_get_METHOD(),
      JavaLangAnnotationElementType_get_FIELD()
    } count:3 type:NSObject_class_()]] autorelease]
  } count:2 type:JavaLangAnnotationAnnotation_class_()];
}

+ (const J2ObjcClassInfo *)__metadata {
  static const J2ObjcMethodInfo methods[] = {
    { "authenticationType", "authenticationType", "Ljavax.annotation.Resource$AuthenticationType;",
      0x401, NULL, NULL },
    { "authenticationTypeDefault", "authenticationType",
      "Ljavax.annotation.Resource$AuthenticationType;", 0x100a, NULL, NULL },
    { "description", "description", "Ljava.lang.String;", 0x401, NULL, NULL },
    { "descriptionDefault", "description", "Ljava.lang.String;", 0x100a, NULL, NULL },
    { "mappedName", "mappedName", "Ljava.lang.String;", 0x401, NULL, NULL },
    { "mappedNameDefault", "mappedName", "Ljava.lang.String;", 0x100a, NULL, NULL },
    { "name", "name", "Ljava.lang.String;", 0x401, NULL, NULL },
    { "nameDefault", "name", "Ljava.lang.String;", 0x100a, NULL, NULL },
    { "shareable", "shareable", "Z", 0x401, NULL, NULL },
    { "shareableDefault", "shareable", "Z", 0x100a, NULL, NULL },
    { "type", "type", "Ljava.lang.Class;", 0x401, NULL, NULL },
    { "typeDefault", "type", "Ljava.lang.Class;", 0x100a, NULL, NULL },
  };
  static const char *inner_classes[] = {"Ljavax.annotation.Resource$AuthenticationType;"};
  static const J2ObjcClassInfo _JavaxAnnotationResource = {
    2, "Resource", "javax.annotation", NULL, 0x2609, 12, methods, 0, NULL, 0, NULL,
    1, inner_classes, NULL, NULL
  };
  return &_JavaxAnnotationResource;
}

@end

J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(JavaxAnnotationResource)

J2OBJC_INITIALIZED_DEFN(JavaxAnnotationResource_AuthenticationType)

JavaxAnnotationResource_AuthenticationType *JavaxAnnotationResource_AuthenticationType_values_[2];

@implementation JavaxAnnotationResource_AuthenticationType

+ (IOSObjectArray *)values {
  return JavaxAnnotationResource_AuthenticationType_values();
}

+ (JavaxAnnotationResource_AuthenticationType *)valueOfWithNSString:(NSString *)name {
  return JavaxAnnotationResource_AuthenticationType_valueOfWithNSString_(name);
}

- (id)copyWithZone:(NSZone *)zone {
  return self;
}

+ (void)initialize {
  if (self == [JavaxAnnotationResource_AuthenticationType class]) {
    size_t objSize = class_getInstanceSize(self);
    size_t allocSize = 2 * objSize;
    uintptr_t ptr = (uintptr_t)calloc(allocSize, 1);
    id e;
    e = objc_constructInstance(self, (void *)ptr);
    JreEnum(JavaxAnnotationResource_AuthenticationType, APPLICATION) = e;
    ptr += objSize;
    JavaxAnnotationResource_AuthenticationType_initWithNSString_withInt_(e, @"APPLICATION", 0);
    e = objc_constructInstance(self, (void *)ptr);
    JreEnum(JavaxAnnotationResource_AuthenticationType, CONTAINER) = e;
    ptr += objSize;
    JavaxAnnotationResource_AuthenticationType_initWithNSString_withInt_(e, @"CONTAINER", 1);
    J2OBJC_SET_INITIALIZED(JavaxAnnotationResource_AuthenticationType)
  }
}

+ (const J2ObjcClassInfo *)__metadata {
  static const J2ObjcFieldInfo fields[] = {
    { "APPLICATION", "APPLICATION", 0x4019, "Ljavax.annotation.Resource$AuthenticationType;",
      &JreEnum(JavaxAnnotationResource_AuthenticationType, APPLICATION), NULL,
      .constantValue.asLong = 0 },
    { "CONTAINER", "CONTAINER", 0x4019, "Ljavax.annotation.Resource$AuthenticationType;",
      &JreEnum(JavaxAnnotationResource_AuthenticationType, CONTAINER), NULL,
      .constantValue.asLong = 0 },
  };
  static const char *superclass_type_args[] = {"Ljavax.annotation.Resource$AuthenticationType;"};
  static const J2ObjcClassInfo _JavaxAnnotationResource_AuthenticationType = {
    2, "AuthenticationType", "javax.annotation", "Resource", 0x4019, 0, NULL, 2, fields, 1,
    superclass_type_args, 0, NULL, NULL,
    "Ljava/lang/Enum<Ljavax/annotation/Resource$AuthenticationType;>;" };
  return &_JavaxAnnotationResource_AuthenticationType;
}

@end

void JavaxAnnotationResource_AuthenticationType_initWithNSString_withInt_(
    JavaxAnnotationResource_AuthenticationType *self, NSString *__name, jint __ordinal) {
  JavaLangEnum_initWithNSString_withInt_(self, __name, __ordinal);
}

IOSObjectArray *JavaxAnnotationResource_AuthenticationType_values() {
  JavaxAnnotationResource_AuthenticationType_initialize();
  return [IOSObjectArray arrayWithObjects:JavaxAnnotationResource_AuthenticationType_values_
                                    count:2
                                     type:JavaxAnnotationResource_AuthenticationType_class_()];
}

JavaxAnnotationResource_AuthenticationType *
    JavaxAnnotationResource_AuthenticationType_valueOfWithNSString_(NSString *name) {
  JavaxAnnotationResource_AuthenticationType_initialize();
  for (int i = 0; i < 2; i++) {
    JavaxAnnotationResource_AuthenticationType *e =
        JavaxAnnotationResource_AuthenticationType_values_[i];
    if ([name isEqual:[e name]]) {
      return e;
    }
  }
  @throw [[[JavaLangIllegalArgumentException alloc] initWithNSString:name] autorelease];
  return nil;
}

JavaxAnnotationResource_AuthenticationType *JavaxAnnotationResource_AuthenticationType_fromOrdinal(
    NSUInteger ordinal) {
  JavaxAnnotationResource_AuthenticationType_initialize();
  if (ordinal >= 2) {
    return nil;
  }
  return JavaxAnnotationResource_AuthenticationType_values_[ordinal];
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(JavaxAnnotationResource_AuthenticationType)
