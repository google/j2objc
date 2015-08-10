---
title: Initialization Normalizer
layout: docs
---

The Initialization Normalizer moves initializer statements into constructors and class initialization methods.  This includes expressions used to initialize fields and static variables, as well as static and instance blocks.  

### Instance Initialization

All initialization expressions for non-static fields, along with any instance blocks, are removed from their original locations and converted to statements, if necessary.  If Apple's designated initializer pattern is detected, which is commonly used in Java, these statements are moved to the top of the designated constructor, after any super invocation.  If the pattern is not detected, then the same initialization code is injected into all constructors.  If the class does not have a constructor, then a default constructor is added to the class.  Execution order for these expressions is from the start of the source file to its end, like Java.

### Static Initialization

If there are any static variable initialization expressions, or any static blocks, then an `initialize` class method is created.  The static variable initialization expressions are converted to statements, and moved to the `initialize` method, along with the contents of static blocks.  Execution order is also the same as Java, from the start of the source file to the end.
