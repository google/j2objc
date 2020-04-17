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

.PHONY: default clean

include environment.mk

EXTRACTED_JAVA_SOURCES = \
    org/mockito/AdditionalAnswers.java \
    org/mockito/AdditionalMatchers.java \
    org/mockito/Answers.java \
    org/mockito/ArgumentCaptor.java \
    org/mockito/ArgumentMatcher.java \
    org/mockito/ArgumentMatchers.java \
    org/mockito/BDDMockito.java \
    org/mockito/Captor.java \
    org/mockito/CheckReturnValue.java \
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
    org/mockito/MockitoFramework.java \
    org/mockito/MockitoSession.java \
    org/mockito/NotExtensible.java \
    org/mockito/Spy.java \
    org/mockito/codegen/InjectionBase.java \
    org/mockito/configuration/AnnotationEngine.java \
    org/mockito/configuration/DefaultMockitoConfiguration.java \
    org/mockito/configuration/IMockitoConfiguration.java \
    org/mockito/creation/instance/InstantiationException.java \
    org/mockito/creation/instance/Instantiator.java \
    org/mockito/exceptions/base/MockitoAssertionError.java \
    org/mockito/exceptions/base/MockitoException.java \
    org/mockito/exceptions/base/MockitoInitializationException.java \
    org/mockito/exceptions/base/MockitoSerializationIssue.java \
    org/mockito/exceptions/misusing/CannotStubVoidMethodWithReturnValue.java \
    org/mockito/exceptions/misusing/CannotVerifyStubOnlyMock.java \
    org/mockito/exceptions/misusing/FriendlyReminderException.java \
    org/mockito/exceptions/misusing/InjectMocksException.java \
    org/mockito/exceptions/misusing/InvalidUseOfMatchersException.java \
    org/mockito/exceptions/misusing/MissingMethodInvocationException.java \
    org/mockito/exceptions/misusing/MockitoConfigurationException.java \
    org/mockito/exceptions/misusing/NotAMockException.java \
    org/mockito/exceptions/misusing/NullInsteadOfMockException.java \
    org/mockito/exceptions/misusing/PotentialStubbingProblem.java \
    org/mockito/exceptions/misusing/RedundantListenerException.java \
    org/mockito/exceptions/misusing/UnfinishedMockingSessionException.java \
    org/mockito/exceptions/misusing/UnfinishedStubbingException.java \
    org/mockito/exceptions/misusing/UnfinishedVerificationException.java \
    org/mockito/exceptions/misusing/UnnecessaryStubbingException.java \
    org/mockito/exceptions/misusing/WrongTypeOfReturnValue.java \
    org/mockito/exceptions/stacktrace/StackTraceCleaner.java \
    org/mockito/exceptions/verification/ArgumentsAreDifferent.java \
    org/mockito/exceptions/verification/MoreThanAllowedActualInvocations.java \
    org/mockito/exceptions/verification/NeverWantedButInvoked.java \
    org/mockito/exceptions/verification/NoInteractionsWanted.java \
    org/mockito/exceptions/verification/SmartNullPointerException.java \
    org/mockito/exceptions/verification/TooLittleActualInvocations.java \
    org/mockito/exceptions/verification/TooManyActualInvocations.java \
    org/mockito/exceptions/verification/VerificationInOrderFailure.java \
    org/mockito/exceptions/verification/WantedButNotInvoked.java \
    org/mockito/exceptions/verification/junit/ArgumentsAreDifferent.java \
    org/mockito/hamcrest/MockitoHamcrest.java \
    org/mockito/internal/InOrderImpl.java \
    org/mockito/internal/InternalMockHandler.java \
    org/mockito/internal/MockitoCore.java \
    org/mockito/internal/configuration/CaptorAnnotationProcessor.java \
    org/mockito/internal/configuration/ClassPathLoader.java \
    org/mockito/internal/configuration/DefaultInjectionEngine.java \
    org/mockito/internal/configuration/FieldAnnotationProcessor.java \
    org/mockito/internal/configuration/GlobalConfiguration.java \
    org/mockito/internal/configuration/IndependentAnnotationEngine.java \
    org/mockito/internal/configuration/InjectingAnnotationEngine.java \
    org/mockito/internal/configuration/MockAnnotationProcessor.java \
    org/mockito/internal/configuration/SpyAnnotationEngine.java \
    org/mockito/internal/configuration/injection/ConstructorInjection.java \
    org/mockito/internal/configuration/injection/MockInjection.java \
    org/mockito/internal/configuration/injection/MockInjectionStrategy.java \
    org/mockito/internal/configuration/injection/PropertyAndSetterInjection.java \
    org/mockito/internal/configuration/injection/SpyOnInjectedFieldsHandler.java \
    org/mockito/internal/configuration/injection/filter/MockCandidateFilter.java \
    org/mockito/internal/configuration/injection/filter/NameBasedCandidateFilter.java \
    org/mockito/internal/configuration/injection/filter/OngoingInjector.java \
    org/mockito/internal/configuration/injection/filter/TerminalMockCandidateFilter.java \
    org/mockito/internal/configuration/injection/filter/TypeBasedCandidateFilter.java \
    org/mockito/internal/configuration/injection/scanner/InjectMocksScanner.java \
    org/mockito/internal/configuration/injection/scanner/MockScanner.java \
    org/mockito/internal/configuration/plugins/DefaultMockitoPlugins.java \
    org/mockito/internal/configuration/plugins/DefaultPluginSwitch.java \
    org/mockito/internal/configuration/plugins/PluginFileReader.java \
    org/mockito/internal/configuration/plugins/PluginFinder.java \
    org/mockito/internal/configuration/plugins/PluginInitializer.java \
    org/mockito/internal/configuration/plugins/PluginLoader.java \
    org/mockito/internal/configuration/plugins/PluginRegistry.java \
    org/mockito/internal/configuration/plugins/Plugins.java \
    org/mockito/internal/creation/DelegatingMethod.java \
    org/mockito/internal/creation/MockSettingsImpl.java \
    org/mockito/internal/creation/SuspendMethod.java \
    org/mockito/internal/creation/instance/ConstructorInstantiator.java \
    org/mockito/internal/creation/instance/InstantiationException.java \
    org/mockito/internal/creation/instance/Instantiator.java \
    org/mockito/internal/creation/instance/InstantiatorProvider2Adapter.java \
    org/mockito/internal/creation/instance/InstantiatorProviderAdapter.java \
    org/mockito/internal/creation/settings/CreationSettings.java \
    org/mockito/internal/creation/util/MockitoMethodProxy.java \
    org/mockito/internal/debugging/FindingsListener.java \
    org/mockito/internal/debugging/InvocationsPrinter.java \
    org/mockito/internal/debugging/Localized.java \
    org/mockito/internal/debugging/LocationImpl.java \
    org/mockito/internal/debugging/LoggingListener.java \
    org/mockito/internal/debugging/MockitoDebuggerImpl.java \
    org/mockito/internal/debugging/VerboseMockInvocationLogger.java \
    org/mockito/internal/debugging/WarningsCollector.java \
    org/mockito/internal/debugging/WarningsFinder.java \
    org/mockito/internal/debugging/WarningsPrinterImpl.java \
    org/mockito/internal/exceptions/ExceptionIncludingMockitoWarnings.java \
    org/mockito/internal/exceptions/Reporter.java \
    org/mockito/internal/exceptions/VerificationAwareInvocation.java \
    org/mockito/internal/exceptions/stacktrace/ConditionalStackTraceFilter.java \
    org/mockito/internal/exceptions/stacktrace/DefaultStackTraceCleaner.java \
    org/mockito/internal/exceptions/stacktrace/DefaultStackTraceCleanerProvider.java \
    org/mockito/internal/exceptions/stacktrace/StackTraceFilter.java \
    org/mockito/internal/exceptions/util/ScenarioPrinter.java \
    org/mockito/internal/framework/DefaultMockitoFramework.java \
    org/mockito/internal/framework/DefaultMockitoSession.java \
    org/mockito/internal/hamcrest/HamcrestArgumentMatcher.java \
    org/mockito/internal/hamcrest/MatcherGenericTypeExtractor.java \
    org/mockito/internal/handler/InvocationNotifierHandler.java \
    org/mockito/internal/handler/MockHandlerFactory.java \
    org/mockito/internal/handler/MockHandlerImpl.java \
    org/mockito/internal/handler/NotifiedMethodInvocationReport.java \
    org/mockito/internal/handler/NullResultGuardian.java \
    org/mockito/internal/invocation/AbstractAwareMethod.java \
    org/mockito/internal/invocation/ArgumentMatcherAction.java \
    org/mockito/internal/invocation/ArgumentsProcessor.java \
    org/mockito/internal/invocation/DefaultInvocationFactory.java \
    org/mockito/internal/invocation/InterceptedInvocation.java \
    org/mockito/internal/invocation/InvocationComparator.java \
    org/mockito/internal/invocation/InvocationMarker.java \
    org/mockito/internal/invocation/InvocationMatcher.java \
    org/mockito/internal/invocation/InvocationsFinder.java \
    org/mockito/internal/invocation/MatcherApplicationStrategy.java \
    org/mockito/internal/invocation/MatchersBinder.java \
    org/mockito/internal/invocation/MockitoMethod.java \
    org/mockito/internal/invocation/RealMethod.java \
    org/mockito/internal/invocation/SerializableMethod.java \
    org/mockito/internal/invocation/StubInfoImpl.java \
    org/mockito/internal/invocation/TypeSafeMatching.java \
    org/mockito/internal/invocation/UnusedStubsFinder.java \
    org/mockito/internal/invocation/finder/AllInvocationsFinder.java \
    org/mockito/internal/invocation/finder/VerifiableInvocationsFinder.java \
    org/mockito/internal/invocation/mockref/MockReference.java \
    org/mockito/internal/invocation/mockref/MockStrongReference.java \
    org/mockito/internal/invocation/mockref/MockWeakReference.java \
    org/mockito/internal/junit/ArgMismatchFinder.java \
    org/mockito/internal/junit/DefaultStubbingLookupListener.java \
    org/mockito/internal/junit/DefaultTestFinishedEvent.java \
    org/mockito/internal/junit/ExceptionFactory.java \
    org/mockito/internal/junit/JUnitRule.java \
    org/mockito/internal/junit/MismatchReportingTestListener.java \
    org/mockito/internal/junit/MockitoTestListener.java \
    org/mockito/internal/junit/NoOpTestListener.java \
    org/mockito/internal/junit/StrictStubsRunnerTestListener.java \
    org/mockito/internal/junit/StubbingArgMismatches.java \
    org/mockito/internal/junit/StubbingHint.java \
    org/mockito/internal/junit/TestFinishedEvent.java \
    org/mockito/internal/junit/UniversalTestListener.java \
    org/mockito/internal/junit/UnnecessaryStubbingsReporter.java \
    org/mockito/internal/junit/UnusedStubbings.java \
    org/mockito/internal/junit/UnusedStubbingsFinder.java \
    org/mockito/internal/junit/VerificationCollectorImpl.java \
    org/mockito/internal/junit/util/JUnitFailureHacker.java \
    org/mockito/internal/listeners/AutoCleanableListener.java \
    org/mockito/internal/listeners/StubbingLookupEvent.java \
    org/mockito/internal/listeners/StubbingLookupListener.java \
    org/mockito/internal/listeners/StubbingLookupNotifier.java \
    org/mockito/internal/listeners/VerificationStartedNotifier.java \
    org/mockito/internal/matchers/And.java \
    org/mockito/internal/matchers/Any.java \
    org/mockito/internal/matchers/ArrayEquals.java \
    org/mockito/internal/matchers/CapturesArguments.java \
    org/mockito/internal/matchers/CapturingMatcher.java \
    org/mockito/internal/matchers/CompareEqual.java \
    org/mockito/internal/matchers/CompareTo.java \
    org/mockito/internal/matchers/Contains.java \
    org/mockito/internal/matchers/ContainsExtraTypeInfo.java \
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
    org/mockito/internal/matchers/text/FormattedText.java \
    org/mockito/internal/matchers/text/MatcherToString.java \
    org/mockito/internal/matchers/text/MatchersPrinter.java \
    org/mockito/internal/matchers/text/ValuePrinter.java \
    org/mockito/internal/progress/ArgumentMatcherStorage.java \
    org/mockito/internal/progress/ArgumentMatcherStorageImpl.java \
    org/mockito/internal/progress/MockingProgress.java \
    org/mockito/internal/progress/MockingProgressImpl.java \
    org/mockito/internal/progress/SequenceNumber.java \
    org/mockito/internal/progress/ThreadSafeMockingProgress.java \
    org/mockito/internal/reporting/Discrepancy.java \
    org/mockito/internal/reporting/Pluralizer.java \
    org/mockito/internal/reporting/PrintSettings.java \
    org/mockito/internal/reporting/SmartPrinter.java \
    org/mockito/internal/runners/DefaultInternalRunner.java \
    org/mockito/internal/runners/InternalRunner.java \
    org/mockito/internal/runners/RunnerFactory.java \
    org/mockito/internal/runners/StrictRunner.java \
    org/mockito/internal/runners/util/FailureDetector.java \
    org/mockito/internal/runners/util/RunnerProvider.java \
    org/mockito/internal/runners/util/TestMethodsFinder.java \
    org/mockito/internal/session/DefaultMockitoSessionBuilder.java \
    org/mockito/internal/session/MockitoLoggerAdapter.java \
    org/mockito/internal/session/MockitoSessionLoggerAdapter.java \
    org/mockito/internal/stubbing/BaseStubbing.java \
    org/mockito/internal/stubbing/ConsecutiveStubbing.java \
    org/mockito/internal/stubbing/DefaultLenientStubber.java \
    org/mockito/internal/stubbing/DoAnswerStyleStubbing.java \
    org/mockito/internal/stubbing/InvocationContainerImpl.java \
    org/mockito/internal/stubbing/OngoingStubbingImpl.java \
    org/mockito/internal/stubbing/StrictnessSelector.java \
    org/mockito/internal/stubbing/StubbedInvocationMatcher.java \
    org/mockito/internal/stubbing/StubberImpl.java \
    org/mockito/internal/stubbing/StubbingComparator.java \
    org/mockito/internal/stubbing/UnusedStubbingReporting.java \
    org/mockito/internal/stubbing/answers/AnswerFunctionalInterfaces.java \
    org/mockito/internal/stubbing/answers/AnswersWithDelay.java \
    org/mockito/internal/stubbing/answers/CallsRealMethods.java \
    org/mockito/internal/stubbing/answers/ClonesArguments.java \
    org/mockito/internal/stubbing/answers/DefaultAnswerValidator.java \
    org/mockito/internal/stubbing/answers/DoesNothing.java \
    org/mockito/internal/stubbing/answers/InvocationInfo.java \
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
    org/mockito/internal/stubbing/defaultanswers/TriesToReturnSelf.java \
    org/mockito/internal/util/Checks.java \
    org/mockito/internal/util/ConsoleMockitoLogger.java \
    org/mockito/internal/util/DefaultMockingDetails.java \
    org/mockito/internal/util/JavaEightUtil.java \
    org/mockito/internal/util/MockCreationValidator.java \
    org/mockito/internal/util/MockitoLogger.java \
    org/mockito/internal/util/MockNameImpl.java \
    org/mockito/internal/util/MockUtil.java \
    org/mockito/internal/util/ObjectMethodsGuru.java \
    org/mockito/internal/util/Platform.java \
    org/mockito/internal/util/Primitives.java \
    org/mockito/internal/util/StringUtil.java \
    org/mockito/internal/util/Supplier.java \
    org/mockito/internal/util/Timer.java \
    org/mockito/internal/util/collections/HashCodeAndEqualsMockWrapper.java \
    org/mockito/internal/util/collections/HashCodeAndEqualsSafeSet.java \
    org/mockito/internal/util/collections/IdentitySet.java \
    org/mockito/internal/util/collections/Iterables.java \
    org/mockito/internal/util/collections/ListUtil.java \
    org/mockito/internal/util/collections/Sets.java \
    org/mockito/internal/util/concurrent/DetachedThreadLocal.java \
    org/mockito/internal/util/concurrent/WeakConcurrentMap.java \
    org/mockito/internal/util/concurrent/WeakConcurrentSet.java \
    org/mockito/internal/util/io/IOUtil.java \
    org/mockito/internal/util/reflection/AccessibilityChanger.java \
    org/mockito/internal/util/reflection/BeanPropertySetter.java \
    org/mockito/internal/util/reflection/FieldCopier.java \
    org/mockito/internal/util/reflection/FieldInitializationReport.java \
    org/mockito/internal/util/reflection/FieldInitializer.java \
    org/mockito/internal/util/reflection/FieldReader.java \
    org/mockito/internal/util/reflection/FieldSetter.java \
    org/mockito/internal/util/reflection/Fields.java \
    org/mockito/internal/util/reflection/GenericMaster.java \
    org/mockito/internal/util/reflection/GenericMetadataSupport.java \
    org/mockito/internal/util/reflection/GenericTypeExtractor.java \
    org/mockito/internal/util/reflection/InstanceField.java \
    org/mockito/internal/util/reflection/LenientCopyTool.java \
    org/mockito/internal/util/reflection/SuperTypesLastSorter.java \
    org/mockito/internal/verification/AtLeast.java \
    org/mockito/internal/verification/AtMost.java \
    org/mockito/internal/verification/Calls.java \
    org/mockito/internal/verification/DefaultRegisteredInvocations.java \
    org/mockito/internal/verification/Description.java \
    org/mockito/internal/verification/InOrderContextImpl.java \
    org/mockito/internal/verification/InOrderWrapper.java \
    org/mockito/internal/verification/MockAwareVerificationMode.java \
    org/mockito/internal/verification/NoMoreInteractions.java \
    org/mockito/internal/verification/Only.java \
    org/mockito/internal/verification/RegisteredInvocations.java \
    org/mockito/internal/verification/SingleRegisteredInvocation.java \
    org/mockito/internal/verification/Times.java \
    org/mockito/internal/verification/VerificationDataImpl.java \
    org/mockito/internal/verification/VerificationEventImpl.java \
    org/mockito/internal/verification/VerificationModeFactory.java \
    org/mockito/internal/verification/VerificationOverTimeImpl.java \
    org/mockito/internal/verification/VerificationWrapper.java \
    org/mockito/internal/verification/VerificationWrapperInOrderWrapper.java \
    org/mockito/internal/verification/api/InOrderContext.java \
    org/mockito/internal/verification/api/VerificationData.java \
    org/mockito/internal/verification/api/VerificationDataInOrder.java \
    org/mockito/internal/verification/api/VerificationDataInOrderImpl.java \
    org/mockito/internal/verification/api/VerificationInOrderMode.java \
    org/mockito/internal/verification/argumentmatching/ArgumentMatchingTool.java \
    org/mockito/internal/verification/checkers/AtLeastDiscrepancy.java \
    org/mockito/internal/verification/checkers/AtLeastXNumberOfInvocationsChecker.java \
    org/mockito/internal/verification/checkers/MissingInvocationChecker.java \
    org/mockito/internal/verification/checkers/NumberOfInvocationsChecker.java \
    org/mockito/invocation/DescribedInvocation.java \
    org/mockito/invocation/Invocation.java \
    org/mockito/invocation/InvocationContainer.java \
    org/mockito/invocation/InvocationFactory.java \
    org/mockito/invocation/InvocationOnMock.java \
    org/mockito/invocation/Location.java \
    org/mockito/invocation/MatchableInvocation.java \
    org/mockito/invocation/MockHandler.java \
    org/mockito/invocation/StubInfo.java \
    org/mockito/junit/MockitoJUnit.java \
    org/mockito/junit/MockitoJUnitRunner.java \
    org/mockito/junit/MockitoRule.java \
    org/mockito/junit/VerificationCollector.java \
    org/mockito/listeners/InvocationListener.java \
    org/mockito/listeners/MethodInvocationReport.java \
    org/mockito/listeners/MockCreationListener.java \
    org/mockito/listeners/MockitoListener.java \
    org/mockito/listeners/VerificationListener.java \
    org/mockito/listeners/VerificationStartedEvent.java \
    org/mockito/listeners/VerificationStartedListener.java \
    org/mockito/mock/MockCreationSettings.java \
    org/mockito/mock/MockName.java \
    org/mockito/mock/SerializableMode.java \
    org/mockito/plugins/AnnotationEngine.java \
    org/mockito/plugins/InstantiatorProvider.java \
    org/mockito/plugins/InstantiatorProvider2.java \
    org/mockito/plugins/MockMaker.java \
    org/mockito/plugins/MockitoPlugins.java \
    org/mockito/plugins/PluginSwitch.java \
    org/mockito/plugins/StackTraceCleanerProvider.java \
    org/mockito/quality/MockitoHint.java \
    org/mockito/quality/Strictness.java \
    org/mockito/runners/ConsoleSpammingMockitoJUnitRunner.java \
    org/mockito/runners/MockitoJUnitRunner.java \
    org/mockito/runners/VerboseMockitoJUnitRunner.java \
    org/mockito/session/MockitoSessionBuilder.java \
    org/mockito/session/MockitoSessionLogger.java \
    org/mockito/stubbing/Answer.java \
    org/mockito/stubbing/Answer1.java \
    org/mockito/stubbing/Answer2.java \
    org/mockito/stubbing/Answer3.java \
    org/mockito/stubbing/Answer4.java \
    org/mockito/stubbing/Answer5.java \
    org/mockito/stubbing/BaseStubber.java \
    org/mockito/stubbing/LenientStubber.java \
    org/mockito/stubbing/OngoingStubbing.java \
    org/mockito/stubbing/Stubber.java \
    org/mockito/stubbing/Stubbing.java \
    org/mockito/stubbing/ValidableAnswer.java \
    org/mockito/stubbing/VoidAnswer1.java \
    org/mockito/stubbing/VoidAnswer2.java \
    org/mockito/stubbing/VoidAnswer3.java \
    org/mockito/stubbing/VoidAnswer4.java \
    org/mockito/stubbing/VoidAnswer5.java \
    org/mockito/verification/After.java \
    org/mockito/verification/Timeout.java \
    org/mockito/verification/VerificationAfterDelay.java \
    org/mockito/verification/VerificationEvent.java \
    org/mockito/verification/VerificationMode.java \
    org/mockito/verification/VerificationStrategy.java \
    org/mockito/verification/VerificationWithTimeout.java

J2OBJC_PLUGIN_JAVA_SOURCES = \
    com/google/j2objc/mockito/InvocationHandlerAdapter.java \
    com/google/j2objc/mockito/IosMockMaker.java \
    org/objenesis/ObjenesisHelper.java

EXTRACTED_JAVA = $(EXTRACTED_JAVA_SOURCES:%=$(EXTRACTED_JAVA_SRC_DIR)/%)
J2OBJC_PLUGIN_JAVA = $(J2OBJC_PLUGIN_JAVA_SOURCES:%=$(J2OBJC_PLUGIN_SRC_DIR)/%)
JAVA_SOURCES_MANIFEST = $(BUILD_DIR)/java_sources.mf
OBJC_SOURCES_MANIFEST = $(BUILD_DIR)/objc_sources.mf

default: lib

CREATE_JAR_NAME = mockito-ios
CREATE_JAR_SOURCES = $(EXTRACTED_JAVA) $(J2OBJC_PLUGIN_JAVA)
CREATE_JAR_JAVAC_ARGS = -classpath $(MOCKITO_CLASSPATH)
include $(J2OBJC_ROOT)/make/create_jar.mk

TRANSLATE_JAVA_RELATIVE = $(EXTRACTED_JAVA_SOURCES) $(J2OBJC_PLUGIN_JAVA_SOURCES)
TRANSLATE_JAVA_FULL = $(EXTRACTED_JAVA) $(J2OBJC_PLUGIN_JAVA)
TRANSLATE_ARGS = -sourcepath $(MOCKITO_SOURCEPATH) -classpath $(MOCKITO_CLASSPATH)
include $(J2OBJC_ROOT)/make/translate.mk

ARCH_HEADERS = $(TRANSLATE_HEADERS:$(GEN_OBJC_DIR)/%=$(ARCH_INCLUDE_DIR)/%)

FAT_LIB_NAME = mockito
FAT_LIB_SOURCES_RELATIVE = $(TRANSLATE_SOURCES:$(GEN_OBJC_DIR)/%=%)
FAT_LIB_SOURCE_DIRS = $(GEN_OBJC_DIR)
FAT_LIB_COMPILE = $(J2OBJCC)
include $(J2OBJC_ROOT)/make/fat_lib.mk

FRAMEWORK_NAME = Mockito
include $(J2OBJC_ROOT)/make/framework.mk

MOCKITO_LICENSE = LICENSE
MOCKITO_LICENSE_DIST = $(DIST_LICENSE_DIR)/mockito_license.txt

java: jar

lib: $(FAT_LIBS)
	@:

$(BUILD_DIR)/.extracted: $(MOCKITO_SRC_JAR) | $(BUILD_DIR)
	@mkdir -p $(EXTRACTED_JAVA_SRC_DIR)
	@unzip -q -o -d $(EXTRACTED_JAVA_SRC_DIR) $(MOCKITO_SRC_JAR) $(EXTRACTED_JAVA_SOURCES)
	@touch $(EXTRACTED_JAVA)
	@touch $@

$(EXTRACTED_JAVA): $(BUILD_DIR)/.extracted

$(ARCH_INCLUDE_DIR)/%.h: $(GEN_OBJC_DIR)/%.h
	@mkdir -p $(@D)
	install -m 0644 $< $@

$(MOCKITO_LICENSE_DIST): $(MOCKITO_LICENSE)
	@mkdir -p $(@D)
	@install -m 0644 $< $@

$(JAVA_SOURCES_MANIFEST): $(EXTRACTED_JAVA) $(J2OBJC_PLUGIN_JAVA) | $(BUILD_DIR)
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

test: dist $(DIST_DIR)/j2objcc
	$(MAKE) -f tests.mk test

clean:
	@rm -rf $(BUILD_DIR) $(FAT_LIBS_DIST) $(MOCKITO_LICENSE_DIST)
	@rm -rf $(ARCH_INCLUDE_DIR)/org/mockito
	@rm -rf $(FRAMEWORK_DIR)

dist: $(FAT_LIBS_DIST) $(ARCH_HEADERS) $(MOCKITO_LICENSE_DIST)
