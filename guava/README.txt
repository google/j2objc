Guava version: 14.0.1

*** Modifications ***

For minor modifications and exclusions, grep for comments labeled
"J2ObjC Modifications".

The following packages are currently excluded:
- com.google.common.eventbus
- com.google.common.reflect

The following classes are currently excluded:
- com.google.common.io.Files
  - missing java.nio.MappedByteBuffer
  - missing java.nio.channels.FileChannel
- com.google.common.net.HostSpecifier
  - missing java.net.InetAddress
- com.google.common.net.InetAddresses
  - missing java.net.Inet4Address
  - missing java.net.Inet6Address
  - missing java.net.InetAddress
  - missing java.net.UnknownHostException

Finalizable references are excluded:
- com.google.common.base.FinalizableReference
- com.google.common.base.FinalizableSoftReference
- com.google.common.base.FinalizableWeakReference
- com.google.common.base.FinalizablePhantomReference
- com.google.common.base.FinalizableReferenceQueue
- com.google.common.base.internal.Finalizer
