Guava version: 14.0.1

*** Modifications ***

For minor modifications and exclusions, grep for comments labeled
"J2ObjC Modifications".

The following classes are currently excluded:
- com.google.common.reflect.ClassPath
  - missing java.util.jar package
  - missing java.net.URLClassLoader

Finalizable references are excluded:
- com.google.common.base.FinalizableReference
- com.google.common.base.FinalizableSoftReference
- com.google.common.base.FinalizableWeakReference
- com.google.common.base.FinalizablePhantomReference
- com.google.common.base.FinalizableReferenceQueue
- com.google.common.base.internal.Finalizer

Updates Stopwatch to rev 957450e1f7f476718d7d13e7723baa325267c763, found at:
https://www.google.com/url?sa=D&q=https%3A%2F%2Fraw.githubusercontent.com%2Fgoogle%2Fguava%2F957450e1f7f476718d7d13e7723baa325267c763%2Fguava%2Fsrc%2Fcom%2Fgoogle%2Fcommon%2Fbase%2FStopwatch.java
Constructors are then marked public for backward compatibility.
