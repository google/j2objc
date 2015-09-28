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
//  NSDataInputStreamTest.m
//  JreEmulation
//

#import <XCTest/XCTest.h>
#import "IOSPrimitiveArray.h"
#import "NSDataInputStream.h"

// Unit tests for NSDataInputStream.
@interface NSDataInputStreamTest : XCTestCase
@end

@implementation NSDataInputStreamTest

- (void)testAvailable {
  NSMutableData *data = [NSMutableData data];
  NSDataInputStream *inputStream = [NSDataInputStream streamWithData:data];
  XCTAssertEqual([inputStream available], 0, @"incorrect availability");

  [data appendBytes:(jbyte[]){ 1, 2, 3 } length:3];
  XCTAssertEqual([inputStream available], 3, @"incorrect availability");
  [data appendBytes:(jbyte[]){ 4, 5, 6, 7, 8 } length:5];
  XCTAssertEqual([inputStream available], 8, @"incorrect availability");
}

- (void)testRead {
  NSMutableData *data = [NSMutableData data];
  NSDataInputStream *inputStream = [NSDataInputStream streamWithData:data];
  [data appendBytes:(jbyte[]){ 1, 2, 3, 0xc7 } length:4];
  XCTAssertEqual([inputStream read], 1, @"incorrect read");
  XCTAssertEqual([inputStream read], 2, @"incorrect read");
  XCTAssertEqual([inputStream read], 3, @"incorrect read");
  XCTAssertEqual([inputStream read], 199, @"incorrect read");
  XCTAssertEqual([inputStream read], -1, @"end of stream not returned");
}

- (void)testReadBytes {
  NSMutableData *data = [NSMutableData data];
  NSDataInputStream *inputStream = [NSDataInputStream streamWithData:data];
  [data appendBytes:(jbyte[]){ 1, 2, 3 } length:3];
  IOSByteArray *array = [IOSByteArray arrayWithLength:3];
  jint n = [inputStream readWithByteArray:array];
  XCTAssertEqual(n, 3, @"incorrect number of bytes returned");
  XCTAssertEqual(*array->buffer_, 1, @"incorrect result");
  XCTAssertEqual(*array->buffer_ + 1, 2, @"incorrect result");
  XCTAssertEqual(*array->buffer_ + 2, 3, @"incorrect result");
  n = [inputStream readWithByteArray:array];
  XCTAssertEqual(n, -1, @"end of stream not returned");
}

- (void)testClose {
  NSMutableData *data = [NSMutableData data];
  NSDataInputStream *inputStream = [NSDataInputStream streamWithData:data];
  [data appendBytes:(jbyte[]){ 1, 2, 3 } length:3];
  XCTAssertEqual([inputStream available], 3, @"incorrect availability");
  [inputStream close];
  XCTAssertEqual([inputStream available], 0, @"stream not closed");
}

@end
