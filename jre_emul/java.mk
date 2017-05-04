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

.SUFFIXES:
.PHONY: clean

include environment.mk
include jre_sources.mk

JAVA_SOURCES_MANIFEST = $(BUILD_DIR)/java_sources.mf

ALL_JAVA_SOURCES = $(JAVA_SOURCES) $(NO_TRANSLATE_JAVA_SOURCES)

ANNOTATIONS_JAR = $(DIST_JAR_DIR)/j2objc_annotations.jar

clean:
	@rm -f $(EMULATION_JAR_DIST) $(EMULATION_SRC_JAR_DIST)

jars_dist: emul_jar_dist emul_src_jar_dist

emul_jar_dist: $(EMULATION_JAR_DIST)
	@:

emul_src_jar_dist: $(EMULATION_SRC_JAR_DIST)
	@:

$(EMULATION_JAR_DIST): $(EMULATION_JAR)
	@mkdir -p $(@D)
	@install -m 0644 $< $@

$(EMULATION_SRC_JAR_DIST): $(EMULATION_SRC_JAR)
	@mkdir -p $(@D)
	@install -m 0644 $< $@

# The following test returns true on Linux or with GNU tools installed,
# otherwise false on macOS which uses the BSD version.
ifeq ($(shell mktemp --version >/dev/null 2>&1 && echo GNU || echo BSD), GNU)
MKTEMP_CMD = mktemp -d --tmpdir j2objc-jre_emul.XXXXXX
else
MKTEMP_CMD = mktemp -d -t j2objc-jre_emul
endif

$(EMULATION_JAR): $(ALL_JAVA_SOURCES)
	@mkdir -p $(@D)
	@echo "building jre_emul.jar"
	@set -e; stage_dir=`${MKTEMP_CMD}`; \
	  ../scripts/javac_no_deprecated_warnings.sh $(JAVAC) \
	  -classpath $(ANNOTATIONS_JAR) \
	  -d $$stage_dir -encoding UTF-8 -source 1.8 -target 1.8 -nowarn $^; \
	jar cf $(EMULATION_JAR) -C $$stage_dir .; \
	rm -rf $$stage_dir

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

find_cycles: cycle_finder_dist $(JAVA_SOURCES_MANIFEST)
	$(DIST_DIR)/cycle_finder -source 1.8 -w cycle_whitelist.txt -s $(JAVA_SOURCES_MANIFEST)

