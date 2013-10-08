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

package tests.api.java.util;

import junit.framework.TestCase;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TimerTest extends TestCase {

    int timerCounter = 0;

    private final Object sync = new Object();

    /**
     * Warning: These tests have the possibility to leave a VM hanging if the
     * Timer is not cancelled.
     */
    class TimerTestTask extends TimerTask {
        int wasRun = 0;

        // Should we sleep for 200 ms each run()?
        boolean sleepInRun = false;

        // Should we increment the timerCounter?
        boolean incrementCount = false;

        // Should we terminate the timer at a specific timerCounter?
        int terminateCount = -1;

        // The timer we belong to
        Timer timer = null;

        public TimerTestTask() {
        }

        public TimerTestTask(Timer t) {
            timer = t;
        }

        public void run() {
            synchronized (this) {
                wasRun++;
            }
            if (incrementCount) {
                timerCounter++;
            }
            if (terminateCount == timerCounter && timer != null) {
                timer.cancel();
            }
            if (sleepInRun) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            synchronized (sync) {
                sync.notify();
            }
        }

        public synchronized int wasRun() {
            return wasRun;
        }

        public void sleepInRun(boolean sleepInRun) {
            this.sleepInRun = sleepInRun;
        }

        public void incrementCount(boolean incrementCount) {
            this.incrementCount = incrementCount;
        }

        public void terminateCount(int terminateCount) {
            this.terminateCount = terminateCount;
        }
    }

    private void awaitRun(TimerTestTask task) throws Exception {
        while (task.wasRun() == 0) {
            Thread.sleep(150);
        }
    }

    /**
     * java.util.Timer#Timer(boolean)
     */
    public void test_ConstructorZ() throws Exception {
        Timer t = null;
        try {
            // Ensure a task is run
            t = new Timer(true);
            TimerTestTask testTask = new TimerTestTask();
            t.schedule(testTask, 200);
            awaitRun(testTask);
            t.cancel();
        } finally {
            if (t != null)
                t.cancel();
        }

    }

    /**
     * java.util.Timer#Timer()
     */
    public void test_Constructor() throws Exception {
        Timer t = null;
        try {
            // Ensure a task is run
            t = new Timer();
            TimerTestTask testTask = new TimerTestTask();
            t.schedule(testTask, 200);
            awaitRun(testTask);
            t.cancel();
        } finally {
            if (t != null)
                t.cancel();
        }

    }

    /**
     * java.util.Timer#Timer(String, boolean)
     */
    public void test_ConstructorSZ() throws Exception {
        Timer t = null;
        try {
            // Ensure a task is run
            t = new Timer("test_ConstructorSZThread", true);
            TimerTestTask testTask = new TimerTestTask();
            t.schedule(testTask, 200);
            awaitRun(testTask);
            t.cancel();
        } finally {
            if (t != null)
                t.cancel();
        }

        try {
            new Timer(null, true);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            new Timer(null, false);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    /**
     * java.util.Timer#Timer(String)
     */
    public void test_ConstructorS() throws Exception {
        Timer t = null;
        try {
            // Ensure a task is run
            t = new Timer("test_ConstructorSThread");
            TimerTestTask testTask = new TimerTestTask();
            t.schedule(testTask, 200);
            awaitRun(testTask);
            t.cancel();
        } finally {
            if (t != null)
                t.cancel();
        }

        try {
            new Timer(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    /**
     * java.util.Timer#cancel()
     */
    public void test_cancel() throws Exception {
        Timer t = null;
        try {
            // Ensure a task throws an IllegalStateException after cancelled
            t = new Timer();
            TimerTestTask testTask = new TimerTestTask();
            t.cancel();
            try {
                t.schedule(testTask, 100, 200);
                fail("Scheduling a task after Timer.cancel() should throw exception");
            } catch (IllegalStateException expected) {
            }

            // Ensure a task is run but not after cancel
            t = new Timer();
            testTask = new TimerTestTask();
            t.schedule(testTask, 100, 500);
            awaitRun(testTask);
            t.cancel();
            synchronized (sync) {
                sync.wait(500);
            }
            assertEquals("TimerTask.run() method should not have been called after cancel",
                         1, testTask.wasRun());

            // Ensure you can call cancel more than once
            t = new Timer();
            testTask = new TimerTestTask();
            t.schedule(testTask, 100, 500);
            awaitRun(testTask);
            t.cancel();
            t.cancel();
            t.cancel();
            synchronized (sync) {
                sync.wait(500);
            }
            assertEquals("TimerTask.run() method should not have been called after cancel",
                         1, testTask.wasRun());

            // Ensure that a call to cancel from within a timer ensures no more
            // run
            t = new Timer();
            testTask = new TimerTestTask(t);
            testTask.incrementCount(true);
            testTask.terminateCount(5); // Terminate after 5 runs
            t.schedule(testTask, 100, 100);
            synchronized (sync) {
                sync.wait(200);
                assertEquals(1, testTask.wasRun());
                sync.wait(200);
                assertEquals(2, testTask.wasRun());
                sync.wait(200);
                assertEquals(3, testTask.wasRun());
                sync.wait(200);
                assertEquals(4, testTask.wasRun());
                sync.wait(200);
                assertEquals(5, testTask.wasRun());
                sync.wait(200);
                assertEquals(5, testTask.wasRun());
            }
            t.cancel();
            Thread.sleep(200);
        } finally {
            if (t != null)
                t.cancel();
        }

    }

    /**
     * java.util.Timer#purge()
     */
    public void test_purge() throws Exception {
        Timer t = null;
        try {
            t = new Timer();
            assertEquals(0, t.purge());

            TimerTestTask[] tasks = new TimerTestTask[100];
            int[] delayTime = { 50, 80, 20, 70, 40, 10, 90, 30, 60 };

            int j = 0;
            for (int i = 0; i < 100; i++) {
                tasks[i] = new TimerTestTask();
                t.schedule(tasks[i], delayTime[j++], 200);
                if (j == 9) {
                    j = 0;
                }
            }

            for (int i = 0; i < 50; i++) {
                tasks[i].cancel();
            }

            assertTrue(t.purge() <= 50);
            assertEquals(0, t.purge());
        } finally {
            if (t != null) {
                t.cancel();
            }
        }
    }

    /**
     * java.util.Timer#schedule(java.util.TimerTask, java.util.Date)
     */
    public void test_scheduleLjava_util_TimerTaskLjava_util_Date() throws Exception {
        Timer t = null;
        try {
            // Ensure a Timer throws an IllegalStateException after cancelled
            t = new Timer();
            TimerTestTask testTask = new TimerTestTask();
            Date d = new Date(System.currentTimeMillis() + 100);
            t.cancel();
            try {
                t.schedule(testTask, d);
                fail("Scheduling a task after Timer.cancel() should throw exception");
            } catch (IllegalStateException expected) {
            }

            // Ensure a Timer throws an IllegalStateException if task already
            // cancelled
            t = new Timer();
            testTask = new TimerTestTask();
            d = new Date(System.currentTimeMillis() + 100);
            testTask.cancel();
            try {
                t.schedule(testTask, d);
                fail("Scheduling a task after cancelling it should throw exception");
            } catch (IllegalStateException expected) {
            }
            t.cancel();

            // Ensure a Timer throws an IllegalArgumentException if delay is
            // negative
            t = new Timer();
            testTask = new TimerTestTask();
            d = new Date(-100);
            try {
                t.schedule(testTask, d);
                fail("Scheduling a task with negative date should throw IllegalArgumentException");
            } catch (IllegalArgumentException expected) {
            }
            t.cancel();

            // Ensure a Timer throws a NullPointerException if the task is null
            t = new Timer();
            d = new Date(System.currentTimeMillis() + 100);
            try {
                t.schedule(null, d);
                fail("Scheduling a null task should throw NullPointerException");
            } catch (NullPointerException expected) {
            }
            t.cancel();

            // Ensure a Timer throws a NullPointerException if the date is null
            t = new Timer();
            testTask = new TimerTestTask();
            try {
                t.schedule(testTask, null);
                fail("Scheduling a null date should throw NullPointerException");
            } catch (NullPointerException expected) {
            }
            t.cancel();

            // Ensure proper sequence of exceptions
            t = new Timer();
            d = new Date(-100);
            try {
                t.schedule(null, d);
                fail("Scheduling a null task with negative date should throw IllegalArgumentException first");
            } catch (IllegalArgumentException expected) {
            }
            t.cancel();

            // Ensure a task is run
            t = new Timer();
            testTask = new TimerTestTask();
            d = new Date(System.currentTimeMillis() + 200);
            t.schedule(testTask, d);
            awaitRun(testTask);
            t.cancel();

            // Ensure multiple tasks are run
            t = new Timer();
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            d = new Date(System.currentTimeMillis() + 100);
            t.schedule(testTask, d);
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            d = new Date(System.currentTimeMillis() + 150);
            t.schedule(testTask, d);
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            d = new Date(System.currentTimeMillis() + 70);
            t.schedule(testTask, d);
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            d = new Date(System.currentTimeMillis() + 10);
            t.schedule(testTask, d);
            Thread.sleep(400);
            assertTrue("Multiple tasks should have incremented counter 4 times not "
                       + timerCounter, timerCounter == 4);
            t.cancel();
        } finally {
            if (t != null)
                t.cancel();
        }
    }

    /**
     * java.util.Timer#schedule(java.util.TimerTask, long)
     */
    public void test_scheduleLjava_util_TimerTaskJ() throws Exception {
        Timer t = null;
        try {
            // Ensure a Timer throws an IllegalStateException after cancelled
            t = new Timer();
            TimerTestTask testTask = new TimerTestTask();
            t.cancel();
            try {
                t.schedule(testTask, 100);
                fail("Scheduling a task after Timer.cancel() should throw exception");
            } catch (IllegalStateException expected) {
            }

            // Ensure a Timer throws an IllegalStateException if task already
            // cancelled
            t = new Timer();
            testTask = new TimerTestTask();
            testTask.cancel();
            try {
                t.schedule(testTask, 100);
                fail("Scheduling a task after cancelling it should throw exception");
            } catch (IllegalStateException expected) {
            }
            t.cancel();

            // Ensure a Timer throws an IllegalArgumentException if delay is
            // negative
            t = new Timer();
            testTask = new TimerTestTask();
            try {
                t.schedule(testTask, -100);
                fail("Scheduling a task with negative delay should throw IllegalArgumentException");
            } catch (IllegalArgumentException expected) {
            }
            t.cancel();

            // Ensure a Timer throws a NullPointerException if the task is null
            t = new Timer();
            try {
                t.schedule(null, 10);
                fail("Scheduling a null task should throw NullPointerException");
            } catch (NullPointerException expected) {
            }
            t.cancel();

            // Ensure proper sequence of exceptions
            t = new Timer();
            try {
                t.schedule(null, -10);
                fail("Scheduling a null task with negative delays should throw IllegalArgumentException first");
            } catch (IllegalArgumentException expected) {
            }
            t.cancel();

            // Ensure a task is run
            t = new Timer();
            testTask = new TimerTestTask();
            t.schedule(testTask, 200);
            awaitRun(testTask);
            t.cancel();

            // Ensure multiple tasks are run
            t = new Timer();
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            t.schedule(testTask, 100);
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            t.schedule(testTask, 150);
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            t.schedule(testTask, 70);
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            t.schedule(testTask, 10);
            Thread.sleep(400);
            assertTrue("Multiple tasks should have incremented counter 4 times not "
                       + timerCounter, timerCounter == 4);
            t.cancel();
        } finally {
            if (t != null)
                t.cancel();
        }
    }

    /**
     * java.util.Timer#schedule(java.util.TimerTask, long, long)
     */
    public void test_scheduleLjava_util_TimerTaskJJ() throws Exception {
        Timer t = null;
        try {
            // Ensure a Timer throws an IllegalStateException after cancelled
            t = new Timer();
            TimerTestTask testTask = new TimerTestTask();
            t.cancel();
            try {
                t.schedule(testTask, 100, 100);
                fail("Scheduling a task after Timer.cancel() should throw exception");
            } catch (IllegalStateException expected) {
            }

            // Ensure a Timer throws an IllegalStateException if task already
            // cancelled
            t = new Timer();
            testTask = new TimerTestTask();
            testTask.cancel();
            try {
                t.schedule(testTask, 100, 100);
                fail("Scheduling a task after cancelling it should throw exception");
            } catch (IllegalStateException expected) {
            }
            t.cancel();

            // Ensure a Timer throws an IllegalArgumentException if delay is
            // negative
            t = new Timer();
            testTask = new TimerTestTask();
            try {
                t.schedule(testTask, -100, 100);
                fail("Scheduling a task with negative delay should throw IllegalArgumentException");
            } catch (IllegalArgumentException expected) {
            }
            t.cancel();

            // Ensure a Timer throws an IllegalArgumentException if period is
            // negative
            t = new Timer();
            testTask = new TimerTestTask();
            try {
                t.schedule(testTask, 100, -100);
                fail("Scheduling a task with negative period should throw IllegalArgumentException");
            } catch (IllegalArgumentException expected) {
            }
            t.cancel();

            // Ensure a Timer throws an IllegalArgumentException if period is
            // zero
            t = new Timer();
            testTask = new TimerTestTask();
            try {
                t.schedule(testTask, 100, 0);
                fail("Scheduling a task with 0 period should throw IllegalArgumentException");
            } catch (IllegalArgumentException expected) {
            }
            t.cancel();

            // Ensure a Timer throws a NullPointerException if the task is null
            t = new Timer();
            try {
                t.schedule(null, 10, 10);
                fail("Scheduling a null task should throw NullPointerException");
            } catch (NullPointerException expected) {
            }
            t.cancel();

            // Ensure proper sequence of exceptions
            t = new Timer();
            try {
                t.schedule(null, -10, -10);
                fail("Scheduling a null task with negative delays should throw IllegalArgumentException first");
            } catch (IllegalArgumentException expected) {
            }
            t.cancel();

            // Ensure a task is run at least twice
            t = new Timer();
            testTask = new TimerTestTask();
            t.schedule(testTask, 100, 100);
            Thread.sleep(400);
            assertTrue("TimerTask.run() method should have been called at least twice ("
                       + testTask.wasRun() + ")", testTask.wasRun() >= 2);
            t.cancel();

            // Ensure multiple tasks are run
            t = new Timer();
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            t.schedule(testTask, 100, 100); // at least 9 times
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            t.schedule(testTask, 200, 100); // at least 7 times
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            t.schedule(testTask, 300, 200); // at least 4 times
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            t.schedule(testTask, 100, 200); // at least 4 times
            Thread.sleep(1200); // Allowed more room for error
            assertTrue("Multiple tasks should have incremented counter 24 times not "
                       + timerCounter, timerCounter >= 24);
            t.cancel();
        } finally {
            if (t != null)
                t.cancel();
        }
    }

    /**
     * java.util.Timer#schedule(java.util.TimerTask, java.util.Date,
     *        long)
     */
    public void test_scheduleLjava_util_TimerTaskLjava_util_DateJ() throws Exception {
        Timer t = null;
        try {
            // Ensure a Timer throws an IllegalStateException after cancelled
            t = new Timer();
            TimerTestTask testTask = new TimerTestTask();
            Date d = new Date(System.currentTimeMillis() + 100);
            t.cancel();
            try {
                t.schedule(testTask, d, 100);
                fail("Scheduling a task after Timer.cancel() should throw exception");
            } catch (IllegalStateException expected) {
            }

            // Ensure a Timer throws an IllegalStateException if task already
            // cancelled
            t = new Timer();
            d = new Date(System.currentTimeMillis() + 100);
            testTask = new TimerTestTask();
            testTask.cancel();
            try {
                t.schedule(testTask, d, 100);
                fail("Scheduling a task after cancelling it should throw exception");
            } catch (IllegalStateException expected) {
            }
            t.cancel();

            // Ensure a Timer throws an IllegalArgumentException if delay is
            // negative
            t = new Timer();
            d = new Date(-100);
            testTask = new TimerTestTask();
            try {
                t.schedule(testTask, d, 100);
                fail("Scheduling a task with negative delay should throw IllegalArgumentException");
            } catch (IllegalArgumentException expected) {
            }
            t.cancel();

            // Ensure a Timer throws an IllegalArgumentException if period is
            // negative
            t = new Timer();
            d = new Date(System.currentTimeMillis() + 100);
            testTask = new TimerTestTask();
            try {
                t.schedule(testTask, d, -100);
                fail("Scheduling a task with negative period should throw IllegalArgumentException");
            } catch (IllegalArgumentException expected) {
            }
            t.cancel();

            // Ensure a Timer throws a NullPointerException if the task is null
            t = new Timer();
            d = new Date(System.currentTimeMillis() + 100);
            try {
                t.schedule(null, d, 10);
                fail("Scheduling a null task should throw NullPointerException");
            } catch (NullPointerException expected) {
            }
            t.cancel();

            // Ensure a Timer throws a NullPointerException if the date is null
            t = new Timer();
            testTask = new TimerTestTask();
            try {
                t.schedule(testTask, null, 10);
                fail("Scheduling a null task should throw NullPointerException");
            } catch (NullPointerException expected) {
            }
            t.cancel();

            // Ensure proper sequence of exceptions
            t = new Timer();
            d = new Date(-100);
            try {
                t.schedule(null, d, 10);
                fail("Scheduling a null task with negative dates should throw IllegalArgumentException first");
            } catch (IllegalArgumentException expected) {
            }
            t.cancel();

            // Ensure a task is run at least twice
            t = new Timer();
            d = new Date(System.currentTimeMillis() + 100);
            testTask = new TimerTestTask();
            t.schedule(testTask, d, 100);
            Thread.sleep(800);
            assertTrue("TimerTask.run() method should have been called at least twice ("
                       + testTask.wasRun() + ")", testTask.wasRun() >= 2);
            t.cancel();

            // Ensure multiple tasks are run
            t = new Timer();
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            d = new Date(System.currentTimeMillis() + 100);
            t.schedule(testTask, d, 100); // at least 9 times
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            d = new Date(System.currentTimeMillis() + 200);
            t.schedule(testTask, d, 100); // at least 7 times
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            d = new Date(System.currentTimeMillis() + 300);
            t.schedule(testTask, d, 200); // at least 4 times
            testTask = new TimerTestTask();
            testTask.incrementCount(true);
            d = new Date(System.currentTimeMillis() + 100);
            t.schedule(testTask, d, 200); // at least 4 times
            Thread.sleep(3000);
            assertTrue("Multiple tasks should have incremented counter 24 times not "
                       + timerCounter, timerCounter >= 24);
            t.cancel();
        } finally {
            if (t != null)
                t.cancel();
        }
    }

    /**
     * java.util.Timer#scheduleAtFixedRate(java.util.TimerTask, long,
     *        long)
     */
    public void test_scheduleAtFixedRateLjava_util_TimerTaskJJ() throws Exception {
        Timer t = null;
        try {
            // Ensure a Timer throws an IllegalStateException after cancelled
            t = new Timer();
            TimerTestTask testTask = new TimerTestTask();
            t.cancel();
            try {
                t.scheduleAtFixedRate(testTask, 100, 100);
                fail("scheduleAtFixedRate after Timer.cancel() should throw exception");
            } catch (IllegalStateException expected) {
            }

            // Ensure a Timer throws an IllegalArgumentException if delay is
            // negative
            t = new Timer();
            testTask = new TimerTestTask();
            try {
                t.scheduleAtFixedRate(testTask, -100, 100);
                fail("scheduleAtFixedRate with negative delay should throw IllegalArgumentException");
            } catch (IllegalArgumentException expected) {
            }
            t.cancel();

            // Ensure a Timer throws an IllegalArgumentException if period is
            // negative
            t = new Timer();
            testTask = new TimerTestTask();
            try {
                t.scheduleAtFixedRate(testTask, 100, -100);
                fail("scheduleAtFixedRate with negative period should throw IllegalArgumentException");
            } catch (IllegalArgumentException expected) {
            }
            t.cancel();

            // Ensure a task is run at least twice
            t = new Timer();
            testTask = new TimerTestTask();
            t.scheduleAtFixedRate(testTask, 100, 100);
            Thread.sleep(400);
            assertTrue("TimerTask.run() method should have been called at least twice ("
                       + testTask.wasRun() + ")", testTask.wasRun() >= 2);
            t.cancel();

            class SlowThenFastTask extends TimerTask {
                int wasRun = 0;

                long startedAt;

                long lastDelta;

                public void run() {
                    if (wasRun == 0)
                        startedAt = System.currentTimeMillis();
                    lastDelta = System.currentTimeMillis()
                            - (startedAt + (100 * wasRun));
                    wasRun++;
                    if (wasRun == 2) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                public long lastDelta() {
                    return lastDelta;
                }

                public int wasRun() {
                    return wasRun;
                }
            }

            // Ensure multiple tasks are run
            t = new Timer();
            SlowThenFastTask slowThenFastTask = new SlowThenFastTask();

            // at least 9 times even when asleep
            t.scheduleAtFixedRate(slowThenFastTask, 100, 100);
            Thread.sleep(1000);
            long lastDelta = slowThenFastTask.lastDelta();
            assertTrue("Fixed Rate Schedule should catch up, but is off by "
                       + lastDelta + " ms", slowThenFastTask.lastDelta < 300);
            t.cancel();
        } finally {
            if (t != null)
                t.cancel();
        }
    }

    /**
     * java.util.Timer#scheduleAtFixedRate(java.util.TimerTask,
     *        java.util.Date, long)
     */
    public void test_scheduleAtFixedRateLjava_util_TimerTaskLjava_util_DateJ() throws Exception {
        Timer t = null;
        try {
            // Ensure a Timer throws an IllegalStateException after cancelled
            t = new Timer();
            TimerTestTask testTask = new TimerTestTask();
            t.cancel();
            Date d = new Date(System.currentTimeMillis() + 100);
            try {
                t.scheduleAtFixedRate(testTask, d, 100);
                fail("scheduleAtFixedRate after Timer.cancel() should throw exception");
            } catch (IllegalStateException expected) {
            }

            // Ensure a Timer throws an IllegalArgumentException if delay is
            // negative
            t = new Timer();
            testTask = new TimerTestTask();
            d = new Date(-100);
            try {
                t.scheduleAtFixedRate(testTask, d, 100);
                fail("scheduleAtFixedRate with negative Date should throw IllegalArgumentException");
            } catch (IllegalArgumentException expected) {
            }
            t.cancel();

            // Ensure a Timer throws an IllegalArgumentException if period is
            // negative
            t = new Timer();
            testTask = new TimerTestTask();
            try {
                t.scheduleAtFixedRate(testTask, d, -100);
                fail("scheduleAtFixedRate with negative period should throw IllegalArgumentException");
            } catch (IllegalArgumentException expected) {
            }
            t.cancel();

            // Ensure a Timer throws an NullPointerException if date is Null
            t = new Timer();
            testTask = new TimerTestTask();
            try {
                t.scheduleAtFixedRate(testTask, null, 100);
                fail("scheduleAtFixedRate with null date should throw NullPointerException");
            } catch (NullPointerException expected) {
            }
            t.cancel();

            // Ensure proper sequence of exceptions
            t = new Timer();
            d = new Date(-100);
            try {
                t.scheduleAtFixedRate(null, d, 10);
                fail("Scheduling a null task with negative date should throw IllegalArgumentException first");
            } catch (IllegalArgumentException expected) {
            }
            t.cancel();

            // Ensure proper sequence of exceptions
            t = new Timer();
            try {
                t.scheduleAtFixedRate(null, null, -10);
                fail("Scheduling a null task & null date & negative period should throw IllegalArgumentException first");
            } catch (IllegalArgumentException expected) {
            }
            t.cancel();

            // Ensure a task is run at least twice
            t = new Timer();
            testTask = new TimerTestTask();
            d = new Date(System.currentTimeMillis() + 100);
            t.scheduleAtFixedRate(testTask, d, 100);
            Thread.sleep(400);
            assertTrue("TimerTask.run() method should have been called at least twice ("
                       + testTask.wasRun() + ")", testTask.wasRun() >= 2);
            t.cancel();

            class SlowThenFastTask extends TimerTask {
                int wasRun = 0;

                long startedAt;

                long lastDelta;

                public void run() {
                    if (wasRun == 0)
                        startedAt = System.currentTimeMillis();
                    lastDelta = System.currentTimeMillis()
                            - (startedAt + (100 * wasRun));
                    wasRun++;
                    if (wasRun == 2) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                public long lastDelta() {
                    return lastDelta;
                }

                public int wasRun() {
                    return wasRun;
                }
            }

            // Ensure multiple tasks are run
            t = new Timer();
            SlowThenFastTask slowThenFastTask = new SlowThenFastTask();
            d = new Date(System.currentTimeMillis() + 100);

            // at least 9 times even when asleep
            t.scheduleAtFixedRate(slowThenFastTask, d, 100);
            Thread.sleep(1000);
            long lastDelta = slowThenFastTask.lastDelta();
            assertTrue("Fixed Rate Schedule should catch up, but is off by "
                       + lastDelta + " ms", lastDelta < 300);
            t.cancel();
        } finally {
            if (t != null)
                t.cancel();
        }
    }

    /**
     * We used to swallow RuntimeExceptions thrown by tasks. Instead, we need to
     * let those exceptions bubble up, where they will both notify the thread's
     * uncaught exception handler and terminate the timer's thread.
     *
     TODO(tball): enable when Thread.setUncaughtExceptionHandler is supported.
    public void testThrowingTaskKillsTimerThread() throws Exception {
        final AtomicReference<Thread> threadRef = new AtomicReference<Thread>();
        new Timer().schedule(new TimerTask() {
            @Override public void run() {
                Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                    public void uncaughtException(Thread thread, Throwable ex) {}
                });
                threadRef.set(Thread.currentThread());
                throw new RuntimeException("task failure!");
            }
        }, 1);

        Thread.sleep(400);
        Thread timerThread = threadRef.get();
        assertFalse(timerThread.isAlive());
    }

    protected void setUp() {
        timerCounter = 0;
    }

    protected void tearDown() {
    }
    */
}
