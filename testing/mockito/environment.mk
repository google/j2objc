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

# Make include file that sets the build environment.  The external
# environment variables are defined by Xcode, allowing this build
# to be used within Xcode.
#
# Author: Tom Ball

J2OBJC_ROOT = ../..

include $(J2OBJC_ROOT)/make/common.mk
include $(J2OBJC_ROOT)/make/j2objc_deps.mk
include $(J2OBJC_ROOT)/java_deps/jars.mk

INCLUDE_DIR = $(BUILD_DIR)/include
SOURCE_BASE = src/main
EXTRACTED_JAVA_SRC_DIR = $(BUILD_DIR)/java
J2OBJC_PLUGIN_SRC_DIR = $(SOURCE_BASE)/java
CLASSES_DIR = $(BUILD_DIR)/classes

MOCKITO_SRC_JAR = $(JAVA_DEPS_JAR_DIR)/$(MOCKITO_SOURCE_JAR)
MOCKITO_JAR_PATH = $(JAVA_DEPS_JAR_DIR)/$(MOCKITO_JAR)
JUNIT_JAR_PATH = $(JAVA_DEPS_JAR_DIR)/$(JUNIT_JAR)
HAMCREST_JAR_PATH = $(JAVA_DEPS_JAR_DIR)/$(HAMCREST_JAR)

SUPERSOURCE_ROOT = src/main/java
MOCKITO_SOURCEPATH = $(SUPERSOURCE_ROOT):$(JAVA_SRC_DIR)
MOCKITO_CLASSPATH = $(MOCKITO_JAR_PATH):$(JUNIT_JAR_PATH):$(HAMCREST_JAR_PATH)

OBJCFLAGS := -ObjC $(CC_WARNINGS) $(DEBUGFLAGS)

# J2ObjC settings
J2OBJCC = $(ARCH_BIN_DIR)/j2objcc -c $(OBJCFLAGS) -I$(GEN_OBJC_DIR)

vpath %.java $(JAVA_SRC_DIR)
