/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.net.url;

import java.util.Locale;

public final class UrlUtils {
    private UrlUtils() {
    }

    /**
     * Returns the path will relative path segments like ".." and "." resolved.
     * The returned path will not necessarily start with a "/" character. This
     * handles ".." and "." segments at both the beginning and end of the path.
     *
     * @param discardRelativePrefix true to remove leading ".." segments from
     *     the path. This is appropriate for paths that are known to be
     *     absolute.
     */
    public static String canonicalizePath(String path, boolean discardRelativePrefix) {
        // the first character of the current path segment
        int segmentStart = 0;

        // the number of segments seen thus far that can be erased by sequences of '..'.
        int deletableSegments = 0;

        for (int i = 0; i <= path.length(); ) {
            int nextSegmentStart;
            if (i == path.length()) {
                nextSegmentStart = i;
            } else if (path.charAt(i) == '/') {
                nextSegmentStart = i + 1;
            } else {
                i++;
                continue;
            }

            /*
             * We've encountered either the end of a segment or the end of the
             * complete path. If the final segment was "." or "..", remove the
             * appropriate segments of the path.
             */
            if (i == segmentStart + 1 && path.regionMatches(segmentStart, ".", 0, 1)) {
                // Given "abc/def/./ghi", remove "./" to get "abc/def/ghi".
                path = path.substring(0, segmentStart) + path.substring(nextSegmentStart);
                i = segmentStart;
            } else if (i == segmentStart + 2 && path.regionMatches(segmentStart, "..", 0, 2)) {
                if (deletableSegments > 0 || discardRelativePrefix) {
                    // Given "abc/def/../ghi", remove "def/../" to get "abc/ghi".
                    deletableSegments--;
                    int prevSegmentStart = path.lastIndexOf('/', segmentStart - 2) + 1;
                    path = path.substring(0, prevSegmentStart) + path.substring(nextSegmentStart);
                    i = segmentStart = prevSegmentStart;
                } else {
                    // There's no segment to delete; this ".." segment must be retained.
                    i++;
                    segmentStart = i;
                }
            } else {
                if (i > 0) {
                    deletableSegments++;
                }
                i++;
                segmentStart = i;
            }
        }
        return path;
    }

    /**
     * Returns a path that can be safely concatenated with {@code authority}. If
     * the authority is null or empty, this can be any path. Otherwise the paths
     * run together like {@code http://android.comindex.html}.
     */
    public static String authoritySafePath(String authority, String path) {
        if (authority != null && !authority.isEmpty() && !path.isEmpty() && !path.startsWith("/")) {
            return "/" + path;
        }
        return path;
    }

    /**
     * Returns the scheme prefix like "http" from the URL spec, or null if the
     * spec doesn't start with a scheme. Scheme prefixes match this pattern:
     * {@code alpha ( alpha | digit | '+' | '-' | '.' )* ':'}
     */
    public static String getSchemePrefix(String spec) {
        int colon = spec.indexOf(':');

        if (colon < 1) {
            return null;
        }

        for (int i = 0; i < colon; i++) {
            char c = spec.charAt(i);
            if (!isValidSchemeChar(i, c)) {
                return null;
            }
        }

        return spec.substring(0, colon).toLowerCase(Locale.US);
    }

    public static boolean isValidSchemeChar(int index, char c) {
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
            return true;
        }
        if (index > 0 && ((c >= '0' && c <= '9') || c == '+' || c == '-' || c == '.')) {
            return true;
        }
        return false;
    }

    /**
     * Returns the index of the first char of {@code chars} in {@code string}
     * bounded between {@code start} and {@code end}. This returns {@code end}
     * if none of the characters exist in the requested range.
     */
    public static int findFirstOf(String string, String chars, int start, int end) {
        for (int i = start; i < end; i++) {
            char c = string.charAt(i);
            if (chars.indexOf(c) != -1) {
                return i;
            }
        }
        return end;
    }
}
