#!/bin/bash
#
# THIS SCRIPT IS GOOGLE-INTERNAL.
#
# Uses class-dump to list all Objective-C classes and protocols linked into an
# iOS app. Since scp is used to copy the IPA, builds from remote machines can
# be used.
#
# class-dump can be downloaded from https://github.com/nygard/class-dump
# (release is old, so build binary from source).

if [ $# -lt 1 ]; then
  printf "usage: dump_ipa_classes.sh <app.ipa>\n" 1>&2
  exit 1
fi

readonly APP_IPA_PATH=$1
readonly APP_IPA=$(basename $APP_IPA_PATH)
readonly TEMP_DIR="/tmp/dump_ipa_classes/"${APP_IPA}

mkdir -p $TEMP_DIR
if [ ! -d $TEMP_DIR ]; then
  printf "failed creating %s directory\n" "$TEMP_DIR" 1>&2
  exit 1
fi

trap '{ rm -rf -- "$TEMP_DIR"; }' EXIT

scp $APP_IPA_PATH $TEMP_DIR
if [ $? -ne 0 ]; then
  printf "failed copying %s\n" "$APP_IPA_PATH" 1>&2
  exit 1
fi

printf "unzipping %s\n" "$APP_IPA" 1>&2
unzip -q -o "$APP_IPA" -d $TEMP_DIR
if [ $? -ne 0 ]; then
  printf "failed unzipping %s\n" "$APP_IPA_PATH" 1>&2
  exit 1
fi

printf "running class-dump ... "  1>&2
class-dump "$TEMP_DIR"/Payload/*.app/ | egrep "@(interface|protocol)" | sort
printf "done\n" 1>&2
