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

include environment.mk

SUPPORT_SOURCES = \
	JSR166TestCase.java \
	org/apache/harmony/nio/tests/java/nio/AbstractBufferTest.java \
	tests/support/Support_CollectionTest.java \
	tests/support/Support_ListTest.java \
	tests/support/Support_MapTest2.java \
	tests/support/Support_SetTest.java \
	tests/support/Support_StringReader.java \
	tests/support/Support_StringWriter.java \
	tests/support/Support_TimeZone.java \
	tests/support/Support_UnmodifiableCollectionTest.java \
	tests/support/Support_UnmodifiableMapTest.java

TEST_SOURCES = \
	CopyOnWriteArrayListTest.java \
	CopyOnWriteArraySetTest.java \
	java/lang/ClassTest.java \
	java/lang/ThrowableTest.java \
	libcore/java/lang/ThrowableTest.java \
	libcore/java/lang/reflect/AnnotationsTest.java \
	libcore/java/lang/reflect/ArrayTest.java \
	libcore/java/lang/reflect/ConstructorTest.java \
	org/apache/harmony/luni/tests/java/io/BufferedInputStreamTest.java \
	org/apache/harmony/luni/tests/java/io/BufferedOutputStreamTest.java \
	org/apache/harmony/luni/tests/java/io/BufferedReaderTest.java \
	org/apache/harmony/luni/tests/java/io/BufferedWriterTest.java \
	org/apache/harmony/luni/tests/java/io/ByteArrayInputStreamTest.java \
	org/apache/harmony/luni/tests/java/io/ByteArrayOutputStreamTest.java \
	org/apache/harmony/luni/tests/java/io/CharArrayReaderTest.java \
	org/apache/harmony/luni/tests/java/io/CharArrayWriterTest.java \
	org/apache/harmony/luni/tests/java/io/DataInputStreamTest.java \
	org/apache/harmony/luni/tests/java/io/DataOutputStreamTest.java \
	org/apache/harmony/luni/tests/java/io/EOFExceptionTest.java \
	org/apache/harmony/luni/tests/java/io/FileDescriptorTest.java \
	org/apache/harmony/luni/tests/java/io/FileInputStreamTest.java \
	org/apache/harmony/luni/tests/java/io/FileNotFoundExceptionTest.java \
	org/apache/harmony/luni/tests/java/io/FileReaderTest.java \
	org/apache/harmony/luni/tests/java/io/FileTest.java \
	org/apache/harmony/luni/tests/java/io/FileWriterTest.java \
	org/apache/harmony/luni/tests/java/io/FilterInputStreamTest.java \
	org/apache/harmony/luni/tests/java/io/FilterOutputStreamTest.java \
	org/apache/harmony/luni/tests/java/io/FileOutputStreamTest.java \
	org/apache/harmony/luni/tests/java/io/InputStreamReaderTest.java \
	org/apache/harmony/luni/tests/java/io/InputStreamTest.java \
	org/apache/harmony/luni/tests/java/io/InterruptedIOExceptionTest.java \
	org/apache/harmony/luni/tests/java/io/IOExceptionTest.java \
	org/apache/harmony/luni/tests/java/io/OutputStreamWriterTest.java \
	org/apache/harmony/luni/tests/java/io/PrintStreamTest.java \
	org/apache/harmony/luni/tests/java/io/PrintWriterTest.java \
	org/apache/harmony/luni/tests/java/io/PushbackInputStreamTest.java \
	org/apache/harmony/luni/tests/java/io/PushbackReaderTest.java \
	org/apache/harmony/luni/tests/java/io/ReaderTest.java \
	org/apache/harmony/luni/tests/java/io/StringReaderTest.java \
	org/apache/harmony/luni/tests/java/io/StringWriterTest.java \
	org/apache/harmony/luni/tests/java/io/UnsupportedEncodingExceptionTest.java \
	org/apache/harmony/luni/tests/java/io/UTFDataFormatExceptionTest.java \
	org/apache/harmony/luni/tests/java/io/WriterTest.java \
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
	org/apache/harmony/luni/tests/java/lang/NoSuchMethodExceptionTest.java \
	org/apache/harmony/luni/tests/java/lang/NullPointerExceptionTest.java \
	org/apache/harmony/luni/tests/java/lang/NumberFormatExceptionTest.java \
	org/apache/harmony/luni/tests/java/lang/NumberTest.java \
	org/apache/harmony/luni/tests/java/lang/ObjectTest.java \
	org/apache/harmony/luni/tests/java/lang/reflect/ArrayTest.java \
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
	org/apache/harmony/luni/tests/java/lang/UnsupportedOperationExceptionTest.java \
	org/apache/harmony/luni/tests/util/UtilTest.java \
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
	org/apache/harmony/nio/tests/java/nio/BufferOverflowExceptionTest.java \
	org/apache/harmony/nio/tests/java/nio/BufferTest.java \
	org/apache/harmony/nio/tests/java/nio/ByteBufferTest.java \
	org/apache/harmony/nio/tests/java/nio/ByteOrderTest.java \
	org/apache/harmony/nio/tests/java/nio/CharBufferTest.java \
	org/apache/harmony/nio/tests/java/nio/DoubleBufferTest.java \
	org/apache/harmony/nio/tests/java/nio/FloatBufferTest.java \
	org/apache/harmony/nio/tests/java/nio/IntBufferTest.java \
	org/apache/harmony/nio/tests/java/nio/InvalidMarkExceptionTest.java \
	org/apache/harmony/nio/tests/java/nio/LongBufferTest.java \
	org/apache/harmony/nio/tests/java/nio/ShortBufferTest.java \
	org/apache/harmony/luni/tests/java/util/AbstractCollectionTest.java \
	org/apache/harmony/luni/tests/java/util/AbstractListTest.java \
	org/apache/harmony/luni/tests/java/util/AbstractMapTest.java \
	org/apache/harmony/luni/tests/java/util/AbstractQueueTest.java \
	org/apache/harmony/luni/tests/java/util/AbstractSequentialListTest.java \
	org/apache/harmony/luni/tests/java/util/ArrayListTest.java \
	org/apache/harmony/luni/tests/java/util/ArraysTest.java \
	org/apache/harmony/luni/tests/java/util/BitSetTest.java \
	org/apache/harmony/luni/tests/java/util/CalendarTest.java \
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
	org/apache/harmony/luni/tests/java/util/TreeMapTest.java \
	org/apache/harmony/luni/tests/java/util/TreeSetTest.java \
	org/apache/harmony/luni/tests/java/util/VectorTest.java \
	org/apache/harmony/nio/tests/java/nio/HeapByteBufferTest.java \
	org/apache/harmony/nio/tests/java/nio/HeapCharBufferTest.java \
	org/apache/harmony/nio/tests/java/nio/HeapDoubleBufferTest.java \
	org/apache/harmony/nio/tests/java/nio/HeapFloatBufferTest.java \
	org/apache/harmony/nio/tests/java/nio/HeapIntBufferTest.java \
	org/apache/harmony/nio/tests/java/nio/HeapLongBufferTest.java \
	org/apache/harmony/nio/tests/java/nio/HeapShortBufferTest.java \
	org/apache/harmony/nio/tests/java/nio/ReadOnlyCharBufferTest.java \
	org/apache/harmony/nio/tests/java/nio/ReadOnlyDoubleBufferTest.java \
	org/apache/harmony/nio/tests/java/nio/ReadOnlyFloatBufferTest.java \
	org/apache/harmony/nio/tests/java/nio/ReadOnlyHeapByteBufferTest.java \
	org/apache/harmony/nio/tests/java/nio/ReadOnlyHeapCharBufferTest.java \
	org/apache/harmony/nio/tests/java/nio/ReadOnlyHeapDoubleBufferTest.java \
	org/apache/harmony/nio/tests/java/nio/ReadOnlyHeapFloatBufferTest.java \
	org/apache/harmony/nio/tests/java/nio/ReadOnlyHeapIntBufferTest.java \
	org/apache/harmony/nio/tests/java/nio/ReadOnlyHeapLongBufferTest.java \
	org/apache/harmony/nio/tests/java/nio/ReadOnlyHeapShortBufferTest.java \
	org/apache/harmony/nio/tests/java/nio/ReadOnlyIntBufferTest.java \
	org/apache/harmony/nio/tests/java/nio/ReadOnlyLongBufferTest.java \
	org/apache/harmony/nio/tests/java/nio/ReadOnlyShortBufferTest.java \
	org/apache/harmony/tests/java/util/regex/Matcher2Test.java \
	org/apache/harmony/tests/java/util/regex/MatcherTest.java \
	org/apache/harmony/tests/java/util/regex/Pattern2Test.java \
	org/apache/harmony/tests/java/util/regex/PatternErrorTest.java \
	org/apache/harmony/tests/java/util/regex/PatternSyntaxExceptionTest.java \
	org/apache/harmony/tests/java/util/regex/PatternTest.java \
	org/apache/harmony/tests/java/util/regex/ReplaceTest.java \
	org/apache/harmony/tests/java/util/regex/SplitTest.java \
	tests/api/java/util/TimerTaskTest.java \
	tests/api/java/util/TimerTest.java \

FAILING_TESTS = \
	org/apache/harmony/luni/tests/java/lang/DoubleTest.java \
	org/apache/harmony/luni/tests/java/lang/FloatTest.java \
	org/apache/harmony/luni/tests/java/lang/StrictMathTest.java \
	org/apache/harmony/luni/tests/java/lang/StringBuilderTest.java \
	org/apache/harmony/tests/java/util/regex/ModeTest.java \

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

JAVA_SOURCE_LIST = $(TMPDIR).tests.list

SUPPORT_OBJS = $(SUPPORT_SOURCES:%.java=$(TESTS_DIR)/%.o)
TEST_OBJS = $(TEST_SOURCES:%.java=$(TESTS_DIR)/%.o)

# Broken tests, plus associated bug id.  Once bug is fixed, move line(s) up.
#	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/StringBuilderTest.o               b/8842295

JUNIT_JAR = ../dist/lib/junit-4.10.jar

TEST_JOC = ../dist/j2objc -classpath $(JUNIT_JAR) -Werror \
	-sourcepath $(TEST_SRC) -d $(TESTS_DIR)
TEST_JOCC = ../dist/j2objcc -g -I$(TESTS_DIR) -l junit -Werror \
	-L$(TESTS_DIR) -l test-support
SUPPORT_LIB = $(TESTS_DIR)/libtest-support.a
TEST_BIN = $(BUILD_DIR)/jre_unit_tests

test: run-tests

j2objc:
	@cd .. && $(MAKE) -f Makefile dist

translate: j2objc pre_translate $(SUPPORT_OBJS:.o=.m) $(TEST_OBJS:.o=.m)
	@if [ `cat $(JAVA_SOURCE_LIST) | wc -l` -ge 1 ] ; then \
	  $(TEST_JOC) `cat $(JAVA_SOURCE_LIST)` ; \
	fi

support-lib: translate $(SUPPORT_LIB)

build: support-lib $(TEST_OBJS)
	@:

link: build $(TEST_BIN)

run-tests: link $(TEST_BIN)
	@/bin/sh ../scripts/runtests.sh $(TEST_BIN) $(subst /,.,$(TEST_SOURCES:%.java=%))

$(SUPPORT_LIB): $(SUPPORT_OBJS)
	libtool -static -o $(SUPPORT_LIB) $(SUPPORT_OBJS)
clean:
	@rm -rf $(TESTS_DIR)

pre_translate:
	@rm -f $(JAVA_SOURCE_LIST)
	@touch $(JAVA_SOURCE_LIST)

$(TESTS_DIR):
	@mkdir -p $@

$(TESTS_DIR)/%.h $(TESTS_DIR)/%.m: $(JRE_TEST_ROOT)/%.java
	@echo $? >> $(JAVA_SOURCE_LIST)

$(TESTS_DIR)/%.h $(TESTS_DIR)/%.m: $(JRE_MATH_TEST_ROOT)/%.java
	@echo $? >> $(JAVA_SOURCE_LIST)

$(TESTS_DIR)/%.h $(TESTS_DIR)/%.m: $(JRE_NIO_TEST_ROOT)/%.java
	@echo $? >> $(JAVA_SOURCE_LIST)

$(TESTS_DIR)/%.h $(TESTS_DIR)/%.m: $(TEST_SUPPORT_ROOT)/%.java
	@echo $? >> $(JAVA_SOURCE_LIST)

$(TESTS_DIR)/%.h $(TESTS_DIR)/%.m: $(MATH_TEST_SUPPORT_ROOT)/%.java
	@echo $? >> $(JAVA_SOURCE_LIST)

$(TESTS_DIR)/%.h $(TESTS_DIR)/%.m: $(MISC_TEST_ROOT)/%.java
	@echo $? >> $(JAVA_SOURCE_LIST)

$(TESTS_DIR)/%.h $(TESTS_DIR)/%.m: $(ANDROID_JRE_TEST_ROOT)/%.java
	@echo $? >> $(JAVA_SOURCE_LIST)

$(TESTS_DIR)/%.h $(TESTS_DIR)/%.m: $(REGEX_TEST_ROOT)/%.java
	@echo $? >> $(JAVA_SOURCE_LIST)

$(TESTS_DIR)/%.h $(TESTS_DIR)/%.m: $(CONCURRENT_TEST_ROOT)/%.java
	@echo $? >> $(JAVA_SOURCE_LIST)

$(TESTS_DIR)/%.o: $(TESTS_DIR)/%.m
	@mkdir -p `dirname $@`
	../dist/j2objcc -g -I$(TESTS_DIR) -c $? -o $@ \
	  -Wno-objc-redundant-literal-use -Wno-format \
	  -Werror -Wno-parentheses -I$(EMULATION_TESTS_DIR) -I$(TESTS_DIR)

$(TEST_BIN): $(TEST_OBJS) ../dist/lib/libjre_emul.a
	$(TEST_JOCC) -o $@ $(TEST_OBJS)
