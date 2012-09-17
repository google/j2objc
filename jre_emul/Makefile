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

.SUFFIXES: .java .m
.PHONY: default j2objc clean

include environment.mk

IOS_OBJS = \
	$(CLASS_DIR)/IOSArray.o \
	$(CLASS_DIR)/IOSArrayClass.o \
	$(CLASS_DIR)/IOSBooleanArray.o \
	$(CLASS_DIR)/IOSByteArray.o \
	$(CLASS_DIR)/IOSCharArray.o \
	$(CLASS_DIR)/IOSClass.o \
	$(CLASS_DIR)/IOSCollection.o \
	$(CLASS_DIR)/IOSDoubleArray.o \
	$(CLASS_DIR)/IOSFloatArray.o \
	$(CLASS_DIR)/IOSIntArray.o \
	$(CLASS_DIR)/IOSIterator.o \
	$(CLASS_DIR)/IOSList.o \
	$(CLASS_DIR)/IOSLongArray.o \
	$(CLASS_DIR)/IOSObjectArray.o \
	$(CLASS_DIR)/IOSPrimitiveClass.o \
	$(CLASS_DIR)/IOSSet.o \
	$(CLASS_DIR)/IOSShortArray.o \
	$(CLASS_DIR)/java/lang/System.o \
	$(CLASS_DIR)/java/lang/Throwable.o \
	$(CLASS_DIR)/java/lang/reflect/AccessibleObject.o \
	$(CLASS_DIR)/java/lang/reflect/Constructor.o \
	$(CLASS_DIR)/java/lang/reflect/ExecutableMember.o \
	$(CLASS_DIR)/java/lang/reflect/Field.o \
	$(CLASS_DIR)/java/lang/reflect/Method.o \
	$(CLASS_DIR)/java/util/HashMap.o \
	$(CLASS_DIR)/java/util/LinkedHashMap.o \
	$(CLASS_DIR)/JreEmulation.o \
	$(CLASS_DIR)/NSException+StackTrace.o \
	$(CLASS_DIR)/NSData+IOSByteArray.o \
	$(CLASS_DIR)/NSObject+JavaObject.o \
	$(CLASS_DIR)/NSString+JavaString.o

JRE_INTERFACES = \
	$(CLASS_DIR)/java/io/Closeable.h \
	$(CLASS_DIR)/java/io/FileFilter.h \
	$(CLASS_DIR)/java/io/FilenameFilter.h \
	$(CLASS_DIR)/java/io/Flushable.h \
	$(CLASS_DIR)/java/io/Serializable.h \
	$(CLASS_DIR)/java/lang/Appendable.h \
	$(CLASS_DIR)/java/lang/Iterable.h \
	$(CLASS_DIR)/java/lang/Runnable.h \
	$(CLASS_DIR)/java/lang/annotation/Annotation.h \
	$(CLASS_DIR)/java/lang/reflect/GenericDeclaration.h \
	$(CLASS_DIR)/java/lang/reflect/Type.h \
	$(CLASS_DIR)/java/lang/reflect/TypeVariable.h \
	$(CLASS_DIR)/java/util/Collection.h \
	$(CLASS_DIR)/java/util/Comparator.h \
	$(CLASS_DIR)/java/util/Deque.o \
	$(CLASS_DIR)/java/util/Dictionary.h \
	$(CLASS_DIR)/java/util/Enumeration.h \
	$(CLASS_DIR)/java/util/EventListener.h \
	$(CLASS_DIR)/java/util/Iterator.h \
	$(CLASS_DIR)/java/util/List.h \
	$(CLASS_DIR)/java/util/ListIterator.h \
	$(CLASS_DIR)/java/util/Map.h \
	$(CLASS_DIR)/java/util/NavigableMap.h \
	$(CLASS_DIR)/java/util/NavigableSet.h \
	$(CLASS_DIR)/java/util/Queue.h \
	$(CLASS_DIR)/java/util/RandomAccess.h \
	$(CLASS_DIR)/java/util/Set.h \
	$(CLASS_DIR)/java/util/SortedMap.h \
	$(CLASS_DIR)/java/util/SortedSet.h \
	$(CLASS_DIR)/java/util/concurrent/ConcurrentMap.h \
	$(CLASS_DIR)/java/util/concurrent/Executor.h \
	$(CLASS_DIR)/java/util/concurrent/ExecutorService.h \
	$(CLASS_DIR)/java/util/concurrent/Future.h

OBJS = \
	$(CLASS_DIR)/java/io/BufferedInputStream.o \
	$(CLASS_DIR)/java/io/BufferedOutputStream.o \
	$(CLASS_DIR)/java/io/BufferedReader.o \
	$(CLASS_DIR)/java/io/BufferedWriter.o \
	$(CLASS_DIR)/java/io/ByteArrayInputStream.o \
	$(CLASS_DIR)/java/io/ByteArrayOutputStream.o \
	$(CLASS_DIR)/java/io/CharArrayReader.o \
	$(CLASS_DIR)/java/io/CharArrayWriter.o \
	$(CLASS_DIR)/java/io/File.o \
	$(CLASS_DIR)/java/io/FileDescriptor.o \
	$(CLASS_DIR)/java/io/FileInputStream.o \
	$(CLASS_DIR)/java/io/FileNotFoundException.o \
	$(CLASS_DIR)/java/io/FileOutputStream.o \
	$(CLASS_DIR)/java/io/FileReader.o \
	$(CLASS_DIR)/java/io/FileWriter.o \
	$(CLASS_DIR)/java/io/FilterInputStream.o \
	$(CLASS_DIR)/java/io/FilterOutputStream.o \
	$(CLASS_DIR)/java/io/InputStream.o \
	$(CLASS_DIR)/java/io/InputStreamReader.o \
	$(CLASS_DIR)/java/io/InterruptedIOException.o \
	$(CLASS_DIR)/java/io/InvalidObjectException.o \
	$(CLASS_DIR)/java/io/IOException.o \
	$(CLASS_DIR)/java/io/ObjectStreamException.o \
	$(CLASS_DIR)/java/io/OutputStream.o \
	$(CLASS_DIR)/java/io/OutputStreamWriter.o \
	$(CLASS_DIR)/java/io/PrintStream.o \
	$(CLASS_DIR)/java/io/PrintWriter.o \
	$(CLASS_DIR)/java/io/Reader.o \
	$(CLASS_DIR)/java/io/StringReader.o \
	$(CLASS_DIR)/java/io/StringWriter.o \
	$(CLASS_DIR)/java/io/SyncFailedException.o \
	$(CLASS_DIR)/java/io/UnsupportedEncodingException.o \
	$(CLASS_DIR)/java/io/Writer.o \
	$(CLASS_DIR)/java/lang/ArithmeticException.o \
	$(CLASS_DIR)/java/lang/AssertionError.o \
	$(CLASS_DIR)/java/lang/ArrayIndexOutOfBoundsException.o \
	$(CLASS_DIR)/java/lang/ArrayStoreException.o \
	$(CLASS_DIR)/java/lang/Boolean.o \
	$(CLASS_DIR)/java/lang/Byte.o \
	$(CLASS_DIR)/java/lang/Character.o \
	$(CLASS_DIR)/java/lang/ClassCastException.o \
	$(CLASS_DIR)/java/lang/ClassNotFoundException.o \
	$(CLASS_DIR)/java/lang/CloneNotSupportedException.o \
	$(CLASS_DIR)/java/lang/Double.o \
	$(CLASS_DIR)/java/lang/Enum.o \
	$(CLASS_DIR)/java/lang/Error.o \
	$(CLASS_DIR)/java/lang/Exception.o \
	$(CLASS_DIR)/java/lang/ExceptionInInitializerError.o \
	$(CLASS_DIR)/java/lang/Float.o \
	$(CLASS_DIR)/java/lang/IllegalAccessException.o \
	$(CLASS_DIR)/java/lang/IllegalArgumentException.o \
	$(CLASS_DIR)/java/lang/IllegalStateException.o \
	$(CLASS_DIR)/java/lang/IllegalThreadStateException.o \
	$(CLASS_DIR)/java/lang/InstantiationException.o \
	$(CLASS_DIR)/java/lang/InternalError.o \
	$(CLASS_DIR)/java/lang/IndexOutOfBoundsException.o \
	$(CLASS_DIR)/java/lang/Integer.o \
	$(CLASS_DIR)/java/lang/InterruptedException.o \
	$(CLASS_DIR)/java/lang/LinkageError.o \
	$(CLASS_DIR)/java/lang/Long.o \
	$(CLASS_DIR)/java/lang/Math.o \
	$(CLASS_DIR)/java/lang/NegativeArraySizeException.o \
	$(CLASS_DIR)/java/lang/NoSuchFieldException.o \
	$(CLASS_DIR)/java/lang/NoSuchMethodException.o \
	$(CLASS_DIR)/java/lang/NullPointerException.o \
	$(CLASS_DIR)/java/lang/NumberFormatException.o \
	$(CLASS_DIR)/java/lang/Readable.o \
	$(CLASS_DIR)/java/lang/reflect/Array.o \
	$(CLASS_DIR)/java/lang/reflect/InvocationTargetException.o \
	$(CLASS_DIR)/java/lang/reflect/Modifier.o \
	$(CLASS_DIR)/java/lang/RuntimeException.o \
	$(CLASS_DIR)/java/lang/SecurityException.o \
	$(CLASS_DIR)/java/lang/Short.o \
	$(CLASS_DIR)/java/lang/StackTraceElement.o \
	$(CLASS_DIR)/java/lang/StrictMath.o \
	$(CLASS_DIR)/java/lang/StringBuffer.o \
	$(CLASS_DIR)/java/lang/StringBuilder.o \
	$(CLASS_DIR)/java/lang/StringIndexOutOfBoundsException.o \
	$(CLASS_DIR)/java/lang/Thread.o \
	$(CLASS_DIR)/java/lang/ThreadDeath.o \
	$(CLASS_DIR)/java/lang/ThreadGroup.o \
	$(CLASS_DIR)/java/lang/ThreadLocal.o \
	$(CLASS_DIR)/java/lang/UnsupportedOperationException.o \
	$(CLASS_DIR)/java/lang/Void.o \
	$(CLASS_DIR)/java/math/BigDecimal.o \
	$(CLASS_DIR)/java/math/BigInteger.o \
	$(CLASS_DIR)/java/math/BitLevel.o \
	$(CLASS_DIR)/java/math/Conversion.o \
	$(CLASS_DIR)/java/math/Division.o \
	$(CLASS_DIR)/java/math/Elementary.o \
	$(CLASS_DIR)/java/math/Logical.o \
	$(CLASS_DIR)/java/math/MathContext.o \
	$(CLASS_DIR)/java/math/Multiplication.o \
	$(CLASS_DIR)/java/math/Primality.o \
	$(CLASS_DIR)/java/math/RoundingMode.o \
	$(CLASS_DIR)/java/nio/Buffer.o \
	$(CLASS_DIR)/java/nio/BufferFactory.o \
	$(CLASS_DIR)/java/nio/BufferOverflowException.o \
	$(CLASS_DIR)/java/nio/BufferUnderflowException.o \
	$(CLASS_DIR)/java/nio/ByteBuffer.o \
	$(CLASS_DIR)/java/nio/ByteOrder.o \
	$(CLASS_DIR)/java/nio/CharArrayBuffer.o \
	$(CLASS_DIR)/java/nio/CharBuffer.o \
	$(CLASS_DIR)/java/nio/CharSequenceAdapter.o \
	$(CLASS_DIR)/java/nio/DoubleArrayBuffer.o \
	$(CLASS_DIR)/java/nio/DoubleBuffer.o \
	$(CLASS_DIR)/java/nio/FloatArrayBuffer.o \
	$(CLASS_DIR)/java/nio/FloatBuffer.o \
	$(CLASS_DIR)/java/nio/HeapByteBuffer.o \
	$(CLASS_DIR)/java/nio/IntArrayBuffer.o \
	$(CLASS_DIR)/java/nio/IntBuffer.o \
	$(CLASS_DIR)/java/nio/InvalidMarkException.o \
	$(CLASS_DIR)/java/nio/LongArrayBuffer.o \
	$(CLASS_DIR)/java/nio/LongBuffer.o \
	$(CLASS_DIR)/java/nio/ReadOnlyBufferException.o \
	$(CLASS_DIR)/java/nio/ReadOnlyCharArrayBuffer.o \
	$(CLASS_DIR)/java/nio/ReadOnlyDoubleArrayBuffer.o \
	$(CLASS_DIR)/java/nio/ReadOnlyFloatArrayBuffer.o \
	$(CLASS_DIR)/java/nio/ReadOnlyHeapByteBuffer.o \
	$(CLASS_DIR)/java/nio/ReadOnlyIntArrayBuffer.o \
	$(CLASS_DIR)/java/nio/ReadOnlyLongArrayBuffer.o \
	$(CLASS_DIR)/java/nio/ReadOnlyShortArrayBuffer.o \
	$(CLASS_DIR)/java/nio/ReadWriteCharArrayBuffer.o \
	$(CLASS_DIR)/java/nio/ReadWriteDoubleArrayBuffer.o \
	$(CLASS_DIR)/java/nio/ReadWriteFloatArrayBuffer.o \
	$(CLASS_DIR)/java/nio/ReadWriteHeapByteBuffer.o \
	$(CLASS_DIR)/java/nio/ReadWriteIntArrayBuffer.o \
	$(CLASS_DIR)/java/nio/ReadWriteLongArrayBuffer.o \
	$(CLASS_DIR)/java/nio/ReadWriteShortArrayBuffer.o \
	$(CLASS_DIR)/java/nio/ShortArrayBuffer.o \
	$(CLASS_DIR)/java/nio/ShortBuffer.o \
	$(CLASS_DIR)/java/util/AbstractCollection.o \
	$(CLASS_DIR)/java/util/AbstractList.o \
	$(CLASS_DIR)/java/util/AbstractMap.o \
	$(CLASS_DIR)/java/util/AbstractQueue.o \
	$(CLASS_DIR)/java/util/AbstractSequentialList.o \
	$(CLASS_DIR)/java/util/AbstractSet.o \
	$(CLASS_DIR)/java/util/ArrayDeque.o \
	$(CLASS_DIR)/java/util/ArrayList.o \
	$(CLASS_DIR)/java/util/Arrays.o \
	$(CLASS_DIR)/java/util/BitSet.o \
	$(CLASS_DIR)/java/util/Collections.o \
	$(CLASS_DIR)/java/util/ConcurrentModificationException.o \
	$(CLASS_DIR)/java/util/concurrent/atomic/AtomicInteger.o \
	$(CLASS_DIR)/java/util/concurrent/atomic/AtomicLong.o \
	$(CLASS_DIR)/java/util/concurrent/ConcurrentHashMap.o \
	$(CLASS_DIR)/java/util/concurrent/Executor.o \
	$(CLASS_DIR)/java/util/concurrent/ExecutorService.o \
	$(CLASS_DIR)/java/util/concurrent/Future.o \
	$(CLASS_DIR)/java/util/concurrent/TimeUnit.o \
	$(CLASS_DIR)/java/util/Date.o \
	$(CLASS_DIR)/java/util/Dictionary.o \
	$(CLASS_DIR)/java/util/EventListener.o \
	$(CLASS_DIR)/java/util/EmptyStackException.o \
	$(CLASS_DIR)/java/util/HashSet.o \
	$(CLASS_DIR)/java/util/Hashtable.o \
	$(CLASS_DIR)/java/util/IdentityHashMap.o \
	$(CLASS_DIR)/java/util/InputMismatchException.o \
	$(CLASS_DIR)/java/util/LinkedList.o \
	$(CLASS_DIR)/java/util/LinkedHashSet.o \
	$(CLASS_DIR)/java/util/Locale.o \
	$(CLASS_DIR)/java/util/logging/Formatter.o \
	$(CLASS_DIR)/java/util/logging/Handler.o \
	$(CLASS_DIR)/java/util/logging/Level.o \
	$(CLASS_DIR)/java/util/logging/Logger.o \
	$(CLASS_DIR)/java/util/logging/LogManager.o \
	$(CLASS_DIR)/java/util/logging/LogRecord.o \
	$(CLASS_DIR)/java/util/logging/NSLogHandler.o \
	$(CLASS_DIR)/java/util/MapEntry.o \
	$(CLASS_DIR)/java/util/NavigableMap.o \
	$(CLASS_DIR)/java/util/NavigableSet.o \
	$(CLASS_DIR)/java/util/NoSuchElementException.o \
	$(CLASS_DIR)/java/util/PriorityQueue.o \
	$(CLASS_DIR)/java/util/Properties.o \
	$(CLASS_DIR)/java/util/Random.o \
	$(CLASS_DIR)/java/util/Stack.o \
	$(CLASS_DIR)/java/util/TreeMap.o \
	$(CLASS_DIR)/java/util/TreeSet.o \
	$(CLASS_DIR)/java/util/Vector.o \
	$(CLASS_DIR)/java/util/regex/Matcher.o \
	$(CLASS_DIR)/java/util/regex/MatchResult.o \
	$(CLASS_DIR)/java/util/regex/MatchResultImpl.o \
	$(CLASS_DIR)/java/util/regex/Pattern.o \
	$(CLASS_DIR)/java/util/regex/PatternSyntaxException.o \
	$(CLASS_DIR)/java/util/regex/Splitter.o \
	$(CLASS_DIR)/org/apache/harmony/luni/platform/Endianness.o

JAVA_SOURCE_LIST = $(BUILD_DIR)/jre_emul.classes.list
OBJC_OUT_LIST = $(BUILD_DIR)/jre_emul.objc.list

ifndef CONFIGURATION_BUILD_DIR
# Settings for building both Mac OS X, iPhone, and iPhone simulator libraries.
BUILD_FAT_LIBARY = YES
EMULATION_MACOSX_LIB = $(BUILD_DIR)/macosx-libjre_emul.a
EMULATION_IPHONE_LIB = $(BUILD_DIR)/iphone-libjre_emul.a
EMULATION_SIMULATOR_LIB = $(BUILD_DIR)/simulator-libjre_emul.a
IPHONE_BUILD_DIR = $(BUILD_DIR)/iphone
IPHONE_OBJS = $(OBJS:$(CLASS_DIR)/%=$(IPHONE_BUILD_DIR)/%) \
  $(IOS_OBJS:$(CLASS_DIR)/%=$(IPHONE_BUILD_DIR)/%)
SIMULATOR_BUILD_DIR = $(BUILD_DIR)/simulator
SIMULATOR_OBJS = $(OBJS:$(CLASS_DIR)/%=$(SIMULATOR_BUILD_DIR)/%) \
  $(IOS_OBJS:$(CLASS_DIR)/%=$(SIMULATOR_BUILD_DIR)/%)

# TODO(user): determine thes directories during build.
IPHONE_SDK_DIR = \
  /Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS5.1.sdk
SIMULATOR_SDK_DIR = \
  /Applications/Xcode.app/Contents/Developer/Platforms/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator5.1.sdk

export ALT_ARCH_FLAGS
export ALT_SDK_FLAGS
endif

# translate is the default so that Xcode can build its own object files
default: lib includes
	@: # suppress make's "nothing to be done" message

translate: j2objc $(EMULATION_JAR) pre_translate $(JRE_INTERFACES) $(OBJS:.o=.m)
	@if [ `cat $(JAVA_SOURCE_LIST) | wc -l` -ge 1 ] ; then \
	  rm -f `cat $(OBJC_OUT_LIST)`; \
	  $(J2OBJC) --inline-field-access -sourcepath .:$(JRE_SRC) `cat $(JAVA_SOURCE_LIST)` ; \
	fi

pre_translate:
	@rm -f $(JAVA_SOURCE_LIST) $(OBJC_OUT_LIST)
	@touch $(JAVA_SOURCE_LIST) $(OBJC_OUT_LIST)

lib: translate $(EMULATION_LIB)
	@:

$(EMULATION_LIB): $(IOS_OBJS) $(OBJS)
	@if [ "x$(BUILD_FAT_LIBARY)" == "xYES" ] ; then \
	  echo "building Mac OS X library" ; \
	  libtool -static -o $(EMULATION_MACOSX_LIB) `find $(CLASS_DIR) -name '*.o'` ; \
	  echo "building iPhoneOS library" ; \
	  env ALT_ARCH_FLAGS="-arch armv7" ALT_SDK_FLAGS="-isysroot $(IPHONE_SDK_DIR)" \
	    make iphone_lib ; \
	  echo "building iPhoneSimulator library" ; \
	  env ALT_ARCH_FLAGS="-arch i386" ALT_SDK_FLAGS="-isysroot $(SIMULATOR_SDK_DIR)" \
	    make simulator_lib ; \
	  lipo -create $(EMULATION_MACOSX_LIB) $(EMULATION_IPHONE_LIB) \
	    $(EMULATION_SIMULATOR_LIB) -output $(EMULATION_LIB) ; \
	else \
	  libtool -static -o $(EMULATION_LIB) `find $(CLASS_DIR) -name '*.o'` ; \
	fi

iphone_lib: $(IPHONE_OBJS)
	libtool -static -o $(EMULATION_IPHONE_LIB) `find $(IPHONE_BUILD_DIR) -name '*.o'` ; \

simulator_lib: $(SIMULATOR_OBJS)
	libtool -static -o $(EMULATION_SIMULATOR_LIB) `find $(SIMULATOR_BUILD_DIR) -name '*.o'` ; \

# Copy top-level class headers to include directory.
includes: $(INCLUDE_DIR)/java/lang/reflect
	install -C $(EMULATION_CLASS_DIR)/*.h $(INCLUDE_DIR)
	cd $(CLASS_DIR) && tar cf - --exclude '*.[mo]' * | ( cd $(INCLUDE_DIR); tar xfp - )
	install -C $(EMULATION_CLASS_DIR)/java/lang/*.h $(INCLUDE_DIR)/java/lang/
	install -C $(EMULATION_CLASS_DIR)/java/lang/reflect/*.h \
	  $(INCLUDE_DIR)/java/lang/reflect/
	install -C $(EMULATION_CLASS_DIR)/java/util/*.h $(INCLUDE_DIR)/java/util/
	find $(INCLUDE_DIR) -type f | xargs chmod 444

$(BUILD_DIR) $(INCLUDE_DIR)/java/lang/reflect:
	@mkdir -p $@

$(CLASS_DIR)/%.h $(CLASS_DIR)/%.m: %.java
	@echo "$?" >> $(JAVA_SOURCE_LIST)
	@echo "$@" >> $(OBJC_OUT_LIST)
	@if [ -e $@ ]; then touch $@; fi

$(CLASS_DIR)/%.o: $(CLASS_DIR)/%.m
	$(CLANG) -c '$?' -o '$@' $(OBJCFLAGS) -I$(EMULATION_CLASS_DIR) -I$(CLASS_DIR)

$(CLASS_DIR)/%.o: $(EMULATION_CLASS_DIR)/%.m
	@mkdir -p `dirname $@`
	$(CLANG) -c $? -o $@ $(OBJCFLAGS) -I$(EMULATION_CLASS_DIR) -I$(CLASS_DIR)

$(IPHONE_BUILD_DIR)/%.o: $(CLASS_DIR)/%.m
	@mkdir -p `dirname $@`
	$(CLANG) -c '$?' -o '$@' $(OBJCFLAGS) -I$(EMULATION_CLASS_DIR) -I$(CLASS_DIR)

$(IPHONE_BUILD_DIR)/%.o: $(EMULATION_CLASS_DIR)/%.m
	@mkdir -p `dirname $@`
	$(CLANG) -c $? -o $@ $(OBJCFLAGS) -I$(EMULATION_CLASS_DIR) -I$(CLASS_DIR)

$(SIMULATOR_BUILD_DIR)/%.o: $(CLASS_DIR)/%.m
	@mkdir -p `dirname $@`
	$(CLANG) -c '$?' -o '$@' $(OBJCFLAGS) -I$(EMULATION_CLASS_DIR) -I$(CLASS_DIR)

$(SIMULATOR_BUILD_DIR)/%.o: $(EMULATION_CLASS_DIR)/%.m
	@mkdir -p `dirname $@`
	$(CLANG) -c $? -o $@ $(OBJCFLAGS) -I$(EMULATION_CLASS_DIR) -I$(CLASS_DIR)

test: default
	$(MAKE) -f tests.mk test

clean:
	@rm -rf $(CLASS_DIR) $(EMULATION_JAR) $(EMULATION_LIB) $(INCLUDE_DIR) \
	  $(BUILD_DIR)/*.octest $(DIST_LIB_DIR)/jre_emul.jar $(DIST_INCLUDE_DIR)/java
	@for i in $(CLASS_DIR)/*.h; do rm -f $(DIST_INCLUDE_DIR)/$${i#$(CLASS_DIR)/}; done
	@$(MAKE) -f tests.mk clean
	@rm -f $(JAVA_SOURCE_LIST)
	@rm -rf $(BUILD_DIR)

dist: lib includes $(EMULATION_JAR)
	@mkdir -p $(DIST_LIB_DIR)
	@mkdir -p $(DIST_INCLUDE_DIR)
	cp -r $(INCLUDE_DIR)/* $(DIST_INCLUDE_DIR)
	@find $(DIST_INCLUDE_DIR) -type f | xargs chmod 0644
	install -m 0644 $(EMULATION_LIB) $(EMULATION_JAR) $(DIST_LIB_DIR)

$(EMULATION_JAR): $(JRE_ROOT)/java/*/*.java | $(BUILD_DIR)
	@echo "building jre_emul.jar"
	@rm -rf $(EMULATION_STAGE)
	@mkdir $(EMULATION_STAGE)
	@javac -d $(EMULATION_STAGE) -sourcepath ../src/main/java \
		  ../src/main/java/com/google/j2objc/annotations/*.java
	@javac -classpath $(EMULATION_STAGE) -d $(EMULATION_STAGE) -sourcepath $(JRE_SRC) \
	  `find $(JRE_SRC_ROOTS) -name '*.java'`
	@jar cf $(EMULATION_JAR) -C $(EMULATION_STAGE) .

$(CLASS_DIR)/NSObject+JavaObject.o: $(EMULATION_CLASS_DIR)/NSObject+JavaObject.m
	$(CLANG) -c $? -o $@ $(OBJCFLAGS) -I$(EMULATION_CLASS_DIR) -I$(CLASS_DIR) \
	    -Wno-deprecated-declarations

$(CLASS_DIR)/java/lang/Math.h $(CLASS_DIR)/java/lang/Math.m: \
	  $(EMULATION_CLASS_DIR)/java/lang/Math.java
	$(J2OBJC) $?

$(CLASS_DIR)/java/lang/System.h $(CLASS_DIR)/java/lang/System.m: \
	  $(EMULATION_CLASS_DIR)/java/lang/System.java
	$(J2OBJC) $?

$(CLASS_DIR)/java/util/ArrayDeque.h $(CLASS_DIR)/java/util/ArrayDeque.m: \
	  $(EMULATION_CLASS_DIR)/java/util/ArrayDeque.java
	$(J2OBJC) $?

$(CLASS_DIR)/java/util/Date.h $(CLASS_DIR)/java/util/Date.m: \
	  $(EMULATION_CLASS_DIR)/java/util/Date.java
	$(J2OBJC) $?

$(CLASS_DIR)/java/util/Deque.h $(CLASS_DIR)/java/util/Deque.m: \
	  $(EMULATION_CLASS_DIR)/java/util/Deque.java
	$(J2OBJC) $?

$(CLASS_DIR)/java/util/EventListener.h $(CLASS_DIR)/java/util/EventListener.m: \
	  $(EMULATION_CLASS_DIR)/java/util/EventListener.java
	$(J2OBJC) $?

$(CLASS_DIR)/java/util/NavigableMap.h $(CLASS_DIR)/java/util/NavigableMap.m: \
	  $(EMULATION_CLASS_DIR)/java/util/NavigableMap.java
	$(J2OBJC) $?

$(CLASS_DIR)/java/util/NavigableSet.h $(CLASS_DIR)/java/util/NavigableSet.m: \
	  $(EMULATION_CLASS_DIR)/java/util/NavigableSet.java
	$(J2OBJC) $?

$(CLASS_DIR)/java/util/concurrent/Future.h $(CLASS_DIR)/java/util/concurrent/Future.m: \
	  $(EMULATION_CLASS_DIR)/java/util/concurrent/Future.java
	$(J2OBJC) $?

$(CLASS_DIR)/java/util/concurrent/Executor.h $(CLASS_DIR)/java/util/concurrent/Executor.m: \
	  $(EMULATION_CLASS_DIR)/java/util/concurrent/Executor.java
	$(J2OBJC) $?

$(CLASS_DIR)/java/util/concurrent/ExecutorService.h $(CLASS_DIR)/java/util/concurrent/ExecutorService.m: \
	  $(EMULATION_CLASS_DIR)/java/util/concurrent/ExecutorService.java
	$(J2OBJC) $?

$(CLASS_DIR)/java/util/regex/PatternSyntaxException.h \
    $(CLASS_DIR)/java/util/regex/PatternSyntaxException.m: \
	  $(EMULATION_CLASS_DIR)/java/util/regex/PatternSyntaxException.java
	$(J2OBJC) $?
