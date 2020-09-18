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

.PHONY: default clean

include environment.mk
include jre_sources.mk
include icu_data.mk

MAIN_OBJ = J2ObjCMain.o

JAVA_TO_TRANSLATE = $(JAVA_SOURCES) $(JAVA_SOURCES_JSON)

MAIN_LIB_OBJS = $(MAIN_OBJ:%=$(CLASS_DIR)/%)

# translate is the default so that Xcode can build its own object files
default: libs
	@: # suppress make's "nothing to be done" message

GEN_OBJC_DIR = $(TRANSLATED_SOURCE_DIR)
TRANSLATE_NAME = jre_emul
TRANSLATE_JAVA_FULL = $(JAVA_TO_TRANSLATE)
TRANSLATE_JAVA_RELATIVE = $(JAVA_TO_TRANSLATE)
TRANSLATE_ARGS = -sourcepath $(JRE_SRC) \
  $(J2OBJC_DEBUGFLAGS) -encoding UTF-8 --doc-comments -Xtranslate-bootclasspath \
  --nullability -external-annotation-file $(ANDROID_ANNOTATIONS) \
  -external-annotation-file $(J2OBJC_ANNOTATIONS)
ifndef JAVA_8
TRANSLATE_ARGS += --patch-module java.base=$(JRE_SRC)
endif
ifdef J2OBJC_JRE_STRIP_REFLECTION
TRANSLATE_ARGS += --strip-reflection
endif
include ../make/translate.mk

FAT_LIB_SOURCE_DIRS = $(NATIVE_SOURCE_DIRS) $(GEN_OBJC_DIR)

# OBJCPP_BUILD is an optional environment variable for Objective-C++ build experiments.
# J2ObjC and app releases should not be built with this flag, nor is its use supported.
ifeq ($(OBJCPP_BUILD), YES)
FAT_LIB_COMPILE = $(CLANG) $(OBJCPPFLAGS) -I$(GEN_OBJC_DIR)
# TODO(tball): add support for Obj-C++ precompiled header.
else
FAT_LIB_COMPILE = $(CLANG) $(OBJCFLAGS) -I$(GEN_OBJC_DIR)
FAT_LIB_PRECOMPILED_HEADER = JreEmulation.h
endif

CORE_OBJS_RELATIVE = $(JAVA_SOURCES_CORE:.java=.o) $(NATIVE_JRE_SOURCES_CORE:.m=.o)
IO_OBJS_RELATIVE = $(JAVA_SOURCES_IO:.java=.o)
NET_OBJS_RELATIVE = $(JAVA_SOURCES_NET:.java=.o) $(NATIVE_JRE_SOURCES_NET:.m=.o)
UTIL_OBJS_RELATIVE = $(JAVA_SOURCES_UTIL:.java=.o)
CONCURRENT_OBJS_RELATIVE = $(JAVA_SOURCES_CONCURRENT:.java=.o)
CHANNELS_OBJS_RELATIVE = $(JAVA_SOURCES_CHANNELS:.java=.o) $(NATIVE_JRE_SOURCES_CHANNELS:.m=.o)
FILE_OBJS_RELATIVE = $(JAVA_SOURCES_FILE:.java=.o) $(NATIVE_JRE_SOURCES_FILE:.m=.o)
SECURITY_OBJS_RELATIVE = $(JAVA_SOURCES_SECURITY:.java=.o)
SSL_OBJS_RELATIVE = $(JAVA_SOURCES_SSL:.java=.o) $(NATIVE_JRE_SOURCES_SSL:.m=.o)
XML_OBJS_RELATIVE = $(JAVA_SOURCES_XML:.java=.o)
ZIP_OBJS_RELATIVE = $(JAVA_SOURCES_ZIP:.java=.o) $(NATIVE_JRE_SOURCES_ZIP:.m=.o)
SQL_OBJS_RELATIVE = $(JAVA_SOURCES_SQL:.java=.o)
BEANS_OBJS_RELATIVE = $(JAVA_SOURCES_BEANS:.java=.o)
TIME_OBJS_RELATIVE = $(JAVA_SOURCES_TIME:.java=.o)
ICU_OBJS_RELATIVE = $(JAVA_SOURCES_ICU:.java=.o) $(NATIVE_JRE_ICU_EMBEDDED_DATA:.m=.o)
JRE_OBJS_RELATIVE = $(JAVA_SOURCES:.java=.o) $(NATIVE_JRE_SOURCES:.m=.o)
ANDROID_OBJS_RELATIVE = $(JAVA_SOURCES_ANDROID:.java=.o)
JSON_OBJS_RELATIVE = $(JAVA_SOURCES_JSON:.java=.o)

include ../make/fat_lib_macros.mk
$(call emit_compile_rules,$(FAT_LIB_SOURCE_DIRS),$(FAT_LIB_COMPILE),$(FAT_LIB_PRECOMPILED_HEADER))
LIBS := $(call emit_library_rules,jre_emul,$(JRE_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_core,$(CORE_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_io,$(IO_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_net,$(NET_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_util,$(UTIL_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_concurrent,$(CONCURRENT_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_channels,$(CHANNELS_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_file,$(FILE_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_security,$(SECURITY_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_ssl,$(SSL_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_xml,$(XML_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_zip,$(ZIP_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_sql,$(SQL_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_beans,$(BEANS_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_time,$(TIME_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_icu,$(ICU_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,android_util,$(ANDROID_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,json,$(JSON_OBJS_RELATIVE))
DIST_LIBS = $(LIBS:$(ARCH_BUILD_DIR)/%=$(ARCH_LIB_DIR)/%)

# This list overlaps with EMULATION_HEADERS, but only includes those headers
# supported for use by native code.
PUBLIC_EMULATION_HEADERS = $(PUBLIC_NATIVE_HEADERS)
PUBLIC_TRANSLATED_HEADERS = $(JAVA_PUBLIC_SOURCES:.java=.h) $(JSON_PUBLIC_SOURCES:.java=.h)
INCLUDES = $(PUBLIC_TRANSLATED_HEADERS) $(PUBLIC_EMULATION_HEADERS)
DIST_INCLUDES = $(INCLUDES:%=$(DIST_INCLUDE_DIR)/%)

FRAMEWORK_NAME = JRE
FRAMEWORK_HEADERS = $(PUBLIC_EMULATION_HEADERS) $(PUBLIC_TRANSLATED_HEADERS)
STATIC_LIBRARY_NAME = $(TRANSLATE_NAME)
STATIC_HEADERS_DIR = $(ARCH_INCLUDE_DIR)
include ../make/framework.mk

JRE_EMUL_LICENSES = \
  $(DIST_LICENSE_DIR)/android_license.txt \
  $(DIST_LICENSE_DIR)/apache_harmony_license.txt \
  $(DIST_LICENSE_DIR)/apple_license.txt \
  $(DIST_LICENSE_DIR)/icu_license.txt \
  $(DIST_LICENSE_DIR)/openjdk_license.txt

fat_lib_dependencies: translate

libs: $(LIBS) $(MAIN_LIB)
	@:

xcode_build: $(DIST_LIBS) $(ARCH_BIN_DIR)/j2objcc $(ARCH_BUILD_DIR)/.includes

xcode_clean:
	@rm -rf $(GEN_OBJC_DIR) $(ARCH_BIN_DIR)/j2objcc

xcode_analyze: analyze

$(MAIN_LIB): $(MAIN_LIB_OBJS)
	@echo $(LIBTOOL) -o $@
	@$(LIBTOOL) -static -o $@ $^

dist_includes: $(BUILD_DIR)/.jre_dist_includes

$(BUILD_DIR)/.jre_dist_includes: $(PUBLIC_TRANSLATED_HEADERS:%=$(GEN_OBJC_DIR)/%) \
    $(PUBLIC_EMULATION_HEADERS:%=$(EMULATION_CLASS_DIR)/%) | $(DIST_INCLUDE_DIR)
	@echo copying jre_emul header files
	@tar cf - -C $(EMULATION_CLASS_DIR) $(PUBLIC_EMULATION_HEADERS) \
	    | tar xfp - -C $(DIST_INCLUDE_DIR)
	@tar cf - -C $(GEN_OBJC_DIR) $(PUBLIC_TRANSLATED_HEADERS) \
	    | tar xfp - -C $(DIST_INCLUDE_DIR)
	@touch $@

$(ARCH_BUILD_DIR)/.includes: $(PUBLIC_TRANSLATED_HEADERS:%=$(GEN_OBJC_DIR)/%) \
    $(PUBLIC_EMULATION_HEADERS:%=$(EMULATION_CLASS_DIR)/%) | $(ARCH_BUILD_DIR) $(ARCH_INCLUDE_DIR)
	@tar cf - -C $(EMULATION_CLASS_DIR) $(PUBLIC_EMULATION_HEADERS) \
	    | tar xfp - -C $(ARCH_INCLUDE_DIR)
	@tar cf - -C $(GEN_OBJC_DIR) $(PUBLIC_TRANSLATED_HEADERS) \
	    | tar xfp - -C $(ARCH_INCLUDE_DIR)
	@touch $@

%/j2objcc: $(J2OBJC_ROOT)/scripts/j2objcc.sh
	@mkdir -p $(@D)
	@install -C $< $@

DIRS_TO_MAKE = $(BUILD_DIR) $(ARCH_BUILD_DIR) $(ARCH_INCLUDE_DIR) \
    $(DIST_INCLUDE_DIR) $(DIST_JAR_DIR) $(DIST_LIB_DIR) $(DIST_LIB_MACOSX_DIR)
$(sort $(DIRS_TO_MAKE)):
	@mkdir -p $@

$(CLASS_DIR)/%.o: $(EMULATION_CLASS_DIR)/%.m | translate
	@mkdir -p $(@D)
	@echo clang -c $<
	@$(CLANG) $(OBJCFLAGS) -I$(EMULATION_CLASS_DIR) -I$(GEN_OBJC_DIR) $(SDK_FLAGS) -c $< -o $@

icu_data: $(ICU_EMBEDDED_RES_DIR)/$(NATIVE_JRE_ICU_EMBEDDED_DATA)
	@:

test: libs_dist $(DIST_DIR)/j2objcc
	$(MAKE) -f tests.mk all-tests

clean:
	@rm -rf $(BUILD_DIR) $(GEN_OBJC_DIR)
	@rm -f $(DIST_DIR)/j2objcc $(DIST_LIBS) $(MAIN_LIB_DIST)
	@rm -f $(DIST_INCLUDES)
	@rm -f $(JRE_EMUL_LICENSES)
	@rm -rf $(FRAMEWORK_DIR)
	@$(MAKE) -f java.mk clean

dist: jre_emul_jars_dist libs_dist $(DIST_DIR)/j2objcc licenses

jars_dist: jre_emul_jars_dist

libs_dist: archs_list libs dist_includes $(DIST_LIBS) $(MAIN_LIB_DIST)

# Log which archs will be built (phony because archs_list isn't a file).
.PHONY: archs_list
archs_list:
	@$(info building jre_emul archs: ${J2OBJC_ARCHS})

$(ARCH_LIB_DIR)/%.a: $(ARCH_BUILD_DIR)/%.a
	@mkdir -p $(@D)
	@install -m 0644 $< $@

$(MAIN_LIB_DIST): $(MAIN_LIB) | $(DIST_LIB_MACOSX_DIR)
	@install -m 0644 $< $@

define objc_sources_manifest_target
$(BUILD_DIR)/$(1).mf: $(2)
	@mkdir -p $$(@D)
	@echo "building $$$$(basename $$@)"
	@if [ -e $$@ ]; then rm $$@; fi
	@for i in $(2); do \
	  echo $(GEN_OBJC_DIR)/$$$${i%.java}.h >> $$@; \
	  echo $(GEN_OBJC_DIR)/$$$${i%.java}.m >> $$@; \
	done
endef

objc_manifest = $(eval $(call objc_sources_manifest_target,$(1),$(2))) $(BUILD_DIR)/$(1).mf

OBJC_SOURCES_MANIFEST := $(call objc_manifest,objc_sources,$(JAVA_SOURCES))

OBJC_SOURCES_MANIFESTS := \
  $(OBJC_SOURCES_MANIFEST) \
  $(call objc_manifest,objc_sources_core,$(JAVA_SOURCES_CORE)) \
  $(call objc_manifest,objc_sources_io,$(JAVA_SOURCES_IO)) \
  $(call objc_manifest,objc_sources_net,$(JAVA_SOURCES_NET)) \
  $(call objc_manifest,objc_sources_util,$(JAVA_SOURCES_UTIL)) \
  $(call objc_manifest,objc_sources_concurrent,$(JAVA_SOURCES_CONCURRENT)) \
  $(call objc_manifest,objc_sources_channels,$(JAVA_SOURCES_CHANNELS)) \
  $(call objc_manifest,objc_sources_file,$(JAVA_SOURCES_FILE)) \
  $(call objc_manifest,objc_sources_security,$(JAVA_SOURCES_SECURITY)) \
  $(call objc_manifest,objc_sources_ssl,$(JAVA_SOURCES_SSL)) \
  $(call objc_manifest,objc_sources_xml,$(JAVA_SOURCES_XML)) \
  $(call objc_manifest,objc_sources_zip,$(JAVA_SOURCES_ZIP)) \
  $(call objc_manifest,objc_sources_sql,$(JAVA_SOURCES_SQL)) \
  $(call objc_manifest,objc_sources_beans,$(JAVA_SOURCES_BEANS)) \
  $(call objc_manifest,objc_sources_time,$(JAVA_SOURCES_TIME)) \
  $(call objc_manifest,objc_sources_icu,$(JAVA_SOURCES_ICU)) \
  $(call objc_manifest,objc_sources_other,$(JAVA_SOURCES_OTHER)) \
  $(call objc_manifest,objc_sources_android,$(JAVA_SOURCES_ANDROID))

objc_sources_manifest: $(OBJC_SOURCES_MANIFEST)

all_objc_sources_manifests: $(OBJC_SOURCES_MANIFESTS)

licenses: $(JRE_EMUL_LICENSES)
	@:

# Both Android and Apache Harmony have same license.
$(DIST_LICENSE_DIR)/android_license.txt \
$(DIST_LICENSE_DIR)/apache_harmony_license.txt: apache_harmony/LICENSE
	@mkdir -p $(@D)
	@install -m 0644 $< $@

$(DIST_LICENSE_DIR)/apple_license.txt: apple_apsl/LICENSE
	@mkdir -p $(@D)
	@install -m 0644 $< $@

$(DIST_LICENSE_DIR)/icu_license.txt: icu4c/LICENSE
	@mkdir -p $(@D)
	@install -m 0644 $< $@

$(DIST_LICENSE_DIR)/openjdk_license.txt: openjdk/LICENSE
	@mkdir -p $(@D)
	@install -m 0644 $< $@
