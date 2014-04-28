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
//  IOSClassTest.m
//  JreEmulation
//
//  Created by Tom Ball on 6/19/13.
//
//

#import "JreEmulation.h"

#import "IOSClass.h"
#import "IOSPrimitiveArray.h"
#import "IOSObjectArray.h"
#import "java/lang/Double.h"
#import "java/util/Arrays.h"

#import <SenTestingKit/SenTestingKit.h>

// Unit tests for IOSClass.
@interface IOSClassTest : SenTestCase
@end


@implementation IOSClassTest

- (void)testCheckDoubleParameterNaming {
  IOSClass *arraysClass = [JavaUtilArrays getClass];
  IOSObjectArray *argTypes =
      [IOSObjectArray arrayWithObjects:
          (id[]){ [IOSDoubleArray iosClass], JavaLangDouble_get_TYPE_() }
                                 count:2
                                  type:[IOSClass getClass]];
  id method = [arraysClass getMethod:@"binarySearch" parameterTypes:argTypes];
  STAssertNotNil(method, @"Arrays.binarySearch(double[], double) not found");
}

@end
