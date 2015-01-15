#!/usr/bin/env sh -e

if $LATEST_JAVA; then
  brew update
  brew install caskroom/cask/brew-cask
  brew cask install java
fi
