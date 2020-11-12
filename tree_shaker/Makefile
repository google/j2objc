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

# Makefile for building tree_shaker, a tool for finding unused code in a
# Java program. Mainly useful in minimizing the size of a project.
#
# Author: Keith Stanger, Priyank Malvania

SOURCE_DIR = src/main
JAVA_SOURCE_DIR = $(SOURCE_DIR)/java
RESOURCE_DIR = $(SOURCE_DIR)/resources
J2OBJC_ROOT = ..
TRANSLATOR_SOURCE_DIR = $(J2OBJC_ROOT)/translator/$(JAVA_SOURCE_DIR)

include ../make/common.mk
include ../make/j2objc_deps.mk
include ../java_deps/jars.mk

BUILD_DIR = build_result
CLASS_DIR = $(BUILD_DIR)/classes
TEST_CLASS_DIR = $(BUILD_DIR)/test
TRANSLATOR_CLASS_DIR = $(J2OBJC_ROOT)/translator/$(CLASS_DIR)
TRANSLATOR_TEST_DIR = $(J2OBJC_ROOT)/translator/$(TEST_CLASS_DIR)

JAVA_SOURCES = \
    com/google/devtools/treeshaker/TreeShaker.java \
    com/google/devtools/treeshaker/ElementReferenceMapper.java \
    com/google/devtools/treeshaker/UnusedCodeTracker.java \
    com/google/devtools/treeshaker/Options.java \

RESOURCES = \
    com/google/devtools/treeshaker/TreeShaker.properties \
    com/google/devtools/j2objc/J2ObjC.properties \
    com/google/devtools/j2objc/JRE.mappings

DIST_DEPS = $(JSR305_JAR) j2objc_annotations.jar
INTERNAL_DEPS = $(GUAVA_JAR) $(PROCYON_JARS) $(SCENELIB_JAR)
ifdef JAVA_8
INTERNAL_DEPS += $(JAVAC_JAR)
endif
JAR_DEPS_DIST = $(DIST_DEPS:%=$(DIST_JAR_DIR)/%) $(INTERNAL_DEPS:%=$(JAVA_DEPS_JAR_DIR)/%)
JAR_DEPS_PATH = $(subst $(eval) ,:,$(strip $(JAR_DEPS_DIST)))
JUNIT_JAR_DIST = $(DIST_JAR_DIR)/$(JUNIT_JAR)

MAIN_CLASS = com.google.devtools.treeshaker.TreeShaker
MANIFEST = $(BUILD_DIR)/manifest.mf
JAR = $(BUILD_DIR)/tree_shaker.jar
JAR_DIST = $(DIST_JAR_DIR)/tree_shaker.jar

JAVA_SOURCES_FULL = $(JAVA_SOURCES:%=$(JAVA_SOURCE_DIR)/%)
RESOURCE_FILES = $(RESOURCES:%=$(CLASS_DIR)/%)

TEST_PATHS = \
  $(CLASS_DIR) $(JAR_DEPS_PATH) $(TEST_CLASS_DIR) $(JUNIT_JAR_DIST) \
  $(TRANSLATOR_CLASS_DIR) $(TRANSLATOR_TEST_DIR)
TEST_CLASSPATH = $(subst $(space),:,$(TEST_PATHS))

ALL_LIBS = $(JAR) $(ECLIPSE_LIBS)

# Files in dependent jars that aren't needed in combined jar.
UNUSED_JAR_CONTENTS = \
  $(CLASS_DIR)/[A-Z]* \
  $(CLASS_DIR)/[ahps]* \
  $(CLASS_DIR)/about_files \
  $(CLASS_DIR)/ant_tasks \
  $(CLASS_DIR)/compiler* \
  $(CLASS_DIR)/hook* \
  $(CLASS_DIR)/jdt*

ifeq ($(shell uname), Linux)
PRUNE_CMD = bash -O globasciiranges -c "rm -rf $(UNUSED_JAR_CONTENTS)"
else
PRUNE_CMD = rm -rf $(UNUSED_JAR_CONTENTS)
endif

default: $(JAR)
	@:

clean:
	@rm -rf $(BUILD_DIR) $(JAR_DIST) $(DIST_DIR)/tree_shaker

$(JAR): $(MANIFEST) $(RESOURCE_FILES) $(JAVA_SOURCES_FULL) \
  | $(CLASS_DIR) java_deps_dist annotations_dist translator
	@echo building tree_shaker jar
	@$(JAVAC) -Xlint:unchecked -sourcepath $(JAVA_SOURCE_DIR):$(TRANSLATOR_SOURCE_DIR) \
	    -classpath $(JAR_DEPS_PATH) -encoding UTF-8 \
	    -d $(CLASS_DIR) -source 1.8 -target 1.8 -nowarn $(JAVA_SOURCES:%=$(JAVA_SOURCE_DIR)/%)
	@for lib in $(JAR_DEPS_DIST); do unzip -oq $$lib -d $(CLASS_DIR); done
	@$(PRUNE_CMD)
	@jar cfm $@-combined $(MANIFEST) -C $(CLASS_DIR) .
	@$(JAVA) -jar $(JAVA_DEPS_JAR_DIR)/$(JARJAR_JAR) process jarjar.rules $@-combined $@

# Format manifest classpath with each jar on a separate line, to avoid
# maximum line length of 72 bytes in UTF-8 encoding.
# http://docs.oracle.com/javase/7/docs/technotes/guides/jar/jar.html
$(MANIFEST): | $(BUILD_DIR)
	@echo creating $@
	@echo "Manifest-Version: 1.0" > $@
	@echo "Main-Class:" $(MAIN_CLASS) >> $@
	@echo "Version:" $(J2OBJC_VERSION) >> $@

$(CLASS_DIR)/%: $(RESOURCE_DIR)/%
	@mkdir -p $(@D)
	@cp -f $< $@

$(CLASS_DIR)/%: $(J2OBJC_ROOT)/translator/$(RESOURCE_DIR)/%
	@mkdir -p $(@D)
	cp -f $< $@

DIRS = $(BUILD_DIR) $(CLASS_DIR) $(TEST_CLASS_DIR) $(DIST_DIR) $(DIST_JAR_DIR)

$(sort $(DIRS)):
	@mkdir -p $@

$(DIST_DIR)/tree_shaker: $(SOURCE_DIR)/bin/tree_shaker.sh | $(DIST_DIR)
	install $< $@

$(JAR_DIST): $(JAR) | $(DIST_JAR_DIR)
	install -m 0644 $< $@

dist: $(JAR_DIST) $(DIST_DIR)/tree_shaker
	@:

test: compile-tests
	$(JAVA) -classpath $(TEST_CLASSPATH) junit.textui.TestRunner \
	    com.google.devtools.treeshaker.ElementReferenceMapperTest

compile-tests: $(JAR) | $(TEST_CLASS_DIR)
	$(JAVAC) -encoding UTF-8 -sourcepath src/test/java \
	    -classpath $(TEST_CLASSPATH) -d $(TEST_CLASS_DIR) \
	    src/test/java/com/google/devtools/treeshaker/ElementReferenceMapperTest.java
