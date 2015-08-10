---
title: Inner Class Converter
layout: docs
---

The Inner Class Extractor pulls inner classes out of their containing class, and makes them top-level 
classes in the same compilation unit as their containing class.  This translation phase is run after 
the [Anonymous Class Converter](Anonymous-Class-Converter.html), so after execution all classes, 
interfaces, and enums are top-level classes.  

The Inner Class Extractor is the first phase where after translation the AST may not be legal Java, 
since it is possible to have a single compilation unit with multiple public classes.  With that 
exception, the AST otherwise represents legal Java syntax.

### Inner Classes

Non-static inner classes are first modified by adding fields for each referenced containing type.
These are added to all constructor parameter lists, as well as assignment to each field.  Next, 
the containing class is scanned, and instantiations of this inner class are modified to pass in 
this references to the containing class(es).  Finally the class is hoisted from its containing type 
and moved to the compilation unit's list of types.  The class is renamed by prefixing its original n
ame with the containing type's name, plus an underscore.  For example, `java.util.Map.Entry` is 
renamed to `JavaUtilMap_Entry`.

### Static Classes

Static classes do not need references to their containers, so they are moved from their containing type and renamed.
