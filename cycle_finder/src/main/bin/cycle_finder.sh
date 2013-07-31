#!/bin/bash
#
# Run cycle_finder translation tool.

if [ -L "$0" ]; then
  readonly DIR=$(dirname $(readlink "$0"))
else
  readonly DIR=$(dirname "$0")
fi

readonly LIB_DIR=/lib

readonly JAR=${DIR}${LIB_DIR}/cycle_finder.jar

if [ $# -eq 0 ]; then
  # Invoke app without arguments, so it displays a help message.
  java -jar ${JAR}
  exit $?
fi

ARGS=""
CLASSPATH=${DIR}${LIB_DIR}/j2objc_annotations.jar

while [ $# -gt 0 ]; do
  case $1 in
    -classpath|-cp) CLASSPATH="${CLASSPATH}:$2"; shift;;
    *) ARGS="${ARGS} $1";;
  esac
  shift
done

java -Xmx2048m -jar ${JAR} -classpath ${CLASSPATH} ${ARGS}
