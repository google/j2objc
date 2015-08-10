---
title: JUnit Translation
layout: docs
---

Tests, especially unit tests, should be a large part of a well-engineered source base.  It is therefore essential that JUnit tests can be translated and executed to verify that the code's intent hasn't been changed.

The issue is that JUnit and other popular test support frameworks, such as !EasyMock, require Java reflection support.  Java reflection is a rich API that closely models the essential aspects of Java, and so doesn't map easily to Objective-C.  Objective-C also supports reflection, though, so a large subset of the Java reflection API can be implemented.

The JUnit test classes, such as `junit.framework.Assert` are not changed, just translated. This includes the test runner classes, so the libjunit.a library includes a version of main() that invokes `org.junit.runner.JUnitCore`, the JUnit 4 command-line runner (which also supports JUnit 3 tests).

To build a JUnit test class:

 * Translate it as usual using j2objc.
 * Link it using j2objcc with a "-l junit" flag to include the JUnit library.
 * Execute the resulting binary on the command-line.

The Objective-C runtime reports any memory leaks it detected after a test binary is run. Ignore them at your (app's) peril!
