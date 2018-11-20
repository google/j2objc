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
* @author Alexander V. Astapchuk
* @version $Revision$
*/

package org.apache.harmony.security.tests.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.math.BigInteger;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;

import java.security.cert.*;
import java.util.*;

import javax.security.auth.x500.X500Principal;

/**
 * The class contains various utility methods used during the java.security
 * classes testing.
 *
 */

public final class TestCertUtils {

    private TestCertUtils() {
        throw new Error("statics only");
    }

    /**
     * Returns new instance of test certificate each time the method is called.
     *
     * @return test certificate
     */
    public static Certificate getCert() {
        return new TestCertificate();
    }

    /**
     * Returns an array of 3 test certificates. IMP: The array returned is not
     * real chain of certificates, it's just an array of 3 certs. The method
     * returns new array each time it's called. The number of 3 was chosen
     * arbitrarily and is subject to change.
     *
     * @return an array of 3 certificates
     */
    public static Certificate[] getCertChain() {
        Certificate[] chain = { new TestCertificate(), new TestCertificate(),
                new TestCertificate() };
        return chain;
    }

    /**
     * Returns a test CertPath, which uses getCertChain() to obtain a list of
     * certificates to store.
     *
     * @return test cert path
     */
    public static CertPath getCertPath() {
        return new TestCertPath();
    }

    /**
     * Generates and returns an instance of TestCertPath.<br>
     * TestCertificate-s included in the CertPath will be uniq (will have
     * different numbers passed to their ctor-s).<br>
     * The second arguments shows which number will have the first Certificate
     * in the CertPath. The second certificate will have (startID+1) number
     * and so on.
     *
     * @param howMany - shows how many TestCerts must contain the CertPath generated
     * @param startID - specifies the starting ID which the first certificate will have
     * @return TestCertPath
     */
    public static CertPath genCertPath(int howMany, int startID) {
        Certificate[] certs = new Certificate[howMany];
        for (int i = 0; i < howMany; i++) {
            certs[i] = new TestCertificate(Integer.toString(startID + i));
        }
        return new TestCertPath(certs);
    }

    private static Provider provider = null;

    private static final String providerName = "TstPrvdr";

    /**
     * A Principal used to form rootCA's certificate
     */
    public static final X500Principal rootPrincipal = new X500Principal(
            UniGen.rootName);

    /**
     * Some fake rootCA's certificate.
     */
    public static final X509Certificate rootCA = new TestX509Certificate(
            rootPrincipal, rootPrincipal);

    public static void install_test_x509_factory() {
        if (provider == null) {
            provider = new TestProvider(providerName, 0.01,
                    "Test provider for serialization testing");
            Security.insertProviderAt(provider, 1);
        }
    }

    public static void uninstall_test_x509_factory() {
        if (provider != null) {
            Security.removeProvider(providerName);
            provider = null;
        }
    }

    /**
     * The class represents test certificate path.
     *
     */

    public static final class TestCertPath extends CertPath implements
            Serializable {

        private static final byte[] encoded = new byte[] { 1, 2, 3, 4, 5, 6, 7,
                8, 9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF };

        private static final String serializedData = "Just a dummy string to be serialized instead of real data";

        private Certificate[] certs;

        /**
         * Default ctor for TestCertPath. Uses {@link TestCertUtils#getCertChain()}
         * to obtain list of certificates.<br>
         * All TestCertPath-s constructed via this ctor will be equals() to each
         * other.
         */
        public TestCertPath() {
            super("testCertPath");
            certs = getCertChain();
        }

        /**
         * Constructs TestCertPath and keeps the given array of certificates.<br>
         * The TestCertPaths constructed via this ctor may be different (if they
         * have different set of certificates)<br>
         * @see TestCertUtils#genCertPath(int, int)
         * @param certs
         */
        public TestCertPath(Certificate[] certs) {
            super("testCertPath");
            this.certs = certs;
        }

        /**
         * @see java.security.cert.CertPath#getCertificates()
         */
        public List<Certificate> getCertificates() {
            return Arrays.asList(certs);
        }

        /**
         * @see java.security.cert.CertPath#getEncoded()
         */
        public byte[] getEncoded() throws CertificateEncodingException {
            return encoded.clone();
        }

        /**
         * @see java.security.cert.CertPath#getEncoded(java.lang.String)
         */
        public byte[] getEncoded(String encoding)
                throws CertificateEncodingException {
            return encoded.clone();
        }

        /**
         * @see java.security.cert.CertPath#getEncodings()
         */
        public Iterator<String> getEncodings() {
            Vector<String> v = new Vector<String>();
            v.add("myTestEncoding");
            return v.iterator();
        }

        public String toString() {
            StringBuffer buf = new StringBuffer(200);
            buf.append("TestCertPath. certs count=");
            if( certs == null ) {
                buf.append("0\n");
            }
            else {
                buf.append(certs.length).append("\n");
                for( int i=0; i<certs.length; i++) {
                    buf.append("\t").append(i).append(" ");
                    buf.append(certs[i]).append("\n");
                }
            }
            return buf.toString();
        }

        /**
         * Writes<br>
         * (String) serializedData<br>
         * (int) number of certificates in this CertPath<br>
         * <array of certificates>
         *
         * @param out
         * @throws IOException
         */
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.writeUTF(serializedData);
            if (certs == null) {
                out.writeInt(0);
            } else {
                out.writeInt(certs.length);
                for (int i = 0; i < certs.length; i++) {
                    out.writeObject(certs[i]);
                }
            }
        }

        private void readObject(ObjectInputStream in) throws IOException,
                ClassNotFoundException {
            String s = in.readUTF();
            if (!serializedData.equals(s)) {
                throw new StreamCorruptedException("expect [" + serializedData
                        + "] got [" + s + "]");
            }
            int count = in.readInt();
            certs = new Certificate[count];
            for (int i = 0; i < count; i++) {
                certs[i] = (Certificate) in.readObject();
            }
        }

        protected Object writeReplace() {
            return this;
        }

        protected Object readResolve() {
            return this;
        }
    }

    /**
     * The class represents empty PublicKey.
     *
     */

    public static final class TestPublicKey implements PublicKey {
        private static final String algo = "testPublicKeyAlgorithm";

        private static final byte[] encoded = new byte[] { 1, 2, 3, 4, 5, 6, 7,
                8, 9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF };

        private static final String format = "testPublicKeyFormat";

        public String getAlgorithm() {
            return algo;
        }

        public byte[] getEncoded() {
            return encoded.clone();
        }

        public String getFormat() {
            return format;
        }
    }

    /**
     * The class represents test certificate.
     *
     */

    public static class TestCertificate extends Certificate implements
            Serializable {

        private static final byte[] encoded = new byte[] { 1, 2, 3, 4, 5, 6, 7,
                8, 9, 0xA, 0xB, 0xC, 0xD, 0xE, 0xF };

        public static final String TYPE = "Test";

        //
        // A String that makes different TestCertificates to be different.
        //
        private String diff = null;

        /**
         * Default ctor. All the TestCertificate-s created with this ctor are equals() to each other.
         * Use TestCertificate(String) if you need non equal TestCertificate-s.
         */
        public TestCertificate() {
            super(TYPE);
        }

        /**
         * A special purpose ctor. Pass different String-s to have different TestCertificates.
         * TestCertificate-s with the same String passed to this ctor are considered equal.
         */
        public TestCertificate(String diff) {
            super(TYPE);
            this.diff = diff;
        }

        /**
         * A ctor that allows to specify both the TYPE of certificate and the
         * diff. Leave the <code>diff</code> null when no difference needed.
         *
         * @param diff
         * @param type
         */
        public TestCertificate(String diff, String type) {
            super(type);
            this.diff = diff;
        }

        public byte[] getEncoded() throws CertificateEncodingException {
            return encoded.clone();
        }

        public void verify(PublicKey key) throws CertificateException,
                NoSuchAlgorithmException, InvalidKeyException,
                NoSuchProviderException, SignatureException {
            // do nothing
        }

        public void verify(PublicKey key, String sigProvider)
                throws CertificateException, NoSuchAlgorithmException,
                InvalidKeyException, NoSuchProviderException,
                SignatureException {
            // do nothing

        }

        public String toString() {
            return "Test certificate - for unit testing only";
        }

        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof TestCertificate)) {
                return false;
            }
            TestCertificate that = (TestCertificate) obj;
            if (this == that) {
                return true;
            }
            if (this.diff == null) {
                return that.diff == null;
            }
            return this.diff.equals(that.diff);
        }

        public PublicKey getPublicKey() {
            return new TestPublicKey();
        }

        /**
         * Writes:<br>
         * boolean - true if this certificate has a diff string,
         * false otherwise, followed by <br>
         * writeUTF() of string (if presented)
         *
         * @param out
         * @throws IOException
         */
        private void writeObject(ObjectOutputStream out) throws IOException {
            if (diff == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(false);
                out.writeUTF(diff);
            }
        }

        private void readObject(ObjectInputStream in) throws IOException,
                ClassNotFoundException {
            boolean hasDiffString = in.readBoolean();
            if (hasDiffString) {
                diff = in.readUTF();
            }
        }

        protected Object writeReplace() {
            return this;
        }

        protected Object readResolve() {
            return this;
        }
    }

    public static class TestInvalidX509Certificate extends TestX509Certificate {
        public TestInvalidX509Certificate(X500Principal subj,
                X500Principal issuer) {
            super(subj, issuer);
        }
    }

    /**
     *
     * TestX509CErtificate.<br>
     * Does nothing interesting, but<br>
     * a) is not abstract, so it can be instantiated<br>
     * b) returns Encoded form<br>
     *
     */
    public static class TestX509Certificate extends X509Certificate {
        private X500Principal subject;

        private X500Principal issuer;

        public TestX509Certificate(X500Principal subj, X500Principal issuer) {
            this.subject = subj;
            this.issuer = issuer;
        }

        public X500Principal getIssuerX500Principal() {
            return issuer;
        }

        public X500Principal getSubjectX500Principal() {
            return subject;
        }

        /**
         * The encoded for of this X509Certificate is a byte array where
         * first are bytes of encoded form of Subject (as X500Principal),
         * followed by one zero byte
         * and followed by the encoded form of Issuer (as X500Principal)
         *
         */
        public byte[] getEncoded() throws CertificateEncodingException {
            byte[] asubj = subject.getEncoded();
            byte[] aissuer = issuer.getEncoded();
            byte[] data = new byte[asubj.length + aissuer.length + 1];

            System.arraycopy(asubj, 0, data, 0, asubj.length);
            //data[asubj.length] = 0;
            System
                    .arraycopy(aissuer, 0, data, asubj.length + 1,
                            aissuer.length);
            return data;
        }

        public void checkValidity() throws CertificateExpiredException,
                CertificateNotYetValidException {
        }

        public void checkValidity(Date date)
                throws CertificateExpiredException,
                CertificateNotYetValidException {
        }

        public int getBasicConstraints() {
            return 0;
        }

        public Principal getIssuerDN() {
            return null;
        }

        public boolean[] getIssuerUniqueID() {
            return null;
        }

        public boolean[] getKeyUsage() {
            return null;
        }

        public Date getNotAfter() {
            return null;
        }

        public Date getNotBefore() {
            return null;
        }

        public BigInteger getSerialNumber() {
            return null;
        }

        public String getSigAlgName() {
            return null;
        }

        public String getSigAlgOID() {
            return null;
        }

        public byte[] getSigAlgParams() {
            return null;
        }

        public byte[] getSignature() {
            return null;
        }

        public Principal getSubjectDN() {
            return null;
        }

        public boolean[] getSubjectUniqueID() {
            return null;
        }

        public byte[] getTBSCertificate() throws CertificateEncodingException {
            return null;
        }

        public int getVersion() {
            return 0;
        }

        public Set getCriticalExtensionOIDs() {
            return null;
        }

        public byte[] getExtensionValue(String oid) {
            return null;
        }

        public Set getNonCriticalExtensionOIDs() {
            return null;
        }

        public boolean hasUnsupportedCriticalExtension() {
            return false;
        }

        public PublicKey getPublicKey() {
            return null;
        }

        public String toString() {
            return null;
        }

        public void verify(PublicKey key, String sigProvider)
                throws CertificateException, NoSuchAlgorithmException,
                InvalidKeyException, NoSuchProviderException,
                SignatureException {

        }

        public void verify(PublicKey key) throws CertificateException,
                NoSuchAlgorithmException, InvalidKeyException,
                NoSuchProviderException, SignatureException {

        }
    }

    /**
     * TestProvider. Does nothing, but pretends to
     * implement X.509 CertificateFactory.
     */
    public static class TestProvider extends Provider {

        private Provider.Service serv;

        public TestProvider(String name, double version, String info) {
            super(name, version, info);
            serv = new Provider.Service(this, "CertificateFactory", "X.509",
                    TestFactorySpi.class.getName(), new ArrayList<String>(), null);
        }

        public synchronized Set<Provider.Service> getServices() {
            HashSet<Provider.Service> s = new HashSet<Service>();
            s.add(serv);
            return s;
        }
    }

    /**
     * Some kind of Certificate Factory, used during unit testing.
     *
     *
     */
    public static class TestFactorySpi extends CertificateFactorySpi {

        /**
         * Tries to create an instance of TestX509Certificate, basing
         * on the presumption that its {@link TestX509Certificate#getEncoded()
         * encoded} form is stored.<br>
         * @throws CertificateException is the presumption is not met or if
         * any IO problem occurs.
         */
        public Certificate engineGenerateCertificate(InputStream is)
                throws CertificateException {
            byte[] data = new byte[0];
            byte[] chunk = new byte[1024];
            int len;
            try {
                while ((len = is.read(chunk)) > 0) {
                    byte[] tmp = new byte[data.length + len];
                    System.arraycopy(data, 0, tmp, 0, data.length);
                    System.arraycopy(chunk, 0, tmp, data.length, len);
                    data = tmp;
                }
            } catch (IOException ex) {
                throw new CertificateException("IO problem", ex);
            }
            int pos = Arrays.binarySearch(data, (byte) 0);
            if (pos < 0) {
                throw new CertificateException("invalid format");
            }
            byte[] subjNameData = new byte[pos];
            System.arraycopy(data, 0, subjNameData, 0, subjNameData.length);
            byte[] issNameData = new byte[data.length - pos - 1];
            System.arraycopy(data, pos + 1, issNameData, 0, issNameData.length);
            X500Principal subjName = new X500Principal(subjNameData);
            X500Principal issName = new X500Principal(issNameData);
            return new TestX509Certificate(subjName, issName);
        }

        /**
         * Not supported yet.
         * @throws UnsupportedOperationException
         */
        public Collection engineGenerateCertificates(InputStream inStream)
                throws CertificateException {
            throw new UnsupportedOperationException("not yet.");
        }

        /**
         * Not supported yet.
         * @throws UnsupportedOperationException
         */
        public CRL engineGenerateCRL(InputStream inStream) throws CRLException {
            throw new UnsupportedOperationException("not yet.");
        }

        /**
         * Not supported yet.
         * @throws UnsupportedOperationException
         */
        public Collection engineGenerateCRLs(InputStream inStream)
                throws CRLException {
            throw new UnsupportedOperationException("not yet.");
        }

        /**
         * Returns an instance of TestCertPath.<br>
         * @throws CertificateException if
         * a) any of Certificates passed is not an instance of X509Certificate
         * b) any of Certificates passed is an instance of TestInvalidX509Certificate
         */
        public CertPath engineGenerateCertPath(List certs)
                throws CertificateException {
            ArrayList<Certificate> validCerts = new ArrayList<Certificate>();
            for (Iterator i = certs.iterator(); i.hasNext();) {
                Certificate c = (Certificate) i.next();
                if (!(c instanceof X509Certificate)) {
                    throw new CertificateException("Not X509: " + c);
                }
                if (c instanceof TestInvalidX509Certificate) {
                    throw new CertificateException("Invalid (test) X509: " + c);
                }
                validCerts.add(c);
            }
            Certificate[] acerts = new Certificate[validCerts.size()];
            validCerts.toArray(acerts);
            return new TestCertPath(acerts);
        }
    }

    /**
     * Utility class used to generate some amount of uniq names.
     */
    public static class UniGen {
        public static final String rootName = "CN=Alex Astapchuk, OU=SSG, O=Intel ZAO, C=RU";

        private static final String datasNames[] = { "CN", "OU", "O", "C" };

        private static final String datas[][] = {
        // Names database
                { "Alex Astapchuk", null, null, null },
                { "John Doe", null, null, null },
                // 'organisation unit'-s
                { null, "SSG", null, null }, { null, "SSG/DRL", null, null },
                // organizations
                { null, null, "Intel ZAO", null },
                { null, null, "Intel Inc", null },
                // countries
                { null, null, null, "RU" }, { null, null, null, "US" },
                { null, null, null, "GB" }, { null, null, null, "JA" },
                { null, null, null, "KO" }, { null, null, null, "TW" }, };

        //
        // Returns a string from <code>data</code> from a given column and
        // position. The positions are looked for first non-null entry. If there
        // are no non empty items left, then it scans column starting from the
        // beginning.
        //
        // @param col
        // @param startRow
        // @return
        //
        private static String getData(int col, int startRow) {
            startRow = startRow % datas.length;
            for (int i = startRow; i < datas.length; i++) {
                if (datas[i][col] != null) {
                    return datas[i][col];
                }
            }
            // no non-null entries left, check from the beginning
            for (int i = 0; i < datas.length; i++) {
                if (datas[i][col] != null) {
                    return datas[i][col];
                }
            }
            // can't be
            throw new Error();
        }

        //
        // Increments a num.<br>
        // <code>num</code> is interpreted as a number with a base of
        // <code>base</code> and each digit of this number is stored as a
        // separate num's element.
        //
        // @param num
        // @param base
        // @return <b>true</b> if overflow happened
        //
        private static boolean inc(int[] num, int base) {
            for (int i = 0; i < num.length; i++) {
                if ((++num[i]) >= base) {
                    num[i] = 0;
                } else {
                    return false;
                }
            }
            return true;
        }

        /**
         * Generates some amount of uniq names, none of which is equals to
         * {@link #rootName}.
         * @param howMany
         * @return
         */
        public static String[] genNames(int howMany) {
            int counts[] = new int[datasNames.length];
            ArrayList<String> al = new ArrayList<String>();

            // not really the thrifty algorithm...
            for (int i = 0; i < howMany;) {

                //                System.out.print("#"+i+": ");
                //                for( int j=0; j<counts.length; j++) {
                //                    System.out.print(""+counts[j]+"|");
                //                }
                //                System.out.println();

                StringBuffer buf = new StringBuffer();
                int j = 0;
                for (; j < datasNames.length - 1; j++) {
                    String name = datasNames[j];
                    String val = getData(j, counts[j]);
                    buf.append(name).append('=').append(val).append(",");
                }
                String name = datasNames[j];
                String val = getData(j, counts[j]);
                buf.append(name).append('=').append(val);

                name = buf.toString();

                if (!(rootName.equals(name) || al.contains(name))) {
                    ++i;
                    al.add(name);
                    //                    System.out.println("generated: "+name);
                } else {
                    //                    System.out.println("rejected: "+name);
                }

                if (inc(counts, datas.length)) {
                    // if this happened, then just add some data into 'datas'
                    throw new Error(
                            "cant generate so many uniq names. sorry. add some more data.");
                }
            }
            return (String[]) al.toArray(new String[al.size()]);
        }

        /**
         * Generates some amount of uniq X500Principals, none of which is equals
         * has a string equals to {@link #rootName}.
         * @param howMany
         * @return
         */
        public static X500Principal[] genX500s(int howMany) {
            String names[] = genNames(howMany);
            X500Principal[] ps = new X500Principal[howMany];
            for (int i = 0; i < howMany; i++) {
                ps[i] = new X500Principal(names[i]);
            }
            return ps;
        }

    }

}

