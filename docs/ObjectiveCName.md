---
title: ObjectiveCName
layout: docs
---

#### com.google.j2objc.annotations

## Annotation Type ObjectiveCName

````
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR })
@Retention(RetentionPolicy.CLASS)
public @interface ObjectiveCName {

  /**
   * The Objective-C name to use.
   */
  public String value();
````

Annotation that specifies what the Objective-C class, protocol, method or constructor name should be when translated.
