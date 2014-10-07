/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.nio.channels.spi;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.IllegalSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code AbstractSelectableChannel} is the base implementation class for
 * selectable channels. It declares methods for registering, unregistering and
 * closing selectable channels. It is thread-safe.
 */
public abstract class AbstractSelectableChannel extends SelectableChannel {

    private final SelectorProvider provider;

    /*
     * The collection of key.
     */
    private List<SelectionKey> keyList = new ArrayList<SelectionKey>();

    private final Object blockingLock = new Object();

    boolean isBlocking = true;

    /**
     * Constructs a new {@code AbstractSelectableChannel}.
     *
     * @param selectorProvider
     *            the selector provider that creates this channel.
     */
    protected AbstractSelectableChannel(SelectorProvider selectorProvider) {
        provider = selectorProvider;
    }

    /**
     * Returns the selector provider that has created this channel.
     *
     * @see java.nio.channels.SelectableChannel#provider()
     * @return this channel's selector provider.
     */
    @Override
    public final SelectorProvider provider() {
        return provider;
    }

    /**
     * Indicates whether this channel is registered with one or more selectors.
     *
     * @return {@code true} if this channel is registered with a selector,
     *         {@code false} otherwise.
     */
    @Override
    synchronized public final boolean isRegistered() {
        return !keyList.isEmpty();
    }

    /**
     * Gets this channel's selection key for the specified selector.
     *
     * @param selector
     *            the selector with which this channel has been registered.
     * @return the selection key for the channel or {@code null} if this channel
     *         has not been registered with {@code selector}.
     */
    @Override
    synchronized public final SelectionKey keyFor(Selector selector) {
        for (SelectionKey key : keyList) {
            if (key != null && key.selector() == selector) {
                return key;
            }
        }
        return null;
    }

    /**
     * Registers this channel with the specified selector for the specified
     * interest set. If the channel is already registered with the selector, the
     * {@link SelectionKey interest set} is updated to {@code interestSet} and
     * the corresponding selection key is returned. If the channel is not yet
     * registered, this method calls the {@code register} method of
     * {@code selector} and adds the selection key to this channel's key set.
     *
     * @param selector
     *            the selector with which to register this channel.
     * @param interestSet
     *            this channel's {@link SelectionKey interest set}.
     * @param attachment
     *            the object to attach, can be {@code null}.
     * @return the selection key for this registration.
     * @throws CancelledKeyException
     *             if this channel is registered but its key has been canceled.
     * @throws ClosedChannelException
     *             if this channel is closed.
     * @throws IllegalArgumentException
     *             if {@code interestSet} is not supported by this channel.
     * @throws IllegalBlockingModeException
     *             if this channel is in blocking mode.
     * @throws IllegalSelectorException
     *             if this channel does not have the same provider as the given
     *             selector.
     */
    @Override
    public final SelectionKey register(Selector selector, int interestSet,
            Object attachment) throws ClosedChannelException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        if (!((interestSet & ~validOps()) == 0)) {
            throw new IllegalArgumentException("no valid ops in interest set: " + interestSet);
        }

        synchronized (blockingLock) {
            if (isBlocking) {
                throw new IllegalBlockingModeException();
            }
            if (!selector.isOpen()) {
                if (interestSet == 0) {
                    // throw ISE exactly to keep consistency
                    throw new IllegalSelectorException();
                }
                // throw NPE exactly to keep consistency
                throw new NullPointerException("selector not open");
            }
            SelectionKey key = keyFor(selector);
            if (key == null) {
                key = ((AbstractSelector) selector).register(this, interestSet, attachment);
                keyList.add(key);
            } else {
                if (!key.isValid()) {
                    throw new CancelledKeyException();
                }
                key.interestOps(interestSet);
                key.attach(attachment);
            }
            return key;
        }
    }

    /**
     * Implements the channel closing behavior. Calls
     * {@code implCloseSelectableChannel()} first, then loops through the list
     * of selection keys and cancels them, which unregisters this channel from
     * all selectors it is registered with.
     *
     * @throws IOException
     *             if a problem occurs while closing the channel.
     */
    @Override
    synchronized protected final void implCloseChannel() throws IOException {
        implCloseSelectableChannel();
        for (SelectionKey key : keyList) {
            if (key != null) {
                key.cancel();
            }
        }
    }

    /**
     * Implements the closing function of the SelectableChannel. This method is
     * called from {@code implCloseChannel()}.
     *
     * @throws IOException
     *             if an I/O exception occurs.
     */
    protected abstract void implCloseSelectableChannel() throws IOException;

    /**
     * Indicates whether this channel is in blocking mode.
     *
     * @return {@code true} if this channel is blocking, {@code false}
     *         otherwise.
     */
    @Override
    public final boolean isBlocking() {
        synchronized (blockingLock) {
            return isBlocking;
        }
    }

    /**
     * Gets the object used for the synchronization of {@code register} and
     * {@code configureBlocking}.
     *
     * @return the synchronization object.
     */
    @Override
    public final Object blockingLock() {
        return blockingLock;
    }

    /**
     * Sets the blocking mode of this channel. A call to this method blocks if
     * other calls to this method or to {@code register} are executing. The
     * actual setting of the mode is done by calling
     * {@code implConfigureBlocking(boolean)}.
     *
     * @see java.nio.channels.SelectableChannel#configureBlocking(boolean)
     * @param blockingMode
     *            {@code true} for setting this channel's mode to blocking,
     *            {@code false} to set it to non-blocking.
     * @return this channel.
     * @throws ClosedChannelException
     *             if this channel is closed.
     * @throws IllegalBlockingModeException
     *             if {@code block} is {@code true} and this channel has been
     *             registered with at least one selector.
     * @throws IOException
     *             if an I/O error occurs.
     */
    @Override
    public final SelectableChannel configureBlocking(boolean blockingMode) throws IOException {
        if (!isOpen()) {
            throw new ClosedChannelException();
        }
        synchronized (blockingLock) {
            if (isBlocking == blockingMode) {
                return this;
            }
            if (blockingMode && containsValidKeys()) {
                throw new IllegalBlockingModeException();
            }
            implConfigureBlocking(blockingMode);
            isBlocking = blockingMode;
        }
        return this;
    }

    /**
     * Implements the configuration of blocking/non-blocking mode.
     *
     * @param blocking true for blocking, false for non-blocking.
     * @throws IOException
     *             if an I/O error occurs.
     */
    protected abstract void implConfigureBlocking(boolean blocking) throws IOException;

    /*
     * package private for deregister method in AbstractSelector.
     */
    synchronized void deregister(SelectionKey k) {
        if (keyList != null) {
            keyList.remove(k);
        }
    }

    /**
     * Returns true if the keyList contains at least 1 valid key and false
     * otherwise.
     */
    private synchronized boolean containsValidKeys() {
        for (SelectionKey key : keyList) {
            if (key != null && key.isValid()) {
                return true;
            }
        }
        return false;
    }
}
