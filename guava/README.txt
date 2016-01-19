Guava version: 19.0

*** Modifications ***

Finalizable reference classes are excluded:
- com.google.common.base.FinalizableReference
- com.google.common.base.FinalizableSoftReference
- com.google.common.base.FinalizableWeakReference
- com.google.common.base.FinalizablePhantomReference
- com.google.common.base.FinalizableReferenceQueue
- com.google.common.base.internal.Finalizer

FuturesGetChecked has its @J2ObjCIncompatible sections manually removed, since they aren't stripped when building j2objc_guava.jar with javac.

# MOE:begin_strip

GoogleInternal is included, even though it's not part of public Guava.

GwtIncompatible no longer requires a "reason" value (CL 110776317)

VisibleForTesting includes MOE-stripped productionVisibility() support.

# MOE:end_strip
