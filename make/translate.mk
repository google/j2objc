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
# This variable is intended for global translation flags, and so should not
# be set in Makefiles. Flags are separated by semi-colons, to avoid complex
# argument escaping in scripts.
#   TRANSLATE_GLOBAL_FLAGS
#
# The including makefile may also add dependent order-only targets by adding
# requirements to the "translate_dependencies" target.
#
# Author: Keith Stanger

include $(J2OBJC_ROOT)/make/translate_macros.mk

ifndef TRANSLATE_NAME
TRANSLATE_NAME = default
endif

TRANSLATE_HEADERS = $(TRANSLATE_JAVA_RELATIVE:%.java=$(GEN_OBJC_DIR)/%.h)
TRANSLATE_SOURCES = $(TRANSLATE_HEADERS:.h=.m)
TRANSLATE_OBJC = $(TRANSLATE_SOURCES) $(TRANSLATE_HEADERS)

ifdef TRANSLATE_OBJCPP
TRANSLATE_SOURCES = $(TRANSLATE_HEADERS:.h=.mm)
TRANSLATE_ARGS += -x objective-c++
endif

TRANSLATE_ARTIFACT := $(call emit_translate_rule,\
  $(TRANSLATE_NAME),\
  $(GEN_OBJC_DIR),\
  $(TRANSLATE_JAVA_FULL),\
  $(TRANSLATE_DEPENDENCIES),\
  $(MEMORY_MODEL_FLAG) $(TRANSLATE_ARGS))

$(TRANSLATE_OBJC): $(TRANSLATE_ARTIFACT)
	@:
