#!/usr/bin/bash
#
# Copyright 2011 Google Inc. All Rights Reserved.
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
# Author: Tom Ball
#
# Run specified unit test executables, or all tests in the build/tests
# directory.

if [ $# -eq 0 ]; then
  TESTS=$(/usr/bin/find build/tests -perm 755 -a -type f)
else
  TESTS=$*
fi

tests=0
errors=0
aborted_tests=0

readonly UNCAUGHT_EXCEPTION_CODE=134
readonly SEGMENTATION_FAULT=139

function add_results {
  case "$?" in
    0 ) ;;
    $UNCAUGHT_EXCEPTION_CODE | $SEGMENTATION_FAULT )
      aborted_tests=$(/bin/expr ${aborted_tests} + 1) ;;
    * )
      errors=$(/bin/expr ${errors} + $1) ;;
  esac
  tests=$(/bin/expr ${tests} + 1)
}

function runtest {
  echo "starting $(basename $@)"
  eval "$@"
  add_results $?
  echo
}

for test in ${TESTS}; do
  runtest ${test}
done
echo "Test binaries run:" ${tests} " Total errors/failures:" ${errors} " Aborted tests:" ${aborted_tests}
