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
#   TRANSLATE_ARGS
#
# This variable is intended for use only by the jre_emul library.
#   TRANSLATE_USE_SYSTEM_BOOT_PATH
#
# The including makefile may also add dependent order-only targets by adding
# requirements to the "translate_dependencies" target.
#
# Author: Keith Stanger

TRANSLATE_SOURCES = $(TRANSLATE_JAVA_RELATIVE:%.java=$(GEN_OBJC_DIR)/%.m)
TRANSLATE_HEADERS = $(TRANSLATE_SOURCES:.m=.h)
TRANSLATE_OBJC = $(TRANSLATE_SOURCES) $(TRANSLATE_HEADERS)
TRANSLATE_TARGET = $(BUILD_DIR)/.translate_mark
TRANSLATE_LIST = $(BUILD_DIR)/.translate_list
TRANSLATE_CMD = $(TRANSLATE_EXE) -d $(GEN_OBJC_DIR) $(TRANSLATE_ARGS)

ifdef TRANSLATE_USE_SYSTEM_BOOT_PATH

TRANSLATE_EXE = USE_SYSTEM_BOOT_PATH=TRUE $(DIST_DIR)/j2objc

$(TRANSLATE_EXE): | translator
	@:

else

TRANSLATE_EXE = $(DIST_DIR)/j2objc

$(TRANSLATE_EXE): | translator_dist
	@:

endif

translate: $(TRANSLATE_TARGET)
	@:

translate_dependencies:
	@:

TRANSLATE_MAKE_LIST = $(if $(filter $(TRANSLATE_EXE),$?),\
    $(filter-out $(TRANSLATE_EXE),$^),\
    $(filter-out $(TRANSLATE_EXE),$?))

$(TRANSLATE_TARGET): $(TRANSLATE_JAVA_FULL) $(TRANSLATE_EXE) | translate_dependencies
	@echo Translating sources.
	@mkdir -p $(GEN_OBJC_DIR)
	$(call long_list_to_file,$(TRANSLATE_LIST),$(TRANSLATE_MAKE_LIST))
	@$(TRANSLATE_CMD) @$(TRANSLATE_LIST)
	@touch $@

$(TRANSLATE_OBJC): $(TRANSLATE_TARGET)
	@:
