# Common variables for the various subprojects of j2objc.
#
# The including makefile must define the variable J2OBJC_ROOT.
#
# Author: Keith Stanger

DIST_DIR = $(J2OBJC_ROOT)/dist
DIST_INCLUDE_DIR = $(DIST_DIR)/include
DIST_LIB_DIR = $(DIST_DIR)/lib
DIST_JAR_DIR = $(DIST_LIB_DIR)
DIST_LICENSE_DIR = $(DIST_DIR)/third_party_licenses
DIST_FRAMEWORK_DIR = $(DIST_DIR)/frameworks

# Release version string used by j2objc and cycle_finder's -version flag.
ifndef J2OBJC_VERSION
CURR_DATE = $(shell date "+%Y/%m/%d")
J2OBJC_VERSION = $(USER)-$(CURR_DATE)
endif

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
ARCH_LIB_MACOSX_DIR = $(DIST_LIB_MACOSX_DIR)
ARCH_INCLUDE_DIR = $(DIST_INCLUDE_DIR)
endif

# Macosx library dirs.
ARCH_BUILD_MACOSX_DIR = $(ARCH_BUILD_DIR)/macosx
ARCH_LIB_MACOSX_DIR = $(ARCH_LIB_DIR)/macosx
DIST_LIB_MACOSX_DIR = $(DIST_LIB_DIR)/macosx

# Watchos library dirs.
ARCH_BUILD_WATCH_DIR = $(ARCH_BUILD_DIR)/watchos
ARCH_LIB_WATCH_DIR = $(ARCH_LIB_DIR)/watchos
DIST_LIB_WATCH_DIR = $(DIST_LIB_DIR)/watchos

# Appletv library dirs.
ARCH_BUILD_TV_DIR = $(ARCH_BUILD_DIR)/appletvos
ARCH_LIB_TV_DIR = $(ARCH_LIB_DIR)/appletvos
DIST_LIB_TV_DIR = $(DIST_LIB_DIR)/appletvos

ifndef GEN_OBJC_DIR
GEN_OBJC_DIR = $(BUILD_DIR)/objc
endif
ifndef GEN_JAVA_DIR
GEN_JAVA_DIR = $(BUILD_DIR)/java
endif

TVOS_AVAILABLE = \
  $(shell if xcodebuild -version -sdk appletvos >/dev/null 2>&1; \
  then echo "YES"; else echo "NO"; fi)

ifndef J2OBJC_ARCHS
J2OBJC_ARCHS = macosx iphone iphone64 watchv7k watchsimulator simulator simulator64
ifeq ($(TVOS_AVAILABLE), YES)
J2OBJC_ARCHS += appletvos appletvsimulator
endif
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

CC_WARNINGS = -Wall -Werror -Wshorten-64-to-32 -Wimplicit-function-declaration \
  -Wmissing-field-initializers -Wduplicate-method-match -Wno-unused-variable \
  -Wno-nullability-completeness

ifdef GCC_PREPROCESSOR_DEFINITIONS
DEBUGFLAGS += $(GCC_PREPROCESSOR_DEFINITIONS:%=-D%)
endif

TRANSLATOR_DEPS = $(DIST_DIR)/j2objc $(DIST_JAR_DIR)/j2objc.jar

# Use Java 8 by default.
# TODO(tball): remove when Java 9 is supported.
JAVA_HOME = $(shell /usr/libexec/java_home -v 1.8)
JAVA = $(JAVA_HOME)/jre/bin/java
ifdef J2OBJC_JAVAC
JAVAC = $(J2OBJC_JAVAC)
else
JAVAC = $(JAVA_HOME)/bin/javac
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

ifeq ($(findstring clean,$(notdir $(MAKECMDGOALS))),clean)
IS_CLEAN_GOAL = 1
endif
ifeq ($(findstring test,$(notdir $(MAKECMDGOALS))),test)
IS_TEST_GOAL = 1
endif

ifndef PROTOBUF_ROOT_DIR
ifndef IS_CLEAN_GOAL
check_protobuf_dir = $(error PROTOBUF_ROOT_DIR not defined)
endif
endif

PROTOBUF_LIB_PATH = $(PROTOBUF_ROOT_DIR)/lib
PROTOBUF_INCLUDE_PATH = $(PROTOBUF_ROOT_DIR)/include
PROTOBUF_BIN_PATH = $(PROTOBUF_ROOT_DIR)/bin
PROTOBUF_PROTOC = $(PROTOBUF_BIN_PATH)/protoc

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

# Specify flag if clang version 7 or greater. This is necessary to support
# iOS 9 apps that have the 'Enable bitcode' option set, which is the default for
# new apps in Xcode 7.
XCODE_7_MINIMUM := $(shell $(CLANG) --version | \
    awk '/^Apple/ { split($$4, arr, "."); print (arr[1] >= 7) ? "YES" : "NO"; }')
