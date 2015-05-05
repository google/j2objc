Guava version: 18.0

*** Modifications ***

For minor modifications and exclusions, grep for comments labeled
"J2ObjC Modifications".

The following classes are currently excluded:
- com.google.common.reflect.ClassPath
  - missing java.util.jar package

Finalizable references are excluded:
- com.google.common.base.FinalizableReference
- com.google.common.base.FinalizableSoftReference
- com.google.common.base.FinalizableWeakReference
- com.google.common.base.FinalizablePhantomReference
- com.google.common.base.FinalizableReferenceQueue
- com.google.common.base.internal.Finalizer
