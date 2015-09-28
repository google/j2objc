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
//  JRELogPane.m
//  JreEmulation
//

#import "JRELogPaneView.h"
#import "JreEmulation.h"

@implementation JRELogPaneView

- (id)initWithFrame:(CGRect)frame {
  self = [super initWithFrame:frame];
  if (self) {
    CGFloat fontSize =
        [UIDevice currentDevice].userInterfaceIdiom == UIUserInterfaceIdiomPad ? 14.0 : 9.0;
    self.font = [UIFont fontWithName:@"Courier" size:fontSize];
    self.editable = NO;
    self.autoresizingMask =
        UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleBottomMargin;
  }
  return self;
}

- (void)printByte:(char)b {
  [self updateText:[NSString stringWithFormat:@"%@%c", self.text, b]];
}

- (void)printBytes:(const char  *)bytes length:(NSUInteger)n {
  NSString *s = [[NSString alloc] initWithBytesNoCopy:(void *)bytes
                                               length:n
                                             encoding:NSUTF8StringEncoding
                                         freeWhenDone:NO];
  [self updateText:[self.text stringByAppendingString:s]];
  RELEASE_(s);
}

- (void)updateText:(NSString *)newText {
  [self setText:newText];
  [self.delegate textViewDidChange:self];
  [self scrollRangeToVisible:NSMakeRange([self.text length], 0)];
}

@end
