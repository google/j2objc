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

SUFFIXES:

.SECONDARY:

J2OBJC_ROOT = ..

include ../make/common.mk
include jars.mk

DISTRIBUTION_JARS = $(ECLIPSE_JARS) $(GUAVA_JAR) $(JSR305_JAR) $(JAVAX_INJECT_JAR) \
    $(JUNIT_JAR) $(MOCKITO_JAR)
INTERNAL_JARS = $(JUNIT_SOURCE_JAR) $(HAMCREST_JAR) $(JSR305_SOURCE_JAR) \
    $(JAVAX_INJECT_SOURCE_JAR) $(MOCKITO_SOURCE_JAR)
DIST_JARS = $(DISTRIBUTION_JARS:%=$(DIST_JAR_DIR)/%)
BUILD_DIR_JARS = $(DISTRIBUTION_JARS:%=$(BUILD_DIR)/%) $(INTERNAL_JARS:%=$(BUILD_DIR)/%)


ifndef M2_HOME
M2_HOME := $(shell echo $$HOME)/.m2
endif
JUNIT_DIR = $(M2_HOME)/repository/junit/junit/4.10
JSR305_DIR = $(M2_HOME)/repository/com/google/code/findbugs/jsr305/2.0.2
JAVAX_INJECT_DIR = $(M2_HOME)/repository/javax/inject/javax.inject/1
HAMCREST_DIR = $(M2_HOME)/repository/org/hamcrest/hamcrest-core/1.1
MOCKITO_DIR = $(M2_HOME)/repository/org/mockito/mockito-core/1.9.5

MAVEN_RESULTS = $(DISTRIBUTION_JARS:%=$(BUILD_DIR)/%) $(JUNIT_DIR)/$(JUNIT_SOURCE_JAR)
$(MAVEN_RESULTS): $(BUILD_DIR)/.maven_generate

$(BUILD_DIR)/.maven_generate: | $(BUILD_DIR)
	mvn generate-resources dependency:sources
	@touch $@

$(BUILD_DIR)/$(JUNIT_SOURCE_JAR): $(JUNIT_DIR)/$(JUNIT_SOURCE_JAR) | $(BUILD_DIR)
	@cp -f $< $@

$(BUILD_DIR)/$(HAMCREST_JAR): $(HAMCREST_DIR)/$(HAMCREST_JAR) | $(BUILD_DIR)
	@cp -f $< $@

$(BUILD_DIR)/$(JSR305_SOURCE_JAR): $(JSR305_DIR)/$(JSR305_SOURCE_JAR) | $(BUILD_DIR)
	@cp -f $< $@

$(BUILD_DIR)/$(JAVAX_INJECT_SOURCE_JAR): \
   $(JAVAX_INJECT_DIR)/$(JAVAX_INJECT_SOURCE_JAR) | $(BUILD_DIR)
	@cp -f $< $@

$(BUILD_DIR)/$(MOCKITO_SOURCE_JAR): $(MOCKITO_DIR)/$(MOCKITO_SOURCE_JAR) | $(BUILD_DIR)
	@cp -f $< $@

$(BUILD_DIR) $(DIST_JAR_DIR):
	@mkdir -p $@

$(DIST_JAR_DIR)/%.jar: $(BUILD_DIR)/%.jar | $(DIST_JAR_DIR)
	@install -m 0644 $< $@

dist: $(BUILD_DIR_JARS) $(DIST_JARS)
	@:

clean:
	@rm -rf $(BUILD_DIR) $(DIST_JARS)
