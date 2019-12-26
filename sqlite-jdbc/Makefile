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

# Makefile for building the iOS sqlitejdbc library.
#
# Author: DaeHoon Zee

.SUFFIXES:
.PHONY: default clean

include environment.mk

SOURCEPATH = sources
SQLITEJDBC_SRC_JAR = sources/sqlite-jdbc-3.27.2.1-sources.jar
JAVA_SOURCES = $(shell jar -tf $(SQLITEJDBC_SRC_JAR) | grep "\.java" | awk '{print}' ORS=' ')

EXTRACTED_JAVA = $(JAVA_SOURCES:%=$(JAVA_SRC_DIR)/%)
JAVA_SOURCES_MANIFEST = $(BUILD_DIR)/java_sources.mf
OBJC_SOURCES_MANIFEST = $(BUILD_DIR)/objc_sources.mf

#CREATE_JAR_NAME = j2objc_sqlitejdbc
#/CREATE_JAR_SOURCES = $(EXTRACTED_JAVA)
#include ../make/create_jar.mk

TRANSLATE_JAVA_FULL = $(JAVA_SOURCES:%=$(JAVA_SRC_DIR)/%)
TRANSLATE_JAVA_RELATIVE = $(JAVA_SOURCES)
TRANSLATE_ARGS = -sourcepath $(JAVA_SRC_DIR)
include ../make/translate.mk

ARCH_HEADERS = $(TRANSLATE_HEADERS:$(GEN_OBJC_DIR)/%=$(ARCH_INCLUDE_DIR)/%)

FAT_LIB_NAME = sqlitejdbc
FAT_LIB_SOURCES_RELATIVE = $(TRANSLATE_SOURCES:$(GEN_OBJC_DIR)/%=%) objc/NativeDB.c objc/NativeDB_helper.m
FAT_LIB_SOURCE_DIRS = $(GEN_OBJC_DIR) sources
FAT_LIB_COMPILE = $(J2OBJCC) -fobjc-abi-version=2 -fobjc-legacy-dispatch
include ../make/fat_lib.mk

STATIC_FRAMEWORK_NAME = SQLITEJDBC
include ../make/static_framework.mk

SQLITEJDBC_LICENSE = sources/LICENSE
SQLITEJDBC_LICENSE_DIST = $(DIST_LICENSE_DIR)/sqlitejdbc_license.txt

JAR = $(BUILD_DIR)/j2objc_sqlitejdbc.jar
DIST_JAR = $(DIST_JAR_DIR)/j2objc_sqlitejdbc.jar

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

$(SQLITEJDBC_SRC_JAR): | java_deps_dist

$(BUILD_DIR)/.extracted: $(SQLITEJDBC_SRC_JAR) | $(BUILD_DIR)
	@mkdir -p build_result
	@mkdir -p $(JAVA_SRC_DIR)
	@unzip -q -o -d $(JAVA_SRC_DIR) $(SQLITEJDBC_SRC_JAR) $(JAVA_SOURCES)
	@cp -R sources/java/* $(JAVA_SRC_DIR)
	@touch $(EXTRACTED_JAVA)
	@touch $@

$(EXTRACTED_JAVA): $(BUILD_DIR)/.extracted
	@:

$(JAR): $(EXTRACTED_JAVA) | $(BUILD_DIR) java_deps_dist annotations_dist
	@echo "building j2objc_sqlitejdbc.jar"
	@stage_dir=`mktemp -d -t j2objc-sqlite-jdbc`; \
	$(JAVAC) -sourcepath $(SOURCEPATH) -encoding UTF-8 \
	    -cp $(DIST_JAR_DIR)/j2objc_annotations.jar -d $$stage_dir \
	    -source 1.7 -target 1.7 -bootclasspath $(DIST_JAR_DIR)/jre_emul.jar $^; \
	jar cf $@ -C $$stage_dir .; \
	rm -rf $$stage_dir

$(DIST_JAR): $(JAR) | $(DIST_JAR_DIR)
	@install -m 0644 $< $@

$(ARCH_INCLUDE_DIR)/%.h: $(GEN_OBJC_DIR)/%.h
	@mkdir -p $(@D)
	install -m 0644 $< $@

$(SQLITEJDBC_LICENSE_DIST): $(SQLITEJDBC_LICENSE)
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
	@rm -rf $(SQLITEJDBC_LICENSE_DIST) $(ARCH_INCLUDE_DIR)/javax/annotation
	@rm -rf $(STATIC_FRAMEWORK_DIR)

dist: $(FAT_LIBS_DIST) $(ARCH_HEADERS) $(SQLITEJDBC_LICENSE_DIST) $(DIST_JAR)
