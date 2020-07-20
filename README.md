# J2ObjC: Java to Objective-C Translator and Runtime #

**Project site:** <https://j2objc.org><br>
**J2ObjC blog:** <https://j2objc.blogspot.com><br>
**Questions and discussion:** <https://groups.google.com/group/j2objc-discuss>

### What J2ObjC Is ###
J2ObjC is an open-source command-line tool from Google that translates
Java source code to Objective-C for the iOS (iPhone/iPad) platform. This tool
enables Java source to be part of an iOS application's build, as no editing
of the generated files is necessary. The goal is to write an app's non-UI
code (such as application logic and data models) in Java, which is then
shared by web apps (using [GWT](http://www.gwtproject.org/)), Android apps,
and iOS apps.

J2ObjC supports most Java language and runtime features required by
client-side application developers, including exceptions, inner and
anonymous classes, generic types, threads and reflection. JUnit test
translation and execution is also supported.

J2ObjC is currently beta quality. Several Google projects rely on it, but
when new projects first start working with it, they usually find new bugs
to be fixed. If you run into issues with your project, please report them!

### What J2ObjC isn't ###
J2ObjC does not provide any sort of platform-independent UI toolkit, nor are
there any plans to do so in the future. We believe that iOS UI code needs to
be written in Objective-C, Objective-C++ or Swift using Apple's iOS SDK (Android
UIs using Android's API, web app UIs using GWT, etc.).

J2ObjC cannot convert Android binary applications. Developers must have source
code for their Android app, which they either own or are licensed to use.

## Requirements ##

* JDK 1.8 or JDK 11 (see [announcement]
  (https://groups.google.com/forum/#!topic/j2objc-discuss/BmDcAIvaTFs))
* Mac workstation or laptop
* OS X 10.12 or higher
* Xcode 8 or higher

## License ##

This library is distributed under the Apache 2.0 license found in the
[LICENSE](https://github.com/google/j2objc/blob/master/LICENSE) file.
The protocol buffers library is distributed under the same BSD license as
Google's protocol buffers. See its
[README](https://github.com/protocolbuffers/protobuf/blob/master/README.md) and
[LICENSE](https://github.com/protocolbuffers/protobuf/blob/master/LICENSE).

## Running on GNU/Linux ##

To build and run on GNU/Linux, install [the Darling project](http://www.darlinghq.org/), then following [its Compile and Run a Program example](https://wiki.darlinghq.org/what_to_try#compile_and_run_a_program). Please note that j2objc is only supported on iOS/macOS.
