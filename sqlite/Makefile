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

# Makefile for building the iOS sqlite library.
#
# Author: DaeHoon Zee

.SUFFIXES:
.PHONY: default clean

include environment.mk
include ../make/translate_macros.mk

SRC_JAR_NAME = sqlite-jdbc-3.27.2.1-sources.jar
# Python command to convert sources list in constants.bzl
JAVA_SOURCES = $(shell jar -tf $(SRC_JAR_NAME) | grep "\.java" | awk '{print}' ORS=' ')
FAT_LIB_NAME = sqlite

TRANSLATE_HEADERS = $(JAVA_SOURCES:%.java=$(GEN_OBJC_DIR)/%.h)
TRANSLATE_SOURCES = $(TRANSLATE_HEADERS:.h=.m)
TRANSLATE_OBJC = $(TRANSLATE_SOURCES) $(TRANSLATE_HEADERS)

ifdef TRANSLATE_OBJCPP
TRANSLATE_SOURCES = $(TRANSLATE_HEADERS:.h=.mm)
TRANSLATE_ARGS += -x objective-c++
endif

TRANSLATE_ARTIFACT := $(call translate_src_jar,\
  $(FAT_LIB_NAME),\
  $(GEN_OBJC_DIR),\
  $(SRC_JAR_NAME),\
  $(TRANSLATE_DEPENDENCIES),\
  $(TRANSLATE_ARGS))

$(TRANSLATE_OBJC): $(TRANSLATE_ARTIFACT)
	@:

ARCH_HEADERS = $(TRANSLATE_HEADERS:$(GEN_OBJC_DIR)/%=$(ARCH_INCLUDE_DIR)/%)

FAT_LIB_SOURCES_RELATIVE = $(TRANSLATE_SOURCES:$(GEN_OBJC_DIR)/%=%)
FAT_LIB_SOURCE_DIRS = $(GEN_OBJC_DIR)
FAT_LIB_COMPILE = $(J2OBJCC) -fobjc-abi-version=2 -fobjc-legacy-dispatch
include ../make/fat_lib.mk

STATIC_FRAMEWORK_NAME = SQLITE
include ../make/static_framework.mk

SQLITE_LICENSE = LICENSE
SQLITE_LICENSE_DIST = $(DIST_LICENSE_DIR)/sqlite_license.txt

default: lib

java: classes jar

# TODO(kstanger): Remove this.
classes: $(JAVA_SOURCES_MANIFEST)
	@echo Building class files
	@mkdir -p $(CLASSES_DIR)
	@$(JAVAC) -d $(CLASSES_DIR) \
	    -sourcepath $(SOURCE_PATH) -classpath $(CLASS_PATH) \
	    $$(cat $(JAVA_SOURCES_MANIFEST))

lib: $(FAT_LIBS)
	@:

$(SQLITE_SRC_JAR): | java_deps_dist

$(BUILD_DIR)/.extracted: $(SQLITE_SRC_JAR) | $(BUILD_DIR)
	@mkdir -p $(JAVA_SRC_DIR)
	@unzip -q -o -d $(JAVA_SRC_DIR) $(SQLITE_SRC_JAR) $(JAVA_SOURCES)
	@touch $(EXTRACTED_JAVA)
	@touch $@

$(EXTRACTED_JAVA): $(BUILD_DIR)/.extracted
	@:

$(ARCH_INCLUDE_DIR)/%.h: $(GEN_OBJC_DIR)/%.h
	@mkdir -p $(@D)
	install -m 0644 $< $@

$(SQLITE_LICENSE_DIST): $(SQLITE_LICENSE)
	@mkdir -p $(@D)
	@install -m 0644 $< $@

$(JAVA_SOURCES_MANIFEST): $(EXTRACTED_JAVA) | $(BUILD_DIR)
	@echo "Building $$(basename $@)"
	@if [ -e $@ ]; then rm $@; fi
	@for i in $^; do echo $$i >> $@; done

java_sources_manifest: $(JAVA_SOURCES_MANIFEST)
	@:

$(OBJC_SOURCES_MANIFEST): $(TRANSLATE_JAVA_FULL) | $(BUILD_DIR)
	@echo "Building $$(basename $@)"
	@if [ -e $@ ]; then rm $@; fi
	@files='$(sort $(TRANSLATE_OBJC))' && for i in $$files; do \
	  echo $$i >> $@; \
	done

objc_sources_manifest: $(OBJC_SOURCES_MANIFEST)
	@:

$(BUILD_DIR):
	@mkdir -p $@

clean:
	@rm -rf $(BUILD_DIR) $(FAT_LIBS_DIST)
	@rm -rf $(SQLITE_LICENSE_DIST) $(ARCH_INCLUDE_DIR)/javax/annotation
	@rm -rf $(STATIC_FRAMEWORK_DIR)

dist: $(FAT_LIBS_DIST) $(ARCH_HEADERS) $(SQLITE_LICENSE_DIST)
