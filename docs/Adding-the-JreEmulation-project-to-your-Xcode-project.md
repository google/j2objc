---
title: Add JreEmulation to Xcode
layout: docs
---

# Adding the JreEmulation Project to Your Xcode Project

J2ObjC's JRE emulation library can be built using Xcode, and can be included as a sub-project in your Xcode projects.  This allows you to control the settings used to build that library so it can best be used by your projects.

### Download the Source Code

Either:
- Download and upzip the [current source bundle](https://github.com/google/j2objc/archive/master.zip) into a local directory; or
- Clone the [source code tree](https://github.com/google/j2objc) using [git](http://git-scm.com/).  The advantage of a local source check out is that it's easier to stay current with the latest J2ObjC project changes, but it does require some knowledge of version control systems.

The source code can go anywhere on your local system.  For the purposes of this document we'll use `/usr/local/src/j2objc`, but change that path as you prefer.

### The JreEmulation Xcode project

In Xcode, open `/usr/local/src/j2objc/jre_emul/JreEmulation.xcodeproj`.  Its folders include:

- **Classes**: the set of core JRE emulation classes, which are not created by translating Java source files.
- **jre_emul_tests**: the set of unit tests which are built and run within Xcode.  These do not include the translated JUnit tests, which are built and run from the command-line using `make test` in the `jre_emul` directory.
- **Transpiled Classes**: these are the files created by translating the JRE library's Java source files.  Notes:
- Xcode marks missing files in red. Because these files are created during the build, they will initially all be red until the project is built.
- We're adding new classes regularly to the JRE emulation library, so this list may be out of date.  If so, right-click on this folder, click "Add Files to JreEmulation.xcodeproj", and add new `.m` and `.h` files from `jre_emul/build_result/Classes` and its sub-directories.

To build the JreEmulation project, select the `jre_emul` target and click Xcode's **Run** button.  To build and run the unit tests, click on the down arrow next to the **Run** button and select **Test**.

### Add the JreEmulation project to your project

- In the Project Naviagator panel on the left, right-click on your project and select "Add Files to *Project Name*".
- Find the `jre_emul/JreEmulation.xcodeproj` file and click the **Add** button.
- Click your project and select its main target, then select the target's Build Phases tab.
- Open the Target Dependencies section, click the **+** button, and select the `jre_emul` target.
- Open the Link Binary With Libraries section, click the **+** button, and select `libjre_emul.a`.
- Select the target's Build Settings tab, find Header Search Paths, and add the path to the `/usr/local/src/j2objc/dist/include` directory (substituting the actual location).

Now when your project is built, the JreEmulation project is built as needed.
