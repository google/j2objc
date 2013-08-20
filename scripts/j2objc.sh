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

if [ -e "${DIR}"/j2objc.jar ]; then
  readonly LIB_DIR="${DIR}"
else
  readonly LIB_DIR="${DIR}"/lib
fi
readonly JAR=${LIB_DIR}/j2objc.jar

if [ $# -eq 0 ]; then
  # Invoke app without arguments, so it displays a help message.
  java -jar "${JAR}"
  exit $?
fi

if [ x${USE_SYSTEM_BOOT_PATH} == x ]; then
  readonly BOOT_PATH=-Xbootclasspath:${LIB_DIR}/jre_emul.jar
fi

PARSING_JAVA_ARGS=0
JAVA_ARGS=$()
J2OBJC_ARGS=$()
CLASSPATH=${LIB_DIR}/j2objc_annotations.jar
CLASSPATH_SET=0
SOURCEPATH=
SOURCEPATH_SET=0

while [ $# -gt 0 ]; do
  case $1 in
    -begin-java-args) PARSING_JAVA_ARGS=1;;
    -end-java-args) PARSING_JAVA_ARGS=0;;
    -classpath|-cp) CLASSPATH="${CLASSPATH}:$2"; CLASSPATH_SET=1; shift;;
    -sourcepath) SOURCEPATH=$2; SOURCEPATH_SET=1; shift;;
    *)
      if [ ${PARSING_JAVA_ARGS} -eq 0 ]; then
        J2OBJC_ARGS[iArgs++]=$1
      else
        JAVA_ARGS[iJavaArgs++]=$1
      fi;;
  esac
  shift
done

if [ ${CLASSPATH_SET} -eq 0 ]; then
  CLASSPATH=".:${CLASSPATH}"
fi
if [ ${SOURCEPATH_SET} -eq 0 ]; then
  SOURCEPATH="."
fi

java ${JAVA_ARGS[*]} -jar "${JAR}" "${BOOT_PATH}" -classpath "${CLASSPATH}" \
    -sourcepath "${SOURCEPATH}" "${J2OBJC_ARGS[@]}"
