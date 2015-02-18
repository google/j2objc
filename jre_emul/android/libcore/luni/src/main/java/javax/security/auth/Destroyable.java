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

package javax.security.auth;

/**
 * Allows for special treatment of sensitive information, when it comes to
 * destroying or clearing of the data.
 */
public interface Destroyable {

    /**
     * Erases the sensitive information. Once an object is destroyed any calls
     * to its methods will throw an {@code IllegalStateException}. If it does
     * not succeed a DestroyFailedException is thrown.
     *
     * @throws DestroyFailedException
     *             if the information cannot be erased.
     */
    void destroy() throws DestroyFailedException;

    /**
     * Returns {@code true} once an object has been safely destroyed.
     *
     * @return whether the object has been safely destroyed.
     */
    boolean isDestroyed();

}
