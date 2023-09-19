#!/bin/bash
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
# Build an XCFramework from a set of single-architecture static libraries.
# xcodebuild -create-xcframework needs all libraries to be included
# in a single command, so it can build the Info.plist listing them all.
#
# Usage:
#   gen_xcframework.sh <output-directory> [static-library-path ...]

if [ $# -lt 2 ]; then
  echo "usage: gen_xcframework.sh <output-directory> library [library ...]"
  exit 1
fi

readonly FRAMEWORK_DIR=$1
shift

readonly FRAMEWORK_HEADERS_DIR="${FRAMEWORK_DIR}/Headers"

if [ ! -d "${FRAMEWORK_HEADERS_DIR}" ]; then
  echo "$FRAMEWORK_HEADERS_DIR should exist but doesn't."
fi

CMD="xcodebuild -create-xcframework -output "${FRAMEWORK_DIR}

while test ${#} -gt 0
do
  CMD=${CMD}" -library "$1
  CMD=${CMD}" -headers "$FRAMEWORK_HEADERS_DIR
  shift
done
echo $CMD
eval $CMD
