---
layout: default
---

# Design Overview

J2ObjC is a [transpiler](http://en.wikipedia.org/wiki/Source-to-source_compiler) that converts Java source code to Objective-C source for the iOS platform.  A transpiler is very similar to a compiler, except that it writes source files instead of object files.

### JDT

J2ObjC uses the [Eclipse Java Development Tools](http://eclipse.org/jdt/core/index.php) (JDT) core for its front-end.  The [JDT API](http://help.eclipse.org/indigo/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Foverview-summary.html) returns a AST for a Java source file that is fully resolved.  The tool then makes several passes over the AST, gradually mutating it to more closely model an Objective-C class.  When finished, two generators walk the AST to create the header and implementation files for that compilation unit.  A major difficulty using JDT for this sort of tool is that it has a read-only type system, however.

### Translation Phases

J2ObjC currently uses the following translation steps before output file generation:

- [Rewriter](Rewriter.html): rewrites Java code that doesn't have an Objective-C equivalent, such as static variables.
- [Autoboxer](Autoboxer.html): adds code to box numeric primitive values, and unbox numeric wrapper classes.
- [iOS Type Converter](IOS Type Converter): converts types that are directly mapped from Java to Foundation classes.
- [iOS Method Converter](IOS Method Converter): maps method declarations and invocations to equivalent Foundation class methods.
- [Initialization Normalizer](Initialization-Normalizer.html): moves initializer statements into constructors and class initialization methods.
- [Anonymous Class Converter](Anonymous-Class-Converter.html): modifies anonymous classes to be inner classes, which includes fields for the final variables referenced by that class.
- [Inner Class Converter](Inner-Class-Converter.html): pulls inner classes out to be top-level classes in the same compilation unit.
- [Destructor Generator](Destructor-Generator): adds dealloc or finalize methods, depending on GC option selected.
- Complex Expression Extractor: breaks up deeply nested expressions such as chained method calls.
- Nil Check Resolver: adds nil_chk calls wherever an expression is dereferenced.
 
### Generation

J2ObjC has two generation steps: one to generate the header (*.h*) file, and the other the implementation file (*.m* for Objective-C, and *.mm* for Objective-C++).  Before outputting a file, the AST is scanned for types that need to be imported.  These lists are different for headers and implementation files, since header files only import the types needed to define the class' API, not what it references.

The statement generator is called by implementation file generator, and walks the AST to output the bulk of the code.  All generators make extensive use of the type system to make semantic translation; for example, casts of a bound type need to be added when doing method chaining, such as for:

```java
    int getLength(List<String> list, int index) {
      return list.get(index).length();
    }
```

is translated to:

```objc
    - (int)getLengthWIthJavaUtilList:(JavaUtilList *)list withInt:index {
      return [(NSString *) [list getWithInt:index] length];
    }
```

### Name Mapping

Java uses packages to informally define namespaces; while Objective C++ has C++ namespaces, Objective C doesn't.  To preserve name uniqueness when using classes from multiple packages, J2ObjC prepends a camel-cased version of the package to the type name.  For example, `java.util.Map` is renamed to `JavaUtilMap`.

#### Inner Class Names

Objective-C doesn't have inner classes, so those classes are pulled up by the [Inner Class Converter](Inner Class Converter).  To preserve their namespace, their class names are appended to their containing class, with a separating underscore.  For example, `java.util.Map.Entry` is renamed to `JavaUtilMap_Entry`.  Deeply nested inner classes follow the same pattern with each containing class; i.e., `MyClass_Inner_MoreInner_Innermost`.

### Anonymous Class Names

Objective-C doesn't have anonymous classes, either, so those classes are converted to inner classes by the [Anonymous Class Converter](Anonymous Class Converter).  Like javac, a dollar sign and an integer are used to name these inner classes, such as `MyClass_$1`. The number is specific to the containing class, so it's common in classes with deeply nested anonymous classes to see names like `MyClass_$1_$2_$1`.