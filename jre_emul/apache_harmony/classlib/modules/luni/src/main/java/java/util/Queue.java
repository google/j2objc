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
package java.util;

/**
 * This kind of collection provides advanced operations compared to basic
 * collections, such as insertion, extraction, and inspection.
 * <p>
 * Generally, a queue orders its elements by means of first-in-first-out.
 * However, a priority queue orders its elements according to a comparator
 * specified or the elements' natural order. Furthermore, a stack orders its
 * elements last-in-first out.
 * <p>
 * A typical queue does not allow {@code null} to be inserted as its element,
 * while some implementations such as {@code LinkedList} allow it. But {@code
 * null} should not be inserted even in these implementations, since the method
 * {@code poll} returns {@code null} to indicate that there is no element left
 * in the queue.
 * <p>
 * {@code Queue} does not provide blocking queue methods, which would block
 * until the operation of the method is allowed. See the
 * {@link java.util.concurrent.BlockingQueue} interface for information about
 * blocking queue methods.
 */
public interface Queue<E> extends Collection<E> {

    /**
     * Inserts the specified element into the queue provided that the condition
     * allows such an operation. The method is generally preferable to
     * {@link Collection#add}, since the latter might throw an exception if the
     * operation fails.
     * 
     * @param o
     *            the specified element to insert into the queue.
     * @return {@code true} if the operation succeeds and {@code false} if it
     *         fails.
     */
    public boolean offer(E o);

    /**
     * Gets and removes the element at the head of the queue, or returns {@code
     * null} if there is no element in the queue.
     * 
     * @return the element at the head of the queue or {@code null} if there is
     *         no element in the queue.
     */
    public E poll();

    /**
     * Gets and removes the element at the head of the queue. Throws a
     * NoSuchElementException if there is no element in the queue.
     * 
     * @return the element at the head of the queue.
     * @throws NoSuchElementException
     *             if there is no element in the queue.
     */
    public E remove();

    /**
     * Gets but does not remove the element at the head of the queue.
     * 
     * @return the element at the head of the queue or {@code null} if there is
     *         no element in the queue.
     */
    public E peek();

    /**
     * Gets but does not remove the element at the head of the queue. Throws a
     * {@code NoSuchElementException} if there is no element in the queue.
     * 
     * @return the element at the head of the queue.
     * @throws NoSuchElementException
     *             if there is no element in the queue.
     */
    public E element();

}
