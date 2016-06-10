#!/bin/bash

# Automatically updates the metadata of hand-written runtime sources.

if [ $# -ne 1 ]; then
  echo "usage: update_metadata.sh <protobuf-install-dir>"
  exit 1
fi

J2OBJC_DIR=`(cd ../.. && pwd)`
TEMP_DIR=metadata_temp
JAVA_DIR=$1/java/src/main/java
J2OBJC=$J2OBJC_DIR/dist/j2objc
REPLACE_SCRIPT=$J2OBJC_DIR/scripts/replace_metadata.py
TRANSLATED_TEMP=translated_temp
REPLACEMENT_TEMP=replacement_temp

function update_file {
  objc_source=../src/$1
  java_source=${1/.m/.java}
  translated_source=$TRANSLATED_TEMP/$1
  result_source=$REPLACEMENT_TEMP/$1

  CLASSPATH=build_result/protobuf-java-2.6.1.jar
  $J2OBJC -cp $CLASSPATH -d $TRANSLATED_TEMP $JAVA_DIR/$java_source
  $REPLACE_SCRIPT $objc_source $translated_source $result_source

  if ! diff -q $objc_source $result_source; then
    if [ ! -w $objc_source ]; then p4 edit $objc_source; fi
    cp $result_source $objc_source
  fi
}

mkdir -p $TEMP_DIR

cd $TEMP_DIR
mkdir -p $TRANSLATED_TEMP
mkdir -p $REPLACEMENT_TEMP

# Grab the opensource protobuf jar.
cp $J2OBJC_DIR/java_deps/pom.xml .
mvn -q generate-resources dependency:sources

update_file com/google/protobuf/Descriptors.m

cd ..
rm -rf $TEMP_DIR
