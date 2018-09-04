#!/bin/bash
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

SDK_TYPE=macosx
if [ $# -gt 0 ]; then
  case $1 in
    --iphoneos ) SDK_TYPE=iphoneos ;;
    --iphonesimulator ) SDK_TYPE=iphonesimulator ;;
    --watchos ) SDK_TYPE=watchos ;;
    --watchsimulator ) SDK_TYPE=watchsimulator ;;
    --appletvos ) SDK_TYPE=appletvos ;;
    --appletvsimulator ) SDK_TYPE=appletvsimulator ;;
    * ) echo "usage: $0 [--iphoneos | --iphonesimulator | --watchos | --watchsimulator | --appletvos | --appletvsimulator]" && exit 1 ;;
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

SDK_PATH=$(xcrun -sdk ${SDK_TYPE} --show-sdk-path)

if [ "x${SDK_PATH}" = "x" ]; then
  # SDKs aren't in standard location, so ask xcodebuild.
  SDK_PATH=$(xcodebuild -sdk ${SDK_TYPE} -version | awk '/^Path:/ { print $2 }')
fi

if [ "x${SDK_PATH}" = "x" ]; then
  echo "No iOS SDKs located."
  exit 1;
fi
echo ${SDK_PATH}
exit 0
