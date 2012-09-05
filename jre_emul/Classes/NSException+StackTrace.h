// Copyright 2011 Google Inc. All Rights Reserved.
//
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
//  NSException+StackTrace.h
//  JreEmulation
//
//  Created by Tom Ball on 11/5/11.
//

#import <Foundation/Foundation.h>

@interface NSException (StackTrace)

// Print the exception stack using atos.  This utility won't be available if
// running on an iOS system, or on a Mac OS system which doesn't have the
// developer tools installed.  If not available, this falls back to
// printCallStackSymbols.
- (void)printStackTrace;

// Print the stack trace using NSException callStackSymbols.  This has the
// advantage of being available on most systems, but doesn't print file names 
// or numbers with debug builds.
- (void)printCallStackSymbols;

@end
