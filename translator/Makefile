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
PROJECT_ROOT = ..

include ../make/detect_xcode.mk

CLASS_DIR = $(BUILD_DIR)/classes
LIB_DIR = $(BUILD_DIR)/lib
TEST_DIR = $(BUILD_DIR)/test

SOURCEPATH = $(CWD):$(JAVA_SOURCE_DIR)
BASE_PACKAGE = com/google/devtools/j2objc

JARS = \
  guava-13.0.jar \
  jsr305-2.0.1.jar \
  org.eclipse.core.contenttype-3.4.100.v20100505-1235.jar \
  org.eclipse.core.jobs-3.5.0.v20100515.jar \
  org.eclipse.core.resources-3.6.0.v20100526-0737.jar \
  org.eclipse.core.runtime-3.6.0.v20100505.jar \
  org.eclipse.equinox.common-3.6.0.v20100503.jar \
  org.eclipse.equinox.preferences-3.3.0.v20100503.jar \
  org.eclipse.jdt.core-3.8.1.v20120531-0637.jar \
  org.eclipse.osgi-3.6.0.v20100517.jar \
  org.eclipse.text-3.5.100.v20110505-0800.jar
JUNIT_JAR = $(LIB_DIR)/junit-4.10.jar

ANNOTATIONS_JAR = $(DIST_JAR_DIR)/j2objc_annotations.jar

JAR_PATH = $(strip $(subst : ,:, $(strip $(foreach j, $(JARS), $(LIB_DIR)/$j:))))
CLASSPATH = $(CLASS_DIR):$(ANNOTATIONS_JAR):$(JAR_PATH)
TEST_CLASSPATH = $(TEST_DIR):$(J2OBJC_JAR):$(ANNOTATIONS_JAR):$(JAR_PATH):$(JUNIT_JAR)

MAIN_CLASS = com.google.devtools.j2objc.J2ObjC
MANIFEST = $(BUILD_DIR)/manifest.mf
MANIFEST_PATH = $(strip $(foreach j, $(JARS), $j)) j2objc_annotations.jar
J2OBJC_JAR = $(LIB_DIR)/j2objc.jar

CLASSES = \
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
	sym/MethodSymbol.java \
	sym/Scope.java \
	sym/Symbol.java \
	sym/Symbols.java \
	sym/SymbolTableBuilder.java \
	sym/TypeSymbol.java \
	sym/VariableSymbol.java \
	translate/ASTFactory.java \
	translate/AnonymousClassConverter.java \
	translate/Autoboxer.java \
	translate/ClassConverter.java \
	translate/DeadCodeEliminator.java \
	translate/DestructorGenerator.java \
	translate/GwtConverter.java \
	translate/InitializationNormalizer.java \
	translate/InnerClassExtractor.java \
	translate/JavaToIOSMethodTranslator.java \
	translate/JavaToIOSTypeConverter.java \
	translate/ReferenceDescription.java \
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
	util/DeadCodeMap.java \
	util/ErrorReportingASTVisitor.java \
	util/NameTable.java \
	util/ProGuardUsageParser.java \
	util/TypeTrackingVisitor.java \
	util/UnicodeUtils.java \

RESOURCES = J2ObjC.properties JRE.mappings
RESOURCE_DIR = $(SOURCE_DIR)/resources

SOURCE_FILES = $(foreach f, $(CLASSES), $(JAVA_SOURCE_DIR)/$(BASE_PACKAGE)/$f)
CLASSFILES = $(foreach c, $(CLASSES:.java=.class), $(CLASS_DIR)/$(BASE_PACKAGE)/$c)
RESOURCE_FILES = $(foreach f, $(RESOURCES), $(CLASS_DIR)/$(BASE_PACKAGE)/$f)

default: jar
	@: # suppress make's "nothing to be done" message

# Only build dependencies when called directly from command-line.
deps:
	@if [ "$(CONFIGURATION_BUILD_DIR)x$(MAKELEVEL)" == "x0" ]; then \
	  echo BUILDING DEPS; \
	  cd ../annotations && make dist; \
	fi

jar: compile $(J2OBJC_JAR)
	@:

$(J2OBJC_JAR): $(MANIFEST) $(RESOURCE_FILES)
	jar cfm $@ $(MANIFEST) -C $(CLASS_DIR) .

compile: plugins $(CLASSFILES)
	@:

$(CLASS_DIR)/$(BASE_PACKAGE)/%.class: $(JAVA_SOURCE_DIR)/$(BASE_PACKAGE)/%.java
	javac -Xlint:unchecked -sourcepath $(SOURCEPATH) -classpath $(CLASSPATH) -d $(CLASS_DIR) $(SOURCE_FILES)

$(MANIFEST): $(CLASSFILES)
	@echo creating $@
	@echo "Manifest-Version: 1.0" > $@
	@echo "Main-Class:" $(MAIN_CLASS) >> $@
	@echo "Class-Path:" $(MANIFEST_PATH) >> $@

$(CLASS_DIR)/%: $(RESOURCE_DIR)/%
	@cp -f $< $@

dirs: $(CLASS_DIR)/$(BASE_PACKAGE) $(LIB_DIR)
	@:

$(CLASS_DIR)/$(BASE_PACKAGE) $(LIB_DIR):
	mkdir -p $@

plugins: dirs
	mvn generate-resources dependency:sources

install-libs: $(LIB_DIR)/j2objc.jar
	@mkdir -p $(DIST_LIB_DIR)
	install -C -m 0644 $(LIB_DIR)/* $(JUNIT_JAR) $(DIST_LIB_DIR)

dist: deps jar install-libs
	@:

# TODO(user): remove files from dist.
clean:
	@rm -rf $(BUILD_DIR)

test: deps jar compile-tests
	java -classpath $(TEST_CLASSPATH) junit.textui.TestRunner com.google.devtools.j2objc.SmallTests

compile-tests:
	@rm -rf $(TEST_DIR)
	@mkdir -p $(TEST_DIR)
	@javac -sourcepath src/test/java -classpath $(TEST_CLASSPATH) -d $(TEST_DIR) \
		`find src/test/java -name '*.java'`
