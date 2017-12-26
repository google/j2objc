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

#import "ContactDetailViewController.h"

#include "src/java/org/contacts/Contact.h"

@interface ContactDetailView : UIView

- (instancetype)initWithContact:(OrgContactsContact *)contact;

@end

@implementation ContactDetailViewController {
  ContactDetailView *_view;
}

- (instancetype)initWithContact:(OrgContactsContact *)contact {
  if (self = [super init]) {
    self.title = @"Contact Detail";
    _view = [[ContactDetailView alloc] initWithContact:contact];
  }
  return self;
}

- (void)viewDidLoad {
  [super viewDidLoad];
  [self.view addSubview:_view];
}

- (void)viewWillLayoutSubviews {
  [_view setFrame:self.view.bounds];
}

@end

#define ELEMENT_HEIGHT 40

#define NAME_TITLE_TOP 0
#define NAME_TITLE_HEIGHT ELEMENT_HEIGHT
#define NAME_TITLE_BOTTOM (NAME_TITLE_TOP + NAME_TITLE_HEIGHT)

#define NAME_DETAIL_TOP NAME_TITLE_BOTTOM
#define NAME_DETAIL_HEIGHT ELEMENT_HEIGHT
#define NAME_DETAIL_BOTTOM (NAME_DETAIL_TOP + NAME_DETAIL_HEIGHT)

#define NUMBER_TITLE_TOP NAME_DETAIL_BOTTOM
#define NUMBER_TITLE_HEIGHT ELEMENT_HEIGHT
#define NUMBER_TITLE_BOTTOM (NUMBER_TITLE_TOP + NUMBER_TITLE_HEIGHT)

#define NUMBER_DETAIL_TOP NUMBER_TITLE_BOTTOM
#define NUMBER_DETAIL_HEIGHT ELEMENT_HEIGHT

@implementation ContactDetailView {
  UILabel *_nameTitle;
  UILabel *_nameDetail;
  UILabel *_numberTitle;
  UILabel *_numberDetail;
}

- (instancetype)initWithContact:(OrgContactsContact *)contact {
  if (self = [super init]) {
    self.backgroundColor = [UIColor whiteColor];
    [self createSubviews:contact];
  }
  return self;
}

- (void)createSubviews:(OrgContactsContact *)contact {
  _nameTitle = [[UILabel alloc] initWithFrame:CGRectZero];
  _nameTitle.text = @"Name:";
  [self addSubview:_nameTitle];

  _nameDetail = [[UILabel alloc] initWithFrame:CGRectZero];
  _nameDetail.text = [contact getName];
  [self addSubview:_nameDetail];

  _numberTitle = [[UILabel alloc] initWithFrame:CGRectZero];
  _numberTitle.text = @"Phone Number:";
  [self addSubview:_numberTitle];

  _numberDetail = [[UILabel alloc] initWithFrame:CGRectZero];
  _numberDetail.text = [contact getNumber];
  [self addSubview:_numberDetail];
}

- (void)layoutSubviews {
  CGFloat width = self.bounds.size.width;
  [_nameTitle setFrame:CGRectMake(0, NAME_TITLE_TOP, width, NAME_TITLE_HEIGHT)];
  [_nameDetail setFrame:CGRectMake(0, NAME_DETAIL_TOP, width, NAME_DETAIL_HEIGHT)];
  [_numberTitle setFrame:CGRectMake(0, NUMBER_TITLE_TOP, width, NUMBER_TITLE_HEIGHT)];
  [_numberDetail setFrame:CGRectMake(0, NUMBER_DETAIL_TOP, width, NUMBER_DETAIL_HEIGHT)];
}

@end
