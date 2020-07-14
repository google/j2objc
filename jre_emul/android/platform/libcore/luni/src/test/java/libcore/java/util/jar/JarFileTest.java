/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.util.jar;

/**
 * J2ObjC removed: These test the modified jar feature j2objc doesn't support,
 * because executing downloaded code is banned from iOS apps
 */
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URL;
//import java.security.CodeSigner;
//import java.security.InvalidKeyException;
//import java.security.InvalidParameterException;
//import java.security.Permission;
//import java.security.PrivateKey;
//import java.security.Provider;
//import java.security.PublicKey;
//import java.security.Security;
//import java.security.SignatureException;
//import java.security.SignatureSpi;
//import java.security.cert.Certificate;
//import java.security.cert.X509Certificate;
//import java.util.Arrays;
//import java.util.ArrayList;
//import java.util.Enumeration;
//import java.util.List;
//import java.util.Vector;
//import java.util.jar.Attributes;
//import java.util.jar.JarEntry;
//import java.util.jar.JarFile;
//import java.util.jar.JarOutputStream;
//import java.util.jar.Manifest;
//import java.util.zip.ZipEntry;
//import java.util.zip.ZipException;
//import java.util.zip.ZipFile;
import junit.framework.TestCase;
//import libcore.io.IoUtils;
//import tests.support.resource.Support_Resources;


public class JarFileTest extends TestCase {

    public void test(){}

    // BEGIN Android-added
//    public byte[] getAllBytesFromStream(InputStream is) throws IOException {
//        ByteArrayOutputStream bs = new ByteArrayOutputStream();
//        byte[] buf = new byte[666];
//        int iRead;
//        int off;
//        while (is.available() > 0) {
//            iRead = is.read(buf, 0, buf.length);
//            if (iRead > 0) bs.write(buf, 0, iRead);
//        }
//        return bs.toByteArray();
//    }

    // END Android-added
//
//    private final String jarName = "hyts_patch.jar"; // a 'normal' jar file
//
//    private final String jarName2 = "hyts_patch2.jar";
//
//    private final String jarName3 = "hyts_manifest1.jar";
//
//    private final String jarName4 = "hyts_signed.jar";
//
//    private final String jarName5 = "hyts_signed_inc.jar";
//
//    private final String jarName6 = "hyts_signed_sha256withrsa.jar";
//
//    private final String jarName7 = "hyts_signed_sha256digest_sha256withrsa.jar";
//
//    private final String jarName8 = "hyts_signed_sha512digest_sha512withecdsa.jar";
//
//    private final String jarName9 = "hyts_signed_sha256digest_sha256withecdsa.jar";
//
//    private final String authAttrsJar = "hyts_signed_authAttrs.jar";
//
//    private final String entryName = "foo/bar/A.class";
//
//    private final String entryName3 = "coucou/FileAccess.class";
//
//    private final String integrateJar = "Integrate.jar";
//
//    private final String integrateJarEntry = "Test.class";
//
//    private final String emptyEntryJar = "EmptyEntries_signed.jar";
//
//    /*
//     * /usr/bin/openssl genrsa 2048 > root1.pem
//     * /usr/bin/openssl req -new -key root1.pem -out root1.csr -subj '/CN=root1'
//     * /usr/bin/openssl x509 -req -days 3650 -in root1.csr -signkey root1.pem -out root1.crt
//     * /usr/bin/openssl genrsa 2048 > root2.pem
//     * /usr/bin/openssl req -new -key root2.pem -out root2.csr -subj '/CN=root2'
//     * echo 4000 > root1.srl
//     * echo 8000 > root2.srl
//     * /usr/bin/openssl x509 -req -days 3650 -in root2.csr -CA root1.crt -CAkey root1.pem -out root2.crt
//     * /usr/bin/openssl x509 -req -days 3650 -in root1.csr -CA root2.crt -CAkey root2.pem -out root1.crt
//     * /usr/bin/openssl genrsa 2048 > signer.pem
//     * /usr/bin/openssl req -new -key signer.pem -out signer.csr -subj '/CN=signer'
//     * /usr/bin/openssl x509 -req -days 3650 -in signer.csr -CA root1.crt -CAkey root1.pem -out signer.crt
//     * /usr/bin/openssl pkcs12 -inkey signer.pem -in signer.crt -export -out signer.p12 -name signer -passout pass:certloop
//     * keytool -importkeystore -srckeystore signer.p12 -srcstoretype PKCS12 -destkeystore signer.jks -srcstorepass certloop -deststorepass certloop
//     * cat signer.crt root1.crt root2.crt > chain.crt
//     * zip -d hyts_certLoop.jar 'META-INF/*'
//     * jarsigner -keystore signer.jks -certchain chain.crt -storepass certloop hyts_certLoop.jar signer
//     */
//    private final String certLoopJar = "hyts_certLoop.jar";
//
//    private final String emptyEntry1 = "subfolder/internalSubset01.js";
//
//    private final String emptyEntry2 = "svgtest.js";
//
//    private final String emptyEntry3 = "svgunit.js";
//
//    private static final String VALID_CHAIN_JAR = "hyts_signed_validChain.jar";
//
//    private static final String INVALID_CHAIN_JAR = "hyts_signed_invalidChain.jar";
//
//    private static final String AMBIGUOUS_SIGNERS_JAR = "hyts_signed_ambiguousSignerArray.jar";
//
//    private File resources;
//
//    // custom security manager
//    SecurityManager sm = new SecurityManager() {
//        final String forbidenPermissionName = "user.dir";
//
//        public void checkPermission(Permission perm) {
//            if (perm.getName().equals(forbidenPermissionName)) {
//                throw new SecurityException();
//            }
//        }
//    };
//
//    @Override
//    protected void setUp() {
//        resources = Support_Resources.createTempFolder();
//    }
//
//    /**
//     * java.util.jar.JarFile#JarFile(java.io.File)
//     */
//    public void test_ConstructorLjava_io_File() {
//        try {
//            JarFile jarFile = new JarFile(new File("Wrong.file"));
//            fail("Should throw IOException");
//        } catch (IOException e) {
//            // expected
//        }
//
//        try {
//            File file = Support_Resources.copyFile(resources, null, jarName);
//            JarFile jarFile = new JarFile(file);
//            jarFile.close();
//        } catch (IOException e) {
//            fail("Should not throw IOException");
//        }
//    }
//
//    /**
//     * java.util.jar.JarFile#JarFile(java.lang.String)
//     */
//    public void test_ConstructorLjava_lang_String() {
//        try {
//            JarFile jarFile = new JarFile("Wrong.file");
//            fail("Should throw IOException");
//        } catch (IOException e) {
//            // expected
//        }
//
//        try {
//            File file = Support_Resources.copyFile(resources, null, jarName);
//            String fileName = file.getCanonicalPath();
//            JarFile jarFile = new JarFile(fileName);
//            jarFile.close();
//        } catch (IOException e) {
//            fail("Should not throw IOException");
//        }
//    }
//
//    /**
//     * java.util.jar.JarFile#JarFile(java.lang.String, boolean)
//     */
//    public void test_ConstructorLjava_lang_StringZ() {
//        try {
//            JarFile jarFile = new JarFile("Wrong.file", false);
//            fail("Should throw IOException");
//        } catch (IOException e) {
//            // expected
//        }
//
//        try {
//            File file = Support_Resources.copyFile(resources, null, jarName);
//            String fileName = file.getCanonicalPath();
//            JarFile jarFile = new JarFile(fileName, true);
//            jarFile.close();
//        } catch (IOException e) {
//            fail("Should not throw IOException");
//        }
//    }
//
//    /**
//     * java.util.jar.JarFile#JarFile(java.io.File, boolean)
//     */
//    public void test_ConstructorLjava_io_FileZ() {
//        try {
//            JarFile jarFile = new JarFile(new File("Wrong.file"), true);
//            fail("Should throw IOException");
//        } catch (IOException e) {
//            // expected
//        }
//
//        try {
//            File file = Support_Resources.copyFile(resources, null, jarName);
//            JarFile jarFile = new JarFile(file, false);
//            jarFile.close();
//        } catch (IOException e) {
//            fail("Should not throw IOException");
//        }
//    }
//
//    /**
//     * java.util.jar.JarFile#JarFile(java.io.File, boolean, int)
//     */
//    public void test_ConstructorLjava_io_FileZI() {
//        try {
//            JarFile jarFile = new JarFile(new File("Wrong.file"), true,
//                    ZipFile.OPEN_READ);
//            fail("Should throw IOException");
//        } catch (IOException e) {
//            // expected
//        }
//
//        try {
//            File file = Support_Resources.copyFile(resources, null, jarName);
//            JarFile jarFile = new JarFile(file, false, ZipFile.OPEN_READ);
//            jarFile.close();
//        } catch (IOException e) {
//            fail("Should not throw IOException");
//        }
//
//        try {
//            File file = Support_Resources.copyFile(resources, null, jarName);
//            JarFile jarFile = new JarFile(file, false,
//                    ZipFile.OPEN_READ | ZipFile.OPEN_DELETE + 33);
//            fail("Should throw IllegalArgumentException");
//        } catch (IOException e) {
//            fail("Should not throw IOException");
//        } catch (IllegalArgumentException e) {
//            // expected
//        }
//    }
//
//    /**
//     * Constructs JarFile object.
//     *
//     * java.util.jar.JarFile#JarFile(java.io.File)
//     * java.util.jar.JarFile#JarFile(java.lang.String)
//     */
//    public void testConstructor_file() throws IOException {
//        File f = Support_Resources.copyFile(resources, null, jarName);
//        try (JarFile jarFile = new JarFile(f)) {
//            assertTrue(jarFile.getEntry(entryName).getName().equals(entryName));
//        }
//
//        try (JarFile jarFile = new JarFile(f.getPath())) {
//            assertTrue(jarFile.getEntry(entryName).getName().equals(entryName));
//        }
//    }
//
//    /**
//     * java.util.jar.JarFile#entries()
//     */
//    public void test_entries() throws Exception {
//        /*
//         * Note only (and all of) the following should be contained in the file
//         * META-INF/ META-INF/MANIFEST.MF foo/ foo/bar/ foo/bar/A.class Blah.txt
//         */
//        File file = Support_Resources.copyFile(resources, null, jarName);
//        JarFile jarFile = new JarFile(file);
//        Enumeration<JarEntry> e = jarFile.entries();
//        int i;
//        for (i = 0; e.hasMoreElements(); i++) {
//            e.nextElement();
//        }
//        assertEquals(jarFile.size(), i);
//        jarFile.close();
//        assertEquals(6, i);
//    }
//
//    /**
//     * @throws IOException
//     * java.util.jar.JarFile#getJarEntry(java.lang.String)
//     */
//    public void test_getEntryLjava_lang_String() throws IOException {
//        try {
//            File file = Support_Resources.copyFile(resources, null, jarName);
//            JarFile jarFile = new JarFile(file);
//            assertEquals("Error in returned entry", 311, jarFile.getEntry(
//                    entryName).getSize());
//            jarFile.close();
//        } catch (Exception e) {
//            fail("Exception during test: " + e.toString());
//        }
//
//        File file = Support_Resources.copyFile(resources, null, jarName);
//        JarFile jarFile = new JarFile(file);
//        Enumeration<JarEntry> enumeration = jarFile.entries();
//        assertTrue(enumeration.hasMoreElements());
//        while (enumeration.hasMoreElements()) {
//            JarEntry je = enumeration.nextElement();
//            jarFile.getEntry(je.getName());
//        }
//
//        enumeration = jarFile.entries();
//        assertTrue(enumeration.hasMoreElements());
//        JarEntry je = enumeration.nextElement();
//        try {
//            jarFile.close();
//            jarFile.getEntry(je.getName());
//            // fail("IllegalStateException expected.");
//        } catch (IllegalStateException ee) { // Per documentation exception
//            // may be thrown.
//            // expected
//        }
//    }
//
//    /**
//     * @throws IOException
//     * java.util.jar.JarFile#getJarEntry(java.lang.String)
//     */
//    public void test_getJarEntryLjava_lang_String() throws IOException {
//        try {
//            File file = Support_Resources.copyFile(resources, null, jarName);
//            JarFile jarFile = new JarFile(file);
//            assertEquals("Error in returned entry", 311, jarFile.getJarEntry(
//                    entryName).getSize());
//            jarFile.close();
//        } catch (Exception e) {
//            fail("Exception during test: " + e.toString());
//        }
//
//        File file = Support_Resources.copyFile(resources, null, jarName);
//        JarFile jarFile = new JarFile(file);
//        Enumeration<JarEntry> enumeration = jarFile.entries();
//        assertTrue(enumeration.hasMoreElements());
//        while (enumeration.hasMoreElements()) {
//            JarEntry je = enumeration.nextElement();
//            jarFile.getJarEntry(je.getName());
//        }
//
//        enumeration = jarFile.entries();
//        assertTrue(enumeration.hasMoreElements());
//        JarEntry je = enumeration.nextElement();
//        try {
//            jarFile.close();
//            jarFile.getJarEntry(je.getName());
//            // fail("IllegalStateException expected.");
//        } catch (IllegalStateException ee) { // Per documentation exception
//            // may be thrown.
//            // expected
//        }
//    }
//
//    /**
//     * java.util.jar.JarFile#getManifest()
//     */
//    public void test_getManifest() {
//        // Test for method java.util.jar.Manifest
//        // java.util.jar.JarFile.getManifest()
//        try {
//            File file = Support_Resources.copyFile(resources, null, jarName);
//            JarFile jarFile = new JarFile(file);
//            assertNotNull("Error--Manifest not returned", jarFile.getManifest());
//            jarFile.close();
//        } catch (Exception e) {
//            fail("Exception during 1st test: " + e.toString());
//        }
//        try {
//            File file = Support_Resources.copyFile(resources, null, jarName2);
//            JarFile jarFile = new JarFile(file);
//            assertNull("Error--should have returned null", jarFile
//                    .getManifest());
//            jarFile.close();
//        } catch (Exception e) {
//            fail("Exception during 2nd test: " + e.toString());
//        }
//
//        try {
//            // jarName3 was created using the following test
//            File file = Support_Resources.copyFile(resources, null, jarName3);
//            JarFile jarFile = new JarFile(file);
//            assertNotNull("Should find manifest without verifying", jarFile
//                    .getManifest());
//            jarFile.close();
//        } catch (Exception e) {
//            fail("Exception during 3rd test: " + e.toString());
//        }
//
//        try {
//            // this is used to create jarName3 used in the previous test
//            Manifest manifest = new Manifest();
//            Attributes attributes = manifest.getMainAttributes();
//            attributes.put(new Attributes.Name("Manifest-Version"), "1.0");
//            ByteArrayOutputStream manOut = new ByteArrayOutputStream();
//            manifest.write(manOut);
//            byte[] manBytes = manOut.toByteArray();
//            File file = File.createTempFile("hyts_manifest1", ".jar");
//            JarOutputStream jarOut = new JarOutputStream(new FileOutputStream(
//                    file.getAbsolutePath()));
//            ZipEntry entry = new ZipEntry("META-INF/");
//            entry.setSize(0);
//            jarOut.putNextEntry(entry);
//            entry = new ZipEntry(JarFile.MANIFEST_NAME);
//            entry.setSize(manBytes.length);
//            jarOut.putNextEntry(entry);
//            jarOut.write(manBytes);
//            entry = new ZipEntry("myfile");
//            entry.setSize(1);
//            jarOut.putNextEntry(entry);
//            jarOut.write(65);
//            jarOut.close();
//            JarFile jar = new JarFile(file.getAbsolutePath(), false);
//            assertNotNull("Should find manifest without verifying", jar
//                    .getManifest());
//            jar.close();
//            file.delete();
//        } catch (IOException e) {
//            fail("IOException 3");
//        }
//        try {
//            File file = Support_Resources.copyFile(resources, null, jarName2);
//            JarFile jF = new JarFile(file);
//            jF.close();
//            jF.getManifest();
//            fail("FAILED: expected IllegalStateException");
//        } catch (IllegalStateException ise) {
//            // expected;
//        } catch (Exception e) {
//            fail("Exception during 4th test: " + e.toString());
//        }
//
//        File file = Support_Resources.copyFile(resources, null, "Broken_manifest.jar");
//        JarFile jf = null;
//        try {
//            jf = new JarFile(file);
//            jf.getManifest();
//            fail("IOException expected.");
//        } catch (IOException e) {
//            // expected.
//        } finally {
//            IoUtils.closeQuietly(jf);
//        }
//    }
//
//    /**
//     * java.util.jar.JarFile#getInputStream(java.util.zip.ZipEntry)
//     */
//    // This test doesn't pass on RI. If entry size is set up incorrectly,
//    // SecurityException is thrown. But SecurityException is thrown on RI only
//    // if jar file is signed incorrectly.
//    public void test_getInputStreamLjava_util_jar_JarEntry_subtest0() throws Exception {
//        File signedFile = null;
//        try {
//            signedFile = Support_Resources.copyFile(resources, null, jarName4);
//        } catch (Exception e) {
//            fail("Failed to create local file 2: " + e);
//        }
//
//        try (JarFile jar = new JarFile(signedFile)) {
//            JarEntry entry = new JarEntry(entryName3);
//            InputStream in = jar.getInputStream(entry);
//            in.read();
//        } catch (Exception e) {
//            fail("Exception during test 3: " + e);
//        }
//
//        try (JarFile jar = new JarFile(signedFile)) {
//            JarEntry entry = new JarEntry(entryName3);
//            InputStream in = jar.getInputStream(entry);
//            // BEGIN Android-added
//            byte[] dummy = getAllBytesFromStream(in);
//            // END Android-added
//            assertNull("found certificates", entry.getCertificates());
//        } catch (Exception e) {
//            fail("Exception during test 4: " + e);
//        }
//
//        try (JarFile jar = new JarFile(signedFile)) {
//            JarEntry entry = jar.getJarEntry(entryName3);
//            entry.setSize(1076);
//            InputStream in = jar.getInputStream(entry);
//            // BEGIN Android-added
//            byte[] dummy = getAllBytesFromStream(in);
//            // END Android-added
//            fail("SecurityException should be thrown.");
//        } catch (SecurityException e) {
//            // expected
//        } catch (Exception e) {
//            fail("Exception during test 5: " + e);
//        }
//
//        try {
//            signedFile = Support_Resources.copyFile(resources, null, jarName5);
//        } catch (Exception e) {
//            fail("Failed to create local file 5: " + e);
//        }
//
//        try (JarFile jar = new JarFile(signedFile)) {
//            JarEntry entry = new JarEntry(entryName3);
//            InputStream in = jar.getInputStream(entry);
//            fail("SecurityException should be thrown.");
//        } catch (SecurityException e) {
//            // expected
//        } catch (Exception e) {
//            fail("Exception during test 5: " + e);
//        }
//
//        // SHA1 digest, SHA256withRSA signed JAR
//        checkSignedJar(jarName6);
//
//        // SHA-256 digest, SHA256withRSA signed JAR
//        checkSignedJar(jarName7);
//
//        // SHA-512 digest, SHA512withECDSA signed JAR
//        checkSignedJar(jarName8);
//
//        // JAR with a signature that has PKCS#7 Authenticated Attributes
//        checkSignedJar(authAttrsJar);
//
//        // JAR with certificates that loop
//        checkSignedJar(certLoopJar, 3);
//    }
//
//    /**
//     * This test uses a jar file signed with an algorithm that has its own OID
//     * that is valid as a signature type. SHA256withECDSA is an algorithm that
//     * isn't combined as DigestAlgorithm + "with" + DigestEncryptionAlgorithm
//     * like RSAEncryption needs to be.
//     */
//    public void testJarFile_Signed_Valid_DigestEncryptionAlgorithm() throws Exception {
//        checkSignedJar(jarName9);
//    }
//
//    /**
//     * Checks that a JAR is signed correctly with a signature length of 1.
//     */
//    private void checkSignedJar(String jarName) throws Exception {
//        checkSignedJar(jarName, 1);
//    }
//
//    /**
//     * Checks that a JAR is signed correctly with a signature length of sigLength.
//     */
//    private void checkSignedJar(String jarName, final int sigLength) throws Exception {
//        File file = Support_Resources.copyFile(resources, null, jarName);
//        assertFirstSignedEntryCertificateLength(file, sigLength);
//    }
//
//    /**
//     * Opens the specified File as a verified JarFile and iterates through the entries, checking the
//     * certificates length is as expected for the first entry found that return a non-null /
//     * non-empty array from {@link JarEntry#getCertificates()}. Fails if no entry can be found with
//     * certificates.
//     */
//    private static void assertFirstSignedEntryCertificateLength(File file, int expectedCertsLength)
//            throws IOException {
//        try (JarFile jarFile = new JarFile(file, true)) {
//            Enumeration<JarEntry> e = jarFile.entries();
//            while (e.hasMoreElements()) {
//                JarEntry entry = e.nextElement();
//                InputStream is = jarFile.getInputStream(entry);
//                is.skip(100000);
//                is.close();
//                Certificate[] certs = entry.getCertificates();
//                if (certs != null && certs.length > 0) {
//                    assertEquals(expectedCertsLength, certs.length);
//                    return;
//                }
//            }
//            fail("No certificates found during signed jar test for jar \"" + file + "\"");
//        }
//    }
//
//    private static class Results {
//        public Certificate[] certificates;
//        public CodeSigner[] signers;
//    }
//
//    private Results getSignedJarCerts(String jarName) throws Exception {
//        File file = Support_Resources.copyFile(resources, null, jarName);
//        Results results = new Results();
//
//        JarFile jarFile = new JarFile(file, true, ZipFile.OPEN_READ);
//        try {
//
//            Enumeration<JarEntry> e = jarFile.entries();
//            while (e.hasMoreElements()) {
//                JarEntry entry = e.nextElement();
//                InputStream is = jarFile.getInputStream(entry);
//                // Skip bytes because we have to read the entire file for it to read signatures.
//                is.skip(entry.getSize());
//                is.close();
//                Certificate[] certs = entry.getCertificates();
//                CodeSigner[] signers = entry.getCodeSigners();
//                if (certs != null && certs.length > 0) {
//                    results.certificates = certs;
//                    results.signers = signers;
//                    break;
//                }
//            }
//        } finally {
//            jarFile.close();
//        }
//
//        return results;
//    }
//
//    public void testJarFile_Signed_ValidChain() throws Exception {
//        Results result = getSignedJarCerts(VALID_CHAIN_JAR);
//        assertNotNull(result);
//        assertEquals(Arrays.deepToString(result.certificates), 3, result.certificates.length);
//        assertEquals(Arrays.deepToString(result.signers), 1, result.signers.length);
//        assertEquals(3, result.signers[0].getSignerCertPath().getCertificates().size());
//        assertEquals("CN=fake-chain", ((X509Certificate) result.certificates[0]).getSubjectDN().toString());
//        assertEquals("CN=intermediate1", ((X509Certificate) result.certificates[1]).getSubjectDN().toString());
//        assertEquals("CN=root1", ((X509Certificate) result.certificates[2]).getSubjectDN().toString());
//    }
//
//    public void testJarFile_Signed_InvalidChain() throws Exception {
//        Results result = getSignedJarCerts(INVALID_CHAIN_JAR);
//        assertNotNull(result);
//        assertEquals(Arrays.deepToString(result.certificates), 3, result.certificates.length);
//        assertEquals(Arrays.deepToString(result.signers), 1, result.signers.length);
//        assertEquals(3, result.signers[0].getSignerCertPath().getCertificates().size());
//        assertEquals("CN=fake-chain", ((X509Certificate) result.certificates[0]).getSubjectDN().toString());
//        assertEquals("CN=intermediate1", ((X509Certificate) result.certificates[1]).getSubjectDN().toString());
//        assertEquals("CN=root1", ((X509Certificate) result.certificates[2]).getSubjectDN().toString());
//    }
//
//    public void testJarFile_Signed_AmbiguousSigners() throws Exception {
//        Results result = getSignedJarCerts(AMBIGUOUS_SIGNERS_JAR);
//        assertNotNull(result);
//        assertEquals(Arrays.deepToString(result.certificates), 2, result.certificates.length);
//        assertEquals(Arrays.deepToString(result.signers), 2, result.signers.length);
//        assertEquals(1, result.signers[0].getSignerCertPath().getCertificates().size());
//        assertEquals(1, result.signers[1].getSignerCertPath().getCertificates().size());
//    }
//
//    /*
//     * The jar created by 1.4 which does not provide a
//     * algorithm-Digest-Manifest-Main-Attributes entry in .SF file.
//     */
//    public void test_Jar_created_before_java_5() throws IOException {
//        String modifiedJarName = "Created_by_1_4.jar";
//        File file = Support_Resources.copyFile(resources, null, modifiedJarName);
//        try (JarFile jarFile = new JarFile(file, true)) {
//            Enumeration<JarEntry> entries = jarFile.entries();
//            while (entries.hasMoreElements()) {
//                ZipEntry zipEntry = entries.nextElement();
//                jarFile.getInputStream(zipEntry);
//            }
//        }
//    }
//
//    /* The jar is intact, then everything is all right. */
//    public void test_JarFile_Integrate_Jar() throws IOException {
//        String modifiedJarName = "Integrate.jar";
//        File file = Support_Resources.copyFile(resources, null, modifiedJarName);
//        try (JarFile jarFile = new JarFile(file, true)) {
//            Enumeration<JarEntry> entries = jarFile.entries();
//            while (entries.hasMoreElements()) {
//                ZipEntry zipEntry = entries.nextElement();
//                jarFile.getInputStream(zipEntry).skip(Long.MAX_VALUE);
//            }
//        }
//    }
//
//    /**
//     * The jar is intact, but the entry object is modified.
//     */
//    public void testJarVerificationModifiedEntry() throws IOException {
//        File f = Support_Resources.copyFile(resources, null, integrateJar);
//
//        try (JarFile jarFile = new JarFile(f)) {
//            ZipEntry zipEntry = jarFile.getJarEntry(integrateJarEntry);
//            zipEntry.setSize(zipEntry.getSize() + 1);
//            jarFile.getInputStream(zipEntry).skip(Long.MAX_VALUE);
//        }
//
//        try (JarFile jarFile = new JarFile(f)) {
//            ZipEntry zipEntry = jarFile.getJarEntry(integrateJarEntry);
//            zipEntry.setSize(zipEntry.getSize() - 1);
//            try {
//                //jarFile.getInputStream(zipEntry).skip(Long.MAX_VALUE);
//                jarFile.getInputStream(zipEntry).read(new byte[5000], 0, 5000);
//                fail("SecurityException expected");
//            } catch (SecurityException e) {
//                // desired
//            }
//        }
//    }
//
//    /*
//     * If another entry is inserted into Manifest, no security exception will be
//     * thrown out.
//     */
//    public void test_JarFile_InsertEntry_in_Manifest_Jar() throws IOException {
//        String modifiedJarName = "Inserted_Entry_Manifest.jar";
//        File file = Support_Resources.copyFile(resources, null, modifiedJarName);
//        try (JarFile jarFile = new JarFile(file, true)) {
//            Enumeration<JarEntry> entries = jarFile.entries();
//            int count = 0;
//            while (entries.hasMoreElements()) {
//
//                ZipEntry zipEntry = entries.nextElement();
//                jarFile.getInputStream(zipEntry);
//                count++;
//            }
//            assertEquals(5, count);
//        }
//    }
//
//    /*
//     * If another entry is inserted into Manifest, no security exception will be
//     * thrown out.
//     */
//    public void test_Inserted_Entry_Manifest_with_DigestCode()
//            throws IOException {
//        String modifiedJarName = "Inserted_Entry_Manifest_with_DigestCode.jar";
//        File file = Support_Resources.copyFile(resources, null, modifiedJarName);
//        try (JarFile jarFile = new JarFile(file, true)) {
//            Enumeration<JarEntry> entries = jarFile.entries();
//            int count = 0;
//            while (entries.hasMoreElements()) {
//                ZipEntry zipEntry = entries.nextElement();
//                jarFile.getInputStream(zipEntry);
//                count++;
//            }
//            assertEquals(5, count);
//        }
//    }
//
//    /*
//     * The content of Test.class is modified, jarFile.getInputStream will not
//     * throw security Exception, but it will anytime before the inputStream got
//     * from getInputStream method has been read to end.
//     */
//    public void test_JarFile_Modified_Class() throws IOException {
//        String modifiedJarName = "Modified_Class.jar";
//        File file = Support_Resources.copyFile(resources, null, modifiedJarName);
//        try (JarFile jarFile = new JarFile(file, true)) {
//            Enumeration<JarEntry> entries = jarFile.entries();
//            while (entries.hasMoreElements()) {
//                ZipEntry zipEntry = entries.nextElement();
//                jarFile.getInputStream(zipEntry);
//            }
//            /* The content of Test.class has been tampered. */
//            ZipEntry zipEntry = jarFile.getEntry("Test.class");
//            InputStream in = jarFile.getInputStream(zipEntry);
//            byte[] buffer = new byte[1024];
//            try {
//                while (in.available() > 0) {
//                    in.read(buffer);
//                }
//                fail("SecurityException expected");
//            } catch (SecurityException e) {
//                // desired
//            }
//        }
//    }
//
//    /*
//     * In the Modified.jar, the main attributes of META-INF/MANIFEST.MF is
//     * tampered manually. Hence the RI 5.0 JarFile.getInputStream of any
//     * JarEntry will throw security exception.
//     */
//    public void test_JarFile_Modified_Manifest_MainAttributes()
//            throws IOException {
//        String modifiedJarName = "Modified_Manifest_MainAttributes.jar";
//        File file = Support_Resources.copyFile(resources, null, modifiedJarName);
//        try (JarFile jarFile = new JarFile(file, true)) {
//            Enumeration<JarEntry> entries = jarFile.entries();
//            while (entries.hasMoreElements()) {
//                ZipEntry zipEntry = entries.nextElement();
//                try {
//                    jarFile.getInputStream(zipEntry);
//                    fail("SecurityException expected");
//                } catch (SecurityException e) {
//                    // desired
//                }
//            }
//        }
//    }
//
//    /*
//     * It is all right in our original JarFile. If the Entry Attributes, for
//     * example Test.class in our jar, the jarFile.getInputStream will throw
//     * Security Exception.
//     */
//    public void test_JarFile_Modified_Manifest_EntryAttributes()
//            throws IOException {
//        String modifiedJarName = "Modified_Manifest_EntryAttributes.jar";
//        File file = Support_Resources.copyFile(resources, null, modifiedJarName);
//        try (JarFile jarFile = new JarFile(file, true)) {
//            Enumeration<JarEntry> entries = jarFile.entries();
//            while (entries.hasMoreElements()) {
//                ZipEntry zipEntry = entries.nextElement();
//                try {
//                    jarFile.getInputStream(zipEntry);
//                    fail("should throw Security Exception");
//                } catch (SecurityException e) {
//                    // desired
//                }
//            }
//        }
//    }
//
//    /*
//     * If the content of the .SA file is modified, no matter what it resides,
//     * JarFile.getInputStream of any JarEntry will throw Security Exception.
//     */
//    public void test_JarFile_Modified_SF_EntryAttributes() throws IOException {
//        String modifiedJarName = "Modified_SF_EntryAttributes.jar";
//        File file = Support_Resources.copyFile(resources, null, modifiedJarName);
//        try (JarFile jarFile = new JarFile(file, true)) {
//            Enumeration<JarEntry> entries = jarFile.entries();
//            while (entries.hasMoreElements()) {
//                ZipEntry zipEntry = entries.nextElement();
//                try {
//                    jarFile.getInputStream(zipEntry);
//                    fail("should throw Security Exception");
//                } catch (SecurityException e) {
//                    // desired
//                }
//            }
//        }
//    }
//
//    public void test_close() throws IOException {
//        String modifiedJarName = "Modified_SF_EntryAttributes.jar";
//        File file = Support_Resources.copyFile(resources, null, modifiedJarName);
//        JarFile jarFile = new JarFile(file, true);
//        Enumeration<JarEntry> entries = jarFile.entries();
//
//        jarFile.close();
//        jarFile.close();
//
//        // Can not check IOException
//    }
//
//    /**
//     * @throws IOException
//     * java.util.jar.JarFile#getInputStream(java.util.zip.ZipEntry)
//     */
//    public void test_getInputStreamLjava_util_jar_JarEntry() throws IOException {
//        File localFile = null;
//        try {
//            localFile = Support_Resources.copyFile(resources, null, jarName);
//        } catch (Exception e) {
//            fail("Failed to create local file: " + e);
//        }
//
//        byte[] b = new byte[1024];
//        try (JarFile jf = new JarFile(localFile)) {
//            java.io.InputStream is = jf.getInputStream(jf.getEntry(entryName));
//            assertTrue("Returned invalid stream", is.available() > 0);
//            int r = is.read(b, 0, 1024);
//            is.close();
//            StringBuffer sb = new StringBuffer(r);
//            for (int i = 0; i < r; i++) {
//                sb.append((char) (b[i] & 0xff));
//            }
//            String contents = sb.toString();
//            assertTrue("Incorrect stream read", contents.indexOf("bar") > 0);
//        } catch (Exception e) {
//            fail("Exception during test: " + e.toString());
//        }
//
//        try (JarFile jf = new JarFile(localFile)) {
//            InputStream in = jf.getInputStream(new JarEntry("invalid"));
//            assertNull("Got stream for non-existent entry", in);
//        } catch (Exception e) {
//            fail("Exception during test 2: " + e);
//        }
//
//        {
//            File signedFile = Support_Resources.copyFile(resources, null, jarName);
//            try (JarFile jf = new JarFile(signedFile)) {
//                JarEntry jre = new JarEntry("foo/bar/A.class");
//                jf.getInputStream(jre);
//                // InputStream returned in any way, exception can be thrown in case
//                // of reading from this stream only.
//                // fail("Should throw ZipException");
//            } catch (ZipException ee) {
//                // expected
//            }
//        }
//
//        {
//            File signedFile = Support_Resources.copyFile(resources, null, jarName);
//            try {
//                JarFile jf = new JarFile(signedFile);
//                JarEntry jre = new JarEntry("foo/bar/A.class");
//                jf.close();
//                jf.getInputStream(jre);
//                // InputStream returned in any way, exception can be thrown in case
//                // of reading from this stream only.
//                // The same for IOException
//                fail("Should throw IllegalStateException");
//            } catch (IllegalStateException ee) {
//                // expected
//            }
//        }
//    }
//
//    /**
//     * The jar is intact, but the entry object is modified.
//     */
//    // Regression test for issue introduced by HARMONY-4569: signed archives containing files with size 0 could not get verified.
//    public void testJarVerificationEmptyEntry() throws IOException {
//        File f = Support_Resources.copyFile(resources, null, emptyEntryJar);
//
//        try (JarFile jarFile = new JarFile(f)) {
//
//            ZipEntry zipEntry = jarFile.getJarEntry(emptyEntry1);
//            int res = jarFile.getInputStream(zipEntry).read(new byte[100], 0, 100);
//            assertEquals("Wrong length of empty jar entry", -1, res);
//
//            zipEntry = jarFile.getJarEntry(emptyEntry2);
//            res = jarFile.getInputStream(zipEntry).read(new byte[100], 0, 100);
//            assertEquals("Wrong length of empty jar entry", -1, res);
//
//            zipEntry = jarFile.getJarEntry(emptyEntry3);
//            res = jarFile.getInputStream(zipEntry).read();
//            assertEquals("Wrong length of empty jar entry", -1, res);
//        }
//    }
//
//    public void testJarFile_BadSignatureProvider_Success() throws Exception {
//        Security.insertProviderAt(new JarFileBadProvider(), 1);
//        try {
//            // Needs a JAR with "RSA" as digest encryption algorithm
//            checkSignedJar(jarName6);
//        } finally {
//            Security.removeProvider(JarFileBadProvider.NAME);
//        }
//    }
//
//    public static class JarFileBadProvider extends Provider {
//        public static final String NAME = "JarFileBadProvider";
//
//        public JarFileBadProvider() {
//            super(NAME, 1.0, "Bad provider for JarFileTest");
//
//            put("Signature.RSA", NotReallyASignature.class.getName());
//        }
//
//        /**
//         * This should never be instantiated, so everything throws an exception.
//         */
//        public static class NotReallyASignature extends SignatureSpi {
//            @Override
//            protected void engineInitVerify(PublicKey publicKey) throws InvalidKeyException {
//                fail("Should not call this provider");
//            }
//
//            @Override
//            protected void engineInitSign(PrivateKey privateKey) throws InvalidKeyException {
//                fail("Should not call this provider");
//            }
//
//            @Override
//            protected void engineUpdate(byte b) throws SignatureException {
//                fail("Should not call this provider");
//            }
//
//            @Override
//            protected void engineUpdate(byte[] b, int off, int len) throws SignatureException {
//                fail("Should not call this provider");
//            }
//
//            @Override
//            protected byte[] engineSign() throws SignatureException {
//                fail("Should not call this provider");
//                return null;
//            }
//
//            @Override
//            protected boolean engineVerify(byte[] sigBytes) throws SignatureException {
//                fail("Should not call this provider");
//                return false;
//            }
//
//            @Override
//            protected void engineSetParameter(String param, Object value)
//                    throws InvalidParameterException {
//                fail("Should not call this provider");
//            }
//
//            @Override
//            protected Object engineGetParameter(String param) throws InvalidParameterException {
//                fail("Should not call this provider");
//                return null;
//            }
//        }
//    }
//
//    /**
//     * java.util.jar.JarFile#stream()
//     */
//    public void test_stream() throws Exception {
//        /*
//         * Note only (and all of) the following should be contained in the file
//         * META-INF/ META-INF/MANIFEST.MF Blah.txt  foo/ foo/bar/ foo/bar/A.class
//         */
//        File file = Support_Resources.copyFile(resources, null, jarName);
//        JarFile jarFile = new JarFile(file);
//
//        final List<String> names = new ArrayList<>();
//        jarFile.stream().forEach((ZipEntry entry) -> names.add(entry.getName()));
//        assertEquals(Arrays.asList("META-INF/", "META-INF/MANIFEST.MF", "Blah.txt", "foo/", "foo/bar/",
//                                   "foo/bar/A.class"), names);
//        jarFile.close();
//    }
//
//
//    /**
//     * hyts_metainf.jar contains an additional entry in META-INF (META-INF/bad_checksum.txt),
//     * that has been altered since jar signing - we expect to detect a mismatching digest.
//     */
//    public void test_metainf_verification() throws Exception {
//        String jarFilename = "hyts_metainf.jar";
//        File file = Support_Resources.copyFile(resources, null, jarFilename);
//        try (JarFile jarFile = new JarFile(file)) {
//
//            JarEntry jre = new JarEntry("META-INF/bad_checksum.txt");
//            InputStream in = jarFile.getInputStream(jre);
//
//            byte[] buffer = new byte[1024];
//            try {
//                while (in.available() > 0) {
//                    in.read(buffer);
//                }
//                fail("SecurityException expected");
//            } catch (SecurityException expected) {}
//        }
//    }
}
