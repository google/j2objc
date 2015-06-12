---
layout: docs
---

# Memory Profiling with MemDebug

As described in [[Memory Management]], iOS requires applications to retain and release the objects they create. [Xcode Instruments](http://developer.apple.com/library/mac/#documentation/DeveloperTools/Conceptual/InstrumentsUserGuide/Introduction/Introduction.html) has several useful tools for profiling memory use in its Instruments:

 * Object Alloc, which shows what objects are in allocated memory,
 * Leaks, which shows where memory is leaking (unable to be reused), and
 * [Zombie Detection](http://developer.apple.com/library/mac/documentation/developertools/Conceptual/InstrumentsUserGuide/MemoryManagementforYouriOSApp/MemoryManagementforYouriOSApp.html#//apple_ref/doc/uid/TP40004652-CH11-SW8), which finds objects that were over-released.

_It is strongly recommended that developers first work with these tools before using MemDebug. MemDebug is useful for developers experienced with iOS profiling and want additional information about their app's memory allocation._

## Introducing MemDebug
J2ObjC provides a third method to profile memory, MemDebug. It's not as easy to use as Xcode's Instruments, but can provide more complete allocation information on the Java files that were translated. When running, a graph of object allocations is built, as well as a log of allocation stack traces. To view the object graph, the generated graph file is viewed with [Graphviz](http://www.graphviz.org/); for example:

<< picture here >>

A log of stack traces for all allocations is also created. Here's a few lines from one of those log files:

<< picture here >>

## How to Use MemDebug

Since profiling memory can impact performance, J2ObjC by default generates Objective-C files without profiling support. To create files that gather profiling data, use the --mem-debug flag:
```sh
$ j2objc -d build -sourcepath src --mem-debug src/*.java
```

If a Java source file has a `main()` method, the translator inserts a call to `JreMemDebugGenerateAllocationsReport` at the end of the method. If the application doesn't have a `main()` method, or data is wanted from a different checkpoint in the application's lifecycle, then that function is added by the developer in any of the app's source files.

iOS applications don't have a main method, so it's easiest to add a "debug view" with a button to call `JreMemDebugGenerateAllocationsReport` to generate the reports.

## MemDebug Reports

MemDebug reports are created in a J2ObjC/ directory in your ~/Library/Logs directory. Each time an app with MemDebug enabled is run, a new numbered sub-directory is created with a .dot Graphviz file, and a .log text file.

###Installing Graphviz

To see the `.dot` file, Graphviz needs to be installed. It takes a couple of steps because by default, the `~/Library` directory is not visible in the Finder, and `.dot` files aren't associated with Graphviz automatically.

 * Download the Graphviz binary package for MacOS and install it.
 * After running an app with MemDebug enabled, in a terminal enter:
```sh
$ open ~/Library/Logs/J2ObjC
```
 * Open the latest (highest number) log directory, then right-click on the `.dot` file.
 * Select "Open With", then select "Other" (not Graphviz).
 * Select Graphviz from the list of applications, click the "Always Open With" checkbox, and click the Open button.

Now `.dot` files can be opened from a terminal using the open command.

###Allocation Graphs

An application graph is a graph of all objects and their relationships that are "live" when the report was generated. Graphviz displays this graph with nodes for the objects and edges which object are being held by other objects. Each node shows the class name, a unique id number, followed by how many objects of this type are being held by the owning object.

<< graph >>
