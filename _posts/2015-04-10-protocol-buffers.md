---
layout: posts
title: Protocol Buffers!
author: Keith Stanger
category: blog
tags: j2objc ios protobufs protocol-buffers
---
[Protocol Buffers](https://developers.google.com/protocol-buffers/) are Google’s preferred method of representing and communicating structured data. For most Google projects, protocol buffers are used for data storage or client-server communication. As such, a working protocol buffer solution has been a requirement for J2ObjC from day one. Until recently our solution contained internal dependencies that prevented it’s public release, but now I am very pleased to be able to make our internal solution available to all J2ObjC users.

Let’s take a quick look at how protocol buffers are used (for a more in-depth look you can read through the [Protocol Buffers Developer Guide](https://developers.google.com/protocol-buffers/docs/overview)). Suppose my app needs a geographic location, so I would create a geo.proto file with the following declaration:

{% highlight java %}
message Location {
  optional string name = 1;
  optional double latitude = 2;
  optional double longitude = 3;
  }
{% endhighlight %}

Then I can use the protocol buffer compiler, “protoc”, to generate data types in the languages I need:

{% highlight bash %}
$ protoc --java_out=src/java --cpp_out=src/cpp geo.proto
{% endhighlight %}

Now I have both a Java and a C++ class for my Location type that can be serialized to a language-independent binary form.

A working protocol buffer implementation for a particular language consists of two parts: the code generator, and an associated runtime. The code generator parses .proto files and generates code in the desired language. The runtime is a library implemented in the target language that provides serialization, base types, and any other support required by the generated types. Google protocol buffers support four languages: C++, Java, Python, and Ruby, but since protoc supports plugins, a plugin and runtime can be written for other languages and platforms.

There are actually several protocol buffer choices available for Java, all of which now work with J2ObjC:

- **Default protos** - Provides the most feature-full environment with builder types for your messages and reflective features. A very rich solution, but perhaps a little bloated for a mobile application.
- **“Lite” protos** - API compatible with the default protos, but requires only a small subset of the runtime library. Reflective features are stripped. See https://developers.google.com/protocol-buffers/docs/proto#options
- **javanano** - An extremely lightweight implementation. Message types are generated without getter or setter methods, only public fields.

All of the above are supported by J2ObjC. For “javanano” protos the solution is simple: just translate the generated sources and runtime as you would any other Java sources. For the default (and lite) protos we provide a protocol buffer generator that creates Objective-C code from your .proto files that is compatible with generated Java code, and a Objective-C runtime library that is compatible with the Java runtime.

There are two reasons that we provide a custom J2ObjC implementation. The first is performance; in particular, fast serialization and deserialization. When an Inbox user opens the app, their entire inbox is stored as binary protocol buffer data and must be deserialized before the page can be rendered. Mobile app developers know that start-up time is critical. Unfortunately, the deserialization code in the Java protobuf library hits on one of J2ObjC’s main weaknesses: object creation. With a carefully tuned runtime, however, we’re able to outperform any translated code. One reason for this is that we were able to reuse parts of the C++ protocol buffer runtime that has been optimized over years of development.

The second benefit of the our custom J2ObjC implementation is code size. The generated Java protocol buffers can be quite bloated, especially when not using lite protos. We’re able to take advantage of [Objective-C’s dynamic method resolution](https://developer.apple.com/library/prerelease/ios/documentation/Cocoa/Reference/Foundation/Classes/NSObject_Class/index.html#//apple_ref/occ/clm/NSObject/resolveInstanceMethod:) to avoid generating any field getter or setter implementations. All accessor methods are added dynamically by the message type’s base class. This helps minimize the footprint of generated data types.

Protocol buffers are an excellent alternative to XML or JSON. Consider using them to build your app’s data model and/or client-server interface. The binary serialization will save you space and the generated types make it easy to read, write, and edit your data. For more information about protocol buffers check out the [developer site](https://developers.google.com/protocol-buffers/). For instructions on how to build protocol buffers with J2ObjC, see our [documentation](https://github.com/google/j2objc/wiki/Using-Protocol-Buffers).