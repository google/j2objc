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

.SUFFIXES:
.PHONY: default j2objc clean

include environment.mk

JUNIT_SOURCES = \
    junit/extensions/ActiveTestSuite.java \
    junit/extensions/RepeatedTest.java \
    junit/extensions/TestDecorator.java \
    junit/extensions/TestSetup.java \
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
    org/junit/FixMethodOrder.java \
    org/junit/Ignore.java \
    org/junit/Rule.java \
    org/junit/Test.java \
    org/junit/experimental/ParallelComputer.java \
    org/junit/experimental/categories/Categories.java \
    org/junit/experimental/categories/Category.java \
    org/junit/experimental/max/CouldNotReadCoreException.java \
    org/junit/experimental/max/MaxCore.java \
    org/junit/experimental/max/MaxHistory.java \
    org/junit/experimental/results/FailureList.java \
    org/junit/experimental/results/PrintableResult.java \
    org/junit/experimental/results/ResultMatchers.java \
    org/junit/experimental/runners/Enclosed.java \
    org/junit/experimental/theories/DataPoint.java \
    org/junit/experimental/theories/DataPoints.java \
    org/junit/experimental/theories/ParameterSignature.java \
    org/junit/experimental/theories/ParametersSuppliedBy.java \
    org/junit/experimental/theories/ParameterSupplier.java \
    org/junit/experimental/theories/PotentialAssignment.java \
    org/junit/experimental/theories/Theories.java \
    org/junit/experimental/theories/Theory.java \
    org/junit/experimental/theories/internal/AllMembersSupplier.java \
    org/junit/experimental/theories/internal/Assignments.java \
    org/junit/experimental/theories/internal/ParameterizedAssertionError.java \
    org/junit/experimental/theories/suppliers/TestedOn.java \
    org/junit/experimental/theories/suppliers/TestedOnSupplier.java \
    org/junit/internal/ArrayComparisonFailure.java \
    org/junit/internal/AssumptionViolatedException.java \
    org/junit/internal/ComparisonCriteria.java \
    org/junit/internal/ExactComparisonCriteria.java \
    org/junit/internal/InexactComparisonCriteria.java \
    org/junit/internal/JUnitSystem.java \
    org/junit/internal/MethodSorter.java \
    org/junit/internal/RealSystem.java \
    org/junit/internal/TextListener.java \
    org/junit/internal/builders/AllDefaultPossibilitiesBuilder.java \
    org/junit/internal/builders/AnnotatedBuilder.java \
    org/junit/internal/builders/IgnoredBuilder.java \
    org/junit/internal/builders/IgnoredClassRunner.java \
    org/junit/internal/builders/JUnit3Builder.java \
    org/junit/internal/builders/JUnit4Builder.java \
    org/junit/internal/builders/NullBuilder.java \
    org/junit/internal/builders/SuiteMethodBuilder.java \
    org/junit/internal/matchers/StacktracePrintingMatcher.java \
    org/junit/internal/matchers/ThrowableCauseMatcher.java \
    org/junit/internal/matchers/ThrowableMessageMatcher.java \
    org/junit/internal/matchers/TypeSafeMatcher.java \
    org/junit/internal/requests/ClassRequest.java \
    org/junit/internal/requests/FilterRequest.java \
    org/junit/internal/requests/SortingRequest.java \
    org/junit/internal/runners/ClassRoadie.java \
    org/junit/internal/runners/ErrorReportingRunner.java \
    org/junit/internal/runners/FailedBefore.java \
    org/junit/internal/runners/InitializationError.java \
    org/junit/internal/runners/JUnit38ClassRunner.java \
    org/junit/internal/runners/JUnit4ClassRunner.java \
    org/junit/internal/runners/MethodRoadie.java \
    org/junit/internal/runners/MethodValidator.java \
    org/junit/internal/runners/SuiteMethod.java \
    org/junit/internal/runners/TestClass.java \
    org/junit/internal/runners/TestMethod.java \
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
    org/junit/matchers/JUnitMatchers.java \
    org/junit/rules/ErrorCollector.java \
    org/junit/rules/ExpectedException.java \
    org/junit/rules/ExpectedExceptionMatcherBuilder.java \
    org/junit/rules/ExternalResource.java \
    org/junit/rules/MethodRule.java \
    org/junit/rules/RuleChain.java \
    org/junit/rules/RunRules.java \
    org/junit/rules/TemporaryFolder.java \
    org/junit/rules/TestName.java \
    org/junit/rules/TestRule.java \
    org/junit/rules/TestWatcher.java \
    org/junit/rules/TestWatchman.java \
    org/junit/rules/Timeout.java \
    org/junit/rules/Verifier.java \
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
    org/junit/runners/AllTests.java \
    org/junit/runners/BlockJUnit4ClassRunner.java \
    org/junit/runners/JUnit4.java \
    org/junit/runners/MethodSorters.java \
    org/junit/runners/Parameterized.java \
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
    org/hamcrest/Condition.java \
    org/hamcrest/CoreMatchers.java \
    org/hamcrest/CustomMatcher.java \
    org/hamcrest/CustomTypeSafeMatcher.java \
    org/hamcrest/Description.java \
    org/hamcrest/DiagnosingMatcher.java \
    org/hamcrest/Factory.java \
    org/hamcrest/FeatureMatcher.java \
    org/hamcrest/Matcher.java \
    org/hamcrest/MatcherAssert.java \
    org/hamcrest/SelfDescribing.java \
    org/hamcrest/StringDescription.java \
    org/hamcrest/TypeSafeDiagnosingMatcher.java \
    org/hamcrest/TypeSafeMatcher.java \
    org/hamcrest/core/AllOf.java \
    org/hamcrest/core/AnyOf.java \
    org/hamcrest/core/CombinableMatcher.java \
    org/hamcrest/core/DescribedAs.java \
    org/hamcrest/core/Every.java \
    org/hamcrest/core/Is.java \
    org/hamcrest/core/IsAnything.java \
    org/hamcrest/core/IsCollectionContaining.java \
    org/hamcrest/core/IsEqual.java \
    org/hamcrest/core/IsInstanceOf.java \
    org/hamcrest/core/IsNot.java \
    org/hamcrest/core/IsNull.java \
    org/hamcrest/core/IsSame.java \
    org/hamcrest/core/ShortcutCombination.java \
    org/hamcrest/core/StringContains.java \
    org/hamcrest/core/StringEndsWith.java \
    org/hamcrest/core/StringStartsWith.java \
    org/hamcrest/core/SubstringMatcher.java \
    org/hamcrest/internal/ArrayIterator.java \
    org/hamcrest/internal/ReflectiveTypeFinder.java \
    org/hamcrest/internal/SelfDescribingValue.java \
    org/hamcrest/internal/SelfDescribingValueIterator.java

JAVA_SOURCE_LIST = $(BUILD_DIR)/junit.classes.list
EXTRACTED_JAVA = $(JUNIT_SOURCES:%=$(JAVA_SRC_DIR)/%) \
    $(HAMCREST_SOURCES:%=$(JAVA_SRC_DIR)/%)
PUBLIC_HEADERS = \
    $(JUNIT_SOURCES:%.java=%.h) \
    $(HAMCREST_SOURCES:%.java=%.h)
ARCH_HEADERS = $(PUBLIC_HEADERS:%=$(ARCH_INCLUDE_DIR)/%)

JAVA_SOURCES = $(EXTRACTED_JAVA)
JUNIT_DIST_JAR = $(DIST_JAR_DIR)/$(JUNIT_JAR)
CLASSPATH = $(DIST_JAR_DIR)/j2objc_annotations.jar

JAVA_SOURCES_MANIFEST = $(BUILD_DIR)/java_sources.mf
OBJC_SOURCES_MANIFEST = $(BUILD_DIR)/objc_sources.mf

TRANSLATE_JAVA_FULL = $(JAVA_SOURCES)
TRANSLATE_JAVA_RELATIVE = $(EXTRACTED_JAVA:$(JAVA_SRC_DIR)/%=%)
TRANSLATE_ARGS = -sourcepath $(JAVA_SRC_DIR) -J-Xmx2G -q
include ../make/translate.mk

FAT_LIB_NAME = junit
FAT_LIB_SOURCES_RELATIVE = $(TRANSLATE_SOURCES:$(GEN_OBJC_DIR)/%=%)
FAT_LIB_SOURCE_DIRS = $(GEN_OBJC_DIR)
FAT_LIB_COMPILE = $(CLANG) $(OBJCFLAGS) -I$(GEN_OBJC_DIR) -I$(ARCH_INCLUDE_DIR)
include ../make/fat_lib.mk

fat_lib_dependencies: jre_emul_dist

java: $(JUNIT_DIST_JAR)
	@:

lib: $(FAT_LIB_LIBRARY)
	@:

$(BUILD_DIR)/.extracted: $(JUNIT_SRC_JAR) $(HAMCREST_SRC_JAR)
	@echo extracting sources
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
	@rm -rf $(BUILD_DIR) $(JUNIT_LIB_DIST)
	@rm -rf $(ARCH_INCLUDE_DIR)/junit $(ARCH_INCLUDE_DIR)/org/junit

dist: lib $(JUNIT_LIB_DIST) $(ARCH_HEADERS)

find_cycles: $(JAVA_SOURCES_MANIFEST) | cycle_finder_dist jre_emul_java_manifest
	$(DIST_DIR)/cycle_finder -sourcepath $(JAVA_SRC_DIR) \
      -cp $(CLASSPATH) -w cycle_whitelist.txt -w ../jre_emul/cycle_whitelist.txt \
      -s ../jre_emul/$(BUILD_DIR_NAME)/java_sources.mf -s $(JAVA_SOURCES_MANIFEST)
