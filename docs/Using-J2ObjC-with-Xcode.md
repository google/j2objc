---
title: Using J2Objc with Xcode
layout: docs
---

# Using J2Objc with Xcode

There two different ways J2ObjC can be integrated with Xcode: an external
build project with a separate make file, or adding a build rule
to any Xcode project type. The advantage of an external build is that
existing Java tools, such as IDEs that support error checking and
refactoring, are still used. The advantage of a build rule is that the
Java source is part of a single Xcode project, and Java sources
can be used when debugging.

- [External Build](External-Build-Projects.html)
- [Xcode Build Rule](Xcode-Build-Rules.html)

Another option is to include the 
[JreEmulation Xcode project](Adding-the-JreEmulation-project-to-your-Xcode-project.html)
into your own project.  This has the tightest integration between your
project's build and the JRE emulation library's, so that build settings
can be shared between them.  You can also debug JRE code, and instrument
and analyze it.
