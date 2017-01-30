---
layout: posts
title: Breaking Retain Cycles with @Weak and Other Techniques
author: Lukhnos Liu
category: blog
excerpt: In this blog post, we'll talk about how you can break retain cycles in your Java code. We will cover the @Weak and @WeakOuter annotations as well as a few additional tools and techniques you can use.
tags: "memory management"
---

The runtime on iOS and macOS uses reference counting to manage memory. When
two objects make strong references to each other, a retain cycle is formed.
Consider a Document object that has a Header. The Document owns the Header,
but often the Header is also modeled to have a document (or “parent”) field:

```java
class Document {
  private final Header header = new Header(this);
}

class Header {
  private final Document document;
  Header(Document document) { this.document = document; }
}
```

If nothing breaks one of those two strong references (such as by setting one
of them to null), a retain cycle is formed, and both objects will never be
deallocated in a reference-counting system even after all outside references
to any of them are gone. They therefore leak memory.

Retain cycles are something Objective-C and Swift programmers have to think
about regularly. They are usually not of concern to Java programmers thanks to
garbage collection, but when your Java program gets translated to Objective-C,
you need to make sure that retain cycles are eventually broken to prevent
memory leaks.

Our experiences show that the number of changes required to fit a Java program
into the reference-counting model is usually small. J2ObjC also provides a
number of tools to help you break retain cycles. Below is a list of the things
that will be useful in your toolbox when working on a cross-platform project
using J2ObjC:

* **Use the @Weak annotation to create a weak reference**. A weak reference
  does not retain the referenced object, and the rule of thumb is to use that
  in the field that references the owner, or the longer-living, object. In the
  example above, since a document owns a header, you can use a weak reference
  in Header so that there will never be a retain cycle:

  ```java
  class Header {
    @Weak private final Document document;
  }
  ```
* **Use the @WeakOuter annotation for inner classes**. In J2ObjC, every inner
  class (not to be confused with nested classes, the ones you declare with the
  static keyword) has an implicit reference to its outer class. By default,
  the reference is strong. If you create inner class instances that are
  retained by their outer class (such as for callback purposes), it’s likely
  you want to add the @WeakOuter annotation to the inner class to make the
  implicit outer reference weak:

  ```java
  @WeakOuter
  class SomeCallback {
    void foo() { OuterClass.this.doSomething(); }
  }
  ```
* **Be careful with lambdas**. If a lambda calls a method in your class or
  make references to a field, it implicitly captures this, and so it has the
  same issue as inner classes. You can’t add an annotation directly to a
  lambda, but you can use a temporary variable for that purpose. Here’s an
  example:

  ```java
  void someMethod() {
    @WeakOuter SomeFunctionalInterface f;
    anotherMethod(f = () -> { /* do the work in lambda */});
  }
  ```

  Note how we assign the lambda to a temporary variable and then pass the
  variable to anotherMethod. When J2ObjC sees this pattern, it will correctly
  make a weak reference to the lambda’s implicitly captured this.
* **Promote anonymous classes to inner classes**. Since Java 8, it is possible
  to create anonymous instances with type annotations. For example: `Runnable
  r = new @Foo Runnable() { … };` For the time being, though, J2ObjC’s 
  @WeakOuter still needs to be backward compatible with Java 7. This means you
  have to promote your anonymous classes to inner classes, so you can add 
  @WeakOuter to them.
* **Use WeakReference&lt;T&gt; only if you have no choice**. Although J2ObjC
  supports java.lang.WeakReference, it’s best to avoid it. It’s not supported
  by GWT (as there are no weak references in JavaScript), and it has a
  performance cost. If you need to wrap a weak reference in something like an
  Lazy&lt;T&gt; or Optional&lt;T&gt;, consider if you can use @Weak on the
  outer wrapper instead. Use something like
  Optional&lt;WeakReference&lt;T&gt;&gt; only if you really have no choice.
* **Only use @RetainedWith as the last-resort tool**. Sometimes it is
  impossible to determine which of the two objects in a retain cycle lives
  longer. For example, each
  [Guava BiMap](https://google.github.io/guava/releases/19.0/api/docs/com/google/common/collect/BiMap.html)
  has an inverse. They make strong references to each other and you can retain
  and use either. It is therefore impossible to use @Weak there, and that
  motivated the introduction of @RetainedWith. It is provided as a “last
  resort” tool for foundational libraries such as Guava, and you should read
  its
  [documentation](https://github.com/google/j2objc/blob/3fe3a48c50957e9784d598f20bfe54d53595d95e/annotations/src/main/java/com/google/j2objc/annotations/RetainedWith.java#L23)
  and understand what it does and doesn’t as well as its performance tradeoff
  before using it.
* **Be careful with Observers**. While it’s tempting to make an Observable use
  a weak reference to its Observer, that can go wrong quickly. If your
  Observable is the only object that make reference to the Observer (as is
  often the case in Java: the Observer is usually written as an anonymous
  class or a lambda), the Observer will then be gone shortly. The correct way
  is to use a strong reference and call removeObserver(). It is just like
  managing resource objects (think of files): Observers should be removed just
  like resource objects should be closed explicitly.
* **Not everything needs the @Weak or @WeakOuter treatment**. At this point
  you may be worried that you have to start adding @Weak and @WeakOuter all
  over your code base. In fact, only those objects that have cyclic references
  among them need this treatment. Use the following techniques to help you
  identify those, especially if you already have a substantial amount of Java
  code.
* **Use Cycle Finder**. J2ObjC provides
  [Cycle Finder](http://j2objc.org/docs/Cycle-Finder-Tool.html), a static
  analysis tool that finds potential reference cycles in your code. Use this
  as a guide, whitelist the cycles that don’t actually exist, and consider
  making it run as part of your continuous build process.

* **Familiarize yourself with Xcode’s Instruments, especially the Leaks and
  Allocation instruments**.
  [The Leaks instrument](https://developer.apple.com/library/content/documentation/DeveloperTools/Conceptual/InstrumentsUserGuide/FindingLeakedMemory.html)
  finds obvious retain cycles that are usually easy to fix. If it does report
  a list of leaked objects, we recommend you start from those found in your
  namespace. This is because when an object leaks memory, it also leaks the
  objects it retains. So when an object in your application leaks, you’ll
  often see J2ObjC objects (such as HashMaps) also leaking, and focusing on
  those library objects will lead you to a dead end (it’s very unlikely that
  they leak). You should also use the Allocation instrument for the ground
  truth. Although the Leaks instrument has become better over the years, there
  are limits to what it can find. If you have a complex object graph, compare
  the stats in Allocation before constructing and after tearing down the graph
  to make sure that all objects involved are no longer resident.

If you plan ahead and use the tools and techniques above, it is possible to
make your cross-platform Java code fit nicely in a reference-counting
environment. Effective memory management has positive impact to application
quality and user experience, and is therefore worth your attention and
investment.

For a detailed discussion, check out J2ObjC’s documentation on
[Memory Management](http://j2objc.org/docs/Memory-Management.html) and
[Memory Model](http://j2objc.org/docs/Memory-Model.html).

