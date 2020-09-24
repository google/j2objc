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
# MAX_STACK_FRAMES          The maximum number of exception stack trace frames
# NO_STACK_FRAME_SYMBOLS    If set, exception stack traces only have addresses
# GENERATE_TEST_COVERAGE    If set, adds flags to generate test coverage files.
#
# Author: Tom Ball

APACHE_HARMONY_BASE = apache_harmony/classlib/modules
JRE_BEANS_ROOT = $(APACHE_HARMONY_BASE)/beans/src/main/java
BEANS_TEST_ROOT = $(APACHE_HARMONY_BASE)/beans/src/test/java
BEANS_TEST_SUPPORT_ROOT = $(APACHE_HARMONY_BASE)/beans/src/test/support/java
ICU4C_I18N_ROOT = icu4c/i18n
ICU4C_COMMON_ROOT = icu4c/common

ANDROID_BASE = android
ANDROID_PLATFORM = android/platform
ANDROID_CORE_ROOT = $(ANDROID_BASE)/frameworks/base/core/java
ANDROID_CORE_TESTS_ROOT = $(ANDROID_BASE)/frameworks/base/core/tests/coretests/src
LIBCORE_BASE = $(ANDROID_PLATFORM)/libcore
ANDROID_JSON_ROOT = $(LIBCORE_BASE)/json/src/main/java
ANDROID_JSON_TEST_ROOT = $(LIBCORE_BASE)/json/src/test/java
ANDROID_LUNI_ROOT = $(LIBCORE_BASE)/luni/src/main/java
ANDROID_LUNI_TEST_ROOT = $(LIBCORE_BASE)/luni/src/test/java
ANDROID_TEST_SUPPORT_ROOT = $(LIBCORE_BASE)/support/src/test/java
ANDROID_APACHE_TEST_ROOT = $(LIBCORE_BASE)/harmony-tests/src/test/java
ANDROID_TESTS_RUNNER_ROOT = $(ANDROID_BASE)/frameworks/base/tests-runner/src
ANDROID_JSR166_TEST_ROOT = $(LIBCORE_BASE)/jsr166-tests/src/test/java
MOCKWEBSERVER_ROOT = $(ANDROID_PLATFORM)/external/mockwebserver/src/main/java
APACHE_COMMONS_LANG_TEST_ROOT = apache_commons_lang/src/test/java
ANDROID_DALVIK_ROOT = $(LIBCORE_BASE)/dalvik/src/main/java
ANDROID_DALVIK_TEST_ROOT = $(LIBCORE_BASE)/dalvik/src/test/java
J2OBJC_LUNI_ROOT = $(LIBCORE_BASE)/luni/src/objc/java
J2OBJC_LUNI_NATIVE = $(LIBCORE_BASE)/luni/src/objc/native
ANDROID_OPENJDK_ROOT = $(LIBCORE_BASE)/ojluni/src/main/java
ANDROID_OPENJDK_NATIVE = $(LIBCORE_BASE)/ojluni/src/main/native
ANDROID_XML_ROOT = $(LIBCORE_BASE)/xml/src/main/java
OKIO_ROOT = $(ANDROID_PLATFORM)/external/okhttp/okio/okio/src/main/java
OKIO_TEST_ROOT = $(ANDROID_PLATFORM)/external/okhttp/okio/okio/src/test/java
OJLUNI_TEST_BASE = $(LIBCORE_BASE)/ojluni/src/test
OJLUNI_NIO_TEST_ROOT = $(LIBCORE_BASE)/ojluni/src
JAVA_TIME_TEST_ROOT = $(OJLUNI_TEST_BASE)/java/time
ICU_ROOT = $(ANDROID_PLATFORM)/external/icu/android_icu4j/src/main/java
ICU_TEST_ROOT = $(ANDROID_PLATFORM)/external/icu/android_icu4j/src/main/tests
ICU_RES_DIR = $(BUILD_DIR)/icu_res
ICU_EMBEDDED_RES_DIR = $(ICU_RES_DIR)/embedded
ANDROID_PLATFORM_COMPAT_ROOT = $(ANDROID_BASE)/tools/platform-compat/java

OPENJDK_SHARE_ROOT = openjdk/src/share/classes
OPENJDK_MACOSX_ROOT = openjdk/src/macosx/classes
OPENJDK_MACOSX_NATIVE = openjdk/src/macosx/native

APPLE_ROOT = apple_apsl

MISC_TEST_ROOT = misc_tests
J2OBJC_ROOT = ..

ANDROID_INCLUDE = $(LIBCORE_BASE)/include

include ../make/common.mk
include ../make/j2objc_deps.mk
include ../java_deps/jars.mk

CLASS_DIR = $(BUILD_DIR)/Classes
EMULATION_JAR = $(BUILD_DIR)/jre_emul.jar
EMULATION_JAR_DIST = $(DIST_JAR_DIR)/jre_emul.jar
EMULATION_MODULE = $(BUILD_DIR)/jre_emul_module
EMULATION_MODULE_DIST = $(DIST_JAR_DIR)/jre_emul_module
EMULATION_SRC_JAR = $(BUILD_DIR)/jre_emul-src.jar
EMULATION_SRC_JAR_DIST = $(DIST_JAR_DIR)/jre_emul-src.jar
EMULATION_LIB_DIST = $(ARCH_LIB_DIR)/libjre_emul.a
MAIN_LIB = $(BUILD_DIR)/libj2objc_main.a
MAIN_LIB_DIST = $(DIST_LIB_MACOSX_DIR)/libj2objc_main.a
EMULATION_CLASS_DIR = Classes
TESTS_DIR = $(BUILD_DIR)/tests
RELATIVE_TESTS_DIR = $(BUILD_DIR_NAME)/tests
STUBS_DIR = stub_classes
ANDROID_NATIVE_DIR = $(LIBCORE_BASE)/luni/src/main/native
ANDROID_NATIVE_TEST_DIR = $(LIBCORE_BASE)/luni/src/test/native
LAMBDA_DIR = $(LIBCORE_BASE)/ojluni/src/lambda/java
JSON_JAR = $(BUILD_DIR)/json.jar
JSON_JAR_DIST = $(DIST_JAR_DIR)/json.jar

ifndef TRANSLATED_SOURCE_DIR
TRANSLATED_SOURCE_DIR = $(CLASS_DIR)
endif

ifdef CONFIGURATION_BUILD_DIR
RESOURCES_DEST_DIR = $(CONFIGURATION_BUILD_DIR)/$(EXECUTABLE_FOLDER_PATH)
else
RESOURCES_DEST_DIR = $(TESTS_DIR)
endif

JRE_SRC_ROOTS = \
    $(ANDROID_DALVIK_ROOT) $(ANDROID_LUNI_ROOT) \
    $(ANDROID_XML_ROOT) $(EMULATION_CLASS_DIR) \
    $(ANDROID_CORE_ROOT) $(ANDROID_JSON_ROOT) \
    $(JRE_BEANS_ROOT) $(J2OBJC_LUNI_ROOT) $(ANDROID_OPENJDK_ROOT) \
    $(OKIO_ROOT) $(LAMBDA_DIR) $(OPENJDK_SHARE_ROOT) $(OPENJDK_MACOSX_ROOT) \
    $(STUBS_DIR) $(ICU_ROOT) $(ANDROID_PLATFORM_COMPAT_ROOT)
JRE_SRC = $(subst $(eval) ,:,$(JRE_SRC_ROOTS))
TEST_SRC_ROOTS = \
    $(MISC_TEST_ROOT) $(ANDROID_TEST_SUPPORT_ROOT) \
    $(ANDROID_LUNI_TEST_ROOT) \
    $(ANDROID_APACHE_TEST_ROOT) \
    $(ANDROID_CORE_TESTS_ROOT) $(ANDROID_TESTS_RUNNER_ROOT) \
    $(ANDROID_JSON_TEST_ROOT) $(BEANS_TEST_ROOT) $(BEANS_TEST_SUPPORT_ROOT) \
    $(ANDROID_JSR166_TEST_ROOT) $(MOCKWEBSERVER_ROOT) \
    $(OKIO_TEST_ROOT) $(ANDROID_DALVIK_TEST_ROOT) \
    $(APACHE_COMMONS_LANG_TEST_ROOT) $(JAVA_TIME_TEST_ROOT) \
    $(OJLUNI_NIO_TEST_ROOT) $(ICU_TEST_ROOT)
TEST_SRC = $(subst $(eval) ,:,$(TEST_SRC_ROOTS))
vpath %.java $(JRE_SRC):$(TEST_SRC)

NATIVE_SOURCE_DIRS = $(EMULATION_CLASS_DIR) $(APPLE_ROOT) $(ANDROID_NATIVE_DIR) \
  $(ANDROID_OPENJDK_NATIVE) $(J2OBJC_LUNI_NATIVE) \
  $(ICU_EMBEDDED_RES_DIR) $(OPENJDK_MACOSX_NATIVE)

# Clang warnings
WARNINGS := $(CC_WARNINGS) $(WARNINGS) -Wno-unused-label -Wno-dangling-else \
  -Wimplicit-function-declaration -Wint-conversion -Wshorten-64-to-32

ifeq ("$(strip $(XCODE_VERSION_MAJOR))", "0500")
OBJCFLAGS += -DSET_MIN_IOS_VERSION
endif

# The -fobjc flags match XCode (a link fails without them because of
# missing symbols of the form OBJC_CLASS_$_[classname]).
OBJCFLAGS += $(WARNINGS) -fno-strict-overflow \
  -fobjc-abi-version=2 -fobjc-legacy-dispatch $(DEBUGFLAGS) \
  -I/System/Library/Frameworks/ExceptionHandling.framework/Headers \
  -I/System/Library/Frameworks/Security.framework/Headers \
  -I$(ANDROID_INCLUDE) $(NATIVE_SOURCE_DIRS:%=-I%)

# Only use the icu headers when building for OSX. For IOS these headers should
# be available in the SDK.
FAT_LIB_OSX_FLAGS = -I$(ICU4C_I18N_ROOT) -I$(ICU4C_COMMON_ROOT)

ifdef MAX_STACK_FRAMES
OBJCFLAGS += -DMAX_STACK_FRAMES=$(MAX_STACK_FRAMES)
endif

ifdef NO_STACK_FRAME_SYMBOLS
OBJCFLAGS += -DNO_STACK_FRAME_SYMBOLS=$(NO_STACK_FRAME_SYMBOLS)
endif

ifdef GENERATE_TEST_COVERAGE
OBJCFLAGS += -ftest-coverage -fprofile-arcs
endif

# Settings for classes that need to always compile without ARC.
OBJCFLAGS_NO_ARC := $(OBJCFLAGS)

OBJCPPFLAGS := $(OBJCFLAGS) -x objective-c++ -DU_SHOW_CPLUSPLUS_API=0

# Require C11 compilation to support Java volatile translation.
OBJCFLAGS += -std=c11

ifeq ("$(strip $(CLANG_ENABLE_OBJC_ARC))", "YES")
TRANSLATE_ARGS += -use-arc
OBJCFLAGS := $(OBJCFLAGS) -fobjc-arc -fobjc-arc-exceptions\
 -Wno-arc-bridge-casts-disallowed-in-nonarc \
 -Xclang -fobjc-runtime-has-weak
endif

# Specify bitcode flag if clang version 7 or greater. This is necessary to support
# iOS 9 apps that have the 'Enable bitcode' option set, which is the default for
# new apps in Xcode 7.
ifeq ("$(XCODE_7_MINIMUM)", "YES")
OBJCFLAGS += -fembed-bitcode
endif
