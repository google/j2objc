# Copyright 2011 Google Inc. All Rights Reserved.
#
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

CWD = .
PROJECT_ROOT = $(CWD)/..

include ../src/main/make/detect_xcode.mk

INCLUDE_DIR = $(BUILD_DIR)/include
SOURCE_BASE = $(CWD)/src/main
OBJC_SOURCE = $(SOURCE_BASE)/native/junit
JAVA_SRC_DIR = $(BUILD_DIR)/java

ifndef M2_HOME
M2_HOME = $(shell echo $$HOME)/.m2
endif
JUNIT_DIR = $(M2_HOME)/repository/junit/junit/4.10
JUNIT_JAR = $(JUNIT_DIR)/junit-4.10.jar
JUNIT_SRC_JAR = $(JUNIT_DIR)/junit-4.10-sources.jar

JUNIT_LIB = $(BUILD_DIR)/libjunit.a

# Compiler settings, based on Xcode log output
WARNINGS = -Wno-trigraphs -Wunused-variable -Werror -Wincompatible-pointer-types

# The -fobjc flags match XCode (a link fails without them because of
# missing symbols of the form OBJC_CLASS_$_[classname]).
OBJCFLAGS := -ObjC -std=gnu99 $(WARNINGS) $(SDK_FLAGS) $(ARCH_FLAGS) \
  -fobjc-abi-version=2 -fobjc-legacy-dispatch $(DEBUGFLAGS) \
  -I/System/Library/Frameworks/ExceptionHandling.framework/Headers

# J2ObjC settings
J2OBJC = $(DIST_DIR)/j2objc -classpath $(JUNIT_JAR) -d $(BUILD_DIR)
J2OBJCC = $(DIST_DIR)/j2objcc -c $(OBJCFLAGS) -I$(OBJC_SOURCE) -I$(BUILD_DIR)

ifdef CLANG_ENABLE_OBJC_ARC
J2OBJC := $(J2OBJC) -use-arc
OBJCFLAGS := $(OBJCFLAGS) -fobjc-arc -fobjc-arc-exceptions
endif
