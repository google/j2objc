---
title: Memory Model
layout: docs
---

# Memory Model

This document describes how memory is managed under J2ObjC translated code, and how programs behave when accessing shared memory.

## Memory Management

One of J2ObjC's goals is to produce translated code that will integrate seamlessly into Objective-C's reference counting environment. This makes translated Java code easy to use from natively written Objective-C because there is no awkward transfer of ownership for objects being passed between Java and Objective-C environments.

Since Java uses garbage collection for memory management, Java code contains no explicit memory management of its objects. J2ObjC must therefore insert reference counting calls appropriately to ensure that objects are deallocated at the right time. We have settled on the following set of rules that we've found to be both performant and practical:

* All objects will live for at least the duration of the current autorelease pool.
  * This general rule allows us to skip many retains and releases that would otherwise be necessary.
* Local variables are not retained.
  * No reference counting calls on reads or writes of local variables.
* Fields are retained.
  * Assignment of a field calls retain on the new value and autorelease on the old value.
* New objects are immediately autoreleased. (unless immediately assigned to a field)

### Reference Cycles

**It is possible for memory leaks to occur in translated code.**{: .j2objc-unsupported} This is an unavoidable side-effect of mapping from a garbage collected environment to a reference counted environment. Leaks occur when reference cycles are created in the Java source. A reference cycle exists when an object refers to itself either directly or indirectly through it's fields. There is no automated way to prevent reference cycles from occuring; however, we do provide a [Cycle Finder](Cycle-Finder-Tool.html) tool that automates detection of cycles. Here are some common ways to fix a reference cycle:

* Add a [@Weak](Weak.html) or [@WeakOuter](WeakOuter.html) annotation to weaken one of the references.
* Add a `cleanup()` method to one of the objects that sets some fields to null. Call `cleanup()` before discarding the object.
* Redesign the code to avoid creating a reference cycle altogether.

## Shared Memory

In a multi-threaded program, some data can be shared by multiple threads. Java provides several tools to allow thread-safe access to shared data. This section describes J2ObjC's support for accessing shared data.

### Synchronized

J2ObjC maps the `synchronized` keyword directly to Objective-C `@synchronized`.

### Atomicity

Java guarantees atomicity for loads and stores of all types except `long` and `double`. See [JLS-17.7](https://docs.oracle.com/javase/specs/jls/se8/html/jls-17.html#jls-17.7). With the exception of `volatile` types (described below) J2ObjC provides no special treatment to ensure atomic loads and stores. This implies the following:

* Since all iOS platforms are 32 or 64-bit, loads and stores of primitive types except `long` and `double` are atomic on 32-bit devices, and all are atomic on 64-bit systems.
* **Loads and stores of object types are not atomic in J2ObjC.**{: .j2objc-unsupported}
  * Atomically updating reference counts is too costly.
  * An object field can be made atomic by declaring it `volatile`. (see below)
  
### Volatile fields

For `volatile` fields, Java provides both atomicity and sequencially consistent ordering ([JLS-8.3.1.4](https://docs.oracle.com/javase/specs/jls/se8/html/jls-8.html#jls-8.3.1.4)), which can be used for synchronization. J2ObjC provides the same guarantees as Java for all `volatile` fields. J2ObjC uses the following mechanisms for `volatile` fields:

* Primitive types are mapped to c11 atomic types.
  * eg. `volatile int` -> `_Atomic(jint)`
* Object fields are protected with spin locks.
  * Mutual exclusion is necessary to prevent race conditions with the reference counting.
  * The implementation is very similar to Objective-C atomic properties.

### Atomic Types

Java provides a number of atomic types in the [java.util.concurrent.atomic](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/atomic/package-summary.html) package. These are all fully supported in J2ObjC with custom implementations.
  
### Final fields

[Issue 629](https://github.com/google/j2objc/issues/629): Java provides memory ordering semantics for final fields that are not yet supported by J2ObjC. ([JSL-17.5](https://docs.oracle.com/javase/specs/jls/se8/html/jls-17.html#jls-17.5))
