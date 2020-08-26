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

default: test

include environment.mk
include test_sources.mk
include $(J2OBJC_ROOT)/make/translate_macros.mk

ALL_TEST_SOURCES = $(TEST_SOURCES) $(JSON_TEST_SOURCES) $(ARC_TEST_SOURCES) \
    $(COPIED_ARC_TEST_SOURCES)
ALL_SUITE_SOURCES = $(SUITE_SOURCES) $(JSON_SUITE_SOURCES)

ifdef J2OBJC_JRE_STRIP_REFLECTION
TESTS_TO_SKIP += $(TESTS_USE_SERIALIZATION)
TESTS_TO_SKIP += $(TESTS_USE_REFLECTION)
endif
TESTS_TO_RUN = $(filter-out $(TESTS_TO_SKIP),$(ALL_TEST_SOURCES))
TESTS_TO_RUN := $(subst /,.,$(TESTS_TO_RUN:%.java=%))

ALL_TESTS_CLASS = AllJreTests
# Creates a test suit that includes all classes in ALL_TEST_SOURCES.
ALL_TESTS_SOURCE = $(RELATIVE_TESTS_DIR)/AllJreTests.java

SUPPORT_OBJS = $(SUPPORT_SOURCES:%.java=$(TESTS_DIR)/%.o) $(NATIVE_SOURCES:%.cpp=$(TESTS_DIR)/%.o)
MOCKWEBSERVER_OBJS = \
    $(MOCKWEBSERVER_SOURCES:%.java=$(TESTS_DIR)/%.o) $(NATIVE_SOURCES:%.cpp=$(TESTS_DIR)/%.o)
TEST_OBJS = \
    $(ALL_TEST_SOURCES:%.java=$(TESTS_DIR)/%.o) \
    $(ALL_SUITE_SOURCES:%.java=$(TESTS_DIR)/%.o) \
    $(TESTS_DIR)/$(ALL_TESTS_CLASS).o

TEST_RESOURCES = $(TEST_RESOURCES_RELATIVE:%=$(RESOURCES_DEST_DIR)/%)

JUNIT_DIST_JAR = $(DIST_JAR_DIR)/$(JUNIT_JAR)
JUNIT_DATAPROVIDER_DIST_JAR = $(DIST_JAR_DIR)/$(JUNIT_DATAPROVIDER_JAR)

INCLUDE_DIRS = $(TESTS_DIR) $(TESTS_DIR)/arc $(CLASS_DIR) $(EMULATION_CLASS_DIR)
INCLUDE_ARGS = $(INCLUDE_DIRS:%=-I%)

ifdef DEVELOPER_DIR
J2OBJCC := DEVELOPER_DIR=$(DEVELOPER_DIR) ../dist/j2objcc
else
J2OBJCC := ../dist/j2objcc
endif

SDK_PATH = $(shell xcrun --show-sdk-path)
TEST_JOCC := $(J2OBJCC) -g $(WARNINGS) -isysroot $(SDK_PATH)
LINK_FLAGS := -ljre_emul -ljson -ljunit -L$(TESTS_DIR) -l test-support -fsanitize=address
COMPILE_FLAGS := $(INCLUDE_ARGS) -c -Wno-objc-redundant-literal-use -Wno-format -Werror \
  -Wno-parentheses

ifeq ($(OBJCPP_BUILD), YES)
LINK_FLAGS += -lc++ -ObjC++
else
LINK_FLAGS += -ObjC
endif

SUPPORT_LIB = $(TESTS_DIR)/libtest-support.a
TEST_BIN = $(TESTS_DIR)/jre_unit_tests

TRANSLATE_ARGS = -classpath $(JUNIT_DIST_JAR):$(JUNIT_DATAPROVIDER_DIST_JAR):$(JSON_JAR) \
    -Werror -sourcepath $(TEST_SRC):$(GEN_JAVA_DIR) \
    -encoding UTF-8 --prefixes $(MISC_TEST_ROOT)/resources/prefixes.properties
ifndef JAVA_8
TRANSLATE_ARGS += --patch-module java.base=$(TEST_SRC):$(GEN_JAVA_DIR)
endif
ifdef J2OBJC_JRE_STRIP_REFLECTION
TRANSLATE_ARGS += -external-annotation-file lite/ignore_tests.jaif
endif
TRANSLATE_SOURCES = \
    $(SUPPORT_SOURCES) \
    $(MOCKWEBSERVER_SOURCES) \
    $(TEST_SOURCES) \
    $(JSON_TEST_SOURCES) \
    $(SUITE_SOURCES) \
    $(JSON_SUITE_SOURCES) \
    $(ALL_TESTS_CLASS).java
TRANSLATE_SOURCES_ARC = $(ARC_TEST_SOURCES) $(COPIED_ARC_TEST_SOURCES)
TRANSLATED_OBJC = $(TRANSLATE_SOURCES:%.java=$(TESTS_DIR)/%.m)
TRANSLATED_OBJC_ARC = $(TRANSLATE_SOURCES_ARC:%.java=$(TESTS_DIR)/arc/%.m)

TRANSLATE_ARTIFACT := $(call emit_translate_rule,\
  jre_emul_tests,\
  $(TESTS_DIR),\
  $(SUPPORT_SOURCES) $(MOCKWEBSERVER_SOURCES) $(TEST_SOURCES) $(JSON_TEST_SOURCES) \
  $(SUITE_SOURCES) $(JSON_SUITE_SOURCES) $(ALL_TESTS_SOURCE),\
  ,\
  $(TRANSLATE_ARGS))

TRANSLATE_ARTIFACT_ARC := $(call emit_translate_rule,\
  jre_emul_tests_arc,\
  $(TESTS_DIR)/arc,\
  $(ARC_TEST_SOURCES) $(COPIED_ARC_TEST_SOURCES:%=$(GEN_JAVA_DIR)/%),\
  ,\
  $(TRANSLATE_ARGS) -use-arc)

TRANSLATE_ARTIFACTS = $(TRANSLATE_ARTIFACT) $(TRANSLATE_ARTIFACT_ARC)

# Make sure any generated source files are generated prior to translation.
translate_dependencies: $(COPIED_ARC_TEST_SOURCES:%=$(GEN_JAVA_DIR)/%)

$(TRANSLATED_OBJC): $(TRANSLATE_ARTIFACT)
	@:

$(TRANSLATED_OBJC_ARC): $(TRANSLATE_ARTIFACT_ARC)
	@:

DIST_JRE_EMUL_LIB = $(DIST_LIB_MACOSX_DIR)/libjre_emul.a
$(DIST_JRE_EMUL_LIB): jre_emul_dist
	@:

DIST_JSON_LIB = $(DIST_LIB_MACOSX_DIR)/libjson.a
$(DIST_JSON_LIB): jre_emul_dist
	@:

DIST_JUNIT_LIB = $(DIST_LIB_MACOSX_DIR)/libjunit.a
$(DIST_JUNIT_LIB): junit_dist
	@:

ifdef GENERATE_TEST_COVERAGE
COVERAGE_FLAGS = -ftest-coverage -fprofile-arcs
TEST_JOCC += $(COVERAGE_FLAGS)
LINK_FLAGS += $(COVERAGE_FLAGS)
RUN_FLAGS := ASAN_OPTIONS=coverage=1
endif

all-tests: test run-xctests

test: print-tools-env run-tests

print-tools-env:
	@echo clang path: `xcrun --find clang`
	@echo SDK path: `xcrun --show-sdk-path`

support-lib: $(SUPPORT_LIB)

build: support-lib $(TEST_OBJS)
	@:

translate-all: translate
	@:

link: build $(TEST_BIN)

resources: $(TEST_RESOURCES)
	@:

define resource_copy_rule
$(RESOURCES_DEST_DIR)/%: $(1)/%
	@mkdir -p $$(@D)
	@install -m 0644 $$< $$@
endef

$(foreach root,$(TEST_RESOURCE_ROOTS),$(eval $(call resource_copy_rule,$(root))))

# A bug in make 3.81 causes subprocesses to inherit a generous amount of stack.
# This distorts the fact that the default stack size is 8 MB for a 64-bit OS X
# binary. Work around with ulimit override.
#
# See http://stackoverflow.com/questions/16279867/gmake-change-the-stack-size-limit
# and https://savannah.gnu.org/bugs/?22010
run-tests: link resources $(TEST_BIN) run-initialization-test run-core-size-test
	@ulimit -s 8192 && $(RUN_FLAGS) $(TEST_BIN) org.junit.runner.JUnitCore $(ALL_TESTS_CLASS)

# Useful when investigating flaky tests. Example:
# make -f tests.mk run-single-test NUM_TEST_RUNS=50 TEST_TO_RUN=jsr166.CompletableFutureTest
run-single-test: link resources $(TEST_BIN)
	@test=0 ; while [[ $$test -lt $(NUM_TEST_RUNS) ]] ; do \
	  echo test $$test ; \
	  ulimit -s 8192 && $(TEST_BIN) org.junit.runner.JUnitCore $(TEST_TO_RUN) || exit 1; \
	  ((test = test + 1)) ; \
	done

run-initialization-test: resources $(TESTS_DIR)/jreinitialization
	@$(TESTS_DIR)/jreinitialization 2>&1 | grep -v "support not implemented"

run-core-size-test: $(TESTS_DIR)/core_size \
  $(TESTS_DIR)/full_jre_size \
  $(TESTS_DIR)/core_plus_android_util \
  $(TESTS_DIR)/core_plus_beans \
  $(TESTS_DIR)/core_plus_channels \
  $(TESTS_DIR)/core_plus_file \
  $(TESTS_DIR)/core_plus_concurrent \
  $(TESTS_DIR)/core_plus_io \
  $(TESTS_DIR)/core_plus_icu \
  $(TESTS_DIR)/core_plus_json \
  $(TESTS_DIR)/core_plus_net \
  $(TESTS_DIR)/core_plus_security \
  $(TESTS_DIR)/core_plus_sql \
  $(TESTS_DIR)/core_plus_ssl \
  $(TESTS_DIR)/core_plus_time \
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
	@$(TEST_BIN) org.junit.runner.JUnitCore jsr166.ConcurrencyTests

run-crypto-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore com.google.j2objc.crypto.CryptoTests

run-icu-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore android.icu.dev.test.Tests

run-io-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore libcore.java.io.SmallTests

run-ios-security-provider-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore com.google.j2objc.security.IosSecurityProviderTests

run-json-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore libcore.org.json.SmallTests

run-java8-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore com.google.j2objc.java8.SmallTests

run-logging-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore \
	    org.apache.harmony.logging.tests.java.util.logging.AllTests

run-net-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore libcore.java.net.SmallTests

run-nio-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore com.google.j2objc.nio.NioTests

run-security-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore com.google.j2objc.security.SecurityTests

run-text-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore libcore.java.text.SmallTests libcore.java.text.LargeTests

run-time-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore test.java.time.Tests

run-zip-tests: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore libcore.java.util.zip.SmallTests

run-zip-tests-large: link resources $(TEST_BIN)
	@$(TEST_BIN) org.junit.runner.JUnitCore libcore.java.util.zip.LargeTests

# Run this when the above has errors and JUnit doesn't report which
# test failed or hung.
run-each-test: link resources $(TEST_BIN)
	@for test in $(TESTS_TO_RUN); do \
	  echo $$test:; \
	  $(TEST_BIN) org.junit.runner.JUnitCore $$test; \
	done

# Build and run the JreEmulation project's test bundle, then close simulator app.
# Note: the simulator app's name was changed to "Simulator" in Xcode 7.
run-xctests: test
	@xcrun xcodebuild -project JreEmulation.xcodeproj -scheme jre_emul -destination \
	    'platform=iOS Simulator,name=iPhone 7 Plus' test
	@killall 'Simulator'

$(SUPPORT_LIB): $(SUPPORT_OBJS) $(MOCKWEBSERVER_OBJS)
	@echo libtool -o $(SUPPORT_LIB)
	@xcrun libtool -static -o $(SUPPORT_LIB) $(SUPPORT_OBJS) $(MOCKWEBSERVER_OBJS)

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
	@$(TEST_JOCC) $(COMPILE_FLAGS) -fobjc-arc -fobjc-arc-exceptions -o $@ $<

$(TESTS_DIR)/%.o: $(ANDROID_NATIVE_TEST_DIR)/%.cpp | $(TESTS_DIR)
	xcrun cc -g -I$(EMULATION_CLASS_DIR) -x objective-c++ -c $? -o $@ \
	  -Werror -Wno-parentheses $(GCOV_FLAGS)

$(TEST_BIN): $(TEST_OBJS) $(SUPPORT_LIB) $(DIST_JRE_EMUL_LIB) $(DIST_JSON_LIB) $(DIST_JUNIT_LIB)
	@echo Building test executable...
	@echo "  " $(TEST_JOCC) $(LINK_FLAGS) ...
	@$(TEST_JOCC) $(LINK_FLAGS) -o $@ $(TEST_OBJS)

$(ALL_TESTS_SOURCE): tests.mk test_sources.mk
	@mkdir -p $(@D)
	@xcrun awk -f gen_all_tests.sh $(TESTS_TO_RUN) > $@

$(TESTS_DIR)/jreinitialization: $(MISC_TEST_ROOT)/JreInitialization.m $(DIST_JRE_EMUL_LIB)
	@echo Verifying JRE initialization
	@$(J2OBJCC) -o $@ -ljre_emul -ObjC $(COVERAGE_FLAGS) -Os $(MISC_TEST_ROOT)/JreInitialization.m

$(GEN_JAVA_DIR)/com/google/j2objc/arc/%.java: $(MISC_TEST_ROOT)/com/google/j2objc/%.java
	@mkdir -p $(@D)
	@echo $<
	@sed 's/^package com\.google\.j2objc;$$/package com.google.j2objc.arc;/' $< > $@

$(TESTS_DIR)/core_size:
	@mkdir -p $(@D)
	$(J2OBJCC) -o $@ -ObjC $(COVERAGE_FLAGS)

$(TESTS_DIR)/full_jre_size:
	@mkdir -p $(@D)
	$(J2OBJCC) -ljre_emul -o $@ -ObjC $(COVERAGE_FLAGS)

$(TESTS_DIR)/core_plus_io:
	@mkdir -p $(@D)
	$(J2OBJCC) -ljre_io -o $@ -ObjC $(COVERAGE_FLAGS)

$(TESTS_DIR)/core_plus_icu:
	@mkdir -p $(@D)
	$(J2OBJCC) -ljre_icu -ljre_channels -ljre_net -ljre_util -ljre_security \
	    -ljre_zip -ljre_io -ljre_concurrent -o $@ -ObjC $(COVERAGE_FLAGS)

$(TESTS_DIR)/core_plus_json:
	@mkdir -p $(@D)
	$(J2OBJCC) -ljson -o $@ -ObjC $(COVERAGE_FLAGS)

$(TESTS_DIR)/core_plus_net:
	@mkdir -p $(@D)
	$(J2OBJCC) -ljre_net -o $@ -ObjC $(COVERAGE_FLAGS)

$(TESTS_DIR)/core_plus_util:
	@mkdir -p $(@D)
	$(J2OBJCC) -ljre_util -o $@ -ObjC $(COVERAGE_FLAGS)

$(TESTS_DIR)/core_plus_concurrent:
	@mkdir -p $(@D)
	$(J2OBJCC) -ljre_concurrent -ljre_util -o $@ -ObjC $(COVERAGE_FLAGS)

$(TESTS_DIR)/core_plus_file:
	@mkdir -p $(@D)
	$(J2OBJCC) -ljre_file -ljre_channels -ljre_concurrent -ljre_net \
	    -ljre_security -ljre_util -o $@ -ObjC $(COVERAGE_FLAGS)

$(TESTS_DIR)/core_plus_channels:
	@mkdir -p $(@D)
	$(J2OBJCC) -ljre_channels -ljre_net -ljre_security -ljre_util -o $@ -ObjC $(COVERAGE_FLAGS)

$(TESTS_DIR)/core_plus_security:
	@mkdir -p $(@D)
	$(J2OBJCC) -ljre_security -ljre_net -o $@ -ObjC $(COVERAGE_FLAGS)

$(TESTS_DIR)/core_plus_ssl:
	@mkdir -p $(@D)
	$(J2OBJCC) -ljre_ssl -ljre_security -ljre_net -ljre_util -o $@ -ObjC $(COVERAGE_FLAGS)

$(TESTS_DIR)/core_plus_time:
	@mkdir -p $(@D)
	$(J2OBJCC) -ljre_time -ljre_icu -ljre_channels -ljre_net -ljre_util \
	    -ljre_security -ljre_zip -ljre_io -ljre_concurrent -o $@ -ObjC $(COVERAGE_FLAGS)

$(TESTS_DIR)/core_plus_xml:
	@mkdir -p $(@D)
	$(J2OBJCC) -ljre_xml -ljre_net -ljre_security -o $@ -ObjC $(COVERAGE_FLAGS)

$(TESTS_DIR)/core_plus_zip:
	@mkdir -p $(@D)
	$(J2OBJCC) -ljre_zip -ljre_security -ljre_net -ljre_io -o $@ -ObjC $(COVERAGE_FLAGS)

$(TESTS_DIR)/core_plus_sql:
	@mkdir -p $(@D)
	$(J2OBJCC) -ljre_sql -o $@ -ObjC $(COVERAGE_FLAGS)

$(TESTS_DIR)/core_plus_beans:
	@mkdir -p $(@D)
	$(J2OBJCC) -ljre_beans -ljre_util -o $@ -ObjC $(COVERAGE_FLAGS)

$(TESTS_DIR)/core_plus_android_util:
	@mkdir -p $(@D)
	$(J2OBJCC) -landroid_util -ljre_net -ljre_util -ljre_concurrent -ljre_security \
	    -o $@ -ObjC $(COVERAGE_FLAGS)
