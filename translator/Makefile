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

# Builds the j2objc translator as a .jar file.
#
# Author: Tom Ball, Keith Stanger

CWD = .
SOURCE_DIR = src/main
JAVA_SOURCE_DIR = $(SOURCE_DIR)/java
J2OBJC_ROOT = ..

include ../make/common.mk
include ../java_deps/jars.mk

CLASS_DIR = $(BUILD_DIR)/classes
TEST_DIR = $(BUILD_DIR)/test

SOURCEPATH = $(CWD):$(JAVA_SOURCE_DIR)
BASE_PACKAGE = com/google/devtools/j2objc

JAR_DEPS = $(ECLIPSE_JARS) $(GUAVA_JAR) $(JSR305_JAR) j2objc_annotations.jar
JAR_DEPS_DIST = $(JAR_DEPS:%=$(DIST_JAR_DIR)/%)
JAR_DEPS_PATH = $(subst $(eval) ,:,$(strip $(JAR_DEPS_DIST)))
JUNIT_JAR_DIST = $(DIST_JAR_DIR)/$(JUNIT_JAR)

CLASSPATH = $(CLASS_DIR):$(JAR_DEPS_PATH)
TEST_CLASSPATH = $(TEST_DIR):$(J2OBJC_JAR):$(JAR_DEPS_PATH):$(JUNIT_JAR_DIST)

MAIN_CLASS = com.google.devtools.j2objc.J2ObjC
MANIFEST = $(BUILD_DIR)/manifest.mf
J2OBJC_JAR = $(BUILD_DIR)/j2objc.jar
J2OBJC_JAR_DIST = $(DIST_JAR_DIR)/j2objc.jar

JAVA_SOURCES = \
	J2ObjC.java \
	Options.java \
	Plugin.java \
	gen/HiddenFieldDetector.java \
	gen/ObjectiveCHeaderGenerator.java \
	gen/ObjectiveCImplementationGenerator.java \
	gen/ObjectiveCSourceFileGenerator.java \
	gen/SourceFileGenerator.java \
	gen/SourcePosition.java \
	gen/StatementGenerator.java \
	translate/ASTFactory.java \
	translate/AnonymousClassConverter.java \
	translate/Autoboxer.java \
	translate/DeadCodeEliminator.java \
	translate/DestructorGenerator.java \
	translate/GwtConverter.java \
	translate/InitializationNormalizer.java \
	translate/InnerClassExtractor.java \
	translate/JavaToIOSMethodTranslator.java \
	translate/JavaToIOSTypeConverter.java \
	translate/OuterReferenceFixer.java \
	translate/OuterReferenceResolver.java \
	translate/Rewriter.java \
	types/BindingMapBuilder.java \
	types/BindingMapVerifier.java \
	types/GeneratedMethodBinding.java \
	types/GeneratedPackageBinding.java \
	types/GeneratedTypeBinding.java \
	types/GeneratedVariableBinding.java \
	types/HeaderImportCollector.java \
	types/ImplementationImportCollector.java \
	types/ImportCollector.java \
	types/IOSArrayTypeBinding.java \
	types/IOSMethod.java \
	types/IOSMethodBinding.java \
	types/IOSParameter.java \
	types/IOSTypeBinding.java \
	types/IOSVariableBinding.java \
	types/JavaMethod.java \
	types/ModifiedTypeBinding.java \
	types/NodeCopier.java \
	types/RenamedTypeBinding.java \
	types/Types.java \
	util/ASTNodeException.java \
	util/ASTUtil.java \
	util/DeadCodeMap.java \
	util/ErrorReportingASTVisitor.java \
	util/NameTable.java \
	util/ProGuardUsageParser.java \
	util/UnicodeUtils.java \

RESOURCES = J2ObjC.properties JRE.mappings
RESOURCE_DIR = $(SOURCE_DIR)/resources

JAVA_SOURCES_FULL = $(JAVA_SOURCES:%=$(JAVA_SOURCE_DIR)/$(BASE_PACKAGE)/%)
RESOURCE_FILES = $(RESOURCES:%=$(CLASS_DIR)/$(BASE_PACKAGE)/%)

default: $(J2OBJC_JAR)
	@: # suppress make's "nothing to be done" message

# Only build dependencies when called directly from command-line.
deps:
	@if [ "$(CONFIGURATION_BUILD_DIR)x$(MAKELEVEL)" == "x0" ]; then \
	  cd ../annotations && make dist; \
	  cd ../java_deps && make dist; \
	fi

$(J2OBJC_JAR): $(MANIFEST) $(RESOURCE_FILES) $(JAVA_SOURCES_FULL) | $(BUILD_DIR)
	javac -Xlint:unchecked -sourcepath $(SOURCEPATH) -classpath $(CLASSPATH) \
	    -d $(CLASS_DIR) $(JAVA_SOURCES_FULL)
	jar cfm $@ $(MANIFEST) -C $(CLASS_DIR) .

$(MANIFEST): | $(BUILD_DIR)
	@echo creating $@
	@echo "Manifest-Version: 1.0" > $@
	@echo "Main-Class:" $(MAIN_CLASS) >> $@
	@echo "Class-Path:" $(JAR_DEPS) >> $@

$(CLASS_DIR)/%: $(RESOURCE_DIR)/%
	@mkdir -p $(@D)
	@cp -f $< $@

$(CLASS_DIR) $(BUILD_DIR) $(DIST_JAR_DIR):
	mkdir -p $@

$(J2OBJC_JAR_DIST): $(J2OBJC_JAR) | $(DIST_JAR_DIR)
	install -m 0644 $< $@

dist: deps $(J2OBJC_JAR_DIST)
	@:

clean:
	@rm -rf $(BUILD_DIR) $(J2OBJC_JAR_DIST)

test: deps $(J2OBJC_JAR) compile-tests
	java -classpath $(TEST_CLASSPATH) junit.textui.TestRunner com.google.devtools.j2objc.SmallTests

compile-tests:
	@rm -rf $(TEST_DIR)
	@mkdir -p $(TEST_DIR)
	@javac -sourcepath src/test/java -classpath $(TEST_CLASSPATH) -d $(TEST_DIR) \
		`find src/test/java -name '*.java'`
