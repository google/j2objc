Guava version: 18.0

*** Modifications ***

For minor modifications and exclusions, grep for comments labeled
"J2ObjC Modifications".

Finalizable reference classes are excluded:
- com.google.common.base.FinalizableReference
- com.google.common.base.FinalizableSoftReference
- com.google.common.base.FinalizableWeakReference
- com.google.common.base.FinalizablePhantomReference
- com.google.common.base.FinalizableReferenceQueue
- com.google.common.base.internal.Finalizer
