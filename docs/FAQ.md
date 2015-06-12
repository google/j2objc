---
layout: docs
---

# Frequently Asked Questions (FAQ)

### I'm having problems building with Xcode

See [Debugging Build Problems](Xcode-Build-Rules.html#wiki-debugging-build-problems).

### I'm having problems using j2objc on the command-line

The fastest way to come up with a command-line for j2objc is to start with javac, specifying the classpath (optional), sourcepath (optional), output directory, and list of source files. Once that compiles successfully, substitute "j2objc" for "javac", and add any j2objc-specific flags. The reason this works is that j2objc uses a Java compiler as it's front-end, and so uses the same arguments as the compiler. Remember: if it doesn't compile, it can't be translated!

### How is garbage collection handled?

See [Memory Management](Memory-Management.html).  We continue to refine generated code to improve how memory is managed, using Xcode's leak detection and profiling tools.  We encourage projects using J2ObjC to also monitor performance and leak detection (as all iOS projects should), and to report any issues found.

### How can the translated code size be reduced?  What can speed up translation?

See [Dead Code Elimination](Dead-Code-Elimination.html).

### How does j2objc handle imports inside of .java files?

j2objc uses the [Eclipse JDT](Design-Overview.html#JDT) as its front-end, so all imports are read as any Java compiler would do.  To specify where to find imported classes, use the same -classpath and -sourcepath options you would use with javac. When generating Objective-C files, external class references are gathered, and `#import` directives are added to either the generated header (.h) or implementation (.m) files as appropriate.

### Why are some of the minimum and maximum values for numeric types different from the Java specification?

These values are different from what Java returns because they are outside what the Objective-C compiler will accept as valid. We instead use the minimum and maximum values defined in /usr/include/values.h.
