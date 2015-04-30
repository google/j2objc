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

SUFFIXES:

CWD = .
SOURCE_DIR = src/main
JAVA_SOURCE_DIR = $(SOURCE_DIR)/java
TEST_SOURCE_DIR = src/test
J2OBJC_ROOT = ..

include ../make/common.mk
include ../make/j2objc_deps.mk
include ../java_deps/jars.mk

CLASS_DIR = $(BUILD_DIR)/classes
TEST_DIR = $(BUILD_DIR)/test

SOURCEPATH = $(CWD):$(JAVA_SOURCE_DIR)
BASE_PACKAGE = com/google/devtools/j2objc

DIST_DEPS = $(GUAVA_JAR) $(JSR305_JAR) j2objc_annotations.jar
INTERNAL_DEPS = $(ECLIPSE_JARS)
JAR_DEPS_DIST = $(DIST_DEPS:%=$(DIST_JAR_DIR)/%) $(INTERNAL_DEPS:%=$(JAVA_DEPS_JAR_DIR)/%)
JAR_DEPS_PATH = $(subst $(eval) ,:,$(strip $(JAR_DEPS_DIST)))
JUNIT_JAR_DIST = $(DIST_JAR_DIR)/$(JUNIT_JAR)

CLASSPATH = $(CLASS_DIR):$(JAR_DEPS_PATH)
TEST_CLASSPATH = $(TEST_DIR):$(CLASS_DIR):$(JAR_DEPS_PATH):$(JUNIT_JAR_DIST)

MAIN_CLASS = com.google.devtools.j2objc.J2ObjC
MANIFEST = $(BUILD_DIR)/manifest.mf
J2OBJC_JAR = $(BUILD_DIR)/j2objc.jar
J2OBJC_JAR_DIST = $(DIST_JAR_DIR)/j2objc.jar
J2OBJC_SH = $(J2OBJC_ROOT)/scripts/j2objc.sh
J2OBJC_SH_DIST = $(DIST_DIR)/j2objc

JAVA_SOURCES = \
	AnnotationPreProcessor.java \
	BuildClosureQueue.java \
	FileProcessor.java \
	GenerationBatch.java \
	InputFilePreprocessor.java \
	J2ObjC.java \
	Options.java \
	ProcessingContext.java \
	TranslationProcessor.java \
	ast/AbstractTypeDeclaration.java \
	ast/Annotation.java \
	ast/AnnotationTypeDeclaration.java \
	ast/AnnotationTypeMemberDeclaration.java \
	ast/AnonymousClassDeclaration.java \
	ast/ArrayAccess.java \
	ast/ArrayCreation.java \
	ast/ArrayInitializer.java \
	ast/ArrayType.java \
	ast/AssertStatement.java \
	ast/Assignment.java \
	ast/Block.java \
	ast/BlockComment.java \
	ast/BodyDeclaration.java \
	ast/BooleanLiteral.java \
	ast/BreakStatement.java \
	ast/CStringLiteral.java \
	ast/CastExpression.java \
	ast/CatchClause.java \
	ast/CharacterLiteral.java \
	ast/ChildLink.java \
	ast/ChildList.java \
	ast/ClassInstanceCreation.java \
	ast/CommaExpression.java \
	ast/Comment.java \
	ast/CompilationUnit.java \
	ast/ConditionalExpression.java \
	ast/ConstructorInvocation.java \
	ast/ContinueStatement.java \
	ast/DebugASTPrinter.java \
	ast/DoStatement.java \
	ast/EmptyStatement.java \
	ast/EnhancedForStatement.java \
	ast/EnumConstantDeclaration.java \
	ast/EnumDeclaration.java \
	ast/Expression.java \
	ast/ExpressionStatement.java \
	ast/FieldAccess.java \
	ast/FieldDeclaration.java \
	ast/ForStatement.java \
	ast/FunctionDeclaration.java \
	ast/FunctionInvocation.java \
	ast/IfStatement.java \
	ast/InfixExpression.java \
	ast/Initializer.java \
	ast/InstanceofExpression.java \
	ast/Javadoc.java \
	ast/LabeledStatement.java \
	ast/LineComment.java \
	ast/MarkerAnnotation.java \
	ast/MemberValuePair.java \
	ast/MethodDeclaration.java \
	ast/MethodInvocation.java \
	ast/Name.java \
	ast/NativeDeclaration.java \
	ast/NativeExpression.java \
	ast/NativeStatement.java \
	ast/NormalAnnotation.java \
	ast/NullLiteral.java \
	ast/NumberLiteral.java \
	ast/PackageDeclaration.java \
	ast/ParameterizedType.java \
	ast/ParenthesizedExpression.java \
	ast/PostfixExpression.java \
	ast/PrefixExpression.java \
	ast/PrimitiveType.java \
	ast/QualifiedName.java \
	ast/QualifiedType.java \
	ast/ReturnStatement.java \
	ast/SimpleName.java \
	ast/SimpleType.java \
	ast/SingleMemberAnnotation.java \
	ast/SingleVariableDeclaration.java \
	ast/Statement.java \
	ast/StringLiteral.java \
	ast/SuperConstructorInvocation.java \
	ast/SuperFieldAccess.java \
	ast/SuperMethodInvocation.java \
	ast/SwitchCase.java \
	ast/SwitchStatement.java \
	ast/SynchronizedStatement.java \
	ast/TagElement.java \
	ast/TextElement.java \
	ast/ThisExpression.java \
	ast/ThrowStatement.java \
	ast/TreeConverter.java \
	ast/TreeNode.java \
	ast/TreeUtil.java \
	ast/TreeVisitor.java \
	ast/TryStatement.java \
	ast/Type.java \
	ast/TypeDeclaration.java \
	ast/TypeDeclarationStatement.java \
	ast/TypeLiteral.java \
	ast/UnionType.java \
	ast/VariableDeclaration.java \
	ast/VariableDeclarationExpression.java \
	ast/VariableDeclarationFragment.java \
	ast/VariableDeclarationStatement.java \
	ast/WhileStatement.java \
	file/InputFile.java \
	file/JarredInputFile.java \
	file/RegularInputFile.java \
	gen/AbstractSourceGenerator.java \
	gen/GenerationUnit.java \
	gen/JavadocGenerator.java \
	gen/LiteralGenerator.java \
	gen/MetadataGenerator.java \
	gen/ObjectiveCHeaderGenerator.java \
	gen/ObjectiveCImplementationGenerator.java \
	gen/ObjectiveCSegmentedHeaderGenerator.java \
	gen/ObjectiveCSourceFileGenerator.java \
	gen/RuntimeAnnotationGenerator.java \
	gen/SignatureGenerator.java \
	gen/SourceBuilder.java \
	gen/StatementGenerator.java \
	gen/TypeDeclarationGenerator.java \
	gen/TypeGenerator.java \
	gen/TypeImplementationGenerator.java \
	gen/TypePrivateDeclarationGenerator.java \
	translate/AbstractMethodRewriter.java \
	translate/AnonymousClassConverter.java \
	translate/ArrayRewriter.java \
	translate/Autoboxer.java \
	translate/CastResolver.java \
	translate/ComplexExpressionExtractor.java \
	translate/ConstantBranchPruner.java \
	translate/DeadCodeEliminator.java \
	translate/DestructorGenerator.java \
	translate/EnhancedForRewriter.java \
	translate/EnumRewriter.java \
	translate/Functionizer.java \
	translate/GwtConverter.java \
	translate/InitializationNormalizer.java \
	translate/InnerClassExtractor.java \
	translate/JavaCloneWriter.java \
	translate/JavaToIOSMethodTranslator.java \
	translate/NilCheckResolver.java \
	translate/OcniExtractor.java \
	translate/OperatorRewriter.java \
	translate/OuterReferenceFixer.java \
	translate/OuterReferenceResolver.java \
	translate/PrivateDeclarationResolver.java \
	translate/Rewriter.java \
	translate/StaticVarRewriter.java \
	translate/SuperMethodInvocationRewriter.java \
	translate/UnsequencedExpressionRewriter.java \
	translate/VarargsRewriter.java \
	translate/VariableRenamer.java \
	types/AbstractBinding.java \
	types/AbstractTypeBinding.java \
	types/GeneratedMethodBinding.java \
	types/GeneratedPackageBinding.java \
	types/GeneratedTypeBinding.java \
	types/GeneratedVariableBinding.java \
	types/HeaderImportCollector.java \
	types/ImplementationImportCollector.java \
	types/Import.java \
	types/IOSMethodBinding.java \
	types/IOSTypeBinding.java \
	types/NullTypeBinding.java \
	types/PointerTypeBinding.java \
	types/Types.java \
	util/BindingUtil.java \
	util/DeadCodeMap.java \
	util/ErrorUtil.java \
	util/FileUtil.java \
	util/JdtParser.java \
	util/NameTable.java \
	util/ProGuardUsageParser.java \
	util/TimeTracker.java \
	util/TranslationUtil.java \
	util/UnicodeUtils.java \

RESOURCES = J2ObjC.properties JRE.mappings
RESOURCE_DIR = $(SOURCE_DIR)/resources
TEST_RESOURCES_DIR = $(TEST_SOURCE_DIR)/resources

JAVA_SOURCES_FULL = $(JAVA_SOURCES:%=$(JAVA_SOURCE_DIR)/$(BASE_PACKAGE)/%)
RESOURCE_FILES = $(RESOURCES:%=$(CLASS_DIR)/$(BASE_PACKAGE)/%)

TEST_RESOURCES = \
  com/google/devtools/j2objc/mappings.j2objc \
  com/google/devtools/j2objc/testMappings.j2objc \
  com/google/devtools/j2objc/annotations/Example.java \
  com/google/devtools/j2objc/annotations/Processor.jar \
  com/google/devtools/j2objc/util/example.jar \
  com/google/devtools/j2objc/util/Example.java \
  com/google/devtools/j2objc/util/package-info.java
TEST_RESOURCE_FILES = $(TEST_RESOURCES:%=$(TEST_DIR)/%)

# Files in dependent jars that aren't needed in combined jar.
UNUSED_JAR_CONTENTS = \
  $(CLASS_DIR)/[A-Z]* \
  $(CLASS_DIR)/[ahps]* \
  $(CLASS_DIR)/about_files \
  $(CLASS_DIR)/ant_tasks \
  $(CLASS_DIR)/compiler* \
  $(CLASS_DIR)/hook* \
  $(CLASS_DIR)/jdt*

default: $(J2OBJC_JAR)
	@: # suppress make's "nothing to be done" message

$(J2OBJC_JAR): $(MANIFEST) $(RESOURCE_FILES) $(JAVA_SOURCES_FULL) \
  | $(BUILD_DIR) java_deps_dist annotations_dist
	@echo building j2objc jar
	@javac -Xlint:unchecked -sourcepath $(SOURCEPATH) -classpath $(CLASSPATH) \
	    -d $(CLASS_DIR) -encoding UTF-8 -source 1.6 -target 1.6 -nowarn \
	    $(JAVA_SOURCES_FULL)
	@for lib in $(JAR_DEPS_DIST); do unzip -oq $$lib -d $(CLASS_DIR); done
	@rm -rf $(UNUSED_JAR_CONTENTS)
	@jar cfm $@-combined $(MANIFEST) -C $(CLASS_DIR) .
	@java -jar $(JAVA_DEPS_JAR_DIR)/$(JARJAR) process jarjar.rules $@-combined $@

# Format manifest classpath with each jar on a separate line, to avoid
# maximum line length of 72 bytes in UTF-8 encoding.
# http://docs.oracle.com/javase/7/docs/technotes/guides/jar/jar.html
$(MANIFEST): | $(BUILD_DIR)
	@echo "Manifest-Version: 1.0" > $@
	@echo "Main-Class:" $(MAIN_CLASS) >> $@

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

test: compile-tests copy-test-resources
	java -classpath $(TEST_CLASSPATH) -ea junit.textui.TestRunner com.google.devtools.j2objc.SmallTests

compile-tests: $(J2OBJC_JAR)
	@mkdir -p $(TEST_DIR)
	@javac -Xlint:unchecked -sourcepath src/test/java \
	    -classpath $(TEST_CLASSPATH) -encoding UTF-8 -d $(TEST_DIR) \
	    `find src/test/java -name '*.java'`

copy-test-resources: $(TEST_RESOURCE_FILES)

$(TEST_DIR)/%: $(TEST_RESOURCES_DIR)/%
	@mkdir -p $(@D)
	@cp $< $@
