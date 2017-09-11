/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.tests.org.xml.sax.support;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple helper class that logs method calls by storing method names and
 * parameter lists. Used as a foundation for various simple SAX handlers.
 */
public class MethodLogger {

    /**
     * The names of the invoked methods, in order.
     */
    private List<String> methods = new ArrayList<String>();

    /**
     * The parameter lists of the invoked methods, in order.
     */
    private List<Object[]> argLists = new ArrayList<Object[]>();

    /**
     * Adds a method call with a variable list of arguments.
     */
    public void add(String method, Object ... args) {
        Object[] argsCopy = new Object[args.length];
        System.arraycopy(args, 0, argsCopy, 0, args.length);

        methods.add(method);
        argLists.add(argsCopy);
    }

    /**
     * Returns the number of method invoked so far.
     */
    public int size() {
        return methods.size();
    }

    /**
     * Returns the method name stored at the given index.
     */
    public String getMethod(int index) {
        return methods.get(index);
    }

    /**
     * Returns the name of the last method that was invoked. Returns null if no
     * method calls have been logged so far.
     */
    public String getMethod() {
        return (size() == 0 ? null : getMethod(size() - 1));
    }

    /**
     * Returns the argument array stored at the given index. May be empty, but
     * not null.
     */
    public Object[] getArgs(int index) {
        return argLists.get(index);
    }

    /**
     * Returns the argument array of the last method that was invoked. Returns
     * null if no method has been invoked so far.
     */
    public Object[] getArgs() {
        return (size() == 0 ? null : getArgs(size() - 1));
    }

    /**
     * Clears the log.
     */
    public void clear() {
        methods.clear();
        argLists.clear();
    }

}
