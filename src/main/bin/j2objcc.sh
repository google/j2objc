#!/bin/bash
# Copyright 2011 Google Inc.  All Rights Reserved.
#
# A convenience wrapper for compiling files translated by j2objc using Clang.
# The JRE emulation and proto wrapper library include and library paths are
# added, as well as standard Clang flags for compiling and linking Objective-C
# applications on iOS.
#
# Usage:
#   j2objcc <clang options> <files>
#

if [ -L "$0" ]; then
  readonly DIR=$(dirname $(readlink "$0"))
else
  readonly DIR=$(dirname "$0")
fi

if [ x$CONFIGURATION_BUILD_DIR != x ]; then
  readonly INCLUDE_PATH=${CONFIGURATION_BUILD_DIR}/Headers
  readonly LIB_PATH=${CONFIGURATION_BUILD_DIR}
else
  readonly INCLUDE_PATH=${DIR}/include
  readonly LIB_PATH=${DIR}/lib
fi

declare CC_FLAGS="-Werror -Wno-parentheses"
declare  OBJC=-ObjC
declare  LINK_FLAGS="-ljre_emul -framework Foundation -framework ExceptionHandling -L ${LIB_PATH}"

for arg; do
  case $arg in
    # Check whether linking is disabled by a -c, -S, or -E option.
    -[cSE]) LINK_FLAGS="" ;;
    # Check whether we need to build for C++ instead of C.
    objective-c\+\+) CC_FLAGS="${CC_FLAGS} -std=gnu++98" OBJC= ;;
  esac
done

clang $* -I ${INCLUDE_PATH} ${CC_FLAGS} ${OBJC} ${LINK_FLAGS}
