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

# Provides rules for creating a jar file from a list of java sources.
#
# The including makefile may define the variables:
#   CREATE_JAR_NAME
#   CREATE_JAR_SOURCES
#   CREATE_JAR_RESOURCES
#   CREATE_JAR_JAVAC_ARGS
#   CREATE_JAR_DEPENDENCIES
# The including makefile may use the following variables:
#   CREATE_JAR_RESULT
#
# Author: Keith Stanger

CREATE_JAR_ARGS_FILE = $(BUILD_DIR)/.$(CREATE_JAR_NAME)_javac_args
CREATE_JAR_RESULT = $(BUILD_DIR)/$(CREATE_JAR_NAME).jar
CREATE_JAR_STAGE_DIR := $(shell echo /tmp/j2objc_$(CREATE_JAR_NAME)_$$$$)

jar: $(CREATE_JAR_RESULT)
	@:

$(CREATE_JAR_RESULT): $(CREATE_JAR_SOURCES) | $(CREATE_JAR_DEPENDENCIES)
	@mkdir -p $(@D)
	@echo "Building $(notdir $@)"
	@rm -rf $(CREATE_JAR_STAGE_DIR)
	@mkdir $(CREATE_JAR_STAGE_DIR)
	$(call long_list_to_file,$(CREATE_JAR_ARGS_FILE),$^)
	@$(JAVAC) $(CREATE_JAR_JAVAC_ARGS) -d $(CREATE_JAR_STAGE_DIR) @$(CREATE_JAR_ARGS_FILE)
	@tar cf - $(CREATE_JAR_RESOURCES) | (cd $(CREATE_JAR_STAGE_DIR) && tar xf -)
	@jar cf $@ -C $(CREATE_JAR_STAGE_DIR) .
