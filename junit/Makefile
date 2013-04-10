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
    junit/framework/TestSuite.java


OBJC_SOURCES = \
    JUnitRunner.m

OBJS = $(JAVA_SOURCES:%.java=$(ARCH_BUILD_DIR)/%.o) $(OBJC_SOURCES:%.m=$(ARCH_BUILD_DIR)/%.o)

JAVA_SOURCE_LIST = $(TMPDIR).classes.list
EXTRACTED_JAVA = $(JAVA_SOURCES:%=$(JAVA_SRC_DIR)/%)
TRANSLATED_OBJC = $(JAVA_SOURCES:%.java=$(BUILD_DIR)/%.m)
SOURCE_HEADERS = $(shell find $(OBJC_SOURCE_DIR) -name '*.h')
SOURCE_HEADERS := $(SOURCE_HEADERS:$(OBJC_SOURCE_DIR)/%=%)
GENERATED_HEADERS = $(JAVA_SOURCES:%.java=%.h)
RELATIVE_HEADERS = $(SOURCE_HEADERS) $(GENERATED_HEADERS)
HEADERS = $(SOURCE_HEADERS:%=$(OBJC_SOURCE_DIR)/%) $(GENERATED_HEADERS:%=$(BUILD_DIR)/%)
DIST_HEADERS = $(RELATIVE_HEADERS:%=$(DIST_INCLUDE_DIR)/%)

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

$(JUNIT_LIB): $(OBJS)
	$(LIBTOOL) -static -o $@ $^

$(BUILD_DIR)/%.h $(BUILD_DIR)/%.m: $(JAVA_SRC_DIR)/%.java
	@echo "$?" >> $(JAVA_SOURCE_LIST)

$(ARCH_BUILD_DIR)/%.o: $(BUILD_DIR)/%.m
	@mkdir -p $(@D)
	$(J2OBJCC) -o $@ $?

$(ARCH_BUILD_DIR)/%.o: $(OBJC_SOURCE_DIR)/%.m
	@mkdir -p $(@D)
	$(J2OBJCC) -o $@ $?

$(JUNIT_LIB_DIST): $(JUNIT_LIB)
	@mkdir -p $(@D)
	install -m 0644 $< $@

$(DIST_INCLUDE_DIR)/%.h: $(BUILD_DIR)/%.h
	@mkdir -p $(@D)
	install -m 0644 $< $@

$(DIST_INCLUDE_DIR)/%.h: $(OBJC_SOURCE_DIR)/%.h
	@mkdir -p $(@D)
	install -m 0644 $< $@

clean:
	@rm -rf $(BUILD_DIR) $(JUNIT_LIB_DIST) $(DIST_HEADERS)

dist: lib $(JUNIT_LIB_DIST) $(DIST_HEADERS)
