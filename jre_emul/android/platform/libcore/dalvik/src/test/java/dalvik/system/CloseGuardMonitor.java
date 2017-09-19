/*
 * Copyright (C) 2014 The Android Open Source Project
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
package dalvik.system;

import dalvik.system.CloseGuard.Reporter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Provides support for detecting issues found by {@link CloseGuard} from within tests.
 *
 * <p>This is a best effort as it relies on both {@link CloseGuard} being enabled and being able to
 * force a GC and finalization, none of which are directly controllable by this.
 *
 * <p>This is loaded using reflection by the AbstractResourceLeakageDetectorTestCase class as that
 * class needs to run on the reference implementation which does not have this class. It implements
 * {@link Runnable} because that is simpler than trying to manage a specialized interface.
 *
 * @hide
 */
public class CloseGuardMonitor implements Runnable {
  /**
   * The {@link Reporter} instance used to receive warnings from {@link CloseGuard}.
   */
  private final Reporter closeGuardReporter;

  /**
   * The list of allocation sites that {@link CloseGuard} has reported as not being released.
   *
   * <p>Is thread safe as this will be called during finalization and so there are no guarantees
   * as to whether it will be called concurrently or not.
   */
  private final List<Throwable> closeGuardAllocationSites = new CopyOnWriteArrayList<>();

  /**
   * Default constructor required for reflection.
   */
  public CloseGuardMonitor() {
    System.logI("Creating CloseGuard monitor");

    // Save current reporter.
    closeGuardReporter = CloseGuard.getReporter();

    // Override the reporter with our own which collates the allocation sites.
    CloseGuard.setReporter(new Reporter() {
      @Override
      public void report(String message, Throwable allocationSite) {
        // Ignore message as it's always the same.
        closeGuardAllocationSites.add(allocationSite);
      }
    });
  }

  /**
   * Check to see whether any resources monitored by {@link CloseGuard} were not released before
   * they were garbage collected.
   */
  @Override
  public void run() {
    // Create a weak reference to an object so that we can detect when it is garbage collected.
    WeakReference<Object> reference = new WeakReference<>(new Object());

    try {
      // 'Force' a GC and finalize to cause CloseGuards to report warnings. Doesn't loop
      // forever as there are no guarantees that the following code does anything at all so
      // don't want a potential infinite loop.
      Runtime runtime = Runtime.getRuntime();
      for (int i = 0; i < 20; ++i) {
        runtime.gc();
        System.runFinalization();
        try {
          Thread.sleep(1);
        } catch (InterruptedException e) {
          throw new AssertionError(e);
        }

        // Check to see if the weak reference has been garbage collected.
        if (reference.get() == null) {
          System.logI("Sentry object has been freed so assuming CloseGuards have reported"
              + " any resource leakages");
          break;
        }
      }
    } finally {
      // Restore the reporter.
      CloseGuard.setReporter(closeGuardReporter);
    }

    if (!closeGuardAllocationSites.isEmpty()) {
      StringWriter writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      int i = 0;
      for (Throwable allocationSite : closeGuardAllocationSites) {
        printWriter.print(++i);
        printWriter.print(") ");
        allocationSite.printStackTrace(printWriter);
        printWriter.println("    --------------------------------");
      }
      throw new AssertionError("Potential resource leakage detected:\n" + writer);
    }
  }
}
