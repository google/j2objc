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

# Makefile for building j2objc.  It's purpose is as a subproject in an Xcode
# project.
#
# Author: Tom Ball

.SUFFIXES: .java .class
.PHONY: default jar compile plugins dirs

CWD = .
SOURCE_DIR = src/main
JAVA_SOURCE_DIR = $(SOURCE_DIR)/java
TEST_DIR = target/test
PROJECT_ROOT = $(CWD)

include $(SOURCE_DIR)/make/detect_xcode.mk

CLASS_DIR = $(BUILD_DIR)/classes
LIB_DIR = $(BUILD_DIR)/lib

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

JAR_PATH = $(strip $(subst : ,:, $(strip $(foreach j, $(JARS), $(LIB_DIR)/$j:))))
CLASSPATH = $(CLASS_DIR):$(JAR_PATH)
TEST_CLASSPATH = $(TEST_DIR):$(J2OBJC_JAR):$(JAR_PATH):$(JUNIT_JAR)

MAIN_CLASS = com.google.devtools.j2objc.J2ObjC
MANIFEST = $(BUILD_DIR)/manifest.mf
MANIFEST_PATH = $(strip $(foreach j, $(JARS), $j))
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
	gen/StatementGenerator.java \
	sym/MethodSymbol.java \
	sym/Scope.java \
	sym/Symbol.java \
	sym/Symbols.java \
	sym/SymbolTableBuilder.java \
	sym/TypeSymbol.java \
	sym/VariableSymbol.java \
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
	../../j2objc/annotations/Weak.java \
	../../j2objc/annotations/WeakOuter.java \
	../../j2objc/annotations/AutoreleasePool.java

RESOURCES = J2ObjC.properties JRE.mappings
RESOURCE_DIR = $(SOURCE_DIR)/resources

MAN_DIR = $(SOURCE_DIR)/man
MAN_PAGES = $(MAN_DIR)/j2objc.1 $(MAN_DIR)/j2objcc.1

SOURCE_FILES = $(foreach f, $(CLASSES), $(JAVA_SOURCE_DIR)/$(BASE_PACKAGE)/$f)
CLASSFILES = $(foreach c, $(CLASSES:.java=.class), $(CLASS_DIR)/$(BASE_PACKAGE)/$c)
RESOURCE_FILES = $(foreach f, $(RESOURCES), $(CLASS_DIR)/$(BASE_PACKAGE)/$f)

default: jar $(BUILD_DIR)/j2objc $(BUILD_DIR)/j2objcc
	@: # suppress make's "nothing to be done" message

jar: compile $(J2OBJC_JAR)
	@:

$(J2OBJC_JAR): $(MANIFEST) $(RESOURCE_FILES)
	jar cfm $@ $(MANIFEST) -C $(CLASS_DIR) .

compile: plugins $(CLASSFILES)
	@:

$(CLASS_DIR)/$(BASE_PACKAGE)/%.class: $(JAVA_SOURCE_DIR)/$(BASE_PACKAGE)/%.java
	javac -sourcepath $(SOURCEPATH) -classpath $(CLASSPATH) -d $(CLASS_DIR) $(SOURCE_FILES)

$(MANIFEST): $(CLASSFILES)
	@echo creating $@
	@echo "Manifest-Version: 1.0" > $@
	@echo "Main-Class:" $(MAIN_CLASS) >> $@
	@echo "Class-Path:" $(MANIFEST_PATH) >> $@

$(CLASS_DIR)/%: $(RESOURCE_DIR)/%
	@cp $< $@

dirs: $(CLASS_DIR)/$(BASE_PACKAGE) $(LIB_DIR)
	@:

$(CLASS_DIR)/$(BASE_PACKAGE) $(LIB_DIR):
	mkdir -p $@

plugins: dirs
	mvn generate-resources dependency:sources

$(BUILD_DIR)/j2objc: $(SOURCE_DIR)/bin/j2objc.sh
	cp $< $@
	@chmod 755 $@

$(BUILD_DIR)/j2objcc: $(SOURCE_DIR)/bin/j2objcc.sh
	@cp $< $@
	@chmod 755 $@

$(DIST_DIR)/j2objc: $(SOURCE_DIR)/bin/j2objc.sh
	@cp $(SOURCE_DIR)/bin/j2objc.sh $(DIST_DIR)/j2objc
	@chmod 755 $(DIST_DIR)/j2objc

install-man-pages: $(MAN_PAGES)
	@mkdir -p $(DIST_DIR)/man/man1
	install -C -m 0644 $? $(DIST_DIR)/man/man1

$(DIST_DIR)/j2objcc: $(SOURCE_DIR)/bin/j2objcc.sh
	cp $(SOURCE_DIR)/bin/j2objcc.sh $(DIST_DIR)/j2objcc
	@chmod 755 $(DIST_DIR)/j2objcc

install-libs:  $(LIB_DIR)/j2objc.jar
	@mkdir -p $(DIST_LIB_DIR)
	install -C -m 0644 $(LIB_DIR)/* $(DIST_LIB_DIR)

translator_dist: jar install-libs $(DIST_DIR)/j2objc $(DIST_DIR)/j2objcc \
	  install-man-pages
	@:

jre_emul_dist: translator_dist
	@cd $(CWD)/jre_emul && make dist

junit_dist: translator_dist jre_emul_dist
	@cd $(CWD)/junit && make dist

dist: translator_dist jre_emul_dist junit_dist

j2objc_clean:
	@rm -rf $(BUILD_DIR) $(DIST_DIR) $(TEST_DIR)

clean: j2objc_clean
	@cd $(CWD)/jre_emul && make clean
	@cd $(CWD)/junit && make clean

test: default compile-tests
	java -classpath $(TEST_CLASSPATH) junit.textui.TestRunner com.google.devtools.j2objc.SmallTests

test_all: test
	@cd $(CWD)/jre_emul && make -f tests.mk

compile-tests:
	@rm -rf $(TEST_DIR)
	@mkdir -p $(TEST_DIR)
	@javac -sourcepath src/test/java -classpath $(TEST_CLASSPATH) -d $(TEST_DIR) \
		`find src/test/java -name '*.java'`
