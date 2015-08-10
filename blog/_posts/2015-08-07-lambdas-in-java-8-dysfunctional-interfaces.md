---
layout: posts
title: Lambdas in Java 8 - (Dys)functional Interfaces
author: Seth Kirby
category: blog
tags: j2objc ios lambdas "java 8"
---

<aside class="quote" align="center"><h6><strong>1970 - Guy Steele and Gerald Sussman create Scheme.</strong><br>Their work leads to a series of "Lambda the Ultimate" papers culminating in "Lambda the Ultimate Kitchen Utensil." This paper becomes the basis for a long running, but ultimately unsuccessful run of late night infomercials. Lambdas are relegated to relative obscurity until Java makes them popular by not having them.</h6>
<strong>James Iry</strong>
<br>
A Brief, Incomplete, and Mostly Wrong History of Programming Languages
</aside>

So we finally have lambda expressions in Java with Java 8.  Well, moreso we have for a year now, and companies and universities have started upgrading at a snails pace.  Some from Java 6 to Java 7, but lets ignore that for now and instead talk about lambdas, implementation details, and how this all impacts j2objc and you.

Before we talk about lambdas we have to talk about functional interfaces, and after we talk about functional interfaces we will need to mitigate confusion with history, with a short lambda ancestry.

### Basic use and ancestry

A basic functional interface Func might be implemented as follows:

{% highlight java %}
@FunctionalInterface
interface Func {
  Object apply(Object x);
}
{% endhighlight %}

We have a new ```@FunctionalInterface``` annotation, but it is entirely optional, as any interface with only one non-Object method is a functional interface.  We have also explicitly defined the types of the functional interface method, when we could use generic types.  Far more often you are going to see functional interfaces like the following:

{% highlight java %}
interface Func<T, R> {
  R apply(T x);
}
{% endhighlight %}

We drop the annotation, add generic types, and with this we can look at our first lambda:

{% highlight java %}
Func<Object, Object> hello = (Object x) -> x + " World!";

System.out.println(hello.apply("Hello"));
{% endhighlight %}

With type inference and the removal of some optional parentheses, we can go a step further:

{% highlight java %}
Func hello = x -> x + " World!";

System.out.println(hello.apply("Hello"));
{% endhighlight %}

If you have written an anonymous class before, and are seeing lambdas for the first time, I'll give you a moment to find your party hat, clean up the confetti, and refactor your codebases.  If you haven't, this is what you have been missing:

{% highlight java %}
Func<Object, Object> hello = new Func<Object, Object>() {
  @Override
  public Object apply(Object x) {
    return x + " World!";
  }
};

System.out.println(hello.apply("Hello"));
{% endhighlight %}

5 lines of boilerplate for such a small function, but technically Java has had functional concepts in place for quite a while, if you overlook the repetitive boilerplate; which is easier to do when you realize that you have probably been writing single use classes in this style for a while:

{% highlight java %}
class Identity implements Func<Object, Object> {
  @Override
  public Object apply(Object x) {
    return x + " World!";
  }
}
Func<Object, Object> identity = new Identity();

identity.apply("Hello");
{% endhighlight %}

Okay, lets look at lambdas one more time in case you missed the confetti memo:

{% highlight java %}
x -> "Hello " + x;

x -> {
  return "Hello " + x;
}

Func<Integer, Integer> f = (Integer x) -> x * x;
{% endhighlight %}

### Why are lambdas important?

First off, don't ask this question randomly in the wild, as you might accidentally find a functional programmer, and wander away from the ensuing conversation much later intellectually drowning in parentheses, type theory, monads, and the like. Among this laundry list of reasons however you will probably hear something about closures and state capturing.  A closure is a function stored together with its environment, and lambdas are just one instance of closures in Java.  For each variable that is not defined within the closure, we store the value of the variable or of the reference to the variable's storage location.

Since closures hold the enclosing state, they play nicely together in a concurrent environment as long as they don't mutate referenced variables.  Looking at a typical loop over a List:

{% highlight java %}
MyList<String> ls = getUsers();

for (int i = 0; i < ls.size(); i++) {
  operate(ls.get(i));
}
{% endhighlight %}

We link the ordering of iteration tightly with the application of our operation on each item of our list.  In many cases, however, we aren't concerned about the order in which the List is traversed, but only on the guarantee that each item is operated on.  A functional representation of the same operations, sans the ordering contract might look like:

{% highlight java %}
MyList<String> ls = getUsers();

ls.each(x -> operate(x));
{% endhighlight %}

Aside from being cleaner, as long as our lambda isn't mutating referenced variables, our list library is given the freedom to optimize for a concurrent environment, while the implementation details are abstracted away and left as an exercise for the library.

### Implementation details

If you are interested in a deeper discussion of the implementation details, direction, and decisions that went in to Java's lambdas, take a look at [Lambdas in Java: A peek under the hood](https://www.youtube.com/watch?v=MLksirK9nnE), a great deep dive by Brian Goetz.  It covers the history of implementations that were ruled out, runtime implementation, invokedynamic, performance specifics, possible future direction, and a host of interesting topics that we aren't going to begin to talk about.

We have two kinds of lambdas, capturing or stateful lambdas and non-capturing or stateless lambdas.  A capturing lambda is bound with the values or references to enclosing variables, as such:

{% highlight java %}
int outerY = 42;

(int x) -> outerY;
{% endhighlight %}

Capturing lambdas have similar performance characteristics to anonymous classes, with a one time cost for setting up the capture, and the overhead of a new instance for each created lambda.

Non-capturing lambdas don't touch the enclosing state:

{% highlight java %}
() -> 1;

(int x) -> x;

(int x, int y) -> {
  if (x < y) {
    return x;
  } else {
    return y;
  }
}
{% endhighlight %}

Non-capturing lambdas are very common, often replacing a limited use method near the context of its use.  Each non-capturing lambda only requires a single instance, is lazily initialized, and offers significant performance gains over the equivalent anonymous class on repeated uses.

### Relevance to j2objc

We have had lambda expressions on our radar for j2objc [for a while](https://github.com/google/j2objc/issues/500), but we have been gated by a few issues, most importantly the state of the overall code sharing environment, as j2objc is part of a larger ecosystem of platform-independent [shared code across Google](http://arstechnica.com/information-technology/2014/11/how-google-inbox-shares-70-of-its-code-across-android-ios-and-the-web/).

We have been working on translating language features, keeping in mind the following goals for our implementation:

- Mimic Java behavior as closely as possible.
- Preserve the performance characteristics of lambdas, specifically the performance gains of non-capturing lambdas.
- Future-proof our implementation against potential code generation and Object method behavior changes in Java's future.  For more on this, again look at [Goetz's talk](https://www.youtube.com/watch?v=MLksirK9nnE), which talks about how they preserved the ability to easily change lambda implementation going forward.
- Leverage Objective-C blocks for readability and capturing.  We already have a strategy for captures from our anonymous class implementation, but we can reduce complexity by leaning on Objective-C's closures.
- Preserve reflection.  Currently lambdas don't offer any guarantee for serialization, but this may change in the future, and we want the ability to respond quickly to any changes without having to change the entirety of our model.

Our strategy within generated code is to subclass Function classes and wrap block calls within an instance or class, for non-capturing and capturing lambdas respectively.

In this way we:

- Preserve reflection.
- Preserve object methods.
- Retain performance benefits of stateless lambdas.
- Leverage variable capturing of Objective-C blocks.
- Work within the existing j2objc model.

For a non-capturing lambda:

{% highlight java %}
(Integer x) -> x
{% endhighlight %}

We generate a block in Objective-C:

{% highlight objc %}
^JavaLangInteger *(id _self, JavaLangInteger * x){
  return x;
}
{% endhighlight %}

This block is swizzled into an Objective-C class as a class method on the first assignment, during which an instance is created.  Future references to this lambda are instance lookups within our [FastPointerLookup](https://github.com/google/j2objc/blob/master/jre_emul/Classes/FastPointerLookup.m).

Lambda classes are created at runtime, using [```objc_allocateClassPair```](https://developer.apple.com/library/mac/documentation/Cocoa/Reference/ObjCRuntimeRef/#//apple_ref/c/func/objc_allocateClassPair) and [```objc_registerClassPair```](https://developer.apple.com/library/mac/documentation/Cocoa/Reference/ObjCRuntimeRef/#//apple_ref/c/func/objc_registerClassPair).  We add the currently unused ```_self``` parameter so we can extract an implementation for the block using [```imp_implementationWithBlock```](https://developer.apple.com/library/mac/documentation/Cocoa/Reference/ObjCRuntimeRef/#//apple_ref/c/func/imp_implementationWithBlock), and we register this implementation as a class method using [```class_addMethod```](https://developer.apple.com/library/mac/documentation/Cocoa/Reference/ObjCRuntimeRef/#//apple_ref/c/func/class_addMethod).

For capturing lambdas, we create the classes at runtime using the same methods, but we need an instance and block for each lambda, as each lambda has captures a potentially unique outer state.  We end up creating one block per class which is swizzled in as a class method to retrieve and apply blocks:

{% highlight objc %}
^JavaLangInteger *(id _self, JavaLangInteger * a) {
  id (^block)() = objc_getAssociatedObject(_self, 0);
  return block(_self, a);
}
{% endhighlight %}

[```objc_getAssociatedObject```](https://developer.apple.com/library/mac/documentation/Cocoa/Reference/ObjCRuntimeRef/#//apple_ref/c/func/objc_getAssociatedObject) is retrieving an underlying instance block stored with [```objc_setAssociatedObject```](https://developer.apple.com/library/mac/documentation/Cocoa/Reference/ObjCRuntimeRef/#//apple_ref/c/func/objc_setAssociatedObject):

{% highlight objc %}
^JavaLangInteger *(id _self, JavaLangInteger * x){
  return outerY;
}
{% endhighlight %}

Future instances of this lambda are class lookups which create a new instance and set the underlying block as an associated object.

These two implementations are encapsulated in GetNonCapturingLambda and GetCapturingLambda within [JreEmulation](https://github.com/google/j2objc/blob/master/jre_emul/Classes/JreEmulation.m) for those looking for the source.

### Using lambdas in j2objc

For now you need to pass two flags to j2objc for lambda transpilation, ```-source 8``` and ```-Xforce-incomplete-java8``` as Java 8 support is still very preliminary.