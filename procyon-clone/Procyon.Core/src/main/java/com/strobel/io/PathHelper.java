/*
 * PathHelper.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.io;

import com.strobel.core.ArrayUtilities;
import com.strobel.core.StringComparison;
import com.strobel.core.StringUtilities;
import com.strobel.util.ContractUtils;

import java.io.File;
import java.io.IOException;

public final class PathHelper {
    public static final char DirectorySeparator;
    public static final char AlternateDirectorySeparator;
    public static final char VolumeSeparator;

    private static final int maxPath = 260;
    private static final int maxDirectoryLength = 255;

    private static final char[] invalidPathCharacters = {
        '\"', '<', '>', '|', '\0', 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
        11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,
        26, 27, 28, 29, 30, 31
    };

    private static final char[] invalidFileNameCharacters = {
        '\"', '<', '>', '|', '\0', 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
        11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,
        26, 27, 28, 29, 30, 31, ':', '*', '?', '\\', '/'
    };

    private static final char[] trimEndChars = {
        0x9, 0xA, 0xB, 0xC, 0xD, 0x20, 0x85, 0xA0
    };

    private static final boolean isWindows;

    static {
        final String osName = System.getProperty("os.name");

        isWindows = osName != null && StringUtilities.startsWithIgnoreCase(osName, "windows");

        if (isWindows) {
            DirectorySeparator = '\\';
            AlternateDirectorySeparator = '/';
            VolumeSeparator = ':';
        }
        else {
            DirectorySeparator = '/';
            AlternateDirectorySeparator = '\\';
            VolumeSeparator = '/';
        }
    }

    private PathHelper() {
        throw ContractUtils.unreachable();
    }

    public static char[] getInvalidPathCharacters() {
        return invalidPathCharacters.clone();
    }

    public static char[] getInvalidFileNameCharacters() {
        return invalidFileNameCharacters;
    }

    public static boolean isPathRooted(final String path) {
        if (StringUtilities.isNullOrEmpty(path)) {
            return false;
        }

        final int length = path.length();

        return path.charAt(0) == DirectorySeparator ||
               path.charAt(0) == AlternateDirectorySeparator ||
               isWindows && length >= 2 && path.charAt(1) == VolumeSeparator;
    }

    public static String combine(final String path1, final String path2) {
        if (path1 == null) {
            return path2 != null ? path2 : StringUtilities.EMPTY;
        }

        if (path2 == null) {
            return path1;
        }

        checkInvalidPathChars(path1);
        checkInvalidPathChars(path2);

        return combineUnsafe(path1, path2);
    }

    public static String combine(final String path1, final String path2, final String path3) {
        return combine(combine(path1, path2), path3);
    }

    public static String combine(final String... paths) {
        if (ArrayUtilities.isNullOrEmpty(paths)) {
            return StringUtilities.EMPTY;
        }

        int finalSize = 0;
        int firstComponent = 0;

        //
        // We have two passes, the first calculates how large a buffer to allocate and
        // does some precondition checks on the paths passed in.  The second actually
        // performs the concatenation.
        //

        for (int i = 0; i < paths.length; i++) {
            final String path = paths[i];

            if (StringUtilities.isNullOrEmpty(path)) {
                continue;
            }

            checkInvalidPathChars(path);

            final int length = path.length();

            if (isPathRooted(path)) {
                firstComponent = i;
                finalSize = length;
            }
            else {
                finalSize += length;
            }

            final char ch = path.charAt(length - 1);

            if (ch != DirectorySeparator &&
                ch != AlternateDirectorySeparator &&
                ch != VolumeSeparator) {

                finalSize++;
            }
        }

        if (finalSize == 0) {
            return StringUtilities.EMPTY;
        }

        final StringBuilder finalPath = new StringBuilder(finalSize);

        for (int i = firstComponent; i < paths.length; i++) {
            final String path = paths[i];

            if (StringUtilities.isNullOrEmpty(path)) {
                continue;
            }

            final int length = finalPath.length();

            if (length == 0) {
                finalPath.append(path);
            }
            else {
                final char ch = finalPath.charAt(length - 1);

                if (ch != DirectorySeparator &&
                    ch != AlternateDirectorySeparator &&
                    ch != VolumeSeparator) {

                    finalPath.append(DirectorySeparator);
                }

                finalPath.append(path);
            }
        }

        return finalPath.toString();
    }

    public static String getDirectoryName(final String path) {
        if (StringUtilities.isNullOrEmpty(path)) {
            return StringUtilities.EMPTY;
        }

        checkInvalidPathChars(path);

        final String normalizedPath = normalizePath(path, false, maxPath);
        final int root = getRootLength(normalizedPath);

        int i = normalizedPath.length();

        if (i > root) {
            i = normalizedPath.length();

            if (i == root) {
                return null;
            }

            while (i > root && !isDirectorySeparator(normalizedPath.charAt(--i))) {
            }

            return normalizedPath.substring(0, i);
        }

        return normalizedPath;
    }

    public static String getFileName(final String path) {
        if (StringUtilities.isNullOrEmpty(path)) {
            return StringUtilities.EMPTY;
        }

        checkInvalidPathChars(path);

        final int length = path.length();

        for (int i = length; --i >= 0; ) {
            final char ch = path.charAt(i);

            if (isDirectorySeparator(ch) || ch == VolumeSeparator) {
                return path.substring(i + 1, length);
            }
        }

        return path;
    }

    public static String getFileNameWithoutExtension(final String path) {
        final String fileName = getFileName(path);

        if (StringUtilities.isNullOrEmpty(fileName)) {
            return fileName;
        }

        if (fileName != null) {
            final int dotPosition = fileName.lastIndexOf('.');

            if (dotPosition == -1) {
                return fileName;
            }
            else {
                return fileName.substring(0, dotPosition);
            }
        }

        return null;
    }

    public static String getFullPath(final String path) {
        if (StringUtilities.isNullOrEmpty(path)) {
            return StringUtilities.EMPTY;
        }

        return normalizePath(path, true, maxPath);
    }
    
    public static String getTempPath() {
        return getFullPath(System.getProperty("java.io.tmpdir"));
    }

    private static String combineUnsafe(final String path1, final String path2) {
        if (path2.length() == 0) {
            return path1;
        }

        if (path1.length() == 0) {
            return path2;
        }

        if (isPathRooted(path2)) {
            return path2;
        }

        final char ch = path1.charAt(path1.length() - 1);

        if (ch != DirectorySeparator &&
            ch != AlternateDirectorySeparator &&
            ch != VolumeSeparator) {

            return path1 + DirectorySeparator + path2;
        }

        return path1 + path2;
    }

    private static void checkInvalidPathChars(final String path) {
        if (!isWindows && path.length() >= 2 && path.charAt(0) == '\\' && path.charAt(1) == '\\') {
            throw Error.invalidPathCharacters();
        }

        for (int i = 0; i < path.length(); i++) {
            final int ch = path.charAt(i);

            if (ch == '\"' || ch == '<' || ch == '>' || ch == '|' || ch < 32) {
                throw Error.invalidPathCharacters();
            }
        }
    }

    private static boolean isDirectorySeparator(final char ch) {
        return ch == DirectorySeparator || ch == AlternateDirectorySeparator;
    }

    private static int getRootLength(final String path) {
        checkInvalidPathChars(path);

        int i = 0;
        final int length = path.length();

        if (isWindows) {
            if (length >= 1 && (isDirectorySeparator(path.charAt(0)))) {
                //
                // Handles UNC names and directories off current volume's root.
                //
                i = 1;

                if (length >= 2 && (isDirectorySeparator(path.charAt(1)))) {
                    i = 2;
                    int n = 2;

                    while (i < length && (!isDirectorySeparator(path.charAt(i)) || --n > 0)) {
                        i++;
                    }
                }
            }
            else if (length >= 2 && path.charAt(1) == VolumeSeparator) {
                //
                // Handles C:\foo.
                //
                i = 2;

                if (length >= 3 && (isDirectorySeparator(path.charAt(2)))) {
                    i++;
                }
            }
        }
        else if (length >= 1 && (isDirectorySeparator(path.charAt(0)))) {
            i = 1;
        }

        return i;
    }

    @SuppressWarnings("ConstantConditions")
    private static String normalizePath(final String p, final boolean fullCheck, final int maxPathLength) {
        final String path;

        //
        // If we're doing a full path check, trim whitespace and look for illegal
        // path characters.
        //
        if (fullCheck) {
            path = StringUtilities.trimAndRemoveRight(p, trimEndChars);
            checkInvalidPathChars(path);
        }
        else {
            path = p;
        }

        int index = 0;

        final StringBuilder newBuffer = new StringBuilder(path.length() + maxPath);

        int spaceCount = 0;
        int dotCount = 0;

        boolean fixupDirectorySeparator = false;

        //
        // Number of significant chars other than potentially suppressible
        // dots and spaces since the last directory or volume separator.
        //
        int significantCharCount = 0;
        int lastSignificantChar = -1; // Index of last significant character.

        //
        // Whether this segment of the path (not the complete path) started
        // with a volume separator character.  Reject "c:...".
        //
        boolean startedWithVolumeSeparator = false;
        boolean firstSegment = true;

        int lastSeparatorPosition = 0;

        if (isWindows) {
            //
            // This code is here for Windows backwards compatibility reasons. It
            // ensures that "\\foo.cs\bar.cs" stays "\\foo.cs\bar.cs" instead of
            // being turned into "\foo.cs\bar.cs".
            //
            if (path.length() > 0 && (path.charAt(0) == DirectorySeparator || path.charAt(0) == AlternateDirectorySeparator)) {
                newBuffer.append('\\');
                index++;
                lastSignificantChar = 0;
            }
        }

        //
        // Normalize the string, stripping out redundant dots, spaces, and slashes.
        //
        while (index < path.length()) {
            final char currentChar = path.charAt(index);

            //
            // We handle both directory separators and dots specially.  For directory
            // separators, we consume consecutive appearances.  For dots, we consume
            // all dots beyond the second in succession.  All other characters are
            // added as-is.  In addition we consume all spaces after the last other
            // character in a directory name up until the directory separator.
            //

            if (currentChar == DirectorySeparator || currentChar == AlternateDirectorySeparator) {
                //
                // If we have a path like "123.../foo", remove the trailing dots.
                // However, if we found "c:\temp\..\bar" or "c:\temp\...\bar", don't.
                // Also,remove trailing spaces from both files and directory names.
                //
                // If we saw a '\' as the previous last significant character and are
                // simply going to write out dots, suppress them.  If we only contain
                // dots and slashes though, only allow a string like [dot]+ [space]*;
                // ignore everything else.
                //
                // Legal: "\.. \", "\...\", "\. \"
                // Illegal: "\.. .\", "\. .\", "\ .\"
                //
                if (significantCharCount == 0) {
                    //
                    // Dot and space handling...
                    //
                    if (dotCount > 0) {
                        //
                        // Look for ".[space]*" or "..[space]*".
                        //
                        final int start = lastSignificantChar + 1;

                        if (path.charAt(start) != '.') {
                            throw Error.illegalPath();
                        }

                        //
                        // Only allow "[dot]+[space]*", and normalize the legal ones to
                        // "." or "..".
                        //
                        if (dotCount >= 2) {
                            //
                            // Reject "C:...".
                            //
                            if (startedWithVolumeSeparator && dotCount > 2) {
                                throw Error.illegalPath();
                            }

                            if (path.charAt(start + 1) == '.') {
                                //
                                // Search for a space in the middle of the  dots and throw.
                                //
                                for (int i = start + 2; i < start + dotCount; i++) {
                                    if (path.charAt(i) != '.') {
                                        throw Error.illegalPath();
                                    }
                                }

                                dotCount = 2;
                            }
                            else {
                                if (dotCount > 1) {
                                    throw Error.illegalPath();
                                }
                                dotCount = 1;
                            }
                        }

                        if (dotCount == 2) {
                            newBuffer.append('.');
                        }

                        newBuffer.append('.');
                        fixupDirectorySeparator = false;

                        //
                        // Continue in this case, potentially writing out '\'.
                        //
                    }

                    if (spaceCount > 0 && firstSegment) {
                        //
                        // Handle strings like " \\server\share".
                        //
                        if (index + 1 < path.length() &&
                            (path.charAt(index + 1) == DirectorySeparator ||
                             path.charAt(index + 1) == AlternateDirectorySeparator)) {

                            newBuffer.append(DirectorySeparator);
                        }
                    }
                }

                dotCount = 0;

                //
                // Suppress trailing spaces.
                //
                spaceCount = 0;

                if (!fixupDirectorySeparator) {
                    fixupDirectorySeparator = true;
                    newBuffer.append(DirectorySeparator);
                }

                significantCharCount = 0;
                lastSignificantChar = index;
                startedWithVolumeSeparator = false;
                firstSegment = false;

                final int thisPos = newBuffer.length() - 1;

                if (thisPos - lastSeparatorPosition > maxDirectoryLength) {
                    throw Error.pathTooLong();
                }

                lastSeparatorPosition = thisPos;
            }
            else if (currentChar == '.') {
                //
                // Reduce only multiple .'s only after slash to 2 dots. For
                // instance a...b is a valid file name.
                //
                dotCount++;
                //
                // Don't flush out non-terminal spaces here, because they may in
                // the end not be significant.  Turn "c:\ . .\foo" -> "c:\foo"
                // which is the conclusion of removing trailing dots & spaces,
                // as well as folding multiple '\' characters.
                //
            }
            else if (currentChar == ' ') {
                spaceCount++;
            }
            else {
                //
                // Normal character logic...
                //

                fixupDirectorySeparator = false;

                //
                // To reject strings like "C:...\foo" and "C  :\foo".
                //
                if (isWindows && firstSegment && currentChar == VolumeSeparator) {
                    //
                    // Only accept "C:", not "c :" or ":".  Get a drive letter or ' '
                    // if index is 0.
                    //
                    final char driveLetter = index > 0 ? path.charAt(index - 1) : ' ';
                    final boolean validPath = dotCount == 0 && significantCharCount >= 1 && driveLetter != ' ';

                    if (!validPath) {
                        throw Error.illegalPath();
                    }

                    startedWithVolumeSeparator = true;

                    //
                    // We need special logic to make " c:" work; we should not fix
                    // paths like "  foo::$DATA".
                    //
                    if (significantCharCount > 1) {
                        //
                        // How many spaces did we write out?  Note that spaceCount has
                        // already been reset.
                        //
                        int tempSpaceCount = 0;

                        while (tempSpaceCount < newBuffer.length() &&
                               newBuffer.charAt(tempSpaceCount) == ' ') {

                            tempSpaceCount++;
                        }

                        if (significantCharCount - tempSpaceCount == 1) {
                            //
                            // Overwrite spaces, we need a special case to not break
                            // "  foo" as a relative path.
                            //
                            newBuffer.setLength(0);
                            newBuffer.append(driveLetter);
                        }
                    }

                    significantCharCount = 0;
                }
                else {
                    significantCharCount += 1 + dotCount + spaceCount;
                }

                //
                // Copy any spaces & dots since the last significant character to here.
                // Note we only counted the number of dots and spaces, and don't know
                // what order they're in (hence the copy).
                //
                if (dotCount > 0 || spaceCount > 0) {
                    final int copyLength = (lastSignificantChar >= 0)
                                           ? index - lastSignificantChar - 1
                                           : index;

                    if (copyLength > 0) {
                        for (int i = 0; i < copyLength; i++) {
                            newBuffer.append(path.charAt(lastSignificantChar + 1 + i));
                        }
                    }

                    dotCount = 0;
                    spaceCount = 0;
                }

                newBuffer.append(currentChar);
                lastSignificantChar = index;
            }

            index++;
        }

        if (newBuffer.length() - 1 - lastSeparatorPosition > maxDirectoryLength) {
            throw Error.pathTooLong();
        }

        //
        // Drop any trailing dots and spaces from file and directory names;
        // note that we MUST make sure that "C:\foo\.." is correctly handled.
        //
        // Also, handle "C:\foo\." -> "C:\foo", while "C:\." -> "C:\".
        //
        if (significantCharCount == 0) {
            if (dotCount > 0) {
                // Look for ".[space]*" or "..[space]*"
                final int start = lastSignificantChar + 1;

                if (path.charAt(start) != '.') {
                    throw Error.illegalPath();
                }

                //
                // Only allow "[dot]+[space]*", and normalize the legal ones to
                // "." or "..".
                //
                if (dotCount >= 2) {
                    //
                    // Reject "C:...".
                    //
                    if (startedWithVolumeSeparator && dotCount > 2) {
                        throw Error.illegalPath();
                    }

                    if (path.charAt(start + 1) == '.') {
                        //
                        // Search for a space in the middle of the dots and throw.
                        //
                        for (int i = start + 2; i < start + dotCount; i++) {
                            if (path.charAt(i) != '.') {
                                throw Error.illegalPath();
                            }
                        }

                        dotCount = 2;
                    }
                    else {
                        if (dotCount > 1) {
                            throw Error.illegalPath();
                        }
                        dotCount = 1;
                    }
                }

                if (dotCount == 2) {
                    newBuffer.append("..");
                }
                else if (start == 0) {
                    newBuffer.append('.');
                }
            }
        }

        //
        // If we ended up eating all the characters, bail out.
        //
        if (newBuffer.length() == 0) {
            throw Error.illegalPath();
        }

        //
        // Disallow URLs here.
        //
        if (fullCheck) {
            if (StringUtilities.startsWith(newBuffer, "http:") ||
                StringUtilities.startsWith(newBuffer, "file:")) {

                throw Error.pathUriFormatNotSupported();
            }
        }

        int normalizedLength = newBuffer.length();

        //
        // Throw an exception for paths like "\\", "\\server", and "\\server\".
        // This check can only be properly done after normalizing, so "\\foo\.."
        // will be properly rejected.
        //
        if (normalizedLength > 1 &&
            newBuffer.charAt(0) == '\\' &&
            newBuffer.charAt(1) == '\\') {

            int startIndex = 2;

            while (startIndex < normalizedLength) {
                if (newBuffer.charAt(startIndex) == '\\') {
                    startIndex++;
                    break;
                }
                else {
                    startIndex++;
                }
            }

            if (startIndex == normalizedLength) {
                throw Error.illegalUncPath();
            }
        }

        //
        // Use the JDK to call the native API for the final canonicalization step.
        //
        if (fullCheck) {
            final String temp = newBuffer.toString();

            newBuffer.setLength(0);

            try {
                newBuffer.append(new File(temp).getCanonicalPath());
            }
            catch (IOException e) {
                throw Error.canonicalizationError(e);
            }

            normalizedLength = newBuffer.length();
        }

        // Check our result and form the managed string as necessary.
        if (newBuffer.length() >= maxPathLength) {
            throw Error.pathTooLong();
        }

        if (normalizedLength == 0) {
            return StringUtilities.EMPTY;
        }

        String returnVal = newBuffer.toString();

        if (StringUtilities.equals(returnVal, path, StringComparison.Ordinal)) {
            returnVal = path;
        }

        return returnVal;
    }
}

final class Error {
    private Error() {
        throw ContractUtils.unreachable();
    }

    static IllegalArgumentException invalidPathCharacters() {
        return new IllegalArgumentException("Path contains invalid characters.");
    }

    static IllegalArgumentException illegalPath() {
        return new IllegalArgumentException("Specified capacity must not be less than the current capacity.");
    }

    static IllegalArgumentException pathUriFormatNotSupported() {
        return new IllegalArgumentException("URI formats are not supported.");
    }

    static IllegalArgumentException illegalUncPath() {
        return new IllegalArgumentException("The UNC path should be of the form \\\\server\\share.");
    }

    static IllegalArgumentException pathTooLong() {
        return new IllegalArgumentException(
            "The specified path, file name, or both are too long. The fully qualified" +
            " file name must be less than 260 characters, and the directory name must" +
            " be less than 248 characters.");
    }

    static IllegalArgumentException canonicalizationError(final Throwable t) {
        return new IllegalArgumentException(t.getMessage());
    }
}
