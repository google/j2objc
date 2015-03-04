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

@implementation JavaxAnnotationResource
@synthesize authenticationType;
@synthesize description;
@synthesize mappedName;
@synthesize name;
@synthesize shareable;
@synthesize type;

- (instancetype)initWithAuthenticationType:
    (JavaxAnnotationResource_AuthenticationTypeEnum *)authenticationType_
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

+ (JavaxAnnotationResource_AuthenticationTypeEnum *)authenticationTypeDefault {
  return JavaxAnnotationResource_AuthenticationTypeEnum_get_CONTAINER();
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
  return YES;
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
      initWithValue:JavaLangAnnotationRetentionPolicyEnum_get_RUNTIME()] autorelease],
    [[[JavaLangAnnotationTarget alloc] initWithValue:[IOSObjectArray arrayWithObjects:(id[]) {
      JavaLangAnnotationElementTypeEnum_get_TYPE(),
      JavaLangAnnotationElementTypeEnum_get_METHOD(),
      JavaLangAnnotationElementTypeEnum_get_FIELD()
    } count:3 type:NSObject_class_()]] autorelease]
  } count:2 type:JavaLangAnnotationAnnotation_class_()];
}

+ (const J2ObjcClassInfo *)__metadata {
  static const J2ObjcClassInfo _JavaxAnnotationResource = {
    1, "Resource", "javax.annotation", NULL, 0x2201, 0, NULL, 0, NULL, 0, NULL
  };
  return &_JavaxAnnotationResource;
}

@end

J2OBJC_INITIALIZED_DEFN(JavaxAnnotationResource_AuthenticationTypeEnum)

J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(JavaxAnnotationResource)

JavaxAnnotationResource_AuthenticationTypeEnum *
    JavaxAnnotationResource_AuthenticationTypeEnum_values_[2];

@implementation JavaxAnnotationResource_AuthenticationTypeEnum

- (instancetype)initWithNSString:(NSString *)__name
                         withInt:(jint)__ordinal {
  return [super initWithNSString:__name withInt:__ordinal];
}

FOUNDATION_EXPORT IOSObjectArray *JavaxAnnotationResource_AuthenticationTypeEnum_values() {
  IOSClass *enumType = JavaxAnnotationResource_AuthenticationTypeEnum_class_();
  return [IOSObjectArray arrayWithObjects:JavaxAnnotationResource_AuthenticationTypeEnum_values_
                                    count:2
                                     type:enumType];
}

+ (IOSObjectArray *)values {
  return JavaxAnnotationResource_AuthenticationTypeEnum_values();
}

+ (JavaxAnnotationResource_AuthenticationTypeEnum *)valueOfWithNSString:(NSString *)name {
  return JavaxAnnotationResource_AuthenticationTypeEnum_valueOfWithNSString_(name);
}

JavaxAnnotationResource_AuthenticationTypeEnum *
    JavaxAnnotationResource_AuthenticationTypeEnum_valueOfWithNSString_(NSString *name) {
  for (int i = 0; i < 2; i++) {
    JavaxAnnotationResource_AuthenticationTypeEnum *e =
        JavaxAnnotationResource_AuthenticationTypeEnum_values_[i];
    if ([name isEqual:[e name]]) {
      return e;
    }
  }
  @throw [[[JavaLangIllegalArgumentException alloc] initWithNSString:name] autorelease];
  return nil;
}

- (id)copyWithZone:(NSZone *)zone {
  return [self retain];
}

+ (void)initialize {
  if (self == [JavaxAnnotationResource_AuthenticationTypeEnum class]) {
    JavaxAnnotationResource_AuthenticationTypeEnum_APPLICATION =
        [[JavaxAnnotationResource_AuthenticationTypeEnum alloc]
          initWithNSString:@"APPLICATION" withInt:0];
    JavaxAnnotationResource_AuthenticationTypeEnum_CONTAINER =
        [[JavaxAnnotationResource_AuthenticationTypeEnum alloc]
          initWithNSString:@"CONTAINER" withInt:1];
    J2OBJC_SET_INITIALIZED(JavaxAnnotationResource_AuthenticationTypeEnum)
  }
}

+ (const J2ObjcClassInfo *)__metadata {
  static const J2ObjcMethodInfo methods[] = {
    { "initWithNSString:withInt:", "init", NULL, 0x1, NULL },
  };
  static const J2ObjcFieldInfo fields[] = {
    { "APPLICATION", "APPLICATION", 0x4019, "Ljavax.annotation.Resource$AuthenticationType;",
        &JavaxAnnotationResource_AuthenticationTypeEnum_APPLICATION,  },
    { "CONTAINER", "CONTAINER", 0x4019, "Ljavax.annotation.Resource$AuthenticationType;",
        &JavaxAnnotationResource_AuthenticationTypeEnum_CONTAINER,  },
  };
  static const char *superclass_type_args[] = {"Ljavax.annotation.Resource$AuthenticationType;"};
  static const J2ObjcClassInfo _JavaxAnnotationResource_AuthenticationTypeEnum = {
    1, "AuthenticationType", "javax.annotation", "Resource", 0x4019, 1,
    methods, 2, fields, 1, superclass_type_args};
  return &_JavaxAnnotationResource_AuthenticationTypeEnum;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(JavaxAnnotationResource_AuthenticationTypeEnum)
