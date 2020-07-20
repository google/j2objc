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

# Makefile for building the iOS javax-inject library.
#
# Author: Tom Ball

.PHONY: default clean

include environment.mk

JAVA_SOURCES = \
    javax/inject/Inject.java \
    javax/inject/Named.java \
    javax/inject/Provider.java \
    javax/inject/Qualifier.java \
    javax/inject/Singleton.java \
    javax/inject/Scope.java

EXTRACTED_JAVA = $(JAVA_SOURCES:%=$(JAVA_SRC_DIR)/%)
JAVA_SOURCES_MANIFEST = $(BUILD_DIR)/java_sources.mf
OBJC_SOURCES_MANIFEST = $(BUILD_DIR)/objc_sources.mf

CREATE_JAR_NAME = javax_inject
CREATE_JAR_SOURCES = $(EXTRACTED_JAVA)
include $(J2OBJC_ROOT)/make/create_jar.mk

TRANSLATE_JAVA_FULL = $(JAVA_SOURCES:%=$(JAVA_SRC_DIR)/%)
TRANSLATE_JAVA_RELATIVE = $(JAVA_SOURCES)
TRANSLATE_ARGS = -sourcepath $(JAVA_SRC_DIR) -q
include $(J2OBJC_ROOT)/make/translate.mk

ARCH_HEADERS = $(TRANSLATE_HEADERS:$(GEN_OBJC_DIR)/%=$(ARCH_INCLUDE_DIR)/%)

FAT_LIB_NAME = javax_inject
FAT_LIB_SOURCES_RELATIVE = $(TRANSLATE_SOURCES:$(GEN_OBJC_DIR)/%=%)
FAT_LIB_SOURCE_DIRS = $(GEN_OBJC_DIR)
FAT_LIB_COMPILE = $(J2OBJCC) -fobjc-abi-version=2 -fobjc-legacy-dispatch
include $(J2OBJC_ROOT)/make/fat_lib.mk

FRAMEWORK_NAME = JavaxInject
include $(J2OBJC_ROOT)/make/framework.mk

JSR330_LICENSE = LICENSE
JSR330_LICENSE_DIST = $(DIST_LICENSE_DIR)/jsr330_license.txt

default: lib

java: jar

lib: $(FAT_LIBS)
	@:

$(BUILD_DIR)/.extracted: $(INJECT_SRC_JAR) | $(BUILD_DIR)
	@mkdir -p $(JAVA_SRC_DIR)
	@unzip -q -o -d $(JAVA_SRC_DIR) $(INJECT_SRC_JAR) $(JAVA_SOURCES)
	@touch $(EXTRACTED_JAVA)
	@touch $@

$(EXTRACTED_JAVA): $(BUILD_DIR)/.extracted
	@:

$(ARCH_INCLUDE_DIR)/%.h: $(GEN_OBJC_DIR)/%.h
	@mkdir -p $(@D)
	install -m 0644 $< $@

$(JSR330_LICENSE_DIST): $(JSR330_LICENSE)
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
	@rm -rf $(BUILD_DIR) $(FAT_LIBS_DIST) $(JSR330_LICENSE_DIST)
	@rm -rf $(ARCH_INCLUDE_DIR)/javax/inject
	@rm -rf $(FRAMEWORK_DIR)

dist: $(FAT_LIBS_DIST) $(ARCH_HEADERS) $(JSR330_LICENSE_DIST)
