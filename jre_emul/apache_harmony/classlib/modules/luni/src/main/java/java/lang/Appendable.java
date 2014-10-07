/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang;

import java.io.IOException;

/**
 * Declares methods to append characters or character sequences. Any class that
 * implements this interface can receive data formatted by a
 * {@link java.util.Formatter}. The appended character or character sequence
 * should be valid according to the rules described in
 * {@link Character Unicode Character Representation}.
 * <p>
 * {@code Appendable} itself does not guarantee thread safety. This
 * responsibility is up to the implementing class.
 * <p>
 * Implementing classes can choose different exception handling mechanism. They
 * can choose to throw exceptions other than {@code IOException} or they do not
 * throw any exceptions at all and use error codes instead.
 */
public interface Appendable {

    /**
     * Appends the specified character.
     * 
     * @param c
     *            the character to append.
     * @return this {@code Appendable}.
     * @throws IOException
     *             if an I/O error occurs.
     */
    Appendable append(char c) throws IOException;

    /**
     * Appends the character sequence {@code csq}. Implementation classes may
     * not append the whole sequence, for example if the target is a buffer with
     * limited size.
     * <p>
     * If {@code csq} is {@code null}, the characters "null" are appended.
     *
     * @param csq
     *            the character sequence to append.
     * @return this {@code Appendable}.
     * @throws IOException
     *             if an I/O error occurs.
     */
    Appendable append(CharSequence csq) throws IOException;

    /**
     * Appends a subsequence of {@code csq}.
     * <p>
     * If {@code csq} is not {@code null} then calling this method is equivalent
     * to calling {@code append(csq.subSequence(start, end))}.
     * <p>
     * If {@code csq} is {@code null}, the characters "null" are appended.
     * 
     * @param csq
     *            the character sequence to append.
     * @param start
     *            the first index of the subsequence of {@code csq} that is
     *            appended.
     * @param end
     *            the last index of the subsequence of {@code csq} that is
     *            appended.
     * @return this {@code Appendable}.
     * @throws IndexOutOfBoundsException
     *             if {@code start < 0}, {@code end < 0}, {@code start > end}
     *             or {@code end} is greater than the length of {@code csq}.
     * @throws IOException
     *             if an I/O error occurs.
     */
    Appendable append(CharSequence csq, int start, int end) throws IOException;
}
