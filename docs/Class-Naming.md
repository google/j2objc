---
title: Class Naming
layout: docs
---

## Packages

Objective-C does not have an equivalent to Java packages.  In practice, Java packages have two functions: officially to provide name spaces classes can be named without collisions, and unofficially to organize source code into directory hierarchies.  Because most Java builds map a package hierarchy to a directory hierarchy, tools like *make* assume that a source file path can be easily derived from a generated file path.  The issue then becomes how can collisions be avoided in a flat name space, while supporting build tools that assume output names only differ from input names by their suffices.

The convention with Objective-C is to use well-known prefixes to separate framework class names.  String, for example, becomes NSString (NS for NeXTStep).  We therefore chose to camel-case Java packages to form prefixes for their classes.  For example, java.lang.Exception becomes !JavaLangException, and junit.framework.!TestCase becomes !JunitFrameworkTestCase.  Although this can form some long names in practice the output code is still legible, which wouldn't be true if we used some sort of encoding system to truncate name length.  This will make executables with debugging symbols slightly larger, but should not impact release executable size.

These names are used for type definitions and references, but not file names.  Generated files follow the convention *javac* uses; java.lang.Exception output is written to java/lang/Exception.h and java/lang/Exception.m.  Import statements use file names, so importing java/lang/Exception.h defines !JavaLangException.

## Inner Classes

Inner classes are converted to toplevel classes, but their names preserve the relationship between their class and their declaring class(es).  We did this by separate the output and inner names with a underscore, and do not prepend the package to the inner class name.  java.util.Map.Entry therefore becomes !JavaUtilMap_Entry.  All outer classes are included in the inner class name, so Foo.Bar.Mumble becomes !Foo_Bar_Mumble.

## Anonymous Classes

Anonymous classes are first converted to inner classes, then to toplevel classes.  The javac convention of using $N where N is the index in the list of inner classes in a type.  Anonymous classes can also be nested, and follow the same definition as inner classes.  Inner and anonymous class names can be deeply nested.
