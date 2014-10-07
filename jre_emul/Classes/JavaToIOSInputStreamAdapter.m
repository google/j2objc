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
//  JavaToIOSInputStreamAdapter.m
//  JreEmulation
//
//  Created by Keith Stanger on 6/12/13.
//

#import "JavaToIOSInputStreamAdapter.h"
#import "JreEmulation.h"
#import "java/io/InputStream.h"

@implementation JavaToIOSInputStreamAdapter

- (instancetype)initWithJavaInputStream:(JavaIoInputStream *)javaStream {
  if ((self = [super init])) {
    delegate_ = RETAIN_(javaStream);
  }
  return self;
}

+ (JavaToIOSInputStreamAdapter *)fromJavaInputStream:(JavaIoInputStream *)javaStream {
  return AUTORELEASE([[JavaToIOSInputStreamAdapter alloc] initWithJavaInputStream:javaStream]);
}

- (void)open {
  // java.io.InputStream doesn't have an open() method.
}

- (void)close {
  [delegate_ close];
}

- (NSInteger)read:(uint8_t *)buffer maxLength:(NSUInteger)len {
  IOSByteArray *javaBytes = [IOSByteArray arrayWithLength:(jint)len];
  int result = [delegate_ readWithByteArray:javaBytes withInt:0 withInt:(int) len];
  if (result == -1) {
    return 0;
  }
  [javaBytes getBytes:(jbyte *)buffer offset:0 length:result];
  return result;
}

- (BOOL)getBuffer:(uint8_t **)buffer length:(NSUInteger *)len {
  return NO;
}

- (BOOL)hasBytesAvailable {
  return [delegate_ available];
}

- (void)dealloc {
  [delegate_ close];
#if ! __has_feature(objc_arc)
  [delegate_ release];
  [super dealloc];
#endif
}

@end
