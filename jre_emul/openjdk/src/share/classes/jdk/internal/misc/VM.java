/*
 * Copyright (c) 1996, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.internal.misc;

import static java.lang.Thread.State.*;
import java.util.Map;
import java.util.Properties;

@jdk.internal.vm.annotation.Preserve(false)
public class VM {

    // the init level when the VM is fully initialized
    private static final int JAVA_LANG_SYSTEM_INITED     = 1;
    private static final int MODULE_SYSTEM_INITED        = 2;
    private static final int SYSTEM_LOADER_INITIALIZING  = 3;
    private static final int SYSTEM_BOOTED               = 4;
    private static final int SYSTEM_SHUTDOWN             = 5;


    // 0, 1, 2, ...
    private static volatile int initLevel;
    private static final Object lock = new Object();

    /**
     * Sets the init level.
     *
     * @see java.lang.System#initPhase1
     * @see java.lang.System#initPhase2
     * @see java.lang.System#initPhase3
     */
    public static void initLevel(int value) {
        synchronized (lock) {
            if (value <= initLevel || value > SYSTEM_SHUTDOWN)
                throw new InternalError("Bad level: " + value);
            initLevel = value;
            lock.notifyAll();
        }
    }

    /**
     * Returns the current init level.
     */
    public static int initLevel() {
        return initLevel;
    }

    /**
     * Waits for the init level to get the given value.
     *
     * @see java.lang.ref.Finalizer
     */
    public static void awaitInitLevel(int value) throws InterruptedException {
        synchronized (lock) {
            while (initLevel < value) {
                lock.wait();
            }
        }
    }

    /**
     * Returns {@code true} if the module system has been initialized.
     * @see java.lang.System#initPhase2
     */
    public static boolean isModuleSystemInited() {
        return VM.initLevel() >= MODULE_SYSTEM_INITED;
    }

    /**
     * Returns {@code true} if the VM is fully initialized.
     */
    public static boolean isBooted() {
        return initLevel >= SYSTEM_BOOTED;
    }

    /**
     * Set shutdown state.  Shutdown completes when all registered shutdown
     * hooks have been run.
     *
     * @see java.lang.Shutdown
     */
    public static void shutdown() {
        initLevel(SYSTEM_SHUTDOWN);
    }

    /**
     * Returns {@code true} if the VM has been shutdown
     */
    public static boolean isShutdown() {
        return initLevel == SYSTEM_SHUTDOWN;
    }

    // A user-settable upper limit on the maximum amount of allocatable direct
    // buffer memory.  This value may be changed during VM initialization if
    // "java" is launched with "-XX:MaxDirectMemorySize=<size>".
    //
    // The initial value of this field is arbitrary; during JRE initialization
    // it will be reset to the value specified on the command line, if any,
    // otherwise to Runtime.getRuntime().maxMemory().
    //
    private static long directMemory = 64 * 1024 * 1024;

    // Returns the maximum amount of allocatable direct buffer memory.
    // The directMemory variable is initialized during system initialization
    // in the saveAndRemoveProperties method.
    //
    public static long maxDirectMemory() {
        return directMemory;
    }

    // User-controllable flag that determines if direct buffers should be page
    // aligned. The "-XX:+PageAlignDirectMemory" option can be used to force
    // buffers, allocated by ByteBuffer.allocateDirect, to be page aligned.
    private static boolean pageAlignDirectMemory;

    // Returns {@code true} if the direct buffers should be page aligned. This
    // variable is initialized by saveAndRemoveProperties.
    public static boolean isDirectMemoryPageAligned() {
        return pageAlignDirectMemory;
    }

    /**
     * Returns true if the given class loader is the bootstrap class loader
     * or the platform class loader.
     */
    public static boolean isSystemDomainLoader(ClassLoader loader) {
        return loader == null || loader == ClassLoader.getPlatformClassLoader();
    }

    /**
     * Returns the system property of the specified key saved at
     * system initialization time.  This method should only be used
     * for the system properties that are not changed during runtime.
     *
     * Note that the saved system properties do not include
     * the ones set by java.lang.VersionProps.init().
     */
    public static String getSavedProperty(String key) {
        if (savedProps == null)
            throw new IllegalStateException("Not yet initialized");

        return savedProps.get(key);
    }

    /**
     * Gets an unmodifiable view of the system properties saved at system
     * initialization time. This method should only be used
     * for the system properties that are not changed during runtime.
     *
     * Note that the saved system properties do not include
     * the ones set by java.lang.VersionProps.init().
     */
    public static Map<String, String> getSavedProperties() {
        if (savedProps == null)
            throw new IllegalStateException("Not yet initialized");

        return savedProps;
    }

    private static Map<String, String> savedProps;

    // Save a private copy of the system properties and remove
    // the system properties that are not intended for public access.
    //
    // This method can only be invoked during system initialization.
    public static void saveAndRemoveProperties(Properties props) {
        if (initLevel() != 0)
            throw new IllegalStateException("Wrong init level");

        @SuppressWarnings({"rawtypes", "unchecked"})
        Map<String, String> sp =
            Map.ofEntries(props.entrySet().toArray(new Map.Entry[0]));
        // only main thread is running at this time, so savedProps and
        // its content will be correctly published to threads started later
        savedProps = sp;

        // Set the maximum amount of direct memory.  This value is controlled
        // by the vm option -XX:MaxDirectMemorySize=<size>.
        // The maximum amount of allocatable direct buffer memory (in bytes)
        // from the system property sun.nio.MaxDirectMemorySize set by the VM.
        // The system property will be removed.
        String s = (String)props.remove("sun.nio.MaxDirectMemorySize");
        if (s != null) {
            if (s.equals("-1")) {
                // -XX:MaxDirectMemorySize not given, take default
                directMemory = Runtime.getRuntime().maxMemory();
            } else {
                long l = Long.parseLong(s);
                if (l > -1)
                    directMemory = l;
            }
        }

        // Check if direct buffers should be page aligned
        s = (String)props.remove("sun.nio.PageAlignDirectMemory");
        if ("true".equals(s))
            pageAlignDirectMemory = true;

        // Remove other private system properties
        // used by java.lang.Integer.IntegerCache
        props.remove("java.lang.Integer.IntegerCache.high");

        // used by sun.launcher.LauncherHelper
        props.remove("sun.java.launcher.diag");

        // used by jdk.internal.loader.ClassLoaders
        props.remove("jdk.boot.class.path.append");
    }

    // Initialize any miscellaneous operating system settings that need to be
    // set for the class libraries.
    //
    public static void initializeOSEnvironment() {
        if (initLevel() == 0) {
            OSEnvironment.initialize();
        }
    }

    /* Current count of objects pending for finalization */
    private static volatile int finalRefCount;

    /* Peak count of objects pending for finalization */
    private static volatile int peakFinalRefCount;

    /*
     * Gets the number of objects pending for finalization.
     *
     * @return the number of objects pending for finalization.
     */
    public static int getFinalRefCount() {
        return finalRefCount;
    }

    /*
     * Gets the peak number of objects pending for finalization.
     *
     * @return the peak number of objects pending for finalization.
     */
    public static int getPeakFinalRefCount() {
        return peakFinalRefCount;
    }

    /*
     * Add {@code n} to the objects pending for finalization count.
     *
     * @param n an integer value to be added to the objects pending
     * for finalization count
     */
    public static void addFinalRefCount(int n) {
        // The caller must hold lock to synchronize the update.

        finalRefCount += n;
        if (finalRefCount > peakFinalRefCount) {
            peakFinalRefCount = finalRefCount;
        }
    }

    /**
     * Returns Thread.State for the given threadStatus
     */
    public static Thread.State toThreadState(int threadStatus) {
        if ((threadStatus & JVMTI_THREAD_STATE_RUNNABLE) != 0) {
            return RUNNABLE;
        } else if ((threadStatus & JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER) != 0) {
            return BLOCKED;
        } else if ((threadStatus & JVMTI_THREAD_STATE_WAITING_INDEFINITELY) != 0) {
            return WAITING;
        } else if ((threadStatus & JVMTI_THREAD_STATE_WAITING_WITH_TIMEOUT) != 0) {
            return TIMED_WAITING;
        } else if ((threadStatus & JVMTI_THREAD_STATE_TERMINATED) != 0) {
            return TERMINATED;
        } else if ((threadStatus & JVMTI_THREAD_STATE_ALIVE) == 0) {
            return NEW;
        } else {
            return RUNNABLE;
        }
    }

    /* The threadStatus field is set by the VM at state transition
     * in the hotspot implementation. Its value is set according to
     * the JVM TI specification GetThreadState function.
     */
    private static final int JVMTI_THREAD_STATE_ALIVE = 0x0001;
    private static final int JVMTI_THREAD_STATE_TERMINATED = 0x0002;
    private static final int JVMTI_THREAD_STATE_RUNNABLE = 0x0004;
    private static final int JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER = 0x0400;
    private static final int JVMTI_THREAD_STATE_WAITING_INDEFINITELY = 0x0010;
    private static final int JVMTI_THREAD_STATE_WAITING_WITH_TIMEOUT = 0x0020;

    /*
     * Returns the first user-defined class loader up the execution stack,
     * or the platform class loader if only code from the platform or
     * bootstrap class loader is on the stack.
     */
    public static ClassLoader latestUserDefinedLoader() {
        ClassLoader loader = latestUserDefinedLoader0();
        return loader != null ? loader : ClassLoader.getPlatformClassLoader();
    }

    /** Whether to scramble hash iteration order, by default. */
    public final static boolean HASH_SCRAMBLE_DEFAULT = false;

    /**
     * A number to be used to scramble hash iteration ordering.
     * Must be non-negative as required by HashMap and ConcurrentHashMap.
     * If the JAVAHASHSEED environment variable is defined and numeric,
     * use that deterministic value, analogously to PYTHONHASHSEED.
     * Else, if HASH_SCRAMBLE_DEFAULT is false, use a seed of 0, which
     * is compatible with the behavior of unmodified upstream JDK.
     * Else a random seed value is obtained from /dev/urandom.
     *
     * <p>Unlike with python, extra scrambling has only limited security
     * benefits - it's more about making google tests non-brittle.
     *
     * @see http://go/java-hash-randomization
     * @see https://docs.python.org/3/using/cmdline.html#envvar-PYTHONHASHSEED
     */
    public final static int HASH_SCRAMBLER
        = hashScrambler(HASH_SCRAMBLE_DEFAULT);
    private native static int hashScrambler(boolean hashScrambleDefault);

    /*
     * Returns the first user-defined class loader up the execution stack,
     * or null if only code from the platform or bootstrap class loader is
     * on the stack.  VM does not keep a reference of platform loader and so
     * it returns null.
     *
     * This method should be replaced with StackWalker::walk and then we can
     * remove the logic in the VM.
     */
    private static native ClassLoader latestUserDefinedLoader0();

    /**
     * Returns {@code true} if we are in a set UID program.
     */
    public static boolean isSetUID() {
        long uid = getuid();
        long euid = geteuid();
        long gid = getgid();
        long egid = getegid();
        return uid != euid  || gid != egid;
    }

    /**
     * Returns the real user ID of the calling process,
     * or -1 if the value is not available.
     */
    public static native long getuid();

    /**
     * Returns the effective user ID of the calling process,
     * or -1 if the value is not available.
     */
    public static native long geteuid();

    /**
     * Returns the real group ID of the calling process,
     * or -1 if the value is not available.
     */
    public static native long getgid();

    /**
     * Returns the effective group ID of the calling process,
     * or -1 if the value is not available.
     */
    public static native long getegid();

    /**
     * Get a nanosecond time stamp adjustment in the form of a single long.
     *
     * This value can be used to create an instant using
     * {@link java.time.Instant#ofEpochSecond(long, long)
     *  java.time.Instant.ofEpochSecond(offsetInSeconds,
     *  getNanoTimeAdjustment(offsetInSeconds))}.
     * <p>
     * The value returned has the best resolution available to the JVM on
     * the current system.
     * This is usually down to microseconds - or tenth of microseconds -
     * depending on the OS/Hardware and the JVM implementation.
     *
     * @param offsetInSeconds The offset in seconds from which the nanosecond
     *        time stamp should be computed.
     *
     * @apiNote The offset should be recent enough - so that
     *         {@code offsetInSeconds} is within {@code +/- 2^32} seconds of the
     *         current UTC time. If the offset is too far off, {@code -1} will be
     *         returned. As such, {@code -1} must not be considered as a valid
     *         nano time adjustment, but as an exception value indicating
     *         that an offset closer to the current time should be used.
     *
     * @return A nanosecond time stamp adjustment in the form of a single long.
     *     If the offset is too far off the current time, this method returns -1.
     *     In that case, the caller should call this method again, passing a
     *     more accurate offset.
     */
    public static native long getNanoTimeAdjustment(long offsetInSeconds);

    /**
     * Returns the VM arguments for this runtime environment.
     *
     * @implNote
     * The HotSpot JVM processes the input arguments from multiple sources
     * in the following order:
     * 1. JAVA_TOOL_OPTIONS environment variable
     * 2. Options from JNI Invocation API
     * 3. _JAVA_OPTIONS environment variable
     *
     * If VM options file is specified via -XX:VMOptionsFile, the vm options
     * file is read and expanded in place of -XX:VMOptionFile option.
     */
    public static native String[] getRuntimeArguments();

    static {
        initialize();
    }
    private static native void initialize();

    /**
     * Initialize archived static fields in the given Class using archived
     * values from CDS dump time. Also initialize the classes of objects in
     * the archived graph referenced by those fields.
     *
     * Those static fields remain as uninitialized if there is no mapped CDS
     * java heap data or there is any error during initialization of the
     * object class in the archived graph.
     */
    public static native void initializeFromArchive(Class<?> c);

    public static native void reportOutOfMemoryError(String message);
}