---
layout: default
---

# How Java Resources map to iOS Resources

## What are Java Resources

Java resources are data files that are packaged with Java applications and libraries. These resources are loaded at runtime either `Class.getResource(String name)`, which returns a `java.net.URL`, or `Class.getResourceAsStream(String name)`, which returns a `java.io.InputStream`. The `getResourceAsStream()` method is normally used when a resource is expected to be available, since it throws an `IOException` if it isn't. The `getResource()` method returns null if the resource isn't present, so it's useful to test for optional resources.

## Resource Names and Paths

The resource name is a relative path from the class's package, after converting the periods ('.') in the package name to forward slashes ('/'). For example, if an app has a `foo.bar.Mumble` class, the base path for resources relative to that class is `foo/bar`. `Mumble.class.getResource("oops/error.jpg")` will therefore have a relative path of `foo/bar/oops/error.jpg` and a relative directory of `foo/bar/oops`.

J2ObjC locates resources by looking in the application's main bundle (`[NSBundle mainBundle]`), using the resource's relative path (described above). 

## Adding Resources to an iOS App

To add resource files to an iOS app in Xcode, open the build target's Build Phases tab. Then:
 * Click the **+** icon (under the General tab) and select New Copy Files Phase.
 * Select "Resources" as the Destination (**not Java Resources**).
 * Specify the relative directory for the resource(s).
 * Select **+** and add the file(s) to its list.

Multiple resources that have the same relative directory can be included in one Copy Files build phase, but resources with different paths need separate Copy Files phases.

## Example

The [JreEmulation project](https://github.com/google/j2objc/tree/master/jre_emul/JreEmulation.xcodeproj) has a "JRE JUnit Tests" app that runs that library's unit tests. Select that target's Build Phases to see several Copy Files phases, one each for every relative path used by its resources:

![Xcode resources](https://raw.github.com/google/j2objc/master/doc/wiki_images/xcode-resources.png)


