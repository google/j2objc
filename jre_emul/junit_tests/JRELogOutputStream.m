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
//  JRELogOutputStream.m
//  JreEmulation
//

#import "IOSPrimitiveArray.h"
#import "JRELogOutputStream.h"
#import "JRELogPaneView.h"
#import "java/util/Arrays.h"
#import "libcore/util/ArrayUtils.h"

@interface JRELogOutputStream () {
  JRELogPaneView *logPane_;
}
@end

@implementation JRELogOutputStream

- (id)initWithJRELogPane:(JRELogPaneView *)logPane {
  self = [super init];
  if (self) {
    logPane_ = logPane;
  }
  return self;
}

- (void)writeWithInt:(jint)oneByte {
  NSString *str = [NSString stringWithFormat:@"%c", (char) oneByte];
  dispatch_async(dispatch_get_main_queue(), ^{
    [logPane_ printString:str];
  });
}

- (void)writeWithByteArray:(IOSByteArray *)buffer
                   withInt:(jint)offset
                   withInt:(jint)length {
  (void)nil_chk(buffer);
  [LibcoreUtilArrayUtils throwsIfOutOfBoundsWithInt:buffer->size_ withInt:offset withInt:length];
  NSString *str = [[[NSString alloc] initWithBytes:(void *)IOSByteArray_GetRef(buffer, offset)
                                           length:length
                                         encoding:NSUTF8StringEncoding] autorelease];
  dispatch_async(dispatch_get_main_queue(), ^{
    [logPane_ printString:str];
  });
}

@end
