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

/**
 * A {@code SelectionKey} represents the relationship between a channel and a
 * selector for which the channel is registered.
 * <h3>Operation set</h3>
 * An operation set is represented by an integer value. The bits of an operation
 * set represent categories of operations for a key's channel: Accepting socket
 * connections ({@code OP_ACCEPT}), connecting with a socket ({@code OP_CONNECT}),
 * reading ({@code OP_READ}) and writing ({@code OP_WRITE}).
 * <h4>Interest set</h4>
 * The interest set is an operation set that defines the operations that a
 * {@link SelectableChannel channel} is interested in performing.
 * <h4>Ready set</h4>
 * The ready set is an operation set that shows the operations that a
 * {@code channel} is ready to execute.
 */
public abstract class SelectionKey {

    /**
     * Interest set mask bit for socket-accept operations.
     */
    public static final int OP_ACCEPT = 16;

    /**
     * Interest set mask bit for socket-connect operations.
     */
    public static final int OP_CONNECT = 8;

    /**
     * Interesting operation mask bit for read operations.
     */
    public static final int OP_READ = 1;

    /**
     * Interest set mask bit for write operations.
     */
    public static final int OP_WRITE = 4;

    private volatile Object attachment = null;

    /**
     * Constructs a new {@code SelectionKey}.
     */
    protected SelectionKey() {
    }

    /**
     * Attaches an object to this key. It is acceptable to attach {@code null},
     * this discards the old attachment.
     *
     * @param anObject
     *            the object to attach, or {@code null} to discard the current
     *            attachment.
     * @return the last attached object or {@code null} if no object has been
     *         attached.
     */
    public final Object attach(Object anObject) {
        Object oldAttachment = attachment;
        attachment = anObject;
        return oldAttachment;
    }

    /**
     * Gets the attached object.
     *
     * @return the attached object or {@code null} if no object has been
     *         attached.
     */
    public final Object attachment() {
        return attachment;
    }

    /**
     * Cancels this key.
     * <p>
     * A key that has been canceled is no longer valid. Calling this method on
     * an already canceled key does nothing.
     * <p>
     * Calling this method is safe at any time. The call might block until
     * another ongoing call to a method of this selector has finished. The
     * reason is that it is synchronizing on the key set of the selector. After
     * this call finishes, the key will have been added to the selectors
     * canceled-keys set and will not be included in any future selects of this
     * selector.
     */
    public abstract void cancel();

    /**
     * Gets the channel of this key.
     *
     * @return the channel of this key.
     */
    public abstract SelectableChannel channel();

    /**
     * Gets this key's {@link SelectionKey interest set}. The returned set has
     * only those bits set that are valid for this key's channel.
     *
     * @return the interest set of this key.
     * @throws CancelledKeyException
     *             if the key has already been canceled.
     */
    public abstract int interestOps();

    /**
     * Sets the {@link SelectionKey interest set} for this key.
     *
     * @param operations
     *            the new interest set.
     * @return this key.
     * @throws IllegalArgumentException
     *             if a bit in {@code operations} is not in the set of
     *             {@link SelectableChannel#validOps() valid operations} of this
     *             key's channel.
     * @throws CancelledKeyException
     *             if the key has already been canceled.
     */
    public abstract SelectionKey interestOps(int operations);

    /**
     * Indicates whether this key's channel is interested in the accept
     * operation and is ready to accept new connections. A call to this method
     * is equal to executing {@code (readyOps() & OP_ACCEPT) == OP_ACCEPT}.
     *
     * @return {@code true} if the channel is interested in the accept operation
     *         and is ready to accept new connections, {@code false} otherwise.
     * @throws CancelledKeyException
     *             if the key has already been canceled.
     */
    public final boolean isAcceptable() {
        return (readyOps() & OP_ACCEPT) == OP_ACCEPT;
    }

    /**
     * Indicates whether this key's channel is interested in the connect
     * operation and is ready to connect. A call to this method is equal to
     * executing {@code (readyOps() & OP_CONNECT) == OP_CONNECT}.
     *
     * @return {@code true} if the channel is interested in the connect
     *         operation and is ready to connect, {@code false} otherwise.
     * @throws CancelledKeyException
     *             if the key has already been canceled.
     */
    public final boolean isConnectable() {
        return (readyOps() & OP_CONNECT) == OP_CONNECT;
    }

    /**
     * Indicates whether this key's channel is interested in the read operation
     * and is ready to read. A call to this method is equal to executing
     * {@code (readyOps() & OP_READ) == OP_READ}.
     *
     * @return {@code true} if the channel is interested in the read operation
     *         and is ready to read, {@code false} otherwise.
     * @throws CancelledKeyException
     *             if the key has already been canceled.
     */
    public final boolean isReadable() {
        return (readyOps() & OP_READ) == OP_READ;
    }

    /**
     * Indicates whether this key is valid. A key is valid as long as it has not
     * been canceled.
     *
     * @return {@code true} if this key has not been canceled, {@code false}
     *         otherwise.
     */
    public abstract boolean isValid();

    /**
     * Indicates whether this key's channel is interested in the write operation
     * and is ready to write. A call to this method is equal to executing
     * {@code (readyOps() & OP_WRITE) == OP_WRITE}.
     *
     * @return {@code true} if the channel is interested in the write operation
     *         and is ready to write, {@code false} otherwise.
     * @throws CancelledKeyException
     *             if the key has already been canceled.
     */
    public final boolean isWritable() {
        return (readyOps() & OP_WRITE) == OP_WRITE;
    }

    /**
     * Gets the set of operations that are ready. The returned set has only
     * those bits set that are valid for this key's channel.
     *
     * @return the operations for which this key's channel is ready.
     * @throws CancelledKeyException
     *             if the key has already been canceled.
     */
    public abstract int readyOps();

    /**
     * Gets the selector for which this key's channel is registered.
     *
     * @return the related selector.
     */
    public abstract Selector selector();
}
