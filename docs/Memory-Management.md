---
title: Memory Management
layout: docs
---

# Memory Management

The first question most Java developers have is how is memory management implemented by J2ObjC, since Java has garbage collection and Objective-C doesn't by default.  What iOS does have are [two methods of memory management](http://developer.apple.com/library/mac/#documentation/Cocoa/Conceptual/MemoryMgmt/Articles/MemoryMgmt.html): reference counting, and Automatic Reference Counting (ARC).  

J2ObjC generates different memory management code, depending on which method is chosen.  By default it uses manual reference counting, which is the recommended method. Translate with the `-use-arc` option to generate code which uses ARC.

## Reference Counting

The [reference counting method](http://developer.apple.com/library/mac/#documentation/Cocoa/Conceptual/MemoryMgmt/Articles/mmRules.html#//apple_ref/doc/uid/20000994-BAJHFBGH) makes object ownership explicit.  A method owns an object when it creates it, until it releases that object.  When receiving an object from another method, the receiving method retains the object if it needs to be referenced after the method returns.  When a method no longer needs to reference an object, it must release it.  When an object's retain count is zero, its memory is freed and the object is no longer valid. When objects are freed, their dealloc() method is called to release ownership of their instance variables.

One problem with this technique is how to transfer ownership of an object. For example, a factory method may be called to create an object. If the factory method releases the object before returning it (since it no longer wants to own the object), then that object is freed before the calling method can retain it.

To transfer ownership of an object, a method sends it an autorelease message (instead of a release message), which defers the release message.  This allows the factory method to create an object to be returned, and relinquish its ownership without invalidating the object. At regular intervals (such as after each event loop iteration in an iOS application) the autorelease pool is "drained", meaning all objects in that pool are sent the deferred release messages. Any objects whose retain counts drop to zero are freed as normal.

Because the burden on memory management is on the developer, it's easy to leak memory with the reference counting method.  However, Apple recommends some [best practices](http://developer.apple.com/library/mac/#documentation/Cocoa/Conceptual/MemoryMgmt/Articles/mmPractical.html#//apple_ref/doc/uid/TP40004447-SW1) to minimize this problem, which J2ObjC implements.  

There is also runtime and tool support to detect memory leaks.  The Objective-C runtime reports any detected leaks when an application exits, which is one reason why J2ObjC translates JUnit tests into executable binaries. Xcode uses Clang, and that compiler has excellent static analysis for memory problems, which Xcode makes available with its Analyze command.

## Automatic Reference Counting (ARC)

[ARC](http://developer.apple.com/library/mac/#releasenotes/ObjectiveC/RN-TransitioningToARC/_index.html#//apple_ref/doc/uid/TP40011226) is Apple's recommended memory management method. It moves the responsibility for reference counting to the compiler, which adds the appropriate retain, release and autorelease methods during compilation.  ARC supports weak references for devices running iOS 5 and later. 

Not all projects use ARC, though, as it can sometimes be slower than hand-written code. This is the case with translated code. Since J2ObjC translated output is not intended to be modified, there is no benefit to translating into ARC code, there is only a potential performance cost.

By default, [ARC is not exception-safe](http://clang.llvm.org/docs/AutomaticReferenceCounting.html#exceptions). Specifically, it leaks memory when exceptions are thrown. Since exceptions are more common in Java and are usually recoverable, this can be problematic. Using -fobjc-arc-exceptions when compiling with arc will fix the leaks but adds a performance cost.

**We recommend that projects avoid using ARC for translated code.** Note that we do support the option to generate ARC code with the "-use-arc" flag for projects that prefer to build their entire apps with ARC.

We recommend that new projects use ARC for their hand-written Objective-C code, and only fall-back to manual reference counting if profiling data shows a real performance issue. Both ARC and non-ARC code can be compiled and linked into the same app without issue.

## Weak References

J2ObjC supports the `WeakReference` class, so Java code that uses it will work the same way when translated.  Fields can also be annotated with [com.google.devtools.j2objc.Weak](Weak), which the transpiler uses to generate fields that follow Objective-C weak reference semantics.  When using reference counting, this means that the field is not retained when initialized, and it is autoreleased when the containing instance is released.  With ARC, weak fields are marked with the `__weak` annotation, and the related properties are declared weak.

In some cases, an inner class instance gets in a reference cycle with its outer instance.  Here, a [com.google.devtools.j2objc.WeakOuter](WeakOuter) annotation is used to mark the inner class, so the reference to the outer class is treated as described above.  Other fields in the inner class are not affected by this annotation.

## Memory Management Tools

- [Cycle Finder Tool](Cycle-Finder-Tool.html) - analyzes Java source files for strong object reference cycles.
- [Xcode Instruments](https://developer.apple.com/library/mac/documentation/DeveloperTools/Conceptual/InstrumentsUserGuide/InstrumentsQuickStart/InstrumentsQuickStart.html) - Xcode's suite of profiling tools.
- [Xcode Memory Diagnostics](https://developer.apple.com/library/mac/recipes/xcode_help-scheme_editor/Articles/SchemeDiagnostics.html) - build options for running with memory diagnostics and logging. 
