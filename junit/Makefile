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

JUNIT_PUBLIC_SOURCES = \
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
    org/junit/Test.java \
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

JUNIT_INTERNAL_SOURCES = \
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
    org/junit/internal/TextListener.java


JUNIT_SOURCES = $(JUNIT_PUBLIC_SOURCES) $(JUNIT_INTERNAL_SOURCES)

HAMCREST_PUBLIC_SOURCES = \
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
    org/hamcrest/Matcher.java \
    org/hamcrest/SelfDescribing.java \
    org/hamcrest/StringDescription.java

HAMCREST_INTERNAL_SOURCES = \
    org/hamcrest/internal/ArrayIterator.java \
    org/hamcrest/internal/SelfDescribingValue.java \
    org/hamcrest/internal/SelfDescribingValueIterator.java

HAMCREST_SOURCES = $(HAMCREST_PUBLIC_SOURCES) $(HAMCREST_INTERNAL_SOURCES)

# These source files have unique fully-qualified names, but the same class
# name as other classes. This is an issue when building a native library,
# as library entries don't include directories. By renaming the generated
# object files for these sources (adding a OrgJunit prefix), libtool
# warnings and possible link errors (though none reported) are avoided.
DUPLICATE_NAME_SOURCES = \
    org/junit/Test.java \
    org/junit/Assert.java \
    org/junit/ComparisonFailure.java \
    org/junit/internal/runners/InitializationError.java \
    org/junit/internal/runners/model/MultipleFailureException.java \
    org/junit/runner/Description.java
DUPLICATE_NAME_OBJS = \
    $(foreach file,$(DUPLICATE_NAME_SOURCES), $(dir $(file))OrgJunit$(basename $(notdir $(file))).o)

OBJS := $(JUNIT_SOURCES:%.java=%.o) \
    $(HAMCREST_SOURCES:%.java=%.o) \
    $(DUPLICATE_NAME_OBJS)
OBJS := $(filter-out $(DUPLICATE_NAME_SOURCES:%.java=%.o),$(OBJS))

IPHONE_SDK_DIR = $(shell bash ../scripts/sysroot_path.sh --iphoneos)
SIMULATOR_SDK_DIR = $(shell bash ../scripts/sysroot_path.sh --iphonesimulator)

JAVA_SOURCE_LIST = $(BUILD_DIR)/junit.classes.list
EXTRACTED_JAVA = $(JUNIT_SOURCES:%=$(JAVA_SRC_DIR)/%) \
    $(HAMCREST_SOURCES:%=$(JAVA_SRC_DIR)/%)
TRANSLATED_OBJC = $(JUNIT_SOURCES:%.java=$(BUILD_DIR)/%.m) \
    $(HAMCREST_SOURCES:%.java=$(BUILD_DIR)/%.m)
PUBLIC_HEADERS = \
    $(JUNIT_PUBLIC_SOURCES:%.java=%.h) \
    $(HAMCREST_PUBLIC_SOURCES:%.java=%.h)
INTERNAL_HEADERS = \
    $(JUNIT_INTERNAL_SOURCES:%.java=%.h) \
    $(HAMCREST_INTERNAL_SOURCES:%.java=%.h)
ARCH_HEADERS = $(PUBLIC_HEADERS:%=$(ARCH_INCLUDE_DIR)/%)

# Settings for building Mac OS X, iPhone, and iPhone simulator libraries.
EMULATION_MACOSX_LIB = $(BUILD_DIR)/macosx-libjunit.a
EMULATION_IPHONE_LIB = $(BUILD_DIR)/iphone-libjunit.a
EMULATION_IPHONEV7S_LIB = $(BUILD_DIR)/iphonev7s-libjunit.a
EMULATION_SIMULATOR_LIB = $(BUILD_DIR)/simulator-libjunit.a
ARCH_LIBS = $(J2OBJC_ARCHS)
ARCH_LIBS := $(ARCH_LIBS:macosx=$(EMULATION_MACOSX_LIB))
ARCH_LIBS := $(ARCH_LIBS:iphone=$(EMULATION_IPHONE_LIB))
ARCH_LIBS := $(ARCH_LIBS:iphonev7s=$(EMULATION_IPHONEV7s_LIB))
ARCH_LIBS := $(ARCH_LIBS:simulator=$(EMULATION_SIMULATOR_LIB))
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
	  $(J2OBJC) -sourcepath $(JAVA_SRC_DIR) `cat $(JAVA_SOURCE_LIST)`; \
	fi

pre_translate:
	@mkdir -p $(BUILD_DIR)
	@rm -f $(JAVA_SOURCE_LIST)
	@touch $(JAVA_SOURCE_LIST)

$(BUILD_DIR)/.extracted: $(JUNIT_SRC_JAR) $(HAMCREST_SRC_JAR)
	@mkdir -p $(JAVA_SRC_DIR)
	unzip -q -o -d $(JAVA_SRC_DIR) $(JUNIT_SRC_JAR) $(JUNIT_SOURCES)
	unzip -q -o -d $(JAVA_SRC_DIR) $(HAMCREST_SRC_JAR) $(HAMCREST_SOURCES)
	@touch $(EXTRACTED_JAVA)
	@touch $@

$(EXTRACTED_JAVA): $(BUILD_DIR)/.extracted

$(BUILD_DIR)/%.h $(BUILD_DIR)/%.m: $(JAVA_SRC_DIR)/%.java | pre_translate
	@echo "$?" >> $(JAVA_SOURCE_LIST)

ifdef TARGET_TEMP_DIR
$(JUNIT_LIB): $(OBJS:%=$(TARGET_TEMP_DIR)/%)
	@mkdir -p $(@D)
	$(LIBTOOL) -static -o $@ $^
else
$(JUNIT_LIB): $(ARCH_LIBS)
	$(LIPO) -create $^ -output $@

$(EMULATION_MACOSX_LIB): $(MACOSX_OBJS)
	@echo "building junit Mac OS X library"
	@$(LIBTOOL) -static -o $@ $^

$(EMULATION_IPHONE_LIB): $(IPHONE_OBJS)
	@echo "building junit iPhoneOS library"
	@$(LIBTOOL) -static -o $@ $^

$(EMULATION_IPHONEV7S_LIB): $(IPHONEV7S_OBJS)
	@echo "building junit iPhonev7S library"
	@$(LIBTOOL) -static -o $@ $^

$(EMULATION_SIMULATOR_LIB): $(SIMULATOR_OBJS)
	@echo "building junit iPhoneSimulator library"
	@$(LIBTOOL) -static -o $@ $^
endif

define compile_rules
$(1)/%.o: $(BUILD_DIR)/%.m | translate
	@mkdir -p $$(@D)
	$$(CLANG) -c $$? -o $$@ $(2) $$(OBJCFLAGS) -I$$(BUILD_DIR) \
	  -I$(ARCH_INCLUDE_DIR)
endef

define rename_compile_rules
$(1)/$(4): $(BUILD_DIR)/$(3) | translate
	@mkdir -p $$(@D)
	$$(CLANG) -c $$? -o $$@ $(2) $$(OBJCFLAGS) -I$$(BUILD_DIR) \
	  -I$$(ARCH_INCLUDE_DIR)
endef

arch_compile_rules = $(eval $(call compile_rules,$(1),$(2))) \
  $(foreach file,$(DUPLICATE_NAME_SOURCES),$(eval $(call \
  rename_compile_rules,$(1),$(2),$(file:%.java=%.m),$(dir $(file))OrgJunit$(basename $(notdir $(file))).o)))

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

$(ARCH_INCLUDE_DIR)/%.h: $(BUILD_DIR)/%.h | translate
	@mkdir -p $(@D)
	install -m 0644 $< $@

clean:
	@rm -rf $(BUILD_DIR) $(JUNIT_LIB_DIST)
	@rm -rf $(ARCH_INCLUDE_DIR)/junit $(ARCH_INCLUDE_DIR)/org/junit

dist: lib $(JUNIT_LIB_DIST) $(ARCH_HEADERS)
