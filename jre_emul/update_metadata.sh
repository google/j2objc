#!/bin/bash

# Automatically updates the metadata of hand-written emulated classes by
# translating the equivalent stub class then substituting the metadata methods.

TRANSLATION_DIR=translation_temp
TEMP_DIR=metadata_temp
REPLACE_SCRIPT=../scripts/replace_metadata.py
J2OBJC=../dist/j2objc
STUBS_DIR=stub_classes
SOURCE_FLAG="-source 1.8"

function update_file {
  objc_source=$1
  java_source=$2
  translated_source=$TRANSLATION_DIR/${java_source/.java/.m}
  result_source=$TEMP_DIR/$objc_source
  $J2OBJC -d $TRANSLATION_DIR $SOURCE_FLAG $STUBS_DIR/$java_source
  $REPLACE_SCRIPT $objc_source $translated_source $result_source

  if ! diff -q $objc_source $result_source; then
    if [ ! -w $objc_source ]; then p4 edit $objc_source; fi
    cp $result_source $objc_source
  fi
}

mkdir -p $TEMP_DIR $TRANSLATION_DIR

update_file Classes/IOSClass.m java/lang/Class.java
update_file Classes/NSCopying+JavaCloneable.m java/lang/Cloneable.java
update_file Classes/NSException+JavaThrowable.m java/lang/Throwable.java
update_file Classes/NSNumber+JavaNumber.m java/lang/Number.java
update_file Classes/NSObject+JavaObject.m java/lang/Object.java
update_file Classes/NSString+JavaString.m java/lang/String.java
update_file Classes/java/lang/AbstractStringBuilder.m java/lang/AbstractStringBuilder.java
update_file Classes/java/lang/Iterable.m java/lang/Iterable.java
update_file Classes/java/lang/reflect/AccessibleObject.m java/lang/reflect/AccessibleObject.java
update_file Classes/java/lang/reflect/Constructor.m java/lang/reflect/Constructor.java
update_file Classes/java/lang/reflect/Executable.m java/lang/reflect/Executable.java
update_file Classes/java/lang/reflect/Field.m java/lang/reflect/Field.java
update_file Classes/java/lang/reflect/Method.m java/lang/reflect/Method.java

rm -rf $TEMP_DIR $TRANSLATION_DIR
