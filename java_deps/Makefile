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

# Defines the names of all dependent .jar files used by j2objc.
#
# Author: Keith Stanger

.SECONDARY:

J2OBJC_ROOT = ..

include ../make/common.mk
include jars.mk

ALL_JARS = $(ECLIPSE_JARS) $(GUAVA_JAR) $(JSR305_JAR) $(JUNIT_JAR) $(ICU4J_JAR)
SOURCES_JARS = $(JUNIT_SOURCE_JAR) $(HAMCREST_SOURCE_JAR) $(ICU4J_SOURCE_JAR)
# MOE:begin_strip
SOURCES_JARS := $(SOURCES_JARS) $(JSR305_SOURCE_JAR)
# MOE:end_strip
DIST_JARS = $(ALL_JARS:%=$(DIST_JAR_DIR)/%)
BUILD_DIR_JARS = $(ALL_JARS:%=$(BUILD_DIR)/%) $(SOURCES_JARS:%=$(BUILD_DIR)/%)

# MOE:begin_strip
GOOGLE3 = ../../../..
ECLIPSE_HOME = $(GOOGLE3)/third_party/java/eclipse/v3_7_1/plugins
GUAVA_HOME = $(GOOGLE3)/third_party/java/android_libs/guava_jdk5
JSR305_HOME = $(GOOGLE3)/third_party/java/jsr305_annotations/v0_r47
JUNIT_HOME = $(GOOGLE3)/third_party/java/junit/v4_10
HAMCREST_HOME = $(GOOGLE3)/third_party/java/hamcrest/v1_1
ICU4J_HOME = $(GOOGLE3)/third_party/java/icu4j/v51_1

$(BUILD_DIR)/%.jar: $(ECLIPSE_HOME)/%.jar | $(BUILD_DIR)
	@cp -f $^ $@

$(BUILD_DIR)/$(GUAVA_JAR): $(GUAVA_HOME)/$(GUAVA_JAR) | $(BUILD_DIR)
	@cp -f $^ $@

$(BUILD_DIR)/%.jar: $(JSR305_HOME)/%.jar| $(BUILD_DIR)
	@cp -f $^ $@

$(BUILD_DIR)/%.jar: $(JUNIT_HOME)/%.jar | $(BUILD_DIR)
	@cp -f $^ $@

$(BUILD_DIR)/%.jar: $(HAMCREST_HOME)/%.jar | $(BUILD_DIR)
	@cp -f $^ $@

$(BUILD_DIR)/%.jar: $(ICU4J_HOME)/%.jar | $(BUILD_DIR)
	@cp -f $^ $@
# MOE:end_strip

# MOE:insert ifndef M2_HOME
# MOE:insert M2_HOME = $(shell echo $$HOME)/.m2
# MOE:insert endif
# MOE:insert JUNIT_DIR = $(M2_HOME)/repository/junit/junit/4.10
# MOE:insert JSR305_DIR = $(M2_HOME)/repository/com/google/code/findbugs/jsr305/2.0.1
# MOE:insert HAMCREST_DIR = $(M2_HOME)/repository/org/hamcrest/hamcrest-core/1.1
# MOE:insert ICU4J_DIR = $(M2_HOME)/repository/com/ibm/icu/icu4j/51.1

# MOE:insert MAVEN_RESULTS = $(ALL_JARS:%=$(BUILD_DIR)/%) $(JUNIT_DIR)/$(JUNIT_SOURCE_JAR)
# MOE:insert $(MAVEN_RESULTS): $(BUILD_DIR)/.maven_generate

# MOE:insert $(BUILD_DIR)/.maven_generate: | $(BUILD_DIR)
# MOE:insert	mvn generate-resources dependency:sources
# MOE:insert	@touch $@

# MOE:insert $(BUILD_DIR)/$(JUNIT_SOURCE_JAR): $(JUNIT_DIR)/$(JUNIT_SOURCE_JAR) | $(BUILD_DIR)
# MOE:insert	cp -f $^ $@

# MOE:insert $(BUILD_DIR)/$(HAMCREST_SOURCE_JAR): $(HAMCREST_DIR)/$(HAMCREST_SOURCE_JAR) | $(BUILD_DIR)
# MOE:insert	cp -f $^ $@

# MOE:insert $(BUILD_DIR)/$(ICU4J_SOURCE_JAR): $(ICU4J_DIR)/$(ICU4J_SOURCE_JAR) | $(BUILD_DIR)
# MOE:insert	cp -f $^ $@

$(BUILD_DIR) $(DIST_JAR_DIR):
	@mkdir -p $@

$(DIST_JAR_DIR)/%.jar: $(BUILD_DIR)/%.jar | $(DIST_JAR_DIR)
	@install -m 0644 $< $@

dist: $(BUILD_DIR_JARS) $(DIST_JARS)
	@:

clean:
	@rm -rf $(BUILD_DIR) $(DIST_JARS)
