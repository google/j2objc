---
layout: docs
---

The Destructor Generator creates memory management-specific destructor methods as needed.

If the class has a `finalize method`, it is renamed when necessary and its statements are executed before any generated code.

- With the default Resource Counting management, the new method is called `dealloc`.  Statements are added that send a release message to each object field.
- When using garbage collection, the new (or existing) method is called `finalize`, just like Java.  Statements are added to null each field, then call the superclass's `finalize` method.
- With Automatic Resource Counting (ARC), a new method is not created.  If a `finalize` method exists, it is renamed to `dealloc` and its `super.finalize();` statement is removed.
