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

# Defines rules for building a fat library for distribution.
#
# The including makefile must define the variables:
#   FAT_LIB_NAME
#   FAT_LIB_SOURCES_FULL
#   FAT_LIB_SOURCES_RELATIVE
#   FAT_LIB_SOURCE_DIRS
#   FAT_LIB_COMPILE
#
# This file defines the following to be used by the including file:
#   FAT_LIB_LIBRARY
#
# Author: Keith Stanger

FAT_LIB_LIBRARY = $(ARCH_BUILD_DIR)/lib$(FAT_LIB_NAME).a
FAT_LIB_ARCH_LIBS = $(J2OBJC_ARCHS:%=$(BUILD_DIR)/%-lib$(FAT_LIB_NAME).a)

FAT_LIB_PLIST_DIR = $(BUILD_DIR)/plists
FAT_LIB_PLISTS = $(FAT_LIB_SOURCES_RELATIVE:%.m=$(FAT_LIB_PLIST_DIR)/%.plist)

FAT_LIB_MACOSX_SDK_DIR = $(shell bash $(J2OBJC_ROOT)/scripts/sysroot_path.sh)
FAT_LIB_IPHONE_SDK_DIR = $(shell bash $(J2OBJC_ROOT)/scripts/sysroot_path.sh --iphoneos)
FAT_LIB_SIMULATOR_SDK_DIR = $(shell bash $(J2OBJC_ROOT)/scripts/sysroot_path.sh --iphonesimulator)

FAT_LIB_MACOSX_FLAGS = -isysroot $(FAT_LIB_MACOSX_SDK_DIR)
FAT_LIB_IPHONE_FLAGS = -arch armv7 -isysroot $(FAT_LIB_IPHONE_SDK_DIR)
FAT_LIB_IPHONEV7S_FLAGS = -arch armv7s -isysroot $(FAT_LIB_IPHONE_SDK_DIR)
FAT_LIB_SIMULATOR_FLAGS = -arch i386 -isysroot $(FAT_LIB_SIMULATOR_SDK_DIR)
FAT_LIB_XCODE_FLAGS = $(ARCH_FLAGS) -isysroot $(SDKROOT)

arch_flags = $(strip \
  $(patsubst macosx,$(FAT_LIB_MACOSX_FLAGS),\
  $(patsubst iphone,$(FAT_LIB_IPHONE_FLAGS),\
  $(patsubst iphonev7s,$(FAT_LIB_IPHONEV7S_FLAGS),\
  $(patsubst simulator,$(FAT_LIB_SIMULATOR_FLAGS),$(1))))))

define compile_rule
$(1): $(2)
	@mkdir -p $$(@D)
	$(FAT_LIB_COMPILE) $(3) -c $$< -o $$@
endef

define analyze_rule
$(FAT_LIB_PLIST_DIR)/%.plist: $(1)/%.m
	@mkdir -p $$(@D)
	$(FAT_LIB_COMPILE) $(STATIC_ANALYZER_FLAGS) -c $$< -o $$@
endef

$(foreach src_dir,$(FAT_LIB_SOURCE_DIRS),$(eval $(call analyze_rule,$(src_dir))))

emit_general_compile_rules = $(foreach src_dir,$(FAT_LIB_SOURCE_DIRS),\
  $(eval $(call compile_rule,$(1)/%.o,$(src_dir)/%.m,$(2))))

ifdef TARGET_TEMP_DIR
emit_rename_compile_rules = \
  $(eval $(call compile_rule,$(TARGET_TEMP_DIR)/$(1),$(2),$(FAT_LIB_XCODE_FLAGS)))
else
emit_rename_compile_rules = $(foreach arch,$(J2OBJC_ARCHS),$(eval \
  $(call compile_rule,$(BUILD_DIR)/objs-$(arch)/$(1),$(2),$(call arch_flags,$(arch)))))
endif

# Builds the list of .o files, resolving duplicates by adding underscores and
# adding targets for renamed files.
FAT_LIB_OBJS :=
seen :=
get_obj_name = $(if $(filter $(1),${seen}),$(call get_obj_name,$(1)_),$(eval seen += $(1))$(1))
$(foreach file,$(FAT_LIB_SOURCES_RELATIVE),\
  $(eval src_name := $(basename $(notdir $(file))))\
  $(eval obj_name := $(call get_obj_name,$(src_name)))\
  $(eval obj_name_full := $(subst ./,,$(dir $(file)))$(obj_name).o)\
  $(eval FAT_LIB_OBJS += $(obj_name_full))\
  $(if $(filter $(obj_name),$(src_name)),,\
    $(foreach src_dir,$(FAT_LIB_SOURCE_DIRS),\
      $(eval src_name_full := $(filter $(src_dir)/$(file),$(FAT_LIB_SOURCES_FULL)))\
      $(if $(src_name_full),\
        $(call emit_rename_compile_rules,$(obj_name_full),$(src_name_full)),))))

ifdef TARGET_TEMP_DIR
# Targets specific to an xcode build

$(FAT_LIB_LIBRARY): $(FAT_LIB_OBJS:%=$(TARGET_TEMP_DIR)/%)
	@mkdir -p $(@D)
	@echo "Building $(notdir $@)"
	@$(LIBTOOL) -static -o $@ $^

$(call emit_general_compile_rules,$(TARGET_TEMP_DIR),$(FAT_LIB_XCODE_FLAGS))

else
# Targets specific to a command-line build

$(FAT_LIB_LIBRARY): $(FAT_LIB_ARCH_LIBS)
	$(LIPO) -create $^ -output $@

define arch_lib_rule
$(BUILD_DIR)/$(1)-lib$(FAT_LIB_NAME).a: $(FAT_LIB_OBJS:%=$(BUILD_DIR)/objs-$(1)/%)
	@echo "Building $$(notdir $$@)"
	@$(LIBTOOL) -static -o $$@ $$^
endef

$(foreach arch,$(J2OBJC_ARCHS),$(eval $(call arch_lib_rule,$(arch))))

$(foreach arch,$(J2OBJC_ARCHS),\
  $(call emit_general_compile_rules,$(BUILD_DIR)/objs-$(arch),$(call arch_flags,$(arch))))

endif

analyze: $(FAT_LIB_PLISTS)
	@:
