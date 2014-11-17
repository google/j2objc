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

# Defines the "translate" target.
#
# The including makefile may define the variables:
#   TRANSLATE_JAVA_FULL
#   TRANSLATE_JAVA_RELATIVE
# And optional variables:
#   TRANSLATE_NAME
#   TRANSLATE_ARGS
#   TRANSLATE_OBJCPP
#   TRANSLATE_DEPENDENCIES
#
# This variable is intended for use only by the jre_emul library.
#   TRANSLATE_USE_SYSTEM_BOOT_PATH
#
# The including makefile may also add dependent order-only targets by adding
# requirements to the "translate_dependencies" target.
#
# Author: Keith Stanger

TRANSLATE_HEADERS = $(TRANSLATE_JAVA_RELATIVE:%.java=$(GEN_OBJC_DIR)/%.h)
TRANSLATE_SOURCES = $(TRANSLATE_HEADERS:.h=.m)
TRANSLATE_OBJC = $(TRANSLATE_SOURCES) $(TRANSLATE_HEADERS)
TRANSLATE_TARGET = $(GEN_OBJC_DIR)/.translate_mark
TRANSLATE_LIST = $(GEN_OBJC_DIR)/.translate_list
TRANSLATE_EXE = $(DIST_DIR)/j2objc
TRANSLATE_CMD = $(TRANSLATE_EXE) -d $(GEN_OBJC_DIR) $(TRANSLATE_ARGS)
TRANSLATE_EXE_DEP = translator_dist

ifdef TRANSLATE_USE_SYSTEM_BOOT_PATH
TRANSLATE_CMD := USE_SYSTEM_BOOT_PATH=TRUE $(TRANSLATE_CMD)
TRANSLATE_EXE_DEP = translator
endif

ifdef TRANSLATE_OBJCPP
TRANSLATE_SOURCES = $(TRANSLATE_HEADERS:.h=.mm)
TRANSLATE_CMD += -x objective-c++
endif

$(TRANSLATE_EXE): | $(TRANSLATE_EXE_DEP)
	@:

translate: $(TRANSLATE_TARGET)
	@:

translate_dependencies:
	@:

TRANSLATE_NON_JAVA_PREREQ = $(TRANSLATE_EXE) $(TRANSLATE_DEPENDENCIES) translate_force

# Resolved sources within the translate target.
TRANSLATE_JAVA_PREREQ = $(filter-out $(TRANSLATE_NON_JAVA_PREREQ) translate_force,$^)

# Find any files that may have been added to the list since the last translation
TRANSLATE_LAST_FILES := $(shell if [ -e $(TRANSLATE_TARGET) ]; then cat $(TRANSLATE_TARGET); fi)
TRANSLATE_NEW_FILES = $(filter-out $(TRANSLATE_LAST_FILES),$(TRANSLATE_JAVA_PREREQ))

TRANSLATE_MAKE_LIST = $(if $(filter $(TRANSLATE_NON_JAVA_PREREQ),$?),\
    $(TRANSLATE_JAVA_PREREQ),$(filter $? $(TRANSLATE_NEW_FILES),$(TRANSLATE_JAVA_PREREQ)))

$(TRANSLATE_TARGET): $(TRANSLATE_JAVA_FULL) $(TRANSLATE_NON_JAVA_PREREQ) | translate_dependencies
	@mkdir -p $(GEN_OBJC_DIR)
	$(call long_list_to_file,$(TRANSLATE_LIST),$(TRANSLATE_MAKE_LIST))
	@if [ -s $(TRANSLATE_LIST) ]; then \
	  echo translating $(TRANSLATE_NAME) sources; \
	  $(TRANSLATE_CMD) @$(TRANSLATE_LIST); \
	fi
	$(call long_list_to_file,$@,$(TRANSLATE_JAVA_PREREQ))

translate_force:
	@:

$(TRANSLATE_OBJC): $(TRANSLATE_TARGET)
	@:
