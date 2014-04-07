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

package javax.net.ssl;

/**
 * The result object describing the state of the {@code SSLEngine} produced
 * by the {@code wrap()} and {@code unwrap()} operations.
 */
public class SSLEngineResult {

    /**
     * The {@code enum} describing the state of the current handshake.
     */
    public enum HandshakeStatus {
        /**
         * No handshake in progress.
         */
        NOT_HANDSHAKING,
        /**
         * The handshake is finished.
         */
        FINISHED,
        /**
         * The results of one (or more) delegated tasks are needed to continue
         * the handshake.
         */
        NEED_TASK,
        /**
         * The engine must send data to the remote side to continue the
         * handshake.
         */
        NEED_WRAP,
        /**
         * The engine needs to receive data from the remote side to continue the
         * handshake.
         */
        NEED_UNWRAP
    }

    /**
     * The {@code enum} describing the result of the {@code SSLEngine}
     * operation.
     */
    public static enum Status {
        /**
         * The size of the destination buffer is too small to hold the result of
         * the current operation.
         */
        BUFFER_OVERFLOW,
        /**
         * There were not enough bytes available in the source buffer to
         * complete the current operation.
         */
        BUFFER_UNDERFLOW,
        /**
         * The operation closed this side of the communication or was already
         * closed.
         */
        CLOSED,
        /**
         * The operation completed successfully.
         */
        OK
    }

    // Store Status object
    private final SSLEngineResult.Status status;

    // Store HandshakeStatus object
    private final SSLEngineResult.HandshakeStatus handshakeStatus;

    // Store bytesConsumed
    private final int bytesConsumed;

    // Store bytesProduced
    private final int bytesProduced;

    /**
     * Creates a new {@code SSLEngineResult} instance with the specified state
     * values.
     *
     * @param status
     *            the return value of the {@code SSLEngine} operation.
     * @param handshakeStatus
     *            the status of the current handshake
     * @param bytesConsumed
     *            the number of bytes retrieved from the source buffer(s).
     * @param bytesProduced
     *            the number of bytes transferred to the destination buffer(s).
     * @throws IllegalArgumentException
     *             if {@code status} or {@code handshakeStatus} is {@code null},
     *             or if {@code bytesConsumed} or {@code bytesProduces} are
     *             negative.
     */
    public SSLEngineResult(SSLEngineResult.Status status,
            SSLEngineResult.HandshakeStatus handshakeStatus, int bytesConsumed, int bytesProduced) {
        if (status == null) {
            throw new IllegalArgumentException("status is null");
        }
        if (handshakeStatus == null) {
            throw new IllegalArgumentException("handshakeStatus is null");
        }
        if (bytesConsumed < 0) {
            throw new IllegalArgumentException("bytesConsumed is negative");
        }
        if (bytesProduced < 0) {
            throw new IllegalArgumentException("bytesProduced is negative");
        }
        this.status = status;
        this.handshakeStatus = handshakeStatus;
        this.bytesConsumed = bytesConsumed;
        this.bytesProduced = bytesProduced;
    }

    /**
     * Returns the return value of the {@code SSLEngine} operation.
     *
     * @return the return value of the {@code SSLEngine} operation.
     */
    public final Status getStatus() {
        return status;
    }

    /**
     * Returns the status of the current handshake.
     *
     * @return the status of the current handshake.
     */
    public final HandshakeStatus getHandshakeStatus() {
        return handshakeStatus;
    }

    /**
     * Returns the number of bytes retrieved from the source buffer(s).
     *
     * @return the number of bytes retrieved from the source buffer(s).
     */
    public final int bytesConsumed() {
        return bytesConsumed;
    }

    /**
     * Returns the number of bytes transferred to the destination buffer(s).
     *
     * @return the number of bytes transferred to the destination buffer(s).
     */
    public final int bytesProduced() {
        return bytesProduced;
    }

    @Override
    public String toString() {
        return "SSLEngineReport: Status = " + status + "  HandshakeStatus = " + handshakeStatus
                + "\n                 bytesConsumed = " + bytesConsumed + " bytesProduced = "
                + bytesProduced;
    }
}
