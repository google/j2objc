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

import java.io.Serializable;
import java.util.Arrays;

/**
 * Used in conjunction with a {@link CallbackHandler} to retrieve a password
 * when needed.
 */
public class PasswordCallback implements Callback, Serializable {

    private static final long serialVersionUID = 2267422647454909926L;

    private String prompt;

    boolean echoOn;

    private char[] inputPassword;

    private void setPrompt(String prompt) throws IllegalArgumentException {
        if (prompt == null || prompt.length() == 0) {
            throw new IllegalArgumentException("Invalid prompt");
        }
        this.prompt = prompt;
    }

    /**
     * Creates a new {@code PasswordCallback} instance.
     *
     * @param prompt
     *            the message that should be displayed to the user
     * @param echoOn
     *            determines whether the user input should be echoed
     */
    public PasswordCallback(String prompt, boolean echoOn) {
        setPrompt(prompt);
        this.echoOn = echoOn;
    }

    /**
     * Returns the prompt that was specified when creating this {@code
     * PasswordCallback}
     *
     * @return the prompt
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     * Queries whether this {@code PasswordCallback} expects user input to be
     * echoed, which is specified during the creation of the object.
     *
     * @return {@code true} if (and only if) user input should be echoed
     */
    public boolean isEchoOn() {
        return echoOn;
    }

    /**
     * Sets the password. The {@link CallbackHandler} that performs the actual
     * provisioning or input of the password needs to call this method to hand
     * back the password to the security service that requested it.
     *
     * @param password
     *            the password. A copy of this is stored, so subsequent changes
     *            to the input array do not affect the {@code PasswordCallback}.
     */
    public void setPassword(char[] password) {
        if (password == null) {
            this.inputPassword = password;
        } else {
            inputPassword = new char[password.length];
            System.arraycopy(password, 0, inputPassword, 0, inputPassword.length);
        }
    }

    /**
     * Returns the password. The security service that needs the password
     * usually calls this method once the {@link CallbackHandler} has finished
     * its work.
     *
     * @return the password. A copy of the internal password is created and
     *         returned, so subsequent changes to the internal password do not
     *         affect the result.
     */
    public char[] getPassword() {
        if (inputPassword != null) {
            char[] tmp = new char[inputPassword.length];
            System.arraycopy(inputPassword, 0, tmp, 0, tmp.length);
            return tmp;
        }
        return null;
    }

    /**
     * Clears the password stored in this {@code PasswordCallback}.
     */
    public void clearPassword() {
        if (inputPassword != null) {
            Arrays.fill(inputPassword, '\u0000');
        }
    }
}
