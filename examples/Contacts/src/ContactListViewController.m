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

#import "ContactListViewController.h"

#import "AddContactViewController.h"
#import "ContactDetailViewController.h"
#include "java/util/List.h"
#include "src/java/org/contacts/Contact.h"
#include "src/java/org/contacts/Store.h"

@interface ContactListViewController () <UITableViewDelegate, AddContactViewDelegate>

@end

@interface ContactListDataSource : NSObject <UITableViewDataSource>

- (instancetype)initWithStore:(OrgContactsStore *)store;

- (OrgContactsContact *)getContactAtRow:(NSUInteger)row;

- (void)reloadData;

@end

@implementation ContactListViewController {
  OrgContactsStore *_store;
  ContactListDataSource *_dataSource;
  UITableView *_tableView;
}

- (instancetype)initWithContactStore:(OrgContactsStore *)store {
  if (self = [super init]) {
    _store = store;
    _dataSource = [[ContactListDataSource alloc] initWithStore:store];

    _tableView = [[UITableView alloc] init];
    _tableView.delegate = self;
    _tableView.dataSource = _dataSource;
    _tableView.separatorStyle = UITableViewCellSeparatorStyleSingleLine;
  }
  return self;
}

- (void)viewDidLoad {
  [super viewDidLoad];
  self.title = @"Contacts";
  self.view.backgroundColor = [UIColor whiteColor];
  [self.view addSubview:_tableView];
  UIBarButtonItem *rightBarButtontem =
      [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd
                                                    target:self
                                                    action:@selector(addButtonTapped)];
  self.navigationController.navigationBar.topItem.rightBarButtonItem = rightBarButtontem;
}

- (void)viewWillLayoutSubviews {
  [_tableView setFrame:self.view.bounds];
}

#pragma mark - Private

- (void)addButtonTapped {
  AddContactViewController *addContactViewController =
      [[AddContactViewController alloc] initWithDelegate:self];
  UINavigationController *navigationController =
      [[UINavigationController alloc] initWithRootViewController:addContactViewController];
  [self presentViewController:navigationController animated:YES completion:nil];
}

#pragma mark - UITableViewDelegate

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
  return 44.0f;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
  OrgContactsContact *contact = [_dataSource getContactAtRow:indexPath.row];
  ContactDetailViewController *detailViewController =
      [[ContactDetailViewController alloc] initWithContact:contact];
  [self.navigationController pushViewController:detailViewController animated:YES];
  [tableView deselectRowAtIndexPath:indexPath animated:YES];
}

#pragma mark - AddContactViewDelegate

- (void)saveNewContact:(OrgContactsContact *)contact {
  [_store addContactWithOrgContactsContact:contact];
  [_dataSource reloadData];
  [_tableView reloadData];
}

@end

@implementation ContactListDataSource {
  OrgContactsStore *_store;
  id<JavaUtilList> _contacts;
}

- (instancetype)initWithStore:(OrgContactsStore *)store {
  if (self = [super init]) {
    _store = store;
    [self reloadData];
  }
  return self;
}

- (void)reloadData {
  _contacts = [_store getOrderedContacts];
}

- (OrgContactsContact *)getContactAtRow:(NSUInteger)row {
  return [_contacts getWithInt:(jint)row];
}

- (UITableViewCell *)tableView:(UITableView *)tableView
         cellForRowAtIndexPath:(NSIndexPath *)indexPath {
  UITableViewCell *cell = [[UITableViewCell alloc] init];
  OrgContactsContact *contact = [self getContactAtRow:indexPath.row];
  cell.textLabel.text = [contact getName];
  return cell;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
  return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
  return [_contacts size];
}

@end
