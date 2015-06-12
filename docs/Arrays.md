---
layout: docs
---

Objective-C doesn't add any syntactic sugar for arrays; developers either use C arrays or Foundation collection classes like `NSArray`.  `NSMutableArray` is similar to `java.util.ArrayList`, while `NSArray` is like a `Collections.UnmodifiableList`.  Java arrays don't map to either of these classes, though, because they are both mutable and fixed size.  Like Java collections, Foundation collections only support object elements, so mapping primitive arrays to them would impact performance.  C arrays cannot be used as substitutes for Java arrays, because of the need for bounds checking, and support for the length and clone methods.

Consequently, array classes for objects and each primitive type were created.  These classes are thin wrappers around C arrays to be fast and small.  Java code that directly references arrays is translated into method calls.  Generated code that uses arrays will have IOS*Array references, such as `IOSObjectArray` or `IOSBooleanArray`.
