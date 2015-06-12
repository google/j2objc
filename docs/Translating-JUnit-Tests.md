---
layout: docs
---

# Translating JUnit Tests

Well-engineered software projects usually have lots of unit tests to verify them. For Java projects, [JUnit](http://junit.sourceforge.net/) is the most common unit test framework.  J2ObjC provides support for translating unit tests, so they can be executed as binaries on OS X. This verifies that the translation didn't change the semantics (behavior) of the translated classes, and verifies that the translated code runs as Objective-C code.

## Translating Tests

Run j2objc with a `junit.jar` in the classpath. A copy of this jar file is included in the j2objc distribution's `lib/` directory:

````
# Example: J2ObjC bundle unzipped into a ~/tools directory
$ export J2OBJC_HOME=~/tools/j2objc
$ ${J2OBJC_HOME}/j2objc -classpath ${J2OBJC_HOME}/lib/junit.jar MyUnitTest.java
````

## Linking Tests

Link with the `libjunit.a` library in the J2ObjC distribution's `lib/` directory, using the compiler's `-l` flag:

````
$ ${J2OBJC_HOME}/j2objcc -ObjC -o mytest -ljunit MyUnitTest.m
````

## Running Tests

Run the test executable with the names of one or more tests and/or test suites, like JUnit tests are run in Java. The names can either be the fully-qualified Java name (with package), or the equivalent translated name. For example, the `com.company.MyUnitTest` test class can also be specified as `ComCompanyMyUnitTest`.

````
$ ./mytest org.junit.runner.JUnitCore com.company.MyUnitTest  # or com.company.Test2
````

`org.junit.runner.JUnitCore` is one of JUnit's test runners, which can run either JUnit3 or JUnit4 tests. Any other JUnit runner can be used, though.

## Building Tests

A good example of how to use `make` to build and run a large set of unit tests is in [j2objc/jre_emul/tests.mk](https://raw.github.com/google/j2objc/master/jre_emul/tests.mk), in the project source code.
