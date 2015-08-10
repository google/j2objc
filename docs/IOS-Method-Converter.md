---
title: iOS Method Converter
layout: docs
---

The IOS Method Translator is the translator that updates method references to refer to iOS replacements.  It does this using a mapping table, JRE.mappings, which declares the methods to replace and their replacement.  For example, the line to map `Object.equals()` to `NSObject.isEqual:` is:
````
    java.lang.Object.equals(Ljava/lang/Object;)Z = NSObject isEqual:(id)anObject
````
The left hand declaration is the full method signature, as defined by the [Java Virtual Machine Specification](http://java.sun.com/docs/books/jvms/second_edition/html/VMSpecTOC.doc.html).  The right hand definition consists of the iOS class, followed by the message's full name.

The method and its mapping must have the same parameters, in the same order (in the future, we might move to a MessageFormat-like pattern to allow different ordering).

Additional mapping files can be specified on the command-line, using the --mapping option.
