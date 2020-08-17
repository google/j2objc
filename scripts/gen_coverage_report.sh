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
# Generates test coverage report for jre_emul's unit tests.
#
# Note: this script uses lcov, which can be installed with HomeBrew.

if [ $(basename $(pwd)) != "jre_emul" ]; then
  echo "This script must be run in the j2objc/jre_emul directory."
  exit 1
fi

# Exit script on any shell error.
set -e

echo "make clean"
make clean
echo

echo "build jre_emul with coverage support"
make -j8 dist GENERATE_TEST_COVERAGE=1
echo

echo "build jre_unit_tests with coverage support"
make -j8 -f tests.mk link resources GENERATE_TEST_COVERAGE=1
echo

readonly coverage_dir="build_result/coverage"
readonly html_dir=${coverage_dir}/html
mkdir -p ${html_dir}

echo "process *.gcno files"
lcov -b . -c -i -d build_result/objs-macosx -d build_result/tests \
    -o ${coverage_dir}/coverage.init
echo

# Run unit tests, but allow test failures (coverage data is still useful).
echo "run unit tests"
set +e
ASAN_OPTIONS=coverage=1 ./build_result/tests/jre_unit_tests \
    org.junit.runner.JUnitCore AllJreTests
set -e
echo

echo "process coverage from executed tests"
lcov -b . -c -d build_result/objs-macosx -d build_result/tests \
    --no-external -o ${coverage_dir}/coverage.run
echo

echo "merge initial and executed coverage runs"
lcov -a ${coverage_dir}/coverage.init -a ${coverage_dir}/coverage.run \
    -o ${coverage_dir}/coverage.total
echo

echo "exclude coverage of unit test files"
lcov -e ${coverage_dir}/coverage.total "`pwd`/*" \
    -o ${coverage_dir}/coverage.total.filtered
lcov -r ${coverage_dir}/coverage.total.filtered \
    "`pwd`/build_result/tests/*" \
    "`pwd`/build_result/*/*.h" \
    -o ${coverage_dir}/coverage.total.filtered
echo

echo "generate HTML report"
genhtml -o ${html_dir} ${coverage_dir}/coverage.total.filtered

open ${html_dir}/index.html
