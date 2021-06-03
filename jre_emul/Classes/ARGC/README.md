Notes by @lukhnos:

The code builds, but it only somewhat worked. Anything that uses Thread crashes.
It seems some thread local object graphs are not properly retained. There was
also one crash involving regex. I suspect those were due to native (JNI) code
not fully covered by the ARGC branch's changes to pointer semantics.

Binary-size wise, the binary size of my app is 42 MiB with the plain jre_emul.a
(all release builds; this is without dead code elimination and I didn't bother
to use jre_core.a). When built with ARGC, it adds about 4 MiB to the binary, so
a 9% increase. I suspect some of that comes from the C++ runtime, which ARGC's
garbage collector needs.

As for the memory footprint, the initial footprint after the app launches is
1 MiB larger when using ARGC (6 MiB vs 5 MiB). I suspect some of the increase
is due to the additional code and the C++ runtime. Footprint delta for sustained
use is unknown due to the crashes.

Performance: due to the crashes, the number of code paths (so user journeys) I
could exercise was very limited. While I didn't see any significant regression
in full-text search, building a full-text search index from scratch (which
involves creating lots of temp objects) was about 50% slower.

In addition, I also had to comment out all the existing @Weak/@WeakOuters, since
those were still translated as `__unsafe_unretained` pointers and would cause the
app to crash (which isn't unexpected, though ideally the ARGC translator would
just ignore those annotations).

Leaks: to my dismay, I still found leaks in the app even with the limited code
paths I could exercise. I thought it could be due to delayed collection, but if
so the collector should still be tracking those objects, and Instruments
wouldn't have indicated those objects as leaked.

There were also other issues. Some translated code needed manual fixing because
at least one unsafe-assign macro was not well-formed, for example.
