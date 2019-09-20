/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 1997, 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */


package javax.net.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import javax.net.ServerSocketFactory;
import java.security.*;

/**
 * <code>SSLServerSocketFactory</code>s create
 * <code>SSLServerSocket</code>s.
 *
 * @since 1.4
 * @see SSLSocket
 * @see SSLServerSocket
 * @author David Brownell
 */
public abstract class SSLServerSocketFactory extends ServerSocketFactory
{
    private static SSLServerSocketFactory defaultServerSocketFactory;

    private static int lastVersion = -1;

    private static void log(String msg) {
        if (SSLSocketFactory.DEBUG) {
            System.out.println(msg);
        }
    }

    /**
     * Constructor is used only by subclasses.
     */
    protected SSLServerSocketFactory() { /* NOTHING */ }

    /**
     * Returns the default SSL server socket factory.
     *
     * <p>The first time this method is called, the security property
     * "ssl.ServerSocketFactory.provider" is examined. If it is non-null, a
     * class by that name is loaded and instantiated. If that is successful and
     * the object is an instance of SSLServerSocketFactory, it is made the
     * default SSL server socket factory.
     *
     * <p>Otherwise, this method returns
     * <code>SSLContext.getDefault().getServerSocketFactory()</code>. If that
     * call fails, an inoperative factory is returned.
     *
     * @return the default <code>ServerSocketFactory</code>
     * @see SSLContext#getDefault
     */
    public static synchronized ServerSocketFactory getDefault() {
        // Android-changed: Use security version instead of propertyChecked.
        //
        // We use the same lookup logic in SSLSocketFactory.getDefault(). Any changes
        // made here must be mirrored in that class.
        if (defaultServerSocketFactory != null && lastVersion == Security.getVersion()) {
            return defaultServerSocketFactory;
        }

        lastVersion = Security.getVersion();
        SSLServerSocketFactory previousDefaultServerSocketFactory = defaultServerSocketFactory;
        defaultServerSocketFactory = null;

        String clsName = SSLSocketFactory.getSecurityProperty
                ("ssl.ServerSocketFactory.provider");
        if (clsName != null) {
            // The instance for the default socket factory is checked for updates quite
            // often (for instance, every time a security provider is added). Which leads
            // to unnecessary overload and excessive error messages in case of class-loading
            // errors. Avoid creating a new object if the class name is the same as before.
            if (previousDefaultServerSocketFactory != null
                    && clsName.equals(previousDefaultServerSocketFactory.getClass().getName())) {
                defaultServerSocketFactory = previousDefaultServerSocketFactory;
                return defaultServerSocketFactory;
            }
            Class cls = null;
            log("setting up default SSLServerSocketFactory");
            try {
                log("setting up default SSLServerSocketFactory");
                try {
                    cls = Class.forName(clsName);
                } catch (ClassNotFoundException e) {
                    // Android-changed; Try the contextClassLoader first.
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    if (cl == null) {
                        cl = ClassLoader.getSystemClassLoader();
                    }

                    if (cl != null) {
                        cls = Class.forName(clsName, true, cl);
                    }
                }
                log("class " + clsName + " is loaded");
                SSLServerSocketFactory fac = (SSLServerSocketFactory) cls.newInstance();
                log("instantiated an instance of class " + clsName);
                defaultServerSocketFactory = fac;
                if (defaultServerSocketFactory != null) {
                    return defaultServerSocketFactory;
                }
            } catch (Exception e) {
                log("SSLServerSocketFactory instantiation failed: " + e);
                // Android-changed: Fallback to the default SSLContext if an exception
                // is thrown during the initialization of ssl.ServerSocketFactory.provider.
            }
        }

        try {
            SSLContext context = SSLContext.getDefault();
            if (context != null) {
                defaultServerSocketFactory = context.getServerSocketFactory();
            }
        } catch (NoSuchAlgorithmException e) {
        }

        if (defaultServerSocketFactory == null) {
            defaultServerSocketFactory = new DefaultSSLServerSocketFactory(
                    new IllegalStateException("No ServerSocketFactory implementation found"));
        }

        return defaultServerSocketFactory;
    }

    /**
     * Returns the list of cipher suites which are enabled by default.
     * Unless a different list is enabled, handshaking on an SSL connection
     * will use one of these cipher suites.  The minimum quality of service
     * for these defaults requires confidentiality protection and server
     * authentication (that is, no anonymous cipher suites).
     *
     * @see #getSupportedCipherSuites()
     * @return array of the cipher suites enabled by default
     */
    public abstract String [] getDefaultCipherSuites();


    /**
     * Returns the names of the cipher suites which could be enabled for use
     * on an SSL connection created by this factory.
     * Normally, only a subset of these will actually
     * be enabled by default, since this list may include cipher suites which
     * do not meet quality of service requirements for those defaults.  Such
     * cipher suites are useful in specialized applications.
     *
     * @return an array of cipher suite names
     * @see #getDefaultCipherSuites()
     */
    public abstract String [] getSupportedCipherSuites();
}


//
// The default factory does NOTHING.
//
class DefaultSSLServerSocketFactory extends SSLServerSocketFactory {

    private final Exception reason;

    DefaultSSLServerSocketFactory(Exception reason) {
        this.reason = reason;
    }

    private ServerSocket throwException() throws SocketException {
        throw (SocketException)
            new SocketException(reason.toString()).initCause(reason);
    }

    @Override
    public ServerSocket createServerSocket() throws IOException {
        return throwException();
    }


    @Override
    public ServerSocket createServerSocket(int port)
    throws IOException
    {
        return throwException();
    }

    @Override
    public ServerSocket createServerSocket(int port, int backlog)
    throws IOException
    {
        return throwException();
    }

    @Override
    public ServerSocket
    createServerSocket(int port, int backlog, InetAddress ifAddress)
    throws IOException
    {
        return throwException();
    }

    @Override
    public String [] getDefaultCipherSuites() {
        return new String[0];
    }

    @Override
    public String [] getSupportedCipherSuites() {
        return new String[0];
    }
}
