#!/usr/bin/env sh -e
#
# Script used by Travis CI build to install Java if necessary.

if $LATEST_JAVA; then
  brew update
  brew cask install java
fi
