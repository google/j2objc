---
title: cycle_finder man page
layout: docs
---

# cycle_finder

The **cycle_finder** tool statically analyzes specified Java source files for strong memory references between objects.

````
cycle_finder [ options ] file1.java ...
````

The following options are supported:

<dl>
<dt>-sourcepath path</dt>
<dd>Specify where to find input source files</dd>

<dt>-classpath path</dt>
<dd>Specify where to find user class files</dd>

<dt>-w, --whitelist file</dt>
<dd>Specifies a <a href="Cycle-Finder-Tool#wiki-whitelisting">whitelist file</a> specifying object references to ignore.</dd>

<dt>--blacklist file</dt>
<dd>When specified, only cycles containing the types and namespaces listed are printed.</dd>

<dt>-Xbootclasspath:path</dt>
<dd>Boot path used by translation (not the tool itself).</dd>

<dt>-version</dt>
<dd>Version information</dd>

<dt>-h, --help</dt>
<dd>Print this message.</dd>
</dl>

## See Also

[Cycle Finder Tool](Cycle-Finder-Tool.html)
