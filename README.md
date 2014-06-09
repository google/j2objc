# J2ObjC: Java to Objective-C Translator and Runtime #

**Project site:** <http://j2objc.org><br>
**J2ObjC blog:** <http://j2objc.blogspot.com><br>
**Questions and discussion:** <http://groups.google.com/group/j2objc-discuss>

### What J2ObjC Is ###
J2ObjC is an open-source command-line tool from Google that translates
Java code to Objective-C for the iOS (iPhone/iPad) platform. This tool
enables Java code to be part of an iOS application's build, as no editing
of the generated files is necessary. The goal is to write an app's non-UI
code (such as data access, or application logic) in Java, which is then
shared by web apps (using GWT), Android apps, and iOS apps.

J2ObjC supports most Java language and runtime features required by
client-side application developers, including exceptions, inner and
anonymous classes, generic types, threads and reflection. JUnit test
translation and execution is also supported.

J2ObjC is currently between alpha and beta quality. Several Google projects
rely on it, but when new projects first start working with it, they usually
find new bugs to be fixed.

### What J2ObjC isn't ###
J2ObjC does not provide any sort of platform-independent UI toolkit, nor are
there any plans to do so in the future. We believe that iOS UI code needs to
be written in Objective-C or Objective-C++ using Apple's iOS SDK (Android UIs
using Android's API, web app UIs using GWT, etc.).

## Requirements ##

* JDK 1.7 or higher
* Xcode 5 or higher
* Mac OS X 10.9 or higher

## License ##

This library is distributed under the Apache 2.0 license found in the
[LICENSE](./LICENSE) file.
