---
title: Property
layout: docs
---

#### com.google.j2objc.annotations ####

## Annotation Type Property

```java
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Property {
  String value() default "";
}
```

Adds property declarations to generated Objective-C for annotated fields.

See [Apple's @property documentation](https://developer.apple.com/library/mac/documentation/Cocoa/Conceptual/ProgrammingWithObjectiveC/EncapsulatingData/EncapsulatingData.html).

Notes:

- Invalid attributes are reported as errors.
- __readwrite__, __strong__, and __atomic__ attributes are removed since they are defaults.
- Strings will include the __copy__ attribute.

Example:

```java
class Foo {
  @Property("copy, nonatomic") protected String bar;
}
```
generates:

```objc
@property (copy, nonatomic) NSString *bar;
```
