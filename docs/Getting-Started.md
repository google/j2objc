---
layout: docs
---

# Getting Started

First, either:

- Download the current distribution from the [Releases
section](https://github.com/google/j2objc/releases) and unzip it, or
- Get the [source](https://github.com/google/j2objc/archive/master.zip) and [build it](Building-J2ObjC.html).

To translate a Java source file (Hello.java, for example):

```java
public class Hello {
  public static void main(String[] args) {
    System.out.println("hello, world");
  }
}
```
````
$ j2objc Hello.java
translating Hello.java
Translated 1 file: 0 errors, 0 warnings
````

To compile the translated file:

```bash
$ j2objcc -c Hello.m
```

j2objcc is a wrapper script that invokes your C compiler (normally clang, aka [LLVM,
Apple's C/C++/Objective-C
compiler](http://developer.apple.com/library/mac/#documentation/CompilerTools/Conceptual/LLVMCompilerOverview/_index.html)).
To build the executable:

```bash
$ j2objcc -o hello Hello.o
$ ./hello Hello
hello, world
```

j2obcc is a wrapper script that adds the JRE emulation library's include and
library paths to whatever options you specify for the Objective-C compiler.
For example, to translate and build multiple sources with debugging symbols:

```bash
$ j2objcc -g -o test Test.m Foo.m Bar.m
```

## Frequently Asked Questions

#### When I run `j2objcc`, it complains that "Foundation/Foundation.h" isn't found.

If compilation fails because Foundation/Foundation.h isn't found, the problem
is that the iOS SDK wasn't found (that's where that header is).

1. Make sure you have Xcode installed.
1. Install the command line tools:
 - Xcode 5: run `xcode-select --install`.
 - Previous versions: in Xcode's Downloads Preferences, check that the Command Line Tools are installed. \
1. Run `xcodebuild -showsdks`, which should show at least one SDK for OS X, iOS,
and iOS Simulator. 
1. If that fails, delete the Xcode application and go to step 1.

#### What flags does `j2objcc` take?

The `j2objcc` script is just a wrapper around the Objective-C compiler, clang.
Run `man cc` or `man clang` to list its options.

#### When compiling with `j2objcc`, my project's header (.h) files cannot be found.

The compiler needs to know the directory where the translated files reside,
using `-I <directory>`. So if the files were generated with `j2objc -d foo/bar ...`, 
then the `j2objcc` command needs `-Ifoo/bar`. If no output directory was
specified in the `j2objc` command, then `-I.` needs to be added.

#### How do I run on Windows or Linux?

J2ObjC is an iOS tool that is for development on Mac OS X. You cannot compile
any translated code because it requires an OS X or iOS SDK from Apple, which
requires its SDKs only be used on Macs.

However, since the J2ObjC translator is pure-Java, translation can be done on
other systems. On Linux, the `j2objc` script should work unchanged. Windows
use requires either [CygWin](http://www.cygwin.com/), or invoking Java
directly. To invoke the translator without the j2objc script, use the
following where `<j2objc-dir>` is the directory where the J2ObjC distribution
file was unzipped:

````
$ java -Xbootclasspath:<j2objc-dir>\lib\jre_emul.jar -jar <j2objc-dir>\lib\j2objc.jar [j2objc-flags] [source files]
````
