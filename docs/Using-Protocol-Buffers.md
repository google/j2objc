---
title: Using Protocol Buffers
layout: docs
---

# Using Protocol Buffers

Here is a quick example to demonstrate how to include protocol buffers in your project.

Here's a simple protocol buffer definition, `geo.proto`:

```proto
message Location {
  optional string name = 1;
  optional double latitude = 2;
  optional double longitude = 3;
}
```

And our main Java program, `Hello.java`:

```java
class Hello {
  public static void main(String[] args) {
    Geo.Location.Builder locationBuilder = Geo.Location.newBuilder();
    locationBuilder.setName("CN Tower");
    locationBuilder.setLatitude(43.6412172);
    locationBuilder.setLongitude(-79.3884058);
    Geo.Location location = locationBuilder.build();
    System.out.println(location.toString());
  }
}
```

First, a little "project" setup:

```bash
$ export J2OBJC_HOME=~/j2objc    # Change to where the j2objc distribution was unzipped.
$ ls $J2OBJC_HOME/j2objc         # Fix above definition until this command works.
$ mkdir java objc classes        # Output directories
```

Next, use `j2objc_protoc` to generate the protocol buffers. Generate Java code with `--java_out` and Objective-C code with `--j2objc_out`. The value specified with each flag is the output directory for the target language. Both output languages can be generated in the same command.

```bash
$ $J2OBJC_HOME/j2objc_protoc --java_out=java --j2objc_out=objc geo.proto
$ ls java
Geo.java
$ ls objc
Geo.h Geo.m
```

The generated Java proto files need to be compiled so references to them in `Hello` are resolved. This is done using `javac` so they aren't translated to Objective C in the following step.

```bash
$ javac -cp $J2OBJC_HOME/lib/protobuf_runtime.jar -d classes java/*.java
$ ls classes/
Geo$1.class                 Geo$Location$Builder.class  Geo$LocationOrBuilder.class
Geo$Location$1.class        Geo$Location.class          Geo.class
```

Translate the Java sources as usual, adding the compiled java protos to the classpath.

```bash
$ $J2OBJC_HOME/j2objc -cp classes:$J2OBJC_HOME/lib/protobuf_runtime.jar -d objc Hello.java
$ ls objc
Geo.h   Geo.m   Hello.h Hello.m
```

Now we have all of our Objective-C sources to compile and link. You'll need to link with the libprotobuf_runtime.a library to include the protobuf runtime.

```bash
$ $J2OBJC_HOME/j2objcc -lprotobuf_runtime -o hello objc/*.m
$ ./hello Hello
name: "CN Tower"
latitude: 43.6412
longitude: -79.3884
```
