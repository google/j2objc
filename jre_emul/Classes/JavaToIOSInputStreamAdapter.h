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
//  JavaToIOSInputStreamAdapter.h
//  JreEmulation
//
//  Created by Keith Stanger on 6/12/13.
//

#ifndef _JavaToIOSInputStreamAdapter_H_
#define _JavaToIOSInputStreamAdapter_H_

#import <Foundation/Foundation.h>

@class JavaIoInputStream;

@interface JavaToIOSInputStreamAdapter : NSInputStream {
 @private
  JavaIoInputStream *delegate_;
}

- (id)initWithJavaInputStream:(JavaIoInputStream *)javaStream;

+ (JavaToIOSInputStreamAdapter *)fromJavaInputStream:(JavaIoInputStream *)javaStream;

@end

#endif // _JavaToIOSInputStreamAdapter_H_
