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

#import "com/google/j2objc/net/ssl/IosSslSocket.h"

#import <Security/Security.h>

#import "J2ObjC_source.h"
#import "com/google/j2objc/security/IosSecurityProvider.h"
#import "java/io/InputStream.h"
#import "java/io/OutputStream.h"
#import "java/lang/Exception.h"
#import "java/lang/IllegalArgumentException.h"
#import "java/lang/NullPointerException.h"
#import "java/lang/UnsupportedOperationException.h"
#import "java/net/InetAddress.h"
#import "java/net/Socket.h"
#import "java/net/SocketException.h"
#import "java/io/IOException.h"
#import "jni_util.h"

#pragma clang diagnostic push
#pragma GCC diagnostic ignored "-Wdeprecated-declarations"

@class SslInputStream;
@class SslOutputStream;
static OSStatus SslReadCallback(SSLConnectionRef connection, void *data, size_t *dataLength);
static OSStatus SslWriteCallback(SSLConnectionRef connection, const void *data, size_t *dataLength);
static void checkStatus(OSStatus status);
static void setUpContext(ComGoogleJ2objcNetSslIosSslSocket *self);
static void tearDownContext(ComGoogleJ2objcNetSslIosSslSocket *self);

// Maps from Java SSL constants to the SSLProtocol enumeration.
static NSDictionary *protocolMapping;

// The users of this class perform I/O via the two stream specializations: SslInputStream and
// SslOutputStream. The actual network I/O operations are perfomed by the inherited java streams.
// Expected data flow:
//
// - Read: SslInputStream.read --> SSLRead --> SslReadCallback --> JavaIoInputStream.read
// - Write: SslOutputStream.write --> SSLWrite --> SslWriteCallback --> JavaIoOutputStream.write
@interface ComGoogleJ2objcNetSslIosSslSocket() {
@public
  SSLContextRef _sslContext;
  SslInputStream *_sslInputStream;
  SslOutputStream *_sslOutputStream;
  IOSObjectArray *enabledProtocols;
  BOOL handshakeCompleted;

  // Used to forward exceptions from the plain streams to the SSL streams.
  JavaLangException *_sslException;
}
- (JavaIoInputStream *)plainInputStream;
- (JavaIoOutputStream *)plainOutputStream;
- (NSString *)getHostname;
@end

// An input stream that uses Apple's SSLRead.
@interface SslInputStream : JavaIoInputStream {
  ComGoogleJ2objcNetSslIosSslSocket *_socket;
}
- (instancetype)initWithSocket:(ComGoogleJ2objcNetSslIosSslSocket *)socket;
@end

@implementation SslInputStream

- (instancetype)initWithSocket:(ComGoogleJ2objcNetSslIosSslSocket *)socket {
  if (self = [super init]) {
    _socket = socket;
  }
  return self;
}

- (jint)readWithByteArray:(IOSByteArray *)b
                  withInt:(jint)off
                  withInt:(jint)len {
  size_t processed = 0;
  OSStatus status;

  [_socket startHandshake];

  // Deplete the SSL buffer before issuing a new read.
  size_t available = 0;
  checkStatus(SSLGetBufferedReadSize(_socket->_sslContext, &available));
  if (available != 0) {
    len = MIN(((jint) available), len);
  }

  @synchronized (_socket) {
    do {
      status = SSLRead(_socket->_sslContext, b->buffer_ + off, len, &processed);
    } while (status == errSSLWouldBlock && processed == 0);
  }

  if (status == errSSLClosedGraceful || status == errSSLClosedAbort) {
    processed = -1;
  } else {
    checkStatus(status);
  }
  if (_socket->_sslException) {
    @throw _socket->_sslException;
  }
  return (jint)processed;
}

- (jint)read {
  IOSByteArray *b = [IOSByteArray arrayWithLength:1];
  jint processed = [self readWithByteArray:b];
  if (processed == 1) {
    return b->buffer_[0];
  }
  return processed;
}

- (jlong)skipWithLong:(jlong)n {
  J2ObjCThrowByName(JavaLangUnsupportedOperationException, @"");
}

@end

// An output stream that uses Apple's SSLWrite.
@interface SslOutputStream : JavaIoOutputStream {
  ComGoogleJ2objcNetSslIosSslSocket *_socket;
}
- (instancetype)initWithSocket:(ComGoogleJ2objcNetSslIosSslSocket *)socket;
@end

@implementation SslOutputStream

- (instancetype)initWithSocket:(ComGoogleJ2objcNetSslIosSslSocket *)socket {
  if (self = [super init]) {
    _socket = socket;
  }
  return self;
}

- (void)writeWithByteArray:(IOSByteArray *)b
                   withInt:(jint)off
                   withInt:(jint)len {
  OSStatus status;
  size_t processed = 0;

  [_socket startHandshake];
  while (len > 0) {
    @synchronized (_socket) {
      status = SSLWrite(_socket->_sslContext, b->buffer_ + off, len, &processed);
    }
    if (status == errSecSuccess || status == errSSLWouldBlock) {
      off += processed;
      len -= processed;
    } else if (_socket->_sslException) {
      @throw _socket->_sslException;
    } else {
      checkStatus(status);
    }
  }
}

- (void)writeWithInt:(jint)b {
  IOSByteArray *array = [IOSByteArray arrayWithLength:1];
  array->buffer_[0] = b;
  [self writeWithByteArray:array];
}

- (void)flush {
  // The framework keeps SSL caches for reading and writing. Whenever a call to SSLWrite returns
  // errSSLWouldBlock, the data has been copied to the cache, but not yet (completely) sent.
  // In order to flush this cache, we have to call SSLWrite on an empty buffer.
  OSStatus status;
  size_t processed = 0;
  @synchronized (_socket) {
    do {
      status = SSLWrite(_socket->_sslContext, nil, 0, &processed);
    } while (status == errSSLWouldBlock);
  }
  [[_socket plainOutputStream] flush];
}

@end


@implementation ComGoogleJ2objcNetSslIosSslSocket

+ (void)initialize {
  if (self != [ComGoogleJ2objcNetSslIosSslSocket class]) {
    return;
  }
  NSMutableDictionary *temp = AUTORELEASE([[NSMutableDictionary alloc] init]);
  NSString *key;
  key = [ComGoogleJ2objcSecurityIosSecurityProvider_SslProtocol_get_DEFAULT() description];
  temp[key] = @(kTLSProtocol1);
  key = [ComGoogleJ2objcSecurityIosSecurityProvider_SslProtocol_get_TLS() description];
  temp[key] = @(kTLSProtocol1);
  key = [ComGoogleJ2objcSecurityIosSecurityProvider_SslProtocol_get_TLS_V1() description];
  temp[key] = @(kTLSProtocol1);
  key = [ComGoogleJ2objcSecurityIosSecurityProvider_SslProtocol_get_TLS_V11() description];
  temp[key] = @(kTLSProtocol11);
  key = [ComGoogleJ2objcSecurityIosSecurityProvider_SslProtocol_get_TLS_V12() description];
  temp[key] = @(kTLSProtocol12);
  protocolMapping = [[NSDictionary alloc] initWithDictionary:temp];
}

- (JavaIoInputStream *)plainInputStream {
  return [super getInputStream];
}

- (JavaIoOutputStream *)plainOutputStream {
  return [super getOutputStream];
}

- (NSString *)getHostname {
  return [[self getInetAddress] getHostName];
}

- (void)dealloc {
  tearDownContext(self);
#if !__has_feature(objc_arc)
  [_sslInputStream release];
  [_sslOutputStream release];
  [_sslException release];
  [super dealloc];
#endif
}

#pragma mark JavaNetSocket methods

- (void)close {
  @synchronized(self) {
    tearDownContext(self);
    if (![self isClosed]) {
      [super close];
    }
  }
}

- (JavaIoInputStream *)getInputStream {
  return JreRetainedLocalValue(_sslInputStream);
}

- (JavaIoOutputStream *)getOutputStream {
  return JreRetainedLocalValue(_sslOutputStream);
}

#pragma mark JavaNetSSlSocket methods

- (IOSObjectArray *)getSupportedCipherSuites {
  return [IOSObjectArray arrayWithLength:0 type:NSString_class_()];
}

- (IOSObjectArray *)getEnabledCipherSuites {
  return [IOSObjectArray arrayWithLength:0 type:NSString_class_()];
}

- (void)setEnabledCipherSuitesWithNSStringArray:(IOSObjectArray *)suites {
  J2ObjCThrowByName(JavaLangUnsupportedOperationException, @"");
}

- (IOSObjectArray *)getSupportedProtocols {
  return [IOSObjectArray arrayWithArray:enabledProtocols];
}

- (IOSObjectArray *)getEnabledProtocols {
  return [IOSObjectArray arrayWithArray:enabledProtocols];
}

- (void)setEnabledProtocolsWithNSStringArray:(IOSObjectArray *)protocols {
  if (!protocols) {
    J2ObjCThrowByName(JavaLangIllegalArgumentException, @"Null argument");
  }
  for (NSString *p in protocols) {
    if (!protocolMapping[p]) {
      NSString *msg = [NSString stringWithFormat:@"Invalid protocol: %@", p];
      J2ObjCThrowByName(JavaLangIllegalArgumentException, msg);
    }
  }
  JreStrongAssign(&enabledProtocols, [IOSObjectArray arrayWithArray:protocols]);
}

- (id<JavaxNetSslSSLSession>)getSession {
  J2ObjCThrowByName(JavaLangUnsupportedOperationException, @"");
}

- (void)addHandshakeCompletedListenerWithJavaxNetSslHandshakeCompletedListener:
    (id<JavaxNetSslHandshakeCompletedListener>)listener {
  J2ObjCThrowByName(JavaLangUnsupportedOperationException, @"");
}

- (void)removeHandshakeCompletedListenerWithJavaxNetSslHandshakeCompletedListener:
    (id<JavaxNetSslHandshakeCompletedListener>)listener {
  J2ObjCThrowByName(JavaLangUnsupportedOperationException, @"");
}

- (void)startHandshake {
  @synchronized(self) {
    if (!handshakeCompleted) {
      setUpContext(self);
      OSStatus status;
      do {
        status = SSLHandshake(_sslContext);
      } while (status == errSSLWouldBlock);
      checkStatus(status);
      handshakeCompleted = TRUE;
    }
  }
}

- (void)setUseClientModeWithBoolean:(jboolean)mode {
  J2ObjCThrowByName(JavaLangUnsupportedOperationException, @"");
}

- (jboolean)getUseClientMode {
  return TRUE;  // Currently only client mode is supported.
}

- (void)setNeedClientAuthWithBoolean:(jboolean)need {
  J2ObjCThrowByName(JavaLangUnsupportedOperationException, @"");
}

- (jboolean)getNeedClientAuth {
  return FALSE;
}

- (void)setWantClientAuthWithBoolean:(jboolean)want {
  J2ObjCThrowByName(JavaLangUnsupportedOperationException, @"");
}

- (jboolean)getWantClientAuth {
  return FALSE;
}

- (void)setEnableSessionCreationWithBoolean:(jboolean)flag {
  J2ObjCThrowByName(JavaLangUnsupportedOperationException, @"");
}

- (jboolean)getEnableSessionCreation {
  return FALSE;
}

@end

static OSStatus SslReadCallback(SSLConnectionRef connection, void *data, size_t *dataLength) {
  @autoreleasepool {
    ComGoogleJ2objcNetSslIosSslSocket *socket = (ComGoogleJ2objcNetSslIosSslSocket *) connection;
    IOSByteArray *array = [IOSByteArray arrayWithLength:*dataLength];
    jint processed;
    @try {
      processed = [[socket plainInputStream] readWithByteArray:array];
    } @catch (JavaIoIOException *e) {
      JreStrongAssign(&socket->_sslException, e);
      return errSSLInternal;
    }

    if (processed  < 0) {
      *dataLength = 0;
      return errSSLClosedGraceful;
    }

    OSStatus status = processed < *dataLength ? errSSLWouldBlock : errSecSuccess;
    if (processed > 0) {
      [array getBytes:(jbyte *)data length:processed];
    }
    *dataLength = processed;
    return status;
  }
}

static OSStatus SslWriteCallback(SSLConnectionRef connection,
                                 const void *data,
                                 size_t *dataLength) {
  @autoreleasepool {
    ComGoogleJ2objcNetSslIosSslSocket *socket = (ComGoogleJ2objcNetSslIosSslSocket *) connection;
    IOSByteArray *array = [IOSByteArray arrayWithBytes:(const jbyte *)data count:*dataLength];
    @try {
      [[socket plainOutputStream] writeWithByteArray:array];
      [[socket plainOutputStream] flush];
    } @catch (JavaIoIOException *e) {
      JreStrongAssign(&socket->_sslException, e);
      return errSSLInternal;
    }
    return errSecSuccess;
  }
}

static void checkStatus(OSStatus status) {
  if (status != errSecSuccess) {
    NSString *msg = [NSString stringWithFormat:@"status: %d", (int)status];
    J2ObjCThrowByName(JavaNetSocketException, msg);
  }
}

static void init(ComGoogleJ2objcNetSslIosSslSocket *self) {
  self->_sslInputStream = [[SslInputStream alloc] initWithSocket:self];
  self->_sslOutputStream = [[SslOutputStream alloc] initWithSocket:self];
  self->_sslException = nil;
}

static void setUpContext(ComGoogleJ2objcNetSslIosSslSocket *self) {
  self->_sslContext = SSLCreateContext(nil, kSSLClientSide, kSSLStreamType);
  checkStatus(SSLSetIOFuncs(self->_sslContext, SslReadCallback, SslWriteCallback));
  checkStatus(SSLSetConnection(self->_sslContext, self));
  NSString *hostName = [self getHostname];
  checkStatus(SSLSetPeerDomainName(self->_sslContext, [hostName UTF8String], [hostName length]));
  SSLProtocol protocol = [protocolMapping[[self->enabledProtocols objectAtIndex:0]] intValue];
  checkStatus(SSLSetProtocolVersionMin(self->_sslContext, protocol));
}

static void tearDownContext(ComGoogleJ2objcNetSslIosSslSocket *self) {
  if (self->_sslContext) {
    SSLClose(self->_sslContext);
    CFRelease(self->_sslContext);
    self->_sslContext = nil;
  }
}

// public IosSslSocket()
void ComGoogleJ2objcNetSslIosSslSocket_init(ComGoogleJ2objcNetSslIosSslSocket *self) {
  JavaxNetSslSSLSocket_init(self);
  init(self);
}

ComGoogleJ2objcNetSslIosSslSocket *new_ComGoogleJ2objcNetSslIosSslSocket_init(void) {
  J2OBJC_NEW_IMPL(ComGoogleJ2objcNetSslIosSslSocket, init)
}

ComGoogleJ2objcNetSslIosSslSocket *create_ComGoogleJ2objcNetSslIosSslSocket_init(void) {
  J2OBJC_CREATE_IMPL(ComGoogleJ2objcNetSslIosSslSocket, init)
}

// public IosSslSocket(String host, int port)
void ComGoogleJ2objcNetSslIosSslSocket_initWithNSString_withInt_(
    ComGoogleJ2objcNetSslIosSslSocket *self, NSString *host, jint port) {
  JavaxNetSslSSLSocket_initWithNSString_withInt_(self, host, port);
  init(self);
}

ComGoogleJ2objcNetSslIosSslSocket *
    new_ComGoogleJ2objcNetSslIosSslSocket_initWithNSString_withInt_(NSString *host, jint port) {
  J2OBJC_NEW_IMPL(ComGoogleJ2objcNetSslIosSslSocket, initWithNSString_withInt_, host, port)
}

ComGoogleJ2objcNetSslIosSslSocket *
    create_ComGoogleJ2objcNetSslIosSslSocket_initWithNSString_withInt_(NSString *host, jint port) {
  J2OBJC_CREATE_IMPL(ComGoogleJ2objcNetSslIosSslSocket, initWithNSString_withInt_, host, port)
}

// public IosSslSocket(String host, int port, InetAddress localAddr, int localPort)
void ComGoogleJ2objcNetSslIosSslSocket_initWithNSString_withInt_withJavaNetInetAddress_withInt_(
    ComGoogleJ2objcNetSslIosSslSocket *self, NSString *host, jint port,
    JavaNetInetAddress *localAddr, jint localPort) {
  JavaxNetSslSSLSocket_initWithNSString_withInt_withJavaNetInetAddress_withInt_(
      self, host, port, localAddr, localPort);
  init(self);
}

ComGoogleJ2objcNetSslIosSslSocket *
new_ComGoogleJ2objcNetSslIosSslSocket_initWithNSString_withInt_withJavaNetInetAddress_withInt_(
    NSString *host, jint port, JavaNetInetAddress *localAddr, jint localPort) {
  J2OBJC_NEW_IMPL(ComGoogleJ2objcNetSslIosSslSocket,
                  initWithNSString_withInt_withJavaNetInetAddress_withInt_, host, port, localAddr,
                  localPort)
}

ComGoogleJ2objcNetSslIosSslSocket *
create_ComGoogleJ2objcNetSslIosSslSocket_initWithNSString_withInt_withJavaNetInetAddress_withInt_(
    NSString *host, jint port, JavaNetInetAddress *localAddr, jint localPort) {
  J2OBJC_CREATE_IMPL(ComGoogleJ2objcNetSslIosSslSocket,
                     initWithNSString_withInt_withJavaNetInetAddress_withInt_, host, port,
                     localAddr, localPort)
}

// public IosSslSocket(InetAddress host, int port)
void ComGoogleJ2objcNetSslIosSslSocket_initWithJavaNetInetAddress_withInt_(
    ComGoogleJ2objcNetSslIosSslSocket *self, JavaNetInetAddress *address, jint port) {
  JavaxNetSslSSLSocket_initWithJavaNetInetAddress_withInt_(self, address, port);
  init(self);
}

ComGoogleJ2objcNetSslIosSslSocket *
new_ComGoogleJ2objcNetSslIosSslSocket_initWithJavaNetInetAddress_withInt_(
    JavaNetInetAddress *address, jint port) {
  J2OBJC_NEW_IMPL(ComGoogleJ2objcNetSslIosSslSocket, initWithJavaNetInetAddress_withInt_, address,
                  port)
}

ComGoogleJ2objcNetSslIosSslSocket *
create_ComGoogleJ2objcNetSslIosSslSocket_initWithJavaNetInetAddress_withInt_(
    JavaNetInetAddress *address, jint port) {
  J2OBJC_CREATE_IMPL(ComGoogleJ2objcNetSslIosSslSocket, initWithJavaNetInetAddress_withInt_,
                     address, port)
}

// public IosSslSocket(InetAddress address, int port, InetAddress localAddr, int localPort)
// NOLINTNEXTLINE
void ComGoogleJ2objcNetSslIosSslSocket_initWithJavaNetInetAddress_withInt_withJavaNetInetAddress_withInt_(
    ComGoogleJ2objcNetSslIosSslSocket *self, JavaNetInetAddress *address, jint port,
    JavaNetInetAddress *localAddr, jint localPort) {
  JavaxNetSslSSLSocket_initWithJavaNetInetAddress_withInt_withJavaNetInetAddress_withInt_(
      self, address, port, localAddr, localPort);
  init(self);
}

// NOLINTNEXTLINE
ComGoogleJ2objcNetSslIosSslSocket *new_ComGoogleJ2objcNetSslIosSslSocket_initWithJavaNetInetAddress_withInt_withJavaNetInetAddress_withInt_(
    JavaNetInetAddress *address, jint port, JavaNetInetAddress *localAddr, jint localPort) {
  J2OBJC_NEW_IMPL(ComGoogleJ2objcNetSslIosSslSocket,
                  initWithJavaNetInetAddress_withInt_withJavaNetInetAddress_withInt_, address, port,
                  localAddr, localPort)
}

// NOLINTNEXTLINE
ComGoogleJ2objcNetSslIosSslSocket *create_ComGoogleJ2objcNetSslIosSslSocket_initWithJavaNetInetAddress_withInt_withJavaNetInetAddress_withInt_(
    JavaNetInetAddress *address, jint port, JavaNetInetAddress *localAddr, jint localPort) {
  J2OBJC_CREATE_IMPL(ComGoogleJ2objcNetSslIosSslSocket,
                     initWithJavaNetInetAddress_withInt_withJavaNetInetAddress_withInt_, address,
                     port, localAddr, localPort)
}

// Delegates most of the calls to the underlying socket.
@interface WrapperSocket : ComGoogleJ2objcNetSslIosSslSocket {
@public
  JavaNetSocket *underlyingSocket;
  NSString *hostname;
  BOOL autoClose;
}
@end

@implementation WrapperSocket

- (void)dealloc {
#if !__has_feature(objc_arc)
  [underlyingSocket release];
  [hostname release];
  [super dealloc];
#endif
}

#pragma mark ComGoogleJ2objcNetSslIosSslSocket methods

- (void)close {
  @synchronized(self) {
    tearDownContext(self);
    if (autoClose && ![underlyingSocket isClosed]) {
      [underlyingSocket close];
    }
  }
}

- (JavaIoInputStream *)plainInputStream {
  return [underlyingSocket getInputStream];
}

- (JavaIoOutputStream *)plainOutputStream {
  return [underlyingSocket getOutputStream];
}

- (NSString *)getHostname {
  return hostname;
}

#pragma mark JavaNetSocket methods

- (void)bindWithJavaNetSocketAddress:(JavaNetSocketAddress *)bindpoint {
  [underlyingSocket bindWithJavaNetSocketAddress:bindpoint];
}

- (void)connectWithJavaNetSocketAddress:(JavaNetSocketAddress *)endpoint {
  [underlyingSocket connectWithJavaNetSocketAddress:endpoint];
}

- (void)connectWithJavaNetSocketAddress:(JavaNetSocketAddress *)endpoint
                                withInt:(jint)timeout {
  [underlyingSocket connectWithJavaNetSocketAddress:endpoint withInt:timeout];
}

- (JavaNioChannelsSocketChannel *)getChannel {
  return [underlyingSocket getChannel];
}

- (JavaIoFileDescriptor *)getFileDescriptor$ {
  return [underlyingSocket getFileDescriptor$];
}

- (JavaNetInetAddress *)getInetAddress {
  return [underlyingSocket getInetAddress];
}

- (jboolean)getKeepAlive {
  return [underlyingSocket getKeepAlive];
}

- (JavaNetInetAddress *)getLocalAddress {
  return [underlyingSocket getLocalAddress];
}

- (jint)getLocalPort {
  return [underlyingSocket getLocalPort];
}

- (JavaNetSocketAddress *)getLocalSocketAddress {
  return [underlyingSocket getLocalSocketAddress];
}

- (jboolean)getOOBInline {
  return [underlyingSocket getOOBInline];
}

- (jint)getPort {
  return [underlyingSocket getPort];
}

- (jint)getReceiveBufferSize {
  return [underlyingSocket getReceiveBufferSize];
}

- (JavaNetSocketAddress *)getRemoteSocketAddress {
  return [underlyingSocket getRemoteSocketAddress];
}

- (jboolean)getReuseAddress {
  return [underlyingSocket getReuseAddress];
}

- (jint)getSendBufferSize {
  return [underlyingSocket getSendBufferSize];
}

- (jint)getSoLinger {
  return [underlyingSocket getSoLinger];
}

- (jint)getSoTimeout {
  return [underlyingSocket getSoTimeout];
}

- (jboolean)getTcpNoDelay {
  return [underlyingSocket getTcpNoDelay];
}

- (jint)getTrafficClass {
  return [underlyingSocket getTrafficClass];
}

- (jboolean)isBound {
  return [underlyingSocket isBound];
}

- (jboolean)isClosed {
  return [underlyingSocket isClosed];
}

- (jboolean)isConnected {
  return [underlyingSocket isConnected];
}

- (jboolean)isInputShutdown {
  return [underlyingSocket isInputShutdown];
}

- (jboolean)isOutputShutdown {
  return [underlyingSocket isOutputShutdown];
}

- (void)sendUrgentDataWithInt:(jint)data {
  [underlyingSocket sendUrgentDataWithInt:data];
}

- (void)setKeepAliveWithBoolean:(jboolean)on {
  [underlyingSocket setKeepAliveWithBoolean:on];
}

- (void)setOOBInlineWithBoolean:(jboolean)on {
  [underlyingSocket setOOBInlineWithBoolean:on];
}

- (void)setPerformancePreferencesWithInt:(jint)connectionTime
                                 withInt:(jint)latency
                                 withInt:(jint)bandwidth {
  [underlyingSocket setPerformancePreferencesWithInt:connectionTime
                                             withInt:latency
                                             withInt:bandwidth];
}

- (void)setReceiveBufferSizeWithInt:(jint)size {
  [underlyingSocket setReceiveBufferSizeWithInt:size];
}

- (void)setReuseAddressWithBoolean:(jboolean)on {
  [underlyingSocket setReuseAddressWithBoolean:on];
}

- (void)setSendBufferSizeWithInt:(jint)size {
  [underlyingSocket setSendBufferSizeWithInt:size];
}

- (void)setSoLingerWithBoolean:(jboolean)on
                       withInt:(jint)linger {
  [underlyingSocket setSoLingerWithBoolean:on withInt:linger];
}

- (void)setSoTimeoutWithInt:(jint)timeout {
  [underlyingSocket setSoTimeoutWithInt:timeout];
}

- (void)setTcpNoDelayWithBoolean:(jboolean)on {
  [underlyingSocket setTcpNoDelayWithBoolean:on];
}

- (void)setTrafficClassWithInt:(jint)tc {
  [underlyingSocket setTrafficClassWithInt:tc];
}

- (void)shutdownInput {
  [underlyingSocket shutdownInput];
}

- (void)shutdownOutput {
  [underlyingSocket shutdownOutput];
}

- (NSString *)description {
  return [underlyingSocket description];
}

- (void)createImplWithBoolean:(jboolean)stream {
  [underlyingSocket createImplWithBoolean:stream];
}

- (JavaNetSocketImpl *)getImpl {
  return [underlyingSocket getImpl];
}

- (void)postAccept {
  [underlyingSocket postAccept];
}

- (void)setBound {
  [underlyingSocket setBound];
}

- (void)setConnected {
  [underlyingSocket setConnected];
}

- (void)setCreated {
  [underlyingSocket setCreated];
}

- (void)setImpl {
  [underlyingSocket setImpl];
}

@end

// public IosSslSocket(Socket s, String host, int port, boolean autoClose)
void WrapperSocket_initWithJavaNetSocket_initWithNSString_withInt_withBoolean_(
    WrapperSocket *self, JavaNetSocket *socket, NSString *host, jint port, jboolean autoClose) {
  if (![nil_chk(socket) isConnected]) {
    J2ObjCThrowByName(JavaNetSocketException, @"socket is not connected.");
  }
  init(self);
  JreStrongAssign(&self->underlyingSocket, socket);
  JreStrongAssign(&self->hostname, host);
  self->autoClose = autoClose;
}

ComGoogleJ2objcNetSslIosSslSocket *
new_ComGoogleJ2objcNetSslIosSslSocket_initWithJavaNetSocket_withNSString_withInt_withBoolean_(
    JavaNetSocket *socket, NSString *host, jint port, jboolean autoClose) {
  J2OBJC_NEW_IMPL(WrapperSocket, initWithJavaNetSocket_initWithNSString_withInt_withBoolean_,
                  socket, host, port, autoClose)
}

ComGoogleJ2objcNetSslIosSslSocket *
create_ComGoogleJ2objcNetSslIosSslSocket_initWithJavaNetSocket_withNSString_withInt_withBoolean_(
    JavaNetSocket *socket, NSString *host, jint port, jboolean autoClose) {
  J2OBJC_CREATE_IMPL(WrapperSocket, initWithJavaNetSocket_initWithNSString_withInt_withBoolean_,
                     socket, host, port, autoClose)
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleJ2objcNetSslIosSslSocket)

#pragma clang diagnostic pop
