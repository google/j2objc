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

public final class OsConstants {
    private OsConstants() { }

    public static boolean S_ISBLK(int mode) { return (mode & S_IFMT) == S_IFBLK; }
    public static boolean S_ISCHR(int mode) { return (mode & S_IFMT) == S_IFCHR; }
    public static boolean S_ISDIR(int mode) { return (mode & S_IFMT) == S_IFDIR; }
    public static boolean S_ISFIFO(int mode) { return (mode & S_IFMT) == S_IFIFO; }
    public static boolean S_ISREG(int mode) { return (mode & S_IFMT) == S_IFREG; }
    public static boolean S_ISLNK(int mode) { return (mode & S_IFMT) == S_IFLNK; }

    public static boolean S_ISSOCK(int mode) { return (mode & S_IFMT) == S_IFSOCK; }
    public static int WEXITSTATUS(int status) { return (status & 0xff00) >> 8; }
    public static boolean WCOREDUMP(int status) { return (status & 0x80) != 0; }
    public static int WTERMSIG(int status) { return status & 0x7f; }
    public static int WSTOPSIG(int status) { return WEXITSTATUS(status); }
    public static boolean WIFEXITED(int status) { return (WTERMSIG(status) == 0); }
    public static boolean WIFSTOPPED(int status) { return (WTERMSIG(status) == 0x7f); }
    public static boolean WIFSIGNALED(int status) { return (WTERMSIG(status + 1) >= 2); }

    public static final int AF_INET = 2;
    public static final int AF_INET6 = 30;
    public static final int AF_UNIX = 1;
    public static final int AF_UNSPEC = 0;
    public static final int AI_ADDRCONFIG = 0x00000400;
    public static final int AI_ALL = 0x00000100;
    public static final int AI_CANONNAME = 0x00000002;
    public static final int AI_NUMERICHOST = 0x00000004;
    public static final int AI_NUMERICSERV = 0x00001000;
    public static final int AI_PASSIVE = 0x00000001;
    public static final int AI_V4MAPPED = 0x00000800;

    public static final int EAI_AGAIN = 2;
    public static final int EAI_BADFLAGS = 3;
    public static final int EAI_FAIL = 4;
    public static final int EAI_FAMILY = 5;
    public static final int EAI_MEMORY = 6;
    public static final int EAI_NODATA = 7;
    public static final int EAI_NONAME = 8;
    public static final int EAI_OVERFLOW = 14;
    public static final int EAI_SERVICE = 9;
    public static final int EAI_SOCKTYPE = 10;
    public static final int EAI_SYSTEM = 11;
    public static final int E2BIG = 7;
    public static final int EACCES = 13;
    public static final int EADDRINUSE = 48;
    public static final int EADDRNOTAVAIL = 49;
    public static final int EAFNOSUPPORT = 47;
    public static final int EAGAIN = 35;
    public static final int EALREADY = 37;
    public static final int EBADF = 9;
    public static final int EBADMSG = 94;
    public static final int EBUSY = 16;
    public static final int ECANCELED = 89;
    public static final int ECHILD = 10;
    public static final int ECONNABORTED = 53;
    public static final int ECONNREFUSED = 61;
    public static final int ECONNRESET = 54;
    public static final int EDEADLK = 11;
    public static final int EDESTADDRREQ = 39;
    public static final int EDOM = 33;
    public static final int EDQUOT = 69;
    public static final int EEXIST = 17;
    public static final int EFAULT = 14;
    public static final int EFBIG = 27;
    public static final int EHOSTUNREACH = 65;
    public static final int EIDRM = 90;
    public static final int EILSEQ = 92;
    public static final int EINPROGRESS = 36;
    public static final int EINTR = 4;
    public static final int EINVAL = 22;
    public static final int EIO = 5;
    public static final int EISCONN = 56;
    public static final int EISDIR = 21;
    public static final int ELOOP = 62;
    public static final int EMFILE = 24;
    public static final int EMLINK = 31;
    public static final int EMSGSIZE = 40;
    public static final int EMULTIHOP = 95;
    public static final int ENAMETOOLONG = 63;
    public static final int ENETDOWN = 50;
    public static final int ENETRESET = 52;
    public static final int ENETUNREACH = 51;
    public static final int ENFILE = 23;
    public static final int ENOBUFS = 55;
    public static final int ENODATA = 96;
    public static final int ENODEV = 19;
    public static final int ENOENT = 2;
    public static final int ENOEXEC = 8;
    public static final int ENOLCK = 77;
    public static final int ENOLINK = 97;
    public static final int ENOMEM = 12;
    public static final int ENOMSG = 91;
    public static final int ENOPROTOOPT = 42;
    public static final int ENOSPC = 28;
    public static final int ENOSR = 98;
    public static final int ENOSTR = 99;
    public static final int ENOSYS = 78;
    public static final int ENOTCONN = 57;
    public static final int ENOTDIR = 20;
    public static final int ENOTEMPTY = 66;
    public static final int ENOTSOCK = 38;
    public static final int ENOTSUP = 45;
    public static final int ENOTTY = 25;
    public static final int ENXIO = 6;
    public static final int EOPNOTSUPP = 102;
    public static final int EOVERFLOW = 84;
    public static final int EPERM = 1;
    public static final int EPIPE = 32;
    public static final int EPROTO = 100;
    public static final int EPROTONOSUPPORT = 43;
    public static final int EPROTOTYPE = 41;
    public static final int ERANGE = 34;
    public static final int EROFS = 30;
    public static final int ESPIPE = 29;
    public static final int ESRCH = 3;
    public static final int ESTALE = 70;
    public static final int ETIME = 101;
    public static final int ETIMEDOUT = 60;
    public static final int ETXTBSY = 26;
    public static final int EWOULDBLOCK = EAGAIN;
    public static final int EXDEV = 18;
    public static final int EXIT_FAILURE = 1;
    public static final int EXIT_SUCCESS = 0;
    public static final int FD_CLOEXEC = 1;
    public static final int FIONREAD = 0x4004667f;
    public static final int F_DUPFD = 0;
    public static final int F_GETFD = 1;
    public static final int F_GETFL = 3;

    // iOS doesn't support 64-bit fcntl commands.
    public static final int F_GETLK = 7;
    public static final int F_GETLK64 = F_GETLK;

    public static final int F_GETOWN = 5;
    public static final int F_OK = 0;
    public static final int F_RDLCK = 1;
    public static final int F_SETFD = 2;
    public static final int F_SETFL = 4;
    public static final int F_SETOWN = 6;

    // iOS doesn't support 64-bit fcntl commands.
    public static final int F_SETLK = 8;
    public static final int F_SETLK64 = F_SETLK;
    public static final int F_SETLKW = 9;
    public static final int F_SETLKW64 = F_SETLKW;

    public static final int F_UNLCK = 2;
    public static final int F_WRLCK = 3;
    public static final int IFF_LOOPBACK = 0x8;
    public static final int IFF_MULTICAST = 0x8000;
    public static final int IFF_POINTOPOINT = 0x10;
    public static final int IFF_UP = 0x1;
    public static final int IPPROTO_IP = 0;
    public static final int IPPROTO_IPV6 = 41;
    public static final int IPPROTO_TCP = 6;
    public static final int IPV6_MULTICAST_HOPS = 10;
    public static final int IPV6_MULTICAST_IF = 9;
    public static final int IPV6_MULTICAST_LOOP = 11;
    public static final int IPV6_TCLASS = 36;
    public static final int IP_MULTICAST_IF = 9;
    public static final int IP_MULTICAST_LOOP = 11;
    public static final int IP_MULTICAST_TTL = 10;
    public static final int IP_TOS = 3;
    public static final int MAP_FIXED = 0x0010;
    public static final int MAP_PRIVATE = 0x0002;
    public static final int MAP_SHARED = 0x0001;
    public static final int MCAST_JOIN_GROUP = 80;
    public static final int MCAST_LEAVE_GROUP = 81;
    public static final int MCAST_JOIN_SOURCE_GROUP = 82;
    public static final int MCAST_LEAVE_SOURCE_GROUP = 83;
    public static final int MCAST_BLOCK_SOURCE = 84;
    public static final int MCAST_UNBLOCK_SOURCE = 85;
    public static final int MCL_CURRENT = 0x0001;
    public static final int MCL_FUTURE = 0x0002;
    public static final int MSG_OOB = 0x1;
    public static final int MSG_PEEK = 0x2;
    public static final int MS_ASYNC = 0x0001;
    public static final int MS_INVALIDATE = 0x0002;
    public static final int NI_NAMEREQD = 0x00000004;
    public static final int NI_NUMERICHOST = 0x00000002;
    public static final int NI_NUMERICSERV = 0x00000008;
    public static final int MS_SYNC = 0x0010;
    public static final int O_ACCMODE = 0x0003;
    public static final int O_APPEND = 0x0008;
    public static final int O_CREAT = 0x0200;
    public static final int O_EXCL = 0x0800;
    public static final int O_NOCTTY = 0x20000;
    public static final int O_NOFOLLOW = 0x0100;
    public static final int O_NONBLOCK = 0x0004;
    public static final int O_RDONLY = 0x0000;
    public static final int O_RDWR = 0x0002;
    public static final int O_SYNC = 0x0080;
    public static final int O_TRUNC = 0x0400;
    public static final int O_WRONLY = 0x0001;
    public static final int POLLERR = 0x0008;
    public static final int POLLHUP = 0x0010;
    public static final int POLLIN = 0x0001;
    public static final int POLLOUT = 0x0004;
    public static final int PROT_EXEC = 0x0004;
    public static final int PROT_NONE = 0x0000;
    public static final int PROT_READ = 0x0001;
    public static final int PROT_WRITE = 0x0002;
    public static final int R_OK = 0x04;
    public static final int SEEK_CUR = 1;
    public static final int SEEK_END = 2;
    public static final int SEEK_SET = 0;
    public static final int SHUT_RD = 0;
    public static final int SHUT_RDWR = 2;
    public static final int SHUT_WR = 1;
    public static final int SIOCGIFADDR = 0xc0206921;
    public static final int SIOCGIFBRDADDR = 0xc0206923;
    public static final int SIOCGIFDSTADDR = 0xc0206922;
    public static final int SIOCGIFNETMASK = 0xc0206925;
    public static final int SOCK_DGRAM = 2;
    public static final int SOCK_STREAM = 1;
    public static final int SOL_SOCKET = 0xffff;
    public static final int SO_BINDTODEVICE = 25;
    public static final int SO_BROADCAST = 0x0020;
    public static final int SO_ERROR = 0x1007;
    public static final int SO_KEEPALIVE = 0x0008;
    public static final int SO_LINGER = 0x0080;
    public static final int SO_OOBINLINE = 0x0100;
    public static final int SO_RCVBUF = 0x1002;
    public static final int SO_RCVTIMEO = 0x1006;
    public static final int SO_REUSEADDR = 0x0004;
    public static final int SO_SNDBUF = 0x1001;
    public static final int STDERR_FILENO = 2;
    public static final int STDIN_FILENO = 0;
    public static final int STDOUT_FILENO = 1;
    public static final int S_IFBLK = 0060000;
    public static final int S_IFCHR = 0020000;
    public static final int S_IFDIR = 0040000;
    public static final int S_IFIFO = 0010000;
    public static final int S_IFLNK = 0120000;
    public static final int S_IFMT = 0170000;
    public static final int S_IFREG = 0100000;
    public static final int S_IFSOCK = 0140000;
    public static final int S_IRGRP = 0000040;
    public static final int S_IROTH = 0000004;
    public static final int S_IRUSR = 0000400;
    public static final int S_IRWXG = 0000070;
    public static final int S_IRWXO = 0000007;
    public static final int S_IRWXU = 0000700;
    public static final int S_IWGRP = 0000020;
    public static final int S_IWOTH = 0000002;
    public static final int S_IWUSR = 0000200;
    public static final int S_IXGRP = 0000010;
    public static final int S_IXOTH = 0000001;
    public static final int S_IXUSR = 0000100;
    public static final int _SC_PAGESIZE = 29;
    public static final int _SC_PAGE_SIZE = _SC_PAGESIZE;
    public static final int TCP_NODELAY = 0x01;
    public static final int W_OK = 0x02;
    public static final int X_OK = 0x01;

    /*
    public static final int IFF_ALLMULTI = placeholder();
    public static final int IFF_AUTOMEDIA = placeholder();
    public static final int IFF_BROADCAST = placeholder();
    public static final int IFF_DEBUG = placeholder();
    public static final int IFF_DYNAMIC = placeholder();
    public static final int IFF_MASTER = placeholder();
    public static final int IFF_NOARP = placeholder();
    public static final int IFF_NOTRAILERS = placeholder();
    public static final int IFF_POINTOPOINT = placeholder();
    public static final int IFF_PORTSEL = placeholder();
    public static final int IFF_PROMISC = placeholder();
    public static final int IFF_RUNNING = placeholder();
    public static final int IFF_SLAVE = placeholder();
    public static final int IPPROTO_ICMP = placeholder();
    public static final int IPPROTO_RAW = placeholder();
    public static final int IPPROTO_UDP = placeholder();
    public static final int IPV6_CHECKSUM = placeholder();
    public static final int IPV6_RECVDSTOPTS = placeholder();
    public static final int IPV6_RECVHOPLIMIT = placeholder();
    public static final int IPV6_RECVHOPOPTS = placeholder();
    public static final int IPV6_RECVPKTINFO = placeholder();
    public static final int IPV6_RECVRTHDR = placeholder();
    public static final int IPV6_RECVTCLASS = placeholder();
    public static final int IPV6_UNICAST_HOPS = placeholder();
    public static final int IPV6_V6ONLY = placeholder();
    public static final int IP_TTL = placeholder();
    public static final int MSG_CTRUNC = placeholder();
    public static final int MSG_DONTROUTE = placeholder();
    public static final int MSG_EOR = placeholder();
    public static final int MSG_TRUNC = placeholder();
    public static final int MSG_WAITALL = placeholder();
    public static final int NI_DGRAM = placeholder();
    public static final int NI_NOFQDN = placeholder();
    public static final int POLLNVAL = placeholder();
    public static final int POLLPRI = placeholder();
    public static final int POLLRDBAND = placeholder();
    public static final int POLLRDNORM = placeholder();
    public static final int POLLWRBAND = placeholder();
    public static final int POLLWRNORM = placeholder();
    public static final int SIGABRT = placeholder();
    public static final int SIGALRM = placeholder();
    public static final int SIGBUS = placeholder();
    public static final int SIGCHLD = placeholder();
    public static final int SIGCONT = placeholder();
    public static final int SIGFPE = placeholder();
    public static final int SIGHUP = placeholder();
    public static final int SIGILL = placeholder();
    public static final int SIGINT = placeholder();
    public static final int SIGIO = placeholder();
    public static final int SIGKILL = placeholder();
    public static final int SIGPIPE = placeholder();
    public static final int SIGPROF = placeholder();
    public static final int SIGPWR = placeholder();
    public static final int SIGQUIT = placeholder();
    public static final int SIGRTMAX = placeholder();
    public static final int SIGRTMIN = placeholder();
    public static final int SIGSEGV = placeholder();
    public static final int SIGSTKFLT = placeholder();
    public static final int SIGSTOP = placeholder();
    public static final int SIGSYS = placeholder();
    public static final int SIGTERM = placeholder();
    public static final int SIGTRAP = placeholder();
    public static final int SIGTSTP = placeholder();
    public static final int SIGTTIN = placeholder();
    public static final int SIGTTOU = placeholder();
    public static final int SIGURG = placeholder();
    public static final int SIGUSR1 = placeholder();
    public static final int SIGUSR2 = placeholder();
    public static final int SIGVTALRM = placeholder();
    public static final int SIGWINCH = placeholder();
    public static final int SIGXCPU = placeholder();
    public static final int SIGXFSZ = placeholder();
    public static final int SIOCGIFADDR = placeholder();
    public static final int SIOCGIFBRDADDR = placeholder();
    public static final int SIOCGIFDSTADDR = placeholder();
    public static final int SIOCGIFNETMASK = placeholder();
    public static final int SOCK_RAW = placeholder();
    public static final int SOCK_SEQPACKET = placeholder();
    public static final int SO_DEBUG = placeholder();
    public static final int SO_DONTROUTE = placeholder();
    public static final int SO_RCVLOWAT = placeholder();
    public static final int SO_SNDLOWAT = placeholder();
    public static final int SO_SNDTIMEO = placeholder();
    public static final int SO_TYPE = placeholder();
    public static final int S_ISGID = placeholder();
    public static final int S_ISUID = placeholder();
    public static final int S_ISVTX = placeholder();
    public static final int WCONTINUED = placeholder();
    public static final int WEXITED = placeholder();
    public static final int WNOHANG = placeholder();
    public static final int WNOWAIT = placeholder();
    public static final int WSTOPPED = placeholder();
    public static final int WUNTRACED = placeholder();
    public static final int _SC_2_CHAR_TERM = placeholder();
    public static final int _SC_2_C_BIND = placeholder();
    public static final int _SC_2_C_DEV = placeholder();
    public static final int _SC_2_C_VERSION = placeholder();
    public static final int _SC_2_FORT_DEV = placeholder();
    public static final int _SC_2_FORT_RUN = placeholder();
    public static final int _SC_2_LOCALEDEF = placeholder();
    public static final int _SC_2_SW_DEV = placeholder();
    public static final int _SC_2_UPE = placeholder();
    public static final int _SC_2_VERSION = placeholder();
    public static final int _SC_AIO_LISTIO_MAX = placeholder();
    public static final int _SC_AIO_MAX = placeholder();
    public static final int _SC_AIO_PRIO_DELTA_MAX = placeholder();
    public static final int _SC_ARG_MAX = placeholder();
    public static final int _SC_ASYNCHRONOUS_IO = placeholder();
    public static final int _SC_ATEXIT_MAX = placeholder();
    public static final int _SC_AVPHYS_PAGES = placeholder();
    public static final int _SC_BC_BASE_MAX = placeholder();
    public static final int _SC_BC_DIM_MAX = placeholder();
    public static final int _SC_BC_SCALE_MAX = placeholder();
    public static final int _SC_BC_STRING_MAX = placeholder();
    public static final int _SC_CHILD_MAX = placeholder();
    public static final int _SC_CLK_TCK = placeholder();
    public static final int _SC_COLL_WEIGHTS_MAX = placeholder();
    public static final int _SC_DELAYTIMER_MAX = placeholder();
    public static final int _SC_EXPR_NEST_MAX = placeholder();
    public static final int _SC_FSYNC = placeholder();
    public static final int _SC_GETGR_R_SIZE_MAX = placeholder();
    public static final int _SC_GETPW_R_SIZE_MAX = placeholder();
    public static final int _SC_IOV_MAX = placeholder();
    public static final int _SC_JOB_CONTROL = placeholder();
    public static final int _SC_LINE_MAX = placeholder();
    public static final int _SC_LOGIN_NAME_MAX = placeholder();
    public static final int _SC_MAPPED_FILES = placeholder();
    public static final int _SC_MEMLOCK = placeholder();
    public static final int _SC_MEMLOCK_RANGE = placeholder();
    public static final int _SC_MEMORY_PROTECTION = placeholder();
    public static final int _SC_MESSAGE_PASSING = placeholder();
    public static final int _SC_MQ_OPEN_MAX = placeholder();
    public static final int _SC_MQ_PRIO_MAX = placeholder();
    public static final int _SC_NGROUPS_MAX = placeholder();
    public static final int _SC_NPROCESSORS_CONF = placeholder();
    public static final int _SC_NPROCESSORS_ONLN = placeholder();
    public static final int _SC_OPEN_MAX = placeholder();
    public static final int _SC_PASS_MAX = placeholder();
    public static final int _SC_PHYS_PAGES = placeholder();
    public static final int _SC_PRIORITIZED_IO = placeholder();
    public static final int _SC_PRIORITY_SCHEDULING = placeholder();
    public static final int _SC_REALTIME_SIGNALS = placeholder();
    public static final int _SC_RE_DUP_MAX = placeholder();
    public static final int _SC_RTSIG_MAX = placeholder();
    public static final int _SC_SAVED_IDS = placeholder();
    public static final int _SC_SEMAPHORES = placeholder();
    public static final int _SC_SEM_NSEMS_MAX = placeholder();
    public static final int _SC_SEM_VALUE_MAX = placeholder();
    public static final int _SC_SHARED_MEMORY_OBJECTS = placeholder();
    public static final int _SC_SIGQUEUE_MAX = placeholder();
    public static final int _SC_STREAM_MAX = placeholder();
    public static final int _SC_SYNCHRONIZED_IO = placeholder();
    public static final int _SC_THREADS = placeholder();
    public static final int _SC_THREAD_ATTR_STACKADDR = placeholder();
    public static final int _SC_THREAD_ATTR_STACKSIZE = placeholder();
    public static final int _SC_THREAD_DESTRUCTOR_ITERATIONS = placeholder();
    public static final int _SC_THREAD_KEYS_MAX = placeholder();
    public static final int _SC_THREAD_PRIORITY_SCHEDULING = placeholder();
    public static final int _SC_THREAD_PRIO_INHERIT = placeholder();
    public static final int _SC_THREAD_PRIO_PROTECT = placeholder();
    public static final int _SC_THREAD_SAFE_FUNCTIONS = placeholder();
    public static final int _SC_THREAD_STACK_MIN = placeholder();
    public static final int _SC_THREAD_THREADS_MAX = placeholder();
    public static final int _SC_TIMERS = placeholder();
    public static final int _SC_TIMER_MAX = placeholder();
    public static final int _SC_TTY_NAME_MAX = placeholder();
    public static final int _SC_TZNAME_MAX = placeholder();
    public static final int _SC_VERSION = placeholder();
    public static final int _SC_XBS5_ILP32_OFF32 = placeholder();
    public static final int _SC_XBS5_ILP32_OFFBIG = placeholder();
    public static final int _SC_XBS5_LP64_OFF64 = placeholder();
    public static final int _SC_XBS5_LPBIG_OFFBIG = placeholder();
    public static final int _SC_XOPEN_CRYPT = placeholder();
    public static final int _SC_XOPEN_ENH_I18N = placeholder();
    public static final int _SC_XOPEN_LEGACY = placeholder();
    public static final int _SC_XOPEN_REALTIME = placeholder();
    public static final int _SC_XOPEN_REALTIME_THREADS = placeholder();
    public static final int _SC_XOPEN_SHM = placeholder();
    public static final int _SC_XOPEN_UNIX = placeholder();
    public static final int _SC_XOPEN_VERSION = placeholder();
    public static final int _SC_XOPEN_XCU_VERSION = placeholder();
    */

    public static String gaiName(int error) {
        if (error == EAI_AGAIN) {
            return "EAI_AGAIN";
        }
        if (error == EAI_BADFLAGS) {
            return "EAI_BADFLAGS";
        }
        if (error == EAI_FAIL) {
            return "EAI_FAIL";
        }
        if (error == EAI_FAMILY) {
            return "EAI_FAMILY";
        }
        if (error == EAI_MEMORY) {
            return "EAI_MEMORY";
        }
        if (error == EAI_NODATA) {
            return "EAI_NODATA";
        }
        if (error == EAI_NONAME) {
            return "EAI_NONAME";
        }
        if (error == EAI_OVERFLOW) {
            return "EAI_OVERFLOW";
        }
        if (error == EAI_SERVICE) {
            return "EAI_SERVICE";
        }
        if (error == EAI_SOCKTYPE) {
            return "EAI_SOCKTYPE";
        }
        if (error == EAI_SYSTEM) {
            return "EAI_SYSTEM";
        }
        return null;
    }

    public static String errnoName(int errno) {
        if (errno == E2BIG) {
            return "E2BIG";
        }
        if (errno == EACCES) {
            return "EACCES";
        }
        if (errno == EADDRINUSE) {
            return "EADDRINUSE";
        }
        if (errno == EADDRNOTAVAIL) {
            return "EADDRNOTAVAIL";
        }
        if (errno == EAFNOSUPPORT) {
            return "EAFNOSUPPORT";
        }
        if (errno == EAGAIN) {
            return "EAGAIN";
        }
        if (errno == EALREADY) {
            return "EALREADY";
        }
        if (errno == EBADF) {
            return "EBADF";
        }
        if (errno == EBADMSG) {
            return "EBADMSG";
        }
        if (errno == EBUSY) {
            return "EBUSY";
        }
        if (errno == ECANCELED) {
            return "ECANCELED";
        }
        if (errno == ECHILD) {
            return "ECHILD";
        }
        if (errno == ECONNABORTED) {
            return "ECONNABORTED";
        }
        if (errno == ECONNREFUSED) {
            return "ECONNREFUSED";
        }
        if (errno == ECONNRESET) {
            return "ECONNRESET";
        }
        if (errno == EDEADLK) {
            return "EDEADLK";
        }
        if (errno == EDESTADDRREQ) {
            return "EDESTADDRREQ";
        }
        if (errno == EDOM) {
            return "EDOM";
        }
        if (errno == EDQUOT) {
            return "EDQUOT";
        }
        if (errno == EEXIST) {
            return "EEXIST";
        }
        if (errno == EFAULT) {
            return "EFAULT";
        }
        if (errno == EFBIG) {
            return "EFBIG";
        }
        if (errno == EHOSTUNREACH) {
            return "EHOSTUNREACH";
        }
        if (errno == EIDRM) {
            return "EIDRM";
        }
        if (errno == EILSEQ) {
            return "EILSEQ";
        }
        if (errno == EINPROGRESS) {
            return "EINPROGRESS";
        }
        if (errno == EINTR) {
            return "EINTR";
        }
        if (errno == EINVAL) {
            return "EINVAL";
        }
        if (errno == EIO) {
            return "EIO";
        }
        if (errno == EISCONN) {
            return "EISCONN";
        }
        if (errno == EISDIR) {
            return "EISDIR";
        }
        if (errno == ELOOP) {
            return "ELOOP";
        }
        if (errno == EMFILE) {
            return "EMFILE";
        }
        if (errno == EMLINK) {
            return "EMLINK";
        }
        if (errno == EMSGSIZE) {
            return "EMSGSIZE";
        }
        if (errno == EMULTIHOP) {
            return "EMULTIHOP";
        }
        if (errno == ENAMETOOLONG) {
            return "ENAMETOOLONG";
        }
        if (errno == ENETDOWN) {
            return "ENETDOWN";
        }
        if (errno == ENETRESET) {
            return "ENETRESET";
        }
        if (errno == ENETUNREACH) {
            return "ENETUNREACH";
        }
        if (errno == ENFILE) {
            return "ENFILE";
        }
        if (errno == ENOBUFS) {
            return "ENOBUFS";
        }
        if (errno == ENODATA) {
            return "ENODATA";
        }
        if (errno == ENODEV) {
            return "ENODEV";
        }
        if (errno == ENOENT) {
            return "ENOENT";
        }
        if (errno == ENOEXEC) {
            return "ENOEXEC";
        }
        if (errno == ENOLCK) {
            return "ENOLCK";
        }
        if (errno == ENOLINK) {
            return "ENOLINK";
        }
        if (errno == ENOMEM) {
            return "ENOMEM";
        }
        if (errno == ENOMSG) {
            return "ENOMSG";
        }
        if (errno == ENOPROTOOPT) {
            return "ENOPROTOOPT";
        }
        if (errno == ENOSPC) {
            return "ENOSPC";
        }
        if (errno == ENOSR) {
            return "ENOSR";
        }
        if (errno == ENOSTR) {
            return "ENOSTR";
        }
        if (errno == ENOSYS) {
            return "ENOSYS";
        }
        if (errno == ENOTCONN) {
            return "ENOTCONN";
        }
        if (errno == ENOTDIR) {
            return "ENOTDIR";
        }
        if (errno == ENOTEMPTY) {
            return "ENOTEMPTY";
        }
        if (errno == ENOTSOCK) {
            return "ENOTSOCK";
        }
        if (errno == ENOTSUP) {
            return "ENOTSUP";
        }
        if (errno == ENOTTY) {
            return "ENOTTY";
        }
        if (errno == ENXIO) {
            return "ENXIO";
        }
        if (errno == EOPNOTSUPP) {
            return "EOPNOTSUPP";
        }
        if (errno == EOVERFLOW) {
            return "EOVERFLOW";
        }
        if (errno == EPERM) {
            return "EPERM";
        }
        if (errno == EPIPE) {
            return "EPIPE";
        }
        if (errno == EPROTO) {
            return "EPROTO";
        }
        if (errno == EPROTONOSUPPORT) {
            return "EPROTONOSUPPORT";
        }
        if (errno == EPROTOTYPE) {
            return "EPROTOTYPE";
        }
        if (errno == ERANGE) {
            return "ERANGE";
        }
        if (errno == EROFS) {
            return "EROFS";
        }
        if (errno == ESPIPE) {
            return "ESPIPE";
        }
        if (errno == ESRCH) {
            return "ESRCH";
        }
        if (errno == ESTALE) {
            return "ESTALE";
        }
        if (errno == ETIME) {
            return "ETIME";
        }
        if (errno == ETIMEDOUT) {
            return "ETIMEDOUT";
        }
        if (errno == ETXTBSY) {
            return "ETXTBSY";
        }
        if (errno == EWOULDBLOCK) {
            return "EWOULDBLOCK";
        }
        if (errno == EXDEV) {
            return "EXDEV";
        }
        return null;
    }
}
