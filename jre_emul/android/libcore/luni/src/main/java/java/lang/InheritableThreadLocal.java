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

package java.lang;


/**
 * A thread-local variable whose value is passed from parent to child thread. By
 * default, the value of an inheritable thread-local variable of a child thread
 * is initialized with the value of the parent thread's variable at thread
 * creation time. However, subclasses may override {code #childValue(Object)}
 * to provide an arbitrary function for passing the value of a parent's
 * thread-local variable to the child's thread-local variable.
 *
 * @see java.lang.Thread
 * @see java.lang.ThreadLocal
 */
public class InheritableThreadLocal<T> extends ThreadLocal<T> {

    /**
     * Creates a new inheritable thread-local variable.
     */
    public InheritableThreadLocal() {
    }

    /**
     * Computes the initial value of this thread-local variable for the child
     * thread given the parent thread's value. Called from the parent thread when
     * creating a child thread. The default implementation returns the parent
     * thread's value.
     *
     * @param parentValue the value of the variable in the parent thread.
     * @return the initial value of the variable for the child thread.
     */
    protected T childValue(T parentValue) {
        return parentValue;
    }

    @Override
    Values values(Thread current) {
        return current.inheritableValues;
    }

    @Override
    Values initializeValues(Thread current) {
        return current.inheritableValues = new Values();
    }
}
