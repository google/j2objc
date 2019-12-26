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

# Builds the j2objc annotations as a .jar file.
#
# Author: Tom Ball, Keith Stanger

SUFFIXES:

J2OBJC_ROOT = ..

include ../make/common.mk
include ../java_deps/jars.mk

BUILD_JAR = $(BUILD_DIR)/Procyon.core/libs/procyon-core-0.5.36.jar $(BUILD_DIR)/Procyon.CompilerTools/libs/procyon-compilertools-0.5.36.jar
DIST_JAR = $(PROCYON_JARS:%=$(JAVA_DEPS_JAR_DIR)/%)

CLASSES_DIR = bin

$(BUILD_JAR) : 
	@echo Building procyon 
	@gradle --quiet jar 

$(DIST_JAR): $(BUILD_JAR)
	@cp -f $(BUILD_DIR)/Procyon.core/libs/procyon-core-0.5.36.jar $(JAVA_DEPS_JAR_DIR)/
	@cp -f $(BUILD_DIR)/Procyon.CompilerTools/libs/procyon-compilertools-0.5.36.jar $(JAVA_DEPS_JAR_DIR)/

dist: $(DIST_JAR)
	@:

clean:
	@rm -rf $(BUILD_DIR) $(DIST_JAR)

: