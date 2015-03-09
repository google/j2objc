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

import java.util.Enumeration;

/**
 * A collection of {@code SSLSession}s.
 */
public interface SSLSessionContext {
    /**
     * Returns an iterable of all session identifiers in this session context.
     *
     * @return an iterable of all session identifiers in this session context.
     */
    public Enumeration<byte[]> getIds();

    /**
     * Returns the session for the specified session identifier.
     *
     * @param sessionId
     *            the session identifier of the session to look up.
     * @return the session for the specified session identifier, or {@code null}
     *         if the specified session identifier does not refer to a session
     *         in this context.
     */
    public SSLSession getSession(byte[] sessionId);

    /**
     * Returns the size of the session cache for this session context.
     *
     * @return the size of the session cache for this session context, or
     *         {@code zero} if unlimited.
     */
    public int getSessionCacheSize();

    /**
     * Returns the timeout for sessions in this session context. Sessions
     * exceeding the timeout are invalidated.
     *
     * @return the timeout in seconds, or {@code zero} if unlimited.
     */
    public int getSessionTimeout();

    /**
     * Sets the size of the session cache for this session context.
     *
     * @param size
     *            the size of the session cache, or {@code zero} for unlimited
     *            cache size.
     * @throws IllegalArgumentException
     *             if {@code size} is negative.
     */
    public void setSessionCacheSize(int size) throws IllegalArgumentException;

    /**
     * Sets the timeout for sessions in this context. Sessions exceeding the
     * timeout are invalidated.
     *
     * @param seconds
     *            the timeout in seconds, or {@code zero} if unlimited.
     * @throws IllegalArgumentException
     *             if {@code seconds} is negative.
     */
    public void setSessionTimeout(int seconds) throws IllegalArgumentException;
}
