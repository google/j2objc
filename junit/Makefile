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

JUNIT_SOURCES = \
    junit/extensions/TestDecorator.java \
    junit/framework/Assert.java \
    junit/framework/AssertionFailedError.java \
    junit/framework/ComparisonCompactor.java \
    junit/framework/ComparisonFailure.java \
    junit/framework/JUnit4TestAdapter.java \
    junit/framework/JUnit4TestAdapterCache.java \
    junit/framework/JUnit4TestCaseFacade.java \
    junit/framework/Protectable.java \
    junit/framework/Test.java \
    junit/framework/TestCase.java \
    junit/framework/TestFailure.java \
    junit/framework/TestListener.java \
    junit/framework/TestResult.java \
    junit/framework/TestSuite.java \
    junit/runner/BaseTestRunner.java \
    junit/runner/TestRunListener.java \
    junit/runner/Version.java \
    junit/textui/ResultPrinter.java \
    junit/textui/TestRunner.java \
    org/junit/After.java \
    org/junit/AfterClass.java \
    org/junit/Assert.java \
    org/junit/Assume.java \
    org/junit/Before.java \
    org/junit/BeforeClass.java \
    org/junit/ClassRule.java \
    org/junit/ComparisonFailure.java \
    org/junit/Ignore.java \
    org/junit/Rule.java \
    org/junit/internal/ArrayComparisonFailure.java \
    org/junit/internal/AssumptionViolatedException.java \
    org/junit/internal/builders/AllDefaultPossibilitiesBuilder.java \
    org/junit/internal/builders/AnnotatedBuilder.java \
    org/junit/internal/builders/IgnoredBuilder.java \
    org/junit/internal/builders/IgnoredClassRunner.java \
    org/junit/internal/builders/JUnit3Builder.java \
    org/junit/internal/builders/JUnit4Builder.java \
    org/junit/internal/builders/NullBuilder.java \
    org/junit/internal/builders/SuiteMethodBuilder.java \
    org/junit/internal/ComparisonCriteria.java \
    org/junit/internal/ExactComparisonCriteria.java \
    org/junit/internal/InexactComparisonCriteria.java \
    org/junit/internal/JUnitSystem.java \
    org/junit/internal/matchers/Each.java \
    org/junit/internal/matchers/IsCollectionContaining.java \
    org/junit/internal/matchers/TypeSafeMatcher.java \
    org/junit/internal/RealSystem.java \
    org/junit/internal/requests/ClassRequest.java \
    org/junit/internal/requests/FilterRequest.java \
    org/junit/internal/requests/SortingRequest.java \
    org/junit/internal/runners/ErrorReportingRunner.java \
    org/junit/internal/runners/InitializationError.java \
    org/junit/internal/runners/JUnit38ClassRunner.java \
    org/junit/internal/runners/model/EachTestNotifier.java \
    org/junit/internal/runners/model/MultipleFailureException.java \
    org/junit/internal/runners/model/ReflectiveCallable.java \
    org/junit/internal/runners/rules/RuleFieldValidator.java \
    org/junit/internal/runners/statements/ExpectException.java \
    org/junit/internal/runners/statements/Fail.java \
    org/junit/internal/runners/statements/FailOnTimeout.java \
    org/junit/internal/runners/statements/InvokeMethod.java \
    org/junit/internal/runners/statements/RunAfters.java \
    org/junit/internal/runners/statements/RunBefores.java \
    org/junit/internal/runners/SuiteMethod.java \
    org/junit/internal/TextListener.java \
    org/junit/rules/MethodRule.java \
    org/junit/rules/RunRules.java \
    org/junit/rules/TestRule.java \
    org/junit/runner/Computer.java \
    org/junit/runner/Describable.java \
    org/junit/runner/Description.java \
    org/junit/runner/JUnitCore.java \
    org/junit/runner/Request.java \
    org/junit/runner/Result.java \
    org/junit/runner/Runner.java \
    org/junit/runner/RunWith.java \
    org/junit/runner/manipulation/Filter.java \
    org/junit/runner/manipulation/Filterable.java \
    org/junit/runner/manipulation/NoTestsRemainException.java \
    org/junit/runner/manipulation/Sortable.java \
    org/junit/runner/manipulation/Sorter.java \
    org/junit/runner/notification/Failure.java \
    org/junit/runner/notification/RunListener.java \
    org/junit/runner/notification/RunNotifier.java \
    org/junit/runner/notification/StoppedByUserException.java \
    org/junit/runners/BlockJUnit4ClassRunner.java \
    org/junit/runners/JUnit4.java \
    org/junit/runners/ParentRunner.java \
    org/junit/runners/Suite.java \
    org/junit/runners/model/FrameworkField.java \
    org/junit/runners/model/FrameworkMember.java \
    org/junit/runners/model/FrameworkMethod.java \
    org/junit/runners/model/InitializationError.java \
    org/junit/runners/model/MultipleFailureException.java \
    org/junit/runners/model/NoGenericTypeParametersValidator.java \
    org/junit/runners/model/RunnerBuilder.java \
    org/junit/runners/model/RunnerScheduler.java \
    org/junit/runners/model/Statement.java \
    org/junit/runners/model/TestClass.java


HAMCREST_SOURCES = \
    org/hamcrest/BaseDescription.java \
    org/hamcrest/BaseMatcher.java \
    org/hamcrest/core/AllOf.java \
    org/hamcrest/core/AnyOf.java \
    org/hamcrest/core/DescribedAs.java \
    org/hamcrest/core/Is.java \
    org/hamcrest/core/IsAnything.java \
    org/hamcrest/core/IsEqual.java \
    org/hamcrest/core/IsInstanceOf.java \
    org/hamcrest/core/IsNot.java \
    org/hamcrest/core/IsNull.java \
    org/hamcrest/core/IsSame.java \
    org/hamcrest/CoreMatchers.java \
    org/hamcrest/Description.java \
    org/hamcrest/Factory.java \
    org/hamcrest/internal/ArrayIterator.java \
    org/hamcrest/internal/SelfDescribingValue.java \
    org/hamcrest/internal/SelfDescribingValueIterator.java \
    org/hamcrest/Matcher.java \
    org/hamcrest/SelfDescribing.java \
    org/hamcrest/StringDescription.java

OBJC_SOURCES = \
    junit/JUnitRunner.m

ORG_JUNIT_TEST_SRC = org/junit/Test.java
ORG_JUNIT_TEST_OBJ = org/junit/OrgJunitTest.o

OBJS = $(JUNIT_SOURCES:%.java=%.o) \
    $(HAMCREST_SOURCES:%.java=%.o) \
    $(OBJC_SOURCES:%.m=%.o) \
    $(ORG_JUNIT_TEST_OBJ)

IPHONE_SDK_DIR = $(shell bash ../scripts/sysroot_path.sh --iphoneos)
SIMULATOR_SDK_DIR = $(shell bash ../scripts/sysroot_path.sh --iphonesimulator)

JAVA_SOURCE_LIST = $(TMPDIR).classes.list
EXTRACTED_JAVA = $(JUNIT_SOURCES:%=$(JAVA_SRC_DIR)/%) \
    $(HAMCREST_SOURCES:%=$(JAVA_SRC_DIR)/%) \
    $(JAVA_SRC_DIR)/$(ORG_JUNIT_TEST_SRC)
TRANSLATED_OBJC = $(JUNIT_SOURCES:%.java=$(BUILD_DIR)/%.m) \
    $(HAMCREST_SOURCES:%.java=$(BUILD_DIR)/%.m) \
    $(ORG_JUNIT_TEST_SRC:%.java=$(BUILD_DIR)/%.m)
SOURCE_HEADERS = $(shell find $(OBJC_SOURCE_DIR) -name '*.h')
SOURCE_HEADERS := $(SOURCE_HEADERS:$(OBJC_SOURCE_DIR)/%=%)
GENERATED_HEADERS = $(JUNIT_SOURCES:%.java=%.h)
RELATIVE_HEADERS = $(SOURCE_HEADERS) $(GENERATED_HEADERS) \
    $(ORG_JUNIT_TEST_SRC:%.java=%.h)
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

lib: translate $(JUNIT_LIB)
	@:

translate: pre_translate $(EXTRACTED_JAVA) $(TRANSLATED_OBJC)
	@if [ `cat $(JAVA_SOURCE_LIST) | wc -l` -ge 1 ] ; then \
	  $(J2OBJC) -sourcepath $(SOURCE_BASE)/java:$(JAVA_SRC_DIR) \
	    `cat $(JAVA_SOURCE_LIST)` ; \
	fi

pre_translate:
	@rm -f $(JAVA_SOURCE_LIST)
	@touch $(JAVA_SOURCE_LIST)

$(EXTRACTED_JAVA): $(JUNIT_SRC_JAR) $(HAMCREST_SRC_JAR)
	@mkdir -p $(JAVA_SRC_DIR)
	unzip -q -o -d $(JAVA_SRC_DIR) $(JUNIT_SRC_JAR) $(JUNIT_SOURCES) \
	    $(ORG_JUNIT_TEST_SRC)
	unzip -q -o -d $(JAVA_SRC_DIR) $(HAMCREST_SRC_JAR) $(HAMCREST_SOURCES)
	@touch $(EXTRACTED_JAVA)

$(BUILD_DIR)/%.h $(BUILD_DIR)/%.m: $(JAVA_SRC_DIR)/%.java
	@echo "$?" >> $(JAVA_SOURCE_LIST)

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

$(1)/$(ORG_JUNIT_TEST_OBJ): $(BUILD_DIR)/$(ORG_JUNIT_TEST_SRC:%.java=%.m)
	@mkdir -p $$(@D)
	$$(CLANG) -c $$? -o $$@ $(2) $$(OBJCFLAGS) -I$$(BUILD_DIR) \
	  -I$$(ARCH_INCLUDE_DIR)

endef

arch_compile_rules = $(eval $(call compile_rules,$(1),$(2)))

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
