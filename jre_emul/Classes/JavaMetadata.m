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
@synthesize enclosingName;
@synthesize modifiers;

- (id)initWithMetadata:(J2ObjcClassInfo *)metadata {
  if (self = [super init]) {
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
    modifiers = metadata->modifiers;
  }
  return self;
}

- (NSString *)qualifiedName {
  NSMutableString *qName = [NSMutableString string];
  if (packageName) {
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
