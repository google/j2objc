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
# Build XCFramework framework from set of single-arch static libraries.
# xcodebuild -create-xcframework needs all libraries to be included
# in a single command, so it can build the Info.plist listing them all.
#
# Usage:
#   gen_xcframework.sh <output-directory> library [library ...]

if [ $# -lt 3 ]; then
  echo "usage: gen_xcframework.sh <output-directory> library [library ...]"
  exit 1
fi

readonly FRAMEWORK_DIR=$1
shift

# xcodebuild won't override any existing framework files.
rm -rf ${FRAMEWORK_DIR}/*

CMD="xcodebuild -create-xcframework -output "${FRAMEWORK_DIR}

while test ${#} -gt 0
do
  # Skip libraries that have equivalent library definitions.
  if [[ $1 != *"iphone64e"* ]] && [[ $1 != *"watchv7k"* ]]; then
    CMD=${CMD}" -library "$1
  fi
  shift
done
echo $CMD
eval $CMD
