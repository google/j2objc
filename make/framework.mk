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

# Provides rules for creating a static library framework using XCFramework.
#
# The including makefile must define these variables:
#   FRAMEWORK_NAME
#
# The including makefile may define these variables:
#   FRAMEWORK_RESOURCE_FILES
#   FRAMEWORK_HEADERS         (defaults to TRANSLATE_HEADERS)
#   FRAMEWORK_PUBLIC_HEADERS  (defaults to FRAMEWORK_HEADERS)
#   STATIC_LIBRARY_NAME              (defaults to FAT_LIB_NAME)
#   STATIC_HEADERS_DIR               (defaults to GEN_OBJC_DIR)
#
# This file defines the following to be used by the including file:
#   FRAMEWORK_DIR
#   FRAMEWORK_RESOURCES_DIR
#
# Author: Tom Ball

ifndef FRAMEWORK_HEADERS
ifndef TRANSLATE_HEADERS
$(error FRAMEWORK_HEADERS not defined)
else
FRAMEWORK_HEADERS = $(TRANSLATE_HEADERS)
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

ifndef FRAMEWORK_PUBLIC_HEADERS
FRAMEWORK_PUBLIC_HEADERS = $(FRAMEWORK_HEADERS)
endif

FRAMEWORK_DIR = $(DIST_FRAMEWORK_DIR)/$(FRAMEWORK_NAME).xcframework
STATIC_LIBRARY = $(BUILD_DIR)/lib$(STATIC_LIBRARY_NAME).a
FRAMEWORK_HEADER = $(BUILD_DIR)/$(FRAMEWORK_NAME).h
MODULE_MAP = $(BUILD_DIR)/module.modulemap

FRAMEWORK_RESOURCES_DIR = $(FRAMEWORK_DIR)/Versions/A/Resources
RESOURCE_FILES = $(FRAMEWORK_RESOURCE_FILES:%=$(FRAMEWORK_RESOURCES_DIR)/%)

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
VERIFY_FLAGS := -I$(FRAMEWORK_DIR)/Headers -I$(DIST_INCLUDE_DIR) \
  -Werror -Weverything $(DISALLOWED_WARNINGS)

framework: dist $(FRAMEWORK_DIR) resources
	@:

$(FRAMEWORK_DIR): $(STATIC_LIBRARY) $(FRAMEWORK_HEADER) $(MODULE_MAP)
	@echo building $(FRAMEWORK_NAME) framework
	@$(J2OBJC_ROOT)/scripts/gen_xcframework.sh $(FRAMEWORK_DIR) \
			$(BUILD_DIR)/objs-*/lib$(STATIC_LIBRARY_NAME).a;
	@mkdir -p $(FRAMEWORK_DIR)/Versions/A/Headers
	@/bin/ln -sfh A $(FRAMEWORK_DIR)/Versions/Current
	@/bin/ln -sfh Versions/Current/Headers $(FRAMEWORK_DIR)/Headers
	@tar cf - -C $(STATIC_HEADERS_DIR) $(FRAMEWORK_HEADERS:$(STATIC_HEADERS_DIR)/%=%) \
	    | tar xfp - -C $(FRAMEWORK_DIR)/Versions/A/Headers
	@install -m 0644 $(FRAMEWORK_HEADER) $(FRAMEWORK_DIR)/Versions/A/Headers
	@install -m 0644 $(MODULE_MAP) $(FRAMEWORK_DIR)/Versions/A/Headers/
	@touch $@

# Creates a framework "master" header file that includes all the framework's header files.
# This header is then test-compiled with all allowed warnings to verify it can be included
# by other projects.
$(FRAMEWORK_HEADER):
	@echo "//\n// $(FRAMEWORK_NAME).h\n//\n" > $@
	@for f in $(FRAMEWORK_PUBLIC_HEADERS:$(STATIC_HEADERS_DIR)/%=%); do\
	    echo '#include <'$${f}'>'; done >> $@

test_warnings: $(FRAMEWORK_HEADER)
	@clang -c -o $(FRAMEWORK_HEADER:%.h=%.o) $(VERIFY_FLAGS) -x objective-c -std=c11 $@
	@clang -c -o $(FRAMEWORK_HEADER:%.h=%.o) $(VERIFY_FLAGS) -x objective-c -std=c11 \
	    -fobjc-arc -fobjc-arc-exceptions $@
	@clang -c -o $(FRAMEWORK_HEADER:%.h=%.o) $(VERIFY_FLAGS) -x objective-c -std=c11 -fno-objc-arc $@
	@clang -c -o $(FRAMEWORK_HEADER:%.h=%.o) $(VERIFY_FLAGS) -x objective-c++ -std=c++11 $@
	@clang -c -o $(FRAMEWORK_HEADER:%.h=%.o) $(VERIFY_FLAGS) -x objective-c++ -std=c++11 \
	    -fobjc-arc -fobjc-arc-exceptions $@
	@clang -c -o $(FRAMEWORK_HEADER:%.h=%.o) $(VERIFY_FLAGS) -x objective-c++ -std=c++11 \
	    -fno-objc-arc $@
	@rm $(FRAMEWORK_HEADER:%.h=%.o)

$(MODULE_MAP):
	@echo "module" $(FRAMEWORK_NAME) "{" > $(MODULE_MAP)
	@echo "  umbrella header" '"'$(FRAMEWORK_NAME).h'"' >> $(MODULE_MAP)
	@echo >> $(MODULE_MAP)
	@echo "  export *" >> $(MODULE_MAP)
	@echo "  module * { export * }" >> $(MODULE_MAP)
	@echo "}" >> $(MODULE_MAP)

resources: $(RESOURCE_FILES)
	@:

$(FRAMEWORK_RESOURCES_DIR):
	@mkdir -p $(FRAMEWORK_RESOURCES_DIR)
	@/bin/ln -sfh Versions/Current/Resources $(FRAMEWORK_DIR)/Resources

$(FRAMEWORK_RESOURCES_DIR)/%: % | $(FRAMEWORK_RESOURCES_DIR)
	@mkdir -p $$(dirname $@)
	@install -m 0644 $< $@
