#!/bin/bash
#
# Run tree_shaker translation tool.

if [ -L "$0" ]; then
  readonly DIR=$(dirname $(readlink "$0"))
else
  readonly DIR=$(dirname "$0")
fi

readonly LIB_DIR=${DIR}/lib

readonly JAR=${LIB_DIR}/tree_shaker.jar

if [ $# -eq 0 ]; then
  # Invoke app without arguments, so it displays a help message.
  java -jar ${JAR}
  exit $?
fi

ARGS=""
JAVA_ARGS=$()
CLASSPATH=${LIB_DIR}/j2objc_annotations.jar
SOURCEPATH=
SOURCEPATH_SET=0

while [ $# -gt 0 ]; do
  case $1 in
    -classpath|-cp) CLASSPATH="${CLASSPATH}:$2"; shift;;
    -sourcepath) SOURCEPATH=$2; SOURCEPATH_SET=1; shift;;
    -J*) JAVA_ARGS[iJavaArgs++]=${1:2};;
    *) ARGS="${ARGS} $1";;
  esac
  shift
done

if [ ${SOURCEPATH_SET} -eq 1 ]; then
  SOURCEPATH=${SOURCEPATH}:${LIB_DIR}/jre_emul-src.jar
else
  SOURCEPATH=${LIB_DIR}/jre_emul.jar
fi

MAX_MEMORY_SET=0
for arg in "${JAVA_ARGS[@]}"; do
  [[ ${arg} =~ ^"-Xmx" ]] && MAX_MEMORY_SET=1;
done
if [ ${MAX_MEMORY_SET} -eq 0 ]; then
  JAVA_ARGS[iJavaArgs++]="-Xmx2048m"
fi

java ${JAVA_ARGS[*]} -jar ${JAR} -classpath ${CLASSPATH} \
    -sourcepath ${SOURCEPATH} ${ARGS}
