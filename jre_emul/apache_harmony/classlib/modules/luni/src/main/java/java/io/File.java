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

package java.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/*-{
#import <sys/utsname.h>
}-*/

/**
 * An "abstract" representation of a file system entity identified by a
 * pathname. The pathname may be absolute (relative to the root directory
 * of the file system) or relative to the current directory in which the program
 * is running.
 * <p>
 * This class provides methods for querying/changing information about the file
 * as well as directory listing capabilities if the file represents a directory.
 * <p>
 * When manipulating file paths, the static fields of this class may be used to
 * determine the platform specific separators.
 *
 * @see java.io.Serializable
 * @see java.lang.Comparable
 */
public class File implements Serializable, Comparable<File> {

    private static final long serialVersionUID = 301077366599181567L;

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    private String path;

    transient String properPath;

    /**
     * The system dependent file separator character.
     */
    public static final char separatorChar = '/';

    /**
     * The system dependent file separator string. The initial value of this
     * field is the system property "file.separator".
     */
    public static final String separator = "/";

    /**
     * The system dependent path separator character.
     */
    public static final char pathSeparatorChar = ':';

    /**
     * The system dependent path separator string. The initial value of this
     * field is the system property "path.separator".
     */
    public static final String pathSeparator = ":";

    /* Temp file counter */
    private static int counter = 0;

    /* identify for different VM processes */
    private static int counterBase = 0;

    private static class TempFileLocker {};

    private static TempFileLocker tempFileLocker = new TempFileLocker();

    private static boolean caseSensitive;

    static {
        caseSensitive = isCaseSensitiveImpl();
    }

    /**
     * Constructs a new file using the specified directory and name.
     *
     * @param dir
     *            the directory where the file is stored.
     * @param name
     *            the file's name.
     * @throws NullPointerException
     *             if {@code name} is {@code null}.
     */
    public File(File dir, String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        if (dir == null) {
            this.path = fixSlashes(name);
        } else {
            this.path = calculatePath(dir.getPath(), name);
        }
    }

    /**
     * Constructs a new file using the specified path.
     *
     * @param path
     *            the path to be used for the file.
     */
    public File(String path) {
        // path == null check & NullPointerException thrown by fixSlashes
        this.path = fixSlashes(path);
    }

    /**
     * Constructs a new File using the specified directory path and file name,
     * placing a path separator between the two.
     *
     * @param dirPath
     *            the path to the directory where the file is stored.
     * @param name
     *            the file's name.
     * @throws NullPointerException
     *             if {@code name} is {@code null}.
     */
    public File(String dirPath, String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        if (dirPath == null) {
            this.path = fixSlashes(name);
        } else {
            this.path = calculatePath(dirPath, name);
        }
    }

    /**
     * Constructs a new File using the path of the specified URI. {@code uri}
     * needs to be an absolute and hierarchical Unified Resource Identifier with
     * file scheme and non-empty path component, but with undefined authority,
     * query or fragment components.
     *
     * @param uri
     *            the Unified Resource Identifier that is used to construct this
     *            file.
     * @throws IllegalArgumentException
     *             if {@code uri} does not comply with the conditions above.
     * @see #toURI
     * @see java.net.URI
     *
     TODO(user): enable when java.net support is implemented.
    public File(URI uri) {
        // check pre-conditions
        checkURI(uri);
        this.path = fixSlashes(uri.getPath());
    }
    */

    private String calculatePath(String dirPath, String name) {
        dirPath = fixSlashes(dirPath);
        if (!name.equals(EMPTY_STRING) || dirPath.equals(EMPTY_STRING)) {
            // Remove all the proceeding separator chars from name
            name = fixSlashes(name);

            int separatorIndex = 0;
            while ((separatorIndex < name.length())
                    && (name.charAt(separatorIndex) == separatorChar)) {
                separatorIndex++;
            }
            if (separatorIndex > 0) {
                name = name.substring(separatorIndex, name.length());
            }

            // Ensure there is a separator char between dirPath and name
            if (dirPath.length() > 0
                    && (dirPath.charAt(dirPath.length() - 1) == separatorChar)) {
                return dirPath + name;
            }
            return dirPath + separatorChar + name;
        }

        return dirPath;
    }

    /* TODO(user): enable when java.net support is implemented.
    @SuppressWarnings("nls")
    private void checkURI(URI uri) {
        if (!uri.isAbsolute()) {
            throw new IllegalArgumentException("URI is not absolute: " + uri);
        } else if (!uri.getRawSchemeSpecificPart().startsWith("/")) {
            throw new IllegalArgumentException("URI is not hierarchical: " + uri);
        }

        String temp = uri.getScheme();
        if (temp == null || !temp.equals("file")) {
            throw new IllegalArgumentException("Expected file scheme in URI: " + uri);
        }

        temp = uri.getRawPath();
        if (temp == null || temp.length() == 0) {
            throw new IllegalArgumentException("Expected non-empty path in URI: " + uri);
        }

        if (uri.getRawAuthority() != null) {
            throw new IllegalArgumentException("Found authority component in URI: " +
        	uri.toString());
        }

        if (uri.getRawQuery() != null) {
            throw new IllegalArgumentException("Found query component in URI: " + uri.toString());
        }

        if (uri.getRawFragment() != null) {
            throw new IllegalArgumentException("Found fragment component in URI: " +
                uri.toString());
        }
    }
    */

    private static native boolean isCaseSensitiveImpl() /*-{
      // True for iOS, not OS X/simulator.
      struct utsname systemInfo;
      uname(&systemInfo);
      if (strcmp(systemInfo.machine, "i386") == 0 || strncmp(systemInfo.machine, "x86", 3) == 0) {
        return false;
      }
      return true;
    }-*/;

    /**
     * Lists the file system roots. The Java platform may support zero or more
     * file systems, each with its own platform-dependent root. Further, the
     * canonical pathname of any file on the system will always begin with one
     * of the returned file system roots.
     *
     * @return the array of file system roots.
     */
    public static File[] listRoots() {
        return new File[] { new File("/") };
    }

    /**
     * The purpose of this method is to take a path and fix the slashes up. This
     * includes changing them all to the current platforms fileSeparator and
     * removing duplicates.
     */
    private String fixSlashes(String origPath) {
        int uncIndex = 1;
        int length = origPath.length(), newLength = 0;
        uncIndex = 0;

        boolean foundSlash = false;
        char newPath[] = origPath.toCharArray();
        for (int i = 0; i < length; i++) {
            char pathChar = newPath[i];
            if (pathChar == '/') {
                /* UNC Name requires 2 leading slashes */
                if ((foundSlash && i == uncIndex) || !foundSlash) {
                    newPath[newLength++] = separatorChar;
                    foundSlash = true;
                }
            } else {
                // check for leading slashes before a drive
                if (pathChar == ':'
                        && uncIndex > 0
                        && (newLength == 2 || (newLength == 3 && newPath[1] == separatorChar))
                        && newPath[0] == separatorChar) {
                    newPath[0] = newPath[newLength - 1];
                    newLength = 1;
                    // allow trailing slash after drive letter
                    uncIndex = 2;
                }
                newPath[newLength++] = pathChar;
                foundSlash = false;
            }
        }
        // remove trailing slash
        if (foundSlash
                && (newLength > (uncIndex + 1) || (newLength == 2 && newPath[0] != separatorChar))) {
            newLength--;
        }

        return new String(newPath, 0, newLength);
    }

    /**
     * Indicates whether the current context is allowed to read from this file.
     *
     * @return {@code true} if this file can be read, {@code false} otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies the
     *             read request.
     */
    public boolean canRead() {
        if (path.length() == 0) {
            return false;
        }
        String pp = properPath(true);
        return existsImpl(pp) && !isWriteOnlyImpl(pp);
    }

    /**
     * Indicates whether the current context is allowed to write to this file.
     *
     * @return {@code true} if this file can be written, {@code false}
     *         otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies the
     *             write request.
     */
    public boolean canWrite() {
        // Cannot use exists() since that does an unwanted read-check.
        boolean exists = false;
        if (path.length() > 0) {
            exists = existsImpl(properPath(true));
        }
        return exists && !isReadOnlyImpl(properPath(true));
    }

    /**
     * Returns the relative sort ordering of the paths for this file and the
     * file {@code another}. The ordering is platform dependent.
     *
     * @param another
     *            a file to compare this file to
     * @return an int determined by comparing the two paths. Possible values are
     *         described in the Comparable interface.
     * @see Comparable
     */
    public int compareTo(File another) {
        if (caseSensitive) {
            return this.getPath().compareTo(another.getPath());
        }
        return this.getPath().compareToIgnoreCase(another.getPath());
    }

    /**
     * Deletes this file. Directories must be empty before they will be deleted.
     *
     * @return {@code true} if this file was deleted, {@code false} otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies the
     *             request.
     * @see java.lang.SecurityManager#checkDelete
     */
    public boolean delete() {
        String propPath = properPath(true);
        if ((path.length() != 0) && isDirectoryImpl(propPath)) {
            return deleteDirImpl(propPath);
        }
        return deleteFileImpl(propPath);
    }

    private native boolean deleteDirImpl(String filePath) /*-{
      NSArray *files =
          [[NSFileManager defaultManager] contentsOfDirectoryAtPath:filePath error:nil];
      if (!files || [files count] > 0) {  // Don't delete if non-empty.
        return false;
      }
      return [self deleteFileImplWithNSString:filePath];
    }-*/;

    private native boolean deleteFileImpl(String filePath) /*-{
      NSFileManager *manager = [NSFileManager defaultManager];
      BOOL isDir;
      if (![manager fileExistsAtPath:filePath isDirectory:&isDir] || isDir) {
        return NO;
      }
      if (![manager isDeletableFileAtPath:filePath]) {
        return NO;
      }
      return [[NSFileManager defaultManager] removeItemAtPath:filePath error:nil];
    }-*/;

    /**
     * Schedules this file to be automatically deleted once the virtual machine
     * terminates. This will only happen when the virtual machine terminates
     * normally as described by the Java Language Specification section 12.9.
     *
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies the
     *             request.
     */
    public void deleteOnExit() {
        throw new AssertionError("not implemented");
    }

    /**
     * Compares {@code obj} to this file and returns {@code true} if they
     * represent the <em>same</em> object using a path specific comparison.
     *
     * @param obj
     *            the object to compare this file with.
     * @return {@code true} if {@code obj} is the same as this object,
     *         {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof File)) {
            return false;
        }
        if (!caseSensitive) {
            return path.equalsIgnoreCase(((File) obj).getPath());
        }
        return path.equals(((File) obj).getPath());
    }

    /**
     * Returns a boolean indicating whether this file can be found on the
     * underlying file system.
     *
     * @return {@code true} if this file exists, {@code false} otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     * @see #getPath
     * @see java.lang.SecurityManager#checkRead(FileDescriptor)
     */
    public boolean exists() {
        if (path.length() == 0) {
            return false;
        }
        return existsImpl(properPath(true));
    }

    private native boolean existsImpl(String filePath) /*-{
      return [[NSFileManager defaultManager] fileExistsAtPath:filePath];
    }-*/;

    /**
     * Returns the absolute path of this file.
     *
     * @return the absolute file path.
     * @see java.lang.SecurityManager#checkPropertyAccess
     */
    public String getAbsolutePath() {
        return properPath(false);
    }

    /**
     * Returns a new file constructed using the absolute path of this file.
     *
     * @return a new file from this file's absolute path.
     * @see java.lang.SecurityManager#checkPropertyAccess
     */
    public File getAbsoluteFile() {
        return new File(this.getAbsolutePath());
    }

    /**
     * Returns the absolute path of this file with all references resolved. An
     * <em>absolute</em> path is one that begins at the root of the file
     * system. The canonical path is one in which all references have been
     * resolved. For the cases of '..' and '.', where the file system supports
     * parent and working directory respectively, these are removed and replaced
     * with a direct directory reference. If the file does not exist,
     * getCanonicalPath() may not resolve any references and simply returns an
     * absolute path name or throws an IOException.
     *
     * @return the canonical path of this file.
     * @throws IOException
     *             if an I/O error occurs.
     * @see java.lang.SecurityManager#checkPropertyAccess
     */
    public String getCanonicalPath() throws IOException {
        return getAbsolutePath();
    }

    /*
     * Resolve symbolic links in the parent directories.
     */
    private byte[] resolve(byte[] newResult) throws IOException {
        int last = 1, nextSize, linkSize;
        byte[] linkPath = newResult, bytes;
        boolean done, inPlace;
        for (int i = 1; i <= newResult.length; i++) {
            if (i == newResult.length || newResult[i] == separatorChar) {
                done = i >= newResult.length - 1;
                // if there is only one segment, do nothing
                if (done && linkPath.length == 1) {
                    return newResult;
                }
                inPlace = false;
                if (linkPath == newResult) {
                    bytes = newResult;
                    // if there are no symbolic links, terminate the C string
                    // instead of copying
                    if (!done) {
                        inPlace = true;
                        newResult[i] = '\0';
                    }
                } else {
                    nextSize = i - last + 1;
                    linkSize = linkPath.length;
                    if (linkPath[linkSize - 1] == separatorChar) {
                        linkSize--;
                    }
                    bytes = new byte[linkSize + nextSize];
                    System.arraycopy(linkPath, 0, bytes, 0, linkSize);
                    System.arraycopy(newResult, last - 1, bytes, linkSize,
                            nextSize);
                    // the full path has already been resolved
                }
                if (done) {
                    return bytes;
                }
                linkPath = resolveLink(bytes, inPlace ? i : bytes.length, true);
                if (inPlace) {
                    newResult[i] = '/';
                }
                last = i + 1;
            }
        }
        throw new InternalError();
    }

    /*
     * Resolve a symbolic link. While the path resolves to an existing path,
     * keep resolving. If an absolute link is found, resolve the parent
     * directories if resolveAbsolute is true.
     */
    private byte[] resolveLink(byte[] pathBytes, int length,
            boolean resolveAbsolute) throws IOException {
        boolean restart = false;
        byte[] linkBytes, temp;
        do {
            linkBytes = getLinkImpl(new String(pathBytes)).getBytes();
            if (linkBytes == pathBytes) {
                break;
            }
            if (linkBytes[0] == separatorChar) {
                // link to an absolute path, if resolving absolute paths,
                // resolve the parent dirs again
                restart = resolveAbsolute;
                pathBytes = linkBytes;
            } else {
                int last = length - 1;
                while (pathBytes[last] != separatorChar) {
                    last--;
                }
                last++;
                temp = new byte[last + linkBytes.length];
                System.arraycopy(pathBytes, 0, temp, 0, last);
                System.arraycopy(linkBytes, 0, temp, last, linkBytes.length);
                pathBytes = temp;
            }
            length = pathBytes.length;
        } while (existsImpl(new String(pathBytes)));
        // resolve the parent directories
        if (restart) {
            return resolve(pathBytes);
        }
        return pathBytes;
    }

    /**
     * Returns a new file created using the canonical path of this file.
     * Equivalent to {@code new File(this.getCanonicalPath())}.
     *
     * @return the new file constructed from this file's canonical path.
     * @throws IOException
     *             if an I/O error occurs.
     * @see java.lang.SecurityManager#checkPropertyAccess
     */
    public File getCanonicalFile() throws IOException {
        return new File(getCanonicalPath());
    }

    /**
     * Returns the name of the file or directory represented by this file.
     *
     * @return this file's name or an empty string if there is no name part in
     *         the file's path.
     */
    public String getName() {
        int separatorIndex = path.lastIndexOf(separator);
        return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1,
                path.length());
    }

    /**
     * Returns the pathname of the parent of this file. This is the path up to
     * but not including the last name. {@code null} is returned if there is no
     * parent.
     *
     * @return this file's parent pathname or {@code null}.
     */
    public String getParent() {
        int length = path.length(), firstInPath = 0;
        int index = path.lastIndexOf(separatorChar);
        if (index == -1 && firstInPath > 0) {
            index = 2;
        }
        if (index == -1 || path.charAt(length - 1) == separatorChar) {
            return null;
        }
        if (path.indexOf(separatorChar) == index
                && path.charAt(firstInPath) == separatorChar) {
            return path.substring(0, index + 1);
        }
        return path.substring(0, index);
    }

    /**
     * Returns a new file made from the pathname of the parent of this file.
     * This is the path up to but not including the last name. {@code null} is
     * returned when there is no parent.
     *
     * @return a new file representing this file's parent or {@code null}.
     */
    public File getParentFile() {
        String tempParent = getParent();
        if (tempParent == null) {
            return null;
        }
        return new File(tempParent);
    }

    /**
     * Returns the path of this file.
     *
     * @return this file's path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns an integer hash code for the receiver. Any two objects for which
     * {@code equals} returns {@code true} must return the same hash code.
     *
     * @return this files's hash value.
     * @see #equals
     */
    @Override
    public int hashCode() {
        if (caseSensitive) {
            return path.hashCode() ^ 1234321;
        }
        return path.toLowerCase(Locale.ENGLISH).hashCode() ^ 1234321;
    }

    /**
     * Indicates if this file's pathname is absolute. Whether a pathname is
     * absolute is platform specific. On UNIX, absolute paths must start with
     * the character '/'; on Windows it is absolute if either it starts with
     * '\\' (to represent a file server), or a letter followed by a colon.
     *
     * @return {@code true} if this file's pathname is absolute, {@code false}
     *         otherwise.
     * @see #getPath
     */
    public boolean isAbsolute() {
        // for Linux/OS X
        return (path.length() > 0 && path.charAt(0) == File.separatorChar);
    }

    /**
     * Indicates if this file represents a <em>directory</em> on the
     * underlying file system.
     *
     * @return {@code true} if this file is a directory, {@code false}
     *         otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     */
    public boolean isDirectory() {
        if (path.length() == 0) {
            return false;
        }
        return isDirectoryImpl(properPath(true));
    }

    private native boolean isDirectoryImpl(String filePath) /*-{
      BOOL isDir;
      BOOL exists = [[NSFileManager defaultManager] fileExistsAtPath:filePath isDirectory:&isDir];
      return exists && isDir;
    }-*/;

    /**
     * Indicates if this file represents a <em>file</em> on the underlying
     * file system.
     *
     * @return {@code true} if this file is a file, {@code false} otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     */
    public boolean isFile() {
        if (path.length() == 0) {
            return false;
        }
        return isFileImpl(properPath(true));
    }

    private native boolean isFileImpl(String filePath) /*-{
      BOOL isDir;
      BOOL exists = [[NSFileManager defaultManager] fileExistsAtPath:filePath isDirectory:&isDir];
      return exists && !isDir;
    }-*/;

    /**
     * Returns whether or not this file is a hidden file as defined by the
     * operating system. The notion of "hidden" is system-dependent. For Unix
     * systems a file is considered hidden if its name starts with a ".". For
     * Windows systems there is an explicit flag in the file system for this
     * purpose.
     *
     * @return {@code true} if the file is hidden, {@code false} otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     */
    public boolean isHidden() {
        if (path.length() == 0) {
            return false;
        }
        return isHiddenImpl(properPath(true));
    }

    private boolean isHiddenImpl(String filePath) {
      return getName().startsWith(".");
    }

    private native boolean isReadOnlyImpl(String filePath) /*-{
      return [[NSFileManager defaultManager] isReadableFileAtPath:filePath];
    }-*/;

    private native boolean isWriteOnlyImpl(String filePath) /*-{
      return [[NSFileManager defaultManager] isWritableFileAtPath:filePath];
    }-*/;

    private native String getLinkImpl(String filePath) /*-{
      return [[NSFileManager defaultManager] destinationOfSymbolicLinkAtPath:filePath error:nil];
    }-*/;

    /**
     * Returns the time when this file was last modified, measured in
     * milliseconds since January 1st, 1970, midnight.
     *
     * @return the time when this file was last modified.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     */
    public long lastModified() {
        long result = lastModifiedImpl(properPath(true));
        /* Temporary code to handle both return cases until natives fixed */
        if (result == -1 || result == 0) {
            return 0;
        }
        return result;
    }

    private native long lastModifiedImpl(String filePath) /*-{
      NSDictionary *attributes = [[NSFileManager defaultManager] attributesOfItemAtPath:filePath
                                                                                  error:nil];
      NSDate *lastModified = [attributes fileModificationDate];
      NSTimeInterval seconds = [lastModified timeIntervalSince1970];
      return llround(seconds * 1000);
    }-*/;

    /**
     * Sets the time this file was last modified, measured in milliseconds since
     * January 1st, 1970, midnight.
     *
     * @param time
     *            the last modification time for this file.
     * @return {@code true} if the operation is successful, {@code false}
     *         otherwise.
     * @throws IllegalArgumentException
     *             if {@code time < 0}.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies write
     *             access to this file.
     */
    public boolean setLastModified(long time) {
        if (time < 0) {
            throw new IllegalArgumentException("time must be positive");
        }
        return (setLastModifiedImpl(properPath(true), time));
    }

    private native boolean setAttribute(String path, String attributeKey, Object value) /*-{
      NSMutableDictionary *attributes = [NSMutableDictionary dictionaryWithCapacity:1];
      [attributes setObject:value forKey:attributeKey];
      return [[NSFileManager defaultManager] setAttributes:attributes ofItemAtPath:path error:nil];
    }-*/;

    private native boolean setLastModifiedImpl(String path, long time) /*-{
      NSTimeInterval seconds = time / 1000.0;
      NSDate *lastModified = [NSDate dateWithTimeIntervalSince1970:seconds];
      return [self setAttributeWithNSString:path
                               withNSString:NSFileModificationDate
                                     withId:lastModified];
    }-*/;

    /**
     * Marks this file or directory to be read-only as defined by the operating
     * system.
     *
     * @return {@code true} if the operation is successful, {@code false}
     *         otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies write
     *             access to this file.
     */
    public boolean setReadOnly() {
        return (setReadOnlyImpl(properPath(true)));
    }

    private native boolean setReadOnlyImpl(String path) /*-{
      return [self setAttributeWithNSString:path
                               withNSString:NSFileImmutable
                                     withId:[NSNumber numberWithBool:YES]];
    }-*/;

    /**
     * Returns the length of this file in bytes.
     *
     * @return the number of bytes in this file.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     */
    public long length() {
        return lengthImpl(properPath(true));
    }

    private native long lengthImpl(String filePath) /*-{
      return [[[NSFileManager defaultManager] attributesOfItemAtPath:filePath error:nil] fileSize];
    }-*/;

    /**
     * Returns an array of strings with the file names in the directory
     * represented by this file. The result is {@code null} if this file is not
     * a directory.
     * <p>
     * The entries {@code .} and {@code ..} representing the current and parent
     * directory are not returned as part of the list.
     *
     * @return an array of strings with file names or {@code null}.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     * @see #isDirectory
     * @see java.lang.SecurityManager#checkRead(FileDescriptor)
     */
    public java.lang.String[] list() {
        if (path.length() == 0) {
            return null;
        }

        String bs = properPath(true);
        if (!isDirectoryImpl(bs) || !existsImpl(bs) || isWriteOnlyImpl(bs)) {
            return null;
        }

        String[] implList = listImpl(bs);
        if (implList == null) {
            // empty list
            return new String[0];
        }
        String result[] = new String[implList.length];
        for (int index = 0; index < implList.length; index++) {
            result[index] = new String(implList[index]);
        }
        return result;
    }

    /**
     * Returns an array of files contained in the directory represented by this
     * file. The result is {@code null} if this file is not a directory. The
     * paths of the files in the array are absolute if the path of this file is
     * absolute, they are relative otherwise.
     *
     * @return an array of files or {@code null}.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     * @see #list
     * @see #isDirectory
     */
    public File[] listFiles() {
        String[] tempNames = list();
        if (tempNames == null) {
            return null;
        }
        int resultLength = tempNames.length;
        File results[] = new File[resultLength];
        for (int i = 0; i < resultLength; i++) {
            results[i] = new File(this, tempNames[i]);
        }
        return results;
    }

    /**
     * Gets a list of the files in the directory represented by this file. This
     * list is then filtered through a FilenameFilter and files with matching
     * names are returned as an array of files. Returns {@code null} if this
     * file is not a directory. If {@code filter} is {@code null} then all
     * filenames match.
     * <p>
     * The entries {@code .} and {@code ..} representing the current and parent
     * directories are not returned as part of the list.
     *
     * @param filter
     *            the filter to match names against, may be {@code null}.
     * @return an array of files or {@code null}.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     * @see #list(FilenameFilter filter)
     * @see #getPath
     * @see #isDirectory
     * @see java.lang.SecurityManager#checkRead(FileDescriptor)
     */
    public File[] listFiles(FilenameFilter filter) {
        String[] tempNames = list(filter);
        if (tempNames == null) {
            return null;
        }
        int resultLength = tempNames.length;
        File results[] = new File[resultLength];
        for (int i = 0; i < resultLength; i++) {
            results[i] = new File(this, tempNames[i]);
        }
        return results;
    }

    /**
     * Gets a list of the files in the directory represented by this file. This
     * list is then filtered through a FileFilter and matching files are
     * returned as an array of files. Returns {@code null} if this file is not a
     * directory. If {@code filter} is {@code null} then all files match.
     * <p>
     * The entries {@code .} and {@code ..} representing the current and parent
     * directories are not returned as part of the list.
     *
     * @param filter
     *            the filter to match names against, may be {@code null}.
     * @return an array of files or {@code null}.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     * @see #getPath
     * @see #isDirectory
     * @see java.lang.SecurityManager#checkRead(FileDescriptor)
     */
    public File[] listFiles(FileFilter filter) {
        if (path.length() == 0) {
            return null;
        }

        String bs = properPath(true);
        if (!isDirectoryImpl(bs) || !existsImpl(bs) || isWriteOnlyImpl(bs)) {
            return null;
        }

        String[] implList = listImpl(bs);
        if (implList == null) {
            return new File[0];
        }
        List<File> tempResult = new ArrayList<File>();
        for (int index = 0; index < implList.length; index++) {
            String aName = new String(implList[index]);
            File aFile = new File(this, aName);
            if (filter == null || filter.accept(aFile)) {
                tempResult.add(aFile);
            }
        }
        return tempResult.toArray(new File[tempResult.size()]);
    }

    /**
     * Gets a list of the files in the directory represented by this file. This
     * list is then filtered through a FilenameFilter and the names of files
     * with matching names are returned as an array of strings. Returns
     * {@code null} if this file is not a directory. If {@code filter} is
     * {@code null} then all filenames match.
     * <p>
     * The entries {@code .} and {@code ..} representing the current and parent
     * directories are not returned as part of the list.
     *
     * @param filter
     *            the filter to match names against, may be {@code null}.
     * @return an array of files or {@code null}.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies read
     *             access to this file.
     * @see #getPath
     * @see #isDirectory
     * @see java.lang.SecurityManager#checkRead(FileDescriptor)
     */
    public java.lang.String[] list(FilenameFilter filter) {
        if (path.length() == 0) {
            return null;
        }

        String bs = properPath(true);
        if (!isDirectoryImpl(bs) || !existsImpl(bs) || isWriteOnlyImpl(bs)) {
            return null;
        }

        String[] implList = listImpl(bs);
        if (implList == null) {
            // empty list
            return new String[0];
        }
        List<String> tempResult = new ArrayList<String>();
        for (int index = 0; index < implList.length; index++) {
            String aName = new String(implList[index]);
            if (filter == null || filter.accept(this, aName)) {
                tempResult.add(aName);
            }
        }

        return tempResult.toArray(new String[tempResult.size()]);
    }

    private synchronized static native String[] listImpl(String path) /*-{
      NSArray *fileArray =
          [[NSFileManager defaultManager] contentsOfDirectoryAtPath:path error:nil];
      IOSObjectArray *files =
          [[IOSObjectArray alloc] initWithLength:[fileArray count]
                                            type:[IOSClass classWithClass:[NSString class]]];
#if ! __has_feature(objc_arc)
      [files autorelease];
#endif
      NSUInteger nFiles = [fileArray count];
      for (NSUInteger i = 0; i < nFiles; i++) {
        [files replaceObjectAtIndex:i withObject:[fileArray objectAtIndex:i]];
      }
      return files;
    }-*/;

    /**
     * Creates the directory named by the trailing filename of this file. Does
     * not create the complete path required to create this directory.
     *
     * @return {@code true} if the directory has been created, {@code false}
     *         otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies write
     *             access for this file.
     * @see #mkdirs
     */
    public boolean mkdir() {
       return mkdirImpl(properPath(true));
    }

    private native boolean mkdirImpl(String filePath) /*-{
      return [[NSFileManager defaultManager] createDirectoryAtPath:filePath
                                       withIntermediateDirectories:NO
                                                        attributes:nil
                                                             error:nil];
    }-*/;

    /**
     * Creates the directory named by the trailing filename of this file,
     * including the complete directory path required to create this directory.
     *
     * @return {@code true} if the necessary directories have been created,
     *         {@code false} if the target directory already exists or one of
     *         the directories can not be created.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies write
     *             access for this file.
     * @see #mkdir
     */
    public boolean mkdirs() {
        /* If the terminal directory already exists, answer false */
        if (exists()) {
            return false;
        }

        /* If the receiver can be created, answer true */
        if (mkdir()) {
            return true;
        }

        String parentDir = getParent();
        /* If there is no parent and we were not created, answer false */
        if (parentDir == null) {
            return false;
        }

        /* Otherwise, try to create a parent directory and then this directory */
        return (new File(parentDir).mkdirs() && mkdir());
    }

    /**
     * Creates a new, empty file on the file system according to the path
     * information stored in this file.
     *
     * @return {@code true} if the file has been created, {@code false} if it
     *         already exists.
     * @throws IOException
     *             if an I/O error occurs or the directory does not exist where
     *             the file should have been created.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies write
     *             access for this file.
     */
    public boolean createNewFile() throws IOException {
        if (0 == path.length()) {
            throw new IOException("No such file or directory");
        }
        int result = newFileImpl(properPath(true));
        switch (result) {
            case 0:
                return true;
            case 1:
                return false;
            default:
                throw new IOException("Cannot create: " + path);
        }
    }

    private native int newFileImpl(String filePath) /*-{
      if ([self existsImplWithNSString:filePath]) {
        return 1;
      }
      if ([[NSFileManager defaultManager] createFileAtPath:filePath
                                                  contents:[NSData data]
                                                attributes:nil]) {
        return 0;
      }
      NSException *e = [[JavaIoIOException alloc] init];
#if ! __has_feature(objc_arc)
      [e autorelease];
#endif
      @throw e;
    }-*/;

    /**
     * Creates an empty temporary file using the given prefix and suffix as part
     * of the file name. If suffix is {@code null}, {@code .tmp} is used. This
     * method is a convenience method that calls
     * {@link #createTempFile(String, String, File)} with the third argument
     * being {@code null}.
     *
     * @param prefix
     *            the prefix to the temp file name.
     * @param suffix
     *            the suffix to the temp file name.
     * @return the temporary file.
     * @throws IOException
     *             if an error occurs when writing the file.
     */
    public static File createTempFile(String prefix, String suffix)
            throws IOException {
        return createTempFile(prefix, suffix, null);
    }

    /**
     * Creates an empty temporary file in the given directory using the given
     * prefix and suffix as part of the file name.
     *
     * @param prefix
     *            the prefix to the temp file name.
     * @param suffix
     *            the suffix to the temp file name.
     * @param directory
     *            the location to which the temp file is to be written, or
     *            {@code null} for the default location for temporary files,
     *            which is taken from the "java.io.tmpdir" system property. It
     *            may be necessary to set this property to an existing, writable
     *            directory for this method to work properly.
     * @return the temporary file.
     * @throws IllegalArgumentException
     *             if the length of {@code prefix} is less than 3.
     * @throws IOException
     *             if an error occurs when writing the file.
     */
    @SuppressWarnings("nls")
    public static File createTempFile(String prefix, String suffix,
            File directory) throws IOException {
        // Force a prefix null check first
        if (prefix.length() < 3) {
            throw new IllegalArgumentException("Prefix must be at least 3 characters");
        }
        String newSuffix = suffix == null ? ".tmp" : suffix;
        File tmpDirFile;
        if (directory == null) {
            String tmpDir = System.getProperty("java.io.tmpdir", ".");
            tmpDirFile = new File(tmpDir);
        } else {
            tmpDirFile = directory;
        }
        File result;
        do {
            result = genTempFile(prefix, newSuffix, tmpDirFile);
        } while (!result.createNewFile());
        return result;
    }

    private static File genTempFile(String prefix, String suffix, File directory) {
        int identify = 0;
        synchronized (tempFileLocker) {
            if (counter == 0) {
                int newInt = new Random().nextInt();
                counter = ((newInt / 65535) & 0xFFFF) + 0x2710;
                counterBase = counter;
            }
            identify = counter++;
        }

        StringBuilder newName = new StringBuilder();
        newName.append(prefix);
        newName.append(counterBase);
        newName.append(identify);
        newName.append(suffix);
        return new File(directory, newName.toString());
    }

    /**
     * Returns a string representing the proper path for this file. If this file
     * path is absolute, the user.dir property is not prepended, otherwise it
     * is.
     *
     * @param internal
     *            is user.dir internal.
     * @return the proper path.
     */
    String properPath(boolean internal) {
        if (properPath != null) {
            return properPath;
        }

        if (isAbsolute()) {
            return path;
        }
        // Check security by getting user.dir when the path is not absolute
        String userdir;
        userdir = System.getProperty("user.dir"); //$NON-NLS-1$

        if (path.length() == 0) {
            return properPath = userdir;
        }
        int length = userdir.length();

        // Handle separator
        String result = userdir;
        if (userdir.charAt(length - 1) != separatorChar) {
            if (path.charAt(0) != separatorChar) {
                result += separator;
            }
        } else if (path.charAt(0) == separatorChar) {
            result = result.substring(0, length - 2);

        }
        result += path;
        return properPath = result;
    }

    /**
     * Renames this file to the name represented by the {@code dest} file. This
     * works for both normal files and directories.
     *
     * @param dest
     *            the file containing the new name.
     * @return {@code true} if the File was renamed, {@code false} otherwise.
     * @throws SecurityException
     *             if a {@code SecurityManager} is installed and it denies write
     *             access for this file or the {@code dest} file.
     */
    public boolean renameTo(java.io.File dest) {
        return renameToImpl(properPath(true), dest.properPath(true));
    }

    private native boolean renameToImpl(String pathExist, String pathNew) /*-{
      return [[NSFileManager defaultManager] moveItemAtPath:pathExist toPath:pathNew error:nil];
    }-*/;

    /**
     * Returns a string containing a concise, human-readable description of this
     * file.
     *
     * @return a printable representation of this file.
     */
    @Override
    public String toString() {
        return path;
    }

    /**
     * Returns a Uniform Resource Identifier for this file. The URI is system
     * dependent and may not be transferable between different operating / file
     * systems.
     *
     * @return an URI for this file.
     */
    /* TODO(user): enable when java.net support is implemented.
    @SuppressWarnings("nls")
    public URI toURI() {
        String name = getAbsoluteName();
        try {
            if (!name.startsWith("/")) {
                // start with sep.
                return new URI("file", null, new StringBuilder(
                        name.length() + 1).append('/').append(name).toString(),
                        null, null);
            } else if (name.startsWith("//")) {
                return new URI("file", "", name, null); // UNC path
            }
            return new URI("file", null, name, null, null);
        } catch (URISyntaxException e) {
            // this should never happen
            return null;
        }
    }
    */

    /**
     * Returns a Uniform Resource Locator for this file. The URL is system
     * dependent and may not be transferable between different operating / file
     * systems.
     *
     * @return a URL for this file.
     * @throws java.net.MalformedURLException
     *             if the path cannot be transformed into a URL.
     */
    /* TODO(user): enable when java.net support is implemented.
    @SuppressWarnings("nls")
    public URL toURL() throws java.net.MalformedURLException {
        String name = getAbsoluteName();
        if (!name.startsWith("/")) {
            // start with sep.
            return new URL(
                    "file", EMPTY_STRING, -1, new StringBuilder(name.length() + 1) //$NON-NLS-1$
                            .append('/').append(name).toString(), null);
        } else if (name.startsWith("//")) {
            return new URL("file:" + name); // UNC path
        }
        return new URL("file", EMPTY_STRING, -1, name, null);
    }
    */

    private String getAbsoluteName() {
        File f = getAbsoluteFile();
        String name = f.getPath();

        if (f.isDirectory() && name.charAt(name.length() - 1) != separatorChar) {
            // Directories must end with a slash
            name = new StringBuilder(name.length() + 1).append(name)
                    .append('/').toString();
        }
        return name;
    }
}
