/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2001-2015, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */

package android.icu.impl.data;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import android.icu.impl.ICUData;
import android.icu.impl.PatternProps;

/**
 * A reader for text resource data in the current package or the package
 * of a given class object.  The
 * resource data is loaded through the class loader, so it will
 * typically be a file in the same directory as the *.class files, or
 * a file within a JAR file in the corresponding subdirectory.  The
 * file must be a text file in one of the supported encodings; when the
 * resource is opened by constructing a <code>ResourceReader</code>
 * object the encoding is specified.
 *
 * <p>2015-sep-03 TODO: Only used in android.icu.dev.test.format, move there.
 *
 * @author Alan Liu
 * @hide Only a subset of ICU is exposed in Android
 */
public class ResourceReader implements Closeable {
    private BufferedReader reader = null;
    private String resourceName;
    private String encoding; // null for default encoding
    private Class<?> root;

    /**
     * The one-based line number. Has the special value -1 before the
     * object is initialized. Has the special value 0 after initialization
     * but before the first line is read.
     */
    private int lineNo;

    /**
     * Construct a reader object for the text file of the given name
     * in this package, using the given encoding.
     * @param resourceName the name of the text file located in this
     * package's ".data" subpackage.
     * @param encoding the encoding of the text file; if unsupported
     * an exception is thrown
     * @exception UnsupportedEncodingException if
     * <code>encoding</code> is not supported by the JDK.
     */
    public ResourceReader(String resourceName, String encoding)
        throws UnsupportedEncodingException {
        this(ICUData.class, "data/" + resourceName, encoding);
    }

    /**
     * Construct a reader object for the text file of the given name
     * in this package, using the default encoding.
     * @param resourceName the name of the text file located in this
     * package's ".data" subpackage.
     */
    public ResourceReader(String resourceName) {
        this(ICUData.class, "data/" + resourceName);
    }

    /**
     * Construct a reader object for the text file of the given name
     * in the given class's package, using the given encoding.
     * @param resourceName the name of the text file located in the
     * given class's package.
     * @param encoding the encoding of the text file; if unsupported
     * an exception is thrown
     * @exception UnsupportedEncodingException if
     * <code>encoding</code> is not supported by the JDK.
     */
    public ResourceReader(Class<?> rootClass, String resourceName, String encoding)
        throws UnsupportedEncodingException {
        this.root = rootClass;
        this.resourceName = resourceName;
        this.encoding = encoding;
        lineNo = -1;
        _reset();
    }

         /**
          * Construct a reader object for the input stream associated with
          * the given resource name.
          * @param is the input stream of the resource
          * @param resourceName the name of the resource
          */
          public ResourceReader(InputStream is, String resourceName, String encoding) {
                   this.root = null;
         this.resourceName = resourceName;
         this.encoding = encoding;

         this.lineNo = -1;
         try {
             InputStreamReader isr = (encoding == null)
                 ? new InputStreamReader(is)
                 : new InputStreamReader(is, encoding);

             this.reader = new BufferedReader(isr);
             this.lineNo= 0;
         }
         catch (UnsupportedEncodingException e) {
         }
     }

          /**
           * Construct a reader object for the input stream associated with
           * the given resource name.
           * @param is the input stream of the resource
           * @param resourceName the name of the resource
           */
          public ResourceReader(InputStream is, String resourceName) {
              this(is, resourceName, null);
          }

    /**
     * Construct a reader object for the text file of the given name
     * in the given class's package, using the default encoding.
     * @param resourceName the name of the text file located in the
     * given class's package.
     */
    public ResourceReader(Class<?> rootClass, String resourceName) {
        this.root = rootClass;
        this.resourceName = resourceName;
        this.encoding = null;
        lineNo = -1;
        try {
            _reset();
        } catch (UnsupportedEncodingException e) {}
    }

    /**
     * Read and return the next line of the file or <code>null</code>
     * if the end of the file has been reached.
     */
    public String readLine() throws IOException {
        if (lineNo == 0) {
            // Remove BOMs
            ++lineNo;
            String line = reader.readLine();
            if (line != null && (line.charAt(0) == '\uFFEF' ||
                                 line.charAt(0) == '\uFEFF')) {
                line = line.substring(1);
            }
            return line;
        }
        ++lineNo;
        return reader.readLine();
    }

    /**
     * Read a line, ignoring blank lines and lines that start with
     * '#'.
     * @param trim if true then trim leading Pattern_White_Space.
     */
    public String readLineSkippingComments(boolean trim) throws IOException {
        for (;;) {
            String line = readLine();
            if (line == null) {
                return line;
            }
            // Skip over white space
            int pos = PatternProps.skipWhiteSpace(line, 0);
            // Ignore blank lines and comment lines
            if (pos == line.length() || line.charAt(pos) == '#') {
                continue;
            }
            // Process line
            if (trim) line = line.substring(pos);
            return line;
        }
    }


    /**
     * Read a line, ignoring blank lines and lines that start with
     * '#'. Do not trim leading Pattern_White_Space.
     */
    public String readLineSkippingComments() throws IOException {
        return readLineSkippingComments(false);
    }

    /**
     * Return the one-based line number of the last line returned by
     * readLine() or readLineSkippingComments(). Should only be called
     * after a call to one of these methods; otherwise the return
     * value is undefined.
     */
    public int getLineNumber() {
        return lineNo;
    }

    /**
     * Return a string description of the position of the last line
     * returned by readLine() or readLineSkippingComments().
     */
    public String describePosition() {
        return resourceName + ':' + lineNo;
    }

    /**
     * Reset this reader so that the next call to
     * <code>readLine()</code> returns the first line of the file
     * again.  This is a somewhat expensive call, however, calling
     * <code>reset()</code> after calling it the first time does
     * nothing if <code>readLine()</code> has not been called in
     * between.
     */
    public void reset() {
        try {
            _reset();
        } catch (UnsupportedEncodingException e) {}
        // We swallow this exception, if there is one.  If the encoding is
        // invalid, the constructor will have thrown this exception already and
        // the caller shouldn't use the object afterwards.
    }

    /**
     * Reset to the start by reconstructing the stream and readers.
     * We could also use mark() and reset() on the stream or reader,
     * but that would cause them to keep the stream data around in
     * memory.  We don't want that because some of the resource files
     * are large, e.g., 400k.
     */
    private void _reset() throws UnsupportedEncodingException {
        try {
            close();
        } catch (IOException e) {}
        if (lineNo == 0) {
            return;
        }
        InputStream is = ICUData.getStream(root, resourceName);
        if (is == null) {
            throw new IllegalArgumentException("Can't open " + resourceName);
        }

        InputStreamReader isr =
            (encoding == null) ? new InputStreamReader(is) :
                                 new InputStreamReader(is, encoding);
        reader = new BufferedReader(isr);
        lineNo = 0;
    }

    /**
     * Closes the underlying reader and releases any system resources
     * associated with it. If the stream is already closed then invoking
     * this method has no effect.
     */
    @Override
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
            reader = null;
        }
    }
}
