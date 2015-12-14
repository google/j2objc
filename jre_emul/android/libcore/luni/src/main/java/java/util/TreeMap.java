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

package java.util;

import com.google.j2objc.annotations.Weak;
import com.google.j2objc.annotations.WeakOuter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import static java.util.TreeMap.Bound.*;
import static java.util.TreeMap.Relation.*;
import libcore.util.Objects;

/**
 * A map whose entries are sorted by their keys. All optional operations such as
 * {@link #put} and {@link #remove} are supported.
 *
 * <p>This map sorts keys using either a user-supplied comparator or the key's
 * natural order:
 * <ul>
 *   <li>User supplied comparators must be able to compare any pair of keys in
 *       this map. If a user-supplied comparator is in use, it will be returned
 *       by {@link #comparator}.
 *   <li>If no user-supplied comparator is supplied, keys will be sorted by
 *       their natural order. Keys must be <i>mutually comparable</i>: they must
 *       implement {@link Comparable} and {@link Comparable#compareTo
 *       compareTo()} must be able to compare each key with any other key in
 *       this map. In this case {@link #comparator} will return null.
 * </ul>
 * With either a comparator or a natural ordering, comparisons should be
 * <i>consistent with equals</i>. An ordering is consistent with equals if for
 * every pair of keys {@code a} and {@code b}, {@code a.equals(b)} if and only
 * if {@code compare(a, b) == 0}.
 *
 * <p>When the ordering is not consistent with equals the behavior of this
 * class is well defined but does not honor the contract specified by {@link
 * Map}. Consider a tree map of case-insensitive strings, an ordering that is
 * not consistent with equals: <pre>   {@code
 *   TreeMap<String, String> map = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
 *   map.put("a", "android");
 *
 *   // The Map API specifies that the next line should print "null" because
 *   // "a".equals("A") is false and there is no mapping for upper case "A".
 *   // But the case insensitive ordering says compare("a", "A") == 0. TreeMap
 *   // uses only comparators/comparable on keys and so this prints "android".
 *   System.out.println(map.get("A"));
 * }</pre>
 *
 * @since 1.2
 */
public class TreeMap<K, V> extends AbstractMap<K, V>
        implements SortedMap<K, V>, NavigableMap<K, V>, Cloneable, Serializable {

    @SuppressWarnings("unchecked") // to avoid Comparable<Comparable<Comparable<...>>>
    private static final Comparator<Comparable> NATURAL_ORDER = new Comparator<Comparable>() {
        public int compare(Comparable a, Comparable b) {
            return a.compareTo(b);
        }
    };

    Comparator<? super K> comparator;
    Node<K, V> root;
    int size = 0;
    int modCount = 0;

    /**
     * Create a natural order, empty tree map whose keys must be mutually
     * comparable and non-null.
     */
    @SuppressWarnings("unchecked") // unsafe! this assumes K is comparable
    public TreeMap() {
        this.comparator = (Comparator<? super K>) NATURAL_ORDER;
    }

    /**
     * Create a natural order tree map populated with the key/value pairs of
     * {@code copyFrom}. This map's keys must be mutually comparable and
     * non-null.
     *
     * <p>Even if {@code copyFrom} is a {@code SortedMap}, the constructed map
     * <strong>will not</strong> use {@code copyFrom}'s ordering. This
     * constructor always creates a naturally-ordered map. Because the {@code
     * TreeMap} constructor overloads are ambiguous, prefer to construct a map
     * and populate it in two steps: <pre>   {@code
     *   TreeMap<String, Integer> customOrderedMap
     *       = new TreeMap<String, Integer>(copyFrom.comparator());
     *   customOrderedMap.putAll(copyFrom);
     * }</pre>
     */
    public TreeMap(Map<? extends K, ? extends V> copyFrom) {
        this();
        for (Map.Entry<? extends K, ? extends V> entry : copyFrom.entrySet()) {
            putInternal(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Create a tree map ordered by {@code comparator}. This map's keys may only
     * be null if {@code comparator} permits.
     *
     * @param comparator the comparator to order elements with, or {@code null} to use the natural
     * ordering.
     */
    @SuppressWarnings("unchecked") // unsafe! if comparator is null, this assumes K is comparable
    public TreeMap(Comparator<? super K> comparator) {
        if (comparator != null) {
            this.comparator = comparator;
        } else {
            this.comparator = (Comparator<? super K>) NATURAL_ORDER;
        }
    }

    /**
     * Create a tree map with the ordering and key/value pairs of {@code
     * copyFrom}. This map's keys may only be null if the {@code copyFrom}'s
     * ordering permits.
     *
     * <p>The constructed map <strong>will always use</strong> {@code
     * copyFrom}'s ordering. Because the {@code TreeMap} constructor overloads
     * are ambiguous, prefer to construct a map and populate it in two steps:
     * <pre>   {@code
     *   TreeMap<String, Integer> customOrderedMap
     *       = new TreeMap<String, Integer>(copyFrom.comparator());
     *   customOrderedMap.putAll(copyFrom);
     * }</pre>
     */
    @SuppressWarnings("unchecked") // if copyFrom's keys are comparable this map's keys must be also
    public TreeMap(SortedMap<K, ? extends V> copyFrom) {
        Comparator<? super K> sourceComparator = copyFrom.comparator();
        if (sourceComparator != null) {
            this.comparator = sourceComparator;
        } else {
            this.comparator = (Comparator<? super K>) NATURAL_ORDER;
        }
        for (Map.Entry<K, ? extends V> entry : copyFrom.entrySet()) {
            putInternal(entry.getKey(), entry.getValue());
        }
    }

    @Override public Object clone() {
        try {
            @SuppressWarnings("unchecked") // super.clone() must return the same type
            TreeMap<K, V> map = (TreeMap<K, V>) super.clone();
            map.root = root != null ? root.copy(null) : null;
            map.entrySet = null;
            map.keySet = null;
            map.valuesCollection = null;
            return map;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override public int size() {
        return size;
    }

    @Override public boolean isEmpty() {
        return size == 0;
    }

    @Override public V get(Object key) {
        Entry<K, V> entry = findByObject(key);
        return entry != null ? entry.getValue() : null;
    }

    @Override public boolean containsKey(Object key) {
        return findByObject(key) != null;
    }

    @Override public V put(K key, V value) {
        return putInternal(key, value);
    }

    @Override public void clear() {
        root = null;
        size = 0;
        modCount++;
    }

    @Override public V remove(Object key) {
        Node<K, V> node = removeInternalByKey(key);
        return node != null ? node.value : null;
    }

    /*
     * AVL methods
     */

    enum Relation {
        LOWER,
        FLOOR,
        EQUAL,
        CREATE,
        CEILING,
        HIGHER;

        /**
         * Returns a possibly-flipped relation for use in descending views.
         *
         * @param ascending false to flip; true to return this.
         */
        Relation forOrder(boolean ascending) {
            if (ascending) {
                return this;
            }

            switch (this) {
                case LOWER:
                    return HIGHER;
                case FLOOR:
                    return CEILING;
                case EQUAL:
                    return EQUAL;
                case CEILING:
                    return FLOOR;
                case HIGHER:
                    return LOWER;
                default:
                    throw new IllegalStateException();
            }
        }
    }

    V putInternal(K key, V value) {
        Node<K, V> created = find(key, Relation.CREATE);
        V result = created.value;
        created.value = value;
        return result;
    }

    /**
     * Returns the node at or adjacent to the given key, creating it if requested.
     *
     * @throws ClassCastException if {@code key} and the tree's keys aren't mutually comparable.
     */
    Node<K, V> find(K key, Relation relation) {
        if (root == null) {
            if (comparator == NATURAL_ORDER && !(key instanceof Comparable)) {
                throw new ClassCastException(key.getClass().getName() + " is not Comparable"); // NullPointerException ok
            }
            if (relation == Relation.CREATE) {
                root = new Node<K, V>(null, key);
                size = 1;
                modCount++;
                return root;
            } else {
                return null;
            }
        }

        /*
         * Micro-optimization: avoid polymorphic calls to Comparator.compare().
         * This is 10% faster for naturally ordered trees.
         */
        @SuppressWarnings("unchecked") // will throw a ClassCastException below if there's trouble
        Comparable<Object> comparableKey = (comparator == NATURAL_ORDER)
                ? (Comparable<Object>) key
                : null;

        Node<K, V> nearest = root;
        while (true) {
            int comparison = (comparableKey != null)
                    ? comparableKey.compareTo(nearest.key)
                    : comparator.compare(key, nearest.key);

            /*
             * We found the requested key.
             */
            if (comparison == 0) {
                switch (relation) {
                    case LOWER:
                        return nearest.prev();
                    case FLOOR:
                    case EQUAL:
                    case CREATE:
                    case CEILING:
                        return nearest;
                    case HIGHER:
                        return nearest.next();
                }
            }

            Node<K, V> child = (comparison < 0) ? nearest.left : nearest.right;
            if (child != null) {
                nearest = child;
                continue;
            }

            /*
             * We found a nearest node. Every key not in the tree has up to two
             * nearest nodes, one lower and one higher.
             */

            if (comparison < 0) { // nearest.key is higher
                switch (relation) {
                    case LOWER:
                    case FLOOR:
                        return nearest.prev();
                    case CEILING:
                    case HIGHER:
                        return nearest;
                    case EQUAL:
                        return null;
                    case CREATE:
                        Node<K, V> created = new Node<K, V>(nearest, key);
                        nearest.left = created;
                        size++;
                        modCount++;
                        rebalance(nearest, true);
                        return created;
                }
            } else { // comparison > 0, nearest.key is lower
                switch (relation) {
                    case LOWER:
                    case FLOOR:
                        return nearest;
                    case CEILING:
                    case HIGHER:
                        return nearest.next();
                    case EQUAL:
                        return null;
                    case CREATE:
                        Node<K, V> created = new Node<K, V>(nearest, key);
                        nearest.right = created;
                        size++;
                        modCount++;
                        rebalance(nearest, true);
                        return created;
                }
            }
        }
    }

    @SuppressWarnings("unchecked") // this method throws ClassCastExceptions!
    Node<K, V> findByObject(Object key) {
        return find((K) key, EQUAL);
    }

    /**
     * Returns this map's entry that has the same key and value as {@code
     * entry}, or null if this map has no such entry.
     *
     * <p>This method uses the comparator for key equality rather than {@code
     * equals}. If this map's comparator isn't consistent with equals (such as
     * {@code String.CASE_INSENSITIVE_ORDER}), then {@code remove()} and {@code
     * contains()} will violate the collections API.
     */
    Node<K, V> findByEntry(Entry<?, ?> entry) {
        Node<K, V> mine = findByObject(entry.getKey());
        boolean valuesEqual = mine != null && Objects.equal(mine.value, entry.getValue());
        return valuesEqual ? mine : null;
    }

    /**
     * Removes {@code node} from this tree, rearranging the tree's structure as
     * necessary.
     */
    void removeInternal(Node<K, V> node) {
        Node<K, V> left = node.left;
        Node<K, V> right = node.right;
        Node<K, V> originalParent = node.parent;
        if (left != null && right != null) {

            /*
             * To remove a node with both left and right subtrees, move an
             * adjacent node from one of those subtrees into this node's place.
             *
             * Removing the adjacent node may change this node's subtrees. This
             * node may no longer have two subtrees once the adjacent node is
             * gone!
             */

            Node<K, V> adjacent = (left.height > right.height) ? left.last() : right.first();
            removeInternal(adjacent); // takes care of rebalance and size--

            int leftHeight = 0;
            left = node.left;
            if (left != null) {
                leftHeight = left.height;
                adjacent.left = left;
                left.parent = adjacent;
                node.left = null;
            }
            int rightHeight = 0;
            right = node.right;
            if (right != null) {
                rightHeight = right.height;
                adjacent.right = right;
                right.parent = adjacent;
                node.right = null;
            }
            adjacent.height = Math.max(leftHeight, rightHeight) + 1;
            replaceInParent(node, adjacent);
            return;
        } else if (left != null) {
            replaceInParent(node, left);
            node.left = null;
        } else if (right != null) {
            replaceInParent(node, right);
            node.right = null;
        } else {
            replaceInParent(node, null);
        }

        rebalance(originalParent, false);
        size--;
        modCount++;
    }

    Node<K, V> removeInternalByKey(Object key) {
        Node<K, V> node = findByObject(key);
        if (node != null) {
            removeInternal(node);
        }
        return node;
    }

    private void replaceInParent(Node<K, V> node, Node<K, V> replacement) {
        Node<K, V> parent = node.parent;
        node.parent = null;
        if (replacement != null) {
            replacement.parent = parent;
        }

        if (parent != null) {
            if (parent.left == node) {
                parent.left = replacement;
            } else {
                // assert (parent.right == node);
                parent.right = replacement;
            }
        } else {
            root = replacement;
        }
    }

    /**
     * Rebalances the tree by making any AVL rotations necessary between the
     * newly-unbalanced node and the tree's root.
     *
     * @param insert true if the node was unbalanced by an insert; false if it
     *     was by a removal.
     */
    private void rebalance(Node<K, V> unbalanced, boolean insert) {
        for (Node<K, V> node = unbalanced; node != null; node = node.parent) {
            Node<K, V> left = node.left;
            Node<K, V> right = node.right;
            int leftHeight = left != null ? left.height : 0;
            int rightHeight = right != null ? right.height : 0;

            int delta = leftHeight - rightHeight;
            if (delta == -2) {
                Node<K, V> rightLeft = right.left;
                Node<K, V> rightRight = right.right;
                int rightRightHeight = rightRight != null ? rightRight.height : 0;
                int rightLeftHeight = rightLeft != null ? rightLeft.height : 0;

                int rightDelta = rightLeftHeight - rightRightHeight;
                if (rightDelta == -1 || (rightDelta == 0 && !insert)) {
                    rotateLeft(node); // AVL right right
                } else {
                    // assert (rightDelta == 1);
                    rotateRight(right); // AVL right left
                    rotateLeft(node);
                }
                if (insert) {
                    break; // no further rotations will be necessary
                }

            } else if (delta == 2) {
                Node<K, V> leftLeft = left.left;
                Node<K, V> leftRight = left.right;
                int leftRightHeight = leftRight != null ? leftRight.height : 0;
                int leftLeftHeight = leftLeft != null ? leftLeft.height : 0;

                int leftDelta = leftLeftHeight - leftRightHeight;
                if (leftDelta == 1 || (leftDelta == 0 && !insert)) {
                    rotateRight(node); // AVL left left
                } else {
                    // assert (leftDelta == -1);
                    rotateLeft(left); // AVL left right
                    rotateRight(node);
                }
                if (insert) {
                    break; // no further rotations will be necessary
                }

            } else if (delta == 0) {
                node.height = leftHeight + 1; // leftHeight == rightHeight
                if (insert) {
                    break; // the insert caused balance, so rebalancing is done!
                }

            } else {
                // assert (delta == -1 || delta == 1);
                node.height = Math.max(leftHeight, rightHeight) + 1;
                if (!insert) {
                    break; // the height hasn't changed, so rebalancing is done!
                }
            }
        }
    }

    /**
     * Rotates the subtree so that its root's right child is the new root.
     */
    private void rotateLeft(Node<K, V> root) {
        Node<K, V> left = root.left;
        Node<K, V> pivot = root.right;
        Node<K, V> pivotLeft = pivot.left;
        Node<K, V> pivotRight = pivot.right;

        // move the pivot's left child to the root's right
        root.right = pivotLeft;
        if (pivotLeft != null) {
            pivotLeft.parent = root;
        }

        replaceInParent(root, pivot);

        // move the root to the pivot's left
        pivot.left = root;
        root.parent = pivot;

        // fix heights
        root.height = Math.max(left != null ? left.height : 0,
                pivotLeft != null ? pivotLeft.height : 0) + 1;
        pivot.height = Math.max(root.height,
                pivotRight != null ? pivotRight.height : 0) + 1;
    }

    /**
     * Rotates the subtree so that its root's left child is the new root.
     */
    private void rotateRight(Node<K, V> root) {
        Node<K, V> pivot = root.left;
        Node<K, V> right = root.right;
        Node<K, V> pivotLeft = pivot.left;
        Node<K, V> pivotRight = pivot.right;

        // move the pivot's right child to the root's left
        root.left = pivotRight;
        if (pivotRight != null) {
            pivotRight.parent = root;
        }

        replaceInParent(root, pivot);

        // move the root to the pivot's right
        pivot.right = root;
        root.parent = pivot;

        // fixup heights
        root.height = Math.max(right != null ? right.height : 0,
                pivotRight != null ? pivotRight.height : 0) + 1;
        pivot.height = Math.max(root.height,
                pivotLeft != null ? pivotLeft.height : 0) + 1;
    }

    /*
     * Navigable methods.
     */

    /**
     * Returns an immutable version of {@param entry}. Need this because we allow entry to be null,
     * in which case we return a null SimpleImmutableEntry.
     */
    private SimpleImmutableEntry<K, V> immutableCopy(Entry<K, V> entry) {
        return entry == null ? null : new SimpleImmutableEntry<K, V>(entry);
    }

    private Node<K, V> firstNode() {
        return root == null ? null : root.first();
    }

    public Entry<K, V> firstEntry() {
        return immutableCopy(firstNode());
    }

    private Entry<K, V> internalPollFirstEntry() {
        if (root == null) {
            return null;
        }
        Node<K, V> result = root.first();
        removeInternal(result);
        return result;
    }

    public Entry<K, V> pollFirstEntry() {
        return immutableCopy(internalPollFirstEntry());
    }

    public K firstKey() {
        if (root == null) {
            throw new NoSuchElementException();
        }
        return root.first().getKey();
    }

    private Node<K, V> lastNode() {
      return root == null ? null : root.last();
    }

    public Entry<K, V> lastEntry() {
        return immutableCopy(lastNode());
    }

    private Entry<K, V> internalPollLastEntry() {
        if (root == null) {
            return null;
        }
        Node<K, V> result = root.last();
        removeInternal(result);
        return result;
    }

    public Entry<K, V> pollLastEntry() {
        return immutableCopy(internalPollLastEntry());
    }

    public K lastKey() {
        if (root == null) {
            throw new NoSuchElementException();
        }
        return root.last().getKey();
    }

    public Entry<K, V> lowerEntry(K key) {
        return immutableCopy(find(key, LOWER));
    }

    public K lowerKey(K key) {
        Entry<K, V> entry = find(key, LOWER);
        return entry != null ? entry.getKey() : null;
    }

    public Entry<K, V> floorEntry(K key) {
        return immutableCopy(find(key, FLOOR));
    }

    public K floorKey(K key) {
        Entry<K, V> entry = find(key, FLOOR);
        return entry != null ? entry.getKey() : null;
    }

    public Entry<K, V> ceilingEntry(K key) {
        return immutableCopy(find(key, CEILING));
    }

    public K ceilingKey(K key) {
        Entry<K, V> entry = find(key, CEILING);
        return entry != null ? entry.getKey() : null;
    }

    public Entry<K, V> higherEntry(K key) {
        return immutableCopy(find(key, HIGHER));
    }

    public K higherKey(K key) {
        Entry<K, V> entry = find(key, HIGHER);
        return entry != null ? entry.getKey() : null;
    }

    public Comparator<? super K> comparator() {
        return comparator != NATURAL_ORDER ? comparator : null;
    }

    /*
     * View factory methods.
     */

    private EntrySet entrySet;
    private KeySet keySet;
    private ValuesCollection valuesCollection;

    @Override public Set<Entry<K, V>> entrySet() {
        EntrySet result = entrySet;
        return result != null ? result : (entrySet = new EntrySet());
    }

    @Override public Set<K> keySet() {
        KeySet result = keySet;
        return result != null ? result : (keySet = new KeySet());
    }

    public NavigableSet<K> navigableKeySet() {
        KeySet result = keySet;
        return result != null ? result : (keySet = new KeySet());
    }

    @Override public Collection<V> values() {
      ValuesCollection result = valuesCollection;
      return result != null ? result : (valuesCollection = new ValuesCollection());
    }

    public NavigableMap<K, V> subMap(K from, boolean fromInclusive, K to, boolean toInclusive) {
        Bound fromBound = fromInclusive ? INCLUSIVE : EXCLUSIVE;
        Bound toBound = toInclusive ? INCLUSIVE : EXCLUSIVE;
        return new BoundedMap(true, from, fromBound, to, toBound);
    }

    public SortedMap<K, V> subMap(K fromInclusive, K toExclusive) {
        return new BoundedMap(true, fromInclusive, INCLUSIVE, toExclusive, EXCLUSIVE);
    }

    public NavigableMap<K, V> headMap(K to, boolean inclusive) {
        Bound toBound = inclusive ? INCLUSIVE : EXCLUSIVE;
        return new BoundedMap(true, null, NO_BOUND, to, toBound);
    }

    public SortedMap<K, V> headMap(K toExclusive) {
        return new BoundedMap(true, null, NO_BOUND, toExclusive, EXCLUSIVE);
    }

    public NavigableMap<K, V> tailMap(K from, boolean inclusive) {
        Bound fromBound = inclusive ? INCLUSIVE : EXCLUSIVE;
        return new BoundedMap(true, from, fromBound, null, NO_BOUND);
    }

    public SortedMap<K, V> tailMap(K fromInclusive) {
        return new BoundedMap(true, fromInclusive, INCLUSIVE, null, NO_BOUND);
    }

    public NavigableMap<K, V> descendingMap() {
        return new BoundedMap(false, null, NO_BOUND, null, NO_BOUND);
    }

    public NavigableSet<K> descendingKeySet() {
        return new BoundedMap(false, null, NO_BOUND, null, NO_BOUND).navigableKeySet();
    }

    static class Node<K, V> implements Map.Entry<K, V> {
        @Weak Node<K, V> parent;
        Node<K, V> left;
        Node<K, V> right;
        final K key;
        V value;
        int height;

        Node(Node<K, V> parent, K key) {
            this.parent = parent;
            this.key = key;
            this.height = 1;
        }

        Node<K, V> copy(Node<K, V> parent) {
            Node<K, V> result = new Node<K, V>(parent, key);
            if (left != null) {
                result.left = left.copy(result);
            }
            if (right != null) {
                result.right = right.copy(result);
            }
            result.value = value;
            result.height = height;
            return result;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        @Override public boolean equals(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry other = (Map.Entry) o;
                return (key == null ? other.getKey() == null : key.equals(other.getKey()))
                        && (value == null ? other.getValue() == null : value.equals(other.getValue()));
            }
            return false;
        }

        @Override public int hashCode() {
            return (key == null ? 0 : key.hashCode())
                    ^ (value == null ? 0 : value.hashCode());
        }

        @Override public String toString() {
            return key + "=" + value;
        }

        /**
         * Returns the next node in an inorder traversal, or null if this is the
         * last node in the tree.
         */
        Node<K, V> next() {
            if (right != null) {
                return right.first();
            }

            Node<K, V> node = this;
            Node<K, V> parent = node.parent;
            while (parent != null) {
                if (parent.left == node) {
                    return parent;
                }
                node = parent;
                parent = node.parent;
            }
            return null;
        }

        /**
         * Returns the previous node in an inorder traversal, or null if this is
         * the first node in the tree.
         */
        public Node<K, V> prev() {
            if (left != null) {
                return left.last();
            }

            Node<K, V> node = this;
            Node<K, V> parent = node.parent;
            while (parent != null) {
                if (parent.right == node) {
                    return parent;
                }
                node = parent;
                parent = node.parent;
            }
            return null;
        }

        /**
         * Returns the first node in this subtree.
         */
        public Node<K, V> first() {
            Node<K, V> node = this;
            Node<K, V> child = node.left;
            while (child != null) {
                node = child;
                child = node.left;
            }
            return node;
        }

        /**
         * Returns the last node in this subtree.
         */
        public Node<K, V> last() {
            Node<K, V> node = this;
            Node<K, V> child = node.right;
            while (child != null) {
                node = child;
                child = node.right;
            }
            return node;
        }
    }

    /**
     * Walk the nodes of the tree left-to-right or right-to-left. Note that in
     * descending iterations, {@code next} will return the previous node.
     */
    abstract class MapIterator<T> implements Iterator<T> {
        protected Node<K, V> next;
        protected Node<K, V> last;
        protected int expectedModCount = modCount;

        MapIterator(Node<K, V> next) {
            this.next = next;
        }

        public boolean hasNext() {
            return next != null;
        }

        protected Node<K, V> stepForward() {
            if (next == null) {
                throw new NoSuchElementException();
            }
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            last = next;
            next = next.next();
            return last;
        }

        protected Node<K, V> stepBackward() {
            if (next == null) {
                throw new NoSuchElementException();
            }
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            last = next;
            next = next.prev();
            return last;
        }

        public void remove() {
            if (last == null) {
                throw new IllegalStateException();
            }
            removeInternal(last);
            expectedModCount = modCount;
            last = null;
        }
    }

    /*
     * View implementations.
     */

    @WeakOuter
    class EntrySet extends AbstractSet<Map.Entry<K, V>> {
        @Override public int size() {
            return size;
        }

        @Override public Iterator<Entry<K, V>> iterator() {
            return new MapIterator<Entry<K, V>>(firstNode()) {
                public Entry<K, V> next() {
                    return stepForward();
                }
            };
        }

        @Override public boolean contains(Object o) {
            return o instanceof Entry && findByEntry((Entry<?, ?>) o) != null;
        }

        @Override public boolean remove(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }

            Node<K, V> node = findByEntry((Entry<?, ?>) o);
            if (node == null) {
                return false;
            }
            removeInternal(node);
            return true;
        }

        @Override public void clear() {
            TreeMap.this.clear();
        }

        /*-[
        - (NSUInteger)countByEnumeratingWithState:(NSFastEnumerationState *)state
                                          objects:(__unsafe_unretained id *)stackbuf
                                            count:(NSUInteger)len {
          SetEndpoints(this$0_, state);
          return EnumerateEntries(this$0_, state, stackbuf, len, 0, YES);
        }
        ]-*/
    }

    @WeakOuter
    class KeySet extends AbstractSet<K> implements NavigableSet<K> {
        @Override public int size() {
            return size;
        }

        @Override public Iterator<K> iterator() {
            return new MapIterator<K>(firstNode()) {
                public K next() {
                    return stepForward().key;
                }
            };
        }

        public Iterator<K> descendingIterator() {
            return new MapIterator<K>(lastNode()) {
                public K next() {
                    return stepBackward().key;
                }
            };
        }

        @Override public boolean contains(Object o) {
            return containsKey(o);
        }

        @Override public boolean remove(Object key) {
            return removeInternalByKey(key) != null;
        }

        @Override public void clear() {
            TreeMap.this.clear();
        }

        public Comparator<? super K> comparator() {
            return TreeMap.this.comparator();
        }

        /*
         * Navigable methods.
         */

        public K first() {
            return firstKey();
        }

        public K last() {
            return lastKey();
        }

        public K lower(K key) {
            return lowerKey(key);
        }

        public K floor(K key) {
            return floorKey(key);
        }

        public K ceiling(K key) {
            return ceilingKey(key);
        }

        public K higher(K key) {
            return higherKey(key);
        }

        public K pollFirst() {
            Entry<K, V> entry = internalPollFirstEntry();
            return entry != null ? entry.getKey() : null;
        }

        public K pollLast() {
            Entry<K, V> entry = internalPollLastEntry();
            return entry != null ? entry.getKey() : null;
        }

        /*
         * View factory methods.
         */

        public NavigableSet<K> subSet(K from, boolean fromInclusive, K to, boolean toInclusive) {
            return TreeMap.this.subMap(from, fromInclusive, to, toInclusive).navigableKeySet();
        }

        public SortedSet<K> subSet(K fromInclusive, K toExclusive) {
            return TreeMap.this.subMap(fromInclusive, true, toExclusive, false).navigableKeySet();
        }

        public NavigableSet<K> headSet(K to, boolean inclusive) {
            return TreeMap.this.headMap(to, inclusive).navigableKeySet();
        }

        public SortedSet<K> headSet(K toExclusive) {
            return TreeMap.this.headMap(toExclusive, false).navigableKeySet();
        }

        public NavigableSet<K> tailSet(K from, boolean inclusive) {
            return TreeMap.this.tailMap(from, inclusive).navigableKeySet();
        }

        public SortedSet<K> tailSet(K fromInclusive) {
            return TreeMap.this.tailMap(fromInclusive, true).navigableKeySet();
        }

        public NavigableSet<K> descendingSet() {
            return new BoundedMap(false, null, NO_BOUND, null, NO_BOUND).navigableKeySet();
        }

        /*-[
        - (NSUInteger)countByEnumeratingWithState:(NSFastEnumerationState *)state
                                          objects:(__unsafe_unretained id *)stackbuf
                                            count:(NSUInteger)len {
          SetEndpoints(this$0_, state);
          return EnumerateEntries(this$0_, state, stackbuf, len, 1, YES);
        }
        ]-*/
    }

    @WeakOuter
    class ValuesCollection extends AbstractCollection<V> {
        @Override public int size() {
            return size;
        }

        @Override public Iterator<V> iterator() {
            return new MapIterator<V>(firstNode()) {
                public V next() {
                    return stepForward().value;
                }
            };
        }

        /*-[
        - (NSUInteger)countByEnumeratingWithState:(NSFastEnumerationState *)state
                                          objects:(__unsafe_unretained id *)stackbuf
                                            count:(NSUInteger)len {
          SetEndpoints(this$0_, state);
          return EnumerateEntries(this$0_, state, stackbuf, len, 2, YES);
        }
        ]-*/
    }

    /*
     * Bounded views implementations.
     */

    enum Bound {
        INCLUSIVE {
            @Override public String leftCap(Object from) {
                return "[" + from;
            }
            @Override public String rightCap(Object to) {
                return to + "]";
            }
        },
        EXCLUSIVE {
            @Override public String leftCap(Object from) {
                return "(" + from;
            }
            @Override public String rightCap(Object to) {
                return to + ")";
            }
        },
        NO_BOUND {
            @Override public String leftCap(Object from) {
                return ".";
            }
            @Override public String rightCap(Object to) {
                return ".";
            }
        };

        public abstract String leftCap(Object from);
        public abstract String rightCap(Object to);
    }

    /**
     * A map with optional limits on its range.
     */
    final class BoundedMap extends AbstractMap<K, V> implements NavigableMap<K, V>, Serializable {
        private final transient boolean ascending;
        private final transient K from;
        private final transient Bound fromBound;
        private final transient K to;
        private final transient Bound toBound;

        BoundedMap(boolean ascending, K from, Bound fromBound, K to, Bound toBound) {
            /*
             * Validate the bounds. In addition to checking that from <= to, we
             * verify that the comparator supports our bound objects.
             */
            if (fromBound != NO_BOUND && toBound != NO_BOUND) {
                if (comparator.compare(from, to) > 0) {
                    throw new IllegalArgumentException(from + " > " + to);
                }
            } else if (fromBound != NO_BOUND) {
                comparator.compare(from, from);
            } else if (toBound != NO_BOUND) {
                comparator.compare(to, to);
            }

            this.ascending = ascending;
            this.from = from;
            this.fromBound = fromBound;
            this.to = to;
            this.toBound = toBound;
        }

        @Override public int size() {
            return count(entrySet().iterator());
        }

        @Override public boolean isEmpty() {
            return endpoint(true) == null;
        }

        @Override public V get(Object key) {
            return isInBounds(key) ? TreeMap.this.get(key) : null;
        }

        @Override public boolean containsKey(Object key) {
            return isInBounds(key) && TreeMap.this.containsKey(key);
        }

        @Override public V put(K key, V value) {
            if (!isInBounds(key)) {
                throw outOfBounds(key, fromBound, toBound);
            }
            return putInternal(key, value);
        }

        @Override public V remove(Object key) {
            return isInBounds(key) ? TreeMap.this.remove(key) : null;
        }

        /**
         * Returns true if the key is in bounds.
         */
        @SuppressWarnings("unchecked") // this method throws ClassCastExceptions!
        private boolean isInBounds(Object key) {
            return isInBounds((K) key, fromBound, toBound);
        }

        /**
         * Returns true if the key is in bounds. Use this overload with
         * NO_BOUND to skip bounds checking on either end.
         */
        private boolean isInBounds(K key, Bound fromBound, Bound toBound) {
            if (fromBound == INCLUSIVE) {
                if (comparator.compare(key, from) < 0) {
                    return false; // less than from
                }
            } else if (fromBound == EXCLUSIVE) {
                if (comparator.compare(key, from) <= 0) {
                    return false; // less than or equal to from
                }
            }

            if (toBound == INCLUSIVE) {
                if (comparator.compare(key, to) > 0) {
                    return false; // greater than 'to'
                }
            } else if (toBound == EXCLUSIVE) {
                if (comparator.compare(key, to) >= 0) {
                    return false; // greater than or equal to 'to'
                }
            }

            return true;
        }

        /**
         * Returns the entry if it is in bounds, or null if it is out of bounds.
         */
        private Node<K, V> bound(Node<K, V> node, Bound fromBound, Bound toBound) {
            return node != null && isInBounds(node.getKey(), fromBound, toBound) ? node : null;
        }

        /*
         * Navigable methods.
         */

        public Entry<K, V> firstEntry() {
            return immutableCopy(endpoint(true));
        }

        public Entry<K, V> pollFirstEntry() {
            Node<K, V> result = endpoint(true);
            if (result != null) {
                removeInternal(result);
            }
            return immutableCopy(result);
        }

        public K firstKey() {
            Entry<K, V> entry = endpoint(true);
            if (entry == null) {
                throw new NoSuchElementException();
            }
            return entry.getKey();
        }

        public Entry<K, V> lastEntry() {
            return immutableCopy(endpoint(false));
        }

        public Entry<K, V> pollLastEntry() {
            Node<K, V> result = endpoint(false);
            if (result != null) {
                removeInternal(result);
            }
            return immutableCopy(result);
        }

        public K lastKey() {
            Entry<K, V> entry = endpoint(false);
            if (entry == null) {
                throw new NoSuchElementException();
            }
            return entry.getKey();
        }

        /**
         * @param first true for the first element, false for the last.
         */
        private Node<K, V> endpoint(boolean first) {
            Node<K, V> node;
            if (ascending == first) {
                switch (fromBound) {
                    case NO_BOUND:
                        node = firstNode();
                        break;
                    case INCLUSIVE:
                        node = find(from, CEILING);
                        break;
                    case EXCLUSIVE:
                        node = find(from, HIGHER);
                        break;
                    default:
                        throw new AssertionError();
                }
                return bound(node, NO_BOUND, toBound);
            } else {
                switch (toBound) {
                    case NO_BOUND:
                        node = lastNode();
                        break;
                    case INCLUSIVE:
                        node = find(to, FLOOR);
                        break;
                    case EXCLUSIVE:
                        node = find(to, LOWER);
                        break;
                    default:
                        throw new AssertionError();
                }
                return bound(node, fromBound, NO_BOUND);
            }
        }

        /**
         * Performs a find on the underlying tree after constraining it to the
         * bounds of this view. Examples:
         *
         *   bound is (A..C)
         *   findBounded(B, FLOOR) stays source.find(B, FLOOR)
         *
         *   bound is (A..C)
         *   findBounded(C, FLOOR) becomes source.find(C, LOWER)
         *
         *   bound is (A..C)
         *   findBounded(D, LOWER) becomes source.find(C, LOWER)
         *
         *   bound is (A..C]
         *   findBounded(D, FLOOR) becomes source.find(C, FLOOR)
         *
         *   bound is (A..C]
         *   findBounded(D, LOWER) becomes source.find(C, FLOOR)
         */
        private Entry<K, V> findBounded(K key, Relation relation) {
            relation = relation.forOrder(ascending);
            Bound fromBoundForCheck = fromBound;
            Bound toBoundForCheck = toBound;

            if (toBound != NO_BOUND && (relation == LOWER || relation == FLOOR)) {
                int comparison = comparator.compare(to, key);
                if (comparison <= 0) {
                    key = to;
                    if (toBound == EXCLUSIVE) {
                        relation = LOWER; // 'to' is too high
                    } else if (comparison < 0) {
                        relation = FLOOR; // we already went lower
                    }
                }
                toBoundForCheck = NO_BOUND; // we've already checked the upper bound
            }

            if (fromBound != NO_BOUND && (relation == CEILING || relation == HIGHER)) {
                int comparison = comparator.compare(from, key);
                if (comparison >= 0) {
                    key = from;
                    if (fromBound == EXCLUSIVE) {
                        relation = HIGHER; // 'from' is too low
                    } else if (comparison > 0) {
                        relation = CEILING; // we already went higher
                    }
                }
                fromBoundForCheck = NO_BOUND; // we've already checked the lower bound
            }

            return bound(find(key, relation), fromBoundForCheck, toBoundForCheck);
        }

        public Entry<K, V> lowerEntry(K key) {
            return immutableCopy(findBounded(key, LOWER));
        }

        public K lowerKey(K key) {
            Entry<K, V> entry = findBounded(key, LOWER);
            return entry != null ? entry.getKey() : null;
        }

        public Entry<K, V> floorEntry(K key) {
            return immutableCopy(findBounded(key, FLOOR));
        }

        public K floorKey(K key) {
            Entry<K, V> entry = findBounded(key, FLOOR);
            return entry != null ? entry.getKey() : null;
        }

        public Entry<K, V> ceilingEntry(K key) {
            return immutableCopy(findBounded(key, CEILING));
        }

        public K ceilingKey(K key) {
            Entry<K, V> entry = findBounded(key, CEILING);
            return entry != null ? entry.getKey() : null;
        }

        public Entry<K, V> higherEntry(K key) {
            return immutableCopy(findBounded(key, HIGHER));
        }

        public K higherKey(K key) {
            Entry<K, V> entry = findBounded(key, HIGHER);
            return entry != null ? entry.getKey() : null;
        }

        public Comparator<? super K> comparator() {
            Comparator<? super K> forward = TreeMap.this.comparator();
            if (ascending) {
                return forward;
            } else {
                return Collections.reverseOrder(forward);
            }
        }

        /*
         * View factory methods.
         */

        private transient BoundedEntrySet entrySet;
        private transient BoundedKeySet keySet;
        private transient BoundedValuesCollection valuesCollection;

        @Override public Set<Entry<K, V>> entrySet() {
            BoundedEntrySet result = entrySet;
            return result != null ? result : (entrySet = new BoundedEntrySet());
        }

        @Override public Set<K> keySet() {
            return navigableKeySet();
        }

        public NavigableSet<K> navigableKeySet() {
            BoundedKeySet result = keySet;
            return result != null ? result : (keySet = new BoundedKeySet());
        }

        @Override public Collection<V> values() {
            BoundedValuesCollection result = valuesCollection;
            return result != null ? result : (valuesCollection = new BoundedValuesCollection());
        }

        public NavigableMap<K, V> descendingMap() {
            return new BoundedMap(!ascending, from, fromBound, to, toBound);
        }

        public NavigableSet<K> descendingKeySet() {
            return new BoundedMap(!ascending, from, fromBound, to, toBound).navigableKeySet();
        }

        public NavigableMap<K, V> subMap(K from, boolean fromInclusive, K to, boolean toInclusive) {
            Bound fromBound = fromInclusive ? INCLUSIVE : EXCLUSIVE;
            Bound toBound = toInclusive ? INCLUSIVE : EXCLUSIVE;
            return subMap(from, fromBound, to, toBound);
        }

        public NavigableMap<K, V> subMap(K fromInclusive, K toExclusive) {
            return subMap(fromInclusive, INCLUSIVE, toExclusive, EXCLUSIVE);
        }

        public NavigableMap<K, V> headMap(K to, boolean inclusive) {
            Bound toBound = inclusive ? INCLUSIVE : EXCLUSIVE;
            return subMap(null, NO_BOUND, to, toBound);
        }

        public NavigableMap<K, V> headMap(K toExclusive) {
            return subMap(null, NO_BOUND, toExclusive, EXCLUSIVE);
        }

        public NavigableMap<K, V> tailMap(K from, boolean inclusive) {
            Bound fromBound = inclusive ? INCLUSIVE : EXCLUSIVE;
            return subMap(from, fromBound, null, NO_BOUND);
        }

        public NavigableMap<K, V> tailMap(K fromInclusive) {
            return subMap(fromInclusive, INCLUSIVE, null, NO_BOUND);
        }

        private NavigableMap<K, V> subMap(K from, Bound fromBound, K to, Bound toBound) {
            if (!ascending) {
                K fromTmp = from;
                Bound fromBoundTmp = fromBound;
                from = to;
                fromBound = toBound;
                to = fromTmp;
                toBound = fromBoundTmp;
            }

            /*
             * If both the current and requested bounds are exclusive, the isInBounds check must be
             * inclusive. For example, to create (C..F) from (A..F), the bound 'F' is in bounds.
             */

            if (fromBound == NO_BOUND) {
                from = this.from;
                fromBound = this.fromBound;
            } else {
                Bound fromBoundToCheck = fromBound == this.fromBound ? INCLUSIVE : this.fromBound;
                if (!isInBounds(from, fromBoundToCheck, this.toBound)) {
                    throw outOfBounds(to, fromBoundToCheck, this.toBound);
                }
            }

            if (toBound == NO_BOUND) {
                to = this.to;
                toBound = this.toBound;
            } else {
                Bound toBoundToCheck = toBound == this.toBound ? INCLUSIVE : this.toBound;
                if (!isInBounds(to, this.fromBound, toBoundToCheck)) {
                    throw outOfBounds(to, this.fromBound, toBoundToCheck);
                }
            }

            return new BoundedMap(ascending, from, fromBound, to, toBound);
        }

        private IllegalArgumentException outOfBounds(Object value, Bound fromBound, Bound toBound) {
            return new IllegalArgumentException(value + " not in range "
                    + fromBound.leftCap(from) + ".." + toBound.rightCap(to));
        }

        /*
         * Bounded view implementations.
         */

        abstract class BoundedIterator<T> extends MapIterator<T> {
            protected BoundedIterator(Node<K, V> next) {
                super(next);
            }

            @Override protected Node<K, V> stepForward() {
                Node<K, V> result = super.stepForward();
                if (next != null && !isInBounds(next.key, NO_BOUND, toBound)) {
                    next = null;
                }
                return result;
            }

            @Override protected Node<K, V> stepBackward() {
                Node<K, V> result = super.stepBackward();
                if (next != null && !isInBounds(next.key, fromBound, NO_BOUND)) {
                    next = null;
                }
                return result;
            }
        }

        @WeakOuter
        final class BoundedEntrySet extends AbstractSet<Entry<K, V>> {
            @Override public int size() {
                return BoundedMap.this.size();
            }

            @Override public boolean isEmpty() {
                return BoundedMap.this.isEmpty();
            }

            @Override public Iterator<Entry<K, V>> iterator() {
                return new BoundedIterator<Entry<K, V>>(endpoint(true)) {
                    public Entry<K, V> next() {
                        return ascending ? stepForward() : stepBackward();
                    }
                };
            }

            @Override public boolean contains(Object o) {
                if (!(o instanceof Entry)) {
                    return false;
                }
                Entry<?, ?> entry = (Entry<?, ?>) o;
                return isInBounds(entry.getKey()) && findByEntry(entry) != null;
            }

            @Override public boolean remove(Object o) {
                if (!(o instanceof Entry)) {
                    return false;
                }
                Entry<?, ?> entry = (Entry<?, ?>) o;
                return isInBounds(entry.getKey()) && TreeMap.this.entrySet().remove(entry);
            }

            /*-[
            - (NSUInteger)countByEnumeratingWithState:(NSFastEnumerationState *)state
                                              objects:(__unsafe_unretained id *)stackbuf
                                                count:(NSUInteger)len {
              SetBoundedMapEndpoints(this$0_, state);
              return EnumerateEntries(
                  this$0_->this$0_, state, stackbuf, len, 0, this$0_->ascending_);
            }
            ]-*/
        }

        @WeakOuter
        final class BoundedKeySet extends AbstractSet<K> implements NavigableSet<K> {
            @Override public int size() {
                return BoundedMap.this.size();
            }

            @Override public boolean isEmpty() {
                return BoundedMap.this.isEmpty();
            }

            @Override public Iterator<K> iterator() {
                return new BoundedIterator<K>(endpoint(true)) {
                    public K next() {
                        return (ascending ? stepForward() : stepBackward()).key;
                    }
                };
            }

            public Iterator<K> descendingIterator() {
                return new BoundedIterator<K>(endpoint(false)) {
                    public K next() {
                        return (ascending ? stepBackward() : stepForward()).key;
                    }
                };
            }

            @Override public boolean contains(Object key) {
                return isInBounds(key) && findByObject(key) != null;
            }

            @Override public boolean remove(Object key) {
                return isInBounds(key) && removeInternalByKey(key) != null;
            }

            /*
             * Navigable methods.
             */

            public K first() {
                return firstKey();
            }

            public K pollFirst() {
                Entry<K, ?> entry = pollFirstEntry();
                return entry != null ? entry.getKey() : null;
            }

            public K last() {
                return lastKey();
            }

            public K pollLast() {
                Entry<K, ?> entry = pollLastEntry();
                return entry != null ? entry.getKey() : null;
            }

            public K lower(K key) {
                return lowerKey(key);
            }

            public K floor(K key) {
                return floorKey(key);
            }

            public K ceiling(K key) {
                return ceilingKey(key);
            }

            public K higher(K key) {
                return higherKey(key);
            }

            public Comparator<? super K> comparator() {
                return BoundedMap.this.comparator();
            }

            /*
             * View factory methods.
             */

            public NavigableSet<K> subSet(K from, boolean fromInclusive, K to, boolean toInclusive) {
                return subMap(from, fromInclusive, to, toInclusive).navigableKeySet();
            }

            public SortedSet<K> subSet(K fromInclusive, K toExclusive) {
                return subMap(fromInclusive, toExclusive).navigableKeySet();
            }

            public NavigableSet<K> headSet(K to, boolean inclusive) {
                return headMap(to, inclusive).navigableKeySet();
            }

            public SortedSet<K> headSet(K toExclusive) {
                return headMap(toExclusive).navigableKeySet();
            }

            public NavigableSet<K> tailSet(K from, boolean inclusive) {
                return tailMap(from, inclusive).navigableKeySet();
            }

            public SortedSet<K> tailSet(K fromInclusive) {
                return tailMap(fromInclusive).navigableKeySet();
            }

            public NavigableSet<K> descendingSet() {
                return new BoundedMap(!ascending, from, fromBound, to, toBound).navigableKeySet();
            }

            /*-[
            - (NSUInteger)countByEnumeratingWithState:(NSFastEnumerationState *)state
                                              objects:(__unsafe_unretained id *)stackbuf
                                                count:(NSUInteger)len {
              SetBoundedMapEndpoints(this$0_, state);
              return EnumerateEntries(
                  this$0_->this$0_, state, stackbuf, len, 1, this$0_->ascending_);
            }
            ]-*/
        }

        @WeakOuter
        final class BoundedValuesCollection extends AbstractCollection<V> {
            @Override public int size() {
                return BoundedMap.this.size();
            }

            @Override public boolean isEmpty() {
                return BoundedMap.this.isEmpty();
            }

            @Override public Iterator<V> iterator() {
                return new BoundedIterator<V>(endpoint(true)) {
                    public V next() {
                        return (ascending ? stepForward() : stepBackward()).value;
                    }
                };
            }

            /*-[
            - (NSUInteger)countByEnumeratingWithState:(NSFastEnumerationState *)state
                                              objects:(__unsafe_unretained id *)stackbuf
                                                count:(NSUInteger)len {
              SetBoundedMapEndpoints(this$0_, state);
              return EnumerateEntries(
                  this$0_->this$0_, state, stackbuf, len, 2, this$0_->ascending_);
            }
            ]-*/
        }

        Object writeReplace() throws ObjectStreamException {
            return ascending
                    ? new AscendingSubMap<K, V>(TreeMap.this, from, fromBound, to, toBound)
                    : new DescendingSubMap<K, V>(TreeMap.this, from, fromBound, to, toBound);
        }
    }

    /**
     * Returns the number of elements in the iteration.
     */
    static int count(Iterator<?> iterator) {
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        return count;
    }

    /*
     * Serialization
     */

    private static final long serialVersionUID = 919286545866124006L;

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.putFields().put("comparator", comparator());
        stream.writeFields();
        stream.writeInt(size);
        for (Map.Entry<K, V> entry : entrySet()) {
            stream.writeObject(entry.getKey());
            stream.writeObject(entry.getValue());
        }
    }

    @SuppressWarnings("unchecked") // we have to trust that keys are Ks and values are Vs
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        GetField fields = stream.readFields();
        comparator = (Comparator<? super K>) fields.get("comparator", null);
        if (comparator == null) {
            comparator = (Comparator<? super K>) NATURAL_ORDER;
        }
        int size = stream.readInt();
        for (int i = 0; i < size; i++) {
            putInternal((K) stream.readObject(), (V) stream.readObject());
        }
    }

    static abstract class NavigableSubMap<K, V> extends AbstractMap<K, V> implements Serializable {
        private static final long serialVersionUID = -2102997345730753016L;
        TreeMap<K, V> m;
        Object lo;
        Object hi;
        boolean fromStart;
        boolean toEnd;
        boolean loInclusive;
        boolean hiInclusive;

        NavigableSubMap(TreeMap<K, V> delegate, K from, Bound fromBound, K to, Bound toBound) {
            this.m = delegate;
            this.lo = from;
            this.hi = to;
            this.fromStart = fromBound == NO_BOUND;
            this.toEnd = toBound == NO_BOUND;
            this.loInclusive = fromBound == INCLUSIVE;
            this.hiInclusive = toBound == INCLUSIVE;
        }

        @Override public Set<Entry<K, V>> entrySet() {
            throw new UnsupportedOperationException();
        }

        @SuppressWarnings("unchecked") // we have to trust that the bounds are Ks
        protected Object readResolve() throws ObjectStreamException {
            Bound fromBound = fromStart ? NO_BOUND : (loInclusive ? INCLUSIVE : EXCLUSIVE);
            Bound toBound = toEnd ? NO_BOUND : (hiInclusive ? INCLUSIVE : EXCLUSIVE);
            boolean ascending = !(this instanceof DescendingSubMap);
            return m.new BoundedMap(ascending, (K) lo, fromBound, (K) hi, toBound);
        }
    }

    static class DescendingSubMap<K, V> extends NavigableSubMap<K, V> {
        private static final long serialVersionUID = 912986545866120460L;
        Comparator<K> reverseComparator;
        DescendingSubMap(TreeMap<K, V> delegate, K from, Bound fromBound, K to, Bound toBound) {
            super(delegate, from, fromBound, to, toBound);
        }
    }

    static class AscendingSubMap<K, V> extends NavigableSubMap<K, V> {
        private static final long serialVersionUID = 912986545866124060L;
        AscendingSubMap(TreeMap<K, V> delegate, K from, Bound fromBound, K to, Bound toBound) {
            super(delegate, from, fromBound, to, toBound);
        }
    }

    class SubMap extends AbstractMap<K, V> implements Serializable {
        private static final long serialVersionUID = -6520786458950516097L;
        Object fromKey;
        Object toKey;
        boolean fromStart;
        boolean toEnd;

        @Override public Set<Entry<K, V>> entrySet() {
            throw new UnsupportedOperationException();
        }

        @SuppressWarnings("unchecked") // we have to trust that the bounds are Ks
        protected Object readResolve() throws ObjectStreamException {
            Bound fromBound = fromStart ? NO_BOUND : INCLUSIVE;
            Bound toBound = toEnd ? NO_BOUND : EXCLUSIVE;
            return new BoundedMap(true, (K) fromKey, fromBound, (K) toKey, toBound);
        }
    }

    /*-[
    static inline void SetEndpoints(
        __unsafe_unretained JavaUtilTreeMap *map, NSFastEnumerationState *state) {
      if (state->state == 0) {
        state->extra[1] = (unsigned long) [map firstNode];
        state->extra[2] = (unsigned long) [map lastNode];
      }
    }
    ]-*/

    /*-[
    static inline void SetBoundedMapEndpoints(
        __unsafe_unretained JavaUtilTreeMap_BoundedMap *bMap, NSFastEnumerationState *state) {
      if (state->state == 0) {
        state->extra[1] = (unsigned long) [bMap endpointWithBoolean:YES];
        state->extra[2] = (unsigned long) [bMap endpointWithBoolean:NO];
      }
    }
    ]-*/

    /*-[
    static NSUInteger EnumerateEntries(
        __unsafe_unretained JavaUtilTreeMap *map, NSFastEnumerationState *state,
        __unsafe_unretained id *stackbuf, NSUInteger len, int type, BOOL forward) {
      // Note: Must not use extra[4] because it is set by TreeSet.
      __unsafe_unretained JavaUtilTreeMap_Node *node;
      __unsafe_unretained JavaUtilTreeMap_Node *startNode = (ARCBRIDGE id) (void *) state->extra[1];
      __unsafe_unretained JavaUtilTreeMap_Node *endNode = (ARCBRIDGE id) (void *) state->extra[2];
      if (state->state == 0) {
        state->state = 1;
        state->mutationsPtr = (unsigned long *) &map->modCount_;
        node = startNode;
      } else {
        node = (ARCBRIDGE id) (void *) state->extra[0];
      }
      state->itemsPtr = stackbuf;
      NSUInteger objCount = 0;
      while (node && objCount < len) {
        switch (type) {
          case 1 : *stackbuf++ = node->key_; break;
          case 2 : *stackbuf++ = node->value_; break;
          default : *stackbuf++ = node; break;
        }
        objCount++;
        if (node == endNode) {
          node = nil;
        } else {
          node = forward ? [node next] : [node prev];
        }
      }
      state->extra[0] = (unsigned long) node;
      return objCount;
    }
    ]-*/
}
