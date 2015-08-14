---
title: LoopTranslation
layout: docs
---

#### com.google.j2objc.annotations

## Annotation Type LoopTranslation

```java
@Target(ElementType.LOCAL_VARIABLE)
@Retention(RetentionPolicy.SOURCE)
public @interface LoopTranslation {

  /**
   * Enumerates the available translation options for enhanced for loops.
   * FAST_ENUMERATION is the default style emitted by the translator.
   */
  enum LoopStyle {
    /*
     * for (id var in expr) {
     *   ...
     * }
     */
    JAVA_ITERATOR,

    /*
     * id<JavaUtilIterator> iter__ = [expr iterator];
     * while ([iter__ hasNext]) {
     *   id var = [iter__ next];
     *   ...
     * }
     */
    FAST_ENUMERATION
  }

  LoopStyle value();
```

Annotation that specifies how an enhanced for loop should be translated by the J2ObjC translator.
