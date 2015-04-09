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

# Makefile for building the iOS Mockito library.
#
# Author: Tom Ball

.SUFFIXES:
.PHONY: default clean

include environment.mk

EXTRACTED_JAVA_SOURCES = \
    org/mockito/AdditionalAnswers.java \
    org/mockito/AdditionalMatchers.java \
    org/mockito/Answers.java \
    org/mockito/ArgumentCaptor.java \
    org/mockito/ArgumentMatcher.java \
    org/mockito/BDDMockito.java \
    org/mockito/Captor.java \
    org/mockito/InOrder.java \
    org/mockito/Incubating.java \
    org/mockito/InjectMocks.java \
    org/mockito/Matchers.java \
    org/mockito/Mock.java \
    org/mockito/MockSettings.java \
    org/mockito/MockingDetails.java \
    org/mockito/Mockito.java \
    org/mockito/MockitoAnnotations.java \
    org/mockito/MockitoDebugger.java \
    org/mockito/ReturnValues.java \
    org/mockito/Spy.java \
    org/mockito/configuration/AnnotationEngine.java \
    org/mockito/configuration/DefaultMockitoConfiguration.java \
    org/mockito/configuration/IMockitoConfiguration.java \
    org/mockito/exceptions/PrintableInvocation.java \
    org/mockito/exceptions/Reporter.java \
    org/mockito/exceptions/base/MockitoAssertionError.java \
    org/mockito/exceptions/base/MockitoException.java \
    org/mockito/exceptions/misusing/FriendlyReminderException.java \
    org/mockito/exceptions/misusing/InvalidUseOfMatchersException.java \
    org/mockito/exceptions/misusing/MissingMethodInvocationException.java \
    org/mockito/exceptions/misusing/MockitoConfigurationException.java \
    org/mockito/exceptions/misusing/NotAMockException.java \
    org/mockito/exceptions/misusing/NullInsteadOfMockException.java \
    org/mockito/exceptions/misusing/UnfinishedStubbingException.java \
    org/mockito/exceptions/misusing/UnfinishedVerificationException.java \
    org/mockito/exceptions/misusing/WrongTypeOfReturnValue.java \
    org/mockito/exceptions/stacktrace/StackTraceCleaner.java \
    org/mockito/exceptions/verification/ArgumentsAreDifferent.java \
    org/mockito/exceptions/verification/NeverWantedButInvoked.java \
    org/mockito/exceptions/verification/NoInteractionsWanted.java \
    org/mockito/exceptions/verification/SmartNullPointerException.java \
    org/mockito/exceptions/verification/TooLittleActualInvocations.java \
    org/mockito/exceptions/verification/TooManyActualInvocations.java \
    org/mockito/exceptions/verification/VerificationInOrderFailure.java \
    org/mockito/exceptions/verification/WantedButNotInvoked.java \
    org/mockito/internal/InOrderImpl.java \
    org/mockito/internal/InternalMockHandler.java \
    org/mockito/internal/MockitoCore.java \
    org/mockito/internal/configuration/CaptorAnnotationProcessor.java \
    org/mockito/internal/configuration/DefaultAnnotationEngine.java \
    org/mockito/internal/configuration/DefaultInjectionEngine.java \
    org/mockito/internal/configuration/FieldAnnotationProcessor.java \
    org/mockito/internal/configuration/GlobalConfiguration.java \
    org/mockito/internal/configuration/InjectingAnnotationEngine.java \
    org/mockito/internal/configuration/MockAnnotationProcessor.java \
    org/mockito/internal/configuration/MockitoAnnotationsMockAnnotationProcessor.java \
    org/mockito/internal/configuration/SpyAnnotationEngine.java \
    org/mockito/internal/configuration/injection/ConstructorInjection.java \
    org/mockito/internal/configuration/injection/MockInjection.java \
    org/mockito/internal/configuration/injection/MockInjectionStrategy.java \
    org/mockito/internal/configuration/injection/PropertyAndSetterInjection.java \
    org/mockito/internal/configuration/injection/SpyOnInjectedFieldsHandler.java \
    org/mockito/internal/configuration/injection/filter/FinalMockCandidateFilter.java \
    org/mockito/internal/configuration/injection/filter/MockCandidateFilter.java \
    org/mockito/internal/configuration/injection/filter/NameBasedCandidateFilter.java \
    org/mockito/internal/configuration/injection/filter/OngoingInjecter.java \
    org/mockito/internal/configuration/injection/filter/TypeBasedCandidateFilter.java \
    org/mockito/internal/configuration/injection/scanner/InjectMocksScanner.java \
    org/mockito/internal/configuration/injection/scanner/MockScanner.java \
    org/mockito/internal/creation/settings/CreationSettings.java \
    org/mockito/internal/debugging/Localized.java \
    org/mockito/internal/debugging/LocationImpl.java \
    org/mockito/internal/debugging/MockitoDebuggerImpl.java \
    org/mockito/internal/debugging/VerboseMockInvocationLogger.java \
    org/mockito/internal/exceptions/VerificationAwareInvocation.java \
    org/mockito/internal/exceptions/stacktrace/ConditionalStackTraceFilter.java \
    org/mockito/internal/exceptions/stacktrace/DefaultStackTraceCleaner.java \
    org/mockito/internal/exceptions/stacktrace/DefaultStackTraceCleanerProvider.java \
    org/mockito/internal/exceptions/stacktrace/StackTraceFilter.java \
    org/mockito/internal/exceptions/util/ScenarioPrinter.java \
    org/mockito/internal/handler/InvocationNotifierHandler.java \
    org/mockito/internal/handler/MockHandlerFactory.java \
    org/mockito/internal/handler/MockHandlerImpl.java \
    org/mockito/internal/handler/NullResultGuardian.java \
    org/mockito/internal/invocation/CapturesArgumensFromInvocation.java \
    org/mockito/internal/invocation/InvocationImpl.java \
    org/mockito/internal/invocation/InvocationMarker.java \
    org/mockito/internal/invocation/InvocationMatcher.java \
    org/mockito/internal/invocation/InvocationsFinder.java \
    org/mockito/internal/invocation/MatchersBinder.java \
    org/mockito/internal/invocation/MockitoMethod.java \
    org/mockito/internal/invocation/SerializableMethod.java \
    org/mockito/internal/invocation/StubInfoImpl.java \
    org/mockito/internal/invocation/UnusedStubsFinder.java \
    org/mockito/internal/invocation/finder/AllInvocationsFinder.java \
    org/mockito/internal/invocation/finder/VerifiableInvocationsFinder.java \
    org/mockito/internal/invocation/realmethod/RealMethod.java \
    org/mockito/internal/junit/JUnitTool.java \
    org/mockito/internal/listeners/MockingProgressListener.java \
    org/mockito/internal/listeners/MockingStartedListener.java \
    org/mockito/internal/listeners/NotifiedMethodInvocationReport.java \
    org/mockito/internal/matchers/And.java \
    org/mockito/internal/matchers/Any.java \
    org/mockito/internal/matchers/AnyVararg.java \
    org/mockito/internal/matchers/ArrayEquals.java \
    org/mockito/internal/matchers/CapturesArguments.java \
    org/mockito/internal/matchers/CapturingMatcher.java \
    org/mockito/internal/matchers/CompareEqual.java \
    org/mockito/internal/matchers/CompareTo.java \
    org/mockito/internal/matchers/Contains.java \
    org/mockito/internal/matchers/ContainsExtraTypeInformation.java \
    org/mockito/internal/matchers/EndsWith.java \
    org/mockito/internal/matchers/Equality.java \
    org/mockito/internal/matchers/Equals.java \
    org/mockito/internal/matchers/EqualsWithDelta.java \
    org/mockito/internal/matchers/Find.java \
    org/mockito/internal/matchers/GreaterOrEqual.java \
    org/mockito/internal/matchers/GreaterThan.java \
    org/mockito/internal/matchers/InstanceOf.java \
    org/mockito/internal/matchers/LessOrEqual.java \
    org/mockito/internal/matchers/LessThan.java \
    org/mockito/internal/matchers/LocalizedMatcher.java \
    org/mockito/internal/matchers/MatcherDecorator.java \
    org/mockito/internal/matchers/MatchersPrinter.java \
    org/mockito/internal/matchers/Matches.java \
    org/mockito/internal/matchers/Not.java \
    org/mockito/internal/matchers/NotNull.java \
    org/mockito/internal/matchers/Null.java \
    org/mockito/internal/matchers/Or.java \
    org/mockito/internal/matchers/Same.java \
    org/mockito/internal/matchers/StartsWith.java \
    org/mockito/internal/matchers/VarargMatcher.java \
    org/mockito/internal/matchers/apachecommons/EqualsBuilder.java \
    org/mockito/internal/matchers/apachecommons/ReflectionEquals.java \
    org/mockito/internal/progress/ArgumentMatcherStorage.java \
    org/mockito/internal/progress/ArgumentMatcherStorageImpl.java \
    org/mockito/internal/progress/HandyReturnValues.java \
    org/mockito/internal/progress/IOngoingStubbing.java \
    org/mockito/internal/progress/MockingProgress.java \
    org/mockito/internal/progress/MockingProgressImpl.java \
    org/mockito/internal/progress/SequenceNumber.java \
    org/mockito/internal/progress/ThreadSafeMockingProgress.java \
    org/mockito/internal/reporting/Discrepancy.java \
    org/mockito/internal/reporting/Pluralizer.java \
    org/mockito/internal/reporting/PrintSettings.java \
    org/mockito/internal/reporting/SmartPrinter.java \
    org/mockito/internal/runners/RunnerFactory.java \
    org/mockito/internal/runners/RunnerImpl.java \
    org/mockito/internal/runners/util/RunnerProvider.java \
    org/mockito/internal/runners/util/TestMethodsFinder.java \
    org/mockito/internal/stubbing/BaseStubbing.java \
    org/mockito/internal/stubbing/ConsecutiveStubbing.java \
    org/mockito/internal/stubbing/InvocationContainer.java \
    org/mockito/internal/stubbing/InvocationContainerImpl.java \
    org/mockito/internal/stubbing/OngoingStubbingImpl.java \
    org/mockito/internal/stubbing/StubbedInvocationMatcher.java \
    org/mockito/internal/stubbing/StubberImpl.java \
    org/mockito/internal/stubbing/VoidMethodStubbableImpl.java \
    org/mockito/internal/stubbing/answers/AnswerReturnValuesAdapter.java \
    org/mockito/internal/stubbing/answers/AnswersValidator.java \
    org/mockito/internal/stubbing/answers/CallsRealMethods.java \
    org/mockito/internal/stubbing/answers/DoesNothing.java \
    org/mockito/internal/stubbing/answers/MethodInfo.java \
    org/mockito/internal/stubbing/answers/Returns.java \
    org/mockito/internal/stubbing/answers/ReturnsArgumentAt.java \
    org/mockito/internal/stubbing/answers/ReturnsElementsOf.java \
    org/mockito/internal/stubbing/answers/ThrowsException.java \
    org/mockito/internal/stubbing/defaultanswers/ForwardsInvocations.java \
    org/mockito/internal/stubbing/defaultanswers/GloballyConfiguredAnswer.java \
    org/mockito/internal/stubbing/defaultanswers/ReturnsDeepStubs.java \
    org/mockito/internal/stubbing/defaultanswers/ReturnsEmptyValues.java \
    org/mockito/internal/stubbing/defaultanswers/ReturnsMocks.java \
    org/mockito/internal/stubbing/defaultanswers/ReturnsMoreEmptyValues.java \
    org/mockito/internal/stubbing/defaultanswers/ReturnsSmartNulls.java \
    org/mockito/internal/util/Checks.java \
    org/mockito/internal/util/Decamelizer.java \
    org/mockito/internal/util/DefaultMockingDetails.java \
    org/mockito/internal/util/MockCreationValidator.java \
    org/mockito/internal/util/MockNameImpl.java \
    org/mockito/internal/util/MockUtil.java \
    org/mockito/internal/util/MockitoSpy.java \
    org/mockito/internal/util/ObjectMethodsGuru.java \
    org/mockito/internal/util/Primitives.java \
    org/mockito/internal/util/RemoveFirstLine.java \
    org/mockito/internal/util/StringJoiner.java \
    org/mockito/internal/util/collections/ArrayUtils.java \
    org/mockito/internal/util/collections/HashCodeAndEqualsMockWrapper.java \
    org/mockito/internal/util/collections/HashCodeAndEqualsSafeSet.java \
    org/mockito/internal/util/collections/IdentitySet.java \
    org/mockito/internal/util/collections/ListUtil.java \
    org/mockito/internal/util/collections/Sets.java \
    org/mockito/internal/util/reflection/AccessibilityChanger.java \
    org/mockito/internal/util/reflection/BeanPropertySetter.java \
    org/mockito/internal/util/reflection/FieldCopier.java \
    org/mockito/internal/util/reflection/FieldInitializationReport.java \
    org/mockito/internal/util/reflection/FieldInitializer.java \
    org/mockito/internal/util/reflection/FieldReader.java \
    org/mockito/internal/util/reflection/FieldSetter.java \
    org/mockito/internal/util/reflection/GenericMaster.java \
    org/mockito/internal/util/reflection/LenientCopyTool.java \
    org/mockito/internal/util/reflection/Whitebox.java \
    org/mockito/internal/verification/AtLeast.java \
    org/mockito/internal/verification/AtMost.java \
    org/mockito/internal/verification/Calls.java \
    org/mockito/internal/verification/InOrderContextImpl.java \
    org/mockito/internal/verification/InOrderWrapper.java \
    org/mockito/internal/verification/MockAwareVerificationMode.java \
    org/mockito/internal/verification/NoMoreInteractions.java \
    org/mockito/internal/verification/Only.java \
    org/mockito/internal/verification/RegisteredInvocations.java \
    org/mockito/internal/verification/Times.java \
    org/mockito/internal/verification/VerificationDataImpl.java \
    org/mockito/internal/verification/VerificationModeFactory.java \
    org/mockito/internal/verification/VerificationWithTimeoutImpl.java \
    org/mockito/internal/verification/api/InOrderContext.java \
    org/mockito/internal/verification/api/VerificationData.java \
    org/mockito/internal/verification/api/VerificationDataInOrder.java \
    org/mockito/internal/verification/api/VerificationDataInOrderImpl.java \
    org/mockito/internal/verification/api/VerificationInOrderMode.java \
    org/mockito/internal/verification/argumentmatching/ArgumentMatchingTool.java \
    org/mockito/internal/verification/checkers/AtLeastDiscrepancy.java \
    org/mockito/internal/verification/checkers/AtLeastXNumberOfInvocationsChecker.java \
    org/mockito/internal/verification/checkers/AtLeastXNumberOfInvocationsInOrderChecker.java \
    org/mockito/internal/verification/checkers/MissingInvocationChecker.java \
    org/mockito/internal/verification/checkers/MissingInvocationInOrderChecker.java \
    org/mockito/internal/verification/checkers/NonGreedyNumberOfInvocationsInOrderChecker.java \
    org/mockito/internal/verification/checkers/NumberOfInvocationsChecker.java \
    org/mockito/internal/verification/checkers/NumberOfInvocationsInOrderChecker.java \
    org/mockito/internal/invocation/ArgumentsComparator.java \
    org/mockito/internal/invocation/ArgumentsProcessor.java \
    org/mockito/invocation/DescribedInvocation.java \
    org/mockito/invocation/Invocation.java \
    org/mockito/invocation/InvocationOnMock.java \
    org/mockito/invocation/Location.java \
    org/mockito/invocation/MockHandler.java \
    org/mockito/invocation/StubInfo.java \
    org/mockito/listeners/InvocationListener.java \
    org/mockito/listeners/MethodInvocationReport.java \
    org/mockito/mock/MockCreationSettings.java \
    org/mockito/mock/MockName.java \
    org/mockito/plugins/MockMaker.java \
    org/mockito/plugins/StackTraceCleanerProvider.java \
    org/mockito/runners/MockitoJUnitRunner.java \
    org/mockito/stubbing/Answer.java \
    org/mockito/stubbing/DeprecatedOngoingStubbing.java \
    org/mockito/stubbing/OngoingStubbing.java \
    org/mockito/stubbing/Stubber.java \
    org/mockito/stubbing/VoidMethodStubbable.java \
    org/mockito/verification/Timeout.java \
    org/mockito/verification/VerificationMode.java \
    org/mockito/verification/VerificationWithTimeout.java

SUPERSOURCE_JAVA_SOURCES = \
    org/mockito/internal/configuration/ClassPathLoader.java \
    org/mockito/internal/creation/ClassNameFinder.java \
    org/mockito/internal/creation/DelegatingMethod.java \
    org/mockito/internal/creation/MockSettingsImpl.java \
    org/mockito/internal/creation/ios/InvocationHandlerAdapter.java \
    org/mockito/internal/creation/ios/IosMockMaker.java \
    org/mockito/internal/creation/jmock/ClassImposterizer.java \
    org/mockito/internal/stubbing/answers/ThrowsExceptionClass.java

EXTRACTED_JAVA = $(EXTRACTED_JAVA_SOURCES:%=$(EXTRACTED_JAVA_SRC_DIR)/%)
SUPERSOURCE_JAVA = $(SUPERSOURCE_JAVA_SOURCES:%=$(SUPERSOURCE_JAVA_SRC_DIR)/%)
JAVA_SOURCES_MANIFEST = $(BUILD_DIR)/java_sources.mf
OBJC_SOURCES_MANIFEST = $(BUILD_DIR)/objc_sources.mf

default: lib

CREATE_JAR_NAME = mockito-ios
CREATE_JAR_SOURCES = $(EXTRACTED_JAVA) $(SUPERSOURCE_JAVA)
CREATE_JAR_JAVAC_ARGS = -classpath $(MOCKITO_CLASSPATH)
include $(J2OBJC_ROOT)/make/create_jar.mk

TRANSLATE_JAVA_RELATIVE = $(EXTRACTED_JAVA_SOURCES) $(SUPERSOURCE_JAVA_SOURCES)
TRANSLATE_JAVA_FULL = $(EXTRACTED_JAVA) $(SUPERSOURCE_JAVA)
TRANSLATE_ARGS = -sourcepath $(MOCKITO_SOURCEPATH) -classpath $(MOCKITO_CLASSPATH) \
    --batch-translate-max=300 -J-Xmx2G
include $(J2OBJC_ROOT)/make/translate.mk

ARCH_HEADERS = $(TRANSLATE_HEADERS:$(GEN_OBJC_DIR)/%=$(ARCH_INCLUDE_DIR)/%)

FAT_LIB_NAME = mockito
FAT_LIB_SOURCES_RELATIVE = $(TRANSLATE_SOURCES:$(GEN_OBJC_DIR)/%=%)
FAT_LIB_SOURCE_DIRS = $(GEN_OBJC_DIR)
FAT_LIB_COMPILE = $(J2OBJCC)
include $(J2OBJC_ROOT)/make/fat_lib.mk

java: jar

lib: $(FAT_LIB_LIBRARY)
	@:

$(BUILD_DIR)/.extracted: $(MOCKITO_SRC_JAR) | $(BUILD_DIR)
	@mkdir -p $(EXTRACTED_JAVA_SRC_DIR)
	@unzip -q -o -d $(EXTRACTED_JAVA_SRC_DIR) $(MOCKITO_SRC_JAR) $(EXTRACTED_JAVA_SOURCES)
	@touch $(EXTRACTED_JAVA)
	@touch $@

$(EXTRACTED_JAVA): $(BUILD_DIR)/.extracted

$(MOCKITO_LIB_DIST): $(FAT_LIB_LIBRARY)
	@mkdir -p $(@D)
	install -m 0644 $< $@

$(ARCH_INCLUDE_DIR)/%.h: $(GEN_OBJC_DIR)/%.h
	@mkdir -p $(@D)
	install -m 0644 $< $@

$(JAVA_SOURCES_MANIFEST): $(EXTRACTED_JAVA) $(SUPERSOURCE_JAVA) | $(BUILD_DIR)
	@echo "Building $$(basename $@)"
	@if [ -e $@ ]; then rm $@; fi
	@for i in $^; do echo $$i >> $@; done

java_sources_manifest: $(JAVA_SOURCES_MANIFEST)
	@:

$(OBJC_SOURCES_MANIFEST): $(TRANSLATE_JAVA_FULL) | $(BUILD_DIR)
	@echo "Building $$(basename $@)"
	@if [ -e $@ ]; then rm $@; fi
	@files='$(sort $(TRANSLATE_OBJC))' && for i in $$files; do \
	  echo $$i >> $@; \
	done

objc_sources_manifest: $(OBJC_SOURCES_MANIFEST)
	@:

$(BUILD_DIR):
	@mkdir -p $@

clean:
	@rm -rf $(BUILD_DIR) $(MOCKITO_LIB_DIST)
	@rm -rf $(ARCH_INCLUDE_DIR)/org/mockito

dist: $(MOCKITO_LIB_DIST) $(ARCH_HEADERS)
