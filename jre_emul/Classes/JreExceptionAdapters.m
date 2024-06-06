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

#import "JreExceptionAdapters.h"

NSErrorDomain JREExceptionErrorDomain = @"JREExceptionErrorDomain";
NSErrorUserInfoKey JREUnderlyingExceptionKey = @"JREUnderlyingExceptionKey";

// Must compile without dependency on transpiled java.lang.Throwable so separately declare the
// selectors we need.
@protocol NSExceptionJreExceptionAdapters

- (NSString *)getLocalizedMessage;

@end

// Although exception argument is nonnull, ensure this function always returns an error.
NSError *JREErrorFromException(NSException *exception) {
  NSString *localizedDescription = @"Unknown exception";
  if ([exception respondsToSelector:@selector(getLocalizedMessage)]) {
    localizedDescription = [(id<NSExceptionJreExceptionAdapters>)exception getLocalizedMessage];
  }

  NSMutableDictionary *userInfo = [NSMutableDictionary dictionary];
  [userInfo setObject:localizedDescription forKey:NSLocalizedDescriptionKey];
  if (exception) {
    [userInfo setObject:exception forKey:JREUnderlyingExceptionKey];
  }

  return [NSError errorWithDomain:JREExceptionErrorDomain
                             code:JREExceptionErrorUnknownException
                         userInfo:userInfo];
}
