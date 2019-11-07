/*
 * Predicates.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.core;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Static utility methods pertaining to {@code Predicate} instances.
 *
 * <p>All of the returned predicates are serializable if given serializable
 * parameters.
 */
@SuppressWarnings("unchecked")
public final class Predicates {

    /**
     * a predicate that evaluates to {@code true} if the reference
     * being tested is {@code null}.
     */
    public static final Predicate<Object> IS_NULL = new Predicate<Object>() {
        @Override
        public boolean test(final Object o) {
            return o == null;
        }
    };

    /**
     * a predicate that evaluates to {@code true} if the reference
     * being tested is not {@code null}.
     */
    public static final Predicate<Object> NON_NULL = new Predicate<Object>() {
        @Override
        public boolean test(final Object o) {
            return o != null;
        }
    };

    /**
     * a predicate who's result is always {@code false}.
     */
    public static final Predicate<Object> FALSE = new Predicate<Object>() {
        @Override
        public boolean test(final Object o) {
            return false;
        }
    };


    /**
     * a predicate who's result is always {@code true}.
     */
    public static final Predicate<Object> TRUE = new Predicate<Object>() {
        @Override
        public boolean test(final Object o) {
            return true;
        }
    };

    /**
     * singleton utils
     */
    private Predicates() {
        throw new AssertionError("No instances!");
    }

    /**
     * Returns a predicate that evaluates to {@code true} if the reference
     * being tested is {@code null}.
     *
     * @return a predicate that evaluates to {@code true} if the reference
     * being tested is {@code null}
     */
    public static <T> Predicate<T> isNull() {
        return (Predicate<T>) IS_NULL;
    }

    /**
     * Returns a predicate that evaluates to {@code true} if the reference
     * being tested is non-{@code null}.
     *
     * @return a predicate that evaluates to {@code true} if the reference
     * being tested is is non-{@code null}
     */
    public static <T> Predicate<T> nonNull() {
        return (Predicate<T>) NON_NULL;
    }

    /**
     * Returns a predicate who's result is always {@code false}.
     *
     * @return a predicate who's result is always {@code false}.
     */
    public static <T> Predicate<T> alwaysFalse() {
        return (Predicate<T>) FALSE;
    }

    /**
     * Returns a predicate who's result is always {@code true}.
     *
     * @return a predicate who's result is always {@code true}.
     */
    public static <T> Predicate<T> alwaysTrue() {
        return (Predicate<T>) TRUE;
    }

    /**
     * Returns a predicate that evaluates to {@code true} if the object being
     * tested is an instance of the provided class. If the object being tested
     * is {@code null} this predicate evaluates to {@code false}.
     *
     * @param clazz The target class to be matched by the predicate.
     * @return a predicate that evaluates to {@code true} if the object being
     * tested is an instance of the provided class
     */
    public static <T> Predicate<T> instanceOf(final Class<?> clazz) {
        return new Predicate<T>() {
            @Override
            public boolean test(final T o) {
                return clazz.isInstance(o);
            }
        };
    }

    /**
     * Returns a predicate that who's result is {@code target == object}.
     *
     * @param <T> the type of predicate values.
     * @param target The target value to be compared for identity equality.
     * @return a predicate that who's result is {@code target == object}
     */
    public static <T> Predicate<T> isSame(final T target) {
        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                return t == target;
            }
        };
    }

    /**
     * Returns a predicate who's result matches
     * {@code Objects.equals(target, t)}.
     *
     * @param <T> the type of predicate values.
     * @param target The target value to be compared for equality.
     * @return a predicate who's result matches {@code Objects.equals(target, t)}
     */
    public static <T> Predicate<T> isEqual(final T target) {
        if (null == target)
            return Predicates.isNull();
            return new Predicate<T>() {
                @Override
                public boolean test(final T t) {
                    return target.equals(t);
                }
            };
    }

    /**
     * Creates a predicate that evaluates to {@code true} if the tested object
     * is a member of the provided collection. The collection is not defensively
     * copied, so changes to it will alter the behavior of the predicate.
     *
     * @param <T> Type of predicate values.
     * @param target the collection against which objects will be tested.
     * @return a predicate that evaluates to {@code true} if the tested object
     * is a member of the provided collection. The collection is not defensively
     * copied, so changes to it will alter the behavior of the predicate.
     */
    public static <T> Predicate<T> contains(final Collection<? extends T> target) {
        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                return target.contains(t);
            }
        };
    }
    /**
     * Creates a predicate that evaluates to {@code true} if the tested object
     * is a key in the provided map. The map is not defensively copied, so changes
     * to it will alter the behavior of the predicate.
     *
     * @param <T> Type of predicate values.
     * @param target the map against which objects will be tested.
     * @return a predicate that evaluates to {@code true} if the tested object
     * is a key in the provided map. The map is not defensively copied, so changes
     * to it will alter the behavior of the predicate.
     */
    public static <T> Predicate<T> containsKey(final Map<? extends T, ?> target) {
        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                return target.containsKey(t);
            }
        };
    }


    /**
     * Returns a predicate that evaluates to {@code true} if the provided
     * predicate evaluates to {@code false}
     *
     * @param <T> the type of values evaluated by the predicate.
     * @param predicate The predicate to be evaluated.
     * @return A predicate who's result is the logical inverse of the provided
     * predicate.
     */
    public static <T, P extends Predicate<? super T>> Predicate<T> negate(final P predicate) {
        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                return !predicate.test(t);
            }
        };
    }

    /**
     * Returns a predicate that evaluates to {@code true} if all of the
     * component predicates evaluate to {@code true}. The components are
     * evaluated in order, and evaluation will terminate upon the first
     * {@code false} predicate.
     *
     * @param <T> the type of values evaluated by the predicates.
     * @param first initial component predicate to be evaluated.
     * @param second additional component predicate to be evaluated.
     * @return A predicate who's result is {@code true} iff all component
     * predicates are {@code true}.
     */
    public static <T, P extends Predicate<? super T>> Predicate<T> and(final Predicate<T> first,  final P second) {
        if(first != null && first == second) {
            return first;
        }

        Objects.requireNonNull(first);
        Objects.requireNonNull(second);

        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                //noinspection ConstantConditions
                return first.test(t) && second.test(t);
            }
        };
    }

    /**
     * Returns a predicate that evaluates to {@code true} if all of the
     * component predicates evaluate to {@code true}. The components are
     * evaluated in order, and evaluation will end upon the first
     * {@code false} predicate.
     *
     * @param <T> the type of values evaluated by the predicates.
     * @param components The predicates to be evaluated.
     * @return A predicate who's result is {@code true} iff all component
     * predicates are {@code true}.
     */
    public static <T, P extends Predicate<? super T>> Predicate<T> and(final Iterable<P> components) {
        final List<P> predicates = safeCopyOf(components);

        if(predicates.isEmpty()) {
            throw new IllegalArgumentException("no predicates");
        }

        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                for (final P predicate : predicates) {
                    if (!predicate.test(t)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    /**
     * Returns a predicate that evaluates to {@code true} if all of the
     * component predicates evaluate to {@code true}. The components are
     * evaluated in order, and evaluation will end upon the first
     * {@code false} predicate.
     *
     * @param <T> the type of values evaluated by the predicates.
     * @param components The predicates to be evaluated.
     * @return A predicate who's result is {@code true} iff all component
     * predicates are {@code true}.
     */
    static <T, P extends Predicate<? super T>> Predicate<T> and(final P first, final Iterable<P> components) {
        final List<P> predicates = safeCopyOf(first, components);

        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                for (final P predicate : predicates) {
                    if (!predicate.test(t)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    /**
     * Returns a predicate that evaluates to {@code true} if all of the
     * component predicates evaluate to {@code true}. The components are
     * evaluated in order, and evaluation will end upon the first
     * {@code false} predicate.
     *
     * @param <T> the type of values evaluated by the predicates.
     * @param components The predicates to be evaluated. A copy is made of the
     * components.
     * @return A predicate who's result is {@code true} iff all component
     * predicates are {@code true}.
     */
    @SafeVarargs
    public static <T, P extends Predicate<? super T>> Predicate<T> and(final P... components) {
        final P[] predicates = safeCopyOf(components);

        if(0 == predicates.length) {
            throw new IllegalArgumentException("no predicates");
        }

        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                for (final P predicate : predicates) {
                    if (!predicate.test(t)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    /**
     * Returns a predicate that evaluates to {@code true} if all of the
     * component predicates evaluate to {@code true}. The components are
     * evaluated in order, and evaluation will end upon the first
     * {@code false} predicate.
     *
     * @param <T> the type of values evaluated by the predicates.
     * @param first first predicate to be evaluated.
     * @param components The predicates to be evaluated. A copy is made of the
     * components.
     * @return A predicate who's result is {@code true} iff all component
     * predicates are {@code true}.
     */
    @SafeVarargs
    static <T, P extends Predicate<? super T>> Predicate<T> and(final P first, final P... components) {
        final P[] predicates = safeCopyOf(first, components);

        if(0 == predicates.length) {
            throw new IllegalArgumentException("no predicates");
        }

        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                for (final P predicate : predicates) {
                    if (!predicate.test(t)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    /**
     * Returns a predicate that evaluates to {@code true} if any of the
     * component predicates evaluate to {@code true}. The components are
     * evaluated in order, and evaluation will end upon the first
     * {@code true} predicate.
     *
     * @param <T> the type of values evaluated by the predicates.
     * @param first initial component predicate to be evaluated.
     * @param second additional component predicate to be evaluated.
     * @return A predicate who's result is {@code true} if any component
     * predicate's result is {@code true}.
     */
    public static <T, P extends Predicate<? super T>> Predicate<T> or(final Predicate<T> first,  final P second) {
        if(first != null && first == second) {
            return first;
        }

        Objects.requireNonNull(first);
        Objects.requireNonNull(second);

        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                //noinspection ConstantConditions
                return first.test(t) || second.test(t);
            }
        };
    }

    /**
     * Returns a predicate that evaluates to {@code true} if any of the
     * component predicates evaluate to {@code true}. The components are
     * evaluated in order, and evaluation will end upon the first
     * {@code true} predicate.
     *
     * @param <T> the type of values evaluated by the predicates.
     * @param components The predicates to be evaluated. A copy is made of the
     * components.
     * @return A predicate who's result is {@code true} if any component
     * predicate's result is {@code true}.
     */
    public static <T, P extends Predicate<? super T>> Predicate<T> or(final Iterable<P> components) {
        final List<P> predicates = safeCopyOf(components);

        if(predicates.isEmpty()) {
            throw new IllegalArgumentException("no predicates");
        }

        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                for (final P predicate : predicates) {
                    if (predicate.test(t)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    /**
     * Returns a predicate that evaluates to {@code true} if any of the
     * component predicates evaluate to {@code true}. The components are
     * evaluated in order, and evaluation will end upon the first
     * {@code true} predicate.
     *
     * @param <T> the type of values evaluated by the predicates.
     * @param components The predicates to be evaluated. A copy is made of the
     * components.
     * @return A predicate who's result is {@code true} if any component
     * predicate's result is {@code true}.
     */
    static <T, P extends Predicate<? super T>> Predicate<T> or(final P first, final Iterable<P> components) {
        final List<P> predicates = safeCopyOf(first, components);

        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                for (final P predicate : predicates) {
                    if (predicate.test(t)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    /**
     * Returns a predicate that evaluates to {@code true} if any of the
     * component predicates evaluate to {@code true}. The components are
     * evaluated in order, and evaluation will terminate upon the first
     * {@code true} predicate.
     *
     * @param <T> the type of values evaluated by the predicates.
     * @param components The predicates to be evaluated. A copy is made of the
     * components.
     * @return A predicate who's result is {@code true} if any component
     * predicate's result is {@code true}.
     */
    @SafeVarargs
    public static <T, P extends Predicate<? super T>> Predicate<T> or(final P... components) {
        final P[] predicates = safeCopyOf(components);

        if(0 == predicates.length) {
            throw new IllegalArgumentException("no predicates");
        }

        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                for (final P predicate : predicates) {
                    if (predicate.test(t)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    /**
     * Returns a predicate that evaluates to {@code true} if any of the
     * component predicates evaluate to {@code true}. The components are
     * evaluated in order, and evaluation will terminate upon the first
     * {@code true} predicate.
     *
     * @param <T> the type of values evaluated by the predicates.
     * @param components The predicates to be evaluated. A copy is made of the
     * components.
     * @return A predicate who's result is {@code true} if any component
     * predicate's result is {@code true}.
     */
    @SafeVarargs
    static <T, P extends Predicate<? super T>> Predicate<T> or(final Predicate<T> first, final P... components) {
        final P[] predicates = safeCopyOf((P) first, components);

        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                for (final P predicate : predicates) {
                    if (predicate.test(t)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    /**
     * Returns a predicate that evaluates to {@code true} if all or none of the
     * component predicates evaluate to {@code true}. The components are
     * evaluated in order, and evaluation will end if a predicate result
     * fails to match the first predicate's result.
     *
     * @param <T> the type of values evaluated by the predicates.
     * @param first initial component predicate to be evaluated.
     * @param second additional component predicate to be evaluated.
     * @return  a predicate that evaluates to {@code true} if all or none of the
     * component predicates evaluate to {@code true}
     */
    public static <T, P extends Predicate<? super T>> Predicate<T> xor(final Predicate<T> first, final P second) {
        if(null != first && first == second) {
            return alwaysFalse();
        }

        Objects.requireNonNull(first);
        Objects.requireNonNull(second);

        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                //noinspection ConstantConditions
                return first.test(t) ^ second.test(t);
            }
        };
    }

    /**
     * Returns a predicate that evaluates to {@code false} if all or none of the
     * component predicates evaluate to {@code true}. The components are
     * evaluated in order, and evaluation will end if a predicate result
     * fails to match the first predicate's result.
     *
     * @param <T> the type of values evaluated by the predicates.
     * @param components The predicates to be evaluated. A copy is made of the
     * components.
     * @return  a predicate that evaluates to {@code false} if all or none of the
     * component predicates evaluate to {@code true}
     */
    public static <T, P extends Predicate<? super T>> Predicate<T> xor(final Iterable<P> components) {
        final List<P> predicates = safeCopyOf(components);

        if(predicates.isEmpty()) {
            throw new IllegalArgumentException("no predicates");
        }

        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                Boolean initial = null;

                for (final P predicate : predicates) {
                    if (null == initial) {
                        initial = predicate.test(t);
                    }
                    else {
                        if (!(initial ^ predicate.test(t))) {
                            return true;
                        }
                    }
                }
                return false;
            }
        };
    }

    /**
     * Returns a predicate that evaluates to {@code false} if all or none of the
     * component predicates evaluate to {@code true}. The components are
     * evaluated in order, and evaluation will terminate if a predicate result
     * fails to match the first predicate's result.
     *
     * @param <T> the type of values evaluated by the predicates.
     * @param components The predicates to be evaluated. A copy is made of the
     * components.
     * @return  a predicate that evaluates to {@code false} if all or none of the
     * component predicates evaluate to {@code true}
     */
    @SafeVarargs
    public static <T, P extends Predicate<? super T>> Predicate<T> xor(final P... components) {
        final P[] predicates = safeCopyOf(components);

        if(0 == predicates.length) {
            throw new IllegalArgumentException("no predicates");
        }

        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                Boolean initial = null;

                for (final P predicate : predicates) {
                    if (null == initial) {
                        initial = predicate.test(t);
                    }
                    else {
                        if (!(initial ^ predicate.test(t))) {
                            return true;
                        }
                    }
                }
                return false;
            }
        };
    }

    /**
     * Returns a predicate that evaluates to {@code false} if all or none of the
     * component predicates evaluate to {@code true}. The components are
     * evaluated in order, and evaluation will end if a predicate result
     * fails to match the first predicate's result.
     *
     * @param <T> the type of values evaluated by the predicates.
     * @param components The predicates to be evaluated. A copy is made of the
     * components.
     * @return  a predicate that evaluates to {@code false} if all or none of the
     * component predicates evaluate to {@code true}
     */
    @SafeVarargs
    static <T, P extends Predicate<? super T>> Predicate<T> xor(final Predicate<T> first, final P... components) {
        final P[] predicates = safeCopyOf((P) first, components);

        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                Boolean initial = null;

                for (final P predicate : predicates) {
                    if (null == initial) {
                        initial = predicate.test(t);
                    }
                    else {
                        if (!(initial ^ predicate.test(t))) {
                            return true;
                        }
                    }
                }
                return false;
            }
        };
    }

    /**
     * Returns a predicate that evaluates to {@code false} if all or none of the
     * component predicates evaluate to {@code true}. The components are
     * evaluated in order, and evaluation will end if a predicate result
     * fails to match the first predicate's result.
     *
     * @param <T> the type of values evaluated by the predicates.
     * @param components The predicates to be evaluated. A copy is made of the
     * components.
     * @return  a predicate that evaluates to {@code false} if all or none of the
     * component predicates evaluate to {@code true}
     */
    static <T, P extends Predicate<? super T>> Predicate<T> xor(final Predicate<T> first, final Iterable<P> components) {
        final List<P> predicates = safeCopyOf((P) first, components);

        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                Boolean initial = null;

                for (final P predicate : predicates) {
                    if (null == initial) {
                        initial = predicate.test(t);
                    }
                    else {
                        if (!(initial ^ predicate.test(t))) {
                            return true;
                        }
                    }
                }
                return false;
            }
        };
    }

    @SafeVarargs
    private static <T> T[] safeCopyOf(final T... array) {
        final T[] copy = Arrays.copyOf(array, array.length);

        for(final T each : copy) {
            Objects.requireNonNull(each);
        }

        return copy;
    }

    @SafeVarargs
    private static <T> T[] safeCopyOf(final T first, final T... array) {
        final T[] copy = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length + 1);

        copy[0] = Objects.requireNonNull(first);
        System.arraycopy(array, 0, copy, 1, array.length);

        for(final T each : copy) {
            Objects.requireNonNull(each);
        }

        return copy;
    }

    private static <T> List<T> safeCopyOf(final T first, final Iterable<T> iterable) {
        final ArrayList<T> list = new ArrayList<>();
        list.add(Objects.requireNonNull(first));

        for (final T element : iterable) {
            list.add(Objects.requireNonNull(element));
        }
        return list;
    }

    private static <T> List<T> safeCopyOf(final Iterable<T> iterable) {
        final ArrayList<T> list = new ArrayList<>();

        for (final T element : iterable) {
            list.add(Objects.requireNonNull(element));
        }

        return list;
    }
}
