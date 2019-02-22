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
package com.google.devtools.j2objc.util;

import com.google.common.io.CharStreams;
import com.google.devtools.j2objc.J2ObjC;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.PackageDeclaration;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.file.JarredInputFile;
import com.google.devtools.j2objc.file.RegularInputFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.annotation.Nullable;
import javax.tools.JavaFileObject;

/**
 * Utilities for reading {@link com.google.devtools.j2objc.file.InputFile}s.
 *
 * @author Tom Ball, Keith Stanger, Mike Thvedt, Tim Gao
 */
public class FileUtil {

  private Set<String> tempDirs = new HashSet<>();
  private List<String> sourcePathEntries = new ArrayList<>();
  private List<String> classPathEntries = new ArrayList<>();
  private File outputDirectory = new File(".");
  private File headerOutputDirectory = null;
  private String fileEncoding = System.getProperty("file.encoding", "UTF-8");
  private Charset charset = Charset.forName(fileEncoding);

  public void setSourcePathEntries(List<String> sourcePathEntries) {
    this.sourcePathEntries = sourcePathEntries;
  }

  public List<String> getSourcePathEntries() {
    return sourcePathEntries;
  }

  public void appendSourcePath(String entry) {
    sourcePathEntries.add(entry);
  }

  public void insertSourcePath(int index, String entry) {
    sourcePathEntries.add(index, entry);
  }

  public List<String> getClassPathEntries() {
    return classPathEntries;
  }

  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public void setHeaderOutputDirectory(File outputDirectory) {
    this.headerOutputDirectory = outputDirectory;
  }

  public File getOutputDirectory() {
    if (!outputDirectory.exists()) {
      outputDirectory.mkdirs();
    }
    return outputDirectory;
  }

  public File getHeaderOutputDirectory() {
    if (headerOutputDirectory != null) {
      if (!headerOutputDirectory.exists()) {
        headerOutputDirectory.mkdirs();
      }
      return headerOutputDirectory;
    } else {
      return getOutputDirectory();
    }
  }

  public void setFileEncoding(String fileEncoding) {
    this.fileEncoding = fileEncoding;
    charset = Charset.forName(fileEncoding);
  }

  public String getFileEncoding() {
    return fileEncoding;
  }

  public Charset getCharset() {
    return charset;
  }

  public void addTempDir(String tempDir) {
    tempDirs.add(tempDir);
  }

  public Set<String> getTempDirs() {
    return tempDirs;
  }

  public static String getMainTypeName(InputFile file) {
    String basename = file.getBasename();
    return removeFileSuffix(basename);
  }

  public static String getMainTypeName(JavaFileObject file) {
    String path = file.getName();
    String basename = path.substring(path.lastIndexOf('/') + 1);
    return removeFileSuffix(basename);
  }

  private static String removeFileSuffix(String basename) {
    int end = basename.lastIndexOf(".java");
    if (end == -1) {
      end = basename.lastIndexOf(".class");
    }
    return end != -1 ? basename.substring(0, end) : basename;
  }

  public static String getQualifiedMainTypeName(InputFile file, CompilationUnit unit) {
    String qualifiedName = getMainTypeName(file);
    PackageDeclaration packageDecl = unit.getPackage();
    if (packageDecl != null) {
      String packageName = packageDecl.getName().getFullyQualifiedName();
      qualifiedName = packageName + "." + qualifiedName;
    }
    return qualifiedName;
  }

  /**
   * Find a {@link com.google.devtools.j2objc.file.InputFile} on the source path,
   * either in a directory or a jar, using a fully-qualified type name.
   * Returns a file guaranteed to exist, or null.
   */
  @Nullable
  public InputFile findTypeOnSourcePath(String qualifiedName) throws IOException {
    return findTypeOnPaths(qualifiedName, sourcePathEntries, ".java");
  }

  /**
   * Find a {@link com.google.devtools.j2objc.file.InputFile} on the class path,
   * either in a directory or a jar, using a fully-qualified type name.
   * Returns a file guaranteed to exist, or null.
   */
  @Nullable
  public InputFile findTypeOnClassPath(String qualifiedName) throws IOException {
    return findTypeOnPaths(qualifiedName, classPathEntries, ".class");
  }

  private static InputFile findTypeOnPaths(
      String qualifiedName, List<String> paths, String extension) throws IOException {
    String sourceFileName = qualifiedName.replace('.', File.separatorChar) + extension;
    return findFileOnPaths(sourceFileName, paths);
  }

  /**
   * Find a source file on the source path(s), either in a directory or a jar.
   *
   * @param sourceFileName the relative path of the source file.
   * @return an InputFile guaranteed to exist, or null.
   */
  @Nullable
  public InputFile findFileOnSourcePath(String sourceFileName) throws IOException {
    return findFileOnPaths(sourceFileName, sourcePathEntries);
  }

  private static InputFile findFileOnPaths(
      String sourceFileName, List<String> paths) throws IOException {
    // Zip/jar files always use forward slashes.
    String jarEntryName = sourceFileName.replace(File.separatorChar, '/');
    for (String pathEntry : paths) {
      File f = new File(pathEntry);
      if (f.isDirectory()) {
        RegularInputFile regularFile = new RegularInputFile(
            pathEntry + File.separatorChar + sourceFileName, sourceFileName);
        if (regularFile.exists()) {
          return regularFile;
        }
      } else {
        // Assume it's a jar file
        JarredInputFile jarFile = new JarredInputFile(pathEntry, jarEntryName);
        if (jarFile.exists()) {
          return jarFile;
        }
      }
    }
    return null;
  }

  public String readFile(InputFile file) throws IOException {
    return CharStreams.toString(file.openReader(charset));
  }

  private static InputStream streamForFile(String filename) throws IOException {
    File f = new File(filename);
    if (f.exists()) {
      return new FileInputStream(f);
    } else {
      InputStream stream = J2ObjC.class.getResourceAsStream(filename);
      if (stream == null) {
        throw new FileNotFoundException(filename);
      }
      return stream;
    }
  }

  /**
   * Reads the given properties file.
   */
  public static Properties loadProperties(String resourceName) throws IOException {
    return loadProperties(streamForFile(resourceName));
  }

  public static Properties loadProperties(InputStream in) throws IOException {
    try {
      Properties p = new Properties();
      p.load(in);
      return p;
    } finally {
      in.close();
    }
  }

  public static File createTempDir(String dirname) throws IOException {
    File tmpDirectory = File.createTempFile(dirname, ".tmp");
    tmpDirectory.delete();
    if (!tmpDirectory.mkdir()) {
      throw new IOException("Could not create tmp directory: " + tmpDirectory.getPath());
    }
    tmpDirectory.deleteOnExit();
    return tmpDirectory;
  }

  /**
   * Recursively delete specified directory.
   */
  public static void deleteTempDir(File dir) {
    // TODO(cpovirk): try Directories.deleteRecursively if a c.g.c.unix dep is OK
    if (dir != null && dir.exists()) {
      for (File f : dir.listFiles()) {
        if (f.isDirectory()) {
          deleteTempDir(f);
        } else {
          f.delete();
        }
      }
      dir.delete();
    }
  }

  /**
   * Extract a ZipEntry to the specified directory.
   */
  public File extractZipEntry(File dir, ZipFile zipFile, ZipEntry entry) throws IOException {
    File outputFile = new File(dir, entry.getName());
    File parentFile = outputFile.getParentFile();
    if (!outputFile.getCanonicalPath().startsWith(dir.getCanonicalPath() + File.separator)
        || (!parentFile.isDirectory() && !parentFile.mkdirs())) {
      throw new IOException("Could not extract " + entry.getName() + " to " + dir.getPath());
    }
    try (InputStream inputStream = zipFile.getInputStream(entry);
        FileOutputStream outputStream = new FileOutputStream(outputFile)) {
      byte[] buf = new byte[1024];
      int n;
      while ((n = inputStream.read(buf)) > 0) {
        outputStream.write(buf, 0, n);
      }
    }
    return outputFile;
  }

  /**
   * Android libraries are packaged in AAR files, which is a zip file with a ".aar"
   * suffix that contains a classes.jar with the classfiles, as well as any Android
   * resources associated with this library. This method extracts the classes.jar
   * entry from the AAR file, so it can be used as a classpath entry.
   * <p>
   * If there is an error extracting the classes.jar entry, a warning is logged
   * and the original path is returned.
   */
  public File extractClassesJarFromAarFile(File aarFile) {
    try (ZipFile zfile = new ZipFile(aarFile)) {
      File tempDir = FileUtil.createTempDir(aarFile.getName());
      ZipEntry entry = zfile.getEntry("classes.jar");
      if (entry == null) {
        ErrorUtil.warning(aarFile.getPath() + " does not have a classes.jar entry");
        return aarFile;
      }
      File tmpFile = extractZipEntry(tempDir, zfile, entry);
      tmpFile.deleteOnExit();
      return tmpFile;
    } catch (IOException e) {
      ErrorUtil.warning("unable to access " + aarFile.getPath() + ": " + e);
      return aarFile;
    }
  }
}
