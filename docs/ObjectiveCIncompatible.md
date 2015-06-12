---
layout: docs
---

#### com.google.j2objc.annotations ####

## Annotation Type ObjectiveCIncompatible

````
@Target({ ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.FIELD, 
    ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.Source)
public @interface ObjectiveCIncompatible {
````

Marks a declaration to be stripped by the J2ObjC translator prior to compilation. It is the 
developer's responsibility to ensure that any code depending on an element marked with 
@J2ObjCIncompatible is also marked with @J2ObjCIncompatible.
