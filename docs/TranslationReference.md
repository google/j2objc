---
title: Translation Reference
layout: docs
---

# Translation Reference

## Types
* For primitive types, J2ObjC has defined JNI-style typedefs.
* For typical class types, the package is camel-cased and prepended to the class name.
  * To rename the generated package prefix see [Package Prefixes](Package-Prefixes.html).
* For the base Object type and all type variables, "id" is used.
* A few core Java types are mapped to foundation types. (eg. String to NSString)
* For fields declared 'volatile', J2ObjC has more typedefs that use C11 _Atomic(...) types.
* For inner types the inner class name is appended to the outer name with an underscore.
  * **TODO(kstanger): Should we use '$' instead of '_' to be consistent with Java?**

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

## Methods
Objective-C methods differ from Java methods in two important ways. They are syntactically
different, embedding the parameters in between components of the method selector. Objective-C
methods don't support overloading like Java does. These differences are resolved by embedding the
parameter types into the generated selector. This is necessary to prevent name collisions between
overloaded Java methods.

Java static methods are translated to Objective-C class methods.

Method names are generated as follows:

* Zero parameter methods are unchanged
* One or more parameter uses the following pattern:
  * `<java name>With<1st param keyword>:with<2nd param keyword>:with<3rd param keyword>:`
* Parameter keyword rules:
  * For primitive types, the keyword is the capitalized name of the Java primitive. (eg. "Char")
  * For non-primitive types, the keyword is the camel-cased fully qualified type name. (eg. "ComGoogleFoo")
  * For array types, "Array" is appended to the keyword of the element type.

##### Example Declarations
```
void foo();

- (void)foo;
```
```
static int foo();

+ (jint)foo;
```
```
String foo(int i);

- (NSString *)fooWithInt:(jint)i;
```
```
static java.util.List foo(String s, long[] l);

+ (id<JavaUtilList>)fooWithNSString:(NSString *)s
                      withLongArray:(IOSLongArray *)l;
```

## Fields

### Instance Fields (non-static)
Java instance variables become Objective-C instance variables. The name is the same with a trailing
underscore. Primitive fields declared "final" are a special case and are not translated to instance
variables.

* Fields can be accessed directly using "->" syntax.
* Primitive fields can be set directly.
* Non-primitive fields must be set using the provided setter function:
  * `ClassName_set_fieldName_(instance, value)`
* Final primitives are accessed as a constant with the following name:
  * `ClassName_fieldName`

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
```objc
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
**TODO(kstanger): Maybe remove underscores?**

### Static Fields
Static variables must be accessed using the provided getter and setter functions.
These accessor functions ensure that class initialization has occurred prior to accessing the variable.
The function names depend on the type of variable:

* Primitive Constant (primitive type with "final" declaration)
  * `ClassName_get_fieldName()`
* Primitive
  * `ClassName_get_fieldName_()`
  * `ClassName_getRef_fieldName_()` // returns a pointer to the variable
* Non-primitive
  * `ClassName_get_fieldName_()`
  * `ClassName_set_fieldName_()`

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
```objc
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
**TODO(kstanger): Provide "\_set\_" function for primitives.**
**TODO(kstanger): Resolve inconsistency with trailing underscores.**

## Enums
J2ObjC generates two types for each Java enum. A Objective-C class type is generated which provides
the full functionality of a Java enum. Additionally a C enum is generated using the Foundation
framework's NS_ENUM macro. The generated C enum is not used within translated code, but may provide
a lighter alternative for use in native Objective-C code.

The generated enum types are named as follows:

* The C enum is named using the same rule as a regular Java class. (see [Types](#Types))
* The fully functional Objective-C class is named as a regular Java class with the "Enum" suffix.

**TODO(kstanger): Reverse this naming so that the generated class name is consistent with other Java classes.**

Enum constants are accessed like static fields.

##### Example Java
```java
package com.google;
enum Color {
  RED, GREEN, BLUE
}
```

##### Example Objective-C Header
```objc
typedef NS_ENUM(NSUInteger, ComGoogleColor) {
  ComGoogleColor_RED = 0;
  ComGoogleColor_GREEN = 1;
  ComGoogleColor_BLUE = 2;
};

@interface ComGoogleColorEnum : JavaLangEnum < NSCopying >
+ (IOSObjectArray *)values;
+ (TestEnum *)valueOfWithNSString:(NSString *)name;
@end

inline ComGoogleColorEnum *ComGoogleColorEnum_get_RED();
inline ComGoogleColorEnum *ComGoogleColorEnum_get_GREEN();
inline ComGoogleColorEnum *ComGoogleColorEnum_get_BLUE();
```
