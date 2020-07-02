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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import junit.framework.TestCase;

import tests.support.resource.Support_Resources;

public class ManifestTest extends TestCase {

    private final String JAR_NAME = "hyts_patch.jar";

    private final String ATT_JAR_NAME = "hyts_att.jar";

    private final String ATT_ENTRY_NAME = "HasAttributes.txt";

    private final String ATT_ATT_NAME = "MyAttribute";

    private final String MANIFEST_NAME = "manifest/hyts_MANIFEST.MF";

    private static final String MANIFEST_CONTENTS = "Manifest-Version: 1.0\nBundle-Name: ClientSupport\nBundle-Description: Provides SessionService, AuthenticationService. Extends RegistryService.\nBundle-Activator: com.ibm.ive.eccomm.client.support.ClientSupportActivator\nImport-Package: com.ibm.ive.eccomm.client.services.log,\n com.ibm.ive.eccomm.client.services.registry,\n com.ibm.ive.eccomm.service.registry; specification-version=1.0.0,\n com.ibm.ive.eccomm.service.session; specification-version=1.0.0,\n com.ibm.ive.eccomm.service.framework; specification-version=1.2.0,\n org.osgi.framework; specification-version=1.0.0,\n org.osgi.service.log; specification-version=1.0.0,\n com.ibm.ive.eccomm.flash; specification-version=1.2.0,\n com.ibm.ive.eccomm.client.xml,\n com.ibm.ive.eccomm.client.http.common,\n com.ibm.ive.eccomm.client.http.client\nImport-Service: org.osgi.service.log.LogReaderService\n org.osgi.service.log.LogService,\n com.ibm.ive.eccomm.service.registry.RegistryService\nExport-Package: com.ibm.ive.eccomm.client.services.authentication; specification-version=1.0.0,\n com.ibm.ive.eccomm.service.authentication; specification-version=1.0.0,\n com.ibm.ive.eccomm.common; specification-version=1.0.0,\n com.ibm.ive.eccomm.client.services.registry.store; specification-version=1.0.0\nExport-Service: com.ibm.ive.eccomm.service.authentication.AuthenticationService,\n com.ibm.ive.eccomm.service.session.SessionService\nBundle-Vendor: IBM\nBundle-Version: 1.2.0\n";

    private static final String MANIFEST_CONTENTS_1 = "Manifest-Version: 2.0\nBundle-Name: ClientSupport\nBundle-Description: Provides SessionService, AuthenticationService. Extends RegistryService.\nBundle-Activator: com.ibm.ive.eccomm.client.support.ClientSupportActivator\nImport-Package: com.ibm.ive.eccomm.client.services.log,\n com.ibm.ive.eccomm.client.services.registry,\n com.ibm.ive.eccomm.service.registry; specification-version=2.0.0,\n com.ibm.ive.eccomm.service.session; specification-version=2.0.0,\n com.ibm.ive.eccomm.service.framework; specification-version=2.1.0,\n org.osgi.framework; specification-version=2.0.0,\n org.osgi.service.log; specification-version=2.0.0,\n com.ibm.ive.eccomm.flash; specification-version=2.2.0,\n com.ibm.ive.eccomm.client.xml,\n com.ibm.ive.eccomm.client.http.common,\n com.ibm.ive.eccomm.client.http.client\nImport-Service: org.osgi.service.log.LogReaderService\n org.osgi.service.log.LogService,\n com.ibm.ive.eccomm.service.registry.RegistryService\nExport-Package: com.ibm.ive.eccomm.client.services.authentication; specification-version=1.0.0,\n com.ibm.ive.eccomm.service.authentication; specification-version=1.0.0,\n com.ibm.ive.eccomm.common; specification-version=1.0.0,\n com.ibm.ive.eccomm.client.services.registry.store; specification-version=1.0.0\nExport-Service: com.ibm.ive.eccomm.service.authentication.AuthenticationService,\n com.ibm.ive.eccomm.service.session.SessionService\nBundle-Vendor: IBM\nBundle-Version: 1.2.0\n";

    private static final String MANIFEST_CONTENTS_2 = "Manifest-Version: 1.0\nName: value\n \n"; // Note penultimate line is single space

    private File resources;

    @Override
    protected void setUp() {
        resources = Support_Resources.createTempFolder();
    }

    private Manifest getManifest(String fileName) {
        try {
            Support_Resources.copyFile(resources, null, fileName);
            JarFile jarFile = new JarFile(new File(resources, fileName));
            Manifest m = jarFile.getManifest();
            jarFile.close();
            return m;
        } catch (Exception e) {
            fail("Exception during setup: " + e.toString());
            return null;
        }
    }

    /**
     * java.util.jar.Manifest#Manifest()
     */
    public void testConstructor() {
        // Test for method java.util.jar.Manifest()
        Manifest emptyManifest = new Manifest();
        assertTrue("Should have no entries", emptyManifest.getEntries()
                .isEmpty());
        assertTrue("Should have no main attributes", emptyManifest
                .getMainAttributes().isEmpty());
    }

    /**
     * java.util.jar.Manifest#Manifest(java.util.jar.Manifest)
     */
    public void testCopyingConstructor() throws IOException {
        Manifest firstManifest = new Manifest(new ByteArrayInputStream(
                MANIFEST_CONTENTS.getBytes("ISO-8859-1")));
        Manifest secondManifest = new Manifest(firstManifest);
        assertEquals(firstManifest, secondManifest);
    }

    private void assertAttribute(Attributes attr, String name, String value) {
        assertEquals("Incorrect " + name, value, attr.getValue(name));
    }

    private void checkManifest(Manifest manifest) {
        Attributes main = manifest.getMainAttributes();
        assertAttribute(main, "Bundle-Name", "ClientSupport");
        assertAttribute(main, "Bundle-Description",
                "Provides SessionService, AuthenticationService. Extends RegistryService.");
        assertAttribute(main, "Bundle-Activator",
                "com.ibm.ive.eccomm.client.support.ClientSupportActivator");
        assertAttribute(
                main,
                "Import-Package",
                "com.ibm.ive.eccomm.client.services.log,com.ibm.ive.eccomm.client.services.registry,com.ibm.ive.eccomm.service.registry; specification-version=1.0.0,com.ibm.ive.eccomm.service.session; specification-version=1.0.0,com.ibm.ive.eccomm.service.framework; specification-version=1.2.0,org.osgi.framework; specification-version=1.0.0,org.osgi.service.log; specification-version=1.0.0,com.ibm.ive.eccomm.flash; specification-version=1.2.0,com.ibm.ive.eccomm.client.xml,com.ibm.ive.eccomm.client.http.common,com.ibm.ive.eccomm.client.http.client");
        assertAttribute(
                main,
                "Import-Service",
                "org.osgi.service.log.LogReaderServiceorg.osgi.service.log.LogService,com.ibm.ive.eccomm.service.registry.RegistryService");
        assertAttribute(
                main,
                "Export-Package",
                "com.ibm.ive.eccomm.client.services.authentication; specification-version=1.0.0,com.ibm.ive.eccomm.service.authentication; specification-version=1.0.0,com.ibm.ive.eccomm.common; specification-version=1.0.0,com.ibm.ive.eccomm.client.services.registry.store; specification-version=1.0.0");
        assertAttribute(
                main,
                "Export-Service",
                "com.ibm.ive.eccomm.service.authentication.AuthenticationService,com.ibm.ive.eccomm.service.session.SessionService");
        assertAttribute(main, "Bundle-Vendor", "IBM");
        assertAttribute(main, "Bundle-Version", "1.2.0");
    }

    /**
     * java.util.jar.Manifest#Manifest(java.io.InputStream)
     */
    public void testStreamConstructor() throws IOException {
        Manifest m = getManifest(ATT_JAR_NAME);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        m.write(baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        Manifest mCopy = new Manifest(is);
        assertEquals(m, mCopy);

        Manifest manifest = new Manifest(new ByteArrayInputStream(
                MANIFEST_CONTENTS.getBytes("ISO-8859-1")));
        checkManifest(manifest);

        // regression test for HARMONY-5424
        String manifestContent = "Manifest-Version: 1.0\nCreated-By: Apache\nPackage: \nBuild-Jdk: 1.4.1_01\n\n"
                + "Name: \nSpecification-Title: foo\nSpecification-Version: 1.0\nSpecification-Vendor: \n"
                + "Implementation-Title: \nImplementation-Version: 1.0\nImplementation-Vendor: \n\n";
        ByteArrayInputStream bis = new ByteArrayInputStream(manifestContent
                .getBytes("ISO-8859-1"));


        Manifest mf = new Manifest(bis);
        assertEquals("Should be 4 main attributes", 4, mf.getMainAttributes()
                .size());

        Map<String, Attributes> entries = mf.getEntries();
        assertEquals("Should be one named entry", 1, entries.size());

        Attributes namedEntryAttributes = (Attributes) (entries.get(""));
        assertEquals("Should be 6 named entry attributes", 6,
                namedEntryAttributes.size());

        // Regression test for HARMONY-6669
        new Manifest(new ByteArrayInputStream(
                MANIFEST_CONTENTS_2.getBytes("ISO-8859-1")));
    }

    /**
     * java.util.jar.Manifest#clear()
     */
    public void testClear() {
        Manifest m = getManifest(ATT_JAR_NAME);
        m.clear();
        assertTrue("Should have no entries", m.getEntries().isEmpty());
        assertTrue("Should have no main attributes", m.getMainAttributes()
                .isEmpty());
    }

    /**
     * java.util.jar.Manifest#clone()
     */
    public void testClone() {
        Manifest m = getManifest(JAR_NAME);
        assertEquals(m, m.clone());
    }

    /**
     * java.util.jar.Manifest#equals(java.lang.Object)
     */
    public void testEquals() throws IOException {
        Manifest firstManifest = new Manifest(new ByteArrayInputStream(
                MANIFEST_CONTENTS.getBytes("ISO-8859-1")));
        Manifest secondManifest = new Manifest(new ByteArrayInputStream(
                MANIFEST_CONTENTS.getBytes("ISO-8859-1")));

        assertEquals(firstManifest, secondManifest);

        Manifest thirdManifest = new Manifest(new ByteArrayInputStream(
                MANIFEST_CONTENTS_1.getBytes("ISO-8859-1")));
        assertNotSame(firstManifest, thirdManifest);

        firstManifest = null;
        assertFalse(secondManifest.equals(firstManifest));
        assertFalse(secondManifest.equals(new String("abc"))); //non Manifest Object
    }

    /**
     * java.util.jar.Manifest#hashCode()
     */
    public void testHashCode() {
        Manifest m = getManifest(JAR_NAME);
        assertEquals(m.hashCode(), m.clone().hashCode());
    }

    /**
     * java.util.jar.Manifest#getAttributes(java.lang.String)
     */
    public void testGetAttributes() {
        Manifest m = getManifest(ATT_JAR_NAME);
        assertNull("Should not exist", m.getAttributes("Doesn't Exist"));
        assertEquals("Should exist", "OK", m.getAttributes(ATT_ENTRY_NAME).get(
                new Attributes.Name(ATT_ATT_NAME)));
    }

    /**
     * java.util.jar.Manifest#getEntries()
     */
    public void testGetEntries() {
        Manifest m = getManifest(ATT_JAR_NAME);
        Map<String, Attributes> myMap = m.getEntries();
        assertNull("Shouldn't exist", myMap.get("Doesn't exist"));
        assertEquals("Should exist", "OK", myMap.get(ATT_ENTRY_NAME).get(
                new Attributes.Name(ATT_ATT_NAME)));
    }

    /**
     * java.util.jar.Manifest#getMainAttributes()
     */
    public void testGetMainAttributes() {
        Manifest m = getManifest(JAR_NAME);
        Attributes a = m.getMainAttributes();
        assertEquals("Manifest_Version should return 1.0", "1.0", a
                .get(Attributes.Name.MANIFEST_VERSION));
    }

    /**
     * {@link java.util.jar.Manifest#write(java.io.OutputStream)
     */
    public void testWrite() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Manifest m = getManifest(JAR_NAME);
        // maximum allowed length is 72 for a header, colon and a following
        // space
        StringBuffer headerName = new StringBuffer(71);
        headerName.append("Manifest-");
        while (headerName.length() < 70) {
            headerName.append("0");
        }
        m.getMainAttributes().put(new Attributes.Name(headerName.toString()),
                "Value");
        m.write(baos); // ok
    }

    /**
     * Ensures compatibility with manifests produced by gcc.
     *
     * @see <a
     *      href="http://issues.apache.org/jira/browse/HARMONY-5662">HARMONY-5662</a>
     */
    public void testNul() throws IOException {
        String manifestContent =
                "Manifest-Version: 1.0\nCreated-By: nasty gcc tool\n\n\0";

        byte[] bytes = manifestContent.getBytes("ISO-8859-1");
        new Manifest(new ByteArrayInputStream(bytes)); // the last NUL is ok

        bytes[bytes.length - 1] = 26;
        new Manifest(new ByteArrayInputStream(bytes)); // the last EOF is ok

        bytes[bytes.length - 1] = 'A'; // the last line ignored
        new Manifest(new ByteArrayInputStream(bytes));

        bytes[2] = 0; // NUL char in Manifest
        try {
            new Manifest(new ByteArrayInputStream(bytes));
            fail("IOException expected");
        } catch (IOException e) {
            // desired
        }
    }

    public void testDecoding() throws IOException {
        Manifest m = getManifest(ATT_JAR_NAME);
        final byte[] bVendor = new byte[] { (byte) 0xd0, (byte) 0x9C,
                (byte) 0xd0, (byte) 0xb8, (byte) 0xd0, (byte) 0xbb,
                (byte) 0xd0, (byte) 0xb0, (byte) 0xd1, (byte) 0x8f, ' ',
                (byte) 0xd0, (byte) 0xb4, (byte) 0xd0, (byte) 0xbe,
                (byte) 0xd1, (byte) 0x87, (byte) 0xd1, (byte) 0x83,
                (byte) 0xd0, (byte) 0xbd, (byte) 0xd1, (byte) 0x8C,
                (byte) 0xd0, (byte) 0xba, (byte) 0xd0, (byte) 0xb0, ' ',
                (byte) 0xd0, (byte) 0x9C, (byte) 0xd0, (byte) 0xb0,
                (byte) 0xd1, (byte) 0x88, (byte) 0xd0, (byte) 0xb0 };

        final byte[] bSpec = new byte[] { (byte) 0xe1, (byte) 0x88,
                (byte) 0xb0, (byte) 0xe1, (byte) 0x88, (byte) 0x8b,
                (byte) 0xe1, (byte) 0x88, (byte) 0x9d, ' ', (byte) 0xe1,
                (byte) 0x9a, (byte) 0xa0, (byte) 0xe1, (byte) 0x9a,
                (byte) 0xb1, (byte) 0xe1, (byte) 0x9b, (byte) 0x81,
                (byte) 0xe1, (byte) 0x9a, (byte) 0xa6, ' ', (byte) 0xd8,
                (byte) 0xb3, (byte) 0xd9, (byte) 0x84, (byte) 0xd8,
                (byte) 0xa7, (byte) 0xd9, (byte) 0x85, ' ', (byte) 0xd8,
                (byte) 0xb9, (byte) 0xd8, (byte) 0xb3, (byte) 0xd9,
                (byte) 0x84, (byte) 0xd8, (byte) 0xa7, (byte) 0xd9,
                (byte) 0x85, (byte) 0xd8, (byte) 0xa9, ' ', (byte) 0xdc,
                (byte) 0xab, (byte) 0xdc, (byte) 0xa0, (byte) 0xdc,
                (byte) 0xa1, (byte) 0xdc, (byte) 0x90, ' ', (byte) 0xe0,
                (byte) 0xa6, (byte) 0xb6, (byte) 0xe0, (byte) 0xa6,
                (byte) 0xbe, (byte) 0xe0, (byte) 0xa6, (byte) 0xa8,
                (byte) 0xe0, (byte) 0xa7, (byte) 0x8d, (byte) 0xe0,
                (byte) 0xa6, (byte) 0xa4, (byte) 0xe0, (byte) 0xa6,
                (byte) 0xbf, ' ', (byte) 0xd0, (byte) 0xa0, (byte) 0xd0,
                (byte) 0xb5, (byte) 0xd0, (byte) 0xba, (byte) 0xd1,
                (byte) 0x8a, (byte) 0xd0, (byte) 0xb5, (byte) 0xd0,
                (byte) 0xbb, ' ', (byte) 0xd0, (byte) 0x9c, (byte) 0xd0,
                (byte) 0xb8, (byte) 0xd1, (byte) 0x80, ' ', (byte) 0xe0,
                (byte) 0xa6, (byte) 0xb6, (byte) 0xe0, (byte) 0xa6,
                (byte) 0xbe, (byte) 0xe0, (byte) 0xa6, (byte) 0xa8,
                (byte) 0xe0, (byte) 0xa7, (byte) 0x8d, (byte) 0xe0,
                (byte) 0xa6, (byte) 0xa4, (byte) 0xe0, (byte) 0xa6,
                (byte) 0xbf, ' ', (byte) 0xe0, (byte) 0xbd, (byte) 0x9e,
                (byte) 0xe0, (byte) 0xbd, (byte) 0xb2, (byte) 0xe0,
                (byte) 0xbc, (byte) 0x8b, (byte) 0xe0, (byte) 0xbd,
                (byte) 0x96, (byte) 0xe0, (byte) 0xbd, (byte) 0x91,
                (byte) 0xe0, (byte) 0xbd, (byte) 0xba, ' ', (byte) 0xd0,
                (byte) 0x9c, (byte) 0xd0, (byte) 0xb0, (byte) 0xd1,
                (byte) 0x88, (byte) 0xd0, (byte) 0xb0, (byte) 0xd1,
                (byte) 0x80, ' ', (byte) 0xe1, (byte) 0x8f, (byte) 0x99,
                (byte) 0xe1, (byte) 0x8e, (byte) 0xaf, (byte) 0xe1,
                (byte) 0x8f, (byte) 0xb1, ' ', (byte) 0xcf, (byte) 0xa8,
                (byte) 0xce, (byte) 0xb9, (byte) 0xcf, (byte) 0x81,
                (byte) 0xce, (byte) 0xb7, (byte) 0xce, (byte) 0xbd,
                (byte) 0xce, (byte) 0xb7, ' ', (byte) 0xde, (byte) 0x90,
                (byte) 0xde, (byte) 0xaa, (byte) 0xde, (byte) 0x85,
                (byte) 0xde, (byte) 0xa6, ' ', (byte) 0xe0, (byte) 0xbd,
                (byte) 0x82, (byte) 0xe0, (byte) 0xbd, (byte) 0x9e,
                (byte) 0xe0, (byte) 0xbd, (byte) 0xb2, (byte) 0xe0,
                (byte) 0xbc, (byte) 0x8b, (byte) 0xe0, (byte) 0xbd,
                (byte) 0x96, (byte) 0xe0, (byte) 0xbd, (byte) 0x91,
                (byte) 0xe0, (byte) 0xbd, (byte) 0xba, ' ', (byte) 0xce,
                (byte) 0x95, (byte) 0xce, (byte) 0xb9, (byte) 0xcf,
                (byte) 0x81, (byte) 0xce, (byte) 0xae, (byte) 0xce,
                (byte) 0xbd, (byte) 0xce, (byte) 0xb7, ' ', (byte) 0xd8,
                (byte) 0xb5, (byte) 0xd9, (byte) 0x84, (byte) 0xd8,
                (byte) 0xad, ' ', (byte) 0xe0, (byte) 0xaa, (byte) 0xb6,
                (byte) 0xe0, (byte) 0xaa, (byte) 0xbe, (byte) 0xe0,
                (byte) 0xaa, (byte) 0x82, (byte) 0xe0, (byte) 0xaa,
                (byte) 0xa4, (byte) 0xe0, (byte) 0xaa, (byte) 0xbf, ' ',
                (byte) 0xe5, (byte) 0xb9, (byte) 0xb3, (byte) 0xe5,
                (byte) 0x92, (byte) 0x8c, ' ', (byte) 0xd7, (byte) 0xa9,
                (byte) 0xd7, (byte) 0x9c, (byte) 0xd7, (byte) 0x95,
                (byte) 0xd7, (byte) 0x9d, ' ', (byte) 0xd7, (byte) 0xa4,
                (byte) 0xd7, (byte) 0xa8, (byte) 0xd7, (byte) 0x99,
                (byte) 0xd7, (byte) 0x93, (byte) 0xd7, (byte) 0x9f, ' ',
                (byte) 0xe5, (byte) 0x92, (byte) 0x8c, (byte) 0xe5,
                (byte) 0xb9, (byte) 0xb3, ' ', (byte) 0xe5, (byte) 0x92,
                (byte) 0x8c, (byte) 0xe5, (byte) 0xb9, (byte) 0xb3, ' ',
                (byte) 0xd8, (byte) 0xaa, (byte) 0xd9, (byte) 0x89,
                (byte) 0xd9, (byte) 0x86, (byte) 0xda, (byte) 0x86,
                (byte) 0xd9, (byte) 0x84, (byte) 0xd9, (byte) 0x89,
                (byte) 0xd9, (byte) 0x82, ' ', (byte) 0xe0, (byte) 0xae,
                (byte) 0x85, (byte) 0xe0, (byte) 0xae, (byte) 0xae,
                (byte) 0xe0, (byte) 0xaf, (byte) 0x88, (byte) 0xe0,
                (byte) 0xae, (byte) 0xa4, (byte) 0xe0, (byte) 0xae,
                (byte) 0xbf, ' ', (byte) 0xe0, (byte) 0xb0, (byte) 0xb6,
                (byte) 0xe0, (byte) 0xb0, (byte) 0xbe, (byte) 0xe0,
                (byte) 0xb0, (byte) 0x82, (byte) 0xe0, (byte) 0xb0,
                (byte) 0xa4, (byte) 0xe0, (byte) 0xb0, (byte) 0xbf, ' ',
                (byte) 0xe0, (byte) 0xb8, (byte) 0xaa, (byte) 0xe0,
                (byte) 0xb8, (byte) 0xb1, (byte) 0xe0, (byte) 0xb8,
                (byte) 0x99, (byte) 0xe0, (byte) 0xb8, (byte) 0x95,
                (byte) 0xe0, (byte) 0xb8, (byte) 0xb4, (byte) 0xe0,
                (byte) 0xb8, (byte) 0xa0, (byte) 0xe0, (byte) 0xb8,
                (byte) 0xb2, (byte) 0xe0, (byte) 0xb8, (byte) 0x9e, ' ',
                (byte) 0xe1, (byte) 0x88, (byte) 0xb0, (byte) 0xe1,
                (byte) 0x88, (byte) 0x8b, (byte) 0xe1, (byte) 0x88,
                (byte) 0x9d, ' ', (byte) 0xe0, (byte) 0xb7, (byte) 0x83,
                (byte) 0xe0, (byte) 0xb7, (byte) 0x8f, (byte) 0xe0,
                (byte) 0xb6, (byte) 0xb8, (byte) 0xe0, (byte) 0xb6,
                (byte) 0xba, ' ', (byte) 0xe0, (byte) 0xa4, (byte) 0xb6,
                (byte) 0xe0, (byte) 0xa4, (byte) 0xbe, (byte) 0xe0,
                (byte) 0xa4, (byte) 0xa8, (byte) 0xe0, (byte) 0xa5,
                (byte) 0x8d, (byte) 0xe0, (byte) 0xa4, (byte) 0xa4,
                (byte) 0xe0, (byte) 0xa4, (byte) 0xbf, (byte) 0xe0,
                (byte) 0xa4, (byte) 0x83, ' ', (byte) 0xe1, (byte) 0x83,
                (byte) 0x9b, (byte) 0xe1, (byte) 0x83, (byte) 0xa8,
                (byte) 0xe1, (byte) 0x83, (byte) 0x95, (byte) 0xe1,
                (byte) 0x83, (byte) 0x98, (byte) 0xe1, (byte) 0x83,
                (byte) 0x93, (byte) 0xe1, (byte) 0x83, (byte) 0x9d,
                (byte) 0xe1, (byte) 0x83, (byte) 0x91, (byte) 0xe1,
                (byte) 0x83, (byte) 0x90 };
        // TODO Cannot make the following word work, encoder changes needed
        // (byte) 0xed, (byte) 0xa0, (byte) 0x80,
        // (byte) 0xed, (byte) 0xbc, (byte) 0xb2, (byte) 0xed,
        // (byte) 0xa0, (byte) 0x80, (byte) 0xed, (byte) 0xbc,
        // (byte) 0xb0, (byte) 0xed, (byte) 0xa0, (byte) 0x80,
        // (byte) 0xed, (byte) 0xbd, (byte) 0x85, (byte) 0xed,
        // (byte) 0xa0, (byte) 0x80, (byte) 0xed, (byte) 0xbc,
        // (byte) 0xb0, (byte) 0xed, (byte) 0xa0, (byte) 0x80,
        // (byte) 0xed, (byte) 0xbc, (byte) 0xb9, (byte) 0xed,
        // (byte) 0xa0, (byte) 0x80, (byte) 0xed, (byte) 0xbc,
        // (byte) 0xb8, (byte) 0xed, (byte) 0xa0, (byte) 0x80,
        // (byte) 0xed, (byte) 0xbc, (byte) 0xb9, ' '

        final String vendor = new String(bVendor, "UTF-8");
        final String spec = new String(bSpec, "UTF-8");
        m.getMainAttributes()
                .put(Attributes.Name.IMPLEMENTATION_VENDOR, vendor);
        m.getAttributes(ATT_ENTRY_NAME).put(
                Attributes.Name.IMPLEMENTATION_VENDOR, vendor);
        m.getEntries().get(ATT_ENTRY_NAME).put(
                Attributes.Name.SPECIFICATION_TITLE, spec);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        m.write(baos);
        m = new Manifest(new ByteArrayInputStream(baos.toByteArray()));

        assertEquals(vendor, m.getMainAttributes().get(
                Attributes.Name.IMPLEMENTATION_VENDOR));
        assertEquals(vendor, m.getEntries().get(ATT_ENTRY_NAME).get(
                Attributes.Name.IMPLEMENTATION_VENDOR));
        assertEquals(spec, m.getAttributes(ATT_ENTRY_NAME).get(
                Attributes.Name.SPECIFICATION_TITLE));
    }

    /**
     * {@link java.util.jar.Manifest#read(java.io.InputStream)
     */
    public void testRead() {
        // Regression for HARMONY-89
        InputStream is = new InputStreamImpl();
        try {
            new Manifest().read(is);
            fail("IOException expected");
        } catch (IOException e) {
            // desired
        }
    }

    // helper class
    private class InputStreamImpl extends InputStream {
        public InputStreamImpl() {
            super();
        }

        @Override
        public int read() {
            return 0;
        }
    }
}
