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

.SECONDARY:

J2OBJC_ROOT = ..

include ../make/common.mk
include jars.mk

DISTRIBUTION_JARS = $(JSR305_JAR) $(JAVAX_INJECT_JAR) \
    $(JUNIT_JAR) $(JUNIT_DATAPROVIDER_JAR) $(HAMCREST_JAR) $(MOCKITO_JAR)
INTERNAL_JARS = \
  $(ERROR_PRONE_ANNOTATIONS_JAR) $(ERROR_PRONE_ANNOTATIONS_SOURCE_JAR) \
  $(CHECKER_QUAL_JAR) $(CHECKER_QUAL_SOURCE_JAR) \
  $(CHECKER_COMPAT_QUAL_JAR) $(CHECKER_COMPAT_QUAL_SOURCE_JAR) \
  $(ANIMAL_SNIFFER_ANNOTATIONS_JAR) $(ANIMAL_SNIFFER_ANNOTATIONS_SOURCE_JAR) \
  $(GUAVA_JAR) $(GUAVA_SOURCE_JAR) \
  $(GUAVA_ANDROID_JAR) $(GUAVA_ANDROID_SOURCE_JAR) \
  $(JUNIT_SOURCE_JAR) $(JUNIT_DATAPROVIDER_SOURCE_JAR) $(HAMCREST_SOURCE_JAR) \
  $(JSR305_SOURCE_JAR) $(JAVAX_INJECT_SOURCE_JAR) \
  $(MOCKITO_SOURCE_JAR) \
  $(JARJAR_JAR) \
  $(PROTOBUF_JAR) \
  $(PROCYON_JARS) \
  $(SCENELIB_JAR) \
  $(PLUME_UTIL_JAR) \
  $(FLOGGER_JAR) $(GOOGLE_EXTENSIONS_JAR)
ifdef JAVA_8
INTERNAL_JARS += $(JAVAC_JAR)
endif
DIST_JARS = $(DISTRIBUTION_JARS:%=$(DIST_JAR_DIR)/%)
DOCLET_JARS = $(DOCLAVA_JAR) $(JSILVER_JAR)
BUILD_DIR_JARS = $(DISTRIBUTION_JARS:%=$(BUILD_DIR)/%) $(INTERNAL_JARS:%=$(BUILD_DIR)/%)
BUILD_DIR_DOCLET_JARS = $(DOCLET_JARS:%=$(BUILD_DIR)/%)

$(BUILD_DIR_JARS): $(BUILD_DIR)/.maven_generate

# Postprocessing: update procyon to not depend on URLClassPath.
$(BUILD_DIR)/.maven_generate: | $(BUILD_DIR)
	mvn -q generate-resources dependency:sources
	@unzip -qq -d $(BUILD_DIR) $(BUILD_DIR)/$(NEW_PROCYON_COMPILERTOOLS_JAR) \
	  com/strobel/assembler/metadata/ClasspathTypeLoader.class
	@cd $(BUILD_DIR) && jar uf $(PROCYON_COMPILERTOOLS_JAR) com/strobel/assembler/metadata/ClasspathTypeLoader.class
	@rm -rf $(BUILD_DIR)/com
	@rm $(BUILD_DIR)/$(NEW_PROCYON_COMPILERTOOLS_JAR)
	@touch $@

$(BUILD_DIR)/$(JUNIT_JAR): $(BUILD_DIR)/$(JUNIT_ROOT_JAR)
	@cp -f $< $@

$(BUILD_DIR)/$(JUNIT_ROOT_JAR): $(BUILD_DIR)/.maven_generate
	@:

$(BUILD_DIR)/$(JAVAC_JAR): | $(BUILD_DIR)
	@cp -f $(JAVA_HOME)/lib/$(JAVAC_JAR) $@

$(BUILD_DIR)/$(SCENELIB_JAR): | $(BUILD_DIR)
	@curl -s -o $@.zip https://checkerframework.org/annotation-file-utilities/annotation-tools-3.9.1.zip
	@unzip -qq -d $(BUILD_DIR) $@.zip annotation-file-utilities/annotation-file-utilities.jar
	@unzip -qq -d $(BUILD_DIR) $(BUILD_DIR)/annotation-file-utilities/annotation-file-utilities.jar "scenelib/*"
	@cd $(BUILD_DIR) && zip -q -r $(SCENELIB_JAR) scenelib/
	@rm -rf $(BUILD_DIR)/annotation-file-utilities/ $(BUILD_DIR)/scenelib/

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
