# Common variables for the various subprojects of j2objc.
#
# The including makefile must define the variable J2OBJC_ROOT.
#
# Author: Keith Stanger

DIST_DIR = $(J2OBJC_ROOT)/dist
DIST_INCLUDE_DIR = $(DIST_DIR)/include
DIST_LIB_DIR = $(DIST_DIR)/lib
DIST_JAR_DIR = $(DIST_LIB_DIR)

ifdef CONFIGURATION_BUILD_DIR
XCODE_INCLUDE_DIR = $(CONFIGURATION_BUILD_DIR)/Headers
endif

BUILD_DIR_NAME = build_result
BUILD_DIR = $(CURDIR)/$(BUILD_DIR_NAME)

ifdef CONFIGURATION_BUILD_DIR
ARCH_BUILD_DIR = $(TARGET_TEMP_DIR)
ARCH_BIN_DIR = $(CONFIGURATION_BUILD_DIR)
ARCH_LIB_DIR = $(CONFIGURATION_BUILD_DIR)
ARCH_INCLUDE_DIR = $(CONFIGURATION_BUILD_DIR)/Headers
else
ARCH_BUILD_DIR = $(BUILD_DIR)
ARCH_BIN_DIR = $(DIST_DIR)
ARCH_LIB_DIR = $(DIST_LIB_DIR)
ARCH_INCLUDE_DIR = $(DIST_INCLUDE_DIR)
endif

ifndef GEN_OBJC_DIR
GEN_OBJC_DIR = $(BUILD_DIR)/objc
endif
ifndef GEN_JAVA_DIR
GEN_JAVA_DIR = $(BUILD_DIR)/java
endif

ifndef J2OBJC_ARCHS
J2OBJC_ARCHS = macosx iphone iphonev7s simulator
endif

# xcrun finds a specified tool in the current SDK /usr/bin directory.
XCRUN = $(shell if test -f /usr/bin/xcrun; then echo xcrun; else echo ""; fi)
MAKE = $(XCRUN) make
CLANG = $(XCRUN) clang
LIBTOOL = $(XCRUN) libtool
LIPO = $(XCRUN) lipo

ifndef CONFIGURATION_BUILD_DIR
# Determine this makefile's path.
SYSROOT_SCRIPT := $(J2OBJC_ROOT)/scripts/sysroot_path.sh
SDKROOT := $(shell bash ${SYSROOT_SCRIPT})
endif

ARCH_FLAGS = $(ARCHS:%=-arch %)
SDK_FLAGS = -isysroot $(SDKROOT)

ifeq ($(DEBUGGING_SYMBOLS), YES)
# Enable when it's decided to distribute JRE with Java source debugging.
# J2OBJC_DEBUGFLAGS = -g
DEBUGFLAGS = -DDEBUG -g
endif

ifdef GCC_OPTIMIZATION_LEVEL
OPTIMIZATION_LEVEL = $(GCC_OPTIMIZATION_LEVEL)
endif
ifdef OPTIMIZATION_LEVEL
DEBUGFLAGS := $(DEBUGFLAGS) -O$(OPTIMIZATION_LEVEL)
endif

TRANSLATOR_DEPS = $(DIST_DIR)/j2objc $(DIST_JAR_DIR)/j2objc.jar

JAVAC = javac
ifdef J2OBJC_JAVAC
JAVAC = $(J2OBJC_JAVAC)
endif

comma=,
space=$(eval) $(eval)
