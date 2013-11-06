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

package org.apache.harmony.luni.tests.java.lang;

import java.lang.Thread.UncaughtExceptionHandler;
import java.security.Permission;
import java.util.Map;

public class ThreadTest extends junit.framework.TestCase {

	static class SimpleThread implements Runnable {
		int delay;

		public void run() {
			try {
				synchronized (this) {
					this.notify();
					this.wait(delay);
				}
			} catch (InterruptedException e) {
				return;
			}

		}

		public SimpleThread(int d) {
			if (d >= 0)
				delay = d;
		}
	}

	static class YieldThread implements Runnable {
		volatile int delay;

		public void run() {
			int x = 0;
			while (true) {
				++x;
			}
		}

		public YieldThread(int d) {
			if (d >= 0)
				delay = d;
		}
	}

	static class ResSupThread implements Runnable {
		Thread parent;

		volatile int checkVal = -1;

		public void run() {
			try {
				synchronized (this) {
					this.notify();
				}
				while (true) {
					checkVal++;
					zz();
					Thread.sleep(100);
				}
			} catch (InterruptedException e) {
				return;
			} catch (BogusException e) {
				try {
					// Give parent a chance to sleep
					Thread.sleep(500);
				} catch (InterruptedException x) {
				}
				parent.interrupt();
				while (!Thread.currentThread().isInterrupted()) {
					// Don't hog the CPU
					try {
						Thread.sleep(50);
					} catch (InterruptedException x) {
						// This is what we've been waiting for...don't throw it
						// away!
						break;
					}
				}
			}
		}

		public void zz() throws BogusException {
		}

		public ResSupThread(Thread t) {
			parent = t;
		}

		public synchronized int getCheckVal() {
			return checkVal;
		}
	}

	static class BogusException extends Throwable {

        private static final long serialVersionUID = 1L;

        public BogusException(String s) {
			super(s);
		}
	}

	Thread st, ct, spinner;

	static boolean calledMySecurityManager = false;

	/**
	 * @tests java.lang.Thread#Thread(java.lang.Runnable)
	 */
	public void test_ConstructorLjava_lang_Runnable() {
            // Test for method java.lang.Thread(java.lang.Runnable)
            ct = new Thread(new SimpleThread(10));
            ct.start();
	}

	/**
	 * @tests java.lang.Thread#Thread(java.lang.Runnable, java.lang.String)
	 */
	public void test_ConstructorLjava_lang_RunnableLjava_lang_String() {
		// Test for method java.lang.Thread(java.lang.Runnable,
		// java.lang.String)
		Thread st1 = new Thread(new SimpleThread(1), "SimpleThread1");
		assertEquals("Constructed thread with incorrect thread name", "SimpleThread1", st1
				.getName());
		st1.start();
	}

	/**
	 * @tests java.lang.Thread#Thread(java.lang.String)
	 */
	public void test_ConstructorLjava_lang_String() {
		// Test for method java.lang.Thread(java.lang.String)
		Thread t = new Thread("Testing");
		assertEquals("Created tread with incorrect name",
				"Testing", t.getName());
		t.start();
	}

	/**
	 * @tests java.lang.Thread#Thread(java.lang.ThreadGroup, java.lang.Runnable)
	 */
	public void test_ConstructorLjava_lang_ThreadGroupLjava_lang_Runnable() {
		// Test for method java.lang.Thread(java.lang.ThreadGroup,
		// java.lang.Runnable)
		ThreadGroup tg = new ThreadGroup("Test Group1");
		st = new Thread(tg, new SimpleThread(1), "SimpleThread2");
		assertTrue("Returned incorrect thread group", st.getThreadGroup() == tg);
		st.start();
		try {
			st.join();
		} catch (InterruptedException e) {
		}
		tg.destroy();
	}

	/**
	 * @tests java.lang.Thread#Thread(java.lang.ThreadGroup, java.lang.Runnable,
	 *        java.lang.String)
	 */
	public void test_ConstructorLjava_lang_ThreadGroupLjava_lang_RunnableLjava_lang_String() {
		// Test for method java.lang.Thread(java.lang.ThreadGroup,
		// java.lang.Runnable, java.lang.String)
		ThreadGroup tg = new ThreadGroup("Test Group2");
		st = new Thread(tg, new SimpleThread(1), "SimpleThread3");
		assertTrue("Constructed incorrect thread", (st.getThreadGroup() == tg)
				&& st.getName().equals("SimpleThread3"));
		st.start();
		try {
			st.join();
		} catch (InterruptedException e) {
		}
		tg.destroy();

		Runnable r = new Runnable() {
			public void run() {
			}
		};

		ThreadGroup foo = null;
		try {
			new Thread(foo = new ThreadGroup("foo"), r, null);
			// Should not get here
			fail("Null cannot be accepted as Thread name");
		} catch (NullPointerException npe) {
			assertTrue("Null cannot be accepted as Thread name", true);
			foo.destroy();
		}

	}

	/**
	 * @tests java.lang.Thread#Thread(java.lang.ThreadGroup, java.lang.String)
	 */
	public void test_ConstructorLjava_lang_ThreadGroupLjava_lang_String() {
		// Test for method java.lang.Thread(java.lang.ThreadGroup,
		// java.lang.String)
		st = new Thread(new SimpleThread(1), "SimpleThread4");
		assertEquals("Returned incorrect thread name",
				"SimpleThread4", st.getName());
		st.start();
	}

	/**
	 * @tests java.lang.Thread#activeCount()
	 */
	public void test_activeCount() {
		// Test for method int java.lang.Thread.activeCount()
		Thread t = new Thread(new SimpleThread(10));
		int active = 0;
		synchronized (t) {
			t.start();
			active = Thread.activeCount();
		}
		assertTrue("Incorrect activeCount for current group: " + active, active > 1);
		try {
			t.join();
		} catch (InterruptedException e) {
		}
	}

	/**
	 * @tests java.lang.Thread#checkAccess()
	 */
	public void test_checkAccess() {
		// Test for method void java.lang.Thread.checkAccess()
		ThreadGroup tg = new ThreadGroup("Test Group3");
		try {
			st = new Thread(tg, new SimpleThread(1), "SimpleThread5");
			st.checkAccess();
			assertTrue("CheckAccess passed", true);
		} catch (SecurityException e) {
			fail("CheckAccess failed : " + e.getMessage());
		}
		st.start();
		try {
			st.join();
		} catch (InterruptedException e) {
		}
		tg.destroy();
	}

	/**
     * @tests java.lang.Thread#countStackFrames()
     */
    @SuppressWarnings("deprecation")
    public void test_countStackFrames() {
        /*
         * Thread.countStackFrames() is unpredictable, so we just test that it
         * doesn't throw an exception.
         */
        Thread.currentThread().countStackFrames();
    }

	/**
	 * @tests java.lang.Thread#currentThread()
	 */
	public void test_currentThread() {
	    assertNotNull(Thread.currentThread());
	}

	/**
	 * @tests java.lang.Thread#enumerate(java.lang.Thread[])
	 */
    public void test_enumerate$Ljava_lang_Thread() {
        // Test for method int java.lang.Thread.enumerate(java.lang.Thread [])
        // The test has been updated according to HARMONY-1974 JIRA issue.

    	class MyThread extends Thread {
            MyThread(ThreadGroup tg, String name) {
                super(tg, name);
            }

            boolean failed = false;
            String failMessage = null;

            public void run() {
            	SimpleThread st1 = null;
            	SimpleThread st2 = null;
            	ThreadGroup mytg = null;
            	Thread firstOne = null;
            	Thread secondOne = null;
                try {
                    int arrayLength = 10;
                    Thread[] tarray = new Thread[arrayLength];
                    st1 = new SimpleThread(-1);
                    st2 = new SimpleThread(-1);
                    mytg = new ThreadGroup("jp");
                    firstOne = new Thread(mytg, st1, "firstOne2");
                    secondOne = new Thread(mytg, st2, "secondOne1");
                    int count = Thread.enumerate(tarray);
                    assertEquals("Incorrect value returned1",
                            1, count);
                    synchronized (st1) {
                        firstOne.start();
                        try {
                            st1.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                    count = Thread.enumerate(tarray);
                    assertEquals("Incorrect value returned2",
                            2, count);
                    synchronized (st2) {
                        secondOne.start();
                        try {
                            st2.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                    count = Thread.enumerate(tarray);
                    assertEquals("Incorrect value returned3",
                            3, count);
                } catch (junit.framework.AssertionFailedError e) {
                    failed = true;
                    failMessage = e.getMessage();
                } finally {
                    synchronized (st1) {
                        firstOne.interrupt();
                    }
                    synchronized (st2) {
                        secondOne.interrupt();
                    }
                    try {
                        firstOne.join();
                        secondOne.join();
                    } catch (InterruptedException e) {
                    }
                    mytg.destroy();
                }
            }
        };

        ThreadGroup tg = new ThreadGroup("tg");
        MyThread t = new MyThread(tg, "top");
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            fail("Unexpected interrupt");
        } finally {
            tg.destroy();
        }
        assertFalse(t.failMessage, t.failed);
    }

	/**
	 * @tests java.lang.Thread#getContextClassLoader()
	 */
	public void test_getContextClassLoader() {
		// Test for method java.lang.ClassLoader
		// java.lang.Thread.getContextClassLoader()
		Thread t = new Thread();
		assertTrue("Incorrect class loader returned",
				t.getContextClassLoader() == Thread.currentThread()
						.getContextClassLoader());
		t.start();

	}

	/**
	 * @tests java.lang.Thread#getName()
	 */
	public void test_getName() {
		// Test for method java.lang.String java.lang.Thread.getName()
		st = new Thread(new SimpleThread(1), "SimpleThread6");
		assertEquals("Returned incorrect thread name",
				"SimpleThread6", st.getName());
		st.start();
	}

	/**
	 * @tests java.lang.Thread#getPriority()
	 */
	public void test_getPriority() {
		// Test for method int java.lang.Thread.getPriority()
		st = new Thread(new SimpleThread(1));
		st.setPriority(Thread.MAX_PRIORITY);
		assertTrue("Returned incorrect thread priority",
				st.getPriority() == Thread.MAX_PRIORITY);
		st.start();
	}

	/**
	 * @tests java.lang.Thread#getThreadGroup()
	 */
	public void test_getThreadGroup() {
		// Test for method java.lang.ThreadGroup
		// java.lang.Thread.getThreadGroup()
		ThreadGroup tg = new ThreadGroup("Test Group4");
		st = new Thread(tg, new SimpleThread(1), "SimpleThread8");
		assertTrue("Returned incorrect thread group", st.getThreadGroup() == tg);
		st.start();
		try {
			st.join();
		} catch (InterruptedException e) {
		}
		assertNull("group should be null", st.getThreadGroup());
		assertNotNull("toString() should not be null", st.toString());
		tg.destroy();

		final Object lock = new Object();
		Thread t = new Thread() {
			@Override
            public void run() {
				synchronized (lock) {
					lock.notifyAll();
				}
			}
		};
		synchronized (lock) {
			t.start();
			try {
				lock.wait();
			} catch (InterruptedException e) {
			}
		}
		int running = 0;
		while (t.isAlive())
			running++;
		ThreadGroup group = t.getThreadGroup();
		assertNull("ThreadGroup is not null", group);
	}

	/**
	 * @tests java.lang.Thread#interrupt()
	 */
	public void test_interrupt() {
		// Test for method void java.lang.Thread.interrupt()
		final Object lock = new Object();
		class ChildThread1 extends Thread {
			Thread parent;

			boolean sync;

			@Override
            public void run() {
				if (sync) {
					synchronized (lock) {
						lock.notify();
						try {
							lock.wait();
						} catch (InterruptedException e) {
						}
					}
				}
				parent.interrupt();
			}

			public ChildThread1(Thread p, String name, boolean sync) {
				super(name);
				parent = p;
				this.sync = sync;
			}
		}
		boolean interrupted = false;
		try {
			ct = new ChildThread1(Thread.currentThread(), "Interrupt Test1",
					false);
			synchronized (lock) {
				ct.start();
				lock.wait();
			}
		} catch (InterruptedException e) {
			interrupted = true;
		}
		assertTrue("Failed to Interrupt thread1", interrupted);

		interrupted = false;
		try {
			ct = new ChildThread1(Thread.currentThread(), "Interrupt Test2",
					true);
			synchronized (lock) {
				ct.start();
				lock.wait();
				lock.notify();
			}
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			interrupted = true;
		}
		assertTrue("Failed to Interrupt thread2", interrupted);

	}

	/**
	 * @tests java.lang.Thread#interrupted()
	 */
	public void test_interrupted() {
        assertFalse("Interrupted returned true for non-interrupted thread", Thread
                .interrupted());
        Thread.currentThread().interrupt();
        assertTrue("Interrupted returned true for non-interrupted thread", Thread.interrupted());
        assertFalse("Failed to clear interrupted flag", Thread.interrupted());
    }

	/**
	 * @tests java.lang.Thread#isAlive()
	 */
	public void test_isAlive() {
		// Test for method boolean java.lang.Thread.isAlive()
		SimpleThread simple;
		st = new Thread(simple = new SimpleThread(500));
		assertFalse("A thread that wasn't started is alive.", st.isAlive());
		synchronized (simple) {
			st.start();
			try {
				simple.wait();
			} catch (InterruptedException e) {
			}
		}
		assertTrue("Started thread returned false", st.isAlive());
		try {
			st.join();
		} catch (InterruptedException e) {
			fail("Thread did not die");
		}
		assertTrue("Stopped thread returned true", !st.isAlive());
	}

	/**
	 * @tests java.lang.Thread#isDaemon()
	 */
	public void test_isDaemon() {
		// Test for method boolean java.lang.Thread.isDaemon()
		st = new Thread(new SimpleThread(1), "SimpleThread10");
		assertTrue("Non-Daemon thread returned true", !st.isDaemon());
		st.setDaemon(true);
		assertTrue("Daemon thread returned false", st.isDaemon());
		st.start();
	}

	/**
	 * @tests java.lang.Thread#isInterrupted()
	 */
	public void test_isInterrupted() {
		// Test for method boolean java.lang.Thread.isInterrupted()
		class SpinThread implements Runnable {
			public volatile boolean done = false;

			public void run() {
				while (!Thread.currentThread().isInterrupted())
					;
				while (!done)
					;
			}
		}

		SpinThread spin = new SpinThread();
		spinner = new Thread(spin);
		spinner.start();
		Thread.yield();
		try {
			assertTrue("Non-Interrupted thread returned true", !spinner
					.isInterrupted());
			spinner.interrupt();
			assertTrue("Interrupted thread returned false", spinner
					.isInterrupted());
			spin.done = true;
		} finally {
			spinner.interrupt();
			spin.done = true;
		}
	}

	/**
	 * @tests java.lang.Thread#join()
	 */
	public void test_join() {
		// Test for method void java.lang.Thread.join()
		SimpleThread simple;
		try {
			st = new Thread(simple = new SimpleThread(100));
			// cause isAlive() to be compiled by the JIT, as it must be called
			// within 100ms below.
			assertTrue("Thread is alive", !st.isAlive());
			synchronized (simple) {
				st.start();
				simple.wait();
			}
			st.join();
		} catch (InterruptedException e) {
			fail("Join failed ");
		}
		assertTrue("Joined thread is still alive", !st.isAlive());
		boolean result = true;
		Thread th = new Thread("test");
		try {
			th.join();
		} catch (InterruptedException e) {
			result = false;
		}
		assertTrue("Hung joining a non-started thread", result);
		th.start();
	}

	/**
	 * @tests java.lang.Thread#join(long)
	 */
	public void test_joinJ() {
		// Test for method void java.lang.Thread.join(long)
		SimpleThread simple;
		try {
			st = new Thread(simple = new SimpleThread(1000), "SimpleThread12");
			// cause isAlive() to be compiled by the JIT, as it must be called
			// within 100ms below.
			assertTrue("Thread is alive", !st.isAlive());
			synchronized (simple) {
				st.start();
				simple.wait();
			}
			st.join(10);
		} catch (InterruptedException e) {
			fail("Join failed ");
		}
		assertTrue("Join failed to timeout", st.isAlive());

		st.interrupt();
		try {
			st = new Thread(simple = new SimpleThread(100), "SimpleThread13");
			synchronized (simple) {
				st.start();
				simple.wait();
			}
			st.join(1000);
		} catch (InterruptedException e) {
			fail("Join failed : " + e.getMessage());
			return;
		}
		assertTrue("Joined thread is still alive", !st.isAlive());

		final Object lock = new Object();
		final Thread main = Thread.currentThread();
		Thread killer = new Thread(new Runnable() {
			public void run() {
				try {
					synchronized (lock) {
						lock.notify();
					}
					Thread.sleep(100);
				} catch (InterruptedException e) {
					return;
				}
				main.interrupt();
			}
		});
		boolean result = true;
		Thread th = new Thread("test");
		try {
			synchronized (lock) {
				killer.start();
				lock.wait();
			}
			th.join(200);
		} catch (InterruptedException e) {
			result = false;
		}
		killer.interrupt();
		assertTrue("Hung joining a non-started thread", result);
		th.start();
	}

	/**
	 * @tests java.lang.Thread#join(long, int)
	 */
	public void test_joinJI() throws Exception {
		// Test for method void java.lang.Thread.join(long, int)
		SimpleThread simple;
                st = new Thread(simple = new SimpleThread(1000), "Squawk1");
                assertTrue("Thread is alive", !st.isAlive());
                synchronized (simple) {
                        st.start();
                        simple.wait();
                }

                long firstRead = System.currentTimeMillis();
                st.join(100, 999999);
                long secondRead = System.currentTimeMillis();
                assertTrue("Did not join by appropriate time: " + secondRead + "-"
                                + firstRead + "=" + (secondRead - firstRead), secondRead
                                - firstRead <= 300);
                assertTrue("Joined thread is not alive", st.isAlive());
                st.interrupt();

		final Object lock = new Object();
		final Thread main = Thread.currentThread();
		Thread killer = new Thread(new Runnable() {
			public void run() {
				try {
					synchronized (lock) {
						lock.notify();
					}
					Thread.sleep(100);
				} catch (InterruptedException e) {
					return;
				}
				main.interrupt();
			}
		});
		boolean result = true;
		Thread th = new Thread("test");
		try {
			synchronized (lock) {
				killer.start();
				lock.wait();
			}
			th.join(200, 20);
		} catch (InterruptedException e) {
			result = false;
		}
		killer.interrupt();
		assertTrue("Hung joining a non-started thread", result);
		th.start();
	}

	/**
	 * @tests java.lang.Thread#run()
	 */
	public void test_run() {
		// Test for method void java.lang.Thread.run()
		class RunThread implements Runnable {
			boolean didThreadRun = false;

			public void run() {
				didThreadRun = true;
			}
		}
		RunThread rt = new RunThread();
		Thread t = new Thread(rt);
		try {
			t.start();
			int count = 0;
			while (!rt.didThreadRun && count < 20) {
				Thread.sleep(100);
				count++;
			}
			assertTrue("Thread did not run", rt.didThreadRun);
			t.join();
		} catch (InterruptedException e) {
			assertTrue("Joined thread was interrupted", true);
		}
		assertTrue("Joined thread is still alive", !t.isAlive());
	}

	/**
	 * @tests java.lang.Thread#setDaemon(boolean)
	 */
	public void test_setDaemonZ() {
		// Test for method void java.lang.Thread.setDaemon(boolean)
		st = new Thread(new SimpleThread(1), "SimpleThread14");
		st.setDaemon(true);
		assertTrue("Failed to set thread as daemon thread", st.isDaemon());
		st.start();
	}

	/**
	 * @tests java.lang.Thread#setName(java.lang.String)
	 */
	public void test_setNameLjava_lang_String() {
		// Test for method void java.lang.Thread.setName(java.lang.String)
		st = new Thread(new SimpleThread(1), "SimpleThread15");
		st.setName("Bogus Name");
		assertEquals("Failed to set thread name",
				"Bogus Name", st.getName());
		try {
			st.setName(null);
			fail("Null should not be accepted as a valid name");
		} catch (NullPointerException e) {
			// success
			assertTrue("Null should not be accepted as a valid name", true);
		}
		st.start();
	}

	/**
	 * @tests java.lang.Thread#setPriority(int)
	 */
	public void test_setPriorityI() {
		// Test for method void java.lang.Thread.setPriority(int)
		st = new Thread(new SimpleThread(1));
		st.setPriority(Thread.MAX_PRIORITY);
		assertTrue("Failed to set priority",
				st.getPriority() == Thread.MAX_PRIORITY);
		st.start();
	}

	/**
	 * @tests java.lang.Thread#sleep(long)
	 */
	public void test_sleepJ() {
		// Test for method void java.lang.Thread.sleep(long)

		// TODO : Test needs enhancing.
		long stime = 0, ftime = 0;
		try {
			stime = System.currentTimeMillis();
			Thread.sleep(1000);
			ftime = System.currentTimeMillis();
		} catch (InterruptedException e) {
			fail("Unexpected interrupt received");
		}
		assertTrue("Failed to sleep long enough", (ftime - stime) >= 800);
	}

	/**
	 * @tests java.lang.Thread#sleep(long, int)
	 */
	public void test_sleepJI() {
		// Test for method void java.lang.Thread.sleep(long, int)

		// TODO : Test needs revisiting.
		long stime = 0, ftime = 0;
		try {
			stime = System.currentTimeMillis();
			Thread.sleep(1000, 999999);
			ftime = System.currentTimeMillis();
		} catch (InterruptedException e) {
			fail("Unexpected interrupt received");
		}
		long result = ftime - stime;
		assertTrue("Failed to sleep long enough: " + result, result >= 900
				&& result <= 1100);
	}

	/**
	 * @tests java.lang.Thread#start()
	 */
	public void test_start() {
		// Test for method void java.lang.Thread.start()
		try {
			ResSupThread t = new ResSupThread(Thread.currentThread());
			synchronized (t) {
				ct = new Thread(t, "Interrupt Test4");
				ct.start();
				t.wait();
			}
			assertTrue("Thread is not running1", ct.isAlive());
			// Let the child thread get going.
			int orgval = t.getCheckVal();
			Thread.sleep(150);
			assertTrue("Thread is not running2", orgval != t.getCheckVal());
			ct.interrupt();
		} catch (InterruptedException e) {
			fail("Unexpected interrupt occurred");
		}
	}

	/**
	 * @tests java.lang.Thread#toString()
	 */
	public void test_toString() {
		// Test for method java.lang.String java.lang.Thread.toString()
		ThreadGroup tg = new ThreadGroup("Test Group5");
		st = new Thread(tg, new SimpleThread(1), "SimpleThread17");
		final String stString = st.toString();
		final String expected = "Thread[SimpleThread17,5,Test Group5]";
		assertTrue("Returned incorrect string: " + stString + "\t(expecting :"
				+ expected + ")", stString.equals(expected));
		st.start();
		try {
			st.join();
		} catch (InterruptedException e) {
		}
		tg.destroy();
	}


    /**
     * @tests java.lang.Thread#getDefaultUncaughtExceptionHandler
     * @tests java.lang.Thread#setDefaultUncaughtExceptionHandler
     */
    public void test_get_setDefaultUncaughtExceptionHandler() {
        class Handler implements UncaughtExceptionHandler {
            public void uncaughtException(Thread thread, Throwable ex) {
            }
        }

        final Handler handler = new Handler();
        Thread.setDefaultUncaughtExceptionHandler(handler);
        assertSame(handler, Thread.getDefaultUncaughtExceptionHandler());

        Thread.setDefaultUncaughtExceptionHandler(null);
        assertNull(Thread.getDefaultUncaughtExceptionHandler());
        //TODO add security-based tests
    }

    /**
     * @tests java.lang.Thread#getStackTrace()
     */
    public void test_getStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        assertNotNull(stackTrace);

        boolean found = false;
        for (int i = 0; i < stackTrace.length; i++) {
            StackTraceElement e = stackTrace[i];
            if (getClass().getName().equals(e.getClassName())) {
                if ("test_getStackTrace".equals(e.getMethodName())) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            fail("class and method not found in stack trace");
        }

        //TODO add security-based tests
    }

    /**
     * @tests java.lang.Thread#getState()
     */
    public void test_getState() {
        Thread.State state = Thread.currentThread().getState();
        assertNotNull(state);
        assertEquals(Thread.State.RUNNABLE, state);
        //TODO add additional state tests
    }

    /**
     * @tests java.lang.Thread#getUncaughtExceptionHandler
     * @tests java.lang.Thread#setUncaughtExceptionHandler
     */
    public void test_get_setUncaughtExceptionHandler() {
        class Handler implements UncaughtExceptionHandler {
            public void uncaughtException(Thread thread, Throwable ex) {
            }
        }

        final Handler handler = new Handler();
        Thread.currentThread().setUncaughtExceptionHandler(handler);
        assertSame(handler, Thread.currentThread().getUncaughtExceptionHandler());

        Thread.currentThread().setUncaughtExceptionHandler(null);
    }

    /**
     * @tests java.lang.Thread#getId()
     */
    public void test_getId() {
        assertTrue("current thread's ID is not positive", Thread.currentThread().getId() > 0);
    }


	@Override
    protected void tearDown() {
		try {
			if (st != null)
				st.interrupt();
		} catch (Exception e) {
		}
		try {
			if (spinner != null)
				spinner.interrupt();
		} catch (Exception e) {
		}
		try {
			if (ct != null)
				ct.interrupt();
		} catch (Exception e) {
		}

		try {
			spinner = null;
			st = null;
			ct = null;
		} catch (Exception e) {
		}
	}
}
