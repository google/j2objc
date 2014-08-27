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

# Makefile for building cycle_finder, a tool for finding reference cycles in a
# Java program. Mainly useful when translating to a language without garbage
# collection.
#
# Author: Keith Stanger

SUFFIXES:

SOURCE_DIR = src/main
JAVA_SOURCE_DIR = $(SOURCE_DIR)/java
RESOURCE_DIR = $(SOURCE_DIR)/resources
J2OBJC_ROOT = ..

include ../make/common.mk
include ../make/j2objc_deps.mk
include ../java_deps/jars.mk

BUILD_DIR = build_result
CLASS_DIR = $(BUILD_DIR)/classes
TEST_CLASS_DIR = $(BUILD_DIR)/test

JAVA_SOURCES = \
    com/google/devtools/cyclefinder/CycleFinder.java \
    com/google/devtools/cyclefinder/Edge.java \
    com/google/devtools/cyclefinder/NameList.java \
    com/google/devtools/cyclefinder/Options.java \
    com/google/devtools/cyclefinder/ReferenceGraph.java \
    com/google/devtools/cyclefinder/Tarjans.java \
    com/google/devtools/cyclefinder/TypeCollector.java

RESOURCES = \
    com/google/devtools/cyclefinder/CycleFinder.properties

JAR_DEPS = $(ECLIPSE_JARS) $(GUAVA_JAR) j2objc.jar j2objc_annotations.jar
JAR_DEPS_DIST = $(JAR_DEPS:%=$(DIST_JAR_DIR)/%)
JAR_DEPS_PATH = $(subst $(eval) ,:,$(strip $(JAR_DEPS_DIST)))
JUNIT_JAR_DIST = $(DIST_JAR_DIR)/$(JUNIT_JAR)

MAIN_CLASS = com.google.devtools.cyclefinder.CycleFinder
MANIFEST = $(BUILD_DIR)/manifest.mf
JAR = $(BUILD_DIR)/cycle_finder.jar
JAR_DIST = $(DIST_JAR_DIR)/cycle_finder.jar

JAVA_SOURCES_FULL = $(JAVA_SOURCES:%=$(JAVA_SOURCE_DIR)/%)
RESOURCE_FILES = $(RESOURCES:%=$(CLASS_DIR)/%)

TEST_CLASSPATH = $(JAR):$(JUNIT_JAR_DIST):$(JAR_DEPS_PATH):$(TEST_CLASS_DIR)

ALL_LIBS = $(JAR) $(ECLIPSE_LIBS)

default: $(JAR)
	@:

clean:
	@rm -rf $(BUILD_DIR) $(JAR_DIST) $(DIST_DIR)/cycle_finder

$(JAR): $(MANIFEST) $(RESOURCE_FILES) $(JAVA_SOURCES_FULL) \
  | $(CLASS_DIR) java_deps_dist annotations_dist translator
	javac -Xlint:unchecked -sourcepath $(JAVA_SOURCE_DIR) -classpath $(JAR_DEPS_PATH) \
	    -d $(CLASS_DIR) -source 1.6 -target 1.6 -nowarn $(JAVA_SOURCES:%=$(JAVA_SOURCE_DIR)/%)
	jar cfm $@ $(MANIFEST) -C $(CLASS_DIR) .

# Format manifest classpath with each jar on a separate line, to avoid
# maximum line length of 72 bytes in UTF-8 encoding.
# http://docs.oracle.com/javase/7/docs/technotes/guides/jar/jar.html
$(MANIFEST): | $(BUILD_DIR)
	@echo creating $@
	@echo "Manifest-Version: 1.0" > $@
	@echo "Main-Class:" $(MAIN_CLASS) >> $@
	@/bin/echo -n "Class-Path:" >> $@
	@for lib in $(JAR_DEPS); do echo " " $$lib; done >> $@

$(CLASS_DIR)/%: $(RESOURCE_DIR)/%
	@mkdir -p $(@D)
	@cp -f $< $@

DIRS = $(BUILD_DIR) $(CLASS_DIR) $(TEST_CLASS_DIR) $(DIST_DIR) $(DIST_JAR_DIR)

$(sort $(DIRS)):
	@mkdir -p $@

$(DIST_DIR)/cycle_finder: $(SOURCE_DIR)/bin/cycle_finder.sh | $(DIST_DIR)
	install $< $@

$(JAR_DIST): $(JAR) | $(DIST_JAR_DIR)
	install -m 0644 $< $@

dist: $(JAR_DIST) $(DIST_DIR)/cycle_finder
	@:

test: compile-tests
	java -classpath $(TEST_CLASSPATH) junit.textui.TestRunner \
	    com.google.devtools.cyclefinder.CycleFinderTest

compile-tests: $(JAR) | $(TEST_CLASS_DIR)
	javac -sourcepath src/test/java -classpath $(TEST_CLASSPATH) -d $(TEST_CLASS_DIR) \
	    src/test/java/com/google/devtools/cyclefinder/CycleFinderTest.java
