---
title: Dead Code Elimination
layout: docs
---

# Dead Code Elimination

## Introduction

Your Java application's source jar probably contains a huge number of source files that are either completely unused or included for just a few methods.  Translating this dead code into Objective-C will bloat your iOS application unnecessarily and increase the likelihood of encountering translation errors, especially if one of your Java libraries uses features not supported by the J2ObjC translator.

[ProGuard](http://proguard.sourceforge.net/) is an open source tool that helps you shrink, obfuscate, and otherwise mangle Java bytecode.  Optionally, given a bytecode jar, it can print out a "usage" report listing all of the unused classes and methods in your application.  J2ObjC can use such a report to skip these classes and methods during translation.

ProGuard can be downloaded [here](http://proguard.sourceforge.net/index.html#downloads.html).


## Configuring ProGuard

ProGuard accepts a configuration file as a command-line argument that specifies the optimizations it should perform and the reports it should generate.  Since J2ObjC only needs to know about dead code, you should disable all optimizations and extraneous logging; the file passed to J2ObjC should consist only of the ProGuard header text and the usage report.

First, ensure that you have a bytecode jar for your application that corresponds to the source jar.  You can use the following configuration file, copied from the [ProGuard manual](http://proguard.sourceforge.net/index.html#manual/examples.html), as a template to produce the output that J2ObjC expects:

```
-injars app-bin.jar
-libraryjars <java.home>/lib/rt.jar

-dontoptimize
-dontobfuscate
-dontpreverify
-printusage
-dontnote

-keep public class com.foo.app.Main {
    public static void main(java.lang.String[]);
}

-keepclassmembers class * {
    static final % *;
    static final java.lang.String *;
}
````

Modify this configuration file to ensure that ProGuard doesn't eliminate anything that it shouldn't.  If your jar is an application and not just a library, then specifying the `main()` method as we have done here should keep everything that is necessary; you can examine the resulting output from ProGuard as a sanity check.

## Running ProGuard

Once you have your application's bytecode jar and a customized ProGuard configuration file (called, say, `usage.pg`), you can create a usage report file with the following command:

````
java -jar proguard.jar @usage.pg > usage.log
````

This will create a file called `usage.log` that lists your application's unused classes and methods.

## Running J2ObjC with dead code elimination

Now that you have the ProGuard usage report, you can enable the dead code elimination phase in J2ObjC using the command-line flag `--dead-code-report <file>`.  The DeadCodeEliminator translation phase will use the usage report to remove dead code from each source file prior to translation to Objective-C.
