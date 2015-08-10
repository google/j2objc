---
title: Autoboxer
layout: docs
---

The Autoboxer translation phase converts primitive references into their equivalent numeric wrappers (boxing), and vice versa (unboxing).  The converted AST is very similar to how javac modifies its AST, with the same performance.

### Boxing

j2objc boxes expressions with primitive types by calling the appropriate wrapper class's `valueOf()` method.  Because the expression is used as the parameter to that method, only one boxing method call is made regardless of the complexity of the expression.  For example, where i and j are ints:
```java
    return i >= 0 ? i + j : i - j;
```
becomes:
```java
    return Integer.valueOf(i >= 0 ? i + j : i - j);
```

### Unboxing

j2objc unboxes numeric wrapper instances using the appropriate primitive type method for that class, such as `booleanValue()` or `longValue()`.  Like for boxing, arbitrarily-long expressions are passed to that method, to reduce overhead.
