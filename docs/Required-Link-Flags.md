---
title: Required Link Settings
layout: docs
---

# Required Link Settings

The link build step (Xcode's "Link Binary With Libraries" build phase) needs J2ObjC-specific flags,
which vary depending on how your application uses translated Java classes. The core flags are set by
the [j2objcc](j2objcc.html) command-line script, but need to be specified when building with Xcode.

## Library Search Path

J2ObjC's distribution includes several static libraries; to use them, your project needs to tell
the linker where to find them.

Generally, the library search path needs to include _$(j2objc_distribution)/lib_, where the
_$(j2objc_distribution)_ variable is the path to your local copy of J2ObjC.  For example, if you
unzipped a [J2ObjC release archive file](https://github.com/google/j2objc/releases) to 
"/usr/local/", this path would be "/usr/local/j2objc".

**Important**: Do not actually use _$(j2objc_distribution)_ in your project; always specify the
actual path where you installed J2ObjC.

If you build J2ObjC from a copy of its source code, then _$(j2objc_distribution)_ is your copy's 
"j2objc/dist/" directory. This directory will not exist until you build J2ObjC with `make dist`.

### Xcode: Library Search Paths

Update the app target's Library Search Paths by adding _$(j2objc_distribution)/lib_ (again, use
the real path).

## JRE Libraries

These libraries implement classes defined by J2ObjC's JRE emulation:

|Library|Link Flag|Description|
|---|---|---|
|libjre_core.a|-l jre_core|
  The minimum set of classes required for J2ObjC's JRE emulation, referenced by all
  generated source files. Using this If your translated Java sources reference JRE support for
  things like networking, XML, SQL, etc., then additional libraries will need to be linked in.|
|libjre_beans.a|-l jre_beans|
  Contains the classes from the java.beans package. Not all Java Beans classes are
  included, since many are only used by Swing and AWT apps.|
|libjre_channels.a<|-l jre_channels|
  The classes from the java.nio.channels and java.nio.channels.spi packages. The main
  java.nio classes are in the jre_core library, so only apps that explicitly use
  java.nio.channels need to link this library.|
|libjre_concurrent.a|-l jre_concurrent|
  The classes in the java.util.concurrent, java.util.concurrent.atomic and
  java.util.concurrent.locks packages.|
|libjre_io.a|-l jre_io|
  Several classes in the java.io package. Many java.io classes are in jre_core, though,
  so only include this library if there are unresolved JavaIo* symbol errors.|

### libjre_emul.a (**-l jre_emul**)

The `jre_emul` library contains all the classes included in J2ObjC's JRE emulation. If an app is
linked with `jre_emul`, none of the other jre_* libraries should be included, or the linker will
report duplicate symbol errors. That is because `jre_emul` is includes all classes defined in
those other libraries.

## Other J2ObjC Libraries

These are popular Java libraries and Android util classes, which are included in the J2ObjC
distribution:

|Library|Link Flag|Description|
|---|---|---|
|libguava.a|-l guava|
  [Guava: Google Core Libraries for Java](https://github.com/google/guava)|
|libjavax_inject.a|-l javax_inject|
  The [JSR-330](https://jcp.org/en/jsr/detail?id=330) dependency injection annotation library.|
|libjsr305.a|-l jsr305|
  The [JSR-305](https://jcp.org/en/jsr/detail?id=305) annotations for software defect detection
  library.|
|libjunit.a|-l junit -ObjC|The [JUnit](http://junit.org/) test framework.|
|libmockito.a|-l mockito -ObjC|
  The [Mockito](http://mockito.org/) mocking framework for unit tests in Java.|
|libprotobuf_runtime.a|-l protobuf_runtime|
  A [Google Protocol Buffer](https://developers.google.com/protocol-buffers/) runtime,
  optimized for J2ObjC apps. Apps using J2ObjC protobufs should compile their proto
  files with j2objc_protoc.|
|libandroid_util.a|-l android_util|
  The android_util library contains a small subset of the Android API utility classes.
  It is not intended to provide emulation for an Android environment, but just a way to share
  useful classes like android.util.Log.|

## The -ObjC Link Flag ##

The **-ObjC** flag is frequently used when linking iOS apps, but it is only required when Objective
C classes and categories need to be dynamically loaded from static libraries. This flag causes all
classes in all linked static libraries to be included in the app, whether or not they are actually
used. It's therefore recommended that apps that use J2ObjC only link with the **-ObjC** flag when
classes fail to load at runtime (one symptom is when `JavaLangClassNotFoundException` is thrown).

The JUnit and Mockito test frameworks rely heavily on reflection, so test apps that use them should
link with **-ObjC**.

An alternative to linking in a whole static library so a few classes can be dynamically loaded is
to instead statically reference those classes. In Java, this can be done in a static initializer
block; here's an example from J2ObjC's
[IosSecurityProvider](https://github.com/google/j2objc/blob/master/jre_emul/Classes/com/google/j2objc/security/IosSecurityProvider.java) class:

```java
  // Reference all dynamically loaded classes, so they are linked into apps.
  @SuppressWarnings("unused")
  private static final Class<?>[] unused = {
    IosCertificateFactory.class,
    IosMD5MessageDigest.class,
    IosRSAKeyFactory.class,
    IosRSAKeyPairGenerator.class,
    IosRSASignature.class,
    IosSecureRandomImpl.class,
    IosSHAMessageDigest.class
  };
```
