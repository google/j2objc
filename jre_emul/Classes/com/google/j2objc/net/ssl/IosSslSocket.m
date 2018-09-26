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

#import "J2ObjC_source.h"
#import "com/google/j2objc/net/ssl/IosSslSocket.h"

@implementation ComGoogleJ2objcNetSslIosSslSocket

// Begin: implementation of JavaNetSSlSocket abstract methods.

- (IOSObjectArray *)getSupportedCipherSuites {
  return [IOSObjectArray arrayWithLength:0 type:NSString_class_()];
}

- (IOSObjectArray *)getEnabledCipherSuites {
  return [IOSObjectArray arrayWithLength:0 type:NSString_class_()];
}

- (void)setEnabledCipherSuitesWithNSStringArray:(IOSObjectArray *)suites {

}

- (IOSObjectArray *)getSupportedProtocols {
  return [IOSObjectArray arrayWithLength:0 type:NSString_class_()];
}

- (IOSObjectArray *)getEnabledProtocols {
  return [IOSObjectArray arrayWithLength:0 type:NSString_class_()];
}

- (void)setEnabledProtocolsWithNSStringArray:(IOSObjectArray *)protocols {

}

- (id<JavaxNetSslSSLSession>)getSession {
  return nil;
}

- (void)addHandshakeCompletedListenerWithJavaxNetSslHandshakeCompletedListener:
    (id<JavaxNetSslHandshakeCompletedListener>)listener {

}

- (void)removeHandshakeCompletedListenerWithJavaxNetSslHandshakeCompletedListener:
    (id<JavaxNetSslHandshakeCompletedListener>)listener {

}

- (void)startHandshake {

}

- (void)setUseClientModeWithBoolean:(jboolean)mode {

}

- (jboolean)getUseClientMode {
  return FALSE;
}

- (void)setNeedClientAuthWithBoolean:(jboolean)need {

}

- (jboolean)getNeedClientAuth {
  return FALSE;
}

- (void)setWantClientAuthWithBoolean:(jboolean)want {

}

- (jboolean)getWantClientAuth {
  return FALSE;
}

- (void)setEnableSessionCreationWithBoolean:(jboolean)flag {

}

- (jboolean)getEnableSessionCreation {
  return FALSE;
}

// End: implementation of JavaNetSSlSocket abstract methods.

@end
