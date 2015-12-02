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

<table>
  <tr><th>Library</th><th>Link Flag</th><th>Description</th></tr>
  <tr>
    <td>libjre_core.a</td>
    <td style="white-space: nowrap;">-l jre_core</td>
    <td>
      The minimum set of classes required for J2ObjC's JRE emulation, referenced by all
      generated source files. Using this If your translated Java sources reference JRE support for
      things like networking, XML, SQL, etc., then additional libraries will need to be linked in.
    </td>
  </tr><tr>
    <td>libjre_beans.a</td>
    <td style="white-space: nowrap;">-l jre_beans</td>
    <td>
      Contains the classes from the `java.beans` package. Not all Java Beans classes are
      included, since many are only used by Swing and AWT apps.
    </td>
  </tr><tr>
    <td>libjre_channels.a</td>
    <td style="white-space: nowrap;">-l jre_channels</td>
    <td>
      The classes from the `java.nio.channels` and `java.nio.channels.spi` packages. The main
      `java.nio` classes are in the `jre_core` library, so only apps that explicitly use
      `java.nio.channels` need to link this library.
    </td>
  </tr><tr>
    <td>libjre_concurrent.a</td>
    <td style="white-space: nowrap;">-l jre_concurrent</td>
    <td>
      The classes in the `java.util.concurrent`, `java.util.concurrent.atomic` and
      `java.util.concurrent.locks` packages.
    </td>
  </tr><tr>
    <td>libjre_io.a</td>
    <td style="white-space: nowrap;">-l jre_io</td>
    <td>
      Several classes in the `java.io` package. Many `java.io` classes are in `jre_core`, though,
      so only include this library if there are unresolved JavaIo* symbol errors.
    </td>
  </tr><tr>
    <td>libjre_net.a</td>
    <td style="white-space: nowrap;">-l jre_net</td>
    <td>
      The classes in the `java.net` package. The `java.net.URLClassLoader` class is in
      `jre_security`, while the `javax.net` and `javax.net.ssl` classes are in `jre_ssl`.
    </td>
  </tr><tr>
    <td>libjre_security.a</td>
    <td style="white-space: nowrap;">-l jre_security</td>
    <td>
      Most classes in the `java.security` package (a few are in `jre_core`), as well as
      the classes in `java.security.*`, `javax.crypto.*`, and `javax.security.*`. 
    </td>
  </tr><tr>
    <td>libjre_sql.a</td>
    <td style="white-space: nowrap;">-l jre_sql</td>
    <td>
      The classes in the `java.sql` package.
    </td>
  </tr><tr>
    <td>libjre_ssl.a</td>
    <td style="white-space: nowrap;">-l jre_ssl</td>
    <td>
      The classes in the `javax.net` and `javax.net.ssl` packages.
    </td>
  </tr><tr>
    <td>libjre_util.a</td>
    <td style="white-space: nowrap;">-l jre_util</td>
    <td>
      Several classes from the `java.util` package, as well as the `java.util.logging` package. 
      Most `java.util` classes are in `jre_core`, though, so only include this library if there
      are unresolved JavaUtil* symbol errors (JavaUtilConcurrent* symbols are in the
      `jre_concurrent` library).
    </td>
  </tr><tr>
    <td>libjre_xml.a</td>
    <td style="white-space: nowrap;">-l jre_xml</td>
    <td>
      The classes from the XML-related packages, including `javax.xml.*`, `org.w3c.dom.*`, and
      `org.xml.sax.*`.
    </td>
  </tr><tr>
    <td>libjre_zip.a</td>
    <td style="white-space: nowrap;">-l jre_zip</td>
    <td>
      The classes from the `java.util.zip` and `java.util.jar` packages.
    </td>
  </tr>
</table>

### libjre_emul.a (**-l jre_emul**)

The `jre_emul` library contains all the classes included in J2ObjC's JRE emulation. If an app is
linked with `jre_emul`, none of the other jre_* libraries should be included, or the linker will
report duplicate symbol errors. That is because `jre_emul` includes all classes defined in those
other libraries.

## Other J2ObjC Libraries

These are popular Java libraries and Android util classes, which are included in the J2ObjC
distribution:

<table>
  <tr><th>Library</th><th>Link Flag</th><th>Description</th></tr>
  <tr>
    <td>libguava.a</td>
    <td style="white-space: nowrap;">-l guava</td>
    <td>
      <a href="https://github.com/google/guava">Guava: Google Core Libraries for Java</a>
    </td>
  </tr><tr>
    <td>libjavax_inject.a</td>
    <td style="white-space: nowrap;">-l javax_inject</td>
    <td>
      The <a href="https://jcp.org/en/jsr/detail?id=330">JSR-330</a> dependency injection
      annotation library.
    </td>
  </tr><tr>
    <td>libjsr305.a</td>
    <td style="white-space: nowrap;">-l jsr305</td>
    <td>
      The <a href="https://jcp.org/en/jsr/detail?id=305">JSR-305</a> annotations for software
      defect detection library.
    </td>
  </tr><tr>
    <td>libjunit.a</td>
    <td style="white-space: nowrap;">-l junit -ObjC</td>
    <td>
      The <a href="http://junit.org/">JUnit</a> test framework.
    </td>
  </tr><tr>
    <td>libmockito.a</td>
    <td style="white-space: nowrap;">-l mockito -ObjC</td>
    <td>
      The <a href="http://mockito.org/">Mockito</a> mocking framework for unit tests in Java.
    </td>
  </tr><tr>
    <td>libprotobuf_runtime.a</td>
    <td style="white-space: nowrap;">-l protobuf_runtime</td>
    <td>
      A <a href="https://developers.google.com/protocol-buffers/">Google Protocol Buffer</a>
      runtime, optimized for J2ObjC apps. Apps using J2ObjC protobufs should compile their proto
      files with j2objc_protoc.
    </td>
  </tr><tr>
    <td>libandroid_util.a</td>
    <td style="white-space: nowrap;">-l android_util</td>
    <td>
      The `android_util` library contains a small subset of the Android API utility classes.
      It is not intended to provide emulation for an Android environment, but just a way to share
      useful classes like `android.util.Log`.
    </td>
  </tr>
</table>

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
