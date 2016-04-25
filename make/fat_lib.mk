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
#   FAT_LIB_SOURCES_RELATIVE
#   FAT_LIB_SOURCE_DIRS
#   FAT_LIB_COMPILE
# The including makefile may define the following optional variables:
#   FAT_LIB_PRECOMPILED_HEADER
#   FAT_LIB_OSX_FLAGS
#
# This file defines the following to be used by the including file:
#   FAT_LIB_LIBRARY
#
# The including file may specify dependencies to compilation by adding
# prerequisites to the "fat_lib_dependencies" target.
#
# Author: Keith Stanger

include $(J2OBJC_ROOT)/make/fat_lib_macros.mk

FAT_LIB_LIBRARY = $(ARCH_BUILD_DIR)/lib$(FAT_LIB_NAME).a

FAT_LIB_PLISTS = \
  $(foreach src,$(FAT_LIB_SOURCES_RELATIVE),$(FAT_LIB_PLIST_DIR)/$(basename $(src)).plist)
FAT_LIB_OBJS = $(foreach file,$(FAT_LIB_SOURCES_RELATIVE),$(basename $(file)).o)

ifndef IS_CLEAN_GOAL

$(call emit_compile_rules,$(FAT_LIB_SOURCE_DIRS),$(FAT_LIB_COMPILE),$(FAT_LIB_PRECOMPILED_HEADER))

FAT_LIBS := $(call emit_library_rules,$(FAT_LIB_NAME),$(FAT_LIB_OBJS))

FAT_LIBS_DIST = $(FAT_LIBS:$(ARCH_BUILD_DIR)/%=$(ARCH_LIB_DIR)/%)

$(ARCH_LIB_DIR)/%.a: $(ARCH_BUILD_DIR)/%.a
	@mkdir -p $(@D)
	@install -m 0644 $< $@

analyze: $(FAT_LIB_PLISTS)
	@:

endif  # ifndef IS_CLEAN_GOAL
