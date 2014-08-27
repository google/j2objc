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
J2OBJC_ARCHS = macosx iphone iphone64 iphonev7s simulator
endif

# xcrun finds a specified tool in the current SDK /usr/bin directory.
XCRUN := $(shell if test -f /usr/bin/xcrun; then echo xcrun; else echo ""; fi)
# xcrun can fail when run concurrently, so we find all the tools up-front.
ifneq ($(XCRUN),)
MAKE := $(shell xcrun --find make)
CLANG := $(shell xcrun --find clang)
LIBTOOL := $(shell xcrun --find libtool)
LIPO := $(shell xcrun --find lipo)
else
MAKE = make
CLANG = clang
LIBTOOL = libtool
LIPO = lipo
endif

ifndef CONFIGURATION_BUILD_DIR
# Determine this makefile's path.
SYSROOT_SCRIPT := $(J2OBJC_ROOT)/scripts/sysroot_path.sh
SDKROOT := $(shell bash ${SYSROOT_SCRIPT})
endif

# Xcode seems to set ARCHS incorrectly in command-line builds when the only
# active architecture setting is on. Use NATIVE_ARCH instead.
ifeq ($(ONLY_ACTIVE_ARCH), YES)
ifdef NATIVE_ARCH
ARCHS = $(NATIVE_ARCH)
endif
endif
ARCH_FLAGS = $(ARCHS:%=-arch %)
SDK_FLAGS = -isysroot $(SDKROOT)

ifeq ($(DEBUGGING_SYMBOLS), YES)
# Enable when it's decided to distribute JRE with Java source debugging.
# J2OBJC_DEBUGFLAGS = -g
DEBUGFLAGS = -g
endif

ifdef GCC_OPTIMIZATION_LEVEL
OPTIMIZATION_LEVEL = $(GCC_OPTIMIZATION_LEVEL)
endif
ifndef OPTIMIZATION_LEVEL
ifdef DEBUG
OPTIMIZATION_LEVEL = 0  # None
else
OPTIMIZATION_LEVEL = s  # Fastest, smallest
endif
endif
DEBUGFLAGS := $(DEBUGFLAGS) -O$(OPTIMIZATION_LEVEL)

ifdef GCC_PREPROCESSOR_DEFINITIONS
DEBUGFLAGS += $(GCC_PREPROCESSOR_DEFINITIONS:%=-D%)
endif

TRANSLATOR_DEPS = $(DIST_DIR)/j2objc $(DIST_JAR_DIR)/j2objc.jar

JAVAC = javac
ifdef J2OBJC_JAVAC
JAVAC = $(J2OBJC_JAVAC)
endif

comma=,
space=$(eval) $(eval)

# Flags for the static analyzer.
STATIC_ANALYZER_FLAGS = \
  -Xclang -analyzer-checker -Xclang security.insecureAPI.UncheckedReturn \
  -Xclang -analyzer-checker -Xclang security.insecureAPI.getpw \
  -Xclang -analyzer-checker -Xclang security.insecureAPI.gets \
  -Xclang -analyzer-checker -Xclang security.insecureAPI.mkstemp \
  -Xclang -analyzer-checker -Xclang  security.insecureAPI.mktemp \
  -Xclang -analyzer-disable-checker -Xclang security.insecureAPI.rand \
  -Xclang -analyzer-disable-checker -Xclang security.insecureAPI.strcpy \
  -Xclang -analyzer-checker -Xclang security.insecureAPI.vfork \
  --analyze

# Avoid bash 'arument list too long' errors.
# See http://stackoverflow.com/questions/512567/create-a-file-from-a-large-makefile-variable
# TODO(iroth): When make 3.82 is available, use the $(file ...) function instead.
# See https://savannah.gnu.org/bugs/?35147
define long_list_to_file
@if [ -e $(1) ]; then rm $(1); fi
@files='$(wordlist    1, 499,$(2))' && for i in $$files; do echo $$i >> $(1); done
@files='$(wordlist  500, 999,$(2))' && for i in $$files; do echo $$i >> $(1); done
@files='$(wordlist 1000,1499,$(2))' && for i in $$files; do echo $$i >> $(1); done
@files='$(wordlist 1500,1999,$(2))' && for i in $$files; do echo $$i >> $(1); done
@files='$(wordlist 2000,2499,$(2))' && for i in $$files; do echo $$i >> $(1); done
@files='$(wordlist 2500,2999,$(2))' && for i in $$files; do echo $$i >> $(1); done
@files='$(wordlist 3000,3499,$(2))' && for i in $$files; do echo $$i >> $(1); done
@files='$(wordlist 3500,3999,$(2))' && for i in $$files; do echo $$i >> $(1); done
@files='$(wordlist 4000,4499,$(2))' && for i in $$files; do echo $$i >> $(1); done
@files='$(wordlist 4500,4999,$(2))' && for i in $$files; do echo $$i >> $(1); done
@files='$(wordlist 5000,5499,$(2))' && for i in $$files; do echo $$i >> $(1); done
@files='$(wordlist 5500,5999,$(2))' && for i in $$files; do echo $$i >> $(1); done
@files='$(wordlist 6000,6499,$(2))' && for i in $$files; do echo $$i >> $(1); done
@files='$(wordlist 6500,6999,$(2))' && for i in $$files; do echo $$i >> $(1); done
@files='$(wordlist 7000,7499,$(2))' && for i in $$files; do echo $$i >> $(1); done
@files='$(wordlist 7500,7999,$(2))' && for i in $$files; do echo $$i >> $(1); done
@files='$(wordlist 8000,8499,$(2))' && for i in $$files; do echo $$i >> $(1); done
@files='$(wordlist 8500,8999,$(2))' && for i in $$files; do echo $$i >> $(1); done
@files='$(wordlist 9000,9499,$(2))' && for i in $$files; do echo $$i >> $(1); done
@files='$(wordlist 9500,9999,$(2))' && for i in $$files; do echo $$i >> $(1); done
@if [ ! -e $(1) ]; then touch $(1); fi
endef
