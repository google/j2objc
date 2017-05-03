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

# Defines the "emit_translate_rule" macro.
#
# This variable is intended for use only by the jre_emul library.
#   TRANSLATE_USE_SYSTEM_BOOT_PATH
#
# This variable is intended for global translation flags, and so should not
# be set in Makefiles. Flags are separated by semi-colons, to avoid complex
# argument escaping in scripts.
#   TRANSLATE_GLOBAL_FLAGS
#
# The including makefile may also add dependent order-only targets by adding
# requirements to the "translate_dependencies" target.
#
# Author: Keith Stanger

TRANSLATE_EXE = $(DIST_DIR)/j2objc
TRANSLATE_CMD = $(TRANSLATE_EXE) $(subst ;, ,$(TRANSLATE_GLOBAL_FLAGS))
TRANSLATE_EXE_DEP = translator_dist

$(TRANSLATE_EXE): $(TRANSLATE_EXE_DEP)
	@:

TRANSLATE_TARGET = $(strip $(2))/.translate_mark_$(strip $(1))
TRANSLATE_LIST = $(strip $(2))/.translate_list_$(strip $(1))

TRANSLATE_NON_JAVA_PREREQ = $(filter-out %.java,$^)
# Resolved sources within the translate target.
TRANSLATE_JAVA_PREREQ = $(filter %.java,$^)

# Find any files that may have been added to the list since the last translation
TRANSLATE_LAST_FILES = $(shell if [ -e $@ ]; then cat $@; fi)
TRANSLATE_NEW_FILES = $(filter-out $(TRANSLATE_LAST_FILES),$(TRANSLATE_JAVA_PREREQ))

TRANSLATE_MAKE_LIST = $(if $(filter $(TRANSLATE_NON_JAVA_PREREQ),$?),\
    $(TRANSLATE_JAVA_PREREQ),$(filter $?,$(TRANSLATE_JAVA_PREREQ)) $(TRANSLATE_NEW_FILES))

translate_force:
	@:

translate_dependencies:
	@:

translate:
	@:

# Generates the rule to translate Java to Objective-C
# Args:
#   1: The name of this translate rule.
#   2: The output directory.
#   3: The full list of fully qualified Java source files.
#   4: Other prerequisites.
#   5: Translation arguments.
#   6: Already translated files (from the previous execution).
define translate_rule
$(TRANSLATE_TARGET): $(subst $$,$$$$,$(3)) $(TRANSLATE_EXE) $4 translate_force \
    | translate_dependencies
	@mkdir -p $(2)
	$$(call long_list_to_file,$(TRANSLATE_LIST),$$(TRANSLATE_MAKE_LIST))
	@if [ -s $(TRANSLATE_LIST) ]; then \
	  echo translating $(1) sources; \
	  $(TRANSLATE_CMD) -d $(2) $(5) @$(TRANSLATE_LIST); \
	fi
	$$(call long_list_to_file,$$@,$$(TRANSLATE_JAVA_PREREQ))

translate: $(TRANSLATE_TARGET)
endef

# Generates the rule to translate Java to Objective-C
# Args:
#   1: The name of this translate rule.
#   2: The output directory.
#   3: The full list of fully qualified Java source files.
#   4: Other prerequisites.
#   5: Translation arguments.
emit_translate_rule = \
  $(eval $(call translate_rule,$(1),$(2),$(3),$(4),$(5)))$(TRANSLATE_TARGET)
