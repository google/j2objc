// Copyright 2024 Google Inc. All Rights Reserved.
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

#ifndef _JavaNSObject_H_
#define _JavaNSObject_H_

#import <Foundation/Foundation.h>

#import "JavaObject.h"
#import "NSObject+JavaObject.h"

/// An NSObject subclass that adds conformance to `JavaObject`.
@interface JavaNSObject : NSObject <JavaObject>

@end

#endif  // _JavaNSObject_H_
