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

# Provides rules for creating a static library framework.
#
# The including makefile must define these variables:
#   STATIC_FRAMEWORK_NAME
#
# The including makefile may define these variables:
#   STATIC_FRAMEWORK_HEADERS         (defaults to TRANSLATE_HEADERS)
#   STATIC_FRAMEWORK_PUBLIC_HEADERS  (defaults to STATIC_FRAMEWORK_HEADERS)
#   STATIC_LIBRARY_NAME              (defaults to FAT_LIB_NAME)
#   STATIC_HEADERS_DIR               (defaults to GEN_OBJC_DIR)
#
# This file defines the following to be used by the including file:
#   STATIC_FRAMEWORK_DIR
#
# Author: Tom Ball

ifndef STATIC_FRAMEWORK_HEADERS
ifndef TRANSLATE_HEADERS
$(error STATIC_FRAMEWORK_HEADERS not defined)
else
STATIC_FRAMEWORK_HEADERS = $(TRANSLATE_HEADERS)
endif
endif

ifndef STATIC_LIBRARY_NAME
ifndef FAT_LIB_NAME
$(error STATIC_LIBRARY_NAME not defined)
else
STATIC_LIBRARY_NAME = $(FAT_LIB_NAME)
endif
endif

ifndef STATIC_HEADERS_DIR
ifndef GEN_OBJC_DIR
$(error STATIC_HEADERS_DIR not defined)
else
STATIC_HEADERS_DIR = $(GEN_OBJC_DIR)
endif
endif

ifndef STATIC_FRAMEWORK_PUBLIC_HEADERS
STATIC_FRAMEWORK_PUBLIC_HEADERS = $(STATIC_FRAMEWORK_HEADERS)
endif

STATIC_FRAMEWORK_DIR = $(DIST_FRAMEWORK_DIR)/$(STATIC_FRAMEWORK_NAME).framework
STATIC_LIBRARY = $(BUILD_DIR)/lib$(STATIC_LIBRARY_NAME).a
FRAMEWORK_HEADER = $(BUILD_DIR)/$(STATIC_FRAMEWORK_NAME).h

framework: dist $(STATIC_FRAMEWORK_DIR)
	@:

$(STATIC_FRAMEWORK_DIR): $(STATIC_LIBRARY) $(FRAMEWORK_HEADER)
	@echo building $(STATIC_FRAMEWORK_NAME) framework
	@mkdir -p $(STATIC_FRAMEWORK_DIR)/Versions/A/Headers
	@/bin/ln -sfh A $(STATIC_FRAMEWORK_DIR)/Versions/Current
	@/bin/ln -sfh Versions/Current/Headers $(STATIC_FRAMEWORK_DIR)/Headers
	@/bin/ln -sfh Versions/Current/$(STATIC_FRAMEWORK_NAME) \
	    $(STATIC_FRAMEWORK_DIR)/$(STATIC_FRAMEWORK_NAME)
	@(cd $(STATIC_HEADERS_DIR) && tar cf - \
	    $(STATIC_FRAMEWORK_HEADERS:$(STATIC_HEADERS_DIR)/%=%)) \
	    | (cd $(STATIC_FRAMEWORK_DIR)/Versions/A/Headers;tar xfp -)
	@cp $(STATIC_LIBRARY) $(STATIC_FRAMEWORK_DIR)/Versions/A/$(STATIC_FRAMEWORK_NAME)
	@cp $(FRAMEWORK_HEADER) $(STATIC_FRAMEWORK_DIR)/Versions/A/Headers
	@touch $@

$(FRAMEWORK_HEADER):
	@echo "//\n// $(STATIC_FRAMEWORK_NAME).h\n//\n" > $@
	@for f in $(STATIC_FRAMEWORK_PUBLIC_HEADERS:$(STATIC_HEADERS_DIR)/%=%); do\
	    echo '#include <'$${f}'>'; done >> $@
