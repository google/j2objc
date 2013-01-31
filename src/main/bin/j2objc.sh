#!/bin/bash
# Copyright 2011 Google Inc.  All Rights Reserved.
#
# Run j2objc translation tool.
#
# Usage:
#   j2objc
#     [-begin-java-args <java-options> -end-java-args]
#     [-classpath <path>]
#     [-sourcepath <path>]
#     [-d <output-directory>]
#     <file.java> ...
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

PARSING_JAVA_ARGS=0
JAVA_ARGS=""
J2OBJC_ARGS=""
for ARG in $@; do
  if [ "${ARG}" == "-begin-java-args" ]; then
    PARSING_JAVA_ARGS=1
  elif [ "${ARG}" == "-end-java-args" ]; then
    PARSING_JAVA_ARGS=0
  else
    if [ ${PARSING_JAVA_ARGS} -eq 0 ]; then
      J2OBJC_ARGS="${J2OBJC_ARGS} ${ARG}"
    else
      JAVA_ARGS="${JAVA_ARGS} ${ARG}"
    fi
  fi
done

java ${JAVA_ARGS} -jar ${JAR} ${BOOT_PATH} ${J2OBJC_ARGS}

