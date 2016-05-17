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
//  IOSMappedClass.m
//  JreEmulation
//
//  Created by Tom Ball on 12/10/13.
//

#import "IOSMappedClass.h"
#import "IOSObjectArray.h"
#import "IOSReflection.h"
#import "java/lang/Package.h"
#import "java/lang/reflect/Method.h"
#import "java/lang/reflect/Modifier.h"

// Class representation for a mapped class, such as NSObject or NSString.
// All reflection information is determined by metadata, to avoid returning
// Objective-C specific methods or classes.
// TODO(kstanger): This class can probably be removed now that metadata is
// required for all classes.
@implementation IOSMappedClass

- (instancetype)initWithClass:(Class)cls package:(NSString *)package name:(NSString *)name {
  if ((self = [super initWithClass:cls])) {
    package_ = RETAIN_(package);
    name_ = RETAIN_(name);
  }
  return self;
}

- (NSString *)getName {
  return [NSString stringWithFormat:@"%@.%@", package_, name_];
}

- (NSString *)getSimpleName {
  return name_;
}

- (NSString *)objcName {
  return NSStringFromClass(class_);
}

- (id)getPackage {
  return AUTORELEASE([[JavaLangPackage alloc] initWithNSString:package_
                                                  withNSString:nil
                                                  withNSString:nil
                                                  withNSString:nil
                                                  withNSString:nil
                                                  withNSString:nil
                                                  withNSString:nil
                                                withJavaNetURL:nil
                                       withJavaLangClassLoader:nil]);
}

- (jboolean)isEnum {
  return false;
}

- (jboolean)isAnonymousClass {
  return false;
}

@end
