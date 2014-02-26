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

package java.nio.channels;

import java.io.IOException;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.channels.spi.SelectorProvider;

/**
 * A channel that can be used with a {@link Selector}. The channel must be
 * registered with a selector by calling one of the {@code register} methods,
 * which return a {@link SelectionKey} object. In order to deregister a channel
 * from a selector, its selection key must be canceled. This can be done
 * explicitly by calling the {@link SelectionKey#cancel()} method but it is also
 * done implicitly when the channel or the selector is closed.
 * <p>
 * A channel may be registered with several selectors at the same time but only
 * once for any given selector.
 */
public abstract class SelectableChannel extends AbstractInterruptibleChannel
        implements Channel {

    /**
     * Constructs a new {@code SelectableChannel}.
     */
    protected SelectableChannel() {
    }

    /**
     * Gets the blocking lock which synchronizes the {@code configureBlocking}
     * and {@code register} methods.
     *
     * @return the blocking object as lock.
     */
    public abstract Object blockingLock();

    /**
     * Sets the blocking mode of this channel. A call to this method blocks if
     * other calls to this method or to a {@code register} method are executing.
     * The new blocking mode is valid for calls to other methods that are
     * invoked after the call to this method. If other methods are already
     * executing when this method is called, they still have the old mode and
     * the call to this method might block depending on the implementation.
     *
     * @param block
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
    public abstract SelectableChannel configureBlocking(boolean block)
            throws IOException;

    /**
     * Indicates whether this channel is in blocking mode.
     *
     * @return {@code true} if this channel is blocking, undefined if this
     *         channel is closed.
     */
    public abstract boolean isBlocking();

    /**
     * Indicates whether this channel is registered with at least one selector.
     *
     * @return {@code true} if this channel is registered, {@code false}
     *         otherwise.
     */
    public abstract boolean isRegistered();

    /**
     * Gets this channel's selection key for the specified selector.
     *
     * @param sel
     *            the selector with which this channel has been registered.
     * @return the selection key for the channel or {@code null} if this channel
     *         has not been registered with {@code sel}.
     */
    public abstract SelectionKey keyFor(Selector sel);

    /**
     * Gets the provider of this channel.
     *
     * @return the provider of this channel.
     */
    public abstract SelectorProvider provider();

    /**
     * Registers this channel with the specified selector for the specified
     * interest set. If the channel is already registered with the selector, the
     * corresponding selection key is returned but the
     * {@link SelectionKey interest set} is updated to {@code operations}. The
     * returned key is canceled if the channel is closed while registering is in
     * progress.
     * <p>
     * Calling this method is valid at any time. If another thread executes this
     * method or the {@code configureBlocking(boolean} method then this call is
     * blocked until the other call finishes. After that, it will synchronize on
     * the key set of the selector and thus may again block if other threads
     * also hold locks on the key set of the same selector.
     * <p>
     * Calling this method is equivalent to calling
     * {@code register(selector, operations, null)}.
     *
     * @param selector
     *            the selector with which to register this channel.
     * @param operations
     *            this channel's {@link SelectionKey interest set}.
     * @return the selection key for this registration.
     * @throws ClosedChannelException
     *             if the channel is closed.
     * @throws IllegalBlockingModeException
     *             if the channel is in blocking mode.
     * @throws IllegalSelectorException
     *             if this channel does not have the same provider as the given
     *             selector.
     * @throws CancelledKeyException
     *             if this channel is registered but its key has been canceled.
     * @throws IllegalArgumentException
     *             if the operation given is not supported by this channel.
     */
    public final SelectionKey register(Selector selector, int operations)
            throws ClosedChannelException {
        return register(selector, operations, null);
    }

    /**
     * Registers this channel with the specified selector for the specified
     * interest set and an object to attach. If the channel is already
     * registered with the selector, the corresponding selection key is returned
     * but its {@link SelectionKey interest set} is updated to {@code ops} and
     * the attached object is updated to {@code att}. The returned key is
     * canceled if the channel is closed while registering is in progress.
     * <p>
     * Calling this method is valid at any time. If another thread executes this
     * method or the {@code configureBlocking(boolean)} method then this call is
     * blocked until the other call finishes. After that, it will synchronize on
     * the key set of the selector and thus may again block if other threads
     * also hold locks on the key set of the same selector.
     *
     * @param sel
     *            the selector with which to register this channel.
     * @param ops
     *            this channel's {@link SelectionKey interest set}.
     * @param att
     *            the object to attach, can be {@code null}.
     * @return the selection key for this registration.
     * @throws ClosedChannelException
     *             if this channel is closed.
     * @throws IllegalArgumentException
     *             if {@code ops} is not supported by this channel.
     * @throws IllegalBlockingModeException
     *             if this channel is in blocking mode.
     * @throws IllegalSelectorException
     *             if this channel does not have the same provider as the given
     *             selector.
     * @throws CancelledKeyException
     *             if this channel is registered but its key has been canceled.
     */
    public abstract SelectionKey register(Selector sel, int ops, Object att)
            throws ClosedChannelException;

    /**
     * Gets the set of valid {@link SelectionKey operations} of this channel.
     * Instances of a concrete channel class always return the same value.
     *
     * @return the set of operations that this channel supports.
     */
    public abstract int validOps();
}
