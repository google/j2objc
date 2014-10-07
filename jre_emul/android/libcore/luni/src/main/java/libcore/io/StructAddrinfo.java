/*
 * Copyright (C) 2011 The Android Open Source Project
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

package libcore.io;

import java.net.InetAddress;

/**
 * Information returned/taken by getaddrinfo(3). Corresponds to C's {@code struct addrinfo} from
 * <a href="http://pubs.opengroup.org/onlinepubs/009695399/basedefs/netdb.h.html">&lt;netdb.h&gt;</a>
 *
 * TODO: we currently only _take_ a StructAddrinfo; getaddrinfo returns an InetAddress[].
 */
public final class StructAddrinfo {
    /** Flags describing the kind of lookup to be done. (Such as AI_ADDRCONFIG.) */
    public int ai_flags;

    /** Desired address family for results. (Such as AF_INET6 for IPv6. AF_UNSPEC means "any".) */
    public int ai_family;

    /** Socket type. (Such as SOCK_DGRAM. 0 means "any".) */
    public int ai_socktype;

    /** Protocol. (Such as IPPROTO_IPV6 IPv6. 0 means "any".) */
    public int ai_protocol;

    /** Address length. (Not useful in Java.) */
    // public int ai_addrlen;

    /** Address. */
    public InetAddress ai_addr;

    /** Canonical name of service location (if AI_CANONNAME provided in ai_flags). */
    // public String ai_canonname;

    /** Next element in linked list. */
    public StructAddrinfo ai_next;
}
