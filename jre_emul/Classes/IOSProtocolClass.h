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
//  IOSProtocolClass.h
//  JreEmulation
//
//  Created by Keith Stanger on 8/16/13.
//

#ifndef _IOSProtocolClass_H_
#define _IOSProtocolClass_H_

#import "IOSClass.h"

@interface IOSProtocolClass : IOSClass {
  Protocol *protocol_;
}

- (id)initWithProtocol:(Protocol *)protocol;

@end

#endif // _IOSProtocolClass_H_
