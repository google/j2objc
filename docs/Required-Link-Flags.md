---
title: Required Link Flags
layout: docs
---

# Required Link Flags

The following linker flags need to be specified when building an application that includes J2ObjC-generated source files:
````
-L $(j2objc_distribution)/lib -l jre_emul -ObjC
````

### **-L $(j2objc_distribution)/lib**

The "$(j2objc_distribution)" path above is the path to your local copy of J2ObjC.  For example, if you unzipped a [J2ObjC release archive file](https://github.com/google/j2objc/releases) to "/usr/local/", this path would be "/usr/local/j2objc" **Important**: do not actually use "$(j2objc_distribution)" in your project; always specify the actual path where you installed J2ObjC.

If you build J2ObjC from a copy of its source code, then "$(j2objc_distribution)" is your copy's "j2objc/dist/" directory. This directory will not exist until you build J2ObjC with `make` or `make dist`.

### **-force_load $(j2objc_distribution)/lib/libjre_emul.a**

The ensures that the JRE emulation library's Objective-C categories are always loaded. Without this flag, some apps with fail with runtime errors that NSStrings do not respond to some selectors, because those selectors are defined by the JRE library's NSString+!JavaString category.

### **-l jre_emul**

This specifies J2ObjC's JRE emulation library, referenced by all generated source files.

### **-ObjC**

This flag tells the linker to include all Objective-C categories in your application. J2ObjC requires this flag because it has categories that extend NSObject and NSString to be support `java.lang.Object` and `java.lang.String` functionality.

**Note**: if you get "unrecognized selector" link errors, this flag wasn't set.

## JUnit Linker Flag

When linking translated JUnit tests, add the following flag to the above:
````
-l junit
````
