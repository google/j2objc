#!/bin/bash
# Copyright 2011 Google Inc.  All Rights Reserved.
#
# Run j2objc translation tool.
#
# Usage:
#   j2objc [-classpath path] [-sourcepath path] [-d outputDirectory] <file.java> ...
#

if [ -L "$0" ]; then
  readonly DIR=$(dirname $(readlink "$0"))
else
  readonly DIR=$(dirname "$0")
fi

if [ -e ${DIR}/j2objc.jar ]; then
  readonly LIB_DIR=${DIR}
else
  readonly LIB_DIR=${DIR}/lib
fi
readonly JAR=${LIB_DIR}/j2objc.jar

if [ x${USE_SYSTEM_BOOT_PATH} == x ]; then
  readonly BOOT_PATH=-Xbootclasspath:${LIB_DIR}/jre_emul.jar
fi

java -jar ${JAR} ${BOOT_PATH} $*
