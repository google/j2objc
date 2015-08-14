---
title: RetainedLocalRef
layout: docs
---

#### com.google.j2objc.annotations ####

## Annotation Type RetainedLocalRef

```java
@Target(LOCAL_VARIABLE)
@Retention(SOURCE)
public @interface RetainedLocalRef {
}
```

Annotation that indicates a local variable should be retained outside of any
subsequent AutoreleasePool use. Otherwise, if a local variable has a copy
of an object in a container that is removed in an AutoreleasePool, it will
be deallocated before the local variable goes out of scope.

For example, a ThreadPoolExecutor is used to process a list of tasks,
removing each task from the list as it is processed. ThreadPoolExecutor
tasks are run inside of an AutoreleasePool, since these executors are often
long-lived. If a local variable is initialized to one of the task list's
elements, by default that variable won't be valid (will be deallocated)
after task processing. Adding a LocalRetain annotation to the local
variable ensures it is still valid after task processing.
