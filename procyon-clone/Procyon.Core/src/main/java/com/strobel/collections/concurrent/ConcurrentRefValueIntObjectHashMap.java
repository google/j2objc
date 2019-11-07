package com.strobel.collections.concurrent;

import com.strobel.annotations.NotNull;
import com.strobel.util.ContractUtils;

import java.lang.ref.ReferenceQueue;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author strobelm
 */
abstract class ConcurrentRefValueIntObjectHashMap<V> implements ConcurrentIntObjectMap<V> {
    private final ConcurrentIntObjectHashMap<IntReference<V>> _map = new ConcurrentIntObjectHashMap<>();
    private final ReferenceQueue<V> _queue = new ReferenceQueue<>();

    protected interface IntReference<V> {
        int key();
        V get();
    }

    protected abstract IntReference<V> createReference(final int key, @NotNull final V value, final ReferenceQueue<V> queue);

    @SuppressWarnings("unchecked")
    private void processQueue() {
        while (true) {
            final IntReference<V> reference = (IntReference<V>)_queue.poll();

            if (reference == null) {
                return;
            }

            _map.remove(reference.key(), reference);
        }
    }

    @NotNull
    @Override
    public V addOrGet(final int key, @NotNull final V value) {
        processQueue();

        final IntReference<V> newReference = createReference(key, value, _queue);

        while (true) {
            final IntReference<V> oldReference = _map.putIfAbsent(key, newReference);

            if (oldReference == null) {
                return value;
            }

            final V oldValue = oldReference.get();

            if (oldValue != null) {
                return oldValue;
            }

            final boolean replaced = _map.replace(key, oldReference, newReference);

            if (replaced) {
                return value;
            }
        }
    }

    @Override
    public V putIfAbsent(final int key, @NotNull final V value) {
        processQueue();

        final IntReference<V> newReference = createReference(key, value, _queue);

        while (true) {
            final IntReference<V> oldReference = _map.putIfAbsent(key, newReference);

            if (oldReference == null) {
                return null;
            }

            final V oldValue = oldReference.get();

            if (oldValue != null) {
                return oldValue;
            }

            final boolean replaced = _map.replace(key, oldReference, newReference);

            if (replaced) {
                return null;
            }
        }
    }

    @Override
    public boolean remove(final int key, @NotNull final V value) {
        processQueue();
        return _map.remove(key, createReference(key, value, _queue));
    }

    @Override
    public boolean replace(final int key, @NotNull final V oldValue, @NotNull final V newValue) {
        processQueue();

        return _map.replace(
            key,
            createReference(key, oldValue, _queue),
            createReference(key, newValue, _queue)
        );
    }

    @Override
    public V put(final int key, @NotNull final V value) {
        processQueue();

        final IntReference<V> oldReference = _map.put(key, createReference(key, value, _queue));

        return oldReference != null ? oldReference.get()
                                    : null;
    }

    @Override
    public V get(final int key) {
        final IntReference<V> reference = _map.get(key);

        return reference != null ? reference.get()
                                 : null;
    }

    @Override
    public V remove(final int key) {
        processQueue();

        final IntReference<V> reference = _map.remove(key);

        return reference != null ? reference.get()
                                 : null;
    }

    @Override
    public int size() {
        return _map.size();
    }

    @Override
    public boolean isEmpty() {
        return _map.isEmpty();
    }

    @Override
    public boolean contains(final int key) {
        return _map.contains(key);
    }

    @Override
    public void clear() {
        _map.clear();
        processQueue();
    }

    @NotNull
    @Override
    public int[] keys() {
        return _map.keys();
    }

    @NotNull
    @Override
    public Iterable<IntObjectEntry<V>> entries() {
        return new Iterable<IntObjectEntry<V>>() {
            @Override
            public Iterator<IntObjectEntry<V>> iterator() {
                return new Iterator<IntObjectEntry<V>>() {
                    final Iterator<IntObjectEntry<IntReference<V>>> entryIterator = _map.entries().iterator();

                    IntObjectEntry<V> next = nextLiveEntry();

                    @Override
                    public boolean hasNext() {
                        return next != null;
                    }

                    @Override
                    public IntObjectEntry<V> next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }

                        final IntObjectEntry<V> result = next;
                        next = nextLiveEntry();
                        return result;
                    }

                    @Override
                    public void remove() {
                        throw ContractUtils.unsupported();
                    }

                    private IntObjectEntry<V> nextLiveEntry() {
                        while (entryIterator.hasNext()) {
                            final IntObjectEntry<IntReference<V>> entry = entryIterator.next();
                            final V value = entry.value().get();

                            if (value == null) {
                                continue;
                            }

                            final int key = entry.key();

                            return new IntObjectEntry<V>() {
                                @Override
                                public int key() {
                                    return key;
                                }

                                @NotNull
                                @Override
                                public V value() {
                                    return value;
                                }
                            };
                        }

                        return null;
                    }
                };
            }
        };
    }
}
