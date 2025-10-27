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


JAVA_ARGS=(\
"-Xmx2048m" \
"--add-exports" "java.compiler/javax.lang.model.element=ALL-UNNAMED" \
"--add-exports" "java.compiler/javax.lang.model.type=ALL-UNNAMED" \
"--add-exports" "java.compiler/javax.lang.model.util=ALL-UNNAMED" \
"--add-exports" "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED" \
"--add-exports" "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED" \
"--add-exports" "jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED" \
"--add-exports" "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED" \
"--add-exports" "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED" \
)

ARGS=""
CLASSPATH=${DIR}${LIB_DIR}/j2objc_annotations.jar

while [ $# -gt 0 ]; do
  case $1 in
    -classpath|-cp) CLASSPATH="${CLASSPATH}:$2"; shift;;
    *) ARGS="${ARGS} $1";;
  esac
  shift
done

java ${JAVA_ARGS[*]} -jar ${JAR} -classpath ${CLASSPATH} ${ARGS}
