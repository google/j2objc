/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tests.targets.security;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Locale;
import junit.framework.TestCase;

public abstract class MessageDigestTest extends TestCase {

    private String digestAlgorithmName;

    MessageDigestTest(String digestAlgorithmName) {
        super();
        this.digestAlgorithmName = digestAlgorithmName;
    }

    private MessageDigest digest;
    private InputStream sourceData;
    private byte[] checkDigest;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        this.source3 = getLongMessage(1000000);
        this.digest = MessageDigest.getInstance(digestAlgorithmName);
        this.sourceData = getSourceData();
        this.checkDigest = getCheckDigest();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        // This is critical. The MessageDigest tests consume a lot of memory due
        // to the 1 MB buffers being allocated. We need to make sure all data is
        // freed after each test. Otherwise the Android runtime simply dies at
        // some point.
        source1 = null;
        source2 = null;
        source3 = null;

        expected1 = null;
        expected2 = null;
        expected3 = null;

        digest = null;
        sourceData.close();
        sourceData = null;
        checkDigest = null;

        System.gc();
    }

    private InputStream getSourceData() {
        InputStream sourceStream = getClass().getResourceAsStream(digestAlgorithmName + ".data");
        assertNotNull("digest source data not found: " + digestAlgorithmName, sourceStream);
        return sourceStream;
    }

    private byte[] getCheckDigest() throws Exception {
        InputStream checkDigestStream =
                getClass().getResourceAsStream(digestAlgorithmName + ".check");
        byte[] checkDigest = new byte[digest.getDigestLength()];
        int read;
        int index = 0;
        while ((read = checkDigestStream.read()) != -1) {
            checkDigest[index++] = (byte)read;
        }
        checkDigestStream.close();
        return checkDigest;
    }

    public void testMessageDigest1() throws Exception {
        byte[] buf = new byte[128];
        int read;
        while ((read = sourceData.read(buf)) != -1) {
            digest.update(buf, 0, read);
        }
        sourceData.close();

        byte[] computedDigest = digest.digest();

        assertNotNull("computed digest is is null", computedDigest);
        assertEquals("digest length mismatch", checkDigest.length, computedDigest.length);

        for (int i = 0; i < checkDigest.length; i++) {
            assertEquals("byte " + i + " of computed and check digest differ",
                         checkDigest[i], computedDigest[i]);
        }
    }

    public void testMessageDigest2() throws Exception {
        int val;
        while ((val = sourceData.read()) != -1) {
            digest.update((byte)val);
        }
        sourceData.close();

        byte[] computedDigest = digest.digest();

        assertNotNull("computed digest is is null", computedDigest);
        assertEquals("digest length mismatch", checkDigest.length, computedDigest.length);

        for (int i = 0; i < checkDigest.length; i++) {
            assertEquals("byte " + i + " of computed and check digest differ",
                         checkDigest[i], computedDigest[i]);
        }
    }


    /**
     * Official FIPS180-2 testcases
     */

    String source1;
    String source2;
    private String source3;
    String expected1;
    String expected2;
    String expected3;

    private String getLongMessage(int length) {
        StringBuilder sourceBuilder = new StringBuilder(length);
        for (int i = 0; i < length / 10; i++) {
            sourceBuilder.append("aaaaaaaaaa");
        }
        return sourceBuilder.toString();
    }

    public void testfips180_2_singleblock() {

        digest.update(source1.getBytes(), 0, source1.length());

        byte[] computedDigest = digest.digest();

        assertNotNull("computed digest is null", computedDigest);

        String actual = digestToString(computedDigest);
        assertEquals("computed and check digest differ", expected1, actual);
    }

    private String digestToString(byte[] computedDigest) {
        StringBuilder sb = new StringBuilder();
        for (byte b : computedDigest) {
          sb.append(String.format(Locale.US, "%02x", b));
        }
        return sb.toString();
    }

    public void testfips180_2_multiblock() {

        digest.update(source2.getBytes(), 0, source2.length());

        byte[] computedDigest = digest.digest();

        assertNotNull("computed digest is null", computedDigest);

        String actual = digestToString(computedDigest);
        assertEquals("computed and check digest differ", expected2, actual);
    }

    public void testfips180_2_longMessage() {

        digest.update(source3.getBytes(), 0, source3.length());

        byte[] computedDigest = digest.digest();

        assertNotNull("computed digest is null", computedDigest);

        String actual = digestToString(computedDigest);
        assertEquals("computed and check digest differ", expected3, actual);
    }
}
