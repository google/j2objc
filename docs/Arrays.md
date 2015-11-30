---
title: Arrays
layout: docs
---

The Objective-C language doesn't add any syntactic sugar to arrays; developers either use C arrays or
Foundation collection classes like `NSArray`.  `NSMutableArray` is similar to `java.util.ArrayList`,
while `NSArray` is like a `Collections.UnmodifiableList`.  Java arrays don't map to either of these
classes, though, because they are both mutable and fixed size.  Like Java collections, Foundation
collections only support object elements, so mapping primitive arrays to them would impact
performance.  C arrays cannot be used as substitutes for Java arrays, because of the need for bounds
checking, support for the length, clone methods, and reflection support.

Consequently, array classes for objects and each primitive type are defined.  These classes are
thin wrappers around C arrays to be fast and small.  Java code that directly references arrays is
translated into function calls.  Generated code that uses arrays will have "IOS*Array" references,
such as `IOSObjectArray` or `IOSBooleanArray`.

### IOSArray

The base class for arrays is
[IOSArray](https://github.com/google/j2objc/blob/master/jre_emul/Classes/IOSArray.h). Being an
abstract class, it is never instantiated directly. `IOSArray` has methods common to all array types,
such as `length` and `elementType`.

All array types support the `clone` method. As with all J2ObjC types that implement the
`java.lang.Cloneable` interface, this maps to the iOS Foundation framework's
[NSCopying](https://developer.apple.com/library/mac/documentation/Cocoa/Reference/Foundation/Protocols/NSCopying_Protocol/)
protocol. J2ObjC arrays can be used wherever Foundation methods take `id<NSCopying>` instances,
such as `NSArray`.

### Primitive Arrays

Since there are separate array types for each Java primitive which are otherwise identical,
[IOSPrimitive.h](https://github.com/google/j2objc/blob/master/jre_emul/Classes/IOSPrimitiveArray.h)
defines all the primitive array types. Type-specific methods include an `IOSCharArray` constructor
that takes a string, and `IOSByteArray` that takes an NSData instance. `IOSByteArray` also has
direct access methods for accessing and replacing regions of its elements buffer.

In addition to access methods, IOSPrimitive.h defines access functions that avoid message passing
overhead. "Get" returns the element at a specified index, "GetRange" returns a specified range of
elements, and "SetRange" replaces a specified range of elements. Each function starts with the type
name, followed by the function's action; for example, `IOSByteArray_Get(array, index)`.

The "GetRef" functions are similar to "Get" functions, but return a pointer to the specified
element. This allows faster access to read or modify the contents of an array using C expressions.
Since this access has no range, bounds, or type checking, *it is critical that these functions be
used carefully.*

### Object Arrays

Object arrays are similar to primitive arrays, with a single
[IOSObjectArray](https://github.com/google/j2objc/blob/master/jre_emul/Classes/IOSObjectArray.h)
representing all non-primitive arrays. `IOSObjectArray` retains and releases its elements, like
`NSMutableArray` does.

In addition to the functions defined for primitive arrays, `IOSObjectArray` adds a "SetAndConsume"
function. This function releases the object being set if an exception is thrown by the function.

### Multi-dimensional Arrays

J2ObjC supports multi-dimensional arrays like Java does, with `IOSObjectArray` instances holding
primitive arrays. For example, an `int[3][5]` array is defined as an `IOSObjectArray` holding 3
`IOSIntArray` instances, each of which hold 5 ints. Additional dimensions are implemented as an
`IOSObjectArray` holding arrays of the next lower dimension. For example, a `byte[2][4][6]` array
consists of an `IOSObjectArray` holding 2 `byte[4][6]` arrays.

Although a C multi-dimensional array would provide faster access, we found that several libraries
require this nesting of array instances.
