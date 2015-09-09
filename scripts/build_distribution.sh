#!/bin/bash
#
# Build j2objc distribution bundle.
#
# Usage:
#   build_distribution.sh <version-number>

if [ $(basename $(pwd)) != "j2objc" ]; then
  echo "This script must be run in the top-level j2objc directory."
  exit 1
fi

if [ $# -ne 2 ]; then
  echo "usage: run_distribution.sh <version-number> <protobuf-install-dir>"
  exit 1
fi
DISTRIBUTION_NAME=j2objc-$1

ENV_CMD="env -i PATH=$PATH HOME=$HOME J2OBJC_VERSION=${1%/} PROTOBUF_ROOT_DIR=${2%/}"

echo "make clean"
make clean
echo "make all_dist"
$ENV_CMD make -j8 all_dist
echo "make test_all"
$ENV_CMD make -j8 test_all

mv dist ${DISTRIBUTION_NAME}
zip -r ${DISTRIBUTION_NAME}.zip ${DISTRIBUTION_NAME}
