---
title: Translation Reference
layout: docs
---

# Translation Reference

## Types
* For primitive types, J2ObjC has defined JNI-style typedefs.
* For typical class types, the package is camel-cased and prepended to the class name
  * TODO(kstanger): Add a pointer to package prefix documentation 
* For the base Object type and all type variables, "id" is used.
* A few core Java types are mapped to foundation types. (eg. String to NSString)
* For fields declared 'volatile', J2ObjC has more typedefs that use C11 _Atomic(...) types.
* For inner types the inner class name is appended to the outer name with an underscore.
  * TODO(kstanger): Should we use '$' instead of '_' to be consistent with Java?

|Java type|Objective-C type|Objective-C volatile type|
|---|---|---|
|boolean|jboolean|volatile_jboolean|
|char|jchar|volatile_jchar|
|byte|jbyte|volatile_jbyte|
|short|jshort|volatile_jshort|
|int|jint|volatile_jint|
|long|jlong|volatile_jlong|
|float|jfloat|volatile_jfloat|
|double|jdouble|volatile_jdouble|
|java.lang.Object|id|volatile_id|
|type variables|id|volatile_id|
|java.lang.String|NSString*|volatile_id|
|java.lang.Number|NSNumber*|volatile_id|
|java.lang.Cloneable|NSCopying*|volatile_id|
|foo.bar.Mumble|FooBarMumble*|volatile_id|
|foo.bar.Mumber$Inner|FooBarMumble_Inner*|volatile_id|

## Fields

### Instance Fields (non-static)
Java instance variables become Objective-C instance variables. The name is the same with a trailing underscore. Primitive fields declared "final" are a special case and are not translated to instance variables.
* Fields can be accessed directly using "->" syntax.
* Primitive fields can be set directly.
* Non-primitive fields must be set using the provided setter function:
  * ClassName_set_fieldName_(instance, value)
* Final primitives are accessed as a constant with the following name:
  * ClassName_fieldName

##### Example Java
```java
package com.google;
class Foo {
  public final int MY_FINAL_INT;
  public int myInt;
  public String myString;
}
```
##### Example Objective-C
```objective-c
Foo *foo = [[Foo alloc] init];
// Access a final primitive field.
jint i = Foo_MY_FINAL_INT;
// Access a primitive field.
i = foo->myInt_;
// Set a primitive field.
foo->myInt_ = 5;
// Access a non-primitive field.
NSString *s = foo->myString_;
// Set a non-primitive field.
ComGoogleFoo_set_myString_(foo, @"bar");
```
TODO(kstanger): Maybe remove underscores?

### Static Fields
Static variables must be accessed using the provided getter and setter functions.
These accessor functions ensure that class initialization has occurred prior to accessing the variable.
The function names depend on the type of variable:
* Primitive Constant (primitive type with "final" declaration)
  * ClassName_get_fieldName()
* Primitive
  * ClassName_get_fieldName_()
  * ClassName_getRef_fieldName_() // returns a pointer to the variable
* Non-primitive
  * ClassName_get_fieldName_()
  * ClassName_set_fieldName_()
  
##### Example Java
```java
package com.google;
class Foo {
  public static final MY_FINAL_INT = 5;
  public static int myInt;
  public static String myString;
}
```
##### Example Objective-C
```objective-c
// Access a primitive constant field.
jint i = ComGoogleFoo_get_MY_FINAL_INT();
// Access a primitive field.
i = ComGoogleFoo_get_myInt_();
// Set a primitive field.
*ComGoogleFoo_getRef_myInt_() = 5;
// Access a non-primitive field.
NSString *s = ComGoogleFoo_get_myString_();
// Set a non-primitive field.
ComGoogleFoo_set_myString_(@"bar");
```
TODO(kstanger): Provide "\_set\_" function for primitives.  
TODO(kstanger): Resolve inconsistency with trailing underscores.

TODO(kstanger): Describe Enums.
