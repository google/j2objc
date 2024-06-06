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

#ifndef _JreExceptionAdapters_H_
#define _JreExceptionAdapters_H_

#import <Foundation/Foundation.h>

//
// Exceptions converted to errors appear in this error domain and codes.
//
extern NSErrorDomain _Nonnull JREExceptionErrorDomain;

typedef NS_ERROR_ENUM(JREExceptionErrorDomain, JREExceptionError){
    JREExceptionErrorUnknownException = 0,
};

// Key the original exception appears under in the generated error.
extern NSErrorUserInfoKey _Nonnull JREUnderlyingExceptionKey;

//
// Return an NSError that represents a caught NSException (most likely a JavaLangThrowable).
//
extern NSError* _Nonnull JREErrorFromException(NSException* _Nonnull exception);

#endif  // _JreExceptionAdapters_H_
