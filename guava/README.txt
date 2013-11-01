Guava version: 14.0.1

*** Modifications ***

For minor modifications and exclusions, grep for comments labeled
"J2ObjC Modifications".

The following packages are currently excluded:
- com.google.common.net
- com.google.common.io
- com.google.common.cache
- com.google.common.reflect
- com.google.common.util.concurrent
- com.google.common.eventbus
- com.google.common.hash

Finalizable references are excluded:
- com.google.common.base.FinalizableReference
- com.google.common.base.FinalizableSoftReference
- com.google.common.base.FinalizableWeakReference
- com.google.common.base.FinalizablePhantomReference
- com.google.common.base.FinalizableReferenceQueue
- com.google.common.base.internal.Finalizer

Due to missing dependencies on java.util.concurrent, the following are excluded:
- com.google.common.collect.MapMaker
- com.google.common.collect.MapMakerInternalMap
- com.google.common.collect.GenericMapMaker
- com.google.common.collect.ComputingConcurrentHashMap
- com.google.common.collect.Queues
- com.google.common.collect.Interners
- com.google.common.collect.ConcurrentHashMultiset
