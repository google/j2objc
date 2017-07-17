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

# Copies all dependent .jar files used by j2objc into the build.
#
# Author: Keith Stanger

SUFFIXES:

.SECONDARY:

J2OBJC_ROOT = ..

include ../make/common.mk
include jars.mk

DISTRIBUTION_JARS = $(JSR305_JAR) $(JAVAX_INJECT_JAR) \
    $(JUNIT_JAR) $(HAMCREST_JAR) $(MOCKITO_JAR)
INTERNAL_JARS = \
  $(GUAVA_JAR) $(GUAVA_SOURCE_JAR) \
  $(JUNIT_SOURCE_JAR) $(HAMCREST_SOURCE_JAR) \
  $(JSR305_SOURCE_JAR) $(JAVAX_INJECT_SOURCE_JAR) \
  $(MOCKITO_SOURCE_JAR) \
  $(ECLIPSE_JARS) \
  $(JARJAR_JAR) \
  $(ASM_JAR) \
  $(JAVAC_JAR) \
  $(PROTOBUF_JAR) \
  $(PROCYON_JARS)
DIST_JARS = $(DISTRIBUTION_JARS:%=$(DIST_JAR_DIR)/%)
DOCLET_JARS = $(DOCLAVA_JAR) $(JSILVER_JAR)
BUILD_DIR_JARS = $(DISTRIBUTION_JARS:%=$(BUILD_DIR)/%) $(INTERNAL_JARS:%=$(BUILD_DIR)/%)
BUILD_DIR_DOCLET_JARS = $(DOCLET_JARS:%=$(BUILD_DIR)/%)


$(BUILD_DIR_JARS): $(BUILD_DIR)/.maven_generate

$(BUILD_DIR)/.maven_generate: | $(BUILD_DIR)
	mvn -q generate-resources dependency:sources
	@touch $@

$(BUILD_DIR)/$(JUNIT_JAR): $(BUILD_DIR)/$(JUNIT_ROOT_JAR)
	@cp -f $< $@

$(BUILD_DIR)/$(JUNIT_ROOT_JAR): $(BUILD_DIR)/.maven_generate
	@:

$(BUILD_DIR)/$(JAVAC_JAR): | $(BUILD_DIR)
	@cp -f $(JAVA_HOME)/lib/$(JAVAC_JAR) $@

$(BUILD_DIR)/$(JARJAR_JAR): | $(BUILD_DIR)
	@cp -f $(JARJAR_HOME)/$(JARJAR_JAR) $@

$(BUILD_DIR) $(DIST_JAR_DIR):
	@mkdir -p $@

$(DIST_JAR_DIR)/%.jar: $(BUILD_DIR)/%.jar | $(DIST_JAR_DIR)
	@install -m 0644 $< $@

dist: $(BUILD_DIR_JARS) $(DIST_JARS)
	@:

javadoc_jars: $(BUILD_DIR_DOCLET_JARS) | $(BUILD_DIR)
	@:

clean:
	@rm -rf $(BUILD_DIR) $(DIST_JARS)
