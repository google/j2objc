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

# Makefile for building the iOS junit library.
#
# Author: Tom Ball

.PHONY: default j2objc clean

include environment.mk
include java_sources.mk

JAVA_SOURCE_LIST = $(BUILD_DIR)/junit.classes.list
EXTRACTED_JAVA = $(JUNIT_SOURCES:%=$(JAVA_SRC_DIR)/%) \
    $(JUNIT_DATAPROVIDER_SOURCES:%=$(JAVA_SRC_DIR)/%) \
    $(HAMCREST_SOURCES:%=$(JAVA_SRC_DIR)/%)
PUBLIC_HEADERS = \
    $(JUNIT_SOURCES:%.java=%.h) \
    $(JUNIT_DATAPROVIDER_SOURCES:%.java=%.h) \
    $(HAMCREST_SOURCES:%.java=%.h)
ARCH_HEADERS = $(PUBLIC_HEADERS:%=$(ARCH_INCLUDE_DIR)/%)

JAVA_SOURCES = $(EXTRACTED_JAVA)
JUNIT_DIST_JAR = $(DIST_JAR_DIR)/$(JUNIT_JAR)
CLASSPATH = $(DIST_JAR_DIR)/j2objc_annotations.jar

JAVA_SOURCES_MANIFEST = $(BUILD_DIR)/java_sources.mf
OBJC_SOURCES_MANIFEST = $(BUILD_DIR)/objc_sources.mf

TRANSLATE_JAVA_FULL = $(JAVA_SOURCES)
TRANSLATE_JAVA_RELATIVE = $(EXTRACTED_JAVA:$(JAVA_SRC_DIR)/%=%)
TRANSLATE_ARGS = -sourcepath $(JAVA_SRC_DIR) -source 1.7
include ../make/translate.mk

FAT_LIB_NAME = junit
FAT_LIB_SOURCES_RELATIVE = $(TRANSLATE_SOURCES:$(GEN_OBJC_DIR)/%=%)
FAT_LIB_SOURCE_DIRS = $(GEN_OBJC_DIR)
FAT_LIB_COMPILE = $(CLANG) $(OBJCFLAGS) -I$(GEN_OBJC_DIR) -I$(ARCH_INCLUDE_DIR)
include ../make/fat_lib.mk

FRAMEWORK_NAME = JUnit
include ../make/framework.mk

JUNIT_LICENSE = LICENSE
JUNIT_LICENSE_DIST = $(DIST_LICENSE_DIR)/junit_license.txt

fat_lib_dependencies: jre_emul_dist

java: $(JUNIT_DIST_JAR)
	@:

lib: $(FAT_LIBS)
	@:

$(BUILD_DIR)/.extracted: $(JUNIT_SRC_JAR) $(JUNIT_DATAPROVIDER_SRC_JAR) $(HAMCREST_SRC_JAR)
	@echo extracting sources
	@mkdir -p $(JAVA_SRC_DIR)
	@unzip -q -o -d $(JAVA_SRC_DIR) $(JUNIT_SRC_JAR) $(JUNIT_SOURCES)
	@unzip -q -o -d $(JAVA_SRC_DIR) $(JUNIT_DATAPROVIDER_SRC_JAR) $(JUNIT_DATAPROVIDER_SOURCES)
	# Removing import that does not impact functionality and just adds another dependency.
	@sed -i '' -e '/import edu.umd.cs.findbugs.annotations.SuppressWarnings;/d' \
		$(JAVA_SRC_DIR)/com/tngtech/java/junit/dataprovider/internal/placeholder/ParameterPlaceholder.java
	@unzip -q -o -d $(JAVA_SRC_DIR) $(HAMCREST_SRC_JAR) $(HAMCREST_SOURCES)
	@touch $(EXTRACTED_JAVA)
	@touch $@

$(EXTRACTED_JAVA): $(BUILD_DIR)/.extracted

$(JAVA_SOURCES_MANIFEST): $(JAVA_SOURCES) | $(BUILD_DIR)
	@echo "Building $$(basename $@)"
	@if [ -e $@ ]; then rm $@; fi
	@files='$^' && for i in $$files; do echo $$i >> $@; done

java_sources_manifest: $(JAVA_SOURCES_MANIFEST)
	@:

$(OBJC_SOURCES_MANIFEST): | $(BUILD_DIR)
	@echo "Building $$(basename $@)"
	@if [ -e $@ ]; then rm $@; fi
	@files='$(TRANSLATE_SOURCES)' && for i in $$files; do \
	  echo $${i%.m}.h >> $@; \
	  echo $$i >> $@; \
	done

objc_sources_manifest: $(OBJC_SOURCES_MANIFEST)
	@:

$(ARCH_INCLUDE_DIR)/%.h: $(GEN_OBJC_DIR)/%.h
	@mkdir -p $(@D)
	install -m 0644 $< $@

$(JUNIT_LICENSE_DIST): $(JUNIT_LICENSE)
	@mkdir -p $(@D)
	@install -m 0644 $< $@

$(BUILD_DIR):
	@mkdir -p $@

clean:
	@rm -rf $(BUILD_DIR) $(FAT_LIBS_DIST) $(JUNIT_LICENSE_DIST)
	@rm -rf $(ARCH_INCLUDE_DIR)/junit $(ARCH_INCLUDE_DIR)/org/junit
	@rm -rf $(FRAMEWORK_DIR)

dist: lib $(FAT_LIBS_DIST) $(ARCH_HEADERS) $(JUNIT_LICENSE_DIST)

# This is currently broken. The hamcrest source can't compile with 1.8 and the included JRE sources
# can't compile with < 1.8.
find_cycles: $(JAVA_SOURCES_MANIFEST) | cycle_finder_dist jre_emul_java_manifest
	$(DIST_DIR)/cycle_finder -sourcepath $(JAVA_SRC_DIR) -source 1.7 \
      -cp $(CLASSPATH) --suppress-list cycle_suppress_list.txt \
			--suppress-list ../jre_emul/cycle_suppress_list.txt \
      -s ../jre_emul/$(BUILD_DIR_NAME)/java_sources.mf -s $(JAVA_SOURCES_MANIFEST)
