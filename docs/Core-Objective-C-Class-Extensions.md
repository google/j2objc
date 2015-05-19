---
layout: default
---

Objective-C has a feature alien to Java developers, categories.  A category is a kind of mix-in, which adds messages to an existing type without needing to modify it.  J2ObjC's JRE emulation library uses categories to make the core Foundation classes more supportive of Java applications.

## NSObject

The root of the Foundation framework is `NSObject`, which is similar to `java.lang.Object`.  Several methods were added to this class via the [NSObject+JavaObject category](https://github.com/google/j2objc/blob/master/jre_emul/Classes/NSObject%2BJavaObject.h):

 * `clone()`: the Foundation framework has a `NSCopying` protocol that is similar to `Object.clone()`, but several corner cases found it incompatible.  [Clone Clone support] was added to the translator, which required this method.

 * `compareTo()`: although `compareTo()` is not an `Object` method, the `Comparable` contract requires that a `ClassCastException` be thrown if the specified object's type prevents it from being compared.  This method therefore throws a `ClassCastException`, and is overridden by any class implementing `Comparable`.

 * `getClass()`: returns an `IOSClass` instance that maps to `java.lang.Class`. This distinction is necessary because Java considers Class instances to be true objects, while Objective-C doesn't.

 * `notify()`, `notifyAll()`, `wait()`: threading primitives that work with Objective-C's @synchronized support.

## NSString

`NSString` is the Foundation framework's answer to `java.lang.String`, but the two have very different capabilities and APIs, making mapping difficult.  String functions are generally performance-sensitive, too.  So rather than have translation support for the unmappable methods, they are implemented in the [NSString+JavaString category](https://github.com/google/j2objc/blob/master/jre_emul/Classes/NSString%2BJavaString.h).