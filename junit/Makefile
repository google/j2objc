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
    org/junit/internal/runners/model/ReflectiveCallable.java \
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
    org/junit/internal/runners/rules/RuleFieldValidator.java \
    org/junit/internal/runners/statements/ExpectException.java \
    org/junit/internal/runners/statements/Fail.java \
    org/junit/internal/runners/statements/FailOnTimeout.java \
    org/junit/internal/runners/statements/InvokeMethod.java \
    org/junit/internal/runners/statements/RunAfters.java \
    org/junit/internal/runners/statements/RunBefores.java \
    org/junit/internal/runners/SuiteMethod.java \
    org/junit/internal/TextListener.java

MODIFIED_SOURCES = \
    junit/framework/TestCase.java \
    org/junit/runners/model/FrameworkMethod.java


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

JAVA_SOURCE_LIST = $(BUILD_DIR)/junit.classes.list
EXTRACTED_JAVA = $(JUNIT_SOURCES:%=$(JAVA_SRC_DIR)/%) \
    $(HAMCREST_SOURCES:%=$(JAVA_SRC_DIR)/%)
PUBLIC_HEADERS = \
    $(JUNIT_PUBLIC_SOURCES:%.java=%.h) \
    $(HAMCREST_PUBLIC_SOURCES:%.java=%.h) \
    $(MODIFIED_SOURCES:%.java=%.h)
ARCH_HEADERS = $(PUBLIC_HEADERS:%=$(ARCH_INCLUDE_DIR)/%)

JAVA_SOURCES = \
    $(EXTRACTED_JAVA) \
    $(MODIFIED_SOURCES:%=$(MODIFIED_JAVA_SRC_DIR)/%)
JUNIT_DIST_JAR = $(DIST_JAR_DIR)/j2objc_junit.jar
CLASSPATH = $(DIST_JAR_DIR)/j2objc_annotations.jar

JAVA_SOURCES_MANIFEST = $(BUILD_DIR)/java_sources.mf
OBJC_SOURCES_MANIFEST = $(BUILD_DIR)/objc_sources.mf

TRANSLATE_JAVA_FULL = $(JAVA_SOURCES)
TRANSLATE_JAVA_RELATIVE = $(EXTRACTED_JAVA:$(JAVA_SRC_DIR)/%=%) $(MODIFIED_SOURCES)
TRANSLATE_ARGS = -sourcepath $(MODIFIED_JAVA_SRC_DIR):$(JAVA_SRC_DIR) \
  --batch-translate-max=300 -J-Xmx2G
include ../make/translate.mk

FAT_LIB_NAME = junit
FAT_LIB_SOURCES_FULL = $(TRANSLATE_SOURCES)
FAT_LIB_SOURCES_RELATIVE = $(TRANSLATE_SOURCES:$(GEN_OBJC_DIR)/%=%)
FAT_LIB_SOURCE_DIRS = $(GEN_OBJC_DIR)
FAT_LIB_COMPILE = $(CLANG) $(OBJCFLAGS) -I$(GEN_OBJC_DIR) -I$(ARCH_INCLUDE_DIR)
include ../make/fat_lib.mk

fat_lib_dependencies: jre_emul_dist

CREATE_JAR_NAME = j2objc_junit
CREATE_JAR_SOURCES = $(JAVA_SOURCES)
CREATE_JAR_JAVAC_ARGS = -cp $(CLASSPATH)
CREATE_JAR_DEPENDENCIES = annotations_dist
include ../make/create_jar.mk

java: $(JUNIT_DIST_JAR)
	@:

$(JUNIT_DIST_JAR): $(CREATE_JAR_RESULT)
	@mkdir -p $(@D)
	@install -m 0644 $< $@

lib: $(FAT_LIB_LIBRARY)
	@:

$(JUNIT_SRC_JAR) $(HAMCREST_SRC_JAR): | java_deps_dist

$(BUILD_DIR)/.extracted: $(JUNIT_SRC_JAR) $(HAMCREST_SRC_JAR)
	@mkdir -p $(JAVA_SRC_DIR)
	@unzip -q -o -d $(JAVA_SRC_DIR) $(JUNIT_SRC_JAR) $(JUNIT_SOURCES)
	@unzip -q -o -d $(JAVA_SRC_DIR) $(HAMCREST_SRC_JAR) $(HAMCREST_SOURCES)
	@touch $(EXTRACTED_JAVA)
	@touch $@

$(EXTRACTED_JAVA): $(BUILD_DIR)/.extracted

$(JAVA_SOURCES_MANIFEST): $(JAVA_SOURCES) | $(BUILD_DIR)
	@echo "Building $$(basename $@)"
	@if [ -e $@ ]; then rm $@; fi
	@files='$^' && for i in $$files; do echo $$i >> $@; done

java_sources_manifest: $(JAVA_SOURCES_MANIFEST)
	@:

$(OBJC_SOURCES_MANIFEST): | $(BUILD_DIR)
	@echo "Building $$(basename $@)"
	@if [ -e $@ ]; then rm $@; fi
	@files='$(TRANSLATE_SOURCES)' && for i in $$files; do \
	  echo $${i%.m}.h >> $@; \
	  echo $$i >> $@; \
	done

objc_sources_manifest: $(OBJC_SOURCES_MANIFEST)
	@:

$(JUNIT_LIB_DIST): $(FAT_LIB_LIBRARY)
	@mkdir -p $(@D)
	install -m 0644 $< $@

$(ARCH_INCLUDE_DIR)/%.h: $(GEN_OBJC_DIR)/%.h
	@mkdir -p $(@D)
	install -m 0644 $< $@

$(BUILD_DIR):
	@mkdir -p $@

clean:
	@rm -rf $(BUILD_DIR) $(JUNIT_LIB_DIST) $(JUNIT_DIST_JAR)
	@rm -rf $(ARCH_INCLUDE_DIR)/junit $(ARCH_INCLUDE_DIR)/org/junit

dist: lib $(JUNIT_LIB_DIST) $(ARCH_HEADERS) $(JUNIT_DIST_JAR)
