/*
 * CollectionUtilities.java
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

import com.strobel.annotations.NotNull;
import com.strobel.functions.Supplier;
import com.strobel.util.ContractUtils;
import com.strobel.util.EmptyArrayCache;

import java.lang.reflect.Array;
import java.util.*;

/**
 * @author Mike Strobel
 */
public final class CollectionUtilities {
    private final static Supplier IDENTITY_MAP_SUPPLIER = new Supplier() {
        @Override
        public Map get() {
            return new IdentityHashMap<>();
        }
    };

    private final static Supplier HASH_MAP_SUPPLIER = new Supplier() {
        @Override
        public Map get() {
            return new HashMap<>();
        }
    };

    private final static Supplier LINKED_HASH_MAP_SUPPLIER = new Supplier() {
        @Override
        public Map get() {
            return new LinkedHashMap<>();
        }
    };

    private final static Supplier LIST_SUPPLIER = new Supplier() {
        @Override
        public List get() {
            return new ArrayList<>();
        }
    };

    private final static Supplier SET_SUPPLIER = new Supplier() {
        @Override
        public Set get() {
            return new LinkedHashSet<>();
        }
    };

    @SuppressWarnings("unchecked")
    public static <T> Supplier<Set<T>> setFactory() {
        return (Supplier<Set<T>>) SET_SUPPLIER;
    }

    @SuppressWarnings("unchecked")
    public static <T> Supplier<List<T>> listFactory() {
        return (Supplier<List<T>>) LIST_SUPPLIER;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Supplier<Map<K, V>> hashMapFactory() {
        return (Supplier<Map<K, V>>) HASH_MAP_SUPPLIER;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Supplier<Map<K, V>> linekdHashMapFactory() {
        return (Supplier<Map<K, V>>) LINKED_HASH_MAP_SUPPLIER;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Supplier<Map<K, V>> identityMapFactory() {
        return (Supplier<Map<K, V>>) IDENTITY_MAP_SUPPLIER;
    }

    public static <T> int indexOfByIdentity(final List<?> collection, final T item) {
        for (int i = 0, n = collection.size(); i < n; i++) {
            if (collection.get(i) == item) {
                return i;
            }
        }
        return -1;
    }

    public static <T> int indexOfByIdentity(final Iterable<?> collection, final T item) {
        VerifyArgument.notNull(collection, "collection");

        if (collection instanceof List<?>) {
            return indexOfByIdentity((List<?>) collection, item);
        }

        int i = -1;

        for (final Object o : collection) {
            ++i;

            if (o == item) {
                return i;
            }
        }

        return -1;
    }

    public static <T> int indexOf(final Iterable<? super T> collection, final T item) {
        VerifyArgument.notNull(collection, "collection");

        if (collection instanceof List<?>) {
            return ((List<?>) collection).indexOf(item);
        }

        int i = -1;

        for (final Object o : collection) {
            ++i;

            if (Objects.equals(o, item)) {
                return i;
            }
        }

        return -1;
    }

    public static <T> List<T> toList(final Enumeration<T> collection) {
        if (!collection.hasMoreElements()) {
            return Collections.emptyList();
        }

        final ArrayList<T> list = new ArrayList<>();

        while (collection.hasMoreElements()) {
            list.add(collection.nextElement());
        }

        return list;
    }

    public static <T> List<T> toList(final Iterable<T> collection) {
        final ArrayList<T> list = new ArrayList<>();

        for (final T item : collection) {
            list.add(item);
        }

        return list;
    }

    public static <T> T getOrDefault(final Iterable<T> collection, final int index) {
        int i = 0;

        for (final T item : collection) {
            if (i++ == index) {
                return item;
            }
        }

        return null;
    }

    public static <T> T getOrDefault(final List<T> collection, final int index) {
        if (index >= VerifyArgument.notNull(collection, "collection").size() || index < 0) {
            return null;
        }
        return collection.get(index);
    }

    public static <T> T get(final Iterable<T> collection, final int index) {
        if (VerifyArgument.notNull(collection, "collection") instanceof List<?>) {
            return get((List<T>) collection, index);
        }

        int i = 0;

        for (final T item : collection) {
            if (i++ == index) {
                return item;
            }
        }

        throw Error.indexOutOfRange(index);
    }

    public static <T> T get(final List<T> list, final int index) {
        if (index >= VerifyArgument.notNull(list, "list").size() || index < 0) {
            throw Error.indexOutOfRange(index);
        }
        return list.get(index);
    }

    public static <T> T single(final List<T> list) {
        switch (VerifyArgument.notNull(list, "list").size()) {
            case 0:
                throw Error.sequenceHasNoElements();
            case 1:
                return list.get(0);
            default:
                throw Error.sequenceHasMultipleElements();
        }
    }

    public static <T> T singleOrDefault(final List<T> list) {
        switch (VerifyArgument.notNull(list, "list").size()) {
            case 0:
                return null;
            case 1:
                return list.get(0);
            default:
                throw Error.sequenceHasMultipleElements();
        }
    }

    public static <T> T single(final Iterable<T> collection) {
        if (collection instanceof List<?>) {
            return single((List<T>) collection);
        }

        final Iterator<T> it = VerifyArgument.notNull(collection, "collection").iterator();

        if (it.hasNext()) {
            final T result = it.next();

            if (it.hasNext()) {
                throw Error.sequenceHasMultipleElements();
            }

            return result;
        }

        throw Error.sequenceHasNoElements();
    }

    public static <T> T first(final List<T> list) {
        if (VerifyArgument.notNull(list, "list").isEmpty()) {
            throw Error.sequenceHasNoElements();
        }
        return list.get(0);
    }

    public static <T> T first(final Iterable<T> collection) {
        if (collection instanceof List<?>) {
            return first((List<T>) collection);
        }

        final Iterator<T> it = VerifyArgument.notNull(collection, "collection").iterator();

        if (it.hasNext()) {
            return it.next();
        }

        throw Error.sequenceHasNoElements();
    }

    public static <T> T singleOrDefault(final Iterable<T> collection) {
        if (collection instanceof List<?>) {
            return singleOrDefault((List<T>) collection);
        }

        final Iterator<T> it = VerifyArgument.notNull(collection, "collection").iterator();

        if (it.hasNext()) {
            final T result = it.next();

            if (it.hasNext()) {
                throw Error.sequenceHasMultipleElements();
            }

            return result;
        }

        return null;
    }

    public static <T, R> Iterable<R> ofType(final Iterable<T> collection, final Class<R> type) {
        return new OfTypeIterator<>(VerifyArgument.notNull(collection, "collection"), type);
    }

    public static <T> T firstOrDefault(final Iterable<T> collection) {
        final Iterator<T> it = VerifyArgument.notNull(collection, "collection").iterator();
        return it.hasNext() ? it.next() : null;
    }

    public static <T> T first(final Iterable<T> collection, final Predicate<T> predicate) {
        VerifyArgument.notNull(predicate, "predicate");

        for (final T item : VerifyArgument.notNull(collection, "collection")) {
            if (predicate.test(item)) {
                return item;
            }
        }

        throw Error.sequenceHasNoElements();
    }

    public static <T> T firstOrDefault(final Iterable<T> collection, final Predicate<T> predicate) {
        VerifyArgument.notNull(predicate, "predicate");

        for (final T item : VerifyArgument.notNull(collection, "collection")) {
            if (predicate.test(item)) {
                return item;
            }
        }

        return null;
    }

    public static <T> T last(final List<T> list) {
        if (VerifyArgument.notNull(list, "list").isEmpty()) {
            throw Error.sequenceHasNoElements();
        }

        return list.get(list.size() - 1);
    }

    public static <T> T last(final Iterable<T> collection) {
        VerifyArgument.notNull(collection, "collection");

        if (collection instanceof List<?>) {
            return last((List<T>) collection);
        }

        final Iterator<T> iterator = collection.iterator();
        final boolean hasAny = iterator.hasNext();

        if (!hasAny) {
            throw Error.sequenceHasNoElements();
        }

        T last;

        do {
            last = iterator.next();
        }
        while (iterator.hasNext());

        return last;
    }

    public static <T> T lastOrDefault(final Iterable<T> collection) {
        VerifyArgument.notNull(collection, "collection");

        if (collection instanceof List<?>) {
            final List<T> list = (List<T>) collection;
            return list.isEmpty() ? null : list.get(list.size() - 1);
        }

        T last = null;

        for (final T item : collection) {
            last = item;
        }

        return last;
    }

    public static <T> int firstIndexWhere(final Iterable<T> collection, final Predicate<T> predicate) {
        VerifyArgument.notNull(collection, "collection");
        VerifyArgument.notNull(predicate, "predicate");

        int index = 0;

        for (final T item : VerifyArgument.notNull(collection, "collection")) {
            if (predicate.test(item)) {
                return index;
            }
            ++index;
        }

        return -1;
    }

    public static <T> int lastIndexWhere(final Iterable<T> collection, final Predicate<T> predicate) {
        VerifyArgument.notNull(collection, "collection");
        VerifyArgument.notNull(predicate, "predicate");

        int index = 0;
        int lastMatch = -1;

        for (final T item : VerifyArgument.notNull(collection, "collection")) {
            if (predicate.test(item)) {
                lastMatch = index;
            }
            ++index;
        }

        return lastMatch;
    }

    public static <T> T last(final Iterable<T> collection, final Predicate<T> predicate) {
        VerifyArgument.notNull(collection, "collection");
        VerifyArgument.notNull(predicate, "predicate");

        T lastMatch = null;
        boolean matchFound = false;

        for (final T item : VerifyArgument.notNull(collection, "collection")) {
            if (predicate.test(item)) {
                lastMatch = item;
                matchFound = true;
            }
        }

        if (matchFound) {
            return lastMatch;
        }

        throw Error.sequenceHasNoElements();
    }

    public static <T> T lastOrDefault(final Iterable<T> collection, final Predicate<T> predicate) {
        VerifyArgument.notNull(collection, "collection");
        VerifyArgument.notNull(predicate, "predicate");

        T lastMatch = null;

        for (final T item : VerifyArgument.notNull(collection, "collection")) {
            if (predicate.test(item)) {
                lastMatch = item;
            }
        }

        return lastMatch;
    }

    public static <T> boolean contains(final Iterable<? super T> collection, final T node) {
        if (collection instanceof Collection<?>) {
            return ((Collection<?>) collection).contains(node);
        }

        for (final Object item : collection) {
            if (Comparer.equals(item, node)) {
                return true;
            }
        }
        return false;
    }

    public static <T> boolean any(final Iterable<T> collection) {
        if (collection instanceof Collection<?>) {
            return !((Collection) collection).isEmpty();
        }
        return collection != null && collection.iterator().hasNext();
    }

    public static <T> Iterable<T> skip(final Iterable<T> collection, final int count) {
        return new SkipIterator<>(collection, count);
    }

    public static <T> Iterable<T> skipWhile(final Iterable<T> collection, final Predicate<? super T> filter) {
        return new SkipIterator<>(collection, filter);
    }

    public static <T> Iterable<T> take(final Iterable<T> collection, final int count) {
        return new TakeIterator<>(collection, count);
    }

    public static <T> Iterable<T> takeWhile(final Iterable<T> collection, final Predicate<? super T> filter) {
        return new TakeIterator<>(collection, filter);
    }

    public static <T> boolean any(final Iterable<T> collection, final Predicate<? super T> predicate) {
        VerifyArgument.notNull(collection, "collection");
        VerifyArgument.notNull(predicate, "predicate");

        for (final T t : collection) {
            if (predicate.test(t)) {
                return true;
            }
        }

        return false;
    }

    public static <T> boolean all(final Iterable<T> collection, final Predicate<? super T> predicate) {
        VerifyArgument.notNull(collection, "collection");
        VerifyArgument.notNull(predicate, "predicate");

        for (final T t : collection) {
            if (!predicate.test(t)) {
                return false;
            }
        }

        return true;
    }

    public static <T> Iterable<T> where(final Iterable<T> source, final Predicate<? super T> filter) {
        VerifyArgument.notNull(source, "source");
        VerifyArgument.notNull(filter, "filter");

        if (source instanceof WhereSelectIterableIterator<?, ?>) {
            return ((WhereSelectIterableIterator<?, T>) source).where(filter);
        }

        return new WhereSelectIterableIterator<>(source, filter, null);
    }

    public static <T, R> Iterable<R> select(final Iterable<T> source, final Selector<? super T, ? extends R> selector) {
        VerifyArgument.notNull(source, "source");
        VerifyArgument.notNull(selector, "selector");

        if (source instanceof WhereSelectIterableIterator<?, ?>) {
            return ((WhereSelectIterableIterator<?, T>) source).select(selector);
        }

        return new WhereSelectIterableIterator<>(source, null, selector);
    }

    public static int hashCode(final List<?> sequence) {
        VerifyArgument.notNull(sequence, "sequence");

        int hashCode = HashUtilities.NullHashCode;

        for (int i = 0; i < sequence.size(); i++) {
            final Object item = sequence.get(i);

            final int itemHashCode;

            if (item instanceof Iterable<?>) {
                itemHashCode = hashCode((Iterable<?>) item);
            }
            else {
                itemHashCode = item != null ? HashUtilities.hashCode(item)
                                            : HashUtilities.NullHashCode;
            }

            hashCode = HashUtilities.combineHashCodes(
                hashCode,
                itemHashCode
            );
        }

        return hashCode;
    }

    public static int hashCode(final Iterable<?> sequence) {
        if (sequence instanceof List<?>) {
            return hashCode((List<?>) sequence);
        }

        VerifyArgument.notNull(sequence, "sequence");

        int hashCode = HashUtilities.NullHashCode;

        for (final Object item : sequence) {
            final int itemHashCode;

            if (item instanceof Iterable<?>) {
                itemHashCode = hashCode((Iterable<?>) item);
            }
            else {
                itemHashCode = item != null ? HashUtilities.hashCode(item)
                                            : HashUtilities.NullHashCode;
            }

            hashCode = HashUtilities.combineHashCodes(
                hashCode,
                itemHashCode
            );
        }

        return hashCode;
    }

    public static <T> boolean sequenceEquals(final List<? extends T> first, final List<? extends T> second) {
        VerifyArgument.notNull(first, "first");
        VerifyArgument.notNull(second, "second");

        if (first == second) {
            return true;
        }

        if (first.size() != second.size()) {
            return false;
        }

        if (first.isEmpty()) {
            return true;
        }

        for (int i = 0, n = first.size(); i < n; i++) {
            if (!Comparer.equals(first.get(i), second.get(i))) {
                return false;
            }
        }

        return true;
    }

    public static <T> boolean sequenceEquals(final Iterable<? extends T> first, final Iterable<? extends T> second) {
        VerifyArgument.notNull(first, "first");
        VerifyArgument.notNull(second, "second");

        if (first == second) {
            return true;
        }

        if (first instanceof List<?> && second instanceof List<?>) {
            return sequenceDeepEquals((List<?>) first, (List<?>) second);
        }

        final Iterator<? extends T> firstIterator = first.iterator();
        final Iterator<? extends T> secondIterator = second.iterator();

        while (firstIterator.hasNext()) {
            if (!secondIterator.hasNext()) {
                return false;
            }

            if (!Comparer.equals(firstIterator.next(), secondIterator.next())) {
                return false;
            }
        }

        return !secondIterator.hasNext();
    }

    public static <T> boolean sequenceDeepEquals(final List<? extends T> first, final List<? extends T> second) {
        VerifyArgument.notNull(first, "first");
        VerifyArgument.notNull(second, "second");

        if (first == second) {
            return true;
        }

        if (first.size() != second.size()) {
            return false;
        }

        if (first.isEmpty()) {
            return true;
        }

        for (int i = 0, n = first.size(); i < n; i++) {
            if (!sequenceDeepEqualsCore(first.get(i), second.get(i))) {
                return false;
            }
        }

        return true;
    }

    public static <T> boolean sequenceDeepEquals(final Iterable<? extends T> first, final Iterable<? extends T> second) {
        VerifyArgument.notNull(first, "first");
        VerifyArgument.notNull(second, "second");

        if (first == second) {
            return true;
        }

        if (first instanceof List<?> && second instanceof List<?>) {
            return sequenceDeepEquals((List<?>) first, (List<?>) second);
        }

        final Iterator<? extends T> firstIterator = first.iterator();
        final Iterator<? extends T> secondIterator = second.iterator();

        while (firstIterator.hasNext()) {
            if (!secondIterator.hasNext()) {
                return false;
            }

            if (!sequenceDeepEqualsCore(firstIterator.next(), secondIterator.next())) {
                return false;
            }
        }

        return !secondIterator.hasNext();
    }

    private static boolean sequenceDeepEqualsCore(final Object first, final Object second) {
        if (first instanceof List<?>) {
            return second instanceof List<?> &&
                   sequenceDeepEquals((List<?>) first, (List<?>) second);
        }
        return Comparer.deepEquals(first, second);
    }

    public static <E> E[] toArray(final Class<E> elementType, final Iterable<? extends E> sequence) {
        VerifyArgument.notNull(elementType, "elementType");
        VerifyArgument.notNull(sequence, "sequence");

        return new Buffer<>(elementType, sequence.iterator()).toArray();
    }

    private final static class Buffer<E> {
        final Class<E> elementType;

        E[] items;
        int count;

        @SuppressWarnings("unchecked")
        Buffer(final Class<E> elementType, final Iterator<? extends E> source) {
            this.elementType = elementType;

            E[] items = null;
            int count = 0;

            if (source instanceof Collection<?>) {
                final Collection<E> collection = (Collection<E>) source;

                count = collection.size();

                if (count > 0) {
                    items = (E[]) Array.newInstance(elementType, count);
                    collection.toArray(items);
                }
            }
            else {
                while (source.hasNext()) {
                    final E item = source.next();

                    if (items == null) {
                        items = (E[]) Array.newInstance(elementType, 4);
                    }
                    else if (items.length == count) {
                        items = Arrays.copyOf(items, count * 2);
                    }

                    items[count] = item;
                    count++;
                }
            }

            this.items = items;
            this.count = count;
        }

        E[] toArray() {
            if (count == 0) {
                return EmptyArrayCache.fromElementType(elementType);
            }

            if (items.length == count) {
                return items;
            }

            return Arrays.copyOf(items, count);
        }
    }

    private abstract static class AbstractIterator<T> implements Iterable<T>, Iterator<T> {
        final static int STATE_UNINITIALIZED = 0;
        final static int STATE_NEED_NEXT = 1;
        final static int STATE_HAS_NEXT = 2;
        final static int STATE_FINISHED = 3;

        long threadId;
        int state;
        T next;

        AbstractIterator() {
            super();
            threadId = Thread.currentThread().getId();
        }

        @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
        protected abstract AbstractIterator<T> clone();

        @Override
        public abstract boolean hasNext();

        @Override
        public T next() {
            if (!hasNext()) {
                throw new IllegalStateException();
            }
            state = STATE_NEED_NEXT;
            return next;
        }

        @NotNull
        @Override
        public Iterator<T> iterator() {
            if (threadId == Thread.currentThread().getId() && state == STATE_UNINITIALIZED) {
                state = STATE_NEED_NEXT;
                return this;
            }
            final AbstractIterator<T> duplicate = clone();
            duplicate.state = STATE_NEED_NEXT;
            return duplicate;
        }

        @Override
        public final void remove() {
            throw ContractUtils.unsupported();
        }
    }

    private final static class SkipIterator<T> extends AbstractIterator<T> {
        private final static int STATE_NEED_SKIP = 4;

        final Iterable<T> source;
        final int skipCount;
        final Predicate<? super T> skipFilter;

        int skipsRemaining;
        Iterator<T> iterator;

        SkipIterator(final Iterable<T> source, final int skipCount) {
            this.source = VerifyArgument.notNull(source, "source");
            this.skipCount = skipCount;
            this.skipFilter = null;
            this.skipsRemaining = skipCount;
        }

        SkipIterator(final Iterable<T> source, final Predicate<? super T> skipFilter) {
            this.source = VerifyArgument.notNull(source, "source");
            this.skipCount = 0;
            this.skipFilter = VerifyArgument.notNull(skipFilter, "skipFilter");
        }

        @Override
        @SuppressWarnings("CloneDoesntCallSuperClone")
        protected SkipIterator<T> clone() {
            if (skipFilter != null) {
                return new SkipIterator<>(source, skipFilter);
            }
            return new SkipIterator<>(source, skipCount);
        }

        @Override
        public boolean hasNext() {
            switch (state) {
                case STATE_NEED_SKIP:
                    iterator = source.iterator();
                    if (skipFilter != null) {
                        while (iterator.hasNext()) {
                            final T current = iterator.next();
                            if (!skipFilter.test(current)) {
                                state = STATE_HAS_NEXT;
                                next = current;
                                return true;
                            }
                        }
                    }
                    else {
                        while (iterator.hasNext() && skipsRemaining > 0) {
                            iterator.next();
                            --skipsRemaining;
                        }
                    }
                    state = STATE_NEED_NEXT;
                    // goto case STATE_NEED_NEXT

                case STATE_NEED_NEXT:
                    if (iterator.hasNext()) {
                        state = STATE_HAS_NEXT;
                        next = iterator.next();
                        return true;
                    }
                    state = STATE_FINISHED;
                    // goto case STATE_FINISHED

                case STATE_FINISHED:
                    return false;

                case STATE_HAS_NEXT:
                    return true;
            }

            return false;
        }

        @NotNull
        @Override
        public Iterator<T> iterator() {
            if (threadId == Thread.currentThread().getId() && state == STATE_UNINITIALIZED) {
                state = STATE_NEED_SKIP;
                return this;
            }
            final SkipIterator<T> duplicate = clone();
            duplicate.state = STATE_NEED_SKIP;
            return duplicate;
        }
    }

    private final static class TakeIterator<T> extends AbstractIterator<T> {
        final Iterable<T> source;
        final int takeCount;
        final Predicate<? super T> takeFilter;

        Iterator<T> iterator;
        int takesRemaining;

        TakeIterator(final Iterable<T> source, final int takeCount) {
            this.source = VerifyArgument.notNull(source, "source");
            this.takeCount = takeCount;
            this.takeFilter = null;
            this.takesRemaining = takeCount;
        }

        TakeIterator(final Iterable<T> source, final Predicate<? super T> takeFilter) {
            this.source = VerifyArgument.notNull(source, "source");
            this.takeCount = Integer.MAX_VALUE;
            this.takeFilter = VerifyArgument.notNull(takeFilter, "takeFilter");
            this.takesRemaining = Integer.MAX_VALUE;
        }

        TakeIterator(final Iterable<T> source, final int takeCount, final Predicate<? super T> takeFilter) {
            this.source = VerifyArgument.notNull(source, "source");
            this.takeCount = takeCount;
            this.takeFilter = takeFilter;
            this.takesRemaining = takeCount;
        }

        @Override
        @SuppressWarnings("CloneDoesntCallSuperClone")
        protected TakeIterator<T> clone() {
            return new TakeIterator<>(source, takeCount, takeFilter);
        }

        @Override
        public boolean hasNext() {
            switch (state) {
                case STATE_NEED_NEXT:
                    if (takesRemaining-- > 0) {
                        if (iterator == null) {
                            iterator = source.iterator();
                        }
                        if (iterator.hasNext()) {
                            final T current = iterator.next();
                            if (takeFilter == null || takeFilter.test(current)) {
                                state = STATE_HAS_NEXT;
                                next = current;
                                return true;
                            }
                        }
                    }
                    state = STATE_FINISHED;
                    // goto case STATE_FINISHED

                case STATE_FINISHED:
                    return false;

                case STATE_HAS_NEXT:
                    return true;
            }

            return false;
        }
    }

    private final static class OfTypeIterator<T, R> extends AbstractIterator<R> {
        final Iterable<T> source;
        final Class<R> type;

        Iterator<T> iterator;

        OfTypeIterator(final Iterable<T> source, final Class<R> type) {
            this.source = VerifyArgument.notNull(source, "source");
            this.type = VerifyArgument.notNull(type, "type");
        }

        @Override
        @SuppressWarnings("CloneDoesntCallSuperClone")
        protected OfTypeIterator<T, R> clone() {
            return new OfTypeIterator<>(source, type);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean hasNext() {
            switch (state) {
                case STATE_NEED_NEXT:
                    if (iterator == null) {
                        iterator = source.iterator();
                    }
                    while (iterator.hasNext()) {
                        final T current = iterator.next();
                        if (type.isInstance(current)) {
                            state = STATE_HAS_NEXT;
                            next = (R) current;
                            return true;
                        }
                    }
                    state = STATE_FINISHED;
                    // goto case STATE_FINISHED

                case STATE_FINISHED:
                    return false;

                case STATE_HAS_NEXT:
                    return true;
            }

            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private final static class WhereSelectIterableIterator<T, R> extends AbstractIterator<R> {
        final Iterable<T> source;
        final Predicate<? super T> filter;
        final Selector<? super T, ? extends R> selector;

        Iterator<T> iterator;

        WhereSelectIterableIterator(final Iterable<T> source, final Predicate<? super T> filter, final Selector<? super T, ? extends R> selector) {
            this.source = VerifyArgument.notNull(source, "source");
            this.filter = filter;
            this.selector = selector;
        }

        @Override
        protected WhereSelectIterableIterator<T, R> clone() {
            return new WhereSelectIterableIterator<>(source, filter, selector);
        }

        @Override
        public boolean hasNext() {
            switch (state) {
                case STATE_NEED_NEXT:
                    if (iterator == null) {
                        iterator = source.iterator();
                    }
                    while (iterator.hasNext()) {
                        final T item = iterator.next();
                        if (filter == null || filter.test(item)) {
                            next = selector != null ? selector.select(item) : (R) item;
                            state = STATE_HAS_NEXT;
                            return true;
                        }
                    }
                    state = STATE_FINISHED;

                case STATE_FINISHED:
                    return false;

                case STATE_HAS_NEXT:
                    return true;
            }
            return false;
        }

        public Iterable<R> where(final Predicate<? super R> filter) {
            if (this.selector != null) {
                return new WhereSelectIterableIterator<>(this, filter, null);
            }
            return new WhereSelectIterableIterator<>(
                this.source,
                Predicates.and((Predicate<T>) this.filter, (Predicate<T>) filter),
                null
            );
        }

        public <R2> Iterable<R2> select(final Selector<? super R, ? extends R2> selector) {
            return new WhereSelectIterableIterator<>(
                this.source,
                this.filter,
                this.selector != null ? Selectors.combine(this.selector, selector)
                                      : (Selector<T, R2>) selector
            );
        }
    }
}
