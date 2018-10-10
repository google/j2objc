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

@class JavaNetInetAddress;
@class JavaNetSocket;

// A socket that uses Apple's SecureTransport API.
@interface ComGoogleJ2objcNetSslIosSslSocket : JavaxNetSslSSLSocket

@end

J2OBJC_EMPTY_STATIC_INIT(ComGoogleJ2objcNetSslIosSslSocket)

// public IosSslSocket()
FOUNDATION_EXPORT void ComGoogleJ2objcNetSslIosSslSocket_init(
    ComGoogleJ2objcNetSslIosSslSocket *self);

FOUNDATION_EXPORT ComGoogleJ2objcNetSslIosSslSocket *
new_ComGoogleJ2objcNetSslIosSslSocket_init(void) NS_RETURNS_RETAINED;

FOUNDATION_EXPORT ComGoogleJ2objcNetSslIosSslSocket *
create_ComGoogleJ2objcNetSslIosSslSocket_init(void);

// public IosSslSocket(String host, int port)
FOUNDATION_EXPORT void ComGoogleJ2objcNetSslIosSslSocket_initWithNSString_withInt_(
    ComGoogleJ2objcNetSslIosSslSocket *self, NSString *host, jint port);

FOUNDATION_EXPORT ComGoogleJ2objcNetSslIosSslSocket *
new_ComGoogleJ2objcNetSslIosSslSocket_initWithNSString_withInt_(NSString *host, jint port)
NS_RETURNS_RETAINED;

FOUNDATION_EXPORT ComGoogleJ2objcNetSslIosSslSocket *
create_ComGoogleJ2objcNetSslIosSslSocket_initWithNSString_withInt_(NSString *host, jint port);

// public IosSslSocket(String host, int port, InetAddress localAddr, int localPort)
FOUNDATION_EXPORT void
ComGoogleJ2objcNetSslIosSslSocket_initWithNSString_withInt_withJavaNetInetAddress_withInt_(
    ComGoogleJ2objcNetSslIosSslSocket *self, NSString *host, jint port,
    JavaNetInetAddress *localAddr, jint localPort);

FOUNDATION_EXPORT ComGoogleJ2objcNetSslIosSslSocket *
new_ComGoogleJ2objcNetSslIosSslSocket_initWithNSString_withInt_withJavaNetInetAddress_withInt_(
    NSString *host, jint port, JavaNetInetAddress *localAddr, jint localPort) NS_RETURNS_RETAINED;

FOUNDATION_EXPORT ComGoogleJ2objcNetSslIosSslSocket *
create_ComGoogleJ2objcNetSslIosSslSocket_initWithNSString_withInt_withJavaNetInetAddress_withInt_(
    NSString *host, jint port, JavaNetInetAddress *localAddr, jint localPort);

// public IosSslSocket(InetAddress host, int port)
FOUNDATION_EXPORT void ComGoogleJ2objcNetSslIosSslSocket_initWithJavaNetInetAddress_withInt_(
    ComGoogleJ2objcNetSslIosSslSocket *self, JavaNetInetAddress *address, jint port);

FOUNDATION_EXPORT ComGoogleJ2objcNetSslIosSslSocket *
new_ComGoogleJ2objcNetSslIosSslSocket_initWithJavaNetInetAddress_withInt_(
    JavaNetInetAddress *address, jint port) NS_RETURNS_RETAINED;

FOUNDATION_EXPORT ComGoogleJ2objcNetSslIosSslSocket *
create_ComGoogleJ2objcNetSslIosSslSocket_initWithJavaNetInetAddress_withInt_(
    JavaNetInetAddress *address, jint port);

// public IosSslSocket(InetAddress address, int port, InetAddress localAddr, int localPort)
// NOLINTNEXTLINE
FOUNDATION_EXPORT void ComGoogleJ2objcNetSslIosSslSocket_initWithJavaNetInetAddress_withInt_withJavaNetInetAddress_withInt_(
    ComGoogleJ2objcNetSslIosSslSocket *self, JavaNetInetAddress *address, jint port,
    JavaNetInetAddress *localAddr, jint localPort);

// NOLINTNEXTLINE
FOUNDATION_EXPORT ComGoogleJ2objcNetSslIosSslSocket *new_ComGoogleJ2objcNetSslIosSslSocket_initWithJavaNetInetAddress_withInt_withJavaNetInetAddress_withInt_(
    JavaNetInetAddress *address, jint port, JavaNetInetAddress *localAddr, jint localPort)
NS_RETURNS_RETAINED;

// NOLINTNEXTLINE
FOUNDATION_EXPORT ComGoogleJ2objcNetSslIosSslSocket *create_ComGoogleJ2objcNetSslIosSslSocket_initWithJavaNetInetAddress_withInt_withJavaNetInetAddress_withInt_(
    JavaNetInetAddress *address, jint port, JavaNetInetAddress *localAddr, jint localPort);

// public IosSslSocket(Socket s, String host, int port, boolean autoClose)
FOUNDATION_EXPORT ComGoogleJ2objcNetSslIosSslSocket *
new_ComGoogleJ2objcNetSslIosSslSocket_initWithJavaNetSocket_withNSString_withInt_withBoolean_(
    JavaNetSocket *socket, NSString *host, jint port, jboolean autoClose) NS_RETURNS_RETAINED;

FOUNDATION_EXPORT ComGoogleJ2objcNetSslIosSslSocket *
create_ComGoogleJ2objcNetSslIosSslSocket_initWithJavaNetSocket_withNSString_withInt_withBoolean_(
    JavaNetSocket *socket, NSString *host, jint port, jboolean autoClose);

J2OBJC_TYPE_LITERAL_HEADER(ComGoogleJ2objcNetSslIosSslSocket)

#endif
