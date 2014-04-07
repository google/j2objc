#!/bin/bash
# Copyright 2012 Google Inc. All Rights Reserved.
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

# Returns the location of the latest installed Xcode Mac OS X, iPhoneOS,
# or iPhoneSimulator SDK root directory.
#
# Usage: sysroot_path [--iphoneos | --iphonesimulator]

SDK_TYPE=MacOSX
if [ $# -gt 0 ]; then
  case $1 in
    --iphoneos ) SDK_TYPE=iPhoneOS ;;
    --iphonesimulator ) SDK_TYPE=iPhoneSimulator ;;
    * ) echo "usage: $0 [--iphoneos | --iphonesimulator]" && exit 1 ;;
  esac
fi

if [[ "$OSTYPE" != "darwin"* ]]; then
  OS_NAME=$(uname -s)
  echo "Apple development tools not available on $OS_NAME."
  exit 1
fi

XCODE_ROOT=$(xcode-select --print-path)

if [ ! -d "${XCODE_ROOT}" ]; then
  echo "Xcode is not installed."
  exit 1
fi

# Try looking in the install directory for Xcode 4.x.
PLATFORM_ROOT=${XCODE_ROOT}/Platforms/${SDK_TYPE}.platform
if [ -d "${PLATFORM_ROOT}" ]; then
  SDKS_ROOT=${PLATFORM_ROOT}/Developer/SDKs
  if [ -d "${SDKS_ROOT}" ]; then
    # Return the alphabetically last SDK in the directory, which should be the
    # latest version.  This will need to be improved if iOS 10 ever releases.
    SDK_PATH=$(ls -rd "${SDKS_ROOT}/${SDK_TYPE}"* | head -1)
  fi
fi

if [ "x${SDK_PATH}" = "x" ]; then
  # SDKs aren't in standard location, so ask xcodebuild to look up a common
  # library, and then remove the library path.
  if [ ${SDK_TYPE} == "iphoneos" ]; then
    SDK_TYPE=iphoneos
  elif [ ${SDK_TYPE} == "iphonesimulator" ]; then
    SDK_TYPE=iphonesimulator
  else
    SDK_TYPE=macosx
  fi

  SDK_PATH=$(xcodebuild -sdk ${SDK_TYPE} -find-library system | sed 's/\/usr\/lib\/libsystem.dylib//')
fi

if [ "x${SDK_PATH}" = "x" ]; then
  echo "No iOS SDKs located."
  exit 1;
fi
echo ${SDK_PATH}
exit 0
