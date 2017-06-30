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

package org.apache.harmony.tests.javax.net.ssl;

import java.security.Principal;
import java.security.cert.Certificate;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSessionBindingEvent;
import javax.security.cert.X509Certificate;

import junit.framework.TestCase;

/**
 * Tests for <code>SSLSessionBindingEvent</code> class constructors and methods.
 *
 */
public class SSLSessionBindingEventTest extends TestCase {

    public final void test_ConstructorLjavax_net_ssl_SSLSessionLjava_lang_String() {
        SSLSession ses = new MySSLSession();

        try {
            SSLSessionBindingEvent event = new SSLSessionBindingEvent(ses, "test");
            if (!"test".equals(event.getName())) {
                fail("incorrect name");
            }
            if (!event.getSession().equals(ses)) {
                fail("incorrect session");
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }

        try {
            SSLSessionBindingEvent event = new SSLSessionBindingEvent(null, "test");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
          // expected
        }

        try {
            SSLSessionBindingEvent event = new SSLSessionBindingEvent(ses, null);
        } catch (IllegalArgumentException e) {
          fail("Unexpected IllegalArgumentException: " + e);
        }
    }

    /**
     * javax.net.ssl.SSLSessionBindingEvent#getName()
     */
    public void test_getName() {
        SSLSession ses = new MySSLSession();
        SSLSessionBindingEvent event = new SSLSessionBindingEvent(ses, "test");
        assertEquals("Incorrect session name", "test", event.getName());
        event = new SSLSessionBindingEvent(ses, null);
        assertEquals("Incorrect session name", null, event.getName());
    }

    /**
     * javax.net.ssl.SSLSessionBindingEvent#getSession()
     */
    public void test_getSession() {
        SSLSession ses = new MySSLSession();
        SSLSessionBindingEvent event = new SSLSessionBindingEvent(ses, "test");
        assertEquals("Incorrect session", ses, event.getSession());
    }
}

class MySSLSession implements SSLSession {
    /*
     * @see javax.net.ssl.SSLSession#getApplicationBufferSize()
     */
    public int getApplicationBufferSize() {
        return 0;
    }

    /*
     * @see javax.net.ssl.SSLSession#getCipherSuite()
     */
    public String getCipherSuite() {
        return "MyTestCipherSuite";
    }

    /*
     * @see javax.net.ssl.SSLSession#getCreationTime()
     */
    public long getCreationTime() {
        return 0;
    }

    /*
     * @see javax.net.ssl.SSLSession#getId()
     */
    public byte[] getId() {
        return null;
    }

    /*
     * @see javax.net.ssl.SSLSession#getLastAccessedTime()
     */
    public long getLastAccessedTime() {
        return 0;
    }

    /*
     * @see javax.net.ssl.SSLSession#getLocalCertificates()
     */
    public Certificate[] getLocalCertificates() {
        return null;
    }

    /*
     * @see javax.net.ssl.SSLSession#getLocalPrincipal()
     */
    public Principal getLocalPrincipal() {
        return null;
    }

    /*
     * @see javax.net.ssl.SSLSession#getPacketBufferSize()
     */
    public int getPacketBufferSize() {
        return 0;
    }

    /*
     * @see javax.net.ssl.SSLSession#getPeerCertificateChain()
     */
    public X509Certificate[] getPeerCertificateChain()
    throws SSLPeerUnverifiedException {
        throw new SSLPeerUnverifiedException("test exception");
    }

    /*
     * @see javax.net.ssl.SSLSession#getPeerCertificates()
     */
    public Certificate[] getPeerCertificates()
    throws SSLPeerUnverifiedException {
        throw new SSLPeerUnverifiedException("test exception");
    }

    /*
     * @see javax.net.ssl.SSLSession#getPeerHost()
     */
    public String getPeerHost() {
        return null;
    }

    /*
     * @see javax.net.ssl.SSLSession#getPeerPort()
     */
    public int getPeerPort() {
        return 0;
    }

    /*
     * @see javax.net.ssl.SSLSession#getPeerPrincipal()
     */
    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        return null;
    }

    /*
     * @see javax.net.ssl.SSLSession#getProtocol()
     */
    public String getProtocol() {
        return null;
    }

    /*
     * @see javax.net.ssl.SSLSession#getSessionContext()
     */
    public SSLSessionContext getSessionContext() {
        return null;
    }

    /*
     * @see javax.net.ssl.SSLSession#getValue(java.lang.String)
     */
    public Object getValue(String name) {
        return null;
    }

    /*
     * @see javax.net.ssl.SSLSession#getValueNames()
     */
    public String[] getValueNames() {
        return null;
    }

    /*
     * @see javax.net.ssl.SSLSession#invalidate()
     */
    public void invalidate() {
    }

    /*
     * @see javax.net.ssl.SSLSession#isValid()
     */
    public boolean isValid() {
        return false;
    }

    /*
     * @see javax.net.ssl.SSLSession#putValue(java.lang.String,
     *      java.lang.Object)
     */
    public void putValue(String name, Object value) {
    }

    /*
     * @see javax.net.ssl.SSLSession#removeValue(java.lang.String)
     */
    public void removeValue(String name) {
    }

}

