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

SUPPORT_OBJS = \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/AbstractBufferTest.o \
	$(TESTS_DIR)/tests/support/Support_CollectionTest.o \
	$(TESTS_DIR)/tests/support/Support_ListTest.o \
	$(TESTS_DIR)/tests/support/Support_MapTest2.o \
	$(TESTS_DIR)/tests/support/Support_SetTest.o \
	$(TESTS_DIR)/tests/support/Support_StringReader.o \
	$(TESTS_DIR)/tests/support/Support_StringWriter.o \
	$(TESTS_DIR)/tests/support/Support_UnmodifiableCollectionTest.o \
	$(TESTS_DIR)/tests/support/Support_UnmodifiableMapTest.o

TEST_OBJS = \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/BufferedInputStreamTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/BufferedOutputStreamTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/BufferedReaderTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/BufferedWriterTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/ByteArrayInputStreamTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/ByteArrayOutputStreamTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/CharArrayReaderTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/CharArrayWriterTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/FileDescriptorTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/FileInputStreamTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/FileNotFoundExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/FileReaderTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/FileWriterTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/FilterOutputStreamTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/FileOutputStreamTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/InputStreamReaderTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/InputStreamTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/InterruptedIOExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/IOExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/OutputStreamWriterTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/PrintStreamTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/PrintWriterTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/ReaderTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/StringReaderTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/StringWriterTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/UnsupportedEncodingExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/WriterTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/ArithmeticExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/ArrayIndexOutOfBoundsExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/ArrayStoreExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/AssertionErrorTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/BooleanTest.o \
	$(TESTS_DIR)/java/lang/ClassTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/ClassCastExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/ClassNotFoundExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/CloneNotSupportedExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/ErrorTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/ExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/IllegalAccessExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/IllegalArgumentExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/IllegalStateExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/IllegalThreadStateExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/IndexOutOfBoundsExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/InstantiationExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/IntegerTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/InterruptedExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/LongTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/NegativeArraySizeExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/NoSuchMethodExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/NullPointerExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/NumberFormatExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/NumberTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/ObjectTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/reflect/ArrayTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/reflect/ModifierTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/RuntimeExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/ShortTest.o \
	$(TESTS_DIR)/java/lang/SplitTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/StrictMathTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/StringBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/StringIndexOutOfBoundsExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/StringTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/ThreadDeathTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/UnsupportedOperationExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/tests/java/math/BigIntegerAddTest.o \
	$(TESTS_DIR)/org/apache/harmony/tests/java/math/BigIntegerAndTest.o \
	$(TESTS_DIR)/org/apache/harmony/tests/java/math/BigIntegerCompareTest.o \
	$(TESTS_DIR)/org/apache/harmony/tests/java/math/BigIntegerConstructorsTest.o \
	$(TESTS_DIR)/org/apache/harmony/tests/java/math/BigIntegerConvertTest.o \
	$(TESTS_DIR)/org/apache/harmony/tests/java/math/BigIntegerDivideTest.o \
	$(TESTS_DIR)/org/apache/harmony/tests/java/math/BigIntegerHashCodeTest.o \
	$(TESTS_DIR)/org/apache/harmony/tests/java/math/BigIntegerMultiplyTest.o \
	$(TESTS_DIR)/org/apache/harmony/tests/java/math/BigIntegerNotTest.o \
	$(TESTS_DIR)/org/apache/harmony/tests/java/math/BigIntegerOrTest.o \
	$(TESTS_DIR)/org/apache/harmony/tests/java/math/BigIntegerSubtractTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/BufferOverflowExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/BufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/BufferOverflowExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/ByteBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/ByteOrderTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/CharBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/DoubleBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/FloatBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/IntBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/InvalidMarkExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/LongBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/ShortBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/AbstractCollectionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/AbstractListTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/AbstractQueueTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/AbstractSequentialListTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/ArrayListTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/ArraysTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/BitSetTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/DateTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/EmptyStackExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/HashMapTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/HashSetTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/HashtableTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/LinkedHashMapTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/LinkedHashSetTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/LinkedListTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/LocaleTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/NoSuchElementExceptionTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/PriorityQueueTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/RandomTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/StackTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/VectorTest.o

FAILING_TESTS = \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/io/FileTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/ByteTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/DoubleTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/EnumTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/FloatTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/StrictMathTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/StringBuilderTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/AbstractMapTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/CollectionsTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/TreeSetTest.o \
	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/TreeMapTest.o \

# These tests fail because they subclass other tests, so linking fails
# because the binary has multiple main() functions.
# TODO(user): enable when JUnit test runner is updated to dynamically
# load tests, instead of the main() function test suite hack.
FAILING_NIO_TESTS = \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/HeapByteBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/HeapCharBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/HeapDoubleBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/HeapFloatBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/HeapIntBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/HeapLongBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/HeapShortBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/ReadOnlyCharBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/ReadOnlyDoubleBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/ReadOnlyFloatBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/ReadOnlyHeapByteBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/ReadOnlyHeapCharBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/ReadOnlyHeapDoubleBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/ReadOnlyHeapFloatBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/ReadOnlyHeapIntBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/ReadOnlyHeapLongBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/ReadOnlyHeapShortBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/ReadOnlyIntBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/ReadOnlyLongBufferTest.o \
	$(TESTS_DIR)/org/apache/harmony/nio/tests/java/nio/ReadOnlyShortBufferTest.o \

# Most of these tests are failing for a common index-out-of-range error.
FAILING_MATH_TESTS = \
	$(TESTS_DIR)/org/apache/harmony/tests/java/math/BigDecimalArithmeticTest.o \
	$(TESTS_DIR)/org/apache/harmony/tests/java/math/BigDecimalCompareTest.o \
	$(TESTS_DIR)/org/apache/harmony/tests/java/math/BigDecimalConstructorsTest.o \
	$(TESTS_DIR)/org/apache/harmony/tests/java/math/BigDecimalConvertTest.o \
	$(TESTS_DIR)/org/apache/harmony/tests/java/math/BigDecimalScaleOperationsTest.o \
	$(TESTS_DIR)/tests/api/java/math/BigDecimalTest.o \
	$(TESTS_DIR)/org/apache/harmony/tests/java/math/BigIntegerModPowTest.o \
	$(TESTS_DIR)/org/apache/harmony/tests/java/math/BigIntegerOperateBitsTest.o \
	$(TESTS_DIR)/org/apache/harmony/tests/java/math/BigIntegerToStringTest.o \
	$(TESTS_DIR)/org/apache/harmony/tests/java/math/BigIntegerXorTest.o \
	$(TESTS_DIR)/tests/api/java/math/BigIntegerTest.o \

JAVA_SOURCE_LIST = $(TMPDIR).tests.list

# Broken tests, plus associated bug id.  Once bug is fixed, move line(s) up.
#	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/ByteTest.o                        b/5738869
#
#	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/EnumTest.o                        b/5729446
#
#	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/StrictMathTest.o                  b/5733596
#	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/lang/StringBuilderTest.o               b/5733596
#
#	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/TreeMapTest.o                     b/5754676
#	$(TESTS_DIR)/org/apache/harmony/luni/tests/java/util/TreeSetTest.o                     b/5754676
#

JUNIT_JAR = ../dist/lib/junit-4.10.jar
TEST_SOURCE_PATH = \
  $(JRE_TEST_ROOT):$(JRE_MATH_TEST_ROOT):$(JRE_NIO_TEST_ROOT):$(TEST_SUPPORT_ROOT):$(MATH_TEST_SUPPORT_ROOT)
TEST_JOC = ../dist/j2objc -classpath $(JUNIT_JAR) -Werror \
	-sourcepath $(TEST_SOURCE_PATH) -d $(TESTS_DIR)
TEST_JOCC = ../dist/j2objcc -g -I$(TESTS_DIR) -l junit -Werror \
	-L$(TESTS_DIR) -l test-support
SUPPORT_LIB = $(TESTS_DIR)/libtest-support.a
TEST_BINS = $(TEST_OBJS:.o=)

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

link: build $(TEST_BINS)

run-tests: link $(TEST_BINS)
	@/bin/sh runtests.sh $(TEST_BINS)

$(SUPPORT_LIB): $(SUPPORT_OBJS)
	libtool -static -o $(SUPPORT_LIB) $(SUPPORT_OBJS)
clean:
	@rm -rf $(TESTS_DIR)

pre_translate:
	@rm -f $(JAVA_SOURCE_LIST)
	@touch $(JAVA_SOURCE_LIST)

foo:
	@echo JRE_MATH_TEST_ROOT=$(JRE_MATH_TEST_ROOT)

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

$(TESTS_DIR)/%.o: $(TESTS_DIR)/%.m
	@mkdir -p `dirname $@`
	../dist/j2objcc -g -I$(TESTS_DIR) -c $? -o $@ \
	  -Wno-objc-redundant-literal-use -Wno-format \
	  -Werror -Wno-parentheses -I$(EMULATION_TESTS_DIR) -I$(TESTS_DIR)

$(TESTS_DIR)/%: $(TESTS_DIR)/%.o
	$(TEST_JOCC) $? -o $@
