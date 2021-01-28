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

J2OBJC_ROOT = ..

include ../make/common.mk
include ../make/j2objc_deps.mk
include ../java_deps/jars.mk

INCLUDE_DIR = $(BUILD_DIR)/include
JAVA_SRC_DIR = $(BUILD_DIR)/java

JUNIT_SRC_JAR = $(JAVA_DEPS_JAR_DIR)/$(JUNIT_SOURCE_JAR)
JUNIT_DATAPROVIDER_SRC_JAR = $(JAVA_DEPS_JAR_DIR)/$(JUNIT_DATAPROVIDER_SOURCE_JAR)
HAMCREST_SRC_JAR = $(JAVA_DEPS_JAR_DIR)/$(HAMCREST_SOURCE_JAR)

RUNNER_LIB = $(ARCH_BUILD_DIR)/libjunit_runner.a
RUNNER_LIB_DIST = $(ARCH_LIB_DIR)/libjunit_runner.a

# The -fobjc flags match XCode (a link fails without them because of
# missing symbols of the form OBJC_CLASS_$_[classname]).
OBJCFLAGS := -ObjC $(CC_WARNINGS) \
  -fobjc-abi-version=2 -fobjc-legacy-dispatch $(DEBUGFLAGS) \
  -I/System/Library/Frameworks/ExceptionHandling.framework/Headers

# J2ObjC settings
J2OBJC = $(DIST_DIR)/j2objc -d $(BUILD_DIR)

ifeq ("$(strip $(CLANG_ENABLE_OBJC_ARC))", "YES")
J2OBJC := $(J2OBJC) -use-arc
OBJCFLAGS := $(OBJCFLAGS) -fobjc-arc -fobjc-arc-exceptions\
 -Wno-arc-bridge-casts-disallowed-in-nonarc \
 -Xclang -fobjc-runtime-has-weak
endif
