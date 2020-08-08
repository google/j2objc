package libcore.java.nio.file.spi;

import org.junit.Test;

import java.nio.file.Paths;
import java.nio.file.spi.FileTypeDetector;

import static org.junit.Assert.assertEquals;

public class FileTypeDetectorTest {

    @Test
    public void test_probeFileType() throws Exception {
        FileTypeDetector defaultFileTypeDetector = sun.nio.fs.DefaultFileTypeDetector.create();
        // The method uses file extensions to deduce mime type, therefore, it doesn't check for
        // file existence.
        assertEquals("text/plain",
                defaultFileTypeDetector.probeContentType(Paths.get("file.txt")));
        assertEquals("text/x-java",
                defaultFileTypeDetector.probeContentType(Paths.get("file.java")));
    }
}
