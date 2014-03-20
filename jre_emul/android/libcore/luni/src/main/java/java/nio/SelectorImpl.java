/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package java.nio;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.IllegalSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import static java.nio.channels.SelectionKey.*;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelectionKey;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UnsafeArrayList;
import libcore.io.ErrnoException;
import libcore.io.IoBridge;
import libcore.io.IoUtils;
import libcore.io.Libcore;
import libcore.io.StructPollfd;
import libcore.util.EmptyArray;
import static libcore.io.OsConstants.*;

/*
 * Default implementation of java.nio.channels.Selector
 */
final class SelectorImpl extends AbstractSelector {

    /**
     * Used to synchronize when a key's interest ops change.
     */
    final Object keysLock = new Object();

    private final Set<SelectionKeyImpl> mutableKeys = new HashSet<SelectionKeyImpl>();

    /**
     * The unmodifiable set of keys as exposed to the user. This object is used
     * for synchronization.
     */
    private final Set<SelectionKey> unmodifiableKeys = Collections
            .<SelectionKey>unmodifiableSet(mutableKeys);

    private final Set<SelectionKey> mutableSelectedKeys = new HashSet<SelectionKey>();

    /**
     * The unmodifiable set of selectable keys as seen by the user. This object
     * is used for synchronization.
     */
    private final Set<SelectionKey> selectedKeys
            = new UnaddableSet<SelectionKey>(mutableSelectedKeys);

    /**
     * The wakeup pipe. To trigger a wakeup, write a byte to wakeupOut. Each
     * time select returns, wakeupIn is drained.
     */
    private final FileDescriptor wakeupIn;
    private final FileDescriptor wakeupOut;

    private final UnsafeArrayList<StructPollfd> pollFds = new UnsafeArrayList<StructPollfd>(StructPollfd.class, 8);

    public SelectorImpl(SelectorProvider selectorProvider) throws IOException {
        super(selectorProvider);

        /*
         * Create a pipe to trigger wakeup. We can't use a NIO pipe because it
         * would be closed if the selecting thread is interrupted. Also
         * configure the pipe so we can fully drain it without blocking.
         */
        try {
            FileDescriptor[] pipeFds = Libcore.os.pipe();
            wakeupIn = pipeFds[0];
            wakeupOut = pipeFds[1];
            IoUtils.setBlocking(wakeupIn, false);
            pollFds.add(new StructPollfd());
            setPollFd(0, wakeupIn, POLLIN, null);
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsIOException();
        }
    }

    @Override protected void implCloseSelector() throws IOException {
        wakeup();
        synchronized (this) {
            synchronized (unmodifiableKeys) {
                synchronized (selectedKeys) {
                    IoUtils.close(wakeupIn);
                    IoUtils.close(wakeupOut);
                    doCancel();
                    for (SelectionKey sk : mutableKeys) {
                        deregister((AbstractSelectionKey) sk);
                    }
                }
            }
        }
    }

    @Override protected SelectionKey register(AbstractSelectableChannel channel,
            int operations, Object attachment) {
        if (!provider().equals(channel.provider())) {
            throw new IllegalSelectorException();
        }
        synchronized (this) {
            synchronized (unmodifiableKeys) {
                SelectionKeyImpl selectionKey = new SelectionKeyImpl(channel, operations,
                        attachment, this);
                mutableKeys.add(selectionKey);
                ensurePollFdsCapacity();
                return selectionKey;
            }
        }
    }

    @Override public synchronized Set<SelectionKey> keys() {
        checkClosed();
        return unmodifiableKeys;
    }

    private void checkClosed() {
        if (!isOpen()) {
            throw new ClosedSelectorException();
        }
    }

    @Override public int select() throws IOException {
        // Blocks until some fd is ready.
        return selectInternal(-1);
    }

    @Override public int select(long timeout) throws IOException {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout < 0: " + timeout);
        }
        // Our timeout is interpreted differently to Unix's --- 0 means block. See selectNow.
        return selectInternal((timeout == 0) ? -1 : timeout);
    }

    @Override public int selectNow() throws IOException {
        return selectInternal(0);
    }

    private int selectInternal(long timeout) throws IOException {
        checkClosed();
        synchronized (this) {
            synchronized (unmodifiableKeys) {
                synchronized (selectedKeys) {
                    doCancel();
                    boolean isBlocking = (timeout != 0);
                    synchronized (keysLock) {
                        preparePollFds();
                    }
                    int rc = -1;
                    try {
                        if (isBlocking) {
                            begin();
                        }
                        try {
                            rc = Libcore.os.poll(pollFds.array(), (int) timeout);
                        } catch (ErrnoException errnoException) {
                            if (errnoException.errno != EINTR) {
                                throw errnoException.rethrowAsIOException();
                            }
                        }
                    } finally {
                        if (isBlocking) {
                            end();
                        }
                    }

                    int readyCount = (rc > 0) ? processPollFds() : 0;
                    readyCount -= doCancel();
                    return readyCount;
                }
            }
        }
    }

    private void setPollFd(int i, FileDescriptor fd, int events, Object object) {
        StructPollfd pollFd = pollFds.get(i);
        pollFd.fd = fd;
        pollFd.events = (short) events;
        pollFd.userData = object;
    }

    private void preparePollFds() {
        int i = 1; // Our wakeup pipe comes before all the user's fds.
        for (SelectionKeyImpl key : mutableKeys) {
            int interestOps = key.interestOpsNoCheck();
            short eventMask = 0;
            if (((OP_ACCEPT | OP_READ) & interestOps) != 0) {
                eventMask |= POLLIN;
            }
            if (((OP_CONNECT | OP_WRITE) & interestOps) != 0) {
                eventMask |= POLLOUT;
            }
            if (eventMask != 0) {
                setPollFd(i++, ((FileDescriptorChannel) key.channel()).getFD(), eventMask, key);
            }
        }
    }

    private void ensurePollFdsCapacity() {
        // We need one slot for each element of mutableKeys, plus one for the wakeup pipe.
        while (pollFds.size() < mutableKeys.size() + 1) {
            pollFds.add(new StructPollfd());
        }
    }

    /**
     * Updates the key ready ops and selected key set.
     */
    private int processPollFds() throws IOException {
        if (pollFds.get(0).revents == POLLIN) {
            // Read bytes from the wakeup pipe until the pipe is empty.
            byte[] buffer = new byte[8];
            while (IoBridge.read(wakeupIn, buffer, 0, 1) > 0) {
            }
        }

        int readyKeyCount = 0;
        for (int i = 1; i < pollFds.size(); ++i) {
            StructPollfd pollFd = pollFds.get(i);
            if (pollFd.revents == 0) {
                continue;
            }
            if (pollFd.fd == null) {
                break;
            }

            SelectionKeyImpl key = (SelectionKeyImpl) pollFd.userData;

            pollFd.fd = null;
            pollFd.userData = null;

            int ops = key.interestOpsNoCheck();
            int selectedOps = 0;
            if ((pollFd.revents & POLLHUP) != 0) {
                // If there was an error condition, we definitely want to wake listeners,
                // regardless of what they're waiting for. Failure is always interesting.
                selectedOps |= ops;
            }
            if ((pollFd.revents & POLLIN) != 0) {
                selectedOps |= ops & (OP_ACCEPT | OP_READ);
            }
            if ((pollFd.revents & POLLOUT) != 0) {
                if (key.isConnected()) {
                    selectedOps |= ops & OP_WRITE;
                } else {
                    selectedOps |= ops & OP_CONNECT;
                }
            }

            if (selectedOps != 0) {
                boolean wasSelected = mutableSelectedKeys.contains(key);
                if (wasSelected && key.readyOps() != selectedOps) {
                    key.setReadyOps(key.readyOps() | selectedOps);
                    ++readyKeyCount;
                } else if (!wasSelected) {
                    key.setReadyOps(selectedOps);
                    mutableSelectedKeys.add(key);
                    ++readyKeyCount;
                }
            }
        }

        return readyKeyCount;
    }

    @Override public synchronized Set<SelectionKey> selectedKeys() {
        checkClosed();
        return selectedKeys;
    }

    /**
     * Removes cancelled keys from the key set and selected key set, and
     * unregisters the corresponding channels. Returns the number of keys
     * removed from the selected key set.
     */
    private int doCancel() {
        int deselected = 0;

        Set<SelectionKey> cancelledKeys = cancelledKeys();
        synchronized (cancelledKeys) {
            if (cancelledKeys.size() > 0) {
                for (SelectionKey currentKey : cancelledKeys) {
                    mutableKeys.remove(currentKey);
                    deregister((AbstractSelectionKey) currentKey);
                    if (mutableSelectedKeys.remove(currentKey)) {
                        deselected++;
                    }
                }
                cancelledKeys.clear();
            }
        }

        return deselected;
    }

    @Override public Selector wakeup() {
        try {
            Libcore.os.write(wakeupOut, new byte[] { 1 }, 0, 1);
        } catch (ErrnoException ignored) {
        }
        return this;
    }

    private static class UnaddableSet<E> implements Set<E> {

        private final Set<E> set;

        UnaddableSet(Set<E> set) {
            this.set = set;
        }

        @Override
        public boolean equals(Object object) {
            return set.equals(object);
        }

        @Override
        public int hashCode() {
            return set.hashCode();
        }

        public boolean add(E object) {
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection<? extends E> c) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            set.clear();
        }

        public boolean contains(Object object) {
            return set.contains(object);
        }

        public boolean containsAll(Collection<?> c) {
            return set.containsAll(c);
        }

        public boolean isEmpty() {
            return set.isEmpty();
        }

        public Iterator<E> iterator() {
            return set.iterator();
        }

        public boolean remove(Object object) {
            return set.remove(object);
        }

        public boolean removeAll(Collection<?> c) {
            return set.removeAll(c);
        }

        public boolean retainAll(Collection<?> c) {
            return set.retainAll(c);
        }

        public int size() {
            return set.size();
        }

        public Object[] toArray() {
            return set.toArray();
        }

        public <T> T[] toArray(T[] a) {
            return set.toArray(a);
        }
    }
}
