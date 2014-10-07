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
 * Observable is used to notify a group of Observer objects when a change
 * occurs. On creation, the set of observers is empty. After a change occurred,
 * the application can call the {@link #notifyObservers()} method. This will
 * cause the invocation of the {@code update()} method of all registered
 * Observers. The order of invocation is not specified. This implementation will
 * call the Observers in the order they registered. Subclasses are completely
 * free in what order they call the update methods.
 *
 * @see Observer
 */
public class Observable {

    List<Observer> observers = new ArrayList<Observer>();

    boolean changed = false;

    /**
     * Constructs a new {@code Observable} object.
     */
    public Observable() {
    }

    /**
     * Adds the specified observer to the list of observers. If it is already
     * registered, it is not added a second time.
     *
     * @param observer
     *            the Observer to add.
     */
    public void addObserver(Observer observer) {
        if (observer == null) {
            throw new NullPointerException("observer == null");
        }
        synchronized (this) {
            if (!observers.contains(observer))
                observers.add(observer);
        }
    }

    /**
     * Clears the changed flag for this {@code Observable}. After calling
     * {@code clearChanged()}, {@code hasChanged()} will return {@code false}.
     */
    protected void clearChanged() {
        changed = false;
    }

    /**
     * Returns the number of observers registered to this {@code Observable}.
     *
     * @return the number of observers.
     */
    public int countObservers() {
        return observers.size();
    }

    /**
     * Removes the specified observer from the list of observers. Passing null
     * won't do anything.
     *
     * @param observer
     *            the observer to remove.
     */
    public synchronized void deleteObserver(Observer observer) {
        observers.remove(observer);
    }

    /**
     * Removes all observers from the list of observers.
     */
    public synchronized void deleteObservers() {
        observers.clear();
    }

    /**
     * Returns the changed flag for this {@code Observable}.
     *
     * @return {@code true} when the changed flag for this {@code Observable} is
     *         set, {@code false} otherwise.
     */
    public boolean hasChanged() {
        return changed;
    }

    /**
     * If {@code hasChanged()} returns {@code true}, calls the {@code update()}
     * method for every observer in the list of observers using null as the
     * argument. Afterwards, calls {@code clearChanged()}.
     * <p>
     * Equivalent to calling {@code notifyObservers(null)}.
     */
    public void notifyObservers() {
        notifyObservers(null);
    }

    /**
     * If {@code hasChanged()} returns {@code true}, calls the {@code update()}
     * method for every Observer in the list of observers using the specified
     * argument. Afterwards calls {@code clearChanged()}.
     *
     * @param data
     *            the argument passed to {@code update()}.
     */
    @SuppressWarnings("unchecked")
    public void notifyObservers(Object data) {
        int size = 0;
        Observer[] arrays = null;
        synchronized (this) {
            if (hasChanged()) {
                clearChanged();
                size = observers.size();
                arrays = new Observer[size];
                observers.toArray(arrays);
            }
        }
        if (arrays != null) {
            for (Observer observer : arrays) {
                observer.update(this, data);
            }
        }
    }

    /**
     * Sets the changed flag for this {@code Observable}. After calling
     * {@code setChanged()}, {@code hasChanged()} will return {@code true}.
     */
    protected void setChanged() {
        changed = true;
    }
}
