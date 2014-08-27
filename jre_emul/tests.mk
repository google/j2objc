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

.SUFFIXES: .java .m

default: test

include environment.mk

SUPPORT_SOURCES = \
    JSR166TestCase.java \
    android/test/MoreAsserts.java \
    android/text/SpannableTest.java \
    com/google/j2objc/package-info.java \
    com/google/j2objc/TestAnnotation.java \
    libcore/java/net/customstreamhandler/http/Handler.java \
    libcore/java/nio/charset/Charset_TestGenerator.java \
    libcore/java/nio/charset/OldCharset_AbstractTest.java \
    libcore/java/util/ServiceLoaderTestInterface.java \
    libcore/util/SerializationTester.java \
    org/apache/harmony/logging/tests/java/util/logging/LevelTestResource.java \
    org/apache/harmony/logging/tests/java/util/logging/util/EnvironmentHelper.java \
    org/apache/harmony/testframework/serialization/SerializationTest.java \
    org/apache/harmony/text/tests/java/text/Support_Format.java \
    org/apache/harmony/text/tests/java/text/Support_MessageFormat.java \
    tests/security/MessageDigestTest.java \
    tests/support/Support_ASimpleInputStream.java \
    tests/support/Support_ASimpleOutputStream.java \
    tests/support/Support_ASimpleReader.java \
    tests/support/Support_ASimpleWriter.java \
    tests/support/Support_CollectionTest.java \
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
    tests/util/CallVerificationStack.java

TEST_SOURCES = \
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
    PriorityBlockingQueueTest.java \
    PriorityQueueTest.java \
    ReentrantLockTest.java \
    ReentrantReadWriteLockTest.java \
    SerializationTest.java \
    ScheduledExecutorTest.java \
    SynchronousQueueTest.java \
    ThreadPoolExecutorTest.java \
    TimeUnitTest.java \
    android/text/SpannableStringBuilderTest.java \
    android/text/SpannableStringTest.java \
    android/text/TextUtilsTest.java \
    com/google/j2objc/ClassTest.java \
    com/google/j2objc/PackageTest.java \
    com/google/j2objc/ThrowableTest.java \
    com/google/j2objc/security/IosSecureRandomImplTest.java \
    java/lang/ref/PhantomReferenceTest.java \
    java/lang/ref/SoftReferenceTest.java \
    java/lang/ref/WeakReferenceTest.java \
    java/lang/reflect/ProxyTest.java \
    java/util/TreeMapTest.java \
    java/util/WeakHashMapTest.java \
    java/util/regex/MatcherTest.java \
    libcore/icu/ICUTest.java \
    libcore/icu/LocaleDataTest.java \
    libcore/io/MemoryTest.java \
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
    libcore/java/io/RandomAccessFileTest.java \
    libcore/java/io/StreamTokenizerTest.java \
    libcore/java/lang/DoubleTest.java \
    libcore/java/lang/CharacterTest.java \
    libcore/java/lang/FloatTest.java \
    libcore/java/lang/IntegralToStringTest.java \
    libcore/java/lang/ThrowableTest.java \
    libcore/java/lang/reflect/AnnotationsTest.java \
    libcore/java/lang/reflect/ArrayTest.java \
    libcore/java/lang/reflect/ConstructorTest.java \
    libcore/java/lang/reflect/FieldTest.java \
    libcore/java/net/DatagramSocketTest.java \
    libcore/java/net/InetAddressTest.java \
    libcore/java/net/InetSocketAddressTest.java \
    libcore/java/net/NetworkInterfaceTest.java \
    libcore/java/net/OldAuthenticatorTest.java \
    libcore/java/net/OldPasswordAuthenticationTest.java \
    libcore/java/net/ServerSocketTest.java \
    libcore/java/net/SocketTest.java \
    libcore/java/net/URITest.java \
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
    libcore/java/nio/charset/CharsetDecoderTest.java \
    libcore/java/nio/charset/CharsetEncoderTest.java \
    libcore/java/text/CollatorTest.java \
    libcore/java/text/DecimalFormatTest.java \
    libcore/java/text/DecimalFormatSymbolsTest.java \
    libcore/java/text/NumberFormatTest.java \
    libcore/java/util/BitSetTest.java \
    libcore/java/util/CalendarTest.java \
    libcore/java/util/FormatterTest.java \
    libcore/java/util/ObjectsTest.java \
    libcore/java/util/OldAndroidArrayListTest.java \
    libcore/java/util/OldTimeZoneTest.java \
    libcore/java/util/ServiceLoaderTest.java \
    libcore/java/util/TreeMapTest.java \
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
    libcore/java/util/zip/ZipEntryTest.java \
    libcore/java/util/zip/ZipFileTest.java \
    libcore/java/util/zip/ZipInputStreamTest.java \
    libcore/java/util/zip/ZipOutputStreamTest.java \
    libcore/net/url/UrlUtilsTest.java \
    org/apache/harmony/archive/tests/java/util/zip/CRC32Test.java \
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
    org/apache/harmony/luni/tests/java/lang/StrictMathTest.java \
    org/apache/harmony/luni/tests/java/lang/StringBufferTest.java \
    org/apache/harmony/luni/tests/java/lang/StringIndexOutOfBoundsExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/StringTest.java \
    org/apache/harmony/luni/tests/java/lang/String2Test.java \
    org/apache/harmony/luni/tests/java/lang/ThreadDeathTest.java \
    org/apache/harmony/luni/tests/java/lang/ThreadTest.java \
    org/apache/harmony/luni/tests/java/lang/TypeNotPresentExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/UnsupportedOperationExceptionTest.java \
    org/apache/harmony/luni/tests/java/lang/VirtualMachineErrorTest.java \
    org/apache/harmony/luni/tests/util/UtilTest.java \
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
    org/apache/harmony/luni/tests/java/util/HashMapTest.java \
    org/apache/harmony/luni/tests/java/util/HashSetTest.java \
    org/apache/harmony/luni/tests/java/util/HashtableTest.java \
    org/apache/harmony/luni/tests/java/util/IdentityHashMapTest.java \
    org/apache/harmony/luni/tests/java/util/LinkedHashMapTest.java \
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
    org/apache/harmony/tests/java/io/ObjectStreamClassTest.java \
    org/apache/harmony/tests/java/io/ObjectStreamConstantsTest.java \
    org/apache/harmony/tests/java/io/ObjectStreamFieldTest.java \
    org/apache/harmony/tests/java/io/RandomAccessFileTest.java \
    org/apache/harmony/tests/java/lang/CharacterTest.java \
    org/apache/harmony/tests/java/lang/DoubleTest.java \
    org/apache/harmony/tests/java/lang/FloatTest.java \
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
    org/apache/harmony/regex/tests/java/util/regex/Matcher2Test.java \
    org/apache/harmony/regex/tests/java/util/regex/ModeTest.java \
    org/apache/harmony/regex/tests/java/util/regex/Pattern2Test.java \
    org/apache/harmony/regex/tests/java/util/regex/PatternSyntaxExceptionTest.java \
    org/apache/harmony/regex/tests/java/util/regex/PatternTest.java \
    org/apache/harmony/regex/tests/java/util/regex/ReplaceTest.java \
    org/apache/harmony/regex/tests/java/util/regex/SplitTest.java \
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
    tests/targets/security/MessageDigestTestMD5.java \
    tests/targets/security/MessageDigestTestSHA1.java \
    tests/targets/security/MessageDigestTestSHA256.java \
    tests/targets/security/MessageDigestTestSHA384.java \
    tests/targets/security/MessageDigestTestSHA512.java \

SUITE_SOURCES = \
    ConcurrencyTests.java \
    libcore/java/io/SmallTests.java \
    libcore/java/net/SmallTests.java \
    libcore/java/util/zip/SmallTests.java \
    org/apache/harmony/logging/tests/java/util/logging/AllTests.java \
    org/json/SmallTests.java \

TESTS_TO_SKIP = \
    ExchangerTest.java

FAILING_TESTS = \
    libcore/java/util/TreeSetTest.java \
    org/apache/harmony/luni/tests/java/lang/StrictMathTest.java \
    org/apache/harmony/luni/tests/java/lang/StringBuilderTest.java \

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

TESTS_TO_RUN = $(filter-out $(TESTS_TO_SKIP),$(TEST_SOURCES))
TESTS_TO_RUN := $(subst /,.,$(TESTS_TO_RUN:%.java=%))

SUPPORT_OBJS = $(SUPPORT_SOURCES:%.java=$(TESTS_DIR)/%.o)
TEST_OBJS = \
    $(TEST_SOURCES:%.java=$(TESTS_DIR)/%.o) \
    $(SUITE_SOURCES:%.java=$(TESTS_DIR)/%.o)

TEST_RESOURCES_SRCS = \
    org/apache/harmony/luni/tests/test_resource.txt \
    org/apache/harmony/luni/tests/java/io/testfile-utf8.txt
ANDROID_TEST_RESOURCES_SRCS = \
    META-INF/services/libcore.java.util.ServiceLoaderTestInterface \
    MD5.check \
    MD5.data \
    SHA-1.check \
    SHA-1.data \
    SHA-256.check \
    SHA-256.data \
    SHA-384.check \
    SHA-384.data \
    SHA-512.check \
    SHA-512.data \
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

TEST_RESOURCES_ROOT = apache_harmony/classlib/modules/luni/src/test/resources
ANDROID_TEST_RESOURCES_ROOT = android/libcore/luni/src/test/resources
LOGGING_TEST_RESOURCES_ROOT = apache_harmony/classlib/modules/logging/src/test/resources
TEST_RESOURCES = \
    $(TEST_RESOURCES_SRCS:%=$(TESTS_DIR)/%) \
    $(ANDROID_TEST_RESOURCES_SRCS:%=$(TESTS_DIR)/%) \
    $(LOGGING_TEST_RESOURCES_SRCS:%=$(TESTS_DIR)/%)

# Broken tests, plus associated bug id.  Once bug is fixed, move line(s) up.
# $(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/StringBuilderTest.o     b/8842295

JUNIT_DIST_JAR = $(DIST_JAR_DIR)/$(JUNIT_JAR)

TEST_JOCC = ../dist/j2objcc -g -I$(TESTS_DIR) -l junit -Werror \
    -L$(TESTS_DIR) -l test-support -ObjC
SUPPORT_LIB = $(TESTS_DIR)/libtest-support.a
TEST_BIN = $(TESTS_DIR)/jre_unit_tests

GEN_OBJC_DIR = $(TESTS_DIR)
TRANSLATE_JAVA_FULL = $(SUPPORT_SOURCES) $(TEST_SOURCES) $(SUITE_SOURCES)
TRANSLATE_JAVA_RELATIVE = $(SUPPORT_SOURCES) $(TEST_SOURCES) $(SUITE_SOURCES)
TRANSLATE_ARGS = -classpath $(JUNIT_DIST_JAR) -Werror -sourcepath $(TEST_SRC) \
    --extract-unsequenced -encoding UTF-8
include ../make/translate.mk

ALL_TESTS_CLASS = AllJreTests
ALL_TESTS_SOURCE = $(RELATIVE_TESTS_DIR)/AllJreTests.java

ifdef GENERATE_TEST_COVERAGE
GCOV_FLAGS = -ftest-coverage -fprofile-arcs
TEST_JOCC += $(GCOV_FLAGS)
endif

test: run-tests

support-lib: $(SUPPORT_LIB)

build: support-lib $(TEST_OBJS) $(ALL_TESTS_SOURCE:%.java=%.o)
	@:

translate-all: translate $(ALL_TESTS_SOURCE:%.java=%.m)
	@:

link: build $(TEST_BIN)

resources: $(TEST_RESOURCES)
	@:

$(TESTS_DIR)/%.txt: $(TEST_RESOURCES_ROOT)/%.txt
	@mkdir -p `dirname $@`
	@cp $< $@

$(TESTS_DIR)/%: $(ANDROID_TEST_RESOURCES_ROOT)/%
	@mkdir -p `dirname $@`
	@cp $< $@

$(TESTS_DIR)/%: $(LOGGING_TEST_RESOURCES_ROOT)/%
	@mkdir -p `dirname $@`
	@cp $< $@

run-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore $(ALL_TESTS_CLASS)

run-concurrency-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore ConcurrencyTests

run-io-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore libcore.java.io.SmallTests

run-json-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore org.json.SmallTests

run-logging-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore \
	    org.apache.harmony.logging.tests.java.util.logging.AllTests

run-net-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore libcore.java.net.SmallTests

run-zip-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore libcore.java.util.zip.SmallTests

# Run this when the above has errors and JUnit doesn't report which
# test failed or hung.
run-each-test: link resources $(TEST_BIN)
	@for test in $(subst /,.,$(TEST_SOURCES:%.java=%)); do \
	  echo $$test:; \
	  $(TEST_BIN) org.junit.runner.JUnitCore $$test; \
	done

$(SUPPORT_LIB): $(SUPPORT_OBJS)
	libtool -static -o $(SUPPORT_LIB) $(SUPPORT_OBJS)

clean:
	@rm -rf $(TESTS_DIR)

$(TESTS_DIR):
	@mkdir -p $@

$(TESTS_DIR)/%.o: $(TESTS_DIR)/%.m
	@mkdir -p `dirname $@`
	../dist/j2objcc -g -I$(TESTS_DIR) -c $? -o $@ \
	  -Wno-objc-redundant-literal-use -Wno-format \
	  -Werror -Wno-parentheses $(GCOV_FLAGS)

$(TEST_BIN): $(TEST_OBJS) $(SUPPORT_LIB) $(ALL_TESTS_SOURCE:%.java=%.o) \
        ../dist/lib/libjre_emul.a ../dist/lib/libjunit.a
	@echo Building test executable...
	@$(TEST_JOCC) -o $@ $(TEST_OBJS) $(ALL_TESTS_SOURCE:%.java=%.o)

$(ALL_TESTS_SOURCE): | $(TESTS_DIR)
	@./gen_all_tests.sh $(TESTS_TO_RUN) > $@

$(ALL_TESTS_SOURCE:%.java=%.m): $(ALL_TESTS_SOURCE)
	@$(TRANSLATE_CMD) $?

$(ALL_TESTS_SOURCE:%.java=%.o): $(ALL_TESTS_SOURCE:%.java=%.m) $(TEST_OBJS:%.o=%.h)
	../dist/j2objcc -g -I$(TESTS_DIR) \
	    -c $(ALL_TESTS_SOURCE:%.java=%.m) -o $(ALL_TESTS_SOURCE:%.java=%.o)
