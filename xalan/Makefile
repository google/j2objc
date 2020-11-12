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

# Builds Android's Apache Xalan library.

default: dist

include environment.mk

JAVA_SOURCES := $(shell find $(SRC_DIR) -name *.java)

JAVA_SOURCES_MANIFEST = $(BUILD_DIR)/java_sources.mf
OBJC_SOURCES_MANIFEST = $(BUILD_DIR)/objc_sources.mf

SOURCEPATH = $(SRC_DIR)
INCLUDE_DIRS = $(GEN_OBJC_DIR)

J2OBJCC := $(ARCH_BIN_DIR)/j2objcc -c $(INCLUDE_DIRS:%=-I%) $(CC_WARNINGS)

TRANSLATE_JAVA_FULL = $(JAVA_SOURCES)
TRANSLATE_JAVA_RELATIVE = $(JAVA_SOURCES:$(SRC_DIR)/%=%)
TRANSLATE_ARGS = -sourcepath $(SOURCEPATH) -encoding UTF-8
include $(J2OBJC_ROOT)/make/translate.mk

FAT_LIB_NAME = xalan
FAT_LIB_SOURCES_RELATIVE = $(TRANSLATE_SOURCES:$(GEN_OBJC_DIR)/%=%)
FAT_LIB_SOURCE_DIRS = $(GEN_OBJC_DIR)
FAT_LIB_COMPILE = $(J2OBJCC)
include $(J2OBJC_ROOT)/make/fat_lib.mk

PUBLIC_JAVA_SOURCES = \
    $(shell cd $(SRC_DIR) && find org -name *.java | xargs egrep -l "^(public|protected) ")

FRAMEWORK_RESOURCE_FILES = \
	META-INF/services/javax.xml.transform.TransformerFactory \
	META-INF/services/javax.xml.xpath.XPathFactory \
	META-INF/services/org.apache.xml.dtm.DTMManager \
	org/apache/xalan/res/XSLTInfo.properties \
	org/apache/xml/serializer/Encodings.properties \
	org/apache/xml/serializer/HTMLEntities.properties \
	org/apache/xml/serializer/output_html.properties \
	org/apache/xml/serializer/output_text.properties \
	org/apache/xml/serializer/output_unknown.properties \
	org/apache/xml/serializer/output_xml.properties \
	org/apache/xml/serializer/XMLEntities.properties

FRAMEWORK_NAME = Xalan
FRAMEWORK_PUBLIC_HEADERS = $(PUBLIC_JAVA_SOURCES:%.java=$(GEN_OBJC_DIR)/%.h)
include $(J2OBJC_ROOT)/make/framework.mk

fat_lib_dependencies: jre_emul_dist

JAR = $(BUILD_DIR)/j2objc_xalan.jar
DIST_JAR = $(DIST_JAR_DIR)/j2objc_xalan.jar

MKTEMP_DIR = j2objc-xalan

XALAN_LICENSE = NOTICE
XALAN_LICENSE_DIST = $(DIST_LICENSE_DIR)/apache_xalan_license.txt

dist: $(FAT_LIBS_DIST) $(DIST_JAR) $(XALAN_LICENSE_DIST)
	@tar cf - -C $(GEN_OBJC_DIR) $(TRANSLATE_HEADERS:$(GEN_OBJC_DIR)/%=%) \
	    | tar xfp - -C $(ARCH_INCLUDE_DIR)

test: $(FAT_LIBS)
	@$(MAKE) -f tests.mk test

clean:
	@rm -rf $(BUILD_DIR) $(FAT_LIBS_DIST) $(ARCH_INCLUDE_DIR)/com/google/common $(DIST_JAR)
	@rm -rf $(XALAN_LICENSE_DIST)
	@rm -rf $(FRAMEWORK_DIR)

java: $(DIST_JAR)

$(JAR): $(JAVA_SOURCES) | $(BUILD_DIR) java_deps_dist annotations_dist
	@echo "building j2objc_xalan.jar"
	@stage_dir=`${MKTEMP_CMD}`; \
	$(JAVAC) -sourcepath $(SOURCEPATH) -encoding UTF-8 \
	    -cp $(DIST_JAR_DIR)/j2objc_annotations.jar -d $$stage_dir \
	    -source 1.7 -target 1.7 -bootclasspath $(DIST_JAR_DIR)/jre_emul.jar $^; \
	jar cf $@ -C $$stage_dir .; \
	rm -rf $$stage_dir

$(DIST_JAR): $(JAR) | $(DIST_JAR_DIR)
	@install -m 0644 $< $@

$(XALAN_LICENSE_DIST): $(XALAN_LICENSE)
	@mkdir -p $(@D)
	@install -m 0644 $< $@

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
	$(DIST_DIR)/cycle_finder -sourcepath $(SOURCEPATH) \
	  --suppress-list ../jre_emul/cycle_suppress_list.txt \
	  -s ../jre_emul/$(BUILD_DIR_NAME)/java_sources.mf -s $(JAVA_SOURCES_MANIFEST)
