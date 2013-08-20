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
 * Timers schedule one-shot or recurring {@link TimerTask tasks} for execution.
 * Prefer {@link java.util.concurrent.ScheduledThreadPoolExecutor
 * ScheduledThreadPoolExecutor} for new code.
 *
 * <p>Each timer has one thread on which tasks are executed sequentially. When
 * this thread is busy running a task, runnable tasks may be subject to delays.
 *
 * <p>One-shot are scheduled to run at an absolute time or after a relative
 * delay.
 *
 * <p>Recurring tasks are scheduled with either a fixed period or a fixed rate:
 * <ul>
 *   <li>With the default <strong>fixed-period execution</strong>, each
 *       successive run of a task is scheduled relative to the start time of
 *       the previous run, so two runs are never fired closer together in time
 *       than the specified {@code period}.
 *   <li>With <strong>fixed-rate execution</strong>, the start time of each
 *       successive run of a task is scheduled without regard for when the
 *       previous run took place. This may result in a series of bunched-up runs
 *       (one launched immediately after another) if delays prevent the timer
 *       from starting tasks on time.
 * </ul>
 *
 * <p>When a timer is no longer needed, users should call {@link #cancel}, which
 * releases the timer's thread and other resources. Timers not explicitly
 * cancelled may hold resources indefinitely.
 *
 * <p>This class does not offer guarantees about the real-time nature of task
 * scheduling. Multiple threads can share a single timer without
 * synchronization.
 */
public class Timer {

    private static final class TimerImpl extends Thread {

        private static final class TimerHeap {
            private int DEFAULT_HEAP_SIZE = 256;

            private TimerTask[] timers = new TimerTask[DEFAULT_HEAP_SIZE];

            private int size = 0;

            private int deletedCancelledNumber = 0;

            public TimerTask minimum() {
                return timers[0];
            }

            public boolean isEmpty() {
                return size == 0;
            }

            public void insert(TimerTask task) {
                if (timers.length == size) {
                    TimerTask[] appendedTimers = new TimerTask[size * 2];
                    System.arraycopy(timers, 0, appendedTimers, 0, size);
                    timers = appendedTimers;
                }
                timers[size++] = task;
                upHeap();
            }

            public void delete(int pos) {
                // posible to delete any position of the heap
                if (pos >= 0 && pos < size) {
                    timers[pos] = timers[--size];
                    timers[size] = null;
                    downHeap(pos);
                }
            }

            private void upHeap() {
                int current = size - 1;
                int parent = (current - 1) / 2;

                while (timers[current].when < timers[parent].when) {
                    // swap the two
                    TimerTask tmp = timers[current];
                    timers[current] = timers[parent];
                    timers[parent] = tmp;

                    // update pos and current
                    current = parent;
                    parent = (current - 1) / 2;
                }
            }

            private void downHeap(int pos) {
                int current = pos;
                int child = 2 * current + 1;

                while (child < size && size > 0) {
                    // compare the children if they exist
                    if (child + 1 < size
                            && timers[child + 1].when < timers[child].when) {
                        child++;
                    }

                    // compare selected child with parent
                    if (timers[current].when < timers[child].when) {
                        break;
                    }

                    // swap the two
                    TimerTask tmp = timers[current];
                    timers[current] = timers[child];
                    timers[child] = tmp;

                    // update pos and current
                    current = child;
                    child = 2 * current + 1;
                }
            }

            public void reset() {
                timers = new TimerTask[DEFAULT_HEAP_SIZE];
                size = 0;
            }

            public void adjustMinimum() {
                downHeap(0);
            }

            public void deleteIfCancelled() {
                for (int i = 0; i < size; i++) {
                    if (timers[i].cancelled) {
                        deletedCancelledNumber++;
                        delete(i);
                        // re-try this point
                        i--;
                    }
                }
            }

            private int getTask(TimerTask task) {
                for (int i = 0; i < timers.length; i++) {
                    if (timers[i] == task) {
                        return i;
                    }
                }
                return -1;
            }

        }

        /**
         * True if the method cancel() of the Timer was called or the !!!stop()
         * method was invoked
         */
        private boolean cancelled;

        /**
         * True if the Timer has become garbage
         */
        private boolean finished;

        /**
         * Contains scheduled events, sorted according to
         * {@code when} field of TaskScheduled object.
         */
        private TimerHeap tasks = new TimerHeap();

        /**
         * Starts a new timer.
         *
         * @param name thread's name
         * @param isDaemon daemon thread or not
         */
        TimerImpl(String name, boolean isDaemon) {
            this.setName(name);
            this.setDaemon(isDaemon);
            this.start();
        }

        /**
         * This method will be launched on separate thread for each Timer
         * object.
         */
        @Override
        public void run() {
            while (true) {
                TimerTask task;
                synchronized (this) {
                    // need to check cancelled inside the synchronized block
                    if (cancelled) {
                        return;
                    }
                    if (tasks.isEmpty()) {
                        if (finished) {
                            return;
                        }
                        // no tasks scheduled -- sleep until any task appear
                        try {
                            this.wait();
                        } catch (InterruptedException ignored) {
                        }
                        continue;
                    }

                    long currentTime = System.currentTimeMillis();

                    task = tasks.minimum();
                    long timeToSleep;

                    synchronized (task.lock) {
                        if (task.cancelled) {
                            tasks.delete(0);
                            continue;
                        }

                        // check the time to sleep for the first task scheduled
                        timeToSleep = task.when - currentTime;
                    }

                    if (timeToSleep > 0) {
                        // sleep!
                        try {
                            this.wait(timeToSleep);
                        } catch (InterruptedException ignored) {
                        }
                        continue;
                    }

                    // no sleep is necessary before launching the task

                    synchronized (task.lock) {
                        int pos = 0;
                        if (tasks.minimum().when != task.when) {
                            pos = tasks.getTask(task);
                        }
                        if (task.cancelled) {
                            tasks.delete(tasks.getTask(task));
                            continue;
                        }

                        // set time to schedule
                        task.setScheduledTime(task.when);

                        // remove task from queue
                        tasks.delete(pos);

                        // set when the next task should be launched
                        if (task.period >= 0) {
                            // this is a repeating task,
                            if (task.fixedRate) {
                                // task is scheduled at fixed rate
                                task.when = task.when + task.period;
                            } else {
                                // task is scheduled at fixed delay
                                task.when = System.currentTimeMillis()
                                        + task.period;
                            }

                            // insert this task into queue
                            insertTask(task);
                        } else {
                            task.when = 0;
                        }
                    }
                }

                boolean taskCompletedNormally = false;
                try {
                    task.run();
                    taskCompletedNormally = true;
                } finally {
                    if (!taskCompletedNormally) {
                        synchronized (this) {
                            cancelled = true;
                        }
                    }
                }
            }
        }

        private void insertTask(TimerTask newTask) {
            // callers are synchronized
            tasks.insert(newTask);
            this.notify();
        }

        /**
         * Cancels timer.
         */
        public synchronized void cancel() {
            cancelled = true;
            tasks.reset();
            this.notify();
        }

        public int purge() {
            if (tasks.isEmpty()) {
                return 0;
            }
            // callers are synchronized
            tasks.deletedCancelledNumber = 0;
            tasks.deleteIfCancelled();
            return tasks.deletedCancelledNumber;
        }

    }

    private static final class FinalizerHelper {
        private final TimerImpl impl;

        FinalizerHelper(TimerImpl impl) {
            this.impl = impl;
        }

        @Override protected void finalize() throws Throwable {
            try {
                synchronized (impl) {
                    impl.finished = true;
                    impl.notify();
                }
            } finally {
                super.finalize();
            }
        }
    }

    private static long timerId;

    private synchronized static long nextId() {
        return timerId++;
    }

    /* This object will be used in synchronization purposes */
    private final TimerImpl impl;

    // Used to finalize thread
    @SuppressWarnings("unused")
    private final FinalizerHelper finalizer;

    /**
     * Creates a new named {@code Timer} which may be specified to be run as a
     * daemon thread.
     *
     * @param name the name of the {@code Timer}.
     * @param isDaemon true if {@code Timer}'s thread should be a daemon thread.
     * @throws NullPointerException is {@code name} is {@code null}
     */
    public Timer(String name, boolean isDaemon) {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        this.impl = new TimerImpl(name, isDaemon);
        this.finalizer = new FinalizerHelper(impl);
    }

    /**
     * Creates a new named {@code Timer} which does not run as a daemon thread.
     *
     * @param name the name of the Timer.
     * @throws NullPointerException is {@code name} is {@code null}
     */
    public Timer(String name) {
        this(name, false);
    }

    /**
     * Creates a new {@code Timer} which may be specified to be run as a daemon thread.
     *
     * @param isDaemon {@code true} if the {@code Timer}'s thread should be a daemon thread.
     */
    public Timer(boolean isDaemon) {
        this("Timer-" + Timer.nextId(), isDaemon);
    }

    /**
     * Creates a new non-daemon {@code Timer}.
     */
    public Timer() {
        this(false);
    }

    /**
     * Cancels the {@code Timer} and all scheduled tasks. If there is a
     * currently running task it is not affected. No more tasks may be scheduled
     * on this {@code Timer}. Subsequent calls do nothing.
     */
    public void cancel() {
        impl.cancel();
    }

    /**
     * Removes all canceled tasks from the task queue. If there are no
     * other references on the tasks, then after this call they are free
     * to be garbage collected.
     *
     * @return the number of canceled tasks that were removed from the task
     *         queue.
     */
    public int purge() {
        synchronized (impl) {
            return impl.purge();
        }
    }

    /**
     * Schedule a task for single execution. If {@code when} is less than the
     * current time, it will be scheduled to be executed as soon as possible.
     *
     * @param task
     *            the task to schedule.
     * @param when
     *            time of execution.
     * @throws IllegalArgumentException
     *                if {@code when.getTime() < 0}.
     * @throws IllegalStateException
     *                if the {@code Timer} has been canceled, or if the task has been
     *                scheduled or canceled.
     */
    public void schedule(TimerTask task, Date when) {
        if (when.getTime() < 0) {
            throw new IllegalArgumentException("when < 0: " + when.getTime());
        }
        long delay = when.getTime() - System.currentTimeMillis();
        scheduleImpl(task, delay < 0 ? 0 : delay, -1, false);
    }

    /**
     * Schedule a task for single execution after a specified delay.
     *
     * @param task
     *            the task to schedule.
     * @param delay
     *            amount of time in milliseconds before execution.
     * @throws IllegalArgumentException
     *                if {@code delay < 0}.
     * @throws IllegalStateException
     *                if the {@code Timer} has been canceled, or if the task has been
     *                scheduled or canceled.
     */
    public void schedule(TimerTask task, long delay) {
        if (delay < 0) {
            throw new IllegalArgumentException("delay < 0: " + delay);
        }
        scheduleImpl(task, delay, -1, false);
    }

    /**
     * Schedule a task for repeated fixed-delay execution after a specific delay.
     *
     * @param task
     *            the task to schedule.
     * @param delay
     *            amount of time in milliseconds before first execution.
     * @param period
     *            amount of time in milliseconds between subsequent executions.
     * @throws IllegalArgumentException
     *                if {@code delay < 0} or {@code period <= 0}.
     * @throws IllegalStateException
     *                if the {@code Timer} has been canceled, or if the task has been
     *                scheduled or canceled.
     */
    public void schedule(TimerTask task, long delay, long period) {
        if (delay < 0 || period <= 0) {
            throw new IllegalArgumentException();
        }
        scheduleImpl(task, delay, period, false);
    }

    /**
     * Schedule a task for repeated fixed-delay execution after a specific time
     * has been reached.
     *
     * @param task
     *            the task to schedule.
     * @param when
     *            time of first execution.
     * @param period
     *            amount of time in milliseconds between subsequent executions.
     * @throws IllegalArgumentException
     *                if {@code when.getTime() < 0} or {@code period <= 0}.
     * @throws IllegalStateException
     *                if the {@code Timer} has been canceled, or if the task has been
     *                scheduled or canceled.
     */
    public void schedule(TimerTask task, Date when, long period) {
        if (period <= 0 || when.getTime() < 0) {
            throw new IllegalArgumentException();
        }
        long delay = when.getTime() - System.currentTimeMillis();
        scheduleImpl(task, delay < 0 ? 0 : delay, period, false);
    }

    /**
     * Schedule a task for repeated fixed-rate execution after a specific delay
     * has passed.
     *
     * @param task
     *            the task to schedule.
     * @param delay
     *            amount of time in milliseconds before first execution.
     * @param period
     *            amount of time in milliseconds between subsequent executions.
     * @throws IllegalArgumentException
     *                if {@code delay < 0} or {@code period <= 0}.
     * @throws IllegalStateException
     *                if the {@code Timer} has been canceled, or if the task has been
     *                scheduled or canceled.
     */
    public void scheduleAtFixedRate(TimerTask task, long delay, long period) {
        if (delay < 0 || period <= 0) {
            throw new IllegalArgumentException();
        }
        scheduleImpl(task, delay, period, true);
    }

    /**
     * Schedule a task for repeated fixed-rate execution after a specific time
     * has been reached.
     *
     * @param task
     *            the task to schedule.
     * @param when
     *            time of first execution.
     * @param period
     *            amount of time in milliseconds between subsequent executions.
     * @throws IllegalArgumentException
     *                if {@code when.getTime() < 0} or {@code period <= 0}.
     * @throws IllegalStateException
     *                if the {@code Timer} has been canceled, or if the task has been
     *                scheduled or canceled.
     */
    public void scheduleAtFixedRate(TimerTask task, Date when, long period) {
        if (period <= 0 || when.getTime() < 0) {
            throw new IllegalArgumentException();
        }
        long delay = when.getTime() - System.currentTimeMillis();
        scheduleImpl(task, delay, period, true);
    }

    /*
     * Schedule a task.
     */
    private void scheduleImpl(TimerTask task, long delay, long period, boolean fixed) {
        synchronized (impl) {
            if (impl.cancelled) {
                throw new IllegalStateException("Timer was canceled");
            }

            long when = delay + System.currentTimeMillis();

            if (when < 0) {
                throw new IllegalArgumentException("Illegal delay to start the TimerTask: " + when);
            }

            synchronized (task.lock) {
                if (task.isScheduled()) {
                    throw new IllegalStateException("TimerTask is scheduled already");
                }

                if (task.cancelled) {
                    throw new IllegalStateException("TimerTask is canceled");
                }

                task.when = when;
                task.period = period;
                task.fixedRate = fixed;
            }

            // insert the newTask into queue
            impl.insertTask(task);
        }
    }
}
