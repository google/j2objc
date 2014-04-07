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

import java.security.NoSuchAlgorithmException;
import java.security.Security;
import javax.net.ServerSocketFactory;

/**
 * The factory for SSL server sockets.
 */
public abstract class SSLServerSocketFactory extends ServerSocketFactory {
    // TODO EXPORT CONTROL

    // The default SSL socket factory
    private static ServerSocketFactory defaultServerSocketFactory;

    private static String defaultName;

    /**
     * Returns the default {@code SSLServerSocketFactory} instance. The default
     * implementation is defined by the security property
     * "ssl.ServerSocketFactory.provider".
     *
     * @return the default {@code SSLServerSocketFactory} instance.
     */
    public static synchronized ServerSocketFactory getDefault() {
        if (defaultServerSocketFactory != null) {
            return defaultServerSocketFactory;
        }
        if (defaultName == null) {
            defaultName = Security.getProperty("ssl.ServerSocketFactory.provider");
            if (defaultName != null) {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                if (cl == null) {
                    cl = ClassLoader.getSystemClassLoader();
                }
                try {
                    final Class<?> ssfc = Class.forName(defaultName, true, cl);
                    defaultServerSocketFactory = (ServerSocketFactory) ssfc.newInstance();
                } catch (Exception e) {
                }
            }
        }
        if (defaultServerSocketFactory == null) {
            SSLContext context;
            try {
                context = SSLContext.getDefault();
            } catch (NoSuchAlgorithmException e) {
                context = null;
            }
            if (context != null) {
                defaultServerSocketFactory = context.getServerSocketFactory();
            }
        }
        if (defaultServerSocketFactory == null) {
            // Use internal dummy implementation
            defaultServerSocketFactory = new DefaultSSLServerSocketFactory(
                    "No ServerSocketFactory installed");
        }
        return defaultServerSocketFactory;
    }

    /**
     * Creates a new {@code SSLServerSocketFactory} instance.
     */
    protected SSLServerSocketFactory() {
    }

    /**
     * Returns the names of the cipher suites that are enabled by default.
     *
     * @return the names of the cipher suites that are enabled by default
     */
    public abstract String[] getDefaultCipherSuites();

    /**
     * Returns the list of supported cipher suites that could be enabled for an
     * SSL connection created by this factory.
     *
     * @return the list of supported cipher suites
     */
    public abstract String[] getSupportedCipherSuites();
}
