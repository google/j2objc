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

import java.util.EventListener;

/**
 * The listener to be implemented to receive event notifications on completion
 * of SSL handshake on an SSL connection.
 */
public interface HandshakeCompletedListener extends EventListener {
    /**
     * The callback method that is invoked when a SSL handshake is completed.
     *
     * @param event
     *            the information on the completed SSL handshake event.
     */
    void handshakeCompleted(HandshakeCompletedEvent event);
}
