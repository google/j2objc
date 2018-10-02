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

#ifndef _ComGoogleJ2objcNetSslIosSslSocket_H_
#define _ComGoogleJ2objcNetSslIosSslSocket_H_

#import "J2ObjC_header.h"
#import "javax/net/ssl/SSLSocket.h"

// A socket that uses Apple's SecureTransport API.
@interface ComGoogleJ2objcNetSslIosSslSocket : JavaxNetSslSSLSocket

@end

J2OBJC_EMPTY_STATIC_INIT(ComGoogleJ2objcNetSslIosSslSocket)

FOUNDATION_EXPORT void ComGoogleJ2objcNetSslIosSslSocket_initWithNSString_withInt_(
    ComGoogleJ2objcNetSslIosSslSocket *self, NSString *host, jint port);

FOUNDATION_EXPORT ComGoogleJ2objcNetSslIosSslSocket *
    new_ComGoogleJ2objcNetSslIosSslSocket_initWithNSString_withInt_(NSString *host, jint port)
    NS_RETURNS_RETAINED;

FOUNDATION_EXPORT ComGoogleJ2objcNetSslIosSslSocket *
    create_ComGoogleJ2objcNetSslIosSslSocket_initWithNSString_withInt_(NSString *host, jint port);

J2OBJC_TYPE_LITERAL_HEADER(ComGoogleJ2objcNetSslIosSslSocket)

#endif
