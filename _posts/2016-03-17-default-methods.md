---
layout: posts
title: Default Methods
author: Lukhnos Liu
category: blog
tags: j2objc "java 8"
---

We are pleased to announce the support for Java 8 default methods in J2ObjC.
The compiler also translates static methods in interfaces now. Together they
make Java interfaces more useful, and, when combined with lambdas, enable a wide
range of new idioms and programming styles. To translate this new language
feature, pass the command line argument `-source 8` to `j2objc`.

[The Java
Tutorials](https://docs.oracle.com/javase/tutorial/java/IandI/defaultmethods.html)
has a good introduction to default and static methods in interfaces. Another
good introduction is Richard Warburton's [_Java 8
Lambdas_](http://shop.oreilly.com/product/0636920030713.do). Here we give three
short examples to show why this language feature is useful.


### Providing New Features that Build Upon Essential Methods

Default methods enable us to keep the requirements of an interface minimal
while still providing good *default* implementations that build upon those
essential methods. Imagine you have an interface for objects (for example,
appliances) whose date and time can be set:

```java
interface DateTimeSettable {
  void setDate(int year, int month, int day);
  void setTime(int hour, int min);
}
```

The interface does not have a way to set date and time in one go. An API
designer used to have two choices: one is to keep the API minimal and require
everyone who needs to set both date and time to make two separate method calls;
the other is to add a new `setDateTime()` method and require every class that
implements `DateTimeSettable` to also implement `setDateTime()`. The second
approach can have rippling effects if your version 1 API only has `setDate()`
and `setTime()` and gets widely adopted (that is, your interface gets
implemented by a lot of classes -- a sign of your API's success!) but now your
version 2 API starts to require the new `setDateTime()` implementation (and
risks the ire of your existing v1 adopters).

With default methods, we can keep the API minimal while still providing a richer
feature set:

```java
interface DateTimeSettable {
  void setDate(int year, int month, int day);
  void setTime(int hour, int min);

  default void setDateTime(int year, int month, int day, int hour, int min) {
    setDate(year, month, day);
    setTime(hour, min);
  }
}
```


Every class that implements `DateTimeSettable` now gets a new default method,
`setDateTime()`, for free. An implementing class can also override that method
to provide a more specific implementation.


### Adding Utility Methods to an Interface

Static methods in an interface serve the same purposes as those in a class. We
can use them to provide utility or factory methods. Before this, we had to
create another utility class for an interface. Now we can just add them to the
same interface. For example, let's design a helper method that applies the same
date and time to a group of objects using Joda Time's
[DateTime](http://www.joda.org/joda-time/apidocs/org/joda/time/base/AbstractDateTime.html):

```java
interface DateTimeSettable {
  static void setDateTime(Collection<DateTimeSettable> objs, DateTime dateTime) {
    for (DateTimeSettable obj : objs) {
      obj.setDateTime(...);
    }
  }

  // ...
}
```


### Pairing Functional Interfaces with Default Methods

Default methods really shine when you pair them with functional interfaces.
Imagine you have a `Predicate<T>` that tells you something about the object of
type `T`:

```java
interface Predicate<T> {
  boolean test(T t);
}
```

Now we have a `Student` class:

```java
class Student {
  public int yearGraduated = -1;
  public boolean knowsObjC = false;
}
```

We can create property filters to help us find graduated students or those who
know Objective-C:

```java
Predicate<Student> graduated = (s) -> s.yearGraduated != -1;
Predicate<Student> knowsObjC = (s) -> s.knowsObjC;
```

Suppose we want to filter students who are still in school. One way to do it is
to create a different predicate:

```java
Predicate<Student> notGraduated = (s) -> s.yearGraduated == -1;
```

Similarly, we can create another predicate for students who have graduated and
know Objective-C:

```java
Predicate<Student> objCGraduate = (s) -> s.yearGraduated != -1 && s.knowsObjC;
```

But there's a better way. We can remove such error-prone repetition by using
two new default methods in `Predicate<T>`:

```java
interface Predicate<T> {
  boolean test(T t);
  default Predicate<T> negate() {
    return (x) -> !test(x);
  }
  default Predicate<T> and(Predicate<T> o) {
    return (x) -> test(x) && o.test(x);
  }
}
```

New predicates can be easily created this way:

```java
Predicate<Student> notGraduated = graduated.negate();
Predicate<Student> objCGraduate = graduated.and(knowsObjC);
```

And to create a filter for intern candidates who knows Objective-C:

```java
Predicate<Student> objCIntern = knowsObjC.and(graduated.negate());
```


### Enabling Default Methods in J2ObjC

In previous J2ObjC versions, supplying `-source 8` did nothing for default method
or static method declarations in an interface. The compiler now handles them
when the proper language level is set.


### How J2ObjC Translates Default Methods

Objective-C does not have the notion of default methods. Since default methods
can only call methods in the declaring interface or in its super-interfaces, we
first turn a default method into a C function that takes a `self` parameter. So
if our interface looks like this:

```java
interface P {
  default void f(String x) {
    g(x);
  }

  void g(String y);
}
```

The translated default method will be a function `P_fWithNSString` that takes a
self as its first argument:

```objc
void P_fWithNSString_(id<P> self, NSString *x) {
  [self gWithNSString:x];
}
```

Then, for every class that implements interface `P`, we add default method shims
that call their actual implementations. So for a class `Q`:

```java
class Q implements P {
  @Override
  public void g(String y) { ... }
}
```

The Objective-C method `-[Q fWithNSString:]` (the default method that `Q` gets
from `P`) is implemented as:

```objc
@implementation Q
// ...

- (void)fWithNSString:(NSString *)arg0 {
  P_fWithNSString_(self, arg0);
}

// ...
@end
```

Of course, if `Q` overrides `f`, the overriding method takes precedence, and no
shim will be generated in such case.


### Making Use of the Companion Class

When we added reflection support to interfaces, we decided to generate one
companion class for each interface. In Objective-C terms, this means for each
translated `@protocol` we generate one `@interface` under the same name.
Objective-C allows a protocol and a class to share the name (`NSObject` is the
best example), and we took advantage of this feature to store the reflection
metadata of an interface.

It turns out that companion class is essential in supporting static interface
methods as well as default methods in functional interfaces (see the
`Predicate<T>` example above). Internally, static methods in an interface are
translated to C functions, like static field getters and setters are.  Still, we
want to maintain a good level of Objective-C interoperability, and therefore we
also generate a class method shim in the interface's companion class. So if an
interface `P` has a static method `f`:

```java
interface P {
  static void f(String x) { ... }
}
```

The translated Objective-C declarations will be:

```objc
@protocol P < NSObject, JavaObject >
@end

@interface P : NSObject < P >
+ (void)fWithNSString:(NSString *)x;
@end
```

And the implementation of `+[P fWithNSString:]` is just a wrapper class method
that calls the actual C function that implements the original `P.f()`.


### Default Methods in Lambdas

When a lambda is created from a functional interface such as the `Predicate<T>`
example above, the lambda will also get the default methods from that functional
interface. Previously, all our lambdas are runtime-created classes that subclass
`NSObject` with runtime-added protocol conformance. We didn't want to inject
default methods when we create a class in runtime, as it would be inefficient.

Once again, companion classes come to the rescue. We add default method shims
to companion classes, as if they were normal classes that implement an
interface with default methods. When a lambda gets created, and if the lambda
is from a functional interface, the lambda will be a subclass of that
functional interface's companion class, and thus it will have all the shims
that in turn call the actual default method implementations.

