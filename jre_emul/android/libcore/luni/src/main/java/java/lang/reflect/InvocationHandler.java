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

package java.lang.reflect;

/**
 * Implementors of this interface dispatch methods invoked on proxy instances.
 *
 * @see Proxy
 */
public interface InvocationHandler {

    /**
     * Handles the method which was originally invoked on the proxy instance. A
     * typical usage pattern follows below:
     *
     * <pre>
     * public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
     *     //do some processing before the method invocation
     *
     *     //invoke the method
     *     Object result = method.invoke(proxy, args);
     *
     *     //do some processing after the method invocation
     *     return result;
     * }</pre>
     *
     * @param proxy
     *            the proxy instance on which the method was invoked
     * @param method
     *            the method invoked on the proxy instance
     * @param args
     *            an array of objects containing the parameters passed to the
     *            method, or {@code null} if no arguments are expected.
     *            Primitive types are boxed.
     *
     * @return the result of executing the method. Primitive types are boxed.
     *
     * @throws Throwable
     *             the exception to throw from the invoked method on the proxy.
     *             The exception must match one of the declared exception types
     *             of the invoked method or any unchecked exception type. If not
     *             then an {@code UndeclaredThrowableException} is thrown
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable;
}
