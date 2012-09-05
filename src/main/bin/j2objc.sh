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
  readonly JAR=${DIR}/j2objc.jar
else
  readonly JAR=${DIR}/lib/j2objc.jar
fi

java -jar ${JAR} $*
