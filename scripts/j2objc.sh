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

if [ -x "/usr/libexec/java_home" ]; then
  # java_home is available on all Mac systems.
  if [ -z "${JAVA_HOME}" ]; then
    readonly JAVA_HOME=`/usr/libexec/java_home -v 1.8 2> /dev/null`
  fi
  readonly JAVA=${JAVA_HOME}/bin/java
else
  # Non-Mac system (not supported, but should still work).
  readonly JAVA=`which java`
fi

SUPPORTED_JAVA_VERSIONS=(1.8 11)
JAVA_VERSION=0
for version in ${SUPPORTED_JAVA_VERSIONS[@]}; do
  ${JAVA} -version 2>&1 | fgrep -q "build ${version}"
  if [ $? -eq 0 ]; then
    JAVA_VERSION=${version}
  fi
done
if [ "${JAVA_VERSION}" = "0" ]; then
  echo "JDK not supported. Please set JAVA_HOME to JDK 1.8 or 11."
  exit 1
fi

J2OBJC_ARGS=()

if [ x${USE_SYSTEM_BOOT_PATH} == x ]; then
  J2OBJC_ARGS+=("-Xbootclasspath:${LIB_DIR}/jre_emul.jar")
fi

${JAVA} -version 2>&1 | fgrep -q "build 1.8"
if [ $? -ne 0 ]; then
  J2OBJC_ARGS+=("--system" "${LIB_DIR}/jre_emul_module")
fi

J2OBJC_ARGS+=(-Xannotations-jar "${LIB_DIR}/j2objc_annotations.jar")

PARSING_JAVA_ARGS=0
JAVA_ARGS=()

while [ $# -gt 0 ]; do
  case $1 in
    -begin-java-args) PARSING_JAVA_ARGS=1;;
    -end-java-args) PARSING_JAVA_ARGS=0;;
    -J*) JAVA_ARGS+=("${1:2}");;
    *)
      if [ ${PARSING_JAVA_ARGS} -eq 0 ]; then
        J2OBJC_ARGS+=("$1")
      else
        JAVA_ARGS+=("$1")
      fi;;
  esac
  shift
done

${JAVA} ${JAVA_ARGS[*]} -jar "${JAR}" "${J2OBJC_ARGS[@]}"
