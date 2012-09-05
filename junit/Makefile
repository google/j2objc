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

FRAMEWORK_OBJS = \
	$(BUILD_DIR)/junit/framework/Assert.o \
	$(BUILD_DIR)/junit/framework/AssertionFailedError.o \
	$(BUILD_DIR)/junit/framework/ComparisonCompactor.o \
	$(BUILD_DIR)/junit/framework/ComparisonFailure.o \
	$(BUILD_DIR)/junit/framework/Protectable.o \
	$(BUILD_DIR)/junit/framework/Test.o \
	$(BUILD_DIR)/junit/framework/TestCase.o \
	$(BUILD_DIR)/junit/framework/TestFailure.o \
	$(BUILD_DIR)/junit/framework/TestListener.o \
	$(BUILD_DIR)/junit/framework/TestResult.o \
	$(BUILD_DIR)/junit/framework/TestSuite.o


TEST_RUNNER_OBJS = \
	$(BUILD_DIR)/JUnitRunner.o

OBJS = $(FRAMEWORK_OBJS) $(INTERNAL_OBJS) $(TEST_RUNNER_OBJS)

JAVA_SOURCES = \
  $(addprefix junit/framework/,$(notdir $(FRAMEWORK_OBJS:.o=.java))) \
  $(addprefix org/junit/internal/,$(notdir $(INTERNAL_OBJS:.o=.java)))
JAVA_SOURCE_LIST = $(TMPDIR).classes.list

all: lib includes
	@: # suppress make's "nothing to be done" message

translate: pre_translate $(JAVA_SOURCES) $(FRAMEWORK_OBJS:.o=.m) $(INTERNAL_OBJS:.o=.m)
	@if [ `cat $(JAVA_SOURCE_LIST) | wc -l` -ge 1 ] ; then \
	  $(J2OBJC) -sourcepath .:$(SOURCE_BASE)/java `cat $(JAVA_SOURCE_LIST)` ; \
	fi

pre_translate:
	@rm -f $(JAVA_SOURCE_LIST)
	@touch $(JAVA_SOURCE_LIST)

lib: translate $(JUNIT_LIB)
	@:

$(JAVA_SOURCES): $(JUNIT_SRC_JAR)
	@mkdir -p $(JAVA_SRC_DIR)
	@unzip -q -o -d $(JAVA_SRC_DIR) $(JUNIT_SRC_JAR) $(JAVA_SOURCES)

$(JUNIT_LIB): $(OBJS)
	libtool -static -o $(JUNIT_LIB) `find $(BUILD_DIR) -name '*.o'`

# Copy top-level class headers to include directory.
includes:
	@mkdir -p $(INCLUDE_DIR)/junit/framework
	@install $(OBJC_SOURCE)/*.h $(INCLUDE_DIR)
	@install $(BUILD_DIR)/junit/framework/*.h $(INCLUDE_DIR)/junit/framework
	@find $(INCLUDE_DIR) -type f | xargs chmod 444

$(BUILD_DIR)/%.h $(BUILD_DIR)/%.m: $(JAVA_SRC_DIR)/%.java
	@echo "$?" >> $(JAVA_SOURCE_LIST)

$(BUILD_DIR)/%.o: $(BUILD_DIR)/%.m
	$(J2OBJCC) -o $@ $?

$(BUILD_DIR)/%.o: $(OBJC_SOURCE)/%.m
	$(J2OBJCC) -o $@ $?

clean:
	@rm -rf $(BUILD_DIR)
	@rm -rf $(DIST_LIB_DIR)/$(JUNIT_LIB) $(DIST_INCLUDE_DIR)/junit
	@rm -f $(JAVA_SOURCE_LIST)

dist: lib includes
	@mkdir -p $(DIST_DIR) $(DIST_LIB_DIR)
	cp -r $(INCLUDE_DIR)/* $(DIST_INCLUDE_DIR)
	@find $(DIST_INCLUDE_DIR) -type f | xargs chmod 0644
	install -m 0644 $(JUNIT_LIB) $(DIST_LIB_DIR)
