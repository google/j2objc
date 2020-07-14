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

package libcore.java.security;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.DomainCombiner;
import java.security.Permission;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.concurrent.atomic.AtomicInteger;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * Android doesn't fully support access controller. This tests that actions are
 * passed through without permission enforcement.
 */
public final class AccessControllerTest extends TestCase {

    public void testDoPrivilegedWithCombiner() {
        final Permission permission = new RuntimePermission("do stuff");
        final DomainCombiner union = new DomainCombiner() {
            @Override
            public ProtectionDomain[] combine(ProtectionDomain[] a, ProtectionDomain[] b) {
                throw new AssertionFailedError("Expected combiner to be unused");
            }
        };

        ProtectionDomain protectionDomain = new ProtectionDomain(null, new Permissions());
        AccessControlContext accessControlContext = new AccessControlContext(
                new AccessControlContext(new ProtectionDomain[] { protectionDomain }), union);

        final AtomicInteger actionCount = new AtomicInteger();

        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                assertEquals(null, AccessController.getContext().getDomainCombiner());
                AccessController.getContext().checkPermission(permission);

                // Calling doPrivileged again would have exercised the combiner
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    @Override
                    public Void run() {
                        actionCount.incrementAndGet();
                        assertEquals(null, AccessController.getContext().getDomainCombiner());
                        AccessController.getContext().checkPermission(permission);
                        return null;
                    }
                });

                return null;
            }
        }, accessControlContext);

        assertEquals(1, actionCount.get());
    }
}
