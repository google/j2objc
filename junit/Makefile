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

# Makefile for building the iOS junit library.
#
# Author: Tom Ball

.SUFFIXES: .java .m
.PHONY: default j2objc clean

include environment.mk

JAVA_SOURCES = \
    junit/framework/Assert.java \
    junit/framework/AssertionFailedError.java \
    junit/framework/ComparisonCompactor.java \
    junit/framework/ComparisonFailure.java \
    junit/framework/Protectable.java \
    junit/framework/Test.java \
    junit/framework/TestCase.java \
    junit/framework/TestFailure.java \
    junit/framework/TestListener.java \
    junit/framework/TestResult.java \
    junit/framework/TestSuite.java \
    junit/runner/BaseTestRunner.java \
    junit/runner/TestRunListener.java \
    junit/textui/ResultPrinter.java


OBJC_SOURCES = \
    JUnitRunner.m

MODIFIED_SOURCES = \
    junit/textui/TestRunner.java \
    junit/runner/Version.java

OBJS = $(JAVA_SOURCES:%.java=%.o) $(OBJC_SOURCES:%.m=%.o) \
    $(MODIFIED_SOURCES:%.java=%.o)

ARCH_OBJS = $(OBJS:%.o=$(ARCH_BUILD_DIR)/%.o)

IPHONE_SDK_DIR = $(shell bash ../scripts/sysroot_path.sh --iphoneos)
SIMULATOR_SDK_DIR = $(shell bash ../scripts/sysroot_path.sh --iphonesimulator)

JAVA_SOURCE_LIST = $(TMPDIR).classes.list
EXTRACTED_JAVA = $(JAVA_SOURCES:%=$(JAVA_SRC_DIR)/%)
TRANSLATED_OBJC = $(JAVA_SOURCES:%.java=$(BUILD_DIR)/%.m) \
    $(MODIFIED_SOURCES:%.java=$(BUILD_DIR)/%.m)
SOURCE_HEADERS = $(shell find $(OBJC_SOURCE_DIR) -name '*.h')
SOURCE_HEADERS := $(SOURCE_HEADERS:$(OBJC_SOURCE_DIR)/%=%)
GENERATED_HEADERS = $(JAVA_SOURCES:%.java=%.h) $(MODIFIED_SOURCES:%.java=%.h)
RELATIVE_HEADERS = $(SOURCE_HEADERS) $(GENERATED_HEADERS)
HEADERS = $(SOURCE_HEADERS:%=$(OBJC_SOURCE_DIR)/%) \
    $(GENERATED_HEADERS:%=$(BUILD_DIR)/%)
ARCH_HEADERS = $(RELATIVE_HEADERS:%=$(ARCH_INCLUDE_DIR)/%)

# Settings for building Mac OS X, iPhone, and iPhone simulator libraries.
EMULATION_MACOSX_LIB = $(BUILD_DIR)/macosx-libjunit.a
EMULATION_IPHONE_LIB = $(BUILD_DIR)/iphone-libjunit.a
EMULATION_IPHONEV7S_LIB = $(BUILD_DIR)/iphonev7s-libjunit.a
EMULATION_SIMULATOR_LIB = $(BUILD_DIR)/simulator-libjunit.a
MACOSX_OBJS = $(OBJS:%=$(BUILD_DIR)/%)
MACOSX_FLAGS = $(SDK_FLAGS)
IPHONE_BUILD_DIR = $(BUILD_DIR)/iphone
IPHONE_OBJS = $(OBJS:%=$(IPHONE_BUILD_DIR)/%)
IPHONE_FLAGS = -arch armv7 -isysroot $(IPHONE_SDK_DIR)
IPHONEV7S_BUILD_DIR = $(BUILD_DIR)/iphonev7s
IPHONEV7S_OBJS = $(OBJS:%=$(IPHONEV7S_BUILD_DIR)/%)
IPHONEV7S_FLAGS = -arch armv7s -isysroot $(IPHONE_SDK_DIR)
SIMULATOR_BUILD_DIR = $(BUILD_DIR)/simulator
SIMULATOR_OBJS = $(OBJS:%=$(SIMULATOR_BUILD_DIR)/%)
SIMULATOR_FLAGS = -arch i386 -isysroot $(SIMULATOR_SDK_DIR)
XCODE_BUILD_DIR = $(TARGET_TEMP_DIR)
XCODE_FLAGS = $(ARCH_FLAGS) $(SDK_FLAGS)

translate: pre_translate $(EXTRACTED_JAVA) $(TRANSLATED_OBJC)
	@if [ `cat $(JAVA_SOURCE_LIST) | wc -l` -ge 1 ] ; then \
	  $(J2OBJC) -sourcepath .:$(SOURCE_BASE)/java `cat $(JAVA_SOURCE_LIST)` ; \
	fi

pre_translate:
	@rm -f $(JAVA_SOURCE_LIST)
	@touch $(JAVA_SOURCE_LIST)

lib: translate $(JUNIT_LIB)
	@:

$(EXTRACTED_JAVA): $(JUNIT_SRC_JAR)
	@mkdir -p $(JAVA_SRC_DIR)
	unzip -q -o -d $(JAVA_SRC_DIR) $(JUNIT_SRC_JAR) $(JAVA_SOURCES)
	@touch $(EXTRACTED_JAVA)

ifdef TARGET_TEMP_DIR
$(JUNIT_LIB): $(OBJS:%=$(TARGET_TEMP_DIR)/%)
	@mkdir -p $(@D)
	$(LIBTOOL) -static -o $@ $^
else
$(JUNIT_LIB): $(EMULATION_MACOSX_LIB) $(EMULATION_IPHONE_LIB) \
    $(EMULATION_IPHONEV7S_LIB) $(EMULATION_SIMULATOR_LIB)
	$(LIPO) -create $^ -output $@

$(EMULATION_MACOSX_LIB): $(MACOSX_OBJS)
	echo "building Mac OS X library"
	$(LIBTOOL) -static -o $@ $^

$(EMULATION_IPHONE_LIB): $(IPHONE_OBJS)
	echo "building iPhoneOS library"
	$(LIBTOOL) -static -o $@ $^

$(EMULATION_IPHONEV7S_LIB): $(IPHONEV7S_OBJS)
	echo "building iPhonev7S library"
	$(LIBTOOL) -static -o $@ $^

$(EMULATION_SIMULATOR_LIB): $(SIMULATOR_OBJS)
	echo "building iPhoneSimulator library"
	$(LIBTOOL) -static -o $@ $^
endif

define compile_rules
$(1)/%.o: $(BUILD_DIR)/%.m
	@mkdir -p $$(@D)
	$$(CLANG) -c $$? -o $$@ $(2) $$(OBJCFLAGS) -I$$(BUILD_DIR) \
	  -I$(ARCH_INCLUDE_DIR)

$(1)/%.o: $(OBJC_SOURCE_DIR)/%.m
	@mkdir -p $$(@D)
	$$(CLANG) -c $$? -o $$@ $(2) $$(OBJCFLAGS) -I$$(BUILD_DIR) \
	  -I$$(ARCH_INCLUDE_DIR)
endef

arch_compile_rules = $(eval $(call compile_rules,$(1),$(2)))

$(BUILD_DIR)/%.h $(BUILD_DIR)/%.m: $(JAVA_SRC_DIR)/%.java
	@echo "$?" >> $(JAVA_SOURCE_LIST)

$(BUILD_DIR)/%.h $(BUILD_DIR)/%.m: $(MODIFIED_JAVA_SRC_DIR)/%.java
	@echo "$?" >> $(JAVA_SOURCE_LIST)

ifdef TARGET_TEMP_DIR
$(call arch_compile_rules,$(XCODE_BUILD_DIR),$(XCODE_FLAGS),xcode)
else
$(call arch_compile_rules,$(BUILD_DIR),$(MACOSX_FLAGS),osx)
$(call arch_compile_rules,$(IPHONE_BUILD_DIR),$(IPHONE_FLAGS),iphone)
$(call arch_compile_rules,$(IPHONEV7S_BUILD_DIR),$(IPHONEV7S_FLAGS),iphone7)
$(call arch_compile_rules,$(SIMULATOR_BUILD_DIR),$(SIMULATOR_FLAGS),simulator)
endif

$(JUNIT_LIB_DIST): $(JUNIT_LIB)
	@mkdir -p $(@D)
	install -m 0644 $< $@

$(ARCH_INCLUDE_DIR)/%.h: $(BUILD_DIR)/%.h
	@mkdir -p $(@D)
	install -m 0644 $< $@

$(ARCH_INCLUDE_DIR)/%.h: $(OBJC_SOURCE_DIR)/%.h
	@mkdir -p $(@D)
	install -m 0644 $< $@

clean:
	@rm -rf $(BUILD_DIR) $(JUNIT_LIB_DIST)
	@rm -rf $(ARCH_INCLUDE_DIR)/junit $(ARCH_INCLUDE_DIR)/org/junit

dist: lib $(JUNIT_LIB_DIST) $(ARCH_HEADERS)
