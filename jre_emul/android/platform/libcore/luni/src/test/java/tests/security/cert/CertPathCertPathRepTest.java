package tests.security.cert;

import junit.framework.TestCase;

import org.apache.harmony.security.tests.support.cert.MyCertPath;
import org.apache.harmony.security.tests.support.cert.MyCertPath.MyCertPathRep;

import java.io.ObjectStreamException;
import java.security.cert.CertPath;

public class CertPathCertPathRepTest extends TestCase {

    private static final byte[] testEncoding = new byte[] { (byte) 1, (byte) 2,
            (byte) 3, (byte) 4, (byte) 5 };

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test for <code>CertPath.CertPathRep(String type, byte[] data)</code>
     * method<br>
     */
    public final void testCertPathCertPathRep() {
        MyCertPath cp = new MyCertPath(testEncoding);
        MyCertPathRep rep = cp.new MyCertPathRep("MyEncoding", testEncoding);
        assertEquals(testEncoding, rep.getData());
        assertEquals("MyEncoding", rep.getType());

        try {
            cp.new MyCertPathRep(null, null);
        } catch (Exception e) {
            fail("Unexpected exeption " + e.getMessage());
        }

    }

    public final void testReadResolve() {
        MyCertPath cp = new MyCertPath(testEncoding);
        MyCertPathRep rep = cp.new MyCertPathRep("MyEncoding", testEncoding);

        try {
            Object obj = rep.readResolve();
            fail("ObjectStreamException was not thrown.");
        } catch (ObjectStreamException e) {
            //expected
        }

        rep = cp.new MyCertPathRep("MyEncoding", new byte[] {(byte) 1, (byte) 2, (byte) 3 });
        try {
            rep.readResolve();
            fail("ObjectStreamException expected");
        } catch (ObjectStreamException e) {
            // expected
            System.out.println(e);
        }
    }
}
