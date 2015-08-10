---
title: Reading Generated Sources
layout: docs
---

# Reading Generated Sources

### Class name mapping

Since J2ObjC only translates non-UI Java classes, an iOS application needs to directly work with the translated classes.  Translated classes are regular NSObject-rooted Objective-C classes, but it helps to understand how class, method, parameter and header file names are converted.

Java uses packages to define namespaces, while Objective-C doesn't have packages and instead has a convention of putting a shared prefix in front of related classes (like NSObject and NSString).  To preserve Java namespaces, [package names are mapped](#Name_Mapping) to a camel-cased prefix.  For example, `java.util.List` is mapped by default to `JavaUtilList`.  This default prefix can be explicitly set using j2objc's [package prefix options](Package-Prefixes.html).

**Note:** We're considering [shortened parameter names](https://github.com/google/j2objc/issues/23), so if you have an opinion, please comment on [the issue](https://github.com/google/j2objc/issues/23).

### Parameter names

Java differentiates overloaded methods by their argument types, while Objective-C uses argument names.  J2ObjC therefore creates argument names from their types.  For example, here is how an object is inserted  into the beginning of an `ArrayList`, and how that list is then added to another list:
```obj-c
[someList addWithInt:0 withId:object];
[otherList addAllWithJavaUtilCollection:somelist];
```

Each argument name consists of "with" plus its type.  This is a bit ugly ([issue 23](https://github.com/google/j2objc/issues/23)), but ensures that the same method is always invoked in Objective-C as it was with Java.

### Header names

Header files do not have their names mapped, however, but instead have the Java source file path ending with ".h" instead of ".java".  This makes supporting tools like Xcode and Make much easier, but can be a little confusing at first, since:
```obj-c
#import "java/util/Date.h"
```
declares the `JavaUtilDate` class, not `Date`.

### Using translated classes

Other than the odd names (which can be simplified with [[package prefixes|Package Prefixes]]), translated classes are used like any other Objective-C class:
```obj-c
#import "java/util/BitSet.h"
...
  JavaUtilBitSet *bitset = [[JavaUtilBitSet alloc] init];
  [bitset setWithInt:10 withBOOL:YES];
  BOOL b = [bitset getWithInt:10];
```
