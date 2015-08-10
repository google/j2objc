---
title: Object.clone() support
layout: docs
---

The Foundation framework has a `NSCopying` protocol that is similar to `Object.clone()`, but different enough to be incompatible.  It's a useful protocol that used widely, however, so `Cloneable` classes should implement it.

The solution was to add `clone()` to `NSObject` [[using a category|Core-Objective-C-Class-Extensions]], which does the same shallow copy that `Object.clone()` does.  The `Cloneable` interface is mapped to `NSCopying`, and a `copyWithZone:` method is added to `Cloneable` classes that calls the class's `clone()` method.

One issue with this solution is that because properties are used for instance variables, there is a memory leak when a `clone()` method updates any fields after calling `super.clone()`.  The solution is to have the statement generator modify assignment expressions in a `clone()` method to directly set its fields.
