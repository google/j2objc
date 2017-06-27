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
# Run j2objc translation tool.
#
# Usage:
#   j2objc
#     [-begin-java-args <java-options> -end-java-args]
#     [-classpath <path>]
#     [-sourcepath <path>]
#     [-d <output-directory>]
#     [j2objc arg] ...
#     [-J<java arg>] ...
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
ANNOTATIONS_ARG="-Xannotations-jar ${LIB_DIR}/j2objc_annotations.jar"

while [ $# -gt 0 ]; do
  case $1 in
    -begin-java-args) PARSING_JAVA_ARGS=1;;
    -end-java-args) PARSING_JAVA_ARGS=0;;
    -J*) JAVA_ARGS[iJavaArgs++]=${1:2};;
    *)
      if [ ${PARSING_JAVA_ARGS} -eq 0 ]; then
        J2OBJC_ARGS[iArgs++]=$1
      else
        JAVA_ARGS[iJavaArgs++]=$1
      fi;;
  esac
  shift
done

java ${JAVA_ARGS[*]} -jar "${JAR}" "${BOOT_PATH}" ${ANNOTATIONS_ARG} \
  "${J2OBJC_ARGS[@]}"
