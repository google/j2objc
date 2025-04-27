// Copyright 2012 Google Inc. All Rights Reserved.
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
//  IOSArrayClass.h
//  JreEmulation
//
//  Created by Tom Ball on 1/23/12.
//

#ifndef _IOSArrayClass_H_
#define _IOSArrayClass_H_

#import "IOSClass.h"

@interface IOSArrayClass : IOSClass {
  // An IOSClass is used instead of a Class so a IOSPrimitiveClass can be used.
  IOSClass *_Nonnull componentType_;
}

- (instancetype)initWithComponentType:(nonnull IOSClass *)type;

@end

#endif // _IOSArrayClass_H_
