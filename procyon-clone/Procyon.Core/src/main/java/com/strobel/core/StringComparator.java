/*
 * StringComparator.java
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

package com.strobel.core;

import java.util.Comparator;

/**
 * @author Mike Strobel
 */
@SuppressWarnings("StringEquality")
public abstract class StringComparator implements Comparator<String>, IEqualityComparator<String> {
    private StringComparator() {}

    public static final StringComparator Ordinal = new StringComparator() {
        @Override
        public int compare(final String s1, final String s2) {
            if (s1 == null) {
                if (s2 == null) {
                    return 0;
                }
                return -1;
            }
            return s1.compareTo(s2);
        }

        @Override
        public boolean equals(final String s1, final String s2) {
            return s1 == null ? s2 == null
                              : s1.equals(s2);
        }

        @Override
        public int hash(final String s) {
            if (s == null) {
                return 0;
            }
            return s.hashCode();
        }
    };


    public static final StringComparator OrdinalIgnoreCase = new StringComparator() {
        @Override
        public int compare(final String s1, final String s2) {
            return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
        }

        @Override
        public boolean equals(final String s1, final String s2) {
            return s1 == null ? s2 == null
                              : s1.equalsIgnoreCase(s2);
        }

        @Override
        public int hash(final String s) {
            return StringUtilities.getHashCodeIgnoreCase(s);
        }
    };
}
