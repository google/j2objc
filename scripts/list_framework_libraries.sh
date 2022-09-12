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
# Lists which static libraries were built that should be included in
# an XCFramework. This list depends upon what architectures were built,
# and whether XCFramework requires a fat or single-arch library.
#
# Usage:
#   list_framework_libraries.sh <library-name>

if [ $# -eq 0 ]; then
  echo "usage: list_framework_libraries.sh <library-name>"
  exit 1
fi

readonly LIBRARY_NAME=$1

# The list of architectures where XCFramework expects a fat library.
FAT_PLATFORMS="iphone simulator macosx maccatalyst watchos watchsimulator"

# The list of architectures were XCFramework only accepts single-arch libraries.
SINGLE_PLATFORMS="appletvos appletvsimulator"

for platform in $FAT_PLATFORMS
do
  library=build_result/$platform/lib${LIBRARY_NAME}.a
  if [ -f $library ]; then
    echo $library
  fi
done
for platform in $SINGLE_PLATFORMS
do
  library=build_result/objs-$platform/lib{LIBRARY_NAME}.a
  if [ -f $library ]; then
    echo $library
  fi
done
