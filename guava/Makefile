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

# Builds a J2ObjC translated Guava library.
#
# Author: Keith Stanger

SUFFIXES:

default: dist

include environment.mk

JAVA_SOURCES_INCLUDE = $(BUILD_DIR)/java_sources.mk
ifndef IS_CLEAN_GOAL
-include $(JAVA_SOURCES_INCLUDE)
endif

OBJC_SOURCES_MANIFEST = $(BUILD_DIR)/objc_sources.mf

CLASSPATH = $(DIST_JAR_DIR)/$(JSR305_JAR):$(DIST_JAR_DIR)/j2objc_annotations.jar

INCLUDE_DIRS = $(GEN_OBJC_DIR)

J2OBJCC := $(ARCH_BIN_DIR)/j2objcc -c -fobjc-abi-version=2 -fobjc-legacy-dispatch \
  $(INCLUDE_DIRS:%=-I%) $(CC_WARNINGS)

# Headers are installed in dist/include/guava/, so that developers can translate
# and use other versions of Guava without conflict.
DIST_GUAVA_INCLUDE_DIR = $(ARCH_INCLUDE_DIR)/guava

TRANSLATE_ARGS = --segmented-headers --extract-unsequenced --batch-translate-max=300 \
    -cp $(CLASSPATH) -encoding UTF-8 -J-Xmx2G -source 1.7 \
    --hide-private-members --final-methods-as-functions -q

TRANSLATE_MARK = $(BUILD_DIR)/.translate_mark
OBJC_SOURCES = $(JAVA_SOURCES:%.java=$(GEN_OBJC_DIR)/%.m)
OBJC_HEADERS = $(JAVA_SOURCES:%.java=$(GEN_OBJC_DIR)/%.h)
DIST_HEADERS = $(OBJC_HEADERS:$(GEN_OBJC_DIR)/%=$(DIST_GUAVA_INCLUDE_DIR)/%)

FAT_LIB_NAME = guava
FAT_LIB_SOURCES_RELATIVE = $(JAVA_SOURCES:.java=.m)
FAT_LIB_SOURCE_DIRS = $(GEN_OBJC_DIR)
FAT_LIB_COMPILE = $(J2OBJCC)
include ../make/fat_lib.mk

STATIC_FRAMEWORK_NAME = Guava
STATIC_FRAMEWORK_HEADERS = $(OBJC_HEADERS)
include ../make/static_framework.mk

fat_lib_dependencies: jre_emul_dist jsr305_dist

DIST_JAR = $(DIST_JAR_DIR)/$(GUAVA_JAR)

dist: $(FAT_LIBS_DIST) $(DIST_JAR) $(DIST_HEADERS)

clean:
	@rm -rf $(BUILD_DIR) $(FAT_LIBS_DIST) $(DIST_GUAVA_INCLUDE_DIR) $(DIST_JAR)
	@rm -rf $(STATIC_FRAMEWORK_DIR)

java: $(DIST_JAR)

$(SRC_JAR) $(JAR): | java_deps_dist
	@:

$(JAVA_SOURCES_INCLUDE): $(SRC_JAR)
	@mkdir -p $(@D)
	@echo "JAVA_SOURCES = \\" > $@
	@for file in `jar tf $< | grep \.java$$`; do \
	  echo "  $$file \\" >> $@; \
	done;

translate: $(TRANSLATE_MARK)
	@:

$(DIST_DIR)/j2objc: | translator_dist
	@:

$(TRANSLATE_MARK): $(DIST_DIR)/j2objc $(SRC_JAR)
	@echo "Translating Guava sources"
	@mkdir -p $(GEN_OBJC_DIR) $(@D)
	@$(DIST_DIR)/j2objc $(TRANSLATE_ARGS) -d $(GEN_OBJC_DIR) $(SRC_JAR)
	@touch $@

$(OBJC_SOURCES) $(OBJC_HEADERS): $(TRANSLATE_MARK)
	@:

$(DIST_JAR): $(JAR)
	@mkdir -p $(@D)
	@install -m 0644 $< $@

$(DIST_GUAVA_INCLUDE_DIR)/%.h: $(GEN_OBJC_DIR)/%.h
	@mkdir -p $(@D)
	@install -m 0644 $< $@

$(OBJC_SOURCES_MANIFEST): $(SRC_JAR)
	@mkdir -p $(@D)
	@echo "Building $$(basename $@)"
	@if [ -e $@ ]; then rm $@; fi
	@files='$(sort $(OBJC_SOURCES) $(OBJC_HEADERS))' && for i in $$files; do \
	  echo $$i >> $@; \
	done

objc_sources_manifest: $(OBJC_SOURCES_MANIFEST)
	@:
