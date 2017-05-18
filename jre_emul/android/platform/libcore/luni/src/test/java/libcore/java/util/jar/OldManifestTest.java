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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import junit.framework.TestCase;
import tests.support.resource.Support_Resources;

public class OldManifestTest extends TestCase {

    public void test_ConstructorLjava_util_jar_Manifest() {
        // Test for method java.util.jar.Manifest()
        Manifest emptyManifest = new Manifest();
        Manifest emptyClone = new Manifest(emptyManifest);
        assertTrue("Should have no entries", emptyClone.getEntries().isEmpty());
        assertTrue("Should have no main attributes", emptyClone
                .getMainAttributes().isEmpty());
        assertEquals(emptyClone, emptyManifest);
        assertEquals(emptyClone, emptyManifest.clone());
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

    public void test_clone() throws IOException {
        Manifest emptyManifest = new Manifest();
        Manifest emptyClone = (Manifest) emptyManifest.clone();
        assertTrue("Should have no entries", emptyClone.getEntries().isEmpty());
        assertTrue("Should have no main attributes", emptyClone
                .getMainAttributes().isEmpty());
        assertEquals(emptyClone, emptyManifest);
        assertEquals(emptyManifest.clone().getClass().getName(),
                "java.util.jar.Manifest");

        Manifest manifest = new Manifest(new URL(Support_Resources
                .getURL("manifest/hyts_MANIFEST.MF")).openStream());
        Manifest manifestClone = (Manifest) manifest.clone();
        manifestClone.getMainAttributes();
        checkManifest(manifestClone);
    }

    public void test_equals() throws IOException {
        Manifest manifest1 = new Manifest(new URL(Support_Resources.getURL(
                "manifest/hyts_MANIFEST.MF")).openStream());
        Manifest manifest2 = new Manifest(new URL(Support_Resources.getURL(
                "manifest/hyts_MANIFEST.MF")).openStream());
        Manifest manifest3 = new Manifest();

        assertTrue(manifest1.equals(manifest1));
        assertTrue(manifest1.equals(manifest2));
        assertFalse(manifest1.equals(manifest3));
        assertFalse(manifest1.equals(this));
    }

    public void test_writeLjava_io_OutputStream() throws IOException {
        byte b[];
        Manifest manifest1 = null;
        Manifest manifest2 = null;
        try {
            manifest1 = new Manifest(new URL(Support_Resources
                    .getURL("manifest/hyts_MANIFEST.MF")).openStream());
        } catch (MalformedURLException e) {
            fail("Malformed URL");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        manifest1.write(baos);

        b = baos.toByteArray();

        File f = File.createTempFile("111", "111");
        FileOutputStream fos = new FileOutputStream(f);
        fos.close();
        try {
            manifest1.write(fos);
            fail("IOException expected");
        } catch (IOException e) {
            // expected
        }
        f.delete();

        ByteArrayInputStream bais = new ByteArrayInputStream(b);

        try {
            manifest2 = new Manifest(bais);
        } catch (MalformedURLException e) {
            fail("Malformed URL");
        }

        assertTrue(manifest1.equals(manifest2));
    }

    public void test_write_no_version() throws Exception {
        // If you write a manifest with no MANIFEST_VERSION, your attributes don't get written out.
        assertEquals(null, doRoundTrip(null));
        // But they do if you supply a MANIFEST_VERSION.
        assertEquals("image/pr0n", doRoundTrip(Attributes.Name.MANIFEST_VERSION));
        assertEquals("image/pr0n", doRoundTrip("Signature-Version"));
        assertEquals(null, doRoundTrip("Random-String-Version"));
    }

    private String doRoundTrip(Object versionName) throws Exception {
        Manifest m1 = new Manifest();
        m1.getMainAttributes().put(Attributes.Name.CONTENT_TYPE, "image/pr0n");
        if (versionName != null) {
            m1.getMainAttributes().putValue(versionName.toString(), "1.2.3");
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        m1.write(os);

        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        Manifest m2 = new Manifest();
        m2.read(is);
        return (String) m2.getMainAttributes().get(Attributes.Name.CONTENT_TYPE);
    }

    public void test_write_two_versions() throws Exception {
        // It's okay to have two versions.
        Manifest m1 = new Manifest();
        m1.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        m1.getMainAttributes().put(Attributes.Name.SIGNATURE_VERSION, "2.0");
        m1.getMainAttributes().putValue("Aardvark-Version", "3.0");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        m1.write(os);

        // The Manifest-Version takes precedence,
        // and the Signature-Version gets no special treatment.
        List<String> lines = Arrays.asList(new String(os.toByteArray(), "UTF-8").split("\r\n"));
        // The first line must always contain the Manifest-Version (or the Signature-Version if
        // the ManifestVersion is missing.
        assertEquals("Manifest-Version: 1.0", lines.get(0));

        assertTrue(lines.contains("Aardvark-Version: 3.0"));
        assertTrue(lines.contains("Signature-Version: 2.0"));
    }
}
