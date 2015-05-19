---
layout: default
---

# Changing Method Names

Method names can be changed using the **--mapping** flag, which takes a properties file with the mappings to use. Each method mapping is defined with the full Java method signature for the key, and a Objective-C declaration value. For example, the line to map Object.equals() to NSObject.isEqual: is:

```
java.lang.Object.equals(Ljava/lang/Object;)Z = NSObject isEqual:(id)anObject
```

The left hand declaration is the full method signature, as [defined by the Java Virtual Machine Specification](http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3.4). The right hand definition consists of the iOS class, followed by the message's declaration. The method and its mapping must have the same parameters, in the same order.

Additional mapping files can be specified on the command-line, using the --mapping option.

>**Note:** parameter names specified in the message declaration (such as _anObject_ in the above example) are ignored, and the name of the parameter in the Java source is used. This avoids the need to convert code that references that parameter, making it easier to compare to the Java version.