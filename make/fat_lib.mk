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
# The including makefile may define the following optional variables:
#   FAT_LIB_EXTENSIONS
#   FAT_LIB_PRECOMPILED_HEADER
#
# This file defines the following to be used by the including file:
#   FAT_LIB_LIBRARY
#
# The including file may specify dependencies to compilation by adding
# prerequisites to the "fat_lib_dependencies" target.
#
# Author: Keith Stanger

FAT_LIB_LIBRARY = $(ARCH_BUILD_DIR)/lib$(FAT_LIB_NAME).a
FAT_LIB_ARCH_LIBS = $(J2OBJC_ARCHS:%=$(BUILD_DIR)/%-lib$(FAT_LIB_NAME).a)

FAT_LIB_EXTENSIONS ?= m

FAT_LIB_PLIST_DIR = $(BUILD_DIR)/plists
FAT_LIB_PLISTS = \
  $(foreach src,$(FAT_LIB_SOURCES_RELATIVE),$(FAT_LIB_PLIST_DIR)/$(basename $(src)).plist)

FAT_LIB_MACOSX_SDK_DIR = $(shell bash $(J2OBJC_ROOT)/scripts/sysroot_path.sh)
FAT_LIB_IPHONE_SDK_DIR = $(shell bash $(J2OBJC_ROOT)/scripts/sysroot_path.sh --iphoneos)
FAT_LIB_SIMULATOR_SDK_DIR = $(shell bash $(J2OBJC_ROOT)/scripts/sysroot_path.sh --iphonesimulator)

FAT_LIB_MACOSX_FLAGS = -isysroot $(FAT_LIB_MACOSX_SDK_DIR)
FAT_LIB_IPHONE_FLAGS = -arch armv7 -miphoneos-version-min=5.0 -isysroot $(FAT_LIB_IPHONE_SDK_DIR)
FAT_LIB_IPHONE64_FLAGS = -arch arm64 -miphoneos-version-min=5.0 -isysroot $(FAT_LIB_IPHONE_SDK_DIR)
FAT_LIB_IPHONEV7S_FLAGS = -arch armv7s -miphoneos-version-min=5.0 -isysroot $(FAT_LIB_IPHONE_SDK_DIR)
FAT_LIB_SIMULATOR_FLAGS = -arch i386 -miphoneos-version-min=5.0 -isysroot $(FAT_LIB_SIMULATOR_SDK_DIR)
FAT_LIB_XCODE_FLAGS = $(ARCH_FLAGS) -isysroot $(SDKROOT)

ifdef FAT_LIB_PRECOMPILED_HEADER
ifndef CONFIGURATION_BUILD_DIR
J2OBJC_PRECOMPILED_HEADER = $(FAT_LIB_PRECOMPILED_HEADER)
endif
endif

ifneq ($(MAKECMDGOALS),clean)

arch_flags = $(strip \
  $(patsubst macosx,$(FAT_LIB_MACOSX_FLAGS),\
  $(patsubst iphone,$(FAT_LIB_IPHONE_FLAGS),\
  $(patsubst iphone64,$(FAT_LIB_IPHONE64_FLAGS),\
  $(patsubst iphonev7s,$(FAT_LIB_IPHONEV7S_FLAGS),\
  $(patsubst simulator,$(FAT_LIB_SIMULATOR_FLAGS),$(1)))))))

fat_lib_dependencies:
	@:

# Generates compile rule.
# Args:
#   1: output file pattern
#   2: input file pattern
#   3: precompiled header file, if J2OBJC_PRECOMPILED_HEADER is defined
#   4: precompiled header include, if J2OBJC_PRECOMPILED_HEADER is defined
#   5: other compiler flags
define compile_rule
$(1): $(2) $(3) | fat_lib_dependencies
	@mkdir -p $$(@D)
	$(FAT_LIB_COMPILE) $(4) $(5) -c $$< -o $$@
endef

# Generates rule to build precompiled headers file.
# Args:
#   1: output file name
#   2: input file
#   3: other compiler flags
define compile_pch_rule
$(1): $(2) | fat_lib_dependencies
	@mkdir -p $$(@D)
	$(FAT_LIB_COMPILE) -x objective-c-header $(3) -c $$< -o $$@
endef

# Generates analyze rule.
# Args:
#   1: input file pattern
define analyze_rule
$(FAT_LIB_PLIST_DIR)/%.plist: $(1) | fat_lib_dependencies
	@mkdir -p $$(@D)
	$(FAT_LIB_COMPILE) $(STATIC_ANALYZER_FLAGS) -c $$< -o $$@
endef

$(foreach src_dir,$(FAT_LIB_SOURCE_DIRS),$(foreach ext,$(FAT_LIB_EXTENSIONS),\
  $(eval $(call analyze_rule,$(src_dir)/%.$(ext)))))

# Generates compile rules when there aren't object file clashes.
# Args:
#   1: output directory
#   2: compilation flags
emit_general_compile_rules = $(foreach src_dir,$(FAT_LIB_SOURCE_DIRS),\
  $(eval $(call compile_pch_rule,$(1)/%.pch,$(src_dir)/%,$(2)))\
  $(foreach ext,$(FAT_LIB_EXTENSIONS),\
  $(if $(J2OBJC_PRECOMPILED_HEADER),\
  $(eval $(call compile_rule,$(1)/%.o,$(src_dir)/%.$(ext),\
    $(1)/$(J2OBJC_PRECOMPILED_HEADER).pch,\
    -include $(1)/$(J2OBJC_PRECOMPILED_HEADER),$(2))),\
  $(eval $(call compile_rule,$(1)/%.o,$(src_dir)/%.$(ext),,,$(2))))))

# Generates compile rules when there are object file clashes, appending
# underscores to .o names to make them unique.
# Args:
#   1: output file name
#   2: input file name
ifdef TARGET_TEMP_DIR
emit_rename_compile_rules = \
  $(if $(J2OBJC_PRECOMPILED_HEADER),\
  $(eval $(call compile_rule,$(TARGET_TEMP_DIR)/$(1),$(2),\
    $(TARGET_TEMP_DIR)/$(J2OBJC_PRECOMPILED_HEADER).pch,\
    -include $(TARGET_TEMP_DIR)/$(J2OBJC_PRECOMPILED_HEADER),\
    $(FAT_LIB_XCODE_FLAGS))),\
  $(eval $(call compile_rule,$(TARGET_TEMP_DIR)/$(1),$(2),,,$(FAT_LIB_XCODE_FLAGS))))
else
emit_rename_compile_rules = $(foreach arch,$(J2OBJC_ARCHS),\
  $(if $(J2OBJC_PRECOMPILED_HEADER),\
  $(eval $(call compile_rule,$(BUILD_DIR)/objs-$(arch)/$(1),$(2),\
    $(BUILD_DIR)/objs-$(arch)/$(J2OBJC_PRECOMPILED_HEADER).pch,\
    -include $(BUILD_DIR)/objs-$(arch)/$(J2OBJC_PRECOMPILED_HEADER),\
    $(call arch_flags,$(arch)))),\
  $(eval $(call compile_rule,$(BUILD_DIR)/objs-$(arch)/$(1),$(2),,,$(call arch_flags,$(arch))))))
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
$(BUILD_DIR)/$(1)-lib$(FAT_LIB_NAME).a: \
    $(J2OBJC_PRECOMPILED_HEADER:%=$(BUILD_DIR)/objs-$(1)/%.pch) \
    $(FAT_LIB_OBJS:%=$(BUILD_DIR)/objs-$(1)/%)
	@echo "Building $$(notdir $$@)"
	@$(LIBTOOL) -static -o $$@ $(FAT_LIB_OBJS:%=$(BUILD_DIR)/objs-$(1)/%)
endef

$(foreach arch,$(J2OBJC_ARCHS),$(eval $(call arch_lib_rule,$(arch))))

$(foreach arch,$(J2OBJC_ARCHS),\
  $(call emit_general_compile_rules,$(BUILD_DIR)/objs-$(arch),$(call arch_flags,$(arch))))

endif

analyze: $(FAT_LIB_PLISTS)
	@:

endif  # ifneq ($(MAKECMDGOALS),clean)
