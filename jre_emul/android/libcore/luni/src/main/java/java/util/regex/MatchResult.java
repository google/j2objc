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

package java.util.regex;

/**
 * Holds the results of a successful match of a {@link Pattern} against a
 * given string. The result is divided into groups, with one group for each
 * pair of parentheses in the regular expression and an additional group for
 * the whole regular expression. The start, end, and contents of each group
 * can be queried.
 *
 * @see Matcher
 * @see Matcher#toMatchResult()
 */
public interface MatchResult {

    /**
     * Returns the index of the first character following the text that matched
     * the whole regular expression.
     *
     * @return the character index.
     */
    int end();

    /**
     * Returns the index of the first character following the text that matched
     * a given group.
     *
     * @param group
     *            the group, ranging from 0 to groupCount() - 1, with 0
     *            representing the whole pattern.
     *
     * @return the character index.
     */
    int end(int group);

    /**
     * Returns the text that matched the whole regular expression.
     *
     * @return the text.
     */
    String group();

    /**
     * Returns the text that matched a given group of the regular expression.
     *
     * @param group
     *            the group, ranging from 0 to groupCount() - 1, with 0
     *            representing the whole pattern.
     *
     * @return the text that matched the group.
     */
    String group(int group);

    /**
     * Returns the number of groups in the result, which is always equal to
     * the number of groups in the original regular expression.
     *
     * @return the number of groups.
     */
    int groupCount();

    /**
     * Returns the index of the first character of the text that matched
     * the whole regular expression.
     *
     * @return the character index.
     */
    int start();

    /**
     * Returns the index of the first character of the text that matched a given
     * group.
     *
     * @param group
     *            the group, ranging from 0 to groupCount() - 1, with 0
     *            representing the whole pattern.
     *
     * @return the character index.
     */
    int start(int group);
}
