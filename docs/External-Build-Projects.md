---
title: External Build Projects
layout: docs
---

# External Build Projects

External [make files](Using-J2ObjC-with-Make.html) can be used with Xcode, using an External
Build System project template.  These files can be modified to take advantage
of Xcode project settings, such as the build type and location, while still
useful when used separately.

## Create an External Project

To create a new external build project, in Xcode select the New->New Project...
dialog, then select the External Build System template.  Put the project in
the same directory as the target Makefile.

## Add Xcode Build Settings

When invoking an external build, Xcode defines environment variables for its
[build settings](http://developer.apple.com/library/mac/#documentation/DeveloperTools/Reference/XcodeBuildSettingRef/1-Build_Setting_Reference/build_setting_ref.html).
These settings define where build files are created, as well as compiler and
linker flags.  We use Make's [conditional directives](http://www.gnu.org/software/make/manual/make.html#Conditionals)
to modify the build when it is invoked by Xcode.

**IMPORTANT**: add -ObjC to each build target's Other Linker Flags.  This is
necessary because J2ObjC uses categories to add Java Object and String methods
to NSObject and NSString.

Here's an example, where the variables being set (such as BUILD_DIR) can be
whatever name you want, while the conditionals use the environment variables
Xcode set:

```make
ifdef CONFIGURATION_BUILD_DIR
# In Xcode build
BUILD_DIR = $(CONFIGURATION_BUILD_DIR)/build
ARCHFLAGS = $(ARCHS:%=-arch %)
SDKFLAGS = -isysroot $(SDKROOT)
else
# In command-line build
BUILD_DIR = $(HOME)/build
ARCHFLAGS =
SDKFLAGS =
endif

ifdef OPTIMIZATION_LEVEL
DEBUGFLAGS := $(DEBUGFLAGS) -O$(OPTIMIZATION_LEVEL)
endif

ifdef OTHER_CFLAGS
DEBUGFLAGS := $(DEBUGFLAGS) $(OTHER_CFLAGS)
endif

# Workaround for iPhoneSimulator SDK's gcc bug
ifdef EFFECTIVE_PLATFORM_NAME
ifneq ($(EFFECTIVE_PLATFORM_NAME), -iphonesimulator)
WARNINGS := $(WARNINGS) -Wreturn-type
endif
endif

J2OBJCC_FLAGS = -ObjC $(WARNINGS) $(SDKFLAGS) $(ARCHFLAGS) $(DEBUGFLAGS)
J2OBJCC = $(J2OBJC_DIST)/j2objcc $(J2OBJCC_FLAGS)
```
