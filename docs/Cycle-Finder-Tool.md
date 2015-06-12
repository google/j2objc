---
layout: docs
---

# Cycle Finder Tool

### Usage

Here's a simple example with a cycle between two classes, Foo and Bar.

```
$ cat Foo.java
package mypkg;
public class Foo {
  Bar myBar;
}

$ cat Bar.java
package mypkg;
public class Bar {
  Foo myFoo;
}

$ cycle_finder Foo.java Bar.java
acceptAST: mypkg/Bar.java
acceptAST: mypkg/Foo.java

***** Found reference cycle *****
Bar -> (field myFoo with type Foo)
Foo -> (field myBar with type Bar)
----- Full Types -----
Lmypkg/Bar;
Lmypkg/Foo;

1 CYCLES FOUND.
````

In the output you will first see "acceptAST" for each java file being parsed. This is just informative logging.

For each cycle, there will be printed two lists. The first list contains a description of the cycle. Each line lists an edge in the reference graph. The edge description will show the origin type followed by a description of how the origin type might refer to the target type.

The second list, under "Full Types" lists the unique type keys for each type in the cycle. This is useful as it provides the full package of each type.

### Whitelisting

Some detected cycles will not require any corrective action. This may be because the cycle contains long-lived objects that don't need to be deallocated. Or the tool may detect a theoretical cycle based on the types of certain fields where it is provable that the objects involved will never form a cycle. For these cases we can use whitelist files to suppress these cycles.

The tool accepts whitelist files using the -w or --whitelist option. Whitelist files are plain text files containing one-line entries. A whitelist entry may take one of 4 forms demonstrated in the below example. It is recommended when adding whitelist entries to be as specific as possible to avoid suppressing a legitimate cycle. Comments can be added using the '#' character.

````
# Specifies that "fieldA" in ClassA will not refer to any objects of type ClassB (or any subtype of ClassB)
FIELD my.pkg.ClassA.fieldA my.pkg.ClassB

# Suppresses all cycles containing "fieldA" in ClassA.
FIELD my.pkg.ClassA.fieldA

# Suppresses all cycles containing any field of ClassA.
TYPE my.pkg.ClassA

# Suppress all cycles containing the Inner's outer reference to ClassA.
OUTER my.pkg.ClassA.Inner

# Suppress all cycles containing the outer reference from any anonymous class declared within myMethod.
OUTER my.pkg.ClassA.myMethod.$

# Suppresses all cycles containing any type in package "my.pkg".
NAMESPACE my.pkg
````
