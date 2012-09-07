# Copyright 2011 Google Inc.  All Rights Reserved.
#
# Make include file that detects whether or not this build was invoked through
# Xcode. BUILD_DIR is set to a directory appropriate to place temporary build
# results in (either $(TARGET_TEMP_DIR) or $(HOME)/build), and DIST_DIR is set
# to a directory where J2ObjC binaries and frameworks should be placed (or
# found, since clients of j2objc will also include this in their Makefiles).
#
# Other than those, DIST_INCLUDE_DIR and DIST_LIB_DIR are also set, for
# convenience. ARCH_FLAGS and SDK_FLAGS are set if necessary, and DEBUG_FLAGS
# are set to include either debugging flags or optimization flags, as set by
# XCode.
#
# If DEPENDENCIES is set, then it is interpreted as described in translate.mk,
# and EXTRA_CLASS_PATH is augmented appropriately.

comma=,

OS := $(shell uname -s)

ifdef CONFIGURATION_BUILD_DIR

BUILD_DIR = $(TARGET_TEMP_DIR)
DIST_DIR = $(CONFIGURATION_BUILD_DIR)
DIST_MACOS_DIR = $(SYMROOT)/$(CONFIGURATION)
DIST_INCLUDE_DIR = $(DIST_DIR)/Headers
DIST_LIB_DIR = $(DIST_DIR)
ARCH_FLAGS = $(ARCHS:%=-arch %)
SDK_FLAGS = -isysroot $(SDKROOT)

dep_build_dir = $(TEMP_ROOT)/$(word 3,$(1)).build/$(CONFIGURATION)$(EFFECTIVE_PLATFORM_NAME)/$(word 1,$(1)).build

else

BUILD_DIR_NAME = build_result
BUILD_DIR = $(CURDIR)/$(BUILD_DIR_NAME)

ifdef PROJECT_ROOT
DIST_DIR = $(PROJECT_ROOT)/dist
else
DIST_DIR = $(J2OBJC_ROOT)/dist
endif

DIST_MACOS_DIR = $(DIST_DIR)
DIST_INCLUDE_DIR = $(DIST_DIR)/include
DIST_LIB_DIR = $(DIST_DIR)/lib
ARCH_FLAGS =
SDK_FLAGS =

dep_build_dir = $(word 2,$(1))/$(BUILD_DIR_NAME)

endif


ifeq ($(DEBUGGING_SYMBOLS), YES)
# Enable when statement line numbers are supported.
# J2OBJC_DEBUGFLAGS = -g
DEBUGFLAGS = -DDEBUG -g
endif

ifdef OPTIMIZATION_LEVEL
DEBUGFLAGS := $(DEBUGFLAGS) -O$(OPTIMIZATION_LEVEL)
endif

ifdef OTHER_CFLAGS
DEBUGFLAGS := $(DEBUGFLAGS) $(OTHER_CFLAGS)
endif
