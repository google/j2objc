/*
 * Copyright (c) 2001, 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package sun.nio.ch;

import java.nio.channels.spi.SelectorProvider;
/* J2ObjC removed.
import java.security.AccessController;
import sun.security.action.GetPropertyAction; */

/**
 * Creates this platform's default SelectorProvider
 */

public class DefaultSelectorProvider {

    /**
     * Prevent instantiation.
     */
    private DefaultSelectorProvider() { }

    /**
     * Returns the default SelectorProvider.
     */
    public static SelectorProvider create() {
        /*
        The OpenJDK epoll based selector suffers from a serious bug where it
        can never successfully deregister keys from closed channels.

        The root cause of this bug is the sequence of operations that occur when
        a channel that's registered with a selector is closed :

        (0) Application code calls Channel.close().

        (1) The channel is "preClosed" - We dup2(2) /dev/null into the channel's
        file descriptor and the channel is marked as closed at the Java level.

        (2) All keys associated with the channel are cancelled. Cancels are
        lazy, which means that the Selectors involved won't necessarily
        deregister these keys until an ongoing call to select() (if any) returns
        or until the next call to select() on that selector.

        (3) Once all selectors associated with the channel deregister these
        cancelled keys, the channel FD is properly closed (via close(2)). Note
        that an arbitrary length of time might elapse between Step 0 and this step.
        This isn't a resource leak because the channel's FD is now a reference
        to "/dev/null".

        THE PROBLEM :
        -------------
        The default Selector implementation on Linux 2.6 and higher uses epoll(7).
        epoll can scale better than poll(2) because a lot of the state related
        to the interest set (the set of descriptors we're polling on) is
        maintained by the kernel. One of the side-effects of this design is that
        callers must call into the kernel to make changes to the interest set
        via epoll_ctl(7), for eg., by using EPOLL_CTL_ADD to add descriptors or
        EPOLL_CTL_DEL to remove descriptors from the interest set. A call to
        epoll_ctl with op = EPOLL_CTL_DEL is made when the selector attempts to
        deregister an FD associated with a channel from the interest set (see
        Step 2, above). These calls will *always fail* because the channel has
        been preClosed (see Step 1). They fail because the kernel uses its own
        internal file structure to maintain state, and rejects the command
        because the descriptor we're passing in describes a different file
        (/dev/null) that isn't selectable and isn't registered with the epoll
        instance.

        This is an issue in upstream OpenJDK as well and various select
        implementations (such as netty) have hacks to work around it. Outside
        of Android, things will work OK in most cases because the kernel has its
        own internal cleanup logic to deregister files from epoll instances
        whenever the last *non epoll* reference to the file has been closed -
        and usually this happens at the point at which the dup2(2) from Step 1
        is called. However, on Android, sockets tagged with the SocketTagger
        will never hit this code path because the socket tagging implementation
        (qtaguid) keeps a reference to the internal file until the socket
        has been untagged. In cases where sockets are closed without being
        untagged, the tagger keeps a reference to it until the process dies.

        THE SOLUTION :
        --------------
        We switch over to using poll(2) instead of epoll(7). One of the
        advantages of poll(2) is that there's less state maintained by the
        kernel. We don't need to make a syscall (analogous to epoll_ctl)
        whenever we want to remove an FD from the interest set; we merely
        remove it from the list of FDs passed in the next time we call
        through to poll. Poll is also slightly more efficient and less
        overhead to set up when the number of FDs being polled is small
        (which is the common case on Android).

        We also need to make sure that all tagged sockets are untagged before
        they're preclosed at the platform level. However, there's nothing we
        can do about applications that abuse public api (android.net.TrafficStats).


        ALTERNATE APPROACHES :
        ----------------------
        For completeness, I'm listing a couple of other approaches that were
        considered but discarded.

        - Removing preClose: This has the disadvantage of increasing the amount
        of time (Delta between Step 0 and Step 3) a channel's descriptor is
        kept alive. This also opens up races in the rare case where a
        closed FD number is reused on a different thread while we have reads
        pending.

        - A Synchronous call to EPOLL_CTL_DEL when a channel is removed: This is a
        non-starter because of the specified order of events in
        AbstractSelectableChannel; implCloseSelectableChannel must be called
        */


        // Android-changed: Always use PollSelectorProvider.
        return new sun.nio.ch.PollSelectorProvider();
    }

}
