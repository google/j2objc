// Copyright 2013 Google Inc. All Rights Reserved.
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
//  TimeZones.mm
//  Objective-C++ native methods for libcore.icu.TimeZones, created from
//  Android's libcore_icu_TimeZones.cpp.
//
//  Created by Tom Ball on 2/6/2013.
//

#import "TimeZonesSupport.h"
#import "java/util/Locale.h"
#import "unicode/ucal.h"
#import "unicode/udat.h"
#import "unicode/ustring.h"

static void setStringArrayElement(IOSObjectArray *array, NSUInteger i,
                                  const char *s) {
  NSString *nsstring = [NSString stringWithUTF8String:s];
  [array replaceObjectAtIndex:i withObject:nsstring];
}

// Returns a ICU string from a C string. The result is freed by the caller.
static UChar *unistring(const char* s) {
  UChar *result  = (UChar *)malloc(sizeof(UChar) * strlen(s) + 1);
  u_uastrcpy(result, s);
  return result;
}

@implementation TimeZonesSupport

+ (IOSObjectArray *)forCountryCode:(NSString *)countryCode {
  if (!countryCode) {
    return nil;
  }
  UErrorCode status = U_ZERO_ERROR;
  UEnumeration *ids =
      ucal_openCountryTimeZones([countryCode UTF8String], &status);
  if (!ids) {
    return nil;
  }
  IOSObjectArray *result = nil;
  int32_t idCount = uenum_count(ids, &status);
  if (U_SUCCESS(status)) {
    IOSClass *stringClass = [IOSClass classWithClass:[NSString class]];
    IOSObjectArray *result =
        [IOSObjectArray arrayWithLength:idCount
                                   type:stringClass];
    for (NSUInteger i = 0; i < idCount; ++i) {
      int32_t len;
      const char* id_ = uenum_next(ids, &len, &status);
      if (U_FAILURE(status)) {
        break;
      }
      setStringArrayElement(result, i, id_);
    }
  }
  uenum_close(ids);
  return result;
}

@end
