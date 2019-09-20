package org.apache.harmony.xnet.tests.support;

import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSessionBindingEvent;
import javax.net.ssl.SSLSessionBindingListener;
import javax.net.ssl.SSLSessionContext;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import javax.net.ssl.SSLSession;

import org.apache.harmony.security.tests.support.TestCertUtils;

public class mySSLSession implements SSLSession {

    private byte[] idData;
    private String nameHost = null;
    private int namePort = -1;
    private Hashtable table;
    private boolean invalidateDone = false;
    private Certificate[] certs = null;
    private X509Certificate[] xCerts = null;

    public mySSLSession(String host, int port, byte[] id) {
        certs = null;
        xCerts = null;
        nameHost = host;
        namePort = port;
        idData = id;
        table = new Hashtable();
    }

    public mySSLSession(X509Certificate[] xc) {
        certs = TestCertUtils.getCertChain();
        xCerts = xc;
    }

    public mySSLSession(Certificate[] xc) throws CertificateEncodingException, CertificateException {
        certs = xc;
        xCerts = new X509Certificate[xc.length];
        int i = 0;
        for (Certificate cert : xc) {
            xCerts[i++] = X509Certificate.getInstance(cert.getEncoded());
        }
    }

    public mySSLSession() {
    }

    public int getApplicationBufferSize() {
        return 1234567;
    }

    public String getCipherSuite() {
        return "SuiteName";
    }

    public long getCreationTime() {
        return 1000l;
    }

    public byte[] getId() {
        return idData;
    }

    public long getLastAccessedTime() {
        return 2000l;
    }

    public Certificate[] getLocalCertificates() {
        return null;
    }

    public Principal getLocalPrincipal() {
        return null;
    }

    public int getPacketBufferSize() {
        return 12345;
    }

    public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
        if (certs == null) {
            throw new SSLPeerUnverifiedException("peer not authenticated");
        } else {
            return certs;
        }
    }

    public X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
        if(xCerts == null) {
            throw new SSLPeerUnverifiedException("peer not authenticated");
        } else {
            return xCerts;
        }
    }

    public String getPeerHost() {
        return nameHost;
    }

    public int getPeerPort() {
        return namePort;
    }

    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        return null;
    }

    public String getProtocol() {
        return "ProtocolName";
    }

    public SSLSessionContext getSessionContext() {
        return null;
    }

    public void putValue(String s, Object obj) {
        if(s == null || obj == null)
            throw new IllegalArgumentException("arguments can not be null");
        Object obj1 = table.put(s, obj);
        if(obj1 instanceof SSLSessionBindingListener) {
            SSLSessionBindingEvent sslsessionbindingevent = new SSLSessionBindingEvent(this, s);
            ((SSLSessionBindingListener)obj1).valueUnbound(sslsessionbindingevent);
        }
        if(obj instanceof SSLSessionBindingListener) {
            SSLSessionBindingEvent sslsessionbindingevent1 = new SSLSessionBindingEvent(this, s);
            ((SSLSessionBindingListener)obj).valueBound(sslsessionbindingevent1);
        }
    }

    public void removeValue(String s) {
        if(s == null)
            throw new IllegalArgumentException("argument can not be null");
        Object obj = table.remove(s);
        if(obj instanceof SSLSessionBindingListener) {
            SSLSessionBindingEvent sslsessionbindingevent = new SSLSessionBindingEvent(this, s);
            ((SSLSessionBindingListener)obj).valueUnbound(sslsessionbindingevent);
        }
    }

    public Object getValue(String s) {
        if(s == null) {
            throw new IllegalArgumentException("argument can not be null");
        } else {
            return table.get(s);
        }
    }

    public String[] getValueNames() {
        Vector vector = new Vector();
        Enumeration enumeration = table.keys();
        while (enumeration.hasMoreElements()) {
            vector.addElement(enumeration.nextElement());
        }
        String as[] = new String[vector.size()];
        vector.copyInto(as);
        return as;
    }

    public void invalidate() {
        invalidateDone = true;
    }

    public boolean isValid() {
        return invalidateDone;
    }

}
