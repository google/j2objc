---
layout: docs
---

There is no notion of static variables in Objective-C; data is only associated with instances.  However, code that references static variables and constants can be translated to simulate static variables effectively.

## Declaration

Accessor methods are used to simulate static variable.  A getter with the same name as the variable is added, and returns the declared type.  Unless the variable is final, a standard setter method is added.  Code that directly references the variable is translated as using the accessors instead.  For example:
```java
class Test {
public static Date currentDate = new Date();
...
currentDate = anotherDate;
```
becomes:
```obj-c
@interface Test : NSObject {
}

+ (JavaUtilDate *)currentDate;
+ (void)setCurrentDateWithJavaUtilDate:(JavaUtilDate *)currentDate;
@end
...
[Test setCurrentDateWithJavaUtilDate:anotherDate];
```
Primitive constants are different; they are output as `#define` directives, with a class name prefix for the name.  Float.SIZE, for example, becomes:
```obj-c
#define JavaLangFloat_SIZE 32
```

## Implementation

In the implementation file, the variable is declared as a C variable, static so its name cannot clash.  The accessor methods simply get and set this variable that is otherwise invisible to the application. The initializers for static variables are moved to the class initialize method.
