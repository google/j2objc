---
layout: docs
---

# How to specify prefixes for package names.

## Name Mapping

Java uses packages to informally define namespaces; while Objective C++ has C++ namespaces, Objective C doesn't.  To preserve name uniqueness when using classes from multiple packages, J2ObjC prepends a camel-cased version of the package to the type name.  For example, `java.util.Map` is renamed to `JavaUtilMap`.

Unfortunately, camel-cased package names can reduce readability of the generated code, especially with long package names.  For example, Google Guava's [Beta annotation](http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/annotations/Beta.html) is in the `com.google.common.annotations` package, and `ComGoogleCommonAnnotationsBeta` is harder to read than `Beta`.

## Defining a Package Prefix

The Objective-C convention for defining informal namespaces is to use a shared prefix, usually two uppercase letters.  The iOS Foundation Framework uses "NS" (from [NeXTStep](http://en.wikipedia.org/wiki/NeXTSTEP)), for instance.  To simplify Google Guava's Beta name, a prefix such as "GG" would improve readability by referring to `Beta` as `GGBeta`.

J2ObjC supports developers specifying their own prefixes to map to package names.  This is done on the command line using "`--prefix `*package*=*prefix*.  To shorten all the class names in `Beta`'s package, the "`--prefix com.google.common.annotations=GG` would be used. A separate prefix declaration is needed for each package.

## Defining Multiple Package Prefixes

Google Guava has multiple packages, and each package that should be shortened would need its own prefix argument (packages can share the same prefix text, but need separate declarations).  To simplify specifying several prefix definitions, a properties file can be used with the "`--prefixes` *file*" argument:
````
$ cat prefixes.properties
com.google.common.annotations: GG
com.google.common.base: GG

# While GG can be used for all packages, let's mix it up.
com.google.common.collect: GC
com.google.common.io: GIO        # A prefix can be more than two characters,
com.google.common.net: GuavaNet  # a lot more!
...
$ j2objc --prefixes prefixes.properties <args>
````

## Prefixed Classes at Runtime

Since the finished app has classes with prefixes, they cannot be located using the original Java class name by default. However, if the app has a file named *prefixes.properties* in its resource bundle with the prefixes used for translation, `Class.forName(javaName)` will find the mapped class. 

To add the above *prefixes.properties* to an iOS app in Xcode, open the build target's Build Phases tab, expand its Copy Bundle Resources section, and add the *prefixes.properties* file to that list. [[Java Resources]] has further information on how Java resource concepts map to iOS resources.
