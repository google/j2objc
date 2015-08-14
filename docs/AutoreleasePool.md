---
title: AutoreleasePool
layout: docs
---

#### com.google.j2objc.annotations ####

## Annotation Type AutoreleasePool

```java
@Target(value={METHOD,LOCAL_VARIABLE})
@Retention(value=CLASS)
public @interface AutoreleasePool
```

Annotation that indicates the translator should inject an autorelease pool
around the method body. Only valid with for loops, and on methods that don't return anything.

Useful in high-level contexts to ensure that temporary objects allocated within the method or loop are deallocated.

Example usage:

```java
// Temporary objects allocated during execution of this method will
// be deallocated upon returning from this method.
@AutoreleasePool
public void doWork() {
  ...
}

public void doWork(Iterable<Runnable> workToDo) {
  // Adding @AutoreleasePool on the loop variable causes a separate
  // autorelease pool to be attached to each loop iteration, clearing
  // up temporary objects after each iteration
  for (@AutoreleasePool Runnable item : workToDo) {
    item.run();
  }
}
```
