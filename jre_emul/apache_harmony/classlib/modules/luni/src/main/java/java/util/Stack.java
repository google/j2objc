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
 * {@code Stack} is a Last-In/First-Out(LIFO) data structure which represents a
 * stack of objects. It enables users to pop to and push from the stack,
 * including null objects. There is no limit to the size of the stack.
 */
public class Stack<E> extends Vector<E> {

    private static final long serialVersionUID = 1224463164541339165L;

    /**
     * Constructs a stack with the default size of {@code Vector}.
     */
    public Stack() {
        super();
    }

    /**
     * Returns whether the stack is empty or not.
     * 
     * @return {@code true} if the stack is empty, {@code false} otherwise.
     */
    public boolean empty() {
        return isEmpty();
    }

    /**
     * Returns the element at the top of the stack without removing it.
     * 
     * @return the element at the top of the stack.
     * @throws EmptyStackException
     *             if the stack is empty.
     * @see #pop
     */
    @SuppressWarnings("unchecked")
    public synchronized E peek() {
        try {
            return (E) elementData[elementCount - 1];
        } catch (IndexOutOfBoundsException e) {
            throw new EmptyStackException();
        }
    }

    /**
     * Returns the element at the top of the stack and removes it.
     * 
     * @return the element at the top of the stack.
     * @throws EmptyStackException
     *             if the stack is empty.
     * @see #peek
     * @see #push
     */
    @SuppressWarnings("unchecked")
    public synchronized E pop() {
        if (elementCount == 0) {
            throw new EmptyStackException();
        }
        final int index = --elementCount;
        final E obj = (E) elementData[index];
        elementData[index] = null;
        modCount++;
        return obj;
    }

    /**
     * Pushes the specified object onto the top of the stack.
     * 
     * @param object
     *            The object to be added on top of the stack.
     * @return the object argument.
     * @see #peek
     * @see #pop
     */
    public E push(E object) {
        addElement(object);
        return object;
    }

    /**
     * Returns the index of the first occurrence of the object, starting from
     * the top of the stack.
     * 
     * @return the index of the first occurrence of the object, assuming that
     *         the topmost object on the stack has a distance of one.
     * @param o
     *            the object to be searched.
     */
    public synchronized int search(Object o) {
        final Object[] dumpArray = elementData;
        final int size = elementCount;
        if (o != null) {
            for (int i = size - 1; i >= 0; i--) {
                if (o.equals(dumpArray[i])) {
                    return size - i;
                }
            }
        } else {
            for (int i = size - 1; i >= 0; i--) {
                if (dumpArray[i] == null) {
                    return size - i;
                }
            }
        }
        return -1;
    }
}
