/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 * Other contributors include Andrew Wright, Jeffrey Hayes,
 * Pat Fisher, Mike Judd.
 */

package jsr166;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

// android-note: These tests have been moved into their own separate
// classes to work around CTS issues.
public class LinkedTransferQueueBlockingQueueTest extends BlockingQueueTest {
    protected BlockingQueue emptyCollection() {
        return new LinkedTransferQueue();
    }
}
