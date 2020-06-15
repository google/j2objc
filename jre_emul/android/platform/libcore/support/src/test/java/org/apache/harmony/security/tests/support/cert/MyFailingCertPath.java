package org.apache.harmony.security.tests.support.cert;

import java.security.cert.CertificateEncodingException;

public class MyFailingCertPath extends MyCertPath {

    public MyFailingCertPath(byte[] encoding) {
        super(encoding);
    }

    @Override
    public byte[] getEncoded() throws CertificateEncodingException {
        throw new CertificateEncodingException("testing purpose");
    }

}
