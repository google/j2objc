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

package javax.security.auth.callback;

import java.io.IOException;

/**
 * Needs to be implemented by classes that want to handle authentication
 * {@link Callback}s. A single method {@link #handle(Callback[])} must be
 * provided that checks the type of the incoming {@code Callback}s and reacts
 * accordingly. {@code CallbackHandler}s can be installed per application. It is
 * also possible to configure a system-default {@code CallbackHandler} by
 * setting the {@code auth.login.defaultCallbackHandler} property in the
 * standard {@code security.properties} file.
 */
public interface CallbackHandler {

    /**
     * Handles the actual {@link Callback}. A {@code CallbackHandler} needs to
     * implement this method. In the method, it is free to select which {@code
     * Callback}s it actually wants to handle and in which way. For example, a
     * console-based {@code CallbackHandler} might choose to sequentially ask
     * the user for login and password, if it implements these {@code Callback}
     * s, whereas a GUI-based one might open a single dialog window for both
     * values. If a {@code CallbackHandler} is not able to handle a specific
     * {@code Callback}, it needs to throw an
     * {@link UnsupportedCallbackException}.
     *
     * @param callbacks
     *            the array of {@code Callback}s that need handling
     * @throws IOException
     *             if an I/O related error occurs
     * @throws UnsupportedCallbackException
     *             if the {@code CallbackHandler} is not able to handle a
     *             specific {@code Callback}
     */
    void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException;

}
