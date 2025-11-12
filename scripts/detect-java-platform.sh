#!/bin/sh
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

# POSIX-compatible script to print the JDK platform tag, e.g. "macos-aarch64".
# Prefers $JAVA_HOME/release (OS_NAME + OS_ARCH). Falls back to jmod describe
# if jmods are available (not true for all JDK distributions).
set -eu

# Find JAVA_HOME if not set
if [ -z "${JAVA_HOME:-}" ]; then
  JAVA_HOME=$(/usr/libexec/java_home)
fi

release="$JAVA_HOME/release"
platform=""

# Parse release file if present
if [ -f "$release" ]; then
  os_raw=$(awk -F= '/^OS_NAME=/{sub(/^"/,"",$2); sub(/"$/,"",$2); print $2; exit}' "$release" || true)
  arch_raw=$(awk -F= '/^OS_ARCH=/{sub(/^"/,"",$2); sub(/"$/,"",$2); print $2; exit}' "$release" || true)

  os=$(echo "${os_raw:-}" | tr '[:upper:]' '[:lower:]')
  arch=$(echo "${arch_raw:-}" | tr '[:upper:]' '[:lower:]')

  case "$os" in
    *mac*|*darwin*) os=macos ;;
    *linux*)        os=linux ;;
  esac

  case "$arch" in
    amd64|x86_64|x86-64) arch=x86_64 ;;
    aarch64|arm64)      arch=aarch64 ;;
    armv7l|armv7*)      arch=armv7l ;;
  esac

  if [ -n "$os" ] && [ -n "$arch" ]; then
    platform="$os-$arch"
  fi
fi

# Fallback: use jmod describe if release not available or parsing failed
if [ -z "$platform" ]; then
  jmod_cmd="$JAVA_HOME/bin/jmod"
  if [ -f "$JAVA_HOME/jmods/java.base.jmod" ]; then
    platform=$($jmod_cmd describe "$JAVA_HOME/jmods/java.base.jmod" 2>/dev/null | awk '/^platform /{print $2; exit}' || true)
  fi
fi

if [ -z "$platform" ]; then
  echo "ERROR: failed to determine Java platform target" >&2
  exit 3
fi

printf '%s\n' "$platform"
