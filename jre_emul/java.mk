# Copyright 2011 Google Inc. All Rights Reserved.
#
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

# Makefile for building the iOS emulation library.
#
# Author: Tom Ball

.PHONY: clean

include environment.mk
include jre_sources.mk

JAVA_SOURCES_MANIFEST = $(BUILD_DIR)/java_sources.mf

ALL_JAVA_SOURCES = $(JAVA_SOURCES) $(NO_TRANSLATE_JAVA_SOURCES)

ANNOTATIONS_JAR = $(DIST_JAR_DIR)/j2objc_annotations.jar

MKTEMP_DIR = j2objc-jre_emul

clean:
	@rm -f $(EMULATION_JAR_DIST) $(EMULATION_SRC_JAR_DIST) $(JSON_JAR_DIST)

jars_dist: emul_jar_dist emul_src_jar_dist json_jar_dist
ifndef JAVA_8
jars_dist: emul_module_dist
endif

emul_jar_dist: $(EMULATION_JAR_DIST)
	@:

emul_module_dist: $(EMULATION_MODULE_DIST)
	@:

emul_src_jar_dist: $(EMULATION_SRC_JAR_DIST)
	@:

$(EMULATION_JAR_DIST): $(EMULATION_JAR)
	@mkdir -p $(@D)
	@install -m 0644 $< $@

$(EMULATION_MODULE_DIST): $(EMULATION_MODULE)
	@mkdir -p $(@D)
	@cp -r $< $@

$(EMULATION_SRC_JAR_DIST): $(EMULATION_SRC_JAR)
	@mkdir -p $(@D)
	@install -m 0644 $< $@

$(EMULATION_JAR): $(ALL_JAVA_SOURCES)
	@mkdir -p $(@D)
	@echo "building jre_emul.jar"
	@set -e; stage_dir=`${MKTEMP_CMD}`; \
	  ../scripts/javac_no_deprecated_warnings.sh $(JAVAC) \
	  -classpath $(ANNOTATIONS_JAR) \
	  -d $$stage_dir -encoding UTF-8 -source 1.8 -target 1.8 -nowarn $^; \
	jar cf $(EMULATION_JAR) -C $$stage_dir .; \
	rm -rf $$stage_dir

$(EMULATION_MODULE): $(EMULATION_JAR)
	@echo "building jre_emul_module"
	@rm -rf $(EMULATION_MODULE)
	@mkdir $(BUILD_DIR)/jre_emul
	@cd $(BUILD_DIR)/jre_emul; jar xf $(EMULATION_JAR)
	@../scripts/gen_module_info.py --name java.base --root $(BUILD_DIR)/jre_emul \
	  --output $(BUILD_DIR)/module-info.java
	@$(JAVAC) --system=none --patch-module=java.base=$(EMULATION_JAR) \
	  -d $(BUILD_DIR)/jre_emul $(BUILD_DIR)/module-info.java
	@mkdir $(BUILD_DIR)/jmod
	@$(JAVA_HOME)/bin/jmod create --module-version $(JAVA_VERSION) \
	  --target-platform osx --class-path $(BUILD_DIR)/jre_emul \
	  $(BUILD_DIR)/jmod/jre_emul.jmod
	@$(JAVA_HOME)/bin/jlink --module-path $(BUILD_DIR)/jmod \
	  --add-modules java.base  --output $(EMULATION_MODULE)
	@cp $(JAVA_HOME)/lib/jrt-fs.jar $(EMULATION_MODULE)/lib/
	@rm -rf $(BUILD_DIR)/jre_emul
	@rm -rf $(BUILD_DIR)/jmod
	@rm $(BUILD_DIR)/module-info.*

$(EMULATION_SRC_JAR): $(ALL_JAVA_SOURCES)
	@mkdir -p $(@D)
	@echo "building jre_emul-src.jar"
	@../scripts/gen_java_source_jar.py -sourcepath $(JRE_SRC) \
	  -o $(EMULATION_SRC_JAR) $^

$(JAVA_SOURCES_MANIFEST): $(ALL_JAVA_SOURCES)
	@mkdir -p $(@D)
	@echo "building $$(basename $@)"
	@if [ -e $@ ]; then rm $@; fi
	@for i in $^; do echo $(CURDIR)/$$i >> $@; done

java_sources_manifest: $(JAVA_SOURCES_MANIFEST)
	@:

json_jar_dist: $(JSON_JAR_DIST)
	@:

$(JSON_JAR_DIST): $(JSON_JAR)
	@mkdir -p $(@D)
	@install -m 0644 $< $@

$(JSON_JAR): $(JSON_PUBLIC_SOURCES) $(JSON_PRIVATE_SOURCES) $(JSON_SOURCE_RETENTION_ANNOTATIONS)
	@mkdir -p $(@D)
	@echo "building json.jar"
	@set -e; stage_dir=`${MKTEMP_CMD}`; \
	  ../scripts/javac_no_deprecated_warnings.sh $(JAVAC) \
	  -d $$stage_dir -encoding UTF-8 -source 1.8 -target 1.8 -nowarn $^; \
	jar cf $(JSON_JAR) -C $$stage_dir .; \
	rm -rf $$stage_dir

find_cycles: cycle_finder_dist $(JAVA_SOURCES_MANIFEST)
	$(DIST_DIR)/cycle_finder --patch-module java.base=$(JRE_SRC) \
	  --suppress-list cycle_suppress_list.txt -s $(JAVA_SOURCES_MANIFEST) \
	  -external-annotation-file $(J2OBJC_ANNOTATIONS)
