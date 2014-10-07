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

package java.net;

/**
 * This immutable class is a data structure that encapsulates username and
 * password which is used by the {@code Authenticator} class.
 *
 * @see Authenticator
 */
public final class PasswordAuthentication {

    private String userName;

    private char[] password;

    /**
     * Creates an instance of a password authentication with a specified
     * username and password.
     *
     * @param userName
     *            the username to store.
     * @param password
     *            the associated password to store.
     */
    public PasswordAuthentication(String userName, char[] password) {
        this.userName = userName;
        this.password = password.clone();
    }

    /**
     * Gets a clone of the password stored by this instance. The user is
     * responsible to finalize the returned array if the password clone is no
     * longer needed.
     *
     * @return the copied password.
     */
    public char[] getPassword() {
        return password.clone();
    }

    /**
     * Gets the username stored by this instance.
     *
     * @return the stored username.
     */
    public String getUserName() {
        return userName;
    }
}
