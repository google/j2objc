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

/**
* @author Vladimir N. Molotkov
* @version $Revision$
*/

package org.apache.harmony.security.tests.support.cert;

import java.io.ObjectStreamException;
import java.security.cert.CertPath;
import java.security.cert.CertificateEncodingException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 * Stub class for <code>java.security.cert.CertPath</code> tests
 *
 */
public class MyCertPath extends CertPath {
    private static final long serialVersionUID = 7444835599161870893L;
    /**
     * my certificates list
     */
    private final Vector<MyCertificate> certificates;

    /**
     * List of encodings supported
     */
    private final Vector<String> encodingNames;
    /**
     * my cert path the only encoding
     */
    private final byte[] encoding;

    /**
     * Constructs new instance of <code>MyCertPath</code>
     *
     */
    public MyCertPath(byte[] encoding) {
        super("MyEncoding");
        this.encoding = encoding;
        certificates = new Vector<MyCertificate>();
        certificates.add(new MyCertificate("MyEncoding", encoding));
        encodingNames = new Vector<String>();
        encodingNames.add("MyEncoding");
    }

    /**
     * @return certificates list
     * @see java.security.cert.CertPath#getCertificates()
     */
    public List<MyCertificate> getCertificates() {
        return Collections.unmodifiableList(certificates);
    }

    /**
     * @return default encoded form of this cert path
     * @throws CertificateEncodingException
     * @see java.security.cert.CertPath#getEncoded()
     */
    public byte[] getEncoded() throws CertificateEncodingException {
        return encoding.clone();
    }

    /**
     * @return encoded form of this cert path as specified by
     * <code>encoding</code> parameter
     * @throws CertificateEncodingException
     *             if <code>encoding</code> not equals "MyEncoding"
     * @see java.security.cert.CertPath#getEncoded(java.lang.String)
     */
    public byte[] getEncoded(String encoding)
            throws CertificateEncodingException {
        if (getType().equals(encoding)) {
            return this.encoding.clone();
        }
        throw new CertificateEncodingException("Encoding not supported: " +
                encoding);
    }

    /**
     * @return iterator through encodings supported
     * @see java.security.cert.CertPath#getEncodings()
     */
    public Iterator<String> getEncodings() {
        return Collections.unmodifiableCollection(encodingNames).iterator();
    }

    /**
     * @return the CertPathRep to be serialized
     * @see java.security.cert.CertPath#writeReplace()
     */
    public Object writeReplace() throws ObjectStreamException {
        return super.writeReplace();
    }

    public class MyCertPathRep extends CertPath.CertPathRep {

        private static final long serialVersionUID = 1609000085450479173L;

        private String type;
        private byte[] data;

        public MyCertPathRep(String type, byte[] data) {
            super(type, data);
            this.data = data;
            this.type = type;
        }

        public Object readResolve() throws ObjectStreamException {
            return super.readResolve();
        }

        public String getType() {
            return type;
        }

        public byte[] getData() {
            return data;
        }
    }
}
