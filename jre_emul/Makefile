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

# Native obj-c sources that do not emulate a specific java class.
IOS_OBJS = \
	IOSArray.o \
	IOSArrayClass.o \
	IOSBooleanArray.o \
	IOSByteArray.o \
	IOSCharArray.o \
	IOSClass.o \
	IOSDoubleArray.o \
	IOSFloatArray.o \
	IOSIntArray.o \
	IOSLongArray.o \
	IOSObjectArray.o \
	IOSPrimitiveClass.o \
	IOSShortArray.o \
	JreEmulation.o \
	JreMemDebug.o \
	JreMemDebugStrongReference.o \
	NSObject+JavaObject.o \
	NSString+JavaString.o \
	java/lang/reflect/ExecutableMember.o

# Java classes with hand written obj-c implementations. Shouldn't be translated,
# but need to include the .java file in jre_emul.jar and the .m file in
# libjre_emul.a.
IOS_JAVA_OBJS = \
	java/lang/Throwable.o \
	java/lang/reflect/AccessibleObject.o \
	java/lang/reflect/Constructor.o \
	java/lang/reflect/Field.o \
	java/lang/reflect/Method.o \
	java/util/HashMap.o \
	java/util/LinkedHashMap.o

# Java interfaces with hand written obj-c headers. Don't need to be translated,
# but need to be included in the jre_emul.jar.
IOS_INTERFACES = \
	java/lang/CharSequence.h \
	java/lang/Cloneable.h \
	java/lang/Comparable.h

# Java interfaces that will generate empty .m files.
JRE_INTERFACES = \
	java/io/Closeable.h \
	java/io/FileFilter.h \
	java/io/FilenameFilter.h \
	java/io/Flushable.h \
	java/io/Serializable.h \
	java/lang/Appendable.h \
	java/lang/Iterable.h \
	java/lang/Runnable.h \
	java/lang/annotation/Annotation.h \
	java/lang/reflect/GenericDeclaration.h \
	java/lang/reflect/Type.h \
	java/lang/reflect/TypeVariable.h \
	java/util/Collection.h \
	java/util/Comparator.h \
	java/util/Deque.h \
	java/util/Dictionary.h \
	java/util/Enumeration.h \
	java/util/EventListener.h \
	java/util/Iterator.h \
	java/util/List.h \
	java/util/ListIterator.h \
	java/util/Map.h \
	java/util/NavigableMap.h \
	java/util/NavigableSet.h \
	java/util/Queue.h \
	java/util/RandomAccess.h \
	java/util/Set.h \
	java/util/SortedMap.h \
	java/util/SortedSet.h \
	java/util/concurrent/ConcurrentMap.h \
	java/util/concurrent/Executor.h \
	java/util/concurrent/ExecutorService.h \
	java/util/concurrent/Future.h

# Java sources to be translated normally.
JAVA_SOURCES = \
	java/io/BufferedInputStream.java \
	java/io/BufferedOutputStream.java \
	java/io/BufferedReader.java \
	java/io/BufferedWriter.java \
	java/io/ByteArrayInputStream.java \
	java/io/ByteArrayOutputStream.java \
	java/io/CharArrayReader.java \
	java/io/CharArrayWriter.java \
	java/io/File.java \
	java/io/FileDescriptor.java \
	java/io/FileInputStream.java \
	java/io/FileNotFoundException.java \
	java/io/FileOutputStream.java \
	java/io/FileReader.java \
	java/io/FileWriter.java \
	java/io/FilterInputStream.java \
	java/io/FilterOutputStream.java \
	java/io/InputStream.java \
	java/io/InputStreamReader.java \
	java/io/InterruptedIOException.java \
	java/io/InvalidObjectException.java \
	java/io/IOException.java \
	java/io/ObjectStreamException.java \
	java/io/OutputStream.java \
	java/io/OutputStreamWriter.java \
	java/io/PrintStream.java \
	java/io/PrintWriter.java \
	java/io/Reader.java \
	java/io/StringReader.java \
	java/io/StringWriter.java \
	java/io/SyncFailedException.java \
	java/io/UnsupportedEncodingException.java \
	java/io/Writer.java \
	java/lang/ArithmeticException.java \
	java/lang/AssertionError.java \
	java/lang/ArrayIndexOutOfBoundsException.java \
	java/lang/ArrayStoreException.java \
	java/lang/Boolean.java \
	java/lang/Byte.java \
	java/lang/Character.java \
	java/lang/ClassCastException.java \
	java/lang/ClassNotFoundException.java \
	java/lang/CloneNotSupportedException.java \
	java/lang/Double.java \
	java/lang/Enum.java \
	java/lang/Error.java \
	java/lang/Exception.java \
	java/lang/ExceptionInInitializerError.java \
	java/lang/Float.java \
	java/lang/IllegalAccessException.java \
	java/lang/IllegalArgumentException.java \
	java/lang/IllegalStateException.java \
	java/lang/IllegalThreadStateException.java \
	java/lang/InstantiationException.java \
	java/lang/InternalError.java \
	java/lang/IndexOutOfBoundsException.java \
	java/lang/Integer.java \
	java/lang/InterruptedException.java \
	java/lang/LinkageError.java \
	java/lang/Long.java \
	java/lang/Math.java \
	java/lang/NegativeArraySizeException.java \
	java/lang/NoSuchFieldException.java \
	java/lang/NoSuchMethodException.java \
	java/lang/NullPointerException.java \
	java/lang/NumberFormatException.java \
	java/lang/Readable.java \
	java/lang/reflect/Array.java \
	java/lang/reflect/InvocationTargetException.java \
	java/lang/reflect/Modifier.java \
	java/lang/RuntimeException.java \
	java/lang/SecurityException.java \
	java/lang/Short.java \
	java/lang/StackTraceElement.java \
	java/lang/StrictMath.java \
	java/lang/StringBuffer.java \
	java/lang/StringBuilder.java \
	java/lang/StringIndexOutOfBoundsException.java \
	java/lang/System.java \
	java/lang/Thread.java \
	java/lang/ThreadDeath.java \
	java/lang/ThreadGroup.java \
	java/lang/ThreadLocal.java \
	java/lang/UnsupportedOperationException.java \
	java/lang/Void.java \
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
	java/nio/Buffer.java \
	java/nio/BufferFactory.java \
	java/nio/BufferOverflowException.java \
	java/nio/BufferUnderflowException.java \
	java/nio/ByteBuffer.java \
	java/nio/ByteOrder.java \
	java/nio/CharArrayBuffer.java \
	java/nio/CharBuffer.java \
	java/nio/CharSequenceAdapter.java \
	java/nio/DoubleArrayBuffer.java \
	java/nio/DoubleBuffer.java \
	java/nio/FloatArrayBuffer.java \
	java/nio/FloatBuffer.java \
	java/nio/HeapByteBuffer.java \
	java/nio/IntArrayBuffer.java \
	java/nio/IntBuffer.java \
	java/nio/InvalidMarkException.java \
	java/nio/LongArrayBuffer.java \
	java/nio/LongBuffer.java \
	java/nio/ReadOnlyBufferException.java \
	java/nio/ReadOnlyCharArrayBuffer.java \
	java/nio/ReadOnlyDoubleArrayBuffer.java \
	java/nio/ReadOnlyFloatArrayBuffer.java \
	java/nio/ReadOnlyHeapByteBuffer.java \
	java/nio/ReadOnlyIntArrayBuffer.java \
	java/nio/ReadOnlyLongArrayBuffer.java \
	java/nio/ReadOnlyShortArrayBuffer.java \
	java/nio/ReadWriteCharArrayBuffer.java \
	java/nio/ReadWriteDoubleArrayBuffer.java \
	java/nio/ReadWriteFloatArrayBuffer.java \
	java/nio/ReadWriteHeapByteBuffer.java \
	java/nio/ReadWriteIntArrayBuffer.java \
	java/nio/ReadWriteLongArrayBuffer.java \
	java/nio/ReadWriteShortArrayBuffer.java \
	java/nio/ShortArrayBuffer.java \
	java/nio/ShortBuffer.java \
	java/text/ParseException.java \
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
	java/util/Collections.java \
	java/util/ConcurrentModificationException.java \
	java/util/concurrent/atomic/AtomicInteger.java \
	java/util/concurrent/atomic/AtomicLong.java \
	java/util/concurrent/ConcurrentHashMap.java \
	java/util/concurrent/Executor.java \
	java/util/concurrent/ExecutorService.java \
	java/util/concurrent/Future.java \
	java/util/concurrent/TimeUnit.java \
	java/util/Currency.java \
	java/util/Date.java \
	java/util/Dictionary.java \
	java/util/EnumSet.java \
	java/util/EventListener.java \
	java/util/EventObject.java \
	java/util/EmptyStackException.java \
	java/util/Grego.java \
	java/util/GregorianCalendar.java \
	java/util/HashSet.java \
	java/util/Hashtable.java \
	java/util/HugeEnumSet.java \
	java/util/IdentityHashMap.java \
	java/util/InputMismatchException.java \
	java/util/LinkedList.java \
	java/util/LinkedHashSet.java \
	java/util/Locale.java \
	java/util/logging/Formatter.java \
	java/util/logging/Handler.java \
	java/util/logging/Level.java \
	java/util/logging/Logger.java \
	java/util/logging/LogManager.java \
	java/util/logging/LogRecord.java \
	java/util/logging/NSLogHandler.java \
	java/util/MapEntry.java \
	java/util/MiniEnumSet.java \
	java/util/NavigableMap.java \
	java/util/NavigableSet.java \
	java/util/NoSuchElementException.java \
	java/util/PriorityQueue.java \
	java/util/Properties.java \
	java/util/Random.java \
	java/util/SimpleTimeZone.java \
	java/util/Stack.java \
	java/util/StringTokenizer.java \
	java/util/TimeZone.java \
	java/util/TreeMap.java \
	java/util/TreeSet.java \
	java/util/Vector.java \
	java/util/regex/Matcher.java \
	java/util/regex/MatchResult.java \
	java/util/regex/MatchResultImpl.java \
	java/util/regex/Pattern.java \
	java/util/regex/PatternSyntaxException.java \
	java/util/regex/Splitter.java \
	org/apache/harmony/luni/platform/Endianness.java

JAVA_SOURCE_LIST = $(BUILD_DIR)/jre_emul.classes.list
OBJC_OUT_LIST = $(BUILD_DIR)/jre_emul.objc.list

STUB_JAVA_SOURCES = $(shell find $(STUBS_DIR) -name *.java)
J2OBJC_ANNOTATION_SOURCES = \
    $(shell find $(J2OBJC_SRC_ROOT)/com/google/j2objc/annotations -name *.java)
ALL_JAVA_SOURCES = $(JAVA_SOURCES) $(IOS_JAVA_OBJS:.o=.java) $(IOS_INTERFACES:.h=.java) \
    $(JRE_INTERFACES:.h=.java) $(STUB_JAVA_SOURCES:$(STUBS_DIR)/%=%) \
    $(J2OBJC_ANNOTATION_SOURCES:$(J2OBJC_SRC_ROOT)/%=%)
TRANSLATED_OBJC = $(JRE_INTERFACES:%=$(CLASS_DIR)/%) $(JAVA_SOURCES:%.java=$(CLASS_DIR)/%.m)
ALL_OBJS = $(JAVA_SOURCES:.java=.o) $(IOS_OBJS) $(IOS_JAVA_OBJS)

ifndef CONFIGURATION_BUILD_DIR
# Settings for building both Mac OS X, iPhone, and iPhone simulator libraries.
BUILD_FAT_LIBARY = YES
EMULATION_MACOSX_LIB = $(BUILD_DIR)/macosx-libjre_emul.a
EMULATION_IPHONE_LIB = $(BUILD_DIR)/iphone-libjre_emul.a
EMULATION_IPHONEV7S_LIB = $(BUILD_DIR)/iphonev7s-libjre_emul.a
EMULATION_SIMULATOR_LIB = $(BUILD_DIR)/simulator-libjre_emul.a
IPHONE_BUILD_DIR = $(BUILD_DIR)/iphone
IPHONE_OBJS = $(ALL_OBJS:%=$(IPHONE_BUILD_DIR)/%)
IPHONEV7S_BUILD_DIR = $(BUILD_DIR)/iphonev7s
IPHONEV7S_OBJS = $(ALL_OBJS:%=$(IPHONEV7S_BUILD_DIR)/%)
SIMULATOR_BUILD_DIR = $(BUILD_DIR)/simulator
SIMULATOR_OBJS = $(ALL_OBJS:%=$(SIMULATOR_BUILD_DIR)/%)

IPHONE_SDK_DIR = $(shell bash ../src/main/make/sysroot_path.sh --iphoneos)
SIMULATOR_SDK_DIR = $(shell bash ../src/main/make/sysroot_path.sh --iphonesimulator)

export ALT_ARCH_FLAGS
export ALT_SDK_FLAGS
endif

# translate is the default so that Xcode can build its own object files
default: lib includes
	@: # suppress make's "nothing to be done" message

analyze: $(OBJS:%.o=$(CLASS_DIR)%.plist)

translate: j2objc $(EMULATION_JAR) pre_translate $(TRANSLATED_OBJC)
	@if [ `cat $(JAVA_SOURCE_LIST) | wc -l` -ge 1 ] ; then \
	  rm -f `cat $(OBJC_OUT_LIST)`; \
	  $(J2OBJC) --inline-field-access -sourcepath .:$(JRE_SRC) `cat $(JAVA_SOURCE_LIST)` ; \
	fi

pre_translate:
	@rm -f $(JAVA_SOURCE_LIST) $(OBJC_OUT_LIST)
	@touch $(JAVA_SOURCE_LIST) $(OBJC_OUT_LIST)

lib: translate $(EMULATION_LIB)
	@:

$(EMULATION_LIB): $(ALL_OBJS:%=$(CLASS_DIR)/%)
	@if [ "x$(BUILD_FAT_LIBARY)" == "xYES" ] ; then \
	  echo "building Mac OS X library" ; \
	  $(LIBTOOL) -static -o $(EMULATION_MACOSX_LIB) $^ ; \
	  echo "building iPhoneOS library" ; \
	  env ALT_ARCH_FLAGS="-arch armv7" ALT_SDK_FLAGS="-isysroot $(IPHONE_SDK_DIR)" \
	    $(MAKE) iphone_lib ; \
	  echo "building iPhonev7S library" ; \
		env ALT_ARCH_FLAGS="-arch armv7s" ALT_SDK_FLAGS="-isysroot $(IPHONE_SDK_DIR)" \
		  $(MAKE) iphonev7s_lib ; \
	  echo "building iPhoneSimulator library" ; \
	  env ALT_ARCH_FLAGS="-arch i386" ALT_SDK_FLAGS="-isysroot $(SIMULATOR_SDK_DIR)" \
	    $(MAKE) simulator_lib ; \
	  $(LIPO) -create $(EMULATION_MACOSX_LIB) $(EMULATION_IPHONE_LIB) \
	    $(EMULATION_SIMULATOR_LIB) $(EMULATION_IPHONEV7S_LIB) -output $(EMULATION_LIB) ; \
	else \
	  $(LIBTOOL) -static -o $(EMULATION_LIB) $^ ; \
	fi

iphone_lib: $(IPHONE_OBJS)
	$(LIBTOOL) -static -o $(EMULATION_IPHONE_LIB) $^ ; \

iphonev7s_lib: $(IPHONEV7S_OBJS)
	$(LIBTOOL) -static -o $(EMULATION_IPHONEV7S_LIB) $^ ; \

simulator_lib: $(SIMULATOR_OBJS)
	$(LIBTOOL) -static -o $(EMULATION_SIMULATOR_LIB) $^ ; \

# Copy top-level class headers to include directory.
includes: $(BUILD_DIR)/.jre_includes

$(BUILD_DIR)/.jre_includes: $(EMULATION_CLASS_DIR)
	mkdir -p $(INCLUDE_DIR)
	install -C $(EMULATION_CLASS_DIR)/*.h $(INCLUDE_DIR)
	cd $(CLASS_DIR) && tar cf - --exclude '*.[mo]' * | ( cd $(INCLUDE_DIR); tar xfp - )
	mkdir -p $(INCLUDE_DIR)/java/lang/
	install -C $(EMULATION_CLASS_DIR)/java/lang/*.h $(INCLUDE_DIR)/java/lang/
	mkdir -p $(INCLUDE_DIR)/java/lang/reflect/
	install -C $(EMULATION_CLASS_DIR)/java/lang/reflect/*.h \
	  $(INCLUDE_DIR)/java/lang/reflect/
	mkdir -p $(INCLUDE_DIR)/java/util/
	install -C $(EMULATION_CLASS_DIR)/java/util/*.h $(INCLUDE_DIR)/java/util/
	find $(INCLUDE_DIR) -type f | xargs chmod 444
	touch $(BUILD_DIR)/.jre_includes

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

$(IPHONEV7S_BUILD_DIR)/%.o: $(CLASS_DIR)/%.m
	@mkdir -p `dirname $@`
	$(CLANG) -c '$?' -o '$@' $(OBJCFLAGS) -I$(EMULATION_CLASS_DIR) -I$(CLASS_DIR)

$(IPHONEV7S_BUILD_DIR)/%.o: $(EMULATION_CLASS_DIR)/%.m
	@mkdir -p `dirname $@`
	$(CLANG) -c $? -o $@ $(OBJCFLAGS) -I$(EMULATION_CLASS_DIR) -I$(CLASS_DIR)

$(SIMULATOR_BUILD_DIR)/%.o: $(CLASS_DIR)/%.m
	@mkdir -p `dirname $@`
	$(CLANG) -c '$?' -o '$@' $(OBJCFLAGS) -I$(EMULATION_CLASS_DIR) -I$(CLASS_DIR)

$(SIMULATOR_BUILD_DIR)/%.o: $(EMULATION_CLASS_DIR)/%.m
	@mkdir -p `dirname $@`
	$(CLANG) -c $? -o $@ $(OBJCFLAGS) -I$(EMULATION_CLASS_DIR) -I$(CLASS_DIR)

$(CLASS_DIR)/%.plist: $(CLASS_DIR)/%.m
	$(CLANG) -c '$?' -o '$@' $(OBJCFLAGS) $(STATIC_ANALYZER_FLAGS) -I$(EMULATION_CLASS_DIR) -I$(CLASS_DIR)

$(CLASS_DIR)/%.plist: $(EMULATION_CLASS_DIR)/%.m
	@mkdir -p `dirname $@`
	$(CLANG) -c $? -o $@ $(OBJCFLAGS) $(STATIC_ANALYZER_FLAGS) -I$(EMULATION_CLASS_DIR) -I$(CLASS_DIR)

$(IPHONE_BUILD_DIR)/%.plist: $(CLASS_DIR)/%.m
	@mkdir -p `dirname $@`
	$(CLANG) -c '$?' -o '$@' $(OBJCFLAGS) $(STATIC_ANALYZER_FLAGS) -I$(EMULATION_CLASS_DIR) -I$(CLASS_DIR)

$(IPHONE_BUILD_DIR)/%.plist: $(EMULATION_CLASS_DIR)/%.m
	@mkdir -p `dirname $@`
	$(CLANG) -c $? -o $@ $(OBJCFLAGS) $(STATIC_ANALYZER_FLAGS) -I$(EMULATION_CLASS_DIR) -I$(CLASS_DIR)

$(IPHONEV7S_BUILD_DIR)/%.plist: $(CLASS_DIR)/%.m
	@mkdir -p `dirname $@`
	$(CLANG) -c '$?' -o '$@' $(OBJCFLAGS) $(STATIC_ANALYZER_FLAGS) -I$(EMULATION_CLASS_DIR) -I$(CLASS_DIR)

$(IPHONEV7S_BUILD_DIR)/%.plist: $(EMULATION_CLASS_DIR)/%.m
	@mkdir -p `dirname $@`
	$(CLANG) -c $? -o $@ $(OBJCFLAGS) $(STATIC_ANALYZER_FLAGS) -I$(EMULATION_CLASS_DIR) -I$(CLASS_DIR)

$(SIMULATOR_BUILD_DIR)/%.plist: $(CLASS_DIR)/%.m
	@mkdir -p `dirname $@`
	$(CLANG) -c '$?' -o '$@' $(OBJCFLAGS) $(STATIC_ANALYZER_FLAGS) -I$(EMULATION_CLASS_DIR) -I$(CLASS_DIR)

$(SIMULATOR_BUILD_DIR)/%.plist: $(EMULATION_CLASS_DIR)/%.m
	@mkdir -p `dirname $@`
	$(CLANG) -c $? -o $@ $(OBJCFLAGS) $(STATIC_ANALYZER_FLAGS) -I$(EMULATION_CLASS_DIR) -I$(CLASS_DIR)

test: default
	$(MAKE) -f tests.mk test

clean:
	@rm -rf $(CLASS_DIR) $(EMULATION_JAR) $(EMULATION_LIB) $(INCLUDE_DIR) \
	  $(BUILD_DIR)/*.octest $(DIST_LIB_DIR)/jre_emul.jar $(DIST_INCLUDE_DIR)/java
	@for i in $(CLASS_DIR)/*.h; do rm -f $(DIST_INCLUDE_DIR)/$${i#$(CLASS_DIR)/}; done
	@$(MAKE) -f tests.mk clean
	@rm -f $(JAVA_SOURCE_LIST)
	@rm -rf $(BUILD_DIR)

dist: lib includes emul_jar_dist dist_includes
	@mkdir -p $(DIST_LIB_DIR)
	install -m 0644 $(EMULATION_LIB) $(DIST_LIB_DIR)

dist_includes: $(BUILD_DIR)/.jre_dist_includes

$(BUILD_DIR)/.jre_dist_includes: $(INCLUDE_DIR)
	@mkdir -p $(DIST_INCLUDE_DIR)
	cp -r $(INCLUDE_DIR)/* $(DIST_INCLUDE_DIR)
	@find $(DIST_INCLUDE_DIR) -type f | xargs chmod 0644
	touch $(BUILD_DIR)/.jre_dist_includes

emul_jar_dist: $(EMULATION_JAR)
	@mkdir -p $(DIST_LIB_DIR)
	install -m 0644 $(EMULATION_JAR) $(DIST_LIB_DIR)

$(EMULATION_JAR): $(ALL_JAVA_SOURCES) | $(BUILD_DIR)
	@echo "building jre_emul.jar"
	@rm -rf $(EMULATION_STAGE)
	@mkdir $(EMULATION_STAGE)
	@javac -classpath $(EMULATION_STAGE) -d $(EMULATION_STAGE) $^
	@jar cf $(EMULATION_JAR) -C $(EMULATION_STAGE) .
