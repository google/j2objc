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
#   STATIC_FRAMEWORK_RESOURCE_FILES
#   STATIC_FRAMEWORK_HEADERS         (defaults to TRANSLATE_HEADERS)
#   STATIC_FRAMEWORK_PUBLIC_HEADERS  (defaults to STATIC_FRAMEWORK_HEADERS)
#   STATIC_LIBRARY_NAME              (defaults to FAT_LIB_NAME)
#   STATIC_HEADERS_DIR               (defaults to GEN_OBJC_DIR)
#
# This file defines the following to be used by the including file:
#   STATIC_FRAMEWORK_DIR
#   STATIC_FRAMEWORK_RESOURCES_DIR
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
MODULE_MAP = $(BUILD_DIR)/module.modulemap

STATIC_FRAMEWORK_RESOURCES_DIR = $(STATIC_FRAMEWORK_DIR)/Versions/A/Resources
RESOURCE_FILES = $(STATIC_FRAMEWORK_RESOURCE_FILES:%=$(STATIC_FRAMEWORK_RESOURCES_DIR)/%)

# These are warnings that are suppressed for J2ObjC headers and generated code.
#
# c++98-compat: NS_ENUM/CF_ENUM fails with this warning.
# direct-ivar-access: direct access is faster than accessor invocations.
# documentation: Javadoc formatting is more lenient than clang's.
# dollar-in-identifier-extension: follows Java inner-class naming.
# objc-interface-ivars: non-private fields are valid in Java.
# overriding-method-mismatch: supports parameterized types.
# reserved-id-macro: external headers (Apple, ICU) have header guards with leading underscores.
# super-class-method-mismatch: overridden methods with parameters with type variables are valid.
DISALLOWED_WARNINGS = \
  -Wno-c++98-compat \
  -Wno-c++98-compat-pedantic \
  -Wno-direct-ivar-access \
  -Wno-documentation \
  -Wno-documentation-unknown-command \
  -Wno-dollar-in-identifier-extension \
  -Wno-objc-interface-ivars \
  -Wno-old-style-cast \
  -Wno-overriding-method-mismatch \
  -Wno-reserved-id-macro \
  -Wno-super-class-method-mismatch

# Check that headers compile with most compiler flags.
VERIFY_FLAGS := -I$(STATIC_FRAMEWORK_DIR)/Headers -I$(DIST_INCLUDE_DIR) \
  -Werror -Weverything $(DISALLOWED_WARNINGS)

framework: dist $(STATIC_FRAMEWORK_DIR) resources
	@:

$(STATIC_FRAMEWORK_DIR): $(STATIC_LIBRARY) $(FRAMEWORK_HEADER) $(MODULE_MAP)
	@echo building $(STATIC_FRAMEWORK_NAME) framework
	@mkdir -p $(STATIC_FRAMEWORK_DIR)/Versions/A/Headers
	@/bin/ln -sfh A $(STATIC_FRAMEWORK_DIR)/Versions/Current
	@/bin/ln -sfh Versions/Current/Headers $(STATIC_FRAMEWORK_DIR)/Headers
	@/bin/ln -sfh Versions/Current/$(STATIC_FRAMEWORK_NAME) \
	    $(STATIC_FRAMEWORK_DIR)/$(STATIC_FRAMEWORK_NAME)
	@tar cf - -C $(STATIC_HEADERS_DIR) $(STATIC_FRAMEWORK_HEADERS:$(STATIC_HEADERS_DIR)/%=%) \
	    | tar xfp - -C $(STATIC_FRAMEWORK_DIR)/Versions/A/Headers
	@cp $(STATIC_LIBRARY) $(STATIC_FRAMEWORK_DIR)/Versions/A/$(STATIC_FRAMEWORK_NAME)
	@install -m 0644 $(FRAMEWORK_HEADER) $(STATIC_FRAMEWORK_DIR)/Versions/A/Headers
	@install -m 0644 $(MODULE_MAP) $(STATIC_FRAMEWORK_DIR)/Versions/A/Headers/
	@touch $@

# Creates a framework "master" header file that includes all the framework's header files.
# This header is then test-compiled with all allowed warnings to verify it can be included
# by other projects.
$(FRAMEWORK_HEADER):
	@echo "//\n// $(STATIC_FRAMEWORK_NAME).h\n//\n" > $@
	@for f in $(STATIC_FRAMEWORK_PUBLIC_HEADERS:$(STATIC_HEADERS_DIR)/%=%); do\
	    echo '#include <'$${f}'>'; done >> $@

test_warnings: $(FRAMEWORK_HEADER)
	@clang -c -o $(FRAMEWORK_HEADER:%.h=%.o) $(VERIFY_FLAGS) -x objective-c -std=c11 $@
	@clang -c -o $(FRAMEWORK_HEADER:%.h=%.o) $(VERIFY_FLAGS) -x objective-c -std=c11 -fobjc-arc $@
	@clang -c -o $(FRAMEWORK_HEADER:%.h=%.o) $(VERIFY_FLAGS) -x objective-c -std=c11 -fno-objc-arc $@
	@clang -c -o $(FRAMEWORK_HEADER:%.h=%.o) $(VERIFY_FLAGS) -x objective-c++ -std=c++11 $@
	@clang -c -o $(FRAMEWORK_HEADER:%.h=%.o) $(VERIFY_FLAGS) -x objective-c++ -std=c++11 -fobjc-arc $@
	@clang -c -o $(FRAMEWORK_HEADER:%.h=%.o) $(VERIFY_FLAGS) -x objective-c++ -std=c++11 \
	    -fno-objc-arc $@
	@rm $(FRAMEWORK_HEADER:%.h=%.o)

$(MODULE_MAP):
	@echo "module" $(STATIC_FRAMEWORK_NAME) "{" > $(MODULE_MAP)
	@echo "  umbrella header" '"'$(STATIC_FRAMEWORK_NAME).h'"' >> $(MODULE_MAP)
	@echo >> $(MODULE_MAP)
	@echo "  export *" >> $(MODULE_MAP)
	@echo "  module * { export * }" >> $(MODULE_MAP)
	@echo "}" >> $(MODULE_MAP)

resources: $(RESOURCE_FILES)
	@:

$(STATIC_FRAMEWORK_RESOURCES_DIR):
	@mkdir -p $(STATIC_FRAMEWORK_RESOURCES_DIR)
	@/bin/ln -sfh Versions/Current/Resources $(STATIC_FRAMEWORK_DIR)/Resources

$(STATIC_FRAMEWORK_RESOURCES_DIR)/%: % | $(STATIC_FRAMEWORK_RESOURCES_DIR)
	@mkdir -p $$(dirname $@)
	@install -m 0644 $< $@
