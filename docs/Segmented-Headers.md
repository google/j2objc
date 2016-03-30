---
title: Segmented Headers
layout: docs
---

# Segmented Headers

J2ObjC generated headers files are divided into segments and can be included one segment at a time.
One segment is created for each translated Java type, so each inner class will have it's own segment.
Preprocessor macros are used to tell the compiler only to read a particular segment when including the header.Segmented headers solve the problem of include cycles in J2ObjC generated headers, described in detail below.

## What you need to know
* Use `#include` instead of `#import` to include J2ObjC generated headers.
  * Using `#import` with segmented headers is problematic because the compiler will skip reading the
  header if it has already seen it. But, because the header is segmented it may not have been fully
  parsed by the compiler the first time.
* Segmented headers can be disabled with the `--no-segmented-headers` flag.

## Circular Includes

J2ObjC generated header files must use includes and forward declarations to resolve necessary type
information. Forward declarations are used as much as possible, however includes are necessary for
types being extended or implemented because the compiler requires the full type declaration.

It is possible to generate include cycles in J2ObjC generated header files. To get such a cycle we
require a class in file A that extends a class in file B, and a class in file B that extends a class
in file A. This is an unlikely scenario, but it does occur in Guava's codebase (and elsewhere).

A natural fix for this problem might be to emit a separate header file for each Java type
encountered in a .java file. But J2ObjC is designed to be used as a build tool and any good build
system relies on **predictable outputs** for each input. That means its necessary that each .java file
produce exactly one .h and one .m file.

## Example

Foo.java:

```java
class Foo extends Bar {}
```

Bar.java:

```java
class Bar {
  static class Baz extends Foo {}
}
```

Foo.h (not segmented):

```objc
#ifndef _Foo_H_
#define _Foo_H_

#include "Bar.h"
#include "J2ObjC_header.h"

@interface Foo : Bar
- (instancetype)init;
@end

#endif // _Foo_H_
```

Bar.h (not segmented):

```objc
#ifndef _Bar_H_
#define _Bar_H_

#include "Foo.h"
#include "J2ObjC_header.h"

@interface Bar : NSObject
- (instancetype)init;
@end

@interface Bar_Baz : Foo
- (instancetype)init;
@end

#endif // _Bar_H_
```

Notice that Foo.h includes Bar.h and Bar.h includes Foo.h. As a result these headers fail to compile:

```
$ ../dist/j2objcc -c Foo.m
In file included from Foo.m:6:
In file included from ./Bar.h:9:
./Foo.h:12:18: error: cannot find interface declaration for 'Bar', superclass of 'Foo'
@interface Foo : Bar
~~~~~~~~~~~~~~   ^
```

Below are the segmented versions of Foo.h and Bar.h, which will compile without error.

Foo.h (segmented):

```objc
#include "J2ObjC_header.h"

#pragma push_macro("Foo_INCLUDE_ALL")
#if Foo_RESTRICT
#define Foo_INCLUDE_ALL 0
#else
#define Foo_INCLUDE_ALL 1
#endif
#undef Foo_RESTRICT

#if !defined (_Foo_) && (Foo_INCLUDE_ALL || Foo_INCLUDE)
#define _Foo_

#define Bar_RESTRICT 1
#define Bar_INCLUDE 1
#include "Bar.h"

@interface Foo : Bar
- (instancetype)init;
@end

#endif

#pragma pop_macro("Foo_INCLUDE_ALL")
```

Bar.h (segmented):

```objc
#include "J2ObjC_header.h"

#pragma push_macro("Bar_INCLUDE_ALL")
#if Bar_RESTRICT
#define Bar_INCLUDE_ALL 0
#else
#define Bar_INCLUDE_ALL 1
#endif
#undef Bar_RESTRICT

#if !defined (_Bar_) && (Bar_INCLUDE_ALL || Bar_INCLUDE)
#define _Bar_

@interface Bar : NSObject
- (instancetype)init;
@end

#endif

#if !defined (_Bar_Baz_) && (Bar_INCLUDE_ALL || Bar_Baz_INCLUDE)
#define _Bar_Baz_

#define Foo_RESTRICT 1
#define Foo_INCLUDE 1
#include "Foo.h"

@interface Bar_Baz : Foo
- (instancetype)init;
@end

#endif

#pragma pop_macro("Bar_INCLUDE_ALL")
```
