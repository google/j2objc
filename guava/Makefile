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
# TODO(kstanger): Build a fat library to distribute.

SUFFIXES:

default: dist

include environment.mk

JAVA_SOURCES := \
    $(filter-out $(SOURCES_TO_EXCLUDE:%=$(SRC_DIR)/%),$(shell find $(SRC_DIR) -name *.java))

JAVA_SOURCES_MANIFEST = $(BUILD_DIR)/java_sources.mf
OBJC_SOURCES_MANIFEST = $(BUILD_DIR)/objc_sources.mf

CLASSPATH = $(DIST_JAR_DIR)/$(JSR305_JAR):$(DIST_JAR_DIR)/j2objc_annotations.jar
SOURCEPATH = $(SRC_DIR)

INCLUDE_DIRS = $(GEN_OBJC_DIR)

J2OBJCC = $(ARCH_BIN_DIR)/j2objcc -c -fobjc-abi-version=2 -fobjc-legacy-dispatch \
  $(INCLUDE_DIRS:%=-I%)

TRANSLATE_JAVA_FULL = $(JAVA_SOURCES)
TRANSLATE_JAVA_RELATIVE = $(JAVA_SOURCES:$(SRC_DIR)/%=%)
TRANSLATE_ARGS = --segmented-headers --extract-unsequenced --batch-translate-max=300 \
    -cp $(CLASSPATH) -sourcepath $(SOURCEPATH) -encoding UTF-8 -J-Xmx2G \
    --hide-private-members --final-methods-as-functions
include ../make/translate.mk

FAT_LIB_NAME = guava
FAT_LIB_SOURCES_RELATIVE = $(TRANSLATE_SOURCES:$(GEN_OBJC_DIR)/%=%)
FAT_LIB_SOURCE_DIRS = $(GEN_OBJC_DIR)
FAT_LIB_COMPILE = $(J2OBJCC)
include ../make/fat_lib.mk

fat_lib_dependencies: jre_emul_dist jsr305_dist

DIST_LIBRARY = $(ARCH_LIB_DIR)/libguava.a
JAR = $(BUILD_DIR)/j2objc_guava.jar
DIST_JAR = $(DIST_JAR_DIR)/j2objc_guava.jar

JAR_STAGE_DIR = /tmp/j2objc_guava

dist: $(DIST_LIBRARY) $(DIST_JAR)
	@(cd $(GEN_OBJC_DIR) && tar cf - $(TRANSLATE_HEADERS:$(GEN_OBJC_DIR)/%=%)) \
	    | (cd $(ARCH_INCLUDE_DIR); tar xfp -)

test: $(FAT_LIB_LIBRARY)
	@$(MAKE) -f tests.mk test

clean:
	@rm -rf $(BUILD_DIR) $(DIST_LIBRARY) $(ARCH_INCLUDE_DIR)/com/google/common $(DIST_JAR)

java: $(DIST_JAR)

# TODO(kstanger): Add -Xlint:unchecked once warnings are fixed.
$(JAR): $(JAVA_SOURCES) | $(BUILD_DIR) java_deps_dist annotations_dist
	@echo "building j2objc_guava.jar"
	@rm -rf $(JAR_STAGE_DIR)
	@mkdir $(JAR_STAGE_DIR)
	@$(JAVAC) -sourcepath $(SOURCEPATH) -cp $(CLASSPATH) -encoding UTF-8 -d $(JAR_STAGE_DIR) $^
	@jar cf $@ -C $(JAR_STAGE_DIR) .

$(DIST_JAR): $(JAR) | $(DIST_JAR_DIR)
	@install -m 0644 $< $@

$(DIST_LIBRARY): $(FAT_LIB_LIBRARY) | $(ARCH_LIB_DIR)
	install -m 0644 $< $@

$(JAVA_SOURCES_MANIFEST): $(JAVA_SOURCES) | $(BUILD_DIR)
	@echo "Building $$(basename $@)"
	@if [ -e $@ ]; then rm $@; fi
	@files='$^' && for i in $$files; do echo $$i >> $@; done

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

find_cycles: $(JAVA_SOURCES_MANIFEST) | cycle_finder_dist jre_emul_java_manifest
	$(DIST_DIR)/cycle_finder -sourcepath $(SOURCEPATH) -cp $(CLASSPATH) \
	  -w ../jre_emul/cycle_whitelist.txt -w cycle_whitelist \
	  -s ../jre_emul/$(BUILD_DIR_NAME)/java_sources.mf -s $(JAVA_SOURCES_MANIFEST)
