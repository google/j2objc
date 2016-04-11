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

# Makefile for building and running the iOS emulation library unit tests.
#
# Author: Tom Ball

.SUFFIXES:

default: test

include environment.mk
include $(J2OBJC_ROOT)/make/translate_macros.mk

SUPPORT_SOURCES = \
    JSR166TestCase.java \
    android/test/MoreAsserts.java \
    android/text/SpannableTest.java \
    bar/Third.java \
    com/google/j2objc/NativeUtil.java \
    com/google/j2objc/TestAnnotation.java \
    com/google/j2objc/java8/Lambdas.java \
    com/google/j2objc/mappedpkg/TestClass.java \
    com/google/j2objc/mappedpkg/package-info.java \
    com/google/j2objc/package-info.java \
    com/google/mockwebserver/Dispatcher.java \
    com/google/mockwebserver/MockResponse.java \
    com/google/mockwebserver/MockWebServer.java \
    com/google/mockwebserver/QueueDispatcher.java \
    com/google/mockwebserver/RecordedRequest.java \
    com/google/mockwebserver/SocketPolicy.java \
    foo/Fourth.java \
    foo/bar/First.java \
    foo/mumble/Second.java \
    java/lang/test/Example.java \
    java/lang/test/package-info.java \
    jsr166/BlockingQueueTest.java \
    jsr166/JSR166TestCase.java \
    libcore/java/net/customstreamhandler/http/Handler.java \
    libcore/java/nio/charset/Charset_TestGenerator.java \
    libcore/java/nio/charset/OldCharset_AbstractTest.java \
    libcore/java/util/ServiceLoaderTestInterface.java \
    libcore/java/util/zip/AbstractZipFileTest.java \
    libcore/javax/net/ssl/TestSSLContext.java \
    libcore/util/SerializationTester.java \
    okio/MockSink.java \
    okio/TestUtil.java \
    org/apache/harmony/beans/tests/support/MisprintBean.java \
    org/apache/harmony/beans/tests/support/MisprintEvent.java \
    org/apache/harmony/beans/tests/support/MisprintListenerr.java \
    org/apache/harmony/beans/tests/support/OtherBean.java \
    org/apache/harmony/beans/tests/support/SampleBean.java \
    org/apache/harmony/beans/tests/support/SampleEvent.java \
    org/apache/harmony/beans/tests/support/SampleListener.java \
    org/apache/harmony/beans/tests/support/mock/FakeFox.java \
    org/apache/harmony/beans/tests/support/mock/FakeFox01.java \
    org/apache/harmony/beans/tests/support/mock/FakeFox011.java \
    org/apache/harmony/beans/tests/support/mock/FakeFox01BeanInfo.java \
    org/apache/harmony/beans/tests/support/mock/FakeFox02.java \
    org/apache/harmony/beans/tests/support/mock/FakeFox02BeanInfo.java \
    org/apache/harmony/beans/tests/support/mock/FakeFox031.java \
    org/apache/harmony/beans/tests/support/mock/FakeFox04.java \
    org/apache/harmony/beans/tests/support/mock/FakeFox041.java \
    org/apache/harmony/beans/tests/support/mock/FakeFox0411.java \
    org/apache/harmony/beans/tests/support/mock/MockButton.java \
    org/apache/harmony/beans/tests/support/mock/MockFakeEvent.java \
    org/apache/harmony/beans/tests/support/mock/MockFakeListener.java \
    org/apache/harmony/beans/tests/support/mock/MockFoo.java \
    org/apache/harmony/beans/tests/support/mock/MockFooButton.java \
    org/apache/harmony/beans/tests/support/mock/MockFooChild.java \
    org/apache/harmony/beans/tests/support/mock/MockFooLabel.java \
    org/apache/harmony/beans/tests/support/mock/MockFooStop.java \
    org/apache/harmony/beans/tests/support/mock/MockFooSub.java \
    org/apache/harmony/beans/tests/support/mock/MockFooSubSub.java \
    org/apache/harmony/beans/tests/support/mock/MockInterface.java \
    org/apache/harmony/beans/tests/support/mock/MockJavaBean.java \
    org/apache/harmony/beans/tests/support/mock/MockNullSubClass.java \
    org/apache/harmony/beans/tests/support/mock/MockNullSuperClass.java \
    org/apache/harmony/beans/tests/support/mock/MockPropertyChangeEvent.java \
    org/apache/harmony/beans/tests/support/mock/MockPropertyChangeListener.java \
    org/apache/harmony/beans/tests/support/mock/MockPropertyChangeListener2.java \
    org/apache/harmony/beans/tests/support/mock/MockPropertyChangeValidListener.java \
    org/apache/harmony/beans/tests/support/mock/MockSubClass.java \
    org/apache/harmony/beans/tests/support/mock/MockSuperClass.java \
    org/apache/harmony/beans/tests/support/mock/homonymy/mocksubject1/info/MockHomonymySubjectBeanInfo.java \
    org/apache/harmony/beans/tests/support/mock/homonymy/mocksubject1/MockHomonymySubject.java \
    org/apache/harmony/beans/tests/support/mock/homonymy/mocksubject2/info/MockHomonymySubjectBeanInfo.java \
    org/apache/harmony/beans/tests/support/mock/homonymy/mocksubject2/MockHomonymySubject.java \
    org/apache/harmony/logging/tests/java/util/logging/LevelTestResource.java \
    org/apache/harmony/logging/tests/java/util/logging/util/EnvironmentHelper.java \
    org/apache/harmony/luni/tests/java/lang/MockEnum.java \
    org/apache/harmony/luni/tests/java/lang/MockEnum2.java \
    org/apache/harmony/security/tests/support/MyAlgorithmParameterGeneratorSpi.java \
    org/apache/harmony/security/tests/support/MyKeyPairGeneratorSpi.java \
    org/apache/harmony/security/tests/support/MyMessageDigest1.java \
    org/apache/harmony/security/tests/support/MySignature1.java \
    org/apache/harmony/security/tests/support/SpiEngUtils.java \
    org/apache/harmony/security/tests/support/TestKeyPair.java \
    org/apache/harmony/security/tests/support/spec/MyEncodedKeySpec.java \
    org/apache/harmony/testframework/serialization/SerializationTest.java \
    org/apache/harmony/tests/java/lang/reflect/GenericReflectionTestsBase.java \
    org/apache/harmony/tests/javax/xml/parsers/SAXParserTestSupport.java \
    org/apache/harmony/tests/org/xml/sax/support/MethodLogger.java \
    org/apache/harmony/tests/org/xml/sax/support/MockHandler.java \
    org/apache/harmony/tests/org/xml/sax/support/MockResolver.java \
    org/apache/harmony/text/tests/java/text/Support_Format.java \
    org/apache/harmony/text/tests/java/text/Support_MessageFormat.java \
    tests/security/MessageDigestTest.java \
    tests/security/interfaces/Util.java \
    tests/support/Support_ASimpleInputStream.java \
    tests/support/Support_ASimpleOutputStream.java \
    tests/support/Support_ASimpleReader.java \
    tests/support/Support_ASimpleWriter.java \
    tests/support/Support_CollectionTest.java \
    tests/support/Support_Configuration.java \
    tests/support/Support_Field.java \
    tests/support/Support_GetPutFields.java \
    tests/support/Support_GetPutFieldsDefaulted.java \
    tests/support/Support_GetPutFieldsDeprecated.java \
    tests/support/Support_ListTest.java \
    tests/support/Support_Locale.java \
    tests/support/Support_MapTest2.java \
    tests/support/Support_OutputStream.java \
    tests/support/Support_PlatformFile.java \
    tests/support/Support_Proxy_I1.java \
    tests/support/Support_Proxy_I2.java \
    tests/support/Support_Proxy_ParentException.java \
    tests/support/Support_Proxy_SubException.java \
    tests/support/Support_SetTest.java \
    tests/support/Support_StringReader.java \
    tests/support/Support_StringWriter.java \
    tests/support/Support_TestResource.java \
    tests/support/Support_TestResource_en.java \
    tests/support/Support_TestResource_en_US.java \
    tests/support/Support_TestResource_fr.java \
    tests/support/Support_TestResource_fr_FR.java \
    tests/support/Support_TestResource_fr_FR_VAR.java \
    tests/support/Support_TimeZone.java \
    tests/support/Support_UnmodifiableCollectionTest.java \
    tests/support/Support_UnmodifiableMapTest.java \
    tests/support/ThrowingReader.java \
    tests/support/resource/Support_Resources.java \
    tests/targets/security/KeyStoreTest.java \
    tests/util/CallVerificationStack.java \
    tests/util/SerializationTester.java

NATIVE_SOURCES = dalvik_system_JniTest.cpp

TEST_SOURCES := \
    AbstractExecutorServiceTest.java \
    AbstractQueuedSynchronizerTest.java \
    AbstractQueueTest.java \
    ArrayBlockingQueueTest.java \
    AtomicBooleanTest.java \
    AtomicIntegerArrayTest.java \
    AtomicIntegerFieldUpdaterTest.java \
    AtomicIntegerTest.java \
    AtomicLongArrayTest.java \
    AtomicLongFieldUpdaterTest.java \
    AtomicLongTest.java \
    AtomicMarkableReferenceTest.java \
    AtomicReferenceArrayTest.java \
    AtomicReferenceFieldUpdaterTest.java \
    AtomicReferenceTest.java \
    AtomicStampedReferenceTest.java \
    ConcurrentHashMapTest.java \
    ConcurrentLinkedQueueTest.java \
    CopyOnWriteArrayListTest.java \
    CopyOnWriteArraySetTest.java \
    CountDownLatchTest.java \
    CyclicBarrierTest.java \
    DelayQueueTest.java \
    ExchangerTest.java \
    ExecutorCompletionServiceTest.java \
    ExecutorsTest.java \
    FutureTaskTest.java \
    LinkedBlockingQueueTest.java \
    LinkedListTest.java \
    LockSupportTest.java \
    MaxFloatingPointTest.java \
    NoPackageTest.java \
    PackagePrefixesTest.java \
    PriorityBlockingQueueTest.java \
    PriorityQueueTest.java \
    ReentrantLockTest.java \
    ReentrantReadWriteLockTest.java \
    SerializationTest.java \
    ScheduledExecutorTest.java \
    SynchronousQueueTest.java \
    SystemTest.java \
    ThreadPoolExecutorTest.java \
    TimeUnitTest.java \
    UTF16EncodingTest.java \
    android/text/SpannableStringBuilderTest.java \
    android/text/SpannableStringTest.java \
    android/text/TextUtilsTest.java \
    android/util/Base64Test.java \
    com/google/j2objc/ArrayTest.java \
    com/google/j2objc/AssertTest.java \
    com/google/j2objc/ClassTest.java \
    com/google/j2objc/LinkedListTest.java \
    com/google/j2objc/MemoryTest.java \
    com/google/j2objc/PackageTest.java \
    com/google/j2objc/ThrowableTest.java \
    com/google/j2objc/net/NSErrorExceptionTest.java \
    com/google/j2objc/security/IosSecureRandomImplTest.java \
    dalvik/system/JniTest.java \
    java/lang/SystemTest.java \
    java/lang/ref/PhantomReferenceTest.java \
    java/lang/ref/SoftReferenceTest.java \
    java/lang/ref/WeakReferenceTest.java \
    java/lang/reflect/MethodTest.java \
    java/lang/reflect/ProxyTest.java \
    java/lang/reflect/ReflectionTest.java \
    java/util/TreeMapTest.java \
    java/util/WeakHashMapTest.java \
    java/util/regex/MatcherTest.java \
    jsr166/LinkedTransferQueueTest.java \
    libcore/icu/ICUTest.java \
    libcore/icu/LocaleDataTest.java \
    libcore/icu/NativeDecimalFormatTest.java \
    libcore/io/MemoryTest.java \
    libcore/io/PosixTest.java \
    libcore/java/awt/font/TextAttributeTest.java \
    libcore/java/io/CharArrayWriterTest.java \
    libcore/java/io/DataOutputStreamTest.java \
    libcore/java/io/FileDescriptorTest.java \
    libcore/java/io/FileInputStreamTest.java \
    libcore/java/io/FileOutputStreamTest.java \
    libcore/java/io/FileTest.java \
    libcore/java/io/InputStreamReaderTest.java \
    libcore/java/io/ObjectOutputStreamTest.java \
    libcore/java/io/OldAndroidBufferedInputStreamTest.java \
    libcore/java/io/OldAndroidBufferedOutputStreamTest.java \
    libcore/java/io/OldAndroidBufferedReaderTest.java \
    libcore/java/io/OldAndroidBufferedWriterTest.java \
    libcore/java/io/OldAndroidByteArrayInputStreamTest.java \
    libcore/java/io/OldAndroidByteArrayOutputStreamTest.java \
    libcore/java/io/OldAndroidCharArrayReaderTest.java \
    libcore/java/io/OldAndroidDataInputStreamTest.java \
    libcore/java/io/OldAndroidDataOutputStreamTest.java \
    libcore/java/io/OldAndroidFileTest.java \
    libcore/java/io/OldAndroidInputStreamReaderTest.java \
    libcore/java/io/OldAndroidLineNumberReaderTest.java \
    libcore/java/io/OldAndroidOutputStreamWriterTest.java \
    libcore/java/io/OldAndroidPipedStreamTest.java \
    libcore/java/io/OldAndroidPushbackInputStreamTest.java \
    libcore/java/io/OldAndroidPushbackReaderTest.java \
    libcore/java/io/OldAndroidSerializationTest.java \
    libcore/java/io/OldAndroidStreamTokenizerTest.java \
    libcore/java/io/OldAndroidStringReaderTest.java \
    libcore/java/io/OldAndroidStringWriterTest.java \
    libcore/java/io/OldBufferedInputStreamTest.java \
    libcore/java/io/OldBufferedOutputStreamTest.java \
    libcore/java/io/OldBufferedReaderTest.java \
    libcore/java/io/OldBufferedWriterTest.java \
    libcore/java/io/OldByteArrayInputStreamTest.java \
    libcore/java/io/OldByteArrayOutputStreamTest.java \
    libcore/java/io/OldCharArrayReaderTest.java \
    libcore/java/io/OldCharArrayWriterTest.java \
    libcore/java/io/OldDataInputOutputStreamTest.java \
    libcore/java/io/OldDataInputStreamTest.java \
    libcore/java/io/OldDataOutputStreamTest.java \
    libcore/java/io/OldFileInputStreamTest.java \
    libcore/java/io/OldFileReaderTest.java \
    libcore/java/io/OldFileTest.java \
    libcore/java/io/OldFileWriterTest.java \
    libcore/java/io/OldFilterInputStreamTest.java \
    libcore/java/io/OldFilterOutputStreamTest.java \
    libcore/java/io/OldFilterReaderTest.java \
    libcore/java/io/OldFilterWriterTest.java \
    libcore/java/io/OldInputStreamReaderTest.java \
    libcore/java/io/OldInputStreamTest.java \
    libcore/java/io/OldLineNumberInputStreamTest.java \
    libcore/java/io/OldLineNumberReaderTest.java \
    libcore/java/io/OldObjectInputOutputStreamTest.java \
    libcore/java/io/OldObjectInputStreamGetFieldTest.java \
    libcore/java/io/OldObjectOutputStreamTest.java \
    libcore/java/io/OldObjectStreamFieldTest.java \
    libcore/java/io/OldOutputStreamTest.java \
    libcore/java/io/OldPipedOutputStreamTest.java \
    libcore/java/io/OldPipedWriterTest.java \
    libcore/java/io/OldPushbackInputStreamTest.java \
    libcore/java/io/OldPushbackReaderTest.java \
    libcore/java/io/OldRandomAccessFileTest.java \
    libcore/java/io/OldReaderTest.java \
    libcore/java/io/OldSequenceInputStreamTest.java \
    libcore/java/io/OldStreamTokenizerTest.java \
    libcore/java/io/OldStringBufferInputStreamTest.java \
    libcore/java/io/OldStringReaderTest.java \
    libcore/java/io/OldStringWriterTest.java \
    libcore/java/io/OldWriterTest.java \
    libcore/java/io/OutputStreamWriterTest.java \
    libcore/java/io/SerializationTest.java \
    libcore/java/io/StreamTokenizerTest.java \
    libcore/java/lang/CharacterTest.java \
    libcore/java/lang/ClassTest.java \
    libcore/java/lang/DoubleTest.java \
    libcore/java/lang/EnumTest.java \
    libcore/java/lang/FloatTest.java \
    libcore/java/lang/IntegralToStringTest.java \
    libcore/java/lang/ThrowableTest.java \
    libcore/java/lang/annotation/AnnotationTypeMismatchExceptionTest.java \
    libcore/java/lang/reflect/AnnotationsTest.java \
    libcore/java/lang/reflect/ArrayTest.java \
    libcore/java/lang/reflect/ConstructorTest.java \
    libcore/java/lang/reflect/FieldTest.java \
    libcore/java/lang/reflect/MethodTest.java \
    libcore/java/lang/reflect/ReflectionTest.java \
    libcore/java/net/CookiesTest.java \
    libcore/java/net/DatagramSocketTest.java \
    libcore/java/net/NetworkInterfaceTest.java \
    libcore/java/net/OldAuthenticatorTest.java \
    libcore/java/net/OldPasswordAuthenticationTest.java \
    libcore/java/net/ServerSocketTest.java \
    libcore/java/net/SocketTest.java \
    libcore/java/net/URITest.java \
    libcore/java/net/URLConnectionTest.java \
    libcore/java/net/UrlEncodingTest.java \
    libcore/java/net/URLStreamHandlerFactoryTest.java \
    libcore/java/net/URLTest.java \
    libcore/java/nio/BufferTest.java \
    libcore/java/nio/NoArrayTest.java \
    libcore/java/nio/OldAndroidNIOTest.java \
    libcore/java/nio/OldDirectIntBufferTest.java \
    libcore/java/nio/OldDirectShortBufferTest.java \
    libcore/java/nio/channels/FileChannelTest.java \
    libcore/java/nio/channels/OldFileChannelTest.java \
    libcore/java/nio/channels/DatagramChannelTest.java \
    libcore/java/nio/charset/CharsetDecoderTest.java \
    libcore/java/nio/charset/CharsetEncoderTest.java \
    libcore/java/text/AttributedCharacterIteratorAttributeTest.java \
    libcore/java/text/CollatorTest.java \
    libcore/java/text/DecimalFormatSymbolsTest.java \
    libcore/java/text/DecimalFormatTest.java \
    libcore/java/text/NormalizerTest.java \
    libcore/java/text/NumberFormatTest.java \
    libcore/java/text/SimpleDateFormatTest.java \
    libcore/java/util/BitSetTest.java \
    libcore/java/util/CalendarTest.java \
    libcore/java/util/EventObjectTest.java \
    libcore/java/util/FormatterTest.java \
    libcore/java/util/ObjectsTest.java \
    libcore/java/util/OldAndroidArrayListTest.java \
    libcore/java/util/OldTimeZoneTest.java \
    libcore/java/util/ServiceLoaderTest.java \
    libcore/java/util/TreeMapTest.java \
    libcore/java/util/TreeSetTest.java \
    libcore/java/util/zip/DeflaterInputStreamTest.java \
    libcore/java/util/zip/DeflaterOutputStreamTest.java \
    libcore/java/util/zip/DeflaterTest.java \
    libcore/java/util/zip/GZIPInputStreamTest.java \
    libcore/java/util/zip/GZIPOutputStreamTest.java \
    libcore/java/util/zip/InflaterTest.java \
    libcore/java/util/zip/OldAndroidChecksumTest.java \
    libcore/java/util/zip/OldAndroidDeflateTest.java \
    libcore/java/util/zip/OldAndroidGZIPStreamTest.java \
    libcore/java/util/zip/OldAndroidZipStreamTest.java \
    libcore/java/util/zip/OldDataFormatExceptionTest.java \
    libcore/java/util/zip/OldZipExceptionTest.java \
    libcore/java/util/zip/Zip64FileTest.java \
    libcore/java/util/zip/ZipEntryTest.java \
    libcore/java/util/zip/ZipFileTest.java \
    libcore/java/util/zip/ZipInputStreamTest.java \
    libcore/java/util/zip/ZipOutputStreamTest.java \
    libcore/javax/xml/parsers/DocumentBuilderTest.java \
    libcore/net/url/UrlUtilsTest.java \
    okio/AsyncTimeoutTest.java \
    okio/BufferedSinkTest.java \
    okio/BufferedSourceTest.java \
    okio/BufferTest.java \
    okio/ByteStringTest.java \
    okio/OkioTest.java \
    okio/RealBufferedSinkTest.java \
    okio/RealBufferedSourceTest.java \
    okio/SegmentSharingTest.java \
    okio/Utf8Test.java \
    org/apache/harmony/archive/tests/java/util/zip/CRC32Test.java \
    org/apache/harmony/beans/tests/java/beans/BeanDescriptorTest.java \
    org/apache/harmony/beans/tests/java/beans/EventSetDescriptorTest.java \
    org/apache/harmony/beans/tests/java/beans/FeatureDescriptorTest.java \
    org/apache/harmony/beans/tests/java/beans/IndexedPropertyDescriptorTest.java \
    org/apache/harmony/beans/tests/java/beans/IntrospectorTest.java \
    org/apache/harmony/beans/tests/java/beans/IntrospectionExceptionTest.java \
    org/apache/harmony/beans/tests/java/beans/MethodDescriptorTest.java \
    org/apache/harmony/beans/tests/java/beans/ParameterDescriptorTest.java \
    org/apache/harmony/beans/tests/java/beans/PropertyChangeEventTest.java \
    org/apache/harmony/beans/tests/java/beans/PropertyChangeListenerProxyTest.java \
    org/apache/harmony/beans/tests/java/beans/PropertyDescriptorTest.java \
    org/apache/harmony/beans/tests/java/beans/PropertyVetoExceptionTest.java \
    org/apache/harmony/beans/tests/java/beans/SimpleBeanInfoTest.java \
    org/apache/harmony/logging/tests/java/util/logging/ConsoleHandlerTest.java \
    org/apache/harmony/logging/tests/java/util/logging/ErrorManagerTest.java \
    org/apache/harmony/logging/tests/java/util/logging/FilterTest.java \
    org/apache/harmony/logging/tests/java/util/logging/FormatterTest.java \
    org/apache/harmony/logging/tests/java/util/logging/HandlerTest.java \
    org/apache/harmony/logging/tests/java/util/logging/LevelTest.java \
    org/apache/harmony/logging/tests/java/util/logging/LogManagerTest.java \
    org/apache/harmony/logging/tests/java/util/logging/LogRecordTest.java \
    org/apache/harmony/logging/tests/java/util/logging/LoggerTest.java \
    org/apache/harmony/logging/tests/java/util/logging/MemoryHandlerTest.java \
    org/apache/harmony/logging/tests/java/util/logging/SimpleFormatterTest.java \
    org/apache/harmony/logging/tests/java/util/logging/StreamHandlerTest.java \
    org/apache/harmony/logging/tests/java/util/logging/XMLFormatterTest.java \
    org/apache/harmony/luni/tests/java/lang/ArithmeticExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/ArrayIndexOutOfBoundsExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/ArrayStoreExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/AssertionErrorTest.java \
    org/apache/harmony/luni/tests/java/lang/BooleanTest.java \
    org/apache/harmony/luni/tests/java/lang/ByteTest.java \
    org/apache/harmony/luni/tests/java/lang/ClassCastExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/ClassNotFoundExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/ClassTest.java \
    org/apache/harmony/luni/tests/java/lang/CloneNotSupportedExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/ErrorTest.java \
    org/apache/harmony/luni/tests/java/lang/EnumTest.java \
    org/apache/harmony/luni/tests/java/lang/ExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/IllegalAccessExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/IllegalArgumentExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/IllegalMonitorStateExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/IllegalStateExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/IllegalThreadStateExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/IndexOutOfBoundsExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/InstantiationExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/IntegerTest.java \
    org/apache/harmony/luni/tests/java/lang/InterruptedExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/LongTest.java \
    org/apache/harmony/luni/tests/java/lang/NegativeArraySizeExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/NoSuchFieldErrorTest.java \
    org/apache/harmony/luni/tests/java/lang/NoSuchFieldExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/NoSuchMethodErrorTest.java \
    org/apache/harmony/luni/tests/java/lang/NoSuchMethodExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/NullPointerExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/NumberFormatExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/NumberTest.java \
    org/apache/harmony/luni/tests/java/lang/ObjectTest.java \
    org/apache/harmony/luni/tests/java/lang/OutOfMemoryErrorTest.java \
    org/apache/harmony/luni/tests/java/lang/reflect/ArrayTest.java \
    org/apache/harmony/luni/tests/java/lang/reflect/FieldTest.java \
    org/apache/harmony/luni/tests/java/lang/reflect/ModifierTest.java \
    org/apache/harmony/luni/tests/java/lang/RuntimeExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/ShortTest.java \
    org/apache/harmony/luni/tests/java/lang/StringBufferTest.java \
    org/apache/harmony/luni/tests/java/lang/StringBuilderTest.java \
    org/apache/harmony/luni/tests/java/lang/StringIndexOutOfBoundsExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/StringTest.java \
    org/apache/harmony/luni/tests/java/lang/String2Test.java \
    org/apache/harmony/luni/tests/java/lang/ThreadDeathTest.java \
    org/apache/harmony/luni/tests/java/lang/ThreadTest.java \
    org/apache/harmony/luni/tests/java/lang/TypeNotPresentExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/UnsupportedOperationExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/VirtualMachineErrorTest.java \
    org/apache/harmony/luni/tests/java/util/AbstractCollectionTest.java \
    org/apache/harmony/luni/tests/java/util/AbstractListTest.java \
    org/apache/harmony/luni/tests/java/util/AbstractMapTest.java \
    org/apache/harmony/luni/tests/java/util/AbstractQueueTest.java \
    org/apache/harmony/luni/tests/java/util/AbstractSequentialListTest.java \
    org/apache/harmony/luni/tests/java/util/ArrayListTest.java \
    org/apache/harmony/luni/tests/java/util/ArraysTest.java \
    org/apache/harmony/luni/tests/java/util/CollectionsTest.java \
    org/apache/harmony/luni/tests/java/util/DateTest.java \
    org/apache/harmony/luni/tests/java/util/EmptyStackExceptionTest.java \
    org/apache/harmony/luni/tests/java/util/EnumSetTest.java \
    org/apache/harmony/luni/tests/java/util/EventObjectTest.java \
    org/apache/harmony/luni/tests/java/util/GregorianCalendarTest.java \
    org/apache/harmony/luni/tests/java/util/HashSetTest.java \
    org/apache/harmony/luni/tests/java/util/IdentityHashMapTest.java \
    org/apache/harmony/luni/tests/java/util/LinkedHashSetTest.java \
    org/apache/harmony/luni/tests/java/util/LinkedListTest.java \
    org/apache/harmony/luni/tests/java/util/LocaleTest.java \
    org/apache/harmony/luni/tests/java/util/NoSuchElementExceptionTest.java \
    org/apache/harmony/luni/tests/java/util/PriorityQueueTest.java \
    org/apache/harmony/luni/tests/java/util/RandomTest.java \
    org/apache/harmony/luni/tests/java/util/SimpleTimeZoneTest.java \
    org/apache/harmony/luni/tests/java/util/StackTest.java \
    org/apache/harmony/luni/tests/java/util/StringTokenizerTest.java \
    org/apache/harmony/luni/tests/java/util/TimeZoneTest.java \
    org/apache/harmony/luni/tests/java/util/TooManyListenersExceptionTest.java \
    org/apache/harmony/luni/tests/java/util/TreeMapTest.java \
    org/apache/harmony/luni/tests/java/util/TreeSetTest.java \
    org/apache/harmony/luni/tests/java/util/UUIDTest.java \
    org/apache/harmony/luni/tests/java/util/VectorTest.java \
    org/apache/harmony/regex/tests/java/util/regex/Matcher2Test.java \
    org/apache/harmony/regex/tests/java/util/regex/ModeTest.java \
    org/apache/harmony/regex/tests/java/util/regex/Pattern2Test.java \
    org/apache/harmony/regex/tests/java/util/regex/PatternSyntaxExceptionTest.java \
    org/apache/harmony/regex/tests/java/util/regex/PatternTest.java \
    org/apache/harmony/regex/tests/java/util/regex/ReplaceTest.java \
    org/apache/harmony/regex/tests/java/util/regex/SplitTest.java \
    org/apache/harmony/security/tests/java/security/AlgorithmParametersSpiTest.java \
    org/apache/harmony/security/tests/java/security/AlgorithmParametersTest.java \
    org/apache/harmony/security/tests/java/security/KeyFactorySpiTest.java \
    org/apache/harmony/security/tests/java/security/KeyFactoryTest.java \
    org/apache/harmony/security/tests/java/security/KeyPairGeneratorSpiTest.java \
    org/apache/harmony/security/tests/java/security/KeyPairTest.java \
    org/apache/harmony/security/tests/java/security/KeyTest.java \
    org/apache/harmony/security/tests/java/security/MessageDigest1Test.java \
    org/apache/harmony/security/tests/java/security/MessageDigest2Test.java \
    org/apache/harmony/security/tests/java/security/MessageDigestSpiTest.java \
    org/apache/harmony/security/tests/java/security/PrivateKeyTest.java \
    org/apache/harmony/security/tests/java/security/ProviderExceptionTest.java \
    org/apache/harmony/security/tests/java/security/PublicKeyTest.java \
    org/apache/harmony/security/tests/java/security/SecureRandom2Test.java \
    org/apache/harmony/security/tests/java/security/SecureRandomSpiTest.java \
    org/apache/harmony/security/tests/java/security/SignatureSpiTest.java \
    org/apache/harmony/security/tests/java/security/SignatureTest.java \
    org/apache/harmony/tests/java/io/ObjectStreamClassTest.java \
    org/apache/harmony/tests/java/io/ObjectStreamConstantsTest.java \
    org/apache/harmony/tests/java/io/ObjectStreamFieldTest.java \
    org/apache/harmony/tests/java/io/RandomAccessFileTest.java \
    org/apache/harmony/tests/java/lang/CharacterTest.java \
    org/apache/harmony/tests/java/lang/DoubleTest.java \
    org/apache/harmony/tests/java/lang/FloatTest.java \
    org/apache/harmony/tests/java/lang/MathTest.java \
    org/apache/harmony/tests/java/lang/StrictMathTest.java \
    org/apache/harmony/tests/java/lang/reflect/AccessibleObjectTest.java \
    org/apache/harmony/tests/java/lang/reflect/ArrayTest.java \
    org/apache/harmony/tests/java/lang/reflect/BoundedGenericMethodsTests.java \
    org/apache/harmony/tests/java/lang/reflect/ConstructorTest.java \
    org/apache/harmony/tests/java/lang/reflect/FieldTest.java \
    org/apache/harmony/tests/java/lang/reflect/GenericArrayTypeTest.java \
    org/apache/harmony/tests/java/lang/reflect/GenericMethodsTests.java \
    org/apache/harmony/tests/java/lang/reflect/MalformedParameterizedTypeExceptionTest.java \
    org/apache/harmony/tests/java/lang/reflect/MalformedParameterizedTypeExceptionTests.java \
    org/apache/harmony/tests/java/lang/reflect/MethodTest.java \
    org/apache/harmony/tests/java/lang/reflect/ModifierTest.java \
    org/apache/harmony/tests/java/lang/reflect/ParameterizedTypeTest.java \
    org/apache/harmony/tests/java/lang/reflect/TypeVariableTest.java \
    org/apache/harmony/tests/java/lang/reflect/UndeclaredThrowableExceptionTest.java \
    org/apache/harmony/tests/java/lang/reflect/UndeclaredThrowableExceptionTests.java \
    org/apache/harmony/tests/java/lang/reflect/WildcardTypeTest.java \
    org/apache/harmony/tests/java/math/BigIntegerAddTest.java \
    org/apache/harmony/tests/java/math/BigIntegerAndTest.java \
    org/apache/harmony/tests/java/math/BigIntegerCompareTest.java \
    org/apache/harmony/tests/java/math/BigIntegerConstructorsTest.java \
    org/apache/harmony/tests/java/math/BigIntegerConvertTest.java \
    org/apache/harmony/tests/java/math/BigIntegerDivideTest.java \
    org/apache/harmony/tests/java/math/BigIntegerHashCodeTest.java \
    org/apache/harmony/tests/java/math/BigIntegerMultiplyTest.java \
    org/apache/harmony/tests/java/math/BigIntegerNotTest.java \
    org/apache/harmony/tests/java/math/BigIntegerOrTest.java \
    org/apache/harmony/tests/java/math/BigIntegerSubtractTest.java \
    org/apache/harmony/tests/java/nio/channels/ChannelsTest.java \
    org/apache/harmony/tests/java/util/HashMapTest.java \
    org/apache/harmony/tests/java/util/HashtableTest.java \
    org/apache/harmony/tests/java/util/LinkedHashMapTest.java \
    org/apache/harmony/tests/javax/xml/parsers/DocumentBuilderFactoryTest.java \
    org/apache/harmony/tests/javax/xml/parsers/FactoryConfigurationErrorTest.java \
    org/apache/harmony/tests/javax/xml/parsers/ParserConfigurationExceptionTest.java \
    org/apache/harmony/text/tests/java/text/ChoiceFormatTest.java \
    org/apache/harmony/text/tests/java/text/CollatorTest.java \
    org/apache/harmony/text/tests/java/text/MessageFormatTest.java \
    org/json/JSONArrayTest.java \
    org/json/JSONObjectTest.java \
    org/json/JSONStringerTest.java \
    org/json/JSONTokenerTest.java \
    org/json/ParsingTest.java \
    org/json/SelfUseTest.java \
    tests/api/java/lang/reflect/ProxyTest.java \
    tests/api/java/util/AbstractMapTest.java \
    tests/api/java/util/BitSetTest.java \
    tests/api/java/util/CalendarTest.java \
    tests/api/java/util/FormattableFlagsTest.java \
    tests/api/java/util/ListResourceBundleTest.java \
    tests/api/java/util/ObservableTest.java \
    tests/api/java/util/PropertyResourceBundleTest.java \
    tests/api/java/util/ResourceBundleTest.java \
    tests/api/java/util/TimerTaskTest.java \
    tests/api/java/util/TimerTest.java \
    tests/java/security/AlgorithmParameterGeneratorSpiTest.java \
    tests/security/interfaces/RSAKeyTest.java \
    tests/security/interfaces/RSAPrivateKeyTest.java \
    tests/security/interfaces/RSAPublicKeyTest.java \
    tests/security/spec/ECFieldF2mTest.java \
    tests/security/spec/ECFieldFpTest.java \
    tests/security/spec/ECGenParameterSpecTest.java \
    tests/security/spec/ECParameterSpecTest.java \
    tests/security/spec/ECPointTest.java \
    tests/security/spec/ECPrivateKeySpecTest.java \
    tests/security/spec/ECPublicKeySpecTest.java \
    tests/security/spec/EllipticCurveTest.java \
    tests/security/spec/InvalidKeySpecExceptionTest.java \
    tests/security/spec/InvalidParameterSpecExceptionTest.java \
    tests/security/spec/MGF1ParameterSpecTest.java \
    tests/security/spec/PKCS8EncodedKeySpecTest.java \
    tests/security/spec/PSSParameterSpecTest.java \
    tests/security/spec/RSAKeyGenParameterSpecTest.java \
    tests/security/spec/RSAMultiPrimePrivateCrtKeySpecTest.java \
    tests/security/spec/RSAOtherPrimeInfoTest.java \
    tests/security/spec/RSAPrivateCrtKeySpecTest.java \
    tests/security/spec/RSAPrivateKeySpecTest.java \
    tests/security/spec/RSAPublicKeySpecTest.java \
    tests/security/spec/X509EncodedKeySpecTest.java \
    tests/targets/security/MessageDigestTestMD5.java \
    tests/targets/security/MessageDigestTestSHA1.java \
    tests/targets/security/MessageDigestTestSHA256.java \
    tests/targets/security/MessageDigestTestSHA384.java \
    tests/targets/security/MessageDigestTestSHA512.java \

SUITE_SOURCES = \
    ConcurrencyTests.java \
    libcore/java/io/SmallTests.java \
    libcore/java/text/LargeTests.java \
    libcore/java/text/SmallTests.java \
    libcore/java/util/zip/LargeTests.java \
    libcore/java/util/zip/SmallTests.java \
    org/apache/harmony/beans/tests/java/beans/AllTests.java \
    org/apache/harmony/logging/tests/java/util/logging/AllTests.java \
    org/json/SmallTests.java \

ARC_TEST_SOURCES = \
    com/google/j2objc/arc/EnumTest.java

# Lists tests that should be copied from Tests/com/google/j2objc to be
# translated and compiled with ARC.
COPIED_ARC_TEST_SOURCES = \
    com/google/j2objc/arc/MemoryTest.java


JAVA8_TEST_SOURCES := \
    com/google/j2objc/java8/CreationReferenceTest.java \
    com/google/j2objc/java8/DefaultMethodsTest.java \
    com/google/j2objc/java8/ExpressionMethodReferenceTest.java \
    com/google/j2objc/java8/LambdaTest.java \
    com/google/j2objc/java8/SuperMethodReferenceTest.java \
    com/google/j2objc/java8/TypeMethodReferenceTest.java \


JAVA8_SUITE_SOURCES = \
    com/google/j2objc/java8/SmallTests.java \


ALL_TEST_SOURCES = $(TEST_SOURCES) $(JAVA8_TEST_SOURCES) $(ARC_TEST_SOURCES) \
  $(COPIED_ARC_TEST_SOURCES)
ALL_SUITE_SOURCES = $(SUITE_SOURCES) $(JAVA8_SUITE_SOURCES)

# These tests fail when run on Travis-CI continuous build, probably due to VM sandbox restrictions.
# The java.net SmallTests is also skipped, since it refers to these classes; SmallTests isn't
# run in a continuous build, just from the command-line.
ifndef TRAVIS
TEST_SOURCES := $(TEST_SOURCES) \
    libcore/java/net/InetAddressTest.java \
    libcore/java/net/InetSocketAddressTest.java
SUITE_SOURCES := $(SUITE_SOURCES) \
    libcore/java/net/SmallTests.java
endif

TESTS_TO_SKIP = \
    ExchangerTest.java

FAILING_TESTS = \
    libcore/java/text/DateFormatSymbolsTest.java

# Most of these tests are failing for a common index-out-of-range error.
FAILING_MATH_TESTS = \
    org/apache/harmony/tests/java/math/BigDecimalArithmeticTest.java \
    org/apache/harmony/tests/java/math/BigDecimalCompareTest.java \
    org/apache/harmony/tests/java/math/BigDecimalConstructorsTest.java \
    org/apache/harmony/tests/java/math/BigDecimalConvertTest.java \
    org/apache/harmony/tests/java/math/BigDecimalScaleOperationsTest.java \
    tests/api/java/math/BigDecimalTest.java \
    org/apache/harmony/tests/java/math/BigIntegerModPowTest.java \
    org/apache/harmony/tests/java/math/BigIntegerOperateBitsTest.java \
    org/apache/harmony/tests/java/math/BigIntegerToStringTest.java \
    org/apache/harmony/tests/java/math/BigIntegerXorTest.java \
    tests/api/java/math/BigIntegerTest.java \

TESTS_TO_RUN = $(filter-out $(TESTS_TO_SKIP),$(ALL_TEST_SOURCES))
TESTS_TO_RUN := $(subst /,.,$(TESTS_TO_RUN:%.java=%))

ALL_TESTS_CLASS = AllJreTests
# Creates a test suit that includes all classes in ALL_TEST_SOURCES.
ALL_TESTS_SOURCE = $(RELATIVE_TESTS_DIR)/AllJreTests.java

SUPPORT_OBJS = $(SUPPORT_SOURCES:%.java=$(TESTS_DIR)/%.o) $(NATIVE_SOURCES:%.cpp=$(TESTS_DIR)/%.o)
TEST_OBJS = \
    $(ALL_TEST_SOURCES:%.java=$(TESTS_DIR)/%.o) \
    $(ALL_SUITE_SOURCES:%.java=$(TESTS_DIR)/%.o) \
    $(TESTS_DIR)/$(ALL_TESTS_CLASS).o

TEST_RESOURCES_SRCS = \
    lib/logging.properties \
    prefixes.properties
HARMONY_TEST_RESOURCES_SRCS = \
    org/apache/harmony/luni/tests/test_resource.txt \
    org/apache/harmony/luni/tests/java/lang/test_resource.txt \
    org/apache/harmony/luni/tests/java/io/testfile-utf8.txt \
    serialization/org/apache/harmony/luni/tests/java/lang/EnumTest.harmony.ser \
    serialization/org/apache/harmony/luni/tests/java/lang/EnumTest.golden.0.ser \
    serialization/org/apache/harmony/luni/tests/java/lang/EnumTest.golden.1.ser \
    serialization/org/apache/harmony/luni/tests/java/lang/EnumTest.golden.2.ser \
    serialization/org/apache/harmony/luni/tests/java/lang/EnumTest.golden.3.ser \
    serialization/org/apache/harmony/regex/tests/java/util/regex/PatternSyntaxExceptionTest.golden.ser
ANDROID_TEST_RESOURCES_SRCS = \
    META-INF/services/libcore.java.util.ServiceLoaderTestInterface \
    tests/targets/security/MD5.check \
    tests/targets/security/MD5.data \
    tests/targets/security/SHA-1.check \
    tests/targets/security/SHA-1.data \
    tests/targets/security/SHA-256.check \
    tests/targets/security/SHA-256.data \
    tests/targets/security/SHA-384.check \
    tests/targets/security/SHA-384.data \
    tests/targets/security/SHA-512.check \
    tests/targets/security/SHA-512.data \
    recipt.xml \
    simple.xml \
    systemid.xml \
    wrong.xml \
    tests/api/java/io/testFields.ser \
    tests/api/java/io/testFieldsDefaulted.ser \
    tests/api/java/io/testFieldsDeprecated.ser
LOGGING_TEST_RESOURCES_SRCS = \
    bundles/java/util/logging/res.properties \
    bundles/java/util/logging/res2.properties \
    bundles/java/util/logging/res3.properties \
    bundles/java/util/logging/res_en_US.properties \
    bundles/java/util/logging/res_zh_CN.properties \
    config/java/util/logging/logging.config
ZIP_TEST_RESOURCES_SRCS = \
    tests/resources/java/util/zip/EmptyArchive.zip \
    tests/resources/java/util/zip/ZipFileBreak.zip
BEANS_TEST_RESOURCES_SRCS = \
    serialization/org/apache/harmony/beans/tests/java/beans/IntrospectionExceptionTest.golden.ser \
    serialization/org/apache/harmony/beans/tests/java/beans/PropertyChangeEventTest.golden.ser \
    serialization/org/apache/harmony/beans/tests/java/beans/PropertyVetoExceptionTest.golden.ser

HARMONY_TEST_RESOURCES_ROOT = apache_harmony/classlib/modules/luni/src/test/resources
ANDROID_TEST_RESOURCES_ROOT = android/libcore/luni/src/test/resources
LOGGING_TEST_RESOURCES_ROOT = apache_harmony/classlib/modules/logging/src/test/resources
BEANS_TEST_RESOURCES_ROOT = apache_harmony/classlib/modules/beans/src/test/resources
TEST_RESOURCES_ROOT = Tests/resources

TEST_RESOURCES = \
    $(TEST_RESOURCES_SRCS:%=$(RESOURCES_DEST_DIR)/%) \
    $(ANDROID_TEST_RESOURCES_SRCS:%=$(RESOURCES_DEST_DIR)/%) \
    $(HARMONY_TEST_RESOURCES_SRCS:%=$(RESOURCES_DEST_DIR)/%) \
    $(LOGGING_TEST_RESOURCES_SRCS:%=$(RESOURCES_DEST_DIR)/%) \
    $(ZIP_TEST_RESOURCES_SRCS:%=$(RESOURCES_DEST_DIR)/%) \
    $(BEANS_TEST_RESOURCES_SRCS:%=$(RESOURCES_DEST_DIR)/%)

# Broken tests, plus associated bug id.  Once bug is fixed, move line(s) up.
# $(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/StringBuilderTest.o     b/8842295

JUNIT_DIST_JAR = $(DIST_JAR_DIR)/$(JUNIT_JAR)

INCLUDE_DIRS = $(TESTS_DIR) $(TESTS_DIR)/arc $(CLASS_DIR) $(EMULATION_CLASS_DIR)
INCLUDE_ARGS = $(INCLUDE_DIRS:%=-I%)

TEST_JOCC := ../dist/j2objcc -g
LINK_FLAGS := -ljre_emul -l junit -L$(TESTS_DIR) -l test-support
COMPILE_FLAGS := $(INCLUDE_ARGS) -c -Wno-objc-redundant-literal-use -Wno-format -Werror \
  -Wno-parentheses

ifeq ($(OBJCPP_BUILD), YES)
LINK_FLAGS += -lc++ -ObjC++
else
LINK_FLAGS += -ObjC
endif

SUPPORT_LIB = $(TESTS_DIR)/libtest-support.a
TEST_BIN = $(TESTS_DIR)/jre_unit_tests

TRANSLATE_ARGS = -q -classpath $(JUNIT_DIST_JAR) -Werror -sourcepath $(TEST_SRC):$(GEN_JAVA_DIR) \
    --extract-unsequenced -encoding UTF-8 \
    --prefixes Tests/resources/prefixes.properties
TRANSLATE_SOURCES = $(SUPPORT_SOURCES) $(TEST_SOURCES) $(SUITE_SOURCES) $(ALL_TESTS_CLASS).java
TRANSLATE_SOURCES_JAVA8 = $(JAVA8_TEST_SOURCES) $(JAVA8_SUITE_SOURCES)
TRANSLATE_SOURCES_ARC = $(ARC_TEST_SOURCES) $(COPIED_ARC_TEST_SOURCES)
TRANSLATED_OBJC = $(TRANSLATE_SOURCES:%.java=$(TESTS_DIR)/%.m)
TRANSLATED_OBJC_JAVA8 = $(TRANSLATE_SOURCES_JAVA8:%.java=$(TESTS_DIR)/%.m)
TRANSLATED_OBJC_ARC = $(TRANSLATE_SOURCES_ARC:%.java=$(TESTS_DIR)/arc/%.m)

TRANSLATE_ARTIFACT := $(call emit_translate_rule,\
  jre_emul_tests,\
  $(TESTS_DIR),\
  $(SUPPORT_SOURCES) $(TEST_SOURCES) $(SUITE_SOURCES) $(ALL_TESTS_SOURCE),\
  ,\
  $(TRANSLATE_ARGS))

TRANSLATE_ARTIFACT_JAVA8 := $(call emit_translate_rule,\
  jre_emul_tests_java8,\
  $(TESTS_DIR),\
  $(JAVA8_TEST_SOURCES) $(JAVA8_SUITE_SOURCES),\
  ,\
  $(TRANSLATE_ARGS) -source 8)

TRANSLATE_ARTIFACT_ARC := $(call emit_translate_rule,\
  jre_emul_tests_arc,\
  $(TESTS_DIR)/arc,\
  $(ARC_TEST_SOURCES) $(COPIED_ARC_TEST_SOURCES:%=$(GEN_JAVA_DIR)/%),\
  ,\
  $(TRANSLATE_ARGS) -use-arc)

TRANSLATE_ARTIFACTS = $(TRANSLATE_ARTIFACT) $(TRANSLATE_ARTIFACT_JAVA8) $(TRANSLATE_ARTIFACT_ARC)

$(TRANSLATED_OBJC): $(TRANSLATE_ARTIFACT)
	@:

$(TRANSLATED_OBJC_JAVA8): $(TRANSLATE_ARTIFACT_JAVA8)
	@:

$(TRANSLATED_OBJC_ARC): $(TRANSLATE_ARTIFACT_ARC)
	@:

ifdef GENERATE_TEST_COVERAGE
TEST_JOCC += -ftest-coverage -fprofile-arcs
endif

all-tests: test run-xctests

test: run-tests

support-lib: $(SUPPORT_LIB)

build: support-lib $(TEST_OBJS)
	@:

translate-all: translate
	@:

link: build $(TEST_BIN)

resources: $(TEST_RESOURCES)
	@:

$(RESOURCES_DEST_DIR)/%: $(TEST_RESOURCES_ROOT)/%
	@mkdir -p `dirname $@`
	@cp $< $@

$(RESOURCES_DEST_DIR)/%: $(ANDROID_TEST_RESOURCES_ROOT)/%
	@mkdir -p `dirname $@`
	@cp $< $@

$(RESOURCES_DEST_DIR)/%: $(LOGGING_TEST_RESOURCES_ROOT)/%
	@mkdir -p `dirname $@`
	@cp $< $@

$(RESOURCES_DEST_DIR)/%: $(BEANS_TEST_RESOURCES_ROOT)/%
	@mkdir -p `dirname $@`
	@cp $< $@

$(RESOURCES_DEST_DIR)/%: $(HARMONY_TEST_RESOURCES_ROOT)/%
	@mkdir -p `dirname $@`
	@cp $< $@

# A bug in make 3.81 causes subprocesses to inherit a generous amount of stack.
# This distorts the fact that the default stack size is 8 MB for a 64-bit OS X
# binary. Work around with ulimit override.
#
# See http://stackoverflow.com/questions/16279867/gmake-change-the-stack-size-limit
# and https://savannah.gnu.org/bugs/?22010
run-tests: link resources $(TEST_BIN) run-initialization-test run-core-size-test
	@ulimit -s 8192 && $(TEST_BIN) org.junit.runner.JUnitCore $(ALL_TESTS_CLASS)

run-initialization-test: $(TESTS_DIR)/jreinitialization
	@$(TESTS_DIR)/jreinitialization > /dev/null 2>&1

run-core-size-test: $(TESTS_DIR)/core_size \
  $(TESTS_DIR)/full_jre_size \
  $(TESTS_DIR)/core_plus_android_util \
  $(TESTS_DIR)/core_plus_beans \
  $(TESTS_DIR)/core_plus_channels \
  $(TESTS_DIR)/core_plus_concurrent \
  $(TESTS_DIR)/core_plus_io \
  $(TESTS_DIR)/core_plus_net \
  $(TESTS_DIR)/core_plus_security \
  $(TESTS_DIR)/core_plus_sql \
  $(TESTS_DIR)/core_plus_ssl \
  $(TESTS_DIR)/core_plus_util \
  $(TESTS_DIR)/core_plus_xml \
  $(TESTS_DIR)/core_plus_zip
	@for bin in $^; do \
	  echo Binary size for $$(basename $$bin):; \
	  ls -l $$bin; \
	  echo Number of classes: `nm $$bin | grep -c "S _OBJC_CLASS_"`; \
	done

run-beans-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore org.apache.harmony.beans.tests.java.beans.AllTests

run-concurrency-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore ConcurrencyTests

run-io-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore libcore.java.io.SmallTests

run-json-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore org.json.SmallTests

run-java8-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore com.google.j2objc.java8.SmallTests

run-logging-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore \
	    org.apache.harmony.logging.tests.java.util.logging.AllTests

run-net-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore libcore.java.net.SmallTests

run-text-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore libcore.java.text.SmallTests libcore.java.text.LargeTests

run-zip-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore libcore.java.util.zip.SmallTests

run-zip-tests-large: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore libcore.java.util.zip.LargeTests

# Run this when the above has errors and JUnit doesn't report which
# test failed or hung.
run-each-test: link resources $(TEST_BIN)
	@for test in $(subst /,.,$(ALL_TEST_SOURCES:%.java=%)); do \
	  echo $$test:; \
	  $(TEST_BIN) org.junit.runner.JUnitCore $$test; \
	done

# Build and run the JreEmulation project's test bundle, then close simulator app.
# Note: the simulator app's name was changed to "Simulator" in Xcode 7.
run-xctests: test
	@xcodebuild -project JreEmulation.xcodeproj -scheme jre_emul -destination \
	    'platform=iOS Simulator,name=iPhone 6' test
	@killall 'iOS Simulator' || killall 'Simulator'

$(SUPPORT_LIB): $(SUPPORT_OBJS)
	@echo libtool -o $(SUPPORT_LIB)
	@libtool -static -o $(SUPPORT_LIB) $(SUPPORT_OBJS)

clean:
	@rm -rf $(TESTS_DIR)

$(TESTS_DIR):
	@mkdir -p $@

$(TESTS_DIR)/%.o: $(TESTS_DIR)/%.m | $(TRANSLATE_ARTIFACTS)
	@mkdir -p $(@D)
	@echo j2objcc -c $?
	@$(TEST_JOCC) $(COMPILE_FLAGS) -o $@ $<

$(TESTS_DIR)/%.o: $(TESTS_DIR)/arc/%.m | $(TRANSLATE_ARTIFACTS)
	@mkdir -p $(@D)
	@echo j2objcc -c $?
	@$(TEST_JOCC) $(COMPILE_FLAGS) -fobjc-arc -o $@ $<

$(TESTS_DIR)/%.o: $(ANDROID_NATIVE_TEST_DIR)/%.cpp | $(TESTS_DIR)
	cc -g -I$(EMULATION_CLASS_DIR) -x objective-c++ -c $? -o $@ \
	  -Werror -Wno-parentheses $(GCOV_FLAGS)

$(TEST_BIN): $(TEST_OBJS) $(SUPPORT_LIB) \
        ../dist/lib/macosx/libjre_emul.a ../dist/lib/macosx/libjunit.a
	@echo Building test executable...
	@$(TEST_JOCC) $(LINK_FLAGS) -o $@ $(TEST_OBJS)

$(ALL_TESTS_SOURCE): tests.mk
	@mkdir -p $(@D)
	@xcrun awk -f gen_all_tests.sh $(TESTS_TO_RUN) > $@

$(TESTS_DIR)/jreinitialization: Tests/JreInitialization.m
	@../dist/j2objcc -o $@ -ljre_emul -ObjC -Os $?

$(GEN_JAVA_DIR)/com/google/j2objc/arc/%.java: $(MISC_TEST_ROOT)/com/google/j2objc/%.java
	@mkdir -p $(@D)
	@echo $<
	@sed 's/^package com\.google\.j2objc;$$/package com.google.j2objc.arc;/' $< > $@

$(TESTS_DIR)/core_size:
	@mkdir -p $(@D)
	../dist/j2objcc -o $@ -ObjC

$(TESTS_DIR)/full_jre_size:
	@mkdir -p $(@D)
	../dist/j2objcc -ljre_emul -o $@ -ObjC

$(TESTS_DIR)/core_plus_io:
	@mkdir -p $(@D)
	../dist/j2objcc -ljre_io -o $@ -ObjC

$(TESTS_DIR)/core_plus_net:
	@mkdir -p $(@D)
	../dist/j2objcc -ljre_net -o $@ -ObjC

$(TESTS_DIR)/core_plus_util:
	@mkdir -p $(@D)
	../dist/j2objcc -ljre_util -o $@ -ObjC

$(TESTS_DIR)/core_plus_concurrent:
	@mkdir -p $(@D)
	../dist/j2objcc -ljre_concurrent -ljre_util -o $@ -ObjC

$(TESTS_DIR)/core_plus_channels:
	@mkdir -p $(@D)
	../dist/j2objcc -ljre_channels -ljre_net -ljre_security -ljre_util -o $@ -ObjC

$(TESTS_DIR)/core_plus_security:
	@mkdir -p $(@D)
	../dist/j2objcc -ljre_security -ljre_net -o $@ -ObjC

$(TESTS_DIR)/core_plus_ssl:
	@mkdir -p $(@D)
	../dist/j2objcc -ljre_ssl -ljre_security -ljre_net -ljre_util -o $@ -ObjC

$(TESTS_DIR)/core_plus_xml:
	@mkdir -p $(@D)
	../dist/j2objcc -ljre_xml -ljre_net -ljre_security -o $@ -ObjC

$(TESTS_DIR)/core_plus_zip:
	@mkdir -p $(@D)
	../dist/j2objcc -ljre_zip -ljre_security -ljre_net -ljre_io -o $@ -ObjC

$(TESTS_DIR)/core_plus_sql:
	@mkdir -p $(@D)
	../dist/j2objcc -ljre_sql -o $@ -ObjC

$(TESTS_DIR)/core_plus_beans:
	@mkdir -p $(@D)
	../dist/j2objcc -ljre_beans -ljre_util -o $@ -ObjC

$(TESTS_DIR)/core_plus_android_util:
	@mkdir -p $(@D)
	../dist/j2objcc -landroid_util -ljre_net -ljre_util -ljre_concurrent -ljre_security -o $@ -ObjC
