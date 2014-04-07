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
 * The exception that is thrown when the identity of a peer has not been
 * verified.
 */
public class SSLPeerUnverifiedException extends SSLException {

    private static final long serialVersionUID = -8919512675000600547L;

    /**
     * Creates a new {@code SSLPeerUnverifiedException} with the specified
     * message.
     *
     * @param reason
     *            the detail message for the exception.
     */
    public SSLPeerUnverifiedException(String reason) {
        super(reason);
    }
}
