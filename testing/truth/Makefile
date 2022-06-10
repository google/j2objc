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

# Makefile for building the iOS truth fluent assertions library.

.PHONY: default clean

include environment.mk

JAVA_SOURCES = \
  com/google/common/truth/AbstractArraySubject.java \
  com/google/common/truth/AssertionErrorWithFacts.java \
  com/google/common/truth/BigDecimalSubject.java \
  com/google/common/truth/BooleanSubject.java \
  com/google/common/truth/ClassSubject.java \
  com/google/common/truth/ComparableSubject.java \
  com/google/common/truth/ComparisonFailureWithFacts.java \
  com/google/common/truth/ComparisonFailures.java \
  com/google/common/truth/Correspondence.java \
  com/google/common/truth/CustomSubjectBuilder.java \
  com/google/common/truth/DiffUtils.java \
  com/google/common/truth/DoubleSubject.java \
  com/google/common/truth/ErrorWithFacts.java \
  com/google/common/truth/Expect.java \
  com/google/common/truth/ExpectFailure.java \
  com/google/common/truth/Fact.java \
  com/google/common/truth/FailureMetadata.java \
  com/google/common/truth/FailureStrategy.java \
  com/google/common/truth/FloatSubject.java \
  com/google/common/truth/GraphMatching.java \
  com/google/common/truth/GuavaOptionalSubject.java \
  com/google/common/truth/IntegerSubject.java \
  com/google/common/truth/IterableSubject.java \
  com/google/common/truth/LazyMessage.java \
  com/google/common/truth/LongSubject.java \
  com/google/common/truth/MapSubject.java \
  com/google/common/truth/MathUtil.java \
  com/google/common/truth/MultimapSubject.java \
  com/google/common/truth/MultisetSubject.java \
  com/google/common/truth/ObjectArraySubject.java \
  com/google/common/truth/Ordered.java \
  com/google/common/truth/Platform.java \
  com/google/common/truth/PrimitiveBooleanArraySubject.java \
  com/google/common/truth/PrimitiveByteArraySubject.java \
  com/google/common/truth/PrimitiveCharArraySubject.java \
  com/google/common/truth/PrimitiveDoubleArraySubject.java \
  com/google/common/truth/PrimitiveFloatArraySubject.java \
  com/google/common/truth/PrimitiveIntArraySubject.java \
  com/google/common/truth/PrimitiveLongArraySubject.java \
  com/google/common/truth/PrimitiveShortArraySubject.java \
  com/google/common/truth/SimpleSubjectBuilder.java \
  com/google/common/truth/StackTraceCleaner.java \
  com/google/common/truth/StandardSubjectBuilder.java \
  com/google/common/truth/StringSubject.java \
  com/google/common/truth/Subject.java \
  com/google/common/truth/SubjectUtils.java \
  com/google/common/truth/TableSubject.java \
  com/google/common/truth/ThrowableSubject.java \
  com/google/common/truth/Truth.java \
  com/google/common/truth/TruthFailureSubject.java \
  com/google/common/truth/TruthJUnit.java \
  com/google/common/truth/package-info.java

EXTRACTED_JAVA = $(JAVA_SOURCES:%=$(JAVA_SRC_DIR)/%)
JAVA_SOURCES_MANIFEST = $(BUILD_DIR)/java_sources.mf
OBJC_SOURCES_MANIFEST = $(BUILD_DIR)/objc_sources.mf

CREATE_JAR_NAME = j2objc_truth
CREATE_JAR_SOURCES = $(EXTRACTED_JAVA)
CREATE_JAR_JAVAC_ARGS = -sourcepath $(JAVA_SRC_DIR) -classpath $(CLASSPATH)
CREATE_JAR_DEPENDENCIES = guava_java
include ../../make/create_jar.mk

CLASSPATH_LIST = \
  $(DIST_JAR_DIR)/$(JSR305_JAR) \
  $(JAVA_DEPS_JAR_DIR)/$(ERROR_PRONE_ANNOTATIONS_JAR) \
  $(JAVA_DEPS_JAR_DIR)/$(CHECKER_QUAL_JAR) \
  $(JAVA_DEPS_JAR_DIR)/$(GUAVA_JAR) \
  $(JAVA_DEPS_JAR_DIR)/$(FAILUREACCESS_JAR) \
  $(JAVA_DEPS_JAR_DIR)/$(JUNIT_JAR) \
  $(JAVA_DEPS_JAR_DIR)/$(HAMCREST_JAR)
CLASSPATH = $(subst $(eval) ,:,$(strip $(CLASSPATH_LIST)))

TRANSLATE_JAVA_FULL = $(JAVA_SOURCES:%=$(JAVA_SRC_DIR)/%)
TRANSLATE_JAVA_RELATIVE = $(JAVA_SOURCES)
TRANSLATE_ARGS = $(CREATE_JAR_JAVAC_ARGS)
include ../../make/translate.mk

ARCH_HEADERS = $(TRANSLATE_HEADERS:$(GEN_OBJC_DIR)/%=$(ARCH_INCLUDE_DIR)/%)

FAT_LIB_NAME = truth
FAT_LIB_SOURCES_RELATIVE = $(TRANSLATE_SOURCES:$(GEN_OBJC_DIR)/%=%)
FAT_LIB_SOURCE_DIRS = $(GEN_OBJC_DIR)
FAT_LIB_COMPILE = $(J2OBJCC) -fobjc-abi-version=2 -fobjc-legacy-dispatch
include ../../make/fat_lib.mk

FRAMEWORK_NAME = Truth
include $(J2OBJC_ROOT)/make/framework.mk

JAR = $(BUILD_DIR)/j2objc_truth.jar
DIST_JAR = $(DIST_JAR_DIR)/j2objc_truth.jar

default: lib

java: $(DIST_JAR)
	@:

$(DIST_JAR): $(JAR) | $(DIST_JAR_DIR)
	@install -m 0644 $< $@

lib: $(FAT_LIBS)
	@:

$(TRUTH_SRC_JAR): | java_deps_dist

$(BUILD_DIR)/.extracted: $(TRUTH_SRC_JAR) | $(BUILD_DIR)
	@mkdir -p $(JAVA_SRC_DIR)
	@unzip -q -o -d $(JAVA_SRC_DIR) $(TRUTH_SRC_JAR) $(JAVA_SOURCES)
	@touch $(EXTRACTED_JAVA)
	@touch $@

$(EXTRACTED_JAVA): $(BUILD_DIR)/.extracted
	@:

$(ARCH_INCLUDE_DIR)/%.h: $(GEN_OBJC_DIR)/%.h
	@mkdir -p $(@D)
	install -m 0644 $< $@

$(JAVA_SOURCES_MANIFEST): $(EXTRACTED_JAVA) | $(BUILD_DIR)
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
	@rm -rf $(BUILD_DIR) $(FAT_LIBS_DIST)

dist: framework

lib: $(FAT_LIBS_DIST) $(ARCH_HEADERS)
