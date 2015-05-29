---
layout: default
---

# Writing Native Methods

J2ObjC supports embedding Objective-C into Java native methods, very similar to how [GWT's JSNI](https://developers.google.com/web-toolkit/doc/latest/DevGuideCodingBasicsJSNI) supports JavaScript embedding.  The main difference between J2ObjC embedding and GWT's is that J2ObjC uses `/**-[` and `]-**/` to delineate Objective-C code.  This facility is called **OCNI** (Objective-C Native Interface), to differentiate itself from GWT's JSNI.

Here's an example from the JRE emulation library's version of `java.lang.System`:

```java
  public static native long currentTimeMillis() /*-[
    // Use NSDate
    return (long long) ([[NSDate date] timeIntervalSince1970] * 1000);
  ]-*/;
```

J2ObjC copies the comment, minus the delimiters, to create the method body:

```objc
  + (long long int)currentTimeMillis {
    // Use NSDate
    return (long long) ([[NSDate date] timeIntervalSince1970] * 1000);
  }
```

> **Warning**: if no OCNI comment is included in a native method declaration, J2ObjC will create
> and use an external native function with a name defined by the
> [Java Native Interface (JNI)](http://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/jniTOC.html).
> This function needs to be separately implemented in a C/C++ file, as is done for native code
> on Java platforms. If the native function isn't implemented, the linker will report an error
> for each function that is missing.

### Native Imports

J2ObjC scans the Java code being translated to add #import directives for its dependencies, as well as importing the Foundation framework.  However, any imports needed only by native code need to be separately added.  To add imports, add an OCNI section above the first class in the Java source file and specify the imports there; for example:

```java
  package my.project;
    
  /*-[
  #import "java/lang/NullPointerException.h"
  ]-*/
    
  public class Test {
    native void test() /*-[
      @throw [[JavaLangNullPointerException alloc] init];
    ]-*/;
  }
````

The import is necessary in the above example because the only place that type is referenced is in 
native code.

### Native Blocks

Within a class body, J2ObjC scans for OCNI blocks. These blocks are added unmodified to the translated file, in the same position relative to translated class members. Here's an example:

```java
  /*-[
    static void log(NSString *msg) {
      NSLog(@"%@", msg);
    }
  ]-*/;
```

This C function can be invoked from any native methods that are declared after this OCNI block.

### J2ObjC and GWT

Different delimiters were chosen so that in the next J2ObjC release, GWT JSNI comments will be ignored (previously the same delimiters were used as GWT). This means that a single Java source can have native methods that have Objective-C, GWT, and Android (via JNI) implementations:

```java
  static native void log(String text) /*-{ // left-brace for JavaScript
    console.log(text);
  }-*/ /*-[                                // left-bracket for Objective-C
     NSLog(@"%@", text); 
  ]-*/;
```

### J2ObjC and Android

J2ObjC and Android native method implementations "just work", because Android native methods are implemented in a separate JNI C or C++ file. Any OCNI comments in Java classes are removed when compiled by javac for Android or any other Java platform.