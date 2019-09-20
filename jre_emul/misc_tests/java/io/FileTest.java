/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.io;

import java.util.UUID;
import junit.framework.TestCase;

/**
 * Unit tests for {@link java.io.File}.
 */
public class FileTest extends TestCase {
  private File tmpDirectory;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    String base = System.getProperty("java.io.tmpdir");
    File directory = new File(base, UUID.randomUUID().toString());
    assertTrue(directory.mkdirs());
    tmpDirectory = directory;
  }

  @Override
  protected void tearDown() throws Exception {
    deleteTestDirectory(tmpDirectory);
    super.tearDown();
  }

  public void testDeleteRecursively() throws IOException {
    // Create 3-deep directory structure with 3 files in each directory.
    File testDir = createTestDirectory(tmpDirectory, "one");
    File middleDir = createTestDirectory(testDir, "two");
    /* innerDir = */ createTestDirectory(middleDir, "three");
    assertEquals(9, countFilesRecursively(testDir));
    deleteTestDirectory(testDir);
  }

  private File createTestDirectory(File currentDir, String path) throws IOException {
    File newDir = new File(currentDir, path);
    assertTrue(newDir.mkdir());
    for (int i = 0; i < 3; i++) {
      File f = File.createTempFile("temp", "file", newDir);
      assertTrue(f.exists());
    }
    assertEquals(3, newDir.list().length);
    return newDir;
  }

  private int countFilesRecursively(File currentDir) {
    int n = 0;
    for (File f : currentDir.listFiles()) {
      if (f.isDirectory()) {
        n += countFilesRecursively(f);
      } else {
        ++n;
      }
    }
    return n;
  }

  private void deleteTestDirectory(File dir) {
    for (File f : dir.listFiles()) {
      if (f.isDirectory()) {
        deleteTestDirectory(f);
      } else {
        assertTrue(f.delete());
      }
    }
    assertTrue(dir.delete());
  }
}
