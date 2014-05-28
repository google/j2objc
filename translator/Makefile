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
include ../make/j2objc_deps.mk
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
J2OBJC_SH = $(J2OBJC_ROOT)/scripts/j2objc.sh
J2OBJC_SH_DIST = $(DIST_DIR)/j2objc

JAVA_SOURCES = \
	DeadCodeProcessor.java \
	FileProcessor.java \
	J2ObjC.java \
	Options.java \
	Plugin.java \
	TranslationProcessor.java \
	gen/HiddenFieldDetector.java \
	gen/MetadataGenerator.java \
	gen/ObjectiveCHeaderGenerator.java \
	gen/ObjectiveCImplementationGenerator.java \
	gen/ObjectiveCSegmentedHeaderGenerator.java \
	gen/ObjectiveCSourceFileGenerator.java \
	gen/SourceBuilder.java \
	gen/SourceFileGenerator.java \
	gen/SourcePosition.java \
	gen/StatementGenerator.java \
	translate/ASTFactory.java \
	translate/AnonymousClassConverter.java \
	translate/ArrayRewriter.java \
	translate/Autoboxer.java \
	translate/ComplexExpressionExtractor.java \
	translate/CopyAllFieldsWriter.java \
	translate/DeadCodeEliminator.java \
	translate/DestructorGenerator.java \
	translate/EnhancedForRewriter.java \
	translate/Functionizer.java \
	translate/GwtConverter.java \
	translate/InitializationNormalizer.java \
	translate/InnerClassExtractor.java \
	translate/JavaToIOSMethodTranslator.java \
	translate/JavaToIOSTypeConverter.java \
	translate/NilCheckResolver.java \
	translate/OperatorRewriter.java \
	translate/OuterReferenceFixer.java \
	translate/OuterReferenceResolver.java \
	translate/Rewriter.java \
	translate/StaticVarRewriter.java \
	translate/TypeSorter.java \
	translate/UnsequencedExpressionRewriter.java \
	types/AbstractBinding.java \
	types/AbstractTypeBinding.java \
	types/BindingMapBuilder.java \
	types/BindingMapVerifier.java \
	types/GeneratedMethodBinding.java \
	types/GeneratedPackageBinding.java \
	types/GeneratedTypeBinding.java \
	types/GeneratedVariableBinding.java \
	types/HeaderImportCollector.java \
	types/ImplementationImportCollector.java \
	types/Import.java \
	types/IOSMethod.java \
	types/IOSMethodBinding.java \
	types/IOSParameter.java \
	types/IOSTypeBinding.java \
	types/JavaMethod.java \
	types/ModifiedTypeBinding.java \
	types/NodeCopier.java \
	types/PointerTypeBinding.java \
	types/RenamedTypeBinding.java \
	types/Types.java \
	util/ASTNodeException.java \
	util/ASTUtil.java \
	util/BindingUtil.java \
	util/DeadCodeMap.java \
	util/ErrorReportingASTVisitor.java \
	util/ErrorUtil.java \
	util/JdtParser.java \
	util/NameTable.java \
	util/ProGuardUsageParser.java \
	util/TimeTracker.java \
	util/UnicodeUtils.java \

RESOURCES = J2ObjC.properties JRE.mappings
RESOURCE_DIR = $(SOURCE_DIR)/resources

JAVA_SOURCES_FULL = $(JAVA_SOURCES:%=$(JAVA_SOURCE_DIR)/$(BASE_PACKAGE)/%)
RESOURCE_FILES = $(RESOURCES:%=$(CLASS_DIR)/$(BASE_PACKAGE)/%)

default: $(J2OBJC_JAR)
	@: # suppress make's "nothing to be done" message

$(J2OBJC_JAR): $(MANIFEST) $(RESOURCE_FILES) $(JAVA_SOURCES_FULL) \
  | $(BUILD_DIR) java_deps_dist annotations_dist
	@echo Building j2objc jar
	@javac -Xlint:unchecked -sourcepath $(SOURCEPATH) -classpath $(CLASSPATH) \
	    -d $(CLASS_DIR) -encoding ISO-8859-1 -source 1.6 -target 1.6 -nowarn \
	    $(JAVA_SOURCES_FULL)
	@jar cfm $@ $(MANIFEST) -C $(CLASS_DIR) .

# Format manifest classpath with each jar on a separate line, to avoid
# maximum line length of 72 bytes in UTF-8 encoding.
# http://docs.oracle.com/javase/7/docs/technotes/guides/jar/jar.html
$(MANIFEST): | $(BUILD_DIR)
	@echo "Manifest-Version: 1.0" > $@
	@echo "Main-Class:" $(MAIN_CLASS) >> $@
	@/bin/echo -n "Class-Path:" >> $@
	@for lib in $(JAR_DEPS); do echo " " $$lib; done >> $@

$(CLASS_DIR)/%: $(RESOURCE_DIR)/%
	@mkdir -p $(@D)
	@cp -f $< $@

$(CLASS_DIR) $(BUILD_DIR) $(DIST_JAR_DIR):
	@mkdir -p $@

$(J2OBJC_JAR_DIST): $(J2OBJC_JAR) | $(DIST_JAR_DIR)
	@install -m 0644 $< $@

# Re-install if the jar changes to update the timestamp. This will help other
# makefiles detect when they need to re-translate their Java files.
$(J2OBJC_SH_DIST): $(J2OBJC_SH) $(J2OBJC_JAR_DIST)
	@mkdir -p $(@D)
	@install $< $@

dist: $(J2OBJC_JAR_DIST) $(J2OBJC_SH_DIST)
	@:

clean:
	@rm -rf $(BUILD_DIR) $(J2OBJC_JAR_DIST) $(J2OBJC_SH_DIST)

test: compile-tests
	java -classpath $(TEST_CLASSPATH) junit.textui.TestRunner com.google.devtools.j2objc.SmallTests

compile-tests: $(J2OBJC_JAR)
	@rm -rf $(TEST_DIR)
	@mkdir -p $(TEST_DIR)
	@javac -Xlint:unchecked -sourcepath src/test/java -classpath $(TEST_CLASSPATH) \
	    -d $(TEST_DIR) `find src/test/java -name '*.java'`
