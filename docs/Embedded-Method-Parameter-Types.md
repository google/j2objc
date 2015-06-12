---
layout: docs
---

## The problem with method overloading

Some developers are initially shocked when they see J2ObjC translate concisely named Java methods into verbose Objective-C.  For example:
```java
public StringBuilder append(char[] str, int offset, int len) {
```
becomes:
```obj-c
- (JavaLangStringBuilder *)appendWithJavaLangCharacterArray:(IOSCharArray *)str
                                                    withInt:(int)offset
                                                    withInt:(int)len;
```
The difference is due to the method's parameter types being embedded in the method signature.  This is necessary because of the different ways Java and Objective-C support method overloading.

Java class files have a method's type information, including the types of each parameter.  Objective-C uses selectors, which are similar to method pointers.  A selector is formed from taking the its name, and appending each parameter, but only up to each colon.  For example:
```obj-c
- (NSUInteger)indexOfObject:(id)anObject;
- (NSUInteger)indexOfObject:(id)anObject inRange:(NSRange)range;
```
The first method's selector is "indexOfObject:", while the second's is "indexOfObject:inRange:".  Every method in a class must have a unique selector, so method overloading is limited to unique strings for the method name and its parameters.  Java's ability to define "valueOf(int)" and "valueOf(String)" in the same class cannot be implemented in Objective-C without changing the names to something like "valueOfInt:" and "valueOfString:".

In theory, Java parameter names could be used, since they are unique.  In append() above, for example, the new method selector could be "append:offset:len:", which would be consistent with Objective-C naming conventions.  This works for method declarations like above, but fails when the method is invoked from another class.  There may not be source available for the called method, and class files don't generally include parameter name information (normally it's only included when debugging).  Java class files can only be counted on having parameter type information.

The solution is therefore to embed that type information in the parameters, so that the method signature generated for a method declaration always matches its callers.
