/*
 * Copyright (C) 2007 The Android Open Source Project
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

/**
 * Holds the results of a successful match of a regular expression against a
 * given string. Only used internally, thus sparsely documented (though the
 * defining public interface has full documentation).
 *
 * @see java.util.regex.MatchResult
 */
class MatchResultImpl implements MatchResult {

    /**
     * Holds the original input text.
     */
    private String text;

    /**
     * Holds the offsets of the groups in the input text. The first two
     * elements specifiy start and end of the zero group, the next two specify
     * group 1, and so on.
     */
    private int[] offsets;

    MatchResultImpl(String text, int[] offsets) {
        this.text = text;
        this.offsets = offsets.clone();
    }

    public int end() {
        return end(0);
    }

    public int end(int group) {
        return offsets[2 * group + 1];
    }

    public String group() {
        return text.substring(start(), end());
    }

    public String group(int group) {
        int from = offsets[group * 2];
        int to = offsets[(group * 2) + 1];
        if (from == -1 || to == -1) {
            return null;
        } else {
            return text.substring(from, to);
        }
    }

    public int groupCount() {
        return (offsets.length / 2) - 1;
    }

    public int start() {
        return start(0);
    }

    public int start(int group) {
        return offsets[2 * group];
    }

}
