---
layout: default
---

# Using J2ObjC with Gradle

[Bruno Bowden](https://gist.github.com/brunobowden) has developed a [Gradle plugin for J2ObjC development](https://github.com/brunobowden/j2objc-gradle). It works best with Android Studio, which uses Gradle as its default build system. This plugin is well-documented, so be sure to read its comments on setup and contributing patches.

List of commands in sequence. 

 1. test - compile and run all Java unit tests (from standard [Gradle Java plugin](http://www.gradle.org/docs/current/userguide/java_plugin.html))
 1. j2objcTranslate - translate all Java sources to Objective-C
 1. j2objcCompile - compile all the translated code to a 'testrunner' binary
 1. j2objcTest - run all the unit tests using 'testrunner'
 1. j2objcCopy - copy translated files to the relevant Xcode project