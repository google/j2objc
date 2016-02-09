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
  echo "usage: build_distribution.sh <version-number> <protobuf-install-dir>"
  exit 1
fi
DISTRIBUTION_NAME=j2objc-$1

# Set j2objc flags used for public builds.
TRANSLATE_GLOBAL_FLAGS="--doc-comments;--generate-deprecated;--swift-friendly"

ENV_CMD="env -i PATH=$PATH HOME=$HOME J2OBJC_VERSION=${1%/} PROTOBUF_ROOT_DIR=${2%/}"
ENV_CMD="${ENV_CMD} TRANSLATE_GLOBAL_FLAGS=${TRANSLATE_GLOBAL_FLAGS}"

echo "make clean"
$ENV_CMD make clean
ERR=$?
if [ ${ERR} -ne 0 ]; then
  exit ${ERR}
fi

echo "make all_dist"
$ENV_CMD make -j8 all_dist
ERR=$?
if [ ${ERR} -ne 0 ]; then
  exit ${ERR}
fi

echo "make test_all"
$ENV_CMD make -j8 test_all
ERR=$?
if [ ${ERR} -ne 0 ]; then
  exit ${ERR}
fi

mv dist ${DISTRIBUTION_NAME}
zip -ry ${DISTRIBUTION_NAME}.zip ${DISTRIBUTION_NAME}
