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
 * A {@code NoConnectionPendingException} is thrown if {@code SocketChannel}'s
 * {@link SocketChannel#finishConnect() finishConnect} method is called before
 * the {@code SocketChannel}'s {@link
 * SocketChannel#connect(java.net.SocketAddress)} connect} method completed
 * without error.
 */
public class NoConnectionPendingException extends IllegalStateException {

    private static final long serialVersionUID = -8296561183633134743L;

    /**
     * Constructs a {@code NoConnectionPendingException}.
     */
    public NoConnectionPendingException() {
    }
}
