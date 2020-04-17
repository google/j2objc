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

JAVA_SRC_DIR = $(BUILD_DIR)/java
JAVA_SOURCES = $(GUAVA_SOURCES:%=$(JAVA_SRC_DIR)/%) \
    $(ERROR_PRONE_ANNOTATIONS_SOURCES:%=$(JAVA_SRC_DIR)/%) \
    $(CHECKER_QUAL_SOURCES:%=$(JAVA_SRC_DIR)/%) \
    $(ANIMAL_SNIFFER_ANNOTATIONS_SOURCES:%=$(JAVA_SRC_DIR)/%)
OBJC_SOURCES_MANIFEST = $(BUILD_DIR)/objc_sources.mf
CLASSPATH_LIST = \
  $(DIST_JAR_DIR)/$(JSR305_JAR) \
  $(JAVA_DEPS_JAR_DIR)/$(ERROR_PRONE_ANNOTATIONS_JAR) \
  $(JAVA_DEPS_JAR_DIR)/$(CHECKER_QUAL_JAR) \
  $(JAVA_DEPS_JAR_DIR)/$(ANIMAL_SNIFFER_ANNOTATIONS_JAR) \
  $(DIST_JAR_DIR)/j2objc_annotations.jar
CLASSPATH = $(subst $(eval) ,:,$(strip $(CLASSPATH_LIST)))

TRANSLATE_JAVA_FULL = $(JAVA_SOURCES)
TRANSLATE_JAVA_RELATIVE = $(JAVA_SOURCES:$(JAVA_SRC_DIR)/%=%)
TRANSLATE_ARGS = -classpath $(CLASSPATH) -encoding UTF-8
include $(J2OBJC_ROOT)/make/translate.mk

J2OBJCC := $(ARCH_BIN_DIR)/j2objcc -c -I$(GEN_OBJC_DIR) $(CC_WARNINGS)
FAT_LIB_NAME = $(GUAVA_FAT_LIB_NAME)
FAT_LIB_SOURCES_RELATIVE = $(TRANSLATE_SOURCES:$(GEN_OBJC_DIR)/%=%)
FAT_LIB_SOURCE_DIRS = $(GEN_OBJC_DIR)
FAT_LIB_COMPILE = $(J2OBJCC)
include $(J2OBJC_ROOT)/make/fat_lib.mk

FRAMEWORK_NAME = $(GUAVA_FRAMEWORK_NAME)
include $(J2OBJC_ROOT)/make/framework.mk

fat_lib_dependencies: jre_emul_dist jsr305_dist

# Headers are installed in dist/include/guava/, so that developers can translate
# and use other versions of Guava without conflict.
DIST_GUAVA_INCLUDE_DIR = $(GUAVA_INCLUDE_DIR)
DIST_HEADERS = $(JAVA_SOURCES:$(JAVA_SRC_DIR)/%.java=$(DIST_GUAVA_INCLUDE_DIR)/%.h)
DIST_JAR = $(GUAVA_DIST_JAR)

dist: $(FAT_LIBS_DIST) $(DIST_JAR) $(DIST_HEADERS)

clean:
	@rm -rf $(BUILD_DIR) $(FAT_LIBS_DIST) $(DIST_GUAVA_INCLUDE_DIR) $(DIST_JAR)
	@rm -rf $(FRAMEWORK_DIR)

java: $(DIST_JAR)

$(JAR) $(GUAVA_SRC_JAR): | java_deps_dist
	@:

$(DIST_JAR): $(JAR)
	@mkdir -p $(@D)
	@install -m 0644 $< $@

$(DIST_GUAVA_INCLUDE_DIR)/%.h: $(GEN_OBJC_DIR)/%.h
	@mkdir -p $(@D)
	@install -m 0644 $< $@

$(OBJC_SOURCES_MANIFEST): | $(BUILD_DIR)
	@echo "Building $$(basename $@)"
	@if [ -e $@ ]; then rm $@; fi
	@files='$(TRANSLATE_SOURCES)' && for i in $$files; do \
	  echo $${i%.m}.h >> $@; \
	  echo $$i >> $@; \
	done

objc_sources_manifest: $(OBJC_SOURCES_MANIFEST)
	@:

$(BUILD_DIR)/.extracted: $(GUAVA_SRC_JAR) $(ERROR_PRONE_ANNOTATIONS_SRC_JAR) \
	$(CHECKER_QUAL_SRC_JAR) $(ANIMAL_SNIFFER_ANNOTATIONS_SRC_JAR) | java_deps_dist
	@echo "Extracting Guava sources"
	@mkdir -p $(JAVA_SRC_DIR)
	@unzip -o -q -d $(JAVA_SRC_DIR) $(GUAVA_SRC_JAR) $(GUAVA_SOURCES)
	@unzip -o -q -d $(JAVA_SRC_DIR) $(ERROR_PRONE_ANNOTATIONS_SRC_JAR) \
		$(ERROR_PRONE_ANNOTATIONS_SOURCES)
	@unzip -o -q -d $(JAVA_SRC_DIR) $(CHECKER_QUAL_SRC_JAR) \
		$(CHECKER_QUAL_SOURCES_ORIGINAL_PATH)
	@unzip -o -q -d $(JAVA_SRC_DIR) $(ANIMAL_SNIFFER_ANNOTATIONS_SRC_JAR) \
		$(ANIMAL_SNIFFER_ANNOTATIONS_SOURCES)
	@echo "Moving checker sources to top level."
	@rsync -a $(JAVA_SRC_DIR)/checker/src/org/ $(JAVA_SRC_DIR)/org/
	@rsync -a $(JAVA_SRC_DIR)/framework/src/org/ $(JAVA_SRC_DIR)/org/
	@echo "Removing problematic imports that are only used in javadoc comments."
	@sed -i '' -e '/import org.checkerframework.checker.nullness.AbstractNullnessChecker;/d' \
		$(JAVA_SRC_DIR)/org/checkerframework/checker/nullness/qual/Nullable.java
	@sed -i '' -e '/import org.checkerframework.checker.nullness.AbstractNullnessChecker;/d' \
		$(JAVA_SRC_DIR)/org/checkerframework/checker/nullness/qual/MonotonicNonNull.java
	@sed -i '' -e '/import org.checkerframework.checker.nullness.AbstractNullnessChecker;/d' \
		$(JAVA_SRC_DIR)/org/checkerframework/checker/nullness/qual/NonNull.java
	@sed -i '' -e '/import org.checkerframework.checker.initialization.InitializationChecker;/d' \
		$(JAVA_SRC_DIR)/org/checkerframework/checker/nullness/qual/NonNull.java
	@sed -i '' -e '/import org.checkerframework.framework.util.defaults.QualifierDefaults;/d' \
		$(JAVA_SRC_DIR)/org/checkerframework/framework/qual/DefaultQualifierInHierarchy.java
	@touch $(JAVA_SOURCES)
	@touch $@

$(JAVA_SOURCES): $(BUILD_DIR)/.extracted
