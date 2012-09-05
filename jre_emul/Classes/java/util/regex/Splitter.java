/*
 * Copyright (C) 2010 The Android Open Source Project
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

package java.util.regex;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to make {@code String.split} fast (and to help {@code Pattern.split} too).
 * @hide
 */
public class Splitter {
    // The RI allows regular expressions beginning with ] or }, but that's probably a bug.
    private static final String METACHARACTERS = "\\?*+[](){}^$.|";

    private Splitter() {
    }

    /**
     * Returns a result equivalent to {@code s.split(separator, limit)} if it's able
     * to compute it more cheaply than ICU, or null if the caller should fall back to
     * using ICU.
     */
    public static String[] fastSplit(String re, String input, int limit) {
        // Can we do it cheaply?
        int len = re.length();
        if (len == 0) {
            return null;
        }
        char ch = re.charAt(0);
        if (len == 1 && METACHARACTERS.indexOf(ch) == -1) {
            // We're looking for a single non-metacharacter. Easy.
        } else if (len == 2 && ch == '\\') {
            // We're looking for a quoted character.
            // Quoted metacharacters are effectively single non-metacharacters.
            ch = re.charAt(1);
            if (METACHARACTERS.indexOf(ch) == -1) {
                return null;
            }
        } else {
            return null;
        }

        // We can do this cheaply...

        // Unlike Perl, which considers the result of splitting the empty string to be the empty
        // array, Java returns an array containing the empty string.
        if (input.isEmpty()) {
            return new String[] { "" };
        }

        // Collect text preceding each occurrence of the separator, while there's enough space.
        ArrayList<String> list = new ArrayList<String>();
        int maxSize = limit <= 0 ? Integer.MAX_VALUE : limit;
        int begin = 0;
        int end;
        while ((end = input.indexOf(ch, begin)) != -1 && list.size() + 1 < maxSize) {
            list.add(input.substring(begin, end));
            begin = end + 1;
        }
        return finishSplit(list, input, begin, maxSize, limit);
    }

    public static String[] split(Pattern pattern, String re, String input, int limit) {
        String[] fastResult = fastSplit(re, input, limit);
        if (fastResult != null) {
            return fastResult;
        }

        // Unlike Perl, which considers the result of splitting the empty string to be the empty
        // array, Java returns an array containing the empty string.
        if (input.isEmpty()) {
            return new String[] { "" };
        }

        // Collect text preceding each occurrence of the separator, while there's enough space.
        ArrayList<String> list = new ArrayList<String>();
        int maxSize = limit <= 0 ? Integer.MAX_VALUE : limit;
        Matcher matcher = new Matcher(pattern, input);
        int begin = 0;
        while (matcher.find() && list.size() + 1 < maxSize) {
            list.add(input.substring(begin, matcher.start()));
            begin = matcher.end();
        }
        return finishSplit(list, input, begin, maxSize, limit);
    }

    private static String[] finishSplit(List<String> list, String input, int begin, int maxSize, int limit) {
        // Add trailing text.
        if (begin < input.length()) {
            list.add(input.substring(begin));
        } else if (limit != 0) { // No point adding the empty string if limit == 0, just to remove it below.
            list.add("");
        }
        // Remove all trailing empty matches in the limit == 0 case.
        if (limit == 0) {
            int i = list.size() - 1;
            while (i >= 0 && list.get(i).isEmpty()) {
                list.remove(i);
                i--;
            }
        }
        // Convert to an array.
        return list.toArray(new String[list.size()]);
    }
}
