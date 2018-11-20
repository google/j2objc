/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2001-2013, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.impl;

import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * <p>A Reader/Writer lock originally written for ICU service
 * implementation. The internal implementation was replaced
 * with the JDK's stock read write lock (ReentrantReadWriteLock)
 * for ICU 52.</p>
 *
 * <p>This assumes that there will be little writing contention.
 * It also doesn't allow active readers to acquire and release
 * a write lock, or deal with priority inversion issues.</p>
 *
 * <p>Access to the lock should be enclosed in a try/finally block
 * in order to ensure that the lock is always released in case of
 * exceptions:<br><pre>
 * try {
 *     lock.acquireRead();
 *     // use service protected by the lock
 * }
 * finally {
 *     lock.releaseRead();
 * }
 * </pre></p>
 *
 * <p>The lock provides utility methods getStats and clearStats
 * to return statistics on the use of the lock.</p>
 * @hide Only a subset of ICU is exposed in Android
 */
public class ICURWLock {
    private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    private Stats stats = null;

    /**
     * Internal class used to gather statistics on the RWLock.
     */
    public final static class Stats {
        /**
         * Number of times read access granted (read count).
         */
        public int _rc;

        /**
         * Number of times concurrent read access granted (multiple read count).
         */
        public int _mrc;

        /**
         * Number of times blocked for read (waiting reader count).
         */
        public int _wrc; // wait for read

        /**
         * Number of times write access granted (writer count).
         */
        public int _wc;

        /**
         * Number of times blocked for write (waiting writer count).
         */
        public int _wwc;

        private Stats() {
        }

        private Stats(int rc, int mrc, int wrc, int wc, int wwc) {
            this._rc = rc;
            this._mrc = mrc;
            this._wrc = wrc;
            this._wc = wc;
            this._wwc = wwc;
        }

        private Stats(Stats rhs) {
            this(rhs._rc, rhs._mrc, rhs._wrc, rhs._wc, rhs._wwc);
        }

        /**
         * Return a string listing all the stats.
         */
        @Override
        public String toString() {
            return " rc: " + _rc +
                " mrc: " + _mrc +
                " wrc: " + _wrc +
                " wc: " + _wc +
                " wwc: " + _wwc;
        }
    }

    /**
     * Reset the stats.  Returns existing stats, if any.
     */
    public synchronized Stats resetStats() {
        Stats result = stats;
        stats = new Stats();
        return result;
    }

    /**
     * Clear the stats (stop collecting stats).  Returns existing stats, if any.
     */
    public synchronized Stats clearStats() {
        Stats result = stats;
        stats = null;
        return result;
    }

    /**
     * Return a snapshot of the current stats.  This does not reset the stats.
     */
    public synchronized Stats getStats() {
        return stats == null ? null : new Stats(stats);
    }

    /**
     * <p>Acquire a read lock, blocking until a read lock is
     * available.  Multiple readers can concurrently hold the read
     * lock.</p>
     *
     * <p>If there's a writer, or a waiting writer, increment the
     * waiting reader count and block on this.  Otherwise
     * increment the active reader count and return.  Caller must call
     * releaseRead when done (for example, in a finally block).</p>
     */
    public void acquireRead() {
        if (stats != null) {    // stats is null by default
            synchronized (this) {
                stats._rc++;
                if (rwl.getReadLockCount() > 0) {
                    stats._mrc++;
                }
                if (rwl.isWriteLocked()) {
                    stats._wrc++;
                }
            }
        }
        rwl.readLock().lock();
    }

    /**
     * <p>Release a read lock and return.  An error will be thrown
     * if a read lock is not currently held.</p>
     *
     * <p>If this is the last active reader, notify the oldest
     * waiting writer.  Call when finished with work
     * controlled by acquireRead.</p>
     */
    public void releaseRead() {
        rwl.readLock().unlock();
    }

    /**
     * <p>Acquire the write lock, blocking until the write lock is
     * available.  Only one writer can acquire the write lock, and
     * when held, no readers can acquire the read lock.</p>
     *
     * <p>If there are no readers and no waiting writers, mark as
     * having an active writer and return.  Otherwise, add a lock to the
     * end of the waiting writer list, and block on it.  Caller
     * must call releaseWrite when done (for example, in a finally
     * block).<p>
     */
    public void acquireWrite() {
        if (stats != null) {    // stats is null by default
            synchronized (this) {
                stats._wc++;
                if (rwl.getReadLockCount() > 0 || rwl.isWriteLocked()) {
                    stats._wwc++;
                }
            }
        }
        rwl.writeLock().lock();
    }

    /**
     * <p>Release the write lock and return.  An error will be thrown
     * if the write lock is not currently held.</p>
     *
     * <p>If there are waiting readers, make them all active and
     * notify all of them.  Otherwise, notify the oldest waiting
     * writer, if any.  Call when finished with work controlled by
     * acquireWrite.</p>
     */
    public void releaseWrite() {
        rwl.writeLock().unlock();
    }
}
