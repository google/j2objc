---
title: ReflectionSupport
layout: docs
---

#### com.google.j2objc.annotations ####

## Annotation Type ReflectionSupport

```java
@Target({ ElementType.TYPE, ElementType.PACKAGE })
@Retention(RetentionPolicy.Source)
public @interface ReflectionSupport {

  /**
   * Enumerates the available levels of reflection support.
   */
  enum Level {
    /*
     * No metadata is emitted, so reflection support is limited to the
     * information that can be obtained from the Objective-C runtime.
     */
    NATIVE_ONLY,
    /*
     * Additional metadata is emitted, allowing for full reflection support.
     */
    FULL
  }

  Level value();
```

Annotation that specifies the level of reflection support for a particular
class.
