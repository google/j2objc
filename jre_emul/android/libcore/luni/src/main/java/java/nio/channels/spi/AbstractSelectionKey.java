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

package java.nio.channels.spi;

import java.nio.channels.SelectionKey;

/**
 * {@code AbstractSelectionKey} is the base implementation class for selection keys.
 * It implements validation and cancellation methods.
 */
public abstract class AbstractSelectionKey extends SelectionKey {

    /*
     * package private for deregister method in AbstractSelector.
     */
    boolean isValid = true;

    /**
     * Constructs a new {@code AbstractSelectionKey}.
     */
    protected AbstractSelectionKey() {
    }

    /**
     * Indicates whether this key is valid. A key is valid as long as it has not
     * been canceled.
     *
     * @return {@code true} if this key has not been canceled, {@code false}
     *         otherwise.
     */
    @Override
    public final boolean isValid() {
        return isValid;
    }

    /**
     * Cancels this key.
     * <p>
     * A key that has been canceled is no longer valid. Calling this method on
     * an already canceled key does nothing.
     */
    @Override
    public final void cancel() {
        if (isValid) {
            isValid = false;
            ((AbstractSelector) selector()).cancel(this);
        }
    }
}
