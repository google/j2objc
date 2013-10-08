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

@implementation JavaClassMetadata

@synthesize typeName;
@synthesize packageName;
@synthesize modifiers;

- (id)initWithMetadata:(J2ObjcClassInfo *)metadata {
  if (self = [super init]) {
    if (metadata) {
      NSStringEncoding defaultEncoding = [NSString defaultCStringEncoding];
      typeName = [[NSString alloc] initWithCString:metadata->typeName encoding:defaultEncoding];
      packageName = [[NSString alloc] initWithCString:metadata->packageName encoding:defaultEncoding];
      modifiers = metadata->modifiers;
    }
  }
  return self;
}

- (NSString *)qualifiedName {
  return packageName ? [NSString stringWithFormat:@"%@.%@", packageName, typeName] : typeName;
}

- (void)dealloc {
  if (attributes) {
    free(attributes);
  }
#if ! __has_feature(objc_arc)
  [typeName release];
  [packageName release];
  [super dealloc];
#endif
}

@end
