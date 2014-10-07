/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

package java.text;

/**
 * Represents a string under the rules of a specific {@code Collator} object.
 * Comparing two {@code CollationKey} instances returns the relative order of
 * the strings they represent.
 * <p>
 * Since the rule set of collators can differ, the sort orders of the same
 * string under two different {@code Collator} instances might differ. Hence
 * comparing collation keys generated from different {@code Collator} instances
 * can give incorrect results.
 * <p>
 * Both the method {@code CollationKey.compareTo(CollationKey)} and the method
 * {@code Collator.compare(String, String)} compares two strings and returns
 * their relative order. The performance characteristics of these two approaches
 * can differ.
 * <p>
 * During the construction of a {@code CollationKey}, the entire source string
 * is examined and processed into a series of bits terminated by a null, that
 * are stored in the {@code CollationKey}. When
 * {@code CollationKey.compareTo(CollationKey)} executes, it performs bitwise
 * comparison on the bit sequences. This can incur startup cost when creating
 * the {@code CollationKey}, but once the key is created, binary comparisons
 * are fast. This approach is recommended when the same strings are to be
 * compared over and over again.
 * <p>
 * On the other hand, implementations of
 * {@code Collator.compare(String, String)} can examine and process the strings
 * only until the first characters differ in order. This approach is
 * recommended if the strings are to be compared only once.
 * <p>
 * The following example shows how collation keys can be used to sort a
 * list of strings:
 * <blockquote>
 *
 * <pre>
 * // Create an array of CollationKeys for the Strings to be sorted.
 * Collator myCollator = Collator.getInstance();
 * CollationKey[] keys = new CollationKey[3];
 * keys[0] = myCollator.getCollationKey(&quot;Tom&quot;);
 * keys[1] = myCollator.getCollationKey(&quot;Dick&quot;);
 * keys[2] = myCollator.getCollationKey(&quot;Harry&quot;);
 * sort(keys);
 * <br>
 * //...
 * <br>
 * // Inside body of sort routine, compare keys this way
 * if( keys[i].compareTo( keys[j] ) &gt; 0 )
 *    // swap keys[i] and keys[j]
 * <br>
 * //...
 * <br>
 * // Finally, when we've returned from sort.
 * System.out.println(keys[0].getSourceString());
 * System.out.println(keys[1].getSourceString());
 * System.out.println(keys[2].getSourceString());
 * </pre>
 *
 * </blockquote>
 *
 * @see Collator
 * @see RuleBasedCollator
 */
public abstract class CollationKey implements Comparable<CollationKey> {
    private final String source;

    protected CollationKey(String source) {
        this.source = source;
    }

    /**
     * Compares this collation key to the given collation key.
     *
     * @param value the other collation key.
     * @return a negative value if this key is less than {@code value},
     *         0 if they are equal, and a positive value if this key is greater.
     */
    public abstract int compareTo(CollationKey value);

    /**
     * Returns the string from which this collation key was created.
     *
     * @return the source string of this collation key.
     */
    public String getSourceString() {
        return source;
    }

    /**
     * Returns this collation key as a byte array.
     *
     * @return an array of bytes.
     */
    public abstract byte[] toByteArray();
}
