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

#import "AddContactViewController.h"

#include "src/java/org/contacts/Contact.h"

@interface AddContactView : UIView

- (NSString *)getName;

- (NSString *)getNumber;

@end

@implementation AddContactViewController {
  __weak id<AddContactViewDelegate> _delegate;
  AddContactView *_view;
}

- (instancetype)initWithDelegate:(id<AddContactViewDelegate>)delegate {
  if (self = [super init]) {
    self.title = @"Add Contact";
    _delegate = delegate;
    _view = [[AddContactView alloc] init];
  }
  return self;
}

- (void)viewDidLoad {
  [super viewDidLoad];
  [self.view addSubview:_view];

  UINavigationBar *navigationBar = self.navigationController.navigationBar;
  navigationBar.translucent = NO;
  UIBarButtonItem *cancelButtonItem =
      [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemCancel
                                                    target:self
                                                    action:@selector(dismissModal)];
  UIBarButtonItem *saveButtonItem =
      [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemSave
                                                    target:self
                                                    action:@selector(save)];
  navigationBar.topItem.leftBarButtonItem = cancelButtonItem;
  navigationBar.topItem.rightBarButtonItem = saveButtonItem;
}

- (void)viewWillLayoutSubviews {
  [_view setFrame:self.view.bounds];
}

- (void)dismissModal {
  [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)save {
  [_delegate saveNewContact:
      create_OrgContactsContact_initWithNSString_withNSString_([_view getName], [_view getNumber])];
  [self dismissModal];
}

@end

#define ELEMENT_HEIGHT 40

#define NAME_TITLE_TOP 0
#define NAME_TITLE_HEIGHT ELEMENT_HEIGHT
#define NAME_TITLE_BOTTOM (NAME_TITLE_TOP + NAME_TITLE_HEIGHT)

#define NAME_FIELD_TOP NAME_TITLE_BOTTOM
#define NAME_FIELD_HEIGHT ELEMENT_HEIGHT
#define NAME_FIELD_BOTTOM (NAME_FIELD_TOP + NAME_FIELD_HEIGHT)

#define NUMBER_TITLE_TOP NAME_FIELD_BOTTOM
#define NUMBER_TITLE_HEIGHT ELEMENT_HEIGHT
#define NUMBER_TITLE_BOTTOM (NUMBER_TITLE_TOP + NUMBER_TITLE_HEIGHT)

#define NUMBER_FIELD_TOP NUMBER_TITLE_BOTTOM
#define NUMBER_FIELD_HEIGHT ELEMENT_HEIGHT

@implementation AddContactView {
  UILabel *_nameTitle;
  UITextField *_nameField;
  UILabel *_numberTitle;
  UITextField *_numberField;
}

- (instancetype)init {
  if (self = [super init]) {
    self.backgroundColor = [UIColor whiteColor];
    [self createSubviews];
  }
  return self;
}

- (void)createSubviews {
  _nameTitle = [[UILabel alloc] initWithFrame:CGRectZero];
  _nameTitle.text = @"Name:";
  [self addSubview:_nameTitle];

  _nameField = [[UITextField alloc] initWithFrame:CGRectZero];
  _nameField.placeholder = @"Contact Name";
  _nameField.borderStyle = UITextBorderStyleRoundedRect;
  [self addSubview:_nameField];

  _numberTitle = [[UILabel alloc] initWithFrame:CGRectZero];
  _numberTitle.text = @"Phone Number:";
  [self addSubview:_numberTitle];

  _numberField = [[UITextField alloc] initWithFrame:CGRectZero];
  _numberField.placeholder = @"Phone Number";
  _numberField.borderStyle = UITextBorderStyleRoundedRect;
  [self addSubview:_numberField];
}

- (void)layoutSubviews {
  CGFloat width = self.bounds.size.width;
  [_nameTitle setFrame:CGRectMake(0, NAME_TITLE_TOP, width, NAME_TITLE_HEIGHT)];
  [_nameField setFrame:CGRectMake(0, NAME_FIELD_TOP, width, NAME_FIELD_HEIGHT)];
  [_numberTitle setFrame:CGRectMake(0, NUMBER_TITLE_TOP, width, NUMBER_TITLE_HEIGHT)];
  [_numberField setFrame:CGRectMake(0, NUMBER_FIELD_TOP, width, NUMBER_FIELD_HEIGHT)];
}

- (NSString *)getName {
  return _nameField.text;
}

- (NSString *)getNumber {
  return _numberField.text;
}

@end
