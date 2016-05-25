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
.PHONY: default clean

include environment.mk
include java_sources.mk

# Native obj-c sources that do not emulate a specific java class.
IOS_PUBLIC_OBJS = \
	IOSArray.o \
	IOSClass.o \
	IOSObjectArray.o \
	IOSPrimitiveArray.o \
	IOSReflection.o \
	J2ObjC_common.o \
	JreRetainedWith.o \
	NSCopying+JavaCloneable.o \
	NSDataInputStream.o \
	NSDataOutputStream.o \
	NSDictionaryMap.o \
	NSException+JavaThrowable.o \
	NSNumber+JavaNumber.o \
	NSObject+JavaObject.o \
	NSString+JavaString.o \
	java/lang/reflect/ExecutableMember.o \
	java/lang/reflect/TypeVariableImpl.o \
	jni.o

IOS_PRIVATE_OBJS = \
	cbigint.o \
	DebugUtils.o \
	FastPointerLookup.o \
	IOSArrayClass.o \
	IOSConcreteClass.o \
	IOSLocaleData.o \
	IOSMappedClass.o \
	IOSPrimitiveClass.o \
	IOSProtocolClass.o \
	IOSProxyClass.o \
	IOSReference.o \
	J2ObjC_icu.o \
	JavaToIOSInputStreamAdapter.o \
	java_lang_IntegralToString.o \
	java_lang_RealToString.o \
	java_lang_StringToReal.o \
	java_util_regex_Matcher.o \
	java_util_regex_Pattern.o \

IOS_OBJS = $(IOS_PUBLIC_OBJS) $(IOS_PRIVATE_OBJS)

APPLE_SRCS = \
	objc-sync.m

# Java classes with hand written obj-c implementations. Shouldn't be translated,
# but need to include the .java file in jre_emul.jar and the .m file in
# libjre_emul.a.
IOS_JAVA_OBJS = \
	java/lang/AbstractStringBuilder.o \
	java/lang/Iterable.o \
	java/lang/reflect/AccessibleObject.o \
	java/lang/reflect/Constructor.o \
	java/lang/reflect/Field.o \
	java/lang/reflect/Method.o \
	javax/annotation/Resource.o

# Java sources that should not be translated but should be included in the jar.
JAR_ONLY_SOURCES = \
	java/lang/Cloneable.java

MAIN_OBJ = J2ObjCMain.o

JAVA_SOURCES_MANIFEST = $(BUILD_DIR)/java_sources.mf

STUB_JAVA_SOURCES := $(shell find $(STUBS_DIR) $(INVOKE_STUBS_DIR) -name *.java)
ALL_JAVA_SOURCES = $(JAVA_SOURCES) $(IOS_JAVA_OBJS:.o=.java) $(JAR_ONLY_SOURCES)
JAVA_TO_TRANSLATE = $(JAVA_SOURCES)
EMULATION_HEADERS := $(shell find $(EMULATION_CLASS_DIR) -name *.h)
INCLUDES = $(JAVA_TO_TRANSLATE:.java=.h) $(EMULATION_HEADERS:$(EMULATION_CLASS_DIR)/%=%)
DIST_INCLUDES = $(INCLUDES:%=$(DIST_INCLUDE_DIR)/%)
ARCH_INCLUDES = $(INCLUDES:%=$(ARCH_INCLUDE_DIR)/%)

ANNOTATIONS_JAR = $(DIST_JAR_DIR)/j2objc_annotations.jar

MAIN_LIB_OBJS = $(MAIN_OBJ:%=$(CLASS_DIR)/%)

# translate is the default so that Xcode can build its own object files
default: libs
	@: # suppress make's "nothing to be done" message

GEN_OBJC_DIR = $(TRANSLATED_SOURCE_DIR)
TRANSLATE_NAME = jre_emul
TRANSLATE_JAVA_FULL = $(JAVA_TO_TRANSLATE)
TRANSLATE_JAVA_RELATIVE = $(JAVA_TO_TRANSLATE)
TRANSLATE_ARGS = -q -sourcepath $(JRE_SRC) -classpath $(EMULATION_JAR) \
  $(J2OBJC_DEBUGFLAGS) -encoding UTF-8 --batch-translate-max=1200 --doc-comments \
  -J-Xmx2G
TRANSLATE_USE_SYSTEM_BOOT_PATH = TRUE
include ../make/translate.mk

FAT_LIB_SOURCE_DIRS = $(EMULATION_CLASS_DIR) $(GEN_OBJC_DIR) $(APPLE_ROOT) $(ANDROID_NATIVE_DIR)

# OBJCPP_BUILD is an optional environment variable for Objective-C++ build experiments.
# J2ObjC and app releases should not be built with this flag, nor is its use supported.
ifeq ($(OBJCPP_BUILD), YES)
FAT_LIB_COMPILE = $(CLANG) $(OBJCPPFLAGS) -I$(EMULATION_CLASS_DIR) -I$(GEN_OBJC_DIR) \
  -I$(ANDROID_NATIVE_DIR)
# TODO(tball): add support for Obj-C++ precompiled header.
else
FAT_LIB_COMPILE = $(CLANG) $(OBJCFLAGS) -I$(EMULATION_CLASS_DIR) -I$(GEN_OBJC_DIR) \
  -I$(ANDROID_NATIVE_DIR)
FAT_LIB_PRECOMPILED_HEADER = JreEmulation.h
endif

NATIVE_OBJS_RELATIVE = $(IOS_OBJS) $(IOS_JAVA_OBJS) $(APPLE_SRCS:.m=.o)
CORE_OBJS_RELATIVE = $(JAVA_SOURCES_CORE:.java=.o) $(NATIVE_OBJS_RELATIVE)
IO_OBJS_RELATIVE = $(JAVA_SOURCES_IO:.java=.o)
NET_OBJS_RELATIVE = $(JAVA_SOURCES_NET:.java=.o)
UTIL_OBJS_RELATIVE = $(JAVA_SOURCES_UTIL:.java=.o)
CONCURRENT_OBJS_RELATIVE = $(JAVA_SOURCES_CONCURRENT:.java=.o)
CHANNELS_OBJS_RELATIVE = $(JAVA_SOURCES_CHANNELS:.java=.o)
SECURITY_OBJS_RELATIVE = $(JAVA_SOURCES_SECURITY:.java=.o)
SSL_OBJS_RELATIVE = $(JAVA_SOURCES_SSL:.java=.o)
XML_OBJS_RELATIVE = $(JAVA_SOURCES_XML:.java=.o)
ZIP_OBJS_RELATIVE = $(JAVA_SOURCES_ZIP:.java=.o)
SQL_OBJS_RELATIVE = $(JAVA_SOURCES_SQL:.java=.o)
BEANS_OBJS_RELATIVE = $(JAVA_SOURCES_BEANS:.java=.o)
JRE_OBJS_RELATIVE = $(JAVA_SOURCES:.java=.o) $(NATIVE_OBJS_RELATIVE)
ANDROID_OBJS_RELATIVE = $(JAVA_SOURCES_ANDROID:.java=.o)

include ../make/fat_lib_macros.mk
$(call emit_compile_rules,$(FAT_LIB_SOURCE_DIRS),$(FAT_LIB_COMPILE),$(FAT_LIB_PRECOMPILED_HEADER))
LIBS := $(call emit_library_rules,jre_emul,$(JRE_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_core,$(CORE_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_io,$(IO_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_net,$(NET_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_util,$(UTIL_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_concurrent,$(CONCURRENT_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_channels,$(CHANNELS_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_security,$(SECURITY_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_ssl,$(SSL_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_xml,$(XML_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_zip,$(ZIP_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_sql,$(SQL_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,jre_beans,$(BEANS_OBJS_RELATIVE))
LIBS += $(call emit_library_rules,android_util,$(ANDROID_OBJS_RELATIVE))
DIST_LIBS = $(LIBS:$(ARCH_BUILD_DIR)/%=$(ARCH_LIB_DIR)/%)

# This list overlaps with EMULATION_HEADERS, but only includes those headers
# supported for use by native code.
PUBLIC_EMULATION_HEADERS = \
    J2ObjC_header.h \
    J2ObjC_source.h \
    J2ObjC_types.h \
    JavaObject.h \
    JreEmulation.h \
    $(IOS_PUBLIC_OBJS:.o=.h) \
    $(IOS_JAVA_OBJS:.o=.h)
PUBLIC_TRANSLATED_HEADERS = $(JAVA_PUBLIC_SOURCES:.java=.h)

STATIC_FRAMEWORK_NAME = JRE
STATIC_FRAMEWORK_HEADERS = $(PUBLIC_EMULATION_HEADERS) $(PUBLIC_TRANSLATED_HEADERS)
STATIC_LIBRARY_NAME = $(TRANSLATE_NAME)
STATIC_HEADERS_DIR = $(ARCH_INCLUDE_DIR)
include ../make/static_framework.mk

JRE_EMUL_LICENSES = \
  $(DIST_LICENSE_DIR)/android_license.txt \
  $(DIST_LICENSE_DIR)/apache_harmony_license.txt \
  $(DIST_LICENSE_DIR)/apple_license.txt \
  $(DIST_LICENSE_DIR)/icu_license.txt

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
	@tar cf - -C $(EMULATION_CLASS_DIR) $(PUBLIC_EMULATION_HEADERS:$(EMULATION_CLASS_DIR)/%=%) \
	    | tar xfp - -C $(DIST_INCLUDE_DIR)
	@tar cf - -C $(GEN_OBJC_DIR) $(PUBLIC_TRANSLATED_HEADERS:$(GEN_OBJC_DIR)/%=%) \
	    | tar xfp - -C $(DIST_INCLUDE_DIR)
	@touch $@

$(ARCH_BUILD_DIR)/.includes: $(TRANSLATE_HEADERS) $(EMULATION_HEADERS) | \
    $(ARCH_BUILD_DIR) $(ARCH_INCLUDE_DIR)
	@tar cf - -C $(EMULATION_CLASS_DIR) $(EMULATION_HEADERS:$(EMULATION_CLASS_DIR)/%=%) \
	    | tar xfp - -C $(ARCH_INCLUDE_DIR)
	@tar cf - -C $(GEN_OBJC_DIR) $(TRANSLATE_HEADERS:$(GEN_OBJC_DIR)/%=%) \
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

test: libs_dist $(DIST_DIR)/j2objcc
	$(MAKE) -f tests.mk all-tests

clean:
	@rm -rf $(BUILD_DIR) $(GEN_OBJC_DIR)
	@rm -f $(DIST_DIR)/j2objcc $(EMULATION_JAR_DIST) $(DIST_LIBS) $(MAIN_LIB_DIST)
	@rm -f $(DIST_INCLUDES)
	@rm -f $(JRE_EMUL_LICENSES)
	@rm -rf $(STATIC_FRAMEWORK_DIR)

dist: emul_jar_dist libs_dist $(DIST_DIR)/j2objcc licenses emul_src_jar_dist

libs_dist: libs dist_includes $(DIST_LIBS) $(MAIN_LIB_DIST)

$(ARCH_LIB_DIR)/%.a: $(ARCH_BUILD_DIR)/%.a
	@mkdir -p $(@D)
	@install -m 0644 $< $@

$(MAIN_LIB_DIST): $(MAIN_LIB) | $(DIST_LIB_MACOSX_DIR)
	@install -m 0644 $< $@

java: emul_jar_dist java_sources_manifest

emul_jar_dist: $(EMULATION_JAR_DIST)
	@:

emul_src_jar_dist: $(EMULATION_SRC_JAR_DIST)
	@:

$(EMULATION_JAR_DIST): $(EMULATION_JAR) | $(DIST_JAR_DIR)
	@install -m 0644 $< $@

$(EMULATION_SRC_JAR_DIST): $(EMULATION_SRC_JAR) | $(DIST_JAR_DIR)
	@install -m 0644 $< $@

$(EMULATION_JAR): $(ALL_JAVA_SOURCES) $(STUB_JAVA_SOURCES) $(SOURCE_RETENTION_ANNOTATIONS) \
	  | $(BUILD_DIR)
	@echo "building jre_emul.jar"
	@stage_dir=`mktemp -d -t j2objc-jre_emul`; \
    ../scripts/javac_no_deprecated_warnings.sh $(JAVAC) \
      -bootclasspath $(STUBS_DIR):$(INVOKE_STUBS_DIR) \
	    -classpath $(ANNOTATIONS_JAR) \
	    -d $$stage_dir -encoding UTF-8 -source 1.8 -target 1.8 -nowarn $^; \
	jar cf $(EMULATION_JAR) -C $$stage_dir .; \
	rm -rf $$stage_dir

$(EMULATION_SRC_JAR): $(ALL_JAVA_SOURCES) $(STUB_JAVA_SOURCES) | $(BUILD_DIR)
	@echo "building jre_emul-src.jar"
	@../scripts/gen_java_source_jar.py -sourcepath $(JRE_SRC):$(STUBS_DIR):$(INVOKE_STUBS_DIR) \
	  -o $(EMULATION_SRC_JAR) $^

$(JAVA_SOURCES_MANIFEST): $(ALL_JAVA_SOURCES) $(STUB_JAVA_SOURCES) | $(BUILD_DIR)
	@echo "building $$(basename $@)"
	@if [ -e $@ ]; then rm $@; fi
	@for i in $^; do echo $(CURDIR)/$$i >> $@; done

java_sources_manifest: $(JAVA_SOURCES_MANIFEST)
	@:

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
  $(call objc_manifest,objc_sources_security,$(JAVA_SOURCES_SECURITY)) \
  $(call objc_manifest,objc_sources_ssl,$(JAVA_SOURCES_SSL)) \
  $(call objc_manifest,objc_sources_xml,$(JAVA_SOURCES_XML)) \
  $(call objc_manifest,objc_sources_zip,$(JAVA_SOURCES_ZIP)) \
  $(call objc_manifest,objc_sources_sql,$(JAVA_SOURCES_SQL)) \
  $(call objc_manifest,objc_sources_beans,$(JAVA_SOURCES_BEANS)) \
  $(call objc_manifest,objc_sources_other,$(JAVA_SOURCES_OTHER)) \
  $(call objc_manifest,objc_sources_android,$(JAVA_SOURCES_ANDROID))

objc_sources_manifest: $(OBJC_SOURCES_MANIFEST)

all_objc_sources_manifests: $(OBJC_SOURCES_MANIFESTS)

find_cycles: cycle_finder_dist $(JAVA_SOURCES_MANIFEST)
	$(DIST_DIR)/cycle_finder -source 1.8 -w cycle_whitelist.txt `cat $(JAVA_SOURCES_MANIFEST)`

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

