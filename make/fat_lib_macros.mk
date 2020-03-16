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

# Defines macros for building fat libraries.
#
# Author: Keith Stanger

FAT_LIB_PLIST_DIR = $(BUILD_DIR)/plists

FAT_LIB_MACOSX_SDK_DIR := $(shell bash $(J2OBJC_ROOT)/scripts/sysroot_path.sh)
FAT_LIB_IPHONE_SDK_DIR := $(shell bash $(J2OBJC_ROOT)/scripts/sysroot_path.sh --iphoneos)
FAT_LIB_SIMULATOR_SDK_DIR := $(shell bash $(J2OBJC_ROOT)/scripts/sysroot_path.sh --iphonesimulator)
FAT_LIB_WATCH_SDK_DIR := $(shell bash $(J2OBJC_ROOT)/scripts/sysroot_path.sh --watchos)
FAT_LIB_WATCHSIMULATOR_SDK_DIR := $(shell bash $(J2OBJC_ROOT)/scripts/sysroot_path.sh --watchsimulator)
FAT_LIB_TV_SDK_DIR := $(shell bash $(J2OBJC_ROOT)/scripts/sysroot_path.sh --appletvos)
FAT_LIB_TVSIMULATOR_SDK_DIR := $(shell bash $(J2OBJC_ROOT)/scripts/sysroot_path.sh --appletvsimulator)

FAT_LIB_MACOSX_FLAGS = $(FAT_LIB_OSX_FLAGS) -DJ2OBJC_BUILD_ARCH=x86_64 -mmacosx-version-min=10.7 \
  -isysroot $(FAT_LIB_MACOSX_SDK_DIR)

FAT_LIB_IPHONE_FLAGS = -arch armv7 -DJ2OBJC_BUILD_ARCH=armv7 -miphoneos-version-min=5.0 \
  -isysroot $(FAT_LIB_IPHONE_SDK_DIR)
FAT_LIB_IPHONE64_FLAGS = -arch arm64 -DJ2OBJC_BUILD_ARCH=arm64 -miphoneos-version-min=5.0 \
  -isysroot $(FAT_LIB_IPHONE_SDK_DIR)
FAT_LIB_IPHONE64E_FLAGS = -arch arm64e -DJ2OBJC_BUILD_ARCH=arm64e -miphoneos-version-min=12.0 \
  -isysroot $(FAT_LIB_IPHONE_SDK_DIR)
FAT_LIB_WATCHV7K_FLAGS = -arch armv7k -DJ2OBJC_BUILD_ARCH=armv7k -mwatchos-version-min=2.0 \
  -isysroot $(FAT_LIB_WATCH_SDK_DIR)
FAT_LIB_WATCH64_FLAGS = -arch arm64_32 -DJ2OBJC_BUILD_ARCH=arm64_32 -mwatchos-version-min=2.0 \
  -isysroot $(FAT_LIB_WATCH_SDK_DIR)
FAT_LIB_WATCHSIMULATOR_FLAGS = -arch i386 -DJ2OBJC_BUILD_ARCH=i386 -mwatchos-version-min=2.0 \
  -isysroot $(FAT_LIB_WATCHSIMULATOR_SDK_DIR)
FAT_LIB_SIMULATOR_FLAGS = -arch i386 -DJ2OBJC_BUILD_ARCH=i386 -miphoneos-version-min=5.0 \
  -isysroot $(FAT_LIB_SIMULATOR_SDK_DIR)
FAT_LIB_SIMULATOR64_FLAGS = -arch x86_64 -DJ2OBJC_BUILD_ARCH=x86_64 -miphoneos-version-min=5.0 \
  -isysroot $(FAT_LIB_SIMULATOR_SDK_DIR)

FAT_LIB_TV_FLAGS = -arch arm64 -DJ2OBJC_BUILD_ARCH=arm64 -mappletvos-version-min=9.0 \
  -isysroot $(FAT_LIB_TV_SDK_DIR)
FAT_LIB_TVSIMULATOR_FLAGS = -arch x86_64 -DJ2OBJC_BUILD_ARCH=x86_64 -mappletvos-version-min=9.0 \
  -isysroot $(FAT_LIB_TVSIMULATOR_SDK_DIR)

FAT_LIB_XCODE_FLAGS = -arch $(1) -DJ2OBJC_BUILD_ARCH=$(1) -miphoneos-version-min=5.0 \
  -isysroot $(SDKROOT)

FAT_LIB_MAC_CATALYST_FLAGS = $(FAT_LIB_OSX_FLAGS) -arch x86_64 -DJ2OBJC_BUILD_ARCH=x86_64 \
  --target=x86_64-apple-ios13-macabi \
  -isysroot $(FAT_LIB_MACOSX_SDK_DIR) \
  -isystem $(FAT_LIB_MACOSX_SDK_DIR)/System/iOSSupport/usr/include \
  -iframework $(FAT_LIB_MACOSX_SDK_DIR)/System/iOSSupport/System/Library/Frameworks

# Only iPhone armv7 and arm64 builds need a bitcode marker.
ifeq ("$(XCODE_7_MINIMUM)", "YES")
FAT_LIB_IPHONE_FLAGS += -fembed-bitcode
FAT_LIB_IPHONE64_FLAGS += -fembed-bitcode
FAT_LIB_IPHONE64E_FLAGS += -fembed-bitcode
FAT_LIB_WATCHV7K_FLAGS += -fembed-bitcode
FAT_LIB_WATCH64_FLAGS += -fembed-bitcode
FAT_LIB_TV_FLAGS += -fembed-bitcode
endif

# Command-line pattern for calling libtool and filtering the "same member name"
# errors from having object files of the same name. (but in different directory)
fat_lib_filtered_libtool = set -o pipefail \
  && $(LIBTOOL) -static -no_warning_for_no_symbols -o $1 -filelist $2 2>&1 \
  | (grep -v "same member name" || true)

arch_flags = $(strip \
  $(patsubst macosx,$(FAT_LIB_MACOSX_FLAGS),\
  $(patsubst iphone,$(FAT_LIB_IPHONE_FLAGS),\
  $(patsubst iphone64,$(FAT_LIB_IPHONE64_FLAGS),\
  $(patsubst iphone64e,$(FAT_LIB_IPHONE64E_FLAGS),\
  $(patsubst watchv7k,$(FAT_LIB_WATCHV7K_FLAGS),\
  $(patsubst watch64,$(FAT_LIB_WATCH64_FLAGS),\
  $(patsubst watchsimulator,$(FAT_LIB_WATCHSIMULATOR_FLAGS),\
  $(patsubst simulator,$(FAT_LIB_SIMULATOR_FLAGS),\
  $(patsubst simulator64,$(FAT_LIB_SIMULATOR64_FLAGS),\
  $(patsubst appletvos,$(FAT_LIB_TV_FLAGS),\
  $(patsubst appletvsimulator,$(FAT_LIB_TVSIMULATOR_FLAGS),\
  $(patsubst maccatalyst,$(FAT_LIB_MAC_CATALYST_FLAGS),$(1))))))))))))))

fat_lib_dependencies:
	@:

# Generates compile rule.
# Args:
#   1: output directory
#   2: input directory
#   3: compile command
#   4: precompiled header file, or empty
#   5: other compiler flags
define compile_rule
$(1)/%.o: $(2)/%.m $(4:%=$(1)/%.pch) | fat_lib_dependencies
	@mkdir -p $$(@D)
	@echo compiling '$$<'
	@$(3) $(4:%=-include $(1)/%) $(5) -MD -c '$$<' -o '$$@'

$(1)/%.o: $(2)/%.mm $(4:%=%.pch) | fat_lib_dependencies
	@mkdir -p $$(@D)
	@echo compiling '$$<'
	@$(3) -x objective-c++ $(4:%=-include %) $(5) -MD -c '$$<' -o '$$@'
endef

# Generates rule to build precompiled headers file.
# Args:
#   1: output file name
#   2: input file
#   3: compile command
#   4: other compiler flags
define compile_pch_rule
$(1): $(2) | fat_lib_dependencies
	@mkdir -p $$(@D)
	@echo compiling '$$<'
	@$(3) -x objective-c-header $(4) -MD -c $$< -o $$@
endef

# Generates analyze rule.
# Args:
#   1: source directory
#   2: compile command
define analyze_rule
$(FAT_LIB_PLIST_DIR)/%.plist: $(1)/%.m | fat_lib_dependencies
	@mkdir -p $$(@D)
	@echo compiling '$$<'
	@$(2) $(STATIC_ANALYZER_FLAGS) -c '$$<' -o '$$@'

$(FAT_LIB_PLIST_DIR)/%.plist: $(1)/%.mm | fat_lib_dependencies
	@mkdir -p $$(@D)
	@echo compiling '$$<'
	@$(2) -x objective-c++ $(STATIC_ANALYZER_FLAGS) -c '$$<' -o '$$@'
endef

# Generates compile rules.
# Args:
#   1: list of source directories
#   2: output directory
#   3: compile command
#   4: precompiled header file, or empty
#   5: compilation flags
emit_compile_rules_for_arch = $(foreach src_dir,$(1),\
  $(eval $(call compile_pch_rule,$(2)/%.pch,$(src_dir)/%,$(3),$(5)))\
  $(eval $(call compile_rule,$(2),$(src_dir),$(3),$(4),$(5)))) \
  $(if $(4),\
    $(eval .SECONDARY: $(2)/$(4).pch) \
    $(eval -include $(2)/$(4).d),)

# Generate the library rule for a single architecture.
# Args:
#   1. Architecture specific output directory.
#   2. Library name.
#   3. Object file list (relative dirs).
define arch_lib_rule
-include $(3:%.o=$(1)/%.d)

$(1)/lib$(2).a: $(subst $$,$$$$,$(3:%=$(1)/%))
	@echo "Building $$(notdir $$@)"
	$$(call long_list_to_file,$(1)/$(2)_objs_list,$$^)
	@$$(call fat_lib_filtered_libtool,$$@,$(1)/$(2)_objs_list)
endef

# Generate the rule to create the fat library.
# Args:
#   1. Library name.
#   2. List of architecture specific libraries.
define fat_lib_rule
$(ARCH_BUILD_DIR)/lib$(1).a: $(2)
	@mkdir -p $$(@D)
	$$(LIPO) -create $$^ -output $$@
endef

# Generate the rule for the macosx library
# Args:
#   1. Library name.
define mac_lib_rule
$(ARCH_BUILD_MACOSX_DIR)/lib$(1).a: $(BUILD_DIR)/objs-macosx/lib$(1).a
	@mkdir -p $$(@D)
	install -m 0644 $$< $$@
endef

# Generate the rule for the maccatalyst library
# Args:
#   1. Library name.
define mac_catalyst_lib_rule
$(ARCH_BUILD_MAC_CATALYST_DIR)/lib$(1).a: $(BUILD_DIR)/objs-maccatalyst/lib$(1).a
	@mkdir -p $$(@D)
	install -m 0644 $$< $$@
endef

# Generate the rule for the watchos library
# Args:
#   1. Library name.
#   2. List of architecture specific libraries.
define watch_lib_rule
$(ARCH_BUILD_WATCH_DIR)/lib$(1).a: $(2)
	@mkdir -p $$(@D)
	$$(LIPO) -create $$^ -output $$@
endef

# Generate the rule for the appletv library
# Args:
#   1. Library name.
#   2. List of architecture specific libraries.
define tv_lib_rule
$(ARCH_BUILD_TV_DIR)/lib$(1).a: $(2)
	@mkdir -p $$(@D)
	$$(LIPO) -create $$^ -output $$@
endef

ifdef TARGET_TEMP_DIR
# Targets specific to an xcode build

XCODE_ARCHS = $(ARCHS)
# Xcode seems to set ARCHS incorrectly in command-line builds when the only
# active architecture setting is on. Use NATIVE_ARCH instead.
ifeq ($(ONLY_ACTIVE_ARCH), YES)
ifdef CURRENT_ARCH
XCODE_ARCHS = $(CURRENT_ARCH)
endif
endif

emit_library_rules = $(foreach arch,$(XCODE_ARCHS),\
  $(eval $(call arch_lib_rule,$(TARGET_TEMP_DIR)/$(arch),$(1),$(2)))) \
  $(eval $(call fat_lib_rule,$(1),$(XCODE_ARCHS:%=$(TARGET_TEMP_DIR)/%/lib$(1).a))) \
  $(ARCH_BUILD_DIR)/lib$(1).a

emit_arch_specific_compile_rules = $(foreach arch,$(XCODE_ARCHS),\
  $(call emit_compile_rules_for_arch,$(1),$(TARGET_TEMP_DIR)/$(arch),$(2),$(3),\
    $(call FAT_LIB_XCODE_FLAGS,$(arch))))

else
# Targets specific to a command-line build

FAT_LIB_IOS_ARCHS = $(filter-out macosx maccatalyst appletv% watch%,$(J2OBJC_ARCHS))
FAT_LIB_MAC_ARCH = $(filter macosx,$(J2OBJC_ARCHS))
FAT_LIB_WATCH_ARCHS = $(filter watch%,$(J2OBJC_ARCHS))
FAT_LIB_TV_ARCHS = $(filter appletv%,$(J2OBJC_ARCHS))
FAT_LIB_MAC_CATALYST_ARCH = $(filter maccatalyst,$(J2OBJC_ARCHS))

emit_library_rules = $(foreach arch,$(J2OBJC_ARCHS),\
  $(eval $(call arch_lib_rule,$(BUILD_DIR)/objs-$(arch),$(1),$(2)))) \
  $(if $(FAT_LIB_IOS_ARCHS),\
    $(eval $(call fat_lib_rule,$(1),$(FAT_LIB_IOS_ARCHS:%=$(BUILD_DIR)/objs-%/lib$(1).a))) \
    $(ARCH_BUILD_DIR)/lib$(1).a,) \
  $(if $(FAT_LIB_WATCH_ARCHS),\
    $(eval $(call watch_lib_rule,$(1),$(FAT_LIB_WATCH_ARCHS:%=$(BUILD_DIR)/objs-%/lib$(1).a))) \
    $(ARCH_BUILD_WATCH_DIR)/lib$(1).a,) \
  $(if $(FAT_LIB_MAC_ARCH),$(eval $(call mac_lib_rule,$(1))) $(ARCH_BUILD_MACOSX_DIR)/lib$(1).a,) \
  $(if $(FAT_LIB_MAC_CATALYST_ARCH),\
    $(eval $(call mac_catalyst_lib_rule,$(1))) $(ARCH_BUILD_MAC_CATALYST_DIR)/lib$(1).a,) \
  $(if $(FAT_LIB_TV_ARCHS),\
    $(eval $(call tv_lib_rule,$(1),$(FAT_LIB_TV_ARCHS:%=$(BUILD_DIR)/objs-%/lib$(1).a))) \
    $(ARCH_BUILD_TV_DIR)/lib$(1).a,) \

emit_arch_specific_compile_rules = $(foreach arch,$(J2OBJC_ARCHS),\
  $(call emit_compile_rules_for_arch,$(1),$(BUILD_DIR)/objs-$(arch),$(2),$(3),\
    $(call arch_flags,$(arch))))

endif

# Generate the compile and analyze rules for ObjC files.
# Args:
#   1. List of source directories.
#   2. Compile command.
#   3. Precompiled header file, or empty.
emit_compile_rules = $(call emit_arch_specific_compile_rules,$(1),$(2),$(3)) \
  $(foreach src_dir,$(1),$(eval $(call analyze_rule,$(src_dir),$(2))))
