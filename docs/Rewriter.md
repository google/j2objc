---
layout: default
---


The Rewriter modifies the initial Java AST model to make it more Objective C friendly.  It was the first translation class, and over time most of its functionality was moved to separate translation phases as their complexity increased.

### Abstract Methods

Objective C does not have the equivalent of an abstract method, so the Rewriter adds method bodies to abstract methods.  To enforce that abstract methods cannot be directly invoked at runtime, a `doesNotRecognizeSelector` message is invoked.  For example, here is the generated code for `java.util.AbstractCollection.size()`:
```objc
    - (int)size {
      // can't call an abstract method
      [self doesNotRecognizeSelector:_cmd];
      return 0;
    }
```

### Forwarding Methods

Java allows a class to implement an interface without having declarations for all of its methods, if the missing methods are defined in a superclass.  One common example is in `java.util.Map`, which declares `equals()` and `hashCode()` that have default implementations in `java.lang.Object`.  Objective C requires that all methods of a protocol be implemented by the class that declares its use, so the Rewriter adds forwarding methods.

### Duplicate or Reserved Member Names

Objective C does not allow fields and methods in a class to have the same name, while Java does.  Objective C also does not allow a field to hide a field in one of its superclasses, even if the hidden field is private.  In either of these cases, the field is renamed.

Class members also renamed if their name is reserved.  Since Objective C is a superset of ANSI C, C reserved names are included.  Reserved names include predefined types (id, bool, unichar, etc.), C typedefs and C99 keywords (auto, inline, signed, union, etc.), C++ keywords (template, mutable), and predefined variables (self, isa).  Since J2ObjC maps `java.lang.Object` to `NSObject`, all of `NSObject`'s message names are also reserved.

In Java, labels within different scopes in a method can have the same name, which C doesn't allow. They are therefore renamed whenever necessary.

### Add Enhanced For Loop Support

Like the javac compiler, the Rewriter converts an enhanced for statement into an equivalent for statement.

### Fix Array Type Declarations

Java allows brackets to be defined either as part of the type or variable name, as does C. However, C allows the mixing of different types in a single declaration statement, such as "int i, a[Java requires these be separate types have separate declarations, so the above is rewritten as "int i; int[](];".) a;".

### Convert Java Format Strings into iOS Format Equivalents

Strings used with `String.format()` are similar to but not identical to strings used with `NSString stringWithFormat:`. The Rewriter steps through each format string and converts type specifiers when necessary.

### Fixed Scoping of Variables Declared in Case Statements

Java allows variables to be declared in case statements. To support this in Objective-C, when a switch statement has a variable declaration in one or more of its case statements, the switch statement is put into a block and those declarations moved above the switch statement in that block. This preserves the Java scoping rules for these variables, while allowing for those variable names to reused after the switch statement completes.
