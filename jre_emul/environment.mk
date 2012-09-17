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
# The following environment variables are useful when building on
# the command-line:
#
# DEBUGGING_SYMBOLS=YES     Enable compiler's -g flag
# OPTIMIZATION_LEVEL=n      Sets compilers optimization; legal values are any
#                           -O option, such as 0-3 or s (-Os)
# WARNINGS='-Wfirst ...'    Add warning flags that aren't set by default
#                           (-Wflag-name) or turn off warnings that are set
#                           (-Wno-flag-name).
# CLANG_ENABLE_OBJC_ARC=YES Translate and build with ARC
#
# Author: Tom Ball

CWD := $(shell pwd)

APACHE_HARMONY_BASE = $(CWD)/apache_harmony/classlib/modules
JRE_ROOT = $(APACHE_HARMONY_BASE)/luni/src/main/java
JRE_ANNOTATION_ROOT = $(APACHE_HARMONY_BASE)/annotation/src/main/java
JRE_CONCURRENT_ROOT = $(APACHE_HARMONY_BASE)/concurrent/src/main/java
JRE_KERNEL_ROOT = $(APACHE_HARMONY_BASE)/luni-kernel/src/main/java
JRE_MATH_ROOT = $(APACHE_HARMONY_BASE)/math/src/main/java
JRE_NIO_ROOT = $(APACHE_HARMONY_BASE)/nio/src/main/java/common
JRE_TEST_ROOT = $(APACHE_HARMONY_BASE)/luni/src/test/api/common
JRE_MATH_TEST_ROOT = $(APACHE_HARMONY_BASE)/math/src/test/java
JRE_NIO_TEST_ROOT = $(APACHE_HARMONY_BASE)/nio/src/test/java/common
TEST_SUPPORT_ROOT = $(APACHE_HARMONY_BASE)/../support/src/test/java
MATH_TEST_SUPPORT_ROOT = $(APACHE_HARMONY_BASE)/math/src/test/java/tests/api
MISC_TEST_ROOT = $(CWD)/Tests
PROJECT_ROOT = $(CWD)/..

include ../src/main/make/detect_xcode.mk

CLASS_DIR = $(BUILD_DIR)/Classes
EMULATION_STAGE = /tmp/jre_emul
EMULATION_JAR = $(BUILD_DIR)/jre_emul.jar
EMULATION_LIB = $(BUILD_DIR)/libjre_emul.a
EMULATION_CLASS_DIR = $(CWD)/Classes
INCLUDE_DIR = $(BUILD_DIR)/include
TESTS_DIR = $(BUILD_DIR)/tests
STUBS_DIR = $(CWD)/stub_classes

JRE_SRC = $(JRE_ROOT):$(JRE_ANNOTATION_ROOT):$(JRE_CONCURRENT_ROOT):$(JRE_KERNEL_ROOT):$(JRE_MATH_ROOT):$(JRE_NIO_ROOT):$(EMULATION_CLASS_DIR)
JRE_SRC_ROOTS = $(subst :, ,$(JRE_SRC)) $(STUBS_DIR)
TEST_SRC = $(JRE_TEST_ROOT):$(JRE_MATH_TEST_ROOT):$(JRE_NIO_TEST_ROOT):$(TEST_SUPPORT_ROOT):$(MATH_TEST_SUPPORT_ROOT):$(MISC_TEST_ROOT)
vpath %.java $(JRE_SRC) $(TEST_SRC)

CLANG=clang

# J2ObjC settings
J2OBJC := USE_SYSTEM_BOOT_PATH=TRUE $(DIST_DIR)/j2objc \
   -classpath $(EMULATION_JAR) -d $(CLASS_DIR) $(J2OBJC_DEBUGFLAGS)

# GCC settings, based on Xcode log output
WARNINGS := $(WARNINGS) -Wno-trigraphs -Wunused-variable -Werror \
  -Wno-logical-op-parentheses -Wno-bitwise-op-parentheses -Wreturn-type

# Don't warn for logical/bitwise op precedence
WARNINGS := $(WARNINGS) -Wno-parentheses

# Workaround for iPhoneSimulator SDK's gcc bug
ifdef EFFECTIVE_PLATFORM_NAME
ifneq ($(EFFECTIVE_PLATFORM_NAME), -iphonesimulator)
WARNINGS := $(WARNINGS) -Wreturn-type
endif
endif

# The -fobjc flags match XCode (a link fails without them because of
# missing symbols of the form OBJC_CLASS_$_[classname]).
OBJCFLAGS := -ObjC $(WARNINGS) $(SDK_FLAGS) $(ALT_SDK_FLAGS) \
  $(ARCH_FLAGS) $(ALT_ARCH_FLAGS) \
  -fobjc-abi-version=2 -fobjc-legacy-dispatch $(DEBUGFLAGS) \
  -I/System/Library/Frameworks/ExceptionHandling.framework/Headers

ifdef CLANG_ENABLE_OBJC_ARC
J2OBJC := $(J2OBJC) -use-arc
OBJCFLAGS := $(OBJCFLAGS) -fobjc-arc -fobjc-arc-exceptions
endif
