#!/bin/bash
# Copyright 2011 Google Inc.  All Rights Reserved.
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

if [ "x${PUBLIC_HEADERS_FOLDER_PATH}" != "x" ]; then
	readonly INCLUDE_PATH=${DIR}/${PUBLIC_HEADERS_FOLDER_PATH}
elif [ -d ${DIR}/include ]; then
  readonly INCLUDE_PATH=${DIR}/include
else
	# Xcode 4 default for new projects.
  readonly INCLUDE_PATH=${DIR}/Headers
fi
readonly LIB_PATH=${DIR}/lib

declare CC_FLAGS="-Werror -Wno-parentheses"
declare OBJC=-ObjC
declare LIBS="-ljre_emul -l j2objc_main"
declare FRAMEWORKS="-framework Foundation -framework ExceptionHandling"
declare LOAD_FLAGS="-force_load ${LIB_PATH}/libjre_emul.a"
declare LINK_FLAGS="${LIBS} ${FRAMEWORKS} -L ${LIB_PATH} ${LOAD_FLAGS}"

for arg; do
  case $arg in
    # Check whether linking is disabled by a -c, -S, or -E option.
    -[cSE]) LINK_FLAGS="" ;;
    # Check whether we need to build for C++ instead of C.
    objective-c\+\+) CC_FLAGS="${CC_FLAGS} -std=gnu++98" OBJC= ;;
  esac
done

xcrun clang $* -I ${INCLUDE_PATH} ${CC_FLAGS} ${OBJC} ${LINK_FLAGS}
