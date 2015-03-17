# Copyright 2011 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Makefile for building the iOS emulation library.
#
# Author: Tom Ball

.SUFFIXES:
.PHONY: default clean

include environment.mk

# Native obj-c sources that do not emulate a specific java class.
IOS_OBJS = \
	cbigint.o \
	DebugUtils.o \
	FastPointerLookup.o \
	IOSArray.o \
	IOSArrayClass.o \
	IOSClass.o \
	IOSConcreteClass.o \
	IOSLocaleData.o \
	IOSMappedClass.o \
	IOSObjectArray.o \
	IOSPrimitiveArray.o \
	IOSPrimitiveClass.o \
	IOSProtocolClass.o \
	IOSReference.o \
	IOSReflection.o \
	JavaMetadata.o \
	JavaToIOSInputStreamAdapter.o \
	JreEmulation.o \
	NSCopying+JavaCloneable.o \
	NSDataInputStream.o \
	NSDataOutputStream.o \
	NSDictionaryMap.o \
	NSNumber+JavaNumber.o \
	NSObject+JavaObject.o \
	NSString+JavaString.o \
	java_lang_IntegralToString.o \
	java_lang_RealToString.o \
	java_lang_StringToReal.o \
	java_util_regex_Matcher.o \
	java_util_regex_Pattern.o \
	java/lang/reflect/ExecutableMember.o \
	java/lang/reflect/ParameterizedTypeImpl.o \
	java/lang/reflect/TypeVariableImpl.o

APPLE_SRCS = \
	objc-sync.m

# Java classes with hand written obj-c implementations. Shouldn't be translated,
# but need to include the .java file in jre_emul.jar and the .m file in
# libjre_emul.a.
IOS_JAVA_OBJS = \
	java/lang/AbstractStringBuilder.o \
	java/lang/Iterable.o \
	java/lang/Throwable.o \
	java/lang/reflect/AccessibleObject.o \
	java/lang/reflect/Constructor.o \
	java/lang/reflect/Field.o \
	java/lang/reflect/Method.o \
	javax/annotation/Resource.o

# Java sources that should not be translated but should be included in the jar.
JAR_ONLY_SOURCES = \
	java/lang/Cloneable.java

# Java sources to be translated normally.
JAVA_SOURCES = \
	android/os/SystemClock.java \
	android/test/suitebuilder/annotation/LargeTest.java \
	android/test/suitebuilder/annotation/MediumTest.java \
	android/test/suitebuilder/annotation/SmallTest.java \
	android/test/suitebuilder/annotation/Smoke.java \
	android/test/suitebuilder/annotation/Suppress.java \
	android/text/Editable.java \
	android/text/GetChars.java \
	android/text/InputFilter.java \
	android/text/InputType.java \
	android/text/NoCopySpan.java \
	android/text/Selection.java \
	android/text/Spannable.java \
	android/text/SpannableString.java \
	android/text/SpannableStringBuilder.java \
	android/text/SpannableStringInternal.java \
	android/text/Spanned.java \
	android/text/SpannedString.java \
	android/text/SpanSet.java \
	android/text/SpanWatcher.java \
	android/text/TextUtils.java \
	android/text/TextWatcher.java \
	android/text/util/Rfc822Token.java \
	android/text/util/Rfc822Tokenizer.java \
	android/util/ArrayMap.java \
	android/util/ArraySet.java \
	android/util/Base64.java \
	android/util/Base64DataException.java \
	android/util/Base64InputStream.java \
	android/util/Base64OutputStream.java \
	android/util/ContainerHelpers.java \
	android/util/Log.java \
	android/util/LruCache.java \
	android/util/MapCollections.java \
	android/util/Pair.java \
	android/util/Printer.java \
	android/util/SparseArray.java \
	android/util/SparseBooleanArray.java \
	android/util/SparseIntArray.java \
	android/util/SparseLongArray.java \
	com/android/internal/util/ArrayUtils.java \
	com/google/android/collect/Lists.java \
	com/google/android/collect/Maps.java \
	com/google/android/collect/Sets.java \
	com/google/j2objc/annotations/ObjectiveCName.java \
	com/google/j2objc/net/IosHttpHandler.java \
	com/google/j2objc/net/IosHttpsHandler.java \
	com/google/j2objc/net/IosHttpURLConnection.java \
	com/google/j2objc/security/IosMD5MessageDigest.java \
	com/google/j2objc/security/IosRSAKey.java \
	com/google/j2objc/security/IosRSAKeyFactory.java \
	com/google/j2objc/security/IosRSAKeyPairGenerator.java \
	com/google/j2objc/security/IosSecureRandomImpl.java \
	com/google/j2objc/security/IosSecurityProvider.java \
	com/google/j2objc/security/IosSHAMessageDigest.java \
	com/google/j2objc/security/cert/IosCertificateFactory.java \
	com/google/j2objc/security/cert/IosX509Certificate.java \
	com/google/j2objc/util/ScopedLocalRef.java \
	dalvik/system/BlockGuard.java \
	dalvik/system/CloseGuard.java \
	java/beans/IndexedPropertyChangeEvent.java \
	java/beans/PropertyChangeEvent.java \
	java/beans/PropertyChangeListener.java \
	java/beans/PropertyChangeListenerProxy.java \
	java/beans/PropertyChangeSupport.java \
	java/io/BufferedInputStream.java \
	java/io/BufferedOutputStream.java \
	java/io/BufferedReader.java \
	java/io/BufferedWriter.java \
	java/io/ByteArrayInputStream.java \
	java/io/ByteArrayOutputStream.java \
	java/io/CharArrayReader.java \
	java/io/CharArrayWriter.java \
	java/io/CharConversionException.java \
	java/io/Closeable.java \
	java/io/DataInput.java \
	java/io/DataInputStream.java \
	java/io/DataOutput.java \
	java/io/DataOutputStream.java \
	java/io/EOFException.java \
	java/io/EmulatedFields.java \
	java/io/EmulatedFieldsForDumping.java \
	java/io/EmulatedFieldsForLoading.java \
	java/io/Externalizable.java \
	java/io/File.java \
	java/io/FileDescriptor.java \
	java/io/FileFilter.java \
	java/io/FileInputStream.java \
	java/io/FileNotFoundException.java \
	java/io/FileOutputStream.java \
	java/io/FilePermission.java \
	java/io/FileReader.java \
	java/io/FileWriter.java \
	java/io/FilenameFilter.java \
	java/io/FilterInputStream.java \
	java/io/FilterOutputStream.java \
	java/io/FilterReader.java \
	java/io/FilterWriter.java \
	java/io/Flushable.java \
	java/io/InputStream.java \
	java/io/InputStreamReader.java \
	java/io/InterruptedIOException.java \
	java/io/InvalidClassException.java \
	java/io/InvalidObjectException.java \
	java/io/IOError.java \
	java/io/IOException.java \
	java/io/LineNumberInputStream.java \
	java/io/LineNumberReader.java \
	java/io/NotActiveException.java \
	java/io/NotSerializableException.java \
	java/io/ObjectInput.java \
	java/io/ObjectInputStream.java \
	java/io/ObjectInputValidation.java \
	java/io/ObjectOutput.java \
	java/io/ObjectOutputStream.java \
	java/io/ObjectStreamClass.java \
	java/io/ObjectStreamConstants.java \
	java/io/ObjectStreamException.java \
	java/io/ObjectStreamField.java \
	java/io/OptionalDataException.java \
	java/io/OutputStream.java \
	java/io/OutputStreamWriter.java \
	java/io/PipedInputStream.java \
	java/io/PipedOutputStream.java \
	java/io/PipedReader.java \
	java/io/PipedWriter.java \
	java/io/PrintStream.java \
	java/io/PrintWriter.java \
	java/io/PushbackInputStream.java \
	java/io/PushbackReader.java \
	java/io/RandomAccessFile.java \
	java/io/Reader.java \
	java/io/SequenceInputStream.java \
	java/io/Serializable.java \
	java/io/SerializablePermission.java \
	java/io/SerializationHandleMap.java \
	java/io/StreamCorruptedException.java \
	java/io/StreamTokenizer.java \
	java/io/StringBufferInputStream.java \
	java/io/StringReader.java \
	java/io/StringWriter.java \
	java/io/SyncFailedException.java \
	java/io/UnsupportedEncodingException.java \
	java/io/UTFDataFormatException.java \
	java/io/WriteAbortedException.java \
	java/io/Writer.java \
	java/lang/AbstractMethodError.java \
	java/lang/Appendable.java \
	java/lang/ArithmeticException.java \
	java/lang/ArrayIndexOutOfBoundsException.java \
	java/lang/ArrayStoreException.java \
	java/lang/AssertionError.java \
	java/lang/AutoCloseable.java \
	java/lang/Boolean.java \
	java/lang/Byte.java \
	java/lang/CharSequence.java \
	java/lang/Character.java \
	java/lang/ClassCastException.java \
	java/lang/ClassFormatError.java \
	java/lang/ClassLoader.java \
	java/lang/ClassNotFoundException.java \
	java/lang/CloneNotSupportedException.java \
	java/lang/Comparable.java \
	java/lang/Deprecated.java \
	java/lang/Double.java \
	java/lang/Enum.java \
	java/lang/Error.java \
	java/lang/Exception.java \
	java/lang/ExceptionInInitializerError.java \
	java/lang/Float.java \
	java/lang/HexStringParser.java \
	java/lang/IllegalAccessError.java \
	java/lang/IllegalAccessException.java \
	java/lang/IllegalArgumentException.java \
	java/lang/IllegalMonitorStateException.java \
	java/lang/IllegalStateException.java \
	java/lang/IllegalThreadStateException.java \
	java/lang/IncompatibleClassChangeError.java \
	java/lang/IndexOutOfBoundsException.java \
	java/lang/InstantiationException.java \
	java/lang/Integer.java \
	java/lang/IntegralToString.java \
	java/lang/InternalError.java \
	java/lang/InterruptedException.java \
	java/lang/LinkageError.java \
	java/lang/Long.java \
	java/lang/Math.java \
	java/lang/NegativeArraySizeException.java \
	java/lang/NoClassDefFoundError.java \
	java/lang/NoSuchFieldError.java \
	java/lang/NoSuchFieldException.java \
	java/lang/NoSuchMethodError.java \
	java/lang/NoSuchMethodException.java \
	java/lang/NullPointerException.java \
	java/lang/NumberFormatException.java \
	java/lang/OutOfMemoryError.java \
	java/lang/Override.java \
	java/lang/Package.java \
	java/lang/Readable.java \
	java/lang/RealToString.java \
	java/lang/Runnable.java \
	java/lang/StringToReal.java \
	java/lang/annotation/Annotation.java \
	java/lang/annotation/AnnotationFormatError.java \
	java/lang/annotation/AnnotationTypeMismatchException.java \
	java/lang/annotation/Documented.java \
	java/lang/annotation/ElementType.java \
	java/lang/annotation/IncompleteAnnotationException.java \
	java/lang/annotation/Inherited.java \
	java/lang/annotation/Retention.java \
	java/lang/annotation/RetentionPolicy.java \
	java/lang/annotation/Target.java \
	java/lang/reflect/AnnotatedElement.java \
	java/lang/reflect/Array.java \
	java/lang/reflect/GenericArrayType.java \
	java/lang/reflect/GenericDeclaration.java \
	java/lang/reflect/GenericSignatureFormatError.java \
	java/lang/reflect/InvocationHandler.java \
	java/lang/reflect/InvocationTargetException.java \
	java/lang/reflect/MalformedParameterizedTypeException.java \
	java/lang/reflect/Member.java \
	java/lang/reflect/Modifier.java \
	java/lang/reflect/ParameterizedType.java \
	java/lang/reflect/Proxy.java \
	java/lang/reflect/ReflectPermission.java \
	java/lang/reflect/Type.java \
	java/lang/reflect/TypeVariable.java \
	java/lang/reflect/UndeclaredThrowableException.java \
	java/lang/reflect/WildcardType.java \
	java/lang/ReflectiveOperationException.java \
	java/lang/Runtime.java \
	java/lang/RuntimeException.java \
	java/lang/RuntimePermission.java \
	java/lang/SafeVarargs.java \
	java/lang/SecurityException.java \
	java/lang/SecurityManager.java \
	java/lang/Short.java \
	java/lang/StackOverflowError.java \
	java/lang/StackTraceElement.java \
	java/lang/StrictMath.java \
	java/lang/StringBuffer.java \
	java/lang/StringBuilder.java \
	java/lang/StringIndexOutOfBoundsException.java \
	java/lang/SuppressWarnings.java \
	java/lang/System.java \
	java/lang/Thread.java \
	java/lang/ThreadDeath.java \
	java/lang/ThreadGroup.java \
	java/lang/ThreadLocal.java \
	java/lang/TypeNotPresentException.java \
	java/lang/UnsupportedOperationException.java \
	java/lang/VirtualMachineError.java \
	java/lang/Void.java \
	java/lang/ref/PhantomReference.java \
	java/lang/ref/Reference.java \
	java/lang/ref/ReferenceQueue.java \
	java/lang/ref/SoftReference.java \
	java/lang/ref/WeakReference.java \
	java/math/BigDecimal.java \
	java/math/BigInteger.java \
	java/math/BitLevel.java \
	java/math/Conversion.java \
	java/math/Division.java \
	java/math/Elementary.java \
	java/math/Logical.java \
	java/math/MathContext.java \
	java/math/Multiplication.java \
	java/math/Primality.java \
	java/math/RoundingMode.java \
	java/net/AddressCache.java \
	java/net/Authenticator.java \
	java/net/BindException.java \
	java/net/CacheRequest.java \
	java/net/CacheResponse.java \
	java/net/ConnectException.java \
	java/net/ContentHandler.java \
	java/net/ContentHandlerFactory.java \
	java/net/CookieHandler.java \
	java/net/CookieManager.java \
	java/net/CookiePolicy.java \
	java/net/CookieStore.java \
	java/net/CookieStoreImpl.java \
	java/net/DatagramPacket.java \
	java/net/DatagramSocket.java \
	java/net/DatagramSocketImpl.java \
	java/net/DatagramSocketImplFactory.java \
	java/net/DefaultFileNameMap.java \
	java/net/FileNameMap.java \
	java/net/HttpCookie.java \
	java/net/HttpRetryException.java \
	java/net/HttpURLConnection.java \
	java/net/Inet4Address.java \
	java/net/Inet6Address.java \
	java/net/InetAddress.java \
	java/net/InetSocketAddress.java \
	java/net/InetUnixAddress.java \
	java/net/InterfaceAddress.java \
	java/net/MalformedURLException.java \
	java/net/MulticastSocket.java \
	java/net/NetworkInterface.java \
	java/net/NoRouteToHostException.java \
	java/net/PasswordAuthentication.java \
	java/net/PlainDatagramSocketImpl.java \
	java/net/PlainServerSocketImpl.java \
	java/net/PlainSocketImpl.java \
	java/net/PortUnreachableException.java \
	java/net/ProtocolException.java \
	java/net/Proxy.java \
	java/net/ProxySelector.java \
	java/net/ProxySelectorImpl.java \
	java/net/ResponseCache.java \
	java/net/SecureCacheResponse.java \
	java/net/ServerSocket.java \
	java/net/Socket.java \
	java/net/SocketAddress.java \
	java/net/SocketException.java \
	java/net/SocketImpl.java \
	java/net/SocketImplFactory.java \
	java/net/SocketOption.java \
	java/net/SocketOptions.java \
	java/net/SocketPermission.java \
	java/net/SocketTimeoutException.java \
	java/net/SocketUtils.java \
	java/net/Socks4Message.java \
	java/net/StandardSocketOptions.java \
	java/net/UnknownHostException.java \
	java/net/UnknownServiceException.java \
	java/net/URI.java \
	java/net/URISyntaxException.java \
	java/net/URL.java \
	java/net/URLConnection.java \
	java/net/URLClassLoader.java \
	java/net/URLDecoder.java \
	java/net/URLEncoder.java \
	java/net/URLStreamHandler.java \
	java/net/URLStreamHandlerFactory.java \
	java/nio/Buffer.java \
	java/nio/BufferOverflowException.java \
	java/nio/BufferUnderflowException.java \
	java/nio/ByteArrayBuffer.java \
	java/nio/ByteBuffer.java \
	java/nio/ByteBufferAsCharBuffer.java \
	java/nio/ByteBufferAsDoubleBuffer.java \
	java/nio/ByteBufferAsFloatBuffer.java \
	java/nio/ByteBufferAsIntBuffer.java \
	java/nio/ByteBufferAsLongBuffer.java \
	java/nio/ByteBufferAsShortBuffer.java \
	java/nio/ByteOrder.java \
	java/nio/CharArrayBuffer.java \
	java/nio/CharBuffer.java \
	java/nio/CharSequenceAdapter.java \
	java/nio/DatagramChannelImpl.java \
	java/nio/DirectByteBuffer.java \
	java/nio/DoubleArrayBuffer.java \
	java/nio/DoubleBuffer.java \
	java/nio/FileChannelImpl.java \
	java/nio/FileDescriptorChannel.java \
	java/nio/FloatArrayBuffer.java \
	java/nio/FloatBuffer.java \
	java/nio/IntArrayBuffer.java \
	java/nio/IntBuffer.java \
	java/nio/InvalidMarkException.java \
	java/nio/IoVec.java \
	java/nio/LongArrayBuffer.java \
	java/nio/LongBuffer.java \
	java/nio/MappedByteBuffer.java \
	java/nio/MemoryBlock.java \
	java/nio/NIOAccess.java \
	java/nio/NioUtils.java \
	java/nio/PipeImpl.java \
	java/nio/ReadOnlyBufferException.java \
	java/nio/SelectionKeyImpl.java \
	java/nio/SelectorImpl.java \
	java/nio/SelectorProviderImpl.java \
	java/nio/ShortArrayBuffer.java \
	java/nio/ShortBuffer.java \
	java/nio/ServerSocketChannelImpl.java \
	java/nio/SocketChannelImpl.java \
	java/nio/charset/CharacterCodingException.java \
	java/nio/charset/Charset.java \
	java/nio/charset/CharsetDecoder.java \
	java/nio/charset/CharsetEncoder.java \
	java/nio/charset/Charsets.java \
	java/nio/charset/CoderMalfunctionError.java \
	java/nio/charset/CoderResult.java \
	java/nio/charset/CodingErrorAction.java \
	java/nio/charset/IllegalCharsetNameException.java \
	java/nio/charset/IOSCharset.java \
	java/nio/charset/IOSCharsetDecoder.java \
	java/nio/charset/IOSCharsetEncoder.java \
	java/nio/charset/MalformedInputException.java \
	java/nio/charset/ModifiedUtf8.java \
	java/nio/charset/StandardCharsets.java \
	java/nio/charset/UnmappableCharacterException.java \
	java/nio/charset/UnsupportedCharsetException.java \
	java/nio/channels/AlreadyBoundException.java \
	java/nio/channels/AlreadyConnectedException.java \
	java/nio/channels/AsynchronousCloseException.java \
	java/nio/channels/ByteChannel.java \
	java/nio/channels/CancelledKeyException.java \
	java/nio/channels/Channel.java \
	java/nio/channels/Channels.java \
	java/nio/channels/ClosedByInterruptException.java \
	java/nio/channels/ClosedChannelException.java \
	java/nio/channels/ClosedSelectorException.java \
	java/nio/channels/ConnectionPendingException.java \
	java/nio/channels/DatagramChannel.java \
	java/nio/channels/FileChannel.java \
	java/nio/channels/FileLock.java \
	java/nio/channels/FileLockInterruptionException.java \
	java/nio/channels/GatheringByteChannel.java \
	java/nio/channels/IllegalBlockingModeException.java \
	java/nio/channels/IllegalSelectorException.java \
	java/nio/channels/InterruptibleChannel.java \
	java/nio/channels/NetworkChannel.java \
	java/nio/channels/NoConnectionPendingException.java \
	java/nio/channels/NonReadableChannelException.java \
	java/nio/channels/NonWritableChannelException.java \
	java/nio/channels/NotYetBoundException.java \
	java/nio/channels/NotYetConnectedException.java \
	java/nio/channels/OverlappingFileLockException.java \
	java/nio/channels/Pipe.java \
	java/nio/channels/ReadableByteChannel.java \
	java/nio/channels/ScatteringByteChannel.java \
	java/nio/channels/SeekableByteChannel.java \
	java/nio/channels/SelectableChannel.java \
	java/nio/channels/SelectionKey.java \
	java/nio/channels/Selector.java \
	java/nio/channels/ServerSocketChannel.java \
	java/nio/channels/SocketChannel.java \
	java/nio/channels/UnresolvedAddressException.java \
	java/nio/channels/UnsupportedAddressTypeException.java \
	java/nio/channels/WritableByteChannel.java \
	java/nio/channels/spi/AbstractInterruptibleChannel.java \
	java/nio/channels/spi/AbstractSelectableChannel.java \
	java/nio/channels/spi/AbstractSelectionKey.java \
	java/nio/channels/spi/AbstractSelector.java \
	java/nio/channels/spi/SelectorProvider.java \
	java/security/AccessControlContext.java \
	java/security/AccessControlException.java \
	java/security/AccessController.java \
	java/security/AlgorithmParameterGenerator.java \
	java/security/AlgorithmParameterGeneratorSpi.java \
	java/security/AlgorithmParameters.java \
	java/security/AlgorithmParametersSpi.java \
	java/security/AllPermission.java \
	java/security/AllPermissionCollection.java \
	java/security/BasicPermission.java \
	java/security/CodeSource.java \
	java/security/DigestException.java \
	java/security/DomainCombiner.java \
	java/security/GeneralSecurityException.java \
	java/security/Guard.java \
	java/security/GuardedObject.java \
	java/security/InvalidAlgorithmParameterException.java \
	java/security/InvalidKeyException.java \
	java/security/InvalidParameterException.java \
	java/security/Key.java \
	java/security/KeyException.java \
	java/security/KeyFactory.java \
	java/security/KeyFactorySpi.java \
	java/security/KeyManagementException.java \
	java/security/KeyPair.java \
	java/security/KeyPairGenerator.java \
	java/security/KeyPairGeneratorSpi.java \
	java/security/KeyStore.java \
	java/security/KeyStoreException.java \
	java/security/KeyStoreSpi.java \
	java/security/MessageDigest.java \
	java/security/MessageDigestSpi.java \
	java/security/NoSuchAlgorithmException.java \
	java/security/NoSuchProviderException.java \
	java/security/Permission.java \
	java/security/PermissionCollection.java \
	java/security/Permissions.java \
	java/security/PermissionsHash.java \
	java/security/Policy.java \
	java/security/Principal.java \
	java/security/PrivilegedAction.java \
	java/security/PrivilegedActionException.java \
	java/security/PrivilegedExceptionAction.java \
	java/security/ProtectionDomain.java \
	java/security/Provider.java \
	java/security/ProviderException.java \
	java/security/PrivateKey.java \
	java/security/PublicKey.java \
	java/security/SecureClassLoader.java \
	java/security/SecureRandom.java \
	java/security/SecureRandomSpi.java \
	java/security/Security.java \
	java/security/SecurityPermission.java \
	java/security/Signature.java \
	java/security/SignatureException.java \
	java/security/SignatureSpi.java \
	java/security/UnrecoverableEntryException.java \
	java/security/UnrecoverableKeyException.java \
	java/security/cert/CRL.java \
	java/security/cert/CRLException.java \
	java/security/cert/CertPath.java \
	java/security/cert/CertPathParameters.java \
	java/security/cert/Certificate.java \
	java/security/cert/CertificateEncodingException.java \
	java/security/cert/CertificateException.java \
	java/security/cert/CertificateExpiredException.java \
	java/security/cert/CertificateFactory.java \
	java/security/cert/CertificateFactorySpi.java \
	java/security/cert/CertificateNotYetValidException.java \
	java/security/cert/CertificateParsingException.java \
	java/security/cert/X509Certificate.java \
	java/security/cert/X509Extension.java \
	java/security/interfaces/ECKey.java \
	java/security/interfaces/ECPrivateKey.java \
	java/security/interfaces/ECPublicKey.java \
	java/security/interfaces/RSAKey.java \
	java/security/interfaces/RSAMultiPrimePrivateCrtKey.java \
	java/security/interfaces/RSAPrivateCrtKey.java \
	java/security/interfaces/RSAPrivateKey.java \
	java/security/interfaces/RSAPublicKey.java \
	java/security/spec/AlgorithmParameterSpec.java \
	java/security/spec/ECField.java \
	java/security/spec/ECFieldF2m.java \
	java/security/spec/ECFieldFp.java \
	java/security/spec/ECGenParameterSpec.java \
	java/security/spec/ECParameterSpec.java \
	java/security/spec/ECPoint.java \
	java/security/spec/ECPrivateKeySpec.java \
	java/security/spec/ECPublicKeySpec.java \
	java/security/spec/EllipticCurve.java \
	java/security/spec/EncodedKeySpec.java \
	java/security/spec/InvalidKeySpecException.java \
	java/security/spec/InvalidParameterSpecException.java \
	java/security/spec/KeySpec.java \
	java/security/spec/MGF1ParameterSpec.java \
	java/security/spec/PKCS8EncodedKeySpec.java \
	java/security/spec/PSSParameterSpec.java \
	java/security/spec/RSAKeyGenParameterSpec.java \
	java/security/spec/RSAMultiPrimePrivateCrtKeySpec.java \
	java/security/spec/RSAOtherPrimeInfo.java \
	java/security/spec/RSAPrivateCrtKeySpec.java \
	java/security/spec/RSAPrivateKeySpec.java \
	java/security/spec/RSAPublicKeySpec.java \
	java/security/spec/X509EncodedKeySpec.java \
	java/sql/Array.java \
	java/sql/BatchUpdateException.java \
	java/sql/Blob.java \
	java/sql/CallableStatement.java \
	java/sql/ClientInfoStatus.java \
	java/sql/Clob.java \
	java/sql/Connection.java \
	java/sql/DatabaseMetaData.java \
	java/sql/DataTruncation.java \
	java/sql/Date.java \
	java/sql/Driver.java \
	java/sql/DriverManager.java \
	java/sql/DriverPropertyInfo.java \
	java/sql/NClob.java \
	java/sql/ParameterMetaData.java \
	java/sql/PreparedStatement.java \
	java/sql/Ref.java \
	java/sql/ResultSet.java \
	java/sql/ResultSetMetaData.java \
	java/sql/RowId.java \
	java/sql/RowIdLifetime.java \
	java/sql/Savepoint.java \
	java/sql/SQLClientInfoException.java \
	java/sql/SQLData.java \
	java/sql/SQLDataException.java \
	java/sql/SQLException.java \
	java/sql/SQLFeatureNotSupportedException.java \
	java/sql/SQLInput.java \
	java/sql/SQLIntegrityConstraintViolationException.java \
	java/sql/SQLInvalidAuthorizationSpecException.java \
	java/sql/SQLNonTransientConnectionException.java \
	java/sql/SQLNonTransientException.java \
	java/sql/SQLOutput.java \
	java/sql/SQLPermission.java \
	java/sql/SQLRecoverableException.java \
	java/sql/SQLSyntaxErrorException.java \
	java/sql/SQLTimeoutException.java \
	java/sql/SQLTransactionRollbackException.java \
	java/sql/SQLTransientConnectionException.java \
	java/sql/SQLTransientException.java \
	java/sql/SQLWarning.java \
	java/sql/SQLXML.java \
	java/sql/Statement.java \
	java/sql/Struct.java \
	java/sql/Time.java \
	java/sql/Timestamp.java \
	java/sql/Types.java \
	java/sql/Wrapper.java \
	java/text/Annotation.java \
	java/text/AttributedCharacterIterator.java \
	java/text/AttributedString.java \
	java/text/CharacterIterator.java \
	java/text/ChoiceFormat.java \
	java/text/CollationKey.java \
	java/text/Collator.java \
	java/text/DateFormat.java \
	java/text/DateFormatSymbols.java \
	java/text/DecimalFormat.java \
	java/text/DecimalFormatSymbols.java \
	java/text/FieldPosition.java \
	java/text/Format.java \
	java/text/IOSCollator.java \
	java/text/MessageFormat.java \
	java/text/Normalizer.java \
	java/text/NumberFormat.java \
	java/text/ParseException.java \
	java/text/ParsePosition.java \
	java/text/SimpleDateFormat.java \
	java/util/AbstractCollection.java \
	java/util/AbstractList.java \
	java/util/AbstractMap.java \
	java/util/AbstractQueue.java \
	java/util/AbstractSequentialList.java \
	java/util/AbstractSet.java \
	java/util/ArrayDeque.java \
	java/util/ArrayList.java \
	java/util/Arrays.java \
	java/util/BitSet.java \
	java/util/Calendar.java \
	java/util/Collection.java \
	java/util/Collections.java \
	java/util/ComparableTimSort.java \
	java/util/Comparator.java \
	java/util/ConcurrentModificationException.java \
	java/util/Currency.java \
	java/util/Date.java \
	java/util/Deque.java \
	java/util/Dictionary.java \
	java/util/DualPivotQuicksort.java \
	java/util/DuplicateFormatFlagsException.java \
	java/util/EnumMap.java \
	java/util/EnumSet.java \
	java/util/Enumeration.java \
	java/util/EventObject.java \
	java/util/EventListener.java \
	java/util/EventListenerProxy.java \
	java/util/EmptyStackException.java \
	java/util/FormatFlagsConversionMismatchException.java \
	java/util/Formattable.java \
	java/util/FormattableFlags.java \
	java/util/Formatter.java \
	java/util/FormatterClosedException.java \
	java/util/Grego.java \
	java/util/GregorianCalendar.java \
	java/util/HashMap.java \
	java/util/HashSet.java \
	java/util/Hashtable.java \
	java/util/HugeEnumSet.java \
	java/util/IdentityHashMap.java \
	java/util/IllegalFormatException.java \
	java/util/IllegalFormatCodePointException.java \
	java/util/IllegalFormatConversionException.java \
	java/util/IllegalFormatFlagsException.java \
	java/util/IllegalFormatPrecisionException.java \
	java/util/IllegalFormatWidthException.java \
	java/util/InputMismatchException.java \
	java/util/InvalidPropertiesFormatException.java \
	java/util/Iterator.java \
	java/util/LinkedHashMap.java \
	java/util/LinkedList.java \
	java/util/LinkedHashSet.java \
	java/util/List.java \
	java/util/ListIterator.java \
	java/util/ListResourceBundle.java \
	java/util/Locale.java \
	java/util/Map.java \
	java/util/MapEntry.java \
	java/util/MiniEnumSet.java \
	java/util/MissingFormatArgumentException.java \
	java/util/MissingFormatWidthException.java \
	java/util/MissingResourceException.java \
	java/util/NavigableMap.java \
	java/util/NavigableSet.java \
	java/util/NoSuchElementException.java \
	java/util/Objects.java \
	java/util/Observer.java \
	java/util/Observable.java \
	java/util/PriorityQueue.java \
	java/util/Properties.java \
	java/util/PropertyPermission.java \
	java/util/PropertyResourceBundle.java \
	java/util/Queue.java \
	java/util/Random.java \
	java/util/RandomAccess.java \
	java/util/ResourceBundle.java \
	java/util/Scanner.java \
	java/util/ServiceConfigurationError.java \
	java/util/ServiceLoader.java \
	java/util/Set.java \
	java/util/SimpleTimeZone.java \
	java/util/SortedMap.java \
	java/util/SortedSet.java \
	java/util/Stack.java \
	java/util/StringTokenizer.java \
	java/util/Timer.java \
	java/util/TimerTask.java \
	java/util/TimeZone.java \
	java/util/TimSort.java \
	java/util/TooManyListenersException.java \
	java/util/TreeMap.java \
	java/util/TreeSet.java \
	java/util/UnknownFormatConversionException.java \
	java/util/UnknownFormatFlagsException.java \
	java/util/UnsafeArrayList.java \
	java/util/UUID.java \
	java/util/Vector.java \
	java/util/WeakHashMap.java \
	java/util/concurrent/AbstractExecutorService.java \
	java/util/concurrent/ArrayBlockingQueue.java \
	java/util/concurrent/BlockingDeque.java \
	java/util/concurrent/BlockingQueue.java \
	java/util/concurrent/BrokenBarrierException.java \
	java/util/concurrent/Callable.java \
	java/util/concurrent/CancellationException.java \
	java/util/concurrent/CompletionService.java \
	java/util/concurrent/ConcurrentHashMap.java \
	java/util/concurrent/ConcurrentLinkedDeque.java \
	java/util/concurrent/ConcurrentLinkedQueue.java \
	java/util/concurrent/ConcurrentMap.java \
	java/util/concurrent/ConcurrentNavigableMap.java \
	java/util/concurrent/ConcurrentSkipListMap.java \
	java/util/concurrent/ConcurrentSkipListSet.java \
	java/util/concurrent/CountDownLatch.java \
	java/util/concurrent/CopyOnWriteArrayList.java \
	java/util/concurrent/CopyOnWriteArraySet.java \
	java/util/concurrent/CyclicBarrier.java \
	java/util/concurrent/DelayQueue.java \
	java/util/concurrent/Delayed.java \
	java/util/concurrent/Exchanger.java \
	java/util/concurrent/Executor.java \
	java/util/concurrent/ExecutorCompletionService.java \
	java/util/concurrent/ExecutorService.java \
	java/util/concurrent/Executors.java \
	java/util/concurrent/ExecutionException.java \
	java/util/concurrent/ForkJoinPool.java \
	java/util/concurrent/ForkJoinTask.java \
	java/util/concurrent/ForkJoinWorkerThread.java \
	java/util/concurrent/Future.java \
	java/util/concurrent/FutureTask.java \
	java/util/concurrent/LinkedBlockingDeque.java \
	java/util/concurrent/LinkedBlockingQueue.java \
	java/util/concurrent/LinkedTransferQueue.java \
	java/util/concurrent/Phaser.java \
	java/util/concurrent/PriorityBlockingQueue.java \
	java/util/concurrent/RecursiveAction.java \
	java/util/concurrent/RecursiveTask.java \
	java/util/concurrent/RejectedExecutionException.java \
	java/util/concurrent/RejectedExecutionHandler.java \
	java/util/concurrent/RunnableFuture.java \
	java/util/concurrent/RunnableScheduledFuture.java \
	java/util/concurrent/ScheduledExecutorService.java \
	java/util/concurrent/ScheduledFuture.java \
	java/util/concurrent/ScheduledThreadPoolExecutor.java \
	java/util/concurrent/Semaphore.java \
	java/util/concurrent/SynchronousQueue.java \
	java/util/concurrent/ThreadFactory.java \
	java/util/concurrent/ThreadLocalRandom.java \
	java/util/concurrent/ThreadPoolExecutor.java \
	java/util/concurrent/TimeoutException.java \
	java/util/concurrent/TimeUnit.java \
	java/util/concurrent/TransferQueue.java \
	java/util/concurrent/atomic/AtomicBoolean.java \
	java/util/concurrent/atomic/AtomicInteger.java \
	java/util/concurrent/atomic/AtomicIntegerArray.java \
	java/util/concurrent/atomic/AtomicIntegerFieldUpdater.java \
	java/util/concurrent/atomic/AtomicLong.java \
	java/util/concurrent/atomic/AtomicLongArray.java \
	java/util/concurrent/atomic/AtomicLongFieldUpdater.java \
	java/util/concurrent/atomic/AtomicMarkableReference.java \
	java/util/concurrent/atomic/AtomicReference.java \
	java/util/concurrent/atomic/AtomicReferenceArray.java \
	java/util/concurrent/atomic/AtomicReferenceFieldUpdater.java \
	java/util/concurrent/atomic/AtomicStampedReference.java \
	java/util/concurrent/atomic/Fences.java \
	java/util/concurrent/locks/AbstractOwnableSynchronizer.java \
	java/util/concurrent/locks/AbstractQueuedLongSynchronizer.java \
	java/util/concurrent/locks/AbstractQueuedSynchronizer.java \
	java/util/concurrent/locks/Condition.java \
	java/util/concurrent/locks/Lock.java \
	java/util/concurrent/locks/LockSupport.java \
	java/util/concurrent/locks/ReadWriteLock.java \
	java/util/concurrent/locks/ReentrantLock.java \
	java/util/concurrent/locks/ReentrantReadWriteLock.java \
	java/util/logging/ConsoleHandler.java \
	java/util/logging/ErrorManager.java \
	java/util/logging/Filter.java \
	java/util/logging/Formatter.java \
	java/util/logging/Handler.java \
	java/util/logging/IOSLogHandler.java \
	java/util/logging/Level.java \
	java/util/logging/LogManager.java \
	java/util/logging/LogRecord.java \
	java/util/logging/Logger.java \
	java/util/logging/LoggingMXBean.java \
	java/util/logging/LoggingPermission.java \
	java/util/logging/MemoryHandler.java \
	java/util/logging/SimpleFormatter.java \
	java/util/logging/StreamHandler.java \
	java/util/logging/XMLFormatter.java \
	java/util/regex/Matcher.java \
	java/util/regex/MatchResult.java \
	java/util/regex/MatchResultImpl.java \
	java/util/regex/Pattern.java \
	java/util/regex/PatternSyntaxException.java \
	java/util/regex/Splitter.java \
	java/util/zip/Adler32.java \
	java/util/zip/CheckedInputStream.java \
	java/util/zip/CheckedOutputStream.java \
	java/util/zip/Checksum.java \
	java/util/zip/CRC32.java \
	java/util/zip/DataFormatException.java \
	java/util/zip/Deflater.java \
	java/util/zip/DeflaterInputStream.java \
	java/util/zip/DeflaterOutputStream.java \
	java/util/zip/GZIPInputStream.java \
	java/util/zip/GZIPOutputStream.java \
	java/util/zip/Inflater.java \
	java/util/zip/InflaterInputStream.java \
	java/util/zip/InflaterOutputStream.java \
	java/util/zip/ZipConstants.java \
	java/util/zip/ZipEntry.java \
	java/util/zip/ZipError.java \
	java/util/zip/ZipException.java \
	java/util/zip/ZipFile.java \
	java/util/zip/ZipInputStream.java \
	java/util/zip/ZipOutputStream.java \
	javax/annotation/Generated.java \
	javax/annotation/PostConstruct.java \
	javax/annotation/PreDestroy.java \
	javax/annotation/Resources.java \
	javax/lang/model/element/Modifier.java \
	javax/crypto/SecretKey.java \
	javax/net/DefaultServerSocketFactory.java \
	javax/net/DefaultSocketFactory.java \
	javax/net/ServerSocketFactory.java \
	javax/net/SocketFactory.java \
	javax/net/ssl/DefaultSSLServerSocketFactory.java \
	javax/net/ssl/DefaultSSLSocketFactory.java \
	javax/net/ssl/HandshakeCompletedEvent.java \
	javax/net/ssl/HandshakeCompletedListener.java \
	javax/net/ssl/HostnameVerifier.java \
	javax/net/ssl/HttpsURLConnection.java \
	javax/net/ssl/KeyManager.java \
	javax/net/ssl/KeyManagerFactory.java \
	javax/net/ssl/KeyManagerFactorySpi.java \
	javax/net/ssl/ManagerFactoryParameters.java \
	javax/net/ssl/SSLContext.java \
	javax/net/ssl/SSLContextSpi.java \
	javax/net/ssl/SSLEngine.java \
	javax/net/ssl/SSLEngineResult.java \
	javax/net/ssl/SSLException.java \
	javax/net/ssl/SSLHandshakeException.java \
	javax/net/ssl/SSLParameters.java \
	javax/net/ssl/SSLPeerUnverifiedException.java \
	javax/net/ssl/SSLProtocolException.java \
	javax/net/ssl/SSLServerSocket.java \
	javax/net/ssl/SSLServerSocketFactory.java \
	javax/net/ssl/SSLSession.java \
	javax/net/ssl/SSLSessionBindingEvent.java \
	javax/net/ssl/SSLSessionBindingListener.java \
	javax/net/ssl/SSLSessionContext.java \
	javax/net/ssl/SSLSocket.java \
	javax/net/ssl/SSLSocketFactory.java \
	javax/net/ssl/TrustManager.java \
	javax/net/ssl/TrustManagerFactory.java \
	javax/net/ssl/TrustManagerFactorySpi.java \
	javax/security/auth/Destroyable.java \
	javax/security/auth/DestroyFailedException.java \
	javax/security/auth/x500/X500Principal.java \
	javax/security/auth/callback/Callback.java \
	javax/security/auth/callback/CallbackHandler.java \
	javax/security/auth/callback/PasswordCallback.java \
	javax/security/auth/callback/UnsupportedCallbackException.java \
	javax/security/cert/Certificate.java \
	javax/security/cert/CertificateEncodingException.java \
	javax/security/cert/CertificateException.java \
	javax/security/cert/CertificateExpiredException.java \
	javax/security/cert/CertificateNotYetValidException.java \
	javax/security/cert/X509Certificate.java \
	javax/xml/XMLConstants.java \
	javax/xml/datatype/DatatypeConfigurationException.java \
	javax/xml/datatype/DatatypeConstants.java \
	javax/xml/datatype/DatatypeFactory.java \
	javax/xml/datatype/Duration.java \
	javax/xml/datatype/FactoryFinder.java \
	javax/xml/datatype/XMLGregorianCalendar.java \
	javax/xml/namespace/NamespaceContext.java \
	javax/xml/namespace/QName.java \
	javax/xml/parsers/DocumentBuilder.java \
	javax/xml/parsers/DocumentBuilderFactory.java \
	javax/xml/parsers/FactoryConfigurationError.java \
	javax/xml/parsers/FilePathToURI.java \
	javax/xml/parsers/ParserConfigurationException.java \
	javax/xml/parsers/SAXParser.java \
	javax/xml/parsers/SAXParserFactory.java \
	javax/xml/transform/dom/DOMLocator.java \
	javax/xml/transform/dom/DOMResult.java \
	javax/xml/transform/dom/DOMSource.java \
	javax/xml/transform/ErrorListener.java \
	javax/xml/transform/OutputKeys.java \
	javax/xml/transform/Result.java \
	javax/xml/transform/sax/SAXResult.java \
	javax/xml/transform/sax/SAXSource.java \
	javax/xml/transform/sax/SAXTransformerFactory.java \
	javax/xml/transform/sax/TemplatesHandler.java \
	javax/xml/transform/sax/TransformerHandler.java \
	javax/xml/transform/Source.java \
	javax/xml/transform/SourceLocator.java \
	javax/xml/transform/stream/FilePathToURI.java \
	javax/xml/transform/stream/StreamResult.java \
	javax/xml/transform/stream/StreamSource.java \
	javax/xml/transform/Templates.java \
	javax/xml/transform/Transformer.java \
	javax/xml/transform/TransformerConfigurationException.java \
	javax/xml/transform/TransformerException.java \
	javax/xml/transform/TransformerFactory.java \
	javax/xml/transform/TransformerFactoryConfigurationError.java \
	javax/xml/transform/URIResolver.java \
	javax/xml/validation/Schema.java \
	javax/xml/validation/SchemaFactory.java \
	javax/xml/validation/SchemaFactoryFinder.java \
	javax/xml/validation/SchemaFactoryLoader.java \
	javax/xml/validation/TypeInfoProvider.java \
	javax/xml/validation/Validator.java \
	javax/xml/validation/ValidatorHandler.java \
	javax/xml/xpath/XPath.java \
	javax/xml/xpath/XPathConstants.java \
	javax/xml/xpath/XPathException.java \
	javax/xml/xpath/XPathExpression.java \
	javax/xml/xpath/XPathExpressionException.java \
	javax/xml/xpath/XPathFactory.java \
	javax/xml/xpath/XPathFactoryConfigurationException.java \
	javax/xml/xpath/XPathFactoryFinder.java \
	javax/xml/xpath/XPathFunction.java \
	javax/xml/xpath/XPathFunctionException.java \
	javax/xml/xpath/XPathFunctionResolver.java \
	javax/xml/xpath/XPathVariableResolver.java \
	libcore/icu/ICU.java \
	libcore/icu/LocaleData.java \
	libcore/icu/NativeDecimalFormat.java \
	libcore/icu/TimeZoneNames.java \
	libcore/icu/TimeZones.java \
	libcore/internal/StringPool.java \
	libcore/io/AsynchronousCloseMonitor.java \
	libcore/io/BufferIterator.java \
	libcore/io/DeleteOnExit.java \
	libcore/io/ErrnoException.java \
	libcore/io/GaiException.java \
	libcore/io/HeapBufferIterator.java \
	libcore/io/IoBridge.java \
	libcore/io/IoUtils.java \
	libcore/io/Libcore.java \
	libcore/io/Memory.java \
	libcore/io/Os.java \
	libcore/io/OsConstants.java \
	libcore/io/Posix.java \
	libcore/io/SizeOf.java \
	libcore/io/Streams.java \
	libcore/io/StructAddrinfo.java \
	libcore/io/StructFlock.java \
	libcore/io/StructGroupReq.java \
	libcore/io/StructGroupSourceReq.java \
	libcore/io/StructLinger.java \
	libcore/io/StructPollfd.java \
	libcore/io/StructStat.java \
	libcore/io/StructStatVfs.java \
	libcore/io/StructTimeval.java \
	libcore/io/StructUtsname.java \
	libcore/net/UriCodec.java \
	libcore/net/MimeUtils.java \
	libcore/net/http/HttpDate.java \
	libcore/net/url/FileHandler.java \
	libcore/net/url/FileURLConnection.java \
	libcore/net/url/UrlUtils.java \
	libcore/reflect/GenericArrayTypeImpl.java \
	libcore/reflect/GenericSignatureParser.java \
	libcore/reflect/ListOfTypes.java \
	libcore/reflect/ListOfVariables.java \
	libcore/reflect/ParameterizedTypeImpl.java \
	libcore/reflect/Types.java \
	libcore/reflect/TypeVariableImpl.java \
	libcore/reflect/WildcardTypeImpl.java \
	libcore/util/BasicLruCache.java \
	libcore/util/EmptyArray.java \
	libcore/util/MutableInt.java \
	libcore/util/MutableLong.java \
	libcore/util/Objects.java \
	libcore/util/SneakyThrow.java \
	org/apache/harmony/security/PrivateKeyImpl.java \
	org/apache/harmony/security/PublicKeyImpl.java \
	org/apache/harmony/security/asn1/ASN1Any.java \
	org/apache/harmony/security/asn1/ASN1Choice.java \
	org/apache/harmony/security/asn1/ASN1Constants.java \
	org/apache/harmony/security/asn1/ASN1Constructed.java \
	org/apache/harmony/security/asn1/ASN1Exception.java \
	org/apache/harmony/security/asn1/ASN1Explicit.java \
	org/apache/harmony/security/asn1/ASN1Integer.java \
	org/apache/harmony/security/asn1/ASN1Oid.java \
	org/apache/harmony/security/asn1/ASN1Primitive.java \
	org/apache/harmony/security/asn1/ASN1Sequence.java \
	org/apache/harmony/security/asn1/ASN1SequenceOf.java \
	org/apache/harmony/security/asn1/ASN1Set.java \
	org/apache/harmony/security/asn1/ASN1SetOf.java \
	org/apache/harmony/security/asn1/ASN1StringType.java \
	org/apache/harmony/security/asn1/ASN1Time.java \
	org/apache/harmony/security/asn1/ASN1Type.java \
	org/apache/harmony/security/asn1/ASN1TypeCollection.java \
	org/apache/harmony/security/asn1/ASN1UTCTime.java \
	org/apache/harmony/security/asn1/ASN1ValueCollection.java \
	org/apache/harmony/security/asn1/BerInputStream.java \
	org/apache/harmony/security/asn1/BerOutputStream.java \
	org/apache/harmony/security/asn1/BitString.java \
	org/apache/harmony/security/asn1/DerInputStream.java \
	org/apache/harmony/security/asn1/DerOutputStream.java \
	org/apache/harmony/security/asn1/ObjectIdentifier.java \
	org/apache/harmony/security/fortress/Engine.java \
	org/apache/harmony/security/fortress/SecurityAccess.java \
	org/apache/harmony/security/fortress/Services.java \
	org/apache/harmony/security/utils/ObjectIdentifier.java \
	org/apache/harmony/security/x501/AttributeTypeAndValue.java \
	org/apache/harmony/security/x501/AttributeTypeAndValueComparator.java \
	org/apache/harmony/security/x501/AttributeValue.java \
	org/apache/harmony/security/x501/DirectoryString.java \
	org/apache/harmony/security/x501/Name.java \
	org/apache/harmony/security/x509/DNParser.java \
	org/apache/harmony/xml/dom/AttrImpl.java \
	org/apache/harmony/xml/dom/CDATASectionImpl.java \
	org/apache/harmony/xml/dom/CharacterDataImpl.java \
	org/apache/harmony/xml/dom/CommentImpl.java \
	org/apache/harmony/xml/dom/DocumentFragmentImpl.java \
	org/apache/harmony/xml/dom/DocumentImpl.java \
	org/apache/harmony/xml/dom/DocumentTypeImpl.java \
	org/apache/harmony/xml/dom/DOMConfigurationImpl.java \
	org/apache/harmony/xml/dom/DOMErrorImpl.java \
	org/apache/harmony/xml/dom/DOMImplementationImpl.java \
	org/apache/harmony/xml/dom/ElementImpl.java \
	org/apache/harmony/xml/dom/EntityImpl.java \
	org/apache/harmony/xml/dom/EntityReferenceImpl.java \
	org/apache/harmony/xml/dom/InnerNodeImpl.java \
	org/apache/harmony/xml/dom/LeafNodeImpl.java \
	org/apache/harmony/xml/dom/NodeImpl.java \
	org/apache/harmony/xml/dom/NodeListImpl.java \
	org/apache/harmony/xml/dom/NotationImpl.java \
	org/apache/harmony/xml/dom/ProcessingInstructionImpl.java \
	org/apache/harmony/xml/dom/TextImpl.java \
	org/apache/harmony/xml/parsers/DocumentBuilderFactoryImpl.java \
	org/apache/harmony/xml/parsers/DocumentBuilderImpl.java \
	org/apache/harmony/xml/parsers/SAXParserFactoryImpl.java \
	org/apache/harmony/xml/parsers/SAXParserImpl.java \
	org/json/JSON.java \
	org/json/JSONArray.java \
	org/json/JSONException.java \
	org/json/JSONObject.java \
	org/json/JSONStringer.java \
	org/json/JSONTokener.java \
	org/kxml2/io/KXmlParser.java \
	org/kxml2/io/KXmlSerializer.java \
	org/w3c/dom/Attr.java \
	org/w3c/dom/CDATASection.java \
	org/w3c/dom/CharacterData.java \
	org/w3c/dom/Comment.java \
	org/w3c/dom/Document.java \
	org/w3c/dom/DocumentFragment.java \
	org/w3c/dom/DocumentType.java \
	org/w3c/dom/DOMConfiguration.java \
	org/w3c/dom/DOMError.java \
	org/w3c/dom/DOMErrorHandler.java \
	org/w3c/dom/DOMException.java \
	org/w3c/dom/DOMImplementation.java \
	org/w3c/dom/DOMImplementationList.java \
	org/w3c/dom/DOMImplementationSource.java \
	org/w3c/dom/DOMLocator.java \
	org/w3c/dom/DOMStringList.java \
	org/w3c/dom/Element.java \
	org/w3c/dom/Entity.java \
	org/w3c/dom/EntityReference.java \
	org/w3c/dom/NamedNodeMap.java \
	org/w3c/dom/NameList.java \
	org/w3c/dom/Node.java \
	org/w3c/dom/NodeList.java \
	org/w3c/dom/Notation.java \
	org/w3c/dom/ProcessingInstruction.java \
	org/w3c/dom/Text.java \
	org/w3c/dom/TypeInfo.java \
	org/w3c/dom/UserDataHandler.java \
	org/w3c/dom/ls/DOMImplementationLS.java \
	org/w3c/dom/ls/LSException.java \
	org/w3c/dom/ls/LSInput.java \
	org/w3c/dom/ls/LSOutput.java \
	org/w3c/dom/ls/LSParser.java \
	org/w3c/dom/ls/LSParserFilter.java \
	org/w3c/dom/ls/LSResourceResolver.java \
	org/w3c/dom/ls/LSSerializer.java \
	org/w3c/dom/ls/LSSerializerFilter.java \
	org/w3c/dom/traversal/NodeFilter.java \
	org/w3c/dom/traversal/NodeIterator.java \
	org/xml/sax/AttributeList.java \
	org/xml/sax/Attributes.java \
	org/xml/sax/ContentHandler.java \
	org/xml/sax/DocumentHandler.java \
	org/xml/sax/DTDHandler.java \
	org/xml/sax/EntityResolver.java \
	org/xml/sax/ErrorHandler.java \
	org/xml/sax/HandlerBase.java \
	org/xml/sax/InputSource.java \
	org/xml/sax/Locator.java \
	org/xml/sax/Parser.java \
	org/xml/sax/SAXException.java \
	org/xml/sax/SAXNotRecognizedException.java \
	org/xml/sax/SAXNotSupportedException.java \
	org/xml/sax/SAXParseException.java \
	org/xml/sax/XMLFilter.java \
	org/xml/sax/XMLReader.java \
	org/xml/sax/ext/Attributes2.java \
	org/xml/sax/ext/Attributes2Impl.java \
	org/xml/sax/ext/DeclHandler.java \
	org/xml/sax/ext/DefaultHandler2.java \
	org/xml/sax/ext/EntityResolver2.java \
	org/xml/sax/ext/LexicalHandler.java \
	org/xml/sax/ext/Locator2.java \
	org/xml/sax/ext/Locator2Impl.java \
	org/xml/sax/helpers/AttributeListImpl.java \
	org/xml/sax/helpers/AttributesImpl.java \
	org/xml/sax/helpers/DefaultHandler.java \
	org/xml/sax/helpers/LocatorImpl.java \
	org/xml/sax/helpers/NamespaceSupport.java \
	org/xml/sax/helpers/NewInstance.java \
	org/xml/sax/helpers/ParserAdapter.java \
	org/xml/sax/helpers/ParserFactory.java \
	org/xml/sax/helpers/XMLFilterImpl.java \
	org/xml/sax/helpers/XMLReaderAdapter.java \
	org/xml/sax/helpers/XMLReaderFactory.java \
	org/xmlpull/v1/XmlPullParser.java \
	org/xmlpull/v1/XmlPullParserException.java \
	org/xmlpull/v1/XmlPullParserFactory.java \
	org/xmlpull/v1/XmlSerializer.java \
	org/xmlpull/v1/sax2/Driver.java \
	sun/misc/Unsafe.java

MAIN_OBJ = J2ObjCMain.o

JAVA_SOURCES_MANIFEST = $(BUILD_DIR)/java_sources.mf
OBJC_SOURCES_MANIFEST = $(BUILD_DIR)/objc_sources.mf

STUB_JAVA_SOURCES := $(shell find $(STUBS_DIR) -name *.java)
ALL_JAVA_SOURCES = $(JAVA_SOURCES) $(IOS_JAVA_OBJS:.o=.java) $(JAR_ONLY_SOURCES)
JAVA_TO_TRANSLATE = $(JAVA_SOURCES)
SOURCE_HEADERS := $(shell find $(EMULATION_CLASS_DIR) -name *.h)
INCLUDES = $(JAVA_TO_TRANSLATE:.java=.h) $(SOURCE_HEADERS:$(EMULATION_CLASS_DIR)/%=%)
DIST_INCLUDES = $(INCLUDES:%=$(DIST_INCLUDE_DIR)/%)
ARCH_INCLUDES = $(INCLUDES:%=$(ARCH_INCLUDE_DIR)/%)

ANNOTATIONS_JAR = $(DIST_JAR_DIR)/j2objc_annotations.jar

MAIN_LIB_OBJS = $(MAIN_OBJ:%=$(CLASS_DIR)/%)

# translate is the default so that Xcode can build its own object files
default: libs
	@: # suppress make's "nothing to be done" message

GEN_OBJC_DIR = $(TRANSLATED_SOURCE_DIR)
TRANSLATE_NAME = jre_emul
TRANSLATE_JAVA_FULL = $(JAVA_TO_TRANSLATE)
TRANSLATE_JAVA_RELATIVE = $(JAVA_TO_TRANSLATE)
TRANSLATE_ARGS = -q -sourcepath $(JRE_SRC) -classpath $(EMULATION_JAR) \
  $(J2OBJC_DEBUGFLAGS) -encoding UTF-8 --extract-unsequenced --batch-translate-max=300 \
  -J-Xmx2G --final-methods-as-functions --hide-private-members
TRANSLATE_USE_SYSTEM_BOOT_PATH = TRUE
include ../make/translate.mk

FAT_LIB_NAME = jre_emul
FAT_LIB_SOURCES_RELATIVE = $(JAVA_SOURCES:.java=.m) $(IOS_OBJS:.o=.m) $(IOS_JAVA_OBJS:.o=.m) \
  $(APPLE_SRCS)
FAT_LIB_SOURCE_DIRS = $(EMULATION_CLASS_DIR) $(GEN_OBJC_DIR) $(APPLE_ROOT) $(ANDROID_NATIVE_DIR)

# OBJCPP_BUILD is an optional environment variable for Objective-C++ build experiments.
# J2ObjC and app releases should not be built with this flag, nor is its use supported.
ifeq ($(OBJCPP_BUILD), YES)
FAT_LIB_COMPILE = $(CLANG) $(OBJCPPFLAGS) -I$(EMULATION_CLASS_DIR) -I$(GEN_OBJC_DIR) \
  -I$(ANDROID_NATIVE_DIR)
# TODO(tball): add support for Obj-C++ precompiled header.
else
FAT_LIB_COMPILE = $(CLANG) $(OBJCFLAGS) -I$(EMULATION_CLASS_DIR) -I$(GEN_OBJC_DIR) \
  -I$(ANDROID_NATIVE_DIR)
#FAT_LIB_PRECOMPILED_HEADER = JreEmulation.h
endif
include ../make/fat_lib.mk

fat_lib_dependencies: translate

libs: $(FAT_LIB_LIBRARY) $(MAIN_LIB)
	@:

xcode_build: $(EMULATION_LIB_DIST) $(ARCH_BIN_DIR)/j2objcc $(ARCH_BUILD_DIR)/.includes

xcode_clean:
	@rm -rf $(GEN_OBJC_DIR) $(ARCH_BIN_DIR)/j2objcc

xcode_analyze: analyze

$(MAIN_LIB): $(MAIN_LIB_OBJS)
	@echo $(LIBTOOL) -o $@
	@$(LIBTOOL) -static -o $@ $^

dist_includes: $(BUILD_DIR)/.jre_dist_includes

$(BUILD_DIR)/.jre_dist_includes: $(TRANSLATE_HEADERS) $(SOURCE_HEADERS) | \
    $(DIST_INCLUDE_DIR)
	@echo copying jre_emul header files
	@(cd $(EMULATION_CLASS_DIR) && tar cf - $(SOURCE_HEADERS:$(EMULATION_CLASS_DIR)/%=%)) \
	    | (cd $(DIST_INCLUDE_DIR); tar xfp -)
	@(cd $(GEN_OBJC_DIR) && tar cf - \
	    $(TRANSLATE_HEADERS:$(GEN_OBJC_DIR)/%=%)) \
	    | (cd $(DIST_INCLUDE_DIR); tar xfp -)
	@touch $@

$(ARCH_BUILD_DIR)/.includes: $(TRANSLATE_HEADERS) $(SOURCE_HEADERS) | \
    $(ARCH_BUILD_DIR) $(ARCH_INCLUDE_DIR)
	@(cd $(EMULATION_CLASS_DIR) && tar cf - $(SOURCE_HEADERS:$(EMULATION_CLASS_DIR)/%=%)) \
	    | (cd $(ARCH_INCLUDE_DIR); tar xfp -)
	@(cd $(GEN_OBJC_DIR) && tar cf - \
	    $(TRANSLATE_HEADERS:$(GEN_OBJC_DIR)/%=%)) \
	    | (cd $(ARCH_INCLUDE_DIR); tar xfp -)
	@touch $@

%/j2objcc: $(J2OBJC_ROOT)/scripts/j2objcc.sh
	@mkdir -p $(@D)
	@install -C $< $@

DIRS_TO_MAKE = $(BUILD_DIR) $(ARCH_BUILD_DIR) $(ARCH_INCLUDE_DIR) \
    $(DIST_INCLUDE_DIR) $(DIST_JAR_DIR) $(DIST_LIB_DIR)
$(sort $(DIRS_TO_MAKE)):
	@mkdir -p $@

$(CLASS_DIR)/%.o: $(EMULATION_CLASS_DIR)/%.m | translate
	@mkdir -p $(@D)
	@echo clang -c $<
	@$(CLANG) $(OBJCFLAGS) -I$(EMULATION_CLASS_DIR) -I$(GEN_OBJC_DIR) $(SDK_FLAGS) -c $< -o $@

test: libs_dist $(DIST_DIR)/j2objcc
	$(MAKE) -f tests.mk test

clean:
	@rm -rf $(BUILD_DIR) $(GEN_OBJC_DIR)
	@rm -f $(DIST_DIR)/j2objcc $(EMULATION_JAR_DIST) $(EMULATION_LIB_DIST) $(MAIN_LIB_DIST)
	@rm -f $(DIST_INCLUDES)

dist: emul_jar_dist libs_dist $(DIST_DIR)/j2objcc

libs_dist: libs dist_includes $(EMULATION_LIB_DIST) $(MAIN_LIB_DIST)

$(EMULATION_LIB_DIST): $(FAT_LIB_LIBRARY) | $(DIST_LIB_DIR)
	@install -m 0644 $< $@

$(MAIN_LIB_DIST): $(MAIN_LIB) | $(DIST_LIB_DIR)
	@install -m 0644 $< $@

java: emul_jar_dist java_sources_manifest

emul_jar_dist: $(EMULATION_JAR_DIST)
	@:

$(EMULATION_JAR_DIST): $(EMULATION_JAR) | $(DIST_JAR_DIR)
	@install -m 0644 $< $@

$(EMULATION_JAR): $(ALL_JAVA_SOURCES) $(STUB_JAVA_SOURCES) | $(BUILD_DIR)
	@echo "building jre_emul.jar"
	@rm -rf $(EMULATION_STAGE)
	@mkdir $(EMULATION_STAGE)
	@bash ../scripts/javac_no_deprecated_warnings.sh $(JAVAC) -bootclasspath $(STUBS_DIR) \
	    -classpath $(ANNOTATIONS_JAR) \
	    -d $(EMULATION_STAGE) -encoding UTF-8 -source 1.7 -target 1.7 -nowarn $^
	@jar cf $(EMULATION_JAR) -C $(EMULATION_STAGE) .

$(JAVA_SOURCES_MANIFEST): $(ALL_JAVA_SOURCES) $(STUB_JAVA_SOURCES) | $(BUILD_DIR)
	@echo "building $$(basename $@)"
	@if [ -e $@ ]; then rm $@; fi
	@for i in $^; do echo $(CURDIR)/$$i >> $@; done

java_sources_manifest: $(JAVA_SOURCES_MANIFEST)
	@:

$(OBJC_SOURCES_MANIFEST): $(ALL_JAVA_SOURCES) | $(BUILD_DIR)
	@echo "building $$(basename $@)"
	@if [ -e $@ ]; then rm $@; fi
	@for i in $(JAVA_SOURCES); do \
	  echo $(GEN_OBJC_DIR)/$${i%.java}.h >> $@; \
	  echo $(GEN_OBJC_DIR)/$${i%.java}.m >> $@; \
	done

objc_sources_manifest: $(OBJC_SOURCES_MANIFEST)

find_cycles: cycle_finder_dist $(JAVA_SOURCES_MANIFEST)
	$(DIST_DIR)/cycle_finder -w cycle_whitelist.txt `cat $(JAVA_SOURCES_MANIFEST)`

